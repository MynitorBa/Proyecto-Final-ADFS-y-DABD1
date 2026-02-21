<script>
  export let navigateTo;
  import '../styles/login.css';
  
  // Form data
  let formData = {
    email: '',
    password: ''
  };
  
  // UI States
  let showPassword = false;
  let rememberMe = false;
  let errors = {};
  let isSubmitting = false;
  let loginSuccess = false;
  let serverError = '';
  let attemptCount = 0;
  
  // Validaciones
  function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
  
  function validateForm() {
    errors = {};
    serverError = '';
    
    if (!formData.email.trim()) {
      errors.email = 'El email es requerido';
    } else if (!validateEmail(formData.email)) {
      errors.email = 'Email inválido';
    }
    
    if (!formData.password) {
      errors.password = 'La contraseña es requerida';
    } else if (formData.password.length < 6) {
      errors.password = 'La contraseña debe tener al menos 6 caracteres';
    }
    
    return Object.keys(errors).length === 0;
  }
  
  // Manejo de envío
  async function handleLogin(e) {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    isSubmitting = true;
    attemptCount++;
    
    // Simular llamada al backend
    setTimeout(() => {
      isSubmitting = false;
      
      // Simulación: 80% éxito, 20% login__error para demo
      if (Math.random() > 0.2 || attemptCount > 2) {
        // Login exitoso
        loginSuccess = true;
        
        console.log('Login exitoso:', {
          email: formData.email,
          rememberMe,
          timestamp: new Date().toISOString()
        });
        
        // En producción, aquí se guardaría el token
        if (rememberMe) {
          localStorage.setItem('rememberEmail', formData.email);
        }
        
        // Redirigir al home después de 1.5 segundos
        setTimeout(() => {
          navigateTo('home');
        }, 1500);
      } else {
        // Login fallido
        serverError = 'Email o contraseña incorrectos. Por favor verifica tus credenciales.';
        formData.password = ''; // Limpiar contraseña
      }
    }, 2000);
  }
  
  // Verificar si hay email guardado
  function checkRememberedEmail() {
    const savedEmail = localStorage.getItem('rememberEmail');
    if (savedEmail) {
      formData.email = savedEmail;
      rememberMe = true;
    }
  }
  
  // Cargar email guardado al montar
  import { onMount } from 'svelte';
  onMount(() => {
    checkRememberedEmail();
  });
  
  // Limpiar errores cuando el usuario empieza a escribir
  $: if (formData.email || formData.password) {
    serverError = '';
  }
</script>

<div class="login-page">
  <div class="login-container">
    <div class="login-card">
      <!-- Back Button -->
      <button class="login__back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <!-- Header -->
      <div class="login__header">
        <div class="login__logo-section">
          <img src="/src/assets/mikuinn-logo.png" alt="Miku Inn" class="login__logo-image" />
        </div>
        <h2 class="login__title">Iniciar Sesión</h2>
        <p class="login__subtitle">Accede a tu cuenta y gestiona tus reservas</p>
      </div>

      {#if loginSuccess}
        <!-- Success Message -->
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
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      {:else}
        <!-- Login Form -->
        <form on:submit={handleLogin} class="login-form">
          <!-- Server Error Alert -->
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

          <!-- Email Field -->
          <div class="login__form-field">
            <label for="email">
              Correo Electrónico
            </label>
            <div class="login__input-with-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                <polyline points="22,6 12,13 2,6"></polyline>
              </svg>
              <input
                type="email"
                id="email"
                bind:value={formData.email}
                placeholder="tu@email.com"
                class:error={errors.email}
                autocomplete="email"
              />
            </div>
            {#if errors.email}
              <span class="login__error-text">{errors.email}</span>
            {/if}
          </div>

          <!-- Password Field -->
          <div class="login__form-field">
            <label for="password">
              Contraseña
            </label>
            <div class="login__password-field">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                bind:value={formData.password}
                placeholder="Ingresa tu contraseña"
                class:error={errors.password}
                autocomplete="current-password"
              />
              <button
                type="button"
                class="login__toggle-btn"
                on:click={() => showPassword = !showPassword}
                tabindex="-1"
              >
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
            {#if errors.password}
              <span class="login__error-text">{errors.password}</span>
            {/if}
          </div>

          <!-- Remember Me & Forgot Password -->
          <div class="form-options">
            <label class="login__checkbox-label">
              <input 
                type="checkbox" 
                bind:checked={rememberMe}
              />
              <span class="login__checkbox-custom"></span>
              <span class="login__checkbox-text">Recordarme</span>
            </label>
            
            <button 
              type="button" 
              class="forgot-link"
              on:click={() => navigateTo('forgot-password')}
            >
              ¿Olvidaste tu contraseña?
            </button>
          </div>

          <!-- Submit Button -->
          <button 
            type="submit" 
            class="login__submit-btn"
            disabled={isSubmitting}
          >
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

          <!-- Divider -->
          <div class="divider">
            <span>o continúa con</span>
          </div>

          <!-- Social Login Buttons -->
          <div class="social-buttons">
            <button type="button" class="social-btn" on:click={() => alert('Google login en desarrollo')}>
              <svg width="20" height="20" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
            
            <button type="button" class="social-btn" on:click={() => alert('Facebook login en desarrollo')}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="#1877F2">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
              </svg>
              Facebook
            </button>

            <button type="button" class="social-btn" on:click={() => alert('Apple login en desarrollo')}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M17.05 20.28c-.98.95-2.05.88-3.08.4-1.09-.5-2.08-.48-3.24 0-1.44.62-2.2.44-3.06-.4C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"/>
              </svg>
              Apple
            </button>
          </div>

          <!-- Register Link -->
          <div class="login__footer-text">
            ¿No tienes una cuenta? 
            <button type="button" class="login__link-btn" on:click={() => navigateTo('register')}>
              Regístrate aquí
            </button>
          </div>
        </form>
      {/if}
    </div>

    <!-- Additional Info -->
    
  </div>
</div>