<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- Barra lateral -->
        <aside class="adm-sidebar">
          <div class="adm-sidebar__head">
            <div class="adm-sidebar__logo">
              <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="20" height="20">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
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
            <router-link to="/admin/metricas" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              Métricas
            </router-link>
          </nav>
        </aside>

        <!-- Área principal -->
        <div class="adm-main">

          <!-- Topbar -->
          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Métricas</h1>
              <p class="adm-topbar__sub">Análisis y reportes de actividad</p>
            </div>
            <div class="met-topbar-actions">
              <!-- Accesos rápidos de período -->
              <div class="met-periodos">
                <button v-for="p in periodos" :key="p.key"
                  class="met-periodo-btn"
                  :class="{ 'met-periodo-btn--active': periodoActivo === p.key }"
                  @click="setPeriodo(p)">
                  {{ p.label }}
                </button>
              </div>
              <!-- Selectores de fecha personalizados -->
              <div class="met-fechas">
                <input type="date" v-model="fechaDesde" class="met-date-input" @change="cargarTodo" />
                <span class="met-fecha-sep">→</span>
                <input type="date" v-model="fechaHasta" class="met-date-input" @change="cargarTodo" />
              </div>
              <!-- Botón exportar -->
              <button class="met-btn-export" @click="mostrarModalExportar = true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                Exportar
              </button>
            </div>
          </div>

          <!-- Spinner global -->
          <div v-if="loadingResumen && loadingNegocio" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando métricas...</p>
          </div>

          <!-- Error de conexión -->
          <div v-else-if="errorResumen && errorNegocio" class="met-error-banner">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <div>
              <strong>No se pudieron cargar las métricas.</strong>
              <span>{{ errorDetalle || 'Verifica que el servidor esté corriendo y tengas sesión de administrador.' }}</span>
            </div>
            <button class="met-btn-retry" @click="cargarTodo">Reintentar</button>
          </div>

          <template v-else>

            <!-- ═══ PANEL 1: KPIs ═══════════════════════════════════════════ -->
            <div class="adm-kpis">
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="20" height="20"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Total Búsquedas</p>
                  <p class="adm-kpi__val">{{ resumen.kpi?.totalBusquedas ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--yellow">
                  <svg viewBox="0 0 24 24" fill="#1C1A18" width="18" height="18"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Búsquedas Vuelo</p>
                  <p class="adm-kpi__val">{{ resumen.kpi?.busquedasVuelo ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--black">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="18" height="18"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Búsquedas Hotel</p>
                  <p class="adm-kpi__val">{{ resumen.kpi?.busquedasHotel ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="20" height="20"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Reservaciones</p>
                  <p class="adm-kpi__val">{{ resumen.kpi?.totalReservaciones ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--green">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="18" height="18"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Ingresos Totales</p>
                  <p class="adm-kpi__val">${{ fmt(resumen.kpi?.ingresosTotales) }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--yellow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#1C1A18" stroke-width="2" width="18" height="18"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Ganancia MOVENT</p>
                  <p class="adm-kpi__val">${{ fmt(resumen.kpi?.gananciaMovent) }}</p>
                </div>
              </div>
            </div>

            <!-- ═══ PANEL 2: Gráficas de búsquedas ════════════════════════════ -->
            <div class="met-grid-2">

              <!-- Búsquedas por día (línea SVG) -->
              <div class="met-card">
                <p class="met-card__titulo">Búsquedas por Día</p>
                <template v-if="resumen.busquedasPorDia?.length">
                  <svg :viewBox="`0 0 560 170`" class="met-svg">
                    <defs>
                      <linearGradient id="gradBusc" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stop-color="#D4AF37" stop-opacity="0.28"/>
                        <stop offset="100%" stop-color="#D4AF37" stop-opacity="0"/>
                      </linearGradient>
                    </defs>
                    <!-- grid -->
                    <template v-for="pct in [0,0.25,0.5,0.75,1]" :key="pct">
                      <line :x1="36" :y1="142 - pct*110" :x2="548" :y2="142 - pct*110" stroke="#EBE6E0" stroke-width="1"/>
                      <text :x="32" :y="145 - pct*110" text-anchor="end" font-size="9" fill="#9a9089">
                        {{ Math.round(maxBusDia * pct) }}
                      </text>
                    </template>
                    <!-- área + línea -->
                    <polygon :points="areaPointsBusDia" fill="url(#gradBusc)"/>
                    <polyline :points="linePointsBusDia" fill="none" stroke="#D4AF37" stroke-width="2"/>
                    <!-- puntos -->
                    <template v-for="(pt, i) in busquedaDiaPts" :key="i">
                      <circle :cx="pt.x" :cy="pt.y" r="3" fill="#D4AF37"/>
                    </template>
                    <!-- etiquetas eje X (cada 5) -->
                    <template v-for="(b, i) in resumen.busquedasPorDia" :key="i">
                      <template v-if="i % Math.ceil(resumen.busquedasPorDia.length / 7) === 0">
                        <text :x="busquedaDiaPts[i]?.x" y="160" text-anchor="middle" font-size="8" fill="#9a9089">
                          {{ b.fecha?.slice(5) }}
                        </text>
                      </template>
                    </template>
                  </svg>
                </template>
                <p v-else class="met-vacio">Sin datos en este período</p>
              </div>

              <!-- Búsquedas por tipo (barras horizontales) -->
              <div class="met-card">
                <p class="met-card__titulo">Búsquedas por Tipo</p>
                <div class="met-barras-h" v-if="resumen.busquedasPorTipo?.length">
                  <div v-for="bt in resumen.busquedasPorTipo" :key="bt.tipo" class="met-barra-h-row">
                    <span class="met-barra-h-lbl">{{ bt.tipo }}</span>
                    <div class="met-barra-h-track">
                      <div class="met-barra-h-fill"
                        :style="{
                          width: maxBusTipo > 0 ? (bt.total/maxBusTipo*100) + '%' : '0%',
                          background: bt.tipo === 'Vuelo' ? '#D4AF37' : '#3B82F6'
                        }">
                      </div>
                    </div>
                    <span class="met-barra-h-val">{{ bt.total }}</span>
                  </div>
                </div>
                <p v-else class="met-vacio">Sin datos</p>
              </div>

            </div>

            <!-- ═══ PANEL 3: Destinos + Reservaciones por tipo ════════════════ -->
            <div class="met-grid-2">

              <!-- Top destinos -->
              <div class="met-card">
                <p class="met-card__titulo">Destinos más Buscados</p>
                <div class="met-barras-h" v-if="resumen.destinosPopulares?.length">
                  <div v-for="dest in resumen.destinosPopulares" :key="dest.ciudad + dest.pais" class="met-barra-h-row">
                    <span class="met-barra-h-lbl met-barra-h-lbl--lg">
                      {{ dest.ciudad }}<span class="met-pais">, {{ dest.pais }}</span>
                    </span>
                    <div class="met-barra-h-track">
                      <div class="met-barra-h-fill"
                        :style="{ width: maxDestino > 0 ? (dest.total/maxDestino*100) + '%' : '0%' }">
                      </div>
                    </div>
                    <span class="met-barra-h-val">{{ dest.total }}</span>
                  </div>
                </div>
                <p v-else class="met-vacio">Sin datos</p>
              </div>

              <!-- Reservaciones por tipo (donut SVG) -->
              <div class="met-card met-card--center">
                <p class="met-card__titulo">Reservaciones por Tipo</p>
                <template v-if="resumen.reservacionesPorTipo?.length">
                  <div class="met-donut-wrap">
                    <svg viewBox="0 0 160 160" class="met-donut-svg">
                      <template v-for="(seg, i) in donutSegments" :key="i">
                        <circle cx="80" cy="80" r="60"
                          fill="none"
                          :stroke="seg.color"
                          stroke-width="28"
                          :stroke-dasharray="seg.dash"
                          :stroke-dashoffset="seg.offset"
                          transform="rotate(-90 80 80)"/>
                      </template>
                      <text x="80" y="76" text-anchor="middle" font-size="18" font-weight="bold" fill="#1C1A18">
                        {{ totalReservaciones }}
                      </text>
                      <text x="80" y="90" text-anchor="middle" font-size="9" fill="#9a9089">reservaciones</text>
                    </svg>
                    <div class="met-donut-leyenda">
                      <div v-for="(rt, i) in resumen.reservacionesPorTipo" :key="rt.tipo" class="met-donut-item">
                        <span class="met-donut-punto" :style="{ background: donutColors[i] }"></span>
                        <span class="met-donut-lbl">{{ rt.tipo }}</span>
                        <span class="met-donut-val">{{ rt.total }}</span>
                      </div>
                    </div>
                  </div>
                </template>
                <p v-else class="met-vacio">Sin reservaciones en el período</p>
              </div>

            </div>

            <!-- ═══ PANEL 4: Análisis de Negocio ══════════════════════════════ -->
            <template v-if="!loadingNegocio">

              <!-- Embudo de conversión -->
              <div class="met-card met-card--wide" v-if="negocio?.embudo">
                <p class="met-card__titulo">Embudo de Conversión</p>
                <div class="met-embudo">
                  <div v-for="etapa in embudoEtapas" :key="etapa.label" class="met-embudo-fila">
                    <span class="met-embudo-lbl">{{ etapa.label }}</span>
                    <div class="met-embudo-barra-track">
                      <div class="met-embudo-barra"
                        :style="{
                          width: negocio?.embudo?.busquedas > 0
                            ? (etapa.val / negocio.embudo.busquedas * 100) + '%'
                            : '0%',
                          background: etapa.color
                        }">
                      </div>
                    </div>
                    <span class="met-embudo-val">{{ etapa.val }}</span>
                    <span class="met-embudo-pct">
                      {{ negocio?.embudo?.busquedas > 0
                          ? ((etapa.val / negocio.embudo.busquedas) * 100).toFixed(1) + '%'
                          : '—' }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="met-card met-card--wide" v-else>
                <p class="met-card__titulo">Embudo de Conversión</p>
                <p class="met-vacio">Sin datos en este período</p>
              </div>

              <!-- Proveedores + Cancelaciones -->
              <div class="met-grid-2">

                <!-- Top proveedores -->
                <div class="met-card">
                  <p class="met-card__titulo">Rendimiento de Proveedores</p>
                  <div class="met-tabla-scroll" v-if="negocio?.proveedores?.length">
                    <table class="met-tabla">
                      <thead>
                        <tr>
                          <th>Proveedor</th>
                          <th>Tipo</th>
                          <th class="met-num">Reserv.</th>
                          <th class="met-num">Ingresos</th>
                          <th class="met-num">Ganancia</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="p in (negocio?.proveedores ?? [])" :key="p.nombre">
                          <td>{{ p.nombre }}</td>
                          <td>
                            <span :class="p.tipoId === 1 ? 'met-badge met-badge--vuelo' : 'met-badge met-badge--hotel'">
                              {{ p.tipo }}
                            </span>
                          </td>
                          <td class="met-num">{{ p.reservaciones }}</td>
                          <td class="met-num">${{ fmt(p.ingresos) }}</td>
                          <td class="met-num met-ganancia">${{ fmt(p.ganancia) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <p v-else class="met-vacio">Sin datos</p>
                </div>

                <!-- Cancelaciones por tipo -->
                <div class="met-card">
                  <p class="met-card__titulo">Cancelaciones por Tipo</p>
                  <div class="met-barras-h" v-if="negocio?.cancelaciones?.length">
                    <div v-for="ct in (negocio?.cancelaciones ?? [])" :key="ct.tipo" class="met-barra-h-row">
                      <span class="met-barra-h-lbl">{{ ct.tipo }}</span>
                      <div class="met-barra-h-track">
                        <div class="met-barra-h-fill met-barra-h-fill--rojo"
                          :style="{ width: maxCancelaciones > 0 ? (ct.total/maxCancelaciones*100) + '%' : '0%' }">
                        </div>
                      </div>
                      <span class="met-barra-h-val">{{ ct.total }}</span>
                    </div>
                  </div>
                  <p v-else class="met-vacio">Sin cancelaciones en el período</p>
                </div>

              </div>

              <!-- Tendencia de ingresos mensual (línea SVG multi-serie) -->
              <div class="met-card met-card--wide" v-if="negocio?.tendencia?.length">
                <p class="met-card__titulo">Tendencia de Ingresos Mensuales</p>
                <svg viewBox="0 0 700 180" class="met-svg">
                  <defs>
                    <linearGradient id="gradVuelo" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stop-color="#D4AF37" stop-opacity="0.2"/>
                      <stop offset="100%" stop-color="#D4AF37" stop-opacity="0"/>
                    </linearGradient>
                    <linearGradient id="gradHotel" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.2"/>
                      <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
                    </linearGradient>
                  </defs>
                  <template v-for="pct in [0,0.25,0.5,0.75,1]" :key="pct">
                    <line :x1="48" :y1="148 - pct*120" :x2="684" :y2="148 - pct*120" stroke="#EBE6E0" stroke-width="1"/>
                    <text :x="44" :y="151 - pct*120" text-anchor="end" font-size="9" fill="#9a9089">
                      {{ Math.round(maxTendencia * pct) }}
                    </text>
                  </template>
                  <!-- Vuelo -->
                  <polygon v-if="tendenciaVueloPts.length > 1" :points="tendenciaAreaVuelo" fill="url(#gradVuelo)"/>
                  <polyline v-if="tendenciaVueloPts.length > 1" :points="tendenciaLineVuelo" fill="none" stroke="#D4AF37" stroke-width="2"/>
                  <!-- Hotel -->
                  <polygon v-if="tendenciaHotelPts.length > 1" :points="tendenciaAreaHotel" fill="url(#gradHotel)"/>
                  <polyline v-if="tendenciaHotelPts.length > 1" :points="tendenciaLineHotel" fill="none" stroke="#3B82F6" stroke-width="2"/>
                  <!-- Etiquetas eje X -->
                  <template v-for="(mes, i) in mesesUnicos" :key="mes">
                    <text :x="tendenciaX(i)" y="165" text-anchor="middle" font-size="8" fill="#9a9089">{{ mes }}</text>
                  </template>
                  <!-- Leyenda -->
                  <circle cx="56" cy="12" r="4" fill="#D4AF37"/>
                  <text x="64" y="16" font-size="9" fill="#5a5047">Vuelo</text>
                  <circle cx="105" cy="12" r="4" fill="#3B82F6"/>
                  <text x="113" y="16" font-size="9" fill="#5a5047">Hotel</text>
                  <circle cx="150" cy="12" r="4" fill="#7C3AED"/>
                  <text x="158" y="16" font-size="9" fill="#5a5047">Paquete</text>
                </svg>
              </div>

              <!-- Heatmap búsquedas por hora y día -->
              <div class="met-card met-card--wide" v-if="negocio?.heatmap?.length">
                <p class="met-card__titulo">Mapa de Calor — Búsquedas por Hora y Día</p>
                <div class="met-heatmap-scroll">
                  <div class="met-heatmap">
                    <div class="met-heatmap-col met-heatmap-col--lbl">
                      <div class="met-heatmap-lbl-top"></div>
                      <div v-for="dia in diasSemana" :key="dia" class="met-heatmap-cell met-heatmap-cell--lbl">{{ dia }}</div>
                    </div>
                    <div v-for="hora in horas" :key="hora" class="met-heatmap-col">
                      <div class="met-heatmap-lbl-top">{{ hora }}h</div>
                      <div v-for="dia in diasSemana" :key="dia" class="met-heatmap-cell"
                        :style="{ opacity: heatmapOpacity(dia, hora) }"
                        :title="`${dia} ${hora}h: ${heatmapVal(dia, hora)}`">
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </template>

            <!-- ═══ PANEL 5: Listado paginado de búsquedas ════════════════════ -->
            <div class="met-card met-card--wide">
              <div class="met-listado-header">
                <p class="met-card__titulo met-card__titulo--inline">Registro de Búsquedas</p>
                <div class="met-listado-filtros">
                  <select v-model="filtroTipo" @change="cargarListado(1)" class="met-select">
                    <option value="">Todos los tipos</option>
                    <option value="Vuelo">Vuelo</option>
                    <option value="Hotel">Hotel</option>
                  </select>
                  <input v-model="filtroUsuario" @input="debounceListado"
                    placeholder="Buscar usuario..." class="met-input" />
                </div>
              </div>

              <div v-if="loadingListado" class="met-listado-loading">
                <div class="adm-spinner adm-spinner--sm"></div>
              </div>
              <template v-else-if="listado.registros?.length">
                <div class="met-tabla-scroll">
                  <table class="met-tabla">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Fecha</th>
                        <th>Tipo</th>
                        <th>Usuario</th>
                        <th>Origen</th>
                        <th>Destino</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="r in listado.registros" :key="r.id">
                        <td class="met-muted">{{ r.id }}</td>
                        <td>{{ formatFecha(r.fecha) }}</td>
                        <td>
                          <span :class="r.tipo === 'Vuelo' ? 'met-badge met-badge--vuelo' : 'met-badge met-badge--hotel'">
                            {{ r.tipo }}
                          </span>
                        </td>
                        <td>{{ r.usuario }}</td>
                        <td class="met-muted">{{ r.ciudadOrigen || '—' }}</td>
                        <td>{{ r.ciudadDestino }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <!-- Paginación -->
                <div class="met-paginacion">
                  <span class="met-paginacion-info">
                    {{ listado.totalRegistros }} registros — Página {{ listado.paginaActual }} / {{ listado.totalPaginas }}
                  </span>
                  <div class="met-paginacion-btns">
                    <button class="met-pag-btn" :disabled="listado.paginaActual <= 1" @click="cargarListado(listado.paginaActual - 1)">
                      ← Ant
                    </button>
                    <template v-for="n in paginasVisibles" :key="n">
                      <button class="met-pag-btn" :class="{ 'met-pag-btn--active': n === listado.paginaActual }"
                        @click="cargarListado(n)">{{ n }}</button>
                    </template>
                    <button class="met-pag-btn" :disabled="listado.paginaActual >= listado.totalPaginas" @click="cargarListado(listado.paginaActual + 1)">
                      Sig →
                    </button>
                  </div>
                </div>
              </template>
              <p v-else class="met-vacio">Sin búsquedas en el período</p>
            </div>

          </template>
        </div><!-- adm-main -->
      </div><!-- adm-layout -->
    </div><!-- adm-page -->

    <!-- ═══ MODAL DE EXPORTACIÓN ═══════════════════════════════════════════════ -->
    <div v-if="mostrarModalExportar" class="met-modal-backdrop" @click.self="mostrarModalExportar = false">
      <div class="met-modal">
        <div class="met-modal-header">
          <p class="met-modal-titulo">Exportar Reporte</p>
          <button class="met-modal-close" @click="mostrarModalExportar = false">✕</button>
        </div>

        <div class="met-modal-body">
          <!-- Formato -->
          <div class="met-form-group">
            <label class="met-label">Formato de exportación</label>
            <div class="met-formato-btns">
              <button v-for="f in formatos" :key="f.key"
                :class="['met-formato-btn', { 'met-formato-btn--active': expFormato === f.key }]"
                @click="expFormato = f.key">
                <span class="met-formato-icon">{{ f.icon }}</span>
                {{ f.label }}
              </button>
            </div>
          </div>

          <!-- Destino: descarga o email -->
          <div class="met-form-group">
            <label class="met-label">Enviar a</label>
            <div class="met-radio-group">
              <label class="met-radio">
                <input type="radio" v-model="expDestino" value="descarga" />
                Descargar archivo
              </label>
              <label class="met-radio">
                <input type="radio" v-model="expDestino" value="correo" />
                Enviar por correo
              </label>
            </div>
          </div>

          <!-- Correos (si aplica) -->
          <div v-if="expDestino === 'correo'" class="met-form-group">
            <label class="met-label">Correos destinatarios</label>
            <div v-for="(correo, i) in expCorreos" :key="i" class="met-correo-row">
              <input v-model="expCorreos[i]" type="email" placeholder="correo@ejemplo.com" class="met-input met-input--full" />
              <button v-if="expCorreos.length > 1" class="met-btn-del" @click="expCorreos.splice(i, 1)">✕</button>
            </div>
            <button class="met-btn-add" @click="expCorreos.push('')">+ Agregar correo</button>
          </div>

          <div v-if="expError" class="met-error-msg">{{ expError }}</div>
        </div>

        <div class="met-modal-footer">
          <button class="met-btn-cancel" @click="mostrarModalExportar = false">Cancelar</button>
          <button class="met-btn-primary" :disabled="exportando" @click="handleExportar">
            <span v-if="exportando">
              <span class="adm-spinner adm-spinner--xs"></span>
              Generando...
            </span>
            <span v-else>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              {{ expDestino === 'correo' ? 'Enviar' : 'Descargar' }}
            </span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'

const API = import.meta.env.VITE_API_URL || ''

// ── Estado ───────────────────────────────────────────────────────────────────
const resumen       = ref(null)
const negocio       = ref(null)
const listado       = ref({ registros: [], totalRegistros: 0, totalPaginas: 1, paginaActual: 1 })

const loadingResumen = ref(true)
const loadingNegocio = ref(true)
const loadingListado = ref(false)
const errorResumen   = ref(false)
const errorNegocio   = ref(false)
const errorDetalle   = ref('')

const fechaHasta   = ref(hoy())
const fechaDesde   = ref(hace1Anio())
const periodoActivo = ref('yr')

const filtroTipo    = ref('')
const filtroUsuario = ref('')
let debounceTimer   = null

const mostrarModalExportar = ref(false)
const expFormato   = ref('excel')
const expDestino   = ref('descarga')
const expCorreos   = ref([''])
const expError     = ref('')
const exportando   = ref(false)

// ── Constantes ───────────────────────────────────────────────────────────────
const periodos = [
  { key: '7d',   label: '7 días',  dias: 7   },
  { key: '30d',  label: '30 días', dias: 30  },
  { key: '90d',  label: '3 meses', dias: 90  },
  { key: 'yr',   label: '1 año',   dias: 365 },
  { key: 'todo', label: 'Todo',    dias: null },
]
const formatos = [
  { key: 'excel', label: 'Excel',  icon: '📊' },
  { key: 'csv',   label: 'CSV',    icon: '📄' },
  { key: 'pdf',   label: 'PDF',    icon: '📑' },
]
const donutColors = ['#D4AF37', '#3B82F6', '#7C3AED', '#10B981']
const diasSemana  = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb']
const horas       = [0,2,4,6,8,10,12,14,16,18,20,22]

// ── Helpers de fecha ─────────────────────────────────────────────────────────
function hoy() {
  return new Date().toISOString().slice(0, 10)
}
function hace1Anio() {
  const d = new Date()
  d.setFullYear(d.getFullYear() - 1)
  return d.toISOString().slice(0, 10)
}
function formatFecha(f) {
  if (!f) return '—'
  return f.slice(0, 16).replace('T', ' ')
}
function fmt(v) {
  if (v === undefined || v === null) return '0.00'
  return Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ── Carga de datos ────────────────────────────────────────────────────────────
async function cargarResumen() {
  loadingResumen.value = true
  errorResumen.value = false
  try {
    const r = await fetch(`${API}/api/admin/metricas/resumen?fechaDesde=${fechaDesde.value}&fechaHasta=${fechaHasta.value}`, { credentials: 'include' })
    if (r.ok) {
      resumen.value = await r.json()
    } else {
      errorResumen.value = true
      errorDetalle.value = `HTTP ${r.status} — ${r.status === 401 ? 'Sesión expirada o no iniciada' : r.status === 403 ? 'Sin permisos de administrador' : r.status === 404 ? 'Ruta no encontrada (reinicia el servidor Go)' : 'Error del servidor'}`
      resumen.value = null
    }
  } catch (e) {
    errorResumen.value = true
    errorDetalle.value = `Error de red — verifica que el servidor Go esté corriendo`
    resumen.value = null
  }
  finally { loadingResumen.value = false }
}

async function cargarNegocio() {
  loadingNegocio.value = true
  errorNegocio.value = false
  try {
    const r = await fetch(`${API}/api/admin/metricas/negocio?fechaDesde=${fechaDesde.value}&fechaHasta=${fechaHasta.value}`, { credentials: 'include' })
    if (r.ok) {
      negocio.value = await r.json()
    } else {
      errorNegocio.value = true
      negocio.value = null
    }
  } catch {
    errorNegocio.value = true
    negocio.value = null
  }
  finally { loadingNegocio.value = false }
}

async function cargarListado(pagina = 1) {
  loadingListado.value = true
  try {
    const r = await fetch(`${API}/api/admin/metricas/listado`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fechaDesde: fechaDesde.value,
        fechaHasta: fechaHasta.value,
        tipo:       filtroTipo.value,
        usuario:    filtroUsuario.value,
        pagina,
        tamanoPagina: 20,
      }),
    })
    if (r.ok) listado.value = await r.json()
  } catch {}
  finally { loadingListado.value = false }
}

function cargarTodo() {
  periodoActivo.value = ''
  cargarResumen()
  cargarNegocio()
  cargarListado(1)
}

function setPeriodo(p) {
  periodoActivo.value = p.key
  if (p.key === 'todo') {
    fechaDesde.value = '2020-01-01'
    fechaHasta.value = hoy()
  } else {
    const d = new Date()
    fechaHasta.value = d.toISOString().slice(0, 10)
    d.setDate(d.getDate() - (p.dias - 1))
    fechaDesde.value = d.toISOString().slice(0, 10)
  }
  cargarResumen()
  cargarNegocio()
  cargarListado(1)
}

function debounceListado() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => cargarListado(1), 500)
}

onMounted(() => {
  cargarResumen()
  cargarNegocio()
  cargarListado(1)
})

// ── Computed para gráficas ────────────────────────────────────────────────────
const maxBusDia = computed(() => {
  const datos = resumen.value?.busquedasPorDia ?? []
  return Math.max(...datos.map(d => d.total), 1)
})

const W = 560, H = 170, PAD = { l: 36, r: 12, t: 16, b: 28 }
const gW = W - PAD.l - PAD.r, gH = H - PAD.t - PAD.b

const busquedaDiaPts = computed(() => {
  const datos = resumen.value?.busquedasPorDia ?? []
  if (!datos.length) return []
  return datos.map((d, i) => ({
    x: PAD.l + (i / Math.max(datos.length - 1, 1)) * gW,
    y: PAD.t + gH - (d.total / maxBusDia.value) * gH,
  }))
})

const linePointsBusDia = computed(() =>
  busquedaDiaPts.value.map(p => `${p.x},${p.y}`).join(' ')
)

const areaPointsBusDia = computed(() => {
  const pts = busquedaDiaPts.value
  if (!pts.length) return ''
  const line = pts.map(p => `${p.x},${p.y}`).join(' ')
  const last = pts[pts.length - 1]
  const first = pts[0]
  return `${line} ${last.x},${PAD.t + gH} ${first.x},${PAD.t + gH}`
})

const maxBusTipo = computed(() => Math.max(...(resumen.value?.busquedasPorTipo ?? []).map(b => b.total), 1))
const maxDestino = computed(() => Math.max(...(resumen.value?.destinosPopulares ?? []).map(d => d.total), 1))
const maxCancelaciones = computed(() => Math.max(...(negocio.value?.cancelaciones ?? []).map(c => c.total), 1))

// Donut
const totalReservaciones = computed(() =>
  (resumen.value?.reservacionesPorTipo ?? []).reduce((s, r) => s + r.total, 0)
)
const donutSegments = computed(() => {
  const items = resumen.value?.reservacionesPorTipo ?? []
  const total = totalReservaciones.value
  const circ = 2 * Math.PI * 60
  let offset = 0
  return items.map((rt, i) => {
    const pct = total > 0 ? rt.total / total : 0
    const dash = `${pct * circ} ${circ}`
    const seg = { color: donutColors[i] ?? '#ccc', dash, offset: -offset * circ }
    offset += pct
    return seg
  })
})

// Embudo
const embudoEtapas = computed(() => {
  const e = negocio.value?.embudo
  if (!e) return []
  return [
    { label: 'Búsquedas',       val: e.busquedas,     color: '#D4AF37' },
    { label: 'Reservaciones',   val: e.reservaciones,  color: '#3B82F6' },
    { label: 'Activas',         val: e.activas,        color: '#10B981' },
    { label: 'Completadas',     val: e.completadas,    color: '#059669' },
    { label: 'Canceladas',      val: e.canceladas,     color: '#DC2626' },
    { label: 'Expiradas',       val: e.expiradas,      color: '#9CA3AF' },
    { label: 'Pendientes',      val: e.pendientes,     color: '#F59E0B' },
  ]
})

// Tendencia mensual
const mesesUnicos = computed(() => {
  const meses = [...new Set((negocio.value?.tendencia ?? []).map(t => t.mes))]
  return meses.sort()
})

const maxTendencia = computed(() => {
  const data = negocio.value?.tendencia ?? []
  return Math.max(...data.map(t => t.ingresos), 1)
})

const TW = 700, TPad = { l: 48, r: 16, t: 20, b: 32 }
const TgW = TW - TPad.l - TPad.r, TgH = 180 - TPad.t - TPad.b

function tendenciaX(idx) {
  const n = Math.max(mesesUnicos.value.length - 1, 1)
  return TPad.l + (idx / n) * TgW
}

function tendenciaPts(tipoNombre) {
  const meses = mesesUnicos.value
  const data = negocio.value?.tendencia ?? []
  return meses.map((mes, i) => {
    const entry = data.find(t => t.mes === mes && t.tipoNombre === tipoNombre)
    const val = entry ? entry.ingresos : 0
    return {
      x: tendenciaX(i),
      y: TPad.t + TgH - (val / maxTendencia.value) * TgH,
    }
  })
}

const tendenciaVueloPts = computed(() => tendenciaPts('Vuelo'))
const tendenciaHotelPts = computed(() => tendenciaPts('Hotel'))

function ptsToLine(pts) {
  return pts.map(p => `${p.x},${p.y}`).join(' ')
}
function ptsToArea(pts) {
  if (pts.length < 2) return ''
  const line = ptsToLine(pts)
  const last = pts[pts.length - 1], first = pts[0]
  return `${line} ${last.x},${TPad.t + TgH} ${first.x},${TPad.t + TgH}`
}

const tendenciaLineVuelo  = computed(() => ptsToLine(tendenciaVueloPts.value))
const tendenciaAreaVuelo  = computed(() => ptsToArea(tendenciaVueloPts.value))
const tendenciaLineHotel  = computed(() => ptsToLine(tendenciaHotelPts.value))
const tendenciaAreaHotel  = computed(() => ptsToArea(tendenciaHotelPts.value))

// Heatmap
function heatmapVal(dia, hora) {
  const mapDia = { Dom: 1, Lun: 2, Mar: 3, 'Mié': 4, Jue: 5, Vie: 6, Sáb: 7 }
  const cell = (negocio.value?.heatmap ?? []).find(
    h => h.diaSemana === mapDia[dia] && h.hora === hora
  )
  return cell ? cell.total : 0
}
function heatmapOpacity(dia, hora) {
  const maxH = Math.max(...(negocio.value?.heatmap ?? []).map(h => h.total), 1)
  const v = heatmapVal(dia, hora)
  return v > 0 ? 0.15 + (v / maxH) * 0.85 : 0.05
}

// Paginación
const paginasVisibles = computed(() => {
  const total = listado.value.totalPaginas ?? 1
  const actual = listado.value.paginaActual ?? 1
  const range = []
  const start = Math.max(1, actual - 2)
  const end   = Math.min(total, actual + 2)
  for (let i = start; i <= end; i++) range.push(i)
  return range
})

// ── Exportación ───────────────────────────────────────────────────────────────
async function handleExportar() {
  expError.value = ''

  if (expDestino.value === 'correo') {
    const correos = expCorreos.value.filter(c => c.trim())
    if (!correos.length) { expError.value = 'Ingresa al menos un correo.'; return }
  }

  exportando.value = true
  try {
    const body = {
      formato:    expFormato.value,
      fechaDesde: fechaDesde.value,
      fechaHasta: fechaHasta.value,
    }

    if (expDestino.value === 'correo') {
      body.correos = expCorreos.value.filter(c => c.trim())
      const r = await fetch(`${API}/api/admin/metricas/exportar-correo`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      const json = await r.json()
      if (!r.ok && r.status !== 206) throw new Error(json.error || 'Error al enviar')
      mostrarModalExportar.value = false
    } else {
      const r = await fetch(`${API}/api/admin/metricas/exportar-archivo`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!r.ok) throw new Error('Error generando archivo')
      const blob = await r.blob()
      const extMap = { excel: 'xlsx', csv: 'zip', pdf: 'pdf' }
      const ext  = extMap[expFormato.value] || 'bin'
      const url  = URL.createObjectURL(blob)
      const a    = document.createElement('a')
      a.href     = url
      a.download = `metricas_movent_${fechaDesde.value}_${fechaHasta.value}.${ext}`
      a.click()
      URL.revokeObjectURL(url)
      mostrarModalExportar.value = false
    }
  } catch (e) {
    expError.value = e.message || 'Error inesperado'
  } finally {
    exportando.value = false
  }
}
</script>

<style scoped>
/* ── Variables ─────────────────────────────────────────────────────────────── */
:root { --gold: #D4AF37; --dark: #1C1A18; --bg: #F5F2EC; }

/* ── Topbar acciones ────────────────────────────────────────────────────────── */
.met-topbar-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.met-periodos        { display: flex; gap: 4px; }
.met-periodo-btn     { padding: 4px 10px; font-size: 11px; border: 1px solid #ddd6cc; border-radius: 6px; background: #fff; cursor: pointer; color: #5a5047; }
.met-periodo-btn--active { background: #1C1A18; color: #FFCC00; border-color: #1C1A18; }
.met-fechas          { display: flex; align-items: center; gap: 6px; }
.met-date-input      { padding: 4px 8px; font-size: 11px; border: 1px solid #ddd6cc; border-radius: 6px; background: #fff; }
.met-fecha-sep       { color: #9a9089; font-size: 12px; }
.met-btn-export      { display: flex; align-items: center; gap: 6px; padding: 6px 14px; font-size: 12px; font-weight: 600; background: #1C1A18; color: #FFCC00; border: none; border-radius: 8px; cursor: pointer; }
.met-btn-export:hover { background: #2a2826; }

/* ── Cards ──────────────────────────────────────────────────────────────────── */
.met-card { background: #fff; border: 1px solid #ddd6cc; border-radius: 12px; padding: 20px; margin-bottom: 16px; }
.met-card--wide  { width: 100%; }
.met-card--center { display: flex; flex-direction: column; align-items: center; }
.met-card__titulo { font-size: 13px; font-weight: 700; color: #1C1A18; margin: 0 0 14px; letter-spacing: 0.3px; }
.met-card__titulo--inline { margin: 0; }
.met-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.met-vacio { color: #9a9089; font-size: 12px; text-align: center; padding: 20px 0; }

/* ── SVG gráficas ───────────────────────────────────────────────────────────── */
.met-svg { width: 100%; height: auto; }

/* ── Barras horizontales ─────────────────────────────────────────────────────── */
.met-barras-h { display: flex; flex-direction: column; gap: 8px; }
.met-barra-h-row { display: flex; align-items: center; gap: 8px; }
.met-barra-h-lbl { font-size: 11px; color: #5a5047; width: 90px; flex-shrink: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.met-barra-h-lbl--lg { width: 130px; }
.met-pais { color: #9a9089; font-size: 10px; }
.met-barra-h-track { flex: 1; background: #F5F2EC; border-radius: 4px; height: 10px; overflow: hidden; }
.met-barra-h-fill { height: 100%; background: #D4AF37; border-radius: 4px; transition: width 0.5s; }
.met-barra-h-fill--rojo { background: #DC2626; }
.met-barra-h-val { font-size: 11px; font-weight: 600; color: #1C1A18; width: 36px; text-align: right; }

/* ── Donut ───────────────────────────────────────────────────────────────────── */
.met-donut-wrap { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; justify-content: center; }
.met-donut-svg { width: 160px; height: 160px; }
.met-donut-leyenda { display: flex; flex-direction: column; gap: 8px; }
.met-donut-item { display: flex; align-items: center; gap: 7px; font-size: 12px; }
.met-donut-punto { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.met-donut-lbl { color: #5a5047; flex: 1; }
.met-donut-val { font-weight: 700; color: #1C1A18; }

/* ── Embudo ──────────────────────────────────────────────────────────────────── */
.met-embudo { display: flex; flex-direction: column; gap: 7px; }
.met-embudo-fila { display: flex; align-items: center; gap: 10px; }
.met-embudo-lbl { font-size: 12px; color: #5a5047; width: 160px; flex-shrink: 0; }
.met-embudo-barra-track { flex: 1; background: #F5F2EC; border-radius: 4px; height: 14px; overflow: hidden; }
.met-embudo-barra { height: 100%; border-radius: 4px; transition: width 0.5s; }
.met-embudo-val { font-size: 12px; font-weight: 700; color: #1C1A18; width: 42px; text-align: right; }
.met-embudo-pct { font-size: 11px; color: #9a9089; width: 46px; }

/* ── Tabla ───────────────────────────────────────────────────────────────────── */
.met-tabla-scroll { overflow-x: auto; }
.met-tabla { width: 100%; border-collapse: collapse; font-size: 12px; }
.met-tabla th { background: #1C1A18; color: #FFCC00; padding: 8px 10px; text-align: left; font-size: 10px; font-weight: 700; letter-spacing: 0.5px; white-space: nowrap; }
.met-tabla td { padding: 7px 10px; border-bottom: 1px solid #f0ebe3; color: #1C1A18; }
.met-tabla tbody tr:hover { background: #faf8f5; }
.met-num { text-align: right; font-variant-numeric: tabular-nums; }
.met-muted { color: #9a9089; }
.met-ganancia { color: #059669; font-weight: 600; }
.met-badge { display: inline-block; padding: 2px 8px; border-radius: 20px; font-size: 10px; font-weight: 700; }
.met-badge--vuelo  { background: #FFF3CD; color: #92701F; }
.met-badge--hotel  { background: #DBEAFE; color: #1E40AF; }

/* ── Heatmap ─────────────────────────────────────────────────────────────────── */
.met-heatmap-scroll { overflow-x: auto; }
.met-heatmap { display: flex; gap: 2px; }
.met-heatmap-col { display: flex; flex-direction: column; gap: 2px; }
.met-heatmap-col--lbl { }
.met-heatmap-lbl-top { height: 18px; font-size: 9px; color: #9a9089; display: flex; align-items: center; justify-content: center; padding: 0 2px; }
.met-heatmap-cell { width: 26px; height: 22px; background: #D4AF37; border-radius: 3px; }
.met-heatmap-cell--lbl { background: transparent; font-size: 9px; color: #9a9089; display: flex; align-items: center; justify-content: flex-end; padding-right: 4px; }

/* ── Listado ─────────────────────────────────────────────────────────────────── */
.met-listado-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 10px; }
.met-listado-filtros { display: flex; gap: 8px; align-items: center; }
.met-listado-loading { display: flex; justify-content: center; padding: 24px; }
.met-select { padding: 5px 10px; font-size: 12px; border: 1px solid #ddd6cc; border-radius: 6px; background: #fff; }
.met-input { padding: 5px 10px; font-size: 12px; border: 1px solid #ddd6cc; border-radius: 6px; background: #fff; }
.met-input--full { flex: 1; }

/* ── Paginación ──────────────────────────────────────────────────────────────── */
.met-paginacion { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; flex-wrap: wrap; gap: 8px; }
.met-paginacion-info { font-size: 11px; color: #9a9089; }
.met-paginacion-btns { display: flex; gap: 4px; }
.met-pag-btn { padding: 4px 10px; font-size: 11px; border: 1px solid #ddd6cc; border-radius: 6px; background: #fff; cursor: pointer; color: #5a5047; }
.met-pag-btn--active { background: #1C1A18; color: #FFCC00; border-color: #1C1A18; }
.met-pag-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── Modal ───────────────────────────────────────────────────────────────────── */
.met-modal-backdrop { position: fixed; inset: 0; background: rgba(28,26,24,.55); display: flex; align-items: center; justify-content: center; z-index: 9999; }
.met-modal { background: #fff; border-radius: 14px; width: 420px; max-width: 96vw; overflow: hidden; box-shadow: 0 12px 40px rgba(0,0,0,.25); }
.met-modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #1C1A18; }
.met-modal-titulo { font-size: 14px; font-weight: 700; color: #FFCC00; margin: 0; }
.met-modal-close { background: none; border: none; color: #6b6358; font-size: 16px; cursor: pointer; }
.met-modal-body { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.met-modal-footer { padding: 14px 20px; border-top: 1px solid #f0ebe3; display: flex; justify-content: flex-end; gap: 10px; }
.met-form-group { display: flex; flex-direction: column; gap: 6px; }
.met-label { font-size: 12px; font-weight: 600; color: #5a5047; }
.met-formato-btns { display: flex; gap: 8px; }
.met-formato-btn { flex: 1; padding: 10px 6px; border: 2px solid #ddd6cc; border-radius: 8px; cursor: pointer; background: #fff; display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 11px; font-weight: 600; color: #5a5047; }
.met-formato-btn--active { border-color: #1C1A18; background: #1C1A18; color: #FFCC00; }
.met-formato-icon { font-size: 18px; }
.met-radio-group { display: flex; gap: 16px; }
.met-radio { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #5a5047; cursor: pointer; }
.met-correo-row { display: flex; gap: 6px; align-items: center; }
.met-btn-del { background: none; border: none; color: #DC2626; cursor: pointer; font-size: 14px; }
.met-btn-add { background: none; border: 1px dashed #ddd6cc; border-radius: 6px; padding: 5px 12px; font-size: 11px; color: #5a5047; cursor: pointer; }
.met-error-msg { font-size: 12px; color: #DC2626; background: #FEF2F2; padding: 8px 12px; border-radius: 6px; }
.met-error-banner {
  display: flex; align-items: center; gap: 12px;
  background: #FEF2F2; border: 1px solid #FECACA;
  color: #991B1B; border-radius: 10px;
  padding: 16px 20px; margin-bottom: 20px;
  font-size: 13px;
}
.met-error-banner svg { flex-shrink: 0; color: #DC2626; }
.met-error-banner div { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.met-error-banner strong { font-size: 13px; }
.met-error-banner span { font-size: 12px; opacity: 0.85; }
.met-btn-retry {
  padding: 6px 14px; font-size: 11px; font-weight: 700;
  background: #DC2626; color: #fff; border: none;
  border-radius: 6px; cursor: pointer; white-space: nowrap;
}
.met-btn-retry:hover { background: #B91C1C; }
.met-btn-cancel { padding: 7px 16px; font-size: 12px; border: 1px solid #ddd6cc; border-radius: 8px; background: #fff; cursor: pointer; color: #5a5047; }
.met-btn-primary { display: flex; align-items: center; gap: 6px; padding: 7px 18px; font-size: 12px; font-weight: 700; background: #1C1A18; color: #FFCC00; border: none; border-radius: 8px; cursor: pointer; }
.met-btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── Spinners ────────────────────────────────────────────────────────────────── */
.adm-spinner--sm  { width: 24px; height: 24px; border-width: 3px; }
.adm-spinner--xs  { width: 14px; height: 14px; border-width: 2px; display: inline-block; }

@media (max-width: 900px) {
  .met-grid-2 { grid-template-columns: 1fr; }
  .met-topbar-actions { flex-direction: column; align-items: flex-start; }
}
</style>
