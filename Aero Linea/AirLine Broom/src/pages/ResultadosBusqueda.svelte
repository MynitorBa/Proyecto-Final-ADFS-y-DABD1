<script>
/**
 * @file ResultadosBusqueda.svelte
 * @description Legacy static search results page for Broom AirLine. Contains a hardcoded
 * allFlights array with six sample flights between Guatemala City, Paris, and London with
 * prices for economico, normal, and ejecutivo seat classes. Supports keyword-based filtering
 * against origin, destination, country, airline, and departure/arrival dates. Results can be
 * sorted by lowest price, highest price, duration, or departure time. Renders a flight card
 * grid with route, schedule, duration, and price information. Each card has a details button
 * that navigates to the 'detalle-vuelo' page. A search bar allows re-filtering in real time.
 */
// @ts-nocheck
  import '../styles/busqueda.css';

  /** Function used to navigate between application pages. @type {function} */
  export let navigateTo;

  /** Search parameters passed from the home page, may contain a keyword property. @type {object|null} */
  export let searchParams = null;

  /**
   * Hardcoded sample flight data array used by this legacy page. Each entry has id, image URL,
   * origin, destination, country, departure and arrival date/time strings, duration, airline, and
   * a prices object with economico, normal, and ejecutivo numeric values.
   * @type {Array<{id: number, image: string, origin: string, destination: string, country: string, departureDate: string, departureTime: string, arrivalDate: string, arrivalTime: string, duration: string, airline: string, prices: {economico: number, normal: number, ejecutivo: number}}>}
   */
  const allFlights = [
    {
      id: 1,
      image: 'https://4.bp.blogspot.com/-UYxMNeyok-M/VYrivzbC-fI/AAAAAAAABMc/df5T_Sdmlsk/s1600/Paris.jpg',
      origin: 'Ciudad de Guatemala',
      destination: 'Paris',
      country: 'Francia',
      departureDate: '2026-02-15',
      departureTime: '08:00',
      arrivalDate: '2026-02-15',
      arrivalTime: '18:30',
      duration: '10h 30m',
      airline: 'Air France',
      prices: {
        economico: 650,
        normal: 920,
        ejecutivo: 1450
      }
    },
    {
      id: 2,
      image: 'https://4.bp.blogspot.com/-UYxMNeyok-M/VYrivzbC-fI/AAAAAAAABMc/df5T_Sdmlsk/s1600/Paris.jpg',
      origin: 'Ciudad de Guatemala',
      destination: 'Paris',
      country: 'Francia',
      departureDate: '2026-02-15',
      departureTime: '14:00',
      arrivalDate: '2026-02-16',
      arrivalTime: '00:30',
      duration: '10h 30m',
      airline: 'Air France',
      prices: {
        economico: 620,
        normal: 890,
        ejecutivo: 1420
      }
    },
    {
      id: 3,
      image: 'https://4.bp.blogspot.com/-UYxMNeyok-M/VYrivzbC-fI/AAAAAAAABMc/df5T_Sdmlsk/s1600/Paris.jpg',
      origin: 'Ciudad de Guatemala',
      destination: 'Paris',
      country: 'Francia',
      departureDate: '2026-02-15',
      departureTime: '20:00',
      arrivalDate: '2026-02-16',
      arrivalTime: '06:30',
      duration: '10h 30m',
      airline: 'Air France',
      prices: {
        economico: 680,
        normal: 950,
        ejecutivo: 1480
      }
    },
    {
      id: 4,
      image: 'https://viajerosocultos.com/wp-content/uploads/2021/07/guatemala-4626550_1920.jpg',
      origin: 'Paris',
      destination: 'Ciudad de Guatemala',
      country: 'Guatemala',
      departureDate: '2026-02-22',
      departureTime: '10:00',
      arrivalDate: '2026-02-22',
      arrivalTime: '16:30',
      duration: '11h 30m',
      airline: 'Air France',
      prices: {
        economico: 670,
        normal: 940,
        ejecutivo: 1480
      }
    },
    {
      id: 5,
      image: 'https://cdn.holidayguru.es/wp-content/uploads/2016/10/Big-Ben-the-Parliament-and-doubledecker-in-London-iStock_71522637_XLARGE-2-2.jpg',
      origin: 'Ciudad de Guatemala',
      destination: 'Londres',
      country: 'Reino Unido',
      departureDate: '2026-03-01',
      departureTime: '09:00',
      arrivalDate: '2026-03-01',
      arrivalTime: '20:15',
      duration: '11h 15m',
      airline: 'British Airways',
      prices: {
        economico: 720,
        normal: 980,
        ejecutivo: 1550
      }
    },
    {
      id: 6,
      image: 'https://cdn.holidayguru.es/wp-content/uploads/2016/10/Big-Ben-the-Parliament-and-doubledecker-in-London-iStock_71522637_XLARGE-2-2.jpg',
      origin: 'Paris',
      destination: 'Londres',
      country: 'Reino Unido',
      departureDate: '2026-02-20',
      departureTime: '11:30',
      arrivalDate: '2026-02-20',
      arrivalTime: '12:45',
      duration: '1h 15m',
      airline: 'British Airways',
      prices: {
        economico: 180,
        normal: 250,
        ejecutivo: 420
      }
    }
  ];

  /** Current keyword string used to filter flights; initialized from searchParams.keyword or empty. @type {string} */
  let searchKeyword = searchParams?.keyword || '';

  /**
   * Filters allFlights against a keyword by checking whether any of origin, destination, country,
   * airline, departureDate, or arrivalDate includes the lowercased search term. Returns all flights
   * if keyword is empty or whitespace-only.
   * @param {string} keyword - The search term to filter by.
   * @returns {Array<object>} Array of matching flight objects from allFlights.
   */
  function filterFlights(keyword) {
    if (!keyword || keyword.trim() === '') {
      return allFlights;
    }

    const searchTerm = keyword.toLowerCase().trim();

    return allFlights.filter(flight => {
      return (
        flight.origin.toLowerCase().includes(searchTerm) ||
        flight.destination.toLowerCase().includes(searchTerm) ||
        flight.country.toLowerCase().includes(searchTerm) ||
        flight.airline.toLowerCase().includes(searchTerm) ||
        flight.departureDate.includes(searchTerm) ||
        flight.arrivalDate.includes(searchTerm)
      );
    });
  }

  // Array of flight objects matching the current keyword filter.
  $: searchResults = filterFlights(searchKeyword);

  /** Current sort criterion for the results list. @type {string} */
  let sortBy = 'price-low';

  // Sorted copy of searchResults according to the selected sortBy criterion.
  $: sortedResults = (() => {
    const results = [...searchResults];

    switch(sortBy) {
      case 'price-low':
        return results.sort((a, b) => a.prices.economico - b.prices.economico);
      case 'price-high':
        return results.sort((a, b) => b.prices.economico - a.prices.economico);
      case 'duration':
        return results.sort((a, b) => {
          const aDuration = parseFloat(a.duration);
          const bDuration = parseFloat(b.duration);
          return aDuration - bDuration;
        });
      case 'departure':
        return results.sort((a, b) => a.departureTime.localeCompare(b.departureTime));
      default:
        return results;
    }
  })();

  /**
   * Navigates to the 'detalle-vuelo' page passing the flight ID as the second argument.
   * @param {number} flightId - The ID of the flight to view.
   */
  function viewFlightDetails(flightId) {
    navigateTo('detalle-vuelo', flightId);
  }

  /**
   * Re-runs filterFlights with the current searchKeyword and assigns the result to searchResults.
   */
  function handleSearch() {
    searchResults = filterFlights(searchKeyword);
  }
