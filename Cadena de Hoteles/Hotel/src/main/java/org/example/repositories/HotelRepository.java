package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.AmenidadDTO;
import org.example.dtos.HabitacionAdminDTO;
import org.example.dtos.HotelAdminDTO;
import org.example.dtos.HotelAmenidadDTO;

import java.util.List;

public class HotelRepository {

    // ════════════════════════════════════════════════════
    //  CATÁLOGO DE AMENIDADES
    // ════════════════════════════════════════════════════

    public List<AmenidadDTO> listarAmenidades() {
        return DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Amenidad ORDER BY ID",
                rs -> {
                    AmenidadDTO d = new AmenidadDTO();
                    d.setId(rs.getInt("ID"));
                    d.setNombre(rs.getString("Nombre"));
                    return d;
                }
        );
    }

    // ════════════════════════════════════════════════════
    //  HOTEL — listado, creación, edición, eliminación
    // ════════════════════════════════════════════════════

    public List<HotelAdminDTO> listarTodos() {
        String sql = """
                SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating,
                       h.EstadoID, e.Estado,
                       c.Nombre AS Ciudad,
                       p.Nombre AS Pais
                FROM   Hotel  h
                JOIN   Estado e ON h.EstadoID  = e.ID
                JOIN   Ciudad c ON h.CiudadID  = c.ID
                JOIN   Pais   p ON c.Pais_ID   = p.ID
                ORDER BY h.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HotelAdminDTO dto = new HotelAdminDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setDireccion(rs.getString("Direccion"));
            dto.setDescripcion(rs.getString("Descripcion"));
            dto.setRating(rs.getDouble("Rating"));
            dto.setEstadoId(rs.getInt("EstadoID"));
            dto.setEstado(rs.getString("Estado"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        });
    }

    public int crearHotel(String nombre, String direccion, String descripcion,
                          double rating, int estadoId, int ciudadId) {
        String sql = "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID",
                nombre, direccion, descripcion, rating, estadoId, ciudadId);
    }

    public void actualizarHotel(int hotelId, String nombre, String direccion,
                                String descripcion, double rating, int estadoId) {
        DatabaseManager.executeUpdate(
                "UPDATE Hotel SET Nombre=?, Direccion=?, Descripcion=?, Rating=?, EstadoID=? WHERE ID=?",
                nombre, direccion, descripcion, rating, estadoId, hotelId);
    }

    /** Elimina un hotel y TODOS sus datos asociados (imágenes, amenidades, habitaciones). */
    public void eliminarHotel(int hotelId) {
        // 1) Eliminar imágenes de habitaciones del hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHabitacion WHERE HabitacionID IN (SELECT ID FROM Habitacion WHERE HotelID=?)",
                hotelId);

        // 2) Eliminar habitaciones del hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE HotelID=?", hotelId);

        // 3) Eliminar imágenes de amenidades del hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHotelAmenidad WHERE HotelAmenidadID IN (SELECT ID FROM HotelAmenidad WHERE HotelID=?)",
                hotelId);

        // 4) Eliminar amenidades del hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM HotelAmenidad WHERE HotelID=?", hotelId);

        // 5) Eliminar imágenes del hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHotel WHERE HotelID=?", hotelId);

        // 6) Eliminar el hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID=?", hotelId);
    }

    public boolean existe(int hotelId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Hotel WHERE ID=?", rs -> rs.getInt(1), hotelId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    public int contarHabitaciones(int hotelId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Habitacion WHERE HotelID=?", rs -> rs.getInt(1), hotelId);
        return r.isEmpty() ? 0 : r.get(0);
    }

    // ════════════════════════════════════════════════════
    //  AMENIDADES DEL HOTEL
    // ════════════════════════════════════════════════════

    /** Devuelve todas las amenidades asignadas a un hotel con sus ids de imagen. */
    public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId) {
        String sql = """
                SELECT ha.ID, ha.HotelID, ha.AmenidadID, a.Nombre AS AmenidadNombre, ha.Descripcion
                FROM   HotelAmenidad ha
                JOIN   Amenidad      a  ON ha.AmenidadID = a.ID
                WHERE  ha.HotelID = ?
                ORDER BY ha.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HotelAmenidadDTO d = new HotelAmenidadDTO();
            d.setId(rs.getInt("ID"));
            d.setHotelId(rs.getInt("HotelID"));
            d.setAmenidadId(rs.getInt("AmenidadID"));
            d.setAmenidadNombre(rs.getString("AmenidadNombre"));
            d.setDescripcion(rs.getString("Descripcion"));
            return d;
        }, hotelId);
    }

    /** Devuelve true si el hotel ya tiene esa amenidad asignada. */
    public boolean tieneAmenidad(int hotelId, int amenidadId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM HotelAmenidad WHERE HotelID=? AND AmenidadID=?",
                rs -> rs.getInt(1), hotelId, amenidadId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    /** Inserta una amenidad en un hotel y devuelve el ID de HotelAmenidad generado. */
    public int agregarAmenidadHotel(int hotelId, int amenidadId, String descripcion) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO HotelAmenidad (HotelID, AmenidadID, Descripcion) VALUES (?, ?, ?)",
                "ID", hotelId, amenidadId, descripcion);
    }

    /** Actualiza la descripción de una amenidad de hotel. */
    public void actualizarAmenidadHotel(int hotelAmenidadId, String descripcion) {
        DatabaseManager.executeUpdate(
                "UPDATE HotelAmenidad SET Descripcion=? WHERE ID=?",
                descripcion, hotelAmenidadId);
    }

    public void eliminarAmenidadHotel(int hotelAmenidadId) {
        // Primero eliminar imágenes asociadas
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHotelAmenidad WHERE HotelAmenidadID=?", hotelAmenidadId);
        DatabaseManager.executeUpdate(
                "DELETE FROM HotelAmenidad WHERE ID=?", hotelAmenidadId);
    }

    public List<Integer> obtenerImagenesAmenidadIds(int hotelAmenidadId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotelAmenidad WHERE HotelAmenidadID=? ORDER BY ID",
                rs -> rs.getInt("ID"), hotelAmenidadId);
    }

    public int agregarImagenAmenidad(int hotelAmenidadId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHotelAmenidad (HotelAmenidadID, Imagen) VALUES (?, ?)",
                "ID", hotelAmenidadId, imagen);
    }

    public void eliminarImagenAmenidad(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotelAmenidad WHERE ID=?", imagenId);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HOTEL
    // ════════════════════════════════════════════════════

    public List<Integer> obtenerImagenesIds(int hotelId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHotel WHERE HotelID=? ORDER BY ID",
                rs -> rs.getInt("ID"), hotelId);
    }

    public int agregarImagenHotel(int hotelId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHotel (HotelID, Imagen) VALUES (?, ?)",
                "ID", hotelId, imagen);
    }

    public void eliminarImagenHotel(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotel WHERE ID=?", imagenId);
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — listado, creación, edición, eliminación
    // ════════════════════════════════════════════════════

    public List<HabitacionAdminDTO> listarHabitacionesPorHotel(int hotelId) {
        String sql = """
                SELECT h.ID, h.HotelID,
                       h.TipoHabitacionID, t.Nombre AS TipoHabitacion,
                       h.CamaID, c.Tipo_de_clase AS TipoCama,
                       h.Precio_por_Persona, h.Precio_por_Noche,
                       h.Capacidad_Maxima, h.Metros_Cuadrados,
                       h.Descripcion,
                       h.Estado_ID, e.Tipo_de_clase AS Estado
                FROM   Habitacion       h
                JOIN   TipoHabitacion   t ON h.TipoHabitacionID = t.ID
                JOIN   Cama             c ON h.CamaID           = c.ID
                JOIN   EstadoHabitacion e ON h.Estado_ID        = e.ID
                WHERE  h.HotelID = ?
                ORDER BY h.ID
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            HabitacionAdminDTO d = new HabitacionAdminDTO();
            d.setId(rs.getInt("ID"));
            d.setHotelId(rs.getInt("HotelID"));
            d.setTipoHabitacionId(rs.getInt("TipoHabitacionID"));
            d.setTipoHabitacion(rs.getString("TipoHabitacion"));
            d.setCamaId(rs.getInt("CamaID"));
            d.setTipoCama(rs.getString("TipoCama"));
            d.setPrecioPorPersona(rs.getDouble("Precio_por_Persona"));
            d.setPrecioPorNoche(rs.getDouble("Precio_por_Noche"));
            d.setCapacidadMaxima(rs.getInt("Capacidad_Maxima"));
            d.setMetrosCuadrados(rs.getDouble("Metros_Cuadrados"));
            d.setDescripcion(rs.getString("Descripcion"));
            d.setEstadoId(rs.getInt("Estado_ID"));
            d.setEstado(rs.getString("Estado"));
            return d;
        }, hotelId);
    }

    public int crearHabitacion(int hotelId, int tipoHabitacionId, int camaId,
                               double precioPorPersona, double precioPorNoche,
                               int capacidadMaxima, double metrosCuadrados,
                               String descripcion, int estadoId) {
        String sqlHab = "INSERT INTO Habitacion " +
                "(HotelID, TipoHabitacionID, CamaID, Precio_por_Persona, Precio_por_Noche, " +
                " Capacidad_Maxima, Metros_Cuadrados, Descripcion, Estado_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return DatabaseManager.executeInsertReturnId(sqlHab, "ID",
                hotelId, tipoHabitacionId, camaId, precioPorPersona, precioPorNoche,
                capacidadMaxima, metrosCuadrados, descripcion, estadoId);
    }

    public boolean existeHabitacion(int habitacionId) {
        List<Integer> r = DatabaseManager.executeQuery(
                "SELECT COUNT(*) FROM Habitacion WHERE ID=?", rs -> rs.getInt(1), habitacionId);
        return !r.isEmpty() && r.get(0) > 0;
    }

    public void actualizarHabitacion(int habitacionId, int tipoHabitacionId, int camaId,
                                     double precioPorPersona, double precioPorNoche,
                                     int capacidadMaxima, double metrosCuadrados,
                                     String descripcion, int estadoId) {
        DatabaseManager.executeUpdate(
                "UPDATE Habitacion SET TipoHabitacionID=?, CamaID=?, Precio_por_Persona=?, " +
                        "Precio_por_Noche=?, Capacidad_Maxima=?, Metros_Cuadrados=?, Descripcion=?, Estado_ID=? " +
                        "WHERE ID=?",
                tipoHabitacionId, camaId, precioPorPersona, precioPorNoche,
                capacidadMaxima, metrosCuadrados, descripcion, estadoId, habitacionId);
    }

    /** Elimina una habitación y todas sus imágenes asociadas. */
    public void eliminarHabitacion(int habitacionId) {
        // 1) Eliminar imágenes de la habitación
        DatabaseManager.executeUpdate(
                "DELETE FROM ImagenHabitacion WHERE HabitacionID=?", habitacionId);
        // 2) Eliminar la habitación
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID=?", habitacionId);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HABITACIÓN
    // ════════════════════════════════════════════════════

    public List<Integer> obtenerImagenesHabitacionIds(int habitacionId) {
        return DatabaseManager.executeQuery(
                "SELECT ID FROM ImagenHabitacion WHERE HabitacionID=? ORDER BY ID",
                rs -> rs.getInt("ID"), habitacionId);
    }

    public int agregarImagenHabitacion(int habitacionId, byte[] imagen) {
        return DatabaseManager.executeInsertReturnId(
                "INSERT INTO ImagenHabitacion (HabitacionID, Imagen) VALUES (?, ?)",
                "ID", habitacionId, imagen);
    }

    public void eliminarImagenHabitacion(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHabitacion WHERE ID=?", imagenId);
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — RESERVACIONES
    // ════════════════════════════════════════════════════

    public List<java.util.Map<String, Object>> listarTodasReservaciones() {
        String sql = """
                SELECT r.ID, r.No_Reservacion, r.Total,
                       r.Fecha_Creacion, r.Fecha_Expiracion,
                       er.Estado,
                       u.Nombre AS UsuarioNombre, u.Apellido AS UsuarioApellido,
                       u.Username,
                       hot.Nombre AS HotelNombre,
                       MIN(dr.FechaCheckIn)  AS CheckIn,
                       MAX(dr.FechaCheckOut) AS CheckOut
                FROM Reservacion r
                JOIN EstadoReserva er ON r.EstadoID     = er.ID
                JOIN Usuario       u  ON r.Usuario_ID   = u.ID
                JOIN DetallesReservacion dr ON dr.ReservacionID = r.ID
                JOIN Habitacion    h   ON dr.HabitacionID = h.ID
                JOIN Hotel         hot ON h.HotelID       = hot.ID
                GROUP BY r.ID, r.No_Reservacion, r.Total,
                         r.Fecha_Creacion, r.Fecha_Expiracion,
                         er.Estado,
                         u.Nombre, u.Apellido, u.Username,
                         hot.Nombre
                ORDER BY r.Fecha_Creacion DESC
                """;
        return DatabaseManager.executeQuery(sql, rs -> {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id",           rs.getInt("ID"));
            row.put("noReservacion",rs.getString("No_Reservacion"));
            row.put("total",        rs.getDouble("Total"));
            row.put("estado",       rs.getString("Estado").toLowerCase());
            row.put("usuario",      rs.getString("Username"));
            row.put("nombreCompleto", rs.getString("UsuarioNombre") + " " + rs.getString("UsuarioApellido"));
            row.put("hotel",        rs.getString("HotelNombre"));
            row.put("checkIn",      rs.getDate("CheckIn") != null  ? rs.getDate("CheckIn").toString()  : null);
            row.put("checkOut",     rs.getDate("CheckOut") != null ? rs.getDate("CheckOut").toString() : null);
            row.put("fechaCreacion",rs.getTimestamp("Fecha_Creacion") != null ? rs.getTimestamp("Fecha_Creacion").toString() : null);
            return row;
        });
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — MÉTRICAS DEL DASHBOARD
    // ════════════════════════════════════════════════════

    public java.util.Map<String, Object> obtenerMetricas() {
        java.util.Map<String, Object> metricas = new java.util.LinkedHashMap<>();

        // Usuarios totales
        List<Integer> totalUsuarios = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Usuario", rs -> rs.getInt("total"));
        metricas.put("totalUsuarios", totalUsuarios.isEmpty() ? 0 : totalUsuarios.get(0));

        // Hoteles activos
        List<Integer> hotelesActivos = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel h JOIN Estado e ON h.EstadoID = e.ID WHERE LOWER(e.Estado) = 'activo'",
                rs -> rs.getInt("total"));
        metricas.put("hotelesActivos", hotelesActivos.isEmpty() ? 0 : hotelesActivos.get(0));

        // Reservas confirmadas activas
        List<Integer> reservasActivas = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getInt("total"));
        metricas.put("reservasActivas", reservasActivas.isEmpty() ? 0 : reservasActivas.get(0));

        // Reservas totales
        List<Integer> reservasTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Reservacion", rs -> rs.getInt("total"));
        metricas.put("reservasTotales", reservasTotales.isEmpty() ? 0 : reservasTotales.get(0));

        // Ingresos totales (solo reservas confirmadas)
        List<Double> ingresosTotales = DatabaseManager.executeQuery(
                "SELECT NVL(SUM(r.Total), 0) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE LOWER(TRIM(er.Estado)) = 'confirmada'",
                rs -> rs.getDouble("total"));
        metricas.put("ingresosTotales", ingresosTotales.isEmpty() ? 0.0 : ingresosTotales.get(0));

        // Conteos por estado de reserva
        List<java.util.Map<String, Object>> porEstado = DatabaseManager.executeQuery(
                "SELECT LOWER(TRIM(er.Estado)) AS estado, COUNT(*) AS total FROM Reservacion r JOIN EstadoReserva er ON r.EstadoID = er.ID GROUP BY LOWER(TRIM(er.Estado))",
                rs -> {
                    java.util.Map<String, Object> e = new java.util.LinkedHashMap<>();
                    e.put("estado", rs.getString("estado"));
                    e.put("total",  rs.getInt("total"));
                    return e;
                });
        metricas.put("reservasPorEstado", porEstado);

        // Hoteles totales
        List<Integer> hotTotales = DatabaseManager.executeQuery(
                "SELECT COUNT(*) AS total FROM Hotel", rs -> rs.getInt("total"));
        metricas.put("hotesTotales", hotTotales.isEmpty() ? 0 : hotTotales.get(0));

        return metricas;
    }
}