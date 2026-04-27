using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de hoteles aliados. Expone el endpoint de busqueda dinamica de hoteles,
    /// los endpoints para que usuarios Webservice consulten y registren su propio hotel,
    /// y los endpoints de administracion completa para el panel de administrador.
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

        // ── Público: hoteles recomendados para el usuario final ───────────────
        /// <summary>
        /// Retorna el nombre y la URL Home de todos los hoteles aliados activos
        /// que tienen su home configurada. Accesible para cualquier usuario autenticado.
        /// Se usa para mostrar recomendaciones de hoteles aliados en la interfaz.
        /// </summary>
        [Authorize]
        [HttpGet("recomendaciones")]
        public async Task<IActionResult> ObtenerRecomendaciones()
        {
            var hoteles = await _service.ObtenerHotelesHome();
            return Ok(hoteles);
        }

        // POST api/hoteles-aliados/busqueda
        /// <summary>
        /// Busca hoteles disponibles en la ciudad destino del pasajero
        /// consultando la API de cada hotel aliado activo de forma dinamica.
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

        // ── Webservice: consulta su propio hotel aliado ───────────────────────
        /// <summary>
        /// Retorna los datos del hotel aliado asociado al usuario Webservice autenticado.
        /// Si el usuario no tiene hotel registrado, retorna tieneHotel = false.
        /// Solo accesible para usuarios con rol Webservice (rolId = 3).
        /// </summary>
        [Authorize]
        [HttpGet("mi-hotel")]
        public async Task<IActionResult> ObtenerMiHotel()
        {
            var rolId = SessionHelper.GetRolId(HttpContext);
            if (rolId != 3)
                return StatusCode(403, new { message = "Acceso restringido a usuarios Webservice." });

            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            if (usuarioId == null)
                return Unauthorized(new { message = "Sesión no válida." });

            var hotel = await _service.ObtenerMiHotel(usuarioId.Value);

            if (hotel == null)
                return Ok(new { tieneHotel = false, hotel = (object?)null });

            return Ok(new { tieneHotel = true, hotel });
        }

        // ── Webservice: registra su propio hotel aliado (solo una vez) ────────
        /// <summary>
        /// Permite a un usuario Webservice autenticado registrar su propio hotel aliado.
        /// Un usuario Webservice solo puede tener un hotel o una agencia, nunca ambos.
        /// Solo accesible para usuarios con rol Webservice (rolId = 3).
        /// </summary>
        [Authorize]
        [HttpPost("mi-hotel")]
        public async Task<IActionResult> CrearMiHotel([FromBody] CrearHotelWebserviceDTO dto)
        {
            var rolId = SessionHelper.GetRolId(HttpContext);
            if (rolId != 3)
                return StatusCode(403, new { message = "Acceso restringido a usuarios Webservice." });

            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            if (usuarioId == null)
                return Unauthorized(new { message = "Sesión no válida." });

            try
            {
                var hotel = await _service.CrearHotelWebservice(usuarioId.Value, dto);
                return Ok(new { message = "Hotel aliado registrado correctamente.", hotel });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: listar todos los hoteles aliados ───────────────────────────
        /// <summary>
        /// Retorna la lista completa de hoteles aliados con datos del usuario asignado.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpGet("todas")]
        public async Task<IActionResult> ObtenerTodosAdmin()
        {
            var hoteles = await _service.ObtenerTodosAdmin();
            return Ok(hoteles);
        }

        // ── Admin: crear hotel y asignarlo a un usuario Webservice ────────────
        /// <summary>
        /// Crea un nuevo hotel aliado y lo vincula al usuario Webservice indicado.
        /// Verifica que el usuario no tenga ya ninguna otra entidad asignada.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPost]
        public async Task<IActionResult> CrearHotelAdmin([FromBody] CrearHotelAdminDTO dto)
        {
            try
            {
                var hotel = await _service.CrearHotelAdmin(dto);
                return Ok(new { message = "Hotel aliado creado correctamente.", hotel });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: actualizar estado de un hotel ──────────────────────────────
        /// <summary>
        /// Actualiza el estado de un hotel aliado segun el catalogo EstadoAliado.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/estado")]
        public async Task<IActionResult> ActualizarEstado(int id, [FromBody] ActualizarEstadoHotelDTO dto)
        {
            try
            {
                await _service.ActualizarEstado(id, dto.EstadoId);
                return Ok(new { message = "Estado actualizado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: asignar usuario Webservice a un hotel ──────────────────────
        /// <summary>
        /// Asigna un usuario Webservice a un hotel aliado existente. El usuario no
        /// puede tener ya ninguna otra entidad asignada. Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/asignar-usuario")]
        public async Task<IActionResult> AsignarUsuario(int id, [FromBody] AsignarUsuarioHotelDTO dto)
        {
            try
            {
                await _service.AsignarUsuario(id, dto.UsuarioId);
                return Ok(new { message = "Usuario asignado correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        // ── Admin: actualizar URLs de un hotel ────────────────────────────────
        /// <summary>
        /// Actualiza la URL de la API y la URL publica para usuarios de un hotel aliado.
        /// Requiere rol Administrador.
        /// </summary>
        [Authorize(Roles = "Administrador")]
        [HttpPut("{id:int}/url")]
        public async Task<IActionResult> ActualizarUrls(int id, [FromBody] ActualizarUrlHotelDTO dto)
        {
            try
            {
                await _service.ActualizarUrls(id, dto.Url, dto.UrlParaUsuario, dto.UrlHomeAliado);
                return Ok(new { message = "URLs actualizadas correctamente." });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}