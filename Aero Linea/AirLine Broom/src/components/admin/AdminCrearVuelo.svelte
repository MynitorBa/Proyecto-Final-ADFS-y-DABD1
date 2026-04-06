<script>
/**
 * @file AdminCrearVuelo.svelte
 * @description Admin panel section for creating a new flight. Provides a multi-section form
 * covering basic info (flight number, date), route (origin and destination airports with
 * searchable dropdowns), schedule (departure time and an auto-calculated estimated arrival
 * preview), aircraft selection (filtered by availability on the selected date), seat distribution
 * with a visual capacity bar, pricing for tourist and executive classes, and crew assignment.
 * Validates all fields before posting to the backend API. Checks route existence and real-time
 * availability of aircraft and crew members for the chosen date. Dispatches 'vueloCreado' on
 * success and 'irARutas' when the user requests to create a missing route.
 */
// @ts-nocheck
  import { createEventDispatcher } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** List of all available airports, used to populate origin and destination dropdowns. @type {any[]} */
  export let aeropuertos  = [];

  /** List of all aircraft in the fleet, used to populate the aircraft selection dropdown. @type {any[]} */
  export let aviones      = [];

  /** List of all crew members, used to populate the crew assignment dropdown. @type {any[]} */
  export let tripulantes  = [];

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Function to show a confirmation dialog. Signature: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Current text in the origin airport search input. @type {string} */
  let busquedaOrigen        = '';

  /** Current text in the destination airport search input. @type {string} */
  let busquedaDestino       = '';

  /** Current text in the aircraft search input. @type {string} */
  let busquedaAvion         = '';

  /** Current text in the crew member search input. @type {string} */
  let busquedaTripulante    = '';

  /** Whether the origin airport dropdown is open. @type {boolean} */
  let mostrarDropdownOrigen     = false;

  /** Whether the destination airport dropdown is open. @type {boolean} */
  let mostrarDropdownDestino    = false;

  /** Whether the aircraft dropdown is open. @type {boolean} */
  let mostrarDropdownAvion      = false;

  /** Whether the crew member dropdown is open. @type {boolean} */
  let mostrarDropdownTripulante = false;

  /**
   * Object holding all form field values for the new flight being created.
   * @type {{
   *   numeroVuelo: string,
   *   aeropuertoOrigenId: string,
   *   aeropuertoDestinoId: string,
   *   avionId: string,
   *   fecha: string,
   *   horaSalida: string,
   *   boletosTurista: string,
   *   boletosEjecutivo: string,
   *   precioTurista: string,
   *   precioEjecutiva: string,
   *   tripulantesSeleccionados: any[]
   * }}
   */
  let nuevoVuelo = {
    numeroVuelo: '',
    aeropuertoOrigenId: '',
    aeropuertoDestinoId: '',
    avionId: '',
    fecha: '',
    horaSalida: '',
    boletosTurista: '',
    boletosEjecutivo: '',
    precioTurista: '',
    precioEjecutiva: '',
    tripulantesSeleccionados: []
  };

  /** Arrival time preview object returned by the calcular-llegada API endpoint. @type {any} */
  let previewLlegada       = null;

  /** Whether the arrival preview API call is in progress. @type {boolean} */
  let loadingPreview       = false;

  /** Debounce timer ID for the arrival preview calculation. @type {any} */
  let previewDebounceTimer = null;

  /**
   * Current route existence check status.
   * Values: null (not checked), 'checking', 'ok', 'missing'.
   * @type {string|null}
   */
  let rutaExisteStatus     = null;

  /** Debounce timer ID for the route existence check. @type {any} */
  let rutaCheckTimer       = null;

  /** Last origin airport ID used in a route existence check, to avoid redundant calls. @type {string|null} */
  let lastOrigenId         = null;

  /** Last destination airport ID used in a route existence check, to avoid redundant calls. @type {string|null} */
  let lastDestinoId        = null;

  /** Set of aircraft IDs already assigned to another flight on the selected date. @type {Set<number>} */
  let avionesOcupadosIds     = new Set();

  /** Set of crew member IDs already assigned to another flight on the selected date and time. @type {Set<number>} */
  let tripulantesOcupadosIds = new Set();

  /** Whether the availability fetch for aircraft and crew is in progress. @type {boolean} */
  let cargandoDisponibilidad = false;

  /** Today's date formatted as YYYY-MM-DD, used as the minimum allowed flight date. @type {string} */
  const hoyStr = new Date().toISOString().split('T')[0];

  /**
   * Formats the flight number input: forces 2 uppercase letters + space + digits.
   * Updates nuevoVuelo.numeroVuelo and the input element value in place.
   * @param {Event} e - The input event from the flight number text field.
   */
  function formatearNumeroVuelo(e) {
    let val = e.target.value.toUpperCase().replace(/[^A-Z0-9 ]/g, '');
    let letras = val.slice(0, 2).replace(/[^A-Z]/g, '');
    let resto  = val.slice(2).replace(/[^0-9]/g, '');
    if (letras.length === 2 && resto.length > 0) {
      nuevoVuelo.numeroVuelo = `${letras} ${resto}`;
    } else {
      nuevoVuelo.numeroVuelo = letras + resto;
    }
    e.target.value = nuevoVuelo.numeroVuelo;
  }

  /**
   * Returns true when the date string is 10 characters long and has a year between the
   * current year and 2099.
   * @param {string} fecha - Date string in YYYY-MM-DD format.
   * @returns {boolean}
   */
  function fechaEsValida(fecha) {
    if (!fecha || fecha.length < 10) return false;
    const year = parseInt(fecha.split('-')[0]);
    const hoy  = new Date();
    return year >= hoy.getFullYear() && year <= 2099;
  }

  /**
   * Returns true when the date string represents a date before today (midnight local time).
   * @param {string} fecha - Date string in YYYY-MM-DD format.
   * @returns {boolean}
   */
  function fechaEsPasada(fecha) {
    if (!fecha) return false;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return new Date(fecha) < hoy;
  }

  // Filters airports for the origin dropdown. Shows first 5 when query < 2 chars; otherwise
  // filters by name, code or city, capped at 10.
  $: aeropuertosFiltradosOrigen = busquedaOrigen.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigen.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigen.toLowerCase())
      ).slice(0, 10);

  // Filters airports for the destination dropdown. Shows first 5 when query < 2 chars; otherwise
  // filters by name, code or city, capped at 10.
  $: aeropuertosFiltradosDestino = busquedaDestino.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaDestino.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaDestino.toLowerCase())
      ).slice(0, 10);

  // Filters aircraft by name/brand/model match AND excludes IDs in avionesOcupadosIds.
  $: avionesFiltrados = aviones.filter(a => {
    const coincide =
      a.nombreCompleto.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.marca.toLowerCase().includes(busquedaAvion.toLowerCase()) ||
      a.modelo.toLowerCase().includes(busquedaAvion.toLowerCase());
    return coincide && !avionesOcupadosIds.has(a.id);
  });

  // Filters crew members by name/role match, excludes already-selected and busy IDs.
  $: tripulantesFiltrados = tripulantes.filter(t => {
    const yaSeleccionado = nuevoVuelo.tripulantesSeleccionados.some(ts => ts.id === t.id);
    const coincide =
      t.nombreCompleto.toLowerCase().includes(busquedaTripulante.toLowerCase()) ||
      t.nombreRol.toLowerCase().includes(busquedaTripulante.toLowerCase());
    return !yaSeleccionado && !tripulantesOcupadosIds.has(t.id) && coincide;
  });

  // Resolves the selected origin airport object from the aeropuertos list.
  $: aeropuertoOrigen  = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoOrigenId));

  // Resolves the selected destination airport object from the aeropuertos list.
  $: aeropuertoDestino = aeropuertos.find(a => a.id === parseInt(nuevoVuelo.aeropuertoDestinoId));

  // Resolves the selected aircraft object from the aviones list.
  $: avionSeleccionado = aviones.find(a => a.id === parseInt(nuevoVuelo.avionId));

  // Sums tourist and executive seat counts to check against aircraft capacity.
  $: totalBoletosAsignados = (parseInt(nuevoVuelo.boletosTurista)   || 0) +
                             (parseInt(nuevoVuelo.boletosEjecutivo) || 0);

  // Passenger capacity of the currently selected aircraft (0 if none selected).
  $: capacidadAvion    = avionSeleccionado?.capacidadPasajeros ?? 0;

  // True when the assigned seat total exceeds the aircraft capacity.
  $: excedeLimite      = capacidadAvion > 0 && totalBoletosAsignados > capacidadAvion;

  // Percentage of capacity occupied, capped at 100, used to drive the capacity progress bar.
  $: porcentajeOcupado = capacidadAvion > 0
    ? Math.min(100, Math.round(totalBoletosAsignados / capacidadAvion * 100))
    : 0;

  // True when origin, destination, a valid non-past date, and departure time are all set.
  $: camposListos = !!nuevoVuelo.aeropuertoOrigenId &&
                    !!nuevoVuelo.aeropuertoDestinoId &&
                    fechaEsValida(nuevoVuelo.fecha) &&
                    !fechaEsPasada(nuevoVuelo.fecha) &&
                    !!nuevoVuelo.horaSalida;

  // When an aircraft is selected and no seat counts have been entered yet, auto-fills
  // 25% executive and 75% tourist based on total capacity.
  $: if (avionSeleccionado && !nuevoVuelo.boletosTurista && !nuevoVuelo.boletosEjecutivo) {
    const cap = avionSeleccionado.capacidadPasajeros;
    const eje = Math.floor(cap * 0.25);
    nuevoVuelo.boletosEjecutivo = eje;
    nuevoVuelo.boletosTurista   = cap - eje;
  }

  // Triggers a route existence check whenever the origin or destination airport changes.
  $: { nuevoVuelo.aeropuertoOrigenId; nuevoVuelo.aeropuertoDestinoId; verificarRutaSiCambioAeropuerto(); }

  // Triggers an arrival preview calculation whenever date or departure time changes and both are valid.
  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) actualizarPreviewLlegada();
    else previewLlegada = null;
  }

  // Triggers an availability check for aircraft and crew when the date or time changes.
  $: {
    nuevoVuelo.fecha; nuevoVuelo.horaSalida;
    if (fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) cargarDisponibilidad();
    else { avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); }
  }

  /**
   * Checks whether a route exists between the currently selected origin and destination airports.
   * Debounces the check by 300ms and only runs when both airports are set and at least one of
   * them changed since the last check. Sets rutaExisteStatus to 'checking', then 'ok' or 'missing'.
   */
  function verificarRutaSiCambioAeropuerto() {
    const origenId  = nuevoVuelo.aeropuertoOrigenId;
    const destinoId = nuevoVuelo.aeropuertoDestinoId;
    if (!origenId || !destinoId) { rutaExisteStatus = null; previewLlegada = null; return; }
    if (origenId === lastOrigenId && destinoId === lastDestinoId) return;
    lastOrigenId = origenId; lastDestinoId = destinoId;
    clearTimeout(rutaCheckTimer);
    rutaCheckTimer = setTimeout(async () => {
      rutaExisteStatus = 'checking';
      try {
        const rc = await fetch(`${API}/api/rutas/existe?origenId=${origenId}&destinoId=${destinoId}`, { credentials: 'include' });
        if (rc.ok) { const { existe } = await rc.json(); rutaExisteStatus = existe ? 'ok' : 'missing'; }
        else rutaExisteStatus = null;
      } catch { rutaExisteStatus = null; }
      if (rutaExisteStatus === 'ok' && fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha)) calcularPreviewLlegada();
      else { previewLlegada = null; loadingPreview = false; }
    }, 300);
  }

  /**
   * Triggers the arrival preview calculation only when the route has been confirmed to exist.
   */
  function actualizarPreviewLlegada() {
    if (rutaExisteStatus !== 'ok') return;
    calcularPreviewLlegada();
  }

  /**
   * Debounces and then calls the calcular-llegada API endpoint with origin, destination, date
   * and departure time. Stores the result in previewLlegada. Aborts the request after 8 seconds
   * using AbortController. Requires all fields to be valid and non-past before sending.
   * @async
   */
  function calcularPreviewLlegada() {
    const origenId  = parseInt(nuevoVuelo.aeropuertoOrigenId);
    const destinoId = parseInt(nuevoVuelo.aeropuertoDestinoId);
    if (!origenId || !destinoId || isNaN(origenId) || isNaN(destinoId) ||
        !fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha) ||
        !nuevoVuelo.horaSalida) {
      previewLlegada = null; loadingPreview = false; return;
    }
    clearTimeout(previewDebounceTimer);
    previewDebounceTimer = setTimeout(async () => {
      loadingPreview = true; previewLlegada = null;
      const controller = new AbortController();
      const tid = setTimeout(() => controller.abort(), 8000);
      try {
        const r = await fetch(`${API}/api/rutas/calcular-llegada`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            aeropuertoOrigenId:  origenId,
            aeropuertoDestinoId: destinoId,
            fechaSalida: nuevoVuelo.fecha,
            horaSalida:  nuevoVuelo.horaSalida
          })
        });
        if (r.ok) previewLlegada = await r.json();
        else previewLlegada = null;
      } catch { previewLlegada = null; }
      finally { clearTimeout(tid); loadingPreview = false; }
    }, 600);
  }

  /**
   * Fetches the IDs of aircraft and crew members already assigned to flights on the selected
   * date (+/-  departure time). Removes any currently selected aircraft or crew that become
   * unavailable. Clears the sets if the date is invalid or in the past.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarDisponibilidad() {
    if (!fechaEsValida(nuevoVuelo.fecha) || fechaEsPasada(nuevoVuelo.fecha)) {
      avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set(); return;
    }
    cargandoDisponibilidad = true;
    try {
      const urlAviones = `${API}/api/admin/vuelos/aviones-ocupados?fecha=${nuevoVuelo.fecha}${nuevoVuelo.horaSalida ? `&horaSalida=${nuevoVuelo.horaSalida}` : ''}`;
      const urlTrip    = nuevoVuelo.horaSalida
        ? `${API}/api/admin/vuelos/tripulantes-ocupados?fecha=${nuevoVuelo.fecha}&horaSalida=${nuevoVuelo.horaSalida}`
        : null;
      const [rA, rT] = await Promise.all([
        fetch(urlAviones, { credentials: 'include' }),
        urlTrip ? fetch(urlTrip, { credentials: 'include' }) : Promise.resolve(null)
      ]);
      if (rA.ok) avionesOcupadosIds = new Set(await rA.json());
      if (rT && rT.ok) tripulantesOcupadosIds = new Set(await rT.json());
      if (nuevoVuelo.avionId && avionesOcupadosIds.has(parseInt(nuevoVuelo.avionId))) {
        nuevoVuelo.avionId = ''; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = '';
        busquedaAvion = '';
      }
      nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados
        .filter(t => !tripulantesOcupadosIds.has(t.id));
    } catch(e) { console.error('Error cargando disponibilidad', e); }
    finally { cargandoDisponibilidad = false; }
  }

  /**
   * Sets the origin airport on the form, updates the search text and closes the dropdown.
   * @param {any} a - The airport object selected from the dropdown.
   */
  function seleccionarAeropuertoOrigen(a)  { nuevoVuelo.aeropuertoOrigenId  = a.id; busquedaOrigen  = `${a.codigo} - ${a.nombre}`; mostrarDropdownOrigen  = false; }

  /**
   * Sets the destination airport on the form, updates the search text and closes the dropdown.
   * @param {any} a - The airport object selected from the dropdown.
   */
  function seleccionarAeropuertoDestino(a) { nuevoVuelo.aeropuertoDestinoId = a.id; busquedaDestino = `${a.codigo} - ${a.nombre}`; mostrarDropdownDestino = false; }

  /**
   * Sets the aircraft on the form, updates the search text, closes the dropdown and resets
   * the seat count fields so they can be auto-filled based on the new aircraft capacity.
   * @param {any} a - The aircraft object selected from the dropdown.
   */
  function seleccionarAvion(a)   { nuevoVuelo.avionId = a.id; busquedaAvion = a.nombreCompleto; mostrarDropdownAvion = false; nuevoVuelo.boletosTurista = ''; nuevoVuelo.boletosEjecutivo = ''; }

  /**
   * Adds a crew member to the selected crew list and resets the crew search input.
   * @param {any} t - The crew member object selected from the dropdown.
   */
  function agregarTripulante(t)  { nuevoVuelo.tripulantesSeleccionados = [...nuevoVuelo.tripulantesSeleccionados, t]; busquedaTripulante = ''; mostrarDropdownTripulante = false; }

  /**
   * Removes a crew member from the selected crew list by their ID.
   * @param {number} id - The ID of the crew member to remove.
   */
  function quitarTripulante(id)  { nuevoVuelo.tripulantesSeleccionados = nuevoVuelo.tripulantesSeleccionados.filter(t => t.id !== id); }

  /**
   * Resets the entire flight creation form back to its initial empty state, including all
   * search inputs, dropdown states, preview data and availability sets.
   */
  function limpiarFormularioVuelo() {
    nuevoVuelo = { numeroVuelo: '', aeropuertoOrigenId: '', aeropuertoDestinoId: '', avionId: '', fecha: '', horaSalida: '', boletosTurista: '', boletosEjecutivo: '', precioTurista: '', precioEjecutiva: '', tripulantesSeleccionados: [] };
    busquedaOrigen = ''; busquedaDestino = ''; busquedaAvion = ''; busquedaTripulante = '';
    previewLlegada = null; rutaExisteStatus = null; lastOrigenId = null; lastDestinoId = null;
    avionesOcupadosIds = new Set(); tripulantesOcupadosIds = new Set();
  }

  /**
   * Validates all required fields, verifies the route exists via the API, then POSTs the new
   * flight to the backend. On success clears the form and dispatches 'vueloCreado'. Shows
   * specific error toasts for each validation failure or API error.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearVuelo() {
    if (!nuevoVuelo.numeroVuelo || nuevoVuelo.numeroVuelo.length < 4) { mostrarToast('error', 'Ingresa el numero de vuelo (ej: AA 1234)'); return; }
    if (!nuevoVuelo.aeropuertoOrigenId)   { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevoVuelo.aeropuertoDestinoId)  { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (!nuevoVuelo.avionId)              { mostrarToast('error', 'Selecciona un avion'); return; }
    if (!fechaEsValida(nuevoVuelo.fecha)) { mostrarToast('error', 'Ingresa una fecha valida'); return; }
    if (fechaEsPasada(nuevoVuelo.fecha))  { mostrarToast('error', 'La fecha del vuelo no puede ser en el pasado'); return; }
    if (!nuevoVuelo.horaSalida)           { mostrarToast('error', 'Ingresa la hora de salida'); return; }
    if (!nuevoVuelo.boletosTurista || parseInt(nuevoVuelo.boletosTurista) < 0)     { mostrarToast('error', 'Ingresa los boletos de clase turista'); return; }
    if (!nuevoVuelo.boletosEjecutivo || parseInt(nuevoVuelo.boletosEjecutivo) < 0) { mostrarToast('error', 'Ingresa los boletos de clase ejecutiva'); return; }
    if (excedeLimite) { mostrarToast('error', `Los boletos (${totalBoletosAsignados}) exceden la capacidad del avion (${capacidadAvion})`); return; }
    if (!nuevoVuelo.precioTurista || !nuevoVuelo.precioEjecutiva) { mostrarToast('error', 'Ingresa los precios de ambas clases'); return; }
    try {
      const rCheck = await fetch(`${API}/api/rutas/existe?origenId=${nuevoVuelo.aeropuertoOrigenId}&destinoId=${nuevoVuelo.aeropuertoDestinoId}`, { credentials: 'include' });
      if (rCheck.ok) { const { existe } = await rCheck.json(); if (!existe) { mostrarToast('error', 'No existe una ruta entre estos aeropuertos. Creala primero en "Gestionar Rutas".'); return; } }
    } catch { }
    try {
      const r = await fetch(`${API}/api/admin/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          numeroVuelo:         nuevoVuelo.numeroVuelo,
          aeropuertoOrigenId:  parseInt(nuevoVuelo.aeropuertoOrigenId),
          aeropuertoDestinoId: parseInt(nuevoVuelo.aeropuertoDestinoId),
          avionId:             parseInt(nuevoVuelo.avionId),
          fecha:               nuevoVuelo.fecha,
          horaSalida:          nuevoVuelo.horaSalida,
          boletosTurista:      parseInt(nuevoVuelo.boletosTurista),
          boletosEjecutivo:    parseInt(nuevoVuelo.boletosEjecutivo),
          precioTurista:       parseFloat(nuevoVuelo.precioTurista),
          precioEjecutiva:     parseFloat(nuevoVuelo.precioEjecutiva),
          tripulantesIds:      nuevoVuelo.tripulantesSeleccionados.map(t => t.id)
        })
      });
      if (r.ok) { mostrarToast('success', '¡Vuelo creado exitosamente!'); limpiarFormularioVuelo(); dispatch('vueloCreado'); }
      else { const err = await r.json(); mostrarToast('error', err.message || 'Error al crear el vuelo'); }
    } catch { mostrarToast('error', 'Error de conexion al crear el vuelo'); }
  }
</script>

<!-- Seccion del formulario para crear un nuevo vuelo con validacion y calculo de llegada -->
<section class="admin-section">
  <h2 class="admin-section__title">Crear Nuevo Vuelo</h2>
  <p class="admin-section__subtitle">Completa todos los datos del vuelo</p>

  <form class="admin-form" on:submit|preventDefault={handleCrearVuelo}>

    <!-- Grupo: numero de vuelo y fecha -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Informacion Basica</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-numero" class="admin-form__label">Numero de Vuelo *</label>
          <input type="text" id="cv-numero" class="admin-form__input"
            value={nuevoVuelo.numeroVuelo}
            on:input={formatearNumeroVuelo}
            placeholder="Ej: AA 1234" maxlength="8"
            style="text-transform:uppercase;letter-spacing:1px"
            autocomplete="off" required />
          <small class="img-hint">2 letras + espacio + numero (ej: AA 1234, LA 820)</small>
        </div>
        <div class="admin-form__field">
          <label for="cv-fecha" class="admin-form__label">Fecha del Vuelo *</label>
          <input type="date" id="cv-fecha" class="admin-form__input"
            bind:value={nuevoVuelo.fecha} min={hoyStr} required />
          {#if nuevoVuelo.fecha && fechaEsPasada(nuevoVuelo.fecha)}
            <small style="color:#c62828;font-size:.78rem">La fecha no puede ser en el pasado</small>
          {/if}
        </div>
      </div>
    </div>

    <!-- Grupo: seleccion de aeropuertos de origen y destino con verificacion de ruta existente -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Ruta</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-origen" class="admin-form__label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input id="cv-origen" type="text" class="admin-form__input" bind:value={busquedaOrigen}
              on:focus={() => mostrarDropdownOrigen = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigen = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownOrigen && aeropuertosFiltradosOrigen.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigen.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosOrigen as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoOrigen(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigen}<p class="selected-item">✔ {aeropuertoOrigen.codigo} — {aeropuertoOrigen.nombre}</p>{/if}
          </div>
        </div>
        <div class="admin-form__field">
          <label for="cv-destino" class="admin-form__label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input id="cv-destino" type="text" class="admin-form__input" bind:value={busquedaDestino}
              on:focus={() => mostrarDropdownDestino = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestino = false, 200)}
              placeholder="Buscar aeropuerto..." autocomplete="off" />
            {#if mostrarDropdownDestino && aeropuertosFiltradosDestino.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestino.length < 2}<div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>{/if}
                {#each aeropuertosFiltradosDestino as a}
                  <button type="button" class="searchable-select__option" on:click={() => seleccionarAeropuertoDestino(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestino}<p class="selected-item">✔ {aeropuertoDestino.codigo} — {aeropuertoDestino.nombre}</p>{/if}
          </div>
        </div>
      </div>
    </div>

    <!-- Grupo: hora de salida y preview de hora de llegada calculada con zonas horarias -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Horarios</h3>
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-hora" class="admin-form__label">Hora de Salida *</label>
          <input type="time" id="cv-hora" class="admin-form__input" bind:value={nuevoVuelo.horaSalida} required />
          <small class="img-hint">Hora local en el aeropuerto de origen</small>
        </div>

        <div class="admin-form__field">
          <p class="admin-form__label">Hora de Llegada</p>

          {#if rutaExisteStatus === 'checking' || loadingPreview}
            <div class="llegada-preview llegada-preview--loading">
              <div class="llegada-loader">
                <span class="llegada-loader__plane">✈</span>
                <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                <span class="llegada-loader__text">
                  {rutaExisteStatus === 'checking' ? 'Verificando ruta...' : 'Calculando hora de llegada...'}
                </span>
              </div>
            </div>

          {:else if rutaExisteStatus === 'missing'}
            <div class="llegada-preview--no-ruta">
              <span class="llegada-preview__no-ruta-icon">🚫</span>
              <span class="llegada-preview__no-ruta-title">No existe esta ruta</span>
              <small class="llegada-preview__no-ruta-msg">Creala en <strong>Gestionar Rutas</strong> antes de crear el vuelo.</small>
              <button type="button" class="llegada-preview__no-ruta-btn" on:click={() => dispatch('irARutas')}>→ Ir a crear la ruta</button>
            </div>

          {:else if previewLlegada}
            <div class="llegada-preview" class:llegada-preview--tz={previewLlegada.usoZonasHorarias}>
              <span class="llegada-preview__time">🛬 {previewLlegada.horaLlegada}
                {#if previewLlegada.fechaLlegada !== nuevoVuelo.fecha}
                  <span class="llegada-preview__nextday">(+1 dia)</span>
                {/if}
              </span>
              <span class="llegada-preview__meta">{previewLlegada.duracionMinutos} min ·
                {#if previewLlegada.usoZonasHorarias}
                  <span class="tz-badge tz-badge--ok">✔ Con zona horaria</span>
                {:else}
                  <span class="tz-badge tz-badge--missing">Sin zona horaria</span>
                {/if}
              </span>
              <small class="llegada-preview__nota">{previewLlegada.nota}</small>
            </div>

          {:else}
            <div class="llegada-preview llegada-preview--empty">
              Se calcula automaticamente al completar origen, destino, fecha y hora de salida
              {#if camposListos}
                <div class="llegada-loader" style="margin-top:.5rem">
                  <span class="llegada-loader__plane">✈</span>
                  <div class="llegada-loader__bar"><div class="llegada-loader__fill"></div></div>
                  <span class="llegada-loader__text">Preparando calculo...</span>
                </div>
              {/if}
            </div>
          {/if}
        </div>
      </div>
    </div>

    <!-- Grupo: seleccion de aeronave filtrada por disponibilidad en la fecha elegida -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Aeronave</h3>
      <div class="admin-form__field admin-form__field--full">
        <label for="cv-avion" class="admin-form__label">Seleccionar Avion *</label>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && avionesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--info">
            {avionesOcupadosIds.size} avion(es) ya asignado(s) a otro vuelo en esta fecha no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            ✔ Mostrando aviones disponibles para {nuevoVuelo.fecha}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-avion" type="text" class="admin-form__input" bind:value={busquedaAvion}
            on:focus={() => mostrarDropdownAvion = true}
            on:blur={() => setTimeout(() => mostrarDropdownAvion = false, 200)}
            placeholder="Buscar avion..." autocomplete="off" />
          {#if mostrarDropdownAvion && avionesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each avionesFiltrados.slice(0, 10) as a}
                <button type="button" class="searchable-select__option" on:click={() => seleccionarAvion(a)}>
                  {#if a.imagenBase64}<img src={a.imagenBase64} alt={a.nombreCompleto} class="dropdown-thumb" />{/if}
                  <span class="searchable-select__option-name">{a.nombreCompleto}</span>
                  <span class="searchable-select__option-detail">{a.capacidadPasajeros} pasajeros</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownAvion && avionesFiltrados.length === 0 && aviones.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty">🚫 Todos los aviones estan ocupados para el {nuevoVuelo.fecha || 'dia seleccionado'}</p>
            </div>
          {/if}
          {#if avionSeleccionado}<p class="selected-item">✔ {avionSeleccionado.nombreCompleto}</p>{/if}
        </div>
      </div>
    </div>

    <!-- Grupo: distribucion de boletos por clase con barra de capacidad y precios -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Distribucion de Asientos y Precios</h3>
      {#if avionSeleccionado}
        <div class="capacidad-bar">
          <div class="capacidad-bar__labels">
            <span>Capacidad total: <strong>{capacidadAvion} pax</strong></span>
            <span class="capacidad-bar__count"
              class:capacidad-bar__count--ok={totalBoletosAsignados === capacidadAvion && !excedeLimite}
              class:capacidad-bar__count--error={excedeLimite}>
              {totalBoletosAsignados} asignados
              {#if excedeLimite}&nbsp;Excede limite{:else if totalBoletosAsignados === capacidadAvion}&nbsp;✔ Completo{/if}
            </span>
          </div>
          <div class="capacidad-bar__track">
            <div class="capacidad-bar__fill" class:capacidad-bar__fill--error={excedeLimite} style="width:{porcentajeOcupado}%"></div>
          </div>
        </div>
      {/if}
      <div class="admin-form__row">
        <div class="admin-form__field">
          <label for="cv-turista" class="admin-form__label">Boletos Clase Turista *</label>
          <input type="number" id="cv-turista" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosTurista} placeholder="Ej: 180"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
        <div class="admin-form__field">
          <label for="cv-ejecutivo" class="admin-form__label">Boletos Clase Ejecutiva *</label>
          <input type="number" id="cv-ejecutivo" class="admin-form__input" min="0"
            bind:value={nuevoVuelo.boletosEjecutivo} placeholder="Ej: 60"
            max={capacidadAvion > 0 ? capacidadAvion : undefined} required />
        </div>
      </div>
      <div class="admin-form__row" style="margin-top:1.5rem">
        <div class="admin-form__field">
          <label for="cv-precio-turista" class="admin-form__label">Precio Turista (USD) *</label>
          <input type="number" id="cv-precio-turista" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioTurista} placeholder="Ej: 150.00" required />
        </div>
        <div class="admin-form__field">
          <label for="cv-precio-eje" class="admin-form__label">Precio Ejecutiva (USD) *</label>
          <input type="number" id="cv-precio-eje" class="admin-form__input" min="0" step="0.01"
            bind:value={nuevoVuelo.precioEjecutiva} placeholder="Ej: 300.00" required />
        </div>
      </div>
    </div>

    <!-- Grupo: asignacion de tripulantes disponibles con busqueda y lista de seleccionados -->
    <div class="admin-form__group">
      <h3 class="admin-form__group-title">Tripulacion</h3>
      <div class="admin-form__field admin-form__field--full">
        <label for="cv-trip" class="admin-form__label">Agregar Tripulantes</label>
        {#if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && tripulantesOcupadosIds.size > 0}
          <small class="disponibilidad-hint disponibilidad-hint--warn">
            {tripulantesOcupadosIds.size} tripulante(s) ya asignado(s) a otro vuelo y no aparecen en la lista
          </small>
        {:else if fechaEsValida(nuevoVuelo.fecha) && !fechaEsPasada(nuevoVuelo.fecha) && nuevoVuelo.horaSalida && !cargandoDisponibilidad}
          <small class="disponibilidad-hint disponibilidad-hint--ok">
            ✔ Mostrando tripulantes disponibles para {nuevoVuelo.fecha} a las {nuevoVuelo.horaSalida}
          </small>
        {/if}
        <div class="searchable-select">
          <input id="cv-trip" type="text" class="admin-form__input" bind:value={busquedaTripulante}
            on:focus={() => mostrarDropdownTripulante = true}
            on:blur={() => setTimeout(() => mostrarDropdownTripulante = false, 200)}
            placeholder="Buscar por nombre o rol..." autocomplete="off" />
          {#if mostrarDropdownTripulante && tripulantesFiltrados.length > 0}
            <div class="searchable-select__dropdown">
              {#each tripulantesFiltrados.slice(0, 10) as t}
                <button type="button" class="searchable-select__option" on:click={() => agregarTripulante(t)}>
                  {#if t.imagenBase64}<img src={t.imagenBase64} alt={t.nombreCompleto} class="dropdown-thumb dropdown-thumb--circle" />{/if}
                  <span class="searchable-select__option-name">{t.nombreCompleto}</span>
                  <span class="searchable-select__option-role">{t.nombreRol}</span>
                </button>
              {/each}
            </div>
          {:else if mostrarDropdownTripulante && tripulantesFiltrados.length === 0 && tripulantes.length > 0}
            <div class="searchable-select__dropdown">
              <p class="searchable-select__empty">🚫 Ningun tripulante disponible.<br><small>Deben pasar 24h desde su vuelo anterior.</small></p>
            </div>
          {/if}
        </div>
        {#if nuevoVuelo.tripulantesSeleccionados.length > 0}
          <div class="tripulantes-seleccionados">
            <p class="tripulantes-seleccionados__title">Tripulantes seleccionados ({nuevoVuelo.tripulantesSeleccionados.length})</p>
            <div class="tripulantes-seleccionados__list">
              {#each nuevoVuelo.tripulantesSeleccionados as t}
                <div class="tripulante-item">
                  <div class="tripulante-item__info">
                    <span class="tripulante-item__name">{t.nombreCompleto}</span>
                    <span class="tripulante-item__rol">{t.nombreRol}</span>
                  </div>
                  <button type="button" class="tripulante-item__remove" on:click={() => quitarTripulante(t.id)}>×</button>
                </div>
              {/each}
            </div>
          </div>
        {/if}
      </div>
    </div>

    <!-- Acciones del formulario: crear vuelo o limpiar todos los campos -->
    <div class="admin-form__actions">
      <button type="submit" class="admin-form__submit">Crear Vuelo</button>
      <button type="button" class="admin-form__cancel" on:click={limpiarFormularioVuelo}>Limpiar</button>
    </div>

  </form>
</section>
