package org.example.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para EmailHelper.
 * El helper usa credenciales reales de Gmail App Password, por lo que
 * el envio se completa sin excepcion cuando hay conexion disponible.
 */
class EmailHelperTest {

    // -- enviar

    /**
     * Verifica que enviar completa sin lanzar excepcion con datos validos.
     */
    @Test
    void enviar_conDatosValidos_completaSinExcepcion() {
        assertDoesNotThrow(() ->
                EmailHelper.enviar("test@test.com", "Asunto", "<p>Test</p>"));
    }

    /**
     * Verifica que enviar completa sin lanzar excepcion con cuerpo HTML minimo.
     */
    @Test
    void enviar_conCuerpoMinimo_completaSinExcepcion() {
        assertDoesNotThrow(() ->
                EmailHelper.enviar("a@b.com", "s", "h"));
    }
}
