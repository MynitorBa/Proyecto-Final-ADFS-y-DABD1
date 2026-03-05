<template>
  <div class="page">
    <Encabezado />

    <div class="dh-page">
      <div class="dh-container">

        <div v-if="loading" class="dh-empty">
          <div class="dh-spinner"></div>
          <p>Cargando detalle del hotel...</p>
        </div>

        <div v-else-if="error" class="dh-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <p>{{ error }}</p>
          <button class="dh-btn dh-btn--ghost" @click="$router.back()" type="button">Volver</button>
        </div>

        <template v-else-if="hotel">

          <!-- Breadcrumb -->
          <div class="dh-breadcrumb">
            <button class="dh-breadcrumb__btn" @click="$router.back()" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
              Volver a resultados
            </button>
            <span class="dh-breadcrumb__sep">/</span>
            <span class="dh-breadcrumb__actual">{{ hotel.nombre }}</span>
          </div>

          <div class="dh-grid">

            <!-- IZQUIERDA -->
            <div class="dh-left">

              <!-- Hero hotel -->
              <div class="dh-hero">
                <div class="dh-hero__img">
                  <div class="dh-hero__img-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="0.8" width="80" height="80"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  </div>
                </div>
                <div class="dh-hero__info">
                  <div class="dh-hero__info-top">
                    <div>
                      <h1 class="dh-hero__nombre">{{ hotel.nombre }}</h1>
                      <p class="dh-hero__ubicacion">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                        {{ hotel.ciudad }}, {{ hotel.pais }}
                      </p>
                    </div>
                    <div class="dh-hero__estadía">
                      <div class="dh-hero__fechas">
                        <div class="dh-hero__fecha-item">
                          <span class="dh-hero__fecha-lbl">Check-in</span>
                          <span class="dh-hero__fecha-val">{{ formatFecha(busqueda.checkIn) }}</span>
                        </div>
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="16" height="16"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                        <div class="dh-hero__fecha-item dh-hero__fecha-item--r">
                          <span class="dh-hero__fecha-lbl">Check-out</span>
                          <span class="dh-hero__fecha-val">{{ formatFecha(busqueda.checkOut) }}</span>
                        </div>
                      </div>
                      <div class="dh-hero__noches">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                        {{ noches }} noche{{ noches!==1?'s':'' }}
                      </div>
                    </div>
                  </div>
                  <p v-if="hotel.descripcion" class="dh-hero__desc">{{ hotel.descripcion }}</p>
                  <div v-if="hotel.servicios?.length" class="dh-hero__servicios">
                    <span v-for="s in hotel.servicios" :key="s" class="dh-servicio">{{ s }}</span>
                  </div>
                </div>
              </div>

              <!-- Habitaciones disponibles -->
              <div class="dh-section">
                <h3 class="dh-section__title">Habitaciones disponibles</h3>

                <div class="dh-habitaciones">
                  <div v-for="hab in hotel.habitaciones" :key="hab.habitacionId"
                    class="dh-hab-card"
                    :class="{
                      'dh-hab-card--active': habSeleccionada?.habitacionId === hab.habitacionId,
                      'dh-hab-card--preseleccionada': route.query.habitacionId == hab.habitacionId
                    }">

                    <div class="dh-hab-card__img">
                      <div class="dh-hab-card__img-placeholder">
                        <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.3)" stroke-width="1" width="32" height="32"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                      </div>
                      <div class="dh-hab-card__tipo">{{ hab.tipoHabitacion }}</div>
                    </div>

                    <div class="dh-hab-card__info">
                      <div class="dh-hab-card__top">
                        <div>
                          <h4 class="dh-hab-card__nombre">{{ hab.tipoHabitacion }}</h4>
                          <p class="dh-hab-card__cama">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                            {{ hab.tipoCama }}
                          </p>
                        </div>
                        <div class="dh-hab-card__precios">
                          <span class="dh-hab-card__precio-lbl">por noche</span>
                          <span class="dh-hab-card__precio">${{ hab.precioPorNoche?.toFixed(2) }}</span>
                          <span class="dh-hab-card__precio-total">Total: <strong>${{ (hab.precioPorNoche * noches).toFixed(2) }}</strong></span>
                        </div>
                      </div>

                      <p class="dh-hab-card__desc">{{ hab.descripcion }}</p>

                      <div class="dh-hab-card__meta">
                        <span>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                          Hasta {{ hab.capacidad }} persona{{ hab.capacidad!==1?'s':'' }}
                        </span>
                        <span v-if="hab.metrosCuadrados">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
                          {{ hab.metrosCuadrados }} m²
                        </span>
                        <span class="dh-hab-card__disp" :class="{ 'dh-hab-card__disp--bajo': (hab.cantidadDisponible??99) <= 3 }">
                          {{ (hab.cantidadDisponible??99) <= 3 ? `¡Solo ${hab.cantidadDisponible} disponible${hab.cantidadDisponible!==1?'s':''}!` : `${hab.cantidadDisponible ?? '--'} disponibles` }}
                        </span>
                      </div>

                      <div v-if="hab.amenidades?.length" class="dh-hab-card__amenidades">
                        <span v-for="am in hab.amenidades.slice(0,6)" :key="am" class="dh-amenidad">{{ am }}</span>
                        <span v-if="hab.amenidades.length > 6" class="dh-amenidad dh-amenidad--mas">+{{ hab.amenidades.length-6 }}</span>
                      </div>

                      <!-- Cantidad de habitaciones -->
                      <div class="dh-hab-card__cantidad-wrap">
                        <div class="dh-hab-card__cantidad">
                          <button class="dh-qty-btn" @click="cambiarCantidad(hab, -1)" :disabled="!getCantidad(hab)" type="button">−</button>
                          <span class="dh-qty-val">{{ getCantidad(hab) }}</span>
                          <button class="dh-qty-btn" @click="cambiarCantidad(hab, 1)" type="button">+</button>
                        </div>
                        <button class="dh-btn dh-btn--yellow dh-hab-card__cta"
                          @click="seleccionarHab(hab)" type="button">
                          {{ habSeleccionada?.habitacionId === hab.habitacionId ? 'Seleccionada ✓' : 'Seleccionar' }}
                        </button>
                      </div>

                    </div>
                  </div>
                </div>
              </div>

              <!-- Huéspedes -->
              <div class="dh-section" v-if="habSeleccionada">
                <h3 class="dh-section__title">
                  Huéspedes
                  <span class="dh-section__sub">{{ huespedes.length }} de {{ busqueda.huespedes }}</span>
                </h3>
                <div v-for="(h, idx) in huespedes" :key="idx" class="dh-huesped">
                  <div class="dh-huesped__head">
                    <span class="dh-huesped__num">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      Huésped {{ idx + 1 }}
                    </span>
                    <button v-if="idx > 0" class="dh-huesped__remove" @click="quitarHuesped(idx)" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                  <div class="dh-huesped__grid">
                    <div class="dh-field">
                      <label class="dh-field__label">Nombre *</label>
                      <input class="dh-field__input" v-model="h.nombre" placeholder="Nombre" type="text" />
                    </div>
                    <div class="dh-field">
                      <label class="dh-field__label">Apellido *</label>
                      <input class="dh-field__input" v-model="h.apellido" placeholder="Apellido" type="text" />
                    </div>
                    <div class="dh-field">
                      <label class="dh-field__label">DPI / Pasaporte *</label>
                      <input class="dh-field__input" v-model="h.documento" placeholder="No. documento" type="text" />
                    </div>
                    <div class="dh-field">
                      <label class="dh-field__label">Correo electrónico</label>
                      <input class="dh-field__input" v-model="h.correo" placeholder="correo@ejemplo.com" type="email" />
                    </div>
                  </div>
                </div>
                <button v-if="huespedes.length < busqueda.huespedes"
                  class="dh-btn dh-btn--ghost dh-add-huesped"
                  @click="agregarHuesped" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  Agregar huésped
                </button>
              </div>

            </div>

            <!-- DERECHA: RESUMEN -->
            <div class="dh-right">
              <div class="dh-resumen">
                <h3 class="dh-resumen__title">Resumen</h3>

                <div class="dh-resumen__hotel">
                  <div class="dh-resumen__hotel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  </div>
                  <div>
                    <p class="dh-resumen__hotel-nombre">{{ hotel.nombre }}</p>
                    <p class="dh-resumen__hotel-loc">{{ hotel.ciudad }}, {{ hotel.pais }}</p>
                  </div>
                </div>

                <div class="dh-resumen__fechas">
                  <div class="dh-resumen__fecha">
                    <span class="dh-resumen__fecha-lbl">Check-in</span>
                    <span class="dh-resumen__fecha-val">{{ formatFecha(busqueda.checkIn) }}</span>
                  </div>
                  <div class="dh-resumen__fecha">
                    <span class="dh-resumen__fecha-lbl">Check-out</span>
                    <span class="dh-resumen__fecha-val">{{ formatFecha(busqueda.checkOut) }}</span>
                  </div>
                  <div class="dh-resumen__fecha">
                    <span class="dh-resumen__fecha-lbl">Noches</span>
                    <span class="dh-resumen__fecha-val">{{ noches }}</span>
                  </div>
                </div>

                <div class="dh-resumen__rows" v-if="habSeleccionada">
                  <div class="dh-resumen__row">
                    <span>Habitación</span>
                    <span>{{ habSeleccionada.tipoHabitacion }}</span>
                  </div>
                  <div class="dh-resumen__row">
                    <span>Cantidad</span>
                    <span>{{ getCantidad(habSeleccionada) }}</span>
                  </div>
                  <div class="dh-resumen__row">
                    <span>Precio/noche c/u</span>
                    <span>${{ habSeleccionada.precioPorNoche?.toFixed(2) }}</span>
                  </div>
                  <div class="dh-resumen__row">
                    <span>{{ noches }} noches × {{ getCantidad(habSeleccionada) }}</span>
                    <span>${{ totalHotel.toFixed(2) }}</span>
                  </div>
                </div>
                <div v-else class="dh-resumen__placeholder">
                  Selecciona una habitación para ver el total
                </div>

                <div class="dh-resumen__total">
                  <span>Total</span>
                  <strong>${{ totalHotel.toFixed(2) }}</strong>
                </div>

                <p v-if="formError" class="dh-form-error">{{ formError }}</p>

                <button class="dh-btn dh-btn--yellow dh-resumen__cta"
                  @click="continuar" :disabled="!habSeleccionada" type="button">
                  Continuar al checkout
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                </button>

                <p class="dh-resumen__nota">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  Precios incluyen impuestos
                </p>
              </div>
            </div>

          </div>
        </template>

      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/detallehotel.css'

