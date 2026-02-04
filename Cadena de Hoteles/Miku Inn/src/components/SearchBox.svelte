<script>
  import { createEventDispatcher } from 'svelte';
  
  const dispatch = createEventDispatcher();
  
  // Tipo de búsqueda: 'hotels' o 'flights'
  let searchType = 'hotels';
  
  // Parámetros para hoteles
  export let destination = '';
  export let checkIn = '';
  export let checkOut = '';
  export let guests = 1;
  
  // Parámetros para vuelos
  let origin = '';
  let flightDestination = '';
  let departureDate = '';
  let returnDate = '';
  let passengers = 1;
  let flightClass = 'Económica';
  let tripType = 'roundtrip'; // 'oneway' o 'roundtrip'
  
  // Lista de ciudades/aeropuertos (en producción vendría de un API)
  const cities = [
    { name: 'Guatemala City', code: 'GUA' },
    { name: 'París', code: 'CDG' },
    { name: 'Madrid', code: 'MAD' },
    { name: 'Nueva York', code: 'JFK' },
    { name: 'Londres', code: 'LHR' },
    { name: 'Miami', code: 'MIA' },
    { name: 'Los Ángeles', code: 'LAX' },
    { name: 'Ciudad de México', code: 'MEX' },
    { name: 'San Salvador', code: 'SAL' },
    { name: 'San José', code: 'SJO' }
  ];
  
  function handleSubmit() {
    if (searchType === 'hotels') {
      dispatch('search', {
        type: 'hotels',
        destination,
        checkIn,
        checkOut,
        guests
      });
      // Redirigir a resultados de hoteles
      const params = new URLSearchParams({
        type: 'hotels',
        destination,
        checkIn,
        checkOut,
        guests: guests.toString()
      });
      window.location.href = `/search?${params.toString()}`;
    } else {
      dispatch('search', {
        type: 'flights',
        origin,
        destination: flightDestination,
        departureDate,
        returnDate: tripType === 'roundtrip' ? returnDate : '',
        passengers,
        flightClass,
        tripType
      });
      // Redirigir a resultados de vuelos
      const params = new URLSearchParams({
        type: 'flights',
        origin,
        destination: flightDestination,
        departureDate,
        passengers: passengers.toString(),
        flightClass
      });
      if (tripType === 'roundtrip' && returnDate) {
        params.append('returnDate', returnDate);
      }
      window.location.href = `/search?${params.toString()}`;
    }
  }
  
  function updateCheckOut() {
    if (checkIn) {
      const checkInDate = new Date(checkIn);
      const minCheckOut = new Date(checkInDate);
      minCheckOut.setDate(minCheckOut.getDate() + 1);
      
      if (!checkOut || new Date(checkOut) <= checkInDate) {
        checkOut = minCheckOut.toISOString().split('T')[0];
      }
    }
  }
  
  function updateReturnDate() {
    if (tripType === 'roundtrip' && departureDate) {
      const depDate = new Date(departureDate);
      const minReturn = new Date(depDate);
      minReturn.setDate(minReturn.getDate() + 1);
      
      if (!returnDate || new Date(returnDate) <= depDate) {
        returnDate = minReturn.toISOString().split('T')[0];
      }
    }
  }
</script>

