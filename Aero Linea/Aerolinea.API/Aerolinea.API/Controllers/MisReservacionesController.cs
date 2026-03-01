using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/mis-reservaciones")]
    [Authorize] // Todas las rutas requieren sesión
    public class MisReservacionesController : ControllerBase
    {
        private readonly GestionReservacionService _service;
        private readonly PdfService _pdfService;


        public MisReservacionesController(GestionReservacionService service, PdfService pdfService)
        {
            _service = service;
            _pdfService = pdfService;
        }

        // GET api/mis-reservaciones
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

        private int ObtenerUsuarioId()
        {
            int? id = SessionHelper.GetUsuarioId(HttpContext);
            if (id == null)
                throw new Exception("No se pudo obtener la sesión del usuario.");
            return id.Value;
        }
    }
}