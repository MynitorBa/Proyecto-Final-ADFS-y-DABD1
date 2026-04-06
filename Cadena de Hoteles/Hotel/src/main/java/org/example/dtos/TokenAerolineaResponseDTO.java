package org.example.dtos;

/**
 * DTO con la respuesta tras generar un token de alianza exitosamente.
 * Contiene el token generado, la URL para redirigir al usuario
 * y la fecha en que expira el token.
 */
public class TokenAerolineaResponseDTO {

    private String token;
    private String urlRedireccion;
    private String fechaExpiracion;

    public TokenAerolineaResponseDTO(String token, String urlRedireccion, String fechaExpiracion) {
        this.token          = token;
        this.urlRedireccion = urlRedireccion;
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getToken()           { return token; }
    public String getUrlRedireccion()  { return urlRedireccion; }
    public String getFechaExpiracion() { return fechaExpiracion; }
}