<div class="search-box">
  <!-- Pestañas para cambiar tipo de búsqueda -->
  <div class="search-tabs">
    <button 
      class="search-tab"
      class:active={searchType === 'hotels'}
      on:click={() => searchType = 'hotels'}
      type="button"
    >
      <span class="tab-icon">🏨</span>
      Hoteles
    </button>
    <button 
      class="search-tab"
      class:active={searchType === 'flights'}
      on:click={() => searchType = 'flights'}
      type="button"
    >
      <span class="tab-icon">✈️</span>
      Vuelos
    </button>
  </div>
  
  <form on:submit|preventDefault={handleSubmit}>
    {#if searchType === 'hotels'}
      <!-- Formulario de búsqueda de hoteles -->
      <h2>Encuentra tu hotel ideal</h2>
      
      <div class="search-grid">
        <div class="form-group">
          <label for="destination">
            <i class="icon">📍</i>
            Destino
          </label>
          <input
            type="text"
            id="destination"
            bind:value={destination}
            placeholder="¿A dónde viajas?"
            required
            list="hotel-destinations"
          />
          <datalist id="hotel-destinations">
            {#each cities as city}
              <option value={city.name}>{city.name}</option>
            {/each}
          </datalist>
        </div>
        
        <div class="form-group">
          <label for="checkIn">
            <i class="icon">📅</i>
            Check-in
          </label>
          <input
            type="date"
            id="checkIn"
            bind:value={checkIn}
            on:change={updateCheckOut}
            min={new Date().toISOString().split('T')[0]}
            required
          />
        </div>
        
        <div class="form-group">
          <label for="checkOut">
            <i class="icon">📅</i>
            Check-out
          </label>
          <input
            type="date"
            id="checkOut"
            bind:value={checkOut}
            min={checkIn}
            required
          />
        </div>
        
        <div class="form-group">
          <label for="guests">
            <i class="icon">👥</i>
            Huéspedes
          </label>
          <select id="guests" bind:value={guests}>
            {#each Array(10) as _, i}
              <option value={i + 1}>{i + 1} {i === 0 ? 'Huésped' : 'Huéspedes'}</option>
            {/each}
          </select>
        </div>
      </div>
      
      <button type="submit" class="search-button">
        Buscar Hoteles
      </button>
    {:else}
      <!-- Formulario de búsqueda de vuelos -->
      <h2>Encuentra tu vuelo ideal</h2>
      
      <!-- Tipo de viaje -->
      <div class="trip-type-selector">
        <label class="trip-type-option">
          <input 
            type="radio" 
            bind:group={tripType} 
            value="roundtrip"
            on:change={updateReturnDate}
          />
          <span>Ida y vuelta</span>
        </label>
        <label class="trip-type-option">
          <input 
            type="radio" 
            bind:group={tripType} 
            value="oneway"
          />
          <span>Solo ida</span>
        </label>
      </div>
      
      <div class="search-grid">
        <div class="form-group">
          <label for="origin">
            <i class="icon">🛫</i>
            Origen
          </label>
          <input
            type="text"
            id="origin"
            bind:value={origin}
            placeholder="Ciudad de origen"
            required
            list="flight-origins"
          />
          <datalist id="flight-origins">
            {#each cities as city}
              <option value="{city.name}">{city.name} ({city.code})</option>
            {/each}
          </datalist>
        </div>
        
        <div class="form-group">
          <label for="flightDestination">
            <i class="icon">🛬</i>
            Destino
          </label>
          <input
            type="text"
            id="flightDestination"
            bind:value={flightDestination}
            placeholder="Ciudad de destino"
            required
            list="flight-destinations"
          />
          <datalist id="flight-destinations">
            {#each cities as city}
              <option value="{city.name}">{city.name} ({city.code})</option>
            {/each}
          </datalist>
        </div>
        
        <div class="form-group">
          <label for="departureDate">
            <i class="icon">📅</i>
            Fecha de salida
          </label>
          <input
            type="date"
            id="departureDate"
            bind:value={departureDate}
            on:change={updateReturnDate}
            min={new Date().toISOString().split('T')[0]}
            required
          />
        </div>
        
        {#if tripType === 'roundtrip'}
          <div class="form-group">
            <label for="returnDate">
              <i class="icon">📅</i>
              Fecha de regreso
            </label>
            <input
              type="date"
              id="returnDate"
              bind:value={returnDate}
              min={departureDate}
              required={tripType === 'roundtrip'}
            />
          </div>
        {/if}
        
        <div class="form-group">
          <label for="passengers">
            <i class="icon">👥</i>
            Pasajeros
          </label>
          <select id="passengers" bind:value={passengers}>
            {#each Array(9) as _, i}
              <option value={i + 1}>{i + 1} {i === 0 ? 'Pasajero' : 'Pasajeros'}</option>
            {/each}
          </select>
        </div>
        
        <div class="form-group">
          <label for="flightClass">
            <i class="icon">💺</i>
            Clase
          </label>
          <select id="flightClass" bind:value={flightClass}>
            <option value="Económica">Económica</option>
            <option value="Premium Economy">Premium Economy</option>
            <option value="Business">Business</option>
            <option value="Primera">Primera Clase</option>
          </select>
        </div>
      </div>
      
      <button type="submit" class="search-button">
        Buscar Vuelos
      </button>
    {/if}
  </form>
</div>

<style>
  .search-box {
    background: white;
    padding: 0;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    overflow: hidden;
  }
  
  /* ========== PESTAÑAS ========== */
  .search-tabs {
    display: flex;
    background: #f8f9fa;
    border-bottom: 2px solid #e0e0e0;
  }
  
  .search-tab {
    flex: 1;
    padding: 1.25rem 2rem;
    background: none;
    border: none;
    cursor: pointer;
    font-weight: 600;
    font-size: 1rem;
    color: #666;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    border-bottom: 3px solid transparent;
  }
  
  .search-tab:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.05);
  }
  
  .search-tab.active {
    color: #667eea;
    background: white;
    border-bottom-color: #667eea;
  }
  
  .tab-icon {
    font-size: 1.3rem;
  }
  
  /* ========== FORMULARIO ========== */
  form {
    padding: 2.5rem;
  }
  
  .search-box h2 {
    color: #333;
    margin-bottom: 1.5rem;
    font-size: 1.8rem;
    text-align: center;
  }
  
  /* Selector de tipo de viaje */
  .trip-type-selector {
    display: flex;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
    justify-content: center;
  }
  
  .trip-type-option {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    font-weight: 500;
    color: #555;
  }
  
  .trip-type-option input[type="radio"] {
    cursor: pointer;
  }
  
  .search-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .form-group {
    display: flex;
    flex-direction: column;
  }
  
  .form-group label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 600;
    color: #555;
    margin-bottom: 0.5rem;
    font-size: 0.95rem;
  }
  
  .icon {
    font-size: 1.2rem;
  }
  
  .form-group input,
  .form-group select {
    padding: 0.875rem;
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    transition: all 0.3s ease;
  }
  
  .form-group input:focus,
  .form-group select:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .search-button {
    width: 100%;
    padding: 1rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1.1rem;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }
  
  .search-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
  }
  
  .search-button:active {
    transform: translateY(0);
  }
  
  @media (max-width: 768px) {
    form {
      padding: 1.5rem;
    }
    
    .search-grid {
      grid-template-columns: 1fr;
    }
    
    .search-tab {
      padding: 1rem;
      font-size: 0.9rem;
    }
    
    .trip-type-selector {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.75rem;
    }
  }
</style>