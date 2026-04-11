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
      <div class="info-hero__icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M18 8h1a4 4 0 0 1 0 8h-1"/>
          <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/>
          <line x1="6" y1="1" x2="6" y2="4"/>
          <line x1="10" y1="1" x2="10" y2="4"/>
          <line x1="14" y1="1" x2="14" y2="4"/>
        </svg>
      </div>
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
      <!-- Canal: telefono y whatsapp -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.21h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6 6l.92-.92a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Atención 24/7</p>
          <p class="info-contact-item__value">+502 4276-8687</p>
          <p class="info-contact-item__sub">Llamadas y WhatsApp</p>
        </div>
      </div>

      <!-- Canal: correo electronico -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Email</p>
          <p class="info-contact-item__value">info@mikuinn.com</p>
          <p class="info-contact-item__sub">Respuesta en menos de 24h</p>
        </div>
      </div>

      <!-- Canal: ubicacion de oficina central -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
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
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </span>
          <h3 class="info-card__title">Mis reservaciones</h3>
          <p class="info-card__text">Consulta, modifica o cancela tus reservaciones desde tu perfil en "Mis Reservaciones".</p>
        </div>

        <!-- Tema: pagos y facturacion -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </span>
          <h3 class="info-card__title">Pagos y facturas</h3>
          <p class="info-card__text">Aceptamos tarjetas de crédito y débito. Tu factura se genera automáticamente al confirmar el pago.</p>
        </div>

        <!-- Tema: procedimientos de entrada y salida -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </span>
          <h3 class="info-card__title">Check-in / Check-out</h3>
          <p class="info-card__text">Consulta los horarios y procedimientos de cada hotel directamente en su página de detalle.</p>
        </div>

        <!-- Tema: politica de cancelaciones -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </span>
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