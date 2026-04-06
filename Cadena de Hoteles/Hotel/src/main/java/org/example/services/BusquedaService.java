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

/**
 * Service de busqueda de hoteles para usuarios web.
 * Localiza hoteles por ciudad, guarda el registro de busqueda
 * y calcula combinaciones de habitaciones para grupos grandes.
 */
public class BusquedaService {

    private final BusquedaRepository busquedaRepository;

    /**
     * Crea una instancia de BusquedaService con sus dependencias inyectadas.
     */
    public BusquedaService(BusquedaRepository busquedaRepository) {
        this.busquedaRepository = busquedaRepository;
    }

    /**
     * Busca hoteles disponibles segun los criterios del request.
     * Valida que la ciudad exista, guarda la busqueda y enriquece
     * cada hotel con imagenes, amenidades, tipos de habitacion y combinaciones.
     * @param request   criterios: ciudad, pais, fechas y cantidad de personas.
     * @param usuarioId ID del usuario que realiza la busqueda, puede ser null si no hay sesion.
     * @return lista de hoteles con toda la informacion necesaria para mostrar resultados.
     * @throws IllegalArgumentException si la ciudad no existe en la base de datos.
     */
    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, Integer usuarioId) {

        Integer ciudadId = busquedaRepository.buscarCiudadId(request.getCiudad(), request.getPais());
        if (ciudadId == null) {
            throw new IllegalArgumentException(
                    "No se encontro la ciudad '" + request.getCiudad() +
                            "' en el pais '" + request.getPais() + "'"
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

            // Todos los tipos disponibles (capacidad >= 1) para calcular combinaciones
            List<TipoHabitacionResultadoDTO> todosLosTipos = busquedaRepository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                tipo.setHabitacionesDisponibles(busquedaRepository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

            // Stock por capacidad: cuantas habitaciones fisicas hay de cada tipo
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

            // Tipos agrupados por capacidad (solo los que no cumplen solos la cantidad pedida)
            Map<Integer, List<TipoHabitacionResultadoDTO>> tiposPorCapacidad = todosLosTipos.stream()
                    .filter(t -> t.getCapacidadMaxima() < request.getCantidadPersonas())
                    .collect(Collectors.groupingBy(TipoHabitacionResultadoDTO::getCapacidadMaxima));

            hotel.setTiposHabitacionPorCapacidad(tiposPorCapacidad);

            // Combinaciones posibles usando el stock real de habitaciones fisicas
            List<List<Integer>> combinaciones = CombinacionHelper.calcular(
                    request.getCantidadPersonas(), stockPorCapacidad);
            hotel.setCombinacionesNumericas(combinaciones);
        }

        return hoteles;
    }
}