<script>
  // @ts-nocheck
  import '../styles/info-pages.css';
  export let navigateTo;

  const API = 'https://localhost:7107';
  let formData = { nombre: '', correo: '', asunto: '', mensaje: '' };
  let status = '';
  let statusMsg = '';
  let errors = {};

  function validate() {
    errors = {};
    if (!formData.nombre.trim()) errors.nombre = 'Nombre requerido';
    if (!formData.correo.trim()) errors.correo = 'Correo requerido';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) errors.correo = 'Correo invalido';
    if (!formData.mensaje.trim()) errors.mensaje = 'Mensaje requerido';
    else if (formData.mensaje.trim().length < 10) errors.mensaje = 'Minimo 10 caracteres';
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit() {
    if (!validate()) return;
    status = 'sending'; statusMsg = '';
    try {
      const res = await fetch(`${API}/api/contacto`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:  formData.nombre.trim(),
          correo:  formData.correo.trim(),
          asunto:  formData.asunto.trim(),
          mensaje: formData.mensaje.trim()
        })
      });
      const data = await res.json().catch(() => ({}));
      if (res.ok) {
        status = 'success';
        formData = { nombre: '', correo: '', asunto: '', mensaje: '' };
      } else {
        status = 'error';
        statusMsg = data.mensaje || 'Error al enviar el mensaje';
      }
    } catch {
      status = 'error';
      statusMsg = 'Error de conexion con el servidor';
    }
  }
</script>

<div class="info-page">
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
          <polyline points="22,6 12,13 2,6"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Broom AirLine</p>
      <h1 class="info-hero__title">Contactanos</h1>
      <p class="info-hero__subtitle">Nuestro equipo de soporte esta disponible para ayudarte con tus vuelos, reservas y cualquier consulta sobre nuestros servicios.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
      </svg>
      Volver al inicio
    </button>

    <div class="info-contact-grid">
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Soporte al cliente</p>
          <p class="info-contact-item__value">+502 2000-0000</p>
          <p class="info-contact-item__sub">Atencion 24/7 — Llamadas y WhatsApp</p>
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
          <p class="info-contact-item__label">Correo electronico</p>
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

    {#if status === 'success'}
      <div class="info-card">
        <div class="info-contacto-success">
          <div class="info-contacto-success__icon">
            <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="#8B6B4A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <h2 class="info-section-title" style="border:none;text-align:center;margin:0 0 0.5rem;">Mensaje enviado</h2>
          <p class="info-prose" style="text-align:center;">Hemos recibido tu mensaje. Te responderemos a la brevedad con informacion de contacto para soporte.</p>
          <button class="info-back" style="margin:1rem auto 0;display:flex;" on:click={() => { status = ''; }}>
            Enviar otro mensaje
          </button>
        </div>
      </div>
    {:else}
      <div class="info-card">
        <h2 class="info-section-title" style="margin-top:0;">Envianos un mensaje</h2>
        <p class="info-prose">Completa el formulario y nuestro equipo te respondera con los detalles de tu consulta. Para asuntos urgentes llamanos directamente al +502 2000-0000.</p>

        <form on:submit|preventDefault={handleSubmit} class="info-contacto-form">
          <div class="info-contacto-grid">
            <div class="info-contacto-field">
              <label class="info-contacto-label" for="cNombre">Nombre completo <span class="info-contacto-req">*</span></label>
              <input type="text" id="cNombre" class="info-contacto-input" class:info-contacto-input--error={errors.nombre}
                bind:value={formData.nombre} placeholder="Tu nombre" autocomplete="name" />
              {#if errors.nombre}<span class="info-contacto-error">{errors.nombre}</span>{/if}
            </div>
            <div class="info-contacto-field">
              <label class="info-contacto-label" for="cCorreo">Correo electronico <span class="info-contacto-req">*</span></label>
              <input type="email" id="cCorreo" class="info-contacto-input" class:info-contacto-input--error={errors.correo}
                bind:value={formData.correo} placeholder="tu@email.com" autocomplete="email" />
              {#if errors.correo}<span class="info-contacto-error">{errors.correo}</span>{/if}
            </div>
          </div>

          <div class="info-contacto-field">
            <label class="info-contacto-label" for="cAsunto">Asunto</label>
            <input type="text" id="cAsunto" class="info-contacto-input"
              bind:value={formData.asunto} placeholder="Sobre que nos escribes? (opcional)" />
          </div>

          <div class="info-contacto-field">
            <label class="info-contacto-label" for="cMensaje">Mensaje <span class="info-contacto-req">*</span></label>
            <textarea id="cMensaje" class="info-contacto-input info-contacto-textarea" class:info-contacto-input--error={errors.mensaje}
              bind:value={formData.mensaje} placeholder="Escribe tu consulta aqui..." rows="6"></textarea>
            {#if formData.mensaje && !errors.mensaje}<span class="info-contacto-helper">{formData.mensaje.length} / 500 caracteres</span>{/if}
            {#if errors.mensaje}<span class="info-contacto-error">{errors.mensaje}</span>{/if}
          </div>

          {#if status === 'error'}
            <div class="info-highlight" style="border-left-color:#ef4444;background:rgba(239,68,68,.07);color:#ef4444;">
              {statusMsg}
            </div>
          {/if}

          <button type="submit" class="info-contacto-submit" disabled={status === 'sending'}>
            {#if status === 'sending'}
              <svg class="info-contacto-spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Enviando...
            {:else}
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              Enviar Mensaje
            {/if}
          </button>
        </form>
      </div>
    {/if}

    <div class="info-highlight">
      Para consultas urgentes sobre reservas o vuelos, llamanos al <strong>+502 2000-0000</strong> disponible las 24 horas, los 7 dias de la semana.
    </div>
  </div>
</div>