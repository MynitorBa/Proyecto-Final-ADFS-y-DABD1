<script>
/**
 * @file Vuelos.svelte
 * @description Main flight listing and selection page for Broom AirLine. Supports two entry
 * modes: a global keyword search (fromGlobalSearch flag) that shows a flat list of direct
 * flights with a re-search input, and a standard origin/destination/date search that shows
 * outbound and return flight steps with direct and escala tabs. Filters (price range, class,
 * flight type tab) are shown only in standard mode and are applied via POST /api/vuelos/buscar.
 * Global re-search uses GET /api/vuelos/busqueda-general. Each flight card renders schedule
 * info and class-option buttons for Turista and Ejecutiva; escala cards additionally render
 * a layover timeline with connection wait times. Selecting a flight and clicking the next-step
 * button calls POST /api/reservaciones and navigates to 'datos-pasajeros'. The DetalleVuelo
 * component is mounted as a modal when the user clicks "Ver Detalles".
 */
// @ts-nocheck
  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';
  import { sesion } from '../stores/sesion.js';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Search parameters object passed from the home or header search. @type {object|null} */
  export let searchParams = null;

  import { API } from '../lib/api.js';

  /** True when the page was opened from a global keyword search via the header. @type {boolean} */
  let isGlobalSearch = false;

  /** The original query string for the current global search session. @type {string} */
  let globalSearchQuery = '';

  /** The current value of the inline re-search input in global search mode. @type {string} */
  let newGlobalQuery = '';

  /** Search parameters object with origin/destination IDs, names, codes, dates, passenger count, and trip type. @type {object} */
  let searchData = {
    origenId: null, destinoId: null,
    origenNombre: '', destinoNombre: '',
    origenCodigo: '', destinoCodigo: '',
    fechaIda: '', fechaVuelta: '',
    pasajeros: 1, tripType: 'roundtrip',
    flightMode: 'todos'
  };

  /** Current step being displayed, either 'outbound' or 'return'. @type {string} */
  let currentView = 'outbound';

  /** Currently selected outbound flight/escala and class, or all-null before selection. @type {{type: string|null, flight: object|null, escala: object|null, clase: object|null}} */
  let selectedOutbound = { type: null, flight: null, escala: null, clase: null };

  /** Currently selected return flight/escala and class, or all-null before selection. @type {{type: string|null, flight: object|null, escala: object|null, clase: object|null}} */
  let selectedReturn   = { type: null, flight: null, escala: null, clase: null };

  /** True when the flight detail modal is open. @type {boolean} */
  let showDetailModal = false;

  /** Flight or escala object currently displayed in the detail modal. @type {object|null} */
  let detailFlight    = null;

  /** Flight lists for the outbound direction with directos and conEscala arrays. @type {{directos: object[], conEscala: object[]}} */
  let vuelosIda    = { directos: [], conEscala: [] };

  /** Flight lists for the return direction with directos and conEscala arrays. @type {{directos: object[], conEscala: object[]}} */
  let vuelosVuelta = { directos: [], conEscala: [] };

  /** True while outbound flights are being fetched. @type {boolean} */
  let loadingIda   = false;

  /** True while return flights are being fetched. @type {boolean} */
  let loadingVuelta = false;

  /** Error message for outbound flight fetch failures. @type {string} */
  let errorIda     = '';

  /** Error message for return flight fetch failures. @type {string} */
  let errorVuelta  = '';

  /** True while the reservation creation POST request is in progress. @type {boolean} */
  let creandoReserva = false;

  /** Error message shown if the reservation creation fails. @type {string} */
  let errorReserva   = '';

  /** Active tab for the outbound view, 'directos' or 'escalas'. @type {string} */
  let tabIda    = 'directos';

  /** Active tab for the return view, 'directos' or 'escalas'. @type {string} */
  let tabVuelta = 'directos';

  /**
   * Static seat class options available for selection.
   * @type {Array<{id: number, tipoDeClase: string}>}
   */
  const clases = [
    { id: 1, tipoDeClase: 'Turista' },
    { id: 2, tipoDeClase: 'Ejecutiva' }
  ];

  /** Minimum price filter value string (empty means no minimum). @type {string} */
  let precioMin = '';

  /** Maximum price filter value string (empty means no maximum). @type {string} */
  let precioMax = '';

  /** Selected class ID string for the class filter (empty means all classes). @type {string} */
  let claseSeleccionada = '';

  if (searchParams?.fromGlobalSearch) {
    isGlobalSearch = true;
    globalSearchQuery = searchParams.globalSearchQuery || '';
    newGlobalQuery = globalSearchQuery;
    const vuelos = searchParams.globalSearchResults || [];
    vuelosIda = { directos: vuelos, conEscala: [] };
    tabIda = 'directos';
    searchData.tripType = 'oneway';
  } else if (searchParams?.searchData) {
    searchData   = searchParams.searchData;
    vuelosIda    = searchParams.vuelosIda   ?? { directos: [], conEscala: [] };
    vuelosVuelta = searchParams.vuelosVuelta ?? { directos: [], conEscala: [] };

    const fm = searchData.flightMode ?? 'todos';
    if (fm === 'escalas') {
      tabIda = 'escalas'; tabVuelta = 'escalas';
    } else if (fm === 'directo') {
      tabIda = 'directos'; tabVuelta = 'directos';
    } else {
      tabIda    = (vuelosIda.directos?.length    ?? 0) > 0 ? 'directos' : 'escalas';
      tabVuelta = (vuelosVuelta.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
    }
  } else {
    errorIda = 'No se encontro informacion de busqueda. Realiza una nueva busqueda.';
  }

  // Flight lists for the currently active direction (outbound or return).
  $: currentVuelos    = currentView === 'outbound' ? vuelosIda : vuelosVuelta;

  // Active tab for the currently displayed direction.
  $: currentTab       = currentView === 'outbound' ? tabIda    : tabVuelta;

  // True while the currently displayed direction is loading.
  $: loading          = currentView === 'outbound' ? loadingIda    : loadingVuelta;

  // Error message for the currently displayed direction.
  $: errorActual      = currentView === 'outbound' ? errorIda      : errorVuelta;

  // Direct flight list for the current direction.
  $: listaDirectos    = currentVuelos.directos  ?? [];

  // Escala itinerary list for the current direction.
  $: listaEscalas     = currentVuelos.conEscala ?? [];

  // List rendered in the main content area based on currentTab.
  $: listaActiva      = currentTab === 'directos' ? listaDirectos : listaEscalas;

  // Total number of results (direct + escala) for the current direction.
  $: totalResultados  = listaDirectos.length + listaEscalas.length;

  // True when a flight has been selected for the currently active step.
  $: canProceed       = currentView === 'outbound'
      ? selectedOutbound.type !== null
      : selectedReturn.type   !== null;

  // Filtered direct list (currently passes through unchanged; API returns pre-filtered data).
  $: listaDirectosFiltrada = filtrarSegunMode(listaDirectos);

  // Filtered escala list (currently passes through unchanged).
  $: listaEscalasFiltrada  = filtrarSegunMode(listaEscalas);

  /**
   * Placeholder filter function; the API already filters by flight mode so this returns the
   * list unchanged. Tab visibility is determined by the flightMode value in searchData.
   * @param {object[]} lista - Flight list array to filter.
   * @returns {object[]} The same lista array unchanged.
   */
  function filtrarSegunMode(lista) { return lista; }

  // True when the Directos tab should be shown based on flightMode and available results.
  $: mostrarTabDirectos = (searchData.flightMode === 'todos' || searchData.flightMode === 'directo') && listaDirectos.length > 0;

  // True when the Escalas tab should be shown based on flightMode and available results.
  $: mostrarTabEscalas  = (searchData.flightMode === 'todos' || searchData.flightMode === 'escalas')  && listaEscalas.length  > 0;

  /**
   * Sets the active tab for the current direction to the given value ('directos' or 'escalas').
   * @param {string} tab - The tab identifier to activate.
   */
  function setTab(tab) {
    if (currentView === 'outbound') tabIda = tab;
    else tabVuelta = tab;
  }

  /**
   * Sends a GET request to /api/vuelos/busqueda-general with the newGlobalQuery string and
   * replaces vuelosIda.directos with the results. Updates globalSearchQuery and resets the
   * outbound selection. Requires at least 2 characters to proceed.
   * @async
   * @returns {Promise<void>}
   */
  async function reBuscarGlobal() {
    const q = newGlobalQuery.trim();
    if (q.length < 2) return;
    loadingIda = true; errorIda = '';
    try {
      const res = await fetch(`${API}/api/vuelos/busqueda-general?query=${encodeURIComponent(q)}`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error();
      const vuelos = await res.json();
      vuelosIda = { directos: vuelos, conEscala: [] };
      globalSearchQuery = q;
      tabIda = 'directos';
      selectedOutbound = { type: null, flight: null, escala: null, clase: null };
    } catch {
      errorIda = 'Error al buscar vuelos.';
    } finally {
      loadingIda = false;
    }
  }

  /**
   * Constructs a filter body from precioMin, precioMax, claseSeleccionada, and searchData, then
   * sends a POST request to /api/vuelos/buscar for either the outbound or return direction.
   * Updates the appropriate vuelosIda or vuelosVuelta and auto-selects the first available tab.
   * Does nothing in global search mode.
   * @async
   * @param {boolean} esIda - True to search outbound flights, false to search return flights.
   * @returns {Promise<void>}
   */
  async function buscarVuelos(esIda) {
    if (isGlobalSearch) return;
    const origen  = esIda ? searchData.origenId  : searchData.destinoId;
    const destino = esIda ? searchData.destinoId : searchData.origenId;
    const fecha   = esIda ? searchData.fechaIda  : searchData.fechaVuelta;
    if (!origen || !destino || !fecha) return;

    if (esIda) { loadingIda = true;    errorIda = ''; }
    else       { loadingVuelta = true; errorVuelta = ''; }

    try {
      const body = { origenId: origen, destinoId: destino, fecha, cantidadPasajeros: searchData.pasajeros };
      if (precioMin !== '') body.precioMinimo = parseFloat(precioMin);
      if (precioMax !== '') body.precioMaximo = parseFloat(precioMax);
      if (claseSeleccionada !== '') body.claseId = parseInt(claseSeleccionada);

      const res = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error();
      const data = await res.json();

      if (esIda) {
        vuelosIda = data;
        tabIda = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      } else {
        vuelosVuelta = data;
        tabVuelta = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      }
    } catch {
      if (esIda) errorIda = 'No se pudieron cargar los vuelos de ida.';
      else errorVuelta = 'No se pudieron cargar los vuelos de vuelta.';
    } finally {
      if (esIda) loadingIda = false;
      else loadingVuelta = false;
    }
  }

  /**
   * Applies the current filter values by calling reBuscarGlobal in global mode or buscarVuelos
   * for the current direction in standard mode.
   * @async
   * @returns {Promise<void>}
   */
  async function aplicarFiltros() {
    if (isGlobalSearch) { reBuscarGlobal(); return; }
    await buscarVuelos(currentView === 'outbound');
  }

  /**
   * Clears the price and class filter fields then immediately applies filters to refresh results.
   */
  function limpiarFiltros() {
    precioMin = ''; precioMax = ''; claseSeleccionada = '';
    aplicarFiltros();
  }

  /**
   * Sets the outbound or return selection to a direct flight with the given class.
   * @param {object} vuelo - The direct flight object to select.
   * @param {object} clase - The class object ({ id, tipoDeClase }) to select.
   */
  function selectDirecto(vuelo, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'directo', flight: vuelo, escala: null, clase };
    else selectedReturn = { type: 'directo', flight: vuelo, escala: null, clase };
  }

  /**
   * Returns true if the given direct flight and class combination is currently selected for the
   * active direction.
   * @param {object} vuelo - The direct flight object to check.
   * @param {object} clase - The class object to check.
   * @returns {boolean} Whether this flight+class is the active selection.
   */
  function isSelectedDirecto(vuelo, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.type === 'directo' && s.flight?.id === vuelo.id && s.clase?.id === clase.id;
  }

  /**
   * Sets the outbound or return selection to a layover itinerary with the given class.
   * @param {object} escala - The escala itinerary object to select.
   * @param {object} clase - The class object ({ id, tipoDeClase }) to select.
   */
  function selectEscala(escala, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'escala', flight: null, escala, clase };
    else selectedReturn = { type: 'escala', flight: null, escala, clase };
  }

  /**
   * Returns true if the given escala itinerary and class combination is currently selected for the
   * active direction, matched by the first tramo's ID.
   * @param {object} escala - The escala itinerary object to check.
   * @param {object} clase - The class object to check.
   * @returns {boolean} Whether this escala+class is the active selection.
   */
  function isSelectedEscala(escala, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.type === 'escala' &&
      s.escala?.tramos?.[0]?.id === escala?.tramos?.[0]?.id &&
      s.clase?.id === clase.id;
  }

  /**
   * Opens the flight detail modal by setting detailFlight and showDetailModal.
   * @param {object} vuelo - The flight or escala object to display in the modal.
   */
  function viewDetails(vuelo) { detailFlight = vuelo; showDetailModal = true; }

  /**
   * Closes the flight detail modal by clearing showDetailModal and detailFlight.
   */
  function closeModal()       { showDetailModal = false; detailFlight = null; }

  /**
   * Handles the next-step button. For a round-trip outbound selection, switches currentView to
   * 'return' and fetches return flights if not already loaded. For one-way or return completion,
   * calls crearReserva directly.
   * @async
   * @returns {Promise<void>}
   */
  async function nextStep() {
    errorReserva = '';
    if (currentView === 'outbound' && selectedOutbound.type) {
      if (!isGlobalSearch && searchData.tripType === 'roundtrip') {
        currentView = 'return';
        if ((vuelosVuelta.directos?.length ?? 0) === 0 && (vuelosVuelta.conEscala?.length ?? 0) === 0)
          await buscarVuelos(false);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        await crearReserva();
      }
    } else if (currentView === 'return' && selectedReturn.type) {
      await crearReserva();
    }
  }

  /**
   * Builds the vuelos payload from selectedOutbound and selectedReturn using _agregarVuelos, then
   * posts it to POST /api/reservaciones. On success navigates to 'datos-pasajeros' with the
   * reservation response and searchData. Redirects to login if no session is active. On failure
   * sets errorReserva with the server or connection error message.
   * @async
   * @returns {Promise<void>}
   */
  async function crearReserva() {
    const sesionActual = $sesion;
    if (!sesionActual) { navigateTo('login'); return; }

    creandoReserva = true;
    errorReserva   = '';
    const pasajeros = Number(searchData.pasajeros) || 1;
    const vuelos = [];
    _agregarVuelos(vuelos, selectedOutbound, pasajeros);
    if (selectedReturn.type) _agregarVuelos(vuelos, selectedReturn, pasajeros);

    console.log('Body reserva:', JSON.stringify({ vuelos }, null, 2));
    try {
      const res = await fetch(`${API}/api/reservaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ vuelos })
      });
      const data = await res.json();
      if (!res.ok) { errorReserva = data.message ?? 'Error al crear la reservacion.'; return; }
      navigateTo('datos-pasajeros', { reserva: data, searchData });
    } catch {
      errorReserva = 'Error de conexion al crear la reservacion.';
    } finally {
      creandoReserva = false;
    }
  }

  /**
   * Appends flight entry objects to the arr array from a selection object. For directo selections,
   * adds one entry with vueloId, claseId, and cantidadPasajeros. For escala selections, adds one
   * entry per tramo in the itinerary.
   * @param {Array<object>} arr - The target array to push flight entries into.
   * @param {{type: string, flight: object|null, escala: object|null, clase: object}} sel - Selection object.
   * @param {number} pasajeros - Number of passengers for each entry.
   */
  function _agregarVuelos(arr, sel, pasajeros) {
    if (sel.type === 'directo') {
      arr.push({ vueloId: Number(sel.flight.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    } else if (sel.type === 'escala') {
      for (const tramo of sel.escala.tramos)
        arr.push({ vueloId: Number(tramo.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    }
  }

  /**
   * Navigates back: switches from 'return' to 'outbound' with smooth scroll, or returns to 'home'
   * if already on the outbound step.
   */
  function goBack() {
    if (currentView === 'return') { currentView = 'outbound'; window.scrollTo({ top: 0, behavior: 'smooth' }); }
    else navigateTo('home');
  }

  /**
   * Formats a duration in minutes as a human-readable string such as '2h 30m'.
   * @param {number} min - Duration in minutes.
   * @returns {string} Formatted duration string, or empty string if min is falsy.
   */
  function formatDuracion(min) {
    if (!min) return '';
    return `${Math.floor(min / 60)}h ${min % 60}m`;
  }

  /**
   * Extracts the HH:MM portion from a time string, or returns empty string if falsy.
   * @param {string} h - Time string in HH:MM:SS or HH:MM format.
   * @returns {string} The first 5 characters (HH:MM), or empty string.
   */
  function formatHora(h) { return h ? h.substring(0, 5) : ''; }

  /**
   * Formats a date string using Spanish (Guatemala) locale with day, month abbreviation, and year.
   * Falls back to the original string on parse error.
   * @param {string} f - ISO date string or date-compatible string.
   * @returns {string} Localized date string, or empty string if f is falsy.
   */
  function formatFecha(f) {
    if (!f) return '';
    try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }); }
    catch { return f; }
  }

  /**
   * Returns the price for a direct flight given a class ID (1=Turista, 2=Ejecutiva), or null.
   * @param {object} vuelo - Direct flight object with precioTurista and precioEjecutiva properties.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number|null} The price, or null if class ID is unrecognized.
   */
  function getPrecioDirecto(vuelo, claseId) {
    return claseId === 1 ? vuelo.precioTurista : claseId === 2 ? vuelo.precioEjecutiva : null;
  }

  /**
   * Returns the available ticket count for a direct flight given a class ID.
   * @param {object} vuelo - Direct flight object with boletosDisponiblesTurista and boletosDisponiblesEjecutiva.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number} Available ticket count, or 0 if class ID is unrecognized.
   */
  function getBoletosDirecto(vuelo, claseId) {
    return claseId === 1 ? (vuelo.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (vuelo.boletosDisponiblesEjecutiva ?? 0) : 0;
  }

  /**
   * Returns the total price for a layover itinerary given a class ID.
   * @param {object} escala - Escala itinerary object with precioTuristaTotal and precioEjecutivaTotal.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number|null} The total itinerary price, or null if class ID is unrecognized.
   */
  function getPrecioEscala(escala, claseId) {
    return claseId === 1 ? escala.precioTuristaTotal : claseId === 2 ? escala.precioEjecutivaTotal : null;
  }

  /**
   * Returns the minimum available ticket count across all tramos for a layover itinerary and class.
   * @param {object} escala - Escala itinerary object with boletosDisponiblesTurista and boletosDisponiblesEjecutiva.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number} Available ticket count, or 0 if class ID is unrecognized.
   */
  function getBoletosEscala(escala, claseId) {
    return claseId === 1 ? (escala.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (escala.boletosDisponiblesEjecutiva ?? 0) : 0;
  }

  /**
   * Formats a numeric price as a USD string with two decimal places, e.g. '$ 1,234.00'.
   * Returns 'No disponible' if the value is falsy.
   * @param {number} p - Price value to format.
   * @returns {string} Formatted price string.
   */
  function formatPrecio(p) {
    if (!p) return 'No disponible';
    return `$ ${p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  /**
   * Returns a human-readable layover count label such as '1 escala' or '2 escalas'.
   * @param {number} n - Number of layovers.
   * @returns {string} Label string.
   */
  function labelEscala(n) { return n === 1 ? '1 escala' : `${n} escalas`; }
</script>

<!-- Modal de detalle de vuelo, montado sobre la pagina cuando el usuario solicita ver mas informacion -->
{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<!-- Contenedor principal de la pagina de listado y seleccion de vuelos -->
<div class="vuelos-page">
  <div class="vuelos-page__container">

    <!-- Cabecera con boton de regreso, resumen de busqueda e indicador de pasos ida/vuelta -->
    <div class="vuelos-page__header">
      <button class="vuelos-page__back" on:click={goBack}>Volver</button>

      {#if isGlobalSearch}
        <!-- Resumen de busqueda global con campo de re-busqueda en tiempo real -->
        <div class="search-summary">
          <div class="search-summary__route">
            <span class="search-summary__origin">Resultados para</span>
            <span class="search-summary__destination">"{globalSearchQuery}"</span>
          </div>
          <div class="search-summary__details">
            <div class="search-summary__item">
              <span class="search-summary__label">Vuelos encontrados</span>
              <span class="search-summary__value">{listaDirectos.length}</span>
            </div>
          </div>
          <div style="display:flex; gap:0.5rem; margin-top:1rem;">
            <input type="text" bind:value={newGlobalQuery}
              on:keydown={e => e.key === 'Enter' && reBuscarGlobal()}
              placeholder="Buscar otra vez..."
              style="flex:1; padding:0.6rem 1rem; border:1px solid #B89A7A; border-radius:20px; font-size:0.85rem; font-family:inherit; outline:none;"
            >
            <button on:click={reBuscarGlobal}
              style="padding:0.6rem 1.2rem; background:#1C1A18; color:#fff; border:none; border-radius:20px; cursor:pointer; font-family:inherit; font-size:0.8rem; font-weight:600;">
              Buscar
            </button>
          </div>
        </div>
      {:else}
        <!-- Resumen de busqueda estandar: ruta, fechas, pasajeros y tipo de vuelo seleccionado -->
        <div class="search-summary">
          <div class="search-summary__route">
            <span class="search-summary__origin">{searchData.origenNombre || 'Origen'}</span>
            <span class="search-summary__arrow">→</span>
            <span class="search-summary__destination">{searchData.destinoNombre || 'Destino'}</span>
          </div>
          <div class="search-summary__details">
            <div class="search-summary__item">
              <span class="search-summary__label">Salida</span>
              <span class="search-summary__value">{searchData.fechaIda || '—'}</span>
            </div>
            {#if searchData.tripType === 'roundtrip'}
              <div class="search-summary__item">
                <span class="search-summary__label">Regreso</span>
                <span class="search-summary__value">{searchData.fechaVuelta || '—'}</span>
              </div>
            {/if}
            <div class="search-summary__item">
              <span class="search-summary__label">Pasajeros</span>
              <span class="search-summary__value">{searchData.pasajeros}</span>
            </div>
            {#if searchData.flightMode && searchData.flightMode !== 'todos'}
              <div class="search-summary__item">
                <span class="search-summary__label">Tipo</span>
                <span class="search-summary__value search-summary__value--mode">
                  {searchData.flightMode === 'directo' ? '✈ Directo' : '⇌ Con escalas'}
                </span>
              </div>
            {/if}
          </div>
        </div>

        <!-- Indicador de pasos del flujo de seleccion: vuelo de ida y vuelo de vuelta -->
        <div class="step-indicator">
          <div class="step-indicator__item"
            class:step-indicator__item--active={currentView === 'outbound'}
            class:step-indicator__item--completed={selectedOutbound.type}>
            <span class="step-indicator__number">1</span>
            <span class="step-indicator__label">Vuelo de Ida</span>
          </div>
          {#if searchData.tripType === 'roundtrip'}
            <div class="step-indicator__line"></div>
            <div class="step-indicator__item"
              class:step-indicator__item--active={currentView === 'return'}
              class:step-indicator__item--completed={selectedReturn.type}>
              <span class="step-indicator__number">2</span>
              <span class="step-indicator__label">Vuelo de Vuelta</span>
            </div>
          {/if}
        </div>
      {/if}
    </div>

    <div class="vuelos-page__content" style={isGlobalSearch ? 'grid-template-columns: 1fr;' : ''}>

      <!-- Panel lateral de filtros por tipo de vuelo, precio y clase (oculto en busqueda global) -->
      {#if !isGlobalSearch}
        <aside class="vuelos-page__filters">
          <div class="filters-panel">
            <div class="filters-panel__header">
              <h3 class="filters-panel__title">Filtros</h3>
              <button class="filters-panel__clear" on:click={limpiarFiltros}>Limpiar</button>
            </div>

            <!-- Selector de tab: vuelos directos vs vuelos con escalas -->
            <div class="filter-group">
              <span class="filter-group__label">Tipo de vuelo</span>
              <div class="filter-flight-mode">
                <button
                  class="filter-mode-btn"
                  class:filter-mode-btn--active={currentTab === 'directos'}
                  on:click={() => setTab('directos')}
                  disabled={listaDirectos.length === 0}
                >
                  <span class="filter-mode-btn__icon">✈</span>
                  <span class="filter-mode-btn__text">Directo</span>
                  {#if listaDirectos.length > 0}
                    <span class="filter-mode-btn__count">{listaDirectos.length}</span>
                  {/if}
                </button>
                <button
                  class="filter-mode-btn filter-mode-btn--escala"
                  class:filter-mode-btn--active={currentTab === 'escalas'}
                  on:click={() => setTab('escalas')}
                  disabled={listaEscalas.length === 0}
                >
                  <span class="filter-mode-btn__icon">⇌</span>
                  <span class="filter-mode-btn__text">Con escalas</span>
                  {#if listaEscalas.length > 0}
                    <span class="filter-mode-btn__count">{listaEscalas.length}</span>
                  {/if}
                </button>
              </div>
            </div>

            <!-- Filtro de rango de precio minimo y maximo en USD -->
            <div class="filter-group">
              <label class="filter-group__label" for="precioMin">Rango de Precio (USD)</label>
              <div class="filter-group__price-range">
                <input id="precioMin" type="number" class="filter-group__input" placeholder="Min" bind:value={precioMin} />
                <span>-</span>
                <input type="number" class="filter-group__input" placeholder="Max" bind:value={precioMax} />
              </div>
            </div>

            <!-- Filtro de clase de asiento (Turista / Ejecutiva) -->
            <div class="filter-group">
              <label class="filter-group__label" for="filtroClase">Clase</label>
              <div class="filter-group__select">
                <select id="filtroClase" class="filter-group__select-element" bind:value={claseSeleccionada}>
                  <option value="">Todas</option>
                  {#each clases as c}
                    <option value={c.id}>{c.tipoDeClase}</option>
                  {/each}
                </select>
              </div>
            </div>

            <button class="filters-panel__apply" on:click={aplicarFiltros}>Aplicar Filtros</button>
          </div>
        </aside>
      {/if}

      <!-- Area principal con titulo, contador de resultados y lista de vuelos -->
      <div class="vuelos-page__main">

        <!-- Encabezado de la lista con titulo dinamico y conteo de opciones disponibles -->
        <div class="flights-header">
          <h2 class="flights-header__title">
            {#if isGlobalSearch}
              Vuelos disponibles
            {:else}
              {currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}
            {/if}
          </h2>
          <p class="flights-header__subtitle">
            {#if loading}
              Buscando vuelos...
            {:else if isGlobalSearch}
              {totalResultados} vuelos encontrados para "{globalSearchQuery}"
            {:else}
              {totalResultados} opciones — mostrando {currentTab === 'directos' ? 'vuelos directos' : 'vuelos con escalas'}
            {/if}
          </p>
        </div>

        <!-- Estado de carga, error o sin resultados mientras se procesa la busqueda -->
        {#if loading}
          <div class="vuelos-estado">Buscando vuelos...</div>

        {:else if errorActual}
          <div class="vuelos-estado vuelos-estado--info">{errorActual}</div>

        {:else if totalResultados === 0}
          <div class="vuelos-estado">
            {#if isGlobalSearch}
              No se encontraron vuelos para "{globalSearchQuery}". Intenta con otra busqueda.
            {:else}
              No hay vuelos disponibles para esta ruta y fecha.
            {/if}
          </div>

        {:else if currentTab === 'directos'}

          <!-- Grilla de tarjetas de vuelos directos con horario, duracion y opciones de clase -->
          {#if listaDirectos.length === 0}
            <div class="vuelos-estado">No hay vuelos directos. Cambia a "Con escalas" en el panel de filtros.</div>
          {:else}
            <div class="flights-list">
              {#each listaDirectos as vuelo}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'directo' && selObj.flight?.id === vuelo.id}

                <div class="flight-card" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#c9a96e" stroke-width="0.6"/>
                    <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#c9a96e" opacity="0.18"/>
                    <line x1="60" y1="125" x2="200" y2="125" stroke="#c9a96e" stroke-width="0.5" stroke-dasharray="4 7"/>
                    <line x1="80" y1="135" x2="200" y2="135" stroke="#c9a96e" stroke-width="0.4" stroke-dasharray="3 8"/>
                  </svg>

                  <div class="flight-card__content">
                    <div class="flight-card__header">
                      <div class="flight-card__code-info">
                        <span class="flight-card__code">{vuelo.numeroVuelo || 'N/A'}</span>
                        <span class="flight-card__airline">{vuelo.avionMarca || ''} {vuelo.avionModelo || ''}</span>
                      </div>
                      {#if isGlobalSearch && vuelo.fecha}
                        <span class="flight-card__badge flight-card__badge--directo">{formatFecha(vuelo.fecha)}</span>
                      {:else}
                        <span class="flight-card__badge flight-card__badge--directo">Directo</span>
                      {/if}
                    </div>

                    <!-- Linea de horario: hora de salida, duracion del vuelo y hora de llegada -->
                    <div class="flight-card__schedule">
                      <div class="schedule-point">
                        <span class="schedule-point__time">{formatHora(vuelo.horaSalida)}</span>
                        <span class="schedule-point__code">{vuelo.origenCodigo || ''}</span>
                      </div>
                      <div class="schedule-duration">
                        <div class="schedule-duration__track">
                          <div class="schedule-duration__dot"></div>
                          <div class="schedule-duration__line"></div>
                          <span class="schedule-duration__plane">✈</span>
                          <div class="schedule-duration__line"></div>
                          <div class="schedule-duration__dot"></div>
                        </div>
                        <span class="schedule-duration__time">{formatDuracion(vuelo.duracionMinutos)}</span>
                        {#if isGlobalSearch}
                          <span class="schedule-duration__type">{vuelo.origenCiudad || ''} → {vuelo.destinoCiudad || ''}</span>
                        {:else}
                          <span class="schedule-duration__type">Directo</span>
                        {/if}
                      </div>
                      <div class="schedule-point schedule-point--right">
                        <span class="schedule-point__time">{formatHora(vuelo.horaLlegada)}</span>
                        <span class="schedule-point__code">{vuelo.destinoCodigo || ''}</span>
                      </div>
                    </div>

                    <!-- Botones de seleccion de clase con precio y disponibilidad por tipo -->
                    <div class="flight-card__class-selection">
                      {#each clases as clase}
                        {@const precio     = getPrecioDirecto(vuelo, clase.id)}
                        {@const boletos    = getBoletosDirecto(vuelo, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= (isGlobalSearch ? 1 : searchData.pasajeros)}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedDirecto(vuelo, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectDirecto(vuelo, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedDirecto(vuelo, clase) ? 'Seleccionado ✓' : 'por persona'}</span>
                          {:else}
                            <span class="class-option__label class-option__label--unavailable">No disponible</span>
                          {/if}
                        </button>
                      {/each}
                    </div>

                    <button class="flight-card__details-btn" on:click={() => viewDetails(vuelo)}>
                      Ver Detalles
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {/if}

        {:else}
          <!-- Grilla de tarjetas de vuelos con escalas, con timeline de tramos y conexiones -->
          {#if listaEscalas.length === 0}
            <div class="vuelos-estado">No hay vuelos con escalas.</div>
          {:else}
            <div class="flights-list">
              {#each listaEscalas as escala}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'escala' && selObj.escala?.tramos?.[0]?.id === escala?.tramos?.[0]?.id}

                <div class="flight-card flight-card--escala" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#3b4f6b" stroke-width="0.6"/>
                    <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#3b4f6b" opacity="0.18"/>
                    <line x1="60" y1="125" x2="200" y2="125" stroke="#3b4f6b" stroke-width="0.5" stroke-dasharray="4 7"/>
                  </svg>

                  <div class="flight-card__content">
                    <div class="flight-card__header">
                      <div class="flight-card__code-info">
                        <span class="flight-card__code">
                          {escala.tramos[0]?.origenCodigo} → {escala.tramos[escala.tramos.length - 1]?.destinoCodigo}
                        </span>
                        <span class="flight-card__airline">
                          {escala.tramos.map(t => t.numeroVuelo).join(' · ')}
                        </span>
                      </div>
                      <span class="flight-card__badge flight-card__badge--escala">
                        {labelEscala(escala.numeroEscalas)}
                      </span>
                    </div>

                    <!-- Timeline de tramos con aeropuerto de origen, conexion y aeropuerto de destino por segmento -->
                    <div class="escala-timeline">
                      {#each escala.tramos as tramo, ti}
                        <div class="escala-tramo">
                          <div class="escala-tramo__airports">
                            <div class="escala-tramo__point">
                              <span class="escala-tramo__time">{formatHora(tramo.horaSalida)}</span>
                              <span class="escala-tramo__code">{tramo.origenCodigo}</span>
                              <span class="escala-tramo__city">{tramo.origenCiudad}</span>
                            </div>
                            <div class="escala-tramo__middle">
                              <div class="escala-tramo__track">
                                <div class="escala-tramo__dot"></div>
                                <div class="escala-tramo__line"></div>
                                <span class="escala-tramo__plane">✈</span>
                                <div class="escala-tramo__line"></div>
                                <div class="escala-tramo__dot"></div>
                              </div>
                              <span class="escala-tramo__dur">{formatDuracion(tramo.duracionMinutos)}</span>
                              <span class="escala-tramo__num">{tramo.numeroVuelo}</span>
                            </div>
                            <div class="escala-tramo__point escala-tramo__point--right">
                              <span class="escala-tramo__time">{formatHora(tramo.horaLlegada)}</span>
                              <span class="escala-tramo__code">{tramo.destinoCodigo}</span>
                              <span class="escala-tramo__city">{tramo.destinoCiudad}</span>
                            </div>
                          </div>
                        </div>

                        <!-- Insignia de conexion con tiempo de espera entre tramos consecutivos -->
                        {#if ti < escala.tramos.length - 1}
                            {@const llegada = new Date(`1970-01-01T${escala.tramos[ti].horaLlegada}`)}
                            {@const salida  = new Date(`1970-01-01T${escala.tramos[ti + 1].horaSalida}`)}
                            {@const minutos = ((salida - llegada) / 60000 + 1440) % 1440}
                            <div class="escala-conexion">
                              <div class="escala-conexion__line"></div>
                              <div class="escala-conexion__badge">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                  <circle cx="12" cy="12" r="4"/>
                                </svg>
                                Escala · {formatDuracion(minutos)} en {escala.tramos[ti].destinoCiudad}
                              </div>
                              <div class="escala-conexion__line"></div>
                            </div>
                          {/if}
                      {/each}
                    </div>

                    <!-- Resumen de duracion total y numero de escalas del itinerario -->
                    <div class="escala-resumen">
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Duracion total</span>
                        <span class="escala-resumen__value">{formatDuracion(escala.duracionTotalMinutos)}</span>
                      </div>
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Escalas</span>
                        <span class="escala-resumen__value">{escala.numeroEscalas}</span>
                      </div>
                    </div>

                    <!-- Botones de seleccion de clase con precio total y disponibilidad para vuelos con escalas -->
                    <div class="flight-card__class-selection">
                      {#each clases as clase}
                        {@const precio     = getPrecioEscala(escala, clase.id)}
                        {@const boletos    = getBoletosEscala(escala, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= searchData.pasajeros}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedEscala(escala, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectEscala(escala, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedEscala(escala, clase) ? 'Seleccionado ✓' : 'precio total'}</span>
                          {:else}
                            <span class="class-option__label class-option__label--unavailable">No disponible</span>
                          {/if}
                        </button>
                      {/each}
                    </div>

                    <button class="flight-card__details-btn" on:click={() => viewDetails(escala)}>
                      Ver Detalles de la Ruta
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        {/if}
      </div>
    </div>

    <!-- Mensaje de error al intentar crear la reservacion -->
    {#if errorReserva}
      <div class="vuelos-error-reserva">{errorReserva}</div>
    {/if}

    <!-- Barra fija de accion con boton para avanzar al siguiente paso del flujo de compra -->
    {#if canProceed}
      <div class="vuelos-page__next-step">
        <button class="next-step-btn" on:click={nextStep} disabled={creandoReserva}>
          {#if creandoReserva}
            Creando reservacion...
          {:else if !isGlobalSearch && currentView === 'outbound' && searchData.tripType === 'roundtrip'}
            Seleccionar Vuelo de Vuelta
          {:else}
            Siguiente Paso
          {/if}
        </button>
      </div>
    {/if}

  </div>
</div>
