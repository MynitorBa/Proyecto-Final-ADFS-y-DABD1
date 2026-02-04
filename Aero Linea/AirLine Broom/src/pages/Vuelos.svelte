<script>
// @ts-nocheck

  import '../styles/vuelos.css';
  import DetalleVueloModal from './DetalleVuelo.svelte';

  export let navigateTo;

  // Datos de búsqueda
  const searchData = {
    origin: 'Ciudad de Guatemala',
    originCode: 'GUA',
    destination: 'Paris',
    destinationCode: 'CDG',
    departureDate: '15 Feb 2026',
    returnDate: '22 Feb 2026',
    passengers: 2,
    tripType: 'roundtrip'
  };

  // Estado actual: 'outbound' (ida) o 'return' (vuelta)
  let currentView = 'outbound';

  // Vuelos y clases seleccionadas
  let selectedOutbound = { flightId: null, class: null };
  let selectedReturn = { flightId: null, class: null };

  // Modal de detalles
  let showDetailModal = false;
  let detailFlight = null;

  // Vuelos de IDA
  const outboundFlights = [
    {
      id: 'out-1',
      code: 'AF 1234',
      image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
      departure: { time: '08:00', code: 'GUA' },
      arrival: { time: '18:30', code: 'CDG' },
      type: 'Directo',
      duration: '10h 30m',
      prices: { turista: 650, ejecutiva: 1450 },
      rating: 4.5,
      aircraft: 'Boeing 787-9',
      seatsAvailable: { turista: 45, ejecutiva: 12 },
      stops: []
    },
    {
      id: 'out-2',
      code: 'AA 5821',
      image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
      departure: { time: '10:30', code: 'GUA' },
      arrival: { time: '22:15', code: 'CDG' },
      type: '1 Escala',
      duration: '14h 45m',
      prices: { turista: 580, ejecutiva: 1320 },
      rating: 4.2,
      aircraft: 'Boeing 777-200',
      seatsAvailable: { turista: 38, ejecutiva: 10 },
      stops: [
        {
          city: 'Miami',
          code: 'MIA',
          arrival: '14:30',
          departure: '16:45',
          duration: '2h 15m',
          terminal: 'Terminal Norte'
        }
      ]
    },
    {
      id: 'out-3',
      code: 'IB 9012',
      image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
      departure: { time: '22:00', code: 'GUA' },
      arrival: { time: '18:20', code: 'CDG' },
      type: '2 Escalas',
      duration: '16h 20m',
      prices: { turista: 520, ejecutiva: 1180 },
      rating: 4.0,
      aircraft: 'Airbus A330',
      seatsAvailable: { turista: 52, ejecutiva: 15 },
      stops: [
        {
          city: 'Miami',
          code: 'MIA',
          arrival: '05:45',
          departure: '08:20',
          duration: '2h 35m',
          terminal: 'Terminal Norte'
        },
        {
          city: 'Madrid',
          code: 'MAD',
          arrival: '14:10',
          departure: '16:15',
          duration: '2h 05m',
          terminal: 'Terminal 4'
        }
      ]
    },
    {
      id: 'out-4',
      code: 'LH 7834',
      image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
      departure: { time: '06:15', code: 'GUA' },
      arrival: { time: '17:45', code: 'CDG' },
      type: 'Directo',
      duration: '11h 30m',
      prices: { turista: 690, ejecutiva: 1520 },
      rating: 4.7,
      aircraft: 'Airbus A350',
      seatsAvailable: { turista: 42, ejecutiva: 11 },
      stops: []
    },
    {
      id: 'out-5',
      code: 'BA 3309',
      image: 'https://media-cdn.tripadvisor.com/media/photo-m/1280/17/15/6d/d6/paris.jpg',
      departure: { time: '13:20', code: 'GUA' },
      arrival: { time: '23:50', code: 'CDG' },
      type: '1 Escala',
      duration: '12h 30m',
      prices: { turista: 610, ejecutiva: 1390 },
      rating: 4.4,
      aircraft: 'Boeing 787-10',
      seatsAvailable: { turista: 40, ejecutiva: 9 },
      stops: [
        {
          city: 'Houston',
          code: 'IAH',
          arrival: '16:00',
          departure: '18:30',
          duration: '2h 30m',
          terminal: 'Terminal D'
        }
      ]
    }
  ];

  // Vuelos de VUELTA
  const returnFlights = [
    {
      id: 'ret-1',
      code: 'AF 1235',
      image: 'https://mediaim.expedia.com/destination/1/9c29d66bae2b17dfd77c756458b5c9f2.jpg?impolicy=fcrop&w=1040&h=580&q=mediumHigh',
      departure: { time: '10:00', code: 'CDG' },
      arrival: { time: '16:30', code: 'GUA' },
      type: 'Directo',
      duration: '11h 30m',
      prices: { turista: 670, ejecutiva: 1480 },
      rating: 4.6,
      aircraft: 'Boeing 787-9',
      seatsAvailable: { turista: 42, ejecutiva: 11 },
      stops: []
    },
    {
      id: 'ret-2',
      code: 'LH 5679',
      image: 'https://mediaim.expedia.com/destination/1/9c29d66bae2b17dfd77c756458b5c9f2.jpg?impolicy=fcrop&w=1040&h=580&q=mediumHigh',
      departure: { time: '14:30', code: 'CDG' },
      arrival: { time: '23:45', code: 'GUA' },
      type: '1 Escala',
      duration: '13h 15m',
      prices: { turista: 610, ejecutiva: 1350 },
      rating: 4.3,
      aircraft: 'Airbus A350',
      seatsAvailable: { turista: 35, ejecutiva: 8 },
      stops: [
        {
          city: 'Frankfurt',
          code: 'FRA',
          arrival: '17:20',
          departure: '19:45',
          duration: '2h 25m',
          terminal: 'Terminal 1'
        }
      ]
    },
    {
      id: 'ret-3',
      code: 'BA 4421',
      image: 'https://mediaim.expedia.com/destination/1/9c29d66bae2b17dfd77c756458b5c9f2.jpg?impolicy=fcrop&w=1040&h=580&q=mediumHigh',
      departure: { time: '08:45', code: 'CDG' },
      arrival: { time: '19:20', code: 'GUA' },
      type: 'Directo',
      duration: '11h 35m',
      prices: { turista: 700, ejecutiva: 1550 },
      rating: 4.8,
      aircraft: 'Boeing 777-300',
      seatsAvailable: { turista: 48, ejecutiva: 12 },
      stops: []
    },
    {
      id: 'ret-4',
      code: 'IB 7722',
      image: 'https://mediaim.expedia.com/destination/1/9c29d66bae2b17dfd77c756458b5c9f2.jpg?impolicy=fcrop&w=1040&h=580&q=mediumHigh',
      departure: { time: '20:15', code: 'CDG' },
      arrival: { time: '08:30', code: 'GUA' },
      type: '1 Escala',
      duration: '14h 15m',
      prices: { turista: 590, ejecutiva: 1310 },
      rating: 4.1,
      aircraft: 'Airbus A330',
      seatsAvailable: { turista: 50, ejecutiva: 14 },
      stops: [
        {
          city: 'Madrid',
          code: 'MAD',
          arrival: '23:30',
          departure: '02:15',
          duration: '2h 45m',
          terminal: 'Terminal 4'
        }
      ]
    }
  ];

  // Funciones
  function selectFlight(flightId, flightClass) {
    if (currentView === 'outbound') {
      selectedOutbound = { flightId, class: flightClass };
    } else {
      selectedReturn = { flightId, class: flightClass };
    }
  }

  function viewDetails(flight) {
    detailFlight = flight;
    showDetailModal = true;
  }

  function closeModal() {
    showDetailModal = false;
    detailFlight = null;
  }

  function nextStep() {
    if (currentView === 'outbound' && selectedOutbound.flightId && selectedOutbound.class) {
      currentView = 'return';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else if (currentView === 'return' && selectedReturn.flightId && selectedReturn.class) {
      navigateTo('datos-pasajeros');
    }
  }

  function goBack() {
    if (currentView === 'return') {
      currentView = 'outbound';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      navigateTo('home');
    }
  }

  // Computed
  $: currentFlights = currentView === 'outbound' ? outboundFlights : returnFlights;
  $: selectedFlight = currentView === 'outbound' ? selectedOutbound : selectedReturn;
  $: canProceed = selectedFlight.flightId !== null && selectedFlight.class !== null;
</script>

{#if showDetailModal && detailFlight}
  <DetalleVueloModal flight={detailFlight} onClose={closeModal} />
{/if}

<div class="vuelos-page">
  <div class="vuelos-page__container">
    
    <!-- Header con resumen de búsqueda -->
    <div class="vuelos-page__header">
      <button class="vuelos-page__back" on:click={goBack}>
        Volver
      </button>
      
      <div class="search-summary">
        <div class="search-summary__route">
          <span class="search-summary__origin">{searchData.origin}</span>
          <span class="search-summary__arrow">→</span>
          <span class="search-summary__destination">{searchData.destination}</span>
        </div>
        
        <div class="search-summary__details">
          <div class="search-summary__item">
            <span class="search-summary__label">Salida</span>
            <span class="search-summary__value">{searchData.departureDate}</span>
          </div>
          <div class="search-summary__item">
            <span class="search-summary__label">Regreso</span>
            <span class="search-summary__value">{searchData.returnDate}</span>
          </div>
          <div class="search-summary__item">
            <span class="search-summary__label">Pasajeros</span>
            <span class="search-summary__value">{searchData.passengers}</span>
          </div>
        </div>
      </div>

      <!-- Indicador de paso actual -->
      <div class="step-indicator">
        <div class="step-indicator__item" class:step-indicator__item--active={currentView === 'outbound'} class:step-indicator__item--completed={selectedOutbound.flightId}>
          <span class="step-indicator__number">1</span>
          <span class="step-indicator__label">Vuelo de Ida</span>
        </div>
        <div class="step-indicator__line"></div>
        <div class="step-indicator__item" class:step-indicator__item--active={currentView === 'return'} class:step-indicator__item--completed={selectedReturn.flightId}>
          <span class="step-indicator__number">2</span>
          <span class="step-indicator__label">Vuelo de Vuelta</span>
        </div>
      </div>
    </div>

    <!-- Contenido principal -->
    <div class="vuelos-page__content">
      
      <!-- Filtros laterales -->
      <aside class="vuelos-page__filters">
        <div class="filters-panel">
          <div class="filters-panel__header">
            <h3 class="filters-panel__title">Filtros</h3>
            <button class="filters-panel__clear">Limpiar</button>
          </div>

          <div class="filter-group">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="filter-group__label">Rango de Precio</label>
            <div class="filter-group__price-range">
              <input type="number" class="filter-group__input" placeholder="Min" value="500">
              <span>-</span>
              <input type="number" class="filter-group__input" placeholder="Max" value="1600">
            </div>
          </div>

          <div class="filter-group">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="filter-group__label">Tipo de Vuelo</label>
            <select class="filter-group__select">
              <option value="all">Todos</option>
              <option value="directo">Directo</option>
              <option value="escala">Con Escalas</option>
            </select>
          </div>

          <div class="filter-group">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="filter-group__label">Rating Minimo</label>
            <select class="filter-group__select">
              <option value="0">Todos</option>
              <option value="4">4.0+</option>
              <option value="4.5">4.5+</option>
            </select>
          </div>

          <button class="filters-panel__apply">Aplicar Filtros</button>
        </div>
      </aside>

      <!-- Lista de vuelos -->
      <div class="vuelos-page__main">
        <div class="flights-header">
          <h2 class="flights-header__title">
            {currentView === 'outbound' ? 'Vuelos de Ida' : 'Vuelos de Regreso'}
          </h2>
          <p class="flights-header__subtitle">
            {currentFlights.length} vuelos disponibles
          </p>
        </div>

        <div class="flights-list">
          {#each currentFlights as flight}
            <div class="flight-card" class:flight-card--selected={selectedFlight.flightId === flight.id}>
              <div class="flight-card__image">
                <img src={flight.image} alt="">
                {#if selectedFlight.flightId === flight.id}
                  <div class="flight-card__selected-badge">Seleccionado</div>
                {/if}
              </div>

              <div class="flight-card__content">
                <div class="flight-card__header">
                  <div class="flight-card__code-info">
                    <span class="flight-card__code">{flight.code}</span>
                  </div>
                  <div class="flight-card__rating">
                    <span class="flight-card__rating-value">{flight.rating}</span>
                    <span class="flight-card__rating-stars">★</span>
                  </div>
                </div>

                <div class="flight-card__schedule">
                  <div class="schedule-point">
                    <span class="schedule-point__time">{flight.departure.time}</span>
                    <span class="schedule-point__code">{flight.departure.code}</span>
                  </div>
                  
                  <div class="schedule-duration">
                    <div class="schedule-duration__line"></div>
                    <span class="schedule-duration__time">{flight.duration}</span>
                    <span class="schedule-duration__type">{flight.type}</span>
                  </div>
                  
                  <div class="schedule-point">
                    <span class="schedule-point__time">{flight.arrival.time}</span>
                    <span class="schedule-point__code">{flight.arrival.code}</span>
                  </div>
                </div>

                <!-- Selección de Clase -->
                <div class="flight-card__class-selection">
                  <button 
                    class="class-option" 
                    class:class-option--selected={selectedFlight.flightId === flight.id && selectedFlight.class === 'turista'}
                    on:click={() => selectFlight(flight.id, 'turista')}
                  >
                    <span class="class-option__name">Turista</span>
                    <span class="class-option__price">${flight.prices.turista}</span>
                    <span class="class-option__label">por persona</span>
                  </button>
                  
                  <button 
                    class="class-option" 
                    class:class-option--selected={selectedFlight.flightId === flight.id && selectedFlight.class === 'ejecutiva'}
                    on:click={() => selectFlight(flight.id, 'ejecutiva')}
                  >
                    <span class="class-option__name">Ejecutiva</span>
                    <span class="class-option__price">${flight.prices.ejecutiva}</span>
                    <span class="class-option__label">por persona</span>
                  </button>
                </div>

                <button class="flight-card__details-btn" on:click={() => viewDetails(flight)}>
                  Ver Detalles
                </button>
              </div>
            </div>
          {/each}
        </div>
      </div>
    </div>

    {#if canProceed}
      <div class="vuelos-page__next-step">
        <button class="next-step-btn" on:click={nextStep}>
          {currentView === 'outbound' ? 'Seleccionar Vuelo de Vuelta' : 'Siguiente Paso'}
        </button>
      </div>
    {/if}
  </div>
</div>