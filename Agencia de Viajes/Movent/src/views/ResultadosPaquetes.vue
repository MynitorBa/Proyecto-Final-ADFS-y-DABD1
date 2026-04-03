<template>
  <div class="page">
    <Encabezado />

    <div class="rp-page">
      <div class="rp-layout">

        <!-- ═══ SIDEBAR (se adapta al paso actual) ═══ -->
        <aside class="rp-sidebar" :class="{ 'rp-sidebar--collapsed': sidebarColapsado }">
          <div class="rp-sidebar__head" @click="sidebarColapsado = !sidebarColapsado">
            <h3 class="rp-sidebar__title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="11" y1="18" x2="13" y2="18"/>
              </svg>
              Filtros {{ paso === 1 ? 'vuelos' : 'hoteles' }}
              <span v-if="cantFiltrosActivos > 0" class="rp-sidebar__badge">{{ cantFiltrosActivos }}</span>
            </h3>
            <button class="rp-sidebar__toggle" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"
                :style="{ transform: sidebarColapsado ? 'rotate(0deg)' : 'rotate(180deg)', transition: 'transform .2s' }">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
          </div>

          <div class="rp-sidebar__body">

            <!-- ── FILTROS VUELO (paso 1) ── -->
            <template v-if="paso === 1">
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Precio por persona</h4>
                <div class="rp-price-inputs">
                  <div class="rp-price-input"><span>$</span><input type="number" v-model.number="fv.precioMin" :min="0" placeholder="0" /></div>
                  <span class="rp-price-sep">—</span>
                  <div class="rp-price-input"><span>$</span><input type="number" v-model.number="fv.precioMax" placeholder="9999" /></div>
                </div>
              </div>
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Clase</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="c in clasesFilter" :key="c.val">
                    <input type="checkbox" v-model="fv.clases" :value="c.val" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ c.label }}</span>
                  </label>
                </div>
              </div>
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Escalas</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="e in escalasOpts" :key="e.val">
                    <input type="checkbox" v-model="fv.escalas" :value="e.val" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ e.label }}</span>
                  </label>
                </div>
              </div>
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Duración máxima</h4>
                <div class="rp-dur-btns">
                  <button v-for="d in duracionOpts" :key="d.val"
                    :class="['rp-dur-btn', { 'rp-dur-btn--active': fv.duracionMax === d.val }]"
                    @click="fv.duracionMax = fv.duracionMax === d.val ? 9999 : d.val" type="button">{{ d.label }}</button>
                </div>
              </div>
              <div class="rp-filter-group" v-if="aerolineasDisponibles.length">
                <h4 class="rp-filter-group__title">Aerolínea</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="a in aerolineasDisponibles" :key="a">
                    <input type="checkbox" v-model="fv.aerolineas" :value="a" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ a }}</span>
                  </label>
                </div>
              </div>
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Horario de salida</h4>
                <div class="rp-horarios">
                  <button v-for="h in horariosOpts" :key="h.val"
                    :class="['rp-horario-btn', { 'rp-horario-btn--active': fv.horario === h.val }]"
                    @click="fv.horario = fv.horario === h.val ? '' : h.val" type="button">
                    <span v-html="h.icon"></span>
                    <span>{{ h.label }}</span>
                    <small>{{ h.rango }}</small>
                  </button>
                </div>
              </div>
            </template>

            <!-- ── FILTROS HOTEL (paso 2+) ── -->
            <template v-else>
              <div class="rp-filter-group">
                <h4 class="rp-filter-group__title">Precio por noche</h4>
                <div class="rp-price-inputs">
                  <div class="rp-price-input"><span>$</span><input type="number" v-model.number="fh.precioMin" :min="0" placeholder="0" /></div>
                  <span class="rp-price-sep">—</span>
                  <div class="rp-price-input"><span>$</span><input type="number" v-model.number="fh.precioMax" placeholder="9999" /></div>
                </div>
              </div>
              <div class="rp-filter-group" v-if="tiposHabitacionDisponibles.length">
                <h4 class="rp-filter-group__title">Tipo de habitación</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="t in tiposHabitacionDisponibles" :key="t">
                    <input type="checkbox" v-model="fh.tipos" :value="t" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ t }}</span>
                  </label>
                </div>
              </div>
              <div class="rp-filter-group" v-if="hotelesDisponibles.length">
                <h4 class="rp-filter-group__title">Hotel</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="h in hotelesDisponibles" :key="h">
                    <input type="checkbox" v-model="fh.hoteles" :value="h" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ h }}</span>
                  </label>
                </div>
              </div>
              <div class="rp-filter-group" v-if="amenidadesDisponibles.length">
                <h4 class="rp-filter-group__title">Amenidades</h4>
                <div class="rp-checkboxes">
                  <label class="rp-checkbox" v-for="a in amenidadesDisponibles" :key="a">
                    <input type="checkbox" v-model="fh.amenidades" :value="a" />
                    <span class="rp-checkbox__box"></span>
                    <span class="rp-checkbox__label">{{ a }}</span>
                  </label>
                </div>
              </div>
            </template>

            <button v-if="cantFiltrosActivos > 0" class="rp-btn rp-btn--ghost rp-sidebar__reset" @click="resetFiltros" type="button">Limpiar filtros</button>
          </div>
        </aside>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rp-main">

          <!-- ── SEARCH BAR ── -->
          <div class="rp-search-bar" :class="{ 'rp-search-bar--open': modificarAbierto }">
            <div class="rp-search-bar__summary" @click="toggleModificar">
              <div class="rp-search-bar__ruta">
                <span class="rp-search-bar__ciudad">{{ busqueda.origen }}</span>
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14">
                  <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>
                </svg>
                <span class="rp-search-bar__ciudad">{{ busqueda.destino }}</span>
              </div>
              <div class="rp-search-bar__detalles">
                <span class="rp-search-bar__tag">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="11" height="11"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                  Vuelo {{ formatFecha(busqueda.fecha) }}
                </span>
                <span class="rp-search-bar__dot">·</span>
                <span class="rp-search-bar__tag">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                  Hotel {{ formatFecha(busqueda.checkIn) }} – {{ formatFecha(busqueda.checkOut) }}
                </span>
                <span class="rp-search-bar__dot">·</span>
                <span class="rp-search-bar__tag">{{ busqueda.cantidadPersonas }} pers. · {{ noches }} noches</span>
              </div>
            </div>
            <button class="rp-search-bar__mod-btn" @click="toggleModificar" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"
                :style="{ transform: modificarAbierto ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
              {{ modificarAbierto ? 'Cerrar' : 'Modificar' }}
            </button>
          </div>

          <!-- ── FORM MODIFICAR INLINE ── -->
          <transition name="rp-expand">
            <div v-if="modificarAbierto" class="rp-modificar-inline">
              <div class="rp-modificar-grid">

                <div class="rp-mod-section">
                  <p class="rp-mod-section__label">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                    Origen
                  </p>
                  <div class="rp-mod-row">
                    <div class="rp-mod-field rp-ac-wrap">
                      <label class="rp-mod-label">País</label>
                      <input class="rp-mod-input" type="text" v-model="form.oPaisQ"
                        @input="onOPaisInput" @blur="blurClose(() => form.oPaisSug = [])"
                        placeholder="Guatemala..." autocomplete="off" />
                      <ul v-if="form.oPaisSug.length" class="rp-ac-list">
                        <li v-for="p in form.oPaisSug" :key="p.country"><button type="button" @click="selOPais(p)">{{ p.country }}</button></li>
                      </ul>
                    </div>
                    <div class="rp-mod-field rp-ac-wrap">
                      <label class="rp-mod-label">Ciudad <span v-if="form.oCiudadLoading" class="rp-mod-hint">cargando...</span></label>
                      <input class="rp-mod-input" type="text" v-model="form.oCiudadQ"
                        @input="onOCiudadInput" @blur="blurClose(() => form.oCiudadSug = [])"
                        :disabled="!form.oPaisSel || form.oCiudadLoading"
                        placeholder="Guatemala City..." autocomplete="off" />
                      <ul v-if="form.oCiudadSug.length" class="rp-ac-list">
                        <li v-for="c in form.oCiudadSug" :key="c"><button type="button" @click="selOCiudad(c)">{{ c }}</button></li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="rp-mod-section">
                  <p class="rp-mod-section__label">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    Destino
                  </p>
                  <div class="rp-mod-row">
                    <div class="rp-mod-field rp-ac-wrap">
                      <label class="rp-mod-label">País</label>
                      <input class="rp-mod-input" type="text" v-model="form.dPaisQ"
                        @input="onDPaisInput" @blur="blurClose(() => form.dPaisSug = [])"
                        placeholder="Mexico..." autocomplete="off" />
                      <ul v-if="form.dPaisSug.length" class="rp-ac-list">
                        <li v-for="p in form.dPaisSug" :key="p.country"><button type="button" @click="selDPais(p)">{{ p.country }}</button></li>
                      </ul>
                    </div>
                    <div class="rp-mod-field rp-ac-wrap">
                      <label class="rp-mod-label">Ciudad <span v-if="form.dCiudadLoading" class="rp-mod-hint">cargando...</span></label>
                      <input class="rp-mod-input" type="text" v-model="form.dCiudadQ"
                        @input="onDCiudadInput" @blur="blurClose(() => form.dCiudadSug = [])"
                        :disabled="!form.dPaisSel || form.dCiudadLoading"
                        placeholder="Mexico City..." autocomplete="off" />
                      <ul v-if="form.dCiudadSug.length" class="rp-ac-list">
                        <li v-for="c in form.dCiudadSug" :key="c"><button type="button" @click="selDCiudad(c)">{{ c }}</button></li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="rp-mod-section rp-mod-section--row">
                  <div class="rp-mod-field" style="grid-column:1/-1;">
                    <div class="rv-trip-toggle">
                      <button :class="['rv-trip-btn', { 'rv-trip-btn--active': form.tipoVuelo === 'ida' }]" @click="form.tipoVuelo = 'ida'; form.fechaRegreso = ''" type="button">Solo ida</button>
                      <button :class="['rv-trip-btn', { 'rv-trip-btn--active': form.tipoVuelo === 'idaVuelta' }]" @click="form.tipoVuelo = 'idaVuelta'" type="button">Ida y vuelta</button>
                    </div>
                  </div>
                  <div class="rp-mod-field">
                    <label class="rp-mod-label">Fecha vuelo</label>
                    <input class="rp-mod-input" type="date" v-model="form.fecha" :min="hoy" />
                  </div>
                  <div class="rp-mod-field" v-if="form.tipoVuelo === 'idaVuelta'">
                    <label class="rp-mod-label">Fecha regreso vuelo</label>
                    <input class="rp-mod-input" type="date" v-model="form.fechaRegreso"
                      :min="form.fecha ? (() => { const d = new Date(form.fecha); d.setDate(d.getDate()+1); return d.toISOString().split('T')[0] })() : hoy" />
                  </div>
                  <div class="rp-mod-field">
                    <label class="rp-mod-label">Check-in</label>
                    <input class="rp-mod-input" type="date" v-model="form.checkIn" :min="hoy" />
                  </div>
                  <div class="rp-mod-field">
                    <label class="rp-mod-label">Check-out</label>
                    <input class="rp-mod-input" type="date" v-model="form.checkOut" :min="minCheckOut" />
                  </div>
                  <div class="rp-mod-field">
                    <label class="rp-mod-label">Personas</label>
                    <select class="rp-mod-input" v-model="form.cantidadPersonas">
                      <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Persona' : 'Personas' }}</option>
                    </select>
                  </div>
                  <div class="rp-mod-field rp-mod-field--cta">
                    <p v-if="modError" class="rp-mod-error">⚠ {{ modError }}</p>
                    <button class="rp-mod-buscar" @click="rebuscar" :disabled="buscando" type="button">
                      <div v-if="buscando" class="rp-spinner rp-spinner--sm"></div>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
                      {{ buscando ? 'Buscando...' : 'Buscar paquetes' }}
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </transition>

          <!-- ── INDICADOR DE PASOS ── -->
          <div v-if="!loading" class="rp-pasos">
            <div :class="['rp-paso', { 'rp-paso--activo': paso === 1, 'rp-paso--done': paso > 1 }]">
              <span class="rp-paso__num">{{ paso > 1 ? '✓' : '1' }}</span>
              <div>
                <span class="rp-paso__label">Vuelo de ida</span>
                <span class="rp-paso__sub">{{ busqueda.origen }} → {{ busqueda.destino }} · {{ formatFecha(busqueda.fecha) }}</span>
              </div>
            </div>
            <div class="rp-paso__sep">→</div>
            <template v-if="esIdaVuelta">
              <div :class="['rp-paso', { 'rp-paso--activo': paso === 2, 'rp-paso--done': paso > 2, 'rp-paso--pendiente': paso < 2 }]">
                <span class="rp-paso__num">{{ paso > 2 ? '✓' : '2' }}</span>
                <div>
                  <span class="rp-paso__label">Vuelo de regreso</span>
                  <span class="rp-paso__sub">{{ busqueda.destino }} → {{ busqueda.origen }} · {{ formatFecha(busqueda.fechaRegreso) }}</span>
                </div>
              </div>
              <div class="rp-paso__sep">→</div>
            </template>
            <div :class="['rp-paso', { 'rp-paso--activo': paso === pasoHotel, 'rp-paso--pendiente': paso < pasoHotel }]">
              <span class="rp-paso__num">{{ esIdaVuelta ? '3' : '2' }}</span>
              <div>
                <span class="rp-paso__label">Hospedaje</span>
                <span class="rp-paso__sub">{{ busqueda.destino }} · {{ formatFecha(busqueda.checkIn) }} – {{ formatFecha(busqueda.checkOut) }}</span>
              </div>
            </div>
            <button v-if="paso > 1" class="rp-btn rp-btn--ghost rp-paso__back" @click="retroceder" type="button">
              ← Atrás
            </button>
          </div>

          <!-- ── VUELO SELECCIONADO (pasos 2 y 3) ── -->
          <div v-if="paso >= 2 && vueloSel" class="rp-vuelo-sel">
            <div class="rp-vuelo-sel__icon">
              <svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
            </div>
            <div class="rp-vuelo-sel__info">
              <span class="rp-vuelo-sel__ruta">{{ vueloSel.origenCodigo }} → {{ vueloSel.destinoCodigo }}</span>
              <span class="rp-vuelo-sel__meta">{{ vueloSel.aerolinea }} · {{ vueloSel.clase === 'ejecutiva' ? 'Ejecutiva' : 'Económica' }} · {{ formatFecha(busqueda.fecha) }}</span>
            </div>
            <div class="rp-vuelo-sel__precio">
              <span class="rp-vuelo-sel__lbl">Ida</span>
              <span class="rp-vuelo-sel__val">${{ (vueloSel.precio * busqueda.cantidadPersonas).toFixed(2) }}</span>
            </div>
            <template v-if="vueloRegresoSel">
              <div style="width:1px;background:rgba(255,255,255,0.1);align-self:stretch;margin:0 4px;flex-shrink:0;"></div>
              <div class="rp-vuelo-sel__icon">
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18" style="transform:scaleX(-1)"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
              </div>
              <div class="rp-vuelo-sel__info">
                <span class="rp-vuelo-sel__ruta">{{ vueloRegresoSel.origenCodigo }} → {{ vueloRegresoSel.destinoCodigo }}</span>
                <span class="rp-vuelo-sel__meta">{{ vueloRegresoSel.aerolinea }} · {{ formatFecha(busqueda.fechaRegreso) }}</span>
              </div>
              <div class="rp-vuelo-sel__precio">
                <span class="rp-vuelo-sel__lbl">Regreso</span>
                <span class="rp-vuelo-sel__val">${{ (vueloRegresoSel.precio * busqueda.cantidadPersonas).toFixed(2) }}</span>
              </div>
            </template>
          </div>

          <!-- Banda vuelo regreso (en paso 2 de idaVuelta) -->
          <div v-if="esIdaVuelta && paso === 2 && !vueloRegresoSel" class="rp-regreso-header">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="16" height="16"><path d="M7 23l-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
            Elige tu vuelo de regreso — {{ busqueda.destino }} → {{ busqueda.origen }} · {{ formatFecha(busqueda.fechaRegreso) }}
          </div>

          <!-- ── RESUMEN FINAL ── -->
          <div v-if="vueloSel && (!esIdaVuelta || vueloRegresoSel) && hotelSel" class="rp-resumen">
            <div class="rp-resumen__col">
              <span class="rp-resumen__lbl">✈ Ida</span>
              <span class="rp-resumen__val">${{ (vueloSel.precio * busqueda.cantidadPersonas).toFixed(2) }}</span>
            </div>
            <template v-if="esIdaVuelta && vueloRegresoSel">
              <div class="rp-resumen__sep">+</div>
              <div class="rp-resumen__col">
                <span class="rp-resumen__lbl">✈ Regreso</span>
                <span class="rp-resumen__val">${{ (vueloRegresoSel.precio * busqueda.cantidadPersonas).toFixed(2) }}</span>
              </div>
            </template>
            <div class="rp-resumen__sep">+</div>
            <div class="rp-resumen__col">
              <span class="rp-resumen__lbl">🏨 Hotel ({{ noches }}n)</span>
              <span class="rp-resumen__val">${{ (hotelSel.precioNoche * noches).toFixed(2) }}</span>
            </div>
            <div class="rp-resumen__sep">=</div>
            <div class="rp-resumen__col rp-resumen__col--total">
              <span class="rp-resumen__lbl">Paquete total</span>
              <span class="rp-resumen__total">${{ precioTotal.toFixed(2) }}</span>
            </div>
            <button class="rp-btn rp-btn--yellow rp-resumen__cta" @click="reservarPaquete" type="button">
              Reservar paquete
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
          </div>

          <!-- ── TOOLBAR ── -->
          <div class="rp-toolbar">
            <p class="rp-toolbar__count">
              <strong>{{ paso < pasoHotel ? (paso === 1 ? vuelosFiltrados.length : vuelosRegreso.length) : gruposPorHotel.length }}</strong>
              {{ paso < pasoHotel
                ? `vuelo${(paso === 1 ? vuelosFiltrados.length : vuelosRegreso.length) !== 1 ? 's' : ''} disponible${(paso === 1 ? vuelosFiltrados.length : vuelosRegreso.length) !== 1 ? 's' : ''}`
                : `hotel${gruposPorHotel.length !== 1 ? 'es' : ''} disponible${gruposPorHotel.length !== 1 ? 's' : ''}` }}
            </p>
            <div class="rp-sort">
              <select v-if="paso < pasoHotel" v-model="ordenVuelos" class="rp-sort__select">
                <option value="precio-asc">Precio ↑</option>
                <option value="precio-desc">Precio ↓</option>
                <option value="duracion">Menor duración</option>
                <option value="salida">Hora salida</option>
                <option value="escalas">Menos escalas</option>
              </select>
              <select v-else v-model="ordenHoteles" class="rp-sort__select">
                <option value="precio-asc">Precio: menor a mayor</option>
                <option value="precio-desc">Precio: mayor a menor</option>
                <option value="capacidad">Mayor capacidad</option>
              </select>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="rp-empty">
            <div class="rp-spinner"></div>
            <p>Buscando vuelos y hoteles...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="rp-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5"/><circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/></svg>
            <p class="rp-empty__title">{{ error }}</p>
            <button class="rp-btn rp-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
          </div>

          <!-- ══════════════════════════
               PASOS 1 Y 2: VUELOS
          ══════════════════════════ -->
          <template v-if="!loading && !error && paso < pasoHotel">
            <template v-if="paso === 1">
              <div v-if="vuelos.length === 0" class="rp-empty">
                <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="48" height="48"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                <p class="rp-empty__title">Sin vuelos disponibles</p>
                <button class="rp-btn rp-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
              </div>
              <div v-else-if="vuelosFiltrados.length === 0" class="rp-empty">
                <p class="rp-empty__title">Ningún vuelo coincide con los filtros</p>
                <button class="rp-btn rp-btn--ghost" @click="resetFiltros" type="button">Quitar filtros</button>
              </div>
              <div v-else class="rv-lista">
                <article v-for="vuelo in vuelosFiltrados" :key="vuelo.id"
                  class="rv-card" :class="{ 'rv-card--sel': vueloSel?.id === vuelo.id }">
                  <div class="rv-card__head">
                    <div class="rv-card__aerolinea">
                      <div class="rv-card__logo"><svg viewBox="0 0 24 24" fill="#FFCC00" width="15" height="15"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg></div>
                      <div><span class="rv-card__nombre">{{ vuelo.aerolinea }}</span><span class="rv-card__num">Nro. {{ vuelo.numeroVuelo }}</span></div>
                    </div>
                    <div class="rv-card__tags">
                      <span v-if="vuelo.escalas === 0" class="rv-tag rv-tag--directo">✓ Directo</span>
                      <span v-else class="rv-tag rv-tag--escala">{{ vuelo.escalas }} escala{{ vuelo.escalas !== 1 ? 's' : '' }}</span>
                      <span v-if="vuelo.asientosTurista > 0 && vuelo.asientosTurista <= 5" class="rv-tag rv-tag--urgente">¡Últimos!</span>
                    </div>
                  </div>
                  <div class="rv-card__ruta">
                    <div class="rv-card__punto"><span class="rv-card__iata">{{ vuelo.origenCodigo }}</span><span class="rv-card__ciudad">{{ vuelo.origenCiudad }}</span><span class="rv-card__hora">{{ vuelo.horaSalida }}</span></div>
                    <div class="rv-card__medio">
                      <span class="rv-card__dur">{{ formatDuracion(vuelo.duracionMinutos) }}</span>
                      <div class="rv-card__track"><div class="rv-card__dot"></div><div class="rv-card__line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18" class="rv-card__avion"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="rv-card__line"></div><div class="rv-card__dot"></div></div>
                    </div>
                    <div class="rv-card__punto rv-card__punto--r"><span class="rv-card__iata">{{ vuelo.destinoCodigo }}</span><span class="rv-card__ciudad">{{ vuelo.destinoCiudad }}</span><span class="rv-card__hora">{{ vuelo.horaLlegada }}</span></div>
                  </div>
                  <div class="rv-card__precios">
                    <button class="rv-precio-btn" :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'economica', 'rv-precio-btn--out': vuelo.asientosTurista === 0 }" @click.stop="vuelo.claseSeleccionada = 'economica'" :disabled="vuelo.asientosTurista === 0" type="button">
                      <span class="rv-precio-btn__clase"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M20 9V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v2"/><path d="M2 11v5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H6v-2a2 2 0 0 0-4 0z"/></svg>Económica</span>
                      <span class="rv-precio-btn__val">${{ Number(vuelo.precioTurista).toFixed(2) }}</span>
                      <span class="rv-precio-btn__seats"><template v-if="vuelo.asientosTurista === 0">Agotado</template><template v-else-if="vuelo.asientosTurista <= 5">¡Solo {{ vuelo.asientosTurista }}!</template><template v-else>{{ vuelo.asientosTurista }} asientos</template></span>
                    </button>
                    <button class="rv-precio-btn rv-precio-btn--ejec" :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'ejecutiva', 'rv-precio-btn--out': vuelo.asientosEjecutiva === 0 }" @click.stop="vuelo.claseSeleccionada = 'ejecutiva'" :disabled="vuelo.asientosEjecutiva === 0" type="button">
                      <span class="rv-precio-btn__clase"><svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>Ejecutiva</span>
                      <span class="rv-precio-btn__val rv-precio-btn__val--ejec">${{ Number(vuelo.precioEjecutiva).toFixed(2) }}</span>
                      <span class="rv-precio-btn__seats"><template v-if="vuelo.asientosEjecutiva === 0">Agotado</template><template v-else-if="vuelo.asientosEjecutiva <= 5">¡Solo {{ vuelo.asientosEjecutiva }}!</template><template v-else>{{ vuelo.asientosEjecutiva }} asientos</template></span>
                    </button>
                  </div>
                  <div class="rv-card__foot">
                    <div class="rv-card__foot-info">
                      <span class="rv-card__foot-precio">${{ Number(vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva : vuelo.precioTurista).toFixed(2) }}<small>/ persona</small></span>
                    </div>
                    <button class="rp-btn rp-btn--yellow rv-card__cta" @click="seleccionarVuelo(vuelo)"
                      :disabled="vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva === 0 : vuelo.asientosTurista === 0" type="button">
                      {{ esIdaVuelta ? 'Seleccionar ida' : 'Seleccionar vuelo' }}
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>
                </article>
              </div>
            </template>

            <!-- Lista vuelos REGRESO (paso 2, solo idaVuelta) -->
            <template v-if="paso === 2 && esIdaVuelta">
              <div v-if="vuelosRegreso.length === 0" class="rp-empty">
                <p class="rp-empty__title">Sin vuelos de regreso</p>
                <button class="rp-btn rp-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
              </div>
              <div v-else class="rv-lista">
                <article v-for="vuelo in vuelosRegreso" :key="vuelo.id"
                  class="rv-card" :class="{ 'rv-card--sel': vueloRegresoSel?.id === vuelo.id }">
                  <div class="rv-card__head">
                    <div class="rv-card__aerolinea">
                      <div class="rv-card__logo"><svg viewBox="0 0 24 24" fill="#FFCC00" width="15" height="15"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg></div>
                      <div><span class="rv-card__nombre">{{ vuelo.aerolinea }}</span><span class="rv-card__num">Nro. {{ vuelo.numeroVuelo }}</span></div>
                    </div>
                    <div class="rv-card__tags">
                      <span v-if="vuelo.escalas === 0" class="rv-tag rv-tag--directo">✓ Directo</span>
                      <span v-else class="rv-tag rv-tag--escala">{{ vuelo.escalas }} escala{{ vuelo.escalas !== 1 ? 's' : '' }}</span>
                    </div>
                  </div>
                  <div class="rv-card__ruta">
                    <div class="rv-card__punto"><span class="rv-card__iata">{{ vuelo.origenCodigo }}</span><span class="rv-card__ciudad">{{ vuelo.origenCiudad }}</span><span class="rv-card__hora">{{ vuelo.horaSalida }}</span></div>
                    <div class="rv-card__medio">
                      <span class="rv-card__dur">{{ formatDuracion(vuelo.duracionMinutos) }}</span>
                      <div class="rv-card__track"><div class="rv-card__dot"></div><div class="rv-card__line"></div><svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18" class="rv-card__avion"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg><div class="rv-card__line"></div><div class="rv-card__dot"></div></div>
                    </div>
                    <div class="rv-card__punto rv-card__punto--r"><span class="rv-card__iata">{{ vuelo.destinoCodigo }}</span><span class="rv-card__ciudad">{{ vuelo.destinoCiudad }}</span><span class="rv-card__hora">{{ vuelo.horaLlegada }}</span></div>
                  </div>
                  <div class="rv-card__precios">
                    <button class="rv-precio-btn" :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'economica', 'rv-precio-btn--out': vuelo.asientosTurista === 0 }" @click.stop="vuelo.claseSeleccionada = 'economica'" :disabled="vuelo.asientosTurista === 0" type="button">
                      <span class="rv-precio-btn__clase"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M20 9V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v2"/><path d="M2 11v5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H6v-2a2 2 0 0 0-4 0z"/></svg>Económica</span>
                      <span class="rv-precio-btn__val">${{ Number(vuelo.precioTurista).toFixed(2) }}</span>
                      <span class="rv-precio-btn__seats"><template v-if="vuelo.asientosTurista === 0">Agotado</template><template v-else-if="vuelo.asientosTurista <= 5">¡Solo {{ vuelo.asientosTurista }}!</template><template v-else>{{ vuelo.asientosTurista }} asientos</template></span>
                    </button>
                    <button class="rv-precio-btn rv-precio-btn--ejec" :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'ejecutiva', 'rv-precio-btn--out': vuelo.asientosEjecutiva === 0 }" @click.stop="vuelo.claseSeleccionada = 'ejecutiva'" :disabled="vuelo.asientosEjecutiva === 0" type="button">
                      <span class="rv-precio-btn__clase"><svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>Ejecutiva</span>
                      <span class="rv-precio-btn__val rv-precio-btn__val--ejec">${{ Number(vuelo.precioEjecutiva).toFixed(2) }}</span>
                      <span class="rv-precio-btn__seats"><template v-if="vuelo.asientosEjecutiva === 0">Agotado</template><template v-else-if="vuelo.asientosEjecutiva <= 5">¡Solo {{ vuelo.asientosEjecutiva }}!</template><template v-else>{{ vuelo.asientosEjecutiva }} asientos</template></span>
                    </button>
                  </div>
                  <div class="rv-card__foot">
                    <div class="rv-card__foot-info">
                      <span class="rv-card__foot-precio">${{ Number(vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva : vuelo.precioTurista).toFixed(2) }}<small>/ persona</small></span>
                    </div>
                    <button class="rp-btn rp-btn--yellow rv-card__cta" @click="seleccionarVuelo(vuelo)"
                      :disabled="vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva === 0 : vuelo.asientosTurista === 0" type="button">
                      Seleccionar regreso
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>
                </article>
              </div>
            </template>
          </template>

          <!-- ══════════════════════════
               PASO HOTEL
          ══════════════════════════ -->
          <template v-if="!loading && !error && paso === pasoHotel">
            <div v-if="todasLasHabitaciones.length === 0" class="rp-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="48" height="48"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
              <p class="rp-empty__title">Sin hoteles disponibles</p>
              <button class="rp-btn rp-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
            </div>
            <div v-else-if="gruposPorHotel.length === 0" class="rp-empty">
              <p class="rp-empty__title">Sin hospedajes para {{ busqueda.cantidadPersonas }} personas</p>
              <button class="rp-btn rp-btn--ghost" @click="resetFiltros" type="button">Quitar filtros</button>
            </div>

            <template v-else>
              <div v-for="grupo in gruposPorHotel" :key="`${grupo.proveedorId}-${grupo.hotelId}`" class="rh-grupo">
                <div class="rh-grupo__head">
                  <div class="rh-grupo__hotel-info">
                    <div class="rh-grupo__hotel-icon">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    </div>
                    <div>
                      <div class="rh-grupo__header-row">
                        <h3 class="rh-grupo__nombre">{{ grupo.nombreHotel }}</h3>
                        <div v-if="grupo.rating" class="rh-grupo__rating">
                          <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                          {{ grupo.rating.toFixed(1) }}
                        </div>
                      </div>
                      <p class="rh-grupo__ubicacion">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                        {{ grupo.direccion || `${grupo.ciudad}, ${grupo.pais}` }}
                      </p>
                      <p v-if="grupo.descripcion" class="rh-grupo__desc">{{ grupo.descripcion }}</p>
                    </div>
                  </div>
                  <div class="rh-grupo__head-right">
                    <span class="rh-grupo__proveedor">{{ grupo.proveedorNombre }}</span>
                    <div class="rh-grupo__desde">
                      <span class="rh-grupo__desde-lbl">Desde</span>
                      <span class="rh-grupo__desde-precio">{{ fmt(Math.min(...grupo.habitaciones.map(h => h.precioPorNoche))) }}</span>
                      <span class="rh-grupo__desde-sub">/ noche</span>
                    </div>
                    <span class="rh-grupo__count">{{ grupo.habitaciones.length }} tipo{{ grupo.habitaciones.length !== 1 ? 's' : '' }}</span>
                  </div>
                </div>

                <div v-if="grupo.amenidades.length" class="rh-grupo__amenidades">
                  <span v-for="am in grupo.amenidades" :key="am.amenidadId" class="rh-amenidad-hotel" :title="am.descripcion">{{ am.nombre }}</span>
                </div>

                <!-- Combos -->
                <template v-if="getHotelCombos(grupo)">
                  <div class="rh-hotel-combos">
                    <div v-if="getHotelCombos(grupo).combo" class="rh-combo-panel rh-combo-panel--exact">
                      <div class="rh-combo-panel__label">Combinación para {{ busqueda.cantidadPersonas }} personas</div>
                      <div v-for="(item, i) in getHotelCombos(grupo).combo.habs" :key="i" class="rh-combo-row">
                        <span class="rh-combo-tipo">Hab.{{ i+1 }} · <strong>{{ item.tipo }}</strong> <span class="rh-combo-cap">({{ item.cap }} pers.)</span></span>
                        <span class="rh-combo-precio">{{ fmt(item.precio) }}<small>/noche</small></span>
                      </div>
                      <div class="rh-combo-total">Total: <strong>{{ fmt(getHotelCombos(grupo).combo.total) }}/noche</strong> · {{ fmt(getHotelCombos(grupo).combo.total * noches) }} por {{ noches }} noches</div>
                      <div v-if="grupo.amenidades.length" class="rh-combo-amenidades">
                        <span v-for="am in grupo.amenidades.slice(0,5)" :key="am.amenidadId" class="rh-combo-amenidad-chip">{{ am.nombre }}</span>
                      </div>
                      <button class="rh-btn rh-btn--yellow rh-combo-panel__cta" @click="seleccionarHotelCombo(getHotelCombos(grupo).combo, grupo)" type="button">
                        Agregar al paquete <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                      </button>
                    </div>
                    <div v-if="getHotelCombos(grupo).aprox" class="rh-combo-panel rh-combo-panel--aprox">
                      <div class="rh-combo-panel__label">Opción cercana — {{ getHotelCombos(grupo).aprox.capacidadTotal }} de {{ busqueda.cantidadPersonas }} pers.</div>
                      <div v-for="(item, i) in getHotelCombos(grupo).aprox.habs" :key="i" class="rh-combo-row">
                        <span class="rh-combo-tipo">Hab.{{ i+1 }} · <strong>{{ item.tipo }}</strong> <span class="rh-combo-cap">({{ item.cap }} pers.)</span></span>
                        <span class="rh-combo-precio">{{ fmt(item.precio) }}<small>/noche</small></span>
                      </div>
                      <div class="rh-combo-total">Total: <strong>{{ fmt(getHotelCombos(grupo).aprox.total) }}/noche</strong></div>
                      <button class="rh-btn rh-btn--yellow rh-combo-panel__cta" @click="seleccionarHotelCombo(getHotelCombos(grupo).aprox, grupo)" type="button">
                        Agregar al paquete <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                      </button>
                    </div>
                    <div v-if="getHotelCombos(grupo).extra" class="rh-combo-panel rh-combo-panel--extra">
                      <div class="rh-combo-panel__label">Habitación + 1 persona extra</div>
                      <div class="rh-combo-row">
                        <span class="rh-combo-tipo">{{ getHotelCombos(grupo).extra.tipo }} <span class="rh-combo-cap">(cap. {{ getHotelCombos(grupo).extra.cap }} +1)</span></span>
                        <span class="rh-combo-precio">{{ fmt(getHotelCombos(grupo).extra.precioPorNoche) }}<small>/noche</small></span>
                      </div>
                      <div class="rh-combo-total">Total: <strong>{{ fmt(getHotelCombos(grupo).extra.total) }}/noche</strong></div>
                      <button class="rh-btn rh-btn--yellow rh-combo-panel__cta" @click="seleccionarHotelExtra(getHotelCombos(grupo).extra, grupo)" type="button">
                        Agregar al paquete <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                      </button>
                    </div>
                  </div>
                </template>

                <!-- Habitaciones individuales -->
                <div class="rh-habitaciones"
                  v-if="grupo.habitaciones.filter(h => h.capacidadMaxima >= busqueda.cantidadPersonas).length > 0">
                  <article v-for="hab in grupo.habitaciones.filter(h => h.capacidadMaxima >= busqueda.cantidadPersonas)"
                    :key="hab.uid" class="rh-card"
                    :class="{ 'rh-card--seleccionada': hotelSel?.uid === hab.uid }">
                    <div class="rh-card__img">
                      <div class="rh-card__img-placeholder">
                        <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.3)" stroke-width="1" width="36" height="36"><path d="M2 7a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2z"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                      </div>
                      <div class="rh-card__tipo-badge">{{ hab.tipoHabitacion }}</div>
                      <div v-if="hab.cantidadDisponible <= 2" class="rh-card__urgente-badge">¡Solo {{ hab.cantidadDisponible }}!</div>
                    </div>
                    <div class="rh-card__info">
                      <div class="rh-card__info-top">
                        <div class="rh-card__info-left">
                          <h4 class="rh-card__nombre">{{ hab.tipoHabitacion }}</h4>
                          <p class="rh-card__cama">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                            {{ hab.tipoCama }}
                          </p>
                        </div>
                        <div class="rh-card__precio-bloque">
                          <span class="rh-card__precio-lbl">por noche</span>
                          <span class="rh-card__precio">${{ hab.precioPorNoche.toFixed(2) }}</span>
                          <span class="rh-card__precio-total">{{ noches }} noches: <strong>${{ (hab.precioPorNoche * noches).toFixed(2) }}</strong></span>
                        </div>
                      </div>
                      <div class="rh-card__meta">
                        <span>
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                          Hasta {{ hab.capacidadMaxima }} pers.
                        </span>
                        <span v-if="hab.metrosCuadrados">{{ hab.metrosCuadrados }} m²</span>
                        <span class="rh-card__disponibles" :class="{ 'rh-card__disponibles--bajo': hab.cantidadDisponible <= 3 }">
                          <template v-if="hab.cantidadDisponible === 0">Agotado</template>
                          <template v-else>{{ hab.cantidadDisponible }} disponibles</template>
                        </span>
                      </div>
                      <button class="rp-btn rp-btn--yellow rh-card__cta"
                        :disabled="hab.cantidadDisponible === 0"
                        @click="seleccionarHotelHab(hab, grupo)" type="button">
                        <template v-if="hab.cantidadDisponible === 0">Sin disponibilidad</template>
                        <template v-else>Agregar al paquete <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg></template>
                      </button>
                    </div>
                  </article>
                </div>
              </div>
            </template>
          </template>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/resultadosvuelos.css'
