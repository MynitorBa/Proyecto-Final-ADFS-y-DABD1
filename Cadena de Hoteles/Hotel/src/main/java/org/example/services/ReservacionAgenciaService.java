package org.example.services;

import org.example.dtos.HabitacionAgenciaResponseDTO;
import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionAgenciaResponseDTO;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.repositories.ReservacionAgenciaRepository;

import org.example.repositories.LogReservacionRepository;


import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service para la gestion de reservaciones realizadas por agencias.
 * Maneja la creacion de reservaciones SIN aplicar descuento (devuelve precios originales),
 * consulta de reservaciones y expiracion manual de reservaciones pendientes.
 * El descuento es responsabilidad de Movent (nivel logico/negocio).
 */
public class ReservacionAgenciaService {

    private final ReservacionAgenciaRepository repository;
    private final LogReservacionRepository logReservacionRepository;


    /**
     * Crea una instancia de ReservacionAgenciaService con sus dependencias inyectadas.
     */
    public ReservacionAgenciaService(ReservacionAgenciaRepository repository,
                                     LogReservacionRepository logReservacionRepository) {
        this.repository                = repository;
        this.logReservacionRepository  = logReservacionRepository;
    }

    /**
     * Crea una nueva reservacion para una agencia SIN aplicar descuento.
     * Devuelve precios originales. El descuento es responsabilidad de Movent.
     * Valida disponibilidad de cada habitacion, calcula totales con precios originales,
     * genera el numero de reservacion y persiste los detalles.
     * La reservacion expira automaticamente en 10 minutos si no se paga.
     * @param request   datos de la reservacion: lista de habitaciones con fechas y personas.
     * @param agenciaId ID de la agencia que realiza la reservacion.
     * @return DTO con los datos de la reservacion creada y el desglose por habitacion (precios originales).
     * @throws IllegalArgumentException si la agencia no esta activa, no hay habitaciones,
     *                                  las fechas son invalidas o alguna habitacion no esta disponible.
     */
    public ReservacionAgenciaResponseDTO crearReservacion(ReservacionRequestDTO request,
                                                          int agenciaId, String ip, String userAgent) {
        try {
            int[] datosAgencia = repository.obtenerDatosAgencia(agenciaId);
            if (datosAgencia == null)
                throw new IllegalArgumentException("La agencia no esta activa");

            int    usuarioWebisId      = datosAgencia[0];
            double porcentajeDescuento = repository.obtenerDescuentoAgencia(agenciaId);

            if (request.getHabitaciones() == null || request.getHabitaciones().isEmpty())
                throw new IllegalArgumentException("Debe incluir al menos una habitacion");

            // NO aplicar descuento: devolver precios originales SIN modificar
            // El descuento es responsabilidad de Movent (nivel logico/negocio)
            double totalGeneral = 0;
            List<HabitacionAgenciaResponseDTO> desglose = new ArrayList<>();

            for (HabitacionReservaRequestDTO item : request.getHabitaciones()) {
                LocalDate checkIn  = LocalDate.parse(item.getFechaCheckIn());
                LocalDate checkOut = LocalDate.parse(item.getFechaCheckOut());
                long dias = ChronoUnit.DAYS.between(checkIn, checkOut);

                if (dias <= 0)
                    throw new IllegalArgumentException(
                            "Las fechas de la habitacion " + item.getHabitacionId() + " son invalidas");

                Date fechaCheckIn  = Date.valueOf(checkIn);
                Date fechaCheckOut = Date.valueOf(checkOut);

                if (repository.existeTraslape(item.getHabitacionId(), fechaCheckIn, fechaCheckOut))
                    throw new IllegalArgumentException(
                            "La habitacion " + item.getHabitacionId() +
                                    " no esta disponible para las fechas seleccionadas");

                double[] precios = repository.obtenerPrecios(item.getHabitacionId());

                double precioPorNoche   = precios[0];
                double precioPorPersona = precios[1];
                int    capacidadMaxima  = precios.length > 2 ? (int) precios[2] : Integer.MAX_VALUE;
                int    personasExtra    = Math.max(0, item.getCantidadPersonas() - capacidadMaxima);

                double totalHab = Math.round(
                        ((dias * precioPorNoche) + (personasExtra * dias * precioPorPersona)) * 100.0
                ) / 100.0;

                totalGeneral += totalHab;

                HabitacionAgenciaResponseDTO habDTO = new HabitacionAgenciaResponseDTO();
                habDTO.setHabitacionId(item.getHabitacionId());
                habDTO.setPrecioPorNoche(precioPorNoche);
                habDTO.setPrecioPorPersona(precioPorPersona);
                habDTO.setPersonasExtra(personasExtra);
                habDTO.setNoches((int) dias);
                habDTO.setTotal(totalHab);
                desglose.add(habDTO);
            }

            totalGeneral = Math.round(totalGeneral * 100.0) / 100.0;

            String noReservacion      = "MIKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Timestamp fechaCreacion   = Timestamp.valueOf(LocalDateTime.now());
            Timestamp fechaExpiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));

