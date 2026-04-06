package org.example.dtos;

/**
 * DTO con los datos de identidad de una aerolinea aliada autenticada por token.
 */
public class AerolineaIdentidadDTO {

    private int    id;
    private String nombre;
    private String urlAerolinea;

    public AerolineaIdentidadDTO(int id, String nombre, String urlAerolinea) {
        this.id          = id;
        this.nombre      = nombre;
        this.urlAerolinea = urlAerolinea;
    }

    public int    getId()           { return id; }
    public String getNombre()       { return nombre; }
    public String getUrlAerolinea() { return urlAerolinea; }
}