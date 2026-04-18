<script>
  /**
   * @file Contactanos.svelte
   * @description Pagina de contacto de Miku Inn. Muestra la informacion de contacto
   * de la empresa (telefono, email, oficina) y un formulario para que el usuario
   * envie un mensaje directamente al equipo de soporte.
   */

  // @ts-nocheck
  import '../styles/info-pages.css';

  /** Funcion de navegacion inyectada por el router. @type {Function} */
  export let navigateTo;

  /**
   * Datos del formulario de contacto.
   * @type {{ nombre: string, correo: string, asunto: string, mensaje: string }}
   */
  let formData = { nombre: '', correo: '', asunto: '', mensaje: '' };

  /**
   * Estado del envio del formulario.
   * Puede ser: '' (inicial), 'sending', 'success' o 'error'.
   * @type {string}
   */
  let status = '';

  /** Mensaje descriptivo del resultado del envio, ya sea exito o error. @type {string} */
  let statusMsg = '';

  /**
   * Objeto con los errores de validacion por campo.
   * Las claves coinciden con los nombres de los campos del formulario.
   * @type {Record<string, string>}
   */
  let errors = {};

  /**
   * Valida los campos requeridos del formulario antes de enviarlo.
   * Actualiza el objeto `errors` con los mensajes correspondientes.
   * @returns {boolean} True si el formulario es valido, false en caso contrario.
   */
  function validate() {
    errors = {};
    if (!formData.nombre.trim()) errors.nombre = 'Nombre requerido';
    if (!formData.correo.trim()) errors.correo = 'Correo requerido';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) errors.correo = 'Correo inválido';
    if (!formData.mensaje.trim()) errors.mensaje = 'Mensaje requerido';
    else if (formData.mensaje.trim().length < 10) errors.mensaje = 'Mínimo 10 caracteres';
    return Object.keys(errors).length === 0;
  }

  /**
   * Maneja el envio del formulario. Valida primero los datos y luego hace
   * un POST al endpoint /contacto del backend. Actualiza el estado segun la respuesta.
   * @async
   * @returns {Promise<void>}
   */
  async function handleSubmit() {
    if (!validate()) return;
    status = 'sending';
    statusMsg = '';

    try {
      const res = await fetch('http://localhost:7000/contacto', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:  formData.nombre.trim(),
          correo:  formData.correo.trim(),
          asunto:  formData.asunto.trim(),
          mensaje: formData.mensaje.trim()
        })
      });
      const data = await res.json();

      if (res.ok) {
        status = 'success';
        statusMsg = '¡Mensaje enviado correctamente! Te responderemos pronto.';
        formData = { nombre: '', correo: '', asunto: '', mensaje: '' };
      } else {
        status = 'error';
        statusMsg = data.mensaje || 'Error al enviar el mensaje';
      }
    } catch {
      status = 'error';
      statusMsg = 'Error de conexión con el servidor';
    }
  }
</script>

