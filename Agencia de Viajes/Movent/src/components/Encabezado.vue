<template>
  <header class="header" :class="{ scrolled: isScrolled }">
    <div class="header-container">

      <!-- Logo de MOVENT, clickeable para volver al inicio -->
      <button class="logo" @click="$router.push('/principal')" aria-label="Ir al inicio" type="button">
        <img src="/movent.png" alt="Movent" class="logo-image" />
      </button>

      <!-- Navegación principal visible en desktop -->
      <nav class="desktop-nav">
        <router-link to="/principal"        class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
          Inicio
        </router-link>
        <router-link to="/informacion"       class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          Información
        </router-link>
        <router-link v-if="sesion" to="/mis-reservaciones" class="nav-link" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Mis Reservas
        </router-link>

        <!-- Enlace al panel de administración, solo visible para administradores -->
        <router-link v-if="sesion?.isAdmin" to="/admin/dashboard" class="nav-link nav-link--panel" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          Panel Admin
        </router-link>

        <!-- Enlace al panel WebService, solo visible para usuarios con rol WS que no sean admin -->
        <router-link v-if="sesion?.isWS && !sesion?.isAdmin" to="/admin/webservice" class="nav-link nav-link--ws" active-class="active">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
          Panel WS
        </router-link>
      </nav>

      <!-- Barra de búsqueda de destinos con autocompletado -->
      <div class="search-bar" ref="searchBarRef">
        <div class="search-input-wrapper">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            type="text"
            v-model="searchQuery"
            @input="onSearchInput"
            @focus="onSearchFocus"
            @keydown.enter.prevent="onSearchEnter"
            @keydown.esc="closeSearch"
            @keydown.arrow-down.prevent="moveSelection(1)"
            @keydown.arrow-up.prevent="moveSelection(-1)"
            placeholder="Busca un destino..."
            class="search-input"
            autocomplete="off"
            translate="no"
          />
          <button v-if="searchQuery" class="search-clear" @click="clearSearch" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="user-actions">
        <!-- Campana de notificaciones con badge de no leídas -->
        <div v-if="sesion" class="notif-bell-wrap">
          <button
            class="notif-bell-btn"
            :class="{ 'notif-bell-btn--active': notifNoLeidas > 0 }"
            @click="irANotificaciones"
            aria-label="Ver notificaciones"
            type="button"
          >
            <svg viewBox="0 0 24 24" fill="none" :stroke="notifNoLeidas > 0 ? '#FFCC00' : 'currentColor'" stroke-width="1.8" width="20" height="20">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="notifNoLeidas > 0" class="notif-bell-badge">
              {{ notifNoLeidas > 9 ? '9+' : notifNoLeidas }}
            </span>
          </button>
        </div>

        <!-- Botón de carrito con indicador de reserva pendiente de pago -->
        <div v-if="sesion" class="cart-wrap" ref="cartWrapRef">
          <button
            class="cart-btn"
            :class="{ 'cart-btn--active': reservaActiva, 'cart-btn--open': showCartDropdown }"
            @click.stop="toggleCartDropdown"
            aria-label="Ver reserva activa"
            type="button"
          >
            <svg viewBox="0 0 24 24" fill="none" :stroke="reservaActiva ? '#FFCC00' : 'currentColor'" stroke-width="1.8" width="20" height="20">
              <rect x="1" y="5" width="22" height="14" rx="2.5"/>
              <line x1="15.5" y1="5" x2="15.5" y2="19"/>
              <line x1="18" y1="9.5" x2="21" y2="9.5"/>
              <line x1="18" y1="12"  x2="21" y2="12"/>
              <line x1="18" y1="14.5" x2="21" y2="14.5"/>
              <path d="M5 10.5 C4 10.5 3.5 11 3.5 12 C3.5 13 4 13.5 5 13.5" stroke-linecap="round"/>
            </svg>
            <span v-if="reservaActiva" class="cart-badge">
              <svg viewBox="0 0 10 10" width="8" height="8"><circle cx="5" cy="5" r="5" fill="#D40511"/></svg>
            </span>
          </button>

          <!-- Dropdown del carrito con detalles de la reserva pendiente -->
          <Transition name="enc-drop">
            <div v-if="showCartDropdown" class="cart-dropdown" @click.stop>
              <!-- Estado vacío cuando no hay reserva pendiente -->
              <template v-if="!reservaActiva">
                <div class="cart-dropdown__empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,204,0,0.4)" stroke-width="1.2" width="40" height="40">
                    <rect x="1" y="5" width="22" height="14" rx="2.5"/><line x1="15.5" y1="5" x2="15.5" y2="19"/>
                    <line x1="18" y1="9.5" x2="21" y2="9.5"/><line x1="18" y1="12" x2="21" y2="12"/>
                  </svg>
                  <p class="cart-dropdown__empty-title">Sin reservas pendientes</p>
                  <p class="cart-dropdown__empty-sub">Busca un destino en la barra de arriba</p>
                  <button class="cart-dropdown__cta-btn" @click="$router.push('/principal'); closeCartDropdown()" type="button">Explorar viajes</button>
                </div>
              </template>
              <!-- Estado con reserva activa: muestra código, tipo y total -->
              <template v-else>
                <div class="cart-dropdown__head">
                  <div class="cart-dropdown__head-left">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="14" height="14">
                      <rect x="1" y="5" width="22" height="14" rx="2.5"/><line x1="15.5" y1="5" x2="15.5" y2="19"/>
                    </svg>
                    <span>Reserva pendiente de pago</span>
                  </div>
                  <button class="cart-dropdown__close-btn" @click="closeCartDropdown" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="12" height="12">
                      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
                <div class="cart-dropdown__info-block">
                  <div class="cart-dropdown__info-row">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="13" height="13"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
                    <span class="cart-dropdown__info-label">Código</span>
                    <span class="cart-dropdown__info-val cart-dropdown__info-val--mono">{{ noReservacionActiva || '—' }}</span>
                  </div>
                  <div v-if="tipoReservaActiva" class="cart-dropdown__info-row">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    <span class="cart-dropdown__info-label">Tipo</span>
                    <span class="cart-dropdown__info-val">{{ tipoReservaActiva }}</span>
                  </div>
                  <div v-if="totalReservaActiva" class="cart-dropdown__info-row cart-dropdown__info-row--total">
                    <svg viewBox="0 0 24 24" fill="none" stroke="#9a9089" stroke-width="2" width="13" height="13"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                    <span class="cart-dropdown__info-label">Total</span>
                    <strong class="cart-dropdown__info-total">{{ totalReservaActiva }}</strong>
                  </div>
                </div>
                <!-- Acciones principales y secundarias del carrito -->
                <template v-if="!cancelandoDesdeCart">
                  <div class="cart-dropdown__actions">
                    <button class="cart-dropdown__btn-pay" @click="handleCartClick" type="button">
                      <svg v-if="cartCtaIcon === 'form'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      <svg v-else-if="cartCtaIcon === 'seat'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M20.2 7.8l-7.7 7.7-4-4-5.7 5.7"/><path d="M15 7h6v6"/></svg>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                      {{ cartCtaLabel }}
                    </button>
                  </div>
                  <div class="cart-dropdown__secondary-actions">
                    <button class="cart-dropdown__btn-secondary" @click="$router.push('/mis-reservaciones'); closeCartDropdown()" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                      Mis reservaciones
                    </button>
                    <button class="cart-dropdown__btn-cancel-open" @click="cancelandoDesdeCart = true" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                      Cancelar reserva
                    </button>
                  </div>
                </template>
                <!-- Formulario de confirmación de cancelación desde el carrito -->
                <template v-else>
                  <div class="cart-dropdown__cancel-form">
                    <div class="cart-dropdown__cancel-head">
                      <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="2" width="16" height="16"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                      <div>
                        <p class="cart-dropdown__cancel-title">Cancelar reserva</p>
                        <p class="cart-dropdown__cancel-sub">Esta acción es irreversible.</p>
                      </div>
                    </div>
                    <textarea class="cart-dropdown__cancel-textarea" v-model="cancelMotivoCart" placeholder="Motivo de cancelación (requerido)..." rows="2"></textarea>
                    <p v-if="cancelErrorCart" class="cart-dropdown__cancel-error">{{ cancelErrorCart }}</p>
                    <div class="cart-dropdown__cancel-btns">
                      <button class="cart-dropdown__btn-back" @click="cancelandoDesdeCart=false; cancelMotivoCart=''; cancelErrorCart=''" :disabled="cancelLoadingCart" type="button">Volver</button>
                      <button class="cart-dropdown__btn-confirm-cancel" @click="cancelarDesdeCarrito" :disabled="cancelLoadingCart" type="button">
                        <span v-if="cancelLoadingCart" class="cart-spin"></span>
                        <template v-else>Confirmar</template>
                      </button>
                    </div>
                  </div>
                </template>
              </template>
            </div>
          </Transition>
        </div>

        <!-- Sección de usuario autenticado con chip y dropdown de opciones -->
        <template v-if="sesion">
          <div v-if="showUserMenu" class="user-dropdown-overlay" @click="showUserMenu = false"></div>
          <div class="user-chip" @click.stop="toggleUserMenu">
            <div class="user-chip__avatar">{{ iniciales }}</div>
            <span class="user-chip__nombre">{{ nombreVisible }}</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
          </div>

          <!-- Dropdown de usuario con accesos rápidos según el rol -->
          <div v-if="showUserMenu" class="user-dropdown" @click.stop>
            <div class="user-dropdown__head">
              <div class="user-dropdown__avatar">{{ iniciales }}</div>
              <div>
                <p class="user-dropdown__nombre">{{ nombreVisible }}</p>
                <span class="user-dropdown__rol"
                  :class="{
                    'user-dropdown__rol--admin': sesion.isAdmin,
                    'user-dropdown__rol--ws':    sesion.isWS && !sesion.isAdmin
                  }">
                  {{ sesion.isAdmin ? 'Administrador' : sesion.isWS ? 'WebService' : 'Cliente' }}
                </span>
              </div>
            </div>
            <div class="user-dropdown__divider"></div>

            <!-- Links exclusivos para el rol Administrador -->
            <template v-if="sesion.isAdmin">
              <router-link to="/admin/dashboard"   class="user-dropdown__item user-dropdown__item--admin" @click="showUserMenu=false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>Dashboard
              </router-link>
              <router-link to="/admin/roles"        class="user-dropdown__item user-dropdown__item--admin" @click="showUserMenu=false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>Roles
              </router-link>
              <router-link to="/admin/proveedores"  class="user-dropdown__item user-dropdown__item--admin" @click="showUserMenu=false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>Proveedores
              </router-link>
              <router-link to="/admin/paquetes"     class="user-dropdown__item user-dropdown__item--admin" @click="showUserMenu=false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>Finanzas
              </router-link>
              <div class="user-dropdown__divider"></div>
            </template>

            <!-- Links exclusivos para el rol WebService (rol 3, no admin) -->
            <template v-if="sesion.isWS && !sesion.isAdmin">
              <router-link to="/admin/webservice" class="user-dropdown__item user-dropdown__item--ws" @click="showUserMenu=false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>Panel WebService
              </router-link>
              <div class="user-dropdown__divider"></div>
            </template>

            <!-- Links disponibles para todos los usuarios autenticados -->
            <router-link to="/mis-reservaciones" class="user-dropdown__item" @click="showUserMenu=false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>Mis reservaciones
            </router-link>
            <router-link to="/perfil" class="user-dropdown__item" @click="showUserMenu=false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>Mi perfil
            </router-link>
            <div class="user-dropdown__divider"></div>
            <button class="user-dropdown__item user-dropdown__item--logout" @click="cerrarSesion" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>Cerrar sesión
            </button>
          </div>
        </template>
        <!-- Botones para usuarios no autenticados -->
        <template v-else>
          <router-link to="/ingreso"  class="btn-secondary">Iniciar Sesión</router-link>
          <router-link to="/registro" class="btn-primary">Registrarse</router-link>
        </template>

        <!-- Botón hamburguesa para abrir el menú móvil -->
        <button class="mobile-menu-toggle" @click.stop="toggleMobileMenu" aria-label="Abrir menú" type="button">
          <svg v-if="showMobileMenu" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
      </div>
    </div>

    <!-- Dropdown de autocompletado con 3 pills por sugerencia -->
    <div v-if="searchSuggestions.length > 0 && !miniFormMode" class="search-ac-dropdown" translate="no">
      <div class="search-ac-inner">
        <div v-for="(sug, i) in searchSuggestions" :key="`${sug.pais}-${sug.ciudad}`"
          :class="['search-ac-item', { 'search-ac-item--sel': i === selectedIdx }]">
          <div class="search-ac-item__left" @mousedown.prevent="irA(sug, 'hoteles')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            <span class="search-ac-item__ciudad">{{ sug.ciudad }}</span>
            <span class="search-ac-item__pais">{{ sug.pais }}</span>
          </div>
          <div class="search-ac-item__pills">
            <!-- Vuelo: abre mini-form para elegir origen → destino -->
            <span :class="['search-ac-pill','search-ac-pill--vuelo', { 'search-ac-pill--loading': pillLoading === sug.ciudad+'-vuelos' }]"
              @mousedown.prevent.stop="openMiniForm(sug, 'vuelo')">
              <svg viewBox="0 0 24 24" fill="currentColor" width="10" height="10"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelo
            </span>
            <!-- Hotel: búsqueda directa en esa ciudad -->
            <span :class="['search-ac-pill','search-ac-pill--hotel', { 'search-ac-pill--loading': pillLoading === sug.ciudad+'-hoteles' }]"
              @mousedown.prevent.stop="irA(sug, 'hoteles')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
              Hotel
            </span>
            <!-- Paquete: abre mini-form para elegir origen → destino -->
            <span :class="['search-ac-pill','search-ac-pill--paquete', { 'search-ac-pill--loading': pillLoading === sug.ciudad+'-paquete' }]"
              @mousedown.prevent.stop="openMiniForm(sug, 'paquete')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Paquete
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Mini-form de origen→destino para vuelos y paquetes (4 campos: país+ciudad × 2) -->
    <div v-if="miniFormMode" class="search-ac-dropdown search-mini-form" translate="no">
      <div class="search-ac-inner smf-inner">
        <div class="smf-header">
          <div class="smf-title">
            <svg v-if="miniFormMode === 'vuelo'" viewBox="0 0 24 24" fill="currentColor" width="14" height="14"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            {{ miniFormMode === 'vuelo' ? 'Buscar vuelos' : 'Buscar paquetes' }}
          </div>
          <button class="smf-close" @mousedown.prevent="miniFormMode = ''" type="button">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <div class="smf-fields">
          <!-- ── ORIGEN ── -->
          <div class="smf-group">
            <span class="smf-group-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              Desde
            </span>
            <div class="smf-group-inputs">
              <!-- País origen -->
              <div class="smf-ac-wrap">
                <input class="smf-input" v-model="miniOPaisQ" @input="onMiniOPaisInput"
                  @focus="miniActive = 'opais'"
                  placeholder="País origen..." autocomplete="off" translate="no" />
                <div v-if="miniOPaisSug.length > 0 && miniActive === 'opais'" class="smf-suggestions" translate="no">
                  <div v-for="p in miniOPaisSug" :key="p.country" class="smf-suggestion"
                    @mousedown.prevent="selMiniOPais(p)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                    {{ p.country }}
                  </div>
                </div>
              </div>
              <!-- Ciudad origen -->
              <div class="smf-ac-wrap">
                <input class="smf-input" v-model="miniOCiudadQ" @input="onMiniOCiudadInput"
                  @focus="miniActive = 'ociudad'"
                  :placeholder="miniOCiudadLoading ? 'Cargando ciudades...' : 'Ciudad origen...'"
                  :disabled="!miniOPaisSel || miniOCiudadLoading"
                  autocomplete="off" translate="no" />
                <div v-if="miniOCiudadSug.length > 0 && miniActive === 'ociudad'" class="smf-suggestions" translate="no">
                  <div v-for="c in miniOCiudadSug" :key="`oc-${c}`" class="smf-suggestion"
                    @mousedown.prevent="selMiniOCiudad(c)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    {{ c }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Flecha -->
          <div class="smf-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="20" height="20">
              <line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/>
            </svg>
          </div>

          <!-- ── DESTINO ── -->
          <div class="smf-group">
            <span class="smf-group-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              Hasta
            </span>
            <div class="smf-group-inputs">
              <!-- País destino -->
              <div class="smf-ac-wrap">
                <input class="smf-input" v-model="miniDPaisQ" @input="onMiniDPaisInput"
                  @focus="miniActive = 'dpais'"
                  placeholder="País destino..." autocomplete="off" translate="no" />
                <div v-if="miniDPaisSug.length > 0 && miniActive === 'dpais'" class="smf-suggestions" translate="no">
                  <div v-for="p in miniDPaisSug" :key="p.country" class="smf-suggestion"
                    @mousedown.prevent="selMiniDPais(p)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                    {{ p.country }}
                  </div>
                </div>
              </div>
              <!-- Ciudad destino -->
              <div class="smf-ac-wrap">
                <input class="smf-input" v-model="miniDCiudadQ" @input="onMiniDCiudadInput"
                  @focus="miniActive = 'dciudad'"
                  :placeholder="miniDCiudadLoading ? 'Cargando ciudades...' : 'Ciudad destino...'"
                  :disabled="!miniDPaisSel || miniDCiudadLoading"
                  autocomplete="off" translate="no" />
                <div v-if="miniDCiudadSug.length > 0 && miniActive === 'dciudad'" class="smf-suggestions" translate="no">
                  <div v-for="c in miniDCiudadSug" :key="`dc-${c}`" class="smf-suggestion"
                    @mousedown.prevent="selMiniDCiudad(c)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    {{ c }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Botón buscar -->
          <button class="smf-btn" @mousedown.prevent="doMiniSearch" type="button"
            :disabled="!!pillLoading || !miniOrigen.ciudad || !miniDestino.ciudad">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
            Buscar
          </button>
        </div>
      </div>
    </div>

    <!-- Overlay de carga global durante la búsqueda y navegación a resultados -->
    <Teleport to="body">
      <div v-if="pillLoading" class="loading-overlay">
        <div class="loading-overlay__card">
          <div class="loading-overlay__ring"></div>
          <span class="loading-overlay__text">Buscando disponibilidad...</span>
          <span class="loading-overlay__sub">Un momento por favor</span>
        </div>
      </div>
    </Teleport>

    <!-- Menú de navegación móvil desplegable -->
    <nav v-if="showMobileMenu" class="mobile-nav">
      <form class="mobile-search" @submit.prevent>
        <div class="search-input-wrapper">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input type="text" v-model="searchQuery" @input="onSearchInput" placeholder="Busca un destino..." class="search-input" />
        </div>
      </form>
      <div class="mobile-nav-links">
        <router-link to="/principal"        class="mobile-nav-link" @click="showMobileMenu=false">Inicio</router-link>
        <router-link to="/informacion"       class="mobile-nav-link" @click="showMobileMenu=false">Información</router-link>
        <router-link v-if="sesion" to="/mis-reservaciones" class="mobile-nav-link" @click="showMobileMenu=false">Mis Reservas</router-link>
        <router-link v-if="sesion" to="/notificaciones" class="mobile-nav-link" @click="showMobileMenu=false">
          Notificaciones
          <span v-if="notifNoLeidas > 0" class="notif-filtro__n">{{ notifNoLeidas }}</span>
        </router-link>

        <!-- Acceso directo al checkout si hay reserva activa -->
        <button v-if="reservaActiva" class="mobile-nav-link mobile-nav-link--pagar" @click="handleCartClick; showMobileMenu=false" type="button">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="1" y="5" width="22" height="14" rx="2.5"/><line x1="15.5" y1="5" x2="15.5" y2="19"/></svg>
          Ir a pagar · {{ noReservacionActiva }}
        </button>

        <!-- Links del panel admin en móvil, solo para administradores -->
        <template v-if="sesion?.isAdmin">
          <div class="mobile-divider"></div>
          <router-link to="/admin/dashboard"   class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu=false">Panel Admin</router-link>
          <router-link to="/admin/roles"        class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu=false">Roles</router-link>
          <router-link to="/admin/proveedores"  class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu=false">Proveedores</router-link>
          <router-link to="/admin/paquetes"     class="mobile-nav-link mobile-nav-link--admin" @click="showMobileMenu=false">Finanzas</router-link>
        </template>

        <!-- Link al panel WS en móvil, solo para rol WebService -->
        <template v-if="sesion?.isWS && !sesion?.isAdmin">
          <div class="mobile-divider"></div>
          <router-link to="/admin/webservice" class="mobile-nav-link mobile-nav-link--ws" @click="showMobileMenu=false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            Panel WebService
          </router-link>
        </template>

        <div class="mobile-divider"></div>
        <template v-if="sesion">
          <router-link to="/mis-reservaciones" class="mobile-nav-link" @click="showMobileMenu=false">Mis Reservaciones</router-link>
          <router-link to="/perfil" class="mobile-nav-link" @click="showMobileMenu=false">Mi Perfil</router-link>
          <button class="mobile-nav-link" @click="cerrarSesion" type="button">Cerrar sesión ({{ nombreVisible }})</button>
        </template>
        <template v-else>
          <router-link to="/ingreso"  class="mobile-nav-link" @click="showMobileMenu=false">Iniciar Sesión</router-link>
          <router-link to="/registro" class="mobile-nav-link primary" @click="showMobileMenu=false">Registrarse</router-link>
        </template>
      </div>
    </nav>
  </header>
