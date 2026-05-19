// ============================================
// Shine - Checkout and Bizum Payment
// ============================================

(function () {
  const TAX_RATE = 0.06;
  const CHECK_SVG = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>';

  const MAX_ADDRESSES = 3;
  let currentStep = 0;
  let carrito = null;
  let direcciones = [];

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

  function getUsuarioId() {
    return window.ID_USUARIO || (typeof ID_USUARIO !== 'undefined' ? ID_USUARIO : 1);
  }

  function getCartItems() {
    return Array.isArray(carrito?.items) ? carrito.items : [];
  }

  function getItemQty(item) {
    return Number(item.cantidad ?? item.qty ?? 1);
  }

  function getItemUnitPrice(item) {
    return Number(item.precioUnitario ?? item.precio ?? item.price ?? 0);
  }

  function getItemSubtotal(item) {
    return Number(item.subtotal ?? (getItemUnitPrice(item) * getItemQty(item)));
  }

  function getSubtotal() {
    return getCartItems().reduce((sum, item) => sum + getItemSubtotal(item), 0);
  }

  function getShippingCost() {
    const checked = document.querySelector('input[name="co-shipping"]:checked');
    if (!checked || checked.value === 'free') return 0;
    if (checked.value === 'express') return 4;
    if (checked.value === 'scheduled') return 2;
    return 0;
  }

  function getSelectedAddressId() {
    const checked = document.querySelector('input[name="co-address"]:checked');
    return checked ? Number(checked.value) : null;
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

  function showBizumModal({ pedido, pago, puntosGanados, puntosTotal }) {
    document.getElementById('bizumPaymentModal')?.remove();
    document.body.style.overflow = 'hidden';

    const pointsBadge = puntosGanados > 0
      ? `<div style="display:inline-flex;align-items:center;gap:6px;background:var(--petal);border-radius:999px;padding:6px 14px;margin-bottom:var(--sp-lg);font-size:var(--fs-sm);border:1px solid rgba(212,145,154,.25)">
           <svg width="14" height="14" viewBox="0 0 24 24" fill="var(--rose)" stroke="var(--rose)" stroke-width="1.5"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
           <span style="color:var(--rose);font-weight:700">+${puntosGanados} puntos ganados</span>
           ${puntosTotal != null ? `<span style="color:var(--text-muted)">· ${puntosTotal} total</span>` : ''}
         </div>`
      : '';

    const modal = document.createElement('div');
    modal.id = 'bizumPaymentModal';
    modal.style.cssText = 'position:fixed;inset:0;z-index:10001;background:rgba(12,12,12,.78);backdrop-filter:blur(6px);-webkit-backdrop-filter:blur(6px);display:flex;align-items:center;justify-content:center;padding:20px;box-sizing:border-box';
    modal.innerHTML = `
      <div style="max-width:400px;width:100%;background:#fff;border-radius:20px;box-shadow:0 32px 80px rgba(0,0,0,.28);padding:40px 32px 32px;text-align:center;position:relative">
        <div style="width:68px;height:68px;border-radius:50%;display:grid;place-items:center;background:var(--petal);margin:0 auto 20px;box-shadow:0 0 0 12px rgba(212,145,154,.12)">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--rose)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
        </div>
        <h3 style="margin:0 0 8px;font-size:1.5rem;color:#1a1a1a">¡Pedido confirmado!</h3>
        <p style="margin:0 0 16px;font-size:.9rem;color:#888;line-height:1.5">
          Pedido <strong style="color:#444">#${escapeHtml(pedido.idPedido)}</strong> procesado mediante Bizum.<br>Tu pedido está siendo preparado.
        </p>
        ${pointsBadge}
        <div style="display:flex;flex-direction:column;gap:10px;margin-top:8px">
          <button class="btn btn--primary" id="bizumOrdersBtn" type="button" style="width:100%;padding:14px">Ver mis pedidos</button>
          <button class="btn btn--outline" id="bizumDoneBtn" type="button" style="width:100%;padding:14px">Volver al inicio</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);

    const cleanup = () => { document.body.style.overflow = ''; };

    document.getElementById('bizumOrdersBtn')?.addEventListener('click', () => {
      cleanup();
      window.location.href = 'orders.html';
    });
    document.getElementById('bizumDoneBtn')?.addEventListener('click', () => {
      cleanup();
      window.location.href = 'index.html';
    });
  }

  function updateStepper() {
    const panels = document.querySelectorAll('.co-panel');
    const steps = document.querySelectorAll('#coStepper .stepper-step');
    const lines = document.querySelectorAll('#coStepper .stepper-line');
    const prevBtn = document.getElementById('coPrevBtn');
    const nextBtn = document.getElementById('coNextBtn');

    panels.forEach((panel, index) => panel.classList.toggle('active', index === currentStep));
    steps.forEach((step, index) => {
      const circle = step.querySelector('.stepper-circle');
      step.classList.remove('active', 'completed');

      if (index < currentStep) {
        step.classList.add('completed');
        if (circle) circle.innerHTML = CHECK_SVG;
      } else {
        if (circle) circle.textContent = String(index + 1);
        if (index === currentStep) step.classList.add('active');
      }
    });
    lines.forEach((line, index) => line.classList.toggle('filled', index < currentStep));

    if (prevBtn) prevBtn.disabled = currentStep === 0;
    if (nextBtn) nextBtn.textContent = currentStep === panels.length - 1 ? 'Pay with Bizum' : 'Next';
  }

  function renderDirecciones() {
    const list = document.getElementById('coAddressList');
    if (!list) return;

    // Hide/show add address button based on limit
    const addBtn = document.getElementById('coAddAddrBtn');
    if (addBtn) addBtn.style.display = direcciones.length >= MAX_ADDRESSES ? 'none' : '';

    if (!direcciones.length) {
      list.innerHTML = '<p class="text-sm text-muted">Add an address to continue.</p>';
      return;
    }

    list.innerHTML = direcciones.map((direccion, index) => `
      <label class="co-address-option">
        <input type="radio" name="co-address" value="${escapeHtml(direccion.id)}" ${index === 0 ? 'checked' : ''}>
        <div style="flex:1;min-width:0">
          <div style="display:flex;align-items:center;gap:var(--sp-sm);margin-bottom:var(--sp-xs);flex-wrap:wrap">
            <span style="font-weight:700">${escapeHtml(index === 0 ? 'Primary' : 'Address')}</span>
            ${index === 0 ? '<span class="co-addr-badge">Default</span>' : ''}
          </div>
          <p class="text-sm text-muted" style="margin:0;line-height:1.7">
            ${escapeHtml(direccion.calle)}, ${escapeHtml(direccion.codigoPostal)}, ${escapeHtml(direccion.ciudad)}
          </p>
        </div>
      </label>
    `).join('');

    syncAddressSummary();
  }

  function renderSummaryItems() {
    const container = document.getElementById('coSummaryItems');
    if (!container) return;

    const items = getCartItems();
    if (!items.length) {
      container.innerHTML = '<p class="text-sm text-muted">Your cart is empty.</p>';
      recalcTotals();
      return;
    }

    container.innerHTML = items.map(item => {
      const name = item.nombre || item.name || 'Producto Shine';
      const qty = getItemQty(item);
      const subtotal = getItemSubtotal(item);
      let img = item.imagen || item.imagenUrl || item.image || 'assets/img/product-bodyoil.png';
      if (item.idPerfCust) {
        const cached = sessionStorage.getItem('custom_img_' + item.idPerfCust);
        if (cached) img = cached;
      }

      return `
        <div class="co-summary-item">
          <img src="${escapeHtml(img)}" alt="${escapeHtml(name)}" onerror="this.onerror=null;this.src='assets/img/product-bodyoil.png'">
          <div>
            <div class="co-summary-item__name">${escapeHtml(name)}</div>
            <div class="co-summary-item__price">${escapeHtml(formatCurrency(getItemUnitPrice(item)))} × ${escapeHtml(qty)}</div>
          </div>
          <span style="margin-left:auto;font-weight:600;font-size:var(--fs-sm)">${escapeHtml(formatCurrency(subtotal))}</span>
        </div>
      `;
    }).join('');

    recalcTotals();
  }

  function syncAddressSummary() {
    const checked = document.querySelector('input[name="co-address"]:checked');
    const summary = document.getElementById('coSummaryAddress');
    if (!checked || !summary) return;

    const text = checked.closest('.co-address-option')?.querySelector('p')?.innerText || '';
    summary.innerHTML = `<strong>Address</strong>${escapeHtml(text)}`;
  }

  function syncShippingSummary() {
    const checked = document.querySelector('input[name="co-shipping"]:checked');
    if (!checked) return;

    const label = checked.closest('.co-shipping-option')?.querySelector('strong')?.textContent || 'Regular shipment';
    const price = getShippingCost();
    const priceLabel = price === 0 ? 'Free' : formatCurrency(price);

    const summaryShipping = document.getElementById('coSummaryShipping');
    const shippingCost = document.getElementById('coShippingCost');

    if (summaryShipping) summaryShipping.innerHTML = `<strong>Shipping method</strong>${escapeHtml(priceLabel)} — ${escapeHtml(label)}`;
    if (shippingCost) shippingCost.textContent = priceLabel;

    recalcTotals();
  }

  function recalcTotals() {
    const subtotal = getSubtotal();
    const tax = subtotal * TAX_RATE;
    const shipping = getShippingCost();
    const total = subtotal + tax + shipping;

    const subtotalEl = document.getElementById('coSubtotal');
    const taxEl = document.getElementById('coTax');
    const shippingEl = document.getElementById('coShippingCost');
    const totalEl = document.getElementById('coTotal');
    const pointsEl = document.getElementById('coPointsToEarn');

    if (subtotalEl) subtotalEl.textContent = formatCurrency(subtotal);
    if (taxEl) taxEl.textContent = formatCurrency(tax);
    if (shippingEl) shippingEl.textContent = shipping === 0 ? 'Free' : formatCurrency(shipping);
    if (totalEl) totalEl.textContent = formatCurrency(total);

    if (pointsEl) {
      const puntos = Math.round(subtotal * 10);
      pointsEl.textContent = puntos > 0 ? `+${puntos} pts` : '0 pts';
    }
  }

  async function cargarCheckout() {
    if (!window.ShineAPI) {
      showToast('Could not initialize the connection with the API.');
      return;
    }

    try {
      const [carritoData, direccionesData] = await Promise.all([
        window.ShineAPI.get('/carrito'),
        window.ShineAPI.get('/direcciones')
      ]);

      carrito = carritoData;
      direcciones = Array.isArray(direccionesData) ? direccionesData : [];

      renderDirecciones();
      renderSummaryItems();
      syncShippingSummary();

      if (!getCartItems().length) {
        showToast('Your cart is empty.');
      }
    } catch (error) {
      showToast('Could not load the checkout. Make sure the backend is running.');
    }
  }

  async function guardarDireccion() {
    if (direcciones.length >= MAX_ADDRESSES) {
      showToast(`Maximum of ${MAX_ADDRESSES} addresses reached. Delete one first.`);
      return;
    }

    const calle = document.getElementById('coAddrLine')?.value.trim();
    const ciudad = document.getElementById('coAddrCity')?.value.trim();
    const codigoPostal = document.getElementById('coAddrZip')?.value.trim();

    if (!calle || !ciudad || !codigoPostal) {
      showToast('Please fill in street, city and postcode.');
      return;
    }

    try {
      const direccion = await window.ShineAPI.post('/direcciones', {
        idUsuario: getUsuarioId(),
        calle,
        ciudad,
        codigoPostal
      });

      direcciones.push(direccion);
      renderDirecciones();
      document.getElementById('coAddressForm')?.classList.remove('open');
      ['coAddrLabel', 'coAddrPhone', 'coAddrLine', 'coAddrCity', 'coAddrZip'].forEach(id => {
        const input = document.getElementById(id);
        if (input) input.value = '';
      });
      showToast('Address saved.');
    } catch (error) {
      showToast(error.message || 'Could not save the address.');
    }
  }

  function validarPasoActual() {
    if (currentStep === 0 && !getSelectedAddressId()) return 'Please select or add a delivery address.';
    if (currentStep === 2 && !getCartItems().length) return 'Cannot create an order with an empty cart.';
    return '';
  }

  // ── Bizum UI & Timer ──────────────────────────────────────────────
  let _bizumTimer = null;
  let _timerIniciado = false;

  function iniciarBizumUI() {
    if (_timerIniciado) return;
    _timerIniciado = true;

    // Calculamos el total actual en tiempo real
    const subtotal = getSubtotal();
    const tax      = subtotal * TAX_RATE;
    const shipping = getShippingCost();
    const total    = (subtotal + tax + shipping).toFixed(2);

    // ID provisional de pedido para el concepto
    const orderId = 'SHINE-' + Math.floor(100000 + Math.random() * 900000);

    // Actualizar labels del panel
    const labelId     = document.getElementById('bizumOrderIdLabel');
    const labelAmount = document.getElementById('bizumAmountLabel');
    if (labelId)     labelId.textContent     = orderId;
    if (labelAmount) labelAmount.textContent = `€${total}`;

    // Iniciar cuenta atrás de 5 minutos (300 segundos)
    let timeLeft = 300;
    const countdownEl = document.getElementById('bizumCountdown');
    
    if (_bizumTimer) clearInterval(_bizumTimer);
    
    _bizumTimer = setInterval(() => {
      if (timeLeft <= 0) {
        clearInterval(_bizumTimer);
        if (countdownEl) countdownEl.textContent = "00:00";
        showToast("El tiempo para pagar ha expirado. Por favor, recarga la página para intentarlo de nuevo.");
        const nextBtn = document.getElementById('coNextBtn');
        if (nextBtn) {
            nextBtn.disabled = true;
            nextBtn.textContent = 'Tiempo expirado';
        }
        return;
      }
      
      timeLeft--;
      const m = Math.floor(timeLeft / 60).toString().padStart(2, '0');
      const s = (timeLeft % 60).toString().padStart(2, '0');
      if (countdownEl) countdownEl.textContent = `${m}:${s}`;
    }, 1000);
  }

  async function crearPedidoYPagarBizum() {
    const validation = validarPasoActual();
    if (validation) {
      showToast(validation);
      return;
    }

    const nextBtn = document.getElementById('coNextBtn');
    if (nextBtn) {
      nextBtn.disabled = true;
      nextBtn.textContent = 'Registrando pedido…';
    }

    try {
      const puntosGanados = Math.round(getSubtotal() * 10);

      // 1. Crear el pedido
      const pedido = await window.ShineAPI.post('/pedidos', {
        idUsuario:   getUsuarioId(),
        idDireccion: getSelectedAddressId()
      });

      // 2. Crear registro de pago en estado 'pendiente' (esperando confirmación manual)
      const pago = await window.ShineAPI.post('/pagos', {
        idPedido:   pedido.idPedido,
        metodoPago: 'bizum',
        estado:     'pendiente'   // ← pendiente: admin lo valida desde la intranet
      });

      // 3. Actualizar el estado del pedido a 'pendiente_bizum'
      //    (indica que está esperando validación manual en la intranet)
      try {
        await window.ShineAPI.put(`/intranet/pedidos/${pedido.idPedido}/estado`, {
          estado: 'pendiente_bizum'
        });
      } catch (_) {
        // Si el endpoint rechaza 'pendiente_bizum' (por validación de estados),
        // el pedido queda en 'pendiente' igual — la intranet puede filtrarlo.
      }

      // 4. Vaciar carrito
      try { await window.ShineAPI.delete('/carrito'); } catch (_) {}
      sessionStorage.removeItem('shine:carrito:v1');
      localStorage.removeItem('shineCart');

      // 5. Puntos de fidelización (actualizamos preview, se confirmarán al validar)
      let puntosTotal = null;
      try {
        const puntosData = await window.ShineAPI.get(`/usuarios/${getUsuarioId()}/puntos`);
        puntosTotal = puntosData?.puntos ?? null;
        const user = JSON.parse(localStorage.getItem('shineUser') || 'null');
        if (user && puntosTotal !== null) {
          user.puntos = puntosTotal;
          localStorage.setItem('shineUser', JSON.stringify(user));
        }
      } catch (_) {}

      carrito = { items: [], total: 0 };
      renderSummaryItems();
      showBizumModal({ pedido, pago, puntosGanados, puntosTotal });

    } catch (error) {
      showToast(error.message || 'No se pudo completar el pago Bizum.');
    } finally {
      if (nextBtn) {
        nextBtn.disabled    = false;
        nextBtn.textContent = 'Pay with Bizum';
      }
    }
  }

  function setupCheckoutStepper() {
    const panels  = document.querySelectorAll('.co-panel');
    const prevBtn = document.getElementById('coPrevBtn');
    const nextBtn = document.getElementById('coNextBtn');

    nextBtn?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (currentStep === panels.length - 1) {
        crearPedidoYPagarBizum();
        return;
      }

      const validation = validarPasoActual();
      if (validation) {
        showToast(validation);
        return;
      }

      if (currentStep === 0) syncAddressSummary();
      if (currentStep === 1) syncShippingSummary();

      currentStep++;
      if (currentStep === 2) {
        renderSummaryItems();
        // Iniciar timer Bizum al llegar al paso de pago
        setTimeout(iniciarBizumUI, 150);
      }
      updateStepper();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, true);

    prevBtn?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (currentStep > 0) {
        currentStep--;
        _timerIniciado = false;
        if (_bizumTimer) clearInterval(_bizumTimer);
        updateStepper();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }, true);

    updateStepper();
  }

  function setupCheckoutInteractions() {
    document.addEventListener('change', event => {
      if (event.target.matches('input[name="co-address"]')) syncAddressSummary();
      if (event.target.matches('input[name="co-shipping"]')) {
        syncShippingSummary();
        // Si ya estamos en el paso de pago, actualizar el total
        if (currentStep === 2 && !_timerIniciado) iniciarBizumUI();
      }
    });

    document.getElementById('coAddAddrBtn')?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      document.getElementById('coAddressForm')?.classList.toggle('open');
    }, true);

    document.getElementById('coCancelAddrBtn')?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      document.getElementById('coAddressForm')?.classList.remove('open');
    }, true);

    document.getElementById('coSaveAddrBtn')?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
      guardarDireccion();
    }, true);
  }

  function addBusinessDays(date, days) {
    const result = new Date(date);
    let added = 0;
    while (added < days) {
      result.setDate(result.getDate() + 1);
      const dow = result.getDay();
      if (dow !== 0 && dow !== 6) added++;
    }
    return result;
  }

  function formatShippingDate(date) {
    return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  function initShippingDates() {
    const today = new Date();
    const freeEl    = document.getElementById('coDateFree');
    const expressEl = document.getElementById('coDateExpress');
    if (freeEl)    freeEl.textContent    = formatShippingDate(addBusinessDays(today, 6));
    if (expressEl) expressEl.textContent = formatShippingDate(addBusinessDays(today, 3));
  }

  function setupCartCheckoutButton() {
    document.getElementById('checkoutBtn')?.addEventListener('click', event => {
      event.preventDefault();
      const userId = window.ID_USUARIO || Number(localStorage.getItem('shineUserId')) || null;
      if (!userId || userId <= 0) {
        window.location.href = 'login.html';
        return;
      }
      window.location.href = 'checkout.html';
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupCartCheckoutButton();

    if (!document.getElementById('coPanels')) return;

    initShippingDates();
    setupCheckoutStepper();
    setupCheckoutInteractions();
    cargarCheckout();
  });
})();
