package org.example.services;

import org.example.dtos.TokenAerolineaRequestDTO;
import org.example.dtos.TokenAerolineaResponseDTO;
import org.example.repositories.AerolineaAliadaRepository;
import org.example.repositories.TokenAerolineaRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service encargado de generar tokens de alianza para aerolineas.
 * Valida la identidad de la aerolinea, resuelve la ciudad destino
 * y persiste el token con una ventana de expiracion de 15 minutos.
 */
public class TokenAerolineaService {

    private final TokenAerolineaRepository tokenRepository;
    private final AerolineaAliadaRepository aerolineaRepository;

    /**
     * Crea una instancia de TokenAerolineaService con sus dependencias inyectadas.
     */
    public TokenAerolineaService(TokenAerolineaRepository tokenRepository,
                                 AerolineaAliadaRepository aerolineaRepository) {
        this.tokenRepository    = tokenRepository;
        this.aerolineaRepository = aerolineaRepository;
    }

    /**
     * Genera y persiste un token de alianza para una aerolinea autenticada.
     * El token expira a los 15 minutos y no tiene reservacion asociada todavia.
     * La URL de redireccion se construye con el token para que la aerolinea
     * pueda enviarsela directamente al usuario.
     *
     * @param request datos de la solicitud: ciudad y pais destino del pasajero.
     * @param tokenHash hash del token de la aerolinea enviado en el header.
     * @return TokenAerolineaResponseDTO con el token generado, URL de redireccion y fecha de expiracion.
     * @throws IllegalArgumentException si el token no corresponde a una aerolinea activa
     *                                  o si la ciudad no existe en la base de datos.
     */
    public TokenAerolineaResponseDTO generarToken(TokenAerolineaRequestDTO request, String tokenHash) {

        // Valida que el token corresponda a una aerolinea aliada activa
        Integer aerolineaId = tokenRepository.obtenerAerolineaIdPorToken(tokenHash);
        if (aerolineaId == null) {
            throw new IllegalArgumentException("Token invalido o aerolinea no activa");
        }

        // Resuelve el ID de la ciudad destino
        Integer ciudadId = tokenRepository.buscarCiudadId(request.getCiudad(), request.getPais());
        if (ciudadId == null) {
            throw new IllegalArgumentException(
                    "No se encontro la ciudad '" + request.getCiudad() +
                            "' en el pais '" + request.getPais() + "'"
            );
        }

        // Genera el token de un solo uso y calcula su expiracion
        String token            = UUID.randomUUID().toString();
        LocalDateTime ahora     = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(15);
        Timestamp tsExpiracion  = Timestamp.valueOf(expiracion);

        tokenRepository.insertarToken(aerolineaId, ciudadId, token, tsExpiracion);

        // Obtiene la URLParaUsuario de la aerolinea para construir la URL de redireccion
        String urlBase = aerolineaRepository.obtenerAerolineaPorToken(tokenHash).getUrlAerolinea();
        String urlRedireccion = urlBase + "?token=" + token;

        String fechaFormateada = expiracion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new TokenAerolineaResponseDTO(token, urlRedireccion, fechaFormateada);
    }
}