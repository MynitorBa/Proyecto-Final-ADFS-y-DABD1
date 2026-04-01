<template>
  <div class="page">
    <Encabezado />

    <div class="rh-page">
      <div class="rh-layout">

        <!-- ═══ SIDEBAR FILTROS ═══ -->
        <aside class="rh-sidebar" :class="{ 'rh-sidebar--open': filtrosAbiertos }">
          <div class="rh-sidebar__head">
            <h3 class="rh-sidebar__title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/>
                <line x1="11" y1="18" x2="13" y2="18"/>
              </svg>
              Filtros
              <span v-if="cantFiltrosActivos > 0" class="rh-sidebar__badge">{{ cantFiltrosActivos }}</span>
            </h3>
            <button class="rh-sidebar__close" @click="filtrosAbiertos = false" type="button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="18" height="18">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <div class="rh-sidebar__body">
            <div class="rh-filter-group">
              <h4 class="rh-filter-group__title">Precio por noche</h4>
              <div class="rh-price-inputs">
                <div class="rh-price-input">
                  <span>$</span>
                  <input type="number" v-model.number="filtros.precioMin" :min="0" placeholder="0" />
                </div>
                <span class="rh-price-sep">—</span>
                <div class="rh-price-input">
                  <span>$</span>
                  <input type="number" v-model.number="filtros.precioMax" placeholder="9999" />
                </div>
              </div>
            </div>

            <div class="rh-filter-group" v-if="tiposDisponibles.length">
              <h4 class="rh-filter-group__title">Tipo de habitación</h4>
              <div class="rh-checkboxes">
                <label class="rh-checkbox" v-for="t in tiposDisponibles" :key="t">
                  <input type="checkbox" v-model="filtros.tipos" :value="t" />
                  <span class="rh-checkbox__box"></span>
                  <span class="rh-checkbox__label">{{ t }}</span>
                </label>
              </div>
            </div>

            <div class="rh-filter-group" v-if="hotelesDisponibles.length">
              <h4 class="rh-filter-group__title">Hotel</h4>
              <div class="rh-checkboxes">
                <label class="rh-checkbox" v-for="h in hotelesDisponibles" :key="h">
                  <input type="checkbox" v-model="filtros.hoteles" :value="h" />
                  <span class="rh-checkbox__box"></span>
                  <span class="rh-checkbox__label">{{ h }}</span>
                </label>
              </div>
            </div>

            <div class="rh-filter-group" v-if="proveedoresDisponibles.length > 1">
              <h4 class="rh-filter-group__title">Proveedor</h4>
              <div class="rh-checkboxes">
                <label class="rh-checkbox" v-for="p in proveedoresDisponibles" :key="p">
                  <input type="checkbox" v-model="filtros.proveedores" :value="p" />
                  <span class="rh-checkbox__box"></span>
                  <span class="rh-checkbox__label">{{ p }}</span>
                </label>
              </div>
            </div>

            <div class="rh-filter-group" v-if="amenidadesDisponibles.length">
              <h4 class="rh-filter-group__title">Amenidades</h4>
              <div class="rh-checkboxes">
                <label class="rh-checkbox" v-for="a in amenidadesDisponibles" :key="a">
                  <input type="checkbox" v-model="filtros.amenidades" :value="a" />
                  <span class="rh-checkbox__box"></span>
                  <span class="rh-checkbox__label">{{ a }}</span>
                </label>
              </div>
            </div>

            <button v-if="hayFiltrosActivos" class="rh-btn rh-btn--ghost rh-sidebar__reset" @click="resetFiltros" type="button">
              Limpiar filtros
            </button>
          </div>
        </aside>

        <div v-if="filtrosAbiertos" class="rh-sidebar-overlay" @click="filtrosAbiertos = false"></div>

        <!-- ═══ CONTENIDO PRINCIPAL ═══ -->
        <div class="rh-main">

          <!-- ── SEARCH BAR con Modificar inline ── -->
          <div class="rh-search-bar" :class="{ 'rh-search-bar--open': modificarAbierto }">
            <div class="rh-search-bar__summary" @click="toggleModificar">
              <div class="rh-search-bar__destino">
                <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3" fill="#1C1A18"/>
                </svg>
                <span class="rh-search-bar__ciudad">{{ busqueda.ciudad }}, {{ busqueda.pais }}</span>
              </div>
              <div class="rh-search-bar__detalles">
                <span>Check-in {{ formatFecha(busqueda.checkIn) }}</span>
                <span>· Check-out {{ formatFecha(busqueda.checkOut) }}</span>
                <span>· {{ noches }} noches</span>
                <span>· {{ busqueda.cantidadPersonas || 1 }} persona{{ (busqueda.cantidadPersonas || 1) !== 1 ? 's' : '' }}</span>
              </div>
            </div>
            <div class="rh-search-bar__actions">
              <button class="rh-search-bar__mod-btn" @click="toggleModificar" type="button">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"
                  :style="{ transform: modificarAbierto ? 'rotate(180deg)' : 'none', transition: 'transform .2s' }">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
                {{ modificarAbierto ? 'Cerrar' : 'Modificar' }}
              </button>
            </div>
          </div>

          <!-- ── FORM MODIFICAR INLINE ── -->
          <transition name="rh-expand">
            <div v-if="modificarAbierto" class="rh-modificar-inline">
              <div class="rh-modificar-grid">

                <div class="rh-mod-section">
                  <p class="rh-mod-section__label">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                      <polyline points="9 22 9 12 15 12 15 22"/>
                    </svg>
                    Destino
                  </p>
                  <div class="rh-mod-row">
                    <div class="rh-mod-field rh-ac-wrap">
                      <label class="rh-mod-label">País</label>
                      <input class="rh-mod-input" type="text" v-model="form.dPaisQ"
                        @input="onFormDPaisInput" @blur="blurClose(() => form.dPaisSug = [])"
                        placeholder="Guatemala..." autocomplete="off" />
                      <ul v-if="form.dPaisSug.length" class="rh-ac-list">
                        <li v-for="p in form.dPaisSug" :key="p.country">
                          <button type="button" @click="selFormDPais(p)">{{ p.country }}</button>
                        </li>
                      </ul>
                    </div>
                    <div class="rh-mod-field rh-ac-wrap">
                      <label class="rh-mod-label">
                        Ciudad
                        <span v-if="form.dCiudadLoading" class="rh-mod-hint">cargando...</span>
                      </label>
                      <input class="rh-mod-input" type="text" v-model="form.dCiudadQ"
                        @input="onFormDCiudadInput" @blur="blurClose(() => form.dCiudadSug = [])"
                        :disabled="!form.dPaisSel || form.dCiudadLoading"
                        placeholder="Guatemala City..." autocomplete="off" />
                      <ul v-if="form.dCiudadSug.length" class="rh-ac-list">
                        <li v-for="c in form.dCiudadSug" :key="c">
                          <button type="button" @click="selFormDCiudad(c)">{{ c }}</button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>

                <div class="rh-mod-section rh-mod-section--row">
                  <div class="rh-mod-field">
                    <label class="rh-mod-label">Check-in</label>
                    <input class="rh-mod-input" type="date" v-model="form.checkIn" :min="hoy" />
                  </div>
                  <div class="rh-mod-field">
                    <label class="rh-mod-label">Check-out</label>
                    <input class="rh-mod-input" type="date" v-model="form.checkOut" :min="minCheckOutForm" />
                  </div>
                  <div class="rh-mod-field">
                    <label class="rh-mod-label">Personas</label>
                    <select class="rh-mod-input" v-model="form.cantidadPersonas">
                      <option v-for="n in 10" :key="n" :value="n">{{ n }} {{ n === 1 ? 'Persona' : 'Personas' }}</option>
                    </select>
                  </div>
                  <div class="rh-mod-field rh-mod-field--cta">
                    <p v-if="modError" class="rh-mod-error">⚠ {{ modError }}</p>
                    <button class="rh-mod-buscar" @click="rebuscar" :disabled="buscandoMod" type="button">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
                        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                      </svg>
                      {{ buscandoMod ? 'Buscando...' : 'Buscar hoteles' }}
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </transition>

          <!-- Chips filtros activos -->
          <div v-if="hayFiltrosActivos" class="rh-chips-activos">
            <button v-if="filtros.precioMin > 0 || filtros.precioMax < 9999"
              class="rh-chip" @click="filtros.precioMin = 0; filtros.precioMax = 9999" type="button">
              ${{ filtros.precioMin }}–${{ filtros.precioMax }}/noche ✕
            </button>
            <button v-for="t in filtros.tipos" :key="'t'+t"
              class="rh-chip" @click="filtros.tipos = filtros.tipos.filter(x => x !== t)" type="button">
              {{ t }} ✕
            </button>
            <button v-for="h in filtros.hoteles" :key="'h'+h"
              class="rh-chip" @click="filtros.hoteles = filtros.hoteles.filter(x => x !== h)" type="button">
              {{ h }} ✕
            </button>
            <button v-for="p in filtros.proveedores" :key="'p'+p"
              class="rh-chip" @click="filtros.proveedores = filtros.proveedores.filter(x => x !== p)" type="button">
              {{ p }} ✕
            </button>
            <button v-for="a in filtros.amenidades" :key="'a'+a"
              class="rh-chip" @click="filtros.amenidades = filtros.amenidades.filter(x => x !== a)" type="button">
              {{ a }} ✕
            </button>
            <button class="rh-chip rh-chip--clear" @click="resetFiltros" type="button">Limpiar todo</button>
          </div>

          <!-- Toolbar -->
          <div class="rh-toolbar">
            <p class="rh-toolbar__count">
              <strong>{{ gruposPorHotel.length }}</strong>
              hotel{{ gruposPorHotel.length !== 1 ? 'es' : '' }} disponible{{ gruposPorHotel.length !== 1 ? 's' : '' }}
              <span v-if="hayFiltrosActivos" class="rh-toolbar__filtered"> · {{ todasLasHabitaciones.length }} habitaciones total</span>
            </p>
            <div class="rh-sort">
              <label class="rh-sort__label">Ordenar:</label>
              <select v-model="ordenar" class="rh-sort__select">
                <option value="precio-asc">Precio: menor a mayor</option>
                <option value="precio-desc">Precio: mayor a menor</option>
                <option value="capacidad">Mayor capacidad</option>
                <option value="disponibles">Más disponibles</option>
              </select>
            </div>
          </div>

          <!-- Aviso proveedores con error -->
          <div v-if="erroresProveedores.length > 0" class="rh-warn">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13.5"/><circle cx="12" cy="17" r="1" fill="currentColor" stroke="none"/>
            </svg>
            {{ erroresProveedores.length }} proveedor{{ erroresProveedores.length !== 1 ? 'es' : '' }} sin respuesta. Los resultados pueden estar incompletos.
          </div>

          <!-- Loading -->
          <div v-if="loading" class="rh-empty">
            <div class="rh-spinner"></div>
            <p>Buscando hospedajes disponibles...</p>
          </div>

          <!-- Error total -->
          <div v-else-if="error && todasLasHabitaciones.length === 0" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="44" height="44">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13.5" stroke-linecap="round"/>
              <circle cx="12" cy="17" r="1" fill="#D40511" stroke="none"/>
            </svg>
            <p class="rh-empty__title">{{ error }}</p>
            <button class="rh-btn rh-btn--yellow" @click="toggleModificar" type="button">Modificar búsqueda</button>
          </div>

          <!-- Sin resultados con filtros -->
          <div v-else-if="!loading && todasLasHabitaciones.length > 0 && habitacionesFiltradas.length === 0" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="52" height="52">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            <p class="rh-empty__title">Ninguna habitación coincide con los filtros</p>
            <p class="rh-empty__sub">{{ todasLasHabitaciones.length }} disponible{{ todasLasHabitaciones.length !== 1 ? 's' : '' }} sin filtros.</p>
            <button class="rh-btn rh-btn--ghost" @click="resetFiltros" type="button">Quitar filtros</button>
          </div>

          <!-- Sin hoteles para N personas -->
          <div v-else-if="!loading && todasLasHabitaciones.length > 0 && gruposPorHotel.length === 0" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="52" height="52">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            <p class="rh-empty__title">Sin hospedajes para {{ busqueda.cantidadPersonas }} personas</p>
            <p class="rh-empty__sub">Ningún hotel disponible puede acomodar a {{ busqueda.cantidadPersonas }} personas en <strong>{{ busqueda.ciudad }}</strong> para esas fechas.</p>
            <button class="rh-btn rh-btn--yellow" @click="toggleModificar" type="button">Cambiar búsqueda</button>
          </div>

          <!-- Sin resultados sin filtros -->
          <div v-else-if="!loading && todasLasHabitaciones.length === 0 && !error" class="rh-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1" width="52" height="52">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            <p class="rh-empty__title">Sin hospedajes disponibles</p>
            <p class="rh-empty__sub">No hay habitaciones en <strong>{{ busqueda.ciudad }}</strong> para las fechas seleccionadas.</p>
            <button class="rh-btn rh-btn--yellow" @click="toggleModificar" type="button">Buscar otra fecha</button>
          </div>

          <!-- Lista agrupada por hotel -->
          <template v-if="!loading && gruposPorHotel.length > 0">
            <div v-for="grupo in gruposPorHotel" :key="`${grupo.proveedorId}-${grupo.hotelId}`" class="rh-grupo">

              <!-- Header hotel -->
              <div class="rh-grupo__head">
                <div class="rh-grupo__hotel-info">
                  <div class="rh-grupo__hotel-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22">
                      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                    </svg>
                  </div>
                  <div>
                    <div class="rh-grupo__header-row">
                      <h3 class="rh-grupo__nombre">{{ grupo.nombreHotel }}</h3>
                      <div v-if="grupo.rating" class="rh-grupo__rating">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13">
                          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                        </svg>
                        {{ grupo.rating.toFixed(1) }}
                      </div>
                    </div>
                    <p class="rh-grupo__ubicacion">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
                      </svg>
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

              <!-- Amenidades del hotel -->
              <div v-if="grupo.amenidades.length" class="rh-grupo__amenidades">
                <span v-for="am in grupo.amenidades" :key="am.amenidadId" class="rh-amenidad-hotel" :title="am.descripcion">
                  {{ am.nombre }}
                </span>
              </div>

              <!-- ══ PANEL COMBOS DEL HOTEL ══ -->
              <template v-if="getHotelCombos(grupo)">
                <div class="rh-hotel-combos">

                  <!-- Combinación exacta -->
                  <div v-if="getHotelCombos(grupo).combo" class="rh-combo-panel rh-combo-panel--exact">
                    <div class="rh-combo-panel__label">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                      </svg>
                      Combinación para {{ busqueda.cantidadPersonas }} personas
                    </div>
                    <div v-for="(item, i) in getHotelCombos(grupo).combo.habs" :key="i" class="rh-combo-hab-block">
                      <div class="rh-combo-row">
                        <span class="rh-combo-tipo">
                          Hab.{{ i+1 }} · <strong>{{ item.tipo }}</strong>
                          <span class="rh-combo-cap">({{ item.cap }} pers.)</span>
                        </span>
                        <span class="rh-combo-precio">{{ fmt(item.precio) }}<small>/noche</small></span>
                      </div>
                      <div class="rh-combo-detail">
                        <span v-if="item.tipoCama">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                          {{ item.tipoCama }}
                        </span>
                        <span v-if="item.metrosCuadrados">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
                          {{ item.metrosCuadrados }} m²
                        </span>
                        <span v-if="item.cantidadDisponible > 0" :class="{ 'rh-combo-urgente': item.cantidadDisponible <= 2 }">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                          {{ item.cantidadDisponible <= 2 ? `¡Solo ${item.cantidadDisponible} disponible!` : `${item.cantidadDisponible} disponibles` }}
                        </span>
                        <span v-if="item.habitacionesDisponibles?.length">
                          Hab:
                          <span v-for="h in item.habitacionesDisponibles.slice(0,3)" :key="h.id" class="rh-combo-room-badge">{{ h.numeroHabitacion }}</span>
                          <span v-if="item.habitacionesDisponibles.length > 3" class="rh-combo-room-badge">+{{ item.habitacionesDisponibles.length - 3 }}</span>
                        </span>
                      </div>
                    </div>
                    <div class="rh-combo-total">
                      Total: <strong>{{ fmt(getHotelCombos(grupo).combo.total) }}/noche</strong>
                      · {{ fmt(getHotelCombos(grupo).combo.total * noches) }} por {{ noches }} noche{{ noches !== 1 ? 's' : '' }}
                    </div>
                    <div v-if="grupo.amenidades.length" class="rh-combo-amenidades">
                      <span v-for="am in grupo.amenidades.slice(0,5)" :key="am.amenidadId" class="rh-combo-amenidad-chip" :title="am.descripcion">{{ am.nombre }}</span>
                      <span v-if="grupo.amenidades.length > 5" class="rh-combo-amenidad-chip rh-combo-amenidad-chip--mas">+{{ grupo.amenidades.length - 5 }}</span>
                    </div>
                    <button class="rh-btn rh-btn--yellow rh-combo-panel__cta"
                      @click="reservarCombo(getHotelCombos(grupo).combo, grupo)" type="button">
                      Reservar combinación
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>

                  <!-- Combinación aproximada -->
                  <div v-if="getHotelCombos(grupo).aprox" class="rh-combo-panel rh-combo-panel--aprox">
                    <div class="rh-combo-panel__label">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="12" height="12">
                        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                      </svg>
                      Opción cercana — {{ getHotelCombos(grupo).aprox.capacidadTotal }} de {{ busqueda.cantidadPersonas }} pers.
                    </div>
                    <div v-for="(item, i) in getHotelCombos(grupo).aprox.habs" :key="i" class="rh-combo-hab-block">
                      <div class="rh-combo-row">
                        <span class="rh-combo-tipo">
                          Hab.{{ i+1 }} · <strong>{{ item.tipo }}</strong>
                          <span class="rh-combo-cap">({{ item.cap }} pers.)</span>
                        </span>
                        <span class="rh-combo-precio">{{ fmt(item.precio) }}<small>/noche</small></span>
                      </div>
                      <div class="rh-combo-detail">
                        <span v-if="item.tipoCama">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                          {{ item.tipoCama }}
                        </span>
                        <span v-if="item.metrosCuadrados">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
                          {{ item.metrosCuadrados }} m²
                        </span>
                        <span v-if="item.cantidadDisponible > 0" :class="{ 'rh-combo-urgente': item.cantidadDisponible <= 2 }">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                          {{ item.cantidadDisponible <= 2 ? `¡Solo ${item.cantidadDisponible} disponible!` : `${item.cantidadDisponible} disponibles` }}
                        </span>
                        <span v-if="item.habitacionesDisponibles?.length">
                          Hab:
                          <span v-for="h in item.habitacionesDisponibles.slice(0,3)" :key="h.id" class="rh-combo-room-badge">{{ h.numeroHabitacion }}</span>
                          <span v-if="item.habitacionesDisponibles.length > 3" class="rh-combo-room-badge">+{{ item.habitacionesDisponibles.length - 3 }}</span>
                        </span>
                      </div>
                    </div>
                    <div class="rh-combo-total">
                      Total: <strong>{{ fmt(getHotelCombos(grupo).aprox.total) }}/noche</strong>
                      · {{ fmt(getHotelCombos(grupo).aprox.total * noches) }} por {{ noches }} noche{{ noches !== 1 ? 's' : '' }}
                    </div>
                    <div v-if="grupo.amenidades.length" class="rh-combo-amenidades">
                      <span v-for="am in grupo.amenidades.slice(0,5)" :key="am.amenidadId" class="rh-combo-amenidad-chip" :title="am.descripcion">{{ am.nombre }}</span>
                      <span v-if="grupo.amenidades.length > 5" class="rh-combo-amenidad-chip rh-combo-amenidad-chip--mas">+{{ grupo.amenidades.length - 5 }}</span>
                    </div>
                    <button class="rh-btn rh-btn--yellow rh-combo-panel__cta"
                      @click="reservarCombo(getHotelCombos(grupo).aprox, grupo)" type="button">
                      Reservar combinación
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>

                  <!-- Habitación + persona extra -->
                  <div v-if="getHotelCombos(grupo).extra" class="rh-combo-panel rh-combo-panel--extra">
                    <div class="rh-combo-panel__label">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/>
                        <line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/>
                      </svg>
                      Habitación + 1 persona extra
                    </div>
                    <div class="rh-combo-row">
                      <span class="rh-combo-tipo">{{ getHotelCombos(grupo).extra.tipo }} <span class="rh-combo-cap">(cap. {{ getHotelCombos(grupo).extra.cap }} +1 extra)</span></span>
                      <span class="rh-combo-precio">{{ fmt(getHotelCombos(grupo).extra.precioPorNoche) }}<small>/noche</small></span>
                    </div>
                    <div class="rh-combo-row">
                      <span class="rh-combo-tipo rh-combo-tipo--extra">+1 persona extra</span>
                      <span class="rh-combo-precio rh-combo-precio--extra">+{{ fmt(getHotelCombos(grupo).extra.precioPorPersona) }}<small>/noche</small></span>
                    </div>
                    <div class="rh-combo-total">
                      Total: <strong>{{ fmt(getHotelCombos(grupo).extra.total) }}/noche</strong>
                      · {{ fmt(getHotelCombos(grupo).extra.total * noches) }} por {{ noches }} noche{{ noches !== 1 ? 's' : '' }}
                    </div>
                    <div v-if="grupo.amenidades.length" class="rh-combo-amenidades">
                      <span v-for="am in grupo.amenidades.slice(0,5)" :key="am.amenidadId" class="rh-combo-amenidad-chip" :title="am.descripcion">{{ am.nombre }}</span>
                      <span v-if="grupo.amenidades.length > 5" class="rh-combo-amenidad-chip rh-combo-amenidad-chip--mas">+{{ grupo.amenidades.length - 5 }}</span>
                    </div>
                    <button class="rh-btn rh-btn--yellow rh-combo-panel__cta"
                      @click="reservarExtra(getHotelCombos(grupo).extra, grupo)" type="button">
                      Reservar con persona extra
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13"><polyline points="9 18 15 12 9 6"/></svg>
                    </button>
                  </div>

                </div>
              </template>

              <!-- Tarjetas individuales de habitaciones que caben el grupo completo -->
              <div class="rh-habitaciones"
                v-if="grupo.habitaciones.filter(h => h.capacidadMaxima >= busqueda.cantidadPersonas).length > 0">
                <article
                  v-for="hab in grupo.habitaciones.filter(h => h.capacidadMaxima >= busqueda.cantidadPersonas)"
                  :key="hab.uid"
                  class="rh-card" :class="{ 'rh-card--seleccionada': seleccionada === hab.uid }">

                  <div class="rh-card__img">
                    <div class="rh-card__img-placeholder">
                      <svg viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.3)" stroke-width="1" width="36" height="36">
                        <path d="M2 7a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2z"/>
                        <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
                      </svg>
                    </div>
                    <div class="rh-card__tipo-badge">{{ hab.tipoHabitacion }}</div>
                    <div v-if="hab.cantidadDisponible <= 2" class="rh-card__urgente-badge">
                      ¡Solo {{ hab.cantidadDisponible }}!
                    </div>
                  </div>

                  <div class="rh-card__info">
                    <div class="rh-card__info-top">
                      <div class="rh-card__info-left">
                        <h4 class="rh-card__nombre">{{ hab.tipoHabitacion }}</h4>
                        <p class="rh-card__cama">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                            <rect x="2" y="7" width="20" height="14" rx="2"/>
                            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/>
                          </svg>
                          {{ hab.tipoCama }}
                        </p>
                      </div>
                      <div class="rh-card__precio-bloque">
                        <span class="rh-card__precio-lbl">por noche</span>
                        <span class="rh-card__precio">${{ hab.precioPorNoche.toFixed(2) }}</span>
                        <span class="rh-card__precio-persona">
                          ${{ hab.precioPorPersona.toFixed(2) }}<small>/persona</small>
                        </span>
                        <span class="rh-card__precio-total">
                          {{ noches }} noches:
                          <strong>${{ (hab.precioPorNoche * noches).toFixed(2) }}</strong>
                        </span>
                      </div>
                    </div>

                    <div class="rh-card__meta">
                      <span>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
                        </svg>
                        Hasta {{ hab.capacidadMaxima }} persona{{ hab.capacidadMaxima !== 1 ? 's' : '' }}
                      </span>
                      <span v-if="hab.metrosCuadrados">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                          <rect x="3" y="3" width="18" height="18" rx="2"/>
                        </svg>
                        {{ hab.metrosCuadrados }} m²
                      </span>
                      <span class="rh-card__disponibles" :class="{ 'rh-card__disponibles--bajo': hab.cantidadDisponible <= 3 }">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13">
                          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                        </svg>
                        <template v-if="hab.cantidadDisponible === 0">Agotado</template>
                        <template v-else-if="hab.cantidadDisponible <= 3">
                          ¡Solo {{ hab.cantidadDisponible }} disponible{{ hab.cantidadDisponible !== 1 ? 's' : '' }}!
                        </template>
                        <template v-else>{{ hab.cantidadDisponible }} disponibles</template>
                      </span>
                    </div>

                    <div v-if="hab.habitacionesDisponibles.length" class="rh-card__numeros">
                      <span class="rh-card__numeros-label">Habitaciones:</span>
                      <span v-for="h in hab.habitacionesDisponibles.slice(0,6)" :key="h.id" class="rh-numero-badge">
                        {{ h.numeroHabitacion }}
                      </span>
                      <span v-if="hab.habitacionesDisponibles.length > 6" class="rh-numero-badge rh-numero-badge--mas">
                        +{{ hab.habitacionesDisponibles.length - 6 }}
                      </span>
                    </div>

                    <button class="rh-btn rh-btn--yellow rh-card__cta"
                      :disabled="hab.cantidadDisponible === 0"
                      @click="seleccionarHabitacion(hab, grupo)" type="button">
                      <template v-if="hab.cantidadDisponible === 0">Sin disponibilidad</template>
                      <template v-else>
                        Reservar habitación
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="13" height="13">
                          <polyline points="9 18 15 12 9 6"/>
                        </svg>
                      </template>
                    </button>
                  </div>

                </article>
              </div>

            </div>
          </template>

        </div>
      </div>
    </div>

    <Piepagina />
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/resultadoshoteles.css'

