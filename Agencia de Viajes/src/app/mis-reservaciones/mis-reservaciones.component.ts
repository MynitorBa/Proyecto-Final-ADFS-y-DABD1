import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-mis-reservaciones',
  standalone: true,
  imports: [CommonModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './mis-reservaciones.component.html',
  styleUrls: ['./mis-reservaciones.component.css']
})
export class MisReservacionesComponent {
  filtro: string = 'todas';

  reservaciones = [
    {
      tipo: 'Vuelo + Hotel',
      codigo: 'MV-2026-A1',
      ruta: 'GUA → MEX',
      fecha: '15 Feb 2026',
      pasajeros: '2 Adultos',
      total: '3,280.00',
      estado: 'confirmada'
    },
    {
      tipo: 'Vuelo Ida y Vuelta',
      codigo: 'MV-2026-B9',
      ruta: 'GUA → CUN',
      fecha: '10 Mar 2026',
      pasajeros: '1 Adulto',
      total: '1,450.00',
      estado: 'cancelada'
    }
  ];
}