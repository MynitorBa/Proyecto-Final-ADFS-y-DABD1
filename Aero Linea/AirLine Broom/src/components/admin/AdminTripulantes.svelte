<script>
/**
 * @file AdminTripulantes.svelte
 * @description Admin panel section for managing crew members (tripulantes). Displays a table
 * listing all crew members with their photo, ID, first name, last name and role badge. Allows
 * creating new crew members and editing existing ones through a modal form. The form includes
 * first name, last name, role (loaded from the API), and an optional profile photo uploaded as
 * base64. Profile photos can also be removed individually via a confirmation dialog. Dispatches
 * 'tripulantesActualizados' to the parent after any successful create, update, or photo deletion
 * so the parent can refresh its own crew list.
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

  /** List of crew members currently registered in the system. @type {any[]} */
  let tripulantes        = [];

  /** Available crew roles fetched from the backend, mapped to { id, nombre }. @type {{ id: number, nombre: string }[]} */
  let rolesTripulacion   = [];

  /** Whether the crew member list fetch is in progress. @type {boolean} */
  let loadingTripulantes = false;

  /** True when the modal is editing an existing crew member, false when creating a new one. @type {boolean} */
  let modoEdicion        = false;

  /** Whether the create/edit modal form is visible. @type {boolean} */
  let mostrarFormulario       = false;

  /**
   * Form data bound to the create/edit modal fields.
   * @type {{ id: number|null, nombre: string, apellido: string, rolID: string|number }}
   */
  let tripulanteForm          = { id: null, nombre: '', apellido: '', rolID: '' };

  /** Data URL of the photo preview shown in the modal before saving. @type {string|null} */
  let tripulanteImagenPreview = null;

  /** Base64-encoded photo string sent to the backend on form submission. @type {string|null} */
  let tripulanteImagenBase64  = null;

  /**
   * On mount: loads both the crew member list and available roles in parallel.
   * @async
   * @returns {Promise<void>}
   */
  onMount(async () => {
    await Promise.all([cargarTripulantes(), cargarRoles()]);
  });

  /**
   * Fetches the complete crew member list from the backend and stores it in tripulantes.
   * Shows a toast on error and sets loadingTripulantes during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTripulantes() {
    loadingTripulantes = true;
    try {
      const r = await fetch(`${API}/api/tripulacion`);
      if (r.ok) tripulantes = await r.json();
      else mostrarToast('error', 'Error al cargar tripulantes');
    } catch { mostrarToast('error', 'Error de conexion al cargar tripulantes'); }
    finally { loadingTripulantes = false; }
  }

  /**
   * Fetches the available crew roles from the backend API and maps each entry to { id, nombre }
   * using the cargo field as the display name. Logs an error to the console on failure.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarRoles() {
    try {
      const r = await fetch(`${API}/api/tripulacion/roles`);
      if (r.ok) {
        const roles = await r.json();
        rolesTripulacion = roles.map(rol => ({ id: rol.id, nombre: rol.cargo }));
      }
    } catch { console.error('Error al cargar roles de tripulacion'); }
  }

  /**
   * Reads the file selected in the photo input, converts it to a base64 data URL and stores
   * it in both tripulanteImagenBase64 (for submission) and tripulanteImagenPreview (for display).
   * @param {Event} e - The change event from the file input element.
   */
  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { tripulanteImagenBase64 = reader.result; tripulanteImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Resets the form to empty values and opens the modal in creation mode.
   */
  function abrirNuevo() {
    modoEdicion = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
    mostrarFormulario = true;
  }

  /**
   * Pre-fills the form with the selected crew member's data and opens the modal in edit mode.
   * @param {any} t - The crew member row object from the table.
   */
  function abrirEditar(t) {
    modoEdicion = true;
    tripulanteForm = { id: t.id, nombre: t.nombre, apellido: t.apellido, rolID: t.rolID };
    tripulanteImagenBase64 = null; tripulanteImagenPreview = t.imagenBase64 || null;
    mostrarFormulario = true;
  }

  /**
   * Closes the modal and resets all form fields and photo state.
   */
  function cerrar() {
    mostrarFormulario = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
  }

  /**
   * Validates nombre, apellido and rolID, then sends a POST or PUT request to the backend.
   * On success reloads the crew list, dispatches 'tripulantesActualizados' and closes the modal.
   * Shows error toasts for validation failures or API errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardar() {
    if (!tripulanteForm.nombre.trim())   { mostrarToast('error', 'El nombre es obligatorio'); return; }
    if (!tripulanteForm.apellido.trim()) { mostrarToast('error', 'El apellido es obligatorio'); return; }
    if (!tripulanteForm.rolID)           { mostrarToast('error', 'Selecciona un rol'); return; }
    try {
      const url    = modoEdicion ? `${API}/api/tripulacion/${tripulanteForm.id}` : `${API}/api/tripulacion`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:       tripulanteForm.nombre,
          apellido:     tripulanteForm.apellido,
          rolID:        parseInt(tripulanteForm.rolID),
          imagenBase64: tripulanteImagenBase64 || null
        })
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Tripulante actualizado correctamente' : 'Tripulante creado correctamente');
        await cargarTripulantes();
        dispatch('tripulantesActualizados');
        cerrar();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el tripulante');
      }
    } catch { mostrarToast('error', 'Error de conexion al guardar el tripulante'); }
  }

  /**
   * Asks for confirmation and then sends a DELETE request to remove the photo from a crew member
   * record. On success reloads the crew list and dispatches 'tripulantesActualizados'.
   * @async
   * @param {number} tripulanteId - The ID of the crew member whose photo should be removed.
   * @returns {Promise<void>}
   */
  async function handleEliminarFoto(tripulanteId) {
    const ok = await mostrarConfirm('¿Quitar la foto de este tripulante?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/tripulacion/${tripulanteId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) {
        mostrarToast('success', 'Foto eliminada');
        await cargarTripulantes();
        dispatch('tripulantesActualizados');
      } else { mostrarToast('error', 'Error al eliminar la foto'); }
    } catch { mostrarToast('error', 'Error de conexion'); }
  }
