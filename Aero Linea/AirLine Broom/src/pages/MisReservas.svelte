<script>
/**
 * @file MisReservas.svelte
 * @description Pagina completa de gestion de reservaciones para usuarios autenticados. Carga
 * todas las reservaciones y un widget de resumen al montar, luego las muestra en una cuadricula
 * de tarjetas con filtros. Al hacer clic en una tarjeta se abre un modal de detalle que muestra
 * todos los boletos con ruta de vuelo, datos del pasajero, asiento e informacion de clase. Desde
 * el modal el usuario puede: calificar rutas completadas con un comentario de 1 a 5 estrellas,
 * cancelar reservaciones confirmadas con un campo obligatorio de motivo, descargar el comprobante
 * PDF de la reservacion o enviarlo al correo registrado. Las notificaciones toast confirman las
 * acciones exitosas. Redirige al login si no hay sesion activa.
 */
  import '../styles/Misreservas.css';
  import { sesion } from '../stores/sesion.js';
  import { onMount } from 'svelte';

  /** Funcion para navegar entre las paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Codigo de reservacion a pre-buscar, inyectado desde App.svelte cuando se navega desde el buscador global. @type {string|null} */
  export let buscarCodigo = null;

  import { API } from '../lib/api.js';

  /** Lista de todos los objetos de reservacion obtenidos para el usuario actual. @type {Array<object>} */
  let reservas       = [];

  /** Objeto de estadisticas de resumen con totales por estado y monto total gastado. @type {object|null} */
  let resumen        = null;

  /** Verdadero mientras se esta obteniendo la lista de reservaciones. @type {boolean} */
  let loading        = true;

  /** Mensaje de error mostrado cuando falla la obtencion de reservaciones. @type {string} */
  let error          = '';

  /** Clave del filtro activo actualmente aplicado a la lista de reservaciones. @type {string} */
  let filtroActivo   = 'todas';

  /** Texto de busqueda para filtrar reservaciones por codigo, numero de vuelo o destino. @type {string} */
  let buscarTexto    = '';

  /** Arreglo de objetos de comentario publicados por el usuario actual, usado para verificar si una ruta ya fue calificada. @type {Array<object>} */
  let misComentarios = [];

  /** El objeto de reservacion mostrado actualmente en el modal de detalle, null cuando el modal esta cerrado. @type {object|null} */
  let reservaDetalle    = null;

  /** Verdadero mientras se obtiene el detalle completo de la reservacion desde la API. @type {boolean} */
  let detalleLoading    = false;

  /** Mensaje de error mostrado dentro del modal de detalle cuando falla la obtencion. @type {string} */
  let detalleError      = '';

  /** Si el panel de formulario de cancelacion es visible dentro del modal de detalle. @type {boolean} */
  let cancelarAbierto   = false;

  /** Texto del motivo ingresado por el usuario en el formulario de cancelacion. @type {string} */
  let cancelMotivo      = '';

  /** Verdadero mientras la solicitud POST de cancelacion esta en progreso. @type {boolean} */
  let cancelLoading     = false;

  /** Mensaje de error de validacion o de la API mostrado dentro del formulario de cancelacion. @type {string} */
  let cancelError       = '';

  /** Calificacion de estrellas seleccionada al pasar el cursor o hacer clic en el formulario de comentario (1-5). @type {number} */
  let comentarEstrellas = 0;

  /** Cantidad de estrellas resaltadas actualmente por el hover del mouse en el widget de calificacion. @type {number} */
  let comentarHover     = 0;

  /** Contenido de texto del comentario que el usuario esta redactando. @type {string} */
  let comentarContenido = '';

  /** Verdadero mientras la solicitud POST de comentario esta en progreso. @type {boolean} */
  let comentarLoading   = false;

  /** Mensaje de error mostrado cuando el envio de comentario falla la validacion o la API retorna un error. @type {string} */
  let comentarError     = '';

  /** Mensaje de exito mostrado cuando un comentario se envia exitosamente. @type {string} */
  let comentarExito     = '';

  /** Verdadero mientras el comprobante PDF se esta descargando. @type {boolean} */
  let comprobanteLoading = false;

  /** Verdadero mientras la solicitud de envio de comprobante por correo esta en progreso. @type {boolean} */
  let enviarCorreoLoading = false;

  /** Arreglo de objetos de notificacion toast activos, cada uno con campos id, msg y tipo. @type {Array<{id: number, msg: string, tipo: string}>} */
  let toasts = [];

  onMount(async () => {
    if (!$sesion) { navigateTo('login'); return; }
    if (buscarCodigo) buscarTexto = buscarCodigo;
    await Promise.all([cargarReservas(), cargarResumen(), cargarMisComentarios()]);
  });

  /**
   * Crea una nueva notificacion toast y la elimina automaticamente despues de 4 segundos.
   * @param {string} msg - El texto del mensaje a mostrar en el toast.
   * @param {'success'|'error'} tipo - Estilo visual del toast.
   */
  function addToast(msg, tipo = 'success') {
    const id = Date.now();
    toasts = [...toasts, { id, msg, tipo }];
    setTimeout(() => { toasts = toasts.filter(t => t.id !== id); }, 4000);
  }

  /**
   * Mapea una cadena de estado de reservacion o boleto a su clase CSS modificadora de badge correspondiente.
   * Maneja Confirmada, Cancelada, Completada, Expirada y usa Pendiente como valor por defecto.
   * @param {string} estado - La cadena de etiqueta de estado de la API.
   * @returns {string} La cadena de clase CSS del badge.
   */
  function estadoClase(estado) {
    if (!estado) return 'mr-badge--pendiente';
    const e = estado.toLowerCase();
    if (e === 'confirmada') return 'mr-badge--confirmada';
    if (e === 'cancelada')  return 'mr-badge--cancelada';
    if (e === 'completada') return 'mr-badge--completada';
    if (e === 'expirada')   return 'mr-badge--expirada';
    return 'mr-badge--pendiente';
  }

  /**
   * Formatea una cadena ISO de fecha y hora en una cadena de fecha y hora localizada usando el locale es-GT.
   * Retorna '--' si el input es falsy.
   * @param {string} f - Cadena ISO de fecha y hora de la API.
   * @returns {string} Cadena de fecha y hora formateada como '06 abr 2026 14:30'.
   */
  function formatFechaHora(f) {
    if (!f) return '--';
    const d = new Date(f);
    return d.toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
      + ' ' + d.toLocaleTimeString('es-GT', { hour:'2-digit', minute:'2-digit' });
  }

  /**
   * Formatea una cadena ISO de fecha en una fecha corta localizada usando el locale es-GT.
   * Retorna '--' si el input es falsy.
   * @param {string} f - Cadena ISO de fecha.
   * @returns {string} Cadena de fecha formateada como '06 abr 2026'.
   */
  function formatFecha(f) {
    if (!f) return '--';
    return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' });
  }

  /**
   * Extrae la porcion HH:MM de una cadena de hora, retornando '--' si el input es falsy.
   * @param {string} h - Cadena de hora en formato HH:MM:SS o HH:MM.
   * @returns {string} Los primeros 5 caracteres de la cadena de hora, o '--'.
   */
  function formatHora(h) {
    if (!h) return '--';
    return h.substring(0, 5);
  }

  /**
   * Convierte una duracion de vuelo en minutos totales a una cadena legible Xh Ym.
   * Retorna '--' si el input es falsy.
   * @param {number} min - Duracion en minutos.
   * @returns {string} Duracion formateada como '2h 30m'.
   */
  function formatDuracion(min) {
    if (!min) return '--';
    const hrs = Math.floor(min / 60);
    const m = min % 60;
    return `${hrs}h${m > 0 ? ' ' + m + 'm' : ''}`;
  }

  /**
   * Verifica si el usuario actual ya publico un comentario de calificacion de nivel raiz para una ruta.
   * Solo considera comentarios con cantidadEstrellas no nulo y sin padre (nivel raiz).
   * @param {number} rutaId - El ID de ruta a verificar contra misComentarios.
   * @returns {boolean} Verdadero si existe un comentario coincidente.
   */
  function yaComentaRuta(rutaId) {
    return misComentarios.some(c => c.rutaId === rutaId && c.comentarioPadreId === null && c.cantidadEstrellas !== null);
  }

  /**
   * Retorna el primer comentario de calificacion de nivel raiz publicado por el usuario para una ruta dada.
   * @param {number} rutaId - El ID de ruta a buscar.
   * @returns {object|undefined} El objeto de comentario coincidente, o undefined si no se encuentra.
   */
  function obtenerComentarioRuta(rutaId) {
    return misComentarios.find(c => c.rutaId === rutaId && c.comentarioPadreId === null && c.cantidadEstrellas !== null);
  }

  /**
   * Obtiene la lista de reservaciones del usuario autenticado desde GET /api/mis-reservaciones.
   * Actualiza el arreglo reservas en caso de exito o establece un mensaje de error en caso de fallo.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarReservas() {
    loading = true; error = '';
    try {
      const r = await fetch(`${API}/api/mis-reservaciones`, { credentials: 'include' });
      if (r.ok) reservas = await r.json();
      else error = 'No se pudieron cargar tus reservas.';
    } catch { error = 'Error de conexion.'; }
    finally { loading = false; }
  }

  /**
   * Obtiene las estadisticas de resumen de reservaciones desde GET /api/mis-reservaciones/resumen.
   * Establece resumen en caso de exito; ignora errores silenciosamente ya que son datos suplementarios.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarResumen() {
    try {
      const r = await fetch(`${API}/api/mis-reservaciones/resumen`, { credentials: 'include' });
      if (r.ok) resumen = await r.json();
    } catch {}
  }

  /**
   * Obtiene todos los comentarios publicados por el usuario actual desde GET /api/comentarios/usuario.
   * Establece misComentarios en caso de exito; usado para determinar si una ruta ya fue calificada.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarMisComentarios() {
    try {
      const r = await fetch(`${API}/api/comentarios/usuario`, { credentials: 'include' });
      if (r.ok) misComentarios = await r.json();
    } catch {}
  }

  /**
   * Abre el modal de detalle de la reservacion para la reservacion dada. Obtiene el detalle completo
   * desde GET /api/mis-reservaciones/:id y actualiza reservaDetalle con la respuesta.
   * Restablece el estado del formulario de comentario y cancelacion antes de cargar.
   * @async
   * @param {object} reserva - El objeto de resumen de reservacion de la lista.
   * @returns {Promise<void>}
   */
  async function abrirDetalle(reserva) {
    detalleLoading = true;
    detalleError = '';
    reservaDetalle = reserva;
    resetComentarForm();
    cancelarAbierto = false;
    cancelMotivo = '';
    cancelError = '';
    try {
      const r = await fetch(`${API}/api/mis-reservaciones/${reserva.reservacionId}`, { credentials:'include' });
      if (r.ok) reservaDetalle = await r.json();
      else detalleError = 'No se pudo cargar el detalle.';
    } catch { detalleError = 'Error de conexion.'; }
    finally { detalleLoading = false; }
  }

  /**
   * Cierra el modal de detalle limpiando reservaDetalle y restableciendo el estado relacionado
   * incluyendo el panel de cancelacion y el formulario de comentario.
   */
  function cerrarDetalle() {
    reservaDetalle = null;
    detalleError = '';
    cancelarAbierto = false;
    resetComentarForm();
  }

  /**
   * Alterna el panel del formulario de cancelacion entre abierto y cerrado, limpiando
   * el motivo y el error cada vez que se alterna.
   */
  function toggleCancelar() {
    cancelarAbierto = !cancelarAbierto;
    cancelMotivo = '';
    cancelError = '';
  }

  /**
   * Envia la solicitud de cancelacion para la reservacion abierta actualmente mediante
   * POST /api/mis-reservaciones/:id/cancelar con el motivo escrito. En caso de exito,
   * muestra un toast, recarga tanto la lista como el resumen, y actualiza la vista de detalle.
   * @async
   * @returns {Promise<void>}
   */
  async function confirmarCancelar() {
    if (!cancelMotivo.trim()) { cancelError = 'Escribe un motivo de cancelacion.'; return; }
    cancelLoading = true; cancelError = '';
    try {
      const r = await fetch(`${API}/api/mis-reservaciones/${reservaDetalle.reservacionId}/cancelar`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo: cancelMotivo.trim() })
      });
      if (r.ok) {
        addToast('Reservacion cancelada exitosamente');
        cancelarAbierto = false;
        await Promise.all([cargarReservas(), cargarResumen()]);
        const r2 = await fetch(`${API}/api/mis-reservaciones/${reservaDetalle.reservacionId}`, { credentials:'include' });
        if (r2.ok) reservaDetalle = await r2.json();
      } else {
        const body = await r.json().catch(() => ({}));
        cancelError = body.message || 'No se pudo cancelar la reservacion.';
      }
    } catch { cancelError = 'Error de conexion.'; }
    finally { cancelLoading = false; }
  }

  /**
   * Descarga el comprobante PDF de una reservacion desde GET /api/mis-reservaciones/:id/comprobante.
   * Crea un elemento ancla temporal para disparar la descarga del navegador, luego revoca la URL del objeto.
   * Muestra un toast de exito o error segun el resultado.
   * @async
   * @param {number} reservaId - El ID de la reservacion cuyo comprobante debe descargarse.
   * @returns {Promise<void>}
   */
    async function descargarComprobante(reservaId) {
        comprobanteLoading = true;
        try {
          window.open(`${API}/api/mis-reservaciones/${reservaId}/comprobante`, '_blank');
          addToast('Comprobante abierto en nueva pestana');
        } catch {
          addToast('Error al abrir comprobante', 'error');
        } finally {
          comprobanteLoading = false;
        }
      }

  /**
   * Envia el comprobante PDF al correo registrado del usuario mediante
   * POST /api/mis-reservaciones/:id/enviar-comprobante. Muestra un toast con el resultado.
   * @async
   * @param {number} reservaId - El ID de la reservacion cuyo comprobante debe enviarse por correo.
   * @returns {Promise<void>}
   */
  async function enviarComprobantePorCorreo(reservaId) {
    enviarCorreoLoading = true;
    try {
      const r = await fetch(`${API}/api/mis-reservaciones/${reservaId}/enviar-comprobante`, {
        method: 'POST',
        credentials: 'include'
      });
      if (r.ok) {
        addToast('Comprobante enviado a tu correo');
      } else {
        const body = await r.json().catch(() => ({}));
        addToast(body.message || 'No se pudo enviar el comprobante', 'error');
      }
    } catch { addToast('Error de conexion', 'error'); }
    finally { enviarCorreoLoading = false; }
  }

  /**
   * Restablece todos los campos del formulario de comentario a su estado inicial vacio o cero.
   */
  function resetComentarForm() {
    comentarEstrellas = 0;
    comentarHover = 0;
    comentarContenido = '';
    comentarError = '';
    comentarExito = '';
  }

  /**
   * Envia un nuevo comentario de calificacion de ruta mediante POST /api/comentarios/ruta. Valida
   * que al menos una estrella este seleccionada y que el texto del comentario no este vacio antes
   * de enviar. En caso de exito, muestra un mensaje de exito, un toast, y recarga misComentarios
   * para que la calificacion se refleje inmediatamente sin reabrir el modal.
   * @async
   * @param {number} rutaId - El ID de la ruta que se esta calificando.
   * @returns {Promise<void>}
   */
  async function enviarComentario(rutaId) {
    if (comentarEstrellas < 1) { comentarError = 'Selecciona al menos 1 estrella.'; return; }
    if (!comentarContenido.trim()) { comentarError = 'Escribe tu comentario.'; return; }
    comentarLoading = true; comentarError = '';
    try {
      const r = await fetch(`${API}/api/comentarios/ruta`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          rutaId,
          cantidadEstrellas: comentarEstrellas,
          contenido: comentarContenido.trim()
        })
      });
      if (r.ok) {
        comentarExito = 'Comentario enviado exitosamente.';
        addToast('Comentario enviado');
        await cargarMisComentarios();
      } else {
        const body = await r.json().catch(() => ({}));
        comentarError = body.message || 'No se pudo enviar el comentario.';
      }
    } catch { comentarError = 'Error de conexion.'; }
    finally { comentarLoading = false; }
  }

  /**
   * Arreglo de configuracion de filtros que define la etiqueta y la clave de filtro de cada pestana.
   * La clave se compara contra el campo estadoReserva (en minusculas) de cada reservacion.
   * @type {Array<{key: string, label: string}>}
   */
  const filtros = [
    { key: 'todas',      label: 'Todas' },
    { key: 'confirmada', label: 'Confirmadas' },
    { key: 'cancelada',  label: 'Canceladas' },
    { key: 'expirada',   label: 'Expiradas' },
    { key: 'completada', label: 'Completadas' },
    { key: 'pendiente',  label: 'Pendientes' },
  ];

  // Subconjunto filtrado de reservas que coincide con la pestana de filtro de estado y el texto de busqueda.
  $: reservasFiltradas = (() => {
    let base = filtroActivo === 'todas'
      ? reservas
      : reservas.filter(r => r.estadoReserva?.toLowerCase() === filtroActivo);
    if (buscarTexto.trim().length >= 2) {
      const q = buscarTexto.trim().toLowerCase();
      base = base.filter(r =>
        r.noReservacion?.toLowerCase().includes(q) ||
        r.boletos?.some(b =>
          b.numeroVuelo?.toLowerCase().includes(q) ||
          b.codigoAvion?.toLowerCase().includes(q) ||
          b.destinoCiudad?.toLowerCase().includes(q) ||
          b.origenCiudad?.toLowerCase().includes(q) ||
          b.nombrePasajero?.toLowerCase().includes(q)
        )
      );
    }
    return base;
  })();
