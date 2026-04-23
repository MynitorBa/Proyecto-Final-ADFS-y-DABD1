using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador principal de vuelos del portal web. Expone endpoints publicos para buscar
    /// vuelos disponibles por criterios de origen, destino, fecha y pasajeros, y para realizar
    /// una busqueda general por texto libre. No requiere autenticacion en ningun endpoint.
    /// </summary>
    [ApiController]
    [Route("api/vuelos")]
    public class VuelosController : ControllerBase
    {
        private readonly IVueloService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de vuelos.
        /// </summary>
        public VuelosController(IVueloService service)
        {
            _service = service;
        }

        /// <summary>
        /// Realiza una busqueda general de vuelos por texto libre (numero de vuelo, ciudad,
        /// codigo de aeropuerto, etc.). Requiere al menos 2 caracteres en el termino de busqueda.
        /// Endpoint publico, no requiere autenticacion.
        /// </summary>
        [HttpGet("busqueda-general")]
        public async Task<IActionResult> BusquedaGeneral([FromQuery] string query)
        {
            if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
                return BadRequest(new { message = "El término de búsqueda debe tener al menos 2 caracteres." });

            try
            {
                var vuelos = await _service.BusquedaGeneral(query.Trim());
                return Ok(vuelos);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Busca vuelos disponibles entre origen y destino para la fecha y cantidad de pasajeros
        /// indicados en el DTO. Si el usuario esta autenticado, registra la busqueda para metricas.
        /// Endpoint publico, no requiere autenticacion.
        /// </summary>
        [HttpPost("buscar")]
        public async Task<IActionResult> BuscarVuelos([FromBody] BuscarVueloDTO dto)
        {
            try
            {
                int? usuarioId = SessionHelper.GetUsuarioId(HttpContext);
                var resultado = await _service.BuscarVuelos(dto, usuarioId);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
