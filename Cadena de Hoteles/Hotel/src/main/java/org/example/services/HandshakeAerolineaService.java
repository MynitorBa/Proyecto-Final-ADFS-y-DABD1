package org.example.services;

import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.helpers.TokenHelper;
import org.example.repositories.AerolineaAliadaRepository;

/**
 * Service para el proceso de handshake entre el sistema hotelero y una aerolinea aliada externa.
 * Valida la URL de la aerolinea registrada en la tabla AerolineaAliado, genera un token de salida
 * y persiste ambos tokens para que la aerolinea pueda autenticarse en llamadas futuras.
 */
public class HandshakeAerolineaService {

    private final AerolineaAliadaRepository repo;

    /**
     * Crea una instancia de HandshakeAerolineaService con sus dependencias inyectadas.
     *
     * @param repo repositorio de aerolineas aliadas para consulta y actualizacion de tokens.
     */
    public HandshakeAerolineaService(AerolineaAliadaRepository repo) {
        this.repo = repo;
    }

    /**
     * Procesa el handshake de una aerolinea aliada externa.
     * Busca la aerolinea por su URL registrada en la base de datos, genera un token de respuesta
     * y guarda el token de entrada y el de salida vinculados a ese registro.
     *
     * @param dto datos del handshake con la URL de la aerolinea y el token de entrada.
     * @return DTO con el token de salida generado para que la aerolinea lo use en requests futuros.
     * @throws IllegalArgumentException si no existe una aerolinea registrada con esa URL
     *                                  o si los tokens no se pudieron persistir en la base de datos.
     */
    public HandshakeResponseDTO procesarHandshake(HandshakeRequestDTO dto) {

        // Busca la aerolinea aliada registrada con esa URL
        Integer aerolineaId = repo.obtenerAerolineaIdPorURL(dto.getUrlAgencia());
        if (aerolineaId == null)
            throw new IllegalArgumentException(
                    "No se encontro ninguna aerolinea registrada con la URL: " + dto.getUrlAgencia());

        // Genera el token que se devuelve a la aerolinea para sus llamadas futuras
        String tokenSalida = TokenHelper.generarTokenHash();

        // Persiste ambos tokens vinculados al registro de la aerolinea aliada
        boolean guardado = repo.guardarTokensAerolinea(aerolineaId, dto.getTokenEntrada(), tokenSalida);
        if (!guardado)
            throw new IllegalArgumentException(
                    "No se pudieron guardar los tokens de la aerolinea aliada.");

        return new HandshakeResponseDTO(tokenSalida);
    }
}