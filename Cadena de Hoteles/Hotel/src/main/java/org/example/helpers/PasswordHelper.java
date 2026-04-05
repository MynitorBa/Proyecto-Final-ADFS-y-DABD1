package org.example.helpers;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Helper para el hasheo y verificacion de contrasenas usando BCrypt.
 * Utiliza un factor de costo de 12 rondas para el salt, lo que ofrece
 * un balance adecuado entre seguridad y rendimiento.
 */
public class PasswordHelper {

    /** Factor de costo para la generacion del salt BCrypt. */
    private static final int SALT_ROUNDS = 12;

    /**
     * Genera el hash BCrypt de una contrasena en texto plano.
     *
     * @param passwordPlano contrasena en texto plano a hashear.
     * @return hash BCrypt listo para almacenar en base de datos.
     */
    public static String hashear(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(SALT_ROUNDS));
    }

    /**
     * Verifica si una contrasena en texto plano coincide con su hash BCrypt.
     *
     * @param passwordPlano     contrasena en texto plano ingresada por el usuario.
     * @param passwordHasheado  hash BCrypt almacenado en base de datos.
     * @return true si la contrasena coincide con el hash; false en caso contrario.
     */
    public static boolean verificar(String passwordPlano, String passwordHasheado) {
        return BCrypt.checkpw(passwordPlano, passwordHasheado);
    }
}