using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de facturacion. Gestiona la logica de negocio para procesar la compra
    /// de una reservacion, validando datos fiscales y el formato de tarjeta de pago
    /// antes de generar la factura y emitir los boletos.
    /// Tras una compra exitosa genera el comprobante en PDF y lo envia por correo.
    /// </summary>
    public class FacturaService
    {
        private readonly FacturaRepository             _repository;
        private readonly PdfService                    _pdfService;
        private readonly EmailHelper                   _emailHelper;
        private readonly ILogger<FacturaService>       _logger;

        /// <summary>
        /// Inicializa el servicio con el repositorio de facturas, el generador de PDF,
        /// el helper de correo y el logger.
        /// </summary>
        public FacturaService(
            FacturaRepository       repository,
            PdfService              pdfService,
            EmailHelper             emailHelper,
            ILogger<FacturaService> logger)
        {
            _repository  = repository;
            _pdfService  = pdfService;
            _emailHelper = emailHelper;
            _logger      = logger;
        }

        /// <summary>
        /// Procesa la compra de una reservacion existente. Valida que el NIT y codigo postal
        /// esten presentes, verifica el formato de la tarjeta de pago mediante TarjetaHelper
        /// (sin almacenar datos de la tarjeta) y delega la creacion de factura y boletos al repositorio.
        /// Al finalizar exitosamente genera el comprobante PDF y lo adjunta al correo de confirmacion
        /// (best-effort: si el envio falla se registra en el log pero NO se revierte la compra).
        /// </summary>
        public async Task<CompraRealizadaDTO> ComprarReservacion(
            int reservacionId,
            int usuarioId,
            ComprarReservacionDTO dto)
        {
            // ── Validar datos de factura ──────────────────────────────────────
            if (string.IsNullOrWhiteSpace(dto.NIT))
                throw new Exception("El NIT es requerido. Si no tienes, ingresa 'CF'.");

            if (string.IsNullOrWhiteSpace(dto.CodigoPostal))
                throw new Exception("El codigo postal es requerido.");

            // ── Validar formato de tarjeta (nunca se guarda) ─────────────────
            TarjetaHelper.ValidarFormato(
                dto.NumeroTarjeta,
                dto.NombreTitular,
                dto.FechaExpiracion,
                dto.CVV);

            // ── Ejecutar la compra en el repositorio (transaccional) ──────────
            var resultado = await _repository.ComprarReservacion(reservacionId, usuarioId, dto);

            // ── Enviar correo con comprobante PDF adjunto (best-effort) ───────
            // Un fallo en el correo nunca revierte la compra ya confirmada.
            // El usuario puede solicitar el comprobante manualmente desde Mis Reservaciones.
            if (!string.IsNullOrEmpty(resultado.UsuarioEmail))
            {
                try
                {
                    _logger.LogInformation(
                        "Enviando correo de confirmacion de compra para reservacion {NoReservacion} a {Email}",
                        resultado.NoReservacion, resultado.UsuarioEmail);

                    string html = EmailTemplates.CorreoConfirmacion(
                        resultado.UsuarioNombre,
                        resultado.NoReservacion,
                        resultado.Total);

                    string asunto = $"Broom AirLine - Reservacion {resultado.NoReservacion} Confirmada";

                    // Intentar adjuntar el comprobante en PDF
                    byte[] pdf = _pdfService.GenerarComprobante(resultado);

                    if (pdf.Length > 0)
                    {
                        await _emailHelper.EnviarConAdjunto(
                            resultado.UsuarioEmail,
                            asunto,
                            html,
                            pdf,
                            $"comprobante-{resultado.NoReservacion}.pdf");
                    }
                    else
                    {
                        // PDF no disponible: enviar correo sin adjunto
                        await _emailHelper.Enviar(resultado.UsuarioEmail, asunto, html);
                    }

                    _logger.LogInformation(
                        "Correo de confirmacion enviado exitosamente para reservacion {NoReservacion}",
                        resultado.NoReservacion);
                }
                catch (Exception ex)
                {
                    // El fallo del correo nunca debe bloquear ni revertir la compra
                    _logger.LogError(ex,
                        "Error al enviar correo de confirmacion para reservacion {NoReservacion} a {Email}",
                        resultado.NoReservacion, resultado.UsuarioEmail);
                }
            }
            else
            {
                _logger.LogWarning(
                    "No se pudo enviar correo de confirmacion: email vacio para reservacion {NoReservacion}",
                    resultado.NoReservacion);
            }

            return resultado;
        }
    }
}
