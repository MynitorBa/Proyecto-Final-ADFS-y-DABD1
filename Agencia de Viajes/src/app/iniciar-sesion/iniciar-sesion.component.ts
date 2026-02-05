import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EncabezadoComponent } from '../encabezado/encabezado.component';
import { PiepaginaComponent } from '../piepagina/piepagina.component';

@Component({
  selector: 'app-iniciar-sesion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, EncabezadoComponent, PiepaginaComponent],
  templateUrl: './iniciar-sesion.component.html',
  styleUrls: ['./iniciar-sesion.component.css']
})
export class IniciarSesionComponent {
  loginData = { email: '', password: '' };

  onSubmit() {
    console.log('Iniciando sesión:', this.loginData);
    alert('Inicio de sesión exitoso');
  }
}