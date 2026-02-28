<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Politica de Cookies</h1>
      <p class="info-hero__subtitle">Conoce como usamos las cookies para mantener tu sesion activa y mejorar tu experiencia en la plataforma.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
      </svg>
      Volver al inicio
    </button>

    <div class="info-card">
      <span class="info-badge">Vigente desde febrero 2026</span>

      <h2 class="info-section-title">Que son las cookies?</h2>
      <p class="info-prose">Las cookies son pequenos archivos de texto que se almacenan en tu dispositivo cuando visitas nuestra plataforma. Nos permiten reconocerte entre visitas, mantener tu sesion activa y recordar tus preferencias sin que tengas que volver a ingresar tus datos cada vez.</p>

      <h2 class="info-section-title">Tipos de cookies que usamos</h2>

      <div class="info-cards-grid" style="margin-top:1rem">
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
          </span>
          <h3 class="info-card__title">Cookies de sesion</h3>
          <p class="info-card__text">Esenciales para el funcionamiento del sistema. Mantienen tu sesion activa mientras navegas segun tu rol (Cliente, Administrador o WebService). Se eliminan automaticamente al cerrar el navegador. Sin estas cookies no es posible iniciar sesion ni completar una reserva.</p>
        </div>
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>
            </svg>
          </span>
          <h3 class="info-card__title">Cookies funcionales</h3>
          <p class="info-card__text">Recuerdan tus preferencias de busqueda como origen, destino y tipo de vuelo para facilitarte futuras consultas. No recopilan datos personales sensibles y pueden desactivarse desde la configuracion de tu navegador.</p>
        </div>
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
          </span>
          <h3 class="info-card__title">Cookies analiticas</h3>
          <p class="info-card__text">Nos ayudan a registrar las busquedas realizadas (origen, destino, fechas de viaje) para generar estadisticas de rutas mas buscadas y mejorar la oferta de vuelos. Los datos se almacenan de forma anonimizada y son accesibles solo para administradores.</p>
        </div>
      </div>

      <h2 class="info-section-title">Seguridad y CAPTCHA</h2>
      <p class="info-prose">Como medida de seguridad adicional, implementamos CAPTCHA en los formularios de registro y en el login despues de 2 intentos fallidos consecutivos. Esto previene accesos automatizados por bots y protege la integridad de las cuentas de nuestros usuarios.</p>

      <h2 class="info-section-title">Como controlar las cookies</h2>
      <p class="info-prose">Puedes gestionar o deshabilitar las cookies desde la configuracion de tu navegador. Toma en cuenta que las cookies de sesion son indispensables para el funcionamiento del sistema: sin ellas no podras iniciar sesion, realizar reservas ni acceder a tu historial.</p>

      <div class="info-highlight">
        Las cookies de sesion son necesarias para autenticarte y completar el proceso de compra. Deshabilitarlas impedira el correcto funcionamiento de la plataforma.
      </div>
    </div>
  </div>
</div>