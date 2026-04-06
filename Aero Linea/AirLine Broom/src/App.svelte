<script>
  /**
   * @file App.svelte
   * @description Componente raiz de la aplicacion AirLine Broom. Gestiona el
   * enrutamiento del lado del cliente, protege rutas por rol y sesion,
   * persiste parametros de busqueda en sessionStorage y decide que layout
   * (header, footer) renderizar segun la pagina activa. Toda la navegacion
   * del SPA pasa por navigateTo().
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
  import DetalleVuelo from './pages/DetalleVuelo.svelte';
  import DetalleVueloAdmin from './pages/DetalleVueloAdmin.svelte';
  import DetallsReserva from './pages/DetallesReserva.svelte';
  import ComentarioNodo from './pages/ComentarioNodo.svelte';

  import Contactanos from './pages/Contactanos.svelte';
  import CentroAyuda from './pages/CentroAyuda.svelte';
  import PreguntasFrecuentes from './pages/PreguntasFrecuentes.svelte';
  import Privacidad from './pages/Privacidad.svelte';
  import Terminos from './pages/Terminos.svelte';
  import Cookies from './pages/Cookies.svelte';
  import PoliticaCancelacion from './pages/PoliticaCancelacion.svelte';
  import SobreNosotros from './pages/SobreNosotros.svelte';

  import './app.css';

  /** Clave de sessionStorage para persistir los parametros de busqueda de vuelos entre recargas. @type {string} */
  const SS_SEARCH_PARAMS = 'broom_searchParams';

  /** Clave de sessionStorage para persistir el nombre de la pagina activa. @type {string} */
  const SS_PAGE = 'broom_currentPage';

  /** Controla la visibilidad del splash de carga al iniciar la aplicacion. @type {boolean} */
  let isLoading = true;

  /** Se vuelve true una vez que la verificacion inicial de sesion con el backend ha terminado. @type {boolean} */
  let sesionCargada = false;

  /** Nombre de la pagina actualmente renderizada en el router outlet. @type {string} */
  let currentPage = 'home';

  /** Aeropuerto sugerido desde FlightNotification para pre-llenar el buscador del Home. @type {object|null} */
  let suggestedAeropuerto = null;

  /** ID del vuelo cuya vista de detalle se esta mostrando. @type {number|null} */
  let currentFlightId = null;

  /** Parametros de la ultima busqueda de vuelos, pasados a Vuelos y paginas relacionadas. @type {object|null} */
  let searchParams = null;

  /** Reservaciones confirmadas tras un checkout exitoso, pasadas a Confirmacion. @type {Array} */
  let reservacionesConfirmadas = [];

  /** Facturas generadas tras un checkout exitoso, pasadas a Confirmacion. @type {Array} */
  let facturasConfirmadas = [];

  /**
   * Timestamp numerico que se renueva en cada navegacion para forzar el re-mount
   * completo de paginas que deben reiniciar su estado interno al revisitarse.
   * @type {number}
   */
  let pageKey = Date.now();

  /** Payload de seleccion de asientos con grupos de vuelos, pasado a SeleccionAsientos. @type {Array|null} */
  let asientosData = null;

  /**
   * Rutas que requieren sesion autenticada. Usuarios no autenticados
   * son redirigidos al login al intentar acceder.
   * @type {string[]}
   */
  const paginasProtegidas = ['profile','admin','reservas','checkout','datos-pasajeros','carrito','seleccion-asientos','mi-agencia'];

  /**
   * Rutas exclusivas para usuarios con rol Administrador.
   * Cualquier otro rol autenticado es redirigido a acceso-denegado.
   * @type {string[]}
   */
  const paginasSoloAdmin = ['admin'];

  /**
   * Rutas exclusivas para usuarios con rolId === 3 (Webservice/Agencia).
   * Cualquier otro rol autenticado es redirigido a acceso-denegado.
   * @type {string[]}
   */
  const paginasSoloWebservice = ['mi-agencia'];

  /**
   * Rutas de paginas informativas estaticas como avisos legales,
   * centro de ayuda y contacto. No requieren autenticacion.
   * @type {string[]}
   */
  const infoPages = [
    'contactanos','centro-ayuda','preguntas-frecuentes',
    'privacidad','terminos','cookies','politica-cancelacion','sobre-nosotros',
  ];

  /**
   * Serializa los parametros de busqueda en sessionStorage para que
   * sobrevivan una recarga. Pasar null elimina la entrada almacenada.
   * @param {object|null} params - Parametros a persistir o null para limpiarlos.
   */
  function saveSearchParams(params) {
    try {
      if (params) sessionStorage.setItem(SS_SEARCH_PARAMS, JSON.stringify(params));
      else        sessionStorage.removeItem(SS_SEARCH_PARAMS);
    } catch (e) { }
  }

  /**
   * Lee y parsea los parametros de busqueda guardados en sessionStorage.
   * Retorna null si la entrada no existe o no puede parsearse.
   * @returns {object|null} Los parametros restaurados o null.
   */
  function loadSearchParams() {
    try {
      const raw = sessionStorage.getItem(SS_SEARCH_PARAMS);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  /**
   * Escribe el nombre de la pagina activa en sessionStorage para que
   * otro codigo que inspeccione el storage pueda referenciarlo.
   * @param {string} page - Nombre de la pagina a persistir.
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
    isLoading = false;

    return () => {
      window.removeEventListener('popstate', actualizarPaginaDesdeURL);
    };
  });

  /** Rastrea si el flujo de compra fue completado para bloquear re-entrada a paginas de reserva. @type {boolean} */
  let pagoCompletado = false;

  /**
   * Lista ordenada de rutas que forman el flujo de compra de boletos.
   * Se usa para impedir la navegacion hacia atras despues de confirmar el pago.
   * @type {string[]}
   */
  const paginasFlujoCompra = ['vuelos', 'datos-pasajeros', 'seleccion-asientos', 'carrito', 'checkout'];

  /**
   * Lee window.location.pathname para determinar la pagina activa y actualiza
   * el estado del componente. Aplica dos guardas: si el pago fue completado
   * redirige al home, y el acceso directo a /confirmacion se bloquea si no
   * hay reservaciones o facturas en memoria.
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
   * Evalua las reglas de proteccion de ruta para el nombre de pagina dado
   * contra el estado de sesion actual. Redirige usuarios no autenticados
   * al login y usuarios sin el rol requerido a acceso-denegado.
   * @param {string} page - Nombre de la ruta a evaluar.
   * @returns {string} El nombre de ruta resuelto que el usuario puede ver.
   */
  function resolverPagina(page) {
    if (!sesionCargada) return page;
    const sesionActual = $sesion;
    if (paginasProtegidas.includes(page) && !sesionActual) return 'login';
    if (paginasSoloAdmin.includes(page) && sesionActual?.rolNombre !== 'Administrador') return 'acceso-denegado';
    if (paginasSoloWebservice.includes(page) && sesionActual?.rolId !== 3) return 'acceso-denegado';
    return page;
  }

  /* Re-evalua la pagina cuando la sesion termina de cargar */
  $: if (sesionCargada && $sesion !== null) {
    const paginaResuelta = resolverPagina(currentPage);
    if (paginaResuelta !== currentPage) {
      currentPage = paginaResuelta;
      window.history.replaceState({}, '', '/' + paginaResuelta);
    }
  }

  /**
   * Realiza la navegacion programatica del SPA a la pagina indicada.
   * Ejecuta la proteccion de rutas, actualiza el historial del navegador,
   * persiste la nueva pagina en sessionStorage y distribuye los datos
   * a las variables de estado correctas segun el destino.
   * @param {string} page - Nombre de la ruta destino.
   * @param {object|null} [data=null] - Payload opcional cuya forma varia segun la pagina destino.
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
   * Recibe una sugerencia de aeropuerto del componente FlightNotification.
   * Guarda el aeropuerto en suggestedAeropuerto para que el buscador del Home
   * pre-llene el campo de destino. Si el usuario no esta en home, navega primero
   * y luego hace scroll a la seccion de busqueda.
   * @param {object} aeropuertoData - Datos del aeropuerto a pre-llenar en el buscador.
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
   * Paginas del flujo de compra que muestran el header simplificado
   * en lugar del header de navegacion completo.
   * @type {string[]}
   */
  const simpleHeaderPages = ['vuelos','carrito','datos-pasajeros','seleccion-asientos','checkout','confirmacion'];

  /**
   * Paginas que ocultan el Footer completamente, incluyendo el flujo de compra
   * y paginas de cuenta autenticada.
   * @type {string[]}
   */
  const noFooterPages = ['profile','admin','login','register','acceso-denegado','reservas','mi-agencia',...simpleHeaderPages];

  /**
   * Paginas que renderizan sin header (layouts standalone de pantalla completa).
   * @type {string[]}
   */
  const noHeaderPages = ['login','register'];

  /* True cuando la pagina usa el header simplificado */
  $: useSimpleHeader = simpleHeaderPages.includes(currentPage);
  /* True cuando la pagina debe mostrar el Header */
  $: showHeader      = !noHeaderPages.includes(currentPage);
  /* True cuando la pagina debe mostrar el Footer */
  $: showFooter      = !noFooterPages.includes(currentPage);
</script>

<!-- Splash de carga inicial mientras se verifica la sesion del usuario -->
{#if isLoading}
  <Loading />
{:else}

  <!-- Notificacion flotante de vuelos sugeridos por destino -->
  <FlightNotification on:suggest={e => handleDestinationSuggestion(e.detail)} />

  <!-- Header principal o simplificado segun la pagina activa -->
  {#if showHeader}
    {#if useSimpleHeader}
      <HeaderSimple {navigateTo} />
    {:else}
      <Header {navigateTo} sesion={$sesion} />
    {/if}
  {/if}

  <!-- Router outlet con key para forzar re-mount en cada navegacion -->
  {#key pageKey}

    {#if currentPage === 'home'}
      <Home {navigateTo} {suggestedAeropuerto} />

    {:else if currentPage === 'vuelos'}
      <Vuelos {navigateTo} {searchParams} />

    {:else if currentPage === 'resultados-busqueda'}
      <ResultadosBusqueda {navigateTo} {searchParams} />

    {:else if currentPage === 'confirmacion'}
      <Comfirmacion {navigateTo} reservaciones={reservacionesConfirmadas} facturas={facturasConfirmadas} />

    {:else if currentPage === 'carrito'}
      <Carrito {navigateTo} />

    {:else if currentPage === 'checkout'}
      <Checkout {navigateTo} />

    {:else if currentPage === 'datos-pasajeros'}
      <DatosPasajeros {navigateTo} />

    {:else if currentPage === 'seleccion-asientos'}
      <SeleccionAsientos {navigateTo} flightData={asientosData} />

    {:else if currentPage === 'login'}
      <Login {navigateTo} />

    {:else if currentPage === 'register'}
      <Register {navigateTo} />

    {:else if currentPage === 'profile'}
      <Profile {navigateTo} />

    {:else if currentPage === 'reservas'}
      <MisReservas {navigateTo} />

    {:else if currentPage === 'admin'}
      <Admin {navigateTo} />

    {:else if currentPage === 'mi-agencia'}
      <MiAgencia {navigateTo} />

    {:else if currentPage === 'informacion-asientos'}
      <InformacionAsientos {navigateTo} />

    {:else if currentPage === 'informacion-seguridad'}
      <InformacionSeguridad {navigateTo} />

    {:else if currentPage === 'acceso-denegado'}
      <AccesoDenegado {navigateTo} />

    {:else if currentPage === 'detalle-vuelo'}
      <DetalleVuelo {navigateTo} flightId={currentFlightId} />

    {:else if currentPage === 'detalle-vuelo-admin'}
      <DetalleVueloAdmin {navigateTo} flightId={currentFlightId} />

    {:else if currentPage === 'detalle-reserva'}
      <DetallsReserva {navigateTo} />

    {:else if currentPage === 'comentario-nodo'}
      <ComentarioNodo {navigateTo} />

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
      <!-- Fallback al home si la ruta no coincide con ninguna pagina registrada -->
      <Home {navigateTo} />
    {/if}

  {/key}

  <!-- Footer global oculto en paginas de cuenta y flujo de compra -->
  {#if showFooter}
    <Footer {navigateTo} />
  {/if}

{/if}