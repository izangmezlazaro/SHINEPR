// ============================================
// Shine — Premium Interactive Experience v2.0
// ============================================

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ---------- Cart Utility ----------
const ShineCart = {
  KEY: 'shineCart',
  get()  { try { return JSON.parse(localStorage.getItem(this.KEY)) || []; } catch(e) { return []; } },
  save(items) { localStorage.setItem(this.KEY, JSON.stringify(items)); },
  add(product) {
    const items = this.get();
    const existing = items.find(i => i.id === product.id);
    if (existing) existing.qty++;
    else items.push({ ...product, qty: 1 });
    this.save(items);
    this._updateBadge();
  },
  remove(id) { this.save(this.get().filter(i => i.id !== id)); this._updateBadge(); },
  updateQty(id, qty) {
    const items = this.get();
    const item = items.find(i => i.id === id);
    if (item) item.qty = Math.max(1, Math.min(99, qty));
    this.save(items);
    this._updateBadge();
  },
  count() { return this.get().reduce((s, i) => s + i.qty, 0); },
  _updateBadge() {
    const count = this.count();
    document.querySelectorAll('.nav-cta').forEach(btn => {
      btn.querySelector('.cart-count')?.remove();
      if (count > 0) {
        const el = document.createElement('span');
        el.className = 'cart-count';
        el.textContent = count;
        btn.appendChild(el);
      }
    });
  }
};

function showToast(msg) {
  document.querySelector('.shine-toast')?.remove();
  const toast = document.createElement('div');
  toast.className = 'shine-toast';
  toast.textContent = msg;
  document.body.appendChild(toast);
  requestAnimationFrame(() => requestAnimationFrame(() => toast.classList.add('show')));
  setTimeout(() => { toast.classList.remove('show'); setTimeout(() => toast.remove(), 400); }, 2500);
}

