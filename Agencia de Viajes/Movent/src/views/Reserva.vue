<template>
  <div class="page">
    <Encabezado />

    <div class="res-page">
      <div class="res-container">

        <!-- Header -->
        <div class="res-header">

          <div class="res-header__text">
            <h1 class="res-header__title">Completar Reserva</h1>
            <p class="res-header__sub">Completa los datos del pasajero para continuar</p>
          </div>
        </div>

        <div class="res-layout">

          <!-- ═══ IZQUIERDA: FORMULARIO ═══ -->
          <div class="res-form-col">
            <div class="res-form-card">
              <div class="res-form-card__head">
                <div class="res-form-card__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <h2 class="res-form-card__title">Datos del Pasajero</h2>
              </div>

              <div class="res-form-card__body">

                <!-- Nombre + Apellido -->
                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">Nombre(s) *</label>
                    <input class="res-field__input" type="text" v-model="form.nombre"
                      placeholder="Ej: Carlos Andrés" autocomplete="off" />
                    <span v-if="errors.nombre" class="res-field__error">{{ errors.nombre }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">Apellido(s) *</label>
                    <input class="res-field__input" type="text" v-model="form.apellido"
                      placeholder="Ej: López García" autocomplete="off" />
                    <span v-if="errors.apellido" class="res-field__error">{{ errors.apellido }}</span>
                  </div>
                </div>

                <!-- Pasaporte + Fecha nacimiento -->
                <div class="res-form-row">
                  <div class="res-field">
                    <label class="res-field__label">Número de Pasaporte *</label>
                    <input class="res-field__input" type="text" v-model="form.pasaporte"
                      placeholder="Solo números" autocomplete="off"
                      @input="form.pasaporte = form.pasaporte.replace(/\D/g, '')" />
                    <span v-if="errors.pasaporte" class="res-field__error">{{ errors.pasaporte }}</span>
                  </div>
                  <div class="res-field">
                    <label class="res-field__label">Fecha de Nacimiento *</label>
                    <input class="res-field__input" type="date" v-model="form.fechaNacimiento" />
                    <span v-if="errors.fechaNacimiento" class="res-field__error">{{ errors.fechaNacimiento }}</span>
                  </div>
                </div>

                <!-- País -->
                <div class="res-form-row">
                  <div class="res-field res-field--full">
                    <label class="res-field__label">País de residencia *</label>
                    <div class="res-autocomplete">
                      <input class="res-field__input" type="text" v-model="paisQuery"
                        @input="onPaisInput" @blur="validarPais"
                        placeholder="Escribe tu país..." autocomplete="off" />
                      <ul v-if="paisesSugeridos.length" class="res-autocomplete__list">
                        <li v-for="p in paisesSugeridos" :key="p.country"
                          class="res-autocomplete__item"
                          @mousedown.prevent="seleccionarPais(p)">
                          {{ p.country }}
                        </li>
                      </ul>
                    </div>
                    <span v-if="errors.pais" class="res-field__error">{{ errors.pais }}</span>
                  </div>
                  <div class="res-field res-field--full">
                    <label class="res-field__label">Ciudad *</label>
                    <div class="res-autocomplete">
                      <input class="res-field__input" type="text" v-model="ciudadQuery"
                        @input="onCiudadInput" @blur="validarCiudad"
                        :placeholder="paisSeleccionado ? 'Escribe tu ciudad...' : 'Selecciona un país primero'"
                        :disabled="!paisSeleccionado" autocomplete="off" />
                      <ul v-if="ciudadesSugeridas.length" class="res-autocomplete__list">
                        <li v-for="c in ciudadesSugeridas" :key="c"
                          class="res-autocomplete__item"
                          @mousedown.prevent="seleccionarCiudad(c)">
                          {{ c }}
                        </li>
                      </ul>
                    </div>
                    <span v-if="errors.ciudad" class="res-field__error">{{ errors.ciudad }}</span>
                  </div>
                </div>

                <!-- Teléfono -->
                <div class="res-form-row">
                  <div class="res-field res-field--full">
                    <label class="res-field__label">
                      Teléfono de contacto *
                      <span v-if="dialCode" class="res-field__hint">— {{ phoneDigits }} dígitos locales</span>
                    </label>
                    <div class="res-phone" :class="{ 'res-phone--error': errors.telefono }">
                      <span v-if="dialCode" class="res-phone__prefix">{{ dialCode }}</span>
                      <input class="res-field__input" type="tel" v-model="form.telefono"
                        @input="onPhoneInput"
                        :placeholder="dialCode ? phonePlaceholder : 'Selecciona un país primero'"
                        :disabled="!dialCode" autocomplete="off" />
                    </div>
                    <span v-if="form.telefono && !errors.telefono && dialCode" class="res-field__ok">
                      {{ telefonoDigitos === phoneDigits ? '✓ Número completo' : telefonoDigitos + '/' + phoneDigits + ' dígitos' }}
                    </span>
                    <span v-if="errors.telefono" class="res-field__error">{{ errors.telefono }}</span>
                  </div>
                  <div class="res-field res-field--full">
                    <label class="res-field__label">Correo electrónico *</label>
                    <input class="res-field__input" type="email" v-model="form.email"
                      placeholder="ejemplo@correo.com" autocomplete="off" />
                    <span v-if="errors.email" class="res-field__error">{{ errors.email }}</span>
                  </div>
                </div>

                <!-- Nota -->
                <div class="res-field res-field--full" style="margin-top:8px">
                  <label class="res-field__label">Nota adicional <span class="res-field__optional">(opcional)</span></label>
                  <textarea class="res-field__textarea" v-model="form.nota"
                    placeholder="Peticiones especiales, alergias, asistencia, etc." rows="3"></textarea>
                </div>

              </div>
            </div>
          </div>

          <!-- ═══ DERECHA: RESUMEN ═══ -->
          <aside class="res-summary-col">
            <div class="res-summary">
              <div class="res-summary__head">
                <h2 class="res-summary__title">Resumen de tu reserva</h2>
              </div>

              <!-- Sin item seleccionado -->
              <div v-if="!item" class="res-summary__empty">
                <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="1.5" width="40" height="40"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                <p>No hay ningún item seleccionado.</p>
                <button class="res-btn res-btn--ghost" @click="$router.push('/principal')" type="button">Buscar viajes</button>
              </div>

              <!-- VUELO -->
              <template v-else-if="tipoItem === 'vuelo'">
                <div class="res-summary__tag res-summary__tag--vuelo">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  Vuelo
                </div>
                <div class="res-summary__aerolinea">
                  <strong>{{ item.aerolinea }}</strong>
                  <span class="res-summary__num">{{ item.numeroVuelo }}</span>
                </div>
                <div class="res-summary__ruta">
                  <div class="res-summary__punto">
                    <span class="res-summary__iata">{{ item.origenCodigo }}</span>
                    <span class="res-summary__ciudad">{{ item.origenCiudad }}</span>
                    <span class="res-summary__hora">{{ item.horaSalida }}</span>
                  </div>
                  <div class="res-summary__track">
                    <div class="res-summary__track-line"></div>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <div class="res-summary__track-line"></div>
                  </div>
                  <div class="res-summary__punto res-summary__punto--r">
                    <span class="res-summary__iata">{{ item.destinoCodigo }}</span>
                    <span class="res-summary__ciudad">{{ item.destinoCiudad }}</span>
                    <span class="res-summary__hora">{{ item.horaLlegada }}</span>
                  </div>
                </div>
                <div class="res-summary__details">
                  <div class="res-summary__detail-row">
                    <span>Clase</span><span>{{ item.clase }}</span>
                  </div>
                  <div class="res-summary__detail-row">
                    <span>Duración</span><span>{{ formatDuracion(item.duracionMinutos) }}</span>
                  </div>
                  <div class="res-summary__detail-row">
                    <span>Escalas</span><span>{{ item.escalas === 0 ? 'Directo' : item.escalas + ' escala(s)' }}</span>
                  </div>
                  <div class="res-summary__detail-row">
                    <span>Avión</span><span>{{ item.avionMarca }} {{ item.avionModelo }}</span>
                  </div>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Por persona</span>
                  <span class="res-summary__precio">${{ item.precio?.toFixed(2) }}</span>
                </div>
              </template>

              <!-- HOTEL -->
              <template v-else-if="tipoItem === 'hotel'">
                <div class="res-summary__tag res-summary__tag--hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  Hospedaje
                </div>
                <h3 class="res-summary__hotel-nombre">{{ item.nombreHotel }}</h3>
                <p class="res-summary__hotel-ubicacion">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  {{ item.ciudad }}, {{ item.pais }}
                </p>
                <div class="res-summary__details">
                  <div class="res-summary__detail-row">
                    <span>Habitación</span><span>{{ item.tipoHabitacion }}</span>
                  </div>
                  <div class="res-summary__detail-row">
                    <span>Cama</span><span>{{ item.tipoCama }}</span>
                  </div>
                  <div class="res-summary__detail-row">
                    <span>Capacidad</span><span>{{ item.capacidad }} persona(s)</span>
                  </div>
                  <div class="res-summary__detail-row" v-if="item.metrosCuadrados">
                    <span>Tamaño</span><span>{{ item.metrosCuadrados }} m²</span>
                  </div>
                </div>
                <div v-if="item.amenidades?.length" class="res-summary__amenidades">
                  <span v-for="a in item.amenidades.slice(0,4)" :key="a" class="res-summary__amenidad">{{ a }}</span>
                </div>
                <div class="res-summary__precio-wrap">
                  <span class="res-summary__precio-lbl">Por noche</span>
                  <span class="res-summary__precio">${{ item.precioPorNoche?.toFixed(2) }}</span>
                </div>
              </template>

              <!-- PAQUETE -->
              <template v-else-if="tipoItem === 'paquete'">
                <div class="res-summary__tag res-summary__tag--paquete">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                  Paquete Completo
                </div>
                <h3 class="res-summary__paquete-nombre">{{ item.nombre }}</h3>
                <div class="res-summary__paquete-seccion">
                  <span class="res-summary__paquete-lbl">Vuelo incluido</span>
                  <div class="res-summary__ruta res-summary__ruta--sm">
                    <span class="res-summary__iata res-summary__iata--sm">{{ item.vuelo?.origenCodigo }}</span>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <span class="res-summary__iata res-summary__iata--sm">{{ item.vuelo?.destinoCodigo }}</span>
                  </div>
                  <span class="res-summary__paquete-meta">{{ item.vuelo?.aerolinea }} · {{ item.vuelo?.numeroVuelo }}</span>
                </div>
                <div class="res-summary__paquete-divider"></div>
                <div class="res-summary__paquete-seccion">
                  <span class="res-summary__paquete-lbl">Hospedaje incluido</span>
                  <span class="res-summary__paquete-val">{{ item.hotel?.nombre }}</span>
                  <span class="res-summary__paquete-meta">{{ item.hotel?.ciudad }} · {{ item.noches }} noches · {{ item.hotel?.tipoHabitacion }}</span>
                </div>
                <div class="res-summary__precio-wrap">
                  <div>
                    <span class="res-summary__precio-lbl">Por persona</span>
                    <span class="res-summary__precio-individual"><s>Q{{ calcPrecioIndividual.toLocaleString() }}</s></span>
                  </div>
                  <span class="res-summary__precio">Q{{ item.precioEspecial?.toLocaleString() }}</span>
                </div>
              </template>

              <!-- Botón reservar -->
              <div v-if="item" class="res-summary__footer">
                <button class="res-btn res-btn--yellow res-btn--full" @click="handleReservar" type="button" :disabled="submitting">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="15" height="15"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/></svg>
                  {{ submitting ? 'Procesando...' : 'Confirmar Reserva' }}
                </button>
                <p class="res-summary__aviso">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  Los datos deben coincidir con el pasaporte
                </p>
              </div>

            </div>
          </aside>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/reserva.css'

const router   = useRouter()
const API      = 'http://localhost:7000'

// ── Item seleccionado ─────────────────────────────────────────────
const item     = ref(null)
const tipoItem = ref('') // 'vuelo' | 'hotel' | 'paquete'

onMounted(() => {
  const vuelo   = sessionStorage.getItem('vuelo_seleccionado')
  const hotel   = sessionStorage.getItem('hotel_seleccionado')
  const paquete = sessionStorage.getItem('paquete_seleccionado')
  if (vuelo)   { item.value = JSON.parse(vuelo);   tipoItem.value = 'vuelo'   }
  else if (hotel)   { item.value = JSON.parse(hotel);   tipoItem.value = 'hotel'   }
  else if (paquete) { item.value = JSON.parse(paquete); tipoItem.value = 'paquete' }
})

// ── Formulario ───────────────────────────────────────────────────
const form = ref({
  nombre: '', apellido: '', pasaporte: '',
  fechaNacimiento: '', pais: '', ciudad: '',
  telefono: '', email: '', nota: '',
})

const errors    = ref({})
const submitting = ref(false)

// ── Países / ciudades ─────────────────────────────────────────────
const todosLosPaises   = ref([])
const paisQuery        = ref('')
const paisesSugeridos  = ref([])
const paisSeleccionado = ref(null)
const ciudadQuery      = ref('')
const ciudadesSugeridas = ref([])

// ── Teléfono / dial codes ─────────────────────────────────────────
const dialCode    = ref('')
const phoneDigits = ref(8)
const dialCodesMap = ref({})

const knownDigits = {
  '+1':10,'+502':8,'+503':8,'+504':8,'+505':8,'+506':8,'+507':8,
  '+52':10,'+53':8,'+54':10,'+55':11,'+56':9,'+57':10,'+58':10,
  '+34':9,'+44':10,'+49':10,'+33':9,'+39':10,'+81':10,'+86':11,
  '+91':10,'+55':11,'+7':10,'+20':10,'+27':9,'+82':10,'+84':9,
}

onMounted(async () => {
  try {
    const res  = await fetch('https://countriesnow.space/api/v0.1/countries')
    const data = await res.json()
    todosLosPaises.value = data.data
  } catch { console.warn('No se cargaron países') }

  try {
    const res  = await fetch('https://restcountries.com/v3.1/all?fields=name,idd')
    const data = await res.json()
    data.forEach(p => {
      if (p.idd?.root) {
        const suffixes = p.idd.suffixes ?? ['']
        const code = suffixes.length === 1 ? p.idd.root + suffixes[0] : p.idd.root
        const digits = knownDigits[code] ?? 9
        dialCodesMap.value[p.name.common.toLowerCase()] = { code, digits }
      }
    })
  } catch { console.warn('No se cargaron dial codes') }
})

function onPaisInput() {
  const q = paisQuery.value.toLowerCase()
  paisesSugeridos.value = q.length < 2 ? [] :
    todosLosPaises.value.filter(p => p.country.toLowerCase().includes(q)).slice(0, 6)
  if (!paisSeleccionado.value) form.value.pais = ''
}

function seleccionarPais(pais) {
  paisSeleccionado.value   = pais
  paisQuery.value          = pais.country
  form.value.pais          = pais.country
  paisesSugeridos.value    = []
  ciudadQuery.value        = ''
  form.value.ciudad        = ''
  ciudadesSugeridas.value  = []
  const info = dialCodesMap.value[pais.country.toLowerCase()]
  dialCode.value    = info?.code ?? ''
  phoneDigits.value = info?.digits ?? 9
  form.value.telefono = ''
}

function validarPais() {
  if (paisQuery.value && !paisSeleccionado.value) {
    paisQuery.value = ''
    paisesSugeridos.value = []
  }
}

function onCiudadInput() {
  if (!paisSeleccionado.value) return
  const q = ciudadQuery.value.toLowerCase()
  ciudadesSugeridas.value = q.length < 2 ? [] :
    paisSeleccionado.value.cities.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function seleccionarCiudad(c) {
  ciudadQuery.value       = c
  form.value.ciudad       = c
  ciudadesSugeridas.value = []
}

function validarCiudad() {
  if (ciudadQuery.value && !form.value.ciudad) {
    ciudadQuery.value = ''
    ciudadesSugeridas.value = []
  }
}

// ── Teléfono ──────────────────────────────────────────────────────
const telefonoDigitos = computed(() =>
  form.value.telefono.replace(/\D/g, '').length
)

const phonePlaceholder = computed(() => {
  const n = phoneDigits.value
  const s = '5'.repeat(n)
  if (n <= 7)  return s.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  if (n === 8) return s.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  if (n === 9) return s.replace(/^(\d{3})(\d{3})(\d{3})/, '$1 $2 $3')
  return s.replace(/^(\d{3})(\d{3})(\d{4})/, '$1 $2 $3')
})

function onPhoneInput(e) {
  const raw   = e.target.value.replace(/\D/g, '')
  const capped = raw.slice(0, phoneDigits.value)
  const n = phoneDigits.value
  let formatted = capped
  if (n <= 7)       formatted = capped.replace(/^(\d{3})(\d{0,4})/, '$1 $2').trim()
  else if (n === 8) formatted = capped.replace(/^(\d{4})(\d{0,4})/, '$1 $2').trim()
  else if (n === 9) formatted = capped.replace(/^(\d{3})(\d{0,3})(\d{0,3})/, '$1 $2 $3').trim()
  else              formatted = capped.replace(/^(\d{3})(\d{0,3})(\d{0,4})/, '$1 $2 $3').trim()
  form.value.telefono = formatted
  errors.value.telefono = ''
}

// ── Precio individual paquete ─────────────────────────────────────
const calcPrecioIndividual = computed(() => {
  if (!item.value || tipoItem.value !== 'paquete') return 0
  return (item.value.vuelo?.precioBase || 0) +
         (item.value.hotel?.precioPorNoche || 0) * (item.value.noches || 1)
})

function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min/60)}h${min%60 > 0 ? ' ' + min%60 + 'm' : ''}`
}

// ── Validar y enviar ──────────────────────────────────────────────
async function handleReservar() {
  errors.value = {}
  const f = form.value

  if (!f.nombre)          errors.value.nombre = 'Campo requerido'
  if (!f.apellido)        errors.value.apellido = 'Campo requerido'
  if (!f.pasaporte)       errors.value.pasaporte = 'Campo requerido'
  if (!/^\d+$/.test(f.pasaporte)) errors.value.pasaporte = 'Solo números'
  if (!f.fechaNacimiento) errors.value.fechaNacimiento = 'Campo requerido'
  if (!f.pais)            errors.value.pais = 'Selecciona un país de la lista'
  if (!f.ciudad)          errors.value.ciudad = 'Selecciona una ciudad de la lista'
  if (!f.email)           errors.value.email = 'Campo requerido'
  if (dialCode.value && telefonoDigitos.value !== phoneDigits.value)
    errors.value.telefono = `Se requieren ${phoneDigits.value} dígitos`

  if (Object.keys(errors.value).length) return

  submitting.value = true
  try {
    const telefonoCompleto = dialCode.value
      ? `${dialCode.value} ${f.telefono.replace(/\s/g,'')}`
      : f.telefono

    const payload = {
      tipoItem: tipoItem.value,
      itemId:   item.value.id || item.value.habitacionId,
      pasajero: { ...f, telefono: telefonoCompleto },
    }

    // Guardar en sessionStorage para checkout
    sessionStorage.setItem('reserva_datos', JSON.stringify(payload))
    router.push('/checkout')

  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>