import '../styles/resultadoshoteles.css'
import '../styles/resultadospaquetes.css'

const router = useRouter()
const API    = 'http://localhost:8080'
const hoy    = new Date().toISOString().split('T')[0]

// ── State desde history ───────────────────────────────────────
const state = history.state || {}
const busqueda = ref({
  origen:           state.busqueda?.origen           || '',
  origenPais:       state.busqueda?.origenPais       || '',
  destino:          state.busqueda?.destino          || '',
  destinoPais:      state.busqueda?.destinoPais      || '',
  fecha:            state.busqueda?.fecha            || '',
  fechaRegreso:     state.busqueda?.fechaRegreso     || '',
  checkIn:          state.busqueda?.checkIn          || '',
  checkOut:         state.busqueda?.checkOut         || '',
  cantidadPersonas: state.busqueda?.cantidadPersonas || 1,
  tipoVuelo:        state.busqueda?.tipoVuelo        || 'ida',
})

const esIdaVuelta = computed(() => busqueda.value.tipoVuelo === 'idaVuelta')
const pasoHotel   = computed(() => esIdaVuelta.value ? 3 : 2)

// ── Datos ─────────────────────────────────────────────────────
const vuelos               = ref([])
const vuelosRegreso        = ref([])
const todasLasHabitaciones = ref([])
const loading              = ref(true)
const buscando             = ref(false)
const error                = ref('')
const modError             = ref('')
const paso                 = ref(1)
const vueloSel             = ref(null)
const vueloRegresoSel      = ref(null)
const hotelSel             = ref(null)
const modificarAbierto     = ref(false)
const sidebarColapsado     = ref(false)
const ordenVuelos          = ref('precio-asc')
const ordenHoteles         = ref('precio-asc')

