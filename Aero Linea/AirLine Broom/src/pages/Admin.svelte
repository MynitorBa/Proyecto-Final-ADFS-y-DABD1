<script>
// @ts-nocheck
  import '../styles/admin.css';
  import { onMount, onDestroy } from 'svelte';
  import { sesion } from '../stores/sesion.js';

  import AdminCrearVuelo   from '../components/admin/AdminCrearVuelo.svelte';
  import AdminRutas        from '../components/admin/AdminRutas.svelte';
  import AdminAviones      from '../components/admin/AdminAviones.svelte';
  import AdminTripulantes  from '../components/admin/AdminTripulantes.svelte';
  import AdminAeropuertos  from '../components/admin/AdminAeropuertos.svelte';
  import AdminHistorial    from '../components/admin/AdminHistorial.svelte';
  import AdminUsuarios     from '../components/admin/AdminUsuarios.svelte';
  import AdminMetricas     from '../components/admin/AdminMetricas.svelte';
  import AdminAgencias     from '../components/admin/AdminAgencias.svelte';

  export let navigateTo = (page, data = null) => {};

  import { API } from '../lib/api.js';

  // ── Sesión ───────────────────────────────────────────────────────
  let rolNombre = null;
  const unsubscribeSesion = sesion.subscribe(s => { rolNombre = s?.rolNombre ?? null; });

  // ── Navegación ───────────────────────────────────────────────────
  let activeSection = 'crear-vuelo';

  // ── Datos compartidos (los módulos que crean vuelos los necesitan) ─
  // AdminCrearVuelo los recibe como props para sus dropdowns.
  // Se recargan cuando los módulos hijos emiten eventos de actualización.
  let aeropuertos = [];
  let aviones     = [];
  let tripulantes = [];

  // ── Referencia al historial para recargarlo tras crear un vuelo ──
  let historialRef;

  // ── Sistema de toasts ────────────────────────────────────────────
  let toasts  = [];
  let toastId = 0;

  function mostrarToast(tipo, mensaje, duracion = 4000) {
    const id = ++toastId;
    toasts = [...toasts, { id, tipo, mensaje }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, duracion);
  }

  function cerrarToast(id) { toasts = toasts.filter(t => t.id !== id); }

  // ── Modal de confirmación ────────────────────────────────────────
  let confirmVisible  = false;
  let confirmMensaje  = '';
  let confirmSubtexto = '';
  let confirmTipo     = 'danger';
  let confirmResolve  = null;

  function mostrarConfirm(mensaje, subtexto = '', tipo = 'danger') {
    confirmMensaje  = mensaje;
    confirmSubtexto = subtexto;
    confirmTipo     = tipo;
    confirmVisible  = true;
    return new Promise(resolve => { confirmResolve = resolve; });
  }

  function confirmarAccion() {
    confirmVisible = false;
    if (confirmResolve) confirmResolve(true);
    confirmResolve = null;
  }

  function cancelarConfirm() {
    confirmVisible = false;
    if (confirmResolve) confirmResolve(false);
    confirmResolve = null;
  }

  // ── Carga de datos compartidos ───────────────────────────────────
  async function cargarAeropuertos() {
    try {
      const r = await fetch(`${API}/api/aeropuertos`);
      if (r.ok) aeropuertos = await r.json();
    } catch { console.error('Error cargando aeropuertos'); }
  }

  async function cargarAviones() {
    try {
      const r = await fetch(`${API}/api/aviones`);
      if (r.ok) aviones = await r.json();
    } catch { console.error('Error cargando aviones'); }
  }

  async function cargarTripulantes() {
    try {
      const r = await fetch(`${API}/api/tripulacion`);
      if (r.ok) tripulantes = await r.json();
    } catch { console.error('Error cargando tripulantes'); }
  }

  async function cargarDatosCompartidos() {
    await Promise.all([cargarAeropuertos(), cargarAviones(), cargarTripulantes()]);
  }

  // ── Eventos de los módulos hijos ─────────────────────────────────
  function onVueloCreado() {
    // Cambia a historial y lo recarga (AdminHistorial se auto-recarga en onMount,
    // pero si ya estaba montado necesitamos un mecanismo manual)
    activeSection = 'historial';
  }

  function onAvionesActualizados()     { cargarAviones(); }
  function onTripulantesActualizados() { cargarTripulantes(); }
  function onAeropuertosActualizados() { cargarAeropuertos(); }

  // ── Nav items ────────────────────────────────────────────────────
  const navItems = [
    { id: 'crear-vuelo',          label: 'Crear Vuelo',         icon: 'M12 4v16m8-8H4' },
    { id: 'gestionar-rutas',      label: 'Gestionar Rutas',     icon: 'M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7' },
    { id: 'gestionar-aviones',    label: 'Gestionar Aviones',   icon: 'M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064' },
    { id: 'gestionar-tripulantes',label: 'Tripulantes',          icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'gestionar-aeropuertos',label: 'Aeropuertos',          icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    { id: 'historial',            label: 'Historial',            icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { id: 'usuarios',             label: 'Usuarios',             icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'metricas',             label: 'Métricas',             icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
    { id: 'agencias',             label: 'Agencias',             icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-2 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4' },
  ];

  // ── Lifecycle ────────────────────────────────────────────────────
  onMount(async () => {
    if (rolNombre !== 'Administrador') { navigateTo('acceso-denegado'); return; }
    await cargarDatosCompartidos();
  });

  onDestroy(() => { unsubscribeSesion(); });
</script>

<!-- ── Toasts ─────────────────────────────────────────────────────── -->
<div class="toast-stack" aria-live="polite">
  {#each toasts as toast (toast.id)}
    <div class="toast toast--{toast.tipo}" role="alert">
      <span class="toast__icon">
        {#if toast.tipo === 'success'}✓{:else if toast.tipo === 'error'}✕{:else}⚠{/if}
      </span>
      <span class="toast__msg">{toast.mensaje}</span>
      <button class="toast__close" on:click={() => cerrarToast(toast.id)} aria-label="Cerrar">×</button>
    </div>
  {/each}
</div>

<!-- ── Modal confirmación ─────────────────────────────────────────── -->
{#if confirmVisible}
  <div class="modal-overlay" on:click={cancelarConfirm} role="dialog" aria-modal="true">
    <div class="confirm-dialog" on:click|stopPropagation>
      <div class="confirm-dialog__icon confirm-dialog__icon--{confirmTipo}">
        {#if confirmTipo === 'danger'}
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
          </svg>
        {:else}
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
          </svg>
        {/if}
      </div>
      <h3 class="confirm-dialog__title">{confirmMensaje}</h3>
      {#if confirmSubtexto}
        <p class="confirm-dialog__sub">{confirmSubtexto}</p>
      {/if}
      <div class="confirm-dialog__actions">
        <button class="confirm-dialog__btn confirm-dialog__btn--cancel" on:click={cancelarConfirm}>
          No, cancelar
        </button>
        <button class="confirm-dialog__btn confirm-dialog__btn--{confirmTipo}" on:click={confirmarAccion}>
          Sí, confirmar
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- ── Panel principal ────────────────────────────────────────────── -->
<div class="admin">
  <div class="admin__container">

    <div class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>← Salir del Panel</button>
      <h1 class="admin__title">Panel de Administración</h1>
      <p class="admin__subtitle">Gestión de vuelos, usuarios y métricas</p>
    </div>

    <div class="admin__content">

      <!-- ── Sidebar ── -->
      <aside class="admin__sidebar">
        <nav class="admin-nav">
          {#each navItems as item}
            <button
              class="admin-nav__item"
              class:admin-nav__item--active={activeSection === item.id}
              on:click={() => activeSection = item.id}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d={item.icon} />
              </svg>
              {item.label}
            </button>
          {/each}
        </nav>
        <div class="admin__sidebar-footer" style="padding:1rem">
          <button class="admin__back" on:click={() => navigateTo('home')}>
            ← Volver al sitio
          </button>
        </div>
      </aside>

      <!-- ── Contenido principal ── -->
      <main class="admin__main">

        <!-- Topbar -->
        <header style="margin-bottom:2rem">
          <h2 style="font-size:1.4rem;color:var(--secondary-color);margin:0 0 .25rem;font-weight:600">
            {#if activeSection === 'crear-vuelo'}Crear Nuevo Vuelo
            {:else if activeSection === 'gestionar-rutas'}Gestionar Rutas
            {:else if activeSection === 'gestionar-aviones'}Gestionar Aviones
            {:else if activeSection === 'gestionar-tripulantes'}Gestionar Tripulantes
            {:else if activeSection === 'gestionar-aeropuertos'}Gestionar Aeropuertos
            {:else if activeSection === 'historial'}Historial de Vuelos
            {:else if activeSection === 'usuarios'}Usuarios
            {:else if activeSection === 'metricas'}Métricas y Analíticos
            {:else if activeSection === 'agencias'}Agencias
            {/if}
          </h2>
          <p style="font-size:.8rem;color:var(--text-muted);margin:0">
            Panel de Administración · Broom AirLine ·
            {new Date().toLocaleDateString('es-GT', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </header>

        <!-- ── Módulos ── -->
        {#if activeSection === 'crear-vuelo'}
          <AdminCrearVuelo
            {API}
            {aeropuertos}
            {aviones}
            {tripulantes}
            {mostrarToast}
            {mostrarConfirm}
            on:vueloCreado={onVueloCreado}
            on:irARutas={() => activeSection = 'gestionar-rutas'}
          />

        {:else if activeSection === 'gestionar-rutas'}
          <AdminRutas
            {API}
            {aeropuertos}
            {mostrarToast}
            on:rutaCreada={() => {}}
          />

        {:else if activeSection === 'gestionar-aviones'}
          <AdminAviones
            {API}
            {mostrarToast}
            {mostrarConfirm}
            on:avionesActualizados={onAvionesActualizados}
          />

        {:else if activeSection === 'gestionar-tripulantes'}
          <AdminTripulantes
            {API}
            {mostrarToast}
            {mostrarConfirm}
            on:tripulantesActualizados={onTripulantesActualizados}
          />

        {:else if activeSection === 'gestionar-aeropuertos'}
          <AdminAeropuertos
            {API}
            {mostrarToast}
            {mostrarConfirm}
            on:aeropuertosActualizados={onAeropuertosActualizados}
          />

        {:else if activeSection === 'historial'}
          <AdminHistorial
            {API}
            {mostrarToast}
            {mostrarConfirm}
            on:vueloCancelado={() => {}}
          />

        {:else if activeSection === 'usuarios'}
          <AdminUsuarios
            {API}
            {mostrarToast}
          />

        {:else if activeSection === 'metricas'}
          <AdminMetricas
            {API}
            {mostrarToast}
          />

        {:else if activeSection === 'agencias'}
          <AdminAgencias
            {API}
            {mostrarToast}
            {mostrarConfirm}
          />

        {/if}

      </main>
    </div>
  </div>
</div>