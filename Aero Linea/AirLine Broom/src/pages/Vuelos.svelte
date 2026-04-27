<script>
/**
 * @file Vuelos.svelte
 * @description Pagina principal de listado y seleccion de vuelos de Broom AirLine. Soporta dos modos de
 * entrada: una busqueda global por palabra clave (bandera fromGlobalSearch) que muestra una lista plana de
 * vuelos directos con un input de re-busqueda, y una busqueda estandar por origen/destino/fecha que muestra
 * pasos de vuelo de ida y regreso con pestanas de directo y escala. Los filtros (rango de precio, clase,
 * pestana de tipo de vuelo) se muestran solo en modo estandar y se aplican via POST /api/vuelos/buscar.
 * La re-busqueda global usa GET /api/vuelos/busqueda-general. Cada tarjeta de vuelo renderiza informacion
 * de horario y botones de opcion de clase para Turista y Ejecutiva; las tarjetas de escala adicionalmente
 * renderizan una linea de tiempo de escala con tiempos de espera de conexion. Seleccionar un vuelo y hacer
 * clic en el boton de siguiente paso llama a POST /api/reservaciones y navega a 'datos-pasajeros'. El componente
 * DetalleVuelo se monta como modal cuando el usuario hace clic en "Ver Detalles".
 */