</script>

<!-- Seccion de gestion de tripulantes con tabla y modal de creacion/edicion -->
<section class="admin-section">
  <!-- Encabezado de seccion con titulo y boton de nuevo tripulante -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Tripulantes</h2>
      <p class="admin-section__subtitle">Administra los miembros de tripulacion</p>
    </div>
    <button class="btn-add" on:click={abrirNuevo}>
      <span class="btn-add__icon">+</span> Nuevo Tripulante
    </button>
  </div>

  <!-- Tabla de tripulantes con foto, nombre, apellido y rol -->
  {#if loadingTripulantes}
    <p class="loading-text">Cargando tripulantes...</p>

  {:else if tripulantes.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay tripulantes registrados.</p>
    </div>

  {:else}
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">Foto</th>
          <th class="table__header">ID</th>
          <th class="table__header">Nombre</th>
          <th class="table__header">Apellido</th>
          <th class="table__header">Rol</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each tripulantes as t}
          <tr class="table__row">
            <td class="table__cell" data-label="Foto">
              {#if t.imagenBase64}
                <img src={t.imagenBase64} alt={t.nombreCompleto}
                  class="entity-thumb entity-thumb--circle" />
              {:else}
                <span style="color:#9ca3af">—</span>
              {/if}
            </td>
            <td class="table__cell" data-label="ID">{t.id}</td>
            <td class="table__cell" data-label="Nombre">{t.nombre}</td>
            <td class="table__cell" data-label="Apellido">{t.apellido}</td>
            <td class="table__cell" data-label="Rol">
              <span class="rol-badge--tripulacion">{t.nombreRol}</span>
            </td>
            <td class="table__cell" data-label="Acciones">
              <div class="table__actions">
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => abrirEditar(t)}>Editar</button>
                {#if t.imagenBase64}
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => handleEliminarFoto(t.id)}>Quitar foto</button>
                {/if}
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>

<!-- Modal de creacion y edicion de tripulante con nombre, apellido, rol y foto -->
{#if mostrarFormulario}
  <div class="modal-overlay" on:click={cerrar} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Tripulante</h3>
        <button class="modal__close" on:click={cerrar}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardar}>

        <div class="form-field">
          <label for="at-nombre" class="form-label">Nombre *</label>
          <input type="text" id="at-nombre" class="form-input"
            bind:value={tripulanteForm.nombre} placeholder="Ej: Juan" required />
        </div>

        <div class="form-field">
          <label for="at-apellido" class="form-label">Apellido *</label>
          <input type="text" id="at-apellido" class="form-input"
            bind:value={tripulanteForm.apellido} placeholder="Ej: Perez" required />
        </div>

        <div class="form-field">
          <label for="at-rol" class="form-label">Rol *</label>
          <select id="at-rol" class="form-input" bind:value={tripulanteForm.rolID} required>
            <option value="">Selecciona un rol</option>
            {#each rolesTripulacion as rol}
              <option value={rol.id}>{rol.nombre}</option>
            {/each}
          </select>
        </div>

        <div class="form-field">
          <label for="at-foto" class="form-label">Foto del Tripulante</label>
          {#if tripulanteImagenPreview}
            <img src={tripulanteImagenPreview} alt="Preview"
              class="img-preview img-preview--circle" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { tripulanteImagenPreview = null; tripulanteImagenBase64 = null; }}>
              Quitar foto
            </button>
          {/if}
          <input id="at-foto" type="file" accept="image/*" class="form-input"
            on:change={onImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Tripulante
          </button>
          <button type="button" class="btn-secondary" on:click={cerrar}>Cancelar</button>
        </div>

      </form>
    </div>
  </div>
{/if}
