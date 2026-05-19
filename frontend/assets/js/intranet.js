/* ══════════════════════════════════════════
   SHINE INTRANET v3 — JavaScript Engine
══════════════════════════════════════════ */

const INTRANET_API = 'https://api.shinebeauty.store/api/v1';

// Cache de pedidos cargados desde API, accesible por todos los módulos
const _apiOrderCache = {};

function staffSession() {
  try { return JSON.parse(localStorage.getItem('shineStaff')) || {}; } catch { return {}; }
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

document.addEventListener('DOMContentLoaded', () => {

// ── Data ──────────────────────────────────
const orderData = {
  'SH-1047': { customer: 'Laura Martínez', email: 'laura@email.com', date: '04 May 2026', items: [{ name: 'Radiant Glow Serum', qty: 1, price: 89 }, { name: 'Velvet Hydra Cream', qty: 1, price: 65 }], status: 'Processing', address: 'Calle Alcalá 12, Madrid' },
  'SH-1046': { customer: 'James Kim', email: 'james@email.com', date: '03 May 2026', items: [{ name: 'Radiant Glow Serum', qty: 1, price: 89 }], status: 'Shipped', address: 'Av. Diagonal 200, Barcelona' },
  'SH-1045': { customer: 'Sofia Petrov', email: 'sofia@email.com', date: '02 May 2026', items: [{ name: 'Signature Gift Box', qty: 1, price: 145 }, { name: 'Lip Balm', qty: 2, price: 43.5 }], status: 'Delivered', address: 'C/ Sierpes 5, Sevilla' },
  'SH-1044': { customer: 'Marc Torres', email: 'marc@email.com', date: '01 May 2026', items: [{ name: 'Velvet Hydra Cream', qty: 1, price: 65 }], status: 'Shipped', address: 'Pl. Catalunya 1, Barcelona' },
  'SH-1043': { customer: 'Anna Müller', email: 'anna@email.com', date: '30 Apr 2026', items: [{ name: 'Noir Essence EDP', qty: 1, price: 120 }], status: 'Delivered', address: 'Gran Vía 28, Madrid' }
};

// ── Tab Navigation ─────────────────────────
const links = document.querySelectorAll('.sidebar-link[data-tab]');
const sections = document.querySelectorAll('.dash-section');
const titleEl = document.getElementById('pageTitle');
const tabTitles = {
  home:'Home', dashboard:'Dashboard', orders:'Orders', catalogue:'Catalogue',
  agreements:'Agreement & Laws', 'documents-info':'Documents & Info',
  'objectives-2026':'Objectives 2026', comite:'Employee Committee',
  calendario:'Calendario',
  'bright-box':'Bright Box',
  team:'Team', reports:'Reports', settings:'Settings', planner:'Task Planner',
  'admin-roles':'Gestión de Roles', 'admin-fichajes':'Registro de Fichajes',
  'admin-creacion':'Creación de Productos',
  'admin-bright-box':'Bright Box — Admin'
};

const ADMIN_ONLY_TABS = ['dashboard', 'orders', 'team', 'reports', 'planner', 'admin-roles', 'admin-fichajes', 'admin-creacion', 'admin-bright-box'];

function goToTab(tab) {
  // Guard: redirigir si no hay sesión
  if (!window._staffRole) { window.location.href = 'staff-login.html'; return; }

  // Guard: empleados no pueden acceder a tabs restringidos
  if (window._staffRole !== 'admin' && ADMIN_ONLY_TABS.includes(tab)) {
    if (typeof showToast === 'function') showToast('Acceso restringido. Solo administradores.', 'warn');
    return;
  }

  links.forEach(l => l.classList.toggle('active', l.dataset.tab === tab));
  sections.forEach(s => s.classList.toggle('active', s.id === 'sec-' + tab));
  titleEl.textContent = tabTitles[tab] || tab;
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebarOverlay').classList.remove('active');
  if (tab === 'reports') initReportsCharts();
  if (tab === 'objectives-2026') animateProgressBars();
  if (tab === 'admin-fichajes' && typeof window.loadFichajesAdmin === 'function') window.loadFichajesAdmin();
  if (tab === 'catalogue' && typeof window.loadCatalogueProducts === 'function') window.loadCatalogueProducts();
  if (tab === 'orders' && typeof window.loadIntranetOrders === 'function') window.loadIntranetOrders();
  if (tab === 'admin-bright-box' && typeof window.loadBrightBoxAdmin === 'function') window.loadBrightBoxAdmin();
  if (tab === 'team' && typeof window.loadTeamMembers === 'function') window.loadTeamMembers();
  if (tab === 'admin-roles' && typeof window.loadRolesTable === 'function') window.loadRolesTable();
}

links.forEach(l => l.addEventListener('click', () => goToTab(l.dataset.tab)));
document.querySelectorAll('[data-goto]').forEach(el => el.addEventListener('click', () => goToTab(el.dataset.goto)));

// ── Sidebar Collapse ──────────────────────
const sidebar = document.getElementById('sidebar');
const dashMain = document.getElementById('dashMain');
const overlay = document.getElementById('sidebarOverlay');

document.getElementById('sidebarCollapseBtn').addEventListener('click', () => {
  sidebar.classList.toggle('collapsed');
  dashMain.classList.toggle('sidebar-collapsed');
});

document.getElementById('menuToggle').addEventListener('click', () => {
  sidebar.classList.toggle('open');
  overlay.classList.toggle('active');
});
overlay.addEventListener('click', () => {
  sidebar.classList.remove('open');
  overlay.classList.remove('active');
});

// ── Announcement Banner ──────────────────
document.getElementById('announceClose').addEventListener('click', () => {
  document.getElementById('announceBanner').style.display = 'none';
});

// ── Dark Mode ─────────────────────────────
const html = document.documentElement;
const themeIcon = document.getElementById('themeIcon');
const darkModeSetting = document.getElementById('darkModeToggleSetting');

function setTheme(dark) {
  html.setAttribute('data-theme', dark ? 'dark' : 'light');
  themeIcon.innerHTML = dark
    ? '<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>'    : '<path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>';
  if (darkModeSetting) darkModeSetting.checked = dark;
  localStorage.setItem('shine-theme', dark ? 'dark' : 'light');
}

// Load saved theme
const savedTheme = localStorage.getItem('shine-theme');
if (savedTheme === 'dark') setTheme(true);

document.getElementById('themeToggle').addEventListener('click', () => {
  setTheme(html.getAttribute('data-theme') !== 'dark');
});

if (darkModeSetting) {
  darkModeSetting.addEventListener('change', () => setTheme(darkModeSetting.checked));
}

// ── Clock ─────────────────────────────────
const clockEl = document.getElementById('topbarClock');
function updateClock() {
  const now = new Date();
  clockEl.textContent = now.toLocaleTimeString('en-GB', { hour:'2-digit', minute:'2-digit', second:'2-digit' }) +
    ' · ' + now.toLocaleDateString('en-GB', { weekday:'short', day:'numeric', month:'short' });
}
updateClock(); setInterval(updateClock, 1000);

// ── Punch In / Punch Out ───────────────────
const punchBtn       = document.getElementById('punchBtn');
const punchStatus    = document.getElementById('punchStatus');
const punchIndicator = document.getElementById('punchIndicator');
const homePunchPanel = document.getElementById('homePunchPanel');
const punchStateKey  = 'shine-punch-state';

function getPunchState() {
  try { return JSON.parse(localStorage.getItem(punchStateKey)) || { punched: false }; }
  catch { return { punched: false }; }
}
function savePunchState(state) { localStorage.setItem(punchStateKey, JSON.stringify(state)); }

function updatePunchUI(state) {
  if (!punchStatus || !punchBtn || !punchIndicator || !homePunchPanel) return;
  punchStatus.textContent = state.punched ? 'Estado: Fichado' : 'Estado: No fichado';
  punchBtn.textContent = state.punched ? 'Desfichar' : 'Fichar';
  punchBtn.classList.toggle('dash-btn--outline', state.punched);
  punchBtn.classList.toggle('dash-btn--rose', !state.punched);
  homePunchPanel.classList.toggle('home-punched', state.punched);
}

if (punchBtn) {
  let punchState = getPunchState();
  updatePunchUI(punchState);

  punchBtn.addEventListener('click', async () => {
    const nextPunched = !punchState.punched;
    const tipo = nextPunched ? 'ENTRADA' : 'SALIDA';
    const staff = staffSession();

    punchBtn.disabled = true;
    punchBtn.textContent = nextPunched ? 'Fichando…' : 'Desfichando…';

    try {
      const r = await fetch(`${INTRANET_API}/fichajes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          empleadoEmail:  staff.email  || 'desconocido@shine-beauty.com',
          empleadoNombre: staff.name   || 'Empleado',
          tipo
        })
      });
      if (!r.ok) {
        const data = await r.json().catch(() => ({}));
        showToast(data.error || `Error del servidor (${r.status})`, 'error');
        punchBtn.disabled = false;
        updatePunchUI(punchState);
        return;
      }
      punchState.punched = nextPunched;
      savePunchState(punchState);
      showToast(nextPunched ? 'Fichado correctamente.' : 'Desfichado correctamente.', 'success');
      if (typeof window.loadFichajesAdmin === 'function') window.loadFichajesAdmin();
    } catch (e) {
      console.error('No se pudo conectar con el servidor para fichar:', e);
      showToast('No se pudo conectar con el servidor.', 'error');
    } finally {
      punchBtn.disabled = false;
      updatePunchUI(punchState);
    }
  });
}

// ── Notifications ─────────────────────────
const notifBell = document.getElementById('notifBell');
const notifDropdown = document.getElementById('notifDropdown');
notifBell.addEventListener('click', e => { e.stopPropagation(); notifDropdown.classList.toggle('open'); });
document.addEventListener('click', () => notifDropdown.classList.remove('open'));
notifDropdown.addEventListener('click', e => e.stopPropagation());
document.getElementById('markAllRead').addEventListener('click', () => {
  document.querySelectorAll('.notif-item--unread').forEach(n => n.classList.remove('notif-item--unread'));
  const c = document.getElementById('notifCount');
  c.textContent = '0'; c.style.display = 'none';
  showToast('All notifications marked as read', 'success');
});

// ── Global Search ─────────────────────────
const searchData = [
  { type:'Order', label:'#SH-1047 — Laura Martínez', tab:'orders' },
  { type:'Order', label:'#SH-1046 — James Kim', tab:'orders' },
  { type:'Order', label:'#SH-1045 — Sofia Petrov', tab:'orders' },
  { type:'Product', label:'Radiant Glow Serum', tab:'catalogue' },
  { type:'Product', label:'Noir Essence EDP', tab:'catalogue' },
  { type:'Product', label:'Rose Mist Toner', tab:'catalogue' },
  { type:'Product', label:'Velvet Hydra Cream', tab:'catalogue' },
  { type:'Team', label:'Elena Ruiz — Creative Director', tab:'team' },
  { type:'Team', label:'Marco Torres — R&D', tab:'team' },
  { type:'Team', label:'Sofia Chen — Operations', tab:'team' },
  { type:'Report', label:'Monthly Revenue', tab:'reports' },
  { type:'Report', label:'Sales by Category', tab:'reports' },
  { type:'Agreement', label:'Sephora España', tab:'agreements' },
  { type:'Agreement', label:'El Corte Inglés', tab:'agreements' },
  { type:'Agreement', label:'Douglas EU (Draft)', tab:'agreements' },
];

const searchInput = document.getElementById('globalSearch');
const searchResults = document.getElementById('searchResults');

searchInput.addEventListener('input', () => {
  const q = searchInput.value.trim().toLowerCase();
  if (!q) { searchResults.classList.remove('open'); return; }
  const hits = searchData.filter(d => d.label.toLowerCase().includes(q) || d.type.toLowerCase().includes(q)).slice(0, 6);
  if (!hits.length) { searchResults.classList.remove('open'); return; }
  searchResults.innerHTML = hits.map(h => `
    <div class="search-result-item" data-tab="${h.tab}">
      <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <span>${h.label}</span>
      <span class="search-result-cat">${h.type}</span>
    </div>
  `).join('');
  searchResults.querySelectorAll('.search-result-item').forEach(item => {
    item.addEventListener('click', () => {
      goToTab(item.dataset.tab);
      searchInput.value = '';
      searchResults.classList.remove('open');
    });
  });
  searchResults.classList.add('open');
});
document.addEventListener('click', e => {
  if (!e.target.closest('#searchWrap')) searchResults.classList.remove('open');
});

// ── Agreement Sub-tabs ────────────────────
document.querySelectorAll('.dash-tabs[data-tabgroup="agreements"] .dash-tab[data-subtab]').forEach(tab => {
  tab.addEventListener('click', () => {
    const parent = tab.closest('.dash-card');
    parent.querySelectorAll('.dash-tabs[data-tabgroup="agreements"] .dash-tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    parent.querySelectorAll('.agreement-panel').forEach(p => p.classList.toggle('active', p.id === 'agreement-' + tab.dataset.subtab));
  });
});

// ── Settings Tabs ─────────────────────────
document.querySelectorAll('.dash-tab[data-settings-tab]').forEach(tab => {
  tab.addEventListener('click', () => {
    tab.closest('.dash-tabs').querySelectorAll('.dash-tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    ['general','branding','notifications','security'].forEach(k => {
      const el = document.getElementById('settings-' + k);
      if (el) el.style.display = k === tab.dataset.settingsTab ? '' : 'none';
    });
  });
});

document.getElementById('saveSettings')?.addEventListener('click', () => {
  showToast('Settings saved successfully!', 'success');
});

// ── Revenue Chart ─────────────────────────
const revCtx = document.getElementById('revenueChart').getContext('2d');
const revGrad = revCtx.createLinearGradient(0, 0, 0, 200);
revGrad.addColorStop(0, 'rgba(212,145,154,0.28)');
revGrad.addColorStop(1, 'rgba(212,145,154,0)');

const revData = {
  week:  { labels: ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'], data: [1820,2340,1950,2800,2100,3200,2630] },
  month: { labels: ['Wk 1','Wk 2','Wk 3','Wk 4'], data: [8400,9200,10800,12440] },
  year:  { labels: ['Jun','Jul','Aug','Sep','Oct','Nov','Dec','Jan','Feb','Mar','Apr','May'], data: [42000,48000,51000,45000,53000,61000,58000,67000,72000,69000,78000,85000] }
};

Chart.defaults.font.family = 'Outfit';

const revChart = new Chart(revCtx, {
  type: 'line',
  data: {
    labels: revData.week.labels,
    datasets: [{
      data: revData.week.data,
      borderColor: '#D4919A',
      backgroundColor: revGrad,
      borderWidth: 2.2,
      pointRadius: 3,
      pointBackgroundColor: '#D4919A',
      pointHoverRadius: 5,
      tension: 0.42,
      fill: true
    }]
  },
  options: {
    plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false, callbacks: { label: c => ' €' + c.raw.toLocaleString() } } },
    scales: {
      x: { grid: { display: false }, ticks: { font: { size:11 }, color:'#8A7B82' } },
      y: { grid: { color:'rgba(0,0,0,0.04)' }, ticks: { font: { size:11 }, color:'#8A7B82', callback: v => '€' + (v >= 1000 ? (v/1000).toFixed(0)+'k' : v) } }
    },
    interaction: { mode: 'index', intersect: false }
  }
});

window.revChart = revChart;

document.querySelectorAll('.period-toggle button').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.period-toggle button').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const source = window._dashRevData || revData;
    const d = source[btn.dataset.period];
    revChart.data.labels = d.labels;
    revChart.data.datasets[0].data = d.data;
    revChart.update('active');
  });
});

// ── Reports Charts ────────────────────────
let reportsInited = false;
function initReportsCharts() {
  if (reportsInited) return;
  reportsInited = true;

  const barCtx = document.getElementById('revenueBarChart').getContext('2d');
  new Chart(barCtx, {
    type: 'bar',
    data: {
      labels: ['Dec','Jan','Feb','Mar','Apr','May'],
      datasets: [{
        label: 'Revenue (€)',
        data: [42000,48000,51000,45000,53000,61000],
        backgroundColor: ctx => {
          const g = ctx.chart.ctx.createLinearGradient(0,0,0,300);
          g.addColorStop(0,'rgba(212,145,154,0.85)');
          g.addColorStop(1,'rgba(212,145,154,0.2)');
          return g;
        },
        borderRadius: 7, borderSkipped: false
      }]
    },
    options: {
      plugins: { legend: { display: false }, tooltip: { callbacks: { label: c => ' €' + c.raw.toLocaleString() } } },
      scales: {
        x: { grid: { display:false }, ticks: { font:{size:12}, color:'#8A7B82' } },
        y: { grid: { color:'rgba(0,0,0,0.04)' }, ticks: { font:{size:11}, color:'#8A7B82', callback: v => '€'+(v/1000).toFixed(0)+'k' } }
      }
    }
  });

  const catColors = ['#D4919A','#2563EB','#16A34A','#D97706'];
  const catLabels = ['Skincare','Fragrance','Body Care','Gift Sets'];
  const catData   = [45, 28, 15, 12];
  new Chart(document.getElementById('categoryChart').getContext('2d'), {
    type: 'doughnut',
    data: { labels: catLabels, datasets: [{ data: catData, backgroundColor: catColors, borderWidth: 0, hoverOffset: 8 }] },
    options: { cutout:'68%', plugins:{ legend:{display:false}, tooltip:{ callbacks:{ label: c => c.label + ': ' + c.raw + '%' } } } }
  });

  const legend = document.getElementById('categoryLegend');
  catLabels.forEach((l,i) => {
    legend.innerHTML += `<div class="legend-item"><div class="legend-dot" style="background:${catColors[i]}"></div><span class="legend-label">${l}</span><span class="legend-val">${catData[i]}%</span></div>`;
  });
}

// ── Filters ───────────────────────────────
function filterTable(tableId, searchVal, statusVal) {
  document.querySelectorAll('#' + tableId + ' tbody tr').forEach(tr => {
    const text = tr.textContent.toLowerCase();
    const status = (tr.dataset.status || '').toLowerCase();
    const matchText = !searchVal || text.includes(searchVal.toLowerCase());
    const matchStatus = !statusVal || status === statusVal;
    tr.style.display = matchText && matchStatus ? '' : 'none';
  });
}

document.getElementById('orderSearch').addEventListener('input', e => {
  filterTable('ordersTable', e.target.value, document.getElementById('orderStatusFilter').value);
});
document.getElementById('orderStatusFilter').addEventListener('change', e => {
  filterTable('ordersTable', document.getElementById('orderSearch').value, e.target.value);
});

function filterCatalogue() {
  const search = document.getElementById('catalogueSearch').value.toLowerCase();
  const cat = document.getElementById('categoryFilter').value.toLowerCase();
  document.querySelectorAll('#catalogueTable tbody tr').forEach(tr => {
    const text = tr.textContent.toLowerCase();
    const category = (tr.dataset.category || '').toLowerCase();
    tr.style.display = (!search || text.includes(search)) && (!cat || category === cat) ? '' : 'none';
  });
}
document.getElementById('catalogueSearch')?.addEventListener('input', filterCatalogue);
document.getElementById('categoryFilter')?.addEventListener('change', filterCatalogue);

// Team Search
document.getElementById('teamSearch').addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  document.querySelectorAll('#teamGrid .team-card').forEach(card => {
    card.style.display = !q || card.dataset.name.includes(q) ? '' : 'none';
  });
});

// ── Modals ────────────────────────────────
window.openModal = id => document.getElementById(id).classList.add('open');
window.closeModal = id => document.getElementById(id).classList.remove('open');

document.querySelectorAll('[data-close]').forEach(btn => {
  btn.addEventListener('click', () => closeModal(btn.dataset.close));
});
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', e => {
    if (e.target === overlay) overlay.classList.remove('open');
  });
});

document.getElementById('openNewOrderModal').addEventListener('click', () => openModal('newOrderModal'));
document.getElementById('openAddProductModal').addEventListener('click', () => openModal('addProductModal'));
document.getElementById('openAddTaskModal').addEventListener('click', () => openModal('addTaskModal'));
document.getElementById('qNewOrder').addEventListener('click', () => openModal('newOrderModal'));

// Submit New Order
document.getElementById('submitNewOrder').addEventListener('click', () => {
  const customer = document.getElementById('no-customer').value;
  if (!customer.trim()) { showToast('Please enter a customer name', 'warn'); return; }
  closeModal('newOrderModal');
  const id = '#SH-' + (1048 + Math.floor(Math.random()*10));
  const tbody = document.querySelector('#ordersTable tbody');
  const tr = document.createElement('tr');
  tr.dataset.status = 'processing';
  tr.innerHTML = `<td><strong>${escapeHtml(id)}</strong></td><td>${new Date().toLocaleDateString('en-GB',{day:'numeric',month:'short',year:'numeric'})}</td><td>${escapeHtml(customer)}</td><td>—</td><td>—</td><td><span class="status status--pending">Processing</span></td><td><div class="row-actions"><button class="dash-btn dash-btn--outline dash-btn--sm">View</button><button class="dash-btn dash-btn--outline dash-btn--sm">Edit</button></div></td>`;
  tbody.prepend(tr);
  document.querySelector('.sidebar-link[data-tab="orders"] .badge').textContent =
    parseInt(document.querySelector('.sidebar-link[data-tab="orders"] .badge').textContent) + 1;
  showToast(`Order ${id} created!`, 'success');
});

// View Order Detail (API data)
document.addEventListener('click', e => {
  const btn = e.target.closest('.view-order-api');
  if (!btn) return;
  const id = btn.dataset.id;
  const p = _apiOrderCache[id];
  if (!p) return;

  const statusMap = { procesando: 'status--pending', pendiente: 'status--pending', enviado: 'status--shipped', entregado: 'status--active', cancelado: 'status--danger' };
  const statusLabel = { procesando: 'Procesando', pendiente: 'Pendiente', enviado: 'Enviado', entregado: 'Entregado', cancelado: 'Cancelado' };
  const estado = (p.estado || '').toLowerCase();
  const fechaStr = p.fecha ? new Date(p.fecha).toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' }) : '—';
  const detalles = p.detalles || [];
  const totalCalc = detalles.reduce((s, d) => s + (parseFloat(d.precioUnitario) * d.cantidad), 0);

  document.getElementById('orderDetailTitle').textContent = 'Pedido #' + id;
  document.getElementById('orderDetailTitle').dataset.orderId = id;
  const statusSelect = document.getElementById('orderDetailStatusSelect');
  if (statusSelect) statusSelect.value = estado || 'pendiente';
  document.getElementById('orderDetailBody').innerHTML = `
    <div class="dash-grid-2" style="margin-bottom:18px">
      <div>
        <div class="dash-label">Cliente</div>
        <div style="font-weight:600;margin-top:4px">${escapeHtml(p.nombreUsuario || '—')}</div>
        <div style="color:var(--dash-muted);font-size:0.8rem">${escapeHtml(p.emailUsuario || '')}</div>
      </div>
      <div>
        <div class="dash-label">Estado</div>
        <span class="status ${statusMap[estado] || 'status--pending'}" style="margin-top:6px;display:inline-flex">${statusLabel[estado] || p.estado}</span>
      </div>
    </div>
    <div class="dash-grid-2" style="margin-bottom:18px">
      <div>
        <div class="dash-label">Fecha</div>
        <div style="margin-top:4px">${fechaStr}</div>
      </div>
      <div>
        <div class="dash-label">Total</div>
        <div style="margin-top:4px;font-weight:700;color:var(--dash-rose)">€${parseFloat(p.total || totalCalc).toFixed(2)}</div>
      </div>
    </div>
    <div class="dash-label" style="margin-bottom:10px">Artículos del pedido</div>
    <table class="dash-table" style="margin-bottom:0">
      <thead><tr><th>Producto</th><th>Cant.</th><th>Precio unit.</th><th>Subtotal</th></tr></thead>
      <tbody>
        ${detalles.length
          ? detalles.map(d => `<tr><td>${escapeHtml(d.nombre || '—')}</td><td>${d.cantidad}</td><td>€${parseFloat(d.precioUnitario).toFixed(2)}</td><td>€${(parseFloat(d.precioUnitario) * d.cantidad).toFixed(2)}</td></tr>`).join('')
          : '<tr><td colspan="4" style="text-align:center;color:var(--dash-muted)">Sin artículos</td></tr>'}
        <tr style="font-weight:700"><td colspan="3" style="text-align:right">Total</td><td>€${parseFloat(p.total || totalCalc).toFixed(2)}</td></tr>
      </tbody>
    </table>
  `;
  openModal('orderDetailModal');

  // Mostrar botón "Bizum recibido" solo si el pago está pendiente
  const confirmBtn = document.getElementById('orderDetailConfirmBizum');
  confirmBtn.style.display = 'none';
  confirmBtn.dataset.pedidoId = id;
  fetch(`${INTRANET_API}/pagos/pedido/${id}`)
    .then(r => r.ok ? r.json() : null)
    .then(pago => {
      if (pago && pago.estado === 'pendiente') {
        confirmBtn.style.display = 'inline-flex';
        confirmBtn.textContent = '✓ Bizum recibido';
        confirmBtn.disabled = false;
      }
    })
    .catch(() => {});
});

// Confirmar pago Bizum desde intranet
document.getElementById('orderDetailConfirmBizum')?.addEventListener('click', async function () {
  const idPedido = this.dataset.pedidoId;
  if (!idPedido) return;
  this.disabled = true;
  this.textContent = 'Confirmando…';
  try {
    const res = await fetch(`${INTRANET_API}/pagos/pedido/${idPedido}/confirmar`, { method: 'PUT' });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || `HTTP ${res.status}`);
    }
    this.style.display = 'none';
    closeModal('orderDetailModal');
    showToast(`Pago del pedido #${idPedido} confirmado. ¡Puntos añadidos!`, 'success');
    if (typeof window.loadIntranetOrders === 'function') window.loadIntranetOrders();
    if (typeof window.updateDashboardKPIs === 'function') window.updateDashboardKPIs();
  } catch (e) {
    showToast(`Error al confirmar: ${e.message}`, 'error');
    this.disabled = false;
    this.textContent = '✓ Bizum recibido';
  }
});

// Guardar estado del pedido desde modal de detalle
document.getElementById('orderDetailSaveStatus')?.addEventListener('click', async () => {
  const titleEl   = document.getElementById('orderDetailTitle');
  const selectEl  = document.getElementById('orderDetailStatusSelect');
  const orderId   = titleEl?.dataset.orderId;
  const newStatus = selectEl?.value;
  if (!orderId || !newStatus) return;

  const btn = document.getElementById('orderDetailSaveStatus');
  btn.disabled = true;
  btn.textContent = 'Guardando…';

  try {
    const res = await fetch(`${INTRANET_API}/intranet/pedidos/${orderId}/estado`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ estado: newStatus })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || `HTTP ${res.status}`);
    }
    // Actualizar caché local
    if (_apiOrderCache[orderId]) _apiOrderCache[orderId].estado = newStatus;
    closeModal('orderDetailModal');
    showToast(`Estado del pedido #${orderId} actualizado a "${newStatus}"`, 'success');
    if (typeof window.loadIntranetOrders === 'function') window.loadIntranetOrders();
    if (typeof window.updateDashboardKPIs === 'function') window.updateDashboardKPIs();
  } catch (e) {
    showToast(`Error al actualizar estado: ${e.message}`, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Guardar estado';
  }
});

// Add Task
document.getElementById('submitTask').addEventListener('click', () => {
  const title = document.getElementById('task-title').value.trim();
  if (!title) { showToast('Please enter a task title', 'warn'); return; }
  const assignee = document.getElementById('task-assignee').value;
  const date = document.getElementById('task-date').value;
  const priority = document.getElementById('task-priority').value;
  const priorityMap = { urgent:'status--expired', high:'status--pending', medium:'status--shipped', low:'status--draft' };
  const card = document.createElement('div');
  card.className = 'kanban-card';
  card.draggable = true;
  card.dataset.id = 't' + Date.now();
  card.innerHTML = `
    <div class="kanban-card__title">${title}</div>
    <div class="kanban-card__meta"><span class="status ${priorityMap[priority]}">${priority.charAt(0).toUpperCase()+priority.slice(1)}</span><span>${assignee.split(' ')[0]} · ${date || '—'}</span></div>
  `;
  document.getElementById('cards-todo').appendChild(card);
  updateKanbanCounts();
  initCardDrag(card);
  closeModal('addTaskModal');
  document.getElementById('task-title').value = '';
  showToast('Task added to To Do!', 'success');
});

// ── Kanban Drag & Drop ───────────────────
function initCardDrag(card) {
  card.addEventListener('dragstart', e => {
    e.dataTransfer.setData('text/plain', card.dataset.id);
    card.classList.add('dragging');
  });
  card.addEventListener('dragend', () => card.classList.remove('dragging'));
}

document.querySelectorAll('.kanban-card').forEach(initCardDrag);

document.querySelectorAll('.kanban-cards').forEach(col => {
  col.addEventListener('dragover', e => { e.preventDefault(); col.style.background = 'rgba(212,145,154,0.07)'; });
  col.addEventListener('dragleave', () => col.style.background = '');
  col.addEventListener('drop', e => {
    e.preventDefault();
    col.style.background = '';
    const id = e.dataTransfer.getData('text/plain');
    const card = document.querySelector(`.kanban-card[data-id="${id}"]`);
    if (card) { col.appendChild(card); updateKanbanCounts(); showToast('Task moved!', 'success'); }
  });
});

function updateKanbanCounts() {
  ['todo','inprogress','done'].forEach(col => {
    const cnt = document.getElementById('cards-' + col)?.querySelectorAll('.kanban-card').length || 0;
    const el = document.getElementById('cnt-' + col);
    if (el) el.textContent = cnt;
  });
}

// ── Progress Bars Animation ──────────────
function animateProgressBars() {
  document.querySelectorAll('.progress-bar__fill').forEach(bar => {
    const target = bar.style.width;
    bar.style.width = '0%';
    setTimeout(() => { bar.style.width = target; }, 100);
  });
}

// ── Export CSV ────────────────────────────
function tableToCSV(tableId) {
  const rows = document.querySelectorAll('#' + tableId + ' tr');
  return Array.from(rows).map(r =>
    Array.from(r.querySelectorAll('th,td')).map(c => '"' + c.textContent.trim().replace(/"/g,'""') + '"').join(',')
  ).join('\n');
}

function downloadCSV(content, filename) {
  const a = document.createElement('a');
  a.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(content);
  a.download = filename;
  a.click();
}

document.getElementById('exportOrdersCSV')?.addEventListener('click', () => {
  downloadCSV(tableToCSV('ordersTable'), 'shine-orders.csv');
  showToast('Orders exported to CSV', 'success');
});
document.getElementById('exportReportsCSV')?.addEventListener('click', () => {
  showToast('Report exported!', 'success');
});

// ── Toast System ──────────────────────────
window.showToast = function(message, type = 'default', duration = 3000) {
  const stack = document.getElementById('toastStack');
  const icons = {
    success: '<circle cx="12" cy="12" r="10"/><polyline points="9 12 11 14 15 10"/>',
    error: '<circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>',
    warn: '<path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>',
    default: '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>'
  };
  const toast = document.createElement('div');
  toast.className = 'toast' + (type !== 'default' ? ' toast--' + type : '');
  toast.innerHTML = `<svg viewBox="0 0 24 24">${icons[type]||icons.default}</svg>${message}`;
  stack.appendChild(toast);
  setTimeout(() => {
    toast.style.animation = 'toastOut 0.3s ease forwards';
    setTimeout(() => toast.remove(), 300);
  }, duration);
};

// ── Speed-Dial FAB ────────────────────────
const speedDial = document.getElementById('speedDial');
const sdMain = document.getElementById('sdMain');
sdMain.addEventListener('click', () => {
  speedDial.classList.toggle('open');
  const badge = sdMain.querySelector('.sd-badge');
  if (badge) badge.style.display = 'none';
});
document.addEventListener('click', e => {
  if (!e.target.closest('.speed-dial')) speedDial.classList.remove('open');
});
// ── Alert Team ──────────────────────────────────────────
document.getElementById('alertPriorityGrid')?.addEventListener('click', e => {
  const btn = e.target.closest('.alert-priority-btn');
  if (!btn) return;
  document.querySelectorAll('.alert-priority-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
});

document.getElementById('submitAlert')?.addEventListener('click', () => {
  const msg = document.getElementById('alertMessage').value.trim();
  if (!msg) { showToast('Please write a message before sending.', 'warn'); return; }
  const priority = document.querySelector('.alert-priority-btn.active')?.dataset.priority || 'normal';
  const labels = { urgent: '🔴 Urgent', high: '🟠 High', normal: '🟡 Normal', info: '🔵 Info' };
  closeModal('alertTeamModal');
  document.getElementById('alertMessage').value = '';
  showToast(`${labels[priority]} alert sent to the team!`, 'warn');
});

document.getElementById('sdAlert')?.addEventListener('click', () => {
  speedDial.classList.remove('open');
  openModal('alertTeamModal');
});

// ── Quick Note ───────────────────────────────────────────
const NOTES_KEY = 'shine_quick_notes';

function loadNotes() {
  try { return JSON.parse(localStorage.getItem(NOTES_KEY)) || []; } catch { return []; }
}
function saveNotes(notes) { localStorage.setItem(NOTES_KEY, JSON.stringify(notes)); }

function renderNotes() {
  const list = document.getElementById('notesList');
  if (!list) return;
  const notes = loadNotes();
  if (!notes.length) {
    list.innerHTML = '<p class="notes-empty">No notes yet. Write your first one above!</p>';
    return;
  }
  list.innerHTML = notes.map((n, i) => `
    <div class="note-item">
      <div class="note-item__content">
        ${n.title ? `<div class="note-item__title">${escapeHtml(n.title)}</div>` : ''}
        <div class="note-item__body">${escapeHtml(n.body)}</div>
        <div class="note-item__date">${escapeHtml(n.date)}</div>
      </div>
      <button class="note-item__delete" data-note-idx="${i}" title="Delete">✕</button>
    </div>
  `).join('');
}

document.getElementById('notesList')?.addEventListener('click', e => {
  const btn = e.target.closest('.note-item__delete');
  if (!btn) return;
  const notes = loadNotes();
  notes.splice(parseInt(btn.dataset.noteIdx), 1);
  saveNotes(notes);
  renderNotes();
});

document.getElementById('submitNote')?.addEventListener('click', () => {
  const title = document.getElementById('noteTitle').value.trim();
  const body = document.getElementById('noteBody').value.trim();
  if (!body) { showToast('Write something first!', 'warn'); return; }
  const notes = loadNotes();
  notes.unshift({ title, body, date: new Date().toLocaleString() });
  saveNotes(notes);
  document.getElementById('noteTitle').value = '';
  document.getElementById('noteBody').value = '';
  renderNotes();
  showToast('Note saved!', 'success');
});

document.getElementById('sdNote')?.addEventListener('click', () => {
  speedDial.classList.remove('open');
  renderNotes();
  openModal('quickNoteModal');
});

// ── Team Chat ────────────────────────────────────────────
const CHAT_KEY = 'shine_team_chat';
const DEFAULT_MESSAGES = [
  { user: 'Sofia C.', initials: 'SC', text: 'Morning everyone! Ready for the day? 👋', time: '9:02 AM', out: false },
  { user: 'Marco R.', initials: 'MR', text: 'Hi! Just finished the weekly report.', time: '9:05 AM', out: false },
  { user: 'Elena', initials: 'EL', text: 'Inventory is all updated. Stock looking good!', time: '9:11 AM', out: true },
  { user: 'Sofia C.', initials: 'SC', text: 'Perfect, thanks Elena!', time: '9:13 AM', out: false },
];

function loadChat() {
  try { return JSON.parse(localStorage.getItem(CHAT_KEY)) || DEFAULT_MESSAGES; } catch { return DEFAULT_MESSAGES; }
}
function saveChat(msgs) { localStorage.setItem(CHAT_KEY, JSON.stringify(msgs)); }

function renderChat() {
  const container = document.getElementById('chatMessages');
  if (!container) return;
  const msgs = loadChat();
  container.innerHTML = msgs.map(m => `
    <div class="chat-msg ${m.out ? 'chat-msg--out' : 'chat-msg--in'}">
      <div class="chat-msg__avatar">${escapeHtml(m.initials)}</div>
      <div class="chat-msg__body">
        ${!m.out ? `<div class="chat-msg__name">${escapeHtml(m.user)}</div>` : ''}
        <div class="chat-msg__bubble">${escapeHtml(m.text)}</div>
        <div class="chat-msg__time">${escapeHtml(m.time)}</div>
      </div>
    </div>
  `).join('');
  container.scrollTop = container.scrollHeight;
}

function sendChatMessage() {
  const input = document.getElementById('chatInput');
  const text = input.value.trim();
  if (!text) return;
  const msgs = loadChat();
  const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  msgs.push({ user: 'You', initials: 'EL', text, time, out: true });
  saveChat(msgs);
  input.value = '';
  renderChat();
}

function openChatPanel() {
  document.getElementById('chatPanel').classList.add('open');
  document.getElementById('chatPanelOverlay').classList.add('open');
  renderChat();
}
function closeChatPanel() {
  document.getElementById('chatPanel').classList.remove('open');
  document.getElementById('chatPanelOverlay').classList.remove('open');
}

document.getElementById('chatPanelClose')?.addEventListener('click', closeChatPanel);
document.getElementById('chatPanelOverlay')?.addEventListener('click', closeChatPanel);
document.getElementById('chatSend')?.addEventListener('click', sendChatMessage);
document.getElementById('chatInput')?.addEventListener('keydown', e => {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendChatMessage(); }
});

document.getElementById('sdChat')?.addEventListener('click', () => {
  speedDial.classList.remove('open');
  openChatPanel();
});

// ── Keyboard Shortcuts ────────────────────
document.addEventListener('keydown', e => {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault();
    searchInput.focus();
    searchInput.select();
  }
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
    searchResults.classList.remove('open');
    notifDropdown.classList.remove('open');
    speedDial.classList.remove('open');
    closeChatPanel();
  }
});

// ── Welcome Toast (handled by role-based block below) ─────────────────────────

// ══════════════════════════════════════════
// PREMIUM ENHANCEMENTS v2.0
// ══════════════════════════════════════════

// ── Time-Based Dynamic Greeting ──────────
const greetingEl = document.getElementById('homeGreeting');
if (greetingEl) {
  const hour = new Date().getHours();
  let greeting, emoji;
  if (hour < 6)       { greeting = 'Good night'; emoji = '🌙'; }
  else if (hour < 12) { greeting = 'Good morning'; emoji = '☀️'; }
  else if (hour < 18) { greeting = 'Good afternoon'; emoji = '🌤️'; }
  else if (hour < 22) { greeting = 'Good evening'; emoji = '🌅'; }
  else                 { greeting = 'Good night'; emoji = '🌙'; }
  greetingEl.textContent = greeting;
  // Update welcome username emoji
  const usernameEl = document.querySelector('.home-username');
  if (usernameEl) {
    let firstName = 'Staff';
    try {
      const s = JSON.parse(localStorage.getItem('shineStaff'));
      if (s && s.name) firstName = s.name.split(' ')[0];
    } catch(e) {}
    usernameEl.textContent = `Welcome back, ${firstName} ${emoji}`;
  }
}

// ── Animated KPI Counters ─────────────────
function animateKPIValue(el, target, prefix = '', suffix = '') {
  const duration = 1800;
  const start = performance.now();
  function update(now) {
    const elapsed = now - start;
    const progress = Math.min(elapsed / duration, 1);
    const eased = 1 - Math.pow(1 - progress, 3);
    const current = Math.round(eased * target);
    el.textContent = prefix + current.toLocaleString() + suffix;
    if (progress < 1) requestAnimationFrame(update);
  }
  requestAnimationFrame(update);
}

// Trigger KPI refresh when dashboard tab is shown
const origGoToTab = goToTab;
goToTab = function(tab) {
  origGoToTab(tab);
  if (tab === 'dashboard') {
    setTimeout(() => {
      if (typeof window.updateDashboardKPIs === 'function') window.updateDashboardKPIs();
      if (typeof window.loadActivityFeed === 'function')    window.loadActivityFeed();
    }, 200);
  }
  // Scroll to top on tab change
  document.querySelector('.dash-content')?.scrollTo({ top: 0, behavior: 'smooth' });
};

// ── Ripple Effect on Buttons ──────────────
document.addEventListener('mousedown', e => {
  const btn = e.target.closest('.dash-btn');
  if (!btn) return;
  const rect = btn.getBoundingClientRect();
  const x = ((e.clientX - rect.left) / rect.width * 100).toFixed(1);
  const y = ((e.clientY - rect.top) / rect.height * 100).toFixed(1);
  btn.style.setProperty('--ripple-x', x + '%');
  btn.style.setProperty('--ripple-y', y + '%');
});

// ── KPI Card Tilt Effect ──────────────────
document.querySelectorAll('.kpi-card').forEach(card => {
  card.addEventListener('mousemove', function(e) {
    const rect = this.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width - 0.5;
    const y = (e.clientY - rect.top) / rect.height - 0.5;
    this.style.transform = `translateY(-2px) perspective(500px) rotateX(${y * -4}deg) rotateY(${x * 4}deg)`;
  });
  card.addEventListener('mouseleave', function() {
    this.style.transform = '';
  });
});

// ── Home Quick Link Tilt ──────────────────
document.querySelectorAll('.home-ql-btn').forEach(btn => {
  btn.addEventListener('mousemove', function(e) {
    const rect = this.getBoundingClientRect();
    const x = (e.clientX - rect.left) / rect.width - 0.5;
    const y = (e.clientY - rect.top) / rect.height - 0.5;
    this.style.transform = `translateY(-1px) perspective(400px) rotateX(${y * -3}deg) rotateY(${x * 3}deg)`;
  });
  btn.addEventListener('mouseleave', function() {
    this.style.transform = '';
  });
});

// ── Live Meeting Countdown ────────────────
function updateMeetingCountdowns() {
  const now = new Date();
  document.querySelectorAll('.home-meeting-item--today .home-meeting-time').forEach(timeEl => {
    const [hours, minutes] = timeEl.textContent.split(':').map(Number);
    const meetingTime = new Date();
    meetingTime.setHours(hours, minutes, 0, 0);
    const diff = meetingTime - now;
    const parentItem = timeEl.closest('.home-meeting-item');
    let existingCountdown = parentItem.querySelector('.meeting-countdown');

    if (diff > 0 && diff < 3600000) { // Less than 1 hour away
      const minsLeft = Math.ceil(diff / 60000);
      if (!existingCountdown) {
        existingCountdown = document.createElement('span');
        existingCountdown.className = 'meeting-countdown';
        existingCountdown.style.cssText = 'font-size:0.65rem;color:var(--dash-rose);font-weight:600;display:block;margin-top:2px;animation:countdownPulse 1s ease infinite';
        timeEl.parentElement.appendChild(existingCountdown);
      }
      existingCountdown.textContent = `in ${minsLeft}m`;
    } else if (diff > 0 && diff < 7200000) { // 1-2 hours
      if (!existingCountdown) {
        existingCountdown = document.createElement('span');
        existingCountdown.className = 'meeting-countdown';
        existingCountdown.style.cssText = 'font-size:0.65rem;color:var(--dash-muted);font-weight:500;display:block;margin-top:2px';
        timeEl.parentElement.appendChild(existingCountdown);
      }
      const hrs = Math.floor(diff / 3600000);
      const mins = Math.ceil((diff % 3600000) / 60000);
      existingCountdown.textContent = `in ${hrs}h ${mins}m`;
    }
  });
}
updateMeetingCountdowns();
setInterval(updateMeetingCountdowns, 60000);

// ── Task Completion Celebration ───────────
document.querySelectorAll('.home-task-check').forEach(check => {
  check.addEventListener('change', function() {
    if (this.checked) {
      const label = this.nextElementSibling;
      const title = label?.querySelector('.home-task-title')?.textContent || 'Task';
      showToast(`✅ "${title}" completed!`, 'success');

      // Confetti-like burst effect
      const rect = this.getBoundingClientRect();
      for (let i = 0; i < 6; i++) {
        const particle = document.createElement('div');
        particle.style.cssText = `
          position:fixed;
          width:6px;height:6px;
          border-radius:50%;
          background:${['#D4919A','#e8a4ad','#FBBF24','#4ADE80','#60A5FA'][Math.floor(Math.random()*5)]};
          left:${rect.left + rect.width/2}px;
          top:${rect.top + rect.height/2}px;
          z-index:9999;
          pointer-events:none;
          transition:all 0.6s cubic-bezier(.22,1,.36,1);
        `;
        document.body.appendChild(particle);
        requestAnimationFrame(() => {
          particle.style.left = `${rect.left + (Math.random() - 0.5) * 80}px`;
          particle.style.top = `${rect.top + (Math.random() - 0.5) * 80 - 20}px`;
          particle.style.opacity = '0';
          particle.style.transform = `scale(0)`;
        });
        setTimeout(() => particle.remove(), 700);
      }
    }
  });
});

// ── Sidebar Link Ripple ───────────────────
document.querySelectorAll('.sidebar-link').forEach(link => {
  link.addEventListener('click', function(e) {
    const ripple = document.createElement('div');
    const rect = this.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    ripple.style.cssText = `
      position:absolute;
      width:${size}px;height:${size}px;
      border-radius:50%;
      background:rgba(255,255,255,0.15);
      left:${e.clientX - rect.left - size/2}px;
      top:${e.clientY - rect.top - size/2}px;
      transform:scale(0);
      pointer-events:none;
      animation:sidebarRipple 0.5s ease-out forwards;
    `;
    this.style.position = 'relative';
    this.style.overflow = 'hidden';
    this.appendChild(ripple);
    setTimeout(() => ripple.remove(), 500);
  });
});

// Add sidebar ripple keyframe
const sidebarRippleStyle = document.createElement('style');
sidebarRippleStyle.textContent = `
  @keyframes sidebarRipple { to { transform: scale(2.5); opacity: 0; } }
  @keyframes countdownPulse { 0%,100%{opacity:1} 50%{opacity:0.5} }
`;
document.head.appendChild(sidebarRippleStyle);

// ── Cursor Glow Effect ────────────────────
const cursorGlow = document.getElementById('cursorGlow');
if (cursorGlow && window.innerWidth > 768) {
  let glowActive = false;
  document.addEventListener('mousemove', e => {
    if (!glowActive) { cursorGlow.classList.add('active'); glowActive = true; }
    cursorGlow.style.left = e.clientX + 'px';
    cursorGlow.style.top = e.clientY + 'px';
  });
  document.addEventListener('mouseleave', () => {
    cursorGlow.classList.remove('active');
    glowActive = false;
  });
}

// ── Sidebar Sliding Indicator ─────────────
const sidebarIndicator = document.getElementById('sidebarIndicator');
function updateSidebarIndicator() {
  const activeLink = document.querySelector('.sidebar-link.active');
  if (activeLink && sidebarIndicator) {
    const nav = document.getElementById('sidebarNav');
    const navRect = nav.getBoundingClientRect();
    const linkRect = activeLink.getBoundingClientRect();
    sidebarIndicator.style.top = (linkRect.top - navRect.top + nav.scrollTop) + 'px';
    sidebarIndicator.style.height = linkRect.height + 'px';
  }
}
setTimeout(updateSidebarIndicator, 100);
const origGoToTab2 = goToTab;
goToTab = function(tab) {
  origGoToTab2(tab);
  setTimeout(updateSidebarIndicator, 50);
};

// ── Scroll Reveal (Intersection Observer) ──
const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('revealed');
    }
  });
}, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });
document.querySelectorAll('.reveal-on-scroll').forEach(el => revealObserver.observe(el));

