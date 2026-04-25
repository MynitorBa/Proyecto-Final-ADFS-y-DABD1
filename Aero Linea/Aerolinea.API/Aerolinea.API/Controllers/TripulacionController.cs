using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;
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
        private readonly AdminVueloRepository _adminVueloRepo;

        /// <summary>
        /// Inicializa el controlador con el servicio de tripulacion y el repositorio de vuelos admin
        /// (usado para verificar conflictos al desactivar un tripulante).
        /// </summary>
        public TripulacionController(ITripulacionService service, AdminVueloRepository adminVueloRepo)
        {
            _service        = service;
            _adminVueloRepo = adminVueloRepo;
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
        /// Retorna los vuelos activos futuros asignados al tripulante, separados en dos grupos:
        /// vuelos en menos de 48 horas (bloquean la desactivacion) y vuelos lejanos
        /// (el tripulante sera desasignado al confirmar la desactivacion). Requiere Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("{id}/vuelos-asignados")]
        public async Task<IActionResult> ObtenerVuelosAsignados(int id)
        {
            var vuelos      = await _service.ObtenerVuelosAsignadosDetallados(id);
            var vuelos48h   = vuelos.Where(v => v.HorasRestantes <= 48).ToList();
            var vuelosLejos = vuelos.Where(v => v.HorasRestantes > 48).ToList();
            return Ok(new { vuelos48h, vuelosLejanos = vuelosLejos });
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
        /// Cambia el estado activo/inactivo de un tripulante. Requiere rol Administrador.
        /// Al desactivar:
        ///   - Bloquea si hay vuelos asignados en menos de 48 horas.
        ///   - Si solo hay vuelos con mas de 48 horas: elimina al tripulante de EquipoPivote
        ///     para esos vuelos (los vuelos quedan activos, solo sin este tripulante) y desactiva.
        ///   - Si no hay vuelos futuros: desactiva directamente.
        /// Al reactivar: sin validaciones adicionales.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/estado")]
        public async Task<IActionResult> CambiarEstadoTripulante(int id, [FromBody] CambiarEstadoDTO dto)
        {
            if (!dto.Activo)
            {
                var vuelos      = await _service.ObtenerVuelosAsignadosDetallados(id);
                var vuelos48h   = vuelos.Where(v => v.HorasRestantes <= 48).ToList();
                var vuelosLejos = vuelos.Where(v => v.HorasRestantes > 48).ToList();

                // Bloquear si hay vuelos inminentes (< 48 h)
                if (vuelos48h.Count > 0)
                    return BadRequest(new
                    {
                        message  = $"No se puede desactivar. El tripulante tiene {vuelos48h.Count} vuelo(s) asignado(s) en menos de 48 horas.",
                        vuelos48h
                    });

                // Desasignar de vuelos lejanos (los vuelos quedan activos, solo sin este tripulante)
                if (vuelosLejos.Count > 0)
                    await _service.DesasignarDeFuturosVuelos(id, vuelosLejos.Select(v => v.Id));

                var resultado2 = await _service.CambiarEstado(id, false);
                if (!resultado2)
                    return NotFound(new { message = "Tripulante no encontrado" });

                return Ok(new
                {
                    message              = "Tripulante desactivado correctamente",
                    vuelosDesasignados   = vuelosLejos.Count,
                    nota                 = vuelosLejos.Count > 0
                        ? "Los vuelos quedan activos. Asigna un reemplazo desde el panel de vuelos."
                        : null
                });
            }

            var resultado = await _service.CambiarEstado(id, dto.Activo);
            if (!resultado)
                return NotFound(new { message = "Tripulante no encontrado" });

            return Ok(new { message = "Tripulante activado correctamente" });
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
