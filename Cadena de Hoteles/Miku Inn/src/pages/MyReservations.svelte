<script>
  /**
   * @file MyReservations.svelte
   * @description Pagina de mis reservaciones con cambio de fechas ATOMICO.
   * Todos los cambios de fechas de una reservacion se acumulan y se envian
   * juntos en una sola peticion. Si alguno falla, ningun detalle se persiste.
   */

  import '../styles/myreservations.css';

  export let navigateTo = (/** @type {string} */ _page, /** @type {any} */ _data = null) => {};

  import { API } from '../lib/api.js';

  let reservations        = [];
  let loading             = true;
  let loadError           = '';
  let selectedReservation = null;
  let hotelesConResena    = new Set();
  let comentario          = { resena: 5, contenido: '' };
  let comentarioSaving    = false;
  let comentarioError     = '';
  let comentarioOk        = false;
  let downloadingId       = null;
  let downloadError       = '';

  // ---------------------------------------------------------------------------
  // Estado del cambio de fechas ATOMICO
  // pendingChanges: Map<detalleId, { fechaCheckIn, fechaCheckOut }>
  // Solo se activa cuando hay al menos una habitacion con fechas modificadas.
  // ---------------------------------------------------------------------------

  /** Indica si el panel de edicion atomica esta abierto. @type {boolean} */
  let atomicEditOpen    = false;

  /**
   * Mapa de cambios pendientes: detalleId -> { newCheckIn, newCheckOut }.
   * Se carga con las fechas actuales al abrir el panel.
   * @type {Map<number, { newCheckIn: string, newCheckOut: string, original: { checkIn: string, checkOut: string } }>}
   */
  let pendingChanges    = new Map();

  /**
   * Fecha de check-in global que se aplica a TODAS las habitaciones.
   * El usuario ingresa una sola fecha y se replica en todos los detalles.
   * @type {string}
   */
  let globalCheckIn  = '';

  /**
   * Fecha de check-out global que se aplica a TODAS las habitaciones.
   * @type {string}
   */
  let globalCheckOut = '';

  /** Indica si la peticion atomica esta en vuelo. @type {boolean} */
  let atomicSaving      = false;

  /** Mensaje de error del flujo atomico. @type {string} */
  let atomicError       = '';

  /** Indica si el guardado atomico fue exitoso. @type {boolean} */
  let atomicOk          = false;

  /**
   * Abre el panel de edicion atomica para la reservacion seleccionada.
   * Inicializa una sola fecha global tomada de la primera habitacion.
   */
  function openAtomicEdit() {
    if (!selectedReservation) return;
    const primera = selectedReservation.habitaciones[0];
    // Un solo par de fechas global que aplica a todas las habitaciones
    pendingChanges = new Map(
      selectedReservation.habitaciones.map(h => [
        h.detalleId,
        {
          newCheckIn:  h.fechaCheckIn  ? h.fechaCheckIn.toString().split(' ')[0]  : '',
          newCheckOut: h.fechaCheckOut ? h.fechaCheckOut.toString().split(' ')[0] : '',
          original: {
            checkIn:  h.fechaCheckIn  ? h.fechaCheckIn.toString().split(' ')[0]  : '',
            checkOut: h.fechaCheckOut ? h.fechaCheckOut.toString().split(' ')[0] : '',
          }
        }
      ])
    );
    // Fecha global = fechas de la primera habitación
    globalCheckIn  = primera?.fechaCheckIn  ? primera.fechaCheckIn.toString().split(' ')[0]  : '';
    globalCheckOut = primera?.fechaCheckOut ? primera.fechaCheckOut.toString().split(' ')[0] : '';
    atomicEditOpen = true;
    atomicError    = '';
    atomicOk       = false;
    atomicSaving   = false;
  }

  /** Cierra y limpia el panel de edicion atomica. */
  function closeAtomicEdit() {
    atomicEditOpen = false;
    pendingChanges = new Map();
    globalCheckIn  = '';
    globalCheckOut = '';
    atomicError    = '';
    atomicOk       = false;
  }

  /**
   * Validacion frontend antes de enviar la fecha global.
   * Retorna un mensaje de error o cadena vacia si todo esta bien.
   * @returns {string}
   */
  function validateChanges() {
    if (!globalCheckIn || !globalCheckOut) {
      return 'Debes ingresar el check-in y el check-out.';
    }

    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const ci = new Date(globalCheckIn);
    const co = new Date(globalCheckOut);

    if (ci < hoy) return 'El check-in no puede ser anterior a hoy.';
    if (co <= ci)  return 'El check-out debe ser posterior al check-in.';

    // Validar que la duración coincide con la original (todas las habitaciones
    // tienen la misma duración, así que basta con la primera)
    const primera = pendingChanges.values().next().value;
    if (primera) {
      const diasOriginales = Math.round(
        (new Date(primera.original.checkOut) - new Date(primera.original.checkIn)) / 86400000
      );
      const diasNuevos = Math.round((co - ci) / 86400000);
      if (diasNuevos !== diasOriginales) {
        return `La reservación es por ${diasOriginales} noche(s). ` +
               `Solo puedes mover las fechas, no cambiar la duración.`;
      }

      // Validar 48 horas de anticipación sobre el check-in ACTUAL
      const checkInActual  = new Date(primera.original.checkIn);
      const horasRestantes = (checkInActual - new Date()) / 36e5;
      if (horasRestantes <= 48) {
        return 'El check-in actual está a menos de 48 horas y no puede modificarse.';
      }
    }

    return '';
  }

  /**
   * Devuelve true si las fechas globales difieren de las originales.
   * @returns {boolean}
   */
  function hasChanges() {
    const primera = pendingChanges.values().next().value;
    if (!primera) return false;
    return globalCheckIn  !== primera.original.checkIn ||
           globalCheckOut !== primera.original.checkOut;
  }

  /**
   * Envia la fecha global a todas las habitaciones en una sola peticion PATCH atomica.
   * El backend valida y persiste todos o ninguno.
   * @async
   */
  async function submitAtomicChanges() {
    if (!selectedReservation) return;

    const validationError = validateChanges();
    if (validationError) { atomicError = validationError; return; }

    if (!hasChanges()) { atomicError = 'No has modificado ninguna fecha.'; return; }

    // Aplicar la misma fecha global a TODAS las habitaciones
    const cambios = [];
    for (const [detalleId] of pendingChanges) {
      cambios.push({
        detalleId,
        fechaCheckIn:  globalCheckIn,
        fechaCheckOut: globalCheckOut,
      });
    }

    atomicSaving = true;
    atomicError  = '';

    try {
      const res = await fetch(
        `${API}/reservaciones/${selectedReservation.id}/fechas`,
        {
          method:      'PATCH',
          credentials: 'include',
          headers:     { 'Content-Type': 'application/json' },
          body:        JSON.stringify({ cambios }),
        }
      );
      const data = await res.json();
      if (!res.ok) {
        atomicError = data.mensaje || data.message || `Error ${res.status}`;
      } else {
        atomicOk = true;
        await fetchAll();
        selectedReservation = reservations.find(r => r.id === selectedReservation.id) ?? null;
        setTimeout(closeAtomicEdit, 2000);
      }
    } catch (e) {
      atomicError = /** @type {any} */ (e).message || 'Error de conexión al cambiar las fechas';
    } finally {
      atomicSaving = false;
    }
  }

  // ---------------------------------------------------------------------------
  // Resto de la logica (sin cambios respecto al original)
  // ---------------------------------------------------------------------------

  async function downloadFactura(reservacionId) {
    downloadingId = reservacionId;
    downloadError = '';
    try {
      const res = await fetch(`${API}/reservaciones/${reservacionId}/pdf`, {
        credentials: 'include',
      });
      if (!res.ok) {
        let msg = `Error ${res.status}`;
        try { const d = await res.json(); msg = d.mensaje || d.message || msg; } catch(_) {}
        throw new Error(msg);
      }
      const blob = await res.blob();
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `factura-${reservacionId}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch(e) {
      downloadError = /** @type {any} */ (e).message || 'Error al descargar la factura';
    } finally {
      downloadingId = null;
    }
  }

  function groupReservations(rows) {
    const map = new Map();
    for (const row of rows) {
      if (!map.has(row.id)) {
        map.set(row.id, {
          id:                row.id,
          noReservacion:     row.noReservacion,
          total:             row.total,
          estado:            row.estado,
          fechaCreacion:     row.fechaCreacion,
          fechaExpiracion:   row.fechaExpiracion,
          fechaCancelacion:  row.fechaCancelacion,
          motivoCancelacion: row.motivoCancelacion,
          nombreHotel:       row.nombreHotel,
          hotelId:           row.hotelId,
          habitaciones:      [],
        });
      }
      map.get(row.id).habitaciones.push({
        detalleId:             row.detalleId,
        habitacionId:          row.habitacionId,
        numeroHabitacion:      row.numeroHabitacion,
        hotelId:               row.hotelId,
        fechaCheckIn:          row.fechaCheckIn,
        fechaCheckOut:         row.fechaCheckOut,
        cantidadPersonas:      row.cantidadPersonas,
        totalDetalle:          row.totalDetalle,
        descripcionHabitacion: row.descripcionHabitacion,
        tipoHabitacion:        row.tipoHabitacion,
        tipoCama:              row.tipoCama,
      });
    }
    return Array.from(map.values());
  }

  async function fetchAll() {
    loading = true; loadError = '';
    try {
      const resRes = await fetch(`${API}/reservaciones`, { credentials: 'include' });
      if (!resRes.ok) {
        let msg = `Error ${resRes.status}`;
        try { const d = await resRes.json(); msg = d.mensaje || d.message || msg; } catch(_) {}
        throw new Error(msg);
      }
      const rawRes = await resRes.json();
      reservations = groupReservations(rawRes);

      try {
        const comRes = await fetch(`${API}/comentarios/usuario`, { credentials: 'include' });
        if (comRes.ok) {
          const comentarios = await comRes.json();
          hotelesConResena = new Set(
            comentarios
              .filter((/** @type {any} */ c) => c.resena !== null && c.resena !== undefined)
              .map((/** @type {any} */ c) => c.hotelId)
          );
        }
      } catch(_) {}
    } catch(e) {
      loadError = /** @type {any} */ (e).message || 'No se pudieron cargar las reservaciones';
    } finally {
      loading = false;
    }
  }

  fetchAll();

  function getStatus(estado) {
    const e = (estado || '').toLowerCase();
    const checkSvg = `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="display:inline;vertical-align:middle"><polyline points="20 6 9 17 4 12"/></svg>`;
    const xSvg     = `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="display:inline;vertical-align:middle"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`;
    const clockSvg = `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display:inline;vertical-align:middle"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`;
    const hourSvg  = `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display:inline;vertical-align:middle"><path d="M5 22h14M5 2h14"/><path d="M17 22v-4l-5-6-5 6v4"/><path d="M7 2v4l5 6 5-6V2"/></svg>`;
    if (e === 'confirmada') return { text: 'Confirmada', cls: 'confirmed', icon: checkSvg };
    if (e === 'completada') return { text: 'Completada', cls: 'completed', icon: checkSvg };
    if (e === 'cancelada')  return { text: 'Cancelada',  cls: 'cancelled', icon: xSvg     };
    if (e === 'pendiente')  return { text: 'Pendiente',  cls: 'pending',   icon: clockSvg };
    if (e === 'expirada')   return { text: 'Expirada',   cls: 'expirada',  icon: hourSvg  };
    return { text: estado, cls: 'pending', icon: clockSvg };
  }

  function calcNights(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    return Math.max(0, Math.ceil(
      (Number(new Date(checkOut)) - Number(new Date(checkIn))) / 86400000
    ));
  }

  function fmtDate(str) {
    if (!str) return '—';
    return str.toString().split(' ')[0];
  }

  function fmt(p) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency', currency: 'USD', minimumFractionDigits: 0
    }).format(p);
  }

  const FILTERS = [
    ['all','Todas'],['pendiente','Pendientes'],['confirmada','Confirmadas'],
    ['completada','Completadas'],['cancelada','Canceladas'],['expirada','Expiradas']
  ];

  let filter = 'all';
  let search = '';

  $: filtered = reservations.filter(r => {
    const estado = (r.estado || '').toLowerCase();
    const matchFilter = filter === 'all' || estado === filter;
    const matchSearch = !search ||
      (r.noReservacion || '').toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  let cancelingId  = null;
  let cancelMotivo = '';
  let cancelError  = '';
  let cancelSaving = false;

  function openCancelDialog(id) {
    cancelingId  = id;
    cancelMotivo = '';
    cancelError  = '';
    cancelSaving = false;
  }

  function closeCancelDialog() {
    cancelingId  = null;
    cancelMotivo = '';
    cancelError  = '';
  }

  async function confirmCancel() {
    if (!cancelMotivo.trim()) { cancelError = 'Escribe un motivo para cancelar.'; return; }
    cancelSaving = true; cancelError = '';
    try {
      const res = await fetch(`${API}/reservaciones/${cancelingId}/cancelar`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivoCancelacion: cancelMotivo.trim() }),
      });
      const data = await res.json();
      if (!res.ok) {
        cancelError = data.mensaje || data.message || `Error ${res.status}`;
      } else {
        closeCancelDialog();
        selectedReservation = null;
        await fetchAll();
      }
    } catch(e) {
      cancelError = /** @type {any} */ (e).message || 'Error al cancelar';
    } finally {
      cancelSaving = false;
    }
  }

  function openPanel(r) {
    selectedReservation = r;
    comentario       = { resena: 5, contenido: '' };
    comentarioError  = '';
    comentarioOk     = false;
    downloadError    = '';
    closeAtomicEdit();
  }

  async function submitComentario() {
    if (!selectedReservation) return;
    const hotelId = selectedReservation.hotelId
      ?? selectedReservation.habitaciones?.find((/** @type {any} */ h) => h.hotelId)?.hotelId;

    if (!hotelId) { comentarioError = 'No se encontró el hotel.'; return; }
    if (!comentario.contenido.trim()) {
      comentarioError = 'Escribe un comentario antes de enviar.'; return;
    }
    comentarioError  = '';
    comentarioSaving = true;
    try {
      const res = await fetch(`${API}/comentarios`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          hotelId,
          resena:    comentario.resena,
          contenido: comentario.contenido.trim(),
        }),
      });
      const data = await res.json();
      if (!res.ok) {
        comentarioError = data.mensaje || data.message || `Error ${res.status}`;
      } else {
        comentarioOk = true;
        hotelesConResena = new Set([...hotelesConResena, hotelId]);
      }
    } catch(e) {
      comentarioError = /** @type {any} */ (e).message || 'Error al enviar';
    } finally {
      comentarioSaving = false;
    }
  }
