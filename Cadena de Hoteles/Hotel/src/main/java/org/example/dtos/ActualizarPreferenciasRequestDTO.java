package org.example.dtos;

/** DTO para guardar o limpiar las preferencias de ofertas del usuario. */
public class ActualizarPreferenciasRequestDTO {
    private String preferenciasOferta; // JSON string, o null para desactivar

    public String getPreferenciasOferta()         { return preferenciasOferta; }
    public void   setPreferenciasOferta(String v) { this.preferenciasOferta = v; }
}