// ── Topbar Pulse Animation ────────────────
function animatePulse() {
  const polyline = document.getElementById('pulsePolyline');
  if (!polyline) return;
  const pts = [];
  for (let i = 0; i <= 7; i++) {
    const x = (i / 7) * 60;
    const y = 9 + (Math.random() - 0.5) * 12;
    pts.push(`${x.toFixed(1)},${y.toFixed(1)}`);
  }
  polyline.setAttribute('points', pts.join(' '));
}
setInterval(animatePulse, 2000);

// ── openChat stub for team cards ──────────
window.openChat = function(name) {
  showToast(`Opening chat with ${name}…`, 'success');
};

// ── Keyboard Shortcut Hints ───────────────
const searchInputEl = document.getElementById('globalSearch');
if (searchInputEl && window.innerWidth > 768) {
  const hint = document.createElement('kbd');
  hint.textContent = '⌘K';
  hint.style.cssText = `
    position:absolute;right:12px;top:50%;transform:translateY(-50%);
    font-size:0.62rem;font-family:'Outfit',sans-serif;
    background:var(--dash-border);color:var(--dash-muted);
    padding:2px 6px;border-radius:4px;pointer-events:none;
    opacity:0.6;
  `;
  searchInputEl.parentElement.appendChild(hint);
  searchInputEl.addEventListener('focus', () => hint.style.display = 'none');
  searchInputEl.addEventListener('blur', () => { if (!searchInputEl.value) hint.style.display = ''; });
}

