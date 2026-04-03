<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- SIDEBAR -->
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

        <!-- CONTENIDO -->
        <div class="adm-main">

          <!-- Topbar -->
          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Finanzas</h1>
              <p class="adm-topbar__sub">Ingresos, pagos a proveedores y ganancia MOVENT</p>
            </div>
            <div class="adm-topbar__actions">
              <div class="adm-search">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="busqueda" type="text" placeholder="Buscar código o usuario..." class="adm-search__input" />
              </div>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando métricas financieras...</p>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="adm-empty">
            <svg viewBox="0 0 24 24" fill="none" stroke="#D40511" stroke-width="1.5" width="40" height="40"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/></svg>
            <p>{{ error }}</p>
            <button class="adm-btn adm-btn--yellow" @click="cargar" type="button">Reintentar</button>
          </div>

          <template v-else>

            <!-- ── KPIs globales ── -->
            <div class="adm-kpis">
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Total facturado</p>
                  <p class="adm-kpi__val">${{ fmt(resumen.totalCobrado) }}</p>
                </div>
              </div>
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Pagado a proveedores</p>
                  <p class="adm-kpi__val">${{ fmt(resumen.totalBase) }}</p>
                </div>
              </div>
              <div class="adm-kpi" style="border: 2px solid #FFCC00;">
                <div class="adm-kpi__icon adm-kpi__icon--yellow">
                  <svg viewBox="0 0 24 24" fill="#1C1A18" width="20" height="20"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Ganancia MOVENT</p>
                  <p class="adm-kpi__val" style="color:#1C1A18">${{ fmt(resumen.totalGanancia) }}</p>
                </div>
              </div>
            </div>

            <!-- ── Desglose por tipo ── -->
            <div class="adm-fin-tipos">
              <div class="adm-fin-tipo adm-fin-tipo--vuelo">
                <div class="adm-fin-tipo__head">
                  <svg viewBox="0 0 24 24" fill="#FFCC00" width="16" height="16"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                  <span>Vuelos</span>
                  <span class="adm-fin-tipo__cant">{{ resumen.vuelos?.cantidad ?? 0 }} reservas</span>
                </div>
                <div class="adm-fin-tipo__nums">
                  <div><span class="adm-fin-tipo__lbl">Cobrado</span><span class="adm-fin-tipo__val">${{ fmt(resumen.vuelos?.cobrado) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Proveedor</span><span class="adm-fin-tipo__val">${{ fmt(resumen.vuelos?.base) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Ganancia</span><span class="adm-fin-tipo__val adm-fin-tipo__val--gain">${{ fmt(resumen.vuelos?.ganancia) }}</span></div>
                </div>
              </div>

              <div class="adm-fin-tipo adm-fin-tipo--hotel">
                <div class="adm-fin-tipo__head">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="16" height="16"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                  <span>Hoteles</span>
                  <span class="adm-fin-tipo__cant">{{ resumen.hoteles?.cantidad ?? 0 }} reservas</span>
                </div>
                <div class="adm-fin-tipo__nums">
                  <div><span class="adm-fin-tipo__lbl">Cobrado</span><span class="adm-fin-tipo__val">${{ fmt(resumen.hoteles?.cobrado) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Proveedor</span><span class="adm-fin-tipo__val">${{ fmt(resumen.hoteles?.base) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Ganancia</span><span class="adm-fin-tipo__val adm-fin-tipo__val--gain">${{ fmt(resumen.hoteles?.ganancia) }}</span></div>
                </div>
              </div>

              <div class="adm-fin-tipo adm-fin-tipo--paquete">
                <div class="adm-fin-tipo__head">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                  <span>Paquetes</span>
                  <span class="adm-fin-tipo__cant">{{ resumen.paquetes?.cantidad ?? 0 }} reservas</span>
                </div>
                <div class="adm-fin-tipo__nums">
                  <div><span class="adm-fin-tipo__lbl">Cobrado</span><span class="adm-fin-tipo__val">${{ fmt(resumen.paquetes?.cobrado) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Proveedor</span><span class="adm-fin-tipo__val">${{ fmt(resumen.paquetes?.base) }}</span></div>
                  <div><span class="adm-fin-tipo__lbl">Ganancia</span><span class="adm-fin-tipo__val adm-fin-tipo__val--gain">${{ fmt(resumen.paquetes?.ganancia) }}</span></div>
                </div>
              </div>
            </div>

            <!-- ── Filtro multi-tipo ── -->
            <div class="adm-filtros-rol">
              <button
                v-for="t in tiposOpts" :key="t.val"
                :class="['adm-filtro-rol', { 'adm-filtro-rol--active': filtros.includes(t.val) }]"
                @click="toggleFiltro(t.val)" type="button">
                {{ t.label }}
                <span class="adm-filtro-rol__n">{{ contarTipo(t.val) }}</span>
              </button>
            </div>

            <!-- Sin resultados -->
            <div v-if="reservasFiltradas.length === 0" class="adm-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1" width="44" height="44"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              <p>Sin resultados{{ busqueda ? ` para "${busqueda}"` : '' }}</p>
            </div>

            <!-- ── Tabla de reservaciones con desglose financiero ── -->
            <div v-else class="adm-card adm-card--full">
              <div class="adm-card__head">
                <h3 class="adm-card__title">Detalle por reservación</h3>
                <span class="adm-card__count">{{ reservasFiltradas.length }} registros</span>
              </div>
              <div class="adm-tabla-wrap">
                <table class="adm-tabla">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Usuario</th>
                      <th>Tipo</th>
                      <th>Fecha</th>
                      <th>Cobrado</th>
                      <th>Proveedor</th>
                      <th style="color:#22c55e">Ganancia</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="r in reservasFiltradas" :key="r.id">
                      <!-- Fila principal -->
                      <tr class="adm-fin-row" @click="toggleDetalle(r.id)" style="cursor:pointer">
                        <td><span class="adm-tabla__codigo">{{ r.noReservacion }}</span></td>
                        <td>{{ r.usuario }}</td>
                        <td><span class="adm-tabla__tipo" :class="`adm-tabla__tipo--${r.tipoNombre}`">{{ r.tipoNombre }}</span></td>
                        <td>{{ formatFecha(r.fechaCreacion) }}</td>
                        <td><strong>${{ fmt(r.totalCobrado) }}</strong></td>
                        <td class="adm-fin-row__base">${{ fmt(r.totalBase) }}</td>
                        <td><span class="adm-fin-gain">${{ fmt(r.totalGanancia) }}</span></td>
                        <td><span class="adm-badge" :class="`adm-badge--${r.estado}`">{{ r.estado }}</span></td>
                      </tr>
                      <!-- Fila de detalle expandible -->
                      <tr v-if="detalleAbierto === r.id" class="adm-fin-detalle-row">
                        <td colspan="8">
                          <div class="adm-fin-detalle">
                            <div v-if="r.vuelo" class="adm-fin-detalle__item adm-fin-detalle__item--vuelo">
                              <div class="adm-fin-detalle__icon">
                                <svg viewBox="0 0 24 24" fill="#FFCC00" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                              </div>
                              <div class="adm-fin-detalle__info">
                                <span class="adm-fin-detalle__label">Vuelo</span>
                                <span>Cobrado: <strong>${{ fmt(r.vuelo.cobrado) }}</strong></span>
                                <span>Aerolínea: <strong>${{ fmt(r.vuelo.base) }}</strong></span>
                                <span class="adm-fin-gain">Ganancia: ${{ fmt(r.vuelo.ganancia) }}</span>
                                <span class="adm-fin-detalle__pct">{{ r.vuelo.porcentaje }}% markup</span>
                              </div>
                            </div>
                            <div v-if="r.hotel" class="adm-fin-detalle__item adm-fin-detalle__item--hotel">
                              <div class="adm-fin-detalle__icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                              </div>
                              <div class="adm-fin-detalle__info">
                                <span class="adm-fin-detalle__label">Hotel</span>
                                <span>Cobrado: <strong>${{ fmt(r.hotel.cobrado) }}</strong></span>
                                <span>Hotelera: <strong>${{ fmt(r.hotel.base) }}</strong></span>
                                <span class="adm-fin-gain">Ganancia: ${{ fmt(r.hotel.ganancia) }}</span>
                                <span class="adm-fin-detalle__pct">{{ r.hotel.porcentaje }}% markup</span>
                              </div>
                            </div>
                          </div>
                        </td>
                      </tr>
                    </template>
                  </tbody>
                </table>
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
import { ref, computed, onMounted } from 'vue'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

const API = 'http://localhost:8080'

const loading      = ref(true)
const error        = ref('')
const resumen      = ref({})
const reservaciones = ref([])
const busqueda     = ref('')
const filtros      = ref(['vuelo', 'hotel', 'paquete']) // todos activos por defecto
const detalleAbierto = ref(null)

const tiposOpts = [
  { val: 'vuelo',   label: 'Vuelos' },
  { val: 'hotel',   label: 'Hoteles' },
  { val: 'paquete', label: 'Paquetes' },
]

// ── Computed ──────────────────────────────────────────────────────────
const reservasFiltradas = computed(() => {
  let list = reservaciones.value.filter(r => filtros.value.includes(r.tipoNombre))
  if (busqueda.value.trim()) {
    const q = busqueda.value.toLowerCase()
    list = list.filter(r =>
      r.noReservacion.toLowerCase().includes(q) ||
      r.usuario.toLowerCase().includes(q)
    )
  }
  return list
})

const contarTipo = (tipo) =>
  reservaciones.value.filter(r => r.tipoNombre === tipo).length

// ── Helpers ───────────────────────────────────────────────────────────
function toggleFiltro(val) {
  if (filtros.value.includes(val)) {
    if (filtros.value.length === 1) return // mínimo uno activo
    filtros.value = filtros.value.filter(f => f !== val)
  } else {
    filtros.value = [...filtros.value, val]
  }
}

function toggleDetalle(id) {
  detalleAbierto.value = detalleAbierto.value === id ? null : id
}

function fmt(n) {
  if (!n && n !== 0) return '0.00'
  return Number(n).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatFecha(f) {
  if (!f) return '--'
  try { return new Date(f).toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' }) }
  catch { return f }
}

// ── Carga ─────────────────────────────────────────────────────────────
onMounted(() => cargar())

async function cargar() {
  loading.value = true; error.value = ''
  try {
    const res = await fetch(`${API}/api/admin/metricas`, { credentials: 'include' })
    if (!res.ok) throw new Error(`Error ${res.status}`)
    const data = await res.json()
    resumen.value       = data.resumen       ?? {}
    reservaciones.value = data.reservaciones ?? []
  } catch {
    error.value = 'No se pudieron cargar las métricas financieras.'
  } finally {
    loading.value = false
  }
}
</script>