const router = useRouter()
const API    = 'http://localhost:8080'

// ── Estado inicial desde history.state ───────────────────────
const state         = history.state || {}
const resultadosRaw = state.resultados || null
const busqueda = ref({
  ciudad:           state.busqueda?.ciudad           || '',
  pais:             state.busqueda?.pais             || '',
  checkIn:          state.busqueda?.checkIn          || '',
  checkOut:         state.busqueda?.checkOut         || '',
  cantidadPersonas: state.busqueda?.cantidadPersonas || 1,
})

// ── UI ────────────────────────────────────────────────────────
const loading            = ref(true)
const error              = ref('')
const seleccionada       = ref(null)
const filtrosAbiertos    = ref(false)
const ordenar            = ref('precio-asc')
const erroresProveedores = ref([])
const modificarAbierto   = ref(false)
const buscandoMod        = ref(false)
const modError           = ref('')
const hoy                = new Date().toISOString().split('T')[0]

// ── Datos ─────────────────────────────────────────────────────
const todasLasHabitaciones = ref([])

// ── Filtros ───────────────────────────────────────────────────
const filtros = ref({
  precioMin: 0, precioMax: 9999,
  tipos: [], hoteles: [], proveedores: [],
  amenidades: [],
})

// ── Computed noches ───────────────────────────────────────────
const noches = computed(() => calcNoches(busqueda.value.checkIn, busqueda.value.checkOut))