// ── Enhanced Sidebar Brand Animation ──────
const brandEl = document.querySelector('.sidebar-brand-icon img');
if (brandEl) {
  brandEl.style.transition = 'transform 0.3s cubic-bezier(.34,1.56,.64,1)';
  brandEl.parentElement.addEventListener('mouseenter', () => {
    brandEl.style.transform = 'scale(1.1) rotate(-5deg)';
  });
  brandEl.parentElement.addEventListener('mouseleave', () => {
    brandEl.style.transform = '';
  });
}

// ── Auto-refresh Clock with Smooth Transition ──
const clockContainer = document.getElementById('topbarClock');
if (clockContainer) {
  clockContainer.style.transition = 'opacity 0.2s';
}

// ══════════════════════════════════════════════
//   CALENDARIO — Sistema completo
// ══════════════════════════════════════════════

const CAL_MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
const CAL_DIAS = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];

// Festivos nacionales España 2026 (mes 0-indexado)
const FESTIVOS_2026 = [
  { m:0, d:1, nombre:'Año Nuevo' },
  { m:0, d:6, nombre:'Reyes Magos' },
  { m:3, d:2, nombre:'Jueves Santo' },
  { m:3, d:3, nombre:'Viernes Santo' },
  { m:4, d:1, nombre:'Día del Trabajo' },
  { m:7, d:15, nombre:'Asunción de la Virgen' },
  { m:9, d:12, nombre:'Fiesta Nacional' },
  { m:10, d:1, nombre:'Todos los Santos' },
  { m:11, d:6, nombre:'Día de la Constitución' },
  { m:11, d:8, nombre:'Inmaculada Concepción' },
  { m:11, d:25, nombre:'Navidad' },
];

