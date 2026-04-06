package org.example.services;

import org.example.repositories.ReservacionRepository;

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

    /**
     * Crea una instancia de ExpiracionService con sus dependencias inyectadas.
     */
    public ExpiracionService(ReservacionRepository reservacionRepository) {
        this.reservacionRepository = reservacionRepository;
    }

    /**
     * Arranca el hilo programado que revisa y expira reservaciones cada minuto.
     * El primer ciclo inicia un minuto despues de llamar a este metodo.
     */
    public void iniciar() {
        scheduler.scheduleAtFixedRate(this::expirarReservaciones, 1, 1, TimeUnit.MINUTES);
        System.out.println("[ExpiracionService] Hilo de expiracion iniciado - revisa cada 1 minuto.");
    }

    /**
     * Ejecuta la expiracion de reservaciones vencidas en cada ciclo del scheduler.
     * Registra en consola cuantas reservaciones fueron expiradas, si las hay.
     * Los errores se capturan para que el hilo no se detenga por una falla puntual.
     */
    private void expirarReservaciones() {
        try {
            int expiradas = reservacionRepository.expirarReservacionesVencidas();
            if (expiradas > 0) {
                System.out.println("[ExpiracionService] " + expiradas + " reservacion(es) expiradas.");
            }
        } catch (Exception e) {
            System.err.println("[ExpiracionService] Error al expirar reservaciones: " + e.getMessage());
        }
    }

    /**
     * Detiene el hilo del scheduler al apagar el servidor.
     * Se llama desde el ShutdownHook registrado en Main.
     */
    public void detener() {
        scheduler.shutdown();
        System.out.println("[ExpiracionService] Hilo de expiracion detenido.");
    }
}