<template>
  <div class="page">
    <Encabezado />

    <!-- HERO -->
    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">Tu Próxima Aventura<br>Comienza <span>Aquí</span></h1>
          <p class="hero-subtitle">Vuelos, hospedajes y paquetes combinados de múltiples proveedores en un solo lugar</p>
          <div class="hero-stats">
            <div class="hero-stat"><strong>500+</strong><span>Aerolíneas</span></div>
            <div class="hero-stat"><strong>12K+</strong><span>Hoteles</span></div>
            <div class="hero-stat"><strong>180+</strong><span>Países</span></div>
            <div class="hero-stat"><strong>98%</strong><span>Satisfacción</span></div>
          </div>
        </div>

        <div class="search-card">
          <h2 class="search-card-title">¿A dónde viajamos?</h2>

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

          <div :class="['vuelos-cards', { 'vuelos-cards--solo': searchType === 'hotels' }]">

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

          <!-- TAB VUELOS -->
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

          <!-- TAB HOTELES -->
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

          <!-- TAB COMBO -->
          <template v-if="searchType === 'combo'">
            <div class="combo-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
              Vuelo
            </div>
            <!-- Toggle ida / ida y vuelta -->
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

            <!-- Sección hotel — fechas siempre dentro del rango de vuelo -->
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
                <input
                  class="form-input"
                  type="date"
                  v-model="comboData.checkIn"
                  :min="comboData.fecha || hoy"
                  :max="maxCheckInCombo || undefined"
                  :disabled="!comboData.fecha"
                />
              </div>
              <div class="form-group">
                <label class="form-label">
                  <svg class="label-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  Check-out
                </label>
                <input
                  class="form-input"
                  type="date"
                  v-model="comboData.checkOut"
                  :min="minCheckOutCombo"
                  :max="comboTipoVuelo === 'idaVuelta' ? (comboData.fechaRegreso || undefined) : undefined"
                  :disabled="!comboData.checkIn"
                />
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

    <!-- FEATURES -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">¿Por qué elegir Movent?</h2>
          <p class="section-description">Experiencias de viaje excepcionales con los mejores proveedores del mundo</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="f in features" :key="f.title">
            <div class="feature-icon" v-html="f.icon"></div>
            <h3 class="feature-title">{{ f.title }}</h3>
            <p class="feature-description">{{ f.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">¿Listo para tu próxima aventura?</h2>
          <p class="cta-description">Únete a miles de viajeros que confían en Movent para sus experiencias de viaje</p>
          <div class="cta-buttons">
            <button class="cta-btn primary" type="button" @click="$router.push('/resultados-paquetes')">
              Explorar Paquetes
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
            </button>
            <button class="cta-btn secondary" type="button" @click="$router.push('/informacion')">Centro de Ayuda</button>
          </div>
        </div>
      </div>
    </section>

    <button v-if="showScrollTop" class="scroll-top" type="button" @click="scrollToTop">
      <svg viewBox="0 0 24 24" class="avion-icon">
        <path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z" />
      </svg>
    </button>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/principal.css'

const router      = useRouter()
const API         = 'http://localhost:8080'
const buscando    = ref(false)
const searchError = ref('')

const searchType     = ref('flights')
const showScrollTop  = ref(false)
const hoy            = new Date().toISOString().split('T')[0]
const tipoVuelo      = ref('ida')
const comboTipoVuelo = ref('ida')

const flightData = ref({ fecha: '', fechaRegreso: '', cantidadPasajeros: 1 })
const hotelData  = ref({ checkIn: '', checkOut: '', cantidadPersonas: 1 })
const comboData  = ref({ fecha: '', fechaRegreso: '', cantidadPersonas: 1, checkIn: '', checkOut: '' })

// ── Computed: fechas mínimas para vuelos ──────────────────────
const minFechaRegreso = computed(() => {
  if (!flightData.value.fecha) return hoy
  const d = new Date(flightData.value.fecha); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

const minFechaRegresoCombo = computed(() => {
  if (!comboData.value.fecha) return hoy
  const d = new Date(comboData.value.fecha); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

const minCheckOutHotel = computed(() => {
  if (!hotelData.value.checkIn) return hoy
  const d = new Date(hotelData.value.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

// ── Computed: límites del hotel DENTRO del rango de vuelo ─────

// checkIn hotel: máximo = día anterior al checkOut elegido
// o día anterior al regreso si no hay checkOut aún (solo idaVuelta)
const maxCheckInCombo = computed(() => {
  if (comboTipoVuelo.value === 'idaVuelta' && comboData.value.fechaRegreso) {
    const base = comboData.value.checkOut || comboData.value.fechaRegreso
    const d = new Date(base)
    d.setDate(d.getDate() - 1)
    return d.toISOString().split('T')[0]
  }
  return undefined
})

// checkOut hotel: mínimo = checkIn + 1 día
const minCheckOutCombo = computed(() => {
  if (!comboData.value.checkIn) return hoy
  const d = new Date(comboData.value.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

// ── Watchers combo: hotel siempre se sincroniza con vuelo ─────

// Cuando cambia la fecha de IDA → checkIn = esa fecha (siempre)
watch(() => comboData.value.fecha, (nuevaFecha) => {
  if (!nuevaFecha) return

  // Siempre fijar checkIn a la fecha del vuelo de ida
  comboData.value.checkIn = nuevaFecha

  // Si checkOut quedó inválido (≤ nuevo checkIn), resetearlo
  if (comboData.value.checkOut && comboData.value.checkOut <= nuevaFecha) {
    comboData.value.checkOut = ''
  }
})

// Cuando cambia la fecha de REGRESO → checkOut = esa fecha (siempre)
watch(() => comboData.value.fechaRegreso, (nuevaFechaRegreso) => {
  if (!nuevaFechaRegreso || comboTipoVuelo.value !== 'idaVuelta') return

  // Siempre fijar checkOut a la fecha de regreso
  comboData.value.checkOut = nuevaFechaRegreso

  // Si checkIn quedó igual o posterior al regreso, volver al día de ida
  if (comboData.value.checkIn && comboData.value.checkIn >= nuevaFechaRegreso) {
    comboData.value.checkIn = comboData.value.fecha || ''
  }
})

// Cuando cambia el tipo a solo-ida: limpiar regreso y checkOut
watch(() => comboTipoVuelo.value, (tipo) => {
  if (tipo === 'ida') {
    comboData.value.fechaRegreso = ''
    comboData.value.checkOut = ''
  }
})

// Cuando el usuario ajusta checkIn manualmente: validar checkOut
watch(() => comboData.value.checkIn, (nuevoCheckIn) => {
  if (!nuevoCheckIn) return
  if (comboData.value.checkOut && comboData.value.checkOut <= nuevoCheckIn) {
    comboData.value.checkOut = ''
  }
})

// ── Helper de formato de fecha corta para el hint ─────────────
function formatFechaCorta(f) {
  if (!f) return ''
  try {
    return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short' })
  } catch { return f }
}

// ── Autocomplete países/ciudades ──────────────────────────────
let paisesCache = null
async function getPaises() {
  if (paisesCache) return paisesCache
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); paisesCache = d.data || [] } catch { paisesCache = [] }
  return paisesCache
}
async function getCiudades(country) {
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ country }) }); const d = await r.json(); return d.data || [] } catch { return [] }
}
function blur(fn) { setTimeout(fn, 200) }

// ── Origen ────────────────────────────────────────────────────
const oPaisQ = ref(''); const oPaisSug = ref([]); const oPaisSel = ref(null)
const oCiudadQ = ref(''); const oCiudadSug = ref([]); const oCiudadLoading = ref(false)
const oCiudades = ref([])
const origen = ref({ pais: '', ciudad: '' })

async function onOPaisInput() {
  oPaisSel.value = null; oCiudadQ.value = ''; oCiudades.value = []; origen.value = { pais: '', ciudad: '' }
  const q = oPaisQ.value.trim(); if (q.length < 2) { oPaisSug.value = []; return }
  oPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selOPais(p) {
  oPaisSel.value = p; oPaisQ.value = p.country; oPaisSug.value = []; origen.value.pais = p.country
  oCiudadLoading.value = true; oCiudades.value = await getCiudades(p.country); oCiudadLoading.value = false
}
function onOCiudadInput() {
  const q = oCiudadQ.value.toLowerCase()
  oCiudadSug.value = q.length < 2 ? [] : oCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  origen.value.ciudad = ''
}
function selOCiudad(c) { oCiudadQ.value = c; oCiudadSug.value = []; origen.value.ciudad = c; searchError.value = '' }

// ── Destino ───────────────────────────────────────────────────
const dPaisQ = ref(''); const dPaisSug = ref([]); const dPaisSel = ref(null)
const dCiudadQ = ref(''); const dCiudadSug = ref([]); const dCiudadLoading = ref(false)
const dCiudades = ref([])
const destino = ref({ pais: '', ciudad: '' })

async function onDPaisInput() {
  dPaisSel.value = null; dCiudadQ.value = ''; dCiudades.value = []; destino.value = { pais: '', ciudad: '' }
  const q = dPaisQ.value.trim(); if (q.length < 2) { dPaisSug.value = []; return }
  dPaisSug.value = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selDPais(p) {
  dPaisSel.value = p; dPaisQ.value = p.country; dPaisSug.value = []; destino.value.pais = p.country
  dCiudadLoading.value = true; dCiudades.value = await getCiudades(p.country); dCiudadLoading.value = false
}
function onDCiudadInput() {
  const q = dCiudadQ.value.toLowerCase()
  dCiudadSug.value = q.length < 2 ? [] : dCiudades.value.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  destino.value.ciudad = ''
}
function selDCiudad(c) { dCiudadQ.value = c; dCiudadSug.value = []; destino.value.ciudad = c; searchError.value = '' }

// ── Helpers de validación de resultados ───────────────────────
function tieneVuelos(respuesta) {
  if (!Array.isArray(respuesta) || respuesta.length === 0) return false
  return respuesta.some(b => b.datos && (
    (b.datos.directos?.length  > 0) ||
    (b.datos.conEscala?.length > 0)
  ))
}

function tieneHoteles(respuesta) {
  if (!Array.isArray(respuesta) || respuesta.length === 0) return false
  return respuesta.some(b => Array.isArray(b.datos) && b.datos.length > 0)
}

// ── Buscar Vuelos ─────────────────────────────────────────────
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

      if (!tieneVuelos(resultados)) {
        searchError.value = `No hay vuelos de ${origen.value.ciudad} a ${destino.value.ciudad} para el ${flightData.value.fecha}.`
        return
      }
      if (!tieneVuelos(resultadosRegreso)) {
        searchError.value = `No hay vuelos de regreso de ${destino.value.ciudad} a ${origen.value.ciudad} para el ${flightData.value.fechaRegreso}. Prueba otra fecha de regreso.`
        return
      }

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

// ── Buscar Hoteles ────────────────────────────────────────────
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
  } catch (err) {
    searchError.value = 'No se pudieron obtener hoteles. Intenta de nuevo.'
  } finally { buscando.value = false }
}

// ── Buscar Paquetes ───────────────────────────────────────────
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

  if (comboData.value.checkIn < comboData.value.fecha) {
    searchError.value = `El check-in del hotel no puede ser antes de la salida del vuelo (${formatFechaCorta(comboData.value.fecha)}).`
    return
  }
  if (comboTipoVuelo.value === 'idaVuelta' && comboData.value.fechaRegreso) {
    if (comboData.value.checkOut > comboData.value.fechaRegreso) {
      searchError.value = `El check-out del hotel no puede ser después del vuelo de regreso (${formatFechaCorta(comboData.value.fechaRegreso)}).`
      return
    }
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
  } catch (err) {
    searchError.value = 'No se pudieron obtener los paquetes. Intenta de nuevo.'
  } finally { buscando.value = false }
}

// ── Features ──────────────────────────────────────────────────
const features = [
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>`, title: 'Vuelos Globales', description: 'Accede a vuelos de múltiples aerolíneas con las mejores tarifas garantizadas desde cualquier destino.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`, title: 'Hospedaje Premium', description: 'Desde hoteles boutique hasta resorts de lujo, encuentra el alojamiento perfecto para tu viaje.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`, title: 'Atención Experta', description: 'Nuestro equipo de asesores está disponible para ayudarte a planificar cada detalle de tu viaje.' },
  { icon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>`, title: 'Mejor Precio', description: 'Comparamos precios de múltiples proveedores para ofrecerte siempre la mejor tarifa disponible.' },
]

const onScroll    = () => { showScrollTop.value = window.scrollY > 300 }
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>