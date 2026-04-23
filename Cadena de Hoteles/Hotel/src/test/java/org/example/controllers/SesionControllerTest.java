package org.example.controllers;

import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import org.example.helpers.JwtHelper;
import org.example.services.SesionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SesionController - Tests unitarios")
class SesionControllerTest {

    @Mock private SesionService sesionService;
    @Mock private Context ctx;
    private SesionController controller;

    @BeforeEach
    void setUp() {
        controller = new SesionController(sesionService);
    }

    // ---- handleSesion ----

    @Test
    @DisplayName("handleSesion_sinToken_retornaSinSesion")
    void handleSesion_sinToken_retornaSinSesion() {
        // arrange
        when(ctx.cookie("auth_token")).thenReturn(null);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(null).when(sesionService).sinSesion();

        // act
        controller.handleSesion(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(any());
        verify(sesionService).sinSesion();
        verify(sesionService, never()).obtenerSesion(anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("handleSesion_tokenInvalido_retornaSinSesion")
    void handleSesion_tokenInvalido_retornaSinSesion() {
        // arrange
        when(ctx.cookie("auth_token")).thenReturn("token-invalido");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(null).when(sesionService).sinSesion();

        try (MockedStatic<JwtHelper> mocked = mockStatic(JwtHelper.class)) {
            mocked.when(() -> JwtHelper.esValido("token-invalido")).thenReturn(false);

            // act
            controller.handleSesion(ctx);

            // assert
            verify(ctx).status(200);
            verify(ctx).json(any());
            verify(sesionService).sinSesion();
            verify(sesionService, never()).obtenerSesion(anyInt(), anyString(), anyInt());
        }
    }

    @Test
    @DisplayName("handleSesion_tokenValido_retornaConSesion")
    void handleSesion_tokenValido_retornaConSesion() {
        // arrange
        Claims mockClaims = mock(Claims.class);

        when(ctx.cookie("auth_token")).thenReturn("valid-token");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(null).when(sesionService).obtenerSesion(42, "miku", 2);

        try (MockedStatic<JwtHelper> mocked = mockStatic(JwtHelper.class)) {
            mocked.when(() -> JwtHelper.esValido("valid-token")).thenReturn(true);
            mocked.when(() -> JwtHelper.verificarToken("valid-token")).thenReturn(mockClaims);
            mocked.when(() -> JwtHelper.getUsuarioId(mockClaims)).thenReturn(42);
            mocked.when(() -> JwtHelper.getUsername(mockClaims)).thenReturn("miku");
            mocked.when(() -> JwtHelper.getRolId(mockClaims)).thenReturn(2);

            // act
            controller.handleSesion(ctx);

            // assert
            verify(ctx).status(200);
            verify(ctx).json(any());
            verify(sesionService).obtenerSesion(42, "miku", 2);
            verify(sesionService, never()).sinSesion();
        }
    }
}
