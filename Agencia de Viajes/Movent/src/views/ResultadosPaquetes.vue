<template>
  <div class="page">
    <Encabezado />

    <div class="rp-page">
      <div class="rp-layout">

        <!-- ═══ SIDEBAR FILTROS ═══ -->
        <aside class="rp-sidebar" :class="{ 'rp-sidebar--open': sidebarOpen }">
          <div class="rp-sidebar__head">
            <h3 class="rp-sidebar__title">Filtros</h3>
            <button class="rp-sidebar__close" @click="sidebarOpen = false" type="button">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <!-- Precio -->
          <div class="rp-filter-group">
            <h4 class="rp-filter-group__title">Precio del paquete</h4>
            <div class="rp-price-inputs">
              <div class="rp-price-input"><span>Q</span><input type="number" v-model="filtros.precioMin" placeholder="Mín" min="0" /></div>
              <span class="rp-price-sep">—</span>
              <div class="rp-price-input"><span>Q</span><input type="number" v-model="filtros.precioMax" placeholder="Máx" min="0" /></div>
            </div>
          </div>

          <!-- Destino -->
          <div class="rp-filter-group">
            <h4 class="rp-filter-group__title">Destino</h4>
            <div class="rp-checkboxes">
              <label v-for="d in destinosUnicos" :key="d" class="rp-checkbox">
                <input type="checkbox" :value="d" v-model="filtros.destinos" />
                <span class="rp-checkbox__box"></span>
                <span class="rp-checkbox__label">{{ d }}</span>
              </label>
            </div>
          </div>

          <!-- Aerolínea -->
          <div class="rp-filter-group">
            <h4 class="rp-filter-group__title">Aerolínea</h4>
            <div class="rp-checkboxes">
              <label v-for="a in aerolineasUnicas" :key="a" class="rp-checkbox">
                <input type="checkbox" :value="a" v-model="filtros.aerolineas" />
                <span class="rp-checkbox__box"></span>
                <span class="rp-checkbox__label">{{ a }}</span>
              </label>
            </div>
          </div>

          <!-- Escalas -->
          <div class="rp-filter-group">
            <h4 class="rp-filter-group__title">Escalas del vuelo</h4>
            <div class="rp-checkboxes">
              <label class="rp-checkbox">
                <input type="checkbox" :value="0" v-model="filtros.escalas" />
                <span class="rp-checkbox__box"></span>
                <span class="rp-checkbox__label">Directo</span>
              </label>
              <label class="rp-checkbox">
                <input type="checkbox" :value="1" v-model="filtros.escalas" />
                <span class="rp-checkbox__box"></span>
                <span class="rp-checkbox__label">1 escala</span>
              </label>
              <label class="rp-checkbox">
                <input type="checkbox" :value="2" v-model="filtros.escalas" />
                <span class="rp-checkbox__box"></span>
                <span class="rp-checkbox__label">2+ escalas</span>
              </label>
            </div>
          </div>

          <!-- Noches -->
          <div class="rp-filter-group">
            <h4 class="rp-filter-group__title">Noches de hospedaje</h4>
            <div class="rp-noches">
              <button class="rp-cap-btn" @click="filtros.nochesMin = Math.max(1, filtros.nochesMin - 1)" type="button">−</button>
              <span class="rp-cap-val">{{ filtros.nochesMin }}+ noches</span>
              <button class="rp-cap-btn" @click="filtros.nochesMin++" type="button">+</button>
            </div>
          </div>

          <button class="rp-btn rp-btn--ghost rp-sidebar__reset" @click="resetFiltros" type="button">Limpiar filtros</button>
        </aside>

        <div v-if="sidebarOpen" class="rp-sidebar-overlay" @click="sidebarOpen = false"></div>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rp-main">

          <!-- Barra búsqueda resumida -->
          <div class="rp-search-bar">
            <div class="rp-search-bar__info">
              <!-- Fila 1: título -->
              <div class="rp-search-bar__titulo-row">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="#FFCC00"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                <span class="rp-search-bar__titulo">Paquetes Turísticos</span>
              </div>
              <!-- Fila 2: ruta origen → destino -->
              <div class="rp-search-bar__ruta" v-if="busqueda.origen || busqueda.destino">
                <span class="rp-search-bar__iata">{{ busqueda.origen || '---' }}</span>
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                <span class="rp-search-bar__iata">{{ busqueda.destino || '---' }}</span>
              </div>
              <!-- Fila 3: detalles -->
              <div class="rp-search-bar__detalles">
                <span v-if="busqueda.checkIn">{{ formatFecha(busqueda.checkIn) }} — {{ formatFecha(busqueda.checkOut) }}</span>
                <span v-if="busqueda.pasajeros">· {{ busqueda.pasajeros }} pasajero{{ busqueda.pasajeros > 1 ? 's' : '' }}</span>
                <span v-if="busqueda.clase">· {{ busqueda.clase }}</span>
                <span v-if="busqueda.tipoHabitacion">· {{ busqueda.tipoHabitacion }}</span>
              </div>
            </div>
            <div class="rp-search-bar__actions">
              <button class="rp-btn rp-btn--outline" @click="$router.push('/principal')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Modificar
              </button>
              <button class="rp-btn rp-btn--yellow rp-search-bar__filter-btn" @click="sidebarOpen = true" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="11" y1="18" x2="13" y2="18"/></svg>
                Filtros
              </button>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="rp-toolbar">
            <p class="rp-toolbar__count">
              <strong>{{ paquetesFiltrados.length }}</strong> paquete{{ paquetesFiltrados.length !== 1 ? 's' : '' }} encontrado{{ paquetesFiltrados.length !== 1 ? 's' : '' }}
            </p>
            <div class="rp-sort">
              <label class="rp-sort__label">Ordenar:</label>
              <select v-model="orden" class="rp-sort__select">
                <option value="precio_asc">Precio: menor a mayor</option>
                <option value="precio_desc">Precio: mayor a menor</option>
                <option value="ahorro">Mayor ahorro</option>
                <option value="noches">Más noches</option>
              </select>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="cargando" class="rp-empty">
            <div class="rp-spinner"></div>
            <p class="rp-empty__title">Buscando paquetes...</p>
            <p class="rp-empty__sub">Combinando vuelos y hospedajes para ti</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="rp-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5" stroke-linecap="round"/><circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/></svg>
            <p class="rp-empty__title">{{ error }}</p>
            <button class="rp-btn rp-btn--yellow" @click="cargarPaquetes" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="paquetesFiltrados.length === 0" class="rp-empty">
            <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            <p class="rp-empty__title">Sin paquetes disponibles</p>
            <p class="rp-empty__sub">Intenta ajustar los filtros o cambia las fechas</p>
            <button class="rp-btn rp-btn--ghost" @click="resetFiltros" type="button">Limpiar filtros</button>
          </div>

          <!-- ── CARDS ── -->
          <div v-else class="rp-lista">
            <article v-for="paquete in paquetesFiltrados" :key="paquete.id"
              class="rp-card" @click="verDetalle(paquete)">

              <!-- Franja superior negra -->
              <div class="rp-card__franja">
                <div class="rp-card__franja-left">
                  <span class="rp-card__tag rp-card__tag--paquete">Paquete completo</span>
                  <h3 class="rp-card__nombre">{{ paquete.nombre }}</h3>
                </div>
                <div class="rp-card__ahorro" v-if="calcAhorro(paquete) > 0">
                  <span class="rp-card__ahorro-lbl">Ahorras</span>
                  <span class="rp-card__ahorro-val">Q{{ calcAhorro(paquete).toLocaleString() }}</span>
                </div>
              </div>

              <!-- Body vuelo | hotel -->
              <div class="rp-card__body">
                <!-- Vuelo -->
                <div class="rp-card__seccion">
                  <div class="rp-card__seccion-icon">
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  </div>
                  <div class="rp-card__seccion-info">
                    <span class="rp-card__seccion-lbl">Vuelo incluido</span>
                    <div class="rp-card__ruta">
                      <span class="rp-card__iata">{{ paquete.vuelo?.origenCodigo || '---' }}</span>
                      <div class="rp-card__ruta-track">
                        <div class="rp-card__ruta-dot"></div>
                        <div class="rp-card__ruta-line"></div>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <div class="rp-card__ruta-line"></div>
                        <div class="rp-card__ruta-dot"></div>
                      </div>
                      <span class="rp-card__iata">{{ paquete.vuelo?.destinoCodigo || '---' }}</span>
                    </div>
                    <span class="rp-card__seccion-meta">
                      {{ paquete.vuelo?.aerolinea }} · {{ paquete.vuelo?.numeroVuelo }}
                      · <span :style="paquete.vuelo?.escalas === 0 ? 'color:#22c55e;font-weight:700' : 'color:#f59e0b;font-weight:700'">
                          {{ paquete.vuelo?.escalas === 0 ? 'Directo' : paquete.vuelo?.escalas === 1 ? '1 escala' : `${paquete.vuelo?.escalas} escalas` }}
                        </span>
                    </span>
                  </div>
                </div>

                <!-- Divisor -->
                <div class="rp-card__divisor"></div>

                <!-- Hotel -->
                <div class="rp-card__seccion">
                  <div class="rp-card__seccion-icon rp-card__seccion-icon--hotel">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#1C1A18" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  </div>
                  <div class="rp-card__seccion-info">
                    <span class="rp-card__seccion-lbl">Hospedaje incluido</span>
                    <span class="rp-card__seccion-val">{{ paquete.hotel?.nombre || 'Hotel incluido' }}</span>
                    <span class="rp-card__seccion-meta">{{ paquete.hotel?.ciudad }} · {{ paquete.noches || '?' }} noches · {{ paquete.hotel?.tipoHabitacion }}</span>
                  </div>
                </div>
              </div>

              <!-- Footer -->
              <div class="rp-card__foot">
                <div class="rp-card__foot-meta">
                  <span class="rp-card__precio-individual">Individual: <s>Q{{ calcPrecioIndividual(paquete).toLocaleString() }}</s></span>
                </div>
                <div class="rp-card__precio-wrap">
                  <div class="rp-card__precio-bloque">
                    <span class="rp-card__precio-lbl">por persona</span>
                    <span class="rp-card__precio">Q{{ paquete.precioEspecial?.toLocaleString() || '---' }}</span>
                  </div>
                  <button class="rp-btn rp-btn--yellow rp-card__cta" @click.stop="verDetalle(paquete)" type="button">
                    Ver paquete
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                  </button>
                </div>
              </div>

            </article>
          </div>

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
import '../styles/resultadospaquetes.css'

