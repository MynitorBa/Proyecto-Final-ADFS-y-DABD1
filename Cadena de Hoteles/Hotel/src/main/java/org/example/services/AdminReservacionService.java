package org.example.services;

import org.example.repositories.AdminReservacionRepository;

import java.util.List;
import java.util.Map;

public class AdminReservacionService {

    private final AdminReservacionRepository repo = new AdminReservacionRepository();

    // ════════════════════════════════════════════════════
    //  Listar todas las reservaciones
    // ════════════════════════════════════════════════════

    public List<Map<String, Object>> listarTodas() {
        return repo.listarTodas();
    }

    // ════════════════════════════════════════════════════
    //  Cancelar reservación (admin)
    //  Solo cancela si está en Pendiente (1) o Confirmada (2)
    // ════════════════════════════════════════════════════

    public void cancelarReservacion(int reservacionId, String motivo) {
        Object[] datos = repo.obtenerReservacion(reservacionId);
        if (datos == null) {
            throw new IllegalArgumentException("Reservación #" + reservacionId + " no encontrada");
        }

        int    estadoId = (int)    datos[1];
        String estado   = (String) datos[2];

        if (estadoId != 1 && estadoId != 2) {
            throw new IllegalArgumentException(
                    "No se puede cancelar: estado actual es \"" + estado + "\""
            );
        }

        repo.cancelarReservacion(reservacionId, motivo);
    }
}