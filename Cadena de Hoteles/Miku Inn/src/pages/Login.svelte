<script>
  /**
   * @file Login.svelte
   * @description Pagina de inicio de sesion de Miku Inn. Permite al usuario autenticarse
   * con su email/usuario y contrasena. Soporta la opcion "recordarme" guardando el email
   * en localStorage, muestra errores de validacion en tiempo real y redirige al home
   * tras un login exitoso.
   */

  import { createEventDispatcher } from 'svelte';
  export let navigateTo;
  import '../styles/login.css';
  import { onMount } from 'svelte';

  const dispatch = createEventDispatcher();

  /** URL base del backend. @type {string} */
      import { API } from '../lib/api.js';


  /**
   * Datos del formulario de inicio de sesion.
   * @type {{ email: string, password: string }}
   */
  let formData = { email: '', password: '' };

  /** Controla si la contrasena se muestra en texto plano o como puntos. @type {boolean} */
  let showPassword = false;

  /** Si true, guarda el email en localStorage para pre-rellenarlo en futuros accesos. @type {boolean} */
  let rememberMe = false;

  /**
   * Errores de validacion por campo del formulario.
   * @type {Record<string, string>}
   */
  let errors = {};

  /** True mientras la peticion de login esta en curso. @type {boolean} */
  let isSubmitting = false;

  /** True cuando el login fue exitoso; muestra el mensaje de bienvenida. @type {boolean} */
  let loginSuccess = false;

  /** Mensaje de error proveniente del servidor (credenciales incorrectas, etc.). @type {string} */
  let serverError = '';

  /** Flag que evita limpiar el error del servidor antes de que el usuario lo vea. @type {boolean} */
  let errorShown = false;

  /** Indica si el captcha fue verificado. @type {boolean} */
  let captchaVerified = false;

  /** Indica si la verificacion del captcha esta en proceso. @type {boolean} */
  let captchaLoading = false;

  /** Indica si hubo un error en la verificacion del captcha. @type {boolean} */
  let captchaError = false;

  /**
   * Simula la verificacion del captcha con un pequeno retraso y un 5% de
   * probabilidad de fallo para imitar el comportamiento de reCAPTCHA.
   */
  function handleCaptchaClick() {
    if (captchaVerified) return;
    captchaLoading = true; captchaError = false;
    setTimeout(() => {
      captchaLoading = false;
      if (Math.random() > 0.05) { captchaVerified = true; }
      else { captchaError = true; setTimeout(() => { captchaError = false; }, 3000); }
    }, 1500);
  }

  /** Reinicia el estado del captcha. */
  function resetCaptcha() { captchaVerified = false; captchaLoading = false; captchaError = false; }

  onMount(() => {
    // Si el usuario tenia "recordarme" activo, pre-rellena el campo de email
    const saved = localStorage.getItem('rememberEmail');
    if (saved) { formData.email = saved; rememberMe = true; }
  });

  /**
   * Limpia el error del servidor cuando el usuario empieza a editar los campos
   * despues de haber visto el error. Evita que el mensaje desaparezca al montar.
   */
  function onFieldChange() {
    if (errorShown) {
      serverError = '';
      errorShown = false;
    }
  }

  /**
   * Valida los campos del formulario antes de enviarlo.
   * @returns {boolean} True si es valido, false si hay errores.
   */
  function validateForm() {
    errors = {};
    if (!formData.email.trim()) errors.email = 'El usuario o email es requerido';
    if (!formData.password) errors.password = 'La contraseña es requerida';
    if (!captchaVerified) errors.captcha = 'Debes verificar que no eres un robot';
    return Object.keys(errors).length === 0;
  }

  /**
   * Envia las credenciales al endpoint de autenticacion del backend.
   * Si el login es exitoso, persiste el email si aplica, emite el evento 'login'
   * y navega al home tras 1.5 segundos.
   * @async
   * @param {Event} e - Evento submit del formulario.
   * @returns {Promise<void>}
   */
  async function handleLogin(e) {
    e.preventDefault();
    if (!validateForm()) return;

    isSubmitting = true;
    serverError = '';
    errorShown = false;

    try {
      const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          identificador: formData.email,
          contrasena: formData.password
        })
      });

      const data = await res.json();

      if (!res.ok) {
        serverError = 'Usuario o contraseña incorrectos.';
        errorShown = true;
        formData.password = '';
        return;
      }

      // Persistir o limpiar el email segun la preferencia del usuario
      if (rememberMe) localStorage.setItem('rememberEmail', formData.email);
      else            localStorage.removeItem('rememberEmail');

      loginSuccess = true;
      dispatch('login', { name: data.username, rolId: data.rolId });
      setTimeout(() => navigateTo('home'), 1500);

    } catch (err) {
      serverError = `Error de conexión: ${err.message}`;
      errorShown = true;
    } finally {
      isSubmitting = false;
    }
  }
</script>

