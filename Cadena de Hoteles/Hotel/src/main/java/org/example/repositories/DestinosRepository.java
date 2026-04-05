package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.HotelResultadoDTO;

import java.util.List;

/**
 * Repository para la consulta de destinos turisticos disponibles.
 * Provee acceso a los hoteles activos y sus imagenes para la vista de destinos.
 */
public class DestinosRepository {

    /**
     * Retorna todos los hoteles activos del sistema ordenados por pais, ciudad y nombre.
     * @return lista de HotelResultadoDTO con los datos de cada hotel activo.
     */
    public List<HotelResultadoDTO> obtenerTodosLosHoteles() {
        String sql = "SELECT h.ID, h.Nombre, h.Direccion, h.Descripcion, h.Rating, " +
                "e.Estado, c.Nombre AS Ciudad, p.Nombre AS Pais " +
                "FROM Hotel h " +
                "JOIN Estado  e ON h.EstadoID = e.ID " +
                "JOIN Ciudad  c ON h.CiudadID = c.ID " +
                "JOIN Pais    p ON c.Pais_ID  = p.ID " +
                "WHERE LOWER(TRIM(e.Estado)) = 'activo' " +
                "ORDER BY p.Nombre, c.Nombre, h.Nombre";

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
        });
    }

    /**
     * Retorna los IDs de las imagenes asociadas a un hotel especifico.
     * @param hotelId ID del hotel del que se quieren obtener las imagenes.
     * @return lista de IDs de imagenes del hotel.
     */
    public List<Integer> obtenerImagenesHotel(int hotelId) {
        String sql = "SELECT ID FROM ImagenHotel WHERE HotelID = ?";
        return DatabaseManager.executeQuery(sql, rs -> rs.getInt("ID"), hotelId);
    }
}