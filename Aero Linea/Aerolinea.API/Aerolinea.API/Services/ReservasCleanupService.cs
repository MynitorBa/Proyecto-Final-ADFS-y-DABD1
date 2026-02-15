using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class ReservasCleanupService : BackgroundService
    {
        private readonly IServiceProvider _serviceProvider;
        private readonly ILogger<ReservasCleanupService> _logger;

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

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    await LiberarReservasExpiradas();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error al liberar reservas expiradas");
                }

                // Esperar 1 minuto antes de la siguiente ejecución
                await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);
            }

            _logger.LogInformation("Servicio de limpieza de reservas detenido.");
        }

        private async Task LiberarReservasExpiradas()
        {
            using var scope = _serviceProvider.CreateScope();
            var repository = scope.ServiceProvider.GetRequiredService<ReservacionRepository>();

            int reservasLiberadas = await repository.LiberarReservasExpiradas();

            if (reservasLiberadas > 0)
            {
                _logger.LogInformation($"Se liberaron {reservasLiberadas} reservas expiradas.");
            }
        }
    }
}