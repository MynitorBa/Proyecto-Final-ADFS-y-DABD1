<script>
// @ts-nocheck
  import '../styles/busqueda.css';

  export let navigateTo;
  export let searchParams = null;

  // Todos los vuelos disponibles en el sistema
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

  // Palabra clave de búsqueda (viene de searchParams o vacía por defecto)
  let searchKeyword = searchParams?.keyword || '';

  // Filtrar vuelos basándose en la palabra clave
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

  // Resultados filtrados
  $: searchResults = filterFlights(searchKeyword);

  // Función de ordenamiento
  let sortBy = 'price-low';
  
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

  function viewFlightDetails(flightId) {
    navigateTo('detalle-vuelo', flightId);
  }

  function handleSearch() {
    searchResults = filterFlights(searchKeyword);
  }
</script>

<div class="resultados-busqueda">
  <div class="resultados-busqueda__container">
    <div class="resultados-busqueda__header">
      <button class="resultados-busqueda__back" on:click={() => navigateTo('home')}>
        Volver a busqueda
      </button>
      <h1 class="resultados-busqueda__title">Resultados de busqueda</h1>
      
      <!-- Barra de búsqueda por palabra clave -->
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

      {#if sortedResults.length === 0}
        <div class="no-results">
          <p class="no-results__message">No se encontraron vuelos para "{searchKeyword}"</p>
          <p class="no-results__suggestion">Intenta con otra palabra clave como: Paris, Londres, Guatemala, Air France, etc.</p>
        </div>
      {:else}
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