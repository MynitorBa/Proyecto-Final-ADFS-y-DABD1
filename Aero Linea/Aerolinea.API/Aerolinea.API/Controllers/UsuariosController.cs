using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/usuarios")]
    public class UsuariosController : ControllerBase
    {
        private readonly UsuarioService _service;

        public UsuariosController(UsuarioService service)
        {
            _service = service;
        }

        [HttpPost]
        public async Task<IActionResult> CrearUsuario([FromBody] CrearUsuarioDTO dto)
        {
            await _service.CrearUsuario(dto);
            return Ok(new { message = "Usuario creado correctamente" });
        }
    }
}
