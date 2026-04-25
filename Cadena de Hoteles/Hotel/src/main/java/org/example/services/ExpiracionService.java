package org.example.services;

import org.example.repositories.ReservacionRepository;

import org.example.repositories.LogReservacionRepository;


import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio en segundo plano que expira reservaciones pendientes vencidas.
 * Corre en un hilo separado cada minuto desde que arranca el servidor.
 */
public class ExpiracionService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ReservacionRepository reservacionRepository;
    private final LogReservacionRepository logReservacionRepository;

    /**
     * Crea una instancia de ExpiracionService con sus dependencias inyectadas.
     */
    public ExpiracionService(ReservacionRepository reservacionRepository,
                             LogReservacionRepository logReservacionRepository) {
        this.reservacionRepository    = reservacionRepository;
        this.logReservacionRepository = logReservacionRepository;
    }

    /**
     * Arranca el hilo programado que revisa y expira reservaciones cada minuto.
     */
    public void iniciar() {
        scheduler.scheduleAtFixedRate(this::expirarReservaciones, 1, 1, TimeUnit.MINUTES);
        System.out.println("[ExpiracionService] Hilo de expiracion iniciado - revisa cada 1 minuto.");
    }

    /**
     * Ejecuta la expiracion de reservaciones vencidas en cada ciclo del scheduler.
     * Por cada reservacion expirada registra un log automatico sin IP ni UserAgent
     * ya que la accion la ejecuta el servidor, no un cliente.
     */
    private void expirarReservaciones() {
        try {
            List<Integer> ids = reservacionRepository.expirarReservacionesVencidas();
            if (!ids.isEmpty()) {
                System.out.println("[ExpiracionService] " + ids.size() + " reservacion(es) expiradas.");
                for (int reservacionId : ids) {
                    logReservacionRepository.registrar(
                            LogReservacionRepository.TIPO_RESERVACION_EXPIRADA_AUTO,
                            reservacionId,
                            null,
                            null,
                            null,
                            null,
                            true,
                            null,
                            null,
                            "Expirada automaticamente por el scheduler"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[ExpiracionService] Error al expirar reservaciones: " + e.getMessage());
        }
    }

    /**
     * Detiene el hilo del scheduler al apagar el servidor.
     */
    public void detener() {
        scheduler.shutdown();
        System.out.println("[ExpiracionService] Hilo de expiracion detenido.");
    }
}