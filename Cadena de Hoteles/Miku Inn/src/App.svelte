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
  import Profile from './pages/Profile.svelte';
  import Administrador from './pages/Administrador.svelte';

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
  import './styles/profile.css';
  import './styles/administrador.css';

  const API = 'http://localhost:7000';

  let currentPage    = 'home';
  let checkoutData   = null;
  let currentHotelData = null;
  let currentDestinoId = null;
  let searchParams   = null;
  let destinationSuggestion = null;
  let pageKey        = Date.now();
  let cartItemsCount = 0;

  let isLoggedIn    = false;
  let userName      = '';
  let userRolId     = null;
  let sessionChecked = false;

  onMount(() => {
    checkSession();
    syncFromURL();
    window.addEventListener('popstate', syncFromURL);
    return () => window.removeEventListener('popstate', syncFromURL);
  });

  async function checkSession() {
    try {
      const res = await fetch(`${API}/sesion`, { method: 'GET', credentials: 'include' });
      if (res.ok) {
        const data = await res.json();
        if (data.autenticado) {
          isLoggedIn = true;
          userName   = data.username ?? '';
          userRolId  = data.rolId    ?? null;
        } else {
          isLoggedIn = false; userName = ''; userRolId = null;
        }
      } else {
        isLoggedIn = false; userName = ''; userRolId = null;
      }
    } catch (_) {
      isLoggedIn = false; userName = ''; userRolId = null;
    } finally {
      sessionChecked = true;
    }
  }

  function syncFromURL() {
    const path = window.location.pathname.slice(1) || 'home';
    currentPage = path;
    pageKey = Date.now();
  }

  function navigateTo(page, data = null) {
    if (page === 'administrador' && userRolId !== 2) {
      navigateTo('home');
      return;
    }
    currentPage = page;
    pageKey     = Date.now();
    window.history.pushState({}, '', `/${page}`);

    if (page === 'hotel-detail')   currentHotelData = data;
    if (page === 'destino-detail') currentDestinoId = data;
    if (page === 'search-results') searchParams     = data;
    if (page === 'checkout')       checkoutData     = data;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleHeaderSearch(event) {
    navigateTo('search-results', { ciudad: event.detail, pais: '', hotels: [] });
  }

  function handleLogin(event) {
    const name  = event.detail?.name  ?? event.detail?.email ?? 'Usuario';
    const rolId = event.detail?.rolId ?? null;
    isLoggedIn = true;
    userName   = name;
    userRolId  = rolId;
    navigateTo('home');
  }

  async function handleLogout() {
    try {
      await fetch(`${API}/auth/logout`, { method: 'POST', credentials: 'include' });
    } catch (_) {}
    isLoggedIn = false; userName = ''; userRolId = null; cartItemsCount = 0;
    navigateTo('home');
  }

  const noHeaderFooter = ['login', 'register'];
  $: showHeaderFooter = !noHeaderFooter.includes(currentPage);
</script>

<div class="app-wrapper">
  {#if !sessionChecked}
    <div style="display:none"></div>
  {:else}
    {#if showHeaderFooter}
      <Header
        {navigateTo}
        {currentPage}
        {isLoggedIn}
        {userName}
        {userRolId}
        {cartItemsCount}
        on:search={handleHeaderSearch}
        on:logout={handleLogout}
      />
    {/if}

    <main class="app-main">
      {#if currentPage === 'home'}
        {#key pageKey}
          <Home {navigateTo} {destinationSuggestion} />
        {/key}
      {:else if currentPage === 'search-results'}
        {#key pageKey}
          <SearchResults {navigateTo} {searchParams} />
        {/key}
      {:else if currentPage === 'hotel-detail'}
        {#key pageKey}
          <HotelDetail
            {navigateTo}
            hotel={currentHotelData?.hotel ?? null}
            cantidadPersonas={currentHotelData?.cantidadPersonas ?? 1}
            fechaCheckIn={currentHotelData?.fechaCheckIn ?? ''}
            fechaCheckOut={currentHotelData?.fechaCheckOut ?? ''}
          />
        {/key}
      {:else if currentPage === 'checkout'}
        <Checkout {navigateTo} {checkoutData} />
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
      {:else if currentPage === 'profile'}
        <Profile {navigateTo} />
      {:else if currentPage === 'administrador' && userRolId === 2}
        <Administrador {navigateTo} />
      {/if}
    </main>

    {#if showHeaderFooter}
      <Footer />
    {/if}
  {/if}
</div>