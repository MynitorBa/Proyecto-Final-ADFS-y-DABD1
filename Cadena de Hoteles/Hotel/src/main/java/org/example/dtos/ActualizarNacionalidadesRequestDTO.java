package org.example.dtos;

import java.util.List;

/** DTO para reemplazar todas las nacionalidades del usuario. */
public class ActualizarNacionalidadesRequestDTO {
    private List<String> nacionalidades;

    public List<String> getNacionalidades()         { return nacionalidades; }
    public void         setNacionalidades(List<String> v) { this.nacionalidades = v; }
}
