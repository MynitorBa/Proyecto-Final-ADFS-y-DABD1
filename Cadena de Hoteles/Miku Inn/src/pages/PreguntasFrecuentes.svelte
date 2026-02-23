<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;

  const faqs = [
    {
      q: '¿Cómo realizo una reservación?',
      a: 'Busca tu destino, selecciona las fechas y número de huéspedes, elige la habitación que desees y completa el proceso de pago. Recibirás un correo de confirmación con tu código de reservación (ej. MIKU-42979478).'
    },
    {
      q: '¿Qué tipos de habitación están disponibles?',
      a: 'Ofrecemos cuatro tipos: Doble, Junior Suite, Suite y Gran Suite. Cada tipo tiene capacidad, precio y servicios diferenciados que puedes consultar en la página de cada hotel.'
    },
    {
      q: '¿Puedo cancelar mi reservación?',
      a: 'Sí. Puedes cancelar desde tu perfil en "Mis Reservaciones" hasta 24 horas antes de tu check-in. Recibirás confirmación de cancelación por correo electrónico.'
    },
    {
      q: '¿Cómo encuentro mi código de reservación?',
      a: 'Tu código de reservación es alfanumérico (ej. MIKU-42979478) y se envía al correo con el que te registraste inmediatamente después de confirmar el pago. También lo puedes consultar en "Mis Reservaciones" dentro de tu perfil.'
    },
    {
      q: '¿Qué métodos de pago aceptan?',
      a: 'Aceptamos tarjetas de crédito y débito. El formulario de pago solicita número de tarjeta, fecha de vencimiento, CVV y nombre del titular. Todos los pagos están protegidos con encriptación SSL.'
    },
    {
      q: '¿Puedo reservar para un grupo?',
      a: 'Sí, puedes seleccionar múltiples habitaciones en una sola transacción. Todas las habitaciones del grupo compartirán el mismo código de reservación.'
    },
    {
      q: '¿Dónde veo mis reservaciones anteriores?',
      a: 'En tu perfil, dentro de la sección "Mis Reservaciones", encontrarás todas tus reservaciones categorizadas: activas, completadas y canceladas.'
    },
    {
      q: '¿Cómo dejo una reseña?',
      a: 'Solo los usuarios que hayan completado una estadía pueden dejar comentarios y calificaciones. Puedes hacerlo desde la página del hotel o desde tu historial de reservaciones.'
    },
  ];

  let open = faqs.map(() => false);

  function toggle(i) {
    open[i] = !open[i];
    open = [...open];
  }
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">❓</div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Preguntas Frecuentes</h1>
      <p class="info-hero__subtitle">Respuestas a las dudas más comunes sobre reservaciones, pagos y políticas.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    {#each faqs as faq, i}
      <div class="info-faq">
        <button class="info-faq__q" class:open={open[i]} on:click={() => toggle(i)}>
          {faq.q}
          <svg class="info-faq__chevron" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
        </button>
        {#if open[i]}
          <div class="info-faq__a">{faq.a}</div>
        {/if}
      </div>
    {/each}

    <div class="info-card" style="margin-top:2rem">
      <p class="info-prose" style="margin:0">¿No encontraste tu respuesta? Escríbenos a <strong>info@mikuinn.com</strong> o llámanos al <strong>+502 4276-8687</strong></p>
    </div>
  </div>
</div>