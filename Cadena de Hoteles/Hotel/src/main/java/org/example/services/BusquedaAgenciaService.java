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

    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, int usuarioId) {

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

        repository.guardarBusqueda(ciudadId, fechaCheckIn, fechaCheckOut,
                request.getCantidadPersonas(), usuarioId);

        List<HotelResultadoDTO> hoteles = repository.buscarHotelesPorCiudad(ciudadId);

        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(repository.buscarImagenesHotel(hotel.getId()));

            List<AmenidadHotelDTO> amenidades = repository.buscarAmenidadesHotel(hotel.getId());
            for (AmenidadHotelDTO amenidad : amenidades) {
                amenidad.setImagenesIds(repository.buscarImagenesAmenidad(amenidad.getHotelAmenidadId()));
            }
            hotel.setAmenidades(amenidades);

            // Tipos que cumplen capacidad >= cantidadPersonas — con descuento aplicado
            List<TipoHabitacionResultadoDTO> tiposCumplen = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : tiposCumplen) {
                aplicarDescuento(tipo, porcentajeDescuento);
                tipo.setImagenesIds(repository.buscarImagenesHabitacion(tipo.getTipoHabitacionId()));
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }
            hotel.setTiposHabitacion(tiposCumplen);

            // Todos los tipos para combinaciones
            List<TipoHabitacionResultadoDTO> todosLosTipos = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

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

            Map<Integer, List<TipoHabitacionResultadoDTO>> tiposPorCapacidad = todosLosTipos.stream()
                    .filter(t -> t.getCapacidadMaxima() < request.getCantidadPersonas())
                    .collect(Collectors.groupingBy(TipoHabitacionResultadoDTO::getCapacidadMaxima));

            hotel.setTiposHabitacionPorCapacidad(tiposPorCapacidad);

            hotel.setCombinacionesNumericas(
                    CombinacionHelper.calcular(request.getCantidadPersonas(), stockPorCapacidad));
        }

        return hoteles;
    }

    private void aplicarDescuento(TipoHabitacionResultadoDTO tipo, double porcentaje) {
        double factor = 1.0 - (porcentaje / 100.0);
        tipo.setPrecioPorPersona(Math.round(tipo.getPrecioPorPersona() * factor * 100.0) / 100.0);
        tipo.setPrecioPorNoche(Math.round(tipo.getPrecioPorNoche() * factor * 100.0) / 100.0);
    }


    public List<HotelResultadoDTO> buscarPorToken(BusquedaRequestDTO request, String token) {

        Double porcentajeDescuento = repository.obtenerDescuentoAgenciaPorToken(token);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("Token inválido o agencia no activa");
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

        // Guardamos búsqueda sin usuarioId — pasamos NULL
        repository.guardarBusquedaSinUsuario(ciudadId, fechaCheckIn, fechaCheckOut,
                request.getCantidadPersonas());

        List<HotelResultadoDTO> hoteles = repository.buscarHotelesPorCiudad(ciudadId);

        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(repository.buscarImagenesHotel(hotel.getId()));

            List<AmenidadHotelDTO> amenidades = repository.buscarAmenidadesHotel(hotel.getId());
            for (AmenidadHotelDTO amenidad : amenidades) {
                amenidad.setImagenesIds(repository.buscarImagenesAmenidad(amenidad.getHotelAmenidadId()));
            }
            hotel.setAmenidades(amenidades);

            List<TipoHabitacionResultadoDTO> tiposCumplen = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), request.getCantidadPersonas(), fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : tiposCumplen) {
                aplicarDescuento(tipo, porcentajeDescuento);
                tipo.setImagenesIds(repository.buscarImagenesHabitacion(tipo.getTipoHabitacionId()));
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }
            hotel.setTiposHabitacion(tiposCumplen);

            List<TipoHabitacionResultadoDTO> todosLosTipos = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

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

            Map<Integer, List<TipoHabitacionResultadoDTO>> tiposPorCapacidad = todosLosTipos.stream()
                    .filter(t -> t.getCapacidadMaxima() < request.getCantidadPersonas())
                    .collect(Collectors.groupingBy(TipoHabitacionResultadoDTO::getCapacidadMaxima));

            hotel.setTiposHabitacionPorCapacidad(tiposPorCapacidad);
            hotel.setCombinacionesNumericas(
                    CombinacionHelper.calcular(request.getCantidadPersonas(), stockPorCapacidad));
        }

        return hoteles;
    }
}