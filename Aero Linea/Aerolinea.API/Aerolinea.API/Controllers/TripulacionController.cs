using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/tripulacion")]
    public class TripulacionController : ControllerBase
    {
        private readonly TripulacionService _service;

        public TripulacionController(TripulacionService service)
        {
            _service = service;
        }

        [HttpGet]
        public async Task<IActionResult> ObtenerTodos()
        {
            var tripulantes = await _service.ObtenerTodos();
            return Ok(tripulantes);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var tripulante = await _service.ObtenerPorId(id);

            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(tripulante);
        }
    }
}