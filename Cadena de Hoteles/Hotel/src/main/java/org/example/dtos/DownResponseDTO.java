package org.example.dtos;

public class DownResponseDTO {
    private int    id;
    private int    comentarioId;
    private int    valor;
    private String fecha;
    private int    hotelId;
    private String contenidoComentario;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComentarioId() { return comentarioId; }
    public void setComentarioId(int comentarioId) { this.comentarioId = comentarioId; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getContenidoComentario() { return contenidoComentario; }
    public void setContenidoComentario(String contenidoComentario) { this.contenidoComentario = contenidoComentario; }
}