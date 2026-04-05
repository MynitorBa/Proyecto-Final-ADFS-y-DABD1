<script>
  /**
   * @file AdminReservas.svelte
   * @description Modulo de administracion de reservaciones. Permite visualizar, filtrar
   * y cancelar reservas del sistema con un modal de confirmacion que incluye motivo opcional.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  export let API_BASE;

  /**
   * Funcion que retorna la clase CSS del badge segun el estado de la reserva.
   * @type {function(string): string}
   */
  export let badge;

  /** Contador total de reservas, expuesto al componente padre. @type {number} */
  export let count = 0;

  /** Lista completa de reservas cargadas desde el servidor. @type {Array<Object>} */
  let reservas = [];

  /** Indica si la peticion de carga esta en progreso. @type {boolean} */
  let cargandoReservas = false;

  /** Mensaje de error si la carga falla. @type {string|null} */
  let errorReservas = null;

  /** Texto del buscador para filtrar reservas por multiples campos. @type {string} */
  let busquedaReserva = '';

  /** Estado seleccionado en el selector de filtro; 'todos' para no filtrar. @type {string} */
  let filtroEstadoReserva = 'todos';

  /** Controla la visibilidad del modal de cancelacion. @type {boolean} */
  let showModalCancelarReserva = false;

  /** Reserva sobre la que se esta procesando la cancelacion. @type {Object|null} */
  let reservaCancelando = null;

  /** Texto del motivo de cancelacion ingresado por el admin. @type {string} */
  let motivoCancelacion = '';

  /** Indica si la peticion de cancelacion esta en curso. @type {boolean} */
  let cancelando = false;

  /** Mensaje de error que se muestra dentro del modal de cancelacion. @type {string|null} */
  let mensajeCancelar = null;

  // Conteos por estado calculados sobre la lista completa, sin aplicar filtros.
  $: conteoReservas = {
    confirmada: reservas.filter(r => r.estado === 'confirmada').length,
    pendiente:  reservas.filter(r => r.estado === 'pendiente').length,
    cancelada:  reservas.filter(r => r.estado === 'cancelada').length,
    total:      reservas.length
  };

  // Mantiene el contador exportado sincronizado con el total de reservas.
  $: count = conteoReservas.total;

  // Lista de reservas filtradas reactivamente segun busqueda y estado seleccionado.
  $: reservasFiltradas = reservas.filter(r => {
    const q = busquedaReserva.toLowerCase().trim();
    const matchEstado = filtroEstadoReserva === 'todos' || r.estado === filtroEstadoReserva;
    const matchBusqueda = q === '' ||
      (r.noReservacion ?? '').toLowerCase().includes(q) ||
      (r.usuario ?? '').toLowerCase().includes(q) ||
      (r.nombreCompleto ?? '').toLowerCase().includes(q) ||
      (r.hotel ?? '').toLowerCase().includes(q) ||
      (r.correo ?? '').toLowerCase().includes(q);
    return matchEstado && matchBusqueda;
  });

  onMount(() => { cargarReservas(); });

  /**
   * Obtiene todas las reservaciones del sistema desde el backend.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarReservas() {
    cargandoReservas = true;
    errorReservas = null;
    try {
      const res = await fetch(`${API_BASE}/admin/reservaciones`, { credentials: 'include' });
      if (!res.ok) throw new Error(`Error ${res.status}`);
      reservas = await res.json();
    } catch (e) {
      errorReservas = 'No se pudieron cargar las reservas. ' + e.message;
    } finally {
      cargandoReservas = false;
    }
  }

  /**
   * Actualiza el filtro de estado activo. Se usa al hacer clic en las tarjetas de conteo.
   * @param {string} estado - Estado a filtrar o 'todos' para quitar el filtro.
   */
  function filtrarPorEstado(estado) {
    filtroEstadoReserva = estado;
  }

  /**
   * Abre el modal de cancelacion precargando los datos de la reserva seleccionada.
   * @param {Object} r - Objeto de la reserva a cancelar.
   */
  function abrirModalCancelar(r) {
    reservaCancelando = r;
    motivoCancelacion = '';
    mensajeCancelar = null;
    showModalCancelarReserva = true;
  }

  /**
   * Cierra el modal de cancelacion y limpia todo el estado relacionado.
   */
  function cerrarModalCancelar() {
    showModalCancelarReserva = false;
    reservaCancelando = null;
    motivoCancelacion = '';
    mensajeCancelar = null;
    cancelando = false;
  }

  /**
   * Envia la solicitud de cancelacion al backend y recarga la lista si tiene exito.
   * @async
   * @returns {Promise<void>}
   */
  async function confirmarCancelacion() {
    if (!reservaCancelando) return;
    cancelando = true;
    mensajeCancelar = null;
    try {
      const res = await fetch(`${API_BASE}/admin/reservaciones/${reservaCancelando.id}/cancelar`, {
        method: 'PATCH', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo: motivoCancelacion || 'Cancelada por administrador' })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje || `Error ${res.status}`);
      await cargarReservas();
      cerrarModalCancelar();
    } catch (e) {
      mensajeCancelar = e.message;
    } finally {
      cancelando = false;
    }
  }
