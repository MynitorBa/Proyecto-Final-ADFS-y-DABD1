package org.example.controllers;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import org.example.dtos.LoginRequestDTO;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.AuthService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Tests unitarios")
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private Context ctx;
    @Mock private LoginRequestDTO loginRequestDTO;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    // ---- handleLogin ----

    @Test
    @DisplayName("handleLogin_credencialesValidas_emiteCookieYRetorna200")
    void handleLogin_credencialesValidas_emiteCookieYRetorna200() throws Exception {
        // arrange
        AuthService.LoginResultado resultado = mock(AuthService.LoginResultado.class);
        when(resultado.token()).thenReturn("jwt-token-abc");
        doReturn(null).when(resultado).respuesta();

        when(ctx.bodyAsClass(LoginRequestDTO.class)).thenReturn(loginRequestDTO);
        when(authService.login(loginRequestDTO)).thenReturn(resultado);
        when(ctx.status(200)).thenReturn(ctx);

        // act
        controller.handleLogin(ctx);

        // assert
        verify(ctx).cookie(any(Cookie.class));
        verify(ctx).status(200);
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleLogin_credencialesInvalidas_retorna401")
    void handleLogin_credencialesInvalidas_retorna401() throws Exception {
        // arrange
        when(ctx.bodyAsClass(LoginRequestDTO.class)).thenReturn(loginRequestDTO);
        doThrow(new CredencialesInvalidasException()).when(authService).login(loginRequestDTO);
        when(ctx.status(401)).thenReturn(ctx);

        // act
        controller.handleLogin(ctx);

        // assert
        verify(ctx).status(401);
        verify(ctx).json(Map.of("mensaje", "Usuario o contrasena incorrectos"));
        verify(ctx, never()).status(200);
    }

    // ---- handleLogout ----

    @Test
    @DisplayName("handleLogout_siempre_invalidaCookieYRetorna200")
    void handleLogout_siempre_invalidaCookieYRetorna200() {
        // arrange
        when(ctx.status(200)).thenReturn(ctx);

        // act
        controller.handleLogout(ctx);

        // assert
        verify(ctx).cookie(any(Cookie.class));
        verify(ctx).status(200);
        verify(ctx).json(any());
    }
}