const noches = computed(() => {
  if (!busqueda.value.checkIn || !busqueda.value.checkOut) return 0
  return Math.max(0, Math.ceil((new Date(busqueda.value.checkOut) - new Date(busqueda.value.checkIn)) / 86400000))
})

const precioTotal = computed(() => {
  if (!vueloSel.value || !hotelSel.value) return 0
  if (esIdaVuelta.value && !vueloRegresoSel.value) return 0
  const pflight  = vueloSel.value.precio * busqueda.value.cantidadPersonas
  const pregreso = esIdaVuelta.value ? (vueloRegresoSel.value.precio * busqueda.value.cantidadPersonas) : 0
  const photel   = hotelSel.value.precioNoche * noches.value
  return pflight + pregreso + photel
})

// ── Filtros ───────────────────────────────────────────────────
const fv = ref({ precioMin: 0, precioMax: 9999, clases: [], escalas: [], duracionMax: 9999, aerolineas: [], horario: '' })
const fh = ref({ precioMin: 0, precioMax: 9999, tipos: [], hoteles: [], amenidades: [] })

const clasesFilter = [{ val: 'economica', label: 'Económica' }, { val: 'ejecutiva', label: 'Ejecutiva' }]
const escalasOpts  = [{ val: 0, label: 'Solo directos' }, { val: 1, label: '1 escala' }, { val: 2, label: '2+ escalas' }]
const duracionOpts = [{ val: 180, label: '< 3h' }, { val: 360, label: '< 6h' }, { val: 720, label: '< 12h' }, { val: 1440, label: '< 24h' }]
const horariosOpts = [
  { val: 'madrugada', icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`, label: 'Madrugada', rango: '00:00–05:59' },
  { val: 'manana',    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M17 18a5 5 0 0 0-10 0"/><line x1="12" y1="9" x2="12" y2="2"/></svg>`, label: 'Mañana', rango: '06:00–11:59' },
  { val: 'tarde',     icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/></svg>`, label: 'Tarde', rango: '12:00–17:59' },
  { val: 'noche',     icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/></svg>`, label: 'Noche', rango: '18:00–23:59' },
]

