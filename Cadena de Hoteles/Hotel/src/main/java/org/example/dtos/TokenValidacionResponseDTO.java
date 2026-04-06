package org.example.dtos;

/**
 * DTO con la respuesta de validacion de un token de alianza.
 * Contiene los datos necesarios para que el frontend configure
 * la busqueda con descuento sin exponer logica interna.
 */
public class TokenValidacionResponseDTO {

    private String ciudad;
    private String pais;
    private double porcentajeDescuento;
    private String fechaExpiracion;

    public TokenValidacionResponseDTO(String ciudad, String pais,
                                      double porcentajeDescuento, String fechaExpiracion) {
        this.ciudad              = ciudad;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaExpiracion     = fechaExpiracion;
        this.pais                = pais;
    }

    public String getCiudad()               { return ciudad; }
    public String getPais()                 { return pais; }
    public double getPorcentajeDescuento()  { return porcentajeDescuento; }
    public String getFechaExpiracion()      { return fechaExpiracion; }
}