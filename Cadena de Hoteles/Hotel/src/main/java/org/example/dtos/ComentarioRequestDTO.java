package org.example.dtos;

/**
 * DTO con los datos necesarios para publicar un comentario o respuesta en un hotel.
 * Si comentarioPadreId es null se trata de un comentario raiz; si tiene valor es una respuesta.
 * La resena solo aplica para comentarios raiz, no para respuestas.
 */
public class ComentarioRequestDTO {

    private int     hotelId;
    private Integer comentarioPadreId;
    private Integer resena;
    private String  contenido;

    /**
     * Retorna el ID del hotel sobre el que se publica el comentario.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Asigna el ID del hotel sobre el que se publica el comentario.
     * @param hotelId ID del hotel.
     */
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    /**
     * Retorna el ID del comentario padre si es una respuesta, null si es un comentario raiz.
     * @return ID del comentario padre, o null.
     */
    public Integer getComentarioPadreId() { return comentarioPadreId; }

    /**
     * Asigna el ID del comentario padre si es una respuesta, null si es un comentario raiz.
     * @param comentarioPadreId ID del comentario padre, o null.
     */
    public void setComentarioPadreId(Integer comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }

    /**
     * Retorna la puntuacion de la resena del hotel, null si el comentario es una respuesta.
     * @return puntuacion de la resena, o null.
     */
    public Integer getResena() { return resena; }

    /**
     * Asigna la puntuacion de la resena del hotel, null si el comentario es una respuesta.
     * @param resena puntuacion de la resena, o null.
     */
    public void setResena(Integer resena) { this.resena = resena; }

    /**
     * Retorna el texto del comentario o respuesta.
     * @return contenido del comentario.
     */
    public String getContenido() { return contenido; }

    /**
     * Asigna el texto del comentario o respuesta.
     * @param contenido contenido del comentario.
     */
    public void setContenido(String contenido) { this.contenido = contenido; }
}