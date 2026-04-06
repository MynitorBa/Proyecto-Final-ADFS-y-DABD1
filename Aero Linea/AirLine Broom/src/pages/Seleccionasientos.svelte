<script>
/**
 * @file Seleccionasientos.svelte
 * @description Interactive seat map page for selecting seats across one or multiple flight groups.
 * The plane is visually rendered with a nose SVG, a cabin body divided into an Ejecutiva section
 * (fixed 4 rows, columns A-F, IDs: E-{col}{row}) and a Turista section (dynamic rows, IDs:
 * {col}{row}), and a tail SVG. On mount it loads the seat map from GET /api/asientos/:vueloId
 * and builds a local map keyed by backend seat IDs. When the user clicks a free seat it calls
 * PUT /api/asientos/:boletoId to persist the assignment and updates the local state without
 * reloading. A sidebar shows passenger assignment progress, a seat legend, and a continue button
 * that advances to the next flight group or navigates to 'carrito' when all groups are done.
 */
// @ts-nocheck
  import '../styles/asientos.css';
  import { onMount } from 'svelte';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Array of flight group objects passed from the previous step, each with vueloId, numeroVuelo, avionModelo, avionMarca, clase, and boletos array. @type {Array<object>} */
  export let flightData = [];

  import { API } from '../lib/api.js';

  /** Number of seat columns in both Ejecutiva and Turista zones. @type {number} */
  const NUM_COLUMNAS    = 6;

  /** Fixed number of row rows in the Ejecutiva zone. @type {number} */
  const FILAS_EJECUTIVA = 4;

  /** Column letter labels used as seat column identifiers. @type {string[]} */
  const COLS_LABEL      = ['A','B','C','D','E','F'];

  /** Index of the currently active flight group being seat-mapped. @type {number} */
  let grupoActualIdx = 0;

  // Current flight group object resolved from flightData at grupoActualIdx.
  $: grupoActual   = flightData?.[grupoActualIdx] ?? null;

  // Total number of flight groups in flightData.
  $: totalGrupos   = flightData?.length ?? 0;

  // True when grupoActualIdx points to the last flight group.
  $: esUltimoGrupo = grupoActualIdx === totalGrupos - 1;

  // Flight ID of the current group, or null if unavailable.
  $: vueloId          = grupoActual?.vueloId      ?? null;

  // Flight number string for the current group's display header.
  $: numeroVuelo      = grupoActual?.numeroVuelo   ?? '';

  // Aircraft model name for the current group's display header.
  $: avionModelo      = grupoActual?.avionModelo   ?? '';

  // Aircraft brand name for the current group's display header.
  $: avionMarca       = grupoActual?.avionMarca    ?? '';

  // Seat class for the current group ('Turista' or 'Ejecutiva'), restricts selectable seats.
  $: claseActual      = grupoActual?.clase         ?? 'Turista';

  // Total number of passengers (boletos) in the current group.
  $: pasajerosTotales = grupoActual?.boletos?.length ?? 1;

  /** True while the seat map API request is in progress. @type {boolean} */
  let loading      = true;

  /** Error message shown if the seat map fails to load. @type {string|null} */
  let error        = null;

  /** True while a seat assignment PUT request is in progress. @type {boolean} */
  let guardando    = false;

  /** Error message shown if a seat assignment PUT request fails. @type {string|null} */
  let errorGuardar = null;

  /** Total number of rows (Ejecutiva + Turista) for the current flight's aircraft. @type {number} */
  let totalFilas       = 0;

  /** Set of seat ID strings already occupied by other passengers. @type {Set<string>} */
  let asientosOcupados = new Set();

  /** Array of boleto objects with boletoId and asiento for the current user's reservation. @type {Array<{boletoId: number, asiento: string}>} */
  let boletosUsuario   = [];

  /** Map of seat ID string to seat object with id, fila, col, clase, and estado. @type {Object.<string, object>} */
  let asientos         = {};

  /** Array of seat ID strings, one per passenger, representing current seat assignments. @type {string[]} */
  let seleccionados    = [];

  /** Index of the passenger whose seat is currently being assigned. @type {number} */
  let pasajeroActual   = 0;

  /**
   * Constructs the backend seat ID string for an Ejecutiva seat given row number and column letter.
   * @param {number} fila - Row number (1-based).
   * @param {string} col - Column letter (A-F).
   * @returns {string} Seat ID in the format 'E-{col}{fila}', e.g. 'E-A1'.
   */
  function idEjecutiva(fila, col) { return `E-${col}${fila}`; }

  /**
   * Constructs the backend seat ID string for a Turista seat given row number and column letter.
   * @param {number} fila - Row number (1-based).
   * @param {string} col - Column letter (A-F).
   * @returns {string} Seat ID in the format '{col}{fila}', e.g. 'A1'.
   */
  function idTurista(fila, col)   { return `${col}${fila}`;   }

  // Row number array for the Ejecutiva zone, always [1, 2, 3, 4].
  $: filasEje = Array.from({ length: FILAS_EJECUTIVA }, (_, i) => i + 1);

  // Row number array for the Turista zone, dynamically sized from totalFilas minus Ejecutiva rows.
  $: filasT = totalFilas > FILAS_EJECUTIVA
      ? Array.from({ length: totalFilas - FILAS_EJECUTIVA }, (_, i) => i + 1)
      : [];

  onMount(async () => {
    if (!flightData || flightData.length === 0) {
      error = 'No se recibio informacion de vuelos.';
      loading = false;
      return;
    }
    await cargarAsientos();
  });

  /** Tracks the last grupoActualIdx for which seats were loaded to avoid redundant reloads. @type {number} */
  let ultimoGrupoIdx = -1;

  // Reload seat map whenever the active group index changes and a valid vueloId is present.
  $: if (grupoActualIdx !== ultimoGrupoIdx && vueloId) {
    ultimoGrupoIdx = grupoActualIdx;
    cargarAsientos();
  }

  /**
   * Fetches seat availability for the current vueloId from GET /api/asientos/:vueloId. Populates
   * totalFilas, asientosOcupados, boletosUsuario, and seleccionados from the response, resets
   * pasajeroActual to 0, and rebuilds the seat map via construirMapa.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAsientos() {
    if (!vueloId) return;
    loading = true; error = null; errorGuardar = null;
    try {
      const res = await fetch(`${API}/api/asientos/${vueloId}`, { credentials: 'include' });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message ?? 'Error al cargar asientos.'); }
      const data = await res.json();

      totalFilas       = data.totalFilas;
      asientosOcupados = new Set(data.asientosOcupados ?? []);
      boletosUsuario   = data.boletosUsuario ?? [];
      seleccionados    = boletosUsuario.map(b => b.asiento);
      pasajeroActual   = 0;
      construirMapa(data.totalFilas);
    } catch (e) {
      error = e.message;
    } finally {
      loading = false;
    }
  }

  /**
   * Builds the asientos map keyed by backend seat ID for both Ejecutiva and Turista zones.
   * Each entry has id, fila, col, clase, and estado ('ocupado', 'propio', or 'libre').
   * @param {number} totalF - Total row count for the aircraft (Ejecutiva + Turista rows combined).
   */
  function construirMapa(totalF) {
    const mapa = {};
    const filasT = totalF - FILAS_EJECUTIVA;

    for (let row = 1; row <= FILAS_EJECUTIVA; row++) {
      for (const col of COLS_LABEL) {
        const id = idEjecutiva(row, col);
        mapa[id] = {
          id, fila: row, col,
          clase: 'Ejecutiva',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    for (let row = 1; row <= filasT; row++) {
      for (const col of COLS_LABEL) {
        const id = idTurista(row, col);
        mapa[id] = {
          id, fila: row, col,
          clase: 'Turista',
          estado: asientosOcupados.has(id) ? 'ocupado'
                : seleccionados.includes(id) ? 'propio'
                : 'libre'
        };
      }
    }

    asientos = mapa;
  }

  /**
   * Returns true if the given seat object can be selected by the current passenger. A seat is
   * selectable when it exists, is not occupied, and belongs to the same class as claseActual.
   * @param {object} a - Seat object from the asientos map.
   * @returns {boolean} Whether the seat can be selected.
   */
  function puedeSeleccionar(a) {
    if (!a) return false;
    if (a.estado === 'ocupado') return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return false;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return false;
    return true;
  }

  /**
   * Returns true if the seat button should be disabled. A seat is blocked if it is null, occupied,
   * or belongs to a different class than claseActual. Seats with estado 'propio' are never blocked
   * so the user can click them to shift passenger focus.
   * @param {object} a - Seat object from the asientos map.
   * @returns {boolean} Whether the seat button should be rendered as disabled.
   */
  function esBloqueado(a) {
    if (!a) return true;
    if (a.estado === 'ocupado') return true;
    if (a.estado === 'propio')  return false;
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return true;
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return true;
    return false;
  }

  /**
   * Returns the index of the given seat ID in the seleccionados array, or -1 if not found.
   * Used to render the passenger number badge on already-assigned seats.
   * @param {string} id - Seat ID string to look up.
   * @returns {number} Index in seleccionados, or -1.
   */
  function indicePasajero(id) { return seleccionados.indexOf(id); }

  /**
   * Handles a seat button click. If the seat is already owned by the user, shifts pasajeroActual
   * to that passenger's index. Otherwise validates selectability, sends PUT /api/asientos/:boletoId
   * to update the assignment, updates the local asientos map and seleccionados array without
   * reloading, and advances pasajeroActual to the next passenger without an assigned seat.
   * @async
   * @param {object} a - Seat object from the asientos map.
   * @returns {Promise<void>}
   */
  async function seleccionarAsiento(a) {
    if (guardando || !a || a.estado === 'ocupado') return;

    if (a.estado === 'propio') {
      const idx = seleccionados.indexOf(a.id);
      if (idx !== -1) pasajeroActual = idx;
      return;
    }

    if (!puedeSeleccionar(a)) return;
    if (seleccionados[pasajeroActual] === a.id) return;

    const boletoId        = boletosUsuario[pasajeroActual]?.boletoId;
    const asientoAnterior = seleccionados[pasajeroActual];
    if (!boletoId) return;

    guardando = true; errorGuardar = null;
    try {
      const res = await fetch(`${API}/api/asientos/${boletoId}`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nuevoAsiento: a.id })
      });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message ?? 'Error al cambiar asiento.'); }

      if (asientoAnterior && asientos[asientoAnterior])
        asientos[asientoAnterior] = { ...asientos[asientoAnterior], estado: 'libre' };
      asientos[a.id] = { ...asientos[a.id], estado: 'propio' };

      seleccionados[pasajeroActual] = a.id;
      seleccionados = [...seleccionados];
      asientos      = { ...asientos };

      const sig = seleccionados.findIndex((s, i) => i > pasajeroActual && !s);
      if (sig !== -1) pasajeroActual = sig;

    } catch (e) {
      errorGuardar = e.message;
    } finally {
      guardando = false;
    }
  }

  /**
   * Returns the CSS class string for a seat button based on its state and class compatibility.
   * Possible return values: 'asiento--vacio', 'asiento--ocupado', 'asiento--seleccionado',
   * 'asiento--clase-incorrecta', or 'asiento--libre'.
   * @param {object} a - Seat object from the asientos map, or null/undefined.
   * @returns {string} CSS modifier class string.
   */
  function claseAsiento(a) {
    if (!a) return 'asiento--vacio';
    if (a.estado === 'ocupado') return 'asiento--ocupado';
    if (a.estado === 'propio')  return 'asiento--seleccionado';
    if (claseActual === 'Ejecutiva' && a.clase !== 'Ejecutiva') return 'asiento--clase-incorrecta';
    if (claseActual === 'Turista'   && a.clase !== 'Turista')   return 'asiento--clase-incorrecta';
    return 'asiento--libre';
  }

  // True when all passengers in the current group have an assigned seat.
  $: todoSeleccionado = seleccionados.length === pasajerosTotales && seleccionados.every(Boolean);

  // Percentage of seats assigned out of total passengers for the progress bar.
  $: progreso         = (seleccionados.filter(Boolean).length / pasajerosTotales) * 100;

  /**
   * Advances to the next flight group if not on the last one, or navigates to 'carrito' when
   * all flight groups have been seat-mapped. Does nothing if not all seats are selected.
   */
  function handleContinuar() {
    if (!todoSeleccionado) return;
    if (!esUltimoGrupo) grupoActualIdx++;
    else navigateTo('carrito');
  }
