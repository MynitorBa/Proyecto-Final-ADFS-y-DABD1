import { Routes } from '@angular/router';
import { PrincipalComponent } from './principal/principal.component';
import { MisReservacionesComponent } from './mis-reservaciones/mis-reservaciones.component';
import { InformacionComponent } from './informacion/informacion.component';
import { IniciarSesionComponent } from './iniciar-sesion/iniciar-sesion.component';
import { RegistrarseComponent } from './registrarse/registrarse.component';
import { ReservaComponent } from './reserva/reserva.component';
import { CheckoutComponent } from './checkout/checkout.component';

export const routes: Routes = [
  { path: '', redirectTo: '/principal', pathMatch: 'full' }, 
  
  { path: 'principal', component: PrincipalComponent },
  { path: 'mis-reservaciones', component: MisReservacionesComponent },
  { path: 'reservar', component: ReservaComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'informacion', component: InformacionComponent },
  { path: 'ingreso', component: IniciarSesionComponent },
  { path: 'registro', component: RegistrarseComponent },
  
  { path: '**', redirectTo: '/principal' } 
];