<script>
  /**
   * @file App.svelte
   * @description Componente raiz de AirLine Broom. Gestiona el enrutamiento del lado
   * del cliente, la sesion del usuario y decide que pagina y layout se muestran
   * en cada momento.
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

  /** Clave de sessionStorage para persistir los parametros de busqueda. @type {string} */
  const SS_SEARCH_PARAMS = 'broom_searchParams';

  /** Clave de sessionStorage para persistir la pagina activa. @type {string} */
  const SS_PAGE          = 'broom_currentPage';

  /** Indica si la pantalla de carga inicial esta activa. @type {boolean} */
  let isLoading = true;

  /** Bandera que se activa cuando la verificacion inicial de sesion termina. @type {boolean} */
  let sesionCargada = false;

  /** Pagina activa en este momento. @type {string} */
  let currentPage = 'home';

  /** Sugerencia de aeropuerto que puede llegar desde otras secciones. @type {object|null} */
  let suggestedAeropuerto = null;

  /** ID del vuelo seleccionado para ver su detalle. @type {number|null} */
  let currentFlightId = null;

  /** Parametros de la ultima busqueda realizada. @type {object|null} */
  let searchParams = null;

  /** Lista de reservaciones confirmadas tras completar el pago. @type {Array} */
  let reservacionesConfirmadas = [];

  /** Lista de facturas confirmadas tras completar el pago. @type {Array} */
  let facturasConfirmadas = [];

  /**
   * Llave que cambia con cada navegacion para forzar el re-mount
   * de paginas que necesitan reiniciarse por completo.
   * @type {number}
   */
  let pageKey = Date.now();

  /** Datos para la seleccion de asientos, contiene grupos de vuelo. @type {Array|null} */
  let asientosData = null;

  /**
   * Rutas que requieren sesion activa para acceder.
   * @type {string[]}
   */
  const paginasProtegidas = ['profile','admin','reservas','checkout','datos-pasajeros','carrito','seleccion-asientos','mi-agencia'];

  /**
   * Rutas accesibles unicamente para el rol Administrador.
   * @type {string[]}
   */
  const paginasSoloAdmin      = ['admin'];

  /**
   * Rutas accesibles unicamente para el rol Webservice.
   * @type {string[]}
   */
  const paginasSoloWebservice = ['mi-agencia'];

  /**
   * Rutas de paginas informativas de la aplicacion.
   * @type {string[]}
   */
  const infoPages = [
    'contactanos','centro-ayuda','preguntas-frecuentes',
    'privacidad','terminos','cookies','politica-cancelacion','sobre-nosotros',
  ];

  /**
   * Guarda los parametros de busqueda en sessionStorage.
   * @param {object|null} params - Parametros a persistir.
   */
  function saveSearchParams(params) {
    try {
      if (params) sessionStorage.setItem(SS_SEARCH_PARAMS, JSON.stringify(params));
      else        sessionStorage.removeItem(SS_SEARCH_PARAMS);
    } catch (e) { }
  }

  /**
   * Recupera los parametros de busqueda desde sessionStorage.
   * @returns {object|null} Parametros guardados o null si no existen.
   */
  function loadSearchParams() {
    try {
      const raw = sessionStorage.getItem(SS_SEARCH_PARAMS);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  /**
   * Persiste la pagina activa en sessionStorage.
   * @param {string} page - Nombre de la pagina a guardar.
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

  /** Indica si el pago del flujo de compra actual fue completado. @type {boolean} */
  let pagoCompletado = false;

  /**
   * Rutas que forman parte del flujo de compra de boletos.
   * @type {string[]}
   */
  const paginasFlujoCompra = ['vuelos', 'datos-pasajeros', 'seleccion-asientos', 'carrito', 'checkout'];

  /**
   * Sincroniza la pagina activa con el path de la URL actual.
   * Protege el flujo de compra y la pagina de confirmacion contra acceso invalido.
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
   * Aplica las reglas de proteccion de rutas segun el estado de sesion y rol del usuario.
   * @param {string} page - Nombre de la ruta a resolver.
   * @returns {string} La ruta final permitida para el usuario actual.
   */
  function resolverPagina(page) {
    if (!sesionCargada) return page;
    const sesionActual = $sesion;
    if (paginasProtegidas.includes(page) && !sesionActual) return 'login';
    if (paginasSoloAdmin.includes(page) && sesionActual?.rolNombre !== 'Administrador') return 'acceso-denegado';
    if (paginasSoloWebservice.includes(page) && sesionActual?.rolId !== 3) return 'acceso-denegado';
    return page;
  }

  // Re-evalua la pagina actual una vez que la sesion termina de cargar.
  // Esto cubre el caso de navegacion directa por URL (ej: /mi-agencia, /admin)
  // donde actualizarPaginaDesdeURL no pasa por resolverPagina.
  $: if (sesionCargada && $sesion !== null) {
    const paginaResuelta = resolverPagina(currentPage);
    if (paginaResuelta !== currentPage) {
      currentPage = paginaResuelta;
      window.history.replaceState({}, '', '/' + paginaResuelta);
    }
  }

  /**
   * Navega programaticamente a una pagina y actualiza el historial del navegador.
   * Aplica proteccion de rutas y gestiona los datos de cada pagina del flujo.
   * @param {string} page - Nombre de la ruta destino.
   * @param {object|null} [data=null] - Datos opcionales que se pasan a la nueva pagina.
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
      // data = array de grupos de vuelo: [{ vueloId, numeroVuelo, avionModelo, avionMarca, clase, boletos[] }, ...]
      asientosData = data;

    } else if (paginaFinal === 'datos-pasajeros') {
      // No tocar searchParams

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
   * Maneja la sugerencia de un aeropuerto destino desde FlightNotification.
   * Navega al inicio y hace scroll al buscador si no se esta en home.
   * @param {object} aeropuertoData - Datos del aeropuerto sugerido.
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
   * Paginas que usan el header simplificado en lugar del header completo.
   * @type {string[]}
   */
  const simpleHeaderPages = ['vuelos','carrito','datos-pasajeros','seleccion-asientos','checkout','confirmacion'];

  /**
   * Paginas que no muestran el Footer.
   * @type {string[]}
   */
  const noFooterPages = ['profile','admin','login','register','acceso-denegado','reservas','mi-agencia',...simpleHeaderPages];

  /**
   * Paginas que no muestran ningun Header.
   * @type {string[]}
   */
  const noHeaderPages = ['login','register'];

  // Indica si la pagina actual debe usar el header simplificado.
  $: useSimpleHeader = simpleHeaderPages.includes(currentPage);

  // Indica si la pagina actual debe mostrar el Header.
  $: showHeader      = !noHeaderPages.includes(currentPage);

  // Indica si la pagina actual debe mostrar el Footer.
  $: showFooter      = !noFooterPages.includes(currentPage);
</script>