const cantFiltrosActivos = computed(() => {
  if (paso.value === 1) {
    let n = 0
    if (fv.value.precioMin > 0 || fv.value.precioMax < 9999) n++
    n += fv.value.clases.length + fv.value.escalas.length + fv.value.aerolineas.length
    if (fv.value.duracionMax < 9999) n++; if (fv.value.horario) n++
    return n
  }
  let n = 0
  if (fh.value.precioMin > 0 || fh.value.precioMax < 9999) n++
  n += fh.value.tipos.length + fh.value.hoteles.length + fh.value.amenidades.length
  return n
})

function resetFiltros() {
  if (paso.value === 1) fv.value = { precioMin: 0, precioMax: 9999, clases: [], escalas: [], duracionMax: 9999, aerolineas: [], horario: '' }
  else fh.value = { precioMin: 0, precioMax: 9999, tipos: [], hoteles: [], amenidades: [] }
}

// ── Form modificar ────────────────────────────────────────────
const form = reactive({
  oPaisQ: '', oPaisSug: [], oPaisSel: null,
  oCiudadQ: '', oCiudadSug: [], oCiudadLoading: false, oCiudades: [],
  oPais: '', oCiudad: '',
  dPaisQ: '', dPaisSug: [], dPaisSel: null,
  dCiudadQ: '', dCiudadSug: [], dCiudadLoading: false, dCiudades: [],
  dPais: '', dCiudad: '',
  fecha: '', fechaRegreso: '', checkIn: '', checkOut: '',
  cantidadPersonas: 1, tipoVuelo: 'ida',
})

