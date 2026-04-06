using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de reservaciones del usuario autenticado. Permite consultar el listado
    /// y detalle de sus reservaciones, descargar o enviar comprobantes, obtener un resumen
    /// estadistico y cancelar reservaciones activas. Todos los endpoints requieren sesion activa.
    /// </summary>
    [ApiController]
    [Route("api/mis-reservaciones")]
    [Authorize] // Todas las rutas requieren sesión
    public class MisReservacionesController : ControllerBase
    {
        private readonly GestionReservacionService _service;
        private readonly PdfService _pdfService;

        /// <summary>
        /// Inicializa el controlador con el servicio de gestion de reservaciones y el servicio PDF.
        /// </summary>
        public MisReservacionesController(GestionReservacionService service, PdfService pdfService)
        {
            _service = service;
            _pdfService = pdfService;
        }

        // GET api/mis-reservaciones
        /// <summary>
        /// Retorna el listado de todas las reservaciones del usuario autenticado,
        /// incluyendo estado, vuelos y monto total de cada una.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerMisReservaciones()
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var reservaciones = await _service.ObtenerMisReservaciones(usuarioId);
                return Ok(reservaciones);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/mis-reservaciones/{reservacionId}
        /// <summary>
        /// Retorna el detalle completo de una reservacion especifica del usuario autenticado,
        /// incluyendo boletos, pasajeros, vuelos y datos de facturacion.
        /// </summary>
        [HttpGet("{reservacionId}")]
        public async Task<IActionResult> ObtenerDetalleReservacion(int reservacionId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var reservacion = await _service.ObtenerDetalleReservacion(reservacionId, usuarioId);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/mis-reservaciones/{reservacionId}/comprobante
        /// <summary>
        /// Genera y descarga el comprobante de una reservacion en formato PDF.
        /// El archivo se nombra con el numero de reservacion para facilitar su identificacion.
        /// </summary>
        [HttpGet("{reservacionId}/comprobante")]
        public async Task<IActionResult> DescargarComprobante(int reservacionId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var reservacion = await _service.ObtenerDetalleReservacion(reservacionId, usuarioId);
                string html = PdfHtmlHelper.GenerarComprobante(reservacion);
                byte[] pdf = _pdfService.GenerarPdf(html);
                return File(pdf, "application/pdf", $"comprobante-{reservacion.NoReservacion}.pdf");
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // GET api/mis-reservaciones/resumen
        /// <summary>
        /// Retorna un resumen estadistico de las reservaciones del usuario autenticado,
        /// como totales por estado, monto gastado y proximos vuelos.
        /// </summary>
        [HttpGet("resumen")]
        public async Task<IActionResult> ObtenerResumen()
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var resumen = await _service.ObtenerResumen(usuarioId);
                return Ok(resumen);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST api/mis-reservaciones/{reservacionId}/cancelar
        // Body: { "motivo": "Cambio de planes" }  (opcional)
        /// <summary>
        /// Cancela una reservacion activa del usuario autenticado. El motivo de cancelacion
        /// es opcional. Solo se pueden cancelar reservaciones que aun no hayan sido completadas.
        /// </summary>
        [HttpPost("{reservacionId}/cancelar")]
        public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                await _service.CancelarReservacion(reservacionId, usuarioId, dto?.Motivo);
                return Ok(new { message = "Reservación cancelada exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST api/mis-reservaciones/{reservacionId}/enviar-comprobante
        /// <summary>
        /// Envia el comprobante de una reservacion al correo electronico registrado del usuario.
        /// Genera el PDF en memoria y lo adjunta al correo antes de enviarlo.
        /// </summary>
        [HttpPost("{reservacionId}/enviar-comprobante")]
        public async Task<IActionResult> EnviarComprobanteEmail(int reservacionId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                await _service.EnviarComprobanteEmail(reservacionId, usuarioId);
                return Ok(new { message = "Comprobante enviado exitosamente al correo registrado." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        private int ObtenerUsuarioId()
        {
            int? id = SessionHelper.GetUsuarioId(HttpContext);
            if (id == null)
                throw new Exception("No se pudo obtener la sesión del usuario.");
            return id.Value;
        }
    }
}
