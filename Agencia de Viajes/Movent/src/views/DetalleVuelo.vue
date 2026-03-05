<template>
  <div class="page">
    <Encabezado />

    <div class="dv-page">
      <div class="dv-container">

        <div v-if="loading" class="dv-empty">
          <div class="dv-spinner"></div>
          <p>Cargando detalle del vuelo...</p>
        </div>

        <div v-else-if="error" class="dv-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <p>{{ error }}</p>
          <button class="dv-btn dv-btn--ghost" @click="$router.back()" type="button">Volver</button>
        </div>

        <template v-else-if="vuelo">

          <div class="dv-breadcrumb">
            <button class="dv-breadcrumb__btn" @click="$router.back()" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
              Volver a resultados
            </button>
            <span class="dv-breadcrumb__sep">/</span>
            <span class="dv-breadcrumb__actual">{{ vuelo.origenCodigo }} → {{ vuelo.destinoCodigo }}</span>
          </div>

          <div class="dv-grid">

            <!-- IZQUIERDA -->
            <div class="dv-left">

              <!-- Hero -->
              <div class="dv-hero">
                <div class="dv-hero__head">
                  <div class="dv-hero__aerolinea">
                    <div class="dv-hero__logo">
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    </div>
                    <div>
                      <p class="dv-hero__aerolinea-nombre">{{ vuelo.aerolinea }}</p>
                      <p class="dv-hero__vuelo-num">Vuelo {{ vuelo.numeroVuelo }}</p>
                    </div>
                  </div>
                  <div class="dv-hero__tags">
                    <span v-if="vuelo.escalas === 0" class="dv-tag dv-tag--directo">Directo</span>
                    <span v-else class="dv-tag dv-tag--escala">{{ vuelo.escalas }} escala{{ vuelo.escalas!==1?'s':'' }}</span>
                  </div>
                </div>

                <div class="dv-hero__ruta">
                  <div class="dv-hero__punto">
                    <span class="dv-hero__iata">{{ vuelo.origenCodigo }}</span>
                    <span class="dv-hero__ciudad">{{ vuelo.origenCiudad }}</span>
                    <span class="dv-hero__pais">{{ vuelo.origenPais }}</span>
                    <span class="dv-hero__hora">{{ vuelo.horaSalida }}</span>
                    <span class="dv-hero__fecha">{{ formatFecha(vuelo.fechaVuelo) }}</span>
                  </div>

                  <div class="dv-hero__medio">
                    <span class="dv-hero__dur">{{ formatDuracion(vuelo.duracionMinutos) }}</span>
                    <div class="dv-hero__track">
                      <div class="dv-hero__track-dot"></div>
                      <div class="dv-hero__track-line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="28" height="28" class="dv-hero__track-avion"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="dv-hero__track-line"></div>
                      <div class="dv-hero__track-dot"></div>
                    </div>
                    <div v-if="vuelo.paradas?.length" class="dv-hero__paradas">
                      <div v-for="p in vuelo.paradas" :key="p.codigo" class="dv-hero__parada">
                        <div class="dv-hero__parada-dot"></div>
                        <div class="dv-hero__parada-info">
                          <span class="dv-hero__parada-iata">{{ p.codigo }}</span>
                          <span class="dv-hero__parada-ciudad">{{ p.ciudad }}</span>
                          <span class="dv-hero__parada-espera">Espera: {{ p.espera }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="dv-hero__punto dv-hero__punto--r">
                    <span class="dv-hero__iata">{{ vuelo.destinoCodigo }}</span>
                    <span class="dv-hero__ciudad">{{ vuelo.destinoCiudad }}</span>
                    <span class="dv-hero__pais">{{ vuelo.destinoPais }}</span>
                    <span class="dv-hero__hora">{{ vuelo.horaLlegada }}</span>
                    <span class="dv-hero__fecha">{{ formatFechaLlegada() }}</span>
                  </div>
                </div>

                <div class="dv-hero__avion-info">
                  <div class="dv-hero__avion-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="14" height="14"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                    <span>{{ vuelo.avionMarca }} {{ vuelo.avionModelo }}</span>
                  </div>
                  <div class="dv-hero__avion-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="14" height="14"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                    <span>{{ vuelo.capacidadTotal }} asientos en total</span>
                  </div>
                  <div class="dv-hero__avion-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    <span>Duración: {{ formatDuracion(vuelo.duracionMinutos) }}</span>
                  </div>
                </div>
              </div>

              <!-- Selector de clase -->
              <div class="dv-section">
                <h3 class="dv-section__title">Selecciona tu clase</h3>
                <div class="dv-clases">
                  <div v-for="clase in vuelo.clases" :key="clase.nombre"
                    class="dv-clase-card"
                    :class="{ 'dv-clase-card--active': claseSeleccionada === clase.nombre }"
                    @click="seleccionarClase(clase)">
                    <div class="dv-clase-card__head">
                      <div>
                        <h4 class="dv-clase-card__nombre">{{ clase.nombre }}</h4>
                        <p class="dv-clase-card__asientos">{{ clase.asientosDisponibles }} asientos disponibles</p>
                      </div>
                      <div class="dv-clase-card__precio-wrap">
                        <span class="dv-clase-card__precio-lbl">por persona</span>
                        <span class="dv-clase-card__precio">${{ clase.precio?.toFixed(2) }}</span>
                      </div>
                    </div>
                    <div class="dv-clase-card__beneficios">
                      <div v-for="b in clase.beneficios" :key="b" class="dv-clase-card__beneficio">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
                        <span>{{ b }}</span>
                      </div>
                    </div>
                    <div class="dv-clase-card__equipaje">
                      <div v-if="clase.equipajeMano" class="dv-clase-card__eq-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                        Equipaje de mano: {{ clase.equipajeMano }}
                      </div>
                      <div v-if="clase.equipajeBodega" class="dv-clase-card__eq-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
                        Bodega: {{ clase.equipajeBodega }}
                      </div>
                    </div>
                    <div v-if="claseSeleccionada === clase.nombre" class="dv-clase-card__check">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
                      Seleccionada
                    </div>
                  </div>
                </div>
              </div>

              <!-- Pasajeros -->
              <div class="dv-section" v-if="claseSeleccionada">
                <h3 class="dv-section__title">
                  Pasajeros
                  <span class="dv-section__sub">{{ pasajeros.length }} de {{ busqueda.pasajeros }}</span>
                </h3>
                <div v-for="(p, idx) in pasajeros" :key="idx" class="dv-pasajero">
                  <div class="dv-pasajero__head">
                    <span class="dv-pasajero__num">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      Pasajero {{ idx + 1 }}
                    </span>
                    <button v-if="idx > 0" class="dv-pasajero__remove" @click="quitarPasajero(idx)" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    </button>
                  </div>
                  <div class="dv-pasajero__grid">
                    <div class="dv-field">
                      <label class="dv-field__label">Nombre *</label>
                      <input class="dv-field__input" v-model="p.nombre" placeholder="Nombre" type="text" />
                    </div>
                    <div class="dv-field">
                      <label class="dv-field__label">Apellido *</label>
                      <input class="dv-field__input" v-model="p.apellido" placeholder="Apellido" type="text" />
                    </div>
                    <div class="dv-field">
                      <label class="dv-field__label">Pasaporte *</label>
                      <input class="dv-field__input" v-model="p.pasaporte" placeholder="No. pasaporte" type="text" />
                    </div>
                    <div class="dv-field">
                      <label class="dv-field__label">Nacionalidad *</label>
                      <input class="dv-field__input" v-model="p.nacionalidad" placeholder="País" type="text" />
                    </div>
                    <div class="dv-field">
                      <label class="dv-field__label">Fecha de nacimiento *</label>
                      <input class="dv-field__input" v-model="p.fechaNacimiento" type="date" />
                    </div>
                    <div class="dv-field">
                      <label class="dv-field__label">Asiento preferido</label>
                      <select class="dv-field__input" v-model="p.preferenciaAsiento">
                        <option value="">Sin preferencia</option>
                        <option value="ventana">Ventana</option>
                        <option value="pasillo">Pasillo</option>
                        <option value="centro">Centro</option>
                      </select>
                    </div>
                  </div>
                </div>
                <button v-if="pasajeros.length < busqueda.pasajeros"
                  class="dv-btn dv-btn--ghost dv-add-pasajero"
                  @click="agregarPasajero" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                  Agregar pasajero
                </button>
              </div>

            </div>

            <!-- DERECHA: RESUMEN -->
            <div class="dv-right">
              <div class="dv-resumen">
                <h3 class="dv-resumen__title">Resumen</h3>
                <div class="dv-resumen__ruta">
                  <span class="dv-resumen__iata">{{ vuelo.origenCodigo }}</span>
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span class="dv-resumen__iata">{{ vuelo.destinoCodigo }}</span>
                </div>
                <div class="dv-resumen__rows">
                  <div class="dv-resumen__row"><span>Vuelo</span><span>{{ vuelo.numeroVuelo }}</span></div>
                  <div class="dv-resumen__row"><span>Fecha</span><span>{{ formatFecha(vuelo.fechaVuelo) }}</span></div>
                  <div class="dv-resumen__row"><span>Clase</span><span>{{ claseSeleccionada || '—' }}</span></div>
                  <div class="dv-resumen__row"><span>Pasajeros</span><span>{{ pasajeros.length }}</span></div>
                  <div v-if="precioClase" class="dv-resumen__row"><span>Precio c/u</span><span>${{ precioClase?.toFixed(2) }}</span></div>
                </div>
                <div class="dv-resumen__total">
                  <span>Total</span>
                  <strong>${{ totalVuelo.toFixed(2) }}</strong>
                </div>
                <p v-if="formError" class="dv-form-error">{{ formError }}</p>
                <button class="dv-btn dv-btn--yellow dv-resumen__cta"
                  @click="continuar" :disabled="!claseSeleccionada" type="button">
                  Continuar al checkout
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                </button>
                <p class="dv-resumen__nota">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  Precios incluyen impuestos y tasas
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
import '../styles/detallevuelo.css'

const router = useRouter()
const route  = useRoute()
const API    = 'http://localhost:7000'

const vuelo             = ref(null)
const loading           = ref(true)
const error             = ref('')
const claseSeleccionada = ref('')
const precioClase       = ref(0)
const formError         = ref('')

const busqueda = ref({ pasajeros: Number(route.query.pasajeros) || 1 })

const pasajeroVacio = () => ({ nombre:'', apellido:'', pasaporte:'', nacionalidad:'', fechaNacimiento:'', preferenciaAsiento:'' })
const pasajeros = ref([pasajeroVacio()])

const totalVuelo = computed(() => (precioClase.value || 0) * pasajeros.value.length)

onMounted(() => cargarVuelo())

async function cargarVuelo() {
  loading.value = true; error.value = ''
  try {
    const r = await fetch(`${API}/api/vuelos/${route.params.id}`, { credentials: 'include' })
    if (r.ok) {
      vuelo.value = await r.json()
      if (route.query.clase && vuelo.value.clases) {
        const c = vuelo.value.clases.find(c => c.nombre === route.query.clase)
        if (c) seleccionarClase(c)
      }
      const n = busqueda.value.pasajeros
      pasajeros.value = Array.from({ length: n }, () => pasajeroVacio())
    } else error.value = 'No se encontró el vuelo.'
  } catch { error.value = 'Error de conexión.' }
  finally { loading.value = false }
}

function seleccionarClase(clase) {
  claseSeleccionada.value = clase.nombre
  precioClase.value = clase.precio
}

function agregarPasajero() {
  if (pasajeros.value.length < busqueda.value.pasajeros) pasajeros.value.push(pasajeroVacio())
}

function quitarPasajero(idx) { pasajeros.value.splice(idx, 1) }

function validar() {
  if (!claseSeleccionada.value) { formError.value = 'Selecciona una clase.'; return false }
  for (let i = 0; i < pasajeros.value.length; i++) {
    const p = pasajeros.value[i]
    if (!p.nombre.trim() || !p.apellido.trim() || !p.pasaporte.trim() || !p.nacionalidad.trim() || !p.fechaNacimiento) {
      formError.value = `Completa todos los campos del pasajero ${i+1}.`; return false
    }
  }
  formError.value = ''; return true
}

function continuar() {
  if (!validar()) return
  sessionStorage.setItem('checkout_vuelo', JSON.stringify({
    vueloId: route.params.id, vuelo: vuelo.value,
    clase: claseSeleccionada.value, precio: precioClase.value,
    pasajeros: pasajeros.value, total: totalVuelo.value,
  }))
  router.push({ path: '/checkout', query: { tipo: 'vuelo' } })
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}

function formatFechaLlegada() {
  if (!vuelo.value?.fechaVuelo || !vuelo.value?.duracionMinutos) return formatFecha(vuelo.value?.fechaVuelo)
  const d = new Date(vuelo.value.fechaVuelo)
  const [h, m] = (vuelo.value.horaSalida || '00:00').split(':').map(Number)
  d.setHours(h, m); d.setMinutes(d.getMinutes() + vuelo.value.duracionMinutos)
  return formatFecha(d.toISOString())
}

function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min/60)}h${min%60>0?' '+min%60+'m':''}`
}
</script>