using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de tripulacion. Expone endpoints REST para consultar, crear, actualizar
    /// y eliminar tripulantes, asi como gestionar sus imagenes y obtener el catalogo de roles.
    /// Los endpoints de lectura son publicos; los de escritura requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/tripulacion")]
    public class TripulacionController : ControllerBase
    {
        private readonly ITripulacionService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de tripulacion.
        /// </summary>
        public TripulacionController(ITripulacionService service)
        {
            _service = service;
        }

        // Público: necesario para cargar listas en formularios del panel
        /// <summary>
        /// Retorna la lista de tripulantes registrados. Por defecto solo retorna tripulantes activos.
        /// Con incluirInactivos=true retorna todos (activos e inactivos). Endpoint publico, utilizado
        /// para poblar selectores en el formulario de creacion de vuelos del panel de admin.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerTodos([FromQuery] bool incluirInactivos = false)
        {
            var tripulantes = await _service.ObtenerTodos(incluirInactivos);
            return Ok(tripulantes);
        }

        /// <summary>
        /// Retorna los datos de un tripulante especifico por su identificador.
        /// Devuelve 404 si el tripulante no existe.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> ObtenerPorId(int id)
        {
            var tripulante = await _service.ObtenerPorId(id);

            if (tripulante == null)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(tripulante);
        }

        /// <summary>
        /// Retorna el catalogo de roles de tripulacion disponibles (piloto, copiloto,
        /// auxiliar de vuelo, etc.). Endpoint publico.
        /// </summary>
        [HttpGet("roles")]
        public async Task<IActionResult> ObtenerRoles()
        {
            var roles = await _service.ObtenerRoles();
            return Ok(roles);
        }

        // Solo administradores: operaciones de escritura
        /// <summary>
        /// Crea un nuevo tripulante con los datos del DTO. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> Crear([FromBody] CrearTripulanteDTO crearTripulanteDTO)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var tripulante = await _service.Crear(crearTripulanteDTO);
            return CreatedAtAction(nameof(ObtenerPorId), new { id = tripulante.Id }, tripulante);
        }

        /// <summary>
        /// Actualiza los datos de un tripulante existente. Requiere rol Administrador.
        /// Retorna 404 si el tripulante no existe.
        /// </summary>
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

        /// <summary>
        /// Elimina un tripulante por su identificador. Requiere rol Administrador.
        /// Retorna 400 si el tripulante tiene vuelos asignados activos; 404 si no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpDelete("{id}")]
        public async Task<IActionResult> Eliminar(int id)
        {
            var (totalFuturos, numeros48h) = await _service.VerificarVuelosAsignados(id);

            if (numeros48h.Count > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El tripulante tiene {numeros48h.Count} vuelo(s) asignado(s) en menos de 48 horas.",
                    vuelos = numeros48h
                });

            if (totalFuturos > 0)
                return BadRequest(new
                {
                    message = $"No se puede eliminar. El tripulante tiene {totalFuturos} vuelo(s) activo(s) asignados.",
                    cantidadVuelos = totalFuturos
                });

            var resultado = await _service.Eliminar(id);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante eliminado correctamente" });
        }

        /// <summary>
        /// Cambia el estado activo/inactivo de un tripulante (soft-delete). Requiere rol Administrador.
        /// Al desactivar, retorna 400 si el tripulante tiene vuelos activos asignados; 404 si no existe.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/estado")]
        public async Task<IActionResult> CambiarEstadoTripulante(int id, [FromBody] CambiarEstadoDTO dto)
        {
            if (!dto.Activo)
            {
                var (totalFuturos, numeros48h) = await _service.VerificarVuelosAsignados(id);

                if (numeros48h.Count > 0)
                    return BadRequest(new
                    {
                        message = $"No se puede desactivar. El tripulante tiene {numeros48h.Count} vuelo(s) asignado(s) en menos de 48 horas.",
                        vuelos = numeros48h
                    });

                if (totalFuturos > 0)
                    return BadRequest(new
                    {
                        message = $"No se puede desactivar. El tripulante tiene {totalFuturos} vuelo(s) activo(s) asignados.",
                        cantidadVuelos = totalFuturos
                    });
            }

            var resultado = await _service.CambiarEstado(id, dto.Activo);

            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            var estado = dto.Activo ? "activado" : "desactivado";
            return Ok(new { message = $"Tripulante {estado} correctamente" });
        }

        // ===== ENDPOINTS DE IMAGEN =====

        /// <summary>
        /// Sube o reemplaza la imagen de un tripulante enviada como cadena Base64.
        /// Requiere rol Administrador. Retorna 404 si el tripulante no existe.
        /// </summary>
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

        /// <summary>
        /// Elimina la imagen asociada a un tripulante. Requiere rol Administrador.
        /// Retorna 404 si el tripulante no existe.
        /// </summary>
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