// ── Form modificar ────────────────────────────────────────────
const form = reactive({
  dPaisQ: '', dPaisSug: [], dPaisSel: null,
  dCiudadQ: '', dCiudadSug: [], dCiudadLoading: false,
  dCiudades: [], pais: '', ciudad: '',
  checkIn: '', checkOut: '', cantidadPersonas: 1,
})

const minCheckOutForm = computed(() => {
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

async function onFormDPaisInput() {
  form.dPaisSel = null; form.dCiudadQ = ''; form.dCiudades = []; form.pais = ''; form.ciudad = ''
  const q = form.dPaisQ.trim(); if (q.length < 2) { form.dPaisSug = []; return }
  form.dPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}
async function selFormDPais(p) {
  form.dPaisSel = p; form.dPaisQ = p.country; form.dPaisSug = []; form.pais = p.country
  form.dCiudadLoading = true; form.dCiudades = await getCiudades(p.country); form.dCiudadLoading = false
}
function onFormDCiudadInput() {
  const q = form.dCiudadQ.toLowerCase()
  form.dCiudadSug = q.length < 2 ? [] : form.dCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  form.ciudad = ''
}
function selFormDCiudad(c) { form.dCiudadQ = c; form.dCiudadSug = []; form.ciudad = c; modError.value = '' }

function toggleModificar() {
  modificarAbierto.value = !modificarAbierto.value
  if (modificarAbierto.value) {
    Object.assign(form, { dPaisQ:'', dPaisSug:[], dPaisSel:null, dCiudadQ:'', dCiudadSug:[], dCiudades:[], pais:'', ciudad:'', checkIn:'', checkOut:'', cantidadPersonas:1 })
    modError.value = ''
  }
}

async function rebuscar() {
  modError.value = ''
  if (!form.pais || !form.ciudad)    { modError.value = 'Selecciona país y ciudad de destino.'; return }
  if (!form.checkIn)                 { modError.value = 'Selecciona la fecha de check-in.'; return }
  if (form.checkIn < hoy)            { modError.value = 'El check-in no puede ser una fecha pasada.'; return }
  if (!form.checkOut)                { modError.value = 'Selecciona la fecha de check-out.'; return }
  if (form.checkOut <= form.checkIn) { modError.value = 'El check-out debe ser posterior al check-in.'; return }
  if (form.cantidadPersonas < 1)     { modError.value = 'Debe haber al menos 1 persona.'; return }

  buscandoMod.value = true
  try {
    const res = await fetch(`${API}/api/busqueda/hoteles`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ciudad: form.ciudad, pais: form.pais, fechaCheckIn: form.checkIn, fechaCheckOut: form.checkOut, cantidadPersonas: form.cantidadPersonas })
    })
    if (!res.ok) throw new Error()
    busqueda.value = { ciudad: form.ciudad, pais: form.pais, checkIn: form.checkIn, checkOut: form.checkOut, cantidadPersonas: form.cantidadPersonas }
    erroresProveedores.value = []
    todasLasHabitaciones.value = mapearRespuesta(await res.json())
    error.value = todasLasHabitaciones.value.length === 0 ? 'Sin habitaciones disponibles.' : ''
    resetFiltros()
    modificarAbierto.value = false
  } catch { modError.value = 'No se pudieron obtener hoteles. Intenta de nuevo.' }
  finally { buscandoMod.value = false }
}

