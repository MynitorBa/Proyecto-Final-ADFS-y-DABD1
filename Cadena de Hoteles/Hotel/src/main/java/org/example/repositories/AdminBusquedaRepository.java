package org.example.repositories;

import org.example.data.DatabaseManager;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminBusquedaRepository {

    // ════════════════════════════════════════════════════════════════════════
    //  LISTAR BÚSQUEDAS CON FILTROS + PAGINACIÓN
    //
    //  Parámetros de filtro opcionales:
    //    destino        — nombre de ciudad (LIKE)
    //    usuarioAgencia — username del usuario O nombre de la agencia (LIKE)
    //    tipoBusquedaId — 1 (Web/Usuario) | 2 (REST/Agencia) | null (todos)
    //    fechaDesde     — filtrar Busqueda.Fecha >= ?
    //    fechaHasta     — filtrar Busqueda.Fecha <= ?
    //    offset         — para paginación OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    //    porPagina      — filas por página
    // ════════════════════════════════════════════════════════════════════════

    public List<Map<String, Object>> listar(
            String destino, String usuarioAgencia, Integer tipoBusquedaId,
            Date fechaDesde, Date fechaHasta,
            int offset, int porPagina) {

        StringBuilder sql = new StringBuilder(
                "SELECT b.ID, " +
                        "       c.Nombre      AS Ciudad, " +
                        "       b.FechaCheckIn, " +
                        "       b.FechaCheckOut, " +
                        "       b.CantidadPersonas, " +
                        "       u.Username    AS Usuario, " +
                        "       a.Nombre      AS Agencia, " +
                        "       tb.Estado     AS TipoBusqueda, " +
                        "       b.Fecha       AS FechaHora " +
                        "FROM   Busqueda b " +
                        "JOIN   Ciudad        c  ON b.CiudadID        = c.ID " +
                        "LEFT JOIN Usuario    u  ON b.UsuarioID       = u.ID " +
                        "LEFT JOIN Agencia    a  ON b.AgenciaID       = a.ID " +
                        "LEFT JOIN TipoBusqueda tb ON b.TipoBusquedaID = tb.ID " +
                        "WHERE  1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (destino != null && !destino.isBlank()) {
            sql.append("AND LOWER(c.Nombre) LIKE LOWER(?) ");
            params.add("%" + destino.trim() + "%");
        }
        if (usuarioAgencia != null && !usuarioAgencia.isBlank()) {
            sql.append("AND (LOWER(u.Username) LIKE LOWER(?) OR LOWER(a.Nombre) LIKE LOWER(?)) ");
            String like = "%" + usuarioAgencia.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (tipoBusquedaId != null) {
            sql.append("AND b.TipoBusquedaID = ? ");
            params.add(tipoBusquedaId);
        }
        if (fechaDesde != null) {
            sql.append("AND TRUNC(b.Fecha) >= ? ");
            params.add(fechaDesde);
        }
        if (fechaHasta != null) {
            sql.append("AND TRUNC(b.Fecha) <= ? ");
            params.add(fechaHasta);
        }

        sql.append("ORDER BY b.Fecha DESC, b.ID DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(porPagina);

        return DatabaseManager.executeQuery(sql.toString(), rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",       rs.getInt("ID"));
            row.put("destino",  rs.getString("Ciudad"));
            row.put("checkIn",  rs.getDate("FechaCheckIn")  != null ? rs.getDate("FechaCheckIn").toString()  : null);
            row.put("checkOut", rs.getDate("FechaCheckOut") != null ? rs.getDate("FechaCheckOut").toString() : null);
            row.put("personas", rs.getInt("CantidadPersonas"));

            String tipoBusqueda = rs.getString("TipoBusqueda"); // 'Usuario' o 'Agencia'
            boolean esRest = tipoBusqueda != null && tipoBusqueda.equalsIgnoreCase("Agencia");
            row.put("tipo",     esRest ? "rest" : "web");
            row.put("usuario",  rs.getString("Usuario"));
            row.put("agencia",  rs.getString("Agencia"));

            java.sql.Date fecha = rs.getDate("FechaHora");
            row.put("fechaHora", fecha != null ? fecha.toString() + "T00:00:00" : null);
            return row;
        }, params.toArray());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONTAR BÚSQUEDAS (para calcular total de páginas)
    // ════════════════════════════════════════════════════════════════════════

    public int contar(String destino, String usuarioAgencia, Integer tipoBusquedaId,
                      Date fechaDesde, Date fechaHasta) {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS TOTAL " +
                        "FROM   Busqueda b " +
                        "JOIN   Ciudad        c  ON b.CiudadID        = c.ID " +
                        "LEFT JOIN Usuario    u  ON b.UsuarioID       = u.ID " +
                        "LEFT JOIN Agencia    a  ON b.AgenciaID       = a.ID " +
                        "WHERE  1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (destino != null && !destino.isBlank()) {
            sql.append("AND LOWER(c.Nombre) LIKE LOWER(?) ");
            params.add("%" + destino.trim() + "%");
        }
        if (usuarioAgencia != null && !usuarioAgencia.isBlank()) {
            sql.append("AND (LOWER(u.Username) LIKE LOWER(?) OR LOWER(a.Nombre) LIKE LOWER(?)) ");
            String like = "%" + usuarioAgencia.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (tipoBusquedaId != null) {
            sql.append("AND b.TipoBusquedaID = ? ");
            params.add(tipoBusquedaId);
        }
        if (fechaDesde != null) {
            sql.append("AND TRUNC(b.Fecha) >= ? ");
            params.add(fechaDesde);
        }
        if (fechaHasta != null) {
            sql.append("AND TRUNC(b.Fecha) <= ? ");
            params.add(fechaHasta);
        }

        List<Integer> result = DatabaseManager.executeQuery(
                sql.toString(), rs -> rs.getInt("TOTAL"), params.toArray()
        );
        return result.isEmpty() ? 0 : result.get(0);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RESUMEN PARA EL DASHBOARD
    //    - totalWeb  (TipoBusquedaID = 1)
    //    - totalRest (TipoBusquedaID = 2)
    //    - porDia    (últimos 30 días, agrupado por fecha)
    //    - topDestinos (top 10 ciudades más buscadas)
    // ════════════════════════════════════════════════════════════════════════

    public int contarPorTipo(Integer tipoBusquedaId) {
        String sql = tipoBusquedaId == null
                ? "SELECT COUNT(*) AS TOTAL FROM Busqueda"
                : "SELECT COUNT(*) AS TOTAL FROM Busqueda WHERE TipoBusquedaID = ?";

        Object[] params = tipoBusquedaId == null ? new Object[]{} : new Object[]{tipoBusquedaId};

        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt("TOTAL"), params);
        return result.isEmpty() ? 0 : result.get(0);
    }

    /** Retorna lista de {dia: "YYYY-MM-DD", total: N} para los últimos 30 días */
    public List<Map<String, Object>> busquedasPorDia() {
        String sql =
                "SELECT TO_CHAR(b.Fecha, 'YYYY-MM-DD') AS DIA, COUNT(*) AS TOTAL " +
                        "FROM   Busqueda b " +
                        "WHERE  b.Fecha >= SYSDATE - 30 " +
                        "GROUP  BY TO_CHAR(b.Fecha, 'YYYY-MM-DD') " +
                        "ORDER  BY DIA ASC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("dia",   rs.getString("DIA"));
            row.put("total", rs.getInt("TOTAL"));
            return row;
        });
    }

    /** Retorna los 10 destinos (ciudades) más buscados: {nombre, total} */
    public List<Map<String, Object>> topDestinos() {
        String sql =
                "SELECT c.Nombre AS Ciudad, COUNT(*) AS TOTAL " +
                        "FROM   Busqueda b " +
                        "JOIN   Ciudad c ON b.CiudadID = c.ID " +
                        "GROUP  BY c.Nombre " +
                        "ORDER  BY TOTAL DESC " +
                        "FETCH  FIRST 10 ROWS ONLY";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nombre", rs.getString("Ciudad"));
            row.put("total",  rs.getInt("TOTAL"));
            return row;
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXPORTAR: todas las búsquedas que coincidan con los filtros (sin paginación)
    // ════════════════════════════════════════════════════════════════════════

    public List<Map<String, Object>> exportar(
            String destino, String usuarioAgencia, Integer tipoBusquedaId,
            Date fechaDesde, Date fechaHasta) {

        StringBuilder sql = new StringBuilder(
                "SELECT b.ID, " +
                        "       c.Nombre      AS Ciudad, " +
                        "       b.FechaCheckIn, " +
                        "       b.FechaCheckOut, " +
                        "       b.CantidadPersonas, " +
                        "       u.Username    AS Usuario, " +
                        "       a.Nombre      AS Agencia, " +
                        "       tb.Estado     AS TipoBusqueda, " +
                        "       b.Fecha       AS FechaHora " +
                        "FROM   Busqueda b " +
                        "JOIN   Ciudad        c  ON b.CiudadID        = c.ID " +
                        "LEFT JOIN Usuario    u  ON b.UsuarioID       = u.ID " +
                        "LEFT JOIN Agencia    a  ON b.AgenciaID       = a.ID " +
                        "LEFT JOIN TipoBusqueda tb ON b.TipoBusquedaID = tb.ID " +
                        "WHERE  1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (destino != null && !destino.isBlank()) {
            sql.append("AND LOWER(c.Nombre) LIKE LOWER(?) ");
            params.add("%" + destino.trim() + "%");
        }
        if (usuarioAgencia != null && !usuarioAgencia.isBlank()) {
            sql.append("AND (LOWER(u.Username) LIKE LOWER(?) OR LOWER(a.Nombre) LIKE LOWER(?)) ");
            String like = "%" + usuarioAgencia.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (tipoBusquedaId != null) {
            sql.append("AND b.TipoBusquedaID = ? ");
            params.add(tipoBusquedaId);
        }
        if (fechaDesde != null) {
            sql.append("AND TRUNC(b.Fecha) >= ? ");
            params.add(fechaDesde);
        }
        if (fechaHasta != null) {
            sql.append("AND TRUNC(b.Fecha) <= ? ");
            params.add(fechaHasta);
        }

        sql.append("ORDER BY b.Fecha DESC, b.ID DESC");

        return DatabaseManager.executeQuery(sql.toString(), rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id",       rs.getInt("ID"));
            row.put("destino",  rs.getString("Ciudad"));
            row.put("checkIn",  rs.getDate("FechaCheckIn")  != null ? rs.getDate("FechaCheckIn").toString()  : null);
            row.put("checkOut", rs.getDate("FechaCheckOut") != null ? rs.getDate("FechaCheckOut").toString() : null);
            row.put("personas", rs.getInt("CantidadPersonas"));
            String tipoBusqueda = rs.getString("TipoBusqueda");
            boolean esRest = tipoBusqueda != null && tipoBusqueda.equalsIgnoreCase("Agencia");
            row.put("tipo",     esRest ? "REST" : "Web");
            row.put("usuario",  rs.getString("Usuario") != null ? rs.getString("Usuario") : rs.getString("Agencia"));
            return row;
        }, params.toArray());
    }
}