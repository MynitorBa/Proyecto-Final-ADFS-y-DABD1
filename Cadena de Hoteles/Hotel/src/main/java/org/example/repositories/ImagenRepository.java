package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

/**
 * Repository para la recuperacion y eliminacion de imagenes almacenadas en la base de datos.
 * Cubre imagenes de hoteles, habitaciones y amenidades.
 */
public class ImagenRepository {

    /**
     * Retorna los bytes de una imagen de hotel por su ID.
     * @param id ID de la imagen a recuperar.
     * @return arreglo de bytes con la imagen, o null si no existe.
     */
    public byte[] obtenerImagenHotel(int id) {
        String sql = "SELECT Imagen FROM ImagenHotel WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Retorna los bytes de una imagen de habitacion por su ID.
     * @param id ID de la imagen a recuperar.
     * @return arreglo de bytes con la imagen, o null si no existe.
     */
    public byte[] obtenerImagenHabitacion(int id) {
        String sql = "SELECT Imagen FROM ImagenHabitacion WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Retorna los bytes de una imagen de amenidad de hotel por su ID.
     * @param id ID de la imagen a recuperar.
     * @return arreglo de bytes con la imagen, o null si no existe.
     */
    public byte[] obtenerImagenAmenidad(int id) {
        String sql = "SELECT Imagen FROM ImagenHotelAmenidad WHERE ID = ?";
        List<byte[]> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getBytes("Imagen"), id
        );
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Elimina una imagen de hotel por su ID.
     * @param id ID de la imagen a eliminar.
     */
    public void eliminarImagenHotel(int id) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotel WHERE ID=?", id);
    }

    /**
     * Elimina una imagen de habitacion por su ID.
     * @param id ID de la imagen a eliminar.
     */
    public void eliminarImagenHabitacion(int id) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHabitacion WHERE ID=?", id);
    }

    /**
     * Elimina una imagen de amenidad de hotel por su ID.
     * @param id ID de la imagen a eliminar.
     */
    public void eliminarImagenAmenidad(int id) {
        DatabaseManager.executeUpdate("DELETE FROM ImagenHotelAmenidad WHERE ID=?", id);
    }
}