// ── Computed filtros dinámicos ────────────────────────────────
const tiposDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.tipoHabitacion).filter(Boolean))]
)
const hotelesDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.nombreHotel).filter(Boolean))]
)
const proveedoresDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.proveedorNombre).filter(Boolean))]
)
const amenidadesDisponibles = computed(() => {
  const set = new Set()
  todasLasHabitaciones.value.forEach(h => h.amenidades?.forEach(a => set.add(a.nombre)))
  return [...set]
})
const hayFiltrosActivos = computed(() =>
  filtros.value.precioMin > 0 || filtros.value.precioMax < 9999 ||
  filtros.value.tipos.length > 0 || filtros.value.hoteles.length > 0 ||
  filtros.value.proveedores.length > 0 || filtros.value.amenidades.length > 0
)
const cantFiltrosActivos = computed(() => {
  let n = 0
  if (filtros.value.precioMin > 0 || filtros.value.precioMax < 9999) n++
  n += filtros.value.tipos.length + filtros.value.hoteles.length + filtros.value.proveedores.length + filtros.value.amenidades.length
  return n
})

// ── Habitaciones filtradas y ordenadas ────────────────────────
const habitacionesFiltradas = computed(() => {
  let list = todasLasHabitaciones.value
  if (filtros.value.precioMin > 0)    list = list.filter(h => h.precioPorNoche >= filtros.value.precioMin)
  if (filtros.value.precioMax < 9999) list = list.filter(h => h.precioPorNoche <= filtros.value.precioMax)
  if (filtros.value.tipos.length > 0) list = list.filter(h => filtros.value.tipos.includes(h.tipoHabitacion))
  if (filtros.value.hoteles.length > 0) list = list.filter(h => filtros.value.hoteles.includes(h.nombreHotel))
  if (filtros.value.proveedores.length > 0) list = list.filter(h => filtros.value.proveedores.includes(h.proveedorNombre))
  if (filtros.value.amenidades.length > 0)
    list = list.filter(h => filtros.value.amenidades.every(a => h.amenidades?.some(x => x.nombre === a)))
  return [...list].sort((a, b) => {
    switch (ordenar.value) {
      case 'precio-asc':  return a.precioPorNoche - b.precioPorNoche
      case 'precio-desc': return b.precioPorNoche - a.precioPorNoche
      case 'capacidad':   return b.capacidadMaxima - a.capacidadMaxima
      case 'disponibles': return b.cantidadDisponible - a.cantidadDisponible
      default: return 0
    }
  })
})

