using Aerolinea.API.DTOs.Agencia;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/asientos-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class AsientoAgenciaController : ControllerBase
    {
        private readonly AsientoAgenciaService _service;

        public AsientoAgenciaController(AsientoAgenciaService service)
        {
            _service = service;
        }

        [HttpGet("reservacion/{reservacionId}")]
        public async Task<IActionResult> ObtenerAsientosPorReservacion(int reservacionId)
        {
            try
            {
                int agenciaId = ObtenerAgenciaId();
                var result = await _service.ObtenerAsientosPorReservacion(reservacionId, agenciaId);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPut("{boletoId}")]
        public async Task<IActionResult> CambiarAsiento(int boletoId, [FromBody] CambiarAsientoAgenciaRequestDTO dto)
        {
            try
            {
                int agenciaId = ObtenerAgenciaId();
                await _service.CambiarAsiento(boletoId, dto.NuevoAsiento, agenciaId);
                return Ok(new { message = "Asiento actualizado exitosamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        private int ObtenerAgenciaId()
        {
            var agencia = HttpContext.Items["agencia_id"];
            if (agencia == null) throw new Exception("No autorizado.");
            return (int)agencia;
        }
    }
}