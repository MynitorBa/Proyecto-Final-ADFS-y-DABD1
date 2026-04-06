using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de aeropuertos. Expone endpoints REST para consultar, crear, actualizar
    /// y eliminar aeropuertos, asi como gestionar sus imagenes. Los endpoints de lectura son
    /// publicos; los de escritura requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/aeropuertos")]
    public class AeropuertosController : ControllerBase
    {
        private readonly AeropuertoService _service;
        private readonly ILogger<AeropuertosController> _logger;

        /// <summary>
        /// Inicializa el controlador con el servicio de aeropuertos y el logger de la aplicacion.
        /// </summary>
        public AeropuertosController(AeropuertoService service, ILogger<AeropuertosController> logger)
        {
            _service = service;
            _logger = logger;
        }

        // Público: usado en búsqueda de vuelos por usuarios no autenticados
        /// <summary>
        /// Retorna la lista completa de aeropuertos registrados. Endpoint publico, utilizado
        /// en el buscador de vuelos para poblar los selectores de origen y destino.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerAeropuertos()
        {
            var aeropuertos = await _service.ObtenerAeropuertos();
            return Ok(aeropuertos);
        }

        /// <summary>
        /// Retorna los datos de un aeropuerto especifico por su identificador.
        /// Devuelve 404 si el aeropuerto no existe.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var aeropuerto = await _service.ObtenerPorId(id);
            if (aeropuerto == null)
                return NotFound(new { message = "Aeropuerto no encontrado" });
            return Ok(aeropuerto);
        }

        // GET /api/aeropuertos/fechas-disponibles
        /// <summary>
        /// Retorna las fechas con vuelos disponibles para una ruta determinada, filtrando por
        /// cantidad de pasajeros y clase. Se usa en el calendario del buscador de vuelos.
        /// </summary>
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
        /// <summary>
        /// Crea un nuevo aeropuerto con los datos del DTO. Requiere rol Administrador.
        /// Retorna 409 si ya existe un aeropuerto con el mismo codigo IATA.
        /// </summary>
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

        /// <summary>
        /// Actualiza los datos de un aeropuerto existente. Requiere rol Administrador.
        /// Retorna 404 si el aeropuerto no existe y 409 si hay conflicto con el codigo IATA.
        /// </summary>
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

        /// <summary>
        /// Elimina un aeropuerto por su identificador. Requiere rol Administrador.
        /// Retorna 404 si el aeropuerto no existe.
        /// </summary>
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

        /// <summary>
        /// Sube o reemplaza la imagen de un aeropuerto enviada como cadena Base64.
        /// Requiere rol Administrador. Retorna 404 si el aeropuerto no existe.
        /// </summary>
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

        /// <summary>
        /// Elimina la imagen asociada a un aeropuerto. Requiere rol Administrador.
        /// Retorna 404 si el aeropuerto no existe.
        /// </summary>
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
