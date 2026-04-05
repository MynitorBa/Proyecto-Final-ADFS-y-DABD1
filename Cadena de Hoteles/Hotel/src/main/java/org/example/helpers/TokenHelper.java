package org.example.helpers;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Helper para la generacion de tokens de autenticacion seguros.
 * Usa SecureRandom para garantizar aleatoriedad criptografica.
 */
public class TokenHelper {

    /**
     * Genera un token aleatorio de 64 caracteres en formato hexadecimal.
     * Se usa como token de acceso para agencias externas u otros casos
     * donde se necesite un identificador unico e impredecible.
     *
     * @return string hexadecimal de 64 caracteres generado con SecureRandom.
     */
    public static String generarTokenHash() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes); // 64 chars hex
    }
}