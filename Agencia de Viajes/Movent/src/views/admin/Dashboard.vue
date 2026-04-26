<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- Barra lateral de navegación del panel de administración -->
        <aside class="adm-sidebar">
          <div class="adm-sidebar__head">
            <div class="adm-sidebar__logo">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="20" height="20"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            </div>
            <div>
              <p class="adm-sidebar__titulo">Panel Admin</p>
              <p class="adm-sidebar__rol">Administrador</p>
            </div>
          </div>
          <nav class="adm-nav">
            <router-link to="/admin/dashboard" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
              Dashboard
            </router-link>
            <router-link to="/admin/proveedores" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
              Proveedores
            </router-link>
            <router-link to="/admin/paquetes" class="adm-nav__item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            Finanzas
            </router-link>
            <router-link to="/admin/roles" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Roles
            </router-link>
            <router-link to="/admin/metricas" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              Métricas
            </router-link>
          </nav>
        </aside>

        <!-- Área principal del dashboard con KPIs, gráficas y tablas -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Dashboard</h1>
              <p class="adm-topbar__sub">Resumen general de Movent</p>
            </div>
            <div class="adm-topbar__fecha">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ fechaHoy }}
            </div>
          </div>

          <!-- Indicador de carga mientras llegan los datos del servidor -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando estadísticas...</p>
          </div>

          <template v-else>

            <!-- Fila 1: tarjetas KPI con métricas generales del sistema -->
            <div class="adm-kpis">
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Reservaciones totales</p>
                  <p class="adm-kpi__val">{{ stats.reservaciones ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Ingresos totales</p>
                  <p class="adm-kpi__val">${{ formatMoney(stats.ingresosTotales) }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--yellow">
                  <svg viewBox="0 0 24 24" fill="#1C1A18" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Vuelos reservados</p>
                  <p class="adm-kpi__val">{{ stats.vuelosReservados ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--black">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Hospedajes reservados</p>
                  <p class="adm-kpi__val">{{ stats.hotelesReservados ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--red">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Paquetes reservados</p>
                  <p class="adm-kpi__val">{{ stats.paquetesActivos ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--green">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Usuarios registrados</p>
                  <p class="adm-kpi__val">{{ stats.usuarios ?? '--' }}</p>
                </div>
              </div>
            </div>

            <!-- Fila 2: gráfica donut de estados y barras verticales por tipo de reserva -->
            <div class="adm-row2">

              <!-- Donut SVG que muestra la distribución de estados de reservaciones -->
              <div class="adm-card">
                <div class="adm-card__head">
                  <h3 class="adm-card__title">Estados de reservaciones</h3>
                  <span class="adm-card__count">{{ stats.reservaciones ?? 0 }} total</span>
                </div>
                <div class="adm-chart-wrap">
                  <!-- Gráfica de anillo construida con stroke-dasharray en SVG -->
                  <div class="adm-donut">
                    <svg viewBox="0 0 100 100" width="160" height="160">
                      <!-- Fondo gris del anillo -->
                      <circle cx="50" cy="50" r="36" fill="none" stroke="#f0ebe3" stroke-width="14"/>
                      <!-- Segmentos coloreados por estado -->
                      <circle
                        v-for="(seg, i) in donutSegments"
                        :key="i"
                        cx="50" cy="50" r="36"
                        fill="none"
                        :stroke="seg.color"
                        stroke-width="14"
                        :stroke-dasharray="`${seg.dash} ${226.2 - seg.dash}`"
                        :stroke-dashoffset="seg.offset"
                        style="transform: rotate(-90deg); transform-origin: 50px 50px; transition: stroke-dasharray 0.6s ease;"
                      />
                      <!-- Texto central con el total de reservas -->
                      <text x="50" y="46" text-anchor="middle" font-size="14" font-weight="800" fill="#1C1A18">{{ stats.reservaciones ?? 0 }}</text>
                      <text x="50" y="58" text-anchor="middle" font-size="7" fill="#9a9089">reservas</text>
                    </svg>
                    <!-- Leyenda de colores por estado -->
                    <div class="adm-donut__legend">
                      <div v-for="e in estadosRes" :key="e.label" class="adm-donut__legend-item">
                        <span class="adm-donut__legend-dot" :style="{ background: e.color }"></span>
                        <span class="adm-donut__legend-label">{{ e.label }}</span>
                        <span class="adm-donut__legend-val">{{ e.val ?? 0 }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Barras verticales que muestran la distribución por tipo (vuelo, hotel, paquete) -->
              <div class="adm-card">
                <div class="adm-card__head">
                  <h3 class="adm-card__title">Distribución por tipo</h3>
                  <span class="adm-card__count">{{ stats.reservaciones ?? 0 }} total</span>
                </div>
                <div class="adm-vchart">
                  <div class="adm-vchart__bars">
                    <div v-for="b in barData" :key="b.label" class="adm-vchart__col">
                      <span class="adm-vchart__val">{{ b.val }}</span>
                      <div class="adm-vchart__bar-wrap">
                        <div
                          class="adm-vchart__bar"
                          :style="{ height: (b.pct || 2) + '%', background: b.color }"
                        ></div>
                      </div>
                      <span class="adm-vchart__pct">{{ b.pct }}%</span>
                      <span class="adm-vchart__label">{{ b.label }}</span>
                    </div>
                  </div>
                  <!-- Mini métricas de conteo de proveedores y usuarios -->
                  <div class="adm-mini-metrics">
                    <div class="adm-mini-metric">
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <span class="adm-mini-metric__val">{{ stats.aerolineas }}</span>
                      <span class="adm-mini-metric__lbl">Aerolíneas</span>
                    </div>
                    <div class="adm-mini-metric">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#1C1A18" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      <span class="adm-mini-metric__val">{{ stats.hoteles }}</span>
                      <span class="adm-mini-metric__lbl">Hoteles</span>
                    </div>
                    <div class="adm-mini-metric">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" width="14" height="14"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      <span class="adm-mini-metric__val">{{ stats.usuarios }}</span>
                      <span class="adm-mini-metric__lbl">Usuarios</span>
                    </div>
                  </div>
                </div>
              </div>

            </div>

            <!-- Fila 3: listado de proveedores configurados con su estado de conexión -->
            <div class="adm-card">
              <div class="adm-card__head">
                <h3 class="adm-card__title">Proveedores conectados</h3>
                <button class="adm-card__link" @click="$router.push('/admin/proveedores')" type="button">Ver todos</button>
              </div>
              <div v-if="proveedores.length === 0" class="adm-card__empty">Sin proveedores configurados</div>
              <div v-else class="adm-proveedores">
                <div v-for="p in proveedores" :key="p.id" class="adm-proveedor">
                  <div class="adm-proveedor__info">
                    <div class="adm-proveedor__tipo-icon" :class="p.tipoProveedorId===1 ? 'adm-proveedor__tipo-icon--aerolinea' : 'adm-proveedor__tipo-icon--hotel'">
                      <svg v-if="p.tipoProveedorId===1" viewBox="0 0 24 24" fill="currentColor" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    </div>
                    <div>
                      <p class="adm-proveedor__nombre">{{ p.nombre }}</p>
                      <p class="adm-proveedor__url">{{ p.url }}</p>
                    </div>
                  </div>
                  <span class="adm-badge" :class="p.activo ? 'adm-badge--on' : 'adm-badge--off'">
                    {{ p.activo ? 'Activo' : 'Inactivo' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Fila 4: tabla con las reservaciones más recientes de todos los usuarios -->
            <div class="adm-card adm-card--full">
              <div class="adm-card__head">
                <h3 class="adm-card__title">Reservaciones recientes</h3>
                <span class="adm-card__count">Todos los usuarios</span>
              </div>
              <div v-if="reservacionesRecientes.length === 0" class="adm-card__empty">Sin reservaciones recientes</div>
              <div v-else class="adm-tabla-wrap">
                <table class="adm-tabla">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Usuario</th>
                      <th>Tipo</th>
                      <th>Fecha</th>
                      <th>Total</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in reservacionesRecientes" :key="r.id">
                      <td><span class="adm-tabla__codigo">{{ r.noReservacion }}</span></td>
                      <td>{{ r.usuario }}</td>
                      <td><span class="adm-tabla__tipo" :class="`adm-tabla__tipo--${r.tipo}`">{{ r.tipo }}</span></td>
                      <td>{{ formatFecha(r.fechaReservacion) }}</td>
                      <td><strong>${{ r.totalReservacion?.toFixed(2) }}</strong></td>
                      <td><span class="adm-badge" :class="`adm-badge--${r.estado}`">{{ r.estado }}</span></td>
                      <td>
                        <button
                          v-if="puedeCancel(r)"
                          class="adm-btn adm-btn--sm adm-btn--danger-ghost"
                          @click="abrirModalCancelacion(r)"
                          type="button"
                        >Cancelar</button>
                        <span v-else style="color:#c9c0b8;font-size:13px;">—</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

          </template>
        </div>
      </div>
    </div>

    <Piepagina />

    <!-- Modal de confirmación para cancelar una reservación desde el panel admin -->
    <div v-if="modalCancelacion" class="adm-modal-overlay" @click.self="cerrarModalCancelacion">
      <div class="adm-modal adm-modal--sm">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">Cancelar reservación</h3>
          <button class="adm-modal__close" @click="cerrarModalCancelacion" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="adm-modal__body">
          <p class="adm-cancel-info">
            Vas a cancelar la reservación
            <strong>{{ modalCancelacion.noReservacion }}</strong>
            del usuario <strong>{{ modalCancelacion.usuario }}</strong>.
            Esta acción notificará al cliente por correo electrónico y no se puede deshacer.
          </p>

          <p class="adm-modal__lbl" style="margin-top:16px">
            Motivo de la cancelación <span style="color:#D40511">*</span>
          </p>
          <textarea
            ref="textareaRef"
            v-model="motivoCancelacion"
            class="adm-field__input adm-field__textarea adm-field__textarea--cancel"
            rows="4"
            maxlength="500"
            placeholder="Ej: Pago rechazado por el banco, duplicación de reservación, solicitud del cliente por teléfono..."
          ></textarea>
          <p
            class="adm-field-hint"
            :class="{ 'adm-field-hint--err': motivoCancelacion.trim().length > 0 && motivoCancelacion.trim().length < 10 }"
          >Mínimo 10 caracteres ({{ motivoCancelacion.trim().length }}/500)</p>
        </div>

        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="cerrarModalCancelacion" type="button">Cerrar</button>
          <button
            class="adm-btn adm-btn--danger"
            @click="confirmarCancelacion"
            :disabled="motivoCancelacion.trim().length < 10 || cancelando"
            type="button"
          >
            <div v-if="cancelando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>Confirmar cancelación</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Toast de notificación de éxito o error -->
    <div v-if="toast" class="adm-toast" :class="`adm-toast--${toast.tipo}`">
      <svg v-if="toast.tipo==='ok'" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><polyline points="20 6 9 17 4 12"/></svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      {{ toast.msg }}
    </div>

  </div>
</template>

<script setup>
/**
 * @file Dashboard.vue
 * @description Vista principal del panel de administración. Muestra KPIs globales,
 * gráfica de estados de reservaciones (donut SVG), distribución por tipo (barras),
 * lista de proveedores y tabla de reservaciones recientes.
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

/** Instancia del router para navegación programática. */
const router  = useRouter()

/** URL base del backend. @type {string} */
const API     = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Controla si los datos están siendo cargados desde el servidor. @type {import('vue').Ref<boolean>} */
const loading = ref(true)

/** Objeto con todas las estadísticas generales del sistema. @type {import('vue').Ref<Object>} */
const stats                  = ref({})

/** Lista de proveedores configurados (aerolíneas y hoteles). @type {import('vue').Ref<Array>} */
const proveedores            = ref([])

/** Las últimas reservaciones realizadas por cualquier usuario. @type {import('vue').Ref<Array>} */
const reservacionesRecientes = ref([])

/** Referencia al textarea del modal para enfocar al abrir. @type {import('vue').Ref<HTMLElement|null>} */
const textareaRef           = ref(null)

/** Reservación seleccionada para cancelar (null = modal cerrado). @type {import('vue').Ref<Object|null>} */
const modalCancelacion      = ref(null)

/** Motivo de cancelación ingresado por el administrador. @type {import('vue').Ref<string>} */
const motivoCancelacion     = ref('')

/** Indica si hay una cancelación en proceso. @type {import('vue').Ref<boolean>} */
const cancelando            = ref(false)

/** Notificación temporal de éxito o error. @type {import('vue').Ref<{tipo: string, msg: string}|null>} */
const toast                 = ref(null)

/**
 * Fecha de hoy formateada al español de Guatemala para mostrar en el topbar.
 * @type {string}
 */
const fechaHoy = new Date().toLocaleDateString('es-GT', {
  weekday: 'long', day: '2-digit', month: 'long', year: 'numeric'
})

/**
 * Lista de estados posibles de una reservación con su color para la gráfica donut.
 * Se construye reactivamente desde los datos de stats.
 * @type {import('vue').ComputedRef<Array<{label: string, val: number, color: string}>>}
 */
const estadosRes = computed(() => [
  { label: 'Confirmadas', val: stats.value.confirmadas, color: '#22c55e' },
  { label: 'Pendientes',  val: stats.value.pendientes,  color: '#f59e0b' },
  { label: 'Completadas', val: stats.value.completadas, color: '#3b82f6' },
  { label: 'En Curso',    val: stats.value.enCurso,     color: '#8b5cf6' },
  { label: 'Canceladas',  val: stats.value.canceladas,  color: '#D40511' },
  { label: 'Expiradas',   val: stats.value.expiradas,   color: '#d1cdc7' },
])

/**
 * Suma total de reservaciones entre todos los estados, usado como denominador en la donut.
 * Se le da al menos 1 para evitar división por cero.
 * @type {import('vue').ComputedRef<number>}
 */
const totalRes = computed(() =>
  estadosRes.value.reduce((s, e) => s + (e.val || 0), 0) || 1
)

/**
 * Circunferencia del círculo SVG con radio 36.
 * Fórmula: 2 * PI * 36 ≈ 226.2
 * @type {number}
 */
const CIRC = 226.2

/**
 * Calcula los segmentos del donut como offsets y dasharray de SVG.
 * Cada segmento ocupa un arco proporcional a su cantidad de reservaciones.
 * @type {import('vue').ComputedRef<Array<{color: string, dash: number, offset: number}>>}
 */
const donutSegments = computed(() => {
  let cumulative = 0
  return estadosRes.value.map(e => {
    const pct  = (e.val || 0) / totalRes.value
    const dash = pct * CIRC
    // stroke-dashoffset negativo para mover el inicio del segmento
    const offset = -(cumulative * CIRC) + CIRC * 0.25 // +0.25 para empezar arriba
    cumulative += pct
    return { color: e.color, dash, offset }
  })
})

/**
 * Datos para las barras verticales por tipo de reservación.
 * Incluye el valor absoluto y el porcentaje respecto al total.
 * @type {import('vue').ComputedRef<Array<{label: string, val: number, color: string, pct: number}>>}
 */
const barData = computed(() => {
  const total = stats.value.reservaciones || 1
  return [
    { label: 'Vuelos',   val: stats.value.vuelosReservados  || 0, color: '#FFCC00', pct: Math.round((stats.value.vuelosReservados  || 0) / total * 100) },
    { label: 'Hoteles',  val: stats.value.hotelesReservados || 0, color: '#1C1A18', pct: Math.round((stats.value.hotelesReservados || 0) / total * 100) },
    { label: 'Paquetes', val: stats.value.paquetesActivos   || 0, color: '#D40511', pct: Math.round((stats.value.paquetesActivos   || 0) / total * 100) },
  ]
})

/** Cierra el modal de cancelación al presionar ESC. */
function onKeydown(e) {
  if (e.key === 'Escape' && modalCancelacion.value) cerrarModalCancelacion()
}

/** Carga todos los datos al montar el componente y registra el listener de teclado. */
onMounted(() => {
  cargarTodo()
  window.addEventListener('keydown', onKeydown)
})

/** Limpia el listener de teclado al desmontar el componente. */
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})

/**
 * Carga en paralelo las estadísticas, los proveedores, las reservaciones recientes
 * y las métricas financieras. ingresosTotales se obtiene de /api/admin/metricas
 * (resumen.totalCobrado) para mantener /api/stats como endpoint público sin
 * datos financieros sensibles.
 * @returns {Promise<void>}
 */
async function cargarTodo() {
  loading.value = true
  try {
    const [resStats, resProv, resRec, resMet] = await Promise.all([
      fetch(`${API}/api/stats`,                         { credentials: 'include' }),
      fetch(`${API}/api/proveedores`,                   { credentials: 'include' }),
      fetch(`${API}/api/admin/reservaciones/recientes`, { credentials: 'include' }),
      fetch(`${API}/api/admin/metricas`,                { credentials: 'include' }),
    ])
    if (resStats.ok) stats.value = await resStats.json()
    if (resProv.ok)  proveedores.value            = await resProv.json()
    if (resRec.ok)   reservacionesRecientes.value = await resRec.json()
    // ingresosTotales ya no está en /api/stats; lo obtenemos de /api/admin/metricas
    if (resMet.ok) {
      const metricas = await resMet.json()
      stats.value = { ...stats.value, ingresosTotales: metricas.resumen?.totalCobrado ?? 0 }
    }
  } catch (e) {
    console.error('Error cargando dashboard:', e)
  } finally {
    loading.value = false
  }
}

/**
 * Recarga solo la tabla de reservaciones recientes sin refrescar el dashboard completo.
 * Se llama tras una cancelación exitosa para reflejar el estado actualizado.
 * @returns {Promise<void>}
 */
async function cargarRecientes() {
  try {
    const res = await fetch(`${API}/api/admin/reservaciones/recientes`, { credentials: 'include' })
    if (res.ok) reservacionesRecientes.value = await res.json()
  } catch (e) {
    console.error('Error recargando reservaciones:', e)
  }
}

/**
 * Retorna true si la reservación puede ser cancelada según su estado.
 * Los estados cancelables son pendiente y confirmada. El estado retenida (7)
 * aparece como "pendiente" en este endpoint, por lo que queda cubierto.
 * @param {Object} r - La reservación a evaluar.
 * @returns {boolean}
 */
function puedeCancel(r) {
  return r.estado === 'pendiente' || r.estado === 'confirmada'
}

/**
 * Abre el modal de cancelación para la reservación indicada y enfoca el textarea.
 * @param {Object} r - La reservación a cancelar.
 */
function abrirModalCancelacion(r) {
  modalCancelacion.value  = r
  motivoCancelacion.value = ''
  cancelando.value        = false
  nextTick(() => textareaRef.value?.focus())
}

/**
 * Cierra el modal de cancelación y limpia el estado. No cierra si hay un fetch activo.
 */
function cerrarModalCancelacion() {
  if (cancelando.value) return
  modalCancelacion.value  = null
  motivoCancelacion.value = ''
}

/**
 * Ejecuta la cancelación de la reservación seleccionada llamando al endpoint admin.
 * Si tiene éxito: cierra el modal, muestra toast verde y refresca la tabla.
 * Si falla: muestra toast rojo con el error específico.
 * @returns {Promise<void>}
 */
async function confirmarCancelacion() {
  if (!modalCancelacion.value || motivoCancelacion.value.trim().length < 10) return
  cancelando.value = true
  try {
    const res = await fetch(
      `${API}/api/admin/reservaciones/${modalCancelacion.value.id}/cancelar`,
      {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ motivo: motivoCancelacion.value.trim() }),
      }
    )
    if (res.ok) {
      modalCancelacion.value  = null
      motivoCancelacion.value = ''
      mostrarToast('ok', 'Reservación cancelada. Correo enviado al cliente.')
      await cargarRecientes()
    } else {
      const data = await res.json().catch(() => ({}))
      if      (res.status === 404) mostrarToast('err', 'La reservación no existe.')
      else if (res.status === 409) mostrarToast('err', 'La reservación ya estaba cancelada.')
      else                         mostrarToast('err', data.error || 'Error al cancelar la reservación.')
      cancelando.value = false
    }
  } catch (e) {
    console.error('Error cancelando reservación:', e)
    mostrarToast('err', 'Error de conexión al cancelar.')
    cancelando.value = false
  }
}

/**
 * Muestra una notificación toast y la oculta automáticamente tras 3.5 segundos.
 * @param {'ok'|'err'} tipo - Tipo de notificación.
 * @param {string} msg - Mensaje a mostrar.
 */
function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => { toast.value = null }, 3500)
}

/**
 * Formatea un número como dinero con separadores de miles y dos decimales.
 * @param {number|null} n - El valor a formatear.
 * @returns {string} El número formateado o '--' si no hay valor.
 */
function formatMoney(n) {
  if (!n && n !== 0) return '--'
  return Number(n).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * Formatea una cadena de fecha ISO a formato legible en español de Guatemala.
 * @param {string} f - La fecha en formato ISO o string.
 * @returns {string} La fecha formateada o '--' si no hay valor.
 */
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) }
  catch { return f }
}
</script>
