<template>
  <div class="page">
    <Encabezado />

    <div class="rv-page">
      <div class="rv-layout">

        <!-- ═══ SIDEBAR FILTROS ═══ -->
        <aside class="rv-sidebar" :class="{ 'rv-sidebar--open': filtrosAbiertos }">
          <div class="rv-sidebar__head">
            <h3 class="rv-sidebar__title">Filtros</h3>
            <button class="rv-sidebar__close" @click="filtrosAbiertos=false" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <!-- Precio -->
          <div class="rv-filter-group">
            <h4 class="rv-filter-group__title">Rango de precio</h4>
            <div class="rv-price-inputs">
              <div class="rv-price-input">
                <span>$</span>
                <input type="number" v-model.number="filtros.precioMin" :min="0" placeholder="0" />
              </div>
              <span class="rv-price-sep">—</span>
              <div class="rv-price-input">
                <span>$</span>
                <input type="number" v-model.number="filtros.precioMax" placeholder="9999" />
              </div>
            </div>
          </div>

          <!-- Clase -->
          <div class="rv-filter-group">
            <h4 class="rv-filter-group__title">Clase</h4>
            <div class="rv-checkboxes">
              <label class="rv-checkbox" v-for="c in clases" :key="c.val">
                <input type="checkbox" v-model="filtros.clases" :value="c.val" />
                <span class="rv-checkbox__box"></span>
                <span class="rv-checkbox__label">{{ c.label }}</span>
              </label>
            </div>
          </div>

          <!-- Escalas -->
          <div class="rv-filter-group">
            <h4 class="rv-filter-group__title">Escalas</h4>
            <div class="rv-checkboxes">
              <label class="rv-checkbox" v-for="e in escalasOpts" :key="e.val">
                <input type="checkbox" v-model="filtros.escalas" :value="e.val" />
                <span class="rv-checkbox__box"></span>
                <span class="rv-checkbox__label">{{ e.label }}</span>
              </label>
            </div>
          </div>

          <!-- Aerolíneas -->
          <div class="rv-filter-group" v-if="aerolineasDisponibles.length > 0">
            <h4 class="rv-filter-group__title">Aerolínea</h4>
            <div class="rv-checkboxes">
              <label class="rv-checkbox" v-for="a in aerolineasDisponibles" :key="a">
                <input type="checkbox" v-model="filtros.aerolineas" :value="a" />
                <span class="rv-checkbox__box"></span>
                <span class="rv-checkbox__label">{{ a }}</span>
              </label>
            </div>
          </div>

          <!-- Horario -->
          <div class="rv-filter-group">
            <h4 class="rv-filter-group__title">Horario de salida</h4>
            <div class="rv-horarios">
              <button v-for="h in horariosOpts" :key="h.val"
                :class="['rv-horario-btn', { 'rv-horario-btn--active': filtros.horario === h.val }]"
                @click="filtros.horario = filtros.horario === h.val ? '' : h.val" type="button">
                <span>{{ h.icon }}</span>
                <span>{{ h.label }}</span>
                <small>{{ h.rango }}</small>
              </button>
            </div>
          </div>

          <button class="rv-btn rv-btn--ghost rv-sidebar__reset" @click="resetFiltros" type="button">
            Limpiar filtros
          </button>
        </aside>

        <div v-if="filtrosAbiertos" class="rv-sidebar-overlay" @click="filtrosAbiertos=false"></div>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rv-main">

          <!-- Barra búsqueda resumida -->
          <div class="rv-search-bar">
            <div class="rv-search-bar__info">
              <div class="rv-search-bar__ruta">
                <span class="rv-search-bar__iata">{{ busqueda.origen || 'GUA' }}</span>
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                <span class="rv-search-bar__iata">{{ busqueda.destino || 'MIA' }}</span>
              </div>
              <div class="rv-search-bar__detalles">
                <span>{{ formatFecha(busqueda.fechaSalida) }}</span>
                <span v-if="busqueda.fechaRetorno">· Regreso {{ formatFecha(busqueda.fechaRetorno) }}</span>
                <span>· {{ busqueda.pasajeros || 1 }} pasajero{{ (busqueda.pasajeros||1)!==1?'s':'' }}</span>
                <span>· {{ busqueda.clase || 'Económica' }}</span>
              </div>
            </div>
            <div class="rv-search-bar__actions">
              <button class="rv-btn rv-btn--outline" @click="$router.push('/principal')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Modificar
              </button>
              <button class="rv-btn rv-btn--yellow rv-search-bar__filter-btn" @click="filtrosAbiertos=true" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="11" y1="18" x2="13" y2="18"/></svg>
                Filtros
              </button>
            </div>
          </div>

          <!-- Toolbar -->
          <div class="rv-toolbar">
            <p class="rv-toolbar__count">
              <strong>{{ vuelosFiltrados.length }}</strong> vuelo{{ vuelosFiltrados.length!==1?'s':'' }} encontrado{{ vuelosFiltrados.length!==1?'s':'' }}
            </p>
            <div class="rv-sort">
              <label class="rv-sort__label">Ordenar:</label>
              <select v-model="ordenar" class="rv-sort__select">
                <option value="precio-asc">Precio: menor a mayor</option>
                <option value="precio-desc">Precio: mayor a menor</option>
                <option value="duracion">Duración</option>
                <option value="salida">Hora de salida</option>
              </select>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="rv-empty">
            <div class="rv-spinner"></div>
            <p>Buscando vuelos disponibles...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="rv-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5" stroke-linecap="round"/><circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/></svg>
            <p>{{ error }}</p>
            <button class="rv-btn rv-btn--yellow" @click="cargarVuelos" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="vuelosFiltrados.length === 0" class="rv-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="52" height="52"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
            <p class="rv-empty__title">Sin vuelos disponibles</p>
            <p class="rv-empty__sub">Intenta ajustar los filtros o cambia las fechas</p>
            <button class="rv-btn rv-btn--ghost" @click="resetFiltros" type="button">Limpiar filtros</button>
          </div>

          <!-- Lista -->
          <div v-else class="rv-lista">
            <article v-for="vuelo in vuelosFiltrados" :key="vuelo.id"
              class="rv-card" :class="{ 'rv-card--seleccionado': seleccionado === vuelo.id }">

              <div class="rv-card__head">
                <div class="rv-card__aerolinea">
                  <div class="rv-card__logo">
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  </div>
                  <div>
                    <span class="rv-card__aerolinea-nombre">{{ vuelo.aerolinea }}</span>
                    <span class="rv-card__vuelo-num">{{ vuelo.numeroVuelo }}</span>
                  </div>
                </div>
                <div class="rv-card__tags">
                  <span class="rv-tag rv-tag--clase">{{ vuelo.clase }}</span>
                  <span v-if="vuelo.escalas === 0" class="rv-tag rv-tag--directo">Directo</span>
                  <span v-else class="rv-tag rv-tag--escala">{{ vuelo.escalas }} escala{{ vuelo.escalas!==1?'s':'' }}</span>
                </div>
              </div>

              <div class="rv-card__ruta">
                <div class="rv-card__punto">
                  <span class="rv-card__iata">{{ vuelo.origenCodigo }}</span>
                  <span class="rv-card__ciudad">{{ vuelo.origenCiudad }}</span>
                  <span class="rv-card__hora">{{ vuelo.horaSalida }}</span>
                </div>

                <div class="rv-card__medio">
                  <span class="rv-card__dur">{{ formatDuracion(vuelo.duracionMinutos) }}</span>
                  <div class="rv-card__track">
                    <div class="rv-card__track-dot"></div>
                    <div class="rv-card__track-line"></div>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="20" height="20" class="rv-card__track-avion"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <div class="rv-card__track-line"></div>
                    <div class="rv-card__track-dot"></div>
                  </div>
                  <div v-if="vuelo.escalas > 0 && vuelo.paradas" class="rv-card__paradas">
                    <span v-for="p in vuelo.paradas" :key="p.codigo" class="rv-card__parada">
                      {{ p.codigo }} ({{ p.espera }})
                    </span>
                  </div>
                </div>

                <div class="rv-card__punto rv-card__punto--r">
                  <span class="rv-card__iata">{{ vuelo.destinoCodigo }}</span>
                  <span class="rv-card__ciudad">{{ vuelo.destinoCiudad }}</span>
                  <span class="rv-card__hora">{{ vuelo.horaLlegada }}</span>
                </div>
              </div>

              <div class="rv-card__foot">
                <div class="rv-card__foot-meta">
                  <span class="rv-card__avion-txt">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                    {{ vuelo.avionMarca }} {{ vuelo.avionModelo }}
                  </span>
                  <span class="rv-card__asientos" :class="{ 'rv-card__asientos--bajo': (vuelo.asientosDisponibles??99) <= 5 }">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    {{ (vuelo.asientosDisponibles??99) <= 5 ? `¡Solo ${vuelo.asientosDisponibles} lugares!` : `${vuelo.asientosDisponibles ?? '--'} asientos` }}
                  </span>
                </div>
                <div class="rv-card__precio-wrap">
                  <div class="rv-card__precio-bloque">
                    <span class="rv-card__precio-lbl">por persona</span>
                    <span class="rv-card__precio">${{ vuelo.precio?.toFixed(2) }}</span>
                  </div>
                  <button class="rv-btn rv-btn--yellow rv-card__cta" @click="seleccionarVuelo(vuelo)" type="button">
                    Seleccionar
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
import '../styles/resultadosvuelos.css'

