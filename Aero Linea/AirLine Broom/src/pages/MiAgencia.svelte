<script>
/**
 * @file MiAgencia.svelte
 * @description Panel de usuario Webservice para registrar y visualizar su entidad vinculada.
 * Al cargar verifica si el usuario ya tiene una agencia (GET /api/agencias/mi-agencia)
 * o un hotel aliado (GET /api/hoteles-aliados/mi-hotel). Un usuario Webservice solo puede tener
 * una de las dos. Si no existe ninguna, se muestra un selector de tipo para que el usuario elija
 * que desea registrar. El formulario de agencia recopila nombre, correo y URL; el formulario de
 * hotel recopila nombre, URL de la API y URL para el usuario. Los tokens no se muestran ni
 * se solicitan, ya que se generan automaticamente durante el proceso de conexion.
 * Redirige a 'acceso-denegado' en caso de 401/403.
 */
// @ts-nocheck
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';
  import '../styles/admin.css';
  import '../styles/miagencia.css';

  /** Funcion utilizada para navegar entre las paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Verdadero mientras la carga inicial de datos esta en progreso. @type {boolean} */
  let cargando     = true;

  /** Verdadero mientras el formulario de creacion esta siendo enviado. @type {boolean} */
  let enviando     = false;

  /** Verdadero cuando la API confirma que el usuario ya tiene una agencia registrada. @type {boolean} */
  let tieneAgencia = false;

  /** Verdadero cuando la API confirma que el usuario ya tiene un hotel aliado registrado. @type {boolean} */
  let tieneHotel   = false;

  /** El objeto de agencia devuelto por la API si existe, de lo contrario null. @type {object|null} */
  let agencia      = null;

  /** El objeto de hotel devuelto por la API si existe, de lo contrario null. @type {object|null} */
  let hotel        = null;

  /** Mensaje de error global que se muestra cuando la llamada a la API falla por completo. @type {string} */
  let errorGlobal  = '';

  /** Objeto de notificacion toast actual con tipo y mensaje, o null cuando esta oculto. @type {{tipo: string, mensaje: string}|null} */
  let toast        = null;

  /**
   * Controla que tipo de entidad esta registrando el usuario: null muestra el selector de tipo,
   * 'agencia' muestra el formulario de agencia, 'hotel' muestra el formulario de hotel.
   * @type {'agencia'|'hotel'|null}
   */
  let tipoSeleccionado = null;

  // -- Campos del formulario de agencia ------------------------------------

  /** Campo de nombre de la agencia vinculado al input del formulario de creacion. @type {string} */
  let nombre     = '';

  /** Campo de correo de contacto de la agencia vinculado al input del formulario de creacion. @type {string} */
  let correo     = '';

  /** URL publica de la agencia registrada por el usuario Webservice. @type {string} */
  let urlAgencia = '';

  /** Mensajes de error de validacion por campo para el formulario de agencia. @type {{nombre: string, correo: string, urlAgencia: string}} */
  let erroresAgencia = { nombre: '', correo: '', urlAgencia: '' };

  // -- Campos del formulario de hotel --------------------------------------

  /** Campo de nombre del hotel vinculado al formulario de creacion de hotel. @type {string} */
  let nombreHotel    = '';

  /** URL base de la API del hotel para que la aerolinea se comunique con el. @type {string} */
  let urlHotel       = '';

  /** URL publica del hotel que se muestra a los pasajeros. @type {string} */
  let urlParaUsuario = '';

  /** Mensajes de error de validacion por campo para el formulario de hotel. @type {{nombre: string, url: string, urlParaUsuario: string}} */
  let erroresHotel = { nombre: '', url: '', urlParaUsuario: '' };

  onMount(async () => {
    await cargarMiEntidad();
  });

  /**
   * Carga en paralelo los datos de agencia y hotel para el usuario Webservice autenticado.
   * Establece tieneAgencia/agencia y tieneHotel/hotel a partir de las respuestas de la API.
   * Redirige a 'acceso-denegado' en caso de 401 o 403. Asigna errorGlobal en otros fallos.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarMiEntidad() {
    cargando    = true;
    errorGlobal = '';
    try {
      const [rAgencia, rHotel] = await Promise.all([
        fetch(`${API}/api/agencias/mi-agencia`,        { credentials: 'include' }),
        fetch(`${API}/api/hoteles-aliados/mi-hotel`,   { credentials: 'include' })
      ]);

      // Redirigir si cualquiera de las dos respuestas indica acceso denegado
      if (rAgencia.status === 401 || rAgencia.status === 403 ||
          rHotel.status  === 401 || rHotel.status  === 403) {
        navigateTo('acceso-denegado');
        return;
      }

      if (!rAgencia.ok || !rHotel.ok) {
        errorGlobal = 'Error al consultar tu informacion. Intenta de nuevo.';
        return;
      }

      const dataAgencia = await rAgencia.json();
      const dataHotel   = await rHotel.json();

      tieneAgencia = dataAgencia.tieneAgencia;
      agencia      = dataAgencia.agencia;
      tieneHotel   = dataHotel.tieneHotel;
      hotel        = dataHotel.hotel;

    } catch {
      errorGlobal = 'Error de conexion. Intenta de nuevo.';
    } finally {
      cargando = false;
    }
  }

  // -- Formulario de agencia -----------------------------------------------

  /**
   * Valida el formulario de creacion de agencia. Verifica que nombre no este vacio, que correo
   * coincida con un patron de correo basico y que urlAgencia sea una URL valida no vacia.
   * Rellena erroresAgencia con mensajes para cada campo que falle.
   * @returns {boolean} Verdadero si todos los campos de la agencia son validos.
   */
  function validarAgencia() {
    erroresAgencia = { nombre: '', correo: '', urlAgencia: '' };
    let ok = true;
    if (!nombre.trim())     { erroresAgencia.nombre    = 'El nombre es obligatorio.';  ok = false; }
    if (!correo.trim())     { erroresAgencia.correo    = 'El correo es obligatorio.';  ok = false; }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
                              erroresAgencia.correo    = 'Ingresa un correo valido.';   ok = false; }
    if (!urlAgencia.trim()) { erroresAgencia.urlAgencia = 'La URL de la agencia es obligatoria.'; ok = false; }
    else if (!/^https?:\/\/.+/.test(urlAgencia.trim())) {
                              erroresAgencia.urlAgencia = 'Ingresa una URL valida (debe iniciar con http:// o https://).'; ok = false; }
    return ok;
  }

  /**
   * Envia el formulario de creacion de agencia a POST /api/agencias/mi-agencia.
   * En caso de exito muestra un toast de exito, recarga los datos de la entidad y limpia el formulario.
   * En caso de fallo muestra un toast de error con el mensaje del servidor.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearAgencia() {
    if (!validarAgencia()) return;
    enviando = true;
    try {
      const r = await fetch(`${API}/api/agencias/mi-agencia`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:     nombre.trim(),
          correo:     correo.trim(),
          urlAgencia: urlAgencia.trim()
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Agencia registrada exitosamente!');
        await cargarMiEntidad();
        nombre = ''; correo = ''; urlAgencia = '';
      } else {
        mostrarToast('error', data.message || 'Error al crear la agencia.');
      }
    } catch {
      mostrarToast('error', 'Error de conexion. Intenta de nuevo.');
    } finally {
      enviando = false;
    }
  }

  // -- Formulario de hotel -------------------------------------------------

  /**
   * Valida el formulario de creacion de hotel. Verifica que nombreHotel no este vacio y que ambas
   * URLs sean direcciones HTTP/HTTPS validas y no vacias.
   * Rellena erroresHotel con mensajes para cada campo que falle.
   * @returns {boolean} Verdadero si todos los campos del hotel son validos.
   */
  function validarHotel() {
    erroresHotel = { nombre: '', url: '', urlParaUsuario: '' };
    let ok = true;
    if (!nombreHotel.trim())    { erroresHotel.nombre        = 'El nombre es obligatorio.'; ok = false; }
    if (!urlHotel.trim())       { erroresHotel.url           = 'La URL de la API es obligatoria.'; ok = false; }
    else if (!/^https?:\/\/.+/.test(urlHotel.trim())) {
                                  erroresHotel.url           = 'Ingresa una URL valida (http:// o https://).'; ok = false; }
    if (!urlParaUsuario.trim()) { erroresHotel.urlParaUsuario = 'La URL publica es obligatoria.'; ok = false; }
    else if (!/^https?:\/\/.+/.test(urlParaUsuario.trim())) {
                                  erroresHotel.urlParaUsuario = 'Ingresa una URL valida (http:// o https://).'; ok = false; }
    return ok;
  }

  /**
   * Envia el formulario de creacion de hotel a POST /api/hoteles-aliados/mi-hotel.
   * En caso de exito muestra un toast de exito, recarga los datos de la entidad y limpia el formulario.
   * En caso de fallo muestra un toast de error con el mensaje del servidor.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrearHotel() {
    if (!validarHotel()) return;
    enviando = true;
    try {
      const r = await fetch(`${API}/api/hoteles-aliados/mi-hotel`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:        nombreHotel.trim(),
          url:           urlHotel.trim(),
          urlParaUsuario: urlParaUsuario.trim()
        })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Hotel aliado registrado exitosamente!');
        await cargarMiEntidad();
        nombreHotel = ''; urlHotel = ''; urlParaUsuario = '';
      } else {
        mostrarToast('error', data.message || 'Error al registrar el hotel.');
      }
    } catch {
      mostrarToast('error', 'Error de conexion. Intenta de nuevo.');
    } finally {
      enviando = false;
    }
  }

  // -- Utilidades ----------------------------------------------------------

  /**
   * Muestra una notificacion toast durante 4 segundos y luego la oculta.
   * @param {'success'|'error'} tipo - El tipo visual del toast.
   * @param {string} mensaje - El texto del mensaje a mostrar en el toast.
   */
  function mostrarToast(tipo, mensaje) {
    toast = { tipo, mensaje };
    setTimeout(() => { toast = null; }, 4000);
  }

  /**
   * Convierte un ID de estado de agencia en su etiqueta legible por humanos.
   * @param {number} id - El valor de estadoAgenciaID (1=Activa, 2=Inactiva, 3=Suspendida).
   * @returns {string} La etiqueta correspondiente o 'Desconocido' para IDs desconocidos.
   */
  const estadoAgenciaLabel = (id) => ({ 1: 'Activa', 2: 'Inactiva', 3: 'Suspendida' }[id] ?? 'Desconocido');

  /**
   * Convierte un ID de estado de agencia en su clase CSS modificadora de badge.
   * @param {number} id - El valor de estadoAgenciaID.
   * @returns {string} La cadena de clase CSS o cadena vacia para IDs desconocidos.
   */
  const estadoAgenciaClass = (id) => ({ 1: 'badge--active', 2: 'badge--inactive', 3: 'badge--suspended' }[id] ?? '');

  /**
   * Convierte un ID de estado de hotel aliado en su etiqueta legible por humanos.
   * @param {number} id - El valor de EstadoID (1=Activo, otros pueden variar).
   * @returns {string} La etiqueta correspondiente o 'Desconocido' para IDs desconocidos.
   */
  const estadoHotelLabel = (id) => ({ 1: 'Activo', 2: 'Inactivo', 3: 'Suspendido' }[id] ?? 'Desconocido');

  /**
   * Convierte un ID de estado de hotel aliado en su clase CSS modificadora de badge.
   * @param {number} id - El valor de EstadoID.
   * @returns {string} La cadena de clase CSS o cadena vacia para IDs desconocidos.
   */
  const estadoHotelClass = (id) => ({ 1: 'badge--active', 2: 'badge--inactive', 3: 'badge--suspended' }[id] ?? '');
