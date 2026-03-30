<script>
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  export let API;
  export let mostrarToast;
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  let aeropuertos        = [];
  let loadingAeropuertos = false;
  let modoEdicion        = false;

  let mostrarFormulario       = false;
  let aeropuertoForm          = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
  let aeropuertoImagenPreview = null;
  let aeropuertoImagenBase64  = null;

  // ── Base de datos IATA real ──────────────────────────────────────
  let todosLosAeropuertosIATA = {};
  let loadingIATA             = false;
  let iataQuery               = '';
  let mostrarDropdownIATA     = false;
  let iataSeleccionado        = false;

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

  // ── Timezone searchable ──────────────────────────────────────────
  let todosLosTimezones  = [];
  let busquedaTimezone   = '';
  let mostrarDropdownTZ  = false;
  let loadingTimezones   = false;

  $: timezonesFiltrados = busquedaTimezone.length < 2
    ? todosLosTimezones.slice(0, 6)
    : todosLosTimezones
        .filter(tz => tz.toLowerCase().includes(busquedaTimezone.toLowerCase()))
        .slice(0, 20);

  // Mapa de ISO-2 a nombre de país — fallback cuando restcountries falla
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

  async function resolverPais(isoCode) {
    if (!isoCode) return '';
    const iso = isoCode.toUpperCase();

    // 1. Intentar con restcountries.com
    try {
      const r    = await fetch(`https://restcountries.com/v3.1/alpha/${iso}`);
      if (r.ok) {
        const data = await r.json();
        if (data[0]?.name?.common) return data[0].name.common;
      }
    } catch { /* fallback */ }

    // 2. Fallback: mapa local ISO → nombre
    if (ISO_PAISES[iso]) return ISO_PAISES[iso];

    // 3. Último fallback: devolver el código tal cual
    return iso;
  }

  onMount(async () => {
    await Promise.all([cargarAeropuertos(), cargarTimezones(), cargarIATA()]);
  });

  async function cargarIATA() {
    loadingIATA = true;
    try {
      const r = await fetch('https://raw.githubusercontent.com/mwgg/Airports/master/airports.json');
      if (r.ok) todosLosAeropuertosIATA = await r.json();
    } catch { console.error('Error cargando base de datos IATA'); }
    finally { loadingIATA = false; }
  }

  async function seleccionarIATA(icao, ap) {
    const code = ap.iata;
    iataQuery           = code;
    mostrarDropdownIATA = false;
    iataSeleccionado    = true;

    aeropuertoForm.codigo = code;
    aeropuertoForm.nombre = ap.name  || '';

    // ciudad: usar city, si vacío usar state, si vacío usar municipio del nombre
    aeropuertoForm.ciudad = ap.city || ap.state || ap.municipality || '';

    // timezone directo del JSON
    aeropuertoForm.zonaHoraria = ap.tz || '';
    busquedaTimezone = ap.tz || '';

    // país con fallbacks
    aeropuertoForm.pais = await resolverPais(ap.country);
  }

  async function cargarTimezones() {
    loadingTimezones = true;
    try {
      const r = await fetch('https://worldtimeapi.org/api/timezone');
      if (r.ok) todosLosTimezones = await r.json();
    } catch { console.error('Error al cargar timezones'); }
    finally { loadingTimezones = false; }
  }

  function seleccionarTimezone(tz) {
    aeropuertoForm.zonaHoraria = tz;
    busquedaTimezone = tz;
    mostrarDropdownTZ = false;
  }

  function limpiarTimezone() {
    aeropuertoForm.zonaHoraria = '';
    busquedaTimezone = '';
  }

  async function cargarAeropuertos() {
    loadingAeropuertos = true;
    try {
      const r = await fetch(`${API}/api/aeropuertos`);
      if (r.ok) aeropuertos = await r.json();
      else mostrarToast('error', 'Error al cargar aeropuertos');
    } catch { mostrarToast('error', 'Error de conexión al cargar aeropuertos'); }
    finally { loadingAeropuertos = false; }
  }

  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { aeropuertoImagenBase64 = reader.result; aeropuertoImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  function abrirNuevo() {
    modoEdicion = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
    busquedaTimezone = ''; mostrarDropdownTZ = false;
    iataQuery = ''; mostrarDropdownIATA = false; iataSeleccionado = false;
    mostrarFormulario = true;
  }

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

  function cerrar() {
    mostrarFormulario = false;
    aeropuertoForm = { id: null, codigo: '', nombre: '', ciudad: '', pais: '', zonaHoraria: '' };
    aeropuertoImagenPreview = null; aeropuertoImagenBase64 = null;
    busquedaTimezone = ''; mostrarDropdownTZ = false;
    iataQuery = ''; mostrarDropdownIATA = false; iataSeleccionado = false;
  }

  async function handleGuardar() {
    if (!aeropuertoForm.codigo || aeropuertoForm.codigo.length !== 3) {
      mostrarToast('error', 'Selecciona un aeropuerto de la lista IATA'); return;
    }
    if (!aeropuertoForm.nombre.trim()) { mostrarToast('error', 'El nombre es obligatorio'); return; }
    if (!aeropuertoForm.pais.trim())   { mostrarToast('error', 'El país es obligatorio'); return; }
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
    } catch { mostrarToast('error', 'Error de conexión al guardar el aeropuerto'); }
  }

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
    } catch { mostrarToast('error', 'Error de conexión'); }
  }