// ── Grupos por hotel ──────────────────────────────────────────
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
        rating: hab.hotelRating, amenidades: hab.amenidades || [],
        habitaciones: [],
        tiposHabitacion:             hab._tiposHabitacion || [],
        tiposHabitacionPorCapacidad: hab._tiposHabitacionPorCapacidad || {},
        combinacionesNumericas:      hab._combinacionesNumericas || [],
      })
    }
    map.get(key).habitaciones.push(hab)
  }

  const personas = busqueda.value.cantidadPersonas
  return Array.from(map.values()).filter(g => {
    if (g.tiposHabitacion?.some(r => r.capacidadMaxima >= personas))                         return true
    if (Object.keys(g.tiposHabitacionPorCapacidad || {}).some(k => Number(k) >= personas))   return true
    if (_getComboHabs(g))                  return true
    if (_getComboAproximado(g, personas))  return true
    if (_getPersonaExtraMin(g, personas))  return true
    return false
  })
})

// ══ COMBO HELPERS ═════════════════════════════════════════════
function fmt(p) {
  return new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }).format(p)
}

function _getComboHabs(hotel) {
  if (!hotel.combinacionesNumericas?.length) return null
  const combo = hotel.combinacionesNumericas[0]
  if (combo.length <= 1) return null
  const usados = {}
  const result = []
  for (const cap of combo) {
    const key   = String(cap)
    const rooms = hotel.tiposHabitacionPorCapacidad?.[key]
    if (!rooms?.length) return null
    const idx = usados[key] ?? 0
    if (idx >= rooms.length) return null
    const r = rooms[idx]
    result.push({
      tipo: r.tipoHabitacion, precio: r.precioPorNoche,
      precioPorPersona: r.precioPorPersona, cap,
      tipoCama: r.tipoCama, metrosCuadrados: r.metrosCuadrados || null,
      habitacionesDisponibles: r.habitacionesDisponibles || [],
      cantidadDisponible: (r.habitacionesDisponibles || []).length,
    })
    usados[key] = idx + 1
  }
  const total = result.reduce((s, h) => s + h.precio, 0)
  return { habs: result, total }
}

