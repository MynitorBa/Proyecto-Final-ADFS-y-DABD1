package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO con la respuesta del servidor al proceso de handshake de una agencia externa.
 * Contiene el token de salida que la agencia debera usar en peticiones posteriores.
 */
public class HandshakeResponseDTO {

    @JsonProperty("token_salida")
    private String tokenSalida;

    /**
     * Constructor que inicializa la respuesta con el token generado por el servidor.
     * @param tokenSalida token de salida que la agencia usara para autenticarse en siguientes peticiones.
     */
    public HandshakeResponseDTO(String tokenSalida) {
        this.tokenSalida = tokenSalida;
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
}