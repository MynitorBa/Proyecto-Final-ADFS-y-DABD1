<script>
/**
 * @file AdminAviones.svelte
 * @description Seccion del panel de administracion para gestionar la flota de aviones. Muestra una tabla con
 * todos los aviones registrados con su miniatura de imagen, ID, marca, modelo y capacidad de pasajeros.
 * Permite crear nuevos aviones y editar los existentes mediante un formulario modal. El campo de marca
 * esta restringido solo a letras (primera letra en mayuscula), y el campo de modelo tiene formato
 * [0-2 letras][2-4 digitos][-sufijo] (por ejemplo, A380-800, 737-MAX). Se puede subir una imagen
 * opcional en base64. Despacha 'avionesActualizados' al padre tras cualquier creacion, actualizacion
 * o eliminacion de imagen exitosa.
 */
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  /** Lista de aviones registrados actualmente en el sistema, cargada desde la API del backend. @type {any[]} */
  let aviones        = [];

  /** Indica si la carga de la lista de aviones esta en progreso. @type {boolean} */
  let loadingAviones = false;

  /** Verdadero cuando el modal esta editando un avion existente, falso cuando crea uno nuevo. @type {boolean} */
  let modoEdicion    = false;

  /** Indica si el formulario modal de creacion/edicion esta visible. @type {boolean} */
  let mostrarFormulario  = false;

  /**
   * Objeto de datos del formulario vinculado a los campos del formulario de creacion/edicion.
   * @type {{ id: number|null, marca: string, modelo: string, capacidadPasajeros: string|number }}
   */
  let avionForm          = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };

  /** URL de datos de la vista previa de imagen seleccionada mostrada antes de guardar. @type {string|null} */
  let avionImagenPreview = null;

  /** Cadena de imagen en base64 enviada al backend al enviar el formulario. @type {string|null} */
  let avionImagenBase64  = null;

  /**
   * Al montar: carga la lista de aviones desde el backend.
   */
  onMount(() => { cargarAviones(); });

  /**
   * Obtiene la lista de aviones desde la API del backend y los almacena en aviones.
   * Muestra un toast en caso de error y establece loadingAviones durante la solicitud.
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
   * Maneja el input en el campo de marca. Elimina cualquier caracter que no sea letra ni espacio y
   * pone en mayuscula la primera letra antes de actualizar avionForm.marca y el valor del elemento input.
   * @param {Event} e - El evento de input del campo de texto de marca.
   */
  function formatearMarca(e) {
    let val = e.target.value.replace(/[^a-zA-Z\s]/g, '');
    val = val.charAt(0).toUpperCase() + val.slice(1);
    avionForm.marca = val;
    e.target.value = val;
  }

  /**
   * Maneja el input en el campo de modelo. Aplica el formato real de modelo de avion:
   * hasta 2 letras mayusculas seguidas de hasta 4 digitos, opcionalmente con un guion y
   * hasta 5 caracteres alfanumericos de sufijo (por ejemplo, A380-800, 737-MAX, E195-E2, B787-9).
   * Actualiza avionForm.modelo y el valor del elemento input.
   * @param {Event} e - El evento de input del campo de texto de modelo.
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
   * Lee el archivo seleccionado en el input de imagen, lo convierte a una URL de datos base64 y lo almacena
   * en avionImagenBase64 (para envio) y avionImagenPreview (para visualizacion).
   * @param {Event} e - El evento de cambio del elemento input de archivo.
   */
  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { avionImagenBase64 = reader.result; avionImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Reinicia el formulario a valores vacios y abre el modal en modo de creacion.
   */
  function abrirNuevo() {
    modoEdicion = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
    mostrarFormulario = true;
  }

  /**
   * Pre-rellena el formulario con los datos del avion seleccionado y abre el modal en modo de edicion.
   * @param {any} avion - El objeto fila del avion de la tabla.
   */
  function abrirEditar(avion) {
    modoEdicion = true;
    avionForm = { id: avion.id, marca: avion.marca, modelo: avion.modelo, capacidadPasajeros: avion.capacidadPasajeros };
    avionImagenBase64 = null; avionImagenPreview = avion.imagenBase64 || null;
    mostrarFormulario = true;
  }

  /**
   * Cierra el modal y reinicia todo el estado del formulario y las vistas previas de imagen.
   */
  function cerrar() {
    mostrarFormulario = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
  }

  /**
   * Valida el formulario (marca requerida, modelo requerido y debe tener al menos 2 digitos,
   * capacidadPasajeros debe ser mayor o igual a 1) y luego envia una solicitud POST o PUT al backend.
   * Si tiene exito recarga la lista de aviones, despacha 'avionesActualizados' y cierra el modal.
   * Muestra toasts de error para fallos de validacion o errores de la API.
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
   * Pide confirmacion y luego envia una solicitud DELETE para eliminar la imagen de un registro de avion.
   * Si tiene exito recarga la lista de aviones y despacha 'avionesActualizados'.
   * @async
   * @param {number} avionId - El ID del avion cuya imagen debe eliminarse.
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
