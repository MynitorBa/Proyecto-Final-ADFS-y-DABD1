<template>
  <div class="page">
    <Encabezado />

    <div class="rv-page">
      <div class="rv-layout">

        <!-- ═══ SIDEBAR ═══ -->
        <aside class="rv-sidebar" :class="{ 'rv-sidebar--collapsed': sidebarColapsado }">
          <div class="rv-sidebar__head" @click="sidebarColapsado = !sidebarColapsado">
            <h3 class="rv-sidebar__title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/>
                <line x1="11" y1="18" x2="13" y2="18"/>
              </svg>
              Filtros
              <span v-if="cantFiltrosActivos > 0" class="rv-sidebar__badge">{{ cantFiltrosActivos }}</span>
            </h3>
            <button class="rv-sidebar__toggle" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="16" height="16"
                :style="{ transform: sidebarColapsado ? 'rotate(0deg)' : 'rotate(180deg)', transition: 'transform .2s' }">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
          </div>

          <div class="rv-sidebar__body">
            <div class="rv-filter-group">
              <h4 class="rv-filter-group__title">Precio por persona</h4>
              <div class="rv-price-inputs">
                <div class="rv-price-input"><span>$</span><input type="number" v-model.number="filtros.precioMin" :min="0" placeholder="0" /></div>
                <span class="rv-price-sep">—</span>
                <div class="rv-price-input"><span>$</span><input type="number" v-model.number="filtros.precioMax" placeholder="9999" /></div>
              </div>
            </div>

            <div class="rv-filter-group">
              <h4 class="rv-filter-group__title">Clase disponible</h4>
              <div class="rv-checkboxes">
                <label class="rv-checkbox" v-for="c in clasesFilter" :key="c.val">
                  <input type="checkbox" v-model="filtros.clases" :value="c.val" />
                  <span class="rv-checkbox__box"></span>
                  <span class="rv-checkbox__label">{{ c.label }}</span>
                </label>
              </div>
            </div>

            <div class="rv-filter-group">
              <h4 class="rv-filter-group__title">Escalas</h4>
              <div class="rv-checkboxes">
                <label class="rv-checkbox" v-for="e in escalasOpts" :key="e.val">
                  <input type="checkbox" v-model="filtros.escalas" :value="e.val" />
                  <span class="rv-checkbox__box"></span>
                  <span class="rv-checkbox__label">{{ e.label }}</span>
                </label>
              </div>
            </div>

            <div class="rv-filter-group">
              <h4 class="rv-filter-group__title">Duración máxima</h4>
              <div class="rv-dur-btns">
                <button v-for="d in duracionOpts" :key="d.val"
                  :class="['rv-dur-btn', { 'rv-dur-btn--active': filtros.duracionMax === d.val }]"
                  @click="filtros.duracionMax = filtros.duracionMax === d.val ? 9999 : d.val"
                  type="button">{{ d.label }}</button>
              </div>
            </div>

            <div class="rv-filter-group" v-if="aerolineasDisponibles.length > 0">
              <h4 class="rv-filter-group__title">Aerolínea</h4>
              <div class="rv-checkboxes">
                <label class="rv-checkbox" v-for="a in aerolineasDisponibles" :key="a">
                  <input type="checkbox" v-model="filtros.aerolineas" :value="a" />
                  <span class="rv-checkbox__box"></span>
                  <span class="rv-checkbox__label">{{ a }}</span>
                </label>
              </div>
            </div>

            <div class="rv-filter-group">
              <h4 class="rv-filter-group__title">Horario de salida</h4>
              <div class="rv-horarios">
                <button v-for="h in horariosOpts" :key="h.val"
                  :class="['rv-horario-btn', { 'rv-horario-btn--active': filtros.horario === h.val }]"
                  @click="filtros.horario = filtros.horario === h.val ? '' : h.val" type="button">
                  <span v-html="h.icon"></span>
                  <span>{{ h.label }}</span>
                  <small>{{ h.rango }}</small>
                </button>
              </div>
            </div>

            <button v-if="hayFiltrosActivos" class="rv-btn rv-btn--ghost rv-sidebar__reset" @click="resetFiltros" type="button">
              Limpiar filtros
            </button>
          </div>
        </aside>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rv-main">

          <!-- Search bar -->
          <div class="rv-search-bar" :class="{ 'rv-search-bar--open': modificarAbierto }">
            <div class="rv-search-bar__summary" @click="toggleModificar">
              <div class="rv-search-bar__ruta">
                <span class="rv-search-bar__ciudad">{{ busqueda.origen }}</span>
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="14" height="14">
                  <path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/>
                </svg>
                <span class="rv-search-bar__ciudad">{{ busqueda.destino }}</span>
              </div>
              <div class="rv-search-bar__meta">
                <span class="rv-search-bar__meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {{ formatFecha(busqueda.fecha) }}
                </span>
                <span class="rv-search-bar__dot">·</span>
                <span class="rv-search-bar__meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  {{ busqueda.cantidadPasajeros }} pasajero{{ busqueda.cantidadPasajeros !== 1 ? 's' : '' }}
                </span>
              </div>
            </div>
            <button class="rv-search-bar__mod-btn" @click="toggleModificar" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"
                :style="{ transform: modificarAbierto ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
              {{ modificarAbierto ? 'Cerrar' : 'Modificar' }}
            </button>
          </div>

          <!-- Form modificar inline -->
          <transition name="rv-expand">
            <div v-if="modificarAbierto" class="rv-modificar-inline">
              <div class="rv-modificar-grid">

                <div class="rv-mod-section">
                  <p class="rv-mod-section__label">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M21,16L14,11V5A2,2 0 0,0 12,3A2,2 0 0,0 10,5V11L3,16V18L10,15.5V21L8,22.5V24L12,23L16,24V22.5L14,21V15.5L21,18V16Z"/></svg>
                    Origen
                  </p>
                  <div class="rv-mod-row">
                    <div class="rv-mod-field rv-ac-wrap">
                      <label class="rv-mod-label">País</label>
                      <input class="rv-mod-input" type="text" v-model="form.origenPaisQ"
                        @input="onFormOPaisInput" @blur="blurClose(() => form.origenPaisSug = [])"
                        placeholder="Guatemala..." autocomplete="off" />
                      <ul v-if="form.origenPaisSug.length" class="rv-ac-list">
                        <li v-for="p in form.origenPaisSug" :key="p.country">
                          <button type="button" @click="selFormOPais(p)">{{ p.country }}</button>
                        </li>
                      </ul>
                    </div>
                    <div class="rv-mod-field rv-ac-wrap">
                      <label class="rv-mod-label">Ciudad <span v-if="form.origenCiudadLoading" class="rv-mod-hint">cargando...</span></label>
                      <input class="rv-mod-input" type="text" v-model="form.origenCiudadQ"
                        @input="onFormOCiudadInput" @blur="blurClose(() => form.origenCiudadSug = [])"
                        :disabled="!form.origenPaisSel || form.origenCiudadLoading"
                        placeholder="Guatemala City..." autocomplete="off" />
                      <ul v-if="form.origenCiudadSug.length" class="rv-ac-list">
                        <li v-for="c in form.origenCiudadSug" :key="c">
                          <button type="button" @click="selFormOCiudad(c)">{{ c }}</button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="rv-mod-section">
                  <p class="rv-mod-section__label">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M2.5 19h19v2h-19v-2m7.18-1.68L5.07 15.1l2.8-.75 3.53 3.03 6.03-1.61a1.5 1.5 0 0 1 .78 2.9L9.85 20.2a2 2 0 0 1-1.5-.13l-.67-.75m-1.9-5.88l1.42.88-2.09.55-.25-.97 2.56-1.23a2 2 0 0 1 1.5.13l5.74 3.55 2.09-.56-6.52-7.05L11.85 7l7.25 7.83-8.83 2.37a2 2 0 0 1-1.5-.13l-1.64-.88z"/></svg>
                    Destino
                  </p>
                  <div class="rv-mod-row">
                    <div class="rv-mod-field rv-ac-wrap">
                      <label class="rv-mod-label">País</label>
                      <input class="rv-mod-input" type="text" v-model="form.destinoPaisQ"
                        @input="onFormDPaisInput" @blur="blurClose(() => form.destinoPaisSug = [])"
                        placeholder="Mexico..." autocomplete="off" />
                      <ul v-if="form.destinoPaisSug.length" class="rv-ac-list">
                        <li v-for="p in form.destinoPaisSug" :key="p.country">
                          <button type="button" @click="selFormDPais(p)">{{ p.country }}</button>
                        </li>
                      </ul>
                    </div>
                    <div class="rv-mod-field rv-ac-wrap">
                      <label class="rv-mod-label">Ciudad <span v-if="form.destinoCiudadLoading" class="rv-mod-hint">cargando...</span></label>
                      <input class="rv-mod-input" type="text" v-model="form.destinoCiudadQ"
                        @input="onFormDCiudadInput" @blur="blurClose(() => form.destinoCiudadSug = [])"
                        :disabled="!form.destinoPaisSel || form.destinoCiudadLoading"
                        placeholder="Mexico City..." autocomplete="off" />
                      <ul v-if="form.destinoCiudadSug.length" class="rv-ac-list">
                        <li v-for="c in form.destinoCiudadSug" :key="c">
                          <button type="button" @click="selFormDCiudad(c)">{{ c }}</button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="rv-mod-section rv-mod-section--row">
                  <div class="rv-mod-field rv-mod-field--full">
                    <div class="rv-trip-toggle">
                      <button :class="['rv-trip-btn', { 'rv-trip-btn--active': form.tipoVuelo === 'ida' }]"
                        @click="form.tipoVuelo = 'ida'" type="button">Solo ida</button>
                      <button :class="['rv-trip-btn', { 'rv-trip-btn--active': form.tipoVuelo === 'idaVuelta' }]"
                        @click="form.tipoVuelo = 'idaVuelta'" type="button">Ida y vuelta</button>
                    </div>
                  </div>
                  <div class="rv-mod-field">
                    <label class="rv-mod-label">Fecha de ida</label>
                    <input class="rv-mod-input" type="date" v-model="form.fecha" :min="hoy" />
                  </div>
                  <div class="rv-mod-field" v-if="form.tipoVuelo === 'idaVuelta'">
                    <label class="rv-mod-label">Fecha de regreso</label>
                    <input class="rv-mod-input" type="date" v-model="form.fechaRegreso" :min="minFechaRegresoForm" />
                  </div>
                  <div class="rv-mod-field">
                    <label class="rv-mod-label">Pasajeros</label>
                    <select class="rv-mod-input" v-model="form.cantidadPasajeros">
                      <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Pasajero' : 'Pasajeros' }}</option>
                    </select>
                  </div>
                  <div class="rv-mod-field rv-mod-field--cta">
                    <p v-if="modError" class="rv-mod-error">⚠ {{ modError }}</p>
                    <button class="rv-mod-buscar" @click="rebuscar" :disabled="buscando" type="button">
                      <div v-if="buscando" class="rv-spinner rv-spinner--sm"></div>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
                      {{ buscando ? 'Buscando...' : 'Buscar vuelos' }}
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </transition>

          <!-- Indicador de pasos -->
          <div v-if="esIdaVuelta && !loading" class="rv-pasos">
            <div :class="['rv-paso', { 'rv-paso--activo': paso === 1, 'rv-paso--done': paso === 2 }]">
              <span class="rv-paso__num">{{ paso === 2 ? '✓' : '1' }}</span>
              <div>
                <span class="rv-paso__label">Vuelo de ida</span>
                <span class="rv-paso__sub">{{ busqueda.origen }} → {{ busqueda.destino }} · {{ formatFecha(busqueda.fecha) }}</span>
              </div>
            </div>
            <div class="rv-paso__sep">→</div>
            <div :class="['rv-paso', { 'rv-paso--activo': paso === 2, 'rv-paso--pendiente': paso === 1 }]">
              <span class="rv-paso__num">2</span>
              <div>
                <span class="rv-paso__label">Vuelo de regreso</span>
                <span class="rv-paso__sub">{{ busqueda.destino }} → {{ busqueda.origen }} · {{ formatFecha(busqueda.fechaRegreso) }}</span>
              </div>
            </div>
            <button v-if="paso === 2" class="rv-btn rv-btn--ghost rv-paso__back" @click="paso = 1; seleccionadoRegreso = null" type="button">
              ← Cambiar ida
            </button>
          </div>

          <!-- Toolbar -->
          <div class="rv-toolbar">
            <p class="rv-toolbar__count">
              <strong>{{ vuelosFiltrados.length }}</strong>
              vuelo{{ vuelosFiltrados.length !== 1 ? 's' : '' }}
              <span v-if="hayFiltrosActivos" class="rv-toolbar__filtered"> · {{ vuelos.length }} total</span>
            </p>
            <div class="rv-sort">
              <select v-model="ordenar" class="rv-sort__select">
                <option value="precio-asc">Precio ↑</option>
                <option value="precio-desc">Precio ↓</option>
                <option value="duracion">Menor duración</option>
                <option value="salida">Hora salida</option>
                <option value="escalas">Menos escalas</option>
              </select>
            </div>
          </div>

          <!-- Chips -->
          <div v-if="hayFiltrosActivos" class="rv-chips-activos">
            <button v-if="filtros.precioMin > 0 || filtros.precioMax < 9999" class="rv-chip" @click="filtros.precioMin=0; filtros.precioMax=9999" type="button">${{ filtros.precioMin }}–${{ filtros.precioMax }} ✕</button>
            <button v-for="c in filtros.clases" :key="'c'+c" class="rv-chip" @click="filtros.clases = filtros.clases.filter(x=>x!==c)" type="button">{{ c === 'economica' ? 'Económica' : 'Ejecutiva' }} ✕</button>
            <button v-for="e in filtros.escalas" :key="'e'+e" class="rv-chip" @click="filtros.escalas = filtros.escalas.filter(x=>x!==e)" type="button">{{ e === 0 ? 'Directo' : e === 1 ? '1 escala' : '2+ escalas' }} ✕</button>
            <button v-if="filtros.duracionMax < 9999" class="rv-chip" @click="filtros.duracionMax=9999" type="button">Máx. {{ formatDuracion(filtros.duracionMax) }} ✕</button>
            <button v-for="a in filtros.aerolineas" :key="'a'+a" class="rv-chip" @click="filtros.aerolineas = filtros.aerolineas.filter(x=>x!==a)" type="button">{{ a }} ✕</button>
            <button v-if="filtros.horario" class="rv-chip" @click="filtros.horario=''" type="button">{{ horariosOpts.find(h=>h.val===filtros.horario)?.label }} ✕</button>
            <button class="rv-chip rv-chip--clear" @click="resetFiltros" type="button">Limpiar todo</button>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="rv-empty">
            <div class="rv-spinner"></div>
            <p>Consultando aerolíneas...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error && vuelos.length === 0" class="rv-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5"/><circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/></svg>
            <p>{{ error }}</p>
            <button class="rv-btn rv-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
          </div>

          <!-- Proveedores parciales -->
          <div v-if="!loading && erroresProveedores.length > 0" class="rv-warn">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13.5"/><circle cx="12" cy="17" r="1" fill="currentColor" stroke="none"/></svg>
            {{ erroresProveedores.length }} proveedor{{ erroresProveedores.length !== 1 ? 'es sin respuesta' : ' sin respuesta' }}. Resultados pueden estar incompletos.
          </div>

          <!-- Sin vuelos -->
          <div v-if="!loading && vuelos.length === 0 && !error" class="rv-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="48" height="48"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
            <p class="rv-empty__title">Sin vuelos disponibles</p>
            <p class="rv-empty__sub">No hay vuelos de <strong>{{ busqueda.origen }}</strong> a <strong>{{ busqueda.destino }}</strong> para el {{ formatFecha(busqueda.fecha) }}.</p>
            <button class="rv-btn rv-btn--yellow" @click="toggleModificar" type="button">Buscar otra ruta</button>
          </div>

          <!-- Sin resultados por filtros -->
          <div v-if="!loading && vuelos.length > 0 && vuelosFiltrados.length === 0" class="rv-empty">
            <p class="rv-empty__title">Ningún vuelo coincide</p>
            <p class="rv-empty__sub">{{ vuelos.length }} disponible{{ vuelos.length !== 1 ? 's' : '' }} sin filtros.</p>
            <button class="rv-btn rv-btn--ghost" @click="resetFiltros" type="button">Quitar filtros</button>
          </div>

          <!-- ═══ LISTA DE VUELOS ═══ -->
          <div v-if="!loading && (paso === 1 ? vuelosFiltrados.length > 0 : vuelosRegreso.length > 0)" class="rv-lista">

            <div v-if="esIdaVuelta && paso === 2" class="rv-regreso-header">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="16" height="16"><path d="M7 23l-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>
              Elige tu vuelo de regreso — {{ busqueda.destino }} → {{ busqueda.origen }} · {{ formatFecha(busqueda.fechaRegreso) }}
            </div>

            <!-- Cada card envuelta con su sección de comentarios -->
            <div
              v-for="vuelo in (paso === 1 ? vuelosFiltrados : vuelosRegreso)"
              :key="vuelo.id"
              :data-vuelo-key="getVueloKey(vuelo)"
              class="rv-card-wrap"
            >

              <!-- ─── CARD DEL VUELO ─── -->
              <article class="rv-card" :class="{ 'rv-card--sel': (paso === 1 ? seleccionado : seleccionadoRegreso) === vuelo.id }">

                <div class="rv-card__head">
                  <div class="rv-card__aerolinea">
                    <div class="rv-card__logo">
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="15" height="15"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                    </div>
                    <div>
                      <span class="rv-card__nombre">{{ vuelo.aerolinea }}</span>
                      <span class="rv-card__num">Nro. {{ vuelo.numeroVuelo }}</span>
                    </div>
                  </div>
                  <div class="rv-card__tags">
                    <span v-if="vuelo.escalas === 0" class="rv-tag rv-tag--directo">✓ Directo</span>
                    <span v-else class="rv-tag rv-tag--escala">{{ vuelo.escalas }} escala{{ vuelo.escalas !== 1 ? 's' : '' }}</span>
                    <span v-if="vuelo.asientosTurista > 0 && vuelo.asientosTurista <= 5" class="rv-tag rv-tag--urgente">¡Últimos!</span>
                    <span v-if="getPromedioVuelo(vuelo) > 0" class="rv-tag rv-tag--rating">
                      ★ {{ getPromedioVuelo(vuelo).toFixed(1) }}
                      <small>({{ getResenasRaizVuelo(vuelo).length }})</small>
                    </span>
                  </div>
                </div>

                <div class="rv-card__ruta">
                  <div class="rv-card__punto">
                    <span class="rv-card__iata">{{ vuelo.origenCodigo }}</span>
                    <span class="rv-card__ciudad">{{ vuelo.origenCiudad }}</span>
                    <span class="rv-card__hora">{{ vuelo.horaSalida }}</span>
                  </div>
                  <div class="rv-card__medio">
                    <span class="rv-card__dur">{{ formatDuracion(vuelo.duracionMinutos) }}</span>
                    <div class="rv-card__track">
                      <div class="rv-card__dot"></div>
                      <div class="rv-card__line"></div>
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="18" height="18" class="rv-card__avion"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <div class="rv-card__line"></div>
                      <div class="rv-card__dot"></div>
                    </div>
                    <div v-if="vuelo.paradas?.length" class="rv-card__paradas">
                      <span v-for="p in vuelo.paradas" :key="p.codigo" class="rv-card__parada">{{ p.codigo }}<template v-if="p.ciudad"> · {{ p.ciudad }}</template></span>
                    </div>
                  </div>
                  <div class="rv-card__punto rv-card__punto--r">
                    <span class="rv-card__iata">{{ vuelo.destinoCodigo }}</span>
                    <span class="rv-card__ciudad">{{ vuelo.destinoCiudad }}</span>
                    <span class="rv-card__hora">{{ vuelo.horaLlegada }}</span>
                  </div>
                </div>

                <div class="rv-card__meta">
                  <span class="rv-card__meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                    {{ vuelo.avionMarca }} {{ vuelo.avionModelo }}
                  </span>
                  <span v-if="vuelo.tiempoEscalaMinutos" class="rv-card__meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
                    {{ formatDuracion(vuelo.tiempoEscalaMinutos) }} en escalas
                  </span>
                </div>

                <div class="rv-card__precios">
                  <button class="rv-precio-btn"
                    :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'economica', 'rv-precio-btn--out': vuelo.asientosTurista === 0 }"
                    @click.stop="vuelo.claseSeleccionada = 'economica'" :disabled="vuelo.asientosTurista === 0" type="button">
                    <span class="rv-precio-btn__clase">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M20 9V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v2"/><path d="M2 11v5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H6v-2a2 2 0 0 0-4 0z"/></svg>
                      Económica
                    </span>
                    <span class="rv-precio-btn__val">${{ Number(vuelo.precioTurista).toFixed(2) }}</span>
                    <span class="rv-precio-btn__seats">
                      <template v-if="vuelo.asientosTurista === 0">Agotado</template>
                      <template v-else-if="vuelo.asientosTurista <= 5">¡Solo {{ vuelo.asientosTurista }}!</template>
                      <template v-else>{{ vuelo.asientosTurista }} asientos</template>
                    </span>
                  </button>
                  <button class="rv-precio-btn rv-precio-btn--ejec"
                    :class="{ 'rv-precio-btn--sel': vuelo.claseSeleccionada === 'ejecutiva', 'rv-precio-btn--out': vuelo.asientosEjecutiva === 0 }"
                    @click.stop="vuelo.claseSeleccionada = 'ejecutiva'" :disabled="vuelo.asientosEjecutiva === 0" type="button">
                    <span class="rv-precio-btn__clase">
                      <svg viewBox="0 0 24 24" fill="#FFCC00" width="11" height="11"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                      Ejecutiva
                    </span>
                    <span class="rv-precio-btn__val rv-precio-btn__val--ejec">${{ Number(vuelo.precioEjecutiva).toFixed(2) }}</span>
                    <span class="rv-precio-btn__seats">
                      <template v-if="vuelo.asientosEjecutiva === 0">Agotado</template>
                      <template v-else-if="vuelo.asientosEjecutiva <= 5">¡Solo {{ vuelo.asientosEjecutiva }}!</template>
                      <template v-else>{{ vuelo.asientosEjecutiva }} asientos</template>
                    </span>
                  </button>
                </div>

                <div v-if="vuelo.escalas > 0 && vuelo.tramos?.length" class="rv-itinerario">
                  <button class="rv-itinerario__toggle" @click.stop="toggleTramos(vuelo.id)" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="11" height="11"
                      :style="{ transform: tramosAbiertos[vuelo.id] ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }">
                      <polyline points="6 9 12 15 18 9"/>
                    </svg>
                    {{ tramosAbiertos[vuelo.id] ? 'Ocultar itinerario' : `Itinerario (${vuelo.tramos.length} tramos)` }}
                  </button>
                  <transition name="rv-slide">
                    <div v-if="tramosAbiertos[vuelo.id]" class="rv-itinerario__body">
                      <div v-for="(tramo, idx) in vuelo.tramos" :key="tramo.id ?? idx" class="rv-tramo">
                        <div class="rv-tramo__tl">
                          <div class="rv-tramo__dot" :class="{ 'rv-tramo__dot--o': idx === 0 }"></div>
                          <div v-if="idx < vuelo.tramos.length - 1" class="rv-tramo__line"></div>
                          <div class="rv-tramo__dot rv-tramo__dot--d"></div>
                        </div>
                        <div class="rv-tramo__content">
                          <div class="rv-tramo__airport">
                            <span class="rv-tramo__iata">{{ tramo.origenCodigo }}</span>
                            <div><span class="rv-tramo__nombre">{{ tramo.origenNombre || tramo.origenCiudad }}</span><span class="rv-tramo__pais">{{ tramo.origenPais }}</span></div>
                            <span class="rv-tramo__hora rv-tramo__hora--s">{{ formatHora(tramo.horaSalida) }}</span>
                          </div>
                          <div class="rv-tramo__info">
                            <span class="rv-tramo__badge">{{ tramo.numeroVuelo }}</span>
                            <span class="rv-tramo__dur">{{ formatDuracion(tramo.duracionMinutos) }}</span>
                            <span class="rv-tramo__avion">{{ tramo.avionMarca }} {{ tramo.avionModelo }}</span>
                            <template v-if="tramo.tripulantes?.length">
                              <span class="rv-tramo__sep">·</span>
                              <span v-for="t in tramo.tripulantes" :key="t.id" class="rv-tramo__crew">{{ t.nombreCompleto }} <em>({{ t.nombreRol }})</em></span>
                            </template>
                          </div>
                          <div class="rv-tramo__airport rv-tramo__airport--l">
                            <span class="rv-tramo__iata">{{ tramo.destinoCodigo }}</span>
                            <div><span class="rv-tramo__nombre">{{ tramo.destinoNombre || tramo.destinoCiudad }}</span><span class="rv-tramo__pais">{{ tramo.destinoPais }}</span></div>
                            <span class="rv-tramo__hora rv-tramo__hora--l">{{ formatHora(tramo.horaLlegada) }}</span>
                          </div>
                          <div v-if="idx < vuelo.tramos.length - 1" class="rv-tramo__escala">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="10" height="10"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
                            Escala en {{ tramo.destinoCiudad }} · {{ tramo.destinoPais }}
                          </div>
                        </div>
                      </div>
                      <div class="rv-tramo rv-tramo--final">
                        <div class="rv-tramo__tl"><div class="rv-tramo__dot rv-tramo__dot--f"></div></div>
                        <div class="rv-tramo__airport">
                          <span class="rv-tramo__iata">{{ vuelo.tramos[vuelo.tramos.length-1].destinoCodigo }}</span>
                          <div><span class="rv-tramo__nombre">{{ vuelo.tramos[vuelo.tramos.length-1].destinoNombre || vuelo.tramos[vuelo.tramos.length-1].destinoCiudad }}</span><span class="rv-tramo__pais">{{ vuelo.tramos[vuelo.tramos.length-1].destinoPais }}</span></div>
                          <span class="rv-tramo__hora rv-tramo__hora--l">{{ formatHora(vuelo.tramos[vuelo.tramos.length-1].horaLlegada) }}</span>
                        </div>
                      </div>
                    </div>
                  </transition>
                </div>

                <div class="rv-card__foot">
                  <div class="rv-card__foot-info">
                    <span class="rv-card__foot-clase">
                      <template v-if="vuelo.claseSeleccionada === 'ejecutiva'">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="12" height="12"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                        Ejecutiva
                      </template>
                      <template v-else>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M20 9V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v2"/><path d="M2 11v5a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a2 2 0 0 0-4 0v2H6v-2a2 2 0 0 0-4 0z"/></svg>
                        Económica
                      </template>
                    </span>
                    <span class="rv-card__foot-precio">
                      ${{ Number(vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva : vuelo.precioTurista).toFixed(2) }}
                      <small>/ persona</small>
                    </span>
                  </div>
                  <button class="rv-btn rv-btn--yellow rv-card__cta" @click="seleccionarVuelo(vuelo)"
                    :disabled="vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva === 0 : vuelo.asientosTurista === 0"
                    type="button">
                    Seleccionar
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                  </button>
                </div>

              </article>

              <!-- ─── COMENTARIOS DEL VUELO (solo lectura) ─── -->
              <div
                v-if="getVueloKey(vuelo) && (comentariosLoadingSet.has(getVueloKey(vuelo)) || yaObservadoRV.has(getVueloKey(vuelo)))"
                class="rv-resenas"
              >
                <!-- Cargando -->
                <div v-if="comentariosLoadingSet.has(getVueloKey(vuelo))" class="rv-resenas__loading">
                  <div class="rv-spinner rv-spinner--sm"></div>
                  <span>Cargando opiniones...</span>
                </div>

                <!-- Con comentarios -->
                <template v-else-if="getComentariosRaizVuelo(vuelo).length > 0">
                  <div class="rv-resenas__head">
                    <div class="rv-resenas__rating-wrap">
                      <span class="rv-resenas__avg">{{ getPromedioVuelo(vuelo).toFixed(1) }}</span>
                      <div class="rv-resenas__stars-row">
                        <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
                          :fill="n <= Math.round(getPromedioVuelo(vuelo)) ? '#FFCC00' : 'none'"
                          :stroke="n <= Math.round(getPromedioVuelo(vuelo)) ? '#FFCC00' : '#d0c9be'"
                          stroke-width="2" width="14" height="14">
                          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                        </svg>
                      </div>
                      <span class="rv-resenas__count">
                        {{ getResenasRaizVuelo(vuelo).length }} reseña{{ getResenasRaizVuelo(vuelo).length !== 1 ? 's' : '' }}
                      </span>
                    </div>
                    <h4 class="rv-resenas__title">Opiniones de pasajeros</h4>
                  </div>
                  <div class="rv-resenas__lista">
                    <ComentarioNodo
                      v-for="c in getComentariosRaizVuelo(vuelo)"
                      :key="c.id"
                      :comentario="c"
                      :getHijos="(id) => getHijosVuelo(vuelo, id)"
                      :estadoNodos="estadoNodosRV"
                      :haySession="false"
                      :formatFecha="formatFechaCorta"
                      @votar="() => {}"
                      @toggleForm="() => {}"
                      @toggleExpandido="toggleExpandidoRV"
                      @enviarRespuesta="() => {}"
                      @textoChange="() => {}"
                    />
                  </div>
                </template>

                <!-- Sin opiniones -->
                <div v-else-if="yaObservadoRV.has(getVueloKey(vuelo))" class="rv-resenas__empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="18" height="18">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  Aún no hay opiniones para este vuelo.
                </div>
              </div>
              <!-- ─── FIN COMENTARIOS ─── -->

            </div>
          </div>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import ComentarioNodo from '../components/Comentarionodo.vue'
