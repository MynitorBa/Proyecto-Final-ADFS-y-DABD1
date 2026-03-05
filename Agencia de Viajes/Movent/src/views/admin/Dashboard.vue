<template>
  <div class="page">
    <Encabezado />

    <div class="adm-page">
      <div class="adm-layout">

        <!-- ═══ SIDEBAR ADMIN ═══ -->
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
            <router-link to="/admin/dashboard" class="adm-nav__item adm-nav__item--active">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
              Dashboard
            </router-link>
            <router-link to="/admin/proveedores" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
              Proveedores
            </router-link>
            <router-link to="/admin/paquetes" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              Paquetes
            </router-link>
            <router-link to="/admin/roles" class="adm-nav__item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Roles
            </router-link>
          </nav>
        </aside>

        <!-- ═══ CONTENIDO ═══ -->
        <div class="adm-main">

          <div class="adm-topbar">
            <div>
              <h1 class="adm-topbar__titulo">Dashboard</h1>
              <p class="adm-topbar__sub">Resumen general de Movent</p>
            </div>
            <div class="adm-topbar__fecha">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              {{ fechaHoy }}
            </div>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="adm-empty">
            <div class="adm-spinner"></div>
            <p>Cargando estadísticas...</p>
          </div>

          <template v-else>

            <!-- ── FILA 1: KPIs principales ── -->
            <div class="adm-kpis">
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Reservaciones totales</p>
                  <p class="adm-kpi__val">{{ stats.totalReservaciones ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi adm-kpi--dark">
                <div class="adm-kpi__icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#FFCC00" stroke-width="2" width="22" height="22"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Ingresos totales</p>
                  <p class="adm-kpi__val">${{ formatMoney(stats.ingresosTotales) }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--yellow">
                  <svg viewBox="0 0 24 24" fill="#1C1A18" width="20" height="20"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Vuelos reservados</p>
                  <p class="adm-kpi__val">{{ stats.vuelosReservados ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--black">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Hospedajes reservados</p>
                  <p class="adm-kpi__val">{{ stats.hotelesReservados ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--red">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Paquetes activos</p>
                  <p class="adm-kpi__val">{{ stats.paquetesActivos ?? '--' }}</p>
                </div>
              </div>
              <div class="adm-kpi">
                <div class="adm-kpi__icon adm-kpi__icon--green">
                  <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" width="20" height="20"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div>
                  <p class="adm-kpi__lbl">Usuarios registrados</p>
                  <p class="adm-kpi__val">{{ stats.usuariosRegistrados ?? '--' }}</p>
                </div>
              </div>
            </div>

            <!-- ── FILA 2: Estados de reservaciones ── -->
            <div class="adm-estados">
              <div class="adm-estado" v-for="e in estadosRes" :key="e.label">
                <div class="adm-estado__top">
                  <span class="adm-estado__dot" :style="{ background: e.color }"></span>
                  <span class="adm-estado__label">{{ e.label }}</span>
                </div>
                <span class="adm-estado__val">{{ e.val ?? '--' }}</span>
                <div class="adm-estado__bar">
                  <div class="adm-estado__bar-fill" :style="{ width: pctEstado(e.val) + '%', background: e.color }"></div>
                </div>
              </div>
            </div>

            <!-- ── FILA 3: Búsquedas recientes + proveedores ── -->
            <div class="adm-row2">

              <!-- Búsquedas recientes -->
              <div class="adm-card">
                <div class="adm-card__head">
                  <h3 class="adm-card__title">Búsquedas recientes</h3>
                  <span class="adm-card__count">{{ busquedasRecientes.length }}</span>
                </div>
                <div v-if="busquedasRecientes.length === 0" class="adm-card__empty">Sin registros</div>
                <div v-else class="adm-busquedas">
                  <div v-for="b in busquedasRecientes" :key="b.id" class="adm-busqueda">
                    <div class="adm-busqueda__tipo" :class="`adm-busqueda__tipo--${b.tipo}`">
                      <svg v-if="b.tipo==='vuelo'" viewBox="0 0 24 24" fill="currentColor" width="12" height="12"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    </div>
                    <div class="adm-busqueda__info">
                      <span class="adm-busqueda__query">{{ b.query }}</span>
                      <span class="adm-busqueda__meta">{{ b.usuario || 'Anónimo' }} · {{ formatFechaHora(b.fecha) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Estado proveedores -->
              <div class="adm-card">
                <div class="adm-card__head">
                  <h3 class="adm-card__title">Proveedores</h3>
                  <button class="adm-card__link" @click="$router.push('/admin/proveedores')" type="button">Ver todos</button>
                </div>
                <div v-if="proveedores.length === 0" class="adm-card__empty">Sin proveedores configurados</div>
                <div v-else class="adm-proveedores">
                  <div v-for="p in proveedores" :key="p.id" class="adm-proveedor">
                    <div class="adm-proveedor__info">
                      <div class="adm-proveedor__tipo-icon" :class="`adm-proveedor__tipo-icon--${p.tipo}`">
                        <svg v-if="p.tipo==='aerolinea'" viewBox="0 0 24 24" fill="currentColor" width="13" height="13"><path d="M17.8 19.2L16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.4-.1.9.3 1.1l5.5 3.1-3 3-1.7-.5c-.3-.1-.7 0-.9.2l-.5.5c-.2.2-.2.6 0 .8l2.1 2.1c.2.2.6.2.8 0l.5-.5c.2-.2.3-.6.2-.9l-.5-1.7 3-3 3.1 5.5c.2.4.7.5 1.1.3l.5-.3c.4-.2.6-.7.5-1.1z"/></svg>
                        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                      </div>
                      <div>
                        <p class="adm-proveedor__nombre">{{ p.nombre }}</p>
                        <p class="adm-proveedor__url">{{ p.url }}</p>
                      </div>
                    </div>
                    <span class="adm-badge" :class="p.activo ? 'adm-badge--on' : 'adm-badge--off'">
                      {{ p.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </div>
                </div>
              </div>

            </div>

            <!-- ── FILA 4: Reservaciones recientes ── -->
            <div class="adm-card adm-card--full">
              <div class="adm-card__head">
                <h3 class="adm-card__title">Reservaciones recientes</h3>
                <button class="adm-card__link" @click="$router.push('/mis-reservaciones')" type="button">Ver todas</button>
              </div>
              <div v-if="reservacionesRecientes.length === 0" class="adm-card__empty">Sin reservaciones recientes</div>
              <div v-else class="adm-tabla-wrap">
                <table class="adm-tabla">
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Usuario</th>
                      <th>Tipo</th>
                      <th>Fecha</th>
                      <th>Total</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in reservacionesRecientes" :key="r.id">
                      <td><span class="adm-tabla__codigo">{{ r.noReservacion }}</span></td>
                      <td>{{ r.usuario }}</td>
                      <td>
                        <span class="adm-tabla__tipo" :class="`adm-tabla__tipo--${r.tipo}`">{{ r.tipo }}</span>
                      </td>
                      <td>{{ formatFecha(r.fechaReservacion) }}</td>
                      <td><strong>${{ r.totalReservacion?.toFixed(2) }}</strong></td>
                      <td>
                        <span class="adm-badge" :class="`adm-badge--${r.estado}`">{{ r.estado }}</span>
                      </td>
                    </tr>
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
import { useRouter } from 'vue-router'
import Encabezado from '../../components/Encabezado.vue'
import Piepagina from '../../components/Piepagina.vue'
import '../../styles/admin.css'

const router  = useRouter()
const API     = 'http://localhost:7000'
const loading = ref(true)

const stats                = ref({})
const busquedasRecientes   = ref([])
const proveedores          = ref([])
const reservacionesRecientes = ref([])

const fechaHoy = new Date().toLocaleDateString('es-GT', { weekday:'long', day:'2-digit', month:'long', year:'numeric' })

const estadosRes = computed(() => [
  { label: 'Confirmadas', val: stats.value.confirmadas,  color: '#22c55e' },
  { label: 'Pendientes',  val: stats.value.pendientes,   color: '#f59e0b' },
  { label: 'Completadas', val: stats.value.completadas,  color: '#3b82f6' },
  { label: 'Canceladas',  val: stats.value.canceladas,   color: '#D40511' },
  { label: 'Expiradas',   val: stats.value.expiradas,    color: '#8b5cf6' },
])

const totalRes = computed(() => estadosRes.value.reduce((s, e) => s + (e.val || 0), 0))
const pctEstado = (val) => totalRes.value > 0 ? Math.round((val || 0) / totalRes.value * 100) : 0

onMounted(() => cargarTodo())

async function cargarTodo() {
  loading.value = true
  // ── DEMO DATA ────────────────────────────────
  await new Promise(r => setTimeout(r, 400)) // simula carga
  stats.value = {
    totalReservaciones: 248,
    ingresosTotales:    94320.50,
    vuelosReservados:   133,
    hotelesReservados:  71,
    paquetesActivos:    12,
    usuariosRegistrados: 186,
    confirmadas:  142,
    pendientes:    38,
    completadas:   51,
    canceladas:    12,
    expiradas:      5,
  }
  busquedasRecientes.value = [
    { id:1, tipo:'vuelo', query:'GUA → MIA  ·  2 pasajeros', usuario:'Carlos Méndez', fecha: new Date(Date.now()-3600000).toISOString() },
    { id:2, tipo:'hotel', query:'Antigua Guatemala · 3 noches', usuario:'María López', fecha: new Date(Date.now()-7200000).toISOString() },
    { id:3, tipo:'vuelo', query:'GUA → CUN  ·  1 pasajero',  usuario:'Anónimo',       fecha: new Date(Date.now()-14400000).toISOString() },
    { id:4, tipo:'vuelo', query:'GUA → MEX  ·  4 pasajeros', usuario:'Pedro Ruiz',    fecha: new Date(Date.now()-28800000).toISOString() },
    { id:5, tipo:'hotel', query:'Ciudad de Guatemala · 1 noche', usuario:'Ana García', fecha: new Date(Date.now()-43200000).toISOString() },
    { id:6, tipo:'vuelo', query:'GUA → PTY  ·  2 pasajeros', usuario:'Luis Castillo', fecha: new Date(Date.now()-86400000).toISOString() },
  ]
  proveedores.value = [
    { id:1, nombre:'Broom AirLine',     tipo:'aerolinea', url:'http://localhost:7000', activo:true  },
    { id:2, nombre:'Casa Santo Domingo',tipo:'hotel',     url:'http://api.casasantodomingo.com', activo:true  },
    { id:3, nombre:'Barceló Guatemala', tipo:'hotel',     url:'http://api.barcelo.com/gt',       activo:false },
    { id:4, nombre:'Copa Airlines',     tipo:'aerolinea', url:'http://api.copaair.com',           activo:true  },
  ]
  reservacionesRecientes.value = [
    { id:1, noReservacion:'MV-2026-04821', usuario:'Carlos Méndez',  tipo:'vuelo',   fechaReservacion: new Date(Date.now()-3600000).toISOString(),  totalReservacion:320.00, estado:'confirmada'  },
    { id:2, noReservacion:'MV-2026-04820', usuario:'María López',    tipo:'hotel',   fechaReservacion: new Date(Date.now()-7200000).toISOString(),  totalReservacion:185.00, estado:'pendiente'   },
    { id:3, noReservacion:'MV-2026-04819', usuario:'Pedro Ruiz',     tipo:'paquete', fechaReservacion: new Date(Date.now()-86400000).toISOString(), totalReservacion:4850.00,estado:'confirmada'  },
    { id:4, noReservacion:'MV-2026-04818', usuario:'Ana García',     tipo:'vuelo',   fechaReservacion: new Date(Date.now()-172800000).toISOString(),totalReservacion:210.00, estado:'completada'  },
    { id:5, noReservacion:'MV-2026-04817', usuario:'Luis Castillo',  tipo:'paquete', fechaReservacion: new Date(Date.now()-259200000).toISOString(),totalReservacion:2600.00,estado:'cancelada'   },
    { id:6, noReservacion:'MV-2026-04816', usuario:'Sofía Reyes',    tipo:'vuelo',   fechaReservacion: new Date(Date.now()-345600000).toISOString(),totalReservacion:320.00, estado:'completada'  },
    { id:7, noReservacion:'MV-2026-04815', usuario:'Diego Hernández',tipo:'hotel',   fechaReservacion: new Date(Date.now()-432000000).toISOString(),totalReservacion:370.00, estado:'confirmada'  },
  ]
  loading.value = false
}

function formatMoney(n) {
  if (!n && n !== 0) return '--'
  return Number(n).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatFecha(f) {
  if (!f) return '--'
  return new Date(f).toLocaleDateString('es-GT', { day:'2-digit', month:'short', year:'numeric' })
}

function formatFechaHora(f) {
  if (!f) return '--'
  return new Date(f).toLocaleString('es-GT', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' })
}
</script>