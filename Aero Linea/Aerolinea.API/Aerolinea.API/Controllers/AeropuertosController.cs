using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/aeropuertos")]
    public class AeropuertosController : ControllerBase
    {
        private readonly AeropuertoService _service;
        private readonly ILogger<AeropuertosController> _logger;

        public AeropuertosController(AeropuertoService service, ILogger<AeropuertosController> logger)
        {
            _service = service;
            _logger = logger;
        }

        // Público: usado en búsqueda de vuelos por usuarios no autenticados
        [HttpGet]
        public async Task<IActionResult> ObtenerAeropuertos()
        {
            var aeropuertos = await _service.ObtenerAeropuertos();
            return Ok(aeropuertos);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var aeropuerto = await _service.ObtenerPorId(id);
            if (aeropuerto == null)
                return NotFound(new { message = "Aeropuerto no encontrado" });
            return Ok(aeropuerto);
        }

        // GET /api/aeropuertos/fechas-disponibles
        [HttpGet("fechas-disponibles")]
        public async Task<IActionResult> ObtenerFechasDisponibles(
            [FromQuery] int? origenId,
            [FromQuery] int? destinoId,
            [FromQuery] int cantidadPersonas = 1,
            [FromQuery] int? claseId = null)
        {
            var fechas = await _service.ObtenerFechasDisponiblesPorRuta(
                origenId, destinoId, cantidadPersonas, claseId);
            return Ok(fechas);
        }

        // Solo administradores: operaciones de escritura
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> Crear([FromBody] CrearAeropuertoDTO crearAeropuertoDTO)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            _logger.LogInformation("=== CREAR AEROPUERTO: Nombre={Nombre}, Codigo={Codigo}, Ciudad={Ciudad}, Pais={Pais}, ZonaHoraria={ZonaHoraria}",
                crearAeropuertoDTO.Nombre, crearAeropuertoDTO.Codigo,
                crearAeropuertoDTO.Ciudad, crearAeropuertoDTO.Pais,
                crearAeropuertoDTO.ZonaHoraria ?? "(null)");

            try
            {
                var aeropuerto = await _service.Crear(crearAeropuertoDTO);
                _logger.LogInformation("=== AEROPUERTO CREADO OK: ID={Id}", aeropuerto?.Id);
                return CreatedAtAction(nameof(ObtenerPorId), new { id = aeropuerto!.Id }, aeropuerto);
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "=== ERROR AL CREAR AEROPUERTO: {Message} | InnerException: {Inner}",
                    ex.Message, ex.InnerException?.Message ?? "(none)");
                return StatusCode(500, new { message = ex.Message, detalle = ex.InnerException?.Message });
            }
        }

        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}")]
        public async Task<IActionResult> Actualizar(int id, [FromBody] CrearAeropuertoDTO actualizarAeropuertoDto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            _logger.LogInformation("=== ACTUALIZAR AEROPUERTO ID={Id}, ZonaHoraria={ZonaHoraria}",
                id, actualizarAeropuertoDto.ZonaHoraria ?? "(null)");

            try
            {
                var resultado = await _service.Actualizar(id, actualizarAeropuertoDto);
                if (!resultado)
                    return NotFound(new { message = "Aeropuerto no encontrado" });

                return Ok(new { message = "Aeropuerto actualizado correctamente" });
            }
            catch (InvalidOperationException ex)
            {
                return Conflict(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "=== ERROR AL ACTUALIZAR AEROPUERTO ID={Id}: {Message} | Inner: {Inner}",
                    id, ex.Message, ex.InnerException?.Message ?? "(none)");
                return StatusCode(500, new { message = ex.Message, detalle = ex.InnerException?.Message });
            }
        }

        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> Eliminar(int id)
        {
            var resultado = await _service.Eliminar(id);

            if (!resultado)
                return NotFound(new { message = "Aeropuerto no encontrado" });

            return Ok(new { message = "Aeropuerto eliminado correctamente" });
        }

        // ===== ENDPOINTS DE IMAGEN =====

        /// POST api/aeropuertos/{id}/imagen — sube o reemplaza la imagen del aeropuerto
        [Authorize(Roles = "Administrador")]
        [HttpPost("{id}/imagen")]
        public async Task<IActionResult> SubirImagen(int id, [FromBody] SubirImagenDTO dto)
        {
            if (string.IsNullOrEmpty(dto.ImagenBase64))
                return BadRequest(new { message = "La imagen no puede estar vacía" });

            var aeropuerto = await _service.ObtenerPorId(id);
            if (aeropuerto == null)
                return NotFound(new { message = "Aeropuerto no encontrado" });

            await _service.GuardarImagen(id, dto.ImagenBase64);
            return Ok(new { message = "Imagen guardada correctamente" });
        }

        /// DELETE api/aeropuertos/{id}/imagen — elimina la imagen del aeropuerto
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}/imagen")]
        public async Task<IActionResult> EliminarImagen(int id)
        {
            var aeropuerto = await _service.ObtenerPorId(id);
            if (aeropuerto == null)
                return NotFound(new { message = "Aeropuerto no encontrado" });

            await _service.EliminarImagen(id);
            return Ok(new { message = "Imagen eliminada correctamente" });
        }
    }
}