package org.example.dtos;

/**
 * DTO con los datos completos de un down para retornar al cliente.
 * Incluye informacion del comentario valorado y el hotel al que pertenece.
 */
public class DownResponseDTO {

    private int    id;
    private int    comentarioId;
    private int    valor;
    private String fecha;
    private int    hotelId;
    private String contenidoComentario;

    /**
     * Retorna el identificador unico del down.
     * @return ID del down.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico del down.
     * @param id ID del down.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el ID del comentario sobre el que se aplico el down.
     * @return ID del comentario.
     */
    public int getComentarioId() { return comentarioId; }

    /**
     * Asigna el ID del comentario sobre el que se aplico el down.
     * @param comentarioId ID del comentario.
     */
    public void setComentarioId(int comentarioId) { this.comentarioId = comentarioId; }

    /**
     * Retorna el valor del down, que puede ser 1 o -1.
     * @return valor del down.
     */
    public int getValor() { return valor; }

    /**
     * Asigna el valor del down, que puede ser 1 o -1.
     * @param valor valor del down.
     */
    public void setValor(int valor) { this.valor = valor; }

    /**
     * Retorna la fecha en que se registro el down.
     * @return fecha del down.
     */
    public String getFecha() { return fecha; }

    /**
     * Asigna la fecha en que se registro el down.
     * @param fecha fecha del down.
     */
    public void setFecha(String fecha) { this.fecha = fecha; }

    /**
     * Retorna el ID del hotel al que pertenece el comentario valorado.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Asigna el ID del hotel al que pertenece el comentario valorado.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Retorna el texto del comentario sobre el que se aplico el down.
     * @return contenido del comentario.
     */
    public String getContenidoComentario() { return contenidoComentario; }

    /**
     * Asigna el texto del comentario sobre el que se aplico el down.
     * @param contenidoComentario contenido del comentario.
     */
    public void setContenidoComentario(String contenidoComentario) { this.contenidoComentario = contenidoComentario; }
}