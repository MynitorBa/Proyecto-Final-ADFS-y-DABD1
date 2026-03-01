using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/vuelos")]
    public class VuelosController : ControllerBase
    {
        private readonly VueloService _service;

        public VuelosController(VueloService service)
        {
            _service = service;
        }

        // POST api/vuelos/buscar
        // Devuelve { directos: [...], conEscala: [...] }
        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloDTO dto)
        {
            try
            {
                int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);
                var resultado = await _service.BuscarVuelos(dto, usuarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}