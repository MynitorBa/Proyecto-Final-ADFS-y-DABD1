<script>
  import '../styles/reslog.css';
  export let navigateTo;

  let loginData = {
    email: '',
    password: ''
  };

  let rememberMe = false;

  function handleLogin() {
    console.log('Intentando login:', loginData);
    // Aquí irá la lógica de autenticación con backend
    navigateTo('home');
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
            <h1 class="login__title">Iniciar sesion</h1>
            <p class="login__subtitle">Ingresa tus credenciales para continuar</p>
          </div>

          <form class="login-form" on:submit|preventDefault={handleLogin}>
            <div class="login-form__field">
              <label for="email" class="login-form__label">Email</label>
              <input 
                type="email" 
                id="email"
                class="login-form__input"
                bind:value={loginData.email}
                placeholder="correo@ejemplo.com"
              />
            </div>

            <div class="login-form__field">
              <label for="password" class="login-form__label">Contraseña</label>
              <input 
                type="password" 
                id="password"
                class="login-form__input"
                bind:value={loginData.password}
                placeholder="Tu contraseña"
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

            <button type="submit" class="login-form__submit">
              Iniciar sesion
            </button>
          </form>

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

<style>
  h2 {
    color: white;
}
</style>