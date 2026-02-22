<script>
  import '../styles/administrador.css';

  // CORRECCIÓN 1: el prop acepta (page, data) igual que en App.svelte
  export let navigateTo = (page, data = null) => {};

  let activeSection = 'dashboard';

  let showModalCrearUsuario    = false;
  let showModalEditarUsuario   = false;
  let showModalEliminarUsuario = false;
  let showModalCrearHotel      = false;
  let showModalEditarHotel     = false;
  let usuarioSeleccionado      = null;
  let hotelSeleccionado        = null;

  let busquedaUsuario   = '';
  let filtroRol         = 'todos';
  let busquedaHotel     = '';
  let filtroEstadoHotel = 'todos';

  let nuevoUsuario = { nombre: '', apellido: '', username: '', correo: '', pasaporte: '', telefono: '', fechaNacimiento: '', pais: '', ciudad: '', rolId: 1, contrasena: '' };
  let editUsuario  = { nombre: '', apellido: '', username: '', correo: '', pasaporte: '', telefono: '', pais: '', ciudad: '', rolId: 1, estado: 'activo' };
  let nuevoHotel   = { nombre: '', ciudad: '', pais: '', descripcion: '', estrellas: 5, precio: '', imagen: '', estado: 'activo' };
  let editHotel    = { nombre: '', ciudad: '', pais: '', descripcion: '', estrellas: 5, precio: '', imagen: '', estado: 'activo' };

  const statsData = [
    { label: 'Usuarios Totales', value: '1,248',    change: '+12%', icon: 'users',    color: 'blue'   },
    { label: 'Reservas Activas', value: '384',       change: '+8%',  icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',  value: '47',        change: '+3',   icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos del Mes', value: 'Q 284,500', change: '+21%', icon: 'money',    color: 'amber'  },
  ];

  const usuariosMock = [
    { id: 1, nombre: 'María',     apellido: 'González',  username: 'maria.g',  correo: 'maria@mail.com',  pais: 'Guatemala', rolId: 1, rol: 'Usuario',       estado: 'activo',     fechaRegistro: '2024-01-15' },
    { id: 2, nombre: 'Carlos',    apellido: 'Méndez',    username: 'carlosm',  correo: 'carlos@mail.com', pais: 'México',    rolId: 1, rol: 'Usuario',       estado: 'activo',     fechaRegistro: '2024-02-03' },
    { id: 3, nombre: 'Ana',       apellido: 'Rodríguez', username: 'ana.rod',  correo: 'ana@mail.com',    pais: 'España',    rolId: 2, rol: 'Administrador', estado: 'activo',     fechaRegistro: '2023-11-20' },
    { id: 4, nombre: 'Luis',      apellido: 'Pérez',     username: 'luisp',    correo: 'luis@mail.com',   pais: 'Colombia',  rolId: 1, rol: 'Usuario',       estado: 'inactivo',   fechaRegistro: '2024-03-10' },
    { id: 5, nombre: 'Sofía',     apellido: 'Martínez',  username: 'sofiamt',  correo: 'sofia@mail.com',  pais: 'Argentina', rolId: 1, rol: 'Usuario',       estado: 'activo',     fechaRegistro: '2024-04-22' },
    { id: 6, nombre: 'Diego',     apellido: 'Torres',    username: 'diegot',   correo: 'diego@mail.com',  pais: 'Guatemala', rolId: 1, rol: 'Usuario',       estado: 'activo',     fechaRegistro: '2024-05-05' },
    { id: 7, nombre: 'Valentina', apellido: 'Ruiz',      username: 'vale.r',   correo: 'vale@mail.com',   pais: 'Chile',     rolId: 1, rol: 'Usuario',       estado: 'suspendido', fechaRegistro: '2024-06-18' },
    { id: 8, nombre: 'Andrés',    apellido: 'López',     username: 'andresl',  correo: 'andres@mail.com', pais: 'Perú',      rolId: 2, rol: 'Administrador', estado: 'activo',     fechaRegistro: '2023-09-01' },
  ];

  const hotelesMock = [
    { id: 1, nombre: 'Grand Miku París',  ciudad: 'París',      pais: 'Francia',        estrellas: 5, precio: 450, estado: 'activo',        habitaciones: 120, reservas: 84  },
    { id: 2, nombre: 'Miku Inn Londres',  ciudad: 'Londres',    pais: 'Reino Unido',    estrellas: 4, precio: 320, estado: 'activo',        habitaciones: 95,  reservas: 61  },
    { id: 3, nombre: 'Tokio Miku Palace', ciudad: 'Tokio',      pais: 'Japón',          estrellas: 5, precio: 580, estado: 'activo',        habitaciones: 200, reservas: 178 },
    { id: 4, nombre: 'Miku NY Suite',     ciudad: 'Nueva York', pais: 'Estados Unidos', estrellas: 5, precio: 620, estado: 'activo',        habitaciones: 150, reservas: 132 },
    { id: 5, nombre: 'Miku Antigua',      ciudad: 'Antigua',    pais: 'Guatemala',      estrellas: 4, precio: 180, estado: 'mantenimiento', habitaciones: 60,  reservas: 0   },
    { id: 6, nombre: 'Barcelona Miku',    ciudad: 'Barcelona',  pais: 'España',         estrellas: 4, precio: 290, estado: 'activo',        habitaciones: 85,  reservas: 47  },
  ];

  const reservasMock = [
    { id: 'RES-001', usuario: 'maria.g',  hotel: 'Grand Miku París',  checkIn: '2025-03-10', checkOut: '2025-03-15', monto: 'Q 2,250', estado: 'confirmada' },
    { id: 'RES-002', usuario: 'carlosm',  hotel: 'Tokio Miku Palace', checkIn: '2025-04-01', checkOut: '2025-04-07', monto: 'Q 4,060', estado: 'pendiente'  },
    { id: 'RES-003', usuario: 'sofiamt',  hotel: 'Miku NY Suite',     checkIn: '2025-02-20', checkOut: '2025-02-25', monto: 'Q 3,100', estado: 'confirmada' },
    { id: 'RES-004', usuario: 'luisp',    hotel: 'Miku Inn Londres',  checkIn: '2025-05-05', checkOut: '2025-05-08', monto: 'Q 960',   estado: 'cancelada'  },
    { id: 'RES-005', usuario: 'diegot',   hotel: 'Barcelona Miku',    checkIn: '2025-06-12', checkOut: '2025-06-16', monto: 'Q 1,160', estado: 'confirmada' },
    { id: 'RES-006', usuario: 'vale.r',   hotel: 'Miku Antigua',      checkIn: '2025-01-30', checkOut: '2025-02-02', monto: 'Q 540',   estado: 'cancelada'  },
  ];

  $: usuariosFiltrados = usuariosMock.filter(u => {
    const q = busquedaUsuario.toLowerCase();
    const matchBusqueda = q === '' || u.nombre.toLowerCase().includes(q) || u.apellido.toLowerCase().includes(q) || u.username.toLowerCase().includes(q) || u.correo.toLowerCase().includes(q);
    return matchBusqueda && (filtroRol === 'todos' || String(u.rolId) === filtroRol);
  });

  $: hotelesFiltrados = hotelesMock.filter(h => {
    const q = busquedaHotel.toLowerCase();
    const matchBusqueda = q === '' || h.nombre.toLowerCase().includes(q) || h.ciudad.toLowerCase().includes(q) || h.pais.toLowerCase().includes(q);
    return matchBusqueda && (filtroEstadoHotel === 'todos' || h.estado === filtroEstadoHotel);
  });

  function abrirCrearUsuario() {
    nuevoUsuario = { nombre: '', apellido: '', username: '', correo: '', pasaporte: '', telefono: '', fechaNacimiento: '', pais: '', ciudad: '', rolId: 1, contrasena: '' };
    showModalCrearUsuario = true;
  }

  function abrirEditarUsuario(u) {
    usuarioSeleccionado = u;
    editUsuario = { nombre: u.nombre, apellido: u.apellido, username: u.username, correo: u.correo, pasaporte: u.pasaporte || '', telefono: u.telefono || '', pais: u.pais, ciudad: u.ciudad || '', rolId: u.rolId, estado: u.estado };
    showModalEditarUsuario = true;
  }

  function abrirEliminarUsuario(u) {
    usuarioSeleccionado = u;
    showModalEliminarUsuario = true;
  }

  function abrirCrearHotel() {
    nuevoHotel = { nombre: '', ciudad: '', pais: '', descripcion: '', estrellas: 5, precio: '', imagen: '', estado: 'activo' };
    showModalCrearHotel = true;
  }

  function abrirEditarHotel(h) {
    hotelSeleccionado = h;
    editHotel = { nombre: h.nombre, ciudad: h.ciudad, pais: h.pais, descripcion: '', estrellas: h.estrellas, precio: h.precio, imagen: '', estado: h.estado };
    showModalEditarHotel = true;
  }

  function cerrarModales() {
    showModalCrearUsuario = showModalEditarUsuario = showModalEliminarUsuario = showModalCrearHotel = showModalEditarHotel = false;
    usuarioSeleccionado = hotelSeleccionado = null;
  }

  // CORRECCIÓN 2: handler para cerrar overlay con teclado (accesibilidad)
  function handleOverlayKey(e) {
    if (e.key === 'Escape' || e.key === 'Enter' || e.key === ' ') cerrarModales();
  }

  function badge(estado) {
    if (estado === 'activo'     || estado === 'confirmada')    return 'badge--green';
    if (estado === 'inactivo'   || estado === 'cancelada')     return 'badge--red';
    if (estado === 'suspendido' || estado === 'mantenimiento') return 'badge--orange';
    if (estado === 'pendiente')                                return 'badge--yellow';
    return 'badge--gray';
  }

  function getStars(n) { return '★'.repeat(n) + '☆'.repeat(5 - n); }

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { id: 'usuarios',  label: 'Usuarios',  icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'hoteles',   label: 'Hoteles',   icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    { id: 'reservas',  label: 'Reservas',  icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { id: 'reportes',  label: 'Reportes',  icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
  ];
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
        <button class="adm__nav-btn" class:adm__nav-btn--active={activeSection === item.id} on:click={() => activeSection = item.id}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d={item.icon} />
          </svg>
          {item.label}
          {#if item.id === 'usuarios'}<span class="adm__nav-count">{usuariosMock.length}</span>
          {:else if item.id === 'hoteles'}<span class="adm__nav-count">{hotelesMock.length}</span>
          {:else if item.id === 'reservas'}<span class="adm__nav-count">{reservasMock.length}</span>
          {/if}
        </button>
      {/each}
    </nav>
    <div class="adm__sidebar-footer">
      <!-- navigateTo acepta argumento correctamente -->
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
          {:else if activeSection === 'reservas'}Reservas
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
        {#if activeSection === 'usuarios'}
          <button class="adm__btn adm__btn--primary" on:click={abrirCrearUsuario}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Nuevo Usuario
          </button>
        {:else if activeSection === 'hoteles'}
          <button class="adm__btn adm__btn--primary" on:click={abrirCrearHotel}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Nuevo Hotel
          </button>
        {/if}
      </div>
    </header>

    <div class="adm__content">

      <!-- ═══ DASHBOARD ═══ -->
      {#if activeSection === 'dashboard'}
        <div class="adm__stats-grid">
          {#each statsData as stat}
            <div class="adm__stat-card adm__stat-card--{stat.color}">
              <div class="adm__stat-top">
                <div class="adm__stat-icon-wrap">
                  {#if stat.icon === 'users'}
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  {:else if stat.icon === 'calendar'}
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {:else if stat.icon === 'hotel'}
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  {:else}
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                  {/if}
                </div>
                <span class="adm__stat-change">{stat.change}</span>
              </div>
              <p class="adm__stat-value">{stat.value}</p>
              <p class="adm__stat-label">{stat.label}</p>
            </div>
          {/each}
        </div>

        <div class="adm__dashboard-grid">
          <div class="adm__card">
            <div class="adm__card-header">
              <h3 class="adm__card-title">Últimas Reservas</h3>
              <button class="adm__link-btn" on:click={() => activeSection = 'reservas'}>Ver todas →</button>
            </div>
            <div class="adm__table-wrap">
              <table class="adm__table">
                <thead><tr><th>ID</th><th>Usuario</th><th>Hotel</th><th>Estado</th></tr></thead>
                <tbody>
                  {#each reservasMock.slice(0, 5) as r}
                    <tr>
                      <td class="adm__table-mono">{r.id}</td>
                      <td>{r.usuario}</td>
                      <td>{r.hotel}</td>
                      <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            </div>
          </div>

          <div class="adm__card">
            <div class="adm__card-header">
              <h3 class="adm__card-title">Hoteles por Ocupación</h3>
            </div>
            <div class="adm__hotel-bars">
              {#each hotelesMock as h}
                {@const pct = Math.round((h.reservas / h.habitaciones) * 100)}
                <div class="adm__hotel-bar-item">
                  <div class="adm__hotel-bar-info">
                    <span class="adm__hotel-bar-name">{h.nombre}</span>
                    <span class="adm__hotel-bar-pct">{pct}%</span>
                  </div>
                  <div class="adm__hotel-bar-track">
                    <div class="adm__hotel-bar-fill" style="width: {pct}%"></div>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        </div>

        <div class="adm__card">
          <div class="adm__card-header">
            <h3 class="adm__card-title">Usuarios Recientes</h3>
            <button class="adm__link-btn" on:click={() => activeSection = 'usuarios'}>Ver todos →</button>
          </div>
          <div class="adm__table-wrap">
            <table class="adm__table">
              <thead><tr><th>Nombre</th><th>Username</th><th>País</th><th>Rol</th><th>Estado</th><th>Registro</th></tr></thead>
              <tbody>
                {#each usuariosMock.slice(0, 5) as u}
                  <tr>
                    <td>
                      <div class="adm__user-mini">
                        <div class="adm__user-mini-avatar" style="background: {u.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
                          {u.nombre.charAt(0)}
                        </div>
                        {u.nombre} {u.apellido}
                      </div>
                    </td>
                    <td class="adm__table-mono">@{u.username}</td>
                    <td>{u.pais}</td>
                    <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : 'badge--blue'}">{u.rol}</span></td>
                    <td><span class="adm__badge {badge(u.estado)}">{u.estado}</span></td>
                    <td>{u.fechaRegistro}</td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>

      <!-- ═══ USUARIOS ═══ -->
      {:else if activeSection === 'usuarios'}
        <div class="adm__filters-bar">
          <div class="adm__search-wrap">
            <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            <!-- aria-label en el input evita el warning de label -->
            <input class="adm__search-input" type="text" bind:value={busquedaUsuario}
              placeholder="Buscar por nombre, username, correo..." aria-label="Buscar usuarios" />
          </div>
          <select class="adm__select" bind:value={filtroRol} aria-label="Filtrar por rol">
            <option value="todos">Todos los roles</option>
            <option value="1">Usuarios</option>
            <option value="2">Administradores</option>
          </select>
          <span class="adm__count-label">{usuariosFiltrados.length} resultado(s)</span>
        </div>

        <div class="adm__card adm__card--no-pad">
          <div class="adm__table-wrap">
            <table class="adm__table">
              <thead>
                <tr><th>Usuario</th><th>Correo</th><th>País</th><th>Rol</th><th>Estado</th><th>Registro</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {#each usuariosFiltrados as u (u.id)}
                  <tr>
                    <td>
                      <div class="adm__user-mini">
                        <div class="adm__user-mini-avatar" style="background: {u.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
                          {u.nombre.charAt(0)}
                        </div>
                        <div>
                          <p class="adm__user-mini-name">{u.nombre} {u.apellido}</p>
                          <p class="adm__user-mini-sub">@{u.username}</p>
                        </div>
                      </div>
                    </td>
                    <td class="adm__table-mono">{u.correo}</td>
                    <td>{u.pais}</td>
                    <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : 'badge--blue'}">{u.rol}</span></td>
                    <td><span class="adm__badge {badge(u.estado)}">{u.estado}</span></td>
                    <td>{u.fechaRegistro}</td>
                    <td>
                      <div class="adm__action-btns">
                        <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarUsuario(u)} title="Editar usuario">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                        </button>
                        <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => abrirEliminarUsuario(u)} title="Eliminar usuario">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6M9 6V4h6v2"/></svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                {/each}
                {#if usuariosFiltrados.length === 0}
                  <tr><td colspan="7" class="adm__empty-cell">No se encontraron usuarios con esos filtros.</td></tr>
                {/if}
              </tbody>
            </table>
          </div>
        </div>

      <!-- ═══ HOTELES ═══ -->
      {:else if activeSection === 'hoteles'}
        <div class="adm__filters-bar">
          <div class="adm__search-wrap">
            <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            <input class="adm__search-input" type="text" bind:value={busquedaHotel}
              placeholder="Buscar hotel, ciudad, país..." aria-label="Buscar hoteles" />
          </div>
          <select class="adm__select" bind:value={filtroEstadoHotel} aria-label="Filtrar por estado">
            <option value="todos">Todos los estados</option>
            <option value="activo">Activo</option>
            <option value="mantenimiento">Mantenimiento</option>
            <option value="inactivo">Inactivo</option>
          </select>
          <span class="adm__count-label">{hotelesFiltrados.length} hotel(es)</span>
        </div>

        <div class="adm__hotels-grid">
          {#each hotelesFiltrados as h (h.id)}
            <div class="adm__hotel-card">
              <div class="adm__hotel-card-header">
                <div class="adm__hotel-card-icon">🏨</div>
                <span class="adm__badge {badge(h.estado)}">{h.estado}</span>
              </div>
              <h3 class="adm__hotel-card-name">{h.nombre}</h3>
              <p class="adm__hotel-card-location">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                {h.ciudad}, {h.pais}
              </p>
              <p class="adm__hotel-card-stars">{getStars(h.estrellas)}</p>
              <div class="adm__hotel-card-stats">
                <div class="adm__hotel-stat"><span class="adm__hotel-stat-val">{h.habitaciones}</span><span class="adm__hotel-stat-lbl">Habitaciones</span></div>
                <div class="adm__hotel-stat"><span class="adm__hotel-stat-val">{h.reservas}</span><span class="adm__hotel-stat-lbl">Reservas activas</span></div>
                <div class="adm__hotel-stat"><span class="adm__hotel-stat-val">Q {h.precio}</span><span class="adm__hotel-stat-lbl">Precio/noche</span></div>
              </div>
              <div class="adm__hotel-card-actions">
                <button class="adm__btn adm__btn--ghost" on:click={() => abrirEditarHotel(h)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Editar
                </button>
                <button class="adm__btn adm__btn--danger-ghost">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>
                  Eliminar
                </button>
              </div>
            </div>
          {/each}
          {#if hotelesFiltrados.length === 0}
            <div class="adm__empty-state">No se encontraron hoteles con esos filtros.</div>
          {/if}
        </div>

      <!-- ═══ RESERVAS ═══ -->
      {:else if activeSection === 'reservas'}
        <div class="adm__card adm__card--no-pad">
          <div class="adm__card-header adm__card-header--pad">
            <h3 class="adm__card-title">Todas las Reservas</h3>
            <div class="adm__reservas-stats">
              <span class="adm__badge badge--green">Confirmadas: {reservasMock.filter(r => r.estado === 'confirmada').length}</span>
              <span class="adm__badge badge--yellow">Pendientes: {reservasMock.filter(r => r.estado === 'pendiente').length}</span>
              <span class="adm__badge badge--red">Canceladas: {reservasMock.filter(r => r.estado === 'cancelada').length}</span>
            </div>
          </div>
          <div class="adm__table-wrap">
            <table class="adm__table">
              <thead>
                <tr><th>ID Reserva</th><th>Usuario</th><th>Hotel</th><th>Check-in</th><th>Check-out</th><th>Monto</th><th>Estado</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {#each reservasMock as r}
                  <tr>
                    <td class="adm__table-mono">{r.id}</td>
                    <td>{r.usuario}</td>
                    <td>{r.hotel}</td>
                    <td>{r.checkIn}</td>
                    <td>{r.checkOut}</td>
                    <td class="adm__table-money">{r.monto}</td>
                    <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
                    <td>
                      <div class="adm__action-btns">
                        <button class="adm__icon-btn adm__icon-btn--view" title="Ver detalle">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        </button>
                        <button class="adm__icon-btn adm__icon-btn--delete" title="Cancelar reserva">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>

      <!-- ═══ REPORTES ═══ -->
      {:else if activeSection === 'reportes'}
        <div class="adm__reportes-grid">
          <div class="adm__card adm__reporte-card">
            <h3 class="adm__card-title">Ingresos por Mes (2025)</h3>
            <div class="adm__bar-chart" role="img" aria-label="Gráfica de ingresos por mes">
              {#each [{mes:'Ene',val:65},{mes:'Feb',val:80},{mes:'Mar',val:55},{mes:'Abr',val:90},{mes:'May',val:70},{mes:'Jun',val:85}] as b}
                <div class="adm__bar-col">
                  <div class="adm__bar" style="height: {b.val}%"></div>
                  <span class="adm__bar-label">{b.mes}</span>
                </div>
              {/each}
            </div>
          </div>

          <div class="adm__card adm__reporte-card">
            <h3 class="adm__card-title">Reservas por Estado</h3>
            <div class="adm__donut-wrap">
              <div class="adm__donut" role="img" aria-label="Gráfica de reservas por estado">
                <span class="adm__donut-center">6</span>
              </div>
              <div class="adm__donut-legend">
                <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--green"></span> Confirmadas (3)</div>
                <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--yellow"></span> Pendientes (1)</div>
                <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--red"></span> Canceladas (2)</div>
              </div>
            </div>
          </div>

          <div class="adm__card adm__reporte-card adm__reporte-card--wide">
            <h3 class="adm__card-title">Resumen General</h3>
            <div class="adm__kpi-grid">
              <div class="adm__kpi-item"><p class="adm__kpi-val">Q 284,500</p><p class="adm__kpi-lbl">Ingresos totales</p></div>
              <div class="adm__kpi-item"><p class="adm__kpi-val">1,248</p><p class="adm__kpi-lbl">Usuarios registrados</p></div>
              <div class="adm__kpi-item"><p class="adm__kpi-val">384</p><p class="adm__kpi-lbl">Reservas activas</p></div>
              <div class="adm__kpi-item"><p class="adm__kpi-val">78%</p><p class="adm__kpi-lbl">Tasa de ocupación</p></div>
            </div>
          </div>

          <div class="adm__card adm__reporte-card adm__reporte-card--wide">
            <div class="adm__card-header"><h3 class="adm__card-title">Exportar Reportes</h3></div>
            <div class="adm__export-btns">
              <button class="adm__btn adm__btn--export">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                Reporte de Usuarios (PDF)
              </button>
              <button class="adm__btn adm__btn--export">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Reporte de Reservas (Excel)
              </button>
              <button class="adm__btn adm__btn--export">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                Reporte Financiero (PDF)
              </button>
              <button class="adm__btn adm__btn--export">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                Ocupación Hotelera (PDF)
              </button>
            </div>
          </div>
        </div>
      {/if}

    </div>
  </div>
</div>

{#if showModalCrearUsuario}
  <dialog class="adm__dialog" open on:click={e => e.target === e.currentTarget && cerrarModales()}>
    <div class="adm__modal">
      <div class="adm__modal-header">
        <h2 class="adm__modal-title">Crear Nuevo Usuario</h2>
        <button class="adm__modal-close" on:click={cerrarModales} aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="adm__modal-body">
        <div class="adm__form-grid">
          <div class="adm__field"><label for="cu-nombre">Nombre</label><input id="cu-nombre" type="text" bind:value={nuevoUsuario.nombre} placeholder="Nombre" /></div>
          <div class="adm__field"><label for="cu-apellido">Apellido</label><input id="cu-apellido" type="text" bind:value={nuevoUsuario.apellido} placeholder="Apellido" /></div>
          <div class="adm__field"><label for="cu-username">Username</label><input id="cu-username" type="text" bind:value={nuevoUsuario.username} placeholder="username123" /></div>
          <div class="adm__field"><label for="cu-correo">Correo Electrónico</label><input id="cu-correo" type="email" bind:value={nuevoUsuario.correo} placeholder="correo@email.com" /></div>
          <div class="adm__field"><label for="cu-pasaporte">Pasaporte</label><input id="cu-pasaporte" type="text" bind:value={nuevoUsuario.pasaporte} placeholder="AB1234567" /></div>
          <div class="adm__field"><label for="cu-telefono">Teléfono</label><input id="cu-telefono" type="tel" bind:value={nuevoUsuario.telefono} placeholder="+502 1234 5678" /></div>
          <div class="adm__field"><label for="cu-fnac">Fecha de Nacimiento</label><input id="cu-fnac" type="date" bind:value={nuevoUsuario.fechaNacimiento} /></div>
          <div class="adm__field"><label for="cu-pais">País</label><input id="cu-pais" type="text" bind:value={nuevoUsuario.pais} placeholder="Guatemala" /></div>
          <div class="adm__field"><label for="cu-ciudad">Ciudad</label><input id="cu-ciudad" type="text" bind:value={nuevoUsuario.ciudad} placeholder="Ciudad de Guatemala" /></div>
          <div class="adm__field">
            <label for="cu-rol">Rol</label>
            <select id="cu-rol" bind:value={nuevoUsuario.rolId}>
              <option value={1}>Usuario Registrado</option>
              <option value={2}>Administrador</option>
            </select>
          </div>
          <div class="adm__field adm__field--full"><label for="cu-pass">Contraseña Temporal</label><input id="cu-pass" type="password" bind:value={nuevoUsuario.contrasena} placeholder="Mínimo 8 caracteres" /></div>
        </div>
      </div>
      <div class="adm__modal-footer">
        <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={cerrarModales}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
          Crear Usuario
        </button>
      </div>
    </div>
  </dialog>
{/if}

{#if showModalEditarUsuario && usuarioSeleccionado}
  <dialog class="adm__dialog" open on:click={e => e.target === e.currentTarget && cerrarModales()}>
    <div class="adm__modal">
      <div class="adm__modal-header">
        <h2 class="adm__modal-title">Editar Usuario — @{usuarioSeleccionado.username}</h2>
        <button class="adm__modal-close" on:click={cerrarModales} aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="adm__modal-body">
        <div class="adm__form-grid">
          <div class="adm__field"><label for="eu-nombre">Nombre</label><input id="eu-nombre" type="text" bind:value={editUsuario.nombre} /></div>
          <div class="adm__field"><label for="eu-apellido">Apellido</label><input id="eu-apellido" type="text" bind:value={editUsuario.apellido} /></div>
          <div class="adm__field"><label for="eu-username">Username</label><input id="eu-username" type="text" bind:value={editUsuario.username} /></div>
          <div class="adm__field"><label for="eu-correo">Correo</label><input id="eu-correo" type="email" bind:value={editUsuario.correo} /></div>
          <div class="adm__field"><label for="eu-pasaporte">Pasaporte</label><input id="eu-pasaporte" type="text" bind:value={editUsuario.pasaporte} /></div>
          <div class="adm__field"><label for="eu-telefono">Teléfono</label><input id="eu-telefono" type="tel" bind:value={editUsuario.telefono} /></div>
          <div class="adm__field"><label for="eu-pais">País</label><input id="eu-pais" type="text" bind:value={editUsuario.pais} /></div>
          <div class="adm__field"><label for="eu-ciudad">Ciudad</label><input id="eu-ciudad" type="text" bind:value={editUsuario.ciudad} /></div>
          <div class="adm__field">
            <label for="eu-rol">Rol</label>
            <select id="eu-rol" bind:value={editUsuario.rolId}>
              <option value={1}>Usuario Registrado</option>
              <option value={2}>Administrador</option>
            </select>
          </div>
          <div class="adm__field">
            <label for="eu-estado">Estado</label>
            <select id="eu-estado" bind:value={editUsuario.estado}>
              <option value="activo">Activo</option>
              <option value="inactivo">Inactivo</option>
              <option value="suspendido">Suspendido</option>
            </select>
          </div>
        </div>
      </div>
      <div class="adm__modal-footer">
        <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={cerrarModales}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
          Guardar Cambios
        </button>
      </div>
    </div>
  </dialog>
{/if}

{#if showModalEliminarUsuario && usuarioSeleccionado}
  <dialog class="adm__dialog" open on:click={e => e.target === e.currentTarget && cerrarModales()}>
    <div class="adm__modal adm__modal--sm">
      <div class="adm__modal-header">
        <h2 class="adm__modal-title adm__modal-title--danger">Eliminar Usuario</h2>
        <button class="adm__modal-close" on:click={cerrarModales} aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="adm__modal-body adm__modal-body--center">
        <div class="adm__danger-icon">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        </div>
        <p class="adm__danger-text">¿Estás seguro de que deseas eliminar a <strong>{usuarioSeleccionado.nombre} {usuarioSeleccionado.apellido}</strong> (@{usuarioSeleccionado.username})?</p>
        <p class="adm__danger-sub">Esta acción no se puede deshacer.</p>
      </div>
      <div class="adm__modal-footer">
        <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
        <button class="adm__btn adm__btn--danger" on:click={cerrarModales}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></svg>
          Eliminar Usuario
        </button>
      </div>
    </div>
  </dialog>
{/if}

{#if showModalCrearHotel}
  <dialog class="adm__dialog" open on:click={e => e.target === e.currentTarget && cerrarModales()}>
    <div class="adm__modal">
      <div class="adm__modal-header">
        <h2 class="adm__modal-title">Agregar Nuevo Hotel</h2>
        <button class="adm__modal-close" on:click={cerrarModales} aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="adm__modal-body">
        <div class="adm__form-grid">
          <div class="adm__field adm__field--full"><label for="ch-nombre">Nombre del Hotel</label><input id="ch-nombre" type="text" bind:value={nuevoHotel.nombre} placeholder="Grand Miku..." /></div>
          <div class="adm__field"><label for="ch-ciudad">Ciudad</label><input id="ch-ciudad" type="text" bind:value={nuevoHotel.ciudad} placeholder="París" /></div>
          <div class="adm__field"><label for="ch-pais">País</label><input id="ch-pais" type="text" bind:value={nuevoHotel.pais} placeholder="Francia" /></div>
          <div class="adm__field">
            <label for="ch-estrellas">Estrellas (1–5)</label>
            <select id="ch-estrellas" bind:value={nuevoHotel.estrellas}>
              {#each [1,2,3,4,5] as n}<option value={n}>{n} ★</option>{/each}
            </select>
          </div>
          <div class="adm__field"><label for="ch-precio">Precio por Noche (Q)</label><input id="ch-precio" type="number" bind:value={nuevoHotel.precio} placeholder="450" min="0" /></div>
          <div class="adm__field">
            <label for="ch-estado">Estado</label>
            <select id="ch-estado" bind:value={nuevoHotel.estado}>
              <option value="activo">Activo</option>
              <option value="mantenimiento">Mantenimiento</option>
              <option value="inactivo">Inactivo</option>
            </select>
          </div>
          <div class="adm__field"><label for="ch-imagen">URL de Imagen</label><input id="ch-imagen" type="url" bind:value={nuevoHotel.imagen} placeholder="https://..." /></div>
          <div class="adm__field adm__field--full"><label for="ch-desc">Descripción</label><textarea id="ch-desc" bind:value={nuevoHotel.descripcion} placeholder="Descripción del hotel..." rows="3"></textarea></div>
        </div>
      </div>
      <div class="adm__modal-footer">
        <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={cerrarModales}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
          Crear Hotel
        </button>
      </div>
    </div>
  </dialog>
{/if}

{#if showModalEditarHotel && hotelSeleccionado}
  <dialog class="adm__dialog" open on:click={e => e.target === e.currentTarget && cerrarModales()}>
    <div class="adm__modal">
      <div class="adm__modal-header">
        <h2 class="adm__modal-title">Editar Hotel — {hotelSeleccionado.nombre}</h2>
        <button class="adm__modal-close" on:click={cerrarModales} aria-label="Cerrar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="adm__modal-body">
        <div class="adm__form-grid">
          <div class="adm__field adm__field--full"><label for="eh-nombre">Nombre del Hotel</label><input id="eh-nombre" type="text" bind:value={editHotel.nombre} /></div>
          <div class="adm__field"><label for="eh-ciudad">Ciudad</label><input id="eh-ciudad" type="text" bind:value={editHotel.ciudad} /></div>
          <div class="adm__field"><label for="eh-pais">País</label><input id="eh-pais" type="text" bind:value={editHotel.pais} /></div>
          <div class="adm__field">
            <label for="eh-estrellas">Estrellas</label>
            <select id="eh-estrellas" bind:value={editHotel.estrellas}>
              {#each [1,2,3,4,5] as n}<option value={n}>{n} ★</option>{/each}
            </select>
          </div>
          <div class="adm__field"><label for="eh-precio">Precio por Noche (Q)</label><input id="eh-precio" type="number" bind:value={editHotel.precio} min="0" /></div>
          <div class="adm__field">
            <label for="eh-estado">Estado</label>
            <select id="eh-estado" bind:value={editHotel.estado}>
              <option value="activo">Activo</option>
              <option value="mantenimiento">Mantenimiento</option>
              <option value="inactivo">Inactivo</option>
            </select>
          </div>
          <div class="adm__field"><label for="eh-imagen">URL de Imagen</label><input id="eh-imagen" type="url" bind:value={editHotel.imagen} /></div>
          <div class="adm__field adm__field--full"><label for="eh-desc">Descripción</label><textarea id="eh-desc" bind:value={editHotel.descripcion} rows="3"></textarea></div>
        </div>
      </div>
      <div class="adm__modal-footer">
        <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
        <button class="adm__btn adm__btn--primary" on:click={cerrarModales}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
          Guardar Cambios
        </button>
      </div>
    </div>
  </dialog>
{/if}