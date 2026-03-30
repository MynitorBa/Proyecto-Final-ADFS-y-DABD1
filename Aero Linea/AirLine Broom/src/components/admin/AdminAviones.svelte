<script>
// @ts-nocheck
  import { createEventDispatcher, onMount } from 'svelte';

  export let API;
  export let mostrarToast;
  export let mostrarConfirm;

  const dispatch = createEventDispatcher();

  let aviones        = [];
  let loadingAviones = false;
  let modoEdicion    = false;

  let mostrarFormulario  = false;
  let avionForm          = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
  let avionImagenPreview = null;
  let avionImagenBase64  = null;

  onMount(() => { cargarAviones(); });

  async function cargarAviones() {
    loadingAviones = true;
    try {
      const r = await fetch(`${API}/api/aviones`);
      if (r.ok) aviones = await r.json();
      else mostrarToast('error', 'Error al cargar aviones');
    } catch { mostrarToast('error', 'Error de conexión al cargar aviones'); }
    finally { loadingAviones = false; }
  }

  // ── Formato marca: solo letras y espacios, primera letra mayúscula ──
  function formatearMarca(e) {
    let val = e.target.value.replace(/[^a-zA-Z\s]/g, '');
    val = val.charAt(0).toUpperCase() + val.slice(1);
    avionForm.marca = val;
    e.target.value = val;
  }

  // ── Formato modelo real de avión: [0-2 letras][2-4 dígitos][-sufijo]
  // Ej: A380-800, 737-MAX, E195-E2, B787-9
  function formatearModelo(e) {
    let val = e.target.value.toUpperCase().replace(/[^A-Z0-9\-]/g, '');

    const guionIdx = val.indexOf('-');
    let base   = guionIdx >= 0 ? val.slice(0, guionIdx) : val;
    let sufijo = guionIdx >= 0 ? val.slice(guionIdx + 1) : '';

    // Base: máx 2 letras al inicio + máx 4 dígitos
    let letras  = base.replace(/[^A-Z]/g, '').slice(0, 2);
    let digitos = base.replace(/[^0-9]/g, '').slice(0, 4);

    // Sufijo: máx 5 chars alfanuméricos tras el guión
    sufijo = sufijo.replace(/[^A-Z0-9]/g, '').slice(0, 5);

    let resultado = letras + digitos;
    if (guionIdx >= 0) resultado += '-' + sufijo;

    avionForm.modelo = resultado;
    e.target.value   = resultado;
  }

  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { avionImagenBase64 = reader.result; avionImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  function abrirNuevo() {
    modoEdicion = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
    mostrarFormulario = true;
  }

  function abrirEditar(avion) {
    modoEdicion = true;
    avionForm = { id: avion.id, marca: avion.marca, modelo: avion.modelo, capacidadPasajeros: avion.capacidadPasajeros };
    avionImagenBase64 = null; avionImagenPreview = avion.imagenBase64 || null;
    mostrarFormulario = true;
  }

  function cerrar() {
    mostrarFormulario = false;
    avionForm = { id: null, marca: '', modelo: '', capacidadPasajeros: '' };
    avionImagenPreview = null; avionImagenBase64 = null;
  }

  async function handleGuardar() {
    if (!avionForm.marca.trim())  { mostrarToast('error', 'La marca es obligatoria'); return; }
    if (!avionForm.modelo.trim()) { mostrarToast('error', 'El modelo es obligatorio'); return; }
    if (avionForm.modelo.replace(/[^0-9]/g, '').length < 2) {
      mostrarToast('error', 'El modelo debe tener al menos 2 dígitos (ej: A380-800, 737-MAX)'); return;
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
        mostrarToast('success', modoEdicion ? 'Avión actualizado correctamente' : 'Avión creado correctamente');
        await cargarAviones();
        dispatch('avionesActualizados');
        cerrar();
      } else {
        const err = await r.json();
        mostrarToast('error', err.message || 'Error al guardar el avión');
      }
    } catch { mostrarToast('error', 'Error de conexión al guardar el avión'); }
  }

  async function handleEliminarImagen(avionId) {
    const ok = await mostrarConfirm('¿Quitar la imagen de este avión?', '', 'warning');
    if (!ok) return;
    try {
      const r = await fetch(`${API}/api/aviones/${avionId}/imagen`, { method: 'DELETE', credentials: 'include' });
      if (r.ok) {
        mostrarToast('success', 'Imagen eliminada');
        await cargarAviones();
        dispatch('avionesActualizados');
      } else { mostrarToast('error', 'Error al eliminar la imagen'); }
    } catch { mostrarToast('error', 'Error de conexión'); }
  }
</script>

<section class="admin-section">
  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Gestionar Aviones</h2>
      <p class="admin-section__subtitle">Administra la flota de aviones</p>
    </div>
    <button class="btn-add" on:click={abrirNuevo}>
      <span class="btn-add__icon">+</span> Nuevo Avión
    </button>
  </div>

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

{#if mostrarFormulario}
  <div class="modal-overlay" on:click={cerrar} role="dialog" aria-modal="true">
    <div class="modal" on:click|stopPropagation>
      <div class="modal__header">
        <h3 class="modal__title">{modoEdicion ? 'Editar' : 'Agregar'} Avión</h3>
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
            Formato: [letras][números]-[sufijo] — ej: A380-800 · 737-MAX · B787-9 · E195-E2
          </small>
        </div>

        <div class="form-field">
          <label for="aa-capacidad" class="form-label">Capacidad de Pasajeros *</label>
          <input type="number" id="aa-capacidad" class="form-input"
            bind:value={avionForm.capacidadPasajeros}
            placeholder="Ej: 240" min="1" max="900" required />
        </div>

        <div class="form-field">
          <label for="aa-imagen" class="form-label">Imagen del Avión</label>
          {#if avionImagenPreview}
            <img src={avionImagenPreview} alt="Preview" class="img-preview" />
            <button type="button" class="table__action-btn table__action-btn--cancel"
              on:click={() => { avionImagenPreview = null; avionImagenBase64 = null; }}>
              Quitar imagen
            </button>
          {/if}
          <input id="aa-imagen" type="file" accept="image/*" class="form-input"
            on:change={onImagenChange} />
          <small class="img-hint">JPG, PNG o WEBP. Máx recomendado: 1 MB.</small>
        </div>

        <div class="modal__actions">
          <button type="submit" class="btn-primary">
            {modoEdicion ? 'Actualizar' : 'Crear'} Avión
          </button>
          <button type="button" class="btn-secondary" on:click={cerrar}>Cancelar</button>
        </div>

      </form>
    </div>
  </div>
{/if}