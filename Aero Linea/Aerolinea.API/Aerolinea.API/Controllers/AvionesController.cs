using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de aviones. Expone endpoints REST para consultar, crear, actualizar, eliminar
    /// aviones y gestionar sus imagenes. Los endpoints de lectura son publicos; los de escritura
    /// requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public class AvionesController : ControllerBase
    {
        private readonly IAvionService _avionService;

        /// <summary>
        /// Inicializa el controlador con el servicio de aviones.
        /// </summary>
        public AvionesController(IAvionService avionService)
        {
            _avionService = avionService;
        }

        // Público: necesario para cargar listas en formularios del panel
        /// <summary>
        /// Retorna la lista completa de aviones registrados. Endpoint publico, utilizado
        /// para poblar selectores en el formulario de creacion de vuelos del panel de admin.
        /// </summary>
        [HttpGet]
        public async Task<ActionResult<List<AvionDTO>>> ObtenerTodos()
        {
            var aviones = await _avionService.ObtenerTodos();
            return Ok(aviones);
        }

        /// <summary>
        /// Retorna los datos de un avion especifico por su identificador.
        /// Devuelve 404 si el avion no existe.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<ActionResult<AvionDTO>> ObtenerPorId(int id)
        {
            var avion = await _avionService.ObtenerPorId(id);

            if (avion == null)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(avion);
        }

        // Solo administradores: operaciones de escritura
        /// <summary>
        /// Crea un nuevo avion con los datos del DTO. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<ActionResult<AvionDTO>> Crear([FromBody] CrearAvionDTO crearAvionDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var avionCreado = await _avionService.Crear(crearAvionDto);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = avionCreado.Id }, avionCreado);
        }

        /// <summary>
        /// Actualiza los datos de un avion existente. Requiere rol Administrador.
        /// Retorna 404 si el avion no existe.
        /// </summary>
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

        /// <summary>
        /// Elimina un avion por su identificador. Requiere rol Administrador.
        /// Retorna 404 si el avion no existe.
        /// </summary>
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

        /// <summary>
        /// Sube o reemplaza la imagen de un avion enviada como cadena Base64.
        /// Requiere rol Administrador. Retorna 404 si el avion no existe.
        /// </summary>
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

        /// <summary>
        /// Elimina la imagen asociada a un avion. Requiere rol Administrador.
        /// Retorna 404 si el avion no existe.
        /// </summary>
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
