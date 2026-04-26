<template>
  <div class="page">
    <Encabezado />

    <div class="rh-page">
      <div class="rh-layout">

        <!-- Sidebar de filtros con precio, tipo de habitación, hotel, proveedor y amenidades -->
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

        <!-- Contenido principal: search bar, chips, toolbar y lista agrupada por hotel -->
        <div class="rh-main">

          <!-- Search bar con resumen del destino y fechas activos -->
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

          <!-- Formulario expandible para modificar destino, fechas y personas sin salir de la vista -->
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

          <!-- Chips de filtros activos: cada chip elimina ese filtro al hacer click -->
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

          <!-- Toolbar: contador de hoteles visibles y selector de ordenamiento -->
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

          <!-- Lista de grupos, uno por hotel, con sus habitaciones, combos y reseñas -->
          <template v-if="!loading && gruposPorHotel.length > 0">
            <div
              v-for="grupo in gruposPorHotel"
              :key="`${grupo.proveedorId}-${grupo.hotelId}`"
              class="rh-grupo"
              :data-hotel-key="getHotelKey(grupo)"
            >

              <!-- Header hotel -->
              <div class="rh-grupo__head">
                <div class="rh-grupo__hotel-info">
                  <div class="rh-grupo__hotel-icon">
                    <img
                      v-if="grupo.proveedorImagen"
                      :src="'data:image/png;base64,' + grupo.proveedorImagen"
                      class="rh-grupo__hotel-img"
                      @error="e => e.target.style.display='none'"
                    />
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="22" height="22">
                      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                    </svg>
                  </div>
                  <div>
                    <div class="rh-grupo__header-row">
                      <h3 class="rh-grupo__nombre">{{ grupo.nombreHotel }}</h3>
                      <!-- Rating dinámico desde comentarios reales -->
                      <div v-if="getPromedioHotel(grupo) > 0" class="rh-grupo__rating">
                        <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13">
                          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                        </svg>
                        {{ getPromedioHotel(grupo).toFixed(1) }}
                        <span class="rh-grupo__rating-count">({{ getResenasRaiz(grupo).length }})</span>
                      </div>
                      <div v-else-if="grupo.rating" class="rh-grupo__rating">
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
                  <div class="rh-grupo__fechas-badge">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="11" height="11"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    {{ formatFecha(busqueda.checkIn) }} → {{ formatFecha(busqueda.checkOut) }}
                    <span class="rh-grupo__noches-badge">· {{ noches }} noche{{ noches !== 1 ? 's' : '' }}</span>
                  </div>
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

              <!-- Panel de combos del hotel: muestra combinaciones exactas, aproximadas y con persona extra -->
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
                    <img
                      v-if="hab.imagenesIds?.length"
                      :src="imgUrlHab(hab.imagenesIds[0])"
                      class="rh-card__img-real"
                      @error="e => e.target.style.display='none'"
                    />
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

                  <!-- ── Botón modal — span ambas columnas del grid ── -->
                  <button class="rh-det-toggle" @click.stop="abrirModalHab(hab, grupo)" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="12" height="12">
                      <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                    </svg>
                    Ver detalles de la habitación
                  </button>

                </article>
              </div>

              <!-- Sección de reseñas del hotel: se carga lazy con IntersectionObserver -->
              <div v-if="comentariosLoadingSet.has(getHotelKey(grupo)) || yaObservado.has(getHotelKey(grupo))" class="rh-resenas">

                <!-- Cargando -->
                <div v-if="comentariosLoadingSet.has(getHotelKey(grupo))" class="rh-resenas__loading">
                  <div class="rh-spinner rh-spinner--sm"></div>
                  <span>Cargando reseñas...</span>
                </div>

                <!-- Con comentarios -->
                <template v-else-if="getComentariosRaiz(grupo).length > 0">
                  <div class="rh-resenas__head">
                    <div class="rh-resenas__rating-wrap">
                      <span class="rh-resenas__avg">{{ getPromedioHotel(grupo).toFixed(1) }}</span>
                      <div class="rh-resenas__stars-row">
                        <svg v-for="n in 5" :key="n" viewBox="0 0 24 24"
                          :fill="n <= Math.round(getPromedioHotel(grupo)) ? '#FFCC00' : 'none'"
                          :stroke="n <= Math.round(getPromedioHotel(grupo)) ? '#FFCC00' : '#d0c9be'"
                          stroke-width="2" width="15" height="15">
                          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                        </svg>
                      </div>
                      <span class="rh-resenas__count">
                        {{ getResenasRaiz(grupo).length }} reseña{{ getResenasRaiz(grupo).length !== 1 ? 's' : '' }}
                      </span>
                    </div>
                    <h4 class="rh-resenas__title">Opiniones de huéspedes</h4>
                  </div>

                  <div class="rh-resenas__lista">
                    <ComentarioNodo
                      v-for="c in getComentariosRaiz(grupo)"
                      :key="c.id"
                      :comentario="c"
                      :getHijos="(id) => getHijos(grupo, id)"
                      :estadoNodos="estadoNodos"
                      :haySession="false"
                      :formatFecha="formatFechaCorta"
                      @votar="() => {}"
                      @toggleForm="() => {}"
                      @toggleExpandido="toggleExpandido"
                      @enviarRespuesta="() => {}"
                      @textoChange="() => {}"
                    />
                  </div>
                </template>

                <!-- Sin reseñas (ya cargó) -->
                <div v-else-if="yaObservado.has(getHotelKey(grupo))" class="rh-resenas__empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  Aún no hay reseñas para este hotel.
                </div>

              </div>
              <!-- Fin sección reseñas del hotel -->

            </div>
          </template>

        </div>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════════════════════════
         MODAL DE DETALLES DE HABITACIÓN — teleportado a <body>
         ═══════════════════════════════════════════════════════════════════ -->
    <Teleport to="body">
      <div v-if="modalHab" class="mv-backdrop" @click.self="cerrarModalHab">
        <div class="mv-modal" role="dialog" aria-modal="true">

          <!-- Header oscuro -->
          <div class="mv-header">
            <img v-if="modalHab.grupo.proveedorImagen"
              :src="'data:image/png;base64,' + modalHab.grupo.proveedorImagen"
              alt="Logo proveedor" class="mv-header__logo"
              @error="e => e.target.style.display='none'" />
            <div v-else class="mv-header__logo--placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="1.8" width="26" height="26">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </div>
            <div class="mv-header__info">
              <div class="mv-header__nombre">{{ modalHab.grupo.nombreHotel }}</div>
              <div class="mv-header__sub">{{ modalHab.grupo.ciudad }}, {{ modalHab.grupo.pais }} · {{ modalHab.hab.tipoHabitacion }}</div>
            </div>
            <button class="mv-header__close" @click="cerrarModalHab" type="button" aria-label="Cerrar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="20" height="20">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="mv-modal__scroll">

          <!-- Body -->
          <div class="mv-body">

            <!-- Layout 2 columnas: imagen hotel (izq) + collage habitaciones (der) -->
            <div class="mv-media-layout">
              <!-- Izquierda: imagen principal del hotel (click = lightbox) -->
              <div class="mv-media-layout__hotel"
                :class="{ 'mv-media-layout__hotel--clickable': modalHab.grupo.imagenesIds?.length }"
                @click="modalHab.grupo.imagenesIds?.length && abrirLightbox(modalHab.grupo.imagenesIds.map(imgUrlHotel), 0)">
                <img v-if="modalHab.grupo.imagenesIds?.length"
                  :src="imgUrlHotel(modalHab.grupo.imagenesIds[0])"
                  :alt="modalHab.grupo.nombreHotel"
                  class="mv-media-layout__hotel-img"
                  @error="e => e.target.style.display='none'" />
                <div v-else class="mv-media-layout__hotel-empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="28" height="28">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <span v-if="modalHab.grupo.imagenesIds?.length" class="mv-media-layout__label">Hotel</span>
              </div>
              <!-- Derecha: collage de imágenes del tipo de habitación -->
              <div class="mv-media-layout__rooms">
                <template v-if="modalHab.hab.imagenesIds?.length">
                  <img v-for="(id, i) in modalHab.hab.imagenesIds.slice(0, 4)" :key="id"
                    :src="imgUrlHab(id)"
                    :alt="`Hab. ${i + 1}`"
                    class="mv-media-layout__room-img"
                    @click="abrirLightbox(modalHab.hab.imagenesIds.map(imgUrlHab), i)"
                    @error="e => e.target.style.display='none'" />
                  <div v-if="modalHab.hab.imagenesIds.length > 4"
                    class="mv-media-layout__room-img mv-media-layout__room-more"
                    @click="abrirLightbox(modalHab.hab.imagenesIds.map(imgUrlHab), 4)">
                    +{{ modalHab.hab.imagenesIds.length - 4 }}
                  </div>
                </template>
                <div v-else class="mv-media-layout__rooms-empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="20" height="20">
                    <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/>
                    <polyline points="21 15 16 10 5 21"/>
                  </svg>
                  <span>Sin imágenes</span>
                </div>
              </div>
            </div>

            <!-- Info del hotel -->
            <template v-if="modalHab.grupo.descripcion || modalHab.grupo.direccion">
              <div class="mv-sep">Sobre el hotel</div>
              <p v-if="modalHab.grupo.descripcion" style="font-size:.88rem;color:#3d3630;line-height:1.55;margin:0 0 10px">{{ modalHab.grupo.descripcion }}</p>
              <div v-if="modalHab.grupo.direccion" class="mv-grid">
                <div class="mv-bloque" style="grid-column: 1 / -1">
                  <span class="mv-lbl">Dirección</span>
                  <span class="mv-val">{{ modalHab.grupo.direccion }}</span>
                </div>
              </div>
            </template>

            <!-- Specs de habitación -->
            <div class="mv-sep">Detalles de la habitación</div>
            <div class="mv-grid">
              <div class="mv-bloque">
                <span class="mv-lbl">Tipo</span>
                <span class="mv-val">{{ modalHab.hab.tipoHabitacion }}</span>
              </div>
              <div class="mv-bloque">
                <span class="mv-lbl">Tipo de cama</span>
                <span class="mv-val">{{ modalHab.hab.tipoCama }}</span>
              </div>
              <div v-if="modalHab.hab.metrosCuadrados" class="mv-bloque">
                <span class="mv-lbl">Superficie</span>
                <span class="mv-val">{{ modalHab.hab.metrosCuadrados }} m²</span>
              </div>
              <div class="mv-bloque">
                <span class="mv-lbl">Capacidad máxima</span>
                <span class="mv-val">{{ modalHab.hab.capacidadMaxima }} persona{{ modalHab.hab.capacidadMaxima !== 1 ? 's' : '' }}</span>
              </div>
              <div class="mv-bloque">
                <span class="mv-lbl">Disponibilidad</span>
                <span class="mv-val" :class="{ 'mv-val--urgente': modalHab.hab.cantidadDisponible > 0 && modalHab.hab.cantidadDisponible <= 3, 'mv-val--out': modalHab.hab.cantidadDisponible === 0 }">
                  {{ modalHab.hab.cantidadDisponible === 0 ? 'Agotado' : modalHab.hab.cantidadDisponible + ' habitaciones' }}
                </span>
              </div>
            </div>

            <!-- Amenidades -->
            <template v-if="modalHab.grupo.amenidades?.length">
              <div class="mv-sep">Amenidades del hotel</div>
              <div class="mv-amenidades">
                <span v-for="am in modalHab.grupo.amenidades" :key="am.amenidadId"
                  class="mv-amenidad" :title="am.descripcion">
                  <span class="mv-amenidad__icon" v-html="amenityIcon(am.nombre)"></span>
                  {{ am.nombre }}
                </span>
              </div>
            </template>

          </div>
          </div><!-- /mv-modal__scroll -->
        </div>
      </div>
    </Teleport>

    <!-- Lightbox pantalla completa -->
    <Teleport to="body">
      <div v-if="lightbox" class="mv-lightbox" @click.self="cerrarLightbox">
        <img :src="lightbox.imgs[lightbox.idx]" class="mv-lightbox__img" @error="onImgError" />
        <button class="mv-lightbox__close" @click="cerrarLightbox" type="button" aria-label="Cerrar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="20" height="20">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        <button v-if="lightbox.imgs.length > 1" class="mv-lightbox__arrow mv-lightbox__arrow--prev" @click="lightboxPrev" type="button" aria-label="Anterior">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="20" height="20"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <button v-if="lightbox.imgs.length > 1" class="mv-lightbox__arrow mv-lightbox__arrow--next" @click="lightboxNext" type="button" aria-label="Siguiente">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="20" height="20"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
        <div v-if="lightbox.imgs.length > 1" class="mv-lightbox__counter">
          {{ lightbox.idx + 1 }} / {{ lightbox.imgs.length }}
        </div>
      </div>
    </Teleport>

    <Piepagina />
  </div>
