<template>
  <div class="page">
    <Encabezado />
    <section class="info-hero" style="background-image: url('/empleado.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Preguntas Frecuentes</h1>
        <p class="info-hero-subtitle">Respuestas a las dudas más comunes sobre reservaciones, pagos y políticas.</p>
      </div>
    </section>
    <div class="info-wrap">
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>
      <div class="info-section-body">
        <div v-for="(faq, i) in faqs" :key="i" class="info-faq">
          <button class="info-faq-q" :class="{ open: abierto[i] }" @click="toggle(i)">
            {{ faq.q }}
            <svg class="info-faq-chevron" :style="{ transform: abierto[i] ? 'rotate(180deg)' : 'rotate(0deg)' }"
              width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>
          <div v-if="abierto[i]" class="info-faq-a">{{ faq.a }}</div>
        </div>
        <div class="info-card" style="margin-top:1rem">
          <p class="info-prose" style="margin:0">
            ¿No encontraste tu respuesta? Escríbenos a <strong>info@movent.com</strong> o llámanos al <strong>+502 5754-5388</strong>
          </p>
        </div>
      </div>
    </div>
    <Piepagina />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/informacion.css'

const faqs = [
  { q: '¿Cómo realizo una reservación?', a: 'Busca tu vuelo u hotel con los filtros disponibles (origen, destino, fechas, pasajeros, clase). Selecciona la opción deseada, revisa el itinerario completo con precio total y completa el proceso de pago. Recibirás un voucher PDF con tu código único #MVT-2026-XXXXXX.' },
  { q: '¿Qué tipos de asiento están disponibles?', a: 'Ofrecemos tres clases: Económica, Ejecutiva y Primera Clase. Cada una tiene precio diferenciado con características claramente visibles antes de reservar.' },
  { q: '¿Qué tipos de habitación están disponibles?', a: 'Ofrecemos cuatro tipos: Doble, Junior Suite, Suite y Gran Suite. Cada tipo muestra capacidad, tamaño de cama, metros cuadrados y servicios incluidos.' },
  { q: '¿Puedo reservar vuelo y hotel juntos?', a: 'Sí. En la búsqueda principal selecciona la pestaña "Vuelo + Hotel" para buscar paquetes combinados. También puedes crear tu propio paquete combinando un vuelo y un hospedaje en una sola transacción con código compartido.' },
  { q: '¿Cómo encuentro mi código de reservación?', a: 'Tu código es alfanumérico (ej. #MVT-2026-X9Y2Z5) y se envía al correo registrado inmediatamente después de confirmar el pago. También lo encuentras en "Mis Reservaciones" en tu perfil.' },
  { q: '¿Qué métodos de pago aceptan?', a: 'Aceptamos tarjetas de crédito y débito. El formulario solicita número de tarjeta, fecha de vencimiento, CVV y nombre del titular. Todos los pagos están protegidos con encriptación SSL.' },
  { q: '¿Puedo reservar para un grupo?', a: 'Sí. Selecciona la cantidad de pasajeros/huéspedes antes de buscar. Para reservas grupales se solicita información de cada persona. Todos los servicios del grupo comparten el mismo código de reservación de Movent.' },
  { q: '¿Dónde veo mis reservaciones anteriores?', a: 'En tu perfil, sección "Mis Reservaciones", encontrarás todas tus reservaciones categorizadas: activas, completadas y canceladas. Desde ahí también puedes descargar el voucher PDF.' },
]

const abierto = ref(faqs.map(() => false))
const toggle = (i) => { abierto.value[i] = !abierto.value[i] }
</script>