// Empleados con sus colores y días de vacaciones/ausencias
const EMPLEADOS = [
  { nombre:'Elena R.', color:'#d4919a', dias: [
    {m:6,d:1},{m:6,d:2},{m:6,d:3},{m:6,d:4},{m:6,d:7},{m:6,d:8},{m:6,d:9},{m:6,d:10},{m:6,d:11},
    {m:6,d:14},{m:6,d:15},{m:6,d:16},{m:6,d:17},{m:6,d:18},
    {m:11,d:22},{m:11,d:23},{m:11,d:24},{m:11,d:26},{m:11,d:29},{m:11,d:30}
  ]},
  { nombre:'Marco T.', color:'#2563eb', dias: [
    {m:7,d:3},{m:7,d:4},{m:7,d:5},{m:7,d:6},{m:7,d:7},{m:7,d:10},{m:7,d:11},{m:7,d:12},{m:7,d:13},{m:7,d:14},
    {m:7,d:17},{m:7,d:18},{m:7,d:19},{m:7,d:20},{m:7,d:21}
  ]},
  { nombre:'Sofía C.', color:'#16a34a', dias: [
    {m:5,d:15},{m:5,d:16},{m:5,d:17},{m:5,d:18},{m:5,d:19},
    {m:5,d:22},{m:5,d:23},{m:5,d:24},{m:5,d:25},{m:5,d:26},
    {m:11,d:22},{m:11,d:23},{m:11,d:24},{m:11,d:26},{m:11,d:29},{m:11,d:30}
  ]},
  { nombre:'Anna K.', color:'#d97706', dias: [
    {m:7,d:1},{m:7,d:4},{m:7,d:5},{m:7,d:6},{m:7,d:7},{m:7,d:8},
    {m:7,d:11},{m:7,d:12},{m:7,d:13},{m:7,d:14},
    {m:3,d:6},{m:3,d:7},{m:3,d:8},{m:3,d:9},{m:3,d:10}
  ]},
  { nombre:'Lucas P.', color:'#7c3aed', dias: [
    {m:8,d:1},{m:8,d:2},{m:8,d:3},{m:8,d:4},{m:8,d:5},
    {m:8,d:8},{m:8,d:9},{m:8,d:10},{m:8,d:11},{m:8,d:12},
    {m:4,d:5},{m:4,d:6},{m:4,d:7},{m:4,d:8},{m:4,d:9}
  ]},
];

function isFestivo(m, d) {
  return FESTIVOS_2026.find(f => f.m === m && f.d === d);
}

function getEmpleadosEnDia(m, d) {
  return EMPLEADOS.filter(e => e.dias.some(dia => dia.m === m && dia.d === d));
}

function renderCalMonth(container, year, month, isAnnual) {
  const firstDay = new Date(year, month, 1);
  let startDow = firstDay.getDay(); // 0=Dom
  startDow = startDow === 0 ? 6 : startDow - 1; // convert to Mon=0
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const prevDays = new Date(year, month, 0).getDate();
  const today = new Date();

  let html = '<div class="cal-grid-mini">';
  CAL_DIAS.forEach(d => { html += `<div class="cal-day-header">${isAnnual ? d.charAt(0) : d}</div>`; });

  // Previous month fill
  for (let i = startDow - 1; i >= 0; i--) {
    html += `<div class="cal-day cal-day--other">${prevDays - i}</div>`;
  }

  // Current month
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d);
    const dow = date.getDay();
    const isWeekend = dow === 0 || dow === 6;
    const isToday = d === today.getDate() && month === today.getMonth() && year === today.getFullYear();
    const festivo = isFestivo(month, d);
    const empleados = getEmpleadosEnDia(month, d);
    const adminEvts = getAdminCalEvents(month, d);

    let cls = 'cal-day';
    let title = '';
    if (isToday) { cls += ' cal-day--today'; title = 'Hoy'; }
    else if (festivo) { cls += ' cal-day--festivo'; title = festivo.nombre; }
    else if (isWeekend) { cls += ' cal-day--weekend'; }

    if (empleados.length > 0 && !isToday) {
      title += (title ? ' | ' : '') + empleados.map(e => e.nombre).join(', ');
    }
    if (adminEvts.length > 0) {
      title += (title ? ' | ' : '') + adminEvts.map(e => e.title).join(', ');
    }

    html += `<div class="${cls}" ${title ? `title="${title}"` : ''}>${d}`;
    if (empleados.length > 0 || adminEvts.length > 0) {
      html += '<div class="cal-day-dots">';
      empleados.forEach(e => { html += `<span class="cal-dot" style="background:${e.color}"></span>`; });
      adminEvts.forEach(e => { html += `<span class="cal-dot" style="background:${e.color}"></span>`; });
      html += '</div>';
    }
    html += '</div>';
  }

  // Next month fill
  const totalCells = startDow + daysInMonth;
  const remaining = (7 - (totalCells % 7)) % 7;
  for (let i = 1; i <= remaining; i++) {
    html += `<div class="cal-day cal-day--other">${i}</div>`;
  }

  html += '</div>';
  container.innerHTML = html;
}

function renderLegend(container) {
  let html = `<div class="cal-legend-item"><span class="cal-legend-dot" style="background:#2563eb"></span>Festivo</div>`;
  EMPLEADOS.forEach(e => {
    html += `<div class="cal-legend-item"><span class="cal-legend-dot" style="background:${e.color}"></span>${e.nombre}</div>`;
  });
  container.innerHTML = html;
}

// ── Mini Calendar (Home Tab) ──
let miniCalYear = new Date().getFullYear();
let miniCalMonth = new Date().getMonth();

function renderMiniCalendar() {
  const container = document.getElementById('miniCalendar');
  if (!container) return;

  let html = `<div class="cal-mini">
    <div class="cal-mini-header">
      <h4>${CAL_MESES[miniCalMonth]} ${miniCalYear}</h4>
      <div class="cal-mini-nav">
        <button id="calPrev">‹</button>
        <button id="calNext">›</button>
      </div>
    </div>
    <div id="calMiniGrid"></div>
  </div>`;
  container.innerHTML = html;

  const grid = document.getElementById('calMiniGrid');
  renderCalMonth(grid, miniCalYear, miniCalMonth, false);

  document.getElementById('calPrev').addEventListener('click', () => {
    miniCalMonth--;
    if (miniCalMonth < 0) { miniCalMonth = 11; miniCalYear--; }
    renderMiniCalendar();
  });
  document.getElementById('calNext').addEventListener('click', () => {
    miniCalMonth++;
    if (miniCalMonth > 11) { miniCalMonth = 0; miniCalYear++; }
    renderMiniCalendar();
  });

  renderLegend(document.getElementById('calLegendMini'));
}

renderMiniCalendar();

// ── Annual Calendar Modal ──
document.getElementById('openAnnualCalendar')?.addEventListener('click', () => {
  const grid = document.getElementById('annualCalendarGrid');
  const year = 2026;
  let html = '';
  for (let m = 0; m < 12; m++) {
    html += `<div class="annual-month">
      <div class="annual-month__title">${CAL_MESES[m]}</div>
      <div id="annualMonth${m}"></div>
    </div>`;
  }
  grid.innerHTML = html;

  for (let m = 0; m < 12; m++) {
    renderCalMonth(document.getElementById(`annualMonth${m}`), year, m, true);
  }

  renderLegend(document.getElementById('calLegendAnnual'));
  document.getElementById('annualCalendarModal').classList.add('open');
});

// ── Sign Out ──────────────────────────────
document.getElementById('intranetSignOut')?.addEventListener('click', () => {
  localStorage.removeItem('shineStaff');
  localStorage.removeItem('shineUser');
  window.location.href = 'staff-login.html';
});

});

