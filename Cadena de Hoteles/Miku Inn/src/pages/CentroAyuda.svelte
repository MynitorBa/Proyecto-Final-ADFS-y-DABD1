<script>
  /**
   * @file CentroAyuda.svelte
   * @description Pagina informativa del Centro de Ayuda de Miku Inn.
   * Muestra los canales de contacto disponibles, temas de ayuda frecuentes
   * y un bloque de soporte para consultas que no tienen respuesta rapida.
   */

  // @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion de navegacion recibida desde App. @type {Function} */
  export let navigateTo;
</script>

<!-- Pagina de ayuda con hero, contacto, temas frecuentes y bloque de soporte -->
<div class="info-page">
  <!-- Banner principal de la seccion -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">🛎️</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Centro de Ayuda</h1>
      <p class="info-hero__subtitle">Encuentra respuestas rápidas y soporte para todo lo relacionado con tus reservaciones.</p>
    </div>
  </div>

  <div class="info-container">
    <!-- Boton para regresar al inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <!-- Tarjetas de canales de contacto: telefono, email y ubicacion -->
    <div class="info-contact-grid">
      <div class="info-contact-item">
        <div class="info-contact-item__icon">📞</div>
        <div>
          <p class="info-contact-item__label">Atención 24/7</p>
          <p class="info-contact-item__value">+502 4276-8687</p>
          <p class="info-contact-item__sub">Llamadas y WhatsApp</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">✉️</div>
        <div>
          <p class="info-contact-item__label">Email</p>
          <p class="info-contact-item__value">info@mikuinn.com</p>
          <p class="info-contact-item__sub">Respuesta en menos de 24h</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">📍</div>
        <div>
          <p class="info-contact-item__label">Oficina Central</p>
          <p class="info-contact-item__value">Guatemala City</p>
          <p class="info-contact-item__sub">Guatemala</p>
        </div>
      </div>
    </div>

    <!-- Cuadricula con los temas de ayuda mas consultados -->
    <div class="info-card">
      <h2 class="info-section-title">Temas de ayuda frecuentes</h2>
      <div class="info-cards-grid">
        <!-- Tema: gestion de reservaciones -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">📋</span>
          <h3 class="info-card__title">Mis reservaciones</h3>
          <p class="info-card__text">Consulta, modifica o cancela tus reservaciones desde tu perfil en "Mis Reservaciones".</p>
        </div>
        <!-- Tema: pagos y facturacion -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">💳</span>
          <h3 class="info-card__title">Pagos y facturas</h3>
          <p class="info-card__text">Aceptamos tarjetas de crédito y débito. Tu factura se genera automáticamente al confirmar el pago.</p>
        </div>
        <!-- Tema: procedimientos de entrada y salida -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">🔑</span>
          <h3 class="info-card__title">Check-in / Check-out</h3>
          <p class="info-card__text">Consulta los horarios y procedimientos de cada hotel directamente en su página de detalle.</p>
        </div>
        <!-- Tema: politica de cancelaciones -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">❌</span>
          <h3 class="info-card__title">Cancelaciones</h3>
          <p class="info-card__text">Puedes cancelar hasta 24 horas antes del check-in. Ver política completa de cancelación.</p>
        </div>
      </div>
    </div>

    <!-- Bloque de soporte adicional con datos de contacto destacados -->
    <div class="info-card">
      <h2 class="info-section-title">¿No encontraste lo que buscabas?</h2>
      <p class="info-prose">Nuestro equipo de soporte está disponible las 24 horas del día, los 7 días de la semana. Contáctanos por teléfono o escríbenos al correo y te responderemos a la brevedad posible.</p>
      <div class="info-highlight">
        Para consultas urgentes llámanos al <strong>+502 4276-8687</strong> o escríbenos a <strong>info@mikuinn.com</strong>
      </div>
    </div>
  </div>
</div>
