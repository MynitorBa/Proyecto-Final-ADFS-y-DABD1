using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/metricas")]
    [Authorize(Roles = "Administrador")]
    public class MetricasController : ControllerBase
    {
        private readonly MetricasService _service;

        public MetricasController(MetricasService service)
        {
            _service = service;
        }

        // GET api/metricas/resumen?fechaDesde=2025-01-01&fechaHasta=2025-01-31
        [HttpGet("resumen")]
        public async Task<IActionResult> ObtenerResumen(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerResumen(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener métricas: " + ex.Message });
            }
        }

        // GET api/metricas/busquedas-por-dia
        [HttpGet("busquedas-por-dia")]
        public async Task<IActionResult> BusquedasPorDia(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerBusquedasPorDia(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET api/metricas/rutas-mas-buscadas
        [HttpGet("rutas-mas-buscadas")]
        public async Task<IActionResult> RutasMasBuscadas(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta,
            [FromQuery] string? tipo)
        {
            try
            {
                var resultado = await _service.ObtenerRutasMasBuscadas(fechaDesde, fechaHasta, tipo);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET api/metricas/busquedas-por-tipo
        [HttpGet("busquedas-por-tipo")]
        public async Task<IActionResult> BusquedasPorTipo(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerBusquedasPorTipo(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // POST api/metricas/listado  (con filtros en body)
        [HttpPost("listado")]
        public async Task<IActionResult> ObtenerListado([FromBody] MetricasFiltroDTO filtro)
        {
            try
            {
                var resultado = await _service.ObtenerListado(filtro);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }
    }
}