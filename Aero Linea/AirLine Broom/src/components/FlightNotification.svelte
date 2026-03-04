<script lang="ts">
  import '../styles/flight-notification.css';
  import avionPath from '../assets/AvionB.png';
  import { onMount } from 'svelte';

  // CORREGIDO: el callback ahora pasa el objeto aeropuerto completo
  export let onDestinationClick = (aeropuertoObj: any) => {};

  let destinations = [];
  let loadingDestinations = true;
  let showNotification = false;
  let currentDestination = null;
  let isVisible = false;

  onMount(async () => {
    try {
      const res = await fetch('https://localhost:7107/api/aeropuertos');
      const aeropuertos = await res.json();
      
      // CORREGIDO: guardamos el aeropuerto original completo para pasarlo después
      destinations = aeropuertos.map(a => ({
        city: `${a.ciudad} (${a.codigo})`,
        country: a.pais || 'Destino internacional',
        codigo: a.codigo,
        id: a.id,
        aeropuertoOriginal: a
      }));
      
      console.log('Destinos cargados para notificaciones:', destinations.length);
      
      if (destinations.length > 0) {
        setTimeout(() => {
          startAnimation();
        }, 3000);

        setInterval(() => {
          if (!showNotification) {
            startAnimation();
          }
        }, 15000);
      }
      
    } catch (err) {
      console.error('Error cargando destinos:', err);
    } finally {
      loadingDestinations = false;
    }
  });

  function getRandomDestination() {
    if (destinations.length === 0) return null;
    const randomIndex = Math.floor(Math.random() * destinations.length);
    return destinations[randomIndex];
  }

  function startAnimation() {
    if (destinations.length === 0) return;
    
    currentDestination = getRandomDestination();
    if (!currentDestination) return;
    
    showNotification = true;
    isVisible = true;

    setTimeout(() => {
      isVisible = false;
      setTimeout(() => {
        showNotification = false;
      }, 500);
    }, 6000);
  }

  function handleDestinationClick() {
    if (currentDestination) {
      // CORREGIDO: pasamos el objeto aeropuerto completo (con id, ciudad, codigo, nombre, pais, etc.)
      onDestinationClick(currentDestination.aeropuertoOriginal);
      isVisible = false;
      setTimeout(() => {
        showNotification = false;
      }, 500);
    }
  }
</script>

{#if showNotification && currentDestination}
  <div 
    class="broom-flight-notification" 
    class:broom-flight-notification--visible={isVisible}
  >
    <div class="broom-flight-notification__plane">
      <img 
        src={avionPath} 
        alt="Avión" 
        class="broom-flight-notification__plane-img"
      >
    </div>

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
      
      <div class="broom-flight-notification__bubble-tail"></div>
    </div>

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