</template>

<script setup>
/**
 * @file ResultadosHoteles.vue
 * @description Vista de resultados de búsqueda de hoteles. Agrupa las habitaciones por hotel,
 * muestra combos inteligentes de habitaciones (exacto, aproximado, persona extra), aplica
 * filtros dinámicos, carga reseñas de forma lazy con IntersectionObserver y pre-crea la
 * reservación en background al seleccionar.
 */
import { ref, computed, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Encabezado from '../components/Encabezado.vue'
import Piepagina from '../components/Piepagina.vue'
import '../styles/resultadoshoteles.css'
import '../styles/detalle-modal.css'
import ComentarioNodo from '../components/Comentarionodo.vue'

const router = useRouter()

/** URL base del backend. @type {string} */
const API = import.meta.env.VITE_API_URL || 'http://localhost:8080'

/**
 * Genera los headers de autenticación incluyendo el JWT si está disponible en storage.
 * @returns {Record<string, string>}
 */
function authHeaders() {
  // La autenticacion viaja por cookie HttpOnly (credentials: 'include')
  return {
    'Content-Type': 'application/json',
  }
}
/**
 * Wrapper de fetch que adjunta headers de auth y lanza error en respuestas no-OK.
 * @param {string} url
 * @param {RequestInit} [opts]
 * @returns {Promise<any>}
 */
async function apiFetch(url, opts = {}) {
  const res = await fetch(url, { headers: authHeaders(), credentials: 'include', ...opts })
  if (!res.ok) throw new Error(`Error ${res.status}`)
  return res.json()
}

// Estado inicial recuperado desde history.state (inyectado por el buscador principal)
const state = history.state || {}

/** Resultados crudos de hoteles recibidos desde el buscador, o null si no hay. @type {any[]|null} */
const resultadosRaw = state.resultados || null

// Fallback de sesión: cuando Vue Router reemplaza history.state al navegar a /reservar,
// los datos de búsqueda y resultados se recuperan desde sessionStorage al volver.
const _savedRhBusqueda   = (() => { try { return JSON.parse(sessionStorage.getItem('_rh_busqueda')   || 'null') } catch { return null } })()
const _savedRhHabitaciones = (() => { try { return JSON.parse(sessionStorage.getItem('_rh_habitaciones') || 'null') } catch { return null } })()

/**
 * Parámetros de la búsqueda activa. Se inicializan desde history.state y se actualizan al rebuscar.
 * @type {import('vue').Ref<{ciudad: string, pais: string, checkIn: string, checkOut: string, cantidadPersonas: number}>}
 */
const busqueda = ref({
  ciudad:           state.busqueda?.ciudad           || _savedRhBusqueda?.ciudad           || '',
  pais:             state.busqueda?.pais             || _savedRhBusqueda?.pais             || '',
  checkIn:          state.busqueda?.checkIn          || _savedRhBusqueda?.checkIn          || '',
  checkOut:         state.busqueda?.checkOut         || _savedRhBusqueda?.checkOut         || '',
  cantidadPersonas: state.busqueda?.cantidadPersonas || _savedRhBusqueda?.cantidadPersonas || 1,
})

/** Indica que se está realizando una búsqueda (muestra spinner). @type {import('vue').Ref<boolean>} */
const loading = ref(true)

/** Mensaje de error global; vacío cuando no hay error. @type {import('vue').Ref<string>} */
const error = ref('')

/** UID de la habitación actualmente marcada como seleccionada (feedback visual). @type {import('vue').Ref<string|null>} */
const seleccionada = ref(null)

/** Habitación + grupo abiertos en el modal de detalles. @type {import('vue').Ref<{hab:object,grupo:object}|null>} */
const modalHab = ref(null)
/** Índice de la imagen activa en la galería del modal. @type {import('vue').Ref<number>} */
const modalImgIdx = ref(0)
/** URL base del proveedor de hoteles para construir URLs de imágenes. @type {string} */
const HOTEL_API = import.meta.env.VITE_HOTEL_API_URL || 'http://localhost:7000'

function abrirModalHab(hab, grupo) {
  modalHab.value = { hab, grupo }
  modalImgIdx.value = 0
  lightbox.value = null
  document.body.style.overflow = 'hidden'
}
function cerrarModalHab() { modalHab.value = null; lightbox.value = null; document.body.style.overflow = '' }
function imgUrlHab(id)   { return `${HOTEL_API}/imagenes/habitacion/${id}` }
function imgUrlHotel(id) { return `${HOTEL_API}/imagenes/hotel/${id}` }
function onImgError(e) { e.target.style.display = 'none' }

function prevImg() {
  const imgs = modalHab.value?.hab?.imagenesIds
  if (!imgs?.length) return
  modalImgIdx.value = (modalImgIdx.value - 1 + imgs.length) % imgs.length
}
function nextImg() {
  const imgs = modalHab.value?.hab?.imagenesIds
  if (!imgs?.length) return
  modalImgIdx.value = (modalImgIdx.value + 1) % imgs.length
}

/** Lightbox: { imgs, idx } */
const lightbox = ref(null)
function abrirLightbox(imgs, idx) { lightbox.value = { imgs, idx } }
function cerrarLightbox() { lightbox.value = null }
function lightboxPrev() { if (!lightbox.value) return; lightbox.value.idx = (lightbox.value.idx - 1 + lightbox.value.imgs.length) % lightbox.value.imgs.length }
function lightboxNext() { if (!lightbox.value) return; lightbox.value.idx = (lightbox.value.idx + 1) % lightbox.value.imgs.length }

const _S = (d) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">${d}</svg>`
const AMENITY_ICONS = {
  wifi:            _S('<path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><circle cx="12" cy="20" r="1" fill="currentColor"/>'),
  'wi-fi':         _S('<path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><circle cx="12" cy="20" r="1" fill="currentColor"/>'),
  internet:        _S('<path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><circle cx="12" cy="20" r="1" fill="currentColor"/>'),
  piscina:         _S('<path d="M2 12h20"/><path d="M2 6l4 4 4-4 4 4 4-4 4 4"/><path d="M2 18l4-4 4 4 4-4 4 4"/>'),
  pool:            _S('<path d="M2 12h20"/><path d="M2 6l4 4 4-4 4 4 4-4 4 4"/><path d="M2 18l4-4 4 4 4-4 4 4"/>'),
  alberca:         _S('<path d="M2 12h20"/><path d="M2 6l4 4 4-4 4 4 4-4 4 4"/><path d="M2 18l4-4 4 4 4-4 4 4"/>'),
  gimnasio:        _S('<path d="M6 5v14"/><path d="M18 5v14"/><path d="M2 9v6"/><path d="M22 9v6"/><line x1="6" y1="12" x2="18" y2="12"/>'),
  gym:             _S('<path d="M6 5v14"/><path d="M18 5v14"/><path d="M2 9v6"/><path d="M22 9v6"/><line x1="6" y1="12" x2="18" y2="12"/>'),
  fitness:         _S('<path d="M6 5v14"/><path d="M18 5v14"/><path d="M2 9v6"/><path d="M22 9v6"/><line x1="6" y1="12" x2="18" y2="12"/>'),
  restaurante:     _S('<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/>'),
  restaurant:      _S('<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/>'),
  comedor:         _S('<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/>'),
  estacionamiento: _S('<rect x="1" y="3" width="15" height="13" rx="2"/><path d="M16 8h4l3 3v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>'),
  parking:         _S('<rect x="1" y="3" width="15" height="13" rx="2"/><path d="M16 8h4l3 3v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>'),
  parqueo:         _S('<rect x="1" y="3" width="15" height="13" rx="2"/><path d="M16 8h4l3 3v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>'),
  spa:             _S('<path d="M12 22c4.97 0 9-2.69 9-6V4c-3.87 1.52-5.68 3.97-7 6-1.32-2.03-3.13-4.48-7-6v12c0 3.31 4.03 6 5 6z"/>'),
  masaje:          _S('<path d="M12 22c4.97 0 9-2.69 9-6V4c-3.87 1.52-5.68 3.97-7 6-1.32-2.03-3.13-4.48-7-6v12c0 3.31 4.03 6 5 6z"/>'),
  bar:             _S('<polyline points="8 22 8 14 2 2 22 2 16 14 16 22"/><line x1="8" y1="22" x2="16" y2="22"/>'),
  'cafetería':     _S('<path d="M17 8h1a4 4 0 1 1 0 8h-1"/><path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V8z"/><line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/>'),
  'café':          _S('<path d="M17 8h1a4 4 0 1 1 0 8h-1"/><path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V8z"/><line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/>'),
  'lavandería':    _S('<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="12" cy="13" r="4"/><path d="M9 7h1"/><path d="M12 7h1"/>'),
  laundry:         _S('<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="12" cy="13" r="4"/><path d="M9 7h1"/><path d="M12 7h1"/>'),
  desayuno:        _S('<circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4"/><line x1="21.17" y1="8" x2="12" y2="8"/><line x1="3.95" y1="6.06" x2="8.54" y2="14"/>'),
  breakfast:       _S('<circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4"/><line x1="21.17" y1="8" x2="12" y2="8"/><line x1="3.95" y1="6.06" x2="8.54" y2="14"/>'),
  aire:            _S('<path d="M17.7 7.7a2.5 2.5 0 1 1 1.8 4.3H2"/><path d="M9.6 4.6A2 2 0 1 1 11 8H2"/><path d="M12.6 19.4A2 2 0 1 0 14 16H2"/>'),
  'a/c':           _S('<path d="M17.7 7.7a2.5 2.5 0 1 1 1.8 4.3H2"/><path d="M9.6 4.6A2 2 0 1 1 11 8H2"/><path d="M12.6 19.4A2 2 0 1 0 14 16H2"/>'),
  tv:              _S('<rect x="2" y="7" width="20" height="15" rx="2"/><polyline points="17 2 12 7 7 2"/>'),
  'televisión':    _S('<rect x="2" y="7" width="20" height="15" rx="2"/><polyline points="17 2 12 7 7 2"/>'),
  television:      _S('<rect x="2" y="7" width="20" height="15" rx="2"/><polyline points="17 2 12 7 7 2"/>'),
  terraza:         _S('<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>'),
  'balcón':        _S('<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>'),
  balcon:          _S('<circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/>'),
  'jardín':        _S('<path d="M17 8c0 4-2.5 6-5 8.5C9.5 14 7 12 7 8a5 5 0 0 1 10 0z"/><path d="M12 21v-5"/>'),
  jardin:          _S('<path d="M17 8c0 4-2.5 6-5 8.5C9.5 14 7 12 7 8a5 5 0 0 1 10 0z"/><path d="M12 21v-5"/>'),
  seguridad:       _S('<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>'),
  vigilancia:      _S('<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>'),
  'recepción':     _S('<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>'),
  recepcion:       _S('<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>'),
  '24 horas':      _S('<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>'),
}
const _DEFAULT_ICON = _S('<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>')
function amenityIcon(nombre) {
  const key = (nombre || '').toLowerCase()
  for (const [k, v] of Object.entries(AMENITY_ICONS)) {
    if (key.includes(k)) return v
  }
  return _DEFAULT_ICON
}

// Teclado: ESC cierra, flechas navegan
function _onKeydown(e) {
  if (lightbox.value) {
    if (e.key === 'Escape') { cerrarLightbox(); return }
    if (e.key === 'ArrowLeft')  { lightboxPrev(); return }
    if (e.key === 'ArrowRight') { lightboxNext(); return }
  }
  if (modalHab.value) {
    if (e.key === 'Escape') { cerrarModalHab(); return }
    if (e.key === 'ArrowLeft')  { prevImg(); return }
    if (e.key === 'ArrowRight') { nextImg(); return }
  }
}

/** Controla la visibilidad del sidebar de filtros en móvil. @type {import('vue').Ref<boolean>} */
const filtrosAbiertos = ref(false)

/** Criterio de ordenamiento activo en el selector de la toolbar. @type {import('vue').Ref<string>} */
const ordenar = ref('precio-asc')

/** Lista de proveedores que devolvieron error; se usa para mostrar el aviso de resultados incompletos. @type {import('vue').Ref<any[]>} */
const erroresProveedores = ref([])

/** Controla si el formulario de modificar búsqueda está expandido. @type {import('vue').Ref<boolean>} */
const modificarAbierto = ref(false)

/** True mientras se ejecuta una nueva búsqueda desde el formulario de modificar. @type {import('vue').Ref<boolean>} */
const buscandoMod = ref(false)

/** Mensaje de error específico del formulario de modificar búsqueda. @type {import('vue').Ref<string>} */
const modError = ref('')

/** Fecha de hoy en formato ISO (YYYY-MM-DD), usada como mínimo para los inputs de fecha. @type {string} */
const hoy = new Date().toISOString().split('T')[0]

/** Lista plana de todas las habitaciones disponibles mapeadas desde la API. @type {import('vue').Ref<object[]>} */
const todasLasHabitaciones = ref([])

/**
 * Objeto reactivo con todos los filtros aplicables a la lista de habitaciones.
 * @type {import('vue').Ref<{precioMin: number, precioMax: number, tipos: string[], hoteles: string[], proveedores: string[], amenidades: string[]}>}
 */
const filtros = ref({
  precioMin: 0, precioMax: 9999,
  tipos: [], hoteles: [], proveedores: [],
  amenidades: [],
})

/**
 * Número de noches entre check-in y check-out de la búsqueda activa.
 * @type {import('vue').ComputedRef<number>}
 */
const noches = computed(() => calcNoches(busqueda.value.checkIn, busqueda.value.checkOut))

/**
 * Estado reactivo del formulario de modificar búsqueda: campos de país/ciudad con
 * autocompletado, fechas y cantidad de personas.
 * @type {{dPaisQ: string, dPaisSug: any[], dPaisSel: any, dCiudadQ: string, dCiudadSug: string[], dCiudadLoading: boolean, dCiudades: string[], pais: string, ciudad: string, checkIn: string, checkOut: string, cantidadPersonas: number}}
 */
const form = reactive({
  dPaisQ: '', dPaisSug: [], dPaisSel: null,
  dCiudadQ: '', dCiudadSug: [], dCiudadLoading: false,
  dCiudades: [], pais: '', ciudad: '',
  checkIn: '', checkOut: '', cantidadPersonas: 1,
})

/**
 * Fecha mínima permitida para el check-out en el formulario: un día después del check-in seleccionado.
 * @type {import('vue').ComputedRef<string>}
 */
const minCheckOutForm = computed(() => {
  if (!form.checkIn) return hoy
  const d = new Date(form.checkIn); d.setDate(d.getDate() + 1)
  return d.toISOString().split('T')[0]
})

/** Caché en memoria de la lista de países obtenida de CountriesNow. @type {any[]|null} */
let paisesCache = null

/**
 * Obtiene la lista de países desde la API de CountriesNow, usando caché en memoria.
 * @returns {Promise<any[]>}
 */
async function getPaises() {
  if (paisesCache) return paisesCache
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries'); const d = await r.json(); paisesCache = d.data || [] } catch { paisesCache = [] }
  return paisesCache
}

/**
 * Obtiene la lista de ciudades de un país específico desde CountriesNow.
 * @param {string} country - Nombre del país en inglés
 * @returns {Promise<string[]>}
 */
async function getCiudades(country) {
  try { const r = await fetch('https://countriesnow.space/api/v0.1/countries/cities', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ country }) }); const d = await r.json(); return d.data || [] } catch { return [] }
}

