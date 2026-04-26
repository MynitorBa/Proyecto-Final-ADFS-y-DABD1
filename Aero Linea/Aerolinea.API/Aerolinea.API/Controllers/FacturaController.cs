using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de facturacion y compra de reservaciones. Expone el endpoint que procesa
    /// el pago de una reservacion pendiente, genera la factura y confirma los boletos del usuario.
    /// </summary>
    [ApiController]
    [Route("api/reservaciones")]
    public class FacturaController : ControllerBase
    {
        private readonly FacturaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de facturacion.
        /// </summary>
        public FacturaController(FacturaService service)
        {
            _service = service;
        }

        // POST api/reservaciones/{id}/comprar
        /// <summary>
        /// Procesa la compra de una reservacion pendiente validando el metodo de pago y generando
        /// la factura correspondiente. Requiere sesion activa. Solo el propietario de la reservacion
        /// puede realizar la compra.
        /// </summary>
        [HttpPost("{id}/comprar")]
        [Authorize]
        public async Task<IActionResult> ComprarReservacion(int id, [FromBody] ComprarReservacionDTO dto)
        {
            string? ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            string? userAgent = Request.Headers["User-Agent"].ToString();
            try
            {
                int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);
                if (usuarioId == null)
                    return Unauthorized(new { message = "Debes iniciar sesión para realizar una compra." });

                var resultado = await _service.ComprarReservacion(id, usuarioId.Value, dto, ip, userAgent);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
