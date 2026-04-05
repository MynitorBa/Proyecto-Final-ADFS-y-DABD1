package org.example.services;

import org.example.dtos.*;
import org.example.repositories.CiudadRepository;
import org.example.repositories.HotelRepository;
import org.example.repositories.PaisRepository;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Service principal para la gestion de hoteles desde el panel de administracion.
 * Cubre catalogos, hoteles, amenidades, habitaciones, imagenes y metricas.
 */
public class HotelService {

    private final HotelRepository  hotelRepository  = new HotelRepository();
    private final CiudadRepository ciudadRepository = new CiudadRepository();
    private final PaisRepository   paisRepository   = new PaisRepository();

    /**
     * Retorna todas las amenidades disponibles en el catalogo.
     * @return lista de amenidades.
     */
    public List<AmenidadDTO> listarAmenidades() { return hotelRepository.listarAmenidades(); }

    /**
     * Retorna todos los paises registrados en el sistema.
     * @return lista de paises.
     */
    public List<PaisDTO> listarPaises() { return paisRepository.listarPaises(); }

    /**
     * Retorna todas las ciudades registradas en el sistema.
     * @return lista de ciudades.
     */
    public List<CiudadDTO> listarCiudades() { return paisRepository.listarCiudades(); }

    /**
     * Crea una nueva amenidad en el catalogo.
     * @param nombre nombre de la amenidad, no puede ser nulo ni vacio.
     * @return mapa con el ID, nombre y mensaje de confirmacion.
     * @throws IllegalArgumentException si el nombre es nulo o vacio.
     */
    public Map<String, Object> crearAmenidad(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la amenidad es obligatorio");
        int id = hotelRepository.crearAmenidad(nombre.trim());
        return Map.of("id", id, "nombre", nombre.trim(), "mensaje", "Amenidad creada correctamente");
    }

    /**
     * Retorna todos los hoteles con su cantidad de habitaciones e IDs de imagenes.
     * @return lista de hoteles con datos enriquecidos para el panel de admin.
     */
    public List<HotelAdminDTO> listarTodos() {
        List<HotelAdminDTO> hoteles = hotelRepository.listarTodos();
        for (HotelAdminDTO hotel : hoteles) {
            hotel.setCantidadHabitaciones(hotelRepository.contarHabitaciones(hotel.getId()));
            hotel.setImagenesIds(hotelRepository.obtenerImagenesIds(hotel.getId()));
        }
        return hoteles;
    }

    /**
     * Crea un nuevo hotel, buscando o creando el pais y ciudad si no existen.
     * @param req datos del hotel: nombre, rating, estado, ciudad y pais.
     * @return mapa con el ID del hotel creado y mensaje de confirmacion.
     * @throws IllegalArgumentException si algun campo obligatorio es invalido.
     */
    public Map<String, Object> crearHotel(CrearHotelRequestDTO req) {
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        if (req.getCiudad() == null || req.getCiudad().isBlank())
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        if (req.getPaisNombre() == null || req.getPaisNombre().isBlank())
            throw new IllegalArgumentException("El nombre del pais es obligatorio");

        int paisId   = paisRepository.buscarOCrearPorNombre(req.getPaisNombre().trim());
        int ciudadId = ciudadRepository.buscarOCrearPorNombre(req.getCiudad().trim(), paisId);

        int hotelId = hotelRepository.crearHotel(
                req.getNombre().trim(), safe(req.getDireccion()),
                safe(req.getDescripcion()), req.getRating(),
                req.getEstadoId(), ciudadId);
        return Map.of("id", hotelId, "mensaje", "Hotel creado correctamente");
    }

    /**
     * Edita los datos de un hotel existente.
     * @param hotelId ID del hotel a editar.
     * @param req     datos actualizados del hotel.
     * @throws IllegalArgumentException si el hotel no existe o los datos son invalidos.
     */
    public void editarHotel(int hotelId, EditarHotelRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        validarCamposHotel(req.getNombre(), req.getRating(), req.getEstadoId());
        hotelRepository.actualizarHotel(hotelId, req.getNombre().trim(),
                safe(req.getDireccion()), safe(req.getDescripcion()),
                req.getRating(), req.getEstadoId());
    }

