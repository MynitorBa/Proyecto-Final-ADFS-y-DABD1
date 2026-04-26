package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository para el modulo de metricas del panel de administracion.
 * Todas las consultas reciben fechaDesde/fechaHasta como strings YYYY-MM-DD
 * que se convierten a DATE con TO_DATE en Oracle.
 */
public class MetricasHotelRepository {

    // ───────────────────────── PANEL 1: RESUMEN ─────────────────────────────

    /**
     * KPI de ingresos: totales, por canal (Directo / Agencia), ticket promedio,
     * total de reservaciones y reservaciones pagadas (confirmada + completada).
     */
    public Map<String, Object> obtenerIngresosKpi(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT " +
            "  NVL(SUM(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') THEN r.Total ELSE 0 END), 0) AS IngresosTotales, " +
            "  NVL(SUM(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') " +
            "    AND NOT EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) THEN r.Total ELSE 0 END), 0) AS IngresosDirecto, " +
            "  NVL(SUM(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') " +
            "    AND EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) THEN r.Total ELSE 0 END), 0) AS IngresosAgencia, " +
            "  COUNT(*) AS TotalReservaciones, " +
            "  COUNT(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') THEN 1 END) AS ReservacionesPagadas, " +
            "  NVL(AVG(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') THEN r.Total END), 0) AS TicketPromedio " +
            "FROM Reservacion r " +
            "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
            "WHERE TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD')";

        List<Map<String, Object>> rows = DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ingresosTotales",       rs.getDouble("IngresosTotales"));
            row.put("ingresosDirecto",        rs.getDouble("IngresosDirecto"));
            row.put("ingresosAgencia",        rs.getDouble("IngresosAgencia"));
            row.put("totalReservaciones",     rs.getInt("TotalReservaciones"));
            row.put("reservacionesPagadas",   rs.getInt("ReservacionesPagadas"));
            row.put("ticketPromedio",         rs.getDouble("TicketPromedio"));
            return row;
        }, fechaDesde, fechaHasta);

        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    /**
     * Reservaciones creadas por dia en el rango indicado.
     * Retorna [{fecha: "YYYY-MM-DD", total: N}].
     */
    public List<Map<String, Object>> reservacionesPorDia(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT TO_CHAR(r.Fecha_Creacion, 'YYYY-MM-DD') AS Fecha, COUNT(*) AS Total " +
            "FROM Reservacion r " +
            "WHERE TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY TO_CHAR(r.Fecha_Creacion, 'YYYY-MM-DD') " +
            "ORDER BY Fecha ASC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("fecha", rs.getString("Fecha"));
            row.put("total", rs.getInt("Total"));
            return row;
        }, fechaDesde, fechaHasta);
    }

    /**
     * Division de reservaciones por canal: Directo vs Agencia.
     * Retorna [{canal: "Directo"|"Agencia", total: N}].
     */
    public List<Map<String, Object>> canalSplit(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT " +
            "  CASE WHEN EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) " +
            "       THEN 'Agencia' ELSE 'Directo' END AS Canal, " +
            "  COUNT(*) AS Total " +
            "FROM Reservacion r " +
            "WHERE TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY CASE WHEN EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) " +
            "              THEN 'Agencia' ELSE 'Directo' END " +
            "ORDER BY Total DESC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("canal", rs.getString("Canal"));
            row.put("total", rs.getInt("Total"));
            return row;
        }, fechaDesde, fechaHasta);
    }

    // ───────────────────────── PANEL 2: NEGOCIO ─────────────────────────────

    /**
     * Embudo de conversion: distribucion de reservaciones por estado final.
     * Retorna un mapa con completadas, pagadas, pendientes, expiradas, canceladas.
     */
    public Map<String, Object> embudo(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN LOWER(TRIM(er.Estado)) = 'completada'  THEN 1 ELSE 0 END) AS Completadas, " +
            "  SUM(CASE WHEN LOWER(TRIM(er.Estado)) = 'confirmada'  THEN 1 ELSE 0 END) AS Pagadas, " +
            "  SUM(CASE WHEN LOWER(TRIM(er.Estado)) = 'pendiente'   THEN 1 ELSE 0 END) AS Pendientes, " +
            "  SUM(CASE WHEN LOWER(TRIM(er.Estado)) = 'expirada'    THEN 1 ELSE 0 END) AS Expiradas, " +
            "  SUM(CASE WHEN LOWER(TRIM(er.Estado)) = 'cancelada'   THEN 1 ELSE 0 END) AS Canceladas " +
            "FROM Reservacion r " +
            "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
            "WHERE TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD')";

        List<Map<String, Object>> rows = DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("completadas", rs.getInt("Completadas"));
            row.put("pagadas",     rs.getInt("Pagadas"));
            row.put("pendientes",  rs.getInt("Pendientes"));
            row.put("expiradas",   rs.getInt("Expiradas"));
            row.put("canceladas",  rs.getInt("Canceladas"));
            return row;
        }, fechaDesde, fechaHasta);

        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    /**
     * Top hoteles por ingresos confirmados en el rango.
     * Retorna hasta 10 hoteles con nombre, reservaciones e ingresos.
     */
    public List<Map<String, Object>> topHoteles(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT h.Nombre AS Hotel, " +
            "  COUNT(DISTINCT r.ID) AS TotalReservaciones, " +
            "  NVL(SUM(CASE WHEN LOWER(TRIM(er.Estado)) IN ('confirmada','completada') THEN r.Total ELSE 0 END), 0) AS IngresosTotales " +
            "FROM Hotel h " +
            "JOIN Habitacion hab ON hab.HotelID = h.ID " +
            "JOIN DetallesReservacion dr ON dr.HabitacionID = hab.ID " +
            "JOIN Reservacion r ON dr.ReservacionID = r.ID " +
            "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
            "WHERE TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY h.Nombre " +
            "ORDER BY IngresosTotales DESC " +
            "FETCH FIRST 10 ROWS ONLY";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("hotel",              rs.getString("Hotel"));
            row.put("totalReservaciones", rs.getInt("TotalReservaciones"));
            row.put("ingresosTotales",    rs.getDouble("IngresosTotales"));
            return row;
        }, fechaDesde, fechaHasta);
    }

    /**
     * Cancelaciones agrupadas por quien las originó:
     * tipos 14/15 = Usuario, 16/17 = Agencia, 19/20 = Administrador.
     * Retorna [{tipo: "Usuario"|"Agencia"|"Administrador", total: N}].
     */
    public List<Map<String, Object>> cancelacionesPorTipo(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT " +
            "  CASE " +
            "    WHEN lr.TipoEventoID IN (14,15) THEN 'Usuario' " +
            "    WHEN lr.TipoEventoID IN (16,17) THEN 'Agencia' " +
            "    WHEN lr.TipoEventoID IN (19,20) THEN 'Administrador' " +
            "    ELSE 'Otro' " +
            "  END AS Tipo, " +
            "  COUNT(*) AS Total " +
            "FROM LogReservacion lr " +
            "WHERE lr.TipoEventoID IN (14,15,16,17,19,20) " +
            "  AND TRUNC(lr.Fecha) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(lr.Fecha) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY CASE " +
            "    WHEN lr.TipoEventoID IN (14,15) THEN 'Usuario' " +
            "    WHEN lr.TipoEventoID IN (16,17) THEN 'Agencia' " +
            "    WHEN lr.TipoEventoID IN (19,20) THEN 'Administrador' " +
            "    ELSE 'Otro' " +
            "  END " +
            "ORDER BY Total DESC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("tipo",  rs.getString("Tipo"));
            row.put("total", rs.getInt("Total"));
            return row;
        }, fechaDesde, fechaHasta);
    }

    /**
     * Tendencia de ingresos mes a mes separada por canal (Directo / Agencia).
     * Solo incluye reservaciones confirmadas o completadas.
     * Retorna [{mes: "YYYY-MM", canal: "Directo"|"Agencia", revenue: N}].
     */
    public List<Map<String, Object>> ingresosTendencia(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT TO_CHAR(r.Fecha_Creacion, 'YYYY-MM') AS Mes, " +
            "  CASE WHEN EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) " +
            "       THEN 'Agencia' ELSE 'Directo' END AS Canal, " +
            "  NVL(SUM(r.Total), 0) AS Revenue " +
            "FROM Reservacion r " +
            "JOIN EstadoReserva er ON r.EstadoID = er.ID " +
            "WHERE LOWER(TRIM(er.Estado)) IN ('confirmada','completada') " +
            "  AND TRUNC(r.Fecha_Creacion) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(r.Fecha_Creacion) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY TO_CHAR(r.Fecha_Creacion, 'YYYY-MM'), " +
            "  CASE WHEN EXISTS (SELECT 1 FROM Agencia a WHERE a.UsuarioWebis_ID = r.Usuario_ID) " +
            "       THEN 'Agencia' ELSE 'Directo' END " +
            "ORDER BY Mes ASC";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("mes",     rs.getString("Mes"));
            row.put("canal",   rs.getString("Canal"));
            row.put("revenue", rs.getDouble("Revenue"));
            return row;
        }, fechaDesde, fechaHasta);
    }

    /**
     * Mapa de calor de busquedas de hotel por dia de semana y hora del dia.
     * Retorna [{diaSemana: 1-7, hora: 0-23, total: N}] donde 1=Domingo en Oracle.
     */
    public List<Map<String, Object>> heatmap(String fechaDesde, String fechaHasta) {
        String sql =
            "SELECT TO_NUMBER(TO_CHAR(b.Fecha, 'D'))  AS DiaSemana, " +
            "       TO_NUMBER(TO_CHAR(b.Fecha, 'HH24')) AS Hora, " +
            "       COUNT(*) AS Total " +
            "FROM Busqueda b " +
            "WHERE TRUNC(b.Fecha) >= TO_DATE(?, 'YYYY-MM-DD') " +
            "  AND TRUNC(b.Fecha) <= TO_DATE(?, 'YYYY-MM-DD') " +
            "GROUP BY TO_NUMBER(TO_CHAR(b.Fecha, 'D')), " +
            "         TO_NUMBER(TO_CHAR(b.Fecha, 'HH24')) " +
            "ORDER BY DiaSemana, Hora";

        return DatabaseManager.executeQuery(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("diaSemana", rs.getInt("DiaSemana"));
            row.put("hora",      rs.getInt("Hora"));
            row.put("total",     rs.getInt("Total"));
            return row;
        }, fechaDesde, fechaHasta);
    }
}
