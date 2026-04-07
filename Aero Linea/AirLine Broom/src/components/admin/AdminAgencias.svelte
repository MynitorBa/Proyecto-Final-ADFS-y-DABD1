<script>
/**
 * @file AdminAgencias.svelte
 * @description Admin panel section for managing travel agencies. Displays a summary stats bar
 * (total, active, inactive, without assigned user) and a table of all agencies. Provides four
 * modal dialogs: one to create a new agency (with name, email, URL, webservice user and initial
 * discount), one to assign an available webservice user to an existing agency, one to edit an
 * agency's discount percentage, and one to edit its public URL. Agency status can also be changed
 * inline through a select element in the table row. All mutations call the backend API and refresh
 * the local state on success.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** Base API URL used for all backend requests. @type {string} */
  export let API;

  /** Function to show a toast notification. Signature: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Function to show a confirmation dialog. Signature: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  /** List of agencies loaded from the backend. @type {any[]} */
  let agencias           = [];

  /** Webservice-role users that have no entity assigned yet, used to populate the user selectors. @type {any[]} */
  let usuariosDisponibles = [];

  /** Whether the main data fetch is in progress. @type {boolean} */
  let cargando           = false;

  // ── Create modal state ────────────────────────────────────────────────

  /** Controls visibility of the create-agency modal. @type {boolean} */
  let modalCrear         = false;

  /** Whether the create-agency API request is in flight. @type {boolean} */
  let creando            = false;

  /** Agency name field value for the create form. @type {string} */
  let crearNombre        = '';

  /** Agency email field value for the create form. @type {string} */
  let crearCorreo        = '';

  /** Public URL of the agency for the create form. @type {string} */
  let crearUrl           = '';

  /** Selected webservice user ID for the create form. @type {string} */
  let crearUsuarioId     = '';

  /** Initial discount percentage value for the create form. @type {number} */
  let crearDescuento     = 0;

  /** Field-level validation errors for the create form. @type {Record<string, string>} */
  let crearErrores       = {};

  // ── Assign-user modal state ───────────────────────────────────────────

  /** Controls visibility of the assign-user modal. @type {boolean} */
  let modalAsignar       = false;

  /** Whether the assign-user API request is in flight. @type {boolean} */
  let asignando          = false;

  /** The agency row currently selected for user assignment. @type {any} */
  let agenciaSeleccionada = null;

  /** Selected webservice user ID in the assign-user modal. @type {string} */
  let asignarUsuarioId   = '';

  // ── Edit-discount modal state ─────────────────────────────────────────

  /** Controls visibility of the edit-discount modal. @type {boolean} */
  let modalDescuento     = false;

  /** Whether the save-discount API request is in flight. @type {boolean} */
  let guardandoDescuento = false;

  /** Current discount percentage value being edited. @type {number} */
  let descuentoEditando  = 0;

  /** The agency row being edited in the discount modal. @type {any} */
  let agenciaDescuento   = null;

  // ── Edit-URL modal state ──────────────────────────────────────────────

  /** Controls visibility of the edit-URL modal. @type {boolean} */
  let modalUrl           = false;

  /** Whether the save-URL API request is in flight. @type {boolean} */
  let guardandoUrl       = false;

  /** URL value being edited in the URL modal. @type {string} */
  let urlEditando        = '';

  /** The agency row being edited in the URL modal. @type {any} */
  let agenciaUrl         = null;

  /**
   * Status option definitions used to render the inline status select and badge styles.
   * @type {{ id: number, label: string, class: string }[]}
   */
  const estadoOpciones = [
    { id: 1, label: 'Activa',     class: 'badge--active'    },
    { id: 2, label: 'Inactiva',   class: 'badge--inactive'  },
    { id: 3, label: 'Suspendida', class: 'badge--suspended' },
  ];

  /**
   * Returns the status option object matching the given status ID, or a default unknown object.
   * @param {number} id - The estadoAgenciaID value from an agency record.
   * @returns {{ id: number, label: string, class: string }} The matching status option.
   */
  const estadoInfo = (id) => estadoOpciones.find(e => e.id === id) ?? { label: 'Desconocido', class: '' };

  /**
   * On mount: loads agencies and available webservice users in parallel.
   */
  onMount(() => { cargarTodo(); });

  /**
   * Fetches the full agency list and the list of unassigned webservice users in parallel.
   * Updates agencias and usuariosDisponibles on success. Shows toasts on errors.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTodo() {
    cargando = true;
    try {
      const [rAgencias, rUsuarios] = await Promise.all([
        fetch(`${API}/api/agencias/todas`,                  { credentials: 'include' }),
        fetch(`${API}/api/agencias/webservice-disponibles`, { credentials: 'include' }),
      ]);
      if (rAgencias.ok) agencias            = await rAgencias.json();
      else mostrarToast('error', 'Error al cargar agencias.');
      if (rUsuarios.ok) usuariosDisponibles = await rUsuarios.json();
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally  { cargando = false; }
  }

  /**
   * Refreshes only the list of available webservice users without reloading the full agency list.
   * Used after operations that may change user availability.
   * @async
   * @returns {Promise<void>}
   */
  async function recargarUsuariosDisponibles() {
    try {
      const r = await fetch(`${API}/api/agencias/webservice-disponibles`, { credentials: 'include' });
      if (r.ok) usuariosDisponibles = await r.json();
    } catch {}
  }

  /**
   * Resets the create-agency form fields and errors, then opens the create modal.
   */
  function abrirModalCrear() {
    crearNombre = ''; crearCorreo = ''; crearUrl = ''; crearUsuarioId = ''; crearDescuento = 0;
    crearErrores = {};
    modalCrear = true;
  }

  /**
   * Validates all fields in the create-agency form. Populates crearErrores with any
   * field-level messages and returns false if validation fails.
   * @returns {boolean} True when all fields are valid.
   */
  function validarCrear() {
    crearErrores = {};
    if (!crearNombre.trim()) crearErrores.nombre = 'Requerido.';
    if (!crearCorreo.trim()) crearErrores.correo = 'Requerido.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(crearCorreo)) crearErrores.correo = 'Correo invalido.';
    if (!crearUrl.trim()) crearErrores.url = 'Requerido.';
    else if (!/^https?:\/\/.+/.test(crearUrl.trim())) crearErrores.url = 'URL invalida (debe iniciar con http:// o https://).';
    if (!crearUsuarioId) crearErrores.usuario = 'Debes seleccionar un usuario Webservice.';
    if (crearDescuento < 0 || crearDescuento > 100) crearErrores.descuento = 'Entre 0 y 100.';
    return Object.keys(crearErrores).length === 0;
  }

  /**
   * Validates the create form and, if valid, POSTs the new agency to the backend.
   * On success closes the modal and reloads all data. On failure shows an error toast.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearAgencia() {
    if (!validarCrear()) return;
    creando = true;
    try {
      const r = await fetch(`${API}/api/agencias`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              crearNombre.trim(),
          correo:              crearCorreo.trim(),
          urlAgencia:          crearUrl.trim(),
          usuarioWebID:        parseInt(crearUsuarioId),
          porcentajeDescuento: parseFloat(crearDescuento),
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Agencia creada correctamente.');
        modalCrear = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al crear la agencia.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { creando = false; }
  }

  /**
   * Sets the selected agency and resets the user selector, then opens the assign-user modal.
   * @param {any} agencia - The agency row object from the table.
   */
  function abrirModalAsignar(agencia) {
    agenciaSeleccionada = agencia;
    asignarUsuarioId    = '';
    modalAsignar        = true;
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
      const r = await fetch(`${API}/api/agencias/${agenciaSeleccionada.id}/asignar-usuario`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usuarioWebId: parseInt(asignarUsuarioId) })
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
   * Pre-fills the discount modal with the agency's current discount and opens it.
   * @param {any} agencia - The agency row object from the table.
   */
  function abrirModalDescuento(agencia) {
    agenciaDescuento  = agencia;
    descuentoEditando = agencia.porcentajeDescuento;
    modalDescuento    = true;
  }

  /**
   * Validates the discount value (0–100) and PUTs the updated discount to the backend.
   * On success closes the modal and reloads all data. Shows error toasts on failures.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardarDescuento() {
    if (descuentoEditando < 0 || descuentoEditando > 100) {
      mostrarToast('error', 'El descuento debe estar entre 0 y 100.'); return;
    }
    guardandoDescuento = true;
    try {
      const r = await fetch(`${API}/api/agencias/${agenciaDescuento.id}/descuento`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ descuento: parseFloat(descuentoEditando) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Descuento actualizado.');
        modalDescuento = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al actualizar descuento.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { guardandoDescuento = false; }
  }

  /**
   * Pre-fills the URL modal with the agency's current URL and opens it.
   * @param {any} agencia - The agency row object from the table.
   */
  function abrirModalUrl(agencia) {
    agenciaUrl  = agencia;
    urlEditando = agencia.urlAgencia ?? '';
    modalUrl    = true;
  }

  /**
   * Validates the URL value and PUTs the updated URL to the backend.
   * On success closes the modal and reloads all data. Shows error toasts on failures.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardarUrl() {
    if (!urlEditando.trim()) { mostrarToast('error', 'La URL no puede estar vacia.'); return; }
    if (!/^https?:\/\/.+/.test(urlEditando.trim())) { mostrarToast('error', 'URL invalida (debe iniciar con http:// o https://).'); return; }
    guardandoUrl = true;
    try {
      const r = await fetch(`${API}/api/agencias/${agenciaUrl.id}/url`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ urlAgencia: urlEditando.trim() })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'URL actualizada.');
        modalUrl = false;
        await cargarTodo();
      } else {
        mostrarToast('error', data.message || 'Error al actualizar URL.');
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
    finally { guardandoUrl = false; }
  }

  /**
   * Sends a PUT request to change an agency's status. On success updates the local agencias
   * array optimistically without a full reload. On failure reloads the full list to revert.
   * @async
   * @param {any} agencia - The agency row object whose status is being changed.
   * @param {string|number} nuevoEstadoId - The new status ID to apply.
   * @returns {Promise<void>}
   */
  async function handleCambiarEstado(agencia, nuevoEstadoId) {
    try {
      const r = await fetch(`${API}/api/agencias/${agencia.id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: parseInt(nuevoEstadoId) })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Estado actualizado.');
        agencias = agencias.map(a => a.id === agencia.id ? { ...a, estadoAgenciaID: parseInt(nuevoEstadoId) } : a);
      } else {
        mostrarToast('error', data.message || 'Error al cambiar estado.');
        await cargarTodo();
      }
    } catch { mostrarToast('error', 'Error de conexion.'); }
  }


</script>

<!-- Modal de creacion de nueva agencia con nombre, correo, URL, usuario webservice y descuento -->
{#if modalCrear}
  <div class="ag-overlay" on:click={() => modalCrear = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Nueva Agencia</h3>
        <button class="ag-modal__close" on:click={() => modalCrear = false}>×</button>
      </div>

      <div class="ag-modal__body">
        <div class="ag-field">
          <label class="ag-field__label">Nombre <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.nombre}
            type="text" bind:value={crearNombre} placeholder="Agencia Viajes GT" maxlength="120" />
          {#if crearErrores.nombre}<p class="ag-field__err">{crearErrores.nombre}</p>{/if}
        </div>

        <div class="ag-field">
          <label class="ag-field__label">Correo <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.correo}
            type="email" bind:value={crearCorreo} placeholder="agencia@ejemplo.com" maxlength="200" />
          {#if crearErrores.correo}<p class="ag-field__err">{crearErrores.correo}</p>{/if}
        </div>

        <!-- URL publica de la agencia para que la aerolinea pueda comunicarse con ella -->
        <div class="ag-field">
          <label class="ag-field__label">URL de la agencia <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.url}
            type="url" bind:value={crearUrl} placeholder="https://mi-agencia.com" maxlength="300" />
          {#if crearErrores.url}<p class="ag-field__err">{crearErrores.url}</p>{/if}
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

        <div class="ag-field">
          <label class="ag-field__label">Descuento inicial (%)</label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.descuento}
            type="number" min="0" max="100" step="0.01" bind:value={crearDescuento} />
          {#if crearErrores.descuento}<p class="ag-field__err">{crearErrores.descuento}</p>{/if}
        </div>
      </div>

      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalCrear = false} disabled={creando}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleCrearAgencia} disabled={creando || usuariosDisponibles.length === 0}>
          {creando ? 'Creando...' : 'Crear Agencia'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Modal para asignar un usuario webservice disponible a una agencia existente -->
{#if modalAsignar}
  <div class="ag-overlay" on:click={() => modalAsignar = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Asignar Usuario</h3>
        <button class="ag-modal__close" on:click={() => modalAsignar = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">
          Agencia: <strong>{agenciaSeleccionada?.nombre}</strong>
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

<!-- Modal para editar el porcentaje de descuento de una agencia -->
{#if modalDescuento}
  <div class="ag-overlay" on:click={() => modalDescuento = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Editar Descuento</h3>
        <button class="ag-modal__close" on:click={() => modalDescuento = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">Agencia: <strong>{agenciaDescuento?.nombre}</strong></p>
        <div class="ag-field">
          <label class="ag-field__label">Porcentaje de descuento (0 – 100)</label>
          <input class="ag-field__input" type="number" min="0" max="100" step="0.01"
            bind:value={descuentoEditando} />
        </div>
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalDescuento = false} disabled={guardandoDescuento}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleGuardarDescuento} disabled={guardandoDescuento}>
          {guardandoDescuento ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Modal para editar la URL publica de una agencia -->
{#if modalUrl}
  <div class="ag-overlay" on:click={() => modalUrl = false} role="dialog" aria-modal="true">
    <div class="ag-modal ag-modal--sm" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Editar URL</h3>
        <button class="ag-modal__close" on:click={() => modalUrl = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">Agencia: <strong>{agenciaUrl?.nombre}</strong></p>
        <div class="ag-field">
          <label class="ag-field__label">URL publica de la agencia</label>
          <input class="ag-field__input" type="url" placeholder="https://mi-agencia.com"
            bind:value={urlEditando} maxlength="300" />
        </div>
      </div>
      <div class="ag-modal__footer">
        <button class="btn-secondary" on:click={() => modalUrl = false} disabled={guardandoUrl}>
          Cancelar
        </button>
        <button class="btn-primary" on:click={handleGuardarUrl} disabled={guardandoUrl}>
          {guardandoUrl ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  </div>
{/if}

<!-- Seccion principal de gestion de agencias con estadisticas y tabla -->
<section class="admin-section">

  <!-- Encabezado de seccion con titulo y botones de actualizar y crear agencia -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Agencias</h2>
      <p class="admin-section__subtitle">Crea y gestiona las agencias Webservice</p>
    </div>
    <div style="display:flex; gap:.75rem;">
      <button class="btn-add" on:click={cargarTodo} style="background:#4b5563">
        ↻ Actualizar
      </button>
      <button class="btn-add" on:click={abrirModalCrear}>
        + Nueva Agencia
      </button>
    </div>
  </div>

  {#if cargando}
    <p class="loading-text">Cargando agencias...</p>

  {:else if agencias.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay agencias registradas todavia.</p>
    </div>

  <!-- Tabla de agencias con usuario asignado, descuento editable, URL editable y selector de estado -->
  {:else}
    <div class="vuelos-table">
      <table class="table">
        <thead class="table__head">
          <tr>
            <th class="table__header">ID</th>
            <th class="table__header">Nombre / Correo</th>
            <th class="table__header">URL</th>
            <th class="table__header">Usuario Webservice</th>
            <th class="table__header">Descuento</th>
            <th class="table__header">Estado</th>
            <th class="table__header">Acciones</th>
          </tr>
        </thead>
        <tbody class="table__body">
          {#each agencias as ag}
            <tr class="table__row">
              <td class="table__cell" data-label="ID">#{ag.id}</td>

              <td class="table__cell" data-label="Nombre">
                <strong>{ag.nombre}</strong>
                <br/>
                <span style="font-size:.78rem; color:var(--text-muted)">{ag.correo}</span>
              </td>

              <!-- URL publica de la agencia; se trunca visualmente para no romper el layout -->
              <td class="table__cell" data-label="URL">
                {#if ag.urlAgencia}
                  <span style="font-size:.78rem; color:var(--text-muted); word-break:break-all; max-width:160px; display:block;">
                    {ag.urlAgencia}
                  </span>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL</span>
                {/if}
              </td>

              <td class="table__cell" data-label="Usuario">
                {#if ag.usuarioWebID}
                  <div class="ag-user-cell">
                    <span class="ag-user-cell__name">{ag.usuarioWebNombre}</span>
                    <span class="ag-user-cell__user">@{ag.usuarioWebUsername}</span>
                  </div>
                {:else}
                  <span class="ag-sin-usuario">Sin asignar</span>
                {/if}
              </td>

              <td class="table__cell" data-label="Descuento">
                <button class="ag-discount-btn" on:click={() => abrirModalDescuento(ag)}
                  title="Editar descuento">
                  {ag.porcentajeDescuento}%
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
              </td>

              <td class="table__cell" data-label="Estado">
                <select class="ag-estado-select ag-estado--{ag.estadoAgenciaID}"
                  value={ag.estadoAgenciaID}
                  on:change={(e) => handleCambiarEstado(ag, e.target.value)}>
                  {#each estadoOpciones as op}
                    <option value={op.id}>{op.label}</option>
                  {/each}
                </select>
              </td>

              <td class="table__cell" data-label="Acciones">
                <div class="table__actions">
                  <!-- Boton para editar la URL publica de la agencia -->
                  <button class="table__action-btn ag-btn-asignar"
                    style="background:#6366f1"
                    on:click={() => abrirModalUrl(ag)}
                    title="Editar URL">
                    🔗 URL
                  </button>
                  <button class="table__action-btn ag-btn-asignar"
                    on:click={() => abrirModalAsignar(ag)}
                    title="Asignar usuario Webservice">
                    👤 Asignar
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