</template>

<script setup>
/**
 * @file Encabezado.vue
 * @description Componente de encabezado global de la aplicación MOVENT. Incluye
 * navegación principal, barra de búsqueda de destinos con autocompletado usando
 * la API de CountriesNow, carrito de reserva activa con opción de cancelación,
 * menú de usuario con accesos según rol y menú móvil responsive.
 */
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import '../styles/encabezado.css'
import '../styles/notificaciones.css'

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Instancia del router para navegación programática. */
const router = useRouter()

/** Ruta activa actual, usada para reaccionar a cambios de página. */
const route  = useRoute()

/**
 * Devuelve la fecha de hoy en formato ISO (YYYY-MM-DD).
 * @returns {string}
 */
function fechaHoy()     { return new Date().toISOString().split('T')[0] }

/**
 * Devuelve la fecha de hoy más un mes en formato ISO.
 * Se usa como fecha de checkout por defecto en búsquedas rápidas.
 * @returns {string}
 */
function fechaEnUnMes() { const d = new Date(); d.setMonth(d.getMonth() + 1); return d.toISOString().split('T')[0] }

/** Indica si el usuario hizo scroll hacia abajo (para aplicar estilos al header). @type {import('vue').Ref<boolean>} */
const isScrolled       = ref(false)

/** Controla la visibilidad del menú de navegación móvil. @type {import('vue').Ref<boolean>} */
const showMobileMenu   = ref(false)

