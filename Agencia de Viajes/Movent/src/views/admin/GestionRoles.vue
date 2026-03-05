<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- ═══ SIDEBAR ADMIN ═══ -->
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
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Paquetes
            </router-link>
            <router-link to="/admin/roles" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Roles
            </router-link>
          </nav>
        </aside>

        <!-- ═══ CONTENIDO ═══ -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Gestión de Roles</h1>
              <p class="adm-topbar__sub">Administra los roles y permisos de los usuarios</p>
            </div>
            <div class="adm-topbar__actions">
              <div class="adm-search">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="busqueda" type="text" placeholder="Buscar usuario..." class="adm-search__input" />
              </div>
            </div>
          </div>

          <!-- Filtro por rol -->
          <div class="adm-filtros-rol">
            <button v-for="r in rolesOpts" :key="r.val"
              :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtroRol === r.val }]"
              @click="filtroRol = r.val" type="button">
              {{ r.label }}
              <span class="adm-filtro-rol__n">{{ contarPorRol(r.val) }}</span>
            </button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando usuarios...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargarUsuarios" type="button">Reintentar</button>
          </div>

          <!-- Sin resultados -->
          <div v-else-if="usuariosFiltrados.length === 0" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <p>No se encontraron usuarios</p>
          </div>

          <!-- Tabla usuarios -->
          <div v-else class="adm-tabla-wrap">
            <table class="adm-tabla">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Correo</th>
                  <th>Rol actual</th>
                  <th>Registro</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in usuariosFiltrados" :key="u.id">
                  <td>
                    <div class="adm-tabla__user">
                      <div class="adm-tabla__avatar">{{ iniciales(u) }}</div>
                      <div>
                        <p class="adm-tabla__nombre">{{ u.nombre }} {{ u.apellido }}</p>
                        <p class="adm-tabla__pais">{{ u.pais }}</p>
                      </div>
                    </div>
                  </td>
                  <td class="adm-tabla__correo">{{ u.correo }}</td>
                  <td>
                    <span class="adm-badge" :class="`adm-badge--rol-${u.rol}`">{{ u.rol }}</span>
                  </td>
                  <td>{{ formatFecha(u.fechaRegistro) }}</td>
                  <td>
                    <button class="adm-btn adm-btn--sm adm-btn--outline"
                      @click="abrirModal(u)" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      Cambiar rol
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

        </div>
      </div>
    </div>

    <!-- ═══ MODAL CAMBIO DE ROL ═══ -->
    <div v-if="modalUsuario" class="adm-modal-overlay" @click.self="cerrarModal">
      <div class="adm-modal">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">Cambiar rol</h3>
          <button class="adm-modal__close" @click="cerrarModal" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="adm-modal__user">
          <div class="adm-modal__avatar">{{ iniciales(modalUsuario) }}</div>
          <div>
            <p class="adm-modal__nombre">{{ modalUsuario.nombre }} {{ modalUsuario.apellido }}</p>
            <p class="adm-modal__correo">{{ modalUsuario.correo }}</p>
          </div>
        </div>

        <div class="adm-modal__body">
          <p class="adm-modal__lbl">Rol actual</p>
          <span class="adm-badge" :class="`adm-badge--rol-${modalUsuario.rol}`">{{ modalUsuario.rol }}</span>

          <p class="adm-modal__lbl" style="margin-top:16px">Nuevo rol</p>
          <div class="adm-roles-grid">
            <button v-for="r in rolesDisponibles" :key="r.val"
              :class="['adm-rol-card', { 'adm-rol-card--active': nuevoRol === r.val }]"
              @click="nuevoRol = r.val" type="button">
              <div class="adm-rol-card__icon" :style="{ background: r.color }">
                <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="16" height="16" v-html="r.icon"></svg>
              </div>
              <div>
                <p class="adm-rol-card__nombre">{{ r.label }}</p>
                <p class="adm-rol-card__desc">{{ r.desc }}</p>
              </div>
            </button>
          </div>

          <p v-if="modalError" class="adm-form-error">{{ modalError }}</p>
        </div>

        <div class="adm-modal__foot">
          <button class="adm-btn adm-btn--ghost" @click="cerrarModal" type="button">Cancelar</button>
          <button class="adm-btn adm-btn--yellow" @click="guardarRol"
            :disabled="!nuevoRol || nuevoRol === modalUsuario.rol || guardando" type="button">
            <div v-if="guardando" class="adm-spinner adm-spinner--sm"></div>
            <span v-else>Guardar cambio</span>
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

const usuarios     = ref([])
const loading      = ref(true)
const error        = ref('')
const busqueda     = ref('')
const filtroRol    = ref('todos')
const modalUsuario = ref(null)
const nuevoRol     = ref('')
const guardando    = ref(false)
const modalError   = ref('')
const toast        = ref(null)

