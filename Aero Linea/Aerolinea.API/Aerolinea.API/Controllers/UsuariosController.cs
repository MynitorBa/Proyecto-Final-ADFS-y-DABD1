using Aerolinea.API.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
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

        // Público: cualquiera puede registrarse
        [HttpPost]
        public async Task<IActionResult> CrearUsuario([FromBody] CrearUsuarioDTO dto)
        {
            var constraints = await _service.VerificarConstraints(dto);

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

            await _service.CrearUsuario(dto);
            return Ok(new { message = "Usuario creado correctamente" });
        }

        // Público: validación en tiempo real durante el registro
        [HttpPost("verificar")]
        public async Task<IActionResult> VerificarConstraints([FromBody] CrearUsuarioDTO dto)
        {
            var constraints = await _service.VerificarConstraints(dto);
            return Ok(constraints);
        }

        // Solo administradores
        [Authorize(Roles = "Administrador")]
        [HttpPost("cambiar-rol")]
        public async Task<IActionResult> CambiarRol([FromBody] CambiarRolDTO dto)
        {
            var (exito, mensaje) = await _service.CambiarRol(dto);

            if (exito)
                return Ok(new { message = mensaje });
            else
                return BadRequest(new { message = mensaje });
        }

        // Solo administradores
        [Authorize(Roles = "Administrador")]
        [HttpGet]
        public async Task<IActionResult> ObtenerTodos()
        {
            var usuarios = await _service.ObtenerTodos();
            return Ok(usuarios);
        }
    }
}