</script>

<!-- Contenedor de notificaciones toast apiladas en pantalla -->
<div class="mr-toast-container">
  {#each toasts as t (t.id)}
    <div class="mr-toast mr-toast--{t.tipo}">
      {#if t.tipo === 'success'}
        <svg class="mr-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
      {:else}
        <svg class="mr-toast__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      {/if}
      <span>{t.msg}</span>
    </div>
  {/each}
</div>

<!-- Modal de detalle de reservacion con boletos, calificacion y acciones -->
{#if reservaDetalle}
  <div class="mr-overlay" on:click={cerrarDetalle} role="dialog" aria-modal="true">
    <div class="mr-detail-modal" on:click|stopPropagation>

      <button class="mr-modal__close-btn" on:click={cerrarDetalle}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>

      <!-- Estado de carga o error del detalle de la reservacion -->
      {#if detalleLoading}
        <div class="mr-detail-modal__center">
          <div class="mr-spinner"></div>
          <p>Cargando detalle...</p>
        </div>
      {:else if detalleError}
        <div class="mr-detail-modal__center">
          <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="40" height="40"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          <p>{detalleError}</p>
        </div>
      {:else}

        <!-- Cabecera del modal con codigo, estado y total de la reservacion -->
        <div class="mr-detail__top">
          <div class="mr-detail__top-left">
            <span class="mr-detail__noreserva">{reservaDetalle.noReservacion}</span>
            <span class="mr-badge {estadoClase(reservaDetalle.estadoReserva)}">{reservaDetalle.estadoReserva}</span>
          </div>
          <span class="mr-detail__total">${reservaDetalle.total?.toFixed(2)}</span>
        </div>

        <!-- Fila de metadatos de la reservacion con fechas, cancelacion y usuario -->
        <div class="mr-detail__info-row">
          <div class="mr-detail__info-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <span>Creada: {formatFechaHora(reservaDetalle.fechaCreacion)}</span>
          </div>
          {#if reservaDetalle.fechaExpiracion}
            <div class="mr-detail__info-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              <span>Expira: {formatFechaHora(reservaDetalle.fechaExpiracion)}</span>
            </div>
          {/if}
          {#if reservaDetalle.fechaCancelacion}
            <div class="mr-detail__info-item mr-detail__info-item--cancel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
              <span>Cancelada: {formatFechaHora(reservaDetalle.fechaCancelacion)}</span>
            </div>
          {/if}
          {#if reservaDetalle.motivoCancelacion}
            <div class="mr-detail__info-item mr-detail__info-item--cancel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              <span>Motivo: {reservaDetalle.motivoCancelacion}</span>
            </div>
          {/if}
          <div class="mr-detail__info-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <span>{reservaDetalle.usuarioNombre}</span>
          </div>
        </div>

        <div class="mr-detail__section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="2" width="18" height="18"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
          <span>Boletos ({reservaDetalle.boletos?.length ?? 0})</span>
        </div>

        <!-- Listado de boletos de la reservacion con ruta, asiento y datos del pasajero -->
        <div class="mr-detail__boletos">
          {#each reservaDetalle.boletos ?? [] as boleto}
            <div class="mr-boleto">
              <div class="mr-boleto__header">
                <div class="mr-boleto__flight-info">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span class="mr-boleto__vuelo-num">{boleto.numeroVuelo}</span>
                  <span class="mr-boleto__avion">{boleto.avionMarca} {boleto.avionModelo}</span>
                </div>
                <span class="mr-badge mr-badge--sm {estadoClase(boleto.estadoBoleto)}">{boleto.estadoBoleto}</span>
              </div>

              <div class="mr-boleto__ruta">
                <div class="mr-boleto__punto">
                  <span class="mr-boleto__code">{boleto.origenCodigo}</span>
                  <span class="mr-boleto__city">{boleto.origenCiudad}</span>
                  <span class="mr-boleto__time">{formatHora(boleto.horaSalida)}</span>
                </div>
                <div class="mr-boleto__line">
                  <div class="mr-boleto__line-track"></div>
                  <svg class="mr-boleto__plane-svg" viewBox="0 0 24 24" fill="#8B6B4A" stroke="none" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span class="mr-boleto__duration">{formatDuracion(boleto.duracionMinutos)}</span>
                </div>
                <div class="mr-boleto__punto mr-boleto__punto--right">
                  <span class="mr-boleto__code">{boleto.destinoCodigo}</span>
                  <span class="mr-boleto__city">{boleto.destinoCiudad}</span>
                  <span class="mr-boleto__time">{formatHora(boleto.horaLlegada)}</span>
                </div>
              </div>

              <div class="mr-boleto__grid">
                <div class="mr-boleto__cell">
                  <span class="mr-boleto__cell-label">Asiento</span>
                  <span class="mr-boleto__cell-val">{boleto.noAsiento}</span>
                </div>
                <div class="mr-boleto__cell">
                  <span class="mr-boleto__cell-label">Clase</span>
                  <span class="mr-boleto__cell-val">{boleto.clase}</span>
                </div>
                <div class="mr-boleto__cell">
                  <span class="mr-boleto__cell-label">Fecha vuelo</span>
                  <span class="mr-boleto__cell-val">{formatFecha(boleto.fechaVuelo)}</span>
                </div>
                <div class="mr-boleto__cell">
                  <span class="mr-boleto__cell-label">Precio</span>
                  <span class="mr-boleto__cell-val mr-boleto__cell-val--price">${boleto.precio?.toFixed(2)}</span>
                </div>
                <div class="mr-boleto__cell mr-boleto__cell--wide">
                  <span class="mr-boleto__cell-label">No. Boleto</span>
                  <span class="mr-boleto__cell-val mr-boleto__cell-val--mono">{boleto.noBoleto}</span>
                </div>
              </div>

              {#if boleto.pasajero}
                <div class="mr-boleto__pasajero">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="2" width="16" height="16"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <div class="mr-boleto__pasajero-data">
                    <span class="mr-boleto__pasajero-name">{boleto.pasajero.nombre} {boleto.pasajero.apellido}</span>
                    <span class="mr-boleto__pasajero-info">Pasaporte: {boleto.pasajero.pasaporte} &middot; Tel: {boleto.pasajero.telefono} &middot; {boleto.pasajero.ciudad}, {boleto.pasajero.pais}</span>
                  </div>
                </div>
              {:else}
                <div class="mr-boleto__pasajero mr-boleto__pasajero--empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#aaa" stroke-width="2" width="16" height="16"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <span>Sin pasajero asignado</span>
                </div>
              {/if}
            </div>
          {/each}
        </div>

        <!-- Seccion de calificacion de ruta disponible para reservaciones completadas -->
        {#if reservaDetalle.estadoReserva?.toLowerCase() === 'completada'}
          {@const primerBoleto = reservaDetalle.boletos?.[0]}
          {#if primerBoleto}
            {@const rutaId = primerBoleto.rutaId}
            {@const yaComento = yaComentaRuta(rutaId)}

            <div class="mr-detail__section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="2" width="18" height="18"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              <span>Calificar ruta {primerBoleto.origenCodigo} - {primerBoleto.destinoCodigo}</span>
            </div>

            {#if yaComento}
              {@const miComentario = obtenerComentarioRuta(rutaId)}
              <div class="mr-ya-comento">
                <div class="mr-ya-comento__top">
                  <div class="mr-ya-comento__stars">
                    {#each [1,2,3,4,5] as n}
                      <svg viewBox="0 0 24 24" fill={n <= (miComentario?.cantidadEstrellas ?? 0) ? '#D4A056' : 'none'} stroke={n <= (miComentario?.cantidadEstrellas ?? 0) ? '#D4A056' : '#D8D1C5'} stroke-width="2" width="20" height="20"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    {/each}
                    <span class="mr-ya-comento__rating">{miComentario?.cantidadEstrellas}/5</span>
                  </div>
                  <span class="mr-ya-comento__date">{formatFechaHora(miComentario?.fecha)}</span>
                </div>
                <p class="mr-ya-comento__text">{miComentario?.contenido}</p>
                <div class="mr-ya-comento__badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#27ae60" stroke-width="2" width="14" height="14"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                  <span>Ya dejaste tu calificacion en esta ruta</span>
                </div>
              </div>
            {:else}
              <!-- Formulario de estrellas y comentario para calificar la ruta -->
              <div class="mr-comentar-form">
                <div class="mr-stars">
                  {#each [1,2,3,4,5] as n}
                    <button class="mr-star" class:mr-star--active={n <= (comentarHover || comentarEstrellas)}
                      on:mouseenter={() => comentarHover = n}
                      on:mouseleave={() => comentarHover = 0}
                      on:click={() => comentarEstrellas = n}
                      type="button">
                      <svg viewBox="0 0 24 24" fill={n <= (comentarHover || comentarEstrellas) ? '#D4A056' : 'none'} stroke={n <= (comentarHover || comentarEstrellas) ? '#D4A056' : '#B89A7A'} stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                    </button>
                  {/each}
                  <span class="mr-stars__label">{comentarEstrellas > 0 ? `${comentarEstrellas}/5` : 'Selecciona'}</span>
                </div>
                <textarea class="mr-comentar-form__textarea" bind:value={comentarContenido} placeholder="Cuenta tu experiencia en este vuelo..." rows="3"></textarea>
                {#if comentarError}<p class="mr-form-error">{comentarError}</p>{/if}
                {#if comentarExito}<p class="mr-form-exito">{comentarExito}</p>{/if}
                <button class="mr-btn mr-btn--primary" on:click={() => enviarComentario(rutaId)} disabled={comentarLoading} type="button">
                  {#if comentarLoading}
                    <span class="mr-btn__spinner"></span> Enviando...
                  {:else}
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    Enviar calificacion
                  {/if}
                </button>
              </div>
            {/if}
          {/if}
        {/if}

        <!-- Formulario de cancelacion disponible para reservaciones confirmadas -->
        {#if reservaDetalle.estadoReserva?.toLowerCase() === 'confirmada'}
          {#if !cancelarAbierto}
            <div class="mr-detail__cancel-trigger">
              <button class="mr-btn mr-btn--danger-outline" on:click={toggleCancelar} type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                Cancelar esta reservacion
              </button>
            </div>
          {:else}
            <div class="mr-cancel-section">
              <div class="mr-cancel-section__header">
                <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="22" height="22"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                <div>
                  <h4 class="mr-cancel-section__title">Cancelar reservacion</h4>
                  <p class="mr-cancel-section__sub">Esta accion no se puede deshacer. Todos los boletos seran cancelados.</p>
                </div>
              </div>
              <label class="mr-cancel-section__label">Motivo de cancelacion</label>
              <textarea class="mr-cancel-section__textarea" bind:value={cancelMotivo} placeholder="Ej: Cambio de planes, enfermedad..." rows="3"></textarea>
              {#if cancelError}<p class="mr-form-error">{cancelError}</p>{/if}
              <div class="mr-cancel-section__actions">
                <button class="mr-btn mr-btn--ghost" on:click={toggleCancelar} disabled={cancelLoading} type="button">Volver</button>
                <button class="mr-btn mr-btn--danger" on:click={confirmarCancelar} disabled={cancelLoading} type="button">
                  {#if cancelLoading}
                    <span class="mr-btn__spinner"></span> Cancelando...
                  {:else}
                    Confirmar cancelacion
                  {/if}
                </button>
              </div>
            </div>
          {/if}
        {/if}

        <!-- Acciones del pie del modal para cerrar, descargar o enviar comprobante -->
        <div class="mr-detail__footer-actions">
          <button class="mr-btn mr-btn--ghost" on:click={cerrarDetalle} type="button">Cerrar</button>
          {#if ['confirmada','completada','cancelada'].includes(reservaDetalle.estadoReserva?.toLowerCase())}
            <button class="mr-btn mr-btn--outline" on:click={() => descargarComprobante(reservaDetalle.reservacionId)} disabled={comprobanteLoading} type="button">
              {#if comprobanteLoading}
                <span class="mr-btn__spinner mr-btn__spinner--dark"></span> Descargando...
              {:else}
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                Descargar comprobante
              {/if}
            </button>
            <button class="mr-btn mr-btn--secondary" on:click={() => enviarComprobantePorCorreo(reservaDetalle.reservacionId)} disabled={enviarCorreoLoading} type="button">
              {#if enviarCorreoLoading}
                <span class="mr-btn__spinner mr-btn__spinner--dark"></span> Enviando...
              {:else}
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                Enviar al correo
              {/if}
            </button>
          {/if}
        </div>

      {/if}
    </div>
  </div>
{/if}

<!-- Pagina principal de listado de reservaciones con encabezado, resumen y filtros -->
<div class="mr-page">
  <div class="mr-container">

    <!-- Encabezado de pagina con titulo y boton de regreso al inicio -->
    <div class="mr-header">
      <div>
        <h1 class="mr-title">Mis Reservaciones</h1>
        <p class="mr-subtitle">Gestiona y consulta tu historial de vuelos</p>
      </div>
      <button class="mr-btn mr-btn--back" on:click={() => navigateTo('home')} type="button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Inicio
      </button>
    </div>

    <!-- Widget de resumen con totales por estado y monto gastado -->
    {#if resumen}
      <div class="mr-resumen">
        <div class="mr-resumen__card">
          <svg viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="2" width="22" height="22"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          <span class="mr-resumen__val">{resumen.totalReservaciones}</span>
          <span class="mr-resumen__label">Total</span>
        </div>
        <div class="mr-resumen__card mr-resumen__card--confirmada">
          <svg viewBox="0 0 24 24" fill="none" stroke="#27ae60" stroke-width="2" width="22" height="22"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          <span class="mr-resumen__val">{resumen.confirmadas}</span>
          <span class="mr-resumen__label">Confirmadas</span>
        </div>
        <div class="mr-resumen__card mr-resumen__card--pendiente">
          <svg viewBox="0 0 24 24" fill="none" stroke="#e67e22" stroke-width="2" width="22" height="22"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          <span class="mr-resumen__val">{resumen.pendientes}</span>
          <span class="mr-resumen__label">Pendientes</span>
        </div>
        <div class="mr-resumen__card mr-resumen__card--completada">
          <svg viewBox="0 0 24 24" fill="none" stroke="#2980b9" stroke-width="2" width="22" height="22"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>
          <span class="mr-resumen__val">{resumen.completadas}</span>
          <span class="mr-resumen__label">Completadas</span>
        </div>
        <div class="mr-resumen__card mr-resumen__card--cancelada">
          <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="22" height="22"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          <span class="mr-resumen__val">{resumen.canceladas}</span>
          <span class="mr-resumen__label">Canceladas</span>
        </div>
        <div class="mr-resumen__card mr-resumen__card--gasto">
          <svg viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="2" width="22" height="22"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          <span class="mr-resumen__val">${resumen.totalGastado?.toFixed(2)}</span>
          <span class="mr-resumen__label">Gastado</span>
        </div>
      </div>
    {/if}

    <!-- Campo de busqueda por codigo de reservacion, vuelo, destino o pasajero -->
    <div class="mr-buscar">
      <svg class="mr-buscar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
      </svg>
      <input
        type="text"
        class="mr-buscar__input"
        bind:value={buscarTexto}
        placeholder="Buscar por codigo reserva, vuelo, avion, destino..."
        autocomplete="off"
      />
      {#if buscarTexto}
        <button type="button" class="mr-buscar__clear" on:click={() => buscarTexto = ''} aria-label="Limpiar busqueda">✕</button>
      {/if}
    </div>

    <!-- Barra de filtros por estado de reservacion con contadores -->
    <div class="mr-filtros">
      {#each filtros as f}
        <button class="mr-filtro" class:mr-filtro--active={filtroActivo === f.key} on:click={() => filtroActivo = f.key} type="button">
          {f.label}
          {#if f.key !== 'todas' && resumen}
            <span class="mr-filtro__count">
              {f.key === 'confirmada' ? resumen.confirmadas :
               f.key === 'cancelada' ? resumen.canceladas :
               f.key === 'expirada' ? resumen.expiradas :
               f.key === 'completada' ? resumen.completadas :
               f.key === 'pendiente' ? resumen.pendientes : ''}
            </span>
          {/if}
        </button>
      {/each}
    </div>

    <!-- Grid de tarjetas de reservaciones o estado vacio segun el filtro activo -->
    {#if loading}
      <div class="mr-empty-state">
        <div class="mr-spinner mr-spinner--lg"></div>
        <p class="mr-empty-state__text">Cargando tus reservaciones...</p>
      </div>
    {:else if error}
      <div class="mr-empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="#c0392b" stroke-width="2" width="48" height="48"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        <p class="mr-empty-state__text">{error}</p>
        <button class="mr-btn mr-btn--primary" on:click={cargarReservas} type="button">Reintentar</button>
      </div>
    {:else if reservasFiltradas.length === 0}
      <div class="mr-empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="#B89A7A" stroke-width="1.5" width="56" height="56"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
        {#if filtroActivo === 'todas'}
          <p class="mr-empty-state__title">No tienes reservaciones aun</p>
          <p class="mr-empty-state__text">Busca un vuelo y haz tu primera reserva</p>
          <button class="mr-btn mr-btn--primary" on:click={() => navigateTo('home')} type="button">Buscar vuelos</button>
        {:else}
          <p class="mr-empty-state__title">Sin resultados</p>
          <p class="mr-empty-state__text">No hay reservaciones con estado "{filtroActivo}"</p>
          <button class="mr-btn mr-btn--ghost" on:click={() => filtroActivo = 'todas'} type="button">Ver todas</button>
        {/if}
      </div>
    {:else}
      <div class="mr-grid">
        {#each reservasFiltradas as reserva (reserva.reservacionId)}
          {@const pb = reserva.boletos?.[0]}
          <article class="mr-card" on:click={() => abrirDetalle(reserva)} role="button" tabindex="0" on:keydown={e => e.key === 'Enter' && abrirDetalle(reserva)}>
            <div class="mr-card__top">
              <span class="mr-card__noreserva">{reserva.noReservacion}</span>
              <span class="mr-badge {estadoClase(reserva.estadoReserva)}">{reserva.estadoReserva}</span>
            </div>
            {#if pb}
              <div class="mr-card__ruta">
                <div class="mr-card__punto">
                  <span class="mr-card__code">{pb.origenCodigo}</span>
                  <span class="mr-card__city">{pb.origenCiudad}</span>
                </div>
                <div class="mr-card__line">
                  <div class="mr-card__line-track"></div>
                  <svg class="mr-card__plane-icon" viewBox="0 0 24 24" fill="#8B6B4A" stroke="none" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                </div>
                <div class="mr-card__punto mr-card__punto--right">
                  <span class="mr-card__code">{pb.destinoCodigo}</span>
                  <span class="mr-card__city">{pb.destinoCiudad}</span>
                </div>
              </div>
              <div class="mr-card__meta">
                <span class="mr-card__meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  {pb.numeroVuelo}
                </span>
                <span class="mr-card__meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {formatFecha(pb.fechaVuelo)}
                </span>
                <span class="mr-card__meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  {formatHora(pb.horaSalida)} - {formatHora(pb.horaLlegada)}
                </span>
              </div>
            {/if}
            <div class="mr-card__bottom">
              <span class="mr-card__boletos">{reserva.boletos?.length ?? 0} boleto{(reserva.boletos?.length ?? 0) !== 1 ? 's' : ''}</span>
              <span class="mr-card__total">${reserva.total?.toFixed(2)}</span>
            </div>
            <div class="mr-card__cta">
              Ver detalle
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="9 18 15 12 9 6"/></svg>
            </div>
          </article>
        {/each}
      </div>
    {/if}

  </div>
</div>
