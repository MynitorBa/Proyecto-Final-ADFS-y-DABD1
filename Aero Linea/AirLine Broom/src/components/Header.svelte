<script>
/**
 * @file Header.svelte
 * @description Main site header for Broom AirLine. Renders the logo, desktop inline nav links,
 * a searchable flight-search bar (desktop and mobile variants), a cart icon with pending-ticket
 * count, a profile icon, and a hamburger menu for mobile navigation. Integrates with the session
 * store to show role-specific links (admin panel, agency panel, reservations). Performs a
 * full-page navigation using window.location.href to ensure stores and state are reset on each
 * route change.
 */
  // @ts-nocheck
  import '../styles/header.css';
  import logoPath from '../assets/logBAL.png';
  import { sesion, logout } from '../stores/sesion.js';
  import { onMount } from 'svelte';

  /** Function provided by the parent to navigate to a named page. @type {(page: string, params?: any) => void} */
  export let navigateTo;

  /** The currently active page name, used to apply active styles to nav links. @type {string} */
  export let currentPage = 'home';

  import { API } from '../lib/api.js';

  /** Whether the mobile hamburger navigation menu is open. @type {boolean} */
  let menuActive = false;

  /** Number of pending-status tickets in the user's cart, shown as a badge on the cart icon. @type {number} */
  let cartCount = 0;

  /** Full list of airports fetched on mount, used as the source for the search autocomplete. @type {any[]} */
  let aeropuertos = [];

  /** Current text value of the search input shared between desktop and mobile inputs. @type {string} */
  let searchQuery = '';

  /** Filtered subset of airports matching the current searchQuery, capped at 8 results. @type {any[]} */
  let searchResults = [];

  /** Whether the autocomplete dropdown should be visible. @type {boolean} */
  let showSearchResults = false;

  /** Bound reference to the desktop search input element, used for focus management. @type {HTMLInputElement|null} */
  let searchInputDesktop = null;

  /** Bound reference to the mobile search input element, used for focus management. @type {HTMLInputElement|null} */
  let searchInputMobile = null;

  /** Whether an API flight-search request is currently in progress. @type {boolean} */
  let searching = false;

  /**
   * On mount: fetches all airports for the search autocomplete, updates the cart count,
   * and registers a document click listener to close the dropdown when clicking outside.
   * Returns a cleanup function that removes the listener on component destruction.
   * @async
   * @returns {Promise<void>}
   */
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

  /**
   * Fetches the user's reservations from the API and counts all tickets that have
   * estadoReservaId === 1 (pending). Sets cartCount to 0 if the user is not logged in
   * or if the request fails.
   * @async
   * @returns {Promise<void>}
   */
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

  // Updates cart count whenever the session becomes truthy.
  $: if ($sesion) actualizarCartCount();

  // Resets cart count to zero when the user logs out.
  $: if (!$sesion) cartCount = 0;

  /**
   * Closes the search autocomplete dropdown when the user clicks anywhere outside
   * an element with the broom-header__search class.
   * @param {MouseEvent} e - The document-level click event.
   */
  function handleClickOutside(e) {
    const isInSearch = e.target.closest('.broom-header__search');
    if (!isInSearch) showSearchResults = false;
  }

  /**
   * Filters the airports list against the current searchQuery (city, name, code, or country)
   * and updates searchResults. Hides the dropdown if the query is shorter than 1 character.
   */
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

  /**
   * Calls the general flight search API endpoint with the provided query text, then navigates
   * to the vuelos page passing the results. If the query is shorter than 2 characters, returns
   * early. On network error it still navigates with an empty results array. Resets the search
   * input and closes the mobile menu on completion.
   * @async
   * @param {string} queryText - The search text to send to the API.
   * @returns {Promise<void>}
   */
  async function buscarYNavegar(queryText) {
    if (!queryText || queryText.trim().length < 2) return;
    showSearchResults = false;
    searching = true;
    const q = queryText.trim();
    try {
      const res = await fetch(`${API}/api/vuelos/busqueda-general?query=${encodeURIComponent(q)}`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error('Error en busqueda');
      const vuelos = await res.json();
      navigateTo('vuelos', {
        fromGlobalSearch: true,
        globalSearchQuery: q,
        globalSearchResults: vuelos
      });
    } catch (err) {
      console.error('Error en busqueda global:', err);
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

  /**
   * Selects an airport from the autocomplete dropdown and triggers a flight search using
   * that airport's city name as the query.
   * @param {any} aeropuerto - The airport object selected from the dropdown.
   */
  function selectSearchResult(aeropuerto) {
    showSearchResults = false;
    buscarYNavegar(aeropuerto.ciudad);
  }

  /**
   * Handles keyboard events on the search input. Enter triggers a search using the first
   * result or the raw query. Escape closes the dropdown.
   * @param {KeyboardEvent} e - The keydown event from the search input.
   */
  function handleSearchKeydown(e) {
    if (e.key === 'Enter') {
      if (searchResults.length > 0) selectSearchResult(searchResults[0]);
      else if (searchQuery.trim().length >= 2) buscarYNavegar(searchQuery);
    } else if (e.key === 'Escape') {
      showSearchResults = false;
    }
  }

  /**
   * Toggles the mobile hamburger navigation menu open or closed.
   */
  function toggleMenu() { menuActive = !menuActive; }

  /**
   * Closes the mobile menu and navigates to the given page using a full page reload via
   * window.location.href so that all component state is reset.
   * @param {string} page - The route segment to navigate to (e.g. 'home', 'admin').
   */
  function handleNavigation(page) {
    menuActive = false;
    window.location.href = '/' + page;
  }

  /**
   * Calls the logout store action and then redirects the browser to the home page.
   * @async
   * @returns {Promise<void>}
   */
  async function handleLogout() {
    await logout();
    window.location.href = '/home';
  }

  // True when a session object is present in the store.
  $: isLoggedIn   = !!$sesion;

  // True when the logged-in user has the Administrador role.
  $: isAdmin      = $sesion?.rolNombre === 'Administrador';

  // True when the logged-in user has rolId 3 (Webservice / agency).
  $: isWebservice = $sesion?.rolId === 3;
</script>

<!-- Cabecera principal con logo, navegacion inline, buscador de vuelos, carrito y menu -->
<header class="broom-header">
  <div class="broom-header__container">

    <!-- Logo de la aerolinea con enlace a inicio -->
    <div class="broom-header__logo">
      <a href="/home" on:click|preventDefault={() => handleNavigation('home')}>
        <img src={logoPath} alt="Broom AirLine" class="broom-header__logo-img">
      </a>
    </div>

    <!-- Navegacion inline visible en desktop con enlaces segun rol -->
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
      {#if isWebservice}
        <a href="/mi-agencia" class="broom-header__inline-link broom-header__inline-link--admin"
          class:broom-header__inline-link--active={currentPage === 'mi-agencia'}
          on:click|preventDefault={() => handleNavigation('mi-agencia')}>
          ✦ Mi Agencia
        </a>
      {/if}
    </nav>

    <!-- Buscador de vuelos con autocompletado para desktop -->
    <div class="broom-header__search broom-header__search--desktop">
      <input
        bind:this={searchInputDesktop}
        bind:value={searchQuery}
        on:input={onSearchInput}
        on:keydown={handleSearchKeydown}
        on:focus={() => { if (searchResults.length > 0) showSearchResults = true; }}
        type="text"
        class="broom-header__search-input"
        placeholder={searching ? 'Buscando vuelos...' : 'Buscar vuelos: ciudad, pais, codigo...'}
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

    <!-- Botones de accion: carrito de boletos, perfil y boton hamburguesa movil -->
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
        aria-label="Menu de navegacion"
        aria-expanded={menuActive}>
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
        <span class="broom-header__hamburger"></span>
      </button>
    </div>
  </div>

  <!-- Menu de navegacion movil desplegable con buscador y enlaces de sesion -->
  <nav
    class="broom-header__nav"
    class:broom-header__nav--active={menuActive}
    aria-label="Navegacion principal">

    <!-- Buscador de vuelos dentro del menu movil -->
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

    <!-- Lista de enlaces del menu movil con opciones condicionales por rol y sesion -->
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
            ✦ Panel Administracion
          </a>
        </li>
      {/if}
      {#if isWebservice}
        <li class="broom-header__nav-item">
          <a href="/mi-agencia" class="broom-header__nav-link broom-header__nav-link--admin"
            class:broom-header__nav-link--active={currentPage === 'mi-agencia'}
            on:click|preventDefault={() => handleNavigation('mi-agencia')}>
            ✦ Mi Agencia
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
            Cerrar Sesion
          </a>
        </li>
      {:else}
        <li class="broom-header__nav-item">
          <a href="/login" class="broom-header__nav-link"
            class:broom-header__nav-link--active={currentPage === 'login'}
            on:click|preventDefault={() => handleNavigation('login')}>
            Iniciar Sesion
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

<!-- Espaciador para compensar el alto del header fijo -->
<div class="broom-header-spacer"></div>
