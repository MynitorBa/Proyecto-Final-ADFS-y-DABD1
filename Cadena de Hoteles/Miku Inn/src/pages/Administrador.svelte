<script>
  import '../styles/administrador.css';
  import { onMount } from 'svelte';

  const API_BASE = 'http://localhost:7000';

  // CORRECCIÓN 1: el prop acepta (page, data) igual que en App.svelte
  export let navigateTo = (page, data = null) => {};

  let activeSection = 'dashboard';

  let showModalCrearUsuario    = false;
  let showModalEditarUsuario   = false;
  let showModalEliminarUsuario = false;
  let usuarioSeleccionado      = null;
  let hotelSeleccionado        = null;

  let busquedaUsuario   = '';
  let filtroRol         = 'todos';
  let busquedaHotel     = '';
  let filtroEstadoHotel = 'todos';

  // ── Estado de usuarios desde la API ───────────────────────────────────────
  let usuarios          = [];
  let cargandoUsuarios  = false;
  let errorUsuarios     = null;

  // ── Feedback de cambio de rol ─────────────────────────────────────────────
  let guardandoRol      = false;
  let mensajeRol        = null;   // { tipo: 'ok'|'error', texto: string }

  let nuevoUsuario = { nombre: '', apellido: '', username: '', correo: '', pasaporte: '', telefono: '', fechaNacimiento: '', pais: '', ciudad: '', rolId: 1, contrasena: '' };
  let editUsuario  = { rolId: 1 };

  const statsData = [
    { label: 'Usuarios Totales', value: '1,248',    change: '+12%', icon: 'users',    color: 'blue'   },
    { label: 'Reservas Activas', value: '384',       change: '+8%',  icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',  value: '47',        change: '+3',   icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos del Mes', value: 'Q 284,500', change: '+21%', icon: 'money',    color: 'amber'  },
  ];

  // ── Carga usuarios desde el backend ──────────────────────────────────────
  async function cargarUsuarios() {
    cargandoUsuarios = true;
    errorUsuarios    = null;
    try {
      const res = await fetch(`${API_BASE}/admin/usuarios`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      usuarios = await res.json();
    } catch (e) {
      errorUsuarios = 'No se pudo cargar la lista de usuarios. ' + e.message;
    } finally {
      cargandoUsuarios = false;
    }
  }

  onMount(() => {
    cargarUsuarios();
    cargarHoteles();
  });

  // ════════════════════════════════════════════════════
  //  HOTELES — estado
  // ════════════════════════════════════════════════════
  let hoteles          = [];
  let cargandoHoteles  = false;
  let errorHoteles     = null;

  // Vista lista vs detalle
  let vistaHoteles   = 'lista';   // 'lista' | 'detalle'
  let hotelDetalle   = null;      // hotel completo en vista detalle
  let tabDetalle     = 'info';    // 'info' | 'imagenes' | 'habitaciones'

  // Editar info del hotel
  let editInfoHotel   = { nombre: '', direccion: '', descripcion: '', rating: 0, estadoId: 1 };
  let guardandoInfo   = false;
  let mensajeInfo     = null;

  // Imágenes del hotel
  let subiendoImgHotel  = false;
  let mensajeImgHotel   = null;

  // Habitaciones
  let habitaciones         = [];
  let cargandoHabitaciones = false;
  let errorHabitaciones    = null;
  let showModalHabitacion  = false;
  let habitacionEditando   = null;
  let editHabitacion       = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 1, metrosCuadrados: 0, descripcion: '', estadoId: 1 };
  let guardandoHabitacion  = false;
  let mensajeHabitacion    = null;
  let subiendoImgHab       = false;
  let mensajeImgHab        = null;

  // Combos fijos (coinciden con inserts SQL)
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

  // ════════════════════════════════════════════════════
  //  HOTELES — funciones
  // ════════════════════════════════════════════════════

  async function cargarHoteles() {
    cargandoHoteles = true;
    errorHoteles    = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hoteles = await res.json();
    } catch (e) {
      errorHoteles = 'No se pudo cargar la lista de hoteles. ' + e.message;
    } finally {
      cargandoHoteles = false;
    }
  }

  function abrirDetalleHotel(h) {
    hotelDetalle = { ...h };
    editInfoHotel = { nombre: h.nombre ?? '', direccion: h.direccion ?? '', descripcion: h.descripcion ?? '', rating: h.rating ?? 0, estadoId: h.estadoId ?? 1 };
    tabDetalle   = 'info';
    mensajeInfo  = null;
    vistaHoteles = 'detalle';
    cargarHabitacionesDetalle(h.id);
  }

  function volverListaHoteles() {
    vistaHoteles = 'lista';
    hotelDetalle = null;
    habitaciones = [];
  }

  // ── Info ──────────────────────────────────────────

  async function guardarInfoHotel() {
    guardandoInfo = true;
    mensajeInfo   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: editInfoHotel.nombre, direccion: editInfoHotel.direccion, descripcion: editInfoHotel.descripcion, rating: Number(editInfoHotel.rating), estadoId: Number(editInfoHotel.estadoId) })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeInfo = { tipo: 'ok', texto: 'Hotel actualizado correctamente.' };
      // Actualizar localmente
      const estadoStr = editInfoHotel.estadoId === 1 ? 'Activo' : 'Cerrado';
      hotelDetalle = { ...hotelDetalle, ...editInfoHotel, estado: estadoStr };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, ...editInfoHotel, estado: estadoStr } : h);
    } catch (e) {
      mensajeInfo = { tipo: 'error', texto: e.message };
    } finally {
      guardandoInfo = false;
    }
  }

  // ── Imágenes del hotel ────────────────────────────

  async function subirImagenHotel(event) {
    const file = event.target.files[0];
    if (!file) return;
    subiendoImgHotel = true;
    mensajeImgHotel  = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/imagenes`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64 })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      // Agregar el nuevo id a la lista local
      hotelDetalle = { ...hotelDetalle, imagenesIds: [...(hotelDetalle.imagenesIds ?? []), data.id] };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
      mensajeImgHotel = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) {
      mensajeImgHotel = { tipo: 'error', texto: e.message };
    } finally {
      subiendoImgHotel = false;
      event.target.value = '';
    }
  }

  async function eliminarImagenHotel(imagenId) {
    if (!confirm('¿Eliminar esta imagen?')) return;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hotelDetalle = { ...hotelDetalle, imagenesIds: hotelDetalle.imagenesIds.filter(id => id !== imagenId) };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
    } catch (e) {
      mensajeImgHotel = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message };
    }
  }

  // ── Habitaciones ──────────────────────────────────

  async function cargarHabitacionesDetalle(hotelId) {
    cargandoHabitaciones = true;
    errorHabitaciones    = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelId}/habitaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      habitaciones = await res.json();
    } catch (e) {
      errorHabitaciones = 'No se pudieron cargar las habitaciones. ' + e.message;
    } finally {
      cargandoHabitaciones = false;
    }
  }

  function abrirEditarHabitacion(h) {
    habitacionEditando = h;
    editHabitacion = {
      tipoHabitacionId: h.tipoHabitacionId,
      camaId:           h.camaId,
      precioPorPersona: h.precioPorPersona,
      precioPorNoche:   h.precioPorNoche,
      capacidadMaxima:  h.capacidadMaxima,
      metrosCuadrados:  h.metrosCuadrados,
      descripcion:      h.descripcion ?? '',
      estadoId:         h.estadoId
    };
    mensajeHabitacion = null;
    mensajeImgHab     = null;
    showModalHabitacion = true;
  }

  async function guardarHabitacion() {
    guardandoHabitacion = true;
    mensajeHabitacion   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...editHabitacion, tipoHabitacionId: Number(editHabitacion.tipoHabitacionId), camaId: Number(editHabitacion.camaId), precioPorPersona: Number(editHabitacion.precioPorPersona), precioPorNoche: Number(editHabitacion.precioPorNoche), capacidadMaxima: Number(editHabitacion.capacidadMaxima), metrosCuadrados: Number(editHabitacion.metrosCuadrados), estadoId: Number(editHabitacion.estadoId) })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeHabitacion = { tipo: 'ok', texto: 'Habitación actualizada.' };
      const tipoNombre = tiposHabitacion.find(t => t.id === Number(editHabitacion.tipoHabitacionId))?.nombre ?? '';
      const camaNombre = tiposCama.find(c => c.id === Number(editHabitacion.camaId))?.nombre ?? '';
      const estadoNombre = editHabitacion.estadoId == 1 ? 'Activa' : 'Cerrada';
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, ...editHabitacion, tipoHabitacion: tipoNombre, tipoCama: camaNombre, estado: estadoNombre } : h);
    } catch (e) {
      mensajeHabitacion = { tipo: 'error', texto: e.message };
    } finally {
      guardandoHabitacion = false;
    }
  }

  // ── Imágenes de habitación ────────────────────────

  async function subirImagenHabitacion(event) {
    const file = event.target.files[0];
    if (!file) return;
    subiendoImgHab = true;
    mensajeImgHab  = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}/imagenes`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64 })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: [...(h.imagenesIds ?? []), data.id] } : h);
      habitacionEditando = { ...habitacionEditando, imagenesIds: [...(habitacionEditando.imagenesIds ?? []), data.id] };
      mensajeImgHab = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) {
      mensajeImgHab = { tipo: 'error', texto: e.message };
    } finally {
      subiendoImgHab = false;
      event.target.value = '';
    }
  }

  async function eliminarImagenHabitacion(imagenId) {
    if (!confirm('¿Eliminar esta imagen?')) return;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      const nuevosIds = (habitacionEditando.imagenesIds ?? []).filter(id => id !== imagenId);
      habitacionEditando = { ...habitacionEditando, imagenesIds: nuevosIds };
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: nuevosIds } : h);
    } catch (e) {
      mensajeImgHab = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message };
    }
  }

  // ── Util ──────────────────────────────────────────

  function fileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload  = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }


  const reservasMock = [
    { id: 'RES-001', usuario: 'maria.g',  hotel: 'Grand Miku Paris',  checkIn: '2025-03-10', checkOut: '2025-03-15', monto: 'Q 2,250', estado: 'confirmada' },
    { id: 'RES-002', usuario: 'carlosm',  hotel: 'Tokio Miku Palace', checkIn: '2025-04-01', checkOut: '2025-04-07', monto: 'Q 4,060', estado: 'pendiente'  },
    { id: 'RES-003', usuario: 'sofiamt',  hotel: 'Miku NY Suite',     checkIn: '2025-02-20', checkOut: '2025-02-25', monto: 'Q 3,100', estado: 'confirmada' },
    { id: 'RES-004', usuario: 'luisp',    hotel: 'Miku Inn Londres',  checkIn: '2025-05-05', checkOut: '2025-05-08', monto: 'Q 960',   estado: 'cancelada'  },
    { id: 'RES-005', usuario: 'diegot',   hotel: 'Barcelona Miku',    checkIn: '2025-06-12', checkOut: '2025-06-16', monto: 'Q 1,160', estado: 'confirmada' },
    { id: 'RES-006', usuario: 'vale.r',   hotel: 'Miku Antigua',      checkIn: '2025-01-30', checkOut: '2025-02-02', monto: 'Q 540',   estado: 'cancelada'  },
  ];

  $: usuariosFiltrados = usuarios.filter(u => {
    const q = busquedaUsuario.toLowerCase();
    const matchBusqueda = q === '' || u.nombre.toLowerCase().includes(q) || u.apellido.toLowerCase().includes(q) || u.username.toLowerCase().includes(q) || u.correo.toLowerCase().includes(q);
    return matchBusqueda && (filtroRol === 'todos' || String(u.rolId) === filtroRol);
  });

  $: hotelesFiltrados = hoteles.filter(h => {
    const q = busquedaHotel.toLowerCase();
    const matchBusqueda = q === '' || h.nombre.toLowerCase().includes(q) || h.ciudad.toLowerCase().includes(q) || h.pais.toLowerCase().includes(q);
    const estadoNorm = h.estado?.toLowerCase() ?? '';
    return matchBusqueda && (filtroEstadoHotel === 'todos' || estadoNorm === filtroEstadoHotel);
  });

  function abrirCrearUsuario() {
    nuevoUsuario = { nombre: '', apellido: '', username: '', correo: '', pasaporte: '', telefono: '', fechaNacimiento: '', pais: '', ciudad: '', rolId: 1, contrasena: '' };
    showModalCrearUsuario = true;
  }

  function abrirEditarUsuario(u) {
    usuarioSeleccionado = u;
    editUsuario = { rolId: u.rolId };
    mensajeRol = null;
    showModalEditarUsuario = true;
  }

  async function guardarCambioRol() {
    if (!usuarioSeleccionado) return;
    guardandoRol = true;
    mensajeRol   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/usuarios/${usuarioSeleccionado.id}/rol`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rolId: editUsuario.rolId })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeRol = { tipo: 'ok', texto: 'Rol actualizado correctamente.' };
      // Reflejar el cambio en la lista local sin recargar todo
      usuarios = usuarios.map(u =>
        u.id === usuarioSeleccionado.id
          ? { ...u, rolId: editUsuario.rolId, rolNombre: editUsuario.rolId === 2 ? 'Administrador' : 'Usuario Registrado' }
          : u
      );
    } catch (e) {
      mensajeRol = { tipo: 'error', texto: e.message };
    } finally {
      guardandoRol = false;
    }
  }

  function abrirEliminarUsuario(u) {
    usuarioSeleccionado = u;
    showModalEliminarUsuario = true;
  }


  function cerrarModales() {
    showModalCrearUsuario = showModalEditarUsuario = showModalEliminarUsuario = false;
    showModalHabitacion = false;
    usuarioSeleccionado = hotelSeleccionado = null;
    habitacionEditando  = null;
  }

  // CORRECCIÓN 2: handler para cerrar overlay con teclado (accesibilidad)
  function handleOverlayKey(e) {
    if (e.key === 'Escape' || e.key === 'Enter' || e.key === ' ') cerrarModales();
  }

  function badge(estado) {
    const e = (estado ?? '').toLowerCase();
    if (e === 'activo'  || e === 'confirmada' || e === 'activa')    return 'badge--green';
    if (e === 'cerrado' || e === 'inactivo'   || e === 'cancelada') return 'badge--red';
    if (e === 'suspendido' || e === 'mantenimiento')                return 'badge--orange';
    if (e === 'pendiente')                                          return 'badge--yellow';
    return 'badge--gray';
  }


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
          {#if item.id === 'usuarios'}<span class="adm__nav-count">{usuarios.length}</span>
          {:else if item.id === 'hoteles'}<span class="adm__nav-count">{hoteles.length}</span>
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
              <h3 class="adm__card-title">Hoteles</h3>
              <button class="adm__link-btn" on:click={() => activeSection = 'hoteles'}>Ver todos →</button>
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

        <div class="adm__card">
          <div class="adm__card-header">
            <h3 class="adm__card-title">Usuarios Recientes</h3>
            <button class="adm__link-btn" on:click={() => activeSection = 'usuarios'}>Ver todos →</button>
          </div>
          <div class="adm__table-wrap">
            <table class="adm__table">
              <thead><tr><th>Nombre</th><th>Username</th><th>País</th><th>Rol</th><th>Ciudad</th></tr></thead>
              <tbody>
                {#each usuarios.slice(0, 5) as u}
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
                    <td>{u.pais ?? '—'}</td>
                    <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : 'badge--blue'}">{u.rolNombre}</span></td>
                    <td>{u.ciudad ?? '—'}</td>
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
          <button class="adm__btn adm__btn--ghost" on:click={cargarUsuarios} title="Recargar lista" aria-label="Recargar usuarios">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </button>
        </div>

        {#if cargandoUsuarios}
          <div class="adm__loading-state">
            <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            <p>Cargando usuarios...</p>
          </div>
        {:else if errorUsuarios}
          <div class="adm__error-state">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <p>{errorUsuarios}</p>
            <button class="adm__btn adm__btn--ghost" on:click={cargarUsuarios}>Reintentar</button>
          </div>
        {:else}

        <div class="adm__card adm__card--no-pad">
          <div class="adm__table-wrap">
            <table class="adm__table">
              <thead>
                <tr><th>Usuario</th><th>Correo</th><th>País</th><th>Rol</th><th>Ciudad</th><th>Nacimiento</th><th>Acciones</th></tr>
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
                    <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : 'badge--blue'}">{u.rolNombre}</span></td>
                    <td>{u.ciudad ?? '—'}</td>
                    <td>{u.fechaNacimiento ?? '—'}</td>
                    <td>
                      <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarUsuario(u)} title="Cambiar rol">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      </button>
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
        {/if}

      <!-- ═══ HOTELES ═══ -->
      {:else if activeSection === 'hoteles'}

        {#if vistaHoteles === 'lista'}
          <!-- ── VISTA LISTA ── -->
          <div class="adm__filters-bar">
            <div class="adm__search-wrap">
              <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              <input class="adm__search-input" type="text" bind:value={busquedaHotel} placeholder="Buscar hotel, ciudad, país..." />
            </div>
            <select class="adm__select" bind:value={filtroEstadoHotel}>
              <option value="todos">Todos los estados</option>
              <option value="activo">Activo</option>
              <option value="cerrado">Cerrado</option>
            </select>
            <span class="adm__count-label">{hotelesFiltrados.length} hotel(es)</span>
            <button class="adm__btn adm__btn--ghost" on:click={cargarHoteles} title="Recargar">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
            </button>
          </div>

          {#if cargandoHoteles}
            <div class="adm__loading-state">
              <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              <p>Cargando hoteles...</p>
            </div>
          {:else if errorHoteles}
            <div class="adm__error-state">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <p>{errorHoteles}</p>
              <button class="adm__btn adm__btn--ghost" on:click={cargarHoteles}>Reintentar</button>
            </div>
          {:else}
            <div class="adm__card adm__card--no-pad">
              <div class="adm__table-wrap">
                <table class="adm__table">
                  <thead>
                    <tr><th>Hotel</th><th>Ubicación</th><th>Dirección</th><th>Rating</th><th>Habitaciones</th><th>Estado</th><th>Acciones</th></tr>
                  </thead>
                  <tbody>
                    {#each hotelesFiltrados as h (h.id)}
                      <tr>
                        <td>
                          <div class="adm__hotel-mini">
                            <div class="adm__hotel-mini-thumb">
                              {#if h.imagenesIds?.length > 0}
                                <img src="{API_BASE}/imagenes/hotel/{h.imagenesIds[0]}" alt={h.nombre} />
                              {:else}<span>🏨</span>{/if}
                            </div>
                            <div>
                              <p class="adm__hotel-mini-name">{h.nombre}</p>
                              <p class="adm__hotel-mini-id">ID #{h.id}</p>
                            </div>
                          </div>
                        </td>
                        <td><p class="adm__hotel-city">{h.ciudad}</p><p class="adm__hotel-country">{h.pais}</p></td>
                        <td class="adm__hotel-address">{h.direccion ?? '—'}</td>
                        <td>
                          <div class="adm__rating-pill">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="#f0a030" stroke="#f0a030" stroke-width="1"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                            {h.rating?.toFixed(1) ?? '—'}
                          </div>
                        </td>
                        <td class="adm__table-center">{h.cantidadHabitaciones ?? '—'}</td>
                        <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                        <td>
                          <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirDetalleHotel(h)} title="Gestionar hotel">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                          </button>
                        </td>
                      </tr>
                    {/each}
                    {#if hotelesFiltrados.length === 0}
                      <tr><td colspan="7" class="adm__empty-cell">No se encontraron hoteles.</td></tr>
                    {/if}
                  </tbody>
                </table>
              </div>
            </div>
          {/if}

        {:else}
          <!-- ── VISTA DETALLE ── -->
          <div class="adm__detalle-header">
            <button class="adm__btn adm__btn--ghost" on:click={volverListaHoteles}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
              Volver a hoteles
            </button>
            <div class="adm__detalle-title">
              <h2>{hotelDetalle.nombre}</h2>
              <span class="adm__badge {badge(hotelDetalle.estado)}">{hotelDetalle.estado}</span>
              <span class="adm__detalle-loc">{hotelDetalle.ciudad}, {hotelDetalle.pais}</span>
            </div>
          </div>

          <!-- Tabs -->
          <div class="adm__tabs">
            <button class="adm__tab {tabDetalle === 'info' ? 'adm__tab--active' : ''}" on:click={() => tabDetalle = 'info'}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              Información
            </button>
            <button class="adm__tab {tabDetalle === 'imagenes' ? 'adm__tab--active' : ''}" on:click={() => tabDetalle = 'imagenes'}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              Imágenes ({hotelDetalle.imagenesIds?.length ?? 0})
            </button>
            <button class="adm__tab {tabDetalle === 'habitaciones' ? 'adm__tab--active' : ''}" on:click={() => tabDetalle = 'habitaciones'}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Habitaciones ({habitaciones.length})
            </button>
          </div>

          <!-- ── Tab: Información ── -->
          {#if tabDetalle === 'info'}
            <div class="adm__card adm__detalle-form-card">
              <div class="adm__form-grid">
                <div class="adm__field adm__field--full">
                  <label>Nombre del Hotel</label>
                  <input type="text" bind:value={editInfoHotel.nombre} />
                </div>
                <div class="adm__field adm__field--full">
                  <label>Dirección</label>
                  <input type="text" bind:value={editInfoHotel.direccion} />
                </div>
                <div class="adm__field">
                  <label>Rating (0–5)</label>
                  <input type="number" bind:value={editInfoHotel.rating} min="0" max="5" step="0.1" />
                </div>
                <div class="adm__field">
                  <label>Estado</label>
                  <select bind:value={editInfoHotel.estadoId}>
                    <option value={1}>Activo</option>
                    <option value={2}>Cerrado</option>
                  </select>
                </div>
                <div class="adm__field adm__field--full">
                  <label>Descripción</label>
                  <textarea bind:value={editInfoHotel.descripcion} rows="4"></textarea>
                </div>
              </div>

              {#if mensajeInfo}
                <div class="adm__feedback adm__feedback--{mensajeInfo.tipo}" style="margin-top:1rem">
                  {#if mensajeInfo.tipo === 'ok'}<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  {:else}<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>{/if}
                  {mensajeInfo.texto}
                </div>
              {/if}

              <div style="display:flex; justify-content:flex-end; margin-top:1.25rem;">
                <button class="adm__btn adm__btn--primary" on:click={guardarInfoHotel} disabled={guardandoInfo}>
                  {#if guardandoInfo}
                    <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                    Guardando...
                  {:else}
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    Guardar cambios
                  {/if}
                </button>
              </div>
            </div>

          <!-- ── Tab: Imágenes del hotel ── -->
          {:else if tabDetalle === 'imagenes'}
            <div class="adm__card">
              <div class="adm__img-section-header">
                <p class="adm__img-section-title">Imágenes del hotel</p>
                <label class="adm__btn adm__btn--primary adm__upload-btn">
                  {#if subiendoImgHotel}
                    <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                    Subiendo...
                  {:else}
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Agregar imagen
                  {/if}
                  <input type="file" accept="image/*" on:change={subirImagenHotel} disabled={subiendoImgHotel} style="display:none" />
                </label>
              </div>

              {#if mensajeImgHotel}
                <div class="adm__feedback adm__feedback--{mensajeImgHotel.tipo}" style="margin-bottom:1rem">
                  {mensajeImgHotel.texto}
                </div>
              {/if}

              {#if hotelDetalle.imagenesIds?.length > 0}
                <div class="adm__img-grid">
                  {#each hotelDetalle.imagenesIds as imgId (imgId)}
                    <div class="adm__img-card">
                      <img src="{API_BASE}/imagenes/hotel/{imgId}" alt="Imagen {imgId}" />
                      <button class="adm__img-delete" on:click={() => eliminarImagenHotel(imgId)} title="Eliminar imagen">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                      </button>
                    </div>
                  {/each}
                </div>
              {:else}
                <div class="adm__img-empty">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <p>Sin imágenes. Agrega la primera.</p>
                </div>
              {/if}
            </div>

          <!-- ── Tab: Habitaciones ── -->
          {:else if tabDetalle === 'habitaciones'}
            {#if cargandoHabitaciones}
              <div class="adm__loading-state">
                <svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                <p>Cargando habitaciones...</p>
              </div>
            {:else if errorHabitaciones}
              <div class="adm__error-state">
                <p>{errorHabitaciones}</p>
                <button class="adm__btn adm__btn--ghost" on:click={() => cargarHabitacionesDetalle(hotelDetalle.id)}>Reintentar</button>
              </div>
            {:else}
              <div class="adm__card adm__card--no-pad">
                <div class="adm__table-wrap">
                  <table class="adm__table">
                    <thead>
                      <tr><th>Tipo</th><th>Cama</th><th>Precio/Noche</th><th>Precio/Persona</th><th>Capacidad</th><th>m²</th><th>Estado</th><th>Imágenes</th><th>Acciones</th></tr>
                    </thead>
                    <tbody>
                      {#each habitaciones as h (h.id)}
                        <tr>
                          <td>
                            <p style="font-weight:600; color:var(--adm-text); margin:0 0 2px">{h.tipoHabitacion}</p>
                            <p style="font-size:.72rem; color:var(--adm-text-muted); margin:0">ID #{h.id}</p>
                          </td>
                          <td>{h.tipoCama}</td>
                          <td class="adm__table-money">Q {h.precioPorNoche?.toFixed(2)}</td>
                          <td class="adm__table-money">Q {h.precioPorPersona?.toFixed(2)}</td>
                          <td class="adm__table-center">{h.capacidadMaxima}</td>
                          <td class="adm__table-center">{h.metrosCuadrados}</td>
                          <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                          <td class="adm__table-center">{h.imagenesIds?.length ?? 0}</td>
                          <td>
                            <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarHabitacion(h)} title="Editar">
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                            </button>
                          </td>
                        </tr>
                      {/each}
                      {#if habitaciones.length === 0}
                        <tr><td colspan="9" class="adm__empty-cell">No hay habitaciones registradas.</td></tr>
                      {/if}
                    </tbody>
                  </table>
                </div>
              </div>
            {/if}
          {/if}
        {/if}

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
  <!-- Overlay -->
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>

  <!-- Modal -->
  <div class="adm__rol-modal">

    <!-- Header con gradiente -->
    <div class="adm__rol-modal__header">
      <div class="adm__rol-modal__avatar" style="background: {usuarioSeleccionado.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
        {usuarioSeleccionado.nombre.charAt(0)}
      </div>
      <div class="adm__rol-modal__user-info">
        <p class="adm__rol-modal__name">{usuarioSeleccionado.nombre} {usuarioSeleccionado.apellido}</p>
        <p class="adm__rol-modal__username">@{usuarioSeleccionado.username}</p>
        <p class="adm__rol-modal__email">{usuarioSeleccionado.correo}</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModales} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Body -->
    <div class="adm__rol-modal__body">
      <p class="adm__rol-modal__label">Seleccionar nuevo rol</p>

      <!-- Cards de selección -->
      <div class="adm__rol-cards">
        <button
          class="adm__rol-card {editUsuario.rolId === 1 ? 'adm__rol-card--active-user' : ''}"
          on:click={() => editUsuario.rolId = 1}
        >
          <div class="adm__rol-card__icon adm__rol-card__icon--user">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </div>
          <div class="adm__rol-card__text">
            <span class="adm__rol-card__title">Usuario Registrado</span>
            <span class="adm__rol-card__desc">Acceso estándar al sistema</span>
          </div>
          {#if editUsuario.rolId === 1}
            <div class="adm__rol-card__check">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
          {/if}
        </button>

        <button
          class="adm__rol-card {editUsuario.rolId === 2 ? 'adm__rol-card--active-admin' : ''}"
          on:click={() => editUsuario.rolId = 2}
        >
          <div class="adm__rol-card__icon adm__rol-card__icon--admin">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          </div>
          <div class="adm__rol-card__text">
            <span class="adm__rol-card__title">Administrador</span>
            <span class="adm__rol-card__desc">Acceso total al panel</span>
          </div>
          {#if editUsuario.rolId === 2}
            <div class="adm__rol-card__check">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
          {/if}
        </button>
      </div>

      <!-- Feedback -->
      {#if mensajeRol}
        <div class="adm__feedback adm__feedback--{mensajeRol.tipo}">
          {#if mensajeRol.tipo === 'ok'}
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {/if}
          {mensajeRol.texto}
        </div>
      {/if}
    </div>

    <!-- Footer -->
    <div class="adm__rol-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={guardarCambioRol} disabled={guardandoRol}>
        {#if guardandoRol}
          <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
          Guardando...
        {:else}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          Guardar cambio
        {/if}
      </button>
    </div>

  </div>
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

{#if showModalHabitacion && habitacionEditando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>

  <!-- Modal editar habitación -->
  <div class="adm__hotel-modal adm__hotel-modal--wide">

    <!-- Header -->
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b; font-size:1.4rem; display:flex; align-items:center; justify-content:center;">
        {#if habitacionEditando.imagenesIds?.length > 0}
          <img src="{API_BASE}/imagenes/habitacion/{habitacionEditando.imagenesIds[0]}" alt="hab" />
        {:else}<span>&#x1F6CF;</span>{/if}
      </div>
      <div class="adm__hotel-modal__info">
        <p class="adm__hotel-modal__name">{habitacionEditando.tipoHabitacion} — ID #{habitacionEditando.id}</p>
        <p class="adm__hotel-modal__loc">{habitacionEditando.tipoCama} &middot; {habitacionEditando.capacidadMaxima} persona(s) &middot; {habitacionEditando.metrosCuadrados} m&sup2;</p>
      </div>
      <button class="adm__rol-modal__close" on:click={cerrarModales} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Body -->
    <div class="adm__hotel-modal__body">

      <!-- Datos -->
      <p class="adm__modal-section-title">Datos de la habitacion</p>
      <div class="adm__form-grid">
        <div class="adm__field">
          <label>Tipo de Habitacion</label>
          <select bind:value={editHabitacion.tipoHabitacionId}>
            {#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}
          </select>
        </div>
        <div class="adm__field">
          <label>Tipo de Cama</label>
          <select bind:value={editHabitacion.camaId}>
            {#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}
          </select>
        </div>
        <div class="adm__field">
          <label>Precio por Noche (Q)</label>
          <input type="number" bind:value={editHabitacion.precioPorNoche} min="0" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Precio por Persona (Q)</label>
          <input type="number" bind:value={editHabitacion.precioPorPersona} min="0" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Capacidad Maxima</label>
          <input type="number" bind:value={editHabitacion.capacidadMaxima} min="1" />
        </div>
        <div class="adm__field">
          <label>Metros Cuadrados</label>
          <input type="number" bind:value={editHabitacion.metrosCuadrados} min="0" step="0.1" />
        </div>
        <div class="adm__field">
          <label>Estado</label>
          <select bind:value={editHabitacion.estadoId}>
            <option value={1}>Activa</option>
            <option value={2}>Cerrada</option>
          </select>
        </div>
        <div class="adm__field adm__field--full">
          <label>Descripcion</label>
          <textarea bind:value={editHabitacion.descripcion} rows="3" placeholder="Descripcion de la habitacion..."></textarea>
        </div>
      </div>

      {#if mensajeHabitacion}
        <div class="adm__feedback adm__feedback--{mensajeHabitacion.tipo}" style="margin:.75rem 0">
          {#if mensajeHabitacion.tipo === 'ok'}<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>{/if}
          {mensajeHabitacion.texto}
        </div>
      {/if}

      <div style="display:flex; justify-content:flex-end; margin-bottom:1.5rem;">
        <button class="adm__btn adm__btn--primary" on:click={guardarHabitacion} disabled={guardandoHabitacion}>
          {#if guardandoHabitacion}
            <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Guardando...
          {:else}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>Guardar cambios
          {/if}
        </button>
      </div>

      <!-- Imagenes -->
      <div class="adm__modal-section-divider"></div>
      <div class="adm__img-section-header" style="margin-top:1rem">
        <p class="adm__modal-section-title" style="margin:0">Imagenes de la habitacion</p>
        <label class="adm__btn adm__btn--ghost adm__upload-btn">
          {#if subiendoImgHab}
            <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Subiendo...
          {:else}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>Agregar imagen
          {/if}
          <input type="file" accept="image/*" on:change={subirImagenHabitacion} disabled={subiendoImgHab} style="display:none" />
        </label>
      </div>

      {#if mensajeImgHab}
        <div class="adm__feedback adm__feedback--{mensajeImgHab.tipo}" style="margin:.5rem 0">
          {mensajeImgHab.texto}
        </div>
      {/if}

      {#if habitacionEditando.imagenesIds?.length > 0}
        <div class="adm__img-grid adm__img-grid--sm" style="margin-top:.75rem">
          {#each habitacionEditando.imagenesIds as imgId (imgId)}
            <div class="adm__img-card">
              <img src="{API_BASE}/imagenes/habitacion/{imgId}" alt="hab {imgId}" />
              <button class="adm__img-delete" on:click={() => eliminarImagenHabitacion(imgId)} title="Eliminar">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          {/each}
        </div>
      {:else}
        <div class="adm__img-empty" style="padding:1.5rem 0">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
          <p>Sin imagenes.</p>
        </div>
      {/if}

    </div>

    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cerrar</button>
    </div>

  </div>
{/if}