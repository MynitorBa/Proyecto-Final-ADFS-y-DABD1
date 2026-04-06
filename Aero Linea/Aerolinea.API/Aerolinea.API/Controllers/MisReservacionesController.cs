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
    [Authorize]
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
        /// Genera y retorna el comprobante de una reservacion como HTML para que el usuario
        /// lo abra en una nueva pestana e imprima como PDF desde el navegador.
        /// No requiere la libreria nativa wkhtmltopdf.
        /// </summary>
        [HttpGet("{reservacionId}/comprobante")]
        public async Task<IActionResult> DescargarComprobante(int reservacionId)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                var reservacion = await _service.ObtenerDetalleReservacion(reservacionId, usuarioId);
                string html = PdfHtmlHelper.GenerarComprobante(reservacion);
                return Content(html, "text/html; charset=utf-8");
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
        /// es opcional. Solo se pueden cancelar reservaciones pendientes o confirmadas.
        /// </summary>
        [HttpPost("{reservacionId}/cancelar")]
        public async Task<IActionResult> CancelarReservacion(int reservacionId, [FromBody] CancelarReservacionDTO dto)
        {
            try
            {
                int usuarioId = ObtenerUsuarioId();
                await _service.CancelarReservacion(reservacionId, usuarioId, dto?.Motivo);
                return Ok(new { message = "Reservacion cancelada exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST api/mis-reservaciones/{reservacionId}/enviar-comprobante
        /// <summary>
        /// Envia el comprobante de una reservacion al correo electronico registrado del usuario.
        /// Genera el HTML del comprobante y lo adjunta al correo antes de enviarlo.
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
                throw new Exception("No se pudo obtener la sesion del usuario.");
            return id.Value;
        }
    }
}