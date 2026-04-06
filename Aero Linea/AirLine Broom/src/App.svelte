<script>
  /**
   * @file App.svelte
   * @description Root component of the AirLine Broom application. Owns the
   * client-side routing logic, guards protected and role-restricted routes,
   * persists search parameters across page reloads via sessionStorage, and
   * decides which layout (header variant, footer presence) to render for each
   * page. All navigation in the SPA flows through navigateTo().
   */

  // @ts-nocheck

  import { onMount } from 'svelte';
  import { sesion, cargarSesion } from './stores/sesion.js';
  import Header from './components/Header.svelte';
  import HeaderSimple from './components/HeaderSimple.svelte';
  import Footer from './components/Footer.svelte';
  import Loading from './components/Loading.svelte';
  import FlightNotification from './components/FlightNotification.svelte';

  import Home from './pages/Home.svelte';
  import Vuelos from './pages/Vuelos.svelte';
  import ResultadosBusqueda from './pages/ResultadosBusqueda.svelte';
  import Comfirmacion from './pages/Confirmacion.svelte';
  import Carrito from './pages/Carrito.svelte';
  import Checkout from './pages/Checkout.svelte';
  import DatosPasajeros from './pages/DatosPasajeros.svelte';
  import SeleccionAsientos from './pages/Seleccionasientos.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import Profile from './pages/Profile.svelte';
  import MisReservas from './pages/MisReservas.svelte';
  import Admin from './pages/Admin.svelte';
  import MiAgencia from './pages/MiAgencia.svelte';
  import InformacionAsientos from './pages/InformacionAsientos.svelte';
  import InformacionSeguridad from './pages/InformacionSeguridad.svelte';
  import AccesoDenegado from './pages/Accesodenegado.svelte';

  import Contactanos from './pages/Contactanos.svelte';
  import CentroAyuda from './pages/CentroAyuda.svelte';
  import PreguntasFrecuentes from './pages/PreguntasFrecuentes.svelte';
  import Privacidad from './pages/Privacidad.svelte';
  import Terminos from './pages/Terminos.svelte';
  import Cookies from './pages/Cookies.svelte';
  import PoliticaCancelacion from './pages/PoliticaCancelacion.svelte';
  import SobreNosotros from './pages/SobreNosotros.svelte';

  import './app.css';

  /** sessionStorage key used to persist flight search parameters between page reloads. @type {string} */
  const SS_SEARCH_PARAMS = 'broom_searchParams';

  /** sessionStorage key used to persist the name of the currently active page. @type {string} */
  const SS_PAGE          = 'broom_currentPage';

  /** Controls the visibility of the full-screen loading splash shown at startup. @type {boolean} */
  let isLoading = true;

  /** Set to true once the initial session verification request to the backend has resolved. @type {boolean} */
  let sesionCargada = false;

  /** Name of the page currently rendered inside the router outlet. @type {string} */
  let currentPage = 'home';

  /** Airport object forwarded from FlightNotification to pre-fill the Home search widget. @type {object|null} */
  let suggestedAeropuerto = null;

  /** ID of the flight whose detail view is being displayed. @type {number|null} */
  let currentFlightId = null;

  /** Parameters from the most recent flight search, passed down to Vuelos and related pages. @type {object|null} */
  let searchParams = null;

  /** Array of reservation objects populated after a successful checkout, forwarded to Confirmacion. @type {Array} */
  let reservacionesConfirmadas = [];

  /** Array of invoice objects populated after a successful checkout, forwarded to Confirmacion. @type {Array} */
  let facturasConfirmadas = [];

  /**
   * Numeric timestamp that is renewed on every navigation to force a full
   * re-mount (keyed component) of pages that must reset their internal state
   * when revisited.
   * @type {number}
   */
  let pageKey = Date.now();

  /** Seat-selection payload containing an array of flight groups passed to SeleccionAsientos. @type {Array|null} */
  let asientosData = null;

  /**
   * Route names that require an authenticated session. Unauthenticated users
   * attempting to access these pages are redirected to the login page.
   * @type {string[]}
   */
  const paginasProtegidas = ['profile','admin','reservas','checkout','datos-pasajeros','carrito','seleccion-asientos','mi-agencia'];

  /**
   * Route names exclusively accessible to users with the Administrador role.
   * Any other authenticated role is redirected to the access-denied page.
   * @type {string[]}
   */
  const paginasSoloAdmin      = ['admin'];

  /**
   * Route names exclusively accessible to users with rolId === 3 (Webservice/Agency).
   * Any other authenticated role is redirected to the access-denied page.
   * @type {string[]}
   */
  const paginasSoloWebservice = ['mi-agencia'];

  /**
   * Route names for static informational pages such as legal notices,
   * help center and contact — no authentication required.
   * @type {string[]}
   */
  const infoPages = [
    'contactanos','centro-ayuda','preguntas-frecuentes',
    'privacidad','terminos','cookies','politica-cancelacion','sobre-nosotros',
  ];

  /**
   * Serializes the given search parameters object to sessionStorage so that
   * they survive a hard reload. Passing null removes the stored entry.
   * @param {object|null} params - Search parameters to persist, or null to clear them.
   */
  function saveSearchParams(params) {
    try {
      if (params) sessionStorage.setItem(SS_SEARCH_PARAMS, JSON.stringify(params));
      else        sessionStorage.removeItem(SS_SEARCH_PARAMS);
    } catch (e) { }
  }

  /**
   * Reads and parses the search parameters object previously saved in
   * sessionStorage. Returns null if the entry is missing or cannot be parsed.
   * @returns {object|null} The restored search parameters or null.
   */
  function loadSearchParams() {
    try {
      const raw = sessionStorage.getItem(SS_SEARCH_PARAMS);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  /**
   * Writes the given page name into sessionStorage so that the active route
   * can be referenced by other code that inspects storage directly.
   * @param {string} page - Name of the page to persist.
   */
  function saveCurrentPage(page) {
    try { sessionStorage.setItem(SS_PAGE, page); } catch {}
  }

  onMount(async () => {
    actualizarPaginaDesdeURL();
    window.addEventListener('popstate', actualizarPaginaDesdeURL);

    if (currentPage === 'vuelos' && !searchParams) {
      const guardados = loadSearchParams();
      if (guardados) searchParams = guardados;
    }

    await cargarSesion();
    sesionCargada = true;

    setTimeout(() => { isLoading = false; }, 3000);

    return () => {
      window.removeEventListener('popstate', actualizarPaginaDesdeURL);
    };
  });

  /** Tracks whether the purchase flow has been completed to block re-entry into booking pages. @type {boolean} */
  let pagoCompletado = false;

  /**
   * Ordered list of route names that form the ticket-purchase flow.
   * Used to prevent back-navigation into this flow after payment is confirmed.
   * @type {string[]}
   */
  const paginasFlujoCompra = ['vuelos', 'datos-pasajeros', 'seleccion-asientos', 'carrito', 'checkout'];

  /**
   * Reads window.location.pathname to determine the current page and updates
   * the component state accordingly. Enforces two guards: if the payment flow
   * has been completed the user is redirected to home when trying to re-enter
   * any purchase step, and direct URL access to /confirmacion is blocked unless
   * there are confirmed reservations or invoices available in memory.
   */
  function actualizarPaginaDesdeURL() {
    const path = window.location.pathname.slice(1) || 'home';

    if (pagoCompletado && (paginasFlujoCompra.includes(path) || path === 'confirmacion')) {
      currentPage = 'home';
      pageKey = Date.now();
      window.history.replaceState({}, '', '/home');
      return;
    }

    if (path === 'confirmacion' && reservacionesConfirmadas.length === 0 && facturasConfirmadas.length === 0) {
      currentPage = 'home';
      pageKey = Date.now();
      window.history.replaceState({}, '', '/home');
      return;
    }

    currentPage = path;
    pageKey = Date.now();

    if (path === 'vuelos' && !searchParams) {
      const guardados = loadSearchParams();
      if (guardados) searchParams = guardados;
    }
  }

  /**
   * Evaluates route-protection rules for the given page name against the
   * current session state. Redirects unauthenticated users to login, users
   * without the required role to acceso-denegado, and returns the original
   * page name when access is permitted. If the session has not finished loading
   * the page name is returned unchanged to avoid premature redirects.
   * @param {string} page - Name of the route to evaluate.
   * @returns {string} The resolved route name that the user is allowed to view.
   */
  function resolverPagina(page) {
    if (!sesionCargada) return page;
    const sesionActual = $sesion;
    if (paginasProtegidas.includes(page) && !sesionActual) return 'login';
    if (paginasSoloAdmin.includes(page) && sesionActual?.rolNombre !== 'Administrador') return 'acceso-denegado';
    if (paginasSoloWebservice.includes(page) && sesionActual?.rolId !== 3) return 'acceso-denegado';
    return page;
  }

  $: if (sesionCargada && $sesion !== null) { /* Re-evalua la pagina cuando la sesion termina de cargar */
    const paginaResuelta = resolverPagina(currentPage);
    if (paginaResuelta !== currentPage) {
      currentPage = paginaResuelta;
      window.history.replaceState({}, '', '/' + paginaResuelta);
    }
  }

  /**
   * Performs programmatic SPA navigation to the specified page. Runs route
   * protection via resolverPagina, updates browser history, persists the new
   * page to sessionStorage, and distributes any accompanying data to the
   * correct state variables depending on the destination page:
   *   - detalle-vuelo  : stores the flight ID in currentFlightId.
   *   - vuelos         : stores search result data or a busquedaId in searchParams and persists them.
   *   - resultados-busqueda : stores generic search data in searchParams.
   *   - seleccion-asientos  : stores the seat-group array in asientosData.
   *   - confirmacion   : saves reservations and invoices, marks pagoCompletado = true, clears searchParams.
   *   - home (with airport) : sets suggestedAeropuerto, clears searchParams and pagoCompletado.
   *   - all other pages : clears searchParams unless they are part of the purchase flow.
   * Scrolls the window to the top after every navigation.
   * @param {string} page - Name of the destination route.
   * @param {object|null} [data=null] - Optional payload whose shape varies per destination page.
   */
  function navigateTo(page, data = null) {
    const paginaFinal = resolverPagina(page);
    currentPage = paginaFinal;
    pageKey = Date.now();

    window.history.pushState({}, '', `/${paginaFinal}`);
    saveCurrentPage(paginaFinal);

    if (paginaFinal === 'detalle-vuelo') {
      currentFlightId = data;
    }

    if (paginaFinal === 'vuelos') {
      if (data?.fromGlobalSearch) {
        searchParams = {
          fromGlobalSearch: true,
          globalSearchQuery: data.globalSearchQuery,
          globalSearchResults: data.globalSearchResults
        };
      } else if (data?.searchData) {
        searchParams = {
          vuelosIda:    data.vuelosIda    ?? [],
          vuelosVuelta: data.vuelosVuelta ?? [],
          searchData:   data.searchData
        };
      } else if (data?.busquedaId) {
        searchParams = { busquedaId: data.busquedaId };
      } else {
        searchParams = data;
      }
      saveSearchParams(searchParams);

    } else if (paginaFinal === 'resultados-busqueda') {
      searchParams = data;

    } else if (paginaFinal === 'seleccion-asientos') {
      asientosData = data;

    } else if (paginaFinal === 'datos-pasajeros') {

    } else if (paginaFinal === 'confirmacion') {
      if (data?.reservaciones) reservacionesConfirmadas = data.reservaciones;
      if (data?.facturas)      facturasConfirmadas      = data.facturas;
      pagoCompletado = true;
      searchParams = null;
      sessionStorage.removeItem(SS_SEARCH_PARAMS);
      window.history.replaceState({ paid: true }, '', '/confirmacion');

    } else if (paginaFinal === 'home' && data?.suggestedAeropuerto) {
      suggestedAeropuerto = data.suggestedAeropuerto;
      searchParams = null;
      pagoCompletado = false;

    } else if (!['vuelos','datos-pasajeros','seleccion-asientos','carrito','checkout','confirmacion'].includes(paginaFinal)) {
      searchParams = null;
    }

    if (paginaFinal === 'home') pagoCompletado = false;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Receives an airport suggestion dispatched by the FlightNotification component.
   * Stores the airport data in suggestedAeropuerto so the Home search widget can
   * pre-fill the destination field. If the user is not already on the home page
   * navigateTo('home') is called first, then after a short delay the page scrolls
   * to the search section to draw the user's attention.
   * @param {object} aeropuertoData - Airport data object to pre-fill in the search widget.
   */
  function handleDestinationSuggestion(aeropuertoData) {
    suggestedAeropuerto = aeropuertoData;
    if (currentPage !== 'home') navigateTo('home');
    setTimeout(() => {
      const searchSection = document.querySelector('.broom-home__search-section');
      if (searchSection) searchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  }

  /**
   * Pages in the purchase flow that display the simplified header instead of
   * the full navigation header.
   * @type {string[]}
   */
  const simpleHeaderPages = ['vuelos','carrito','datos-pasajeros','seleccion-asientos','checkout','confirmacion'];

  /**
   * Pages that suppress the Footer entirely, including the purchase flow and
   * authenticated account pages.
   * @type {string[]}
   */
  const noFooterPages = ['profile','admin','login','register','acceso-denegado','reservas','mi-agencia',...simpleHeaderPages];

  /**
   * Pages that render with no header at all (standalone full-page layouts).
   * @type {string[]}
   */
  const noHeaderPages = ['login','register'];

  $: useSimpleHeader = simpleHeaderPages.includes(currentPage); /* True cuando la pagina usa el header simplificado */
  $: showHeader      = !noHeaderPages.includes(currentPage);    /* True cuando la pagina debe mostrar el Header */
  $: showFooter      = !noFooterPages.includes(currentPage);    /* True cuando la pagina debe mostrar el Footer */
</script>