/**
 * Cierra un dropdown de autocompletado con un pequeño delay para permitir que el click en la opción se registre antes.
 * @param {Function} fn - Función que limpia la lista de sugerencias
 */
function blurClose(fn) { setTimeout(fn, 200) }

/** Filtra la lista de países según el texto ingresado en el campo de país del formulario. */
async function onFormDPaisInput() {
  form.dPaisSel = null; form.dCiudadQ = ''; form.dCiudades = []; form.pais = ''; form.ciudad = ''
  const q = form.dPaisQ.trim(); if (q.length < 2) { form.dPaisSug = []; return }
  form.dPaisSug = (await getPaises()).filter(x => x.country.toLowerCase().includes(q.toLowerCase())).slice(0, 6)
}

/**
 * Selecciona un país del dropdown y carga sus ciudades disponibles.
 * @param {{country: string}} p - Objeto país de CountriesNow
 */
async function selFormDPais(p) {
  form.dPaisSel = p; form.dPaisQ = p.country; form.dPaisSug = []; form.pais = p.country
  form.dCiudadLoading = true; form.dCiudades = await getCiudades(p.country); form.dCiudadLoading = false
}

/** Filtra la lista de ciudades según el texto ingresado en el campo de ciudad del formulario. */
function onFormDCiudadInput() {
  const q = form.dCiudadQ.toLowerCase()
  form.dCiudadSug = q.length < 2 ? [] : form.dCiudades.filter(c => c.toLowerCase().includes(q)).slice(0, 6)
  form.ciudad = ''
}

