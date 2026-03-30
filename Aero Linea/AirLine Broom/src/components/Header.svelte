<script>
  // @ts-nocheck
  import '../styles/header.css';
  import logoPath from '../assets/logBAL.png';
  import { sesion, logout } from '../stores/sesion.js';
  import { onMount } from 'svelte';

  export let navigateTo;
  export let currentPage = 'home';

  const API = 'https://localhost:7107';

  let menuActive = false;
  let cartCount = 0;

  let aeropuertos = [];
  let searchQuery = '';
  let searchResults = [];
  let showSearchResults = false;
  let searchInputDesktop = null;
  let searchInputMobile = null;
  let searching = false;

  onMount(async () => {
    try {
      const res = await fetch(`${API}/api/aeropuertos`);
      aeropuertos = await res.json();
    } catch (err) {
      console.error('Error cargando aeropuertos para buscador:', err);
    }
    await actualizarCartCount();
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  });

  async function actualizarCartCount() {
    if (!$sesion) { cartCount = 0; return; }
    try {
      const res = await fetch(`${API}/api/mis-reservaciones`, { credentials: 'include' });
      if (res.ok) {
        const todas = await res.json();
        const pendientes = todas.filter(r => r.estadoReservaId === 1);
        cartCount = pendientes.reduce((sum, r) => sum + (r.boletos?.length || 0), 0);
      }
    } catch { cartCount = 0; }
  }

  $: if ($sesion) actualizarCartCount();
  $: if (!$sesion) cartCount = 0;

  function handleClickOutside(e) {
    const isInSearch = e.target.closest('.broom-header__search');
    if (!isInSearch) showSearchResults = false;
  }

  function onSearchInput() {
    const q = searchQuery.toLowerCase().trim();
    if (q.length < 1) { searchResults = []; showSearchResults = false; return; }
    searchResults = aeropuertos.filter(a =>
      a.ciudad?.toLowerCase().includes(q) ||
      a.nombre?.toLowerCase().includes(q) ||
      a.codigo?.toLowerCase().includes(q) ||
      a.pais?.toLowerCase().includes(q)
    ).slice(0, 8);
    showSearchResults = searchResults.length > 0;
  }

  async function buscarYNavegar(queryText) {
    if (!queryText || queryText.trim().length < 2) return;
    showSearchResults = false;
    searching = true;
    const q = queryText.trim();
    try {
      const res = await fetch(`${API}/api/vuelos/busqueda-general?query=${encodeURIComponent(q)}`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error en búsqueda');
      const vuelos = await res.json();
      navigateTo('vuelos', {
        fromGlobalSearch: true,
        globalSearchQuery: q,
        globalSearchResults: vuelos
      });
    } catch (err) {
      console.error('Error en búsqueda global:', err);
      navigateTo('vuelos', {
        fromGlobalSearch: true,
        globalSearchQuery: q,
        globalSearchResults: []
      });
    } finally {
      searching = false;
      searchQuery = '';
      menuActive = false;
    }
  }

  function selectSearchResult(aeropuerto) {
    showSearchResults = false;
    buscarYNavegar(aeropuerto.ciudad);
  }

  function handleSearchKeydown(e) {
    if (e.key === 'Enter') {
      if (searchResults.length > 0) selectSearchResult(searchResults[0]);
      else if (searchQuery.trim().length >= 2) buscarYNavegar(searchQuery);
    } else if (e.key === 'Escape') {
      showSearchResults = false;
    }
  }

  function toggleMenu() { menuActive = !menuActive; }

  // ── Navegación con recarga completa de página ────────────────────
  function handleNavigation(page) {
    menuActive = false;
    window.location.href = '/' + page;
  }

  async function handleLogout() {
    await logout();
    window.location.href = '/home';
  }

  $: isLoggedIn = !!$sesion;
  $: isAdmin    = $sesion?.rolNombre === 'Administrador';
</script>

<header class="broom-header">
  <div class="broom-header__container">

    <!-- Logo -->
    <div class="broom-header__logo">
      <a href="/home" on:click|preventDefault={() => handleNavigation('home')}>
        <img src={logoPath} alt="Broom AirLine" class="broom-header__logo-img">
      </a>
    </div>

    <!-- Links inline desktop -->
    <nav class="broom-header__inline-nav">
      {#if isLoggedIn}
        <a href="/reservas" class="broom-header__inline-link"
          class:broom-header__inline-link--active={currentPage === 'reservas'}
          on:click|preventDefault={() => handleNavigation('reservas')}>
          Mis Reservas
        </a>
      {/if}
      {#if isAdmin}
        <a href="/admin" class="broom-header__inline-link broom-header__inline-link--admin"
          class:broom-header__inline-link--active={currentPage === 'admin'}
          on:click|preventDefault={() => handleNavigation('admin')}>
          ✦ Panel Admin
        </a>
      {/if}
    </nav>

    <!-- Buscador desktop -->
    <div class="broom-header__search broom-header__search--desktop">
      <input
        bind:this={searchInputDesktop}
        bind:value={searchQuery}
        on:input={onSearchInput}
        on:keydown={handleSearchKeydown}
        on:focus={() => { if (searchResults.length > 0) showSearchResults = true; }}
        type="text"
        class="broom-header__search-input"
        placeholder={searching ? 'Buscando vuelos...' : 'Buscar vuelos: ciudad, país, código...'}
        aria-label="Buscar vuelos"
        autocomplete="off"
        disabled={searching}
      >
      <button class="broom-header__search-btn" aria-label="Buscar"
        on:click={() => {
          if (searchResults.length > 0) selectSearchResult(searchResults[0]);
          else if (searchQuery.trim().length >= 2) buscarYNavegar(searchQuery);
        }}
        disabled={searching}>
        <svg class="broom-header__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </button>

      {#if showSearchResults && searchResults.length > 0}
        <ul class="broom-header__search-results">
          {#each searchResults as a}
            <li class="broom-header__search-result-item">
              <button type="button" class="broom-header__search-result-btn" on:click={() => selectSearchResult(a)}>
                <span class="broom-header__search-result-code">{a.codigo}</span>
                <div class="broom-header__search-result-info">
                  <span class="broom-header__search-result-city">{a.ciudad}</span>
                  <span class="broom-header__search-result-detail">{a.nombre} · {a.pais}</span>
                </div>
                <span class="broom-header__search-result-arrow">→</span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}
    </div>

    <!-- Acciones derecha -->
    <div class="broom-header__actions">

      <button class="broom-header__action-btn broom-header__cart" aria-label="Carrito"
        on:click|preventDefault={() => handleNavigation('datos-pasajeros')}>
        <svg class="broom-header__action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="9" cy="21" r="1"></circle>
          <circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
        <span class="broom-header__cart-count">{cartCount}</span>
      </button>

      <button class="broom-header__action-btn broom-header__user" aria-label="Perfil"
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
        aria-expanded={menuActive}>
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
      </button>
    </div>
  </div>

  <!-- Nav hamburguesa -->
  <nav
    class="broom-header__nav"
    class:broom-header__nav--active={menuActive}
    aria-label="Navegación principal">

    <!-- Buscador mobile -->
    <div class="broom-header__search broom-header__search--mobile">
      <input
        bind:this={searchInputMobile}
        bind:value={searchQuery}
        on:input={onSearchInput}
        on:keydown={handleSearchKeydown}
        on:focus={() => { if (searchResults.length > 0) showSearchResults = true; }}
        type="text"
        class="broom-header__search-input"
        placeholder={searching ? 'Buscando...' : 'Buscar vuelos...'}
        aria-label="Buscar vuelos"
        autocomplete="off"
        disabled={searching}
      >
      <button class="broom-header__search-btn" aria-label="Buscar"
        on:click={() => {
          if (searchResults.length > 0) selectSearchResult(searchResults[0]);
          else if (searchQuery.trim().length >= 2) buscarYNavegar(searchQuery);
        }}
        disabled={searching}>
        <svg class="broom-header__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </button>

      {#if showSearchResults && searchResults.length > 0}
        <ul class="broom-header__search-results">
          {#each searchResults as a}
            <li class="broom-header__search-result-item">
              <button type="button" class="broom-header__search-result-btn" on:click={() => selectSearchResult(a)}>
                <span class="broom-header__search-result-code">{a.codigo}</span>
                <div class="broom-header__search-result-info">
                  <span class="broom-header__search-result-city">{a.ciudad}</span>
                  <span class="broom-header__search-result-detail">{a.nombre} · {a.pais}</span>
                </div>
                <span class="broom-header__search-result-arrow">→</span>
              </button>
            </li>
          {/each}
        </ul>
      {/if}
    </div>

    <ul class="broom-header__nav-list">
      <li class="broom-header__nav-item">
        <a href="/home" class="broom-header__nav-link"
          class:broom-header__nav-link--active={currentPage === 'home'}
          on:click|preventDefault={() => handleNavigation('home')}>
          Inicio
        </a>
      </li>
      <li class="broom-header__nav-item">
        <a href="/contactanos" class="broom-header__nav-link"
          class:broom-header__nav-link--active={currentPage === 'contactanos'}
          on:click|preventDefault={() => handleNavigation('contactanos')}>
          Contacto
        </a>
      </li>
      {#if isAdmin}
        <li class="broom-header__nav-item">
          <a href="/admin" class="broom-header__nav-link broom-header__nav-link--admin"
            class:broom-header__nav-link--active={currentPage === 'admin'}
            on:click|preventDefault={() => handleNavigation('admin')}>
            ✦ Panel Administración
          </a>
        </li>
      {/if}
      {#if isLoggedIn}
        <li class="broom-header__nav-item">
          <a href="/profile" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'profile'}
            on:click|preventDefault={() => handleNavigation('profile')}>
            Mi Perfil
          </a>
        </li>
        <li class="broom-header__nav-item">
          <a href="#logout" class="broom-header__nav-link broom-header__nav-link--logout"
            on:click|preventDefault={handleLogout}>
            Cerrar Sesión
          </a>
        </li>
      {:else}
        <li class="broom-header__nav-item">
          <a href="/login" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'login'}
            on:click|preventDefault={() => handleNavigation('login')}>
            Iniciar Sesión
          </a>
        </li>
        <li class="broom-header__nav-item">
          <a href="/register" class="broom-header__nav-link broom-header__nav-link--register"
            class:broom-header__nav-link--active={currentPage === 'register'}
            on:click|preventDefault={() => handleNavigation('register')}>
            Registrarse
          </a>
        </li>
      {/if}
    </ul>
  </nav>
</header>

<div class="broom-header-spacer"></div>