package org.example.services;

import org.example.repositories.ReservacionRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiracionService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ReservacionRepository reservacionRepository = new ReservacionRepository();

    public void iniciar() {
        // Corre cada 1 minuto, empieza al minuto de arrancar el servidor
        scheduler.scheduleAtFixedRate(this::expirarReservaciones, 1, 1, TimeUnit.MINUTES);
        System.out.println("[ExpiracionService] Hilo de expiración iniciado — revisa cada 1 minuto.");
    }

    private void expirarReservaciones() {
        try {
            int expiradas = reservacionRepository.expirarReservacionesVencidas();
            if (expiradas > 0) {
                System.out.println("[ExpiracionService] " + expiradas + " reservación(es) expiradas.");
            }
        } catch (Exception e) {
            System.err.println("[ExpiracionService] Error al expirar reservaciones: " + e.getMessage());
        }
    }

    public void detener() {
        scheduler.shutdown();
        System.out.println("[ExpiracionService] Hilo de expiración detenido.");
    }
}