function _getComboAproximado(hotel, personas) {
  const tieneDirecta = hotel.tiposHabitacion?.length > 0
  const tieneCombo   = !!_getComboHabs(hotel)
  if (tieneDirecta || tieneCombo) return null

  const porCap = hotel.tiposHabitacionPorCapacidad
  if (!porCap || Object.keys(porCap).length === 0) return null

  const todasHabs = []
  for (const [capStr, rooms] of Object.entries(porCap)) {
    const cap = Number(capStr)
    for (const room of rooms) {
      todasHabs.push({
        tipo: room.tipoHabitacion, precio: room.precioPorNoche,
        precioPorPersona: room.precioPorPersona, cap,
        tipoCama: room.tipoCama, metrosCuadrados: room.metrosCuadrados || null,
        habitacionesDisponibles: room.habitacionesDisponibles || [],
        cantidadDisponible: (room.habitacionesDisponibles || []).length,
      })
    }
  }
  todasHabs.sort((a, b) => b.cap - a.cap)

  let sumCap = 0
  const selec = []
  for (const hab of todasHabs) {
    if (sumCap >= personas) break
    selec.push(hab); sumCap += hab.cap
  }
  if (sumCap < personas || sumCap > personas + 2 || selec.length <= 1) return null
  const total = selec.reduce((s, h) => s + h.precio, 0)
  return { habs: selec, capacidadTotal: sumCap, total }
}

