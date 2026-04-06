package org.example.services;

import org.example.repositories.CancelacionRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.example.dtos.PuedeCancelarDTO;

/**
 * Service para cancelacion de reservaciones.
 * Maneja cancelaciones de usuarios web y de agencias,
 * validando estado y la regla de las 24 horas antes del check-in.
 */
public class CancelacionService {

    private final CancelacionRepository cancelacionRepository;

    /**
     * Crea una instancia de CancelacionService con sus dependencias inyectadas.
     */
    public CancelacionService(CancelacionRepository cancelacionRepository) {
        this.cancelacionRepository = cancelacionRepository;
    }

    /**
     * Cancela una reservacion de un usuario web.
     * Verifica que la reservacion exista, pertenezca al usuario,
     * tenga un estado valido y cumpla la regla de las 24 horas.
     * @param reservacionId      ID de la reservacion a cancelar.
     * @param usuarioId          ID del usuario dueno de la reservacion.
     * @param motivoCancelacion  razon de la cancelacion.
     * @throws IllegalArgumentException si la reservacion no existe, el estado no lo permite
     *                                  o faltan menos de 24 horas para el check-in.
     */
    public void cancelarReservacion(int reservacionId, int usuarioId, String motivoCancelacion) {

        // Verifica que la reservacion existe y pertenece al usuario
        Object[] reservacion = cancelacionRepository.obtenerReservacionParaCancelar(reservacionId, usuarioId);
        if (reservacion == null) {
            throw new IllegalArgumentException("Reservacion no encontrada o no pertenece al usuario");
        }

        // Solo se puede cancelar si esta Pendiente (1) o Confirmada (2)
        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];
        if (estadoId != 1 && estadoId != 2) {
            throw new IllegalArgumentException(
                    "La reservacion no puede cancelarse, estado actual: " + estado
            );
        }

        // Obtiene la fecha de check-in mas proxima para validar el plazo
        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null) {
            throw new IllegalArgumentException("La reservacion no tiene habitaciones asociadas");
        }

        // Regla: reservaciones confirmadas requieren mas de 24 horas de anticipacion
        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );
        if (horasRestantes < 24 && estadoId != 1) {
            throw new IllegalArgumentException(
                    "No se puede cancelar con menos de 24 horas de anticipacion al check-in"
            );
        }

        cancelacionRepository.cancelarReservacion(reservacionId, motivoCancelacion);
    }

    /**
     * Verifica si una reservacion de agencia puede cancelarse sin ejecutar la cancelacion.
     * Util para que el frontend consulte antes de mostrar el boton de cancelar.
     * @param reservacionId ID de la reservacion a evaluar.
     * @param agenciaId     ID de la agencia duena de la reservacion.
     * @return DTO con el resultado (puede o no cancelar) y un mensaje explicativo.
     */
    public PuedeCancelarDTO puedeCancelar(int reservacionId, int agenciaId) {
        Object[] reservacion = cancelacionRepository.obtenerReservacionAgenciaParaCancelar(reservacionId, agenciaId);
        if (reservacion == null)
            return new PuedeCancelarDTO(false, "Reservacion no encontrada o no pertenece a esta agencia");

        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];

        if (estadoId != 1 && estadoId != 2)
            return new PuedeCancelarDTO(false, "Estado actual no permite cancelacion: " + estado);

        // Pendiente siempre puede cancelarse sin restriccion de tiempo
        if (estadoId == 1)
            return new PuedeCancelarDTO(true, "Reservacion pendiente, puede cancelarse");

        // Confirmada: valida que falten mas de 24 horas para el check-in
        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null)
            return new PuedeCancelarDTO(false, "La reservacion no tiene habitaciones asociadas");

        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );

        if (horasRestantes < 24)
            return new PuedeCancelarDTO(false,
                    "No se puede cancelar con menos de 24 horas de anticipacion al check-in");

        return new PuedeCancelarDTO(true,
                "Puede cancelarse. Faltan " + horasRestantes + " horas para el check-in");
    }

    /**
     * Cancela una reservacion perteneciente a una agencia.
     * Aplica las mismas validaciones de estado y plazo que cancelarReservacion.
     * @param reservacionId ID de la reservacion a cancelar.
     * @param agenciaId     ID de la agencia duena de la reservacion.
     * @param motivo        razon de la cancelacion.
     * @throws IllegalArgumentException si la reservacion no existe, el estado no lo permite
     *                                  o faltan menos de 24 horas para el check-in.
     */
    public void cancelarReservacionAgencia(int reservacionId, int agenciaId, String motivo) {
        Object[] reservacion = cancelacionRepository.obtenerReservacionAgenciaParaCancelar(reservacionId, agenciaId);
        if (reservacion == null)
            throw new IllegalArgumentException("Reservacion no encontrada o no pertenece a esta agencia");

        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];

        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("La reservacion no puede cancelarse, estado actual: " + estado);

        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null)
            throw new IllegalArgumentException("La reservacion no tiene habitaciones asociadas");

        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );
        if (horasRestantes < 24 && estadoId != 1)
            throw new IllegalArgumentException(
                    "No se puede cancelar con menos de 24 horas de anticipacion al check-in");

        cancelacionRepository.cancelarReservacion(reservacionId, motivo);
    }
}