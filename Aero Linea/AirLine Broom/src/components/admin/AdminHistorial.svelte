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
  import AdminEditarVuelo from './AdminEditarVuelo.svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Controla la visibilidad del modal de edicion de vuelo. @type {boolean} */
  let mostrarEditarModal = false;

  /** Vuelo actualmente seleccionado para editar. @type {any} */
  let vueloEditar = null;

  /** Lista de aeropuertos cargados para el modal de edicion. @type {any[]} */
  let aeropuertos = [];

  /** Lista de aviones cargados para el modal de edicion. @type {any[]} */
  let aviones = [];

  /** Lista de tripulantes cargados para el modal de edicion. @type {any[]} */
  let tripulacion = [];

  /**
   * Carga aeropuertos, aviones y tripulantes necesarios para el modal de edicion.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarDatosEdicion() {
    try {
      const [rA, rAv, rT] = await Promise.all([
        fetch(`${API}/api/aeropuertos`,  { credentials: 'include' }),
        fetch(`${API}/api/aviones`,      { credentials: 'include' }),
        fetch(`${API}/api/tripulacion`,  { credentials: 'include' })
      ]);
      if (rA.ok)  aeropuertos = await rA.json();
      if (rAv.ok) aviones     = await rAv.json();
      if (rT.ok)  tripulacion = await rT.json();
    } catch(e) { console.error('Error cargando datos de edicion', e); }
  }

  /** Lista completa de vuelos cargados desde el endpoint del historial. @type {any[]} */
  let historialVuelos        = [];

  /** Indica si la carga del historial esta en progreso. @type {boolean} */
  let loadingHistorialVuelos = false;

  /**
   * Clave de la pestana activa.
   * Valores: 'todos', 'Activo', 'En curso', 'Cancelado', 'Finalizado', 'recientes'.
   * @type {string}
   */
  let filtroEstado   = 'todos';

  /** Texto libre: busca en numero de vuelo, origen y destino. @type {string} */
  let filtroBusqueda = '';

  /** Codigo IATA de origen para filtrar (p.ej. "GUA"). @type {string} */
  let filtroOrigen   = '';

  /** Codigo IATA de destino para filtrar (p.ej. "YYZ"). @type {string} */
  let filtroDestino  = '';

  /** Fecha minima del rango (YYYY-MM-DD). @type {string} */
  let fechaDesde     = '';

  /** Fecha maxima del rango (YYYY-MM-DD). @type {string} */
  let fechaHasta     = '';

  /** Vuelos que coinciden con todos los filtros activos. @type {any[]} */
  let vuelosFiltrados = [];
  $: {
    // ── Base: para "recientes" se preordena por ID desc y se toman 20 ──
    const base = filtroEstado === 'recientes'
      ? [...historialVuelos].sort((a, b) => (b.id ?? 0) - (a.id ?? 0)).slice(0, 20)
      : historialVuelos;

    vuelosFiltrados = base.filter(v => {
      // ── Filtro por estado (recientes y todos no filtran por estado) ──
      if (filtroEstado !== 'todos' && filtroEstado !== 'recientes') {
        if (v.estado !== filtroEstado) return false;
      }

      // ── Busqueda por texto libre ─────────────────────────────────
      const q = filtroBusqueda.trim().toLowerCase();
      if (q) {
        const enNumero  = v.numeroVuelo?.toLowerCase().includes(q);
        const enOrigen  = v.origen?.toLowerCase().includes(q);
        const enDestino = v.destino?.toLowerCase().includes(q);
        if (!enNumero && !enOrigen && !enDestino) return false;
      }

      // ── Filtro por IATA origen ───────────────────────────────────
      const fOrigen = filtroOrigen.trim().toUpperCase();
      if (fOrigen && !v.origen?.toUpperCase().startsWith(fOrigen)) return false;

      // ── Filtro por IATA destino ──────────────────────────────────
      const fDestino = filtroDestino.trim().toUpperCase();
      if (fDestino && !v.destino?.toUpperCase().startsWith(fDestino)) return false;

      // ── Filtro por rango de fechas ───────────────────────────────
      const fechaVuelo = (v.fecha ?? '').split('T')[0];
      if (fechaDesde && fechaVuelo < fechaDesde) return false;
      if (fechaHasta && fechaVuelo > fechaHasta) return false;

      return true;
    });
  }

  /** Conteo de vuelos por estado para los badges. */
  $: contadores = {
    todos:      historialVuelos.length,
    Activo:     historialVuelos.filter(v => v.estado === 'Activo').length,
    'En curso': historialVuelos.filter(v => v.estado === 'En curso').length,
    Cancelado:  historialVuelos.filter(v => v.estado === 'Cancelado').length,
    Finalizado: historialVuelos.filter(v => v.estado === 'Finalizado').length,
    recientes:  Math.min(20, historialVuelos.length),
  };

  /** Indica si hay algun filtro avanzado activo (ademas del tab). @type {boolean} */
  $: hayFiltrosActivos = !!(filtroBusqueda || filtroOrigen || filtroDestino || fechaDesde || fechaHasta);

  /** Limpia todos los filtros de busqueda y fecha sin tocar la pestana de estado. */
  function limpiarFiltros() {
    filtroBusqueda = '';
    filtroOrigen   = '';
    filtroDestino  = '';
    fechaDesde     = '';
    fechaHasta     = '';
  }

  /**
   * Al montar: carga el historial de vuelos y los datos compartidos para el modal de edicion.
   */
  onMount(() => { cargarHistorial(); cargarDatosEdicion(); });

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
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:4px"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>Actualizar
    </button>
  </div>

  {#if loadingHistorialVuelos}
    <p class="loading-text">Cargando historial...</p>

  {:else if historialVuelos.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay vuelos registrados.</p>
    </div>

  {:else}
    <!-- Barra de filtros: pestanas por estado + filtros avanzados -->
    <div class="historial-filtros">

      <!-- Pestanas de estado -->
      <div class="historial-tabs">
        {#each [
          { key: 'todos',      label: 'Todos',       color: '#6b7280' },
          { key: 'Activo',     label: 'Activos',     color: '#2e7d32' },
          { key: 'En curso',   label: 'En curso',    color: '#b45309' },
          { key: 'Cancelado',  label: 'Cancelados',  color: '#c62828' },
          { key: 'Finalizado', label: 'Finalizados', color: '#1565c0' },
          { key: 'recientes',  label: 'Recientes',   color: '#8b5cf6' }
        ] as tab}
          <button
            class="historial-tab"
            class:historial-tab--active={filtroEstado === tab.key}
            on:click={() => filtroEstado = tab.key}
            style="--tab-color:{tab.color}">
            {tab.label}
            <span class="historial-tab__count">{contadores[tab.key] ?? 0}</span>
          </button>
        {/each}
      </div>

      <!-- Filtros avanzados -->
      <div class="historial-advanced">

        <!-- Busqueda por texto -->
        <div class="hf-search-wrap">
          <svg class="hf-search-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input
            type="text"
            class="hf-search"
            bind:value={filtroBusqueda}
            placeholder="Buscar por número, ciudad, código..."
          />
        </div>

        <!-- Fila de filtros específicos -->
        <div class="hf-row">

          <div class="hf-field">
            <label class="hf-label">Origen IATA</label>
            <input
              type="text"
              class="hf-iata"
              bind:value={filtroOrigen}
              placeholder="GUA"
              maxlength="4"
            />
          </div>

          <div class="hf-field">
            <label class="hf-label">Destino IATA</label>
            <input
              type="text"
              class="hf-iata"
              bind:value={filtroDestino}
              placeholder="YYZ"
              maxlength="4"
            />
          </div>

          <div class="hf-field">
            <label class="hf-label">Desde</label>
            <input type="date" class="hf-date" bind:value={fechaDesde} />
          </div>

          <div class="hf-field">
            <label class="hf-label">Hasta</label>
            <input type="date" class="hf-date" bind:value={fechaHasta} />
          </div>

          {#if hayFiltrosActivos}
            <button class="hf-clear" on:click={limpiarFiltros} title="Limpiar filtros">
              <svg width="11" height="11" viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="vertical-align:-1px;margin-right:4px"><path d="M2 2l8 8M10 2L2 10"/></svg>Limpiar
            </button>
          {/if}

        </div>
      </div>
    </div>

    {#if vuelosFiltrados.length === 0}
      <div class="hf-empty">
        <span class="hf-empty__icon"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><circle cx="10" cy="10" r="7"/><path d="M16 16l4 4"/></svg></span>
        <p class="hf-empty__title">Sin resultados</p>
        <p class="hf-empty__sub">
          {#if hayFiltrosActivos}
            Ningún vuelo coincide con los filtros aplicados.
          {:else}
            No hay vuelos en esta categoría.
          {/if}
        </p>
        {#if hayFiltrosActivos}
          <button class="hf-clear hf-clear--center" on:click={limpiarFiltros}>Limpiar filtros</button>
        {/if}
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
                      <button class="table__action-btn table__action-btn--edit"
                        on:click={() => { vueloEditar = vuelo; mostrarEditarModal = true; }}>
                        Editar
                      </button>
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

{#if mostrarEditarModal && vueloEditar}
  <AdminEditarVuelo
    vuelo={vueloEditar}
    {aeropuertos}
    {aviones}
    {tripulacion}
    {mostrarToast}
    onClose={() => mostrarEditarModal = false}
    onGuardado={() => { mostrarEditarModal = false; cargarHistorial(); dispatch('vueloCancelado'); }}
  />
{/if}

<style>
.table__action-btn--edit {
  background: #1d4ed8;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: .35rem .8rem;
  font-size: .8rem;
  font-weight: 600;
  cursor: pointer;
  transition: background .15s;
  margin-right: .4rem;
}
.table__action-btn--edit:hover { background: #1e40af; }

/* ── Filtros avanzados ─────────────────────────────────────── */
.historial-advanced {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

/* Barra de búsqueda general */
.hf-search-wrap {
  position: relative;
}
.hf-search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #9ca3af;
  pointer-events: none;
}
.hf-search {
  width: 100%;
  padding: 10px 12px 10px 36px;
  font-size: 0.875rem;
  border: 1.5px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  color: #111827;
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}
.hf-search:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,.12);
}

/* Fila de filtros específicos */
.hf-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-end;
}
.hf-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hf-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: .03em;
}
.hf-iata {
  width: 80px;
  padding: 8px 10px;
  font-size: 0.875rem;
  font-family: monospace;
  font-weight: 700;
  text-transform: uppercase;
  border: 1.5px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #111827;
  transition: border-color 0.15s;
}
.hf-iata:focus {
  outline: none;
  border-color: #6366f1;
}
.hf-date {
  padding: 8px 10px;
  font-size: 0.875rem;
  border: 1.5px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #111827;
  transition: border-color 0.15s;
}
.hf-date:focus {
  outline: none;
  border-color: #6366f1;
}

/* Botón limpiar */
.hf-clear {
  padding: 8px 14px;
  font-size: 0.8rem;
  font-weight: 600;
  background: #f3f4f6;
  color: #374151;
  border: 1.5px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  align-self: flex-end;
}
.hf-clear:hover { background: #e5e7eb; color: #111827; }
.hf-clear--center { margin-top: 8px; }

/* Estado vacío */
.hf-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 40px 20px;
  text-align: center;
  color: #6b7280;
}
.hf-empty__icon { font-size: 2rem; }
.hf-empty__title { font-size: 1rem; font-weight: 700; color: #374151; margin: 0; }
.hf-empty__sub   { font-size: 0.875rem; margin: 0; }

</style>
