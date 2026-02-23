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

  // ── Métricas del dashboard (datos reales) ─────────────────────────────────
  let metricas = null;
  let cargandoMetricas = false;

  async function cargarMetricas() {
    cargandoMetricas = true;
    try {
      const res = await fetch(`${API_BASE}/admin/metricas`, { credentials: 'include' });
      if (res.ok) metricas = await res.json();
    } catch (e) { /* silencioso */ }
    finally { cargandoMetricas = false; }
  }

  $: statsData = metricas ? [
    { label: 'Usuarios Totales', value: metricas.totalUsuarios.toLocaleString('es-GT'),    icon: 'users',    color: 'blue'   },
    { label: 'Reservas Confirmadas', value: metricas.reservasActivas.toLocaleString('es-GT'), icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',  value: metricas.hotelesActivos.toLocaleString('es-GT'),   icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos Totales', value: '$ ' + (metricas.ingresosTotales ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 }), icon: 'money', color: 'amber' },
  ] : [
    { label: 'Usuarios Totales',     value: '—', icon: 'users',    color: 'blue'   },
    { label: 'Reservas Confirmadas', value: '—', icon: 'calendar', color: 'green'  },
    { label: 'Hoteles Activos',      value: '—', icon: 'hotel',    color: 'purple' },
    { label: 'Ingresos Totales',     value: '—', icon: 'money',    color: 'amber'  },
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
    cargarCatalogos();
    cargarMetricas();
    cargarReservas();
    cargarConteosReservas();
    cargarAgencias();
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
  let tabDetalle     = 'info';    // 'info' | 'imagenes' | 'amenidades' | 'habitaciones'

  // Amenidades del hotel en detalle
  let amenidades            = [];        // catálogo (12 amenidades)
  let amenidadesHotel       = [];        // las asignadas al hotel actual
  let cargandoAmenidades    = false;
  let mensajeAmenidad       = null;
  let showFormAmenidad      = false;
  let nuevaAmenidad         = { amenidadId: 1, descripcion: '' };
  let amenidadEditandoId    = null;      // ID en HotelAmenidad que se está editando
  let editDescAmenidad      = '';
  let subiendoImgAmenidad   = false;
  let mensajeImgAmenidad    = null;

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
    hotelDetalle  = { ...h };
    editInfoHotel = { nombre: h.nombre ?? '', direccion: h.direccion ?? '', descripcion: h.descripcion ?? '', rating: h.rating ?? 0, estadoId: h.estadoId ?? 1 };
    tabDetalle    = 'info';
    mensajeInfo   = null;
    vistaHoteles  = 'detalle';
    amenidadesHotel = [];
    cargarHabitacionesDetalle(h.id);
    cargarAmenidadesHotel(h.id);
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

  // ── Amenidades del hotel ──────────────────────────

  async function cargarAmenidadesHotel(hotelId) {
    cargandoAmenidades = true;
    mensajeAmenidad    = null;
    try {
      const [rA, rH] = await Promise.all([
        fetch(`${API_BASE}/admin/amenidades`, { credentials: 'include' }),
        fetch(`${API_BASE}/admin/hoteles/${hotelId}/amenidades`, { credentials: 'include' }),
      ]);
      if (rA.ok) amenidades = await rA.json();
      if (rH.ok) amenidadesHotel = await rH.json();
    } catch(e) { /* silencioso */ }
    finally { cargandoAmenidades = false; }
  }

  async function agregarAmenidadHotel() {
    mensajeAmenidad = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/amenidades`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      const catAm = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId));
      amenidadesHotel = [...amenidadesHotel, { id: data.id, hotelId: hotelDetalle.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: catAm?.nombre ?? '', descripcion: nuevaAmenidad.descripcion, imagenesIds: [] }];
      mensajeAmenidad  = { tipo: 'ok', texto: 'Amenidad agregada.' };
      showFormAmenidad = false;
      nuevaAmenidad    = { amenidadId: 1, descripcion: '' };
    } catch(e) {
      mensajeAmenidad = { tipo: 'error', texto: e.message };
    }
  }

  async function guardarDescAmenidad(ha) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${ha.id}`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amenidadId: ha.amenidadId, descripcion: editDescAmenidad })
      });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === ha.id ? { ...a, descripcion: editDescAmenidad } : a);
      amenidadEditandoId = null;
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  async function eliminarAmenidadHotel(haId) {
    if (!confirm('¿Eliminar esta amenidad?')) return;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.filter(a => a.id !== haId);
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  async function subirImagenAmenidad(event, haId) {
    const file = event.target.files[0];
    if (!file) return;
    subiendoImgAmenidad = true;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}/imagenes`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64 })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === haId ? { ...a, imagenesIds: [...(a.imagenesIds ?? []), data.id] } : a);
    } catch(e) { mensajeImgAmenidad = { tipo: 'error', texto: e.message }; }
    finally { subiendoImgAmenidad = false; event.target.value = ''; }
  }

  async function eliminarImagenAmenidad(haId, imgId) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === haId ? { ...a, imagenesIds: (a.imagenesIds ?? []).filter(i => i !== imgId) } : a);
    } catch(e) { /* silencioso */ }
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


  // ════════════════════════════════════════════════════
  //  CREAR HOTEL — estado
  // ════════════════════════════════════════════════════
  let pasos            = ['info', 'imagenes', 'habitaciones'];
  let pasoActual       = 'info';   // 'info' | 'imagenes' | 'habitaciones'
  let hotelCreadoId    = null;     // ID devuelto tras crear el hotel
  let hotelCreadoNombre = '';

  // Catálogos
  let paises   = [];
  let ciudades = [];

  // Paso 1 — info
  let nuevoHotel = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' };

  // ── Autocomplete de país y ciudad del wizard (mismo mecanismo que Register) ──
  let todosLosPaisesWizard   = [];   // [{ country, cities[] }] de countriesnow
  let wizardPaisQuery        = '';
  let wizardPaisesSugeridos  = [];
  let wizardPaisSeleccionado = null; // { country, cities[] }
  let wizardPaisError        = '';
  let wizardCiudadesSugeridas  = [];
  let wizardCiudadSeleccionada = false;
  let wizardCiudadError        = '';

  let guardandoNuevoHotel = false;
  let mensajeNuevoHotel   = null;

  // Paso 2 — imágenes del hotel
  let imagenesNuevoHotel   = [];   // [{ id, base64Preview }]
  let subiendoImgNuevoHotel = false;
  let mensajeImgNuevo      = null;

  // Paso 2 — amenidades del nuevo hotel
  let amenidadesNuevoHotel = [];  // [{id (HotelAmenidad), amenidadId, amenidadNombre, descripcion}]

  // Paso 3 — habitaciones
  let habitacionesNuevas   = [];   // habitaciones ya creadas en BD [{id, tipoHabitacion, ...}]
  let showFormHabNueva     = false;
  let nuevaHabitacion      = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1 };
  let guardandoNuevaHab    = false;
  let mensajeNuevaHab      = null;
  let imagenesHabNueva       = {};   // { habitacionId: [{id, preview}, ...] }
  let subiendoImgHabNuevaSet = new Set(); // Set de habitacionIds que están subiendo

  // Filtro ciudad por país seleccionado

  async function cargarCatalogos() {
    try {
      const [rP, rC, rA] = await Promise.all([
        fetch(`${API_BASE}/admin/paises`,    { credentials: 'include' }),
        fetch(`${API_BASE}/admin/ciudades`,  { credentials: 'include' }),
        fetch(`${API_BASE}/admin/amenidades`,{ credentials: 'include' }),
      ]);
      if (rP.ok) paises    = await rP.json();
      if (rC.ok) ciudades  = await rC.json();
      if (rA.ok) amenidades = await rA.json();
    } catch (e) { /* silencioso */ }
  }

  // Carga países de la API externa (igual que Register)
  async function cargarPaisesWizard() {
    if (todosLosPaisesWizard.length > 0) return; // ya cargados
    try {
      const res  = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await res.json();
      todosLosPaisesWizard = data.data; // [{ country, cities[] }]
    } catch (e) { /* silencioso */ }
  }

  function onWizardPaisInput() {
    wizardPaisSeleccionado = null;
    wizardPaisError        = '';
    nuevoHotel.ciudadNombre  = '';
    wizardCiudadesSugeridas  = [];
    wizardCiudadSeleccionada = false;
    const q = wizardPaisQuery.toLowerCase();
    wizardPaisesSugeridos = q.length < 2
      ? []
      : todosLosPaisesWizard.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6);
  }

  function seleccionarWizardPais(p) {
    wizardPaisSeleccionado  = p;
    wizardPaisQuery         = p.country;
    wizardPaisesSugeridos   = [];
    wizardPaisError         = '';
    nuevoHotel.ciudadNombre  = '';
    wizardCiudadesSugeridas  = [];
    wizardCiudadSeleccionada = false;
  }

  function validarWizardPais() {
    if (wizardPaisQuery && !wizardPaisSeleccionado) {
      wizardPaisError  = 'Selecciona un país de la lista.';
      wizardPaisQuery  = '';
    }
  }

  function onWizardCiudadInput() {
    wizardCiudadSeleccionada = false;
    wizardCiudadError        = '';
    if (!wizardPaisSeleccionado) return;
    const q = nuevoHotel.ciudadNombre.toLowerCase();
    wizardCiudadesSugeridas = q.length < 2
      ? []
      : (wizardPaisSeleccionado.cities ?? []).filter(c => c.toLowerCase().includes(q)).slice(0, 6);
  }

  function seleccionarWizardCiudad(c) {
    nuevoHotel.ciudadNombre  = c;
    wizardCiudadesSugeridas  = [];
    wizardCiudadSeleccionada = true;
    wizardCiudadError        = '';
  }

  function validarWizardCiudad() {
    // La ciudad puede ser nueva (no forzamos selección de lista)
    wizardCiudadesSugeridas = [];
  }

  function resetWizardPaisCiudad() {
    wizardPaisQuery          = '';
    wizardPaisSeleccionado   = null;
    wizardPaisError          = '';
    wizardPaisesSugeridos    = [];
    wizardCiudadesSugeridas  = [];
    wizardCiudadSeleccionada = false;
    wizardCiudadError        = '';
  }

    function resetCrearHotel() {
    pasoActual          = 'info';
    hotelCreadoId       = null;
    hotelCreadoNombre   = '';
    nuevoHotel          = { nombre: '', direccion: '', descripcion: '', rating: 3.0, estadoId: 1, ciudadNombre: '' };
    mensajeNuevoHotel   = null;
    imagenesNuevoHotel  = [];
    mensajeImgNuevo     = null;
    amenidadesNuevoHotel = [];
    habitacionesNuevas  = [];
    showFormHabNueva    = false;
    showFormAmenidad    = false;
    nuevaHabitacion     = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1 };
    nuevaAmenidad       = { amenidadId: 1, descripcion: '' };
    mensajeNuevaHab     = null;
    mensajeAmenidad     = null;
    imagenesHabNueva    = {};
    subiendoImgHabNuevaSet = new Set();
    resetWizardPaisCiudad();
  }

  // ── Paso 1: Crear hotel ───────────────────────────

  async function crearNuevoHotel() {
    if (!nuevoHotel.nombre.trim()) { mensajeNuevoHotel = { tipo: 'error', texto: 'El nombre es obligatorio.' }; return; }
    if (!wizardPaisSeleccionado)   { wizardPaisError = 'Selecciona un país de la lista.'; return; }
    if (!nuevoHotel.ciudadNombre.trim()) { mensajeNuevoHotel = { tipo: 'error', texto: 'El nombre de la ciudad es obligatorio.' }; return; }

    guardandoNuevoHotel = true;
    mensajeNuevoHotel   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:      nuevoHotel.nombre.trim(),
          direccion:   nuevoHotel.direccion.trim(),
          descripcion: nuevoHotel.descripcion.trim(),
          rating:      Number(nuevoHotel.rating),
          estadoId:    Number(nuevoHotel.estadoId),
          ciudad:      nuevoHotel.ciudadNombre.trim(),
          paisNombre:  wizardPaisSeleccionado.country,
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      hotelCreadoId     = data.id;
      hotelCreadoNombre = nuevoHotel.nombre;
      mensajeNuevoHotel = { tipo: 'ok', texto: `Hotel creado con ID #${data.id}. Ahora agrega imágenes.` };
      pasoActual = 'contenido';
      await cargarHoteles(); // refresca la lista de gestión
    } catch (e) {
      mensajeNuevoHotel = { tipo: 'error', texto: e.message };
    } finally {
      guardandoNuevoHotel = false;
    }
  }

  // ── Paso 2: Imágenes del hotel ────────────────────

  async function subirImagenNuevoHotel(event) {
    const file = event.target.files[0];
    if (!file || !hotelCreadoId) return;
    subiendoImgNuevoHotel = true;
    mensajeImgNuevo       = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/imagenes`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64 })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      imagenesNuevoHotel = [...imagenesNuevoHotel, { id: data.id, preview: base64 }];
      mensajeImgNuevo = { tipo: 'ok', texto: 'Imagen subida.' };
    } catch (e) {
      mensajeImgNuevo = { tipo: 'error', texto: e.message };
    } finally {
      subiendoImgNuevoHotel = false;
      event.target.value = '';
    }
  }

  async function eliminarImgNuevoHotel(imgId) {
    if (!confirm('¿Eliminar esta imagen?')) return;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      imagenesNuevoHotel = imagenesNuevoHotel.filter(i => i.id !== imgId);
    } catch (e) {
      mensajeImgNuevo = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message };
    }
  }

  async function agregarAmenidadNuevoHotel() {
    if (!hotelCreadoId) return;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/amenidades`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      const catAm = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId));
      amenidadesNuevoHotel = [...amenidadesNuevoHotel, { id: data.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: catAm?.nombre ?? '', descripcion: nuevaAmenidad.descripcion }];
      showFormAmenidad = false;
      nuevaAmenidad    = { amenidadId: 1, descripcion: '' };
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  async function eliminarAmenidadHotelById(haId) {
    try {
      await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}`, { method: 'DELETE', credentials: 'include' });
      amenidadesNuevoHotel = amenidadesNuevoHotel.filter(a => a.id !== haId);
    } catch(e) { /* silencioso */ }
  }

  // ── Paso 3: Habitaciones ──────────────────────────

  async function crearNuevaHabitacion() {
    if (!hotelCreadoId) return;
    guardandoNuevaHab = true;
    mensajeNuevaHab   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelCreadoId}/habitaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tipoHabitacionId: Number(nuevaHabitacion.tipoHabitacionId),
          camaId:           Number(nuevaHabitacion.camaId),
          precioPorPersona: Number(nuevaHabitacion.precioPorPersona),
          precioPorNoche:   Number(nuevaHabitacion.precioPorNoche),
          capacidadMaxima:  Number(nuevaHabitacion.capacidadMaxima),
          metrosCuadrados:  Number(nuevaHabitacion.metrosCuadrados),
          descripcion:      nuevaHabitacion.descripcion,
          estadoId:         Number(nuevaHabitacion.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);

      const tipoNom  = tiposHabitacion.find(t => t.id === Number(nuevaHabitacion.tipoHabitacionId))?.nombre ?? '';
      const camaNom  = tiposCama.find(c => c.id === Number(nuevaHabitacion.camaId))?.nombre ?? '';
      habitacionesNuevas = [...habitacionesNuevas, { ...nuevaHabitacion, id: data.id, tipoHabitacion: tipoNom, tipoCama: camaNom, imagenesIds: [] }];
      imagenesHabNueva = { ...imagenesHabNueva, [data.id]: [] };

      mensajeNuevaHab  = { tipo: 'ok', texto: `Habitación ${tipoNom} creada (ID #${data.id}).` };
      showFormHabNueva = false;
      nuevaHabitacion  = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1 };
    } catch (e) {
      mensajeNuevaHab = { tipo: 'error', texto: e.message };
    } finally {
      guardandoNuevaHab = false;
    }
  }

  async function subirImagenHabNueva(event, habitacionId) {
    const file = event.target.files[0];
    if (!file) return;
    subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet, habitacionId]);
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionId}/imagenes`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ base64 })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(`Error ${res.status}`);
      imagenesHabNueva = { ...imagenesHabNueva, [habitacionId]: [...(imagenesHabNueva[habitacionId] ?? []), { id: data.id, preview: base64 }] };
    } catch (e) { /* silencioso */ }
    finally {
      subiendoImgHabNuevaSet = new Set([...subiendoImgHabNuevaSet].filter(id => id !== habitacionId));
      event.target.value = '';
    }
  }

  async function eliminarImgHabNueva(habitacionId, imgId) {
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      imagenesHabNueva = { ...imagenesHabNueva, [habitacionId]: imagenesHabNueva[habitacionId].filter(i => i.id !== imgId) };
    } catch(e) { /* silencioso */ }
  }

  // ── Añadir habitación desde GESTIÓN ──────────────

  let showModalNuevaHabGestion = false;
  let nuevaHabGestion = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1 };
  let guardandoNuevaHabGestion = false;
  let mensajeNuevaHabGestion   = null;

  function abrirModalNuevaHabGestion() {
    nuevaHabGestion = { tipoHabitacionId: 1, camaId: 1, precioPorPersona: 0, precioPorNoche: 0, capacidadMaxima: 2, metrosCuadrados: 25, descripcion: '', estadoId: 1 };
    mensajeNuevaHabGestion = null;
    showModalNuevaHabGestion = true;
  }

  async function crearHabGestion() {
    if (!hotelDetalle) return;
    guardandoNuevaHabGestion = true;
    mensajeNuevaHabGestion   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/habitaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tipoHabitacionId: Number(nuevaHabGestion.tipoHabitacionId),
          camaId:           Number(nuevaHabGestion.camaId),
          precioPorPersona: Number(nuevaHabGestion.precioPorPersona),
          precioPorNoche:   Number(nuevaHabGestion.precioPorNoche),
          capacidadMaxima:  Number(nuevaHabGestion.capacidadMaxima),
          metrosCuadrados:  Number(nuevaHabGestion.metrosCuadrados),
          descripcion:      nuevaHabGestion.descripcion,
          estadoId:         Number(nuevaHabGestion.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);

      const tipoNom = tiposHabitacion.find(t => t.id === Number(nuevaHabGestion.tipoHabitacionId))?.nombre ?? '';
      const camaNom = tiposCama.find(c => c.id === Number(nuevaHabGestion.camaId))?.nombre ?? '';
      const estNom  = nuevaHabGestion.estadoId == 1 ? 'Activa' : 'Cerrada';
      habitaciones  = [...habitaciones, { ...nuevaHabGestion, id: data.id, tipoHabitacion: tipoNom, tipoCama: camaNom, estado: estNom, imagenesIds: [] }];
      mensajeNuevaHabGestion = { tipo: 'ok', texto: `Habitación creada correctamente (ID #${data.id}).` };
    } catch (e) {
      mensajeNuevaHabGestion = { tipo: 'error', texto: e.message };
    } finally {
      guardandoNuevaHabGestion = false;
    }
  }

  // ── Reservaciones reales ──────────────────────────────────────────────────
  let reservas          = [];
  let cargandoReservas  = false;
  let errorReservas     = null;

  // Filtros — se envían al backend, no se filtran en el frontend
  let busquedaReserva     = '';
  let filtroEstadoReserva = 'todos';

  // Conteos totales para las tarjetas (siempre sin filtro)
  let conteoReservas = { confirmada: 0, pendiente: 0, cancelada: 0, total: 0 };

  // Debounce para búsqueda de texto
  let debounceTimer = null;

  // Modal cancelar
  let showModalCancelarReserva = false;
  let reservaCancelando        = null;
  let motivoCancelacion        = '';
  let cancelando               = false;
  let mensajeCancelar          = null;

  async function cargarReservas() {
    cargandoReservas = true;
    errorReservas    = null;
    try {
      const params = new URLSearchParams();
      if (filtroEstadoReserva && filtroEstadoReserva !== 'todos') params.set('estado', filtroEstadoReserva);
      if (busquedaReserva.trim()) params.set('busqueda', busquedaReserva.trim());

      const url = `${API_BASE}/admin/reservaciones${params.toString() ? '?' + params.toString() : ''}`;
      const res = await fetch(url, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      reservas = await res.json();
    } catch (e) {
      errorReservas = 'No se pudieron cargar las reservas. ' + e.message;
    } finally {
      cargandoReservas = false;
    }
  }

  async function cargarConteosReservas() {
    try {
      const res = await fetch(`${API_BASE}/admin/reservaciones`, { credentials: 'include' });
      if (!res.ok) return;
      const todas = await res.json();
      conteoReservas = {
        confirmada: todas.filter(r => r.estado === 'confirmada').length,
        pendiente:  todas.filter(r => r.estado === 'pendiente').length,
        cancelada:  todas.filter(r => r.estado === 'cancelada').length,
        total:      todas.length
      };
    } catch (e) { /* silencioso */ }
  }

  // Cuando cambia el filtro de estado, recarga inmediatamente
  function onFiltroEstadoCambia() {
    cargarReservas();
  }

  // Cuando cambia la búsqueda, espera 400ms antes de disparar
  function onBusquedaCambia() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => cargarReservas(), 400);
  }

  function abrirModalCancelar(r) {
    reservaCancelando    = r;
    motivoCancelacion    = '';
    mensajeCancelar      = null;
    showModalCancelarReserva = true;
  }

  function cerrarModalCancelar() {
    showModalCancelarReserva = false;
    reservaCancelando    = null;
    motivoCancelacion    = '';
    mensajeCancelar      = null;
    cancelando           = false;
  }

  async function confirmarCancelacion() {
    if (!reservaCancelando) return;
    cancelando      = true;
    mensajeCancelar = null;
    try {
      const res = await fetch(
        `${API_BASE}/admin/reservaciones/${reservaCancelando.id}/cancelar`,
        {
          method: 'PATCH',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ motivo: motivoCancelacion || 'Cancelada por administrador' })
        }
      );
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      // Recargar desde backend para reflejar el cambio real
      await cargarReservas();
      await cargarConteosReservas();
      cargarMetricas();
      cerrarModalCancelar();
    } catch (e) {
      mensajeCancelar = e.message;
    } finally {
      cancelando = false;
    }
  }

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
      const rolNombres = { 1: 'Usuario Registrado', 2: 'Administrador', 3: 'Webservice' };
      usuarios = usuarios.map(u =>
        u.id === usuarioSeleccionado.id
          ? { ...u, rolId: editUsuario.rolId, rolNombre: rolNombres[editUsuario.rolId] ?? 'Usuario Registrado' }
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



  // ════════════════════════════════════════════════════
  //  AGENCIAS (admin)
  // ════════════════════════════════════════════════════
  let agencias          = [];
  let cargandoAgencias  = false;
  let errorAgencias     = null;
  let busquedaAgencia   = '';
  let showModalEditarAgencia = false;
  let agenciaEditando        = null;
  let editAgencia            = { nombre: '', correo: '', porcentajeDescuento: 0, estadoId: 1 };
  let guardandoAgencia       = false;
  let mensajeAgencia         = null;

  $: agenciasFiltradas = agencias.filter(a =>
    a.nombre?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    a.correo?.toLowerCase().includes(busquedaAgencia.toLowerCase()) ||
    String(a.id).includes(busquedaAgencia)
  );

  async function cargarAgencias() {
    cargandoAgencias = true;
    errorAgencias    = null;
    try {
      const res = await fetch(`${API_BASE}/admin/agencias`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      agencias = await res.json();
    } catch (e) {
      errorAgencias = 'No se pudo cargar la lista de agencias. ' + e.message;
    } finally {
      cargandoAgencias = false;
    }
  }

  function abrirEditarAgencia(ag) {
    agenciaEditando = ag;
    editAgencia = {
      nombre:              ag.nombre              ?? '',
      correo:              ag.correo              ?? '',
      porcentajeDescuento: ag.porcentajeDescuento ?? 0,
      estadoId:            ag.estadoId            ?? 1,
    };
    mensajeAgencia         = null;
    showModalEditarAgencia = true;
  }

  function cerrarModalAgencia() {
    showModalEditarAgencia = false;
    agenciaEditando        = null;
    mensajeAgencia         = null;
  }

  async function guardarAgencia() {
    if (!editAgencia.nombre.trim()) {
      mensajeAgencia = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    guardandoAgencia = true;
    mensajeAgencia   = null;
    try {
      const res = await fetch(`${API_BASE}/admin/agencias/${agenciaEditando.id}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              editAgencia.nombre.trim(),
          correo:              editAgencia.correo.trim(),
          porcentajeDescuento: Number(editAgencia.porcentajeDescuento),
          estadoId:            Number(editAgencia.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);
      agencias = agencias.map(a =>
        a.id === agenciaEditando.id
          ? { ...a, ...editAgencia, estado: Number(editAgencia.estadoId) === 1 ? 'Activo' : 'Cerrado' }
          : a
      );
      mensajeAgencia = { tipo: 'ok', texto: 'Agencia actualizada correctamente.' };
      setTimeout(cerrarModalAgencia, 1200);
    } catch (e) {
      mensajeAgencia = { tipo: 'error', texto: e.message };
    } finally {
      guardandoAgencia = false;
    }
  }

  const navItems = [
    { id: 'dashboard',    label: 'Dashboard',     icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { id: 'usuarios',     label: 'Usuarios',       icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z' },
    { id: 'hoteles',      label: 'Hoteles',        icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    { id: 'crear-hotel',  label: 'Crear Hotel',    icon: 'M12 4v16m8-8H4' },
    { id: 'reservas',     label: 'Reservas',       icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { id: 'agencias',     label: 'Agencias',       icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4' },
    { id: 'reportes',     label: 'Reportes',       icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
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
        <button class="adm__nav-btn" class:adm__nav-btn--active={activeSection === item.id} on:click={() => { activeSection = item.id; if (item.id === 'crear-hotel') cargarPaisesWizard(); }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d={item.icon} />
          </svg>
          {item.label}
          {#if item.id === 'usuarios'}<span class="adm__nav-count">{usuarios.length}</span>
          {:else if item.id === 'hoteles'}<span class="adm__nav-count">{hoteles.length}</span>
          {:else if item.id === 'reservas'}<span class="adm__nav-count">{reservas.length}</span>
          {:else if item.id === 'agencias'}<span class="adm__nav-count">{agencias.length}</span>
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
                <span class="adm__stat-change">{stat.change ?? ''}</span>
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
            <option value="3">Webservice</option>
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
                        <div class="adm__user-mini-avatar" style="background: {u.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : u.rolId === 3 ? 'linear-gradient(135deg,#8b5cf6,#6d28d9)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
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
                    <td><span class="adm__badge {u.rolId === 2 ? 'badge--amber' : u.rolId === 3 ? 'badge--purple' : 'badge--blue'}">{u.rolNombre}</span></td>
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
            <button class="adm__tab {tabDetalle === 'amenidades' ? 'adm__tab--active' : ''}" on:click={() => tabDetalle = 'amenidades'}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              Amenidades ({amenidadesHotel.length})
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

          <!-- ── Tab: Amenidades ── -->
          {:else if tabDetalle === 'amenidades'}
            <!-- Barra acciones -->
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
              <span style="color:var(--adm-text-muted); font-size:.85rem">{amenidadesHotel.length} amenidad(es)</span>
              <button class="adm__btn adm__btn--primary" on:click={() => { showFormAmenidad = true; mensajeAmenidad = null; }}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Agregar Amenidad
              </button>
            </div>

            <!-- Formulario nueva amenidad -->
            {#if showFormAmenidad}
              <div class="adm__wizard-subcard" style="margin-bottom:1rem">
                <p class="adm__modal-section-title">Nueva Amenidad</p>
                <div class="adm__form-grid">
                  <div class="adm__field">
                    <label>Tipo de Amenidad</label>
                    <select bind:value={nuevaAmenidad.amenidadId}>
                      {#each amenidades.filter(a => !amenidadesHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}
                    </select>
                  </div>
                  <div class="adm__field adm__field--full">
                    <label>Descripción</label>
                    <textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Ej: WiFi de alta velocidad en todas las áreas..."></textarea>
                  </div>
                </div>
                <div style="display:flex; gap:.75rem; justify-content:flex-end; margin-top:.75rem">
                  <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button>
                  <button class="adm__btn adm__btn--primary" on:click={agregarAmenidadHotel}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>Agregar
                  </button>
                </div>
              </div>
            {/if}

            {#if mensajeAmenidad}
              <div class="adm__feedback adm__feedback--{mensajeAmenidad.tipo}" style="margin-bottom:1rem">{mensajeAmenidad.texto}</div>
            {/if}

            <!-- Lista de amenidades -->
            {#if cargandoAmenidades}
              <div class="adm__loading-state"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
            {:else if amenidadesHotel.length === 0 && !showFormAmenidad}
              <div class="adm__img-empty" style="padding:2.5rem 0">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                <p>Este hotel no tiene amenidades. Agrega la primera.</p>
              </div>
            {:else}
              <div style="display:flex; flex-direction:column; gap:.75rem">
                {#each amenidadesHotel as ha (ha.id)}
                  <div class="adm__amenidad-card">
                    <!-- Encabezado -->
                    <div class="adm__amenidad-header">
                      <div class="adm__amenidad-icon">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                      </div>
                      <div style="flex:1; min-width:0">
                        <p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>
                        {#if amenidadEditandoId === ha.id}
                          <textarea class="adm__amenidad-desc-input" bind:value={editDescAmenidad} rows="2"></textarea>
                          <div style="display:flex; gap:.5rem; margin-top:.5rem">
                            <button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarDescAmenidad(ha)}>Guardar</button>
                            <button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => amenidadEditandoId = null}>Cancelar</button>
                          </div>
                        {:else}
                          <p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>
                        {/if}
                      </div>
                      <div style="display:flex; gap:.4rem; flex-shrink:0">
                        {#if amenidadEditandoId !== ha.id}
                          <button class="adm__icon-btn adm__icon-btn--edit" title="Editar descripción" on:click={() => { amenidadEditandoId = ha.id; editDescAmenidad = ha.descripcion; }}>
                            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                          </button>
                        {/if}
                        <button class="adm__icon-btn adm__icon-btn--delete" title="Eliminar" on:click={() => eliminarAmenidadHotel(ha.id)}>
                          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                        </button>
                      </div>
                    </div>

                    <!-- Imágenes de la amenidad -->
                    <div class="adm__amenidad-imgs">
                      <div class="adm__img-grid adm__img-grid--sm">
                        {#each (ha.imagenesIds ?? []) as imgId (imgId)}
                          <div class="adm__img-card">
                            <img src="{API_BASE}/imagenes/amenidad/{imgId}" alt="amenidad img" />
                            <button class="adm__img-delete" on:click={() => eliminarImagenAmenidad(ha.id, imgId)}>
                              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                            </button>
                          </div>
                        {/each}
                        <label class="adm__wizard-add-img-btn adm__upload-btn" title="Subir imagen">
                          {#if subiendoImgAmenidad}
                            <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                          {:else}
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                          {/if}
                          <input type="file" accept="image/*" on:change={(e) => subirImagenAmenidad(e, ha.id)} disabled={subiendoImgAmenidad} style="display:none" />
                        </label>
                      </div>
                    </div>
                  </div>
                {/each}
              </div>
            {/if}

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
              <!-- Barra de acciones -->
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
                <span style="color:var(--adm-text-muted); font-size:.85rem">{habitaciones.length} habitación(es)</span>
                <button class="adm__btn adm__btn--primary" on:click={abrirModalNuevaHabGestion}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  Nueva Habitación
                </button>
              </div>

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
                          <td class="adm__table-money">$ {h.precioPorNoche?.toFixed(2)}</td>
                          <td class="adm__table-money">$ {h.precioPorPersona?.toFixed(2)}</td>
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
                        <tr><td colspan="9" class="adm__empty-cell">No hay habitaciones registradas. Crea la primera.</td></tr>
                      {/if}
                    </tbody>
                  </table>
                </div>
              </div>
            {/if}
          {/if}
        {/if}

      <!-- ═══ CREAR HOTEL ═══ -->
      {:else if activeSection === 'crear-hotel'}

        <!-- Wizard pasos -->
        <div class="adm__wizard-steps">
          <div class="adm__wizard-step" class:adm__wizard-step--done={hotelCreadoId && pasoActual !== 'info'} class:adm__wizard-step--active={pasoActual === 'info'}>
            <div class="adm__wizard-step-num">1</div>
            <span>Información</span>
          </div>
          <div class="adm__wizard-connector"></div>
          <div class="adm__wizard-step" class:adm__wizard-step--done={pasoActual === 'habitaciones'} class:adm__wizard-step--active={pasoActual === 'contenido'} class:adm__wizard-step--disabled={!hotelCreadoId}>
            <div class="adm__wizard-step-num">2</div>
            <span>Imágenes y Amenidades</span>
          </div>
          <div class="adm__wizard-connector"></div>
          <div class="adm__wizard-step" class:adm__wizard-step--active={pasoActual === 'habitaciones'} class:adm__wizard-step--disabled={!hotelCreadoId}>
            <div class="adm__wizard-step-num">3</div>
            <span>Habitaciones</span>
          </div>
        </div>

        <!-- ── Paso 1: Info ── -->
        {#if pasoActual === 'info'}
          <div class="adm__wizard-card">
            <h3 class="adm__wizard-card-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              Datos del nuevo hotel
            </h3>
            <div class="adm__form-grid adm__form-grid--wizard">
              <div class="adm__field adm__field--full">
                <label>Nombre del Hotel *</label>
                <input type="text" bind:value={nuevoHotel.nombre} placeholder="Ej: Miku Inn Paris" />
              </div>
              <div class="adm__field">
                <label>País *</label>
                <div class="adm__autocomplete-wrap">
                  <input
                    type="text"
                    bind:value={wizardPaisQuery}
                    on:input={onWizardPaisInput}
                    on:blur={() => setTimeout(validarWizardPais, 150)}
                    placeholder="Escribe el país..."
                    autocomplete="off"
                  />
                  {#if wizardPaisesSugeridos.length > 0}
                    <ul class="adm__autocomplete-list">
                      {#each wizardPaisesSugeridos as p}
                        <li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardPais(p)}>{p.country}</button></li>
                      {/each}
                    </ul>
                  {/if}
                </div>
                {#if wizardPaisError}<span class="adm__field-error">{wizardPaisError}</span>{/if}
              </div>
              <div class="adm__field">
                <label>Ciudad *</label>
                <div class="adm__autocomplete-wrap">
                  <input
                    type="text"
                    bind:value={nuevoHotel.ciudadNombre}
                    on:input={onWizardCiudadInput}
                    on:blur={() => setTimeout(() => { wizardCiudadesSugeridas = []; }, 150)}
                    placeholder={wizardPaisSeleccionado ? "Escribe la ciudad..." : "Primero selecciona un país"}
                    disabled={!wizardPaisSeleccionado}
                    autocomplete="off"
                  />
                  {#if wizardCiudadesSugeridas.length > 0}
                    <ul class="adm__autocomplete-list">
                      {#each wizardCiudadesSugeridas as c}
                        <li><button type="button" class="adm__autocomplete-item" on:mousedown|preventDefault={() => seleccionarWizardCiudad(c)}>{c}</button></li>
                      {/each}
                    </ul>
                  {/if}
                </div>
                {#if wizardCiudadError}<span class="adm__field-error">{wizardCiudadError}</span>{/if}
              </div>
              <div class="adm__field adm__field--full">
                <label>Dirección</label>
                <input type="text" bind:value={nuevoHotel.direccion} placeholder="Calle, número, colonia..." />
              </div>
              <div class="adm__field">
                <label>Rating inicial (0–5)</label>
                <input type="number" bind:value={nuevoHotel.rating} min="0" max="5" step="0.1" />
              </div>
              <div class="adm__field">
                <label>Estado</label>
                <select bind:value={nuevoHotel.estadoId}>
                  <option value={1}>Activo</option>
                  <option value={2}>Cerrado</option>
                </select>
              </div>
              <div class="adm__field adm__field--full">
                <label>Descripción</label>
                <textarea bind:value={nuevoHotel.descripcion} rows="4" placeholder="Describe el hotel..."></textarea>
              </div>
            </div>

            {#if mensajeNuevoHotel}
              <div class="adm__feedback adm__feedback--{mensajeNuevoHotel.tipo}" style="margin-top:1rem">
                {mensajeNuevoHotel.texto}
              </div>
            {/if}

            <div class="adm__wizard-actions">
              <button class="adm__btn adm__btn--ghost" on:click={resetCrearHotel}>Limpiar</button>
              <button class="adm__btn adm__btn--primary adm__btn--lg" on:click={crearNuevoHotel} disabled={guardandoNuevoHotel}>
                {#if guardandoNuevoHotel}
                  <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Creando hotel...
                {:else}
                  Crear Hotel y Continuar
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
                {/if}
              </button>
            </div>
          </div>

        <!-- ── Paso 2: Imágenes y Amenidades ── -->
        {:else if pasoActual === 'contenido'}
          <div class="adm__wizard-card">
            <h3 class="adm__wizard-card-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              Imágenes y Amenidades — <strong>{hotelCreadoNombre}</strong>
            </h3>

            <!-- Imágenes -->
            <p class="adm__modal-section-title">Imágenes del hotel</p>
            <div class="adm__img-section-header" style="margin-bottom:.75rem">
              <span style="font-size:.8rem; color:var(--adm-text-muted)">{imagenesNuevoHotel.length} imagen(es)</span>
              <label class="adm__btn adm__btn--ghost adm__upload-btn">
                {#if subiendoImgNuevoHotel}
                  <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Subiendo...
                {:else}
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>Agregar imagen
                {/if}
                <input type="file" accept="image/*" on:change={subirImagenNuevoHotel} disabled={subiendoImgNuevoHotel} style="display:none" />
              </label>
            </div>
            {#if mensajeImgNuevo}<div class="adm__feedback adm__feedback--{mensajeImgNuevo.tipo}" style="margin-bottom:.75rem">{mensajeImgNuevo.texto}</div>{/if}
            {#if imagenesNuevoHotel.length > 0}
              <div class="adm__img-grid" style="margin-bottom:1.5rem">
                {#each imagenesNuevoHotel as img (img.id)}
                  <div class="adm__img-card">
                    <img src={img.preview} alt="img" />
                    <button class="adm__img-delete" on:click={() => eliminarImgNuevoHotel(img.id)}>
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                {/each}
              </div>
            {:else}
              <div class="adm__img-empty" style="padding:1rem 0; margin-bottom:1.5rem">
                <p>Sin imágenes aún. Puedes omitir.</p>
              </div>
            {/if}

            <!-- Amenidades -->
            <div class="adm__modal-section-divider"></div>
            <div style="display:flex; justify-content:space-between; align-items:center; margin:.75rem 0">
              <p class="adm__modal-section-title" style="margin:0">Amenidades del hotel</p>
              <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = !showFormAmenidad}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Agregar
              </button>
            </div>

            {#if showFormAmenidad}
              <div class="adm__wizard-subcard" style="margin-bottom:1rem">
                <div class="adm__form-grid adm__form-grid--wizard">
                  <div class="adm__field">
                    <label>Tipo</label>
                    <select bind:value={nuevaAmenidad.amenidadId}>
                      {#each amenidades.filter(a => !amenidadesNuevoHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}
                    </select>
                  </div>
                  <div class="adm__field adm__field--full">
                    <label>Descripción</label>
                    <textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Describe esta amenidad..."></textarea>
                  </div>
                </div>
                <div style="display:flex; gap:.75rem; justify-content:flex-end; margin-top:.75rem">
                  <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button>
                  <button class="adm__btn adm__btn--primary" on:click={agregarAmenidadNuevoHotel}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>Agregar
                  </button>
                </div>
              </div>
            {/if}

            {#if amenidadesNuevoHotel.length > 0}
              <div style="display:flex; flex-direction:column; gap:.6rem; margin-bottom:1rem">
                {#each amenidadesNuevoHotel as ha (ha.id)}
                  <div class="adm__amenidad-card">
                    <div class="adm__amenidad-header">
                      <div class="adm__amenidad-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div>
                      <div style="flex:1">
                        <p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>
                        <p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>
                      </div>
                      <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => amenidadesNuevoHotel = amenidadesNuevoHotel.filter(a => a.id !== ha.id) && eliminarAmenidadHotelById(ha.id)}>
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                      </button>
                    </div>
                  </div>
                {/each}
              </div>
            {:else if !showFormAmenidad}
              <div class="adm__img-empty" style="padding:1rem 0">
                <p>Sin amenidades aún. Puedes omitir.</p>
              </div>
            {/if}

            <div class="adm__wizard-actions">
              <button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'info'}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>Volver
              </button>
              <button class="adm__btn adm__btn--primary adm__btn--lg" on:click={() => pasoActual = 'habitaciones'}>
                Continuar a Habitaciones
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
              </button>
            </div>
          </div>


        <!-- ── Paso 3: Habitaciones ── -->
        {:else if pasoActual === 'habitaciones'}
          <div class="adm__wizard-card">
            <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:1.25rem; flex-wrap:wrap; gap:.75rem">
              <h3 class="adm__wizard-card-title" style="margin:0">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/></svg>
                Habitaciones de <strong>{hotelCreadoNombre}</strong>
              </h3>
              <button class="adm__btn adm__btn--primary" on:click={() => { showFormHabNueva = true; mensajeNuevaHab = null; }}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Agregar Habitación
              </button>
            </div>

            <!-- Formulario nueva habitación -->
            {#if showFormHabNueva}
              <div class="adm__wizard-subcard">
                <p class="adm__modal-section-title">Nueva Habitación</p>
                <div class="adm__form-grid adm__form-grid--wizard">
                  <div class="adm__field">
                    <label>Tipo</label>
                    <select bind:value={nuevaHabitacion.tipoHabitacionId}>
                      {#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}
                    </select>
                  </div>
                  <div class="adm__field">
                    <label>Cama</label>
                    <select bind:value={nuevaHabitacion.camaId}>
                      {#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}
                    </select>
                  </div>
                  <div class="adm__field">
                    <label>Precio/Noche ($)</label>
                    <input type="number" bind:value={nuevaHabitacion.precioPorNoche} min="0" step="0.01" />
                  </div>
                  <div class="adm__field">
                    <label>Precio/Persona ($)</label>
                    <input type="number" bind:value={nuevaHabitacion.precioPorPersona} min="0" step="0.01" />
                  </div>
                  <div class="adm__field">
                    <label>Capacidad máx.</label>
                    <input type="number" bind:value={nuevaHabitacion.capacidadMaxima} min="1" />
                  </div>
                  <div class="adm__field">
                    <label>m²</label>
                    <input type="number" bind:value={nuevaHabitacion.metrosCuadrados} min="0" step="0.1" />
                  </div>
                  <div class="adm__field">
                    <label>Estado</label>
                    <select bind:value={nuevaHabitacion.estadoId}>
                      <option value={1}>Activa</option>
                      <option value={2}>Cerrada</option>
                    </select>
                  </div>
                  <div class="adm__field adm__field--full">
                    <label>Descripción</label>
                    <textarea bind:value={nuevaHabitacion.descripcion} rows="2" placeholder="Descripción..."></textarea>
                  </div>
                </div>
                {#if mensajeNuevaHab}
                  <div class="adm__feedback adm__feedback--{mensajeNuevaHab.tipo}" style="margin:.75rem 0">{mensajeNuevaHab.texto}</div>
                {/if}
                <div style="display:flex; gap:.75rem; justify-content:flex-end; margin-top:1rem">
                  <button class="adm__btn adm__btn--ghost" on:click={() => showFormHabNueva = false}>Cancelar</button>
                  <button class="adm__btn adm__btn--primary" on:click={crearNuevaHabitacion} disabled={guardandoNuevaHab}>
                    {#if guardandoNuevaHab}
                      <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Creando...
                    {:else}
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>Crear Habitación
                    {/if}
                  </button>
                </div>
              </div>
            {/if}

            <!-- Lista de habitaciones creadas -->
            {#if habitacionesNuevas.length > 0}
              <div style="display:flex; flex-direction:column; gap:1rem; margin-top:1rem">
                {#each habitacionesNuevas as h (h.id)}
                  <div class="adm__wizard-hab-card">
                    <div class="adm__wizard-hab-info">
                      <p class="adm__wizard-hab-tipo">{h.tipoHabitacion}</p>
                      <p class="adm__wizard-hab-meta">{h.tipoCama} · {h.capacidadMaxima} pers. · {h.metrosCuadrados} m² · $ {Number(h.precioPorNoche).toFixed(2)}/noche</p>
                    </div>
                    <div class="adm__wizard-hab-imgs">
                      <div class="adm__img-grid adm__img-grid--sm">
                        {#each (imagenesHabNueva[h.id] ?? []) as img (img.id)}
                          <div class="adm__img-card">
                            <img src={img.preview} alt="img" />
                            <button class="adm__img-delete" on:click={() => eliminarImgHabNueva(h.id, img.id)}>
                              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                            </button>
                          </div>
                        {/each}
                        <label class="adm__wizard-add-img-btn adm__upload-btn">
                          {#if subiendoImgHabNuevaSet.has(h.id)}
                            <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                          {:else}
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                          {/if}
                          <input type="file" accept="image/*" on:change={(e) => subirImagenHabNueva(e, h.id)} style="display:none" />
                        </label>
                      </div>
                    </div>
                  </div>
                {/each}
              </div>
            {:else if !showFormHabNueva}
              <div class="adm__img-empty" style="padding:2rem 0">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                <p>Agrega las habitaciones del hotel.</p>
              </div>
            {/if}

            <div class="adm__wizard-actions" style="margin-top:1.5rem">
              <button class="adm__btn adm__btn--ghost" on:click={() => pasoActual = 'contenido'}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>Volver
              </button>
              <button class="adm__btn adm__btn--success adm__btn--lg" on:click={() => { resetCrearHotel(); activeSection = 'hoteles'; }}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                Finalizar — Ver Hoteles
              </button>
            </div>
          </div>
        {/if}

      <!-- ═══ RESERVAS ═══ -->
      {:else if activeSection === 'reservas'}

        <!-- Tarjetas rápidas clicables — usan conteoReservas (sin filtro) -->
        <div class="adm__stats-grid" style="margin-bottom:1.25rem">
          <div class="adm__stat-card adm__stat-card--green" style="cursor:pointer" on:click={() => { filtroEstadoReserva='confirmada'; onFiltroEstadoCambia(); }} on:keydown={() => {}} role="button" tabindex="0">
            <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg></div></div>
            <p class="adm__stat-value">{conteoReservas.confirmada}</p>
            <p class="adm__stat-label">Confirmadas</p>
          </div>
          <div class="adm__stat-card adm__stat-card--amber" style="cursor:pointer" on:click={() => { filtroEstadoReserva='pendiente'; onFiltroEstadoCambia(); }} on:keydown={() => {}} role="button" tabindex="0">
            <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div></div>
            <p class="adm__stat-value">{conteoReservas.pendiente}</p>
            <p class="adm__stat-label">Pendientes</p>
          </div>
          <div class="adm__stat-card adm__stat-card--red" style="cursor:pointer" on:click={() => { filtroEstadoReserva='cancelada'; onFiltroEstadoCambia(); }} on:keydown={() => {}} role="button" tabindex="0">
            <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div></div>
            <p class="adm__stat-value">{conteoReservas.cancelada}</p>
            <p class="adm__stat-label">Canceladas</p>
          </div>
          <div class="adm__stat-card adm__stat-card--blue" style="cursor:pointer" on:click={() => { filtroEstadoReserva='todos'; onFiltroEstadoCambia(); }} on:keydown={() => {}} role="button" tabindex="0">
            <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div></div>
            <p class="adm__stat-value">{conteoReservas.total}</p>
            <p class="adm__stat-label">Total</p>
          </div>
        </div>

        <div class="adm__card adm__card--no-pad">
          <!-- Header -->
          <div class="adm__card-header adm__card-header--pad">
            <h3 class="adm__card-title">
              Reservaciones del Sistema
              {#if filtroEstadoReserva !== 'todos'}
                <span style="font-size:.78rem; font-weight:400; color:var(--adm-text-muted); margin-left:.5rem">
                  — filtrando: <strong style="color:var(--adm-text)">{filtroEstadoReserva}</strong>
                </span>
              {/if}
            </h3>
            <button class="adm__btn adm__btn--ghost" on:click={cargarReservas} title="Recargar lista">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
              Recargar
            </button>
          </div>

          <!-- Filtros -->
          <div class="adm__filters-bar" style="padding:.75rem 1.25rem; border-bottom:1px solid var(--adm-border)">
            <div class="adm__search-wrap">
              <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              <input
                class="adm__search-input"
                type="text"
                placeholder="Buscar por No. reserva, usuario, correo o hotel..."
                bind:value={busquedaReserva}
                on:input={onBusquedaCambia}
                aria-label="Buscar reservaciones"
              />
            </div>
            <select class="adm__select" bind:value={filtroEstadoReserva} on:change={onFiltroEstadoCambia}>
              <option value="todos">Todos los estados</option>
              <option value="pendiente">Pendiente</option>
              <option value="confirmada">Confirmada</option>
              <option value="completada">Completada</option>
              <option value="cancelada">Cancelada</option>
              <option value="expirada">Expirada</option>
            </select>
          </div>

          {#if cargandoReservas}
            <div class="adm__loading-state" style="padding:3rem">
              <svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              <p>Cargando reservaciones...</p>
            </div>
          {:else if errorReservas}
            <div class="adm__error-state" style="padding:2rem">
              <p>{errorReservas}</p>
              <button class="adm__btn adm__btn--ghost" on:click={cargarReservas}>Reintentar</button>
            </div>
          {:else}
            <div class="adm__table-wrap">
              <table class="adm__table">
                <thead>
                  <tr>
                    <th>No. Reserva</th>
                    <th>Usuario</th>
                    <th>Hotel</th>
                    <th>Check-in</th>
                    <th>Check-out</th>
                    <th>Habs.</th>
                    <th>Total</th>
                    <th>Creada</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {#each reservas as r (r.id)}
                    <tr>
                      <td class="adm__table-mono" style="font-size:.8rem">{r.noReservacion}</td>
                      <td>
                        <p style="margin:0; font-weight:600; font-size:.85rem">@{r.usuario}</p>
                        <p style="margin:0; font-size:.72rem; color:var(--adm-text-muted)">{r.nombreCompleto}</p>
                        <p style="margin:0; font-size:.68rem; color:var(--adm-text-muted)">{r.correo ?? ''}</p>
                      </td>
                      <td style="font-size:.85rem">{r.hotel ?? '—'}</td>
                      <td style="font-size:.82rem">{r.checkIn ?? '—'}</td>
                      <td style="font-size:.82rem">{r.checkOut ?? '—'}</td>
                      <td class="adm__table-center">{r.cantidadHabitaciones ?? 0}</td>
                      <td class="adm__table-money">$ {(r.total ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                      <td style="font-size:.75rem; color:var(--adm-text-muted)">{r.fechaCreacion ? r.fechaCreacion.substring(0,16) : '—'}</td>
                      <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
                      <td>
                        {#if r.estadoId === 1 || r.estadoId === 2}
                          <button
                            class="adm__icon-btn adm__icon-btn--delete"
                            title="Cancelar reservación"
                            on:click={() => abrirModalCancelar(r)}
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <circle cx="12" cy="12" r="10"/>
                              <line x1="15" y1="9" x2="9" y2="15"/>
                              <line x1="9" y1="9" x2="15" y2="15"/>
                            </svg>
                          </button>
                        {:else}
                          <span style="color:var(--adm-text-muted); font-size:.75rem; padding:0 .5rem">—</span>
                        {/if}
                      </td>
                    </tr>
                  {/each}
                  {#if reservas.length === 0}
                    <tr>
                      <td colspan="10" class="adm__empty-cell">
                        {busquedaReserva || filtroEstadoReserva !== 'todos'
                          ? 'Sin resultados para los filtros aplicados.'
                          : 'No hay reservaciones registradas.'}
                      </td>
                    </tr>
                  {/if}
                </tbody>
              </table>
            </div>
            <div style="padding:.6rem 1.25rem; color:var(--adm-text-muted); font-size:.78rem; border-top:1px solid var(--adm-border)">
              Mostrando {reservas.length} reservaciones
              {#if filtroEstadoReserva !== 'todos' || busquedaReserva}
                · con filtros activos
              {/if}
            </div>
          {/if}
        </div>

      <!-- ═══ REPORTES ═══ -->
      {:else if activeSection === 'agencias'}

        <div class="adm__filters-bar">
          <div class="adm__search-wrap">
            <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            <input class="adm__search-input" type="text" placeholder="Buscar por nombre, correo o ID..." bind:value={busquedaAgencia} />
          </div>
          <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias} disabled={cargandoAgencias} title="Recargar">
            <svg class={cargandoAgencias ? 'adm__spinner' : ''} width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            Recargar
          </button>
        </div>

        {#if errorAgencias}
          <div class="adm__alert adm__alert--error" style="margin-bottom:1rem">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {errorAgencias}
            <button class="adm__btn adm__btn--ghost" on:click={cargarAgencias}>Reintentar</button>
          </div>
        {/if}

        <div class="adm__table-card">
          {#if cargandoAgencias}
            <div class="adm__loading-state" style="padding:3rem 0">
              <svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              <p>Cargando agencias...</p>
            </div>
          {:else}
            <div class="adm__table-wrap">
              <table class="adm__table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Usuario WS</th>
                    <th>Descuento %</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {#if agenciasFiltradas.length === 0}
                    <tr>
                      <td colspan="7" class="adm__empty-cell">
                        {busquedaAgencia ? 'Sin resultados para esa búsqueda.' : 'No hay agencias registradas.'}
                      </td>
                    </tr>
                  {:else}
                    {#each agenciasFiltradas as ag (ag.id)}
                      <tr>
                        <td class="adm__table-mono" style="color:var(--adm-text-muted);font-size:.8rem">#{ag.id}</td>
                        <td style="font-weight:600">{ag.nombre}</td>
                        <td style="color:var(--adm-text-muted)">{ag.correo}</td>
                        <td class="adm__table-mono" style="font-size:.8rem">WS #{ag.usuarioWebisId}</td>
                        <td><span style="font-weight:700;color:#2dd4bf">{ag.porcentajeDescuento?.toFixed(2)}%</span></td>
                        <td><span class="adm__badge {badge(ag.estado)}">{ag.estado}</span></td>
                        <td>
                          <button class="adm__icon-btn" title="Editar" on:click={() => abrirEditarAgencia(ag)}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                            </svg>
                          </button>
                        </td>
                      </tr>
                    {/each}
                  {/if}
                </tbody>
              </table>
            </div>
          {/if}
        </div>

      {:else if activeSection === 'reportes'}
        <div class="adm__reportes-grid">

          <!-- Reservas por estado (donut con datos reales) -->
          <div class="adm__card adm__reporte-card">
            <h3 class="adm__card-title">Reservas por Estado</h3>
            {#if metricas}
              {@const confirmadas = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'confirmada')?.total ?? 0}
              {@const pendientes  = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'pendiente')?.total  ?? 0}
              {@const canceladas  = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'cancelada')?.total  ?? 0}
              {@const expiradas   = (metricas.reservasPorEstado ?? []).find(e => e.estado === 'expirada')?.total   ?? 0}
              {@const totalRes    = metricas.reservasTotales ?? 0}
              <div class="adm__donut-wrap">
                <div class="adm__donut" role="img" aria-label="Gráfica de reservas por estado">
                  <span class="adm__donut-center">{totalRes}</span>
                </div>
                <div class="adm__donut-legend">
                  <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--green"></span> Confirmadas ({confirmadas})</div>
                  <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--yellow"></span> Pendientes ({pendientes})</div>
                  <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--red"></span> Canceladas ({canceladas})</div>
                  <div class="adm__legend-item"><span class="adm__legend-dot adm__legend-dot--gray" style="background:#6b7280"></span> Expiradas ({expiradas})</div>
                </div>
              </div>
            {:else}
              <div class="adm__loading-state" style="padding:1.5rem 0">
                <svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                <p>Cargando...</p>
              </div>
            {/if}
          </div>

          <!-- Resumen General (datos reales) -->
          <div class="adm__card adm__reporte-card">
            <h3 class="adm__card-title">Resumen General</h3>
            {#if metricas}
              <div class="adm__kpi-grid">
                <div class="adm__kpi-item">
                  <p class="adm__kpi-val">$ {(metricas.ingresosTotales ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                  <p class="adm__kpi-lbl">Ingresos confirmados</p>
                </div>
                <div class="adm__kpi-item">
                  <p class="adm__kpi-val">{(metricas.totalUsuarios ?? 0).toLocaleString('es-GT')}</p>
                  <p class="adm__kpi-lbl">Usuarios registrados</p>
                </div>
                <div class="adm__kpi-item">
                  <p class="adm__kpi-val">{metricas.reservasActivas ?? 0}</p>
                  <p class="adm__kpi-lbl">Reservas confirmadas</p>
                </div>
                <div class="adm__kpi-item">
                  <p class="adm__kpi-val">{metricas.reservasTotales ?? 0}</p>
                  <p class="adm__kpi-lbl">Reservas totales</p>
                </div>
                <div class="adm__kpi-item">
                  <p class="adm__kpi-val">{metricas.hotelesActivos ?? 0} / {metricas.hotesTotales ?? 0}</p>
                  <p class="adm__kpi-lbl">Hoteles activos / total</p>
                </div>
              </div>
            {:else}
              <div class="adm__loading-state" style="padding:1.5rem 0">
                <svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                <p>Cargando métricas...</p>
              </div>
            {/if}
          </div>

          <!-- Top Hoteles (datos reales por rating) -->
          <div class="adm__card adm__reporte-card adm__reporte-card--wide">
            <h3 class="adm__card-title">Hoteles por Rating</h3>
            <div class="adm__hotel-bars">
              {#each [...hoteles].sort((a,b) => b.rating - a.rating).slice(0, 8) as h}
                <div class="adm__hotel-bar-item">
                  <div class="adm__hotel-bar-info">
                    <span class="adm__hotel-bar-name">{h.nombre}</span>
                    <span class="adm__hotel-bar-pct">{h.ciudad}, {h.pais} — ★ {h.rating?.toFixed(1)}</span>
                  </div>
                  <div class="adm__hotel-bar-track">
                    <div class="adm__hotel-bar-fill" style="width: {Math.round((h.rating / 5) * 100)}%"></div>
                  </div>
                </div>
              {/each}
              {#if hoteles.length === 0}
                <p style="color: var(--adm-text-muted); font-size: .85rem; text-align:center; padding: 1rem 0;">Sin hoteles registrados</p>
              {/if}
            </div>
          </div>

          <!-- Últimas reservas en reportes -->
          <div class="adm__card adm__reporte-card adm__reporte-card--wide">
            <div class="adm__card-header">
              <h3 class="adm__card-title">Últimas 10 Reservas</h3>
              <button class="adm__link-btn" on:click={() => activeSection = 'reservas'}>Ver todas →</button>
            </div>
            <div class="adm__table-wrap">
              <table class="adm__table">
                <thead><tr><th>No. Reserva</th><th>Usuario</th><th>Hotel</th><th>Total</th><th>Estado</th></tr></thead>
                <tbody>
                  {#each reservas.slice(0, 10) as r}
                    <tr>
                      <td class="adm__table-mono">{r.noReservacion}</td>
                      <td>@{r.usuario}</td>
                      <td>{r.hotel}</td>
                      <td class="adm__table-money">$ {(r.total ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                      <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
                    </tr>
                  {/each}
                  {#if reservas.length === 0}
                    <tr><td colspan="5" class="adm__empty-cell">Sin reservas</td></tr>
                  {/if}
                </tbody>
              </table>
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
      <div class="adm__rol-modal__avatar" style="background: {usuarioSeleccionado.rolId === 2 ? 'linear-gradient(135deg,#f59e0b,#d97706)' : usuarioSeleccionado.rolId === 3 ? 'linear-gradient(135deg,#8b5cf6,#6d28d9)' : 'linear-gradient(135deg,#667eea,#764ba2)'}">
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

        <button
          class="adm__rol-card {editUsuario.rolId === 3 ? 'adm__rol-card--active-ws' : ''}"
          on:click={() => editUsuario.rolId = 3}
        >
          <div class="adm__rol-card__icon adm__rol-card__icon--ws">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>
          </div>
          <div class="adm__rol-card__text">
            <span class="adm__rol-card__title">Webservice</span>
            <span class="adm__rol-card__desc">Acceso para integraciones API</span>
          </div>
          {#if editUsuario.rolId === 3}
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

{#if showModalNuevaHabGestion && hotelDetalle}
  <div class="adm__overlay" on:click={() => { showModalNuevaHabGestion = false; }} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b; font-size:1.4rem; display:flex; align-items:center; justify-content:center;">&#x1F6CF;</div>
      <div class="adm__hotel-modal__info">
        <p class="adm__hotel-modal__name">Nueva Habitación</p>
        <p class="adm__hotel-modal__loc">{hotelDetalle.nombre}</p>
      </div>
      <button class="adm__rol-modal__close" on:click={() => showModalNuevaHabGestion = false} aria-label="Cerrar">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="adm__hotel-modal__body">
      <p class="adm__modal-section-title">Datos de la habitacion</p>
      <div class="adm__form-grid">
        <div class="adm__field">
          <label>Tipo</label>
          <select bind:value={nuevaHabGestion.tipoHabitacionId}>
            {#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}
          </select>
        </div>
        <div class="adm__field">
          <label>Cama</label>
          <select bind:value={nuevaHabGestion.camaId}>
            {#each tiposCama as c}<option value={c.id}>{c.nombre}</option>{/each}
          </select>
        </div>
        <div class="adm__field">
          <label>Precio/Noche ($)</label>
          <input type="number" bind:value={nuevaHabGestion.precioPorNoche} min="0" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Precio/Persona ($)</label>
          <input type="number" bind:value={nuevaHabGestion.precioPorPersona} min="0" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Capacidad máx.</label>
          <input type="number" bind:value={nuevaHabGestion.capacidadMaxima} min="1" />
        </div>
        <div class="adm__field">
          <label>m²</label>
          <input type="number" bind:value={nuevaHabGestion.metrosCuadrados} min="0" step="0.1" />
        </div>
        <div class="adm__field">
          <label>Estado</label>
          <select bind:value={nuevaHabGestion.estadoId}>
            <option value={1}>Activa</option>
            <option value={2}>Cerrada</option>
          </select>
        </div>
        <div class="adm__field adm__field--full">
          <label>Descripcion</label>
          <textarea bind:value={nuevaHabGestion.descripcion} rows="3" placeholder="Descripcion..."></textarea>
        </div>
      </div>
      {#if mensajeNuevaHabGestion}
        <div class="adm__feedback adm__feedback--{mensajeNuevaHabGestion.tipo}" style="margin:.75rem 0">
          {mensajeNuevaHabGestion.texto}
        </div>
      {/if}
    </div>
    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={() => showModalNuevaHabGestion = false}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearHabGestion} disabled={guardandoNuevaHabGestion}>
        {#if guardandoNuevaHabGestion}
          <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>Creando...
        {:else}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>Crear Habitacion
        {/if}
      </button>
    </div>
  </div>
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
          <label>Precio por Noche ($)</label>
          <input type="number" bind:value={editHabitacion.precioPorNoche} min="0" step="0.01" />
        </div>
        <div class="adm__field">
          <label>Precio por Persona ($)</label>
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

<!-- ═══ MODAL CANCELAR RESERVACIÓN ═══ -->
{#if showModalCancelarReserva && reservaCancelando}
  <div
    class="adm__overlay"
    on:click={cerrarModalCancelar}
    on:keydown={e => e.key === 'Escape' && cerrarModalCancelar()}
    role="button" tabindex="-1" aria-label="Cerrar modal"
  ></div>

  <div class="adm__rol-modal" style="max-width:480px; border-radius:16px; overflow:hidden">

    <!-- Header rojo con clases propias -->
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="15" y1="9" x2="9" y2="15"/>
          <line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
      </div>
      <div>
        <p class="adm__cancel-modal__title">Cancelar Reservación</p>
        <p class="adm__cancel-modal__subtitle">{reservaCancelando.noReservacion} · {reservaCancelando.hotel ?? 'Sin hotel'}</p>
      </div>
      <button class="adm__cancel-modal__close" on:click={cerrarModalCancelar} aria-label="Cerrar">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- Cuerpo -->
    <div class="adm__cancel-modal__body">

      <!-- Info de la reserva -->
      <div class="adm__cancel-info-box">
        <div class="adm__cancel-info-row">
          <span class="adm__cancel-info-row__label">Cliente</span>
          <span class="adm__cancel-info-row__value">@{reservaCancelando.usuario} — {reservaCancelando.nombreCompleto}</span>
        </div>
        <div class="adm__cancel-info-row">
          <span class="adm__cancel-info-row__label">Hotel</span>
          <span class="adm__cancel-info-row__value">{reservaCancelando.hotel ?? '—'}</span>
        </div>
        <div class="adm__cancel-info-row">
          <span class="adm__cancel-info-row__label">Fechas</span>
          <span class="adm__cancel-info-row__value">{reservaCancelando.checkIn ?? '—'} → {reservaCancelando.checkOut ?? '—'}</span>
        </div>
        <div class="adm__cancel-info-row">
          <span class="adm__cancel-info-row__label">Total</span>
          <span class="adm__cancel-info-row__value adm__cancel-info-row__value--money">
            $ {(reservaCancelando.total ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </span>
        </div>
        <div class="adm__cancel-info-row">
          <span class="adm__cancel-info-row__label">Estado actual</span>
          <span class="adm__badge {badge(reservaCancelando.estado)}">{reservaCancelando.estado}</span>
        </div>
      </div>

      <!-- Motivo -->
      <label class="adm__cancel-motivo-label" for="motivo-cancel">
        Motivo de cancelación <span style="text-transform:none; font-weight:400">(opcional)</span>
      </label>
      <textarea
        id="motivo-cancel"
        class="adm__cancel-motivo-textarea"
        bind:value={motivoCancelacion}
        rows="3"
        placeholder="Ej: Solicitud del cliente, error en la reserva..."
      ></textarea>

      {#if mensajeCancelar}
        <div class="adm__feedback adm__feedback--error" style="margin-top:.75rem">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {mensajeCancelar}
        </div>
      {/if}

      <!-- Advertencia -->
      <div class="adm__cancel-warning">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0; margin-top:.1rem">
          <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
          <line x1="12" y1="9" x2="12" y2="13"/>
          <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
        <span>Esta acción no se puede deshacer. El estado pasará a <strong>Cancelada (ID 4)</strong> de forma inmediata.</span>
      </div>
    </div>

    <!-- Footer -->
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalCancelar} disabled={cancelando}>
        No, mantener reserva
      </button>
      <button
        class="adm__btn--cancel-confirm"
        on:click={confirmarCancelacion}
        disabled={cancelando}
      >
        {#if cancelando}
          <svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
          Cancelando...
        {:else}
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          Sí, cancelar reservación
        {/if}
      </button>
    </div>

  </div>
{/if}

<!-- ═══════════════════════════════════════════════
     MODAL: EDITAR AGENCIA (admin)
     ════════════════════════════════════════════════ -->
{#if showModalEditarAgencia && agenciaEditando}
  <div
    class="adm__overlay"
    on:click={cerrarModalAgencia}
    on:keydown={e => e.key === 'Escape' && cerrarModalAgencia()}
    role="button" tabindex="-1" aria-label="Cerrar modal"
  ></div>

  <div style="
    position:fixed; top:50%; left:50%; transform:translate(-50%,-50%); z-index:2001;
    width:90%; max-width:520px; border-radius:16px; overflow:hidden;
    background:#1e2a3a; border:1px solid #3a4a5c;
    box-shadow:0 24px 80px rgba(0,0,0,0.7);
    animation: modalIn 0.25s ease;
  ">

    <!-- Header -->
    <div style="display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.5rem;background:#253142;border-bottom:1px solid #3a4a5c">
      <div style="display:flex;align-items:center;gap:.75rem">
        <div style="width:38px;height:38px;border-radius:9px;background:linear-gradient(135deg,#2dd4bf,#0d9488);display:flex;align-items:center;justify-content:center;flex-shrink:0">
          <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5">
            <path d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
          </svg>
        </div>
        <div>
          <p style="font-weight:700;font-size:1rem;color:#e8f0fe;margin:0;line-height:1.3">Editar Agencia</p>
          <p style="font-size:.75rem;color:#8eacc8;margin:0">ID #{agenciaEditando.id} · WS #{agenciaEditando.usuarioWebisId}</p>
        </div>
      </div>
      <button on:click={cerrarModalAgencia} aria-label="Cerrar" style="background:rgba(255,255,255,0.08);border:none;color:#b0c4d8;cursor:pointer;display:flex;align-items:center;justify-content:center;width:30px;height:30px;border-radius:7px;transition:background .15s" on:mouseenter={e=>e.currentTarget.style.background='rgba(255,255,255,0.15)'} on:mouseleave={e=>e.currentTarget.style.background='rgba(255,255,255,0.08)'}>
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Body -->
    <div style="padding:1.5rem;display:flex;flex-direction:column;gap:1.1rem;background:#1e2a3a">

      <div style="display:flex;flex-direction:column;gap:.35rem">
        <label for="ea-nombre" style="font-size:.78rem;font-weight:600;color:#7eb8d4;text-transform:uppercase;letter-spacing:.05em">Nombre de la agencia</label>
        <input id="ea-nombre" type="text" bind:value={editAgencia.nombre} placeholder="Nombre"
          style="width:100%;padding:.7rem .9rem;background:#253142;border:1px solid #3a4a5c;border-radius:8px;color:#e8f0fe;font-size:.9rem;font-family:inherit;box-sizing:border-box;outline:none;transition:border-color .2s"
          on:focus={e=>{e.target.style.borderColor='#2dd4bf';e.target.style.boxShadow='0 0 0 3px rgba(45,212,191,.15)'}}
          on:blur={e=>{e.target.style.borderColor='#3a4a5c';e.target.style.boxShadow='none'}}
        />
      </div>

      <div style="display:flex;flex-direction:column;gap:.35rem">
        <label for="ea-correo" style="font-size:.78rem;font-weight:600;color:#7eb8d4;text-transform:uppercase;letter-spacing:.05em">Correo electrónico</label>
        <input id="ea-correo" type="email" bind:value={editAgencia.correo} placeholder="agencia@ejemplo.com"
          style="width:100%;padding:.7rem .9rem;background:#253142;border:1px solid #3a4a5c;border-radius:8px;color:#e8f0fe;font-size:.9rem;font-family:inherit;box-sizing:border-box;outline:none;transition:border-color .2s"
          on:focus={e=>{e.target.style.borderColor='#2dd4bf';e.target.style.boxShadow='0 0 0 3px rgba(45,212,191,.15)'}}
          on:blur={e=>{e.target.style.borderColor='#3a4a5c';e.target.style.boxShadow='none'}}
        />
      </div>

      <div style="display:flex;flex-direction:column;gap:.35rem">
        <label for="ea-descuento" style="font-size:.78rem;font-weight:600;color:#7eb8d4;text-transform:uppercase;letter-spacing:.05em">Porcentaje de descuento (0–100)</label>
        <input id="ea-descuento" type="number" min="0" max="100" step="0.01" bind:value={editAgencia.porcentajeDescuento}
          style="width:100%;padding:.7rem .9rem;background:#253142;border:1px solid #3a4a5c;border-radius:8px;color:#e8f0fe;font-size:.9rem;font-family:inherit;box-sizing:border-box;outline:none;transition:border-color .2s"
          on:focus={e=>{e.target.style.borderColor='#2dd4bf';e.target.style.boxShadow='0 0 0 3px rgba(45,212,191,.15)'}}
          on:blur={e=>{e.target.style.borderColor='#3a4a5c';e.target.style.boxShadow='none'}}
        />
      </div>

      <div style="display:flex;flex-direction:column;gap:.35rem">
        <label for="ea-estado" style="font-size:.78rem;font-weight:600;color:#7eb8d4;text-transform:uppercase;letter-spacing:.05em">Estado</label>
        <select id="ea-estado" bind:value={editAgencia.estadoId}
          style="width:100%;padding:.7rem .9rem;background:#253142;border:1px solid #3a4a5c;border-radius:8px;color:#e8f0fe;font-size:.9rem;font-family:inherit;box-sizing:border-box;outline:none;cursor:pointer;appearance:auto">
          <option value={1} style="background:#253142;color:#e8f0fe">Activo</option>
          <option value={2} style="background:#253142;color:#e8f0fe">Cerrado</option>
        </select>
      </div>

      {#if mensajeAgencia}
        <div style="padding:.7rem 1rem;border-radius:8px;font-size:.85rem;font-weight:500;
          background:{mensajeAgencia.tipo==='ok' ? 'rgba(63,185,80,.15)' : 'rgba(248,81,73,.15)'};
          border:1px solid {mensajeAgencia.tipo==='ok' ? 'rgba(63,185,80,.4)' : 'rgba(248,81,73,.4)'};
          color:{mensajeAgencia.tipo==='ok' ? '#3fb950' : '#f85149'}">
          {mensajeAgencia.texto}
        </div>
      {/if}
    </div>

    <!-- Footer -->
    <div style="display:flex;justify-content:flex-end;gap:.75rem;padding:1rem 1.5rem;border-top:1px solid #3a4a5c;background:#253142">
      <button on:click={cerrarModalAgencia} disabled={guardandoAgencia}
        style="padding:.6rem 1.1rem;background:transparent;border:1px solid #3a4a5c;border-radius:8px;color:#b0c4d8;font-size:.875rem;font-weight:500;cursor:pointer;transition:all .15s"
        on:mouseenter={e=>{e.currentTarget.style.background='rgba(255,255,255,0.06)';e.currentTarget.style.borderColor='#5a7a9c'}}
        on:mouseleave={e=>{e.currentTarget.style.background='transparent';e.currentTarget.style.borderColor='#3a4a5c'}}
      >Cancelar</button>
      <button on:click={guardarAgencia} disabled={guardandoAgencia}
        style="padding:.6rem 1.3rem;background:linear-gradient(135deg,#2dd4bf,#0d9488);border:none;border-radius:8px;color:#fff;font-size:.875rem;font-weight:600;cursor:pointer;transition:opacity .15s;opacity:{guardandoAgencia?'.6':'1'}"
        on:mouseenter={e=>{ if(!guardandoAgencia) e.currentTarget.style.opacity='.85' }}
        on:mouseleave={e=>{ if(!guardandoAgencia) e.currentTarget.style.opacity='1' }}
      >
        {#if guardandoAgencia}Guardando...{:else}Guardar cambios{/if}
      </button>
    </div>

  </div>
{/if}