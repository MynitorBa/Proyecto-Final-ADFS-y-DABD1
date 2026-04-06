package org.example.dtos;

/**
 * DTO con los datos que envia la aerolinea para solicitar un token de alianza.
 * La ciudad y pais se usan para identificar el destino del pasajero.
 */
public class TokenAerolineaRequestDTO {

    private String ciudad;
    private String pais;

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
}