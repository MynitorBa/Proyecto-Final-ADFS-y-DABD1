using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de generacion de comprobantes en HTML para impresion como PDF.
    /// Genera el HTML del comprobante de una reservacion y lo retorna como contenido
    /// de texto para que el frontend lo abra en una nueva pestana e imprima desde el navegador.
    /// </summary>
    [ApiController]
    [Route("api")]
    public class PdfController : ControllerBase
    {
        private readonly GestionReservacionRepository _reservacionRepo;

        /// <summary>
        /// Inicializa el controlador con el repositorio de reservaciones necesario para
        /// obtener los datos del comprobante.
        /// </summary>
        public PdfController(GestionReservacionRepository reservacionRepo)
        {
            _reservacionRepo = reservacionRepo;
        }

        //  GET /api/reservaciones/{id}/comprobante
        //  Devuelve HTML formateado que el usuario imprime como PDF
        //  El frontend abre esto en una pestaña nueva: window.open(url)

        /// <summary>
        /// Retorna el HTML formateado del comprobante de una reservacion para que el usuario
        /// lo imprima como PDF desde el navegador. Solo accesible para roles Administrador (1)
        /// y Cliente (2). Verifica que la reservacion pertenezca al usuario autenticado.
        /// </summary>
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
