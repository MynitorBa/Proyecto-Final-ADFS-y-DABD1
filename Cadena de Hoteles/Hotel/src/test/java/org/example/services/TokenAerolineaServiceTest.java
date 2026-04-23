package org.example.services;

import org.example.dtos.AerolineaIdentidadDTO;
import org.example.dtos.TokenAerolineaRequestDTO;
import org.example.dtos.TokenAerolineaResponseDTO;
import org.example.repositories.AerolineaAliadaRepository;
import org.example.repositories.TokenAerolineaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para TokenAerolineaService.
 * Mockea TokenAerolineaRepository y AerolineaAliadaRepository para validar
 * la logica de generacion de tokens de alianza: autenticacion, ciudad y persistencia.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenAerolineaService - Tests unitarios")
class TokenAerolineaServiceTest {

    @Mock
    private TokenAerolineaRepository tokenRepository;

    @Mock
    private AerolineaAliadaRepository aerolineaRepository;

    private TokenAerolineaService service;

    @BeforeEach
    void setUp() {
        service = new TokenAerolineaService(tokenRepository, aerolineaRepository);
    }

    /**
     * Verifica que generarToken con token valido y ciudad existente retorne un DTO
     * con token UUID, URL de redireccion y fecha de expiracion.
     */
    @Test
    @DisplayName("generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto")
    void generarToken_tokenValidoCiudadExistente_retornaResponseDTOCompleto() {
        TokenAerolineaRequestDTO request = new TokenAerolineaRequestDTO();
        request.setCiudad("Guatemala");
        request.setPais("Guatemala");

        when(tokenRepository.obtenerAerolineaIdPorToken("token-hash-valido")).thenReturn(3);
        when(tokenRepository.buscarCiudadId("Guatemala", "Guatemala")).thenReturn(1);
        doNothing().when(tokenRepository).insertarToken(anyInt(), anyInt(), anyString(), any(Timestamp.class));

        AerolineaIdentidadDTO identidad = new AerolineaIdentidadDTO(3, "AeroTest", "https://aerotest.com");
        when(aerolineaRepository.obtenerAerolineaPorToken("token-hash-valido")).thenReturn(identidad);

        TokenAerolineaResponseDTO resultado = service.generarToken(request, "token-hash-valido");

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
        assertFalse(resultado.getToken().isBlank());
        assertNotNull(resultado.getUrlRedireccion());
        assertTrue(resultado.getUrlRedireccion().contains(resultado.getToken()));
        assertNotNull(resultado.getFechaExpiracion());

        verify(tokenRepository).obtenerAerolineaIdPorToken("token-hash-valido");
        verify(tokenRepository).buscarCiudadId("Guatemala", "Guatemala");
        verify(tokenRepository).insertarToken(eq(3), eq(1), anyString(), any(Timestamp.class));
        verify(aerolineaRepository).obtenerAerolineaPorToken("token-hash-valido");
    }

    /**
     * Verifica que generarToken con token invalido lanza IllegalArgumentException
     * sin consultar la ciudad ni insertar nada.
     */
    @Test
    @DisplayName("generarToken_tokenInvalido_lanzaIllegalArgumentException")
    void generarToken_tokenInvalido_lanzaIllegalArgumentException() {
        TokenAerolineaRequestDTO request = new TokenAerolineaRequestDTO();
        request.setCiudad("Guatemala");
        request.setPais("Guatemala");

        when(tokenRepository.obtenerAerolineaIdPorToken("token-invalido")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.generarToken(request, "token-invalido")
        );

        assertEquals("Token invalido o aerolinea no activa", ex.getMessage());
        verify(tokenRepository, never()).buscarCiudadId(anyString(), anyString());
        verify(tokenRepository, never()).insertarToken(anyInt(), anyInt(), anyString(), any());
    }

    /**
     * Verifica que generarToken con ciudad inexistente lanza IllegalArgumentException.
     */
    @Test
    @DisplayName("generarToken_ciudadNoExiste_lanzaIllegalArgumentException")
    void generarToken_ciudadNoExiste_lanzaIllegalArgumentException() {
        TokenAerolineaRequestDTO request = new TokenAerolineaRequestDTO();
        request.setCiudad("CiudadFantasma");
        request.setPais("PaisInexistente");

        when(tokenRepository.obtenerAerolineaIdPorToken("token-valido")).thenReturn(3);
        when(tokenRepository.buscarCiudadId("CiudadFantasma", "PaisInexistente")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.generarToken(request, "token-valido")
        );

        assertTrue(ex.getMessage().contains("CiudadFantasma"));
        assertTrue(ex.getMessage().contains("PaisInexistente"));
        verify(tokenRepository, never()).insertarToken(anyInt(), anyInt(), anyString(), any());
        verify(aerolineaRepository, never()).obtenerAerolineaPorToken(anyString());
    }

    /**
     * Verifica que generarToken inserta el token en el repositorio con aerolineaId y ciudadId correctos.
     */
    @Test
    @DisplayName("generarToken_datosValidos_insertaTokenConIdsCorrectos")
    void generarToken_datosValidos_insertaTokenConIdsCorrectos() {
        TokenAerolineaRequestDTO request = new TokenAerolineaRequestDTO();
        request.setCiudad("Antigua");
        request.setPais("Guatemala");

        when(tokenRepository.obtenerAerolineaIdPorToken("hash-test")).thenReturn(5);
        when(tokenRepository.buscarCiudadId("Antigua", "Guatemala")).thenReturn(9);
        doNothing().when(tokenRepository).insertarToken(anyInt(), anyInt(), anyString(), any(Timestamp.class));

        AerolineaIdentidadDTO identidad = new AerolineaIdentidadDTO(5, "AeroAntigua", "https://aeroantigua.com");
        when(aerolineaRepository.obtenerAerolineaPorToken("hash-test")).thenReturn(identidad);

        service.generarToken(request, "hash-test");

        verify(tokenRepository).insertarToken(eq(5), eq(9), anyString(), any(Timestamp.class));
    }

    /**
     * Verifica que la URL de redireccion contiene la URL base de la aerolinea mas el token generado.
     */
    @Test
    @DisplayName("generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken")
    void generarToken_datosValidos_urlRedireccionContieneUrlBaseYToken() {
        TokenAerolineaRequestDTO request = new TokenAerolineaRequestDTO();
        request.setCiudad("Quetzaltenango");
        request.setPais("Guatemala");

        when(tokenRepository.obtenerAerolineaIdPorToken("hash-xela")).thenReturn(2);
        when(tokenRepository.buscarCiudadId("Quetzaltenango", "Guatemala")).thenReturn(4);
        doNothing().when(tokenRepository).insertarToken(anyInt(), anyInt(), anyString(), any(Timestamp.class));

        AerolineaIdentidadDTO identidad = new AerolineaIdentidadDTO(2, "AeroXela", "https://aeroxela.com");
        when(aerolineaRepository.obtenerAerolineaPorToken("hash-xela")).thenReturn(identidad);

        TokenAerolineaResponseDTO resultado = service.generarToken(request, "hash-xela");

        assertNotNull(resultado.getUrlRedireccion());
        assertTrue(resultado.getUrlRedireccion().startsWith("https://aeroxela.com"));
        assertTrue(resultado.getUrlRedireccion().contains("?token="));
        assertTrue(resultado.getUrlRedireccion().contains(resultado.getToken()));
    }
}
