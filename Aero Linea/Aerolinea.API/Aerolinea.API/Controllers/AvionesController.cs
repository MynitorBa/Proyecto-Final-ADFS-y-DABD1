using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
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

        // Público: necesario para cargar listas en formularios del panel
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

        // Solo administradores: operaciones de escritura
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<ActionResult<AvionDTO>> Crear([FromBody] CrearAvionDTO crearAvionDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var avionCreado = await _avionService.Crear(crearAvionDto);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = avionCreado.Id }, avionCreado);
        }

        [Authorize(Roles = "Administrador")]
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

        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<ActionResult> Eliminar(int id)
        {
            var eliminado = await _avionService.Eliminar(id);

            if (!eliminado)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(new { message = "Avión eliminado correctamente" });
        }

        // ===== ENDPOINTS DE IMAGEN =====

        /// POST api/aviones/{id}/imagen — sube o reemplaza la imagen del avión
        [Authorize(Roles = "Administrador")]
        [HttpPost("{id}/imagen")]
        public async Task<ActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
        {
            if (string.IsNullOrEmpty(dto.ImagenBase64))
                return BadRequest(new { message = "La imagen no puede estar vacía" });

            var avion = await _avionService.ObtenerPorId(id);
            if (avion == null)
                return NotFound(new { message = "Avión no encontrado" });

            await _avionService.GuardarImagen(id, dto.ImagenBase64);
            return Ok(new { message = "Imagen guardada correctamente" });
        }

        /// DELETE api/aviones/{id}/imagen — elimina la imagen del avión
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}/imagen")]
        public async Task<ActionResult> EliminarImagen(int id)
        {
            var avion = await _avionService.ObtenerPorId(id);
            if (avion == null)
                return NotFound(new { message = "Avión no encontrado" });

            await _avionService.EliminarImagen(id);
            return Ok(new { message = "Imagen eliminada correctamente" });
        }
    }
}