const router = useRouter()
const route  = useRoute()
const API    = 'http://localhost:7000'

const vuelos          = ref([])
const loading         = ref(true)
const error           = ref('')
const seleccionado    = ref(null)
const filtrosAbiertos = ref(false)
const ordenar         = ref('precio-asc')

const busqueda = ref({
  origen:       route.query.origen       || '',
  destino:      route.query.destino      || '',
  fechaSalida:  route.query.fechaSalida  || '',
  fechaRetorno: route.query.fechaRetorno || '',
  pasajeros:    Number(route.query.pasajeros) || 1,
  clase:        route.query.clase        || 'Económica',
  tipo:         route.query.tipo         || 'directo',
})

const filtros = ref({
  precioMin:  0,
  precioMax:  9999,
  clases:     [],
  escalas:    [],
  aerolineas: [],
  horario:    '',
})

const clases = [
  { val: 'Económica', label: 'Económica' },
  { val: 'Ejecutiva', label: 'Ejecutiva' },
]

const escalasOpts = [
  { val: 0, label: 'Directo' },
  { val: 1, label: '1 escala' },
  { val: 2, label: '2+ escalas' },
]

const horariosOpts = [
  { val: 'madrugada', icon: '🌙', label: 'Madrugada', rango: '00:00–05:59' },
  { val: 'mañana',    icon: '🌅', label: 'Mañana',    rango: '06:00–11:59' },
  { val: 'tarde',     icon: '☀️',  label: 'Tarde',     rango: '12:00–17:59' },
  { val: 'noche',     icon: '🌆', label: 'Noche',     rango: '18:00–23:59' },
]

