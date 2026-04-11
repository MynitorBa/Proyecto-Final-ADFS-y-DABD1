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
  <!-- Hero de la pagina con icono SVG y subtitulo -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
      </div>
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
      <!-- Tarjeta: presencia regional -->
      <div class="info-card">
        <span class="info-card__icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <circle cx="12" cy="12" r="10"/>
            <line x1="2" y1="12" x2="22" y2="12"/>
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
          </svg>
        </span>
        <h3 class="info-card__title">Presencia regional</h3>
        <p class="info-card__text">Hoteles ubicados estratégicamente en Guatemala y con planes de expansión en toda Centroamérica.</p>
      </div>

      <!-- Tarjeta: pagos seguros -->
      <div class="info-card">
        <span class="info-card__icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
        </span>
        <h3 class="info-card__title">Pagos seguros</h3>
        <p class="info-card__text">Procesamiento de pagos con encriptación SSL y verificación en tiempo real para tu tranquilidad.</p>
      </div>

      <!-- Tarjeta: soporte 24/7 -->
      <div class="info-card">
        <span class="info-card__icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.21h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6 6l.92-.92a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </span>
        <h3 class="info-card__title">Soporte 24/7</h3>
        <p class="info-card__text">Nuestro equipo está disponible las 24 horas para asistirte antes, durante y después de tu estadía.</p>
      </div>

      <!-- Tarjeta: calidad garantizada -->
      <div class="info-card">
        <span class="info-card__icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </span>
        <h3 class="info-card__title">Calidad garantizada</h3>
        <p class="info-card__text">Habitaciones Doble, Junior Suite, Suite y Gran Suite con características y precios claramente definidos.</p>
      </div>
    </div>
  </div>
</div>