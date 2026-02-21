package org.example.helpers;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHelper {

    private static final int SALT_ROUNDS = 12;

    public static String hashear(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(SALT_ROUNDS));
    }

    public static boolean verificar(String passwordPlano, String passwordHasheado) {
        return BCrypt.checkpw(passwordPlano, passwordHasheado);
    }
}
