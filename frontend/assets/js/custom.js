// ============================================
// Shine - Custom Perfume Configurator
// ============================================

(function () {
  const FRAGRANCE_IMAGE = 'assets/img/category-fragrance.png';
  const BASE_IMAGE = 'assets/img/product-bodyoil.png';
  const BOTTLE_IMAGE = 'assets/img/product-perfume.png';
  const NOTE_FALLBACKS = {
    salida:   'assets/img/product-toner.png',
    corazon:  'assets/img/product-serum.png',
    fondo:    'assets/img/product-bodyoil.png'
  };
  const NOTE_DESCRIPTIONS = {
    'Tobacco':    'Warm and enveloping',
    'Sandalwood': 'Fresh wood',
    'White Musk': 'Enveloping musky scent'
  };
  const TYPE_PRICE = {
    eau_de_parfum: 42,
    eau_de_toilette: 32,
    body_mist: 22
  };

  const TYPE_QUANTITY_PRICE = {
    elixir:          { 50: 65,  75: 90,  100: 112, 125: 132 },
    eau_de_parfum:   { 50: 42,  75: 58,  100: 72,  125: 85  },
    eau_de_toilette: { 50: 32,  75: 44,  100: 55,  125: 65  },
    eau_de_cologne:  { 50: 24,  75: 33,  100: 42,  125: 50  },
    body_mist:       { 50: 18,  75: 24,  100: 30,  125: 36  }
  };

  let currentStep = 0;
  let frascos = [];
  let fragancias = [];
  let notasOlfativas = [];

  const selections = {
    intensidad: null,
    cantidad: null,
    idFrasco: null,
    bottleLabel: null,
    base: null,
    notes: []
  };

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

  function getUsuarioId() {
    const fromWindow = window.ID_USUARIO;
    if (fromWindow && Number.isFinite(Number(fromWindow))) return Number(fromWindow);
    const fromStorage = Number(localStorage.getItem('shineUserId'));
    return Number.isFinite(fromStorage) && fromStorage > 0 ? fromStorage : null;
  }

  function normalizarTexto(value) {
    return String(value ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  function labelIntensidad(intensidad) {
    if (intensidad === 'eau_de_parfum') return 'Eau de Parfum';
    if (intensidad === 'eau_de_toilette') return 'Eau de Toilette';
    if (intensidad === 'body_mist') return 'Body Mist';
    return '—';
  }

  function intensidadDesdeCard(card) {
    const value = normalizarTexto(card.dataset.value);
    const text  = normalizarTexto(card.textContent);

    if (value === 'body_mist' || text.includes('solide') || text.includes('mist')) return 'body_mist';
    if (value === 'eau_de_toilette' || value === 'eau_de_cologne' || text.includes('toilette') || text.includes('cologne')) return 'eau_de_toilette';
    return 'eau_de_parfum';
  }

  function getFrasco(idFrasco) {
    return frascos.find(frasco => Number(frasco.idFrasco) === Number(idFrasco));
  }

  function getFragancia(idFragancia) {
    return fragancias.find(fragancia => Number(fragancia.idFragancia) === Number(idFragancia));
  }

  function getFraganciaLabel(idFragancia) {
    return getFragancia(idFragancia)?.nombre || '—';
  }

  function getFrascoLabel(idFrasco) {
    const frasco = getFrasco(idFrasco);
    if (frasco) return `${frasco.forma} (${frasco.capacidadMl}ml)`;
    return selections.bottleLabel || '—';
  }

  function calcularPrecio() {
    const frasco = getFrasco(selections.idFrasco);
    const precioFrasco = Number(frasco?.precio || 0);
    const typeKey = document.querySelector('[data-group="type"] .option-card.selected')?.dataset.value;
    const qty = Number(selections.cantidad) || null;
    const precioIntensidad = (typeKey && qty && TYPE_QUANTITY_PRICE[typeKey]?.[qty])
      || (typeKey && TYPE_QUANTITY_PRICE[typeKey]?.[50])
      || TYPE_PRICE[selections.intensidad]
      || 0;
    const precioFragancias = new Set([selections.base, ...selections.notes].filter(Boolean)).size * 1;
    return precioFrasco + precioIntensidad + precioFragancias;
  }

  function updateTypePriceReveals() {
    const qty = Number(selections.cantidad) || null;
    document.querySelectorAll('[data-group="type"] .option-card').forEach(card => {
      const reveal = card.querySelector('.type-price-reveal');
      if (!reveal) return;
      const typeKey = card.dataset.value;
      const prices = TYPE_QUANTITY_PRICE[typeKey];
      if (!prices) return;
      if (qty && prices[qty] !== undefined) {
        reveal.textContent = formatCurrency(prices[qty]);
      } else {
        const vals = Object.values(prices);
        reveal.textContent = `${formatCurrency(Math.min(...vals))} – ${formatCurrency(Math.max(...vals))}`;
      }
      reveal.classList.add('visible');
    });
  }

  function updatePriceBar() {
    const el = document.getElementById('priceBarTotal');
    if (!el) return;
    const total = calcularPrecio();
    el.textContent = total > 0 ? formatCurrency(total) : '—';
  }

  function optionCard({ id, label, description, image, group, apiValue }) {
    return `
      <div class="option-card" data-value="${escapeHtml(id)}" data-api-value="${escapeHtml(apiValue ?? id)}" data-group="${escapeHtml(group)}">
        <img src="${escapeHtml(image)}" alt="${escapeHtml(label)}">
        <div class="option-card__label">${escapeHtml(label)}</div>
        <div class="option-card__desc">${escapeHtml(description)}</div>
      </div>
    `;
  }

  function renderFrascos() {
    const grid = document.querySelector('[data-group="bottle"]');
    if (!grid) return;
    if (grid.querySelector('.option-card')) return;

    if (!frascos.length) {
      grid.innerHTML = '<p class="text-sm text-muted">No hay frascos disponibles.</p>';
      return;
    }

    // Sort by price (cheapest first)
    const sortedFrascos = [...frascos].sort((a, b) => Number(a.precio) - Number(b.precio));

    grid.innerHTML = sortedFrascos.map(frasco => optionCard({
      id: frasco.idFrasco,
      label: `${frasco.forma} (${frasco.capacidadMl}ml)`,
      description: `${frasco.material || 'Premium glass'} · ${formatCurrency(frasco.precio)}`,
      image: BOTTLE_IMAGE,
      group: 'bottle'
    })).join('');
  }

  function renderNotasOlfativas() {
    const carousel = document.getElementById('notesCarousel');
    if (!carousel) return;

    if (!notasOlfativas.length) {
      carousel.innerHTML = '<p class="text-sm text-muted" style="padding:var(--sp-xl)">No olfactory notes available.</p>';
      return;
    }

    carousel.innerHTML = notasOlfativas.map(nota => {
      const imagen = nota.urlImagen
        ? escapeHtml(nota.urlImagen)
        : escapeHtml(NOTE_FALLBACKS[nota.tipo] || FRAGRANCE_IMAGE);

      const tipoLabel = { salida: 'Top note', corazon: 'Heart note', fondo: 'Base note' }[nota.tipo] || nota.tipo;
      const desc = NOTE_DESCRIPTIONS[nota.nombre] || tipoLabel;

      return `
        <div class="option-card" data-value="${escapeHtml(String(nota.idNota))}" data-api-value="${escapeHtml(String(nota.idFragancia))}" data-group="notes">
          <img src="${imagen}" alt="${escapeHtml(nota.nombre)}" onerror="this.src='${escapeHtml(FRAGRANCE_IMAGE)}'">
          <div class="option-card__label">${escapeHtml(nota.nombre)}</div>
          <div class="option-card__desc">${escapeHtml(desc)}</div>
        </div>`;
    }).join('');
  }

  function renderFragancias() {
    const baseGrid = document.querySelector('[data-group="base"]');
    const notesGrid = document.querySelector('[data-group="notes"]');
    const bases = fragancias.filter(fragancia => fragancia.esBase === true);
    const notes = fragancias.filter(fragancia => fragancia.esBase !== true);

    if (baseGrid) {
      baseGrid.innerHTML = bases.length
        ? bases.map(fragancia => optionCard({
            id: fragancia.idFragancia,
            label: fragancia.nombre,
            description: NOTE_DESCRIPTIONS[fragancia.nombre] || fragancia.familia || 'Base note',
            image: BASE_IMAGE,
            group: 'base'
          })).join('')
        : '<p class="text-sm text-muted">No hay bases disponibles.</p>';
    }

    if (notesGrid) {
      notesGrid.innerHTML = notes.length
        ? notes.map(fragancia => optionCard({
            id: fragancia.idFragancia,
            label: fragancia.nombre,
            description: fragancia.familia || 'Fragrance note',
            image: FRAGRANCE_IMAGE,
            group: 'notes'
          })).join('')
        : '<p class="text-sm text-muted">No hay notas disponibles.</p>';
    }
  }

  function prepararTiposIntensidad() {
    document.querySelectorAll('[data-group="type"] .option-card').forEach(card => {
      const intensidad = intensidadDesdeCard(card);
      card.dataset.apiValue = intensidad;
      card.dataset.value = intensidad;
    });
  }

  function updateStepper() {
    const panels = document.querySelectorAll('.step-panel');
    const steps = document.querySelectorAll('.stepper-step');
    const lines = document.querySelectorAll('.stepper-line');
    const prevBtn = document.getElementById('prevStepBtn');
    const nextBtn = document.getElementById('nextStepBtn');

    panels.forEach((panel, index) => panel.classList.toggle('active', index === currentStep));
    steps.forEach((step, index) => {
      step.classList.toggle('completed', index < currentStep);
      step.classList.toggle('active', index === currentStep);
    });
    lines.forEach((line, index) => line.classList.toggle('filled', index < currentStep));

    if (prevBtn) prevBtn.disabled = currentStep === 0;
    if (nextBtn) nextBtn.textContent = currentStep === panels.length - 1 ? 'Crear y añadir al carrito' : 'Next Step';
  }

  function updateReview() {
    const reviewType = document.getElementById('reviewType');
    const reviewQuantity = document.getElementById('reviewQuantity');
    const reviewBase = document.getElementById('reviewBase');
    const reviewNotes = document.getElementById('reviewNotes');
    const reviewBottle = document.getElementById('reviewBottle');
    const previewComposition = document.getElementById('previewComposition');

    const noteLabels = selections.notes
      .map(id => notasOlfativas.find(n => Number(n.idNota) === Number(id))?.nombre || getFraganciaLabel(id))
      .filter(label => label !== '—');

    const cantidadLabel = selections.cantidad ? `${selections.cantidad} ml` : '—';

    if (reviewType) reviewType.innerHTML = `<strong>Type:</strong> ${escapeHtml(labelIntensidad(selections.intensidad))}`;
    if (reviewQuantity) reviewQuantity.innerHTML = `<strong>Quantity:</strong> ${escapeHtml(cantidadLabel)}`;
    if (reviewBase) reviewBase.innerHTML = `<strong>Base:</strong> ${escapeHtml(getFraganciaLabel(selections.base))}`;
    if (reviewNotes) reviewNotes.innerHTML = `<strong>Notes:</strong> ${escapeHtml(noteLabels.length ? noteLabels.join(', ') : '—')}`;
    if (reviewBottle) reviewBottle.innerHTML = `<strong>Bottle:</strong> ${escapeHtml(getFrascoLabel(selections.idFrasco))}`;

    if (previewComposition) {
      const composition = [
        labelIntensidad(selections.intensidad),
        cantidadLabel,
        getFraganciaLabel(selections.base),
        ...noteLabels,
        getFrascoLabel(selections.idFrasco),
        formatCurrency(calcularPrecio())
      ].filter(value => value && value !== '—').join(' · ');

      previewComposition.textContent = composition || '—';
    }

    updateTypePriceReveals();
    updatePriceBar();
  }

  function selectSingleCard(card, group) {
    const container = card.closest('.options-grid, .options-carousel');
    container?.querySelectorAll('.option-card').forEach(item => item.classList.remove('selected'));
    card.classList.add('selected');

    if (group === 'type') selections.intensidad = card.dataset.apiValue || intensidadDesdeCard(card);
    if (group === 'quantity') selections.cantidad = card.dataset.value;
    if (group === 'base') selections.base = Number(card.dataset.apiValue || card.dataset.value);
    if (group === 'bottle') {
      selections.idFrasco = card.dataset.value;
      selections.bottleLabel = card.querySelector('.option-card__label')?.textContent?.trim() || card.dataset.value;
    }
  }

  function toggleNoteCard(card) {
    const idFragancia = Number(card.dataset.apiValue || card.dataset.value);
    const isSelected = card.classList.contains('selected');

    if (isSelected) {
      card.classList.remove('selected');
      selections.notes = selections.notes.filter(id => id !== idFragancia);
      return;
    }

    if (selections.notes.length >= 5) {
      showToast('You can select up to 5 notes.');
      return;
    }

    card.classList.add('selected');
    selections.notes.push(idFragancia);
  }

  function setupSelections() {
    document.addEventListener('click', event => {
      const card = event.target.closest('.option-card');
      const container = event.target.closest('.options-grid, .options-carousel');
      if (!card || !container) return;

      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      const group = container.dataset.group || card.dataset.group;

      if (group === 'notes') toggleNoteCard(card);
      else selectSingleCard(card, group);

      updateReview();
    }, true);
  }

  function validarPaso() {
    if (currentStep === 0 && !selections.intensidad) return 'Please select a fragrance type.';
    if (currentStep === 1 && !selections.cantidad) return 'Please select a size.';
    if (currentStep === 2 && !selections.base) return 'Please select a base note.';
    if (currentStep === 3 && !selections.notes.length) return 'Please select at least one note.';
    if (currentStep === 4 && !selections.idFrasco) return 'Please select a bottle.';
    return '';
  }

  function crearPayload() {
    const nombre = document.getElementById('perfumeName')?.value.trim() || 'Perfume personalizado Shine';
    const fraganciasSeleccionadas = [selections.base, ...selections.notes]
      .filter(Boolean)
      .filter((id, index, array) => array.indexOf(id) === index)
      .slice(0, 5)
      .map((idFragancia, index) => ({
        idFragancia: Number(idFragancia),
        orden: index + 1
      }));

    return {
      idUsuario: getUsuarioId(),
      idFrasco: Number(selections.idFrasco),
      nombrePersonalizado: nombre,
      intensidad: selections.intensidad,
      precioCalculado: Number(calcularPrecio().toFixed(2)),
      fragancias: fraganciasSeleccionadas
    };
  }

  async function crearYAnadirAlCarrito() {
    const finalValidation = validarPaso();
    if (finalValidation) {
      showToast(finalValidation);
      return;
    }

    const nextBtn = document.getElementById('nextStepBtn');
    if (nextBtn) {
      nextBtn.disabled = true;
      nextBtn.textContent = 'Creating…';
    }

    try {
      const perfume = await window.ShineAPI.post('/perfumes-custom', crearPayload());
      const idPerfCust = perfume.idPerfCust ?? perfume.id ?? perfume.idPerfumeCustom;

      if (typeof window.añadirPerfumeCustomAlCarrito === 'function') {
        await window.añadirPerfumeCustomAlCarrito(idPerfCust, 1);
      }

      showToast('Fragrance created and added to cart');
      setTimeout(() => {
        window.location.href = 'cart.html';
      }, 700);
    } catch (error) {
      showToast(error.message || 'Could not create the custom fragrance');
    } finally {
      if (nextBtn) {
        nextBtn.disabled = false;
        nextBtn.textContent = 'Create & Add to Cart';
      }
    }
  }

  function setupStepper() {
    const panels = document.querySelectorAll('.step-panel');
    const prevBtn = document.getElementById('prevStepBtn');
    const nextBtn = document.getElementById('nextStepBtn');
    const nameInput = document.getElementById('perfumeName');

    nextBtn?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (currentStep < panels.length - 1) {
        const validationMessage = validarPaso();
        if (validationMessage) {
          showToast(validationMessage);
          return;
        }

        currentStep++;
        updateStepper();
        updateReview();
        window.scrollTo({ top: document.getElementById('stepperProgress').offsetTop - 100, behavior: 'smooth' });
        return;
      }

      crearYAnadirAlCarrito();
    }, true);

    prevBtn?.addEventListener('click', event => {
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (currentStep > 0) {
        currentStep--;
        updateStepper();
      }
    }, true);

    nameInput?.addEventListener('input', () => {
      const previewLabel = document.getElementById('previewLabel');
      if (previewLabel) previewLabel.textContent = nameInput.value || 'Your Fragrance';
    });

    updateStepper();
  }

  function setupCarousel() {
    const scrollAmount = 176;

    function bindCarousel(carouselId, prevId, nextId) {
      const carousel = document.getElementById(carouselId);
      if (!carousel) return;

      document.getElementById(prevId)?.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
        carousel.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
      }, true);

      document.getElementById(nextId)?.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
        carousel.scrollBy({ left: scrollAmount, behavior: 'smooth' });
      }, true);
    }

    bindCarousel('typeCarousel',     'typePrev',     'typeNext');
    bindCarousel('quantityCarousel', 'quantityPrev', 'quantityNext');
    bindCarousel('baseCarousel',     'basePrev',     'baseNext');
    bindCarousel('notesCarousel',    'notesPrev',    'notesNext');
    bindCarousel('bottleCarousel',   'bottlePrev',   'bottleNext');
  }

  async function cargarOpcionesBackend() {
    if (!window.ShineAPI) {
      showToast('Could not initialize the connection with the API.');
      return;
    }

    try {
      const [frascosData, fraganciasData, notasData] = await Promise.all([
        window.ShineAPI.get('/frascos'),
        window.ShineAPI.get('/fragancias'),
        window.ShineAPI.get('/notas-olfativas')
      ]);

      frascos          = Array.isArray(frascosData)    ? frascosData    : [];
      fragancias       = Array.isArray(fraganciasData) ? fraganciasData : [];
      notasOlfativas   = Array.isArray(notasData)      ? notasData      : [];

      renderFrascos();
      renderFragancias();
      renderNotasOlfativas();
      updateReview();
    } catch (error) {
      showToast('Could not load the configurator options.');
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('stepperPanels')) return;

    prepararTiposIntensidad();
    setupSelections();
    setupStepper();
    setupCarousel();
    cargarOpcionesBackend();
    updateTypePriceReveals();
  });
})();
