<script>
  export let navigateTo;
  
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
      
      // Simulación: 80% éxito, 20% error para demo
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
      <button class="back-link" on:click={() => navigateTo('home')}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </button>

      <!-- Header -->
      <div class="header">
        <div class="logo-section">
          <img src="/src/assets/mikuinn-logo.png" alt="Miku Inn" class="logo-image" />
        </div>
        <h2 class="title">Iniciar Sesión</h2>
        <p class="subtitle">Accede a tu cuenta y gestiona tus reservas</p>
      </div>

      {#if loginSuccess}
        <!-- Success Message -->
        <div class="success-message">
          <div class="success-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h3>¡Bienvenido de vuelta!</h3>
          <p>Iniciando sesión...</p>
          <div class="loading-dots">
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
          <div class="form-field">
            <label for="email">
              Correo Electrónico
            </label>
            <div class="input-with-icon">
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
              <span class="error-text">{errors.email}</span>
            {/if}
          </div>

          <!-- Password Field -->
          <div class="form-field">
            <label for="password">
              Contraseña
            </label>
            <div class="password-field">
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
                class="toggle-btn"
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
              <span class="error-text">{errors.password}</span>
            {/if}
          </div>

          <!-- Remember Me & Forgot Password -->
          <div class="form-options">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                bind:checked={rememberMe}
              />
              <span class="checkbox-custom"></span>
              <span class="checkbox-text">Recordarme</span>
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
            class="submit-btn"
            disabled={isSubmitting}
          >
            {#if isSubmitting}
              <svg class="spinner" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
          <div class="footer-text">
            ¿No tienes una cuenta? 
            <button type="button" class="link-btn" on:click={() => navigateTo('register')}>
              Regístrate aquí
            </button>
          </div>
        </form>
      {/if}
    </div>

    <!-- Additional Info -->
    <div class="info-cards">
      <div class="info-card">
        <div class="info-icon">🔒</div>
        <div class="info-text">
          <strong>Seguro</strong>
          <span>Conexión encriptada</span>
        </div>
      </div>
      <div class="info-card">
        <div class="info-icon">⚡</div>
        <div class="info-text">
          <strong>Rápido</strong>
          <span>Acceso instantáneo</span>
        </div>
      </div>
      <div class="info-card">
        <div class="info-icon">💼</div>
        <div class="info-text">
          <strong>Reservas</strong>
          <span>Gestiona fácilmente</span>
        </div>
      </div>
    </div>
  </div>
</div>

<style>
  /* Variables */
  :root {
    --primary: #667eea;
    --primary-dark: #5568d3;
    --secondary: #764ba2;
    --success: #10b981;
    --danger: #ef4444;
    --warning: #f59e0b;
    --info: #3b82f6;
  }
  
  /* Base */
  .login-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
    padding: 2rem 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .login-container {
    width: 100%;
    max-width: 480px;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .login-card {
    background: rgba(30, 41, 59, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 2.5rem;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  /* Back Link */
  .back-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: #94a3b8;
    font-size: 0.9rem;
    font-weight: 500;
    margin-bottom: 2rem;
    transition: all 0.2s;
    background: none;
    border: none;
    cursor: pointer;
    padding: 0;
  }
  
  .back-link:hover {
    color: var(--primary);
    transform: translateX(-4px);
  }
  
  /* Header */
  .header {
    text-align: center;
    margin-bottom: 2rem;
  }
  
  .logo-section {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 1.5rem;
  }
  
  .logo-image {
    height: 180px;
    width: auto;
    display: block;
  }
  
  .title {
    font-size: 1.75rem;
    font-weight: 700;
    color: #f8fafc;
    margin: 0 0 0.75rem 0;
  }
  
  .subtitle {
    font-size: 1rem;
    color: #94a3b8;
    margin: 0;
    line-height: 1.6;
  }
  
  /* Success Message */
  .success-message {
    text-align: center;
    padding: 2rem 1rem;
  }
  
  .success-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 96px;
    height: 96px;
    background: linear-gradient(135deg, var(--success), #059669);
    border-radius: 50%;
    margin-bottom: 1.5rem;
    animation: scaleIn 0.5s ease;
  }
  
  .success-icon svg {
    color: white;
  }
  
  @keyframes scaleIn {
    from {
      transform: scale(0);
      opacity: 0;
    }
    to {
      transform: scale(1);
      opacity: 1;
    }
  }
  
  .success-message h3 {
    font-size: 1.5rem;
    font-weight: 700;
    color: #f8fafc;
    margin: 0 0 0.75rem 0;
  }
  
  .success-message p {
    font-size: 1rem;
    color: #94a3b8;
    margin: 0 0 2rem 0;
  }
  
  .loading-dots {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
  }
  
  .loading-dots span {
    width: 10px;
    height: 10px;
    background: var(--primary);
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
  }
  
  .loading-dots span:nth-child(1) {
    animation-delay: -0.32s;
  }
  
  .loading-dots span:nth-child(2) {
    animation-delay: -0.16s;
  }
  
  @keyframes bounce {
    0%, 80%, 100% {
      transform: scale(0);
    }
    40% {
      transform: scale(1);
    }
  }
  
  /* Form */
  .login-form {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }
  
  /* Alert */
  .alert {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    border-radius: 12px;
    font-size: 0.9rem;
    font-weight: 500;
    animation: slideIn 0.3s ease;
  }
  
  @keyframes slideIn {
    from {
      transform: translateY(-10px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }
  
  .alert-error {
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid var(--danger);
    color: var(--danger);
  }
  
  .alert svg {
    flex-shrink: 0;
  }
  
  /* Form Fields */
  .form-field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .form-field label {
    font-size: 0.9rem;
    font-weight: 600;
    color: #e2e8f0;
  }
  
  .form-field input {
    width: 100%;
    padding: 0.875rem 1rem;
    border: 1.5px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    font-size: 0.95rem;
    font-family: inherit;
    color: #f8fafc;
    background: rgba(15, 23, 42, 0.5);
    transition: all 0.2s;
  }
  
  .form-field input:focus {
    outline: none;
    border-color: var(--primary);
    background: rgba(15, 23, 42, 0.8);
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
  }
  
  .form-field input.error {
    border-color: var(--danger);
    background: rgba(239, 68, 68, 0.1);
  }
  
  .form-field input::placeholder {
    color: #64748b;
  }
  
  /* Input with icon */
  .input-with-icon {
    position: relative;
  }
  
  .input-with-icon svg {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: #64748b;
    pointer-events: none;
  }
  
  .input-with-icon input {
    padding-left: 3rem;
  }
  
  /* Password field */
  .password-field {
    position: relative;
  }
  
  .password-field input {
    padding-right: 3rem;
  }
  
  .toggle-btn {
    position: absolute;
    right: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #64748b;
    cursor: pointer;
    padding: 0.5rem;
    display: flex;
    transition: color 0.2s;
  }
  
  .toggle-btn:hover {
    color: var(--primary);
  }
  
  /* Error text */
  .error-text {
    display: block;
    color: var(--danger);
    font-size: 0.8rem;
    font-weight: 500;
  }
  
  /* Form Options */
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    cursor: pointer;
  }
  
  .checkbox-label input {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
  }
  
  .checkbox-custom {
    width: 20px;
    height: 20px;
    border: 2px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    flex-shrink: 0;
    transition: all 0.2s;
    position: relative;
    background: rgba(15, 23, 42, 0.5);
  }
  
  .checkbox-label input:checked ~ .checkbox-custom {
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    border-color: transparent;
  }
  
  .checkbox-label input:checked ~ .checkbox-custom::after {
    content: '';
    position: absolute;
    left: 6px;
    top: 2px;
    width: 4px;
    height: 9px;
    border: solid white;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
  }
  
  .checkbox-text {
    font-size: 0.875rem;
    color: #cbd5e1;
  }
  
  .forgot-link {
    background: none;
    border: none;
    color: var(--primary);
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    padding: 0;
  }
  
  .forgot-link:hover {
    text-decoration: underline;
  }
  
  /* Submit Button */
  .submit-btn {
    width: 100%;
    padding: 1rem;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  }
  
  .submit-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
  }
  
  .submit-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }
  
  .spinner {
    animation: spin 0.8s linear infinite;
  }
  
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  
  /* Divider */
  .divider {
    position: relative;
    text-align: center;
    margin: 1rem 0;
  }
  
  .divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.1);
  }
  
  .divider span {
    position: relative;
    display: inline-block;
    padding: 0 1rem;
    background: rgba(30, 41, 59, 0.95);
    color: #94a3b8;
    font-size: 0.85rem;
    font-weight: 500;
  }
  
  /* Social Buttons */
  .social-buttons {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 0.75rem;
  }
  
  .social-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.875rem 0.5rem;
    background: rgba(15, 23, 42, 0.5);
    border: 1.5px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    color: #e2e8f0;
    font-size: 0.8rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: inherit;
  }
  
  .social-btn:hover {
    background: rgba(15, 23, 42, 0.8);
    border-color: var(--primary);
    transform: translateY(-2px);
  }
  
  /* Footer */
  .footer-text {
    text-align: center;
    font-size: 0.9rem;
    color: #94a3b8;
    margin-top: 0.5rem;
  }
  
  .link-btn {
    background: none;
    border: none;
    color: var(--primary);
    text-decoration: none;
    font-weight: 600;
    cursor: pointer;
    padding: 0;
    font-size: inherit;
  }
  
  .link-btn:hover {
    text-decoration: underline;
  }
  
  /* Info Cards */
  .info-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
  }
  
  .info-card {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    background: rgba(30, 41, 59, 0.5);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
  }
  
  .info-icon {
    font-size: 1.5rem;
  }
  
  .info-text {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }
  
  .info-text strong {
    color: #f8fafc;
    font-size: 0.85rem;
    font-weight: 600;
  }
  
  .info-text span {
    color: #94a3b8;
    font-size: 0.75rem;
  }
  
  /* Responsive */
  @media (max-width: 640px) {
    .login-page {
      padding: 1rem;
    }
    
    .login-card {
      padding: 2rem 1.5rem;
    }
    
    .logo-image {
      height: 120px;
    }
    
    .title {
      font-size: 1.5rem;
    }
    
    .social-buttons {
      grid-template-columns: 1fr;
    }
    
    .info-cards {
      grid-template-columns: 1fr;
    }
    
    .form-options {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.75rem;
    }
  }
</style>