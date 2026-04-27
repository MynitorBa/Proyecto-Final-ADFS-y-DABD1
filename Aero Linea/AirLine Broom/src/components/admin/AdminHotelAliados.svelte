<script>
/**
 * @file AdminHotelAliados.svelte
 * @description Seccion del panel de administracion para gestionar hoteles aliados. Muestra una
 * barra de estadisticas resumidas (total, activos, inactivos, sin usuario asignado) y una tabla
 * de todos los hoteles aliados. Proporciona tres dialogos modales: uno para crear un nuevo hotel
 * (con nombre, URL de API, URL publica, URL home aliado y usuario webservice), uno para asignar
 * un usuario webservice disponible a un hotel existente, y uno para editar las tres URLs de un
 * hotel. El estado del hotel puede cambiarse inline mediante un elemento select en la fila de la
 * tabla. Un boton de handshake inicia el flujo de autenticacion entre la aerolinea y el hotel
 * aliado, generando y almacenando un token de sesion en HotelAliado.TokenHASH.
 * Todas las mutaciones llaman a la API del backend y actualizan el estado local al tener exito.
 */
// @ts-nocheck
  import { onMount } from 'svelte';

  /** URL base de la API usada para todas las solicitudes al backend. @type {string} */
  export let API;

  /** Funcion para mostrar una notificacion toast. Firma: (type: string, message: string) => void. @type {Function} */
  export let mostrarToast;

  /** Funcion para mostrar un dialogo de confirmacion. Firma: (msg, sub, type) => Promise<boolean>. @type {Function} */
  export let mostrarConfirm;

  /** Lista de hoteles aliados cargados desde el backend. @type {any[]} */
  let hoteles            = [];

  /** Usuarios con rol Webservice que aun no tienen entidad asignada, usados para poblar los selectores de usuario. @type {any[]} */
  let usuariosDisponibles = [];

  /** Indica si la carga principal de datos esta en progreso. @type {boolean} */
  let cargando           = false;

  // -- Estado del modal de creacion --

  /** Controla la visibilidad del modal de creacion de hotel. @type {boolean} */
  let modalCrear         = false;

  /** Indica si la solicitud de la API para crear el hotel esta en curso. @type {boolean} */
  let creando            = false;

  /** Valor del campo nombre del hotel en el formulario de creacion. @type {string} */
  let crearNombre        = '';

  /** Valor del campo URL de API en el formulario de creacion. @type {string} */
  let crearUrl           = '';

  /** Valor del campo URL publica en el formulario de creacion. @type {string} */
  let crearUrlParaUsuario = '';

  /** Valor del campo URL home aliado en el formulario de creacion (opcional). @type {string} */
  let crearUrlHomeAliado = '';

  /** ID del usuario webservice seleccionado en el formulario de creacion. @type {string} */
  let crearUsuarioId     = '';

  /** Errores de validacion por campo para el formulario de creacion. @type {Record<string, string>} */
  let crearErrores       = {};

  // -- Estado del modal de asignacion de usuario --

  /** Controla la visibilidad del modal de asignacion de usuario. @type {boolean} */
  let modalAsignar       = false;

  /** Indica si la solicitud de la API para asignar usuario esta en curso. @type {boolean} */
  let asignando          = false;

  /** La fila del hotel actualmente seleccionado para asignacion de usuario. @type {any} */
  let hotelSeleccionado  = null;

  /** ID del usuario webservice seleccionado en el modal de asignacion. @type {string} */
  let asignarUsuarioId   = '';

  // -- Estado del modal de edicion de URLs --

  /** Controla la visibilidad del modal de edicion de URLs. @type {boolean} */
  let modalUrls          = false;

  /** Indica si la solicitud de la API para guardar URLs esta en curso. @type {boolean} */
  let guardandoUrls      = false;

  /** Valor de la URL de API que se esta editando en el modal de URLs. @type {string} */
  let urlEditando        = '';

  /** Valor de la URL publica que se esta editando en el modal de URLs. @type {string} */
  let urlParaUsuarioEditando = '';

  /** Valor de la URL home aliado que se esta editando en el modal de URLs (opcional). @type {string} */
  let urlHomeAliadoEditando = '';

  /** La fila del hotel que se esta editando en el modal de URLs. @type {any} */
  let hotelUrls          = null;

  // -- Estado del handshake --

  /**
   * ID del hotel cuyo handshake esta en curso. Null cuando ninguno esta activo.
   * Se usa para deshabilitar el boton del hotel especifico durante la operacion.
   * @type {number|null}
   */
  let handshakeEnCurso   = null;

  /**
   * Definiciones de opciones de estado usadas para renderizar el select de estado inline y los estilos de insignia.
   * Refleja el catalogo EstadoAliado en la base de datos.
   * @type {{ id: number, label: string }[]}
   */
  const estadoOpciones = [
    { id: 1, label: 'Activo'     },
    { id: 2, label: 'Inactivo'   },
    { id: 3, label: 'Suspendido' },
  ];

  /**
   * Al montar: carga los hoteles aliados y los usuarios webservice disponibles en paralelo.
   */
  onMount(() => { cargarTodo(); });

  /**
   * Obtiene la lista completa de hoteles aliados y la lista de usuarios webservice sin asignar en paralelo.
   * Actualiza hoteles y usuariosDisponibles al tener exito. Muestra toasts en caso de errores.
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
   * Reinicia los campos del formulario de creacion de hotel y los errores, luego abre el modal de creacion.
   */
  function abrirModalCrear() {
    crearNombre        = '';
    crearUrl           = '';
    crearUrlParaUsuario= '';
    crearUrlHomeAliado = '';
    crearUsuarioId     = '';
    crearErrores       = {};
    modalCrear         = true;
  }

  /**
   * Valida todos los campos obligatorios del formulario de creacion de hotel.
   * La URLHomeAliado es opcional: si se proporciona se valida el formato, si no se omite.
   * Rellena crearErrores con los mensajes por campo y retorna false si la validacion falla.
   * @returns {boolean} True cuando todos los campos requeridos son validos.
   */
  function validarCrear() {
    crearErrores = {};
    if (!crearNombre.trim())
      crearErrores.nombre = 'Requerido.';
    if (!crearUrl.trim())
      crearErrores.url = 'Requerido.';
    else if (!/^https?:\/\/.+/.test(crearUrl.trim()))
      crearErrores.url = 'URL invalida (http:// o https://).';
    if (!crearUrlParaUsuario.trim())
      crearErrores.urlParaUsuario = 'Requerido.';
    else if (!/^https?:\/\/.+/.test(crearUrlParaUsuario.trim()))
      crearErrores.urlParaUsuario = 'URL invalida (http:// o https://).';
    if (crearUrlHomeAliado.trim() && !/^https?:\/\/.+/.test(crearUrlHomeAliado.trim()))
      crearErrores.urlHomeAliado = 'URL invalida (http:// o https://).';
    if (!crearUsuarioId)
      crearErrores.usuario = 'Debes seleccionar un usuario Webservice.';
    return Object.keys(crearErrores).length === 0;
  }

  /**
   * Valida el formulario de creacion y, si es valido, envia con POST el nuevo hotel al backend.
   * Incluye urlHomeAliado si fue proporcionada (campo opcional).
   * Al tener exito cierra el modal y recarga todos los datos. En caso de fallo muestra un toast de error.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearHotel() {
    if (!validarCrear()) return;
    creando = true;
    try {
      const body = {
        nombre:         crearNombre.trim(),
        url:            crearUrl.trim(),
        urlParaUsuario: crearUrlParaUsuario.trim(),
        usuarioWEBIs:   parseInt(crearUsuarioId),
      };
      if (crearUrlHomeAliado.trim())
        body.urlHomeAliado = crearUrlHomeAliado.trim();

      const r = await fetch(`${API}/api/hoteles-aliados`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
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
   * Establece el hotel seleccionado y reinicia el selector de usuario, luego abre el modal de asignacion.
   * @param {any} hotel - El objeto fila del hotel de la tabla.
   */
  function abrirModalAsignar(hotel) {
    hotelSeleccionado = hotel;
    asignarUsuarioId  = '';
    modalAsignar      = true;
  }

  /**
   * Valida que se haya seleccionado un usuario y envia con PUT la asignacion al backend. Al tener
   * exito cierra el modal y recarga todos los datos. Muestra toasts de validacion o error segun sea necesario.
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
   * Pre-rellena el modal de URLs con las URLs actuales del hotel (incluyendo URLHomeAliado) y lo abre.
   * @param {any} hotel - El objeto fila del hotel de la tabla.
   */
  function abrirModalUrls(hotel) {
    hotelUrls                 = hotel;
    urlEditando               = hotel.url             ?? '';
    urlParaUsuarioEditando    = hotel.urlParaUsuario  ?? '';
    urlHomeAliadoEditando     = hotel.urlHomeAliado   ?? '';
    modalUrls                 = true;
  }

  /**
   * Valida la URL de API y la URL publica (obligatorias). La URLHomeAliado es opcional:
   * si se proporciona se valida el formato. Envia con PUT las URLs actualizadas al backend.
   * Al tener exito cierra el modal y recarga todos los datos. Muestra toasts de error en caso de fallos.
   * @async
   * @returns {Promise<void>}
   */
  async function handleGuardarUrls() {
    if (!urlEditando.trim()) {
      mostrarToast('error', 'La URL de la API no puede estar vacia.'); return;
    }
    if (!/^https?:\/\/.+/.test(urlEditando.trim())) {
      mostrarToast('error', 'URL de API invalida.'); return;
    }
    if (!urlParaUsuarioEditando.trim()) {
      mostrarToast('error', 'La URL publica no puede estar vacia.'); return;
    }
    if (!/^https?:\/\/.+/.test(urlParaUsuarioEditando.trim())) {
      mostrarToast('error', 'URL publica invalida.'); return;
    }
    if (urlHomeAliadoEditando.trim() && !/^https?:\/\/.+/.test(urlHomeAliadoEditando.trim())) {
      mostrarToast('error', 'URL home aliado invalida.'); return;
    }

    guardandoUrls = true;
    try {
      const body = {
        url:            urlEditando.trim(),
        urlParaUsuario: urlParaUsuarioEditando.trim(),
        urlHomeAliado:  urlHomeAliadoEditando.trim() || null,
      };

      const r = await fetch(`${API}/api/hoteles-aliados/${hotelUrls.id}/url`, {
        method: 'PUT', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
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
   * Envia una solicitud PUT para cambiar el estado de un hotel. Al tener exito actualiza el arreglo
   * local hoteles de forma optimista sin recarga completa. En caso de fallo recarga la lista completa para revertir.
   * @async
   * @param {any} hotel - El objeto fila del hotel cuyo estado se esta cambiando.
   * @param {string|number} nuevoEstadoId - El nuevo ID de estado a aplicar.
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
   * Inicia el flujo de autenticacion handshake entre la aerolinea y el hotel aliado seleccionado.
   * @async
   * @param {any} hotel - El objeto fila del hotel de la tabla.
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

<!-- ═══════════════════════════════════════════════════════════════════════
     MODAL: Crear nuevo hotel aliado
     ═══════════════════════════════════════════════════════════════════════ -->
{#if modalCrear}
  <div class="ag-overlay" on:click={() => modalCrear = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Nuevo Hotel Aliado</h3>
        <button class="ag-modal__close" on:click={() => modalCrear = false}>×</button>
      </div>

      <div class="ag-modal__body">

        <!-- Nombre -->
        <div class="ag-field">
          <label class="ag-field__label">Nombre del hotel <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.nombre}
            type="text" bind:value={crearNombre} placeholder="Hotel Las Palmas" maxlength="120" />
          {#if crearErrores.nombre}<p class="ag-field__err">{crearErrores.nombre}</p>{/if}
        </div>

        <!-- URL base de la API del hotel -->
        <div class="ag-field">
          <label class="ag-field__label">URL de la API del hotel <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.url}
            type="url" bind:value={crearUrl} placeholder="https://api.mi-hotel.com" maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL interna usada por la aerolinea para consultar disponibilidad y hacer handshake.
          </p>
          {#if crearErrores.url}<p class="ag-field__err">{crearErrores.url}</p>{/if}
        </div>

        <!-- URL publica para usuarios -->
        <div class="ag-field">
          <label class="ag-field__label">URL publica para usuarios <span class="ag-required">*</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.urlParaUsuario}
            type="url" bind:value={crearUrlParaUsuario} placeholder="https://www.mi-hotel.com" maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL visible para los pasajeros en los resultados de busqueda.
          </p>
          {#if crearErrores.urlParaUsuario}<p class="ag-field__err">{crearErrores.urlParaUsuario}</p>{/if}
        </div>

        <!-- URL Home Aliado (opcional) -->
        <div class="ag-field">
          <label class="ag-field__label">URL Home del aliado <span style="font-size:.72rem; color:var(--text-muted); font-weight:400;">(opcional)</span></label>
          <input class="ag-field__input" class:ag-field__input--err={crearErrores.urlHomeAliado}
            type="url" bind:value={crearUrlHomeAliado} placeholder="https://www.mi-hotel.com/home" maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL de la pagina de inicio del hotel aliado que se mostrara como recomendacion a los usuarios.
          </p>
          {#if crearErrores.urlHomeAliado}<p class="ag-field__err">{crearErrores.urlHomeAliado}</p>{/if}
        </div>

        <!-- Usuario Webservice -->
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

<!-- ═══════════════════════════════════════════════════════════════════════
     MODAL: Asignar usuario webservice a un hotel existente
     ═══════════════════════════════════════════════════════════════════════ -->
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

<!-- ═══════════════════════════════════════════════════════════════════════
     MODAL: Editar URLs del hotel (API, publica y home aliado)
     ═══════════════════════════════════════════════════════════════════════ -->
{#if modalUrls}
  <div class="ag-overlay" on:click={() => modalUrls = false} role="dialog" aria-modal="true">
    <div class="ag-modal" on:click|stopPropagation>
      <div class="ag-modal__header">
        <h3 class="ag-modal__title">Editar URLs</h3>
        <button class="ag-modal__close" on:click={() => modalUrls = false}>×</button>
      </div>
      <div class="ag-modal__body">
        <p class="ag-modal__desc">Hotel: <strong>{hotelUrls?.nombre}</strong></p>

        <!-- URL base de la API del hotel -->
        <div class="ag-field">
          <label class="ag-field__label">URL de la API del hotel <span class="ag-required">*</span></label>
          <input class="ag-field__input" type="url" placeholder="https://api.mi-hotel.com"
            bind:value={urlEditando} maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL interna usada por la aerolinea para consultar disponibilidad y hacer handshake.
          </p>
        </div>

        <!-- URL publica visible a los pasajeros -->
        <div class="ag-field">
          <label class="ag-field__label">URL publica para usuarios <span class="ag-required">*</span></label>
          <input class="ag-field__input" type="url" placeholder="https://www.mi-hotel.com"
            bind:value={urlParaUsuarioEditando} maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            URL visible para los pasajeros en los resultados de busqueda.
          </p>
        </div>

        <!-- URL Home Aliado (opcional) -->
        <div class="ag-field">
          <label class="ag-field__label">
            URL Home del aliado
            <span style="font-size:.72rem; color:var(--text-muted); font-weight:400;">(opcional)</span>
          </label>
          <input class="ag-field__input" type="url" placeholder="https://www.mi-hotel.com/home"
            bind:value={urlHomeAliadoEditando} maxlength="300" />
          <p style="font-size:.72rem; color:var(--text-muted); margin:.1rem 0 0;">
            Pagina de inicio del hotel aliado mostrada como recomendacion a usuarios. Dejar vacia para quitar.
          </p>
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

<!-- ═══════════════════════════════════════════════════════════════════════
     SECCION PRINCIPAL: tabla de hoteles aliados
     ═══════════════════════════════════════════════════════════════════════ -->
<section class="admin-section">

  <div class="section-header">
    <div>
      <h2 class="admin-section__title">Hoteles Aliados</h2>
      <p class="admin-section__subtitle">Crea y gestiona los hoteles aliados Webservice</p>
    </div>
    <div style="display:flex; gap:.75rem;">
      <button class="btn-add" on:click={cargarTodo} style="background:#4b5563">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round"
          style="display:inline-block;vertical-align:middle;margin-right:4px">
          <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
        </svg>Actualizar
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

  {:else}
    <div class="vuelos-table">
      <table class="table">
        <thead class="table__head">
          <tr>
            <th class="table__header">ID</th>
            <th class="table__header">Nombre</th>
            <th class="table__header">URL API</th>
            <th class="table__header">URL Publica</th>
            <th class="table__header">URL Home</th>
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

              <!-- URL base de la API del hotel -->
              <td class="table__cell" data-label="URL API">
                {#if h.url}
                  <span style="font-size:.78rem; color:var(--text-muted); word-break:break-all; max-width:130px; display:block;">
                    {h.url}
                  </span>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL</span>
                {/if}
              </td>

              <!-- URL publica del hotel que se muestra a los pasajeros -->
              <td class="table__cell" data-label="URL Publica">
                {#if h.urlParaUsuario}
                  <span style="font-size:.78rem; color:var(--text-muted); word-break:break-all; max-width:130px; display:block;">
                    {h.urlParaUsuario}
                  </span>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL</span>
                {/if}
              </td>

              <!-- URL Home Aliado: mostrada con icono de enlace externo si existe -->
              <td class="table__cell" data-label="URL Home">
                {#if h.urlHomeAliado}
                  <a href={h.urlHomeAliado} target="_blank" rel="noopener noreferrer"
                    style="font-size:.78rem; color:#6366f1; word-break:break-all; max-width:130px; display:flex; align-items:center; gap:3px;">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                      stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
                      <polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>
                    </svg>
                    <span style="overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                      {h.urlHomeAliado.replace(/^https?:\/\//, '')}
                    </span>
                  </a>
                {:else}
                  <span style="font-size:.75rem; color:#9ca3af; font-style:italic">Sin URL home</span>
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
                  <!-- Editar las tres URLs del hotel aliado -->
                  <button class="table__action-btn ag-btn-asignar"
                    style="background:#6366f1"
                    on:click={() => abrirModalUrls(h)}
                    title="Editar URLs">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                      stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                      style="display:inline-block;vertical-align:middle;margin-right:4px">
                      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                    </svg>URLs
                  </button>

                  <!-- Asignar o reasignar usuario Webservice al hotel -->
                  <button class="table__action-btn ag-btn-asignar"
                    on:click={() => abrirModalAsignar(h)}
                    title="Asignar usuario Webservice">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                      stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                      style="display:inline-block;vertical-align:middle;margin-right:4px">
                      <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>Asignar
                  </button>

                  <!-- Handshake de autenticacion con el hotel aliado -->
                  <button class="table__action-btn ag-btn-asignar"
                    style="background:#059669"
                    on:click={() => handleHandshake(h)}
                    disabled={handshakeEnCurso === h.id}
                    title="Iniciar handshake de autenticacion con el hotel">
                    {#if handshakeEnCurso === h.id}
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                        style="display:inline-block;vertical-align:middle;margin-right:4px">
                        <path d="M5 22h14"/><path d="M5 2h14"/>
                        <path d="M17 22v-4.172a2 2 0 0 0-.586-1.414L12 12l-4.414 4.414A2 2 0 0 0 7 17.828V22"/>
                        <path d="M7 2v4.172a2 2 0 0 0 .586 1.414L12 12l4.414-4.414A2 2 0 0 0 17 6.172V2"/>
                      </svg>Conectando...
                    {:else}
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                        stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                        style="display:inline-block;vertical-align:middle;margin-right:4px">
                        <path d="m11 17 2 2a1 1 0 1 0 3-3"/>
                        <path d="m14 14 2.5 2.5a1 1 0 1 0 3-3l-3.88-3.88a3 3 0 0 0-4.24 0l-.88.88a1 1 0 1 1-3-3l2.81-2.81a5.79 5.79 0 0 1 7.06-.87l.47.28a2 2 0 0 0 1.42.25L21 4"/>
                        <path d="m21 3 1 11h-2"/>
                        <path d="M3 3 2 14l6.5 6.5a1 1 0 1 0 3-3"/>
                        <path d="M3 4h8"/>
                      </svg>Handshake
                    {/if}
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