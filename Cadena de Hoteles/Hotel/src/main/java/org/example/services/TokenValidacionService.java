package org.example.services;

import org.example.dtos.TokenValidacionResponseDTO;
import org.example.repositories.TokenValidacionRepository;

/**
 * Service encargado de validar tokens de alianza enviados por usuarios
 * que llegan desde una aerolinea aliada.
 */
public class TokenValidacionService {

    private final TokenValidacionRepository repository;

    /**
     * Crea una instancia de TokenValidacionService con sus dependencias inyectadas.
     */
    public TokenValidacionService(TokenValidacionRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida un token de alianza y retorna los datos necesarios para
     * configurar la busqueda con descuento en el frontend.
     * No marca el token como usado; eso ocurre al momento del pago.
     *
     * @param token string UUID recibido desde la URL del usuario.
     * @return TokenValidacionResponseDTO con ciudad, pais, descuento y expiracion.
     * @throws IllegalArgumentException si el token no existe, ya fue usado o expiro.
     */
    public TokenValidacionResponseDTO validar(String token) {
        TokenValidacionResponseDTO resultado = repository.buscarTokenValido(token);
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "Token invalido, ya utilizado o expirado"
            );
        }
        return resultado;
    }
}