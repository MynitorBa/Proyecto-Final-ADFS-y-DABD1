package org.example.helpers;

import io.javalin.http.Context;
import org.example.dtos.AgenciaIdentidad;
import org.example.repositories.AgenciaRepository;
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
 * Pruebas unitarias para AgenciaAuthMiddleware.
 * Inyecta un AgenciaRepository mockeado directamente en el campo estatico
 * package-private del middleware para verificar la logica de autenticacion:
 * token valido (inyecta atributos), ausente (401), vacio (401) e invalido (401).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AgenciaAuthMiddleware - Tests unitarios")
class AgenciaAuthMiddlewareTest {

    @Mock
    private AgenciaRepository mockRepo;

    @Mock
    private Context ctx;

    /**
     * Inyecta el repositorio mockeado en el campo estatico package-private
     * antes de cada test para aislar completamente de la base de datos.
     */
    @BeforeEach
    void setUp() {
        AgenciaAuthMiddleware.repo = mockRepo;
    }

    /**
     * Restaura el repositorio real despues de cada test para evitar
     * que el mock contamine otros tests de la suite.
     */
    @AfterEach
    void tearDown() {
        AgenciaAuthMiddleware.repo = new AgenciaRepository();
    }

    /**
     * Verifica que con token valido se retorne true, se inyecten los tres atributos
     * de la agencia (agenciaId, agenciaNombre, agenciaUrl) en el contexto,
     * y NO se escriba ninguna respuesta de error (sin status 401).
     */
    @Test
    @DisplayName("verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia")
    void verificar_tokenValido_retornaTrueEInyectaAtributosDeAgencia() {
        AgenciaIdentidad agencia = new AgenciaIdentidad();
        agencia.setId(7);
        agencia.setNombre("Agencia Valida");
        agencia.setUrlAgencia("https://agencia.valida.com");

        when(ctx.header("X-Agencia-Token")).thenReturn("token-valido-abc");
        when(mockRepo.obtenerAgenciaPorToken("token-valido-abc")).thenReturn(agencia);

        boolean resultado = AgenciaAuthMiddleware.verificar(ctx);

        assertTrue(resultado);
        verify(ctx).attribute("agenciaId",     7);
        verify(ctx).attribute("agenciaNombre", "Agencia Valida");
        verify(ctx).attribute("agenciaUrl",    "https://agencia.valida.com");
        verify(ctx, never()).status(anyInt());
    }

    /**
     * Verifica que con header X-Agencia-Token ausente (null) se retorne false,
     * se escriba status 401 con un mensaje de error, y NO se consulte el repositorio.
     */
    @Test
    @DisplayName("verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo")
    void verificar_tokenAusente_retornaFalseYStatus401SinConsultarRepo() {
        when(ctx.header("X-Agencia-Token")).thenReturn(null);
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AgenciaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(mockRepo, never()).obtenerAgenciaPorToken(anyString());
    }

    /**
     * Verifica que con header en blanco (solo espacios) se retorne false,
     * se escriba status 401, y NO se consulte el repositorio.
     */
    @Test
    @DisplayName("verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo")
    void verificar_tokenEnBlanco_retornaFalseYStatus401SinConsultarRepo() {
        when(ctx.header("X-Agencia-Token")).thenReturn("   ");
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AgenciaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
        verify(mockRepo, never()).obtenerAgenciaPorToken(anyString());
    }

    /**
     * Verifica que con token presente pero no reconocido por el repositorio
     * (obtenerAgenciaPorToken retorna null) se retorne false y se escriba status 401.
     */
    @Test
    @DisplayName("verificar_tokenNoReconocido_retornaFalseYStatus401")
    void verificar_tokenNoReconocido_retornaFalseYStatus401() {
        when(ctx.header("X-Agencia-Token")).thenReturn("token-inexistente-xyz");
        when(mockRepo.obtenerAgenciaPorToken("token-inexistente-xyz")).thenReturn(null);
        when(ctx.status(401)).thenReturn(ctx);

        boolean resultado = AgenciaAuthMiddleware.verificar(ctx);

        assertFalse(resultado);
        verify(ctx).status(401);
        verify(ctx).json(argThat(obj ->
                obj instanceof Map && ((Map<?, ?>) obj).containsKey("mensaje")
        ));
    }
}
