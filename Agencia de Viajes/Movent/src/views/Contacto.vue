<template>
  <div class="page">
    <!-- Barra de navegación superior -->
    <Encabezado />

    <!-- Hero con imagen de fondo y presentación de la sección -->
    <section class="info-hero" style="background-image: url('/compañeros.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Contáctanos</h1>
        <p class="info-hero-subtitle">¿Tienes alguna duda? Envíanos un mensaje y te responderemos pronto.</p>
      </div>
    </section>

    <div class="info-wrap">
      <!-- Botón para regresar a la vista anterior -->
      <button class="info-back" @click="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>

      <!-- Datos de contacto: teléfono, email y dirección de oficina -->
      <div class="info-contact-grid" style="margin-bottom:2rem">
        <div class="info-contact-item">
          <div class="info-contact-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
          </div>
          <div>
            <p class="info-contact-label">Atención Lun–Vie</p>
            <p class="info-contact-value">+502 5754-5388</p>
            <p class="info-contact-sub">Llamadas y WhatsApp</p>
          </div>
        </div>
        <div class="info-contact-item">
          <div class="info-contact-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
          </div>
          <div>
            <p class="info-contact-label">Email</p>
            <p class="info-contact-value">info@movent.com</p>
            <p class="info-contact-sub">Respuesta en menos de 24h</p>
          </div>
        </div>
        <div class="info-contact-item">
          <div class="info-contact-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          </div>
          <div>
            <p class="info-contact-label">Oficina Central</p>
            <p class="info-contact-value">Guatemala City</p>
            <p class="info-contact-sub">Guatemala</p>
          </div>
        </div>
      </div>

      <div class="info-section-body">
        <!-- Confirmación visible tras enviar el mensaje con éxito -->
        <div v-if="status === 'success'" class="info-card">
          <div class="contacto-success">
            <div class="contacto-success-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <h2 class="info-section-title" style="border:none;text-align:center;margin:0 0 0.5rem;">Mensaje enviado</h2>
            <p class="info-prose" style="text-align:center;margin:0 0 1.25rem;">Hemos recibido tu mensaje. Te responderemos a la brevedad.</p>
            <button class="info-back" style="margin:0 auto;display:flex;" @click="status = ''">
              Enviar otro mensaje
            </button>
          </div>
        </div>

        <!-- Formulario de contacto, visible mientras no haya éxito -->
        <template v-else>
          <div class="info-card">
            <h2 class="info-section-title">Envíanos un mensaje</h2>

            <!-- Campos de nombre y correo en columnas -->
            <div class="contacto-grid">
              <div class="contacto-field">
                <label class="contacto-label" for="cNombre">Nombre completo <span class="contacto-req">*</span></label>
                <input id="cNombre" type="text" class="contacto-input" :class="{ error: errors.nombre }"
                  v-model="form.nombre" placeholder="Tu nombre" autocomplete="name" />
                <span v-if="errors.nombre" class="contacto-error">{{ errors.nombre }}</span>
              </div>
              <div class="contacto-field">
                <label class="contacto-label" for="cCorreo">Correo electrónico <span class="contacto-req">*</span></label>
                <input id="cCorreo" type="email" class="contacto-input" :class="{ error: errors.correo }"
                  v-model="form.correo" placeholder="tu@email.com" autocomplete="email" />
                <span v-if="errors.correo" class="contacto-error">{{ errors.correo }}</span>
              </div>
            </div>

            <!-- Campo opcional de asunto -->
            <div class="contacto-field">
              <label class="contacto-label" for="cAsunto">Asunto</label>
              <input id="cAsunto" type="text" class="contacto-input"
                v-model="form.asunto" placeholder="¿Sobre qué nos escribes? (opcional)" />
            </div>

            <!-- Textarea del mensaje con contador de caracteres -->
            <div class="contacto-field">
              <label class="contacto-label" for="cMensaje">Mensaje <span class="contacto-req">*</span></label>
              <textarea id="cMensaje" class="contacto-input contacto-textarea" :class="{ error: errors.mensaje }"
                v-model="form.mensaje" placeholder="Escribe tu mensaje aquí..." rows="6"></textarea>
              <span v-if="form.mensaje && !errors.mensaje" class="contacto-helper">{{ form.mensaje.length }} caracteres</span>
              <span v-if="errors.mensaje" class="contacto-error">{{ errors.mensaje }}</span>
            </div>

            <!-- Alerta de error al fallar el envío -->
            <div v-if="status === 'error'" class="info-highlight" style="border-left-color:#ef4444;background:rgba(239,68,68,.07);color:#b91c1c;margin-bottom:1rem;">
              {{ statusMsg }}
            </div>

            <!-- Botón de envío, deshabilitado mientras se procesa la solicitud -->
            <button class="contacto-submit" :disabled="status === 'sending'" @click="enviar">
              <template v-if="status === 'sending'">
                <svg class="contacto-spinner" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                Enviando...
              </template>
              <template v-else>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                Enviar mensaje
              </template>
            </button>
          </div>

          <!-- Nota de contacto alternativo para consultas urgentes -->
          <div class="info-highlight">
            Para consultas urgentes llámanos al <strong>+502 5754-5388</strong> o escríbenos a <strong>info@movent.com</strong>
          </div>
        </template>
      </div>
    </div>

    <!-- Pie de página -->
    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Contacto.vue
 * @description Vista de contacto de Movent. Muestra la información de contacto
 * de la empresa (teléfono, email, ubicación) y un formulario para que el usuario
 * envíe un mensaje directamente al backend. Incluye validación en cliente y
 * manejo de estados de envío, éxito y error.
 */

