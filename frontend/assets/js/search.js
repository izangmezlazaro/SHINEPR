// ============================================
// Shine - Header Search
// ============================================
(function () {
  const FALLBACK_IMAGE = 'assets/img/product-perfume.png';
  const API_BASE = 'http://localhost:8080/api/v1';
  const CACHE_KEY = 'shine:productos:v2';

  let allProducts = [];
  let searchTimeout = null;

  // ── Bootstrap ─────────────────────────────
  function init() {
    const headerInner = document.querySelector('.header-inner');
    if (!headerInner) return;

    // Inject search button into nav area
    const searchBtn = document.createElement('button');
    searchBtn.className = 'header-search-btn';
    searchBtn.id = 'headerSearchBtn';
    searchBtn.setAttribute('aria-label', 'Search products');
    searchBtn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`;

    const hamburger = headerInner.querySelector('.hamburger');
    headerInner.insertBefore(searchBtn, hamburger || null);

    // Inject search panel below header
    const panel = document.createElement('div');
    panel.className = 'header-search-panel';
    panel.id = 'headerSearchPanel';
    panel.setAttribute('aria-hidden', 'true');
    panel.innerHTML = `
      <div class="header-search-bar container">
        <div class="header-search-field">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input
            type="search"
            class="header-search-input"
            id="headerSearchInput"
            placeholder="Search perfumes, skincare…"
            autocomplete="off"
            spellcheck="false"
            aria-label="Search products"
          >
          <button class="header-search-clear" id="headerSearchClear" aria-label="Clear search" tabindex="-1">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </div>
      <div class="header-search-results-wrap container">
        <div class="header-search-results" id="headerSearchResults" role="listbox"></div>
      </div>
    `;

    const siteHeader = document.querySelector('.site-header');
    siteHeader.appendChild(panel);

    // Backdrop
    const backdrop = document.createElement('div');
    backdrop.className = 'header-search-backdrop';
    backdrop.id = 'headerSearchBackdrop';
    document.body.appendChild(backdrop);

    // Events
    searchBtn.addEventListener('click', openSearch);
    document.getElementById('headerSearchClear').addEventListener('click', () => {
      document.getElementById('headerSearchInput').value = '';
      document.getElementById('headerSearchResults').innerHTML = '';
      document.getElementById('headerSearchInput').focus();
    });
    document.getElementById('headerSearchInput').addEventListener('input', onInput);
    document.getElementById('headerSearchInput').addEventListener('keydown', onKeydown);
    backdrop.addEventListener('click', closeSearch);
    document.addEventListener('keydown', e => { if (e.key === 'Escape') closeSearch(); });

    loadProducts();
  }

  // ── Open / Close ───────────────────────────
  function openSearch() {
    const panel = document.getElementById('headerSearchPanel');
    const backdrop = document.getElementById('headerSearchBackdrop');
    panel.classList.add('open');
    panel.setAttribute('aria-hidden', 'false');
    backdrop.classList.add('open');
    document.body.classList.add('search-open');
    setTimeout(() => document.getElementById('headerSearchInput').focus(), 60);
  }

  function closeSearch() {
    const panel = document.getElementById('headerSearchPanel');
    const backdrop = document.getElementById('headerSearchBackdrop');
    panel.classList.remove('open');
    panel.setAttribute('aria-hidden', 'true');
    backdrop.classList.remove('open');
    document.body.classList.remove('search-open');
    document.getElementById('headerSearchInput').value = '';
    document.getElementById('headerSearchResults').innerHTML = '';
  }

  // ── Input handling ─────────────────────────
  function onInput(e) {
    clearTimeout(searchTimeout);
    const q = e.target.value.trim();
    if (!q) { document.getElementById('headerSearchResults').innerHTML = ''; return; }
    searchTimeout = setTimeout(() => runSearch(q), 140);
  }

  function onKeydown(e) {
    const results = document.querySelectorAll('.search-result-item');
    if (!results.length) return;
    const focused = document.querySelector('.search-result-item.focused');
    let idx = Array.from(results).indexOf(focused);

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      focused?.classList.remove('focused');
      results[Math.min(idx + 1, results.length - 1)]?.classList.add('focused');
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      focused?.classList.remove('focused');
      results[Math.max(idx - 1, 0)]?.classList.add('focused');
    } else if (e.key === 'Enter') {
      const active = document.querySelector('.search-result-item.focused');
      if (active) { active.click(); }
    }
  }

  // ── Search logic ───────────────────────────
  function normalize(str) {
    return String(str || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
  }

  function scoreProduct(p, words) {
    const fields = [
      { text: normalize(p.nombre),            weight: 4 },
      { text: normalize(p.tipoFragancia),      weight: 2 },
      { text: normalize(p.categoria?.nombre),  weight: 2 },
      { text: normalize(p.descripcion),        weight: 1 },
    ];
    let score = 0;
    let matched = 0;
    for (const word of words) {
      let hit = false;
      for (const { text, weight } of fields) {
        if (text.includes(word)) { score += weight; hit = true; }
      }
      if (hit) matched++;
    }
    if (matched === 0) return 0;
    if (matched === words.length) score += 5;
    return score;
  }

  function runSearch(q) {
    const words = normalize(q).split(/\s+/).filter(Boolean);
    if (!words.length) { renderResults([], q); return; }

    const scored = allProducts
      .map(p => ({ p, score: scoreProduct(p, words) }))
      .filter(({ score }) => score > 0)
      .sort((a, b) => b.score - a.score)
      .slice(0, 8)
      .map(({ p }) => p);

    renderResults(scored, q);
  }

  // ── Render ─────────────────────────────────
  function escHtml(v) {
    return String(v ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  function highlight(text, q) {
    const safe = escHtml(text);
    const words = q.trim().split(/\s+/).filter(Boolean);
    if (!words.length) return safe;
    const pattern = words.map(w => w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|');
    return safe.replace(new RegExp(`(${pattern})`, 'gi'), '<mark class="search-mark">$1</mark>');
  }

  function getImage(p) {
    const url = String(p.imagenes?.[0]?.url || '').trim();
    if (!url) return FALLBACK_IMAGE;
    return url;
  }

  function renderResults(results, q) {
    const container = document.getElementById('headerSearchResults');

    if (!results.length) {
      container.innerHTML = `<div class="search-empty">No results for "<strong>${escHtml(q)}</strong>"</div>`;
      return;
    }

    container.innerHTML = results.map((p, i) => {
      const img   = getImage(p);
      const name  = p.nombre || 'Producto Shine';
      const tipo  = p.tipoFragancia || '';
      const cat   = p.categoria?.nombre || '';
      const precio = Number(p.precio || 0).toLocaleString('es-ES', { style: 'currency', currency: 'EUR' });

      // Parse name: try to split "Brand / Product" style names
      // e.g. "Perfume Hombre M043" → show as-is; later can be enhanced
      const badge = tipo || cat;

      return `
        <a class="search-result-item" href="product.html?id=${p.idProducto}" role="option" aria-selected="false" tabindex="-1">
          <div class="sri-img-wrap">
            <img class="sri-img" src="${escHtml(img)}" alt="${escHtml(name)}" loading="lazy" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'">
          </div>
          <div class="sri-body">
            <span class="sri-name">${highlight(name, q)}</span>
            ${badge ? `<span class="sri-badge">${escHtml(badge)}</span>` : ''}
          </div>
          <span class="sri-price">${escHtml(precio)}</span>
        </a>
      `;
    }).join('');

    container.querySelectorAll('.search-result-item').forEach(el => {
      el.addEventListener('click', () => setTimeout(closeSearch, 80));
    });
  }

  // ── Data loading ───────────────────────────
  async function loadProducts() {
    try {
      const cached = JSON.parse(localStorage.getItem(CACHE_KEY));
      if (cached?.data && Array.isArray(cached.data)) allProducts = cached.data;
    } catch (_) {}

    try {
      const res = await fetch(`${API_BASE}/productos`, {
        headers: { Accept: 'application/json' },
        credentials: 'include'
      });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          allProducts = data;
          try {
            localStorage.setItem(CACHE_KEY, JSON.stringify({ timestamp: Date.now(), data }));
          } catch (_) {}
        }
      }
    } catch (_) {}
  }

  document.addEventListener('DOMContentLoaded', init);
})();
