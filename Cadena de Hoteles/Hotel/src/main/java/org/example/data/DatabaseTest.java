package org.example.data;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseTest {

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