<script>
  /**
   * @file Terminos.svelte
   * @description Pagina de Terminos y Condiciones de Miku Inn. Detalla las
   * normas de uso de la plataforma, las condiciones de reservacion y pagos,
   * y las politicas relacionadas con resenas y calificaciones de hoteles.
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
      <div class="info-hero__icon">📄</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Términos y Condiciones</h1>
      <p class="info-hero__subtitle">Lee con atención los términos que rigen el uso de nuestra plataforma.</p>
    </div>
  </div>

  <div class="info-container">
    <!-- Boton para regresar al inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <!-- Bloque: normas de uso de la plataforma -->
    <div class="info-card">
      <span class="info-badge">📄 Vigente desde febrero 2026</span>
      <h2 class="info-section-title">Uso de la plataforma</h2>
      <p class="info-prose">Al utilizar la plataforma de Miku Inn, aceptas cumplir con los presentes términos y condiciones. El uso de nuestros servicios está destinado a personas mayores de 18 años.</p>
      <ul class="info-list">
        <li>Debes proporcionar información veraz y completa al registrarte</li>
        <li>Eres responsable de mantener la confidencialidad de tu contraseña</li>
        <li>No puedes usar la plataforma para actividades ilegales o fraudulentas</li>
        <li>Cada usuario puede tener una sola cuenta activa</li>
      </ul>
    </div>

    <!-- Bloque: condiciones que rigen las reservaciones -->
    <div class="info-card">
      <h2 class="info-section-title">Reservaciones</h2>
      <ul class="info-list">
        <li>Las reservaciones se confirman únicamente al completar el pago</li>
        <li>Cada reservación genera un código único alfanumérico (ej. MIKU-42979478)</li>
        <li>La disponibilidad se verifica en tiempo real antes de confirmar</li>
        <li>El precio total incluye el costo por noche multiplicado por el número de noches y habitaciones</li>
        <li>Las reservaciones grupales comparten el mismo código de reservación</li>
      </ul>
    </div>

    <!-- Bloque: condiciones de pago -->
    <div class="info-card">
      <h2 class="info-section-title">Pagos</h2>
      <p class="info-prose">Todos los pagos se procesan de forma segura mediante encriptación SSL.</p>
      <ul class="info-list">
        <li>Aceptamos tarjetas de crédito y débito</li>
        <li>Si el pago falla, la reservación no se guarda en el sistema</li>
        <li>No almacenamos datos completos de tarjetas de crédito</li>
        <li>Cada transacción genera un número único de referencia</li>
      </ul>
    </div>

    <!-- Bloque: politica de resenas y calificaciones -->
    <div class="info-card">
      <h2 class="info-section-title">Reseñas y calificaciones</h2>
      <ul class="info-list">
        <li>Solo usuarios con estadía completada pueden dejar reseñas</li>
        <li>Las reseñas deben ser verídicas y respetuosas</li>
        <li>Miku Inn se reserva el derecho de eliminar reseñas inapropiadas</li>
        <li>Cada usuario puede calificar un hotel una sola vez</li>
      </ul>
      <div class="info-highlight">
        Al aceptar estos términos, reconoces haber leído y comprendido todas las condiciones aquí descritas. Para dudas escríbenos a <strong>info@mikuinn.com</strong>
      </div>
    </div>
  </div>
</div>