import '../styles/resultadosvuelos.css'

const router = useRouter()
const API    = 'http://localhost:8080'

// ── Estado inicial desde history.state ───────────────────────
const state = history.state || {}
const busqueda = ref({
  origen:            state.busqueda?.origen            || '',
  origenPais:        state.busqueda?.origenPais        || '',
  destino:           state.busqueda?.destino           || '',
  destinoPais:       state.busqueda?.destinoPais       || '',
  fecha:             state.busqueda?.fecha             || '',
  fechaRegreso:      state.busqueda?.fechaRegreso      || '',
  cantidadPasajeros: state.busqueda?.cantidadPasajeros || 1,
  tipoVuelo:         state.busqueda?.tipoVuelo         || 'ida',
})

const resultadosRaw        = state.resultados || null
const resultadosRegresoRaw = state.resultadosRegreso || null

// ── Estado principal ──────────────────────────────────────────
const vuelos               = ref([])
const vuelosRegreso        = ref([])
const loading              = ref(true)
const buscando             = ref(false)
const error                = ref('')
const erroresProveedores   = ref([])
const seleccionado         = ref(null)
const seleccionadoRegreso  = ref(null)
const paso                 = ref(1)
const ordenar              = ref('precio-asc')
const modificarAbierto     = ref(false)
const modError             = ref('')
const sidebarColapsado     = ref(false)
const tramosAbiertos       = reactive({})
const hoy                  = new Date().toISOString().split('T')[0]

