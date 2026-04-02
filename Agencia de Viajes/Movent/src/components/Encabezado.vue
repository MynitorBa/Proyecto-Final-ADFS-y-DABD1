<template>
  <header class="header" :class="{ scrolled: isScrolled }">
    <div class="header-container">

      <button class="logo" @click="$router.push('/principal')" aria-label="Ir al inicio" type="button">
        <img src="/movent.png" alt="Movent" class="logo-image" />
      </button>

      <nav class="desktop-nav">
        <router-link to="/principal" class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
          Inicio
        </router-link>
        <router-link to="/informacion" class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          Información
        </router-link>
        <router-link to="/mis-reservaciones" class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Mis Reservas
        </router-link>
      </nav>

      <form class="search-bar" @submit.prevent="handleSearch">
        <div class="search-input-wrapper">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input type="text" v-model="searchQuery" placeholder="Busca tu próximo destino..." class="search-input" aria-label="Buscar" />
        </div>
      </form>

      <div class="user-actions">

        <!-- ── CARRITO ── -->
        <div class="cart-wrap">
          <button
            class="cart-btn"
            :class="{ 'cart-btn--active': reservaActiva }"
            @click="handleCartClick"
            @mouseenter="reservaActiva && (showCartPreview = true)"
            @mouseleave="showCartPreview = false"
            aria-label="Ir a pagar"
            type="button"
          >
            <svg viewBox="0 0 24 24" fill="none" :stroke="reservaActiva ? '#FFCC00' : '#1C1A18'" stroke-width="2" width="22" height="22">
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            <!-- Badge indicador de reserva activa -->
            <span v-if="reservaActiva" class="cart-badge">
              <svg viewBox="0 0 24 24" fill="currentColor" width="8" height="8"><circle cx="12" cy="12" r="10"/></svg>
            </span>
          </button>

          <!-- Preview tooltip al hover si hay reserva activa -->
          <div v-if="reservaActiva && showCartPreview" class="cart-dropdown">
            <div class="cart-dropdown__head">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14">
                <path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/>
              </svg>
              Reserva pendiente de pago
            </div>

            <div class="cart-dropdown__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
              <span style="font-family:monospace;font-size:11px;font-weight:700;color:#1C1A18;">
                {{ noReservacionActiva || '—' }}
              </span>
            </div>

            <div v-if="tipoReservaActiva" class="cart-dropdown__item" style="font-size:11px;color:#9a9089;padding-top:4px;padding-bottom:4px;">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>
              </svg>
              {{ tipoReservaActiva }}
            </div>

            <div v-if="totalReservaActiva" class="cart-dropdown__item" style="border-top:1px solid #f0ebe3;justify-content:space-between;">
              <span style="font-size:11px;color:#9a9089;">Total</span>
              <strong style="font-size:14px;color:#1C1A18;">{{ totalReservaActiva }}</strong>
            </div>

            <button class="cart-dropdown__item cart-dropdown__item--cta" @click="handleCartClick" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
              Ir a pagar
            </button>

            <button class="cart-dropdown__item cart-dropdown__item--cancel" @click.stop="descartarReserva" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
              Descartar reserva
            </button>
          </div>
        </div>

        <!-- ── CON SESIÓN ── -->
        <template v-if="sesion">
          <div v-if="showUserMenu" class="user-dropdown-overlay" @click="showUserMenu = false"></div>
          <div class="user-chip" @click.stop="toggleUserMenu">
            <div class="user-chip__avatar">{{ iniciales }}</div>
            <span class="user-chip__nombre">{{ nombreVisible }}</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
          </div>

          <div v-if="showUserMenu" class="user-dropdown" @click.stop>
            <div class="user-dropdown__head">
              <div class="user-dropdown__avatar">{{ iniciales }}</div>
              <div>
                <p class="user-dropdown__nombre">{{ nombreVisible }}</p>
                <span class="user-dropdown__rol" :class="{ 'user-dropdown__rol--admin': sesion.isAdmin }">
                  {{ sesion.isAdmin ? 'Administrador' : 'Cliente' }}
                </span>
              </div>
            </div>
            <div class="user-dropdown__divider"></div>

            <template v-if="sesion.isAdmin">
              <router-link to="/admin/dashboard" class="user-dropdown__item user-dropdown__item--admin" @click="showUserMenu = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                Panel Admin
              </router-link>
              <router-link to="/admin/roles" class="user-dropdown__item" @click="showUserMenu = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                Roles
              </router-link>
              <router-link to="/admin/proveedores" class="user-dropdown__item" @click="showUserMenu = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
                Proveedores
              </router-link>
              <router-link to="/admin/paquetes" class="user-dropdown__item" @click="showUserMenu = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                Paquetes
              </router-link>
              <div class="user-dropdown__divider"></div>
            </template>

            <router-link to="/mis-reservaciones" class="user-dropdown__item" @click="showUserMenu = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              Mis reservaciones
            </router-link>
            <div class="user-dropdown__divider"></div>
            <button class="user-dropdown__item user-dropdown__item--logout" @click="cerrarSesion" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              Cerrar sesión
            </button>
          </div>
        </template>

        <!-- ── SIN SESIÓN ── -->
        <template v-else>
          <router-link to="/ingreso" class="btn-secondary">Iniciar Sesión</router-link>
          <router-link to="/registro" class="btn-primary">Registrarse</router-link>
        </template>

        <button class="mobile-menu-toggle" @click.stop="toggleMobileMenu" aria-label="Abrir menu" type="button">
          <svg v-if="showMobileMenu" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
      </div>
    </div>

    <!-- ── MENÚ MÓVIL ── -->
    <nav v-if="showMobileMenu" class="mobile-nav">
      <form class="mobile-search" @submit.prevent="handleSearch">
        <div class="search-input-wrapper">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input type="text" v-model="searchQuery" placeholder="Busca tu próximo destino..." class="search-input" />
        </div>
      </form>
      <div class="mobile-nav-links">
        <router-link to="/principal"         class="mobile-nav-link" @click="showMobileMenu = false">Inicio</router-link>
        <router-link to="/informacion"        class="mobile-nav-link" @click="showMobileMenu = false">Información</router-link>
        <router-link to="/mis-reservaciones"  class="mobile-nav-link" @click="showMobileMenu = false">Mis Reservas</router-link>

        <!-- Ir a pagar en móvil si hay reserva activa -->
        <button v-if="reservaActiva" class="mobile-nav-link mobile-nav-link--pagar" @click="handleCartClick; showMobileMenu = false" type="button">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          Ir a pagar · {{ noReservacionActiva }}
        </button>
        <router-link v-else to="/checkout" class="mobile-nav-link" @click="showMobileMenu = false">Ir a pagar</router-link>

        <template v-if="sesion?.isAdmin">
          <div class="mobile-divider"></div>
          <router-link to="/admin/dashboard"   class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu = false">Panel Admin</router-link>
          <router-link to="/admin/roles"        class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu = false">Roles</router-link>
          <router-link to="/admin/proveedores"  class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu = false">Proveedores</router-link>
          <router-link to="/admin/paquetes"     class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu = false">Paquetes</router-link>
        </template>

        <div class="mobile-divider"></div>
        <template v-if="sesion">
          <button class="mobile-nav-link" @click="cerrarSesion" type="button">
            Cerrar sesión ({{ nombreVisible }})
          </button>
        </template>
        <template v-else>
          <router-link to="/ingreso"  class="mobile-nav-link" @click="showMobileMenu = false">Iniciar Sesión</router-link>
          <router-link to="/registro" class="mobile-nav-link primary" @click="showMobileMenu = false">Registrarse</router-link>
        </template>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import '../styles/encabezado.css'

