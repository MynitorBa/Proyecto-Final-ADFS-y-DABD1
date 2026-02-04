<script>
  import avionImg from '../assets/AvionB.png';
  
  export let message = 'Preparando el despegue...';
  export let size = 'medium'; 
</script>

<div class="broom-loader">
  <div class="broom-loader__overlay">
    <div class="broom-loader__sky">
      <div class="broom-loader__cloud broom-loader__cloud--1"></div>
      <div class="broom-loader__cloud broom-loader__cloud--2"></div>
    </div>

    <div class="broom-loader__content">
      <div class="broom-loader__plane-container" class:broom-loader__plane-container--small={size === 'small'} class:broom-loader__plane-container--large={size === 'large'}>
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

<style>
.broom-loader__overlay {
    position: fixed;
    inset: 0;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    pointer-events: all; 
    cursor: wait;
}

.broom-loader__content {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    z-index: 10;
}

/* Contenedor del Avión */
.broom-loader__plane-container {
    width: 140px;
    position: relative;
    margin-bottom: 2rem;
    animation: broom-fly 2.5s ease-in-out infinite;
}

.broom-loader__plane-container--small { width: 90px; }
.broom-loader__plane-container--large { width: 200px; }

.broom-loader__plane-img {
    width: 100%;
    height: auto;
    display: block;
    filter: drop-shadow(0 10px 10px rgba(0,0,0,0.05));
}

/* Sombra dinámica en el suelo */
.broom-loader__plane-shadow {
    width: 60%;
    height: 6px;
    background: rgba(107, 87, 68, 0.15); /* Café muy suave */
    border-radius: 50%;
    margin: 15px auto 0;
    filter: blur(4px);
    animation: broom-shadow 2.5s ease-in-out infinite;
}

/* Texto y Progreso */
.broom-loader__info {
    text-align: center;
}

.broom-loader__message {
    color: #6b5744;
    font-size: 1rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 2px;
    margin-bottom: 1rem;
}

/* Pista de carga */
.broom-loader__track {
    width: 180px;
    height: 2px;
    background-color: #f0f0f0;
    position: relative;
    margin: 0 auto;
    overflow: hidden;
}

.broom-loader__progress {
    position: absolute;
    height: 100%;
    width: 60px;
    background-color: #6b5744;
    animation: broom-progress 1.5s infinite linear;
}

/* Nubes decorativas integradas con la marca */
.broom-loader__cloud {
    position: absolute;
    background-color: #4d2e2e;
    filter: blur(30px);
    border-radius: 50%;
    z-index: 1;
}

.broom-loader__cloud--1 { width: 300px; height: 100px; top: 15%; left: -10%; opacity: 0.5; }
.broom-loader__cloud--2 { width: 400px; height: 150px; bottom: 10%; right: -5%; opacity: 0.3; }

/* --- Animaciones --- */
@keyframes broom-fly {
    0%, 100% { transform: translateY(0) rotate(-1deg); }
    50% { transform: translateY(-25px) rotate(3deg); }
}

@keyframes broom-shadow {
    0%, 100% { transform: scale(1); opacity: 1; }
    50% { transform: scale(0.7); opacity: 0.4; }
}

@keyframes broom-progress {
    from { left: -100%; }
    to { left: 150%; }
}
</style>