</script>

<!-- Tarjetas de conteo clicables para filtrar por estado -->
<div class="adm__stats-grid" style="margin-bottom:1.25rem">
  <div class="adm__stat-card adm__stat-card--green" style="cursor:pointer" on:click={() => filtrarPorEstado('confirmada')} on:keydown={() => {}} role="button" tabindex="0">
    <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg></div></div>
    <p class="adm__stat-value">{conteoReservas.confirmada}</p>
    <p class="adm__stat-label">Confirmadas</p>
  </div>
  <div class="adm__stat-card adm__stat-card--amber" style="cursor:pointer" on:click={() => filtrarPorEstado('pendiente')} on:keydown={() => {}} role="button" tabindex="0">
    <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div></div>
    <p class="adm__stat-value">{conteoReservas.pendiente}</p>
    <p class="adm__stat-label">Pendientes</p>
  </div>
  <div class="adm__stat-card adm__stat-card--red" style="cursor:pointer" on:click={() => filtrarPorEstado('cancelada')} on:keydown={() => {}} role="button" tabindex="0">
    <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div></div>
    <p class="adm__stat-value">{conteoReservas.cancelada}</p>
    <p class="adm__stat-label">Canceladas</p>
  </div>
  <div class="adm__stat-card adm__stat-card--blue" style="cursor:pointer" on:click={() => filtrarPorEstado('todos')} on:keydown={() => {}} role="button" tabindex="0">
    <div class="adm__stat-top"><div class="adm__stat-icon-wrap"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div></div>
    <p class="adm__stat-value">{conteoReservas.total}</p>
    <p class="adm__stat-label">Total</p>
  </div>
</div>

