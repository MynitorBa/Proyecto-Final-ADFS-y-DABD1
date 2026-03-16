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

    public Map<String, Object> crearHotel(CrearHotelRequestDTO req) {
        System.out.println("=== [crearHotel] INICIO ===");
        System.out.println("  nombre      = " + req.getNombre());
        System.out.println("  direccion   = " + req.getDireccion());
        System.out.println("  descripcion = " + req.getDescripcion());
        System.out.println("  rating      = " + req.getRating());
        System.out.println("  estadoId    = " + req.getEstadoId());
        System.out.println("  ciudad      = " + req.getCiudad());
        System.out.println("  paisNombre  = " + req.getPaisNombre());

        try {
            validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
            System.out.println("  [OK] validarCamposHotel paso");
        } catch (Exception e) {
            System.out.println("  [ERROR] validarCamposHotel: " + e.getMessage());
            throw e;
        }

        if (req.getCiudad() == null || req.getCiudad().isBlank())
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        if (req.getPaisNombre() == null || req.getPaisNombre().isBlank())
            throw new IllegalArgumentException("El nombre del país es obligatorio");

        int paisId;
        try {
            System.out.println("  Buscando/creando pais: '" + req.getPaisNombre().trim() + "'");
            paisId = paisRepository.buscarOCrearPorNombre(req.getPaisNombre().trim());
            System.out.println("  [OK] paisId = " + paisId);
        } catch (Exception e) {
            System.out.println("  [ERROR] buscarOCrearPorNombre (pais): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        int ciudadId;
        try {
            System.out.println("  Buscando/creando ciudad: '" + req.getCiudad().trim() + "' en paisId=" + paisId);
            ciudadId = ciudadRepository.buscarOCrearPorNombre(req.getCiudad().trim(), paisId);
            System.out.println("  [OK] ciudadId = " + ciudadId);
        } catch (Exception e) {
            System.out.println("  [ERROR] buscarOCrearPorNombre (ciudad): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        int hotelId;
        try {
            System.out.println("  Insertando hotel en BD...");
            System.out.println("    nombre      = '" + req.getNombre().trim() + "'");
            System.out.println("    direccion   = '" + safe(req.getDireccion()) + "'");
            System.out.println("    descripcion = '" + safe(req.getDescripcion()) + "'");
            System.out.println("    rating      = " + req.getRating());
            System.out.println("    estadoId    = " + req.getEstadoId());
            System.out.println("    ciudadId    = " + ciudadId);

            hotelId = hotelRepository.crearHotel(
                    req.getNombre().trim(),
                    safe(req.getDireccion()),
                    safe(req.getDescripcion()),
                    req.getRating(),
                    req.getEstadoId(),
                    ciudadId
            );
            System.out.println("  [OK] hotelId = " + hotelId);
        } catch (Exception e) {
            System.out.println("  [ERROR] hotelRepository.crearHotel: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== [crearHotel] FIN OK — id=" + hotelId + " ===");
        return Map.of("id", hotelId, "mensaje", "Hotel creado correctamente");
    }

    // ════════════════════════════════════════════════════
    //  HOTEL — editar
    // ════════════════════════════════════════════════════

    public void editarHotel(int hotelId, EditarHotelRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());

        hotelRepository.actualizarHotel(
                hotelId,
                req.getNombre().trim(),
                safe(req.getDireccion()),
                safe(req.getDescripcion()),
                req.getRating(),
                req.getEstadoId()
        );
    }

    // ════════════════════════════════════════════════════
    //  HOTEL — eliminar
    // ════════════════════════════════════════════════════

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
        System.out.println("=== [agregarAmenidadHotel] INICIO ===");
        System.out.println("  hotelId    = " + hotelId);
        System.out.println("  amenidadId = " + req.getAmenidadId());
        System.out.println("  descripcion= " + req.getDescripcion());

        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        if (req.getAmenidadId() <= 0)
            throw new IllegalArgumentException("Amenidad inválida");
        if (hotelRepository.tieneAmenidad(hotelId, req.getAmenidadId()))
            throw new IllegalArgumentException("Este hotel ya tiene esa amenidad asignada.");

        int id;
        try {
            System.out.println("  Insertando HotelAmenidad...");
            id = hotelRepository.agregarAmenidadHotel(hotelId, req.getAmenidadId(), safe(req.getDescripcion()));
            System.out.println("  [OK] HotelAmenidad id = " + id);
        } catch (Exception e) {
            System.out.println("  [ERROR] agregarAmenidadHotel: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== [agregarAmenidadHotel] FIN OK ===");
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
    //  HABITACIONES — listar
    // ════════════════════════════════════════════════════

    public List<HabitacionAdminDTO> listarHabitaciones(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HabitacionAdminDTO> lista = hotelRepository.listarHabitacionesPorHotel(hotelId);
        for (HabitacionAdminDTO h : lista)
            h.setImagenesIds(hotelRepository.obtenerImagenesHabitacionIds(h.getId()));
        return lista;
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — crear
    // ════════════════════════════════════════════════════

    public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO req) {
        System.out.println("=== [crearHabitacion] INICIO ===");
        System.out.println("  hotelId          = " + req.getHotelId());
        System.out.println("  tipoHabitacionId = " + req.getTipoHabitacionId());
        System.out.println("  camaId           = " + req.getCamaId());
        System.out.println("  precioPorPersona = " + req.getPrecioPorPersona());
        System.out.println("  precioPorNoche   = " + req.getPrecioPorNoche());
        System.out.println("  capacidadMaxima  = " + req.getCapacidadMaxima());
        System.out.println("  metrosCuadrados  = " + req.getMetrosCuadrados());
        System.out.println("  descripcion      = " + req.getDescripcion());
        System.out.println("  estadoId         = " + req.getEstadoId());

        if (!hotelRepository.existe(req.getHotelId()))
            throw new IllegalArgumentException("Hotel no encontrado: " + req.getHotelId());

        try {
            validarHabitacion(req.getPrecioPorNoche(), req.getPrecioPorPersona(),
                    req.getCapacidadMaxima(), req.getEstadoId());
            System.out.println("  [OK] validarHabitacion paso");
        } catch (Exception e) {
            System.out.println("  [ERROR] validarHabitacion: " + e.getMessage());
            throw e;
        }

        int id;
        try {
            System.out.println("  Insertando habitacion en BD...");
            id = hotelRepository.crearHabitacion(
                    req.getHotelId(), req.getTipoHabitacionId(), req.getCamaId(),
                    req.getPrecioPorPersona(), req.getPrecioPorNoche(),
                    req.getCapacidadMaxima(), req.getMetrosCuadrados(),
                    safe(req.getDescripcion()), req.getEstadoId()
            );
            System.out.println("  [OK] habitacionId = " + id);
        } catch (Exception e) {
            System.out.println("  [ERROR] hotelRepository.crearHabitacion: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== [crearHabitacion] FIN OK — id=" + id + " ===");
        return Map.of("id", id, "mensaje", "Habitación creada correctamente");
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — editar
    // ════════════════════════════════════════════════════

    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO req) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitación no encontrada: " + habitacionId);
        validarHabitacion(req.getPrecioPorNoche(), req.getPrecioPorPersona(),
                req.getCapacidadMaxima(), req.getEstadoId());

        hotelRepository.actualizarHabitacion(
                habitacionId, req.getTipoHabitacionId(), req.getCamaId(),
                req.getPrecioPorPersona(), req.getPrecioPorNoche(),
                req.getCapacidadMaxima(), req.getMetrosCuadrados(),
                safe(req.getDescripcion()), req.getEstadoId()
        );
    }

    // ════════════════════════════════════════════════════
    //  HABITACIONES — eliminar
    // ════════════════════════════════════════════════════

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

    private void validarHabitacion(double precioPorNoche, double precioPorPersona,
                                   int capacidadMaxima, int estadoId) {
        if (precioPorNoche < 0 || precioPorPersona < 0)
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        if (capacidadMaxima < 1)
            throw new IllegalArgumentException("La capacidad mínima es 1 persona");
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

    public java.util.List<java.util.Map<String, Object>> listarTodasReservaciones() {
        return hotelRepository.listarTodasReservaciones();
    }

    public java.util.Map<String, Object> obtenerMetricas() {
        return hotelRepository.obtenerMetricas();
    }
}