using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/aeropuertos")]
    public class AeropuertosController : ControllerBase
    {
        private readonly AeropuertoService _service;

        public AeropuertosController(AeropuertoService service)
        {
            _service = service;
        }

        [HttpGet]
        public async Task<IActionResult> ObtenerAeropuertos()
        {
            var aeropuertos = await _service.ObtenerAeropuertos();
            return Ok(aeropuertos);
        }

        [HttpGet("fechas-disponibles")]
        public async Task<IActionResult> ObtenerFechasDisponibles([FromQuery] int? origenId, [FromQuery] int? destinoId)
        {
            var fechas = await _service.ObtenerFechasDisponiblesPorRuta(origenId, destinoId);
            return Ok(fechas);
        }
    }
}