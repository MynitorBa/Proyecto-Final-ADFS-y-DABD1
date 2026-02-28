using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones")]
    public class FacturaController : ControllerBase
    {
        private readonly FacturaService _service;

        public FacturaController(FacturaService service)
        {
            _service = service;
        }

        // POST api/reservaciones/{id}/comprar
        [HttpPost("{id}/comprar")]
        [Authorize]
        public async Task<IActionResult> ComprarReservacion(int id, [FromBody] ComprarReservacionDTO dto)
        {
            try
            {
                int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);
                if (usuarioId == null)
                    return Unauthorized(new { message = "Debes iniciar sesión para realizar una compra." });

                var resultado = await _service.ComprarReservacion(id, usuarioId.Value, dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}