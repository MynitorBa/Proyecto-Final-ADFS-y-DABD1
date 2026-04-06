<script>
/**
 * @file Home.svelte
 * @description Main landing page of the Broom AirLine application. Renders a hero banner,
 * a flight search form with origin/destination autocomplete, passenger count selector,
 * dual interactive calendars that highlight available flight dates fetched from the API,
 * and a grid of featured destinations sourced from airports that have a stored image.
 * Submits a flight search to the API and navigates to the Vuelos page with the results.
 */
  import '../styles/home.css';
  import logoHero from '../assets/BroomHero1.png';
  import { onMount } from 'svelte';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Optional airport object pre-filled as the destination when arriving from a featured destination card or the flying plane animation. @type {object|null} */
  export let suggestedAeropuerto = null;

  import { API } from '../lib/api.js';

  /** Trip type selection: 'roundtrip' for round trip or 'oneway' for one way. @type {string} */
  let tripType      = 'roundtrip';

  /** Selected departure date in YYYY-MM-DD format. @type {string} */
  let departureDate = '';

  /** Selected return date in YYYY-MM-DD format, only used when tripType is 'roundtrip'. @type {string} */
  let returnDate    = '';

  /** Number of passengers selected by the user (1-9). @type {number} */
  let passengers    = 1;

  /** Full list of airport objects fetched from the API on mount. @type {Array<object>} */
  let aeropuertos        = [];

  /** True while the airport list is being fetched from the API. @type {boolean} */
  let loadingAeropuertos = true;

  /** Current text value in the origin autocomplete input. @type {string} */
  let fromQuery          = '';

  /** Filtered list of airports matching the origin query, limited to 5 results. @type {Array<object>} */
  let fromSugeridos      = [];

  /** The airport object selected as the origin, or null if not yet chosen. @type {object|null} */
  let fromSeleccionado   = null;

  /** Current text value in the destination autocomplete input. @type {string} */
  let toQuery            = '';

  /** Filtered list of airports matching the destination query, limited to 5 results. @type {Array<object>} */
  let toSugeridos        = [];

  /** The airport object selected as the destination, or null if not yet chosen. @type {object|null} */
  let toSeleccionado     = null;

  /** Array of date strings (YYYY-MM-DD) with outbound flights available for the selected route. @type {Array<string>} */
  let fechasDisponiblesIda    = [];

  /** Array of date strings (YYYY-MM-DD) with return flights available for the inverse route. @type {Array<string>} */
  let fechasDisponiblesVuelta = [];

  /** True while available flight dates are being fetched from the API. @type {boolean} */
  let loadingFechas      = false;

  /** True after both origin and destination are selected and dates have been fetched. @type {boolean} */
  let mostrarCalendarios = false;

  /** Date object representing the currently displayed month in the outbound calendar. @type {Date} */
  let mesIda    = new Date();

  /** Date object representing the currently displayed month in the return calendar. @type {Date} */
  let mesVuelta = new Date();

  /** Validation or search error message shown below the search form. @type {string} */
  let searchError = '';

  /** True while the flight search POST request is in progress, disables the submit button. @type {boolean} */
  let buscando    = false;

  /** Abbreviated weekday labels used as column headers in the calendar grid. @type {Array<string>} */
  const diasSemana  = ['LU','MA','MI','JU','VI','SA','DO'];

  /** Full month name list used to build calendar titles. @type {Array<string>} */
  const mesesNombre = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  // Airports that have a base64 image stored, used to populate the featured destinations grid.
  $: destinosConImagen = aeropuertos.filter(a => a.imagenBase64);

  onMount(async () => {
    try {
      const res = await fetch(`${API}/api/aeropuertos`);
      aeropuertos = await res.json();
    } catch (err) {
      console.error('Error cargando aeropuertos:', err);
    } finally {
      loadingAeropuertos = false;
    }
  });

  // When a suggested airport arrives (e.g. from a flying plane animation), auto-select it as the destination
  // and briefly highlight the destination input field with a CSS class.
  $: if (suggestedAeropuerto) {
    /** @type {any} */
    const ap = suggestedAeropuerto;
    if (ap.id) {
      toSeleccionado = ap;
      toQuery = `${ap.ciudad} (${ap.codigo})`;
      toSugeridos = [];

      setTimeout(() => {
        const f = document.getElementById('toCity');
        if (f) {
          f.classList.add('broom-home__form-input--highlighted');
          setTimeout(() => f.classList.remove('broom-home__form-input--highlighted'), 2000);
        }
      }, 50);
    }
  }

  // Automatically reload available dates whenever passengers, origin, or destination change.
  $: if (passengers && fromSeleccionado && toSeleccionado) cargarFechasDisponibles();

  /**
   * Filters the airport list based on the current fromQuery text and updates fromSugeridos.
   * Clears the selected origin and resets calendars if the user edits the field after a selection.
   */
  function onFromInput() {
    const q = fromQuery.toLowerCase();
    fromSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      a.ciudad.toLowerCase().includes(q) ||
      a.nombre.toLowerCase().includes(q) ||
      a.codigo.toLowerCase().includes(q)
    ).slice(0, 5);
    if (fromSeleccionado && fromQuery !== `${fromSeleccionado.ciudad} (${fromSeleccionado.codigo})`) {
      fromSeleccionado = null; resetCalendarios();
    }
  }

  /**
   * Confirms an airport as the selected origin, fills the input text, clears the suggestion
   * dropdown, and triggers a date availability reload.
   * @param {object} a - The airport object chosen from the autocomplete list.
   */
  function seleccionarOrigen(a) {
    fromSeleccionado = a;
    fromQuery = `${a.ciudad} (${a.codigo})`;
    fromSugeridos = [];
    cargarFechasDisponibles();
  }

  /**
   * Filters the airport list based on the current toQuery text and updates toSugeridos,
   * excluding the currently selected origin airport from suggestions.
   * Clears the selected destination and resets calendars if the user edits the field.
   */
  function onToInput() {
    const q = toQuery.toLowerCase();
    toSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      (a.ciudad.toLowerCase().includes(q) ||
       a.nombre.toLowerCase().includes(q) ||
       a.codigo.toLowerCase().includes(q)) &&
      a.id !== fromSeleccionado?.id
    ).slice(0, 5);
    if (toSeleccionado && toQuery !== `${toSeleccionado.ciudad} (${toSeleccionado.codigo})`) {
      toSeleccionado = null; resetCalendarios();
    }
  }

  /**
   * Confirms an airport as the selected destination, fills the input text, clears the
   * suggestion dropdown, and triggers a date availability reload.
   * @param {object} a - The airport object chosen from the autocomplete list.
   */
  function seleccionarDestino(a) {
    toSeleccionado = a;
    toQuery = `${a.ciudad} (${a.codigo})`;
    toSugeridos = [];
    cargarFechasDisponibles();
  }

  /**
   * Resets all calendar-related state: clears available date arrays, clears selected dates,
   * and hides the calendar panels.
   */
  function resetCalendarios() {
    fechasDisponiblesIda = []; fechasDisponiblesVuelta = [];
    departureDate = ''; returnDate = '';
    mostrarCalendarios = false;
  }

  /**
   * Fetches available flight dates for both the outbound (origin→destination) and return
   * (destination→origin) directions in parallel using the aeropuertos/fechas-disponibles endpoint.
   * Parses the response dates and sets the calendar starting month to the first available date.
   * Shows the calendar panels once data is loaded.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarFechasDisponibles() {
    if (!fromSeleccionado || !toSeleccionado) return;
    loadingFechas = true;
    resetCalendarios();
    try {
      const [resIda, resVuelta] = await Promise.all([
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${fromSeleccionado.id}&destinoId=${toSeleccionado.id}&cantidadPersonas=${passengers}`),
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${toSeleccionado.id}&destinoId=${fromSeleccionado.id}&cantidadPersonas=${passengers}`)
      ]);
      const dataIda    = await resIda.json();
      const dataVuelta = await resVuelta.json();
      fechasDisponiblesIda    = dataIda.map(f => f.split('T')[0]);
      fechasDisponiblesVuelta = dataVuelta.map(f => f.split('T')[0]);

      const priIda = fechasDisponiblesIda[0];
      mesIda = priIda
        ? (() => { const d = new Date(priIda + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(new Date().getFullYear(), new Date().getMonth(), 1);

      const priVuelta = fechasDisponiblesVuelta[0];
      mesVuelta = priVuelta
        ? (() => { const d = new Date(priVuelta + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(mesIda.getFullYear(), mesIda.getMonth() + 1, 1);

      mostrarCalendarios = true;
    } catch (err) {
      console.error('Error cargando fechas:', err);
    } finally {
      loadingFechas = false;
    }
  }

  /**
   * Returns true if the given YYYY-MM-DD date string is in the outbound available dates list.
   * @param {string} f - Date string to check.
   * @returns {boolean} True if the date has available outbound flights.
   */
  function esFechaDisponibleIda(f)    { return fechasDisponiblesIda.includes(f); }

  /**
   * Returns true if the given YYYY-MM-DD date string is in the return available dates list.
   * @param {string} f - Date string to check.
   * @returns {boolean} True if the date has available return flights.
   */
  function esFechaDisponibleVuelta(f) { return fechasDisponiblesVuelta.includes(f); }

  /**
   * Builds the array of day cells for a given month, prepending null placeholders for
   * the weekday offset so the first day of the month falls in the correct column.
   * @param {Date} fecha - A Date object set to any day within the target month.
   * @returns {Array<null|{dia: number, fecha: string}>} Array of null or day descriptor objects.
   */
  function getDias(fecha) {
    const y = fecha.getFullYear(), m = fecha.getMonth();
    let ini = new Date(y, m, 1).getDay() - 1;
    if (ini < 0) ini = 6;
    const dias = [];
    for (let i = 0; i < ini; i++) dias.push(null);
    const total = new Date(y, m + 1, 0).getDate();
    for (let d = 1; d <= total; d++)
      dias.push({ dia: d, fecha: `${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}` });
    return dias;
  }

  // Day cell arrays for the outbound and return calendars, rebuilt when the displayed month changes.
  $: diasIda    = getDias(mesIda);
  $: diasVuelta = getDias(mesVuelta);

  // Title string shown above the outbound calendar, e.g. 'Abril 2026'.
  $: titIda     = `${mesesNombre[mesIda.getMonth()]} ${mesIda.getFullYear()}`;

  // Title string shown above the return calendar.
  $: titVuelta  = `${mesesNombre[mesVuelta.getMonth()]} ${mesVuelta.getFullYear()}`;

  /** Navigates the outbound calendar one month backward. */
  function prevIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    - 1, 1); }

  /** Navigates the outbound calendar one month forward. */
  function nextIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    + 1, 1); }

  /** Navigates the return calendar one month backward. */
  function prevVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() - 1, 1); }

  /** Navigates the return calendar one month forward. */
  function nextVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() + 1, 1); }

  /**
   * Selects an outbound flight date if it is available; clears any existing search error.
   * Does nothing if the clicked date is not in the available list.
   * @param {string} f - Date string in YYYY-MM-DD format.
   */
  function pickIda(f) {
    if (!esFechaDisponibleIda(f)) return;
    departureDate = f; searchError = '';
  }

  /**
   * Selects a return flight date if it is available and not earlier than the departure date.
   * Does nothing if the date is blocked or unavailable.
   * @param {string} f - Date string in YYYY-MM-DD format.
   */
  function pickVuelta(f) {
    if (departureDate && f < departureDate) return;
    if (!esFechaDisponibleVuelta(f)) return;
    returnDate = f; searchError = '';
  }

  /**
   * Validates the search form fields, then posts the search to /api/vuelos/buscar for both
   * outbound and (if roundtrip) return legs in sequence. On success, navigates to the 'vuelos'
   * page passing the results and all search parameters. Sets searchError on validation failures
   * or API errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleSearchFlight() {
    searchError = '';
    if (!fromSeleccionado) { searchError = 'Selecciona el aeropuerto de origen.';  return; }
    if (!toSeleccionado)   { searchError = 'Selecciona el aeropuerto de destino.'; return; }
    if (!departureDate)    { searchError = 'Selecciona la fecha de ida.';           return; }
    if (tripType === 'roundtrip' && !returnDate) { searchError = 'Selecciona la fecha de regreso.'; return; }

    buscando = true;
    try {
      const resIda = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origenId: fromSeleccionado.id, destinoId: toSeleccionado.id,
          fecha: departureDate, cantidadPasajeros: passengers
        })
      });
      if (!resIda.ok) throw new Error();
      const vuelosIda = await resIda.json();

      let vuelosVuelta = { directos: [], conEscala: [] };
      if (tripType === 'roundtrip' && returnDate) {
        const resVuelta = await fetch(`${API}/api/vuelos/buscar`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            origenId: toSeleccionado.id, destinoId: fromSeleccionado.id,
            fecha: returnDate, cantidadPasajeros: passengers
          })
        });
        if (resVuelta.ok) vuelosVuelta = await resVuelta.json();
      }

      navigateTo('vuelos', {
        vuelosIda, vuelosVuelta,
        searchData: {
          origenId:      fromSeleccionado.id,
          destinoId:     toSeleccionado.id,
          origenNombre:  fromSeleccionado.ciudad,
          destinoNombre: toSeleccionado.ciudad,
          origenCodigo:  fromSeleccionado.codigo,
          destinoCodigo: toSeleccionado.codigo,
          fechaIda:      departureDate,
          fechaVuelta:   returnDate || '',
          pasajeros:     passengers,
          tripType
        }
      });
    } catch (err) {
      console.error('Error en busqueda:', err);
      searchError = 'Error al buscar vuelos. Intenta nuevamente.';
    } finally {
      buscando = false;
    }
  }
</script>

<div class="broom-home">

  <!-- Banner hero con imagen de fondo y mensaje principal de la aerolinea -->
  <section class="broom-home__hero">
    <img src={logoHero} alt="Broom AirLine Hero">
    <div class="broom-home__hero-overlay">
      <h1 class="broom-home__hero-title">Vuela a donde tus suenos te lleven</h1>
      <p class="broom-home__hero-subtitle">Descubre el mundo con Broom AirLine</p>
    </div>
  </section>

  <!-- Formulario de busqueda de vuelos con selector de tipo de viaje, origen, destino y pasajeros -->
  <section class="broom-home__search-section">
    <div class="broom-home__search-container">
      <h2 class="broom-home__search-title">Encuentra tu vuelo</h2>

      <form class="broom-home__search-form" on:submit|preventDefault={handleSearchFlight}>

        <div class="broom-home__trip-type">
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="roundtrip" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Ida y vuelta</span>
          </label>
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="oneway" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Solo ida</span>
          </label>
        </div>

        <div class="broom-home__form-grid">

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="fromCity" class="broom-home__form-label">Desde</label>
            <input type="text" id="fromCity" bind:value={fromQuery} on:input={onFromInput}
              placeholder={loadingAeropuertos ? 'Cargando...' : 'Ciudad de origen'}
              class="broom-home__form-input" autocomplete="off" />
            {#if fromSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each fromSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarOrigen(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="toCity" class="broom-home__form-label">Hacia</label>
            <input type="text" id="toCity" bind:value={toQuery} on:input={onToInput}
              placeholder="Ciudad de destino"
              class="broom-home__form-input" autocomplete="off" />
            {#if toSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each toSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarDestino(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group">
            <label for="passengers" class="broom-home__form-label">Pasajeros</label>
            <select id="passengers" bind:value={passengers} class="broom-home__form-input broom-home__form-select">
              {#each Array(9) as _, i}
                <option value={i + 1}>{i + 1} {i === 0 ? 'Pasajero' : 'Pasajeros'}</option>
              {/each}
            </select>
          </div>

          <div class="broom-home__form-group broom-home__form-group--btn">
            <div class="broom-home__form-label broom-home__form-label--hidden" aria-hidden="true">·</div>
            <button type="submit" class="broom-home__search-btn" disabled={buscando}>
              <svg class="broom-home__search-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              {buscando ? 'Buscando...' : 'Buscar vuelo'}
            </button>
          </div>

        </div>

        <!-- Calendarios duales con fechas disponibles resaltadas para ida y regreso -->
        {#if loadingFechas}
          <div class="cal-loading">Cargando disponibilidad de vuelos...</div>
        {:else if mostrarCalendarios}
          <div class="cal-wrapper">
            <div class="cal-header-info">
              {#if fechasDisponiblesIda.length > 0 || fechasDisponiblesVuelta.length > 0}
                <span class="cal-info-text">✈ Dias con vuelo estan marcados — selecciona tu fecha</span>
              {:else}
                <span class="cal-info-text cal-info-text--empty">
                  No hay vuelos disponibles en esta ruta para {passengers} {passengers === 1 ? 'pasajero' : 'pasajeros'}
                </span>
              {/if}
            </div>

            <div class="cal-dual" class:cal-dual--single={tripType === 'oneway'}>

              <div class="cal-container">
                <div class="cal-label">✈ Fecha de ida</div>
                <div class="cal-nav">
                  <button type="button" class="cal-nav__btn" on:click={prevIda}>‹</button>
                  <span class="cal-nav__title">{titIda}</span>
                  <button type="button" class="cal-nav__btn" on:click={nextIda}>›</button>
                </div>
                <div class="cal-grid cal-grid--header">
                  {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                </div>
                <div class="cal-grid">
                  {#each diasIda as item}
                    {#if item === null}
                      <span class="cal-day cal-day--empty"></span>
                    {:else}
                      {@const disp = esFechaDisponibleIda(item.fecha)}
                      {@const sel  = departureDate === item.fecha}
                      <button type="button" class="cal-day"
                        class:cal-day--disponible={disp && !sel}
                        class:cal-day--seleccionado-ida={sel}
                        class:cal-day--bloqueado={!disp}
                        on:click={() => pickIda(item.fecha)}
                        disabled={!disp}
                        title={disp ? 'Vuelo disponible' : 'Sin vuelos'}>
                        {item.dia}
                      </button>
                    {/if}
                  {/each}
                </div>
                {#if departureDate}
                  <div class="cal-selected-info">Ida: <strong>{departureDate}</strong></div>
                {/if}
              </div>

              {#if tripType === 'roundtrip'}
                <div class="cal-container">
                  <div class="cal-label">↩ Fecha de regreso</div>
                  <div class="cal-nav">
                    <button type="button" class="cal-nav__btn" on:click={prevVuelta}>‹</button>
                    <span class="cal-nav__title">{titVuelta}</span>
                    <button type="button" class="cal-nav__btn" on:click={nextVuelta}>›</button>
                  </div>
                  <div class="cal-grid cal-grid--header">
                    {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                  </div>
                  <div class="cal-grid">
                    {#each diasVuelta as item}
                      {#if item === null}
                        <span class="cal-day cal-day--empty"></span>
                      {:else}
                        {@const bloqIda  = departureDate && item.fecha < departureDate}
                        {@const bloqDisp = !esFechaDisponibleVuelta(item.fecha)}
                        {@const bloq = bloqIda || bloqDisp}
                        {@const sel  = returnDate === item.fecha}
                        <button type="button" class="cal-day"
                          class:cal-day--disponible-vuelta={!bloq && !sel}
                          class:cal-day--seleccionado-vuelta={sel}
                          class:cal-day--bloqueado={bloq}
                          on:click={() => pickVuelta(item.fecha)}
                          disabled={bloq}
                          title={bloqIda ? 'Fecha anterior a la ida' : bloqDisp ? 'Sin vuelos de regreso' : 'Vuelo disponible'}>
                          {item.dia}
                        </button>
                      {/if}
                    {/each}
                  </div>
                  {#if returnDate}
                    <div class="cal-selected-info cal-selected-info--vuelta">Regreso: <strong>{returnDate}</strong></div>
                  {/if}
                </div>
              {/if}

            </div>
          </div>
        {/if}

        {#if searchError}
          <p class="broom-home__search-error">{searchError}</p>
        {/if}

      </form>
    </div>
  </section>

  <!-- Grilla de destinos destacados con imagen, ciudad, pais y nombre de aeropuerto -->
  {#if destinosConImagen.length > 0}
  <section class="broom-home__destinations">
    <div class="broom-home__destinations-container">
      <h2 class="broom-home__destinations-title">Destinos destacados</h2>
      <div class="broom-home__destinations-grid">
        {#each destinosConImagen as aeropuerto}
          <article class="broom-home__destination-card"
            on:click={() => { toQuery = `${aeropuerto.ciudad} (${aeropuerto.codigo})`; toSeleccionado = aeropuerto; }}
            role="button" tabindex="0"
            on:keydown={e => e.key === 'Enter' && navigateTo('vuelos')}>
            <div class="broom-home__destination-image">
              <img
                src={aeropuerto.imagenBase64.startsWith("data:") ? aeropuerto.imagenBase64 : `data:image/jpeg;base64,${aeropuerto.imagenBase64}`}
                alt="{aeropuerto.ciudad}, {aeropuerto.pais}"
                class="broom-home__destination-image-visual"
              />
              <div class="broom-home__destination-badge">{aeropuerto.codigo}</div>
            </div>
            <div class="broom-home__destination-content">
              <h3 class="broom-home__destination-name">{aeropuerto.ciudad}</h3>
              <p class="broom-home__destination-meta">{aeropuerto.pais}</p>
              <p class="broom-home__destination-description">{aeropuerto.nombre}</p>
            </div>
          </article>
        {/each}
      </div>
      <div class="broom-home__destinations-actions">
        <button type="button" class="broom-home__destinations-btn" on:click={() => navigateTo('destinos-destacados')}>
          Ver mas destinos
        </button>
      </div>
    </div>
  </section>
  {/if}

</div>