/**
 * Selecciona una ciudad del dropdown y la establece como destino en el formulario.
 * @param {string} c - Nombre de la ciudad seleccionada
 */
function selFormDCiudad(c) { form.dCiudadQ = c; form.dCiudadSug = []; form.ciudad = c; modError.value = '' }

/**
 * Abre o cierra el formulario de modificar búsqueda y, al abrir, pre-llena
 * los campos con los valores de la búsqueda activa.
 */
function toggleModificar() {
  modificarAbierto.value = !modificarAbierto.value
  if (modificarAbierto.value) {
    Object.assign(form, {
      // Destino — pre-llenar con búsqueda actual
      dPaisQ:         busqueda.value.pais    || '',
      pais:           busqueda.value.pais    || '',
      dCiudadQ:       busqueda.value.ciudad  || '',
      ciudad:         busqueda.value.ciudad  || '',
      dPaisSug:       [],
      dPaisSel:       busqueda.value.pais ? { country: busqueda.value.pais } : null,
      dCiudadSug:     [],
      dCiudadLoading: false,
      dCiudades:      [],

      // Fechas
      checkIn:          busqueda.value.checkIn          || '',
      checkOut:         busqueda.value.checkOut         || '',

      // Personas
      cantidadPersonas: busqueda.value.cantidadPersonas || 1,
    })
    modError.value = ''
  }
}

