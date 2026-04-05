package org.example.dtos;

/**
 * DTO con el resultado de verificar si una reservacion puede ser cancelada.
 * Incluye un indicador booleano y la razon en caso de que no sea posible cancelar.
 */
public class PuedeCancelarDTO {

    private boolean puedeCancelar;
    private String  razon;

    /**
     * Constructor que inicializa el resultado de la verificacion de cancelacion.
     * @param puedeCancelar indica si la reservacion puede cancelarse.
     * @param razon         razon por la que no puede cancelarse, null si si puede.
     */
    public PuedeCancelarDTO(boolean puedeCancelar, String razon) {
        this.puedeCancelar = puedeCancelar;
        this.razon         = razon;
    }

    /**
     * Retorna si la reservacion puede ser cancelada.
     * @return true si puede cancelarse, false en caso contrario.
     */
    public boolean isPuedeCancelar() { return puedeCancelar; }

    /**
     * Retorna la razon por la que la reservacion no puede cancelarse.
     * @return razon de la restriccion, o null si la cancelacion es posible.
     */
    public String getRazon() { return razon; }
}