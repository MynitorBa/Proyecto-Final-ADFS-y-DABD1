package org.example.services;

import org.example.repositories.ImagenRepository;

public class ImagenService {

    private final ImagenRepository imagenRepository = new ImagenRepository();

    public byte[] obtenerImagenHotel(int id) {
        return imagenRepository.obtenerImagenHotel(id);
    }

    public byte[] obtenerImagenHabitacion(int id) {
        return imagenRepository.obtenerImagenHabitacion(id);
    }

    public byte[] obtenerImagenAmenidad(int id) {
        return imagenRepository.obtenerImagenAmenidad(id);
    }
}