package org.example.services;

import org.example.dtos.*;
import org.example.helpers.CombinacionHelper;
import org.example.repositories.BusquedaAgenciaRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BusquedaAgenciaService {

    private final BusquedaAgenciaRepository repository = new BusquedaAgenciaRepository();
/*
    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, int usuarioId) {

        // Verificar agencia activa y obtener descuento
        Double porcentajeDescuento = repository.obtenerDescuentoAgencia(usuarioId);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("El usuario no tiene una agencia activa asociada");
        }

        Integer ciudadId = repository.buscarCiudadId(request.getCiudad(), request.getPais());
        if (ciudadId == null) {
            throw new IllegalArgumentException(
                    "No se encontró la ciudad '" + request.getCiudad() +
                            "' en el país '" + request.getPais() + "'"
            );
        }

        Date fechaCheckIn  = Date.valueOf(LocalDate.parse(request.getFechaCheckIn()));
        Date fechaCheckOut = Date.valueOf(LocalDate.parse(request.getFechaCheckOut()));

        //Guardar búsqueda tipo 2 (Agencia)
        repository.guardarBusqueda(ciudadId, fechaCheckIn, fechaCheckOut,
                request.getCantidadPersonas(), usuarioId);

        // Construir resultados
        List<HotelResultadoDTO> hoteles = repository.buscarHotelesPorCiudad(ciudadId);

        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(repository.buscarImagenesHotel(hotel.getId()));

            List<AmenidadHotelDTO> amenidades = repository.buscarAmenidadesHotel(hotel.getId());
            for (AmenidadHotelDTO amenidad : amenidades) {
                amenidad.setImagenesIds(repository.buscarImagenesAmenidad(amenidad.getHotelAmenidadId()));
            }
            hotel.setAmenidades(amenidades);

            // Habitaciones que cumplen capacidad >= cantidadPersonas
            List<HabitacionDTO> habitaciones = repository.buscarHabitacionesDisponibles(
                    hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut
            );
            List<HabitacionDTO> habitacionesConDescuento = new ArrayList<>();
            for (HabitacionDTO hab : habitaciones) {
                hab.setImagenesIds(repository.buscarImagenesHabitacion(hab.getId()));
                habitacionesConDescuento.add(aplicarDescuento(hab, porcentajeDescuento));
            }
            hotel.setHabitaciones(habitacionesConDescuento);

            // Todas disponibles para combinaciones
            List<HabitacionDTO> todasDisponibles = repository.buscarHabitacionesDisponibles(
                    hotel.getId(), 1, fechaCheckIn, fechaCheckOut
            );

            // Agrupar por capacidad — excluir las que ya cumplen solas
            Map<Integer, List<HabitacionDTO>> porCapacidad = new HashMap<>();
            for (HabitacionDTO hab : todasDisponibles) {
                if (hab.getCapacidadMaxima() < request.getCantidadPersonas()) {
                    hab.setImagenesIds(repository.buscarImagenesHabitacion(hab.getId()));
                    HabitacionAgenciaDTO habConDesc = aplicarDescuento(hab, porcentajeDescuento);
                    porCapacidad.computeIfAbsent(hab.getCapacidadMaxima(), k -> new ArrayList<>()).add(habConDesc);
                }
            }
            hotel.setHabitacionesPorCapacidad(porCapacidad);

            // Combinaciones numéricas validando stock
            Map<Integer, Integer> stockPorCapacidad = new HashMap<>();
            porCapacidad.forEach((cap, habs) -> stockPorCapacidad.put(cap, habs.size()));
            hotel.setCombinacionesNumericas(
                    CombinacionHelper.calcular(request.getCantidadPersonas(), stockPorCapacidad)
            );
        }

        return hoteles;
    }

    // ----------------- Crea un HabitacionAgenciaDTO con los precios con descuento ---------------

    private HabitacionAgenciaDTO aplicarDescuento(HabitacionDTO hab, double porcentaje) {
        HabitacionAgenciaDTO dto = new HabitacionAgenciaDTO();
        dto.setId(hab.getId());
        dto.setTipoHabitacion(hab.getTipoHabitacion());
        dto.setPrecioPorPersona(hab.getPrecioPorPersona());
        dto.setPrecioPorNoche(hab.getPrecioPorNoche());
        dto.setCapacidadMaxima(hab.getCapacidadMaxima());
        dto.setTipoCama(hab.getTipoCama());
        dto.setMetrosCuadrados(hab.getMetrosCuadrados());
        dto.setDescripcion(hab.getDescripcion());
        dto.setEstado(hab.getEstado());
        dto.setImagenesIds(hab.getImagenesIds());

        double factor = 1.0 - (porcentaje / 100.0);
        dto.setPorcentajeDescuento(porcentaje);
        dto.setPrecioPorNocheConDescuento(Math.round(hab.getPrecioPorNoche() * factor * 100.0) / 100.0);
        dto.setPrecioPorPersonaConDescuento(Math.round(hab.getPrecioPorPersona() * factor * 100.0) / 100.0);

        return dto;
    }*/
}