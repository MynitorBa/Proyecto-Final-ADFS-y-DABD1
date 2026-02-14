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
    edad: '',
    numeroEmergencia: '',
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

  onMount(async () => {
    try {
      const response = await fetch('http://localhost:5190/api/nacionalidades');
      if (!response.ok) throw new Error('Error al cargar nacionalidades');
      nacionalidades = await response.json();
    } catch (error) {
      errorNacionalidades = 'No se pudieron cargar los países. Intenta de nuevo.';
      console.error('Error cargando nacionalidades:', error);
    } finally {
      loadingNacionalidades = false;
    }
  });

  async function handleRegister() {
    submitError = '';

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
      correo: registerData.correo,
      contrasena: registerData.contrasena,
      pasaporte: registerData.pasaporte,
      username: registerData.username,
      nombre: registerData.nombre,
      apellido: registerData.apellido,
      edad: parseInt(registerData.edad),
      numeroEmergencia: registerData.numeroEmergencia,
      nacionalidadId: parseInt(registerData.nacionalidadId)
    };

    try {
      const response = await fetch('http://localhost:5190/api/usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const err = await response.text();
        throw new Error(err || 'Error al crear usuario');
      }

      submitSuccess = true;
      setTimeout(() => navigateTo('login'), 2000);

    } catch (error) {
      submitError = 'No se pudo crear la cuenta. Verifica tus datos e intenta de nuevo.';
      console.error('Error al registrar:', error);
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

              <!-- Username y Edad -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="username" class="register-form__label">Username</label>
                  <input type="text" id="username" class="register-form__input"
                    bind:value={registerData.username} placeholder="usuario123" required />
                </div>
                <div class="register-form__field">
                  <label for="edad" class="register-form__label">Edad</label>
                  <input type="number" id="edad" class="register-form__input"
                    bind:value={registerData.edad} placeholder="25" min="1" max="120" required />
                </div>
              </div>

              <!-- Correo -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
                  <label for="correo" class="register-form__label">Correo electrónico</label>
                  <input type="email" id="correo" class="register-form__input"
                    bind:value={registerData.correo} placeholder="correo@ejemplo.com" required />
                </div>
              </div>

              <!-- Pasaporte y Número de emergencia -->
              <div class="register-form__row">
                <div class="register-form__field">
                  <label for="pasaporte" class="register-form__label">Pasaporte</label>
                  <input type="text" id="pasaporte" class="register-form__input"
                    bind:value={registerData.pasaporte} placeholder="AB123456" required />
                </div>
                <div class="register-form__field">
                  <label for="numeroEmergencia" class="register-form__label">Número de emergencia</label>
                  <input type="tel" id="numeroEmergencia" class="register-form__input"
                    bind:value={registerData.numeroEmergencia} placeholder="50211223344" required />
                </div>
              </div>

              <!-- País (dropdown desde API) -->
              <div class="register-form__row">
                <div class="register-form__field register-form__field--full">
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
                  <input type="password" id="contrasena" class="register-form__input"
                    bind:value={registerData.contrasena} placeholder="Mínimo 8 caracteres" required />
                </div>
                <div class="register-form__field">
                  <label for="confirmPassword" class="register-form__label">Confirmar contraseña</label>
                  <input type="password" id="confirmPassword" class="register-form__input"
                    bind:value={registerData.confirmPassword} placeholder="Repite tu contraseña" required />
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
                {submitting ? 'Creando cuenta...' : 'Crear cuenta'}
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