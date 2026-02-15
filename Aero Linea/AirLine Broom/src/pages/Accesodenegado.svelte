<script>
  import '../styles/Accesodenegado.css';
  export let navigateTo;

  const isLoggedIn = !!sessionStorage.getItem('usuarioId');
</script>

<div class="acceso-denegado">
  <div class="acceso-denegado__icon">🔒</div>
  
  <h1 class="acceso-denegado__title">Acceso Denegado</h1>
  
  <p class="acceso-denegado__message">
    {#if !isLoggedIn}
      Debes iniciar sesión para acceder a esta sección.
    {:else}
      No tienes permisos suficientes para acceder a esta sección.
    {/if}
  </p>

  <div class="acceso-denegado__actions">
    {#if !isLoggedIn}
      <button
        class="acceso-denegado__btn acceso-denegado__btn--primary"
        on:click={() => navigateTo('login')}
      >
        Iniciar Sesión
      </button>
    {/if}
    
    <button
      class="acceso-denegado__btn acceso-denegado__btn--secondary"
      on:click={() => navigateTo('home')}
    >
      Volver al Inicio
    </button>
  </div>
</div>