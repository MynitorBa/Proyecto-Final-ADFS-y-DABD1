<script>
/**
 * @file Header.svelte
 * @description Cabecera principal del sitio para Broom AirLine. Renderiza el logo, enlaces de navegacion
 * en linea para escritorio, una barra de busqueda de vuelos con autocompletado (variantes desktop y movil),
 * un icono de carrito con contador de boletos pendientes, un icono de perfil y un menu hamburguesa
 * para navegacion movil. Se integra con el almacen de sesion para mostrar enlaces segun el rol
 * (panel admin, panel agencia, reservas). Realiza navegacion de pagina completa usando window.location.href
 * para asegurar que los almacenes y el estado se reinicien en cada cambio de ruta.
 */
  // @ts-nocheck
  import '../styles/header.css';
  import logoPath from '../assets/logBAL.png';
  import { sesion, logout } from '../stores/sesion.js';
  import { onMount } from 'svelte';

  /** Funcion proporcionada por el padre para navegar a una pagina nombrada. @type {(page: string, params?: any) => void} */
  export let navigateTo;

  /** El nombre de la pagina actualmente activa, usado para aplicar estilos activos a los enlaces de navegacion. @type {string} */
  export let currentPage = 'home';

  import { API } from '../lib/api.js';

  /** Indica si el menu de navegacion hamburguesa movil esta abierto. @type {boolean} */
  let menuActive = false;

  /** Numero de boletos en estado pendiente en el carrito del usuario, mostrado como insignia en el icono del carrito. @type {number} */
  let cartCount = 0;

  /** Lista completa de aeropuertos obtenida al montar, usada como fuente para el autocompletado de busqueda. @type {any[]} */
  let aeropuertos = [];

  /** Valor de texto actual del campo de busqueda compartido entre los inputs de desktop y movil. @type {string} */
  let searchQuery = '';

  /** Subconjunto filtrado de aeropuertos que coinciden con el searchQuery actual, limitado a 8 resultados. @type {any[]} */
  let searchResults = [];

  /** Indica si el dropdown de autocompletado debe estar visible. @type {boolean} */
  let showSearchResults = false;

  /** Referencia vinculada al elemento input de busqueda en desktop, usada para gestion del foco. @type {HTMLInputElement|null} */
  let searchInputDesktop = null;

  /** Referencia vinculada al elemento input de busqueda en movil, usada para gestion del foco. @type {HTMLInputElement|null} */
  let searchInputMobile = null;

  /** Indica si una solicitud de busqueda de vuelos a la API esta en progreso. @type {boolean} */
  let searching = false;

  /**
   * Al montar: obtiene todos los aeropuertos para el autocompletado de busqueda, actualiza el conteo del carrito
   * y registra un listener de clic en el documento para cerrar el dropdown al hacer clic fuera.
   * Devuelve una funcion de limpieza que elimina el listener al destruir el componente.
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
   * Obtiene las reservaciones del usuario desde la API y cuenta todos los boletos con
   * estadoReservaId igual a 1 (pendiente). Establece cartCount en 0 si el usuario no
   * ha iniciado sesion o si la solicitud falla.
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

  // Actualiza el conteo del carrito cada vez que la sesion se vuelve verdadera.
  $: if ($sesion) actualizarCartCount();

  // Reinicia el conteo del carrito a cero cuando el usuario cierra sesion.
  $: if (!$sesion) cartCount = 0;

  /**
   * Cierra el dropdown de autocompletado de busqueda cuando el usuario hace clic
   * fuera de un elemento con la clase broom-header__search.
   * @param {MouseEvent} e - El evento de clic a nivel de documento.
   */
  function handleClickOutside(e) {
    const isInSearch = e.target.closest('.broom-header__search');
    if (!isInSearch) showSearchResults = false;
  }

  /**
   * Filtra aeropuertos y genera acciones rapidas de busqueda por codigo de vuelo/avion
   * y por codigo de reservacion. Cada item del dropdown tiene un campo `tipo`:
   * 'aeropuerto', 'vuelo' o 'reservacion'.
   */
  function onSearchInput() {
    const q = searchQuery.toLowerCase().trim();
    if (q.length < 1) { searchResults = []; showSearchResults = false; return; }

    const resultados = [];

    // Aeropuertos matching (max 5)
    const matchAeropuertos = aeropuertos.filter(a =>
      a.ciudad?.toLowerCase().includes(q) ||
      a.nombre?.toLowerCase().includes(q) ||
      a.codigo?.toLowerCase().includes(q) ||
      a.pais?.toLowerCase().includes(q)
    ).slice(0, 5).map(a => ({ tipo: 'aeropuerto', data: a }));
    resultados.push(...matchAeropuertos);

    // Accion rapida: buscar vuelo por codigo (codigo avion, numero vuelo)
    if (q.length >= 2) {
      resultados.push({ tipo: 'vuelo', query: searchQuery.trim() });
    }

    // Accion rapida: buscar reservacion (solo si hay sesion activa)
    if (q.length >= 2 && $sesion) {
      resultados.push({ tipo: 'reservacion', query: searchQuery.trim().toUpperCase() });
    }

    searchResults = resultados;
    showSearchResults = resultados.length > 0;
  }

  /**
   * Llama al endpoint general de busqueda de vuelos de la API con el texto de consulta proporcionado,
   * luego navega a la pagina de vuelos pasando los resultados. Si la consulta tiene menos de 2 caracteres,
   * retorna de inmediato. En caso de error de red igual navega con un arreglo de resultados vacio.
   * Reinicia el input de busqueda y cierra el menu movil al finalizar.
   * @async
   * @param {string} queryText - El texto de busqueda a enviar a la API.
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
   * Selecciona un item del dropdown de autocompletado. Segun el tipo:
   * - 'aeropuerto': busca vuelos por ciudad del aeropuerto
   * - 'vuelo': busca vuelos usando el query directo (codigo avion / numero vuelo)
   * - 'reservacion': navega a Mis Reservas con el codigo pre-buscado
   * @param {any} result - Item del dropdown con campo `tipo` y datos asociados.
   */
  function selectSearchResult(result) {
    showSearchResults = false;
    if (result.tipo === 'aeropuerto') {
      buscarYNavegar(result.data.ciudad);
    } else if (result.tipo === 'vuelo') {
      buscarYNavegar(result.query);
    } else if (result.tipo === 'reservacion') {
      searchQuery = '';
      menuActive = false;
      navigateTo('reservas', { buscarCodigo: result.query });
    }
  }

  /**
   * Maneja eventos de teclado en el input de busqueda. Enter activa una busqueda usando el primer
   * resultado o la consulta directa. Escape cierra el dropdown.
   * @param {KeyboardEvent} e - El evento keydown del input de busqueda.
   */
  function handleSearchKeydown(e) {
    if (e.key === 'Enter') {
      // Prefer first aeropuerto result; fallback to direct query
      const firstAeropuerto = searchResults.find(r => r.tipo === 'aeropuerto');
      if (firstAeropuerto) selectSearchResult(firstAeropuerto);
      else if (searchResults.length > 0) selectSearchResult(searchResults[0]);
      else if (searchQuery.trim().length >= 2) buscarYNavegar(searchQuery);
    } else if (e.key === 'Escape') {
      showSearchResults = false;
    }
  }

  /**
   * Alterna el menu de navegacion hamburguesa movil entre abierto y cerrado.
   */
  function toggleMenu() { menuActive = !menuActive; }

  /**
   * Cierra el menu movil y navega a la pagina indicada mediante una recarga completa con
   * window.location.href para que todo el estado del componente se reinicie.
   * @param {string} page - El segmento de ruta al que navegar (por ejemplo, 'home', 'admin').
   */
  function handleNavigation(page) {
    menuActive = false;
    window.location.href = '/' + page;
  }

  /**
   * Llama a la accion de logout del almacen y luego redirige el navegador a la pagina de inicio.
   * @async
   * @returns {Promise<void>}
   */
  async function handleLogout() {
    await logout();
    window.location.href = '/home';
  }

  // Verdadero cuando hay un objeto de sesion presente en el almacen.
  $: isLoggedIn   = !!$sesion;

  // Verdadero cuando el usuario autenticado tiene el rol Administrador.
  $: isAdmin      = $sesion?.rolNombre === 'Administrador';

  // Verdadero cuando el usuario autenticado tiene rolId 3 (Webservice / agencia).
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
          const firstAeropuerto = searchResults.find(r => r.tipo === 'aeropuerto');
          if (firstAeropuerto) selectSearchResult(firstAeropuerto);
          else if (searchResults.length > 0) selectSearchResult(searchResults[0]);
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
          {#each searchResults as r}
            <li class="broom-header__search-result-item">
              {#if r.tipo === 'aeropuerto'}
                <button type="button" class="broom-header__search-result-btn" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code">{r.data.codigo}</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">{r.data.ciudad}</span>
                    <span class="broom-header__search-result-detail">{r.data.nombre} · {r.data.pais}</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {:else if r.tipo === 'vuelo'}
                <button type="button" class="broom-header__search-result-btn broom-header__search-result-btn--action" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code broom-header__search-result-code--action">✈</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">Buscar vuelo: {r.query}</span>
                    <span class="broom-header__search-result-detail">Por numero de vuelo o codigo de avion</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {:else if r.tipo === 'reservacion'}
                <button type="button" class="broom-header__search-result-btn broom-header__search-result-btn--action" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code broom-header__search-result-code--action">📋</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">Ver reservacion: {r.query}</span>
                    <span class="broom-header__search-result-detail">Buscar en Mis Reservas</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {/if}
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
          const firstAeropuerto = searchResults.find(r => r.tipo === 'aeropuerto');
          if (firstAeropuerto) selectSearchResult(firstAeropuerto);
          else if (searchResults.length > 0) selectSearchResult(searchResults[0]);
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
          {#each searchResults as r}
            <li class="broom-header__search-result-item">
              {#if r.tipo === 'aeropuerto'}
                <button type="button" class="broom-header__search-result-btn" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code">{r.data.codigo}</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">{r.data.ciudad}</span>
                    <span class="broom-header__search-result-detail">{r.data.nombre} · {r.data.pais}</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {:else if r.tipo === 'vuelo'}
                <button type="button" class="broom-header__search-result-btn broom-header__search-result-btn--action" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code broom-header__search-result-code--action">✈</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">Buscar vuelo: {r.query}</span>
                    <span class="broom-header__search-result-detail">Por numero de vuelo o codigo de avion</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {:else if r.tipo === 'reservacion'}
                <button type="button" class="broom-header__search-result-btn broom-header__search-result-btn--action" on:click={() => selectSearchResult(r)}>
                  <span class="broom-header__search-result-code broom-header__search-result-code--action">📋</span>
                  <div class="broom-header__search-result-info">
                    <span class="broom-header__search-result-city">Ver reservacion: {r.query}</span>
                    <span class="broom-header__search-result-detail">Buscar en Mis Reservas</span>
                  </div>
                  <span class="broom-header__search-result-arrow">→</span>
                </button>
              {/if}
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
