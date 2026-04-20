import { createRouter, createWebHistory } from 'vue-router'
import Principal           from '../views/Principal.vue'
import Reserva             from '../views/Reserva.vue'
import SeleccionAsientos   from '../views/SeleccionAsientos.vue'
import Checkout            from '../views/Checkout.vue'
import Confirmacion        from '../views/Confirmacion.vue'
import MisReservaciones    from '../views/MisReservaciones.vue'
import IniciarSesion       from '../views/IniciarSesion.vue'
import Registrarse         from '../views/Registrarse.vue'
import Informacion         from '../views/Informacion.vue'
import SobreMovent         from '../views/SobreMovent.vue'
import CentroAyuda         from '../views/CentroAyuda.vue'
import PreguntasFrecuentes from '../views/PreguntasFrecuentes.vue'
import Cancelacion         from '../views/Cancelacion.vue'
import Privacidad          from '../views/Privacidad.vue'
import Terminos            from '../views/Terminos.vue'
import Contacto            from '../views/Contacto.vue'
import ResultadosVuelos    from '../views/ResultadosVuelos.vue'
import ResultadosHoteles   from '../views/ResultadosHoteles.vue'
import ResultadosPaquetes  from '../views/ResultadosPaquetes.vue'
import Dashboard           from '../views/admin/Dashboard.vue'
import GestionRoles        from '../views/admin/GestionRoles.vue'
import GestionProveedores  from '../views/admin/GestionProveedores.vue'
import GestionFinanzas     from '../views/admin/GestionFinanzas.vue'
import WebService          from '../views/WebService.vue'
import Profile             from '../views/Profile.vue'
import Notificaciones      from '../views/Notificaciones.vue'
import AccessDenied        from '../views/AccessDenied.vue'

const routes = [
  { path: '/', redirect: '/principal', name: 'raiz' },
  { path: '/acceso-denegado', component: AccessDenied, name: 'accessDenied' },

  { path: '/principal', component: Principal, name: 'principal' },

  { path: '/reservar',           component: Reserva,           name: 'reservar' },
  { path: '/seleccion-asientos', component: SeleccionAsientos, name: 'seleccionAsientos' },
  { path: '/checkout',           component: Checkout,          name: 'checkout' },
  { path: '/confirmacion',       component: Confirmacion,      name: 'confirmacion' },

  { path: '/mis-reservaciones',  component: MisReservaciones,  name: 'misReservaciones' },
  { path: '/perfil',             component: Profile,           name: 'perfil' },
  { path: '/notificaciones',     component: Notificaciones,    name: 'notificaciones' },
  { path: '/ingreso',            component: IniciarSesion,     name: 'ingreso' },
  { path: '/registro',           component: Registrarse,       name: 'registro' },

  { path: '/informacion',          component: Informacion,         name: 'informacion' },
  { path: '/sobre-movent',         component: SobreMovent,         name: 'sobreMovent' },
  { path: '/centro-ayuda',         component: CentroAyuda,         name: 'centroAyuda' },
  { path: '/preguntas-frecuentes', component: PreguntasFrecuentes, name: 'preguntasFrecuentes' },
  { path: '/cancelacion',          component: Cancelacion,         name: 'cancelacion' },
  { path: '/privacidad',           component: Privacidad,          name: 'privacidad' },
  { path: '/terminos',             component: Terminos,            name: 'terminos' },
  { path: '/contacto',             component: Contacto,            name: 'contacto' },

  { path: '/resultados-vuelos',   component: ResultadosVuelos,   name: 'resultadosVuelos' },
  { path: '/resultados-hoteles',  component: ResultadosHoteles,  name: 'resultadosHoteles' },
  { path: '/resultados-paquetes', component: ResultadosPaquetes, name: 'resultadosPaquetes' },

  { path: '/admin',             redirect: '/admin/dashboard', name: 'admin' },
  { path: '/admin/dashboard',   component: Dashboard,         name: 'dashboard' },
  { path: '/admin/roles',       component: GestionRoles,      name: 'gestionRoles' },
  { path: '/admin/proveedores', component: GestionProveedores, name: 'gestionProveedores' },
  { path: '/admin/paquetes',    component: GestionFinanzas,   name: 'gestionFinanzas' },
  { path: '/admin/webservice',  component: WebService,        name: 'webService' },

  { path: '/:pathMatch(.*)*', redirect: '/principal', name: 'notFound' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  },
})

router.beforeEach((to, from) => {
  const esRutaAdmin = to.path.startsWith('/admin')
  if (!esRutaAdmin) return true

  const sesionRaw = sessionStorage.getItem('usuario_sesion')
                 || localStorage.getItem('usuario_sesion')

  if (!sesionRaw) return '/ingreso'

  try {
    const sesion = JSON.parse(sesionRaw)

    // /admin/webservice solo para rol 3 (WebService)
    if (to.path.startsWith('/admin/webservice')) {
      if (!sesion.isWS) return '/acceso-denegado'
      return true
    }

    // Resto de /admin/* solo para rol 2 (Administrador)
    if (!sesion.isAdmin) return '/acceso-denegado'
    return true

  } catch {
    return '/ingreso'
  }
})

export default router