<!-- Tabla de reservaciones con buscador y selector de estado -->
<div class="adm__card adm__card--no-pad">
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

  <!-- Controles de busqueda y filtro por estado -->
  <div class="adm__filters-bar" style="padding:.75rem 1.25rem; border-bottom:1px solid var(--adm-border)">
    <div class="adm__search-wrap">
      <svg class="adm__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
      <input
        class="adm__search-input"
        type="text"
        placeholder="Buscar por No. reserva, usuario, nombre o hotel..."
        bind:value={busquedaReserva}
        aria-label="Buscar reservaciones"
      />
    </div>
    <select class="adm__select" bind:value={filtroEstadoReserva}>
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
            <th>No. Reserva</th><th>Usuario</th><th>Hotel</th><th>Check-in</th><th>Check-out</th>
            <th>Total</th><th>Creada</th><th>Estado</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {#each reservasFiltradas as r (r.id)}
            <tr>
              <td class="adm__table-mono" style="font-size:.8rem">{r.noReservacion}</td>
              <td>
                <p style="margin:0; font-weight:600; font-size:.85rem">@{r.usuario}</p>
                <p style="margin:0; font-size:.72rem; color:var(--adm-text-muted)">{r.nombreCompleto}</p>
              </td>
              <td style="font-size:.85rem">{r.hotel ?? '—'}</td>
              <td style="font-size:.82rem">{r.checkIn ?? '—'}</td>
              <td style="font-size:.82rem">{r.checkOut ?? '—'}</td>
              <td class="adm__table-money">$ {(r.total ?? 0).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
              <td style="font-size:.75rem; color:var(--adm-text-muted)">{r.fechaCreacion ? r.fechaCreacion.substring(0,16) : '—'}</td>
              <td><span class="adm__badge {badge(r.estado)}">{r.estado}</span></td>
              <td>
                <!-- Solo se puede cancelar si la reserva esta confirmada o pendiente -->
                {#if r.estado === 'confirmada' || r.estado === 'pendiente'}
                  <button class="adm__icon-btn adm__icon-btn--delete" title="Cancelar reservación" on:click={() => abrirModalCancelar(r)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                  </button>
                {:else}
                  <span style="color:var(--adm-text-muted); font-size:.75rem; padding:0 .5rem">—</span>
                {/if}
              </td>
            </tr>
          {/each}
          {#if reservasFiltradas.length === 0}
            <tr>
              <td colspan="9" class="adm__empty-cell">
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
      Mostrando {reservasFiltradas.length} de {reservas.length} reservaciones
      {#if filtroEstadoReserva !== 'todos' || busquedaReserva}
        · con filtros activos
      {/if}
    </div>
  {/if}
</div>

<!-- Modal de confirmacion para cancelar una reservacion -->
{#if showModalCancelarReserva && reservaCancelando}
  <div class="adm__overlay" on:click={cerrarModalCancelar} on:keydown={e => e.key === 'Escape' && cerrarModalCancelar()} role="button" tabindex="-1" aria-label="Cerrar modal"></div>
  <div class="adm__rol-modal" style="max-width:480px; border-radius:16px; overflow:hidden">

    <div class="adm__cancel-modal__header">
      <div class="adm__cancel-modal__icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      </div>
      <div>
        <p class="adm__cancel-modal__title">Cancelar Reservación</p>
        <p class="adm__cancel-modal__subtitle">{reservaCancelando.noReservacion} · {reservaCancelando.hotel ?? 'Sin hotel'}</p>
      </div>
      <button class="adm__cancel-modal__close" on:click={cerrarModalCancelar} aria-label="Cerrar">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <!-- Resumen de la reserva que se va a cancelar -->
    <div class="adm__cancel-modal__body">
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

      <label class="adm__cancel-motivo-label" for="motivo-cancel">
        Motivo de cancelación <span style="text-transform:none; font-weight:400">(opcional)</span>
      </label>
      <textarea id="motivo-cancel" class="adm__cancel-motivo-textarea" bind:value={motivoCancelacion} rows="3" placeholder="Ej: Solicitud del cliente, error en la reserva..."></textarea>

      {#if mensajeCancelar}
        <div class="adm__feedback adm__feedback--error" style="margin-top:.75rem">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {mensajeCancelar}
        </div>
      {/if}

      <!-- Advertencia de que la accion es irreversible -->
      <div class="adm__cancel-warning">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0; margin-top:.1rem">
          <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
        <span>Esta acción no se puede deshacer. El estado pasará a <strong>Cancelada</strong> de forma inmediata.</span>
      </div>
    </div>

    <div class="adm__cancel-modal__footer">
      <button class="adm__btn adm__btn--ghost" on:click={cerrarModalCancelar} disabled={cancelando}>No, mantener reserva</button>
      <button class="adm__btn--cancel-confirm" on:click={confirmarCancelacion} disabled={cancelando}>
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
