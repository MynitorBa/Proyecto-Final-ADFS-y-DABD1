package org.example.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HandshakeRequestDTO {

    @JsonProperty("token_entrada")
    private String tokenEntrada;

    @JsonProperty("url_agencia")
    private String urlAgencia;

    public String getTokenEntrada() { return tokenEntrada; }
    public void setTokenEntrada(String tokenEntrada) { this.tokenEntrada = tokenEntrada; }

    public String getUrlAgencia() { return urlAgencia; }
    public void setUrlAgencia(String urlAgencia) { this.urlAgencia = urlAgencia; }
}