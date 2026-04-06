<script>
/**
 * @file Login.svelte
 * @description Login page for Broom AirLine. Renders a two-panel layout with a decorative
 * image section on the left and a login form on the right. Calls the sesion store's login
 * function with the user's credentials and navigates to the home page on success. Displays
 * an inline error message on authentication failure or connection problems.
 */
  import '../styles/login.css';
  import { onMount } from 'svelte';
  import { login } from '../stores/sesion.js';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Form data object holding the user's credentials. @type {{correoOUsername: string, contrasena: string}} */
  let loginData = {
    correoOUsername: '',
    contrasena: ''
  };

  /** Whether the remember-me checkbox is checked (UI only, no persistence implemented). @type {boolean} */
  let rememberMe = false;

  /** Error message shown below the form when login fails or a connection error occurs. @type {string} */
  let loginError = '';

  /** True while the login request is in progress, used to disable the submit button. @type {boolean} */
  let submitting = false;

  onMount(() => {
    loginData = { correoOUsername: '', contrasena: '' };
    rememberMe = false;
    loginError = '';
  });

  /**
   * Submits the login credentials to the sesion store's login function. On success,
   * navigates the user to the home page. On failure, sets loginError with a descriptive
   * message distinguishing between bad credentials and connection errors.
   * @async
   * @returns {Promise<void>}
   */
  async function handleLogin() {
    loginError = '';
    submitting = true;

    try {
      const resultado = await login(loginData.correoOUsername, loginData.contrasena);

      if (!resultado.ok) {
        loginError = 'Correo, username o contrasena incorrectos.';
        return;
      }

      navigateTo('home');

    } catch (error) {
      loginError = 'No se pudo conectar con el servidor.';
      console.error('Error en login:', error);
    } finally {
      submitting = false;
    }
  }
</script>

<!-- Contenedor principal de la pagina de login con layout de dos paneles -->
<div class="login">
  <div class="login__container">
    <div class="login__content">
      <!-- Panel decorativo izquierdo con imagen y mensaje de bienvenida -->
      <div class="login__image-section">
        <div class="login__image-overlay">
          <h2 class="login__image-title">Bienvenido de vuelta</h2>
          <p class="login__image-subtitle">
            Accede a tu cuenta para gestionar tus reservas y disfrutar de beneficios exclusivos
          </p>
        </div>
      </div>

      <!-- Panel derecho con formulario de autenticacion -->
      <div class="login__form-section">
        <div class="login__form-container">
          <button class="login__back" on:click={() => navigateTo('home')}>
            Volver al inicio
          </button>

          <!-- Encabezado del formulario con titulo y subtitulo -->
          <div class="login__header">
            <h1 class="login__title">Iniciar sesion</h1>
            <p class="login__subtitle">Ingresa tus credenciales para continuar</p>
          </div>

          <!-- Formulario de login con campos de correo, contrasena y recordarme -->
          <form class="login-form" on:submit|preventDefault={handleLogin}>

            <div class="login-form__field">
              <label for="correoOUsername" class="login-form__label">Correo o Username</label>
              <input
                type="text"
                id="correoOUsername"
                class="login-form__input"
                bind:value={loginData.correoOUsername}
                placeholder="correo@ejemplo.com o usuario123"
                autocomplete="off"
                required
              />
            </div>

            <div class="login-form__field">
              <label for="contrasena" class="login-form__label">Contrasena</label>
              <input
                type="password"
                id="contrasena"
                class="login-form__input"
                bind:value={loginData.contrasena}
                placeholder="Tu contrasena"
                autocomplete="off"
                required
              />
            </div>

            <div class="login-form__options">
              <label class="login-form__checkbox">
                <input
                  type="checkbox"
                  bind:checked={rememberMe}
                  class="login-form__checkbox-input"
                />
                <span class="login-form__checkbox-label">Recordarme</span>
              </label>
            </div>

            <!-- Mensaje de error de autenticacion mostrado condicionalmente -->
            {#if loginError}
              <div class="login-form__error">{loginError}</div>
            {/if}

            <button type="submit" class="login-form__submit" disabled={submitting}>
              {submitting ? 'Iniciando sesion...' : 'Iniciar sesion'}
            </button>

          </form>

          <!-- Enlace de navegacion hacia la pagina de registro -->
          <div class="login__register">
            <p class="login__register-text">
              No tienes una cuenta?
              <button
                type="button"
                class="login__register-link"
                on:click={() => navigateTo('register')}
              >
                Registrate aqui
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
