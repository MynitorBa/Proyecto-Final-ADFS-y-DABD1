package org.example.dtos;

public class ReservacionResponseDTO {
    private int    id;
    private String noReservacion;
    private double total;
    private String estado;
    private String fechaCreacion;
    private String fechaExpiracion;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoReservacion() { return noReservacion; }
    public void setNoReservacion(String noReservacion) { this.noReservacion = noReservacion; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(String fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}