</script>

<!-- Contenedor principal de la pagina de resultados de busqueda -->
<div class="resultados-busqueda">
  <div class="resultados-busqueda__container">

    <!-- Cabecera con boton de regreso, titulo y barra de busqueda por palabra clave -->
    <div class="resultados-busqueda__header">
      <button class="resultados-busqueda__back" on:click={() => navigateTo('home')}>
        Volver a busqueda
      </button>
      <h1 class="resultados-busqueda__title">Resultados de busqueda</h1>

      <!-- Barra de busqueda en tiempo real por destino, origen, aerolinea o fecha -->
      <div class="search-bar">
        <input
          type="text"
          class="search-bar__input"
          placeholder="Buscar por destino, origen, aerolinea, fecha..."
          bind:value={searchKeyword}
          on:input={handleSearch}
        />
        <button class="search-bar__btn" on:click={handleSearch}>
          Buscar
        </button>
      </div>

      <!-- Indicador del termino de busqueda activo, visible solo cuando hay un filtro aplicado -->
      {#if searchKeyword}
        <div class="resultados-busqueda__search-info">
          <p class="search-info-item">
            <span class="search-info-item__label">Buscando:</span>
            <span class="search-info-item__value">"{searchKeyword}"</span>
          </p>
        </div>
      {/if}
    </div>

    <div class="resultados-busqueda__content">

      <!-- Barra de conteo de resultados y selector de criterio de ordenamiento -->
      <div class="results-header">
        <p class="results-header__count">
          {sortedResults.length} {sortedResults.length === 1 ? 'vuelo encontrado' : 'vuelos encontrados'}
        </p>
        <div class="results-header__sort">
          <label for="sort-select" class="results-header__sort-label">Ordenar por:</label>
          <select id="sort-select" class="results-header__sort-select" bind:value={sortBy}>
            <option value="price-low">Precio: menor a mayor</option>
            <option value="price-high">Precio: mayor a menor</option>
            <option value="duration">Duracion</option>
            <option value="departure">Hora de salida</option>
          </select>
        </div>
      </div>

      <!-- Mensaje de sin resultados o grilla de tarjetas de vuelos encontrados -->
      {#if sortedResults.length === 0}
        <div class="no-results">
          <p class="no-results__message">No se encontraron vuelos para "{searchKeyword}"</p>
          <p class="no-results__suggestion">Intenta con otra palabra clave como: Paris, Londres, Guatemala, Air France, etc.</p>
        </div>
      {:else}
        <!-- Grilla de tarjetas de resultados de vuelos con imagen, ruta, horarios y precios por clase -->
        <div class="flights-results-list">
          {#each sortedResults as flight}
            <article class="flight-result-card">
              <div class="flight-result-card__image">
                <img src={flight.image} alt={flight.destination} />
              </div>

              <div class="flight-result-card__content">
                <div class="flight-result-card__header">
                  <div class="flight-destination">
                    <h3 class="flight-destination__name">{flight.destination}</h3>
                    <p class="flight-destination__country">{flight.country}</p>
                  </div>
                  <div class="flight-airline">
                    <p class="flight-airline__name">{flight.airline}</p>
                  </div>
                </div>

                <!-- Linea de ruta con origen, duracion y destino del vuelo -->
                <div class="flight-result-card__route">
                  <div class="route-point">
                    <span class="route-point__time">{flight.departureTime}</span>
                    <span class="route-point__location">{flight.origin}</span>
                    <span class="route-point__date">{flight.departureDate}</span>
                  </div>

                  <div class="route-duration">
                    <div class="route-duration__line"></div>
                    <span class="route-duration__text">{flight.duration}</span>
                  </div>

                  <div class="route-point">
                    <span class="route-point__time">{flight.arrivalTime}</span>
                    <span class="route-point__location">{flight.destination}</span>
                    <span class="route-point__date">{flight.arrivalDate}</span>
                  </div>
                </div>

                <!-- Precios por clase: economico, normal y ejecutivo -->
                <div class="flight-result-card__prices">
                  <div class="price-option-compact">
                    <span class="price-option-compact__label">Economico</span>
                    <span class="price-option-compact__value">${flight.prices.economico}</span>
                  </div>
                  <div class="price-option-compact">
                    <span class="price-option-compact__label">Normal</span>
                    <span class="price-option-compact__value">${flight.prices.normal}</span>
                  </div>
                  <div class="price-option-compact">
                    <span class="price-option-compact__label">Ejecutivo</span>
                    <span class="price-option-compact__value">${flight.prices.ejecutivo}</span>
                  </div>
                </div>

                <!-- Boton de navegacion al detalle del vuelo seleccionado -->
                <button
                  class="flight-result-card__btn-details"
                  on:click={() => viewFlightDetails(flight.id)}
                >
                  Ver detalles del vuelo
                </button>
              </div>
            </article>
          {/each}
        </div>
      {/if}
    </div>
  </div>
</div>
