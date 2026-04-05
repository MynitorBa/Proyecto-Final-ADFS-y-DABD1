<template>
  <div class="page">
    <Encabezado />

    <!-- Hero de la vista con imagen de fondo -->
    <section class="info-hero" style="background-image: url('/cancelacion.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Política de Cancelación</h1>
        <p class="info-hero-subtitle">Condiciones y proceso para cancelar tus reservaciones.</p>
      </div>
    </section>

    <div class="info-wrap">
      <!-- Botón para volver a la página de información general -->
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>

      <div class="info-section-body">

        <!-- Tarjeta: política de cancelación por parte del cliente -->
        <div class="info-card">
          <span class="info-badge">Política vigente 2026</span>
          <h2 class="info-section-title">Cancelación de parte del cliente</h2>
          <p class="info-prose">Los clientes pueden cancelar sus reservaciones directamente desde su perfil en "Mis Reservaciones". El sistema consulta las políticas de cancelación de cada proveedor antes de permitir la operación.</p>
          <ul class="info-list">
            <li>La cancelación debe confirmarse por el usuario antes de procesarse</li>
            <li>El sistema muestra las penalizaciones aplicables según el proveedor</li>
            <li>Recibirás una confirmación por correo electrónico inmediatamente</li>
            <li>El historial de cancelaciones se conserva permanentemente en tu perfil</li>
            <li>Para paquetes combinados, se cancela tanto el vuelo como el hospedaje en la misma operación</li>
          </ul>
          <!-- Nota sobre casos especiales y contacto de soporte -->
          <div class="info-highlight">
            Cancelaciones dentro de las políticas del proveedor se procesan automáticamente. Para casos especiales contacta soporte al <strong>+502 5754-5388</strong>.
          </div>
        </div>

        <!-- Tarjeta: guía paso a paso del proceso de cancelación -->
        <div class="info-card">
          <h2 class="info-section-title">Proceso paso a paso</h2>
          <ul class="info-list">
            <li>Ingresa a tu cuenta y ve a "Mis Reservaciones"</li>
            <li>Selecciona la reservación activa que deseas cancelar</li>
            <li>Haz clic en "Cancelar reserva" y revisa las penalizaciones</li>
            <li>Confirma la cancelación en el diálogo de verificación</li>
            <li>Recibirás el correo de confirmación en minutos</li>
          </ul>
        </div>

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Cancelacion.vue
 * @description Vista estática que explica la política de cancelación de Movent:
 * condiciones para cancelar reservas, penalizaciones por proveedor y el proceso
 * paso a paso que debe seguir el usuario desde "Mis Reservaciones".
 */
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/informacion.css'
</script>
