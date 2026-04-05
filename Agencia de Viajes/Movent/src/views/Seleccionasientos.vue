<template>
  <div class="page">
    <Encabezado />

    <!-- Overlay de pantalla completa cuando el tiempo de reserva expira -->
    <div v-if="tiempoRestante === 0" class="asi-overlay">
      <div class="asi-overlay__card asi-overlay__card--error">
        <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="52" height="52">
          <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
        </svg>
        <h3 class="asi-overlay__titulo">Reserva expirada</h3>
        <p class="asi-overlay__msg">El tiempo para completar la reserva ha vencido.</p>
        <button class="asi-btn asi-btn--yellow" @click="$router.push('/principal')" type="button">
          Realizar nueva búsqueda
        </button>
      </div>
    </div>

    <div class="asi-page">
      <div class="asi-container">

        <!-- Encabezado con info del vuelo activo, número de pasajeros y timer de cuenta regresiva -->
        <div class="asi-header">
          <button class="asi-back" @click="$router.push('/reserva')" type="button">
            ← Datos pasajeros
          </button>
          <div class="asi-titulo">
            <h1 class="asi-titulo__main">Selección de Asientos</h1>
            <p class="asi-titulo__sub" v-if="grupoActual">
              {{ grupoActual.avionMarca }} {{ grupoActual.avionModelo }}
              &nbsp;·&nbsp; Vuelo {{ grupoActual.numeroVuelo }}
              &nbsp;·&nbsp; {{ boletosActuales.length }} pasajero{{ boletosActuales.length > 1 ? 's' : '' }}
              <template v-if="totalGrupos > 1">
                &nbsp;·&nbsp; <strong>Vuelo {{ grupoActualIdx + 1 }} de {{ totalGrupos }}</strong>
              </template>
            </p>
          </div>
          <!-- Timer que cambia de color según el tiempo restante -->
          <div v-if="tiempoRestante > 0" class="asi-timer"
            :class="{
              'asi-timer--warn':    tiempoRestante <= 180 && tiempoRestante > 60,
              'asi-timer--urgente': tiempoRestante <= 60,
            }">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
              <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
            </svg>
            {{ formatTiempo(tiempoRestante) }}
          </div>
        </div>

        <!-- Barra de progreso de vuelos cuando la reserva incluye más de uno -->
        <div v-if="totalGrupos > 1" class="asi-vuelos-prog">
          <template v-for="(g, i) in flightGroups" :key="i">
            <div class="asi-vuelos-prog__item"
              :class="{
                'asi-vuelos-prog__item--activo':    i === grupoActualIdx,
                'asi-vuelos-prog__item--completo':  i < grupoActualIdx,
              }">
              <span class="asi-vuelos-prog__num">{{ i + 1 }}</span>
              <span class="asi-vuelos-prog__label">{{ g.numeroVuelo }}</span>
            </div>
            <div v-if="i < totalGrupos - 1" class="asi-vuelos-prog__linea"
              :class="{ 'asi-vuelos-prog__linea--completa': i < grupoActualIdx }"></div>
          </template>
        </div>

        <!-- Estados de carga y error mientras se obtiene el mapa de asientos -->
        <div v-if="loading" class="asi-estado">
          <div class="asi-spinner"></div>
          <span>Cargando mapa de asientos...</span>
        </div>
        <div v-else-if="error" class="asi-estado asi-estado--error">
          <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="28" height="28">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ error }}
          <button class="asi-btn asi-btn--ghost" @click="cargarAsientos" style="margin-top:8px">Reintentar</button>
        </div>

        <!-- Cuerpo principal: mapa del avión + sidebar de pasajeros -->
        <div v-else-if="grupoActual" class="asi-body">

          <!-- Mapa visual del avión con zonas ejecutiva y turista -->
          <div class="asi-mapa-wrap">

            <!-- Nariz del avión (decorativa) -->
            <div class="asi-avion-nariz">
              <svg viewBox="0 0 220 90" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M110 4 C70 4, 14 30, 10 58 L10 86 L210 86 L210 58 C206 30, 150 4, 110 4Z"
                  fill="#1C1A18" stroke="#c9a96e" stroke-width="1.2"/>
                <path d="M110 14 C78 14, 28 36, 24 60 L24 78 L196 78 L196 60 C192 36, 142 14, 110 14Z"
                  fill="#2c2a24" stroke="#c9a96e" stroke-width="0.6" opacity="0.6"/>
                <ellipse cx="68"  cy="50" rx="8" ry="5" fill="#FFCC00" opacity="0.25"/>
                <ellipse cx="110" cy="44" rx="8" ry="5" fill="#FFCC00" opacity="0.25"/>
                <ellipse cx="152" cy="50" rx="8" ry="5" fill="#FFCC00" opacity="0.25"/>
                <text x="110" y="73" text-anchor="middle" fill="#c9a96e"
                  font-size="9" letter-spacing="3" font-family="inherit">CABINA</text>
              </svg>
            </div>

            <!-- Cuerpo del avión con filas de asientos -->
            <div class="asi-avion-cuerpo">

              <!-- Cabecera con letras de columna (A-F) -->
              <div class="asi-cols-header">
                <div class="asi-fila-num"></div>
                <template v-for="(lbl, ci) in COLS_LABEL" :key="ci">
                  <div v-if="ci === 3" class="asi-pasillo"></div>
                  <div class="asi-col-lbl">{{ lbl }}</div>
                </template>
              </div>

              <!-- Zona Ejecutiva: filas delanteras -->
              <div class="asi-zona-lbl asi-zona-lbl--eje"><span>Ejecutiva</span></div>
              <div v-for="fila in filasEjeActuales" :key="'eje-'+fila" class="asi-fila asi-fila--eje">
                <div class="asi-fila-num">{{ fila }}</div>
                <template v-for="(colLetter, ci) in COLS_LABEL" :key="ci">
                  <div v-if="ci === 3" class="asi-pasillo"></div>
                  <button
                    :class="['asi-seat', 'asi-seat--eje', claseAsiento(asientos[idEje(fila, colLetter)])]"
                    :disabled="guardando || esBloqueado(asientos[idEje(fila, colLetter)])"
                    @click="seleccionarAsiento(asientos[idEje(fila, colLetter)])"
                    :title="`E-${colLetter}${fila}`"
                    type="button"
                  >
                    <span v-if="asientos[idEje(fila, colLetter)]?.estado === 'propio'" class="asi-seat__num">
                      {{ indicePasajero(idEje(fila, colLetter)) + 1 }}
                    </span>
                  </button>
                </template>
              </div>

              <!-- Separador visual entre cabina ejecutiva y turista -->
              <div class="asi-separador">
                <div class="asi-separador__linea"></div>
                <span class="asi-separador__lbl">Separador de Cabina</span>
                <div class="asi-separador__linea"></div>
              </div>

              <!-- Zona Turista: filas traseras -->
              <div class="asi-zona-lbl asi-zona-lbl--tur"><span>Turista</span></div>
              <div v-for="fila in filasTActuales" :key="'tur-'+fila" class="asi-fila">
                <div class="asi-fila-num">{{ fila }}</div>
                <template v-for="(colLetter, ci) in COLS_LABEL" :key="ci">
                  <div v-if="ci === 3" class="asi-pasillo"></div>
                  <button
                    :class="['asi-seat', claseAsiento(asientos[idTur(fila, colLetter)])]"
                    :disabled="guardando || esBloqueado(asientos[idTur(fila, colLetter)])"
                    @click="seleccionarAsiento(asientos[idTur(fila, colLetter)])"
                    :title="`${colLetter}${fila}`"
                    type="button"
                  >
                    <span v-if="asientos[idTur(fila, colLetter)]?.estado === 'propio'" class="asi-seat__num">
                      {{ indicePasajero(idTur(fila, colLetter)) + 1 }}
                    </span>
                  </button>
                </template>
              </div>

            </div><!-- /avion-cuerpo -->

            <!-- Cola del avión (decorativa) -->
            <div class="asi-avion-cola">
              <svg viewBox="0 0 220 50" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10 0 L210 0 L210 24 C180 44, 140 50, 110 50 C80 50, 40 44, 10 24 Z"
                  fill="#1C1A18" stroke="#c9a96e" stroke-width="1.2"/>
              </svg>
            </div>

          </div><!-- /mapa-wrap -->

          <!-- Sidebar derecho: progreso, lista de pasajeros, leyenda y botón continuar -->
          <aside class="asi-sidebar">

            <!-- Barra de progreso indicando cuántos asientos han sido asignados -->
            <div class="asi-progreso">
              <div class="asi-progreso__header">
                <span class="asi-progreso__titulo">Pasajeros</span>
                <span class="asi-progreso__conteo">
                  {{ seleccionados.filter(Boolean).length }} / {{ boletosActuales.length }}
                </span>
              </div>
              <div class="asi-progreso__barra">
                <div class="asi-progreso__fill" :style="{ width: progreso + '%' }"></div>
              </div>
              <p v-if="guardando" class="asi-progreso__guardando">
                <span class="asi-spinner-sm"></span> Guardando asiento...
              </p>
              <p v-if="errorGuardar" class="asi-progreso__error">{{ errorGuardar }}</p>
            </div>

            <!-- Lista de pasajeros con su asiento actual y clase -->
            <div class="asi-pax-lista">
              <div v-for="(b, i) in boletosActuales" :key="b.boletoId"
                class="asi-pax-item"
                :class="{
                  'asi-pax-item--activo':   i === pasajeroActual && !todoSeleccionado,
                  'asi-pax-item--completo': !!seleccionados[i],
                }"
                @click="pasajeroActual = i"
                role="button" tabindex="0">
                <div class="asi-pax-item__num">{{ i + 1 }}</div>
                <div class="asi-pax-item__info">
                  <span class="asi-pax-item__label">Pasajero {{ i + 1 }}</span>
                  <span class="asi-pax-item__asiento">
                    {{ seleccionados[i] ? `Asiento ${seleccionados[i]}` : 'Sin asignar' }}
                  </span>
                  <span class="asi-pax-item__clase">{{ b.claseId === 2 ? 'Ejecutiva' : 'Turista' }}</span>
                </div>
              </div>
            </div>

            <!-- Leyenda visual de los estados posibles de un asiento -->
            <div class="asi-leyenda">
              <h3 class="asi-leyenda__titulo">Leyenda</h3>
              <div class="asi-leyenda__items">
                <div class="asi-leyenda__item">
                  <div class="asi-seat asi-seat--libre asi-seat--muestra"></div>
                  <span>Disponible</span>
                </div>
                <div class="asi-leyenda__item">
                  <div class="asi-seat asi-seat--seleccionado asi-seat--muestra"></div>
                  <span>Tuyo</span>
                </div>
                <div class="asi-leyenda__item">
                  <div class="asi-seat asi-seat--ocupado asi-seat--muestra"></div>
                  <span>Ocupado</span>
                </div>
                <div class="asi-leyenda__item">
                  <div class="asi-seat asi-seat--bloqueado asi-seat--muestra"></div>
                  <span>Otra clase</span>
                </div>
              </div>
            </div>

            <!-- Botón de continuar: avanza al siguiente vuelo o confirma todos los asientos -->
            <button class="asi-continuar"
              :class="{ 'asi-continuar--listo': todoSeleccionado && !guardando }"
              :disabled="!todoSeleccionado || guardando"
              @click="handleContinuar"
              type="button">
              <template v-if="guardando">Guardando...</template>
              <template v-else-if="!todoSeleccionado">
                Selecciona {{ boletosActuales.length - seleccionados.filter(Boolean).length }}
                asiento{{ (boletosActuales.length - seleccionados.filter(Boolean).length) !== 1 ? 's' : '' }} más
              </template>
              <template v-else-if="!esUltimoGrupo">Siguiente vuelo →</template>
              <template v-else>Confirmar Asientos ✓</template>
            </button>

          </aside>
        </div><!-- /body -->

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Seleccionasientos.vue
 * @description Vista interactiva para que los pasajeros elijan sus asientos en el mapa
 * del avión. Soporta múltiples vuelos (grupos), clases ejecutiva y turista, timer de
 * expiración de reserva y sincronización en tiempo real con el backend.
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/Seleccionasientos.css'