/**
 * Valida el formulario de modificar y lanza una nueva búsqueda de hoteles al backend.
 * Al completar, reemplaza los resultados y reinicia los comentarios y filtros.
 */
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
    // Limpiar comentarios de búsqueda anterior
    comentariosHoteles.value = {}
    comentariosLoadingSet.value = new Set()
    yaObservado.value = new Set()
    todasLasHabitaciones.value = mapearRespuesta(await res.json())
    error.value = todasLasHabitaciones.value.length === 0 ? 'Sin habitaciones disponibles.' : ''
    resetFiltros()
    modificarAbierto.value = false
  } catch { modError.value = 'No se pudieron obtener hoteles. Intenta de nuevo.' }
  finally { buscandoMod.value = false }
}

/**
 * Lista deduplicada de tipos de habitación presentes en los resultados,
 * usada para poblar el filtro de tipo.
 * @type {import('vue').ComputedRef<string[]>}
 */
const tiposDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.tipoHabitacion).filter(Boolean))]
)

/**
 * Lista deduplicada de nombres de hotel presentes en los resultados,
 * usada para poblar el filtro de hotel.
 * @type {import('vue').ComputedRef<string[]>}
 */
const hotelesDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.nombreHotel).filter(Boolean))]
)

/**
 * Lista deduplicada de nombres de proveedor, usada para el filtro de proveedor.
 * Solo se muestra si hay más de uno disponible.
 * @type {import('vue').ComputedRef<string[]>}
 */
