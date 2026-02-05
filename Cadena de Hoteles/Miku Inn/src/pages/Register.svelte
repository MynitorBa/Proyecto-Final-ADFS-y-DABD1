<script>
  import logo from '../assets/mikuinn-logo.png';
  export let navigateTo;
  
  // Form data
  let formData = {
    // Información personal
    firstName: '',
    lastName: '',
    birthDate: '',
    phone: '',
    country: '',
    city: '',
    address: '',
    
    // Documento de identidad
    documentType: 'passport', // passport, dni, license
    documentNumber: '',
    
    // Credenciales
    email: '',
    password: '',
    confirmPassword: ''
  };
  
  // UI States
  let showPassword = false;
  let showConfirmPassword = false;
  let acceptTerms = false;
  let acceptPrivacy = false;
  let acceptMarketing = false;
  let errors = {};
  let isSubmitting = false;
  let registrationSuccess = false;
  
  // Captcha state
  let captchaVerified = false;
  let captchaLoading = false;
  let captchaError = false;
  
  // Países
  const countries = [
    { code: 'GT', name: 'Guatemala', flag: '🇬🇹' },
    { code: 'MX', name: 'México', flag: '🇲🇽' },
    { code: 'US', name: 'Estados Unidos', flag: '🇺🇸' },
    { code: 'CA', name: 'Canadá', flag: '🇨🇦' },
    { code: 'ES', name: 'España', flag: '🇪🇸' },
    { code: 'FR', name: 'Francia', flag: '🇫🇷' },
    { code: 'IT', name: 'Italia', flag: '🇮🇹' },
    { code: 'UK', name: 'Reino Unido', flag: '🇬🇧' },
    { code: 'DE', name: 'Alemania', flag: '🇩🇪' },
    { code: 'JP', name: 'Japón', flag: '🇯🇵' },
    { code: 'CN', name: 'China', flag: '🇨🇳' },
    { code: 'BR', name: 'Brasil', flag: '🇧🇷' },
    { code: 'AR', name: 'Argentina', flag: '🇦🇷' },
    { code: 'CL', name: 'Chile', flag: '🇨🇱' },
    { code: 'CO', name: 'Colombia', flag: '🇨🇴' },
    { code: 'PE', name: 'Perú', flag: '🇵🇪' }
  ];
  
  // Calcular edad desde fecha de nacimiento
  function calculateAge(birthDate) {
    if (!birthDate) return 0;
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }
  
  $: userAge = calculateAge(formData.birthDate);
  
  // Validaciones
  function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }
  
  function validatePhone(phone) {
    const re = /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/;
    return re.test(phone);
  }
  
  function validatePassword(password) {
    return {
      minLength: password.length >= 8,
      hasUpperCase: /[A-Z]/.test(password),
      hasLowerCase: /[a-z]/.test(password),
      hasNumber: /[0-9]/.test(password),
      hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password)
    };
  }
  
  function getPasswordStrength(password) {
    const validation = validatePassword(password);
    const checks = Object.values(validation).filter(Boolean).length;
    
    if (checks <= 2) return { text: 'Muy débil', color: '#ef4444', width: '25%' };
    if (checks <= 3) return { text: 'Débil', color: '#f59e0b', width: '50%' };
    if (checks <= 4) return { text: 'Buena', color: '#3b82f6', width: '75%' };
    return { text: 'Excelente', color: '#10b981', width: '100%' };
  }
  
  $: passwordStrength = formData.password ? getPasswordStrength(formData.password) : null;
  $: passwordValidation = validatePassword(formData.password);
  
  // Captcha - Simulación visual
  function handleCaptchaClick() {
    if (captchaVerified) return;
    
    captchaLoading = true;
    captchaError = false;
    
    // Simular verificación (1.5 segundos)
    setTimeout(() => {
      captchaLoading = false;
      // 95% de éxito
      if (Math.random() > 0.05) {
        captchaVerified = true;
      } else {
        captchaError = true;
        setTimeout(() => {
          captchaError = false;
        }, 3000);
      }
    }, 1500);
  }
  
  function resetCaptcha() {
    captchaVerified = false;
    captchaLoading = false;
    captchaError = false;
  }
  
  // Validación completa del formulario
  function validateForm() {
    errors = {};
    
    // Información personal
    if (!formData.firstName.trim()) {
      errors.firstName = 'El nombre es requerido';
    } else if (formData.firstName.trim().length < 2) {
      errors.firstName = 'El nombre debe tener al menos 2 caracteres';
    }
    
    if (!formData.lastName.trim()) {
      errors.lastName = 'Los apellidos son requeridos';
    } else if (formData.lastName.trim().length < 2) {
      errors.lastName = 'Los apellidos deben tener al menos 2 caracteres';
    }
    
    if (!formData.birthDate) {
      errors.birthDate = 'La fecha de nacimiento es requerida';
    } else if (userAge < 18) {
      errors.birthDate = 'Debes tener al menos 18 años';
    } else if (userAge > 120) {
      errors.birthDate = 'Fecha de nacimiento inválida';
    }
    
    if (!formData.phone.trim()) {
      errors.phone = 'El teléfono es requerido';
    } else if (!validatePhone(formData.phone)) {
      errors.phone = 'Formato de teléfono inválido';
    }
    
    if (!formData.country) {
      errors.country = 'Selecciona tu país';
    }
    
    if (!formData.city.trim()) {
      errors.city = 'La ciudad es requerida';
    }
    
    if (!formData.address.trim()) {
      errors.address = 'La dirección es requerida';
    }
    
    // Documento
    if (!formData.documentNumber.trim()) {
      errors.documentNumber = 'El número de documento es requerido';
    } else if (formData.documentNumber.trim().length < 5) {
      errors.documentNumber = 'Número de documento inválido';
    }
    
    // Credenciales
    if (!formData.email.trim()) {
      errors.email = 'El email es requerido';
    } else if (!validateEmail(formData.email)) {
      errors.email = 'Email inválido';
    }
    
    if (!formData.password) {
      errors.password = 'La contraseña es requerida';
    } else if (!passwordValidation.minLength || !passwordValidation.hasUpperCase || 
               !passwordValidation.hasLowerCase || !passwordValidation.hasNumber) {
      errors.password = 'La contraseña no cumple los requisitos mínimos';
    }
    
    if (!formData.confirmPassword) {
      errors.confirmPassword = 'Confirma tu contraseña';
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = 'Las contraseñas no coinciden';
    }
    
    // Términos
    if (!acceptTerms) {
      errors.terms = 'Debes aceptar los términos y condiciones';
    }
    
    if (!acceptPrivacy) {
      errors.privacy = 'Debes aceptar la política de privacidad';
    }
    
    // Captcha
    if (!captchaVerified) {
      errors.captcha = 'Por favor verifica que no eres un robot';
    }
    
    return Object.keys(errors).length === 0;
  }
  
  // Manejo de envío
  async function handleRegister(e) {
    e.preventDefault();
    
    if (!validateForm()) {
      // Scroll al primer error
      const firstError = document.querySelector('.error');
      if (firstError) {
        firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      return;
    }
    
    isSubmitting = true;
    
    // Simular llamada al backend
    setTimeout(() => {
      isSubmitting = false;
      registrationSuccess = true;
      
      console.log('Registro exitoso:', {
        ...formData,
        password: '***',
        confirmPassword: '***',
        captchaVerified,
        acceptTerms,
        acceptPrivacy,
        acceptMarketing
      });
      
      // Redirigir al login después de 2 segundos
      setTimeout(() => {
        navigateTo('login');
      }, 2000);
    }, 2000);
  }
  
  // Formatear número de teléfono mientras se escribe
  function formatPhoneNumber(value) {
    // Remover todo excepto números y el símbolo +
    const cleaned = value.replace(/[^\d+]/g, '');
    formData.phone = cleaned;
  }
  
  // Obtener label del documento
  function getDocumentLabel() {
    switch(formData.documentType) {
      case 'passport': return 'Número de Pasaporte';
      case 'dni': return 'DPI / Cédula';
      case 'license': return 'Licencia de Conducir';
      default: return 'Número de Documento';
    }
  }
  
  function getDocumentPlaceholder() {
    switch(formData.documentType) {
      case 'passport': return 'AB123456';
      case 'dni': return '12345678A';
      case 'license': return 'DL-12345678';
      default: return 'Ingresa tu número';
    }
  }
</script>

<div class="register-page">
  <div class="register-container">
    <div class="register-card">
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
         <img 
  src="{logo}" 
  alt="Miku Inn Logo"
  class="logo-image"
 />
          
        </div>
        <h2 class="title">Crear tu Cuenta</h2>
        <p class="subtitle">Únete a nuestra comunidad y comienza a reservar experiencias inolvidables</p>
      </div>

      {#if registrationSuccess}
        <!-- Success Message -->
        <div class="success-message">
          <div class="success-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
              <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
          </div>
          <h3>¡Cuenta Creada Exitosamente!</h3>
          <p>Te estamos redirigiendo al inicio de sesión...</p>
          <div class="loading-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      {:else}
        <!-- Registration Form -->
        <form on:submit={handleRegister} class="register-form">
          <!-- Sección: Información Personal -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              Información Personal
            </h3>
            
            <div class="form-grid">
              <!-- Nombre -->
              <div class="form-field">
                <label for="firstName">
                  Nombre <span class="required">*</span>
                </label>
                <input
                  type="text"
                  id="firstName"
                  bind:value={formData.firstName}
                  placeholder="Nombres"
                  class:error={errors.firstName}
                  autocomplete="given-name"
                />
                {#if errors.firstName}
                  <span class="error-text">{errors.firstName}</span>
                {/if}
              </div>

              <!-- Apellidos -->
              <div class="form-field">
                <label for="lastName">
                  Apellidos <span class="required">*</span>
                </label>
                <input
                  type="text"
                  id="lastName"
                  bind:value={formData.lastName}
                  placeholder="Apellidos"
                  class:error={errors.lastName}
                  autocomplete="family-name"
                />
                {#if errors.lastName}
                  <span class="error-text">{errors.lastName}</span>
                {/if}
              </div>
            </div>

            <div class="form-grid">
              <!-- Fecha de nacimiento -->
              <div class="form-field">
                <label for="birthDate">
                  Fecha de Nacimiento <span class="required">*</span>
                </label>
                <input
                  type="date"
                  id="birthDate"
                  bind:value={formData.birthDate}
                  max={new Date(new Date().setFullYear(new Date().getFullYear() - 18)).toISOString().split('T')[0]}
                  class:error={errors.birthDate}
                />
                {#if formData.birthDate && userAge >= 18}
                  <span class="helper-text success">✓ {userAge} años</span>
                {:else if formData.birthDate && userAge < 18}
                  <span class="helper-text error">✗ Debes tener al menos 18 años</span>
                {/if}
                {#if errors.birthDate}
                  <span class="error-text">{errors.birthDate}</span>
                {/if}
              </div>

              <!-- Teléfono -->
              <div class="form-field">
                <label for="phone">
                  Teléfono <span class="required">*</span>
                </label>
                <input
                  type="tel"
                  id="phone"
                  value={formData.phone}
                  on:input={(e) => formatPhoneNumber(e.target.value)}
                  placeholder="+502 1234 5678"
                  class:error={errors.phone}
                  autocomplete="tel"
                />
                {#if errors.phone}
                  <span class="error-text">{errors.phone}</span>
                {/if}
              </div>
            </div>

            <div class="form-grid">
              <!-- País -->
              <div class="form-field">
                <label for="country">
                  País <span class="required">*</span>
                </label>
                <div class="select-wrapper">
                  <select 
                    id="country" 
                    bind:value={formData.country}
                    class:error={errors.country}
                    autocomplete="country"
                  >
                    <option value="">Selecciona tu país</option>
                    {#each countries as country}
                      <option value={country.code}>
                        {country.flag} {country.name}
                      </option>
                    {/each}
                  </select>
                  <svg class="select-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </div>
                {#if errors.country}
                  <span class="error-text">{errors.country}</span>
                {/if}
              </div>

              <!-- Ciudad -->
              <div class="form-field">
                <label for="city">
                  Ciudad <span class="required">*</span>
                </label>
                <input
                  type="text"
                  id="city"
                  bind:value={formData.city}
                  placeholder="Ciudad"
                  class:error={errors.city}
                  autocomplete="address-level2"
                />
                {#if errors.city}
                  <span class="error-text">{errors.city}</span>
                {/if}
              </div>
            </div>

            <!-- Dirección -->
            <div class="form-field">
              <label for="address">
                Dirección <span class="required">*</span>
              </label>
              <input
                type="text"
                id="address"
                bind:value={formData.address}
                placeholder="Calle, número, colonia..."
                class:error={errors.address}
                autocomplete="street-address"
              />
              {#if errors.address}
                <span class="error-text">{errors.address}</span>
              {/if}
            </div>
          </div>

          <!-- Sección: Documento de Identidad -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="16" rx="2" ry="2"></rect>
                <line x1="7" y1="12" x2="17" y2="12"></line>
                <line x1="7" y1="16" x2="12" y2="16"></line>
              </svg>
              Documento de Identidad
            </h3>

            <div class="form-grid">
              <!-- Tipo de documento -->
              <div class="form-field">
                <label for="documentType">
                  Tipo de Documento <span class="required">*</span>
                </label>
                <div class="document-type-selector">
                  <label class="radio-card" class:selected={formData.documentType === 'passport'}>
                    <input 
                      type="radio" 
                      name="documentType" 
                      value="passport"
                      bind:group={formData.documentType}
                    />
                    <span class="radio-icon">🛂</span>
                    <span class="radio-label">Pasaporte</span>
                  </label>
                  
                  <label class="radio-card" class:selected={formData.documentType === 'dni'}>
                    <input 
                      type="radio" 
                      name="documentType" 
                      value="dni"
                      bind:group={formData.documentType}
                    />
                    <span class="radio-icon">🪪</span>
                    <span class="radio-label">Documento de Identidficación</span>
                  </label>
                  
                 
                </div>
              </div>

              <!-- Número de documento -->
              <div class="form-field">
                <label for="documentNumber">
                  {getDocumentLabel()} <span class="required">*</span>
                </label>
                <input
                  type="text"
                  id="documentNumber"
                  bind:value={formData.documentNumber}
                  placeholder={getDocumentPlaceholder()}
                  class:error={errors.documentNumber}
                />
                {#if errors.documentNumber}
                  <span class="error-text">{errors.documentNumber}</span>
                {/if}
              </div>
            </div>
          </div>

          <!-- Sección: Credenciales -->
          <div class="form-section">
            <h3 class="section-title">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              Credenciales de Acceso
            </h3>

            <!-- Email -->
            <div class="form-field">
              <label for="email">
                Correo Electrónico <span class="required">*</span>
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

            <!-- Contraseña -->
            <div class="form-field">
              <label for="password">
                Contraseña <span class="required">*</span>
              </label>
              <div class="password-field">
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  bind:value={formData.password}
                  placeholder="Mínimo 8 caracteres"
                  class:error={errors.password}
                  autocomplete="new-password"
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

              <!-- Password Strength Indicator -->
              {#if formData.password && passwordStrength}
                <div class="strength-indicator">
                  <div class="strength-bar">
                    <div 
                      class="strength-fill" 
                      style="width: {passwordStrength.width}; background: {passwordStrength.color}">
                    </div>
                  </div>
                  <span class="strength-text" style="color: {passwordStrength.color}">
                    {passwordStrength.text}
                  </span>
                </div>
              {/if}

              <!-- Password Requirements -->
              <div class="requirements">
                <div class="req" class:met={passwordValidation.minLength}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  Mínimo 8 caracteres
                </div>
                <div class="req" class:met={passwordValidation.hasUpperCase}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  Una mayúscula
                </div>
                <div class="req" class:met={passwordValidation.hasLowerCase}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  Una minúscula
                </div>
                <div class="req" class:met={passwordValidation.hasNumber}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  Un número
                </div>
              </div>

              {#if errors.password}
                <span class="error-text">{errors.password}</span>
              {/if}
            </div>

            <!-- Confirmar Contraseña -->
            <div class="form-field">
              <label for="confirmPassword">
                Confirmar Contraseña <span class="required">*</span>
              </label>
              <div class="password-field">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  id="confirmPassword"
                  bind:value={formData.confirmPassword}
                  placeholder="Repite tu contraseña"
                  class:error={errors.confirmPassword}
                  autocomplete="new-password"
                />
                <button
                  type="button"
                  class="toggle-btn"
                  on:click={() => showConfirmPassword = !showConfirmPassword}
                  tabindex="-1"
                >
                  {#if showConfirmPassword}
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
              
              {#if formData.confirmPassword && formData.password === formData.confirmPassword}
                <span class="helper-text success">✓ Las contraseñas coinciden</span>
              {:else if formData.confirmPassword}
                <span class="helper-text error">✗ Las contraseñas no coinciden</span>
              {/if}
              
              {#if errors.confirmPassword}
                <span class="error-text">{errors.confirmPassword}</span>
              {/if}
            </div>
          </div>

          <!-- Captcha -->
          <div class="captcha-container">
            <div class="captcha-box" class:verified={captchaVerified} class:error={captchaError}>
              <label class="captcha-label" on:click={handleCaptchaClick}>
                <div class="captcha-checkbox">
                  {#if captchaLoading}
                    <div class="captcha-spinner"></div>
                  {:else if captchaVerified}
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  {:else}
                    <!-- Empty checkbox -->
                  {/if}
                </div>
                <span class="captcha-text">No soy un robot</span>
              </label>
              
              <div class="captcha-logo">
                <div class="recaptcha-badge">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#667eea"/>
                    <path d="M2 17L12 22L22 17" stroke="#667eea" stroke-width="2"/>
                    <path d="M2 12L12 17L22 12" stroke="#667eea" stroke-width="2"/>
                  </svg>
                  <div class="recaptcha-text">
                    <span>reCAPTCHA</span>
                    <div class="recaptcha-links">
                      <a href="#">Privacidad</a>
                      <span>-</span>
                      <a href="#">Términos</a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            {#if errors.captcha}
              <span class="error-text">{errors.captcha}</span>
            {/if}
            {#if captchaError}
              <div class="captcha-error-message">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="8" x2="12" y2="12"></line>
                  <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                Error en la verificación. Por favor intenta de nuevo.
                <button type="button" class="retry-captcha" on:click={resetCaptcha}>Reintentar</button>
              </div>
            {/if}
          </div>

          <!-- Términos y Condiciones -->
          <div class="terms-section">
            <label class="checkbox-label" class:error={errors.terms}>
              <input 
                type="checkbox" 
                bind:checked={acceptTerms}
              />
              <span class="checkbox-custom"></span>
              <span class="checkbox-text">
                Acepto los <button type="button" class="link-btn">Términos y Condiciones</button> <span class="required">*</span>
              </span>
            </label>
            {#if errors.terms}
              <span class="error-text">{errors.terms}</span>
            {/if}

            <label class="checkbox-label" class:error={errors.privacy}>
              <input 
                type="checkbox" 
                bind:checked={acceptPrivacy}
              />
              <span class="checkbox-custom"></span>
              <span class="checkbox-text">
                Acepto la <button type="button" class="link-btn">Política de Privacidad</button> <span class="required">*</span>
              </span>
            </label>
            {#if errors.privacy}
              <span class="error-text">{errors.privacy}</span>
            {/if}

            <label class="checkbox-label">
              <input 
                type="checkbox" 
                bind:checked={acceptMarketing}
              />
              <span class="checkbox-custom"></span>
              <span class="checkbox-text">
                Deseo recibir ofertas y promociones por email (opcional)
              </span>
            </label>
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
              Creando cuenta...
            {:else}
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                <polyline points="10 17 15 12 10 7"></polyline>
                <line x1="15" y1="12" x2="3" y2="12"></line>
              </svg>
              Crear Cuenta
            {/if}
          </button>

          <!-- Login Link -->
          <div class="footer-text">
            ¿Ya tienes una cuenta? 
            <button type="button" class="link-btn" on:click={() => navigateTo('login')}>
              Inicia sesión aquí
            </button>
          </div>
        </form>
      {/if}
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
  .register-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
    padding: 2rem 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .register-container {
    width: 100%;
    max-width: 700px;
  }
  
  .register-card {
    background: rgba(30, 41, 59, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    padding: 3rem 2.5rem;
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
    margin-bottom: 2.5rem;
  }
  
  .logo-section {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }
  
 .logo-circle {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.logo-image {
  width: 300px;      
  max-width: 100%;
  height: auto;
  object-fit: contain;
  margin-bottom: 1.5rem;
  filter: drop-shadow(0 10px 25px rgba(0, 0, 0, 0.35));
}

  
  .logo-icon {
    font-size: 2rem;
  }
  
  .logo-text {
    font-size: 2rem;
    font-weight: 700;
    background: linear-gradient(135deg, var(--primary), var(--secondary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0;
  }
  
  .title {
    font-size: 1.875rem;
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
    padding: 3rem 2rem;
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
  .register-form {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }
  
  /* Form Sections */
  .form-section {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1.125rem;
    font-weight: 700;
    color: #f8fafc;
    margin: 0 0 0.5rem 0;
    padding-bottom: 0.75rem;
    border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  }
  
  .section-title svg {
    color: var(--primary);
  }
  
  /* Form Grid */
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.25rem;
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
  
  .required {
    color: var(--danger);
  }
  
  .form-field input,
  .form-field select {
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
  
  .form-field input:focus,
  .form-field select:focus {
    outline: none;
    border-color: var(--primary);
    background: rgba(15, 23, 42, 0.8);
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
  }
  
  .form-field input.error,
  .form-field select.error {
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
  
  /* Select wrapper */
  .select-wrapper {
    position: relative;
  }
  
  .select-wrapper select {
    appearance: none;
    cursor: pointer;
    padding-right: 3rem;
  }
  
  .select-icon {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: #64748b;
    pointer-events: none;
  }
  
  .form-field select option {
    background: #1e293b;
    color: #f8fafc;
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
  
  /* Password strength */
  .strength-indicator {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .strength-bar {
    flex: 1;
    height: 4px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2px;
    overflow: hidden;
  }
  
  .strength-fill {
    height: 100%;
    transition: all 0.3s;
  }
  
  .strength-text {
    font-size: 0.8rem;
    font-weight: 600;
    min-width: 80px;
    text-align: right;
  }
  
  /* Requirements */
  .requirements {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.5rem;
  }
  
  .req {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    font-size: 0.8rem;
    color: #64748b;
  }
  
  .req svg {
    flex-shrink: 0;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  .req.met {
    color: var(--success);
  }
  
  .req.met svg {
    opacity: 1;
  }
  
  /* Helper text */
  .helper-text {
    font-size: 0.8rem;
    font-weight: 500;
  }
  
  .helper-text.success {
    color: var(--success);
  }
  
  .helper-text.error {
    color: var(--danger);
  }
  
  /* Error text */
  .error-text {
    display: block;
    color: var(--danger);
    font-size: 0.8rem;
    font-weight: 500;
  }
  
  /* Document Type Selector */
  .document-type-selector {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 0.75rem;
    grid-column: 1 / -1;
  }
  
  .radio-card {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 1rem;
    border: 2px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    background: rgba(15, 23, 42, 0.5);
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .radio-card input {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
  }
  
  .radio-card:hover {
    border-color: var(--primary);
    background: rgba(102, 126, 234, 0.05);
  }
  
  .radio-card.selected {
    border-color: var(--primary);
    background: rgba(102, 126, 234, 0.1);
  }
  
  .radio-icon {
    font-size: 2rem;
  }
  
  .radio-label {
    font-size: 0.85rem;
    font-weight: 600;
    color: #e2e8f0;
  }
  
  /* Captcha */
  .captcha-container {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .captcha-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.25rem;
    background: rgba(15, 23, 42, 0.5);
    border: 2px solid rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    transition: all 0.3s;
  }
  
  .captcha-box:hover {
    border-color: rgba(255, 255, 255, 0.2);
  }
  
  .captcha-box.verified {
    border-color: var(--success);
    background: rgba(16, 185, 129, 0.05);
  }
  
  .captcha-box.error {
    border-color: var(--danger);
    background: rgba(239, 68, 68, 0.05);
    animation: shake 0.5s;
  }
  
  @keyframes shake {
    0%, 100% { transform: translateX(0); }
    10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
    20%, 40%, 60%, 80% { transform: translateX(5px); }
  }
  
  .captcha-label {
    display: flex;
    align-items: center;
    gap: 0.875rem;
    cursor: pointer;
  }
  
  .captcha-checkbox {
    width: 32px;
    height: 32px;
    border: 2px solid rgba(255, 255, 255, 0.2);
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.05);
    transition: all 0.2s;
  }
  
  .captcha-box.verified .captcha-checkbox {
    background: var(--success);
    border-color: var(--success);
  }
  
  .captcha-checkbox svg {
    color: white;
  }
  
  .captcha-spinner {
    width: 20px;
    height: 20px;
    border: 3px solid rgba(255, 255, 255, 0.2);
    border-top-color: var(--primary);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  
  .captcha-text {
    font-size: 0.95rem;
    font-weight: 500;
    color: #e2e8f0;
  }
  
  .captcha-logo {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.25rem;
  }
  
  .recaptcha-badge {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .recaptcha-text {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .recaptcha-text span {
    font-size: 0.65rem;
    font-weight: 600;
    color: #64748b;
  }
  
  .recaptcha-links {
    display: flex;
    gap: 0.25rem;
    font-size: 0.55rem;
    color: #475569;
  }
  
  .recaptcha-links a {
    color: #475569;
    text-decoration: none;
  }
  
  .recaptcha-links a:hover {
    text-decoration: underline;
  }
  
  .captcha-error-message {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid var(--danger);
    border-radius: 8px;
    color: var(--danger);
    font-size: 0.85rem;
  }
  
  .retry-captcha {
    margin-left: auto;
    padding: 0.375rem 0.875rem;
    background: var(--danger);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 0.8rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .retry-captcha:hover {
    background: #dc2626;
  }
  
  /* Terms */
  .terms-section {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .checkbox-label {
    display: flex;
    align-items: start;
    gap: 0.75rem;
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
    margin-top: 0.15rem;
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
    line-height: 1.5;
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
    margin-top: 1rem;
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
  
  /* Footer */
  .footer-text {
    text-align: center;
    font-size: 0.9rem;
    color: #94a3b8;
    margin-top: 1rem;
  }
  
  /* Responsive */
  @media (max-width: 640px) {
    .register-page {
      padding: 1rem;
    }
    
    .register-card {
      padding: 2rem 1.5rem;
    }
    
    .logo-section {
      flex-direction: column;
    }
    
    .title {
      font-size: 1.5rem;
    }
    
    .form-grid {
      grid-template-columns: 1fr;
    }
    
    .document-type-selector {
      grid-template-columns: 1fr;
    }
    
    .requirements {
      grid-template-columns: 1fr;
    }
    
    .captcha-box {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }
    
    .recaptcha-badge {
      align-self: flex-end;
    }
  }
</style>