/** Controla la visibilidad del dropdown de usuario. @type {import('vue').Ref<boolean>} */
const showUserMenu     = ref(false)

/** Controla la visibilidad del dropdown del carrito. @type {import('vue').Ref<boolean>} */
const showCartDropdown = ref(false)

/** Datos de la sesión actual del usuario o null si no está autenticado. @type {import('vue').Ref<Object|null>} */
const sesion           = ref(null)

/** Ref al contenedor del carrito para detectar clics fuera de él. @type {import('vue').Ref<HTMLElement|null>} */
const cartWrapRef      = ref(null)

/** Ref al contenedor de la barra de búsqueda. @type {import('vue').Ref<HTMLElement|null>} */
const searchBarRef     = ref(null)

/** Modo de búsqueda activo: 'vuelo' | 'hotel' | 'paquete'. @type {import('vue').Ref<string>} */
const searchMode        = ref('vuelo')

/** Texto actualmente escrito en la barra de búsqueda. @type {import('vue').Ref<string>} */
const searchQuery       = ref('')

/** Sugerencias devueltas por CountriesNow para el autocomplete. @type {import('vue').Ref<Array>} */
const searchSuggestions = ref([])

/** Índice de la sugerencia actualmente resaltada con el teclado. @type {import('vue').Ref<number>} */
const selectedIdx       = ref(-1)