// @ts-nocheck
  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';
  import { sesion } from '../stores/sesion.js';

  /** Funcion usada para navegar entre paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Objeto de parametros de busqueda pasado desde la pagina de inicio o la busqueda del header. @type {object|null} */
  export let searchParams = null;

  import { API } from '../lib/api.js';

  /** True cuando la pagina fue abierta desde una busqueda global por palabra clave via el header. @type {boolean} */
  let isGlobalSearch = false;

  /** La cadena de consulta original de la sesion de busqueda global actual. @type {string} */
  let globalSearchQuery = '';

  /** El valor actual del input de re-busqueda inline en modo de busqueda global. @type {string} */
  let newGlobalQuery = '';

  /** Objeto de parametros de busqueda con IDs de origen/destino, nombres, codigos, fechas, conteo de pasajeros y tipo de viaje. @type {object} */
  let searchData = {
    origenId: null, destinoId: null,
    origenNombre: '', destinoNombre: '',
    origenCodigo: '', destinoCodigo: '',
    fechaIda: '', fechaVuelta: '',
    pasajeros: 1, tripType: 'roundtrip',
    flightMode: 'todos'
  };

  /** Paso que se esta mostrando actualmente, ya sea 'outbound' o 'return'. @type {string} */
  let currentView = 'outbound';

  /** Vuelo de ida/escala y clase actualmente seleccionados, o todos null antes de la seleccion. @type {{type: string|null, flight: object|null, escala: object|null, clase: object|null}} */
  let selectedOutbound = { type: null, flight: null, escala: null, clase: null };

  /** Vuelo de regreso/escala y clase actualmente seleccionados, o todos null antes de la seleccion. @type {{type: string|null, flight: object|null, escala: object|null, clase: object|null}} */
  let selectedReturn   = { type: null, flight: null, escala: null, clase: null };

  /** True cuando el modal de detalle de vuelo esta abierto. @type {boolean} */
  let showDetailModal = false;

  /** Objeto de vuelo o escala actualmente mostrado en el modal de detalle. @type {object|null} */
  let detailFlight    = null;

  /** Listas de vuelos para la direccion de ida con arreglos directos y conEscala. @type {{directos: object[], conEscala: object[]}} */
  let vuelosIda    = { directos: [], conEscala: [] };

  /** Listas de vuelos para la direccion de regreso con arreglos directos y conEscala. @type {{directos: object[], conEscala: object[]}} */
  let vuelosVuelta = { directos: [], conEscala: [] };

  /** True mientras se obtienen los vuelos de ida. @type {boolean} */
  let loadingIda   = false;

  /** True mientras se obtienen los vuelos de regreso. @type {boolean} */
  let loadingVuelta = false;

  /** Mensaje de error para fallos al obtener los vuelos de ida. @type {string} */
  let errorIda     = '';

  /** Mensaje de error para fallos al obtener los vuelos de regreso. @type {string} */
  let errorVuelta  = '';

  /** True mientras la solicitud POST de creacion de reservacion esta en progreso. @type {boolean} */
  let creandoReserva = false;

  /** Mensaje de error mostrado si la creacion de la reservacion falla. @type {string} */
  let errorReserva   = '';

  /** Pestana activa para la vista de ida, 'directos' o 'escalas'. @type {string} */
  let tabIda    = 'directos';

  /** Pestana activa para la vista de regreso, 'directos' o 'escalas'. @type {string} */
  let tabVuelta = 'directos';

  /**
   * Opciones estaticas de clase de asiento disponibles para seleccion.
   * @type {Array<{id: number, tipoDeClase: string}>}
   */
  const clases = [
    { id: 1, tipoDeClase: 'Turista' },
    { id: 2, tipoDeClase: 'Ejecutiva' }
  ];

  /** Cadena de valor minimo del filtro de precio (vacia significa sin minimo). @type {string} */
  let precioMin = '';

  /** Cadena de valor maximo del filtro de precio (vacia significa sin maximo). @type {string} */
  let precioMax = '';

  /** Cadena de ID de clase seleccionado para el filtro de clase (vacia significa todas las clases). @type {string} */
  let claseSeleccionada = '';

  /** Calificacion minima de aerolinea para el filtro de rating (0 = sin filtro, 1-5 = minimo). @type {number} */
  let ratingMin = 0;

  /**
   * Retorna una calificacion determinista de 3-5 estrellas para una marca de aerolinea basada en su nombre.
   * @param {string} marca - Nombre de la aerolinea.
   * @returns {number} Calificacion entre 3 y 5.
   */
  function getAirlineRating(marca) {
    if (!marca) return 3;
    let sum = 0;
    for (let i = 0; i < marca.length; i++) sum += marca.charCodeAt(i);
    return (sum % 3) + 3;
  }

  if (searchParams?.fromGlobalSearch) {
    isGlobalSearch = true;
    globalSearchQuery = searchParams.globalSearchQuery || '';
    newGlobalQuery = globalSearchQuery;
    const vuelos = searchParams.globalSearchResults || [];
    vuelosIda = { directos: vuelos, conEscala: [] };
    tabIda = 'directos';
    searchData.tripType = 'oneway';
  } else if (searchParams?.searchData) {
    searchData   = searchParams.searchData;
    vuelosIda    = searchParams.vuelosIda   ?? { directos: [], conEscala: [] };
    vuelosVuelta = searchParams.vuelosVuelta ?? { directos: [], conEscala: [] };

    // Pre-cargar clase preferida desde el formulario del Home
    if (searchData.clasePreferida) claseSeleccionada = searchData.clasePreferida;

    const fm = searchData.flightMode ?? 'todos';
    if (fm === 'escalas') {
      tabIda = 'escalas'; tabVuelta = 'escalas';
    } else if (fm === 'directo') {
      tabIda = 'directos'; tabVuelta = 'directos';
    } else {
      tabIda    = (vuelosIda.directos?.length    ?? 0) > 0 ? 'directos' : 'escalas';
      tabVuelta = (vuelosVuelta.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
    }
  } else {
    errorIda = 'No se encontro informacion de busqueda. Realiza una nueva busqueda.';
  }

  // Listas de vuelos para la direccion activa actualmente (ida o regreso).
  $: currentVuelos    = currentView === 'outbound' ? vuelosIda : vuelosVuelta;

  // Pestana activa para la direccion actualmente mostrada.
  $: currentTab       = currentView === 'outbound' ? tabIda    : tabVuelta;

  // True mientras la direccion actualmente mostrada esta cargando.
  $: loading          = currentView === 'outbound' ? loadingIda    : loadingVuelta;

  // Mensaje de error para la direccion actualmente mostrada.
  $: errorActual      = currentView === 'outbound' ? errorIda      : errorVuelta;

  // Lista de vuelos directos para la direccion actual (sin filtro de rating).
  $: listaDirectosRaw = currentVuelos.directos  ?? [];

  // Lista de itinerarios con escala para la direccion actual (sin filtro de rating).
  $: listaEscalasRaw  = currentVuelos.conEscala ?? [];

  // Lista de vuelos directos filtrada por calificacion minima de aerolinea.
  $: listaDirectos = ratingMin > 0
    ? listaDirectosRaw.filter(v => getAirlineRating(v.avionMarca) >= ratingMin)
    : listaDirectosRaw;

  // Lista de escalas filtrada por calificacion minima de aerolinea (se evalua el primer tramo).
  $: listaEscalas = ratingMin > 0
    ? listaEscalasRaw.filter(e => getAirlineRating(e.tramos?.[0]?.avionMarca ?? '') >= ratingMin)
    : listaEscalasRaw;

  // Lista renderizada en el area de contenido principal segun currentTab.
  $: listaActiva      = currentTab === 'directos' ? listaDirectos : listaEscalas;

  // Total de resultados (directos + escalas) para la direccion actual.
  $: totalResultados  = listaDirectos.length + listaEscalas.length;

  // True cuando se ha seleccionado un vuelo para el paso activo actual.
  $: canProceed       = currentView === 'outbound'
      ? selectedOutbound.type !== null
      : selectedReturn.type   !== null;

  // Lista filtrada de directos (actualmente pasa sin cambios; la API devuelve datos pre-filtrados).
  $: listaDirectosFiltrada = filtrarSegunMode(listaDirectos);

  // Lista filtrada de escalas (actualmente pasa sin cambios).
  $: listaEscalasFiltrada  = filtrarSegunMode(listaEscalas);

  /**
   * Funcion de filtro de marcador; la API ya filtra por modo de vuelo por lo que retorna la lista sin cambios.
   * La visibilidad de las pestanas esta determinada por el valor flightMode en searchData.
   * @param {object[]} lista - Arreglo de lista de vuelos a filtrar.
   * @returns {object[]} El mismo arreglo lista sin cambios.
   */
  function filtrarSegunMode(lista) { return lista; }

  // True cuando la pestana Directos debe mostrarse segun flightMode y los resultados disponibles.
  $: mostrarTabDirectos = (searchData.flightMode === 'todos' || searchData.flightMode === 'directo') && listaDirectos.length > 0;

  // True cuando la pestana Escalas debe mostrarse segun flightMode y los resultados disponibles.
  $: mostrarTabEscalas  = (searchData.flightMode === 'todos' || searchData.flightMode === 'escalas')  && listaEscalas.length  > 0;

  /**
   * Establece la pestana activa para la direccion actual al valor dado ('directos' o 'escalas').
   * @param {string} tab - El identificador de pestana a activar.
   */
  function setTab(tab) {
    if (currentView === 'outbound') tabIda = tab;
    else tabVuelta = tab;
  }

  /**
   * Envia una solicitud GET a /api/vuelos/busqueda-general con la cadena newGlobalQuery y
   * reemplaza vuelosIda.directos con los resultados. Actualiza globalSearchQuery y reinicia la
   * seleccion de ida. Requiere al menos 2 caracteres para proceder.
   * @async
   * @returns {Promise<void>}
   */
  async function reBuscarGlobal() {
    const q = newGlobalQuery.trim();
    if (q.length < 2) return;
    loadingIda = true; errorIda = '';
    try {
      const res = await fetch(`${API}/api/vuelos/busqueda-general?query=${encodeURIComponent(q)}`, {
        credentials: 'include'
      });
      if (!res.ok) throw new Error();
      const vuelos = await res.json();
      vuelosIda = { directos: vuelos, conEscala: [] };
      globalSearchQuery = q;
      tabIda = 'directos';
      selectedOutbound = { type: null, flight: null, escala: null, clase: null };
    } catch {
      errorIda = 'Error al buscar vuelos.';
    } finally {
      loadingIda = false;
    }
  }

  /**
   * Construye un cuerpo de filtro desde precioMin, precioMax, claseSeleccionada y searchData, luego
   * envia una solicitud POST a /api/vuelos/buscar para la direccion de ida o regreso.
   * Actualiza el vuelosIda o vuelosVuelta correspondiente y auto-selecciona la primera pestana disponible.
   * No hace nada en modo de busqueda global.
   * @async
   * @param {boolean} esIda - True para buscar vuelos de ida, false para buscar vuelos de regreso.
   * @returns {Promise<void>}
   */
  async function buscarVuelos(esIda) {
    if (isGlobalSearch) return;
    const origen  = esIda ? searchData.origenId  : searchData.destinoId;
    const destino = esIda ? searchData.destinoId : searchData.origenId;
    const fecha   = esIda ? searchData.fechaIda  : searchData.fechaVuelta;
    if (!origen || !destino || !fecha) return;

    if (esIda) { loadingIda = true;    errorIda = ''; }
    else       { loadingVuelta = true; errorVuelta = ''; }

    try {
      const body = { origenId: origen, destinoId: destino, fecha, cantidadPasajeros: searchData.pasajeros };
      if (precioMin !== '') body.precioMinimo = parseFloat(precioMin);
      if (precioMax !== '') body.precioMaximo = parseFloat(precioMax);
      if (claseSeleccionada !== '') body.claseId = parseInt(claseSeleccionada);

      const res = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error();
      const data = await res.json();

      if (esIda) {
        vuelosIda = data;
        tabIda = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      } else {
        vuelosVuelta = data;
        tabVuelta = (data.directos?.length ?? 0) > 0 ? 'directos' : 'escalas';
      }
    } catch {
      if (esIda) errorIda = 'No se pudieron cargar los vuelos de ida.';
      else errorVuelta = 'No se pudieron cargar los vuelos de vuelta.';
    } finally {
      if (esIda) loadingIda = false;
      else loadingVuelta = false;
    }
  }

  /**
   * Aplica los valores de filtro actuales llamando a reBuscarGlobal en modo global o buscarVuelos
   * para la direccion actual en modo estandar.
   * @async
   * @returns {Promise<void>}
   */
  async function aplicarFiltros() {
    if (isGlobalSearch) { reBuscarGlobal(); return; }
    await buscarVuelos(currentView === 'outbound');
  }

  /**
   * Limpia los campos de filtro de precio y clase, luego aplica los filtros inmediatamente para actualizar resultados.
   */
  function limpiarFiltros() {
    precioMin = ''; precioMax = ''; claseSeleccionada = ''; ratingMin = 0;
    aplicarFiltros();
  }

  /**
   * Establece la seleccion de ida o regreso a un vuelo directo con la clase dada.
   * @param {object} vuelo - El objeto de vuelo directo a seleccionar.
   * @param {object} clase - El objeto de clase ({ id, tipoDeClase }) a seleccionar.
   */
  function selectDirecto(vuelo, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'directo', flight: vuelo, escala: null, clase };
    else selectedReturn = { type: 'directo', flight: vuelo, escala: null, clase };
  }

  /**
   * Retorna true si la combinacion de vuelo directo y clase dada esta actualmente seleccionada para la
   * direccion activa.
   * @param {object} vuelo - El objeto de vuelo directo a verificar.
   * @param {object} clase - El objeto de clase a verificar.
   * @returns {boolean} Si este vuelo+clase es la seleccion activa.
   */
  function isSelectedDirecto(vuelo, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    return s.type === 'directo' && s.flight?.id === vuelo.id && s.clase?.id === clase.id;
  }

  /**
   * Establece la seleccion de ida o regreso a un itinerario con escala con la clase dada.
   * @param {object} escala - El objeto de itinerario de escala a seleccionar.
   * @param {object} clase - El objeto de clase ({ id, tipoDeClase }) a seleccionar.
   */
  function selectEscala(escala, clase) {
    if (currentView === 'outbound') selectedOutbound = { type: 'escala', flight: null, escala, clase };
    else selectedReturn = { type: 'escala', flight: null, escala, clase };
  }

  /**
   * Retorna true si la combinacion de itinerario de escala y clase dada esta actualmente seleccionada para la
   * direccion activa, comparando toda la secuencia de tramos (no solo el primero).
   * @param {object} escala - El objeto de itinerario de escala a verificar.
   * @param {object} clase - El objeto de clase a verificar.
   * @returns {boolean} Si esta escala+clase es la seleccion activa.
   */
  function isSelectedEscala(escala, clase) {
    const s = currentView === 'outbound' ? selectedOutbound : selectedReturn;
    if (s.type !== 'escala' || s.clase?.id !== clase.id) return false;

    const selectedTramos = s.escala?.tramos ?? [];
    const currentTramos = escala?.tramos ?? [];

    if (selectedTramos.length !== currentTramos.length) return false;

    return selectedTramos.every((t, idx) => t.id === currentTramos[idx]?.id);
  }

  /**
   * Abre el modal de detalle de vuelo estableciendo detailFlight y showDetailModal.
   * @param {object} vuelo - El objeto de vuelo o escala a mostrar en el modal.
   */
  function viewDetails(vuelo) { detailFlight = vuelo; showDetailModal = true; }

  /**
   * Cierra el modal de detalle de vuelo limpiando showDetailModal y detailFlight.
   */
  function closeModal()       { showDetailModal = false; detailFlight = null; }

  /**
   * Maneja el boton de siguiente paso. Para una seleccion de ida en viaje de ida y vuelta, cambia currentView a
   * 'return' y obtiene vuelos de regreso si no se han cargado aun. Para solo ida o finalizacion del regreso,
   * llama a crearReserva directamente.
   * @async
   * @returns {Promise<void>}
   */
  async function nextStep() {
    errorReserva = '';
    if (currentView === 'outbound' && selectedOutbound.type) {
      if (!isGlobalSearch && searchData.tripType === 'roundtrip') {
        currentView = 'return';
        if ((vuelosVuelta.directos?.length ?? 0) === 0 && (vuelosVuelta.conEscala?.length ?? 0) === 0)
          await buscarVuelos(false);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        await crearReserva();
      }
    } else if (currentView === 'return' && selectedReturn.type) {
      await crearReserva();
    }
  }

  /**
   * Construye el payload de vuelos desde selectedOutbound y selectedReturn usando _agregarVuelos, luego
   * lo publica en POST /api/reservaciones. Al tener exito navega a 'datos-pasajeros' con la respuesta
   * de reservacion y searchData. Redirige al login si no hay sesion activa. En caso de fallo
   * establece errorReserva con el mensaje del servidor o de conexion.
   * @async
   * @returns {Promise<void>}
   */
  async function crearReserva() {
    const sesionActual = $sesion;
    if (!sesionActual) { navigateTo('login'); return; }

    creandoReserva = true;
    errorReserva   = '';
    const pasajeros = Number(searchData.pasajeros) || 1;
    const vuelos = [];
    _agregarVuelos(vuelos, selectedOutbound, pasajeros);
    if (selectedReturn.type) _agregarVuelos(vuelos, selectedReturn, pasajeros);

    console.log('Body reserva:', JSON.stringify({ vuelos }, null, 2));
    try {
      const res = await fetch(`${API}/api/reservaciones`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ vuelos })
      });
      const data = await res.json();
      if (!res.ok) { errorReserva = data.message ?? 'Error al crear la reservacion.'; return; }
      navigateTo('datos-pasajeros', { reserva: data, searchData });
    } catch {
      errorReserva = 'Error de conexion al crear la reservacion.';
    } finally {
      creandoReserva = false;
    }
  }

  /**
   * Agrega objetos de entrada de vuelo al arreglo arr desde un objeto de seleccion. Para selecciones de directo,
   * agrega una entrada con vueloId, claseId y cantidadPasajeros. Para selecciones de escala, agrega una
   * entrada por tramo en el itinerario.
   * @param {Array<object>} arr - El arreglo destino al que se agregan las entradas de vuelo.
   * @param {{type: string, flight: object|null, escala: object|null, clase: object}} sel - Objeto de seleccion.
   * @param {number} pasajeros - Numero de pasajeros para cada entrada.
   */
  function _agregarVuelos(arr, sel, pasajeros) {
    if (sel.type === 'directo') {
      arr.push({ vueloId: Number(sel.flight.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    } else if (sel.type === 'escala') {
      for (const tramo of sel.escala.tramos)
        arr.push({ vueloId: Number(tramo.id), claseId: Number(sel.clase.id), cantidadPasajeros: pasajeros });
    }
  }

  /**
   * Navega hacia atras: cambia de 'return' a 'outbound' con scroll suave, o regresa a 'home'
   * si ya se esta en el paso de ida.
   */
  function goBack() {
    if (currentView === 'return') { currentView = 'outbound'; window.scrollTo({ top: 0, behavior: 'smooth' }); }
    else navigateTo('home');
  }

  /**
   * Formatea una duracion en minutos como una cadena legible como '2h 30m'.
   * @param {number} min - Duracion en minutos.
   * @returns {string} Cadena de duracion formateada, o cadena vacia si min es falsy.
   */
  function formatDuracion(min) {
    if (!min) return '';
    return `${Math.floor(min / 60)}h ${min % 60}m`;
  }

  /**
   * Extrae la porcion HH:MM de una cadena de hora, o retorna cadena vacia si es falsy.
   * @param {string} h - Cadena de hora en formato HH:MM:SS o HH:MM.
   * @returns {string} Los primeros 5 caracteres (HH:MM), o cadena vacia.
   */
  function formatHora(h) { return h ? h.substring(0, 5) : ''; }

  /**
   * Formats a date string using Spanish (Guatemala) locale with day, month abbreviation, and year.
   * Falls back to the original string on parse error.
   * @param {string} f - ISO date string or date-compatible string.
   * @returns {string} Localized date string, or empty string if f is falsy.
   */
  function formatFecha(f) {
    if (!f) return '';
    try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }); }
    catch { return f; }
  }

  /**
   * Returns the price for a direct flight given a class ID (1=Turista, 2=Ejecutiva), or null.
   * @param {object} vuelo - Direct flight object with precioTurista and precioEjecutiva properties.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number|null} The price, or null if class ID is unrecognized.
   */
  function getPrecioDirecto(vuelo, claseId) {
    return claseId === 1 ? vuelo.precioTurista : claseId === 2 ? vuelo.precioEjecutiva : null;
  }

  /**
   * Returns the available ticket count for a direct flight given a class ID.
   * @param {object} vuelo - Direct flight object with boletosDisponiblesTurista and boletosDisponiblesEjecutiva.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number} Available ticket count, or 0 if class ID is unrecognized.
   */
  function getBoletosDirecto(vuelo, claseId) {
    return claseId === 1 ? (vuelo.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (vuelo.boletosDisponiblesEjecutiva ?? 0) : 0;
  }

  /**
   * Returns the total price for a layover itinerary given a class ID.
   * @param {object} escala - Escala itinerary object with precioTuristaTotal and precioEjecutivaTotal.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number|null} The total itinerary price, or null if class ID is unrecognized.
   */
  function getPrecioEscala(escala, claseId) {
    return claseId === 1 ? escala.precioTuristaTotal : claseId === 2 ? escala.precioEjecutivaTotal : null;
  }

  /**
   * Returns the minimum available ticket count across all tramos for a layover itinerary and class.
   * @param {object} escala - Escala itinerary object with boletosDisponiblesTurista and boletosDisponiblesEjecutiva.
   * @param {number} claseId - Class identifier (1 or 2).
   * @returns {number} Available ticket count, or 0 if class ID is unrecognized.
   */
  function getBoletosEscala(escala, claseId) {
    return claseId === 1 ? (escala.boletosDisponiblesTurista ?? 0) : claseId === 2 ? (escala.boletosDisponiblesEjecutiva ?? 0) : 0;
  }

  /**
   * Formats a numeric price as a USD string with two decimal places, e.g. '$ 1,234.00'.
   * Returns 'No disponible' if the value is falsy.
   * @param {number} p - Price value to format.
   * @returns {string} Formatted price string.
   */
  function formatPrecio(p) {
    if (!p) return 'No disponible';
    return `$ ${p.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  /**
   * Returns a human-readable layover count label such as '1 escala' or '2 escalas'.
   * @param {number} n - Number of layovers.
   * @returns {string} Label string.
   */
  function labelEscala(n) { return n === 1 ? '1 escala' : `${n} escalas`; }
</script>

<!-- Modal de detalle de vuelo, montado sobre la pagina cuando el usuario solicita ver mas informacion -->
{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<!-- Contenedor principal de la pagina de listado y seleccion de vuelos -->
<div class="vuelos-page">
  <div class="vuelos-page__container">

    <!-- Cabecera con boton de regreso, resumen de busqueda e indicador de pasos ida/vuelta -->
    <div class="vuelos-page__header">
      <button class="vuelos-page__back" on:click={goBack}>Volver</button>

      {#if isGlobalSearch}
        <!-- Resumen de busqueda global con campo de re-busqueda en tiempo real -->
        <div class="search-summary">
          <div class="search-summary__route">
            <span class="search-summary__origin">Resultados para</span>
            <span class="search-summary__destination">"{globalSearchQuery}"</span>
          </div>
          <div class="search-summary__details">
            <div class="search-summary__item">
              <span class="search-summary__label">Vuelos encontrados</span>
              <span class="search-summary__value">{listaDirectos.length}</span>
            </div>
          </div>
          <div style="display:flex; gap:0.5rem; margin-top:1rem;">
            <input type="text" bind:value={newGlobalQuery}
              on:keydown={e => e.key === 'Enter' && reBuscarGlobal()}
              placeholder="Buscar otra vez..."
              style="flex:1; padding:0.6rem 1rem; border:1px solid #B89A7A; border-radius:20px; font-size:0.85rem; font-family:inherit; outline:none;"
            >
            <button on:click={reBuscarGlobal}
              style="padding:0.6rem 1.2rem; background:#1C1A18; color:#fff; border:none; border-radius:20px; cursor:pointer; font-family:inherit; font-size:0.8rem; font-weight:600;">
              Buscar
            </button>
          </div>
        </div>
      {:else}
        <!-- Resumen de busqueda estandar: ruta, fechas, pasajeros y tipo de vuelo seleccionado -->
        <div class="search-summary">
          <div class="search-summary__route">
            <span class="search-summary__origin">{searchData.origenNombre || 'Origen'}</span>
            <span class="search-summary__arrow">→</span>
            <span class="search-summary__destination">{searchData.destinoNombre || 'Destino'}</span>
          </div>
          <div class="search-summary__details">
            <div class="search-summary__item">
              <span class="search-summary__label">Salida</span>
              <span class="search-summary__value">{searchData.fechaIda || '—'}</span>
            </div>
            {#if searchData.tripType === 'roundtrip'}
              <div class="search-summary__item">
                <span class="search-summary__label">Regreso</span>
                <span class="search-summary__value">{searchData.fechaVuelta || '—'}</span>
              </div>
            {/if}
            <div class="search-summary__item">
              <span class="search-summary__label">Pasajeros</span>
              <span class="search-summary__value">{searchData.pasajeros}</span>
            </div>
            {#if searchData.flightMode && searchData.flightMode !== 'todos'}
              <div class="search-summary__item">
                <span class="search-summary__label">Tipo</span>
                <span class="search-summary__value search-summary__value--mode">
                  {searchData.flightMode === 'directo' ? '✈ Directo' : '⇌ Con escalas'}
                </span>
              </div>
            {/if}
          </div>
        </div>

        <!-- Indicador de pasos del flujo de seleccion: vuelo de ida y vuelo de vuelta -->
        <div class="step-indicator">
          <div class="step-indicator__item"
            class:step-indicator__item--active={currentView === 'outbound'}
            class:step-indicator__item--completed={selectedOutbound.type}>
            <span class="step-indicator__number">1</span>
            <span class="step-indicator__label">Vuelo de Ida</span>
          </div>
          {#if searchData.tripType === 'roundtrip'}
            <div class="step-indicator__line"></div>
            <div class="step-indicator__item"
              class:step-indicator__item--active={currentView === 'return'}
              class:step-indicator__item--completed={selectedReturn.type}>
              <span class="step-indicator__number">2</span>
              <span class="step-indicator__label">Vuelo de Vuelta</span>
            </div>
          {/if}
        </div>
      {/if}
    </div>

    <div class="vuelos-page__content" style={isGlobalSearch ? 'grid-template-columns: 1fr;' : ''}>

      <!-- Panel lateral de filtros por tipo de vuelo, precio y clase (oculto en busqueda global) -->
      {#if !isGlobalSearch}
        <aside class="vuelos-page__filters">
          <div class="filters-panel">
            <div class="filters-panel__header">
              <h3 class="filters-panel__title">Filtros</h3>
              <button class="filters-panel__clear" on:click={limpiarFiltros}>Limpiar</button>
            </div>

            <!-- Selector de tab: vuelos directos vs vuelos con escalas -->
            <div class="filter-group">
              <span class="filter-group__label">Tipo de vuelo</span>
              <div class="filter-flight-mode">
                <button
                  class="filter-mode-btn"
                  class:filter-mode-btn--active={currentTab === 'directos'}
                  on:click={() => setTab('directos')}
                  disabled={listaDirectos.length === 0}
                >
                  <span class="filter-mode-btn__icon">✈</span>
                  <span class="filter-mode-btn__text">Directo</span>
                  {#if listaDirectos.length > 0}
                    <span class="filter-mode-btn__count">{listaDirectos.length}</span>
                  {/if}
                </button>
                <button
                  class="filter-mode-btn filter-mode-btn--escala"
                  class:filter-mode-btn--active={currentTab === 'escalas'}
                  on:click={() => setTab('escalas')}
                  disabled={listaEscalas.length === 0}
                >
                  <span class="filter-mode-btn__icon">⇌</span>
                  <span class="filter-mode-btn__text">Con escalas</span>
                  {#if listaEscalas.length > 0}
                    <span class="filter-mode-btn__count">{listaEscalas.length}</span>
                  {/if}
                </button>
              </div>
            </div>

            <!-- Filtro de rango de precio minimo y maximo en USD -->
            <div class="filter-group">
              <label class="filter-group__label" for="precioMin">Rango de Precio (USD)</label>
              <div class="filter-group__price-range">
                <input id="precioMin" type="number" class="filter-group__input" placeholder="Min" bind:value={precioMin} />
                <span>-</span>
                <input type="number" class="filter-group__input" placeholder="Max" bind:value={precioMax} />
              </div>
            </div>

            <!-- Filtro de clase de asiento (Turista / Ejecutiva) -->
            <div class="filter-group">
              <label class="filter-group__label" for="filtroClase">Clase</label>
              <div class="filter-group__select">
                <select id="filtroClase" class="filter-group__select-element" bind:value={claseSeleccionada}>
                  <option value="">Todas</option>
                  {#each clases as c}
                    <option value={c.id}>{c.tipoDeClase}</option>
                  {/each}
                </select>
              </div>
            </div>

            <!-- Filtro de calificacion minima de aerolinea (1-5 estrellas, aplicado en cliente) -->
            <div class="filter-group">
              <span class="filter-group__label">Calificación mínima</span>
              <div class="filter-rating">
                {#each [0, 3, 4, 5] as r}
                  <button
                    type="button"
                    class="filter-rating__btn"
                    class:filter-rating__btn--active={ratingMin === r}
                    on:click={() => ratingMin = r}
                  >
                    {#if r === 0}
                      Todas
                    {:else}
                      {'★'.repeat(r)}{'☆'.repeat(5 - r)}
                    {/if}
                  </button>
                {/each}
              </div>
            </div>

            <button class="filters-panel__apply" on:click={aplicarFiltros}>Aplicar Filtros</button>
          </div>
        </aside>
      {/if}

      <!-- Area principal con titulo, contador de resultados y lista de vuelos -->
      <div class="vuelos-page__main">

        <!-- Encabezado de la lista con titulo dinamico y conteo de opciones disponibles -->
        <div class="flights-header">
          <h2 class="flights-header__title">
            {#if isGlobalSearch}
              Vuelos disponibles
            {:else}
              {currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}
            {/if}
          </h2>
          <p class="flights-header__subtitle">
            {#if loading}
              Buscando vuelos...
            {:else if isGlobalSearch}
              {totalResultados} vuelos encontrados para "{globalSearchQuery}"
            {:else}
              {totalResultados} opciones — mostrando {currentTab === 'directos' ? 'vuelos directos' : 'vuelos con escalas'}
            {/if}
          </p>
        </div>

        <!-- Estado de carga, error o sin resultados mientras se procesa la busqueda -->
        {#if loading}
          <div class="vuelos-estado">Buscando vuelos...</div>

        {:else if errorActual}
          <div class="vuelos-estado vuelos-estado--info">{errorActual}</div>

        {:else if totalResultados === 0}
          <div class="vuelos-estado">
            {#if isGlobalSearch}
              No se encontraron vuelos para "{globalSearchQuery}". Intenta con otra busqueda.
            {:else}
              No hay vuelos disponibles para esta ruta y fecha.
            {/if}
          </div>

        {:else if currentTab === 'directos'}

          <!-- Grilla de tarjetas de vuelos directos con horario, duracion y opciones de clase -->
          {#if listaDirectos.length === 0}
            <div class="vuelos-estado">No hay vuelos directos. Cambia a "Con escalas" en el panel de filtros.</div>
          {:else}
            <div class="flights-list">
              {#each listaDirectos as vuelo}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'directo' && selObj.flight?.id === vuelo.id}

                <div class="flight-card" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#c9a96e" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#c9a96e" stroke-width="0.6"/>
                    <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#c9a96e" opacity="0.18"/>
                    <line x1="60" y1="125" x2="200" y2="125" stroke="#c9a96e" stroke-width="0.5" stroke-dasharray="4 7"/>
                    <line x1="80" y1="135" x2="200" y2="135" stroke="#c9a96e" stroke-width="0.4" stroke-dasharray="3 8"/>
                  </svg>

                  <div class="flight-card__content">
                    <div class="flight-card__header">
                      <div class="flight-card__code-info">
                        <span class="flight-card__code">{vuelo.numeroVuelo || 'N/A'}</span>
                        <span class="flight-card__airline">{vuelo.avionMarca || ''} {vuelo.avionModelo || ''}</span>
                        {#if vuelo.avionMarca}
                          <span class="flight-card__rating" title="Calificación de la aerolínea">
                            {'★'.repeat(getAirlineRating(vuelo.avionMarca))}{'☆'.repeat(5 - getAirlineRating(vuelo.avionMarca))}
                          </span>
                        {/if}
                      </div>
                      {#if isGlobalSearch && vuelo.fecha}
                        <span class="flight-card__badge flight-card__badge--directo">{formatFecha(vuelo.fecha)}</span>
                      {:else}
                        <span class="flight-card__badge flight-card__badge--directo">Directo</span>
                      {/if}
                    </div>

                    <!-- Linea de horario: hora de salida, duracion del vuelo y hora de llegada -->
                    <div class="flight-card__schedule">
                      <div class="schedule-point">
                        <span class="schedule-point__time">{formatHora(vuelo.horaSalida)}</span>
                        <span class="schedule-point__code">{vuelo.origenCodigo || ''}</span>
                      </div>
                      <div class="schedule-duration">
                        <div class="schedule-duration__track">
                          <div class="schedule-duration__dot"></div>
                          <div class="schedule-duration__line"></div>
                          <span class="schedule-duration__plane">✈</span>
                          <div class="schedule-duration__line"></div>
                          <div class="schedule-duration__dot"></div>
                        </div>
                        <span class="schedule-duration__time">{formatDuracion(vuelo.duracionMinutos)}</span>
                        {#if isGlobalSearch}
                          <span class="schedule-duration__type">{vuelo.origenCiudad || ''} → {vuelo.destinoCiudad || ''}</span>
                        {:else}
                          <span class="schedule-duration__type">Directo</span>
                        {/if}
                      </div>
                      <div class="schedule-point schedule-point--right">
                        <span class="schedule-point__time">{formatHora(vuelo.horaLlegada)}</span>
                        <span class="schedule-point__code">{vuelo.destinoCodigo || ''}</span>
                      </div>
                    </div>

                    <!-- Botones de seleccion de clase con precio y disponibilidad por tipo -->
                    <div class="flight-card__class-selection">
                      {#each (claseSeleccionada !== '' ? clases.filter(c => String(c.id) === String(claseSeleccionada)) : clases) as clase}
                        {@const precio     = getPrecioDirecto(vuelo, clase.id)}
                        {@const boletos    = getBoletosDirecto(vuelo, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= (isGlobalSearch ? 1 : searchData.pasajeros)}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedDirecto(vuelo, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectDirecto(vuelo, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedDirecto(vuelo, clase) ? 'Seleccionado ✓' : 'por persona'}</span>
                          {:else}
                            <span class="class-option__label class-option__label--unavailable">No disponible</span>
                          {/if}
                        </button>
                      {/each}
                    </div>

                    <button class="flight-card__details-btn" on:click={() => viewDetails(vuelo)}>
                      Ver Detalles
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {/if}

        {:else}
          <!-- Grilla de tarjetas de vuelos con escalas, con timeline de tramos y conexiones -->
          {#if listaEscalas.length === 0}
            <div class="vuelos-estado">No hay vuelos con escalas.</div>
          {:else}
            <div class="flights-list">
              {#each listaEscalas as escala}
                {@const selObj = currentView === 'outbound' ? selectedOutbound : selectedReturn}
                {@const estaSeleccionado = selObj.type === 'escala' && selObj.escala?.tramos?.length === escala?.tramos?.length && selObj.escala?.tramos?.every((t, idx) => t.id === escala?.tramos?.[idx]?.id)}

                <div class="flight-card flight-card--escala" class:flight-card--selected={estaSeleccionado}>
                  <svg class="flight-card__deco" viewBox="0 0 200 140" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                    <circle cx="190" cy="15" r="90" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="62" stroke="#3b4f6b" stroke-width="0.6"/>
                    <circle cx="190" cy="15" r="36" stroke="#3b4f6b" stroke-width="0.6"/>
                    <path d="M95 72 L148 48 L160 53 L142 66 L156 62 L160 68 L128 79 Z" fill="#3b4f6b" opacity="0.18"/>
                    <line x1="60" y1="125" x2="200" y2="125" stroke="#3b4f6b" stroke-width="0.5" stroke-dasharray="4 7"/>
                  </svg>

                  <div class="flight-card__content">
                    <div class="flight-card__header">
                      <div class="flight-card__code-info">
                        <span class="flight-card__code">
                          {escala.tramos[0]?.origenCodigo} → {escala.tramos[escala.tramos.length - 1]?.destinoCodigo}
                        </span>
                        <span class="flight-card__airline">
                          {escala.tramos.map(t => t.numeroVuelo).join(' · ')}
                        </span>
                        {#if escala.tramos[0]?.avionMarca}
                          {@const r = getAirlineRating(escala.tramos[0].avionMarca)}
                          <span class="flight-card__rating" title="Calificación de la aerolínea">
                            {'★'.repeat(r)}{'☆'.repeat(5 - r)}
                          </span>
                        {/if}
                      </div>
                      <span class="flight-card__badge flight-card__badge--escala">
                        {labelEscala(escala.numeroEscalas)}
                      </span>
                    </div>

                    <!-- Timeline de tramos con aeropuerto de origen, conexion y aeropuerto de destino por segmento -->
                    <div class="escala-timeline">
                      {#each escala.tramos as tramo, ti}
                        <div class="escala-tramo">
                          <div class="escala-tramo__airports">
                            <div class="escala-tramo__point">
                              <span class="escala-tramo__time">{formatHora(tramo.horaSalida)}</span>
                              <span class="escala-tramo__code">{tramo.origenCodigo}</span>
                              <span class="escala-tramo__city">{tramo.origenCiudad}</span>
                            </div>
                            <div class="escala-tramo__middle">
                              <div class="escala-tramo__track">
                                <div class="escala-tramo__dot"></div>
                                <div class="escala-tramo__line"></div>
                                <span class="escala-tramo__plane">✈</span>
                                <div class="escala-tramo__line"></div>
                                <div class="escala-tramo__dot"></div>
                              </div>
                              <span class="escala-tramo__dur">{formatDuracion(tramo.duracionMinutos)}</span>
                              <span class="escala-tramo__num">{tramo.numeroVuelo}</span>
                            </div>
                            <div class="escala-tramo__point escala-tramo__point--right">
                              <span class="escala-tramo__time">{formatHora(tramo.horaLlegada)}</span>
                              <span class="escala-tramo__code">{tramo.destinoCodigo}</span>
                              <span class="escala-tramo__city">{tramo.destinoCiudad}</span>
                            </div>
                          </div>
                        </div>

                        <!-- Insignia de conexion con tiempo de espera entre tramos consecutivos -->
                        {#if ti < escala.tramos.length - 1}
                            {@const llegada = new Date(`1970-01-01T${escala.tramos[ti].horaLlegada}`)}
                            {@const salida  = new Date(`1970-01-01T${escala.tramos[ti + 1].horaSalida}`)}
                            {@const minutos = ((salida - llegada) / 60000 + 1440) % 1440}
                            <div class="escala-conexion">
                              <div class="escala-conexion__line"></div>
                              <div class="escala-conexion__badge">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                  <circle cx="12" cy="12" r="4"/>
                                </svg>
                                Escala · {formatDuracion(minutos)} en {escala.tramos[ti].destinoCiudad}
                              </div>
                              <div class="escala-conexion__line"></div>
                            </div>
                          {/if}
                      {/each}
                    </div>

                    <!-- Resumen de duracion total y numero de escalas del itinerario -->
                    <div class="escala-resumen">
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Duracion total</span>
                        <span class="escala-resumen__value">{formatDuracion(escala.duracionTotalMinutos)}</span>
                      </div>
                      <div class="escala-resumen__item">
                        <span class="escala-resumen__label">Escalas</span>
                        <span class="escala-resumen__value">{escala.numeroEscalas}</span>
                      </div>
                    </div>

                    <!-- Botones de seleccion de clase con precio total y disponibilidad para vuelos con escalas -->
                    <div class="flight-card__class-selection">
                      {#each (claseSeleccionada !== '' ? clases.filter(c => String(c.id) === String(claseSeleccionada)) : clases) as clase}
                        {@const precio     = getPrecioEscala(escala, clase.id)}
                        {@const boletos    = getBoletosEscala(escala, clase.id)}
                        {@const disponible = precio !== null && precio > 0 && boletos >= searchData.pasajeros}
                        <button
                          class="class-option"
                          class:class-option--selected={isSelectedEscala(escala, clase)}
                          class:class-option--disabled={!disponible}
                          disabled={!disponible}
                          on:click={() => disponible && selectEscala(escala, clase)}
                        >
                          <span class="class-option__name">{clase.tipoDeClase}</span>
                          {#if disponible}
                            <span class="class-option__price">{formatPrecio(precio)}</span>
                            <span class="class-option__label">{isSelectedEscala(escala, clase) ? 'Seleccionado ✓' : 'precio total'}</span>
                          {:else}
                            <span class="class-option__label class-option__label--unavailable">No disponible</span>
                          {/if}
                        </button>
                      {/each}
                    </div>

                    <button class="flight-card__details-btn" on:click={() => viewDetails(escala)}>
                      Ver Detalles de la Ruta
                    </button>
                  </div>
                </div>
              {/each}
            </div>
          {/if}
        {/if}
      </div>
    </div>

    <!-- Mensaje de error al intentar crear la reservacion -->
    {#if errorReserva}
      <div class="vuelos-error-reserva">{errorReserva}</div>
    {/if}

    <!-- Barra fija de accion con boton para avanzar al siguiente paso del flujo de compra -->
    {#if canProceed}
      <div class="vuelos-page__next-step">
        <button class="next-step-btn" on:click={nextStep} disabled={creandoReserva}>
          {#if creandoReserva}
            Creando reservacion...
          {:else if !isGlobalSearch && currentView === 'outbound' && searchData.tripType === 'roundtrip'}
            Seleccionar Vuelo de Vuelta
          {:else}
            Siguiente Paso
          {/if}
        </button>
      </div>
    {/if}

  </div>
</div>
