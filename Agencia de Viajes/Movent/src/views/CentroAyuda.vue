<template>
  <div class="page">
    <Encabezado />

    <!-- Hero de la vista con imagen representativa del equipo de ayuda -->
    <section class="info-hero" style="background-image: url('/empleada.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Centro de Ayuda</h1>
        <p class="info-hero-subtitle">Encuentra respuestas rápidas sobre reservaciones, pagos y más.</p>
      </div>
    </section>

    <div class="info-wrap">
      <!-- Botón para volver a la página de información general -->
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>

      <div class="info-section-body">

        <!-- Tarjeta de temas frecuentes con mini-cards por categoría -->
        <div class="info-card">
          <h2 class="info-section-title">Temas frecuentes</h2>
          <div class="info-cards-grid">

            <!-- Mini-card: gestión de reservaciones -->
            <div class="info-mini-card">
              <div class="info-mini-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </div>
              <h3>Mis Reservaciones</h3>
              <p>Consulta, modifica o cancela desde tu perfil usando tu código único #MVT-2026-XXXXXX.</p>
            </div>

            <!-- Mini-card: métodos de pago y comprobantes -->
            <div class="info-mini-card">
              <div class="info-mini-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>
              </div>
              <h3>Pagos y Facturas</h3>
              <p>Aceptamos tarjetas de crédito y débito. Tu voucher PDF se genera al confirmar el pago.</p>
            </div>

            <!-- Mini-card: información sobre escalas y tiempos de vuelo -->
            <div class="info-mini-card">
              <div class="info-mini-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </div>
              <h3>Escalas y Tiempos</h3>
              <p>Mostramos vuelos directos y con escalas. Cada tramo incluye aeropuerto y tiempo de conexión.</p>
            </div>

            <!-- Mini-card: política de cancelaciones -->
            <div class="info-mini-card">
              <div class="info-mini-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
              </div>
              <h3>Cancelaciones</h3>
              <p>Cancela siguiendo las políticas de cada proveedor desde "Mis Reservaciones".</p>
            </div>

          </div>
        </div>

        <!-- Tarjeta con información de contacto directo -->
        <div class="info-card">
          <h2 class="info-section-title">¿No encontraste lo que buscabas?</h2>
          <p class="info-prose">Nuestro equipo está disponible de lunes a viernes de 8:00 a 18:00.</p>
          <!-- Datos de contacto: teléfono y correo -->
          <div class="info-highlight">
            Llámanos al <strong>+502 5754-5388</strong> o escríbenos a <strong>info@movent.com</strong>
          </div>
        </div>

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file CentroAyuda.vue
 * @description Vista estática del Centro de Ayuda de Movent. Muestra mini-cards
 * con los temas de soporte más frecuentes (reservaciones, pagos, escalas,
 * cancelaciones) y los datos de contacto del equipo de atención al cliente.
 */
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/informacion.css'
</script>