const esIdaVuelta = computed(() => busqueda.value.tipoVuelo === 'idaVuelta')

function toggleTramos(id) { tramosAbiertos[id] = !tramosAbiertos[id] }

// ── Filtros ───────────────────────────────────────────────────
const filtros = ref({ precioMin: 0, precioMax: 9999, clases: [], escalas: [], duracionMax: 9999, aerolineas: [], horario: '' })
const clasesFilter = [{ val: 'economica', label: 'Económica' }, { val: 'ejecutiva', label: 'Ejecutiva' }]
const escalasOpts  = [{ val: 0, label: 'Solo directos' }, { val: 1, label: '1 escala' }, { val: 2, label: '2+ escalas' }]
const duracionOpts = [{ val: 180, label: '< 3h' }, { val: 360, label: '< 6h' }, { val: 720, label: '< 12h' }, { val: 1440, label: '< 24h' }]
const horariosOpts = [
  { val: 'madrugada', icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`, label: 'Madrugada', rango: '00:00–05:59' },
  { val: 'manana',    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M17 18a5 5 0 0 0-10 0"/><line x1="12" y1="9" x2="12" y2="2"/><line x1="4.22" y1="10.22" x2="5.64" y2="11.64"/><line x1="1" y1="18" x2="3" y2="18"/><line x1="21" y1="18" x2="23" y2="18"/><line x1="18.36" y1="11.64" x2="19.78" y2="10.22"/><polyline points="8 6 12 2 16 6"/></svg>`, label: 'Mañana', rango: '06:00–11:59' },
  { val: 'tarde',     icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>`, label: 'Tarde', rango: '12:00–17:59' },
  { val: 'noche',     icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/><path d="M19 3v4M21 5h-4"/></svg>`, label: 'Noche', rango: '18:00–23:59' },
]