            int reservacionId = repository.crearReservacion(
                    noReservacion, totalGeneral, usuarioWebisId, fechaCreacion, fechaExpiracion
            );

            for (int i = 0; i < desglose.size(); i++) {
                HabitacionAgenciaResponseDTO hab = desglose.get(i);
                HabitacionReservaRequestDTO item = request.getHabitaciones().get(i);
                int detalleId = repository.crearDetalle(
                        reservacionId,
                        hab.getHabitacionId(),
                        Date.valueOf(LocalDate.parse(item.getFechaCheckIn())),
                        Date.valueOf(LocalDate.parse(item.getFechaCheckOut())),
                        item.getCantidadPersonas(),
                        hab.getTotal()
                );
                hab.setDetalleId(detalleId);
            }

            Object[] datos = repository.obtenerReservacion(reservacionId);

            ReservacionAgenciaResponseDTO response = new ReservacionAgenciaResponseDTO();
            response.setId((int) datos[0]);
            response.setNoReservacion((String) datos[1]);
            response.setTotal((double) datos[2]);
            response.setFechaCreacion((String) datos[3]);
            response.setFechaExpiracion((String) datos[4]);
            response.setEstado((String) datos[5]);
            response.setHabitaciones(desglose);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_AGENCIA_EXITOSA,
                    response.getId(),
                    null,
                    agenciaId,
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
                    LogReservacionRepository.TIPO_AGENCIA_FALLIDA,
                    null,
                    null,
                    agenciaId,
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
                    LogReservacionRepository.TIPO_AGENCIA_ERROR,
                    null,
                    null,
                    agenciaId,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw new RuntimeException("Error interno al crear reservacion de agencia", e);
        }
    }

    /**
     * Retorna todas las reservaciones asociadas a una agencia.
     * @param agenciaId ID de la agencia.
     * @return lista de reservaciones con sus detalles.
     */
    public List<ReservacionDetalleDTO> obtenerReservaciones(int agenciaId) {
        return repository.obtenerReservacionesDeAgencia(agenciaId);
    }

    /**
     * Expira manualmente una reservacion pendiente de una agencia.
     * Solo aplica si la reservacion pertenece a la agencia y esta en estado Pendiente.
     * @param reservacionId ID de la reservacion a expirar.
     * @param agenciaId     ID de la agencia duena de la reservacion.
     * @throws IllegalArgumentException si la reservacion no existe, no pertenece a la agencia
     *                                  o no esta en estado pendiente.
     */
    public void expirarReservacion(int reservacionId, int agenciaId, String ip, String userAgent) {
        boolean valida = repository.perteneceAAgenciaYEstaPendiente(reservacionId, agenciaId);

        if (!valida) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_AGENCIA_FALLIDA,
                    reservacionId,
                    null,
                    agenciaId,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    "Intento de expiracion invalido: reservacion no encontrada o no pendiente"
            );
            throw new IllegalArgumentException(
                    "La reservacion no existe, no pertenece a esta agencia, o no esta en estado pendiente");
        }

        repository.expirarReservacion(reservacionId);

        logReservacionRepository.registrar(
                LogReservacionRepository.TIPO_AGENCIA_EXPIRADA,
                reservacionId,
                null,
                agenciaId,
                null,
                null,
                true,
                ip,
                userAgent,
                null
        );
    }

    /**
     * Retorna el detalle completo de una reservacion de agencia con imagenes incluidas.
     * @param reservacionId ID de la reservacion.
     * @param agenciaId     ID de la agencia duena de la reservacion.
     * @return lista de DTOs con detalles de habitaciones, hotel e imagenes.
     * @throws IllegalArgumentException si la reservacion no existe o no pertenece a la agencia.
     */
    public List<ReservacionDetalleDTO> obtenerDetalleReservacion(int reservacionId, int agenciaId) {
        List<ReservacionDetalleDTO> detalles = repository.obtenerDetalleReservacionAgencia(reservacionId, agenciaId);

        if (detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("Reservacion no encontrada o no pertenece a esta agencia");

        // Carga las imagenes del hotel y la habitacion para cada detalle
        for (ReservacionDetalleDTO dto : detalles) {
            dto.setImagenesHotelIds(repository.obtenerImagenesHotel(dto.getHotelId()));
            dto.setImagenesHabitacionIds(repository.obtenerImagenesHabitacion(dto.getHabitacionId()));
        }

        return detalles;
    }



    /**
     * Cambia el rango de fechas de multiples detalles de una reservacion de agencia de forma atomica.
     * Cada detalle debe mantener exactamente la misma duracion original (solo se mueve el rango).
     * El total no se recalcula. Si alguna validacion falla, nada se persiste.
     *
     * @param reservacionId ID de la reservacion padre.
     * @param cambios       lista de cambios con detalleId, fechaCheckIn y fechaCheckOut.
     * @param agenciaId     ID de la agencia autenticada.
     */
    public ReservacionAgenciaResponseDTO cambiarFechasMultiple(
            int reservacionId,
            List<org.example.dtos.CambioFechasMultipleRequestDTO.CambioDetalle> cambios,
            int agenciaId, String ip, String userAgent) {
        try {
            if (cambios == null || cambios.isEmpty()) {
                throw new IllegalArgumentException("Debes incluir al menos un cambio de fechas");
            }

            List<Object[]> persistir = new ArrayList<>();

            for (org.example.dtos.CambioFechasMultipleRequestDTO.CambioDetalle cambio : cambios) {

                Object[] detalle = repository.obtenerDetalleDeAgencia(cambio.getDetalleId(), agenciaId);
                if (detalle == null) {
                    throw new IllegalArgumentException(
                            "El detalle " + cambio.getDetalleId() +
                                    " no existe o no pertenece a esta agencia"
                    );
                }

                String estado         = (String) detalle[7];
                int    habitacionId   = (int)    detalle[2];
                int    resIdDelDetalle= (int)    detalle[1];
                double totalActual    = (double) detalle[6]; // total original — no cambia

                if (resIdDelDetalle != reservacionId) {
                    throw new IllegalArgumentException(
                            "El detalle " + cambio.getDetalleId() +
                                    " no pertenece a la reservacion " + reservacionId
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

                if (repository.existeTraslapeExcluyendoDetalle(
                        habitacionId, fechaCheckIn, fechaCheckOut, cambio.getDetalleId())) {
                    throw new IllegalArgumentException(
                            "La habitación " + habitacionId +
                                    " no está disponible del " + cambio.getFechaCheckIn() +
                                    " al " + cambio.getFechaCheckOut()
                    );
                }

                persistir.add(new Object[]{ fechaCheckIn, fechaCheckOut, totalActual, cambio.getDetalleId() });
            }

            repository.actualizarFechasDetallesAtomico(persistir);

            Object[] datos = repository.obtenerReservacion(reservacionId);

            ReservacionAgenciaResponseDTO response = new ReservacionAgenciaResponseDTO();
            response.setId((int)    datos[0]);
            response.setNoReservacion((String) datos[1]);
            response.setTotal((double)  datos[2]);
            response.setFechaCreacion((String)  datos[3]);
            response.setFechaExpiracion((String) datos[4]);
            response.setEstado((String) datos[5]);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_AGENCIA_EXITOSO,
                    response.getId(), null, agenciaId,
                    response.getNoReservacion(), response.getTotal(),
                    true, ip, userAgent,
                    "Cambio atomico (misma duracion) de " + cambios.size() + " detalle(s)"
            );
            return response;

        } catch (IllegalArgumentException e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_AGENCIA_FALLIDO,
                    null, null, agenciaId, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_CAMBIO_FECHAS_AGENCIA_ERROR_INTERNO,
                    null, null, agenciaId, null, null,
                    false, ip, userAgent, e.getMessage()
            );
            throw new RuntimeException("Error interno al cambiar fechas de agencia", e);
        }
    }
}