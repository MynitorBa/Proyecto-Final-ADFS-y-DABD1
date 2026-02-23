<script>
  // @ts-nocheck
  import { onMount } from 'svelte';
  import Header from './components/Header.svelte';
  import Footer from './components/Footer.svelte';

  import Home from './pages/Home.svelte';
  import SearchResults from './pages/SearchResults.svelte';
  import HotelDetail from './pages/HotelDetail.svelte';
  import Checkout from './pages/Checkout.svelte';
  import Agradecimiento from './pages/Agradecimiento.svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import MyReservations from './pages/MyReservations.svelte';
  import Destinos from './pages/Destinos.svelte';
  import DestinoDetail from './pages/DestinoDetail.svelte';
  import Profile from './pages/Profile.svelte';
  import Administrador from './pages/Administrador.svelte';
  import WebService from './pages/WebService.svelte';

  import SobreNosotros       from './pages/SobreNosotros.svelte';
  import CentroAyuda         from './pages/CentroAyuda.svelte';
  import Contactanos         from './pages/Contactanos.svelte';
  import PreguntasFrecuentes from './pages/PreguntasFrecuentes.svelte';
  import PoliticaCancelacion from './pages/PoliticaCancelacion.svelte';
  import Privacidad          from './pages/Privacidad.svelte';
  import Terminos            from './pages/Terminos.svelte';
  import Cookies             from './pages/Cookies.svelte';

  import './app.css';
  import './styles/home.css';
  import './styles/searchresults.css';
  import './styles/hoteldetail.css';
  import './styles/checkout.css';
  import './styles/Agradecimiento.css';
  import './styles/login.css';
  import './styles/register.css';
  import './styles/myreservations.css';
  import './styles/destinodetail.css';
  import './styles/destinos.css';
  import './styles/profile.css';
  import './styles/administrador.css';
  import './styles/webservice.css';
  import './styles/info-pages.css';

  const API = 'http://localhost:7000';

  let currentPage           = 'home';
  let checkoutData          = null;
  let agradecimientoData    = null;
  let currentHotelData      = null;
  let currentDestinoId      = null;
  let searchParams          = null;
  let destinationSuggestion = null;
  let pageKey               = Date.now();

  let isLoggedIn     = false;
  let userName       = '';
  let userRolId      = null;
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
    if (page === 'administrador' && userRolId !== 2) { navigateTo('home'); return; }
    if (page === 'webservice'    && userRolId !== 3) { navigateTo('home'); return; }

    currentPage = page;
    pageKey     = Date.now();
    window.history.pushState({}, '', `/${page}`);

    if (page === 'hotel-detail')   currentHotelData   = data;
    if (page === 'destino-detail') currentDestinoId   = data;
    if (page === 'search-results') searchParams       = data;
    if (page === 'checkout')       checkoutData       = data;
    if (page === 'agradecimiento') agradecimientoData = data;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleHeaderSearch(event) {
    navigateTo('search-results', { ciudad: event.detail, pais: '', hotels: [] });
  }

  function handleLogin(event) {
    const name  = event.detail?.name  ?? event.detail?.email ?? 'Usuario';
    const rolId = event.detail?.rolId ?? null;
    isLoggedIn = true; userName = name; userRolId = rolId;
    navigateTo('home');
  }

  async function handleLogout() {
    try { await fetch(`${API}/auth/logout`, { method: 'POST', credentials: 'include' }); } catch (_) {}
    isLoggedIn = false; userName = ''; userRolId = null;
    navigateTo('home');
  }

  const noHeaderFooter = ['login', 'register', 'administrador', 'webservice'];
  $: showHeaderFooter = !noHeaderFooter.includes(currentPage);
</script>

<div class="app-wrapper">
  {#if !sessionChecked}
    <div style="display:none"></div>
  {:else}
    {#if showHeaderFooter}
      <Header
        {navigateTo} {currentPage} {isLoggedIn} {userName} {userRolId}
        on:search={handleHeaderSearch}
        on:logout={handleLogout}
      />
    {/if}

    <main class="app-main">
      {#if currentPage === 'home'}
        {#key pageKey}<Home {navigateTo} {destinationSuggestion} />{/key}

      {:else if currentPage === 'search-results'}
        <SearchResults {navigateTo} {searchParams} />

      {:else if currentPage === 'hotel-detail'}
        <HotelDetail
          {navigateTo}
          hotel={currentHotelData?.hotel ?? null}
          cantidadPersonas={currentHotelData?.cantidadPersonas ?? 1}
          fechaCheckIn={currentHotelData?.fechaCheckIn ?? ''}
          fechaCheckOut={currentHotelData?.fechaCheckOut ?? ''}
        />

      {:else if currentPage === 'checkout'}
        <Checkout {navigateTo} {checkoutData} />

      {:else if currentPage === 'agradecimiento'}
        {#key pageKey}<Agradecimiento {navigateTo} {agradecimientoData} />{/key}

      {:else if currentPage === 'login'}
        {#key pageKey}<Login {navigateTo} on:login={handleLogin} />{/key}

      {:else if currentPage === 'register'}
        {#key pageKey}<Register {navigateTo} on:login={handleLogin} />{/key}

      {:else if currentPage === 'reservations'}
        {#key pageKey}<MyReservations {navigateTo} />{/key}

      {:else if currentPage === 'destinations'}
        <Destinos {navigateTo} />

      {:else if currentPage === 'destino-detail'}
        <DestinoDetail {navigateTo} destinoId={currentDestinoId} />

      {:else if currentPage === 'profile'}
        <Profile {navigateTo} />

      {:else if currentPage === 'administrador' && userRolId === 2}
        <Administrador {navigateTo} />

      {:else if currentPage === 'webservice' && userRolId === 3}
        <WebService {navigateTo} />

      <!-- ── Páginas informativas ── -->
      {:else if currentPage === 'sobre-nosotros'}
        {#key pageKey}<SobreNosotros {navigateTo} />{/key}

      {:else if currentPage === 'centro-ayuda'}
        {#key pageKey}<CentroAyuda {navigateTo} />{/key}

      {:else if currentPage === 'contactanos'}
        {#key pageKey}<Contactanos {navigateTo} />{/key}

      {:else if currentPage === 'preguntas-frecuentes'}
        {#key pageKey}<PreguntasFrecuentes {navigateTo} />{/key}

      {:else if currentPage === 'politica-cancelacion'}
        {#key pageKey}<PoliticaCancelacion {navigateTo} />{/key}

      {:else if currentPage === 'privacidad'}
        {#key pageKey}<Privacidad {navigateTo} />{/key}

      {:else if currentPage === 'terminos'}
        {#key pageKey}<Terminos {navigateTo} />{/key}

      {:else if currentPage === 'cookies'}
        {#key pageKey}<Cookies {navigateTo} />{/key}

      {/if}
    </main>

    {#if showHeaderFooter}
      <Footer {navigateTo} />
    {/if}
  {/if}
</div>