const rolesOpts = [
  { val: 'todos',              label: 'Todos' },
  { val: 'Administrador',      label: 'Administrador' },
  { val: 'Cliente Registrado', label: 'Cliente' },
  { val: 'WebService',         label: 'WebService' },
]

const rolesDisponibles = [
  {
    val:   'Administrador',
    label: 'Administrador',
    desc:  'Acceso total al sistema',
    color: '#1C1A18',
    icon:  '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  },
  {
    val:   'Cliente Registrado',
    label: 'Cliente Registrado',
    desc:  'Puede buscar y reservar',
    color: '#FFCC00',
    icon:  '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  },
  {
    val:   'WebService',
    label: 'WebService',
    desc:  'Acceso solo a la API',
    color: '#3b82f6',
    icon:  '<circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>',
  },
]

const usuariosFiltrados = computed(() => {
  let list = usuarios.value
  if (filtroRol.value !== 'todos')
    list = list.filter(u => u.rol === filtroRol.value)
  if (busqueda.value.trim())
    list = list.filter(u =>
      `${u.nombre} ${u.apellido} ${u.correo}`.toLowerCase()
        .includes(busqueda.value.toLowerCase())
    )
  return list
})

const contarPorRol = (rol) =>
  rol === 'todos'
    ? usuarios.value.length
    : usuarios.value.filter(u => u.rol === rol).length

onMounted(() => cargarUsuarios())

async function cargarUsuarios() {
  loading.value = true; error.value = ''
  await new Promise(r => setTimeout(r, 400))
  usuarios.value = [
    { id:1, nombre:'Carlos',  apellido:'Méndez',    correo:'carlos.mendez@gmail.com',   pais:'Guatemala', rol:'Administrador',      fechaRegistro:'2025-01-15T10:30:00Z' },
    { id:2, nombre:'María',   apellido:'López',     correo:'maria.lopez@hotmail.com',   pais:'Guatemala', rol:'Cliente Registrado',  fechaRegistro:'2025-02-03T14:20:00Z' },
    { id:3, nombre:'Pedro',   apellido:'Ruiz',      correo:'pedro.ruiz@yahoo.com',      pais:'México',    rol:'Cliente Registrado',  fechaRegistro:'2025-02-18T09:15:00Z' },
    { id:4, nombre:'Ana',     apellido:'García',    correo:'ana.garcia@gmail.com',      pais:'Guatemala', rol:'Cliente Registrado',  fechaRegistro:'2025-03-01T16:45:00Z' },
    { id:5, nombre:'Luis',    apellido:'Castillo',  correo:'luis.castillo@empresa.com', pais:'Costa Rica',rol:'WebService',          fechaRegistro:'2025-03-10T11:00:00Z' },
    { id:6, nombre:'Sofía',   apellido:'Reyes',     correo:'sofia.reyes@gmail.com',     pais:'Guatemala', rol:'Cliente Registrado',  fechaRegistro:'2025-04-05T08:30:00Z' },
    { id:7, nombre:'Diego',   apellido:'Hernández', correo:'diego.h@outlook.com',       pais:'El Salvador',rol:'Cliente Registrado', fechaRegistro:'2025-04-22T13:10:00Z' },
    { id:8, nombre:'Valeria', apellido:'Morales',   correo:'valeria.m@gmail.com',       pais:'Guatemala', rol:'Cliente Registrado',  fechaRegistro:'2025-05-14T17:25:00Z' },
    { id:9, nombre:'API',     apellido:'Bot',       correo:'api@movent.gt',             pais:'Guatemala', rol:'WebService',          fechaRegistro:'2025-01-01T00:00:00Z' },
  ]
  loading.value = false
}

function abrirModal(u) {
  modalUsuario.value = u
  nuevoRol.value     = u.rol
  modalError.value   = ''
}

function cerrarModal() {
  modalUsuario.value = null
  nuevoRol.value     = ''
  modalError.value   = ''
}

async function guardarRol() {
  if (!nuevoRol.value || nuevoRol.value === modalUsuario.value.rol) return
  guardando.value = true; modalError.value = ''
  await new Promise(r => setTimeout(r, 600))
  // Actualizar localmente (demo)
  const idx = usuarios.value.findIndex(u => u.id === modalUsuario.value.id)
  if (idx !== -1) usuarios.value[idx].rol = nuevoRol.value
  mostrarToast('ok', `Rol actualizado a "${nuevoRol.value}"`)
  guardando.value = false
  cerrarModal()
}

function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => toast.value = null, 3500)
}

function iniciales(u) {
  if (!u) return '?'
  return `${(u.nombre||'')[0]||''}${(u.apellido||'')[0]||''}`.toUpperCase() || '?'
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}
</script>