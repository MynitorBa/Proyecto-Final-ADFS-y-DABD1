package org.example.data;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Clase de prueba para verificar la conexion a la base de datos Oracle.
 * Solo debe usarse en desarrollo para validar credenciales y URL JDBC.
 */
public class DatabaseTest {

    /**
     * Intenta abrir una conexion JDBC con Oracle usando credenciales fijas.
     * Imprime un mensaje de exito o error segun el resultado.
     */
    public static void testConnection() {

        String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
        String user = "system";
        String password = "meme1234";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {

            System.out.println("Conexión exitosa a Oracle!");

        } catch (Exception e) {
            System.out.println("Error de conexión");
            e.printStackTrace();
        }
    }
}