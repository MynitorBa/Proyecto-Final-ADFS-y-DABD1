package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO con los datos enviados por una agencia externa para iniciar el proceso de handshake.
 * Los campos se mapean desde snake_case del JSON mediante anotaciones de Jackson.
 */
public class HandshakeRequestDTO {

    @JsonProperty("token_entrada")
    private String tokenEntrada;

    @JsonProperty("url_agencia")
    private String urlAgencia;

    /**
     * Retorna el token de entrada proporcionado por la agencia para autenticarse.
     * @return token de entrada.
     */
    public String getTokenEntrada() { return tokenEntrada; }

    /**
     * Asigna el token de entrada proporcionado por la agencia para autenticarse.
     * @param tokenEntrada token de entrada.
     */
    public void setTokenEntrada(String tokenEntrada) { this.tokenEntrada = tokenEntrada; }

    /**
     * Retorna la URL base del sistema de la agencia externa.
     * @return URL de la agencia.
     */
    public String getUrlAgencia() { return urlAgencia; }

    /**
     * Asigna la URL base del sistema de la agencia externa.
     * @param urlAgencia URL de la agencia.
     */
    public void setUrlAgencia(String urlAgencia) { this.urlAgencia = urlAgencia; }
}