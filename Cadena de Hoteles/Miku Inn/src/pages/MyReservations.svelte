<script>
  /**
   * @file MyReservations.svelte
   * @description Pagina de mis reservaciones. Muestra todas las reservas del usuario
   * autenticado, permite filtrarlas, ver el detalle en un panel lateral, descargar
   * la factura en PDF, cancelar una reserva activa y dejar una resena cuando la
   * estadia ya fue completada.
   */

  import '../styles/myreservations.css';

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo = (/** @type {string} */ _page, /** @type {any} */ _data = null) => {};

  /** URL base de la API del backend. @type {string} */
  const API = 'http://localhost:7000';

  /** Lista de reservaciones agrupadas del usuario. @type {any[]} */
  let reservations        = [];

  /** Indica si la carga inicial todavia esta en curso. @type {boolean} */
  let loading             = true;

  /** Mensaje de error si la carga falla. @type {string} */
  let loadError           = '';

  /** Reservacion seleccionada para el panel de detalle. @type {any} */
  let selectedReservation = null;

  /** IDs de hoteles en los que el usuario ya dejo una resena. @type {Set<number>} */
  let hotelesConResena = new Set();

  /** Datos del comentario/resena que el usuario esta por enviar. @type {{ resena: number, contenido: string }} */
  let comentario       = { resena: 5, contenido: '' };

  /** Indica si el comentario se esta enviando al servidor. @type {boolean} */
  let comentarioSaving = false;

  /** Mensaje de error al enviar el comentario. @type {string} */
  let comentarioError  = '';

  /** Indica si el comentario fue enviado con exito. @type {boolean} */
  let comentarioOk     = false;

  /** ID de la reservacion cuya factura se esta descargando en este momento. @type {any} */
  let downloadingId = null;

  /** Mensaje de error al intentar descargar la factura PDF. @type {string} */
  let downloadError = '';

  /**
   * Descarga la factura PDF de una reservacion y la abre como archivo.
   * @async
   * @param {number} reservacionId - ID de la reservacion a descargar.
   * @returns {Promise<void>}
   */
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

  /**
   * Agrupa las filas planas devueltas por la API en objetos de reservacion,
   * cada uno con un array de habitaciones anidado.
   * @param {any[]} rows - Filas crudas de la API.
   * @returns {any[]} Array de reservaciones agrupadas.
   */
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

  /**
   * Carga las reservaciones del usuario y los hoteles que ya tienen resena.
   * Se llama al montar el componente y al reintentar tras error.
   * @async
   * @returns {Promise<void>}
   */
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

  // Carga inicial al montar el componente
  fetchAll();

  /**
   * Devuelve la etiqueta visual y clase CSS correspondiente al estado de una reservacion.
   * @param {string|null|undefined} estado - Estado de la reservacion.
   * @returns {{ text: string|null|undefined, cls: string, icon: string }}
   */
  function getStatus(estado) {
    const e = (estado || '').toLowerCase();
    if (e === 'confirmada') return { text: 'Confirmada', cls: 'confirmed', icon: '✓' };
    if (e === 'completada') return { text: 'Completada', cls: 'completed', icon: '✓' };
    if (e === 'cancelada')  return { text: 'Cancelada',  cls: 'cancelled', icon: '✕' };
    if (e === 'pendiente')  return { text: 'Pendiente',  cls: 'pending',   icon: '⏳' };
    if (e === 'expirada')   return { text: 'Expirada',   cls: 'expirada',  icon: '⌛' };
    return { text: estado, cls: 'pending', icon: '⏳' };
  }

  /**
   * Calcula la cantidad de noches entre check-in y check-out.
   * @param {string} checkIn - Fecha de entrada.
   * @param {string} checkOut - Fecha de salida.
   * @returns {number} Numero de noches (minimo 0).
   */
  function calcNights(checkIn, checkOut) {
    if (!checkIn || !checkOut) return 0;
    return Math.max(0, Math.ceil(
      (Number(new Date(checkOut)) - Number(new Date(checkIn))) / 86400000
    ));
  }

  /**
   * Formatea una fecha ISO a solo la parte de fecha (YYYY-MM-DD).
   * @param {any} str - Cadena de fecha.
   * @returns {string}
   */
  function fmtDate(str) {
    if (!str) return '—';
    return str.toString().split(' ')[0];
  }

  /**
   * Formatea un numero como moneda USD.
   * @param {number} p - Valor a formatear.
   * @returns {string}
   */
  function fmt(p) {
    return new Intl.NumberFormat('es-GT', {
      style: 'currency', currency: 'USD', minimumFractionDigits: 0
    }).format(p);
  }

  /**
   * Pares [valor, etiqueta] para los botones de filtro de estado.
   * @type {[string, string][]}
   */
  const FILTERS = [
    ['all','Todas'],['pendiente','Pendientes'],['confirmada','Confirmadas'],
    ['completada','Completadas'],['cancelada','Canceladas'],['expirada','Expiradas']
  ];

  /** Filtro activo actualmente. @type {string} */
  let filter = 'all';

  /** Texto de busqueda por codigo de reserva. @type {string} */
  let search = '';

  // Lista de reservaciones que pasan el filtro y la busqueda activos.
  $: filtered = reservations.filter(r => {
    const estado = (r.estado || '').toLowerCase();
    const matchFilter = filter === 'all' || estado === filter;
    const matchSearch = !search ||
      (r.noReservacion || '').toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  /** ID de la reservacion que esta siendo cancelada. @type {any} */
  let cancelingId  = null;

  /** Motivo escrito por el usuario para la cancelacion. @type {string} */
  let cancelMotivo = '';

  /** Mensaje de error en el flujo de cancelacion. @type {string} */
  let cancelError  = '';

  /** Indica si la peticion de cancelacion esta en vuelo. @type {boolean} */
  let cancelSaving = false;

  /**
   * Abre el dialogo de cancelacion para la reservacion indicada.
   * @param {number} id - ID de la reservacion a cancelar.
   */
  function openCancelDialog(id) {
    cancelingId  = id;
    cancelMotivo = '';
    cancelError  = '';
    cancelSaving = false;
  }

  /**
   * Cierra el dialogo de cancelacion y limpia su estado.
   */
  function closeCancelDialog() {
    cancelingId = null;
    cancelMotivo = '';
    cancelError  = '';
  }

  /**
   * Envia la solicitud de cancelacion al servidor con el motivo ingresado.
   * @async
   * @returns {Promise<void>}
   */
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

  /**
   * Abre el panel lateral de detalle para una reservacion y reinicia
   * el formulario de comentario.
   * @param {any} r - Objeto de reservacion.
   */
  function openPanel(r) {
    selectedReservation = r;
    comentario       = { resena: 5, contenido: '' };
    comentarioError  = '';
    comentarioOk     = false;
    downloadError    = '';
  }

  /**
   * Envia la resena del usuario para el hotel de la reservacion seleccionada.
   * Solo disponible cuando la reservacion esta en estado "completada".
   * @async
   * @returns {Promise<void>}
   */
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

<!-- Cierra el panel con Escape -->
<svelte:window on:keydown={(e) => {
  if (e.key === 'Escape' && selectedReservation) selectedReservation = null;
}} />

<div class="wrap">
  <div class="inner">

    <!-- Encabezado principal de la pagina -->
    <header class="hdr">
      <div>
        <h1>Mis Reservas</h1>
        <p class="sub">Gestiona todas tus reservaciones en un solo lugar</p>
      </div>
    </header>

    <!-- Barra de filtros y busqueda por codigo -->
    <div class="controls">
      <div class="filters">
        {#each FILTERS as [val, label]}
          <button class="fbtn" class:active={filter === val} on:click={() => filter = val}>{label}</button>
        {/each}
      </div>
      <div class="search">
        <span>🔍</span>
        <input bind:value={search} placeholder="Buscar por código de reserva..." />
      </div>
    </div>

    <!-- Estado de carga -->
    {#if loading}
      <div class="empty">
        <div class="empty-icon">⏳</div>
        <h2>Cargando reservaciones...</h2>
      </div>

    <!-- Estado de error con boton de reintento -->
    {:else if loadError}
      <div class="empty">
        <div class="empty-icon">⚠️</div>
        <h2>Error al cargar</h2>
        <p>{loadError}</p>
        <button class="btn-retry" on:click={fetchAll}>Reintentar</button>
      </div>

    {:else}
      <!-- Lista de tarjetas de reservacion -->
      <div class="list">
        {#if filtered.length === 0}
          <div class="empty">
            <div class="empty-icon">📋</div>
            <h2>{reservations.length === 0 ? 'No tienes reservaciones aún' : 'No se encontraron reservas'}</h2>
            <p>{reservations.length === 0 ? 'Realiza tu primera reserva' : 'Intenta ajustar los filtros'}</p>
          </div>
        {:else}
          {#each filtered as r}
            {@const s = getStatus(r.estado)}
            {@const hab = r.habitaciones?.[0]}
            {@const nights = calcNights(hab?.fechaCheckIn, hab?.fechaCheckOut)}
            <div class="card">

              <!-- Imagen placeholder con badge de estado -->
              <div class="img-wrap">
                <div class="img-placeholder">
                  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.55)" stroke-width="1.2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <span class="badge {s.cls}">{s.icon} {s.text}</span>
              </div>

              <!-- Informacion resumida de la reservacion -->
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

                <!-- Grid de datos rapidos: fechas, noches, huespedes, total -->
                <div class="grid">
                  <div class="cell"><span class="lbl">📅 Check-in</span>    <span class="val">{fmtDate(hab?.fechaCheckIn)}</span></div>
                  <div class="cell"><span class="lbl">📅 Check-out</span>   <span class="val">{fmtDate(hab?.fechaCheckOut)}</span></div>
                  <div class="cell"><span class="lbl">🌙 Noches</span>      <span class="val">{nights}</span></div>
                  <div class="cell"><span class="lbl">👥 Huéspedes</span>   <span class="val">{r.habitaciones.reduce((s,h) => s + (h.cantidadPersonas||0), 0)}</span></div>
                  <div class="cell"><span class="lbl">🛏 Habitaciones</span><span class="val">{r.habitaciones.length}</span></div>
                  <div class="cell"><span class="lbl">💰 Total</span>       <span class="val">{fmt(r.total)}</span></div>
                </div>

                {#if r.estado?.toLowerCase() === 'cancelada'}
                  <div class="cancel-note">⚠️ Cancelada el {fmtDate(r.fechaCancelacion)}</div>
                {:else if r.fechaExpiracion && r.estado?.toLowerCase() !== 'expirada'}
                  <div class="expiry-note">⏱ Expira: {fmtDate(r.fechaExpiracion)}</div>
                {/if}
              </div>

              <!-- Acciones de la tarjeta: detalle, PDF y cancelar -->
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

<!-- Panel lateral deslizante con el detalle completo de la reservacion -->
{#if selectedReservation}
  {@const sr = selectedReservation}
  {@const ss = getStatus(sr.estado)}
  {@const esCompletada = (sr.estado || '').toLowerCase() === 'completada'}
  {@const yaReseno = hotelesConResena.has(sr.hotelId)}
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <!-- svelte-ignore a11y-no-static-element-interactions -->
  <div class="panel-overlay" on:click|self={() => selectedReservation = null}>
    <div class="panel" role="dialog" aria-modal="true" aria-label="Detalle de reservación">

      <!-- Encabezado del panel con hotel, codigo y badge de estado -->
      <div class="panel-header">
        <div class="panel-header-info">
          <div class="panel-hotel-name">{sr.nombreHotel}</div>
          <div class="panel-res-code">{sr.noReservacion}</div>
        </div>
        <div class="panel-header-right">
          <span class="panel-badge {ss.cls}">{ss.icon} {ss.text}</span>
          <button class="panel-close" on:click={() => selectedReservation = null} aria-label="Cerrar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="panel-body">

        <!-- Boton para descargar la factura PDF desde el panel -->
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

        <!-- Informacion general de la reservacion -->
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

        <!-- Detalle de las habitaciones reservadas -->
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

                  <!-- Numero fisico de la habitacion -->
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
                  <span>{h.fechaCheckIn} → {h.fechaCheckOut}</span>
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
        </div>

        <!-- Desglose de costos por habitacion y total -->
        <div class="panel-section panel-totals">
          <h4 class="panel-section-title">Desglose de costos</h4>
          {#each sr.habitaciones as h}
            <div class="panel-total-row">
              <!-- Tipo de habitacion + numero para identificar la linea facilmente -->
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

        <!-- Seccion de resena: solo visible si la estadia fue completada -->
        {#if esCompletada}
          <div class="panel-section panel-comment-section">
            <h4 class="panel-section-title">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              Dejar una reseña
            </h4>

            {#if yaReseno && !comentarioOk}
              <!-- Aviso de resena ya existente para este hotel -->
              <div class="comment-already">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                Ya dejaste una reseña para <strong>{sr.nombreHotel}</strong>. Solo se permite una reseña por hotel.
              </div>

            {:else if comentarioOk}
              <!-- Confirmacion de envio exitoso -->
              <div class="comment-success">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                ¡Gracias por tu reseña! Tu comentario fue enviado correctamente.
              </div>

            {:else}
              <!-- Formulario de calificacion con estrellas y campo de texto -->
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

        <!-- Boton de cancelacion visible solo para reservas pendientes o confirmadas -->
        {#if sr.estado?.toLowerCase() === 'pendiente' || sr.estado?.toLowerCase() === 'confirmada'}
          <button class="panel-cancel-btn" on:click={() => openCancelDialog(sr.id)}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            Cancelar esta reservación
          </button>
        {/if}

      </div>
    </div>
  </div>
{/if}

<!-- Modal de confirmacion de cancelacion con campo de motivo -->
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