<!-- Pagina completa de contacto -->
<div class="info-page">

  <!-- Hero con icono SVG y titulo de la seccion -->
  <div class="info-hero">
    <div class="info-hero__content">
      <div class="info-hero__icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
          <polyline points="22,6 12,13 2,6"/>
        </svg>
      </div>
      <p class="info-hero__eyebrow">Miku Inn</p>
      <h1 class="info-hero__title">Contáctanos</h1>
      <p class="info-hero__subtitle">¿Tienes alguna duda o comentario? Envíanos un mensaje y te responderemos lo antes posible.</p>
    </div>
  </div>

  <div class="info-container">
    <button class="info-back" on:click={() => navigateTo('home')}>
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
      Volver al inicio
    </button>

    <!-- Grid con los datos de contacto de la empresa -->
    <div class="info-contact-grid">
      <!-- Canal: telefono y whatsapp -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.41 2 2 0 0 1 3.6 1.21h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6 6l.92-.92a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Atención 24/7</p>
          <p class="info-contact-item__value">+502 4276-8687</p>
          <p class="info-contact-item__sub">Llamadas y WhatsApp</p>
        </div>
      </div>

      <!-- Canal: correo electronico -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Email</p>
          <p class="info-contact-item__value">info@mikuinn.com</p>
          <p class="info-contact-item__sub">Respuesta en menos de 24h</p>
        </div>
      </div>

      <!-- Canal: ubicacion de oficina central -->
      <div class="info-contact-item">
        <div class="info-contact-item__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
        </div>
        <div>
          <p class="info-contact-item__label">Oficina Central</p>
          <p class="info-contact-item__value">Guatemala City</p>
          <p class="info-contact-item__sub">Guatemala</p>
        </div>
      </div>
    </div>

    <!-- Mensaje de exito tras enviar el formulario -->
    {#if status === 'success'}
      <div class="info-card">
        <div class="info-contacto-success">
          <!-- Icono SVG de exito -->
          <div class="info-contacto-success__icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <h2 class="info-section-title" style="border:none;text-align:center;margin:0 0 0.5rem;">¡Mensaje Enviado!</h2>
          <p class="info-prose" style="text-align:center;">Hemos recibido tu mensaje. Te responderemos a la brevedad posible.</p>
          <button class="info-back" style="margin:1rem auto 0;display:flex;"
            on:click={() => { status = ''; statusMsg = ''; }}>
            Enviar otro mensaje
          </button>
        </div>
      </div>

    <!-- Formulario de contacto -->
    {:else}
      <div class="info-card">
        <h2 class="info-section-title" style="margin-top:0;">Envíanos un mensaje</h2>

        <form on:submit|preventDefault={handleSubmit} class="info-contacto-form">

          <!-- Fila de nombre y correo -->
          <div class="info-contacto-grid">
            <div class="info-contacto-field">
              <label class="info-contacto-label" for="contactNombre">Nombre completo <span class="info-contacto-req">*</span></label>
              <input type="text" id="contactNombre" class="info-contacto-input" class:info-contacto-input--error={errors.nombre}
                bind:value={formData.nombre} placeholder="Tu nombre" autocomplete="name" />
              {#if errors.nombre}<span class="info-contacto-error">{errors.nombre}</span>{/if}
            </div>
            <div class="info-contacto-field">
              <label class="info-contacto-label" for="contactCorreo">Correo electrónico <span class="info-contacto-req">*</span></label>
              <input type="email" id="contactCorreo" class="info-contacto-input" class:info-contacto-input--error={errors.correo}
                bind:value={formData.correo} placeholder="tu@email.com" autocomplete="email" />
              {#if errors.correo}<span class="info-contacto-error">{errors.correo}</span>{/if}
            </div>
          </div>

          <!-- Campo de asunto (opcional) -->
          <div class="info-contacto-field">
            <label class="info-contacto-label" for="contactAsunto">Asunto</label>
            <input type="text" id="contactAsunto" class="info-contacto-input"
              bind:value={formData.asunto} placeholder="¿Sobre qué nos escribes? (opcional)" autocomplete="off" />
          </div>

          <!-- Area de texto para el mensaje principal -->
          <div class="info-contacto-field">
            <label class="info-contacto-label" for="contactMensaje">Mensaje <span class="info-contacto-req">*</span></label>
            <textarea id="contactMensaje" class="info-contacto-input info-contacto-textarea" class:info-contacto-input--error={errors.mensaje}
              bind:value={formData.mensaje} placeholder="Escribe tu mensaje aquí..." rows="6"></textarea>
            {#if formData.mensaje && !errors.mensaje}
              <span class="info-contacto-helper">{formData.mensaje.length} caracteres</span>
            {/if}
            {#if errors.mensaje}<span class="info-contacto-error">{errors.mensaje}</span>{/if}
          </div>

          <!-- Alerta de error en el envio -->
          {#if status === 'error'}
            <div class="info-highlight" style="border-left-color:#ef4444;background:rgba(239,68,68,.07);color:#ef4444;">
              {statusMsg}
            </div>
          {/if}

          <!-- Boton de envio con spinner mientras procesa -->
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

    <!-- Nota de contacto urgente al pie de la pagina -->
    <div class="info-highlight">
      Para consultas urgentes llámanos al <strong>+502 4276-8687</strong> o escríbenos a <strong>info@mikuinn.com</strong>
    </div>
  </div>
</div>