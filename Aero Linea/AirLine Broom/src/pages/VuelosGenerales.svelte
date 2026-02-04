<script>
// @ts-nocheck
  import '../styles/vuelosGenerales.css';
  export let navigateTo;

  const allFlights = [
    {
      id: 1,
      image: 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800',
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
      image: 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Tokio',
      country: 'Japon',
      departureDate: '2026-02-20',
      departureTime: '14:00',
      arrivalDate: '2026-02-21',
      arrivalTime: '22:30',
      duration: '16h 30m',
      airline: 'Japan Airlines',
      prices: {
        economico: 890,
        normal: 1200,
        ejecutivo: 2100
      }
    },
    {
      id: 3,
      image: 'https://images.unsplash.com/photo-1539037116277-4db20889f2d4?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Madrid',
      country: 'España',
      departureDate: '2026-02-18',
      departureTime: '10:30',
      arrivalDate: '2026-02-18',
      arrivalTime: '20:45',
      duration: '10h 15m',
      airline: 'Iberia',
      prices: {
        economico: 580,
        normal: 850,
        ejecutivo: 1350
      }
    },
    {
      id: 4,
      image: 'https://images.unsplash.com/photo-1467269204594-9661b134dd2b?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Berlin',
      country: 'Alemania',
      departureDate: '2026-03-01',
      departureTime: '09:00',
      arrivalDate: '2026-03-01',
      arrivalTime: '19:30',
      duration: '10h 30m',
      airline: 'Lufthansa',
      prices: {
        economico: 720,
        normal: 980,
        ejecutivo: 1580
      }
    },
    {
      id: 5,
      image: 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Roma',
      country: 'Italia',
      departureDate: '2026-02-25',
      departureTime: '11:00',
      arrivalDate: '2026-02-25',
      arrivalTime: '21:15',
      duration: '10h 15m',
      airline: 'Alitalia',
      prices: {
        economico: 690,
        normal: 950,
        ejecutivo: 1480
      }
    },
    {
      id: 6,
      image: 'https://images.unsplash.com/photo-1589909202802-8f4aadce1849?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Buenos Aires',
      country: 'Argentina',
      departureDate: '2026-03-10',
      departureTime: '16:00',
      arrivalDate: '2026-03-11',
      arrivalTime: '06:30',
      duration: '14h 30m',
      airline: 'Aerolineas Argentinas',
      prices: {
        economico: 520,
        normal: 780,
        ejecutivo: 1250
      }
    },
    {
      id: 7,
      image: 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Londres',
      country: 'Reino Unido',
      departureDate: '2026-02-28',
      departureTime: '12:00',
      arrivalDate: '2026-02-28',
      arrivalTime: '22:30',
      duration: '10h 30m',
      airline: 'British Airways',
      prices: {
        economico: 710,
        normal: 990,
        ejecutivo: 1620
      }
    },
    {
      id: 8,
      image: 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800',
      origin: 'Ciudad de Guatemala',
      destination: 'Dubai',
      country: 'Emiratos Arabes Unidos',
      departureDate: '2026-03-05',
      departureTime: '15:00',
      arrivalDate: '2026-03-06',
      arrivalTime: '18:30',
      duration: '19h 30m',
      airline: 'Emirates',
      prices: {
        economico: 950,
        normal: 1350,
        ejecutivo: 2450
      }
    }
  ];

  let filters = {
    destination: '',
    origin: '',
    departureDate: '',
    minPrice: '',
    maxPrice: '',
    airline: '',
    country: ''
  };

  function handleFilterChange() {
    console.log('Filtros actuales:', filters);
  }

  function clearFilters() {
    filters = {
      destination: '',
      origin: '',
      departureDate: '',
      minPrice: '',
      maxPrice: '',
      airline: '',
      country: ''
    };
  }

  function viewFlightDetails(flightId) {
    navigateTo('detalle-vuelo', flightId);
  }
</script>