// ══════════════════════════════════════════════
//   ROLE-BASED ACCESS SYSTEM
// ══════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

  // ── Read Staff Session ──────────────────────
  let staffSession = null;
  try {
    staffSession = JSON.parse(localStorage.getItem('shineStaff'));
  } catch(e) {}

  // Redirigir si no hay sesión de staff
  if (!staffSession) {
    window.location.href = 'staff-login.html';
    return;
  }

  const currentRole = staffSession.role || 'empleado';
  const currentName = staffSession.name || 'Staff';
  const currentInitials = staffSession.initials || 'ST';

  // Exponer rol globalmente para el guard de goToTab
  window._staffRole = currentRole;

  // Set role on HTML element for CSS-driven visibility
  document.documentElement.setAttribute('data-role', currentRole);

  // ── Update Sidebar User Info ────────────────
  const sidebarAvatar = document.getElementById('sidebarAvatar');
  const sidebarUserName = document.getElementById('sidebarUserName');
  const sidebarUserRole = document.getElementById('sidebarUserRole');

  if (sidebarAvatar) sidebarAvatar.textContent = currentInitials;
  if (sidebarUserName) sidebarUserName.textContent = currentName;
  if (sidebarUserRole) {
    const roleBadge = currentRole === 'admin' ? 'Admin' : 'Empleado';
    sidebarUserRole.innerHTML = `Online · ${roleBadge}`;
  }

  // Update welcome message
  const usernameEl = document.querySelector('.home-username');
  if (usernameEl && staffSession) {
    const hour = new Date().getHours();
    let emoji = '👋';
    if (hour < 6) emoji = '🌙';
    else if (hour < 12) emoji = '☀️';
    else if (hour < 18) emoji = '🌤️';
    else if (hour < 22) emoji = '🌅';
    else emoji = '🌙';
    usernameEl.textContent = `Welcome back, ${currentName.split(' ')[0]} ${emoji}`;
  }

  // ── Register Admin Tab Titles ───────────────
  // Extend the tab titles for the admin sections
  const pageTitleEl = document.getElementById('pageTitle');
  const adminTabTitles = {
    'admin-roles': 'Gestión de Roles',
    'admin-fichajes': 'Registro de Fichajes',
    'admin-creacion': 'Creación de Productos'
  };

  // Hook into tab navigation for admin tabs
  document.querySelectorAll('.sidebar-link[data-tab]').forEach(link => {
    if (link.dataset.tab.startsWith('admin-')) {
      link.addEventListener('click', () => {
        // Update title
        if (pageTitleEl) pageTitleEl.textContent = adminTabTitles[link.dataset.tab] || link.dataset.tab;
      });
    }
  });

  // ── Employee Restrictions on Catalogue ──────
  if (currentRole === 'empleado') {
    // Hide "Add Product" button (already done via CSS, but reinforce)
    const addProductBtn = document.getElementById('openAddProductModal');
    if (addProductBtn) addProductBtn.style.display = 'none';

    // In catalogue table, remove delete buttons but keep edit
    document.querySelectorAll('#catalogueTable .row-actions').forEach(actions => {
      // Keep edit buttons, they'll work
      // Don't add any create/delete capabilities
    });

    // Override welcome toast
    setTimeout(() => {
      if (window.showToast) window.showToast(`Bienvenido/a, ${currentName.split(' ')[0]}! (Empleado)`, 'success');
    }, 900);
  } else if (currentRole === 'admin') {
    setTimeout(() => {
      if (window.showToast) window.showToast(`Bienvenido/a, ${currentName.split(' ')[0]}! (Administrador)`, 'success');
    }, 900);
  }

  // ══════════════════════════════════════════════
  //   ADMIN PANEL: ROLES MANAGEMENT
  // ══════════════════════════════════════════════

  if (currentRole === 'admin') {

    // ── Open Create Account Modal ──────────────
    const openCreateBtn = document.getElementById('openCreateAccountModal');
    if (openCreateBtn) {
      openCreateBtn.addEventListener('click', () => {
        if (window.openModal) window.openModal('createAccountModal');
      });
    }

    // ── Submit Create Account → API ────────────
    const submitCreateBtn = document.getElementById('submitCreateAccount');
    if (submitCreateBtn) {
      submitCreateBtn.addEventListener('click', async () => {
        const fname = document.getElementById('ca-fname')?.value.trim();
        const lname = document.getElementById('ca-lname')?.value.trim();
        const email = document.getElementById('ca-email')?.value.trim();
        const password = document.getElementById('ca-password')?.value;
        const role = document.getElementById('ca-role')?.value;

        if (!fname || !lname || !email || !password || password.length < 6) {
          if (window.showToast) window.showToast('Por favor completa todos los campos (contraseña mín. 6 caracteres)', 'warn');
          return;
        }

        submitCreateBtn.disabled = true;
        submitCreateBtn.textContent = 'Creando…';
        try {
          const res = await fetch(`${INTRANET_API}/auth/register-staff`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre: `${fname} ${lname}`, email, password, rol: role })
          });
          if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || err.error || `Error ${res.status}`);
          }
          ['ca-fname','ca-lname','ca-email','ca-password','ca-position'].forEach(id => {
            const el = document.getElementById(id); if (el) el.value = '';
          });
          if (window.closeModal) window.closeModal('createAccountModal');
          if (window.showToast) window.showToast(`Cuenta creada para ${fname} ${lname} (${role})`, 'success');
          if (typeof window.loadRolesTable === 'function') window.loadRolesTable();
          if (typeof window.loadTeamMembers === 'function') window.loadTeamMembers();
        } catch (err) {
          if (window.showToast) window.showToast(`Error al crear la cuenta: ${err.message}`, 'error');
        } finally {
          submitCreateBtn.disabled = false;
          submitCreateBtn.textContent = 'Crear Cuenta';
        }

      });
    }

    // ── Save Role Changes → API ───────────────
    document.addEventListener('click', async e => {
      const saveBtn = e.target.closest('.btn-save-role');
      if (!saveBtn) return;
      const tr = saveBtn.closest('tr');
      const select = tr?.querySelector('.role-select');
      const userId = tr?.dataset.userId;
      if (!select || !userId) return;
      const newRole = select.value;
      saveBtn.disabled = true;
      try {
        const res = await fetch(`${INTRANET_API}/usuarios/${userId}/rol`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ rol: newRole })
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        if (window.showToast) window.showToast(`Rol actualizado a "${newRole}"`, 'success');
        if (typeof window.loadRolesTable === 'function') window.loadRolesTable();
      } catch (err) {
        if (window.showToast) window.showToast(`Error al actualizar el rol: ${err.message}`, 'error');
      } finally {
        saveBtn.disabled = false;
      }
    });

    // ── Delete Account → API ──────────────────
    document.addEventListener('click', async e => {
      const deleteBtn = e.target.closest('.btn-delete-account');
      if (!deleteBtn) return;
      const tr = deleteBtn.closest('tr');
      const userId = tr?.dataset.userId;
      const nombre = tr?.dataset.nombre || 'este usuario';
      if (!userId) return;
      if (!confirm(`¿Eliminar la cuenta de ${nombre}?`)) return;
      deleteBtn.disabled = true;
      try {
        const res = await fetch(`${INTRANET_API}/usuarios/${userId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        tr.style.animation = 'fadeUp 0.3s ease reverse forwards';
        setTimeout(() => tr.remove(), 300);
        if (window.showToast) window.showToast(`Cuenta de ${nombre} eliminada`, 'success');
        if (typeof window.loadTeamMembers === 'function') window.loadTeamMembers();
      } catch (err) {
        if (window.showToast) window.showToast(`Error al eliminar: ${err.message}`, 'error');
        deleteBtn.disabled = false;
      }
    });

    // ══════════════════════════════════════════════
    //   ADMIN PANEL: FICHAJES
    // ══════════════════════════════════════════════

    function localDateStr() {
      const d = new Date();
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
    }

    const dateFilter = document.getElementById('fichajesDateFilter');
    if (dateFilter) dateFilter.value = localDateStr();

    window.loadFichajesAdmin = async function loadFichajesAdmin() {
      const tbody = document.getElementById('fichajesBody');
      if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--dash-muted)">Cargando fichajes…</td></tr>';
      const fecha = dateFilter?.value || localDateStr();
      try {
        const res = await fetch(`${INTRANET_API}/fichajes?fecha=${fecha}`);
        if (!res.ok) {
          if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--dash-danger)">Error del servidor (${res.status})</td></tr>`;
          return;
        }
        const eventos = await res.json();
        renderFichajesAdmin(eventos, fecha);
      } catch (e) {
        console.error('Error cargando fichajes:', e);
        if (tbody) tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--dash-danger)">No se puede conectar con el servidor. ¿Está el backend arrancado?</td></tr>';
      }
    };

    function renderFichajesAdmin(fichajes, fecha) {
      const tbody = document.getElementById('fichajesBody');
      if (!tbody) return;

      const fechaDisplay = new Date(fecha + 'T12:00:00').toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
      let total = 0, completed = 0, active = 0;
      tbody.innerHTML = '';

      if (!fichajes.length) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:32px;color:var(--dash-muted)">No hay fichajes registrados para el ${fechaDisplay}</td></tr>`;
        const el = id => document.getElementById(id);
        if (el('fichajesTotal'))     el('fichajesTotal').textContent     = 0;
        if (el('fichajesCompleted')) el('fichajesCompleted').textContent = 0;
        if (el('fichajesActive'))    el('fichajesActive').textContent    = 0;
        if (el('fichajesAbsent'))    el('fichajesAbsent').textContent    = 0;
        return;
      }

      fichajes.forEach(f => {
        total++;
        const fmtTime = iso => iso ? new Date(iso).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }) : null;
        const entrada = fmtTime(f.horaEntrada);
        const salida  = fmtTime(f.horaSalida);
        let duration = '—', statusHtml = '';
        if (entrada && salida) {
          const [eh, em] = entrada.split(':').map(Number);
          const [sh, sm] = salida.split(':').map(Number);
          const mins = (sh * 60 + sm) - (eh * 60 + em);
          duration = `${Math.floor(mins / 60)}h ${mins % 60}m`;
          statusHtml = '<span class="status status--active">Completado</span>';
          completed++;
        } else if (entrada) {
          statusHtml = '<span class="status status--pending">En turno</span>';
          active++;
        } else {
          statusHtml = '<span class="status status--expired">Sin entrada</span>';
        }
        tbody.innerHTML += `<tr>
          <td><strong>${escapeHtml(f.empleadoNombre || '—')}</strong></td>
          <td>${fechaDisplay}</td>
          <td>${entrada || '—'}</td>
          <td>${salida  || '—'}</td>
          <td>${duration}</td>
          <td>${statusHtml}</td>
        </tr>`;
      });

      const el = id => document.getElementById(id);
      if (el('fichajesTotal'))     el('fichajesTotal').textContent     = total;
      if (el('fichajesCompleted')) el('fichajesCompleted').textContent = completed;
      if (el('fichajesActive'))    el('fichajesActive').textContent    = active;
      if (el('fichajesAbsent'))    el('fichajesAbsent').textContent    = Math.max(0, total - completed - active);
    }

    const fichajesBtn = document.getElementById('fichajesToday');
    if (fichajesBtn) {
      fichajesBtn.addEventListener('click', () => {
        if (dateFilter) dateFilter.value = localDateStr();
        loadFichajesAdmin();
      });
    }
    if (dateFilter) dateFilter.addEventListener('change', loadFichajesAdmin);
    loadFichajesAdmin();

    // ── Auto-refresh: Fichajes y Pedidos cada 30s ──
    setInterval(() => {
      if (typeof window.loadFichajesAdmin === 'function') window.loadFichajesAdmin();
      if (typeof window.loadIntranetOrders === 'function') window.loadIntranetOrders();
    }, 30000);

    // Preload orders data at startup
    if (typeof window.loadIntranetOrders === 'function') window.loadIntranetOrders();

    // Export fichajes CSV
    const exportFichajes = document.getElementById('exportFichajesCSV');
    if (exportFichajes) {
      exportFichajes.addEventListener('click', () => {
        const rows = document.querySelectorAll('#fichajesTable tr');
        const csv = Array.from(rows).map(r =>
          Array.from(r.querySelectorAll('th,td')).map(c => '"' + c.textContent.trim().replace(/"/g,'""') + '"').join(',')
        ).join('\n');
        const a = document.createElement('a');
        a.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
        a.download = 'shine-fichajes.csv';
        a.click();
        showToast('Fichajes exportados a CSV', 'success');
      });
    }

    // ══════════════════════════════════════════════
    //   ADMIN PANEL: PRODUCT CREATION
    // ══════════════════════════════════════════════

    const cpCategoryEl = document.getElementById('cp-category');
    const cpSubcategoryEl = document.getElementById('cp-subcategory');
    if (cpCategoryEl && cpSubcategoryEl) {
      cpCategoryEl.addEventListener('change', async () => {
        const catId = cpCategoryEl.value;
        cpSubcategoryEl.innerHTML = '<option value="">Sin subcategoría</option>';
        cpSubcategoryEl.disabled = true;
        if (!catId) return;
        try {
          const res = await fetch(`${INTRANET_API}/subcategorias?categoriaId=${catId}`);
          if (!res.ok) return;
          const subs = await res.json();
          if (Array.isArray(subs) && subs.length) {
            subs.forEach(s => {
              const opt = document.createElement('option');
              opt.value = s.idSubcategoria;
              opt.textContent = s.nombre;
              cpSubcategoryEl.appendChild(opt);
            });
            cpSubcategoryEl.disabled = false;
          }
        } catch (_) {}
      });
    }

    const createProductForm = document.getElementById('createProductForm');
    if (createProductForm) {
      createProductForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const name = document.getElementById('cp-name')?.value.trim();
        const sku = document.getElementById('cp-sku')?.value.trim();
        const catVal = document.getElementById('cp-category')?.value;
        const subcatVal = document.getElementById('cp-subcategory')?.value;
        const price = parseFloat(document.getElementById('cp-price')?.value || 0);
        const stock = parseInt(document.getElementById('cp-stock')?.value || 0);
        const desc = document.getElementById('cp-desc')?.value.trim() || '';
        const ingredients = document.getElementById('cp-ingredients')?.value.trim() || '';

        if (!name || !sku || !catVal || !price) {
          if (window.showToast) window.showToast('Por favor completa los campos obligatorios', 'warn');
          return;
        }

        const submitBtn = createProductForm.querySelector('[type="submit"]');
        if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = 'Creando...'; }

        try {
          const payload = { nombre: name, sku, idCategoria: parseInt(catVal), precio: price, stock, descripcion: desc, ingredientes: ingredients, imagenUrl: document.getElementById('cp-image')?.value.trim() || null };
          if (subcatVal) payload.idSubcategoria = parseInt(subcatVal);
          const res = await fetch(`${INTRANET_API}/productos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || `HTTP ${res.status}`);
          }

          createProductForm.reset();
          localStorage.removeItem('shine:productos:v2');
          if (window.showToast) window.showToast(`Producto "${name}" creado y añadido al sistema.`, 'success');

          // Navegar al catálogo y recargarlo
          document.querySelectorAll('.sidebar-link[data-tab]').forEach(l => l.classList.toggle('active', l.dataset.tab === 'catalogue'));
          document.querySelectorAll('.dash-section').forEach(s => s.classList.toggle('active', s.id === 'sec-catalogue'));
          if (pageTitleEl) pageTitleEl.textContent = 'Catalogue';
          if (typeof window.loadCatalogueProducts === 'function') window.loadCatalogueProducts();
        } catch (err) {
          if (window.showToast) window.showToast(`Error al crear el producto: ${err.message}`, 'error');
        } finally {
          if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Crear Producto'; }
        }
      });
    }

    // ── Delete Product (admin only) ───────────
    document.addEventListener('click', e => {
      const deleteBtn = e.target.closest('.btn-delete-product');
      if (!deleteBtn) return;
      const tr = deleteBtn.closest('tr');
      const productName = tr?.querySelector('strong')?.textContent || 'Producto';
      if (confirm(`¿Eliminar "${productName}" del catálogo?`)) {
        tr.style.animation = 'fadeUp 0.3s ease reverse forwards';
        setTimeout(() => {
          tr.remove();
          if (window.showToast) window.showToast(`"${productName}" eliminado del catálogo`, 'success');
        }, 300);
      }
    });
  }

}); // Role system DOMContentLoaded
// ══════════════════════════════════════════
// ADMIN FEATURES
// ══════════════════════════════════════════

function getAdminUserName() {
  try { const s = JSON.parse(localStorage.getItem('shineStaff')); return s?.name || 'Admin'; } catch { return 'Admin'; }
}

// ══════════════════════════════════════════════
//   BRIGHT BOX
// ══════════════════════════════════════════════

(function initBrightBox() {
  // ── Anonymous toggle ──────────────────────
  const bbAnonimo = document.getElementById('bb-anonimo');
  const bbNombre = document.getElementById('bb-nombre');
  const bbApellidos = document.getElementById('bb-apellidos');
  const bbReqMarks = document.querySelectorAll('.bb-name-req');

  if (bbAnonimo) {
    bbAnonimo.addEventListener('change', () => {
      const anon = bbAnonimo.checked;
      if (bbNombre) { bbNombre.disabled = anon; bbNombre.value = anon ? '' : bbNombre.value; bbNombre.placeholder = anon ? 'Anónimo' : 'Tu nombre'; }
      if (bbApellidos) { bbApellidos.disabled = anon; bbApellidos.value = anon ? '' : bbApellidos.value; bbApellidos.placeholder = anon ? 'Anónimo' : 'Tus apellidos'; }
      bbReqMarks.forEach(el => { el.style.display = anon ? 'none' : ''; });
    });
  }

  // ── Form submit ───────────────────────────
  const brightBoxForm = document.getElementById('brightBoxForm');
  if (brightBoxForm) {
    brightBoxForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const anon = bbAnonimo?.checked || false;
      const nombre = anon ? 'Anónimo' : (bbNombre?.value.trim() || '');
      const apellidos = anon ? 'Anónimo' : (bbApellidos?.value.trim() || '');
      const asunto = document.getElementById('bb-asunto')?.value.trim() || '';
      const texto = document.getElementById('bb-texto')?.value.trim() || '';

      if (!anon && (!nombre || !apellidos)) {
        if (window.showToast) showToast('Por favor, rellena tu nombre y apellidos (o activa Anónimo).', 'warn');
        return;
      }
      if (!asunto) {
        if (window.showToast) showToast('El asunto es obligatorio.', 'warn');
        return;
      }

      const submitBtn = brightBoxForm.querySelector('[type="submit"]');
      if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = 'Enviando…'; }

      try {
        const res = await fetch(`${INTRANET_API}/buzon`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ nombre, apellidos, asunto, texto })
        });
        if (!res.ok) {
          const err = await res.json().catch(() => ({}));
          throw new Error(err.error || `Error ${res.status}`);
        }
        brightBoxForm.reset();
        if (bbNombre) { bbNombre.disabled = false; bbNombre.placeholder = 'Tu nombre'; }
        if (bbApellidos) { bbApellidos.disabled = false; bbApellidos.placeholder = 'Tus apellidos'; }
        bbReqMarks.forEach(el => { el.style.display = ''; });
        if (window.showToast) showToast('Mensaje enviado. Gracias por tu confianza.', 'success');
      } catch (err) {
        if (window.showToast) showToast(`No se pudo enviar el mensaje: ${err.message}`, 'error');
      } finally {
        if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Enviar mensaje'; }
      }
    });
  }

  // ── Admin: load messages ──────────────────
  window.loadBrightBoxAdmin = async function loadBrightBoxAdmin() {
    const loading = document.getElementById('bbAdminLoading');
    const empty = document.getElementById('bbAdminEmpty');
    const list = document.getElementById('bbAdminList');
    if (!list) return;

    if (loading) loading.style.display = '';
    if (empty) empty.style.display = 'none';
    list.innerHTML = '';

    try {
      const res = await fetch(`${INTRANET_API}/buzon`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const mensajes = await res.json();

      if (loading) loading.style.display = 'none';

      if (!mensajes.length) {
        if (empty) empty.style.display = '';
        return;
      }

      list.innerHTML = mensajes.map(m => {
        const fecha = m.fecha ? new Date(m.fecha).toLocaleString('es-ES', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '—';
        const esAnonimo = m.nombre === 'Anónimo';
        return `
          <div class="bb-message-card" style="border:1px solid var(--dash-border);border-radius:12px;padding:18px 20px;margin-bottom:14px;background:var(--dash-surface)">
            <div style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:8px;margin-bottom:10px">
              <div style="display:flex;align-items:center;gap:10px">
                <div class="avatar ${esAnonimo ? 'avatar--rose' : 'avatar--teal'}" style="width:36px;height:36px;font-size:0.7rem;flex-shrink:0">${esAnonimo ? '?' : (escapeHtml(m.nombre[0]) + (m.apellidos?.[0] ? escapeHtml(m.apellidos[0]) : ''))}</div>
                <div>
                  <div style="font-weight:600;font-size:0.9rem;color:var(--dash-text)">${esAnonimo ? '<em style="color:var(--dash-muted)">Anónimo</em>' : escapeHtml(m.nombre) + ' ' + escapeHtml(m.apellidos || '')}</div>
                  <div style="font-size:0.75rem;color:var(--dash-muted)">${fecha}</div>
                </div>
              </div>
              <span style="font-size:0.78rem;background:var(--dash-rose-soft,rgba(212,145,154,.12));color:var(--dash-rose);padding:3px 10px;border-radius:20px;font-weight:500">${escapeHtml(m.asunto)}</span>
            </div>
            <p style="font-size:0.88rem;color:var(--dash-text);line-height:1.65;white-space:pre-wrap;margin:0">${escapeHtml(m.texto || '')}</p>
          </div>`;
      }).join('');

    } catch (err) {
      if (loading) loading.style.display = 'none';
      list.innerHTML = `<p style="color:var(--dash-danger);text-align:center;padding:24px 0">Error al cargar los mensajes: ${escapeHtml(err.message)}</p>`;
    }
  };

  // ── Refresh button ────────────────────────
  document.getElementById('refreshBrightBox')?.addEventListener('click', () => {
    if (window.loadBrightBoxAdmin) window.loadBrightBoxAdmin();
  });
})();

// ── Admin: Announcements ─────────────────────────────────
const ANNC_TAG_CLASS = { new: 'rose', hr: 'blue', event: 'green', reminder: 'amber', info: 'blue' };

function renderAnnouncementItem(item, list) {
  const div = document.createElement('div');
  div.className = 'home-annc-item home-annc-item--new admin-annc-injected';
  div.dataset.anncId = item.id || '';
  const tagClass = ANNC_TAG_CLASS[item.tag] || 'rose';
  const tagLabel = item.tagLabel || item.tag;
  const fecha = item.fecha ? new Date(item.fecha).toLocaleDateString('es-ES') : '';
  div.innerHTML = `
    <div class="home-annc-dot"></div>
    <div class="home-annc-body">
      <span class="home-annc-tag home-annc-tag--${tagClass}">${escapeHtml(tagLabel)}</span>
      <p class="home-annc-title">${escapeHtml(item.titulo || item.title || '')}</p>
      <p class="home-annc-meta">Publicado por ${escapeHtml(item.autor || item.author || '')} · ${fecha}</p>
    </div>`;
  list.prepend(div);
}

async function fetchAndRenderAnnouncements() {
  try {
    const res = await fetch(`${INTRANET_API}/anuncios`);
    if (!res.ok) return;
    const items = await res.json();
    const list = document.getElementById('homeanncList');
    if (!list) return;
    list.querySelectorAll('.admin-annc-injected').forEach(el => el.remove());
    items.forEach(item => renderAnnouncementItem(item, list));
  } catch (e) { console.warn('Error cargando anuncios:', e); }
}

document.getElementById('openNewAnnouncement')?.addEventListener('click', () => openModal('newAnnouncementModal'));

document.getElementById('submitNewAnnouncement')?.addEventListener('click', async () => {
  const titulo   = document.getElementById('anncTitle').value.trim();
  const mensaje  = document.getElementById('anncMessage')?.value.trim() || '';
  const tagEl    = document.getElementById('anncTag');
  const tag      = tagEl.value;
  const tagLabel = tagEl.options[tagEl.selectedIndex].text;
  if (!titulo) { showToast('Introduce un título para el anuncio.', 'warn'); return; }
  try {
    const res = await fetch(`${INTRANET_API}/anuncios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ titulo, tag, tagLabel, mensaje, autor: getAdminUserName() })
    });
    if (!res.ok) throw new Error(await res.text());
    await fetchAndRenderAnnouncements();
    closeModal('newAnnouncementModal');
    document.getElementById('anncTitle').value = '';
    if (document.getElementById('anncMessage')) document.getElementById('anncMessage').value = '';
    showToast('Anuncio publicado correctamente.', 'success');
  } catch (e) {
    console.error('Error publicando anuncio:', e);
    showToast('Error al publicar el anuncio.', 'error');
  }
});

