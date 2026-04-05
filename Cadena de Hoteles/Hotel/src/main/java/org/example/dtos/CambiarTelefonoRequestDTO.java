package org.example.dtos;

/**
 * DTO con el nuevo numero de telefono a asignar al usuario autenticado.
 */
public class CambiarTelefonoRequestDTO {

    private String telefono;

    /**
     * Retorna el nuevo numero de telefono del usuario.
     * @return numero de telefono.
     */
    public String getTelefono() { return telefono; }

    /**
     * Asigna el nuevo numero de telefono del usuario.
     * @param telefono numero de telefono.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }
}