<div class="vuelos-generales">
  <div class="vuelos-generales__container">
    <div class="vuelos-generales__header">
      <h1 class="vuelos-generales__title">Buscar Vuelos</h1>
      <p class="vuelos-generales__subtitle">Explora todos nuestros destinos disponibles</p>
    </div>

    <div class="vuelos-generales__content">
      <aside class="vuelos-generales__filters">
        <div class="filters-panel">
          <div class="filters-panel__header">
            <h2 class="filters-panel__title">Filtros de busqueda</h2>
            <button class="filters-panel__clear" on:click={clearFilters}>
              Limpiar filtros
            </button>
          </div>

          <div class="filters-panel__body">
            <div class="filter-group">
              <label class="filter-group__label" for="filter-destination">Destino</label>
              <input 
                type="text" 
                id="filter-destination"
                class="filter-group__input"
                placeholder="Ej: Paris, Tokio..."
                bind:value={filters.destination}
                on:input={handleFilterChange}
              />
            </div>

            <div class="filter-group">
              <label class="filter-group__label" for="filter-origin">Origen</label>
              <input 
                type="text" 
                id="filter-origin"
                class="filter-group__input"
                placeholder="Ej: Ciudad de Guatemala"
                bind:value={filters.origin}
                on:input={handleFilterChange}
              />
            </div>

            <div class="filter-group">
              <label class="filter-group__label" for="filter-country">Pais</label>
              <input 
                type="text" 
                id="filter-country"
                class="filter-group__input"
                placeholder="Ej: Francia, Japon..."
                bind:value={filters.country}
                on:input={handleFilterChange}
              />
            </div>

            <div class="filter-group">
              <label class="filter-group__label" for="filter-airline">Aerolinea</label>
              <input 
                type="text" 
                id="filter-airline"
                class="filter-group__input"
                placeholder="Ej: Air France..."
                bind:value={filters.airline}
                on:input={handleFilterChange}
              />
            </div>

            <div class="filter-group">
              <label class="filter-group__label" for="filter-date">Fecha de salida</label>
              <input 
                type="date" 
                id="filter-date"
                class="filter-group__input"
                bind:value={filters.departureDate}
                on:input={handleFilterChange}
              />
            </div>

            <div class="filter-group">
              <!-- svelte-ignore a11y_label_has_associated_control -->
              <label class="filter-group__label">Rango de precio</label>
              <div class="filter-group__price-range">
                <input 
                  type="number" 
                  class="filter-group__input filter-group__input--small"
                  placeholder="Min"
                  bind:value={filters.minPrice}
                  on:input={handleFilterChange}
                />
                <span class="filter-group__separator">-</span>
                <input 
                  type="number" 
                  class="filter-group__input filter-group__input--small"
                  placeholder="Max"
                  bind:value={filters.maxPrice}
                  on:input={handleFilterChange}
                />
              </div>
            </div>

            <button class="filters-panel__apply">
              Aplicar filtros
            </button>
          </div>
        </div>
      </aside>

      <main class="vuelos-generales__results">
        <div class="results-header">
          <p class="results-header__count">
            Mostrando {allFlights.length} vuelos disponibles
          </p>
        </div>

        <div class="flights-grid">
          {#each allFlights as flight}
            <article class="flight-card-general">
              <div class="flight-card-general__image">
                <img 
                  src={flight.image} 
                  alt={flight.destination}
                  on:error={(e) => {
                    e.target.src = 'https://via.placeholder.com/800x600?text=' + flight.destination;
                  }}
                />
              </div>

              <div class="flight-card-general__content">
                <div class="flight-card-general__destination">
                  <h3 class="destination-name">{flight.destination}</h3>
                  <p class="destination-country">{flight.country}</p>
                </div>

                <div class="flight-card-general__route">
                  <div class="route-info">
                    <span class="route-info__label">Desde</span>
                    <span class="route-info__value">{flight.origin}</span>
                  </div>
                  <div class="route-separator"></div>
                  <div class="route-info">
                    <span class="route-info__label">Hacia</span>
                    <span class="route-info__value">{flight.destination}</span>
                  </div>
                </div>

                <div class="flight-card-general__details">
                  <div class="detail-item">
                    <span class="detail-item__label">Salida</span>
                    <span class="detail-item__value">{flight.departureDate} - {flight.departureTime}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-item__label">Llegada</span>
                    <span class="detail-item__value">{flight.arrivalDate} - {flight.arrivalTime}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-item__label">Duracion</span>
                    <span class="detail-item__value">{flight.duration}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-item__label">Aerolinea</span>
                    <span class="detail-item__value">{flight.airline}</span>
                  </div>
                </div>

                <div class="flight-card-general__prices">
                  <div class="price-tag">
                    <span class="price-tag__label">Economico</span>
                    <span class="price-tag__value">${flight.prices.economico}</span>
                  </div>
                  <div class="price-tag">
                    <span class="price-tag__label">Normal</span>
                    <span class="price-tag__value">${flight.prices.normal}</span>
                  </div>
                  <div class="price-tag">
                    <span class="price-tag__label">Ejecutivo</span>
                    <span class="price-tag__value">${flight.prices.ejecutivo}</span>
                  </div>
                </div>

                <button class="flight-card-general__btn" on:click={() => viewFlightDetails(flight.id)}>
                  Ver detalles
                </button>
              </div>
            </article>
          {/each}
        </div>
      </main>
    </div>
  </div>
</div>