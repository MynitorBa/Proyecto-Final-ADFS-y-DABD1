package org.example.dtos;

import java.util.List;

/**
 * DTO que representa un tipo de habitacion para el panel de administracion.
 * Incluye todos los datos del tipo, el nombre del tipo de cama y los IDs
 * de imagenes asociadas para gestion desde el admin.
 */
public class TipoHabitacionAdminDTO {

    /** ID del tipo de habitacion. */
    private int id;

    /** Nombre del tipo (ej. "Doble", "Suite", "Gran Suite"). */
    private String nombre;

    /** Precio por persona adicional. */
    private double precioPorPersona;

    /** Precio base por noche. */
    private double precioPorNoche;

    /** Capacidad maxima de personas del tipo. */
    private int capacidadMaxima;

    /** ID del tipo de cama asignado. */
    private int tipoCamaId;

    /** Nombre del tipo de cama (de la tabla Cama). */
    private String tipoCama;

    /** Metros cuadrados de la habitacion. */
    private double metrosCuadrados;

    /** IDs de las imagenes asociadas a este tipo de habitacion. */
    private List<Integer> imagenesIds;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecioPorPersona() { return precioPorPersona; }
    public void setPrecioPorPersona(double precioPorPersona) { this.precioPorPersona = precioPorPersona; }

    public double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public int getTipoCamaId() { return tipoCamaId; }
    public void setTipoCamaId(int tipoCamaId) { this.tipoCamaId = tipoCamaId; }

    public String getTipoCama() { return tipoCama; }
    public void setTipoCama(String tipoCama) { this.tipoCama = tipoCama; }

    public double getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(double metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public List<Integer> getImagenesIds() { return imagenesIds; }
    public void setImagenesIds(List<Integer> imagenesIds) { this.imagenesIds = imagenesIds; }
}