const hayFiltrosActivos = computed(() =>
  filtros.value.precioMin > 0 || filtros.value.precioMax < 9999 ||
  filtros.value.clases.length > 0 || filtros.value.escalas.length > 0 ||
  filtros.value.duracionMax < 9999 || filtros.value.aerolineas.length > 0 || !!filtros.value.horario
)
const cantFiltrosActivos = computed(() => {
  let n = 0
  if (filtros.value.precioMin > 0 || filtros.value.precioMax < 9999) n++
  n += filtros.value.clases.length + filtros.value.escalas.length + filtros.value.aerolineas.length
  if (filtros.value.duracionMax < 9999) n++
  if (filtros.value.horario) n++
  return n
})
function resetFiltros() {
  filtros.value = { precioMin: 0, precioMax: 9999, clases: [], escalas: [], duracionMax: 9999, aerolineas: [], horario: '' }
}

const aerolineasDisponibles = computed(() => [...new Set(vuelos.value.map(v => v.aerolinea).filter(Boolean))])

// ── Computed vuelosFiltrados ─────────────────────────────────
// IMPORTANTE: este computed debe definirse ANTES del watch que lo usa
const vuelosFiltrados = computed(() => {
  let list = vuelos.value
  if (filtros.value.precioMin > 0)    list = list.filter(v => v.precioTurista >= filtros.value.precioMin)
  if (filtros.value.precioMax < 9999) list = list.filter(v => v.precioTurista <= filtros.value.precioMax)
  if (filtros.value.clases.length > 0) {
    const soloEjec = filtros.value.clases.includes('ejecutiva') && !filtros.value.clases.includes('economica')
    if (soloEjec) list = list.filter(v => v.asientosEjecutiva > 0 && v.precioEjecutiva > 0)
  }
  if (filtros.value.escalas.length > 0) list = list.filter(v => {
    const sel = filtros.value.escalas
    return sel.includes(2) ? (sel.includes(v.escalas) || v.escalas >= 2) : sel.includes(v.escalas)
  })
  if (filtros.value.duracionMax < 9999) list = list.filter(v => v.duracionMinutos <= filtros.value.duracionMax)
  if (filtros.value.aerolineas.length > 0) list = list.filter(v => filtros.value.aerolineas.includes(v.aerolinea))
  if (filtros.value.horario) {
    const rangos = { madrugada:[0,6], manana:[6,12], tarde:[12,18], noche:[18,24] }
    const [min, max] = rangos[filtros.value.horario] || [0,24]
    list = list.filter(v => { const h = parseInt(v.horaSalida?.split(':')[0] ?? 0); return h >= min && h < max })
  }
  return [...list].sort((a, b) => {
    switch (ordenar.value) {
      case 'precio-asc':  return a.precioTurista - b.precioTurista
      case 'precio-desc': return b.precioTurista - a.precioTurista
      case 'duracion':    return (a.duracionMinutos||0) - (b.duracionMinutos||0)
      case 'salida':      return (a.horaSalida||'').localeCompare(b.horaSalida||'')
      case 'escalas':     return a.escalas - b.escalas
      default: return 0
    }
  })
})

