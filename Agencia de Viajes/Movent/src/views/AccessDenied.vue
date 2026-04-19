<template>
  <div class="page">
    <Encabezado />
    <div class="access-denied">
      <div class="access-denied__content">
        <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="80" height="80">
          <circle cx="12" cy="12" r="10"/>
          <line x1="15" y1="9" x2="9" y2="15"/>
          <line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
        <h1 class="access-denied__title">Acceso Denegado</h1>
        <p class="access-denied__message">No tienes permisos de administrador para acceder a esta sección.</p>
        <button class="access-denied__btn" @click="$router.push('/principal')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Volver al inicio
        </button>
      </div>
    </div>
    <Piepagina />
  </div>
</template>

<script setup>
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/access-denied.css'
</script>