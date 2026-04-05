<script>
  /**
   * @file App.svelte
   * @description Componente raiz de Miku Inn. Gestiona el enrutamiento del lado
   * del cliente, la sesion del usuario y decide que pagina y layout se muestran
   * en cada momento.
   */

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
  import Profile from './pages/Profile.svelte';
  import Administrador from './pages/Administrador.svelte';
  import WebService from './pages/WebService.svelte';
  import AccesoDenegado from './pages/AccesoDenegado.svelte';

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
  import './styles/destinos.css';
  import './styles/profile.css';
  import './styles/administrador.css';
  import './styles/webservice.css';
  import './styles/info-pages.css';

  /** URL base del backend. @type {string} */
  const API = 'http://localhost:7000';

  /**
   * Conjunto de rutas validas en la aplicacion.
   * Cualquier path que no este aqui se redirige a home.
   * @type {Set<string>}
   */
  const VALID_ROUTES = new Set([
    'home', 'search-results', 'hotel-detail', 'checkout', 'agradecimiento',
    'login', 'register', 'reservations', 'destinations', 'profile',
    'administrador', 'webservice', 'acceso-denegado',
    'sobre-nosotros', 'centro-ayuda', 'contactanos', 'preguntas-frecuentes',
    'politica-cancelacion', 'privacidad', 'terminos', 'cookies'
  ]);

  /** Pagina activa en este momento. @type {string} */
  let currentPage           = 'home';

  /** Datos que se pasan a la pagina de checkout. @type {object|null} */
  let checkoutData          = null;

  /** Datos que se pasan a la pagina de agradecimiento tras el pago. @type {object|null} */
  let agradecimientoData    = null;

  /** Datos del hotel seleccionado para ver su detalle. @type {object|null} */
  let currentHotelData      = null;

  /** Parametros de la ultima busqueda realizada. @type {object|null} */
  let searchParams          = null;

  /** Sugerencia de destino que puede llegar desde otras secciones. @type {string|null} */
  let destinationSuggestion = null;

  /**
   * Llave que cambia con cada navegacion para forzar el re-mount
   * de paginas que necesitan reiniciarse por completo.
   * @type {number}
   */
  let pageKey               = Date.now();

  /** Indica si el usuario tiene sesion activa. @type {boolean} */
  let isLoggedIn     = false;

  /** Nombre de usuario de la sesion activa. @type {string} */
  let userName       = '';

  /** ID del rol del usuario (2 = admin, 3 = webservice). @type {number|null} */
  let userRolId      = null;

  /**
   * Bandera que se activa cuando la verificacion inicial de sesion termina.
   * Evita renderizar la UI antes de saber si hay sesion.
   * @type {boolean}
   */
  let sessionChecked = false;

  onMount(() => {
    checkSession();
    syncFromURL();
    window.addEventListener('popstate', syncFromURL);
    return () => window.removeEventListener('popstate', syncFromURL);
  });

  /**
   * Consulta al backend si hay una sesion activa y actualiza el estado
   * de autenticacion del usuario.
   * @async
   * @returns {Promise<void>}
   */
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

  /**
   * Sincroniza la pagina activa con el path de la URL actual.
   * Si el path no existe en VALID_ROUTES lo manda a home.
   */
  function syncFromURL() {
    const path = window.location.pathname.slice(1) || 'home';
    if (!VALID_ROUTES.has(path)) {
      currentPage = 'home';
      pageKey = Date.now();
      window.history.replaceState({}, '', '/home');
      return;
    }
    currentPage = path;
    pageKey = Date.now();
  }

  /**
   * Navega programaticamente a una pagina y actualiza el historial del navegador.
   * Aplica proteccion de rutas para administrador y webservice.
   * @param {string} page - Nombre de la ruta destino.
   * @param {object|null} [data=null] - Datos opcionales que se pasan a la nueva pagina.
   */
  function navigateTo(page, data = null) {
    if (!VALID_ROUTES.has(page)) { page = 'home'; }

    // Rutas protegidas: mostrar acceso denegado en lugar de redirigir silenciosamente
    if (page === 'administrador' && userRolId !== 2) { showAccesoDenegado(); return; }
    if (page === 'webservice'    && userRolId !== 3) { showAccesoDenegado(); return; }

    currentPage = page;
    pageKey     = Date.now();
    window.history.pushState({}, '', `/${page}`);

    if (page === 'hotel-detail')   currentHotelData   = data;
    if (page === 'search-results') searchParams       = data;
    if (page === 'checkout')       checkoutData       = data;
    if (page === 'agradecimiento') agradecimientoData = data;

    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Muestra la pantalla de acceso denegado y actualiza la URL correspondiente.
   */
  function showAccesoDenegado() {
    currentPage = 'acceso-denegado';
    pageKey     = Date.now();
    window.history.pushState({}, '', '/acceso-denegado');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /**
   * Maneja el evento de login exitoso disparado por Login o Register.
   * Actualiza el estado de sesion y redirige al inicio.
   * @param {CustomEvent} event - Evento con detail: { name, email, rolId }.
   */
  function handleLogin(event) {
    const name  = event.detail?.name  ?? event.detail?.email ?? 'Usuario';
    const rolId = event.detail?.rolId ?? null;
    isLoggedIn = true; userName = name; userRolId = rolId;
    navigateTo('home');
  }

  /**
   * Cierra la sesion del usuario en el backend, limpia el estado local
   * y redirige al inicio.
   * @async
   * @returns {Promise<void>}
   */
  async function handleLogout() {
    try { await fetch(`${API}/auth/logout`, { method: 'POST', credentials: 'include' }); } catch (_) {}
    isLoggedIn = false; userName = ''; userRolId = null;
    navigateTo('home');
  }

  /**
   * Paginas que no muestran el Header ni el Footer (pantalla completa).
   * @type {string[]}
   */
  const noHeaderFooter = ['login', 'register', 'administrador', 'webservice', 'acceso-denegado'];

  // Indica si la pagina actual debe mostrar el Header y el Footer.
  $: showHeaderFooter = !noHeaderFooter.includes(currentPage);
</script>

<!-- Contenedor principal de la aplicacion -->
<div class="app-wrapper">
  {#if !sessionChecked}
    <!-- Esperando la respuesta de sesion antes de renderizar cualquier cosa -->
    <div style="display:none"></div>
  {:else}
    {#if showHeaderFooter}
      <!-- Header global con navegacion y acciones de usuario -->
      <Header
        {navigateTo} {currentPage} {isLoggedIn} {userName} {userRolId}
        on:logout={handleLogout}
      />
    {/if}

    <!-- Area de contenido principal donde se renderiza la pagina activa -->
    <main class="app-main">
      {#if currentPage === 'home'}
        {#key pageKey}<Home {navigateTo} {destinationSuggestion} />{/key}

      {:else if currentPage === 'search-results'}
        <SearchResults {navigateTo} {searchParams} />

      {:else if currentPage === 'hotel-detail'}
        <!-- Se pasan los datos del hotel seleccionado desde la busqueda -->
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

      {:else if currentPage === 'profile'}
        <Profile {navigateTo} />

      {:else if currentPage === 'administrador' && userRolId === 2}
        <Administrador {navigateTo} />

      {:else if currentPage === 'webservice' && userRolId === 3}
        <WebService {navigateTo} />

      <!-- Alguien entro directo por URL sin permisos -->
      {:else if currentPage === 'administrador' || currentPage === 'webservice'}
        {#key pageKey}<AccesoDenegado {navigateTo} />{/key}

      {:else if currentPage === 'acceso-denegado'}
        {#key pageKey}<AccesoDenegado {navigateTo} />{/key}

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
      <!-- Footer global con links de navegacion, soporte y legales -->
      <Footer {navigateTo} />
    {/if}
  {/if}
</div>
