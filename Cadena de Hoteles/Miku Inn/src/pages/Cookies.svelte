<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">🍪</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Política de Cookies</h1>
      <p class="info-hero__subtitle">Información sobre cómo usamos cookies para mejorar tu experiencia.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <div class="info-card">
      <span class="info-badge">🍪 Vigente desde febrero 2026</span>
      <h2 class="info-section-title">¿Qué son las cookies?</h2>
      <p class="info-prose">Las cookies son pequeños archivos de texto que se almacenan en tu dispositivo cuando visitas nuestra plataforma. Nos ayudan a recordar tus preferencias y a mantener tu sesión activa de forma segura.</p>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Tipos de cookies que usamos</h2>
      <div class="info-cards-grid">
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">🔐</span>
          <h3 class="info-card__title">Sesión</h3>
          <p class="info-card__text">Mantienen tu sesión iniciada mientras navegas. Se eliminan al cerrar el navegador.</p>
        </div>
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">⚙️</span>
          <h3 class="info-card__title">Funcionales</h3>
          <p class="info-card__text">Recuerdan tus preferencias de idioma, país y configuración de búsqueda.</p>
        </div>
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">📊</span>
          <h3 class="info-card__title">Analíticas</h3>
          <p class="info-card__text">Nos permiten entender cómo los usuarios interactúan con la plataforma para mejorarla.</p>
        </div>
      </div>
    </div>

    <div class="info-card">
      <h2 class="info-section-title">Control de cookies</h2>
      <p class="info-prose">Puedes controlar y/o eliminar las cookies desde la configuración de tu navegador. Ten en cuenta que deshabilitar cookies esenciales puede afectar el funcionamiento de la plataforma, especialmente el inicio de sesión y el proceso de reservación.</p>
      <div class="info-highlight">
        Las cookies de sesión son necesarias para que puedas iniciar sesión y completar tus reservaciones de forma segura.
      </div>
      <p class="info-prose">Para más información sobre cómo gestionar cookies, visita la sección de ayuda de tu navegador o escríbenos a <strong>info@mikuinn.com</strong></p>
    </div>
  </div>
</div>