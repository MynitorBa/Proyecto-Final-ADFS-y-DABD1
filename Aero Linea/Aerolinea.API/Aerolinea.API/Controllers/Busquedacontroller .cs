using Microsoft.AspNetCore.Mvc;
using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Aerolinea.API.Models;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/busquedas")]
    public class BusquedaController : ControllerBase
    {
        private readonly BusquedaTemporalService _busquedaService;

        public BusquedaController(BusquedaTemporalService busquedaService)
        {
            _busquedaService = busquedaService;
        }

        // POST api/busquedas/guardar
        [HttpPost("guardar")]
        public IActionResult GuardarBusqueda([FromBody] GuardarBusquedaDto dto)
        {
            var busqueda = new BusquedaVuelo
            {
                OrigenId = dto.OrigenId,
                DestinoId = dto.DestinoId,
                OrigenNombre = dto.OrigenNombre,
                DestinoNombre = dto.DestinoNombre,
                OrigenCodigo = dto.OrigenCodigo,
                DestinoCodigo = dto.DestinoCodigo,
                FechaIda = dto.FechaIda,
                FechaVuelta = dto.FechaVuelta,
                Pasajeros = dto.Pasajeros,
                TripType = dto.TripType
            };

            var busquedaId = _busquedaService.GuardarBusqueda(busqueda);

            return Ok(new { id = busquedaId });
        }

        // GET api/busquedas/{id}
        [HttpGet("{id}")]
        public IActionResult ObtenerBusqueda(string id)
        {
            var busqueda = _busquedaService.ObtenerBusqueda(id);

            if (busqueda == null)
            {
                return NotFound(new { mensaje = "Búsqueda no encontrada o expirada" });
            }

            return Ok(busqueda);
        }
    }
}