const proveedoresDisponibles = computed(() =>
  [...new Set(todasLasHabitaciones.value.map(h => h.proveedorNombre).filter(Boolean))]
)

/**
 * Lista deduplicada de nombres de amenidad presentes en todos los hoteles,
 * usada para el filtro de amenidades.
 * @type {import('vue').ComputedRef<string[]>}
 */
const amenidadesDisponibles = computed(() => {
  const set = new Set()
  todasLasHabitaciones.value.forEach(h => h.amenidades?.forEach(a => set.add(a.nombre)))
  return [...set]
})

/**
 * True si al menos uno de los filtros tiene un valor diferente al predeterminado.
 * @type {import('vue').ComputedRef<boolean>}
 */
const hayFiltrosActivos = computed(() =>
  filtros.value.precioMin > 0 || filtros.value.precioMax < 9999 ||
  filtros.value.tipos.length > 0 || filtros.value.hoteles.length > 0 ||
  filtros.value.proveedores.length > 0 || filtros.value.amenidades.length > 0
)

/**
 * Número total de filtros activos; se usa para mostrar el badge en el sidebar.
 * @type {import('vue').ComputedRef<number>}
 */
const cantFiltrosActivos = computed(() => {
  let n = 0
  if (filtros.value.precioMin > 0 || filtros.value.precioMax < 9999) n++
  n += filtros.value.tipos.length + filtros.value.hoteles.length + filtros.value.proveedores.length + filtros.value.amenidades.length
  return n
})

/**
 * Lista de habitaciones que pasan todos los filtros activos, ordenada según el criterio seleccionado.
 * @type {import('vue').ComputedRef<object[]>}
 */
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

/**
 * Agrupa las habitaciones filtradas por hotel y filtra los grupos que no pueden
 * alojar a la cantidad de personas buscada (ni directamente ni mediante combos).
 * @type {import('vue').ComputedRef<object[]>}
 */
const gruposPorHotel = computed(() => {
  const map = new Map()
  for (const hab of habitacionesFiltradas.value) {
    const key = `${hab.proveedorId}-${hab.hotelId}`
    if (!map.has(key)) {
      map.set(key, {
        hotelId: hab.hotelId, proveedorId: hab.proveedorId,
        proveedorNombre: hab.proveedorNombre, proveedorImagen: hab.proveedorImagen || '', nombreHotel: hab.nombreHotel,
        ciudad: hab.hotelCiudad, pais: hab.hotelPais,
        descripcion: hab.hotelDescripcion, direccion: hab.hotelDireccion,
        rating: hab.hotelRating, amenidades: hab.amenidades || [],
        imagenesIds: hab.hotelImagenesIds || [],
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

/**
 * Formatea un número como precio en USD con separadores de miles.
 * @param {number} p - Monto a formatear
 * @returns {string}
 */
function fmt(p) {
  return new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD', minimumFractionDigits: 0 }).format(p)
}

/**
 * Intenta construir una combinación exacta de habitaciones que cubra exactamente
 * a los pasajeros buscados usando la primera combinación numérica disponible del hotel.
 * @param {object} hotel - Grupo de hotel con combinacionesNumericas y tiposHabitacionPorCapacidad
 * @returns {{habs: object[], total: number}|null}
 */
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

/**
 * Intenta armar una combinación aproximada de habitaciones cuando no existe
 * una combinación exacta ni habitación individual. Acepta hasta 2 personas de exceso.
 * Solo aplica si hay más de una habitación en la combinación.
 * @param {object} hotel - Grupo de hotel
 * @param {number} personas - Cantidad de personas buscadas
 * @returns {{habs: object[], capacidadTotal: number, total: number}|null}
 */
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
      const stockFisico = room.habitacionesDisponibles?.length || 1
      for (let i = 0; i < stockFisico; i++) {
        todasHabs.push({
          tipo: room.tipoHabitacion, precio: room.precioPorNoche,
          precioPorPersona: room.precioPorPersona, cap,
          tipoCama: room.tipoCama, metrosCuadrados: room.metrosCuadrados || null,
          habitacionesDisponibles: room.habitacionesDisponibles || [],
          cantidadDisponible: (room.habitacionesDisponibles || []).length,
        })
      }
    }
  }
  todasHabs.sort((a, b) => b.cap - a.cap)

  let sumCap = 0
  const selec = []
  for (const hab of todasHabs) {
    if (sumCap >= personas) break
    if (selec.length >= 3) break
    selec.push(hab); sumCap += hab.cap
  }
  if (!selec.length) return null
  const minCap = Math.min(...selec.map(h => h.cap))
  if (sumCap < personas || sumCap > personas + minCap || selec.length <= 1) return null
  const total = selec.reduce((s, h) => s + h.precio, 0)
  return { habs: selec, capacidadTotal: sumCap, total }
}

/**
 * Busca la habitación de capacidad (personas - 1) más económica que acepte persona extra.
 * @param {object} hotel - Grupo de hotel
 * @param {number} personas - Cantidad de personas buscadas
 * @returns {object|null} Datos de la habitación con el precio extra calculado, o null si no aplica
 */
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
    habitacionesDisponibles: best.habitacionesDisponibles || [],
  }
}

/**
 * Calcula los tres tipos de combo disponibles para un grupo de hotel:
 * combinación exacta, aproximada y habitación con persona extra.
 * Devuelve null si ninguno aplica.
 * @param {object} grupo - Grupo de hotel de gruposPorHotel
 * @returns {{combo: object|null, aprox: object|null, extra: object|null}|null}
 */
function getHotelCombos(grupo) {
  const personas = busqueda.value.cantidadPersonas
  const combo    = _getComboHabs(grupo)
  const aprox    = _getComboAproximado(grupo, personas)
  const extra    = _getPersonaExtraMin(grupo, personas)
  if (!combo && !aprox && !extra) return null
  return { combo, aprox, extra }
}

/**
 * Mapa reactivo de comentarios indexado por clave de hotel (`proveedorId::hotelId`).
 * @type {import('vue').Ref<Record<string, object[]>>}
 */
const comentariosHoteles = ref({})

/**
 * Set reactivo de claves de hotel cuyos comentarios están actualmente cargando.
 * @type {import('vue').Ref<Set<string>>}
 */
const comentariosLoadingSet = ref(new Set())

/**
 * Set reactivo de claves de hotel que ya fueron observadas por el IntersectionObserver
 * (evita peticiones duplicadas).
 * @type {import('vue').Ref<Set<string>>}
 */
const yaObservado = ref(new Set())

/** Instancia del IntersectionObserver para carga lazy de reseñas. @type {IntersectionObserver|null} */
let hotelObserver = null

/**
 * Estado de UI de cada nodo de comentario (expandido, formulario, etc.).
 * @type {import('vue').Ref<Record<number, object>>}
 */
const estadoNodos = ref({})

/**
 * Genera la clave única para identificar el hotel en el mapa de comentarios.
 * @param {object} grupo - Grupo de hotel
 * @returns {string}
 */
function getHotelKey(grupo) {
  return `${grupo.proveedorId}::${grupo.hotelId}`
}

/**
 * Devuelve todos los comentarios (raíz e hijos) de un hotel.
 * @param {object} grupo - Grupo de hotel
 * @returns {object[]}
 */
function getComentariosHotel(grupo) {
  return comentariosHoteles.value[getHotelKey(grupo)] ?? []
}

/**
 * Devuelve solo los comentarios raíz (sin padre) de un hotel, usados por ComentarioNodo.
 * @param {object} grupo - Grupo de hotel
 * @returns {object[]}
 */
function getComentariosRaiz(grupo) {
  return getComentariosHotel(grupo).filter(c => c.comentarioPadreId === null)
}

/**
 * Devuelve los comentarios raíz que tienen puntuación, usados para calcular el promedio.
 * @param {object} grupo - Grupo de hotel
 * @returns {object[]}
 */
function getResenasRaiz(grupo) {
  return getComentariosHotel(grupo).filter(c => c.comentarioPadreId === null && c.resena !== null)
}

/**
 * Devuelve los comentarios hijos de un comentario padre, usado internamente por ComentarioNodo.
 * @param {object} grupo - Grupo de hotel
 * @param {number} parentId - ID del comentario padre
 * @returns {object[]}
 */
function getHijos(grupo, parentId) {
  return getComentariosHotel(grupo).filter(c => c.comentarioPadreId === parentId)
}

/**
 * Calcula el promedio de estrellas del hotel a partir de las reseñas con puntuación.
 * @param {object} grupo - Grupo de hotel
 * @returns {number} Promedio de 0 a 5
 */
function getPromedioHotel(grupo) {
  const resenas = getResenasRaiz(grupo)
  if (!resenas.length) return 0
  return resenas.reduce((s, r) => s + (r.resena ?? 0), 0) / resenas.length
}

/**
 * Alterna el estado expandido de un nodo de comentario (para ver respuestas anidadas).
 * @param {number} id - ID del comentario
 */
function toggleExpandido(id) {
  estadoNodos.value = {
    ...estadoNodos.value,
    [id]: { ...(estadoNodos.value[id] ?? { expandido: false, mostrandoForm: false, textoRespuesta: '', enviando: false, votoActual: null }), expandido: !estadoNodos.value[id]?.expandido }
  }
}

/**
 * Carga los comentarios de un hotel específico desde la API y los almacena en el mapa reactivo.
 * Previene cargas duplicadas verificando `yaObservado`.
 * @param {number} proveedorId
 * @param {number} hotelId
 * @param {string} key - Clave compuesta del hotel
 */
async function cargarComentariosHotel(proveedorId, hotelId, key) {
  if (yaObservado.value.has(key)) return

  // Marcar como observado y en carga
  yaObservado.value = new Set([...yaObservado.value, key])
  comentariosLoadingSet.value = new Set([...comentariosLoadingSet.value, key])

  try {
    const data = await apiFetch(`${API}/api/comentarios/hotel/${proveedorId}/${hotelId}`)
    comentariosHoteles.value = { ...comentariosHoteles.value, [key]: data ?? [] }
  } catch {
    comentariosHoteles.value = { ...comentariosHoteles.value, [key]: [] }
  } finally {
    const s = new Set(comentariosLoadingSet.value)
    s.delete(key)
    comentariosLoadingSet.value = s
  }
}

/**
 * Inicializa (o reinicia) el IntersectionObserver que dispara la carga de reseñas
 * cuando un grupo de hotel entra en el viewport.
 */
function initHotelObserver() {
  if (hotelObserver) hotelObserver.disconnect()
  hotelObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue
      const key = entry.target.dataset.hotelKey
      if (!key || yaObservado.value.has(key)) continue
      const [pid, hid] = key.split('::')
      cargarComentariosHotel(Number(pid), Number(hid), key)
      hotelObserver.unobserve(entry.target)
    }
  }, { rootMargin: '200px 0px' })
}

