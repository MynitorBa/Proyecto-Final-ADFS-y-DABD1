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
          </nav>
        </aside>

        <!-- Área principal: tabla de usuarios con filtros y buscador -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Gestión de Roles</h1>
              <p class="adm-topbar__sub">Administra los roles y permisos de los usuarios</p>
            </div>
            <div class="adm-topbar__actions">
              <!-- Buscador en tiempo real por nombre, apellido o correo -->
              <div class="adm-search">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="busqueda" type="text" placeholder="Buscar usuario..." class="adm-search__input" />
              </div>
            </div>
          </div>

          <!-- Botones de filtro rápido por tipo de rol -->
          <div class="adm-filtros-rol">
            <button v-for="r in rolesOpts" :key="r.val"
              :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtroRol === r.val }]"
              @click="filtroRol = r.val" type="button">
              {{ r.label }}
              <span class="adm-filtro-rol__n">{{ contarPorRol(r.val) }}</span>
            </button>
          </div>

          <!-- Estado de carga mientras se obtienen los usuarios -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando usuarios...</p>
          </div>

          <!-- Mensaje de error con botón para reintentar -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargarUsuarios" type="button">Reintentar</button>
          </div>

          <!-- Cuando no hay resultados según los filtros activos -->
          <div v-else-if="usuariosFiltrados.length === 0" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <p>No se encontraron usuarios</p>
          </div>

          <!-- Tabla principal de usuarios con su rol actual y acciones disponibles -->
          <div v-else class="adm-tabla-wrap">
            <table class="adm-tabla">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Correo</th>
                  <th>Rol actual</th>
                  <th>Nacimiento</th>
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
                        <p class="adm-tabla__pais">{{ u.correo }}</p>
                      </div>
                    </div>
                  </td>
                  <td class="adm-tabla__correo">{{ u.correo }}</td>
                  <td>
                    <span class="adm-badge" :class="`adm-badge--rol-${u.rolId}`">{{ u.rol }}</span>
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

    <!-- Modal para cambiar el rol de un usuario seleccionado -->
    <div v-if="modalUsuario" class="adm-modal-overlay" @click.self="cerrarModal">
      <div class="adm-modal">
        <div class="adm-modal__head">
          <h3 class="adm-modal__title">Cambiar rol</h3>
          <button class="adm-modal__close" @click="cerrarModal" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <!-- Identificación del usuario que se va a modificar -->
        <div class="adm-modal__user">
          <div class="adm-modal__avatar">{{ iniciales(modalUsuario) }}</div>
          <div>
            <p class="adm-modal__nombre">{{ modalUsuario.nombre }} {{ modalUsuario.apellido }}</p>
            <p class="adm-modal__correo">{{ modalUsuario.correo }}</p>
          </div>
        </div>

        <div class="adm-modal__body">
          <p class="adm-modal__lbl">Rol actual</p>
          <span class="adm-badge" :class="`adm-badge--rol-${modalUsuario.rolId}`">{{ modalUsuario.rol }}</span>

          <!-- Selector de tarjetas para elegir el nuevo rol -->
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

    <!-- Notificación toast de éxito o error al guardar un cambio de rol -->
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
 * @file GestionRoles.vue
 * @description Vista del panel de administración para gestionar los roles de los usuarios.
 * Permite filtrar usuarios por rol, buscarlos por nombre o correo y cambiar su rol
 * mediante un modal con tarjetas de selección.
 */
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Lista completa de usuarios cargados desde el servidor. @type {import('vue').Ref<Array>} */
const usuarios     = ref([])

/** Indica si la petición inicial de usuarios está en curso. @type {import('vue').Ref<boolean>} */
const loading      = ref(true)

/** Mensaje de error si la carga de usuarios falla. @type {import('vue').Ref<string>} */
const error        = ref('')

/** Texto ingresado en el buscador para filtrar usuarios. @type {import('vue').Ref<string>} */
const busqueda     = ref('')

/** Valor del filtro de rol activo ('todos', 'Administrador', etc.). @type {import('vue').Ref<string>} */
const filtroRol    = ref('todos')

/** Usuario seleccionado para editar su rol en el modal. @type {import('vue').Ref<Object|null>} */
const modalUsuario = ref(null)

/** Nombre del nuevo rol seleccionado dentro del modal. @type {import('vue').Ref<string>} */
const nuevoRol     = ref('')

/** Indica si la petición de guardado de rol está en proceso. @type {import('vue').Ref<boolean>} */
const guardando    = ref(false)

/** Error mostrado dentro del modal si el cambio de rol falla. @type {import('vue').Ref<string>} */
const modalError   = ref('')

/** Notificación temporal de éxito o error. @type {import('vue').Ref<{tipo: string, msg: string}|null>} */
const toast        = ref(null)

/**
 * Mapeo de nombre de rol a su ID numérico en la base de datos.
 * @type {Object.<string, number>}
 */
const ROL_ID = {
  'Administrador':      2,
  'Registrado': 1,
  'WebService':         3,
}

