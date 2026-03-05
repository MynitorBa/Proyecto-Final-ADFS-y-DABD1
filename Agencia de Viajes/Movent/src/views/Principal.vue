<template>
  <div class="page">
    <Encabezado />

    <!-- HERO -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">Tu Próxima Aventura Comienza Aquí</h1>
          <p class="hero-subtitle">Vuelos, hospedajes y paquetes combinados de múltiples proveedores en un solo lugar</p>
        </div>

        <div class="search-card">
          <h2 class="search-card-title">¿A dónde viajamos?</h2>

          <!-- Tabs -->
          <div class="search-tabs">
            <button :class="{ active: searchType === 'flights' }" @click="searchType = 'flights'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelos
            </button>
            <button :class="{ active: searchType === 'hotels' }" @click="searchType = 'hotels'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Hoteles
            </button>
            <button :class="{ active: searchType === 'combo' }" @click="searchType = 'combo'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Vuelo + Hotel
            </button>
          </div>

          <!-- ── TAB VUELOS ── -->
          <template v-if="searchType === 'flights'">
            <div class="trip-type">
              <button :class="{ active: tripType === 'roundtrip' }" @click="tripType = 'roundtrip'" type="button">Ida y Vuelta</button>
              <button :class="{ active: tripType === 'oneway' }" @click="tripType = 'oneway'" type="button">Solo Ida</button>
            </div>
            <div class="form-grid" :style="{ gridTemplateColumns: tripType === 'oneway' ? 'repeat(4, 1fr)' : 'repeat(5, 1fr)' }">
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/></svg>
                  Origen
                </label>
                <input class="form-input" type="text" placeholder="Ciudad de origen"
                  v-model="origenQuery" @input="onOrigenInput" @blur="blurOrigen" autocomplete="off" />
                <ul v-if="origenSugeridos.length" class="inline-autocomplete">
                  <li v-for="c in origenSugeridos" :key="c"><button type="button" @click="seleccionarOrigen(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  Destino
                </label>
                <input class="form-input" type="text" placeholder="Ciudad de destino"
                  v-model="destinoVueloQuery" @input="onDestinoVueloInput" @blur="blurDestinoVuelo" autocomplete="off" />
                <ul v-if="destinoVueloSugeridos.length" class="inline-autocomplete">
                  <li v-for="c in destinoVueloSugeridos" :key="c"><button type="button" @click="seleccionarDestinoVuelo(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Salida
                </label>
                <input class="form-input" type="date" v-model="flightData.fechaIda" />
              </div>
              <div class="form-group" v-if="tripType === 'roundtrip'">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Regreso
                </label>
                <input class="form-input" type="date" v-model="flightData.fechaVuelta" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  Pasajeros
                </label>
                <select class="form-input" v-model="flightData.pasajeros">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Pasajero' : 'Pasajeros' }}</option>
                </select>
              </div>
            </div>
            <div class="seat-class-row">
              <span class="seat-label">Clase:</span>
              <button v-for="c in seatClasses" :key="c.val" type="button"
                :class="['seat-btn', { active: flightData.clase === c.val }]"
                @click="flightData.clase = c.val">{{ c.label }}</button>
            </div>
            <button class="search-btn" type="button" @click="buscarVuelos">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              Buscar Vuelos
            </button>
          </template>

          <!-- ── TAB HOTELES ── -->
          <template v-if="searchType === 'hotels'">
            <div class="form-grid" style="grid-template-columns: repeat(4, 1fr)">
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/></svg>
                  País
                </label>
                <input class="form-input" type="text" placeholder="Escribe el país"
                  v-model="hotelPaisQuery" @input="onHotelPaisInput" @blur="blurHotelPais" autocomplete="off" />
                <div v-if="hotelPaisLoading" class="inline-loading">Buscando...</div>
                <ul v-else-if="hotelPaisesSugeridos.length" class="inline-autocomplete">
                  <li v-for="p in hotelPaisesSugeridos" :key="p.country"><button type="button" @click="seleccionarHotelPais(p)">{{ p.country }}</button></li>
                </ul>
              </div>
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  Ciudad
                  <span v-if="hotelCiudadLoading" style="font-weight:400;color:#94a3b8;font-size:0.65rem;"> cargando...</span>
                </label>
                <input class="form-input" type="text" placeholder="Escribe la ciudad"
                  v-model="hotelCiudadQuery" @input="onHotelCiudadInput" @blur="blurHotelCiudad"
                  :disabled="!hotelPaisSeleccionado || hotelCiudadLoading" autocomplete="off" />
                <ul v-if="hotelCiudadesSugeridas.length" class="inline-autocomplete">
                  <li v-for="c in hotelCiudadesSugeridas" :key="c"><button type="button" @click="seleccionarHotelCiudad(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-in
                </label>
                <input class="form-input" type="date" v-model="hotelData.checkIn" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-out
                </label>
                <input class="form-input" type="date" v-model="hotelData.checkOut" />
              </div>
            </div>
            <div class="seat-class-row">
              <span class="seat-label">Huéspedes:</span>
              <select class="seat-select" v-model="hotelData.huespedes">
                <option v-for="n in 10" :key="n" :value="n">{{ n }}</option>
              </select>
              <span class="seat-label" style="margin-left:1rem">Habitación:</span>
              <button v-for="t in roomTypes" :key="t.val" type="button"
                :class="['seat-btn', { active: hotelData.tipoHabitacion === t.val }]"
                @click="hotelData.tipoHabitacion = t.val">{{ t.label }}</button>
            </div>
            <button class="search-btn" type="button" @click="buscarHoteles">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              Buscar Hoteles
            </button>
          </template>

          <!-- ── TAB COMBINADO ── -->
          <template v-if="searchType === 'combo'">
            <div class="combo-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelo
            </div>
            <div class="form-grid" style="grid-template-columns: repeat(5, 1fr); margin-bottom:0; border-bottom-left-radius:0; border-bottom-right-radius:0; border-bottom: none;">
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/></svg>
                  Origen
                </label>
                <input class="form-input" type="text" placeholder="Ciudad de origen"
                  v-model="comboOrigenQuery" @input="onComboOrigenInput" @blur="blurComboOrigen" autocomplete="off" />
                <ul v-if="comboOrigenSugeridos.length" class="inline-autocomplete">
                  <li v-for="c in comboOrigenSugeridos" :key="c"><button type="button" @click="seleccionarComboOrigen(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  Destino Vuelo
                </label>
                <input class="form-input" type="text" placeholder="Ciudad destino"
                  v-model="comboDestinoQuery" @input="onComboDestinoInput" @blur="blurComboDestino" autocomplete="off" />
                <ul v-if="comboDestinoSugeridos.length" class="inline-autocomplete">
                  <li v-for="c in comboDestinoSugeridos" :key="c"><button type="button" @click="seleccionarComboDestino(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Ida
                </label>
                <input class="form-input" type="date" v-model="comboData.fechaIda" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Vuelta
                </label>
                <input class="form-input" type="date" v-model="comboData.fechaVuelta" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Pasajeros
                </label>
                <select class="form-input" v-model="comboData.pasajeros">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }}</option>
                </select>
              </div>
            </div>

            <div class="combo-label" style="border-top: 1px solid #e2e8f0; margin-top:0; border-radius:0;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Hotel
            </div>
            <div class="form-grid" style="grid-template-columns: repeat(4, 1fr); border-top-left-radius:0; border-top-right-radius:0; border-top: none; margin-bottom: 1.25rem;">
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10z"/></svg>
                  País Hotel
                </label>
                <input class="form-input" type="text" placeholder="País destino"
                  v-model="comboPaisQuery" @input="onComboPaisInput" @blur="blurComboPais" autocomplete="off" />
                <div v-if="comboPaisLoading" class="inline-loading">Buscando...</div>
                <ul v-else-if="comboPaisesSugeridos.length" class="inline-autocomplete">
                  <li v-for="p in comboPaisesSugeridos" :key="p.country"><button type="button" @click="seleccionarComboPais(p)">{{ p.country }}</button></li>
                </ul>
              </div>
              <div class="form-group autocomplete-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  Ciudad Hotel
                  <span v-if="comboCiudadLoading" style="font-weight:400;color:#94a3b8;font-size:0.65rem;"> cargando...</span>
                </label>
                <input class="form-input" type="text" placeholder="Ciudad hotel"
                  v-model="comboCiudadQuery" @input="onComboCiudadInput" @blur="blurComboCiudad"
                  :disabled="!comboPaisSeleccionado || comboCiudadLoading" autocomplete="off" />
                <ul v-if="comboCiudadesSugeridas.length" class="inline-autocomplete">
                  <li v-for="c in comboCiudadesSugeridas" :key="c"><button type="button" @click="seleccionarComboCiudad(c)">{{ c }}</button></li>
                </ul>
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-in
                </label>
                <input class="form-input" type="date" v-model="comboData.checkIn" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-out
                </label>
                <input class="form-input" type="date" v-model="comboData.checkOut" />
              </div>
            </div>

            <div class="seat-class-row">
              <span class="seat-label">Clase vuelo:</span>
              <button v-for="c in seatClasses" :key="c.val" type="button"
                :class="['seat-btn', { active: comboData.clase === c.val }]"
                @click="comboData.clase = c.val">{{ c.label }}</button>
              <span class="seat-label" style="margin-left:1rem">Habitación:</span>
              <button v-for="t in roomTypes" :key="t.val" type="button"
                :class="['seat-btn', { active: comboData.tipoHabitacion === t.val }]"
                @click="comboData.tipoHabitacion = t.val">{{ t.label }}</button>
            </div>
            <button class="search-btn" type="button" @click="buscarPaquetes">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              Buscar Paquete Completo
            </button>
          </template>

        </div>
      </div>
    </section>

    <!-- FEATURES -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">¿Por qué elegir Movent?</h2>
          <p class="section-description">Experiencias de viaje excepcionales con los mejores proveedores del mundo</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="f in features" :key="f.title">
            <div class="feature-icon" v-html="f.icon"></div>
            <h3 class="feature-title">{{ f.title }}</h3>
            <p class="feature-description">{{ f.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">¿Listo para tu próxima aventura?</h2>
          <p class="cta-description">Únete a miles de viajeros que confían en Movent para sus experiencias de viaje</p>
          <div class="cta-buttons">
            <button class="cta-btn primary" type="button" @click="$router.push('/resultados-paquetes')">
              Explorar Paquetes
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </button>
            <button class="cta-btn secondary" type="button" @click="$router.push('/informacion')">Centro de Ayuda</button>
          </div>
        </div>
      </div>
    </section>

    <button v-if="showScrollTop" class="scroll-top" type="button" @click="scrollToTop">
      <svg viewBox="0 0 24 24" class="avion-icon">
        <path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z" />
      </svg>
    </button>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/principal.css'

const router = useRouter()

// ── Opciones ──
const seatClasses = [
  { val: 'economica', label: 'Económica' },
  { val: 'ejecutiva', label: 'Ejecutiva' },
  { val: 'primera',   label: 'Primera Clase' },
]
const roomTypes = [
  { val: 'doble',      label: 'Doble' },
  { val: 'junior',     label: 'Junior Suite' },
  { val: 'suite',      label: 'Suite' },
  { val: 'gran_suite', label: 'Gran Suite' },
]

// ── Tabs ──
const searchType    = ref('flights')
const tripType      = ref('roundtrip')
const showScrollTop = ref(false)

// ── Flight data ──
const flightData = ref({ fechaIda: '', fechaVuelta: '', pasajeros: 1, clase: 'economica' })

// ── Hotel data ──
const hotelData = ref({ checkIn: '', checkOut: '', huespedes: 1, tipoHabitacion: 'doble' })

// ── Combo data ──
const comboData = ref({ fechaIda: '', fechaVuelta: '', pasajeros: 1, clase: 'economica', checkIn: '', checkOut: '', tipoHabitacion: 'doble' })

// ══════════════════════════════
// FUNCIONES DE BÚSQUEDA — navegan a la ruta correcta con query params
// ══════════════════════════════
const buscarVuelos = () => {
  const q = {}
  if (origenQuery.value)        q.origen    = origenQuery.value
  if (destinoVueloQuery.value)  q.destino   = destinoVueloQuery.value
  if (flightData.value.fechaIda)    q.fechaIda  = flightData.value.fechaIda
  if (flightData.value.fechaVuelta) q.fechaVuelta = flightData.value.fechaVuelta
  q.pasajeros = flightData.value.pasajeros
  q.clase     = flightData.value.clase
  q.tipo      = tripType.value
  router.push({ path: '/resultados-vuelos', query: q })
}

const buscarHoteles = () => {
  const q = {}
  if (hotelPaisQuery.value)    q.pais     = hotelPaisQuery.value
  if (hotelCiudadQuery.value)  q.ciudad   = hotelCiudadQuery.value
  if (hotelData.value.checkIn)  q.checkIn  = hotelData.value.checkIn
  if (hotelData.value.checkOut) q.checkOut = hotelData.value.checkOut
  q.huespedes      = hotelData.value.huespedes
  q.tipoHabitacion = hotelData.value.tipoHabitacion
  router.push({ path: '/resultados-hoteles', query: q })
}

const buscarPaquetes = () => {
  const q = {}
  if (comboOrigenQuery.value)   q.origen    = comboOrigenQuery.value
  if (comboDestinoQuery.value)  q.destino   = comboDestinoQuery.value
  if (comboData.value.fechaIda)    q.fechaIda  = comboData.value.fechaIda
  if (comboData.value.fechaVuelta) q.fechaVuelta = comboData.value.fechaVuelta
  if (comboPaisQuery.value)    q.pais     = comboPaisQuery.value
  if (comboCiudadQuery.value)  q.ciudad   = comboCiudadQuery.value
  if (comboData.value.checkIn)  q.checkIn  = comboData.value.checkIn
  if (comboData.value.checkOut) q.checkOut = comboData.value.checkOut
  q.pasajeros      = comboData.value.pasajeros
  q.clase          = comboData.value.clase
  q.tipoHabitacion = comboData.value.tipoHabitacion
  router.push({ path: '/resultados-paquetes', query: q })
}

// ── Caché de países ──
let paisesCache = null
async function getPaises() {
  if (paisesCache) return paisesCache
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries')
    const data = await res.json()
    paisesCache = data.data || []
  } catch { paisesCache = [] }
  return paisesCache
}

async function getCiudades(country) {
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ country })
    })
    const data = await res.json()
    return data.data || []
  } catch { return [] }
}

