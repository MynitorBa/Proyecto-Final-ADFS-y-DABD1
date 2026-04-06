<script>
/**
 * @file AdminAviones.svelte
 * @description Admin panel section for managing the aircraft fleet. Displays a table showing
 * all registered planes with their image thumbnail, ID, brand, model, and passenger capacity.
 * Allows creating new planes and editing existing ones via a modal form. The brand field is
 * restricted to letters only (first letter capitalized), and the model field is formatted to the
 * [0-2 letters][2-4 digits][-suffix] pattern (e.g. A380-800, 737-MAX). An optional image can
 * be uploaded as base64. Dispatches 'avionesActualizados' to the parent after any successful
 * create, update, or image deletion.
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

  /** List of aircraft currently registered in the system, loaded from the backend API. @type {any[]} */
  let aviones        = [];

  /** Whether the aircraft list fetch is in progress. @type {boolean} */
  let loadingAviones = false;

  /** True when the modal is editing an existing plane, false when creating a new one. @type {boolean} */
  let modoEdicion    = false;

  /** Whether the create/edit modal form is visible. @type {boolean} */
  let mostrarFormulario  = false;

  /**
   * Form data object bound to the create/edit form fields.
   * @type {{ id: number|null, marca: string, modelo: string, capacidadPasajeros: string|number }}
   */
  let avionForm          = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };

  /** Data URL of the selected image preview shown before saving. @type {string|null} */
  let avionImagenPreview = null;

  /** Base64-encoded image string sent to the backend on form submission. @type {string|null} */
  let avionImagenBase64  = null;

  /**
   * On mount: loads the list of aircraft from the backend.
   */
  onMount(() => { cargarAviones(); });

  /**
   * Fetches the list of aircraft from the backend API and stores them in aviones.
   * Shows a toast on error and sets loadingAviones during the request.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarAviones() {
    loadingAviones = true;
    try {
      const r = await fetch(`${API}/api/aviones`);
      if (r.ok) aviones = await r.json();
      else mostrarToast('error', 'Error al cargar aviones');
    } catch { mostrarToast('error', 'Error de conexion al cargar aviones'); }
    finally { loadingAviones = false; }
  }

  /**
   * Handles input on the brand field. Strips any non-letter and non-space characters and
   * capitalizes the first letter before updating avionForm.marca and the input element value.
   * @param {Event} e - The input event from the brand text field.
   */
  function formatearMarca(e) {
    let val = e.target.value.replace(/[^a-zA-Z\s]/g, '');
    val = val.charAt(0).toUpperCase() + val.slice(1);
    avionForm.marca = val;
    e.target.value = val;
  }

  /**
   * Handles input on the model field. Enforces the real aircraft model format:
   * up to 2 uppercase letters followed by up to 4 digits, optionally with a hyphen and
   * up to 5 alphanumeric suffix characters (e.g. A380-800, 737-MAX, E195-E2, B787-9).
   * Updates avionForm.modelo and the input element value.
   * @param {Event} e - The input event from the model text field.
   */
  function formatearModelo(e) {
    let val = e.target.value.toUpperCase().replace(/[^A-Z0-9\-]/g, '');

    const guionIdx = val.indexOf('-');
    let base   = guionIdx >= 0 ? val.slice(0, guionIdx) : val;
    let sufijo = guionIdx >= 0 ? val.slice(guionIdx + 1) : '';

    let letras  = base.replace(/[^A-Z]/g, '').slice(0, 2);
    let digitos = base.replace(/[^0-9]/g, '').slice(0, 4);

    sufijo = sufijo.replace(/[^A-Z0-9]/g, '').slice(0, 5);

    let resultado = letras + digitos;
    if (guionIdx >= 0) resultado += '-' + sufijo;

    avionForm.modelo = resultado;
    e.target.value   = resultado;
  }

  /**
   * Reads the file selected in the image input, converts it to a base64 data URL and stores
   * it in both avionImagenBase64 (for submission) and avionImagenPreview (for display).
   * @param {Event} e - The change event from the file input element.
   */
  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { avionImagenBase64 = reader.result; avionImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Resets the form to empty values and opens the modal in creation mode.
   */
  function abrirNuevo() {
    modoEdicion = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
    mostrarFormulario = true;
  }

  /**
   * Pre-fills the form with the selected aircraft's data and opens the modal in edit mode.
   * @param {any} avion - The aircraft row object from the table.
   */
  function abrirEditar(avion) {
    modoEdicion = true;
    avionForm = { id: avion.id, marca: avion.marca, modelo: avion.modelo, capacidadPasajeros: avion.capacidadPasajeros };
    avionImagenBase64 = null; avionImagenPreview = avion.imagenBase64 || null;
    mostrarFormulario = true;
  }

  /**
   * Closes the modal and resets all form state and image previews.
   */
  function cerrar() {
    mostrarFormulario = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
  }

  /**
   * Validates the form (marca required, modelo required and must have at least 2 digits,
   * capacidadPasajeros must be >= 1) then sends a POST or PUT request to the backend.
   * On success reloads the aircraft list, dispatches 'avionesActualizados' and closes the modal.
   * Shows error toasts for validation failures or API errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardar() {
    if (!avionForm.marca.trim())  { mostrarToast('error', 'La marca es obligatoria'); return; }
    if (!avionForm.modelo.trim()) { mostrarToast('error', 'El modelo es obligatorio'); return; }
    if (avionForm.modelo.replace(/[^0-9]/g, '').length < 2) {
      mostrarToast('error', 'El modelo debe tener al menos 2 digitos (ej: A380-800, 737-MAX)'); return;
    }
    if (!avionForm.capacidadPasajeros || parseInt(avionForm.capacidadPasajeros) < 1) {
      mostrarToast('error', 'La capacidad debe ser mayor a 0'); return;
    }
    try {
      const url    = modoEdicion ? `${API}/api/aviones/${avionForm.id}` : `${API}/api/aviones`;
      const method = modoEdicion ? 'PUT' : 'POST';
      const r = await fetch(url, {
        method, credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          marca:              avionForm.marca,
          modelo:             avionForm.modelo,
          capacidadPasajeros: parseInt(avionForm.capacidadPasajeros),
          imagenBase64:       avionImagenBase64 || null
        })
      });
      if (r.ok) {
        mostrarToast('success', modoEdicion ? 'Avion actualizado correctamente' : 'Avion creado correctamente');
        await cargarAviones();
        dispatch('avionesActualizados');
        cerrar();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el avion');
      }
    } catch { mostrarToast('error', 'Error de conexion al guardar el avion'); }
  }

  /**
   * Asks for confirmation and then sends a DELETE request to remove the image from an aircraft
   * record. On success reloads the aircraft list and dispatches 'avionesActualizados'.
   * @async
   * @param {number} avionId - The ID of the aircraft whose image should be removed.
   * @returns {Promise<void>}
   */
  async function handleEliminarImagen(avionId) {
    const ok = await mostrarConfirm('¿Quitar la imagen de este avion?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/aviones/${avionId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) {
        mostrarToast('success', 'Imagen eliminada');
        await cargarAviones();
        dispatch('avionesActualizados');
      } else { mostrarToast('error', 'Error al eliminar la imagen'); }
    } catch { mostrarToast('error', 'Error de conexion'); }
  }