/**
 * Opciones disponibles para el filtro por rol en la barra superior.
 * @type {Array<{val: string, label: string}>}
 */
const rolesOpts = [
  { val: 'todos',              label: 'Todos' },
  { val: 'Administrador',      label: 'Administrador' },
  { val: 'Registrado', label: 'Cliente' },
  { val: 'WebService',         label: 'WebService' },
]

/**
 * Roles que se pueden asignar desde el modal, con descripción e ícono.
 * @type {Array<{val: string, label: string, desc: string, color: string, icon: string}>}
 */
const rolesDisponibles = [
  { val:'Administrador',      label:'Administrador',      desc:'Acceso total al sistema',    color:'#1C1A18', icon:'<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>' },
  { val:'Registrado',         label:'Registrado',         desc:'Puede buscar y reservar',    color:'#8B6B4A', icon:'<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>' },
  { val:'WebService',         label:'WebService',         desc:'Acceso solo a la API',       color:'#3b82f6', icon:'<circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>' },
]

/**
 * Lista de usuarios filtrada según el rol activo y el texto de búsqueda.
 * @type {import('vue').ComputedRef<Array>}
 */
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

/**
 * Devuelve la cantidad de usuarios que pertenecen a un rol dado.
 * @param {string} rol - El nombre del rol o 'todos' para el total.
 * @returns {number}
 */
const contarPorRol = (rol) =>
  rol === 'todos'
    ? usuarios.value.length
    : usuarios.value.filter(u => u.rol === rol).length

/** Carga los usuarios al montar el componente. */
onMounted(() => cargarUsuarios())

/**
 * Obtiene la lista de usuarios desde el backend y la almacena en el ref.
 * @returns {Promise<void>}
 */
async function cargarUsuarios() {
  loading.value = true; error.value = ''
  try {
    const res = await fetch(`${API}/api/usuarios`, { credentials: 'include' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    usuarios.value = await res.json()
  } catch (e) {
    error.value = 'No se pudieron cargar los usuarios. Verifica que el servidor esté activo.'
  } finally {
    loading.value = false
  }
}

/**
 * Abre el modal de cambio de rol para el usuario indicado.
 * @param {Object} u - El objeto usuario seleccionado desde la tabla.
 */
function abrirModal(u) {
  modalUsuario.value = u
  nuevoRol.value     = u.rol
  modalError.value   = ''
}

/**
 * Cierra el modal y limpia el estado interno.
 */
function cerrarModal() {
  modalUsuario.value = null
  nuevoRol.value     = ''
  modalError.value   = ''
}

/**
 * Envía la petición PUT al backend para actualizar el rol del usuario.
 * Si el backend devuelve un error con mensaje específico (ej: auto-degradación),
 * se muestra ese mensaje al usuario en lugar del genérico.
 * @returns {Promise<void>}
 */
async function guardarRol() {
  if (!nuevoRol.value || nuevoRol.value === modalUsuario.value.rol) return
  const rolId = ROL_ID[nuevoRol.value]
  if (!rolId) { modalError.value = 'Rol inválido'; return }

  guardando.value = true; modalError.value = ''
  try {
    const res = await fetch(`${API}/api/usuarios/${modalUsuario.value.id}/rol`, {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rolId }),
    })
    // Si la respuesta no es OK, leer el mensaje específico del backend
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      throw new Error(data.error || `Error ${res.status}`)
    }

    // Actualizar localmente para no tener que recargar toda la lista
    const idx = usuarios.value.findIndex(u => u.id === modalUsuario.value.id)
    if (idx !== -1) {
      usuarios.value[idx].rol   = nuevoRol.value
      usuarios.value[idx].rolId = rolId
    }
    mostrarToast('ok', `Rol actualizado a "${nuevoRol.value}"`)
    cerrarModal()
  } catch (err) {
    // Mostrar el mensaje específico del backend si viene, si no usar genérico
    modalError.value = err.message || 'Error al actualizar el rol. Intenta de nuevo.'
  } finally {
    guardando.value = false
  }
}

/**
 * Muestra una notificación temporal en pantalla y la oculta después de 3.5 segundos.
 * @param {'ok'|'err'} tipo - Tipo de notificación.
 * @param {string} msg - Mensaje a mostrar.
 */
function mostrarToast(tipo, msg) {
  toast.value = { tipo, msg }
  setTimeout(() => toast.value = null, 3500)
}

/**
 * Genera las iniciales de un usuario a partir de su nombre y apellido.
 * @param {Object} u - El objeto usuario.
 * @returns {string} Dos letras en mayúscula o '?' si no hay datos.
 */
function iniciales(u) {
  if (!u) return '?'
  return `${(u.nombre||'')[0]||''}${(u.apellido||'')[0]||''}`.toUpperCase() || '?'
}

/**
 * Formatea una fecha ISO a formato legible en español de Guatemala.
 * @param {string} f - La fecha a formatear.
 * @returns {string} Fecha formateada o '--'.
 */
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' }) }
  catch { return f }
}
</script>