using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using static Aerolinea.API.Repositories.AdminReservacionesRepository;
using static Aerolinea.API.Services.AgenciaNotificadorExternoService;

namespace Aerolinea.API.Services
{
    public class AdminReservacionesService
    {
        private readonly AdminReservacionesRepository       _repo;
        private readonly AgenciaNotificadorExternoService   _notificadorAgencia;
        private readonly EmailHelper                        _emailHelper;
        private readonly ILogger<AdminReservacionesService> _logger;
        private readonly LogReservacionRepository           _logRepo;

        public AdminReservacionesService(
            AdminReservacionesRepository       repo,
            AgenciaNotificadorExternoService   notificadorAgencia,
            EmailHelper                        emailHelper,
            ILogger<AdminReservacionesService> logger,
            LogReservacionRepository           logRepo)
        {
            _repo               = repo;
            _notificadorAgencia = notificadorAgencia;
            _emailHelper        = emailHelper;
            _logger             = logger;
            _logRepo            = logRepo;
        }

        public Task<List<VueloResumenDto>> ObtenerVuelosConReservacionesAsync()
            => _repo.ObtenerVuelosConReservacionesAsync();

        public async Task EditarDatosPasajeroAsync(int boletoId, EditarDatosPasajeroDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Nombre))
                throw new ArgumentException("El nombre es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Apellido))
                throw new ArgumentException("El apellido es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Pasaporte))
                throw new ArgumentException("El pasaporte es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Telefono))
                throw new ArgumentException("El teléfono es obligatorio.");

            await _repo.EditarDatosPasajeroAsync(boletoId, dto);
        }

        public Task<List<ReservacionResumenDto>> ObtenerPorVueloAsync(int vueloId)
            => _repo.ObtenerPorVueloAsync(vueloId);

        public Task<List<ReservacionResumenDto>> ObtenerTodasAsync()
            => _repo.ObtenerTodasAsync();

        public Task<ReservacionResumenDto?> ObtenerPorIdAsync(int id)
            => _repo.ObtenerPorIdAsync(id);

        public async Task<(bool Ok, string Mensaje, ResultadoNotificacion? Agencia)> CancelarAsync(
            int reservacionId, string motivo, string? ip = null, string? userAgent = null)
        {
            if (string.IsNullOrWhiteSpace(motivo))
                return (false, "El motivo de cancelacion es obligatorio.", null);

            var usuario = await _repo.ObtenerUsuarioPorReservacionAsync(reservacionId);
            if (usuario == null)
            {
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminFallida,
                    reservacionId, null, null, null, false, ip, userAgent,
                    "Reservacion no encontrada");
                return (false, "La reservacion no fue encontrada.", null);
            }

            var detalle = await _repo.ObtenerPorIdAsync(reservacionId);
            if (detalle == null)
            {
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminFallida,
                    reservacionId, null, null, null, false, ip, userAgent,
                    "Detalle de reservacion no encontrado");
                return (false, "La reservacion no fue encontrada.", null);
            }

            try
            {
                // 1. Notificar a la agencia PRIMERO (antes de cancelar en nuestra BD)
                var resultadoAgencia = await _notificadorAgencia.NotificarCancelacionAsync(
                    reservacionId, motivo.Trim());

                // 2. Cancelar en nuestra BD
                var cancelado = await _repo.CancelarAsync(reservacionId, motivo.Trim());
                if (!cancelado)
                {
                    await _logRepo.Registrar(
                        LogReservacionRepository.TipoCancelacionAdminFallida,
                        reservacionId, null, null, null, false, ip, userAgent,
                        "La reservacion ya esta cancelada o no existe");
                    return (false, "La reservacion ya esta cancelada o no existe.", resultadoAgencia);
                }

                // 3. Log de exito
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminExitosa,
                    reservacionId, null, null, null, true, ip, userAgent,
                    motivo.Trim());

                // 4. Correo al usuario (best-effort)
                try
                {
                    await _emailHelper.Enviar(
                        usuario.Email,
                        $"Broom AirLine \u2013 Reservacion {detalle.NoReservacion} Cancelada",
                        ConstruirCorreoCancelacion(usuario, detalle, motivo.Trim()));
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error correo cancelacion reservacion {Id}", reservacionId);
                }

                return (true, "Reservacion cancelada exitosamente.", resultadoAgencia);
            }
            catch (Exception ex)
            {
                await _logRepo.Registrar(
                    LogReservacionRepository.TipoCancelacionAdminFallida,
                    reservacionId, null, null, null, false, ip, userAgent,
                    ex.Message);
                throw;
            }
        }
        // ObtenerVuelosElegiblesAsync() — REMOVIDO
        // FUNCIONALIDAD DESHABILITADA: No se permite cambiar de vuelo una vez creada la reserva

        // CambiarVueloAsync() — REMOVIDO
        // FUNCIONALIDAD DESHABILITADA: No se permite cambiar de vuelo una vez creada la reserva

        private static string ConstruirCorreoCancelacion(
            UsuarioEmailDto usuario, ReservacionResumenDto detalle, string motivo)
        {
            var primerBoleto = detalle.Boletos.FirstOrDefault();
            var ruta = primerBoleto != null
                ? $"{primerBoleto.OrigenCodigo} \u2192 {primerBoleto.DestinoCodigo}"
                : "N/A";

            return $@"<!DOCTYPE html>
<html lang=""es"">
<head><meta charset=""UTF-8""><title>Cancelacion</title></head>
<body style=""margin:0;padding:0;background:#F5F1EB;font-family:'Segoe UI',sans-serif;"">
  <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#F5F1EB;padding:40px 0"">
    <tr><td align=""center"">
      <table width=""600"" cellpadding=""0"" cellspacing=""0"" style=""background:#fff;border-radius:4px 24px 4px 24px;box-shadow:0 8px 32px rgba(28,26,24,.10);overflow:hidden"">
        <tr><td style=""background:#1C1A18;padding:28px 40px;text-align:center"">
          <h1 style=""margin:0;font-size:22px;font-weight:300;color:#fff;letter-spacing:2px"">BROOM <span style=""color:#D4A056;font-weight:700"">AIRLINE</span></h1>
        </td></tr>
        <tr><td style=""padding:28px 40px"">
          <p style=""margin:0 0 20px;color:#1C1A18;font-size:15px"">Hola, <strong>{usuario.Nombre}</strong>:</p>
          <table width=""100%"" cellpadding=""6"" cellspacing=""0"" style=""background:#F5F1EB;border-radius:8px;margin-bottom:20px"">
            <tr><td style=""font-size:12px;color:#7f8c8d"">N° Reservacion</td><td style=""font-size:14px;font-weight:700;color:#8B6B4A;font-family:monospace"">{detalle.NoReservacion}</td></tr>
            <tr><td style=""font-size:12px;color:#7f8c8d"">Ruta</td><td style=""font-size:14px;font-weight:600"">{ruta}</td></tr>
            <tr><td style=""font-size:12px;color:#7f8c8d"">Total</td><td style=""font-size:14px;font-weight:700"">${detalle.Total:F2}</td></tr>
          </table>
          <div style=""background:#fdedec;border-left:4px solid #c0392b;padding:14px 18px;margin-bottom:20px"">
            <p style=""margin:0 0 4px;font-size:12px;color:#c0392b;font-weight:700"">Motivo</p>
            <p style=""margin:0;font-size:14px"">{motivo}</p>
          </div>
        </td></tr>
        <tr><td style=""background:#1C1A18;padding:18px 40px;text-align:center"">
          <p style=""margin:0;font-size:11px;color:#888"">&copy; {DateTime.Now.Year} Broom AirLine</p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>";
        }
    }
}