<template>
  <div class="page">
    <Encabezado />

    <!-- ═══ OVERLAY: Reserva expirada ═══ -->
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

        <!-- ═══ HEADER ═══ -->
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
          <!-- Timer -->
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

        <!-- ═══ PROGRESO MULTI-VUELO ═══ -->
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

        <!-- ═══ ESTADO: CARGANDO / ERROR ═══ -->
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

        <!-- ═══ BODY ═══ -->
        <div v-else-if="grupoActual" class="asi-body">

          <!-- ══ MAPA DEL AVIÓN ══ -->
          <div class="asi-mapa-wrap">

            <!-- Nariz -->
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

            <!-- Cuerpo del avión -->
            <div class="asi-avion-cuerpo">

              <!-- Cabecera columnas -->
              <div class="asi-cols-header">
                <div class="asi-fila-num"></div>
                <template v-for="(lbl, ci) in COLS_LABEL" :key="ci">
                  <div v-if="ci === 3" class="asi-pasillo"></div>
                  <div class="asi-col-lbl">{{ lbl }}</div>
                </template>
              </div>

              <!-- Zona Ejecutiva -->
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

              <!-- Separador cabina -->
              <div class="asi-separador">
                <div class="asi-separador__linea"></div>
                <span class="asi-separador__lbl">Separador de Cabina</span>
                <div class="asi-separador__linea"></div>
              </div>

              <!-- Zona Turista -->
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

            <!-- Cola -->
            <div class="asi-avion-cola">
              <svg viewBox="0 0 220 50" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10 0 L210 0 L210 24 C180 44, 140 50, 110 50 C80 50, 40 44, 10 24 Z"
                  fill="#1C1A18" stroke="#c9a96e" stroke-width="1.2"/>
              </svg>
            </div>

          </div><!-- /mapa-wrap -->

          <!-- ══ SIDEBAR ══ -->
          <aside class="asi-sidebar">

            <!-- Progreso selección -->
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

            <!-- Lista pasajeros -->
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

            <!-- Leyenda -->
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

            <!-- Botón continuar -->
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/Seleccionasientos.css'

const router = useRouter()
const API    = 'http://localhost:8080'

// ── Constantes ────────────────────────────────────────────────
const COLS_LABEL = ['A','B','C','D','E','F']

// ── Timer ─────────────────────────────────────────────────────
const tiempoRestante = ref(0)
const tiempoTotal    = ref(600)
const timerInterval  = ref(null)

function formatTiempo(s) {
  return `${Math.floor(s/60).toString().padStart(2,'0')}:${(s%60).toString().padStart(2,'0')}`
}
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

// ── Datos de sesión ───────────────────────────────────────────
const reservacionId = ref(null)
const proveedorId   = ref(null)

// ── Estado vuelos ─────────────────────────────────────────────
const flightGroups   = ref([])   // respuesta completa de la API
const grupoActualIdx = ref(0)
const loading        = ref(true)
const error          = ref(null)
const guardando      = ref(false)
const errorGuardar   = ref(null)

// ── Estado selección del grupo actual ────────────────────────
const asientos       = ref({})   // mapa id → { id, fila, col, clase, estado }
const seleccionados  = ref([])   // [asientoId | null] por índice de pasajero
const pasajeroActual = ref(0)

// ── Computed del grupo actual ─────────────────────────────────
const grupoActual    = computed(() => flightGroups.value[grupoActualIdx.value] ?? null)
const totalGrupos    = computed(() => flightGroups.value.length)
const esUltimoGrupo  = computed(() => grupoActualIdx.value === totalGrupos.value - 1)
const boletosActuales = computed(() => grupoActual.value?.boletosAgencia ?? [])

// Clase del pasajero activo determina qué zona puede usar
const claseActual = computed(() => {
  const b = boletosActuales.value[pasajeroActual.value]
  return b?.claseId === 2 ? 'Ejecutiva' : 'Turista'
})

// Filas = números (1, 2, 3...), columnas = letras A-F
const filasEjeActuales = computed(() => {
  const g = grupoActual.value
  if (!g) return []
  return Array.from({ length: g.filasEjecutiva }, (_, i) => i + 1)
})
const filasTActuales = computed(() => {
  const g = grupoActual.value
  if (!g) return []
  return Array.from({ length: g.totalFilas - g.filasEjecutiva }, (_, i) => i + 1)
})

const progreso = computed(() => {
  const n = boletosActuales.value.length
  if (!n) return 0
  return (seleccionados.value.filter(Boolean).length / n) * 100
})
const todoSeleccionado = computed(() =>
  boletosActuales.value.length > 0 &&
  seleccionados.value.length === boletosActuales.value.length &&
  seleccionados.value.every(Boolean)
)

// ── Generadores de letras: A,B,...,Z,AA,AB,... ───────────────
function* generarLetras(cantidad) {
  const abc = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  for (let i = 0; i < cantidad; i++) {
    let s = '', n = i
    do { s = abc[n % 26] + s; n = Math.floor(n / 26) - 1 } while (n >= 0)
    yield s
  }
}

