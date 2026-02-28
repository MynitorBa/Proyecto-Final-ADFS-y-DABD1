using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/admin/vuelos")]
    [Authorize(Roles = "Administrador")]   // Toda la clase exige sesión de administrador
    public class AdminVuelosController : ControllerBase
    {
        private readonly AdminVueloService _adminVueloService;

        public AdminVuelosController(AdminVueloService adminVueloService)
        {
            _adminVueloService = adminVueloService;
        }

        [HttpPost]
        public async Task<IActionResult> CrearVuelo([FromBody] CrearVueloAdminDTO dto)
        {
            try
            {
                var vueloId = await _adminVueloService.CrearVuelo(dto);
                return Ok(new
                {
                    message = "Vuelo creado exitosamente",
                    vueloId = vueloId
                });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al crear el vuelo: " + ex.Message });
            }
        }

        [HttpGet("historial")]
        public async Task<IActionResult> ObtenerHistorialVuelos()
        {
            try
            {
                var vuelos = await _adminVueloService.ObtenerHistorialVuelos();
                return Ok(vuelos);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener el historial: " + ex.Message });
            }
        }

        [HttpPut("{id}/cancelar")]
        public async Task<IActionResult> CancelarVuelo(int id)
        {
            try
            {
                var resultado = await _adminVueloService.CancelarVuelo(id);

                if (!resultado)
                    return NotFound(new { message = "No se pudo cancelar el vuelo. Puede que ya esté cancelado o no exista." });

                return Ok(new { message = "Vuelo cancelado exitosamente" });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al cancelar el vuelo: " + ex.Message });
            }
        }
    }
}