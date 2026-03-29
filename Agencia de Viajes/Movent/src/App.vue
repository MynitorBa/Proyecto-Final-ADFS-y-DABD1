<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

const API_BASE = 'http://localhost:8080'

onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE}/api/sesion`, {
      credentials: 'include', // necesario para mandar la cookie
    })

    if (!res.ok) {
      sessionStorage.removeItem('usuario_sesion')
      return
    }

    const data = await res.json()
    // data = { usuario_id, username, rol_id }

    // Sincronizar con sessionStorage
    const sesionActual = JSON.parse(sessionStorage.getItem('usuario_sesion') || '{}')
    sessionStorage.setItem('usuario_sesion', JSON.stringify({
      ...sesionActual,
      id:      data.usuario_id,
      usuario: data.username,
      rol:     data.rol_id === 2 ? 'Administrador' : 'Registrado',
      isAdmin: data.rol_id === 2,
    }))

  } catch {
    // Si el backend no responde, no hacer nada
  }
})
</script>