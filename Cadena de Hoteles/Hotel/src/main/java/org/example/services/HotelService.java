package org.example.services;

import org.example.dtos.*;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class HotelService {

    private final HotelRepository  hotelRepository  = new HotelRepository();
    private final CiudadRepository ciudadRepository = new CiudadRepository();
    private final PaisRepository   paisRepository   = new PaisRepository();

    // ════════════════════════════════════════════════════
    //  CATÁLOGOS
    // ════════════════════════════════════════════════════

    public List<AmenidadDTO>  listarAmenidades() { return hotelRepository.listarAmenidades(); }
    public List<PaisDTO>      listarPaises()     { return paisRepository.listarPaises(); }
    public List<CiudadDTO>    listarCiudades()   { return paisRepository.listarCiudades(); }

    public Map<String, Object> crearAmenidad(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la amenidad es obligatorio");
        int id = hotelRepository.crearAmenidad(nombre.trim());
        return Map.of("id", id, "nombre", nombre.trim(), "mensaje", "Amenidad creada correctamente");
    }

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

    public Map<String, Object> crearHotel(CrearHotelRequestDTO req) {
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        if (req.getCiudad() == null || req.getCiudad().isBlank())
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        if (req.getPaisNombre() == null || req.getPaisNombre().isBlank())
            throw new IllegalArgumentException("El nombre del país es obligatorio");

        int paisId   = paisRepository.buscarOCrearPorNombre(req.getPaisNombre().trim());
        int ciudadId = ciudadRepository.buscarOCrearPorNombre(req.getCiudad().trim(), paisId);

        int hotelId = hotelRepository.crearHotel(
                req.getNombre().trim(), safe(req.getDireccion()),
                safe(req.getDescripcion()), req.getRating(),
                req.getEstadoId(), ciudadId);
        return Map.of("id", hotelId, "mensaje", "Hotel creado correctamente");
    }

    public void editarHotel(int hotelId, EditarHotelRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        hotelRepository.actualizarHotel(hotelId, req.getNombre().trim(),
                safe(req.getDireccion()), safe(req.getDescripcion()),
                req.getRating(), req.getEstadoId());
    }

    public void eliminarHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        hotelRepository.eliminarHotel(hotelId);
    }

    // ════════════════════════════════════════════════════
    //  AMENIDADES DEL HOTEL
    // ════════════════════════════════════════════════════

    public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HotelAmenidadDTO> lista = hotelRepository.listarAmenidadesHotel(hotelId);
        for (HotelAmenidadDTO a : lista)
            a.setImagenesIds(hotelRepository.obtenerImagenesAmenidadIds(a.getId()));
        return lista;
    }

    public Map<String, Object> agregarAmenidadHotel(int hotelId, AgregarAmenidadRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        if (req.getAmenidadId() <= 0)
            throw new IllegalArgumentException("Amenidad inválida");
        if (hotelRepository.tieneAmenidad(hotelId, req.getAmenidadId()))
            throw new IllegalArgumentException("Este hotel ya tiene esa amenidad asignada.");
        int id = hotelRepository.agregarAmenidadHotel(hotelId, req.getAmenidadId(), safe(req.getDescripcion()));
        return Map.of("id", id, "mensaje", "Amenidad agregada");
    }

    public void actualizarAmenidadHotel(int hotelAmenidadId, AgregarAmenidadRequestDTO req) {
        hotelRepository.actualizarAmenidadHotel(hotelAmenidadId, safe(req.getDescripcion()));
    }

    public void eliminarAmenidadHotel(int hotelAmenidadId) {
        hotelRepository.eliminarAmenidadHotel(hotelAmenidadId);
    }

    public Map<String, Object> agregarImagenAmenidad(int hotelAmenidadId, String base64) {
        int nuevoId = hotelRepository.agregarImagenAmenidad(hotelAmenidadId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen de amenidad agregada");
    }

    public void eliminarImagenAmenidad(int imagenId) {
        hotelRepository.eliminarImagenAmenidad(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HOTEL
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHotel(int hotelId, String base64) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        int nuevoId = hotelRepository.agregarImagenHotel(hotelId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    public void eliminarImagenHotel(int imagenId) {
        hotelRepository.eliminarImagenHotel(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES
    // ════════════════════════════════════════════════════

    public List<HabitacionAdminDTO> listarHabitaciones(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HabitacionAdminDTO> lista = hotelRepository.listarHabitacionesPorHotel(hotelId);
        for (HabitacionAdminDTO h : lista)
            h.setImagenesIds(hotelRepository.obtenerImagenesHabitacionIds(h.getId()));
        return lista;
    }

    public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO req) {
        if (!hotelRepository.existe(req.getHotelId()))
            throw new IllegalArgumentException("Hotel no encontrado: " + req.getHotelId());
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitación inválido");
        validarEstadoHabitacion(req.getEstadoId());

        // El número de habitación se auto-asigna en el repositorio
        int id = hotelRepository.crearHabitacion(
                req.getHotelId(),
                req.getTipoHabitacionId(),
                safe(req.getDescripcion()),
                req.getEstadoId()
        );
        return Map.of("id", id, "mensaje", "Habitación creada correctamente");
    }

    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO req) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada: " + habitacionId);
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitación inválido");
        validarEstadoHabitacion(req.getEstadoId());
        hotelRepository.actualizarHabitacion(habitacionId,
                req.getTipoHabitacionId(),
                safe(req.getNumeroHabitacion()),
                safe(req.getDescripcion()),
                req.getEstadoId());
    }

    public void eliminarHabitacion(int habitacionId) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada: " + habitacionId);
        hotelRepository.eliminarHabitacion(habitacionId);
    }

    // ════════════════════════════════════════════════════
    //  IMÁGENES DE HABITACIÓN
    // ════════════════════════════════════════════════════

    public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada: " + habitacionId);
        int nuevoId = hotelRepository.agregarImagenHabitacion(habitacionId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    public void eliminarImagenHabitacion(int imagenId) {
        hotelRepository.eliminarImagenHabitacion(imagenId);
    }

    // ════════════════════════════════════════════════════
    //  VALIDACIONES Y UTILIDADES
    // ════════════════════════════════════════════════════

    private void validarCamposHotel(String nombre, double rating, int estadoId) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del hotel no puede estar vacío");
        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("El rating debe estar entre 0 y 5");
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado inválido: use 1 (Activo) o 2 (Cerrado)");
    }

    private void validarEstadoHabitacion(int estadoId) {
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado inválido: use 1 (Activa) o 2 (Cerrada)");
    }

    private byte[] decodeBase64(String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacía");
        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        return Base64.getDecoder().decode(datos);
    }

    private String safe(String s) { return s != null ? s.trim() : ""; }

    // ════════════════════════════════════════════════════
    //  ADMIN — RESERVACIONES Y MÉTRICAS
    // ════════════════════════════════════════════════════

    public List<Map<String, Object>> listarTodasReservaciones() {
        return hotelRepository.listarTodasReservaciones();
    }

    public Map<String, Object> obtenerMetricas() {
        return hotelRepository.obtenerMetricas();
    }
}