const minCheckOut = computed(() => {
  if (!form.checkIn) return hoy
  const d = new Date(form.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

// ── countriesnow ──────────────────────────────────────────────
let paisesCache = null
async function getPaises() {
  if (paisesCache) return paisesCache
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); paisesCache = d.data || [] } catch { paisesCache = [] }
  return paisesCache
}
async function getCiudades(country) {
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ country }) }); const d = await r.json(); return d.data || [] } catch { return [] }
}
function blurClose(fn) { setTimeout(fn, 200) }

async function onOPaisInput() {
  form.oPaisSel = null; form.oCiudadQ = ''; form.oCiudades = []; form.oPais = ''; form.oCiudad = ''
  const q = form.oPaisQ.trim(); if (q.length < 2) { form.oPaisSug = []; return }
  form.oPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selOPais(p) {
  form.oPaisSel = p; form.oPaisQ = p.country; form.oPaisSug = []; form.oPais = p.country
  form.oCiudadLoading = true; form.oCiudades = await getCiudades(p.country); form.oCiudadLoading = false
}
function onOCiudadInput() { const q = form.oCiudadQ.toLowerCase(); form.oCiudadSug = q.length < 2 ? [] : form.oCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6); form.oCiudad = '' }
function selOCiudad(c) { form.oCiudadQ = c; form.oCiudadSug = []; form.oCiudad = c; modError.value = '' }

async function onDPaisInput() {
  form.dPaisSel = null; form.dCiudadQ = ''; form.dCiudades = []; form.dPais = ''; form.dCiudad = ''
  const q = form.dPaisQ.trim(); if (q.length < 2) { form.dPaisSug = []; return }
  form.dPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selDPais(p) {
  form.dPaisSel = p; form.dPaisQ = p.country; form.dPaisSug = []; form.dPais = p.country
  form.dCiudadLoading = true; form.dCiudades = await getCiudades(p.country); form.dCiudadLoading = false
}
function onDCiudadInput() { const q = form.dCiudadQ.toLowerCase(); form.dCiudadSug = q.length < 2 ? [] : form.dCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6); form.dCiudad = '' }
function selDCiudad(c) { form.dCiudadQ = c; form.dCiudadSug = []; form.dCiudad = c; modError.value = '' }

function toggleModificar() {
  modificarAbierto.value = !modificarAbierto.value
  if (modificarAbierto.value) {
    Object.assign(form, {
      // Origen — pre-llenar con búsqueda actual
      oPaisQ:         busqueda.value.origenPais    || '',
      oPais:          busqueda.value.origenPais    || '',
      oCiudadQ:       busqueda.value.origen        || '',
      oCiudad:        busqueda.value.origen        || '',
      oPaisSug:       [],
      oPaisSel:       busqueda.value.origenPais ? { country: busqueda.value.origenPais } : null,
      oCiudadSug:     [],
      oCiudadLoading: false,
      oCiudades:      [],

      // Destino — pre-llenar con búsqueda actual
      dPaisQ:         busqueda.value.destinoPais   || '',
      dPais:          busqueda.value.destinoPais   || '',
      dCiudadQ:       busqueda.value.destino       || '',
      dCiudad:        busqueda.value.destino       || '',
      dPaisSug:       [],
      dPaisSel:       busqueda.value.destinoPais ? { country: busqueda.value.destinoPais } : null,
      dCiudadSug:     [],
      dCiudadLoading: false,
      dCiudades:      [],

      // Fechas vuelo
      fecha:            busqueda.value.fecha            || '',
      fechaRegreso:     busqueda.value.fechaRegreso     || '',

      // Fechas hotel
      checkIn:          busqueda.value.checkIn          || '',
      checkOut:         busqueda.value.checkOut         || '',

      // Personas y tipo
      cantidadPersonas: busqueda.value.cantidadPersonas || 1,
      tipoVuelo:        busqueda.value.tipoVuelo        || 'ida',
    })
    modError.value = ''
  }
}