<!-- Pagina de inicio de sesion -->
<div class="login-page">
  <div class="login-container">
    <div class="login-card">

      <!-- Boton para volver a la pagina principal sin iniciar sesion -->
      <button class="login__back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <!-- Cabecera con logo, titulo y subtitulo -->
      <div class="login__header">
        <div class="login__logo-section">
          <img src="/src/assets/mikuinn-logo.png" alt="Miku Inn" class="login__logo-image" />
        </div>
        <h2 class="login__title">Iniciar Sesión</h2>
        <p class="login__subtitle">Accede a tu cuenta y gestiona tus reservas</p>
      </div>

      <!-- Pantalla de exito que se muestra brevemente tras el login -->
      {#if loginSuccess}
        <div class="login__success-message">
          <div class="login__success-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h3>¡Bienvenido de vuelta!</h3>
          <p>Iniciando sesión...</p>
          <div class="login__loading-dots">
            <span></span><span></span><span></span>
          </div>
        </div>

      {:else}
        <!-- Formulario de credenciales -->
        <form on:submit={handleLogin} class="login-form">

          <!-- Alerta de error proveniente del servidor -->
          {#if serverError}
            <div class="alert alert-error">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <span>{serverError}</span>
            </div>
          {/if}

          <!-- Campo de usuario o correo electronico -->
          <div class="login__form-field">
            <label for="email">Usuario o Correo Electrónico</label>
            <div class="login__input-with-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input type="text" id="email" bind:value={formData.email}
                on:input={onFieldChange}
                placeholder="usuario o tu@email.com"
                class:error={errors.email}
                autocomplete="username" />
            </div>
            {#if errors.email}<span class="login__error-text">{errors.email}</span>{/if}
          </div>

          <!-- Campo de contrasena con toggle de visibilidad -->
          <div class="login__form-field">
            <label for="password">Contraseña</label>
            <div class="login__password-field">
              <input type={showPassword ? 'text' : 'password'} id="password"
                bind:value={formData.password}
                on:input={onFieldChange}
                placeholder="Ingresa tu contraseña"
                class:error={errors.password}
                autocomplete="current-password" />
              <button type="button" class="login__toggle-btn"
                on:click={() => showPassword = !showPassword} tabindex="-1">
                {#if showPassword}
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                {:else}
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                {/if}
              </button>
            </div>
            {#if errors.password}<span class="login__error-text">{errors.password}</span>{/if}
          </div>

          <!-- Opciones adicionales: checkbox "recordarme" -->
          <div class="form-options">
            <label class="login__checkbox-label">
              <input type="checkbox" bind:checked={rememberMe} />
              <span class="login__checkbox-custom"></span>
              <span class="login__checkbox-text">Recordarme</span>
            </label>

          </div>

          <!-- Captcha simulado estilo reCAPTCHA -->
          <div class="captcha-container">
            <div class="captcha-box" class:verified={captchaVerified} class:error={captchaError}>
              <button type="button" class="captcha-label" on:click={handleCaptchaClick}
                style="background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:0.875rem;padding:0;">
                <div class="captcha-checkbox">
                  {#if captchaLoading}
                    <div class="captcha-spinner"></div>
                  {:else if captchaVerified}
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  {/if}
                </div>
                <span class="captcha-text">No soy un robot</span>
              </button>
              <div class="captcha-logo">
                <div class="recaptcha-badge">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#667eea"/>
                    <path d="M2 17L12 22L22 17" stroke="#667eea" stroke-width="2"/>
                    <path d="M2 12L12 17L22 12" stroke="#667eea" stroke-width="2"/>
                  </svg>
                  <div class="recaptcha-text">
                    <span>reCAPTCHA</span>
                    <div class="recaptcha-links"><span>Privacidad</span><span>-</span><span>Términos</span></div>
                  </div>
                </div>
              </div>
            </div>
            {#if errors.captcha}<span class="login__error-text">{errors.captcha}</span>{/if}
            {#if captchaError}
              <div class="captcha-error-message">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                Error en la verificación. Intenta de nuevo.
                <button type="button" class="retry-captcha" on:click={resetCaptcha}>Reintentar</button>
              </div>
            {/if}
          </div>

          <!-- Boton de envio con spinner mientras procesa -->
          <button type="submit" class="login__submit-btn" disabled={isSubmitting}>
            {#if isSubmitting}
              <svg class="login__spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 1 1-6.219-8.56" />
              </svg>
              Iniciando sesión...
            {:else}
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" y1="12" x2="3" y2="12"></line>
              </svg>
              Iniciar Sesión
            {/if}
          </button>

          <div class="divider"><span>o continua con</span></div>

        </form>

        <!-- Enlace para ir a la pagina de registro -->
        <div class="login__footer-text">
          ¿No tienes una cuenta?
          <button type="button" class="login__link-btn" on:click={() => navigateTo('register')}>
            Regístrate aquí
          </button>
        </div>

      {/if}
    </div>
  </div>
</div>
