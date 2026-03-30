using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/agencias")]
    public class AgenciaController : ControllerBase
    {
        private readonly AgenciaService _service;

        public AgenciaController(AgenciaService service)
        {
            _service = service;
        }

        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> CrearAgencia([FromBody] CrearAgenciaDTO dto)
        {
            try
            {
                var agencia = await _service.CrearAgencia(dto);
                return Ok(new { message = "Agencia creada correctamente", agencia });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}