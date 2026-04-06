<script>
/**
 * @file SobreNosotros.svelte
 * @description Static "About Us" page for Broom AirLine. Presents the company's background
 * as a Guatemalan airline entering the tourism market in 2026, its vision as an integrated
 * operational platform, and six feature cards covering flight management, secure payments,
 * role-based access control, the admin dashboard with metrics, robust security (CAPTCHA, email
 * verification), and REST API integration for travel agencies and hotels. Includes a contact
 * section with a button to navigate to the Contactanos page. Accessible from the main navigation
 * with a back button that returns to the home page.
 */
// @ts-nocheck
  import '../styles/info-pages.css';

  /** Function used to navigate to another page in the application. @type {function} */
  export let navigateTo;
</script>

<!-- Contenedor principal de la pagina Sobre Nosotros -->
<div class="info-page">

  <!-- Seccion hero con icono, nombre de la aerolinea y subtitulo introductorio -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21 4 19 4c-1 0-1.5.5-3.5 2.5L8 3.2 6.8 4.4l5.2 4-2.2 2.2-2.8-.8L5.7 11l3.4 1.6L7 15l-1.5-.5L4 16l4 1 1 4 1.5-1.5L10 18l3-2 1.6 3.4 1.2-1.2-.8-2.8 2.2-2.2 4 5.2z"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Sobre Nosotros</h1>
      <p class="info-hero__subtitle">Conoce la vision, mision y el sistema que respalda las operaciones de Broom AirLine.</p>
    </div>
  </div>

  <div class="info-container">

    <!-- Boton de regreso a la pagina de inicio -->
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
      </svg>
      Volver al inicio
    </button>

    <!-- Descripcion de la empresa: quienes somos y nuestro rol en el mercado aereo guatemalteco -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Quienes somos</h2>
      <p class="info-prose">Broom AirLine es una aerolinea guatemalteca que, tras un proceso de consolidacion interna, ingresa al mercado turistico en 2026 respaldada por un sistema tecnologico que actua como la columna vertebral de su operacion aerea y comercial.</p>
      <p class="info-prose">Nuestro sistema no se limita a un portal de ventas: integra la gestion operativa de vuelos, el control de inventarios de asientos, la administracion de usuarios por roles, la trazabilidad de cada transaccion y la generacion de metricas para la toma de decisiones. A traves de una interfaz web intuitiva, la plataforma permite a la aerolinea operar de manera eficiente mientras ofrece a los pasajeros una experiencia de compra clara y confiable.</p>
    </div>

    <!-- Vision del proyecto: objetivo estrategico de la plataforma a largo plazo -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Vision del proyecto</h2>
      <p class="info-prose">La vision de Broom AirLine es consolidarse como un referente dentro de la industria aerea, apoyandose en un sistema integral que unifique operacion, tecnologia y experiencia de usuario en una sola plataforma.</p>
      <p class="info-prose">El sistema esta disenado para ser el nucleo operativo que permita la planificacion de vuelos, la administracion de inventarios, la atencion al cliente y la integracion con socios comerciales como agencias de viajes y hoteles mediante servicios REST. La plataforma evolucionara conforme crezcan las necesidades del negocio, manteniendo altos estandares de disponibilidad y seguridad.</p>
    </div>

    <!-- Grilla de seis tarjetas de caracteristicas principales de la plataforma -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Lo que ofrece nuestra plataforma</h2>
      <div class="info-cards-grid">

        <!-- Tarjeta: gestion integral de vuelos con disponibilidad en tiempo real -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="2" y1="12" x2="22" y2="12"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </span>
          <h3 class="info-card__title">Gestion integral de vuelos</h3>
          <p class="info-card__text">Planificacion de vuelos con origen, destino, escalas, duracion, terminal, puerta, fecha, hora, precio por tipo de asiento y control de disponibilidad en tiempo real.</p>
        </div>

        <!-- Tarjeta: pagos con cifrado SSL y boleto PDF por correo -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </span>
          <h3 class="info-card__title">Pagos y transacciones seguras</h3>
          <p class="info-card__text">Procesamiento de pagos con tarjeta de credito y debito bajo cifrado SSL. Registro permanente de cada transaccion con fecha, hora, monto y numero unico. Boleto electronico en PDF enviado por correo automaticamente.</p>
        </div>

        <!-- Tarjeta: sistema de roles con tres tipos de usuario y permisos especificos -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </span>
          <h3 class="info-card__title">Roles y control de acceso</h3>
          <p class="info-card__text">Tres tipos de usuario: Cliente Registrado, Administrador y WebService. Cada rol tiene permisos especificos verificados en cada peticion al servidor. Solo administradores acceden al panel de gestion de vuelos, usuarios y metricas.</p>
        </div>

        <!-- Tarjeta: dashboard administrativo con graficos y metricas de ventas -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
            </svg>
          </span>
          <h3 class="info-card__title">Dashboard y metricas</h3>
          <p class="info-card__text">Panel administrativo con graficos visuales de ventas del mes, numero de reservas y vuelos mas vendidos. Filtrado por rango de fechas y actualizacion en tiempo real para la toma de decisiones.</p>
        </div>

        <!-- Tarjeta: seguridad con CAPTCHA, verificacion de correo y compatibilidad Linux -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </span>
          <h3 class="info-card__title">Seguridad robusta</h3>
          <p class="info-card__text">CAPTCHA en formularios de registro y login (despues de 2 intentos fallidos). Verificacion de correo electronico obligatoria para activar la cuenta. Compatibilidad completa con servidores Linux para un despliegue seguro y estable.</p>
        </div>

        <!-- Tarjeta: API REST para agencias de viajes y hoteles con autenticacion por API Key -->
        <div class="info-card" style="margin:0">
          <span class="info-card__icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
          </span>
          <h3 class="info-card__title">Integracion REST</h3>
          <p class="info-card__text">API REST para agencias de viajes y hoteles. GET /api/vuelos para consultar vuelos disponibles y POST /api/reservas para crear reservaciones externas. Respuestas en JSON con autenticacion por API Key.</p>
        </div>

      </div>
    </div>

    <!-- Seccion de contacto con datos de comunicacion y boton hacia la pagina Contactanos -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Contacto</h2>
      <p class="info-prose">Tienes alguna pregunta sobre nuestra empresa o quieres saber mas de nuestros servicios? Estamos aqui para ayudarte.</p>
      <div class="info-highlight">
        Escribenos a <strong>info@broomairline.com</strong> o llamanos al <strong>+502 2000-0000</strong> — disponibles las 24 horas, los 7 dias de la semana.
      </div>
      <button class="info-contacto-submit" style="margin-top:1rem" on:click={() => navigateTo('contactanos')}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
        </svg>
        Contactanos
      </button>
    </div>
  </div>
</div>
