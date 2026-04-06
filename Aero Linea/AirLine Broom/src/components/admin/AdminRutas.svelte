<script>
/**
 * @file AdminRutas.svelte
 * @description Admin panel section for managing flight routes between airports. Displays a table
 * of all registered routes showing origin, destination, timezone availability for each airport,
 * estimated duration, and total flight count. The estimated duration of any route can be edited
 * inline directly in the table row. New routes can be created through a modal with searchable
 * airport dropdowns that exclude the already-selected origin from the destination list. The
 * component receives the airports list from the parent and dispatches 'rutaCreada' after a
 * successful route creation.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** List of all airports, provided by the parent, used to populate origin and destination dropdowns. @type {any[]} */
  export let aeropuertos = [];

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  const dispatch = createEventDispatcher();

  /** List of routes loaded from the backend. @type {any[]} */
  let rutas        = [];

  /** Whether the route list fetch is in progress. @type {boolean} */
  let loadingRutas = false;

  /** ID of the route row currently in inline duration-edit mode, or null if none. @type {number|null} */
  let editandoRutaId   = null;

  /** Current value of the duration input while editing inline. @type {string} */
  let rutaDuracionEdit = '';

  /** Whether the save-duration API request is in flight. @type {boolean} */
  let guardandoDuracion = false;

  /** Whether the create-route modal is visible. @type {boolean} */
  let mostrarModalCrearRuta = false;

  /**
   * Form data for the new route being created in the modal.
   * @type {{ origenId: string, destinoId: string, duracion: number }}
   */
  let nuevaRuta  = { origenId: '', destinoId: '', duracion: 120 };

  /** Whether the create-route API request is in flight. @type {boolean} */
  let creandoRuta = false;

  /** Current text in the origin airport search input inside the modal. @type {string} */
  let busquedaOrigenModal  = '';

  /** Current text in the destination airport search input inside the modal. @type {string} */
  let busquedaDestinoModal = '';

  /** Whether the origin airport dropdown inside the modal is open. @type {boolean} */
  let mostrarDropdownOrigenModal  = false;

  /** Whether the destination airport dropdown inside the modal is open. @type {boolean} */
  let mostrarDropdownDestinoModal = false;

  // Filters airports for the origin dropdown in the modal.
  // Shows first 5 when query is shorter than 2 chars; otherwise filters by name, code or city.
  $: aeropuertosFiltradosOrigenModal = busquedaOrigenModal.length < 2
    ? aeropuertos.slice(0, 5)
    : aeropuertos.filter(a =>
        a.nombre.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.codigo.toLowerCase().includes(busquedaOrigenModal.toLowerCase()) ||
        a.ciudad.toLowerCase().includes(busquedaOrigenModal.toLowerCase())
      ).slice(0, 10);

  // Filters airports for the destination dropdown in the modal.
  // Excludes the currently selected origin airport. Shows first 5 when query is shorter than 2.
  $: aeropuertosFiltradosDestinoModal = busquedaDestinoModal.length < 2
    ? aeropuertos.filter(a => a.id !== parseInt(nuevaRuta.origenId)).slice(0, 5)
    : aeropuertos.filter(a =>
        a.id !== parseInt(nuevaRuta.origenId) && (
          a.nombre.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.codigo.toLowerCase().includes(busquedaDestinoModal.toLowerCase()) ||
          a.ciudad.toLowerCase().includes(busquedaDestinoModal.toLowerCase())
        )
      ).slice(0, 10);

  // Resolves the currently selected origin airport object for the confirmation label.
  $: aeropuertoOrigenSeleccionado  = aeropuertos.find(a => a.id === parseInt(nuevaRuta.origenId));

  // Resolves the currently selected destination airport object for the confirmation label.
  $: aeropuertoDestinoSeleccionado = aeropuertos.find(a => a.id === parseInt(nuevaRuta.destinoId));

  /**
   * Sets the origin airport from the modal dropdown, updates the search text, closes the
   * dropdown, and clears the destination if it was the same airport.
   * @param {any} a - The airport object selected from the origin dropdown.
   */
  function seleccionarOrigenModal(a) {
    nuevaRuta.origenId = a.id;
    busquedaOrigenModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownOrigenModal = false;
    if (parseInt(nuevaRuta.destinoId) === a.id) {
      nuevaRuta.destinoId = '';
      busquedaDestinoModal = '';
    }
  }

  /**
   * Sets the destination airport from the modal dropdown and closes the dropdown.
   * @param {any} a - The airport object selected from the destination dropdown.
   */
  function seleccionarDestinoModal(a) {
    nuevaRuta.destinoId = a.id;
    busquedaDestinoModal = `${a.codigo} — ${a.nombre}`;
    mostrarDropdownDestinoModal = false;
  }

  /**
   * On mount: loads the route list from the backend.
   */
  onMount(() => { cargarRutas(); });

  /**
   * Fetches all routes from the backend API and stores them in rutas. Shows a toast on error
   * and sets loadingRutas during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarRutas() {
    loadingRutas = true;
    try {
      const r = await fetch(`${API}/api/rutas`, { credentials: 'include' });
      if (r.ok) rutas = await r.json();
      else mostrarToast('error', 'Error al cargar rutas');
    } catch { mostrarToast('error', 'Error de conexion al cargar rutas'); }
    finally { loadingRutas = false; }
  }

  /**
   * Validates the modal form (origin, destination, valid duration, origin != destination) then
   * POSTs the new route to the backend. On success closes the modal, resets form state,
   * reloads routes and dispatches 'rutaCreada'. Shows error toasts for validation or API errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearRuta() {
    if (!nuevaRuta.origenId)  { mostrarToast('error', 'Selecciona el aeropuerto de origen'); return; }
    if (!nuevaRuta.destinoId) { mostrarToast('error', 'Selecciona el aeropuerto de destino'); return; }
    if (nuevaRuta.origenId === nuevaRuta.destinoId) { mostrarToast('error', 'El origen y destino no pueden ser el mismo'); return; }
    if (!nuevaRuta.duracion || nuevaRuta.duracion <= 0) { mostrarToast('error', 'Ingresa una duracion valida'); return; }

    creandoRuta = true;
    try {
      const r = await fetch(`${API}/api/rutas`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origenId:         parseInt(nuevaRuta.origenId),
          destinoId:        parseInt(nuevaRuta.destinoId),
          duracionEstimada: parseInt(nuevaRuta.duracion)
        })
      });
      if (r.ok) {
        mostrarToast('success', '¡Ruta creada correctamente!');
        mostrarModalCrearRuta = false;
        nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
        busquedaOrigenModal = ''; busquedaDestinoModal = '';
        await cargarRutas();
        dispatch('rutaCreada');
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al crear la ruta');
      }
    } catch { mostrarToast('error', 'Error de conexion al crear la ruta'); }
    finally { creandoRuta = false; }
  }

  /**
   * Validates that the inline-edited duration is a positive integer, then PUTs the updated
   * value to the backend. On success resets the inline edit state and reloads routes.
   * Shows error toasts for validation or API errors.
   * @async
   * @param {number} rutaId - The ID of the route whose duration is being updated.
   * @returns {Promise<void>}
   */
  async function guardarDuracionRuta(rutaId) {
    const minutos = parseInt(rutaDuracionEdit);
    if (!minutos || minutos <= 0) { mostrarToast('error', 'La duracion debe ser mayor a 0 minutos'); return; }
    guardandoDuracion = true;
    try {
      const r = await fetch(`${API}/api/rutas/${rutaId}/duracion`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ duracionEstimada: minutos })
      });
      if (r.ok) {
        mostrarToast('success', 'Duracion actualizada correctamente');
        editandoRutaId = null; rutaDuracionEdit = '';
        await cargarRutas();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al actualizar la duracion');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { guardandoDuracion = false; }
  }

  /**
   * Resets the create-route form and opens the modal.
   */
  function abrirModalCrearRuta() {
    nuevaRuta = { origenId: '', destinoId: '', duracion: 120 };
    busquedaOrigenModal = ''; busquedaDestinoModal = '';
    mostrarModalCrearRuta = true;
  }
