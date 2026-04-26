package org.example.dtos;

/** DTO para actualizar username, correo y/o pasaporte del usuario. */
public class ActualizarCredencialesRequestDTO {
    private String username;
    private String correo;
    private String pasaporte;

    public String getUsername()         { return username; }
    public void   setUsername(String v) { this.username = v; }

    public String getCorreo()         { return correo; }
    public void   setCorreo(String v) { this.correo = v; }

    public String getPasaporte()         { return pasaporte; }
    public void   setPasaporte(String v) { this.pasaporte = v; }
}
