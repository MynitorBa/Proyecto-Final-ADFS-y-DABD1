package org.example.dtos;

public class CrearAgenciaRequestDTO {

    private String nombre;
    private String correo;
    // El descuento siempre inicia en 0%; solo el admin puede modificarlo

    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
}