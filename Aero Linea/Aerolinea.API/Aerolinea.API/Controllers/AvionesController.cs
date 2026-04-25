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
        /// Retorna la lista de aviones registrados. Por defecto solo retorna aviones activos.
        /// Con incluirInactivos=true retorna todos (activos e inactivos). Endpoint publico,
        /// utilizado para poblar selectores en el formulario de creacion de vuelos del panel de admin.
        /// </summary>
        [HttpGet]
        public async Task<ActionResult<List<AvionDTO>>> ObtenerTodos([FromQuery] bool incluirInactivos = false)
        {
            var aviones = await _avionService.ObtenerTodos(incluirInactivos);
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
        /// Retorna 400 si el avion tiene vuelos activos programados; 404 si no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<ActionResult> Eliminar(int id)
        {
            var (totalFuturos, numeros48h) = await _avionService.VerificarVuelosActivos(id);

            if (numeros48h.Count > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El avión tiene {numeros48h.Count} vuelo(s) en menos de 48 horas.",
                    vuelos = numeros48h
                });

            if (totalFuturos > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El avión tiene {totalFuturos} vuelo(s) activo(s) programados.",
                    cantidadVuelos = totalFuturos
                });

            var eliminado = await _avionService.Eliminar(id);

            if (!eliminado)
                return NotFound(new { message = "Avión no encontrado" });

            return Ok(new { message = "Avión eliminado correctamente" });
        }

        /// <summary>
        /// Cambia el estado activo/inactivo de un avion (soft-delete). Requiere rol Administrador.
        /// Al desactivar, retorna 400 si el avion tiene vuelos activos programados; 404 si no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/estado")]
        public async Task<ActionResult> CambiarEstadoAvion(int id, [FromBody] CambiarEstadoDTO dto)
        {
            if (!dto.Activo)
            {
                var (totalFuturos, numeros48h) = await _avionService.VerificarVuelosActivos(id);

                if (numeros48h.Count > 0)
                    return BadRequest(new
                    {
                        message = $"No se puede desactivar. El avión tiene {numeros48h.Count} vuelo(s) en menos de 48 horas.",
                        vuelos = numeros48h
                    });

                if (totalFuturos > 0)
                    return BadRequest(new
                    {
                        message = $"No se puede desactivar. El avión tiene {totalFuturos} vuelo(s) activo(s) programados.",
                        cantidadVuelos = totalFuturos
                    });
            }

            var resultado = await _avionService.CambiarEstado(id, dto.Activo);

            if (!resultado)
                return NotFound(new { message = "Avión no encontrado" });

            var estado = dto.Activo ? "activado" : "desactivado";
            return Ok(new { message = $"Avión {estado} correctamente" });
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
