package org.example.services;

import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.repositories.ReservacionRepository;

import org.example.repositories.LogReservacionRepository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import java.util.ArrayList;
import org.example.dtos.CambioFechasMultipleRequestDTO;

/**
 * Service para la gestion de reservaciones de habitaciones.
 * Maneja la creacion de reservaciones con validacion de fechas, disponibilidad
 * y calculo de precios, incluyendo cargos por personas extra.
 */
public class ReservacionService {

    private final ReservacionRepository reservacionRepository;

    private final LogReservacionRepository logReservacionRepository;


    /**
     * Crea una instancia de ReservacionService con sus dependencias inyectadas.
     */
    public ReservacionService(ReservacionRepository reservacionRepository,
                              LogReservacionRepository logReservacionRepository) {
        this.reservacionRepository     = reservacionRepository;
        this.logReservacionRepository  = logReservacionRepository;
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
    public ReservacionResponseDTO crearReservacion(ReservacionRequestDTO request,
                                                   int usuarioId, String ip, String userAgent) {
        try {
            if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty()) {
                throw new IllegalArgumentException("Debe incluir al menos una habitación");
            }

            double totalGeneral = 0;
            for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {

                LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
                LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
                LocalDate hoy      = LocalDate.now();

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

                long dias = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (dias < 1) {
                    throw new IllegalArgumentException(
                            "La fecha de check-out debe ser al menos 1 día después del check-in"
                    );
                }

                Date fechaCheckIn  = Date.valueOf(checkIn);
                Date fechaCheckOut = Date.valueOf(checkOut);

                if (reservacionRepository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut)) {
                    throw new IllegalArgumentException(
                            "La habitación " + item.getHabitacionId() +
                                    " no está disponible para las fechas seleccionadas"
                    );
                }

                double[] precios        = reservacionRepository.obtenerPrecios(item.getHabitacionId());
                double precioPorNoche   = precios[0];
                double precioPorPersona = precios[1];
                int capacidadMaxima     = (int) precios[2];

                int personasSolicitadas = item.getCantidadPersonas();

                if (personasSolicitadas > capacidadMaxima + 1) {
                    throw new IllegalArgumentException(
                            "La habitación " + item.getHabitacionId() +
                                    " tiene capacidad máxima de " + capacidadMaxima +
                                    " personas (+1 extra). No se pueden alojar " + personasSolicitadas + " personas."
                    );
                }

                int personasExtra = Math.max(0, personasSolicitadas - capacidadMaxima);
                totalGeneral += (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);
            }

            String noReservacion      = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
            Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

            int reservacionId = reservacionRepository.crearReservacion(
                    noReservacion, totalGeneral, usuarioId, fechaCreacion, fechaExpiracion
            );

            reservacionRepository.expirarPendientesDeUsuario(usuarioId, reservacionId);

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
                double totalHabitacion  = (dias * precioPorNoche) + (personasExtra * dias * precioPorPersona);

                reservacionRepository.crearDetalle(
                        reservacionId,
                        item.getHabitacionId(),
                        Date.valueOf(checkIn),
                        Date.valueOf(checkOut),
                        item.getCantidadPersonas(),
                        totalHabitacion
                );
            }

            Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