/**
 * Registra todos los elementos `[data-hotel-key]` visibles para ser observados
 * por el IntersectionObserver. Se llama después de cada actualización del DOM.
 */
async function observarGrupos() {
  await nextTick()
  if (!hotelObserver) return
  document.querySelectorAll('[data-hotel-key]').forEach(el => {
    const key = el.dataset.hotelKey
    if (key && !yaObservado.value.has(key)) {
      hotelObserver.observe(el)
    }
  })
}

// Re-observar cuando cambian los grupos visibles (por rebuscar o cambio de filtros)
watch(gruposPorHotel, () => observarGrupos(), { flush: 'post' })

/**
 * Transforma la respuesta cruda de la API (array de proveedores con hoteles anidados)
 * en una lista plana de habitaciones normalizadas. Registra proveedores con error.
 * @param {any[]} respuesta - Array de objetos proveedor retornado por el backend
 * @returns {object[]} Lista plana de habitaciones listas para mostrar
 */
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
          proveedorId: proveedor.proveedor_id, proveedorNombre: proveedor.proveedor, proveedorImagen: proveedor.proveedorImagen || '',
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
          imagenesIds: Array.isArray(room.imagenesIds) ? room.imagenesIds : [],
          hotelImagenesIds: Array.isArray(hotel.imagenesIds) ? hotel.imagenesIds : [],
          _tiposHabitacion:             Array.isArray(hotel.tiposHabitacion) ? hotel.tiposHabitacion : [],
          _tiposHabitacionPorCapacidad: hotel.tiposHabitacionPorCapacidad || null,
          _combinacionesNumericas:      hotel.combinacionesNumericas || [],
        })
      }
    }
  }
  return resultado
}

/**
 * Formatea una fecha ISO (YYYY-MM-DD) en formato legible en español guatemalteco.
 * @param {string} f - Fecha en formato ISO
 * @returns {string}
 */
function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}

/**
 * Formatea una fecha ISO completa (con hora) en formato corto legible en español.
 * @param {string} f - Fecha ISO completa
 * @returns {string}
 */
function formatFechaCorta(f) {
  if (!f) return ''
  try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) } catch { return f }
}

