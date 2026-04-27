package org.example.dtos;

import java.time.LocalDate;

/**
 * DTO con los datos que envia la aerolinea para solicitar un token de alianza.
 * La ciudad y pais se usan para identificar el destino del pasajero.
 */
public class TokenAerolineaRequestDTO {

    private String ciudad;
    private String pais;
    private String fechaIda;
    private String fechaVuelta;

    public String getFechaIda()              { return fechaIda; }
    public void setFechaIda(String fechaIda) { this.fechaIda = fechaIda; }

    public String getFechaVuelta()                 { return fechaVuelta; }
    public void setFechaVuelta(String fechaVuelta) { this.fechaVuelta = fechaVuelta; }

    public TokenAerolineaRequestDTO() {}

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
}