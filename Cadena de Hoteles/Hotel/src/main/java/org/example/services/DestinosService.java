package org.example.services;

import org.example.dtos.HotelResultadoDTO;
import org.example.repositories.DestinosRepository;

import java.util.List;

/**
 * Service para obtener el listado de destinos disponibles.
 * Retorna todos los hoteles con sus imagenes para la pagina de destinos.
 */
public class DestinosService {

    private final DestinosRepository destinosRepository = new DestinosRepository();

    /**
     * Obtiene todos los hoteles disponibles como destinos y les asigna sus imagenes.
     * @return lista de hoteles con sus IDs de imagenes cargados.
     */
    public List<HotelResultadoDTO> obtenerDestinos() {
        List<HotelResultadoDTO> hoteles = destinosRepository.obtenerTodosLosHoteles();
        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(destinosRepository.obtenerImagenesHotel(hotel.getId()));
        }
        return hoteles;
    }
}