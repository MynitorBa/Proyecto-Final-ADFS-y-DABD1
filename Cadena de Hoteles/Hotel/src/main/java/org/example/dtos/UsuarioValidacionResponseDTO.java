package org.example.dtos;

public class UsuarioValidacionResponseDTO {
    private boolean usernameExiste;
    private boolean correoExiste;
    private boolean pasaporteExiste;

    public UsuarioValidacionResponseDTO(boolean usernameExiste, boolean correoExiste, boolean pasaporteExiste) {
        this.usernameExiste = usernameExiste;
        this.correoExiste = correoExiste;
        this.pasaporteExiste = pasaporteExiste;
    }

    public boolean isUsernameExiste() { return usernameExiste; }
    public boolean isCorreoExiste() { return correoExiste; }
    public boolean isPasaporteExiste() { return pasaporteExiste; }
}