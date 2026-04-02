package org.example.services;

import org.example.repositories.CancelacionRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.example.dtos.PuedeCancelarDTO;

public class CancelacionService {

    private final CancelacionRepository cancelacionRepository = new CancelacionRepository();

    public void cancelarReservacion(int reservacionId, int usuarioId, String motivoCancelacion) {

        // Verificar que la reservación existe y pertenece al usuario
        Object[] reservacion = cancelacionRepository.obtenerReservacionParaCancelar(reservacionId, usuarioId);
        if (reservacion == null) {
            throw new IllegalArgumentException("Reservación no encontrada o no pertenece al usuario");
        }

        // Verificar estado válido para cancelar (Pendiente=1 o Confirmada=2)
        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];
        if (estadoId != 1 && estadoId != 2) {
            throw new IllegalArgumentException(
                    "La reservación no puede cancelarse, estado actual: " + estado
            );
        }

        // Obtener check-in más próximo
        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null) {
            throw new IllegalArgumentException("La reservación no tiene habitaciones asociadas");
        }

        // Verificar más de 24 horas de anticipación
        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );
        if (horasRestantes < 24 && estadoId != 1) {
            throw new IllegalArgumentException(
                    "No se puede cancelar con menos de 24 horas de anticipación al check-in"
            );
        }

        cancelacionRepository.cancelarReservacion(reservacionId, motivoCancelacion);
    }


    // Verificar usando agenciaId en lugar de usuarioId
    public PuedeCancelarDTO puedeCancelar(int reservacionId, int agenciaId) {
        Object[] reservacion = cancelacionRepository.obtenerReservacionAgenciaParaCancelar(reservacionId, agenciaId);
        if (reservacion == null)
            return new PuedeCancelarDTO(false, "Reservación no encontrada o no pertenece a esta agencia");

        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];

        if (estadoId != 1 && estadoId != 2)
            return new PuedeCancelarDTO(false, "Estado actual no permite cancelación: " + estado);

        if (estadoId == 1)
            return new PuedeCancelarDTO(true, "Reservación pendiente, puede cancelarse");

        // Confirmada — validar 24hrs antes del check-in
        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null)
            return new PuedeCancelarDTO(false, "La reservación no tiene habitaciones asociadas");

        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );

        if (horasRestantes < 24)
            return new PuedeCancelarDTO(false,
                    "No se puede cancelar con menos de 24 horas de anticipación al check-in");

        return new PuedeCancelarDTO(true,
                "Puede cancelarse. Faltan " + horasRestantes + " horas para el check-in");
    }

    public void cancelarReservacionAgencia(int reservacionId, int agenciaId, String motivo) {
        Object[] reservacion = cancelacionRepository.obtenerReservacionAgenciaParaCancelar(reservacionId, agenciaId);
        if (reservacion == null)
            throw new IllegalArgumentException("Reservación no encontrada o no pertenece a esta agencia");

        int estadoId  = (int) reservacion[1];
        String estado = (String) reservacion[2];

        if (estadoId != 1 && estadoId != 2)
            throw new IllegalArgumentException("La reservación no puede cancelarse, estado actual: " + estado);

        Date fechaCheckIn = cancelacionRepository.obtenerFechaCheckInMasReciente(reservacionId);
        if (fechaCheckIn == null)
            throw new IllegalArgumentException("La reservación no tiene habitaciones asociadas");

        long horasRestantes = ChronoUnit.HOURS.between(
                LocalDate.now().atStartOfDay(),
                fechaCheckIn.toLocalDate().atStartOfDay()
        );
        if (horasRestantes < 24 && estadoId != 1)
            throw new IllegalArgumentException(
                    "No se puede cancelar con menos de 24 horas de anticipación al check-in");

        cancelacionRepository.cancelarReservacion(reservacionId, motivo);
    }
}