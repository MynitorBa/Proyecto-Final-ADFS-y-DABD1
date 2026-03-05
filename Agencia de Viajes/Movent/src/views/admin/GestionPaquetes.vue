<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- SIDEBAR -->
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
            <router-link to="/admin/paquetes" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Paquetes
            </router-link>
            <router-link to="/admin/roles" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Roles
            </router-link>
          </nav>
        </aside>

        <!-- CONTENIDO -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Paquetes Turísticos</h1>
              <p class="adm-topbar__sub">Combina vuelos y hospedajes con precio especial</p>
            </div>
            <button class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              Nuevo paquete
            </button>
          </div>

          <!-- Filtros estado -->
          <div class="adm-filtros-rol">
            <button v-for="f in filtrosOpts" :key="f.val"
              :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtroEstado === f.val }]"
              @click="filtroEstado = f.val" type="button">
              {{ f.label }}
              <span class="adm-filtro-rol__n">{{ contarPorEstado(f.val) }}</span>
            </button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando paquetes...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargarPaquetes" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="paquetesFiltrados.length === 0" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            <p>No hay paquetes creados aún</p>
            <button class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">Crear el primero</button>
          </div>

          <!-- Grid paquetes -->
          <div v-else class="adm-paquetes-grid">
            <div v-for="paq in paquetesFiltrados" :key="paq.id" class="adm-paq-card"
              :class="{ 'adm-paq-card--inactivo': !paq.activo }">

              <!-- Franja superior -->
              <div class="adm-paq-card__franja">
                <span class="adm-paq-card__nombre">{{ paq.nombre }}</span>
                <span class="adm-badge" :class="paq.activo ? 'adm-badge--on' : 'adm-badge--off'">
                  {{ paq.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </div>

              <!-- Vuelo -->
              <div class="adm-paq-card__seccion">
                <div class="adm-paq-card__seccion-icon adm-paq-card__seccion-icon--vuelo">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                </div>
                <div class="adm-paq-card__seccion-info">
                  <span class="adm-paq-card__seccion-lbl">Vuelo</span>
                  <span class="adm-paq-card__seccion-val">{{ paq.vuelo?.origen }} → {{ paq.vuelo?.destino }}</span>
                  <span class="adm-paq-card__seccion-meta">{{ paq.vuelo?.aerolinea }} · {{ paq.vuelo?.numeroVuelo }}</span>
                </div>
              </div>

              <!-- Hotel -->
              <div class="adm-paq-card__seccion">
                <div class="adm-paq-card__seccion-icon adm-paq-card__seccion-icon--hotel">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="14" height="14"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                </div>
                <div class="adm-paq-card__seccion-info">
                  <span class="adm-paq-card__seccion-lbl">Hotel</span>
                  <span class="adm-paq-card__seccion-val">{{ paq.hotel?.nombre }}</span>
                  <span class="adm-paq-card__seccion-meta">{{ paq.hotel?.tipoHabitacion }} · {{ paq.hotel?.ciudad }}</span>
                </div>
              </div>

              <!-- Precios -->
              <div class="adm-paq-card__precios">
                <div class="adm-paq-card__precio-item">
                  <span class="adm-paq-card__precio-lbl">Precio individual</span>
                  <span class="adm-paq-card__precio-tachado">${{ paq.precioIndividual?.toFixed(2) }}</span>
                </div>
                <div class="adm-paq-card__precio-item">
                  <span class="adm-paq-card__precio-lbl">Precio paquete</span>
                  <span class="adm-paq-card__precio-especial">${{ paq.precioEspecial?.toFixed(2) }}</span>
                </div>
                <div v-if="paq.precioIndividual && paq.precioEspecial" class="adm-paq-card__ahorro">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><polyline points="20 6 9 17 4 12"/></svg>
                  Ahorro: ${{ (paq.precioIndividual - paq.precioEspecial).toFixed(2) }}
                </div>
              </div>

              <!-- Reservas activas -->
              <div v-if="paq.reservasActivas > 0" class="adm-paq-card__reservas">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                {{ paq.reservasActivas }} reserva{{ paq.reservasActivas!==1?'s':'' }} activa{{ paq.reservasActivas!==1?'s':'' }}
              </div>

              <p v-if="paq.descripcion" class="adm-paq-card__desc">{{ paq.descripcion }}</p>

              <!-- Acciones -->
              <div class="adm-paq-card__foot">
                <button class="adm-btn adm-btn--sm adm-btn--outline" @click="abrirFormEditar(paq)" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Editar
                </button>
                <button class="adm-btn adm-btn--sm adm-btn--ghost" @click="toggleActivo(paq)" type="button">
                  {{ paq.activo ? 'Desactivar' : 'Activar' }}
                </button>
                <button class="adm-btn adm-btn--sm adm-btn--danger-ghost"
                  @click="confirmarEliminar(paq)"
                  :disabled="paq.reservasActivas > 0"
                  :title="paq.reservasActivas > 0 ? 'Tiene reservas activas' : ''"
                  type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
                  Eliminar
                </button>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- ═══ MODAL FORM PAQUETE ═══ -->
    <div v-if="formAbierto" class="adm-modal-overlay" @click.self="cerrarForm">
      <div class="adm-modal adm-modal--lg">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">{{ editando ? 'Editar paquete' : 'Nuevo paquete turístico' }}</h3>
          <button class="adm-modal__close" @click="cerrarForm" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="adm-modal__body">
          <div class="adm-form-grid">

            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Nombre del paquete *</label>
              <input class="adm-field__input" v-model="form.nombre" placeholder="Ej. Paquete Cancún Todo Incluido" type="text" />
            </div>

            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Descripción</label>
              <textarea class="adm-field__input adm-field__textarea" v-model="form.descripcion"
                placeholder="Describe el paquete..."></textarea>
            </div>

            <!-- Selección de vuelo -->
            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Vuelo *</label>
              <select class="adm-field__input" v-model="form.vueloId">
                <option value="">Seleccionar vuelo...</option>
                <option v-for="v in vuelosDisponibles" :key="v.id" :value="v.id">
                  {{ v.numeroVuelo }} — {{ v.origenCodigo }} → {{ v.destinoCodigo }} ({{ v.aerolinea }})
                </option>
              </select>
            </div>

            <!-- Selección de hotel/habitación -->
            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Hotel / Habitación *</label>
              <select class="adm-field__input" v-model="form.habitacionId">
                <option value="">Seleccionar hospedaje...</option>
                <option v-for="h in habitacionesDisponibles" :key="h.id" :value="h.id">
                  {{ h.nombreHotel }} — {{ h.tipoHabitacion }} ({{ h.ciudad }})
                </option>
              </select>
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Precio especial del paquete *</label>
              <div class="adm-field__prefix">
                <span>$</span>
                <input class="adm-field__input" v-model.number="form.precioEspecial" type="number" min="0" step="0.01" placeholder="0.00" />
              </div>
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Estado</label>
              <select class="adm-field__input" v-model="form.activo">
                <option :value="true">Activo</option>
                <option :value="false">Inactivo</option>
              </select>
            </div>

          </div>

          <!-- Preview del ahorro -->
          <div v-if="previewAhorro !== null" class="adm-paq-preview">
            <svg viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" width="14" height="14"><polyline points="20 6 9 17 4 12"/></svg>
            Los clientes ahorrarán <strong>${{ previewAhorro.toFixed(2) }}</strong> vs. reserva individual
          </div>

          <p v-if="formError" class="adm-form-error">{{ formError }}</p>
        </div>

        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="cerrarForm" type="button">Cancelar</button>
          <button class="adm-btn adm-btn--yellow" @click="guardarPaquete" :disabled="guardando" type="button">
            <div v-if="guardando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>{{ editando ? 'Guardar cambios' : 'Crear paquete' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- ═══ MODAL CONFIRMAR ELIMINAR ═══ -->
    <div v-if="paqueteAEliminar" class="adm-modal-overlay" @click.self="paqueteAEliminar=null">
      <div class="adm-modal adm-modal--sm">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">Confirmar eliminación</h3>
          <button class="adm-modal__close" @click="paqueteAEliminar=null" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="adm-modal__body">
          <p>¿Estás seguro que deseas eliminar el paquete <strong>{{ paqueteAEliminar.nombre }}</strong>? Esta acción no se puede deshacer.</p>
        </div>
        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="paqueteAEliminar=null" type="button">Cancelar</button>
          <button class="adm-btn adm-btn--danger" @click="eliminarPaquete" :disabled="eliminando" type="button">
            <div v-if="eliminando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>Eliminar</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div v-if="toast" class="adm-toast" :class="`adm-toast--${toast.tipo}`">
      <svg v-if="toast.tipo==='ok'" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><polyline points="20 6 9 17 4 12"/></svg>
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" width="15" height="15"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      {{ toast.msg }}
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

const API = 'http://localhost:7000'

const paquetes             = ref([])
const vuelosDisponibles    = ref([])
const habitacionesDisponibles = ref([])
const loading              = ref(true)
const error                = ref('')
const filtroEstado         = ref('todos')
const formAbierto          = ref(false)
const editando             = ref(null)
const guardando            = ref(false)
const eliminando           = ref(false)
const paqueteAEliminar     = ref(null)
const formError            = ref('')
const toast                = ref(null)

const formVacio = () => ({
  nombre: '', descripcion: '', vueloId: '', habitacionId: '', precioEspecial: '', activo: true
})
const form = ref(formVacio())

const filtrosOpts = [
  { val: 'todos',    label: 'Todos' },
  { val: 'activo',   label: 'Activos' },
  { val: 'inactivo', label: 'Inactivos' },
]

const paquetesFiltrados = computed(() => {
  if (filtroEstado.value === 'todos')    return paquetes.value
  if (filtroEstado.value === 'activo')   return paquetes.value.filter(p => p.activo)
  if (filtroEstado.value === 'inactivo') return paquetes.value.filter(p => !p.activo)
  return paquetes.value
})

const contarPorEstado = (e) => {
  if (e === 'todos')    return paquetes.value.length
  if (e === 'activo')   return paquetes.value.filter(p => p.activo).length
  if (e === 'inactivo') return paquetes.value.filter(p => !p.activo).length
  return 0
}

const previewAhorro = computed(() => {
  const vuelo = vuelosDisponibles.value.find(v => v.id === form.value.vueloId)
  const hab   = habitacionesDisponibles.value.find(h => h.id === form.value.habitacionId)
  if (!vuelo || !hab || !form.value.precioEspecial) return null
  const individual = (vuelo.precio || 0) + (hab.precioPorNoche || 0)
  return Math.max(0, individual - form.value.precioEspecial)
})

onMounted(() => cargarTodo())

async function cargarTodo() {
  loading.value = true; error.value = ''
  await new Promise(r => setTimeout(r, 400))
  vuelosDisponibles.value = [
    { id:1, numeroVuelo:'MV-101', origenCodigo:'GUA', destinoCodigo:'CUN', aerolinea:'Broom AirLine', precio: 320 },
    { id:2, numeroVuelo:'MV-205', origenCodigo:'GUA', destinoCodigo:'MEX', aerolinea:'Broom AirLine', precio: 280 },
    { id:3, numeroVuelo:'CM-412', origenCodigo:'GUA', destinoCodigo:'MIA', aerolinea:'Copa Airlines',  precio: 390 },
    { id:4, numeroVuelo:'AV-309', origenCodigo:'GUA', destinoCodigo:'PTY', aerolinea:'Avianca',        precio: 210 },
  ]
  habitacionesDisponibles.value = [
    { id:1, nombreHotel:'Casa Santo Domingo', tipoHabitacion:'Suite Deluxe',    ciudad:'Antigua',          precioPorNoche: 185 },
    { id:2, nombreHotel:'Casa Santo Domingo', tipoHabitacion:'Habitación Doble', ciudad:'Antigua',         precioPorNoche: 120 },
    { id:3, nombreHotel:'Barceló Guatemala',  tipoHabitacion:'Superior King',   ciudad:'Ciudad de Guatemala', precioPorNoche: 95 },
    { id:4, nombreHotel:'Camino Real',         tipoHabitacion:'Junior Suite',    ciudad:'Ciudad de Guatemala', precioPorNoche: 150 },
    { id:5, nombreHotel:'Hotel Cancún Palace', tipoHabitacion:'Ocean View',      ciudad:'Cancún',           precioPorNoche: 220 },
  ]
  paquetes.value = [
    {
      id:1, nombre:'Paquete Cancún Todo Incluido', descripcion:'Vuelo directo + 7 noches en hotel frente al mar. Desayuno incluido.', activo:true, reservasActivas:3,
      precioIndividual:2560.00, precioEspecial:1999.00,
      vuelo: { id:1, origen:'GUA', destino:'CUN', aerolinea:'Broom AirLine', numeroVuelo:'MV-101' },
      hotel: { habitacionId:5, nombre:'Hotel Cancún Palace', tipoHabitacion:'Ocean View', ciudad:'Cancún' },
    },
    {
      id:2, nombre:'Escapada Ciudad de México', descripcion:'4 noches en el corazón de CDMX con vuelo incluido.', activo:true, reservasActivas:1,
      precioIndividual:1400.00, precioEspecial:1099.00,
      vuelo: { id:2, origen:'GUA', destino:'MEX', aerolinea:'Broom AirLine', numeroVuelo:'MV-205' },
      hotel: { habitacionId:3, nombre:'Barceló Guatemala', tipoHabitacion:'Superior King', ciudad:'Ciudad de México' },
    },
    {
      id:3, nombre:'Fin de Semana en Antigua', descripcion:'2 noches en Casa Santo Domingo + vuelo.', activo:true, reservasActivas:0,
      precioIndividual:680.00, precioEspecial:520.00,
      vuelo: { id:4, origen:'GUA', destino:'PTY', aerolinea:'Avianca', numeroVuelo:'AV-309' },
      hotel: { habitacionId:2, nombre:'Casa Santo Domingo', tipoHabitacion:'Habitación Doble', ciudad:'Antigua' },
    },
    {
      id:4, nombre:'Miami Express', descripcion:'Paquete exprés 3 noches en Miami.', activo:false, reservasActivas:0,
      precioIndividual:1170.00, precioEspecial:950.00,
      vuelo: { id:3, origen:'GUA', destino:'MIA', aerolinea:'Copa Airlines', numeroVuelo:'CM-412' },
      hotel: { habitacionId:4, nombre:'Camino Real', tipoHabitacion:'Junior Suite', ciudad:'Miami' },
    },
  ]
  loading.value = false
}

async function cargarPaquetes() {
  // Demo: no-op, los datos ya están en memoria
}

function abrirFormNuevo() {
  editando.value  = null
  form.value      = formVacio()
  formError.value = ''
  formAbierto.value = true
}

function abrirFormEditar(paq) {
  editando.value = paq.id
  form.value = {
    nombre:         paq.nombre,
    descripcion:    paq.descripcion || '',
    vueloId:        paq.vuelo?.id || '',
    habitacionId:   paq.hotel?.habitacionId || '',
    precioEspecial: paq.precioEspecial || '',
    activo:         paq.activo,
  }
  formError.value   = ''
  formAbierto.value = true
}

function cerrarForm() {
  formAbierto.value = false
  editando.value    = null
}

function validarForm() {
  if (!form.value.nombre.trim())    { formError.value = 'El nombre es obligatorio.'; return false }
  if (!form.value.vueloId)          { formError.value = 'Selecciona un vuelo.'; return false }
  if (!form.value.habitacionId)     { formError.value = 'Selecciona un hospedaje.'; return false }
  if (!form.value.precioEspecial || form.value.precioEspecial <= 0) {
    formError.value = 'El precio especial debe ser mayor a 0.'; return false
  }
  formError.value = ''; return true
}

async function guardarPaquete() {
  if (!validarForm()) return
  guardando.value = true
  await new Promise(r => setTimeout(r, 600))
  const vuelo = vuelosDisponibles.value.find(v => v.id === form.value.vueloId)
  const hab   = habitacionesDisponibles.value.find(h => h.id === form.value.habitacionId)
  if (editando.value) {
    const idx = paquetes.value.findIndex(p => p.id === editando.value)
    if (idx !== -1) paquetes.value[idx] = {
      ...paquetes.value[idx], ...form.value,
      precioIndividual: (vuelo?.precio||0) + (hab?.precioPorNoche||0)*7,
      vuelo: vuelo ? { id:vuelo.id, origen:vuelo.origenCodigo, destino:vuelo.destinoCodigo, aerolinea:vuelo.aerolinea, numeroVuelo:vuelo.numeroVuelo } : paquetes.value[idx].vuelo,
      hotel: hab   ? { habitacionId:hab.id, nombre:hab.nombreHotel, tipoHabitacion:hab.tipoHabitacion, ciudad:hab.ciudad } : paquetes.value[idx].hotel,
    }
    mostrarToast('ok', 'Paquete actualizado.')
  } else {
    const newId = Math.max(...paquetes.value.map(p => p.id)) + 1
    paquetes.value.push({
      id: newId, nombre: form.value.nombre, descripcion: form.value.descripcion,
      activo: form.value.activo, reservasActivas: 0,
      precioIndividual: (vuelo?.precio||0) + (hab?.precioPorNoche||0)*7,
      precioEspecial: form.value.precioEspecial,
      vuelo: vuelo ? { id:vuelo.id, origen:vuelo.origenCodigo, destino:vuelo.destinoCodigo, aerolinea:vuelo.aerolinea, numeroVuelo:vuelo.numeroVuelo } : null,
      hotel: hab   ? { habitacionId:hab.id, nombre:hab.nombreHotel, tipoHabitacion:hab.tipoHabitacion, ciudad:hab.ciudad } : null,
    })
    mostrarToast('ok', 'Paquete creado.')
  }
  guardando.value = false
  cerrarForm()
}

async function toggleActivo(paq) {
  await new Promise(r => setTimeout(r, 300))
  paq.activo = !paq.activo
  mostrarToast('ok', `Paquete ${paq.activo ? 'activado' : 'desactivado'}.`)
}

function confirmarEliminar(paq) { paqueteAEliminar.value = paq }

async function eliminarPaquete() {
  if (!paqueteAEliminar.value) return
  eliminando.value = true
  await new Promise(r => setTimeout(r, 500))
  paquetes.value = paquetes.value.filter(p => p.id !== paqueteAEliminar.value.id)
  mostrarToast('ok', 'Paquete eliminado.')
  paqueteAEliminar.value = null
  eliminando.value = false
}

function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => toast.value = null, 3500)
}
</script>