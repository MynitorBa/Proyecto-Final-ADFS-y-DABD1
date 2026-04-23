package org.example.services;

import org.example.dtos.TokenValidacionResponseDTO;
import org.example.repositories.TokenValidacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para TokenValidacionService.
 * Mockea TokenValidacionRepository para validar la logica de verificacion
 * de tokens de alianza: token valido, invalido, expirado o ya utilizado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenValidacionService - Tests unitarios")
class TokenValidacionServiceTest {

    @Mock
    private TokenValidacionRepository repository;

    private TokenValidacionService service;

    @BeforeEach
    void setUp() {
        service = new TokenValidacionService(repository);
    }

    /**
     * Verifica que validar con un token valido retorne el DTO con los datos del token.
     */
    @Test
    @DisplayName("validar_tokenValido_retornaTokenValidacionResponseDTO")
    void validar_tokenValido_retornaTokenValidacionResponseDTO() {
        String token = "uuid-token-valido-12345";
        TokenValidacionResponseDTO dtoEsperado = new TokenValidacionResponseDTO(
                "Guatemala", "Guatemala", 10.0, "2026-05-01 12:00:00");
        when(repository.buscarTokenValido(token)).thenReturn(dtoEsperado);

        TokenValidacionResponseDTO resultado = service.validar(token);

        assertNotNull(resultado);
        assertEquals("Guatemala", resultado.getCiudad());
        assertEquals("Guatemala", resultado.getPais());
        assertEquals(10.0, resultado.getPorcentajeDescuento());
        assertNotNull(resultado.getFechaExpiracion());
        verify(repository).buscarTokenValido(token);
    }

    /**
     * Verifica que validar con token inexistente lanza IllegalArgumentException.
     */
    @Test
    @DisplayName("validar_tokenInexistente_lanzaIllegalArgumentException")
    void validar_tokenInexistente_lanzaIllegalArgumentException() {
        when(repository.buscarTokenValido("token-no-existe")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.validar("token-no-existe")
        );

        assertEquals("Token invalido, ya utilizado o expirado", ex.getMessage());
        verify(repository).buscarTokenValido("token-no-existe");
    }

    /**
     * Verifica que validar con token expirado (repositorio retorna null) lanza excepcion.
     */
    @Test
    @DisplayName("validar_tokenExpirado_lanzaIllegalArgumentException")
    void validar_tokenExpirado_lanzaIllegalArgumentException() {
        // El repositorio filtra por FechaExpiracion > SYSDATE, por lo que retorna null si expiro
        when(repository.buscarTokenValido("token-expirado-abc")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.validar("token-expirado-abc")
        );

        assertEquals("Token invalido, ya utilizado o expirado", ex.getMessage());
        verify(repository).buscarTokenValido("token-expirado-abc");
    }

    /**
     * Verifica que validar con token ya utilizado (repositorio retorna null) lanza excepcion.
     */
    @Test
    @DisplayName("validar_tokenYaUtilizado_lanzaIllegalArgumentException")
    void validar_tokenYaUtilizado_lanzaIllegalArgumentException() {
        // El repositorio filtra por Usado = 0, por lo que retorna null si ya fue usado
        when(repository.buscarTokenValido("token-ya-usado-xyz")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.validar("token-ya-usado-xyz")
        );

        assertEquals("Token invalido, ya utilizado o expirado", ex.getMessage());
        verify(repository).buscarTokenValido("token-ya-usado-xyz");
    }
}