const router = useRouter()
const route  = useRoute()
const API    = 'http://localhost:7000'

const paquetes    = ref([])
const cargando    = ref(true)
const error       = ref('')
const sidebarOpen = ref(false)
const orden       = ref('precio_asc')

const busqueda = ref({
  origen:          route.query.origen          || '',
  destino:         route.query.destino         || '',
  fechaIda:        route.query.fechaIda        || '',
  fechaVuelta:     route.query.fechaVuelta     || '',
  pais:            route.query.pais            || '',
  ciudad:          route.query.ciudad          || '',
  checkIn:         route.query.checkIn         || '',
  checkOut:        route.query.checkOut        || '',
  pasajeros:       Number(route.query.pasajeros) || 1,
  clase:           route.query.clase           || '',
  tipoHabitacion:  route.query.tipoHabitacion  || '',
})

const filtros = ref({
  precioMin:  '',
  precioMax:  '',
  destinos:   [],
  aerolineas: [],
  escalas:    [],
  nochesMin:  1,
})

// ── Computed ─────────────────────────────────────
const destinosUnicos = computed(() =>
  [...new Set(paquetes.value.map(p => p.vuelo?.destinoCiudad).filter(Boolean))]
)

const aerolineasUnicas = computed(() =>
  [...new Set(paquetes.value.map(p => p.vuelo?.aerolinea).filter(Boolean))]
)