// ══ COMENTARIOS VUELOS (solo lectura) ═════════════════════════
// key = `${proveedorId}::${rutaId}`
const comentariosVuelos     = ref({})
const comentariosLoadingSet = ref(new Set())
const yaObservadoRV         = ref(new Set())
const estadoNodosRV         = ref({})
let   vueloObserver         = null

function getVueloKey(vuelo) {
  const pid = parseProveedorId(vuelo.id)
  const rid = vuelo.rutaId
  if (!pid || !rid) return null
  return `${pid}::${rid}`
}

function getComentariosVuelo(vuelo) {
  const key = getVueloKey(vuelo)
  return key ? (comentariosVuelos.value[key] ?? []) : []
}

function getComentariosRaizVuelo(vuelo) {
  return getComentariosVuelo(vuelo).filter(c => c.comentarioPadreId === null)
}

function getResenasRaizVuelo(vuelo) {
  return getComentariosVuelo(vuelo).filter(c => c.comentarioPadreId === null && c.cantidadEstrellas !== null)
}

function getHijosVuelo(vuelo, parentId) {
  return getComentariosVuelo(vuelo).filter(c => c.comentarioPadreId === parentId)
}

function getPromedioVuelo(vuelo) {
  const r = getResenasRaizVuelo(vuelo)
  if (!r.length) return 0
  return r.reduce((s, c) => s + (c.cantidadEstrellas ?? 0), 0) / r.length
}

