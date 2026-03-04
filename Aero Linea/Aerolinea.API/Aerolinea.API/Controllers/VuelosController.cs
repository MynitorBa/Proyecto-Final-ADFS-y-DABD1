using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/vuelos")]
    public class VuelosController : ControllerBase
    {
        private readonly VueloService _service;

        public VuelosController(VueloService service)
        {
            _service = service;
        }

        // ═══════════════════════════════════════════════════════════
        //  NUEVO: GET api/vuelos/busqueda-general?query=madrid
        // ═══════════════════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════════════════
        //  POST api/vuelos/buscar (sin cambios)
        // ═══════════════════════════════════════════════════════════
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