function _getPersonaExtraMin(hotel, personas) {
  if (personas <= 1) return null
  const rooms = hotel.tiposHabitacionPorCapacidad?.[String(personas - 1)]
  if (!rooms?.length) return null
  const best = rooms.reduce((min, r) =>
    (r.precioPorNoche + r.precioPorPersona) < (min.precioPorNoche + min.precioPorPersona) ? r : min
  , rooms[0])
  return {
    tipo: best.tipoHabitacion,
    precioPorNoche: best.precioPorNoche,
    precioPorPersona: best.precioPorPersona,
    cap: personas - 1,
    total: best.precioPorNoche + best.precioPorPersona,
    // ← incluir habitaciones para poder pre-crear la reserva
    habitacionesDisponibles: best.habitacionesDisponibles || [],
  }
}

function getHotelCombos(grupo) {
  const personas = busqueda.value.cantidadPersonas
  const combo    = _getComboHabs(grupo)
  const aprox    = _getComboAproximado(grupo, personas)
  const extra    = _getPersonaExtraMin(grupo, personas)
  if (!combo && !aprox && !extra) return null
  return { combo, aprox, extra }
}

// ── Mapeo respuesta API ───────────────────────────────────────
function mapearRespuesta(respuesta) {
  const resultado = []
  if (!Array.isArray(respuesta)) return resultado

  for (const proveedor of respuesta) {
    if (proveedor.error) { erroresProveedores.value.push(proveedor); continue }
    if (!Array.isArray(proveedor.datos)) continue

    for (const hotel of proveedor.datos) {
      if (!hotel) continue

      const roomsMap = new Map()
      if (Array.isArray(hotel.tiposHabitacion)) {
        for (const room of hotel.tiposHabitacion) roomsMap.set(room.tipoHabitacionId, room)
      }
      if (hotel.tiposHabitacionPorCapacidad && typeof hotel.tiposHabitacionPorCapacidad === 'object') {
        for (const rooms of Object.values(hotel.tiposHabitacionPorCapacidad)) {
          if (!Array.isArray(rooms)) continue
          for (const room of rooms) {
            if (!roomsMap.has(room.tipoHabitacionId)) roomsMap.set(room.tipoHabitacionId, room)
          }
        }
      }

      for (const room of roomsMap.values()) {
        const habitacionesDisp = Array.isArray(room.habitacionesDisponibles) ? room.habitacionesDisponibles : []
        resultado.push({
          uid: `${proveedor.proveedor_id}-${hotel.id}-${room.tipoHabitacionId}`,
          proveedorId: proveedor.proveedor_id, proveedorNombre: proveedor.proveedor,
          hotelId: hotel.id, nombreHotel: hotel.nombre,
          hotelCiudad: hotel.ciudad, hotelPais: hotel.pais,
          hotelDescripcion: hotel.descripcion, hotelDireccion: hotel.direccion,
          hotelRating: hotel.rating || null,
          amenidades: Array.isArray(hotel.amenidades) ? hotel.amenidades : [],
          estado: hotel.estado,
          tipoHabitacionId: room.tipoHabitacionId,
          tipoHabitacion: room.tipoHabitacion, tipoCama: room.tipoCama,
          precioPorNoche: room.precioPorNoche ?? 0,
          precioPorPersona: room.precioPorPersona ?? 0,
          metrosCuadrados: room.metrosCuadrados || null,
          capacidadMaxima: room.capacidadMaxima ?? 1,
          habitacionesDisponibles: habitacionesDisp,
          cantidadDisponible: habitacionesDisp.length,
          _tiposHabitacion:             Array.isArray(hotel.tiposHabitacion) ? hotel.tiposHabitacion : [],
          _tiposHabitacionPorCapacidad: hotel.tiposHabitacionPorCapacidad || null,
          _combinacionesNumericas:      hotel.combinacionesNumericas || [],
        })
      }
    }
  }
  return resultado
}

