<template>
  <div class="page">
    <Encabezado />

    <!-- HERO -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">Tu Próxima Aventura<br>Comienza <span>Aquí</span></h1>
          <p class="hero-subtitle">Vuelos, hospedajes y paquetes combinados de múltiples proveedores en un solo lugar</p>
          <div class="hero-stats">
            <div class="hero-stat">
              <strong>500+</strong>
              <span>Aerolíneas</span>
            </div>
            <div class="hero-stat">
              <strong>12K+</strong>
              <span>Hoteles</span>
            </div>
            <div class="hero-stat">
              <strong>180+</strong>
              <span>Países</span>
            </div>
            <div class="hero-stat">
              <strong>98%</strong>
              <span>Satisfacción</span>
            </div>
          </div>
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

          <!-- ══ SELECTORES COMPARTIDOS: Origen + Destino ══ -->
          <div :class="['vuelos-cards', { 'vuelos-cards--solo': searchType === 'hotels' }]">

            <!-- DESDE (vuelos y combo) -->
            <div class="vuelo-card" v-if="searchType !== 'hotels'">
              <div class="vuelo-card__label">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                Desde
              </div>
              <div class="campo autocomplete-wrap">
                <label class="form-label">País</label>
                <input class="form-input campo-input" type="text" v-model="oPaisQ"
                  @input="onOPaisInput" @blur="blur(() => oPaisSug = [])"
                  placeholder="Guatemala..." autocomplete="off" />
                <ul v-if="oPaisSug.length" class="inline-autocomplete">
                  <li v-for="p in oPaisSug" :key="p.country">
                    <button type="button" @click="selOPais(p)">{{ p.country }}</button>
                  </li>
                </ul>
              </div>
              <div class="campo autocomplete-wrap">
                <label class="form-label">
                  Ciudad
                  <span v-if="oCiudadLoading" class="form-label__hint"> cargando...</span>
                </label>
                <input class="form-input campo-input" type="text" v-model="oCiudadQ"
                  @input="onOCiudadInput" @blur="blur(() => oCiudadSug = [])"
                  :disabled="!oPaisSel || oCiudadLoading"
                  placeholder="Guatemala City..." autocomplete="off" />
                <ul v-if="oCiudadSug.length" class="inline-autocomplete">
                  <li v-for="c in oCiudadSug" :key="c">
                    <button type="button" @click="selOCiudad(c)">{{ c }}</button>
                  </li>
                </ul>
              </div>
            </div>

            <!-- HACIA / DESTINO (todos los tabs) -->
            <div :class="['vuelo-card', { 'vuelo-card--full': searchType === 'hotels' }]">
              <div class="vuelo-card__label">
                <template v-if="searchType === 'hotels'">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  Destino
                </template>
                <template v-else>
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M2.5,19H21.5V21H2.5V19M9.68,13.27L14.03,14.43L20.8,16.26C21.56,16.45 22,16.17 22,15.62C22,15.26 21.78,14.88 21.37,14.68L16.5,12.22L12.03,3H9.7L12,12.28L7.45,11L5.92,7.5H4.04L5.42,12C5.7,13 6.68,13.53 7.62,13.27"/></svg>
                  Hacia
                </template>
              </div>
              <div :class="searchType === 'hotels' ? 'destino-fila' : ''">
                <div class="campo autocomplete-wrap">
                  <label class="form-label">País</label>
                  <input class="form-input campo-input" type="text" v-model="dPaisQ"
                    @input="onDPaisInput" @blur="blur(() => dPaisSug = [])"
                    placeholder="Mexico..." autocomplete="off" />
                  <ul v-if="dPaisSug.length" class="inline-autocomplete">
                    <li v-for="p in dPaisSug" :key="p.country">
                      <button type="button" @click="selDPais(p)">{{ p.country }}</button>
                    </li>
                  </ul>
                </div>
                <div class="campo autocomplete-wrap">
                  <label class="form-label">
                    Ciudad
                    <span v-if="dCiudadLoading" class="form-label__hint"> cargando...</span>
                  </label>
                  <input class="form-input campo-input" type="text" v-model="dCiudadQ"
                    @input="onDCiudadInput" @blur="blur(() => dCiudadSug = [])"
                    :disabled="!dPaisSel || dCiudadLoading"
                    placeholder="Mexico City..." autocomplete="off" />
                  <ul v-if="dCiudadSug.length" class="inline-autocomplete">
                    <li v-for="c in dCiudadSug" :key="c">
                      <button type="button" @click="selDCiudad(c)">{{ c }}</button>
                    </li>
                  </ul>
                </div>
              </div>
            </div>

          </div>
          <!-- ══ FIN SELECTORES COMPARTIDOS ══ -->

          <!-- ── TAB VUELOS ── -->
          <template v-if="searchType === 'flights'">
            <div class="form-grid" style="grid-template-columns: repeat(2, 1fr)">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha
                </label>
                <input class="form-input" type="date" v-model="flightData.fecha" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  Pasajeros
                </label>
                <select class="form-input" v-model="flightData.cantidadPasajeros">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Pasajero' : 'Pasajeros' }}</option>
                </select>
              </div>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarVuelos" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Vuelos' }}
            </button>
          </template>

          <!-- ── TAB HOTELES ── -->
          <template v-if="searchType === 'hotels'">
            <div class="form-grid" style="grid-template-columns: repeat(3, 1fr)">
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
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Huéspedes
                </label>
                <select class="form-input" v-model="hotelData.huespedes">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Huésped' : 'Huéspedes' }}</option>
                </select>
              </div>
            </div>
            <div class="seat-class-row">
              <span class="seat-label">Habitación:</span>
              <button v-for="t in roomTypes" :key="t.val" type="button"
                :class="['seat-btn', { active: hotelData.tipoHabitacion === t.val }]"
                @click="hotelData.tipoHabitacion = t.val">{{ t.label }}</button>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarHoteles" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Hoteles' }}
            </button>
          </template>

          <!-- ── TAB COMBO ── -->
          <template v-if="searchType === 'combo'">
            <div class="combo-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelo
            </div>
            <div class="form-grid" style="grid-template-columns: repeat(2, 1fr); margin-bottom:0; border-bottom-left-radius:0; border-bottom-right-radius:0; border-bottom: none;">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha
                </label>
                <input class="form-input" type="date" v-model="comboData.fecha" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Pasajeros
                </label>
                <select class="form-input" v-model="comboData.cantidadPasajeros">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }}</option>
                </select>
              </div>
            </div>
            <div class="combo-label" style="border-top: 1px solid #e2e8f0; margin-top:0; border-radius:0;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Hotel
            </div>
            <div class="form-grid" style="grid-template-columns: repeat(2, 1fr); border-top-left-radius:0; border-top-right-radius:0; border-top: none; margin-bottom: 1.25rem;">
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
              <span class="seat-label">Habitación:</span>
              <button v-for="t in roomTypes" :key="t.val" type="button"
                :class="['seat-btn', { active: comboData.tipoHabitacion === t.val }]"
                @click="comboData.tipoHabitacion = t.val">{{ t.label }}</button>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarPaquetes" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Paquete Completo' }}
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

