using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador que expone el endpoint de busqueda de hoteles aliados.
    /// Requiere sesion activa; consulta la API de hoteles con el token
    /// de aerolinea configurado en el sistema.
    /// </summary>
    [ApiController]
    [Route("api/hoteles-aliados")]
    public class HotelAliadoController : ControllerBase
    {
        private readonly HotelAliadoService _service;

        /// <summary>
        /// Inicializa el controlador con el servicio de hoteles aliados.
        /// </summary>
        public HotelAliadoController(HotelAliadoService service)
        {
            _service = service;
        }

        // POST api/hoteles-aliados/busqueda
        /// <summary>
        /// Busca hoteles disponibles en la ciudad destino del pasajero
        /// consultando la API de hoteles aliados. Retorna solo el ID
        /// y nombre de cada hotel para que el frontend pueda mostrarlos.
        /// </summary>
        [HttpPost("busqueda")]
        [Authorize]
        public async Task<IActionResult> BuscarHoteles([FromBody] BusquedaHotelesDTO dto)
        {
            try
            {
                var hoteles = await _service.BuscarHoteles(dto);
                return Ok(hoteles);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}