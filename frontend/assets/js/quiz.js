// ============================================
// Shine — Beauty Quiz Logic v2
// ============================================

(function () {
  const FALLBACK_IMG = 'assets/img/product-bodyoil.png';
  const answers = {};

  // ── Quiz Navigation ───────────────────────
  function initQuiz() {
    const steps = document.querySelectorAll('.qz-step');
    const fill = document.getElementById('progressFill');
    const dots = document.querySelectorAll('.qz-dot');
    const tagEl = document.getElementById('visualTag');
    const total = steps.length;

    function goToStep(num) {
      steps.forEach(s => s.classList.remove('active'));
      const next = document.querySelector(`[data-step="${num}"]`);
      if (!next) return;
      next.classList.add('active');
      fill.style.width = ((num - 1) / total * 100) + '%';

      // Update visual panel dots
      dots.forEach((d, i) => {
        d.classList.toggle('active', i === num - 1);
      });

      // Update visual panel tagline
      if (tagEl && next.dataset.tag) {
        tagEl.style.opacity = 0;
        setTimeout(() => {
          tagEl.textContent = next.dataset.tag;
          tagEl.style.opacity = 1;
        }, 250);
      }
    }

    document.querySelectorAll('.qz-opt').forEach(btn => {
      btn.addEventListener('click', () => {
        const parent = btn.closest('.qz-opts');
        const key = parent.dataset.key;
        parent.querySelectorAll('.qz-opt').forEach(o => o.classList.remove('selected'));
        btn.classList.add('selected');
        answers[key] = btn.dataset.value;

        setTimeout(() => {
          const stepNum = parseInt(btn.closest('.qz-step').dataset.step);
          if (stepNum < total) {
            goToStep(stepNum + 1);
          } else {
            fill.style.width = '100%';
            showKit();
          }
        }, 450);
      });
    });
  }

  // ── Product Fetching ──────────────────────
  async function fetchProducts() {
    try {
      const cached = JSON.parse(localStorage.getItem('shine:productos:v2'));
      if (cached?.data?.length) return cached.data;
    } catch (_) {}
    if (window.ShineAPI) {
      try {
        const data = await window.ShineAPI.get('/productos');
        if (Array.isArray(data) && data.length) return data;
      } catch (_) {}
    }
    return [];
  }

  function getCategoryName(cat) {
    const id = cat?.idCategoria ?? cat?.id;
    const map = { 1: 'skincare', 2: 'fragrance', 3: 'supplements', 5: 'makeup', 7: 'haircare' };
    return map[id] || 'other';
  }

  function getCategoryLabel(key) {
    const map = { skincare: 'Skincare', fragrance: 'Fragrance', haircare: 'Hair Care', supplements: 'Wellness', makeup: 'Makeup' };
    return map[key] || 'Beauty';
  }

  function getProductImage(p) {
    if (Array.isArray(p.imagenes) && p.imagenes[0]?.url) {
      const url = p.imagenes[0].url;
      return url.startsWith('/') ? `http://localhost:8080${url}` : url;
    }
    return FALLBACK_IMG;
  }

  function fmt(n) {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(n);
  }

  // ── Kit Building ──────────────────────────
  function buildKit(products) {
    const byCategory = {};
    products.forEach(p => {
      const cat = getCategoryName(p.categoria);
      if (!byCategory[cat]) byCategory[cat] = [];
      byCategory[cat].push(p);
    });

    const pick = (cat) => {
      const pool = byCategory[cat];
      if (!pool?.length) return null;
      const featured = pool.find(p => p.badge || p.etiqueta || p.destacado || p.bestSeller);
      return featured || pool[Math.floor(Math.random() * pool.length)];
    };

    const kit = [];
    const tryPush = (cat) => {
      const p = pick(cat);
      if (p) kit.push({ ...p, _cat: cat });
    };

    tryPush('skincare');
    tryPush('fragrance');
    tryPush('haircare');
    tryPush('supplements');
    if (kit.length < 4) tryPush('makeup');

    // Fill remaining from any category
    if (kit.length < 4) {
      const usedIds = new Set(kit.map(p => p.idProducto));
      for (const arr of Object.values(byCategory)) {
        for (const p of arr) {
          if (kit.length >= 4) break;
          if (!usedIds.has(p.idProducto)) {
            kit.push({ ...p, _cat: getCategoryName(p.categoria) });
            usedIds.add(p.idProducto);
          }
        }
      }
    }
    return kit;
  }

  // ── Show Kit ──────────────────────────────
  async function showKit() {
    const quizPhase = document.getElementById('quizPhase');
    const kitPhase = document.getElementById('kitPhase');
    const container = document.getElementById('kitProducts');
    const totalEl = document.getElementById('kitTotalPrice');

    quizPhase.style.display = 'none';
    kitPhase.style.display = 'block';
    container.innerHTML = '<div class="kit-loading"><div class="spinner"></div>Building your kit…</div>';

    const raw = await fetchProducts();
    const kit = buildKit(raw);

    if (!kit.length) {
      container.innerHTML = '<div class="kit-loading">No products available. <a href="shop.html" style="color:var(--rose)">Browse the shop</a></div>';
      return;
    }

    let total = 0;
    container.innerHTML = kit.map(p => {
      const price = Number(p.precio) || 0;
      total += price;
      return `<div class="kit-card" data-id="${p.idProducto}">
        <img class="kit-card-img" src="${getProductImage(p)}" alt="${p.nombre || ''}" onerror="this.src='${FALLBACK_IMG}'">
        <div>
          <div class="kit-card-cat">${getCategoryLabel(p._cat)}</div>
          <div class="kit-card-name">${p.nombre || 'Shine Product'}</div>
        </div>
        <div class="kit-card-price">${fmt(price)}</div>
      </div>`;
    }).join('');

    totalEl.textContent = fmt(total);
    window._shineKit = kit;
  }

  // ── Accept Kit ────────────────────────────
  function setupAcceptKit() {
    const btn = document.getElementById('acceptKit');
    if (!btn) return;

    btn.addEventListener('click', async () => {
      btn.innerHTML = 'Adding…';
      btn.disabled = true;

      const kit = window._shineKit || [];
      const userId = localStorage.getItem('shineUserId');

      for (const product of kit) {
        try {
          if (userId && window.ShineAPI) {
            await window.ShineAPI.post('/carrito', {
              idUsuario: parseInt(userId),
              idProducto: product.idProducto,
              cantidad: 1
            });
          } else {
            const guest = JSON.parse(localStorage.getItem('shineGuestCart') || '[]');
            const ex = guest.find(i => i.id === product.idProducto);
            if (ex) ex.qty += 1;
            else guest.push({ id: product.idProducto, qty: 1 });
            localStorage.setItem('shineGuestCart', JSON.stringify(guest));
          }
        } catch (_) {}
      }
      window.location.href = 'cart.html';
    });
  }

  // ── Init ──────────────────────────────────
  document.addEventListener('DOMContentLoaded', () => {
    initQuiz();
    setupAcceptKit();
  });
})();