fetchAndRenderAnnouncements();

// ── Admin: Meetings ──────────────────────────────────────
const PLATFORM_HTML = {
  meet:       '<svg viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg> Google Meet',
  zoom:       '<svg viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg> Zoom',
  teams:      '<svg viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg> MS Teams',
  presencial: '<svg viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg> Presencial',
};

function formatMeetingDay(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  const today = new Date();
  const tomorrow = new Date(today); tomorrow.setDate(today.getDate() + 1);
  if (d.toDateString() === today.toDateString()) return 'Hoy';
  if (d.toDateString() === tomorrow.toDateString()) return 'Mañana';
  return d.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric' });
}

function renderMeetingItem(m, list) {
  const fecha  = m.fecha  || m.date  || '';
  const hora   = m.hora   || m.time  || '';
  const titulo = m.titulo || m.title || '';
  const plat   = m.plataforma || m.platform || '';
  const asist  = m.asistentes || m.attendees || 'Equipo';
  const color  = m.color || 'rose';
  const isToday = fecha && new Date(fecha + 'T00:00:00').toDateString() === new Date().toDateString();
  const div = document.createElement('div');
  div.className = 'home-meeting-item' + (isToday ? ' home-meeting-item--today' : '') + ' admin-meeting-injected';
  div.dataset.reunionId = m.id || '';
  div.innerHTML = `
    <div class="home-meeting-date">
      <span class="home-meeting-day">${formatMeetingDay(fecha)}</span>
      <span class="home-meeting-time">${hora}</span>
    </div>
    <div class="home-meeting-bar home-meeting-bar--${color}"></div>
    <div class="home-meeting-info">
      <p class="home-meeting-title">${escapeHtml(titulo)}</p>
      <p class="home-meeting-meta">
        <svg viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        ${escapeHtml(asist)}
      </p>
      <p class="home-meeting-meta">${PLATFORM_HTML[plat] || escapeHtml(plat)}</p>
    </div>`;
  list.appendChild(div);
}

async function fetchAndRenderMeetings() {
  try {
    const res = await fetch(`${INTRANET_API}/reuniones`);
    if (!res.ok) return;
    const meetings = await res.json();
    const list = document.getElementById('homeMeetingsList');
    if (!list) return;
    list.querySelectorAll('.admin-meeting-injected').forEach(el => el.remove());
    meetings
      .sort((a, b) => {
        const fa = a.fecha || a.date || '', ha = a.hora || a.time || '';
        const fb = b.fecha || b.date || '', hb = b.hora || b.time || '';
        return new Date(fa + 'T' + ha) - new Date(fb + 'T' + hb);
      })
      .forEach(m => renderMeetingItem(m, list));
  } catch (e) { console.warn('Error cargando reuniones:', e); }
}

document.getElementById('openNewMeeting')?.addEventListener('click', () => openModal('newMeetingModal'));

document.getElementById('submitNewMeeting')?.addEventListener('click', async () => {
  const titulo     = document.getElementById('meetingTitle').value.trim();
  const fecha      = document.getElementById('meetingDate').value;
  const hora       = document.getElementById('meetingTime').value;
  const plataforma = document.getElementById('meetingPlatform').value;
  const asistentes = document.getElementById('meetingAttendees').value.trim();
  const color      = document.getElementById('meetingColor').value;
  if (!titulo || !fecha || !hora) { showToast('Completa título, fecha y hora.', 'warn'); return; }
  try {
    const res = await fetch(`${INTRANET_API}/reuniones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ titulo, fecha, hora, plataforma, asistentes, color, creadoPor: getAdminUserName() })
    });
    if (!res.ok) throw new Error(await res.text());
    await fetchAndRenderMeetings();
    closeModal('newMeetingModal');
    document.getElementById('meetingTitle').value = '';
    document.getElementById('meetingDate').value = '';
    document.getElementById('meetingTime').value = '';
    document.getElementById('meetingAttendees').value = '';
    showToast('Reunión programada correctamente.', 'success');
  } catch (e) {
    console.error('Error programando reunión:', e);
    showToast('Error al programar la reunión.', 'error');
  }
});

fetchAndRenderMeetings();

// ── Admin: Calendar Events ───────────────────────────────
const CAL_ADMIN_KEY = 'shine_admin_cal_events';

function getAdminCalEvents(month, day) {
  try {
    const events = JSON.parse(localStorage.getItem('shine_admin_cal_events')) || [];
    return events.filter(e => {
      const d = new Date(e.date + 'T00:00:00');
      return d.getMonth() === month && d.getDate() === day;
    });
  } catch { return []; }
}

function loadCalAdminEvents() {
  try { return JSON.parse(localStorage.getItem(CAL_ADMIN_KEY)) || []; } catch { return []; }
}
function saveCalAdminEvents(events) { localStorage.setItem(CAL_ADMIN_KEY, JSON.stringify(events)); }

function renderCalEvtList() {
  const container = document.getElementById('calEvtList');
  if (!container) return;
  const events = loadCalAdminEvents();
  if (!events.length) {
    container.innerHTML = '<p style="font-size:0.78rem;opacity:0.45;padding:6px 0">No events added yet.</p>';
    return;
  }
  container.innerHTML = events.map((e, i) => `
    <div style="display:flex;align-items:center;gap:8px;padding:6px 10px;border:1px solid var(--dash-border);border-radius:8px;background:var(--dash-card)">
      <span style="width:10px;height:10px;border-radius:50%;background:${e.color};flex-shrink:0"></span>
      <span style="flex:1;font-size:0.8rem;color:var(--dash-text)">${e.title} <span style="opacity:0.5">(${e.date})</span></span>
      <button onclick="deleteCalAdminEvent(${i})" style="background:none;border:none;cursor:pointer;color:var(--dash-danger);font-size:0.85rem;opacity:0.6;padding:0 4px" title="Delete">&#x2715;</button>
    </div>
  `).join('');
}

window.deleteCalAdminEvent = function(idx) {
  const events = loadCalAdminEvents();
  events.splice(idx, 1);
  saveCalAdminEvents(events);
  renderCalEvtList();
  renderMiniCalendar();
  showToast('Event removed.', 'success');
};

document.getElementById('openCalendarEdit')?.addEventListener('click', () => {
  renderCalEvtList();
  openModal('editCalendarModal');
});

document.getElementById('submitCalEvt')?.addEventListener('click', () => {
  const title = document.getElementById('calEvtTitle').value.trim();
  const date = document.getElementById('calEvtDate').value;
  const color = document.getElementById('calEvtColor').value;
  if (!title || !date) { showToast('Please fill in title and date.', 'warn'); return; }
  const events = loadCalAdminEvents();
  events.push({ title, date, color });
  saveCalAdminEvents(events);
  renderCalEvtList();
  renderMiniCalendar();
  document.getElementById('calEvtTitle').value = '';
  document.getElementById('calEvtDate').value = '';
  showToast('Event added to calendar!', 'success');
});

// ── Pedidos desde API ──────────────────────
window.loadIntranetOrders = async function () {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--dash-muted)">Cargando pedidos…</td></tr>';

  const STATUS_CLS   = {
    procesando:       'pending',
    pendiente:        'pending',
    pendiente_bizum:  'pending',
    enviado:          'shipped',
    entregado:        'active',
    cancelado:        'danger'
  };
  const STATUS_LABEL = {
    procesando:       'Procesando',
    pendiente:        'Pendiente',
    pendiente_bizum:  '📱 Bizum pendiente',
    enviado:          'Enviado',
    entregado:        'Entregado',
    cancelado:        'Cancelado'
  };

  function fmtFecha(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  try {
    const res = await fetch(`${INTRANET_API}/intranet/pedidos`);
    const pedidos = await res.json();

    if (!res.ok) {
      const msg = pedidos?.error || `Error del servidor (${res.status})`;
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--dash-danger)">⚠ ${escapeHtml(msg)}</td></tr>`;
      console.error('[loadIntranetOrders] Error del servidor:', res.status, pedidos);
      return;
    }

    if (!Array.isArray(pedidos) || !pedidos.length) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--dash-muted)">No hay pedidos todavía.</td></tr>';
      return;
    }

    // Cachear para el modal de detalle
    pedidos.forEach(p => { _apiOrderCache[p.idPedido] = p; });

    tbody.innerHTML = pedidos.map(p => {
      const estado = (p.estado || '').toLowerCase();
      const cls    = STATUS_CLS[estado]   || 'pending';
      const label  = STATUS_LABEL[estado] || p.estado || '—';
      const articulos = (p.detalles || []).map(d => escapeHtml(d.nombre) + (d.cantidad > 1 ? ` ×${d.cantidad}` : '')).join(', ') || '—';
      const fecha = fmtFecha(p.fecha);
      const total = p.total != null ? `€${parseFloat(p.total).toFixed(2)}` : '—';

      // Botón "Validar Pago" solo para pedidos en estado pendiente_bizum
      const validarBtn = estado === 'pendiente_bizum'
        ? `<button
              class="dash-btn dash-btn--sm btn-validar-bizum"
              data-id="${p.idPedido}"
              style="background:#16a34a;color:#fff;border-color:#16a34a;white-space:nowrap"
              title="Confirmar que el Bizum ha sido recibido">
              ✓ Validar Pago
           </button>`
        : '';

      return `<tr data-status="${cls}" data-pedido-id="${p.idPedido}" ${estado === 'pendiente_bizum' ? 'style="background:rgba(22,163,74,0.06)"' : ''}>
        <td><strong>#${p.idPedido}</strong></td>
        <td>${fecha}</td>
        <td>${escapeHtml(p.nombreUsuario || '—')}</td>
        <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="${escapeHtml(articulos)}">${escapeHtml(articulos)}</td>
        <td>${total}</td>
        <td><span class="status status--${cls}">${label}</span></td>
        <td><div class="row-actions">
          <button class="dash-btn dash-btn--outline dash-btn--sm view-order-api" data-id="${p.idPedido}">Ver</button>
          ${validarBtn}
        </div></td>
      </tr>`;
    }).join('');

    // Actualizar badge con pedidos activos (procesando + pendiente + pendiente_bizum)
    const activos = pedidos.filter(p => ['procesando', 'pendiente', 'pendiente_bizum'].includes((p.estado || '').toLowerCase())).length;
    const badge = document.querySelector('.sidebar-link[data-tab="orders"] .badge');
    if (badge) badge.textContent = activos;

    // ── Delegar click en botones "Validar Pago" ─────────────────────────────
    tbody.addEventListener('click', async (e) => {
      const btn = e.target.closest('.btn-validar-bizum');
      if (!btn) return;

      const idPedido = parseInt(btn.dataset.id, 10);
      if (!idPedido) return;

      const confirmar = confirm(`¿Confirmas que has recibido el Bizum del pedido #${idPedido}?\nEsto marcará el pedido como "Procesando" y enviará un email de confirmación al cliente.`);
      if (!confirmar) return;

      btn.disabled    = true;
      btn.textContent = '⏳ Validando…';

      try {
        const res = await fetch(`${INTRANET_API}/intranet/validar-bizum`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ idPedido })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          throw new Error(data.error || `Error ${res.status}`);
        }

        // ── Actualizar fila visualmente sin recargar ──────────────────────
        const fila = tbody.querySelector(`tr[data-pedido-id="${idPedido}"]`);
        if (fila) {
          fila.style.background = '';
          // Actualizar badge de estado
          const statusBadge = fila.querySelector('.status');
          if (statusBadge) {
            statusBadge.className   = 'status status--active';
            statusBadge.textContent = '✓ Procesando';
          }
          // Ocultar botón de validar
          btn.remove();
        }

        if (window.showToast) showToast(`✅ Pago del pedido #${idPedido} validado. Email enviado al cliente.`, 'success');

        // Refrescar badge del sidebar
        const badgeEl = document.querySelector('.sidebar-link[data-tab="orders"] .badge');
        if (badgeEl) {
          const curr = parseInt(badgeEl.textContent, 10);
          if (!isNaN(curr) && curr > 0) badgeEl.textContent = curr - 1;
        }

      } catch (err) {
        btn.disabled    = false;
        btn.textContent = '✓ Validar Pago';
        if (window.showToast) showToast(`Error al validar: ${escapeHtml(err.message)}`, 'error');
        console.error('[ValidarBizum]', err);
      }
    });

  } catch (err) {
    console.error('[loadIntranetOrders]', err);
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--dash-danger)">No se pudieron cargar los pedidos. ${escapeHtml(err.message || 'Comprueba que el servidor está activo.')}</td></tr>`;
  }
};

