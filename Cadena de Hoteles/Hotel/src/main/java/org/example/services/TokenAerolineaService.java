package org.example.services;

import org.example.dtos.TokenAerolineaRequestDTO;
import org.example.dtos.TokenAerolineaResponseDTO;
import org.example.repositories.AerolineaAliadaRepository;
import org.example.repositories.TokenAerolineaRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
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

        // Valida que las fechas no sean nulas o vacias
        if (request.getFechaIda() == null || request.getFechaIda().isBlank()) {
            throw new IllegalArgumentException("La fecha de ida es obligatoria");
        }
        if (request.getFechaVuelta() == null || request.getFechaVuelta().isBlank()) {
            throw new IllegalArgumentException("La fecha de vuelta es obligatoria");
        }

        // Convierte String a LocalDate (espera formato yyyy-MM-dd)
        LocalDate fechaIda    = LocalDate.parse(request.getFechaIda());
        LocalDate fechaVuelta = LocalDate.parse(request.getFechaVuelta());

        // Valida que la fecha de vuelta sea posterior a la de ida
        if (!fechaVuelta.isAfter(fechaIda)) {
            throw new IllegalArgumentException("La fecha de vuelta debe ser posterior a la fecha de ida");
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
        String token             = UUID.randomUUID().toString();
        LocalDateTime ahora      = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(15);
        Timestamp tsExpiracion   = Timestamp.valueOf(expiracion);

        // Convierte LocalDate a java.sql.Date para Oracle
        java.sql.Date sqlFechaIda    = java.sql.Date.valueOf(fechaIda);
        java.sql.Date sqlFechaVuelta = java.sql.Date.valueOf(fechaVuelta);

        tokenRepository.insertarToken(aerolineaId, ciudadId, token, tsExpiracion, sqlFechaIda, sqlFechaVuelta);

        String urlBase         = aerolineaRepository.obtenerAerolineaPorToken(tokenHash).getUrlAerolinea();
        String urlRedireccion  = urlBase + "?token=" + token;
        String fechaFormateada = expiracion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return new TokenAerolineaResponseDTO(token, urlRedireccion, fechaFormateada);
    }
}