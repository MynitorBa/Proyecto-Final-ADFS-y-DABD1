package org.example.dtos;

/**
 * DTO con los datos completos de un comentario para retornar al cliente.
 * Incluye informacion del autor, contenido, resena y total de downs recibidos.
 */
public class ComentarioResponseDTO {

    private int     id;
    private int     usuarioId;
    private String  username;
    private int     hotelId;
    private Integer comentarioPadreId;
    private Integer resena;
    private String  contenido;
    private String  fecha;
    private int     downs;

    /**
     * Retorna el identificador unico del comentario.
     * @return ID del comentario.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico del comentario.
     * @param id ID del comentario.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el ID del usuario que publico el comentario.
     * @return ID del usuario.
     */
    public int getUsuarioId() { return usuarioId; }

    /**
     * Asigna el ID del usuario que publico el comentario.
     * @param usuarioId ID del usuario.
     */
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    /**
     * Retorna el nombre de usuario del autor del comentario.
     * @return username del autor.
     */
    public String getUsername() { return username; }

    /**
     * Asigna el nombre de usuario del autor del comentario.
     * @param username username del autor.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Retorna el ID del hotel al que pertenece el comentario.
     * @return ID del hotel.
     */
    public int getHotelId() { return hotelId; }

    /**
     * Asigna el ID del hotel al que pertenece el comentario.
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
     * Retorna la puntuacion de la resena, null si el comentario es una respuesta.
     * @return puntuacion de la resena, o null.
     */
    public Integer getResena() { return resena; }

    /**
     * Asigna la puntuacion de la resena, null si el comentario es una respuesta.
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

    /**
     * Retorna la fecha de publicacion del comentario.
     * @return fecha del comentario.
     */
    public String getFecha() { return fecha; }

    /**
     * Asigna la fecha de publicacion del comentario.
     * @param fecha fecha del comentario.
     */
    public void setFecha(String fecha) { this.fecha = fecha; }

    /**
     * Retorna la cantidad total de downs recibidos por el comentario.
     * @return total de downs.
     */
    public int getDowns() { return downs; }

    /**
     * Asigna la cantidad total de downs recibidos por el comentario.
     * @param downs total de downs.
     */
    public void setDowns(int downs) { this.downs = downs; }
}