// ── Lightbox ─────────────────────────────
const lightboxOverlay = document.getElementById('lightboxOverlay');
const lightboxImg     = document.getElementById('lightboxImg');

function openLightbox(src, alt) {
  lightboxImg.src = src;
  lightboxImg.alt = alt;
  lightboxOverlay.classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeLightbox() {
  lightboxOverlay.classList.remove('open');
  document.body.style.overflow = '';
}

document.querySelectorAll('.infographic-img').forEach(img => {
  img.addEventListener('click', () => openLightbox(img.src, img.alt));
});
document.getElementById('lightboxClose').addEventListener('click', closeLightbox);
document.getElementById('lightboxBackdrop').addEventListener('click', closeLightbox);
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeLightbox(); });

// ══════════════════════════════════════════════
//   CATÁLOGO: GESTIÓN DINÁMICA
// ══════════════════════════════════════════════

const PRODUCT_CACHE_KEY_INTRANET = 'shine:productos:v2';

function getHiddenProductIds() {
  try { return JSON.parse(localStorage.getItem('shineHiddenProducts') || '[]'); } catch { return []; }
}

function saveHiddenProductIds(ids) {
  localStorage.setItem('shineHiddenProducts', JSON.stringify(ids));
}

function normalizeCategoryForFilter(catName) {
  const n = (catName || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
  if (n.includes('skin') || n.includes('facial')) return 'skincare';
  if (n.includes('frag') || n.includes('perfume')) return 'fragrance';
  if (n.includes('body') || n.includes('corporal') || n.includes('cuerpo')) return 'body care';
  if (n.includes('gift') || n.includes('regalo') || n.includes('set')) return 'gift sets';
  return n.replace(/\s+/g, '-');
}

function renderCatalogueRow(p, hiddenIds, isAdmin) {
  const id = p.idProducto;
  const isHidden = hiddenIds.includes(id);
  const stock = Number(p.stock || 0);
  const stockMax = 50;
  const stockPct = Math.min(100, Math.round((stock / stockMax) * 100));
  const catName = p.categoria?.nombre || '—';
  const catFilter = normalizeCategoryForFilter(catName);

  let stockCellClass = '';
  let stockFillClass = '';
  if (stock === 0) { stockCellClass = 'stock-cell--empty'; stockFillClass = 'stock-bar__fill--empty'; }
  else if (stock < 15) { stockCellClass = 'stock-cell--low'; stockFillClass = 'stock-bar__fill--low'; }

  const statusHtml = isHidden
    ? '<span class="status status--draft">Oculto</span>'
    : stock === 0
      ? '<span class="status status--expired">Sin Stock</span>'
      : stock < 15
        ? '<span class="status status--pending">Stock Bajo</span>'
        : '<span class="status status--active">Activo</span>';

  const hideBtn = isAdmin
    ? `<button class="dash-btn dash-btn--sm ${isHidden ? 'dash-btn--rose' : 'dash-btn--outline'} btn-toggle-hidden" data-id="${id}" title="${isHidden ? 'Mostrar producto en tienda' : 'Ocultar producto en tienda'}">${isHidden ? 'Mostrar' : 'Ocultar'}</button>`
    : '';

  return `
    <tr data-category="${escapeHtml(catFilter)}" data-id="${id}" style="${isHidden ? 'opacity:.55' : ''}">
      <td><strong>${escapeHtml(p.nombre)}</strong></td>
      <td>${escapeHtml(catName)}</td>
      <td>€${Number(p.precio || 0).toFixed(2)}</td>
      <td>
        <div class="stock-cell ${stockCellClass}">
          <span>${stock}</span>
          <div class="stock-bar"><div class="stock-bar__fill ${stockFillClass}" style="width:${stockPct}%"></div></div>
        </div>
      </td>
      <td>${statusHtml}</td>
      <td>
        <div class="row-actions">
          <button class="dash-btn dash-btn--outline dash-btn--sm btn-edit-catalogue" data-id="${id}">Editar</button>
          ${hideBtn}
        </div>
      </td>
    </tr>
  `;
}

function setupCatalogueRowActions() {
  const isAdmin = window._staffRole === 'admin';

  document.querySelectorAll('.btn-edit-catalogue').forEach(btn => {
    btn.addEventListener('click', () => openCatalogueEditModal(btn.dataset.id));
  });

  if (isAdmin) {
    document.querySelectorAll('.btn-toggle-hidden').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = parseInt(btn.dataset.id);
        const ids = getHiddenProductIds();
        const idx = ids.indexOf(id);
        if (idx > -1) ids.splice(idx, 1); else ids.push(id);
        saveHiddenProductIds(ids);
        localStorage.removeItem(PRODUCT_CACHE_KEY_INTRANET);
        window.loadCatalogueProducts();
        const action = idx > -1 ? 'visible' : 'oculto';
        if (window.showToast) window.showToast(`Producto ${action} en la tienda.`, 'success');
      });
    });
  }
}

async function loadEpSubcategories(catId, selectedSubcatId) {
  const subSelect = document.getElementById('ep-subcategory');
  if (!subSelect) return;
  subSelect.innerHTML = '<option value="">Sin subcategoría</option>';
  subSelect.disabled = true;
  if (!catId) return;
  try {
    const res = await fetch(`${INTRANET_API}/subcategorias?categoriaId=${catId}`);
    if (!res.ok) return;
    const subs = await res.json();
    if (Array.isArray(subs) && subs.length) {
      subs.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.idSubcategoria;
        opt.textContent = s.nombre;
        if (selectedSubcatId && s.idSubcategoria === selectedSubcatId) opt.selected = true;
        subSelect.appendChild(opt);
      });
      subSelect.disabled = false;
    }
  } catch (_) {}
}

