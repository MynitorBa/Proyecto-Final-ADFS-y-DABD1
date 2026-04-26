package org.example.dtos;

/** DTO para actualizar el país y ciudad de residencia del usuario. */
public class ActualizarCiudadRequestDTO {
    private String pais;
    private String ciudad;

    public String getPais()         { return pais; }
    public void   setPais(String v) { this.pais = v; }

    public String getCiudad()         { return ciudad; }
    public void   setCiudad(String v) { this.ciudad = v; }
}
