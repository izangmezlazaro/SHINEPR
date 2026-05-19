// ============================================
// Shine - Product Detail Integration
// ============================================

(function () {
  const FALLBACK_IMAGE = 'assets/img/product-bodyoil.png';
  const PRODUCT_CACHE_KEY = 'shine:productos:v2'; // must match productos.js
  const PRODUCT_CACHE_TTL = 10 * 60 * 1000;

  let productoActual = null;
  let productosRelacionados = [];

  function leerProductosCache() {
    if (window.ShineProductCache?.read) return window.ShineProductCache.read();

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
    if (window.ShineProductCache?.write) {
      window.ShineProductCache.write(data);
      return;
    }

    try {
      localStorage.setItem(PRODUCT_CACHE_KEY, JSON.stringify({
        timestamp: Date.now(),
        data
      }));
    } catch (error) {
      // Cache is an optimization only.
    }
  }

  function actualizarProductoEnCache(productoActualizado) {
    const cached = leerProductosCache();
    if (!cached.length) return;

    const next = cached.map(producto =>
      Number(producto.idProducto) === Number(productoActualizado.idProducto)
        ? productoActualizado
        : producto
    );
    guardarProductosCache(next);
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function isPerfume(producto) {
    const catName = String(producto.categoria?.nombre || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    const prodName = String(producto.nombre || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    return catName.includes('frag') || catName.includes('perfume') || catName.includes('fragancia')
        || prodName.includes('perfume') || prodName.includes('fragancia');
  }

  function formatCurrency(value) {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(Number(value || 0));
  }

  function getProductIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const rawId = params.get('id') || params.get('idProducto');
    if (!rawId) return null;

    const numericId = Number(rawId);
    if (Number.isFinite(numericId)) return numericId;

    const match = rawId.match(/\d+/);
    return match ? Number(match[0]) : null;
  }

  function getImages(producto) {
    if (!Array.isArray(producto?.imagenes) || producto.imagenes.length === 0) {
      return [{ url: FALLBACK_IMAGE, descripcion: producto?.nombre || 'Producto Shine' }];
    }

    return producto.imagenes
      .filter(imagen => imagen?.url)
      .map(imagen => ({
        url: normalizeImageUrl(imagen.url),
        descripcion: imagen.descripcion || producto.nombre || 'Producto Shine'
      }));
  }

  function normalizeImageUrl(url) {
    const cleanUrl = String(url || '').trim();
    if (!cleanUrl) return FALLBACK_IMAGE;
    if (/^(https?:|data:|assets\/|\.\/|\.\.\/)/i.test(cleanUrl)) return cleanUrl;
    if (cleanUrl.startsWith('/')) return `https://api.shinebeauty.store${cleanUrl}`;
    return cleanUrl;
  }

  function getMainImage(producto) {
    return getImages(producto)[0]?.url || FALLBACK_IMAGE;
  }

  function getDescription(producto) {
    return (
      producto.descripcion ||
      producto.description ||
      producto.imagenes?.find(imagen => imagen?.descripcion)?.descripcion ||
      `${producto.nombre} es un producto Shine de la categoria ${producto.categoria?.nombre || 'catalogo premium'}, seleccionado para una rutina elegante y cuidada.`
    );
  }

  function getBadge(producto) {
    if (Number(producto.stock) <= 0) return 'Out of Stock';
    if (producto.categoria?.nombre) return producto.categoria.nombre;
    return 'Shine';
  }

  function setText(selector, value) {
    const element = document.querySelector(selector);
    if (element) element.textContent = value;
  }

  function setHtml(selector, value) {
    const element = document.querySelector(selector);
    if (element) element.innerHTML = value;
  }

  function renderGallery(producto) {
    const mainImage = document.getElementById('mainProductImage');
    const thumbs = document.querySelector('.gallery-thumbs');
    const images = getImages(producto);

    if (mainImage) {
      mainImage.src = images[0].url;
      mainImage.alt = `${producto.nombre} main view`;
      mainImage.onerror = function() { this.onerror = null; this.src = FALLBACK_IMAGE; };
    }

    if (!thumbs) return;

    thumbs.innerHTML = images.map((imagen, index) => `
      <button class="gallery-thumb${index === 0 ? ' active' : ''}" type="button" data-img="${escapeHtml(imagen.url)}">
        <img src="${escapeHtml(imagen.url)}" alt="${escapeHtml(imagen.descripcion)}" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'">
      </button>
    `).join('');
  }

  function renderAccordion(producto) {
    const sku = producto.sku || 'N/A';
    const stock = Number(producto.stock || 0);
    const categoria = producto.categoria?.nombre || 'Uncategorised';
    const ingredientes = producto.ingredientes || 'Ingredients to be completed for this product.';
    const modoUso = producto.modoUso || producto.modo_uso || 'Usage instructions to be completed for this product.';

    setHtml('#productAccordion', `
      <div class="accordion__item open">
        <button class="accordion__trigger" type="button">Description <svg viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></svg></button>
        <div class="accordion__content">
          <div class="accordion__inner">${escapeHtml(getDescription(producto))}</div>
        </div>
      </div>
      <div class="accordion__item">
        <button class="accordion__trigger" type="button">Ingredients <svg viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></svg></button>
        <div class="accordion__content">
          <div class="accordion__inner">${escapeHtml(ingredientes)}</div>
        </div>
      </div>
      <div class="accordion__item">
        <button class="accordion__trigger" type="button">How to Use <svg viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></svg></button>
        <div class="accordion__content">
          <div class="accordion__inner">${escapeHtml(modoUso)}</div>
        </div>
      </div>
      <div class="accordion__item">
        <button class="accordion__trigger" type="button">Details <svg viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></svg></button>
        <div class="accordion__content">
          <div class="accordion__inner">
            SKU: ${escapeHtml(sku)}<br>
            Category: ${escapeHtml(categoria)}<br>
            Available stock: ${escapeHtml(stock)}
          </div>
        </div>
      </div>
      <div class="accordion__item">
        <button class="accordion__trigger" type="button">Shipping &amp; Returns <svg viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></svg></button>
        <div class="accordion__content">
          <div class="accordion__inner">
            Free standard shipping on orders over €50. Express delivery available. Returns accepted within 30 days.
          </div>
        </div>
      </div>
    `);
  }

  function renderProduct(producto) {
    productoActual = producto;

    document.title = `Shine - ${producto.nombre}`;
    document.querySelector('meta[name="description"]')?.setAttribute('content', getDescription(producto));

    setText('#productBreadcrumbName', producto.nombre);
    setText('#productBadge', getBadge(producto));
    setText('#productName', producto.nombre);
    setText('#productPrice', formatCurrency(producto.precio));
    setText('#productDescription', getDescription(producto));

    const addButton = document.getElementById('addToBagBtn');
    if (addButton) {
      addButton.disabled = Number(producto.stock) <= 0;
      addButton.textContent = Number(producto.stock) <= 0 ? 'Out of Stock' : 'Add to Cart';
    }

    // For perfume products: also show "Create a Custom Fragrance" link
    const esPerfume = isPerfume(producto);
    const customLink = document.getElementById('customFragranceLink');
    if (customLink) customLink.style.display = esPerfume ? 'inline-flex' : 'none';

    renderGallery(producto);
    renderAccordion(producto);

    document.querySelectorAll('.product-gallery, .product-detail .reveal').forEach(el => el.classList.add('visible'));

    renderStaffPanel(producto);
  }

  function renderStaffPanel(producto) {
    const staffSession = (() => {
      try { return JSON.parse(localStorage.getItem('shineStaff')); } catch { return null; }
    })();
    if (!staffSession) return;

    const existing = document.getElementById('staffEditPanel');
    if (existing) existing.remove();

    const panel = document.createElement('div');
    panel.id = 'staffEditPanel';
    panel.style.cssText = 'margin-top:28px;padding:20px 24px;border:1px solid #e0d5ce;border-radius:12px;background:#fdf9f7';

    panel.innerHTML = `
      <div style="display:flex;align-items:center;gap:8px;margin-bottom:16px">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#c49a9a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        <span style="font-size:0.82rem;font-weight:600;color:#8a7b82;text-transform:uppercase;letter-spacing:.05em">Edición de personal</span>
      </div>
      <div style="display:grid;gap:14px">
        <div>
          <label style="font-size:0.82rem;font-weight:500;color:#4a3f3f;display:block;margin-bottom:6px">Descripción</label>
          <textarea id="staffEditDesc" rows="3" style="width:100%;padding:10px 12px;border:1px solid #ddd0c8;border-radius:8px;font-family:inherit;font-size:0.88rem;color:#2a2020;resize:vertical;box-sizing:border-box;outline:none">${escapeHtml(producto.descripcion || '')}</textarea>
        </div>
        <div>
          <label style="font-size:0.82rem;font-weight:500;color:#4a3f3f;display:block;margin-bottom:6px">Precio (€)</label>
          <input id="staffEditPrice" type="number" step="0.01" min="0" value="${Number(producto.precio || 0).toFixed(2)}" style="width:100%;padding:10px 12px;border:1px solid #ddd0c8;border-radius:8px;font-family:inherit;font-size:0.88rem;color:#2a2020;box-sizing:border-box;outline:none">
        </div>
        <div style="display:flex;align-items:center;gap:12px">
          <button id="staffSaveBtn" style="padding:9px 22px;background:#c49a9a;color:#fff;border:none;border-radius:8px;font-size:0.88rem;font-family:inherit;font-weight:500;cursor:pointer;transition:background .2s">Guardar cambios</button>
          <span id="staffSaveStatus" style="font-size:0.82rem"></span>
        </div>
      </div>
    `;

    const infoSection = document.querySelector('.product-detail > div:not(.product-gallery)');
    if (infoSection) infoSection.appendChild(panel);

    document.getElementById('staffSaveBtn')?.addEventListener('click', async () => {
      if (!productoActual) return;

      const newDesc = document.getElementById('staffEditDesc')?.value.trim() ?? '';
      const newPrice = parseFloat(document.getElementById('staffEditPrice')?.value || '0');
      const statusEl = document.getElementById('staffSaveStatus');
      const saveBtn = document.getElementById('staffSaveBtn');

      saveBtn.disabled = true;
      saveBtn.textContent = 'Guardando...';
      if (statusEl) statusEl.textContent = '';

      try {
        const payload = {
          nombre: productoActual.nombre,
          sku: productoActual.sku,
          descripcion: newDesc,
          ingredientes: productoActual.ingredientes || '',
          modoUso: productoActual.modoUso || '',
          precio: newPrice,
          stock: productoActual.stock || 0,
          genero: productoActual.genero || null,
          tipoFragancia: productoActual.tipoFragancia || null,
          idCategoria: productoActual.categoria?.idCategoria
        };

        const res = await fetch(`https://api.shinebeauty.store/api/v1/productos/${productoActual.idProducto}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        productoActual.descripcion = newDesc;
        productoActual.precio = newPrice;
        setText('#productDescription', newDesc);
        setText('#productPrice', formatCurrency(newPrice));
        actualizarProductoEnCache(productoActual);
        localStorage.removeItem('shine:productos:v2');

        saveBtn.textContent = 'Guardar cambios';
        saveBtn.disabled = false;
        if (statusEl) {
          statusEl.style.color = '#4a7a4a';
          statusEl.textContent = '✓ Guardado correctamente';
          setTimeout(() => { statusEl.textContent = ''; }, 3500);
        }
      } catch (err) {
        saveBtn.textContent = 'Guardar cambios';
        saveBtn.disabled = false;
        if (statusEl) {
          statusEl.style.color = '#c0392b';
          statusEl.textContent = '✗ Error al guardar. Verifica el servidor.';
        }
      }
    });
  }

  function productCard(producto) {
    const image = getMainImage(producto);

    return `
      <article class="product-card reveal visible" data-id="${escapeHtml(producto.idProducto)}">
        <div class="product-card__img-wrap">
          <img class="product-card__img" src="${escapeHtml(image)}" alt="${escapeHtml(producto.nombre)}" loading="lazy" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'">
          <button type="button" class="product-card__action" aria-label="Add to cart">
            <svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
          </button>
        </div>
        <div class="product-card__body">
          <div class="product-card__name">${escapeHtml(producto.nombre)}</div>
          <div class="product-card__price">${escapeHtml(formatCurrency(producto.precio))}</div>
        </div>
      </article>
    `;
  }

  function renderRelated(products) {
    const grid = document.getElementById('relatedProductsGrid');
    if (!grid) return;

    productosRelacionados = products.slice(0, 4);

    if (!productosRelacionados.length) {
      grid.innerHTML = '<p class="text-sm text-muted" style="grid-column:1/-1">No related products yet.</p>';
      return;
    }

    grid.innerHTML = productosRelacionados.map(productCard).join('');
    grid.classList.add('visible');
  }

  function renderError(message) {
    const detail = document.querySelector('.product-detail');
    if (!detail) return;

    detail.innerHTML = `
      <div class="cart-empty" style="grid-column:1/-1">
        <p>${escapeHtml(message)}</p>
        <a href="shop.html" class="btn btn--primary">Back to Catalogue</a>
      </div>
    `;
  }

  function filtrarRelacionados(products, productId, categoryName) {
    const related = Array.isArray(products)
      ? products.filter(producto => {
          const differentProduct = Number(producto.idProducto) !== Number(productId);
          const sameCategory = categoryName
            ? producto.categoria?.nombre === categoryName
            : true;
          return differentProduct && sameCategory;
        })
      : [];

    const fallback = Array.isArray(products)
      ? products.filter(producto => Number(producto.idProducto) !== Number(productId))
      : [];

    return related.length ? related : fallback;
  }

  async function loadRelatedProducts(productId, categoryName, shouldFetch = true) {
    const cachedProducts = leerProductosCache();
    if (cachedProducts.length) {
      renderRelated(filtrarRelacionados(cachedProducts, productId, categoryName));
      if (!shouldFetch) return;
    }

    try {
      const products = await window.ShineAPI.get('/productos');
      if (Array.isArray(products)) guardarProductosCache(products);
      renderRelated(filtrarRelacionados(products, productId, categoryName));
    } catch (error) {
      if (!cachedProducts.length) renderRelated([]);
    }
  }

  async function loadProduct() {
    const productId = getProductIdFromUrl();

    if (!productId) {
      renderError('No product ID provided.');
      return;
    }

    if (!window.ShineAPI) {
      renderError('Could not initialize the API connection.');
      return;
    }

    const cachedProducts = leerProductosCache();
    const cachedProduct = cachedProducts.find(producto => Number(producto.idProducto) === Number(productId));

    if (cachedProduct) {
      renderProduct(cachedProduct);
      renderRelated(filtrarRelacionados(cachedProducts, cachedProduct.idProducto, cachedProduct.categoria?.nombre));
    }

    try {
      const producto = await window.ShineAPI.get(`/productos/${productId}`);
      actualizarProductoEnCache(producto);
      renderProduct(producto);
      loadRelatedProducts(producto.idProducto, producto.categoria?.nombre, !cachedProducts.length);
    } catch (error) {
      if (!cachedProduct) {
        renderError('Could not load this product. Please check it exists in the backend.');
      }
    }
  }

  function setupGallery() {
    document.querySelector('.gallery-thumbs')?.addEventListener('click', event => {
      const thumb = event.target.closest('.gallery-thumb');
      if (!thumb) return;

      const mainImage = document.getElementById('mainProductImage');
      const image = thumb.dataset.img || thumb.querySelector('img')?.src;
      if (mainImage && image) mainImage.src = image;

      document.querySelectorAll('.gallery-thumb').forEach(item => item.classList.remove('active'));
      thumb.classList.add('active');
    });
  }

  function setupAddToCart() {
    document.getElementById('addToBagBtn')?.addEventListener('click', async () => {
      if (!productoActual) return;

      const quantity = Number(document.querySelector('.product-detail .qty-selector span')?.textContent || 1);

      if (typeof window.añadirAlCarrito === 'function') {
        await window.añadirAlCarrito(productoActual.idProducto, quantity);
        return;
      }

      if (typeof window.anadirAlCarrito === 'function') {
        await window.anadirAlCarrito(productoActual.idProducto, quantity);
      }
    });
  }

  function setupRelatedProducts() {
    document.getElementById('relatedProductsGrid')?.addEventListener('click', async event => {
      const card = event.target.closest('.product-card[data-id]');
      if (!card) return;

      const button = event.target.closest('.product-card__action');
      if (button) {
        event.preventDefault();
        event.stopPropagation();

        if (typeof window.añadirAlCarrito === 'function') {
          await window.añadirAlCarrito(card.dataset.id, 1);
        }
        return;
      }

      window.location.href = `product.html?id=${card.dataset.id}`;
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('.product-detail')) return;

    setupGallery();
    setupAddToCart();
    setupRelatedProducts();
    loadProduct();
  });
})();
