package org.example.dtos;

public class HabitacionAgenciaResponseDTO {
    private int habitacionId;
    private double precioPorNoche;
    private double precioPorPersona;
    private int personasExtra;
    private int noches;
    private double total;

    public int getHabitacionId()              { return habitacionId; }
    public void setHabitacionId(int v)        { this.habitacionId = v; }
    public double getPrecioPorNoche()          { return precioPorNoche; }
    public void setPrecioPorNoche(double v)    { this.precioPorNoche = v; }
    public double getPrecioPorPersona()        { return precioPorPersona; }
    public void setPrecioPorPersona(double v)  { this.precioPorPersona = v; }
    public int getPersonasExtra()              { return personasExtra; }
    public void setPersonasExtra(int v)        { this.personasExtra = v; }
    public int getNoches()                     { return noches; }
    public void setNoches(int v)              { this.noches = v; }
    public double getTotal()                   { return total; }
    public void setTotal(double v)            { this.total = v; }
}