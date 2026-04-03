import { createRouter, createWebHistory } from 'vue-router'
import Principal         from '../views/Principal.vue'
import Reserva           from '../views/Reserva.vue'
import SeleccionAsientos from '../views/SeleccionAsientos.vue'
import Checkout          from '../views/Checkout.vue'
import Confirmacion      from '../views/Confirmacion.vue'
import MisReservaciones  from '../views/MisReservaciones.vue'
import IniciarSesion     from '../views/IniciarSesion.vue'
import Registrarse       from '../views/Registrarse.vue'
import Informacion       from '../views/Informacion.vue'
import SobreMovent       from '../views/SobreMovent.vue'
import CentroAyuda       from '../views/CentroAyuda.vue'
import PreguntasFrecuentes from '../views/PreguntasFrecuentes.vue'
import Cancelacion       from '../views/Cancelacion.vue'
import Privacidad        from '../views/Privacidad.vue'
import Terminos          from '../views/Terminos.vue'
import Contacto          from '../views/Contacto.vue'
import ResultadosVuelos  from '../views/ResultadosVuelos.vue'
import ResultadosHoteles from '../views/ResultadosHoteles.vue'
import ResultadosPaquetes from '../views/ResultadosPaquetes.vue'
import Dashboard         from '../views/admin/Dashboard.vue'
import GestionRoles      from '../views/admin/GestionRoles.vue'
import GestionProveedores from '../views/admin/GestionProveedores.vue'
import GestionFinanzas   from '../views/admin/GestionFinanzas.vue'
import WebService        from '../views/WebService.vue'

const routes = [
  { path: '/', redirect: '/principal' },
  { path: '/principal',            component: Principal },
  { path: '/reservar',             component: Reserva },
  { path: '/seleccion-asientos',   component: SeleccionAsientos },
  { path: '/checkout',             component: Checkout },
  { path: '/confirmacion',         component: Confirmacion },
  { path: '/mis-reservaciones',    component: MisReservaciones },
  { path: '/ingreso',              component: IniciarSesion },
  { path: '/registro',             component: Registrarse },
  { path: '/informacion',          component: Informacion },
  { path: '/sobre-movent',         component: SobreMovent },
  { path: '/centro-ayuda',         component: CentroAyuda },
  { path: '/preguntas-frecuentes', component: PreguntasFrecuentes },
  { path: '/cancelacion',          component: Cancelacion },
  { path: '/privacidad',           component: Privacidad },
  { path: '/terminos',             component: Terminos },
  { path: '/contacto',             component: Contacto },
  { path: '/resultados-vuelos',    component: ResultadosVuelos },
  { path: '/resultados-hoteles',   component: ResultadosHoteles },
  { path: '/resultados-paquetes',  component: ResultadosPaquetes },
  { path: '/admin',                redirect: '/admin/dashboard' },
  { path: '/admin/dashboard',      component: Dashboard },
  { path: '/admin/roles',          component: GestionRoles },
  { path: '/admin/proveedores',    component: GestionProveedores },
  { path: '/admin/paquetes',       component: GestionFinanzas },
  { path: '/admin/webservice',     component: WebService },
  { path: '/:pathMatch(.*)*',      redirect: '/principal' },
]

export default createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  },
})