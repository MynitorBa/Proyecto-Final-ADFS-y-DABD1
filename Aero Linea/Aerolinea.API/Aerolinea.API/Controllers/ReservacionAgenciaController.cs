using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/reservaciones-agencia")]
    public class ReservacionAgenciaController : ControllerBase
    {
        private readonly ReservacionAgenciaService _service;
        private readonly AgenciaAuthMiddleware _agenciaAuth;

        public ReservacionAgenciaController(
            ReservacionAgenciaService service,
            AgenciaAuthMiddleware agenciaAuth)
        {
            _service = service;
            _agenciaAuth = agenciaAuth;
        }

        [HttpPost]
        public async Task<IActionResult> CrearReservacion([FromBody] CrearReservacionDTO dto)
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null)
                return Unauthorized(new { message = "Token de agencia requerido" });

            int agenciaId = (int)agencia;

            try
            {
                var reservacion = await _service.CrearReservacion(dto, agenciaId);
                return Ok(reservacion);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}