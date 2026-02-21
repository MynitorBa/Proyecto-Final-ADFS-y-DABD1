package org.example.dtos;

public class LoginResponseDTO {
    private String mensaje;
    private String username;
    private int    rolId;

    public LoginResponseDTO(String mensaje, String username, int rolId) {
        this.mensaje   = mensaje;
        this.username  = username;
        this.rolId     = rolId;
    }

    public String getMensaje()  { return mensaje; }
    public String getUsername() { return username; }
    public int    getRolId()    { return rolId; }
}