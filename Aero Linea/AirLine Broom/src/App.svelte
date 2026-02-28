<script>
  import { onMount } from 'svelte';
  import { sesion, cargarSesion } from './stores/sesion.js';
  import Header from './components/Header.svelte';
  import HeaderSimple from './components/HeaderSimple.svelte';
  import Footer from './components/Footer.svelte';
  import Loading from './components/Loading.svelte';
  import FlightNotification from './components/FlightNotification.svelte';

  import Home from './pages/Home.svelte';
  import Vuelos from './pages/Vuelos.svelte';
  import VuelosGenerales from './pages/VuelosGenerales.svelte';
  import ResultadosBusqueda from './pages/ResultadosBusqueda.svelte';
  import Comfirmacion from './pages/Comfirmacion.svelte';
  import Carrito from './pages/Carrito.svelte';
  import Checkout from './pages/Checkout.svelte';
  import DatosPasajeros from './pages/DatosPasajeros.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import Profile from './pages/Profile.svelte';
  import MisReservas from './pages/MisReservas.svelte';
  import Admin from './pages/Admin.svelte';
  import DestinosDestacados from './pages/DestinosDestacados.svelte';
  import InformacionAsientos from './pages/InformacionAsientos.svelte';
  import InformacionSeguridad from './pages/InformacionSeguridad.svelte';
  import AccesoDenegado from './pages/Accesodenegado.svelte';
  import './app.css';

  let isLoading = true;
  let sesionCargada = false;   // evita que el guard actúe antes de conocer la sesión
  let currentPage = 'home';
  let suggestedDestination = null;
  let currentFlightId = null;
  let searchParams = null;
  let reservacionesConfirmadas = [];
  let pageKey = Date.now();

  // Páginas que requieren sesión activa para entrar
  const paginasProtegidas = [
    'profile',
    'admin',
    'reservas',
    'checkout',
    'datos-pasajeros',
    'carrito'
  ];

  // Solo admin puede entrar aquí; el resto va a acceso-denegado
  const paginasSoloAdmin = ['admin'];

  onMount(async () => {
    actualizarPaginaDesdeURL();
    window.addEventListener('popstate', actualizarPaginaDesdeURL);

    // Intentamos restaurar la sesión antes de quitar el loading
    await cargarSesion();
    sesionCargada = true;

    setTimeout(() => {
      isLoading = false;
    }, 3000);

    return () => {
      window.removeEventListener('popstate', actualizarPaginaDesdeURL);
    };
  });

  function actualizarPaginaDesdeURL() {
    const path = window.location.pathname.slice(1) || 'home';
    currentPage = path;
    pageKey = Date.now();
  }

  /**
   * Guard de navegación.
   * Devuelve la página real a la que debe ir el usuario
   * teniendo en cuenta su sesión y rol.
   */
  function resolverPagina(page) {
    // Si la sesión aún no terminó de cargar, no redirigir todavía
    if (!sesionCargada) return page;

    const sesionActual = $sesion;

    // Si intenta ir a una página protegida sin sesión → login
    if (paginasProtegidas.includes(page) && !sesionActual) {
      return 'login';
    }

    // Si intenta ir a admin sin ser administrador → acceso-denegado
    if (paginasSoloAdmin.includes(page) && sesionActual?.rolNombre !== 'Administrador') {
      return 'acceso-denegado';
    }

    return page;
  }

  function navigateTo(page, data = null) {
    const paginaFinal = resolverPagina(page);
    currentPage = paginaFinal;
    pageKey = Date.now();

    window.history.pushState({}, '', `/${paginaFinal}`);

    if (paginaFinal === 'detalle-vuelo') {
      currentFlightId = data;
    }

    if (paginaFinal === 'vuelos' && data?.busquedaId) {
      searchParams = { busquedaId: data.busquedaId };
      console.log('✅ App.svelte - navegando a vuelos con busquedaId:', data.busquedaId);
    } else if (paginaFinal === 'resultados-busqueda') {
      searchParams = data;
    } else if (paginaFinal === 'confirmacion' && data?.reservaciones) {
      reservacionesConfirmadas = data.reservaciones;
      console.log('✅ App.svelte - reservaciones para confirmación:', reservacionesConfirmadas);
    } else if (paginaFinal !== 'vuelos') {
      searchParams = null;
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleDestinationSuggestion(destination) {
    suggestedDestination = destination;
    if (currentPage !== 'home') navigateTo('home');
    setTimeout(() => {
      const searchSection = document.querySelector('.broom-home__search-section');
      if (searchSection) {
        searchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 100);
  }

  const simpleHeaderPages = ['vuelos', 'carrito', 'datos-pasajeros', 'checkout', 'confirmacion'];
  const noFooterPages    = ['profile', 'admin', 'login', 'register', 'acceso-denegado', ...simpleHeaderPages];
  const noHeaderPages    = ['login', 'register'];

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
      <!-- Pasamos la sesión al Header para que muestre nombre/rol o botón login -->
      <Header {navigateTo} {currentPage} sesion={$sesion} />
    {/if}
  {/if}

  <main class="app-main">
    {#if currentPage === 'home'}
      <Home {navigateTo} {suggestedDestination} />
    {:else if currentPage === 'VuelosGenerales'}
      <VuelosGenerales {navigateTo} />
    {:else if currentPage === 'reservas'}
      <MisReservas {navigateTo} />
    {:else if currentPage === 'vuelos'}
      {#key searchParams?.busquedaId}
        <Vuelos {navigateTo} {searchParams} />
      {/key}
    {:else if currentPage === 'confirmacion'}
      <Comfirmacion {navigateTo} reservaciones={reservacionesConfirmadas} />
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
    {:else if currentPage === 'destinos-destacados'}
      <DestinosDestacados {navigateTo} />
    {:else if currentPage === 'info-asientos'}
      <InformacionAsientos {navigateTo} />
    {:else if currentPage === 'info-seguridad'}
      <InformacionSeguridad {navigateTo} />
    {:else if currentPage === 'acceso-denegado'}
      <AccesoDenegado {navigateTo} />
    {:else}
      <Home {navigateTo} />
    {/if}
  </main>

  {#if showFooter}
    <Footer {navigateTo} />
  {/if}
</div>