</script>

<!-- Seccion de gestion de rutas con tabla de duraciones editables inline -->
<section class="admin-section">
  <!-- Encabezado con descripcion de zonas horarias y botones de accion -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Rutas</h2>
      <p class="admin-section__subtitle">
        Edita la duracion estimada en minutos de cada ruta.
        La hora de llegada se calculara automaticamente usando las zonas horarias de cada aeropuerto.
      </p>
    </div>
    <div style="display:flex;gap:.75rem">
      <button class="btn-add" on:click={abrirModalCrearRuta}>+ Nueva Ruta</button>
      <button class="btn-add" on:click={cargarRutas} style="background:#4b5563">↻ Actualizar</button>
    </div>
  </div>

  {#if loadingRutas}
    <p class="loading-text">Cargando rutas...</p>

  {:else if rutas.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">
        No hay rutas registradas. Crea una ruta con el boton <strong>+ Nueva Ruta</strong>,
        o selecciona aeropuertos al crear un vuelo para generarla automaticamente.
      </p>
    </div>

  {:else}
    <!-- Aviso informativo sobre el calculo de llegada con zona horaria -->
    <div class="rutas-tz-note">
      <span>💡</span>
      <span>
        Las rutas con <strong>✔ TZ</strong> en ambos aeropuertos calcularan la hora de llegada
        con conversion de zona horaria real. Si algun aeropuerto no tiene timezone,
        editalo en <em>Gestionar Aeropuertos</em>.
      </span>
    </div>

    <!-- Tabla de rutas con zonas horarias, duracion editable inline y total de vuelos -->
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">Origen</th>
          <th class="table__header">Destino</th>
          <th class="table__header">TZ Origen</th>
          <th class="table__header">TZ Destino</th>
          <th class="table__header">Duracion (min)</th>
          <th class="table__header">Vuelos</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each rutas as ruta}
          <tr class="table__row">
            <td class="table__cell">
              <span class="ruta-code">{ruta.codigoOrigen}</span>
              <span class="ruta-name">{ruta.origen}</span>
            </td>
            <td class="table__cell">
              <span class="ruta-code">{ruta.codigoDestino}</span>
              <span class="ruta-name">{ruta.destino}</span>
            </td>
            <td class="table__cell">
              {#if ruta.zonaHorariaOrigen}
                <span class="tz-badge tz-badge--ok">✔ {ruta.zonaHorariaOrigen}</span>
              {:else}
                <span class="tz-badge tz-badge--missing">Sin TZ</span>
              {/if}
            </td>
            <td class="table__cell">
              {#if ruta.zonaHorariaDestino}
                <span class="tz-badge tz-badge--ok">✔ {ruta.zonaHorariaDestino}</span>
              {:else}
                <span class="tz-badge tz-badge--missing">Sin TZ</span>
              {/if}
            </td>
            <td class="table__cell">
              {#if editandoRutaId === ruta.id}
                <div class="duracion-edit">
                  <input type="number" class="form-input duracion-input" min="1" max="10000"
                    bind:value={rutaDuracionEdit} placeholder="min" />
                  <button class="table__action-btn table__action-btn--view"
                    disabled={guardandoDuracion}
                    on:click={() => guardarDuracionRuta(ruta.id)}>
                    {guardandoDuracion ? '...' : '✔'}
                  </button>
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => { editandoRutaId = null; rutaDuracionEdit = ''; }}>✕</button>
                </div>
              {:else}
                <span class="duracion-display">
                  <strong>{ruta.duracionEstimada}</strong> min
                  ({Math.floor(ruta.duracionEstimada / 60)}h {ruta.duracionEstimada % 60}m)
                </span>
              {/if}
            </td>
            <td class="table__cell">{ruta.totalVuelos}</td>
            <td class="table__cell">
              {#if editandoRutaId !== ruta.id}
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => { editandoRutaId = ruta.id; rutaDuracionEdit = String(ruta.duracionEstimada); }}>
                  ✎ Editar duracion
                </button>
              {/if}
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>

<!-- Modal de creacion de nueva ruta con dropdowns de aeropuertos y duracion estimada -->
{#if mostrarModalCrearRuta}
  <div class="modal-overlay" on:click={() => mostrarModalCrearRuta = false}
    role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation style="max-width:480px">
      <div class="modal__header">
        <h3 class="modal__title">Crear Nueva Ruta</h3>
        <button class="modal__close" on:click={() => mostrarModalCrearRuta = false}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleCrearRuta}>
        <p style="font-size:.88rem;color:var(--text-muted);margin-bottom:.5rem">
          Una ruta define el trayecto entre dos aeropuertos y su duracion estimada.
        </p>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Origen *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaOrigenModal}
              on:focus={() => mostrarDropdownOrigenModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownOrigenModal = false, 200)}
              placeholder="Buscar aeropuerto de origen..."
              autocomplete="off" />
            {#if mostrarDropdownOrigenModal && aeropuertosFiltradosOrigenModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaOrigenModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosOrigenModal as a}
                  <button type="button" class="searchable-select__option"
                    on:click={() => seleccionarOrigenModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoOrigenSeleccionado}
              <p class="selected-item">✔ {aeropuertoOrigenSeleccionado.codigo} — {aeropuertoOrigenSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label class="form-label">Aeropuerto de Destino *</label>
          <div class="searchable-select">
            <input type="text" class="form-input"
              bind:value={busquedaDestinoModal}
              on:focus={() => mostrarDropdownDestinoModal = true}
              on:blur={() => setTimeout(() => mostrarDropdownDestinoModal = false, 200)}
              placeholder="Buscar aeropuerto de destino..."
              autocomplete="off" />
            {#if mostrarDropdownDestinoModal && aeropuertosFiltradosDestinoModal.length > 0}
              <div class="searchable-select__dropdown">
                {#if busquedaDestinoModal.length < 2}
                  <div class="searchable-select__hint">Mostrando los primeros 5 — escribe para filtrar</div>
                {/if}
                {#each aeropuertosFiltradosDestinoModal as a}
                  <button type="button" class="searchable-select__option"
                    on:click={() => seleccionarDestinoModal(a)}>
                    <span class="searchable-select__option-code">{a.codigo}</span>
                    <span class="searchable-select__option-name">{a.nombre}</span>
                    <span class="searchable-select__option-city">{a.ciudad}</span>
                  </button>
                {/each}
              </div>
            {/if}
            {#if aeropuertoDestinoSeleccionado}
              <p class="selected-item">✔ {aeropuertoDestinoSeleccionado.codigo} — {aeropuertoDestinoSeleccionado.nombre}</p>
            {/if}
          </div>
        </div>

        <div class="form-field">
          <label for="ar-duracion" class="form-label">Duracion Estimada (minutos) *</label>
          <input id="ar-duracion" type="number" class="form-input"
            bind:value={nuevaRuta.duracion}
            min="1" max="10000" placeholder="Ej: 180 para 3 horas" required />
          {#if nuevaRuta.duracion > 0}
            <small class="img-hint">
              ≈ {Math.floor(nuevaRuta.duracion / 60)}h {nuevaRuta.duracion % 60}m
            </small>
          {/if}
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary" disabled={creandoRuta}>
            {creandoRuta ? 'Creando...' : 'Crear Ruta'}
          </button>
          <button type="button" class="btn-secondary"
            on:click={() => mostrarModalCrearRuta = false}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
