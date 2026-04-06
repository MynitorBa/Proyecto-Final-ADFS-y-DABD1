package org.example.services;

import org.example.repositories.ImagenRepository;

/**
 * Service para recuperar imagenes almacenadas en la base de datos.
 * Sirve los bytes de imagenes de hoteles, habitaciones y amenidades.
 */
public class ImagenService {

    private final ImagenRepository imagenRepository;

    /**
     * Crea una instancia de ImagenService con sus dependencias inyectadas.
     */
    public ImagenService(ImagenRepository imagenRepository) {
        this.imagenRepository = imagenRepository;
    }

    /**
     * Retorna los bytes de una imagen de hotel.
     * @param id ID de la imagen.
     * @return array de bytes de la imagen.
     */
    public byte[] obtenerImagenHotel(int id) {
        return imagenRepository.obtenerImagenHotel(id);
    }

    /**
     * Retorna los bytes de una imagen de habitacion.
     * @param id ID de la imagen.
     * @return array de bytes de la imagen.
     */
    public byte[] obtenerImagenHabitacion(int id) {
        return imagenRepository.obtenerImagenHabitacion(id);
    }

    /**
     * Retorna los bytes de una imagen de amenidad.
     * @param id ID de la imagen.
     * @return array de bytes de la imagen.
     */
    public byte[] obtenerImagenAmenidad(int id) {
        return imagenRepository.obtenerImagenAmenidad(id);
    }
}