const router = useRouter()

/** URL base del backend. @type {string} */
const API = 'http://localhost:8080'

/** Etiquetas de columna para el mapa de asientos (A-F). @type {string[]} */
const COLS_LABEL = ['A','B','C','D','E','F']

/** Segundos restantes en el contador regresivo de la reserva. @type {import('vue').Ref<number>} */
const tiempoRestante = ref(0)

/** Total de segundos con que se inició el timer (para animaciones de barra si se necesitara). @type {import('vue').Ref<number>} */
const tiempoTotal = ref(600)

/** Referencia al intervalo del timer para poder limpiarlo al desmontar. @type {import('vue').Ref<number|null>} */
const timerInterval = ref(null)

/**
 * Formatea segundos a string MM:SS.
 * @param {number} s - segundos totales
 * @returns {string}
 */
function formatTiempo(s) {
  return `${Math.floor(s/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`
}

/**
 * Inicia o reinicia el contador regresivo con la cantidad de segundos indicada.
 * @param {number} segs - segundos totales para la cuenta regresiva
 */
function iniciarTimer(segs) {
  tiempoRestante.value = segs
  tiempoTotal.value    = segs
  if (timerInterval.value) clearInterval(timerInterval.value)
  timerInterval.value = setInterval(() => {
    tiempoRestante.value = Math.max(0, tiempoRestante.value - 1)
    if (tiempoRestante.value === 0) clearInterval(timerInterval.value)
  }, 1000)
}

