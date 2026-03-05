<template>
  <div class="page">
    <Encabezado />
    <div class="co-page">
      <div class="co-container">

        <div class="co-header">

          <div class="co-steps">
            <div class="co-step co-step--done">
              <div class="co-step__num">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
              <span class="co-step__lbl">Datos</span>
            </div>
            <div class="co-step__connector"></div>
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

          <!-- ═══ PAGO — tarjeta visual ═══ -->
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

            <!-- Formulario SOLO tarjeta -->
            <div class="pago-form">
              <div class="pago-form__field pago-form__field--full">
                <label class="pago-form__label">Número de tarjeta</label>
                <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='tarjeta' }">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                  <input
                    class="pago-form__input"
                    v-model="pago.tarjeta"
                    @input="formatTarjeta"
                    @focus="focusField='tarjeta'"
                    @blur="focusField=''"
                    placeholder="0000  0000  0000  0000"
                    maxlength="19"
                    type="text"
                    inputmode="numeric"
                  />
                  <div class="pago-form__type-badge">
                    <span v-if="tipoTarjeta==='visa'"       class="badge-visa">VISA</span>
                    <span v-else-if="tipoTarjeta==='mastercard'" class="badge-mc">MC</span>
                    <span v-else-if="tipoTarjeta==='amex'"  class="badge-amex">AMEX</span>
                  </div>
                </div>
              </div>

              <div class="pago-form__row">
                <div class="pago-form__field">
                  <label class="pago-form__label">Vencimiento</label>
                  <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='venc' }">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    <input
                      class="pago-form__input"
                      v-model="pago.vencimiento"
                      @input="formatVencimiento"
                      @focus="focusField='venc'"
                      @blur="focusField=''"
                      placeholder="MM/AA"
                      maxlength="5"
                      type="text"
                      inputmode="numeric"
                    />
                  </div>
                </div>
                <div class="pago-form__field">
                  <label class="pago-form__label">CVV</label>
                  <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='cvv' }">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                    <input
                      class="pago-form__input"
                      v-model="pago.cvv"
                      @input="e => { pago.cvv = e.target.value.replace(/\D/g,'').slice(0,4) }"
                      @focus="mostrandoCVV=true; focusField='cvv'"
                      @blur="mostrandoCVV=false; focusField=''"
                      placeholder="•••"
                      maxlength="4"
                      type="password"
                      inputmode="numeric"
                    />
                  </div>
                </div>
              </div>

              <div class="pago-form__field pago-form__field--full">
                <label class="pago-form__label">Nombre en la tarjeta</label>
                <div class="pago-form__input-wrap" :class="{ 'pago-form__input-wrap--focus': focusField==='nombre' }">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16" class="pago-form__ico"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input
                    class="pago-form__input pago-form__input--upper"
                    v-model="pago.nombre"
                    @focus="focusField='nombre'"
                    @blur="focusField=''"
                    placeholder="COMO APARECE EN LA TARJETA"
                    type="text"
                    autocomplete="cc-name"
                  />
                </div>
              </div>

              <div class="pago-badges">
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  Encriptación SSL
                </div>
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><polyline points="20 6 9 17 4 12"/></svg>
                  Pago seguro 3D
                </div>
                <div class="pago-badge">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2" width="13" height="13"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  Datos no almacenados
                </div>
              </div>

              <p v-if="formError" class="co-error">{{ formError }}</p>
              <p v-if="pagoError" class="co-error">{{ pagoError }}</p>

              <div class="pago-actions">
                <button class="pago-btn-confirm" @click="confirmarPago" :disabled="pagando" type="button">
                  <span v-if="pagando" class="pago-btn-confirm__spin"></span>
                  <template v-else>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                    Pagar ahora
                  </template>
                </button>
              </div>
            </div>

          </div>

          <!-- ═══ SIDEBAR resumen del pasajero + item ═══ -->
          <aside class="co-sidebar">
            <div class="co-resumen">
              <div class="co-resumen__head">Resumen de reserva</div>

              <!-- Pasajero -->
              <div v-if="reservaDatos?.pasajero" class="co-resumen__pasajero">
                <div class="co-resumen__pasajero-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="16" height="16"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div>
                  <p class="co-resumen__pasajero-nombre">{{ reservaDatos.pasajero.nombre }} {{ reservaDatos.pasajero.apellido }}</p>
                  <p class="co-resumen__pasajero-meta">{{ reservaDatos.pasajero.email }}</p>
                  <p class="co-resumen__pasajero-meta">{{ reservaDatos.pasajero.pais }}, {{ reservaDatos.pasajero.ciudad }}</p>
                </div>
              </div>

              <!-- Item (vuelo) -->
              <div v-if="itemSeleccionado" class="co-resumen__item">
                <div class="co-resumen__item-badge co-resumen__item-badge--vuelo" v-if="reservaDatos?.tipoItem==='vuelo'">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  Vuelo
                </div>
                <div class="co-resumen__item-badge co-resumen__item-badge--hotel" v-else-if="reservaDatos?.tipoItem==='hotel'">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  Hotel
                </div>
                <div class="co-resumen__item-badge co-resumen__item-badge--paquete" v-else-if="reservaDatos?.tipoItem==='paquete'">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                  Paquete
                </div>

                <!-- Ruta vuelo -->
                <div v-if="reservaDatos?.tipoItem==='vuelo'" class="co-resumen__ruta">
                  <span class="co-resumen__iata">{{ itemSeleccionado.origenCodigo }}</span>
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span class="co-resumen__iata">{{ itemSeleccionado.destinoCodigo }}</span>
                </div>

                <p class="co-resumen__item-nombre">
                  <template v-if="reservaDatos?.tipoItem==='vuelo'">{{ itemSeleccionado.aerolinea }} · {{ itemSeleccionado.numeroVuelo }}</template>
                  <template v-else-if="reservaDatos?.tipoItem==='hotel'">{{ itemSeleccionado.nombreHotel }}</template>
                  <template v-else>{{ itemSeleccionado.nombre }}</template>
                </p>
              </div>

              <!-- Precio -->
              <div class="co-resumen__precio-row">
                <span>Total a pagar</span>
                <strong>
                  <template v-if="reservaDatos?.tipoItem==='vuelo'">${{ itemSeleccionado?.precio?.toFixed(2) }}</template>
                  <template v-else-if="reservaDatos?.tipoItem==='hotel'">${{ itemSeleccionado?.precioPorNoche?.toFixed(2) }}</template>
                  <template v-else>Q{{ itemSeleccionado?.precioEspecial?.toLocaleString() }}</template>
                </strong>
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
const API    = 'http://localhost:7000'

