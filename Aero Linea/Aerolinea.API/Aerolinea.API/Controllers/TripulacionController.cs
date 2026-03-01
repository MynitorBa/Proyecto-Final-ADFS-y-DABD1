using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
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

        // Público: necesario para cargar listas en formularios del panel
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

        [HttpGet("roles")]
        public async Task<IActionResult> ObtenerRoles()
        {
            var roles = await _service.ObtenerRoles();
            return Ok(roles);
        }

        // Solo administradores: operaciones de escritura
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> Crear([FromBody] CrearTripulanteDTO crearTripulanteDTO)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var tripulante = await _service.Crear(crearTripulanteDTO);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = tripulante.Id }, tripulante);
        }

        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] CrearTripulanteDTO actualizarTripulanteDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var resultado = await _service.Actualizar(id, actualizarTripulanteDto);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante actualizado correctamente" });
        }

        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> Eliminar(int id)
        {
            var resultado = await _service.Eliminar(id);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante eliminado correctamente" });
        }

        // ===== ENDPOINTS DE IMAGEN =====

        /// POST api/tripulacion/{id}/imagen — sube o reemplaza la imagen del tripulante
        [Authorize(Roles = "Administrador")]
        [HttpPost("{id}/imagen")]
        public async Task<IActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
        {
            if (string.IsNullOrEmpty(dto.ImagenBase64))
                return BadRequest(new { message = "La imagen no puede estar vacía" });

            var tripulante = await _service.ObtenerPorId(id);
            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            await _service.GuardarImagen(id, dto.ImagenBase64);
            return Ok(new { message = "Imagen guardada correctamente" });
        }

        /// DELETE api/tripulacion/{id}/imagen — elimina la imagen del tripulante
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}/imagen")]
        public async Task<IActionResult> EliminarImagen(int id)
        {
            var tripulante = await _service.ObtenerPorId(id);
            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            await _service.EliminarImagen(id);
            return Ok(new { message = "Imagen eliminada correctamente" });
        }
    }
}