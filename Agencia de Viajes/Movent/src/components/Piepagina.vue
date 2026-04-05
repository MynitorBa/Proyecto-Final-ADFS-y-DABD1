<template>
  <footer class="footer">
    <div class="footer-main">
      <div class="footer-container">

        <!-- Sección de marca: logo, descripción y redes sociales -->
        <div class="footer-brand">
          <img src="/movent.png" alt="Movent" class="footer-logo" />
          <p class="footer-description">
            Tu agencia de viajes de confianza. Conectamos viajeros con vuelos,
            hospedajes y paquetes turísticos de los mejores proveedores del mundo.
          </p>
          <div class="footer-social">
            <a href="#" class="social-link" aria-label="Facebook">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>
            </a>
            <a href="#" class="social-link" aria-label="Instagram">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="20" rx="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/></svg>
            </a>
            <a href="#" class="social-link" aria-label="Twitter">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>
            </a>
          </div>
        </div>

        <!-- Columna de navegación general del sitio -->
        <div class="footer-col">
          <h4 class="footer-col-title">Navegación</h4>
          <ul class="footer-links">
            <li><router-link to="/principal" class="footer-link">Inicio</router-link></li>
            <li><router-link to="/reservar" class="footer-link">Reservar</router-link></li>
            <li><router-link to="/mis-reservaciones" class="footer-link">Mis Reservas</router-link></li>
            <li><router-link to="/informacion" class="footer-link">Información</router-link></li>
            <li><router-link to="/sobre-movent" class="footer-link">Sobre Movent</router-link></li>
          </ul>
        </div>

        <!-- Columna de soporte y políticas -->
        <div class="footer-col">
          <h4 class="footer-col-title">Soporte</h4>
          <ul class="footer-links">
            <li><router-link to="/centro-ayuda" class="footer-link">Centro de Ayuda</router-link></li>
            <li><router-link to="/cancelacion" class="footer-link">Política de Cancelación</router-link></li>
            <li><router-link to="/preguntas-frecuentes" class="footer-link">Preguntas Frecuentes</router-link></li>
            <li><router-link to="/contacto" class="footer-link">Contáctanos</router-link></li>
          </ul>
        </div>

        <!-- Columna de información de contacto directa -->
        <div class="footer-col">
          <h4 class="footer-col-title">Contáctanos</h4>
          <ul class="footer-contact">
            <li class="contact-item">
              <div class="contact-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
              </div>
              <div class="contact-info">
                <span class="contact-label">Teléfono</span>
                <span class="contact-value">+502 5754-5388</span>
              </div>
            </li>
            <li class="contact-item">
              <div class="contact-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </div>
              <div class="contact-info">
                <span class="contact-label">Correo</span>
                <span class="contact-value">info@movent.com</span>
              </div>
            </li>
            <li class="contact-item">
              <div class="contact-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </div>
              <div class="contact-info">
                <span class="contact-label">Dirección</span>
                <span class="contact-value">Guatemala City, Guatemala</span>
              </div>
            </li>
          </ul>
        </div>

      </div>
    </div>

    <!-- Barra inferior con copyright y links legales -->
    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <p class="footer-copy">&copy; {{ currentYear }} <strong>Movent</strong>. Todos los derechos reservados.</p>
        <div class="footer-legal">
          <router-link to="/privacidad" class="legal-link">Privacidad</router-link>
          <span class="legal-sep">·</span>
          <router-link to="/terminos" class="legal-link">Términos</router-link>
          <span class="legal-sep">·</span>
          <router-link to="/contacto" class="legal-link">Contacto</router-link>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
/**
 * @file Piepagina.vue
 * @description Componente de pie de página global de MOVENT. Muestra la marca,
 * descripción, redes sociales, columnas de navegación, soporte, información de
 * contacto y la barra inferior con copyright y links legales.
 */
import '../styles/piepagina.css'

/**
 * Año actual calculado dinámicamente para el copyright.
 * @type {number}
 */
const currentYear = new Date().getFullYear()
</script>