</script>

<!-- Notificacion toast de exito o error mostrada temporalmente -->
{#if toast}
  <div class="mi-agencia-toast mi-agencia-toast--{toast.tipo}">
    <span class="mi-agencia-toast__icon">{toast.tipo === 'success' ? '✓' : '✕'}</span>
    {toast.mensaje}
  </div>
{/if}

<!-- Contenedor principal del panel de agencia / hotel -->
<div class="admin">
  <div class="admin__container">

    <!-- Encabezado de pagina con boton de regreso y titulo del panel -->
    <header class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        ← Volver al inicio
      </button>
      <h1 class="admin__title">Mi Panel Webservice</h1>
      <p class="admin__subtitle">
        Gestiona tu agencia de viajes o tu hotel aliado desde aqui
      </p>
    </header>

    <!-- Estado de carga, error global, detalle de entidad existente o formularios de registro -->
    {#if cargando}
      <div class="admin-section" style="text-align:center; padding:4rem 2rem;">
        <div class="mi-agencia-spinner"></div>
        <p style="color:var(--text-muted); margin-top:1rem;">Cargando informacion...</p>
      </div>

    {:else if errorGlobal}
      <div class="admin-section">
        <div class="mi-agencia-alert mi-agencia-alert--error">
          <span>⚠</span> {errorGlobal}
        </div>
        <button class="btn-primary" style="max-width:200px; margin-top:1.5rem;" on:click={cargarMiEntidad}>
          Reintentar
        </button>
      </div>

    {:else if tieneAgencia && agencia}
      <!-- Tarjetas de informacion de la agencia ya registrada -->
      <div class="admin-section">
        <div class="section-header">
          <div>
            <h2 class="admin-section__title">Tu agencia registrada</h2>
            <p class="admin-section__subtitle">
              Esta es la informacion de tu agencia. El descuento y el estado son gestionados por el administrador.
            </p>
          </div>
          <span class="mi-agencia-badge {estadoAgenciaClass(agencia.estadoAgenciaID)}">
            {estadoAgenciaLabel(agencia.estadoAgenciaID)}
          </span>
        </div>

        <div class="mi-agencia-cards">

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🏢</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Nombre de la agencia</p>
              <p class="mi-agencia-card__value">{agencia.nombre}</p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">✉️</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Correo de contacto</p>
              <p class="mi-agencia-card__value">{agencia.correo}</p>
            </div>
          </div>

          <!-- URL de la agencia registrada al crear -->
          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🔗</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">URL de la agencia</p>
              <p class="mi-agencia-card__value mi-agencia-card__value--url">
                {agencia.urlAgencia || '—'}
              </p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🏷️</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Descuento asignado</p>
              <p class="mi-agencia-card__value mi-agencia-card__value--highlight">
                {agencia.porcentajeDescuento}%
              </p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🆔</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">ID de agencia</p>
              <p class="mi-agencia-card__value"># {agencia.id}</p>
            </div>
          </div>

        </div>

        <div class="mi-agencia-notice">
          <span>ℹ️</span>
          <p>
            Para modificar los datos de tu agencia, el descuento o el estado,
            comunicarte con el administrador del sistema.
          </p>
        </div>
      </div>

    {:else if tieneHotel && hotel}
      <!-- Tarjetas de informacion del hotel aliado ya registrado -->
      <div class="admin-section">
        <div class="section-header">
          <div>
            <h2 class="admin-section__title">Tu hotel aliado registrado</h2>
            <p class="admin-section__subtitle">
              Esta es la informacion de tu hotel aliado. El estado es gestionado por el administrador.
            </p>
          </div>
          <span class="mi-agencia-badge {estadoHotelClass(hotel.estadoID)}">
            {estadoHotelLabel(hotel.estadoID)}
          </span>
        </div>

        <div class="mi-agencia-cards">

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🏨</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">Nombre del hotel</p>
              <p class="mi-agencia-card__value">{hotel.nombre}</p>
            </div>
          </div>

          <!-- URL base de la API del hotel para la comunicacion con la aerolinea -->
          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🔗</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">URL de la API del hotel</p>
              <p class="mi-agencia-card__value mi-agencia-card__value--url">
                {hotel.url || '—'}
              </p>
            </div>
          </div>

          <!-- URL publica que se muestra a los pasajeros en el buscador -->
          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🌐</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">URL publica para usuarios</p>
              <p class="mi-agencia-card__value mi-agencia-card__value--url">
                {hotel.urlParaUsuario || '—'}
              </p>
            </div>
          </div>

          <div class="mi-agencia-card">
            <span class="mi-agencia-card__icon">🆔</span>
            <div class="mi-agencia-card__body">
              <p class="mi-agencia-card__label">ID del hotel</p>
              <p class="mi-agencia-card__value"># {hotel.id}</p>
            </div>
          </div>

        </div>

        <div class="mi-agencia-notice">
          <span>ℹ️</span>
          <p>
            Los tokens de conexion se generan automaticamente al establecer el enlace con la aerolinea.
            Para modificar el estado o cualquier otro dato, comunicarte con el administrador del sistema.
          </p>
        </div>
      </div>

    {:else}
      <!-- El usuario aun no tiene ninguna entidad registrada -->
      <div class="admin-section">

        {#if tipoSeleccionado === null}
          <!-- Paso 1: selector de tipo (agencia o hotel) -->
          <div class="section-header">
            <div>
              <h2 class="admin-section__title">Registra tu entidad</h2>
              <p class="admin-section__subtitle">
                Como usuario Webservice puedes registrar <strong>una agencia de viajes</strong>
                o un <strong>hotel aliado</strong>. Solo puedes elegir una de las dos opciones.
              </p>
            </div>
          </div>

          <!-- Dos tarjetas clicables para que el usuario elija que desea registrar -->
          <div class="mi-agencia-tipo-grid">

            <button class="mi-agencia-tipo-card" on:click={() => tipoSeleccionado = 'agencia'}>
              <span class="mi-agencia-tipo-card__icon">🏢</span>
              <p class="mi-agencia-tipo-card__title">Agencia de viajes</p>
              <p class="mi-agencia-tipo-card__desc">
                Registra tu agencia para ofrecer vuelos con descuentos a tus clientes
                a traves de la plataforma.
              </p>
            </button>

            <button class="mi-agencia-tipo-card" on:click={() => tipoSeleccionado = 'hotel'}>
              <span class="mi-agencia-tipo-card__icon">🏨</span>
              <p class="mi-agencia-tipo-card__title">Hotel aliado</p>
              <p class="mi-agencia-tipo-card__desc">
                Registra tu hotel para que los pasajeros puedan encontrarlo al buscar
                alojamiento en sus destinos.
              </p>
            </button>

          </div>

        {:else if tipoSeleccionado === 'agencia'}
          <!-- Paso 2a: formulario de registro de agencia -->
          <button class="mi-agencia-tipo-back" on:click={() => tipoSeleccionado = null}>
            ← Cambiar tipo de entidad
          </button>

          <div class="section-header">
            <div>
              <h2 class="admin-section__title">Registra tu agencia</h2>
              <p class="admin-section__subtitle">
                Completa los campos a continuacion para registrar tu agencia de viajes.
              </p>
            </div>
          </div>

          <div class="mi-agencia-form">

            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="nombre">
                Nombre de la agencia <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="nombre"
                class="mi-agencia-field__input {erroresAgencia.nombre ? 'mi-agencia-field__input--error' : ''}"
                type="text"
                placeholder="Ej. Agencia Viajes GT"
                bind:value={nombre}
                disabled={enviando}
                maxlength="120"
              />
              {#if erroresAgencia.nombre}
                <p class="mi-agencia-field__error">{erroresAgencia.nombre}</p>
              {/if}
            </div>

            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="correo">
                Correo de contacto <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="correo"
                class="mi-agencia-field__input {erroresAgencia.correo ? 'mi-agencia-field__input--error' : ''}"
                type="email"
                placeholder="agencia@ejemplo.com"
                bind:value={correo}
                disabled={enviando}
                maxlength="200"
              />
              {#if erroresAgencia.correo}
                <p class="mi-agencia-field__error">{erroresAgencia.correo}</p>
              {/if}
            </div>

            <!-- URL publica de la agencia: necesaria para que la aerolinea se comunique con ella -->
            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="urlAgencia">
                URL de la agencia <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="urlAgencia"
                class="mi-agencia-field__input {erroresAgencia.urlAgencia ? 'mi-agencia-field__input--error' : ''}"
                type="url"
                placeholder="https://mi-agencia.com"
                bind:value={urlAgencia}
                disabled={enviando}
                maxlength="300"
              />
              {#if erroresAgencia.urlAgencia}
                <p class="mi-agencia-field__error">{erroresAgencia.urlAgencia}</p>
              {/if}
            </div>

            <div class="mi-agencia-notice">
              <span>ℹ️</span>
              <p>
                El porcentaje de descuento y el estado de tu agencia seran asignados
                posteriormente por el administrador. Los tokens de conexion se generan
                automaticamente al establecer el enlace con el sistema.
              </p>
            </div>

            <!-- Boton de envio del formulario de creacion de agencia -->
            <div class="mi-agencia-form__actions">
              <button
                class="btn-primary"
                on:click={handleCrearAgencia}
                disabled={enviando}
              >
                {#if enviando}
                  Registrando...
                {:else}
                  Registrar agencia
                {/if}
              </button>
            </div>

          </div>

        {:else if tipoSeleccionado === 'hotel'}
          <!-- Paso 2b: formulario de registro de hotel aliado -->
          <button class="mi-agencia-tipo-back" on:click={() => tipoSeleccionado = null}>
            ← Cambiar tipo de entidad
          </button>

          <div class="section-header">
            <div>
              <h2 class="admin-section__title">Registra tu hotel aliado</h2>
              <p class="admin-section__subtitle">
                Completa los campos a continuacion para registrar tu hotel aliado en la plataforma.
              </p>
            </div>
          </div>

          <div class="mi-agencia-form">

            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="nombreHotel">
                Nombre del hotel <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="nombreHotel"
                class="mi-agencia-field__input {erroresHotel.nombre ? 'mi-agencia-field__input--error' : ''}"
                type="text"
                placeholder="Ej. Hotel Las Palmas"
                bind:value={nombreHotel}
                disabled={enviando}
                maxlength="120"
              />
              {#if erroresHotel.nombre}
                <p class="mi-agencia-field__error">{erroresHotel.nombre}</p>
              {/if}
            </div>

            <!-- URL base de la API del hotel para que la aerolinea pueda consultarlo -->
            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="urlHotel">
                URL de la API del hotel <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="urlHotel"
                class="mi-agencia-field__input {erroresHotel.url ? 'mi-agencia-field__input--error' : ''}"
                type="url"
                placeholder="https://api.mi-hotel.com"
                bind:value={urlHotel}
                disabled={enviando}
                maxlength="300"
              />
              <p style="font-size:.78rem; color:var(--text-muted); margin:.25rem 0 0;">
                URL interna que usa la aerolinea para comunicarse con tu sistema.
              </p>
              {#if erroresHotel.url}
                <p class="mi-agencia-field__error">{erroresHotel.url}</p>
              {/if}
            </div>

            <!-- URL publica del hotel que se mostrara a los pasajeros en los resultados -->
            <div class="mi-agencia-field">
              <label class="mi-agencia-field__label" for="urlParaUsuario">
                URL publica para usuarios <span class="mi-agencia-field__required">*</span>
              </label>
              <input
                id="urlParaUsuario"
                class="mi-agencia-field__input {erroresHotel.urlParaUsuario ? 'mi-agencia-field__input--error' : ''}"
                type="url"
                placeholder="https://www.mi-hotel.com"
                bind:value={urlParaUsuario}
                disabled={enviando}
                maxlength="300"
              />
              <p style="font-size:.78rem; color:var(--text-muted); margin:.25rem 0 0;">
                URL que se mostrara a los pasajeros en los resultados de busqueda.
              </p>
              {#if erroresHotel.urlParaUsuario}
                <p class="mi-agencia-field__error">{erroresHotel.urlParaUsuario}</p>
              {/if}
            </div>

            <div class="mi-agencia-notice">
              <span>ℹ️</span>
              <p>
                Los tokens de conexion se generan automaticamente al establecer el enlace
                con la aerolinea. El estado del hotel sera asignado por el administrador.
              </p>
            </div>

            <!-- Boton de envio del formulario de creacion de hotel aliado -->
            <div class="mi-agencia-form__actions">
              <button
                class="btn-primary"
                on:click={handleCrearHotel}
                disabled={enviando}
              >
                {#if enviando}
                  Registrando...
                {:else}
                  Registrar hotel aliado
                {/if}
              </button>
            </div>

          </div>
        {/if}

      </div>
    {/if}

  </div>
</div>