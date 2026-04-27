<template>
  <div class="page">
    <Encabezado />

    <!-- HERO: sección principal con estadísticas y buscador -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">Tu Próxima Aventura<br>Comienza <span>Aquí</span></h1>
          <p class="hero-subtitle">Vuelos, hospedajes y paquetes combinados de múltiples proveedores en un solo lugar</p>

          <!-- Estadísticas en tiempo real cargadas desde el backend -->
          <div class="hero-stats">
            <div class="hero-stat">
              <strong>{{ stats.aerolineas }}</strong>
              <span>Aerolínea{{ stats.aerolineas !== 1 ? 's' : '' }}</span>
            </div>
            <div class="hero-stat">
              <strong>{{ stats.hoteles }}</strong>
              <span>Hotel{{ stats.hoteles !== 1 ? 'es' : '' }}</span>
            </div>
            <div class="hero-stat">
              <strong>{{ stats.reservaciones }}</strong>
              <span>Reservacion{{ stats.reservaciones !== 1 ? 'es' : '' }}</span>
            </div>
            <div class="hero-stat">
              <strong>{{ stats.usuarios }}</strong>
              <span>Usuario{{ stats.usuarios !== 1 ? 's' : '' }}</span>
            </div>
          </div>
        </div>

        <!-- Tarjeta de búsqueda: vuelos, hoteles o paquete combinado -->
        <div class="search-card">
          <h2 class="search-card-title">¿A dónde viajamos?</h2>

          <!-- Tabs para seleccionar el tipo de búsqueda -->
          <div class="search-tabs">
            <button :class="{ active: searchType === 'flights' }" @click="searchType = 'flights'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelos
            </button>
            <button :class="{ active: searchType === 'hotels' }" @click="searchType = 'hotels'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Hoteles
            </button>
            <button :class="{ active: searchType === 'combo' }" @click="searchType = 'combo'" type="button">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Vuelo + Hotel
            </button>
          </div>

          <!-- Autocomplete de origen y destino (país + ciudad) -->
          <div :class="['vuelos-cards', { 'vuelos-cards--solo': searchType === 'hotels' }]">

            <!-- Panel de origen (oculto en modo hoteles) -->
            <div class="vuelo-card" v-if="searchType !== 'hotels'">
              <div class="vuelo-card__label">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                Desde
              </div>
              <div class="campo autocomplete-wrap">
                <label class="form-label">País</label>
                <input class="form-input campo-input" type="text" v-model="oPaisQ"
                  @input="onOPaisInput" @blur="blur(() => oPaisSug = [])"
                  placeholder="Guatemala..." autocomplete="off" />
                <ul v-if="oPaisSug.length" class="inline-autocomplete">
                  <li v-for="p in oPaisSug" :key="p.country">
                    <button type="button" @click="selOPais(p)">{{ p.country }}</button>
                  </li>
                </ul>
              </div>
              <div class="campo autocomplete-wrap">
                <label class="form-label">
                  Ciudad
                  <span v-if="oCiudadLoading" class="form-label__hint"> cargando...</span>
                </label>
                <input class="form-input campo-input" type="text" v-model="oCiudadQ"
                  @input="onOCiudadInput" @blur="blur(() => oCiudadSug = [])"
                  :disabled="!oPaisSel || oCiudadLoading"
                  placeholder="Guatemala City..." autocomplete="off" />
                <ul v-if="oCiudadSug.length" class="inline-autocomplete">
                  <li v-for="c in oCiudadSug" :key="c">
                    <button type="button" @click="selOCiudad(c)">{{ c }}</button>
                  </li>
                </ul>
              </div>
            </div>

            <!-- Panel de destino (siempre visible) -->
            <div :class="['vuelo-card', { 'vuelo-card--full': searchType === 'hotels' }]">
              <div class="vuelo-card__label">
                <template v-if="searchType === 'hotels'">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  Destino
                </template>
                <template v-else>
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M2.5,19H21.5V21H2.5V19M9.68,13.27L14.03,14.43L20.8,16.26C21.56,16.45 22,16.17 22,15.62C22,15.26 21.78,14.88 21.37,14.68L16.5,12.22L12.03,3H9.7L12,12.28L7.45,11L5.92,7.5H4.04L5.42,12C5.7,13 6.68,13.53 7.62,13.27"/></svg>
                  Hacia
                </template>
              </div>
              <div :class="searchType === 'hotels' ? 'destino-fila' : ''">
                <div class="campo autocomplete-wrap">
                  <label class="form-label">País</label>
                  <input class="form-input campo-input" type="text" v-model="dPaisQ"
                    @input="onDPaisInput" @blur="blur(() => dPaisSug = [])"
                    placeholder="Mexico..." autocomplete="off" />
                  <ul v-if="dPaisSug.length" class="inline-autocomplete">
                    <li v-for="p in dPaisSug" :key="p.country">
                      <button type="button" @click="selDPais(p)">{{ p.country }}</button>
                    </li>
                  </ul>
                </div>
                <div class="campo autocomplete-wrap">
                  <label class="form-label">
                    Ciudad
                    <span v-if="dCiudadLoading" class="form-label__hint"> cargando...</span>
                  </label>
                  <input class="form-input campo-input" type="text" v-model="dCiudadQ"
                    @input="onDCiudadInput" @blur="blur(() => dCiudadSug = [])"
                    :disabled="!dPaisSel || dCiudadLoading"
                    placeholder="Mexico City..." autocomplete="off" />
                  <ul v-if="dCiudadSug.length" class="inline-autocomplete">
                    <li v-for="c in dCiudadSug" :key="c">
                      <button type="button" @click="selDCiudad(c)">{{ c }}</button>
                    </li>
                  </ul>
                </div>
              </div>
            </div>

          </div>

          <!-- TAB VUELOS: fecha, pasajeros y tipo de viaje -->
          <template v-if="searchType === 'flights'">
            <div class="trip-type-toggle">
              <button :class="['trip-type-btn', { 'trip-type-btn--active': tipoVuelo === 'ida' }]"
                @click="tipoVuelo = 'ida'" type="button">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                Solo ida
              </button>
              <button :class="['trip-type-btn', { 'trip-type-btn--active': tipoVuelo === 'idaVuelta' }]"
                @click="tipoVuelo = 'idaVuelta'" type="button">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 1l4 4-4 4"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><path d="M7 23l-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
                Ida y vuelta
              </button>
            </div>

            <div class="form-grid" :style="tipoVuelo === 'idaVuelta' ? 'grid-template-columns: repeat(3, 1fr)' : 'grid-template-columns: repeat(2, 1fr)'">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha de ida
                </label>
                <input class="form-input" type="date" v-model="flightData.fecha" :min="hoy" />
              </div>
              <div class="form-group" v-if="tipoVuelo === 'idaVuelta'">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha de regreso
                </label>
                <input class="form-input" type="date" v-model="flightData.fechaRegreso" :min="minFechaRegreso" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                  Pasajeros
                </label>
                <select class="form-input" v-model="flightData.cantidadPasajeros">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Pasajero' : 'Pasajeros' }}</option>
                </select>
              </div>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarVuelos" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Vuelos' }}
            </button>
          </template>

          <!-- TAB HOTELES: check-in, check-out y cantidad de personas -->
          <template v-if="searchType === 'hotels'">
            <div class="form-grid" style="grid-template-columns: repeat(3, 1fr)">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-in
                </label>
                <input class="form-input" type="date" v-model="hotelData.checkIn" :min="hoy" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-out
                </label>
                <input class="form-input" type="date" v-model="hotelData.checkOut" :min="minCheckOutHotel" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Personas
                </label>
                <select class="form-input" v-model="hotelData.cantidadPersonas">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Persona' : 'Personas' }}</option>
                </select>
              </div>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarHoteles" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Hoteles' }}
            </button>
          </template>

          <!-- TAB COMBO: fechas de vuelo + check-in/out del hotel dentro del período del vuelo -->
          <template v-if="searchType === 'combo'">
            <div class="combo-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelo
            </div>
            <div class="trip-type-toggle" style="margin-top: 10px; margin-bottom: 10px;">
              <button :class="['trip-type-btn', { 'trip-type-btn--active': comboTipoVuelo === 'ida' }]"
                @click="comboTipoVuelo = 'ida'" type="button">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                Solo ida
              </button>
              <button :class="['trip-type-btn', { 'trip-type-btn--active': comboTipoVuelo === 'idaVuelta' }]"
                @click="comboTipoVuelo = 'idaVuelta'" type="button">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 1l4 4-4 4"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><path d="M7 23l-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
                Ida y vuelta
              </button>
            </div>
            <div class="form-grid" :style="comboTipoVuelo === 'idaVuelta' ? 'grid-template-columns: repeat(3, 1fr); margin-bottom:0; border-bottom-left-radius:0; border-bottom-right-radius:0; border-bottom: none;' : 'grid-template-columns: repeat(2, 1fr); margin-bottom:0; border-bottom-left-radius:0; border-bottom-right-radius:0; border-bottom: none;'">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha de ida
                </label>
                <input class="form-input" type="date" v-model="comboData.fecha" :min="hoy" />
              </div>
              <div class="form-group" v-if="comboTipoVuelo === 'idaVuelta'">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Fecha de regreso
                </label>
                <input class="form-input" type="date" v-model="comboData.fechaRegreso" :min="minFechaRegresoCombo" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Personas
                </label>
                <select class="form-input" v-model="comboData.cantidadPersonas">
                  <option v-for="n in 10" :key="n" :value="n">{{ n }}</option>
                </select>
              </div>
            </div>

            <!-- Sección hotel del combo con restricción de fechas dentro del vuelo -->
            <div class="combo-label" style="border-top: 1px solid #e2e8f0; margin-top:0; border-radius:0;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              Hotel
              <span v-if="comboData.fecha" style="font-size:11px; font-weight:400; color:#9a9089; margin-left:6px;">
                · Dentro del período del vuelo
                <template v-if="comboTipoVuelo === 'idaVuelta' && comboData.fechaRegreso">
                  ({{ formatFechaCorta(comboData.fecha) }} – {{ formatFechaCorta(comboData.fechaRegreso) }})
                </template>
                <template v-else-if="comboTipoVuelo === 'ida' && comboData.fecha">
                  (desde {{ formatFechaCorta(comboData.fecha) }})
                </template>
              </span>
              <span v-else style="font-size:11px; font-weight:400; color:#9a9089; margin-left:6px;">· Dentro del período del vuelo</span>
            </div>
            <div class="form-grid" style="grid-template-columns: repeat(2, 1fr); border-top-left-radius:0; border-top-right-radius:0; border-top: none; margin-bottom: 1.25rem;">
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-in
                </label>
                <input class="form-input" type="date" v-model="comboData.checkIn"
                  :min="comboData.fecha || hoy" :max="maxCheckInCombo || undefined" :disabled="!comboData.fecha" />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-out
                </label>
                <input class="form-input" type="date" v-model="comboData.checkOut"
                  :min="minCheckOutCombo"
                  :max="comboTipoVuelo === 'idaVuelta' ? (comboData.fechaRegreso || undefined) : undefined"
                  :disabled="!comboData.checkIn" />
              </div>
            </div>
            <p v-if="searchError" class="search-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {{ searchError }}
            </p>
            <button class="search-btn" type="button" @click="buscarPaquetes" :disabled="buscando">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
              {{ buscando ? 'Buscando...' : 'Buscar Paquete Completo' }}
            </button>
          </template>

        </div>
      </div>
    </section>

    <!-- SECCIÓN 1: Pasos del flujo de reserva — horizontal, compacto -->
    <section style="background:#0f0c0a; padding:44px 0 48px; position:relative; overflow:hidden;">
      <!-- Trail decorativo de vuelo en el fondo -->
      <svg aria-hidden="true" style="position:absolute;inset:0;width:100%;height:100%;pointer-events:none;" preserveAspectRatio="none" viewBox="0 0 1200 120">
        <path d="M-60,95 Q200,18 460,68 T940,25 T1260,58" fill="none" stroke="#FFCC00" stroke-width="1.5" stroke-dasharray="10 7" opacity="0.12"/>
        <path d="M-60,75 Q250,12 510,52 T990,18 T1260,45" fill="none" stroke="#FFCC00" stroke-width="1" stroke-dasharray="5 5" opacity="0.06"/>
      </svg>
      <div class="container">
        <div style="display:flex; align-items:center; gap:0; flex-wrap:wrap;">
          <!-- Paso 1 -->
          <div style="flex:1; min-width:160px; display:flex; align-items:flex-start; gap:14px; padding:20px 16px;">
            <div style="width:44px;height:44px;flex-shrink:0;background:#FFCC00;border-radius:50%;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 6px rgba(255,204,0,0.12);">
              <svg viewBox="0 0 24 24" fill="none" stroke="#0f0c0a" stroke-width="2.5" width="20" height="20"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
            </div>
            <div>
              <p style="font-size:0.68rem;font-weight:700;letter-spacing:0.08em;color:#FFCC00;text-transform:uppercase;margin:0 0 4px;">01</p>
              <h3 style="font-size:0.95rem;font-weight:700;color:#fff;margin:0 0 4px;">Busca</h3>
              <p style="font-size:0.8rem;color:rgba(255,255,255,0.45);margin:0;line-height:1.5;">Vuelo, hotel o paquete. Selecciona fechas y pasajeros.</p>
            </div>
          </div>
          <!-- Conector SVG con flecha -->
          <div style="flex-shrink:0;display:flex;align-items:center;padding:0 4px;">
            <svg width="40" height="16" viewBox="0 0 40 16" fill="none">
              <line x1="0" y1="8" x2="29" y2="8" stroke="rgba(255,204,0,0.35)" stroke-width="1.5" stroke-dasharray="3.5 3"/>
              <path d="M28 3L38 8L28 13" stroke="rgba(255,204,0,0.5)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <!-- Paso 2 -->
          <div style="flex:1; min-width:160px; display:flex; align-items:flex-start; gap:14px; padding:20px 16px;">
            <div style="width:44px;height:44px;flex-shrink:0;background:#FFCC00;border-radius:50%;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 6px rgba(255,204,0,0.12);">
              <svg viewBox="0 0 24 24" fill="none" stroke="#0f0c0a" stroke-width="2.5" width="20" height="20"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
            </div>
            <div>
              <p style="font-size:0.68rem;font-weight:700;letter-spacing:0.08em;color:#FFCC00;text-transform:uppercase;margin:0 0 4px;">02</p>
              <h3 style="font-size:0.95rem;font-weight:700;color:#fff;margin:0 0 4px;">Compara y reserva</h3>
              <p style="font-size:0.8rem;color:rgba(255,255,255,0.45);margin:0;line-height:1.5;">Múltiples proveedores, confirmación inmediata.</p>
            </div>
          </div>
          <!-- Conector SVG con flecha -->
          <div style="flex-shrink:0;display:flex;align-items:center;padding:0 4px;">
            <svg width="40" height="16" viewBox="0 0 40 16" fill="none">
              <line x1="0" y1="8" x2="29" y2="8" stroke="rgba(255,204,0,0.35)" stroke-width="1.5" stroke-dasharray="3.5 3"/>
              <path d="M28 3L38 8L28 13" stroke="rgba(255,204,0,0.5)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <!-- Paso 3 -->
          <div style="flex:1; min-width:160px; display:flex; align-items:flex-start; gap:14px; padding:20px 16px;">
            <div style="width:44px;height:44px;flex-shrink:0;background:#FFCC00;border-radius:50%;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 6px rgba(255,204,0,0.12);">
              <svg viewBox="0 0 24 24" fill="none" stroke="#0f0c0a" stroke-width="2.5" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
            </div>
            <div>
              <p style="font-size:0.68rem;font-weight:700;letter-spacing:0.08em;color:#FFCC00;text-transform:uppercase;margin:0 0 4px;">03</p>
              <h3 style="font-size:0.95rem;font-weight:700;color:#fff;margin:0 0 4px;">Viaja</h3>
              <p style="font-size:0.8rem;color:rgba(255,255,255,0.45);margin:0;line-height:1.5;">Historial completo en Mis Reservas. Sin papeleos.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Wave divider: dark (#0f0c0a) → light (#faf9f7) -->
    <div aria-hidden="true" style="background:#0f0c0a;line-height:0;margin-bottom:-1px;">
      <svg viewBox="0 0 1440 56" preserveAspectRatio="none" style="display:block;width:100%;height:56px;">
        <path d="M0,0 Q360,56 720,28 T1440,0 L1440,56 L0,56 Z" fill="#faf9f7"/>
      </svg>
    </div>

    <!-- SECCIÓN 2: Categorías de búsqueda — navegación rápida y compacta -->
    <section style="background:#faf9f7; padding:28px 0 32px;">
      <div class="container">
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:14px;">

          <!-- Vuelos -->
          <div class="cat-card" @click="irACategoria('flights')" role="button" tabindex="0" @keydown.enter="irACategoria('flights')">
            <div style="width:50px;height:50px;background:#fff8e1;border-radius:12px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="#8B6B4A"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
            </div>
            <div style="flex:1;min-width:0;">
              <h3 style="font-size:0.97rem;font-weight:800;color:#1C1A18;margin:0 0 3px;">Vuelos</h3>
              <p style="font-size:0.78rem;color:#64748b;margin:0;line-height:1.4;">Ida o ida y vuelta, múltiples destinos</p>
            </div>
          </div>

          <!-- Hoteles -->
          <div class="cat-card" @click="irACategoria('hotels')" role="button" tabindex="0" @keydown.enter="irACategoria('hotels')">
            <div style="width:50px;height:50px;background:#e8f4fd;border-radius:12px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="1.8"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            </div>
            <div style="flex:1;min-width:0;">
              <h3 style="font-size:0.97rem;font-weight:800;color:#1C1A18;margin:0 0 3px;">Hoteles</h3>
              <p style="font-size:0.78rem;color:#64748b;margin:0;line-height:1.4;">Por ciudad, fechas y número de personas</p>
            </div>
          </div>

          <!-- Paquetes -->
          <div class="cat-card" @click="irACategoria('combo')" role="button" tabindex="0" @keydown.enter="irACategoria('combo')">
            <div style="width:50px;height:50px;background:#f0fdf4;border-radius:12px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
              <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            </div>
            <div style="flex:1;min-width:0;">
              <h3 style="font-size:0.97rem;font-weight:800;color:#1C1A18;margin:0 0 3px;">Paquete Completo</h3>
              <p style="font-size:0.78rem;color:#64748b;margin:0;line-height:1.4;">Vuelo + hotel en un solo paquete</p>
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- Wave divider: light (#faf9f7) → dark (#1C1A18) -->
    <div aria-hidden="true" style="background:#faf9f7;line-height:0;margin-bottom:-1px;">
      <svg viewBox="0 0 1440 56" preserveAspectRatio="none" style="display:block;width:100%;height:56px;">
        <path d="M0,56 Q360,0 720,28 T1440,56 L1440,56 L0,56 Z" fill="#1C1A18"/>
      </svg>
    </div>

    <!-- SECCIÓN 3: Por qué Movent — split layout compacto -->
    <section style="background:#1C1A18; padding:52px 0 60px; position:relative; overflow:hidden;">
      <div class="container">
        <!-- Globo terráqueo decorativo como watermark de fondo -->
        <svg aria-hidden="true" style="position:absolute;right:-60px;top:50%;transform:translateY(-50%);pointer-events:none;opacity:0.055;" width="480" height="480" viewBox="0 0 480 480">
          <circle cx="240" cy="240" r="210" fill="none" stroke="white" stroke-width="1.5"/>
          <ellipse cx="240" cy="240" rx="105" ry="210" fill="none" stroke="white" stroke-width="1"/>
          <ellipse cx="240" cy="240" rx="105" ry="210" fill="none" stroke="white" stroke-width="1" transform="rotate(45,240,240)"/>
          <ellipse cx="240" cy="240" rx="105" ry="210" fill="none" stroke="white" stroke-width="1" transform="rotate(90,240,240)"/>
          <ellipse cx="240" cy="240" rx="105" ry="210" fill="none" stroke="white" stroke-width="1" transform="rotate(135,240,240)"/>
          <ellipse cx="240" cy="170" rx="187" ry="38" fill="none" stroke="white" stroke-width="1"/>
          <ellipse cx="240" cy="240" rx="210" ry="42" fill="none" stroke="white" stroke-width="1"/>
          <ellipse cx="240" cy="310" rx="187" ry="38" fill="none" stroke="white" stroke-width="1"/>
          <line x1="240" y1="30" x2="240" y2="450" stroke="white" stroke-width="1"/>
          <line x1="30" y1="240" x2="450" y2="240" stroke="white" stroke-width="1"/>
        </svg>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:48px;align-items:center;">
          <!-- Izquierda: título + stats del sistema -->
          <div>
            <p style="font-size:0.72rem;font-weight:700;letter-spacing:0.1em;color:#FFCC00;text-transform:uppercase;margin:0 0 12px;">Movent · Agencia de viajes</p>
            <h2 style="font-size:clamp(1.6rem,3vw,2.2rem);font-weight:800;color:#fff;line-height:1.2;margin:0 0 16px;">Tu viaje,<br>sin complicaciones</h2>
            <p style="font-size:0.9rem;color:rgba(255,255,255,0.5);line-height:1.7;margin:0 0 28px;">Buscamos entre múltiples proveedores para darte las mejores opciones de vuelos, hoteles y paquetes en un solo lugar.</p>
            <!-- Si está logueado: sus propias cifras reales -->
            <div v-if="usuarioLogueado" style="display:flex;gap:28px;flex-wrap:wrap;">
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">{{ misReservas.vuelos }}</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">vuelos reservados</p>
              </div>
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">{{ misReservas.hoteles }}</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">hoteles reservados</p>
              </div>
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">{{ misReservas.paquetes }}</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">paquetes reservados</p>
              </div>
            </div>
            <!-- Si no está logueado: beneficios de la plataforma -->
            <div v-else style="display:flex;gap:28px;flex-wrap:wrap;">
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">3</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">tipos de reserva</p>
              </div>
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">100%</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">confirmación digital</p>
              </div>
              <div>
                <p style="font-size:1.6rem;font-weight:800;color:#FFCC00;margin:0;">∞</p>
                <p style="font-size:0.78rem;color:rgba(255,255,255,0.45);margin:0;">historial de viajes</p>
              </div>
            </div>
            <div style="margin-top:28px;">
              <router-link v-if="!usuarioLogueado" to="/ingreso" style="display:inline-block;background:#FFCC00;color:#0f0c0a;font-weight:700;padding:12px 28px;border-radius:8px;text-decoration:none;font-size:0.9rem;">Crear cuenta gratis →</router-link>
              <router-link v-else to="/mis-reservaciones" style="display:inline-block;border:1.5px solid rgba(255,255,255,0.2);color:#fff;font-weight:600;padding:11px 28px;border-radius:8px;text-decoration:none;font-size:0.9rem;">Ver mis reservaciones →</router-link>
            </div>
          </div>
          <!-- Derecha: 4 features 2x2 -->
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
            <div style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.09);border-top:3px solid #FFCC00;border-radius:14px;padding:28px 22px;">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="28" height="28" style="margin-bottom:14px;"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              <h4 style="color:#fff;font-size:1rem;font-weight:700;margin:0 0 7px;">Reservas seguras</h4>
              <p style="color:rgba(255,255,255,0.45);font-size:0.82rem;line-height:1.6;margin:0;">Confirmación inmediata con código único por reserva.</p>
            </div>
            <div style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.09);border-top:3px solid #FFCC00;border-radius:14px;padding:28px 22px;">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="28" height="28" style="margin-bottom:14px;"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              <h4 style="color:#fff;font-size:1rem;font-weight:700;margin:0 0 7px;">Mejor precio</h4>
              <p style="color:rgba(255,255,255,0.45);font-size:0.82rem;line-height:1.6;margin:0;">Comparamos tarifas entre múltiples proveedores al instante.</p>
            </div>
            <div style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.09);border-top:3px solid #FFCC00;border-radius:14px;padding:28px 22px;">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="28" height="28" style="margin-bottom:14px;"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              <h4 style="color:#fff;font-size:1rem;font-weight:700;margin:0 0 7px;">Vuelo + Hotel</h4>
              <p style="color:rgba(255,255,255,0.45);font-size:0.82rem;line-height:1.6;margin:0;">Paquetes combinados, todo en una sola reservación.</p>
            </div>
            <div style="background:rgba(255,255,255,0.05);border:1px solid rgba(255,255,255,0.09);border-top:3px solid #FFCC00;border-radius:14px;padding:28px 22px;">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="28" height="28" style="margin-bottom:14px;"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              <h4 style="color:#fff;font-size:1rem;font-weight:700;margin:0 0 7px;">Historial siempre</h4>
              <p style="color:rgba(255,255,255,0.45);font-size:0.82rem;line-height:1.6;margin:0;">Todas tus reservas accesibles desde tu perfil, siempre.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- RECOMENDACIONES: basadas en la primera reservación del usuario -->
    <section v-if="recomendaciones.length > 0" class="quicksec" style="background: #0f0c0a;">
      <div class="container">
        <div class="section-header">
          <div>
            <h2 class="section-title section-title--light">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="#FFCC00" style="vertical-align:middle; margin-right:6px;"><path d="M12 2l2.4 7.4H22l-6.2 4.5 2.4 7.4L12 17l-6.2 4.3 2.4-7.4L2 9.4h7.6z"/></svg>
              Recomendados para ti
            </h2>
            <p class="section-description section-description--light">Basado en tus viajes — destinos similares con oferta especial</p>
          </div>
        </div>
        <div class="quick-grid">
          <div
            v-for="(rec, i) in recomendaciones"
            :key="i"
            :class="['panel-card', `panel-card--grad-${(i % 6) + 1}`]"
            @click="rec.tipo === 'vuelo' ? explorarVueloRecom(rec, i) : rec.tipo === 'paquete' ? explorarPaqueteRecom(rec, i) : explorarHotelRecom(rec, i)"
            role="button" tabindex="0"
            @keydown.enter="rec.tipo === 'vuelo' ? explorarVueloRecom(rec, i) : rec.tipo === 'paquete' ? explorarPaqueteRecom(rec, i) : explorarHotelRecom(rec, i)"
          >
            <div class="panel-card__descuento">-{{ rec.descuento }}%</div>
            <div class="panel-card__badge">
              <template v-if="rec.tipo === 'vuelo'">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                Vuelo
              </template>
              <template v-else-if="rec.tipo === 'paquete'">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                Paquete
              </template>
              <template v-else>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                Hotel
              </template>
            </div>
            <div class="panel-card__icon">
              <template v-if="rec.tipo === 'vuelo'">
                <svg viewBox="0 0 24 24" fill="currentColor" width="52" height="52"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              </template>
              <template v-else-if="rec.tipo === 'paquete'">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" width="52" height="52"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              </template>
              <template v-else>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="52" height="52"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              </template>
            </div>
            <div class="panel-card__body">
              <p class="panel-card__ciudad">{{ rec.ciudad }}</p>
              <p class="panel-card__pais">{{ rec.pais }}</p>
            </div>
            <div class="panel-card__footer">
              <span v-if="buscandoRecom === i" class="panel-card__searching">
                <svg class="panel-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                Buscando...
              </span>
              <span v-else class="panel-card__cta">
                {{ rec.tipo === 'vuelo' ? 'Ver vuelos' : rec.tipo === 'paquete' ? 'Ver paquete' : 'Ver hoteles' }} →
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- RESUMEN DE ACTIVIDAD: stats reales del usuario sobre fondo oscuro -->
    <section v-if="misReservas.total > 0" class="recientes-section">
      <div class="container">
        <div class="resumen-card">

          <!-- Stat: total gastado (el más prominente) -->
          <div class="resumen-stat resumen-stat--main">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.8" width="22" height="22" style="margin-bottom:10px;"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            <p class="resumen-stat__label">Total gastado</p>
            <p class="resumen-stat__value">${{ resumenUsuario.totalGastado }}</p>
          </div>

          <div class="resumen-divider"></div>

          <!-- Stat: total reservaciones -->
          <div class="resumen-stat">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.8" width="22" height="22" style="margin-bottom:10px;"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            <p class="resumen-stat__label">Reservaciones</p>
            <p class="resumen-stat__value">{{ misReservas.total }}</p>
          </div>

          <div class="resumen-divider"></div>

          <!-- Stat: tipo favorito -->
          <div class="resumen-stat">
            <svg viewBox="0 0 24 24" fill="#FFCC00" width="22" height="22" style="margin-bottom:10px;"><path d="M12 2l2.4 7.4H22l-6.2 4.5 2.4 7.4L12 17l-6.2 4.3 2.4-7.4L2 9.4h7.6z"/></svg>
            <p class="resumen-stat__label">Tu tipo favorito</p>
            <p class="resumen-stat__value">{{ resumenUsuario.tipoFavorito }}</p>
          </div>

          <!-- Stat: destino más frecuente (si existe) -->
          <template v-if="resumenUsuario.destinoFavorito">
            <div class="resumen-divider"></div>
            <div class="resumen-stat">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.8" width="22" height="22" style="margin-bottom:10px;"><path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              <p class="resumen-stat__label">Destino frecuente</p>
              <p class="resumen-stat__value resumen-stat__value--sm">{{ resumenUsuario.destinoFavorito }}</p>
            </div>
          </template>

          <div class="resumen-divider"></div>

          <!-- CTA -->
          <div class="resumen-stat resumen-stat--cta">
            <router-link to="/mis-reservaciones" class="resumen-cta-btn">
              Ver historial
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </router-link>
          </div>

        </div>
      </div>
    </section>

    <!-- FEATURES: beneficios de usar Movent -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">¿Por qué elegir Movent?</h2>
          <p class="section-description">Experiencias de viaje excepcionales con los mejores proveedores del mundo</p>
        </div>
        <div class="features-grid">

          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.6">
                <path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/>
              </svg>
            </div>
            <h3 class="feature-title">Vuelos Globales</h3>
            <p class="feature-description">Accede a vuelos de múltiples aerolíneas con las mejores tarifas desde cualquier destino.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.6">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </div>
            <h3 class="feature-title">Hospedaje Premium</h3>
            <p class="feature-description">Desde hoteles boutique hasta resorts de lujo, el alojamiento perfecto para tu viaje.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.6">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <h3 class="feature-title">Atención Experta</h3>
            <p class="feature-description">Asesores disponibles para ayudarte a planificar cada detalle de tu próximo viaje.</p>
          </div>

          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.6">
                <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
              </svg>
            </div>
            <h3 class="feature-title">Mejor Precio</h3>
            <p class="feature-description">Comparamos precios de múltiples proveedores para darte siempre la mejor tarifa.</p>
          </div>

        </div>
      </div>
    </section>

    <!-- Wave divider: dark recientes (#0f0c0a) → cream features (#faf9f7) -->
    <div v-if="reservacionesRecientes.length > 0" aria-hidden="true" style="background:#0f0c0a;line-height:0;margin-bottom:-1px;">
      <svg viewBox="0 0 1440 56" preserveAspectRatio="none" style="display:block;width:100%;height:56px;">
        <path d="M0,38 C320,8 1120,56 1440,22 L1440,56 L0,56 Z" fill="#faf9f7"/>
      </svg>
    </div>

    <!-- Wave divider: cream (#faf9f7) → dark CTA (#1C1A18) -->
    <div aria-hidden="true" style="background:#faf9f7;line-height:0;margin-bottom:-1px;">
      <svg viewBox="0 0 1440 56" preserveAspectRatio="none" style="display:block;width:100%;height:56px;">
        <path d="M0,56 Q360,0 720,28 T1440,56 L1440,56 L0,56 Z" fill="#1C1A18"/>
      </svg>
    </div>

    <!-- CTA: llamada a la acción — botón "Empezar ahora" sube al buscador -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">¿Listo para tu próxima aventura?</h2>
          <p class="cta-description">Vuelos, hoteles y paquetes en un solo lugar. Empieza a buscar ahora.</p>
          <div style="display:flex;gap:16px;justify-content:center;flex-wrap:wrap;margin-top:2rem;">
            <button class="cta-btn primary" type="button" @click="scrollToSearch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16" style="margin-right:8px;"><path d="M12 19V5M5 12l7-7 7 7"/></svg>
              Empezar ahora
            </button>
            <router-link v-if="!usuarioLogueado" to="/ingreso" class="cta-btn secondary">Iniciar sesión</router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Botón flotante de scroll-to-top, visible después de 300px de desplazamiento -->
    <button v-if="showScrollTop" class="scroll-top" type="button" @click="scrollToTop">
      <svg viewBox="0 0 24 24" class="avion-icon">
        <path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z" />
      </svg>
    </button>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file Principal.vue
 * @description Vista principal (home) de Movent. Muestra estadísticas en tiempo real,
 * un buscador con autocompletado para vuelos, hoteles y paquetes combinados,
 * una sección de features y un CTA. Navega a las vistas de resultados según
 * el tipo de búsqueda seleccionado.
 */

import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/principal.css'

/** Instancia del router para navegar a las vistas de resultados. */
const router = useRouter()

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/** Indica si hay una búsqueda en curso para deshabilitar el botón. @type {boolean} */
const buscando = ref(false)

/** Mensaje de error de validación o de respuesta vacía en la búsqueda. @type {string} */
const searchError = ref('')

/** Tipo de búsqueda activo: 'flights' | 'hotels' | 'combo'. @type {string} */
const searchType = ref('flights')

/** Controla la visibilidad del botón flotante de volver arriba. @type {boolean} */
const showScrollTop = ref(false)

/** Fecha de hoy en formato ISO (YYYY-MM-DD) usada como mínimo en los inputs de fecha. @type {string} */
const hoy = new Date().toISOString().split('T')[0]

/** Tipo de vuelo seleccionado en el tab de vuelos: 'ida' | 'idaVuelta'. @type {string} */
const tipoVuelo = ref('ida')

/** Tipo de vuelo seleccionado dentro del combo vuelo+hotel. @type {string} */
const comboTipoVuelo = ref('ida')

/** Parámetros del formulario de búsqueda de vuelos. @type {{ fecha: string, fechaRegreso: string, cantidadPasajeros: number }} */
const flightData = ref({ fecha: '', fechaRegreso: '', cantidadPasajeros: 1 })

/** Parámetros del formulario de búsqueda de hoteles. @type {{ checkIn: string, checkOut: string, cantidadPersonas: number }} */
const hotelData = ref({ checkIn: '', checkOut: '', cantidadPersonas: 1 })

/** Parámetros del formulario de búsqueda de paquetes combinados. @type {Object} */
const comboData = ref({ fecha: '', fechaRegreso: '', cantidadPersonas: 1, checkIn: '', checkOut: '' })

/** Estadísticas reales de la plataforma cargadas desde el backend al montar. @type {{ aerolineas: number, hoteles: number, reservaciones: number, usuarios: number }} */
const stats = ref({ aerolineas: 0, hoteles: 0, reservaciones: 0, usuarios: 0 })

/**
 * Fecha mínima para el campo de regreso en búsqueda de vuelos (día siguiente a la ida).
 * @type {import('vue').ComputedRef<string>}
 */
const minFechaRegreso = computed(() => {
  if (!flightData.value.fecha) return hoy
  const d = new Date(flightData.value.fecha); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

/**
 * Fecha mínima de regreso dentro del combo (día siguiente a la fecha de vuelo).
 * @type {import('vue').ComputedRef<string>}
 */
const minFechaRegresoCombo = computed(() => {
  if (!comboData.value.fecha) return hoy
  const d = new Date(comboData.value.fecha); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

/**
 * Fecha mínima de check-out en hoteles (día siguiente al check-in).
 * @type {import('vue').ComputedRef<string>}
 */
const minCheckOutHotel = computed(() => {
  if (!hotelData.value.checkIn) return hoy
  const d = new Date(hotelData.value.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

/**
 * Fecha máxima de check-in en el combo, calculada para que el checkout
 * no supere la fecha de regreso del vuelo.
 * @type {import('vue').ComputedRef<string|undefined>}
 */
const maxCheckInCombo = computed(() => {
  if (comboTipoVuelo.value === 'idaVuelta' && comboData.value.fechaRegreso) {
    const base = comboData.value.checkOut || comboData.value.fechaRegreso
    const d = new Date(base)
    d.setDate(d.getDate() - 1)
    return d.toISOString().split('T')[0]
  }
  return undefined
})

/**
 * Fecha mínima de check-out dentro del combo (día siguiente al check-in del hotel).
 * @type {import('vue').ComputedRef<string>}
 */
const minCheckOutCombo = computed(() => {
  if (!comboData.value.checkIn) return hoy
  const d = new Date(comboData.value.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

// Sincroniza el check-in del hotel con la fecha de ida del vuelo en el combo
watch(() => comboData.value.fecha, (nuevaFecha) => {
  if (!nuevaFecha) return
  comboData.value.checkIn = nuevaFecha
  if (comboData.value.checkOut && comboData.value.checkOut <= nuevaFecha)
    comboData.value.checkOut = ''
})

// Sincroniza el check-out del hotel con la fecha de regreso cuando aplica
watch(() => comboData.value.fechaRegreso, (nuevaFechaRegreso) => {
  if (!nuevaFechaRegreso || comboTipoVuelo.value !== 'idaVuelta') return
  comboData.value.checkOut = nuevaFechaRegreso
  if (comboData.value.checkIn && comboData.value.checkIn >= nuevaFechaRegreso)
    comboData.value.checkIn = comboData.value.fecha || ''
})

// Limpia las fechas de regreso si se cambia a solo ida en el combo
watch(() => comboTipoVuelo.value, (tipo) => {
  if (tipo === 'ida') { comboData.value.fechaRegreso = ''; comboData.value.checkOut = '' }
})

// Resetea el check-out si el check-in cambia a una fecha posterior
watch(() => comboData.value.checkIn, (nuevoCheckIn) => {
  if (!nuevoCheckIn) return
  if (comboData.value.checkOut && comboData.value.checkOut <= nuevoCheckIn)
    comboData.value.checkOut = ''
})

/**
 * Formatea una fecha ISO a formato legible corto en español (ej. "04 abr").
 *
 * @param {string} f - Fecha en formato YYYY-MM-DD.
 * @returns {string} Fecha formateada o la cadena original si falla el parse.
 */
function formatFechaCorta(f) {
  if (!f) return ''
  try { return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short' }) }
  catch { return f }
}

/** Cache en memoria de la lista de países de la API externa. @type {Array|null} */
let paisesCache = null

/**
 * Obtiene la lista de países desde countriesnow.space.
 * Usa cache en memoria para evitar múltiples llamadas.
 *
 * @async
 * @returns {Promise<Array>} Lista de objetos con al menos { country: string }.
 */
async function getPaises() {
  if (paisesCache) return paisesCache
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); paisesCache = d.data || [] } catch { paisesCache = [] }
  return paisesCache
}

/**
 * Obtiene las ciudades de un país desde countriesnow.space.
 *
 * @async
 * @param {string} country - Nombre del país en inglés.
 * @returns {Promise<string[]>} Lista de nombres de ciudades.
 */
async function getCiudades(country) {
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ country }) }); const d = await r.json(); return d.data || [] } catch { return [] }
}

/**
 * Aplaza una función de cierre de dropdown para que el clic en la lista
 * se procese antes de que desaparezca.
 *
 * @param {Function} fn - Función que limpia el array de sugerencias.
 */
function blur(fn) { setTimeout(fn, 200) }

/** Query del input de país de origen. @type {string} */
const oPaisQ = ref(''); const oPaisSug = ref([]); const oPaisSel = ref(null)
/** Query del input de ciudad de origen. @type {string} */
const oCiudadQ = ref(''); const oCiudadSug = ref([]); const oCiudadLoading = ref(false)
/** Lista completa de ciudades del país de origen seleccionado. @type {string[]} */
const oCiudades = ref([])
/** Objeto con el país y ciudad de origen confirmados. @type {{ pais: string, ciudad: string }} */
const origen = ref({ pais: '', ciudad: '' })

/**
 * Filtra países que coincidan con el input de origen y actualiza oPaisSug.
 * Resetea la selección de ciudad al cambiar de país.
 *
 * @async
 */
async function onOPaisInput() {
  oPaisSel.value = null; oCiudadQ.value = ''; oCiudades.value = []; origen.value = { pais: '', ciudad: '' }
  const q = oPaisQ.value.trim(); if (q.length < 2) { oPaisSug.value = []; return }
  oPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}

/**
 * Confirma la selección de un país de origen y carga sus ciudades.
 *
 * @async
 * @param {{ country: string }} p - Objeto del país seleccionado.
 */
async function selOPais(p) {
  oPaisSel.value = p; oPaisQ.value = p.country; oPaisSug.value = []; origen.value.pais = p.country
  oCiudadLoading.value = true; oCiudades.value = await getCiudades(p.country); oCiudadLoading.value = false
}

/** Filtra las ciudades del país de origen según el texto escrito. */
function onOCiudadInput() {
  const q = oCiudadQ.value.toLowerCase()
  oCiudadSug.value = q.length < 2 ? [] : oCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  origen.value.ciudad = ''
}

/**
 * Confirma la selección de la ciudad de origen.
 *
 * @param {string} c - Nombre de la ciudad.
 */
function selOCiudad(c) { oCiudadQ.value = c; oCiudadSug.value = []; origen.value.ciudad = c; searchError.value = '' }

/** Query del input de país de destino. @type {string} */
const dPaisQ = ref(''); const dPaisSug = ref([]); const dPaisSel = ref(null)
/** Query del input de ciudad de destino. @type {string} */
const dCiudadQ = ref(''); const dCiudadSug = ref([]); const dCiudadLoading = ref(false)
/** Lista completa de ciudades del país de destino seleccionado. @type {string[]} */
const dCiudades = ref([])
/** Objeto con el país y ciudad de destino confirmados. @type {{ pais: string, ciudad: string }} */
const destino = ref({ pais: '', ciudad: '' })

/**
 * Filtra países que coincidan con el input de destino.
 *
 * @async
 */
async function onDPaisInput() {
  dPaisSel.value = null; dCiudadQ.value = ''; dCiudades.value = []; destino.value = { pais: '', ciudad: '' }
  const q = dPaisQ.value.trim(); if (q.length < 2) { dPaisSug.value = []; return }
  dPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}

/**
 * Confirma la selección de un país de destino y carga sus ciudades.
 *
 * @async
 * @param {{ country: string }} p - Objeto del país seleccionado.
 */
async function selDPais(p) {
  dPaisSel.value = p; dPaisQ.value = p.country; dPaisSug.value = []; destino.value.pais = p.country
  dCiudadLoading.value = true; dCiudades.value = await getCiudades(p.country); dCiudadLoading.value = false
}

/** Filtra las ciudades del país de destino según el texto escrito. */
function onDCiudadInput() {
  const q = dCiudadQ.value.toLowerCase()
  dCiudadSug.value = q.length < 2 ? [] : dCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  destino.value.ciudad = ''
}

/**
 * Confirma la selección de la ciudad de destino.
 *
 * @param {string} c - Nombre de la ciudad.
 */
function selDCiudad(c) { dCiudadQ.value = c; dCiudadSug.value = []; destino.value.ciudad = c; searchError.value = '' }

/**
 * Verifica si la respuesta del backend contiene al menos un vuelo disponible
 * (ya sea directo o con escala en algún bloque de resultados).
 *
 * @param {Array} respuesta - Array de bloques de resultados devueltos por /api/busqueda/vuelos.
 * @returns {boolean}
 */
function tieneVuelos(respuesta) {
  if (!Array.isArray(respuesta) || respuesta.length === 0) return false
  return respuesta.some(b => b.datos && (
    (b.datos.directos?.length  > 0) ||
    (b.datos.conEscala?.length > 0)
  ))
}

/**
 * Verifica si la respuesta del backend contiene al menos un hotel disponible.
 *
 * @param {Array} respuesta - Array de bloques de resultados devueltos por /api/busqueda/hoteles.
 * @returns {boolean}
 */
function tieneHoteles(respuesta) {
  if (!Array.isArray(respuesta) || respuesta.length === 0) return false
  return respuesta.some(b => Array.isArray(b.datos) && b.datos.length > 0)
}

/**
 * Valida los campos del buscador de vuelos, llama al endpoint correspondiente
 * y navega a /resultados-vuelos pasando los resultados por router state.
 * Soporta vuelo de solo ida e ida y vuelta (hace dos peticiones en paralelo).
 *
 * @async
 * @returns {Promise<void>}
 */
const buscarVuelos = async () => {
  searchError.value = ''
  if (!origen.value.pais || !origen.value.ciudad)   { searchError.value = 'Selecciona el país y ciudad de origen.'; return }
  if (!destino.value.pais || !destino.value.ciudad)  { searchError.value = 'Selecciona el país y ciudad de destino.'; return }
  if (!flightData.value.fecha)                       { searchError.value = 'Selecciona una fecha de ida.'; return }
  if (flightData.value.fecha < hoy)                  { searchError.value = 'La fecha de ida no puede ser en el pasado.'; return }
  if (tipoVuelo.value === 'idaVuelta') {
    if (!flightData.value.fechaRegreso)              { searchError.value = 'Selecciona la fecha de regreso.'; return }
    if (flightData.value.fechaRegreso <= flightData.value.fecha) { searchError.value = 'La fecha de regreso debe ser posterior a la de ida.'; return }
  }

  buscando.value = true
  try {
    const bodyIda = {
      origen: origen.value.ciudad, origenPais: origen.value.pais,
      destino: destino.value.ciudad, destinoPais: destino.value.pais,
      fecha: flightData.value.fecha, cantidadPasajeros: flightData.value.cantidadPasajeros,
    }

    if (tipoVuelo.value === 'idaVuelta') {
      const bodyRegreso = {
        origen: destino.value.ciudad, origenPais: destino.value.pais,
        destino: origen.value.ciudad, destinoPais: origen.value.pais,
        fecha: flightData.value.fechaRegreso, cantidadPasajeros: flightData.value.cantidadPasajeros,
      }
      const [resIda, resRegreso] = await Promise.all([
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) }),
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyRegreso) }),
      ])
      if (!resIda.ok) throw new Error(`Error ${resIda.status}`)
      const resultados        = await resIda.json()
      const resultadosRegreso = resRegreso.ok ? await resRegreso.json() : []

      if (!tieneVuelos(resultados)) { searchError.value = `No hay vuelos de ${origen.value.ciudad} a ${destino.value.ciudad} para el ${flightData.value.fecha}.`; return }
      if (!tieneVuelos(resultadosRegreso)) { searchError.value = `No hay vuelos de regreso de ${destino.value.ciudad} a ${origen.value.ciudad} para el ${flightData.value.fechaRegreso}. Prueba otra fecha de regreso.`; return }

      router.push({
        path: '/resultados-vuelos',
        state: {
          resultados, resultadosRegreso,
          busqueda: {
            origen: origen.value.ciudad, origenPais: origen.value.pais,
            destino: destino.value.ciudad, destinoPais: destino.value.pais,
            fecha: flightData.value.fecha, fechaRegreso: flightData.value.fechaRegreso,
            cantidadPasajeros: flightData.value.cantidadPasajeros,
            tipoVuelo: 'idaVuelta',
          }
        }
      })
    } else {
      const res = await fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) })
      if (!res.ok) throw new Error(`Error ${res.status}`)
      const resultados = await res.json()
      router.push({
        path: '/resultados-vuelos',
        state: {
          resultados,
          busqueda: {
            origen: origen.value.ciudad, origenPais: origen.value.pais,
            destino: destino.value.ciudad, destinoPais: destino.value.pais,
            fecha: flightData.value.fecha, cantidadPasajeros: flightData.value.cantidadPasajeros,
            tipoVuelo: 'ida',
          }
        }
      })
    }
  } catch (err) {
    console.error('Error buscando vuelos:', err)
    searchError.value = 'No se pudieron obtener vuelos. Intenta de nuevo.'
  } finally { buscando.value = false }
}

