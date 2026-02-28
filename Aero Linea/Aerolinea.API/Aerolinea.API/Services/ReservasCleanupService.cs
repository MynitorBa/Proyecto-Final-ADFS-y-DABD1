using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ReservasCleanupService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<ReservasCleanupService> _logger;

        private int _ciclos = 0;
        private const int CiclosParaActualizarVuelos = 30; // cada 30 min

        public ReservasCleanupService(
            IServiceProvider serviceProvider,
            ILogger<ReservasCleanupService> logger)
        {
            _serviceProvider = serviceProvider;
            _logger = logger;
        }

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

        private async Task LiberarReservasExpiradas()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<ReservacionRepository>();
            int reservasLiberadas = await repository.LiberarReservasExpiradas();

            if (reservasLiberadas > 0)
                _logger.LogInformation("Se liberaron {Count} reservas expiradas.", reservasLiberadas);
        }

        private async Task CompletarReservaciones()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<ReservacionRepository>();
            int completadas = await repository.CompletarReservaciones();

            if (completadas > 0)
                _logger.LogInformation("{Count} reservación(es) pasaron a estado Completada.", completadas);
        }

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