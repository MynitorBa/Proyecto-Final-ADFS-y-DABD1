<script>
  /**
   * @file AdminHoteles.svelte
   * @description Modulo de gestion de hoteles del panel de administracion.
   * Contiene dos vistas: la lista de hoteles con busqueda y filtros, y el detalle
   * de un hotel seleccionado con pestanas para informacion, imagenes, amenidades y habitaciones.
   * Permite editar, crear, eliminar hoteles y gestionar sus recursos relacionados.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Funcion que retorna la clase CSS del badge segun el estado del registro.
   * @type {function(string): string}
   */
  export let badge;

  /**
   * Convierte un File a cadena Base64 para subirlo al backend.
   * @type {function(File): Promise<string>}
   */
  export let fileToBase64;

  /** Lista de tipos de habitacion disponibles (proveniente del padre). @type {Array<{id: number, nombre: string}>} */
  export let tiposHabitacion;

  /** Contador de hoteles totales, expuesto al componente padre. @type {number} */
  export let count = 0;

  /** Dialogo de confirmacion personalizado para acciones destructivas. @type {Object|null} */
  let confirmDialog = null;

  /**
   * Abre el dialogo de confirmacion con titulo, mensaje y funcion de callback.
   * @param {string} t - Titulo del dialogo.
   * @param {string} m - Mensaje descriptivo.
   * @param {function} fn - Funcion a ejecutar al confirmar.
   */
  function pedirConfirmacion(t, m, fn) { confirmDialog = { titulo: t, mensaje: m, onConfirm: fn }; }

  /** Cierra el dialogo sin ejecutar ninguna accion. */
  function cerrarConfirm() { confirmDialog = null; }

  /** Ejecuta la accion confirmada y cierra el dialogo. */
  function ejecutarConfirm() { if (confirmDialog?.onConfirm) confirmDialog.onConfirm(); confirmDialog = null; }

  /** Lista completa de hoteles cargados desde el backend. @type {Array<Object>} */
  let hoteles = [];

  /** Indica si se esta cargando la lista de hoteles. @type {boolean} */
  let cargandoHoteles = false;

  /** Mensaje de error si la carga de hoteles falla. @type {string|null} */
  let errorHoteles = null;

  /** Texto del buscador para filtrar hoteles por nombre, ciudad o pais. @type {string} */
  let busquedaHotel = '';

  /** Estado seleccionado en el filtro de hoteles: 'todos', 'activo' o 'cerrado'. @type {string} */
  let filtroEstadoHotel = 'todos';

  /** Vista actual del modulo: 'lista' o 'detalle'. @type {string} */
  let vistaHoteles = 'lista';

  /** Hotel actualmente seleccionado en la vista de detalle. @type {Object|null} */
  let hotelDetalle = null;

  /** Pestana activa en la vista de detalle: 'info', 'imagenes', 'amenidades' o 'habitaciones'. @type {string} */
  let tabDetalle = 'info';

  /** Copia editable de los datos basicos del hotel en la pestana de informacion. @type {Object} */
  let editInfoHotel = { nombre: '', direccion: '', descripcion: '', rating: 0, estadoId: 1 };

  /** Indica si se esta guardando la informacion basica del hotel. @type {boolean} */
  let guardandoInfo = false;

  /** Mensaje de retroalimentacion en la pestana de informacion. @type {{tipo: string, texto: string}|null} */
  let mensajeInfo = null;

  /** Indica si se esta subiendo una imagen del hotel. @type {boolean} */
  let subiendoImgHotel = false;

  /** Mensaje de retroalimentacion en la seccion de imagenes del hotel. @type {{tipo: string, texto: string}|null} */
  let mensajeImgHotel = null;

  /** Lista del catalogo global de amenidades. @type {Array<{id: number, nombre: string}>} */
  let amenidades = [];

  /** Lista de amenidades asignadas al hotel en detalle. @type {Array<Object>} */
  let amenidadesHotel = [];

  /** Indica si se estan cargando las amenidades del hotel. @type {boolean} */
  let cargandoAmenidades = false;

  /** Mensaje de retroalimentacion en la seccion de amenidades. @type {{tipo: string, texto: string}|null} */
  let mensajeAmenidad = null;

  /** Controla la visibilidad del formulario de asignacion de amenidad. @type {boolean} */
  let showFormAmenidad = false;

  /** Datos del formulario de nueva amenidad a asignar. @type {{amenidadId: number, descripcion: string}} */
  let nuevaAmenidad = { amenidadId: 1, descripcion: '' };

  /** ID de la amenidad cuya descripcion esta siendo editada inline. @type {number|null} */
  let amenidadEditandoId = null;

  /** Texto temporal de la descripcion mientras se edita. @type {string} */
  let editDescAmenidad = '';

  /** Set con los IDs de amenidades cuya imagen se esta subiendo. @type {Set<number>} */
  let subiendoImgAmenidadSet = new Set();

  /** Controla la visibilidad del formulario para crear una nueva categoria de amenidad. @type {boolean} */
  let showFormNuevaAmenidadCatalogo = false;

  /** Nombre de la nueva categoria a crear en el catalogo global. @type {string} */
  let nuevaAmenidadCatalogoNombre = '';

  /** Indica si se esta creando una nueva categoria de amenidad. @type {boolean} */
  let creandoAmenidadCatalogo = false;

  /** Mensaje de retroalimentacion al crear una categoria. @type {{tipo: string, texto: string}|null} */
  let mensajeNuevaAmenidadCatalogo = null;

  /** Lista de habitaciones del hotel en detalle. @type {Array<Object>} */
  let habitaciones = [];

  /** Indica si se estan cargando las habitaciones. @type {boolean} */
  let cargandoHabitaciones = false;

  /** Mensaje de error si la carga de habitaciones falla. @type {string|null} */
  let errorHabitaciones = null;

  /** Controla la visibilidad del modal de edicion de habitacion existente. @type {boolean} */
  let showModalHabitacion = false;

  /** Habitacion siendo editada en el modal. @type {Object|null} */
  let habitacionEditando = null;

  /**
   * Copia editable de los campos de la habitacion en el modal.
   * Incluye numeroHabitacion ya que la habitacion existe en BD y se puede editar.
   * @type {Object}
   */
  let editHabitacion = { tipoHabitacionId: 1, numeroHabitacion: '', descripcion: '', estadoId: 1 };

  /** Indica si se esta guardando la habitacion en edicion. @type {boolean} */
  let guardandoHabitacion = false;

  /** Mensaje de retroalimentacion en el modal de habitacion. @type {{tipo: string, texto: string}|null} */
  let mensajeHabitacion = null;

  /** Indica si se esta subiendo una imagen en el modal de habitacion. @type {boolean} */
  let subiendoImgHab = false;

  /** Mensaje de retroalimentacion en la seccion de imagenes del modal de habitacion. @type {{tipo: string, texto: string}|null} */
  let mensajeImgHab = null;

  /** Controla la visibilidad del modal de nueva habitacion. @type {boolean} */
  let showModalNuevaHab = false;

  /**
   * Datos del formulario de nueva habitacion en el modal de gestion.
   * Sin numeroHabitacion: el backend lo asigna automaticamente.
   * @type {Object}
   */
  let nuevaHabGestion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1 };

  /** Indica si se esta creando la nueva habitacion desde el modal de gestion. @type {boolean} */
  let guardandoNuevaHab = false;

  /** Mensaje de retroalimentacion en el modal de nueva habitacion. @type {{tipo: string, texto: string}|null} */
  let mensajeNuevaHab = null;

  /** Controla la visibilidad del modal de confirmacion de eliminacion de habitacion. @type {boolean} */
  let showModalEliminarHab = false;

  /** Habitacion pendiente de eliminar. @type {Object|null} */
  let habEliminando = null;

  /** Indica si la eliminacion de habitacion esta en curso. @type {boolean} */
  let eliminandoHab = false;

  /** Controla la visibilidad del modal de cierre suave de habitacion. @type {boolean} */
  let showModalCerrarHab = false;

  /** Habitacion pendiente de cerrar. @type {Object|null} */
  let habCerrando = null;

  /** Reservaciones activas de la habitacion que se va a cerrar. @type {{ count: number, reservaciones: Array }|null} */
  let reservasActivasHab = null;

  /** Indica si se estan cargando las reservas activas de la habitacion. @type {boolean} */
  let cargandoReservasHab = false;

  /** Indica si el cierre/eliminacion de habitacion con cancelaciones esta en curso. @type {boolean} */
  let cerrandoHab = false;

  /** Controla la visibilidad del modal de reactivacion de habitacion. @type {boolean} */
  let showModalReactivarHab = false;

  /** Habitacion pendiente de reactivar. @type {Object|null} */
  let habReactivando = null;

  /** Indica si la reactivacion de habitacion esta en curso. @type {boolean} */
  let reactivandoHab = false;

  /** Controla la visibilidad del modal de confirmacion de eliminacion de hotel. @type {boolean} */
  let showModalEliminarHotel = false;

  /** Controla la visibilidad del modal de cierre suave de hotel. @type {boolean} */
  let showModalCerrarHotel = false;

  /** Controla la visibilidad del modal de reactivacion de hotel. @type {boolean} */
  let showModalReactivarHotel = false;

  /** Hotel pendiente de eliminar o cerrar. @type {Object|null} */
  let hotelEliminando = null;

  /** Hotel pendiente de reactivar. @type {Object|null} */
  let hotelReactivando = null;

  /** Indica si la eliminacion o cierre de hotel esta en curso. @type {boolean} */
  let eliminandoHotel = false;

  /** Indica si la reactivacion de hotel esta en curso. @type {boolean} */
  let reactivandoHotel = false;

  /** Indica si hay una operacion masiva de creacion en progreso. @type {boolean} */
  let creandoMasivo = false;

  /** Mensaje de progreso durante la creacion masiva. @type {string} */
  let creandoMasivoProgreso = '';

  /** Indica si hay una operacion masiva de eliminacion en progreso. @type {boolean} */
  let eliminandoMasivo = false;

  /** Mensaje de progreso durante la eliminacion masiva. @type {string} */
  let eliminandoMasivoProgreso = '';

  // Lista de hoteles filtrada reactivamente por busqueda y estado.
  $: hotelesFiltrados = hoteles.filter(h => {
    const q = busquedaHotel.toLowerCase();
    const matchBusqueda = q === '' || h.nombre.toLowerCase().includes(q) || (h.ciudad ?? '').toLowerCase().includes(q) || (h.pais ?? '').toLowerCase().includes(q);
    const estadoNorm = h.estado?.toLowerCase() ?? '';
    return matchBusqueda && (filtroEstadoHotel === 'todos' || estadoNorm === filtroEstadoHotel);
  });

  // Mantiene el contador exportado sincronizado con el total de hoteles.
  $: count = hoteles.length;

  onMount(() => { cargarHoteles(); });

  /**
   * Carga la lista completa de hoteles desde el backend.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarHoteles() {
    cargandoHoteles = true; errorHoteles = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hoteles = await res.json();
    } catch (e) { errorHoteles = 'No se pudo cargar la lista de hoteles. ' + e.message; }
    finally { cargandoHoteles = false; }
  }

  /**
   * Abre la vista de detalle de un hotel y carga sus habitaciones y amenidades.
   * @param {Object} h - Hotel a mostrar en detalle.
   */
  function abrirDetalleHotel(h) {
    hotelDetalle = { ...h };
    editInfoHotel = { nombre: h.nombre ?? '', direccion: h.direccion ?? '', descripcion: h.descripcion ?? '', rating: h.rating ?? 0, estadoId: h.estadoId ?? 1 };
    tabDetalle = 'info'; mensajeInfo = null; vistaHoteles = 'detalle'; amenidadesHotel = [];
    cargarHabitacionesDetalle(h.id); cargarAmenidadesHotel(h.id);
  }

  /**
   * Vuelve a la vista de lista y limpia el estado del detalle.
   */
  function volverListaHoteles() { vistaHoteles = 'lista'; hotelDetalle = null; habitaciones = []; }

  /**
   * Guarda los cambios de informacion basica del hotel mediante PATCH.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarInfoHotel() {
    guardandoInfo = true; mensajeInfo = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}`, {
        method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: editInfoHotel.nombre, direccion: editInfoHotel.direccion, descripcion: editInfoHotel.descripcion, rating: Number(editInfoHotel.rating), estadoId: Number(editInfoHotel.estadoId) })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeInfo = { tipo: 'ok', texto: 'Hotel actualizado correctamente.' };
      const estadoStr = editInfoHotel.estadoId === 1 ? 'Activo' : 'Cerrado';
      hotelDetalle = { ...hotelDetalle, ...editInfoHotel, estado: estadoStr };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, ...editInfoHotel, estado: estadoStr } : h);
    } catch (e) { mensajeInfo = { tipo: 'error', texto: e.message }; }
    finally { guardandoInfo = false; }
  }

  /**
   * Reservaciones activas del hotel que se esta por eliminar.
   * Se carga al abrir el modal de confirmacion de eliminacion.
   * @type {{ count: number, reservaciones: Array } | null}
   */
  let reservasActivasHotel = null;
  let cargandoReservasHotel = false;

  /**
   * Abre el modal de eliminacion definitiva del hotel y precarga sus reservas activas.
   * @param {Object} h - Hotel a eliminar.
   */
  function abrirEliminarHotel(h) {
    hotelEliminando = h; reservasActivasHotel = null; showModalEliminarHotel = true;
    _cargarReservasHotel(h.id);
  }

  /** Atajo para abrir el modal de eliminacion desde la vista de detalle. */
  function abrirEliminarHotelDetalle() { abrirEliminarHotel(hotelDetalle); }

  /**
   * Abre el modal de cierre suave del hotel y precarga sus reservas activas.
   * @param {Object} h - Hotel a cerrar.
   */
  function abrirCerrarHotel(h) {
    hotelEliminando = h; reservasActivasHotel = null; showModalCerrarHotel = true;
    _cargarReservasHotel(h.id);
  }

  /** Atajo para abrir el modal de cierre suave desde la vista de detalle. */
  function abrirCerrarHotelDetalle() { abrirCerrarHotel(hotelDetalle); }

  /**
   * Abre el modal de reactivacion del hotel.
   * @param {Object} h - Hotel a reactivar.
   */
  function abrirReactivarHotel(h) { hotelReactivando = h; showModalReactivarHotel = true; }

  /** Atajo para abrir el modal de reactivacion desde la vista de detalle. */
  function abrirReactivarHotelDetalle() { abrirReactivarHotel(hotelDetalle); }

  /**
   * Carga las reservaciones activas del hotel para mostrarlas en el modal de confirmacion.
   * @async
   * @param {number} hotelId - ID del hotel.
   */
  async function _cargarReservasHotel(hotelId) {
    cargandoReservasHotel = true;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelId}/reservas-activas`, { credentials: 'include' });
      if (res.ok) reservasActivasHotel = await res.json();
    } catch {}
    finally { cargandoReservasHotel = false; }
  }

  /**
   * Elimina el hotel seleccionado del backend de forma definitiva y lo quita de la lista local.
   * Si el hotel no tiene reservas activas, usa DELETE normal.
   * Si tiene reservas activas, usa POST /cerrar-con-cancelaciones con eliminarDefinitivo=true.
   * @async
   * @param {boolean} volverLista - Si es true, vuelve a la vista de lista tras eliminar.
   * @returns {Promise<void>}
   */
  async function _eliminarHotel(volverLista) {
    if (!hotelEliminando) return;
    eliminandoHotel = true; showModalEliminarHotel = false;
    eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando hotel "${hotelEliminando.nombre}"...`;
    const hayReservas = reservasActivasHotel && reservasActivasHotel.count > 0;
    try {
      let res;
      if (hayReservas) {
        // Cancelar reservas activas, enviar emails y eliminar definitivamente
        eliminandoMasivoProgreso = `Cancelando ${reservasActivasHotel.count} reserva(s) y eliminando hotel...`;
        res = await fetch(`${API_BASE}/admin/hoteles/${hotelEliminando.id}/cerrar-con-cancelaciones`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ hotelNombre: hotelEliminando.nombre, eliminarDefinitivo: true })
        });
      } else {
        res = await fetch(`${API_BASE}/admin/hoteles/${hotelEliminando.id}`, { method: 'DELETE', credentials: 'include' });
      }
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      hoteles = hoteles.filter(h => h.id !== hotelEliminando.id);
      hotelEliminando = null; reservasActivasHotel = null;
      if (volverLista) volverListaHoteles();
    } catch (e) { mensajeInfo = { tipo: 'error', texto: 'No se pudo eliminar el hotel: ' + e.message }; }
    finally { eliminandoHotel = false; eliminandoMasivo = false; }
  }

  /**
   * Cierra el hotel (soft-close: EstadoID=2) cancelando reservas activas si las hay.
   * El hotel permanece en BD y puede reactivarse.
   * @async
   * @param {boolean} volverLista - No vuelve a la lista; solo actualiza el estado local.
   * @returns {Promise<void>}
   */
  async function _cerrarHotel() {
    if (!hotelEliminando) return;
    eliminandoHotel = true; showModalCerrarHotel = false;
    eliminandoMasivo = true; eliminandoMasivoProgreso = `Cerrando hotel "${hotelEliminando.nombre}"...`;
    const hayReservas = reservasActivasHotel && reservasActivasHotel.count > 0;
    if (hayReservas) eliminandoMasivoProgreso = `Cancelando ${reservasActivasHotel.count} reserva(s) y cerrando hotel...`;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelEliminando.id}/cerrar-con-cancelaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ hotelNombre: hotelEliminando.nombre, eliminarDefinitivo: false })
      });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      // Actualizar estado local: el hotel sigue en la lista pero como "Cerrado"
      const hotelId = hotelEliminando.id;
      hoteles = hoteles.map(h => h.id === hotelId ? { ...h, estadoId: 2, estado: 'Cerrado' } : h);
      if (hotelDetalle?.id === hotelId) hotelDetalle = { ...hotelDetalle, estadoId: 2, estado: 'Cerrado' };
      hotelEliminando = null; reservasActivasHotel = null;
      mensajeInfo = { tipo: 'ok', texto: 'Hotel cerrado. Puedes reactivarlo desde el panel.' };
    } catch (e) { mensajeInfo = { tipo: 'error', texto: 'No se pudo cerrar el hotel: ' + e.message }; }
    finally { eliminandoHotel = false; eliminandoMasivo = false; }
  }

  /**
   * Reactiva el hotel cerrado (EstadoID=1). El hotel vuelve a aparecer en busquedas.
   * @async
   * @returns {Promise<void>}
   */
  async function _reactivarHotel() {
    if (!hotelReactivando) return;
    reactivandoHotel = true; showModalReactivarHotel = false;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelReactivando.id}/reactivar`, {
        method: 'POST', credentials: 'include'
      });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      const hotelId = hotelReactivando.id;
      hoteles = hoteles.map(h => h.id === hotelId ? { ...h, estadoId: 1, estado: 'Activo' } : h);
      if (hotelDetalle?.id === hotelId) hotelDetalle = { ...hotelDetalle, estadoId: 1, estado: 'Activo' };
      hotelReactivando = null;
      mensajeInfo = { tipo: 'ok', texto: 'Hotel reactivado. Ya aparece en búsquedas.' };
    } catch (e) { mensajeInfo = { tipo: 'error', texto: 'No se pudo reactivar: ' + e.message }; }
    finally { reactivandoHotel = false; }
  }

  /**
   * Sube una imagen al hotel actualmente en detalle.
   * @async
   * @param {Event} event - Evento del input file.
   * @returns {Promise<void>}
   */
  async function subirImagenHotel(event) {
    const file = event.target.files[0]; if (!file) return;
    if (file.size > 7 * 1024 * 1024) {
      mensajeImgHotel = { tipo: 'error', texto: 'La imagen excede 7 MB. Usa una imagen más pequeña.' };
      event.target.value = ''; return;
    }
    subiendoImgHotel = true; mensajeImgHotel = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      if (!res.ok) {
        const ct = res.headers.get('content-type') || '';
        let msg;
        if (ct.includes('application/json')) {
          const err = await res.json().catch(() => ({}));
          msg = err.mensaje || `Error ${res.status}`;
        } else {
          msg = res.status === 413
            ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).'
            : (await res.text().catch(() => '') || `Error ${res.status}`);
        }
        throw new Error(msg);
      }
      const data = await res.json();
      hotelDetalle = { ...hotelDetalle, imagenesIds: [...(hotelDetalle.imagenesIds ?? []), data.id] };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
      mensajeImgHotel = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) { mensajeImgHotel = { tipo: 'error', texto: e.message }; }
    finally { subiendoImgHotel = false; event.target.value = ''; }
  }

  /**
   * Solicita confirmacion antes de eliminar una imagen del hotel.
   * @param {number} imagenId - ID de la imagen a eliminar.
   */
  function pedirEliminarImgHotel(imagenId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen del hotel?', () => _eliminarImgHotel(imagenId)); }

  /**
   * Elimina una imagen del hotel en el backend y actualiza el estado local.
   * @async
   * @param {number} imagenId - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImgHotel(imagenId) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      hotelDetalle = { ...hotelDetalle, imagenesIds: (hotelDetalle.imagenesIds ?? []).filter(id => id !== imagenId) };
      hoteles = hoteles.map(h => h.id === hotelDetalle.id ? { ...h, imagenesIds: hotelDetalle.imagenesIds } : h);
    } catch (e) { mensajeImgHotel = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  /**
   * Carga en paralelo el catalogo de amenidades y las amenidades asignadas al hotel.
   * @async
   * @param {number} hotelId - ID del hotel.
   * @returns {Promise<void>}
   */
  async function cargarAmenidadesHotel(hotelId) {
    cargandoAmenidades = true; mensajeAmenidad = null;
    try {
      const [rA, rH] = await Promise.all([
        fetch(`${API_BASE}/admin/amenidades`, { credentials: 'include' }),
        fetch(`${API_BASE}/admin/hoteles/${hotelId}/amenidades`, { credentials: 'include' }),
      ]);
      if (rA.ok) amenidades = await rA.json();
      if (rH.ok) amenidadesHotel = await rH.json();
    } catch(e) {}
    finally { cargandoAmenidades = false; }
  }

  /**
   * Crea una nueva categoria de amenidad en el catalogo global.
   * @async
   * @returns {Promise<void>}
   */
  async function crearAmenidadCatalogo() {
    if (!nuevaAmenidadCatalogoNombre.trim()) { mensajeNuevaAmenidadCatalogo = { tipo: 'error', texto: 'El nombre es obligatorio.' }; return; }
    creandoAmenidadCatalogo = true; mensajeNuevaAmenidadCatalogo = null;
    try {
      const res = await fetch(`${API_BASE}/admin/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nombre: nuevaAmenidadCatalogoNombre.trim() }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      amenidades = [...amenidades, { id: data.id, nombre: data.nombre }];
      mensajeNuevaAmenidadCatalogo = { tipo: 'ok', texto: `"${data.nombre}" añadida al catálogo.` };
      nuevaAmenidadCatalogoNombre = '';
      setTimeout(() => { showFormNuevaAmenidadCatalogo = false; mensajeNuevaAmenidadCatalogo = null; }, 1500);
    } catch (e) { mensajeNuevaAmenidadCatalogo = { tipo: 'error', texto: e.message }; }
    finally { creandoAmenidadCatalogo = false; }
  }

  /**
   * Asigna una amenidad del catalogo al hotel actual.
   * @async
   * @returns {Promise<void>}
   */
  async function agregarAmenidadHotel() {
    mensajeAmenidad = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/amenidades`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: Number(nuevaAmenidad.amenidadId), descripcion: nuevaAmenidad.descripcion }) });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      const catAm = amenidades.find(a => a.id === Number(nuevaAmenidad.amenidadId));
      amenidadesHotel = [...amenidadesHotel, { id: data.id, hotelId: hotelDetalle.id, amenidadId: Number(nuevaAmenidad.amenidadId), amenidadNombre: catAm?.nombre ?? '', descripcion: nuevaAmenidad.descripcion, imagenesIds: [] }];
      mensajeAmenidad = { tipo: 'ok', texto: 'Amenidad agregada.' };
      showFormAmenidad = false; nuevaAmenidad = { amenidadId: 1, descripcion: '' };
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  /**
   * Guarda la descripcion editada de una amenidad del hotel.
   * @async
   * @param {Object} ha - Amenidad asignada al hotel.
   * @returns {Promise<void>}
   */
  async function guardarDescAmenidad(ha) {
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${ha.id}`, { method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ amenidadId: ha.amenidadId, descripcion: editDescAmenidad }) });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      amenidadesHotel = amenidadesHotel.map(a => a.id === ha.id ? { ...a, descripcion: editDescAmenidad } : a);
      amenidadEditandoId = null;
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  /**
   * Solicita confirmacion antes de eliminar una amenidad del hotel.
   * @param {number} haId - ID de la amenidad asignada.
   * @param {string} nombre - Nombre de la amenidad para el mensaje.
   */
  function pedirEliminarAmenidad(haId, nombre) { pedirConfirmacion('Eliminar amenidad', `¿Eliminar "${nombre}" y sus imágenes?`, () => _eliminarAmenidad(haId)); }

  /**
   * Elimina una amenidad del hotel en el backend.
   * @async
   * @param {number} haId - ID de la amenidad asignada.
   * @returns {Promise<void>}
   */
  async function _eliminarAmenidad(haId) {
    try {
      await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}`, { method: 'DELETE', credentials: 'include' });
      amenidadesHotel = amenidadesHotel.filter(a => a.id !== haId);
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: e.message }; }
  }

  /**
   * Sube una imagen a una amenidad especifica del hotel.
   * @async
   * @param {Event} event - Evento del input file.
   * @param {number} haId - ID de la amenidad destino.
   * @returns {Promise<void>}
   */
  async function subirImagenAmenidad(event, haId) {
    const file = event.target.files[0]; if (!file) return;
    if (file.size > 7 * 1024 * 1024) {
      mensajeAmenidad = { tipo: 'error', texto: 'La imagen excede 7 MB. Usa una imagen más pequeña.' };
      event.target.value = ''; return;
    }
    subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet, haId]);
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/hoteles/amenidades/${haId}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      if (!res.ok) {
        const ct = res.headers.get('content-type') || '';
        let msg;
        if (ct.includes('application/json')) {
          const err = await res.json().catch(() => ({}));
          msg = err.mensaje || `Error ${res.status}`;
        } else {
          msg = res.status === 413
            ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).'
            : (await res.text().catch(() => '') || `Error ${res.status}`);
        }
        throw new Error(msg);
      }
      const data = await res.json();
      amenidadesHotel = amenidadesHotel.map(a => a.id === haId ? { ...a, imagenesIds: [...(a.imagenesIds ?? []), data.id] } : a);
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: 'Error subiendo imagen: ' + e.message }; }
    finally { subiendoImgAmenidadSet = new Set([...subiendoImgAmenidadSet].filter(id => id !== haId)); event.target.value = ''; }
  }

  /**
   * Solicita confirmacion antes de eliminar una imagen de una amenidad.
   * @param {number} haId - ID de la amenidad.
   * @param {number} imgId - ID de la imagen.
   */
  function pedirEliminarImgAmenidad(haId, imgId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen de la amenidad?', () => _eliminarImgAmenidad(haId, imgId)); }

  /**
   * Elimina una imagen de una amenidad del hotel.
   * @async
   * @param {number} haId - ID de la amenidad.
   * @param {number} imgId - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImgAmenidad(haId, imgId) {
    try {
      await fetch(`${API_BASE}/admin/hoteles/amenidades/imagenes/${imgId}`, { method: 'DELETE', credentials: 'include' });
      amenidadesHotel = amenidadesHotel.map(a => { if (a.id !== haId) return a; return { ...a, imagenesIds: (a.imagenesIds ?? []).filter(i => i !== imgId) }; });
    } catch(e) { mensajeAmenidad = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  /**
   * Carga la lista de habitaciones del hotel seleccionado.
   * @async
   * @param {number} hotelId - ID del hotel.
   * @returns {Promise<void>}
   */
  async function cargarHabitacionesDetalle(hotelId) {
    cargandoHabitaciones = true; errorHabitaciones = null;
    try {
      const res = await fetch(`${API_BASE}/admin/hoteles/${hotelId}/habitaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      habitaciones = await res.json();
    } catch (e) { errorHabitaciones = 'No se pudieron cargar las habitaciones. ' + e.message; }
    finally { cargandoHabitaciones = false; }
  }

  /**
   * Abre el modal de edicion de una habitacion existente. Incluye el numeroHabitacion
   * ya que la habitacion ya existe en la base de datos y el campo es editable.
   * @param {Object} h - Habitacion a editar.
   */
  function abrirEditarHabitacion(h) {
    habitacionEditando = { ...h };
    editHabitacion = {
      tipoHabitacionId: h.tipoHabitacionId,
      numeroHabitacion: h.numeroHabitacion ?? '',
      descripcion:      h.descripcion ?? '',
      estadoId:         h.estadoId,
    };
    mensajeHabitacion = null; mensajeImgHab = null; showModalHabitacion = true;
  }

  /**
   * Guarda los cambios de la habitacion editada mediante PATCH.
   * @async
   * @returns {Promise<void>}
   */
  async function guardarHabitacion() {
    guardandoHabitacion = true; mensajeHabitacion = null;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}`, {
        method: 'PATCH', credentials: 'include', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tipoHabitacionId: Number(editHabitacion.tipoHabitacionId),
          numeroHabitacion: editHabitacion.numeroHabitacion.trim(),
          descripcion:      editHabitacion.descripcion,
          estadoId:         Number(editHabitacion.estadoId),
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      mensajeHabitacion = { tipo: 'ok', texto: 'Habitación actualizada.' };
      await cargarHabitacionesDetalle(hotelDetalle.id);
    } catch (e) { mensajeHabitacion = { tipo: 'error', texto: e.message }; }
    finally { guardandoHabitacion = false; }
  }

  /**
   * Sube una imagen a la habitacion que se esta editando en el modal.
   * @async
   * @param {Event} event - Evento del input file.
   * @returns {Promise<void>}
   */
  async function subirImagenHabitacion(event) {
    const file = event.target.files[0]; if (!file) return;
    if (file.size > 7 * 1024 * 1024) {
      mensajeImgHab = { tipo: 'error', texto: 'La imagen excede 7 MB. Usa una imagen más pequeña.' };
      event.target.value = ''; return;
    }
    subiendoImgHab = true; mensajeImgHab = null;
    try {
      const base64 = await fileToBase64(file);
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionEditando.id}/imagenes`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ base64 }) });
      if (!res.ok) {
        const ct = res.headers.get('content-type') || '';
        let msg;
        if (ct.includes('application/json')) {
          const err = await res.json().catch(() => ({}));
          msg = err.mensaje || `Error ${res.status}`;
        } else {
          msg = res.status === 413
            ? 'La imagen es demasiado grande. Usa una imagen de menor tamaño (máximo 7 MB).'
            : (await res.text().catch(() => '') || `Error ${res.status}`);
        }
        throw new Error(msg);
      }
      const data = await res.json();
      const nuevosIds = [...(habitacionEditando.imagenesIds ?? []), data.id];
      habitacionEditando = { ...habitacionEditando, imagenesIds: nuevosIds };
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: nuevosIds } : h);
      mensajeImgHab = { tipo: 'ok', texto: 'Imagen agregada.' };
    } catch (e) { mensajeImgHab = { tipo: 'error', texto: e.message }; }
    finally { subiendoImgHab = false; event.target.value = ''; }
  }

  /**
   * Solicita confirmacion antes de eliminar una imagen de la habitacion en edicion.
   * @param {number} imagenId - ID de la imagen.
   */
  function pedirEliminarImgHab(imagenId) { pedirConfirmacion('Eliminar imagen', '¿Eliminar esta imagen de la habitación?', () => _eliminarImgHab(imagenId)); }

  /**
   * Elimina una imagen de la habitacion en edicion.
   * @async
   * @param {number} imagenId - ID de la imagen.
   * @returns {Promise<void>}
   */
  async function _eliminarImgHab(imagenId) {
    try {
      await fetch(`${API_BASE}/admin/habitaciones/imagenes/${imagenId}`, { method: 'DELETE', credentials: 'include' });
      const nuevosIds = (habitacionEditando.imagenesIds ?? []).filter(id => id !== imagenId);
      habitacionEditando = { ...habitacionEditando, imagenesIds: nuevosIds };
      habitaciones = habitaciones.map(h => h.id === habitacionEditando.id ? { ...h, imagenesIds: nuevosIds } : h);
    } catch (e) { mensajeImgHab = { tipo: 'error', texto: 'No se pudo eliminar: ' + e.message }; }
  }

  /**
   * Abre el modal de confirmacion de eliminacion de una habitacion.
   * @param {Object} h - Habitacion a eliminar.
   */
  function abrirEliminarHab(h) { habEliminando = h; showModalEliminarHab = true; }

  /**
   * Confirma y ejecuta la eliminacion de la habitacion seleccionada.
   * @async
   * @returns {Promise<void>}
   */
  async function confirmarEliminarHab() {
    if (!habEliminando) return;
    eliminandoHab = true; showModalEliminarHab = false;
    eliminandoMasivo = true; eliminandoMasivoProgreso = `Eliminando habitación #${habEliminando.id}...`;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habEliminando.id}`, { method: 'DELETE', credentials: 'include' });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      habitaciones = habitaciones.filter(h => h.id !== habEliminando.id);
      habEliminando = null;
    } catch (e) { mensajeHabitacion = { tipo: 'error', texto: e.message }; }
    finally { eliminandoHab = false; eliminandoMasivo = false; }
  }

  /**
   * Abre el modal de cierre suave de habitacion y precarga sus reservas activas.
   * @param {Object} h - Habitacion a cerrar.
   */
  function abrirCerrarHab(h) {
    habCerrando = h; reservasActivasHab = null; showModalCerrarHab = true;
    _cargarReservasHab(h.id);
  }

  /**
   * Carga las reservaciones activas de la habitacion para mostrarlas en el modal.
   * @async
   * @param {number} habitacionId - ID de la habitacion.
   */
  async function _cargarReservasHab(habitacionId) {
    cargandoReservasHab = true;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habitacionId}/reservas-activas`, { credentials: 'include' });
      if (res.ok) reservasActivasHab = await res.json();
    } catch {}
    finally { cargandoReservasHab = false; }
  }

  /**
   * Cierra (o elimina) la habitacion seleccionada con cancelacion de reservas.
   * @async
   * @param {boolean} eliminarDefinitivo - true elimina fisicamente, false solo cierra.
   */
  async function _cerrarHab(eliminarDefinitivo) {
    if (!habCerrando) return;
    cerrandoHab = true; showModalCerrarHab = false;
    eliminandoMasivo = true;
    eliminandoMasivoProgreso = eliminarDefinitivo
      ? `Eliminando habitación #${habCerrando.id}...`
      : `Cerrando habitación #${habCerrando.id}...`;
    const hayReservas = reservasActivasHab && reservasActivasHab.count > 0;
    if (hayReservas) eliminandoMasivoProgreso = `Cancelando ${reservasActivasHab.count} reserva(s) y ${eliminarDefinitivo ? 'eliminando' : 'cerrando'} habitación...`;
    try {
      const nombreHabitacion = `${habCerrando.tipoHabitacion} #${habCerrando.numeroHabitacion || habCerrando.id}`;
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habCerrando.id}/cerrar-con-cancelaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombreHabitacion, eliminarDefinitivo })
      });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      if (eliminarDefinitivo) {
        habitaciones = habitaciones.filter(h => h.id !== habCerrando.id);
      } else {
        habitaciones = habitaciones.map(h => h.id === habCerrando.id ? { ...h, estadoId: 2, estado: 'Cerrada' } : h);
      }
      habCerrando = null; reservasActivasHab = null;
    } catch (e) { mensajeHabitacion = { tipo: 'error', texto: e.message }; }
    finally { cerrandoHab = false; eliminandoMasivo = false; }
  }

  /**
   * Abre el modal de reactivacion de habitacion.
   * @param {Object} h - Habitacion a reactivar.
   */
  function abrirReactivarHab(h) { habReactivando = h; showModalReactivarHab = true; }

  /**
   * Reactiva la habitacion cerrada (ESTADO_ID=1).
   * @async
   */
  async function _reactivarHab() {
    if (!habReactivando) return;
    reactivandoHab = true; showModalReactivarHab = false;
    try {
      const res = await fetch(`${API_BASE}/admin/habitaciones/${habReactivando.id}/reactivar`, {
        method: 'POST', credentials: 'include'
      });
      if (!res.ok) { const data = await res.json().catch(() => ({})); throw new Error(data.mensaje || `Error ${res.status}`); }
      const habId = habReactivando.id;
      habitaciones = habitaciones.map(h => h.id === habId ? { ...h, estadoId: 1, estado: 'Activa' } : h);
      habReactivando = null;
    } catch (e) { mensajeHabitacion = { tipo: 'error', texto: 'No se pudo reactivar: ' + e.message }; }
    finally { reactivandoHab = false; }
  }

  /**
   * Abre el modal de nueva habitacion reseteando el formulario.
   * Sin numeroHabitacion: el backend lo asigna automaticamente (count + 1).
   */
  function abrirModalNuevaHab() {
    nuevaHabGestion = { tipoHabitacionId: 1, descripcion: '', estadoId: 1, cantidad: 1 };
    mensajeNuevaHab = null; showModalNuevaHab = true;
  }

  /**
   * Crea una o varias habitaciones nuevas en el hotel en detalle.
   * El numero de habitacion es generado automaticamente por el backend.
   * @async
   * @returns {Promise<void>}
   */
  async function crearHabGestion() {
    if (!hotelDetalle) return;
    const cant = Math.max(1, Math.min(50, Number(nuevaHabGestion.cantidad) || 1));
    guardandoNuevaHab = true; mensajeNuevaHab = null;
    if (cant > 1) { creandoMasivo = true; creandoMasivoProgreso = `Creando habitación 0 de ${cant}...`; }
    try {
      // Payload sin numeroHabitacion — el backend lo genera automaticamente (count + 1)
      const payload = {
        tipoHabitacionId: Number(nuevaHabGestion.tipoHabitacionId),
        descripcion:      nuevaHabGestion.descripcion,
        estadoId:         Number(nuevaHabGestion.estadoId),
      };
      const tipoNom = tiposHabitacion.find(t => t.id === payload.tipoHabitacionId)?.nombre ?? '';
      const estNom  = payload.estadoId === 1 ? 'Activa' : 'Cerrada';
      let creadas = 0;
      for (let i = 0; i < cant; i++) {
        if (cant > 1) creandoMasivoProgreso = `Creando habitación ${i + 1} de ${cant}...`;
        const res = await fetch(`${API_BASE}/admin/hoteles/${hotelDetalle.id}/habitaciones`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        const data = await res.json();
        if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
        creadas++;
      }
      await cargarHabitacionesDetalle(hotelDetalle.id);
      mensajeNuevaHab = { tipo: 'ok', texto: `${creadas} habitación(es) ${tipoNom} creada(s). Número asignado automáticamente.` };
    } catch (e) { mensajeNuevaHab = { tipo: 'error', texto: e.message }; }
    finally { guardandoNuevaHab = false; creandoMasivo = false; }
  }

  /**
   * Cierra todos los modales abiertos y limpia su estado relacionado.
   */
  function cerrarModales() {
    showModalHabitacion = false; showModalNuevaHab = false;
    showModalEliminarHab = false; showModalEliminarHotel = false;
    showModalCerrarHotel = false; showModalReactivarHotel = false;
    showModalCerrarHab = false; showModalReactivarHab = false;
    habitacionEditando = null; habEliminando = null; hotelEliminando = null; hotelReactivando = null;
    habCerrando = null; habReactivando = null; reservasActivasHab = null;
  }

  /**
   * Maneja la tecla Escape sobre los overlays para cerrar modales.
   * @param {KeyboardEvent} e
   */
  function handleOverlayKey(e) { if (e.key === 'Escape') cerrarModales(); }
</script>

<!-- Overlay bloqueante durante creacion masiva de habitaciones -->
{#if creandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#7b93ff" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Creando habitaciones</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{creandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera. No cierres esta página.</p>
    </div>
  </div>
{/if}

<!-- Overlay bloqueante durante eliminacion masiva -->
{#if eliminandoMasivo}
  <div class="adm__overlay" style="z-index:3500"></div>
  <div class="adm__confirm" style="z-index:3501;text-align:center">
    <div class="adm__confirm__header" style="justify-content:center;flex-direction:column;gap:.5rem;padding:1.5rem">
      <svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
      <p class="adm__confirm__title" style="font-size:1rem">Eliminando</p>
    </div>
    <div class="adm__confirm__body" style="padding:1rem 1.5rem 1.5rem">
      <p style="margin:0 0 .5rem;font-size:.9rem;color:#e8eeff">{eliminandoMasivoProgreso}</p>
      <p style="margin:0;font-size:.78rem;color:#8b949e">Por favor espera. No cierres esta página.</p>
    </div>
  </div>
{/if}

{#if vistaHoteles === 'lista'}
<!-- VISTA: Lista de hoteles con buscador, filtro de estado y tabla -->
  <div class="adm__filters-bar">
    <div class="adm__search-wrap">
      <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
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
    <div class="adm__loading-state"><svg class="adm__spinner" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando hoteles...</p></div>
  {:else if errorHoteles}
    <div class="adm__error-state"><p>{errorHoteles}</p><button class="adm__btn adm__btn--ghost" on:click={cargarHoteles}>Reintentar</button></div>
  {:else}
    <!-- Tabla de hoteles con miniatura, ubicacion, rating, habitaciones y acciones -->
    <div class="adm__card adm__card--no-pad">
      <div class="adm__table-wrap">
        <table class="adm__table">
          <thead><tr><th>Hotel</th><th>Ubicación</th><th>Dirección</th><th>Rating</th><th>Habitaciones</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>
            {#each hotelesFiltrados as h (h.id)}
              <tr>
                <td><div class="adm__hotel-mini"><div class="adm__hotel-mini-thumb">{#if h.imagenesIds?.length > 0}<img src="{API_BASE}/imagenes/hotel/{h.imagenesIds[0]}" alt={h.nombre} />{:else}<span>🏨</span>{/if}</div><div><p class="adm__hotel-mini-name">{h.nombre}</p><p class="adm__hotel-mini-id">ID #{h.id}</p></div></div></td>
                <td><p class="adm__hotel-city">{h.ciudad}</p><p class="adm__hotel-country">{h.pais}</p></td>
                <td class="adm__hotel-address">{h.direccion ?? '—'}</td>
                <td><div class="adm__rating-pill"><svg width="12" height="12" viewBox="0 0 24 24" fill="#f0a030" stroke="#f0a030" stroke-width="1"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>{h.rating?.toFixed(1) ?? '—'}</div></td>
                <td class="adm__table-center">{h.cantidadHabitaciones ?? '—'}</td>
                <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                <td>
                  <div style="display:flex;gap:.3rem">
                    <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirDetalleHotel(h)} title="Gestionar"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                    {#if h.estadoId === 2}
                      <button class="adm__icon-btn adm__icon-btn--success" on:click={() => abrirReactivarHotel(h)} title="Reactivar hotel"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg></button>
                    {:else}
                      <button class="adm__icon-btn adm__icon-btn--close" on:click={() => abrirCerrarHotel(h)} title="Cerrar hotel"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></button>
                    {/if}
                    <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => abrirEliminarHotel(h)} title="Eliminar definitivamente"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                  </div>
                </td>
              </tr>
            {/each}
            {#if hotelesFiltrados.length === 0}<tr><td colspan="7" class="adm__empty-cell">No se encontraron hoteles.</td></tr>{/if}
          </tbody>
        </table>
      </div>
    </div>
  {/if}

{:else}
<!-- VISTA: Detalle del hotel con pestanas de gestion -->
  <div class="adm__detalle-header">
    <button class="adm__btn adm__btn--ghost" on:click={volverListaHoteles}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>Volver a hoteles</button>
    <div class="adm__detalle-title"><h2>{hotelDetalle.nombre}</h2><span class="adm__badge {badge(hotelDetalle.estado)}">{hotelDetalle.estado}</span><span class="adm__detalle-loc">{hotelDetalle.ciudad}, {hotelDetalle.pais}</span></div>
    <div style="display:flex;gap:.5rem;margin-left:auto">
      {#if hotelDetalle.estadoId === 2}
        <button class="adm__btn adm__btn--success" on:click={abrirReactivarHotelDetalle}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>Reactivar Hotel</button>
      {:else}
        <button class="adm__btn adm__btn--warn" on:click={abrirCerrarHotelDetalle}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>Cerrar Hotel</button>
      {/if}
      <button class="adm__btn adm__btn--danger" on:click={abrirEliminarHotelDetalle}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>Eliminar Hotel</button>
    </div>
  </div>

  <!-- Pestanas: informacion, imagenes, amenidades, habitaciones -->
  <div class="adm__tabs">
    {#each [
      { key: 'info',         label: 'Información',  icon: 'M12 12a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 16v-4 M12 8h.01' },
      { key: 'imagenes',     label: `Imágenes (${hotelDetalle.imagenesIds?.length ?? 0})`, icon: 'M3 3h18v18H3z M8.5 8.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z M21 15l-5-5L5 21' },
      { key: 'amenidades',   label: `Amenidades (${amenidadesHotel.length})`, icon: 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z' },
      { key: 'habitaciones', label: `Habitaciones (${habitaciones.length})`, icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z M9 22V12h6v10' },
    ] as tab}
      <button class="adm__tab" class:adm__tab--active={tabDetalle === tab.key} on:click={() => tabDetalle = tab.key}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d={tab.icon}/></svg>
        {tab.label}
      </button>
    {/each}
  </div>

  <!-- Pestana: Informacion basica del hotel -->
  {#if tabDetalle === 'info'}
    <div class="adm__card adm__detalle-form-card">
      <div class="adm__form-grid">
        <div class="adm__field adm__field--full"><label>Nombre del Hotel</label><input type="text" bind:value={editInfoHotel.nombre} /></div>
        <div class="adm__field adm__field--full"><label>Dirección</label><input type="text" bind:value={editInfoHotel.direccion} /></div>
        <div class="adm__field"><label>Rating (0–5)</label><input type="number" bind:value={editInfoHotel.rating} min="0" max="5" step="0.1" /></div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editInfoHotel.descripcion} rows="4"></textarea></div>
      </div>
      {#if mensajeInfo}<div class="adm__feedback adm__feedback--{mensajeInfo.tipo}" style="margin-top:1rem">{mensajeInfo.texto}</div>{/if}
      <div style="display:flex;justify-content:flex-end;margin-top:1.25rem">
        <button class="adm__btn adm__btn--primary" on:click={guardarInfoHotel} disabled={guardandoInfo}>{#if guardandoInfo}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Guardando...{:else}Guardar cambios{/if}</button>
      </div>
    </div>

  <!-- Pestana: Imagenes del hotel -->
  {:else if tabDetalle === 'imagenes'}
    <div class="adm__card">
      <div class="adm__img-section-header">
        <p class="adm__img-section-title">Imágenes del hotel</p>
        <label class="adm__btn adm__btn--primary adm__upload-btn">{#if subiendoImgHotel}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Subiendo...{:else}+ Agregar imagen{/if}<input type="file" accept="image/*" on:change={subirImagenHotel} disabled={subiendoImgHotel} style="display:none" /></label>
      </div>
      {#if mensajeImgHotel}<div class="adm__feedback adm__feedback--{mensajeImgHotel.tipo}" style="margin-bottom:1rem">{mensajeImgHotel.texto}</div>{/if}
      {#if hotelDetalle.imagenesIds?.length > 0}
        <div class="adm__img-grid">{#each hotelDetalle.imagenesIds as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/hotel/{imgId}" alt="Imagen {imgId}" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHotel(imgId)}><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>
      {:else}<div class="adm__img-empty"><p>Sin imágenes. Agrega la primera.</p></div>{/if}
    </div>

  <!-- Pestana: Amenidades del hotel -->
  {:else if tabDetalle === 'amenidades'}
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;flex-wrap:wrap;gap:.5rem">
      <span style="color:var(--adm-text-muted);font-size:.85rem">{amenidadesHotel.length} amenidad(es) asignada(s)</span>
      <div style="display:flex;gap:.6rem">
        <button class="adm__btn adm__btn--ghost" on:click={() => { showFormNuevaAmenidadCatalogo = !showFormNuevaAmenidadCatalogo; mensajeNuevaAmenidadCatalogo = null; nuevaAmenidadCatalogoNombre = ''; }}>
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/><line x1="12" y1="9" x2="12" y2="15"/><line x1="9" y1="12" x2="15" y2="12"/></svg>
          Nueva categoría
        </button>
        <button class="adm__btn adm__btn--primary" on:click={() => { showFormAmenidad = true; mensajeAmenidad = null; }}>+ Asignar amenidad</button>
      </div>
    </div>

    <!-- Formulario de nueva categoria de amenidad en el catalogo global -->
    {#if showFormNuevaAmenidadCatalogo}
      <div class="adm__wizard-subcard" style="margin-bottom:1rem;border-color:rgba(123,147,255,.35)">
        <p class="adm__modal-section-title" style="color:#a5b4fc">Nueva categoría (catálogo global)</p>
        <div class="adm__form-grid"><div class="adm__field adm__field--full"><label>Nombre</label><input type="text" bind:value={nuevaAmenidadCatalogoNombre} placeholder="Ej: Spa, Terraza..." /></div></div>
        {#if mensajeNuevaAmenidadCatalogo}<div class="adm__feedback adm__feedback--{mensajeNuevaAmenidadCatalogo.tipo}" style="margin:.75rem 0">{mensajeNuevaAmenidadCatalogo.texto}</div>{/if}
        <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
          <button class="adm__btn adm__btn--ghost" on:click={() => showFormNuevaAmenidadCatalogo = false}>Cancelar</button>
          <button class="adm__btn adm__btn--primary" on:click={crearAmenidadCatalogo} disabled={creandoAmenidadCatalogo}>{#if creandoAmenidadCatalogo}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> Creando...{:else}Crear en catálogo{/if}</button>
        </div>
      </div>
    {/if}

    <!-- Formulario de asignacion de amenidad existente al hotel -->
    {#if showFormAmenidad}
      <div class="adm__wizard-subcard" style="margin-bottom:1rem">
        <p class="adm__modal-section-title">Asignar amenidad a {hotelDetalle.nombre}</p>
        <div class="adm__form-grid">
          <div class="adm__field"><label>Tipo</label><select bind:value={nuevaAmenidad.amenidadId}>{#each amenidades.filter(a => !amenidadesHotel.some(h => h.amenidadId === a.id)) as a}<option value={a.id}>{a.nombre}</option>{/each}</select></div>
          <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaAmenidad.descripcion} rows="2" placeholder="Ej: WiFi de alta velocidad..."></textarea></div>
        </div>
        <div style="display:flex;gap:.75rem;justify-content:flex-end;margin-top:.75rem">
          <button class="adm__btn adm__btn--ghost" on:click={() => showFormAmenidad = false}>Cancelar</button>
          <button class="adm__btn adm__btn--primary" on:click={agregarAmenidadHotel}>Asignar</button>
        </div>
      </div>
    {/if}

    {#if mensajeAmenidad}<div class="adm__feedback adm__feedback--{mensajeAmenidad.tipo}" style="margin-bottom:1rem">{mensajeAmenidad.texto}</div>{/if}

    {#if cargandoAmenidades}
      <div class="adm__loading-state"><svg class="adm__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando...</p></div>
    {:else if amenidadesHotel.length === 0 && !showFormAmenidad && !showFormNuevaAmenidadCatalogo}
      <div class="adm__img-empty" style="padding:2.5rem 0"><p>Sin amenidades. Asigna la primera.</p></div>
    {:else}
      <!-- Lista de amenidades con edicion inline de descripcion e imagenes -->
      <div style="display:flex;flex-direction:column;gap:.75rem">
        {#each amenidadesHotel as ha (ha.id)}
          <div class="adm__amenidad-card">
            <div class="adm__amenidad-header">
              <div class="adm__amenidad-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg></div>
              <div style="flex:1;min-width:0">
                <p class="adm__amenidad-nombre">{ha.amenidadNombre}</p>
                {#if amenidadEditandoId === ha.id}<textarea class="adm__amenidad-desc-input" bind:value={editDescAmenidad} rows="2"></textarea><div style="display:flex;gap:.5rem;margin-top:.5rem"><button class="adm__btn adm__btn--primary adm__btn--xs" on:click={() => guardarDescAmenidad(ha)}>Guardar</button><button class="adm__btn adm__btn--ghost adm__btn--xs" on:click={() => amenidadEditandoId = null}>Cancelar</button></div>{:else}<p class="adm__amenidad-desc">{ha.descripcion || '—'}</p>{/if}
              </div>
              <div style="display:flex;gap:.4rem;flex-shrink:0">
                {#if amenidadEditandoId !== ha.id}<button class="adm__icon-btn adm__icon-btn--edit" on:click={() => { amenidadEditandoId = ha.id; editDescAmenidad = ha.descripcion; }}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>{/if}
                <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => pedirEliminarAmenidad(ha.id, ha.amenidadNombre)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
              </div>
            </div>
            <div class="adm__amenidad-imgs"><div class="adm__img-grid adm__img-grid--sm">{#each (ha.imagenesIds ?? []) as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/amenidad/{imgId}" alt="img" /><button class="adm__img-delete" on:click={() => pedirEliminarImgAmenidad(ha.id, imgId)}><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}<label class="adm__wizard-add-img-btn adm__upload-btn">{#if subiendoImgAmenidadSet.has(ha.id)}<svg class="adm__spinner adm__spinner--sm" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>{:else}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{/if}<input type="file" accept="image/*" on:change={(e) => subirImagenAmenidad(e, ha.id)} disabled={subiendoImgAmenidadSet.has(ha.id)} style="display:none" /></label></div></div>
          </div>
        {/each}
      </div>
    {/if}

  <!-- Pestana: Habitaciones del hotel -->
  {:else if tabDetalle === 'habitaciones'}
    {#if cargandoHabitaciones}
      <div class="adm__loading-state"><svg class="adm__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg><p>Cargando habitaciones...</p></div>
    {:else if errorHabitaciones}
      <div class="adm__error-state"><p>{errorHabitaciones}</p><button class="adm__btn adm__btn--ghost" on:click={() => cargarHabitacionesDetalle(hotelDetalle.id)}>Reintentar</button></div>
    {:else}
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem">
        <span style="color:var(--adm-text-muted);font-size:.85rem">{habitaciones.length} habitación(es)</span>
        <button class="adm__btn adm__btn--primary" on:click={abrirModalNuevaHab}>+ Nueva Habitación</button>
      </div>
      <!-- Tabla de habitaciones: tipo, numero, cama, precio, capacidad, estado, imagenes -->
      <div class="adm__card adm__card--no-pad">
        <div class="adm__table-wrap">
          <table class="adm__table">
            <thead><tr><th>Tipo</th><th>Nro.</th><th>Cama</th><th>$/Noche</th><th>Cap.</th><th>Estado</th><th>Imgs</th><th>Acciones</th></tr></thead>
            <tbody>
              {#each habitaciones as h (h.id)}
                <tr>
                  <td><p style="font-weight:600;color:var(--adm-text);margin:0 0 2px">{h.tipoHabitacion}</p><p style="font-size:.72rem;color:var(--adm-text-muted);margin:0">ID #{h.id}</p></td>
                  <td class="adm__table-mono" style="font-size:.85rem">{h.numeroHabitacion || '—'}</td>
                  <td style="font-size:.85rem">{h.tipoCama || '—'}</td>
                  <td class="adm__table-money">$ {h.precioPorNoche?.toFixed(2) ?? '—'}</td>
                  <td class="adm__table-center">{h.capacidadMaxima ?? '—'}</td>
                  <td><span class="adm__badge {badge(h.estado)}">{h.estado}</span></td>
                  <td class="adm__table-center">{h.imagenesIds?.length ?? 0}</td>
                  <td>
                    <div style="display:flex;gap:.3rem">
                      <button class="adm__icon-btn adm__icon-btn--edit" on:click={() => abrirEditarHabitacion(h)} title="Editar habitación"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                      {#if h.estadoId === 2}
                        <button class="adm__icon-btn adm__icon-btn--success" on:click={() => abrirReactivarHab(h)} title="Reactivar habitación"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg></button>
                      {:else}
                        <button class="adm__icon-btn adm__icon-btn--close" on:click={() => abrirCerrarHab(h)} title="Cerrar habitación"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></button>
                      {/if}
                      <button class="adm__icon-btn adm__icon-btn--delete" on:click={() => abrirEliminarHab(h)} title="Eliminar definitivamente"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></button>
                    </div>
                  </td>
                </tr>
              {/each}
              {#if habitaciones.length === 0}<tr><td colspan="8" class="adm__empty-cell">No hay habitaciones registradas.</td></tr>{/if}
            </tbody>
          </table>
        </div>
      </div>
    {/if}
  {/if}
{/if}

<!-- MODALES -->

<!-- Modal de edicion de habitacion existente (incluye numeroHabitacion editable) -->
{#if showModalHabitacion && habitacionEditando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b;font-size:1.4rem;display:flex;align-items:center;justify-content:center;">{#if habitacionEditando.imagenesIds?.length > 0}<img src="{API_BASE}/imagenes/habitacion/{habitacionEditando.imagenesIds[0]}" alt="hab" />{:else}<span>🛏</span>{/if}</div>
      <div class="adm__hotel-modal__info"><p class="adm__hotel-modal__name">{habitacionEditando.tipoHabitacion} — ID #{habitacionEditando.id}</p><p class="adm__hotel-modal__loc">Nro. {habitacionEditando.numeroHabitacion || '—'}</p></div>
      <button class="adm__rol-modal__close" on:click={cerrarModales}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__hotel-modal__body">
      <p class="adm__modal-section-title">Datos de la habitación</p>
      <div class="adm__form-grid">
        <div class="adm__field"><label>Tipo de Habitación</label><select bind:value={editHabitacion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
        <div class="adm__field"><label>Número de Habitación</label><input type="text" bind:value={editHabitacion.numeroHabitacion} placeholder="Ej: 101, 202A..." /></div>
        <div class="adm__field"><label>Estado</label><select bind:value={editHabitacion.estadoId}><option value={1}>Activa</option><option value={2}>Cerrada</option></select></div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={editHabitacion.descripcion} rows="3" placeholder="Descripción..."></textarea></div>
      </div>
      <!-- Caracteristicas del tipo de habitacion (solo lectura, heredadas del tipo) -->
      <div style="margin:.75rem 0;padding:.75rem 1rem;background:rgba(123,147,255,.06);border:1px solid rgba(123,147,255,.15);border-radius:8px;font-size:.78rem;color:#8b95b0">
        <strong style="color:#a5b4fc">Características (solo lectura):</strong> Cama: {habitacionEditando.tipoCama || '—'} · Capacidad: {habitacionEditando.capacidadMaxima ?? '—'} pers. · Precio noche: $ {habitacionEditando.precioPorNoche?.toFixed(2) ?? '—'}
      </div>
      {#if mensajeHabitacion}<div class="adm__feedback adm__feedback--{mensajeHabitacion.tipo}" style="margin:.75rem 0">{mensajeHabitacion.texto}</div>{/if}
      <div style="display:flex;justify-content:flex-end;margin-bottom:1.5rem"><button class="adm__btn adm__btn--primary" on:click={guardarHabitacion} disabled={guardandoHabitacion}>{#if guardandoHabitacion}Guardando...{:else}Guardar cambios{/if}</button></div>
      <div class="adm__modal-section-divider"></div>
      <!-- Seccion de imagenes dentro del modal de edicion de habitacion -->
      <div class="adm__img-section-header" style="margin-top:1rem">
        <p class="adm__modal-section-title" style="margin:0">Imágenes</p>
        <label class="adm__btn adm__btn--ghost adm__upload-btn">{#if subiendoImgHab}Subiendo...{:else}+ Agregar{/if}<input type="file" accept="image/*" on:change={subirImagenHabitacion} disabled={subiendoImgHab} style="display:none" /></label>
      </div>
      {#if mensajeImgHab}<div class="adm__feedback adm__feedback--{mensajeImgHab.tipo}" style="margin:.5rem 0">{mensajeImgHab.texto}</div>{/if}
      {#if habitacionEditando.imagenesIds?.length > 0}
        <div class="adm__img-grid adm__img-grid--sm" style="margin-top:.75rem">{#each habitacionEditando.imagenesIds as imgId (imgId)}<div class="adm__img-card"><img src="{API_BASE}/imagenes/habitacion/{imgId}" alt="hab" /><button class="adm__img-delete" on:click={() => pedirEliminarImgHab(imgId)}><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>{/each}</div>
      {:else}<div class="adm__img-empty" style="padding:1.5rem 0"><p>Sin imágenes.</p></div>{/if}
    </div>
    <div class="adm__hotel-modal__footer"><button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cerrar</button></div>
  </div>
{/if}

<!-- Modal de nueva habitacion: sin campo de numero (lo asigna el backend automaticamente) -->
{#if showModalNuevaHab && hotelDetalle}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__hotel-modal adm__hotel-modal--wide">
    <div class="adm__hotel-modal__header">
      <div class="adm__hotel-modal__thumb" style="background:#252b3b;font-size:1.4rem;display:flex;align-items:center;justify-content:center;">🛏</div>
      <div class="adm__hotel-modal__info"><p class="adm__hotel-modal__name">Nueva Habitación</p><p class="adm__hotel-modal__loc">{hotelDetalle.nombre}</p></div>
      <button class="adm__rol-modal__close" on:click={cerrarModales}><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__hotel-modal__body">
      <p class="adm__modal-section-title">Datos de la habitación</p>
      <!-- Aviso informativo sobre la asignacion automatica del numero -->
      <div style="margin-bottom:.75rem;padding:.6rem .9rem;background:rgba(45,212,191,.06);border:1px solid rgba(45,212,191,.2);border-radius:8px;font-size:.78rem;color:#8b95b0">
        El número de habitación se asigna automáticamente según el total existente en el hotel.
      </div>
      <div class="adm__form-grid">
        <div class="adm__field"><label>Tipo de Habitación</label><select bind:value={nuevaHabGestion.tipoHabitacionId}>{#each tiposHabitacion as t}<option value={t.id}>{t.nombre}</option>{/each}</select></div>
        <div class="adm__field"><label>Estado</label><select bind:value={nuevaHabGestion.estadoId}><option value={1}>Activa</option><option value={2}>Cerrada</option></select></div>
        <div class="adm__field">
          <label>Cantidad a crear</label>
          <input type="number" bind:value={nuevaHabGestion.cantidad} min="1" max="50" />
          {#if nuevaHabGestion.cantidad > 50}<span class="adm__field-error">Máximo 50 por lote</span>{:else if nuevaHabGestion.cantidad > 1}<span style="font-size:.72rem;color:var(--adm-blue);margin-top:2px">Se crearán {nuevaHabGestion.cantidad} idénticas</span>{/if}
        </div>
        <div class="adm__field adm__field--full"><label>Descripción</label><textarea bind:value={nuevaHabGestion.descripcion} rows="3" placeholder="Descripción..."></textarea></div>
      </div>
      <!-- Nota: caracteristicas como precio, cama y capacidad se heredan del tipo de habitacion -->
      <div style="margin:.75rem 0;padding:.75rem 1rem;background:rgba(45,212,191,.06);border:1px solid rgba(45,212,191,.15);border-radius:8px;font-size:.78rem;color:#8b95b0">
        Las características (precio, cama, capacidad) son heredadas del <strong style="color:#2dd4bf">Tipo de Habitación</strong>.
      </div>
      {#if mensajeNuevaHab}<div class="adm__feedback adm__feedback--{mensajeNuevaHab.tipo}" style="margin:.75rem 0">{mensajeNuevaHab.texto}</div>{/if}
    </div>
    <div class="adm__hotel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales}>Cancelar</button>
      <button class="adm__btn adm__btn--primary" on:click={crearHabGestion} disabled={guardandoNuevaHab || nuevaHabGestion.cantidad > 50}>{#if guardandoNuevaHab}Creando...{:else}Crear {nuevaHabGestion.cantidad > 1 ? `${Math.min(nuevaHabGestion.cantidad,50)} Habitaciones` : 'Habitación'}{/if}</button>
    </div>
  </div>
{/if}

<!-- Modal de confirmacion para eliminar una habitacion -->
{#if showModalEliminarHab && habEliminando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:420px">
    <div class="adm__cancel-modal__header"><div class="adm__cancel-modal__icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></div><div><p class="adm__cancel-modal__title">Eliminar Habitación</p><p class="adm__cancel-modal__subtitle">{habEliminando.tipoHabitacion} Nro. {habEliminando.numeroHabitacion || habEliminando.id}</p></div><button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button></div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box"><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Tipo</span><span class="adm__cancel-info-row__value">{habEliminando.tipoHabitacion}</span></div><div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Número</span><span class="adm__cancel-info-row__value">{habEliminando.numeroHabitacion || '—'}</span></div></div>
      <div class="adm__cancel-warning"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span>Esta acción no se puede deshacer.</span></div>
    </div>
    <div class="adm__cancel-modal__footer"><button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={eliminandoHab}>Cancelar</button><button class="adm__btn--cancel-confirm" on:click={confirmarEliminarHab} disabled={eliminandoHab}>{#if eliminandoHab}Eliminando...{:else}Sí, eliminar{/if}</button></div>
  </div>
{/if}

<!-- Modal de confirmacion para eliminar un hotel completo -->
{#if showModalEliminarHotel && hotelEliminando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:500px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg></div>
      <div><p class="adm__cancel-modal__title">Eliminar Hotel</p><p class="adm__cancel-modal__subtitle">{hotelEliminando.nombre} — ID #{hotelEliminando.id}</p></div>
      <button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box">
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Hotel</span><span class="adm__cancel-info-row__value">{hotelEliminando.nombre}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Ubicación</span><span class="adm__cancel-info-row__value">{hotelEliminando.ciudad}, {hotelEliminando.pais}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Habitaciones</span><span class="adm__cancel-info-row__value">{hotelEliminando.cantidadHabitaciones ?? 0}</span></div>
      </div>

      <!-- Cargando reservas activas -->
      {#if cargandoReservasHotel}
        <p style="font-size:13px;color:#888;margin:.75rem 0;">Verificando reservaciones activas...</p>
      {:else if reservasActivasHotel && reservasActivasHotel.count > 0}
        <!-- El hotel tiene reservas activas: mostrar lista y advertencia fuerte -->
        <div class="adm__cancel-warning" style="border-left-color:#C62828;background:#FDECEA;">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <span>Este hotel tiene <strong>{reservasActivasHotel.count} reservación(es) en proceso</strong>. Se cancelarán todas y se notificará a cada usuario por correo.</span>
        </div>
        <div style="max-height:140px;overflow-y:auto;margin:.5rem 0;border:1px solid #e0e0e0;border-radius:6px;font-size:12px;">
          {#each reservasActivasHotel.reservaciones as rv}
            <div style="padding:6px 10px;border-bottom:1px solid #f0f0f0;display:flex;justify-content:space-between;">
              <span style="font-family:monospace;color:#1A3C5E;font-weight:600;">{rv.noReservacion}</span>
              <span style="color:#555;">{rv.correo}</span>
            </div>
          {/each}
        </div>
      {:else}
        <!-- Sin reservas activas: advertencia estándar -->
        <div class="adm__cancel-warning"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span>Se eliminarán <strong>todas</strong> las habitaciones, amenidades e imágenes.</span></div>
      {/if}
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={eliminandoHotel}>Cancelar</button>
      <button class="adm__btn--cancel-confirm" on:click={() => _eliminarHotel(vistaHoteles === 'detalle')} disabled={eliminandoHotel || cargandoReservasHotel}>
        {#if eliminandoHotel}
          Procesando...
        {:else if reservasActivasHotel && reservasActivasHotel.count > 0}
          Cancelar reservas y eliminar hotel
        {:else}
          Sí, eliminar hotel
        {/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de cierre suave de hotel (soft-close: EstadoID=2, sin eliminar) -->
{#if showModalCerrarHotel && hotelEliminando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:500px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon" style="background:rgba(245,158,11,0.15);color:#f59e0b;border-color:rgba(245,158,11,0.3)"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>
      <div><p class="adm__cancel-modal__title">Cerrar Hotel</p><p class="adm__cancel-modal__subtitle">{hotelEliminando.nombre} — ID #{hotelEliminando.id}</p></div>
      <button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box">
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Hotel</span><span class="adm__cancel-info-row__value">{hotelEliminando.nombre}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Ubicación</span><span class="adm__cancel-info-row__value">{hotelEliminando.ciudad}, {hotelEliminando.pais}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Habitaciones</span><span class="adm__cancel-info-row__value">{hotelEliminando.cantidadHabitaciones ?? 0}</span></div>
      </div>

      {#if cargandoReservasHotel}
        <p style="font-size:13px;color:#888;margin:.75rem 0;">Verificando reservaciones activas...</p>
      {:else if reservasActivasHotel && reservasActivasHotel.count > 0}
        <div class="adm__cancel-warning" style="border-left-color:#f59e0b;background:rgba(245,158,11,0.08);">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <span style="color:#f59e0b">Este hotel tiene <strong>{reservasActivasHotel.count} reservación(es) en proceso</strong>. Se cancelarán y se notificará a cada usuario por correo. El hotel quedará cerrado, <em>no eliminado</em>.</span>
        </div>
        <div style="max-height:140px;overflow-y:auto;margin:.5rem 0;border:1px solid #e0e0e0;border-radius:6px;font-size:12px;">
          {#each reservasActivasHotel.reservaciones as rv}
            <div style="padding:6px 10px;border-bottom:1px solid #f0f0f0;display:flex;justify-content:space-between;">
              <span style="font-family:monospace;color:#1A3C5E;font-weight:600;">{rv.noReservacion}</span>
              <span style="color:#555;">{rv.correo}</span>
            </div>
          {/each}
        </div>
      {:else}
        <div class="adm__cancel-warning" style="border-left-color:#f59e0b;background:rgba(245,158,11,0.08);"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span style="color:#f59e0b">El hotel quedará <strong>cerrado y oculto</strong> de las búsquedas. Puedes reactivarlo en cualquier momento.</span></div>
      {/if}
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={eliminandoHotel}>Cancelar</button>
      <button class="adm__btn--cancel-confirm adm__btn--cancel-confirm--warn" on:click={_cerrarHotel} disabled={eliminandoHotel || cargandoReservasHotel}>
        {#if eliminandoHotel}
          Procesando...
        {:else if reservasActivasHotel && reservasActivasHotel.count > 0}
          Cancelar reservas y cerrar hotel
        {:else}
          Cerrar hotel
        {/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de confirmacion de reactivacion de hotel -->
{#if showModalReactivarHotel && hotelReactivando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:420px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon" style="background:rgba(63,185,80,0.1);color:#3fb950;border-color:rgba(63,185,80,0.3)"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg></div>
      <div><p class="adm__cancel-modal__title">Reactivar Hotel</p><p class="adm__cancel-modal__subtitle">{hotelReactivando.nombre}</p></div>
      <button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-warning" style="border-left-color:#3fb950;background:rgba(63,185,80,0.07);"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#3fb950" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><polyline points="20 6 9 17 4 12"/></svg><span style="color:#3fb950">El hotel volverá a aparecer en las búsquedas públicas y estará disponible para reservas.</span></div>
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={reactivandoHotel}>Cancelar</button>
      <button class="adm__btn adm__btn--success" on:click={_reactivarHotel} disabled={reactivandoHotel} style="padding:.6rem 1.25rem;font-size:.85rem;">
        {#if reactivandoHotel}Procesando...{:else}Sí, reactivar hotel{/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de cierre suave de habitacion (soft-close: ESTADO_ID=2, sin eliminar / o eliminacion con cancelaciones) -->
{#if showModalCerrarHab && habCerrando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:500px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon" style="background:rgba(245,158,11,0.15);color:#f59e0b;border-color:rgba(245,158,11,0.3)"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg></div>
      <div><p class="adm__cancel-modal__title">Cerrar Habitación</p><p class="adm__cancel-modal__subtitle">{habCerrando.tipoHabitacion} — ID #{habCerrando.id}</p></div>
      <button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-info-box">
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Habitación</span><span class="adm__cancel-info-row__value">{habCerrando.tipoHabitacion}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">Nro.</span><span class="adm__cancel-info-row__value">{habCerrando.numeroHabitacion || '—'}</span></div>
        <div class="adm__cancel-info-row"><span class="adm__cancel-info-row__label">ID</span><span class="adm__cancel-info-row__value">#{habCerrando.id}</span></div>
      </div>

      {#if cargandoReservasHab}
        <p style="font-size:13px;color:#888;margin:.75rem 0;">Verificando reservaciones activas...</p>
      {:else if reservasActivasHab && reservasActivasHab.count > 0}
        <div class="adm__cancel-warning" style="border-left-color:#f59e0b;background:rgba(245,158,11,0.08);">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <span style="color:#f59e0b">Esta habitación tiene <strong>{reservasActivasHab.count} reservación(es) en proceso</strong>. Se cancelarán y se notificará a cada huésped por correo.</span>
        </div>
        <div style="max-height:140px;overflow-y:auto;margin:.5rem 0;border:1px solid #e0e0e0;border-radius:6px;font-size:12px;">
          {#each reservasActivasHab.reservaciones as rv}
            <div style="padding:6px 10px;border-bottom:1px solid #f0f0f0;display:flex;justify-content:space-between;">
              <span style="font-family:monospace;color:#1A3C5E;font-weight:600;">{rv.noReservacion}</span>
              <span style="color:#555;">{rv.correo}</span>
            </div>
          {/each}
        </div>
      {:else}
        <div class="adm__cancel-warning" style="border-left-color:#f59e0b;background:rgba(245,158,11,0.08);"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg><span style="color:#f59e0b">No hay reservaciones activas. La habitación quedará <strong>cerrada y oculta</strong> para nuevas reservas. Puedes reactivarla en cualquier momento.</span></div>
      {/if}
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={cerrandoHab}>Cancelar</button>
      <button class="adm__btn--cancel-confirm adm__btn--cancel-confirm--warn" on:click={() => _cerrarHab(false)} disabled={cerrandoHab || cargandoReservasHab}>
        {#if cerrandoHab}
          Procesando...
        {:else if reservasActivasHab && reservasActivasHab.count > 0}
          Cancelar reservas y cerrar
        {:else}
          Cerrar temporalmente
        {/if}
      </button>
      <button class="adm__btn--cancel-confirm" on:click={() => _cerrarHab(true)} disabled={cerrandoHab || cargandoReservasHab} style="background:#c0392b;">
        {#if cerrandoHab}
          Procesando...
        {:else}
          Eliminar definitivamente
        {/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de confirmacion de reactivacion de habitacion -->
{#if showModalReactivarHab && habReactivando}
  <div class="adm__overlay" on:click={cerrarModales} on:keydown={handleOverlayKey} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:420px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon" style="background:rgba(63,185,80,0.1);color:#3fb950;border-color:rgba(63,185,80,0.3)"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg></div>
      <div><p class="adm__cancel-modal__title">Reactivar Habitación</p><p class="adm__cancel-modal__subtitle">{habReactivando.tipoHabitacion} — #{habReactivando.id}</p></div>
      <button class="adm__cancel-modal__close" on:click={cerrarModales}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>
    </div>
    <div class="adm__cancel-modal__body">
      <div class="adm__cancel-warning" style="border-left-color:#3fb950;background:rgba(63,185,80,0.07);"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#3fb950" stroke-width="2" style="flex-shrink:0;margin-top:.1rem"><polyline points="20 6 9 17 4 12"/></svg><span style="color:#3fb950">La habitación volverá a estar disponible para nuevas reservas.</span></div>
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModales} disabled={reactivandoHab}>Cancelar</button>
      <button class="adm__btn adm__btn--success" on:click={_reactivarHab} disabled={reactivandoHab} style="padding:.6rem 1.25rem;font-size:.85rem;">
        {#if reactivandoHab}Procesando...{:else}Sí, reactivar habitación{/if}
      </button>
    </div>
  </div>
{/if}

<!-- Modal de confirmacion generico para acciones destructivas (eliminar imagenes y amenidades) -->
{#if confirmDialog}
  <div class="adm__overlay" on:click={cerrarConfirm} on:keydown={(e) => e.key === 'Escape' && cerrarConfirm()} role="button" tabindex="-1" aria-label="Cerrar"></div>
  <div class="adm__rol-modal" style="max-width:420px">
    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
      </div>
      <div>
        <p class="adm__cancel-modal__title">{confirmDialog.titulo}</p>
        <p class="adm__cancel-modal__subtitle">{confirmDialog.mensaje}</p>
      </div>
      <button class="adm__cancel-modal__close" on:click={cerrarConfirm}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>
    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarConfirm}>Cancelar</button>
      <button class="adm__btn--cancel-confirm" on:click={ejecutarConfirm}>Sí, eliminar</button>
    </div>
  </div>
{/if}