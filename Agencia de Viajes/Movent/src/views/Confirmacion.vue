<template>
  <div class="page">
    <Encabezado />
    <div class="conf-page">
      <div class="conf-container">

        <!-- ═══ STEPS ═══ -->
        <div class="conf-steps-bar">
          <div class="conf-step conf-step--done">
            <div class="conf-step__num"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg></div>
            <span class="conf-step__lbl">Datos</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--done">
            <div class="conf-step__num"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg></div>
            <span class="conf-step__lbl">Pago</span>
          </div>
          <div class="conf-step__connector conf-step__connector--done"></div>
          <div class="conf-step conf-step--active">
            <div class="conf-step__num">3</div>
            <span class="conf-step__lbl">Confirmación</span>
          </div>
        </div>

        <!-- ═══ HERO ═══ -->
        <div class="conf-hero">
          <div class="conf-hero__check">
            <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" width="32" height="32"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <h1 class="conf-hero__title">¡Reserva confirmada!</h1>
          <p class="conf-hero__sub">Tu compra fue procesada exitosamente. Puedes descargar tu comprobante a continuación.</p>
          <div class="conf-hero__no">
            <span class="conf-hero__no-lbl">No. de reserva</span>
            <span class="conf-hero__no-val">{{ noReservacion || '—' }}</span>
          </div>
        </div>

        <!-- ═══ CONTENIDO ═══ -->
        <div class="conf-grid">

          <div class="conf-main">

            <!-- ── TARJETA DE DETALLES ── -->
            <div class="conf-card">
              <div class="conf-card__head">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Detalles de tu reserva
              </div>
              <div class="conf-card__body">

                <!-- Sin datos -->
                <template v-if="!itemData && !tipoItem">
                  <div class="conf-card__empty">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.5" width="40" height="40"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
                    <div>
                      <p class="conf-card__empty-title">Reserva {{ noReservacion }} confirmada</p>
                      <p class="conf-card__empty-sub">Revisa los detalles completos en <strong>Mis Reservaciones</strong> o descarga el comprobante PDF.</p>
                    </div>
                  </div>
                </template>

                <!-- ══ VUELO SOLO IDA ══ -->
                <template v-else-if="tipoItem === 'vuelo' && itemData?.tipoVuelo === 'ida'">
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
                      <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.clase }}</strong></div>
                      <div class="conf-detalle"><span>Escalas</span><strong>{{ itemData.escalas === 0 ? 'Directo' : itemData.escalas + ' escala(s)' }}</strong></div>
                    </div>
                  </div>
                  <div v-if="boletos.length" class="conf-boletos">
                    <div class="conf-boletos__titulo">Boletos</div>
                    <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                      <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                      <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                    </div>
                  </div>
                </template>

                <!-- ══ VUELO IDA Y VUELTA ══ -->
                <template v-else-if="tipoItem === 'vuelo' && itemData?.tipoVuelo === 'idaVuelta'">
                  <div class="conf-ruta-wrap">
                    <div class="conf-tramo">
                      <div class="conf-tramo__badge">Ida</div>
                      <div class="conf-ruta">
                        <div class="conf-ruta__punto">
                          <span class="conf-ruta__iata">{{ itemData.ida?.origenCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.ida?.origenCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.ida?.horaSalida }}</span>
                        </div>
                        <div class="conf-ruta__track">
                          <div class="conf-ruta__line"></div>
                          <svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                          <div class="conf-ruta__line"></div>
                        </div>
                        <div class="conf-ruta__punto conf-ruta__punto--r">
                          <span class="conf-ruta__iata">{{ itemData.ida?.destinoCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.ida?.destinoCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.ida?.horaLlegada }}</span>
                        </div>
                      </div>
                      <div class="conf-detalles-row conf-detalles-row--sm">
                        <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.ida?.aerolinea }}</strong></div>
                        <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.ida?.numeroVuelo }}</strong></div>
                        <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.ida?.clase }}</strong></div>
                      </div>
                    </div>
                    <div class="conf-tramo conf-tramo--regreso">
                      <div class="conf-tramo__badge conf-tramo__badge--reg">Regreso</div>
                      <div class="conf-ruta">
                        <div class="conf-ruta__punto">
                          <span class="conf-ruta__iata">{{ itemData.regreso?.origenCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.regreso?.origenCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.regreso?.horaSalida }}</span>
                        </div>
                        <div class="conf-ruta__track">
                          <div class="conf-ruta__line"></div>
                          <svg viewBox="0 0 24 24" fill="#9a9089" width="18" height="18" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                          <div class="conf-ruta__line"></div>
                        </div>
                        <div class="conf-ruta__punto conf-ruta__punto--r">
                          <span class="conf-ruta__iata">{{ itemData.regreso?.destinoCodigo }}</span>
                          <span class="conf-ruta__ciudad">{{ itemData.regreso?.destinoCiudad }}</span>
                          <span class="conf-ruta__hora">{{ itemData.regreso?.horaLlegada }}</span>
                        </div>
                      </div>
                      <div class="conf-detalles-row conf-detalles-row--sm">
                        <div class="conf-detalle"><span>Aerolínea</span><strong>{{ itemData.regreso?.aerolinea }}</strong></div>
                        <div class="conf-detalle"><span>Vuelo</span><strong>{{ itemData.regreso?.numeroVuelo }}</strong></div>
                        <div class="conf-detalle"><span>Clase</span><strong style="text-transform:capitalize">{{ itemData.regreso?.clase }}</strong></div>
                      </div>
                    </div>
                  </div>
                  <div v-if="boletos.length" class="conf-boletos">
                    <div class="conf-boletos__titulo">Boletos</div>
                    <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                      <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                      <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}<template v-if="b.numeroVuelo"> · Vuelo {{ b.numeroVuelo }}</template></span>
                    </div>
                  </div>
                </template>

                <!-- ══ HOTEL ══ -->
                <template v-else-if="tipoItem === 'hotel' && itemData">
                  <div class="conf-hotel-wrap">
                    <div class="conf-hotel__top">
                      <h3 class="conf-hotel__nombre">{{ itemData.nombreHotel }}</h3>
                    </div>
                    <p class="conf-hotel__ubicacion">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="12" height="12"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                      {{ itemData.hotelCiudad || itemData.busqueda?.ciudad }}, {{ itemData.hotelPais || itemData.busqueda?.pais }}
                    </p>
                    <div class="conf-detalles-row">
                      <div class="conf-detalle"><span>Habitación</span><strong>{{ itemData.tipoHabitacion || itemData.tipo }}</strong></div>
                      <div v-if="itemData.tipoCama" class="conf-detalle"><span>Cama</span><strong>{{ itemData.tipoCama }}</strong></div>
                      <div class="conf-detalle"><span>Check-in</span><strong>{{ itemData.busqueda?.checkIn }}</strong></div>
                      <div class="conf-detalle"><span>Check-out</span><strong>{{ itemData.busqueda?.checkOut }}</strong></div>
                      <div class="conf-detalle"><span>Noches</span><strong>{{ itemData.noches }}</strong></div>
                      <div class="conf-detalle"><span>Personas</span><strong>{{ itemData.busqueda?.cantidadPersonas }}</strong></div>
                    </div>
                  </div>
                </template>

                <!-- ══ PAQUETE ══ -->
                <template v-else-if="tipoItem === 'paquete' && itemData">
                  <div class="conf-paquete-wrap">
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        Vuelo incluido
                      </div>
                      <div class="conf-ruta conf-ruta--sm">
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.origenCodigo }}</span>
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <span class="conf-ruta__iata conf-ruta__iata--sm">{{ itemData.vuelo?.destinoCodigo }}</span>
                      </div>
                      <p class="conf-paquete__val">{{ itemData.vuelo?.aerolinea }} · Vuelo {{ itemData.vuelo?.numeroVuelo }} · <span style="text-transform:capitalize">{{ itemData.vuelo?.clase }}</span></p>
                    </div>
                    <div class="conf-paquete__seccion">
                      <div class="conf-paquete__lbl">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="12" height="12"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                        Hotel incluido
                      </div>
                      <p class="conf-paquete__val conf-paquete__val--nombre">{{ itemData.hotel?.nombreHotel }}</p>
                      <p class="conf-paquete__val">{{ itemData.hotel?.ciudad }} · {{ itemData.noches }} noches · {{ itemData.hotel?.tipoHabitacion }}</p>
                    </div>
                    <div v-if="boletos.length" class="conf-boletos">
                      <div class="conf-boletos__titulo">Boletos</div>
                      <div v-for="b in boletos" :key="b.boletoId" class="conf-boleto">
                        <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                        <span class="conf-boleto__no">{{ b.noBoleto }}</span>
                        <span class="conf-boleto__asiento">Asiento {{ b.noAsiento }} · {{ b.clase }}</span>
                      </div>
                    </div>
                  </div>
                </template>

              </div>
            </div>

            <!-- ── Acciones ── -->
            <div class="conf-actions">
              <button class="conf-btn conf-btn--pdf" @click="descargarPDF" :disabled="descargando" type="button">
                <span v-if="descargando" class="conf-btn__spin"></span>
                <template v-else>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>
                  Descargar comprobante PDF
                </template>
              </button>
            </div>

            <p v-if="pdfError" class="conf-toast conf-toast--err">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ pdfError }}
            </p>

          </div>

          <!-- ═══ SIDEBAR ═══ -->
          <aside class="conf-sidebar">
            <div class="conf-resumen">
              <div class="conf-resumen__head">
                <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="15" height="15"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/></svg>
                Tu reserva
              </div>
              <div class="conf-resumen__check-row">
                <div class="conf-resumen__check-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
                </div>
                <span>Pago procesado exitosamente</span>
              </div>
              <div class="conf-resumen__body">
                <div class="conf-resumen__row">
                  <span>No. reserva</span>
                  <strong class="conf-resumen__code">{{ noReservacion || '—' }}</strong>
                </div>
                <div class="conf-resumen__row" v-if="pasajeroNombre">
                  <span>Pasajero</span>
                  <strong>{{ pasajeroNombre }}</strong>
                </div>
                <div class="conf-resumen__row" v-if="tipoItem">
                  <span>Tipo</span>
                  <strong style="text-transform:capitalize">
                    {{ tipoItem === 'vuelo' ? 'Vuelo' : tipoItem === 'hotel' ? 'Hospedaje' : 'Paquete' }}
                  </strong>
                </div>
                <div class="conf-resumen__row">
                  <span>Fecha</span>
                  <strong>{{ fechaHoy }}</strong>
                </div>
              </div>
              <div class="conf-resumen__total">
                <span>Total pagado</span>
                <strong>{{ totalPagado !== '--' ? totalPagado : '—' }}</strong>
              </div>
            </div>

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
const API    = 'http://localhost:8080'

