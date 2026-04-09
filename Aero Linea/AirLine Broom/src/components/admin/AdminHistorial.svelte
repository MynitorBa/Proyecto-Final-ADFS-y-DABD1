<script>
/**
 * @file AdminHistorial.svelte
 * @description Seccion del panel de administracion que muestra el historial completo de vuelos del sistema.
 * Proporciona filtrado por pestanas segun el estado del vuelo (Todos, Activo, Cancelado, Finalizado) y una
 * barra de busqueda por texto que coincide con el numero de vuelo, origen o destino. Cada fila muestra
 * fechas y horarios de salida/llegada, asientos disponibles por clase, precios y una insignia de estado.
 * Los vuelos activos y en curso tienen un boton de Cancelar que activa un dialogo de confirmacion antes de
 * llamar al endpoint de cancelacion del backend. Despacha 'vueloCancelado' al padre tras una cancelacion exitosa.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Lista completa de vuelos cargados desde el endpoint del historial. @type {any[]} */
  let historialVuelos        = [];

  /** Indica si la carga del historial esta en progreso. @type {boolean} */
  let loadingHistorialVuelos = false;

  /**
   * Clave de la pestana activa para el filtrado por estado.
   * Valores posibles: 'todos', 'Activo', 'Cancelado', 'Finalizado'.
   * @type {string}
   */
  let filtroEstado  = 'todos';

  /** Texto usado para filtrar vuelos por numero de vuelo, origen o destino. @type {string} */
  let filtroBusqueda = '';

  /** Vuelos que coinciden con el filtro de estado activo y la busqueda por texto. @type {any[]} */
  let vuelosFiltrados = [];
  $: vuelosFiltrados = historialVuelos.filter(v => {
    const coincideEstado = filtroEstado === 'todos' || v.estado === filtroEstado;
    const q = filtroBusqueda.toLowerCase();
    const coincideBusqueda = !q ||
      v.numeroVuelo?.toLowerCase().includes(q) ||
      v.origen?.toLowerCase().includes(q) ||
      v.destino?.toLowerCase().includes(q);
    return coincideEstado && coincideBusqueda;
  });

  /** Conteo de vuelos por estado para los badges de las pestanas. @type {{ todos: number, Activo: number, Cancelado: number, Finalizado: number }} */
  let contadores = { todos: 0, Activo: 0, Cancelado: 0, Finalizado: 0 };
  $: contadores = {
    todos:      historialVuelos.length,
    Activo:     historialVuelos.filter(v => v.estado === 'Activo').length,
    Cancelado:  historialVuelos.filter(v => v.estado === 'Cancelado').length,
    Finalizado: historialVuelos.filter(v => v.estado === 'Finalizado').length,
  };

  /**
   * Al montar: carga el historial de vuelos desde el backend.
   */
  onMount(() => { cargarHistorial(); });

  /**
   * Obtiene el historial completo de vuelos desde la API del backend y lo almacena en historialVuelos.
   * Muestra un toast en caso de error y establece loadingHistorialVuelos durante la solicitud.
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
   * Muestra un dialogo de confirmacion de peligro y, si se confirma, envia una solicitud PUT para cancelar
   * el vuelo especificado. Tambien cancela todos los boletos activos y las reservaciones asociadas al vuelo.
   * Al tener exito recarga el historial y despacha 'vueloCancelado'.
   * @async
   * @param {number} vueloId - El ID del vuelo a cancelar.
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
