using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/rutas")]
    public class RutasController : ControllerBase
    {
        private readonly RutaService _service;

        public RutasController(RutaService service)
        {
            _service = service;
        }

        // GET /api/rutas — lista todas las rutas (solo administradores)
        [Authorize(Roles = "Administrador")]
        [HttpGet]
        public async Task<IActionResult> ObtenerTodas()
        {
            var rutas = await _service.ObtenerTodas();
            return Ok(rutas);
        }

        // PUT /api/rutas/{id}/duracion — actualiza duración en minutos
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/duracion")]
        public async Task<IActionResult> ActualizarDuracion(int id, [FromBody] EditarDuracionRutaDTO dto)
        {
            try
            {
                var resultado = await _service.ActualizarDuracion(id, dto.DuracionEstimada);
                if (!resultado)
                    return NotFound(new { message = "Ruta no encontrada" });

                return Ok(new { message = "Duración actualizada correctamente" });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // POST /api/rutas/calcular-llegada — calcula hora de llegada con zonas horarias
        // Usado por el formulario de creación de vuelo para mostrar preview
        [Authorize(Roles = "Administrador")]
        [HttpPost("calcular-llegada")]
        public async Task<IActionResult> CalcularLlegada([FromBody] CalculoLlegadaRequestDTO request)
        {
            try
            {
                var resultado = await _service.CalcularLlegada(request);
                return Ok(resultado);
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
        // POST /api/rutas — crea una ruta manualmente
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> CrearRuta([FromBody] CrearRutaDTO dto)
        {
            var (creada, rutaId, mensaje) = await _service.CrearRuta(
                dto.OrigenId, dto.DestinoId, dto.DuracionEstimada);

            if (!creada)
                return BadRequest(new { message = mensaje });

            return Ok(new { id = rutaId, message = mensaje });
        }

        // GET /api/rutas/existe?origenId=1&destinoId=2 — verifica si existe una ruta
        [Authorize(Roles = "Administrador")]
        [HttpGet("existe")]
        public async Task<IActionResult> ExisteRuta(
            [FromQuery] int origenId, [FromQuery] int destinoId)
        {
            var existe = await _service.ExisteRuta(origenId, destinoId);
            return Ok(new { existe });
        }

    }
}