const noReservacion  = ref('')
const tipoItem       = ref('')
const itemData       = ref(null)
const detalleVuelo   = ref(null)
const detalleHotel   = ref(null)
const reservacionId  = ref(null)
const pasajeroNombre = ref('')
const totalPagado    = ref('--')
const descargando    = ref(false)
const pdfError       = ref('')

const fechaHoy = computed(() => {
  return new Date().toLocaleDateString('es-GT', { day:'2-digit', month:'long', year:'numeric' })
})

const boletos = computed(() => detalleVuelo.value?.detalle?.boletos || [])

function formatDuracion(min) {
  if (!min) return '--'
  return `${Math.floor(min / 60)}h${min % 60 > 0 ? ' ' + (min % 60) + 'm' : ''}`
}

onMounted(async () => {
  noReservacion.value = route.query.noReservacion || ''

  // 1. Leer checkout_data principal
  const raw = sessionStorage.getItem('checkout_data')
  if (raw) {
    try {
      const cd = JSON.parse(raw)
      tipoItem.value      = cd.tipoItem     || ''
      itemData.value      = cd.item         || null
      detalleVuelo.value  = cd.detalleVuelo || null
      detalleHotel.value  = cd.detalleHotel || null
      reservacionId.value = cd.reservacionId || null
      if (!noReservacion.value) noReservacion.value = cd.noReservacion || ''
      const p = cd.pasajero
      if (p) pasajeroNombre.value = `${p.nombre || ''} ${p.apellido || ''}`.trim()
      const tv = cd.detalleVuelo?.total_con_ganancia ?? 0
      const th = cd.detalleHotel?.total_con_ganancia ?? 0
      if      (cd.tipoItem === 'vuelo')   totalPagado.value = tv > 0      ? `$${tv.toFixed(2)}`       : '--'
      else if (cd.tipoItem === 'hotel')   totalPagado.value = th > 0      ? `$${th.toFixed(2)}`       : '--'
      else if (cd.tipoItem === 'paquete') totalPagado.value = (tv+th) > 0 ? `$${(tv+th).toFixed(2)}` : '--'
    } catch { /**/ }
  }

  // 2. Fallback: leer _reserva_id si checkout_data no tenía el ID
  if (!reservacionId.value) {
    const savedId = sessionStorage.getItem('_reserva_id')
    if (savedId) reservacionId.value = savedId
  }
  if (!noReservacion.value) {
    noReservacion.value = sessionStorage.getItem('_reserva_no') || ''
  }

  // 3. Último recurso: buscar en la API por noReservacion (URL query)
  if (!reservacionId.value && noReservacion.value) {
    try {
      const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
      const res = await fetch(`${API}/api/reservaciones/mias`, {
        credentials: 'include',
        headers: token ? { Authorization: `Bearer ${token}` } : {}
      })
      if (res.ok) {
        const lista = await res.json()
        const encontrada = lista.find(r => r.no_reservacion === noReservacion.value)
        if (encontrada) reservacionId.value = encontrada.id
      }
    } catch { /**/ }
  }

  // Limpiar sesión — ya leímos todo lo necesario
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
})

async function descargarPDF() {
  if (!reservacionId.value) { pdfError.value = 'No hay reservación disponible.'; return }
  descargando.value = true; pdfError.value = ''
  try {
    const res = await fetch(`${API}/api/reservaciones/${reservacionId.value}/pdf`, { credentials: 'include' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const blob = await res.blob()
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href = url; a.download = `reserva-${noReservacion.value || reservacionId.value}.pdf`
    a.click(); URL.revokeObjectURL(url)
  } catch {
    pdfError.value = 'No se pudo generar el PDF. Intenta más tarde.'
  } finally {
    descargando.value = false
  }
}
</script>