const mostrandoCVV = ref(false)
const focusField   = ref('')
const formError    = ref('')
const pagoError    = ref('')
const pagando      = ref(false)

// Leer datos de Reserva.vue
const reservaDatos   = ref(null)
const itemSeleccionado = ref(null)

onMounted(() => {
  const raw = sessionStorage.getItem('reserva_datos')
  if (raw) {
    reservaDatos.value = JSON.parse(raw)
    // Leer también el item seleccionado para mostrar en sidebar
    const tipo = reservaDatos.value.tipoItem
    const itemRaw = sessionStorage.getItem(`${tipo}_seleccionado`)
    if (itemRaw) itemSeleccionado.value = JSON.parse(itemRaw)
  }
})

const pago = ref({ tarjeta: '', vencimiento: '', cvv: '', nombre: '' })

// Detección tipo tarjeta
const tipoTarjeta = computed(() => {
  const n = pago.value.tarjeta.replace(/\s/g, '')
  if (/^4/.test(n))        return 'visa'
  if (/^5[1-5]/.test(n))  return 'mastercard'
  if (/^(34|37)/.test(n)) return 'amex'
  return ''
})

// Grupos número
const numeroGrupos = computed(() => {
  const n = pago.value.tarjeta.replace(/\s/g, '').padEnd(16, '·')
  return [n.slice(0,4), n.slice(4,8), n.slice(8,12), n.slice(12,16)]
})

function formatTarjeta(e) {
  let val = e.target.value.replace(/\D/g,'').substring(0,16)
  pago.value.tarjeta = val.replace(/(.{4})/g,'$1 ').trim()
}

function formatVencimiento(e) {
  let val = e.target.value.replace(/\D/g,'').substring(0,4)
  if (val.length >= 3) val = val.slice(0,2) + '/' + val.slice(2)
  pago.value.vencimiento = val
}

async function confirmarPago() {
  formError.value = ''; pagoError.value = ''
  if (!pago.value.tarjeta || !pago.value.vencimiento || !pago.value.cvv || !pago.value.nombre) {
    formError.value = 'Completa todos los datos de la tarjeta.'
    return
  }
  if (pago.value.tarjeta.replace(/\s/g,'').length < 16) {
    formError.value = 'El número de tarjeta debe tener 16 dígitos.'
    return
  }
  if (!/^\d{3,4}$/.test(pago.value.cvv)) {
    formError.value = 'El CVV debe tener 3 o 4 dígitos.'
    return
  }

  pagando.value = true
  await new Promise(r => setTimeout(r, 1800))
  const noReservacion = 'MV-' + new Date().getFullYear() + '-' + String(Math.floor(Math.random()*99999)).padStart(5,'0')
  pagando.value = false
  router.push({ path: '/confirmacion', query: { noReservacion } })
}
</script>