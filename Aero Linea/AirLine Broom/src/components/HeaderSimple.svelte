<script>
/**
 * @file HeaderSimple.svelte
 * @description Minimal header variant used on pages that do not require full navigation
 * (e.g. login, register). Shows only the Broom AirLine logo which, when clicked, navigates
 * the user back to the home page via the parent-supplied navigateTo function. Applies styles
 * from HeaderSimple.css and adds a spacer div below to prevent content overlap.
 */
  import '../styles/HeaderSimple.css';
  import logoPath from '../assets/logBAL.png';

  /** Function provided by the parent to navigate to a named page within the SPA. @type {(page: string) => void} */
  export let navigateTo;
</script>

<!-- Cabecera minimalista con logo y titulo para paginas sin navegacion completa -->
<header class="simple-header">
  <div class="simple-header__container">
    <!-- Logo con enlace de regreso a inicio -->
    <div class="simple-header__logo">
      <a href="#home" on:click|preventDefault={() => navigateTo('home')}>
        <img src={logoPath} alt="Broom AirLine" class="simple-header__logo-img">
      </a>
    </div>

    <!-- Titulo de la aerolinea -->
    <div class="simple-header__title">
      <h1>Broom AirLine</h1>
    </div>
  </div>
</header>

<!-- Espaciador para compensar el alto del header fijo -->
<div class="simple-header-spacer"></div>