onUnmounted(() => { if (timerInterval.value) clearInterval(timerInterval.value) })

/** ID de la reservación en curso. @type {import('vue').Ref<number|null>} */
const reservacionId = ref(null)

/** ID del proveedor de la reservación. @type {import('vue').Ref<number|null>} */
const proveedorId = ref(null)

/** Grupos de vuelos devueltos por la API, cada uno con su propio mapa de asientos. @type {import('vue').Ref<any[]>} */
const flightGroups = ref([])

/** Índice del grupo (vuelo) que se está procesando actualmente. @type {import('vue').Ref<number>} */
const grupoActualIdx = ref(0)

/** Indica si la carga inicial de asientos está en curso. @type {import('vue').Ref<boolean>} */
const loading = ref(true)

/** Mensaje de error de carga o de API. @type {import('vue').Ref<string|null>} */
const error = ref(null)

/** Indica si se está guardando la elección de un asiento en el backend. @type {import('vue').Ref<boolean>} */
const guardando = ref(false)

/** Mensaje de error cuando falla el guardado de un asiento. @type {import('vue').Ref<string|null>} */
const errorGuardar = ref(null)

/**
 * Mapa plano de todos los asientos del vuelo actual.
 * Clave: ID del asiento (ej. "A3" o "E-B2"), valor: objeto con estado, clase, fila y columna.
 * @type {import('vue').Ref<Record<string, {id: string, fila: number, col: string, clase: string, estado: string}>>}
 */
