package org.example.services;

import org.example.dtos.EditarHabitacionRequestDTO;
import org.example.dtos.EditarHotelRequestDTO;
import org.example.dtos.HabitacionAdminDTO;
import org.example.dtos.HotelAdminDTO;
import org.example.repositories.HotelRepository;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HotelService {

    private final HotelRepository hotelRepository = new HotelRepository();

    // ════════════════════════════════════════════════════
    //  HOTEL
    // ════════════════════════════════════════════════════

    public List<HotelAdminDTO> listarTodos() {
        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();
        for (HotelAdminDTO hotel : hoteles) {
            hotel.setCantidadHabitaciones(hotelRepository.contarHabitaciones(hotel.getId()));
            hotel.setImagenesIds(hotelRepository.obtenerImagenesIds(hotel.getId()));
        }
        return hoteles;
    }

    public void editarHotel(int hotelId, EditarHotelRequestDTO request) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + hotelId);
        if (request.getNombre() == null || request.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre del hotel no puede estar vacío");
        if (request.getRating() < 0 || request.getRating() > 5)
            throw new IllegalArgumentException("El rating debe estar entre 0 y 5");
        if (request.getEstadoId() != 1 && request.getEstadoId() != 2)
            throw new IllegalArgumentException("Estado inválido. Use 1 (Activo) o 2 (Cerrado)");

        hotelRepository.actualizarHotel(
                hotelId,
                request.getNombre().trim(),
                request.getDireccion()   != null ? request.getDireccion().trim()   : "",
                request.getDescripcion() != null ? request.getDescripcion().trim() : "",
                request.getRating(),
                request.getEstadoId()
        );
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HOTEL
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHotel(int hotelId, String base64) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado con ID: " + hotelId);
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacía");

        // Quitar encabezado data:image/...;base64, si viene incluido
        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        byte[] bytes = Base64.getDecoder().decode(datos);

        int nuevoId = hotelRepository.agregarImagenHotel(hotelId, bytes);
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada correctamente");
    }

    public void eliminarImagenHotel(int imagenId) {
        hotelRepository.eliminarImagenHotel(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES
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

    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO request) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada con ID: " + habitacionId);
        if (request.getPrecioPorNoche() < 0 || request.getPrecioPorPersona() < 0)
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        if (request.getCapacidadMaxima() < 1)
            throw new IllegalArgumentException("La capacidad mínima es 1 persona");
        if (request.getEstadoId() != 1 && request.getEstadoId() != 2)
            throw new IllegalArgumentException("Estado inválido. Use 1 (Activa) o 2 (Cerrada)");

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

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HABITACIÓN
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada con ID: " + habitacionId);
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacía");

        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        byte[] bytes = Base64.getDecoder().decode(datos);

        int nuevoId = hotelRepository.agregarImagenHabitacion(habitacionId, bytes);
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada correctamente");
    }

    public void eliminarImagenHabitacion(int imagenId) {
        hotelRepository.eliminarImagenHabitacion(imagenId);
    }
}