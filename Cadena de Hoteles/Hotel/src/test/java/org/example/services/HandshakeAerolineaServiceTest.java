package org.example.services;

import org.example.dtos.HandshakeRequestDTO;
import org.example.dtos.HandshakeResponseDTO;
import org.example.repositories.AerolineaAliadaRepository;
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
 * Pruebas unitarias para HandshakeAerolineaService.
 * Mockea AerolineaAliadaRepository para validar la logica de autenticacion
 * de aerolineas aliadas externas: URL registrada, token persistido y errores.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HandshakeAerolineaService - Tests unitarios")
class HandshakeAerolineaServiceTest {

    @Mock
    private AerolineaAliadaRepository repo;

    private HandshakeAerolineaService service;

    @BeforeEach
    void setUp() {
        service = new HandshakeAerolineaService(repo);
    }

    /**
     * Verifica que procesarHandshake con URL registrada retorne un DTO con token de salida
     * e invoque guardarTokensAerolinea con los parametros correctos.
     */
    @Test
    @DisplayName("procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida")
    void procesarHandshake_urlRegistrada_retornaResponseDTOConTokenDeSalida() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://aerolinea-aliada.example.com");
        dto.setTokenEntrada("token-entrada-aerolinea-123");

        when(repo.obtenerAerolineaIdPorURL("https://aerolinea-aliada.example.com")).thenReturn(7);
        when(repo.guardarTokensAerolinea(eq(7), eq("token-entrada-aerolinea-123"), anyString()))
                .thenReturn(true);

        HandshakeResponseDTO resultado = service.procesarHandshake(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getTokenSalida());
        assertFalse(resultado.getTokenSalida().isBlank());
        verify(repo).obtenerAerolineaIdPorURL("https://aerolinea-aliada.example.com");
        verify(repo).guardarTokensAerolinea(eq(7), eq("token-entrada-aerolinea-123"), anyString());
    }

    /**
     * Verifica que procesarHandshake con URL no registrada lanza IllegalArgumentException
     * y no intenta guardar tokens.
     */
    @Test
    @DisplayName("procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException")
    void procesarHandshake_urlNoRegistrada_lanzaIllegalArgumentException() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://desconocida-aerolinea.example.com");
        dto.setTokenEntrada("token-cualquiera");

        when(repo.obtenerAerolineaIdPorURL("https://desconocida-aerolinea.example.com"))
                .thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarHandshake(dto)
        );

        assertTrue(ex.getMessage().contains("No se encontro ninguna aerolinea registrada"));
        verify(repo).obtenerAerolineaIdPorURL("https://desconocida-aerolinea.example.com");
        verify(repo, never()).guardarTokensAerolinea(anyInt(), anyString(), anyString());
    }

    /**
     * Verifica que procesarHandshake lanza IllegalArgumentException cuando los tokens
     * no se pueden persistir en la base de datos.
     */
    @Test
    @DisplayName("procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException")
    void procesarHandshake_tokensNoPersistidos_lanzaIllegalArgumentException() {
        HandshakeRequestDTO dto = new HandshakeRequestDTO();
        dto.setUrlAgencia("https://aerolinea-aliada.example.com");
        dto.setTokenEntrada("token-problema-xyz");

        when(repo.obtenerAerolineaIdPorURL("https://aerolinea-aliada.example.com")).thenReturn(7);
        when(repo.guardarTokensAerolinea(eq(7), eq("token-problema-xyz"), anyString()))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.procesarHandshake(dto)
        );

        assertEquals("No se pudieron guardar los tokens de la aerolinea aliada.", ex.getMessage());
        verify(repo).guardarTokensAerolinea(eq(7), eq("token-problema-xyz"), anyString());
    }
}
