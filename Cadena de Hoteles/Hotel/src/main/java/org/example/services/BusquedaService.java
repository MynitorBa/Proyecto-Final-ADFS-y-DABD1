package org.example.services;

import org.example.dtos.*;
import org.example.helpers.CombinacionHelper;
import org.example.repositories.BusquedaRepository;
import org.example.dtos.TipoHabitacionResultadoDTO;
import org.example.dtos.HabitacionResumenDTO;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BusquedaService {

    private final BusquedaRepository busquedaRepository = new BusquedaRepository();

    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, Integer usuarioId) {

        Integer ciudadId = busquedaRepository.buscarCiudadId(request.getCiudad(), request.getPais());
        if (ciudadId == null) {
            throw new IllegalArgumentException(
                    "No se encontró la ciudad '" + request.getCiudad() +
                            "' en el país '" + request.getPais() + "'"
            );
        }

        Date fechaCheckIn  = Date.valueOf(LocalDate.parse(request.getFechaCheckIn()));
        Date fechaCheckOut = Date.valueOf(LocalDate.parse(request.getFechaCheckOut()));

        busquedaRepository.guardarBusqueda(
                ciudadId, fechaCheckIn, fechaCheckOut,
                request.getCantidadPersonas(), usuarioId
        );

        List<HotelResultadoDTO> hoteles = busquedaRepository.buscarHotelesPorCiudad(ciudadId);

        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(busquedaRepository.buscarImagenesHotel(hotel.getId()));

            List<AmenidadHotelDTO> amenidades = busquedaRepository.buscarAmenidadesHotel(hotel.getId());
            for (AmenidadHotelDTO amenidad : amenidades) {
                amenidad.setImagenesIds(busquedaRepository.buscarImagenesAmenidad(amenidad.getHotelAmenidadId()));
            }
            hotel.setAmenidades(amenidades);

            // Tipos que cumplen capacidad >= cantidadPersonas
            List<TipoHabitacionResultadoDTO> tiposCumplen = busquedaRepository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : tiposCumplen) {
                tipo.setImagenesIds(busquedaRepository.buscarImagenesHabitacion(tipo.getTipoHabitacionId()));
                tipo.setHabitacionesDisponibles(busquedaRepository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }
            hotel.setTiposHabitacion(tiposCumplen);

            // Todos los tipos disponibles (capacidad >= 1) para combinaciones
            List<TipoHabitacionResultadoDTO> todosLosTipos = busquedaRepository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                tipo.setHabitacionesDisponibles(busquedaRepository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

            // Stock por capacidad: cuántas habitaciones físicas hay de cada tipo
            Map<Integer, Integer> stockPorCapacidad = new HashMap<>();
            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                if (tipo.getCapacidadMaxima() < request.getCantidadPersonas()) {
                    stockPorCapacidad.merge(
                            tipo.getCapacidadMaxima(),
                            tipo.getHabitacionesDisponibles().size(),
                            Integer::sum
                    );
                }
            }

            // Tipos agrupados por capacidad (solo los que no cumplen solos)
            Map<Integer, List<TipoHabitacionResultadoDTO>> tiposPorCapacidad = todosLosTipos.stream()
                    .filter(t -> t.getCapacidadMaxima() < request.getCantidadPersonas())
                    .collect(Collectors.groupingBy(TipoHabitacionResultadoDTO::getCapacidadMaxima));

            hotel.setTiposHabitacionPorCapacidad(tiposPorCapacidad);

            // Combinaciones (misma lógica, ahora con stock real de habitaciones físicas)
            List<List<Integer>> combinaciones = CombinacionHelper.calcular(
                    request.getCantidadPersonas(), stockPorCapacidad);
            hotel.setCombinacionesNumericas(combinaciones);
        }

        return hoteles;
    }
}