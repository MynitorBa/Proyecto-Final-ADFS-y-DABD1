using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using static Aerolinea.API.Repositories.AdminReservacionesRepository;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de logica de negocio para la gestion administrativa de reservaciones.
    /// Coordina el repositorio con el envio de correos al cancelar y expone
    /// la vista agrupada por vuelo para el panel de administracion.
    /// </summary>
    public class AdminReservacionesService
    {
        private readonly AdminReservacionesRepository _repo;
        private readonly ILogger<AdminReservacionesService> _logger;

        public AdminReservacionesService(
            AdminReservacionesRepository repo,
            ILogger<AdminReservacionesService> logger)
        {
            _repo = repo;
            _logger = logger;
        }

        // ── Vuelos agrupados ──────────────────────────────────────────────────────────

        /// <summary>
        /// Retorna todos los vuelos que tienen al menos una reservacion, con conteos
        /// por estado. Punto de entrada de la vista agrupada del panel admin.
        /// </summary>
        public Task<List<VueloResumenDto>> ObtenerVuelosConReservacionesAsync()
            => _repo.ObtenerVuelosConReservacionesAsync();

        /// <summary>
        /// Retorna las reservaciones que tienen boletos para el vuelo indicado.
        /// </summary>
        public Task<List<ReservacionResumenDto>> ObtenerPorVueloAsync(int vueloId)
            => _repo.ObtenerPorVueloAsync(vueloId);

        // ── Listado y detalle ─────────────────────────────────────────────────────────

        public Task<List<ReservacionResumenDto>> ObtenerTodasAsync()
            => _repo.ObtenerTodasAsync();

        public Task<ReservacionResumenDto?> ObtenerPorIdAsync(int id)
            => _repo.ObtenerPorIdAsync(id);

        // ── Cancelacion ───────────────────────────────────────────────────────────────

        /// <summary>
        /// Cancela una reservacion administrativamente:
        /// cambia estado a Cancelada (3), cancela boletos, devuelve disponibilidad
        /// en BoletosTurista/BoletosEjecutivo del vuelo y notifica al usuario por correo.
        /// </summary>
        public async Task<(bool Ok, string Mensaje)> CancelarAsync(int reservacionId, string motivo)
        {
            if (string.IsNullOrWhiteSpace(motivo))
                return (false, "El motivo de cancelacion es obligatorio.");

            var usuario = await _repo.ObtenerUsuarioPorReservacionAsync(reservacionId);
            if (usuario == null)
                return (false, "La reservacion no fue encontrada.");

            var detalle = await _repo.ObtenerPorIdAsync(reservacionId);
            if (detalle == null)
                return (false, "La reservacion no fue encontrada.");

            var cancelado = await _repo.CancelarAsync(reservacionId, motivo.Trim());
            if (!cancelado)
                return (false, "La reservacion ya esta cancelada o no existe.");

            try
            {
                string asunto = $"Broom AirLine \u2013 Reservacion {detalle.NoReservacion} Cancelada";
                string cuerpo = ConstruirCorreoCancelacion(usuario, detalle, motivo.Trim());
                await EmailHelper.Enviar(usuario.Email, asunto, cuerpo);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex,
                    "Error al enviar correo de cancelacion admin para reservacion {Id}", reservacionId);
            }

            return (true, "Reservacion cancelada exitosamente. Se notifico al usuario por correo.");
        }

        // ── Correo HTML ───────────────────────────────────────────────────────────────

        private static string ConstruirCorreoCancelacion(
            UsuarioEmailDto usuario,
            ReservacionResumenDto detalle,
            string motivo)
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
        <tr><td style=""padding:32px 40px 0;text-align:center"">
          <h2 style=""margin:16px 0 6px;font-size:20px;font-weight:700;color:#c0392b"">Reservacion Cancelada</h2>
          <p style=""margin:0;color:#7f8c8d;font-size:14px"">Tu reservacion fue cancelada por el equipo de administracion.</p>
        </td></tr>
        <tr><td style=""padding:28px 40px"">
          <p style=""margin:0 0 20px;color:#1C1A18;font-size:15px"">Hola, <strong>{usuario.Nombre}</strong>:</p>
          <table width=""100%"" cellpadding=""6"" cellspacing=""0"" style=""background:#F5F1EB;border-radius:8px;margin-bottom:20px"">
            <tr><td style=""font-size:12px;color:#7f8c8d;text-transform:uppercase"">N&deg; Reservacion</td><td style=""font-size:14px;font-weight:700;color:#8B6B4A;font-family:monospace"">{detalle.NoReservacion}</td></tr>
            <tr><td style=""font-size:12px;color:#7f8c8d;text-transform:uppercase"">Ruta</td><td style=""font-size:14px;font-weight:600;color:#1C1A18"">{ruta}</td></tr>
            <tr><td style=""font-size:12px;color:#7f8c8d;text-transform:uppercase"">Total</td><td style=""font-size:14px;font-weight:700;color:#1C1A18"">${detalle.Total:F2}</td></tr>
          </table>
          <div style=""background:#fdedec;border-left:4px solid #c0392b;border-radius:0 8px 8px 0;padding:14px 18px;margin-bottom:20px"">
            <p style=""margin:0 0 4px;font-size:12px;text-transform:uppercase;color:#c0392b;font-weight:700"">Motivo</p>
            <p style=""margin:0;font-size:14px;color:#4a4a4a"">{motivo}</p>
          </div>
          <div style=""background:#eafaf1;border-left:4px solid #27ae60;border-radius:0 8px 8px 0;padding:12px 18px;margin-bottom:20px"">
            <p style=""margin:0;font-size:13px;color:#1C1A18"">Los asientos de tu reservacion han sido liberados.<br>Puedes buscar nueva disponibilidad en nuestra plataforma.</p>
          </div>
          <p style=""margin:0;color:#4a4a4a;font-size:13px"">Atentamente,<br><strong>Equipo de Administracion &middot; Broom AirLine</strong></p>
        </td></tr>
        <tr><td style=""background:#1C1A18;padding:18px 40px;text-align:center"">
          <p style=""margin:0;font-size:11px;color:#888"">&copy; {DateTime.Now.Year} Broom AirLine &middot; Correo automatico.</p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>";
        }
    }
}