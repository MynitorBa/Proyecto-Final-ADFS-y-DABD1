package org.example.helpers;

import java.security.SecureRandom;
import java.util.HexFormat;

public class TokenHelper {

    public static String generarTokenHash() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes); // 64 chars hex
    }
}