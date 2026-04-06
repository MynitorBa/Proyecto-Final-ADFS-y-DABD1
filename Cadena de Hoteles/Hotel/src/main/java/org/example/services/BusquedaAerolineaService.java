package org.example.services;

import org.example.dtos.*;
import org.example.helpers.CombinacionHelper;
import org.example.repositories.AerolineaAliadaRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de busqueda de hoteles para aerolineas aliadas.
 * Aplica el descuento de la aerolinea a los precios y calcula
 * combinaciones de habitaciones para grupos grandes.
 */
public class BusquedaAerolineaService {

    private final AerolineaAliadaRepository repository;

    public BusquedaAerolineaService(AerolineaAliadaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca hoteles disponibles para una aerolinea autenticada por token.
     * Valida que el token corresponda a una aerolinea activa, obtiene su descuento,
     * guarda la busqueda y retorna los hoteles con precios ya descontados.
     *
     * @param request criterios de busqueda: ciudad, pais, fechas y cantidad de personas.
     * @param token   token de acceso de la aerolinea aliada.
     * @return lista de hoteles con tipos de habitacion, amenidades y combinaciones disponibles.
     * @throws IllegalArgumentException si el token es invalido o la ciudad no existe.
     */
    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, String token) {

        Double porcentajeDescuento = repository.obtenerDescuentoAerolinea(token);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("Token invalido o aerolinea no activa");
        }

        Integer ciudadId = repository.buscarCiudadId(request.getCiudad(), request.getPais());
        if (ciudadId == null) {
            throw new IllegalArgumentException(
                    "No se encontro la ciudad '" + request.getCiudad() +
                            "' en el pais '" + request.getPais() + "'"
            );
        }

        Date fechaCheckIn  = Date.valueOf(LocalDate.parse(request.getFechaCheckIn()));
        Date fechaCheckOut = Date.valueOf(LocalDate.parse(request.getFechaCheckOut()));

        repository.guardarBusqueda(ciudadId, fechaCheckIn, fechaCheckOut,
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
                aplicarDescuento(tipo, porcentajeDescuento);
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
}