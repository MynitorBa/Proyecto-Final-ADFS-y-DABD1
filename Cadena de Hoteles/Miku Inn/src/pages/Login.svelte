<script>
  import { createEventDispatcher } from 'svelte';
  export let navigateTo;
  import '../styles/login.css';
  import { onMount } from 'svelte';

  const dispatch = createEventDispatcher();
  const API = 'http://localhost:7000';

  let formData = { email: '', password: '' };
  let showPassword = false;
  let rememberMe = false;
  let errors = {};
  let isSubmitting = false;
  let loginSuccess = false;
  let serverError = '';
  let errorShown = false; // flag: se mostró un error del servidor

  onMount(() => {
    const saved = localStorage.getItem('rememberEmail');
    if (saved) { formData.email = saved; rememberMe = true; }
  });

  // Solo limpia el error cuando el usuario CAMBIA algo DESPUÉS de que se mostró
  function onFieldChange() {
    if (errorShown) {
      serverError = '';
      errorShown = false;
    }
  }

  function validateForm() {
    errors = {};
    if (!formData.email.trim()) errors.email = 'El usuario o email es requerido';
    if (!formData.password) errors.password = 'La contraseña es requerida';
    return Object.keys(errors).length === 0;
  }

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

<div class="login-page">
  <div class="login-container">
    <div class="login-card">

      <button class="login__back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <div class="login__header">
        <div class="login__logo-section">
          <img src="/src/assets/mikuinn-logo.png" alt="Miku Inn" class="login__logo-image" />
        </div>
        <h2 class="login__title">Iniciar Sesión</h2>
        <p class="login__subtitle">Accede a tu cuenta y gestiona tus reservas</p>
      </div>

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
        <form on:submit={handleLogin} class="login-form">

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

          <div class="form-options">
            <label class="login__checkbox-label">
              <input type="checkbox" bind:checked={rememberMe} />
              <span class="login__checkbox-custom"></span>
              <span class="login__checkbox-text">Recordarme</span>
            </label>
            
          </div>

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