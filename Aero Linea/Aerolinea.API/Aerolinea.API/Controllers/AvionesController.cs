using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/aviones")]
    public class AvionesController : ControllerBase
    {
        private readonly AvionService _service;

        public AvionesController(AvionService service)
        {
            _service = service;
        }

        [HttpGet]
        public async Task<IActionResult> ObtenerTodos()
        {
            var aviones = await _service.ObtenerTodos();
            return Ok(aviones);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var avion = await _service.ObtenerPorId(id);

            if (avion == null)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(avion);
        }
    }
}