/**
 * Clave de la pill que está en proceso de carga.
 * Formato: 'Ciudad-tipo'. Vacío si no hay carga activa.
 * @type {import('vue').Ref<string>}
 */
const pillLoading       = ref('')

/** Timer del debounce de búsqueda para no disparar peticiones en cada tecla. */
let searchDebounce = null

/** Cache de países de CountriesNow para no volver a pedirlos. */
let paisesCache    = null

// ── Mini-form (4 campos: país+ciudad origen → país+ciudad destino) ────────────
/** Modo activo del mini-form: '' | 'vuelo' | 'paquete'. */
const miniFormMode       = ref('')

// Origen
const miniOPaisQ         = ref('');  const miniOPaisSug       = ref([]);  const miniOPaisSel       = ref(null)
const miniOCiudadQ       = ref('');  const miniOCiudadSug     = ref([]);  const miniOCiudades      = ref([])
const miniOCiudadLoading = ref(false)
const miniOrigen         = ref({ pais: '', ciudad: '' })

// Destino
const miniDPaisQ         = ref('');  const miniDPaisSug       = ref([]);  const miniDPaisSel       = ref(null)
const miniDCiudadQ       = ref('');  const miniDCiudadSug     = ref([]);  const miniDCiudades      = ref([])
const miniDCiudadLoading = ref(false)
const miniDestino        = ref({ pais: '', ciudad: '' })

/** Qué campo del mini-form tiene el foco activo: 'opais'|'ociudad'|'dpais'|'dciudad'|''. */
const miniActive         = ref('')

/** Cambia el modo activo de búsqueda y limpia sugerencias. */
function setSearchMode(mode) {
  searchMode.value = mode
  searchSuggestions.value = []
}

/** Indica si hay una reservación pendiente de pago en sesión. @type {import('vue').Ref<boolean>} */
const reservaActiva       = ref(false)

/** Número de reservación activa (ej. MOV-12345). @type {import('vue').Ref<string>} */
const noReservacionActiva = ref('')

/** ID interno de la reservación activa. @type {import('vue').Ref<number|null>} */
const reservacionIdActiva = ref(null)

/** Tipo de la reservación activa en texto (Vuelo, Hospedaje, Paquete completo). @type {import('vue').Ref<string>} */
const tipoReservaActiva   = ref('')

/** Total formateado de la reservación activa (ej. '$250.00'). @type {import('vue').Ref<string>} */
const totalReservaActiva  = ref('')

/** Flag para evitar doble ejecución de verificarReservaActiva. */
let   verificandoReserva  = false

/** Muestra el formulario de confirmación de cancelación dentro del carrito. @type {import('vue').Ref<boolean>} */
const cancelandoDesdeCart = ref(false)

/** Motivo ingresado por el usuario para cancelar la reservación desde el carrito. @type {import('vue').Ref<string>} */
const cancelMotivoCart    = ref('')

/** Indica si la petición de cancelación está en proceso. @type {import('vue').Ref<boolean>} */
const cancelLoadingCart   = ref(false)

/** Error de cancelación mostrado dentro del formulario del carrito. @type {import('vue').Ref<string>} */
const cancelErrorCart     = ref('')

/** Cantidad de notificaciones no leídas del usuario. @type {import('vue').Ref<number>} */
const notifNoLeidas       = ref(0)

/** Timer del polling de notificaciones cada 60 segundos. */
let   notifPollTimer      = null

/**
 * Lee y parsea la sesión guardada en sessionStorage.
 * Determina si el usuario es admin (rolId 2) o WebService (rolId 3).
 */
function cargarSesion() {
  try {
    const raw = sessionStorage.getItem('usuario_sesion')
    if (!raw) { sesion.value = null; return }
    const p = JSON.parse(raw)
    p.isAdmin = p.isAdmin === true || p.rol_id === 2 || p.rol === 'Administrador'
    p.isWS    = p.isWS    === true || p.rol_id === 3 || p.rol === 'WebService'
    sesion.value = p
  } catch { sesion.value = null }
}

/**
 * Consulta el backend para obtener la cantidad de notificaciones no leídas
 * del usuario autenticado. Si no hay sesión, deja el contador en 0.
 */
async function cargarNotifNoLeidas() {
  if (!sesion.value) { notifNoLeidas.value = 0; return }
  try {
    const res = await fetch(`${API}/api/notificaciones`, { credentials: 'include' })
    if (!res.ok) { notifNoLeidas.value = 0; return }
    const data = await res.json()
    const lista = data.notificaciones || []
    notifNoLeidas.value = lista.filter(n => !n.leido).length
  } catch {
    notifNoLeidas.value = 0
  }
}

/**
 * Cierra cualquier dropdown abierto y navega a la vista completa de notificaciones.
 */
function irANotificaciones() {
  showMobileMenu.value = false
  showUserMenu.value   = false
  closeCartDropdown()
  router.push('/notificaciones')
}

/**
 * Observa cambios de ruta para recargar la sesión y verificar si hay reserva activa.
 * En la ruta '/confirmacion' solo limpia el UI del carrito.
 */
watch(() => route.path, (path) => {
  cargarSesion()
  if (path === '/confirmacion') {
    // Solo limpiar la UI del carrito — checkout_data lo lee y borra Confirmacion.vue
    limpiarEstadoCarrito()
  } else {
    verificarReservaActiva()
  }
  cargarNotifNoLeidas()
}, { immediate: true })

/**
 * Nombre visible del usuario para el chip del header.
 * Prioriza nombre > username > usuario > 'Usuario'.
 * @type {import('vue').ComputedRef<string>}
 */
const nombreVisible = computed(() => sesion.value?.nombre || sesion.value?.username || sesion.value?.usuario || 'Usuario')

/**
 * Iniciales del usuario para el avatar, máximo 2 caracteres.
 * @type {import('vue').ComputedRef<string>}
 */
const iniciales     = computed(() => { const n = nombreVisible.value; return (!n || n === 'Usuario') ? '?' : n.slice(0,2).toUpperCase() })

/**
 * Determina el texto e ícono del botón CTA del carrito según el estado del flujo.
 * - Sin pasajero       → "Completar datos"
 * - Vuelo/paquete + timer → "Seleccionar asientos"
 * - Resto              → "Ir a pagar"
 */
