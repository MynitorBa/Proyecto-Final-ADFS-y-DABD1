<script>
  export let navigateTo;

  const isLoggedIn = !!sessionStorage.getItem('usuarioId');
</script>

<div style="
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 4rem 2rem;
">
  <div style="font-size: 4rem; margin-bottom: 1.5rem;">🔒</div>
  <h1 style="font-size: 2rem; font-weight: 300; color: #1C1A18; margin-bottom: 1rem;">
    Acceso Denegado
  </h1>
  <p style="color: #6b6b6b; max-width: 400px; margin-bottom: 2rem; line-height: 1.6;">
    {#if !isLoggedIn}
      Debes iniciar sesión para acceder a esta sección.
    {:else}
      No tienes permisos suficientes para acceder a esta sección.
    {/if}
  </p>

  <div style="display: flex; gap: 1rem; flex-wrap: wrap; justify-content: center;">
    {#if !isLoggedIn}
      <button
        on:click={() => navigateTo('login')}
        style="
          padding: 0.75rem 2rem;
          background: #8B6B4A;
          color: white;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 0.9rem;
          letter-spacing: 1px;
          text-transform: uppercase;
        "
      >
        Iniciar Sesión
      </button>
    {/if}
    <button
      on:click={() => navigateTo('home')}
      style="
        padding: 0.75rem 2rem;
        background: transparent;
        color: #8B6B4A;
        border: 2px solid #8B6B4A;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        letter-spacing: 1px;
        text-transform: uppercase;
      "
    >
      Volver al Inicio
    </button>
  </div>
</div>