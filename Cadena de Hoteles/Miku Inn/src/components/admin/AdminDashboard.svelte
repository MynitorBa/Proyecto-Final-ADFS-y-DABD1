<script>
  import { onMount } from 'svelte';

  export let API_BASE;
  export let badge;
  export let setSection;

  let metricas = null;
  let cargandoMetricas = false;

  let reservas = [];
  let cargandoReservas = false;

  let hoteles = [];
  let usuarios = [];

  $: statsData = metricas ? [
    { label: 'Usuarios Totales',     value: metricas.totalUsuarios.toLocaleString('es-GT'),    icon: 'users',    color: 'blue'   },
    { label: 'Reservas Confirmadas', value: metricas.reservasActivas.toLocaleString('es-GT'),  icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',      value: metricas.hotelesActivos.toLocaleString('es-GT'),   icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos Totales',     value: '$ ' + (metricas.ingresosTotales ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 }), icon: 'money', color: 'amber' },
  ] : [
    { label: 'Usuarios Totales',     value: '—', icon: 'users',    color: 'blue'   },
    { label: 'Reservas Confirmadas', value: '—', icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',      value: '—', icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos Totales',     value: '—', icon: 'money',    color: 'amber'  },
  ];

  onMount(() => {
    cargarMetricas();
    cargarReservas();
    cargarHoteles();
    cargarUsuarios();
  });

  async function cargarMetricas() {
    cargandoMetricas = true;
    try {
      const res = await fetch(`${API_BASE}/admin/metricas`, { credentials: 'include' });
      if (res.ok) metricas = await res.json();
    } catch (e) { /* silencioso */ }
    finally { cargandoMetricas = false; }
  }

  async function cargarReservas() {
    cargandoReservas = true;
    try {
      const res = await fetch(`${API_BASE}/admin/reservaciones`, { credentials: 'include' });
      if (res.ok) reservas = await res.json();
    } catch (e) { /* silencioso */ }
    finally { cargandoReservas = false; }
  }

  async function cargarHoteles() {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { credentials: 'include' });
      if (res.ok) hoteles = await res.json();
    } catch (e) { /* silencioso */ }
  }

  async function cargarUsuarios() {
    try {
      const res = await fetch(`${API_BASE}/admin/usuarios`, { credentials: 'include' });
      if (res.ok) usuarios = await res.json();
    } catch (e) { /* silencioso */ }
  }
</script>

<!-- Stats cards -->
<div class="adm__stats-grid">
  {#each statsData as stat}
    <div class="adm__stat-card adm__stat-card--{stat.color}">
      <div class="adm__stat-top">
        <div class="adm__stat-icon-wrap">
          {#if stat.icon === 'users'}
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          {:else if stat.icon === 'calendar'}
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          {:else if stat.icon === 'hotel'}
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
          {:else}
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          {/if}
        </div>
        <span class="adm__stat-change"></span>
      </div>
      <p class="adm__stat-value">{stat.value}</p>
      <p class="adm__stat-label">{stat.label}</p>
    </div>
  {/each}
</div>

<!-- Grid: Últimas Reservas + Hoteles -->
<div class="adm__dashboard-grid">
  <div class="adm__card">
    <div class="adm__card-header">
      <h3 class="adm__card-title">Últimas Reservas</h3>
      <button class="adm__link-btn" on:click={() => setSection('reservas')}>Ver todas →</button>
    </div>
    <div class="adm__table-wrap">
      <table class="adm__table">
        <thead><tr><th>ID</th><th>Usuario</th><th>Hotel</th><th>Estado</th></tr></thead>
        <tbody>
          {#if cargandoReservas}
            <tr><td colspan="4" style="text-align:center; color:var(--adm-text-muted); padding:1.5rem">Cargando reservas...</td></tr>
          {:else if reservas.length === 0}
            <tr><td colspan="4" class="adm__empty-cell">Sin reservas registradas</td></tr>
          {:else}
            {#each reservas.slice(0, 5) as r}
              <tr>
                <td class="adm__table-mono">{r.noReservacion}</td>
                <td>{r.usuario}</td>
                <td>{r.hotel}</td>
                <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
              </tr>
            {/each}
          {/if}
        </tbody>
      </table>
    </div>
  </div>

  <div class="adm__card">
    <div class="adm__card-header">
      <h3 class="adm__card-title">Hoteles</h3>
      <button class="adm__link-btn" on:click={() => setSection('hoteles')}>Ver todos →</button>
    </div>
    <div class="adm__hotel-bars">
      {#each hoteles.slice(0, 5) as h}
        <div class="adm__hotel-bar-item">
          <div class="adm__hotel-bar-info">
            <span class="adm__hotel-bar-name">{h.nombre}</span>
            <span class="adm__hotel-bar-pct">{h.ciudad}</span>
          </div>
          <div class="adm__hotel-bar-track">
            <div class="adm__hotel-bar-fill" style="width: {Math.round((h.rating / 5) * 100)}%"></div>
          </div>
        </div>
      {/each}
      {#if hoteles.length === 0}
        <p style="color: var(--adm-text-muted); font-size: .85rem; text-align:center; padding: 1rem 0;">Sin datos aún</p>
      {/if}
    </div>
  </div>
</div>

<!-- Usuarios recientes -->
<div class="adm__card">
  <div class="adm__card-header">
    <h3 class="adm__card-title">Usuarios Recientes</h3>
    <button class="adm__link-btn" on:click={() => setSection('usuarios')}>Ver todos →</button>
  </div>
  <div class="adm__table-wrap">
    <table class="adm__table">
      <thead><tr><th>Nombre</th><th>Username</th><th>País</th><th>Rol</th><th>Ciudad</th></tr></thead>
      <tbody>
        {#each usuarios.slice(0, 5) as u}
          <tr>
            <td>
              <div class="adm__user-mini">
                <div class="adm__user-mini-avatar" style="background: {u.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : u.rolId === 3 ? 'linear-gradient(135deg,#8b5cf6,#6d28d9)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
                  {u.nombre.charAt(0)}
                </div>
                {u.nombre} {u.apellido}
              </div>
            </td>
            <td class="adm__table-mono">@{u.username}</td>
            <td>{u.pais ?? '—'}</td>
            <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : u.rolId === 3 ? 'badge--purple' : 'badge--blue'}">{u.rolNombre}</span></td>
            <td>{u.ciudad ?? '—'}</td>
          </tr>
        {/each}
        {#if usuarios.length === 0}
          <tr><td colspan="5" class="adm__empty-cell">Sin usuarios</td></tr>
        {/if}
      </tbody>
    </table>
  </div>
</div>