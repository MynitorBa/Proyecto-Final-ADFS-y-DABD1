<script>
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
  
  import './app.css';
  
  let isLoading = true;
  let currentPage = 'home';
  let suggestedDestination = null;
  let currentFlightId = null;
  let searchParams = null;
  
  setTimeout(() => {
    isLoading = false;
  }, 3000);
  
  function navigateTo(page, data = null) {
    currentPage = page;
    
    if (page === 'detalle-vuelo') {
      currentFlightId = data;
    }
    
    if (page === 'resultados-busqueda') {
      searchParams = data;
    }
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleDestinationSuggestion(destination) {
    suggestedDestination = destination;
    
    if (currentPage !== 'home') {
      navigateTo('home');
    }
    
    setTimeout(() => {
      const searchSection = document.querySelector('.broom-home__search-section');
      if (searchSection) {
        searchSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 100);
  }

  // Páginas que usan el header simple (sin menú completo)
  const simpleHeaderPages = ['vuelos', 'carrito', 'datos-pasajeros', 'checkout', 'confirmacion'];
  
  // Páginas que NO muestran footer
  const noFooterPages = ['profile', 'admin', 'login', 'register', ...simpleHeaderPages];
  
  // Páginas que NO muestran ningún header
  const noHeaderPages = ['login', 'register'];

  $: useSimpleHeader = simpleHeaderPages.includes(currentPage);
  $: showHeader = !noHeaderPages.includes(currentPage);
  $: showFooter = !noFooterPages.includes(currentPage);
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
      <Header {navigateTo} {currentPage} />
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
      <Vuelos {navigateTo} />
    {:else if currentPage === 'confirmacion'}
      <Comfirmacion {navigateTo} />
    {:else if currentPage === 'resultados-busqueda'}
      <ResultadosBusqueda {navigateTo} {searchParams} />
    {:else if currentPage === 'carrito'}
      <Carrito {navigateTo} />
    {:else if currentPage === 'checkout'}
      <Checkout {navigateTo} />
    {:else if currentPage === 'datos-pasajeros'}
      <DatosPasajeros {navigateTo} />
    {:else if currentPage === 'login'}
      <Login {navigateTo} />
    {:else if currentPage === 'register'}
      <Register {navigateTo} />
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
    {:else}
      <Home {navigateTo} />
    {/if}
  </main>
  
  {#if showFooter}
    <Footer {navigateTo} />
  {/if}
</div>