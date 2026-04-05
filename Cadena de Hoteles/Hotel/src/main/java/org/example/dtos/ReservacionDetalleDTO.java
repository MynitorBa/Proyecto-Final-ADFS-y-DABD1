package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos completos de una reservacion y su detalle de habitacion.
 * Incluye informacion del hotel, fechas, estado y IDs de imagenes asociadas.
 */
public class ReservacionDetalleDTO {

    private int    id;
    private String noReservacion;
    private double total;
    private String estado;
    private String fechaCreacion;
    private String fechaExpiracion;
    private String fechaCancelacion;
    private String motivoCancelacion;

    // Detalle de habitacion
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

    // IDs de imagenes
    private List<Integer> imagenesHotelIds;
    private List<Integer> imagenesHabitacionIds;

    /**
     * Retorna el identificador unico de la reservacion.
     * @return ID de la reservacion.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico de la reservacion.
     * @param id ID de la reservacion.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el numero de reservacion generado por el sistema.
     * @return numero de reservacion.
     */
    public String getNoReservacion() { return noReservacion; }

    /**
     * Asigna el numero de reservacion generado por el sistema.
     * @param noReservacion numero de reservacion.
     */
    public void setNoReservacion(String noReservacion) { this.noReservacion = noReservacion; }

    /**
     * Retorna el monto total de la reservacion.
     * @return total de la reservacion.
     */
    public double getTotal() { return total; }

    /**
     * Asigna el monto total de la reservacion.
     * @param total total de la reservacion.
     */
    public void setTotal(double total) { this.total = total; }

    /**
     * Retorna el nombre del estado actual de la reservacion.
     * @return nombre del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Asigna el nombre del estado actual de la reservacion.
     * @param estado nombre del estado.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna la fecha en que se creo la reservacion.
     * @return fecha de creacion.
     */
    public String getFechaCreacion() { return fechaCreacion; }

    /**
     * Asigna la fecha en que se creo la reservacion.
     * @param fechaCreacion fecha de creacion.
     */
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    /**
     * Retorna la fecha en que expira la reservacion si no es pagada.
     * @return fecha de expiracion.
     */
    public String getFechaExpiracion() { return fechaExpiracion; }