document.addEventListener('DOMContentLoaded', () => {

  // Init cart badge on every page
  ShineCart._updateBadge();

  // ---------- Preloader ----------
  const preloader = document.getElementById('preloader');
  if (preloader) {
    window.addEventListener('load', () => {
      setTimeout(() => {
        preloader.classList.add('done');
        setTimeout(() => preloader.remove(), 600);
      }, 600);
    });
    // Fallback: remove after 2s max
    setTimeout(() => {
      if (preloader && !preloader.classList.contains('done')) {
        preloader.classList.add('done');
        setTimeout(() => preloader.remove(), 600);
      }
    }, 2000);
  }

  // ---------- Scroll Progress Bar ----------
  const scrollProgress = document.getElementById('scrollProgress');
  if (scrollProgress) {
    window.addEventListener('scroll', () => {
      const scrollTop = window.scrollY;
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      const percent = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;
      scrollProgress.style.width = percent + '%';
    }, { passive: true });
  }

  // ---------- Header scroll effect ----------
  const header = document.getElementById('siteHeader');
  if (header) {
    const onScroll = () => {
      header.classList.toggle('scrolled', window.scrollY > 30);
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
  }

  // ---------- Mobile hamburger menu ----------
  const hamburger = document.getElementById('hamburgerBtn');
  const mobileNav = document.getElementById('mobileNav');
  if (hamburger && mobileNav) {
    hamburger.addEventListener('click', () => {
      hamburger.classList.toggle('open');
      mobileNav.classList.toggle('open');
      document.body.style.overflow = mobileNav.classList.contains('open') ? 'hidden' : '';
    });
    mobileNav.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        hamburger.classList.remove('open');
        mobileNav.classList.remove('open');
        document.body.style.overflow = '';
      });
    });
  }

  // ---------- Hero Text Reveal Animation ----------
  const lineInners = document.querySelectorAll('.line-inner');
  const heroDesc = document.querySelector('.hero-ultra__desc');
  const heroActions = document.querySelector('.hero-ultra__actions');
  const heroImgFrame = document.querySelector('.hero-ultra__img-frame');

  setTimeout(() => {
    lineInners.forEach((line, i) => {
      setTimeout(() => line.classList.add('visible'), i * 200);
    });
    if (heroDesc) setTimeout(() => heroDesc.classList.add('visible'), 500);
    if (heroActions) setTimeout(() => heroActions.classList.add('visible'), 700);
    if (heroImgFrame) setTimeout(() => heroImgFrame.classList.add('visible'), 400);
  }, preloader ? 900 : 300);

  // ---------- Scroll Reveal (IntersectionObserver) ----------
  const revealEls = document.querySelectorAll('.reveal, .reveal-up, .reveal-scale');
  if (revealEls.length) {
    const revealObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          revealObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });
    revealEls.forEach(el => revealObserver.observe(el));
  }

  // ---------- Animated Counters ----------
  const counters = document.querySelectorAll('[data-count]');
  if (counters.length) {
    const counterObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const el = entry.target;
          const target = parseInt(el.getAttribute('data-count'));
          const duration = 2000;
          const start = performance.now();
          const suffix = target === 100 ? '%' : target >= 1000 ? '+' : '+';

          function updateCounter(now) {
            const elapsed = now - start;
            const progress = Math.min(elapsed / duration, 1);
            // Ease out cubic
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(eased * target);
            
            if (target >= 1000) {
              el.textContent = current.toLocaleString() + suffix;
            } else {
              el.textContent = current + suffix;
            }

            if (progress < 1) {
              requestAnimationFrame(updateCounter);
            }
          }
          requestAnimationFrame(updateCounter);
          counterObserver.unobserve(el);
        }
      });
    }, { threshold: 0.5 });

    counters.forEach(c => counterObserver.observe(c));
  }

  // ---------- Horizontal Drag Scroll ----------
  const hscroll = document.getElementById('hscrollTrack');
  if (hscroll) {
    let isDown = false;
    let startX, scrollLeft;

    hscroll.addEventListener('mousedown', (e) => {
      isDown = true;
      hscroll.style.cursor = 'grabbing';
      startX = e.pageX - hscroll.offsetLeft;
      scrollLeft = hscroll.scrollLeft;
    });
    hscroll.addEventListener('mouseleave', () => {
      isDown = false;
      hscroll.style.cursor = 'grab';
    });
    hscroll.addEventListener('mouseup', () => {
      isDown = false;
      hscroll.style.cursor = 'grab';
    });
    hscroll.addEventListener('mousemove', (e) => {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - hscroll.offsetLeft;
      const walk = (x - startX) * 2;
      hscroll.scrollLeft = scrollLeft - walk;
    });

    // Carousel navigation buttons
    const prevBtn = document.getElementById('hscrollPrev');
    const nextBtn = document.getElementById('hscrollNext');

    function getCarouselStep() {
      const card = hscroll.querySelector('.product-card');
      return card ? card.offsetWidth + 24 : 260; // card width + --sp-lg (24px)
    }

    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        hscroll.scrollBy({ left: -getCarouselStep(), behavior: 'smooth' });
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        hscroll.scrollBy({ left: getCarouselStep(), behavior: 'smooth' });
      });
    }
  }

  // ---------- Magnetic Button Effect ----------
  document.querySelectorAll('.btn-magnetic').forEach(btn => {
    btn.addEventListener('mousemove', function(e) {
      const rect = this.getBoundingClientRect();
      const x = e.clientX - rect.left - rect.width / 2;
      const y = e.clientY - rect.top - rect.height / 2;
      this.style.transform = `translate(${x * 0.15}px, ${y * 0.15}px) translateY(-3px)`;
    });
    btn.addEventListener('mouseleave', function() {
      this.style.transform = '';
    });
  });

  // ---------- Parallax on Hero Orbs ----------
  const orbs = document.querySelectorAll('.hero-ultra__orb');
  if (orbs.length && window.matchMedia('(prefers-reduced-motion: no-preference)').matches) {
    window.addEventListener('scroll', () => {
      const scrollY = window.scrollY;
      orbs.forEach((orb, i) => {
        const speed = (i + 1) * 0.03;
        orb.style.transform = `translateY(${scrollY * speed}px)`;
      });
    }, { passive: true });
  }

  // ---------- Niche Card Tilt Effect ----------
  document.querySelectorAll('.niche-card-ultra').forEach(card => {
    card.addEventListener('mousemove', function(e) {
      const rect = this.getBoundingClientRect();
      const x = (e.clientX - rect.left) / rect.width - 0.5;
      const y = (e.clientY - rect.top) / rect.height - 0.5;
      this.style.transform = `translateY(-10px) perspective(600px) rotateX(${y * -5}deg) rotateY(${x * 5}deg)`;
    });
    card.addEventListener('mouseleave', function() {
      this.style.transform = '';
    });
  });

  // ---------- Product Card Hover Scale ----------
  document.querySelectorAll('.product-card, .bento-grid__item').forEach(card => {
    card.addEventListener('mouseenter', function() {
      this.style.transition = 'transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1)';
    });
  });

  // ---------- Accordion toggle ----------
  document.querySelectorAll('.accordion__trigger').forEach(trigger => {
    trigger.addEventListener('click', () => {
      const item = trigger.closest('.accordion__item');
      const isOpen = item.classList.contains('open');
      item.parentElement.querySelectorAll('.accordion__item.open').forEach(openItem => {
        openItem.classList.remove('open');
      });
      if (!isOpen) item.classList.add('open');
    });
  });

  // ---------- Quantity selector ----------
  document.querySelectorAll('.qty-selector').forEach(selector => {
    const minus = selector.querySelector('[data-qty="minus"]');
    const plus = selector.querySelector('[data-qty="plus"]');
    const display = selector.querySelector('span');
    if (minus && plus && display) {
      minus.addEventListener('click', () => {
        let val = parseInt(display.textContent);
        if (val > 1) display.textContent = val - 1;
      });
      plus.addEventListener('click', () => {
        let val = parseInt(display.textContent);
        if (val < 99) display.textContent = val + 1;
      });
    }
  });

  // ---------- Gallery thumbnails ----------
  document.querySelectorAll('.gallery-thumb').forEach(thumb => {
    thumb.addEventListener('click', () => {
      const gallery = thumb.closest('.product-gallery');
      const mainImg = gallery.querySelector('.gallery-main img');
      gallery.querySelectorAll('.gallery-thumb').forEach(t => t.classList.remove('active'));
      thumb.classList.add('active');
      mainImg.src = thumb.querySelector('img').src;
    });
  });

  // ---------- Filter items (shop page) ----------
  document.querySelectorAll('.filter-item').forEach(item => {
    item.addEventListener('click', () => {
      const group = item.closest('.filter-group__list');
      group.querySelectorAll('.filter-item').forEach(i => i.classList.remove('active'));
      item.classList.add('active');
    });
  });

  // ---------- Keyboard accessibility ----------
  document.body.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') document.documentElement.classList.add('show-focus');
  });
  document.body.addEventListener('mousedown', () => {
    document.documentElement.classList.remove('show-focus');
  });

  // ---------- Brand Reveal — Scroll-Driven Parallax ----------
  const brandReveal = document.getElementById('brandReveal');
  const brandText   = document.getElementById('brandRevealText');
  const brandSub    = document.getElementById('brandRevealSub');

  if (brandReveal && brandText) {
    const smoothstep = (t) => t * t * (3 - 2 * t);

    const onBrandScroll = () => {
      const rect       = brandReveal.getBoundingClientRect();
      const scrollable = brandReveal.offsetHeight - window.innerHeight;
      const scrolled   = -rect.top;
      const raw        = scrolled / scrollable;
      const progress   = Math.min(Math.max(raw, 0), 1);

      // Background image moves from 20% → 4% as you scroll (upper, bright bottle area)
      const bgY = 20 - smoothstep(progress) * 16;
      brandText.style.backgroundPositionY = bgY + '%';

      // Fade: starts at 0.18, reaches 1 by 8%, full 8–88%, out last 12%
      let alpha;
      const MIN_ALPHA = 0.18;
      if (progress < 0.08)      alpha = MIN_ALPHA + (1 - MIN_ALPHA) * smoothstep(progress / 0.08);
      else if (progress > 0.88) alpha = 1 - smoothstep((progress - 0.88) / 0.12);
      else                      alpha = 1;
      brandText.style.opacity = alpha;

      // Sub-tagline: fade + slide in at 6%, hold, fade out at 88%
      if (brandSub) {
        let subAlpha, subY;
        if (progress < 0.06) {
          subAlpha = 0; subY = 14;
        } else if (progress < 0.16) {
          const t = (progress - 0.06) / 0.10;
          subAlpha = smoothstep(t); subY = 14 * (1 - smoothstep(t));
        } else if (progress > 0.88) {
          const t = (progress - 0.88) / 0.12;
          subAlpha = 1 - smoothstep(Math.min(t, 1)); subY = 0;
        } else {
          subAlpha = 1; subY = 0;
        }
        brandSub.style.opacity   = subAlpha;
        brandSub.style.transform = `translateY(${subY}px)`;
      }
    };

    window.addEventListener('scroll', onBrandScroll, { passive: true });
    onBrandScroll(); // run once on load
  }

  // ---------- User Avatar / Account Dropdown ----------
  const nav = document.querySelector('.nav');
  if (nav) {
    let user = null;
    try { user = JSON.parse(localStorage.getItem('shineUser')); } catch(e) {}

    if (user) {
      const loginLink = nav.querySelector('a[href="login.html"]');
      if (loginLink) loginLink.style.display = 'none';

      // Check if user has staff access
      let staffUser = null;
      try { staffUser = JSON.parse(localStorage.getItem('shineStaff')); } catch(e) {}
      const hasStaffAccess = staffUser && (staffUser.role === 'admin' || staffUser.role === 'empleado');

      const intranetLink = hasStaffAccess ? `
          <a href="intranet.html" class="nav-avatar__menu-item" style="color:#D97706;font-weight:600">
            <svg viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>
            Intranet ${staffUser.role === 'admin' ? '(Admin)' : '(Empleado)'}
          </a>
          <div class="nav-avatar__divider"></div>` : '';

      const avatarEl = document.createElement('div');
      avatarEl.className = 'nav-avatar';
      avatarEl.innerHTML = `
        <button class="nav-avatar__btn" id="avatarBtn" aria-label="Account menu" aria-expanded="false">${escapeHtml(user.initials)}</button>
        <div class="nav-avatar__dropdown" id="avatarDropdown">
          ${intranetLink}
          <a href="account.html" class="nav-avatar__menu-item">
            <svg viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            My Account
          </a>
          <a href="profile.html" class="nav-avatar__menu-item">
            <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            My Profile
          </a>
          <a href="addresses.html" class="nav-avatar__menu-item">
            <svg viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            My Addresses
          </a>
          <a href="orders.html" class="nav-avatar__menu-item">
            <svg viewBox="0 0 24 24"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.99-1.61L23 6H6"/></svg>
            My Orders
          </a>
          <div class="nav-avatar__divider"></div>
          <button class="nav-avatar__signout" id="signOutBtn">Sign Out</button>
        </div>
      `;

      if (loginLink) {
        loginLink.parentNode.insertBefore(avatarEl, loginLink.nextSibling);
      } else {
        nav.appendChild(avatarEl);
      }

      const avatarBtn = document.getElementById('avatarBtn');
      const dropdown = document.getElementById('avatarDropdown');

      avatarBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = dropdown.classList.contains('open');
        dropdown.classList.toggle('open');
        avatarBtn.setAttribute('aria-expanded', String(!isOpen));
      });

      document.addEventListener('click', () => {
        dropdown.classList.remove('open');
        if (avatarBtn) avatarBtn.setAttribute('aria-expanded', 'false');
      });

      dropdown.addEventListener('click', (e) => e.stopPropagation());

      document.getElementById('signOutBtn').addEventListener('click', async () => {
        if (window.ShineAPI) {
          try { await window.ShineAPI.post('/auth/logout', {}); } catch(e) {}
        }
        localStorage.removeItem('shineUser');
        localStorage.removeItem('shineUserId');
        localStorage.removeItem('shineStaff');
        window.location.href = 'index.html';
      });
    }
  }

  // ---------- Cookie Consent Banner ----------
  (function () {
    if (localStorage.getItem('shineCookiesAccepted')) return;

    const banner = document.createElement('div');
    banner.className = 'cookie-banner';
    banner.setAttribute('role', 'dialog');
    banner.setAttribute('aria-label', 'Cookie consent');
    banner.innerHTML = `
      <div class="cookie-banner__text">
        We use cookies to save your cart and improve your experience. By continuing you accept our
        <a href="privacy.html">Cookie Policy</a>.
      </div>
      <div class="cookie-banner__actions">
        <button class="btn btn--sm btn--outline" id="cookieDeclineBtn">Necessary only</button>
        <button class="btn btn--sm btn--primary" id="cookieAcceptBtn">Accept all</button>
      </div>
    `;
    document.body.appendChild(banner);
    setTimeout(() => banner.classList.add('show'), 300);

    document.getElementById('cookieAcceptBtn').addEventListener('click', () => {
      localStorage.setItem('shineCookiesAccepted', 'all');
      banner.classList.remove('show');
      setTimeout(() => banner.remove(), 420);
    });

    document.getElementById('cookieDeclineBtn').addEventListener('click', () => {
      localStorage.setItem('shineCookiesAccepted', 'necessary');
      banner.classList.remove('show');
      setTimeout(() => banner.remove(), 420);
    });
  })();

  // ---------- Shop: card click → product page ----------
  // Add-to-cart is handled by carrito.js (capture phase). This only handles navigation.
  const productGrid = document.getElementById('productGrid');
  if (productGrid) {
    productGrid.addEventListener('click', e => {
      if (e.target.closest('.product-card__action')) return; // handled by carrito.js
      const card = e.target.closest('[data-id]');
      if (card) window.location.href = `product.html?id=${card.dataset.id}`;
    });
  }

  // ---------- Cart Page (legacy — skipped when carrito.js is present) ----------
  const cartItemsList = document.getElementById('cartItemsList');
  if (cartItemsList && !window.cargarCarrito) {
    function renderCart() {
      const items = ShineCart.get();

      if (items.length === 0) {
        cartItemsList.innerHTML = `
          <div class="cart-empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--text-muted)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="display:block;margin:0 auto var(--sp-md)">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.99-1.61L23 6H6"/>
            </svg>
            <p>Your cart is empty.</p>
            <a href="shop.html" class="btn btn--primary">Continue Shopping</a>
          </div>`;
        updateSummary([]);
        return;
      }

      cartItemsList.innerHTML = items.map(item => `
        <div class="cart-item" data-cart-item="${escapeHtml(item.id)}">
          <img class="cart-item__img" src="${escapeHtml(item.img)}" alt="${escapeHtml(item.name)}">
          <div class="cart-item__info">
            <div class="cart-item__name">${escapeHtml(item.name)}</div>
            <div class="cart-item__price">€${item.price.toFixed(2)}</div>
          </div>
          <div class="qty-selector">
            <button data-qty="minus" aria-label="Decrease">−</button>
            <span>${item.qty}</span>
            <button data-qty="plus" aria-label="Increase">+</button>
          </div>
          <button class="cart-item__remove" aria-label="Remove item">✕</button>
        </div>
      `).join('') + `<a href="shop.html" class="btn btn--outline mt-xl">Continue Shopping</a>`;

      updateSummary(items);
    }

    function updateSummary(items) {
      const subtotal = items.reduce((s, i) => s + i.price * i.qty, 0);
      const tax      = subtotal * 0.10;
      const total    = subtotal + tax;
      const subEl = document.getElementById('summarySubtotal');
      const taxEl = document.getElementById('summaryTax');
      const totEl = document.getElementById('summaryTotal');
      if (subEl) subEl.textContent = `€${subtotal.toFixed(2)}`;
      if (taxEl) taxEl.textContent = `€${tax.toFixed(2)}`;
      if (totEl) totEl.textContent = `€${total.toFixed(2)}`;
    }

    cartItemsList.addEventListener('click', e => {
      const item = e.target.closest('[data-cart-item]');
      if (!item) return;
      const id = item.dataset.cartItem;

      if (e.target.closest('.cart-item__remove')) {
        ShineCart.remove(id);
        renderCart();
      } else if (e.target.dataset.qty === 'minus') {
        const qtyEl  = item.querySelector('.qty-selector span');
        const newQty = parseInt(qtyEl.textContent) - 1;
        if (newQty < 1) ShineCart.remove(id);
        else ShineCart.updateQty(id, newQty);
        renderCart();
      } else if (e.target.dataset.qty === 'plus') {
        const qtyEl  = item.querySelector('.qty-selector span');
        ShineCart.updateQty(id, parseInt(qtyEl.textContent) + 1);
        renderCart();
      }
    });

    renderCart();
  }

});
