<script lang="ts">
  import '../styles/flight-notification.css';
  import avionPath from '../assets/AvionB.png';

  export let onDestinationClick = (city: string) => {};

  // Lista de destinos que se mostrarán aleatoriamente
  const destinations = [
    { city: 'París', country: 'Francia' },
    { city: 'Tokio', country: 'Japón' },
    { city: 'Nueva York', country: 'Estados Unidos' },
    { city: 'Barcelona', country: 'España' },
    { city: 'Londres', country: 'Inglaterra' },
    { city: 'Roma', country: 'Italia' },
    { city: 'Dubái', country: 'Emiratos Árabes' },
    { city: 'Cancún', country: 'México' }
  ];

  let showNotification = false;
  let currentDestination = destinations[0];
  let isVisible = false;

  function getRandomDestination() {
    const randomIndex = Math.floor(Math.random() * destinations.length);
    return destinations[randomIndex];
  }

  function startAnimation() {
    currentDestination = getRandomDestination();
    showNotification = true;
    isVisible = true;

    // Ocultar después de que termine la animación
    setTimeout(() => {
      isVisible = false;
      setTimeout(() => {
        showNotification = false;
      }, 500);
    }, 6000);
  }

  function handleDestinationClick() {
    onDestinationClick(currentDestination.city);
    isVisible = false;
    setTimeout(() => {
      showNotification = false;
    }, 500);
  }

  // Iniciar la primera animación después de un delay
  setTimeout(() => {
    startAnimation();
  }, 3000);

  // Repetir cada 15 segundos
  setInterval(() => {
    if (!showNotification) {
      startAnimation();
    }
  }, 15000);
</script>

{#if showNotification}
  <div 
    class="broom-flight-notification" 
    class:broom-flight-notification--visible={isVisible}
  >
    <!-- Avión -->
    <div class="broom-flight-notification__plane">
      <img 
        src={avionPath} 
        alt="Avión" 
        class="broom-flight-notification__plane-img"
      >
    </div>

    <!-- Burbuja de texto -->
    <div class="broom-flight-notification__bubble">
      <button 
        class="broom-flight-notification__bubble-content"
        on:click={handleDestinationClick}
        type="button"
      >
        <span class="broom-flight-notification__bubble-label">¿Qué tal volar a</span>
        <span class="broom-flight-notification__bubble-destination">
          {currentDestination.city}?
        </span>
        <span class="broom-flight-notification__bubble-country">
          {currentDestination.country}
        </span>
      </button>
      
      <!-- Piquito de la burbuja -->
      <div class="broom-flight-notification__bubble-tail"></div>
    </div>

    <!-- Botón para cerrar -->
    <button 
      class="broom-flight-notification__close"
      on:click={() => { isVisible = false; setTimeout(() => showNotification = false, 500); }}
      aria-label="Cerrar notificación"
      type="button"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" class="broom-flight-notification__close-icon">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>
  </div>
{/if}