async function openCatalogueEditModal(productId) {
  try {
    const res = await fetch(`${INTRANET_API}/productos/${productId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const p = await res.json();

    document.getElementById('ep-id').value = p.idProducto || '';
    document.getElementById('ep-name').value = p.nombre || '';
    document.getElementById('ep-sku').value = p.sku || '';
    document.getElementById('ep-price').value = Number(p.precio || 0).toFixed(2);
    document.getElementById('ep-stock').value = p.stock || 0;
    document.getElementById('ep-desc').value = p.descripcion || '';
    document.getElementById('ep-ingredients').value = p.ingredientes || '';
    document.getElementById('ep-fragancia').value = p.tipoFragancia || '';

    const catId = p.categoria?.idCategoria;
    const catSelect = document.getElementById('ep-category');
    if (catSelect && catId) catSelect.value = catId;

    await loadEpSubcategories(catId, p.idSubcategoria ?? null);

    if (window.openModal) window.openModal('editProductModal');
  } catch (e) {
    if (window.showToast) window.showToast('No se pudo cargar el producto del servidor.', 'error');
  }
}

window.loadCatalogueProducts = async function () {
  const tbody = document.querySelector('#catalogueTable tbody');
  if (!tbody) return;

  const hiddenIds = getHiddenProductIds();
  const isAdmin = window._staffRole === 'admin';

  try {
    const res = await fetch(`${INTRANET_API}/productos`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const products = await res.json();

    if (!Array.isArray(products) || !products.length) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:20px;color:var(--dash-muted)">No hay productos en el catálogo.</td></tr>';
      return;
    }

    tbody.innerHTML = products.map(p => renderCatalogueRow(p, hiddenIds, isAdmin)).join('');
    setupCatalogueRowActions();
    if (typeof filterCatalogue === 'function') filterCatalogue();
  } catch (e) {
    console.error('[loadCatalogueProducts]', e);
    if (window.showToast) window.showToast(`Error al cargar el catálogo: ${e.message}`, 'warn');
  }
};

// ── Wiring: Submit Add Product (modal rápido catálogo) ───
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('submitAddProduct')?.addEventListener('click', async () => {
    const name = document.getElementById('ap-name')?.value.trim();
    const sku = document.getElementById('ap-sku')?.value.trim();
    const catId = parseInt(document.getElementById('ap-category')?.value || '0');
    const price = parseFloat(document.getElementById('ap-price')?.value || '0');
    const stock = parseInt(document.getElementById('ap-stock')?.value || '0');
    const desc = document.getElementById('ap-desc')?.value.trim() || '';
    const ingredients = document.getElementById('ap-ingredients')?.value.trim() || '';

    if (!name || !sku || !catId || !price) {
      if (window.showToast) window.showToast('Completa los campos obligatorios (*)', 'warn');
      return;
    }

    try {
      const res = await fetch(`${INTRANET_API}/productos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: name, sku, idCategoria: catId, precio: price, stock, descripcion: desc, ingredientes: ingredients })
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `HTTP ${res.status}`);
      }

      if (window.closeModal) window.closeModal('addProductModal');
      ['ap-name','ap-sku','ap-price','ap-stock','ap-desc','ap-ingredients'].forEach(id => {
        const el = document.getElementById(id); if (el) el.value = '';
      });
      document.getElementById('ap-category').value = '';

      localStorage.removeItem(PRODUCT_CACHE_KEY_INTRANET);
      if (typeof window.loadCatalogueProducts === 'function') window.loadCatalogueProducts();
      if (window.showToast) window.showToast(`Producto "${name}" añadido al sistema.`, 'success');
    } catch (e) {
      if (window.showToast) window.showToast(`Error: ${e.message}`, 'error');
    }
  });

  // ── Wiring: Category change in Edit modal → reload subcategories ──────────
  document.getElementById('ep-category')?.addEventListener('change', function () {
    loadEpSubcategories(this.value || null, null);
  });

  // ── Wiring: Submit Edit Product ───────────────────────────
  document.getElementById('submitEditProduct')?.addEventListener('click', async () => {
    const id = document.getElementById('ep-id')?.value;
    const name = document.getElementById('ep-name')?.value.trim();
    const sku = document.getElementById('ep-sku')?.value.trim();
    const catId = parseInt(document.getElementById('ep-category')?.value || '0');
    const subcatVal = document.getElementById('ep-subcategory')?.value;
    const price = parseFloat(document.getElementById('ep-price')?.value || '0');
    const stock = parseInt(document.getElementById('ep-stock')?.value || '0');
    const desc = document.getElementById('ep-desc')?.value.trim() || '';
    const ingredients = document.getElementById('ep-ingredients')?.value.trim() || '';
    const fragancia = document.getElementById('ep-fragancia')?.value.trim() || null;

    if (!id || !name || !sku || !catId) {
      if (window.showToast) window.showToast('Completa los campos obligatorios (*)', 'warn');
      return;
    }

    const payload = { nombre: name, sku, idCategoria: catId, precio: price, stock, descripcion: desc, ingredientes: ingredients, tipoFragancia: fragancia || null };
    if (subcatVal) payload.idSubcategoria = parseInt(subcatVal);

    try {
      const res = await fetch(`${INTRANET_API}/productos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `HTTP ${res.status}`);
      }

      if (window.closeModal) window.closeModal('editProductModal');
      localStorage.removeItem(PRODUCT_CACHE_KEY_INTRANET);
      if (typeof window.loadCatalogueProducts === 'function') window.loadCatalogueProducts();
      if (window.showToast) window.showToast(`Producto "${name}" actualizado correctamente.`, 'success');
    } catch (e) {
      if (window.showToast) window.showToast(`Error al actualizar: ${e.message}`, 'error');
    }
  });
});

// ══════════════════════════════════════════════
//   EQUIPO — carga dinámica desde API
// ══════════════════════════════════════════════

const TEAM_AVATAR_COLORS = ['rose','blue','green','amber','teal','orange','purple'];

function staffInitials(nombre) {
  const parts = (nombre || '').trim().split(/\s+/);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return (nombre || 'US').substring(0, 2).toUpperCase();
}

window.loadTeamMembers = async function () {
  const grid = document.getElementById('teamGrid');
  const countEl = document.getElementById('teamCount');
  if (!grid) return;

  grid.innerHTML = '<p style="color:var(--dash-muted);padding:20px;grid-column:1/-1;text-align:center">Cargando equipo…</p>';

  try {
    const staff = await fetch(`${INTRANET_API}/usuarios`).then(r => r.json());
    if (!Array.isArray(staff) || !staff.length) {
      grid.innerHTML = '<p style="color:var(--dash-muted);padding:20px;grid-column:1/-1;text-align:center">No hay miembros del equipo registrados.</p>';
      if (countEl) countEl.textContent = '';
      return;
    }

    if (countEl) countEl.textContent = `(${staff.length})`;

    grid.innerHTML = staff.map((u, i) => {
      const color = TEAM_AVATAR_COLORS[i % TEAM_AVATAR_COLORS.length];
      const initials = staffInitials(u.nombre);
      const isAdmin = u.rol === 'admin';
      const roleLabel = isAdmin ? 'Administrador' : 'Empleado';
      return `
        <div class="team-card" data-name="${escapeHtml((u.nombre || '').toLowerCase())}" data-user-id="${u.id}">
          <div class="team-card__avatar avatar--${color}">${initials}</div>
          <div class="team-card__name">${escapeHtml(u.nombre || '—')}</div>
          <div class="team-card__role">${roleLabel}</div>
          <div class="team-card__email">${escapeHtml(u.email || '—')}</div>
          <span class="status status--active">Activo</span>
          <div class="team-card__actions">
            <button class="dash-btn dash-btn--outline dash-btn--sm" onclick="openChat('${escapeHtml(u.nombre || '')}')">Mensaje</button>
          </div>
        </div>`;
    }).join('');

    // Re-apply active search
    const q = document.getElementById('teamSearch')?.value.toLowerCase() || '';
    if (q) {
      grid.querySelectorAll('.team-card').forEach(card => {
        card.style.display = card.dataset.name.includes(q) ? '' : 'none';
      });
    }
  } catch (e) {
    grid.innerHTML = '<p style="color:var(--dash-danger);padding:20px;grid-column:1/-1;text-align:center">Error al cargar el equipo.</p>';
    console.error('[loadTeamMembers]', e);
  }
};

// ══════════════════════════════════════════════
//   ROLES ADMIN — carga dinámica desde API
// ══════════════════════════════════════════════

window.loadRolesTable = async function () {
  const tbody = document.getElementById('rolesTableBody');
  if (!tbody) return;

  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:24px;color:var(--dash-muted)">Cargando usuarios…</td></tr>';

  let currentEmail = '';
  try { currentEmail = (JSON.parse(localStorage.getItem('shineStaff')) || {}).email || ''; } catch {}

  try {
    const staff = await fetch(`${INTRANET_API}/usuarios`).then(r => r.json());
    if (!Array.isArray(staff) || !staff.length) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:24px;color:var(--dash-muted)">No hay empleados registrados.</td></tr>';
      return;
    }

    tbody.innerHTML = staff.map((u, i) => {
      const color = TEAM_AVATAR_COLORS[i % TEAM_AVATAR_COLORS.length];
      const initials = staffInitials(u.nombre);
      const isCurrentUser = u.email === currentEmail;
      const isAdmin = u.rol === 'admin';

      const roleBadge = isCurrentUser
        ? `<span class="status status--active">${isAdmin ? 'Admin' : 'Empleado'} (tú)</span>`
        : `<select class="dash-input role-select" style="width:auto;padding:4px 8px;font-size:0.78rem">
             <option value="empleado"${u.rol === 'empleado' ? ' selected' : ''}>Empleado</option>
             <option value="admin"${isAdmin ? ' selected' : ''}>Admin</option>
           </select>`;

      const actionBtns = isCurrentUser
        ? '<button class="dash-btn dash-btn--outline dash-btn--sm" disabled>Cuenta actual</button>'
        : `<button class="dash-btn dash-btn--outline dash-btn--sm btn-save-role">Guardar</button>
           <button class="dash-btn dash-btn--danger dash-btn--sm btn-delete-account">Eliminar</button>`;

      return `<tr data-user-id="${u.id}" data-nombre="${escapeHtml(u.nombre || '')}">
        <td><div style="display:flex;align-items:center;gap:10px">
          <div class="avatar avatar--${color}" style="width:32px;height:32px;font-size:0.65rem">${initials}</div>
          <strong>${escapeHtml(u.nombre || '—')}</strong>
        </div></td>
        <td>${escapeHtml(u.email || '—')}</td>
        <td>${roleBadge}</td>
        <td><span class="status status--active">Activa</span></td>
        <td><div class="row-actions">${actionBtns}</div></td>
      </tr>`;
    }).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:24px;color:var(--dash-danger)">Error al cargar usuarios: ${escapeHtml(e.message)}</td></tr>`;
    console.error('[loadRolesTable]', e);
  }
};

// ══════════════════════════════════════════════
//   DASHBOARD KPIs — datos reales desde API
// ══════════════════════════════════════════════

window.updateDashboardKPIs = async function () {
  try {
    const s = await fetch(`${INTRANET_API}/intranet/dashboard`).then(r => r.json());
    if (!s || typeof s !== 'object') return;

    // ── KPI cards ────────────────────────────────────────────────────────────
    const kpiRev    = document.getElementById('kpiRevenue');
    const kpiOrd    = document.getElementById('kpiOrders');
    const kpiCust   = document.getElementById('kpiCustomers');
    const kpiActive = document.getElementById('kpiActiveOrders');
    const pillPend  = document.getElementById('pillPendingOrders');
    const badge     = document.querySelector('.sidebar-link[data-tab="orders"] .badge');

    if (kpiRev)    animateKPIValue(kpiRev,    Math.round(s.monthRevenue), '€', '');
    if (kpiOrd)    animateKPIValue(kpiOrd,    s.totalOrders, '', '');
    if (kpiCust)   animateKPIValue(kpiCust,   s.totalCustomers, '', '');
    if (kpiActive) animateKPIValue(kpiActive, s.activeOrders, '', '');
    if (pillPend)  pillPend.textContent = s.activeOrders;
    if (badge)     badge.textContent   = s.activeOrders;

    // ── Home stats strip ─────────────────────────────────────────────────────
    const statRev    = document.getElementById('statRevenue');
    const statOrders = document.getElementById('statActiveOrders');
    if (statRev)    animateKPIValue(statRev,    Math.round(s.monthRevenue), '', ' €');
    if (statOrders) animateKPIValue(statOrders, s.activeOrders, '', '');

    // ── KPI change indicators ────────────────────────────────────────────────
    const revChange = document.getElementById('kpiRevenueChange');
    if (revChange && s.lastMonthRevenue > 0) {
      const pct = ((s.monthRevenue - s.lastMonthRevenue) / s.lastMonthRevenue * 100).toFixed(1);
      const up  = pct >= 0;
      revChange.className = 'kpi-card__change' + (up ? ' up' : ' down');
      revChange.textContent = (up ? '↑' : '↓') + ' ' + Math.abs(pct) + '% vs mes anterior';
    } else if (revChange) {
      revChange.textContent = 'Mes actual';
    }

    const ordChange = document.getElementById('kpiOrdersChange');
    if (ordChange) ordChange.textContent = s.activeOrders + ' activos';

    const custChange = document.getElementById('kpiCustomersChange');
    if (custChange) custChange.textContent = s.totalCustomers + ' registrados';

    const activeChange = document.getElementById('kpiActiveOrdersChange');
    if (activeChange) activeChange.textContent = s.activeOrders > 0 ? s.activeOrders + ' pendientes' : 'Al día';

    // ── Revenue chart ────────────────────────────────────────────────────────
    if (window.revChart && s.revenueWeek && s.revenueMonth && s.revenueYear) {
      window._dashRevData = {
        week:  { labels: s.revenueWeek.map(p => p.label),  data: s.revenueWeek.map(p => p.value)  },
        month: { labels: s.revenueMonth.map(p => p.label), data: s.revenueMonth.map(p => p.value) },
        year:  { labels: s.revenueYear.map(p => p.label),  data: s.revenueYear.map(p => p.value)  }
      };
      const active = document.querySelector('.period-toggle button.active')?.dataset?.period || 'week';
      const d = window._dashRevData[active];
      revChart.data.labels = d.labels;
      revChart.data.datasets[0].data = d.data;
      revChart.update();
    }

    // ── Pedidos recientes ────────────────────────────────────────────────────
    const tbody = document.getElementById('dashRecentOrdersBody');
    if (tbody && Array.isArray(s.recentOrders)) {
      const statusMap   = { procesando:'status--pending', pendiente:'status--pending', enviado:'status--shipped', entregado:'status--active', cancelado:'status--danger' };
      const statusLabel = { procesando:'Procesando', pendiente:'Pendiente', enviado:'Enviado', entregado:'Entregado', cancelado:'Cancelado' };
      if (s.recentOrders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;color:var(--dash-muted)">Sin pedidos aún</td></tr>';
      } else {
        tbody.innerHTML = s.recentOrders.map(o => {
          const estado = (o.estado || '').toLowerCase();
          const name   = (o.customerName || '').split(' ')[0] + (o.customerName?.split(' ')[1] ? ' ' + o.customerName.split(' ')[1][0] + '.' : '');
          return `<tr>
            <td>#SH-${o.id}</td>
            <td>${escapeHtml(name)}</td>
            <td>€${parseFloat(o.total).toFixed(2)}</td>
            <td><span class="status ${statusMap[estado] || 'status--pending'}">${statusLabel[estado] || o.estado}</span></td>
          </tr>`;
        }).join('');
      }
    }

    // ── Mejores productos ────────────────────────────────────────────────────
    const topEl = document.getElementById('dashTopProducts');
    if (topEl && Array.isArray(s.topProducts)) {
      const ranks = ['gold', 'silver', 'bronze', '', ''];
      if (s.topProducts.length === 0) {
        topEl.innerHTML = '<div style="padding:20px;text-align:center;color:var(--dash-muted)">Sin ventas registradas</div>';
      } else {
        const maxRev = s.topProducts[0]?.revenue || 1;
        topEl.innerHTML = s.topProducts.map((p, i) => {
          const pct = Math.round((p.revenue / maxRev) * 100);
          return `<div class="top-product">
            <div class="top-product__rank ${ranks[i] || ''}">${i + 1}</div>
            <div class="top-product__info">
              <span>${escapeHtml(p.nombre)}</span>
              <div class="top-product__bar"><div style="width:${pct}%"></div></div>
            </div>
            <span class="top-product__val">€${Math.round(p.revenue).toLocaleString('es-ES')}</span>
          </div>`;
        }).join('');
      }
    }

    // Sync notification dropdown with low stock and recent orders
    syncNotifications(s);

  } catch (e) {
    console.warn('[updateDashboardKPIs]', e);
  }
};

// ── Home stats: miembros totales ─────────────────────────────────────────────
async function loadStatMembers() {
  try {
    const staff = await fetch(`${INTRANET_API}/usuarios`).then(r => r.json());
    const el = document.getElementById('statMembers');
    if (el && Array.isArray(staff)) el.textContent = staff.length;
  } catch {}
}

// ══════════════════════════════════════════════
//   FEED DE ACTIVIDAD RECIENTE (escalable)
//   Consume GET /api/v1/intranet/actividad
//   Para añadir nuevos tipos de evento, solo
//   ampliar ActividadDAO.java en el backend.
// ══════════════════════════════════════════════

const ACTIVITY_ICONS = {
  order:      '<path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/>',
  clock:      '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  'clock-out':'<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/><line x1="15" y1="3" x2="19" y2="7"/>',
  megaphone:  '<path d="M22 3L9.218 10.083"/><path d="M11.038 14.962L9 22l2-4 2 4-2.038-7.038z"/><path d="M2 8.5h7l-2 7H2z"/><path d="M22 3L11 14.5"/>',
  inbox:      '<polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/>',
  default:    '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>'
};

window.loadActivityFeed = async function () {
  const actList  = document.getElementById('dashActivityList');
  const actCount = document.getElementById('dashActivityCount');
  if (!actList) return;

  try {
    const items = await fetch(`${INTRANET_API}/intranet/actividad`).then(r => r.json());

    if (actCount) actCount.textContent = Array.isArray(items) ? Math.min(items.length, 20) : 0;

    if (!Array.isArray(items) || !items.length) {
      actList.innerHTML = '<div class="timeline-item"><div class="timeline-dot" style="background:var(--dash-muted)"></div><div><p style="color:var(--dash-muted)">Sin actividad reciente</p></div></div>';
      return;
    }

    actList.innerHTML = items.map(it => {
      const ts  = it.timestamp ? new Date(it.timestamp) : null;
      const sub = ts ? relativeTime(ts) : '';
      const svg = ACTIVITY_ICONS[it.icono] || ACTIVITY_ICONS.default;
      return `<div class="timeline-item">
        <div class="timeline-dot" style="background:${escapeHtml(it.color || 'var(--dash-muted)')}">
          <svg viewBox="0 0 24 24" style="width:10px;height:10px;stroke:#fff;fill:none;stroke-width:2">${svg}</svg>
        </div>
        <div>
          <p>${escapeHtml(it.descripcion || '')}</p>
          <small style="color:var(--dash-muted)">${escapeHtml(sub)}</small>
        </div>
      </div>`;
    }).join('');
  } catch (e) {
    console.warn('[loadActivityFeed]', e);
    actList.innerHTML = '<div class="timeline-item"><div class="timeline-dot" style="background:var(--dash-muted)"></div><div><p style="color:var(--dash-muted)">No se pudo cargar la actividad</p></div></div>';
  }
};

function relativeTime(date) {
  const diff = Date.now() - date.getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1)   return 'Ahora mismo';
  if (mins < 60)  return `hace ${mins} min`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)   return `hace ${hrs} hora${hrs > 1 ? 's' : ''}`;
  const days = Math.floor(hrs / 24);
  if (days === 1) return 'Ayer';
  return `hace ${days} días`;
}

function syncNotifications(stats) {
  const list = document.querySelector('.notif-list');
  if (!list) return;

  const entries = [];

  (stats.recentOrders || []).slice(0, 3).forEach(o => {
    entries.push({
      ts: o.fecha ? new Date(o.fecha) : new Date(0),
      html: `<div class="notif-item notif-item--unread">
        <div class="notif-dot" style="background:var(--dash-success)"></div>
        <div><p>Nuevo pedido <strong>#${o.id}</strong> de ${escapeHtml(o.customerName || '')}</p>
        <small>${o.fecha ? relativeTime(new Date(o.fecha)) : ''}</small></div>
      </div>`
    });
  });

  (stats.lowStockProducts || []).forEach(p => {
    entries.push({
      ts: new Date(),
      html: `<div class="notif-item notif-item--unread">
        <div class="notif-dot" style="background:var(--dash-warning)"></div>
        <div><p><strong>${escapeHtml(p.nombre)}</strong> con stock bajo (${p.stock} unidades)</p>
        <small>Ahora</small></div>
      </div>`
    });
  });

  (stats.recentAnuncios || []).forEach(a => {
    entries.push({
      ts: a.fecha ? new Date(a.fecha) : new Date(0),
      html: `<div class="notif-item notif-item--unread">
        <div class="notif-dot" style="background:var(--dash-rose)"></div>
        <div><p>Nuevo anuncio: <strong>${escapeHtml(a.titulo)}</strong></p>
        <small>${a.fecha ? relativeTime(new Date(a.fecha)) : ''}</small></div>
      </div>`
    });
  });

  entries.sort((a, b) => b.ts - a.ts);

  if (entries.length === 0) {
    list.innerHTML = `<div class="notif-item"><div class="notif-dot"></div><div><p style="color:var(--dash-muted);font-size:.8rem">Sin notificaciones nuevas</p></div></div>`;
    const cnt = document.getElementById('notifCount');
    if (cnt) { cnt.textContent = '0'; cnt.style.display = 'none'; }
    return;
  }

  list.innerHTML = entries.map(e => e.html).join('');
  const cnt = document.getElementById('notifCount');
  if (cnt) { cnt.textContent = entries.length; cnt.style.display = ''; }
}

// ══════════════════════════════════════════════
//   HOME STATS PILLS — datos reales
// ══════════════════════════════════════════════

async function updateHomeStats() {
  // Reuniones hoy
  try {
    const meetings = await fetch(`${INTRANET_API}/reuniones`).then(r => r.json());
    const today = new Date().toISOString().split('T')[0];
    const todayCount = (meetings || []).filter(m => (m.fecha || '') === today).length;
    const pill = document.getElementById('pillMeetingsToday');
    if (pill) pill.textContent = todayCount;
  } catch {}

  // Mensajes en el buzón
  try {
    const msgs = await fetch(`${INTRANET_API}/buzon`).then(r => r.json());
    const pill = document.getElementById('pillBuzonCount');
    if (pill) pill.textContent = (msgs || []).length;
  } catch {}

  // Pedidos pendientes (reutiliza la llamada de KPIs para admin)
  if (window._staffRole === 'admin') {
    try {
      const pedidos = await fetch(`${INTRANET_API}/intranet/pedidos`).then(r => r.json());
      const pending = (pedidos || []).filter(p => ['procesando', 'pendiente'].includes((p.estado || '').toLowerCase())).length;
      const pill = document.getElementById('pillPendingOrders');
      if (pill) pill.textContent = pending;
    } catch {}
  }
}

// ── Inicialización al cargar la página ────────
document.addEventListener('DOMContentLoaded', () => {
  updateHomeStats();
  // Pre-cargar equipo al inicio
  if (typeof window.loadTeamMembers === 'function') window.loadTeamMembers();
  // Pre-cargar roles, KPIs y feed de actividad si es admin
  // Espera 300ms para que el role system termine de leer localStorage
  loadStatMembers();
  setTimeout(() => {
    if (window._staffRole === 'admin') {
      if (typeof window.loadRolesTable === 'function')      window.loadRolesTable();
      if (typeof window.updateDashboardKPIs === 'function') window.updateDashboardKPIs();
      if (typeof window.loadActivityFeed === 'function')    window.loadActivityFeed();
      if (typeof window.loadIntranetOrders === 'function')  window.loadIntranetOrders();
    }
  }, 300);

  // Auto-refresh del feed de actividad cada 60 segundos
  setInterval(() => {
    if (window._staffRole === 'admin') {
      if (typeof window.loadActivityFeed === 'function')    window.loadActivityFeed();
      if (typeof window.updateDashboardKPIs === 'function') window.updateDashboardKPIs();
    }
  }, 60000);
});
