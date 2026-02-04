<script>
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

<style>
  /* Reset específico */
  .simple-header,
  .simple-header * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  /* Container principal */
  .simple-header {
    background-color: #F2EFEA;
    box-shadow: 0 2px 8px rgba(28, 26, 24, 0.08);
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    border-bottom: 1px solid #E6E1DA;
  }

  .simple-header__container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0.8rem 2rem;
    display: flex;
    align-items: center;
    gap: 1.5rem;
  }

  /* Logo */
  .simple-header__logo {
    flex-shrink: 0;
  }

  .simple-header__logo-img {
    height: 40px;
    width: auto;
    display: block;
    object-fit: contain;
    cursor: pointer;
    transition: opacity 0.3s ease;
  }

  .simple-header__logo-img:hover {
    opacity: 0.8;
  }

  /* Título */
  .simple-header__title h1 {
    font-size: 1.3rem;
    font-weight: 600;
    color: #1C1A18;
    letter-spacing: -0.02em;
  }

  /* Spacer */
  .simple-header-spacer {
    height: 65px;
  }

  /* Responsive */
  @media (max-width: 768px) {
    .simple-header__container {
      padding: 0.6rem 1.2rem;
    }

    .simple-header__logo-img {
      height: 35px;
    }

    .simple-header__title h1 {
      font-size: 1.1rem;
    }

    .simple-header-spacer {
      height: 55px;
    }
  }

  @media (max-width: 480px) {
    .simple-header__title h1 {
      font-size: 1rem;
    }
  }
</style>