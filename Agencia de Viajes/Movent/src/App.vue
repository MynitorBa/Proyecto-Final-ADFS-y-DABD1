<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

const API_BASE = 'http://localhost:8080'

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