function tieneVuelos(res) {
  return Array.isArray(res) && res.some(b => b.datos && ((b.datos.directos?.length > 0) || (b.datos.conEscala?.length > 0)))
}
function tieneHoteles(res) {
  return Array.isArray(res) && res.some(b => Array.isArray(b.datos) && b.datos.length > 0)
}

async function rebuscar() {
  modError.value = ''
  const o  = form.oCiudad  || form.oCiudadQ.trim()
  const op = form.oPais    || form.oPaisQ.trim()
  const d  = form.dCiudad  || form.dCiudadQ.trim()
  const dp = form.dPais    || form.dPaisQ.trim()
  if (!op || !o)          { modError.value = 'Selecciona origen.'; return }
  if (!dp || !d)          { modError.value = 'Selecciona destino.'; return }
  if (!form.fecha)        { modError.value = 'Selecciona la fecha de vuelo.'; return }
  if (form.fecha < hoy)   { modError.value = 'La fecha de vuelo no puede ser en el pasado.'; return }
  if (form.tipoVuelo === 'idaVuelta') {
    if (!form.fechaRegreso)                       { modError.value = 'Selecciona la fecha de regreso.'; return }
    if (form.fechaRegreso <= form.fecha)           { modError.value = 'El regreso debe ser posterior a la ida.'; return }
  }
  if (!form.checkIn)      { modError.value = 'Selecciona el check-in del hotel.'; return }
  if (form.checkIn < hoy) { modError.value = 'El check-in no puede ser en el pasado.'; return }
  if (!form.checkOut)     { modError.value = 'Selecciona el check-out del hotel.'; return }
  if (form.checkOut <= form.checkIn) { modError.value = 'El check-out debe ser posterior al check-in.'; return }

  buscando.value = true
  // Cancelar promesa previa al rebuscar
  window.__reservaPromise = null

  try {
    const bodyIda   = { origen: o, origenPais: op, destino: d, destinoPais: dp, fecha: form.fecha, cantidadPasajeros: form.cantidadPersonas }
    const bodyHotel = { ciudad: d, pais: dp, fechaCheckIn: form.checkIn, fechaCheckOut: form.checkOut, cantidadPersonas: form.cantidadPersonas }

    let rawV, rawR = [], rawH

    if (form.tipoVuelo === 'idaVuelta') {
      const bodyReg = { origen: d, origenPais: dp, destino: o, destinoPais: op, fecha: form.fechaRegreso, cantidadPasajeros: form.cantidadPersonas }
      const [resIda, resReg, resH] = await Promise.all([
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) }),
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyReg) }),
        fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyHotel) }),
      ])
      rawV = resIda.ok ? await resIda.json() : []
      rawR = resReg.ok ? await resReg.json() : []
      rawH = resH.ok  ? await resH.json()  : []
      if (!tieneVuelos(rawV))  { modError.value = `No hay vuelos de ${o} a ${d} para el ${form.fecha}.`; return }
      if (!tieneVuelos(rawR))  { modError.value = `No hay vuelos de regreso de ${d} a ${o} para el ${form.fechaRegreso}.`; return }
      if (!tieneHoteles(rawH)) { modError.value = `No hay hoteles en ${d} para esas fechas.`; return }
    } else {
      const [resIda, resH] = await Promise.all([
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) }),
        fetch(`${API}/api/busqueda/hoteles`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyHotel) }),
      ])
      rawV = resIda.ok ? await resIda.json() : []
      rawH = resH.ok  ? await resH.json()  : []
      if (!tieneVuelos(rawV))  { modError.value = `No hay vuelos de ${o} a ${d} para el ${form.fecha}.`; return }
      if (!tieneHoteles(rawH)) { modError.value = `No hay hoteles en ${d} para esas fechas.`; return }
    }

    busqueda.value = { origen: o, origenPais: op, destino: d, destinoPais: dp, fecha: form.fecha, fechaRegreso: form.fechaRegreso, checkIn: form.checkIn, checkOut: form.checkOut, cantidadPersonas: form.cantidadPersonas, tipoVuelo: form.tipoVuelo }
    vuelos.value               = mapearVuelos(rawV)
    vuelosRegreso.value        = mapearVuelos(rawR)
    todasLasHabitaciones.value = mapearHoteles(rawH)
    error.value = ''
    fv.value = { precioMin: 0, precioMax: 9999, clases: [], escalas: [], duracionMax: 9999, aerolineas: [], horario: '' }
    fh.value = { precioMin: 0, precioMax: 9999, tipos: [], hoteles: [], amenidades: [] }
    vueloSel.value = null; vueloRegresoSel.value = null; hotelSel.value = null; paso.value = 1
    modificarAbierto.value = false
  } catch { modError.value = 'Error al buscar paquetes. Intenta de nuevo.' }
  finally { buscando.value = false }
}

// ── Mapeo vuelos ──────────────────────────────────────────────
function formatHora(h) { return h ? String(h).substring(0, 5) : '--' }

function mapearVuelos(respuesta) {
  const res = []
  for (const b of respuesta) {
    if (b.error || !b.datos) continue
    for (const v of (b.datos.directos  || [])) res.push(mapDirecto(v, b))
    for (const v of (b.datos.conEscala || [])) res.push(mapEscala(v, b))
  }
  return res
}
function mapDirecto(v, b) {
  return {
    id: `${b.proveedor_id}-d-${v.id ?? Math.random()}`,
    aerolinea: b.proveedor, numeroVuelo: v.numeroVuelo || '--',
    origenCodigo: v.origenCodigo || '', origenCiudad: v.origenCiudad || '',
    destinoCodigo: v.destinoCodigo || '', destinoCiudad: v.destinoCiudad || '',
    horaSalida: formatHora(v.horaSalida), horaLlegada: formatHora(v.horaLlegada),
    duracionMinutos: v.duracionMinutos || 0,
    precioTurista: v.precioTurista ?? 0, precioEjecutiva: v.precioEjecutiva ?? 0,
    asientosTurista: typeof v.boletosDisponiblesTurista === 'number' ? v.boletosDisponiblesTurista : 99,
    asientosEjecutiva: typeof v.boletosDisponiblesEjecutiva === 'number' ? v.boletosDisponiblesEjecutiva : 99,
    claseSeleccionada: 'economica', avionMarca: v.avionMarca || '', avionModelo: v.avionModelo || '',
    escalas: 0, paradas: [], tiempoEscalaMinutos: 0,
  }
}
function mapEscala(v, b) {
  const tramos = Array.isArray(v.tramos) ? v.tramos : []
  const p = tramos[0] || {}, u = tramos[tramos.length - 1] || {}
  return {
    id: `${b.proveedor_id}-e-${v.precioTuristaTotal ?? Math.random()}`,
    aerolinea: b.proveedor, numeroVuelo: p.numeroVuelo || '--',
    origenCodigo: p.origenCodigo || '', origenCiudad: p.origenCiudad || '',
    destinoCodigo: u.destinoCodigo || '', destinoCiudad: u.destinaCiudad || u.destinoCiudad || '',
    horaSalida: formatHora(p.horaSalida), horaLlegada: formatHora(u.horaLlegada),
    duracionMinutos: v.duracionTotalMinutos || 0,
    precioTurista: v.precioTuristaTotal ?? 0, precioEjecutiva: v.precioEjecutivaTotal ?? 0,
    asientosTurista: typeof v.boletosDisponiblesTurista === 'number' ? v.boletosDisponiblesTurista : 99,
    asientosEjecutiva: typeof v.boletosDisponiblesEjecutiva === 'number' ? v.boletosDisponiblesEjecutiva : 99,
    claseSeleccionada: 'economica', avionMarca: p.avionMarca || '', avionModelo: p.avionModelo || '',
    escalas: v.numeroEscalas ?? (tramos.length > 1 ? tramos.length - 1 : 1),
    paradas: tramos.slice(0, -1).map(t => ({ codigo: t.destinoCodigo || '?', ciudad: t.destinaCiudad || t.destinoCiudad || '' })),
    tiempoEscalaMinutos: v.tiempoEscalaMinutos || 0,
  }
}

