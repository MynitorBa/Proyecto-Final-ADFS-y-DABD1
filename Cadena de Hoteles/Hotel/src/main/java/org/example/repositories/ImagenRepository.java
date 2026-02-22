package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class ImagenRepository {

    public byte[] obtenerImagenHotel(int id) {
        String sql = "SELECT Imagen FROM ImagenHotel WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }

    public byte[] obtenerImagenHabitacion(int id) {
        String sql = "SELECT Imagen FROM ImagenHabitacion WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }

    public byte[] obtenerImagenAmenidad(int id) {
        String sql = "SELECT Imagen FROM ImagenHotelAmenidad WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }
}