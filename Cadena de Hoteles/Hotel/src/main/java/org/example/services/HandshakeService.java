package org.example.services;

import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.helpers.TokenHelper;
import org.example.repositories.AgenciaRepository;

public class HandshakeService {

    private final AgenciaRepository repo = new AgenciaRepository();

    public HandshakeResponseDTO procesarHandshake(HandshakeRequestDTO dto) {

        // 1. Buscar agencia por su URL
        Integer agenciaId = repo.obtenerAgenciaIdPorURL(dto.getUrlAgencia());
        if (agenciaId == null)
            throw new IllegalArgumentException("No se encontró ninguna agencia registrada con esa URL.");

        // 2. Generar token de salida
        String tokenSalida = TokenHelper.generarTokenHash();

        // 3. Guardar ambos tokens
        boolean guardado = repo.guardarTokens(agenciaId, dto.getTokenEntrada(), tokenSalida);
        if (!guardado)
            throw new IllegalArgumentException("No se pudieron guardar los tokens.");

        return new HandshakeResponseDTO(tokenSalida);
    }
}