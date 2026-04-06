package org.example.services;

import org.example.dtos.*;
import org.example.helpers.CombinacionHelper;
import org.example.repositories.BusquedaAgenciaRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de busqueda de hoteles para agencias de viaje.
 * Aplica el descuento de la agencia a los precios y calcula
 * combinaciones de habitaciones para grupos grandes.
 */
public class BusquedaAgenciaService {

    private final BusquedaAgenciaRepository repository;

    /**
     * Crea una instancia de BusquedaAgenciaService con sus dependencias inyectadas.
     */
    public BusquedaAgenciaService(BusquedaAgenciaRepository repository) {
        this.repository = repository;
    }

    /**
     * Busca hoteles disponibles para una agencia autenticada por sesion de usuario.
     * Valida que el usuario tenga una agencia activa, obtiene su descuento,
     * guarda la busqueda y retorna los hoteles con precios ya descontados.
     * @param request   criterios de busqueda: ciudad, pais, fechas y cantidad de personas.
     * @param usuarioId ID del usuario dueno de la agencia.
     * @return lista de hoteles con tipos de habitacion, amenidades y combinaciones disponibles.
     * @throws IllegalArgumentException si el usuario no tiene agencia activa o la ciudad no existe.
     */
    public List<HotelResultadoDTO> buscar(BusquedaRequestDTO request, int usuarioId) {

        Double porcentajeDescuento = repository.obtenerDescuentoAgencia(usuarioId);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("El usuario no tiene una agencia activa asociada");
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
                request.getCantidadPersonas(), usuarioId);

        List<HotelResultadoDTO> hoteles = repository.buscarHotelesPorCiudad(ciudadId);

        for (HotelResultadoDTO hotel : hoteles) {
            hotel.setImagenesIds(repository.buscarImagenesHotel(hotel.getId()));

            List<AmenidadHotelDTO> amenidades = repository.buscarAmenidadesHotel(hotel.getId());
            for (AmenidadHotelDTO amenidad : amenidades) {
                amenidad.setImagenesIds(repository.buscarImagenesAmenidad(amenidad.getHotelAmenidadId()));
            }
            hotel.setAmenidades(amenidades);

            // Tipos que cumplen exactamente con la capacidad solicitada
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

            // Todos los tipos disponibles para calcular combinaciones de habitaciones
            List<TipoHabitacionResultadoDTO> todosLosTipos = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                aplicarDescuento(tipo, porcentajeDescuento);
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

            // Stock acumulado por capacidad para tipos que no alcanzan slos la cantidad pedida
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

    /**
     * Aplica el descuento de la agencia al precio por persona y por noche de un tipo de habitacion.
     * Redondea a dos decimales para evitar errores de punto flotante.
     * @param tipo       tipo de habitacion cuyos precios se van a modificar.
     * @param porcentaje porcentaje de descuento a aplicar (ej. 15.0 para 15%).
     */
    private void aplicarDescuento(TipoHabitacionResultadoDTO tipo, double porcentaje) {
        double factor = 1.0 - (porcentaje / 100.0);
        tipo.setPrecioPorPersona(Math.round(tipo.getPrecioPorPersona() * factor * 100.0) / 100.0);
        tipo.setPrecioPorNoche(Math.round(tipo.getPrecioPorNoche() * factor * 100.0) / 100.0);
    }

    /**
     * Busca hoteles disponibles para una agencia autenticada por token de API.
     * Funciona igual que buscar() pero valida la agencia con un token en lugar de sesion de usuario.
     * La busqueda se guarda sin vincular a ningun usuario.
     * @param request criterios de busqueda: ciudad, pais, fechas y cantidad de personas.
     * @param token   token de acceso de la agencia.
     * @return lista de hoteles con tipos de habitacion, amenidades y combinaciones disponibles.
     * @throws IllegalArgumentException si el token es invalido, la agencia no esta activa o la ciudad no existe.
     */
    public List<HotelResultadoDTO> buscarPorToken(BusquedaRequestDTO request, String token) {

        Double porcentajeDescuento = repository.obtenerDescuentoAgenciaPorToken(token);
        if (porcentajeDescuento == null) {
            throw new IllegalArgumentException("Token invalido o agencia no activa");
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

            // Tipos que cumplen exactamente con la capacidad solicitada
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

            // Todos los tipos para calcular combinaciones
            List<TipoHabitacionResultadoDTO> todosLosTipos = repository
                    .buscarTiposHabitacionDisponibles(
                            hotel.getId(), 1, fechaCheckIn, fechaCheckOut);

            for (TipoHabitacionResultadoDTO tipo : todosLosTipos) {
                aplicarDescuento(tipo, porcentajeDescuento);
                tipo.setHabitacionesDisponibles(repository.buscarHabitacionesResumenPorTipo(
                        hotel.getId(), tipo.getTipoHabitacionId(), fechaCheckIn, fechaCheckOut));
            }

            // Stock por capacidad para tipos que no alcanzan solos la cantidad pedida
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