const paquetesFiltrados = computed(() => {
  let lista = [...paquetes.value]

  if (filtros.value.precioMin !== '')
    lista = lista.filter(p => p.precioEspecial >= Number(filtros.value.precioMin))
  if (filtros.value.precioMax !== '')
    lista = lista.filter(p => p.precioEspecial <= Number(filtros.value.precioMax))
  if (filtros.value.destinos.length)
    lista = lista.filter(p => filtros.value.destinos.includes(p.vuelo?.destinoCiudad))
  if (filtros.value.aerolineas.length)
    lista = lista.filter(p => filtros.value.aerolineas.includes(p.vuelo?.aerolinea))
  if (filtros.value.escalas.length)
    lista = lista.filter(p => {
      const e = p.vuelo?.escalas ?? 0
      return filtros.value.escalas.some(s => s === 2 ? e >= 2 : s === e)
    })
  if (filtros.value.nochesMin > 1)
    lista = lista.filter(p => (p.noches || 0) >= filtros.value.nochesMin)

  return [...lista].sort((a, b) => {
    if (orden.value === 'precio_asc')  return a.precioEspecial - b.precioEspecial
    if (orden.value === 'precio_desc') return b.precioEspecial - a.precioEspecial
    if (orden.value === 'ahorro')      return calcAhorro(b) - calcAhorro(a)
    if (orden.value === 'noches')      return (b.noches || 0) - (a.noches || 0)
    return 0
  })
})

// ── Helpers ──────────────────────────────────────
const calcPrecioIndividual = (p) =>
  (p.vuelo?.precioBase || 0) + (p.hotel?.precioPorNoche || 0) * (p.noches || 1)

const calcAhorro = (p) =>
  Math.max(0, calcPrecioIndividual(p) - (p.precioEspecial || 0))

const formatFecha = (f) => {
  if (!f) return ''
  return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' })
}

const resetFiltros = () => {
  filtros.value = { precioMin: '', precioMax: '', destinos: [], aerolineas: [], escalas: [], nochesMin: 1 }
}

const verDetalle = (paquete) => {
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.setItem('paquete_seleccionado', JSON.stringify(paquete))
  router.push('/reservar')
}