    /**
     * Asigna la fecha en que expira la reservacion si no es pagada.
     * @param fechaExpiracion fecha de expiracion.
     */
    public void setFechaExpiracion(String fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    /**
     * Retorna la fecha en que se cancelo la reservacion.
     * @return fecha de cancelacion, o null si no fue cancelada.
     */
    public String getFechaCancelacion() { return fechaCancelacion; }

    /**
     * Asigna la fecha en que se cancelo la reservacion.
     * @param fechaCancelacion fecha de cancelacion.
     */
    public void setFechaCancelacion(String fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    /**
     * Retorna el motivo por el que se cancelo la reservacion.
     * @return motivo de cancelacion, o null si no fue cancelada.
     */
    public String getMotivoCancelacion() { return motivoCancelacion; }

    /**
     * Asigna el motivo por el que se cancelo la reservacion.
     * @param motivoCancelacion motivo de cancelacion.
     */
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    /**
     * Retorna el identificador unico del detalle de la reservacion.
     * @return ID del detalle.
     */
    public int getDetalleId() { return detalleId; }

    /**
     * Asigna el identificador unico del detalle de la reservacion.
     * @param detalleId ID del detalle.
     */
    public void setDetalleId(int detalleId) { this.detalleId = detalleId; }

    /**
     * Retorna el ID de la habitacion reservada.
     * @return ID de la habitacion.
     */
    public int getHabitacionId() { return habitacionId; }

    /**
     * Asigna el ID de la habitacion reservada.
     * @param habitacionId ID de la habitacion.
     */
    public void setHabitacionId(int habitacionId) { this.habitacionId = habitacionId; }

    /**
     * Retorna el numero o identificador de la habitacion dentro del hotel.
     * @return numero de la habitacion.
     */
    public String getNumeroHabitacion() { return numeroHabitacion; }

    /**
     * Asigna el numero o identificador de la habitacion dentro del hotel.
     * @param numeroHabitacion numero de la habitacion.
     */
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    /**
     * Retorna la fecha de entrada en la habitacion.
     * @return fecha de check-in.
     */
    public String getFechaCheckIn() { return fechaCheckIn; }

    /**
     * Asigna la fecha de entrada en la habitacion.
     * @param fechaCheckIn fecha de check-in.
     */
    public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    /**
     * Retorna la fecha de salida de la habitacion.
     * @return fecha de check-out.
     */
    public String getFechaCheckOut() { return fechaCheckOut; }

    /**
     * Asigna la fecha de salida de la habitacion.
     * @param fechaCheckOut fecha de check-out.
     */
    public void setFechaCheckOut(String fechaCheckOut) { this.fechaCheckOut = fechaCheckOut; }

    /**
     * Retorna la cantidad de personas que ocupan la habitacion.
     * @return cantidad de personas.
     */
    public int getCantidadPersonas() { return cantidadPersonas; }

    /**
     * Asigna la cantidad de personas que ocupan la habitacion.
     * @param cantidadPersonas cantidad de personas.
     */
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }

    /**
     * Retorna el subtotal correspondiente a este detalle de habitacion.
     * @return total del detalle.
     */
    public double getTotalDetalle() { return totalDetalle; }

    /**
     * Asigna el subtotal correspondiente a este detalle de habitacion.
     * @param totalDetalle total del detalle.
     */
    public void setTotalDetalle(double totalDetalle) { this.totalDetalle = totalDetalle; }

    /**
     * Retorna la descripcion de la habitacion reservada.
     * @return descripcion de la habitacion.
     */
    public String getDescripcionHabitacion() { return descripcionHabitacion; }

    /**
     * Asigna la descripcion de la habitacion reservada.
     * @param descripcionHabitacion descripcion de la habitacion.
     */
    public void setDescripcionHabitacion(String descripcionHabitacion) { this.descripcionHabitacion = descripcionHabitacion; }

    /**
     * Retorna el nombre del tipo de habitacion reservada.
     * @return tipo de habitacion.
     */
    public String getTipoHabitacion() { return tipoHabitacion; }

    /**
     * Asigna el nombre del tipo de habitacion reservada.
     * @param tipoHabitacion tipo de habitacion.
     */
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }

    /**
     * Retorna el tipo de cama de la habitacion reservada.
     * @return tipo de cama.
     */
    public String getTipoCama() { return tipoCama; }

    /**
     * Asigna el tipo de cama de la habitacion reservada.
     * @param tipoCama tipo de cama.
     */
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    /**
     * Retorna el ID del hotel al que pertenece la habitacion reservada.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Asigna el ID del hotel al que pertenece la habitacion reservada.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Retorna el nombre del hotel al que pertenece la habitacion reservada.
     * @return nombre del hotel.
     */
    public String getNombreHotel() { return nombreHotel; }

    /**
     * Asigna el nombre del hotel al que pertenece la habitacion reservada.
     * @param nombreHotel nombre del hotel.
     */
    public void setNombreHotel(String nombreHotel) { this.nombreHotel = nombreHotel; }

    /**
     * Retorna los IDs de imagenes asociadas al hotel.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> getImagenesHotelIds() { return imagenesHotelIds; }

    /**
     * Asigna los IDs de imagenes asociadas al hotel.
     * @param imagenesHotelIds lista de IDs de imagenes del hotel.
     */
    public void setImagenesHotelIds(List<Integer> imagenesHotelIds) { this.imagenesHotelIds = imagenesHotelIds; }

    /**
     * Retorna los IDs de imagenes asociadas a la habitacion.
     * @return lista de IDs de imagenes de la habitacion.
     */
    public List<Integer> getImagenesHabitacionIds() { return imagenesHabitacionIds; }

    /**
     * Asigna los IDs de imagenes asociadas a la habitacion.
     * @param imagenesHabitacionIds lista de IDs de imagenes de la habitacion.
     */
    public void setImagenesHabitacionIds(List<Integer> imagenesHabitacionIds) { this.imagenesHabitacionIds = imagenesHabitacionIds; }
}