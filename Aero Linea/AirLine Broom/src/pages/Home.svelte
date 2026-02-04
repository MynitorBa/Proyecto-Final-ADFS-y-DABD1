<script>
  import '../styles/home.css';
  import logoHero from '../assets/BroomHero1.png';
  
  export let navigateTo;
  export let suggestedDestination = null;

  let tripType = 'roundtrip'; 
  let fromCity = '';
  let toCity = '';
  let departureDate = '';
  let returnDate = '';
  let passengers = 1;

  $: if (suggestedDestination) {
    toCity = suggestedDestination;
    highlightDestinationField();
  }

  function highlightDestinationField() {
    const toField = document.getElementById('toCity');
    if (toField) {
      toField.classList.add('broom-home__form-input--highlighted');
      setTimeout(() => {
        toField.classList.remove('broom-home__form-input--highlighted');
      }, 2000);
    }
  }

  function handleSearchFlight() {
    console.log('Buscando vuelo:', {
      tripType,
      fromCity,
      toCity,
      departureDate,
      returnDate,
      passengers
    });
    
    // Navegar a la página de vuelos
    navigateTo('vuelos');
  }

  function handleMoreDestinations() {
    navigateTo('destinos-destacados');
  }

  function handleManageReservations() {
    navigateTo('reservas');
  }
</script>

