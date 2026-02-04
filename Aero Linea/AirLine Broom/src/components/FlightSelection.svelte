<script>
  import '../styles/vuelos.css';
  export let onFlightSelect;
  export let flightType = 'ida';
  
  const flights = [
    {
      id: 1,
      origin: 'Ciudad de Guatemala',
      destination: 'Madrid',
      departure: '08:00',
      arrival: '18:30',
      duration: '10h 30m',
      airline: 'Aerolínea Nacional',
      prices: {
        economico: 450,
        normal: 680,
        ejecutivo: 1200
      }
    },
    {
      id: 2,
      origin: 'Ciudad de Guatemala',
      destination: 'Madrid',
      departure: '14:00',
      arrival: '00:30',
      duration: '10h 30m',
      airline: 'Aerolínea Nacional',
      prices: {
        economico: 420,
        normal: 650,
        ejecutivo: 1150
      }
    },
    {
      id: 3,
      origin: 'Ciudad de Guatemala',
      destination: 'Madrid',
      departure: '20:00',
      arrival: '06:30',
      duration: '10h 30m',
      airline: 'Aerolínea Nacional',
      prices: {
        economico: 480,
        normal: 700,
        ejecutivo: 1250
      }
    }
  ];

  let selectedFlight = null;
  let selectedCategory = null;

  function selectFlight(flight, category) {
    selectedFlight = flight;
    selectedCategory = category;
    onFlightSelect({ flight, category, flightType });
  }
</script>

<div class="flight-selection">
  <h2 class="flight-selection__title">
    {flightType === 'ida' ? 'Vuelo de Ida' : 'Vuelo de Regreso'}
  </h2>

  <div class="flight-selection__list">
    {#each flights as flight}
      <div class="flight-card">
        <div class="flight-card__info">
          <div class="flight-card__route">
            <div class="flight-card__time">
              <span class="time">{flight.departure}</span>
              <span class="location">{flight.origin}</span>
            </div>
            
            <div class="flight-card__duration">
              <div class="duration-line"></div>
              <span class="duration-text">{flight.duration}</span>
            </div>
            
            <div class="flight-card__time">
              <span class="time">{flight.arrival}</span>
              <span class="location">{flight.destination}</span>
            </div>
          </div>
          
          <div class="flight-card__airline">{flight.airline}</div>
        </div>

        <div class="flight-card__prices">
          <button 
            class="price-option" 
            class:selected={selectedFlight?.id === flight.id && selectedCategory === 'economico'}
            on:click={() => selectFlight(flight, 'economico')}
          >
            <span class="category">Economico</span>
            <span class="price">${flight.prices.economico}</span>
          </button>

          <button 
            class="price-option" 
            class:selected={selectedFlight?.id === flight.id && selectedCategory === 'normal'}
            on:click={() => selectFlight(flight, 'normal')}
          >
            <span class="category">Normal</span>
            <span class="price">${flight.prices.normal}</span>
          </button>

          <button 
            class="price-option" 
            class:selected={selectedFlight?.id === flight.id && selectedCategory === 'ejecutivo'}
            on:click={() => selectFlight(flight, 'ejecutivo')}
          >
            <span class="category">Ejecutivo</span>
            <span class="price">${flight.prices.ejecutivo}</span>
          </button>
        </div>
      </div>
    {/each}
  </div>
</div>