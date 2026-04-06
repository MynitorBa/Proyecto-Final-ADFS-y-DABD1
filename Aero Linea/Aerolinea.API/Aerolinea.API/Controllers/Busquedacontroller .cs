using Microsoft.AspNetCore.Mvc;
using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Aerolinea.API.Models;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de busquedas temporales de vuelos. Permite guardar los parametros de una
    /// busqueda en memoria y recuperarlos por identificador, facilitando la navegacion entre
    /// pasos del flujo de reservacion sin depender de la URL ni del estado del frontend.
    /// </summary>
    [ApiController]
    [Route("api/busquedas")]
    public class BusquedaController : ControllerBase
    {
        private readonly BusquedaTemporalService _busquedaService;

        /// <summary>
        /// Inicializa el controlador con el servicio de almacenamiento temporal de busquedas.
        /// </summary>
        public BusquedaController(BusquedaTemporalService busquedaService)
        {
            _busquedaService = busquedaService;
        }

        // POST api/busquedas/guardar
        /// <summary>
        /// Guarda los parametros de una busqueda de vuelos (origen, destino, fechas, pasajeros)
        /// en memoria y retorna un identificador unico para recuperarlos posteriormente.
        /// </summary>
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
        /// <summary>
        /// Recupera los parametros de una busqueda previamente guardada por su identificador.
        /// Retorna 404 si la busqueda no existe o ha expirado.
        /// </summary>
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