    /**
     * Elimina un hotel del sistema.
     * @param hotelId ID del hotel a eliminar.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public void eliminarHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        hotelRepository.eliminarHotel(hotelId);
    }

    /**
     * Retorna las amenidades de un hotel con sus IDs de imagenes.
     * @param hotelId ID del hotel.
     * @return lista de amenidades del hotel.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public List<HotelAmenidadDTO> listarAmenidadesHotel(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HotelAmenidadDTO> lista = hotelRepository.listarAmenidadesHotel(hotelId);
        for (HotelAmenidadDTO a : lista)
            a.setImagenesIds(hotelRepository.obtenerImagenesAmenidadIds(a.getId()));
        return lista;
    }

    /**
     * Agrega una amenidad del catalogo a un hotel.
     * Valida que el hotel exista y que no tenga ya esa amenidad asignada.
     * @param hotelId ID del hotel.
     * @param req     datos con el ID de amenidad y descripcion opcional.
     * @return mapa con el ID del registro creado y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe, la amenidad es invalida
     *                                  o el hotel ya tiene esa amenidad.
     */
    public Map<String, Object> agregarAmenidadHotel(int hotelId, AgregarAmenidadRequestDTO req) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        if (req.getAmenidadId() <= 0)
            throw new IllegalArgumentException("Amenidad invalida");
        if (hotelRepository.tieneAmenidad(hotelId, req.getAmenidadId()))
            throw new IllegalArgumentException("Este hotel ya tiene esa amenidad asignada.");
        int id = hotelRepository.agregarAmenidadHotel(hotelId, req.getAmenidadId(), safe(req.getDescripcion()));
        return Map.of("id", id, "mensaje", "Amenidad agregada");
    }

    /**
     * Actualiza la descripcion de una amenidad asignada a un hotel.
     * @param hotelAmenidadId ID del registro hotel-amenidad.
     * @param req             datos con la nueva descripcion.
     */
    public void actualizarAmenidadHotel(int hotelAmenidadId, AgregarAmenidadRequestDTO req) {
        hotelRepository.actualizarAmenidadHotel(hotelAmenidadId, safe(req.getDescripcion()));
    }

    /**
     * Elimina una amenidad asignada a un hotel.
     * @param hotelAmenidadId ID del registro hotel-amenidad a eliminar.
     */
    public void eliminarAmenidadHotel(int hotelAmenidadId) {
        hotelRepository.eliminarAmenidadHotel(hotelAmenidadId);
    }

    /**
     * Agrega una imagen a una amenidad de hotel decodificando el base64 recibido.
     * @param hotelAmenidadId ID del registro hotel-amenidad.
     * @param base64          imagen codificada en base64, con o sin prefijo data URI.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     */
    public Map<String, Object> agregarImagenAmenidad(int hotelAmenidadId, String base64) {
        int nuevoId = hotelRepository.agregarImagenAmenidad(hotelAmenidadId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen de amenidad agregada");
    }

    /**
     * Elimina una imagen de amenidad por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenAmenidad(int imagenId) {
        hotelRepository.eliminarImagenAmenidad(imagenId);
    }

    /**
     * Agrega una imagen al hotel decodificando el base64 recibido.
     * @param hotelId ID del hotel.
     * @param base64  imagen codificada en base64.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public Map<String, Object> agregarImagenHotel(int hotelId, String base64) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        int nuevoId = hotelRepository.agregarImagenHotel(hotelId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    /**
     * Elimina una imagen de hotel por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHotel(int imagenId) {
        hotelRepository.eliminarImagenHotel(imagenId);
    }

    /**
     * Retorna las habitaciones de un hotel con sus IDs de imagenes.
     * @param hotelId ID del hotel.
     * @return lista de habitaciones con datos para el panel de admin.
     * @throws IllegalArgumentException si el hotel no existe.
     */
    public List<HabitacionAdminDTO> listarHabitaciones(int hotelId) {
        if (!hotelRepository.existe(hotelId))
            throw new IllegalArgumentException("Hotel no encontrado: " + hotelId);
        List<HabitacionAdminDTO> lista = hotelRepository.listarHabitacionesPorHotel(hotelId);
        for (HabitacionAdminDTO h : lista)
            h.setImagenesIds(hotelRepository.obtenerImagenesHabitacionIds(h.getId()));
        return lista;
    }

    /**
     * Crea una nueva habitacion en un hotel.
     * El numero de habitacion se auto-asigna en el repositorio.
     * @param req datos de la habitacion: hotelId, tipo, descripcion y estado.
     * @return mapa con el ID de la habitacion creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si el hotel no existe, el tipo es invalido o el estado no es valido.
     */
    public Map<String, Object> crearHabitacion(CrearHabitacionRequestDTO req) {
        if (!hotelRepository.existe(req.getHotelId()))
            throw new IllegalArgumentException("Hotel no encontrado: " + req.getHotelId());
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitacion invalido");
        validarEstadoHabitacion(req.getEstadoId());

        int id = hotelRepository.crearHabitacion(
                req.getHotelId(),
                req.getTipoHabitacionId(),
                safe(req.getDescripcion()),
                req.getEstadoId()
        );
        return Map.of("id", id, "mensaje", "Habitacion creada correctamente");
    }

    /**
     * Edita los datos de una habitacion existente.
     * @param habitacionId ID de la habitacion a editar.
     * @param req          datos actualizados de la habitacion.
     * @throws IllegalArgumentException si la habitacion no existe, el tipo es invalido o el estado no es valido.
     */
    public void editarHabitacion(int habitacionId, EditarHabitacionRequestDTO req) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        if (req.getTipoHabitacionId() <= 0)
            throw new IllegalArgumentException("Tipo de habitacion invalido");
        validarEstadoHabitacion(req.getEstadoId());
        hotelRepository.actualizarHabitacion(habitacionId,
                req.getTipoHabitacionId(),
                safe(req.getNumeroHabitacion()),
                safe(req.getDescripcion()),
                req.getEstadoId());
    }

    /**
     * Elimina una habitacion del sistema.
     * @param habitacionId ID de la habitacion a eliminar.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public void eliminarHabitacion(int habitacionId) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        hotelRepository.eliminarHabitacion(habitacionId);
    }

    /**
     * Agrega una imagen a una habitacion decodificando el base64 recibido.
     * @param habitacionId ID de la habitacion.
     * @param base64       imagen codificada en base64.
     * @return mapa con el ID de la imagen creada y mensaje de confirmacion.
     * @throws IllegalArgumentException si la habitacion no existe.
     */
    public Map<String, Object> agregarImagenHabitacion(int habitacionId, String base64) {
        if (!hotelRepository.existeHabitacion(habitacionId))
            throw new IllegalArgumentException("Habitacion no encontrada: " + habitacionId);
        int nuevoId = hotelRepository.agregarImagenHabitacion(habitacionId, decodeBase64(base64));
        return Map.of("id", nuevoId, "mensaje", "Imagen agregada");
    }

    /**
     * Elimina una imagen de habitacion por su ID.
     * @param imagenId ID de la imagen a eliminar.
     */
    public void eliminarImagenHabitacion(int imagenId) {
        hotelRepository.eliminarImagenHabitacion(imagenId);
    }

    /**
     * Valida los campos basicos de un hotel: nombre, rating y estado.
     * @throws IllegalArgumentException si alguno de los valores es invalido.
     */
    private void validarCamposHotel(String nombre, double rating, int estadoId) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del hotel no puede estar vacio");
        if (rating < 0 || rating > 5)
            throw new IllegalArgumentException("El rating debe estar entre 0 y 5");
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado invalido: use 1 (Activo) o 2 (Cerrado)");
    }

    /**
     * Valida que el estado de una habitacion sea 1 (Activa) o 2 (Cerrada).
     * @throws IllegalArgumentException si el estado no es valido.
     */
    private void validarEstadoHabitacion(int estadoId) {
        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("Estado invalido: use 1 (Activa) o 2 (Cerrada)");
    }

    /**
     * Decodifica una imagen en base64 a bytes.
     * Soporta strings con o sin prefijo data URI (ej. data:image/png;base64,...).
     * @param base64 imagen en base64.
     * @return array de bytes de la imagen.
     * @throws IllegalArgumentException si el string es nulo o vacio.
     */
    private byte[] decodeBase64(String base64) {
        if (base64 == null || base64.isBlank())
            throw new IllegalArgumentException("La imagen no puede estar vacia");
        String datos = base64.contains(",") ? base64.split(",", 2)[1] : base64;
        return Base64.getDecoder().decode(datos);
    }

    /**
     * Retorna el string recortado o vacio si es null.
     * Evita insertar nulls en la base de datos para campos opcionales.
     */
    private String safe(String s) { return s != null ? s.trim() : ""; }

    /**
     * Retorna todas las reservaciones del sistema para el panel de admin.
     * @return lista de reservaciones con sus datos principales.
     */
    public List<Map<String, Object>> listarTodasReservaciones() {
        return hotelRepository.listarTodasReservaciones();
    }

    /**
     * Retorna metricas generales del sistema: reservaciones, ingresos, ocupacion, etc.
     * @return mapa con los valores de cada metrica.
     */
    public Map<String, Object> obtenerMetricas() {
        return hotelRepository.obtenerMetricas();
    }
}