// ── Mapeo hoteles ─────────────────────────────────────────────
function mapearHoteles(respuesta) {
  const resultado = []
  if (!Array.isArray(respuesta)) return resultado
  for (const proveedor of respuesta) {
    if (proveedor.error || !Array.isArray(proveedor.datos)) continue
    for (const hotel of proveedor.datos) {
      if (!hotel) continue
      const roomsMap = new Map()
      if (Array.isArray(hotel.tiposHabitacion)) {
        for (const room of hotel.tiposHabitacion) roomsMap.set(room.tipoHabitacionId, room)
      }
      if (hotel.tiposHabitacionPorCapacidad) {
        for (const rooms of Object.values(hotel.tiposHabitacionPorCapacidad)) {
          if (!Array.isArray(rooms)) continue
          for (const room of rooms) { if (!roomsMap.has(room.tipoHabitacionId)) roomsMap.set(room.tipoHabitacionId, room) }
        }
      }
      for (const room of roomsMap.values()) {
        const disp = Array.isArray(room.habitacionesDisponibles) ? room.habitacionesDisponibles : []
        resultado.push({
          uid: `${proveedor.proveedor_id}-${hotel.id}-${room.tipoHabitacionId}`,
          proveedorId: proveedor.proveedor_id, proveedorNombre: proveedor.proveedor,
          hotelId: hotel.id, nombreHotel: hotel.nombre,
          hotelCiudad: hotel.ciudad, hotelPais: hotel.pais,
          hotelDescripcion: hotel.descripcion, hotelDireccion: hotel.direccion,
          hotelRating: hotel.rating || null,
          amenidades: Array.isArray(hotel.amenidades) ? hotel.amenidades : [],
          tipoHabitacionId: room.tipoHabitacionId,
          tipoHabitacion: room.tipoHabitacion, tipoCama: room.tipoCama,
          precioPorNoche: room.precioPorNoche ?? 0,
          precioPorPersona: room.precioPorPersona ?? 0,
          metrosCuadrados: room.metrosCuadrados || null,
          capacidadMaxima: room.capacidadMaxima ?? 1,
          habitacionesDisponibles: disp, cantidadDisponible: disp.length,
          _tiposHabitacion:             Array.isArray(hotel.tiposHabitacion) ? hotel.tiposHabitacion : [],
          _tiposHabitacionPorCapacidad: hotel.tiposHabitacionPorCapacidad || {},
          _combinacionesNumericas:      hotel.combinacionesNumericas || [],
        })
      }
    }
  }
  return resultado
}

// ── Computed vuelos filtrados ─────────────────────────────────
const aerolineasDisponibles = computed(() => [...new Set(vuelos.value.map(v => v.aerolinea).filter(Boolean))])

const vuelosFiltrados = computed(() => {
  let list = vuelos.value
  if (fv.value.precioMin > 0)    list = list.filter(v => v.precioTurista >= fv.value.precioMin)
  if (fv.value.precioMax < 9999) list = list.filter(v => v.precioTurista <= fv.value.precioMax)
  if (fv.value.clases.length > 0 && fv.value.clases.includes('ejecutiva') && !fv.value.clases.includes('economica'))
    list = list.filter(v => v.asientosEjecutiva > 0 && v.precioEjecutiva > 0)
  if (fv.value.escalas.length > 0) list = list.filter(v => { const sel = fv.value.escalas; return sel.includes(2) ? (sel.includes(v.escalas) || v.escalas >= 2) : sel.includes(v.escalas) })
  if (fv.value.duracionMax < 9999) list = list.filter(v => v.duracionMinutos <= fv.value.duracionMax)
  if (fv.value.aerolineas.length > 0) list = list.filter(v => fv.value.aerolineas.includes(v.aerolinea))
  if (fv.value.horario) {
    const rangos = { madrugada:[0,6], manana:[6,12], tarde:[12,18], noche:[18,24] }
    const [min, max] = rangos[fv.value.horario] || [0,24]
    list = list.filter(v => { const h = parseInt(v.horaSalida?.split(':')[0] ?? 0); return h >= min && h < max })
  }
  return [...list].sort((a, b) => {
    switch (ordenVuelos.value) {
      case 'precio-asc':  return a.precioTurista - b.precioTurista
      case 'precio-desc': return b.precioTurista - a.precioTurista
      case 'duracion':    return (a.duracionMinutos||0) - (b.duracionMinutos||0)
      case 'salida':      return (a.horaSalida||'').localeCompare(b.horaSalida||'')
      case 'escalas':     return a.escalas - b.escalas
      default: return 0
    }
  })
})

// ── Computed hoteles filtrados ────────────────────────────────
const tiposHabitacionDisponibles = computed(() => [...new Set(todasLasHabitaciones.value.map(h => h.tipoHabitacion).filter(Boolean))])
const hotelesDisponibles         = computed(() => [...new Set(todasLasHabitaciones.value.map(h => h.nombreHotel).filter(Boolean))])
const amenidadesDisponibles      = computed(() => { const s = new Set(); todasLasHabitaciones.value.forEach(h => h.amenidades?.forEach(a => s.add(a.nombre))); return [...s] })

const habitacionesFiltradas = computed(() => {
  let list = todasLasHabitaciones.value
  if (fh.value.precioMin > 0)    list = list.filter(h => h.precioPorNoche >= fh.value.precioMin)
  if (fh.value.precioMax < 9999) list = list.filter(h => h.precioPorNoche <= fh.value.precioMax)
  if (fh.value.tipos.length > 0) list = list.filter(h => fh.value.tipos.includes(h.tipoHabitacion))
  if (fh.value.hoteles.length > 0) list = list.filter(h => fh.value.hoteles.includes(h.nombreHotel))
  if (fh.value.amenidades.length > 0) list = list.filter(h => fh.value.amenidades.every(a => h.amenidades?.some(x => x.nombre === a)))
  return [...list].sort((a, b) => {
    switch (ordenHoteles.value) {
      case 'precio-asc':  return a.precioPorNoche - b.precioPorNoche
      case 'precio-desc': return b.precioPorNoche - a.precioPorNoche
      case 'capacidad':   return b.capacidadMaxima - a.capacidadMaxima
      default: return 0
    }
  })
})

const gruposPorHotel = computed(() => {
  const map = new Map()
  for (const hab of habitacionesFiltradas.value) {
    const key = `${hab.proveedorId}-${hab.hotelId}`
    if (!map.has(key)) {
      map.set(key, {
        hotelId: hab.hotelId, proveedorId: hab.proveedorId,
        proveedorNombre: hab.proveedorNombre, nombreHotel: hab.nombreHotel,
        ciudad: hab.hotelCiudad, pais: hab.hotelPais,
        descripcion: hab.hotelDescripcion, direccion: hab.hotelDireccion,
        rating: hab.hotelRating, amenidades: hab.amenidades || [], habitaciones: [],
        tiposHabitacion:             hab._tiposHabitacion || [],
        tiposHabitacionPorCapacidad: hab._tiposHabitacionPorCapacidad || {},
        combinacionesNumericas:      hab._combinacionesNumericas || [],
      })
    }
    map.get(key).habitaciones.push(hab)
  }
  const p = busqueda.value.cantidadPersonas
  return Array.from(map.values()).filter(g =>
    g.tiposHabitacion?.some(r => r.capacidadMaxima >= p) ||
    Object.keys(g.tiposHabitacionPorCapacidad || {}).some(k => Number(k) >= p) ||
    _getComboHabs(g) || _getComboAproximado(g, p) || _getPersonaExtraMin(g, p)
  )
})

// ══ COMBO HELPERS ═════════════════════════════════════════════
function fmt(p) { return new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }).format(p) }

function _getComboHabs(hotel) {
  if (!hotel.combinacionesNumericas?.length) return null
  const combo = hotel.combinacionesNumericas[0]; if (combo.length <= 1) return null
  const usados = {}; const result = []
  for (const cap of combo) {
    const key = String(cap); const rooms = hotel.tiposHabitacionPorCapacidad?.[key]
    if (!rooms?.length) return null; const idx = usados[key] ?? 0; if (idx >= rooms.length) return null
    const r = rooms[idx]
    result.push({ tipo: r.tipoHabitacion, precio: r.precioPorNoche, precioPorPersona: r.precioPorPersona, cap, tipoCama: r.tipoCama, metrosCuadrados: r.metrosCuadrados || null, habitacionesDisponibles: r.habitacionesDisponibles || [], cantidadDisponible: (r.habitacionesDisponibles || []).length })
    usados[key] = idx + 1
  }
  return { habs: result, total: result.reduce((s, h) => s + h.precio, 0) }
}

function _getComboAproximado(hotel, personas) {
  if (hotel.tiposHabitacion?.length || _getComboHabs(hotel)) return null
  const porCap = hotel.tiposHabitacionPorCapacidad
  if (!porCap || !Object.keys(porCap).length) return null
  const todasHabs = []
  for (const [capStr, rooms] of Object.entries(porCap)) {
    const cap = Number(capStr)
    for (const room of rooms) todasHabs.push({ tipo: room.tipoHabitacion, precio: room.precioPorNoche, precioPorPersona: room.precioPorPersona, cap, tipoCama: room.tipoCama, habitacionesDisponibles: room.habitacionesDisponibles || [], cantidadDisponible: (room.habitacionesDisponibles || []).length })
  }
  todasHabs.sort((a, b) => b.cap - a.cap)
  let sumCap = 0; const selec = []
  for (const hab of todasHabs) { if (sumCap >= personas) break; selec.push(hab); sumCap += hab.cap }
  if (sumCap < personas || sumCap > personas + 2 || selec.length <= 1) return null
  return { habs: selec, capacidadTotal: sumCap, total: selec.reduce((s, h) => s + h.precio, 0) }
}

function _getPersonaExtraMin(hotel, personas) {
  if (personas <= 1) return null
  const rooms = hotel.tiposHabitacionPorCapacidad?.[String(personas - 1)]
  if (!rooms?.length) return null
  const best = rooms.reduce((min, r) => (r.precioPorNoche + r.precioPorPersona) < (min.precioPorNoche + min.precioPorPersona) ? r : min, rooms[0])
  return { tipo: best.tipoHabitacion, precioPorNoche: best.precioPorNoche, precioPorPersona: best.precioPorPersona, cap: personas - 1, total: best.precioPorNoche + best.precioPorPersona, habitacionesDisponibles: best.habitacionesDisponibles || [] }
}

