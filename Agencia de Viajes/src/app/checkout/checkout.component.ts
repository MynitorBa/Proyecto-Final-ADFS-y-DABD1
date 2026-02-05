import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent {
  // Ejemplo de items en el carrito
  itemsCarrito = [
    {
      nombre: 'Paquete Ciudad de México',
      descripcion: 'Vuelo + Hotel 4 estrellas',
      destino: 'México (MEX)',
      duracion: '5 días / 4 noches',
      pasajeros: 2,
      precioUnitario: 3280.00,
      subtotal: 6560.00
    }
  ];

  constructor(private router: Router) {}

  // Cálculos
  get subtotal(): number {
    return this.itemsCarrito.reduce((sum, item) => sum + item.subtotal, 0);
  }

  get impuestos(): number {
    return this.subtotal * 0.12; // 12% de impuestos
  }

  get total(): number {
    return this.subtotal + this.impuestos;
  }

  eliminarItem(index: number) {
    this.itemsCarrito.splice(index, 1);
  }

  confirmarCompra() {
    this.router.navigate(['/mis-reservaciones']);
  }
}