package org.example.dtos;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public Integer getComentarioPadreId() { return comentarioPadreId; }
    public void setComentarioPadreId(Integer comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }

    public Integer getResena() { return resena; }
    public void setResena(Integer resena) { this.resena = resena; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getDowns() { return downs; }
    public void setDowns(int downs) { this.downs = downs; }
}