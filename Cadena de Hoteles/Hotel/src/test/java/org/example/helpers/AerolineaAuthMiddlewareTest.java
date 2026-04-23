package org.example.helpers;

import io.javalin.http.Context;
import org.example.dtos.AerolineaIdentidadDTO;
import org.example.repositories.AerolineaAliadaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AerolineaAuthMiddleware.
 * Inyecta un AerolineaAliadaRepository mockeado directamente en el campo estatico
 * package-private del middleware para verificar la logica de autenticacion:
 * token valido (inyecta atributos), ausente (401), vacio (401) e invalido (401).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AerolineaAuthMiddleware - Tests unitarios")
class AerolineaAuthMiddlewareTest {

    @Mock
    private AerolineaAliadaRepository mockRepo;

    @Mock
    private Context ctx;

    /**
     * Inyecta el repositorio mockeado en el campo estatico package-private
     * antes de cada test para aislar completamente de la base de datos.
     */
    @BeforeEach
    void setUp() {
        AerolineaAuthMiddleware.repo = mockRepo;
    }

    /**
     * Restaura el repositorio real despues de cada test para evitar
     * que el mock contamine otros tests de la suite.
     */
    @AfterEach
    void tearDown() {
        AerolineaAuthMiddleware.repo = new AerolineaAliadaRepository();
    }

    /**
     * Verifica que con token valido se retorne true, se inyecten los tres atributos
     * de la aerolinea (aerolineaId, aerolineaNombre, aerolineaUrl) en el contexto,
     * y NO se escriba ninguna respuesta de error (sin status 401).
     */
    @Test
    @DisplayName("verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea")
    void verificar_tokenValido_retornaTrueEInyectaAtributosDeAerolinea() {
        AerolineaIdentidadDTO aerolinea = new AerolineaIdentidadDTO(
                3, "Aerolinea Valida", "https://aerolinea.valida.com");

        when(ctx.header("X-Aerolinea-Token")).thenReturn("token-aerolinea-abc");
        when(mockRepo.obtenerAerolineaPorToken("token-aerolinea-abc")).thenReturn(aerolinea);

        boolean resultado = AerolineaAuthMiddleware.verificar(ctx);

        assertTrue(resultado);
        verify(ctx).attribute("aerolineaId",     3);
        verify(ctx).attribute("aerolineaNombre", "Aerolinea Valida");
        verify(ctx).attribute("aerolineaUrl",    "https://aerolinea.valida.com");
        verify(ctx, never()).status(anyInt());
    }

    /**
     * Verifica que con header X-Aerolinea-Token ausente (null) se retorne false,
     * se escriba status 401 con un mensaje de error, y NO se consulte el repositorio.
     */
    @Test
    @DisplayName("verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo")
    void verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo() {
        when(ctx.header("X-Aerolinea-Token")).thenReturn(null);
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AerolineaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(mockRepo, never()).obtenerAerolineaPorToken(anyString());
    }

    /**
     * Verifica que con header en blanco (solo espacios) se retorne false,
     * se escriba status 401, y NO se consulte el repositorio.
     */
    @Test
    @DisplayName("verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo")
    void verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo() {
        when(ctx.header("X-Aerolinea-Token")).thenReturn("   ");
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AerolineaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(mockRepo, never()).obtenerAerolineaPorToken(anyString());
    }

    /**
     * Verifica que con token presente pero no reconocido por el repositorio
     * (obtenerAerolineaPorToken retorna null) se retorne false y se escriba status 401.
     */
    @Test
    @DisplayName("verificar_tokenNoReconocido_retornaFalseYStatus401")
    void verificar_tokenNoReconocido_retornaFalseYStatus401() {
        when(ctx.header("X-Aerolinea-Token")).thenReturn("token-inexistente-xyz");
        when(mockRepo.obtenerAerolineaPorToken("token-inexistente-xyz")).thenReturn(null);
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AerolineaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }
}
