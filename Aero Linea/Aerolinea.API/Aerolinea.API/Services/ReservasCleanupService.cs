using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio en segundo plano para el mantenimiento automatico de reservaciones y vuelos.
    /// Se ejecuta como BackgroundService y realiza tres tareas periodicas: liberar reservas
    /// expiradas, completar reservaciones cuyo vuelo ya paso, y actualizar el estado de los
    /// vuelos (en transcurso y finalizados) cada 30 ciclos de ejecucion (30 minutos).
    /// </summary>
    public class ReservasCleanupService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<ReservasCleanupService> _logger;

        private int _ciclos = 0;
        private const int CiclosParaActualizarVuelos = 30; // cada 30 min

        /// <summary>
        /// Inicializa el servicio con el proveedor de servicios y el logger de la aplicacion.
        /// </summary>
        public ReservasCleanupService(
            IServiceProvider serviceProvider,
            ILogger<ReservasCleanupService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

        /// <summary>
        /// Ejecuta el servicio en segundo plano de forma continua.
        /// Libera reservas expiradas, completa reservaciones cuyo vuelo ya paso
        /// y actualiza el estado de los vuelos cada 30 ciclos (30 minutos).
        /// </summary>
        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("Servicio de limpieza de reservas iniciado.");

            await LiberarReservasExpiradas();
            await ActualizarEstadosVuelos();
            await CompletarReservaciones();

            while (!stoppingToken.IsCancellationRequested)
            {
                await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);
                _ciclos++;

                try { await LiberarReservasExpiradas(); }
                catch (Exception ex) { _logger.LogError(ex, "Error al liberar reservas expiradas."); }

                try { await CompletarReservaciones(); }
                catch (Exception ex) { _logger.LogError(ex, "Error al completar reservaciones."); }

                if (_ciclos >= CiclosParaActualizarVuelos)
                {
                    try { await ActualizarEstadosVuelos(); }
                    catch (Exception ex) { _logger.LogError(ex, "Error al actualizar estados de vuelos."); }

                    _ciclos = 0;
                }
            }

            _logger.LogInformation("Servicio de limpieza de reservas detenido.");
        }

        /// <summary>
        /// Crea un scope de dependencias y llama al repositorio para liberar todas las
        /// reservas cuyo tiempo de espera ha expirado, devolviendo asientos al inventario.
        /// </summary>
        private async Task LiberarReservasExpiradas()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<ReservacionRepository>();
            int reservasLiberadas = await repository.LiberarReservasExpiradas();

            if (reservasLiberadas > 0)
                _logger.LogInformation("Se liberaron {Count} reservas expiradas.", reservasLiberadas);
        }

        /// <summary>
        /// Crea un scope de dependencias y llama al repositorio para marcar como completadas
        /// todas las reservaciones asociadas a vuelos que ya han concluido.
        /// </summary>
        private async Task CompletarReservaciones()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<ReservacionRepository>();
            int completadas = await repository.CompletarReservaciones();

            if (completadas > 0)
                _logger.LogInformation("{Count} reservación(es) pasaron a estado Completada.", completadas);
        }

        /// <summary>
        /// Crea un scope de dependencias y llama al repositorio interno de vuelos para
        /// actualizar los estados de vuelos que deben pasar a En Transcurso o Finalizado
        /// segun la hora actual comparada con su horario programado.
        /// </summary>
        private async Task ActualizarEstadosVuelos()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<VueloAdminInternoRepository>();
            var (enTranscurso, finalizados) = await repository.ActualizarEstadosVuelos();

            if (enTranscurso > 0)
                _logger.LogInformation("{Count} vuelo(s) pasaron a estado En Transcurso.", enTranscurso);
            if (finalizados > 0)
                _logger.LogInformation("{Count} vuelo(s) pasaron a estado Finalizado.", finalizados);
        }
    }
}
