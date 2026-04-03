<template>
  <div class="page">
    <Encabezado />
    <div class="co-page">
      <div class="co-container">

        <!-- ═══ STEPS ═══ -->
        <div class="co-header">
          <div class="co-steps">
            <div class="co-step co-step--done">
              <div class="co-step__num">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
              <span class="co-step__lbl">Datos</span>
            </div>
            <div class="co-step__connector co-step__connector--done"></div>
            <div class="co-step co-step--active">
              <div class="co-step__num">2</div>
              <span class="co-step__lbl">Pago</span>
            </div>
            <div class="co-step__connector"></div>
            <div class="co-step">
              <div class="co-step__num">3</div>
              <span class="co-step__lbl">Confirmación</span>
            </div>
          </div>
        </div>

        <div class="co-layout">

          <!-- ═══ PAGO ═══ -->
          <div class="pago-stage">

            <!-- Tarjeta 3D flip -->
            <div class="card-scene">
              <div class="card-wrap" :class="{ 'card-wrap--flip': mostrandoCVV }">

                <!-- FRENTE -->
                <div class="card-face card-face--front">
                  <div class="card-noise"></div>
                  <div class="card-top">
                    <div class="card-brand">
                      <svg viewBox="0 0 24 24" fill="none" width="22" height="22"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z" fill="#FFCC00"/></svg>
                      <span>MOVENT</span>
                    </div>
                    <div class="card-type-icon">
                      <div v-if="tipoTarjeta==='visa'" class="card-visa">VISA</div>
                      <div v-else-if="tipoTarjeta==='mastercard'" class="card-mc">
                        <div class="card-mc__l"></div><div class="card-mc__r"></div>
                      </div>
                      <div v-else-if="tipoTarjeta==='amex'" class="card-amex">AMEX</div>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="1.5" width="36" height="36"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                    </div>
                  </div>
                  <div class="card-chip">
                    <div class="card-chip__inner">
                      <div class="card-chip__h"></div>
                      <div class="card-chip__v"></div>
                    </div>
                  </div>
                  <div class="card-number">
                    <span v-for="(g, i) in numeroGrupos" :key="i" class="card-number__group">{{ g }}</span>
                  </div>
                  <div class="card-foot">
                    <div class="card-foot__holder">
                      <span class="card-foot__lbl">Titular</span>
                      <span class="card-foot__val">{{ pago.nombre || 'NOMBRE APELLIDO' }}</span>
                    </div>
                    <div class="card-foot__exp">
                      <span class="card-foot__lbl">Vence</span>
                      <span class="card-foot__val">{{ pago.vencimiento || 'MM/AA' }}</span>
                    </div>
                  </div>
                </div>

                <!-- REVERSO -->
                <div class="card-face card-face--back">
                  <div class="card-noise"></div>
                  <div class="card-stripe"></div>
                  <div class="card-cvv-row">
                    <span class="card-cvv__lbl">CVV</span>
                    <div class="card-cvv__box">{{ pago.cvv ? '•'.repeat(pago.cvv.length) : '•••' }}</div>
                  </div>
                  <div class="card-back-brand">
                    <svg viewBox="0 0 24 24" fill="none" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z" fill="#FFCC00"/></svg>
                    MOVENT
                  </div>
                </div>

              </div>
            </div>

            <!-- ── Formulario de pago ── -->
            <div class="pago-form">

              <!-- Número tarjeta -->
              <div class="pago-form__field pago-form__field--full">
                <label class="pago-form__label">Número de tarjeta</label>
                <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='tarjeta' }">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                  <input class="pago-form__input" v-model="pago.tarjeta"
                    @input="formatTarjeta" @focus="focusField='tarjeta'" @blur="focusField=''"
                    placeholder="0000  0000  0000  0000" maxlength="19" type="text" inputmode="numeric"/>
                  <div class="pago-form__type-badge">
                    <span v-if="tipoTarjeta==='visa'"       class="badge-visa">VISA</span>
                    <span v-else-if="tipoTarjeta==='mastercard'" class="badge-mc">MC</span>
                    <span v-else-if="tipoTarjeta==='amex'"  class="badge-amex">AMEX</span>
                  </div>
                </div>
              </div>

              <!-- Vencimiento + CVV -->
              <div class="pago-form__row">
                <div class="pago-form__field">
                  <label class="pago-form__label">Vencimiento</label>
                  <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='venc' }">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    <input class="pago-form__input" v-model="pago.vencimiento"
                      @input="formatVencimiento" @focus="focusField='venc'" @blur="focusField=''"
                      placeholder="MM/AA" maxlength="5" type="text" inputmode="numeric"/>
                  </div>
                </div>
                <div class="pago-form__field">
                  <label class="pago-form__label">CVV</label>
                  <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='cvv' }">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    <input class="pago-form__input" v-model="pago.cvv"
                      @input="e => { pago.cvv = e.target.value.replace(/\D/g,'').slice(0,3) }"
                      @focus="mostrandoCVV=true; focusField='cvv'"
                      @blur="mostrandoCVV=false; focusField=''"
                      placeholder="•••" maxlength="3" type="password" inputmode="numeric"/>
                  </div>
                </div>
              </div>

              <!-- Nombre en tarjeta -->
              <div class="pago-form__field pago-form__field--full">
                <label class="pago-form__label">Nombre en la tarjeta</label>
                <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='nombre' }">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input class="pago-form__input pago-form__input--upper" v-model="pago.nombre"
                    @focus="focusField='nombre'" @blur="focusField=''"
                    placeholder="COMO APARECE EN LA TARJETA" type="text" autocomplete="cc-name"/>
                </div>
              </div>

              <!-- ── Datos de facturación ── -->
              <div class="pago-billing">
                <div class="pago-billing__title">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                  Datos de facturación
                </div>
                <div class="pago-form__row">
                  <div class="pago-form__field">
                    <label class="pago-form__label">NIT <span class="pago-form__hint">(solo números)</span></label>
                    <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='nit' }">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/></svg>
                      <input class="pago-form__input" v-model="pago.nit"
                        @input="pago.nit = pago.nit.replace(/\D/g, '')"
                        @focus="focusField='nit'" @blur="focusField=''"
                        placeholder="12345678" type="text" inputmode="numeric" autocomplete="off"/>
                    </div>
                  </div>
                  <div class="pago-form__field">
                    <label class="pago-form__label">Código Postal</label>
                    <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='postal' }">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                      <input class="pago-form__input" v-model="pago.codigoPostal"
                        @input="pago.codigoPostal = pago.codigoPostal.replace(/\D/g,'').slice(0,5)"
                        @focus="focusField='postal'" @blur="focusField=''"
                        placeholder="01001" maxlength="5" type="text" inputmode="numeric"/>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Badges seguridad -->
              <div class="pago-badges">
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                </div>
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
                </div>
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                </div>
              </div>

              <p v-if="formError" class="co-error">{{ formError }}</p>
              <p v-if="pagoError" class="co-error co-error--api">{{ pagoError }}</p>

              <div class="pago-actions">
                <button class="pago-btn-confirm" @click="confirmarPago" :disabled="pagando" type="button">
                  <span v-if="pagando" class="pago-btn-confirm__spin"></span>
                  <template v-else>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                    Pagar {{ totalDisplay }}
                  </template>
                </button>
              </div>
            </div>

          </div><!-- /pago-stage -->

          <!-- ═══ SIDEBAR RESUMEN ═══ -->
          <aside class="co-sidebar">
            <div class="co-resumen">
              <div class="co-resumen__head">Resumen de reserva</div>

              <!-- Número de reserva -->
              <div class="co-resumen__nro">
                <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/></svg>
                <span>{{ cd?.noReservacion || '—' }}</span>
              </div>

              <!-- ── VUELO solo ida ── -->
              <template v-if="tipoItem === 'vuelo' && cd?.item?.tipoVuelo === 'ida'">
                <div class="co-resumen__item">
                  <div class="co-resumen__item-badge co-resumen__item-badge--vuelo">
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    Vuelo · Solo ida
                  </div>
                  <div class="co-resumen__ruta">
                    <span class="co-resumen__iata">{{ cd.item.origenCodigo }}</span>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <span class="co-resumen__iata">{{ cd.item.destinoCodigo }}</span>
                  </div>
                  <p class="co-resumen__item-nombre">{{ cd.item.aerolinea }} · Vuelo {{ cd.item.numeroVuelo }}</p>
                  <p class="co-resumen__item-nombre">{{ cd.item.horaSalida }} → {{ cd.item.horaLlegada }} · {{ cd.item.clase }}</p>
                </div>
              </template>

              <!-- ── VUELO ida y vuelta ── -->
              <template v-else-if="tipoItem === 'vuelo' && cd?.item?.tipoVuelo === 'idaVuelta'">
                <div class="co-resumen__item">
                  <div class="co-resumen__item-badge co-resumen__item-badge--vuelo">
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    Vuelo · Ida y vuelta
                  </div>
                  <div class="co-resumen__ruta">
                    <span class="co-resumen__iata">{{ cd.item.ida?.origenCodigo }}</span>
                    <svg viewBox="0 0 24 24" fill="#9a9089" width="12" height="12"><path d="M8 3l4-1 4 1v14l-4 1-4-1V3z" fill="none" stroke="currentColor" stroke-width="2"/></svg>
                    <span class="co-resumen__iata">{{ cd.item.ida?.destinoCodigo }}</span>
                  </div>
                  <p class="co-resumen__item-nombre">Ida: {{ cd.item.ida?.aerolinea }} · Vuelo {{ cd.item.ida?.numeroVuelo }}</p>
                  <p class="co-resumen__item-nombre">Regreso: {{ cd.item.regreso?.aerolinea }} · Vuelo {{ cd.item.regreso?.numeroVuelo }}</p>
                </div>
              </template>

              <!-- ── HOTEL ── -->
              <template v-else-if="tipoItem === 'hotel'">
                <div class="co-resumen__item">
                  <div class="co-resumen__item-badge co-resumen__item-badge--hotel">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    Hospedaje
                  </div>
                  <p class="co-resumen__item-nombre co-resumen__item-nombre--lg">{{ cd?.item?.nombreHotel }}</p>
                  <p class="co-resumen__item-nombre">{{ cd?.item?.hotelCiudad || cd?.item?.busqueda?.ciudad }}</p>
                  <p class="co-resumen__item-nombre">{{ cd?.item?.busqueda?.checkIn }} → {{ cd?.item?.busqueda?.checkOut }} · {{ cd?.item?.noches }} noches</p>
                  <p class="co-resumen__item-nombre">{{ cd?.item?.tipoHabitacion }}</p>
                </div>
              </template>

              <!-- ── PAQUETE ── -->
              <template v-else-if="tipoItem === 'paquete'">
                <div class="co-resumen__item">
                  <div class="co-resumen__item-badge co-resumen__item-badge--paquete">
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                    Paquete completo
                  </div>
                  <div class="co-resumen__ruta">
                    <span class="co-resumen__iata">{{ cd?.item?.vuelo?.origenCodigo }}</span>
                    <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <span class="co-resumen__iata">{{ cd?.item?.vuelo?.destinoCodigo }}</span>
                  </div>
                  <p class="co-resumen__item-nombre">{{ cd?.item?.vuelo?.aerolinea }}</p>
                  <div class="co-resumen__divider"></div>
                  <p class="co-resumen__item-nombre co-resumen__item-nombre--lg">{{ cd?.item?.hotel?.nombreHotel }}</p>
                  <p class="co-resumen__item-nombre">{{ cd?.item?.noches }} noches · {{ cd?.item?.hotel?.tipoHabitacion }}</p>
                </div>
              </template>

              <!-- Pasajero -->
              <div v-if="cd?.pasajero" class="co-resumen__pasajero">
                <div class="co-resumen__pasajero-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="16" height="16"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div>
                  <p class="co-resumen__pasajero-nombre">{{ cd.pasajero.nombre }} {{ cd.pasajero.apellido }}</p>
                  <p class="co-resumen__pasajero-meta">{{ cd.pasajero.pais }}, {{ cd.pasajero.ciudad }}</p>
                </div>
              </div>

              <!-- Total -->
              <div class="co-resumen__precio-row">
                <span>Total a pagar</span>
                <strong>{{ totalDisplay }}</strong>
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
import '../styles/checkout.css'