const router      = useRouter()
const API         = 'http://localhost:8080'
const buscando    = ref(false)
const searchError = ref('')

// ── Opciones ──────────────────────────────────────────────────
const roomTypes = [
  { val: 'doble',      label: 'Doble' },
  { val: 'junior',     label: 'Junior Suite' },
  { val: 'suite',      label: 'Suite' },
  { val: 'gran_suite', label: 'Gran Suite' },
]

// ── UI ────────────────────────────────────────────────────────
const searchType    = ref('flights')
const showScrollTop = ref(false)

// ── Datos por tab ─────────────────────────────────────────────
const flightData = ref({ fecha: '', cantidadPasajeros: 1 })
const hotelData  = ref({ checkIn: '', checkOut: '', huespedes: 1, tipoHabitacion: 'doble' })
const comboData  = ref({ fecha: '', cantidadPasajeros: 1, checkIn: '', checkOut: '', tipoHabitacion: 'doble' })

// ══════════════════════════════════════════════════════════════
// countriesnow.space — países y ciudades
// ══════════════════════════════════════════════════════════════
let paisesCache = null
async function getPaises() {
  if (paisesCache) return paisesCache
  try {
    const r = await fetch('https://countriesnow.space/api/v0.1/countries')
    const d = await r.json()
    paisesCache = d.data || []
  } catch { paisesCache = [] }
  return paisesCache
}
async function getCiudades(country) {
  try {
    const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ country })
    })
    const d = await r.json()
    return d.data || []
  } catch { return [] }
}

