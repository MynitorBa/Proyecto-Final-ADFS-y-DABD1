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
            // Verificar constraints primero
            var constraints = await _service.VerificarConstraints(dto);

            // Si algo ya existe, devolver error
            if (constraints.CorreoExiste || constraints.UsernameExiste || constraints.PasaporteExiste)
            {
                return BadRequest(new
                {
                    message = "No se puede crear el usuario",
                    correoExiste = constraints.CorreoExiste,
                    usernameExiste = constraints.UsernameExiste,
                    pasaporteExiste = constraints.PasaporteExiste
                });
            }

            // Si todo está libre, crear el usuario
            await _service.CrearUsuario(dto);
            return Ok(new { message = "Usuario creado correctamente" });
        }

        [HttpPost("verificar")]
        public async Task<IActionResult> VerificarConstraints([FromBody] CrearUsuarioDTO dto)
        {
            var constraints = await _service.VerificarConstraints(dto);
            return Ok(constraints);
        }
    }
}