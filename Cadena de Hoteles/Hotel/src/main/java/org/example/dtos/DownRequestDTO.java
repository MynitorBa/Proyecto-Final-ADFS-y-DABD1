package org.example.dtos;

/**
 * DTO con el valor de un down aplicado a un comentario.
 * El valor debe ser 1 para down positivo o -1 para down negativo.
 */
public class DownRequestDTO {

    private int valor;

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
}