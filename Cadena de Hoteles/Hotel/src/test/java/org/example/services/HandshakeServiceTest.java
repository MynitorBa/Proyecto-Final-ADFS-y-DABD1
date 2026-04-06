package org.example.services;

import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.repositories.AgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HandshakeService.
 * Covers the procesarHandshake method: success path, unknown URL, and token persistence failure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HandshakeService Tests")
class HandshakeServiceTest {

    @Mock
    private AgenciaRepository repo;

    private HandshakeService service;

    @BeforeEach
    void setUp() {
        service = new HandshakeService(repo);
    }

    // -- procesarHandshake

    @Test
    @DisplayName("procesarHandshake_urlValida_retornaResponseDTONoNulo")
    void procesarHandshake_urlValida_retornaResponseDTONoNulo() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://agencia.example.com");
        dto.setTokenEntrada("token-entrada-123");

        when(repo.obtenerAgenciaIdPorURL("https://agencia.example.com")).thenReturn(5);
        when(repo.guardarTokens(eq(5), eq("token-entrada-123"), anyString())).thenReturn(true);

        HandshakeResponseDTO resultado = service.procesarHandshake(dto);

        assertNotNull(resultado);
        verify(repo).obtenerAgenciaIdPorURL("https://agencia.example.com");
        verify(repo).guardarTokens(eq(5), eq("token-entrada-123"), anyString());
    }

    @Test
    @DisplayName("procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException")
    void procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://desconocida.example.com");
        dto.setTokenEntrada("token-entrada-abc");

        when(repo.obtenerAgenciaIdPorURL("https://desconocida.example.com")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarHandshake(dto)
        );

        assertEquals("No se encontro ninguna agencia registrada con esa URL.", ex.getMessage());
        verify(repo).obtenerAgenciaIdPorURL("https://desconocida.example.com");
        verify(repo, never()).guardarTokens(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException")
    void procesarHandshake_tokenesNoGuardados_lanzaIllegalArgumentException() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://agencia.example.com");
        dto.setTokenEntrada("token-entrada-xyz");

        when(repo.obtenerAgenciaIdPorURL("https://agencia.example.com")).thenReturn(5);
        when(repo.guardarTokens(eq(5), eq("token-entrada-xyz"), anyString())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarHandshake(dto)
        );

        assertEquals("No se pudieron guardar los tokens.", ex.getMessage());
        verify(repo).guardarTokens(eq(5), eq("token-entrada-xyz"), anyString());
    }
}