let todasCiudadesCache = null
async function getTodasCiudades() {
  if (todasCiudadesCache) return todasCiudadesCache
  try {
    const paises = await getPaises()
    todasCiudadesCache = paises.flatMap(p => (p.cities || []).slice(0, 30))
  } catch { todasCiudadesCache = [] }
  return todasCiudadesCache
}

// ── VUELOS: Origen ──
const origenQuery       = ref('')
const origenSugeridos   = ref([])
let origenTimer = null

function onOrigenInput() {
  const q = origenQuery.value.trim()
  if (q.length < 2) { origenSugeridos.value = []; return }
  clearTimeout(origenTimer)
  origenTimer = setTimeout(async () => {
    const cities = await getTodasCiudades()
    origenSugeridos.value = cities.filter(c => c.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
  }, 300)
}
function seleccionarOrigen(c) { origenQuery.value = c; origenSugeridos.value = [] }
function blurOrigen() { setTimeout(() => { origenSugeridos.value = [] }, 200) }

// ── VUELOS: Destino ──
const destinoVueloQuery     = ref('')
const destinoVueloSugeridos = ref([])
let destinoVueloTimer = null

function onDestinoVueloInput() {
  const q = destinoVueloQuery.value.trim()
  if (q.length < 2) { destinoVueloSugeridos.value = []; return }
  clearTimeout(destinoVueloTimer)
  destinoVueloTimer = setTimeout(async () => {
    const cities = await getTodasCiudades()
    destinoVueloSugeridos.value = cities.filter(c => c.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
  }, 300)
}
function seleccionarDestinoVuelo(c) { destinoVueloQuery.value = c; destinoVueloSugeridos.value = [] }
function blurDestinoVuelo() { setTimeout(() => { destinoVueloSugeridos.value = [] }, 200) }

// ── HOTELES: País / Ciudad ──
const hotelPaisQuery        = ref('')
const hotelPaisesSugeridos  = ref([])
const hotelPaisSeleccionado = ref(null)
const hotelPaisLoading      = ref(false)
const hotelCiudadQuery      = ref('')
const hotelCiudadesSugeridas = ref([])
const hotelCiudadLoading    = ref(false)
const hotelTodasCiudades    = ref([])
let hotelPaisTimer = null

function onHotelPaisInput() {
  hotelPaisSeleccionado.value = null
  hotelCiudadQuery.value = ''; hotelCiudadesSugeridas.value = []; hotelTodasCiudades.value = []
  const q = hotelPaisQuery.value.trim()
  if (q.length < 2) { hotelPaisesSugeridos.value = []; return }
  clearTimeout(hotelPaisTimer)
  hotelPaisTimer = setTimeout(async () => {
    hotelPaisLoading.value = true
    const paises = await getPaises()
    hotelPaisesSugeridos.value = paises.filter(p => p.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
    hotelPaisLoading.value = false
  }, 300)
}

async function seleccionarHotelPais(p) {
  hotelPaisSeleccionado.value = p; hotelPaisQuery.value = p.country; hotelPaisesSugeridos.value = []
  hotelCiudadQuery.value = ''; hotelCiudadesSugeridas.value = []
  hotelCiudadLoading.value = true
  hotelTodasCiudades.value = await getCiudades(p.country)
  hotelCiudadLoading.value = false
}

function blurHotelPais() { setTimeout(() => { hotelPaisesSugeridos.value = [] }, 200) }

function onHotelCiudadInput() {
  const q = hotelCiudadQuery.value.toLowerCase().trim()
  hotelCiudadesSugeridas.value = q.length < 2
    ? [] : hotelTodasCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function seleccionarHotelCiudad(c) { hotelCiudadQuery.value = c; hotelCiudadesSugeridas.value = [] }
function blurHotelCiudad() { setTimeout(() => { hotelCiudadesSugeridas.value = [] }, 200) }

// ── COMBO: Origen/Destino vuelo ──
const comboOrigenQuery     = ref('')
const comboOrigenSugeridos = ref([])
let comboOrigenTimer = null

function onComboOrigenInput() {
  const q = comboOrigenQuery.value.trim()
  if (q.length < 2) { comboOrigenSugeridos.value = []; return }
  clearTimeout(comboOrigenTimer)
  comboOrigenTimer = setTimeout(async () => {
    const cities = await getTodasCiudades()
    comboOrigenSugeridos.value = cities.filter(c => c.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
  }, 300)
}
function seleccionarComboOrigen(c) { comboOrigenQuery.value = c; comboOrigenSugeridos.value = [] }
function blurComboOrigen() { setTimeout(() => { comboOrigenSugeridos.value = [] }, 200) }

const comboDestinoQuery     = ref('')
const comboDestinoSugeridos = ref([])
let comboDestinoTimer = null

function onComboDestinoInput() {
  const q = comboDestinoQuery.value.trim()
  if (q.length < 2) { comboDestinoSugeridos.value = []; return }
  clearTimeout(comboDestinoTimer)
  comboDestinoTimer = setTimeout(async () => {
    const cities = await getTodasCiudades()
    comboDestinoSugeridos.value = cities.filter(c => c.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
  }, 300)
}
function seleccionarComboDestino(c) { comboDestinoQuery.value = c; comboDestinoSugeridos.value = [] }
function blurComboDestino() { setTimeout(() => { comboDestinoSugeridos.value = [] }, 200) }

// ── COMBO: País/Ciudad hotel ──
const comboPaisQuery        = ref('')
const comboPaisesSugeridos  = ref([])
const comboPaisSeleccionado = ref(null)
const comboPaisLoading      = ref(false)
const comboCiudadQuery      = ref('')
const comboCiudadesSugeridas = ref([])
const comboCiudadLoading    = ref(false)
const comboTodasCiudades    = ref([])
let comboPaisTimer = null

function onComboPaisInput() {
  comboPaisSeleccionado.value = null
  comboCiudadQuery.value = ''; comboCiudadesSugeridas.value = []; comboTodasCiudades.value = []
  const q = comboPaisQuery.value.trim()
  if (q.length < 2) { comboPaisesSugeridos.value = []; return }
  clearTimeout(comboPaisTimer)
  comboPaisTimer = setTimeout(async () => {
    comboPaisLoading.value = true
    const paises = await getPaises()
    comboPaisesSugeridos.value = paises.filter(p => p.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
    comboPaisLoading.value = false
  }, 300)
}

async function seleccionarComboPais(p) {
  comboPaisSeleccionado.value = p; comboPaisQuery.value = p.country; comboPaisesSugeridos.value = []
  comboCiudadQuery.value = ''; comboCiudadesSugeridas.value = []
  comboCiudadLoading.value = true
  comboTodasCiudades.value = await getCiudades(p.country)
  comboCiudadLoading.value = false
}

function blurComboPais() { setTimeout(() => { comboPaisesSugeridos.value = [] }, 200) }

function onComboCiudadInput() {
  const q = comboCiudadQuery.value.toLowerCase().trim()
  comboCiudadesSugeridas.value = q.length < 2
    ? [] : comboTodasCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function seleccionarComboCiudad(c) { comboCiudadQuery.value = c; comboCiudadesSugeridas.value = [] }
function blurComboCiudad() { setTimeout(() => { comboCiudadesSugeridas.value = [] }, 200) }

// ── Features ──
const features = [
  {
    icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>`,
    title: 'Vuelos Globales',
    description: 'Accede a vuelos de múltiples aerolíneas con las mejores tarifas garantizadas desde cualquier destino.'
  },
  {
    icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
    title: 'Hospedaje Premium',
    description: 'Desde hoteles boutique hasta resorts de lujo, encuentra el alojamiento perfecto para tu viaje.'
  },
  {
    icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    title: 'Atención Experta',
    description: 'Nuestro equipo de asesores está disponible para ayudarte a planificar cada detalle de tu viaje.'
  },
  {
    icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`,
    title: 'Mejor Precio',
    description: 'Comparamos precios de múltiples proveedores para ofrecerte siempre la mejor tarifa disponible.'
  }
]

// ── Scroll ──
const onScroll   = () => { showScrollTop.value = window.scrollY > 300 }
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>