            ReservacionResponseDTO response = new ReservacionResponseDTO();
            response.setId((int) datos[0]);
            response.setNoReservacion((String) datos[1]);
            response.setTotal((double) datos[2]);
            response.setFechaCreacion((String) datos[3]);
            response.setFechaExpiracion((String) datos[4]);
            response.setEstado((String) datos[5]);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_RESERVACION_EXITOSA,
                    response.getId(),
                    usuarioId,
                    null,
                    response.getNoReservacion(),
                    response.getTotal(),
                    true,
                    ip,
                    userAgent,
                    null
            );

            return response;

        } catch (IllegalArgumentException e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_RESERVACION_FALLIDA,
                    null,
                    usuarioId,
                    null,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_RESERVACION_ERROR_INTERNO,
                    null,
                    usuarioId,
                    null,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw new RuntimeException("Error interno al crear reservacion", e);
        }
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








    /**
     * Cambia el rango de fechas de un detalle sin modificar la duracion de la estadia.
     * El check-out se desplaza automaticamente para mantener los mismos dias.
     * El total NO se recalcula — misma duracion, mismo precio.
     */
    public ReservacionResponseDTO cambiarFechas(int detalleId, String nuevaCheckIn,
                                                String nuevaCheckOut, int usuarioId,
                                                String ip, String userAgent) {
        try {
            Object[] detalle = reservacionRepository.obtenerDetalle(detalleId);
            if (detalle == null) {
                throw new IllegalArgumentException("El detalle de reservacion no existe");
            }

            int    propietario   = (int)    detalle[7];
            String estado        = (String) detalle[8];
            int    habitacionId  = (int)    detalle[2];
            int    personas      = (int)    detalle[5];
            int    reservacionId = (int)    detalle[1];
            double totalActual   = (double) detalle[6]; // total original — no cambia

            if (propietario != usuarioId) {
                throw new SecurityException("No tienes permiso para modificar esta reservacion");
            }
            if (!estado.equals("pendiente") && !estado.equals("confirmada")) {
                throw new IllegalArgumentException(
                        "Solo se pueden cambiar fechas de reservaciones pendientes o confirmadas"
                );
            }

            // Dias originales de la reservacion
            Date      fechaCheckInActual  = (Date) detalle[3];
            Date      fechaCheckOutActual = (Date) detalle[4];
            long diasOriginales = ChronoUnit.DAYS.between(
                    fechaCheckInActual.toLocalDate(),
                    fechaCheckOutActual.toLocalDate()
            );

            // Validar 48 horas de anticipacion sobre el check-in ACTUAL
            long horasHastaCheckIn = ChronoUnit.HOURS.between(
                    LocalDateTime.now(), fechaCheckInActual.toLocalDate().atTime(0, 0)
            );
            if (horasHastaCheckIn <= 48) {
                throw new IllegalArgumentException(
                        "No se pueden cambiar las fechas con menos de 48 horas de anticipación al check-in actual"
                );
            }

            LocalDate checkIn  = LocalDate.parse(nuevaCheckIn);
            LocalDate checkOut = LocalDate.parse(nuevaCheckOut);
            LocalDate hoy      = LocalDate.now();

            if (checkIn.isBefore(hoy)) {
                throw new IllegalArgumentException("La fecha de check-in no puede ser anterior a hoy");
            }

            // *** REGLA: misma duracion, solo se mueve el rango ***
            long diasNuevos = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (diasNuevos != diasOriginales) {
                throw new IllegalArgumentException(
                        "Solo puedes mover las fechas, no cambiar la duración. " +
                                "La estadía es de " + diasOriginales + " noche(s)."
                );
            }

            Date fechaCheckIn  = Date.valueOf(checkIn);
            Date fechaCheckOut = Date.valueOf(checkOut);

            if (reservacionRepository.existeTraslapeExcluyendoDetalle(
                    habitacionId, fechaCheckIn, fechaCheckOut, detalleId)) {
                throw new IllegalArgumentException(
                        "La habitacion no está disponible para las fechas seleccionadas"
                );
            }

            // Pasar totalActual — el repository lo escribe sin recalcular nada
            reservacionRepository.actualizarFechasDetalle(
                    detalleId, fechaCheckIn, fechaCheckOut, personas, totalActual
            );

            Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

            ReservacionResponseDTO response = new ReservacionResponseDTO();
            response.setId((int)    datos[0]);
            response.setNoReservacion((String) datos[1]);
            response.setTotal((double)  datos[2]);
            response.setFechaCreacion((String)  datos[3]);
            response.setFechaExpiracion((String) datos[4]);
            response.setEstado((String) datos[5]);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_EXITOSO,
                    response.getId(), usuarioId, null,
                    response.getNoReservacion(), response.getTotal(),
                    true, ip, userAgent, null
            );
            return response;

        } catch (IllegalArgumentException | SecurityException e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_FALLIDO,
                    null, usuarioId, null, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_ERROR_INTERNO,
                    null, usuarioId, null, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw new RuntimeException("Error interno al cambiar fechas", e);
        }
    }

    /**
     * Cambia el rango de fechas de multiples detalles de forma atomica.
     * Cada detalle debe mantener exactamente la misma duracion original.
     * El total de ningun detalle cambia. Si alguna validacion falla, nada se persiste.
     */
    public ReservacionResponseDTO cambiarFechasMultiple(
            int reservacionId,
            List<CambioFechasMultipleRequestDTO.CambioDetalle> cambios,
            int usuarioId, String ip, String userAgent) {
        try {
            if (cambios == null || cambios.isEmpty()) {
                throw new IllegalArgumentException("Debes incluir al menos un cambio de fechas");
            }

            List<Object[]> persistir = new ArrayList<>();

            for (CambioFechasMultipleRequestDTO.CambioDetalle cambio : cambios) {

                Object[] detalle = reservacionRepository.obtenerDetalle(cambio.getDetalleId());
                if (detalle == null) {
                    throw new IllegalArgumentException(
                            "El detalle " + cambio.getDetalleId() + " no existe"
                    );
                }

                int    propietario    = (int)    detalle[7];
                String estado         = (String) detalle[8];
                int    habitacionId   = (int)    detalle[2];
                int    personas       = (int)    detalle[5];
                int    resIdDelDetalle= (int)    detalle[1];
                double totalActual    = (double) detalle[6]; // total original — no cambia

                if (resIdDelDetalle != reservacionId) {
                    throw new IllegalArgumentException(
                            "El detalle " + cambio.getDetalleId() +
                                    " no pertenece a la reservacion " + reservacionId
                    );
                }
                if (propietario != usuarioId) {
                    throw new SecurityException(
                            "No tienes permiso para modificar el detalle " + cambio.getDetalleId()
                    );
                }
                if (!estado.equals("pendiente") && !estado.equals("confirmada")) {
                    throw new IllegalArgumentException(
                            "Solo se pueden cambiar fechas de reservaciones pendientes o confirmadas"
                    );
                }

                // Dias originales de este detalle
                Date fechaCheckInActual  = (Date) detalle[3];
                Date fechaCheckOutActual = (Date) detalle[4];
                long diasOriginales = ChronoUnit.DAYS.between(
                        fechaCheckInActual.toLocalDate(),
                        fechaCheckOutActual.toLocalDate()
                );

                // Validar 48 horas sobre el check-in ACTUAL
                long horasHastaCheckIn = ChronoUnit.HOURS.between(
                        LocalDateTime.now(), fechaCheckInActual.toLocalDate().atTime(0, 0)
                );
                if (horasHastaCheckIn <= 48) {
                    throw new IllegalArgumentException(
                            "La habitación " + habitacionId +
                                    " no permite cambios con menos de 48 horas de anticipación al check-in actual"
                    );
                }

                LocalDate checkIn  = LocalDate.parse(cambio.getFechaCheckIn());
                LocalDate checkOut = LocalDate.parse(cambio.getFechaCheckOut());
                LocalDate hoy      = LocalDate.now();

                if (checkIn.isBefore(hoy)) {
                    throw new IllegalArgumentException(
                            "La fecha de check-in no puede ser anterior a hoy " +
                                    "(habitación " + habitacionId + ")"
                    );
                }

                // *** REGLA: misma duracion, solo se mueve el rango ***
                long diasNuevos = ChronoUnit.DAYS.between(checkIn, checkOut);
                if (diasNuevos != diasOriginales) {
                    throw new IllegalArgumentException(
                            "La habitación " + habitacionId + " está reservada por " +
                                    diasOriginales + " noche(s). No puedes cambiar la duración, solo mover las fechas."
                    );
                }

                Date fechaCheckIn  = Date.valueOf(checkIn);
                Date fechaCheckOut = Date.valueOf(checkOut);

                if (reservacionRepository.existeTraslapeExcluyendoDetalle(
                        habitacionId, fechaCheckIn, fechaCheckOut, cambio.getDetalleId())) {
                    throw new IllegalArgumentException(
                            "La habitación " + habitacionId +
                                    " no está disponible del " + cambio.getFechaCheckIn() +
                                    " al " + cambio.getFechaCheckOut()
                    );
                }

                // Total intacto — acumular para persistir atomicamente
                persistir.add(new Object[]{ fechaCheckIn, fechaCheckOut, totalActual, cambio.getDetalleId() });
            }

            reservacionRepository.actualizarFechasDetallesAtomico(persistir);

            Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

            ReservacionResponseDTO response = new ReservacionResponseDTO();
            response.setId((int)     datos[0]);
            response.setNoReservacion((String)  datos[1]);
            response.setTotal((double)   datos[2]);
            response.setFechaCreacion((String)  datos[3]);
            response.setFechaExpiracion((String) datos[4]);
            response.setEstado((String)  datos[5]);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_EXITOSO,
                    response.getId(), usuarioId, null,
                    response.getNoReservacion(), response.getTotal(),
                    true, ip, userAgent,
                    "Cambio atomico (misma duracion) de " + cambios.size() + " detalle(s)"
            );
            return response;

        } catch (IllegalArgumentException | SecurityException e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_FALLIDO,
                    null, usuarioId, null, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_ERROR_INTERNO,
                    null, usuarioId, null, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw new RuntimeException("Error interno al cambiar fechas", e);
        }
    }
}