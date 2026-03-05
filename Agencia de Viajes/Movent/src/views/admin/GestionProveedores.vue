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
            <router-link to="/admin/proveedores" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
              Proveedores
            </router-link>
            <router-link to="/admin/paquetes" class="adm-nav__item">
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
              <h1 class="adm-topbar__titulo">Proveedores</h1>
              <p class="adm-topbar__sub">Configura aerolíneas y hoteles conectados</p>
            </div>
            <button class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              Agregar proveedor
            </button>
          </div>

          <!-- Filtros tipo -->
          <div class="adm-filtros-rol">
            <button v-for="t in tiposOpts" :key="t.val"
              :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtroTipo === t.val }]"
              @click="filtroTipo = t.val" type="button">
              {{ t.label }}
              <span class="adm-filtro-rol__n">{{ contarPorTipo(t.val) }}</span>
            </button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando proveedores...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargarProveedores" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="proveedoresFiltrados.length === 0" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
            <p>No hay proveedores configurados</p>
            <button class="adm-btn adm-btn--yellow" @click="abrirFormNuevo" type="button">Agregar el primero</button>
          </div>

          <!-- Grid de proveedores -->
          <div v-else class="adm-proveedores-grid">
            <div v-for="p in proveedoresFiltrados" :key="p.id" class="adm-prov-card"
              :class="{ 'adm-prov-card--inactivo': !p.activo }">

              <div class="adm-prov-card__head">
                <div class="adm-prov-card__tipo-icon" :class="`adm-prov-card__tipo-icon--${p.tipo}`">
                  <svg v-if="p.tipo==='aerolinea'" viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                </div>
                <div class="adm-prov-card__info">
                  <h4 class="adm-prov-card__nombre">{{ p.nombre }}</h4>
                  <p class="adm-prov-card__tipo-lbl">{{ p.tipo === 'aerolinea' ? 'Aerolínea' : 'Hotel' }}</p>
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
                <div v-if="p.apiKey" class="adm-prov-card__row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/></svg>
                  <span class="adm-prov-card__apikey">••••••••{{ p.apiKey?.slice(-4) }}</span>
                </div>
                <div v-if="p.ultimaConexion" class="adm-prov-card__row">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                  <span>Última conexión: {{ formatFechaHora(p.ultimaConexion) }}</span>
                </div>
              </div>

              <div class="adm-prov-card__foot">
                <button class="adm-btn adm-btn--sm adm-btn--ghost" @click="probarConexion(p)" :disabled="probando===p.id" type="button">
                  <div v-if="probando===p.id" class="adm-spinner adm-spinner--sm"></div>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
                  Probar
                </button>
                <button class="adm-btn adm-btn--sm adm-btn--outline" @click="abrirFormEditar(p)" type="button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  Editar
                </button>
                <button class="adm-btn adm-btn--sm" :class="p.activo ? 'adm-btn--danger-ghost' : 'adm-btn--ghost'"
                  @click="toggleActivo(p)" type="button">
                  {{ p.activo ? 'Desactivar' : 'Activar' }}
                </button>
              </div>

            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- ═══ MODAL FORM PROVEEDOR ═══ -->
    <div v-if="formAbierto" class="adm-modal-overlay" @click.self="cerrarForm">
      <div class="adm-modal adm-modal--lg">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">{{ editando ? 'Editar proveedor' : 'Nuevo proveedor' }}</h3>
          <button class="adm-modal__close" @click="cerrarForm" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="adm-modal__body">
          <div class="adm-form-grid">

            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Nombre del proveedor *</label>
              <input class="adm-field__input" v-model="form.nombre" placeholder="Ej. Broom AirLine" type="text" />
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Tipo *</label>
              <select class="adm-field__input" v-model="form.tipo">
                <option value="">Seleccionar...</option>
                <option value="aerolinea">Aerolínea</option>
                <option value="hotel">Hotel</option>
              </select>
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Estado</label>
              <select class="adm-field__input" v-model="form.activo">
                <option :value="true">Activo</option>
                <option :value="false">Inactivo</option>
              </select>
            </div>

            <div class="adm-field adm-field--full">
              <label class="adm-field__label">URL del servicio REST *</label>
              <input class="adm-field__input" v-model="form.url" placeholder="https://api.proveedor.com" type="url" />
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Usuario / API Key</label>
              <input class="adm-field__input" v-model="form.apiKey" placeholder="API Key o usuario" type="text" />
            </div>

            <div class="adm-field">
              <label class="adm-field__label">Contraseña</label>
              <div class="adm-field__pass">
                <input class="adm-field__input" v-model="form.password"
                  :type="mostrarPass ? 'text' : 'password'" placeholder="Contraseña" />
                <button class="adm-field__pass-toggle" @click="mostrarPass=!mostrarPass" type="button">
                  <svg v-if="!mostrarPass" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                </button>
              </div>
            </div>

            <div class="adm-field adm-field--full">
              <label class="adm-field__label">Parámetros adicionales (JSON)</label>
              <textarea class="adm-field__input adm-field__textarea" v-model="form.parametros"
                placeholder='{"timeout": 5000, "version": "v2"}'></textarea>
            </div>

          </div>

          <p v-if="formError" class="adm-form-error">{{ formError }}</p>
        </div>

        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="cerrarForm" type="button">Cancelar</button>
          <button class="adm-btn adm-btn--outline" @click="probarConexionForm" :disabled="!form.url || probandoForm" type="button">
            <div v-if="probandoForm" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>Probar conexión</span>
          </button>
          <button class="adm-btn adm-btn--yellow" @click="guardarProveedor" :disabled="guardando" type="button">
            <div v-if="guardando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>{{ editando ? 'Guardar cambios' : 'Agregar proveedor' }}</span>
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

