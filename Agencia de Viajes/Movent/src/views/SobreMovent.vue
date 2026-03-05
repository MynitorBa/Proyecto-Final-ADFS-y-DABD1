<template>
  <div class="page">
    <Encabezado />
    <section class="info-hero" style="background-image: url('/compañeros.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Sobre Nosotros</h1>
        <p class="info-hero-subtitle">Conoce quiénes somos, nuestra misión y los servicios que ofrecemos.</p>
      </div>
    </section>
    <div class="info-wrap">
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>
      <div class="info-section-body">
        <div class="info-card">
          <h2 class="info-section-title">¿Quiénes somos?</h2>
          <p class="info-prose">Movent es una agencia de viajes guatemalteca fundada para ser la plataforma tecnológica de referencia en 2026. Integramos vuelos y hospedajes de múltiples proveedores mediante APIs REST, presentando opciones consolidadas y ordenadas por precio para que encuentres siempre la mejor tarifa.</p>
          <p class="info-prose">Nuestra misión es que los usuarios comparen opciones, reserven servicios individuales o paquetes combinados de vuelo con hospedaje, y administren sus viajes con seguimiento completo de cada operación, todo desde un solo lugar.</p>
        </div>
        <div class="info-card">
          <h2 class="info-section-title">Nuestra visión</h2>
          <div class="info-highlight">
            Establecer a Movent como proveedor líder de servicios de viajes mediante una plataforma que integre tecnología, operaciones y experiencia del cliente en un solo sistema conectado a los mejores proveedores del mundo.
          </div>
        </div>
        <div class="info-cards-grid">
          <div class="info-mini-card">
            <div class="info-mini-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            </div>
            <h3>Vuelos Globales</h3>
            <p>Múltiples aerolíneas consolidadas en una sola búsqueda con tarifas comparativas en tiempo real.</p>
          </div>
          <div class="info-mini-card">
            <div class="info-mini-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            </div>
            <h3>Hospedaje Premium</h3>
            <p>Hoteles de múltiples proveedores: Doble, Junior Suite, Suite y Gran Suite con precios diferenciados.</p>
          </div>
          <div class="info-mini-card">
            <div class="info-mini-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            </div>
            <h3>Paquetes Combinados</h3>
            <p>Vuelo + hospedaje en una sola transacción con precio especial y código único compartido.</p>
          </div>
          <div class="info-mini-card">
            <div class="info-mini-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            </div>
            <h3>Pagos Seguros</h3>
            <p>Procesamiento con encriptación SSL, confirmación inmediata y voucher PDF por correo.</p>
          </div>
        </div>
      </div>
    </div>
    <Piepagina />
  </div>
</template>

<script setup>
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/informacion.css'
</script>