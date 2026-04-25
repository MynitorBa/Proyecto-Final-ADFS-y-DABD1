<script>
/**
 * @file Home.svelte
 * @description Pagina de inicio principal de la aplicacion Broom AirLine. Renderiza un banner hero,
 * un formulario de busqueda de vuelos con autocompletado de origen/destino, selector de cantidad de
 * pasajeros, calendarios duales interactivos que resaltan las fechas de vuelos disponibles obtenidas
 * de la API, y una grilla de destinos destacados provenientes de aeropuertos con imagen almacenada.
 * Envia una busqueda de vuelos a la API y navega a la pagina de Vuelos con los resultados.
 */
  import '../styles/home.css';
  import logoHero from '../assets/BroomHero1.png';
  import { onMount } from 'svelte';

  /** Funcion usada para navegar entre paginas de la aplicacion. @type {function} */
  export let navigateTo;

  /** Objeto de aeropuerto opcional pre-rellenado como destino al llegar desde una tarjeta de destino destacado o la animacion del avion en vuelo. @type {object|null} */
  export let suggestedAeropuerto = null;

  import { API } from '../lib/api.js';

  /** Seleccion del tipo de viaje: 'roundtrip' para ida y vuelta o 'oneway' para solo ida. @type {string} */
  let tripType      = 'roundtrip';

  /** Fecha de salida seleccionada en formato YYYY-MM-DD. @type {string} */
  let departureDate = '';

  /** Fecha de regreso seleccionada en formato YYYY-MM-DD, usada solo cuando tripType es 'roundtrip'. @type {string} */
  let returnDate    = '';

  /** Numero de pasajeros seleccionado por el usuario (1-9). @type {number} */
  let passengers    = 1;

  /** Lista completa de objetos de aeropuerto obtenidos de la API al montar. @type {Array<object>} */
  let aeropuertos        = [];

  /** True mientras se obtiene la lista de aeropuertos de la API. @type {boolean} */
  let loadingAeropuertos = true;

  /** Texto actual en el input de autocompletado de origen. @type {string} */
  let fromQuery          = '';

  /** Lista filtrada de aeropuertos que coinciden con la consulta de origen, limitada a 5 resultados. @type {Array<object>} */
  let fromSugeridos      = [];

  /** El objeto de aeropuerto seleccionado como origen, o null si aun no se ha elegido. @type {object|null} */
  let fromSeleccionado   = null;

  /** Texto actual en el input de autocompletado de destino. @type {string} */
  let toQuery            = '';

  /** Lista filtrada de aeropuertos que coinciden con la consulta de destino, limitada a 5 resultados. @type {Array<object>} */
  let toSugeridos        = [];

  /** El objeto de aeropuerto seleccionado como destino, o null si aun no se ha elegido. @type {object|null} */
  let toSeleccionado     = null;

  /** Arreglo de cadenas de fecha (YYYY-MM-DD) con vuelos de ida disponibles para la ruta seleccionada. @type {Array<string>} */
  let fechasDisponiblesIda    = [];

  /** Arreglo de cadenas de fecha (YYYY-MM-DD) con vuelos de regreso disponibles para la ruta inversa. @type {Array<string>} */
  let fechasDisponiblesVuelta = [];

  /** True mientras se obtienen las fechas de vuelos disponibles de la API. @type {boolean} */
  let loadingFechas      = false;

  /** True despues de que se han seleccionado origen y destino y se han obtenido las fechas. @type {boolean} */
  let mostrarCalendarios = false;

  /** Objeto Date que representa el mes actualmente mostrado en el calendario de ida. @type {Date} */
  let mesIda    = new Date();

  /** Objeto Date que representa el mes actualmente mostrado en el calendario de regreso. @type {Date} */
  let mesVuelta = new Date();

  /** Mensaje de error de validacion o busqueda mostrado debajo del formulario de busqueda. @type {string} */
  let searchError = '';

  /** True mientras la solicitud POST de busqueda de vuelos esta en progreso, deshabilita el boton de envio. @type {boolean} */
  let buscando    = false;

  /** Etiquetas abreviadas de dias de la semana usadas como encabezados de columna en la grilla del calendario. @type {Array<string>} */
  const diasSemana  = ['LU','MA','MI','JU','VI','SA','DO'];

  /** Lista completa de nombres de meses usada para construir los titulos del calendario. @type {Array<string>} */
  const mesesNombre = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                       'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  // Aeropuertos con imagen en base64 almacenada, usados para poblar la grilla de destinos destacados.
  $: destinosConImagen = aeropuertos.filter(a => a.imagenBase64);

  onMount(async () => {
    try {
      const res = await fetch(`${API}/api/aeropuertos`);
      aeropuertos = await res.json();
    } catch (err) {
      console.error('Error cargando aeropuertos:', err);
    } finally {
      loadingAeropuertos = false;
    }
  });

  // Cuando llega un aeropuerto sugerido (por ejemplo, de la animacion del avion en vuelo), se selecciona
  // automaticamente como destino y se resalta brevemente el campo de destino con una clase CSS.
  $: if (suggestedAeropuerto) {
    /** @type {any} */
    const ap = suggestedAeropuerto;
    if (ap.id) {
      toSeleccionado = ap;
      toQuery = `${ap.ciudad} (${ap.codigo})`;
      toSugeridos = [];

      setTimeout(() => {
        const f = document.getElementById('toCity');
        if (f) {
          f.classList.add('broom-home__form-input--highlighted');
          setTimeout(() => f.classList.remove('broom-home__form-input--highlighted'), 2000);
        }
      }, 50);
    }
  }

  // Recarga automaticamente las fechas disponibles cada vez que cambian los pasajeros, el origen o el destino.
  $: if (passengers && fromSeleccionado && toSeleccionado) cargarFechasDisponibles();

  // Fecha de hoy en formato YYYY-MM-DD para el atributo min de los inputs de fecha.
  $: fechaHoy = new Date().toISOString().split('T')[0];

  // Si el usuario cambia a "solo ida", limpiar la fecha de regreso.
  $: if (tripType === 'oneway') returnDate = '';

  // Evitar que la fecha de regreso sea anterior a la de ida.
  $: if (returnDate && departureDate && returnDate < departureDate) returnDate = departureDate;

  // Cuando el usuario escribe una fecha de ida en el input nativo, navegar el calendario a ese mes.
  $: if (departureDate) {
    const _d = new Date(departureDate + 'T00:00:00');
    if (!isNaN(_d.getTime())) mesIda = new Date(_d.getFullYear(), _d.getMonth(), 1);
  }

  // Cuando el usuario escribe una fecha de regreso en el input nativo, navegar el calendario a ese mes.
  $: if (returnDate) {
    const _r = new Date(returnDate + 'T00:00:00');
    if (!isNaN(_r.getTime())) mesVuelta = new Date(_r.getFullYear(), _r.getMonth(), 1);
  }

  /**
   * Filtra la lista de aeropuertos segun el texto actual de fromQuery y actualiza fromSugeridos.
   * Limpia el origen seleccionado y reinicia los calendarios si el usuario edita el campo despues de una seleccion.
   */
  function onFromInput() {
    const q = fromQuery.toLowerCase();
    fromSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      a.ciudad.toLowerCase().includes(q) ||
      a.nombre.toLowerCase().includes(q) ||
      a.codigo.toLowerCase().includes(q)
    ).slice(0, 5);
    if (fromSeleccionado && fromQuery !== `${fromSeleccionado.ciudad} (${fromSeleccionado.codigo})`) {
      fromSeleccionado = null; resetCalendarios();
    }
  }

  /**
   * Confirma un aeropuerto como origen seleccionado, rellena el texto del input, limpia el
   * dropdown de sugerencias y activa una recarga de disponibilidad de fechas.
   * @param {object} a - El objeto de aeropuerto elegido de la lista de autocompletado.
   */
  function seleccionarOrigen(a) {
    fromSeleccionado = a;
    fromQuery = `${a.ciudad} (${a.codigo})`;
    fromSugeridos = [];
    cargarFechasDisponibles();
  }

  /**
   * Filtra la lista de aeropuertos segun el texto actual de toQuery y actualiza toSugeridos,
   * excluyendo el aeropuerto de origen actualmente seleccionado de las sugerencias.
   * Limpia el destino seleccionado y reinicia los calendarios si el usuario edita el campo.
   */
  function onToInput() {
    const q = toQuery.toLowerCase();
    toSugeridos = q.length < 1 ? [] : aeropuertos.filter(a =>
      a.ciudad.toLowerCase().includes(q) ||
      a.nombre.toLowerCase().includes(q) ||
      a.codigo.toLowerCase().includes(q)
    ).slice(0, 5);
    if (toSeleccionado && toQuery !== `${toSeleccionado.ciudad} (${toSeleccionado.codigo})`) {
      toSeleccionado = null; resetCalendarios();
    }
  }

  /**
   * Confirma un aeropuerto como destino seleccionado, rellena el texto del input, limpia el
   * dropdown de sugerencias y activa una recarga de disponibilidad de fechas.
   * @param {object} a - El objeto de aeropuerto elegido de la lista de autocompletado.
   */
  function seleccionarDestino(a) {
    toSeleccionado = a;
    toQuery = `${a.ciudad} (${a.codigo})`;
    toSugeridos = [];
    cargarFechasDisponibles();
  }

  /**
   * Reinicia todo el estado relacionado con el calendario: limpia los arreglos de fechas disponibles,
   * limpia las fechas seleccionadas y oculta los paneles del calendario.
   */
  function resetCalendarios() {
    fechasDisponiblesIda = []; fechasDisponiblesVuelta = [];
    departureDate = ''; returnDate = '';
    mostrarCalendarios = false;
  }

  /**
   * Obtiene las fechas de vuelos disponibles para ambas direcciones de ida (origen-destino) y regreso
   * (destino-origen) en paralelo usando el endpoint aeropuertos/fechas-disponibles.
   * Parsea las fechas de la respuesta y establece el mes inicial del calendario a la primera fecha disponible.
   * Muestra los paneles del calendario una vez cargados los datos.
   * @async
   * @returns {Promise<void>}
   */
  async function cargarFechasDisponibles() {
    if (!fromSeleccionado || !toSeleccionado) return;
    loadingFechas = true;
    resetCalendarios();
    try {
      const [resIda, resVuelta] = await Promise.all([
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${fromSeleccionado.id}&destinoId=${toSeleccionado.id}&cantidadPersonas=${passengers}`),
        fetch(`${API}/api/aeropuertos/fechas-disponibles?origenId=${toSeleccionado.id}&destinoId=${fromSeleccionado.id}&cantidadPersonas=${passengers}`)
      ]);
      const dataIda    = await resIda.json();
      const dataVuelta = await resVuelta.json();
      fechasDisponiblesIda    = dataIda.map(f => f.split('T')[0]);
      fechasDisponiblesVuelta = dataVuelta.map(f => f.split('T')[0]);

      const priIda = fechasDisponiblesIda[0];
      mesIda = priIda
        ? (() => { const d = new Date(priIda + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(new Date().getFullYear(), new Date().getMonth(), 1);

      const priVuelta = fechasDisponiblesVuelta[0];
      mesVuelta = priVuelta
        ? (() => { const d = new Date(priVuelta + 'T00:00:00'); return new Date(d.getFullYear(), d.getMonth(), 1); })()
        : new Date(mesIda.getFullYear(), mesIda.getMonth() + 1, 1);

      mostrarCalendarios = true;
    } catch (err) {
      console.error('Error cargando fechas:', err);
    } finally {
      loadingFechas = false;
    }
  }

  /**
   * Retorna true si la cadena de fecha YYYY-MM-DD dada esta en la lista de fechas de ida disponibles.
   * @param {string} f - Cadena de fecha a verificar.
   * @returns {boolean} True si la fecha tiene vuelos de ida disponibles.
   */
  function esFechaDisponibleIda(f)    { return fechasDisponiblesIda.includes(f); }

  /**
   * Retorna true si la cadena de fecha YYYY-MM-DD dada esta en la lista de fechas de regreso disponibles.
   * @param {string} f - Cadena de fecha a verificar.
   * @returns {boolean} True si la fecha tiene vuelos de regreso disponibles.
   */
  function esFechaDisponibleVuelta(f) { return fechasDisponiblesVuelta.includes(f); }

  /**
   * Construye el arreglo de celdas de dias para un mes dado, agregando al inicio marcadores null
   * para el desplazamiento del dia de la semana de modo que el primer dia del mes caiga en la columna correcta.
   * @param {Date} fecha - Un objeto Date apuntando a cualquier dia dentro del mes objetivo.
   * @returns {Array<null|{dia: number, fecha: string}>} Arreglo de null u objetos descriptores de dia.
   */
  function getDias(fecha) {
    const y = fecha.getFullYear(), m = fecha.getMonth();
    let ini = new Date(y, m, 1).getDay() - 1;
    if (ini < 0) ini = 6;
    const dias = [];
    for (let i = 0; i < ini; i++) dias.push(null);
    const total = new Date(y, m + 1, 0).getDate();
    for (let d = 1; d <= total; d++)
      dias.push({ dia: d, fecha: `${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}` });
    return dias;
  }

  // Arreglos de celdas de dias para los calendarios de ida y regreso, reconstruidos cuando cambia el mes mostrado.
  $: diasIda    = getDias(mesIda);
  $: diasVuelta = getDias(mesVuelta);

  // Cadena de titulo mostrada encima del calendario de ida, por ejemplo 'Abril 2026'.
  $: titIda     = `${mesesNombre[mesIda.getMonth()]} ${mesIda.getFullYear()}`;

  // Cadena de titulo mostrada encima del calendario de regreso.
  $: titVuelta  = `${mesesNombre[mesVuelta.getMonth()]} ${mesVuelta.getFullYear()}`;

  /** Navega el calendario de ida un mes hacia atras. */
  function prevIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    - 1, 1); }

  /** Navega el calendario de ida un mes hacia adelante. */
  function nextIda()    { mesIda    = new Date(mesIda.getFullYear(),    mesIda.getMonth()    + 1, 1); }

  /** Navega el calendario de regreso un mes hacia atras. */
  function prevVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() - 1, 1); }

  /** Navega el calendario de regreso un mes hacia adelante. */
  function nextVuelta() { mesVuelta = new Date(mesVuelta.getFullYear(), mesVuelta.getMonth() + 1, 1); }

  /**
   * Selecciona una fecha de vuelo de ida si esta disponible; limpia cualquier error de busqueda existente.
   * No hace nada si la fecha clickeada no esta en la lista de disponibles.
   * @param {string} f - Cadena de fecha en formato YYYY-MM-DD.
   */
  function pickIda(f) {
    if (!esFechaDisponibleIda(f)) return;
    departureDate = f; searchError = '';
  }

  /**
   * Selecciona una fecha de vuelo de regreso si esta disponible y no es anterior a la fecha de salida.
   * No hace nada si la fecha esta bloqueada o no disponible.
   * @param {string} f - Cadena de fecha en formato YYYY-MM-DD.
   */
  function pickVuelta(f) {
    if (departureDate && f < departureDate) return;
    if (!esFechaDisponibleVuelta(f)) return;
    returnDate = f; searchError = '';
  }

  /**
   * Valida los campos del formulario de busqueda, luego envia la busqueda a /api/vuelos/buscar para ambos
   * tramos de ida y (si es ida y vuelta) regreso en secuencia. Al tener exito navega a la pagina 'vuelos'
   * pasando los resultados y todos los parametros de busqueda. Establece searchError en fallos de validacion
   * o errores de la API.
   * @async
   * @returns {Promise<void>}
   */
  async function handleSearchFlight() {
    searchError = '';
    if (!fromSeleccionado) { searchError = 'Selecciona el aeropuerto de origen.';  return; }
    if (!toSeleccionado)   { searchError = 'Selecciona el aeropuerto de destino.'; return; }
    if (fromSeleccionado.id === toSeleccionado.id) { searchError = 'El origen y destino no pueden ser el mismo aeropuerto.'; return; }
    if (!departureDate) {
      departureDate = fechaHoy;
    }
    if (departureDate < fechaHoy) {
      searchError = 'No se pueden buscar vuelos en fechas pasadas.';
      departureDate = fechaHoy;
      return;
    }
    if (tripType === 'roundtrip') {
      if (!returnDate) { searchError = 'Selecciona la fecha de regreso.'; return; }
      if (returnDate < fechaHoy)      { searchError = 'La fecha de regreso no puede ser en el pasado.'; return; }
      if (returnDate < departureDate) { searchError = 'La fecha de regreso debe ser posterior o igual a la fecha de ida.'; return; }
    }

    buscando = true;
    try {
      const resIda = await fetch(`${API}/api/vuelos/buscar`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origenId: fromSeleccionado.id, destinoId: toSeleccionado.id,
          fecha: departureDate, cantidadPasajeros: passengers
        })
      });
      if (!resIda.ok) throw new Error();
      const vuelosIda = await resIda.json();

      let vuelosVuelta = { directos: [], conEscala: [] };
      if (tripType === 'roundtrip' && returnDate) {
        const resVuelta = await fetch(`${API}/api/vuelos/buscar`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            origenId: toSeleccionado.id, destinoId: fromSeleccionado.id,
            fecha: returnDate, cantidadPasajeros: passengers
          })
        });
        if (resVuelta.ok) vuelosVuelta = await resVuelta.json();
      }

      navigateTo('vuelos', {
        vuelosIda, vuelosVuelta,
        searchData: {
          origenId:      fromSeleccionado.id,
          destinoId:     toSeleccionado.id,
          origenNombre:  fromSeleccionado.ciudad,
          destinoNombre: toSeleccionado.ciudad,
          origenCodigo:  fromSeleccionado.codigo,
          destinoCodigo: toSeleccionado.codigo,
          fechaIda:      departureDate,
          fechaVuelta:   returnDate || '',
          pasajeros:     passengers,
          tripType
        }
      });
    } catch (err) {
      console.error('Error en busqueda:', err);
      searchError = 'Error al buscar vuelos. Intenta nuevamente.';
    } finally {
      buscando = false;
    }
  }
</script>

<div class="broom-home">

  <!-- Banner hero con imagen de fondo y mensaje principal de la aerolinea -->
  <section class="broom-home__hero">
    <img src={logoHero} alt="Broom AirLine Hero">
    <div class="broom-home__hero-overlay">
      <h1 class="broom-home__hero-title">Vuela a donde tus suenos te lleven</h1>
      <p class="broom-home__hero-subtitle">Descubre el mundo con Broom AirLine</p>
    </div>
  </section>

  <!-- Formulario de busqueda de vuelos con selector de tipo de viaje, origen, destino y pasajeros -->
  <section class="broom-home__search-section">
    <div class="broom-home__search-container">
      <h2 class="broom-home__search-title">Encuentra tu vuelo</h2>

      <form class="broom-home__search-form" on:submit|preventDefault={handleSearchFlight}>

        <div class="broom-home__trip-type">
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="roundtrip" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Ida y vuelta</span>
          </label>
          <label class="broom-home__radio-label">
            <input type="radio" name="tripType" value="oneway" bind:group={tripType} class="broom-home__radio-input">
            <span class="broom-home__radio-text">Solo ida</span>
          </label>
        </div>

        <div class="broom-home__form-grid">

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="fromCity" class="broom-home__form-label">Desde</label>
            <input type="text" id="fromCity" bind:value={fromQuery} on:input={onFromInput}
              placeholder={loadingAeropuertos ? 'Cargando...' : 'Ciudad de origen'}
              class="broom-home__form-input" autocomplete="off" />
            {#if fromSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each fromSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarOrigen(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group broom-home__form-group--relative">
            <label for="toCity" class="broom-home__form-label">Hacia</label>
            <input type="text" id="toCity" bind:value={toQuery} on:input={onToInput}
              placeholder="Ciudad de destino"
              class="broom-home__form-input" autocomplete="off" />
            {#if toSugeridos.length > 0}
              <ul class="home-autocomplete__list">
                {#each toSugeridos as a}
                  <li class="home-autocomplete__item">
                    <button type="button" class="home-autocomplete__btn" on:click={() => seleccionarDestino(a)}>
                      <span class="home-autocomplete__code">{a.codigo}</span>
                      <div class="home-autocomplete__info">
                        <span class="home-autocomplete__ciudad">{a.ciudad}</span>
                        <span class="home-autocomplete__nombre">{a.nombre} · {a.pais}</span>
                      </div>
                    </button>
                  </li>
                {/each}
              </ul>
            {/if}
          </div>

          <div class="broom-home__form-group">
            <label for="passengers" class="broom-home__form-label">Pasajeros</label>
            <select id="passengers" bind:value={passengers} class="broom-home__form-input broom-home__form-select">
              {#each Array(9) as _, i}
                <option value={i + 1}>{i + 1} {i === 0 ? 'Pasajero' : 'Pasajeros'}</option>
              {/each}
            </select>
          </div>

          <div class="broom-home__form-group broom-home__form-group--btn">
            <div class="broom-home__form-label broom-home__form-label--hidden" aria-hidden="true">·</div>
            <button type="submit" class="broom-home__search-btn" disabled={buscando}>
              <svg class="broom-home__search-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              {buscando ? 'Buscando...' : 'Buscar vuelo'}
            </button>
          </div>

        </div>

        <!-- Inputs de fecha nativa: ida obligatoria, regreso solo en ida y vuelta -->
        <div class="home-date-inputs">
          <div class="home-date-group">
            <label for="dep-date" class="home-date-label">
              {tripType === 'oneway' ? 'Fecha de salida' : 'Fecha de ida'} *
            </label>
            <input type="date" id="dep-date" bind:value={departureDate}
              min={fechaHoy} class="home-date-input"
              on:change={() => searchError = ''} />
          </div>
          {#if tripType === 'roundtrip'}
            <div class="home-date-group">
              <label for="ret-date" class="home-date-label">Fecha de regreso *</label>
              <input type="date" id="ret-date" bind:value={returnDate}
                min={departureDate || fechaHoy} class="home-date-input"
                on:change={() => searchError = ''} />
            </div>
          {/if}
        </div>

        <!-- Pista informativa cuando no se han elegido origen y destino aún -->
        {#if !fromSeleccionado || !toSeleccionado}
          <div class="home-info-hint">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
            <span><strong>Consejo:</strong> Selecciona origen y destino para ver los días con vuelos disponibles en el calendario</span>
          </div>
        {/if}

        <!-- Calendarios duales con fechas disponibles resaltadas para ida y regreso -->
        {#if loadingFechas}
          <div class="cal-loading">Cargando disponibilidad de vuelos...</div>
        {:else if mostrarCalendarios}
          <div class="cal-wrapper">
            <div class="cal-header-info">
              {#if fechasDisponiblesIda.length === 0 && fechasDisponiblesVuelta.length === 0}
                <span class="cal-info-text cal-info-text--empty">
                  No hay vuelos disponibles en esta ruta para {passengers} {passengers === 1 ? 'pasajero' : 'pasajeros'}
                </span>
              {:else}
                <span class="cal-info-text">✈ Dias con vuelo estan marcados — selecciona tu fecha</span>
                {#if tripType === 'roundtrip' && fechasDisponiblesVuelta.length === 0}
                  <span class="cal-info-text cal-info-text--empty">↩ No hay vuelos de regreso disponibles en esta ruta</span>
                {/if}
              {/if}
            </div>

            <div class="cal-dual" class:cal-dual--single={tripType === 'oneway'}>

              <div class="cal-container">
                <div class="cal-label">✈ Fecha de ida</div>
                <div class="cal-nav">
                  <button type="button" class="cal-nav__btn" on:click={prevIda}>‹</button>
                  <span class="cal-nav__title">{titIda}</span>
                  <button type="button" class="cal-nav__btn" on:click={nextIda}>›</button>
                </div>
                <div class="cal-grid cal-grid--header">
                  {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                </div>
                <div class="cal-grid">
                  {#each diasIda as item}
                    {#if item === null}
                      <span class="cal-day cal-day--empty"></span>
                    {:else}
                      {@const disp = esFechaDisponibleIda(item.fecha)}
                      {@const sel  = departureDate === item.fecha}
                      <button type="button" class="cal-day"
                        class:cal-day--disponible={disp && !sel}
                        class:cal-day--seleccionado-ida={sel}
                        class:cal-day--bloqueado={!disp}
                        on:click={() => pickIda(item.fecha)}
                        disabled={!disp}
                        title={disp ? 'Vuelo disponible' : 'Sin vuelos'}>
                        {item.dia}
                      </button>
                    {/if}
                  {/each}
                </div>
                {#if departureDate}
                  <div class="cal-selected-info">Ida: <strong>{departureDate}</strong></div>
                {/if}
              </div>

              {#if tripType === 'roundtrip'}
                <div class="cal-container">
                  <div class="cal-label">↩ Fecha de regreso</div>
                  <div class="cal-nav">
                    <button type="button" class="cal-nav__btn" on:click={prevVuelta}>‹</button>
                    <span class="cal-nav__title">{titVuelta}</span>
                    <button type="button" class="cal-nav__btn" on:click={nextVuelta}>›</button>
                  </div>
                  <div class="cal-grid cal-grid--header">
                    {#each diasSemana as d}<span class="cal-day-name">{d}</span>{/each}
                  </div>
                  <div class="cal-grid">
                    {#each diasVuelta as item}
                      {#if item === null}
                        <span class="cal-day cal-day--empty"></span>
                      {:else}
                        {@const bloqIda  = departureDate && item.fecha < departureDate}
                        {@const bloqDisp = !esFechaDisponibleVuelta(item.fecha)}
                        {@const bloq = bloqIda || bloqDisp}
                        {@const sel  = returnDate === item.fecha}
                        <button type="button" class="cal-day"
                          class:cal-day--disponible-vuelta={!bloq && !sel}
                          class:cal-day--seleccionado-vuelta={sel}
                          class:cal-day--bloqueado={bloq}
                          on:click={() => pickVuelta(item.fecha)}
                          disabled={bloq}
                          title={bloqIda ? 'Fecha anterior a la ida' : bloqDisp ? 'Sin vuelos de regreso' : 'Vuelo disponible'}>
                          {item.dia}
                        </button>
                      {/if}
                    {/each}
                  </div>
                  {#if returnDate}
                    <div class="cal-selected-info cal-selected-info--vuelta">Regreso: <strong>{returnDate}</strong></div>
                  {/if}
                </div>
              {/if}

            </div>
          </div>
        {/if}

        {#if searchError}
          <p class="broom-home__search-error">{searchError}</p>
        {/if}

      </form>
    </div>
  </section>

  <!-- Grilla de destinos destacados con imagen, ciudad, pais y nombre de aeropuerto -->
  {#if destinosConImagen.length > 0}
  <section class="broom-home__destinations">
    <div class="broom-home__destinations-container">
      <h2 class="broom-home__destinations-title">Destinos destacados</h2>
      <div class="broom-home__destinations-grid">
        {#each destinosConImagen as aeropuerto}
          <article class="broom-home__destination-card"
            on:click={() => { toQuery = `${aeropuerto.ciudad} (${aeropuerto.codigo})`; toSeleccionado = aeropuerto; }}
            role="button" tabindex="0"
            on:keydown={e => e.key === 'Enter' && navigateTo('vuelos')}>
            <div class="broom-home__destination-image">
              <img
                src={aeropuerto.imagenBase64.startsWith("data:") ? aeropuerto.imagenBase64 : `data:image/jpeg;base64,${aeropuerto.imagenBase64}`}
                alt="{aeropuerto.ciudad}, {aeropuerto.pais}"
                class="broom-home__destination-image-visual"
              />
              <div class="broom-home__destination-badge">{aeropuerto.codigo}</div>
            </div>
            <div class="broom-home__destination-content">
              <h3 class="broom-home__destination-name">{aeropuerto.ciudad}</h3>
              <p class="broom-home__destination-meta">{aeropuerto.pais}</p>
              <p class="broom-home__destination-description">{aeropuerto.nombre}</p>
            </div>
          </article>
        {/each}
      </div>
      <div class="broom-home__destinations-actions">
        <button type="button" class="broom-home__destinations-btn" on:click={() => navigateTo('destinos-destacados')}>
          Ver mas destinos
        </button>
      </div>
    </div>
  </section>
  {/if}

</div>

<style>
/* ── Inputs de fecha nativa ───────────────────────────────────────── */
.home-date-inputs {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin: 16px 0 8px;
}

.home-date-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 0 0 auto;
}

.home-date-label {
  font-weight: 600;
  color: #5a3e2a;
  font-size: 0.875rem;
}

.home-date-input {
  padding: 10px 12px;
  border: 1.5px solid #c9b99a;
  border-radius: 8px;
  font-size: 0.9rem;
  background: white;
  color: #333;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 220px;
  max-width: 100%;
}

@media (max-width: 480px) {
  .home-date-inputs { flex-direction: column; }
  .home-date-input  { width: 100%; }
}

.home-date-input:focus {
  outline: none;
  border-color: #7a5c3f;
  box-shadow: 0 0 0 3px rgba(122, 92, 63, 0.12);
}

/* ── Pista informativa ────────────────────────────────────────────── */
.home-info-hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #e8f4fd;
  border: 1px solid #93c5fd;
  color: #1e40af;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.83rem;
  margin: 8px 0 12px;
}
</style>
