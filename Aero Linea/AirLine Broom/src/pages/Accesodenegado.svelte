<script>
/**
 * @file Accesodenegado.svelte
 * @description Displays an access-denied page when a user attempts to reach a protected route
 * without the required authentication or permissions. It checks sessionStorage to determine
 * whether the user is logged in and renders a contextual message accordingly. Provides
 * navigation buttons to redirect the user to the login page or back to the home page.
 */

  import '../styles/Accesodenegado.css';
  import pasajerosImg from '../assets/pasajeros.png';

  /** Navigation function received from the app router to switch pages. @type {Function} */
  export let navigateTo;

  /** True when a userId exists in sessionStorage, indicating an active session. @type {boolean} */
  const isLoggedIn = !!sessionStorage.getItem('usuarioId');
</script>

<!-- Contenedor principal de la pagina de acceso denegado -->
<div class="acceso-denegado">
  <h1 class="acceso-denegado__title">Acceso Denegado</h1>

  <!-- Mensaje contextual segun si el usuario tiene sesion activa o no -->
  <p class="acceso-denegado__message">
    {#if !isLoggedIn}
      Debes registrarte o iniciar sesión para acceder a esta sección.
    {:else}
      No tienes permisos para acceder a esta área.
    {/if}
  </p>

  <div class="acceso-denegado__image">
    <img src={pasajerosImg} alt="Pasajeros esperando en el aeropuerto" />
  </div>

  <!-- Botones de navegacion para redirigir al login o a la pagina de inicio -->
  <div class="acceso-denegado__actions">
    {#if !isLoggedIn}
      <button class="acceso-denegado__btn acceso-denegado__btn--primary" on:click={() => navigateTo('login')}>
        Iniciar Sesión
      </button>
    {/if}

    <button class="acceso-denegado__btn acceso-denegado__btn--secondary" on:click={() => navigateTo('home')}>
      Volver al Inicio
    </button>
  </div>
</div>
