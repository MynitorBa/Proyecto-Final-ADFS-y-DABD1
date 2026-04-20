<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- Sidebar exclusivo del panel WebService -->
        <aside class="adm-sidebar adm-sidebar--ws">
          <div class="adm-sidebar__head">
            <div class="adm-sidebar__logo adm-sidebar__logo--ws">
              <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            </div>
            <div>
              <p class="adm-sidebar__titulo">Panel WebService</p>
              <p class="adm-sidebar__rol">Integración REST</p>
            </div>
          </div>
          <nav class="adm-nav">
            <router-link to="/admin/webservice" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
              WebService
            </router-link>
          </nav>
        </aside>

        <!-- Contenido principal del panel operacional -->
        <div class="adm-main">

          <!-- Encabezado con título y controles de refresco -->
          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Panel WebService</h1>
              <p class="adm-topbar__sub">Estado de proveedores y actividad en tiempo real</p>
            </div>
            <div class="ws-topbar-actions">
              <span class="ws-refresh-hint">Auto-refresco cada 30 s</span>
              <button class="adm-btn adm-btn--ghost adm-btn--sm" @click="recargar" :disabled="cargando" type="button">
                <svg :class="{ 'ws-spin': cargando }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                {{ cargando ? 'Cargando...' : 'Actualizar' }}
              </button>
            </div>
          </div>

          <!-- Toast de error -->
          <div v-if="errorMsg" class="ws-toast ws-toast--error">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ errorMsg }}
          </div>

          <!-- ══════════════════════════════════════════════
               SECCIÓN 1: Tarjetas de resumen del sistema
               ══════════════════════════════════════════════ -->
          <div class="ws-cards-grid">
            <div class="ws-summary-card">
              <div class="ws-summary-card__icon ws-summary-card__icon--blue">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
              </div>
              <div>
                <p class="ws-summary-card__label">Proveedores activos</p>
                <p class="ws-summary-card__value">{{ proveedoresActivos }}</p>
              </div>
            </div>
            <div class="ws-summary-card">
              <div class="ws-summary-card__icon ws-summary-card__icon--green">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><polyline points="20 6 9 17 4 12"/></svg>
              </div>
              <div>
                <p class="ws-summary-card__label">Handshakes exitosos</p>
                <p class="ws-summary-card__value">{{ eventos.handshake_exitosos ?? 0 }}</p>
              </div>
            </div>
            <div class="ws-summary-card">
              <div class="ws-summary-card__icon ws-summary-card__icon--red">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
              </div>
              <div>
                <p class="ws-summary-card__label">Handshakes fallidos</p>
                <p class="ws-summary-card__value">{{ eventos.handshake_fallidos ?? 0 }}</p>
              </div>
            </div>
            <div class="ws-summary-card">
              <div class="ws-summary-card__icon ws-summary-card__icon--yellow">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 .49-3.3"/></svg>
              </div>
              <div>
                <p class="ws-summary-card__label">Actualizaciones catálogo</p>
                <p class="ws-summary-card__value">{{ (eventos.catalogo_exitosos ?? 0) + (eventos.catalogo_fallidos ?? 0) }}</p>
              </div>
            </div>
          </div>

          <!-- ══════════════════════════════════════════════
               SECCIÓN 2: Tabla de proveedores registrados
               ══════════════════════════════════════════════ -->
          <div class="adm-card adm-card--full">
            <div class="adm-card__head">
              <h3 class="adm-card__title">Proveedores registrados</h3>
              <span class="ws-tag ws-tag--optional">{{ proveedores.length }} total</span>
            </div>

            <div v-if="cargandoEstado" class="ws-loading-row">
              <svg class="ws-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Cargando proveedores...
            </div>
            <div v-else-if="proveedores.length === 0" class="ws-empty">
              No hay proveedores registrados.
            </div>
            <table v-else class="ws-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Tipo</th>
                  <th>URL API</th>
                  <th>Estado</th>
                  <th>Handshake</th>
                  <th>% Ganancia</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in proveedores" :key="p.id">
                  <td class="ws-td-id">#{{ p.id }}</td>
                  <td class="ws-td-name">{{ p.nombre }}</td>
                  <td>
                    <span class="ws-tipo-badge" :class="p.tipo_proveedor_id === 1 ? 'ws-tipo-badge--aero' : 'ws-tipo-badge--hotel'">
                      {{ p.tipo_nombre }}
                    </span>
                  </td>
                  <td class="ws-td-url">
                    <span v-if="p.url" class="ws-url-chip">{{ p.url }}</span>
                    <span v-else class="ws-no-url">—</span>
                  </td>
                  <td>
                    <span class="adm-badge" :class="p.activo ? 'adm-badge--confirmada' : 'adm-badge--cancelada'">
                      {{ p.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </td>
                  <td>
                    <span v-if="p.handshake_configurado" class="ws-hs-badge ws-hs-badge--ok">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="12" height="12"><polyline points="20 6 9 17 4 12"/></svg>
                      Configurado
                    </span>
                    <span v-else class="ws-hs-badge ws-hs-badge--pending">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="12" height="12"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                      Pendiente
                    </span>
                  </td>
                  <td class="ws-td-pct">{{ p.porcentaje_ganancia }}%</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- ══════════════════════════════════════════════
               SECCIÓN 3: Feed de notificaciones recientes
               ══════════════════════════════════════════════ -->
          <div class="adm-card adm-card--full">
            <div class="adm-card__head">
              <h3 class="adm-card__title">Notificaciones de proveedores</h3>
              <span class="ws-tag ws-tag--optional">Últimas 50</span>
            </div>

            <div v-if="cargandoNotif" class="ws-loading-row">
              <svg class="ws-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
              Cargando notificaciones...
            </div>
            <div v-else-if="notificaciones.length === 0" class="ws-empty">
              No hay notificaciones de proveedores registradas.
            </div>
            <div v-else class="ws-notif-feed">
              <div v-for="n in notificaciones" :key="n.id" class="ws-notif-row" :class="{ 'ws-notif-row--unread': !n.leido }">
                <div class="ws-notif-dot" :class="tipoColor(n.tipo_notificacion_id)"></div>
                <div class="ws-notif-body">
                  <div class="ws-notif-top">
                    <span class="ws-notif-tipo">{{ n.tipo_notificacion }}</span>
                    <code class="ws-notif-res">{{ n.no_reservacion }}</code>
                    <span class="ws-notif-fecha">{{ n.fecha_emision }}</span>
                    <span v-if="!n.leido" class="ws-notif-new">Nuevo</span>
                  </div>
                  <p v-if="n.mensaje_proveedor" class="ws-notif-msg">{{ n.mensaje_proveedor }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- ══════════════════════════════════════════════
               SECCIÓN 4: Documentación técnica (colapsable)
               ══════════════════════════════════════════════ -->
          <div class="adm-card adm-card--full">
            <div class="adm-card__head ws-doc-head" @click="docAbierto = !docAbierto" style="cursor:pointer">
              <h3 class="adm-card__title">Documentación técnica del WebService</h3>
              <div style="display:flex;align-items:center;gap:8px">
                <span class="ws-tag ws-tag--optional">Referencia REST</span>
                <svg :style="{ transform: docAbierto ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s' }"
                  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </div>
            </div>

            <div v-if="docAbierto" class="ws-doc-content">

              <!-- Endpoint card: Cancelar -->
              <div class="ws-endpoint-card">
                <div class="ws-endpoint-card__head">
                  <span class="ws-method ws-method--post">POST</span>
                  <code class="ws-endpoint-url">/api/proveedores-ext/detalles/:idReservaProveedor/cancelar</code>
                  <button class="adm-btn adm-btn--sm adm-btn--ghost" @click="copiarURL" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                    {{ copiado ? 'Copiado' : 'Copiar URL' }}
                  </button>
                </div>
                <p class="ws-endpoint-card__desc">
                  Los proveedores (Broom AirLine, Miku Inn) llaman estos endpoints para notificar cancelaciones o actualizaciones de estado en reservaciones. Se autentican con el header <code>X-Agencia-Token</code> generado durante el handshake. También existe el endpoint <code>POST /api/proveedores-ext/detalles/:idReservaProveedor/actualizar</code> para notificar actualizaciones de estado.
                </p>
              </div>

              <!-- Fila: autenticación y body -->
              <div class="adm-row2">

                <div class="adm-card">
                  <div class="adm-card__head">
                    <h3 class="adm-card__title">Autenticación</h3>
                    <span class="ws-tag ws-tag--required">Requerida</span>
                  </div>
                  <div class="ws-auth-info">
                    <div class="ws-auth-row">
                      <span class="ws-auth-key">Tipo</span>
                      <span class="ws-auth-val">Header personalizado</span>
                    </div>
                    <div class="ws-auth-row">
                      <span class="ws-auth-key">Header</span>
                      <code class="ws-code-inline">X-Agencia-Token</code>
                    </div>
                    <div class="ws-auth-row">
                      <span class="ws-auth-key">Valor</span>
                      <span class="ws-auth-val">Token generado en el Handshake</span>
                    </div>
                    <div class="ws-auth-row">
                      <span class="ws-auth-key">Validación</span>
                      <span class="ws-auth-val">Se verifica contra <code>Token_HASH_Salida</code> en DB</span>
                    </div>
                  </div>
                  <div class="ws-code-block">
                    <div class="ws-code-block__head">
                      <span>Ejemplo de header</span>
                      <button @click="copiarHeader" class="ws-copy-btn" type="button">{{ copiadoHeader ? '✓' : 'Copiar' }}</button>
                    </div>
                    <pre class="ws-code-block__body">X-Agencia-Token: a3f9c2d1e8b4...</pre>
                  </div>
                </div>

                <div class="adm-card">
                  <div class="adm-card__head">
                    <h3 class="adm-card__title">Body (JSON)</h3>
                    <span class="ws-tag ws-tag--json">application/json</span>
                  </div>
                  <div class="ws-fields">
                    <div class="ws-field" v-for="f in bodyFields" :key="f.name">
                      <div class="ws-field__top">
                        <code class="ws-field__name">{{ f.name }}</code>
                        <span class="ws-tag" :class="f.req ? 'ws-tag--required' : 'ws-tag--optional'">{{ f.req ? 'Requerido' : 'Opcional' }}</span>
                        <span class="ws-field__type">{{ f.type }}</span>
                      </div>
                      <p class="ws-field__desc">{{ f.desc }}</p>
                      <p v-if="f.values" class="ws-field__values">Valores: {{ f.values }}</p>
                    </div>
                  </div>
                </div>

              </div>

              <!-- Fila: ejemplo de request y respuestas -->
              <div class="adm-row2">

                <div class="adm-card">
                  <div class="adm-card__head">
                    <h3 class="adm-card__title">Ejemplo de Request</h3>
                    <button @click="copiarRequest" class="adm-btn adm-btn--sm adm-btn--ghost" type="button">{{ copiadoReq ? 'Copiado' : 'Copiar' }}</button>
                  </div>
                  <pre class="ws-json-block">{{ ejemploRequest }}</pre>
                </div>

                <div class="adm-card">
                  <div class="adm-card__head">
                    <h3 class="adm-card__title">Respuestas</h3>
                  </div>
                  <div class="ws-responses">
                    <div v-for="r in responses" :key="r.code" class="ws-response">
                      <span class="ws-response__code" :class="`ws-response__code--${r.color}`">{{ r.code }}</span>
                      <div>
                        <p class="ws-response__title">{{ r.title }}</p>
                        <p class="ws-response__desc">{{ r.desc }}</p>
                      </div>
                    </div>
                  </div>
                </div>

              </div>

              <!-- Estados aceptados -->
              <div class="adm-card adm-card--full">
                <div class="adm-card__head">
                  <h3 class="adm-card__title">Estados aceptados en nuevoEstado</h3>
                </div>
                <div class="ws-estados-grid">
                  <div v-for="e in estadosAceptados" :key="e.valor" class="ws-estado-card">
                    <span class="ws-estado-card__dot" :style="{ background: e.color }"></span>
                    <div>
                      <code class="ws-estado-card__valor">{{ e.valor }}</code>
                      <p class="ws-estado-card__desc">{{ e.desc }}</p>
                    </div>
                    <span class="adm-badge" :class="e.badge">{{ e.estadoId }}</span>
                  </div>
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file WebService.vue
 * @description Panel operacional del WebService de Movent. Muestra en tiempo real
 * el estado de los proveedores (con flag de handshake configurado), el feed de
 * notificaciones generadas por proveedores en todas las reservaciones del sistema,
 * y las tarjetas de resumen de eventos. Incluye sección colapsable con la
 * documentación técnica del endpoint REST para callbacks de proveedores.
 *
 * Accesible por administradores (rol 2) y usuarios WebService (rol 3).
 * Se auto-refresca cada 30 segundos.
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/admin.css'
import '../styles/WebService.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// ── Estado reactivo del panel operacional ──────────────────────────────────

/** Lista de proveedores con campo handshake_configurado. @type {import('vue').Ref<Array>} */
const proveedores = ref([])

/** Conteo de eventos de handshake y catálogo. @type {import('vue').Ref<Object>} */
const eventos = ref({})

/** Lista de notificaciones de proveedores (últimas 50). @type {import('vue').Ref<Array>} */
const notificaciones = ref([])

/** Indica si la petición de estado está en curso. @type {import('vue').Ref<boolean>} */
const cargandoEstado = ref(false)

/** Indica si la petición de notificaciones está en curso. @type {import('vue').Ref<boolean>} */
const cargandoNotif = ref(false)

/** True mientras cualquiera de las dos peticiones está activa. @type {import('vue').ComputedRef<boolean>} */
const cargando = computed(() => cargandoEstado.value || cargandoNotif.value)

/** Mensaje de error para el toast. @type {import('vue').Ref<string>} */
const errorMsg = ref('')

/** Controla si la sección de documentación está abierta. @type {import('vue').Ref<boolean>} */
const docAbierto = ref(false)

/** ID del intervalo de auto-refresco. @type {number|null} */
let intervalo = null

// ── Datos del panel de documentación (sin cambios respecto a correcciones previas) ──

/** Controles de copia para botones de la documentación. */
const copiado      = ref(false)
const copiadoHeader = ref(false)
const copiadoReq   = ref(false)

/** Campos del body del endpoint de cancelación/actualización. */
const bodyFields = [
  { name: 'reservacionProveedorId', type: 'string', req: true,  desc: 'ID de la reservación en el sistema del proveedor.', values: null },
  { name: 'nuevoEstado',            type: 'string', req: true,  desc: 'Nuevo estado que debe reflejarse en MOVENT.', values: '"cancelada" | "confirmada" | "completada" | "en curso"' },
  { name: 'motivo',                 type: 'string', req: false, desc: 'Motivo del cambio. Requerido cuando nuevoEstado = "cancelada".', values: null },
]

/** JSON de ejemplo para la documentación. */
const ejemploRequest = `{
  "reservacionProveedorId": "42",
  "nuevoEstado": "cancelada",
  "motivo": "El vuelo fue cancelado por mantenimiento"
}`

/** Tabla de respuestas HTTP posibles del endpoint. */
const responses = [
  { code: '200', color: 'ok',   title: 'OK',           desc: 'Notificación procesada. Estado actualizado en DB.' },
  { code: '400', color: 'warn', title: 'Bad Request',  desc: 'Datos inválidos, estado desconocido o falta el motivo.' },
  { code: '401', color: 'err',  title: 'Unauthorized', desc: 'Token ausente o no reconocido en la base de datos.' },
  { code: '404', color: 'warn', title: 'Not Found',    desc: 'No existe reservación con ese ID para este proveedor.' },
  { code: '500', color: 'err',  title: 'Server Error', desc: 'Error interno. Reintentar en unos momentos.' },
]

/** Estados aceptados en el campo nuevoEstado del endpoint. */
const estadosAceptados = [
  { valor: 'confirmada', desc: 'La reservación fue confirmada por el proveedor.',       color: '#22c55e', badge: 'adm-badge--confirmada', estadoId: 'EstadoID 2' },
  { valor: 'cancelada',  desc: 'Cancelación. Requiere campo "motivo" en el body.',      color: '#D40511', badge: 'adm-badge--cancelada',  estadoId: 'EstadoID 3' },
  { valor: 'completada', desc: 'El servicio fue completado (vuelo aterrizó/checkout).', color: '#3b82f6', badge: 'adm-badge--completada', estadoId: 'EstadoID 5' },
  { valor: 'en curso',   desc: 'El servicio está en ejecución (vuelo despegó).',        color: '#8b5cf6', badge: 'adm-badge--encurso',    estadoId: 'EstadoID 6' },
]

// ── Computed ────────────────────────────────────────────────────────────────

/** Cantidad de proveedores con activo === true. @type {import('vue').ComputedRef<number>} */
const proveedoresActivos = computed(() => proveedores.value.filter(p => p.activo).length)

// ── Lógica de carga ─────────────────────────────────────────────────────────

/**
 * Obtiene el estado de proveedores y los conteos de eventos del backend.
 * Actualiza proveedores y eventos de forma reactiva.
 * @async
 */
async function cargarEstado() {
  cargandoEstado.value = true
  try {
    const res = await fetch(`${API}/api/admin/webservice/estado`, {
      credentials: 'include',
    })
    if (!res.ok) {
      errorMsg.value = `Error al obtener estado (${res.status})`
      return
    }
    const data = await res.json()
    proveedores.value = data.proveedores ?? []
    eventos.value     = data.eventos ?? {}
    errorMsg.value    = ''
  } catch {
    errorMsg.value = 'No se pudo conectar con el backend.'
  } finally {
    cargandoEstado.value = false
  }
}

/**
 * Obtiene las últimas 50 notificaciones de proveedores del backend.
 * @async
 */
async function cargarNotificaciones() {
  cargandoNotif.value = true
  try {
    const res = await fetch(`${API}/api/admin/webservice/notificaciones`, {
      credentials: 'include',
    })
    if (!res.ok) {
      errorMsg.value = `Error al obtener notificaciones (${res.status})`
      return
    }
    notificaciones.value = await res.json()
    errorMsg.value = ''
  } catch {
    errorMsg.value = 'No se pudo conectar con el backend.'
  } finally {
    cargandoNotif.value = false
  }
}

/**
 * Ejecuta ambas cargas en paralelo. Se llama en el montaje y en el
 * botón manual de actualizar.
 */
async function recargar() {
  await Promise.all([cargarEstado(), cargarNotificaciones()])
}

onMounted(() => {
  recargar()
  intervalo = setInterval(recargar, 30_000)
})

onUnmounted(() => {
  if (intervalo !== null) clearInterval(intervalo)
})

// ── Helpers de UI ───────────────────────────────────────────────────────────

/**
 * Devuelve la clase CSS del punto de color para el tipo de notificación.
 * @param {number} tipoID
 * @returns {string}
 */
function tipoColor(tipoID) {
  const mapa = {
    1: 'ws-notif-dot--cancel',
    2: 'ws-notif-dot--update',
    3: 'ws-notif-dot--info',
  }
  return mapa[tipoID] ?? 'ws-notif-dot--info'
}

/**
 * Activa el feedback visual de un botón de copia por 2 segundos.
 * @param {import('vue').Ref<boolean>} r
 */
function copiar(r) { r.value = true; setTimeout(() => r.value = false, 2000) }

function copiarURL()     { navigator.clipboard.writeText(`${API}/api/proveedores-ext/detalles/:idReservaProveedor/cancelar`).catch(() => {}); copiar(copiado) }
function copiarHeader()  { navigator.clipboard.writeText('X-Agencia-Token: <tu-token>').catch(() => {}); copiar(copiadoHeader) }
function copiarRequest() { navigator.clipboard.writeText(ejemploRequest).catch(() => {}); copiar(copiadoReq) }
</script>
