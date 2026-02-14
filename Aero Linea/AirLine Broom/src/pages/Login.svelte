<script>
  import '../styles/reslog.css';
  export let navigateTo;

  let loginData = {
    correoOUsername: '',
    contrasena: ''
  };

  let rememberMe = false;
  let loginError = '';
  let submitting = false;

  async function handleLogin() {
    loginError = '';
    submitting = true;

    try {
      const response = await fetch('http://localhost:5190/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          correoOUsername: loginData.correoOUsername,
          contrasena: loginData.contrasena
        })
      });

      if (!response.ok) {
        loginError = 'Correo, username o contraseña incorrectos.';
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

<div class="login">
  <div class="login__container">
    <div class="login__content">
      <div class="login__image-section">
        <div class="login__image-overlay">
          <h2 class="login__image-title">Bienvenido de vuelta</h2>
          <p class="login__image-subtitle">
            Accede a tu cuenta para gestionar tus reservas y disfrutar de beneficios exclusivos
          </p>
        </div>
      </div>

      <div class="login__form-section">
        <div class="login__form-container">
          <button class="login__back" on:click={() => navigateTo('home')}>
            Volver al inicio
          </button>

          <div class="login__header">
            <h1 class="login__title">Iniciar sesión</h1>
            <p class="login__subtitle">Ingresa tus credenciales para continuar</p>
          </div>

          <form class="login-form" on:submit|preventDefault={handleLogin}>

            <div class="login-form__field">
              <label for="correoOUsername" class="login-form__label">Correo o Username</label>
              <input
                type="text"
                id="correoOUsername"
                class="login-form__input"
                bind:value={loginData.correoOUsername}
                placeholder="correo@ejemplo.com o usuario123"
                required
              />
            </div>

            <div class="login-form__field">
              <label for="contrasena" class="login-form__label">Contraseña</label>
              <input
                type="password"
                id="contrasena"
                class="login-form__input"
                bind:value={loginData.contrasena}
                placeholder="Tu contraseña"
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

            {#if loginError}
              <div class="login-form__error">{loginError}</div>
            {/if}

            <button type="submit" class="login-form__submit" disabled={submitting}>
              {submitting ? 'Iniciando sesión...' : 'Iniciar sesión'}
            </button>

          </form>

          <div class="login__register">
            <p class="login__register-text">
              ¿No tienes una cuenta?
              <button
                type="button"
                class="login__register-link"
                on:click={() => navigateTo('register')}
              >
                Regístrate aquí
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>