const router = useRouter()
const API    = 'http://localhost:8080'

// ── Datos de la reserva ───────────────────────────────────────
const cd = ref(null)

const tipoItem = computed(() => cd.value?.tipoItem || '')

const totalDisplay = computed(() => {
  if (!cd.value) return '--'
  const tv = cd.value.detalleVuelo?.total_con_ganancia ?? 0
  const th = cd.value.detalleHotel?.total_con_ganancia ?? 0
  if (cd.value.tipoItem === 'vuelo')   return tv > 0 ? `$${tv.toFixed(2)}` : '--'
  if (cd.value.tipoItem === 'hotel')   return th > 0 ? `$${th.toFixed(2)}` : '--'
  if (cd.value.tipoItem === 'paquete') return (tv + th) > 0 ? `$${(tv + th).toFixed(2)}` : '--'
  return '--'
})

// ── Formulario ────────────────────────────────────────────────
const mostrandoCVV = ref(false)
const focusField   = ref('')
const formError    = ref('')
const pagoError    = ref('')
const pagando      = ref(false)

const pago = ref({
  tarjeta:      '',
  vencimiento:  '',
  cvv:          '',
  nombre:       '',
  nit:          '',
  codigoPostal: '',
})

// ── Tipo de tarjeta ───────────────────────────────────────────
const tipoTarjeta = computed(() => {
  const n = pago.value.tarjeta.replace(/\s/g, '')
  if (/^4/.test(n))        return 'visa'
  if (/^5[1-5]/.test(n))  return 'mastercard'
  if (/^(34|37)/.test(n)) return 'amex'
  return ''
})

