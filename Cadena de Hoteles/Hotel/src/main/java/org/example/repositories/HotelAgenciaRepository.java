package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.HotelAgenciaDTO;

import java.util.List;

public class HotelAgenciaRepository {

    public List<HotelAgenciaDTO> listarHotelesParaAgencia() {
        String sql = """
                SELECT h.ID,
                       h.Nombre,
                       c.Nombre AS Ciudad,
                       p.Nombre AS Pais
                FROM   Hotel  h
                JOIN   Ciudad c ON h.CiudadID = c.ID
                JOIN   Pais   p ON c.Pais_ID  = p.ID
                JOIN   Estado e ON h.EstadoID = e.ID
                WHERE  LOWER(e.Estado) = 'activo'
                ORDER BY h.ID
                """;

        return DatabaseManager.executeQuery(sql, rs -> {
            HotelAgenciaDTO dto = new HotelAgenciaDTO();
            dto.setId(rs.getInt("ID"));
            dto.setNombre(rs.getString("Nombre"));
            dto.setCiudad(rs.getString("Ciudad"));
            dto.setPais(rs.getString("Pais"));
            return dto;
        });
    }
}