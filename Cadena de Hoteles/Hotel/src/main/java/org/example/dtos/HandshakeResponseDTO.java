package org.example.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HandshakeResponseDTO {

    @JsonProperty("token_salida")
    private String tokenSalida;

    public HandshakeResponseDTO(String tokenSalida) {
        this.tokenSalida = tokenSalida;
    }

    public String getTokenSalida() { return tokenSalida; }
    public void setTokenSalida(String tokenSalida) { this.tokenSalida = tokenSalida; }
}