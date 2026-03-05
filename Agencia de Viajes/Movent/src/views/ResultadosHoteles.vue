<template>
  <div class="page">
    <Encabezado />

    <div class="rh-page">
      <div class="rh-layout">

        <!-- ═══ SIDEBAR FILTROS ═══ -->
        <aside class="rh-sidebar" :class="{ 'rh-sidebar--open': filtrosAbiertos }">
          <div class="rh-sidebar__head">
            <h3 class="rh-sidebar__title">Filtros</h3>
            <button class="rh-sidebar__close" @click="filtrosAbiertos=false" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <!-- Precio por noche -->
          <div class="rh-filter-group">
            <h4 class="rh-filter-group__title">Precio por noche</h4>
            <div class="rh-price-inputs">
              <div class="rh-price-input">
                <span>$</span>
                <input type="number" v-model.number="filtros.precioMin" :min="0" placeholder="0" />
              </div>
              <span class="rh-price-sep">—</span>
              <div class="rh-price-input">
                <span>$</span>
                <input type="number" v-model.number="filtros.precioMax" placeholder="9999" />
              </div>
            </div>
          </div>

          <!-- Tipo habitación -->
          <div class="rh-filter-group">
            <h4 class="rh-filter-group__title">Tipo de habitación</h4>
            <div class="rh-checkboxes">
              <label class="rh-checkbox" v-for="t in tiposHabitacion" :key="t.val">
                <input type="checkbox" v-model="filtros.tipos" :value="t.val" />
                <span class="rh-checkbox__box"></span>
                <span class="rh-checkbox__label">{{ t.label }}</span>
              </label>
            </div>
          </div>

          <!-- Hoteles -->
          <div class="rh-filter-group" v-if="hotelesDisponibles.length > 0">
            <h4 class="rh-filter-group__title">Hotel</h4>
            <div class="rh-checkboxes">
              <label class="rh-checkbox" v-for="h in hotelesDisponibles" :key="h">
                <input type="checkbox" v-model="filtros.hoteles" :value="h" />
                <span class="rh-checkbox__box"></span>
                <span class="rh-checkbox__label">{{ h }}</span>
              </label>
            </div>
          </div>

          <!-- Capacidad -->
          <div class="rh-filter-group">
            <h4 class="rh-filter-group__title">Capacidad mínima</h4>
            <div class="rh-capacidad">
              <button class="rh-cap-btn" @click="filtros.capacidad = Math.max(1, filtros.capacidad-1)" type="button">−</button>
              <span class="rh-cap-val">{{ filtros.capacidad }} persona{{ filtros.capacidad!==1?'s':'' }}</span>
              <button class="rh-cap-btn" @click="filtros.capacidad++" type="button">+</button>
            </div>
          </div>

          <button class="rh-btn rh-btn--ghost rh-sidebar__reset" @click="resetFiltros" type="button">
            Limpiar filtros
          </button>
        </aside>

        <div v-if="filtrosAbiertos" class="rh-sidebar-overlay" @click="filtrosAbiertos=false"></div>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rh-main">

          <!-- Barra búsqueda resumida -->
          <div class="rh-search-bar">
            <div class="rh-search-bar__info">
              <div class="rh-search-bar__destino">
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3" fill="#1C1A18"/></svg>
                <span class="rh-search-bar__ciudad">{{ busqueda.destino || 'Ciudad de destino' }}</span>
              </div>
              <div class="rh-search-bar__detalles">
                <span>Check-in {{ formatFecha(busqueda.checkIn) }}</span>
                <span>· Check-out {{ formatFecha(busqueda.checkOut) }}</span>
                <span>· {{ calcNoches(busqueda.checkIn, busqueda.checkOut) }} noches</span>
                <span>· {{ busqueda.huespedes || 1 }} huésped{{ (busqueda.huespedes||1)!==1?'es':'' }}</span>
              </div>
            </div>
            <div class="rh-search-bar__actions">
              <button class="rh-btn rh-btn--outline" @click="$router.push('/principal')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Modificar
              </button>
              <button class="rh-btn rh-btn--yellow rh-search-bar__filter-btn" @click="filtrosAbiertos=true" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="11" y1="18" x2="13" y2="18"/></svg>
                Filtros
              </button>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="rh-toolbar">
            <p class="rh-toolbar__count">
              <strong>{{ habitacionesFiltradas.length }}</strong> habitación{{ habitacionesFiltradas.length!==1?'es':'' }} disponible{{ habitacionesFiltradas.length!==1?'s':'' }}
            </p>
            <div class="rh-sort">
              <label class="rh-sort__label">Ordenar:</label>
              <select v-model="ordenar" class="rh-sort__select">
                <option value="precio-asc">Precio: menor a mayor</option>
                <option value="precio-desc">Precio: mayor a menor</option>
                <option value="capacidad">Capacidad</option>
              </select>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="rh-empty">
            <div class="rh-spinner"></div>
            <p>Buscando hospedajes disponibles...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5" stroke-linecap="round"/><circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/></svg>
            <p>{{ error }}</p>
            <button class="rh-btn rh-btn--yellow" @click="cargarHoteles" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="habitacionesFiltradas.length === 0" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="52" height="52"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            <p class="rh-empty__title">Sin hospedajes disponibles</p>
            <p class="rh-empty__sub">Intenta ajustar los filtros o cambia las fechas</p>
            <button class="rh-btn rh-btn--ghost" @click="resetFiltros" type="button">Limpiar filtros</button>
          </div>

          <!-- Lista agrupada por hotel -->
          <template v-else>
            <div v-for="grupo in gruposPorHotel" :key="grupo.hotelId" class="rh-grupo">

              <!-- Header hotel -->
              <div class="rh-grupo__head">
                <div class="rh-grupo__hotel-info">
                  <div class="rh-grupo__hotel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  </div>
                  <div>
                    <h3 class="rh-grupo__nombre">{{ grupo.nombreHotel }}</h3>
                    <p class="rh-grupo__ubicacion">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                      {{ grupo.ciudad }}, {{ grupo.pais }}
                    </p>
                  </div>
                </div>
                <span class="rh-grupo__count">{{ grupo.habitaciones.length }} tipo{{ grupo.habitaciones.length!==1?'s':'' }}</span>
              </div>

              <!-- Habitaciones -->
              <div class="rh-habitaciones">
                <article v-for="hab in grupo.habitaciones" :key="hab.habitacionId"
                  class="rh-card" :class="{ 'rh-card--seleccionada': seleccionada === hab.habitacionId }">

                  <div class="rh-card__img">
                    <div class="rh-card__img-placeholder">
                      <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.35)" stroke-width="1" width="40" height="40"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    </div>
                    <div class="rh-card__tipo-badge">{{ hab.tipoHabitacion }}</div>
                  </div>

                  <div class="rh-card__info">
                    <div class="rh-card__info-top">
                      <div>
                        <h4 class="rh-card__nombre">{{ hab.tipoHabitacion }}</h4>
                        <p class="rh-card__cama">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                          {{ hab.tipoCama }}
                        </p>
                      </div>
                      <div class="rh-card__precio-bloque">
                        <span class="rh-card__precio-lbl">por noche</span>
                        <span class="rh-card__precio">${{ hab.precioPorNoche?.toFixed(2) }}</span>
                        <span class="rh-card__precio-total">
                          {{ calcNoches(busqueda.checkIn, busqueda.checkOut) }} noches:
                          <strong>${{ ((hab.precioPorNoche||0) * calcNoches(busqueda.checkIn, busqueda.checkOut)).toFixed(2) }}</strong>
                        </span>
                      </div>
                    </div>

                    <p class="rh-card__desc">{{ hab.descripcion }}</p>

                    <div v-if="hab.amenidades?.length" class="rh-card__amenidades">
                      <span v-for="am in hab.amenidades.slice(0,5)" :key="am" class="rh-amenidad">{{ am }}</span>
                      <span v-if="hab.amenidades.length > 5" class="rh-amenidad rh-amenidad--mas">+{{ hab.amenidades.length - 5 }} más</span>
                    </div>

                    <div class="rh-card__meta">
                      <span>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                        Hasta {{ hab.capacidad }} persona{{ hab.capacidad!==1?'s':'' }}
                      </span>
                      <span v-if="hab.metrosCuadrados">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
                        {{ hab.metrosCuadrados }} m²
                      </span>
                      <span class="rh-card__disponibles" :class="{ 'rh-card__disponibles--bajo': (hab.cantidadDisponible??99) <= 3 }">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        {{ (hab.cantidadDisponible??99) <= 3 ? `¡Solo ${hab.cantidadDisponible} disponible${hab.cantidadDisponible!==1?'s':''}!` : `${hab.cantidadDisponible ?? '--'} disponibles` }}
                      </span>
                    </div>

                    <button class="rh-btn rh-btn--yellow rh-card__cta"
                      @click="seleccionarHabitacion(hab, grupo)" type="button">
                      Reservar habitación
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>

                </article>
              </div>

            </div>
          </template>

        </div>
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
import '../styles/resultadoshoteles.css'

