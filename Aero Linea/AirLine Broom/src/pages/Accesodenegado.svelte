<script>
/**
 * @file Accesodenegado.svelte
 * @description Muestra una pagina de acceso denegado cuando un usuario intenta acceder a una ruta
 * protegida sin la autenticacion o los permisos requeridos. Verifica sessionStorage para determinar
 * si el usuario ha iniciado sesion y renderiza un mensaje contextual en consecuencia. Proporciona
 * botones de navegacion para redirigir al usuario a la pagina de inicio de sesion o al inicio.
 */

  import '../styles/Accesodenegado.css';
  import pasajerosImg from '../assets/pasajeros.png';

  /** Funcion de navegacion recibida del enrutador de la aplicacion para cambiar de pagina. @type {Function} */
  export let navigateTo;

  /** Verdadero cuando existe un usuarioId en sessionStorage, lo que indica una sesion activa. @type {boolean} */
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
