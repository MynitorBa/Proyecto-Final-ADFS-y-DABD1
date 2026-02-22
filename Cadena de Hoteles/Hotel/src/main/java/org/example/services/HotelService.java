package org.example.services;

import org.example.dtos.*;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HotelService {

    private final HotelRepository hotelRepository   = new HotelRepository();
    private final CiudadRepository ciudadRepository = new CiudadRepository();
    private final PaisRepository   paisRepository   = new PaisRepository();

    // ════════════════════════════════════════════════════
    //  CATÁLOGOS
    // ════════════════════════════════════════════════════

    public List<PaisDTO>   listarPaises()   { return paisRepository.listarPaises(); }
    public List<CiudadDTO> listarCiudades() { return paisRepository.listarCiudades(); }

    // ════════════════════════════════════════════════════
    //  HOTEL — listar
    // ════════════════════════════════════════════════════

    public List<HotelAdminDTO> listarTodos() {
        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();
        for (HotelAdminDTO hotel : hoteles) {
            hotel.setCantidadHabitaciones(hotelRepository.contarHabitaciones(hotel.getId()));
            hotel.setImagenesIds(hotelRepository.obtenerImagenesIds(hotel.getId()));
        }
        return hoteles;
    }

    // ════════════════════════════════════════════════════
    //  HOTEL — crear
    // ════════════════════════════════════════════════════

    public Map<String, Object> crearHotel(CrearHotelRequestDTO request) {
        validarHotel(request.getNombre(), request.getRating(), request.getEstadoId());

        if (request.getCiudad() == null || request.getCiudad().isBlank())
            throw new IllegalArgumentException("El nombre de la ciudad no puede estar vacío");
        if (request.getPaisId() <= 0)
            throw new IllegalArgumentException("Debe seleccionar un país válido");

        int ciudadId = ciudadRepository.buscarOCrearPorNombre(
                request.getCiudad().trim(), request.getPaisId());

        int hotelId = hotelRepository.crearHotel(
                request.getNombre().trim(),
                request.getDireccion()   != null ? request.getDireccion().trim()   : "",
                request.getDescripcion() != null ? request.getDescripcion().trim() : "",
                request.getRating(),
                request.getEstadoId(),
                ciudadId
        );

        return Map.of("id", hotelId, "mensaje", "Hotel creado correctamente");
    }

    // ════════════════════════════════════════════════════
    //  HOTEL — editar
    // ════════════════════════════════════════════════════

    public void editarHotel(int hotelId, EditarHotelRequestDTO request) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + hotelId);
        validarHotel(request.getNombre(), request.getRating(), request.getEstadoId());

        hotelRepository.actualizarHotel(
                hotelId,
                request.getNombre().trim(),
                request.getDireccion()   != null ? request.getDireccion().trim()   : "",
                request.getDescripcion() != null ? request.getDescripcion().trim() : "",
                request.getRating(),
                request.getEstadoId()
        );
    }

    private void validarHotel(String nombre, double rating, int estadoId) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del hotel no puede estar vacío");
        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("El rating debe estar entre 0 y 5");
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado inválido. Use 1 (Activo) o 2 (Cerrado)");
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HOTEL
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHotel(int hotelId, String base64) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + hotelId);
        byte[] bytes = decodeBase64(base64);
        int nuevoId = hotelRepository.agregarImagenHotel(hotelId, bytes);
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada correctamente");
    }

    public void eliminarImagenHotel(int imagenId) {
        hotelRepository.eliminarImagenHotel(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — listar
    // ════════════════════════════════════════════════════

    public List<HabitacionAdminDTO> listarHabitaciones(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + hotelId);
        List<HabitacionAdminDTO> habitaciones = hotelRepository.listarHabitacionesPorHotel(hotelId);
        for (HabitacionAdminDTO h : habitaciones) {
            h.setImagenesIds(hotelRepository.obtenerImagenesHabitacionIds(h.getId()));
        }
        return habitaciones;
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — crear
    // ════════════════════════════════════════════════════

    public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO request) {
        if (!hotelRepository.existe(request.getHotelId()))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + request.getHotelId());
        validarHabitacion(request.getPrecioPorNoche(), request.getPrecioPorPersona(),
                request.getCapacidadMaxima(), request.getEstadoId());

        int habitacionId = hotelRepository.crearHabitacion(
                request.getHotelId(),
                request.getTipoHabitacionId(),
                request.getCamaId(),
                request.getPrecioPorPersona(),
                request.getPrecioPorNoche(),
                request.getCapacidadMaxima(),
                request.getMetrosCuadrados(),
                request.getDescripcion() != null ? request.getDescripcion().trim() : "",
                request.getEstadoId()
        );
        return Map.of("id", habitacionId, "mensaje", "Habitación creada correctamente");
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — editar
    // ════════════════════════════════════════════════════

    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO request) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada con ID: " + habitacionId);
        validarHabitacion(request.getPrecioPorNoche(), request.getPrecioPorPersona(),
                request.getCapacidadMaxima(), request.getEstadoId());

        hotelRepository.actualizarHabitacion(
                habitacionId,
                request.getTipoHabitacionId(),
                request.getCamaId(),
                request.getPrecioPorPersona(),
                request.getPrecioPorNoche(),
                request.getCapacidadMaxima(),
                request.getMetrosCuadrados(),
                request.getDescripcion() != null ? request.getDescripcion().trim() : "",
                request.getEstadoId()
        );
    }

    private void validarHabitacion(double precioPorNoche, double precioPorPersona,
                                   int capacidadMaxima, int estadoId) {
        if (precioPorNoche < 0 || precioPorPersona < 0)
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        if (capacidadMaxima < 1)
            throw new IllegalArgumentException("La capacidad mínima es 1 persona");
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado inválido. Use 1 (Activa) o 2 (Cerrada)");
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HABITACIÓN
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada con ID: " + habitacionId);
        byte[] bytes = decodeBase64(base64);
        int nuevoId = hotelRepository.agregarImagenHabitacion(habitacionId, bytes);
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada correctamente");
    }

    public void eliminarImagenHabitacion(int imagenId) {
        hotelRepository.eliminarImagenHabitacion(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  UTIL
    // ════════════════════════════════════════════════════

    private byte[] decodeBase64(String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacía");
        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        return Base64.getDecoder().decode(datos);
    }
}