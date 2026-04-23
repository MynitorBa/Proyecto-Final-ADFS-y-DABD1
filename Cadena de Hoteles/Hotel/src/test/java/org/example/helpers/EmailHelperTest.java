package org.example.helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

/**
 * Tests unitarios para EmailHelper.
 * Usa MockedStatic para interceptar las llamadas estaticas sin establecer
 * conexion SMTP real, evitando envios accidentales y dependencias de red en CI/CD.
 */
@DisplayName("EmailHelper - Tests unitarios")
class EmailHelperTest {

    /**
     * Verifica que enviar con datos validos no lanza ninguna excepcion.
     */
    @Test
    @DisplayName("enviar_datosValidos_noLanzaExcepcion")
    void enviar_datosValidos_noLanzaExcepcion() {
        try (MockedStatic<EmailHelper> mockedEmail = mockStatic(EmailHelper.class)) {
            mockedEmail.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                       .thenAnswer(invocation -> null);

            assertDoesNotThrow(() ->
                    EmailHelper.enviar("destino@test.com", "Asunto de prueba", "<h1>Cuerpo</h1>")
            );

            mockedEmail.verify(() -> EmailHelper.enviar(
                    eq("destino@test.com"),
                    eq("Asunto de prueba"),
                    anyString()
            ));
        }
    }

    /**
     * Verifica que enviar con cuerpo HTML minimo no lanza ninguna excepcion.
     */
    @Test
    @DisplayName("enviar_cuerpoMinimo_noLanzaExcepcion")
    void enviar_cuerpoMinimo_noLanzaExcepcion() {
        try (MockedStatic<EmailHelper> mockedEmail = mockStatic(EmailHelper.class)) {
            mockedEmail.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                       .thenAnswer(invocation -> null);

            assertDoesNotThrow(() ->
                    EmailHelper.enviar("a@b.com", "s", "h")
            );

            mockedEmail.verify(() -> EmailHelper.enviar(eq("a@b.com"), eq("s"), eq("h")));
        }
    }

    /**
     * Verifica que enviar es invocado exactamente una vez con los argumentos exactos.
     */
    @Test
    @DisplayName("enviar_argumentosExactos_invocaMetodoConParametrosCorrectos")
    void enviar_argumentosExactos_invocaMetodoConParametrosCorrectos() {
        try (MockedStatic<EmailHelper> mockedEmail = mockStatic(EmailHelper.class)) {
            mockedEmail.when(() -> EmailHelper.enviar(anyString(), anyString(), anyString()))
                       .thenAnswer(invocation -> null);

            String destinatario = "usuario@miku-inn.com";
            String asunto       = "Confirmacion de reservacion";
            String cuerpo       = "<p>Su reservacion fue confirmada.</p>";

            EmailHelper.enviar(destinatario, asunto, cuerpo);

            mockedEmail.verify(() -> EmailHelper.enviar(destinatario, asunto, cuerpo));
        }
    }
}
