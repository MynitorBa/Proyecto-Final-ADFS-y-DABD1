<script>
  import { createEventDispatcher } from 'svelte';
  import logo from '../assets/mikuinn-logo.png';
  
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
  
  // Detectar scroll para agregar sombra
  function handleScroll() {
    isScrolled = window.scrollY > 10;
  }
  
  function toggleUserMenu() {
    showUserMenu = !showUserMenu;
    showMobileMenu = false;
  }
  
  function toggleMobileMenu() {
    showMobileMenu = !showMobileMenu;
    showUserMenu = false;
  }
  
  function handleSearch(e) {
    e.preventDefault();
    if (searchQuery.trim()) {
      dispatch('search', searchQuery);
      searchQuery = '';
    }
  }
  
  function handleLogout() {
    dispatch('logout');
    showUserMenu = false;
  }
  
  function closeMenus() {
    showUserMenu = false;
    showMobileMenu = false;
  }
  
  function handleNavClick(page) {
    navigateTo(page);
    showMobileMenu = false;
  }
  
  // Helper para determinar si un link está activo
  function isActivePage(page) {
    return currentPage === page;
  }
</script>

<svelte:window on:scroll={handleScroll} on:click={closeMenus} />

<header class="header" class:scrolled={isScrolled}>
  <div class="header-container">
    <!-- Logo Section -->
    <div class="logo-section">
      <button class="logo" on:click={() => handleNavClick('home')} aria-label="Ir al inicio">
        <img src={logo} alt="Miku Inn" class="logo-image" />
      </button>
    </div>
    
    <!-- Desktop Navigation -->
    <nav class="desktop-nav">
      <button 
        class="nav-link" 
        class:active={isActivePage('home')}
        on:click={() => handleNavClick('home')}
      >
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          </svg>
        </span>
        Inicio
      </button>
      <button 
        class="nav-link" 
        class:active={isActivePage('search-results')}
        on:click={() => handleNavClick('search-results')}
      >
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
        </span>
        Buscar
      </button>
      <button 
        class="nav-link" 
        class:active={isActivePage('destinations')}
        on:click={() => handleNavClick('destinations')}
      >
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="2" y1="12" x2="22" y2="12"></line>
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
          </svg>
        </span>
        Destinos
      </button>
      <button 
        class="nav-link" 
        class:active={isActivePage('offers')}
        on:click={() => handleNavClick('offers')}
      >
        <span class="nav-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
            <line x1="7" y1="7" x2="7.01" y2="7"></line>
          </svg>
        </span>
        Ofertas
      </button>
    </nav>
    
    <!-- Search Bar (Desktop) -->
    <form class="search-bar" on:submit={handleSearch}>
      <div class="search-input-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Buscar hoteles, destinos..."
          class="search-input"
        />
      </div>
    </form>
    
    <!-- User Actions -->
    <div class="user-actions">
      <!-- Cart Button -->
      <button 
        class="action-button cart-button" 
        on:click={() => handleNavClick('checkout')}
        aria-label="Carrito de compras"
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="9" cy="21" r="1"></circle>
          <circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
        {#if cartItemsCount > 0}
          <span class="cart-badge">{cartItemsCount}</span>
        {/if}
      </button>
      
      {#if isLoggedIn}
        <!-- User Menu Button -->
        <div class="user-menu-wrapper">
          <button 
            class="action-button user-button"
            on:click|stopPropagation={toggleUserMenu}
            aria-label="Menú de usuario"
            aria-expanded={showUserMenu}
          >
            <div class="user-avatar">
              {userName.charAt(0).toUpperCase()}
            </div>
            <span class="user-name-desktop">{userName}</span>
            <svg class="dropdown-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
          
          {#if showUserMenu}
            <div class="user-dropdown" on:click|stopPropagation>
              <div class="dropdown-header">
                <div class="dropdown-user-info">
                  <div class="dropdown-avatar">
                    {userName.charAt(0).toUpperCase()}
                  </div>
                  <div class="dropdown-user-details">
                    <strong>{userName}</strong>
                    <span class="user-email">usuario@email.com</span>
                  </div>
                </div>
              </div>
              
              <div class="dropdown-divider"></div>
              
              <nav class="dropdown-nav">
                <button class="dropdown-item" on:click={() => handleNavClick('reservations')}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14 2 14 8 20 8"></polyline>
                  </svg>
                  <span>Mis Reservas</span>
                </button>
                <button class="dropdown-item" on:click={() => handleNavClick('profile')}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <span>Mi Perfil</span>
                </button>
                <button class="dropdown-item" on:click={() => handleNavClick('favorites')}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                  </svg>
                  <span>Favoritos</span>
                </button>
                <button class="dropdown-item" on:click={() => handleNavClick('settings')}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M12 1v6m0 6v6m8.66-9l-5.2 3M8.54 14l-5.2 3m13.32 0l-5.2-3M8.54 10l-5.2-3"></path>
                  </svg>
                  <span>Configuración</span>
                </button>
              </nav>
              
              <div class="dropdown-divider"></div>
              
              <button class="dropdown-item logout-button" on:click={handleLogout}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                  <polyline points="16 17 21 12 16 7"></polyline>
                  <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>Cerrar Sesión</span>
              </button>
            </div>
          {/if}
        </div>
      {:else}
        <!-- Login/Register Buttons -->
        <div class="auth-buttons">
          <button class="btn-secondary" on:click={() => handleNavClick('login')}>
            Iniciar Sesión
          </button>
          <button class="btn-primary" on:click={() => handleNavClick('register')}>
            Registrarse
          </button>
        </div>
      {/if}
      
      <!-- Mobile Menu Toggle -->
      <button 
        class="mobile-menu-toggle"
        on:click|stopPropagation={toggleMobileMenu}
        aria-label="Menú"
        aria-expanded={showMobileMenu}
      >
        {#if showMobileMenu}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        {:else}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="12" x2="21" y2="12"></line>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <line x1="3" y1="18" x2="21" y2="18"></line>
          </svg>
        {/if}
      </button>
    </div>
  </div>
  
  <!-- Mobile Menu -->
  {#if showMobileMenu}
    <div class="mobile-nav" on:click|stopPropagation>
      <!-- Mobile Search -->
      <form class="mobile-search" on:submit={handleSearch}>
        <div class="search-input-wrapper">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input
            type="text"
            bind:value={searchQuery}
            placeholder="Buscar..."
            class="search-input"
          />
        </div>
      </form>
      
      <!-- Mobile Navigation Links -->
      <nav class="mobile-nav-links">
        <button class="mobile-nav-link" on:click={() => handleNavClick('home')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          </svg>
          <span>Inicio</span>
        </button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('search-results')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <span>Buscar Hoteles</span>
        </button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('destinations')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="2" y1="12" x2="22" y2="12"></line>
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
          </svg>
          <span>Destinos</span>
        </button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('offers')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
            <line x1="7" y1="7" x2="7.01" y2="7"></line>
          </svg>
          <span>Ofertas</span>
        </button>
        
        {#if isLoggedIn}
          <div class="mobile-divider"></div>
          
          <button class="mobile-nav-link" on:click={() => handleNavClick('reservations')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
            </svg>
            <span>Mis Reservas</span>
          </button>
          <button class="mobile-nav-link" on:click={() => handleNavClick('profile')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>Mi Perfil</span>
          </button>
          <button class="mobile-nav-link" on:click={() => handleNavClick('favorites')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            <span>Favoritos</span>
          </button>
          <button class="mobile-nav-link" on:click={() => handleNavClick('settings')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M12 1v6m0 6v6m8.66-9l-5.2 3M8.54 14l-5.2 3m13.32 0l-5.2-3M8.54 10l-5.2-3"></path>
            </svg>
            <span>Configuración</span>
          </button>
          
          <div class="mobile-divider"></div>
          
          <button class="mobile-nav-link logout" on:click={handleLogout}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            <span>Cerrar Sesión</span>
          </button>
        {:else}
          <div class="mobile-divider"></div>
          
          <button class="mobile-nav-link" on:click={() => handleNavClick('login')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
              <polyline points="10 17 15 12 10 7"></polyline>
              <line x1="15" y1="12" x2="3" y2="12"></line>
            </svg>
            <span>Iniciar Sesión</span>
          </button>
          <button class="mobile-nav-link primary" on:click={() => handleNavClick('register')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="8.5" cy="7" r="4"></circle>
              <line x1="20" y1="8" x2="20" y2="14"></line>
              <line x1="23" y1="11" x2="17" y2="11"></line>
            </svg>
            <span>Registrarse</span>
          </button>
        {/if}
      </nav>
    </div>
  {/if}
</header>

<style>
  /* ========== HEADER BASE ========== */
  .header {
    position: sticky;
    top: 0;
    z-index: 1000;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
  }
  
  .header.scrolled {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
  
  .header-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 1rem 2rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 2rem;
  }
  
  /* ========== LOGO ========== */
  .logo-section {
    display: flex;
    align-items: center;
  }
  
  .logo {
    display: flex;
    align-items: center;
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    transition: transform 0.2s ease;
  }
  
  .logo:hover {
    transform: scale(1.05);
  }
  
  .logo-image {
    height: 100px;
    width: auto;
  }
  
  /* ========== DESKTOP NAVIGATION ========== */
  .desktop-nav {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .nav-link {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 1rem;
    color: #475569;
    text-decoration: none;
    font-weight: 500;
    font-size: 0.95rem;
    border-radius: 8px;
    transition: all 0.2s ease;
    background: none;
    border: none;
    cursor: pointer;
    font-family: inherit;
  }
  
  .nav-link:hover {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
  }
  
  .nav-link.active {
    background: rgba(102, 126, 234, 0.12);
    color: #667eea;
    font-weight: 600;
  }
  
  .nav-icon {
    display: flex;
    align-items: center;
    opacity: 0.8;
  }
  
  /* ========== SEARCH BAR ========== */
  .search-bar {
    flex: 1;
    max-width: 400px;
  }
  
  .search-input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .search-icon {
    position: absolute;
    left: 1rem;
    color: #94a3b8;
    pointer-events: none;
  }
  
  .search-input {
    width: 100%;
    padding: 0.75rem 1rem 0.75rem 2.75rem;
    background: #f8fafc;
    border: 1.5px solid #e2e8f0;
    border-radius: 12px;
    font-size: 0.95rem;
    transition: all 0.2s ease;
    font-family: inherit;
  }
  
  .search-input:focus {
    outline: none;
    background: white;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .search-input::placeholder {
    color: #94a3b8;
  }
  
  /* ========== USER ACTIONS ========== */
  .user-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .action-button {
    position: relative;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.625rem 0.875rem;
    background: none;
    border: none;
    color: #475569;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s ease;
    text-decoration: none;
    font-family: inherit;
  }
  
  .action-button:hover {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
  }
  
  /* Cart Badge */
  .cart-button {
    position: relative;
  }
  
  .cart-badge {
    position: absolute;
    top: 0;
    right: 0;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 9px;
    font-size: 0.7rem;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  /* ========== USER MENU ========== */
  .user-menu-wrapper {
    position: relative;
  }
  
  .user-button {
    gap: 0.625rem;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 0.9rem;
  }
  
  .user-name-desktop {
    font-weight: 500;
    color: #1e293b;
  }
  
  .dropdown-arrow {
    transition: transform 0.2s ease;
  }
  
  .user-button[aria-expanded="true"] .dropdown-arrow {
    transform: rotate(180deg);
  }
  
  /* User Dropdown */
  .user-dropdown {
    position: absolute;
    top: calc(100% + 0.5rem);
    right: 0;
    min-width: 260px;
    background: white;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    padding: 0.75rem;
    animation: dropdownSlide 0.2s ease;
  }
  
  @keyframes dropdownSlide {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .dropdown-header {
    padding: 0.5rem;
    margin-bottom: 0.5rem;
  }
  
  .dropdown-user-info {
    display: flex;
    align-items: center;
    gap: 0.875rem;
  }
  
  .dropdown-avatar {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    font-size: 1.1rem;
    flex-shrink: 0;
  }
  
  .dropdown-user-details {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }
  
  .dropdown-user-details strong {
    color: #1e293b;
    font-size: 0.95rem;
  }
  
  .user-email {
    color: #64748b;
    font-size: 0.85rem;
  }
  
  .dropdown-divider {
    height: 1px;
    background: rgba(0, 0, 0, 0.08);
    margin: 0.5rem 0;
  }
  
  .dropdown-nav {
    display: flex;
    flex-direction: column;
  }
  
  .dropdown-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
    color: #475569;
    text-decoration: none;
    font-size: 0.95rem;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.2s ease;
    border: none;
    background: none;
    width: 100%;
    text-align: left;
    cursor: pointer;
    font-family: inherit;
  }
  
  .dropdown-item:hover {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
  }
  
  .dropdown-item svg {
    flex-shrink: 0;
    opacity: 0.7;
  }
  
  .logout-button {
    color: #dc2626;
  }
  
  .logout-button:hover {
    background: rgba(220, 38, 38, 0.08);
    color: #dc2626;
  }
  
  /* ========== AUTH BUTTONS ========== */
  .auth-buttons {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .btn-secondary,
  .btn-primary {
    padding: 0.625rem 1.25rem;
    border-radius: 8px;
    font-weight: 600;
    font-size: 0.95rem;
    text-decoration: none;
    transition: all 0.2s ease;
    border: none;
    cursor: pointer;
    font-family: inherit;
  }
  
  .btn-secondary {
    background: transparent;
    color: #475569;
    border: 1.5px solid #e2e8f0;
  }
  
  .btn-secondary:hover {
    background: #f8fafc;
    border-color: #cbd5e1;
  }
  
  .btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
  }
  
  .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  /* ========== MOBILE MENU TOGGLE ========== */
  .mobile-menu-toggle {
    display: none;
    align-items: center;
    justify-content: center;
    padding: 0.5rem;
    background: none;
    border: none;
    color: #475569;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s ease;
  }
  
  .mobile-menu-toggle:hover {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
  }
  
  /* ========== MOBILE MENU ========== */
  .mobile-nav {
    display: none;
    flex-direction: column;
    background: white;
    border-top: 1px solid rgba(0, 0, 0, 0.08);
    animation: slideDown 0.2s ease;
  }
  
  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .mobile-search {
    padding: 1rem 1.5rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  }
  
  .mobile-nav-links {
    padding: 0.5rem;
  }
  
  .mobile-nav-link {
    display: flex;
    align-items: center;
    gap: 0.875rem;
    padding: 0.875rem 1rem;
    color: #475569;
    text-decoration: none;
    font-weight: 500;
    font-size: 0.95rem;
    border-radius: 8px;
    transition: all 0.2s ease;
    border: none;
    background: none;
    width: 100%;
    text-align: left;
    cursor: pointer;
    font-family: inherit;
  }
  
  .mobile-nav-link:hover {
    background: rgba(102, 126, 234, 0.08);
    color: #667eea;
  }
  
  .mobile-nav-link.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: 600;
    margin-top: 0.5rem;
  }
  
  .mobile-nav-link.primary:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }
  
  .mobile-nav-link.logout {
    color: #dc2626;
  }
  
  .mobile-nav-link.logout:hover {
    background: rgba(220, 38, 38, 0.08);
  }
  
  .mobile-divider {
    height: 1px;
    background: rgba(0, 0, 0, 0.08);
    margin: 0.5rem 0;
  }
  
  /* ========== RESPONSIVE ========== */
  @media (max-width: 1024px) {
    .desktop-nav {
      display: none;
    }
    
    .search-bar {
      display: none;
    }
    
    .user-name-desktop {
      display: none;
    }
    
    .dropdown-arrow {
      display: none;
    }
    
    .auth-buttons .btn-secondary {
      display: none;
    }
    
    .mobile-menu-toggle {
      display: flex;
    }
    
    .mobile-nav {
      display: flex;
    }
  }
  
  @media (max-width: 768px) {
    .header-container {
      padding: 0.75rem 1rem;
      gap: 1rem;
    }
    
    .logo-image {
      height: 40px;
    }
  }
  
  /* ========== DARK MODE SUPPORT ========== */
  @media (prefers-color-scheme: dark) {
    .header {
      background: rgba(15, 23, 42, 0.95);
      border-bottom-color: rgba(255, 255, 255, 0.1);
    }
    
    .user-name-desktop {
      color: #f8fafc;
    }
    
    .nav-link,
    .action-button,
    .mobile-nav-link,
    .dropdown-item {
      color: #cbd5e1;
    }
    
    .nav-link:hover,
    .action-button:hover,
    .mobile-nav-link:hover {
      color: #667eea;
      background: rgba(102, 126, 234, 0.15);
    }
    
    .search-input {
      background: rgba(15, 23, 42, 0.5);
      border-color: rgba(255, 255, 255, 0.1);
      color: #f8fafc;
    }
    
    .search-input::placeholder {
      color: #64748b;
    }
    
    .btn-secondary {
      color: #cbd5e1;
      border-color: rgba(255, 255, 255, 0.15);
    }
    
    .user-dropdown,
    .mobile-nav {
      background: #1e293b;
      border-color: rgba(255, 255, 255, 0.1);
    }
    
    .mobile-search,
    .dropdown-divider,
    .mobile-divider {
      border-color: rgba(255, 255, 255, 0.1);
    }
  }
</style>