const cartCtaLabel = computed(() => {
  let cd = {}
  try { cd = JSON.parse(sessionStorage.getItem('checkout_data') || '{}') } catch {}
  if (!cd.pasajero) return 'Completar datos'
  const tipo = cd.tipoItem || ''
  if ((tipo === 'vuelo' || tipo === 'paquete') && sessionStorage.getItem('_reserva_expires_at')) return 'Seleccionar asientos'
  return 'Ir a pagar'
})
const cartCtaIcon = computed(() => {
  let cd = {}
  try { cd = JSON.parse(sessionStorage.getItem('checkout_data') || '{}') } catch {}
  if (!cd.pasajero) return 'form'
  const tipo = cd.tipoItem || ''
  if ((tipo === 'vuelo' || tipo === 'paquete') && sessionStorage.getItem('_reserva_expires_at')) return 'seat'
  return 'pay'
})

/**
 * Obtiene la lista de países y ciudades de CountriesNow.
 * Guarda el resultado en caché para no repetir la petición.
 * @returns {Promise<Array>}
 */
async function getPaises() {
  if (paisesCache) return paisesCache
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); paisesCache = d.data || [] }
  catch { paisesCache = [] }
  return paisesCache
}

/**
 * Busca ciudades que coincidan con el query en los datos de CountriesNow.
 * Puntúa cada resultado por ratio (queryLen/nameLen) × bonificación si empieza
 * con el query, de modo que "United States" aparece antes que "United Arab Emirates"
 * al escribir "united". Devuelve los 20 mejores.
 * @param {string} q - Texto de búsqueda.
 * @returns {Promise<Array<{ciudad: string, pais: string}>>}
 */
async function buscarCiudadesQ(q) {
  if (q.length < 2) return []
  const paises = await getPaises()
  const ql = q.toLowerCase(); const seen = new Set(); const scored = []
  for (const p of paises) {
    const nameLower = p.country.toLowerCase()
    if (nameLower.includes(ql) && p.cities?.length) {
      const k = `${p.cities[0]}-${p.country}`
      if (!seen.has(k)) {
        seen.add(k)
        const score = (ql.length / nameLower.length) * (nameLower.startsWith(ql) ? 1.5 : 1)
        scored.push({ ciudad: p.cities[0], pais: p.country, score })
      }
    }
    if (Array.isArray(p.cities)) {
      for (const city of p.cities) {
        const cityLower = city.toLowerCase()
        if (cityLower.includes(ql)) {
          const k = `${city}-${p.country}`
          if (!seen.has(k)) {
            seen.add(k)
            const score = (ql.length / cityLower.length) * (cityLower.startsWith(ql) ? 1.5 : 1)
            scored.push({ ciudad: city, pais: p.country, score })
          }
        }
      }
    }
  }
  scored.sort((a, b) => b.score - a.score)
  return scored.slice(0, 20).map(({ ciudad, pais }) => ({ ciudad, pais }))
}

/** Dispara la búsqueda de ciudades con debounce de 280ms al escribir en el input. */
function onSearchInput()  { selectedIdx.value = -1; clearTimeout(searchDebounce); const q = searchQuery.value.trim(); if (q.length < 2) { searchSuggestions.value = []; return }; searchDebounce = setTimeout(async () => { searchSuggestions.value = await buscarCiudadesQ(q) }, 280) }

/** Reactiva el autocomplete si ya había texto al recuperar el foco. */
function onSearchFocus()  { if (searchQuery.value.trim().length >= 2 && !searchSuggestions.value.length) onSearchInput() }

/** Navega a la primera sugerencia o a la seleccionada con el teclado al presionar Enter. */
function onSearchEnter()  { doSearch() }

/**
 * Busca con el modo activo y la sugerencia seleccionada (o la primera disponible).
 * @returns {Promise<void>}
 */
async function doSearch() {
  const idx = selectedIdx.value >= 0 ? selectedIdx.value : 0
  const sug = searchSuggestions.value[idx]
  if (sug) { irA(sug, searchMode.value); return }
  const q = searchQuery.value.trim()
  if (q.length < 2) return
  const sugs = await buscarCiudadesQ(q)
  if (sugs.length > 0) irA(sugs[0], searchMode.value)
}

/**
 * Devuelve la fecha de hoy más 3 meses en formato ISO.
 * @returns {string}
 */
function fechaEn3Meses() { const d = new Date(); d.setMonth(d.getMonth() + 3); return d.toISOString().split('T')[0] }

/**
 * Mueve la selección del teclado en el dropdown hacia arriba o abajo.
 * @param {1|-1} d - Dirección del movimiento.
 */
function moveSelection(d) { selectedIdx.value = Math.max(0, Math.min(searchSuggestions.value.length - 1, selectedIdx.value + d)) }

/** Limpia el texto del buscador y las sugerencias. */
function clearSearch()    { searchQuery.value = ''; searchSuggestions.value = [] }

/** Cierra el dropdown de sugerencias sin borrar el texto. */
function closeSearch()    { searchSuggestions.value = [] }

/**
 * Fecha de hoy + n días en formato ISO (YYYY-MM-DD).
 * @param {number} n
 * @returns {string}
 */
function fechaMas(n) { const d = new Date(); d.setDate(d.getDate() + n); return d.toISOString().split('T')[0] }

/**
 * Busca hoteles en 4 ventanas de 5 días (próximos 20 días) en paralelo y mezcla hasta 30 resultados únicos.
 * Así si hoy hay poca disponibilidad, los días siguientes compensan.
 * @param {string} ciudad
 * @param {string} pais
 * @returns {Promise<Array>}
 */
async function buscarHotelesMultiVentana(ciudad, pais) {
  const ventanas = [[0,5],[5,10],[10,15],[15,20]].map(([a,b]) => ({ checkIn: fechaMas(a), checkOut: fechaMas(b) }))
  const lotes = await Promise.all(ventanas.map(v =>
    fetch(`${API}/api/busqueda/hoteles`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ciudad, pais, fechaCheckIn: v.checkIn, fechaCheckOut: v.checkOut, cantidadPersonas: 1 })
    }).then(r => r.ok ? r.json() : []).catch(() => [])
  ))
  const seen = new Set(); const merged = []
  for (const lote of lotes) {
    const arr = Array.isArray(lote) ? lote : (lote?.resultados ?? lote?.hoteles ?? [])
    for (const h of arr) {
      const key = h.hotelId ?? h.id ?? h.nombre ?? h.name ?? JSON.stringify(h).slice(0, 80)
      if (!seen.has(key)) { seen.add(key); merged.push(h) }
      if (merged.length >= 30) return merged
    }
  }
  return merged
}

/**
 * Busca vuelos en 5 fechas (hoy, +5, +10, +15, +20 días) en paralelo y mezcla hasta 30 únicos.
 * @param {string} origen  @param {string} origenPais
 * @param {string} destino @param {string} destinoPais
 * @returns {Promise<Array>}
 */
async function buscarVuelosMultiFecha(origen, origenPais, destino, destinoPais) {
  const fechas = [0, 5, 10, 15, 20].map(fechaMas)
  const lotes = await Promise.all(fechas.map(fecha =>
    fetch(`${API}/api/busqueda/vuelos`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ origen, origenPais, destino, destinoPais, fecha, cantidadPasajeros: 1 })
    }).then(r => r.ok ? r.json() : []).catch(() => [])
  ))
  const seen = new Set(); const merged = []
  for (const lote of lotes) {
    const arr = Array.isArray(lote) ? lote : (lote?.resultados ?? lote?.vuelos ?? [])
    for (const v of arr) {
      const key = v.vueloId ?? v.id ?? v.vuelo_id ?? JSON.stringify(v).slice(0, 80)
      if (!seen.has(key)) { seen.add(key); merged.push(v) }
      if (merged.length >= 30) return merged
    }
  }
  return merged
}

