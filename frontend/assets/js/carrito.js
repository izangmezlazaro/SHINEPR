// ============================================
// Shine - Shopping Cart Integration
// ============================================

(function () {
  const TAX_RATE = 0.10;
  const FALLBACK_IMAGE = 'assets/img/product-bodyoil.png';
  const CART_CACHE_KEY = 'shine:carrito:v1';
  const CART_CACHE_TTL = 30 * 1000;
  const GUEST_CART_KEY = 'shineGuestCart';
  let ultimoCarrito = null;

  function getUsuarioId() {
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

  // ---- Guest Cart (localStorage) ----

  function leerCarritoInvitado() {
    try {
      return JSON.parse(localStorage.getItem(GUEST_CART_KEY)) || [];
    } catch (_) { return []; }
  }

  function guardarCarritoInvitado(items) {
    try { localStorage.setItem(GUEST_CART_KEY, JSON.stringify(items)); } catch (_) {}
  }

  function carritoInvitadoADTO(items) {
    return {
      items: items.map((item, i) => ({
        id: `guest-${i}`,
        nombre: item.nombre || item.perfumeName || 'Custom Fragrance',
        precioUnitario: Number(item.precio ?? item.precioCalculado ?? 0),
        cantidad: Number(item.cantidad ?? 1),
        imagenUrl: item.imagenUrl || FALLBACK_IMAGE,
        idPerfCust: item.type === 'custom' ? `guest-${i}` : null,
        _guestIdx: i
      }))
    };
  }

  async function sincronizarCarritoInvitado() {
    const items = leerCarritoInvitado();
    if (!items.length) return;

    for (const item of items) {
      try {
        if (item.type === 'custom' && item.payload) {
          const perfume = await window.ShineAPI.post('/perfumes-custom', {
            ...item.payload,
            idUsuario: getUsuarioId()
          });
          const idPerfCust = perfume.idPerfCust ?? perfume.id;
          if (idPerfCust) {
            await window.ShineAPI.post('/carrito/items', {
              idPerfCust: Number(idPerfCust),
              idProducto: null,
              cantidad: Number(item.cantidad || 1)
            });
          }
        } else if (item.type === 'product' && item.idProducto) {
          await window.ShineAPI.post('/carrito/items', {
            idProducto: Number(item.idProducto),
            idPerfCust: null,
            cantidad: Number(item.cantidad || 1)
          });
        }
      } catch (_) {}
    }

    guardarCarritoInvitado([]);
    sessionStorage.removeItem(CART_CACHE_KEY);
  }

  // ---- Server Cart Cache ----

  function leerCarritoCache() {
    try {
      const cached = JSON.parse(sessionStorage.getItem(CART_CACHE_KEY));
      if (!cached?.timestamp || !cached.data) return null;
      if (Date.now() - cached.timestamp > CART_CACHE_TTL) return null;
      return cached.data;
    } catch (_) {
      return null;
    }
  }

  function guardarCarritoCache(carrito) {
    try {
      sessionStorage.setItem(CART_CACHE_KEY, JSON.stringify({
        timestamp: Date.now(),
        data: carrito
      }));
    } catch (_) {}
  }

  // ---- Helpers ----

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

  function renderCartItems(items, isGuest) {
    return items.map(item => {
      const id = isGuest ? `guest-${item._guestIdx}` : getItemId(item);
      const isCustom = isGuest ? item.idPerfCust !== null : Boolean(item.idPerfCust);
      const productId = isGuest ? item.idProducto : item.idProducto;
      const name = isGuest ? item.nombre : getItemName(item);
      const unitPrice = isGuest ? item.precioUnitario : getItemUnitPrice(item);
      const qty = isGuest ? item.cantidad : getItemQty(item);
      let imgSrc = item.imagenUrl || FALLBACK_IMAGE;
      
      if (!isGuest && item.idPerfCust) {
        const cachedImg = sessionStorage.getItem('custom_img_' + item.idPerfCust);
        if (cachedImg) imgSrc = cachedImg;
      }

      const isLinkable = !isCustom && productId;
      const linkStart = isLinkable ? `<a href="product.html?id=${productId}" style="color:inherit; text-decoration:none; outline:none; cursor:pointer;">` : '';
      const linkEnd = isLinkable ? `</a>` : '';
      const imgLinkStart = isLinkable ? `<a href="product.html?id=${productId}" style="display:contents;">` : '';
      const imgLinkEnd = isLinkable ? `</a>` : '';

      return `
        <div class="cart-item" data-cart-item="${escapeHtml(String(id))}">
          ${imgLinkStart}
          <img class="cart-item__img" src="${imgSrc}" alt="${escapeHtml(name)}" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'" ${isLinkable ? 'style="cursor:pointer;"' : ''}>
          ${imgLinkEnd}
          <div class="cart-item__info">
            ${linkStart}<div class="cart-item__name" ${isLinkable ? 'style="cursor:pointer;"' : ''}>${escapeHtml(name)}</div>${linkEnd}
            <div class="cart-item__price">
              ${escapeHtml(formatCurrency(unitPrice))}
              ${isCustom ? '<span class="text-sm text-muted"> · Custom perfume</span>' : ''}
            </div>
          </div>
          <div class="qty-selector" aria-label="Quantity">
            <button class="qty-btn qty-minus" type="button" aria-label="Decrease quantity">−</button>
            <input class="qty-input" type="number" min="1" value="${escapeHtml(String(qty))}" aria-label="Quantity">
            <button class="qty-btn qty-plus" type="button" aria-label="Increase quantity">+</button>
          </div>
          <button class="cart-item__remove" type="button" aria-label="Remove item">×</button>
        </div>
      `;
    }).join('');
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

    container.innerHTML = renderCartItems(items, false) +
      `<a href="shop.html" class="btn btn--outline mt-xl">Continue Shopping</a>`;
  }

  function renderCarritoInvitado() {
    const guestItems = leerCarritoInvitado();
    const dto = carritoInvitadoADTO(guestItems);
    updateCartBadge(dto);
    updateSummary(dto);

    const container = document.getElementById('cartItemsList');
    if (!container) return;

    if (!guestItems.length) {
      renderEmptyCart(container);
      setCheckoutEnabled(false);
      return;
    }

    container.innerHTML = renderCartItems(dto.items, true) +
      `<a href="shop.html" class="btn btn--outline mt-xl">Continue Shopping</a>`;
    setCheckoutEnabled(true);
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

  async function cargarCarrito() {
    if (!window.ShineAPI) {
      renderCartError('Could not initialize the connection with the API.');
      return null;
    }

    const idUsuario = getUsuarioId();

    if (!idUsuario) {
      // Guest: render guest cart
      const container = document.getElementById('cartItemsList');
      if (container) {
        renderCarritoInvitado();
      } else {
        // Non-cart page: just update badge
        const guestItems = leerCarritoInvitado();
        if (guestItems.length > 0) {
          updateCartBadge(carritoInvitadoADTO(guestItems));
        }
      }
      return null;
    }

    // Logged in: sync guest cart first if any
    const guestItems = leerCarritoInvitado();
    if (guestItems.length > 0) {
      try {
        await sincronizarCarritoInvitado();
      } catch (_) {
        guardarCarritoInvitado([]);
      }
    }

    try {
      const carrito = await window.ShineAPI.get('/carrito');
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

    const body = {
      idProducto: toApiId(idProducto),
      idPerfCust: toApiId(idPerfCust),
      cantidad: Number(cantidad) || 1
    };

    const carrito = await window.ShineAPI.post('/carrito/items', body);
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

  async function actualizarCantidadItem(idItem, nuevaCantidad) {
    try {
      const carrito = await window.ShineAPI.put(`/carrito/items/${idItem}`, { cantidad: nuevaCantidad });
      renderCart(carrito);
    } catch (error) {
      showToast(error.message || 'Could not update quantity');
      cargarCarrito();
    }
  }

  async function eliminarItemCarrito(idItem) {
    try {
      await window.ShineAPI.delete(`/carrito/items/${idItem}`);
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
        if (!getUsuarioId()) {
          // Guest: add to guest cart
          const idProducto = Number(card.dataset.id);
          const guestItems = leerCarritoInvitado();
          const existing = guestItems.find(i => i.type === 'product' && i.idProducto === idProducto);
          if (existing) {
            existing.cantidad++;
          } else {
            guestItems.push({
              type: 'product',
              idProducto,
              nombre: card.dataset.name || 'Shine Product',
              precio: Number(card.dataset.price) || 0,
              imagenUrl: card.dataset.img || FALLBACK_IMAGE,
              cantidad: 1
            });
          }
          guardarCarritoInvitado(guestItems);
          updateCartBadge(carritoInvitadoADTO(guestItems));
          showToast('Product added to cart');
          button.classList.add('added');
          setTimeout(() => button.classList.remove('added'), 1000);
        } else {
          await añadirAlCarrito(card.dataset.id, 1);
          button.classList.add('added');
          setTimeout(() => button.classList.remove('added'), 1000);
        }
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
      const minusButton  = event.target.closest('.qty-minus');
      const plusButton   = event.target.closest('.qty-plus');
      const item = event.target.closest('[data-cart-item]');
      if (!item) return;
      if (!removeButton && !minusButton && !plusButton) return;

      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      const cartItemId = String(item.dataset.cartItem);
      const isGuest = cartItemId.startsWith('guest-');

      if (removeButton) {
        if (isGuest) {
          const idx = Number(cartItemId.replace('guest-', ''));
          const guests = leerCarritoInvitado();
          guests.splice(idx, 1);
          guardarCarritoInvitado(guests);
          renderCarritoInvitado();
          showToast('Product removed from cart');
        } else {
          eliminarItemCarrito(cartItemId);
        }
        return;
      }

      const qtyInput = item.querySelector('.qty-input');
      const currentQty = qtyInput ? Number(qtyInput.value) : 1;
      const delta = minusButton ? -1 : 1;
      const newQty = currentQty + delta;

      applyQtyChange(cartItemId, isGuest, newQty);
    }, true);

    container.addEventListener('change', event => {
      const qtyInput = event.target.closest('.qty-input');
      const item = event.target.closest('[data-cart-item]');
      if (!qtyInput || !item) return;

      const cartItemId = String(item.dataset.cartItem);
      const isGuest = cartItemId.startsWith('guest-');
      const newQty = parseInt(qtyInput.value, 10);

      if (!Number.isFinite(newQty) || newQty < 1) {
        qtyInput.value = 1;
        applyQtyChange(cartItemId, isGuest, 1);
        return;
      }

      applyQtyChange(cartItemId, isGuest, newQty);
    }, true);
  }

  function applyQtyChange(cartItemId, isGuest, newQty) {
    if (isGuest) {
      const idx = Number(cartItemId.replace('guest-', ''));
      const guests = leerCarritoInvitado();
      if (newQty <= 0) {
        guests.splice(idx, 1);
      } else {
        guests[idx].cantidad = newQty;
      }
      guardarCarritoInvitado(guests);
      renderCarritoInvitado();
    } else {
      if (newQty <= 0) {
        eliminarItemCarrito(cartItemId);
      } else {
        actualizarCantidadItem(cartItemId, newQty);
      }
    }
  }

  function scheduleBadgeRefresh() {
    // Immediately show guest cart badge if not logged in
    if (!getUsuarioId()) {
      const guestItems = leerCarritoInvitado();
      if (guestItems.length > 0) {
        updateCartBadge(carritoInvitadoADTO(guestItems));
      }
    }

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
  window.leerCarritoInvitado = leerCarritoInvitado;
  window.guardarCarritoInvitado = guardarCarritoInvitado;

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
