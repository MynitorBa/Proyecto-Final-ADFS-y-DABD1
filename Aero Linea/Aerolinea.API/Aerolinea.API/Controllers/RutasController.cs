using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de rutas de vuelo. Expone endpoints para que el administrador consulte,
    /// cree y actualice rutas origen-destino, asi como calcule el tiempo estimado de llegada
    /// teniendo en cuenta las zonas horarias de los aeropuertos involucrados.
    /// Todos los endpoints requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/rutas")]
    public class RutasController : ControllerBase
    {
        private readonly IRutaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de rutas.
        /// </summary>
        public RutasController(IRutaService service)
        {
            _service = service;
        }

        // GET /api/rutas
        /// <summary>
        /// Retorna el listado completo de rutas registradas en el sistema. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet]
        public async Task<IActionResult> ObtenerTodas()
        {
            var rutas = await _service.ObtenerTodas();
            return Ok(rutas);
        }

        // PUT /api/rutas/{id}/duracion
        /// <summary>
        /// Actualiza la duracion estimada de vuelo de una ruta existente. Requiere rol Administrador.
        /// Retorna 404 si la ruta no existe y 400 si la duracion proporcionada no es valida.
        /// </summary>
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

        // POST /api/rutas/calcular-llegada
        /// <summary>
        /// Calcula la fecha y hora de llegada estimada a partir del aeropuerto origen, destino,
        /// fecha y hora de salida. Considera las zonas horarias de ambos aeropuertos. Retorna
        /// null si faltan datos en lugar de retornar error, para no bloquear el formulario del admin.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost("calcular-llegada")]
        public async Task<IActionResult> CalcularLlegada([FromBody] CalculoLlegadaRequestDTO request)
        {
            // Si faltan datos retorna null en lugar de error — el frontend lo ignora
            if (request == null ||
                request.AeropuertoOrigenId <= 0 ||
                request.AeropuertoDestinoId <= 0 ||
                string.IsNullOrWhiteSpace(request.FechaSalida) ||
                string.IsNullOrWhiteSpace(request.HoraSalida))
                return Ok((object)null);

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

        // POST /api/rutas
        /// <summary>
        /// Crea una nueva ruta entre dos aeropuertos con la duracion estimada indicada.
        /// Si la ruta ya existe retorna el mensaje correspondiente. Requiere rol Administrador.
        /// </summary>
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

        // GET /api/rutas/existe?origenId=1&destinoId=2
        /// <summary>
        /// Verifica si ya existe una ruta entre dos aeropuertos. Se usa en el formulario
        /// de creacion de rutas para validar en tiempo real. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("existe")]
        public async Task<IActionResult> ExisteRuta(
            [FromQuery] int origenId, [FromQuery] int destinoId)
        {
            var existe = await _service.ExisteRuta(origenId, destinoId);
            return Ok(new { existe });
        }

        // PUT /api/rutas/{id}/desactivar
        /// <summary>
        /// Desactiva una ruta solo si no tiene reservaciones activas. Una ruta desactivada
        /// no acepta nuevos vuelos. Requiere rol Administrador y la columna Activo en la
        /// tabla Ruta (ALTER TABLE Ruta ADD Activo BIT NOT NULL DEFAULT 1).
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/desactivar")]
        public async Task<IActionResult> DesactivarRuta(int id)
        {
            var (ok, mensaje) = await _service.DesactivarRuta(id);
            if (!ok)
                return BadRequest(new { message = mensaje });

            return Ok(new { message = mensaje });
        }

        // PUT /api/rutas/{id}/activar
        /// <summary>
        /// Reactiva una ruta previamente desactivada, permitiendo que vuelva a aceptar nuevos vuelos.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id}/activar")]
        public async Task<IActionResult> ActivarRuta(int id)
        {
            var (ok, mensaje) = await _service.ActivarRuta(id);
            if (!ok)
                return BadRequest(new { message = mensaje });

            return Ok(new { message = mensaje });
        }
    }
}
