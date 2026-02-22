package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.HabitacionAdminDTO;
import org.example.dtos.HotelAdminDTO;

import java.util.List;

public class HotelRepository {

    // ════════════════════════════════════════════════════
    //  HOTEL — listado y edición
    // ════════════════════════════════════════════════════

    public List<HotelAdminDTO> listarTodos() {
        String sql = """
                SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating,
                       h.EstadoID, e.Estado,
                       c.Nombre AS Ciudad,
                       p.Nombre AS Pais
                FROM   Hotel  h
                JOIN   Estado e ON h.EstadoID = e.ID
                JOIN   Ciudad c ON h.CiudadID = c.ID
                JOIN   Pais   p ON c.Pais_ID  = p.ID
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

    public void actualizarHotel(int hotelId, String nombre, String direccion,
                                String descripcion, double rating, int estadoId) {
        String sql = """
                UPDATE Hotel
                SET    Nombre      = ?,
                       Direccion   = ?,
                       Descripcion = ?,
                       Rating      = ?,
                       EstadoID    = ?
                WHERE  ID = ?
                """;
        DatabaseManager.executeUpdate(sql, nombre, direccion, descripcion, rating, estadoId, hotelId);
    }

    public boolean existe(int hotelId) {
        String sql = "SELECT COUNT(*) FROM Hotel WHERE ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), hotelId);
        return !result.isEmpty() && result.get(0) > 0;
    }

    public int contarHabitaciones(int hotelId) {
        String sql = "SELECT COUNT(*) FROM Habitacion WHERE HotelID = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), hotelId);
        return result.isEmpty() ? 0 : result.get(0);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HOTEL
    // ════════════════════════════════════════════════════

    public List<Integer> obtenerImagenesIds(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ? ORDER BY ID";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }

    public int agregarImagenHotel(int hotelId, byte[] imagen) {
        String sql = "INSERT INTO ImagenHotel (HotelID, Imagen) VALUES (?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID", hotelId, imagen);
    }

    public void eliminarImagenHotel(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotel WHERE ID = ?", imagenId);
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — listado y edición
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
            HabitacionAdminDTO dto = new HabitacionAdminDTO();
            dto.setId(rs.getInt("ID"));
            dto.setHotelId(rs.getInt("HotelID"));
            dto.setTipoHabitacionId(rs.getInt("TipoHabitacionID"));
            dto.setTipoHabitacion(rs.getString("TipoHabitacion"));
            dto.setCamaId(rs.getInt("CamaID"));
            dto.setTipoCama(rs.getString("TipoCama"));
            dto.setPrecioPorPersona(rs.getDouble("Precio_por_Persona"));
            dto.setPrecioPorNoche(rs.getDouble("Precio_por_Noche"));
            dto.setCapacidadMaxima(rs.getInt("Capacidad_Maxima"));
            dto.setMetrosCuadrados(rs.getDouble("Metros_Cuadrados"));
            dto.setDescripcion(rs.getString("Descripcion"));
            dto.setEstadoId(rs.getInt("Estado_ID"));
            dto.setEstado(rs.getString("Estado"));
            return dto;
        }, hotelId);
    }

    public boolean existeHabitacion(int habitacionId) {
        String sql = "SELECT COUNT(*) FROM Habitacion WHERE ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(sql, rs -> rs.getInt(1), habitacionId);
        return !result.isEmpty() && result.get(0) > 0;
    }

    public void actualizarHabitacion(int habitacionId, int tipoHabitacionId, int camaId,
                                     double precioPorPersona, double precioPorNoche,
                                     int capacidadMaxima, double metrosCuadrados,
                                     String descripcion, int estadoId) {
        String sql = """
                UPDATE Habitacion
                SET    TipoHabitacionID   = ?,
                       CamaID             = ?,
                       Precio_por_Persona = ?,
                       Precio_por_Noche   = ?,
                       Capacidad_Maxima   = ?,
                       Metros_Cuadrados   = ?,
                       Descripcion        = ?,
                       Estado_ID          = ?
                WHERE  ID = ?
                """;
        DatabaseManager.executeUpdate(sql,
                tipoHabitacionId, camaId, precioPorPersona, precioPorNoche,
                capacidadMaxima, metrosCuadrados, descripcion, estadoId,
                habitacionId);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HABITACIÓN
    // ════════════════════════════════════════════════════

    public List<Integer> obtenerImagenesHabitacionIds(int habitacionId) {
        String sql = "SELECT ID FROM ImagenHabitacion WHERE HabitacionID = ? ORDER BY ID";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), habitacionId);
    }

    public int agregarImagenHabitacion(int habitacionId, byte[] imagen) {
        String sql = "INSERT INTO ImagenHabitacion (HabitacionID, Imagen) VALUES (?, ?)";
        return DatabaseManager.executeInsertReturnId(sql, "ID", habitacionId, imagen);
    }

    public void eliminarImagenHabitacion(int imagenId) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHabitacion WHERE ID = ?", imagenId);
    }
}