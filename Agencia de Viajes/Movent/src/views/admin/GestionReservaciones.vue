<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- Sidebar -->
        <aside class="adm-sidebar">
          <div class="adm-sidebar__head">
            <div class="adm-sidebar__logo">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="20" height="20"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            </div>
            <div>
              <p class="adm-sidebar__titulo">Panel Admin</p>
              <p class="adm-sidebar__rol">Administrador</p>
            </div>
          </div>
          <nav class="adm-nav">
            <router-link to="/admin/dashboard" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
              Dashboard
            </router-link>
            <router-link to="/admin/reservaciones" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Reservaciones
            </router-link>
            <router-link to="/admin/proveedores" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
              Proveedores
            </router-link>
            <router-link to="/admin/paquetes" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              Finanzas
            </router-link>
            <router-link to="/admin/roles" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Roles
            </router-link>
            <router-link to="/admin/metricas" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              Métricas
            </router-link>
          </nav>
        </aside>

        <!-- Main Area -->
        <div class="adm-main">
          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Gestión de Reservaciones</h1>
              <p class="adm-topbar__sub">Todas las reservaciones del sistema</p>
            </div>
            <div class="adm-topbar__fecha">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ fechaHoy }}
            </div>
          </div>

          <!-- Filtros Rápidos -->
          <div style="display: flex; gap: 0.5rem; margin-bottom: 1.5rem; flex-wrap: wrap;">
            <button @click="limpiarFiltros" style="padding: 0.5rem 1rem; border: 1.5px solid #e5e0d8; background: #fff; border-radius: 20px; font-size: 0.8rem; font-weight: 600; cursor: pointer;" :style="{ background: !filtros.estado ? '#FFCC00' : '#fff', color: !filtros.estado ? '#1C1A18' : '#666' }">Todas</button>
            <button @click="filtros.estado = '1'; aplicarFiltros()" style="padding: 0.5rem 1rem; border: 1.5px solid #e5e0d8; background: #fff; border-radius: 20px; font-size: 0.8rem; font-weight: 600; cursor: pointer;" :style="{ background: filtros.estado === '1' ? '#FFCC00' : '#fff', color: filtros.estado === '1' ? '#1C1A18' : '#666' }">Pendientes</button>
            <button @click="filtros.estado = '2'; aplicarFiltros()" style="padding: 0.5rem 1rem; border: 1.5px solid #e5e0d8; background: #fff; border-radius: 20px; font-size: 0.8rem; font-weight: 600; cursor: pointer;" :style="{ background: filtros.estado === '2' ? '#FFCC00' : '#fff', color: filtros.estado === '2' ? '#1C1A18' : '#666' }">Confirmadas</button>
            <button @click="filtros.estado = '3'; aplicarFiltros()" style="padding: 0.5rem 1rem; border: 1.5px solid #e5e0d8; background: #fff; border-radius: 20px; font-size: 0.8rem; font-weight: 600; cursor: pointer;" :style="{ background: filtros.estado === '3' ? '#FFCC00' : '#fff', color: filtros.estado === '3' ? '#1C1A18' : '#666' }">Canceladas</button>
          </div>

          <!-- Filtros Avanzados -->
          <div class="adm-filtros">
            <div class="adm-filtros__grupo">
              <label>Búsqueda</label>
              <input v-model="busqueda" type="text" placeholder="Buscar por código..." />
            </div>
            <div class="adm-filtros__grupo">
              <label>Desde</label>
              <input v-model="filtros.fechaInicio" type="date" />
            </div>
            <div class="adm-filtros__grupo">
              <label>Hasta</label>
              <input v-model="filtros.fechaFin" type="date" />
            </div>
            <div class="adm-filtros__grupo">
              <label>Tipo</label>
              <select v-model="filtros.tipo">
                <option value="">Todos</option>
                <option value="1">Vuelo</option>
                <option value="2">Hotel</option>
                <option value="3">Paquete</option>
              </select>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="cargando" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando reservaciones...</p>
          </div>

          <!-- Tabla -->
          <template v-else>
            <div style="overflow-x: auto; border-radius: 10px; border: 1px solid #f0ede7;">
              <table style="width: 100%; border-collapse: collapse; background: #fff;">
                <thead style="background: #f9f7f3; border-bottom: 2px solid #f0ede7;">
                  <tr>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Código</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Usuario</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Email</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Tipo</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Total</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Fecha</th>
                    <th style="padding: 1rem; text-align: left; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Estado</th>
                    <th style="padding: 1rem; text-align: center; font-size: 0.8rem; font-weight: 700; text-transform: uppercase;">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="reservacionesFiltradas.length === 0" style="border-bottom: 1px solid #f5f2ec;">
                    <td colspan="8" style="padding: 2rem; text-align: center; color: #999;">No hay reservaciones</td>
                  </tr>
                  <tr v-for="r in reservacionesFiltradas" :key="r.id" style="border-bottom: 1px solid #f5f2ec; transition: background 0.18s;" @mouseenter="$event.target.parentElement.style.background = '#fafaf8'" @mouseleave="$event.target.parentElement.style.background = 'transparent'">
                    <td style="padding: 1rem; font-weight: 600; font-family: 'Courier New', monospace;">{{ r.noReservacion }}</td>
                    <td style="padding: 1rem;">{{ r.nombreUsuario }} {{ r.apellidoUsuario }}</td>
                    <td style="padding: 1rem; font-size: 0.85rem; color: #7a7067;">{{ r.correoUsuario }}</td>
                    <td style="padding: 1rem;">
                      <span :style="{ display: 'inline-block', padding: '0.35rem 0.75rem', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600', backgroundColor: r.tipoReserva === 1 ? '#fef3c7' : r.tipoReserva === 2 ? '#c7d2fe' : '#e9d5ff', color: r.tipoReserva === 1 ? '#b45309' : r.tipoReserva === 2 ? '#3730a3' : '#6b21a8' }">
                        {{ r.tipoNombre }}
                      </span>
                    </td>
                    <td style="padding: 1rem; text-align: right; font-weight: 600;">${{ r.total.toFixed(2) }}</td>
                    <td style="padding: 1rem; font-size: 0.85rem; color: #7a7067;">{{ formatFecha(r.fechaCreacion) }}</td>
                    <td style="padding: 1rem;">
                      <span :style="{ display: 'inline-block', padding: '0.35rem 0.75rem', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600', backgroundColor: estadoBg(r.estadoId), color: estadoColor(r.estadoId) }">
                        {{ r.estadoNombre }}
                      </span>
                    </td>
                    <td style="padding: 1rem; text-align: center;">
                      <button v-if="[1, 2].includes(r.estadoId)" @click="abrirCancelacion(r)" style="background: #D40511; color: white; border: none; padding: 0.4rem 0.8rem; border-radius: 6px; font-size: 0.75rem; font-weight: 600; cursor: pointer;">
                        Cancelar
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p style="margin-top: 1rem; padding: 1rem; background: #f9f7f3; border-radius: 8px;">Total: <strong>{{ reservacionesFiltradas.length }}</strong> reservaciones</p>
          </template>

        </div>
      </div>
    </div>

    <!-- Modal Cancelación -->
    <Transition name="fade">
      <div v-if="modalCancelacion" style="position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 2000;" @click.self="cerrarCancelacion">
        <div style="background: white; border-radius: 12px; max-width: 500px; width: 90%; box-shadow: 0 10px 40px rgba(0,0,0,0.2); padding: 1.5rem;">
          <h2 style="margin: 0 0 1rem;">Cancelar Reservación</h2>
          <p><strong>Código:</strong> {{ seleccionada?.noReservacion }}</p>
          <p><strong>Usuario:</strong> {{ seleccionada?.nombreUsuario }} {{ seleccionada?.apellidoUsuario }}</p>
          <p><strong>Email:</strong> {{ seleccionada?.correoUsuario }}</p>
          <p style="margin-top: 1.5rem; margin-bottom: 0.5rem;"><strong>Motivo *</strong></p>
          <textarea v-model="motivoCancelacion" style="width: 100%; padding: 0.6rem; border: 1.5px solid #e5e0d8; border-radius: 8px; font-family: inherit; resize: vertical; min-height: 100px;" placeholder="Explica el motivo..."></textarea>
          <p v-if="errCancelacion" style="color: #D40511; font-size: 0.8rem; margin: 0.5rem 0;">{{ errCancelacion }}</p>
          <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1.5rem;">
            <button @click="cerrarCancelacion" style="padding: 0.6rem 1.15rem; border: none; border-radius: 8px; background: #f0f0f0; font-weight: 600; cursor: pointer;">Cancelar</button>
            <button @click="confirmarCancelacion" :disabled="cancelando" style="padding: 0.6rem 1.15rem; border: none; border-radius: 8px; background: #D40511; color: white; font-weight: 600; cursor: pointer;">
              {{ cancelando ? 'Procesando...' : 'Confirmar' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'

const cargando = ref(false)
const reservaciones = ref([])
const busqueda = ref('')
const cancelando = ref(false)
const modalCancelacion = ref(false)
const seleccionada = ref(null)
const motivoCancelacion = ref('')
const errCancelacion = ref('')

const filtros = ref({
  fechaInicio: '',
  fechaFin: '',
  estado: '',
  tipo: ''
})

const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const fechaHoy = computed(() => {
  return new Date().toLocaleDateString('es-ES', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
})

const reservacionesFiltradas = computed(() => {
  return reservaciones.value.filter(r => {
    if (busqueda.value && !r.noReservacion.toLowerCase().includes(busqueda.value.toLowerCase())) return false
    if (filtros.value.fechaInicio && new Date(r.fechaCreacion) < new Date(filtros.value.fechaInicio)) return false
    if (filtros.value.fechaFin && new Date(r.fechaCreacion) > new Date(filtros.value.fechaFin)) return false
    if (filtros.value.estado && r.estadoId !== parseInt(filtros.value.estado)) return false
    if (filtros.value.tipo && r.tipoReserva !== parseInt(filtros.value.tipo)) return false
    return true
  })
})

const formatFecha = (fecha) => {
  if (!fecha) return '--'
  return new Date(fecha).toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' })
}

const estadoBg = (id) => {
  const map = { 1: '#fef0c3', 2: '#d1fae5', 3: '#fee2e2', 4: '#f3e8ff', 5: '#dbeafe' }
  return map[id] || '#f0f0f0'
}

const estadoColor = (id) => {
  const map = { 1: '#b45309', 2: '#065f46', 3: '#b91c1c', 4: '#6b21a8', 5: '#0c4a6e' }
  return map[id] || '#666'
}

const cargarReservaciones = async () => {
  cargando.value = true
  try {
    const res = await fetch(`${BASE}/api/admin/reservaciones/gestion`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include'
    })

    if (!res.ok) throw new Error('Error cargando')
    const data = await res.json()
    reservaciones.value = data.reservaciones || []
  } catch (err) {
    console.error('Error:', err)
    reservaciones.value = []
  } finally {
    cargando.value = false
  }
}

const aplicarFiltros = () => {
  // Los filtros se aplican automáticamente via computed
}

const limpiarFiltros = () => {
  busqueda.value = ''
  filtros.value = {
    fechaInicio: '',
    fechaFin: '',
    estado: '',
    tipo: ''
  }
}

const abrirCancelacion = (r) => {
  const confirma = confirm(`¿Cancela la reservación ${r.noReservacion} de ${r.nombreUsuario}?`)
  if (!confirma) return

  seleccionada.value = r
  motivoCancelacion.value = ''
  errCancelacion.value = ''
  modalCancelacion.value = true
}

const cerrarCancelacion = () => {
  modalCancelacion.value = false
  seleccionada.value = null
}

const confirmarCancelacion = async () => {
  if (!motivoCancelacion.value.trim()) {
    errCancelacion.value = 'El motivo es requerido'
    return
  }

  cancelando.value = true
  try {
    const res = await fetch(`${BASE}/api/admin/reservaciones/${seleccionada.value.id}/cancelar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ motivo: motivoCancelacion.value })
    })

    if (!res.ok) throw new Error('Error al cancelar')

    cerrarCancelacion()
    cargarReservaciones()
  } catch (err) {
    errCancelacion.value = err.message
  } finally {
    cancelando.value = false
  }
}


onMounted(() => {
  cargarReservaciones()
})
</script>

<style scoped>
.adm-filtros {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
  padding: 1.5rem;
  background: #faf8f4;
  border-radius: 10px;
  border: 1px solid #f0ede7;
  margin-bottom: 1.5rem;
}

.adm-filtros__grupo {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.adm-filtros__grupo label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #1C1A18;
  text-transform: uppercase;
}

.adm-filtros__grupo input,
.adm-filtros__grupo select {
  padding: 0.6rem 0.8rem;
  border: 1.5px solid #e5e0d8;
  border-radius: 8px;
  font-size: 0.85rem;
  background: white;
  color: #1C1A18;
}

.adm-filtros__grupo input:focus,
.adm-filtros__grupo select:focus {
  outline: none;
  border-color: #FFCC00;
}

.adm-btn {
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 8px;
  background: #FFCC00;
  color: #1C1A18;
  font-weight: 600;
  cursor: pointer;
}

.adm-btn:hover {
  background: #ffc000;
}

.adm-btn--sm {
  padding: 0.5rem 0.8rem;
  font-size: 0.8rem;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .adm-filtros {
    grid-template-columns: 1fr;
  }
}
</style>
