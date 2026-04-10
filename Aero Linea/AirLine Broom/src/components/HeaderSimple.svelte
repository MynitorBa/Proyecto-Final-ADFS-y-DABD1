<script>
/**
 * @file HeaderSimple.svelte
 * @description Variante minimalista de cabecera usada en paginas que no requieren navegacion completa
 * (por ejemplo, login, registro). Muestra solo el logo de Broom AirLine que, al hacer clic, navega
 * al usuario de regreso a la pagina de inicio usando la funcion navigateTo proporcionada por el padre.
 * Aplica estilos de HeaderSimple.css y agrega un div espaciador debajo para evitar superposicion de contenido.
 */
  import '../styles/HeaderSimple.css';
  import logoPath from '../assets/logBAL.png';

  /** Funcion proporcionada por el padre para navegar a una pagina nombrada dentro del SPA. @type {(page: string) => void} */
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