const proveedores  = ref([])
const loading      = ref(true)
const error        = ref('')
const filtroTipo   = ref('todos')
const formAbierto  = ref(false)
const editando     = ref(null)
const guardando    = ref(false)
const probando     = ref(null)
const probandoForm = ref(false)
const mostrarPass  = ref(false)
const formError    = ref('')
const toast        = ref(null)

const formVacio = () => ({
  nombre: '', tipo: '', url: '', apiKey: '', password: '', parametros: '', activo: true
})
const form = ref(formVacio())

const tiposOpts = [
  { val: 'todos',     label: 'Todos' },
  { val: 'aerolinea', label: 'Aerolíneas' },
  { val: 'hotel',     label: 'Hoteles' },
]

const proveedoresFiltrados = computed(() =>
  filtroTipo.value === 'todos'
    ? proveedores.value
    : proveedores.value.filter(p => p.tipo === filtroTipo.value)
)

const contarPorTipo = (t) =>
  t === 'todos' ? proveedores.value.length : proveedores.value.filter(p => p.tipo === t).length

onMounted(() => cargarProveedores())

async function cargarProveedores() {
  loading.value = true; error.value = ''
  await new Promise(r => setTimeout(r, 400))
  proveedores.value = [
    { id:1, nombre:'Broom AirLine',      tipo:'aerolinea', url:'http://localhost:7000',             apiKey:'brl_k9x2mN4pQz',  activo:true,  ultimaConexion: new Date(Date.now()-3600000).toISOString() },
    { id:2, nombre:'Copa Airlines',      tipo:'aerolinea', url:'https://api.copaair.com/v2',         apiKey:'copa_api_8sTrXv', activo:true,  ultimaConexion: new Date(Date.now()-7200000).toISOString() },
    { id:3, nombre:'Avianca Guatemala',  tipo:'aerolinea', url:'https://api.avianca.com/flights',    apiKey:'avi_gtm_4kLpWq',  activo:true,  ultimaConexion: new Date(Date.now()-14400000).toISOString() },
    { id:4, nombre:'Casa Santo Domingo', tipo:'hotel',     url:'https://api.casasantodomingo.com.gt',apiKey:'csd_h7nBmRw2',   activo:true,  ultimaConexion: new Date(Date.now()-28800000).toISOString() },
    { id:5, nombre:'Barceló Guatemala',  tipo:'hotel',     url:'https://api.barcelo.com/gt/rooms',   apiKey:'bar_gt_p5vYkJ',  activo:false, ultimaConexion: new Date(Date.now()-604800000).toISOString() },
    { id:6, nombre:'Camino Real',        tipo:'hotel',     url:'https://api.caminoreal.com/gt',      apiKey:'cr_gt_3xHnDm',   activo:true,  ultimaConexion: new Date(Date.now()-86400000).toISOString() },
  ]
  loading.value = false
}

function abrirFormNuevo() {
  editando.value  = null
  form.value      = formVacio()
  formError.value = ''
  formAbierto.value = true
}

function abrirFormEditar(p) {
  editando.value  = p.id
  form.value      = { nombre: p.nombre, tipo: p.tipo, url: p.url, apiKey: p.apiKey || '', password: '', parametros: p.parametros ? JSON.stringify(p.parametros) : '', activo: p.activo }
  formError.value = ''
  formAbierto.value = true
}

function cerrarForm() {
  formAbierto.value = false
  editando.value    = null
  mostrarPass.value = false
}

function validarForm() {
  if (!form.value.nombre.trim()) { formError.value = 'El nombre es obligatorio.'; return false }
  if (!form.value.tipo)          { formError.value = 'Selecciona el tipo.'; return false }
  if (!form.value.url.trim())    { formError.value = 'La URL es obligatoria.'; return false }
  formError.value = ''; return true
}

async function guardarProveedor() {
  if (!validarForm()) return
  guardando.value = true
  await new Promise(r => setTimeout(r, 600))
  if (editando.value) {
    const idx = proveedores.value.findIndex(p => p.id === editando.value)
    if (idx !== -1) proveedores.value[idx] = { ...proveedores.value[idx], ...form.value }
    mostrarToast('ok', 'Proveedor actualizado.')
  } else {
    const newId = Math.max(...proveedores.value.map(p => p.id)) + 1
    proveedores.value.push({ id: newId, ...form.value, ultimaConexion: null })
    mostrarToast('ok', 'Proveedor agregado.')
  }
  guardando.value = false
  cerrarForm()
}

async function probarConexion(p) {
  probando.value = p.id
  await new Promise(r => setTimeout(r, 800))
  mostrarToast(p.activo ? 'ok' : 'err', p.activo ? `Conexión exitosa con ${p.nombre}` : `Sin respuesta de ${p.nombre}`)
  probando.value = null
}

async function probarConexionForm() {
  probandoForm.value = true
  await new Promise(r => setTimeout(r, 800))
  mostrarToast('ok', 'Conexión exitosa.')
  probandoForm.value = false
}

async function toggleActivo(p) {
  await new Promise(r => setTimeout(r, 300))
  p.activo = !p.activo
  mostrarToast('ok', `Proveedor ${p.activo ? 'activado' : 'desactivado'}.`)
}

function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => toast.value = null, 3500)
}

function formatFechaHora(f) {
  if (!f) return '--'
  return new Date(f).toLocaleString('es-GT', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' })
}
</script>