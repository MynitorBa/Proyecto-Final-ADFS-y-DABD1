package org.example.services;

import org.example.dtos.HotelResultadoDTO;
import org.example.repositories.DestinosRepository;

import java.util.List;

public class DestinosService {

    private final DestinosRepository destinosRepository = new DestinosRepository();

    public List<HotelResultadoDTO> obtenerDestinos() {
        List<HotelResultadoDTO> hoteles = destinosRepository.obtenerTodosLosHoteles();
        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(destinosRepository.obtenerImagenesHotel(hotel.getId()));
        }
        return hoteles;
    }
}