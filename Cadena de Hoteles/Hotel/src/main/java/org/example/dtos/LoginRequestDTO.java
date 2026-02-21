package org.example.dtos;

public class LoginRequestDTO {
    private String identificador; // puede ser username o correo
    private String contrasena;

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}