<div class="broom-home">
  <!-- Hero Section -->
  <section class="broom-home__hero">
    <img src="{logoHero}" alt="Broom AirLine Hero">
    <div class="broom-home__hero-overlay">
      <h1 class="broom-home__hero-title">Vuela a donde tus sueños te lleven</h1>
      <p class="broom-home__hero-subtitle">Descubre el mundo con Broom AirLine</p>
    </div>
  </section>

  <!-- Formulario de búsqueda de vuelos -->
  <section class="broom-home__search-section">
    <div class="broom-home__search-container">
      <h2 class="broom-home__search-title">Encuentra tu vuelo</h2>
      
      <form class="broom-home__search-form" on:submit|preventDefault={handleSearchFlight}>
        <!-- Tipo de viaje -->
        <div class="broom-home__trip-type">
          <label class="broom-home__radio-label">
            <input 
              type="radio" 
              name="tripType" 
              value="roundtrip" 
              bind:group={tripType}
              class="broom-home__radio-input"
            >
            <span class="broom-home__radio-text">Ida y vuelta</span>
          </label>
          
          <label class="broom-home__radio-label">
            <input 
              type="radio" 
              name="tripType" 
              value="oneway" 
              bind:group={tripType}
              class="broom-home__radio-input"
            >
            <span class="broom-home__radio-text">Solo ida</span>
          </label>
        </div>

        <!-- Campos del formulario -->
        <div class="broom-home__form-grid">
          <!-- Desde -->
          <div class="broom-home__form-group">
            <label for="fromCity" class="broom-home__form-label">Desde</label>
            <input 
              type="text" 
              id="fromCity"
              bind:value={fromCity}
              placeholder="Ciudad de origen"
              class="broom-home__form-input"
              required
            >
          </div>

          <!-- Hacia -->
          <div class="broom-home__form-group">
            <label for="toCity" class="broom-home__form-label">Hacia</label>
            <input 
              type="text" 
              id="toCity"
              bind:value={toCity}
              placeholder="Ciudad de destino"
              class="broom-home__form-input"
              required
            >
          </div>

          <!-- Fecha de ida -->
          <div class="broom-home__form-group">
            <label for="departureDate" class="broom-home__form-label">Fecha de ida</label>
            <input 
              type="date" 
              id="departureDate"
              bind:value={departureDate}
              class="broom-home__form-input"
              required
            >
          </div>

          <!-- Fecha de regreso -->
          {#if tripType === 'roundtrip'}
            <div class="broom-home__form-group">
              <label for="returnDate" class="broom-home__form-label">Fecha de regreso</label>
              <input 
                type="date" 
                id="returnDate"
                bind:value={returnDate}
                class="broom-home__form-input"
                required
              >
            </div>
          {/if}

          <!-- Cantidad de pasajeros -->
          <div class="broom-home__form-group">
            <label for="passengers" class="broom-home__form-label">Pasajeros</label>
            <select 
              id="passengers"
              bind:value={passengers}
              class="broom-home__form-input broom-home__form-select"
            >
              {#each Array(9) as _, i}
                <option value={i + 1}>{i + 1} {i === 0 ? 'Pasajero' : 'Pasajeros'}</option>
              {/each}
            </select>
          </div>
        </div>

        <!-- Botón de búsqueda -->
        <div class="broom-home__form-actions">
          <button type="submit" class="broom-home__search-btn">
            <svg class="broom-home__search-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <circle cx="11" cy="11" r="8"></circle>
              <path d="m21 21-4.35-4.35"></path>
            </svg>
            Buscar vuelo
          </button>
        </div>
      </form>
    </div>
  </section>

  <!-- Destinos destacados -->
  <section class="broom-home__destinations">
    <div class="broom-home__destinations-container">
      <h2 class="broom-home__destinations-title">Destinos destacados</h2>
      
      <div class="broom-home__destinations-grid">
        <!-- Destino 1 -->
        <article class="broom-home__destination-card">
          <div class="broom-home__destination-image">
            <img src="https://th.bing.com/th/id/R.ef1269d03e8cd5b5d625bda0dca6f222?rik=67LTJJMuW8HnfQ&riu=http%3a%2f%2f1.bp.blogspot.com%2f-XCDP_qtL724%2fUQgMmYegCuI%2fAAAAAAAAjdE%2fhmq_X36t-Hs%2fs1600%2f202_1la_tour_eiffel___paris__france.jpg&ehk=V0GlPlEL31Rm9Kb0i%2fAKEYy92nYb%2bE1GUJsa3Lallnc%3d&risl=&pid=ImgRaw&r=0" alt="" class="broom-home__destination-image-visual">
            <div class="broom-home__destination-image-placeholder"></div>
          </div>
          <div class="broom-home__destination-content">
            <h3 class="broom-home__destination-name">París, Francia</h3>
            <p class="broom-home__destination-description">
              Descubre la ciudad del amor y sus icónicos monumentos. 
              Disfruta de la Torre Eiffel, el Louvre y la exquisita gastronomía francesa.
            </p>
          </div>
        </article>

        <!-- Destino 2 -->
        <article class="broom-home__destination-card">
          <div class="broom-home__destination-image">
            <img src="https://hldak.mmtcdn.com/prod-s3-hld-hpcmsadmin/holidays/images/cities/1921/Tokyo-Tower.jpg" alt="" class="broom-home__destination-image-visual">
            <div class="broom-home__destination-image-placeholder"></div>
          </div>
          <div class="broom-home__destination-content">
            <h3 class="broom-home__destination-name">Tokio, Japón</h3>
            <p class="broom-home__destination-description">
              Experimenta la perfecta fusión entre tradición y modernidad. 
              Templos ancestrales, tecnología de vanguardia y cultura única.
            </p>
          </div>
        </article>
      </div>

      <!-- Botón más destinos -->
      <div class="broom-home__destinations-actions">
        <button 
          type="button"
          class="broom-home__destinations-btn"
          on:click={handleMoreDestinations}
        >
          Ver más destinos
        </button>
      </div>
    </div>
  </section>

  <!-- Administrar reservas -->
  <section class="broom-home__manage-booking">
    <div class="broom-home__manage-booking-container">
      <div class="broom-home__manage-booking-content">
        <div class="broom-home__manage-booking-image">
          <img src="https://i.pinimg.com/originals/a5/0d/05/a50d05dd4ca9116119320a244c438c19.jpg" alt="">
          <div class="broom-home__manage-booking-image-placeholder"></div>
        </div>
        
        <div class="broom-home__manage-booking-info">
          <h2 class="broom-home__manage-booking-title">¿Ya tienes una reserva?</h2>
          <p class="broom-home__manage-booking-description">
            Administra tus vuelos, selecciona asientos, añade equipaje y mucho más.
          </p>
          <button 
            type="button"
            class="broom-home__manage-booking-btn"
            on:click={handleManageReservations}
          >
            Administrar reservas
          </button>
        </div>
      </div>
    </div>
  </section>
</div>