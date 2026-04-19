<template>
  <div class="page">
    <Encabezado />

    <div class="notif-page">
      <div class="notif-container">

        <!-- Encabezado de la página con título y botón marcar todas -->
        <div class="notif-page__head">
          <div>
            <h1 class="notif-page__titulo">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2.5" width="26" height="26">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
              Notificaciones
            </h1>
            <p class="notif-page__sub">Mantente al tanto de los cambios en tus reservaciones</p>
          </div>
          <button
            v-if="noLeidas > 0"
            class="notif-page__mark-all"
            @click="marcarTodasLeidas"
            :disabled="marcandoTodas"
            type="button"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            Marcar todas como leídas
          </button>
        </div>

        <!-- Filtros por estado de lectura -->
        <div class="notif-page__filtros">
          <button
            v-for="f in filtrosOpts"
            :key="f.val"
            :class="['notif-filtro', { 'notif-filtro--active': filtro === f.val }]"
            @click="filtro = f.val"
            type="button"
          >
            {{ f.label }}
            <span class="notif-filtro__n">{{ contarFiltro(f.val) }}</span>
          </button>
        </div>

        <!-- Estado de carga mientras llegan las notificaciones -->
        <div v-if="loading" class="notif-empty">
          <div class="notif-spin notif-spin--lg"></div>
          <p>Cargando notificaciones...</p>
        </div>

        <!-- Estado vacío total o sin resultados del filtro -->
        <div v-else-if="notificacionesFiltradas.length === 0" class="notif-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="56" height="56">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          <p class="notif-empty__titulo">
            {{ notificaciones.length === 0 ? 'No tienes notificaciones' : 'Sin resultados para este filtro' }}
          </p>
          <p class="notif-empty__sub">
            {{ notificaciones.length === 0 ? 'Los cambios en tus reservaciones aparecerán aquí' : 'Prueba con otro filtro' }}
          </p>
        </div>

        <!-- Lista completa de notificaciones filtradas -->
        <div v-else class="notif-page__list">
          <div
            v-for="n in notificacionesFiltradas"
            :key="n.id"
            class="notif-card"
            :class="{ 'notif-card--unread': !n.leido }"
          >
            <!-- Icono según tipo de notificación -->
            <div class="notif-card__icon" :class="`notif-card__icon--tipo-${n.tipo_notificacion_id}`">
              <svg v-if="n.tipo_notificacion_id === 3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              <svg v-else-if="n.tipo_notificacion_id === 4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
            </div>

            <!-- Contenido principal -->
            <div class="notif-card__body">
              <div class="notif-card__head">
                <h3 class="notif-card__tipo">{{ n.tipo_notificacion }}</h3>
                <span v-if="!n.leido" class="notif-card__badge">Nueva</span>
              </div>
              <p class="notif-card__reserva">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                  <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                  <line x1="7" y1="7" x2="7.01" y2="7"/>
                </svg>
                Reserva <strong>#{{ n.no_reservacion }}</strong>
              </p>
              <p v-if="n.mensaje_proveedor" class="notif-card__mensaje">
                {{ n.mensaje_proveedor }}
              </p>
              <p class="notif-card__fecha">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                  <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                </svg>
                {{ formatearFecha(n.fecha_emision) }}
              </p>
            </div>

            <!-- Acciones de la tarjeta -->
            <div class="notif-card__actions">
              <button
                v-if="!n.leido"
                class="notif-card__btn-leer"
                @click="marcarLeida(n.id)"
                type="button"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                Marcar como leída
              </button>
              <button
                class="notif-card__btn-ver"
                @click="verReservacion(n)"
                type="button"
              >
                Ver reservación
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Notificaciones.vue
 * @description Página completa de notificaciones del usuario autenticado.
 * Muestra la lista con filtros por estado (todas, no leídas, leídas),
 * permite marcar individualmente o todas como leídas, y navegar a la
 * reservación asociada.
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/notificaciones.css'

/** URL base del backend. @type {string} */
const API = 'http://localhost:8080'

/** Instancia del router. */
const router = useRouter()

/** Lista completa de notificaciones del usuario. @type {import('vue').Ref<Array>} */
const notificaciones = ref([])

/** Indica si la petición inicial está en curso. @type {import('vue').Ref<boolean>} */
const loading        = ref(true)

