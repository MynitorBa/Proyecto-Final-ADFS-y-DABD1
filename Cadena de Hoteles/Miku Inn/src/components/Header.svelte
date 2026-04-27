<script>
  /**
   * @file Header.svelte
   * @description Barra de navegacion global de Miku Inn. Incluye logo, menu de
   * paginas, barra de busqueda con logica de filtrado contra el backend,
   * menu de usuario con dropdown y soporte de menu movil.
   */

  import { createEventDispatcher } from 'svelte';
  import logo from '../assets/mikuinn-logo.png';
  import '../styles/header.css';

  /** Dispatcher para emitir eventos hacia el componente padre (logout). */
  const dispatch = createEventDispatcher();

  /** Funcion de navegacion recibida desde App. @type {Function} */
  export let navigateTo;

  /** Pagina actualmente activa, usada para resaltar el link correspondiente. @type {string} */
  export let currentPage = 'home';

  /** Indica si hay un usuario con sesion iniciada. @type {boolean} */
  export let isLoggedIn = false;

  /** Nombre del usuario autenticado. @type {string} */
  export let userName = '';

  /** ID del rol del usuario (2 = admin, 3 = webservice). @type {number|null} */
  export let userRolId = null;

  /** URL base del backend. @type {string} */
  import { API } from '../lib/api.js';


  /** Controla la visibilidad del dropdown de usuario. @type {boolean} */
  let showUserMenu = false;

  /** Controla la visibilidad del menu de navegacion movil. @type {boolean} */
  let showMobileMenu = false;

  /** Valor actual del input de busqueda. @type {string} */
  let searchQuery = '';

  /** Indica si el header tiene fondo solido por haber hecho scroll. @type {boolean} */
  let isScrolled = false;

  /** Bloquea el envio de busqueda mientras hay una en curso. @type {boolean} */
  let isSearching = false;

  // true si el usuario tiene rol de administrador.
  $: isAdmin      = userRolId === 2;

  // true si el usuario tiene rol de webservice.
  $: isWebservice = userRolId === 3;

  /** Actualiza isScrolled segun la posicion vertical de la pagina. */
  function handleScroll() { isScrolled = window.scrollY > 10; }

  /** Alterna el dropdown de usuario y cierra el menu movil si estaba abierto. */
  function toggleUserMenu() { showUserMenu = !showUserMenu; showMobileMenu = false; }

  /** Alterna el menu movil y cierra el dropdown de usuario si estaba abierto. */
  function toggleMobileMenu() { showMobileMenu = !showMobileMenu; showUserMenu = false; }

  /** Cierra todos los menus abiertos. */
  function closeMenus() { showUserMenu = false; showMobileMenu = false; }

  /**
   * Calcula una fecha futura en formato ISO (YYYY-MM-DD).
   * @param {number} daysFromNow - Dias a sumar desde hoy.
   * @returns {string} Fecha en formato YYYY-MM-DD.
   */
  function getFutureDate(daysFromNow) {
    const d = new Date();
    d.setDate(d.getDate() + daysFromNow);
    return d.toISOString().split('T')[0];
  }

  /**
   * Maneja el submit del formulario de busqueda. Consulta los destinos disponibles,
   * filtra por el texto ingresado, registra la busqueda (siempre una entrada por accion
   * del usuario), lanza busquedas por ciudad/pais y navega a la pagina de resultados.
   * @async
   * @param {SubmitEvent} e - Evento de submit del formulario.
   * @returns {Promise<void>}
   */
  async function handleSearch(e) {
    e.preventDefault();
    const query = searchQuery.trim();
    if (!query || isSearching) return;

    isSearching = true;
    showMobileMenu = false;

    try {
      // 1. Traer todos los destinos
      let hotelesBasicos = [];
      try {
        const res = await fetch(`${API}/destinos`, { credentials: 'include' });
        if (res.ok) hotelesBasicos = await res.json();
      } catch (_) {}

      // 2. Filtrar hoteles que coincidan con el query (nombre, ciudad, pais, direccion, descripcion)
      const qLower = query.toLowerCase();
      const ciudadesMatch = new Map();

      for (const h of hotelesBasicos) {
        const campos = [
          h.nombre || '', h.ciudad || '', h.pais || '',
          h.direccion || '', h.descripcion || ''
        ];
        const match = campos.some(c => c.toLowerCase().includes(qLower));

        if (match) {
          const key = `${h.ciudad}|||${h.pais}`;
          if (!ciudadesMatch.has(key)) {
            ciudadesMatch.set(key, { ciudad: h.ciudad, pais: h.pais });
          }
        }
      }

      const checkIn  = getFutureDate(1);
      const checkOut = getFutureDate(2);

      // 3. Registrar UNA sola entrada de busqueda para trazar la accion del usuario.
      //    Si hay ciudades coincidentes usamos la primera (datos reales de BD).
      //    Si no hay coincidencias usamos la primera ciudad del catalogo como proxy
      //    para garantizar que la busqueda siempre quede registrada en Busqueda.
      const ciudadParaRegistro = ciudadesMatch.size > 0
        ? Array.from(ciudadesMatch.values())[0]
        : hotelesBasicos.length > 0
          ? { ciudad: hotelesBasicos[0].ciudad, pais: hotelesBasicos[0].pais }
          : null;

      if (ciudadParaRegistro) {
        fetch(`${API}/busqueda`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({
            ciudad: ciudadParaRegistro.ciudad,
            pais:   ciudadParaRegistro.pais,
            fechaCheckIn:     checkIn,
            fechaCheckOut:    checkOut,
            cantidadPersonas: 1
          })
        }).catch(() => {});  // fire-and-forget; errores silenciosos
      }

      // 4. POST /busqueda?registrar=false por cada ciudad para obtener disponibilidad.
      //    Se pasa registrar=false para NO duplicar el registro ya hecho en el paso 3.
      //    Si no hay coincidencias, intentar directamente con el query como ciudad/pais.
      const ciudadesParaBuscar = ciudadesMatch.size > 0
        ? Array.from(ciudadesMatch.values())
        : [{ ciudad: query, pais: query }];

      const promesas = ciudadesParaBuscar.map(async ({ ciudad, pais }) => {
        try {
          const r = await fetch(`${API}/busqueda?registrar=false`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
              ciudad,
              pais,
              fechaCheckIn:     checkIn,
              fechaCheckOut:    checkOut,
              cantidadPersonas: 1
            })
          });
          if (r.ok) return await r.json();
          return [];
        } catch (_) { return []; }
      });

      const resultados = await Promise.all(promesas);
      const hoteles = resultados.flat();

      // 5. Deduplicar resultados por ID de hotel
      const seen = new Set();
      const hotelesUnicos = hoteles.filter(h => {
        if (seen.has(h.id)) return false;
        seen.add(h.id);
        return true;
      });

      // 6. Determinar labels de ciudad y pais para la pagina de resultados
      let ciudadLabel = query;
      let paisLabel   = '';
      if (ciudadesMatch.size >= 1) {
        const first = Array.from(ciudadesMatch.values())[0];
        if (first.ciudad) ciudadLabel = first.ciudad;
        if (first.pais)   paisLabel   = first.pais;
      }

      // 7. Navegar a resultados con los hoteles encontrados
      navigateTo('search-results', {
        pais:             paisLabel,
        ciudad:           ciudadLabel,
        fechaCheckIn:     checkIn,
        fechaCheckOut:    checkOut,
        cantidadPersonas: 1,
        hotels:           hotelesUnicos
      });

      searchQuery = '';

    } catch (err) {
      navigateTo('search-results', {
        pais: '', ciudad: query,
        fechaCheckIn: getFutureDate(1), fechaCheckOut: getFutureDate(2),
        cantidadPersonas: 1, hotels: []
      });
      searchQuery = '';
    } finally {
      isSearching = false;
    }
  }

  /** Emite el evento de logout hacia App y cierra el dropdown de usuario. */
  function handleLogout() { dispatch('logout'); showUserMenu = false; }

  /**
   * Navega a una pagina y cierra todos los menus abiertos.
   * @param {string} page - Nombre de la ruta destino.
   */
  function handleNavClick(page) { navigateTo(page); showMobileMenu = false; showUserMenu = false; }

  /**
   * Comprueba si una pagina dada es la que esta activa.
   * @param {string} page - Nombre de la ruta a comprobar.
   * @returns {boolean}
   */
  function isActivePage(page) { return currentPage === page; }

  /**
   * Cierra el dropdown de usuario al pulsar Escape dentro de el.
   * @param {KeyboardEvent} e
   */
  function handleDropdownKey(e) { if (e.key === 'Escape') closeMenus(); }