// ── Helpers ───────────────────────────────────────────────────
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}
function calcNoches(ci, co) {
  if (!ci || !co) return 0
  return Math.max(0, Math.ceil((new Date(co) - new Date(ci)) / 86400000))
}
function resetFiltros() {
  filtros.value = { precioMin: 0, precioMax: 9999, tipos: [], hoteles: [], proveedores: [], amenidades: [] }
}

// ── PRE-CREACIÓN DE RESERVA EN BACKGROUND ────────────────────
// Construye el payload para POST /api/reservaciones/detalle/hotel
function buildHotelPayload(reservaId, itemData) {
  const b = itemData.busqueda
  let habitaciones = []

  if (itemData.esCombo) {
    // Combo de múltiples habitaciones
    habitaciones = (itemData.habs || [])
      .filter(h => h.habitacionesDisponibles?.length > 0)
      .map(h => ({
        habitacionId:     h.habitacionesDisponibles[0].id,
        fechaCheckIn:     b.checkIn,
        fechaCheckOut:    b.checkOut,
        cantidadPersonas: h.cap,
      }))
  } else {
    // Habitación simple o extra — ambas guardan habitacionesDisponibles
    const rooms = itemData.habitacionesDisponibles || []
    if (!rooms.length) return null
    habitaciones = [{
      habitacionId:     rooms[0].id,
      fechaCheckIn:     b.checkIn,
      fechaCheckOut:    b.checkOut,
      cantidadPersonas: b.cantidadPersonas,
    }]
  }

  if (!habitaciones.length) return null

  return {
    reservacionId: reservaId,
    proveedorId:   itemData.proveedorId,
    habitaciones,
  }
}

async function precrearReservacionHotel(itemData) {
  try {
    // PASO 1: crear la reservación (tipo 2 = hotel)
    const res1 = await fetch(`${API}/api/reservaciones`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: 2 }),
    })
    if (!res1.ok) return null
    const reserva = await res1.json()

    // PASO 2: construir y enviar detalle de hotel
    const payload = buildHotelPayload(reserva.id, itemData)
    let detalle   = null

    if (payload) {
      const res2 = await fetch(`${API}/api/reservaciones/detalle/hotel`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (res2.ok) detalle = await res2.json()
    }

    // expiresAt viene SIEMPRE del backend, nunca calculado localmente.
    // Preferencia: fechaExpiracion del detalle/hotel > fecha_expiracion de la reserva
    let expiresAt = 0
    if (detalle?.detalle?.fechaExpiracion) {
      expiresAt = new Date(detalle.detalle.fechaExpiracion).getTime()
    } else if (reserva.fecha_expiracion) {
      expiresAt = new Date(reserva.fecha_expiracion.replace(' ', 'T')).getTime()
    }
    if (!expiresAt || expiresAt <= Date.now()) expiresAt = Date.now() + 600_000
    const segundos = Math.max(30, Math.floor((expiresAt - Date.now()) / 1000))

    return { reserva, detalle, segundos, expiresAt }
  } catch {
    return null
  }
}

// ── Selección → reservar ──────────────────────────────────────
function seleccionarHabitacion(hab, grupo) {
  seleccionada.value = hab.uid
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')

  const itemData = {
    ...hab,
    noches: noches.value,
    totalEstancia: hab.precioPorNoche * noches.value,
    busqueda: busqueda.value,
  }
  sessionStorage.setItem('hotel_seleccionado', JSON.stringify(itemData))

  // Disparar pre-creación en background
  window.__reservaPromise = precrearReservacionHotel(itemData)

  router.push('/reservar')
}

function reservarExtra(extraInfo, grupo) {
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')

  const itemData = {
    esExtra:         true,
    tipo:            extraInfo.tipo,
    precioPorNoche:  extraInfo.precioPorNoche,
    precioPorPersona: extraInfo.precioPorPersona,
    total:           extraInfo.total,
    // ← habitaciones necesarias para la pre-creación
    habitacionesDisponibles: extraInfo.habitacionesDisponibles || [],
    nombreHotel:     grupo.nombreHotel,
    proveedorNombre: grupo.proveedorNombre,
    proveedorId:     grupo.proveedorId,
    hotelId:         grupo.hotelId,
    noches:          noches.value,
    totalEstancia:   extraInfo.total * noches.value,
    busqueda:        busqueda.value,
  }
  sessionStorage.setItem('hotel_seleccionado', JSON.stringify(itemData))

  // Disparar pre-creación en background
  window.__reservaPromise = precrearReservacionHotel(itemData)

  router.push('/reservar')
}

function reservarCombo(comboInfo, grupo) {
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')

  const itemData = {
    esCombo:         true,
    habs:            comboInfo.habs,
    nombreHotel:     grupo.nombreHotel,
    proveedorNombre: grupo.proveedorNombre,
    proveedorId:     grupo.proveedorId,
    hotelId:         grupo.hotelId,
    direccion:       grupo.direccion,
    total:           comboInfo.total,
    noches:          noches.value,
    totalEstancia:   comboInfo.total * noches.value,
    busqueda:        busqueda.value,
  }
  sessionStorage.setItem('hotel_seleccionado', JSON.stringify(itemData))

  // Disparar pre-creación en background
  window.__reservaPromise = precrearReservacionHotel(itemData)

  router.push('/reservar')
}

onMounted(() => {
  if (!busqueda.value.ciudad) { error.value = 'Faltan datos de búsqueda.'; loading.value = false; return }
  if (resultadosRaw && Array.isArray(resultadosRaw) && resultadosRaw.length > 0) {
    todasLasHabitaciones.value = mapearRespuesta(resultadosRaw)
    if (todasLasHabitaciones.value.length === 0) error.value = 'No hay habitaciones disponibles para los criterios seleccionados.'
  } else {
    error.value = 'No hay resultados. Modifica la búsqueda.'
  }
  loading.value = false
})
</script>