// IDs backend — columna(letra) + fila(número)
function idEje(rowNum, colLetter) { return `E-${colLetter}${rowNum}` }
function idTur(rowNum, colLetter) { return `${colLetter}${rowNum}`   }

// ── Limpieza de sesión de reserva ────────────────────────────
function limpiarSesionReserva() {
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
}

// ── Guard: limpiar al salir del flujo de reserva ──────────────
const FLUJO_RESERVA = ['/reservar', '/seleccion-asientos', '/checkout', '/confirmacion']
onBeforeRouteLeave((to) => {
  if (!FLUJO_RESERVA.includes(to.path)) {
    limpiarSesionReserva()
  }
})

// ── onMounted ─────────────────────────────────────────────────
onMounted(async () => {
  const raw = sessionStorage.getItem('checkout_data')
  if (!raw) { router.push('/principal'); return }

  let cd
  try { cd = JSON.parse(raw) } catch { limpiarSesionReserva(); router.push('/principal'); return }

  // Validar que tenemos los datos mínimos
  if (!cd.reservacionId || !cd.proveedorId) {
    limpiarSesionReserva()
    router.push('/principal')
    return
  }

  reservacionId.value = cd.reservacionId
  proveedorId.value   = cd.proveedorId

  // Restaurar timer desde sessionStorage
  const expAt = sessionStorage.getItem('_reserva_expires_at')
  if (expAt) {
    const segs = Math.floor((Number(expAt) - Date.now()) / 1000)
    if (segs > 0) iniciarTimer(segs)
    else {
      // Timer expirado: limpiar y redirigir
      limpiarSesionReserva()
      router.push('/principal')
      return
    }
  }

  await cargarAsientos()
})

// ── Carga de asientos desde API ───────────────────────────────
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

// ── Construir mapa de asientos para un grupo ─────────────────
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

  // Ejecutiva: filas 1..filasEjecutiva, columnas A-F → E-A1, E-B1...
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

  // Turista: filas 1..(totalFilas-filasEje), columnas A-F → A1, B1...
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

// ── Lógica de asientos ────────────────────────────────────────
function esBloqueado(a) {
  if (!a) return true
  if (a.estado === 'ocupado') return true
  if (a.estado === 'propio')  return false   // siempre clickeable para cambiar foco
  if (claseActual.value === 'Ejecutiva' && a.clase !== 'Ejecutiva') return true
  if (claseActual.value === 'Turista'   && a.clase !== 'Turista')   return true
  return false
}

function claseAsiento(a) {
  if (!a) return 'asi-seat--vacio'
  if (a.estado === 'ocupado') return 'asi-seat--ocupado'
  if (a.estado === 'propio')  return 'asi-seat--seleccionado'
  // No bloquear visualmente si el pasajero activo puede usarlo
  const puedePasajeroActual =
    (claseActual.value === 'Ejecutiva' && a.clase === 'Ejecutiva') ||
    (claseActual.value === 'Turista'   && a.clase === 'Turista')
  if (!puedePasajeroActual) return 'asi-seat--bloqueado'
  return 'asi-seat--libre'
}

function indicePasajero(id) { return seleccionados.value.indexOf(id) }

async function seleccionarAsiento(a) {
  if (guardando.value || !a || a.estado === 'ocupado') return

  // Click en asiento propio → solo cambiar foco al pasajero
  if (a.estado === 'propio') {
    const idx = seleccionados.value.indexOf(a.id)
    if (idx !== -1) pasajeroActual.value = idx
    return
  }

  // Verificar clase
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

    // Actualizar mapa local sin recargar
    const nuevoMapa = { ...asientos.value }
    if (asientoAnterior && nuevoMapa[asientoAnterior]) {
      nuevoMapa[asientoAnterior] = { ...nuevoMapa[asientoAnterior], estado: 'libre' }
    }
    nuevoMapa[a.id] = { ...nuevoMapa[a.id], estado: 'propio' }
    asientos.value  = nuevoMapa

    const nuevaSel = [...seleccionados.value]
    nuevaSel[pasajeroActual.value] = a.id
    seleccionados.value = nuevaSel

    // Avanzar al siguiente pasajero sin asiento
    const sig = nuevaSel.findIndex((s, i) => i > pasajeroActual.value && !s)
    if (sig !== -1) pasajeroActual.value = sig

  } catch (e) {
    errorGuardar.value = e.message
  } finally {
    guardando.value = false
  }
}

// ── Continuar / Confirmar ─────────────────────────────────────
function handleContinuar() {
  if (!todoSeleccionado.value || guardando.value) return

  if (!esUltimoGrupo.value) {
    grupoActualIdx.value++
    construirMapa(grupoActualIdx.value)
  } else {
    // Todos los vuelos tienen asientos → ir al checkout
    if (timerInterval.value) clearInterval(timerInterval.value)
    sessionStorage.removeItem('_reserva_expires_at')
    sessionStorage.removeItem('_reserva_id')
    sessionStorage.removeItem('_reserva_no')
    router.push('/checkout')
  }
}
</script>