<script>
  /**
   * @file SobreNosotros.svelte
   * @description Pagina "Sobre Nosotros" de Miku Inn. Presenta la historia,
   * vision y propuesta de valor de la cadena hotelera guatemalteca, junto con
   * tarjetas que destacan sus puntos clave: presencia regional, pagos seguros,
   * soporte y calidad de habitaciones.
   */

  // @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo;
</script>

<div class="info-page">
  <!-- Hero de la pagina con icono y subtitulo -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">🏨</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Sobre Nosotros</h1>
      <p class="info-hero__subtitle">Una cadena hotelera comprometida con brindarte experiencias inolvidables en cada estadía.</p>
    </div>
  </div>

  <div class="info-container">
    <!-- Boton para regresar al inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <!-- Bloque: quienes somos y descripcion de la plataforma -->
    <div class="info-card">
      <h2 class="info-section-title">¿Quiénes somos?</h2>
      <p class="info-prose">Miku Inn es una cadena hotelera guatemalteca fundada con la visión de ofrecer hospitalidad de primer nivel combinada con tecnología moderna. Contamos con hoteles estratégicamente ubicados para satisfacer tanto a viajeros de negocios como de placer.</p>
      <p class="info-prose">Nuestra plataforma digital centraliza la gestión de reservaciones, control de inventario y atención al cliente, garantizando una experiencia de reservación moderna, confiable y segura.</p>
    </div>

    <!-- Bloque: vision de la empresa -->
    <div class="info-card">
      <h2 class="info-section-title">Nuestra visión</h2>
      <div class="info-highlight">
        Establecer a Miku Inn como referente de innovación hotelera mediante una plataforma que unifique tecnología, operaciones y experiencia del cliente en un solo sistema integrado.
      </div>
      <p class="info-prose">Operamos con estándares altos de disponibilidad y rendimiento, permitiendo a nuestros huéspedes acceder a información actualizada, comparar opciones y gestionar sus estadías en cualquier momento.</p>
    </div>

    <!-- Grid de tarjetas con puntos clave de valor -->
    <div class="info-cards-grid">
      <div class="info-card">
        <span class="info-card__icon">🌍</span>
        <h3 class="info-card__title">Presencia regional</h3>
        <p class="info-card__text">Hoteles ubicados estratégicamente en Guatemala y con planes de expansión en toda Centroamérica.</p>
      </div>
      <div class="info-card">
        <span class="info-card__icon">🔒</span>
        <h3 class="info-card__title">Pagos seguros</h3>
        <p class="info-card__text">Procesamiento de pagos con encriptación SSL y verificación en tiempo real para tu tranquilidad.</p>
      </div>
      <div class="info-card">
        <span class="info-card__icon">📞</span>
        <h3 class="info-card__title">Soporte 24/7</h3>
        <p class="info-card__text">Nuestro equipo está disponible las 24 horas para asistirte antes, durante y después de tu estadía.</p>
      </div>
      <div class="info-card">
        <span class="info-card__icon">⭐</span>
        <h3 class="info-card__title">Calidad garantizada</h3>
        <p class="info-card__text">Habitaciones Doble, Junior Suite, Suite y Gran Suite con características y precios claramente definidos.</p>
      </div>
    </div>
  </div>
</div>
