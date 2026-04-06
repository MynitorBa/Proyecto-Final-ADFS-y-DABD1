<script>
/**
 * @file PreguntasFrecuentes.svelte
 * @description FAQ accordion page for Broom AirLine. Renders a list of ten frequently asked
 * questions covering the reservation flow, seat classes, group purchases, seat blocking,
 * reservation codes, cancellation steps, PDF ticket download, layovers, and payment methods.
 * Each question can be expanded or collapsed individually. A second card provides contact
 * info and a button to navigate to the Contactanos page.
 */
// @ts-nocheck
  import '../styles/info-pages.css';

  /** Function used to navigate to another page in the application. @type {function} */
  export let navigateTo;

  /**
   * Static FAQ data array. Each entry has a question string (q) and an answer string (a).
   * @type {Array<{q: string, a: string}>}
   */
  const faqs = [
    {
      q: 'Como realizo una reserva de vuelo?',
      a: 'Desde la pagina principal ingresa tu origen, destino, fecha de salida y cantidad de pasajeros. El sistema mostrara unicamente los vuelos con asientos disponibles. Selecciona el vuelo, elige el tipo de asiento (Economico o Ejecutivo), ingresa los datos de cada pasajero y completa el pago con tu tarjeta. Al confirmar recibiras un codigo unico de reserva y un correo de confirmacion con tu boleto en PDF.'
    },
    {
      q: 'Puedo buscar vuelos de ida y vuelta?',
      a: 'Si. Al momento de buscar puedes seleccionar la opcion de ida y vuelta para indicar tambien la fecha de regreso. El sistema buscara vuelos en ambas direcciones para que puedas reservarlos en una sola transaccion.'
    },
    {
      q: 'Que tipos de asiento ofrece Broom AirLine?',
      a: 'Ofrecemos dos clases: Economico y Ejecutivo. Cada tipo tiene precio diferente claramente visible al momento de seleccionar el vuelo. El sistema muestra las caracteristicas de cada clase y la disponibilidad en tiempo real antes de confirmar la compra.'
    },
    {
      q: 'Puedo comprar boletos para varios pasajeros a la vez?',
      a: 'Si. Puedes seleccionar la cantidad de pasajeros antes de buscar. Para compras grupales el sistema solicita la informacion individual de cada pasajero. El precio total se calcula multiplicando por el numero de pasajeros y todos los boletos del grupo comparten el mismo codigo de reserva.'
    },
    {
      q: 'Como funciona el bloqueo temporal de asientos?',
      a: 'Al iniciar el proceso de compra, el sistema bloquea temporalmente el asiento seleccionado por 10 minutos. Si no completas la compra en ese tiempo, el asiento se libera automaticamente y queda disponible para otros usuarios. Esto evita que dos personas reserven el mismo asiento al mismo tiempo.'
    },
    {
      q: 'Donde encuentro mi codigo de reserva?',
      a: 'Tu codigo unico de reserva (formato alfanumerico tipo #VGT-2026-A7B9C2) se genera automaticamente al confirmar el pago. Lo recibiras por correo electronico junto con el detalle de tu vuelo y el boleto en PDF. Tambien puedes consultarlo desde tu perfil en la seccion Mis Reservas.'
    },
    {
      q: 'Como cancelo una reserva?',
      a: 'Inicia sesion en tu cuenta, ve a Mis Reservas, selecciona la reserva que deseas cancelar y haz clic en Cancelar reserva. El sistema pedira confirmacion antes de procesar. Al cancelar recibiras un correo de confirmacion y los asientos quedaran disponibles de inmediato para otros usuarios.'
    },
    {
      q: 'Como descargo mi boleto en PDF?',
      a: 'Desde la seccion Mis Reservas de tu perfil, selecciona la reserva y encontraras la opcion de descargar el boleto electronico en formato PDF. El boleto tambien se adjunta automaticamente al correo de confirmacion de compra.'
    },
    {
      q: 'Que pasa si un vuelo tiene escalas?',
      a: 'El sistema indica claramente si un vuelo tiene escalas, mostrando cada ciudad de escala, el tiempo de espera en cada una y la duracion total del viaje. Puedes filtrar tu busqueda por vuelos directos o con escalas segun tu preferencia.'
    },
    {
      q: 'Que metodos de pago aceptan?',
      a: 'Aceptamos tarjetas de credito y debito. El formulario de pago solicita numero de tarjeta, fecha de vencimiento, CVV y nombre del titular. Si el pago falla se muestra un mensaje de error especifico sin guardar la reserva. Todos los pagos se procesan con cifrado SSL.'
    },
  ];

  /** Array of open/closed states for each FAQ item, indexed parallel to faqs. @type {boolean[]} */
  let open = faqs.map(() => false);

  /**
   * Toggles the expanded state of the FAQ item at the given index and triggers Svelte reactivity.
   * @param {number} i - The index of the FAQ item to toggle.
   */
  function toggle(i) { open[i] = !open[i]; open = [...open]; }
</script>

<!-- Contenedor principal de la pagina de preguntas frecuentes -->
<div class="info-page">
  <!-- Hero con icono, titulo y descripcion de la seccion FAQ -->
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
      <h1 class="info-hero__title">Preguntas Frecuentes</h1>
      <p class="info-hero__subtitle">Resuelve tus dudas sobre reservas, pagos, asientos, cancelaciones y mas.</p>
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

    <!-- Acordeon de preguntas y respuestas frecuentes con toggle individual -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">Preguntas frecuentes</h2>
      <div class="info-faq">
        {#each faqs as faq, i}
          <div class="info-faq__item">
            <button class="info-faq__q" class:open={open[i]} on:click={() => toggle(i)}>
              <span>{faq.q}</span>
              <svg class="info-faq__chevron" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
            {#if open[i]}
              <div class="info-faq__a">{faq.a}</div>
            {/if}
          </div>
        {/each}
      </div>
    </div>

    <!-- Tarjeta de contacto para dudas no resueltas con enlace a Contactanos -->
    <div class="info-card">
      <h2 class="info-section-title" style="margin-top:0">No encontraste tu respuesta?</h2>
      <p class="info-prose">Nuestro equipo de soporte esta disponible las 24 horas para atenderte. Tambien puedes consultar nuestro centro de ayuda o escribirnos directamente.</p>
      <div class="info-highlight">
        Escribenos a <strong>info@broomairline.com</strong> o llamanos al <strong>+502 2000-0000</strong>
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
