using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/vuelos-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class VueloAgenciaController : ControllerBase
    {
        private readonly VueloAgenciaService _service;

        public VueloAgenciaController(VueloAgenciaService service)
        {
            _service = service;
        }

        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloAgenciaDTO dto)
        {
            try
            {
                int agenciaId = (int)HttpContext.Items["agencia_id"]!;
                var resultado = await _service.BuscarVuelos(dto, agenciaId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}