<script>
  /**
   * @file PoliticaCancelacion.svelte
   * @description Pagina informativa que explica las politicas de cancelacion
   * de Miku Inn, tanto para cancelaciones iniciadas por el cliente como por
   * el hotel, incluyendo plazos y el proceso paso a paso.
   */

  // @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion de navegacion inyectada por el router padre. @type {Function} */
  export let navigateTo;
</script>

<div class="info-page">
  <!-- Hero de la pagina con icono y titulo principal -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">❌</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Política de Cancelación</h1>
      <p class="info-hero__subtitle">Conoce las condiciones y plazos para cancelar o modificar tus reservaciones.</p>
    </div>
  </div>

  <div class="info-container">
    <!-- Boton para regresar al inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <!-- Bloque: cancelacion iniciada por el cliente -->
    <div class="info-card">
      <span class="info-badge">📋 Política vigente 2026</span>
      <h2 class="info-section-title">Cancelación por parte del cliente</h2>
      <p class="info-prose">Los clientes pueden cancelar sus reservaciones directamente desde su perfil en la sección "Mis Reservaciones" hasta <strong>24 horas antes del check-in</strong>.</p>
      <ul class="info-list">
        <li>La cancelación debe realizarse con al menos 24 horas de anticipación al check-in</li>
        <li>Debes confirmar la cancelación antes de que sea procesada</li>
        <li>Recibirás una confirmación por correo electrónico inmediatamente</li>
        <li>La habitación quedará disponible automáticamente para otros usuarios</li>
        <li>El historial de cancelación se conserva en tu perfil de forma permanente</li>
      </ul>
      <div class="info-highlight">
        Cancelaciones realizadas con menos de 24 horas antes del check-in no están permitidas en la plataforma. Para casos especiales, contacta a nuestro equipo de soporte al <strong>+502 4276-8687</strong>.
      </div>
    </div>

    <!-- Bloque: cancelacion iniciada por el hotel -->
    <div class="info-card">
      <h2 class="info-section-title">Cancelación por parte del hotel</h2>
      <p class="info-prose">En casos excepcionales, el hotel puede cancelar una reservación. En ese caso:</p>
      <ul class="info-list">
        <li>Se te notificará automáticamente por correo electrónico</li>
        <li>El correo incluirá el motivo de la cancelación</li>
        <li>Tu reservación cambiará a estado "Reserva Cancelada por Hotel"</li>
        <li>Nuestro equipo te contactará para ofrecerte alternativas disponibles</li>
      </ul>
    </div>

    <!-- Bloque: proceso paso a paso para cancelar desde la plataforma -->
    <div class="info-card">
      <h2 class="info-section-title">Proceso de cancelación paso a paso</h2>
      <ul class="info-list">
        <li>Ingresa a tu cuenta y ve a "Mis Reservaciones"</li>
        <li>Selecciona la reservación que deseas cancelar</li>
        <li>Haz clic en el botón "Cancelar reserva"</li>
        <li>Confirma la cancelación en el diálogo de verificación</li>
        <li>Recibirás el correo de confirmación en minutos</li>
      </ul>
    </div>
  </div>
</div>
