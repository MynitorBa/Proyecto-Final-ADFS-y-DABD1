package org.example.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL =
            System.getenv().getOrDefault("DB_URL",
                    "jdbc:oracle:thin:@localhost:1521/XEPDB1");

    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "system");
    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASS", "meme1234");

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // SELECT con mapper y parámetros
    public static <T> List<T> executeQuery(
            String sql,
            ResultSetMapper<T> mapper,
            Object... params) {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            ResultSet rs = stmt.executeQuery();

            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(mapper.map(rs));
            }

            return results;

        } catch (Exception e) {
            throw new DataAccessException("Error ejecutando query", e);
        }
    }

    // INSERT, UPDATE, DELETE
    public static int executeUpdate(
            String sql,
            Object... params) {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);

            return stmt.executeUpdate();

        } catch (Exception e) {
            throw new DataAccessException("Error ejecutando update", e);
        }
    }

    // Método privado para parámetros dinámicos
    private static void setParameters(
            PreparedStatement stmt,
            Object... params) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}