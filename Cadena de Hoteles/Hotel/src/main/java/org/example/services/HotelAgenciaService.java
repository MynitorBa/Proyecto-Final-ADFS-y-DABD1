package org.example.services;

import org.example.dtos.HotelAgenciaDTO;
import org.example.repositories.HotelAgenciaRepository;

import java.util.List;

public class HotelAgenciaService {

    private final HotelAgenciaRepository repository = new HotelAgenciaRepository();

    public List<HotelAgenciaDTO> obtenerHotelesParaAgencia() {
        return repository.listarHotelesParaAgencia();
    }
}