package org.example.dtos;

import java.util.List;

public class ReservacionDetalleDTO {
    private int    id;
    private String noReservacion;
    private double total;
    private String estado;
    private String fechaCreacion;
    private String fechaExpiracion;
    private String fechaCancelacion;
    private String motivoCancelacion;

    // Detalle de habitación
    private int    detalleId;
    private int    habitacionId;
    private String numeroHabitacion;
    private String fechaCheckIn;
    private String fechaCheckOut;
    private int    cantidadPersonas;
    private double totalDetalle;
    private String descripcionHabitacion;
    private String tipoHabitacion;
    private String tipoCama;

    // Hotel
    private int    hotelId;
    private String nombreHotel;

    // IDs de imágenes
    private List<Integer> imagenesHotelIds;
    private List<Integer> imagenesHabitacionIds;

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

    public String getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(String fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public int getDetalleId() { return detalleId; }
    public void setDetalleId(int detalleId) { this.detalleId = detalleId; }

    public int getHabitacionId() { return habitacionId; }
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }

    public String getNumeroHabitacion() { return numeroHabitacion; }
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    public String getFechaCheckIn() { return fechaCheckIn; }
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    public String getFechaCheckOut() { return fechaCheckOut; }
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }

    public int getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }

    public double getTotalDetalle() { return totalDetalle; }
    public void setTotalDetalle(double totalDetalle) { this.totalDetalle = totalDetalle; }

    public String getDescripcionHabitacion() { return descripcionHabitacion; }
    public void setDescripcionHabitacion(String descripcionHabitacion) { this.descripcionHabitacion = descripcionHabitacion; }

    public String getTipoHabitacion() { return tipoHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    public String getTipoCama() { return tipoCama; }
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getNombreHotel() { return nombreHotel; }
    public void setNombreHotel(String nombreHotel) { this.nombreHotel = nombreHotel; }

    public List<Integer> getImagenesHotelIds() { return imagenesHotelIds; }
    public void setImagenesHotelIds(List<Integer> imagenesHotelIds) { this.imagenesHotelIds = imagenesHotelIds; }

    public List<Integer> getImagenesHabitacionIds() { return imagenesHabitacionIds; }
    public void setImagenesHabitacionIds(List<Integer> imagenesHabitacionIds) { this.imagenesHabitacionIds = imagenesHabitacionIds; }
}