function toggleExpandidoRV(id) {
  estadoNodosRV.value = {
    ...estadoNodosRV.value,
    [id]: {
      ...(estadoNodosRV.value[id] ?? { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: null }),
      expandido: !estadoNodosRV.value[id]?.expandido
    }
  }
}

async function cargarComentariosVuelo(proveedorId, rutaId, key) {
  if (yaObservadoRV.value.has(key)) return
  yaObservadoRV.value = new Set([...yaObservadoRV.value, key])
  comentariosLoadingSet.value = new Set([...comentariosLoadingSet.value, key])
  try {
    const res = await fetch(`${API}/api/comentarios/vuelo/${proveedorId}/${rutaId}`, { credentials: 'include' })
    if (!res.ok) throw new Error()
    const data = await res.json()
    comentariosVuelos.value = { ...comentariosVuelos.value, [key]: Array.isArray(data) ? data : [] }
  } catch {
    comentariosVuelos.value = { ...comentariosVuelos.value, [key]: [] }
  } finally {
    const s = new Set(comentariosLoadingSet.value)
    s.delete(key)
    comentariosLoadingSet.value = s
  }
}

function initVueloObserver() {
  if (vueloObserver) vueloObserver.disconnect()
  vueloObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue
      const key = entry.target.dataset.vueloKey
      if (!key || key === 'null' || yaObservadoRV.value.has(key)) continue
      const [pid, rid] = key.split('::')
      cargarComentariosVuelo(Number(pid), Number(rid), key)
      vueloObserver.unobserve(entry.target)
    }
  }, { rootMargin: '200px 0px' })
}

async function observarVuelos() {
  await nextTick()
  if (!vueloObserver) return
  document.querySelectorAll('[data-vuelo-key]').forEach(el => {
    const key = el.dataset.vueloKey
    if (key && key !== 'null' && !yaObservadoRV.value.has(key)) {
      vueloObserver.observe(el)
    }
  })
}

// WATCH: debe ir DESPUÉS de que vuelosFiltrados esté definido
watch([vuelosFiltrados, paso], () => observarVuelos(), { flush: 'post' })

// ── Form modificar ────────────────────────────────────────────
const form = reactive({
  origenPaisQ: '', origenPaisSug: [], origenPaisSel: null,
  origenCiudadQ: '', origenCiudadSug: [], origenCiudadLoading: false,
  origenCiudades: [], origenPais: '', origenCiudad: '',
  destinoPaisQ: '', destinoPaisSug: [], destinoPaisSel: null,
  destinoCiudadQ: '', destinoCiudadSug: [], destinoCiudadLoading: false,
  destinoCiudades: [], destinoPais: '', destinoCiudad: '',
  fecha: '', fechaRegreso: '', cantidadPasajeros: 1,
  tipoVuelo: 'ida',
})

