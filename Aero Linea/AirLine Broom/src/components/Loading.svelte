<script>
  import '../styles/Loading.css';
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