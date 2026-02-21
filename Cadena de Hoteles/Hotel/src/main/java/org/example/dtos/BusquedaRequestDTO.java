package org.example.dtos;

public class BusquedaRequestDTO {
    private String pais;
    private String ciudad;
    private String fechaCheckIn;   // "YYYY-MM-DD"
    private String fechaCheckOut;  // "YYYY-MM-DD"
    private int    cantidadPersonas;

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getFechaCheckIn() { return fechaCheckIn; }
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    public String getFechaCheckOut() { return fechaCheckOut; }
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }

    public int getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
}