const asientos = ref({})

/**
 * Array de IDs de asiento seleccionados, indexado por posición de pasajero.
 * null significa que ese pasajero aún no tiene asiento.
 * @type {import('vue').Ref<(string|null)[]>}
 */
const seleccionados = ref([])

/** Índice del pasajero activo al que se le asignará el próximo asiento que se clickee. @type {import('vue').Ref<number>} */
const pasajeroActual = ref(0)

/** Grupo de vuelo actualmente en pantalla. @type {import('vue').ComputedRef<any|null>} */
const grupoActual = computed(() => flightGroups.value[grupoActualIdx.value] ?? null)

/** Cantidad total de grupos (vuelos) en la reserva. @type {import('vue').ComputedRef<number>} */
const totalGrupos = computed(() => flightGroups.value.length)

/** True si el grupo actual es el último vuelo de la reserva. @type {import('vue').ComputedRef<boolean>} */
const esUltimoGrupo = computed(() => grupoActualIdx.value === totalGrupos.value - 1)

/** Boletos del grupo actual, uno por pasajero. @type {import('vue').ComputedRef<any[]>} */
const boletosActuales = computed(() => grupoActual.value?.boletosAgencia ?? [])

/**
 * Clase (Ejecutiva o Turista) del pasajero actualmente seleccionado.
 * Determina qué zona del avión puede usar.
 * @type {import('vue').ComputedRef<string>}
 */
