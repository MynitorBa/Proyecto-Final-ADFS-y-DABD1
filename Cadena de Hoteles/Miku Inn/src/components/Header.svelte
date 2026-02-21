<script>
  import { createEventDispatcher } from 'svelte';
  import logo from '../assets/mikuinn-logo.png';
  import '../styles/header.css';

  const dispatch = createEventDispatcher();

  export let navigateTo;
  export let currentPage = 'home';
  export let isLoggedIn = false;
  export let userName = '';
  export let cartItemsCount = 0;

  let showUserMenu = false;
  let showMobileMenu = false;
  let searchQuery = '';
  let isScrolled = false;

  function handleScroll() { isScrolled = window.scrollY > 10; }
  function toggleUserMenu() { showUserMenu = !showUserMenu; showMobileMenu = false; }
  function toggleMobileMenu() { showMobileMenu = !showMobileMenu; showUserMenu = false; }
  function closeMenus() { showUserMenu = false; showMobileMenu = false; }

  function handleSearch(e) {
    e.preventDefault();
    if (searchQuery.trim()) { dispatch('search', searchQuery); searchQuery = ''; }
  }

  function handleLogout() { dispatch('logout'); showUserMenu = false; }
  function handleNavClick(page) { navigateTo(page); showMobileMenu = false; showUserMenu = false; }
  function isActivePage(page) { return currentPage === page; }
  function handleDropdownKey(e) { if (e.key === 'Escape') closeMenus(); }
</script>

<svelte:window on:scroll={handleScroll} on:click={closeMenus} />

<header class="header" class:scrolled={isScrolled}>
  <div class="header-container">

    <!-- Logo -->
    <button class="logo" on:click={() => handleNavClick('home')} aria-label="Ir al inicio">
      <img src={logo} alt="Miku Inn" class="logo-image" />
    </button>

    <!-- Desktop Nav -->
    <nav class="desktop-nav">
      <button class="nav-link" class:active={isActivePage('home')} on:click={() => handleNavClick('home')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path></svg>
        Inicio
      </button>
      <button class="nav-link" class:active={isActivePage('search-results')} on:click={() => handleNavClick('search-results')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
        Buscar
      </button>
      <button class="nav-link" class:active={isActivePage('destinations')} on:click={() => handleNavClick('destinations')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
        Destinos
      </button>
      <button class="nav-link" class:active={isActivePage('offers')} on:click={() => handleNavClick('offers')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path><line x1="7" y1="7" x2="7.01" y2="7"></line></svg>
        Ofertas
      </button>
      <button class="nav-link" class:active={isActivePage('reservations')} on:click={() => handleNavClick('reservations')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
        Mis Reservas
      </button>
    </nav>

    <!-- Search Bar -->
    <form class="search-bar" on:submit={handleSearch}>
      <div class="search-input-wrapper">
        <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
        <input type="text" bind:value={searchQuery} placeholder="Buscar hoteles, destinos..." class="search-input" aria-label="Buscar" />
      </div>
    </form>

    <!-- User Actions -->
    <div class="user-actions">

      <!-- Botón tiendita → hotel-detail -->
      <button class="action-button" on:click={() => handleNavClick('hotel-detail')} aria-label="Ver hotel">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
        {#if cartItemsCount > 0}
          <span class="cart-badge">{cartItemsCount}</span>
        {/if}
      </button>

      {#if isLoggedIn}
        <div class="user-menu-wrapper">
          <button class="action-button user-button" on:click|stopPropagation={toggleUserMenu}
            aria-label="Menu de usuario" aria-expanded={showUserMenu}>
            <div class="user-avatar">{userName.charAt(0).toUpperCase()}</div>
            <span class="user-name-desktop">{userName}</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>

          {#if showUserMenu}
            <div class="user-dropdown" role="menu" tabindex="-1" aria-label="Menu de usuario" on:keydown={handleDropdownKey} on:click|stopPropagation>
              <div class="dropdown-user-info">
                <div class="dropdown-avatar">{userName.charAt(0).toUpperCase()}</div>
                <div>
                  <strong>{userName}</strong>
                  <span class="user-email">usuario@email.com</span>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item" role="menuitem" on:click={() => handleNavClick('reservations')}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                Mis Reservas
              </button>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout-button" role="menuitem" on:click={handleLogout}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                Cerrar Sesion
              </button>
            </div>
          {/if}
        </div>
      {:else}
        <div class="auth-buttons">
          <button class="btn-secondary" on:click={() => handleNavClick('login')}>Iniciar Sesion</button>
          <button class="btn-primary" on:click={() => handleNavClick('register')}>Registrarse</button>
        </div>
      {/if}

      <!-- Mobile Toggle -->
      <button class="mobile-menu-toggle" on:click|stopPropagation={toggleMobileMenu} aria-label="Abrir menu" aria-expanded={showMobileMenu}>
        {#if showMobileMenu}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        {:else}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
        {/if}
      </button>
    </div>
  </div>

  <!-- Mobile Menu -->
  {#if showMobileMenu}
    <nav class="mobile-nav" aria-label="Menu movil">
      <form class="mobile-search" on:submit={handleSearch}>
        <div class="search-input-wrapper">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
          <input type="text" bind:value={searchQuery} placeholder="Buscar..." class="search-input" aria-label="Buscar" />
        </div>
      </form>
      <div class="mobile-nav-links">
        <button class="mobile-nav-link" on:click={() => handleNavClick('home')}>Inicio</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('search-results')}>Buscar Hoteles</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('destinations')}>Destinos</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('offers')}>Ofertas</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('reservations')}>Mis Reservas</button>
        <div class="mobile-divider"></div>
        {#if isLoggedIn}
          <button class="mobile-nav-link logout" on:click={handleLogout}>Cerrar Sesion</button>
        {:else}
          <button class="mobile-nav-link" on:click={() => handleNavClick('login')}>Iniciar Sesion</button>
          <button class="mobile-nav-link primary" on:click={() => handleNavClick('register')}>Registrarse</button>
        {/if}
      </div>
    </nav>
  {/if}
</header>