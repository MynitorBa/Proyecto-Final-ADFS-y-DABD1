import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-registrarse',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './registrarse.component.html',
  styleUrls: ['./registrarse.component.css']
})
export class RegistrarseComponent {
  userData = { nombre: '', email: '', password: '' };

  onRegister() {
    console.log('Registrando usuario:', this.userData);
    alert('Cuenta creada exitosamente');
  }
}