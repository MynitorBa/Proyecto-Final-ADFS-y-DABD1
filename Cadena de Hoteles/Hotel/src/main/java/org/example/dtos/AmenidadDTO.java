package org.example.dtos;

/**
 * DTO que representa una amenidad del catalogo del sistema.
 * Contiene el identificador y nombre de cada una de las amenidades disponibles.
 */
public class AmenidadDTO {

    private int    id;
    private String nombre;

    /**
     * Constructor por defecto requerido para deserializacion.
     */
    public AmenidadDTO() {}

    /**
     * Constructor para crear una amenidad con todos sus datos.
     * @param id     identificador unico de la amenidad.
     * @param nombre nombre descriptivo de la amenidad.
     */
    public AmenidadDTO(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Retorna el identificador unico de la amenidad.
     * @return ID de la amenidad.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre descriptivo de la amenidad.
     * @return nombre de la amenidad.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el identificador unico de la amenidad.
     * @param id ID de la amenidad.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre descriptivo de la amenidad.
     * @param nombre nombre de la amenidad.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }
}