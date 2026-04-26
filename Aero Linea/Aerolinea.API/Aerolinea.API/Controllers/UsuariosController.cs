using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de usuarios. Expone endpoints para el registro publico de nuevos usuarios,
    /// la validacion de datos en tiempo real durante el registro, el cambio de rol por parte
    /// del administrador y la consulta del listado completo de usuarios.
    /// </summary>
    [ApiController]
    [Route("api/usuarios")]
    public class UsuariosController : ControllerBase
    {
        private readonly IUsuarioService _service;
        private readonly LogRepository _logRepository;


        /// <summary>
        /// Inicializa el controlador con el servicio de usuarios.
        /// </summary>
        public UsuariosController(IUsuarioService service, LogRepository logRepository)
        {
            _service = service;
            _logRepository = logRepository;
        }

        // Público: cualquiera puede registrarse
        /// <summary>
        /// Crea un nuevo usuario en el sistema tras verificar que el correo, nombre de usuario
        /// y pasaporte no esten ya en uso. Endpoint publico, no requiere autenticacion.
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> CrearUsuario([FromBody] CrearUsuarioDTO dto)
        {
            string? ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            string? userAgent = Request.Headers["User-Agent"].ToString();

            var constraints = await _service.VerificarConstraints(dto);
            if (constraints.CorreoExiste || constraints.UsernameExiste || constraints.PasaporteExiste)
            {
                Console.WriteLine($"[DEBUG] Entrando al log de duplicados - username: {dto.Username}");

                await _logRepository.Registrar(
                    LogRepository.TipoRegistroFallido,
                    null,
                    dto.Username,
                    false,
                    ip,
                    userAgent,
                    $"Campos duplicados — correo:{constraints.CorreoExiste} username:{constraints.UsernameExiste} pasaporte:{constraints.PasaporteExiste}"
                );

                return BadRequest(new
                {
                    message = "No se puede crear el usuario",
                    correoExiste = constraints.CorreoExiste,
                    usernameExiste = constraints.UsernameExiste,
                    pasaporteExiste = constraints.PasaporteExiste
                });
            }

            await _service.CrearUsuario(dto, ip, userAgent);
            return Ok(new { message = "Usuario creado correctamente" });
        }

        // Público: validación en tiempo real durante el registro
        /// <summary>
        /// Verifica si el correo, nombre de usuario o pasaporte del DTO ya existen en el sistema.
        /// Se usa para validacion en tiempo real mientras el usuario completa el formulario de registro.
        /// Endpoint publico, no requiere autenticacion.
        /// </summary>
       
        [HttpPost("verificar")]
        public async Task<IActionResult> VerificarConstraints([FromBody] CrearUsuarioDTO dto)
        {
            string? ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            string? userAgent = Request.Headers["User-Agent"].ToString();

            var constraints = await _service.VerificarConstraints(dto);

            // Solo loggeamos si hay duplicados — esto es el intento real de registro del frontend
            if (constraints.CorreoExiste || constraints.UsernameExiste || constraints.PasaporteExiste)
            {
                await _logRepository.Registrar(
                    LogRepository.TipoRegistroFallido,
                    null,
                    dto.Username,
                    false,
                    ip,
                    userAgent,
                    $"Campos duplicados — correo:{constraints.CorreoExiste} username:{constraints.UsernameExiste} pasaporte:{constraints.PasaporteExiste}"
                );
            }

            return Ok(constraints);
        }

        // Solo administradores
        /// <summary>
        /// Cambia el rol de un usuario existente. Requiere rol Administrador.
        /// Retorna el resultado de la operacion con un mensaje descriptivo del cambio realizado.
        /// </summary>
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
        /// <summary>
        /// Retorna el listado completo de usuarios registrados en el sistema.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet]
        public async Task<IActionResult> ObtenerTodos()
        {
            var usuarios = await _service.ObtenerTodos();
            return Ok(usuarios);
        }
    }
}
