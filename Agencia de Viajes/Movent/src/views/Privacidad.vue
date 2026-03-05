<template>
  <div class="page">
    <Encabezado />
    <section class="info-hero" style="background-image: url('/empleado.png')">
      <div class="info-hero-overlay"></div>
      <div class="info-hero-content">
        <div class="info-hero-icon">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
        </div>
        <p class="info-eyebrow">Movent</p>
        <h1 class="info-hero-title">Política de Privacidad</h1>
        <p class="info-hero-subtitle">Cómo recopilamos, usamos y protegemos tus datos personales.</p>
      </div>
    </section>
    <div class="info-wrap">
      <button class="info-back" @click="$router.push('/informacion')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
        Volver a Información
      </button>
      <div class="info-section-body">
        <div class="info-card">
          <span class="info-badge">Vigente desde febrero 2026</span>
          <h2 class="info-section-title">Datos que recopilamos</h2>
          <p class="info-prose">Para brindarte nuestros servicios de reservación de vuelos, hospedajes y paquetes, recopilamos los siguientes datos:</p>
          <ul class="info-list">
            <li>Nombre completo, correo electrónico y número de teléfono</li>
            <li>Número de pasaporte y fecha de nacimiento</li>
            <li>País y ciudad de residencia</li>
            <li>Información de pago (procesada de forma segura, no almacenamos datos de tarjetas)</li>
            <li>Historial de reservaciones, búsquedas y cancelaciones realizadas</li>
          </ul>
        </div>
        <div class="info-card">
          <h2 class="info-section-title">Seguridad de tus datos</h2>
          <ul class="info-list">
            <li>Encriptación SSL en todas las comunicaciones</li>
            <li>Contraseñas encriptadas, nunca almacenadas en texto plano</li>
            <li>Acceso a datos restringido según rol (Cliente, Administrador, Webservice)</li>
            <li>Auditorías de seguridad externas periódicas</li>
            <li>CAPTCHA en formularios críticos para prevenir accesos automatizados</li>
          </ul>
          <div class="info-highlight">
            No compartimos tu información personal con terceros sin tu consentimiento, salvo cuando sea requerido por ley o necesario para procesar tu reservación con el proveedor.
          </div>
        </div>
        <div class="info-card">
          <h2 class="info-section-title">Tus derechos</h2>
          <ul class="info-list">
            <li>Acceder a los datos personales que tenemos sobre ti</li>
            <li>Solicitar la corrección de datos incorrectos</li>
            <li>Solicitar la eliminación de tu cuenta y datos asociados</li>
            <li>Revocar el consentimiento para comunicaciones de marketing</li>
          </ul>
          <p class="info-prose">Para ejercer cualquiera de estos derechos contáctanos en <strong>info@movent.com</strong></p>
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