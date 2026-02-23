<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">🔒</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Política de Privacidad</h1>
      <p class="info-hero__subtitle">Tu privacidad es importante para nosotros. Conoce cómo protegemos tus datos.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <div class="info-card">
      <span class="info-badge">🔒 Vigente desde febrero 2026</span>
      <h2 class="info-section-title">Datos que recopilamos</h2>
      <p class="info-prose">Para brindarte nuestros servicios de reservación, recopilamos los siguientes datos personales:</p>
      <ul class="info-list">
        <li>Nombre completo, correo electrónico y número de teléfono</li>
        <li>Número de pasaporte y fecha de nacimiento</li>
        <li>País y ciudad de residencia</li>
        <li>Información de pago (procesada de forma segura, no almacenamos datos de tarjetas)</li>
        <li>Historial de reservaciones y búsquedas realizadas</li>
      </ul>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Cómo usamos tu información</h2>
      <ul class="info-list">
        <li>Procesar y gestionar tus reservaciones hoteleras</li>
        <li>Enviarte confirmaciones y notificaciones relacionadas a tu estadía</li>
        <li>Mejorar nuestros servicios mediante análisis de tendencias de uso</li>
        <li>Cumplir con obligaciones legales y fiscales</li>
        <li>Enviarte comunicaciones de marketing solo si lo autorizaste al registrarte</li>
      </ul>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Seguridad de tus datos</h2>
      <p class="info-prose">Implementamos medidas técnicas y organizativas para proteger tu información:</p>
      <ul class="info-list">
        <li>Encriptación SSL en todas las comunicaciones</li>
        <li>Autenticación segura con contraseñas encriptadas</li>
        <li>Acceso a datos restringido según rol de usuario</li>
        <li>Auditorías de seguridad periódicas</li>
      </ul>
      <div class="info-highlight">
        No compartimos tu información personal con terceros sin tu consentimiento, salvo cuando sea requerido por ley o sea necesario para procesar tu reservación.
      </div>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Tus derechos</h2>
      <ul class="info-list">
        <li>Acceder a los datos personales que tenemos sobre ti</li>
        <li>Solicitar la corrección de datos incorrectos</li>
        <li>Solicitar la eliminación de tu cuenta y datos asociados</li>
        <li>Revocar el consentimiento para comunicaciones de marketing</li>
      </ul>
      <p class="info-prose">Para ejercer cualquiera de estos derechos, contáctanos en <strong>info@mikuinn.com</strong></p>
    </div>
  </div>
</div>