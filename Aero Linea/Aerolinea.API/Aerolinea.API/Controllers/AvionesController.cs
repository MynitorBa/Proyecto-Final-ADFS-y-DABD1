using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AvionesController : ControllerBase
    {
        private readonly AvionService _avionService;

        public AvionesController(AvionService avionService)
        {
            _avionService = avionService;
        }

        [HttpGet]
        public async Task<ActionResult<List<AvionDTO>>> ObtenerTodos()
        {
            var aviones = await _avionService.ObtenerTodos();
            return Ok(aviones);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<AvionDTO>> ObtenerPorId(int id)
        {
            var avion = await _avionService.ObtenerPorId(id);

            if (avion == null)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(avion);
        }

        [HttpPost]
        public async Task<ActionResult<AvionDTO>> Crear([FromBody] CrearAvionDTO crearAvionDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var avionCreado = await _avionService.Crear(crearAvionDto);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = avionCreado.Id }, avionCreado);
        }

        [HttpPut("{id}")]
        public async Task<ActionResult> Actualizar(int id, [FromBody] CrearAvionDTO actualizarAvionDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var actualizado = await _avionService.Actualizar(id, actualizarAvionDto);

            if (!actualizado)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(new { message = "Avión actualizado correctamente" });
        }

        
    }
}