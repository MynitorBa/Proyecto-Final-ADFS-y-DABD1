<script lang="ts">
/**
 * @file FlightNotification.svelte
 * @description Notificacion flotante animada que aparece periodicamente en la esquina de la
 * pantalla sugiriendo un destino aleatorio al usuario. Obtiene la lista completa de aeropuertos
 * desde la API al montar, luego los recorre mediante un temporizador. Al hacer clic en la burbuja
 * de notificacion se activa el callback proporcionado por el padre con el objeto completo del aeropuerto,
 * permitiendo que la pagina padre navegue a una busqueda de vuelos para ese destino.
 */
  import '../styles/flight-notification.css';
  import avionPath from '../assets/AvionB.png';
  import { onMount } from 'svelte';
  import { API } from '../lib/api.js';

  /**
   * Callback invocado cuando el usuario hace clic en la burbuja de destino.
   * Recibe el objeto completo del aeropuerto (id, ciudad, codigo, nombre, pais, etc.).
   * @type {(aeropuertoObj: any) => void}
   */
  export let onDestinationClick = (aeropuertoObj: any) => {};

  /** Lista mapeada de destinos construida a partir de la respuesta de la API de aeropuertos. @type {any[]} */
  let destinations = [];

  /** Indica si la carga inicial de aeropuertos aun esta en progreso. @type {boolean} */
  let loadingDestinations = true;

  /** Controla si el elemento de notificacion esta presente en el DOM. @type {boolean} */
  let showNotification = false;

  /** El objeto de destino que se muestra actualmente en la burbuja. @type {any} */
  let currentDestination = null;

  /** Controla la transicion de visibilidad y opacidad CSS de la burbuja de notificacion. @type {boolean} */
  let isVisible = false;

  /**
   * Obtiene todos los aeropuertos de la API, los mapea a objetos de destino e inicia el
   * ciclo de animacion. Muestra la primera notificacion despues de 3 segundos y se repite cada 15 segundos
   * si no hay ninguna notificacion visible en ese momento.
   * @async
   * @returns {Promise<void>}
   */
  onMount(async () => {
    try {
      const res = await fetch(`${API}/api/aeropuertos`);
      const aeropuertos = await res.json();

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

  /**
   * Devuelve un objeto de destino seleccionado aleatoriamente del arreglo destinations.
   * Devuelve null si el arreglo esta vacio.
   * @returns {any} Un objeto de destino o null.
   */
  function getRandomDestination() {
    if (destinations.length === 0) return null;
    const randomIndex = Math.floor(Math.random() * destinations.length);
    return destinations[randomIndex];
  }

  /**
   * Elige un destino aleatorio y muestra la burbuja de notificacion con una animacion de deslizamiento.
   * Despues de 6 segundos la burbuja se desvanece; tras el desvanecimiento (500ms) se elimina del DOM.
   * No hace nada si la lista de destinos esta vacia.
   */
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

  /**
   * Maneja un clic en la burbuja de destino. Pasa el objeto original del aeropuerto al
   * callback del padre, luego oculta y elimina la notificacion.
   */
  function handleDestinationClick() {
    if (currentDestination) {
      onDestinationClick(currentDestination.aeropuertoOriginal);
      isVisible = false;
      setTimeout(() => {
        showNotification = false;
      }, 500);
    }
  }
</script>

<!-- Notificacion flotante de destino sugerido con animacion de avion -->
{#if showNotification && currentDestination}
  <div
    class="broom-flight-notification"
    class:broom-flight-notification--visible={isVisible}
  >
    <!-- Icono animado del avion -->
    <div class="broom-flight-notification__plane">
      <img
        src={avionPath}
        alt="Avion"
        class="broom-flight-notification__plane-img"
      >
    </div>

    <!-- Burbuja interactiva con el destino sugerido -->
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

    <!-- Boton para cerrar la notificacion manualmente -->
    <button
      class="broom-flight-notification__close"
      on:click={() => { isVisible = false; setTimeout(() => showNotification = false, 500); }}
      aria-label="Cerrar notificacion"
      type="button"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" class="broom-flight-notification__close-icon">
        <line x1="18" y1="6" x2="6" y2="18"></line>
        <line x1="6" y1="6" x2="18" y2="18"></line>
      </svg>
    </button>
  </div>
{/if}
