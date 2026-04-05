package org.example.services;

import org.example.dtos.HotelAgenciaDTO;
import org.example.repositories.HotelAgenciaRepository;

import java.util.List;

/**
 * Service para exponer informacion de hoteles al modulo de agencias.
 */
public class HotelAgenciaService {

    private final HotelAgenciaRepository repository = new HotelAgenciaRepository();

    /**
     * Retorna la lista de hoteles disponibles para ser consultados por agencias.
     * @return lista de hoteles con los datos relevantes para el contexto de agencia.
     */
    public List<HotelAgenciaDTO> obtenerHotelesParaAgencia() {
        return repository.listarHotelesParaAgencia();
    }
}