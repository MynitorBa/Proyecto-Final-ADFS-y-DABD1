<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">✉️</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Contáctanos</h1>
      <p class="info-hero__subtitle">Estamos aquí para ayudarte. Escríbenos o llámanos en cualquier momento.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <div class="info-contact-grid">
      <div class="info-contact-item">
        <div class="info-contact-item__icon">📞</div>
        <div>
          <p class="info-contact-item__label">Teléfono 24/7</p>
          <p class="info-contact-item__value">+502 4276-8687</p>
          <p class="info-contact-item__sub">Llamadas y WhatsApp disponibles</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">✉️</div>
        <div>
          <p class="info-contact-item__label">Correo electrónico</p>
          <p class="info-contact-item__value">info@mikuinn.com</p>
          <p class="info-contact-item__sub">Respuesta en menos de 24 horas</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">📍</div>
        <div>
          <p class="info-contact-item__label">Oficina Central</p>
          <p class="info-contact-item__value">Guatemala City, Guatemala</p>
          <p class="info-contact-item__sub">Lunes a viernes 8am – 6pm</p>
        </div>
      </div>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Horarios de atención</h2>
      <ul class="info-list">
        <li>Soporte telefónico: disponible las 24 horas, 7 días a la semana</li>
        <li>Soporte por correo: respuesta en menos de 24 horas hábiles</li>
        <li>Oficina central: lunes a viernes de 8:00am a 6:00pm</li>
        <li>Para emergencias durante tu estadía, contacta directamente a recepción del hotel</li>
      </ul>
      <div class="info-highlight">
        Para reservaciones grupales o empresariales, escríbenos a <strong>info@mikuinn.com</strong> indicando el número de habitaciones y fechas deseadas.
      </div>
    </div>
  </div>
</div>