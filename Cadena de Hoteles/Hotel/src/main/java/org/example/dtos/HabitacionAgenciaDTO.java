package org.example.dtos;

public class HabitacionAgenciaDTO extends HabitacionDTO {
    private double porcentajeDescuento;
    private double precioPorNocheConDescuento;
    private double precioPorPersonaConDescuento;

    public double getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public double getPrecioPorNocheConDescuento() { return precioPorNocheConDescuento; }
    public void setPrecioPorNocheConDescuento(double precioPorNocheConDescuento) { this.precioPorNocheConDescuento = precioPorNocheConDescuento; }

    public double getPrecioPorPersonaConDescuento() { return precioPorPersonaConDescuento; }
    public void setPrecioPorPersonaConDescuento(double precioPorPersonaConDescuento) { this.precioPorPersonaConDescuento = precioPorPersonaConDescuento; }
}