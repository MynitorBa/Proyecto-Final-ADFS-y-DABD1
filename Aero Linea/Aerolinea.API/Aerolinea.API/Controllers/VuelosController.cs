using Aerolinea.API.DTOs;
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

        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloDTO dto)
        {
            var vuelos = await _service.BuscarVuelos(dto);
            return Ok(vuelos);
        }
    }
}