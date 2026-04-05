package org.example.services;

import org.example.repositories.AdminReservacionRepository;

import java.util.List;
import java.util.Map;

/**
 * Service para la gestion de reservaciones desde el panel de administracion.
 * Permite listar todas las reservaciones y cancelarlas con validacion de estado.
 */
public class AdminReservacionService {

    private final AdminReservacionRepository repo = new AdminReservacionRepository();

    /**
     * Retorna todas las reservaciones registradas en el sistema.
     * @return lista de mapas con los datos de cada reservacion.
     */
    public List<Map<String, Object>> listarTodas() {
        return repo.listarTodas();
    }

    /**
     * Cancela una reservacion si su estado actual lo permite.
     * Solo se pueden cancelar reservaciones en estado Pendiente (1) o Confirmada (2).
     * @param reservacionId ID de la reservacion a cancelar.
     * @param motivo        razon de la cancelacion.
     * @throws IllegalArgumentException si la reservacion no existe o su estado no permite cancelacion.
     */
    public void cancelarReservacion(int reservacionId, String motivo) {
        Object[] datos = repo.obtenerReservacion(reservacionId);
        if (datos == null) {
            throw new IllegalArgumentException("Reservacion #" + reservacionId + " no encontrada");
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