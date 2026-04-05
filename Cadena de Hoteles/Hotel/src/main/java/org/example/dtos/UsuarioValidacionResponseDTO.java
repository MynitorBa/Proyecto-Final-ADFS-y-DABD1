package org.example.dtos;

/**
 * DTO con el resultado de validar si un username, correo o pasaporte ya existen en el sistema.
 * Se usa antes del registro para informar al cliente de duplicados.
 */
public class UsuarioValidacionResponseDTO {

    private boolean usernameExiste;
    private boolean correoExiste;
    private boolean pasaporteExiste;

    /**
     * Constructor que inicializa el resultado de la validacion de duplicados.
     * @param usernameExiste  true si el username ya esta registrado en el sistema.
     * @param correoExiste    true si el correo ya esta registrado en el sistema.
     * @param pasaporteExiste true si el pasaporte ya esta registrado en el sistema.
     */
    public UsuarioValidacionResponseDTO(boolean usernameExiste, boolean correoExiste, boolean pasaporteExiste) {
        this.usernameExiste  = usernameExiste;
        this.correoExiste    = correoExiste;
        this.pasaporteExiste = pasaporteExiste;
    }

    /**
     * Retorna si el username ya existe en el sistema.
     * @return true si el username esta registrado, false en caso contrario.
     */
    public boolean isUsernameExiste() { return usernameExiste; }

    /**
     * Retorna si el correo ya existe en el sistema.
     * @return true si el correo esta registrado, false en caso contrario.
     */
    public boolean isCorreoExiste() { return correoExiste; }

    /**
     * Retorna si el pasaporte ya existe en el sistema.
     * @return true si el pasaporte esta registrado, false en caso contrario.
     */
    public boolean isPasaporteExiste() { return pasaporteExiste; }
}