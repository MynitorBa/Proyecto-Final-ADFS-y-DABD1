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
            e.printStackTrace();
            throw new DataAccessException("Error ejecutando query", e);
        }
    }

    public static int executeUpdate(
            String sql,
            Object... params) {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            return stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error ejecutando update", e);
        }
    }

    public static int executeInsertReturnId(
            String sql,
            String idColumnName,
            Object... params) {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{idColumnName})) {

            setParameters(stmt, params);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }

            throw new DataAccessException("No se obtuvo ID generado tras el INSERT", null);

        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error ejecutando insert con ID retornado", e);
        }
    }

    private static void setParameters(
            PreparedStatement stmt,
            Object... params) throws SQLException {

        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                stmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(i + 1, (Long) param);
            } else if (param instanceof Double) {
                stmt.setDouble(i + 1, (Double) param);
            } else if (param instanceof Float) {
                stmt.setFloat(i + 1, (Float) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof java.sql.Date) {
                stmt.setDate(i + 1, (java.sql.Date) param);
            } else if (param instanceof java.sql.Timestamp) {
                stmt.setTimestamp(i + 1, (java.sql.Timestamp) param);
            } else if (param instanceof byte[]) {
                stmt.setBytes(i + 1, (byte[]) param);
            } else {
                stmt.setString(i + 1, param.toString());
            }
        }
    }
}