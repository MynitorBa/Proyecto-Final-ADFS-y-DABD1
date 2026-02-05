import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-reserva',
  standalone: true,
  imports: [CommonModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './reserva.component.html',
  styleUrls: ['./reserva.component.css']
})
export class ReservaComponent {
  filtro: string = 'todos';

  catalogo = [
    {
      nombre: 'Paquete Ciudad de México',
      descripcion: 'Vuelo + Hotel 4 estrellas',
      destino: 'México (MEX)',
      duracion: '5 días / 4 noches',
      incluye: 'Vuelo + Hotel + Desayuno',
      precio: '3,280.00',
      disponible: true
    },
    {
      nombre: 'Cancún All Inclusive',
      descripcion: 'Resort todo incluido frente al mar',
      destino: 'Cancún (CUN)',
      duracion: '7 días / 6 noches',
      incluye: 'Vuelo + Resort + Todo Incluido',
      precio: '5,450.00',
      disponible: true
    },
    {
      nombre: 'Miami Beach',
      descripcion: 'Hotel en South Beach',
      destino: 'Miami (MIA)',
      duracion: '4 días / 3 noches',
      incluye: 'Vuelo + Hotel',
      precio: '2,890.00',
      disponible: true
    },
    {
      nombre: 'Nueva York Express',
      descripcion: 'Manhattan - Times Square',
      destino: 'Nueva York (JFK)',
      duracion: '6 días / 5 noches',
      incluye: 'Vuelo + Hotel + Tours',
      precio: '4,650.00',
      disponible: true
    }
  ];
}