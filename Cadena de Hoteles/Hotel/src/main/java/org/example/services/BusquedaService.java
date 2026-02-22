package org.example.services;

import org.example.dtos.*;
import org.example.repositories.BusquedaRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BusquedaService {

    private final BusquedaRepository busquedaRepository = new BusquedaRepository();

    // usuarioId puede ser null si la búsqueda es anónima
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

        // Guardar historial — funciona con o sin sesión
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

            List<HabitacionDTO> habitaciones = busquedaRepository.buscarHabitacionesDisponibles(
                    hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut
            );
            for (HabitacionDTO habitacion : habitaciones) {
                habitacion.setImagenesIds(busquedaRepository.buscarImagenesHabitacion(habitacion.getId()));
            }
            hotel.setHabitaciones(habitaciones);
        }

        return hoteles;
    }
}