const router = useRouter()
const route  = useRoute()
const API    = 'http://localhost:7000'

const hotel         = ref(null)
const loading       = ref(true)
const error         = ref('')
const habSeleccionada = ref(null)
const cantidades    = ref({})
const formError     = ref('')

const busqueda = ref({
  checkIn:   route.query.checkIn  || '',
  checkOut:  route.query.checkOut || '',
  huespedes: Number(route.query.huespedes) || 1,
})

const noches = computed(() => {
  if (!busqueda.value.checkIn || !busqueda.value.checkOut) return 0
  return Math.max(0, Math.ceil((new Date(busqueda.value.checkOut) - new Date(busqueda.value.checkIn)) / 86400000))
})

const totalHotel = computed(() => {
  if (!habSeleccionada.value) return 0
  const cant = getCantidad(habSeleccionada.value)
  return (habSeleccionada.value.precioPorNoche || 0) * noches.value * cant
})

const huespedVacio = () => ({ nombre:'', apellido:'', documento:'', correo:'' })
const huespedes = ref([huespedVacio()])

onMounted(() => cargarHotel())

async function cargarHotel() {
  loading.value = true; error.value = ''
  try {
    const r = await fetch(`${API}/api/hoteles/${route.params.id}?checkIn=${busqueda.value.checkIn}&checkOut=${busqueda.value.checkOut}&huespedes=${busqueda.value.huespedes}`, { credentials: 'include' })
    if (r.ok) {
      hotel.value = await r.json()
      // Preseleccionar habitación si viene en query
      if (route.query.habitacionId && hotel.value.habitaciones) {
        const h = hotel.value.habitaciones.find(h => h.habitacionId == route.query.habitacionId)
        if (h) seleccionarHab(h)
      }
      const n = busqueda.value.huespedes
      huespedes.value = Array.from({ length: n }, () => huespedVacio())
    } else error.value = 'No se encontró el hotel.'
  } catch { error.value = 'Error de conexión.' }
  finally { loading.value = false }
}

