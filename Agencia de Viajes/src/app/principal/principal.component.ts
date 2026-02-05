import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-principal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './principal.component.html',
  styleUrls: ['./principal.component.css']
})
export class PrincipalComponent {
  searchType: string = 'flights';
  tripType: string = 'roundtrip';
  showScrollTop: boolean = false;

  searchData = {
    origen: '',
    destino: '',
    fechaIda: '',
    fechaVuelta: '',
    tipoAsiento: 'Turista',
    pasajeros: 1,
    checkIn: '',
    checkOut: '',
    huespedes: 1
  };

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.showScrollTop = window.scrollY > 300;
  }

  setSearchType(type: string) {
    this.searchType = type;
  }

  setTripType(type: string) {
    this.tripType = type;
  }

  scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}