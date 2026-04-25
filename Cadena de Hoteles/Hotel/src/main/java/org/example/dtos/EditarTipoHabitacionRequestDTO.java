package org.example.dtos;

/**
 * DTO de request para modificar los precios de un tipo de habitacion.
 * Solo permite actualizar precioPorPersona y precioPorNoche;
 * el resto de campos del tipo (nombre, capacidad, cama, metros) son de solo lectura.
 */
public class EditarTipoHabitacionRequestDTO {

    /**
     * Nuevo precio por persona adicional.
     * Debe ser mayor que 0.
     */
    private double precioPorPersona;

    /**
     * Nuevo precio base por noche.
     * Debe ser mayor que 0.
     */
    private double precioPorNoche;

    public double getPrecioPorPersona() { return precioPorPersona; }
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    public double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }
}