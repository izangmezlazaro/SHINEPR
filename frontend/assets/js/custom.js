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

  // ── Canvas preview constants ─────────────────────────────────────────
  const CANVAS_BASE_SRC = 'assets/img/custom-perfume-bg.png';
  const BOTTLE_IMG_MAP = {
    '4':  'assets/img/types/IMPACT_FRASCO.png',
    '5':  'assets/img/types/GRANADE_FRASCO.jpg',
    '6':  'assets/img/types/DIAMOND_FRASCO.JPG',
    '7':  'assets/img/types/PEARL_FRASCO.JPG',
    '8':  'assets/img/types/GENTLEMEN_FRASCO.JPG',
    '9':  'assets/img/types/CHAMPIONS_FRASCO.png',
    '10': 'assets/img/types/HEEL_FRASCO.JPG',
    '11': 'assets/img/types/PANTHER_FRASCO.png'
  };
  /** Matches static bottle cards when API /frascos IDs differ or fail to load. */
  const BOTTLE_SHAPE_BY_ID = {
    '4': 'Impact silhouette bottle',
    '5': 'Grenade-shaped luxury bottle',
    '6': 'Diamond-cut faceted crystal bottle',
    '7': 'Pearl-inspired rounded elegant bottle',
    '8': 'Classic gentlemen rectangular bottle',
    '9': 'Trophy champions sculptural bottle',
    '10': 'High heel sculptural stiletto bottle',
    '11': 'Panther sleek feline sculptural bottle'
  };
  const INTENSITY_PARTICLE_COLOR = {
    elixir:          '#c46090',
    eau_de_parfum:   '#d480aa',
    eau_de_toilette: '#e8a8c8',
    eau_de_cologne:  '#d4a0b8',
    body_mist:       '#f0c8dc'
  };
  const _imgCache = {};
  let _canvasTimer = null;
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
    const labels = {
      elixir: 'Elixir',
      eau_de_parfum: 'Eau de Parfum',
      eau_de_toilette: 'Eau de Toilette',
      eau_de_cologne: 'Eau de Cologne',
      body_mist: 'Eau Solide'
    };
    return labels[intensidad] || '—';
  }

  function intensidadDesdeCard(card) {
    const value = normalizarTexto(card.dataset.value);
    const text = normalizarTexto(card.textContent);

    if (value === 'elixir' || text.includes('elixir')) return 'elixir';
    if (value === 'body_mist' || text.includes('solide') || text.includes('mist')) return 'body_mist';
    if (value === 'eau_de_cologne' || text.includes('cologne')) return 'eau_de_cologne';
    if (value === 'eau_de_toilette' || text.includes('toilette')) return 'eau_de_toilette';
    if (value === 'eau_de_parfum' || text.includes('parfum')) return 'eau_de_parfum';
    return value || 'eau_de_parfum';
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

  /** Shape description for AI when DB row is missing (static carousel). */
  function getFrascoFormaParaPrompt() {
    const id = String(selections.idFrasco ?? '');
    if (BOTTLE_SHAPE_BY_ID[id]) return BOTTLE_SHAPE_BY_ID[id];
    
    const frasco = selections.idFrasco ? getFrasco(selections.idFrasco) : null;
    if (frasco?.forma) return String(frasco.forma).trim();
    
    const lbl = selections.bottleLabel;
    return (lbl && String(lbl).trim()) || 'sleek cylindrical glass perfume bottle';
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
    if (nextBtn) nextBtn.textContent = currentStep === panels.length - 1 ? 'Create & Add to Cart' : 'Next Step';
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

    const nameInput = document.getElementById('perfumeName');
    const previewLabel = document.getElementById('previewLabel');
    if (nameInput && previewLabel) {
      const raw = nameInput.value || '';
      previewLabel.textContent = raw.trim() || 'Your Fragrance';
    }

    updateTypePriceReveals();
    updatePriceBar();
    renderCanvasPreview();
    updateBottleOverlay();

    const personalityProfile = document.getElementById('personalityProfile');
    if (personalityProfile) {
      let phrase = 'A perfectly balanced creation, tailored exclusively for you.';
      const typeStr = (selections.intensidad || '').toLowerCase();
      const baseLabel = (getFraganciaLabel(selections.base) || '').toLowerCase();
      const notesCombined = noteLabels.join(' ').toLowerCase();

      if (typeStr === 'elixir' && (baseLabel.includes('wood') || baseLabel.includes('musk') || notesCombined.includes('leather'))) {
        phrase = 'A bold, intense, and mysterious profile. Crafted for the modern visionary who leaves an unforgettable mark.';
      } else if (typeStr === 'elixir') {
        phrase = 'Rich, opulent, and uncompromisingly luxurious. An intense signature scent that speaks volumes.';
      } else if (baseLabel.includes('floral') || notesCombined.includes('rose') || notesCombined.includes('jasmine')) {
        phrase = 'An elegant, romantic, and blooming fragrance. Perfect for a sophisticated spirit with a timeless charm.';
      } else if (baseLabel.includes('citrus') || notesCombined.includes('lemon') || notesCombined.includes('bergamot')) {
        phrase = 'Bright, energetic, and vibrantly fresh. Designed for an active, optimistic personality that radiates light.';
      } else if (baseLabel.includes('wood') || notesCombined.includes('sandalwood') || notesCombined.includes('cedar')) {
        phrase = 'Grounded, warm, and distinctly confident. A sophisticated composition for a composed and strong character.';
      } else if (typeStr.includes('cologne') || typeStr.includes('mist')) {
        phrase = 'Light, airy, and effortlessly graceful. The ultimate subtle touch for a clean and minimalist lifestyle.';
      }
      personalityProfile.innerHTML = `“${phrase}”`;
    }
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

    const nameInput = document.getElementById('perfumeName');
    const rawName = nameInput?.value?.trim();
    if (!rawName) {
      showToast('Please enter a name for your fragrance before adding to the cart.');
      return;
    }

    const aiImg = document.getElementById('aiResultImage')?.src;
    const imagenFinal = (aiImg && (aiImg.startsWith('data:') || aiImg.startsWith('http'))) ? aiImg : BOTTLE_IMAGE;

    if (!getUsuarioId()) {
      // Guest: save to guest cart and go to cart
      const payload = crearPayload();
      const guestItems = (function () {
        try { return JSON.parse(localStorage.getItem('shineGuestCart')) || []; } catch (_) { return []; }
      })();
      guestItems.push({
        type: 'custom',
        payload,
        nombre: payload.nombrePersonalizado || 'Custom Fragrance',
        perfumeName: payload.nombrePersonalizado,
        precioCalculado: payload.precioCalculado,
        imagenUrl: imagenFinal,
        cantidad: 1
      });
      localStorage.setItem('shineGuestCart', JSON.stringify(guestItems));
      showToast('Fragrance added to cart');
      setTimeout(() => { window.location.href = 'cart.html'; }, 700);
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

      if (imagenFinal.startsWith('data:')) {
        try { sessionStorage.setItem('custom_img_' + idPerfCust, imagenFinal); } catch(e) {}
      }

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
      if (previewLabel) {
        const raw = nameInput.value || '';
        previewLabel.textContent = raw.trim() || 'Your Fragrance';
      }
      renderCanvasPreview();
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

  // ── Canvas Preview System ────────────────────────────────────────────────
  function _loadImg(src) {
    if (_imgCache[src]) return Promise.resolve(_imgCache[src]);
    return new Promise(resolve => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload  = () => { _imgCache[src] = img; resolve(img); };
      img.onerror = () => resolve(null);
      img.src = src;
    });
  }

  function renderCanvasPreview() {
    clearTimeout(_canvasTimer);
    _canvasTimer = setTimeout(_doRenderCanvas, 30);
  }

  async function _doRenderCanvas() {
    const canvas = document.getElementById('previewCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const W = canvas.width, H = canvas.height;

    // 1. Dark premium gradient background
    const grad = ctx.createLinearGradient(0, 0, 0, H);
    grad.addColorStop(0,   '#1c0812');
    grad.addColorStop(0.5, '#0d0408');
    grad.addColorStop(1,   '#080406');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, W, H);

    // 2. Atmosphere base image
    const baseImg = await _loadImg(CANVAS_BASE_SRC);
    if (baseImg) {
      ctx.globalAlpha = 0.55;
      ctx.drawImage(baseImg, 0, 0, W, H);
      ctx.globalAlpha = 1;
    }

    // 3. Deterministic sparkle particles (by intensity type)
    const pColor = INTENSITY_PARTICLE_COLOR[selections.intensidad] || '#d4af37';
    _drawParticles(ctx, W, H, pColor);

    // 4. Subtle glow in centre (where bottle sits)
    const glow = ctx.createRadialGradient(W/2, H*0.42, 0, W/2, H*0.42, W*0.38);
    glow.addColorStop(0, pColor + '1a');
    glow.addColorStop(1, 'transparent');
    ctx.fillStyle = glow;
    ctx.fillRect(0, 0, W, H);

    // 5. Type + ml label (very top, subtle)
    const tipoLabel = labelIntensidad(selections.intensidad);
    const mlLabel   = selections.cantidad ? `${selections.cantidad} ml` : '';
    const topLine   = [tipoLabel !== '\u2014' ? tipoLabel : null, mlLabel].filter(Boolean).join(' \u00B7 ');
    if (topLine) {
      ctx.save();
      ctx.textAlign    = 'center';
      ctx.textBaseline = 'middle';
      ctx.font         = '10px Inter, sans-serif';
      ctx.fillStyle    = 'rgba(255,248,235,0.30)';
      ctx.fillText(topLine, W / 2, H * 0.05);
      ctx.restore();
    }

    // 6. Vignette
    const vign = ctx.createRadialGradient(W/2, H/2, H*0.25, W/2, H/2, H*0.72);
    vign.addColorStop(0, 'transparent');
    vign.addColorStop(1, 'rgba(0,0,0,0.65)');
    ctx.fillStyle = vign;
    ctx.fillRect(0, 0, W, H);

    // NOTE: Bottle, name and note chips are rendered as CSS layers over the canvas
    // See: .canvas-bottle-overlay, .canvas-text-overlay in the HTML
  }

  function _drawParticles(ctx, W, H, color) {
    const N = 22;
    const seed = (selections.intensidad || 'edp').split('').reduce((a, c) => a + c.charCodeAt(0), 0);
    ctx.save();
    for (let i = 0; i < N; i++) {
      const sx    = ((seed * (i + 1) * 7919) % 97) / 97;
      const sy    = ((seed * (i + 3) * 6271) % 97) / 97;
      const size  = 0.8 + ((seed * (i + 5) * 3571) % 100) / 60;
      const alpha = 0.10 + ((seed * (i + 7) * 4513) % 60) / 220;
      ctx.beginPath();
      ctx.arc(sx * W, sy * H, size, 0, Math.PI * 2);
      ctx.fillStyle = color + Math.round(alpha * 255).toString(16).padStart(2, '0');
      ctx.fill();
    }
    ctx.restore();
  }

  function initCanvasPreview() {
    const canvas = document.getElementById('previewCanvas');
    if (!canvas) return;
    canvas.width  = 480;
    canvas.height = 480;
    _loadImg(CANVAS_BASE_SRC);
    updateBottleOverlay();
    renderCanvasPreview();
  }

  function updateBottleOverlay() {
    const img = document.getElementById('previewBottleImg');
    if (!img) return;
    const rel = BOTTLE_IMG_MAP[String(selections.idFrasco)] || '';
    if (img.dataset.overlayBottle !== rel) {
      img.dataset.overlayBottle = rel;
      img.src = rel;
    }
    img.classList.remove('canvas-bottle-overlay--hidden');
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

  // ── AI Generation overlay helpers ───────────────────────────────────────
  const AI_MESSAGES = [
    'Blending your chosen notes…',
    'Composing light and shadow…',
    'Rendering glass reflections…',
    'Applying premium studio lighting…',
    'Adding gold typography…',
    'Final render in progress…'
  ];

  /** OpenAI /v1/images/generations: prefer inline base64 (reliable in img); URLs often break in the browser. */
  function imageUrlFromOpenAiImagesResponse(data) {
    const item = data?.data?.[0];
    if (!item) return null;
    const raw = item.b64_json ?? item.base64 ?? item.image_base64;
    if (typeof raw === 'string' && raw.trim().length > 0) {
      const b64 = raw.replace(/\s/g, '');
      let mime = 'image/png';
      if (b64.startsWith('/9j') || b64.startsWith('/9J')) mime = 'image/jpeg';
      else if (b64.startsWith('UklGR')) mime = 'image/webp';
      else if (b64.startsWith('iVBOR')) mime = 'image/png';
      return `data:${mime};base64,${b64}`;
    }
    const url = typeof item.url === 'string' ? item.url.trim() : '';
    if (url && /^https?:\/\//i.test(url)) return url;
    return null;
  }

  function aiOverlayShow() {
    const overlay = document.getElementById('aiGeneratingOverlay');
    const stateLoad = document.getElementById('aiStateLoading');
    const stateResult = document.getElementById('aiStateResult');
    const msgEl = document.getElementById('aiLoadingMessage');
    if (!overlay) return;

    if (overlay._msgTimer) clearInterval(overlay._msgTimer);
    if (overlay._segTick) clearInterval(overlay._segTick);
    overlay._segTimers?.forEach(t => clearTimeout(t));

    stateLoad.style.display = 'flex';
    stateResult.style.display = 'none';
    overlay.classList.add('ai-overlay--visible');
    document.body.style.overflow = 'hidden';

    document.querySelectorAll('.ai-seg').forEach(s => s.classList.remove('done', 'active'));

    let msgIdx = 0;
    if (msgEl) msgEl.textContent = AI_MESSAGES[0];
    overlay._msgTimer = setInterval(() => {
      msgIdx = (msgIdx + 1) % AI_MESSAGES.length;
      if (msgEl) msgEl.textContent = AI_MESSAGES[msgIdx];
    }, 2200);

    const segs = Array.from(document.querySelectorAll('.ai-seg'));
    const t0 = Date.now();
    const tick = () => {
      if (!segs.length) return;
      const elapsed = Date.now() - t0;
      const phase = Math.min(segs.length - 1, Math.floor(elapsed / 700));
      segs.forEach((s, j) => {
        s.classList.toggle('done', j < phase);
        s.classList.toggle('active', j === phase);
      });
    };
    tick();
    overlay._segTick = setInterval(tick, 400);
  }

  function aiOverlayShowResult(imageUrl, name) {
    const overlay = document.getElementById('aiGeneratingOverlay');
    const stateLoad = document.getElementById('aiStateLoading');
    const stateResult = document.getElementById('aiStateResult');
    const resultImg = document.getElementById('aiResultImage');
    const resultName = document.getElementById('aiResultName');

    if (overlay?._msgTimer) clearInterval(overlay._msgTimer);
    if (overlay?._segTick) clearInterval(overlay._segTick);
    overlay?._segTimers?.forEach(t => clearTimeout(t));

    document.querySelectorAll('.ai-seg').forEach(s => {
      s.classList.remove('active');
      s.classList.add('done');
    });

    if (resultImg) {
      resultImg.loading = 'eager';
      resultImg.src = '';
      requestAnimationFrame(() => {
        resultImg.src = imageUrl;
      });
    }
    if (resultName) resultName.textContent = name || 'Your Exclusive Fragrance';

    stateLoad.style.display = 'none';
    stateResult.style.display = 'flex';
  }

  function aiOverlayHide() {
    const overlay = document.getElementById('aiGeneratingOverlay');
    if (!overlay) return;
    if (overlay._msgTimer) clearInterval(overlay._msgTimer);
    if (overlay._segTick) clearInterval(overlay._segTick);
    overlay._segTimers?.forEach(t => clearTimeout(t));
    overlay.classList.remove('ai-overlay--visible');
    document.body.style.overflow = '';
  }

  function setupGeneradorIA() {
    const btnGenerar = document.getElementById('btnGenerarAI');
    const closeBtn   = document.getElementById('aiCloseBtn');

    if (!btnGenerar) return;

    // Close overlay → draw AI image onto canvas
    closeBtn?.addEventListener('click', () => {
      const resultImg = document.getElementById('aiResultImage');
      if (resultImg?.src) {
        const canvas = document.getElementById('previewCanvas');
        if (canvas) {
          const ctx = canvas.getContext('2d');
          const aiImg = new Image();
          aiImg.crossOrigin = 'anonymous';
          aiImg.onload = () => {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(aiImg, 0, 0, canvas.width, canvas.height);
            document.getElementById('previewBottleImg')?.classList.add('canvas-bottle-overlay--hidden');
          };
          aiImg.src = resultImg.src;
        }
      }
      aiOverlayHide();
      showToast('✦ AI design applied to your preview!');
    });

    // Close on backdrop click
    document.getElementById('aiGeneratingOverlay')?.addEventListener('click', (e) => {
      if (e.target === e.currentTarget) {
        const stateResult = document.getElementById('aiStateResult');
        if (stateResult?.style.display !== 'none') aiOverlayHide();
      }
    });

    btnGenerar.addEventListener('click', async () => {
      const tipo = selections.intensidad || '';
      const base = selections.base ? getFraganciaLabel(selections.base) : '';
      const notas = selections.notes.map(id => notasOlfativas.find(n => Number(n.idNota) === Number(id))?.nombre || getFraganciaLabel(id)).filter(Boolean).join(', ');
      const frascoObj = selections.idFrasco ? getFrasco(selections.idFrasco) : null;
      const frasco = frascoObj ? `${frascoObj.forma} (${frascoObj.capacidadMl}ml)` : '';
      const formaElegida = getFrascoFormaParaPrompt();
      const nameInput = document.getElementById('perfumeName');
      const rawName = nameInput?.value ?? '';
      const nombreParaMostrar = rawName.trim() || 'Shine Custom';
      const nombreParaEnvio = rawName.replace(/\r?\n/g, ' ').trim() || 'Shine Custom';

      if (!tipo && !base && !notas && !selections.idFrasco) {
        showToast('Please select some options before generating your design.');
        return;
      }

      let genUrl = '/api/generar-custom';
      try {
        if (window.ShineAPI?.baseUrl) {
          genUrl = new URL('/api/generar-custom', window.ShineAPI.baseUrl).href;
        }
      } catch (_) { /* keep relative */ }

      aiOverlayShow();
      btnGenerar.disabled = true;

      try {
        const params = new URLSearchParams();
        params.append('tipo', labelIntensidad(tipo));
        params.append('base', base);
        params.append('notas', notas);
        params.append('frasco', frasco);
        params.append('formaElegida', formaElegida);
        params.append('nombreUsuario', nombreParaEnvio);

        const response = await fetch(genUrl, {
          method: 'POST',
          body: params,
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });

        let data;
        try { data = await response.json(); } catch (_) { data = null; }

        if (!response.ok) {
          const msg = [data?.error, data?.detail].filter(Boolean).join(' — ') || `HTTP ${response.status}`;
          throw new Error(msg.length > 400 ? msg.slice(0, 400) + '…' : msg);
        }

        const imageUrl = imageUrlFromOpenAiImagesResponse(data);

        if (!imageUrl) throw new Error('The AI did not return a valid image. Please try again.');

        aiOverlayShowResult(imageUrl, nombreParaMostrar);
        const previewLabel = document.getElementById('previewLabel');
        if (previewLabel) previewLabel.textContent = nombreParaMostrar;
      } catch (error) {
        console.error('[Shine AI]', error);
        aiOverlayHide();
        showToast('⚠️ ' + (error.message || 'Could not generate your design. Please try again.'));
      } finally {
        btnGenerar.disabled = false;
      }
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('stepperPanels')) return;

    prepararTiposIntensidad();
    setupSelections();
    setupStepper();
    setupCarousel();
    setupGeneradorIA();
    initCanvasPreview();
    cargarOpcionesBackend();
    updateTypePriceReveals();
  });
})();
