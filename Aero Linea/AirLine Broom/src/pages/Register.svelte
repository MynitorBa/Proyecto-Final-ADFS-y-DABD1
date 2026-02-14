<script>
  import '../styles/reslog.css';
  import { onMount } from 'svelte';
  export let navigateTo;

  let registerData = {
    correo: '',
    contrasena: '',
    confirmPassword: '',
    pasaporte: '',
    username: '',
    nombre: '',
    apellido: '',
    telefono: '',
    fechaNacimiento: '',
    ciudad: '',
    nacionalidadId: ''
  };

  let acceptTerms = false;
  let receivePromotions = false;
  let captchaVerified = false;

  let nacionalidades = [];
  let loadingNacionalidades = true;
  let errorNacionalidades = '';
  let submitError = '';
  let submitSuccess = false;
  let submitting = false;

  let errores = {
    correo: '',
    username: '',
    pasaporte: '',
    contrasena: ''
  };

  $: passwordStrength = {
    length:    registerData.contrasena.length >= 8,
    uppercase: /[A-Z]/.test(registerData.contrasena),
    number:    /[0-9]/.test(registerData.contrasena)
  };

  $: passwordValid = passwordStrength.length && passwordStrength.uppercase && passwordStrength.number;

  onMount(async () => {
    try {
      const response = await fetch('http://localhost:5190/api/nacionalidades');
      if (!response.ok) throw new Error('Error al cargar nacionalidades');
      nacionalidades = await response.json();
    } catch (error) {
      errorNacionalidades = 'No se pudieron cargar los países. Intenta de nuevo.';
    } finally {
      loadingNacionalidades = false;
    }
  });

  async function handleRegister() {
    submitError = '';
    errores = { correo: '', username: '', pasaporte: '', contrasena: '' };

    if (!passwordValid) {
      errores.contrasena = 'La contraseña debe tener mínimo 8 caracteres, 1 mayúscula y 1 número.';
      return;
    }
    if (registerData.contrasena !== registerData.confirmPassword) {
      submitError = 'Las contraseñas no coinciden.';
      return;
    }
    if (!acceptTerms) {
      submitError = 'Debes aceptar los términos y condiciones.';
      return;
    }
    if (!captchaVerified) {
      submitError = 'Por favor confirma que no eres un robot.';
      return;
    }
    if (!registerData.nacionalidadId) {
      submitError = 'Selecciona tu país de nacionalidad.';
      return;
    }

    submitting = true;

    const payload = {
      correo:          registerData.correo,
      contrasena:      registerData.contrasena,
      pasaporte:       registerData.pasaporte,
      username:        registerData.username,
      nombre:          registerData.nombre,
      apellido:        registerData.apellido,
      telefono:        registerData.telefono,
      fechaNacimiento: registerData.fechaNacimiento,
      ciudad:          registerData.ciudad,
      nacionalidadId:  parseInt(registerData.nacionalidadId)
    };

    try {
      const verificar = await fetch('http://localhost:5190/api/usuarios/verificar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const constraints = await verificar.json();

      if (constraints.correoExiste)   errores.correo   = 'Este correo ya está registrado.';
      if (constraints.usernameExiste) errores.username = 'Este username ya está en uso.';
      if (constraints.pasaporteExiste) errores.pasaporte = 'Este pasaporte ya está registrado.';

      if (constraints.correoExiste || constraints.usernameExiste || constraints.pasaporteExiste) {
        submitting = false;
        return;
      }

      const response = await fetch('http://localhost:5190/api/usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) throw new Error('Error al crear usuario');

      submitSuccess = true;
      setTimeout(() => navigateTo('login'), 2000);

    } catch (error) {
      submitError = 'No se pudo crear la cuenta. Verifica tus datos e intenta de nuevo.';
    } finally {
      submitting = false;
    }
  }

  function handleCaptchaClick() {
    captchaVerified = !captchaVerified;
  }
</script>

<div class="register">
  <div class="register__container">
    <div class="register__content">
      <div class="register__image-section">
        <div class="register__image-overlay">
          <h2 class="register__image-title">Unete a Broom AirLine</h2>
          <p class="register__image-subtitle">
            Crea tu cuenta y empieza a disfrutar de vuelos increibles con ofertas exclusivas
          </p>
          <ul class="register__benefits">
            <li class="register__benefit">Acumula puntos en cada vuelo</li>
            <li class="register__benefit">Acceso a promociones exclusivas</li>
            <li class="register__benefit">Gestion facil de tus reservas</li>
            <li class="register__benefit">Soporte prioritario 24/7</li>
          </ul>
        </div>
      </div>

      <div class="register__form-section">
        <div class="register__form-container">
          <button class="register__back" on:click={() => navigateTo('home')}>
            Volver al inicio
          </button>

          <div class="register__header">
            <h1 class="register__title">Crear cuenta</h1>
            <p class="register__subtitle">Completa tus datos para registrarte</p>
          </div>

          {#if submitSuccess}
            <div class="register-form__success">
              ¡Cuenta creada exitosamente! Redirigiendo al login...
            </div>
          {:else}
            <form class="register-form" on:submit|preventDefault={handleRegister}>

              <!-- Nombre y Apellido -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="nombre" class="register-form__label">Nombre</label>
                  <input type="text" id="nombre" class="register-form__input"
                    bind:value={registerData.nombre} placeholder="Tu nombre" required />
                </div>
                <div class="register-form__field">
                  <label for="apellido" class="register-form__label">Apellido</label>
                  <input type="text" id="apellido" class="register-form__input"
                    bind:value={registerData.apellido} placeholder="Tu apellido" required />
                </div>
              </div>

              <!-- Username y Teléfono -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="username" class="register-form__label">Username</label>
                  <input type="text" id="username"
                    class="register-form__input {errores.username ? 'register-form__input--error' : ''}"
                    bind:value={registerData.username} placeholder="usuario123" required />
                  {#if errores.username}
                    <span class="register-form__field-error">{errores.username}</span>
                  {/if}
                </div>
                <div class="register-form__field">
                  <label for="telefono" class="register-form__label">Teléfono</label>
                  <input type="tel" id="telefono" class="register-form__input"
                    bind:value={registerData.telefono} placeholder="50211223344" required />
                </div>
              </div>

              <!-- Correo -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="correo" class="register-form__label">Correo electrónico</label>
                  <input type="email" id="correo"
                    class="register-form__input {errores.correo ? 'register-form__input--error' : ''}"
                    bind:value={registerData.correo} placeholder="correo@ejemplo.com" required />
                  {#if errores.correo}
                    <span class="register-form__field-error">{errores.correo}</span>
                  {/if}
                </div>
              </div>

              <!-- Fecha de nacimiento y Ciudad -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="fechaNacimiento" class="register-form__label">Fecha de nacimiento</label>
                  <input type="date" id="fechaNacimiento" class="register-form__input"
                    bind:value={registerData.fechaNacimiento} required />
                </div>
                <div class="register-form__field">
                  <label for="ciudad" class="register-form__label">Ciudad</label>
                  <input type="text" id="ciudad" class="register-form__input"
                    bind:value={registerData.ciudad} placeholder="Tu ciudad" required />
                </div>
              </div>

              <!-- Pasaporte y País -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="pasaporte" class="register-form__label">Pasaporte</label>
                  <input type="text" id="pasaporte"
                    class="register-form__input {errores.pasaporte ? 'register-form__input--error' : ''}"
                    bind:value={registerData.pasaporte} placeholder="AB123456" required />
                  {#if errores.pasaporte}
                    <span class="register-form__field-error">{errores.pasaporte}</span>
                  {/if}
                </div>
                <div class="register-form__field">
                  <label for="nacionalidadId" class="register-form__label">País</label>
                  {#if loadingNacionalidades}
                    <select id="nacionalidadId" class="register-form__input register-form__select" disabled>
                      <option>Cargando países...</option>
                    </select>
                  {:else if errorNacionalidades}
                    <select id="nacionalidadId" class="register-form__input register-form__select register-form__select--error" disabled>
                      <option>{errorNacionalidades}</option>
                    </select>
                  {:else}
                    <select id="nacionalidadId" class="register-form__input register-form__select"
                      bind:value={registerData.nacionalidadId} required>
                      <option value="" disabled selected>Selecciona tu país</option>
                      {#each nacionalidades as nac}
                        <option value={nac.id}>{nac.pais}</option>
                      {/each}
                    </select>
                  {/if}
                </div>
              </div>

              <!-- Contraseñas -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="contrasena" class="register-form__label">Contraseña</label>
                  <input type="password" id="contrasena"
                    class="register-form__input {errores.contrasena ? 'register-form__input--error' : ''}"
                    bind:value={registerData.contrasena} placeholder="Mínimo 8 caracteres" required />
                  {#if registerData.contrasena.length > 0}
                    <div class="password-strength">
                      <span class="password-strength__item" class:ok={passwordStrength.length}>
                        {passwordStrength.length ? '✓' : '✗'} 8 caracteres mínimo
                      </span>
                      <span class="password-strength__item" class:ok={passwordStrength.uppercase}>
                        {passwordStrength.uppercase ? '✓' : '✗'} 1 letra mayúscula
                      </span>
                      <span class="password-strength__item" class:ok={passwordStrength.number}>
                        {passwordStrength.number ? '✓' : '✗'} 1 número
                      </span>
                    </div>
                  {/if}
                  {#if errores.contrasena}
                    <span class="register-form__field-error">{errores.contrasena}</span>
                  {/if}
                </div>
                <div class="register-form__field">
                  <label for="confirmPassword" class="register-form__label">Confirmar contraseña</label>
                  <input type="password" id="confirmPassword" class="register-form__input"
                    bind:value={registerData.confirmPassword} placeholder="Repite tu contraseña" required />
                  {#if registerData.confirmPassword.length > 0 && registerData.contrasena !== registerData.confirmPassword}
                    <span class="register-form__field-error">Las contraseñas no coinciden.</span>
                  {/if}
                </div>
              </div>

              <!-- Checkboxes -->
              <div class="register-form__checkboxes">
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={acceptTerms} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">
                    Acepto los términos y condiciones y la política de privacidad
                  </span>
                </label>
                <label class="register-form__checkbox">
                  <input type="checkbox" bind:checked={receivePromotions} class="register-form__checkbox-input" />
                  <span class="register-form__checkbox-label">
                    Deseo recibir promociones y ofertas por correo electrónico
                  </span>
                </label>
              </div>

              <!-- CAPTCHA -->
              <div class="register-form__captcha">
                <div class="captcha-box">
                  <label class="captcha-box__checkbox">
                    <input type="checkbox" bind:checked={captchaVerified}
                      class="captcha-box__input" on:click={handleCaptchaClick} />
                    <span class="captcha-box__label">No soy un robot</span>
                  </label>
                  <div class="captcha-box__logo">
                    <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
                      <rect width="40" height="40" rx="4" fill="#f1f1f1"/>
                      <circle cx="20" cy="20" r="8" stroke="#4285F4" stroke-width="2"/>
                      <path d="M20 12V20L24 24" stroke="#4285F4" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <span class="captcha-box__text">reCAPTCHA</span>
                  </div>
                </div>
              </div>

              {#if submitError}
                <div class="register-form__error">{submitError}</div>
              {/if}

              <button type="submit" class="register-form__submit" disabled={submitting}>
                {submitting ? 'Verificando...' : 'Crear cuenta'}
              </button>

            </form>
          {/if}

          <div class="register__login">
            <p class="register__login-text">
              ¿Ya tienes una cuenta?
              <button type="button" class="register__login-link" on:click={() => navigateTo('login')}>
                Inicia sesión
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>