<script>
  import '../styles/reslog.css';
  export let navigateTo;

  let registerData = {
    nombre: '',
    apellido: '',
    email: '',
    telefono: '',
    password: '',
    confirmPassword: '',
    fechaNacimiento: '',
    pais: '',
    ciudad: ''
  };

  let acceptTerms = false;
  let receivePromotions = false;
  let captchaVerified = false;

  function handleRegister() {
    console.log('Registrando usuario:', registerData);
    // Aquí irá la lógica de registro con backend
    navigateTo('login');
  }

  function handleCaptchaClick() {
    // Simulación de captcha - solo visual
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

          <form class="register-form" on:submit|preventDefault={handleRegister}>
            <div class="register-form__row">
              <div class="register-form__field">
                <label for="nombre" class="register-form__label">Nombre</label>
                <input 
                  type="text" 
                  id="nombre"
                  class="register-form__input"
                  bind:value={registerData.nombre}
                  placeholder="Tu nombre"
                />
              </div>

              <div class="register-form__field">
                <label for="apellido" class="register-form__label">Apellido</label>
                <input 
                  type="text" 
                  id="apellido"
                  class="register-form__input"
                  bind:value={registerData.apellido}
                  placeholder="Tu apellido"
                />
              </div>
            </div>

            <div class="register-form__row">
              <div class="register-form__field register-form__field--full">
                <label for="email" class="register-form__label">Email</label>
                <input 
                  type="email" 
                  id="email"
                  class="register-form__input"
                  bind:value={registerData.email}
                  placeholder="correo@ejemplo.com"
                />
              </div>
            </div>

            <div class="register-form__row">
              <div class="register-form__field">
                <label for="telefono" class="register-form__label">Telefono</label>
                <input 
                  type="tel" 
                  id="telefono"
                  class="register-form__input"
                  bind:value={registerData.telefono}
                  placeholder="+502 1234-5678"
                />
              </div>

              <div class="register-form__field">
                <label for="fechaNacimiento" class="register-form__label">Fecha de nacimiento</label>
                <input 
                  type="date" 
                  id="fechaNacimiento"
                  class="register-form__input"
                  bind:value={registerData.fechaNacimiento}
                />
              </div>
            </div>

            <div class="register-form__row">
              <div class="register-form__field">
                <label for="pais" class="register-form__label">Pais</label>
                <input 
                  type="text" 
                  id="pais"
                  class="register-form__input"
                  bind:value={registerData.pais}
                  placeholder="Guatemala"
                />
              </div>

              <div class="register-form__field">
                <label for="ciudad" class="register-form__label">Ciudad</label>
                <input 
                  type="text" 
                  id="ciudad"
                  class="register-form__input"
                  bind:value={registerData.ciudad}
                  placeholder="Ciudad de Guatemala"
                />
              </div>
            </div>

            <div class="register-form__row">
              <div class="register-form__field">
                <label for="password" class="register-form__label">Contraseña</label>
                <input 
                  type="password" 
                  id="password"
                  class="register-form__input"
                  bind:value={registerData.password}
                  placeholder="Minimo 8 caracteres"
                />
              </div>

              <div class="register-form__field">
                <label for="confirmPassword" class="register-form__label">Confirmar contraseña</label>
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
                  Acepto los terminos y condiciones y la politica de privacidad
                </span>
              </label>

              <label class="register-form__checkbox">
                <input 
                  type="checkbox" 
                  bind:checked={receivePromotions}
                  class="register-form__checkbox-input"
                />
                <span class="register-form__checkbox-label">
                  Deseo recibir promociones y ofertas por correo electronico
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

            <button type="submit" class="register-form__submit">
              Crear cuenta
            </button>
          </form>

          <div class="register__login">
            <p class="register__login-text">
              Ya tienes una cuenta?
              <button 
                type="button" 
                class="register__login-link"
                on:click={() => navigateTo('login')}
              >
                Inicia sesion
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