package org.example.dtos;

/**
 * DTO con el desglose de precios de una habitacion calculado para una agencia.
 * Incluye el total a pagar considerando noches y personas extra.
 */
public class HabitacionAgenciaResponseDTO {

    private int    detalleId;
    private int    habitacionId;
    private double precioPorNoche;
    private double precioPorPersona;
    private int    personasExtra;
    private int    noches;
    private double total;

    /**
     * Retorna el ID del detalle de reservacion creado por MIKU para esta habitacion.
     * Necesario para cambios de fechas posteriores.
     * @return ID del detalle.
     */
    public int getDetalleId() { return detalleId; }

    /**
     * Asigna el ID del detalle de reservacion creado por MIKU para esta habitacion.
     * @param detalleId ID del detalle.
     */
    public void setDetalleId(int detalleId) { this.detalleId = detalleId; }

    /**
     * Retorna el ID de la habitacion cotizada.
     * @return ID de la habitacion.
     */
    public int getHabitacionId() { return habitacionId; }

    /**
     * Asigna el ID de la habitacion cotizada.
     * @param habitacionId ID de la habitacion.
     */
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }

    /**
     * Retorna el precio base por noche aplicado a la reservacion.
     * @return precio por noche.
     */
    public double getPrecioPorNoche() { return precioPorNoche; }

    /**
     * Asigna el precio base por noche aplicado a la reservacion.
     * @param precioPorNoche precio por noche.
     */
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    /**
     * Retorna el precio adicional por persona extra sobre la capacidad base.
     * @return precio por persona adicional.
     */
    public double getPrecioPorPersona() { return precioPorPersona; }

    /**
     * Asigna el precio adicional por persona extra sobre la capacidad base.
     * @param precioPorPersona precio por persona adicional.
     */
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    /**
     * Retorna la cantidad de personas extra sobre la capacidad base de la habitacion.
     * @return personas extra.
     */
    public int getPersonasExtra() { return personasExtra; }

    /**
     * Asigna la cantidad de personas extra sobre la capacidad base de la habitacion.
     * @param personasExtra personas extra.
     */
    public void setPersonasExtra(int personasExtra) { this.personasExtra = personasExtra; }

    /**
     * Retorna el numero de noches de la estancia.
     * @return cantidad de noches.
     */
    public int getNoches() { return noches; }

    /**
     * Asigna el numero de noches de la estancia.
     * @param noches cantidad de noches.
     */
    public void setNoches(int noches) { this.noches = noches; }

    /**
     * Retorna el monto total a pagar considerando noches, personas extra y descuento de agencia.
     * @return total a pagar.
     */
    public double getTotal() { return total; }

    /**
     * Asigna el monto total a pagar considerando noches, personas extra y descuento de agencia.
     * @param total monto total a pagar.
     */
    public void setTotal(double total) { this.total = total; }
}