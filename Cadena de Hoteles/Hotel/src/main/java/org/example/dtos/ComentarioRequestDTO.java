package org.example.dtos;

public class ComentarioRequestDTO {
    private int     hotelId;
    private Integer comentarioPadreId; // null si es comentario raíz
    private Integer resena;            // null si es respuesta a otro comentario
    private String  contenido;

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public Integer getComentarioPadreId() { return comentarioPadreId; }
    public void setComentarioPadreId(Integer comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }

    public Integer getResena() { return resena; }
    public void setResena(Integer resena) { this.resena = resena; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}