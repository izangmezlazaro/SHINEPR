// ============================================
// Shine - Shopping Cart Integration
// ============================================

(function () {
  const TAX_RATE = 0.10;
  const FALLBACK_IMAGE = 'assets/img/product-perfume.png';
  const CART_CACHE_KEY = 'shine:carrito:v1';
  const CART_CACHE_TTL = 30 * 1000;
  let ultimoCarrito = null;

  function getUsuarioId() {
    // Try window.ID_USUARIO first (set by api.js and updated by auth.js on login)
    const fromWindow = window.ID_USUARIO;
    if (fromWindow && Number.isFinite(Number(fromWindow))) return Number(fromWindow);
    const fromStorage = Number(localStorage.getItem('shineUserId'));
    return Number.isFinite(fromStorage) && fromStorage > 0 ? fromStorage : null;
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(Number(value || 0));
  }

  function getCartEndpoint() {
    const idUsuario = getUsuarioId();
    if (!idUsuario) return null;
    return `/carrito`;
  }

  function leerCarritoCache() {
    try {
      const cached = JSON.parse(sessionStorage.getItem(CART_CACHE_KEY));
      if (!cached?.timestamp || !cached.data) return null;
      if (Date.now() - cached.timestamp > CART_CACHE_TTL) return null;
      return cached.data;
    } catch (error) {
      return null;
    }
  }

  function guardarCarritoCache(carrito) {
    try {
      sessionStorage.setItem(CART_CACHE_KEY, JSON.stringify({
        timestamp: Date.now(),
        data: carrito
      }));
    } catch (error) {
      // Cache is an optimization only.
    }
  }

  function toApiId(value) {
    if (value === undefined || value === null || value === '') return null;
    const numeric = Number(value);
    if (Number.isFinite(numeric)) return numeric;

    const match = String(value).match(/\d+/);
    return match ? Number(match[0]) : null;
  }

  function getItems(carrito) {
    return Array.isArray(carrito?.items) ? carrito.items : [];
  }

  function getItemId(item) {
    return item.id ?? item.idItem ?? item.idCarritoItem;
  }

  function getItemName(item) {
    return item.nombre || item.name || 'Producto Shine';
  }

  function getItemQty(item) {
    return Number(item.cantidad ?? item.qty ?? 1);
  }

  function getItemUnitPrice(item) {
    return Number(item.precioUnitario ?? item.precio ?? item.price ?? 0);
  }

  function getItemSubtotal(item) {
    const subtotal = item.subtotal ?? item.total;
    return subtotal !== undefined && subtotal !== null
      ? Number(subtotal)
      : getItemUnitPrice(item) * getItemQty(item);
  }

  function getCartSubtotal(carrito) {
    if (carrito?.total !== undefined && carrito?.total !== null) {
      return Number(carrito.total);
    }

    return getItems(carrito).reduce((sum, item) => sum + getItemSubtotal(item), 0);
  }

  function updateCartBadge(carrito) {
    const count = getItems(carrito).reduce((sum, item) => sum + getItemQty(item), 0);

    document.querySelectorAll('.nav-cta').forEach(btn => {
      btn.querySelector('.cart-count')?.remove();

      if (count > 0) {
        const badge = document.createElement('span');
        badge.className = 'cart-count';
        badge.textContent = String(count);
        btn.appendChild(badge);
      }
    });
  }

  function calcularPuntosAGanar(total) {
    return Math.round(total * 10);
  }

  function updateSummary(carrito) {
    const subtotal = getCartSubtotal(carrito);
    const tax = subtotal * TAX_RATE;
    const total = subtotal + tax;

    const subtotalEl = document.getElementById('summarySubtotal');
    const taxEl = document.getElementById('summaryTax');
    const totalEl = document.getElementById('summaryTotal');
    const pointsEl = document.getElementById('summaryPoints');

    if (subtotalEl) subtotalEl.textContent = formatCurrency(subtotal);
    if (taxEl) taxEl.textContent = formatCurrency(tax);
    if (totalEl) totalEl.textContent = formatCurrency(total);

    if (pointsEl) {
      const puntos = calcularPuntosAGanar(subtotal);
      pointsEl.textContent = puntos > 0 ? `+${puntos} pts` : '0 pts';
    }
  }

  function showToast(message) {
    if (typeof window.showToast === 'function') {
      window.showToast(message);
      return;
    }

    document.querySelector('.shine-toast')?.remove();
    const toast = document.createElement('div');
    toast.className = 'shine-toast';
    toast.textContent = message;
    document.body.appendChild(toast);
    requestAnimationFrame(() => requestAnimationFrame(() => toast.classList.add('show')));
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 400);
    }, 2500);
  }

  function setCheckoutEnabled(enabled) {
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) checkoutBtn.disabled = !enabled;
  }

  function renderEmptyCart(container) {
    container.innerHTML = `
      <div class="cart-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="display:block;margin:0 auto var(--sp-md)">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.99-1.61L23 6H6"/>
        </svg>
        <p>Your cart is empty.</p>
        <a href="shop.html" class="btn btn--primary">Continue Shopping</a>
      </div>
    `;
  }

  function renderCart(carrito) {
    ultimoCarrito = carrito;
    guardarCarritoCache(carrito);
    updateCartBadge(carrito);
    updateSummary(carrito);

    const container = document.getElementById('cartItemsList');
    if (!container) return;

    const items = getItems(carrito);
    setCheckoutEnabled(items.length > 0);

    if (!items.length) {
      renderEmptyCart(container);
      return;
    }

    container.innerHTML = items.map(item => {
      const id = getItemId(item);
      const isCustom = Boolean(item.idPerfCust);
      const name = getItemName(item);
      const unitPrice = getItemUnitPrice(item);
      const qty = getItemQty(item);

      return `
        <div class="cart-item" data-cart-item="${escapeHtml(id)}">
          <img class="cart-item__img" src="${FALLBACK_IMAGE}" alt="${escapeHtml(name)}">
          <div class="cart-item__info">
            <div class="cart-item__name">${escapeHtml(name)}</div>
            <div class="cart-item__price">
              ${escapeHtml(formatCurrency(unitPrice))}
              ${isCustom ? '<span class="text-sm text-muted"> · Custom perfume</span>' : ''}
            </div>
          </div>
          <div class="qty-selector" aria-label="Quantity">
            <span>${escapeHtml(qty)}</span>
          </div>
          <button class="cart-item__remove" type="button" aria-label="Remove item">×</button>
        </div>
      `;
    }).join('') + `<a href="shop.html" class="btn btn--outline mt-xl">Continue Shopping</a>`;
  }

  function renderCartError(message) {
    const container = document.getElementById('cartItemsList');
    if (container) {
      container.innerHTML = `
        <div class="cart-empty">
          <p>${escapeHtml(message)}</p>
          <a href="shop.html" class="btn btn--primary">Continue Shopping</a>
        </div>
      `;
    }
    updateSummary({ items: [], total: 0 });
    setCheckoutEnabled(false);
  }

  function renderLoginRequired(container) {
    if (!container) return;
    container.innerHTML = `
      <div class="cart-empty">
        <p>Please sign in to view your cart.</p>
        <a href="login.html" class="btn btn--primary">Sign In</a>
      </div>
    `;
    updateSummary({ items: [], total: 0 });
    setCheckoutEnabled(false);
  }

  async function cargarCarrito() {
    if (!window.ShineAPI) {
      renderCartError('Could not initialize the connection with the API.');
      return null;
    }

    const endpoint = getCartEndpoint();
    if (!endpoint) {
      // Not logged in
      const container = document.getElementById('cartItemsList');
      if (container) renderLoginRequired(container);
      updateCartBadge({ items: [] });
      return null;
    }

    try {
      const carrito = await window.ShineAPI.get(endpoint);
      renderCart(carrito);
      return carrito;
    } catch (error) {
      renderCartError('Could not load the cart. Make sure the backend is running.');
      return null;
    }
  }

  async function agregarItemCarrito({ idProducto = null, idPerfCust = null, cantidad = 1 }) {
    if (!idProducto && !idPerfCust) {
      throw new Error('Must provide idProducto or idPerfCust');
    }

    const endpoint = getCartEndpoint();
    if (!endpoint) {
      window.location.href = 'login.html';
      throw new Error('Please sign in to add products to your cart.');
    }

    const body = {
      idProducto: toApiId(idProducto),
      idPerfCust: toApiId(idPerfCust),
      cantidad: Number(cantidad) || 1
    };

    const carrito = await window.ShineAPI.post(`${endpoint}/items`, body);
    renderCart(carrito);
    return carrito;
  }

  async function añadirAlCarrito(idProducto, cantidad = 1) {
    try {
      const carrito = await agregarItemCarrito({ idProducto, cantidad });
      showToast('Product added to cart');
      return carrito;
    } catch (error) {
      showToast(error.message || 'Could not add to cart');
      throw error;
    }
  }

  async function añadirPerfumeCustomAlCarrito(idPerfCust, cantidad = 1) {
    try {
      const carrito = await agregarItemCarrito({ idPerfCust, cantidad });
      showToast('Custom fragrance added to cart');
      return carrito;
    } catch (error) {
      showToast(error.message || 'Could not add to cart');
      throw error;
    }
  }

  async function eliminarItemCarrito(idItem) {
    const endpoint = getCartEndpoint();
    if (!endpoint) return;
    try {
      await window.ShineAPI.delete(`${endpoint}/items/${idItem}`);
      const carrito = await cargarCarrito();
      showToast('Product removed from cart');
      return carrito;
    } catch (error) {
      showToast(error.message || 'Could not remove the product');
      throw error;
    }
  }

  function setupCatalogAddButtons() {
    document.addEventListener('click', async event => {
      const button = event.target.closest('.product-card__action');
      const card = event.target.closest('.product-card[data-id]');
      if (!button || !card) return;

      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      button.disabled = true;
      try {
        await añadirAlCarrito(card.dataset.id, 1);
        button.classList.add('added');
        setTimeout(() => button.classList.remove('added'), 1000);
      } finally {
        button.disabled = false;
      }
    }, true);
  }

  function setupCartPageActions() {
    const container = document.getElementById('cartItemsList');
    if (!container) return;

    container.addEventListener('click', event => {
      const removeButton = event.target.closest('.cart-item__remove');
      const item = event.target.closest('[data-cart-item]');
      if (!removeButton || !item) return;

      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      eliminarItemCarrito(item.dataset.cartItem);
    }, true);
  }

  function scheduleBadgeRefresh() {
    const cached = leerCarritoCache();
    if (cached) updateCartBadge(cached);

    const refresh = () => cargarCarrito();
    if ('requestIdleCallback' in window) {
      window.requestIdleCallback(refresh, { timeout: 2000 });
    } else {
      setTimeout(refresh, 800);
    }
  }

  window.cargarCarrito = cargarCarrito;
  window.renderizarCarrito = renderCart;
  window.obtenerCarritoActual = () => ultimoCarrito;
  window.añadirAlCarrito = añadirAlCarrito;
  window.anadirAlCarrito = añadirAlCarrito;
  window.añadirPerfumeCustomAlCarrito = añadirPerfumeCustomAlCarrito;
  window.anadirPerfumeCustomAlCarrito = añadirPerfumeCustomAlCarrito;
  window.eliminarItemCarrito = eliminarItemCarrito;

  document.addEventListener('DOMContentLoaded', () => {
    setupCatalogAddButtons();
    setupCartPageActions();

    if (document.getElementById('cartItemsList')) {
      cargarCarrito();
    } else {
      scheduleBadgeRefresh();
    }
  });
})();