function blur(fn) { setTimeout(fn, 200) }

// ══════════════════════════════════════════════════════════════
// ORIGEN — compartido (vuelos + combo)
// ══════════════════════════════════════════════════════════════
const oPaisQ         = ref('');  const oPaisSug        = ref([]);  const oPaisSel       = ref(null)
const oCiudadQ       = ref('');  const oCiudadSug      = ref([]);  const oCiudadLoading = ref(false)
const oCiudades      = ref([])
const origen         = ref({ pais: '', ciudad: '' })

async function onOPaisInput() {
  oPaisSel.value = null; oCiudadQ.value = ''; oCiudades.value = []
  origen.value = { pais: '', ciudad: '' }
  const q = oPaisQ.value.trim()
  if (q.length < 2) { oPaisSug.value = []; return }
  const p = await getPaises()
  oPaisSug.value = p.filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selOPais(p) {
  oPaisSel.value = p; oPaisQ.value = p.country; oPaisSug.value = []
  origen.value.pais = p.country
  oCiudadLoading.value = true
  oCiudades.value = await getCiudades(p.country)
  oCiudadLoading.value = false
}
function onOCiudadInput() {
  const q = oCiudadQ.value.toLowerCase()
  oCiudadSug.value = q.length < 2 ? [] : oCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  origen.value.ciudad = ''
}
function selOCiudad(c) {
  oCiudadQ.value = c; oCiudadSug.value = []
  origen.value.ciudad = c
  searchError.value = ''
}

// ══════════════════════════════════════════════════════════════
// DESTINO — compartido (vuelos + hoteles + combo)
// ══════════════════════════════════════════════════════════════
const dPaisQ         = ref('');  const dPaisSug        = ref([]);  const dPaisSel       = ref(null)
const dCiudadQ       = ref('');  const dCiudadSug      = ref([]);  const dCiudadLoading = ref(false)
const dCiudades      = ref([])
const destino        = ref({ pais: '', ciudad: '' })

async function onDPaisInput() {
  dPaisSel.value = null; dCiudadQ.value = ''; dCiudades.value = []
  destino.value = { pais: '', ciudad: '' }
  const q = dPaisQ.value.trim()
  if (q.length < 2) { dPaisSug.value = []; return }
  const p = await getPaises()
  dPaisSug.value = p.filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selDPais(p) {
  dPaisSel.value = p; dPaisQ.value = p.country; dPaisSug.value = []
  destino.value.pais = p.country
  dCiudadLoading.value = true
  dCiudades.value = await getCiudades(p.country)
  dCiudadLoading.value = false
}
function onDCiudadInput() {
  const q = dCiudadQ.value.toLowerCase()
  dCiudadSug.value = q.length < 2 ? [] : dCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  destino.value.ciudad = ''
}
function selDCiudad(c) {
  dCiudadQ.value = c; dCiudadSug.value = []
  destino.value.ciudad = c
  searchError.value = ''
}

// ══════════════════════════════════════════════════════════════
// BÚSQUEDAS
// ══════════════════════════════════════════════════════════════

// ── Vuelos ────────────────────────────────────────────────────
const buscarVuelos = async () => {
  searchError.value = ''
  if (!origen.value.pais || !origen.value.ciudad) {
    searchError.value = 'Selecciona el país y ciudad de origen.'; return
  }
  if (!destino.value.pais || !destino.value.ciudad) {
    searchError.value = 'Selecciona el país y ciudad de destino.'; return
  }
  if (!flightData.value.fecha) {
    searchError.value = 'Selecciona una fecha de vuelo.'; return
  }

  buscando.value = true
  try {
    const res = await fetch(`${API}/api/busqueda/vuelos`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        origen:            origen.value.ciudad,
        origenPais:        origen.value.pais,
        destino:           destino.value.ciudad,
        destinoPais:       destino.value.pais,
        fecha:             flightData.value.fecha,
        cantidadPasajeros: flightData.value.cantidadPasajeros,
      })
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const resultados = await res.json()
    router.push({
      path: '/resultados-vuelos',
      state: {
        resultados,
        busqueda: {
          origen:            origen.value.ciudad,
          origenPais:        origen.value.pais,
          destino:           destino.value.ciudad,
          destinoPais:       destino.value.pais,
          fecha:             flightData.value.fecha,
          cantidadPasajeros: flightData.value.cantidadPasajeros,
        }
      }
    })
  } catch (err) {
    console.error('Error buscando vuelos:', err)
    searchError.value = 'No se pudieron obtener vuelos. Intenta de nuevo.'
  } finally {
    buscando.value = false
  }
}

// ── Hoteles ───────────────────────────────────────────────────
const buscarHoteles = async () => {
  searchError.value = ''
  if (!destino.value.pais || !destino.value.ciudad) {
    searchError.value = 'Selecciona el país y ciudad de destino.'; return
  }

  buscando.value = true
  try {
    const res = await fetch(`${API}/api/busqueda/hoteles`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        destino:        destino.value.ciudad,
        destinoPais:    destino.value.pais,
        checkIn:        hotelData.value.checkIn  || null,
        checkOut:       hotelData.value.checkOut || null,
        huespedes:      hotelData.value.huespedes,
        tipoHabitacion: hotelData.value.tipoHabitacion,
      })
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const resultados = await res.json()
    router.push({
      path: '/resultados-hoteles',
      state: {
        resultados,
        busqueda: {
          destino:        destino.value.ciudad,
          destinoPais:    destino.value.pais,
          checkIn:        hotelData.value.checkIn,
          checkOut:       hotelData.value.checkOut,
          huespedes:      hotelData.value.huespedes,
          tipoHabitacion: hotelData.value.tipoHabitacion,
        }
      }
    })
  } catch (err) {
    console.error('Error buscando hoteles:', err)
    searchError.value = 'No se pudieron obtener hoteles. Intenta de nuevo.'
  } finally {
    buscando.value = false
  }
}

