<script>
  import '../styles/HeaderSimple.css';
  import logoPath from '../assets/logBAL.png';

  export let navigateTo;
</script>

<header class="simple-header">
  <div class="simple-header__container">
    <div class="simple-header__logo">
      <a href="#home" on:click|preventDefault={() => navigateTo('home')}>
        <img src={logoPath} alt="Broom AirLine" class="simple-header__logo-img">
      </a>
    </div>
    
    <div class="simple-header__title">
      <h1>Broom AirLine</h1>
    </div>
  </div>
</header>

<div class="simple-header-spacer"></div>