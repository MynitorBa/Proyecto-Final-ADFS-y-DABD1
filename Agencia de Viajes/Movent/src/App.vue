<template>
  <!-- Punto de montaje del router: renderiza la vista activa según la ruta -->
  <router-view />
</template>

<script setup>
/**
 * @file App.vue
 * @description Componente raíz de la aplicación MOVENT. Al montarse, verifica la
 * sesión del usuario con el backend y sincroniza los datos en sessionStorage para
 * que todos los componentes accedan a la información actualizada del usuario.
 */
import { onMounted } from 'vue'

/** URL base del backend. @type {string} */
const API_BASE = 'http://localhost:8080'

/**
 * Al montar la app, consulta el endpoint de sesión para validar la cookie activa.
 * Si la cookie expiró o no existe, limpia los datos de sesión local.
 * Si es válida, mergea los datos del servidor con lo que ya hay guardado
 * para preservar nombre y apellido obtenidos durante el login.
 */
onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE}/api/sesion`, {
      credentials: 'include',
    })

    if (!res.ok) {
      // Cookie expiró o no existe → limpiar sesión local
      sessionStorage.removeItem('usuario_sesion')
      return
    }

    const data = await res.json()
    // data = { usuario_id, username, rol_id }

    // Mergear con lo que ya está guardado (para preservar nombre/apellido del login)
    const sesionActual = JSON.parse(sessionStorage.getItem('usuario_sesion') || '{}')

    sessionStorage.setItem('usuario_sesion', JSON.stringify({
      ...sesionActual,           // preserva nombre, apellido, correo del login
      id:       data.usuario_id,
      username: data.username,   // campo que usa Encabezado como fallback
      rol_id:   data.rol_id,
      isAdmin:  data.rol_id === 2,
      rol:      data.rol_id === 2 ? 'Administrador' : 'Cliente',
    }))

  } catch {
    // Backend no responde — no limpiar sesión, puede ser error de red temporal
  }
})
</script>
