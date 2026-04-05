package org.example.dtos;

/**
 * DTO que representa una ciudad del catalogo del sistema.
 * Incluye el nombre del pais al que pertenece resuelto mediante join.
 */
public class CiudadDTO {

    private int    id;
    private String nombre;
    private int    paisId;
    private String paisNombre;

    /**
     * Constructor por defecto requerido para deserializacion.
     */
    public CiudadDTO() {}

    /**
     * Retorna el identificador unico de la ciudad.
     * @return ID de la ciudad.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre de la ciudad.
     * @return nombre de la ciudad.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el ID del pais al que pertenece la ciudad.
     * @return ID del pais.
     */
    public int getPaisId() { return paisId; }

    /**
     * Retorna el nombre del pais al que pertenece la ciudad.
     * @return nombre del pais.
     */
    public String getPaisNombre() { return paisNombre; }

    /**
     * Asigna el identificador unico de la ciudad.
     * @param id ID de la ciudad.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre de la ciudad.
     * @param nombre nombre de la ciudad.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el ID del pais al que pertenece la ciudad.
     * @param paisId ID del pais.
     */
    public void setPaisId(int paisId) { this.paisId = paisId; }

    /**
     * Asigna el nombre del pais al que pertenece la ciudad.
     * @param paisNombre nombre del pais.
     */
    public void setPaisNombre(String paisNombre) { this.paisNombre = paisNombre; }
}