function getCantidad(hab) { return cantidades.value[hab.habitacionId] || 0 }

function cambiarCantidad(hab, delta) {
  const actual = getCantidad(hab)
  const nuevo = Math.max(0, Math.min(actual + delta, hab.cantidadDisponible ?? 99))
  cantidades.value = { ...cantidades.value, [hab.habitacionId]: nuevo }
}

function seleccionarHab(hab) {
  habSeleccionada.value = hab
  if (!getCantidad(hab)) cantidades.value = { ...cantidades.value, [hab.habitacionId]: 1 }
}

function agregarHuesped() {
  if (huespedes.value.length < busqueda.value.huespedes) huespedes.value.push(huespedVacio())
}

function quitarHuesped(idx) { huespedes.value.splice(idx, 1) }

function validar() {
  if (!habSeleccionada.value) { formError.value = 'Selecciona una habitación.'; return false }
  for (let i = 0; i < huespedes.value.length; i++) {
    const h = huespedes.value[i]
    if (!h.nombre.trim() || !h.apellido.trim() || !h.documento.trim()) {
      formError.value = `Completa los campos del huésped ${i+1}.`; return false
    }
  }
  formError.value = ''; return true
}

function continuar() {
  if (!validar()) return
  sessionStorage.setItem('checkout_hotel', JSON.stringify({
    hotelId:    route.params.id,
    hotel:      hotel.value,
    habitacion: habSeleccionada.value,
    cantidad:   getCantidad(habSeleccionada.value),
    checkIn:    busqueda.value.checkIn,
    checkOut:   busqueda.value.checkOut,
    noches:     noches.value,
    huespedes:  huespedes.value,
    total:      totalHotel.value,
  }))
  router.push({ path: '/checkout', query: { tipo: 'hotel' } })
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}
</script>