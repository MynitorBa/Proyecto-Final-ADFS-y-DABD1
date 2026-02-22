package org.example.services;

import org.example.dtos.*;
import org.example.helpers.CombinacionHelper;
import org.example.repositories.BusquedaRepository;

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

            // Habitaciones que cumplen capacidad >= cantidadPersonas (comportamiento original)
            List<HabitacionDTO> habitaciones = busquedaRepository.buscarHabitacionesDisponibles(
                    hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut
            );
            for (HabitacionDTO hab : habitaciones) {
                hab.setImagenesIds(busquedaRepository.buscarImagenesHabitacion(hab.getId()));
            }
            hotel.setHabitaciones(habitaciones);

            // Todas las habitaciones disponibles con capacidad < cantidadPersonas (para combinaciones)
            List<HabitacionDTO> todasDisponibles = busquedaRepository.buscarHabitacionesDisponibles(
                    hotel.getId(), 1, fechaCheckIn, fechaCheckOut
            );
            for (HabitacionDTO hab : todasDisponibles) {
                hab.setImagenesIds(busquedaRepository.buscarImagenesHabitacion(hab.getId()));
            }

            // Agrupar por capacidad — excluir las que ya cumplen solas (>= cantidadPersonas)
            Map<Integer, List<HabitacionDTO>> porCapacidad = todasDisponibles.stream()
                    .filter(h -> h.getCapacidadMaxima() < request.getCantidadPersonas())
                    .collect(Collectors.groupingBy(HabitacionDTO::getCapacidadMaxima));

            hotel.setHabitacionesPorCapacidad(porCapacidad);

            // Stock por capacidad para validar combinaciones
            Map<Integer, Integer> stockPorCapacidad = new HashMap<>();
            porCapacidad.forEach((cap, habs) -> stockPorCapacidad.put(cap, habs.size()));

            // Calcular combinaciones numéricas validando existencias reales
            List<List<Integer>> combinaciones = CombinacionHelper.calcular(
                    request.getCantidadPersonas(), stockPorCapacidad
            );
            hotel.setCombinacionesNumericas(combinaciones);
        }

        return hoteles;
    }
}