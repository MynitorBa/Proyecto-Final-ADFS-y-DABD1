package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadHotelDTO;
import org.example.dtos.HotelResultadoDTO;
import org.example.dtos.TipoHabitacionResultadoDTO;
import org.example.dtos.HabitacionResumenDTO;

import java.sql.Date;
import java.util.List;

public class BusquedaAgenciaRepository {

    // ── Descuento de la agencia asociada al usuario webservice ────────────────

    public Double obtenerDescuentoAgencia(int usuarioId) {
        String sql = "SELECT PorcentajeDescuento FROM Agencia " +
                "WHERE UsuarioWEBIs_ID = ? AND EstadoID = " +
                "(SELECT ID FROM EstadoAgencia WHERE LOWER(Estado) = 'activa')";
        List<Double> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getDouble("PorcentajeDescuento"), usuarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    // ── Buscar ciudad ─────────────────────────────────────────────────────────

    public Integer buscarCiudadId(String nombreCiudad, String nombrePais) {
        String sql = "SELECT c.ID FROM Ciudad c JOIN Pais p ON c.Pais_ID = p.ID " +
                "WHERE LOWER(TRIM(c.Nombre)) = LOWER(TRIM(?)) AND LOWER(TRIM(p.Nombre)) = LOWER(TRIM(?))";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("ID"), nombreCiudad, nombrePais
        );
        return result.isEmpty() ? null : result.get(0);
    }

    // ── Guardar búsqueda — tipo 2 (Agencia) ──────────────────────────────────

    public void guardarBusqueda(int ciudadId, Date fechaCheckIn, Date fechaCheckOut,
                                int cantidadPersonas, int usuarioId) {
        String sql = "INSERT INTO Busqueda " +
                "(CiudadID, FechaCheckIn, FechaCheckOut, CantidadPersonas, UsuarioID, AgenciaID, TipoBusquedaID, Fecha) " +
                "VALUES (?, ?, ?, ?, ?, NULL, 2, SYSDATE)";
        DatabaseManager.executeUpdate(sql, ciudadId, fechaCheckIn, fechaCheckOut, cantidadPersonas, usuarioId);
    }

    // ── Hoteles activos en una ciudad ─────────────────────────────────────────