</script>

<!-- Listener de scroll para aplicar estilo de header fijo con fondo -->
<svelte:window on:scroll={handleScroll} on:click={closeMenus} />

<!-- Header principal con clase scrolled cuando el usuario ha bajado -->
<header class="header" class:scrolled={isScrolled}>
  <div class="header-container">

    <!-- Logo de la marca, lleva al inicio -->
    <button class="logo" on:click={() => handleNavClick('home')} aria-label="Ir al inicio">
      <img src={logo} alt="Miku Inn" class="logo-image" />
    </button>

    <!-- Navegacion principal visible en pantallas grandes -->
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

      <!-- Enlace al panel de administracion, solo visible para admins -->
      {#if isAdmin}
        <button class="nav-link nav-link--admin" class:active={isActivePage('administrador')} on:click={() => handleNavClick('administrador')}>
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
          Panel Admin
        </button>
      {/if}

      <!-- Enlace al portal webservice, solo visible para ese rol -->
      {#if isWebservice}
        <button class="nav-link nav-link--webservice" class:active={isActivePage('webservice')} on:click={() => handleNavClick('webservice')}>
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          Portal WS
        </button>
      {/if}
    </nav>

    <!-- Barra de busqueda rapida con spinner mientras procesa -->
    <form class="search-bar" on:submit={handleSearch}>
      <div class="search-input-wrapper">
        {#if isSearching}
          <div class="search-spinner"></div>
        {:else}
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
        {/if}
        <input
          type="text"
          bind:value={searchQuery}
          placeholder={isSearching ? 'Buscando...' : 'Buscar hoteles, destinos...'}
          class="search-input"
          aria-label="Buscar"
          disabled={isSearching}
        />
      </div>
    </form>

    <!-- Acciones de usuario: carrito, menu de cuenta y boton movil -->
    <div class="user-actions">

      <!-- Boton de acceso rapido al checkout -->
      <button class="action-button" on:click={() => handleNavClick('checkout')} aria-label="Ir al checkout">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle>
          <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
      </button>

      {#if isLoggedIn}
        <!-- Dropdown de perfil para usuarios autenticados -->
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

          <!-- Menu desplegable con las opciones del usuario -->
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
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                Mi Perfil
              </button>

              {#if isAdmin}
                <button class="dropdown-item dropdown-item--admin" role="menuitem" on:click={() => handleNavClick('administrador')}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                  Panel de Administrador
                </button>
              {/if}

              {#if isWebservice}
                <button class="dropdown-item dropdown-item--webservice" role="menuitem" on:click={() => handleNavClick('webservice')}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
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
        <!-- Botones de login y registro para usuarios no autenticados -->
        <div class="auth-buttons">
          <button class="btn-secondary" on:click={() => handleNavClick('login')}>Iniciar Sesión</button>
          <button class="btn-primary" on:click={() => handleNavClick('register')}>Registrarse</button>
        </div>
      {/if}

      <!-- Boton hamburguesa para abrir el menu en movil -->
      <button class="mobile-menu-toggle" on:click|stopPropagation={toggleMobileMenu} aria-label="Abrir menu" aria-expanded={showMobileMenu}>
        {#if showMobileMenu}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        {:else}
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
        {/if}
      </button>
    </div>
  </div>

  <!-- Menu de navegacion desplegable para pantallas pequeñas -->
  {#if showMobileMenu}
    <nav class="mobile-nav" aria-label="Menu movil">
      <!-- Barra de busqueda dentro del menu movil -->
      <form class="mobile-search" on:submit={handleSearch}>
        <div class="search-input-wrapper">
          {#if isSearching}
            <div class="search-spinner"></div>
          {:else}
            <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
          {/if}
          <input type="text" bind:value={searchQuery} placeholder={isSearching ? 'Buscando...' : 'Buscar hoteles, destinos...'} class="search-input" aria-label="Buscar" disabled={isSearching} />
        </div>
      </form>
      <!-- Links de navegacion en modo movil -->
      <div class="mobile-nav-links">
        <button class="mobile-nav-link" on:click={() => handleNavClick('home')}>Inicio</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('search-results')}>Buscar Hoteles</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('destinations')}>Destinos</button>
        <button class="mobile-nav-link" on:click={() => handleNavClick('reservations')}>Mis Reservas</button>
        {#if isAdmin}
          <button class="mobile-nav-link mobile-nav-link--admin" on:click={() => handleNavClick('administrador')}>⚙ Panel Admin</button>
        {/if}
        {#if isWebservice}
          <button class="mobile-nav-link mobile-nav-link--webservice" on:click={() => handleNavClick('webservice')}>⬡ Portal WS</button>
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
