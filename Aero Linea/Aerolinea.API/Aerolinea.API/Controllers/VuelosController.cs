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

        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloDTO dto)
        {
            // Si hay sesión activa obtenemos el id, si no queda null (búsqueda anónima)
            int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);

            var vuelos = await _service.BuscarVuelos(dto, usuarioId);
            return Ok(vuelos);
        }
    }
}