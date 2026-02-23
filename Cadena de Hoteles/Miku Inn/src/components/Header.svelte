<script>
  import { createEventDispatcher } from 'svelte';
  import logo from '../assets/mikuinn-logo.png';
  import '../styles/header.css';

  const dispatch = createEventDispatcher();

  export let navigateTo;
  export let currentPage = 'home';
  export let isLoggedIn = false;
  export let userName = '';
  export let userRolId = null; // 1 = Usuario, 2 = Administrador, 3 = Webservice

  let showUserMenu = false;
  let showMobileMenu = false;
  let searchQuery = '';
  let isScrolled = false;

  $: isAdmin      = userRolId === 2;
  $: isWebservice = userRolId === 3;

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

      <button class="nav-link" class:active={isActivePage('reservations')} on:click={() => handleNavClick('reservations')}>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
        Mis Reservas
      </button>

      {#if isAdmin}
        <button class="nav-link nav-link--admin" class:active={isActivePage('administrador')} on:click={() => handleNavClick('administrador')}>
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
          </svg>
          Panel Admin
        </button>
      {/if}

      {#if isWebservice}
        <button class="nav-link nav-link--webservice" class:active={isActivePage('webservice')} on:click={() => handleNavClick('webservice')}>
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
          </svg>
          Portal WS
        </button>
      {/if}
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

      <button class="action-button" on:click={() => handleNavClick('checkout')} aria-label="Ir al checkout">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
      </button>

      {#if isLoggedIn}
        <div class="user-menu-wrapper">
          <button class="action-button user-button" on:click|stopPropagation={toggleUserMenu}
            aria-label="Menu de usuario" aria-expanded={showUserMenu}>
            <div class="user-avatar" class:user-avatar--admin={isAdmin} class:user-avatar--webservice={isWebservice}>
              {#if isAdmin}
                <span class="user-avatar__icon">⚙</span>
              {:else if isWebservice}
                <span class="user-avatar__icon">⬡</span>
              {:else}
                {userName.charAt(0).toUpperCase()}
              {/if}
            </div>
            <span class="user-name-desktop">
              {userName}
              {#if isAdmin}<span class="user-admin-badge">Admin</span>{/if}
              {#if isWebservice}<span class="user-ws-badge">WS</span>{/if}
            </span>
            <svg class="dropdown-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>

          {#if showUserMenu}
            <div class="user-dropdown" role="menu" tabindex="-1" on:keydown={handleDropdownKey} on:click|stopPropagation>
              <div class="dropdown-header">
                <div class="dropdown-user-info">
                  <div class="dropdown-avatar" class:dropdown-avatar--admin={isAdmin} class:dropdown-avatar--webservice={isWebservice}>
                    {#if isAdmin}⚙{:else if isWebservice}⬡{:else}{userName.charAt(0).toUpperCase()}{/if}
                  </div>
                  <div class="dropdown-user-details">
                    <strong>{userName}</strong>
                    {#if isAdmin}
                      <span class="dropdown-role-badge">Administrador</span>
                    {:else if isWebservice}
                      <span class="dropdown-role-badge dropdown-role-badge--ws">Webservice</span>
                    {:else}
                      <span class="dropdown-role-label">Usuario</span>
                    {/if}
                  </div>
                </div>
              </div>
              <div class="dropdown-divider"></div>

              <button class="dropdown-item" role="menuitem" on:click={() => handleNavClick('profile')}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                Mi Perfil
              </button>

              {#if isAdmin}
                <button class="dropdown-item dropdown-item--admin" role="menuitem" on:click={() => handleNavClick('administrador')}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <rect x="3" y="3" width="7" height="7"></rect>
                    <rect x="14" y="3" width="7" height="7"></rect>
                    <rect x="14" y="14" width="7" height="7"></rect>
                    <rect x="3" y="14" width="7" height="7"></rect>
                  </svg>
                  Panel de Administrador
                </button>
              {/if}

              {#if isWebservice}
                <button class="dropdown-item dropdown-item--webservice" role="menuitem" on:click={() => handleNavClick('webservice')}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                    <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                    <line x1="12" y1="22.08" x2="12" y2="12"/>
                  </svg>
                  Portal Webservice
                </button>
              {/if}

              <div class="dropdown-divider"></div>
              <button class="dropdown-item logout-button" role="menuitem" on:click={handleLogout}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                Cerrar Sesión
              </button>
            </div>
          {/if}
        </div>

      {:else}
        <div class="auth-buttons">
          <button class="btn-secondary" on:click={() => handleNavClick('login')}>Iniciar Sesión</button>
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
        <button class="mobile-nav-link" on:click={() => handleNavClick('reservations')}>Mis Reservas</button>
        {#if isAdmin}
          <button class="mobile-nav-link mobile-nav-link--admin" on:click={() => handleNavClick('administrador')}>
            ⚙ Panel Admin
          </button>
        {/if}
        {#if isWebservice}
          <button class="mobile-nav-link mobile-nav-link--webservice" on:click={() => handleNavClick('webservice')}>
            ⬡ Portal WS
          </button>
        {/if}
        <div class="mobile-divider"></div>
        {#if isLoggedIn}
          <button class="mobile-nav-link" on:click={() => handleNavClick('profile')}>Mi Perfil</button>
          <button class="mobile-nav-link logout" on:click={handleLogout}>Cerrar Sesión</button>
        {:else}
          <button class="mobile-nav-link" on:click={() => handleNavClick('login')}>Iniciar Sesión</button>
          <button class="mobile-nav-link primary" on:click={() => handleNavClick('register')}>Registrarse</button>
        {/if}
      </div>
    </nav>
  {/if}
</header>