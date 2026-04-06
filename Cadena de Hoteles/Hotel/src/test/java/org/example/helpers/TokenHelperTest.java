package org.example.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TokenHelper.
 * Verifica que generarTokenHash produce tokens hexadecimales de 64 caracteres
 * con aleatoriedad criptografica garantizada por SecureRandom.
 */
class TokenHelperTest {

    // -- generarTokenHash

    /**
     * Verifica que generarTokenHash retorna una cadena de exactamente 64 caracteres.
     */
    @Test
    void generarTokenHash_retornaCadena64Caracteres() {
        String token = TokenHelper.generarTokenHash();

        assertEquals(64, token.length(), "El token debe tener exactamente 64 caracteres");
    }

    /**
     * Verifica que generarTokenHash retorna solo caracteres hexadecimales en minuscula.
     */
    @Test
    void generarTokenHash_retornaSoloHexadecimal() {
        String token = TokenHelper.generarTokenHash();

        assertTrue(token.matches("[0-9a-f]{64}"),
                "El token debe contener solo digitos y letras hexadecimales en minuscula");
    }

    /**
     * Verifica que dos llamadas consecutivas a generarTokenHash retornan valores distintos
     * gracias al uso de SecureRandom.
     */
    @Test
    void generarTokenHash_dosLlamadasRetornanDistinto() {
        String token1 = TokenHelper.generarTokenHash();
        String token2 = TokenHelper.generarTokenHash();

        assertNotEquals(token1, token2, "Cada token debe ser unico por la aleatoriedad de SecureRandom");
    }
}
