<script>
/**
 * @file AdminAgencias.svelte
 * @description Seccion del panel de administracion para gestionar agencias de viaje. Muestra una barra de
 * estadisticas de resumen (total, activas, inactivas, sin usuario asignado) y una tabla de todas las agencias.
 * Proporciona cuatro dialogos modales: uno para crear una nueva agencia (con nombre, correo, URL, usuario
 * webservice y descuento inicial), uno para asignar un usuario webservice disponible a una agencia existente,
 * uno para editar el porcentaje de descuento de una agencia y uno para editar su URL publica. El estado de
 * la agencia tambien puede cambiarse inline mediante un elemento select en la fila de la tabla. Todas las
 * mutaciones llaman a la API del backend y actualizan el estado local al tener exito.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  /** Lista de agencias cargadas desde el backend. @type {any[]} */
  let agencias           = [];

  /** Usuarios con rol webservice que aun no tienen ninguna entidad asignada, usados para poblar los selectores de usuario. @type {any[]} */
  let usuariosDisponibles = [];

  /** Indica si la carga principal de datos esta en progreso. @type {boolean} */
  let cargando           = false;

  // -- Estado del modal de creacion --

  /** Controla la visibilidad del modal de creacion de agencia. @type {boolean} */
  let modalCrear         = false;

  /** Indica si la solicitud de la API para crear agencia esta en vuelo. @type {boolean} */
  let creando            = false;

  /** Valor del campo nombre de agencia para el formulario de creacion. @type {string} */
  let crearNombre        = '';

  /** Valor del campo correo de agencia para el formulario de creacion. @type {string} */
  let crearCorreo        = '';

  /** URL publica de la agencia para el formulario de creacion. @type {string} */
  let crearUrl           = '';

  /** ID de usuario webservice seleccionado para el formulario de creacion. @type {string} */
  let crearUsuarioId     = '';

  /** Valor de porcentaje de descuento inicial para el formulario de creacion. @type {number} */
  let crearDescuento     = 0;

  /** Errores de validacion a nivel de campo para el formulario de creacion. @type {Record<string, string>} */
  let crearErrores       = {};

  // -- Estado del modal de asignacion de usuario --

  /** Controla la visibilidad del modal de asignacion de usuario. @type {boolean} */
  let modalAsignar       = false;

  /** Indica si la solicitud de la API para asignar usuario esta en vuelo. @type {boolean} */
  let asignando          = false;

  /** La fila de agencia actualmente seleccionada para asignacion de usuario. @type {any} */
  let agenciaSeleccionada = null;

  /** ID de usuario webservice seleccionado en el modal de asignacion de usuario. @type {string} */
  let asignarUsuarioId   = '';

  // -- Estado del modal de edicion de descuento --

  /** Controla la visibilidad del modal de edicion de descuento. @type {boolean} */
  let modalDescuento     = false;

  /** Indica si la solicitud de la API para guardar descuento esta en vuelo. @type {boolean} */
  let guardandoDescuento = false;

  /** Valor actual del porcentaje de descuento que se esta editando. @type {number} */
  let descuentoEditando  = 0;

  /** La fila de agencia que se esta editando en el modal de descuento. @type {any} */
  let agenciaDescuento   = null;

  // -- Estado del modal de edicion de URL --

  /** Controla la visibilidad del modal de edicion de URL. @type {boolean} */
  let modalUrl           = false;

  /** Indica si la solicitud de la API para guardar URL esta en vuelo. @type {boolean} */
  let guardandoUrl       = false;

  /** Valor de URL que se esta editando en el modal de URL. @type {string} */
  let urlEditando        = '';

  /** La fila de agencia que se esta editando en el modal de URL. @type {any} */
  let agenciaUrl         = null;

  /**
   * Definiciones de opciones de estado usadas para renderizar el select de estado inline y los estilos de insignia.
   * @type {{ id: number, label: string, class: string }[]}
   */
  const estadoOpciones = [
    { id: 1, label: 'Activa',     class: 'badge--active'    },
    { id: 2, label: 'Inactiva',   class: 'badge--inactive'  },
    { id: 3, label: 'Suspendida', class: 'badge--suspended' },
  ];

  /**
   * Devuelve el objeto de opcion de estado que coincide con el ID de estado dado, o un objeto desconocido por defecto.
   * @param {number} id - El valor estadoAgenciaID de un registro de agencia.
   * @returns {{ id: number, label: string, class: string }} La opcion de estado coincidente.
   */
  const estadoInfo = (id) => estadoOpciones.find(e => e.id === id) ?? { label: 'Desconocido', class: '' };

  /**
   * Al montar: carga las agencias y los usuarios webservice disponibles en paralelo.
   */
  onMount(() => { cargarTodo(); });

  /**
   * Obtiene la lista completa de agencias y la lista de usuarios webservice sin asignar en paralelo.
   * Actualiza agencias y usuariosDisponibles al tener exito. Muestra toasts en caso de errores.
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
   * Actualiza solo la lista de usuarios webservice disponibles sin recargar la lista completa de agencias.
   * Se usa despues de operaciones que pueden cambiar la disponibilidad de usuarios.
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
   * Reinicia los campos del formulario de creacion de agencia y los errores, luego abre el modal de creacion.
   */
  function abrirModalCrear() {
    crearNombre = ''; crearCorreo = ''; crearUrl = ''; crearUsuarioId = ''; crearDescuento = 0;
    crearErrores = {};
    modalCrear = true;
  }

  /**
   * Valida todos los campos del formulario de creacion de agencia. Puebla crearErrores con cualquier
   * mensaje a nivel de campo y devuelve false si la validacion falla.
   * @returns {boolean} Verdadero cuando todos los campos son validos.
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
   * Valida el formulario de creacion y, si es valido, envia la nueva agencia al backend con POST.
   * Si tiene exito cierra el modal y recarga todos los datos. Si falla muestra un toast de error.
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
   * Establece la agencia seleccionada y reinicia el selector de usuario, luego abre el modal de asignacion de usuario.
   * @param {any} agencia - El objeto fila de agencia de la tabla.
   */
  function abrirModalAsignar(agencia) {
    agenciaSeleccionada = agencia;
    asignarUsuarioId    = '';
    modalAsignar        = true;
  }

  /**
   * Valida que se haya seleccionado un usuario y envia la asignacion al backend con PUT. Si tiene exito
   * cierra el modal y recarga todos los datos. Muestra toasts de validacion o error segun sea necesario.
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
   * Pre-rellena el modal de descuento con el descuento actual de la agencia y lo abre.
   * @param {any} agencia - El objeto fila de agencia de la tabla.
   */
  function abrirModalDescuento(agencia) {
    agenciaDescuento  = agencia;
    descuentoEditando = agencia.porcentajeDescuento;
    modalDescuento    = true;
  }

  /**
   * Valida el valor del descuento (0-100) y envia el descuento actualizado al backend con PUT.
   * Si tiene exito cierra el modal y recarga todos los datos. Muestra toasts de error en caso de fallos.
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
   * Pre-rellena el modal de URL con la URL actual de la agencia y lo abre.
   * @param {any} agencia - El objeto fila de agencia de la tabla.
   */
  function abrirModalUrl(agencia) {
    agenciaUrl  = agencia;
    urlEditando = agencia.urlAgencia ?? '';
    modalUrl    = true;
  }

  /**
   * Valida el valor de la URL y envia la URL actualizada al backend con PUT.
   * Si tiene exito cierra el modal y recarga todos los datos. Muestra toasts de error en caso de fallos.
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
   * Envia una solicitud PUT para cambiar el estado de una agencia. Si tiene exito actualiza el arreglo
   * local de agencias de forma optimista sin una recarga completa. Si falla recarga la lista completa para revertir.
   * @async
   * @param {any} agencia - El objeto fila de agencia cuyo estado se esta cambiando.
   * @param {string|number} nuevoEstadoId - El nuevo ID de estado a aplicar.
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