/**
 * Calcula la cantidad de noches entre check-in y check-out.
 * @param {string} ci - Fecha de check-in (YYYY-MM-DD)
 * @param {string} co - Fecha de check-out (YYYY-MM-DD)
 * @returns {number}
 */
function calcNoches(ci, co) {
  if (!ci || !co) return 0
  return Math.max(0, Math.ceil((new Date(co) - new Date(ci)) / 86400000))
}

/** Reinicia todos los filtros a sus valores predeterminados. */
function resetFiltros() {
  filtros.value = { precioMin: 0, precioMax: 9999, tipos: [], hoteles: [], proveedores: [], amenidades: [] }
}

/**
 * Construye el payload para el endpoint de detalle de reserva de hotel.
 * Soporta reservas normales, combos de habitaciones y habitaciones con persona extra.
 * @param {number} reservaId - ID de la reservación recién creada
 * @param {object} itemData - Datos de la habitación/combo seleccionado
 * @returns {{reservacionId: number, proveedorId: number, habitaciones: object[]}|null}
 */
function buildHotelPayload(reservaId, itemData) {
  const b = itemData.busqueda
  let habitaciones = []

  if (itemData.esCombo) {
    const contadorCombo = {}
    habitaciones = (itemData.habs || [])
      .map(h => {
        const disponibles = h.habitacionesDisponibles || []
        if (!disponibles.length) return null
        const key = `${h.cap}-${h.tipo}`
        const idx = contadorCombo[key] ?? 0
        contadorCombo[key] = idx + 1
        if (idx >= disponibles.length) return null
        return {
          habitacionId:     disponibles[idx].id,
          fechaCheckIn:     b.checkIn,
          fechaCheckOut:    b.checkOut,
          cantidadPersonas: h.cap,
          precioPorNoche:   h.precio,
        }
      })
      .filter(h => h !== null)
  } else {
    const rooms = itemData.habitacionesDisponibles || []
    if (!rooms.length) return null
    const precioVisible = itemData.esExtra ? itemData.total : itemData.precioPorNoche
    habitaciones = [{
      habitacionId:     rooms[0].id,
      fechaCheckIn:     b.checkIn,
      fechaCheckOut:    b.checkOut,
      cantidadPersonas: b.cantidadPersonas,
      precioPorNoche:   precioVisible,
    }]
  }

  if (!habitaciones.length) return null
  return { reservacionId: reservaId, proveedorId: itemData.proveedorId, habitaciones }
}

/**
 * Pre-crea la reservación en background al momento de seleccionar una habitación.
 * Primero crea el encabezado de reserva, luego agrega el detalle de hotel.
 * El resultado se asigna a `window.__reservaPromise` para ser consumido en la vista de pago.
 * @param {object} itemData - Datos del hotel/habitación seleccionado
 * @returns {Promise<{reserva: object, detalle: object|null, segundos: number, expiresAt: number}|null>}
 */
async function precrearReservacionHotel(itemData) {
  try {
    const res1 = await fetch(`${API}/api/reservaciones`, {
      method: 'POST', credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tipo_reserva_id: 2 }),
    })
    if (!res1.ok) return null
    const reserva = await res1.json()

    const payload = buildHotelPayload(reserva.id, itemData)
    let detalle   = null

    if (payload) {
      const res2 = await fetch(`${API}/api/reservaciones/detalle/hotel`, {
        method: 'POST', credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (res2.ok) detalle = await res2.json()
    }

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

/**
 * Guarda la habitación seleccionada en sessionStorage, inicia la pre-creación
 * de reserva en background y navega a la vista de confirmación.
 * @param {object} hab - Objeto habitación de la lista filtrada
 * @param {object} grupo - Grupo de hotel al que pertenece la habitación
 */
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
  _persistirBusquedaHotel()
  window.__reservaPromise = precrearReservacionHotel(itemData)
  router.push('/reservar')
}

/**
 * Inicia la reserva de una habitación con persona extra (capacidad exacta - 1 + cargo adicional).
 * @param {object} extraInfo - Datos del combo persona extra devueltos por _getPersonaExtraMin
 * @param {object} grupo - Grupo de hotel
 */
function reservarExtra(extraInfo, grupo) {
  sessionStorage.removeItem('vuelo_seleccionado')
  sessionStorage.removeItem('paquete_seleccionado')

  const itemData = {
    esExtra:         true,
    tipo:            extraInfo.tipo,
    precioPorNoche:  extraInfo.precioPorNoche,
    precioPorPersona: extraInfo.precioPorPersona,
    total:           extraInfo.total,
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
  _persistirBusquedaHotel()
  window.__reservaPromise = precrearReservacionHotel(itemData)
  router.push('/reservar')
}

/**
 * Inicia la reserva de un combo de habitaciones (exacto o aproximado).
 * @param {object} comboInfo - Datos del combo devueltos por _getComboHabs o _getComboAproximado
 * @param {object} grupo - Grupo de hotel
 */
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
  _persistirBusquedaHotel()
  window.__reservaPromise = precrearReservacionHotel(itemData)
  router.push('/reservar')
}

/** Guarda la búsqueda y resultados en sessionStorage antes de navegar a /reservar. */
function _persistirBusquedaHotel() {
  sessionStorage.setItem('_rh_busqueda', JSON.stringify(busqueda.value))
  try {
    sessionStorage.setItem('_rh_habitaciones', JSON.stringify(
      todasLasHabitaciones.value.map(({ proveedorImagen, ...r }) => r)
    ))
  } catch {}
}

/**
 * Al montar: procesa los resultados crudos recibidos desde history.state,
 * inicializa el IntersectionObserver y comienza a observar los grupos de hotel visibles.
 */
onMounted(() => {
  if (!busqueda.value.ciudad) { error.value = 'Faltan datos de búsqueda.'; loading.value = false; return }
  if (resultadosRaw && Array.isArray(resultadosRaw) && resultadosRaw.length > 0) {
    todasLasHabitaciones.value = mapearRespuesta(resultadosRaw)
    if (todasLasHabitaciones.value.length === 0) error.value = 'No hay habitaciones disponibles para los criterios seleccionados.'
  } else if (_savedRhHabitaciones?.length) {
    // Fallback: si Vue Router limpió history.state al navegar a /reservar, restaurar desde sessionStorage
    todasLasHabitaciones.value = _savedRhHabitaciones
  } else {
    error.value = 'No hay resultados. Modifica la búsqueda.'
  }
  loading.value = false

  // Inicializar observer y comenzar a observar grupos visibles
  initHotelObserver()
  observarGrupos()
  window.addEventListener('keydown', _onKeydown)
})
onUnmounted(() => { window.removeEventListener('keydown', _onKeydown) })
</script>