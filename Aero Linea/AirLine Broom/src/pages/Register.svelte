<script>
  import '../styles/reslog.css';
  export let navigateTo;

  const API_URL = 'https://localhost:7107/api';

  let registerData = {
    nombre: '',
    apellido: '',
    correo: '',
    username: '',
    contrasena: '',
    confirmPassword: '',
    pasaporte: '',
    edad: '',
    numeroEmergencia: '',
    nacionalidadId: ''
  };

  let nacionalidades = [];
  let acceptTerms = false;
  let receivePromotions = false;
  let captchaVerified = false;
  let isLoading = false;
  let errorMsg = '';
  let successMsg = '';

  // Cargar nacionalidades al montar
  import { onMount } from 'svelte';

  onMount(async () => {
    try {
      const res = await fetch(`${API_URL}/nacionalidades`);
      if (res.ok) {
        nacionalidades = await res.json();
      }
    } catch (e) {
      console.error('Error al cargar nacionalidades:', e);
    }
  });

  async function handleRegister() {
    errorMsg = '';
    successMsg = '';

    // Validaciones básicas
    if (!registerData.nombre || !registerData.apellido || !registerData.correo ||
        !registerData.username || !registerData.contrasena || !registerData.pasaporte ||
        !registerData.edad || !registerData.nacionalidadId) {
      errorMsg = 'Por favor completa todos los campos obligatorios.';
      return;
    }

    if (registerData.contrasena !== registerData.confirmPassword) {
      errorMsg = 'Las contraseñas no coinciden.';
      return;
    }

    if (!acceptTerms) {
      errorMsg = 'Debes aceptar los términos y condiciones.';
      return;
    }

    if (!captchaVerified) {
      errorMsg = 'Por favor verifica el captcha.';
      return;
    }

    const payload = {
      correo: registerData.correo,
      contrasena: registerData.contrasena,
      pasaporte: registerData.pasaporte,
      username: registerData.username,
      nombre: registerData.nombre,
      apellido: registerData.apellido,
      edad: parseInt(registerData.edad),
      numeroEmergencia: registerData.numeroEmergencia || '',
      nacionalidadId: parseInt(registerData.nacionalidadId)
    };

    isLoading = true;

    try {
      const res = await fetch(`${API_URL}/usuarios`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        successMsg = '¡Cuenta creada exitosamente! Redirigiendo al login...';
        setTimeout(() => navigateTo('login'), 2000);
      } else {
        const data = await res.json().catch(() => null);
        errorMsg = data?.message || `Error al crear la cuenta (${res.status}). Verifica los datos.`;
      }
    } catch (e) {
      errorMsg = 'No se pudo conectar con el servidor. Verifica que el backend esté activo.';
      console.error(e);
    } finally {
      isLoading = false;
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

          <!-- Mensajes de estado -->
          {#if errorMsg}
            <div class="register-form__alert register-form__alert--error">
              {errorMsg}
            </div>
          {/if}

          {#if successMsg}
            <div class="register-form__alert register-form__alert--success">
              {successMsg}
            </div>
          {/if}

          <form class="register-form" on:submit|preventDefault={handleRegister}>

            <!-- Nombre y Apellido -->
            <div class="register-form__row">
              <div class="register-form__field">
                <label for="nombre" class="register-form__label">Nombre *</label>
                <input
                  type="text"
                  id="nombre"
                  class="register-form__input"
                  bind:value={registerData.nombre}
                  placeholder="Tu nombre"
                />
              </div>

              <div class="register-form__field">
                <label for="apellido" class="register-form__label">Apellido *</label>
                <input
                  type="text"
                  id="apellido"
                  class="register-form__input"
                  bind:value={registerData.apellido}
                  placeholder="Tu apellido"
                />
              </div>
            </div>

            <!-- Correo -->
            <div class="register-form__row">
              <div class="register-form__field register-form__field--full">
                <label for="correo" class="register-form__label">Correo electrónico *</label>
                <input
                  type="email"
                  id="correo"
                  class="register-form__input"
                  bind:value={registerData.correo}
                  placeholder="correo@ejemplo.com"
                />
              </div>
            </div>

            <!-- Username -->
            <div class="register-form__row">
              <div class="register-form__field register-form__field--full">
                <label for="username" class="register-form__label">Nombre de usuario *</label>
                <input
                  type="text"
                  id="username"
                  class="register-form__input"
                  bind:value={registerData.username}
                  placeholder="usuario123"
                />
              </div>
            </div>

            <!-- Pasaporte y Edad -->
            <div class="register-form__row">
              <div class="register-form__field">
                <label for="pasaporte" class="register-form__label">Pasaporte *</label>
                <input
                  type="text"
                  id="pasaporte"
                  class="register-form__input"
                  bind:value={registerData.pasaporte}
                  placeholder="A301255"
                />
              </div>

              <div class="register-form__field">
                <label for="edad" class="register-form__label">Edad *</label>
                <input
                  type="number"
                  id="edad"
                  class="register-form__input"
                  bind:value={registerData.edad}
                  placeholder="21"
                  min="1"
                  max="120"
                />
              </div>
            </div>

            <!-- Número de emergencia y Nacionalidad -->
            <div class="register-form__row">
              <div class="register-form__field">
                <label for="numeroEmergencia" class="register-form__label">Número de emergencia</label>
                <input
                  type="tel"
                  id="numeroEmergencia"
                  class="register-form__input"
                  bind:value={registerData.numeroEmergencia}
                  placeholder="+502 1234-5678"
                />
              </div>

              <div class="register-form__field">
                <label for="nacionalidadId" class="register-form__label">Nacionalidad *</label>
                <select
                  id="nacionalidadId"
                  class="register-form__input"
                  bind:value={registerData.nacionalidadId}
                >
                  <option value="">Selecciona tu país</option>
                  {#each nacionalidades as nac}
                    <option value={nac.id}>{nac.pais}</option>
                  {/each}
                </select>
              </div>
            </div>

            <!-- Contraseñas -->
            <div class="register-form__row">
              <div class="register-form__field">
                <label for="contrasena" class="register-form__label">Contraseña *</label>
                <input
                  type="password"
                  id="contrasena"
                  class="register-form__input"
                  bind:value={registerData.contrasena}
                  placeholder="Mínimo 6 caracteres"
                />
              </div>

              <div class="register-form__field">
                <label for="confirmPassword" class="register-form__label">Confirmar contraseña *</label>
                <input
                  type="password"
                  id="confirmPassword"
                  class="register-form__input"
                  bind:value={registerData.confirmPassword}
                  placeholder="Repite tu contraseña"
                />
              </div>
            </div>

            <div class="register-form__checkboxes">
              <label class="register-form__checkbox">
                <input
                  type="checkbox"
                  bind:checked={acceptTerms}
                  class="register-form__checkbox-input"
                />
                <span class="register-form__checkbox-label">
                  Acepto los términos y condiciones y la política de privacidad
                </span>
              </label>

              <label class="register-form__checkbox">
                <input
                  type="checkbox"
                  bind:checked={receivePromotions}
                  class="register-form__checkbox-input"
                />
                <span class="register-form__checkbox-label">
                  Deseo recibir promociones y ofertas por correo electrónico
                </span>
              </label>
            </div>

            <!-- CAPTCHA -->
            <div class="register-form__captcha">
              <div class="captcha-box">
                <label class="captcha-box__checkbox">
                  <input
                    type="checkbox"
                    bind:checked={captchaVerified}
                    class="captcha-box__input"
                    on:click={handleCaptchaClick}
                  />
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

            <button type="submit" class="register-form__submit" disabled={isLoading}>
              {isLoading ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>
          </form>

          <div class="register__login">
            <p class="register__login-text">
              ¿Ya tienes una cuenta?
              <button
                type="button"
                class="register__login-link"
                on:click={() => navigateTo('login')}
              >
                Inicia sesión
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<style>
  h2 {
    color: white;
  }

  /* Alertas */
  .register-form__alert {
    padding: 0.85rem 1.2rem;
    border-radius: 8px;
    margin-bottom: 1rem;
    font-size: 0.9rem;
    font-weight: 500;
  }

  .register-form__alert--error {
    background-color: #fff0f0;
    color: #c0392b;
    border: 1.5px solid #f1a8a8;
  }

  .register-form__alert--success {
    background-color: #f0fff4;
    color: #27ae60;
    border: 1.5px solid #a8f1c0;
  }

  /* Submit deshabilitado */
  .register-form__submit:disabled {
    opacity: 0.65;
    cursor: not-allowed;
  }

  /* Captcha Styles */
  .register-form__captcha {
    margin: 1.5rem 0;
  }

  .captcha-box {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem 1.2rem;
    border: 1.5px solid #d3d3d3;
    border-radius: 8px 0 8px 0;
    background-color: #fafafa;
    transition: all 0.3s ease;
  }

  .captcha-box:hover {
    border-color: var(--primary-color);
    background-color: #fff;
  }

  .captcha-box__checkbox {
    display: flex;
    align-items: center;
    gap: 0.8rem;
    cursor: pointer;
  }

  .captcha-box__input {
    width: 24px;
    height: 24px;
    cursor: pointer;
    accent-color: var(--primary-color);
  }

  .captcha-box__label {
    font-size: 0.95rem;
    color: var(--secondary-color);
    font-weight: 500;
    user-select: none;
  }

  .captcha-box__logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.3rem;
  }

  .captcha-box__text {
    font-size: 0.65rem;
    color: #888;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  @media (max-width: 768px) {
    .captcha-box {
      flex-direction: column;
      gap: 1rem;
      align-items: flex-start;
    }

    .captcha-box__logo {
      align-self: center;
    }
  }
</style>