</script>

<!-- Cierra paneles con Escape -->
<svelte:window on:keydown={(e) => {
  if (e.key === 'Escape') {
    if (atomicEditOpen) { closeAtomicEdit(); return; }
    if (selectedReservation) { selectedReservation = null; }
  }
}} />

<div class="wrap">
  <div class="inner">

    <header class="hdr">
      <div>
        <h1>Mis Reservas</h1>
        <p class="sub">Gestiona todas tus reservaciones en un solo lugar</p>
      </div>
    </header>

    <div class="controls">
      <div class="filters">
        {#each FILTERS as [val, label]}
          <button class="fbtn" class:active={filter === val} on:click={() => filter = val}>{label}</button>
        {/each}
      </div>
      <div class="search">
        <span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </span>
        <input bind:value={search} placeholder="Buscar por código de reserva..." />
      </div>
    </div>

    {#if loading}
      <div class="empty">
        <div class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </div>
        <h2>Cargando reservaciones...</h2>
      </div>

    {:else if loadError}
      <div class="empty">
        <div class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        </div>
        <h2>Error al cargar</h2>
        <p>{loadError}</p>
        <button class="btn-retry" on:click={fetchAll}>Reintentar</button>
      </div>

    {:else}
      <div class="list">
        {#if filtered.length === 0}
          <div class="empty">
            <div class="empty-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#bbb" stroke-width="1.2"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/><line x1="12" y1="11" x2="12" y2="17"/><line x1="9" y1="14" x2="15" y2="14"/></svg>
            </div>
            <h2>{reservations.length === 0 ? 'No tienes reservaciones aún' : 'No se encontraron reservas'}</h2>
            <p>{reservations.length === 0 ? 'Realiza tu primera reserva' : 'Intenta ajustar los filtros'}</p>
          </div>
        {:else}
          {#each filtered as r}
            {@const s = getStatus(r.estado)}
            {@const hab = r.habitaciones?.[0]}
            {@const nights = calcNights(hab?.fechaCheckIn, hab?.fechaCheckOut)}
            <div class="card">
              <div class="img-wrap">
                <div class="img-placeholder">
                  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.55)" stroke-width="1.2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <span class="badge {s.cls}">{@html s.icon} {s.text}</span>
              </div>

              <div class="info">
                <div class="info-hdr">
                  <div>
                    <h3>{r.nombreHotel || 'Reservación'}</h3>
                    <p class="room">{r.habitaciones.map(h => h.tipoHabitacion).join(' · ')}</p>
                  </div>
                  <div class="code">
                    <small>Código de Reserva</small>
                    <strong>{r.noReservacion}</strong>
                  </div>
                </div>

                <div class="grid">
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>Check-in
                    </span>
                    <span class="val">{fmtDate(hab?.fechaCheckIn)}</span>
                  </div>
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>Check-out
                    </span>
                    <span class="val">{fmtDate(hab?.fechaCheckOut)}</span>
                  </div>
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>Noches
                    </span>
                    <span class="val">{nights}</span>
                  </div>
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>Huéspedes
                    </span>
                    <span class="val">{r.habitaciones.reduce((s,h) => s + (h.cantidadPersonas||0), 0)}</span>
                  </div>
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>Habitaciones
                    </span>
                    <span class="val">{r.habitaciones.length}</span>
                  </div>
                  <div class="cell">
                    <span class="lbl">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:3px"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>Total
                    </span>
                    <span class="val">{fmt(r.total)}</span>
                  </div>
                </div>

                {#if r.estado?.toLowerCase() === 'cancelada'}
                  <div class="cancel-note">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                    Cancelada el {fmtDate(r.fechaCancelacion)}
                  </div>
                {:else if r.fechaExpiracion && r.estado?.toLowerCase() !== 'expirada'}
                  <div class="expiry-note">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    Expira: {fmtDate(r.fechaExpiracion)}
                  </div>
                {/if}
              </div>

              <div class="actions">
                <button class="abtn primary" on:click={() => openPanel(r)}>
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                  Ver Detalles
                </button>
                <button
                  class="abtn download"
                  on:click={() => downloadFactura(r.id)}
                  disabled={downloadingId === r.id}
                >
                  {#if downloadingId === r.id}
                    <span class="comment-spinner" style="border-color:rgba(102,126,234,.3);border-top-color:#667eea;"></span>
                    Descargando...
                  {:else}
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                    Descargar Factura
                  {/if}
                </button>
                {#if r.estado?.toLowerCase() === 'pendiente' || r.estado?.toLowerCase() === 'confirmada'}
                  <button class="abtn danger" on:click={() => openCancelDialog(r.id)}>Cancelar Reserva</button>
                {/if}
              </div>
            </div>
          {/each}
        {/if}
      </div>
    {/if}

  </div>
</div>

<!-- ============================================================
     PANEL LATERAL - DETALLE DE RESERVACION
     ============================================================ -->
{#if selectedReservation}
  {@const sr = selectedReservation}
  {@const ss = getStatus(sr.estado)}
  {@const esCompletada = (sr.estado || '').toLowerCase() === 'completada'}
  {@const yaReseno = hotelesConResena.has(sr.hotelId)}
  {@const esModificable = sr.estado?.toLowerCase() === 'pendiente' || sr.estado?.toLowerCase() === 'confirmada'}

  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <!-- svelte-ignore a11y-no-static-element-interactions -->
  <div class="panel-overlay" on:click|self={() => { selectedReservation = null; closeAtomicEdit(); }}>
    <div class="panel" role="dialog" aria-modal="true" aria-label="Detalle de reservación">

      <div class="panel-header">
        <div class="panel-header-info">
          <div class="panel-hotel-name">{sr.nombreHotel}</div>
          <div class="panel-res-code">{sr.noReservacion}</div>
        </div>
        <div class="panel-header-right">
          <span class="panel-badge {ss.cls}">{@html ss.icon} {ss.text}</span>
          <button class="panel-close" on:click={() => { selectedReservation = null; closeAtomicEdit(); }} aria-label="Cerrar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="panel-body">

        <!-- Descargar factura -->
        <div class="panel-download-wrap">
          <button
            class="panel-download-btn"
            on:click={() => downloadFactura(sr.id)}
            disabled={downloadingId === sr.id}
          >
            {#if downloadingId === sr.id}
              <span class="comment-spinner"></span>
              Descargando factura...
            {:else}
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              Descargar Factura PDF
            {/if}
          </button>
          {#if downloadError}
            <div class="download-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {downloadError}
            </div>
          {/if}
        </div>

        <!-- Informacion general -->
        <div class="panel-section">
          <h4 class="panel-section-title">Información general</h4>
          <div class="panel-info-grid">
            <div class="panel-info-cell">
              <span class="panel-lbl">Reservado el</span>
              <span class="panel-val">{fmtDate(sr.fechaCreacion)}</span>
            </div>
            <div class="panel-info-cell">
              <span class="panel-lbl">Total reservación</span>
              <span class="panel-val panel-val-total">{fmt(sr.total)}</span>
            </div>
            {#if sr.fechaExpiracion}
              <div class="panel-info-cell">
                <span class="panel-lbl">Expiración</span>
                <span class="panel-val">{fmtDate(sr.fechaExpiracion)}</span>
              </div>
            {/if}
            {#if sr.fechaCancelacion}
              <div class="panel-info-cell">
                <span class="panel-lbl">Cancelada el</span>
                <span class="panel-val">{fmtDate(sr.fechaCancelacion)}</span>
              </div>
            {/if}
          </div>
        </div>

        <!-- Habitaciones reservadas -->
        <div class="panel-section">
          <h4 class="panel-section-title">
            Habitaciones reservadas
            <span class="panel-section-count">{sr.habitaciones.length}</span>
          </h4>

          {#each sr.habitaciones as h}
            <div class="panel-room">
              <div class="panel-room-top">
                <div class="panel-room-title-wrap">
                  <strong class="panel-room-name">{h.tipoHabitacion}</strong>
                  {#if h.numeroHabitacion}
                    <span class="panel-room-number">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                        <rect x="3" y="3" width="18" height="18" rx="2"/>
                        <path d="M9 9h6M9 12h6M9 15h4"/>
                      </svg>
                      Hab. {h.numeroHabitacion}
                    </span>
                  {/if}
                  <span class="panel-room-bed">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                    {h.tipoCama}
                  </span>
                </div>
                <div class="panel-room-price">{fmt(h.totalDetalle)}</div>
              </div>
              <p class="panel-room-desc">{h.descripcionHabitacion}</p>
              <div class="panel-room-meta">
                <div class="panel-room-meta-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  <!-- Si hay cambio pendiente para esta habitacion, mostrar las fechas nuevas con indicador -->
                  {#if atomicEditOpen && pendingChanges.has(h.detalleId)}
                    {@const pc = pendingChanges.get(h.detalleId)}
                    {#if pc.newCheckIn !== pc.original.checkIn || pc.newCheckOut !== pc.original.checkOut}
                      <span class="dates-changed">
                        {pc.newCheckIn} → {pc.newCheckOut}
                        <span class="dates-changed-tag">nuevo</span>
                      </span>
                    {:else}
                      <span>{h.fechaCheckIn} → {h.fechaCheckOut}</span>
                    {/if}
                  {:else}
                    <span>{h.fechaCheckIn} → {h.fechaCheckOut}</span>
                  {/if}
                </div>
                <div class="panel-room-meta-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  <span>{calcNights(h.fechaCheckIn, h.fechaCheckOut)} noches</span>
                </div>
                <div class="panel-room-meta-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  <span>{h.cantidadPersonas} huésped{h.cantidadPersonas !== 1 ? 'es' : ''}</span>
                </div>
              </div>
            </div>
          {/each}

          <!-- -------------------------------------------------------
               BLOQUE DE CAMBIO DE FECHAS ATOMICO
               Se muestra debajo de todas las habitaciones, solo cuando
               la reservacion es pendiente o confirmada.
               ------------------------------------------------------- -->
          {#if esModificable}
            {#if !atomicEditOpen}
              <!-- Boton para abrir el editor atomico -->
              <button class="atomic-edit-btn" on:click={openAtomicEdit}>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                  <line x1="12" y1="14" x2="12" y2="18"/>
                  <line x1="10" y1="16" x2="14" y2="16"/>
                </svg>
                Cambiar fechas de todas las habitaciones
              </button>

            {:else}
              <!-- Panel de edicion atomica -->
              <div class="atomic-edit-panel">
                <div class="atomic-edit-header">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="4" width="18" height="18" rx="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/>
                    <line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                  <span>Cambio de fechas — todas o ninguna</span>
                  <span class="atomic-edit-info">
                    Si alguna fecha es inválida o no está disponible, ningún cambio se guardará.
                  </span>
                </div>

                {#if atomicOk}
                  <div class="atomic-ok">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                    Todas las fechas fueron actualizadas correctamente
                  </div>
                {:else}
                  <!-- Habitaciones afectadas (solo lectura) -->
                  {#each sr.habitaciones as h}
                    <div class="atomic-room-row" style="justify-content:space-between; align-items:center">
                      <div class="atomic-room-label">
                        <strong>{h.tipoHabitacion}</strong>
                        {#if h.numeroHabitacion}
                          <span class="atomic-room-num">#{h.numeroHabitacion}</span>
                        {/if}
                      </div>
                      <span style="font-size:.75rem; color:var(--mr-text-muted, #9ca3af)">
                        {h.fechaCheckIn?.toString().split(' ')[0] ?? '—'} → {h.fechaCheckOut?.toString().split(' ')[0] ?? '—'}
                      </span>
                    </div>
                  {/each}

                  <!-- Un solo par de fechas que aplica a TODAS las habitaciones -->
                  <div class="atomic-dates-fields" style="margin-top:.75rem; padding-top:.75rem; border-top:1px solid rgba(255,255,255,.07)">
                    <div class="atomic-date-field">
                      <label class="panel-lbl">Check-in</label>
                      <input
                        type="date"
                        class="change-dates-input"
                        bind:value={globalCheckIn}
                        min={new Date().toISOString().split('T')[0]}
                      />
                    </div>
                    <div class="atomic-date-field">
                      <label class="panel-lbl">Check-out</label>
                      <input
                        type="date"
                        class="change-dates-input"
                        bind:value={globalCheckOut}
                        min={globalCheckIn || new Date().toISOString().split('T')[0]}
                      />
                    </div>
                  </div>

                  {#if atomicError}
                    <div class="change-dates-error">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                      {atomicError}
                    </div>
                  {/if}

                  <div class="atomic-edit-actions">
                    <button class="change-dates-cancel-btn" on:click={closeAtomicEdit} disabled={atomicSaving}>
                      Cancelar
                    </button>
                    <button class="change-dates-submit-btn" on:click={submitAtomicChanges} disabled={atomicSaving}>
                      {#if atomicSaving}
                        <span class="comment-spinner" style="border-color:rgba(255,255,255,.3);border-top-color:white;"></span>
                        Guardando...
                      {:else}
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                        Confirmar todos los cambios
                      {/if}
                    </button>
                  </div>
                {/if}
              </div>
            {/if}
          {/if}
          <!-- fin bloque atomico -->
        </div>

        <!-- Desglose de costos -->
        <div class="panel-section panel-totals">
          <h4 class="panel-section-title">Desglose de costos</h4>
          {#each sr.habitaciones as h}
            <div class="panel-total-row">
              <span>
                {h.tipoHabitacion}
                {#if h.numeroHabitacion}
                  <span class="total-row-num">#{h.numeroHabitacion}</span>
                {/if}
              </span>
              <span>{fmt(h.totalDetalle)}</span>
            </div>
          {/each}
          <div class="panel-total-final">
            <span>Total</span>
            <strong>{fmt(sr.total)}</strong>
          </div>
        </div>

        <!-- Seccion de resena -->
        {#if esCompletada}
          <div class="panel-section panel-comment-section">
            <h4 class="panel-section-title">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              Dejar una reseña
            </h4>

            {#if yaReseno && !comentarioOk}
              <div class="comment-already">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                Ya dejaste una reseña para <strong>{sr.nombreHotel}</strong>. Solo se permite una reseña por hotel.
              </div>
            {:else if comentarioOk}
              <div class="comment-success">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                ¡Gracias por tu reseña! Tu comentario fue enviado correctamente.
              </div>
            {:else}
              <div class="stars-row">
                <span class="panel-lbl" style="margin-bottom:.5rem;display:block">Calificación</span>
                <div class="stars">
                  {#each [1,2,3,4,5] as n}
                    <button
                      class="star-btn"
                      class:active={comentario.resena >= n}
                      on:click={() => comentario.resena = n}
                      aria-label="Dar {n} estrellas"
                    >★</button>
                  {/each}
                  <span class="stars-label">{comentario.resena} / 5</span>
                </div>
              </div>

              <div class="comment-field">
                <label for="comentario-txt" class="panel-lbl" style="margin-bottom:.4rem;display:block">Tu comentario</label>
                <textarea
                  id="comentario-txt"
                  class="comment-textarea"
                  bind:value={comentario.contenido}
                  placeholder="Cuéntanos tu experiencia en {sr.nombreHotel}..."
                  rows="4"
                ></textarea>
              </div>

              {#if comentarioError}
                <div class="comment-error">{comentarioError}</div>
              {/if}

              <button class="comment-submit-btn" on:click={submitComentario} disabled={comentarioSaving}>
                {#if comentarioSaving}
                  <span class="comment-spinner"></span>
                  Enviando...
                {:else}
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                  Enviar reseña
                {/if}
              </button>
            {/if}
          </div>
        {/if}

        <!-- Boton cancelar reservacion -->
        {#if esModificable}
          <button class="panel-cancel-btn" on:click={() => openCancelDialog(sr.id)}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            Cancelar esta reservación
          </button>
        {/if}

      </div>
    </div>
  </div>
{/if}

<!-- ============================================================
     MODAL DE CONFIRMACION DE CANCELACION
     ============================================================ -->
{#if cancelingId !== null}
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <!-- svelte-ignore a11y-no-static-element-interactions -->
  <div class="cancel-overlay" on:click|self={closeCancelDialog}>
    <div class="cancel-modal" role="dialog" aria-modal="true">
      <div class="cancel-modal-header">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
        <h3>Cancelar reservación</h3>
      </div>

      <p class="cancel-modal-sub">Por favor indica el motivo de la cancelación.</p>

      <textarea
        class="cancel-motivo-input"
        bind:value={cancelMotivo}
        placeholder="Ej: Cambio de planes de viaje, emergencia personal..."
        rows="3"
        autofocus
      ></textarea>

      {#if cancelError}
        <div class="cancel-modal-error">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {cancelError}
        </div>
      {/if}

      <div class="cancel-modal-actions">
        <button class="cancel-modal-back" on:click={closeCancelDialog} disabled={cancelSaving}>
          Volver
        </button>
        <button class="cancel-modal-confirm" on:click={confirmCancel} disabled={cancelSaving}>
          {#if cancelSaving}
            <span class="comment-spinner"></span> Cancelando...
          {:else}
            Confirmar cancelación
          {/if}
        </button>
      </div>
    </div>
  </div>
{/if}
