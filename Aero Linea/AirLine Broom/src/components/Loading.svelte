<script>
/**
 * @file Loading.svelte
 * @description Full-screen loading overlay displayed while asynchronous operations are in
 * progress. Renders an animated sky background with clouds, a plane image that animates
 * across the screen, a progress bar, and a configurable status message. The plane container
 * size adapts to the size prop ('small', 'medium', or 'large'). Imported and shown by
 * parent pages whenever data fetching or processing is underway.
 */
  import '../styles/Loading.css';
  import avionImg from '../assets/AvionB.png';

  /** Text message displayed below the animated plane to describe the loading state. @type {string} */
  export let message = 'Preparando el despegue...';

  /** Controls the size of the plane container. Accepted values: 'small', 'medium', 'large'. @type {string} */
  export let size = 'medium';
</script>

<!-- Pantalla completa de carga con animacion de cielo, avion y barra de progreso -->
<div class="broom-loader">
  <div class="broom-loader__overlay">
    <!-- Fondo de cielo animado con nubes -->
    <div class="broom-loader__sky">
      <div class="broom-loader__cloud broom-loader__cloud--1"></div>
      <div class="broom-loader__cloud broom-loader__cloud--2"></div>
    </div>

    <!-- Avion animado con mensaje de estado y barra de progreso -->
    <div class="broom-loader__content">
      <div class="broom-loader__plane-container"
        class:broom-loader__plane-container--small={size === 'small'}
        class:broom-loader__plane-container--large={size === 'large'}>
        <img src={avionImg} alt="" class="broom-loader__plane-img">
        <div class="broom-loader__plane-shadow"></div>
      </div>

      <div class="broom-loader__info">
        <p class="broom-loader__message">{message}</p>
        <div class="broom-loader__track">
          <div class="broom-loader__progress"></div>
        </div>
      </div>
    </div>
  </div>
</div>
