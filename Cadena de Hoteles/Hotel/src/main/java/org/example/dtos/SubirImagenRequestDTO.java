package org.example.dtos;

/**
 * DTO con los datos necesarios para subir una imagen al sistema.
 * La imagen debe enviarse codificada en Base64.
 */
public class SubirImagenRequestDTO {

    private String base64; // imagen codificada en Base64

    /**
     * Retorna la imagen codificada en Base64.
     * @return imagen en formato Base64.
     */
    public String getBase64() { return base64; }

    /**
     * Asigna la imagen codificada en Base64.
     * @param base64 imagen en formato Base64.
     */
    public void setBase64(String base64) { this.base64 = base64; }
}