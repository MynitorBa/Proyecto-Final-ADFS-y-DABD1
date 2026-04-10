<script>
/**
 * @file PoliticaCancelacion.svelte
 * @description Pagina informativa estatica que describe la politica de cancelacion de reservas
 * de Broom AirLine. Cubre cancelaciones iniciadas por el cliente (pasos y condiciones),
 * cancelaciones iniciadas por la aerolinea con notificacion automatica al pasajero, y una guia
 * numerada paso a paso para cancelar una reserva desde la seccion Mis Reservas. Accesible desde
 * la navegacion principal con un boton de regreso al inicio.
 */
// @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion utilizada para navegar a otra pagina en la aplicacion. @type {function} */
  export let navigateTo;
</script>

<!-- Contenedor principal de la pagina de politica de cancelacion -->
<div class="info-page">
  <!-- Hero con icono, titulo y descripcion general de la politica -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
          <path d="M10 11v6"/><path d="M14 11v6"/>
          <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Politica de Cancelacion</h1>
      <p class="info-hero__subtitle">Conoce las condiciones, pasos y efectos de cancelar una reserva de vuelo en nuestra plataforma.</p>
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

    <!-- Tarjeta de contenido con las tres secciones de la politica -->
    <div class="info-card">
      <span class="info-badge">Politica vigente 2026</span>

      <!-- Politica de cancelacion iniciada por el cliente con condiciones y efectos -->
      <h2 class="info-section-title">Cancelacion por parte del cliente</h2>
      <p class="info-prose">Como cliente registrado puedes cancelar tus reservas activas directamente desde tu cuenta. El sistema mantiene un registro historico de todas las cancelaciones realizadas con fecha, codigo de reserva y motivo opcional.</p>
      <ul class="info-list">
        <li>Solo puedes cancelar reservas con estado activo desde la seccion Mis Reservas</li>
        <li>El sistema solicitara confirmacion antes de procesar la cancelacion</li>
        <li>Al confirmar, recibiras un correo electronico con la confirmacion de la cancelacion</li>
        <li>Los asientos cancelados se liberan automaticamente de inmediato y quedan disponibles para otros usuarios</li>
        <li>El historial de cancelaciones se mantiene incluso despues de que la fecha del vuelo haya pasado</li>
        <li>Las condiciones de reembolso dependen del tipo de tarifa adquirida al momento de la compra</li>
      </ul>

      <!-- Politica de cancelacion de vuelos iniciada por la aerolinea -->
      <h2 class="info-section-title">Cancelacion de vuelos por parte de Broom AirLine</h2>
      <p class="info-prose">En casos operativos o de fuerza mayor, Broom AirLine puede cancelar vuelos completos. En ese escenario:</p>
      <ul class="info-list">
        <li>El administrador selecciona el vuelo y lo marca como Cancelado indicando el motivo</li>
        <li>El sistema envia automaticamente un correo de notificacion a todos los pasajeros con reservas en ese vuelo</li>
        <li>Todas las reservas afectadas cambian su estado a Vuelo Cancelado en el historial de cada usuario</li>
        <li>Los asientos se liberan inmediatamente del inventario</li>
      </ul>

      <!-- Guia paso a paso para que el usuario cancele su reserva -->
      <h2 class="info-section-title">Como cancelar tu reserva paso a paso</h2>
      <div class="info-steps">
        <div class="info-step">
          <div class="info-step__num">1</div>
          <div>
            <strong>Inicia sesion en tu cuenta</strong>
            <p>Accede con tu correo y contrasena desde el formulario de login.</p>
          </div>
        </div>
        <div class="info-step">
          <div class="info-step__num">2</div>
          <div>
            <strong>Ve a Mis Reservas</strong>
            <p>Accede a la seccion Mis Reservas desde tu perfil para ver todas tus reservas activas.</p>
          </div>
        </div>
        <div class="info-step">
          <div class="info-step__num">3</div>
          <div>
            <strong>Selecciona la reserva</strong>
            <p>Encuentra la reserva por codigo unico, fecha o destino y haz clic en los detalles.</p>
          </div>
        </div>
        <div class="info-step">
          <div class="info-step__num">4</div>
          <div>
            <strong>Haz clic en Cancelar reserva</strong>
            <p>Dentro del detalle de la reserva encontraras el boton de cancelacion.</p>
          </div>
        </div>
        <div class="info-step">
          <div class="info-step__num">5</div>
          <div>
            <strong>Confirma la cancelacion</strong>
            <p>El sistema te pedira una confirmacion final. Al aceptar, recibiras el correo de confirmacion y los asientos quedaran disponibles de inmediato.</p>
          </div>
        </div>
      </div>

      <!-- Bloque de contacto para asistencia en cancelaciones especiales -->
      <div class="info-highlight">
        Para casos especiales o si necesitas asistencia con la cancelacion, contactanos en <strong>info@broomairline.com</strong> o al <strong>+502 2000-0000</strong> disponible 24/7.
      </div>
    </div>
  </div>
</div>
