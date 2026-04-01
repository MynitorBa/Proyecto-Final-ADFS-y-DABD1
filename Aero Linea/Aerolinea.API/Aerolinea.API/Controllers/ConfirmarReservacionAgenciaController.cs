using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class ConfirmarReservacionAgenciaController : ControllerBase
    {
        private readonly ConfirmarReservacionAgenciaService _service;

        public ConfirmarReservacionAgenciaController(ConfirmarReservacionAgenciaService service)
        {
            _service = service;
        }

        // POST api/reservaciones-agencia/{id}/confirmar
        [HttpPost("{id}/confirmar")]
        public async Task<IActionResult> ConfirmarReservacion(
            int id,
            [FromBody] ConfirmarReservacionAgenciaDTO dto)
        {
            try
            {
                var agencia = HttpContext.Items["agencia_id"];
                if (agencia == null)
                    return Unauthorized(new { message = "Token de agencia requerido." });

                int agenciaId = (int)agencia;
                var resultado = await _service.ConfirmarReservacion(id, agenciaId, dto);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}