const numeroGrupos = computed(() => {
  const n = pago.value.tarjeta.replace(/\s/g, '').padEnd(16, '·')
  return [n.slice(0,4), n.slice(4,8), n.slice(8,12), n.slice(12,16)]
})

// ── Formatters ────────────────────────────────────────────────
function formatTarjeta(e) {
  const val = e.target.value.replace(/\D/g,'').substring(0,16)
  pago.value.tarjeta = val.replace(/(.{4})/g,'$1 ').trim()
}
function formatVencimiento(e) {
  let val = e.target.value.replace(/\D/g,'').substring(0,4)
  if (val.length >= 3) val = val.slice(0,2) + '/' + val.slice(2)
  pago.value.vencimiento = val
}

// ── onMounted ─────────────────────────────────────────────────
onMounted(() => {
  const raw = sessionStorage.getItem('checkout_data')
  if (!raw) { router.push('/principal'); return }
  try {
    cd.value = JSON.parse(raw)
    if (!cd.value?.reservacionId) { router.push('/principal'); return }
  } catch {
    router.push('/principal')
  }
})

// ── Confirmar pago ────────────────────────────────────────────
async function confirmarPago() {
  formError.value = ''; pagoError.value = ''

  if (!pago.value.tarjeta || !pago.value.vencimiento || !pago.value.cvv ||
      !pago.value.nombre   || !pago.value.nit         || !pago.value.codigoPostal) {
    formError.value = 'Completa todos los campos requeridos.'
    return
  }

  const numSinEspacios = pago.value.tarjeta.replace(/\s/g, '')
  if (numSinEspacios.length < 16) {
    formError.value = 'El número de tarjeta debe tener 16 dígitos.'
    return
  }
  if (pago.value.cvv.length !== 3) {
    formError.value = 'El CVV debe tener 3 dígitos.'
    return
  }

  const partes = pago.value.vencimiento.split('/')
  if (partes.length !== 2 || partes[0].length !== 2 || partes[1].length !== 2) {
    formError.value = 'Formato de vencimiento inválido (MM/AA).'
    return
  }
  const [mes, anio2] = partes

  if (pago.value.codigoPostal.length !== 5) {
    formError.value = 'El código postal debe tener 5 dígitos.'
    return
  }

  if (!pago.value.nit.trim()) {
    formError.value = 'El NIT es requerido.'
    return
  }

  pagando.value = true
  try {
    const res = await fetch(`${API}/api/reservaciones/pagar`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        reservacion_id:  Number(cd.value.reservacionId),
        tarjeta_numero:  numSinEspacios,
        tarjeta_cvv:     pago.value.cvv,
        tarjeta_mes:     mes,
        tarjeta_anio:    `20${anio2}`,
        nit:             pago.value.nit.trim(),
        codigo_postal:   pago.value.codigoPostal,
      }),
    })

    const data = await res.json().catch(() => ({}))
    if (!res.ok) throw new Error(data.error || data.mensaje || `Error ${res.status}`)

    // Correo automático post-pago (fire-and-forget)
    fetch(`${API}/api/reservaciones/${cd.value.reservacionId}/correo`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
    }).catch(() => {})

    // Pago exitoso → ir a confirmación
    router.push({
      path:  '/confirmacion',
      query: { noReservacion: cd.value.noReservacion },
    })

  } catch (err) {
    pagoError.value = err.message || 'Error al procesar el pago. Intenta de nuevo.'
  } finally {
    pagando.value = false
  }
}
</script>