const router = useRouter()
const route  = useRoute()
const API    = 'http://localhost:7000'

const habitaciones    = ref([])
const loading         = ref(true)
const error           = ref('')
const seleccionada    = ref(null)
const filtrosAbiertos = ref(false)
const ordenar         = ref('precio-asc')

const busqueda = ref({
  destino:   route.query.destino  || '',
  checkIn:   route.query.checkIn  || '',
  checkOut:  route.query.checkOut || '',
  huespedes: Number(route.query.huespedes) || 1,
})

const filtros = ref({
  precioMin:  0,
  precioMax:  9999,
  tipos:      [],
  hoteles:    [],
  capacidad:  1,
})

const tiposHabitacion = [
  { val: 'Doble',        label: 'Doble' },
  { val: 'Junior Suite', label: 'Junior Suite' },
  { val: 'Suite',        label: 'Suite' },
  { val: 'Gran Suite',   label: 'Gran Suite' },
]

const HOTELES_DEMO = [
  {
    habitacionId: 101,
    hotelId: 1,
    nombreHotel: 'Hotel Casa Santo Domingo',
    ciudad: 'Antigua Guatemala',
    pais: 'Guatemala',
    tipoHabitacion: 'Doble',
    tipoCama: 'Cama King',
    capacidad: 2,
    precioPorNoche: 850,
    cantidadDisponible: 3,
    descripcion: 'Habitación colonial con vista al jardín y baño de mármol.',
    amenidades: ['WiFi', 'A/C', 'TV', 'Minibar', 'Caja fuerte'],
    metrosCuadrados: 32,
  },
  {
    habitacionId: 102,
    hotelId: 1,
    nombreHotel: 'Hotel Casa Santo Domingo',
    ciudad: 'Antigua Guatemala',
    pais: 'Guatemala',
    tipoHabitacion: 'Suite',
    tipoCama: 'Cama King + sofá cama',
    capacidad: 3,
    precioPorNoche: 1400,
    cantidadDisponible: 1,
    descripcion: 'Suite de lujo con jacuzzi, sala de estar y vistas al volcán Agua.',
    amenidades: ['WiFi', 'A/C', 'TV 55"', 'Jacuzzi', 'Minibar', 'Servicio a la habitación'],
    metrosCuadrados: 58,
  },
  {
    habitacionId: 201,
    hotelId: 2,
    nombreHotel: 'Barceló Guatemala City',
    ciudad: 'Ciudad de Guatemala',
    pais: 'Guatemala',
    tipoHabitacion: 'Doble',
    tipoCama: 'Cama Queen',
    capacidad: 2,
    precioPorNoche: 520,
    cantidadDisponible: 6,
    descripcion: 'Habitación moderna con escritorio ejecutivo y vistas a la ciudad.',
    amenidades: ['WiFi', 'A/C', 'TV', 'Escritorio'],
    metrosCuadrados: 28,
  },
  {
    habitacionId: 202,
    hotelId: 2,
    nombreHotel: 'Barceló Guatemala City',
    ciudad: 'Ciudad de Guatemala',
    pais: 'Guatemala',
    tipoHabitacion: 'Junior Suite',
    tipoCama: 'Cama King',
    capacidad: 3,
    precioPorNoche: 780,
    cantidadDisponible: 2,
    descripcion: 'Junior suite con sala de estar independiente y acceso a piscina.',
    amenidades: ['WiFi', 'A/C', 'TV', 'Piscina', 'Gimnasio'],
    metrosCuadrados: 44,
  },
]