</script>

<!-- Contenedor principal de la pagina de seleccion de asientos -->
<div class="asientos-page">
  <div class="asientos-page__container">

    <!-- Cabecera con boton de regreso e informacion del vuelo y clase activos -->
    <div class="asientos-page__header">
      <button class="asientos-page__back"
        on:click={() => grupoActualIdx > 0 ? grupoActualIdx-- : navigateTo('datos-pasajeros')}>
        Volver
      </button>
      <div class="asientos-titulo">
        <h1 class="asientos-titulo__main">Seleccion de Asientos</h1>
        <p class="asientos-titulo__sub">
          {avionMarca} {avionModelo}
          &nbsp;·&nbsp; Vuelo {numeroVuelo}
          &nbsp;·&nbsp; Clase {claseActual}
          &nbsp;·&nbsp; {pasajerosTotales} pasajero{pasajerosTotales > 1 ? 's' : ''}
          {#if totalGrupos > 1}&nbsp;·&nbsp;<strong>Vuelo {grupoActualIdx + 1} de {totalGrupos}</strong>{/if}
        </p>
      </div>
    </div>

    <!-- Indicador de progreso por vuelo cuando hay multiples grupos de vuelos -->
    {#if totalGrupos > 1}
      <div class="vuelos-progreso">
        {#each flightData as grupo, i}
          <div class="vuelos-progreso__item"
            class:vuelos-progreso__item--activo={i === grupoActualIdx}
            class:vuelos-progreso__item--completado={i < grupoActualIdx}>
            <span class="vuelos-progreso__numero">{i + 1}</span>
            <span class="vuelos-progreso__label">Vuelo {grupo.numeroVuelo}</span>
          </div>
          {#if i < totalGrupos - 1}
            <div class="vuelos-progreso__linea"
              class:vuelos-progreso__linea--completada={i < grupoActualIdx}></div>
          {/if}
        {/each}
      </div>
    {/if}

    <!-- Estado de carga o error mientras se obtiene el mapa de asientos desde la API -->
    {#if loading}
      <div class="asientos-estado">Cargando mapa de asientos...</div>
    {:else if error}
      <div class="asientos-estado asientos-estado--error">{error}</div>
    {:else}
      <div class="asientos-page__body">
        <div class="asientos-page__mapa-wrap">

          <!-- SVG decorativo de la nariz del avion -->
          <div class="avion-nariz">
            <svg viewBox="0 0 220 90" fill="none" xmlns="http://www.w3.org/2000/svg" class="avion-nariz__svg">
              <path d="M110 4 C70 4, 14 30, 10 58 L10 86 L210 86 L210 58 C206 30, 150 4, 110 4Z" fill="#1C1A18" stroke="#B89A7A" stroke-width="1.2"/>
              <path d="M110 14 C78 14, 28 36, 24 60 L24 78 L196 78 L196 60 C192 36, 142 14, 110 14Z" fill="#2c2a24" stroke="#B89A7A" stroke-width="0.6" opacity="0.6"/>
              <ellipse cx="68"  cy="50" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <ellipse cx="110" cy="44" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <ellipse cx="152" cy="50" rx="8" ry="5" fill="#c9a96e" opacity="0.3"/>
              <text x="110" y="73" text-anchor="middle" fill="#B89A7A" font-size="9" letter-spacing="3" font-family="inherit">CABINA</text>
            </svg>
          </div>

          <!-- Cuerpo del avion con encabezado de columnas, zona Ejecutiva y zona Turista -->
          <div class="avion-cuerpo">

            <!-- Etiquetas de columnas A-F con separador de pasillo central -->
            <div class="avion-cols-header">
              <div class="avion-col-label"></div>
              {#each COLS_LABEL as lbl, ci}
                {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                <div class="avion-col-label">{lbl}</div>
              {/each}
            </div>

            <!-- Filas de asientos de la zona Ejecutiva (filas 1 a 4) -->
            <div class="zona-label zona-label--ejecutiva"><span>Ejecutiva</span></div>
            {#each filasEje as fila}
              <div class="avion-fila avion-fila--ejecutiva">
                <div class="avion-fila__num">{fila}</div>
                {#each COLS_LABEL as col, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idEjecutiva(fila, col)]}
                  <button
                    class="asiento asiento--ejecutiva {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="E-{col}{fila}"
                  >
                    {#if a?.estado === 'propio'}
                      <span class="asiento__num">{indicePasajero(a.id) + 1}</span>
                    {/if}
                  </button>
                {/each}
              </div>
            {/each}

            <!-- Separador visual entre la cabina Ejecutiva y la zona Turista -->
            <div class="zona-separador">
              <div class="zona-separador__line"></div>
              <span class="zona-separador__label">Separador de Cabina</span>
              <div class="zona-separador__line"></div>
            </div>

            <!-- Filas de asientos de la zona Turista (filas dinamicas segun el avion) -->
            <div class="zona-label zona-label--turista"><span>Turista</span></div>
            {#each filasT as fila}
              <div class="avion-fila">
                <div class="avion-fila__num">{fila}</div>
                {#each COLS_LABEL as col, ci}
                  {#if ci === 3}<div class="avion-pasillo"></div>{/if}
                  {@const a = asientos[idTurista(fila, col)]}
                  <button
                    class="asiento {claseAsiento(a)}"
                    disabled={guardando || esBloqueado(a)}
                    on:click={() => seleccionarAsiento(a)}
                    title="{col}{fila}"
                  >
                    {#if a?.estado === 'propio'}
                      <span class="asiento__num">{indicePasajero(a.id) + 1}</span>
                    {/if}
                  </button>
                {/each}
              </div>
            {/each}

          </div>

          <!-- SVG decorativo de la cola del avion -->
          <div class="avion-cola">
            <svg viewBox="0 0 220 50" fill="none" xmlns="http://www.w3.org/2000/svg" class="avion-cola__svg">
              <path d="M10 0 L210 0 L210 24 C180 44, 140 50, 110 50 C80 50, 40 44, 10 24 Z" fill="#1C1A18" stroke="#B89A7A" stroke-width="1.2"/>
            </svg>
          </div>

        </div>

        <!-- Panel lateral con progreso de asignacion, lista de pasajeros, leyenda y boton de continuar -->
        <aside class="asientos-sidebar">

          <!-- Barra de progreso de asientos asignados sobre el total de pasajeros -->
          <div class="seleccion-progreso">
            <div class="seleccion-progreso__header">
              <span class="seleccion-progreso__titulo">Pasajeros</span>
              <span class="seleccion-progreso__conteo">{seleccionados.filter(Boolean).length} / {pasajerosTotales}</span>
            </div>
            <div class="seleccion-progreso__barra">
              <div class="seleccion-progreso__fill" style="width: {progreso}%"></div>
            </div>
            {#if guardando}<p class="seleccion-progreso__guardando">Guardando asiento...</p>{/if}
            {#if errorGuardar}<p class="seleccion-progreso__error">{errorGuardar}</p>{/if}
          </div>

          <!-- Lista de pasajeros con estado de asiento asignado o pendiente por cada boleto -->
          <div class="pasajeros-lista">
            {#each boletosUsuario as _, i}
              <button
                class="pasajero-item"
                class:pasajero-item--activo={i === pasajeroActual && !todoSeleccionado}
                class:pasajero-item--completo={!!seleccionados[i]}
                on:click={() => { pasajeroActual = i; }}
              >
                <div class="pasajero-item__numero">{i + 1}</div>
                <div class="pasajero-item__info">
                  <span class="pasajero-item__label">Pasajero {i + 1}</span>
                  <span class="pasajero-item__asiento">
                    {seleccionados[i] ? `Asiento ${seleccionados[i]}` : 'Sin asignar'}
                  </span>
                </div>
              </button>
            {/each}
          </div>

          <!-- Leyenda de colores de los estados posibles de un asiento -->
          <div class="leyenda">
            <h3 class="leyenda__titulo">Leyenda</h3>
            <div class="leyenda__items">
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--libre asiento--muestra"></div>
                <span>Disponible</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--seleccionado asiento--muestra"></div>
                <span>Seleccionado</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--ocupado asiento--muestra"></div>
                <span>Ocupado</span>
              </div>
              <div class="leyenda__item">
                <div class="leyenda__muestra asiento asiento--clase-incorrecta asiento--muestra"></div>
                <span>Otra clase</span>
              </div>
            </div>
          </div>

          <!-- Boton de avance al siguiente vuelo o confirmacion cuando todos los asientos estan asignados -->
          <button
            class="asientos-continuar"
            class:asientos-continuar--listo={todoSeleccionado}
            disabled={!todoSeleccionado || guardando}
            on:click={handleContinuar}
          >
            {#if guardando}
              Guardando...
            {:else if !todoSeleccionado}
              {@const faltantes = pasajerosTotales - seleccionados.filter(Boolean).length}
              Selecciona {faltantes} asiento{faltantes !== 1 ? 's' : ''} mas
            {:else if !esUltimoGrupo}
              Siguiente vuelo
            {:else}
              Confirmar Asientos
            {/if}
          </button>

        </aside>
      </div>
    {/if}

  </div>
</div>