const claseActual = computed(() => {
  const b = boletosActuales.value[pasajeroActual.value]
  return b?.claseId === 2 ? 'Ejecutiva' : 'Turista'
})

/** Números de fila de la zona ejecutiva del vuelo actual. @type {import('vue').ComputedRef<number[]>} */
const filasEjeActuales = computed(() => {
  const g = grupoActual.value
  if (!g) return []
  return Array.from({ length: g.filasEjecutiva }, (_, i) => i + 1)
})

/** Números de fila de la zona turista del vuelo actual. @type {import('vue').ComputedRef<number[]>} */
const filasTActuales = computed(() => {
  const g = grupoActual.value
  if (!g) return []
  return Array.from({ length: g.totalFilas - g.filasEjecutiva }, (_, i) => i + 1)
})

/** Porcentaje de asientos completados respecto al total de pasajeros. @type {import('vue').ComputedRef<number>} */
const progreso = computed(() => {
  const n = boletosActuales.value.length
  if (!n) return 0
  return (seleccionados.value.filter(Boolean).length / n) * 100
})

/** True cuando todos los pasajeros tienen asiento asignado. @type {import('vue').ComputedRef<boolean>} */
const todoSeleccionado = computed(() =>
  boletosActuales.value.length > 0 &&
  seleccionados.value.length === boletosActuales.value.length &&
  seleccionados.value.every(Boolean)
)

/**
 * Generador de letras para etiquetas de columna (A, B, ..., Z, AA, AB...).
 * @param {number} cantidad - cuántas letras generar
 * @yields {string}
 */
function* generarLetras(cantidad) {
  const abc = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  for (let i = 0; i < cantidad; i++) {
    let s = '', n = i
    do { s = abc[n % 26] + s; n = Math.floor(n / 26) - 1 } while (n >= 0)
    yield s
  }
}

/**
 * Genera el ID de un asiento de zona ejecutiva.
 * @param {number} rowNum - número de fila
 * @param {string} colLetter - letra de columna
 * @returns {string} ej. "E-B3"
 */
function idEje(rowNum, colLetter) { return `E-${colLetter}${rowNum}` }

/**
 * Genera el ID de un asiento de zona turista.
 * @param {number} rowNum - número de fila
 * @param {string} colLetter - letra de columna
 * @returns {string} ej. "B3"
 */
function idTur(rowNum, colLetter) { return `${colLetter}${rowNum}` }

/**
 * Elimina de sessionStorage todos los datos relacionados al flujo de reserva activo.
 */
function limpiarSesionReserva() {
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
}

/** Rutas que pertenecen al flujo de reserva; al salir hacia otra se limpia la sesión. @type {string[]} */
const FLUJO_RESERVA = ['/reservar', '/seleccion-asientos', '/checkout', '/confirmacion']

/** Guard de navegación: limpia la sesión si el usuario abandona el flujo de reserva. */
onBeforeRouteLeave((to) => {
  if (!FLUJO_RESERVA.includes(to.path)) {
    limpiarSesionReserva()
  }
})