/**
 * Valida los campos del buscador de hoteles, llama al endpoint y navega
 * a /resultados-hoteles pasando los resultados por router state.
 *
 * @async
 * @returns {Promise<void>}
 */
const buscarHoteles = async () => {
  searchError.value = ''
  if (!destino.value.pais || !destino.value.ciudad)          { searchError.value = 'Selecciona el país y ciudad de destino.'; return }
  if (!hotelData.value.checkIn)                              { searchError.value = 'Selecciona la fecha de check-in.'; return }
  if (hotelData.value.checkIn < hoy)                         { searchError.value = 'El check-in no puede ser una fecha pasada.'; return }
  if (!hotelData.value.checkOut)                             { searchError.value = 'Selecciona la fecha de check-out.'; return }
  if (hotelData.value.checkOut <= hotelData.value.checkIn)   { searchError.value = 'El check-out debe ser al menos un día después del check-in.'; return }

  buscando.value = true
  try {
    const res = await fetch(`${API}/api/busqueda/hoteles`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ciudad: destino.value.ciudad, pais: destino.value.pais, fechaCheckIn: hotelData.value.checkIn, fechaCheckOut: hotelData.value.checkOut, cantidadPersonas: hotelData.value.cantidadPersonas })
    })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const resultados = await res.json()
    router.push({ path: '/resultados-hoteles', state: { resultados, busqueda: { ciudad: destino.value.ciudad, pais: destino.value.pais, checkIn: hotelData.value.checkIn, checkOut: hotelData.value.checkOut, cantidadPersonas: hotelData.value.cantidadPersonas } } })
  } catch {
    searchError.value = 'No se pudieron obtener hoteles. Intenta de nuevo.'
  } finally { buscando.value = false }
}