/**
 * Navega a resultados según el tipo elegido.
 * - Hotel: sug es la ciudad/país de destino; busca en ventanas de 20 días.
 * - Vuelo / Paquete: pill no se usa directamente (abren mini-form), pero se mantiene por si acaso.
 * @param {{ciudad: string, pais: string}} sug
 * @param {'hoteles'|'hotel'} tipo
 */
async function irA(sug, tipo) {
  pillLoading.value = `${sug.ciudad}-${tipo}`
  closeSearch(); searchQuery.value = ''
  const hoy = fechaHoy(); const veinteDias = fechaMas(20)
  const ruta = '/resultados-hoteles'
  try {
    const resultados = await buscarHotelesMultiVentana(sug.ciudad, sug.pais)
    const state = { resultados, busqueda: { ciudad: sug.ciudad, pais: sug.pais, checkIn: hoy, checkOut: veinteDias, cantidadPersonas: 1 } }
    window.history.pushState(state, '', ruta); window.location.reload()
  } catch {
    window.history.pushState({ resultados: [], busqueda: { ciudad: sug.ciudad, pais: sug.pais, checkIn: hoy, checkOut: veinteDias, cantidadPersonas: 1 } }, '', ruta)
    window.location.reload()
  } finally { pillLoading.value = '' }
}

// ── Mini-form: funciones ─────────────────────────────────────────────────────

/**
 * Obtiene las ciudades de un país desde countriesnow.
 * @param {string} country - Nombre del país en inglés.
 * @returns {Promise<string[]>}
 */
async function getCiudades(country) {
  try {
    const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ country })
    })
    const d = await r.json(); return d.data || []
  } catch { return [] }
}

/**
 * Abre el mini-form prellenando la sugerencia clicada como origen.
 * Carga las ciudades del país de origen en background.
 * @param {{ciudad:string,pais:string}} sug
 * @param {'vuelo'|'paquete'} modo
 */
async function openMiniForm(sug, modo) {
  miniFormMode.value   = modo
  // Pre-fill origin
  miniOPaisQ.value     = sug.pais
  miniOPaisSel.value   = { country: sug.pais }
  miniOrigen.value     = { pais: sug.pais, ciudad: sug.ciudad }
  miniOCiudadQ.value   = sug.ciudad
  miniOPaisSug.value   = []; miniOCiudadSug.value = []
  // Reset destination
  miniDPaisQ.value     = ''; miniDPaisSel.value   = null
  miniDCiudadQ.value   = ''; miniDCiudades.value  = []
  miniDestino.value    = { pais: '', ciudad: '' }
  miniDPaisSug.value   = []; miniDCiudadSug.value = []
  miniActive.value     = 'dpais'
  closeSearch()
  // Load origin cities in background for the input to be functional if user changes it
  miniOCiudadLoading.value = true
  miniOCiudades.value = await getCiudades(sug.pais)
  miniOCiudadLoading.value = false
}

// ── Origen: país ──────────────────────────────────────────────────────────────
async function onMiniOPaisInput() {
  miniOPaisSel.value = null; miniOCiudadQ.value = ''; miniOCiudades.value = []
  miniOrigen.value = { pais: '', ciudad: '' }
  const q = miniOPaisQ.value.trim(); if (q.length < 2) { miniOPaisSug.value = []; return }
  miniOPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}

async function selMiniOPais(p) {
  miniOPaisSel.value = p; miniOPaisQ.value = p.country; miniOPaisSug.value = []
  miniOrigen.value.pais = p.country; miniOCiudadQ.value = ''; miniOrigen.value.ciudad = ''
  miniOCiudadLoading.value = true; miniOCiudades.value = await getCiudades(p.country); miniOCiudadLoading.value = false
  miniActive.value = 'ociudad'
}