/** Filtro activo: 'todas', 'no-leidas' o 'leidas'. @type {import('vue').Ref<string>} */
const filtro         = ref('todas')

/** Evita doble clic en el botón "marcar todas". @type {import('vue').Ref<boolean>} */
const marcandoTodas  = ref(false)

/** Opciones del filtro de estado. */
const filtrosOpts = [
  { val: 'todas',     label: 'Todas' },
  { val: 'no-leidas', label: 'No leídas' },
  { val: 'leidas',    label: 'Leídas' },
]

/**
 * Cantidad de notificaciones no leídas, usada para mostrar el botón "marcar todas".
 * @type {import('vue').ComputedRef<number>}
 */
const noLeidas = computed(() => notificaciones.value.filter(n => !n.leido).length)

/**
 * Lista filtrada según el filtro activo.
 * @type {import('vue').ComputedRef<Array>}
 */
const notificacionesFiltradas = computed(() => {
  if (filtro.value === 'no-leidas') return notificaciones.value.filter(n => !n.leido)
  if (filtro.value === 'leidas')    return notificaciones.value.filter(n =>  n.leido)
  return notificaciones.value
})

/**
 * Cuenta las notificaciones que cumplen un filtro dado.
 * @param {string} tipo - 'todas', 'no-leidas' o 'leidas'.
 * @returns {number}
 */
function contarFiltro(tipo) {
  if (tipo === 'no-leidas') return notificaciones.value.filter(n => !n.leido).length
  if (tipo === 'leidas')    return notificaciones.value.filter(n =>  n.leido).length
  return notificaciones.value.length
}

/**
 * Carga las notificaciones del usuario desde el backend.
 */
async function cargar() {
  loading.value = true
  try {
    const res = await fetch(`${API}/api/notificaciones`, { credentials: 'include' })
    if (!res.ok) { notificaciones.value = []; return }
    const data = await res.json()
    notificaciones.value = data.notificaciones || []
  } catch {
    notificaciones.value = []
  } finally {
    loading.value = false
  }
}

/**
 * Marca una notificación específica como leída y actualiza el estado local.
 * @param {number} id - ID de la notificación.
 */
async function marcarLeida(id) {
  try {
    const res = await fetch(`${API}/api/notificaciones/${id}/leida`, {
      method: 'PATCH',
      credentials: 'include',
    })
    if (!res.ok) return
    const n = notificaciones.value.find(x => x.id === id)
    if (n) n.leido = true
  } catch { /* silencioso */ }
}

/**
 * Marca todas las notificaciones no leídas como leídas, una por una.
 */
async function marcarTodasLeidas() {
  marcandoTodas.value = true
  try {
    const noLeidasList = notificaciones.value.filter(n => !n.leido)
    await Promise.all(noLeidasList.map(n => marcarLeida(n.id)))
  } finally {
    marcandoTodas.value = false
  }
}

/**
 * Marca la notificación como leída (si no lo estaba) y navega a Mis Reservaciones.
 * @param {Object} n - La notificación seleccionada.
 */
async function verReservacion(n) {
  if (!n.leido) await marcarLeida(n.id)
  router.push(`/mis-reservaciones?ver=${n.no_reservacion}`)
}

/**
 * Formatea una fecha ISO o de BD a texto relativo legible.
 * Ej: "Hace 2 horas", "Hace 3 días", o fecha corta para más antiguas.
 * @param {string} fecha - Fecha en formato ISO o "YYYY-MM-DD HH:mm:ss".
 * @returns {string}
 */
function formatearFecha(fecha) {
  if (!fecha) return ''
  try {
    const f = new Date(fecha.replace(' ', 'T'))
    const diffMs = Date.now() - f.getTime()
    const diffMin = Math.floor(diffMs / 60000)
    if (diffMin < 1)    return 'Hace un momento'
    if (diffMin < 60)   return `Hace ${diffMin} min`
    const diffH = Math.floor(diffMin / 60)
    if (diffH < 24)     return `Hace ${diffH} h`
    const diffD = Math.floor(diffH / 24)
    if (diffD < 7)      return `Hace ${diffD} d`
    return f.toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' })
  } catch {
    return fecha
  }
}

/** Carga las notificaciones al montar la vista. */
onMounted(() => cargar())
</script>