/**
 * Valida los campos del buscador de paquetes, ejecuta peticiones paralelas
 * para vuelo(s) y hotel, y navega a /resultados-paquetes con los resultados.
 *
 * @async
 * @returns {Promise<void>}
 */
const buscarPaquetes = async () => {
  searchError.value = ''
  if (!origen.value.pais || !origen.value.ciudad)            { searchError.value = 'Selecciona el país y ciudad de origen.'; return }
  if (!destino.value.pais || !destino.value.ciudad)          { searchError.value = 'Selecciona el país y ciudad de destino.'; return }
  if (!comboData.value.fecha)                                { searchError.value = 'Selecciona la fecha de vuelo.'; return }
  if (comboData.value.fecha < hoy)                           { searchError.value = 'La fecha de vuelo no puede ser en el pasado.'; return }
  if (comboTipoVuelo.value === 'idaVuelta') {
    if (!comboData.value.fechaRegreso)                       { searchError.value = 'Selecciona la fecha de regreso del vuelo.'; return }
    if (comboData.value.fechaRegreso <= comboData.value.fecha) { searchError.value = 'La fecha de regreso debe ser posterior a la de ida.'; return }
  }
  if (!comboData.value.checkIn)                              { searchError.value = 'Selecciona la fecha de check-in del hotel.'; return }
  if (comboData.value.checkIn < hoy)                         { searchError.value = 'El check-in no puede ser una fecha pasada.'; return }
  if (!comboData.value.checkOut)                             { searchError.value = 'Selecciona la fecha de check-out del hotel.'; return }
  if (comboData.value.checkOut <= comboData.value.checkIn)   { searchError.value = 'El check-out debe ser posterior al check-in.'; return }
  if (comboData.value.checkIn < comboData.value.fecha) { searchError.value = `El check-in del hotel no puede ser antes de la salida del vuelo (${formatFechaCorta(comboData.value.fecha)}).`; return }
  if (comboTipoVuelo.value === 'idaVuelta' && comboData.value.fechaRegreso) {
    if (comboData.value.checkOut > comboData.value.fechaRegreso) { searchError.value = `El check-out del hotel no puede ser después del vuelo de regreso (${formatFechaCorta(comboData.value.fechaRegreso)}).`; return }
  }

  buscando.value = true
  try {
    const bodyIda   = { origen: origen.value.ciudad, origenPais: origen.value.pais, destino: destino.value.ciudad, destinoPais: destino.value.pais, fecha: comboData.value.fecha, cantidadPasajeros: comboData.value.cantidadPersonas }
    const bodyHotel = { ciudad: destino.value.ciudad, pais: destino.value.pais, fechaCheckIn: comboData.value.checkIn, fechaCheckOut: comboData.value.checkOut, cantidadPersonas: comboData.value.cantidadPersonas }

    const fetchIda   = fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) })
    const fetchHotel = fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyHotel) })

    let resultadosVuelos, resultadosRegreso, resultadosHoteles

    if (comboTipoVuelo.value === 'idaVuelta') {
      const bodyRegreso  = { origen: destino.value.ciudad, origenPais: destino.value.pais, destino: origen.value.ciudad, destinoPais: origen.value.pais, fecha: comboData.value.fechaRegreso, cantidadPasajeros: comboData.value.cantidadPersonas }
      const fetchRegreso = fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyRegreso) })
      const [resIda, resReg, resHotel] = await Promise.all([fetchIda, fetchRegreso, fetchHotel])
      resultadosVuelos  = resIda.ok   ? await resIda.json()   : []
      resultadosRegreso = resReg.ok   ? await resReg.json()   : []
      resultadosHoteles = resHotel.ok ? await resHotel.json() : []
      if (!tieneVuelos(resultadosVuelos))   { searchError.value = `No hay vuelos de ${origen.value.ciudad} a ${destino.value.ciudad} para el ${comboData.value.fecha}.`; return }
      if (!tieneVuelos(resultadosRegreso))  { searchError.value = `No hay vuelos de regreso de ${destino.value.ciudad} a ${origen.value.ciudad} para el ${comboData.value.fechaRegreso}.`; return }
      if (!tieneHoteles(resultadosHoteles)) { searchError.value = `No hay hoteles en ${destino.value.ciudad} para esas fechas.`; return }
    } else {
      const [resIda, resHotel] = await Promise.all([fetchIda, fetchHotel])
      resultadosVuelos  = resIda.ok   ? await resIda.json()   : []
      resultadosHoteles = resHotel.ok ? await resHotel.json() : []
      resultadosRegreso = []
      if (!tieneVuelos(resultadosVuelos))   { searchError.value = `No hay vuelos de ${origen.value.ciudad} a ${destino.value.ciudad} para el ${comboData.value.fecha}.`; return }
      if (!tieneHoteles(resultadosHoteles)) { searchError.value = `No hay hoteles en ${destino.value.ciudad} para esas fechas.`; return }
    }

    router.push({
      path: '/resultados-paquetes',
      state: {
        resultadosVuelos, resultadosRegreso, resultadosHoteles,
        busqueda: {
          origen: origen.value.ciudad, origenPais: origen.value.pais,
          destino: destino.value.ciudad, destinoPais: destino.value.pais,
          fecha: comboData.value.fecha, fechaRegreso: comboData.value.fechaRegreso,
          cantidadPersonas: comboData.value.cantidadPersonas,
          checkIn: comboData.value.checkIn, checkOut: comboData.value.checkOut,
          tipoVuelo: comboTipoVuelo.value,
        }
      }
    })
  } catch {
    searchError.value = 'No se pudieron obtener los paquetes. Intenta de nuevo.'
  } finally { buscando.value = false }
}