// ── Origen: ciudad ────────────────────────────────────────────────────────────
function onMiniOCiudadInput() {
  const q = miniOCiudadQ.value.toLowerCase(); miniOrigen.value.ciudad = ''
  miniOCiudadSug.value = q.length < 2 ? [] : miniOCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function selMiniOCiudad(c) {
  miniOCiudadQ.value = c; miniOCiudadSug.value = []
  miniOrigen.value.ciudad = c; miniActive.value = 'dpais'
}

// ── Destino: país ─────────────────────────────────────────────────────────────
async function onMiniDPaisInput() {
  miniDPaisSel.value = null; miniDCiudadQ.value = ''; miniDCiudades.value = []
  miniDestino.value = { pais: '', ciudad: '' }
  const q = miniDPaisQ.value.trim(); if (q.length < 2) { miniDPaisSug.value = []; return }
  miniDPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}

async function selMiniDPais(p) {
  miniDPaisSel.value = p; miniDPaisQ.value = p.country; miniDPaisSug.value = []
  miniDestino.value.pais = p.country; miniDCiudadQ.value = ''; miniDestino.value.ciudad = ''
  miniDCiudadLoading.value = true; miniDCiudades.value = await getCiudades(p.country); miniDCiudadLoading.value = false
  miniActive.value = 'dciudad'
}

// ── Destino: ciudad ───────────────────────────────────────────────────────────
function onMiniDCiudadInput() {
  const q = miniDCiudadQ.value.toLowerCase(); miniDestino.value.ciudad = ''
  miniDCiudadSug.value = q.length < 2 ? [] : miniDCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
}

function selMiniDCiudad(c) {
  miniDCiudadQ.value = c; miniDCiudadSug.value = []
  miniDestino.value.ciudad = c; miniActive.value = ''
}

/** Dispara la búsqueda con los 4 campos completos. */
async function doMiniSearch() {
  if (!miniOrigen.value.pais || !miniOrigen.value.ciudad || !miniDestino.value.pais || !miniDestino.value.ciudad) return
  await irAConOrigen(
    { ciudad: miniOrigen.value.ciudad, pais: miniOrigen.value.pais },
    { ciudad: miniDestino.value.ciudad, pais: miniDestino.value.pais },
    miniFormMode.value
  )
}

/**
 * Realiza la búsqueda de vuelos o paquetes con origen y destino explícitos.
 * Para paquetes busca vuelos hacia el destino + hoteles en el destino.
 * @param {{ciudad:string,pais:string}} orig - Ciudad/país de origen.
 * @param {{ciudad:string,pais:string}} dest - Ciudad/país de destino.
 * @param {'vuelo'|'paquete'} tipo - Tipo de búsqueda.
 */
/**
 * Busca vuelos o paquetes con origen y destino explícitos.
 * Usa multi-fecha (próximos 20 días) para vuelos y multi-ventana para hoteles de paquete.
 * @param {{ciudad:string,pais:string}} orig
 * @param {{ciudad:string,pais:string}} dest
 * @param {'vuelo'|'paquete'} tipo
 */
async function irAConOrigen(orig, dest, tipo) {
  pillLoading.value  = `${orig.ciudad}-${tipo}`
  miniFormMode.value = ''
  closeSearch(); searchQuery.value = ''
  const hoy        = fechaHoy()
  const veinteDias = fechaMas(20)
  const esVuelo    = tipo === 'vuelo' || tipo === 'vuelos'
  const ruta       = esVuelo ? '/resultados-vuelos' : '/resultados-paquetes'
  try {
    let state = {}
    if (esVuelo) {
      // Multi-fecha: busca en hoy, +5, +10, +15, +20 días y mezcla hasta 30 resultados
      const resultados = await buscarVuelosMultiFecha(orig.ciudad, orig.pais, dest.ciudad, dest.pais)
      state = { resultados, busqueda: { origen: orig.ciudad, origenPais: orig.pais, destino: dest.ciudad, destinoPais: dest.pais, fecha: hoy, cantidadPasajeros: 1, tipoVuelo: 'ida' } }
    } else {
      // Paquete: multi-fecha vuelos + multi-ventana hoteles en paralelo
      const [resultadosVuelos, resultadosHoteles] = await Promise.all([
        buscarVuelosMultiFecha(orig.ciudad, orig.pais, dest.ciudad, dest.pais),
        buscarHotelesMultiVentana(dest.ciudad, dest.pais),
      ])
      state = { resultadosVuelos, resultadosRegreso: [], resultadosHoteles, busqueda: { origen: orig.ciudad, origenPais: orig.pais, destino: dest.ciudad, destinoPais: dest.pais, fecha: hoy, fechaRegreso: '', checkIn: hoy, checkOut: veinteDias, cantidadPersonas: 1, tipoVuelo: 'ida' } }
    }
    if (esVuelo) sessionStorage.setItem('_rv_busqueda', JSON.stringify(state.busqueda))
    window.history.pushState(state, '', ruta); window.location.reload()
  } catch {
    const sv = esVuelo
      ? { resultados: [], busqueda: { origen: orig.ciudad, origenPais: orig.pais, destino: dest.ciudad, destinoPais: dest.pais, fecha: hoy, cantidadPasajeros: 1, tipoVuelo: 'ida' } }
      : { resultadosVuelos: [], resultadosRegreso: [], resultadosHoteles: [], busqueda: { origen: orig.ciudad, origenPais: orig.pais, destino: dest.ciudad, destinoPais: dest.pais, fecha: hoy, checkIn: hoy, checkOut: veinteDias, cantidadPersonas: 1, tipoVuelo: 'ida' } }
    if (esVuelo) sessionStorage.setItem('_rv_busqueda', JSON.stringify(sv.busqueda))
    window.history.pushState(sv, '', ruta); window.location.reload()
  } finally { pillLoading.value = '' }
}

/**
 * Si sesion.value es null (sessionStorage vacío por cierre de browser) pero el
 * cookie JWT sigue vigente, restaura la sesión consultando /api/sesion.
 * Esto permite que v-if="sesion" se active y el carrito sea visible.
 */
async function _restaurarSesionDesdeServidor() {
  try {
    const r = await fetch(`${API}/api/sesion`, { credentials: 'include' })
    if (!r.ok) return
    const data = await r.json()
    const sesionData = {
      id:       data.usuario_id,
      username: data.username,
      rol_id:   data.rol_id,
      isAdmin:  data.rol_id === 2,
      isWS:     data.rol_id === 3,
    }
    sessionStorage.setItem('usuario_sesion', JSON.stringify(sesionData))
    sesion.value = sesionData
  } catch {}
}

/**
 * Lee checkout_data de sessionStorage y actualiza el estado del carrito.
 * Verifica que la reservación no haya expirado antes de marcarla como activa.
 * Si sessionStorage está vacío (nueva sesión, modo incógnito) intenta restaurar
 * la sesión desde el servidor antes de buscar la reservación pendiente.
 */
async function verificarReservaActiva() {
  if (verificandoReserva) return; verificandoReserva = true
  try {
    // Si no hay sesión local pero el cookie sigue vivo, restaurar antes de continuar
    if (!sesion.value) await _restaurarSesionDesdeServidor()

    const raw = sessionStorage.getItem('checkout_data')
    if (!raw) {
      await _cargarPendienteDeServidor()
      return
    }
    const cd = JSON.parse(raw)
    if (!cd?.reservacionId) {
      await _cargarPendienteDeServidor()
      return
    }
    let expiraEn = null
    const fh = cd.detalleHotel?.detalle?.fechaExpiracion; if (fh) expiraEn = new Date(fh.replace(' ','T')).getTime()
    if (!expiraEn) { const fv = cd.detalleVuelo?.detalle?.fechaExpiracion; if (fv) expiraEn = new Date(fv.replace(' ','T')).getTime() }
    if (!expiraEn) { const ts = sessionStorage.getItem('_reserva_expires_at'); if (ts) expiraEn = Number(ts) }
    if (expiraEn && Date.now() > expiraEn) { limpiarSesionReserva(); limpiarEstadoCarrito(); return }
    reservaActiva.value = true; reservacionIdActiva.value = cd.reservacionId; noReservacionActiva.value = cd.noReservacion || ''
    const tipo = cd.tipoItem || ''
    tipoReservaActiva.value  = tipo === 'vuelo' ? 'Vuelo' : tipo === 'hotel' ? 'Hospedaje' : tipo === 'paquete' ? 'Paquete completo' : ''
    const tv = cd.detalleVuelo?.total_con_ganancia ?? 0, th = cd.detalleHotel?.total_con_ganancia ?? 0
    let total = tipo === 'vuelo' ? tv : tipo === 'hotel' ? th : tipo === 'paquete' ? tv + th : 0
    if (tipo === 'paquete' && total > 0) {
      try {
        const rd = await fetch(`${API}/api/configuracion/descuento`, { credentials: 'include' })
        if (rd.ok) { const dd = await rd.json(); const pct = dd.porcentaje_descuento ?? 0; if (pct > 0) total = Math.round(total * (1 - pct / 100) * 100) / 100 }
      } catch {}
    }
    totalReservaActiva.value = total > 0 ? `$${total.toFixed(2)}` : ''
  } catch {} finally { verificandoReserva = false }
}

/**
 * Consulta el servidor por una reservación pendiente vigente del usuario y,
 * si existe, puebla sessionStorage y los refs del carrito con esos datos.
 * Se usa como fallback cuando sessionStorage no tiene checkout_data
 * (nueva pestaña, modo incógnito, reinicio del navegador).
 */
async function _cargarPendienteDeServidor() {
  try {
    const resp = await fetch(`${API}/api/reservaciones/mias`, { credentials: 'include' })
    if (!resp.ok) { limpiarEstadoCarrito(); return }
    const lista = await resp.json()
    // Buscar la primera reservación pendiente (estado_id=1).
    // No filtramos por fecha_expiracion porque ListarReservaciones tampoco lo hace:
    // si el backend aún no actualizó el estado, igual la mostramos igual que Mis Reservaciones.
    const pend = (lista || []).find(r => r.estado_id === 1)
    if (!pend) { limpiarEstadoCarrito(); return }
    const expiresMs = pend.fecha_expiracion ? new Date(pend.fecha_expiracion.replace(' ', 'T')).getTime() : 0
    const tipoItem = ({ 1: 'vuelo', 2: 'hotel', 3: 'paquete' })[pend.tipo_reserva] || ''
    // det.total es el precio con ganancia guardado en BD — no necesitamos llamar al proveedor
    let detalleVuelo = null, detalleHotel = null, totalVuelo = 0, totalHotel = 0
    for (const det of (pend.detalles || [])) {
      if (det.tipo_detalle_id === 1) { detalleVuelo = { total_con_ganancia: det.total ?? 0 }; totalVuelo = det.total ?? 0 }
      if (det.tipo_detalle_id === 2) { detalleHotel = { total_con_ganancia: det.total ?? 0 }; totalHotel = det.total ?? 0 }
    }
    // Solo persistir checkout_data (para mostrar el carrito en el encabezado).
    // NO escribir _reserva_id ni _reserva_expires_at: esas claves las usa Reserva.vue
    // para Priority 2 (retomar reserva). Si las escribimos aquí, la primera búsqueda
    // nueva del usuario entraría en conflicto con la reserva pendiente antigua.
    // _reserva_id/_reserva_expires_at solo se escriben cuando el usuario hace click en
    // "Completar datos" y Reserva.vue detecta la pendiente vía Priority 3.
    sessionStorage.setItem('checkout_data', JSON.stringify({ reservacionId: pend.id, noReservacion: pend.no_reservacion, tipoItem, detalleVuelo, detalleHotel }))
    // Actualizar refs del carrito
    reservaActiva.value       = true
    reservacionIdActiva.value = pend.id
    noReservacionActiva.value = pend.no_reservacion || ''
    tipoReservaActiva.value   = tipoItem === 'vuelo' ? 'Vuelo' : tipoItem === 'hotel' ? 'Hospedaje' : tipoItem === 'paquete' ? 'Paquete completo' : ''
    let total = tipoItem === 'vuelo' ? totalVuelo : tipoItem === 'hotel' ? totalHotel : tipoItem === 'paquete' ? totalVuelo + totalHotel : 0
    if (tipoItem === 'paquete' && total > 0) {
      try {
        const rd = await fetch(`${API}/api/configuracion/descuento`, { credentials: 'include' })
        if (rd.ok) { const dd = await rd.json(); const pct = dd.porcentaje_descuento ?? 0; if (pct > 0) total = Math.round(total * (1 - pct / 100) * 100) / 100 }
      } catch {}
    }
    totalReservaActiva.value  = total > 0 ? `$${total.toFixed(2)}` : ''
  } catch { limpiarEstadoCarrito() }
}

/** Resetea todos los refs del estado del carrito a sus valores iniciales. */
function limpiarEstadoCarrito() { reservaActiva.value = false; reservacionIdActiva.value = null; noReservacionActiva.value = ''; tipoReservaActiva.value = ''; totalReservaActiva.value = '' }

/** Elimina todos los datos de la reservación actual del sessionStorage. */
function limpiarSesionReserva() { ['checkout_data','_reserva_expires_at','_reserva_id','_reserva_no','vuelo_seleccionado','hotel_seleccionado','paquete_seleccionado'].forEach(k => sessionStorage.removeItem(k)) }

/** Alterna la visibilidad del dropdown del carrito, cerrando el de usuario si está abierto. */
function toggleCartDropdown()   { showCartDropdown.value = !showCartDropdown.value; showUserMenu.value = false; if (!showCartDropdown.value) resetCancelCart() }

/** Cierra el dropdown del carrito y resetea el formulario de cancelación. */
function closeCartDropdown()    { showCartDropdown.value = false; resetCancelCart() }

/** Limpia el estado del formulario de cancelación dentro del carrito. */
function resetCancelCart()      { cancelandoDesdeCart.value = false; cancelMotivoCart.value = ''; cancelErrorCart.value = '' }

/**
 * Cierra el carrito y navega al paso correcto del flujo de reserva según el estado de checkout_data.
 * - Sin pasajero llenado          → /reservar  (completar formulario)
 * - Vuelo/Paquete + timer activo  → /seleccion-asientos (elegir asientos)
 * - Hotel o flujo completo        → /checkout  (pagar)
 */
function handleCartClick() {
  closeCartDropdown(); showMobileMenu.value = false
  let cd = {}
  try { cd = JSON.parse(sessionStorage.getItem('checkout_data') || '{}') } catch {}

  // Sin datos de pasajero: debe completar el formulario primero
  if (!cd.pasajero) {
    router.push('/reservar'); return
  }

  // Vuelo o paquete: si el timer sigue activo, falta seleccionar asientos
  const tipo = cd.tipoItem || ''
  if ((tipo === 'vuelo' || tipo === 'paquete') && sessionStorage.getItem('_reserva_expires_at')) {
    router.push('/seleccion-asientos'); return
  }

  router.push('/checkout')
}

/**
 * Envía la solicitud de cancelación de la reservación activa desde el carrito.
 * Tras cancelar con éxito, limpia la sesión y redirige a Mis Reservaciones.
 * @returns {Promise<void>}
 */
async function cancelarDesdeCarrito() {
  if (!cancelMotivoCart.value.trim()) { cancelErrorCart.value = 'Escribe un motivo de cancelación.'; return }
  cancelLoadingCart.value = true; cancelErrorCart.value = ''
  try {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token') || ''
    const res = await fetch(`${API}/api/reservaciones/${reservacionIdActiva.value}/cancelar`, { method: 'POST', headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, credentials: 'include', body: JSON.stringify({ motivo: cancelMotivoCart.value.trim() }) })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) { cancelErrorCart.value = data.error ?? `Error ${res.status}.`; return }
    limpiarSesionReserva(); limpiarEstadoCarrito(); closeCartDropdown(); router.push('/mis-reservaciones')
  } catch { cancelErrorCart.value = 'Error de conexión. Intenta desde Mis Reservaciones.' }
  finally   { cancelLoadingCart.value = false }
}