const VUELOS_DEMO = [
  {
    id: 1,
    aerolinea: 'Avianca',
    numeroVuelo: 'AV-341',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'MIA', destinoCiudad: 'Miami',
    horaSalida: '07:30', horaLlegada: '10:45',
    duracionMinutos: 195,
    clase: 'Económica', escalas: 0,
    precio: 320.00,
    asientosDisponibles: 18,
    avionMarca: 'Boeing', avionModelo: '737-800',
  },
  {
    id: 2,
    aerolinea: 'Copa Airlines',
    numeroVuelo: 'CM-202',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'PTY', destinoCiudad: 'Ciudad de Panamá',
    horaSalida: '14:00', horaLlegada: '15:50',
    duracionMinutos: 110,
    clase: 'Económica', escalas: 0,
    precio: 210.00,
    asientosDisponibles: 4,
    avionMarca: 'Boeing', avionModelo: '737 MAX',
  },
  {
    id: 3,
    aerolinea: 'American Airlines',
    numeroVuelo: 'AA-917',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'JFK', destinoCiudad: 'Nueva York',
    horaSalida: '06:15', horaLlegada: '14:30',
    duracionMinutos: 495,
    clase: 'Económica', escalas: 1,
    precio: 445.00,
    asientosDisponibles: 22,
    avionMarca: 'Airbus', avionModelo: 'A321',
    paradas: [{ codigo: 'MIA', espera: '1h 45m' }],
  },
  {
    id: 4,
    aerolinea: 'United Airlines',
    numeroVuelo: 'UA-538',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'LAX', destinoCiudad: 'Los Ángeles',
    horaSalida: '09:45', horaLlegada: '21:10',
    duracionMinutos: 565,
    clase: 'Económica', escalas: 1,
    precio: 512.00,
    asientosDisponibles: 9,
    avionMarca: 'Boeing', avionModelo: '787',
    paradas: [{ codigo: 'IAH', espera: '2h 20m' }],
  },
  {
    id: 5,
    aerolinea: 'Avianca',
    numeroVuelo: 'AV-782',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'MAD', destinoCiudad: 'Madrid',
    horaSalida: '22:00', horaLlegada: '17:45',
    duracionMinutos: 1065,
    clase: 'Ejecutiva', escalas: 2,
    precio: 1280.00,
    asientosDisponibles: 6,
    avionMarca: 'Airbus', avionModelo: 'A330',
    paradas: [{ codigo: 'BOG', espera: '1h 30m' }, { codigo: 'LIS', espera: '1h 10m' }],
  },
  {
    id: 6,
    aerolinea: 'Copa Airlines',
    numeroVuelo: 'CM-415',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'GRU', destinoCiudad: 'São Paulo',
    horaSalida: '11:20', horaLlegada: '06:05',
    duracionMinutos: 885,
    clase: 'Económica', escalas: 1,
    precio: 670.00,
    asientosDisponibles: 14,
    avionMarca: 'Boeing', avionModelo: '737-800',
    paradas: [{ codigo: 'PTY', espera: '2h 00m' }],
  },
  {
    id: 7,
    aerolinea: 'Iberia',
    numeroVuelo: 'IB-6403',
    origenCodigo: 'GUA', origenCiudad: 'Ciudad de Guatemala',
    destinoCodigo: 'BCN', destinoCiudad: 'Barcelona',
    horaSalida: '18:30', horaLlegada: '21:15',
    duracionMinutos: 1245,
    clase: 'Ejecutiva', escalas: 2,
    precio: 1890.00,
    asientosDisponibles: 3,
    avionMarca: 'Airbus', avionModelo: 'A350',
    paradas: [{ codigo: 'MIA', espera: '2h 10m' }, { codigo: 'MAD', espera: '1h 25m' }],
  },
]