/**
 * Lista de tarjetas de características mostradas en la sección "¿Por qué elegir Movent?".
 * Cada ítem tiene un SVG inline, un título y una descripción.
 * @type {Array<{ icon: string, title: string, description: string }>}
 */
const features = [
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>`, title: 'Vuelos Globales', description: 'Accede a vuelos de múltiples aerolíneas con las mejores tarifas garantizadas desde cualquier destino.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`, title: 'Hospedaje Premium', description: 'Desde hoteles boutique hasta resorts de lujo, encuentra el alojamiento perfecto para tu viaje.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`, title: 'Atención Experta', description: 'Nuestro equipo de asesores está disponible para ayudarte a planificar cada detalle de tu viaje.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`, title: 'Mejor Precio', description: 'Comparamos precios de múltiples proveedores para ofrecerte siempre la mejor tarifa disponible.' },
]

/** Actualiza showScrollTop según la posición vertical del scroll. */
const onScroll = () => { showScrollTop.value = window.scrollY > 300 }

/** Hace scroll suave hasta el tope de la página. */
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

// ── Paneles de descubrir ──────────────────────────────────────────────────────

/** Paneles de destinos sugeridos devueltos por /api/descubrir. */
const paneles = ref([])

/** Indica si los paneles están cargando. */
const cargandoPaneles = ref(true)

/** Nombre de la ciudad de origen del usuario (para el título de la sección). */
const origenNombre = ref('')

/** Datos del origen del usuario (ciudad + pais). */
const origenData = ref({ ciudad: '', pais: '' })

/** Indica si el usuario está autenticado. */
const usuarioLogueado = ref(false)

/** Destinos frecuentes estáticos para la sección 2. Clic pre-llena el buscador. */
const destinosFrecuentes = [
  { ciudad: 'Cancún',        pais: 'Mexico',        tipo: 'vuelo', grad: 1 },
  { ciudad: 'Miami Beach',   pais: 'United States',  tipo: 'vuelo', grad: 2 },
  { ciudad: 'Bogotá',        pais: 'Colombia',       tipo: 'vuelo', grad: 3 },
  { ciudad: 'Madrid',        pais: 'Spain',          tipo: 'vuelo', grad: 4 },
  { ciudad: 'Guatemala City',pais: 'Guatemala',      tipo: 'hotel', grad: 5 },
  { ciudad: 'Belize City',   pais: 'Belize',         tipo: 'hotel', grad: 6 },
]

/** Pre-llena el buscador con el destino seleccionado y hace scroll al form. */
function irDestino(dest) {
  dPaisQ.value   = dest.pais
  dCiudadQ.value = dest.ciudad
  destino.value  = { pais: dest.pais, ciudad: dest.ciudad }
  searchType.value = dest.tipo === 'vuelo' ? 'flights' : 'hotels'
  document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/** Cambia la categoría de búsqueda activa y hace scroll al buscador. */
function irACategoria(tipo) {
  searchType.value = tipo
  document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/** Hace scroll hasta el buscador (hero). Usado por el CTA "Empezar ahora". */
function scrollToSearch() {
  document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/** Índice del panel que está siendo buscado (para mostrar spinner en ese panel). */
const buscandoPanel = ref(-1)

/**
 * Carga los paneles de descubrimiento personalizados según el origen del usuario.
 * Si está autenticado obtiene su ciudad/país del perfil antes de llamar al endpoint.
 */
async function cargarPaneles() {
  cargandoPaneles.value = true
  try {
    const sesionRaw = sessionStorage.getItem('usuario_sesion') || localStorage.getItem('usuario_sesion')
    let ciudad = '', pais = ''

    if (sesionRaw) {
      usuarioLogueado.value = true
      try {
        // Obtener ciudad/país del perfil
        const perfilRes = await fetch(`${API}/api/perfil`, { credentials: 'include' })
        if (perfilRes.ok) {
          const perfil = await perfilRes.json()
          ciudad = perfil.ciudad || ''
          pais   = perfil.pais   || ''
          origenNombre.value = ciudad || ''
          origenData.value = { ciudad, pais }
        }
      } catch { /* perfil no crítico */ }
    }

    const qs = ciudad && pais ? `?ciudad=${encodeURIComponent(ciudad)}&pais=${encodeURIComponent(pais)}` : ''
    const res = await fetch(`${API}/api/descubrir${qs}`)
    if (res.ok) {
      const data = await res.json()
      paneles.value = data.paneles || []
    }
  } catch { /* silencioso, no interrumpe la página */ } finally {
    cargandoPaneles.value = false
  }
}

/**
 * Gestiona el clic en un panel: lanza la búsqueda correspondiente y navega
 * a la vista de resultados. Para vuelos usa la ciudad del usuario como origen.
 * Para hoteles no necesita origen.
 *
 * @param {object} panel - Panel seleccionado con tipo, ciudad, pais.
 * @param {number} idx   - Índice del panel (para mostrar el spinner correcto).
 */
async function explorarPanel(panel, idx) {
  if (buscandoPanel.value !== -1) return
  buscandoPanel.value = idx ?? paneles.value.indexOf(panel)

  const manana = new Date()
  manana.setDate(manana.getDate() + 7)
  const fechaBase = manana.toISOString().split('T')[0]

  try {
    if (panel.tipo === 'vuelo') {
      const { ciudad: oCiudad, pais: oPais } = origenData.value

      // Sin origen conocido: pre-llena destino en el buscador y hace scroll
      if (!oCiudad || !oPais) {
        dPaisQ.value   = panel.pais
        dCiudadQ.value = panel.ciudad
        destino.value  = { pais: panel.pais, ciudad: panel.ciudad }
        searchType.value = 'flights'
        document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
        return
      }

      const body = {
        origen: oCiudad, origenPais: oPais,
        destino: panel.ciudad, destinoPais: panel.pais,
        fecha: fechaBase, cantidadPasajeros: 1,
      }
      const res = await fetch(`${API}/api/busqueda/vuelos`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) throw new Error(`${res.status}`)
      const resultados = await res.json()
      router.push({
        path: '/resultados-vuelos',
        state: {
          resultados,
          busqueda: { ...body, tipoVuelo: 'ida' },
        },
      })

    } else {
      // Hotel: no necesita origen
      const pasado = new Date(manana)
      pasado.setDate(pasado.getDate() + 3)
      const body = {
        ciudad: panel.ciudad, pais: panel.pais,
        fechaCheckIn: fechaBase,
        fechaCheckOut: pasado.toISOString().split('T')[0],
        cantidadPersonas: 1,
      }
      const res = await fetch(`${API}/api/busqueda/hoteles`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) throw new Error(`${res.status}`)
      const resultados = await res.json()
      router.push({
        path: '/resultados-hoteles',
        state: {
          resultados,
          busqueda: { ...body },
        },
      })
    }
  } catch {
    // Si falla la búsqueda, pre-llena el destino en el buscador y hace scroll
    dPaisQ.value   = panel.pais
    dCiudadQ.value = panel.ciudad
    destino.value  = { pais: panel.pais, ciudad: panel.ciudad }
    searchType.value = panel.tipo === 'vuelo' ? 'flights' : 'hotels'
    document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  } finally {
    buscandoPanel.value = -1
  }
}

// ── Vuelos / Hoteles / Paquetes disponibles (pre-cargados en mount) ──────────

/**
 * Crea 4 cards vacías con estado de carga activo.
 * @returns {Array}
 */
function cardsSkeleton() {
  return Array.from({ length: 4 }, () => ({
    ciudad: '', pais: '', fecha: '', precioDesde: 0,
    cargando: true,
    resultados: null, busqueda: null,
    resultadosVuelos: null, resultadosHoteles: null,
  }))
}

/** Cards de vuelos — 4 slots, skeletons hasta que el prefetch termine. */
const vuelosCards = ref(cardsSkeleton())

/** Cards de hoteles. */
const hotelesCards = ref(cardsSkeleton())

/** Cards de paquetes. */
const paquetesCards = ref(cardsSkeleton())

/** Índice del card de recomendación que muestra spinner (-1 = ninguno). */
const buscandoRecom   = ref(-1)

/**
 * Devuelve una fecha futura en formato YYYY-MM-DD.
 * @param {number} dias - Días desde hoy.
 */
function fechaOffset(dias) {
  const d = new Date(); d.setDate(d.getDate() + dias)
  return d.toISOString().split('T')[0]
}

/**
 * Pre-llena el destino en el buscador principal y hace scroll.
 * Usado como fallback cuando el usuario no tiene origen o la búsqueda falla.
 */
function prefillDest(dest, tipo) {
  dPaisQ.value = dest.pais; dCiudadQ.value = dest.ciudad
  destino.value = { pais: dest.pais, ciudad: dest.ciudad }
  searchType.value = tipo
  document.querySelector('.search-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

/**
 * Extrae el precio mínimo de turista de los resultados de búsqueda de vuelos.
 * @param {Array} resultados
 * @returns {number|null}
 */
function minPrecioVuelos(resultados) {
  let min = Infinity
  for (const b of resultados) {
    for (const v of [...(b.datos?.directos || []), ...(b.datos?.conEscala || [])]) {
      const p = v.precioTurista ?? v.precio ?? 0
      if (p > 0 && p < min) min = p
    }
  }
  return min === Infinity ? null : min
}

/**
 * Extrae el precio mínimo por noche de los resultados de búsqueda de hoteles.
 * @param {Array} resultados
 * @returns {number|null}
 */
function minPrecioHoteles(resultados) {
  let min = Infinity
  for (const b of resultados) {
    if (!Array.isArray(b.datos)) continue
    for (const h of b.datos) {
      const habs = Array.isArray(h.habitaciones) ? h.habitaciones : [h]
      for (const hb of habs) {
        const p = hb.precioPorNoche ?? hb.precio ?? 0
        if (p > 0 && p < min) min = p
      }
    }
  }
  return min === Infinity ? null : min
}

/**
 * Carga los destinos disponibles del backend y dispara las búsquedas reales
 * en paralelo para cada card. Las cards se actualizan conforme llegan los resultados.
 *
 * @async
 */
async function prefetchDisponibles() {
  try {
    const res = await fetch(`${API}/api/descubrir/disponibles`)
    if (!res.ok) throw new Error()
    const data = await res.json()

    // ── Vuelos ──
    // El backend devuelve parámetros completos (origen, destino, fecha, cantidadPasajeros).
    const vDestinos = (data.vuelos || []).slice(0, 4)
    vuelosCards.value = vDestinos.length
      ? vDestinos.map(d => ({ ciudad: d.destino, pais: d.destinoPais, fecha: d.fecha, precioDesde: 0, cargando: true, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null }))
      : cardsSkeleton()

    vDestinos.forEach(async (dest, i) => {
      const body = {
        origen: dest.origen || 'Guatemala City',
        origenPais: dest.origenPais || 'Guatemala',
        destino: dest.destino,
        destinoPais: dest.destinoPais,
        fecha: dest.fecha,
        cantidadPasajeros: dest.cantidadPasajeros || 1,
      }
      try {
        const r = await fetch(`${API}/api/busqueda/vuelos`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body),
        })
        const resultados = r.ok ? await r.json() : null
        const precio = resultados ? minPrecioVuelos(resultados) : null
        vuelosCards.value[i] = {
          ...vuelosCards.value[i],
          cargando: false,
          resultados: tieneVuelos(resultados || []) ? resultados : null,
          busqueda: { ...body, tipoVuelo: 'ida' },
          precioDesde: precio ?? 0,
        }
      } catch {
        vuelosCards.value[i] = { ...vuelosCards.value[i], cargando: false }
      }
    })

    if (vDestinos.length < 4) {
      for (let i = vDestinos.length; i < 4; i++)
        vuelosCards.value[i] = { ciudad: '', pais: '', cargando: false, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null, precioDesde: 0 }
    }

    // ── Hoteles ──
    // El backend devuelve parámetros completos (ciudad, pais, fechaCheckIn, fechaCheckOut, cantidadPersonas).
    const hDestinos = (data.hoteles || []).slice(0, 4)
    hotelesCards.value = hDestinos.length
      ? hDestinos.map(d => ({ ciudad: d.ciudad, pais: d.pais, fecha: d.fechaCheckIn, precioDesde: 0, cargando: true, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null }))
      : cardsSkeleton()

    hDestinos.forEach(async (dest, i) => {
      const body = {
        ciudad: dest.ciudad, pais: dest.pais,
        fechaCheckIn: dest.fechaCheckIn,
        fechaCheckOut: dest.fechaCheckOut,
        cantidadPersonas: dest.cantidadPersonas || 1,
      }
      try {
        const r = await fetch(`${API}/api/busqueda/hoteles`, {
          method: 'POST', credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body),
        })
        const resultados = r.ok ? await r.json() : null
        const precio = resultados ? minPrecioHoteles(resultados) : null
        hotelesCards.value[i] = {
          ...hotelesCards.value[i],
          cargando: false,
          resultados: tieneHoteles(resultados || []) ? resultados : null,
          busqueda: { ciudad: dest.ciudad, pais: dest.pais, checkIn: dest.fechaCheckIn, checkOut: dest.fechaCheckOut, cantidadPersonas: dest.cantidadPersonas || 1 },
          precioDesde: precio ?? 0,
        }
      } catch {
        hotelesCards.value[i] = { ...hotelesCards.value[i], cargando: false }
      }
    })

    if (hDestinos.length < 4) {
      for (let i = hDestinos.length; i < 4; i++)
        hotelesCards.value[i] = { ciudad: '', pais: '', cargando: false, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null, precioDesde: 0 }
    }

    // ── Paquetes ──
    // El backend devuelve los mismos destinos de vuelo; el frontend busca
    // tanto el vuelo como un hotel en la ciudad de destino.
    const pDestinos = (data.paquetes || []).slice(0, 4)
    paquetesCards.value = pDestinos.length
      ? pDestinos.map(d => ({ ciudad: d.destino, pais: d.destinoPais, fecha: d.fecha, precioDesde: 0, cargando: true, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null }))
      : cardsSkeleton()

    pDestinos.forEach(async (dest, i) => {
      const checkOut = (() => { try { const d = new Date(dest.fecha + 'T00:00:00'); d.setDate(d.getDate() + 3); return d.toISOString().split('T')[0] } catch { return fechaOffset(10) } })()
      const bodyV = {
        origen: dest.origen || 'Guatemala City',
        origenPais: dest.origenPais || 'Guatemala',
        destino: dest.destino, destinoPais: dest.destinoPais,
        fecha: dest.fecha, cantidadPasajeros: dest.cantidadPasajeros || 1,
      }
      const bodyH = {
        ciudad: dest.destino, pais: dest.destinoPais,
        fechaCheckIn: dest.fecha, fechaCheckOut: checkOut,
        cantidadPersonas: dest.cantidadPasajeros || 1,
      }
      try {
        const [rV, rH] = await Promise.all([
          fetch(`${API}/api/busqueda/vuelos`,  { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyV) }),
          fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyH) }),
        ])
        const resultadosVuelos  = rV.ok ? await rV.json() : null
        const resultadosHoteles = rH.ok ? await rH.json() : null
        const pV = resultadosVuelos  ? minPrecioVuelos(resultadosVuelos)   : null
        const pH = resultadosHoteles ? minPrecioHoteles(resultadosHoteles) : null
        paquetesCards.value[i] = {
          ...paquetesCards.value[i],
          cargando: false,
          resultadosVuelos:  tieneVuelos(resultadosVuelos   || []) ? resultadosVuelos  : null,
          resultadosHoteles: tieneHoteles(resultadosHoteles || []) ? resultadosHoteles : null,
          busqueda: { origen: bodyV.origen, origenPais: bodyV.origenPais, destino: dest.destino, destinoPais: dest.destinoPais, fecha: dest.fecha, checkIn: dest.fecha, checkOut, cantidadPersonas: dest.cantidadPasajeros || 1, tipoVuelo: 'ida' },
          precioDesde: (pV && pH) ? pV + pH : 0,
        }
      } catch {
        paquetesCards.value[i] = { ...paquetesCards.value[i], cargando: false }
      }
    })

    if (pDestinos.length < 4) {
      for (let i = pDestinos.length; i < 4; i++)
        paquetesCards.value[i] = { ciudad: '', pais: '', cargando: false, resultados: null, busqueda: null, resultadosVuelos: null, resultadosHoteles: null, precioDesde: 0 }
    }

  } catch {
    vuelosCards.value   = vuelosCards.value.map(c => ({ ...c, cargando: false }))
    hotelesCards.value  = hotelesCards.value.map(c => ({ ...c, cargando: false }))
    paquetesCards.value = paquetesCards.value.map(c => ({ ...c, cargando: false }))
  }
}

/**
 * Navega a resultados de vuelo.
 * Si el prefetch ya tiene resultados → navegación instantánea.
 * Si no → búsqueda en vivo (idéntico a explorarPanel) con spinner en la card.
 */
async function irVuelo(card, idx) {
  if (card.cargando || buscandoVuelo.value !== -1 || !card.ciudad) return
  if (card.resultados) {
    router.push({ path: '/resultados-vuelos', state: { resultados: card.resultados, busqueda: card.busqueda } })
    return
  }
  // Construir body: usar busqueda del prefetch si existe; si no, usar origenData + card
  const { ciudad: oCiudad, pais: oPais } = origenData.value
  const body = card.busqueda
    ? { origen: card.busqueda.origen, origenPais: card.busqueda.origenPais, destino: card.busqueda.destino, destinoPais: card.busqueda.destinoPais, fecha: card.busqueda.fecha, cantidadPasajeros: card.busqueda.cantidadPasajeros || 1 }
    : { origen: oCiudad || 'Guatemala City', origenPais: oPais || 'Guatemala', destino: card.ciudad, destinoPais: card.pais, fecha: fechaOffset(180), cantidadPasajeros: 1 }
  buscandoVuelo.value = idx ?? 0
  try {
    const r = await fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
    const resultados = r.ok ? await r.json() : []
    router.push({ path: '/resultados-vuelos', state: { resultados, busqueda: { ...body, tipoVuelo: 'ida' } } })
  } catch {
    prefillDest(card, 'flights')
  } finally {
    buscandoVuelo.value = -1
  }
}

/**
 * Navega a resultados de hotel.
 * Si el prefetch ya tiene resultados → navegación instantánea.
 * Si no → búsqueda en vivo con spinner en la card.
 */
async function irHotel(card, idx) {
  if (card.cargando || buscandoHotel.value !== -1 || !card.ciudad) return
  if (card.resultados) {
    router.push({ path: '/resultados-hoteles', state: { resultados: card.resultados, busqueda: card.busqueda } })
    return
  }
  const checkIn  = card.busqueda?.checkIn  || fechaOffset(180)
  const checkOut = card.busqueda?.checkOut || fechaOffset(183)
  const body = {
    ciudad: card.busqueda?.ciudad || card.ciudad,
    pais:   card.busqueda?.pais   || card.pais,
    fechaCheckIn:  checkIn,
    fechaCheckOut: checkOut,
    cantidadPersonas: card.busqueda?.cantidadPersonas || 1,
  }
  buscandoHotel.value = idx ?? 0
  try {
    const r = await fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
    const resultados = r.ok ? await r.json() : []
    router.push({ path: '/resultados-hoteles', state: { resultados, busqueda: { ciudad: body.ciudad, pais: body.pais, checkIn, checkOut, cantidadPersonas: body.cantidadPersonas } } })
  } catch {
    prefillDest(card, 'hotels')
  } finally {
    buscandoHotel.value = -1
  }
}

/**
 * Navega a resultados de paquete.
 * Si el prefetch ya tiene resultados → navegación instantánea.
 * Si no → búsqueda en vivo (vuelo + hotel en paralelo) con spinner en la card.
 */
async function irPaquete(card, idx) {
  if (card.cargando || buscandoPaquete.value !== -1 || !card.ciudad) return
  if (card.resultadosVuelos || card.resultadosHoteles) {
    router.push({
      path: '/resultados-paquetes',
      state: { resultadosVuelos: card.resultadosVuelos || [], resultadosHoteles: card.resultadosHoteles || [], resultadosRegreso: [], busqueda: card.busqueda },
    })
    return
  }
  const { ciudad: oCiudad, pais: oPais } = origenData.value
  const fecha     = card.busqueda?.fecha       || fechaOffset(180)
  const checkIn   = card.busqueda?.checkIn     || fecha
  const checkOut  = card.busqueda?.checkOut    || fechaOffset(183)
  const destino     = card.busqueda?.destino     || card.ciudad
  const destinoPais = card.busqueda?.destinoPais || card.pais
  const bodyV = {
    origen: card.busqueda?.origen || oCiudad || 'Guatemala City',
    origenPais: card.busqueda?.origenPais || oPais || 'Guatemala',
    destino, destinoPais, fecha,
    cantidadPasajeros: card.busqueda?.cantidadPersonas || 1,
  }
  const bodyH = { ciudad: destino, pais: destinoPais, fechaCheckIn: checkIn, fechaCheckOut: checkOut, cantidadPersonas: card.busqueda?.cantidadPersonas || 1 }
  buscandoPaquete.value = idx ?? 0
  try {
    const [rV, rH] = await Promise.all([
      fetch(`${API}/api/busqueda/vuelos`,  { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyV) }),
      fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyH) }),
    ])
    const resultadosVuelos  = rV.ok ? await rV.json() : []
    const resultadosHoteles = rH.ok ? await rH.json() : []
    const busqueda = { origen: bodyV.origen, origenPais: bodyV.origenPais, destino, destinoPais, fecha, checkIn, checkOut, cantidadPersonas: bodyH.cantidadPersonas, tipoVuelo: 'ida' }
    router.push({ path: '/resultados-paquetes', state: { resultadosVuelos, resultadosHoteles, resultadosRegreso: [], busqueda } })
  } catch {
    prefillDest(card, 'combo')
  } finally {
    buscandoPaquete.value = -1
  }
}

// ── Helpers de navegación para recomendaciones ────────────────────────────────

/**
 * Busca vuelos hacia dest y navega a /resultados-vuelos.
 * Usa el origen del usuario; si no tiene, pre-llena el buscador.
 * @throws {Error} Si el fetch falla.
 */
async function _fetchVueloNav(dest) {
  const { ciudad: oCiudad, pais: oPais } = origenData.value
  const fecha = fechaOffset(7)
  if (!oCiudad || !oPais) { prefillDest(dest, 'flights'); return }
  const body = { origen: oCiudad, origenPais: oPais, destino: dest.ciudad, destinoPais: dest.pais, fecha, cantidadPasajeros: 1 }
  const res = await fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
  if (!res.ok) throw new Error()
  router.push({ path: '/resultados-vuelos', state: { resultados: await res.json(), busqueda: { ...body, tipoVuelo: 'ida' } } })
}

/**
 * Busca hoteles en dest y navega a /resultados-hoteles (check-in +7, check-out +10).
 * @throws {Error} Si el fetch falla.
 */
async function _fetchHotelNav(dest) {
  const checkIn = fechaOffset(7); const checkOut = fechaOffset(10)
  const body = { ciudad: dest.ciudad, pais: dest.pais, fechaCheckIn: checkIn, fechaCheckOut: checkOut, cantidadPersonas: 1 }
  const res = await fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
  if (!res.ok) throw new Error()
  router.push({ path: '/resultados-hoteles', state: { resultados: await res.json(), busqueda: { ciudad: dest.ciudad, pais: dest.pais, checkIn, checkOut, cantidadPersonas: 1 } } })
}

/**
 * Busca vuelos + hoteles en paralelo hacia dest y navega a /resultados-paquetes.
 * Usa el origen del usuario; si no tiene, pre-llena el buscador combo.
 * @throws {Error} Si los fetches fallan.
 */
async function _fetchPaqueteNav(dest) {
  const { ciudad: oCiudad, pais: oPais } = origenData.value
  const fecha = fechaOffset(7); const checkIn = fechaOffset(7); const checkOut = fechaOffset(10)
  if (!oCiudad || !oPais) { prefillDest(dest, 'combo'); return }
  const bodyV = { origen: oCiudad, origenPais: oPais, destino: dest.ciudad, destinoPais: dest.pais, fecha, cantidadPasajeros: 1 }
  const bodyH = { ciudad: dest.ciudad, pais: dest.pais, fechaCheckIn: checkIn, fechaCheckOut: checkOut, cantidadPersonas: 1 }
  const [resV, resH] = await Promise.all([
    fetch(`${API}/api/busqueda/vuelos`,  { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyV) }),
    fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyH) }),
  ])
  const resultadosVuelos  = resV.ok  ? await resV.json()  : []
  const resultadosHoteles = resH.ok  ? await resH.json()  : []
  router.push({
    path: '/resultados-paquetes',
    state: {
      resultadosVuelos, resultadosHoteles, resultadosRegreso: [],
      busqueda: { origen: oCiudad, origenPais: oPais, destino: dest.ciudad, destinoPais: dest.pais, fecha, checkIn, checkOut, cantidadPersonas: 1, tipoVuelo: 'ida' },
    },
  })
}

// ── Recomendaciones personalizadas ────────────────────────────────────────────

/** Ciudades alternativas por país para generar sugerencias similares. */
const SIMILARES_POR_PAIS = {
  'United States': ['New York', 'Orlando', 'Los Angeles', 'Las Vegas'],
  'Mexico':        ['Cancun', 'Mexico City', 'Playa del Carmen', 'Guadalajara'],
  'Colombia':      ['Medellin', 'Bogota', 'Cartagena', 'Cali'],
  'Spain':         ['Barcelona', 'Madrid', 'Seville', 'Valencia'],
  'Argentina':     ['Buenos Aires', 'Cordoba', 'Mendoza', 'Rosario'],
  'Peru':          ['Lima', 'Cusco', 'Arequipa', 'Iquitos'],
  'Brazil':        ['Rio de Janeiro', 'Sao Paulo', 'Salvador', 'Fortaleza'],
  'Chile':         ['Santiago', 'Valparaiso', 'Puerto Montt', 'Punta Arenas'],
  'Guatemala':     ['Antigua Guatemala', 'Quetzaltenango', 'Flores', 'Puerto Barrios'],
}

/** Busca vuelo hacia una recomendación y navega. */
async function explorarVueloRecom(rec, idx) {
  if (buscandoRecom.value !== -1) return
  buscandoRecom.value = idx
  try { await _fetchVueloNav(rec) } catch { prefillDest(rec, 'flights') }
  finally { buscandoRecom.value = -1 }
}

/** Busca hotel hacia una recomendación y navega. */
async function explorarHotelRecom(rec, idx) {
  if (buscandoRecom.value !== -1) return
  buscandoRecom.value = idx
  try { await _fetchHotelNav(rec) } catch { prefillDest(rec, 'hotels') }
  finally { buscandoRecom.value = -1 }
}

/** Busca paquete hacia una recomendación y navega. */
async function explorarPaqueteRecom(rec, idx) {
  if (buscandoRecom.value !== -1) return
  buscandoRecom.value = idx
  try { await _fetchPaqueteNav(rec) } catch { prefillDest(rec, 'combo') }
  finally { buscandoRecom.value = -1 }
}

// ── Reservaciones recientes + Recomendaciones (5ta sección) ───────────────────

/** Últimas 3 reservaciones del usuario para el panel rápido en home. */
const reservacionesRecientes = ref([])

/** Conteo de reservaciones del usuario por tipo. */
const misReservas   = ref({ vuelos: 0, hoteles: 0, paquetes: 0, total: 0 })
const resumenUsuario = ref({ totalGastado: '0.00', tipoFavorito: '—', destinoFavorito: '' })

/** Recomendaciones derivadas de la primera reservación del usuario. */
const recomendaciones = ref([])

/**
 * Convierte tipo_reserva a etiqueta y clase CSS.
 * @param {number} tipo - 1=Vuelo, 2=Hotel, 3=Paquete.
 */
function tipoLabel(tipo) {
  if (tipo === 1) return { texto: 'Vuelo',     clase: 'vuelo'   }
  if (tipo === 2) return { texto: 'Hospedaje', clase: 'hotel'   }
  return                 { texto: 'Paquete',   clase: 'paquete' }
}

/**
 * Convierte estado_id a etiqueta y clase CSS.
 * EstadoID: 1=Pendiente, 2=Confirmada, 3=En Curso, 4=Completada, 5=Cancelada, 6=Expirada, 7=Retenida
 */
function estadoLabel(estado) {
  if (estado === 1) return { texto: 'Pendiente',   clase: 'pending' }
  if (estado === 2) return { texto: 'Confirmada',  clase: 'ok'      }
  if (estado === 3) return { texto: 'En Curso',    clase: 'ok'      }
  if (estado === 4) return { texto: 'Completada',  clase: 'ok'      }
  if (estado === 5) return { texto: 'Cancelada',   clase: 'cancel'  }
  if (estado === 6) return { texto: 'Expirada',    clase: 'cancel'  }
  if (estado === 7) return { texto: 'Retenida',    clase: 'pending' }
  return                   { texto: 'Desconocido', clase: 'pending' }
}

/**
 * Carga reservaciones del usuario en una sola petición y popula tanto
 * `reservacionesRecientes` (últimas 3) como `recomendaciones` (basadas en
 * el destino de la primera reservación).
 */
async function cargarDatosUsuario() {
  if (!usuarioLogueado.value) return
  try {
    const res = await fetch(`${API}/api/reservaciones/mias`, { credentials: 'include' })
    if (!res.ok) return
    const data = await res.json()
    if (!Array.isArray(data)) return

    // Conteo por tipo (datos completos antes de slicear)
    const nVuelos   = data.filter(r => r.tipo_reserva === 1).length
    const nHoteles  = data.filter(r => r.tipo_reserva === 2).length
    const nPaquetes = data.filter(r => r.tipo_reserva === 3).length
    misReservas.value = { vuelos: nVuelos, hoteles: nHoteles, paquetes: nPaquetes, total: data.length }

    // Total gastado
    const totalGastado = data.reduce((s, r) => s + (r.total || 0), 0)

    // Tipo favorito (el que más tiene)
    const tipoMax = nPaquetes >= nVuelos && nPaquetes >= nHoteles ? 'Paquete'
                  : nVuelos  >= nHoteles                          ? 'Vuelo'
                  : 'Hotel'

    // Destino más frecuente (desde parametros_json de los detalles)
    const destConteo = {}
    for (const r of data) {
      if (r.detalles?.length > 0) {
        const p = r.detalles[0].parametros_json
        if (p && typeof p === 'object') {
          const d = p.destino || p.ciudad || ''
          if (d) destConteo[d] = (destConteo[d] || 0) + 1
        }
      }
    }
    const destEntries = Object.entries(destConteo)
    const destinoFavorito = destEntries.length
      ? destEntries.reduce((a, b) => b[1] > a[1] ? b : a)[0]
      : ''

    resumenUsuario.value = {
      totalGastado: totalGastado.toLocaleString('es-GT', { minimumFractionDigits: 2 }),
      tipoFavorito: tipoMax,
      destinoFavorito,
    }

    // Últimas 3 para el panel recientes
    reservacionesRecientes.value = data.slice(0, 3)

    // Recomendaciones basadas en la primera reservación
    if (data.length === 0) return
    let destCity = '', destPais = ''
    const primera = data[0]
    if (primera.detalles?.length > 0) {
      const params = primera.detalles[0].parametros_json
      if (params && typeof params === 'object') {
        destCity = params.destino || params.ciudad || ''
        destPais = params.destinoPais || params.pais || ''
      }
    }
    if (!destCity || !destPais) return

    const similares = (SIMILARES_POR_PAIS[destPais] || []).filter(c => c !== destCity)
    recomendaciones.value = [
      { tipo: 'vuelo',   ciudad: destCity,          pais: destPais, descuento: 15 },
      { tipo: 'hotel',   ciudad: similares[0] || destCity, pais: destPais, descuento: 20 },
      { tipo: 'paquete', ciudad: similares[1] || destCity, pais: destPais, descuento: 12 },
      { tipo: 'vuelo',   ciudad: similares[2] || destCity, pais: destPais, descuento: 18 },
    ]
  } catch { /* silencioso */ }
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll)

  try {
    const res = await fetch(`${API}/api/stats`)
    if (res.ok) stats.value = await res.json()
  } catch {}

  await cargarPaneles()
  prefetchDisponibles()        // fire & forget (no await)
  cargarDatosUsuario()         // fire & forget (no await)
})

onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>
