<script>
/**
 * @file MiAgencia.svelte
 * @description Agency management page for Webservice users. On load it checks whether the
 * authenticated user already has a registered agency via GET /api/agencias/mi-agencia.
 * If an agency exists, it displays its name, contact email, assigned discount percentage,
 * ID, and current status badge. If no agency exists yet, it shows a creation form with
 * name and email fields that submits to POST /api/agencias/mi-agencia. Feedback is
 * provided through a timed toast notification. Redirects to 'acceso-denegado' on 401/403.
 */
// @ts-nocheck
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';
  import '../styles/admin.css';
  import '../styles/miagencia.css';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** True while the initial agency data is being fetched from the API. @type {boolean} */
  let cargando      = true;

  /** True while the agency creation form is being submitted. @type {boolean} */
  let enviando      = false;

  /** True when the API confirms the user already has a registered agency. @type {boolean} */
  let tieneAgencia  = false;

  /** The agency object returned by the API if one exists, otherwise null. @type {object|null} */
  let agencia       = null;

  /** Global error message shown when the API call fails completely. @type {string} */
  let errorGlobal   = '';

  /** Current toast notification object with tipo and mensaje, or null when hidden. @type {{tipo: string, mensaje: string}|null} */
  let toast         = null;

  /** Agency name field bound to the creation form input. @type {string} */
  let nombre = '';

  /** Agency contact email field bound to the creation form input. @type {string} */
  let correo = '';

  /** Field-level validation error messages for the creation form. @type {{nombre: string, correo: string}} */
  let errores = { nombre: '', correo: '' };

  onMount(async () => {
    await cargarMiAgencia();
  });

  /**
   * Fetches the authenticated user's agency data from GET /api/agencias/mi-agencia.
   * Sets tieneAgencia and agencia from the response. Redirects to 'acceso-denegado' on
   * 401 or 403 status codes. Sets errorGlobal on other non-OK responses or connection errors.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarMiAgencia() {
    cargando = true;
    errorGlobal = '';
    try {
      const r = await fetch(`${API}/api/agencias/mi-agencia`, { credentials: 'include' });
      if (r.status === 401 || r.status === 403) {
        navigateTo('acceso-denegado');
        return;
      }
      if (!r.ok) { errorGlobal = 'Error al consultar tu agencia.'; return; }
      const data = await r.json();
      tieneAgencia = data.tieneAgencia;
      agencia      = data.agencia;
    } catch {
      errorGlobal = 'Error de conexion. Intenta de nuevo.';
    } finally {
      cargando = false;
    }
  }

  /**
   * Validates the agency creation form fields. Ensures nombre is non-empty and correo
   * matches a basic email pattern. Populates errores with messages for any failing field.
   * @returns {boolean} True if all fields are valid, false if any validation failed.
   */
  function validar() {
    errores = { nombre: '', correo: '' };
    let ok = true;
    if (!nombre.trim()) { errores.nombre = 'El nombre es obligatorio.'; ok = false; }
    if (!correo.trim()) { errores.correo = 'El correo es obligatorio.'; ok = false; }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) { errores.correo = 'Ingresa un correo valido.'; ok = false; }
    return ok;
  }

  /**
   * Submits the agency creation form to POST /api/agencias/mi-agencia with the nombre
   * and correo values. On success, shows a success toast, reloads the agency data, and
   * clears the form fields. On failure, shows an error toast with the server message.
   * @async
   * @returns {Promise<void>}
   */
  async function handleCrear() {
    if (!validar()) return;
    enviando = true;
    try {
      const r = await fetch(`${API}/api/agencias/mi-agencia`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: nombre.trim(), correo: correo.trim() })
      });
      const data = await r.json();
      if (r.ok) {
        mostrarToast('success', 'Agencia registrada exitosamente!');
        await cargarMiAgencia();
        nombre = ''; correo = '';
      } else {
        mostrarToast('error', data.message || 'Error al crear la agencia.');
      }
    } catch {
      mostrarToast('error', 'Error de conexion. Intenta de nuevo.');
    } finally {
      enviando = false;
    }
  }

  /**
   * Displays a toast notification for 4 seconds then clears it.
   * @param {'success'|'error'} tipo - The visual type of the toast.
   * @param {string} mensaje - The message text to display in the toast.
   */
  function mostrarToast(tipo, mensaje) {
    toast = { tipo, mensaje };
    setTimeout(() => { toast = null; }, 4000);
  }

  /**
   * Maps an agency status ID to its human-readable label string.
   * @param {number} id - The estadoAgenciaID value (1=Activa, 2=Inactiva, 3=Suspendida).
   * @returns {string} The corresponding label or 'Desconocido' for unknown IDs.
   */
  const estadoLabel = (id) => ({ 1: 'Activa', 2: 'Inactiva', 3: 'Suspendida' }[id] ?? 'Desconocido');

  /**
   * Maps an agency status ID to its CSS badge modifier class.
   * @param {number} id - The estadoAgenciaID value.
   * @returns {string} The CSS class string or empty string for unknown IDs.
   */
  const estadoClass = (id) => ({ 1: 'badge--active', 2: 'badge--inactive', 3: 'badge--suspended' }[id] ?? '');
