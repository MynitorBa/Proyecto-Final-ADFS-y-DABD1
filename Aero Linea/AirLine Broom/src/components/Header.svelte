<script>
  import '../styles/header.css';
  import logoPath from '../assets/logBAL.png';
  import { sesion, logout } from '../stores/sesion.js';

  export let navigateTo;
  export let currentPage = 'home';

  let menuActive = false;
  let cartCount = 0;

  function toggleMenu() {
    menuActive = !menuActive;
  }

  function handleNavigation(page) {
    navigateTo(page);
    menuActive = false;
  }

  async function handleLogout() {
    await logout();
    navigateTo('home');
  }

  // Leemos directo del store, son reactivos automáticamente
  $: isLoggedIn = !!$sesion;
  $: isAdmin    = $sesion?.rolNombre === 'Administrador';
</script>

<header class="broom-header">
  <div class="broom-header__container">
    <div class="broom-header__logo">
      <a href="#home" on:click|preventDefault={() => handleNavigation('home')}>
        <img src={logoPath} alt="Broom AirLine" class="broom-header__logo-img">
      </a>
    </div>

    <div class="broom-header__search broom-header__search--desktop">
      <input 
        type="text" 
        class="broom-header__search-input" 
        placeholder="Buscar vuelos, destinos..."
        aria-label="Buscar vuelos y destinos"
      >
      <button class="broom-header__search-btn" aria-label="Buscar"
        class:broom-header__nav-link--active={currentPage === 'resultados-busqueda'}
        on:click|preventDefault={() => handleNavigation('resultados-busqueda')}>
        <svg class="broom-header__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </button>
    </div>

    <div class="broom-header__actions">
      <button class="broom-header__action-btn broom-header__cart" aria-label="Carrito de compras"
        class:broom-header__nav-link--active={currentPage === 'datos-pasajeros'}
        on:click|preventDefault={() => handleNavigation('datos-pasajeros')}>
        <svg class="broom-header__action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="9" cy="21" r="1"></circle>
          <circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
        <span class="broom-header__cart-count">{cartCount}</span>
      </button>

      <button class="broom-header__action-btn broom-header__user" aria-label="Perfil de usuario"
        class:broom-header__nav-link--active={currentPage === 'profile'}
        on:click|preventDefault={() => handleNavigation('profile')}>
        <svg class="broom-header__action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
      </button>

      <button 
        class="broom-header__menu-toggle" 
        class:broom-header__menu-toggle--active={menuActive}
        on:click={toggleMenu}
        aria-label="Menú de navegación"
        aria-expanded={menuActive}
      >
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
      </button>
    </div>
  </div>

  <nav 
    class="broom-header__nav" 
    class:broom-header__nav--active={menuActive}
    aria-label="Navegación principal"
  >
    <div class="broom-header__search broom-header__search--mobile">
      <input 
        type="text" 
        class="broom-header__search-input" 
        placeholder="Buscar vuelos, destinos..."
        aria-label="Buscar vuelos y destinos"
      >
      <button class="broom-header__search-btn" aria-label="Buscar"
        class:broom-header__nav-link--active={currentPage === 'resultados-busqueda'}
        on:click|preventDefault={() => handleNavigation('resultados-busqueda')}>
        <svg class="broom-header__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </button>
    </div>

    <ul class="broom-header__nav-list">
      <li class="broom-header__nav-item">
        <a href="#home" class="broom-header__nav-link" 
          class:broom-header__nav-link--active={currentPage === 'home'}
          on:click|preventDefault={() => handleNavigation('home')}>
          Inicio
        </a>
      </li>
      <li class="broom-header__nav-item">
        <a href="#destinos-destacados" class="broom-header__nav-link"
          class:broom-header__nav-link--active={currentPage === 'destinos-destacados'}
          on:click|preventDefault={() => handleNavigation('destinos-destacados')}>
          Destinos Destacados
        </a>
      </li>
      {#if isLoggedIn}
        <li class="broom-header__nav-item">
          <a href="#reservas" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'reservas'}
            on:click|preventDefault={() => handleNavigation('reservas')}>
            Mis Reservas
          </a>
        </li>
      {/if}
      {#if isAdmin}
        <li class="broom-header__nav-item">
          <a href="#admin" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'admin'}
            on:click|preventDefault={() => handleNavigation('admin')}>
            Administración
          </a>
        </li>
      {/if}
      {#if isLoggedIn}
        <li class="broom-header__nav-item">
          <a href="#logout" class="broom-header__nav-link"
            on:click|preventDefault={handleLogout}>
            Logout
          </a>
        </li>
      {:else}
        <li class="broom-header__nav-item">
          <a href="#login" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'login'}
            on:click|preventDefault={() => handleNavigation('login')}>
            Login
          </a>
        </li>
      {/if}
    </ul>
  </nav>
</header>

<div class="broom-header-spacer"></div>