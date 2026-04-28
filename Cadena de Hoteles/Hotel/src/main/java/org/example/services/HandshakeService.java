package org.example.services;

import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.helpers.TokenHelper;
import org.example.repositories.AgenciaRepository;

/**
 * Service para el proceso de handshake entre el sistema y una agencia externa.
 * Valida la URL de la agencia, genera un token de salida y persiste ambos tokens.
 */
public class HandshakeService {

    private final AgenciaRepository repo;

    /**
     * Crea una instancia de HandshakeService con sus dependencias inyectadas.
     */
    public HandshakeService(AgenciaRepository repo) {
        this.repo = repo;
    }

    /**
     * Procesa el handshake de una agencia externa.
     * Busca la agencia por su URL, genera un token de respuesta,
     * guarda los tokens y retorna el porcentaje de ganancia configurado.
     * @param dto datos del handshake con la URL de la agencia y el token de entrada.
     * @return DTO con el token de salida y el porcentaje de ganancia de la agencia.
     * @throws IllegalArgumentException si no existe una agencia con esa URL o si los tokens no se pudieron guardar.
     */
    public HandshakeResponseDTO procesarHandshake(HandshakeRequestDTO dto) {

        // Busca la agencia registrada con esa URL, obteniendo su ID y porcentaje
        org.example.dtos.AgenciaDTO agencia = repo.obtenerAgenciaConPorcentajePorURL(dto.getUrlAgencia());
        if (agencia == null)
            throw new IllegalArgumentException("No se encontro ninguna agencia registrada con esa URL.");

        // Genera el token que se devuelve a la agencia
        String tokenSalida = TokenHelper.generarTokenHash();

        // Persiste ambos tokens vinculados a la agencia
        boolean guardado = repo.guardarTokens(agencia.getId(), dto.getTokenEntrada(), tokenSalida);
        if (!guardado)
            throw new IllegalArgumentException("No se pudieron guardar los tokens.");

        return new HandshakeResponseDTO(tokenSalida, agencia.getPorcentajeDescuento());
    }
}