<script>
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
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import Profile from './pages/Profile.svelte';
  import MisReservas from './pages/MisReservas.svelte';
  import Admin from './pages/Admin.svelte';
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

  const SS_SEARCH_PARAMS = 'broom_searchParams';
  const SS_PAGE          = 'broom_currentPage';

  let isLoading = true;
  let sesionCargada = false;
  let currentPage = 'home';
  let suggestedAeropuerto = null;
  let currentFlightId = null;
  let searchParams = null;
  let reservacionesConfirmadas = [];
  let facturasConfirmadas = [];
  let pageKey = Date.now();

  const paginasProtegidas = ['profile','admin','reservas','checkout','datos-pasajeros','carrito'];
  const paginasSoloAdmin  = ['admin'];
  const infoPages = [
    'contactanos','centro-ayuda','preguntas-frecuentes',
    'privacidad','terminos','cookies','politica-cancelacion','sobre-nosotros',
  ];

  function saveSearchParams(params) {
    try {
      if (params) sessionStorage.setItem(SS_SEARCH_PARAMS, JSON.stringify(params));
      else        sessionStorage.removeItem(SS_SEARCH_PARAMS);
    } catch (e) { }
  }

  function loadSearchParams() {
    try {
      const raw = sessionStorage.getItem(SS_SEARCH_PARAMS);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

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

  // ══ Flag: se completó un pago en esta sesión ══
  let pagoCompletado = false;

  // Páginas del flujo de compra que se bloquean post-pago
  const paginasFlujoCompra = ['vuelos', 'datos-pasajeros', 'carrito', 'checkout'];

  function actualizarPaginaDesdeURL() {
    const path = window.location.pathname.slice(1) || 'home';

    // ══ POST-PAGO: si ya pagó, no puede regresar al flujo de compra ══
    if (pagoCompletado && (paginasFlujoCompra.includes(path) || path === 'confirmacion')) {
      currentPage = 'home';
      pageKey = Date.now();
      window.history.replaceState({}, '', '/home');
      return;
    }

    // Si intenta volver a confirmación sin datos, ir a home
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

  function resolverPagina(page) {
    if (!sesionCargada) return page;
    const sesionActual = $sesion;
    if (paginasProtegidas.includes(page) && !sesionActual) return 'login';
    if (paginasSoloAdmin.includes(page) && sesionActual?.rolNombre !== 'Administrador') return 'acceso-denegado';
    return page;
  }

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
      // NUEVO: búsqueda global desde Header
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

    } else if (paginaFinal === 'datos-pasajeros') {
      // No sobreescribir searchParams — mantener los datos de vuelos
      // para que si el usuario regresa a vuelos, siga viendo resultados

    } else if (paginaFinal === 'confirmacion') {
      if (data?.reservaciones) reservacionesConfirmadas = data.reservaciones;
      if (data?.facturas)      facturasConfirmadas      = data.facturas;
      // ══ Post-pago: bloquear navegación hacia atrás ══
      pagoCompletado = true;
      searchParams = null;
      sessionStorage.removeItem(SS_SEARCH_PARAMS);
      // Reemplazar historial para que "atrás" no vuelva a checkout/carrito/etc
      window.history.replaceState({ paid: true }, '', '/confirmacion');

    } else if (paginaFinal === 'home' && data?.suggestedAeropuerto) {
      suggestedAeropuerto = data.suggestedAeropuerto;
      searchParams = null;
      pagoCompletado = false;

    } else if (!['vuelos', 'datos-pasajeros', 'carrito', 'checkout', 'confirmacion'].includes(paginaFinal)) {
      searchParams = null;
    }

    // Resetear flag de pago si navega a home (nueva sesión de compra)
    if (paginaFinal === 'home') pagoCompletado = false;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleDestinationSuggestion(aeropuertoData) {
    suggestedAeropuerto = aeropuertoData;
    if (currentPage !== 'home') navigateTo('home');
    setTimeout(() => {
      const searchSection = document.querySelector('.broom-home__search-section');
      if (searchSection) searchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  }

  const simpleHeaderPages = ['vuelos','carrito','datos-pasajeros','checkout','confirmacion'];

  const noFooterPages = ['profile','admin','login','register','acceso-denegado','reservas',...simpleHeaderPages];
  const noHeaderPages = ['login','register'];

  $: useSimpleHeader = simpleHeaderPages.includes(currentPage);
  $: showHeader      = !noHeaderPages.includes(currentPage);
  $: showFooter      = !noFooterPages.includes(currentPage);
</script>

<div class="app-wrapper">
  {#if isLoading}
    <Loading message="Preparando tu vuelo..." />
  {/if}

  {#if !isLoading && currentPage === 'home'}
    <FlightNotification onDestinationClick={handleDestinationSuggestion} />
  {/if}

  {#if showHeader}
    {#if useSimpleHeader}
      <HeaderSimple {navigateTo} />
    {:else}
      <Header {navigateTo} {currentPage} sesion={$sesion} />
    {/if}
  {/if}

  <main class="app-main">
    {#if !sesionCargada && paginasProtegidas.includes(currentPage)}
      <p style="text-align:center; padding:4rem; color:#888">Cargando...</p>
    {:else if currentPage === 'home'}
      <Home {navigateTo} {suggestedAeropuerto} />
    {:else if currentPage === 'reservas'}
      {#key pageKey}
        <MisReservas {navigateTo} />
      {/key}
    {:else if currentPage === 'vuelos'}
      {#key pageKey}
        <Vuelos {navigateTo} {searchParams} />
      {/key}
    {:else if currentPage === 'confirmacion'}
      <Comfirmacion {navigateTo} reservaciones={reservacionesConfirmadas} facturas={facturasConfirmadas} />
    {:else if currentPage === 'resultados-busqueda'}
      <ResultadosBusqueda {navigateTo} {searchParams} />
    {:else if currentPage === 'carrito'}
      <Carrito {navigateTo} />
    {:else if currentPage === 'checkout'}
      <Checkout {navigateTo} />
    {:else if currentPage === 'datos-pasajeros'}
      <DatosPasajeros {navigateTo} />
    {:else if currentPage === 'login'}
      {#key pageKey}
        <Login {navigateTo} />
      {/key}
    {:else if currentPage === 'register'}
      {#key pageKey}
        <Register {navigateTo} />
      {/key}
    {:else if currentPage === 'profile'}
      <Profile {navigateTo} />
    {:else if currentPage === 'admin'}
      <Admin {navigateTo} />
    {:else if currentPage === 'info-asientos'}
      <InformacionAsientos {navigateTo} />
    {:else if currentPage === 'info-seguridad'}
      <InformacionSeguridad {navigateTo} />
    {:else if currentPage === 'acceso-denegado'}
      <AccesoDenegado {navigateTo} />
    {:else if currentPage === 'contactanos'}
      <Contactanos {navigateTo} />
    {:else if currentPage === 'centro-ayuda'}
      <CentroAyuda {navigateTo} />
    {:else if currentPage === 'preguntas-frecuentes'}
      <PreguntasFrecuentes {navigateTo} />
    {:else if currentPage === 'privacidad'}
      <Privacidad {navigateTo} />
    {:else if currentPage === 'terminos'}
      <Terminos {navigateTo} />
    {:else if currentPage === 'cookies'}
      <Cookies {navigateTo} />
    {:else if currentPage === 'politica-cancelacion'}
      <PoliticaCancelacion {navigateTo} />
    {:else if currentPage === 'sobre-nosotros'}
      <SobreNosotros {navigateTo} />
    {:else}
      <Home {navigateTo} />
    {/if}
  </main>

  {#if showFooter}
    <Footer {navigateTo} />
  {/if}
</div>