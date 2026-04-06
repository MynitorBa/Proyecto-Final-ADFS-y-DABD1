<script>
/**
 * @file AdminAeropuertos.svelte
 * @description Admin panel section for managing airports. Displays a table of all registered
 * airports with their IATA code, name, city, country and optional image. Allows creating new
 * airports and editing existing ones through a modal form. The form integrates a live IATA
 * database (fetched from GitHub) for autocomplete, a timezone picker backed by the WorldTimeAPI,
 * and a country name resolver that tries restcountries.com first and falls back to a local
 * ISO-2 map. Dispatches 'aeropuertosActualizados' to the parent after any successful mutation.
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

  /** List of airports currently registered in the system, loaded from the backend API. @type {any[]} */
  let aeropuertos        = [];

  /** Whether the airport list fetch is in progress. @type {boolean} */
  let loadingAeropuertos = false;

  /** True when the modal is editing an existing airport, false when creating a new one. @type {boolean} */
  let modoEdicion        = false;

  /** Whether the create/edit modal form is visible. @type {boolean} */
  let mostrarFormulario       = false;

  /**
   * Form data object bound to the create/edit form fields.
   * @type {{ id: number|null, codigo: string, nombre: string, ciudad: string, pais: string, zonaHoraria: string }}
   */
  let aeropuertoForm          = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };

  /** Data URL of the selected image preview, shown before saving. @type {string|null} */
  let aeropuertoImagenPreview = null;

  /** Base64-encoded image string sent to the backend on form submission. @type {string|null} */
  let aeropuertoImagenBase64  = null;

  /**
   * Full IATA airport database keyed by ICAO code, fetched from the mwgg/Airports GitHub repo.
   * @type {Record<string, any>}
   */
  let todosLosAeropuertosIATA = {};

  /** Whether the IATA database fetch is in progress. @type {boolean} */
  let loadingIATA             = false;

  /** Current text in the IATA code/name search input. @type {string} */
  let iataQuery               = '';

  /** Whether the IATA autocomplete dropdown is open. @type {boolean} */
  let mostrarDropdownIATA     = false;

  /** True after the user has picked an airport from the IATA dropdown, locking dependent fields. @type {boolean} */
  let iataSeleccionado        = false;

  // Filters the IATA database by the current iataQuery against IATA code, name, city or state.
  // Returns up to 15 results. Shows nothing if fewer than 1 character typed.
  $: iataResultados = iataQuery.length < 1 ? [] :
    Object.entries(todosLosAeropuertosIATA)
      .filter(([icao, ap]) =>
        ap.iata && ap.iata.length === 3 && (
          ap.iata.toLowerCase().startsWith(iataQuery.toLowerCase()) ||
          ap.name?.toLowerCase().includes(iataQuery.toLowerCase()) ||
          ap.city?.toLowerCase().includes(iataQuery.toLowerCase()) ||
          ap.state?.toLowerCase().includes(iataQuery.toLowerCase())
        )
      )
      .slice(0, 15);

  /** All IANA timezone strings fetched from WorldTimeAPI. @type {string[]} */
  let todosLosTimezones  = [];

  /** Current text in the timezone search input. @type {string} */
  let busquedaTimezone   = '';

  /** Whether the timezone autocomplete dropdown is open. @type {boolean} */
  let mostrarDropdownTZ  = false;

  /** Whether the timezone list fetch is in progress. @type {boolean} */
  let loadingTimezones   = false;

  // Returns the first 6 timezones when the search query is shorter than 2 chars,
  // otherwise filters the full list by the query (case-insensitive), capped at 20.
  $: timezonesFiltrados = busquedaTimezone.length < 2
    ? todosLosTimezones.slice(0, 6)
    : todosLosTimezones
        .filter(tz => tz.toLowerCase().includes(busquedaTimezone.toLowerCase()))
        .slice(0, 20);

  /**
   * Local fallback map of ISO-2 country codes to English country names.
   * Used when the restcountries.com API call fails.
   * @type {Record<string, string>}
   */
  const ISO_PAISES = {
    AF:'Afghanistan',AR:'Argentina',AU:'Australia',AT:'Austria',BE:'Belgium',
    BR:'Brazil',CA:'Canada',CL:'Chile',CN:'China',CO:'Colombia',CR:'Costa Rica',
    CU:'Cuba',CZ:'Czech Republic',DK:'Denmark',DO:'Dominican Republic',
    EC:'Ecuador',EG:'Egypt',SV:'El Salvador',FI:'Finland',FR:'France',
    DE:'Germany',GH:'Ghana',GT:'Guatemala',HN:'Honduras',HK:'Hong Kong',
    HU:'Hungary',IN:'India',ID:'Indonesia',IR:'Iran',IQ:'Iraq',IE:'Ireland',
    IL:'Israel',IT:'Italy',JM:'Jamaica',JP:'Japan',JO:'Jordan',KE:'Kenya',
    KR:'South Korea',KW:'Kuwait',LB:'Lebanon',MX:'Mexico',MA:'Morocco',
    NL:'Netherlands',NZ:'New Zealand',NI:'Nicaragua',NG:'Nigeria',NO:'Norway',
    PK:'Pakistan',PA:'Panama',PY:'Paraguay',PE:'Peru',PH:'Philippines',
    PL:'Poland',PT:'Portugal',QA:'Qatar',RO:'Romania',RU:'Russia',
    SA:'Saudi Arabia',SN:'Senegal',ZA:'South Africa',ES:'Spain',LK:'Sri Lanka',
    SE:'Sweden',CH:'Switzerland',TW:'Taiwan',TZ:'Tanzania',TH:'Thailand',
    TR:'Turkey',UA:'Ukraine',AE:'United Arab Emirates',GB:'United Kingdom',
    US:'United States',UY:'Uruguay',VE:'Venezuela',VN:'Vietnam',YE:'Yemen',
    ZW:'Zimbabwe',SG:'Singapore',MY:'Malaysia',PG:'Papua New Guinea',
    BO:'Bolivia',ET:'Ethiopia',KZ:'Kazakhstan',UZ:'Uzbekistan',TN:'Tunisia',
    DZ:'Algeria',LY:'Libya',SD:'Sudan',AO:'Angola',ZM:'Zambia',MZ:'Mozambique',
    MG:'Madagascar',CM:'Cameroon',CI:"Cote d'Ivoire",SY:'Syria',AF:'Afghanistan',
    BD:'Bangladesh',MM:'Myanmar',NP:'Nepal',KH:'Cambodia',LA:'Laos',
    MN:'Mongolia',AM:'Armenia',GE:'Georgia',AZ:'Azerbaijan',BY:'Belarus',
    BA:'Bosnia and Herzegovina',HR:'Croatia',SK:'Slovakia',SI:'Slovenia',
    RS:'Serbia',MK:'North Macedonia',AL:'Albania',BG:'Bulgaria',LT:'Lithuania',
    LV:'Latvia',EE:'Estonia',IS:'Iceland',MT:'Malta',CY:'Cyprus',LU:'Luxembourg',
    MC:'Monaco',AD:'Andorra',SM:'San Marino',SK:'Slovakia',NL:'Netherlands',
    MX:'Mexico',GT:'Guatemala',BZ:'Belize',HN:'Honduras',SV:'El Salvador',
    NI:'Nicaragua',CR:'Costa Rica',PA:'Panama',CU:'Cuba',JM:'Jamaica',
    HT:'Haiti',TT:'Trinidad and Tobago',BB:'Barbados',DO:'Dominican Republic',
  };

  /**
   * Resolves a 2-letter ISO country code to its English name. First attempts a fetch to
   * restcountries.com; if that fails it looks up the code in the local ISO_PAISES map;
   * if still not found it returns the raw code string.
   * @async
   * @param {string} isoCode - Two-letter ISO country code (e.g. 'GT', 'US').
   * @returns {Promise<string>} The resolved country name or the original code as fallback.
   */
  async function resolverPais(isoCode) {
    if (!isoCode) return '';
    const iso = isoCode.toUpperCase();

    try {
      const r    = await fetch(`https://restcountries.com/v3.1/alpha/${iso}`);
      if (r.ok) {
        const data = await r.json();
        if (data[0]?.name?.common) return data[0].name.common;
      }
    } catch { /* fallback */ }

    if (ISO_PAISES[iso]) return ISO_PAISES[iso];

    return iso;
  }

  /**
   * On mount: triggers parallel loading of the system airport list, the IANA timezone list,
   * and the full IATA airport database.
   * @async
   * @returns {Promise<void>}
   */
  onMount(async () => {
    await Promise.all([cargarAeropuertos(), cargarTimezones(), cargarIATA()]);
  });

  /**
   * Fetches the IATA airport database JSON from the mwgg/Airports GitHub repository and
   * stores it in todosLosAeropuertosIATA. Sets loadingIATA while the request is in flight.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarIATA() {
    loadingIATA = true;
    try {
      const r = await fetch('https://raw.githubusercontent.com/mwgg/Airports/master/airports.json');
      if (r.ok) todosLosAeropuertosIATA = await r.json();
    } catch { console.error('Error cargando base de datos IATA'); }
    finally { loadingIATA = false; }
  }

  /**
   * Called when the user selects an entry from the IATA autocomplete dropdown. Fills in
   * the form fields (codigo, nombre, ciudad, zonaHoraria) from the IATA record and resolves
   * the country name via resolverPais. Closes the dropdown and marks iataSeleccionado as true.
   * @async
   * @param {string} icao - The ICAO key of the selected airport entry.
   * @param {any} ap - The airport data object from the IATA database.
   * @returns {Promise<void>}
   */
  async function seleccionarIATA(icao, ap) {
    const code = ap.iata;
    iataQuery           = code;
    mostrarDropdownIATA = false;
    iataSeleccionado    = true;

    aeropuertoForm.codigo = code;
    aeropuertoForm.nombre = ap.name  || '';

    aeropuertoForm.ciudad = ap.city || ap.state || ap.municipality || '';

    aeropuertoForm.zonaHoraria = ap.tz || '';
    busquedaTimezone = ap.tz || '';

    aeropuertoForm.pais = await resolverPais(ap.country);
  }

  /**
   * Fetches the full list of IANA timezone strings from WorldTimeAPI and stores them in
   * todosLosTimezones. Sets loadingTimezones while the request is in flight.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTimezones() {
    loadingTimezones = true;
    try {
      const r = await fetch('https://worldtimeapi.org/api/timezone');
      if (r.ok) todosLosTimezones = await r.json();
    } catch { console.error('Error al cargar timezones'); }
    finally { loadingTimezones = false; }
  }

  /**
   * Sets the selected timezone on the form and closes the timezone dropdown.
   * @param {string} tz - The IANA timezone string selected by the user (e.g. 'America/Guatemala').
   */
  function seleccionarTimezone(tz) {
    aeropuertoForm.zonaHoraria = tz;
    busquedaTimezone = tz;
    mostrarDropdownTZ = false;
  }

  /**
   * Clears the timezone fields on the form, allowing the user to select a different one.
   */
  function limpiarTimezone() {
    aeropuertoForm.zonaHoraria = '';
    busquedaTimezone = '';
  }

  /**
   * Fetches the list of airports from the backend API and stores them in the aeropuertos array.
   * Shows a toast on error and sets loadingAeropuertos during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAeropuertos() {
    loadingAeropuertos = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos`);
      if (r.ok) aeropuertos = await r.json();
      else mostrarToast('error', 'Error al cargar aeropuertos');
    } catch { mostrarToast('error', 'Error de conexion al cargar aeropuertos'); }
    finally { loadingAeropuertos = false; }
  }

  /**
   * Reads the file selected in the image input, converts it to a base64 data URL and stores
   * it in both aeropuertoImagenBase64 (for submission) and aeropuertoImagenPreview (for display).
   * @param {Event} e - The change event from the file input element.
   */
  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { aeropuertoImagenBase64 = reader.result; aeropuertoImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Resets the form to an empty state and opens the modal in creation mode.
   */
  function abrirNuevo() {
    modoEdicion = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
    busquedaTimezone = ''; mostrarDropdownTZ = false;
    iataQuery = ''; mostrarDropdownIATA = false; iataSeleccionado = false;
    mostrarFormulario = true;
  }

  /**
   * Fetches the full airport record by ID from the backend, pre-fills the form fields and
   * opens the modal in edit mode. Shows a toast on error.
   * @async
   * @param {any} aeropuerto - The airport row object from the table (must have an id property).
   * @returns {Promise<void>}
   */
  async function abrirEditar(aeropuerto) {
    modoEdicion = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuerto.id}`);
      if (r.ok) {
        const c = await r.json();
        aeropuertoForm = {
          id: c.id, codigo: c.codigo, nombre: c.nombre,
          ciudad: c.ciudad, pais: c.pais, zonaHoraria: c.zonaHoraria || ''
        };
        iataQuery        = c.codigo;
        busquedaTimezone = c.zonaHoraria || '';
        iataSeleccionado = true;
        aeropuertoImagenBase64  = null;
        aeropuertoImagenPreview = c.imagenBase64 || null;
        mostrarFormulario = true;
      } else { mostrarToast('error', 'Error al cargar los datos del aeropuerto'); }
    } catch { mostrarToast('error', 'Error al cargar los datos del aeropuerto'); }
  }

  /**
   * Closes the modal and resets all form state, image previews and dropdown states.
   */
  function cerrar() {
    mostrarFormulario = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
    busquedaTimezone = ''; mostrarDropdownTZ = false;
    iataQuery = ''; mostrarDropdownIATA = false; iataSeleccionado = false;
  }

  /**
   * Validates the form fields (IATA code length, nombre, pais, ciudad) then sends a POST or
   * PUT request to the backend. On success reloads the airport list, dispatches
   * 'aeropuertosActualizados', and closes the modal. Shows error toasts for validation
   * failures or API errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardar() {
    if (!aeropuertoForm.codigo || aeropuertoForm.codigo.length !== 3) {
      mostrarToast('error', 'Selecciona un aeropuerto de la lista IATA'); return;
    }
    if (!aeropuertoForm.nombre.trim()) { mostrarToast('error', 'El nombre es obligatorio'); return; }
    if (!aeropuertoForm.pais.trim())   { mostrarToast('error', 'El pais es obligatorio'); return; }
    if (!aeropuertoForm.ciudad.trim()) { mostrarToast('error', 'La ciudad es obligatoria'); return; }
    try {
      const url    = modoEdicion ? `${API}/api/aeropuertos/${aeropuertoForm.id}` : `${API}/api/aeropuertos`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:       aeropuertoForm.nombre,
          codigo:       aeropuertoForm.codigo.toUpperCase(),
          ciudad:       aeropuertoForm.ciudad,
          pais:         aeropuertoForm.pais,
          zonaHoraria:  aeropuertoForm.zonaHoraria?.trim() || null,
          imagenBase64: aeropuertoImagenBase64 || null
        })
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Aeropuerto actualizado' : 'Aeropuerto creado correctamente');
        await cargarAeropuertos();
        dispatch('aeropuertosActualizados');
        cerrar();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el aeropuerto');
      }
    } catch { mostrarToast('error', 'Error de conexion al guardar el aeropuerto'); }
  }

  /**
   * Asks for confirmation and then sends a DELETE request to remove the image from an airport
   * record. On success reloads the airport list and dispatches 'aeropuertosActualizados'.
   * @async
   * @param {number} aeropuertoId - The ID of the airport whose image should be removed.
   * @returns {Promise<void>}
   */
  async function handleEliminarImagen(aeropuertoId) {
    const ok = await mostrarConfirm('¿Quitar la imagen de este aeropuerto?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/aeropuertos/${aeropuertoId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) {
        mostrarToast('success', 'Imagen eliminada');
        await cargarAeropuertos();
        dispatch('aeropuertosActualizados');
      } else { mostrarToast('error', 'Error al eliminar la imagen'); }
    } catch { mostrarToast('error', 'Error de conexion'); }
  }
</script>

<!-- Seccion principal de gestion de aeropuertos -->
<section class="admin-section">
  <!-- Encabezado de seccion con titulo y boton de nuevo aeropuerto -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Aeropuertos</h2>
      <p class="admin-section__subtitle">Administra los aeropuertos del sistema</p>
    </div>
    <button class="btn-add" on:click={abrirNuevo}>
      <span class="btn-add__icon">+</span> Nuevo Aeropuerto
    </button>
  </div>

  <!-- Tabla de aeropuertos registrados con imagen, codigo, nombre, ciudad y pais -->
  {#if loadingAeropuertos}
    <p class="loading-text">Cargando aeropuertos...</p>
  {:else if aeropuertos.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay aeropuertos registrados.</p>
    </div>
  {:else}
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">Imagen</th>
          <th class="table__header">Codigo</th>
          <th class="table__header">Nombre</th>
          <th class="table__header">Ciudad</th>
          <th class="table__header">Pais</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each aeropuertos as a}
          <tr class="table__row">
            <td class="table__cell">
              {#if a.imagenBase64}
                <img src={a.imagenBase64} alt={a.nombre} class="entity-thumb" />
              {:else}
                <span style="color:#9ca3af">—</span>
              {/if}
            </td>
            <td class="table__cell"><strong>{a.codigo}</strong></td>
            <td class="table__cell">{a.nombre}</td>
            <td class="table__cell">{a.ciudad}</td>
            <td class="table__cell">{a.pais}</td>
            <td class="table__cell">
              <div class="table__actions">
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => abrirEditar(a)}>Editar</button>
                {#if a.imagenBase64}
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => handleEliminarImagen(a.id)}>Quitar img</button>
                {/if}
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>

<!-- Modal de creacion y edicion de aeropuerto con busqueda IATA y selector de zona horaria -->
{#if mostrarFormulario}
  <div class="modal-overlay" on:click={cerrar} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:540px">
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Aeropuerto</h3>
        <button class="modal__close" on:click={cerrar}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardar}>

        <!-- Campo de busqueda IATA con autocompletado de la base de datos global -->
        <div class="form-field">
          <label class="form-label">Codigo IATA *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={iataQuery}
              on:input={() => {
                iataQuery = iataQuery.toUpperCase().replace(/[^A-Z]/g, '').slice(0, 3);
                aeropuertoForm.codigo = iataQuery;
                mostrarDropdownIATA = true;
                iataSeleccionado = false;
                aeropuertoForm = { ...aeropuertoForm, nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
                busquedaTimezone = '';
              }}
              on:focus={() => mostrarDropdownIATA = true}
              on:blur={() => setTimeout(() => mostrarDropdownIATA = false, 200)}
              placeholder={loadingIATA ? 'Cargando base de datos IATA...' : 'Escribe codigo o nombre — ej: GUA, Paris, Tokyo'}
              disabled={loadingIATA}
              maxlength="3"
              style="text-transform:uppercase;letter-spacing:3px;font-weight:700;font-size:1.1rem"
              autocomplete="off"
              required />

            {#if mostrarDropdownIATA && iataResultados.length > 0}
              <div class="searchable-select__dropdown">
                {#each iataResultados as [icao, ap]}
                  <button type="button" class="searchable-select__option"
                    on:click={() => seleccionarIATA(icao, ap)}>
                    <span class="searchable-select__option-code">{ap.iata}</span>
                    <span class="searchable-select__option-name">{ap.name}</span>
                    <span class="searchable-select__option-city">{ap.city || ap.state || ''}, {ap.country}</span>
                  </button>
                {/each}
              </div>
            {:else if mostrarDropdownIATA && iataQuery.length >= 1 && iataResultados.length === 0 && !loadingIATA}
              <div class="searchable-select__dropdown">
                <p class="searchable-select__empty">No se encontraron aeropuertos — prueba con otro nombre o codigo</p>
              </div>
            {/if}

            {#if iataSeleccionado}
              <p class="selected-item">✔ {aeropuertoForm.codigo} — {aeropuertoForm.nombre}</p>
            {/if}
          </div>
          <small class="img-hint">
            {loadingIATA
              ? 'Cargando ~9000 aeropuertos reales...'
              : iataSeleccionado
                ? '✔ Aeropuerto seleccionado — campos rellenados automaticamente'
                : 'Escribe el codigo IATA (GUA) o nombre del aeropuerto para buscarlo'}
          </small>
        </div>

        <div class="form-field">
          <label class="form-label">Nombre del Aeropuerto</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.nombre}
            placeholder="Se rellena al seleccionar el codigo IATA"
            readonly tabindex="-1" />
        </div>

        <div class="form-field">
          <label class="form-label">Ciudad</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.ciudad}
            placeholder="Se rellena al seleccionar el codigo IATA"
            readonly tabindex="-1" />
        </div>

        <div class="form-field">
          <label class="form-label">Pais</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.pais}
            placeholder="Se rellena al seleccionar el codigo IATA"
            readonly tabindex="-1" />
        </div>

        <div class="form-field">
          <label for="aap-imagen" class="form-label">Imagen del Aeropuerto</label>
          {#if aeropuertoImagenPreview}
            <img src={aeropuertoImagenPreview} alt="Preview" class="img-preview" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null; }}>
              Quitar imagen
            </button>
          {/if}
          <input id="aap-imagen" type="file" accept="image/*" class="form-input"
            on:change={onImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>

        <!-- Selector de zona horaria IANA con busqueda y autodeteccion desde IATA -->
        <div class="form-field">
          <label class="form-label">Zona Horaria (IANA)</label>

          {#if aeropuertoForm.zonaHoraria && !mostrarDropdownTZ}
            <input type="text" class="form-input iata-readonly"
              value={aeropuertoForm.zonaHoraria}
              readonly tabindex="-1" />
            <button type="button"
              on:click={() => { mostrarDropdownTZ = true; busquedaTimezone = aeropuertoForm.zonaHoraria; }}
              style="background:none;border:none;color:#1e40af;font-size:.75rem;cursor:pointer;margin-top:.25rem;padding:0">
              ✎ Cambiar timezone
            </button>
          {:else}
            <div class="searchable-select">
              <input type="text" class="form-input"
                bind:value={busquedaTimezone}
                on:focus={() => mostrarDropdownTZ = true}
                on:blur={() => setTimeout(() => { mostrarDropdownTZ = false; }, 200)}
                on:input={() => { aeropuertoForm.zonaHoraria = ''; mostrarDropdownTZ = true; }}
                placeholder={loadingTimezones ? 'Cargando timezones...' : 'Buscar timezone — ej: America/Guatemala'}
                disabled={loadingTimezones}
                autocomplete="off" />

              {#if mostrarDropdownTZ && timezonesFiltrados.length > 0}
                <div class="searchable-select__dropdown">
                  {#if busquedaTimezone.length < 2}
                    <div class="searchable-select__hint">
                      {todosLosTimezones.length} timezones — escribe para filtrar
                    </div>
                  {/if}
                  {#each timezonesFiltrados as tz}
                    <button type="button" class="searchable-select__option"
                      on:click={() => seleccionarTimezone(tz)}>
                      <span class="searchable-select__option-name"
                        style="font-family:monospace;font-size:.82rem">{tz}</span>
                    </button>
                  {/each}
                </div>
              {/if}

              {#if aeropuertoForm.zonaHoraria}
                <p class="selected-item">🌐 {aeropuertoForm.zonaHoraria}</p>
              {/if}
            </div>

            {#if aeropuertoForm.zonaHoraria}
              <button type="button" on:click={limpiarTimezone}
                style="background:none;border:none;color:#c62828;font-size:.75rem;cursor:pointer;margin-top:.3rem;padding:0">
                × Limpiar timezone
              </button>
            {/if}
          {/if}

          <small class="img-hint">
            {aeropuertoForm.zonaHoraria
              ? '✔ Timezone detectado automaticamente desde el codigo IATA'
              : 'Este aeropuerto no tiene timezone en la base de datos — seleccionalo manualmente'}
          </small>
        </div>

        <!-- Acciones del formulario: guardar o cancelar -->
        <div class="modal__actions">
          <button type="submit" class="btn-primary" disabled={!iataSeleccionado && !modoEdicion}>
            {modoEdicion ? 'Actualizar' : 'Crear'} Aeropuerto
          </button>
          <button type="button" class="btn-secondary" on:click={cerrar}>Cancelar</button>
        </div>

      </form>
    </div>
  </div>
{/if}