const minFechaRegresoForm = computed(() => {
  if (!form.fecha) return hoy
  const d = new Date(form.fecha); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

function toggleModificar() {
  modificarAbierto.value = !modificarAbierto.value
  if (modificarAbierto.value) {
    form.origenPaisQ = ''; form.origenPaisSug = []; form.origenPaisSel = null
    form.origenCiudadQ = ''; form.origenCiudadSug = []; form.origenCiudades = []
    form.origenPais = ''; form.origenCiudad = ''
    form.destinoPaisQ = ''; form.destinoPaisSug = []; form.destinoPaisSel = null
    form.destinoCiudadQ = ''; form.destinoCiudadSug = []; form.destinoCiudades = []
    form.destinoPais = ''; form.destinoCiudad = ''
    form.fecha = ''; form.cantidadPasajeros = 1
    modError.value = ''
  }
}

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

async function onFormOPaisInput() {
  form.origenPaisSel = null; form.origenCiudadQ = ''; form.origenCiudades = []; form.origenPais = ''; form.origenCiudad = ''
  const q = form.origenPaisQ.trim(); if (q.length < 2) { form.origenPaisSug = []; return }
  form.origenPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selFormOPais(p) {
  form.origenPaisSel = p; form.origenPaisQ = p.country; form.origenPaisSug = []
  form.origenPais = p.country; form.origenCiudadLoading = true
  form.origenCiudades = await getCiudades(p.country); form.origenCiudadLoading = false
}
function onFormOCiudadInput() {
  const q = form.origenCiudadQ.toLowerCase()
  form.origenCiudadSug = q.length < 2 ? [] : form.origenCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  form.origenCiudad = ''
}
function selFormOCiudad(c) { form.origenCiudadQ = c; form.origenCiudadSug = []; form.origenCiudad = c; modError.value = '' }

async function onFormDPaisInput() {
  form.destinoPaisSel = null; form.destinoCiudadQ = ''; form.destinoCiudades = []; form.destinoPais = ''; form.destinoCiudad = ''
  const q = form.destinoPaisQ.trim(); if (q.length < 2) { form.destinoPaisSug = []; return }
  form.destinoPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selFormDPais(p) {
  form.destinoPaisSel = p; form.destinoPaisQ = p.country; form.destinoPaisSug = []
  form.destinoPais = p.country; form.destinoCiudadLoading = true
  form.destinoCiudades = await getCiudades(p.country); form.destinoCiudadLoading = false
}
function onFormDCiudadInput() {
  const q = form.destinoCiudadQ.toLowerCase()
  form.destinoCiudadSug = q.length < 2 ? [] : form.destinoCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  form.destinoCiudad = ''
}
function selFormDCiudad(c) { form.destinoCiudadQ = c; form.destinoCiudadSug = []; form.destinoCiudad = c; modError.value = '' }

// ── Rebuscar ──────────────────────────────────────────────────
function tieneVuelos(respuesta) {
  if (!Array.isArray(respuesta) || respuesta.length === 0) return false
  return respuesta.some(b => b.datos && ((b.datos.directos?.length > 0) || (b.datos.conEscala?.length > 0)))
}

async function rebuscar() {
  modError.value = ''
  const o  = form.origenCiudad  || form.origenCiudadQ.trim()
  const op = form.origenPais    || form.origenPaisQ.trim()
  const d  = form.destinoCiudad || form.destinoCiudadQ.trim()
  const dp = form.destinoPais   || form.destinoPaisQ.trim()
  if (!op || !o)        { modError.value = 'Selecciona origen.'; return }
  if (!dp || !d)        { modError.value = 'Selecciona destino.'; return }
  if (!form.fecha)      { modError.value = 'Selecciona la fecha de ida.'; return }
  if (form.fecha < hoy) { modError.value = 'La fecha de ida no puede ser en el pasado.'; return }
  if (form.tipoVuelo === 'idaVuelta') {
    if (!form.fechaRegreso) { modError.value = 'Selecciona la fecha de regreso.'; return }
    if (form.fechaRegreso <= form.fecha) { modError.value = 'La fecha de regreso debe ser posterior a la de ida.'; return }
  }

  buscando.value = true
  try {
    const bodyIda = { origen: o, origenPais: op, destino: d, destinoPais: dp, fecha: form.fecha, cantidadPasajeros: form.cantidadPasajeros }

    if (form.tipoVuelo === 'idaVuelta') {
      const bodyRegreso = { origen: d, origenPais: dp, destino: o, destinoPais: op, fecha: form.fechaRegreso, cantidadPasajeros: form.cantidadPasajeros }
      const [resIda, resRegreso] = await Promise.all([
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) }),
        fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyRegreso) }),
      ])
      if (!resIda.ok) throw new Error()
      const rawIda     = await resIda.json()
      const rawRegreso = resRegreso.ok ? await resRegreso.json() : []
      if (!tieneVuelos(rawIda)) { modError.value = `No hay vuelos de ${o} a ${d} para el ${form.fecha}.`; return }
      if (!tieneVuelos(rawRegreso)) { modError.value = `No hay vuelos de regreso de ${d} a ${o} para el ${form.fechaRegreso}. Prueba otra fecha.`; return }
      busqueda.value = { origen: o, origenPais: op, destino: d, destinoPais: dp, fecha: form.fecha, fechaRegreso: form.fechaRegreso, cantidadPasajeros: form.cantidadPasajeros, tipoVuelo: 'idaVuelta' }
      erroresProveedores.value = []
      vuelos.value        = mapearRespuesta(rawIda)
      vuelosRegreso.value = mapearRespuesta(rawRegreso)
    } else {
      const res = await fetch(`${API}/api/busqueda/vuelos`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bodyIda) })
      if (!res.ok) throw new Error()
      busqueda.value = { origen: o, origenPais: op, destino: d, destinoPais: dp, fecha: form.fecha, cantidadPasajeros: form.cantidadPasajeros, tipoVuelo: 'ida' }
      erroresProveedores.value = []
      vuelos.value        = mapearRespuesta(await res.json())
      vuelosRegreso.value = []
    }

    // Limpiar comentarios de búsqueda anterior
    comentariosVuelos.value     = {}
    comentariosLoadingSet.value = new Set()
    yaObservadoRV.value         = new Set()
    estadoNodosRV.value         = {}

    error.value = ''
    resetFiltros()
    seleccionado.value        = null
    seleccionadoRegreso.value = null
    paso.value                = 1
    modificarAbierto.value    = false
  } catch { modError.value = 'No se pudieron obtener vuelos. Intenta de nuevo.' }
  finally { buscando.value = false }
}

// ── Helpers ───────────────────────────────────────────────────
function formatHora(h) { return h ? String(h).substring(0, 5) : '--' }
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}
function formatFechaCorta(f) {
  if (!f) return ''
  try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}
function formatDuracion(min) {
  if (!min || min === 9999) return '--'
  const h = Math.floor(min / 60), m = min % 60
  return `${h}h${m > 0 ? ` ${m}m` : ''}`
}

// ── Mapeo respuesta API ───────────────────────────────────────
function mapearRespuesta(respuesta) {
  const res = []
  for (const b of respuesta) {
    if (b.error) { erroresProveedores.value.push(b); continue }
    if (!b.datos) continue
    for (const v of (b.datos.directos  || [])) res.push(mapDirecto(v, b))
    for (const v of (b.datos.conEscala || [])) res.push(mapEscala(v, b))
  }
  return res
}

function mapDirecto(v, b) {
  return {
    id:       `${b.proveedor_id}-d-${v.id ?? Math.random()}`,
    rutaId:   v.id ?? null,                    // ID real de la ruta en Broom para comentarios
    aerolinea: b.proveedor, numeroVuelo: v.numeroVuelo || '--',
    origenCodigo: v.origenCodigo || '', origenCiudad: v.origenCiudad || '',
    destinoCodigo: v.destinoCodigo || '', destinoCiudad: v.destinoCiudad || '',
    horaSalida: formatHora(v.horaSalida), horaLlegada: formatHora(v.horaLlegada),
    duracionMinutos: v.duracionMinutos || 0,
    precioTurista: v.precioTurista ?? 0, precioEjecutiva: v.precioEjecutiva ?? 0,
    asientosTurista:   typeof v.boletosDisponiblesTurista   === 'number' ? v.boletosDisponiblesTurista   : 99,
    asientosEjecutiva: typeof v.boletosDisponiblesEjecutiva === 'number' ? v.boletosDisponiblesEjecutiva : 99,
    claseSeleccionada: 'economica', avionMarca: v.avionMarca || '', avionModelo: v.avionModelo || '',
    escalas: 0, paradas: [], tramos: [], tiempoEscalaMinutos: 0,
  }
}