</script>

<!-- Notificacion toast de exito o error mostrada temporalmente -->
{#if toast}
  <div class="mi-agencia-toast mi-agencia-toast--{toast.tipo}">
    <span class="mi-agencia-toast__icon">{toast.tipo === 'success' ? '✓' : '✕'}</span>
    {toast.mensaje}
  </div>
{/if}

<!-- Contenedor principal del panel de agencia -->
<div class="admin">
  <div class="admin__container">

    <!-- Encabezado de pagina con boton de regreso y titulo del panel -->
    <header class="admin__header">
      <button class="admin__back" on:click={() => navigateTo('home')}>
        ← Volver al inicio
      </button>
      <h1 class="admin__title">Mi Agencia</h1>
      <p class="admin__subtitle">
        Panel de gestion de tu agencia Webservice
      </p>
    </header>

    <!-- Estado de carga, error global, detalle de agencia existente o formulario de creacion -->
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
        <button class="btn-primary" style="max-width:200px; margin-top:1.5rem;" on:click={cargarMiAgencia}>
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
          <span class="mi-agencia-badge {estadoClass(agencia.estadoAgenciaID)}">
            {estadoLabel(agencia.estadoAgenciaID)}
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

    {:else}
      <!-- Formulario de registro de nueva agencia para usuario sin agencia -->
      <div class="admin-section">
        <div class="section-header">
          <div>
            <h2 class="admin-section__title">Registra tu agencia</h2>
            <p class="admin-section__subtitle">
              Como usuario Webservice puedes registrar <strong>una sola agencia</strong>.
              Completa los campos a continuacion para comenzar.
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
              class="mi-agencia-field__input {errores.nombre ? 'mi-agencia-field__input--error' : ''}"
              type="text"
              placeholder="Ej. Agencia Viajes GT"
              bind:value={nombre}
              disabled={enviando}
              maxlength="120"
            />
            {#if errores.nombre}
              <p class="mi-agencia-field__error">{errores.nombre}</p>
            {/if}
          </div>

          <div class="mi-agencia-field">
            <label class="mi-agencia-field__label" for="correo">
              Correo de contacto <span class="mi-agencia-field__required">*</span>
            </label>
            <input
              id="correo"
              class="mi-agencia-field__input {errores.correo ? 'mi-agencia-field__input--error' : ''}"
              type="email"
              placeholder="agencia@ejemplo.com"
              bind:value={correo}
              disabled={enviando}
              maxlength="200"
            />
            {#if errores.correo}
              <p class="mi-agencia-field__error">{errores.correo}</p>
            {/if}
          </div>

          <div class="mi-agencia-notice">
            <span>ℹ️</span>
            <p>
              El porcentaje de descuento y el estado de tu agencia seran asignados
              posteriormente por el administrador.
            </p>
          </div>

          <!-- Boton de envio del formulario de creacion de agencia -->
          <div class="mi-agencia-form__actions">
            <button
              class="btn-primary"
              on:click={handleCrear}
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
      </div>
    {/if}

  </div>
</div>
