<template>
  <div class="page">
    <Encabezado />

    <!-- Hero con imagen de fondo referente a agentes de viaje -->
    <section class="info-hero" style="background-image: url('/agentes.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Términos y Condiciones</h1>
        <p class="info-hero-subtitle">Lee con atención los términos que rigen el uso de nuestra plataforma.</p>
      </div>
    </section>

    <div class="info-wrap">
      <!-- Botón para volver a la sección de información general -->
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>

      <div class="info-section-body">

        <!-- Condiciones de uso de la plataforma -->
        <div class="info-card">
          <span class="info-badge">Vigente desde febrero 2026</span>
          <h2 class="info-section-title">Uso de la plataforma</h2>
          <p class="info-prose">Al utilizar Movent, aceptas cumplir con los presentes términos. El uso de nuestros servicios está destinado a personas mayores de 18 años.</p>
          <ul class="info-list">
            <li>Debes proporcionar información veraz y completa al registrarte</li>
            <li>Eres responsable de mantener la confidencialidad de tu contraseña</li>
            <li>No puedes usar la plataforma para actividades ilegales o fraudulentas</li>
            <li>Cada usuario puede tener una sola cuenta activa</li>
          </ul>
        </div>

        <!-- Condiciones específicas sobre el proceso de reservación -->
        <div class="info-card">
          <h2 class="info-section-title">Reservaciones</h2>
          <ul class="info-list">
            <li>Las reservaciones se confirman únicamente al completar el pago</li>
            <li>Cada reservación genera un código único (ej. #MVT-2026-X9Y2Z5)</li>
            <li>La disponibilidad se verifica en tiempo real con cada proveedor antes de confirmar</li>
            <li>Los servicios seleccionados se bloquean temporalmente 10 minutos durante el proceso</li>
            <li>Las reservaciones grupales comparten el mismo código de reservación de Movent</li>
          </ul>
        </div>

        <!-- Condiciones de pago y nota de cierre -->
        <div class="info-card">
          <h2 class="info-section-title">Pagos</h2>
          <ul class="info-list">
            <li>Aceptamos tarjetas de crédito y débito</li>
            <li>Si el pago falla, la reservación no se guarda en el sistema</li>
            <li>No almacenamos datos completos de tarjetas de crédito</li>
            <li>Cada transacción genera un número único de referencia</li>
            <li>El voucher PDF se envía automáticamente al correo al confirmar</li>
          </ul>
          <div class="info-highlight">
            Al aceptar estos términos, reconoces haber leído y comprendido todas las condiciones. Para dudas escríbenos a <strong>info@movent.com</strong>
          </div>
        </div>

      </div>
    </div>

    <Piepagina />
  </div>

</template>

<script setup>
/**
 * @file Terminos.vue
 * @description Vista estática que muestra los términos y condiciones de uso de la
 * plataforma Movent: reglas de cuenta, reservaciones y política de pagos.
 */
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/informacion.css'
</script>