const hotelesDisponibles = computed(() =>
  [...new Set(habitaciones.value.map(h => h.nombreHotel).filter(Boolean))]
)

const habitacionesFiltradas = computed(() => {
  let list = habitaciones.value

  if (filtros.value.precioMin > 0)
    list = list.filter(h => h.precioPorNoche >= filtros.value.precioMin)
  if (filtros.value.precioMax < 9999)
    list = list.filter(h => h.precioPorNoche <= filtros.value.precioMax)
  if (filtros.value.tipos.length > 0)
    list = list.filter(h => filtros.value.tipos.includes(h.tipoHabitacion))
  if (filtros.value.hoteles.length > 0)
    list = list.filter(h => filtros.value.hoteles.includes(h.nombreHotel))
  if (filtros.value.capacidad > 1)
    list = list.filter(h => (h.capacidad ?? 1) >= filtros.value.capacidad)

  return [...list].sort((a, b) => {
    if (ordenar.value === 'precio-asc')  return a.precioPorNoche - b.precioPorNoche
    if (ordenar.value === 'precio-desc') return b.precioPorNoche - a.precioPorNoche
    if (ordenar.value === 'capacidad')   return (b.capacidad||0) - (a.capacidad||0)
    return 0
  })
})

const gruposPorHotel = computed(() => {
  const map = new Map()
  for (const h of habitacionesFiltradas.value) {
    if (!map.has(h.hotelId)) {
      map.set(h.hotelId, {
        hotelId:      h.hotelId,
        nombreHotel:  h.nombreHotel,
        ciudad:       h.ciudad,
        pais:         h.pais,
        habitaciones: [],
      })
    }
    map.get(h.hotelId).habitaciones.push(h)
  }
  return Array.from(map.values())
})

onMounted(() => cargarHoteles())

async function cargarHoteles() {
  loading.value = true; error.value = ''
  try {
    const params = new URLSearchParams()
    Object.entries(busqueda.value).forEach(([k, v]) => { if (v) params.append(k, v) })
    const r = await fetch(`${API}/api/hoteles/buscar?${params}`, { credentials: 'include' })
    if (r.ok) {
      const data = await r.json()
      habitaciones.value = data.length ? data : HOTELES_DEMO
    } else {
      habitaciones.value = HOTELES_DEMO
    }
  } catch {
    habitaciones.value = HOTELES_DEMO
  } finally { loading.value = false }
}

function seleccionarHabitacion(hab, grupo) {
  seleccionada.value = hab.habitacionId
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
  sessionStorage.setItem('hotel_seleccionado', JSON.stringify({ ...hab, ...grupo, habitaciones: undefined }))
  router.push('/reservar')
}

function resetFiltros() {
  filtros.value = { precioMin:0, precioMax:9999, tipos:[], hoteles:[], capacidad:1 }
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}

function calcNoches(ci, co) {
  if (!ci || !co) return 0
  return Math.max(0, Math.ceil((new Date(co) - new Date(ci)) / 86400000))
}
</script>