onMounted(async () => {
  const raw = sessionStorage.getItem('checkout_data')
  if (!raw) { router.push('/principal'); return }

  let cd
  try { cd = JSON.parse(raw) } catch { limpiarSesionReserva(); router.push('/principal'); return }

  if (!cd.reservacionId || !cd.proveedorId) {
    limpiarSesionReserva()
    router.push('/principal')
    return
  }

  reservacionId.value = cd.reservacionId
  proveedorId.value   = cd.proveedorId

  // Restaurar timer desde sessionStorage para que sea consistente entre recargas
  const expAt = sessionStorage.getItem('_reserva_expires_at')
  if (expAt) {
    const segs = Math.floor((Number(expAt) - Date.now()) / 1000)
    if (segs > 0) iniciarTimer(segs)
    else {
      limpiarSesionReserva()
      router.push('/principal')
      return
    }
  }

  await cargarAsientos()
})

/**
 * Carga el mapa de asientos desde el backend para la reservación y proveedor actuales.
 * Llena flightGroups y construye el mapa del primer vuelo.
 */
async function cargarAsientos() {
  loading.value = true; error.value = null

  try {
    const res = await fetch(`${API}/api/reservaciones/asientos-vuelo`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        reservacion_id: reservacionId.value,
        proveedor_id:   proveedorId.value,
      }),
    })
    if (!res.ok) {
      const e = await res.json().catch(() => ({}))
      throw new Error(e.error ?? `Error ${res.status} al cargar asientos.`)
    }
    flightGroups.value = await res.json()
    if (!flightGroups.value.length) throw new Error('No se recibieron datos de vuelos.')
    construirMapa(0)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

/**
 * Construye el mapa reactivo de asientos para el grupo (vuelo) indicado.
 * Marca los asientos ocupados, los propios del usuario y los libres.
 * @param {number} idx - índice del grupo en flightGroups
 */
function construirMapa(idx) {
  const grupo = flightGroups.value[idx]
  if (!grupo) return

  const ocupados = new Set(grupo.asientosOcupados ?? [])
  const boletos  = grupo.boletosAgencia ?? []

  seleccionados.value  = boletos.map(b => b.asiento ?? null)
  const primerLibre    = seleccionados.value.findIndex(s => !s)
  pasajeroActual.value = primerLibre === -1 ? 0 : primerLibre

  const mapa = {}
  const cols = ['A','B','C','D','E','F']

  // Zona ejecutiva: E-A1, E-B1, ..., E-F{filasEjecutiva}
  for (let row = 1; row <= grupo.filasEjecutiva; row++) {
    for (const col of cols) {
      const id = idEje(row, col)
      mapa[id] = {
        id, fila: row, col, clase: 'Ejecutiva',
        estado: ocupados.has(id)               ? 'ocupado'
              : seleccionados.value.includes(id) ? 'propio'
              : 'libre',
      }
    }
  }

  // Zona turista: A1, B1, ..., F{filasT}
  const filasT = grupo.totalFilas - grupo.filasEjecutiva
  for (let row = 1; row <= filasT; row++) {
    for (const col of cols) {
      const id = idTur(row, col)
      mapa[id] = {
        id, fila: row, col, clase: 'Turista',
        estado: ocupados.has(id)               ? 'ocupado'
              : seleccionados.value.includes(id) ? 'propio'
              : 'libre',
      }
    }
  }

  asientos.value = mapa
}

/**
 * Determina si un asiento debe estar deshabilitado para el pasajero activo.
 * Un asiento se bloquea si está ocupado, o si pertenece a una clase distinta.
 * @param {object|undefined} a - objeto asiento del mapa
 * @returns {boolean}
 */
function esBloqueado(a) {
  if (!a) return true
  if (a.estado === 'ocupado') return true
  if (a.estado === 'propio')  return false
  if (claseActual.value === 'Ejecutiva' && a.clase !== 'Ejecutiva') return true
  if (claseActual.value === 'Turista'   && a.clase !== 'Turista')   return true
  return false
}