function getHotelCombos(grupo) {
  const personas = busqueda.value.cantidadPersonas
  const combo = _getComboHabs(grupo); const aprox = _getComboAproximado(grupo, personas); const extra = _getPersonaExtraMin(grupo, personas)
  if (!combo && !aprox && !extra) return null
  return { combo, aprox, extra }
}

// ── Helpers UI ────────────────────────────────────────────────
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}
function formatDuracion(min) {
  if (!min || min === 9999) return '--'
  const h = Math.floor(min / 60), m = min % 60
  return `${h}h${m > 0 ? ` ${m}m` : ''}`
}

// ── Helpers para IDs de vuelos ────────────────────────────────
function parseVueloId(compositeId) {
  if (!compositeId) return null
  const parts = String(compositeId).split('-')
  const val   = parseFloat(parts[parts.length - 1])
  return Number.isFinite(val) ? Math.round(val) : null
}
function parseProveedorId(compositeId) {
  if (!compositeId) return null
  return parseInt(String(compositeId).split('-')[0]) || null
}
function claseToId(clase) { return clase === 'ejecutiva' ? 2 : 1 }

// ── PRE-CREACIÓN DE RESERVA EN BACKGROUND (paquetes) ─────────
// Se dispara al confirmar vuelo(s), antes de elegir hotel.
// Reserva.vue awaits window.__reservaPromise en onMounted.
async function precrearReservacion(vueloData, vueloRegresoData) {
  try {
    // PASO 1: crear la reservación (tipo 3 = paquete)
    const res1 = await fetch(`${API}/api/reservaciones`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: 3 }),
    })
    if (!res1.ok) return null
    const reserva = await res1.json()

    // PASO 2: agregar detalle de vuelo(s)
    const pax         = busqueda.value.cantidadPersonas || 1
    const proveedorId = parseProveedorId(vueloData.id)
    const vuelosArr   = [
      { vueloId: parseVueloId(vueloData.id), claseId: claseToId(vueloData.clase), cantidadPasajeros: pax }
    ]
    if (vueloRegresoData) {
      vuelosArr.push({ vueloId: parseVueloId(vueloRegresoData.id), claseId: claseToId(vueloRegresoData.clase), cantidadPasajeros: pax })
    }

    const res2 = await fetch(`${API}/api/reservaciones/detalle/vuelo`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reservacion_id: reserva.id, proveedor_id: proveedorId, vuelos: vuelosArr }),
    })

    let detalle = null
    if (res2.ok) detalle = await res2.json()

    // expiresAt viene SIEMPRE del backend, nunca calculado localmente.
    // Preferencia: fechaExpiracion del detalle/vuelo > fecha_expiracion de la reserva
    let expiresAt = 0
    if (detalle?.detalle?.fechaExpiracion) {
      expiresAt = new Date(detalle.detalle.fechaExpiracion).getTime()
    } else if (reserva.fecha_expiracion) {
      expiresAt = new Date(reserva.fecha_expiracion.replace(' ', 'T')).getTime()
    }
    if (!expiresAt || expiresAt <= Date.now()) expiresAt = Date.now() + 600_000
    const segundos = Math.max(30, Math.floor((expiresAt - Date.now()) / 1000))

    return { reserva, detalle, segundos, expiresAt }
  } catch { return null }
}

// ── Selección vuelo ───────────────────────────────────────────
function seleccionarVuelo(vuelo) {
  const data = {
    id: vuelo.id,
    origenCodigo: vuelo.origenCodigo, destinoCodigo: vuelo.destinoCodigo,
    aerolinea: vuelo.aerolinea, numeroVuelo: vuelo.numeroVuelo,
    horaSalida: vuelo.horaSalida, horaLlegada: vuelo.horaLlegada,
    duracionMinutos: vuelo.duracionMinutos, escalas: vuelo.escalas,
    clase: vuelo.claseSeleccionada,
    precio: vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva : vuelo.precioTurista,
    asientos: vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva : vuelo.asientosTurista,
  }

  if (paso.value === 1) {
    vueloSel.value = data
    if (esIdaVuelta.value) {
      // Ida y vuelta: avanzar a paso 2, aún falta el regreso
      paso.value = 2
    } else {
      // Solo ida: ya tenemos el vuelo → disparar pre-creación ahora
      paso.value = pasoHotel.value
      window.__reservaPromise = null
      window.__reservaPromise = precrearReservacion(data, null)
    }

  } else if (paso.value === 2 && esIdaVuelta.value) {
    // Regreso confirmado → ahora sí tenemos ambos vuelos → disparar pre-creación
    vueloRegresoSel.value = data
    paso.value = pasoHotel.value
    window.__reservaPromise = null
    window.__reservaPromise = precrearReservacion(vueloSel.value, data)
  }

  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ── Selección hotel ───────────────────────────────────────────
function seleccionarHotelHab(hab, grupo) {
  hotelSel.value = {
    tipo: 'habitacion', uid: hab.uid,
    tipoHabitacion: hab.tipoHabitacion, tipoCama: hab.tipoCama,
    precioNoche: hab.precioPorNoche,
    totalEstancia: hab.precioPorNoche * noches.value,
    habitacionesDisponibles: hab.habitacionesDisponibles || [],
    nombreHotel: grupo.nombreHotel, proveedorNombre: grupo.proveedorNombre,
    hotelId: grupo.hotelId, proveedorId: grupo.proveedorId,
    ciudad: grupo.ciudad, amenidades: grupo.amenidades,
    cantidadPersonas: busqueda.value.cantidadPersonas,
    checkIn: busqueda.value.checkIn, checkOut: busqueda.value.checkOut,
    noches: noches.value,
  }
}
function seleccionarHotelCombo(comboInfo, grupo) {
  hotelSel.value = {
    tipo: 'combo', habs: comboInfo.habs,
    precioNoche: comboInfo.total,
    totalEstancia: comboInfo.total * noches.value,
    nombreHotel: grupo.nombreHotel, proveedorNombre: grupo.proveedorNombre,
    hotelId: grupo.hotelId, proveedorId: grupo.proveedorId,
    ciudad: grupo.ciudad, amenidades: grupo.amenidades,
    checkIn: busqueda.value.checkIn, checkOut: busqueda.value.checkOut,
    noches: noches.value,
  }
}
function seleccionarHotelExtra(extraInfo, grupo) {
  hotelSel.value = {
    tipo: 'extra', tipoHabitacion: extraInfo.tipo,
    precioNoche: extraInfo.total,
    totalEstancia: extraInfo.total * noches.value,
    nombreHotel: grupo.nombreHotel, proveedorNombre: grupo.proveedorNombre,
    hotelId: grupo.hotelId, proveedorId: grupo.proveedorId,
    ciudad: grupo.ciudad,
    checkIn: busqueda.value.checkIn, checkOut: busqueda.value.checkOut,
    noches: noches.value,
  }
}

// ── Retroceder ────────────────────────────────────────────────
function retroceder() {
  if (paso.value === pasoHotel.value) {
    hotelSel.value = null
    // Cancelar la promesa: el usuario va a cambiar los vuelos
    window.__reservaPromise = null
    if (esIdaVuelta.value) {
      // Vuelve a seleccionar regreso (no invalida la ida)
      vueloRegresoSel.value = null
      paso.value = 2
    } else {
      vueloSel.value = null
      paso.value = 1
    }
  } else if (paso.value === 2) {
    vueloRegresoSel.value = null
    window.__reservaPromise = null
    paso.value = 1
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ── Reservar paquete ──────────────────────────────────────────
function reservarPaquete() {
  if (!vueloSel.value || !hotelSel.value) return
  if (esIdaVuelta.value && !vueloRegresoSel.value) return

  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.setItem('paquete_seleccionado', JSON.stringify({
    vuelo:            vueloSel.value,
    vueloRegreso:     vueloRegresoSel.value || null,
    hotel:            hotelSel.value,
    noches:           noches.value,
    cantidadPersonas: busqueda.value.cantidadPersonas,
    precioVuelo:      vueloSel.value.precio * busqueda.value.cantidadPersonas,
    precioRegreso:    vueloRegresoSel.value ? (vueloRegresoSel.value.precio * busqueda.value.cantidadPersonas) : 0,
    precioHotel:      hotelSel.value.precioNoche * noches.value,
    precioTotal:      precioTotal.value,
    busqueda:         busqueda.value,
  }))

  // window.__reservaPromise ya fue disparada en seleccionarVuelo
  // Reserva.vue la awaita en onMounted y la usa si está disponible
  router.push('/reservar')
}

// ── Init ──────────────────────────────────────────────────────
onMounted(() => {
  const rawV = state.resultadosVuelos  || null
  const rawR = state.resultadosRegreso || null
  const rawH = state.resultadosHoteles || null

  if (!busqueda.value.origen || !busqueda.value.destino) {
    error.value = 'Faltan datos de búsqueda.'; loading.value = false; return
  }
  if (rawV && rawH) {
    vuelos.value               = mapearVuelos(rawV)
    todasLasHabitaciones.value = mapearHoteles(rawH)
    if (rawR && Array.isArray(rawR)) vuelosRegreso.value = mapearVuelos(rawR)
    if (!vuelos.value.length || !todasLasHabitaciones.value.length) {
      error.value = 'No hay resultados para esta búsqueda.'
    }
  } else {
    error.value = 'No hay resultados. Modifica la búsqueda.'
  }
  loading.value = false
})
</script>