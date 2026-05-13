// ============================================
// Shine - Checkout and Bizum Payment
// ============================================

(function () {
  const TAX_RATE = 0.06;
  const CHECK_SVG = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>';

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

  function showBizumModal({ pedido, pago }) {
    document.getElementById('bizumPaymentModal')?.remove();

    const modal = document.createElement('div');
    modal.id = 'bizumPaymentModal';
    modal.style.cssText = 'position:fixed;inset:0;z-index:9999;background:rgba(18,18,18,.45);display:flex;align-items:center;justify-content:center;padding:24px';
    modal.innerHTML = `
      <div style="max-width:420px;width:100%;background:var(--surface);border-radius:var(--r-lg);box-shadow:0 24px 70px rgba(0,0,0,.22);padding:var(--sp-2xl);text-align:center">
        <div style="width:52px;height:52px;border-radius:50%;display:grid;place-items:center;background:var(--petal);color:var(--rose);margin:0 auto var(--sp-lg)">
          ${CHECK_SVG}
        </div>
        <h3 style="margin-bottom:var(--sp-sm)">Bizum payment completed</h3>
        <p class="text-sm text-muted" style="margin:0 0 var(--sp-lg)">
          Order #${escapeHtml(pedido.idPedido)} confirmed. Payment #${escapeHtml(pago.idPago || 'bizum')} processed via Bizum.
        </p>
        <button class="btn btn--primary" id="bizumDoneBtn" type="button" style="width:100%">Back to Home</button>
      </div>
    `;
    document.body.appendChild(modal);

    document.getElementById('bizumDoneBtn')?.addEventListener('click', () => {
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

      return `
        <div class="co-summary-item">
          <img src="assets/img/product-perfume.png" alt="${escapeHtml(name)}">
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

    if (subtotalEl) subtotalEl.textContent = formatCurrency(subtotal);
    if (taxEl) taxEl.textContent = formatCurrency(tax);
    if (shippingEl) shippingEl.textContent = shipping === 0 ? 'Free' : formatCurrency(shipping);
    if (totalEl) totalEl.textContent = formatCurrency(total);
  }

  async function cargarCheckout() {
    if (!window.ShineAPI) {
      showToast('Could not initialize the connection with the API.');
      return;
    }

    try {
      const [carritoData, direccionesData] = await Promise.all([
        window.ShineAPI.get(`/usuarios/${getUsuarioId()}/carrito`),
        window.ShineAPI.get(`/usuarios/${getUsuarioId()}/direcciones`)
      ]);

      carrito = carritoData;
      direcciones = Array.isArray(direccionesData) ? direccionesData : [];

      renderDirecciones();
      renderSummaryItems();
      syncShippingSummary();

        showToast('Your cart is empty.');
      }
    } catch (error) {
      showToast('Could not load the checkout. Make sure the backend is running.');
    }
  }

  async function guardarDireccion() {
    const calle = document.getElementById('coAddrLine')?.value.trim();
    const ciudad = document.getElementById('coAddrCity')?.value.trim();
    const codigoPostal = document.getElementById('coAddrZip')?.value.trim();

    if (!calle || !ciudad || !codigoPostal) {
      showToast('Please fill in street, city and postcode.');
      return;
    }

    try {
      const direccion = await window.ShineAPI.post(`/usuarios/${getUsuarioId()}/direcciones`, {
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

  async function crearPedidoYPagarBizum() {
    const validation = validarPasoActual();
    if (validation) {
      showToast(validation);
      return;
    }

    const nextBtn = document.getElementById('coNextBtn');
    if (nextBtn) {
      nextBtn.disabled = true;
      nextBtn.textContent = 'Processing Bizum…';
    }

    try {
      const pedido = await window.ShineAPI.post('/pedidos', {
        idUsuario: getUsuarioId(),
        idDireccion: getSelectedAddressId()
      });

      const pago = await window.ShineAPI.post('/pagos', {
        idPedido: pedido.idPedido,
        metodoPago: 'bizum',
        estado: 'completado'
      });

      carrito = { items: [], total: 0 };
      renderSummaryItems();
      showBizumModal({ pedido, pago });
    } catch (error) {
      showToast(error.message || 'Could not complete the Bizum payment.');
    } finally {
      if (nextBtn) {
        nextBtn.disabled = false;
        nextBtn.textContent = 'Pay with Bizum';
      }
    }
  }

  function setupCheckoutStepper() {
    const panels = document.querySelectorAll('.co-panel');
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
      if (currentStep === 2) renderSummaryItems();
      updateStepper();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, true);

    prevBtn?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (currentStep > 0) {
        currentStep--;
        updateStepper();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }, true);

    updateStepper();
  }

  function setupCheckoutInteractions() {
    document.addEventListener('change', event => {
      if (event.target.matches('input[name="co-address"]')) syncAddressSummary();
      if (event.target.matches('input[name="co-shipping"]')) syncShippingSummary();
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

  function setupCartCheckoutButton() {
    document.getElementById('checkoutBtn')?.addEventListener('click', event => {
      event.preventDefault();
      window.location.href = 'checkout.html';
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    setupCartCheckoutButton();

    if (!document.getElementById('coPanels')) return;

    setupCheckoutStepper();
    setupCheckoutInteractions();
    cargarCheckout();
  });
})();