/**
 * Devuelve la clase CSS correspondiente al estado visual del asiento.
 * @param {object|undefined} a - objeto asiento del mapa
 * @returns {string}
 */
function claseAsiento(a) {
  if (!a) return 'asi-seat--vacio'
  if (a.estado === 'ocupado') return 'asi-seat--ocupado'
  if (a.estado === 'propio')  return 'asi-seat--seleccionado'
  const puedePasajeroActual =
    (claseActual.value === 'Ejecutiva' && a.clase === 'Ejecutiva') ||
    (claseActual.value === 'Turista'   && a.clase === 'Turista')
  if (!puedePasajeroActual) return 'asi-seat--bloqueado'
  return 'asi-seat--libre'
}

/**
 * Devuelve el índice del pasajero que tiene asignado el asiento con ese ID.
 * @param {string} id - ID del asiento
 * @returns {number} -1 si no está asignado
 */
function indicePasajero(id) { return seleccionados.value.indexOf(id) }

/**
 * Maneja el click en un asiento: valida clase, llama al backend para guardar
 * el cambio y actualiza el mapa local sin recargar.
 * @param {object|undefined} a - objeto asiento del mapa
 */
async function seleccionarAsiento(a) {
  if (guardando.value || !a || a.estado === 'ocupado') return

  // Click en asiento propio: solo cambiar el foco al pasajero que lo tiene
  if (a.estado === 'propio') {
    const idx = seleccionados.value.indexOf(a.id)
    if (idx !== -1) pasajeroActual.value = idx
    return
  }

  if (claseActual.value === 'Ejecutiva' && a.clase !== 'Ejecutiva') return
  if (claseActual.value === 'Turista'   && a.clase !== 'Turista')   return
  if (seleccionados.value[pasajeroActual.value] === a.id) return

  const boleto         = boletosActuales.value[pasajeroActual.value]
  const asientoAnterior = seleccionados.value[pasajeroActual.value]
  if (!boleto) return

  guardando.value = true; errorGuardar.value = null

  try {
    const res = await fetch(`${API}/api/reservaciones/asientos-vuelo`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        reservacion_id: reservacionId.value,
        proveedor_id:   proveedorId.value,
        boleto_id:      boleto.boletoId,
        nuevo_asiento:  a.id,
      }),
    })
    if (!res.ok) {
      const e = await res.json().catch(() => ({}))
      throw new Error(e.error ?? 'Error al cambiar asiento.')
    }

    // Actualizar mapa local optimistamente sin recargar toda la lista
    const nuevoMapa = { ...asientos.value }
    if (asientoAnterior && nuevoMapa[asientoAnterior]) {
      nuevoMapa[asientoAnterior] = { ...nuevoMapa[asientoAnterior], estado: 'libre' }
    }
    nuevoMapa[a.id] = { ...nuevoMapa[a.id], estado: 'propio' }
    asientos.value  = nuevoMapa

    const nuevaSel = [...seleccionados.value]
    nuevaSel[pasajeroActual.value] = a.id
    seleccionados.value = nuevaSel

    // Avanzar automáticamente al siguiente pasajero sin asiento
    const sig = nuevaSel.findIndex((s, i) => i > pasajeroActual.value && !s)
    if (sig !== -1) pasajeroActual.value = sig

  } catch (e) {
    errorGuardar.value = e.message
  } finally {
    guardando.value = false
  }
}

/**
 * Avanza al siguiente vuelo o navega al checkout si ya se completó el último.
 * Limpia el timer cuando se confirman todos los asientos.
 */
function handleContinuar() {
  if (!todoSeleccionado.value || guardando.value) return

  if (!esUltimoGrupo.value) {
    grupoActualIdx.value++
    construirMapa(grupoActualIdx.value)
  } else {
    if (timerInterval.value) clearInterval(timerInterval.value)
    sessionStorage.removeItem('_reserva_expires_at')
    sessionStorage.removeItem('_reserva_id')
    sessionStorage.removeItem('_reserva_no')
    router.push('/checkout')
  }
}
</script>
