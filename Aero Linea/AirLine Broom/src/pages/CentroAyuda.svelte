<script>
  // @ts-nocheck
/**
 * @file CentroAyuda.svelte
 * @description Static help center page for Broom AirLine. Displays contact information
 * (phone, email, and office location), a grid of frequently asked help topics covering
 * reservations, payments, luggage, and cancellations, and a call-to-action section
 * that redirects the user to the Contactanos page. No API calls are made; all content
 * is static. Appears in the user flow as an informational support resource accessible
 * from the main navigation or the footer.
 */

  import '../styles/info-pages.css';

  /** Navigation function provided by the app router to change the current page. @type {Function} */
  export let navigateTo;
</script>

<!-- Hero con titulo e icono introductorio del centro de ayuda -->
<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
          <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Centro de Ayuda</h1>
      <p class="info-hero__subtitle">Encuentra informacion sobre tus reservas, proceso de compra, politicas de cancelacion y mas.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
      </svg>
      Volver al inicio
    </button>

    <!-- Grilla de canales de contacto: telefono, correo y direccion de oficina -->
    <div class="info-contact-grid">
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Soporte 24/7</p>
          <p class="info-contact-item__value">+502 2000-0000</p>
          <p class="info-contact-item__sub">Llamadas y WhatsApp</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Email</p>
          <p class="info-contact-item__value">info@broomairline.com</p>
          <p class="info-contact-item__sub">Respuesta en menos de 24 horas</p>
        </div>
      </div>
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Oficina Central</p>
          <p class="info-contact-item__value">Guatemala City, Guatemala</p>
          <p class="info-contact-item__sub">Operaciones desde febrero 2026</p>
        </div>
      </div>
    </div>

    <!-- Tarjetas de temas frecuentes: reservas, pagos, equipaje y cancelaciones -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Temas frecuentes de ayuda</h2>
      <div class="info-cards-grid">

        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21 4 19 4c-1 0-1.5.5-3.5 2.5L8 3.2 6.8 4.4l5.2 4-2.2 2.2-2.8-.8L5.7 11l3.4 1.6L7 15l-1.5-.5L4 16l4 1 1 4 1.5-1.5L10 18l3-2 1.6 3.4 1.2-1.2-.8-2.8 2.2-2.2 4 5.2z"/>
            </svg>
          </span>
          <h3 class="info-card__title">Mis Reservas</h3>
          <p class="info-card__text">Consulta todas tus reservas activas y completadas desde tu perfil. Cada reserva muestra el codigo unico, fecha, origen-destino y estado actual. Puedes descargar tu boleto electronico en PDF desde esta seccion.</p>
        </div>

        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </span>
          <h3 class="info-card__title">Pagos y facturacion</h3>
          <p class="info-card__text">Aceptamos tarjetas de credito y debito. El formulario de pago solicita numero de tarjeta, fecha de vencimiento, CVV y nombre del titular. Cada compra genera un numero de transaccion unico y un correo de confirmacion automatico.</p>
        </div>

        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="7" width="20" height="14" rx="2" ry="2"/>
              <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
            </svg>
          </span>
          <h3 class="info-card__title">Equipaje y asientos</h3>
          <p class="info-card__text">Broom AirLine ofrece asientos Economico y Ejecutivo con precios y caracteristicas diferenciadas. La disponibilidad se muestra por tipo de asiento en tiempo real. Consulta la informacion de check-in y equipaje permitido en la seccion Informacion de Vuelo.</p>
        </div>

        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </span>
          <h3 class="info-card__title">Cancelaciones</h3>
          <p class="info-card__text">Puedes cancelar tu reserva desde la seccion Mis Reservas. Al confirmar la cancelacion, el sistema libera automaticamente los asientos para otros usuarios y envia una confirmacion por correo electronico. Consulta nuestra politica de cancelacion para conocer las condiciones de reembolso.</p>
        </div>

      </div>
    </div>

    <!-- Llamado a la accion para contactar soporte si no se encontro respuesta -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">No encontraste lo que buscabas?</h2>
      <p class="info-prose">Nuestro equipo de soporte esta disponible los 7 dias de la semana, las 24 horas del dia, para ayudarte con cualquier consulta relacionada con vuelos, reservas o tu cuenta.</p>
      <div class="info-highlight">
        Llamanos al <strong>+502 2000-0000</strong> o escribenos a <strong>info@broomairline.com</strong>
      </div>
      <button class="info-contacto-submit" style="margin-top:1rem" on:click={() => navigateTo('contactanos')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
        Ir a Contactanos
      </button>
    </div>
  </div>
</div>
