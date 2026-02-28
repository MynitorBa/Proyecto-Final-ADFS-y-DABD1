using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api")]
    public class PdfController : ControllerBase
    {
        private readonly GestionReservacionRepository _reservacionRepo;

        public PdfController(GestionReservacionRepository reservacionRepo)
        {
            _reservacionRepo = reservacionRepo;
        }

        // ══════════════════════════════════════════════════════════════════
        //  GET /api/reservaciones/{id}/comprobante
        //  Devuelve HTML formateado que el usuario imprime como PDF
        //  El frontend abre esto en una pestaña nueva: window.open(url)
        // ══════════════════════════════════════════════════════════════════
        [HttpGet("reservaciones/{id}/comprobante")]
        [Authorize]
        public async Task<IActionResult> ObtenerComprobante(int id)
        {
            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            var rolId = SessionHelper.GetRolId(HttpContext);

            if (usuarioId == null)
                return Unauthorized(new { mensaje = "No autenticado" });

            if (rolId != 1 && rolId != 2)
                return StatusCode(403, new { mensaje = "Acceso denegado" });

            try
            {
                var reservacion = await _reservacionRepo.ObtenerReservacionPorId(id, usuarioId.Value);

                if (reservacion == null)
                    return NotFound(new { mensaje = "Reservación no encontrada o no tienes acceso" });

                string html = PdfHtmlHelper.GenerarComprobante(reservacion);

                return Content(html, "text/html; charset=utf-8");
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al generar comprobante: " + ex.Message });
            }
        }
    }
}