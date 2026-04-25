<script>
/**
 * @file AdminTripulantes.svelte
 * @description Seccion del panel de administracion para gestionar tripulantes. Muestra una tabla con
 * todos los tripulantes con su foto, ID, nombre, apellido y insignia de rol. Permite crear nuevos
 * tripulantes y editar los existentes mediante un formulario modal. El formulario incluye nombre,
 * apellido, rol (cargado desde la API) y una foto de perfil opcional subida en base64. Las fotos
 * de perfil tambien pueden eliminarse individualmente mediante un dialogo de confirmacion. Despacha
 * 'tripulantesActualizados' al padre tras cualquier creacion, actualizacion o eliminacion de foto
 * exitosa para que el padre pueda actualizar su propia lista de tripulantes.
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

  /** Lista de tripulantes registrados actualmente en el sistema. @type {any[]} */
  let tripulantes        = [];

  /** Controla si se muestran tambien los tripulantes inactivos en la tabla. @type {boolean} */
  let mostrarInactivos = false;

  /** Lista filtrada de tripulantes segun el estado mostrarInactivos. @type {any[]} */
  $: tripulantesFiltrados = mostrarInactivos ? tripulantes : tripulantes.filter(t => t.activo !== false);

  /** Roles de tripulacion disponibles obtenidos del backend, mapeados a { id, nombre }. @type {{ id: number, nombre: string }[]} */
  let rolesTripulacion   = [];

  /** Indica si la carga de la lista de tripulantes esta en progreso. @type {boolean} */
  let loadingTripulantes = false;

  /** Verdadero cuando el modal esta editando un tripulante existente, falso cuando crea uno nuevo. @type {boolean} */
  let modoEdicion        = false;

  /** Indica si el formulario modal de creacion/edicion esta visible. @type {boolean} */
  let mostrarFormulario       = false;

  /**
   * Datos del formulario vinculado a los campos del modal de creacion/edicion.
   * @type {{ id: number|null, nombre: string, apellido: string, rolID: string|number }}
   */
  let tripulanteForm          = { id: null, nombre: '', apellido: '', rolID: '' };

  /** URL de datos de la vista previa de foto mostrada en el modal antes de guardar. @type {string|null} */
  let tripulanteImagenPreview = null;

  /** Cadena de foto en base64 enviada al backend al enviar el formulario. @type {string|null} */
  let tripulanteImagenBase64  = null;

  /**
   * Al montar: carga la lista de tripulantes y los roles disponibles en paralelo.
   * @async
   * @returns {Promise<void>}
   */
  onMount(async () => {
    await Promise.all([cargarTripulantes(), cargarRoles()]);
  });

  /**
   * Obtiene la lista completa de tripulantes desde el backend y la almacena en tripulantes.
   * Muestra un toast en caso de error y establece loadingTripulantes durante la solicitud.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarTripulantes() {
    loadingTripulantes = true;
    try {
      const r = await fetch(`${API}/api/tripulacion?incluirInactivos=${mostrarInactivos}`);
      if (r.ok) tripulantes = await r.json();
      else mostrarToast('error', 'Error al cargar tripulantes');
    } catch { mostrarToast('error', 'Error de conexion al cargar tripulantes'); }
    finally { loadingTripulantes = false; }
  }

  /**
   * Obtiene los roles de tripulacion disponibles desde la API del backend y mapea cada entrada a { id, nombre }
   * usando el campo cargo como nombre de visualizacion. Registra un error en la consola si falla.
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
   * Lee el archivo seleccionado en el input de foto, lo convierte a una URL de datos base64 y lo almacena
   * en tripulanteImagenBase64 (para envio) y tripulanteImagenPreview (para visualizacion).
   * @param {Event} e - El evento de cambio del elemento input de archivo.
   */
  function onImagenChange(e) {
    const file = e.target.files[0]; if (!file) return;
    const reader = new FileReader();
    reader.onload = () => { tripulanteImagenBase64 = reader.result; tripulanteImagenPreview = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Reinicia el formulario a valores vacios y abre el modal en modo de creacion.
   */
  function abrirNuevo() {
    modoEdicion = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
    mostrarFormulario = true;
  }

  /**
   * Pre-rellena el formulario con los datos del tripulante seleccionado y abre el modal en modo de edicion.
   * @param {any} t - El objeto fila del tripulante de la tabla.
   */
  function abrirEditar(t) {
    modoEdicion = true;
    tripulanteForm = { id: t.id, nombre: t.nombre, apellido: t.apellido, rolID: t.rolID };
    tripulanteImagenBase64 = null; tripulanteImagenPreview = t.imagenBase64 || null;
    mostrarFormulario = true;
  }

  /**
   * Cierra el modal y reinicia todos los campos del formulario y el estado de la foto.
   */
  function cerrar() {
    mostrarFormulario = false;
    tripulanteForm = { id: null, nombre: '', apellido: '', rolID: '' };
    tripulanteImagenPreview = null; tripulanteImagenBase64 = null;
  }

  /**
   * Valida nombre, apellido y rolID, luego envia una solicitud POST o PUT al backend.
   * Si tiene exito recarga la lista de tripulantes, despacha 'tripulantesActualizados' y cierra el modal.
   * Muestra toasts de error para fallos de validacion o errores de la API.
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
   * Pide confirmacion y luego envia una solicitud DELETE para eliminar la foto de un registro de tripulante.
   * Si tiene exito recarga la lista de tripulantes y despacha 'tripulantesActualizados'.
   * @async
   * @param {number} tripulanteId - El ID del tripulante cuya foto debe eliminarse.
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

  // ── Estado del modal de desactivacion ───────────────────────────────────
  /** Tripulante que se esta intentando desactivar. @type {any} */
  let tripulanteDesactivar     = null;
  /** Indica si el modal de desactivacion esta visible. @type {boolean} */
  let mostrarModalDesact       = false;
  /** Vuelos que bloquean la desactivacion (<48h). @type {any[]} */
  let vuelos48h                = [];
  /** Vuelos de los que sera desasignado al confirmar (>48h). @type {any[]} */
  let vuelosLejanos            = [];
  /** Indica si se esta cargando la lista de vuelos del modal. @type {boolean} */
  let cargandoVuelosModal      = false;
  /** Indica si se esta ejecutando la desactivacion. @type {boolean} */
  let desactivando             = false;

  // ── Estado de reemplazos por vuelo ───────────────────────────────────────
  /**
   * Equipo actual de cada vuelo afectado. { [vueloId]: Tripulante[] }
   * @type {Record<number, any[]>}
   */
  let equiposVuelo = {};

  /**
   * Tripulantes seleccionados como reemplazo para cada vuelo. { [vueloId]: number[] }
   * @type {Record<number, number[]>}
   */
  let reemplazosSeleccionados = {};

  // IDs de rol para piloto y copiloto (resueltos por nombre de cargo)
  $: rolIdPiloto    = rolesTripulacion.find(r => {
    const n = r.nombre.toLowerCase();
    return n.includes('piloto') && !n.includes('co');
  })?.id ?? -1;
  $: rolIdCopiloto  = rolesTripulacion.find(r =>
    r.nombre.toLowerCase().includes('copiloto') || r.nombre.toLowerCase().includes('co-piloto')
  )?.id ?? -2;

  /**
   * Verifica si un vuelo tiene la composicion minima: 1 piloto + 1 copiloto + 3 auxiliares
   * considerando el equipo actual SIN el tripulante que se desactiva, MAS los reemplazos elegidos.
   * @param {number} vueloId
   * @returns {boolean}
   */
  function vueloTieneComposicionValida(vueloId) {
    const equipoActual    = (equiposVuelo[vueloId] ?? []).filter(t => t.id !== tripulanteDesactivar?.id);
    const idsReemplazo    = reemplazosSeleccionados[vueloId] ?? [];
    const nuevos          = tripulantes.filter(t => idsReemplazo.includes(t.id));
    const equipoFinal     = [...equipoActual, ...nuevos];

    const pilotos    = equipoFinal.filter(t => t.rolID === rolIdPiloto).length;
    const copilotos  = equipoFinal.filter(t => t.rolID === rolIdCopiloto).length;
    const auxiliares = equipoFinal.filter(t => t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto).length;

    return pilotos >= 1 && copilotos >= 1 && auxiliares >= 3;
  }

  /** Verdadero cuando todos los vuelos lejanos tienen composicion valida. */
  $: todosVuelosValidos = vuelosLejanos.length > 0 &&
    vuelosLejanos.every(v => vueloTieneComposicionValida(v.id));

  /**
   * Devuelve el resumen de composicion de un vuelo (tras quitar al saliente y agregar reemplazos).
   * @param {number} vueloId
   */
  function resumenComposicion(vueloId) {
    const equipoActual = (equiposVuelo[vueloId] ?? []).filter(t => t.id !== tripulanteDesactivar?.id);
    const idsReemplazo = reemplazosSeleccionados[vueloId] ?? [];
    const nuevos       = tripulantes.filter(t => idsReemplazo.includes(t.id));
    const equipoFinal  = [...equipoActual, ...nuevos];

    const pilotos    = equipoFinal.filter(t => t.rolID === rolIdPiloto).length;
    const copilotos  = equipoFinal.filter(t => t.rolID === rolIdCopiloto).length;
    const auxiliares = equipoFinal.filter(t => t.rolID !== rolIdPiloto && t.rolID !== rolIdCopiloto).length;

    return { pilotos, copilotos, auxiliares, valido: pilotos >= 1 && copilotos >= 1 && auxiliares >= 3 };
  }

  /**
   * Alterna la seleccion de un tripulante como reemplazo en un vuelo especifico.
   * @param {number} vueloId
   * @param {number} tripId
   */
  function toggleReemplazo(vueloId, tripId) {
    const actuales = reemplazosSeleccionados[vueloId] ?? [];
    if (actuales.includes(tripId)) {
      reemplazosSeleccionados = { ...reemplazosSeleccionados, [vueloId]: actuales.filter(id => id !== tripId) };
    } else {
      reemplazosSeleccionados = { ...reemplazosSeleccionados, [vueloId]: [...actuales, tripId] };
    }
  }

  /**
   * Reactiva un tripulante directamente (sin modal).
   * @param {number} id
   */
  async function reactivarTripulante(id) {
    try {
      const res = await fetch(`${API}/api/tripulacion/${id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activo: true })
      });
      if (res.ok) {
        mostrarToast('success', 'Tripulante reactivado correctamente');
        await cargarTripulantes();
      } else {
        const err = await res.json();
        mostrarToast('error', err.message || 'Error al reactivar el tripulante');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
  }

  /**
   * Abre el modal de desactivacion, carga los vuelos asignados y el equipo de cada vuelo lejano.
   * @param {any} t - El objeto tripulante de la fila.
   */
  async function intentarDesactivarTripulante(t) {
    tripulanteDesactivar    = t;
    vuelos48h               = [];
    vuelosLejanos           = [];
    equiposVuelo            = {};
    reemplazosSeleccionados = {};
    cargandoVuelosModal     = true;
    mostrarModalDesact      = true;

    try {
      const r = await fetch(`${API}/api/tripulacion/${t.id}/vuelos-asignados`, { credentials: 'include' });
      if (r.ok) {
        const data    = await r.json();
        vuelos48h     = data.vuelos48h     ?? [];
        vuelosLejanos = data.vuelosLejanos ?? [];

        // Cargar el equipo actual de cada vuelo lejano en paralelo
        await Promise.all(vuelosLejanos.map(async vuelo => {
          try {
            const re = await fetch(`${API}/api/tripulacion/vuelo/${vuelo.id}/equipo`, { credentials: 'include' });
            if (re.ok) {
              const equipo = await re.json();
              equiposVuelo = { ...equiposVuelo, [vuelo.id]: equipo };
            }
          } catch { /* silencioso */ }
        }));
      }
    } catch { /* silencioso — modal sigue abierto */ }
    finally { cargandoVuelosModal = false; }
  }

  /**
   * Confirma la desactivacion del tripulante enviando los reemplazos seleccionados.
   * El backend asignara los nuevos tripulantes, desasignara al saliente y notificara pasajeros.
   */
  async function confirmarDesactivarTripulante() {
    desactivando = true;
    try {
      const reemplazos = vuelosLejanos.map(v => ({
        vueloId: v.id,
        nuevosTripulantesIds: reemplazosSeleccionados[v.id] ?? []
      }));

      const res = await fetch(`${API}/api/tripulacion/${tripulanteDesactivar.id}/estado`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ activo: false, reemplazos })
      });
      const data = await res.json();
      if (res.ok) {
        let msg = data.message || 'Tripulante desactivado correctamente';
        if (data.pasajerosNotificados > 0)
          msg += `. ${data.pasajerosNotificados} pasajero(s) notificado(s) por correo.`;
        mostrarToast('success', msg);
        mostrarModalDesact = false;
        await cargarTripulantes();
        dispatch('tripulantesActualizados');
      } else {
        mostrarToast('error', data.message || 'Error al desactivar el tripulante');
      }
    } catch { mostrarToast('error', 'Error de conexion'); }
    finally { desactivando = false; }
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

  <!-- Barra de filtro para mostrar tripulantes inactivos -->
  <div class="admin-filter-bar">
    <label class="filter-toggle">
      <input type="checkbox" bind:checked={mostrarInactivos}
        on:change={cargarTripulantes}>
      Mostrar tripulantes inactivos
    </label>
  </div>

  <!-- Tabla de tripulantes con foto, nombre, apellido y rol -->
  {#if loadingTripulantes}
    <p class="loading-text">Cargando tripulantes...</p>

  {:else if tripulantesFiltrados.length === 0}
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
          <th class="table__header">Estado</th>
          <th class="table__header">Acciones</th>
        </tr>
      </thead>
      <tbody class="table__body">
        {#each tripulantesFiltrados as t}
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
            <td class="table__cell" data-label="Estado">
              {#if t.activo === false}
                <span class="badge-inactivo">Inactivo</span>
              {:else}
                <span style="color:#198754;font-weight:600;font-size:0.8rem;">Activo</span>
              {/if}
            </td>
            <td class="table__cell" data-label="Acciones">
              <div class="table__actions">
                <button class="table__action-btn table__action-btn--view"
                  on:click={() => abrirEditar(t)}>Editar</button>
                {#if t.imagenBase64}
                  <button class="table__action-btn table__action-btn--cancel"
                    on:click={() => handleEliminarFoto(t.id)}>Quitar foto</button>
                {/if}
                <button
                  class="btn-estado"
                  class:btn-desactivar={t.activo !== false}
                  class:btn-activar={t.activo === false}
                  on:click={() => t.activo === false ? reactivarTripulante(t.id) : intentarDesactivarTripulante(t)}>
                  {t.activo === false ? 'Reactivar' : 'Desactivar'}
                </button>
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

<!-- Modal de confirmacion de desactivacion de tripulante -->
{#if mostrarModalDesact}
  <div class="modal-overlay" role="dialog" aria-modal="true">
    <div class="modal modal--desact" on:click|stopPropagation>
      <div class="modal__header modal__header--warning">
        <h3 class="modal__title">Desactivar Tripulante</h3>
        <button class="modal__close" on:click={() => mostrarModalDesact = false} disabled={desactivando}>×</button>
      </div>

      <div class="modal__body">
        {#if cargandoVuelosModal}
          <p class="modal-loading">Verificando vuelos asignados...</p>

        {:else if vuelos48h.length > 0}
          <!-- Bloqueo: vuelos inminentes -->
          <div class="desact-alert desact-alert--error">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            No se puede desactivar: hay {vuelos48h.length} vuelo(s) asignado(s) en menos de 48 horas.
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
          <!-- Sin bloqueo -->
          <p class="desact-nombre">
            ¿Desactivar a <strong>{tripulanteDesactivar?.nombre} {tripulanteDesactivar?.apellido}</strong>?
          </p>

          {#if vuelosLejanos.length > 0}
            <!-- ── AVISO OBLIGATORIO DE REEMPLAZO ── -->
            <div class="desact-alert desact-alert--warn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              <div>
                <strong>Reemplazo obligatorio</strong> — Este tripulante tiene {vuelosLejanos.length} vuelo(s) programado(s).
                Debes seleccionar reemplazos para que cada vuelo mantenga:
                <span class="cuota-badge">1 Piloto</span>
                <span class="cuota-badge">1 Copiloto</span>
                <span class="cuota-badge">3 Auxiliares</span>
              </div>
            </div>

            <!-- ── SELECTOR DE REEMPLAZOS POR VUELO ── -->
            {#each vuelosLejanos as vuelo}
              {@const composicion = resumenComposicion(vuelo.id)}
              <div class="vuelo-reemplazo-card" class:vuelo-reemplazo-card--ok={composicion.valido} class:vuelo-reemplazo-card--err={!composicion.valido}>
                <!-- Cabecera del vuelo -->
                <div class="vrc-header">
                  <span class="vuelo-num">{vuelo.numeroVuelo}</span>
                  <span class="vuelo-ruta">{vuelo.origen} → {vuelo.destino}</span>
                  <span class="vuelo-fecha">{vuelo.fecha} {vuelo.horaSalida}</span>
                  {#if composicion.valido}
                    <span class="vrc-badge vrc-badge--ok">Listo</span>
                  {:else}
                    <span class="vrc-badge vrc-badge--err">Incompleto</span>
                  {/if}
                </div>

                <!-- Composicion actual -->
                <div class="vrc-composicion">
                  <span class:comp-ok={composicion.pilotos >= 1} class:comp-err={composicion.pilotos < 1}>
                    Pilotos: {composicion.pilotos}/1
                  </span>
                  <span class:comp-ok={composicion.copilotos >= 1} class:comp-err={composicion.copilotos < 1}>
                    Copilotos: {composicion.copilotos}/1
                  </span>
                  <span class:comp-ok={composicion.auxiliares >= 3} class:comp-err={composicion.auxiliares < 3}>
                    Auxiliares: {composicion.auxiliares}/3
                  </span>
                </div>

                <!-- Selector de reemplazos (tripulantes activos disponibles, excluye al saliente y a los ya en el vuelo) -->
                <p class="vrc-sublabel">Selecciona reemplazos para este vuelo:</p>
                <div class="vrc-tripulantes-grid">
                  {#each tripulantes.filter(t => t.activo !== false && t.id !== tripulanteDesactivar?.id && !(equiposVuelo[vuelo.id] ?? []).some(e => e.id === t.id)) as t}
                    {@const seleccionado = (reemplazosSeleccionados[vuelo.id] ?? []).includes(t.id)}
                    <button
                      type="button"
                      class="vrc-trip-btn"
                      class:vrc-trip-btn--sel={seleccionado}
                      on:click={() => toggleReemplazo(vuelo.id, t.id)}>
                      <span class="vrc-trip-nombre">{t.nombre} {t.apellido}</span>
                      <span class="vrc-trip-rol">{t.nombreRol}</span>
                    </button>
                  {/each}
                  {#if tripulantes.filter(t => t.id !== tripulanteDesactivar?.id && !(equiposVuelo[vuelo.id] ?? []).some(e => e.id === t.id)).length === 0}
                    <p class="vrc-sin-disponibles">No hay tripulantes disponibles para asignar.</p>
                  {/if}
                </div>
              </div>
            {/each}

            <!-- Nota de correos -->
            <p class="desact-nota-correo">
              Los pasajeros con reservas activas o pendientes de pago en estos vuelos seran notificados por correo sobre el cambio de personal.
            </p>

          {:else}
            <p class="desact-ok">El tripulante no tiene vuelos asignados. Se puede desactivar sin efectos adicionales.</p>
          {/if}

          <div class="modal__actions">
            <button
              class="btn-danger"
              on:click={confirmarDesactivarTripulante}
              disabled={desactivando || (vuelosLejanos.length > 0 && !todosVuelosValidos)}>
              {#if desactivando}
                Desactivando...
              {:else if vuelosLejanos.length > 0 && !todosVuelosValidos}
                Completa los reemplazos para continuar
              {:else}
                Confirmar desactivacion
              {/if}
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
  .filter-toggle { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.9rem; color: #555; }
  .filter-toggle input { cursor: pointer; }
  .btn-estado { padding: 0.35rem 0.75rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.8rem; font-weight: 600; transition: all 0.2s; }
  .btn-desactivar { background: #fff3cd; color: #856404; }
  .btn-desactivar:hover { background: #ffc107; color: #000; }
  .btn-activar { background: #d1e7dd; color: #0a3622; }
  .btn-activar:hover { background: #198754; color: #fff; }
  .badge-inactivo { background: #e9ecef; color: #6c757d; padding: 0.2rem 0.5rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }

  /* Modal de desactivacion */
  .modal--desact { max-width: 700px; }
  .modal__header--warning { background: #fff8e1; border-bottom: 1px solid #ffe082; }
  .modal__body { padding: 1.25rem 1.5rem; }
  .modal-loading { color: #6b7280; font-style: italic; }
  .desact-nombre { margin-bottom: 1rem; font-size: 1rem; color: #374151; }
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

  /* ── Cuotas de composicion ── */
  .cuota-badge { display: inline-block; background: #1C1A18; color: #F2EFEA; font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; margin: 0 0.15rem; }

  /* ── Tarjeta de reemplazo por vuelo ── */
  .vuelo-reemplazo-card { border: 1.5px solid #e5e7eb; border-radius: 10px; padding: 1rem; margin-bottom: 1rem; background: #fafafa; }
  .vuelo-reemplazo-card--ok  { border-color: #86efac; background: #f0fdf4; }
  .vuelo-reemplazo-card--err { border-color: #fca5a5; background: #fff8f8; }
  .vrc-header { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; margin-bottom: 0.6rem; }
  .vrc-badge { font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; margin-left: auto; }
  .vrc-badge--ok  { background: #dcfce7; color: #166534; }
  .vrc-badge--err { background: #fee2e2; color: #991b1b; }

  /* Composicion numerica */
  .vrc-composicion { display: flex; gap: 1rem; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.75rem; flex-wrap: wrap; }
  .comp-ok  { color: #166534; }
  .comp-err { color: #dc2626; }

  .vrc-sublabel { font-size: 0.75rem; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; }

  /* Grid de tripulantes disponibles */
  .vrc-tripulantes-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 0.4rem; max-height: 180px; overflow-y: auto; }
  .vrc-trip-btn { display: flex; flex-direction: column; align-items: flex-start; gap: 0.1rem; padding: 0.45rem 0.65rem; border: 1.5px solid #e5e7eb; border-radius: 7px; background: #fff; cursor: pointer; font-size: 0.8rem; text-align: left; transition: all 0.15s; }
  .vrc-trip-btn:hover { border-color: #B89A7A; background: #fdf8f3; }
  .vrc-trip-btn--sel { border-color: #1C1A18; background: #1C1A18; color: #F2EFEA; }
  .vrc-trip-nombre { font-weight: 600; line-height: 1.2; }
  .vrc-trip-rol    { font-size: 0.7rem; opacity: 0.75; }
  .vrc-sin-disponibles { font-size: 0.8rem; color: #9ca3af; font-style: italic; grid-column: 1 / -1; }

  /* Nota de correos */
  .desact-nota-correo { font-size: 0.8rem; color: #6b7280; background: #f3f4f6; border-radius: 6px; padding: 0.6rem 0.85rem; margin-top: 0.5rem; margin-bottom: 0.5rem; }
</style>
