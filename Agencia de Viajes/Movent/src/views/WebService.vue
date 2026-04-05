<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- Sidebar exclusivo del panel WebService, sin links de administración general -->
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

        <!-- Contenido principal de la documentación del WebService -->
        <div class="adm-main">

          <!-- Encabezado con título y badge de estado activo -->
          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">WebService</h1>
              <p class="adm-topbar__sub">Endpoint REST para notificaciones de proveedores</p>
            </div>
            <div class="ws-status-badge">
              <span class="ws-status-dot"></span>
              Escuchando notificaciones
            </div>
          </div>

          <!-- Tarjeta del endpoint principal con método, URL y botón de copiar -->
          <div class="ws-endpoint-card">
            <div class="ws-endpoint-card__head">
              <span class="ws-method ws-method--post">POST</span>
              <code class="ws-endpoint-url">/api/webservice/notificacion</code>
              <button class="adm-btn adm-btn--sm adm-btn--ghost" @click="copiarURL" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
                {{ copiado ? 'Copiado' : 'Copiar URL' }}
              </button>
            </div>
            <p class="ws-endpoint-card__desc">
              Los proveedores (Broom AirLine, Miku Inn) llaman este endpoint para notificar cambios de estado en reservaciones. Se autentica con <code>X-Proveedor-Token</code> generado en el handshake.
            </p>
          </div>

          <!-- Fila: autenticación y descripción de campos del body -->
          <div class="adm-row2">

            <!-- Detalles del header de autenticación requerido -->
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
                  <code class="ws-code-inline">X-Proveedor-Token</code>
                </div>
                <div class="ws-auth-row">
                  <span class="ws-auth-key">Valor</span>
                  <span class="ws-auth-val">Token generado en el Handshake</span>
                </div>
                <div class="ws-auth-row">
                  <span class="ws-auth-key">Validación</span>
                  <span class="ws-auth-val">Se verifica contra <code>Token_HASH_Entrada</code> en DB</span>
                </div>
              </div>
              <div class="ws-code-block">
                <div class="ws-code-block__head">
                  <span>Ejemplo de header</span>
                  <button @click="copiarHeader" class="ws-copy-btn" type="button">{{ copiadoHeader ? '✓' : 'Copiar' }}</button>
                </div>
                <pre class="ws-code-block__body">X-Proveedor-Token: a3f9c2d1e8b4...</pre>
              </div>
            </div>

            <!-- Lista de campos del body con sus tipos y descripciones -->
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

          <!-- Fila: ejemplo de request JSON y tabla de respuestas HTTP posibles -->
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

          <!-- Grid con todos los estados aceptados por el endpoint -->
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

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file WebService.vue
 * @description Panel de documentación del endpoint REST que los proveedores
 * (Broom AirLine, Miku Inn) usan para notificar cambios de estado en reservaciones.
 * Incluye especificación de autenticación, campos del body, ejemplos y tabla de respuestas.
 */
import { ref } from 'vue'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/admin.css'
import '../styles/WebService.css'

/** URL base del backend. @type {string} */
const API = 'http://localhost:8080'

/** Controla el feedback visual del botón "Copiar URL". @type {import('vue').Ref<boolean>} */
const copiado = ref(false)

/** Controla el feedback visual del botón de copiar el header de autenticación. @type {import('vue').Ref<boolean>} */
const copiadoHeader = ref(false)

/** Controla el feedback visual del botón de copiar el request de ejemplo. @type {import('vue').Ref<boolean>} */
const copiadoReq = ref(false)

/**
 * Definición de los campos aceptados en el body JSON del endpoint.
 * Cada objeto describe nombre, tipo, obligatoriedad y valores posibles.
 * @type {Array<{name: string, type: string, req: boolean, desc: string, values: string|null}>}
 */
const bodyFields = [
  { name: 'reservacionProveedorId', type: 'string', req: true,  desc: 'ID de la reservación en el sistema del proveedor.', values: null },
  { name: 'nuevoEstado',            type: 'string', req: true,  desc: 'Nuevo estado que debe reflejarse en MOVENT.', values: '"cancelada" | "confirmada" | "completada" | "en curso"' },
  { name: 'motivo',                 type: 'string', req: false, desc: 'Motivo del cambio. Requerido cuando nuevoEstado = "cancelada".', values: null },
]

/**
 * JSON de ejemplo que se muestra en la sección de request.
 * @type {string}
 */
const ejemploRequest = `{
  "reservacionProveedorId": "42",
  "nuevoEstado": "cancelada",
  "motivo": "El vuelo fue cancelado por mantenimiento"
}`

/**
 * Tabla de respuestas HTTP posibles del endpoint con código, título y descripción.
 * @type {Array<{code: string, color: string, title: string, desc: string}>}
 */
const responses = [
  { code: '200', color: 'ok',   title: 'OK',           desc: 'Notificación procesada. Estado actualizado en DB.' },
  { code: '400', color: 'warn', title: 'Bad Request',  desc: 'Datos inválidos, estado desconocido o falta el motivo.' },
  { code: '401', color: 'err',  title: 'Unauthorized', desc: 'Token ausente o no reconocido en la base de datos.' },
  { code: '404', color: 'warn', title: 'Not Found',    desc: 'No existe reservación con ese ID para este proveedor.' },
  { code: '500', color: 'err',  title: 'Server Error', desc: 'Error interno. Reintentar en unos momentos.' },
]

/**
 * Lista de estados que el endpoint acepta en el campo nuevoEstado,
 * con su descripción, color indicador y badge de referencia al EstadoID en DB.
 * @type {Array<{valor: string, desc: string, color: string, badge: string, estadoId: string}>}
 */
const estadosAceptados = [
  { valor: 'confirmada', desc: 'La reservación fue confirmada por el proveedor.',       color: '#22c55e', badge: 'adm-badge--confirmada', estadoId: 'EstadoID 2' },
  { valor: 'cancelada',  desc: 'Cancelación. Requiere campo "motivo" en el body.',      color: '#D40511', badge: 'adm-badge--cancelada',  estadoId: 'EstadoID 3' },
  { valor: 'completada', desc: 'El servicio fue completado (vuelo aterrizó/checkout).', color: '#3b82f6', badge: 'adm-badge--completada', estadoId: 'EstadoID 5' },
  { valor: 'en curso',   desc: 'El servicio está en ejecución (vuelo despegó).',        color: '#8b5cf6', badge: 'adm-badge--encurso',    estadoId: 'EstadoID 6' },
]

/**
 * Activa el feedback visual de un botón de copia durante un tiempo determinado.
 * @param {import('vue').Ref<boolean>} r - ref del estado del botón
 * @param {number} [ms=2000] - milisegundos que dura el estado activo
 */
function copiar(r, ms = 2000) { r.value = true; setTimeout(() => r.value = false, ms) }

/** Copia la URL completa del endpoint al portapapeles. */
function copiarURL()     { navigator.clipboard.writeText(`${API}/api/webservice/notificacion`).catch(() => {}); copiar(copiado) }

/** Copia el ejemplo de header de autenticación al portapapeles. */
function copiarHeader()  { navigator.clipboard.writeText('X-Proveedor-Token: <tu-token>').catch(() => {}); copiar(copiadoHeader) }

/** Copia el JSON de ejemplo del request al portapapeles. */
function copiarRequest() { navigator.clipboard.writeText(ejemploRequest).catch(() => {}); copiar(copiadoReq) }
</script>