const router         = useRouter()
const route          = useRoute()

const isScrolled     = ref(false)
const showMobileMenu = ref(false)
const showUserMenu   = ref(false)
const showCartPreview = ref(false)
const searchQuery    = ref('')
const sesion         = ref(null)

// ── Estado reserva activa en carrito ────────────────────────────────
const reservaActiva       = ref(false)
const noReservacionActiva = ref('')
const reservacionIdActiva = ref(null)
const tipoReservaActiva   = ref('')
const totalReservaActiva  = ref('')
let   verificandoReserva  = false

// ── Leer sesión desde sessionStorage ────────────────────────────────
function cargarSesion() {
  try {
    const raw = sessionStorage.getItem('usuario_sesion')
    if (!raw) { sesion.value = null; return }
    const parsed = JSON.parse(raw)
    parsed.isAdmin =
      parsed.isAdmin === true ||
      parsed.rol_id  === 2   ||
      parsed.rol     === 'Administrador'
    sesion.value = parsed
  } catch {
    sesion.value = null
  }
}

// Recargar sesión cada vez que cambia de ruta
watch(() => route.path, () => {
  cargarSesion()
  verificarReservaActiva()
}, { immediate: true })

function onStorage() { cargarSesion() }

// ── Nombre visible con fallbacks ─────────────────────────────────────
const nombreVisible = computed(() => {
  if (!sesion.value) return ''
  return sesion.value.nombre   ||
         sesion.value.username ||
         sesion.value.usuario  ||
         'Usuario'
})

const iniciales = computed(() => {
  const n = nombreVisible.value
  if (!n || n === 'Usuario') return '?'
  return n.slice(0, 2).toUpperCase()
})