import { ref, reactive } from 'vue'

/** Componente de encabezado/navegación global. */
import Encabezado from '../components/Encabezado.vue'

/** Componente de pie de página global. */
import Piepagina from '../components/Piepagina.vue'

/** Estilos compartidos de las vistas informativas. */
import '../styles/informacion.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/**
 * Campos del formulario de contacto.
 * @type {{ nombre: string, correo: string, asunto: string, mensaje: string }}
 */
const form = reactive({ nombre: '', correo: '', asunto: '', mensaje: '' })

/**
 * Objeto con los mensajes de error por campo.
 * Se puebla al validar y se limpia en cada intento de envío exitoso.
 * @type {import('vue').Ref<Record<string, string>>}
 */
const errors = ref({})

/**
 * Estado actual del proceso de envío.
 * Valores posibles: '' | 'sending' | 'success' | 'error'
 * @type {import('vue').Ref<string>}
 */
const status = ref('')

/**
 * Mensaje descriptivo que se muestra cuando el envío falla.
 * Viene del backend o es un mensaje de error de red.
 * @type {import('vue').Ref<string>}
 */
const statusMsg = ref('')

/**
 * Valida los campos obligatorios del formulario antes de enviarlo.
 * Actualiza `errors` con los mensajes correspondientes a cada campo inválido.
 * @returns {boolean} true si todos los campos son válidos, false si hay errores.
 */
function validar() {
  const e = {}
  if (!form.nombre.trim()) e.nombre = 'Nombre requerido'
  if (!form.correo.trim()) e.correo = 'Correo requerido'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correo)) e.correo = 'Correo inválido'
  if (!form.mensaje.trim()) e.mensaje = 'Mensaje requerido'
  else if (form.mensaje.trim().length < 10) e.mensaje = 'Mínimo 10 caracteres'
  errors.value = e
  return Object.keys(e).length === 0
}

/**
 * Envía el formulario al endpoint del backend si la validación es exitosa.
 * Maneja los estados de envío (sending), éxito (success) y error (error),
 * y limpia el formulario en caso de respuesta exitosa.
 * @async
 * @returns {Promise<void>}
 */
async function enviar() {
  if (!validar()) return
  status.value = 'sending'
  try {
    const res = await fetch(`${API}/api/contacto`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre:  form.nombre.trim(),
        correo:  form.correo.trim(),
        asunto:  form.asunto.trim(),
        mensaje: form.mensaje.trim()
      })
    })
    const data = await res.json()
    if (res.ok) {
      status.value = 'success'
      Object.assign(form, { nombre: '', correo: '', asunto: '', mensaje: '' })
    } else {
      status.value = 'error'
      statusMsg.value = data.mensaje || 'Error al enviar el mensaje'
    }
  } catch {
    status.value = 'error'
    statusMsg.value = 'Error de conexión con el servidor'
  }
}
</script>
