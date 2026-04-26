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

  /** Controla si se muestran tambien los aviones inactivos en la tabla. @type {boolean} */
  let mostrarInactivos = false;

  /** Lista filtrada de aviones: Activos muestra solo activos, Inactivos muestra solo inactivos. @type {any[]} */
  $: avionesFiltrados = mostrarInactivos
    ? aviones.filter(a => a.activo === false)
    : aviones.filter(a => a.activo !== false);

  /** Conteo de aviones activos. @type {number} */
  $: totalActivos   = aviones.filter(a => a.activo !== false).length;
  /** Conteo de aviones inactivos. @type {number} */
  $: totalInactivos = aviones.filter(a => a.activo === false).length;

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
      // Siempre cargamos todos para poder mostrar conteos en ambas pestanas
      const r = await fetch(`${API}/api/aviones?incluirInactivos=true`);
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

  // ── Estado del modal de desactivacion ───────────────────────────────────
  /** Avion que se esta intentando desactivar. @type {any} */
  let avionDesactivar     = null;
  /** Indica si el modal de desactivacion esta visible. @type {boolean} */
  let mostrarModalDesact  = false;
  /** Vuelos que bloquean la desactivacion (<48h). @type {any[]} */
  let vuelos48h           = [];
  /** Vuelos que seran cancelados al confirmar (>48h). @type {any[]} */
  let vuelosLejanos       = [];
  /** Indica si se esta cargando la lista de vuelos del modal. @type {boolean} */
  let cargandoVuelosModal = false;
  /** Indica si se esta ejecutando la desactivacion. @type {boolean} */
  let desactivando        = false;

  /**
   * Reactiva un avion directamente (sin modal). Usado cuando activo === false.
   */
  async function reactivarAvion(id) {
    try {
      const res = await fetch(`${API}/api/aviones/${id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activo: true })
      });
      if (res.ok) {
        mostrarToast('success', 'Avion reactivado correctamente');
        mostrarInactivos = false;  // volver a pestaña Activos para ver el avión reactivado
        await cargarAviones();
      } else {
        const err = await res.json();
        mostrarToast('error', err.message || 'Error al reactivar el avion');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
  }

  /**
   * Abre el modal de desactivacion y carga los vuelos activos del avion
   * para mostrar al admin que se vera afectado.
   * @param {any} avion - El objeto avion de la fila.
   */
  async function intentarDesactivar(avion) {
    avionDesactivar     = avion;
    vuelos48h           = [];
    vuelosLejanos       = [];
    cargandoVuelosModal = true;
    mostrarModalDesact  = true;
    try {
      const r = await fetch(`${API}/api/aviones/${avion.id}/vuelos-activos`, { credentials: 'include' });
      if (r.ok) {
        const data  = await r.json();
        vuelos48h   = data.vuelos48h   ?? [];
        vuelosLejanos = data.vuelosLejanos ?? [];
      }
    } catch { /* silencioso — modal sigue abierto */ }
    finally { cargandoVuelosModal = false; }
  }

  /**
   * Confirma la desactivacion del avion, cancela vuelos lejanos (logica en backend)
   * y cierra el modal.
   */
  async function confirmarDesactivar() {
    desactivando = true;
    try {
      const res = await fetch(`${API}/api/aviones/${avionDesactivar.id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activo: false })
      });
      const data = await res.json();
      if (res.ok) {
        let msg = data.message || 'Avion desactivado correctamente';
        if (data.vuelosCancelados > 0)
          msg += ` — ${data.vuelosCancelados} vuelo(s) cancelado(s), ${data.pasajerosNotificados} pasajero(s) notificado(s)`;
        mostrarToast('success', msg);
        mostrarModalDesact = false;
        mostrarInactivos = true;   // cambiar a pestaña Inactivos para que el admin vea el avión y pueda reactivarlo
        await cargarAviones();
      } else {
        mostrarToast('error', data.message || 'Error al desactivar el avion');
        // Recargar vuelos por si cambió el estado
        await intentarDesactivar(avionDesactivar);
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { desactivando = false; }
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

  <!-- Barra de filtro para alternar entre activos e inactivos -->
  <div class="admin-filter-bar">
    <div class="filtro-tabs">
      <button class="filtro-tab" class:filtro-tab--active={!mostrarInactivos}
        on:click={() => { mostrarInactivos = false; }}>
        Activos <span class="filtro-tab__count">{totalActivos}</span>
      </button>
      <button class="filtro-tab" class:filtro-tab--active={mostrarInactivos}
        on:click={() => { mostrarInactivos = true; }}>
        Inactivos <span class="filtro-tab__count">{totalInactivos}</span>
      </button>
    </div>
  </div>

  <!-- Tabla de aviones con imagen, marca, modelo y capacidad de pasajeros -->
  {#if loadingAviones}
    <p class="loading-text">Cargando aviones...</p>

  {:else if avionesFiltrados.length === 0}
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
          <th class="table__header">Estado</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each avionesFiltrados as avion}
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
              {#if avion.activo === false}
                <span class="badge-inactivo">Inactivo</span>
              {:else}
                <span style="color:#198754;font-weight:600;font-size:0.8rem;">Activo</span>
              {/if}
            </td>
            <td class="table__cell">
              <div class="table__actions">
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => abrirEditar(avion)}>Editar</button>
                {#if avion.imagenBase64}
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => handleEliminarImagen(avion.id)}>Quitar img</button>
                {/if}
                <button
                  class="btn-estado"
                  class:btn-desactivar={avion.activo !== false}
                  class:btn-activar={avion.activo === false}
                  on:click={() => avion.activo === false ? reactivarAvion(avion.id) : intentarDesactivar(avion)}>
                  {avion.activo === false ? 'Reactivar' : 'Desactivar'}
                </button>
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

<!-- Modal de confirmacion de desactivacion de avion -->
{#if mostrarModalDesact}
  <div class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal modal--desact" on:click|stopPropagation>
      <div class="modal__header modal__header--warning">
        <h3 class="modal__title">Desactivar Avion</h3>
        <button class="modal__close" on:click={() => mostrarModalDesact = false} disabled={desactivando}>×</button>
      </div>

      <div class="modal__body">
        {#if cargandoVuelosModal}
          <p class="modal-loading">Verificando vuelos asignados...</p>

        {:else if vuelos48h.length > 0}
          <!-- Bloqueo: vuelos inminentes -->
          <div class="desact-alert desact-alert--error">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            No se puede desactivar: hay {vuelos48h.length} vuelo(s) en menos de 48 horas.
          </div>
          <p class="desact-sublabel">Vuelos bloqueantes:</p>
          <ul class="desact-vuelos-list">
            {#each vuelos48h as v}
              <li class="desact-vuelo-item desact-vuelo-item--block">
                <span class="vuelo-num">{v.numeroVuelo}</span>
                <span class="vuelo-ruta">{v.origen} → {v.destino}</span>
                <span class="vuelo-fecha">{v.fecha} {v.horaSalida}</span>
                <span class="vuelo-horas">{v.horasRestantes.toFixed(1)}h restantes</span>
              </li>
            {/each}
          </ul>
          <div class="modal__actions">
            <button class="btn-secondary" on:click={() => mostrarModalDesact = false}>Cerrar</button>
          </div>

        {:else}
          <!-- Confirmacion: sin bloqueo -->
          <p class="desact-avion-nombre">
            ¿Desactivar <strong>{avionDesactivar?.marca} {avionDesactivar?.modelo}</strong>?
          </p>

          {#if vuelosLejanos.length > 0}
            <div class="desact-alert desact-alert--warn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              Se cancelaran {vuelosLejanos.length} vuelo(s). Los pasajeros seran notificados por correo.
            </div>
            <p class="desact-sublabel">Vuelos que seran cancelados:</p>
            <ul class="desact-vuelos-list">
              {#each vuelosLejanos as v}
                <li class="desact-vuelo-item desact-vuelo-item--cancel">
                  <span class="vuelo-num">{v.numeroVuelo}</span>
                  <span class="vuelo-ruta">{v.origen} → {v.destino}</span>
                  <span class="vuelo-fecha">{v.fecha} {v.horaSalida}</span>
                </li>
              {/each}
            </ul>
          {:else}
            <p class="desact-ok">El avion no tiene vuelos activos. Se puede desactivar sin efectos adicionales.</p>
          {/if}

          <div class="modal__actions">
            <button class="btn-danger" on:click={confirmarDesactivar} disabled={desactivando}>
              {desactivando ? 'Desactivando...' : 'Confirmar desactivacion'}
            </button>
            <button class="btn-secondary" on:click={() => mostrarModalDesact = false} disabled={desactivando}>Cancelar</button>
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}

<style>
  .admin-filter-bar { margin-bottom: 1rem; }
  .filtro-tabs { display: flex; gap: 0; border: 1px solid #d1d5db; border-radius: 8px; overflow: hidden; width: fit-content; }
  .filtro-tab { display: flex; align-items: center; gap: 0.45rem; padding: 0.4rem 1.1rem; font-size: 0.875rem; font-weight: 600; border: none; background: #f9fafb; color: #6b7280; cursor: pointer; transition: all 0.18s; }
  .filtro-tab:hover:not(.filtro-tab--active) { background: #f3f4f6; color: #374151; }
  .filtro-tab--active { background: #1C1A18; color: #D4AF37; }
  .filtro-tab__count { display: inline-flex; align-items: center; justify-content: center; min-width: 1.4rem; height: 1.4rem; padding: 0 0.35rem; border-radius: 999px; font-size: 0.7rem; font-weight: 700; background: rgba(255,255,255,0.18); color: inherit; }
  .filtro-tab--active .filtro-tab__count { background: rgba(212,175,55,0.25); color: #D4AF37; }
  .filtro-tab:not(.filtro-tab--active) .filtro-tab__count { background: #e5e7eb; color: #374151; }
  .btn-estado { padding: 0.35rem 0.75rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.8rem; font-weight: 600; transition: all 0.2s; }
  .btn-desactivar { background: #fff3cd; color: #856404; }
  .btn-desactivar:hover { background: #ffc107; color: #000; }
  .btn-activar { background: #d1e7dd; color: #0a3622; }
  .btn-activar:hover { background: #198754; color: #fff; }
  .badge-inactivo { background: #e9ecef; color: #6c757d; padding: 0.2rem 0.5rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }

  /* Modal de desactivacion */
  .modal--desact { max-width: 540px; }
  .modal__header--warning { background: #fff8e1; border-bottom: 1px solid #ffe082; }
  .modal__body { padding: 1.25rem 1.5rem; }
  .modal-loading { color: #6b7280; font-style: italic; }
  .desact-avion-nombre { margin-bottom: 1rem; font-size: 1rem; color: #374151; }
  .desact-alert { display: flex; align-items: flex-start; gap: 0.5rem; padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.875rem; font-weight: 500; margin-bottom: 1rem; }
  .desact-alert--error { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
  .desact-alert--warn  { background: #fff3cd; color: #856404; border: 1px solid #ffe082; }
  .desact-sublabel { font-size: 0.8rem; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; }
  .desact-vuelos-list { list-style: none; padding: 0; margin: 0 0 1.25rem; display: flex; flex-direction: column; gap: 0.4rem; max-height: 220px; overflow-y: auto; }
  .desact-vuelo-item { display: flex; align-items: center; gap: 0.6rem; padding: 0.45rem 0.75rem; border-radius: 6px; font-size: 0.8rem; flex-wrap: wrap; }
  .desact-vuelo-item--block  { background: #fee2e2; border: 1px solid #fca5a5; }
  .desact-vuelo-item--cancel { background: #fff3cd; border: 1px solid #ffe082; }
  .vuelo-num   { font-weight: 700; min-width: 70px; }
  .vuelo-ruta  { color: #374151; }
  .vuelo-fecha { color: #6b7280; font-size: 0.75rem; }
  .vuelo-horas { margin-left: auto; font-weight: 600; color: #dc2626; font-size: 0.75rem; }
  .desact-ok   { color: #166534; background: #dcfce7; border: 1px solid #86efac; padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.875rem; margin-bottom: 1.25rem; }
  .btn-danger  { padding: 0.55rem 1.25rem; background: #dc2626; color: #fff; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 0.9rem; transition: background 0.2s; }
  .btn-danger:hover:not(:disabled) { background: #b91c1c; }
  .btn-danger:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
