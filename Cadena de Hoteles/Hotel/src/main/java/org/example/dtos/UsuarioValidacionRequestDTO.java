package org.example.dtos;

import java.util.List;

public class UsuarioValidacionRequestDTO {
    private String username;
    private String correo;
    private String contrasena;
    private String pasaporte;
    private String nombre;
    private String apellido;
    private String telefono;
    private String fechaNacimiento;      // "YYYY-MM-DD"
    private String pais;                 // nombre del país como string
    private String ciudad;               // nombre de la ciudad como string
    private List<String> nacionalidades; // lista de nombres como strings

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getPasaporte() { return pasaporte; }
    public void setPasaporte(String pasaporte) { this.pasaporte = pasaporte; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public List<String> getNacionalidades() { return nacionalidades; }
    public void setNacionalidades(List<String> nacionalidades) { this.nacionalidades = nacionalidades; }
}