const aerolineasDisponibles = computed(() => [...new Set(vuelos.value.map(v => v.aerolinea).filter(Boolean))])

const vuelosFiltrados = computed(() => {
  let list = vuelos.value

  if (filtros.value.precioMin > 0)
    list = list.filter(v => v.precio >= filtros.value.precioMin)
  if (filtros.value.precioMax < 9999)
    list = list.filter(v => v.precio <= filtros.value.precioMax)
  if (filtros.value.clases.length > 0)
    list = list.filter(v => filtros.value.clases.includes(v.clase))
  if (filtros.value.escalas.length > 0)
    list = list.filter(v => {
      const sel = filtros.value.escalas
      if (sel.includes(2)) return sel.includes(v.escalas) || v.escalas >= 2
      return sel.includes(v.escalas)
    })
  if (filtros.value.aerolineas.length > 0)
    list = list.filter(v => filtros.value.aerolineas.includes(v.aerolinea))
  if (filtros.value.horario) {
    const rangos = { madrugada:[0,6], mañana:[6,12], tarde:[12,18], noche:[18,24] }
    const [min, max] = rangos[filtros.value.horario]
    list = list.filter(v => {
      const h = parseInt(v.horaSalida?.split(':')[0] ?? 0)
      return h >= min && h < max
    })
  }

  return [...list].sort((a, b) => {
    if (ordenar.value === 'precio-asc')  return a.precio - b.precio
    if (ordenar.value === 'precio-desc') return b.precio - a.precio
    if (ordenar.value === 'duracion')    return (a.duracionMinutos||0) - (b.duracionMinutos||0)
    if (ordenar.value === 'salida')      return (a.horaSalida||'').localeCompare(b.horaSalida||'')
    return 0
  })
})

onMounted(() => cargarVuelos())

async function cargarVuelos() {
  loading.value = true; error.value = ''
  try {
    const params = new URLSearchParams()
    Object.entries(busqueda.value).forEach(([k, v]) => { if (v) params.append(k, v) })
    const r = await fetch(`${API}/api/vuelos/buscar?${params}`, { credentials: 'include' })
    if (r.ok) {
      const data = await r.json()
      vuelos.value = data.length ? data : VUELOS_DEMO
    } else {
      vuelos.value = VUELOS_DEMO
    }
  } catch {
    vuelos.value = VUELOS_DEMO
  } finally { loading.value = false }
}

function seleccionarVuelo(vuelo) {
  seleccionado.value = vuelo.id
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
  sessionStorage.setItem('vuelo_seleccionado', JSON.stringify(vuelo))
  router.push('/reservar')
}

function resetFiltros() {
  filtros.value = { precioMin:0, precioMax:9999, clases:[], escalas:[], aerolineas:[], horario:'' }
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}

function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}`
}
</script>