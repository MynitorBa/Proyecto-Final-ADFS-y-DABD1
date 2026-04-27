package org.example.repositories;

import org.example.data.DatabaseManager;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository para consultas del modulo de reportes de busquedas en el panel de administracion.
 * Soporta filtros por destino, usuario o agencia, tipo de busqueda y rango de fechas.
 */
public class AdminBusquedaRepository {

    /**
     * Retorna una pagina de busquedas aplicando los filtros indicados.
     * Todos los parametros de filtro son opcionales; si son null o vacios se ignoran.
     * @param destino        nombre de ciudad a filtrar con LIKE, o null para no filtrar.
     * @param usuarioAgencia username del usuario o nombre de la agencia a filtrar, o null.
     * @param tipoBusquedaId 1 para busquedas web, 2 para busquedas REST, null para todas.
     * @param fechaDesde     fecha minima de la busqueda, o null para no filtrar.
     * @param fechaHasta     fecha maxima de la busqueda, o null para no filtrar.
     * @param offset         numero de filas a saltar para la paginacion.
     * @param porPagina      cantidad de filas a retornar por pagina.
     * @return lista de mapas con los datos de cada busqueda encontrada.
     */
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

    /**
     * Cuenta el total de busquedas que coinciden con los filtros indicados.
     * Se usa para calcular el total de paginas en la paginacion.
     * @param destino        nombre de ciudad a filtrar con LIKE, o null para no filtrar.
     * @param usuarioAgencia username del usuario o nombre de la agencia a filtrar, o null.
     * @param tipoBusquedaId 1 para busquedas web, 2 para busquedas REST, null para todas.
     * @param fechaDesde     fecha minima de la busqueda, o null para no filtrar.
     * @param fechaHasta     fecha maxima de la busqueda, o null para no filtrar.
     * @return total de busquedas que coinciden con los filtros.
     */
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

    /**
     * Cuenta el total de busquedas filtradas por tipo.
     * Si tipoBusquedaId es null retorna el total sin filtro de tipo.
     * @param tipoBusquedaId 1 para web, 2 para REST, null para todas.
     * @return total de busquedas del tipo indicado.
     */
    public int contarPorTipo(Integer tipoBusquedaId) {
        String sql = tipoBusquedaId == null
                ? "SELECT COUNT(*) AS TOTAL FROM Busqueda"
                : "SELECT COUNT(*) AS TOTAL FROM Busqueda WHERE TipoBusquedaID = ?";

        Object[] params = tipoBusquedaId == null ? new Object[]{} : new Object[]{tipoBusquedaId};

        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt("TOTAL"), params);
        return result.isEmpty() ? 0 : result.get(0);
    }

    /**
     * Retorna el conteo de busquedas agrupado por dia para los ultimos 30 dias.
     * Cada elemento del resultado contiene la fecha y el total de busquedas de ese dia.
     * @return lista de mapas con los campos "dia" (YYYY-MM-DD) y "total".
     */
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

    /**
     * Retorna los 10 destinos mas buscados ordenados por frecuencia descendente.
     * Cada elemento contiene el nombre de la ciudad y el total de busquedas.
     * @return lista de mapas con los campos "nombre" y "total".
     */
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
            return row;
        });
    }

    /**
     * Retorna todas las busquedas que coinciden con los filtros sin paginacion.
     * Se usa para generar el reporte completo a exportar por correo.
     row.put("total",  rs.getInt("TOTAL"));
     * @param destino        nombre de ciudad a filtrar con LIKE, o null para no filtrar.
     * @param usuarioAgencia username del usuario o nombre de la agencia a filtrar, o null.
     * @param tipoBusquedaId 1 para busquedas web, 2 para busquedas REST, null para todas.
     * @param fechaDesde     fecha minima de la busqueda, o null para no filtrar.
     * @param fechaHasta     fecha maxima de la busqueda, o null para no filtrar.
     * @return lista de mapas con los datos de cada busqueda encontrada.
     */
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
            // Fecha real de la búsqueda (timestamp completo)
            java.sql.Timestamp ts = rs.getTimestamp("FechaHora");
            row.put("fechaHora", ts != null ? ts.toString().substring(0, 16).replace(' ', 'T') : null);
            return row;
        }, params.toArray());
    }
}