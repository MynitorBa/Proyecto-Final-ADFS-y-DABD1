package org.example.dtos;

/** DTO para actualizar nombre, apellido y fecha de nacimiento del usuario. */
public class ActualizarDatosPersonalesRequestDTO {
    private String nombre;
    private String apellido;
    private String fechaNacimiento; // YYYY-MM-DD

    public String getNombre()           { return nombre; }
    public void   setNombre(String v)   { this.nombre = v; }

    public String getApellido()         { return apellido; }
    public void   setApellido(String v) { this.apellido = v; }

    public String getFechaNacimiento()         { return fechaNacimiento; }
    public void   setFechaNacimiento(String v) { this.fechaNacimiento = v; }
}
