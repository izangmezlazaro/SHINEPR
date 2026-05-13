// ============================================
// Shine - Catalog Integration
// ============================================

(function () {
  const FALLBACK_IMAGE = 'assets/img/product-perfume.png';
  const DELAY_CLASSES = ['', ' reveal--delay-1', ' reveal--delay-2'];
  const PRODUCT_CACHE_KEY = 'shine:productos:v2';
  const PRODUCT_CACHE_TTL = 10 * 60 * 1000; // 10 min — matches backend Cache-Control

  let productos = [];
  let filtroCategoria = 'skincare';
  let filtroPrecio = 'all';
  let filtroGenero = 'all-genders';

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
      localStorage.setItem(PRODUCT_CACHE_KEY, JSON.stringify({
        timestamp: Date.now(),
        data
      }));
    } catch (error) {
      // Cache is an optimization only.
    }
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function normalizarTexto(value) {
    return String(value ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  function normalizarCategoria(categoria) {
    const nombre = normalizarTexto(categoria?.nombre || categoria?.name || categoria);

    if (nombre.includes('gift') || nombre.includes('regalo') || nombre.includes('set') || nombre === '4' || String(categoria?.id) === '4' || String(categoria?.idCategoria) === '4') return '4';
    if (nombre.includes('makeup') || nombre.includes('maquillaje') || nombre.includes('cosmetica') || nombre.includes('make-up')) return 'makeup';
    if (nombre.includes('body') || nombre.includes('corporal') || nombre.includes('mist') || nombre.includes('cuerpo')) return 'bodycare';
    if (nombre.includes('skin') || nombre.includes('facial') || nombre.includes('cosmetic')) return 'skincare';
    if (nombre.includes('frag') || nombre.includes('perfume') || nombre.includes('fragancia')) return 'fragrance';
    if (nombre.includes('supp') || nombre.includes('suplem') || nombre.includes('vital') || nombre.includes('gummy') || nombre.includes('gomina') || nombre.includes('nutri')) return 'supplements';

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
    const id = producto.idProducto;
    const nombre = producto.nombre || 'Producto Shine';
    const precio = obtenerPrecio(producto);
    const imagen = obtenerImagen(producto);
    const categoria = normalizarCategoria(producto.categoria);
    const genero = normalizarGenero(producto);

    return {
      ...producto,
      id,
      nombre,
      precio,
      imagen,
      categoria,
      genero,
      badge: obtenerBadge(producto)
    };
  }

  function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(precio);
  }

  function crearTarjetaProducto(producto, index) {
    const delayClass = DELAY_CLASSES[index % DELAY_CLASSES.length];
    const disabledClass = Number(producto.stock) <= 0 ? ' product-card--disabled' : '';
    const badgeHtml = producto.badge
      ? `<span class="product-card__badge">${escapeHtml(producto.badge)}</span>`
      : '';

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
    grid.innerHTML = `
      <div class="cart-empty" style="grid-column:1/-1">
        <p>${escapeHtml(mensaje)}</p>
      </div>
    `;
    actualizarContador(0);
  }

  function filtrarPorPrecio(producto) {
    if (filtroPrecio === 'under-50') return producto.precio < 50;
    if (filtroPrecio === '50-100') return producto.precio >= 50 && producto.precio <= 100;
    if (filtroPrecio === 'over-100') return producto.precio > 100;
    return true;
  }

  function ordenarProductos(items) {
    const sort = document.getElementById('shopSort')?.value || 'Featured';
    const copia = [...items];

    if (sort === 'Price: Low to High') {
      return copia.sort((a, b) => a.precio - b.precio);
    }

    if (sort === 'Price: High to Low') {
      return copia.sort((a, b) => b.precio - a.precio);
    }

    if (sort === 'New Arrivals') {
      return copia.sort((a, b) => Number(b.id) - Number(a.id));
    }

    return copia;
  }

  function renderizarCatalogo() {
    const grid = document.getElementById('productGrid');
    if (!grid) return;

    const visibles = ordenarProductos(productos.filter(producto => {
      const coincideCategoria = filtroCategoria === 'all' || producto.categoria === filtroCategoria;
      const coincideGenero = filtroGenero === 'all-genders' || producto.genero === filtroGenero;
      return coincideCategoria && coincideGenero && filtrarPorPrecio(producto);
    }));

    if (!visibles.length) {
      mostrarEstado(grid, 'No products found with these filters.');
      return;
    }

    const htmlProductos = visibles.map(crearTarjetaProducto).join('');
    grid.innerHTML = htmlProductos;
    grid.querySelectorAll('.product-card.reveal').forEach(card => card.classList.add('visible'));
    actualizarContador(visibles.length);
  }

  function configurarFiltros() {
    document.querySelectorAll('#shopFilters .filter-group').forEach(group => {
      const titulo = group.querySelector('.filter-group__title')?.textContent.trim().toLowerCase();

      group.querySelectorAll('.filter-item').forEach(item => {
        item.addEventListener('click', () => {
          group.querySelectorAll('.filter-item').forEach(i => i.classList.remove('active'));
          item.classList.add('active');

          if (titulo === 'category') {
            filtroCategoria = item.dataset.filter || 'all';
          }
          if (titulo === 'gender') {
            filtroGenero = item.dataset.filter || 'all-genders';
          }

          if (titulo === 'price') {
            const text = item.textContent.trim().toLowerCase();
            if (text.includes('under')) filtroPrecio = 'under-50';
            else if (text.includes('50') && text.includes('100')) filtroPrecio = '50-100';
            else if (text.includes('over')) filtroPrecio = 'over-100';
            else filtroPrecio = 'all';
          }

          renderizarCatalogo();
        });
      });
    });

    document.getElementById('shopSort')?.addEventListener('change', renderizarCatalogo);
  }

  function leerProductosOcultos() {
    try { return JSON.parse(localStorage.getItem('shineHiddenProducts') || '[]'); } catch { return []; }
  }

  function filtrarOcultos(lista) {
    const ocultos = leerProductosOcultos();
    if (!ocultos.length) return lista;
    return lista.filter(p => !ocultos.includes(p.idProducto ?? p.id));
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
      // Instant render from cache — user sees products in <50ms
      productos = filtrarOcultos(productosCache).map(normalizarProducto);
      renderizarCatalogo();
    } else {
      // No cache yet — show skeleton placeholders immediately
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
      // If we already showed cached data, keep it visible — don't replace with an error
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('productGrid')) return;

    configurarFiltros();
    cargarProductos();
  });

  window.ShineProductCache = {
    key: PRODUCT_CACHE_KEY,
    read: leerProductosCache,
    write: guardarProductosCache
  };
})();
