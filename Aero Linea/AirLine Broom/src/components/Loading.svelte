<script>
/**
 * @file Loading.svelte
 * @description Superposicion de carga a pantalla completa mostrada mientras operaciones asincronas
 * estan en progreso. Renderiza un fondo de cielo animado con nubes, una imagen de avion que se anima
 * a traves de la pantalla, una barra de progreso y un mensaje de estado configurable. El tamano del
 * contenedor del avion se adapta a la prop size ('small', 'medium' o 'large'). Importado y mostrado
 * por las paginas padre cuando se esta obteniendo o procesando datos.
 */
  import '../styles/Loading.css';
  import avionImg from '../assets/AvionB.png';

  /** Mensaje de texto mostrado debajo del avion animado para describir el estado de carga. @type {string} */
  export let message = 'Preparando el despegue...';

  /** Controla el tamano del contenedor del avion. Valores aceptados: 'small', 'medium', 'large'. @type {string} */
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