</script>

<section class="admin-section">
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Aeropuertos</h2>
      <p class="admin-section__subtitle">Administra los aeropuertos del sistema</p>
    </div>
    <button class="btn-add" on:click={abrirNuevo}>
      <span class="btn-add__icon">+</span> Nuevo Aeropuerto
    </button>
  </div>

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
          <th class="table__header">Código</th>
          <th class="table__header">Nombre</th>
          <th class="table__header">Ciudad</th>
          <th class="table__header">País</th>
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

{#if mostrarFormulario}
  <div class="modal-overlay" on:click={cerrar} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:540px">
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Aeropuerto</h3>
        <button class="modal__close" on:click={cerrar}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardar}>

        <!-- ── Código IATA ── -->
        <div class="form-field">
          <label class="form-label">Código IATA *</label>
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
              placeholder={loadingIATA ? 'Cargando base de datos IATA...' : 'Escribe código o nombre — ej: GUA, Paris, Tokyo'}
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
                <p class="searchable-select__empty">No se encontraron aeropuertos — prueba con otro nombre o código</p>
              </div>
            {/if}

            {#if iataSeleccionado}
              <p class="selected-item">✔ {aeropuertoForm.codigo} — {aeropuertoForm.nombre}</p>
            {/if}
          </div>
          <small class="img-hint">
            {loadingIATA
              ? '⏳ Cargando ~9000 aeropuertos reales...'
              : iataSeleccionado
                ? '✔ Aeropuerto seleccionado — campos rellenados automáticamente'
                : 'Escribe el código IATA (GUA) o nombre del aeropuerto para buscarlo'}
          </small>
        </div>

        <!-- ── Campos solo lectura ── -->
        <div class="form-field">
          <label class="form-label">Nombre del Aeropuerto</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.nombre}
            placeholder="Se rellena al seleccionar el código IATA"
            readonly tabindex="-1" />
        </div>

        <div class="form-field">
          <label class="form-label">Ciudad</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.ciudad}
            placeholder="Se rellena al seleccionar el código IATA"
            readonly tabindex="-1" />
        </div>

        <div class="form-field">
          <label class="form-label">País</label>
          <input type="text" class="form-input iata-readonly"
            value={aeropuertoForm.pais}
            placeholder="Se rellena al seleccionar el código IATA"
            readonly tabindex="-1" />
        </div>

        <!-- ── Imagen ── -->
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
          <small class="img-hint">JPG, PNG o WEBP. Máx recomendado: 1 MB.</small>
        </div>

        <!-- ── Zona horaria — editable solo si vino vacía del IATA ── -->
        <div class="form-field">
          <label class="form-label">Zona Horaria (IANA)</label>

          {#if aeropuertoForm.zonaHoraria && !mostrarDropdownTZ}
            <!-- Ya tiene timezone del JSON IATA — solo lectura con opción de cambiar -->
            <input type="text" class="form-input iata-readonly"
              value={aeropuertoForm.zonaHoraria}
              readonly tabindex="-1" />
            <button type="button"
              on:click={() => { mostrarDropdownTZ = true; busquedaTimezone = aeropuertoForm.zonaHoraria; }}
              style="background:none;border:none;color:#1e40af;font-size:.75rem;cursor:pointer;margin-top:.25rem;padding:0">
              ✎ Cambiar timezone
            </button>
          {:else}
            <!-- Sin timezone o cambiando — mostrar searchable -->
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
              ? '✔ Timezone detectado automáticamente desde el código IATA'
              : '⚠ Este aeropuerto no tiene timezone en la base de datos — selecciónalo manualmente'}
          </small>
        </div>

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