function mapEscala(v, b) {
  const tramos = Array.isArray(v.tramos) ? v.tramos : []
  const p = tramos[0] || {}, u = tramos[tramos.length - 1] || {}
  return {
    id:     `${b.proveedor_id}-e-${v.id ?? v.precioTuristaTotal ?? Math.random()}`,
    rutaId: v.id ?? null,                       // ID real si Broom lo devuelve
    aerolinea: b.proveedor, numeroVuelo: p.numeroVuelo || '--',
    origenCodigo: p.origenCodigo || '', origenCiudad: p.origenCiudad || '',
    destinoCodigo: u.destinoCodigo || '', destinoCiudad: u.destinaCiudad || u.destinoCiudad || '',
    horaSalida: formatHora(p.horaSalida), horaLlegada: formatHora(u.horaLlegada),
    duracionMinutos: v.duracionTotalMinutos || 0,
    precioTurista: v.precioTuristaTotal ?? 0, precioEjecutiva: v.precioEjecutivaTotal ?? 0,
    asientosTurista:   typeof v.boletosDisponiblesTurista   === 'number' ? v.boletosDisponiblesTurista   : 99,
    asientosEjecutiva: typeof v.boletosDisponiblesEjecutiva === 'number' ? v.boletosDisponiblesEjecutiva : 99,
    claseSeleccionada: 'economica', avionMarca: p.avionMarca || '', avionModelo: p.avionModelo || '',
    escalas: v.numeroEscalas ?? (tramos.length > 1 ? tramos.length - 1 : 1),
    paradas: tramos.slice(0, -1).map(t => ({ codigo: t.destinoCodigo || '???', ciudad: t.destinaCiudad || t.destinoCiudad || '' })),
    tramos, tiempoEscalaMinutos: v.tiempoEscalaMinutos || 0,
  }
}

// ── Reservar ──────────────────────────────────────────────────
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

async function precrearReservacion(itemData) {
  try {
    const res1 = await fetch(`${API}/api/reservaciones`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: 1 }),
    })
    if (!res1.ok) return null
    const reserva = await res1.json()

    let vuelosArr = [], proveedorId = null
    if (itemData.tipoVuelo === 'idaVuelta') {
      const ida = itemData.ida, regreso = itemData.regreso, pax = itemData.busqueda?.cantidadPasajeros || 1
      proveedorId = parseProveedorId(ida.id)
      vuelosArr = [
        { vueloId: parseVueloId(ida.id),     claseId: claseToId(ida.clase),     cantidadPasajeros: pax },
        { vueloId: parseVueloId(regreso.id), claseId: claseToId(regreso.clase), cantidadPasajeros: pax },
      ]
    } else {
      const pax = itemData.busqueda?.cantidadPasajeros || 1
      proveedorId = parseProveedorId(itemData.id)
      vuelosArr = [{ vueloId: parseVueloId(itemData.id), claseId: claseToId(itemData.clase), cantidadPasajeros: pax }]
    }

    const res2 = await fetch(`${API}/api/reservaciones/detalle/vuelo`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reservacion_id: reserva.id, proveedor_id: proveedorId, vuelos: vuelosArr }),
    })
    let detalle = null
    if (res2.ok) detalle = await res2.json()

    let expiresAt = 0
    if (detalle?.detalle?.fechaExpiracion) expiresAt = new Date(detalle.detalle.fechaExpiracion).getTime()
    else if (reserva.fecha_expiracion) expiresAt = new Date(reserva.fecha_expiracion.replace(' ', 'T')).getTime()
    if (!expiresAt || expiresAt <= Date.now()) expiresAt = Date.now() + 600_000
    const segundos = Math.max(30, Math.floor((expiresAt - Date.now()) / 1000))
    return { reserva, detalle, segundos, expiresAt }
  } catch { return null }
}

function seleccionarVuelo(vuelo) {
  if (esIdaVuelta.value && paso.value === 1) {
    seleccionado.value = vuelo.id
    sessionStorage.setItem('_vuelo_ida_temp', JSON.stringify({
      ...vuelo, clase: vuelo.claseSeleccionada,
      precio:   vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva   : vuelo.precioTurista,
      asientos: vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva : vuelo.asientosTurista,
    }))
    paso.value = 2
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }

  if (esIdaVuelta.value && paso.value === 2) {
    seleccionadoRegreso.value = vuelo.id
    const vueloIda = JSON.parse(sessionStorage.getItem('_vuelo_ida_temp') || '{}')
    sessionStorage.removeItem('_vuelo_ida_temp')
    sessionStorage.removeItem('hotel_seleccionado')
    sessionStorage.removeItem('paquete_seleccionado')
    const itemData = {
      tipoVuelo: 'idaVuelta',
      ida:     { ...vueloIda, busqueda: busqueda.value },
      regreso: { ...vuelo, clase: vuelo.claseSeleccionada,
        precio:   vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva   : vuelo.precioTurista,
        asientos: vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva : vuelo.asientosTurista },
      busqueda: busqueda.value,
    }
    sessionStorage.setItem('vuelo_seleccionado', JSON.stringify(itemData))
    window.__reservaPromise = precrearReservacion(itemData)
    router.push('/reservar')
    return
  }

  seleccionado.value = vuelo.id
  sessionStorage.removeItem('hotel_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')
  const itemData = {
    ...vuelo, tipoVuelo: 'ida',
    clase:    vuelo.claseSeleccionada,
    precio:   vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.precioEjecutiva   : vuelo.precioTurista,
    asientos: vuelo.claseSeleccionada === 'ejecutiva' ? vuelo.asientosEjecutiva : vuelo.asientosTurista,
    busqueda: busqueda.value,
  }
  sessionStorage.setItem('vuelo_seleccionado', JSON.stringify(itemData))
  window.__reservaPromise = precrearReservacion(itemData)
  router.push('/reservar')
}

// ── onMounted ─────────────────────────────────────────────────
onMounted(() => {
  if (!busqueda.value.origen || !busqueda.value.destino) {
    error.value = 'Faltan datos de búsqueda.'; loading.value = false; return
  }
  if (resultadosRaw && Array.isArray(resultadosRaw) && resultadosRaw.length > 0) {
    vuelos.value = mapearRespuesta(resultadosRaw)
    if (resultadosRegresoRaw && Array.isArray(resultadosRegresoRaw)) {
      vuelosRegreso.value = mapearRespuesta(resultadosRegresoRaw)
    }
    loading.value = false
    initVueloObserver()
    observarVuelos()
    return
  }
  error.value = 'No hay resultados. Modifica la búsqueda.'; loading.value = false
})
</script>