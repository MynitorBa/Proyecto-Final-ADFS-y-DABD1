<script>
/**
 * @file AdminHistorial.svelte
 * @description Admin panel section that displays the full flight history for the system.
 * Provides tab-based filtering by flight status (All, Active, Cancelled, Finished) and a
 * text search bar that matches against flight number, origin or destination. Each row shows
 * departure/arrival dates and times, available seats per class, prices and a status badge.
 * Active and in-progress flights have a Cancel button that triggers a confirmation dialog
 * before calling the backend cancellation endpoint. Dispatches 'vueloCancelado' to the parent
 * after a successful cancellation.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Function to show a confirmation dialog. Signature: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Full list of flights loaded from the historial endpoint. @type {any[]} */
  let historialVuelos        = [];

  /** Whether the historial fetch is in progress. @type {boolean} */
  let loadingHistorialVuelos = false;

  /**
   * Active tab key for status filtering.
   * Possible values: 'todos', 'Activo', 'Cancelado', 'Finalizado'.
   * @type {string}
   */
  let filtroEstado  = 'todos';

  /** Text used to filter flights by flight number, origin or destination. @type {string} */
  let filtroBusqueda = '';

  // Returns flights that match both the active status tab and the text search query.
  $: vuelosFiltrados = historialVuelos.filter(v => {
    const coincideEstado = filtroEstado === 'todos' || v.estado === filtroEstado;
    const q = filtroBusqueda.toLowerCase();
    const coincideBusqueda = !q ||
      v.numeroVuelo?.toLowerCase().includes(q) ||
      v.origen?.toLowerCase().includes(q) ||
      v.destino?.toLowerCase().includes(q);
    return coincideEstado && coincideBusqueda;
  });

  /**
   * Counts of flights by status for the tab badges, computed reactively from historialVuelos.
   * @type {{ todos: number, Activo: number, Cancelado: number, Finalizado: number }}
   */
  $: contadores = {
    todos:      historialVuelos.length,
    Activo:     historialVuelos.filter(v => v.estado === 'Activo').length,
    Cancelado:  historialVuelos.filter(v => v.estado === 'Cancelado').length,
    Finalizado: historialVuelos.filter(v => v.estado === 'Finalizado').length,
  };

  /**
   * On mount: loads the flight history from the backend.
   */
  onMount(() => { cargarHistorial(); });

  /**
   * Fetches the complete flight history from the backend API and stores it in historialVuelos.
   * Shows a toast on error and sets loadingHistorialVuelos during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarHistorial() {
    loadingHistorialVuelos = true;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/historial`, { credentials: 'include' });
      if (r.ok) historialVuelos = await r.json();
      else mostrarToast('error', 'Error al cargar el historial de vuelos');
    } catch { mostrarToast('error', 'Error de conexion al cargar el historial'); }
    finally { loadingHistorialVuelos = false; }
  }

  /**
   * Shows a danger confirmation dialog, and if confirmed, sends a PUT request to cancel
   * the specified flight. Also cancels all active tickets and reservations associated with
   * the flight. On success reloads the historial and dispatches 'vueloCancelado'.
   * @async
   * @param {number} vueloId - The ID of the flight to cancel.
   * @returns {Promise<void>}
   */
  async function handleCancelarVuelo(vueloId) {
    const ok = await mostrarConfirm(
      '¿Cancelar este vuelo?',
      'Se cancelaran tambien los boletos activos y las reservaciones asociadas.',
      'danger'
    );
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/admin/vuelos/${vueloId}/cancelar`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' }
      });
      if (r.ok) {
        mostrarToast('success', 'Vuelo cancelado exitosamente');
        await cargarHistorial();
        dispatch('vueloCancelado');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al cancelar el vuelo');
      }
    } catch { mostrarToast('error', 'Error de conexion al cancelar el vuelo'); }
  }
</script>

<!-- Seccion de historial completo de vuelos con filtros y opcion de cancelacion -->
<section class="admin-section">
  <!-- Encabezado con titulo y boton de actualizacion manual -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Historial de Vuelos</h2>
      <p class="admin-section__subtitle">Todos los vuelos del sistema</p>
    </div>
    <button class="btn-add" on:click={cargarHistorial} style="background:#4b5563">
      ↻ Actualizar
    </button>
  </div>

  {#if loadingHistorialVuelos}
    <p class="loading-text">Cargando historial...</p>

  {:else if historialVuelos.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay vuelos registrados.</p>
    </div>

  {:else}
    <!-- Barra de filtros: pestanas por estado y campo de busqueda por texto -->
    <div class="historial-filtros">

      <div class="historial-tabs">
        {#each [
          { key: 'todos',      label: 'Todos',      color: '#6b7280' },
          { key: 'Activo',     label: 'Activos',    color: '#2e7d32' },
          { key: 'Cancelado',  label: 'Cancelados', color: '#c62828' },
          { key: 'Finalizado', label: 'Finalizados',color: '#1565c0' }
        ] as tab}
          <button
            class="historial-tab"
            class:historial-tab--active={filtroEstado === tab.key}
            on:click={() => filtroEstado = tab.key}
            style="--tab-color:{tab.color}">
            {tab.label}
            <span class="historial-tab__count">{contadores[tab.key] ?? contadores.todos}</span>
          </button>
        {/each}
      </div>

      <input
        type="text"
        class="historial-search"
        bind:value={filtroBusqueda}
        placeholder="Buscar por numero de vuelo, origen o destino..."
      />
    </div>

    {#if vuelosFiltrados.length === 0}
      <div class="placeholder-card">
        <p class="placeholder-card__text">No hay vuelos que coincidan con los filtros.</p>
      </div>
    {:else}
      <!-- Tabla de vuelos filtrados con fechas, horarios, precios, estado y accion de cancelar -->
      <p class="historial-count">
        Mostrando <strong>{vuelosFiltrados.length}</strong> de {historialVuelos.length} vuelos
      </p>
      <div class="vuelos-table">
        <table class="table">
          <thead class="table__head">
            <tr>
              <th class="table__header">No. Vuelo</th>
              <th class="table__header">Origen</th>
              <th class="table__header">Destino</th>
              <th class="table__header">Fecha Salida</th>
              <th class="table__header">Salida</th>
              <th class="table__header">Fecha Llegada</th>
              <th class="table__header">Llegada</th>
              <th class="table__header">Turista</th>
              <th class="table__header">Ejecutiva</th>
              <th class="table__header">P. Turista</th>
              <th class="table__header">P. Ejecutiva</th>
              <th class="table__header">Estado</th>
              <th class="table__header">Acciones</th>
            </tr>
          </thead>
          <tbody class="table__body">
            {#each vuelosFiltrados as vuelo}
              <tr class="table__row">
                <td class="table__cell">{vuelo.numeroVuelo}</td>
                <td class="table__cell">{vuelo.origen}</td>
                <td class="table__cell">{vuelo.destino}</td>
                <td class="table__cell">{vuelo.fecha}</td>
                <td class="table__cell">{vuelo.horaSalida}</td>
                <td class="table__cell">
                  {#if vuelo.fechaLlegada && vuelo.fechaLlegada !== vuelo.fecha}
                    <span class="fecha-llegada-distinta">
                      {vuelo.fechaLlegada}
                      <span class="nextday-tag">+dia</span>
                    </span>
                  {:else}
                    {vuelo.fechaLlegada ?? vuelo.fecha}
                  {/if}
                </td>
                <td class="table__cell">{vuelo.horaLlegada}</td>
                <td class="table__cell">{vuelo.boletosTurista} disp.</td>
                <td class="table__cell">{vuelo.boletosEjecutivo} disp.</td>
                <td class="table__cell">${vuelo.precioTurista}</td>
                <td class="table__cell">${vuelo.precioEjecutiva}</td>
                <td class="table__cell">
                  {#if vuelo.estado === 'Activo'}
                    <span class="status-badge status-badge--activo">{vuelo.estado}</span>
                  {:else if vuelo.estado === 'Cancelado'}
                    <span class="status-badge status-badge--cancelado">{vuelo.estado}</span>
                  {:else if vuelo.estado === 'Finalizado'}
                    <span class="status-badge status-badge--completado">{vuelo.estado}</span>
                  {:else}
                    <span class="status-badge status-badge--activo">{vuelo.estado}</span>
                  {/if}
                </td>
                <td class="table__cell">
                  <div class="table__actions">
                    {#if vuelo.estado === 'Activo' || vuelo.estado === 'En curso'}
                      <button class="table__action-btn table__action-btn--cancel"
                        on:click={() => handleCancelarVuelo(vuelo.id)}>
                        Cancelar
                      </button>
                    {/if}
                  </div>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  {/if}
</section>
