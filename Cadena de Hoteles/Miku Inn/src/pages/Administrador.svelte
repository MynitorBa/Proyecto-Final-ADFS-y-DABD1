<script>
  import '../styles/administrador.css';
  import { onMount } from 'svelte';

  import AdminDashboard  from '../components/admin/AdminDashboard.svelte';
  import AdminUsuarios   from '../components/admin/AdminUsuarios.svelte';
  import AdminHoteles    from '../components/admin/AdminHoteles.svelte';
  import AdminCrearHotel from '../components/admin/AdminCrearHotel.svelte';
  import AdminReservas   from '../components/admin/AdminReservas.svelte';
  import AdminAgencias   from '../components/admin/AdminAgencias.svelte';
  import AdminReportes   from '../components/admin/AdminReportes.svelte';

  const API_BASE = 'http://localhost:7000';

  export let navigateTo = (page, data = null) => {};

  let activeSection = 'dashboard';

  // Contadores para badges del sidebar (cada módulo los actualiza via bind)
  let countUsuarios = 0;
  let countHoteles  = 0;
  let countReservas = 0;
  let countAgencias = 0;

  function setSection(id) {
    activeSection = id;
  }

  // Badge helper — compartido via prop a módulos que lo necesiten
  function badge(estado) {
    const e = (estado ?? '').toLowerCase();
    if (e === 'activo'  || e === 'confirmada' || e === 'activa')    return 'badge--green';
    if (e === 'cerrado' || e === 'inactivo'   || e === 'cancelada') return 'badge--red';
    if (e === 'suspendido' || e === 'mantenimiento')                return 'badge--orange';
    if (e === 'pendiente')                                          return 'badge--yellow';
    return 'badge--gray';
  }

  // Utilidad compartida
  function fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload  = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  // Combos fijos compartidos
  const tiposHabitacion = [
    { id: 1, nombre: 'Doble' },
    { id: 2, nombre: 'Junior Suite' },
    { id: 3, nombre: 'Suite' },
    { id: 4, nombre: 'Gran Suite' },
  ];
  const tiposCama = [
    { id: 1, nombre: 'Cama Doble' },
    { id: 2, nombre: 'Cama King' },
  ];

  const navItems = [
    { id: 'dashboard',    label: 'Dashboard',     icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { id: 'usuarios',     label: 'Usuarios',       icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'hoteles',      label: 'Hoteles',        icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    { id: 'crear-hotel',  label: 'Crear Hotel',    icon: 'M12 4v16m8-8H4' },
    { id: 'reservas',     label: 'Reservas',       icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { id: 'agencias',     label: 'Agencias',       icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4' },
    { id: 'reportes',     label: 'Reportes',       icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
  ];

  function getCount(id) {
    if (id === 'usuarios') return countUsuarios;
    if (id === 'hoteles')  return countHoteles;
    if (id === 'reservas') return countReservas;
    if (id === 'agencias') return countAgencias;
    return 0;
  }
</script>

<div class="adm">

  <!-- SIDEBAR -->
  <aside class="adm__sidebar">
    <div class="adm__sidebar-header">
      <div class="adm__sidebar-logo">
        <div class="adm__sidebar-logo-icon">⚙</div>
        <div>
          <p class="adm__sidebar-logo-title">Miku Inn</p>
          <p class="adm__sidebar-logo-sub">Panel de Administración</p>
        </div>
      </div>
    </div>
    <nav class="adm__sidebar-nav">
      {#each navItems as item}
        <button
          class="adm__nav-btn"
          class:adm__nav-btn--active={activeSection === item.id}
          on:click={() => setSection(item.id)}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d={item.icon} />
          </svg>
          {item.label}
        </button>
      {/each}
    </nav>
    <div class="adm__sidebar-footer">
      <button class="adm__back-btn" on:click={() => navigateTo('home')}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        Volver al sitio
      </button>
    </div>
  </aside>

  <!-- MAIN -->
  <div class="adm__main">
    <header class="adm__topbar">
      <div class="adm__topbar-left">
        <h1 class="adm__topbar-title">
          {#if activeSection === 'dashboard'}Dashboard General
          {:else if activeSection === 'usuarios'}Gestión de Usuarios
          {:else if activeSection === 'hoteles'}Gestión de Hoteles
          {:else if activeSection === 'crear-hotel'}Crear Nuevo Hotel
          {:else if activeSection === 'reservas'}Reservas
          {:else if activeSection === 'agencias'}Gestión de Agencias
          {:else if activeSection === 'reportes'}Reportes y Estadísticas
          {/if}
        </h1>
        <p class="adm__topbar-sub">Panel de Administración · Miku Inn</p>
      </div>
      <div class="adm__topbar-right">
        <div class="adm__topbar-date">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          {new Date().toLocaleDateString('es-GT', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </div>
      </div>
    </header>

    <div class="adm__content">

      {#if activeSection === 'dashboard'}
        <AdminDashboard {API_BASE} {badge} {setSection} />

      {:else if activeSection === 'usuarios'}
        <AdminUsuarios {API_BASE} {badge} bind:count={countUsuarios} />

      {:else if activeSection === 'hoteles'}
        <AdminHoteles {API_BASE} {badge} {fileToBase64} {tiposHabitacion} {tiposCama} bind:count={countHoteles} />

      {:else if activeSection === 'crear-hotel'}
        <AdminCrearHotel {API_BASE} {badge} {fileToBase64} {tiposHabitacion} {tiposCama} onFinish={() => { setSection('hoteles'); }} />

      {:else if activeSection === 'reservas'}
        <AdminReservas {API_BASE} {badge} bind:count={countReservas} />

      {:else if activeSection === 'agencias'}
        <AdminAgencias {API_BASE} {badge} bind:count={countAgencias} />

      {:else if activeSection === 'reportes'}
        <AdminReportes {API_BASE} {badge} />

      {/if}

    </div>
  </div>
</div>