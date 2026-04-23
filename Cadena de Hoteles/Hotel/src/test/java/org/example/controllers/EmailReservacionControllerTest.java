package org.example.controllers;

import io.javalin.http.Context;
import org.example.helpers.EmailHelper;
import org.example.services.EmailReservacionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailReservacionController - Tests unitarios")
class EmailReservacionControllerTest {

    @Mock private EmailReservacionService emailService;
    @Mock private Context ctx;
    private EmailReservacionController controller;

    @BeforeEach
    void setUp() {
        controller = new EmailReservacionController(emailService);
    }

    // ---- handleEnviarCorreoReservacion ----

    @Test
    @DisplayName("handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200")
    void handleEnviarCorreoReservacion_rolAutorizado_enviaCorreoYRetorna200() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.status(200)).thenReturn(ctx);
        doNothing().when(emailService).enviarCorreoReservacion(5, 10);

        // act
        controller.handleEnviarCorreoReservacion(ctx);

        // assert
        verify(emailService).enviarCorreoReservacion(5, 10);
        verify(ctx).status(200);
        verify(ctx).json(Map.of("mensaje", "Correo enviado correctamente"));
    }

    @Test
    @DisplayName("handleEnviarCorreoReservacion_rolNoAutorizado_retorna403")
    void handleEnviarCorreoReservacion_rolNoAutorizado_retorna403() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.attribute("rolId")).thenReturn(3);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.status(403)).thenReturn(ctx);

        // act
        controller.handleEnviarCorreoReservacion(ctx);

        // assert
        verify(ctx).status(403);
        verify(ctx).json(Map.of("mensaje", "Acceso denegado"));
        verify(emailService, never()).enviarCorreoReservacion(anyInt(), anyInt());
    }

    @Test
    @DisplayName("handleEnviarCorreoReservacion_reservacionNoExiste_retorna404")
    void handleEnviarCorreoReservacion_reservacionNoExiste_retorna404() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.pathParam("id")).thenReturn("99");
        when(ctx.status(404)).thenReturn(ctx);
        doThrow(new IllegalArgumentException("Reservacion no encontrada"))
                .when(emailService).enviarCorreoReservacion(99, 10);

        // act
        controller.handleEnviarCorreoReservacion(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Reservacion no encontrada"));
        verify(ctx, never()).status(200);
    }

    @Test
    @DisplayName("handleEnviarCorreoReservacion_errorRuntime_retorna500")
    void handleEnviarCorreoReservacion_errorRuntime_retorna500() {
        // arrange
        when(ctx.attribute("usuarioId")).thenReturn(10);
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.pathParam("id")).thenReturn("5");
        when(ctx.status(500)).thenReturn(ctx);
        doThrow(new RuntimeException("Fallo SMTP"))
                .when(emailService).enviarCorreoReservacion(5, 10);

        // act
        controller.handleEnviarCorreoReservacion(ctx);

        // assert
        verify(ctx).status(500);
        verify(ctx).json(Map.of("mensaje", "Error al enviar el correo: Fallo SMTP"));
        verify(ctx, never()).status(200);
    }

    // ---- handleContacto ----

    @Test
    @DisplayName("handleContacto_camposObligatoriosFaltantes_retorna400")
    void handleContacto_camposObligatoriosFaltantes_retorna400() {
        // arrange — correo vacio
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of(
                "nombre", "Miku",
                "correo", "",
                "mensaje", "Hola"
        ));
        when(ctx.status(400)).thenReturn(ctx);

        // act
        controller.handleContacto(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Nombre, correo y mensaje son obligatorios"));
    }

    @Test
    @DisplayName("handleContacto_formularioValido_enviaCorreoYRetorna200")
    void handleContacto_formularioValido_enviaCorreoYRetorna200() {
        // arrange
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of(
                "nombre", "Miku",
                "correo", "miku@example.com",
                "asunto", "Consulta",
                "mensaje", "Quisiera reservar"
        ));
        when(ctx.status(200)).thenReturn(ctx);

        try (MockedStatic<EmailHelper> mocked = mockStatic(EmailHelper.class)) {
            mocked.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                  .thenAnswer(invocation -> null);

            // act
            controller.handleContacto(ctx);

            // assert
            mocked.verify(() -> EmailHelper.enviar(
                    eq("distribuidorapine@gmail.com"), anyString(), anyString()));
            verify(ctx).status(200);
            verify(ctx).json(Map.of("mensaje", "Mensaje enviado correctamente"));
        }
    }

    // ---- handleNewsletter ----

    @Test
    @DisplayName("handleNewsletter_correoSinArroba_retorna400")
    void handleNewsletter_correoSinArroba_retorna400() {
        // arrange
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of("correo", "correo-invalido"));
        when(ctx.status(400)).thenReturn(ctx);

        // act
        controller.handleNewsletter(ctx);

        // assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Correo inv\u00E1lido"));
    }

    @Test
    @DisplayName("handleNewsletter_correoValido_enviaCorreoYRetorna200")
    void handleNewsletter_correoValido_enviaCorreoYRetorna200() {
        // arrange
        when(ctx.bodyAsClass(Map.class)).thenReturn(Map.of("correo", "suscriptor@example.com"));
        when(ctx.status(200)).thenReturn(ctx);

        try (MockedStatic<EmailHelper> mocked = mockStatic(EmailHelper.class)) {
            mocked.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                  .thenAnswer(invocation -> null);

            // act
            controller.handleNewsletter(ctx);

            // assert
            mocked.verify(() -> EmailHelper.enviar(
                    eq("distribuidorapine@gmail.com"), anyString(), anyString()));
            verify(ctx).status(200);
            verify(ctx).json(Map.of("mensaje", "Suscripci\u00F3n registrada correctamente"));
        }
    }
}
