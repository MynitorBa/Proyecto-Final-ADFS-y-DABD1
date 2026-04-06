using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de rutas para agencias de viaje externas. Expone el listado de rutas
    /// disponibles a agencias autenticadas mediante AgenciaAuthMiddleware, para que puedan
    /// construir sus propios buscadores de vuelos.
    /// </summary>
    [ApiController]
    [Route("api/rutas-agencia")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class RutaAgenciaController : ControllerBase
    {
        private readonly RutaAgenciaService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de rutas para agencias.
        /// </summary>
        public RutaAgenciaController(RutaAgenciaService service)
        {
            _service = service;
        }

        /// <summary>
        /// Retorna el listado completo de rutas con vuelos disponibles para consumo de agencias.
        /// Requiere autenticacion de agencia mediante token en la solicitud.
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> ObtenerRutas()
        {
            try
            {
                var rutas = await _service.ObtenerTodasLasRutas();
                return Ok(rutas);
            }
            catch (Exception ex)
            {
                // Temporal para debug
                Console.WriteLine($"[RUTAS ERROR] {ex.Message}");
                Console.WriteLine($"[RUTAS ERROR] {ex.StackTrace}");
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}
