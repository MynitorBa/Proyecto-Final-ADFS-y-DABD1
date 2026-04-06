package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service para la gestion de reservaciones de habitaciones.
 * Maneja la creacion de reservaciones con validacion de fechas, disponibilidad
 * y calculo de precios, incluyendo cargos por personas extra.
 */
public class ReservacionService {

    private final ReservacionRepository reservacionRepository;

    /**
     * Crea una instancia de ReservacionService con sus dependencias inyectadas.
     */
    public ReservacionService(ReservacionRepository reservacionRepository) {
        this.reservacionRepository = reservacionRepository;
    }

    /**
     * Crea una nueva reservacion para el usuario indicado.
     * Valida fechas, disponibilidad de habitaciones y capacidad maxima antes de persistir.
     * El numero de reservacion se genera automaticamente con el prefijo MIKU-.
     * La reservacion queda en estado pendiente y expira en 10 minutos si no se confirma.
     *
     * @param request    datos de la reservacion, incluyendo la lista de habitaciones solicitadas.
     * @param usuarioId  ID del usuario que realiza la reservacion.
     * @return DTO con los datos de la reservacion creada.
     * @throws IllegalArgumentException si no se incluyen habitaciones, las fechas son invalidas,
     *                                  hay traslape de disponibilidad, o se excede la capacidad permitida.
     */
    public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request, int usuarioId) {

        if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe incluir al menos una habitación");
        }

        // Primera pasada: validar todas las habitaciones y acumular el total antes de persistir
        double totalGeneral = 0;
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {

            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            LocalDate hoy      = LocalDate.now();

            // Fechas no pueden ser en el pasado
            if (checkIn.isBefore(hoy)) {
                throw new IllegalArgumentException(
                        "La fecha de check-in no puede ser anterior a hoy"
                );
            }
            if (checkOut.isBefore(hoy)) {
                throw new IllegalArgumentException(
                        "La fecha de check-out no puede ser anterior a hoy"
                );
            }

            // Check-out debe ser al menos 1 dia despues del check-in
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (dias < 1) {
                throw new IllegalArgumentException(
                        "La fecha de check-out debe ser al menos 1 día después del check-in"
                );
            }

            Date fechaCheckIn  = Date.valueOf(checkIn);
            Date fechaCheckOut = Date.valueOf(checkOut);

            // Verificar que la habitacion no este reservada en ese rango de fechas
            if (reservacionRepository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut)) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " no está disponible para las fechas seleccionadas"
                );
            }

            // precios[0] = precioPorNoche, precios[1] = precioPorPersona, precios[2] = capacidadMaxima
            double[] precios = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];
            int capacidadMaxima     = (int) precios[2];

            int personasSolicitadas = item.getCantidadPersonas();

            // Validar que no exceda capacidad + 1 (maximo 1 persona extra por habitacion)
            if (personasSolicitadas > capacidadMaxima + 1) {
                throw new IllegalArgumentException(
                        "La habitación " + item.getHabitacionId() +
                                " tiene capacidad máxima de " + capacidadMaxima +
                                " personas (+1 extra). No se pueden alojar " + personasSolicitadas + " personas."
                );
            }

            // Calcular personas extra (las que exceden la capacidad base)
            int personasExtra = Math.max(0, personasSolicitadas - capacidadMaxima);

            // Total = (noches * precioPorNoche) + (personasExtra * noches * precioPorPersona)
            totalGeneral += (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);
        }

        // Generar numero de reservacion unico y timestamps de creacion y expiracion
        String noReservacion      = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
        Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

        int reservacionId = reservacionRepository.crearReservacion(
                noReservacion, totalGeneral, usuarioId, fechaCreacion, fechaExpiracion
        );

        // Expirar otras reservaciones pendientes del mismo usuario para evitar duplicados
        reservacionRepository.expirarPendientesDeUsuario(usuarioId, reservacionId);

        // Segunda pasada: persistir el detalle de cada habitacion con su subtotal
        for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {
            LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
            LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
            long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

            double[] precios        = reservacionRepository.obtenerPrecios(item.getHabitacionId());
            double precioPorNoche   = precios[0];
            double precioPorPersona = precios[1];
            int capacidadMaxima     = (int) precios[2];

            int personasSolicitadas = item.getCantidadPersonas();
            int personasExtra       = Math.max(0, personasSolicitadas - capacidadMaxima);

            double totalHabitacion = (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);

            reservacionRepository.crearDetalle(
                    reservacionId,
                    item.getHabitacionId(),
                    Date.valueOf(checkIn),
                    Date.valueOf(checkOut),
                    item.getCantidadPersonas(),
                    totalHabitacion
            );
        }

        // Recuperar la reservacion recien creada para armar el response
        Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

        ReservacionResponseDTO response = new ReservacionResponseDTO();
        response.setId((int) datos[0]);
        response.setNoReservacion((String) datos[1]);
        response.setTotal((double) datos[2]);
        response.setFechaCreacion((String) datos[3]);
        response.setFechaExpiracion((String) datos[4]);
        response.setEstado((String) datos[5]);

        return response;
    }

    /**
     * Obtiene todas las reservaciones de un usuario con sus imagenes asociadas.
     * Por cada reservacion se cargan las imagenes del hotel y de la habitacion correspondiente.
     *
     * @param usuarioId ID del usuario del que se quieren obtener las reservaciones.
     * @return lista de DTOs con el detalle de cada reservacion e imagenes incluidas.
     */
    public List<ReservacionDetalleDTO> obtenerReservaciones(int usuarioId) {
        List<ReservacionDetalleDTO> reservaciones = reservacionRepository.obtenerReservacionesDeUsuario(usuarioId);

        // Cargar imagenes del hotel y habitacion para cada reservacion
        for (ReservacionDetalleDTO dto : reservaciones) {
            dto.setImagenesHotelIds(reservacionRepository.obtenerImagenesHotel(dto.getHotelId()));
            dto.setImagenesHabitacionIds(reservacionRepository.obtenerImagenesHabitacion(dto.getHabitacionId()));
        }

        return reservaciones;
    }
}