    public List<HotelResultadoDTO> buscarHotelesPorCiudad(int ciudadId) {
        String sql = "SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, " +
                "e.Estado, c.Nombre AS Ciudad, p.Nombre AS Pais " +
                "FROM Hotel h " +
                "JOIN Estado  e ON h.EstadoID = e.ID " +
                "JOIN Ciudad  c ON h.CiudadID = c.ID " +
                "JOIN Pais    p ON c.Pais_ID  = p.ID " +
                "WHERE h.CiudadID = ? AND LOWER(TRIM(e.Estado)) = 'activo'";

        return DatabaseManager.executeQuery(sql, rs -> {
            HotelResultadoDTO dto = new HotelResultadoDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setDireccion(rs.getString("Direccion"));
            dto.setDescripcion(rs.getString("Descripcion"));
            dto.setRating(rs.getDouble("Rating"));
            dto.setEstado(rs.getString("Estado"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        }, ciudadId);
    }

    public List<Integer> buscarImagenesHotel(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }

    public List<AmenidadHotelDTO> buscarAmenidadesHotel(int hotelId) {
        String sql = "SELECT ha.ID AS HotelAmenidadId, ha.AmenidadID, ha.Descripcion, " +
                "a.nombre AS NombreAmenidad " +
                "FROM HotelAmenidad ha " +
                "JOIN Amenidad a ON ha.AmenidadID = a.ID " +
                "WHERE ha.HotelID = ?";

        return DatabaseManager.executeQuery(sql, rs -> {
            AmenidadHotelDTO dto = new AmenidadHotelDTO();
            dto.setHotelAmenidadId(rs.getInt("HotelAmenidadId"));
            dto.setAmenidadId(rs.getInt("AmenidadID"));
            dto.setNombre(rs.getString("NombreAmenidad"));
            dto.setDescripcion(rs.getString("Descripcion"));
            return dto;
        }, hotelId);
    }

    public List<Integer> buscarImagenesAmenidad(int hotelAmenidadId) {
        String sql = "SELECT ID FROM ImagenHotelAmenidad WHERE HotelAmenidadID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelAmenidadId);
    }

    // ── Tipos de habitación disponibles (capacidad >= mínima, sin traslape) ──
    //    Precio y cama vienen de TipoHabitacion, ya NO de Habitacion.

    public List<TipoHabitacionResultadoDTO> buscarTiposHabitacionDisponibles(
            int hotelId, int capacidadMinima, Date fechaCheckIn, Date fechaCheckOut) {

        String sql = "SELECT t.ID AS TipoID, t.NOMBRE AS TipoHabitacion, " +
                "t.PRECIOPERSONA, t.PRECIONOCHE, t.CAPACIDADMAXIMA, " +
                "t.METROSCUADRADOS, c.TIPO_DE_CLASE AS TipoCama " +
                "FROM TipoHabitacion t " +
                "JOIN Cama c ON t.TIPOCAMAID = c.ID " +
                "WHERE t.CAPACIDADMAXIMA >= ? " +
                "AND EXISTS (" +
                "  SELECT 1 FROM Habitacion h " +
                "  JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID " +
                "  WHERE h.TIPOHABITACIONID = t.ID " +
                "  AND h.HOTELID = ? " +
                "  AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa' " +
                "  AND h.ID NOT IN (" +
                "    SELECT dr.HabitacionID FROM DetallesReservacion dr " +
                "    JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                "    JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "    WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                "    AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?" +
                "  )" +
                ")";

        return DatabaseManager.executeQuery(sql, rs -> {
            TipoHabitacionResultadoDTO dto = new TipoHabitacionResultadoDTO();
            dto.setTipoHabitacionId(rs.getInt("TipoID"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setPrecioPorPersona(rs.getDouble("PRECIOPERSONA"));
            dto.setPrecioPorNoche(rs.getDouble("PRECIONOCHE"));
            dto.setCapacidadMaxima(rs.getInt("CAPACIDADMAXIMA"));
            dto.setMetrosCuadrados(rs.getDouble("METROSCUADRADOS"));
            dto.setTipoCama(rs.getString("TipoCama"));
            return dto;
        }, capacidadMinima, hotelId, fechaCheckOut, fechaCheckIn);
    }

    // ── Habitaciones concretas disponibles de un tipo (para reservar) ─────────

    public List<HabitacionResumenDTO> buscarHabitacionesResumenPorTipo(
            int hotelId, int tipoHabitacionId, Date fechaCheckIn, Date fechaCheckOut) {

        String sql = "SELECT h.ID, h.NUMEROHABITACION " +
                "FROM Habitacion h " +
                "JOIN EstadoHabitacion e ON h.ESTADO_ID = e.ID " +
                "WHERE h.HOTELID = ? " +
                "AND h.TIPOHABITACIONID = ? " +
                "AND LOWER(TRIM(e.TIPO_DE_CLASE)) = 'activa' " +
                "AND h.ID NOT IN (" +
                "  SELECT dr.HabitacionID FROM DetallesReservacion dr " +
                "  JOIN Reservacion r ON dr.ReservacionID = r.ID " +
                "  JOIN EstadoReserva er ON r.EstadoID = er.ID " +
                "  WHERE LOWER(TRIM(er.Estado)) IN ('pendiente', 'confirmada') " +
                "  AND dr.FechaCheckIn < ? AND dr.FechaCheckOut > ?" +
                ")";

        return DatabaseManager.executeQuery(sql, rs -> {
            HabitacionResumenDTO dto = new HabitacionResumenDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNumeroHabitacion(rs.getString("NUMEROHABITACION"));
            return dto;
        }, hotelId, tipoHabitacionId, fechaCheckOut, fechaCheckIn);
    }

    // ── Imágenes de habitación ────────────────────────────────────────────────

    public List<Integer> buscarImagenesHabitacion(int habitacionId) {
        String sql = "SELECT ID FROM ImagenHabitacion WHERE HabitacionID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), habitacionId);
    }
}