// ── API ──────────────────────────────────────────
const PAQUETES_DEMO = [
  {
    id: 1,
    nombre: 'Escapada Caribeña — Cancún',
    precioEspecial: 4850,
    noches: 7,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'CUN',
      origenCiudad: 'Guatemala', destinoCiudad: 'Cancún',
      aerolinea: 'Avianca', numeroVuelo: 'AV-341',
      escalas: 0, precioBase: 2600,
    },
    hotel: {
      nombre: 'Grand Coral Beach Resort',
      ciudad: 'Cancún', tipoHabitacion: 'Suite',
      precioPorNoche: 480,
    },
  },
  {
    id: 2,
    nombre: 'City Break — Ciudad de México',
    precioEspecial: 2600,
    noches: 4,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'MEX',
      origenCiudad: 'Guatemala', destinoCiudad: 'Ciudad de México',
      aerolinea: 'Interjet', numeroVuelo: 'IN-210',
      escalas: 0, precioBase: 1600,
    },
    hotel: {
      nombre: 'Hotel Histórico Centro',
      ciudad: 'CDMX', tipoHabitacion: 'Doble',
      precioPorNoche: 350,
    },
  },
  {
    id: 3,
    nombre: 'Romance en Miami — Fin de semana',
    precioEspecial: 3200,
    noches: 3,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'MIA',
      origenCiudad: 'Guatemala', destinoCiudad: 'Miami',
      aerolinea: 'Copa Airlines', numeroVuelo: 'CM-508',
      escalas: 0, precioBase: 2100,
    },
    hotel: {
      nombre: 'Fontainebleau Miami Beach',
      ciudad: 'Miami', tipoHabitacion: 'King Deluxe',
      precioPorNoche: 620,
    },
  },
  {
    id: 4,
    nombre: 'Aventura en Bogotá — Cultural',
    precioEspecial: 1950,
    noches: 5,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'BOG',
      origenCiudad: 'Guatemala', destinoCiudad: 'Bogotá',
      aerolinea: 'Avianca', numeroVuelo: 'AV-622',
      escalas: 1, precioBase: 1200,
    },
    hotel: {
      nombre: 'Hotel Casa Dann Carlton',
      ciudad: 'Bogotá', tipoHabitacion: 'Doble Superior',
      precioPorNoche: 210,
    },
  },
  {
    id: 5,
    nombre: 'Europa Clásica — Madrid',
    precioEspecial: 8900,
    noches: 10,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'MAD',
      origenCiudad: 'Guatemala', destinoCiudad: 'Madrid',
      aerolinea: 'Iberia', numeroVuelo: 'IB-6401',
      escalas: 2, precioBase: 6200,
    },
    hotel: {
      nombre: 'Hotel Puerta América',
      ciudad: 'Madrid', tipoHabitacion: 'Suite Junior',
      precioPorNoche: 380,
    },
  },
  {
    id: 6,
    nombre: 'Sol y Arena — Punta Cana',
    precioEspecial: 5600,
    noches: 6,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'PUJ',
      origenCiudad: 'Guatemala', destinoCiudad: 'Punta Cana',
      aerolinea: 'Copa Airlines', numeroVuelo: 'CM-314',
      escalas: 1, precioBase: 2800,
    },
    hotel: {
      nombre: 'Hard Rock Hotel Punta Cana',
      ciudad: 'Punta Cana', tipoHabitacion: 'Junior Suite',
      precioPorNoche: 550,
    },
  },
  {
    id: 7,
    nombre: 'Lima Express — Gastronomía',
    precioEspecial: 3400,
    noches: 5,
    vuelo: {
      origenCodigo: 'GUA', destinoCodigo: 'LIM',
      origenCiudad: 'Guatemala', destinoCiudad: 'Lima',
      aerolinea: 'LATAM', numeroVuelo: 'LA-784',
      escalas: 1, precioBase: 2200,
    },
    hotel: {
      nombre: 'Belmond Miraflores Park',
      ciudad: 'Lima', tipoHabitacion: 'Doble Deluxe',
      precioPorNoche: 300,
    },
  },
]

const cargarPaquetes = async () => {
  cargando.value = true
  error.value    = ''
  try {
    const params = new URLSearchParams()
    Object.entries(busqueda.value).forEach(([k, v]) => { if (v) params.append(k, v) })
    const r = await fetch(`${API}/api/paquetes/buscar?${params}`, { credentials: 'include' })
    if (r.ok) {
      const data = await r.json()
      paquetes.value = data.length ? data : PAQUETES_DEMO
    } else {
      paquetes.value = PAQUETES_DEMO
    }
  } catch {
    paquetes.value = PAQUETES_DEMO
  } finally {
    cargando.value = false
  }
}

onMounted(cargarPaquetes)
</script>