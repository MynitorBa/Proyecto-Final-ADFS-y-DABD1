import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-informacion',
  standalone: true,
  imports: [CommonModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './informacion.component.html',
  styleUrls: ['./informacion.component.css']
})
export class InformacionComponent {
  seccion: string = 'socios';
  imgBase: string = 'agentes.png'; 

  aerolineas = [
    {
      nombre: 'Broom Airline',
      img: this.imgBase,
      descripcion: 'Líder en vuelos de larga distancia con servicios premium.',
      tags: ['Socio Oro', 'Premium']
    },
    {
      nombre: 'AeroSky',
      img: this.imgBase,
      descripcion: 'Máxima puntualidad en todas nuestras rutas regionales.',
      tags: ['Regional', 'Frecuente']
    }
  ];

  setTab(id: string) {
    this.seccion = id;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}