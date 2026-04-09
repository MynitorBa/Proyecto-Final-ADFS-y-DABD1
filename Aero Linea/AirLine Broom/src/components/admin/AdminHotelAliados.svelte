<script>
/**
 * @file AdminHotelAliados.svelte
 * @description Admin panel section for managing hotel aliados. Displays a summary stats bar
 * (total, active, inactive, without assigned user) and a table of all hotel aliados. Provides
 * three modal dialogs: one to create a new hotel (with name, API URL, public URL and webservice
 * user), one to assign an available webservice user to an existing hotel, and one to edit both
 * URLs of a hotel. Hotel status can be changed inline through a select element in the table row.
 * A handshake button triggers the authentication flow between the airline and the hotel aliado,
 * generating and storing a session token in HotelAliado.TokenHASH.
 * All mutations call the backend API and refresh the local state on success.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Function to show a confirmation dialog. Signature: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  /** List of hotel aliados loaded from the backend. @type {any[]} */
  let hoteles            = [];

  /** Webservice-role users that have no entity assigned yet, used to populate the user selectors. @type {any[]} */
  let usuariosDisponibles = [];

  /** Whether the main data fetch is in progress. @type {boolean} */
  let cargando           = false;

  // ── Create modal state ────────────────────────────────────────────────

  /** Controls visibility of the create-hotel modal. @type {boolean} */
  let modalCrear         = false;

  /** Whether the create-hotel API request is in flight. @type {boolean} */
  let creando            = false;

  /** Hotel name field value for the create form. @type {string} */
  let crearNombre        = '';

  /** API URL field value for the create form. @type {string} */
  let crearUrl           = '';

  /** Public URL field value for the create form. @type {string} */
  let crearUrlParaUsuario = '';

  /** Selected webservice user ID for the create form. @type {string} */
  let crearUsuarioId     = '';

  /** Field-level validation errors for the create form. @type {Record<string, string>} */
  let crearErrores       = {};

  // ── Assign-user modal state ───────────────────────────────────────────

  /** Controls visibility of the assign-user modal. @type {boolean} */
  let modalAsignar       = false;

  /** Whether the assign-user API request is in flight. @type {boolean} */
  let asignando          = false;

  /** The hotel row currently selected for user assignment. @type {any} */
  let hotelSeleccionado  = null;

  /** Selected webservice user ID in the assign-user modal. @type {string} */
  let asignarUsuarioId   = '';

  // ── Edit-URLs modal state ─────────────────────────────────────────────

  /** Controls visibility of the edit-URLs modal. @type {boolean} */
  let modalUrls          = false;

  /** Whether the save-URLs API request is in flight. @type {boolean} */
  let guardandoUrls      = false;

  /** API URL value being edited in the URLs modal. @type {string} */
  let urlEditando        = '';

  /** Public URL value being edited in the URLs modal. @type {string} */
  let urlParaUsuarioEditando = '';

  /** The hotel row being edited in the URLs modal. @type {any} */
  let hotelUrls          = null;

  // ── Handshake state ───────────────────────────────────────────────────

  /**
   * ID del hotel cuyo handshake esta en curso. Null cuando ninguno esta activo.
   * Se usa para deshabilitar el boton del hotel especifico durante la operacion.
   * @type {number|null}
   */
  let handshakeEnCurso   = null;

  /**
   * Status option definitions used to render the inline status select and badge styles.
   * Mirrors the EstadoAliado catalog in the database.
   * @type {{ id: number, label: string }[]}
   */
  const estadoOpciones = [
    { id: 1, label: 'Activo'     },
    { id: 2, label: 'Inactivo'   },
    { id: 3, label: 'Suspendido' },
  ];

  /**
   * On mount: loads hotel aliados and available webservice users in parallel.
   */
  onMount(() => { cargarTodo(); });

  /**
   * Fetches the full hotel aliado list and the list of unassigned webservice users in parallel.
   * Updates hoteles and usuariosDisponibles on success. Shows toasts on errors.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTodo() {
    cargando = true;
    try {
      const [rHoteles, rUsuarios] = await Promise.all([
        fetch(`${API}/api/hoteles-aliados/todas`,            { credentials: 'include' }),
        fetch(`${API}/api/agencias/webservice-disponibles`,  { credentials: 'include' }),
      ]);
      if (rHoteles.ok)  hoteles            = await rHoteles.json();
      else mostrarToast('error', 'Error al cargar hoteles.');
      if (rUsuarios.ok) usuariosDisponibles = await rUsuarios.json();
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally  { cargando = false; }
  }

  /**
   * Resets the create-hotel form fields and errors, then opens the create modal.
   */
  function abrirModalCrear() {
    crearNombre = ''; crearUrl = ''; crearUrlParaUsuario = ''; crearUsuarioId = '';
    crearErrores = {};
    modalCrear = true;
  }

  /**
   * Validates all fields in the create-hotel form. Populates crearErrores with any
   * field-level messages and returns false if validation fails.
   * @returns {boolean} True when all fields are valid.
   */
  function validarCrear() {
    crearErrores = {};
    if (!crearNombre.trim()) crearErrores.nombre = 'Requerido.';
    if (!crearUrl.trim()) crearErrores.url = 'Requerido.';
    else if (!/^https?:\/\/.+/.test(crearUrl.trim())) crearErrores.url = 'URL invalida (http:// o https://).';
    if (!crearUrlParaUsuario.trim()) crearErrores.urlParaUsuario = 'Requerido.';
    else if (!/^https?:\/\/.+/.test(crearUrlParaUsuario.trim())) crearErrores.urlParaUsuario = 'URL invalida (http:// o https://).';
    if (!crearUsuarioId) crearErrores.usuario = 'Debes seleccionar un usuario Webservice.';
    return Object.keys(crearErrores).length === 0;
  }

  /**
   * Validates the create form and, if valid, POSTs the new hotel to the backend.
   * On success closes the modal and reloads all data. On failure shows an error toast.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearHotel() {
    if (!validarCrear()) return;
    creando = true;
    try {
      const r = await fetch(`${API}/api/hoteles-aliados`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:         crearNombre.trim(),
          url:            crearUrl.trim(),
          urlParaUsuario: crearUrlParaUsuario.trim(),
          usuarioWEBIs:   parseInt(crearUsuarioId),
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Hotel aliado creado correctamente.');
        modalCrear = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al crear el hotel.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { creando = false; }
  }

  /**
   * Sets the selected hotel and resets the user selector, then opens the assign-user modal.
   * @param {any} hotel - The hotel row object from the table.
   */
  function abrirModalAsignar(hotel) {
    hotelSeleccionado = hotel;
    asignarUsuarioId  = '';
    modalAsignar      = true;
  }

  /**
   * Validates that a user is selected and PUTs the assignment to the backend. On success
   * closes the modal and reloads all data. Shows validation or error toasts as needed.
   * @async
   * @returns {Promise<void>}
   */
  async function handleAsignarUsuario() {
    if (!asignarUsuarioId) { mostrarToast('error', 'Selecciona un usuario.'); return; }
    asignando = true;
    try {
      const r = await fetch(`${API}/api/hoteles-aliados/${hotelSeleccionado.id}/asignar-usuario`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioId: parseInt(asignarUsuarioId) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Usuario asignado correctamente.');
        modalAsignar = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al asignar usuario.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { asignando = false; }
  }

  /**
   * Pre-fills the URLs modal with the hotel's current URLs and opens it.
   * @param {any} hotel - The hotel row object from the table.
   */
  function abrirModalUrls(hotel) {
    hotelUrls              = hotel;
    urlEditando            = hotel.url            ?? '';
    urlParaUsuarioEditando = hotel.urlParaUsuario ?? '';
    modalUrls              = true;
  }

  /**
   * Validates both URL values and PUTs the updated URLs to the backend.
   * On success closes the modal and reloads all data. Shows error toasts on failures.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardarUrls() {
    if (!urlEditando.trim())            { mostrarToast('error', 'La URL de la API no puede estar vacia.'); return; }
    if (!/^https?:\/\/.+/.test(urlEditando.trim())) { mostrarToast('error', 'URL de API invalida.'); return; }
    if (!urlParaUsuarioEditando.trim()) { mostrarToast('error', 'La URL publica no puede estar vacia.'); return; }
    if (!/^https?:\/\/.+/.test(urlParaUsuarioEditando.trim())) { mostrarToast('error', 'URL publica invalida.'); return; }
    guardandoUrls = true;
    try {
      const r = await fetch(`${API}/api/hoteles-aliados/${hotelUrls.id}/url`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          url:            urlEditando.trim(),
          urlParaUsuario: urlParaUsuarioEditando.trim()
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'URLs actualizadas.');
        modalUrls = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al actualizar URLs.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { guardandoUrls = false; }
  }

  /**
   * Sends a PUT request to change a hotel's status. On success updates the local hoteles
   * array optimistically without a full reload. On failure reloads the full list to revert.
   * @async
   * @param {any} hotel - The hotel row object whose status is being changed.
   * @param {string|number} nuevoEstadoId - The new status ID to apply.
   * @returns {Promise<void>}
   */
  async function handleCambiarEstado(hotel, nuevoEstadoId) {
    try {
      const r = await fetch(`${API}/api/hoteles-aliados/${hotel.id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: parseInt(nuevoEstadoId) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Estado actualizado.');
        hoteles = hoteles.map(h => h.id === hotel.id ? { ...h, estadoID: parseInt(nuevoEstadoId) } : h);
      } else {
        mostrarToast('error', data.message || 'Error al cambiar estado.');
        await cargarTodo();
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
  }

  /**
   * Initiates the handshake authentication flow between the airline and the selected hotel aliado.
   * Calls POST /api/hoteles-aliados/{id}/handshake on the backend, which generates a token,
   * sends it to the hotel's /api/aerolineas/handshake endpoint and stores the session token
   * in HotelAliado.TokenHASH. The hotel must have its URL configured before calling this.
   * @async
   * @param {any} hotel - The hotel row object from the table.
   * @returns {Promise<void>}
   */
  async function handleHandshake(hotel) {
    const ok = await mostrarConfirm(
      `¿Iniciar handshake con "${hotel.nombre}"?`,
      'Se generara un nuevo token de sesion y sobreescribira el anterior si existe.',
      'warning'
    );
    if (!ok) return;

    handshakeEnCurso = hotel.id;
    try {
      const r = await fetch(`${API}/api/hoteles-aliados/${hotel.id}/handshake`, {
        method: 'POST', credentials: 'include',
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', `Handshake exitoso con "${hotel.nombre}". Token guardado.`);
      } else {
        mostrarToast('error', data.message || 'Error al realizar el handshake.');
      }
    } catch { mostrarToast('error', 'Error de conexion al intentar el handshake.'); }
    finally { handshakeEnCurso = null; }
  }

</script>

<!-- Modal de creacion de nuevo hotel aliado con nombre, URLs y usuario webservice -->
{#if modalCrear}
  <div class="ag-overlay" on:click={() => modalCrear = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Nuevo Hotel Aliado</h3>
        <button class="ag-modal__close" on:click={() => modalCrear = false}>×</button>
      </div>

      <div class="ag-modal__body">
        <div class="ag-field">
          <label class="ag-field__label">Nombre del hotel <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.nombre}
            type="text" bind:value={crearNombre} placeholder="Hotel Las Palmas" maxlength="120" />
          {#if crearErrores.nombre}<p class="ag-field__err">{crearErrores.nombre}</p>{/if}
        </div>

        <!-- URL base de la API del hotel para que la aerolinea se comunique con el -->
        <div class="ag-field">
          <label class="ag-field__label">URL de la API del hotel <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.url}
            type="url" bind:value={crearUrl} placeholder="https://api.mi-hotel.com" maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL interna usada por la aerolinea para consultar disponibilidad y hacer handshake.
          </p>
          {#if crearErrores.url}<p class="ag-field__err">{crearErrores.url}</p>{/if}
        </div>

        <!-- URL publica del hotel que se mostrara a los pasajeros en los resultados -->
        <div class="ag-field">
          <label class="ag-field__label">URL publica para usuarios <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.urlParaUsuario}
            type="url" bind:value={crearUrlParaUsuario} placeholder="https://www.mi-hotel.com" maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL visible para los pasajeros en los resultados de busqueda.
          </p>
          {#if crearErrores.urlParaUsuario}<p class="ag-field__err">{crearErrores.urlParaUsuario}</p>{/if}
        </div>

        <div class="ag-field">
          <label class="ag-field__label">
            Usuario Webservice <span class="ag-required">*</span>
          </label>
          {#if usuariosDisponibles.length === 0}
            <div class="ag-notice ag-notice--warn">
              No hay usuarios Webservice disponibles sin entidad asignada.
            </div>
          {:else}
            <select class="ag-field__select" class:ag-field__input--err={crearErrores.usuario}
              bind:value={crearUsuarioId}>
              <option value="">— Seleccionar usuario —</option>
              {#each usuariosDisponibles as u}
                <option value={u.id}>{u.nombre} (@{u.username}) · {u.correo}</option>
              {/each}
            </select>
          {/if}
          {#if crearErrores.usuario}<p class="ag-field__err">{crearErrores.usuario}</p>{/if}
        </div>
      </div>

      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalCrear = false} disabled={creando}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleCrearHotel}
          disabled={creando || usuariosDisponibles.length === 0}>
          {creando ? 'Creando...' : 'Crear Hotel'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Modal para asignar un usuario webservice disponible a un hotel existente -->
{#if modalAsignar}
  <div class="ag-overlay" on:click={() => modalAsignar = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Asignar Usuario</h3>
        <button class="ag-modal__close" on:click={() => modalAsignar = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">
          Hotel: <strong>{hotelSeleccionado?.nombre}</strong>
        </p>
        {#if usuariosDisponibles.length === 0}
          <div class="ag-notice ag-notice--warn">
            No hay usuarios Webservice disponibles sin entidad asignada.
          </div>
        {:else}
          <div class="ag-field">
            <label class="ag-field__label">Usuario Webservice disponible</label>
            <select class="ag-field__select" bind:value={asignarUsuarioId}>
              <option value="">— Seleccionar —</option>
              {#each usuariosDisponibles as u}
                <option value={u.id}>{u.nombre} (@{u.username})</option>
              {/each}
            </select>
          </div>
        {/if}
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalAsignar = false} disabled={asignando}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleAsignarUsuario}
          disabled={asignando || !asignarUsuarioId || usuariosDisponibles.length === 0}>
          {asignando ? 'Asignando...' : 'Asignar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Modal para editar la URL de la API y la URL publica de un hotel aliado -->
{#if modalUrls}
  <div class="ag-overlay" on:click={() => modalUrls = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Editar URLs</h3>
        <button class="ag-modal__close" on:click={() => modalUrls = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">Hotel: <strong>{hotelUrls?.nombre}</strong></p>

        <!-- Campo para la URL base de la API del hotel -->
        <div class="ag-field">
          <label class="ag-field__label">URL de la API del hotel</label>
          <input class="ag-field__input" type="url" placeholder="https://api.mi-hotel.com"
            bind:value={urlEditando} maxlength="300" />
        </div>

        <!-- Campo para la URL publica visible a los pasajeros -->
        <div class="ag-field">
          <label class="ag-field__label">URL publica para usuarios</label>
          <input class="ag-field__input" type="url" placeholder="https://www.mi-hotel.com"
            bind:value={urlParaUsuarioEditando} maxlength="300" />
        </div>
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalUrls = false} disabled={guardandoUrls}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleGuardarUrls} disabled={guardandoUrls}>
          {guardandoUrls ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Seccion principal de gestion de hoteles aliados con estadisticas y tabla -->
<section class="admin-section">

  <!-- Encabezado de seccion con titulo y botones de actualizar y crear hotel -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Hoteles Aliados</h2>
      <p class="admin-section__subtitle">Crea y gestiona los hoteles aliados Webservice</p>
    </div>
    <div style="display:flex; gap:.75rem;">
      <button class="btn-add" on:click={cargarTodo} style="background:#4b5563">
        ↻ Actualizar
      </button>
      <button class="btn-add" on:click={abrirModalCrear}>
        + Nuevo Hotel
      </button>
    </div>
  </div>

  {#if cargando}
    <p class="loading-text">Cargando hoteles...</p>

  {:else if hoteles.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay hoteles aliados registrados todavia.</p>
    </div>

  <!-- Tabla de hoteles con usuario asignado, URLs editables, handshake y selector de estado -->
  {:else}
    <div class="vuelos-table">
      <table class="table">
        <thead class="table__head">
          <tr>
            <th class="table__header">ID</th>
            <th class="table__header">Nombre</th>
            <th class="table__header">URL API</th>
            <th class="table__header">URL Publica</th>
            <th class="table__header">Usuario Webservice</th>
            <th class="table__header">Estado</th>
            <th class="table__header">Acciones</th>
          </tr>
        </thead>
        <tbody class="table__body">
          {#each hoteles as h}
            <tr class="table__row">
              <td class="table__cell" data-label="ID">#{h.id}</td>

              <td class="table__cell" data-label="Nombre">
                <strong>{h.nombre}</strong>
              </td>

              <!-- URL base de la API del hotel; truncada para no romper el layout -->
              <td class="table__cell" data-label="URL API">
                {#if h.url}
                  <span style="font-size:.78rem; color:var(--text-muted); word-break:break-all; max-width:140px; display:block;">
                    {h.url}
                  </span>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL</span>
                {/if}
              </td>

              <!-- URL publica del hotel que se muestra a los pasajeros -->
              <td class="table__cell" data-label="URL Publica">
                {#if h.urlParaUsuario}
                  <span style="font-size:.78rem; color:var(--text-muted); word-break:break-all; max-width:140px; display:block;">
                    {h.urlParaUsuario}
                  </span>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL</span>
                {/if}
              </td>

              <td class="table__cell" data-label="Usuario">
                {#if h.usuarioWEBIs}
                  <div class="ag-user-cell">
                    <span class="ag-user-cell__name">{h.usuarioNombre}</span>
                    <span class="ag-user-cell__user">@{h.usuarioUsername}</span>
                  </div>
                {:else}
                  <span class="ag-sin-usuario">Sin asignar</span>
                {/if}
              </td>

              <td class="table__cell" data-label="Estado">
                <select class="ag-estado-select ag-estado--{h.estadoID}"
                  value={h.estadoID}
                  on:change={(e) => handleCambiarEstado(h, e.target.value)}>
                  {#each estadoOpciones as op}
                    <option value={op.id}>{op.label}</option>
                  {/each}
                </select>
              </td>

              <td class="table__cell" data-label="Acciones">
                <div class="table__actions">
                  <!-- Boton para editar ambas URLs del hotel aliado -->
                  <button class="table__action-btn ag-btn-asignar"
                    style="background:#6366f1"
                    on:click={() => abrirModalUrls(h)}
                    title="Editar URLs">
                    🔗 URLs
                  </button>

                  <!-- Boton para asignar o reasignar usuario Webservice al hotel -->
                  <button class="table__action-btn ag-btn-asignar"
                    on:click={() => abrirModalAsignar(h)}
                    title="Asignar usuario Webservice">
                    👤 Asignar
                  </button>

                  <!-- Boton para iniciar el handshake de autenticacion con el hotel aliado.
                       Requiere que el hotel tenga la URL de API configurada previamente. -->
                  <button class="table__action-btn ag-btn-asignar"
                    style="background:#059669"
                    on:click={() => handleHandshake(h)}
                    disabled={handshakeEnCurso === h.id}
                    title="Iniciar handshake de autenticacion con el hotel">
                    {handshakeEnCurso === h.id ? '⏳ Conectando...' : '🤝 Handshake'}
                  </button>
                </div>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </div>
  {/if}

</section>