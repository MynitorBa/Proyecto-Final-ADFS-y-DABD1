<script>
  import { onMount } from 'svelte';
  import Header from './components/Header.svelte';
  import Footer from './components/Footer.svelte';

  import Home from './pages/Home.svelte';
  import SearchResults from './pages/SearchResults.svelte';
  import HotelDetail from './pages/HotelDetail.svelte';
  import Checkout from './pages/Checkout.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import MyReservations from './pages/MyReservations.svelte';
  import Destinos from './pages/Destinos.svelte';
  import DestinoDetail from './pages/DestinoDetail.svelte';
  import Ofertas from './pages/Ofertas.svelte';

  import './app.css';
  import './styles/home.css';
  import './styles/searchresults.css';
  import './styles/hoteldetail.css';
  import './styles/checkout.css';
  import './styles/login.css';
  import './styles/register.css';
  import './styles/myreservations.css';
  import './styles/destinodetail.css';
  import './styles/destinos.css';
  import './styles/ofertas.css';

  let currentPage = 'home';
  let isLoggedIn = false;
  let userName = '';
  let cartItemsCount = 0;
  let currentHotelId = null;
  let currentDestinoId = null;
  let searchParams = null;
  let destinationSuggestion = null;
  let pageKey = Date.now();

  onMount(() => {
    syncFromURL();
    window.addEventListener('popstate', syncFromURL);
    return () => window.removeEventListener('popstate', syncFromURL);
  });

  function syncFromURL() {
    const path = window.location.pathname.slice(1) || 'home';
    currentPage = path;
    pageKey = Date.now();
  }

  function navigateTo(page, data = null) {
    currentPage = page;
    pageKey = Date.now();
    window.history.pushState({}, '', `/${page}`);

    if (page === 'hotel-detail')   currentHotelId = data;
    if (page === 'destino-detail') currentDestinoId = data;
    if (page === 'search-results') searchParams = data;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleDestinationSuggestion(destination) {
    destinationSuggestion = destination;
    if (currentPage !== 'home') navigateTo('home');
    setTimeout(() => {
      document.querySelector('.search-section')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  }

  function handleHeaderSearch(event) {
    navigateTo('search-results', { query: event.detail });
  }

  function handleLogin(event) {
    isLoggedIn = true;
    userName = event.detail?.name || event.detail?.email || 'Usuario';
    navigateTo('home');
  }

  function handleLogout() {
    isLoggedIn = false;
    userName = '';
    cartItemsCount = 0;
    navigateTo('home');
  }

  const noHeaderFooter = ['login', 'register'];
  $: showHeaderFooter = !noHeaderFooter.includes(currentPage);
</script>

<div class="app-wrapper">
  {#if showHeaderFooter}
    <Header
      {navigateTo}
      {currentPage}
      {isLoggedIn}
      {userName}
      {cartItemsCount}
      on:search={handleHeaderSearch}
      on:logout={handleLogout}
    />
  {/if}

  <main class="app-main">
    {#if currentPage === 'home'}
      <Home {navigateTo} {destinationSuggestion} />
    {:else if currentPage === 'search-results'}
      <SearchResults {navigateTo} {searchParams} />
    {:else if currentPage === 'hotel-detail'}
      <HotelDetail {navigateTo} hotelId={currentHotelId} />
    {:else if currentPage === 'checkout'}
      <Checkout {navigateTo} />
    {:else if currentPage === 'login'}
      {#key pageKey}
        <Login {navigateTo} on:login={handleLogin} />
      {/key}
    {:else if currentPage === 'register'}
      {#key pageKey}
        <Register {navigateTo} on:login={handleLogin} />
      {/key}
    {:else if currentPage === 'reservations'}
      <MyReservations {navigateTo} />
    {:else if currentPage === 'destinations'}
      <Destinos {navigateTo} />
    {:else if currentPage === 'destino-detail'}
      <DestinoDetail {navigateTo} destinoId={currentDestinoId} />
    {:else if currentPage === 'offers'}
      <Ofertas {navigateTo} />
    {/if}
  </main>

  {#if showHeaderFooter}
    <Footer />
  {/if}
</div>