// ── Verificar reserva activa leyendo solo sessionStorage ────────────
// No hace llamadas al backend — usa fechaExpiracion del checkout_data
function verificarReservaActiva() {
  if (verificandoReserva) return
  verificandoReserva = true

  try {
    const raw = sessionStorage.getItem('checkout_data')
    if (!raw) { limpiarEstadoCarrito(); return }

    const cd = JSON.parse(raw)
    if (!cd?.reservacionId) { limpiarEstadoCarrito(); return }

    // ── Leer fecha de expiración desde el detalle que devuelve el backend ──
    // Puede estar en detalleHotel.detalle.fechaExpiracion  (hotel / paquete)
    // o en detalleVuelo.detalle.minutosRestantes guardados como _reserva_expires_at
    let expiraEn = null

    // 1. Intentar desde detalleHotel (estructura del JSON que mostraste)
    const fechaHotelStr = cd.detalleHotel?.detalle?.fechaExpiracion
    if (fechaHotelStr) {
      expiraEn = new Date(fechaHotelStr.replace(' ', 'T')).getTime()
    }

    // 2. Intentar desde detalleVuelo
    if (!expiraEn) {
      const fechaVueloStr = cd.detalleVuelo?.detalle?.fechaExpiracion
      if (fechaVueloStr) {
        expiraEn = new Date(fechaVueloStr.replace(' ', 'T')).getTime()
      }
    }

    // 3. Fallback: timestamp guardado por el timer de reserva
    if (!expiraEn) {
      const ts = sessionStorage.getItem('_reserva_expires_at')
      if (ts) expiraEn = Number(ts)
    }

    // Si ya expiró → limpiar
    if (expiraEn && Date.now() > expiraEn) {
      limpiarSesionReserva()
      limpiarEstadoCarrito()
      return
    }

    // ── Reserva válida → activar carrito ──
    reservaActiva.value       = true
    reservacionIdActiva.value = cd.reservacionId
    noReservacionActiva.value = cd.noReservacion || ''

    const tipo = cd.tipoItem || ''
    if      (tipo === 'vuelo')   tipoReservaActiva.value = 'Vuelo'
    else if (tipo === 'hotel')   tipoReservaActiva.value = 'Hospedaje'
    else if (tipo === 'paquete') tipoReservaActiva.value = 'Paquete completo'
    else                         tipoReservaActiva.value = ''

    const tv = cd.detalleVuelo?.total_con_ganancia ?? 0
    const th = cd.detalleHotel?.total_con_ganancia ?? 0
    let total = 0
    if      (tipo === 'vuelo')   total = tv
    else if (tipo === 'hotel')   total = th
    else if (tipo === 'paquete') total = tv + th
    totalReservaActiva.value = total > 0 ? `$${total.toFixed(2)}` : ''

  } catch {
    // JSON corrupto u otro error: dejar el carrito como estaba
  } finally {
    verificandoReserva = false
  }
}

function limpiarEstadoCarrito() {
  reservaActiva.value       = false
  reservacionIdActiva.value = null
  noReservacionActiva.value = ''
  tipoReservaActiva.value   = ''
  totalReservaActiva.value  = ''
}

function limpiarSesionReserva() {
  sessionStorage.removeItem('checkout_data')
  sessionStorage.removeItem('_reserva_expires_at')
  sessionStorage.removeItem('_reserva_id')
  sessionStorage.removeItem('_reserva_no')
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
}

// ── Clic en carrito ──────────────────────────────────────────────────
function handleCartClick() {
  showCartPreview.value = false
  showMobileMenu.value  = false
  router.push('/checkout')
}

// ── Descartar reserva manualmente ───────────────────────────────────
function descartarReserva() {
  showCartPreview.value = false
  limpiarSesionReserva()
  limpiarEstadoCarrito()
}

// ── Cerrar sesión ────────────────────────────────────────────────────
function cerrarSesion() {
  sessionStorage.removeItem('usuario_sesion')
  sesion.value         = null
  showUserMenu.value   = false
  showMobileMenu.value = false
  limpiarEstadoCarrito()
  router.push('/principal')
}

// ── Menús ────────────────────────────────────────────────────────────
const handleScroll     = () => { isScrolled.value = window.scrollY > 10 }
const toggleMobileMenu = () => { showMobileMenu.value = !showMobileMenu.value; showUserMenu.value = false }
const toggleUserMenu   = () => { showUserMenu.value = !showUserMenu.value }
const closeMenus       = () => { showMobileMenu.value = false; showUserMenu.value = false }

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push('/principal')
    searchQuery.value    = ''
    showMobileMenu.value = false
  }
}

// Escuchar cambios en sessionStorage desde otras pestañas o mismo tab
function onStorageChange(e) {
  cargarSesion()
  // Si cambia checkout_data, re-verificar
  if (!e || e.key === 'checkout_data' || e.key === '_reserva_id' || e.key === '_reserva_expires_at') {
    verificarReservaActiva()
  }
}

onMounted(() => {
  cargarSesion()
  verificarReservaActiva()
  window.addEventListener('scroll',  handleScroll)
  window.addEventListener('click',   closeMenus)
  window.addEventListener('storage', onStorageChange)
})

onUnmounted(() => {
  window.removeEventListener('scroll',  handleScroll)
  window.removeEventListener('click',   closeMenus)
  window.removeEventListener('storage', onStorageChange)
})
</script>