</script>

<!-- Seccion principal de gestion de flota de aviones -->
<section class="admin-section">
  <!-- Encabezado de seccion con titulo y boton de nuevo avion -->
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Aviones</h2>
      <p class="admin-section__subtitle">Administra la flota de aviones</p>
    </div>
    <button class="btn-add" on:click={abrirNuevo}>
      <span class="btn-add__icon">+</span> Nuevo Avion
    </button>
  </div>

  <!-- Tabla de aviones con imagen, marca, modelo y capacidad de pasajeros -->
  {#if loadingAviones}
    <p class="loading-text">Cargando aviones...</p>

  {:else if aviones.length === 0}
    <div class="placeholder-card">
      <p class="placeholder-card__text">No hay aviones registrados.</p>
    </div>

  {:else}
    <table class="table">
      <thead class="table__head">
        <tr>
          <th class="table__header">Imagen</th>
          <th class="table__header">ID</th>
          <th class="table__header">Marca</th>
          <th class="table__header">Modelo</th>
          <th class="table__header">Capacidad</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each aviones as avion}
          <tr class="table__row">
            <td class="table__cell">
              {#if avion.imagenBase64}
                <img src={avion.imagenBase64} alt={avion.nombreCompleto} class="entity-thumb" />
              {:else}
                <span style="color:#9ca3af">—</span>
              {/if}
            </td>
            <td class="table__cell">{avion.id}</td>
            <td class="table__cell">{avion.marca}</td>
            <td class="table__cell">{avion.modelo}</td>
            <td class="table__cell">{avion.capacidadPasajeros} pax</td>
            <td class="table__cell">
              <div class="table__actions">
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => abrirEditar(avion)}>Editar</button>
                {#if avion.imagenBase64}
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => handleEliminarImagen(avion.id)}>Quitar img</button>
                {/if}
              </div>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
</section>

<!-- Modal de creacion y edicion de avion con campos de marca, modelo, capacidad e imagen -->
{#if mostrarFormulario}
  <div class="modal-overlay" on:click={cerrar} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Avion</h3>
        <button class="modal__close" on:click={cerrar}>×</button>
      </div>
      <form class="modal__form" on:submit|preventDefault={handleGuardar}>

        <div class="form-field">
          <label for="aa-marca" class="form-label">Marca *</label>
          <input type="text" id="aa-marca" class="form-input"
            value={avionForm.marca}
            on:input={formatearMarca}
            placeholder="Ej: Boeing" required />
          <small class="img-hint">Solo letras (ej: Boeing, Airbus, Embraer)</small>
        </div>

        <div class="form-field">
          <label for="aa-modelo" class="form-label">Modelo *</label>
          <input type="text" id="aa-modelo" class="form-input"
            value={avionForm.modelo}
            on:input={formatearModelo}
            placeholder="Ej: A380-800"
            style="text-transform:uppercase;letter-spacing:1px"
            maxlength="12"
            required />
          <small class="img-hint">
            Formato: [letras][numeros]-[sufijo] — ej: A380-800 · 737-MAX · B787-9 · E195-E2
          </small>
        </div>

        <div class="form-field">
          <label for="aa-capacidad" class="form-label">Capacidad de Pasajeros *</label>
          <input type="number" id="aa-capacidad" class="form-input"
            bind:value={avionForm.capacidadPasajeros}
            placeholder="Ej: 240" min="1" max="900" required />
        </div>

        <div class="form-field">
          <label for="aa-imagen" class="form-label">Imagen del Avion</label>
          {#if avionImagenPreview}
            <img src={avionImagenPreview} alt="Preview" class="img-preview" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { avionImagenPreview = null; avionImagenBase64 = null; }}>
              Quitar imagen
            </button>
          {/if}
          <input id="aa-imagen" type="file" accept="image/*" class="form-input"
            on:change={onImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Max recomendado: 1 MB.</small>
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Avion
          </button>
          <button type="button" class="btn-secondary" on:click={cerrar}>Cancelar</button>
        </div>

      </form>
    </div>
  </div>
{/if}
