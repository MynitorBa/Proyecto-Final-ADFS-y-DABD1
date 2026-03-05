<template>
  <div class="page">
    <Encabezado />
    <section class="info-hero" style="background-image: url('/agentes.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Centro de Información</h1>
        <p class="info-hero-subtitle">Todo lo que necesitas saber sobre vuelos, hospedajes, paquetes y políticas.</p>
      </div>
    </section>
    <div class="info-wrap">
      <button class="info-back" @click="$router.push('/principal')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver al inicio
      </button>
      <div class="info-nav-grid">
        <router-link to="/sobre-movent" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <h3>Sobre Movent</h3>
          <p>Conoce nuestra misión, visión y servicios.</p>
        </router-link>
        <router-link to="/centro-ayuda" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <h3>Centro de Ayuda</h3>
          <p>Respuestas a los temas más frecuentes.</p>
        </router-link>
        <router-link to="/preguntas-frecuentes" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          </div>
          <h3>Preguntas Frecuentes</h3>
          <p>Las dudas más comunes resueltas.</p>
        </router-link>
        <router-link to="/cancelacion" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
          </div>
          <h3>Cancelación</h3>
          <p>Políticas y proceso de cancelación.</p>
        </router-link>
        <router-link to="/privacidad" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          </div>
          <h3>Privacidad</h3>
          <p>Cómo protegemos tus datos personales.</p>
        </router-link>
        <router-link to="/terminos" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <h3>Términos y Condiciones</h3>
          <p>Condiciones de uso de la plataforma.</p>
        </router-link>
        <router-link to="/contacto" class="info-nav-card">
          <div class="info-nav-icon">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </div>
          <h3>Contáctanos</h3>
          <p>Envíanos un mensaje o llámanos.</p>
        </router-link>
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