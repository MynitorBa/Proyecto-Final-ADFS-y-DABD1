package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO con la respuesta del servidor al proceso de handshake de una agencia externa.
 * Contiene el token de salida que la agencia debera usar en peticiones posteriores,
 * y el porcentaje de ganancia configurado para la agencia.
 */
public class HandshakeResponseDTO {

    @JsonProperty("token_salida")
    private String tokenSalida;

    @JsonProperty("porcentajeGanancia")
    private double porcentajeGanancia;

    /**
     * Constructor que inicializa la respuesta con el token generado por el servidor.
     * @param tokenSalida token de salida que la agencia usara para autenticarse en siguientes peticiones.
     */
    public HandshakeResponseDTO(String tokenSalida) {
        this.tokenSalida = tokenSalida;
        this.porcentajeGanancia = 0;
    }

    /**
     * Constructor completo que inicializa la respuesta con token y porcentaje de ganancia.
     * @param tokenSalida token de salida que la agencia usara para autenticarse en siguientes peticiones.
     * @param porcentajeGanancia porcentaje de ganancia configurado para la agencia.
     */
    public HandshakeResponseDTO(String tokenSalida, double porcentajeGanancia) {
        this.tokenSalida = tokenSalida;
        this.porcentajeGanancia = porcentajeGanancia;
    }

    /**
     * Retorna el token de salida generado por el servidor para la agencia.
     * @return token de salida.
     */
    public String getTokenSalida() { return tokenSalida; }

    /**
     * Asigna el token de salida generado por el servidor para la agencia.
     * @param tokenSalida token de salida.
     */
    public void setTokenSalida(String tokenSalida) { this.tokenSalida = tokenSalida; }

    /**
     * Retorna el porcentaje de ganancia configurado para la agencia.
     * @return porcentaje de ganancia.
     */
    public double getPorcentajeGanancia() { return porcentajeGanancia; }

    /**
     * Asigna el porcentaje de ganancia para la agencia.
     * @param porcentajeGanancia porcentaje de ganancia.
     */
    public void setPorcentajeGanancia(double porcentajeGanancia) { this.porcentajeGanancia = porcentajeGanancia; }
}