package org.example.dtos;

public class HabitacionElegibleDTO {

    private int id;
    private String numeroHabitacion;
    private double precioPorNoche;

    public HabitacionElegibleDTO() {}

    public HabitacionElegibleDTO(int id, String numeroHabitacion, double precioPorNoche) {
        this.id = id;
        this.numeroHabitacion = numeroHabitacion;
        this.precioPorNoche = precioPorNoche;
    }

    public int getId() { return id; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public double getPrecioPorNoche() { return precioPorNoche; }

    public void setId(int id) { this.id = id; }
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }
}
