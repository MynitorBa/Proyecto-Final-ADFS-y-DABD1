<template>
  <div class="page">
    <Encabezado />
    <div class="conf-page">
      <div class="conf-container">

        <!-- ═══ STEPS ═══ -->
        <div class="conf-steps-bar">
          <div class="conf-step conf-step--done">
            <div class="conf-step__num">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="conf-step__lbl">Datos</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--done">
            <div class="conf-step__num">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="conf-step__lbl">Pago</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--active">
            <div class="conf-step__num">3</div>
            <span class="conf-step__lbl">Confirmación</span>
          </div>
        </div>

        <!-- ═══ HERO SUCCESS ═══ -->
        <div class="conf-hero">
          <div class="conf-hero__check">
            <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" width="32" height="32"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <h1 class="conf-hero__title">¡Reserva confirmada!</h1>
          <p class="conf-hero__sub">Tu compra fue realizada correctamente. Te enviamos los detalles a tu correo.</p>
          <div class="conf-hero__no">
            <span class="conf-hero__no-lbl">No. de reserva</span>
            <span class="conf-hero__no-val">{{ noReservacion || 'MV-2026-00421' }}</span>
          </div>
        </div>

        <!-- ═══ DATOS DE COMPRA ═══ -->
        <div class="conf-grid">

          <div class="conf-main">

            <!-- Tarjeta de compra -->
            <div class="conf-card">
              <div class="conf-card__head">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Detalles de tu reserva
              </div>
              <div class="conf-card__body">

                <!-- VUELO -->
                <template v-if="tipoItem === 'vuelo' && itemData">
                  <div class="conf-ruta-wrap">
                    <div class="conf-ruta">
                      <div class="conf-ruta__punto">
                        <span class="conf-ruta__iata">{{ itemData.origenCodigo }}</span>
                        <span class="conf-ruta__ciudad">{{ itemData.origenCiudad }}</span>
                        <span class="conf-ruta__hora">{{ itemData.horaSalida }}</span>
                      </div>
                      <div class="conf-ruta__track">
                        <div class="conf-ruta__line"></div>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <div class="conf-ruta__line"></div>
                      </div>
                      <div class="conf-ruta__punto conf-ruta__punto--r">
                        <span class="conf-ruta__iata">{{ itemData.destinoCodigo }}</span>
                        <span class="conf-ruta__ciudad">{{ itemData.destinoCiudad }}</span>
                        <span class="conf-ruta__hora">{{ itemData.horaLlegada }}</span>
                      </div>
                    </div>
                    <div class="conf-detalles-row">
                      <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.aerolinea }}</strong></div>
                      <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.numeroVuelo }}</strong></div>
                      <div class="conf-detalle"><span>Clase</span><strong>{{ itemData.clase }}</strong></div>
                      <div class="conf-detalle"><span>Avión</span><strong>{{ itemData.avionMarca }} {{ itemData.avionModelo }}</strong></div>
                    </div>
                  </div>
                </template>

                <!-- HOTEL -->
                <template v-else-if="tipoItem === 'hotel' && itemData">
                  <div class="conf-hotel-wrap">
                    <div class="conf-hotel__top">
                      <h3 class="conf-hotel__nombre">{{ itemData.nombreHotel }}</h3>
                      <div class="conf-hotel__estrellas">
                        <svg v-for="i in (itemData.estrellas||4)" :key="i" viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
                      </div>
                    </div>
                    <p class="conf-hotel__ubicacion">{{ itemData.ciudad }}, {{ itemData.pais }}</p>
                    <div class="conf-detalles-row">
                      <div class="conf-detalle"><span>Habitación</span><strong>{{ itemData.tipoHabitacion }}</strong></div>
                      <div class="conf-detalle"><span>Cama</span><strong>{{ itemData.tipoCama }}</strong></div>
                      <div class="conf-detalle"><span>Capacidad</span><strong>{{ itemData.capacidad }} persona(s)</strong></div>
                      <div class="conf-detalle" v-if="itemData.metrosCuadrados"><span>Tamaño</span><strong>{{ itemData.metrosCuadrados }} m²</strong></div>
                    </div>
                  </div>
                </template>

                <!-- PAQUETE -->
                <template v-else-if="tipoItem === 'paquete' && itemData">
                  <div class="conf-paquete-wrap">
                    <h3 class="conf-paquete__nombre">{{ itemData.nombre }}</h3>
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">Vuelo incluido</div>
                      <div class="conf-ruta conf-ruta--sm">
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.origenCodigo }}</span>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.destinoCodigo }}</span>
                      </div>
                    </div>
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">Hospedaje incluido</div>
                      <p class="conf-paquete__val">{{ itemData.hotel?.nombre }} · {{ itemData.noches }} noches</p>
                    </div>
                  </div>
                </template>

                <!-- Fallback demo -->
                <template v-else>
                  <div class="conf-ruta-wrap">
                    <div class="conf-ruta">
                      <div class="conf-ruta__punto">
                        <span class="conf-ruta__iata">GUA</span>
                        <span class="conf-ruta__ciudad">Guatemala City</span>
                        <span class="conf-ruta__hora">06:30</span>
                      </div>
                      <div class="conf-ruta__track">
                        <div class="conf-ruta__line"></div>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <div class="conf-ruta__line"></div>
                      </div>
                      <div class="conf-ruta__punto conf-ruta__punto--r">
                        <span class="conf-ruta__iata">PTY</span>
                        <span class="conf-ruta__ciudad">Ciudad de Panamá</span>
                        <span class="conf-ruta__hora">08:15</span>
                      </div>
                    </div>
                  </div>
                </template>

              </div>
            </div>

            <!-- Acciones -->
            <div class="conf-actions">
              <button class="conf-btn conf-btn--pdf" @click="descargarPDF" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
                Descargar PDF
              </button>
              <button class="conf-btn conf-btn--email" @click="enviarCorreo" :disabled="enviandoEmail" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                {{ enviandoEmail ? 'Enviando...' : 'Enviar copia al correo' }}
              </button>
            </div>

            <p v-if="emailOk" class="conf-toast conf-toast--ok">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><polyline points="20 6 9 17 4 12"/></svg>
              Copia enviada a {{ pasajeroEmail }}
            </p>

            <!-- ═══ RECOMENDACIONES ═══ -->
            <div class="conf-recomendaciones">
              <h2 class="conf-recom__title">Destinos que te podrían interesar</h2>
              <p class="conf-recom__sub">Vuelos, hoteles y paquetes combinados desde Guatemala</p>

              <div class="conf-recom-grid">

                <div class="conf-recom-card" v-for="r in recomendaciones" :key="r.id">
                  <div class="conf-recom-card__img" :style="{ background: r.gradient }">
                    <div class="conf-recom-card__overlay"></div>
                    <div class="conf-recom-card__tipo">
                      <svg v-if="r.tipo==='vuelo'" viewBox="0 0 24 24" fill="currentColor" width="11" height="11"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <svg v-else-if="r.tipo==='hotel'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      <svg v-else viewBox="0 0 24 24" fill="currentColor" width="11" height="11"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                      {{ r.tipo === 'vuelo' ? 'Vuelo' : r.tipo === 'hotel' ? 'Hotel' : 'Paquete' }}
                    </div>
                    <div class="conf-recom-card__ruta">
                      <span>{{ r.origen }}</span>
                      <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <span>{{ r.destino }}</span>
                    </div>
                  </div>
                  <div class="conf-recom-card__body">
                    <p class="conf-recom-card__nombre">{{ r.nombre }}</p>
                    <p class="conf-recom-card__desc">{{ r.desc }}</p>
                    <div class="conf-recom-card__footer">
                      <span class="conf-recom-card__precio">{{ r.precio }}</span>
                      <button class="conf-recom-card__btn" @click="$router.push(r.ruta)" type="button">Ver más</button>
                    </div>
                  </div>
                </div>

              </div>
            </div>

          </div>

          <!-- ═══ SIDEBAR pasajero + total ═══ -->
          <aside class="conf-sidebar">
            <div class="conf-resumen">
              <div class="conf-resumen__head">Tu reserva</div>

              <div class="conf-resumen__check-row">
                <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5" width="18" height="18"><polyline points="20 6 9 17 4 12"/></svg>
                <span>Pago procesado exitosamente</span>
              </div>

              <div class="conf-resumen__body">
                <div class="conf-resumen__row"><span>No. reserva</span><strong>{{ noReservacion || 'MV-2026-00421' }}</strong></div>
                <div class="conf-resumen__row" v-if="pasajeroNombre"><span>Pasajero</span><strong>{{ pasajeroNombre }}</strong></div>
                <div class="conf-resumen__row" v-if="pasajeroEmail"><span>Correo</span><strong class="conf-resumen__email">{{ pasajeroEmail }}</strong></div>
                <div class="conf-resumen__row"><span>Fecha</span><strong>{{ fechaHoy }}</strong></div>
              </div>

              <div class="conf-resumen__total">
                <span>Total pagado</span>
                <strong>{{ totalPagado }}</strong>
              </div>
            </div>

            <!-- Navegación -->
            <div class="conf-nav-btns">
              <button class="conf-nav-btn conf-nav-btn--primary" @click="$router.push('/principal')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                Ir al inicio
              </button>
              <button class="conf-nav-btn conf-nav-btn--secondary" @click="$router.push('/mis-reservaciones')" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Mis Reservaciones
              </button>
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
import { useRoute, useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/confirmacion.css'

const route  = useRoute()
const router = useRouter()
const API    = 'http://localhost:7000'

const noReservacion  = ref('')
const enviandoEmail  = ref(false)
const emailOk        = ref(false)
const tipoItem       = ref('')
const itemData       = ref(null)
const pasajeroNombre = ref('')
const pasajeroEmail  = ref('')
const totalPagado    = ref('--')

const fechaHoy = computed(() => {
  const d = new Date()
  return d.toLocaleDateString('es-GT', { day:'2-digit', month:'long', year:'numeric' })
})

onMounted(() => {
  noReservacion.value = route.query.noReservacion || ''

  const rawDatos = sessionStorage.getItem('reserva_datos')
  if (rawDatos) {
    const datos = JSON.parse(rawDatos)
    tipoItem.value = datos.tipoItem || ''
    pasajeroNombre.value = datos.pasajero ? `${datos.pasajero.nombre} ${datos.pasajero.apellido}` : ''
    pasajeroEmail.value  = datos.pasajero?.email || ''
  }

  const tipo = tipoItem.value
  if (tipo) {
    const rawItem = sessionStorage.getItem(`${tipo}_seleccionado`)
    if (rawItem) {
      itemData.value = JSON.parse(rawItem)
      if (tipo === 'vuelo')   totalPagado.value = `$${itemData.value.precio?.toFixed(2)}`
      if (tipo === 'hotel')   totalPagado.value = `$${itemData.value.precioPorNoche?.toFixed(2)}`
      if (tipo === 'paquete') totalPagado.value = `Q${itemData.value.precioEspecial?.toLocaleString()}`
    }
  }
})

async function descargarPDF() {
  try {
    const r = await fetch(`${API}/api/reservaciones/${noReservacion.value || 'MV-2026-00421'}/pdf`, {
      credentials: 'include'
    })
    if (!r.ok) throw new Error()
    const blob = await r.blob()
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href = url; a.download = `reserva-${noReservacion.value || 'MV-2026-00421'}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('No se pudo generar el PDF. Intenta más tarde.')
  }
}

async function enviarCorreo() {
  enviandoEmail.value = true
  try {
    await fetch(`${API}/api/reservaciones/${noReservacion.value}/email`, {
      method: 'POST', credentials: 'include'
    })
    emailOk.value = true
    setTimeout(() => emailOk.value = false, 4000)
  } catch {
    alert('No se pudo enviar el correo. Intenta más tarde.')
  } finally {
    enviandoEmail.value = false
  }
}

const recomendaciones = [
  {
    id: 1,
    tipo: 'vuelo',
    origen: 'GUA', destino: 'MIA',
    nombre: 'Miami, Florida',
    desc: 'Avianca · Vuelo directo · Clase económica',
    precio: 'Desde $320',
    gradient: 'linear-gradient(135deg, #1a3a4a 0%, #2d6a7a 50%, #1a3a4a 100%)',
    ruta: '/resultados-vuelos?origen=GUA&destino=MIA'
  },
  {
    id: 2,
    tipo: 'paquete',
    origen: 'GUA', destino: 'CUN',
    nombre: 'Cancún 7 noches',
    desc: 'Vuelo + Hotel incluido · Todo en uno',
    precio: 'Desde Q4,850',
    gradient: 'linear-gradient(135deg, #0d3d2e 0%, #1a6644 50%, #0d3d2e 100%)',
    ruta: '/resultados-paquetes?origen=GUA&destino=CUN'
  },
  {
    id: 3,
    tipo: 'hotel',
    origen: 'ANT', destino: 'ANT',
    nombre: 'Casa Santo Domingo',
    desc: 'Antigua Guatemala · 5 estrellas · Suite Deluxe',
    precio: 'Desde $185/noche',
    gradient: 'linear-gradient(135deg, #3d1a0d 0%, #7a3a1a 50%, #3d1a0d 100%)',
    ruta: '/resultados-hoteles?pais=Guatemala&ciudad=Antigua'
  },
  {
    id: 4,
    tipo: 'paquete',
    origen: 'GUA', destino: 'MEX',
    nombre: 'Ciudad de México 4 noches',
    desc: 'Copa Airlines + Hotel · Vuelo + Hospedaje',
    precio: 'Desde Q2,600',
    gradient: 'linear-gradient(135deg, #1a1a3d 0%, #3a3a7a 50%, #1a1a3d 100%)',
    ruta: '/resultados-paquetes?origen=GUA&destino=MEX'
  }
]
</script>