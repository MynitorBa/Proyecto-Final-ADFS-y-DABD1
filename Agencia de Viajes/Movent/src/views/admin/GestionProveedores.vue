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
            <router-link to="/admin/reservaciones" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Reservaciones
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

        <!-- Área principal con la grilla de tarjetas de proveedores -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Proveedores</h1>
              <p class="adm-topbar__sub">Configura aerolíneas y hoteles conectados</p>
            </div>
            <div class="adm-topbar__actions">
              <!-- Buscador para filtrar proveedores por nombre o URL -->
              <div class="adm-search">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="busqueda" type="text" placeholder="Buscar proveedor..." class="adm-search__input" />
              </div>
              <!-- Botón para actualizar el catálogo manualmente desde el panel de administración -->
              <button class="adm-btn adm-btn--outline" @click="actualizarCatalogo" :disabled="actualizando" type="button">
                <div v-if="actualizando" class="adm-spinner adm-spinner--sm"></div>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
                Actualizar catálogo
              </button>
              <button class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Agregar proveedor
              </button>
            </div>
          </div>

          <!-- Filtros de tipo: todos, aerolíneas o hoteles -->
          <div class="adm-filtros-rol">
            <button v-for="t in tiposOpts" :key="t.val"
              :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtroTipo === t.val }]"
              @click="filtroTipo = t.val" type="button">
              {{ t.label }}
              <span class="adm-filtro-rol__n">{{ contarPorTipo(t.val) }}</span>
            </button>
          </div>

          <!-- Indicador de carga mientras llega la lista de proveedores -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando proveedores...</p>
          </div>

          <!-- Mensaje de error con opción de reintentar la carga -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargarProveedores" type="button">Reintentar</button>
          </div>

          <!-- Estado vacío: sin proveedores o sin coincidencias en la búsqueda -->
          <div v-else-if="proveedoresFiltrados.length === 0" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
            <p>{{ busqueda ? 'Sin resultados para "' + busqueda + '"' : 'No hay proveedores configurados' }}</p>
            <button v-if="!busqueda" class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">Agregar el primero</button>
          </div>

          <!-- Grilla de tarjetas, una por cada proveedor filtrado -->
          <div v-else class="adm-proveedores-grid">
            <div v-for="p in proveedoresFiltrados" :key="p.id" class="adm-prov-card"
              :class="{ 'adm-prov-card--inactivo': !p.activo }">

              <div class="adm-prov-card__head">
                <div class="adm-prov-card__tipo-icon" :class="`adm-prov-card__tipo-icon--${tipoClase(p)}`">
                  <img v-if="p.imagenBase64" :src="'data:image/png;base64,' + p.imagenBase64"
                    style="width:100%;height:100%;object-fit:cover;border-radius:8px;"
                    @error="e => e.target.style.display='none'" />
                  <template v-else>
                    <svg v-if="p.tipoProveedorId===1" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  </template>
                </div>
                <div class="adm-prov-card__info">
                  <h4 class="adm-prov-card__nombre">{{ p.nombre }}</h4>
                  <p class="adm-prov-card__tipo-lbl">{{ p.tipoNombre }}</p>
                </div>
                <span class="adm-badge" :class="p.activo ? 'adm-badge--on' : 'adm-badge--off'">
                  {{ p.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </div>

              <div class="adm-prov-card__body">
                <div class="adm-prov-card__row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                  <span class="adm-prov-card__url">{{ p.url }}</span>
                </div>
                <div class="adm-prov-card__row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                  <span>Ganancia: {{ p.porcentajeGanancia }}%</span>
                </div>
              </div>

              <!-- Acciones disponibles para cada proveedor -->
              <div class="adm-prov-card__foot">
                <button class="adm-btn adm-btn--sm adm-btn--outline" @click="abrirFormEditar(p)" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Editar
                </button>
                <button class="adm-btn adm-btn--sm" :class="p.activo ? 'adm-btn--danger-ghost' : 'adm-btn--ghost'"
                  @click="toggleActivo(p)" :disabled="toggling===p.id" type="button">
                  <div v-if="toggling===p.id" class="adm-spinner adm-spinner--sm"></div>
                  <span v-else>{{ p.activo ? 'Desactivar' : 'Activar' }}</span>
                </button>
                <button class="adm-btn adm-btn--sm adm-btn--yellow"
                  @click="iniciarHandshake(p)" :disabled="handshaking===p.id" type="button">
                  <div v-if="handshaking===p.id" class="adm-spinner adm-spinner--sm"></div>
                  <span v-else>Handshake</span>
                </button>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Modal de formulario para crear o editar un proveedor -->
    <div v-if="formAbierto" class="adm-modal-overlay" @click.self="cerrarForm">
      <div class="adm-modal prov-modal">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">{{ editando ? 'Editar proveedor' : 'Nuevo proveedor' }}</h3>
          <button class="adm-modal__close" @click="cerrarForm" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <!-- Layout carnet: datos a la izquierda, imagen a la derecha -->
        <div class="adm-modal__body">
          <div class="prov-carnet">

            <!-- IZQUIERDA: campos del formulario -->
            <div class="prov-carnet__fields">
              <div class="adm-field">
                <label class="adm-field__label">Nombre del proveedor *</label>
                <input class="adm-field__input" v-model="form.nombre" placeholder="Ej. Broom AirLine" type="text" />
              </div>

              <template v-if="!editando">
                <div class="adm-field">
                  <label class="adm-field__label">Tipo *</label>
                  <select class="adm-field__input" v-model="form.tipoProveedorId">
                    <option value="">Seleccionar...</option>
                    <option :value="1">Aerolínea</option>
                    <option :value="2">Hotel</option>
                  </select>
                </div>
                <div class="adm-field">
                  <label class="adm-field__label">Usuario WebService *</label>
                  <select class="adm-field__input" v-model.number="form.usuarioId">
                    <option value="">Seleccionar usuario...</option>
                    <option v-for="u in usuariosWS" :key="u.id" :value="u.id">
                      {{ u.nombre }} {{ u.apellido }} — {{ u.correo }}
                    </option>
                  </select>
                </div>
              </template>

              <div class="adm-field">
                <label class="adm-field__label">URL del servicio REST *</label>
                <input class="adm-field__input" v-model="form.url" placeholder="http://localhost:7000" type="url" />
              </div>
            </div>

            <!-- DERECHA: foto carnet -->
            <div class="prov-carnet__photo">
              <p class="prov-carnet__photo-label">Logo / Imagen</p>
              <div class="prov-carnet__frame">
                <img v-if="form.imagenBase64"
                  :src="'data:image/png;base64,' + form.imagenBase64"
                  alt="Logo proveedor"
                  class="prov-carnet__img" />
                <div v-else class="prov-carnet__placeholder">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#c8bfb4" stroke-width="1.5" width="36" height="36"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <span>Sin imagen</span>
                </div>
              </div>
              <div class="prov-carnet__actions">
                <label class="adm-btn adm-btn--sm adm-btn--outline prov-carnet__upload-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  {{ form.imagenBase64 ? 'Cambiar' : 'Subir imagen' }}
                  <input type="file" accept="image/*" @change="seleccionarImagen" style="display:none" />
                </label>
                <button v-if="form.imagenBase64" type="button"
                  class="adm-btn adm-btn--sm adm-btn--danger-ghost"
                  @click="form.imagenBase64 = ''">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
                  Quitar
                </button>
              </div>
            </div>

          </div>

          <p v-if="formError" class="adm-form-error" style="margin-top:10px">{{ formError }}</p>
        </div>

        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="cerrarForm" type="button">Cancelar</button>
          <button class="adm-btn adm-btn--yellow" @click="guardarProveedor" :disabled="guardando" type="button">
            <div v-if="guardando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>{{ editando ? 'Guardar cambios' : 'Agregar proveedor' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Notificación temporal de éxito o error -->
    <div v-if="toast" class="adm-toast" :class="`adm-toast--${toast.tipo}`">
      <svg v-if="toast.tipo==='ok'" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><polyline points="20 6 9 17 4 12"/></svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      {{ toast.msg }}
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file GestionProveedores.vue
 * @description Vista del panel de administración para gestionar los proveedores externos
 * (aerolíneas y hoteles). Permite agregar, editar, activar/desactivar, probar la conexión,
 * ejecutar handshake con cada proveedor conectado y actualizar el catálogo manualmente.
 */
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Lista completa de proveedores cargados desde el servidor. @type {import('vue').Ref<Array>} */
const proveedores    = ref([])

/** Indica si la petición inicial de proveedores está en curso. @type {import('vue').Ref<boolean>} */
const loading        = ref(true)

/** Mensaje de error si la carga de proveedores falla. @type {import('vue').Ref<string>} */
const error          = ref('')

/** Texto del buscador para filtrar por nombre o URL. @type {import('vue').Ref<string>} */
const busqueda       = ref('')

/** Filtro activo de tipo de proveedor ('todos', 'aerolinea', 'hotel'). @type {import('vue').Ref<string>} */
const filtroTipo     = ref('todos')

/** Controla la visibilidad del formulario modal. @type {import('vue').Ref<boolean>} */
const formAbierto    = ref(false)

/** ID del proveedor en edición, null si se está creando uno nuevo. @type {import('vue').Ref<number|null>} */
const editando       = ref(null)

/** Indica si hay una petición de guardado en proceso. @type {import('vue').Ref<boolean>} */
const guardando      = ref(false)

/** ID del proveedor cuya conexión se está probando en este momento. @type {import('vue').Ref<number|null>} */
const probando       = ref(null)

/** ID del proveedor cuyo estado activo/inactivo se está cambiando. @type {import('vue').Ref<number|null>} */
const toggling       = ref(null)

/** ID del proveedor que está ejecutando el handshake. @type {import('vue').Ref<number|null>} */
const handshaking    = ref(null)

/** Indica si la actualización manual del catálogo está en proceso. @type {import('vue').Ref<boolean>} */
const actualizando   = ref(false)

/** Lista de usuarios conectados o disponibles en el WebSocket. @type {import('vue').Ref<any[]>} */
const usuariosWS = ref([])

/** Error de validación o servidor mostrado dentro del formulario modal. @type {import('vue').Ref<string>} */
const formError      = ref('')

/** Notificación temporal de éxito o error. @type {import('vue').Ref<{tipo: string, msg: string}|null>} */
const toast          = ref(null)

/**
 * Resultado de la última prueba de conexión para cada proveedor, indexado por ID.
 * @type {import('vue').Ref<Object.<number, 'ok'|'err'>>}
 */
const estadoConexion = ref({})

/**
 * Devuelve un objeto vacío con los campos del formulario de proveedor.
 * @returns {{nombre: string, tipoProveedorId: string, usuarioId: string, url: string, porcentajeGanancia: number}}
 */
const formVacio = () => ({
  nombre: '', tipoProveedorId: '', usuarioId: '', url: '', porcentajeGanancia: 0, imagenBase64: ''
})

/** Estado reactivo del formulario de creación/edición. @type {import('vue').Ref<Object>} */
const form = ref(formVacio())

/**
 * Opciones para los botones de filtro por tipo de proveedor.
 * @type {Array<{val: string, label: string}>}
 */
const tiposOpts = [
  { val: 'todos',     label: 'Todos' },
  { val: 'aerolinea', label: 'Aerolíneas' },
  { val: 'hotel',     label: 'Hoteles' },
]

/**
 * Lista de proveedores filtrada por tipo y por el texto de búsqueda.
 * @type {import('vue').ComputedRef<Array>}
 */
const proveedoresFiltrados = computed(() => {
  let list = proveedores.value
  if (filtroTipo.value !== 'todos')
    list = list.filter(p => tipoClase(p) === filtroTipo.value)
  if (busqueda.value.trim())
    list = list.filter(p =>
      p.nombre.toLowerCase().includes(busqueda.value.toLowerCase()) ||
      p.url.toLowerCase().includes(busqueda.value.toLowerCase())
    )
  return list
})

/**
 * Cuenta cuántos proveedores hay de un tipo determinado.
 * @param {string} t - El tipo a contar o 'todos' para el total.
 * @returns {number}
 */
const contarPorTipo = (t) =>
  t === 'todos' ? proveedores.value.length : proveedores.value.filter(p => tipoClase(p) === t).length

/**
 * Devuelve la clase CSS correspondiente al tipo de proveedor.
 * @param {Object} p - El proveedor.
 * @returns {'aerolinea'|'hotel'}
 */
function tipoClase(p) {
  return p.tipoProveedorId === 1 ? 'aerolinea' : 'hotel'
}

/** Carga la lista de proveedores al montar el componente. */
onMounted(() => {
  cargarProveedores()
  fetch(`${API}/api/admin/usuarios`, { credentials: 'include' })
    .then(r => r.json())
    .then(d => { usuariosWS.value = d.filter(u => u.rolId === 3) })
    .catch(() => {})
})

/**
 * Obtiene todos los proveedores desde el backend.
 * @returns {Promise<void>}
 */
async function cargarProveedores() {
  loading.value = true; error.value = ''
  try {
    const res = await fetch(`${API}/api/proveedores`, { credentials: 'include' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    proveedores.value = await res.json()
  } catch {
    error.value = 'No se pudieron cargar los proveedores.'
  } finally {
    loading.value = false
  }
}

/**
 * Abre el modal en modo creación con todos los campos vacíos.
 */
function abrirFormNuevo() {
  editando.value = null
  form.value = formVacio()
  formError.value = ''
  formAbierto.value = true
}

/**
 * Abre el modal en modo edición precargando los datos del proveedor seleccionado.
 * @param {Object} p - El proveedor a editar.
 */
function abrirFormEditar(p) {
  editando.value = p.id
  form.value = {
    nombre:             p.nombre,
    tipoProveedorId:    p.tipoProveedorId,
    usuarioId:          '',
    url:                p.url,
    porcentajeGanancia: p.porcentajeGanancia,
    imagenBase64:       p.imagenBase64 || '',
  }
  formError.value = ''
  formAbierto.value = true
}

/**
 * Cierra el modal de formulario y limpia el estado de edición.
 */
function cerrarForm() {
  formAbierto.value = false
  editando.value = null
}

/**
 * Valida los campos del formulario antes de enviarlo.
 * @returns {boolean} true si es válido, false si hay algún campo incompleto.
 */
function validarForm() {
  if (!form.value.nombre.trim()) { formError.value = 'El nombre es obligatorio.'; return false }
  if (!form.value.url.trim())    { formError.value = 'La URL es obligatoria.'; return false }
  if (!editando.value) {
    if (!form.value.tipoProveedorId) { formError.value = 'Selecciona el tipo.'; return false }
    if (!form.value.usuarioId)       { formError.value = 'El ID de usuario WebService es obligatorio.'; return false }
  }
  formError.value = ''; return true
}

/**
 * Guarda el proveedor mediante POST (creación) o PUT (edición).
 * Recarga la lista tras un guardado exitoso.
 * @returns {Promise<void>}
 */
async function guardarProveedor() {
  if (!validarForm()) return
  guardando.value = true
  try {
    if (editando.value) {
      const res = await fetch(`${API}/api/proveedores/${editando.value}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:             form.value.nombre,
          url:                form.value.url,
          porcentajeGanancia: form.value.porcentajeGanancia,
          imagenBase64:       form.value.imagenBase64,
        }),
      })
      if (!res.ok) throw new Error(`Error ${res.status}`)
      mostrarToast('ok', 'Proveedor actualizado.')
    } else {
      const res = await fetch(`${API}/api/proveedores`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nombre:              form.value.nombre,
          tipo_proveedor_id:   form.value.tipoProveedorId,
          url_api:             form.value.url,
          usuario_id:          form.value.usuarioId,
          porcentaje_ganancia: form.value.porcentajeGanancia,
          imagenBase64:        form.value.imagenBase64,
        }),
      })
      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        throw new Error(data.error || `Error ${res.status}`)
      }
      mostrarToast('ok', 'Proveedor agregado.')
    }
    cerrarForm()
    await cargarProveedores()
  } catch (e) {
    formError.value = e.message || 'Error al guardar.'
  } finally {
    guardando.value = false
  }
}

/**
 * Cambia el estado activo/inactivo de un proveedor mediante PATCH.
 * Actualiza el valor localmente sin recargar la lista completa.
 * @param {Object} p - El proveedor a activar o desactivar.
 * @returns {Promise<void>}
 */
async function toggleActivo(p) {
  toggling.value = p.id
  try {
    const res = await fetch(`${API}/api/proveedores/${p.id}/estado`, {
      method: 'PATCH',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ activo: !p.activo }),
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    p.activo = !p.activo
    mostrarToast('ok', `Proveedor ${p.activo ? 'activado' : 'desactivado'}.`)
  } catch {
    mostrarToast('err', 'Error al cambiar estado.')
  } finally {
    toggling.value = null
  }
}

/**
 * Realiza una petición directa a la URL del proveedor para verificar que responde.
 * Guarda el resultado ('ok' o 'err') en estadoConexion indexado por ID.
 * @param {Object} p - El proveedor a probar.
 * @returns {Promise<void>}
 */
async function probarConexion(p) {
  probando.value = p.id
  try {
    const res = await fetch(p.url, { signal: AbortSignal.timeout(5000) })
    estadoConexion.value[p.id] = res.ok || res.status < 500 ? 'ok' : 'err'
    mostrarToast(
      estadoConexion.value[p.id] === 'ok' ? 'ok' : 'err',
      estadoConexion.value[p.id] === 'ok' ? `Conexión exitosa con ${p.nombre}` : `Sin respuesta de ${p.nombre}`
    )
  } catch {
    estadoConexion.value[p.id] = 'err'
    mostrarToast('err', `Sin respuesta de ${p.nombre}`)
  } finally {
    probando.value = null
  }
}

/**
 * Inicia el proceso de handshake con el proveedor para sincronizar credenciales.
 * El endpoint cambia según si es aerolínea o hotelera.
 * @param {Object} p - El proveedor con el que se hará handshake.
 * @returns {Promise<void>}
 */
async function iniciarHandshake(p) {
  handshaking.value = p.id
  try {
    const endpoint = p.tipoProveedorId === 1
      ? `${API}/api/proveedores/${p.id}/handshake`
      : `${API}/api/proveedores/${p.id}/handshake-hotelera`
    const res = await fetch(endpoint, { method: 'POST', credentials: 'include' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    mostrarToast('ok', `Handshake completado con ${p.nombre}`)
    await cargarProveedores()
  } catch {
    mostrarToast('err', 'Error en el handshake.')
  } finally {
    handshaking.value = null
  }
}

/**
 * Dispara la actualización manual del catálogo llamando al endpoint dedicado.
 * Bloquea el botón mientras la petición está en curso y notifica el resultado.
 * @returns {Promise<void>}
 */
async function actualizarCatalogo() {
  actualizando.value = true
  try {
    const res = await fetch(`${API}/api/catalogo/actualizar`, {
      method: 'POST',
      credentials: 'include',
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    mostrarToast('ok', 'Catálogo actualizado correctamente.')
  } catch {
    mostrarToast('err', 'Error al actualizar el catálogo.')
  } finally {
    actualizando.value = false
  }
}

/**
 * Convierte el archivo de imagen seleccionado a Base64 y lo guarda en form.imagenBase64.
 * @param {Event} e - Evento change del input file.
 */
function seleccionarImagen(e) {
  const file = e.target.files[0]
  if (!file) return

  const MAX_W = 400   // px máximos del logo
  const QUALITY = 0.85

  const reader = new FileReader()
  reader.onload = (ev) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const scale = Math.min(1, MAX_W / Math.max(img.width, img.height))
      canvas.width  = Math.round(img.width  * scale)
      canvas.height = Math.round(img.height * scale)
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
      const dataUrl = canvas.toDataURL('image/png', QUALITY)
      form.value.imagenBase64 = dataUrl.split(',')[1]
    }
    img.src = ev.target.result
  }
  reader.readAsDataURL(file)
}

/**
 * Muestra una notificación toast y la oculta automáticamente tras 3.5 segundos.
 * @param {'ok'|'err'} tipo - Tipo de notificación.
 * @param {string} msg - Mensaje a mostrar.
 */
function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => toast.value = null, 3500)
}
</script>

<style scoped>
/* Modal más ancho para el layout carnet */
.prov-modal {
  max-width: 720px;
}

/* Layout carnet: campos a la izquierda, foto a la derecha */
.prov-carnet {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.prov-carnet__fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

/* Panel derecho — foto tipo carnet */
.prov-carnet__photo {
  flex-shrink: 0;
  width: 170px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.prov-carnet__photo-label {
  font-size: 11px;
  font-weight: 700;
  color: #9a9089;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0;
}

/* Marco de la foto — estilo carnet / credencial */
.prov-carnet__frame {
  width: 150px;
  height: 150px;
  border-radius: 12px;
  border: 2px dashed #d4cdc5;
  background: #f5f2ec;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.prov-carnet__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #fff;
}

.prov-carnet__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #b8b0a8;
  font-size: 11px;
}

.prov-carnet__actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.prov-carnet__upload-btn {
  cursor: pointer;
  justify-content: center;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* Responsive: apila cuando el modal no tiene espacio */
@media (max-width: 560px) {
  .prov-carnet {
    flex-direction: column-reverse;
  }
  .prov-carnet__photo {
    width: 100%;
    flex-direction: row;
    align-items: center;
    gap: 16px;
  }
  .prov-carnet__frame {
    width: 90px;
    height: 90px;
  }
}
</style>