/**
 * Cierra la sesión del usuario: llama al backend para limpiar la cookie HTTP
 * de sesión, luego limpia sessionStorage, resetea estados y redirige al inicio.
 * Es importante llamar al backend primero para que la cookie no persista en el
 * navegador después del logout, evitando que búsquedas anónimas posteriores
 * queden asociadas al usuario que acababa de cerrar sesión.
 */
async function cerrarSesion() {
  try {
    await fetch(`${API}/api/usuarios/logout`, { method: 'POST', credentials: 'include' })
  } catch { /* si el backend no responde igual se limpia el estado local */ }
  sessionStorage.removeItem('usuario_sesion')
  sesion.value = null
  showUserMenu.value = false
  showMobileMenu.value = false
  limpiarEstadoCarrito()
  closeCartDropdown()
  router.push('/principal')
}

/** Alterna la visibilidad del menú móvil, cerrando el dropdown de usuario. */
const toggleMobileMenu = () => { showMobileMenu.value = !showMobileMenu.value; showUserMenu.value = false }

/** Alterna la visibilidad del dropdown de usuario, cerrando el carrito. */
const toggleUserMenu   = () => { showUserMenu.value = !showUserMenu.value; closeCartDropdown() }

/**
 * Manejador global de clics para cerrar dropdowns al hacer clic fuera de ellos.
 * @param {MouseEvent} e - El evento de clic del documento.
 */
function handleGlobalClick(e) {
  if (showUserMenu.value) showUserMenu.value = false
  if (showCartDropdown.value && cartWrapRef.value && !cartWrapRef.value.contains(e.target)) closeCartDropdown()
  if (searchSuggestions.value.length > 0 || miniFormMode.value) {
    const header = e.target.closest('.header')
    if (!header) {
      closeSearch(); miniFormMode.value = ''
      miniOPaisSug.value = []; miniOCiudadSug.value = []
      miniDPaisSug.value = []; miniDCiudadSug.value = []
      miniActive.value = ''
    }
  }
  showMobileMenu.value = false
}

/** Actualiza isScrolled según la posición del scroll vertical. */
const handleScroll = () => { isScrolled.value = window.scrollY > 10 }

/**
 * Reacciona a cambios en el storage para sincronizar la sesión entre pestañas.
 * @param {StorageEvent} e - El evento de storage del navegador.
 */
function onStorageChange(e) { cargarSesion(); if (!e || e.key === 'checkout_data' || e.key === '_reserva_expires_at') verificarReservaActiva() }

/** Registra los listeners globales al montar el componente. */
onMounted(() => {
  cargarSesion()
  // En confirmacion no se verifica: checkout_data aún no fue borrado por Confirmacion.vue
  if (route.path !== '/confirmacion') verificarReservaActiva()
  cargarNotifNoLeidas()
  notifPollTimer = setInterval(cargarNotifNoLeidas, 60000)
  window.addEventListener('scroll',  handleScroll)
  window.addEventListener('click',   handleGlobalClick)
  window.addEventListener('storage', onStorageChange)
})

/** Elimina los listeners globales al desmontar para evitar memory leaks. */
onUnmounted(() => {
  if (notifPollTimer) clearInterval(notifPollTimer)
  window.removeEventListener('scroll',  handleScroll)
  window.removeEventListener('click',   handleGlobalClick)
  window.removeEventListener('storage', onStorageChange)
})
</script>