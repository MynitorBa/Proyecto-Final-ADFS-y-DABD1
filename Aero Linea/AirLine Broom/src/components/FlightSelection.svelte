<script>
  import '../styles/vuelos.css';
  import { onMount } from 'svelte';
  
  export let onFlightSelect;
  export let flightType = 'ida';
  export let busquedaId = null;
  export let origenId = null;
  export let destinoId = null;
  export let fecha = null;
  
  let flights = [];
  let loadingFlights = true;
  let errorFlights = '';
  let selectedFlight = null;
  let selectedCategory = null;

  onMount(async () => {
    await cargarVuelos();
  });

  async function cargarVuelos() {
    if (!origenId || !destinoId || !fecha) {
      errorFlights = 'Faltan parámetros para cargar vuelos';
      loadingFlights = false;
      return;
    }

    loadingFlights = true;
    errorFlights = '';

    try {
      const url = `https://localhost:7107/api/vuelos?origenId=${origenId}&destinoId=${destinoId}&fecha=${fecha}`;
      console.log('Cargando vuelos desde:', url);
      
      const response = await fetch(url);
      
      if (!response.ok) {
        throw new Error('Error al cargar vuelos');
      }

      const data = await response.json();
      flights = data;
      
      console.log('Vuelos cargados:', flights.length);

    } catch (err) {
      console.error('Error cargando vuelos:', err);
      errorFlights = 'No se pudieron cargar los vuelos. Intenta nuevamente.';
    } finally {
      loadingFlights = false;
    }
  }

  function selectFlight(flight, category) {
    selectedFlight = flight;
    selectedCategory = category;
    onFlightSelect({ flight, category, flightType });
  }

  function formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  }

  function formatTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  }

  function formatCityWithCode(aeropuerto) {
    if (!aeropuerto) return 'Desconocido';
    return `${aeropuerto.ciudad} (${aeropuerto.codigo})`;
  }
</script>

<div class="flight-selection">
  <h2 class="flight-selection__title">
    {flightType === 'ida' ? 'Vuelo de Ida' : 'Vuelo de Regreso'}
  </h2>

  {#if loadingFlights}
    <div class="flight-selection__loading">
      <p>Cargando vuelos disponibles...</p>
    </div>
  {:else if errorFlights}
    <div class="flight-selection__error">
      <p>{errorFlights}</p>
    </div>
  {:else if flights.length === 0}
    <div class="flight-selection__empty">
      <p>No hay vuelos disponibles para esta ruta y fecha.</p>
    </div>
  {:else}
    <div class="flight-selection__list">
      {#each flights as flight}
        <div class="flight-card">
          <div class="flight-card__info">
            <div class="flight-card__route">
              <div class="flight-card__time">
                <span class="time">{formatTime(flight.horaSalida)}</span>
                <span class="location">{formatCityWithCode(flight.aeropuertoOrigen)}</span>
              </div>
              
              <div class="flight-card__duration">
                <div class="duration-line"></div>
                <span class="duration-text">{formatDuration(flight.duracion)}</span>
              </div>
              
              <div class="flight-card__time">
                <span class="time">{formatTime(flight.horaLlegada)}</span>
                <span class="location">{formatCityWithCode(flight.aeropuertoDestino)}</span>
              </div>
            </div>
            
            <div class="flight-card__airline">{flight.aerolinea || 'Broom AirLine'}</div>
          </div>

          <div class="flight-card__prices">
            <button 
              class="price-option" 
              class:selected={selectedFlight?.id === flight.id && selectedCategory === 'economico'}
              on:click={() => selectFlight(flight, 'economico')}
            >
              <span class="category">Economico</span>
              <span class="price">${flight.precioEconomico || flight.precioBase}</span>
            </button>

            <button 
              class="price-option" 
              class:selected={selectedFlight?.id === flight.id && selectedCategory === 'normal'}
              on:click={() => selectFlight(flight, 'normal')}
            >
              <span class="category">Normal</span>
              <span class="price">${flight.precioNormal || (flight.precioBase * 1.5)}</span>
            </button>

            <button 
              class="price-option" 
              class:selected={selectedFlight?.id === flight.id && selectedCategory === 'ejecutivo'}
              on:click={() => selectFlight(flight, 'ejecutivo')}
            >
              <span class="category">Ejecutivo</span>
              <span class="price">${flight.precioEjecutivo || (flight.precioBase * 2.5)}</span>
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>