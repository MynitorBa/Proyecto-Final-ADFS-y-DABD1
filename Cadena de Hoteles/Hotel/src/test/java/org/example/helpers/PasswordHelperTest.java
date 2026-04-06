package org.example.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PasswordHelper.
 * Verifica el hasheo de contrasenas con BCrypt y la verificacion de coincidencia.
 */
class PasswordHelperTest {

    // -- hashear

    /**
     * Verifica que hashear retorna un hash no nulo con el prefijo BCrypt esperado.
     */
    @Test
    void hashear_retornaHashNoNulo() {
        String result = PasswordHelper.hashear("test123");

        assertNotNull(result);
        assertTrue(result.startsWith("$2a$"), "El hash debe comenzar con el prefijo BCrypt $2a$");
    }

    /**
     * Verifica que dos llamadas con la misma contrasena producen hashes distintos
     * debido al salt aleatorio de BCrypt.
     */
    @Test
    void hashear_resultadoDistintoCadaVez() {
        String hash1 = PasswordHelper.hashear("test");
        String hash2 = PasswordHelper.hashear("test");

        assertNotEquals(hash1, hash2, "Cada hash debe ser distinto por el salt aleatorio");
    }

    // -- verificar

    /**
     * Verifica que verificar retorna true cuando la contrasena en texto plano
     * coincide con su hash BCrypt.
     */
    @Test
    void verificar_contrasenaCorrecta_retornaTrue() {
        String hash = PasswordHelper.hashear("pass");

        assertTrue(PasswordHelper.verificar("pass", hash));
    }

    /**
     * Verifica que verificar retorna false cuando la contrasena en texto plano
     * no coincide con el hash BCrypt almacenado.
     */
    @Test
    void verificar_contrasenaIncorrecta_retornaFalse() {
        String hash = PasswordHelper.hashear("pass");

        assertFalse(PasswordHelper.verificar("wrong", hash));
    }
}
