<script>
  /**
   * @file WebService.svelte
   * @description Portal de Webservice para integraciones con Miku Inn.
   * Permite al usuario registrado como webservice registrar su entidad
   * (agencia de viaje O aerolinea aliada) y gestionar su estado en la plataforma.
   * Un usuario webservice solo puede tener una entidad registrada a la vez.
   */

  import { onMount } from 'svelte';

  /** URL base de la API del backend. @type {string} */
  const API_BASE = 'http://localhost:7000';

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo = (page, data = null) => {};

  /** Nombre de usuario de la sesion activa. @type {string} */
  let username = '';

  /** ID del usuario de la sesion activa. @type {number|null} */
  let usuarioId = null;

  /** Seccion activa del sidebar. @type {string} */
  let activeSection = 'registro';

  /** Indica si los datos estan siendo cargados desde la API. @type {boolean} */
  let cargando = false;

  /** Mensaje de error si la carga de datos falla. @type {string|null} */
  let errorCarga = null;

  /** Lista de agencias del usuario cargadas desde la API. @type {any[]} */
  let agencias = [];

  /** Lista de aerolineas del usuario cargadas desde la API. @type {any[]} */
  let aerolineas = [];

  /** Controla la visibilidad del modal de registro de entidad. @type {boolean} */
  let showModalCrear = false;

  /** Indica si la peticion de creacion esta en vuelo. @type {boolean} */
  let creando = false;

  /** Mensaje de feedback dentro del modal de registro. @type {{ tipo: string, texto: string }|null} */
  let mensajeCrear = null;

  /**
   * Datos del formulario para registrar una nueva entidad.
   * El tipo determina si es agencia o aerolinea.
   * Para agencia: nombre, correo, url (URL_Agencia).
   * Para aerolinea: nombre, url (URL del sistema externo), urlParaUsuario (URL de redireccion al usuario).
   * @type {{ tipo: string, nombre: string, correo: string, url: string, urlParaUsuario: string }}
   */
  let nuevaEntidad = { tipo: 'agencia', nombre: '', correo: '', url: '', urlParaUsuario: '' };

  /** Mensaje de feedback inline en la tarjeta de la entidad registrada. @type {{ tipo: string, texto: string }|null} */
  let mensajeTabla = null;

  /**
   * Carga la sesion del usuario y sus entidades al montar el componente.
   */
  onMount(async () => {
    await cargarSesion();
    await cargarDatos();
  });

  /**
   * Obtiene los datos de la sesion activa (username e ID de usuario).
   * @async
   * @returns {Promise<void>}
   */
  async function cargarSesion() {
    try {
      const res = await fetch(`${API_BASE}/sesion`, { credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        username  = data.username ?? '';
        usuarioId = data.usuarioId ?? null;
      }
    } catch (_) {}
  }

  /**
   * Carga en paralelo las agencias y aerolineas del usuario desde el backend.
   * Cualquiera de los dos endpoints puede retornar lista vacia si el usuario
   * no tiene esa entidad registrada.
   * Si el servidor responde 401 o 403 redirige al login, ya que indica que la
   * sesion expiro o el usuario activo no tiene rol webservice.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarDatos() {
    cargando   = true;
    errorCarga = null;
    try {
      const [resAgencias, resAerolineas] = await Promise.all([
        fetch(`${API_BASE}/webservice/agencias`,   { credentials: 'include' }),
        fetch(`${API_BASE}/webservice/aerolineas`, { credentials: 'include' })
      ]);

      // Redirige al login si la sesion expiro o el rol no corresponde a webservice
      if (resAgencias.status === 401 || resAgencias.status === 403 ||
          resAerolineas.status === 401 || resAerolineas.status === 403) {
        navigateTo('login');
        return;
      }

      if (!resAgencias.ok)   throw new Error(`Error al cargar agencias: ${resAgencias.status}`);
      if (!resAerolineas.ok) throw new Error(`Error al cargar aerolineas: ${resAerolineas.status}`);
      agencias   = await resAgencias.json();
      aerolineas = await resAerolineas.json();

    } catch (e) {
      errorCarga = 'No se pudieron cargar los datos. ' + e.message;
    } finally {
      cargando = false;
    }
  }

  /*
   * Indica si el usuario ya tiene alguna entidad registrada...
   * @type {boolean}
   */
  $: tieneEntidad = agencias.length > 0 || aerolineas.length > 0;

  /**
   * Abre el modal de registro y reinicia todos los campos del formulario.
   */
  function abrirModalCrear() {
    nuevaEntidad = { tipo: 'agencia', nombre: '', correo: '', url: '', urlParaUsuario: '' };
    mensajeCrear = null;
    showModalCrear = true;
  }

  /**
   * Cierra el modal de registro.
   */
  function cerrarModalCrear() {
    showModalCrear = false;
  }

  /**
   * Valida los campos del formulario y envia la peticion de registro segun el tipo elegido.
   * Si el tipo es agencia llama a POST /webservice/agencias con nombre, correo y urlAgencia.
   * Si el tipo es aerolinea llama a POST /webservice/aerolineas con nombre, url y urlParaUsuario.
   * Actualiza la lista local correspondiente al recibir la respuesta exitosa.
   * @async
   * @returns {Promise<void>}
   */
  async function crearEntidad() {
    if (!nuevaEntidad.nombre.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El nombre es obligatorio.' };
      return;
    }
    if (nuevaEntidad.tipo === 'agencia' && !nuevaEntidad.correo.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'El correo es obligatorio para una agencia.' };
      return;
    }
    if (!nuevaEntidad.url.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'La direccion URL del sistema externo es obligatoria.' };
      return;
    }
    // Para aerolinea se requiere tambien la URL de redireccion para el usuario final
    if (nuevaEntidad.tipo === 'aerolinea' && !nuevaEntidad.urlParaUsuario.trim()) {
      mensajeCrear = { tipo: 'error', texto: 'La URL de redireccion para el usuario final es obligatoria.' };
      return;
    }

    creando      = true;
    mensajeCrear = null;

    try {
      // Determina endpoint y cuerpo segun el tipo de entidad seleccionado
      const esAgencia = nuevaEntidad.tipo === 'agencia';
      const endpoint  = esAgencia ? '/webservice/agencias' : '/webservice/aerolineas';
      const cuerpo    = esAgencia
        ? {
            nombre:     nuevaEntidad.nombre.trim(),
            correo:     nuevaEntidad.correo.trim(),
            urlAgencia: nuevaEntidad.url.trim()
          }
        : {
            nombre:         nuevaEntidad.nombre.trim(),
            url:            nuevaEntidad.url.trim(),
            urlParaUsuario: nuevaEntidad.urlParaUsuario.trim()
          };

      const res = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(cuerpo)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      // Agrega el nuevo registro a la lista local correspondiente
      if (esAgencia) {
        agencias = [...agencias, data];
      } else {
        aerolineas = [...aerolineas, data];
      }

      const tipoLabel = esAgencia ? 'Agencia' : 'Aerolinea';
      mensajeTabla = { tipo: 'ok', texto: `${tipoLabel} "${data.nombre}" registrada correctamente.` };
      cerrarModalCrear();
    } catch (e) {
      mensajeCrear = { tipo: 'error', texto: e.message };
    } finally {
      creando = false;
    }
  }

  /**
   * Alterna el estado de una agencia entre Activo (1) y Cerrado (2)
   * y actualiza la lista local al confirmar el cambio en el servidor.
   * @async
   * @param {any} ag - Objeto agencia a modificar.
   * @returns {Promise<void>}
   */
  async function toggleEstadoAgencia(ag) {
    const nuevoEstado = ag.estadoId === 1 ? 2 : 1;
    const texto       = nuevoEstado === 1 ? 'Activo' : 'Cerrado';
    mensajeTabla = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/agencias/${ag.id}/estado`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: nuevoEstado })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      agencias = agencias.map(a =>
        a.id === ag.id ? { ...a, estadoId: nuevoEstado, estado: texto } : a
      );
      mensajeTabla = { tipo: 'ok', texto: `Estado de la agencia cambiado a "${texto}" correctamente.` };
    } catch (e) {
      mensajeTabla = { tipo: 'error', texto: e.message };
    }
  }

  /**
   * Alterna el estado de una aerolinea entre Activo (1) y Cerrado (2)
   * y actualiza la lista local al confirmar el cambio en el servidor.
   * @async
   * @param {any} ae - Objeto aerolinea a modificar.
   * @returns {Promise<void>}
   */
  async function toggleEstadoAerolinea(ae) {
    const nuevoEstado = ae.estadoId === 1 ? 2 : 1;
    const texto       = nuevoEstado === 1 ? 'Activo' : 'Cerrado';
    mensajeTabla = null;
    try {
      const res = await fetch(`${API_BASE}/webservice/aerolineas/${ae.id}/estado`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estadoId: nuevoEstado })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.mensaje ?? `Error ${res.status}`);

      aerolineas = aerolineas.map(a =>
        a.id === ae.id ? { ...a, estadoId: nuevoEstado, estado: texto } : a
      );
      mensajeTabla = { tipo: 'ok', texto: `Estado de la aerolinea cambiado a "${texto}" correctamente.` };
    } catch (e) {
      mensajeTabla = { tipo: 'error', texto: e.message };
    }
  }

  /**
   * Cierra el overlay/modal con la tecla Escape si el foco esta en el.
   * @param {KeyboardEvent} e - Evento de teclado.
   * @param {Function} fn - Funcion de cierre a invocar.
   */
  function handleKeyOverlay(e, fn) {
    if (e.key === 'Escape') fn();
  }

  /**
   * Devuelve la clase CSS del badge de estado segun el estadoId de la entidad.
   * @param {number} estadoId - 1 = Activo, cualquier otro = Cerrado.
   * @returns {string}
   */
  function badgeClass(estadoId) {
    return estadoId === 1 ? 'ws__badge--activo' : 'ws__badge--cerrado';
  }

  /**
   * Sale del portal webservice y regresa al inicio sin cerrar sesion.
   * El usuario permanece autenticado para poder seguir usando la plataforma.
   * @returns {void}
   */
  function salir() {
    navigateTo('home');
  }
</script>

<div class="ws">

  <!-- Sidebar de navegacion con usuario y boton de salida -->
  <aside class="ws__sidebar">

    <!-- Logo y titulo del portal -->
    <div class="ws__sidebar-header">
      <div class="ws__sidebar-logo">
        <div class="ws__sidebar-logo-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0d1117" stroke-width="2.5">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
            <line x1="12" y1="22.08" x2="12" y2="12"/>
          </svg>
        </div>
        <div>
          <p class="ws__sidebar-logo-title">MikuInn</p>
          <p class="ws__sidebar-logo-sub">Portal Webservice</p>
        </div>
      </div>
    </div>

    <!-- Navegacion lateral con seccion activa resaltada -->
    <nav class="ws__sidebar-nav">
      <button
        class="ws__nav-btn {activeSection === 'registro' ? 'ws__nav-btn--active' : ''}"
        on:click={() => activeSection = 'registro'}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        Mi Registro
      </button>
    </nav>

    <!-- Pie del sidebar: badge de usuario y boton de salida -->
    <div class="ws__sidebar-footer">
      <div class="ws__user-badge">
        <div class="ws__user-avatar">{username.charAt(0).toUpperCase()}</div>
        <div>
          <p class="ws__user-name">@{username}</p>
          <p class="ws__user-role">Webservice</p>
        </div>
      </div>
      <button class="ws__btn-exit" on:click={salir}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Salir del panel
      </button>
    </div>
  </aside>

  <!-- Contenido principal del portal -->
  <main class="ws__main">

    {#if activeSection === 'registro'}

      <!-- Estado de carga de datos -->
      {#if cargando}
        <div class="ws__loading-full">
          <svg class="ws__spinner" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          <p>Cargando...</p>
        </div>

      <!-- Estado de error con boton de reintento -->
      {:else if errorCarga}
        <div class="ws__error-full">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#f85149" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <p>{errorCarga}</p>
          <button class="ws__btn ws__btn--ghost" on:click={cargarDatos}>Reintentar</button>
        </div>

      <!-- Pantalla de onboarding cuando el usuario no tiene ninguna entidad registrada -->
      {:else if !tieneEntidad}
        <div class="ws__onboarding">
          <div class="ws__onboarding-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            </svg>
          </div>
          <h2 class="ws__onboarding-title">Aun no tienes una entidad registrada</h2>
          <p class="ws__onboarding-sub">Registra tu agencia de viaje o aerolinea aliada para comenzar a operar en la plataforma Miku Inn.</p>
          <button class="ws__btn ws__btn--primary ws__btn--lg" on:click={abrirModalCrear}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Registrar mi entidad
          </button>
        </div>

      <!-- Vista cuando el usuario ya tiene una entidad registrada -->
      {:else}

        <div class="ws__agency-view">

          <!-- Encabezado de la seccion con titulo y boton de recarga -->
          <div class="ws__page-header" style="margin-bottom:1.5rem">
            <div>
              <h1 class="ws__page-title">
                {#if agencias.length > 0}Mi Agencia{:else}Mi Aerolinea{/if}
              </h1>
              <p class="ws__page-sub">Informacion de tu entidad registrada en la plataforma.</p>
            </div>
            <button class="ws__btn ws__btn--ghost ws__btn--sm" on:click={cargarDatos} disabled={cargando}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                class={cargando ? 'ws__spinner' : ''}>
                <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
              </svg>
              Recargar
            </button>
          </div>

          <!-- Mensaje de feedback tras acciones (crear, cambiar estado) -->
          {#if mensajeTabla}
            <div class="ws__feedback ws__feedback--{mensajeTabla.tipo}" style="margin-bottom:1.25rem">
              {#if mensajeTabla.tipo === 'ok'}
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              {:else}
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/></svg>
              {/if}
              {mensajeTabla.texto}
            </div>
          {/if}

          <!-- Tarjeta de agencia visible cuando el usuario registro una agencia -->
          {#if agencias.length > 0}
            {@const ag = agencias[0]}

            <div class="ws__agency-card">

              <!-- Banner con nombre, ID y badge de estado -->
              <div class="ws__agency-banner">
                <div class="ws__agency-banner-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#0d1117" stroke-width="2">
                    <path d="M19 21V5a2 2 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                  </svg>
                </div>
                <div class="ws__agency-banner-info">
                  <h2 class="ws__agency-name">{ag.nombre}</h2>
                  <p class="ws__agency-id">Agencia · ID #{ag.id}</p>
                </div>
                <span class="ws__badge ws__badge--lg {badgeClass(ag.estadoId)}">
                  <span class="ws__badge-dot"></span>
                  {ag.estado}
                </span>
              </div>

              <!-- Grid de datos: correo, URL, descuento y estado -->
              <div class="ws__agency-grid">

                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--teal">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                      <polyline points="22,6 12,13 2,6"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Correo</p>
                    <p class="ws__agency-stat-value">{ag.correo}</p>
                  </div>
                </div>

                <!-- Direccion URL del sistema externo de la agencia -->
                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="2" y1="12" x2="22" y2="12"/>
                      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Direccion URL</p>
                    <p class="ws__agency-stat-value" style="word-break:break-all;font-size:0.82rem">{ag.urlAgencia ?? '—'}</p>
                  </div>
                </div>

                <!-- Porcentaje de descuento asignado por el administrador -->
                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--amber">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="1" x2="12" y2="23"/>
                      <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Descuento asignado</p>
                    <p class="ws__agency-stat-value ws__agency-stat-value--accent">{ag.porcentajeDescuento?.toFixed(2)}%</p>
                    <p class="ws__agency-stat-hint">Asignado por el administrador</p>
                  </div>
                </div>

                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                      <line x1="7" y1="7" x2="7.01" y2="7"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Estado actual</p>
                    <p class="ws__agency-stat-value">{ag.estado}</p>
                  </div>
                </div>

              </div>

              <!-- Acciones: cerrar o reactivar la agencia segun su estado actual -->
              <div class="ws__agency-actions">
                {#if ag.estadoId === 1}
                  <button class="ws__btn ws__btn--warning" on:click={() => toggleEstadoAgencia(ag)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                    </svg>
                    Cerrar agencia temporalmente
                  </button>
                {:else}
                  <button class="ws__btn ws__btn--primary" on:click={() => toggleEstadoAgencia(ag)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    Reactivar agencia
                  </button>
                {/if}
              </div>

            </div>
          {/if}

          <!-- Tarjeta de aerolinea visible cuando el usuario registro una aerolinea -->
          {#if aerolineas.length > 0}
            {@const ae = aerolineas[0]}

            <div class="ws__agency-card">

              <!-- Banner con nombre, ID y badge de estado -->
              <div class="ws__agency-banner">
                <div class="ws__agency-banner-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#0d1117" stroke-width="2">
                    <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                  </svg>
                </div>
                <div class="ws__agency-banner-info">
                  <h2 class="ws__agency-name">{ae.nombre}</h2>
                  <p class="ws__agency-id">Aerolinea aliada · ID #{ae.id}</p>
                </div>
                <span class="ws__badge ws__badge--lg {badgeClass(ae.estadoId)}">
                  <span class="ws__badge-dot"></span>
                  {ae.estado}
                </span>
              </div>

              <!-- Grid de datos: URL del sistema, URL para usuario, descuento y estado -->
              <div class="ws__agency-grid">

                <!-- URL del sistema externo de la aerolinea -->
                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="2" y1="12" x2="22" y2="12"/>
                      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">URL del sistema externo</p>
                    <p class="ws__agency-stat-value" style="word-break:break-all;font-size:0.82rem">{ae.url ?? '—'}</p>
                  </div>
                </div>

                <!-- URL de redireccion para el usuario final -->
                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--teal">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M10 13a5 5 0 007.54.54l3-3a5 5 0 00-7.07-7.07l-1.72 1.71"/>
                      <path d="M14 11a5 5 0 00-7.54-.54l-3 3a5 5 0 007.07 7.07l1.71-1.71"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">URL para el usuario final</p>
                    <p class="ws__agency-stat-value" style="word-break:break-all;font-size:0.82rem">{ae.urlParaUsuario ?? '—'}</p>
                  </div>
                </div>

                <!-- Porcentaje de descuento asignado por el administrador -->
                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--amber">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="12" y1="1" x2="12" y2="23"/>
                      <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Descuento asignado</p>
                    <p class="ws__agency-stat-value ws__agency-stat-value--accent">{ae.porcentajeDescuento?.toFixed(2)}%</p>
                    <p class="ws__agency-stat-hint">Asignado por el administrador</p>
                  </div>
                </div>

                <div class="ws__agency-stat">
                  <div class="ws__agency-stat-icon ws__agency-stat-icon--blue">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/>
                      <line x1="7" y1="7" x2="7.01" y2="7"/>
                    </svg>
                  </div>
                  <div>
                    <p class="ws__agency-stat-label">Estado actual</p>
                    <p class="ws__agency-stat-value">{ae.estado}</p>
                  </div>
                </div>

              </div>

              <!-- Acciones: cerrar o reactivar la aerolinea segun su estado actual -->
              <div class="ws__agency-actions">
                {#if ae.estadoId === 1}
                  <button class="ws__btn ws__btn--warning" on:click={() => toggleEstadoAerolinea(ae)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                    </svg>
                    Cerrar aerolinea temporalmente
                  </button>
                {:else}
                  <button class="ws__btn ws__btn--primary" on:click={() => toggleEstadoAerolinea(ae)}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    Reactivar aerolinea
                  </button>
                {/if}
              </div>

            </div>
          {/if}

        </div>

      {/if}

    {/if}

  </main>
</div>

<!-- Modal para registrar una nueva entidad (agencia o aerolinea) -->
{#if showModalCrear}
  <div
    class="ws__overlay"
    on:click={cerrarModalCrear}
    on:keydown={e => handleKeyOverlay(e, cerrarModalCrear)}
    role="button" tabindex="-1" aria-label="Cerrar"
  ></div>

  <div class="ws__modal" role="dialog" aria-modal="true" aria-labelledby="modal-crear-titulo">

    <div class="ws__modal-header">
      <p class="ws__modal-title" id="modal-crear-titulo">Registrar entidad</p>
      <button class="ws__modal-close" on:click={cerrarModalCrear} aria-label="Cerrar">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <div class="ws__modal-body">

      <!-- Selector de tipo de entidad: agencia de viaje o aerolinea aliada -->
      <div class="ws__form-group">
        <label class="ws__form-label">Tipo de entidad *</label>
        <div class="ws__tipo-selector">
          <button
            type="button"
            class="ws__tipo-btn {nuevaEntidad.tipo === 'agencia' ? 'ws__tipo-btn--active' : ''}"
            on:click={() => nuevaEntidad = { ...nuevaEntidad, tipo: 'agencia' }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 21V5a2 2 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
            </svg>
            Agencia de viaje
          </button>
          <button
            type="button"
            class="ws__tipo-btn {nuevaEntidad.tipo === 'aerolinea' ? 'ws__tipo-btn--active' : ''}"
            on:click={() => nuevaEntidad = { ...nuevaEntidad, tipo: 'aerolinea' }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
            </svg>
            Aerolinea aliada
          </button>
        </div>
      </div>

      <!-- Campo de nombre de la entidad -->
      <div class="ws__form-group">
        <label class="ws__form-label" for="ent-nombre">
          {nuevaEntidad.tipo === 'agencia' ? 'Nombre de la agencia' : 'Nombre de la aerolinea'} *
        </label>
        <input
          id="ent-nombre"
          class="ws__form-input"
          type="text"
          placeholder={nuevaEntidad.tipo === 'agencia' ? 'Ej: Agencia Turistica Central' : 'Ej: Aerolinea Centroamericana'}
          bind:value={nuevaEntidad.nombre}
        />
      </div>

      <!-- Campo de correo electronico visible solo para el tipo agencia -->
      {#if nuevaEntidad.tipo === 'agencia'}
        <div class="ws__form-group">
          <label class="ws__form-label" for="ent-correo">Correo electronico *</label>
          <input
            id="ent-correo"
            class="ws__form-input"
            type="email"
            placeholder="agencia@ejemplo.com"
            bind:value={nuevaEntidad.correo}
          />
        </div>
      {/if}

      <!-- Campo de URL del sistema externo, requerido para ambos tipos -->
      <div class="ws__form-group">
        <label class="ws__form-label" for="ent-url">
          {nuevaEntidad.tipo === 'agencia' ? 'Direccion URL del sistema' : 'URL del sistema externo'} *
        </label>
        <input
          id="ent-url"
          class="ws__form-input"
          type="text"
          placeholder="http://mi-sistema.com"
          bind:value={nuevaEntidad.url}
        />
        <p style="margin:0.35rem 0 0;font-size:0.76rem;color:var(--ws-muted)">
          {nuevaEntidad.tipo === 'agencia'
            ? 'Direccion del sistema externo que se conectara con Miku Inn.'
            : 'Endpoint de tu sistema al que Miku Inn enviara las solicitudes.'}
        </p>
      </div>

      <!-- Campo de URL para el usuario final, visible solo para el tipo aerolinea -->
      {#if nuevaEntidad.tipo === 'aerolinea'}
        <div class="ws__form-group">
          <label class="ws__form-label" for="ent-url-usuario">URL de redireccion para el usuario final *</label>
          <input
            id="ent-url-usuario"
            class="ws__form-input"
            type="text"
            placeholder="http://mi-sistema.com/reservar"
            bind:value={nuevaEntidad.urlParaUsuario}
          />
          <p style="margin:0.35rem 0 0;font-size:0.76rem;color:var(--ws-muted)">
            URL a la que se redirigira al usuario cuando realice una reservacion desde tu aerolinea.
          </p>
        </div>
      {/if}

      <!-- Aviso: el descuento inicia en 0% y los tokens se generan automaticamente -->
      <div class="ws__form-group" style="background:rgba(45,212,191,0.06);border:1px solid rgba(45,212,191,0.15);border-radius:8px;padding:0.6rem 0.875rem">
        <p style="margin:0;font-size:0.78rem;color:var(--ws-muted)">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2dd4bf" stroke-width="2" style="vertical-align:middle;margin-right:4px"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="8"/><line x1="12" y1="12" x2="12" y2="16"/></svg>
          El porcentaje de descuento inicia en <strong style="color:#2dd4bf">0%</strong> y solo el administrador puede modificarlo.
          Los tokens de autenticacion se generan automaticamente al establecer la conexion.
        </p>
      </div>

      <!-- Feedback de validacion o error del formulario del modal -->
      {#if mensajeCrear}
        <div class="ws__feedback ws__feedback--{mensajeCrear.tipo}">
          {#if mensajeCrear.tipo === 'ok'}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          {:else}
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {/if}
          {mensajeCrear.texto}
        </div>
      {/if}

    </div>

    <!-- Botones de accion del modal -->
    <div class="ws__modal-footer">
      <button class="ws__btn ws__btn--ghost" on:click={cerrarModalCrear} disabled={creando}>Cancelar</button>
      <button class="ws__btn ws__btn--primary" on:click={crearEntidad} disabled={creando}>
        {#if creando}
          <svg class="ws__spinner" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          Guardando...
        {:else}
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          Registrar
        {/if}
      </button>
    </div>

  </div>
{/if}