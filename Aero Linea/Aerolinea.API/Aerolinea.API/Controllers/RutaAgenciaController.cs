using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/rutas")]
    [ServiceFilter(typeof(AgenciaAuthMiddleware))]
    public class RutaAgenciaController : ControllerBase
    {
        private readonly RutaAgenciaService _service;

        public RutaAgenciaController(RutaAgenciaService service)
        {
            _service = service;
        }

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
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}