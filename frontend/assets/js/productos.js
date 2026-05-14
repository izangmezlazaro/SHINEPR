// ============================================
// Shine - Catalog Integration
// ============================================

(function () {
  const FALLBACK_IMAGE = 'assets/img/product-bodyoil.png';
  const DELAY_CLASSES = ['', ' reveal--delay-1', ' reveal--delay-2'];
  const PRODUCT_CACHE_KEY = 'shine:productos:v2';
  const PRODUCT_CACHE_TTL = 10 * 60 * 1000; // 10 min — matches backend Cache-Control

  // Maps frontend filter key → id_categoria in DB
  const CATEGORIA_IDS = {
    skincare:    1,
    fragrance:   2,
    supplements: 3,
    '4':         4,  // Gift Sets — no subcategories shown
    makeup:      5,
    accessories: 6,
    haircare:    7,
  };

  // Fallback subcategories used when the API returns none (table empty or backend offline)
  const SUBCATEGORIAS_ESTATICAS = {
    1: [  // Skincare
      { idSubcategoria: null, nombre: 'Essential Oils',        keywords: ['oil', 'aceite', 'essential', 'esencial', 'argan', 'rosehip', 'jojoba', 'coconut', 'coco'] },
      { idSubcategoria: null, nombre: 'Face Care',             keywords: ['face', 'facial', 'cleanser', 'serum', 'suero', 'moisturizer', 'toner', 'mask', 'mascarilla', 'retinol', 'hyaluronic', 'brightening', 'anti-aging', 'antiage', 'contorno', 'exfolian'] },
      { idSubcategoria: null, nombre: 'Body Care',             keywords: ['body', 'corporal', 'lotion', 'butter', 'manteca', 'scrub', 'exfoliant', 'cuerpo', 'cream', 'crema', 'hidratante', 'moisturizing'] },
      { idSubcategoria: null, nombre: 'Aloe Vera Line',        keywords: ['aloe', 'vera'] },
    ],
    2: [  // Fragrances
      { idSubcategoria: null, nombre: 'Classic',               keywords: ['clasico', 'clasica', 'classic', 'classical', 'original', 'traditional', 'heritage', 'timeless', 'vintage', 'elegance', 'elegancia'] },
      { idSubcategoria: null, nombre: 'New Generation',        keywords: ['new generation', 'nueva generacion', 'modern', 'fresco', 'fresh', 'contemporary', 'neo', 'next', 'nuevo', 'generation'] },
      { idSubcategoria: null, nombre: 'Limited Edition',       keywords: ['edicion limitada', 'edicion lim', 'limited', 'limitado', 'limitada', 'edition', 'collector', 'coleccion'] },
      { idSubcategoria: null, nombre: 'Special Edition',       keywords: ['especial', 'special', 'exclusive', 'exclusivo', 'exclusiva', 'premium', 'vip', 'luxury', 'lujo'] },
    ],
    3: [  // Supplements
      { idSubcategoria: null, nombre: 'Vitamins & Health',     keywords: ['vitamin', 'vitamina', 'health', 'mineral', 'zinc', 'iron', 'hierro', 'omega', 'probiotic', 'probiotico', 'magnesio', 'magnesium', 'calcium', 'calcio'] },
      { idSubcategoria: null, nombre: 'Colostrum Health',      keywords: ['colostrum', 'calostro', 'colostro', 'inmun', 'inmune', 'immune'] },
      { idSubcategoria: null, nombre: 'Dental Care',           keywords: ['dental', 'teeth', 'dientes', 'oral', 'mouth', 'boca', 'white', 'blanqueo', 'blanqueador', 'enamel', 'esmalte', 'encías', 'gum'] },
    ],
    5: [  // Makeup
      { idSubcategoria: null, nombre: 'Face & Foundation',     keywords: ['foundation', 'base', 'concealer', 'corrector', 'blush', 'colorete', 'powder', 'polvo', 'contour', 'bronzer', 'bronceador', 'highlighter', 'iluminador', 'primer', 'setting'] },
      { idSubcategoria: null, nombre: 'Lips',                  keywords: ['lip', 'labial', 'lipstick', 'gloss', 'balm', 'balsamo', 'liner', 'labios', 'labio'] },
      { idSubcategoria: null, nombre: 'Eyes',                  keywords: ['eye', 'ojo', 'mascara', 'rimel', 'eyeliner', 'delineador', 'shadow', 'sombra', 'brow', 'cejas', 'lashes', 'pestanas'] },
    ],
    6: [  // Accessories
      { idSubcategoria: null, nombre: 'Bags & Boxes',          keywords: ['bag', 'bolsa', 'box', 'caja', 'case', 'estuche', 'neceser', 'pouch', 'packaging', 'kit', 'set'] },
      { idSubcategoria: null, nombre: 'Beauty Tools',          keywords: ['brush', 'brocha', 'pincel', 'tool', 'herramienta', 'roller', 'gua sha', 'applicator', 'aplicador', 'mirror', 'espejo', 'towel', 'toalla', 'headband', 'cinta', 'spatula', 'espatula', 'tweezer', 'pinza'] },
    ],
    7: [  // Hair Care
      { idSubcategoria: null, nombre: 'Shampoo & Conditioner', keywords: ['shampoo', 'champu', 'conditioner', 'acondicionador', 'rinse'] },
      { idSubcategoria: null, nombre: 'Styling',               keywords: ['mask', 'mascarilla', 'treatment', 'tratamiento', 'oil', 'aceite', 'serum', 'gel', 'spray', 'wax', 'cera', 'mousse', 'foam', 'style', 'styling', 'lacquer', 'laca'] },
    ],
  };

  let productos = [];
  let subcategoriasData = {};   // idCategoria → [{ idSubcategoria, nombre }]
  let filtroCategoria    = 'skincare';
  let filtroPrecio       = 'all';
  let filtroGenero       = 'all-genders';
  let filtroSubcatId       = null;  // Integer — exact match against producto.idSubcategoria
  let filtroSubcatKeywords = null;  // String[] — keyword match against product name (static subcategories)

  // ── Cache ──────────────────────────────────────────────────────────────────

  function leerProductosCache() {
    try {
      const cached = JSON.parse(localStorage.getItem(PRODUCT_CACHE_KEY));
      if (!cached?.timestamp || !Array.isArray(cached.data)) return [];
      if (Date.now() - cached.timestamp > PRODUCT_CACHE_TTL) return [];
      return cached.data;
    } catch (error) {
      return [];
    }
  }

  function guardarProductosCache(data) {
    try {
      localStorage.setItem(PRODUCT_CACHE_KEY, JSON.stringify({ timestamp: Date.now(), data }));
    } catch (error) { /* optimization only */ }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
  }

  function normalizarTexto(value) {
    return String(value ?? '').trim().toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
  }

  // ── Category normalisation — uses idCategoria from DB first ────────────────

  function normalizarCategoria(categoria) {
    const id = categoria?.idCategoria ?? categoria?.id;
    switch (id) {
      case 1: return 'skincare';
      case 2: return 'fragrance';
      case 3: return 'supplements';
      case 4: return '4';
      case 5: return 'makeup';
      case 6: return 'accessories';
      case 7: return 'haircare';
    }
    // Fallback: keyword matching for legacy/unknown data
    const nombre = normalizarTexto(categoria?.nombre || categoria?.name || categoria);
    if (nombre.includes('gift') || nombre.includes('regalo') || nombre.includes('set') || nombre === '4') return '4';
    if (nombre.includes('makeup') || nombre.includes('maquillaje') || nombre.includes('make-up')) return 'makeup';
    if (nombre.includes('hair') || nombre.includes('cabello') || nombre.includes('capilar') || nombre.includes('shampoo')) return 'haircare';
    if (nombre.includes('accesorio') || nombre.includes('accessor') || nombre.includes('brush') || nombre.includes('bolsa')) return 'accessories';
    if (nombre.includes('skin') || nombre.includes('facial') || nombre.includes('cosmetic')) return 'skincare';
    if (nombre.includes('frag') || nombre.includes('perfume') || nombre.includes('fragancia')) return 'fragrance';
    if (nombre.includes('supp') || nombre.includes('suplem') || nombre.includes('vital') || nombre.includes('colostrum')) return 'supplements';
    if (nombre.includes('body') || nombre.includes('corporal') || nombre.includes('mist') || nombre.includes('cuerpo')) return 'haircare';
    return nombre.replace(/\s+/g, '-') || 'uncategorized';
  }

  function obtenerPrecio(producto) {
    const precio = Number(producto.precio);
    return Number.isFinite(precio) ? precio : 0;
  }

  function obtenerImagen(producto) {
    let imagenPrincipal = null;
    if (Array.isArray(producto.imagenes) && producto.imagenes.length > 0) {
      imagenPrincipal = producto.imagenes[0]?.url;
    }
    if (!imagenPrincipal) return FALLBACK_IMAGE;
    const url = String(imagenPrincipal).trim();
    if (/^(https?:|data:|assets\/|\.\/|\.\.\/)/i.test(url)) return url;
    if (url.startsWith('/')) return `http://localhost:8080${url}`;
    return url;
  }

  function obtenerBadge(producto) {
    if (producto.badge || producto.etiqueta) return producto.badge || producto.etiqueta;
    if (producto.nuevo || producto.esNuevo) return 'New';
    if (producto.destacado || producto.bestSeller) return 'Best Seller';
    if (Number(producto.stock) <= 0) return 'Sold Out';
    return '';
  }

  function normalizarGenero(producto) {
    const texto = normalizarTexto((producto.genero || '') + ' ' + (producto.nombre || ''));
    if (texto.includes('mujer') || texto.includes('woman') || texto.includes('women') || texto.includes('femenino')) return 'women';
    if (texto.includes('hombre') || texto.includes('man') || texto.includes('men') || texto.includes('masculino')) return 'men';
    return 'unisex';
  }

  function normalizarProducto(producto) {
    return {
      ...producto,
      id:             producto.idProducto,
      nombre:         producto.nombre || 'Producto Shine',
      precio:         obtenerPrecio(producto),
      imagen:         obtenerImagen(producto),
      categoria:      normalizarCategoria(producto.categoria),
      idSubcategoria: producto.idSubcategoria ?? null,
      genero:         normalizarGenero(producto),
      badge:          obtenerBadge(producto)
    };
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(precio);
  }

  function crearTarjetaProducto(producto, index) {
    const delayClass    = DELAY_CLASSES[index % DELAY_CLASSES.length];
    const disabledClass = Number(producto.stock) <= 0 ? ' product-card--disabled' : '';
    const badgeHtml     = producto.badge ? `<span class="product-card__badge">${escapeHtml(producto.badge)}</span>` : '';

    return `
      <article
        class="product-card reveal visible${delayClass}${disabledClass}"
        id="prod-${escapeHtml(producto.id)}"
        data-id="${escapeHtml(producto.id)}"
        data-name="${escapeHtml(producto.nombre)}"
        data-price="${escapeHtml(producto.precio.toFixed(2))}"
        data-img="${escapeHtml(producto.imagen)}"
        data-category="${escapeHtml(producto.categoria)}"
      >
        <div class="product-card__img-wrap">
          ${badgeHtml}
          <img class="product-card__img" src="${escapeHtml(producto.imagen)}" alt="${escapeHtml(producto.nombre)}" loading="lazy" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'">
          <button type="button" class="product-card__action" aria-label="Add to cart">
            <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
          </button>
        </div>
        <div class="product-card__body">
          <div class="product-card__name">${escapeHtml(producto.nombre)}</div>
          <div class="product-card__price">${escapeHtml(formatearPrecio(producto.precio))}</div>
        </div>
      </article>
    `;
  }

  function actualizarContador(cantidad) {
    const contador = document.querySelector('.shop-toolbar .text-sm strong');
    if (contador) contador.textContent = String(cantidad);
  }

  function mostrarSkeletons(grid, count = 8) {
    grid.innerHTML = Array.from({ length: count }, () => `
      <article class="product-card product-card--skeleton">
        <div class="product-card__img-wrap skeleton-box"></div>
        <div class="product-card__body">
          <div class="skeleton-line skeleton-line--wide"></div>
          <div class="skeleton-line skeleton-line--narrow"></div>
        </div>
      </article>
    `).join('');
  }

  function mostrarEstado(grid, mensaje) {
    grid.innerHTML = `<div class="cart-empty" style="grid-column:1/-1"><p>${escapeHtml(mensaje)}</p></div>`;
    actualizarContador(0);
  }

  // ── Filters ────────────────────────────────────────────────────────────────

  function filtrarPorPrecio(producto) {
    if (filtroPrecio === 'under-50')  return producto.precio < 50;
    if (filtroPrecio === '50-100')    return producto.precio >= 50 && producto.precio <= 100;
    if (filtroPrecio === 'over-100')  return producto.precio > 100;
    return true;
  }

  function filtrarPorSubcategoria(producto) {
    if (filtroSubcatId !== null) return producto.idSubcategoria === filtroSubcatId;
    if (filtroSubcatKeywords !== null) {
      const texto = normalizarTexto(
        (producto.nombre        || '') + ' ' +
        (producto.descripcion   || '') + ' ' +
        (producto.tipoFragancia || '')
      );
      return filtroSubcatKeywords.some(kw => texto.includes(normalizarTexto(kw)));
    }
    return true;
  }

  function ordenarProductos(items) {
    const sort = document.getElementById('shopSort')?.value || 'Featured';
    const copia = [...items];
    if (sort === 'Price: Low to High')  return copia.sort((a, b) => a.precio - b.precio);
    if (sort === 'Price: High to Low')  return copia.sort((a, b) => b.precio - a.precio);
    if (sort === 'New Arrivals')        return copia.sort((a, b) => Number(b.id) - Number(a.id));
    return copia;
  }

  function renderizarCatalogo() {
    const grid = document.getElementById('productGrid');
    if (!grid) return;

    const visibles = ordenarProductos(productos.filter(producto => {
      const coincideCategoria    = filtroCategoria === 'all' || producto.categoria === filtroCategoria;
      const coincideGenero       = filtroGenero === 'all-genders' || producto.genero === filtroGenero;
      const coincideSubcategoria = filtrarPorSubcategoria(producto);
      return coincideCategoria && coincideGenero && filtrarPorPrecio(producto) && coincideSubcategoria;
    }));

    if (!visibles.length) {
      mostrarEstado(grid, 'No products found with these filters.');
      return;
    }

    grid.innerHTML = visibles.map(crearTarjetaProducto).join('');
    grid.querySelectorAll('.product-card.reveal').forEach(card => card.classList.add('visible'));
    actualizarContador(visibles.length);
  }

  // ── Subcategory UI ─────────────────────────────────────────────────────────

  function cerrarSubcategorias(categoryList) {
    categoryList.querySelectorAll('.filter-subcategory').forEach(el => el.remove());
    categoryList.querySelectorAll('.filter-item--has-sub').forEach(i => i.classList.remove('expanded'));
  }

  function abrirSubcategorias(item, categoryList) {
    const catKey = item.dataset.filter;
    if (catKey === '4' || catKey === 'all') return; // Gift Sets: no subcategories
    const catId = CATEGORIA_IDS[catKey];
    if (!catId) return;

    // Use API subcategories if available, otherwise fall back to static definitions
    const apiSubs = subcategoriasData[catId] || [];
    const subs = apiSubs.length ? apiSubs : (SUBCATEGORIAS_ESTATICAS[catId] || []);
    if (!subs.length) return;

    item.classList.add('expanded');
    const container = document.createElement('div');
    container.className = 'filter-subcategory';

    subs.forEach(sub => {
      const subItem = document.createElement('div');
      subItem.className = 'filter-item filter-item--sub';
      subItem.textContent = sub.nombre;
      subItem.addEventListener('click', e => {
        e.stopPropagation();
        container.querySelectorAll('.filter-item--sub').forEach(s => s.classList.remove('active'));
        subItem.classList.add('active');
        if (sub.idSubcategoria !== null) {
          // API subcategory: filter by exact DB id
          filtroSubcatId = sub.idSubcategoria;
          filtroSubcatKeywords = null;
        } else {
          // Static fallback: filter by keyword matching against product names
          filtroSubcatId = null;
          filtroSubcatKeywords = sub.keywords || [];
        }
        renderizarCatalogo();
      });
      container.appendChild(subItem);
    });

    item.insertAdjacentElement('afterend', container);
  }

  // ── Filter wiring ──────────────────────────────────────────────────────────

  function configurarFiltros() {
    document.querySelectorAll('#shopFilters .filter-group').forEach(group => {
      const titulo = group.querySelector('.filter-group__title')?.textContent.trim().toLowerCase();
      const list   = group.querySelector('.filter-group__list');

      if (titulo === 'category') {
        list.querySelectorAll('.filter-item').forEach(item => {
          const cat = item.dataset.filter;
          // Mark items that have subcategories (all except 'all' and Gift Sets)
          if (cat && cat !== 'all' && cat !== '4' && CATEGORIA_IDS[cat]) {
            item.classList.add('filter-item--has-sub');
          }

          item.addEventListener('click', () => {
            const subcatsYaAbiertas = !!list.querySelector('.filter-subcategory');
            const mismaCategoria    = filtroCategoria === (item.dataset.filter || 'all');
            cerrarSubcategorias(list);
            list.querySelectorAll('.filter-item:not(.filter-item--sub)').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            filtroCategoria      = item.dataset.filter || 'all';
            filtroSubcatId       = null;
            filtroSubcatKeywords = null;

            // Abrir subcategorías salvo que el usuario esté cerrando las ya abiertas (toggle off)
            if (!(mismaCategoria && subcatsYaAbiertas)) {
              abrirSubcategorias(item, list);
            }
            renderizarCatalogo();
          });
        });
      } else {
        group.querySelectorAll('.filter-item').forEach(item => {
          item.addEventListener('click', () => {
            group.querySelectorAll('.filter-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            if (titulo === 'gender') {
              filtroGenero = item.dataset.filter || 'all-genders';
            }
            if (titulo === 'price') {
              const text = item.textContent.trim().toLowerCase();
              if (text.includes('under'))                         filtroPrecio = 'under-50';
              else if (text.includes('50') && text.includes('100')) filtroPrecio = '50-100';
              else if (text.includes('over'))                     filtroPrecio = 'over-100';
              else                                                filtroPrecio = 'all';
            }
            renderizarCatalogo();
          });
        });
      }
    });

    document.getElementById('shopSort')?.addEventListener('change', renderizarCatalogo);
  }

  // ── Hidden products ────────────────────────────────────────────────────────

  function leerProductosOcultos() {
    try { return JSON.parse(localStorage.getItem('shineHiddenProducts') || '[]'); } catch { return []; }
  }

  function filtrarOcultos(lista) {
    const ocultos = leerProductosOcultos();
    if (!ocultos.length) return lista;
    return lista.filter(p => !ocultos.includes(p.idProducto ?? p.id));
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  async function cargarSubcategorias() {
    if (!window.ShineAPI) return;
    try {
      const data = await window.ShineAPI.get('/subcategorias');
      if (Array.isArray(data)) {
        subcategoriasData = {};
        data.forEach(sub => {
          const catId = sub.idCategoria;
          if (!subcategoriasData[catId]) subcategoriasData[catId] = [];
          subcategoriasData[catId].push(sub);
        });
      }
    } catch (e) { /* subcategories are optional — show all products if this fails */ }
  }

  async function cargarProductos() {
    const grid = document.getElementById('productGrid');
    if (!grid) return;

    if (!window.ShineAPI) {
      mostrarEstado(grid, 'Could not initialize API connection.');
      return;
    }

    const productosCache = leerProductosCache();
    if (productosCache.length) {
      productos = filtrarOcultos(productosCache).map(normalizarProducto);
      renderizarCatalogo();
    } else {
      mostrarSkeletons(grid);
    }

    try {
      const data = await window.ShineAPI.get('/productos');
      if (Array.isArray(data)) guardarProductosCache(data);
      productos = Array.isArray(data) ? filtrarOcultos(data).map(normalizarProducto) : [];

      if (!productos.length) {
        mostrarEstado(grid, 'No products available in the catalog yet.');
        return;
      }
      renderizarCatalogo();
    } catch (error) {
      if (!productosCache.length) {
        mostrarEstado(grid, 'Could not load products. Please ensure the backend is running.');
      }
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('productGrid')) return;

    configurarFiltros();

    // Read ?category= from URL
    const urlCategory = new URLSearchParams(window.location.search).get('category');
    if (urlCategory) {
      filtroCategoria = urlCategory;
      const categoryGroup = document.querySelector('#shopFilters .filter-group');
      if (categoryGroup) {
        categoryGroup.querySelectorAll('.filter-item').forEach(item => {
          item.classList.remove('active');
          if (item.dataset.filter === urlCategory) item.classList.add('active');
        });
      }
    }

    // Load subcategories and products in parallel
    cargarSubcategorias();
    cargarProductos();
  });

  window.ShineProductCache = {
    key: PRODUCT_CACHE_KEY,
    read: leerProductosCache,
    write: guardarProductosCache
  };
})();
