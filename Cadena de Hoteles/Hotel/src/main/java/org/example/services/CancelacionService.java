package org.example.services;

import org.example.repositories.CancelacionRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        if (horasRestantes < 24) {
            throw new IllegalArgumentException(
                    "No se puede cancelar con menos de 24 horas de anticipación al check-in"
            );
        }

        cancelacionRepository.cancelarReservacion(reservacionId, motivoCancelacion);
    }
}