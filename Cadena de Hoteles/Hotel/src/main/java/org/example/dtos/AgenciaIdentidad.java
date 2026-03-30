package org.example.dtos;

public class AgenciaIdentidad {
    private int id;
    private String nombre;
    private String urlAgencia;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUrlAgencia() { return urlAgencia; }
    public void setUrlAgencia(String urlAgencia) { this.urlAgencia = urlAgencia; }
}