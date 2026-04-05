package org.example.dtos;

/**
 * DTO que representa un pais del catalogo del sistema.
 */
public class PaisDTO {

    private int    id;
    private String nombre;

    /**
     * Constructor por defecto requerido para deserializacion.
     */
    public PaisDTO() {}

    /**
     * Constructor para crear un pais con todos sus datos.
     * @param id     identificador unico del pais.
     * @param nombre nombre del pais.
     */
    public PaisDTO(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Retorna el identificador unico del pais.
     * @return ID del pais.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre del pais.
     * @return nombre del pais.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el identificador unico del pais.
     * @param id ID del pais.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre del pais.
     * @param nombre nombre del pais.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }
}