// ── Paquetes ──────────────────────────────────────────────────
const buscarPaquetes = async () => {
  searchError.value = ''
  if (!origen.value.pais || !origen.value.ciudad) {
    searchError.value = 'Selecciona el país y ciudad de origen.'; return
  }
  if (!destino.value.pais || !destino.value.ciudad) {
    searchError.value = 'Selecciona el país y ciudad de destino.'; return
  }

  buscando.value = true
  try {
    const [resVuelos, resHoteles] = await Promise.all([
      fetch(`${API}/api/busqueda/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          origen:            origen.value.ciudad,
          origenPais:        origen.value.pais,
          destino:           destino.value.ciudad,
          destinoPais:       destino.value.pais,
          fecha:             comboData.value.fecha,
          cantidadPasajeros: comboData.value.cantidadPasajeros,
        })
      }),
      fetch(`${API}/api/busqueda/hoteles`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          destino:        destino.value.ciudad,
          destinoPais:    destino.value.pais,
          checkIn:        comboData.value.checkIn  || null,
          checkOut:       comboData.value.checkOut || null,
          huespedes:      comboData.value.cantidadPasajeros,
          tipoHabitacion: comboData.value.tipoHabitacion,
        })
      })
    ])

    const resultadosVuelos  = resVuelos.ok  ? await resVuelos.json()  : []
    const resultadosHoteles = resHoteles.ok ? await resHoteles.json() : []

    router.push({
      path: '/resultados-paquetes',
      state: {
        resultadosVuelos,
        resultadosHoteles,
        busqueda: {
          origen:            origen.value.ciudad,
          origenPais:        origen.value.pais,
          destino:           destino.value.ciudad,
          destinoPais:       destino.value.pais,
          fecha:             comboData.value.fecha,
          cantidadPasajeros: comboData.value.cantidadPasajeros,
          checkIn:           comboData.value.checkIn,
          checkOut:          comboData.value.checkOut,
          tipoHabitacion:    comboData.value.tipoHabitacion,
        }
      }
    })
  } catch (err) {
    console.error('Error buscando paquetes:', err)
    searchError.value = 'No se pudieron obtener los paquetes. Intenta de nuevo.'
  } finally {
    buscando.value = false
  }
}

// ── Features ──────────────────────────────────────────────────
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

// ── Scroll ────────────────────────────────────────────────────
const onScroll    = () => { showScrollTop.value = window.scrollY > 300 }
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>