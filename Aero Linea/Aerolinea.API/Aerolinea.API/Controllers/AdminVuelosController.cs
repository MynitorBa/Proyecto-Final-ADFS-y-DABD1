using Aerolinea.API.DTOs;
using Aerolinea.API.Models.DTOs;

using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de administracion de vuelos. Expone endpoints REST para crear, cancelar y consultar
    /// vuelos desde el panel de administrador. Todos los endpoints requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/admin/vuelos")]
    [Authorize(Roles = "Administrador")]
    public class AdminVuelosController : ControllerBase
    {
        private readonly AdminVueloService _adminVueloService;

        /// <summary>
        /// Inicializa el controlador con el servicio de administracion de vuelos.
        /// </summary>
        public AdminVuelosController(AdminVueloService adminVueloService)
        {
            _adminVueloService = adminVueloService;
        }

        /// <summary>
        /// Crea un nuevo vuelo con los datos provistos por el administrador.
        /// Traduce errores de base de datos a mensajes legibles para el usuario.
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> CrearVuelo([FromBody] CrearVueloAdminDTO dto)
        {
            try
            {
                var vueloId = await _adminVueloService.CrearVuelo(dto);
                return Ok(new { message = "Vuelo creado exitosamente", vueloId });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (SqlException ex)
            {
                return BadRequest(new { message = TraducirErrorSql(ex, dto) });
            }
            catch (Exception ex) when (ex.InnerException is SqlException sqlEx)
            {
                return BadRequest(new { message = TraducirErrorSql(sqlEx, dto) });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Ocurrió un error inesperado al crear el vuelo. Intenta de nuevo." });
            }
        }

        /// <summary>
        /// Retorna el historial completo de vuelos registrados en el sistema, incluyendo
        /// vuelos pasados, activos y cancelados.
        /// </summary>
        [HttpGet("historial")]
        public async Task<IActionResult> ObtenerHistorialVuelos()
        {
            try
            {
                var vuelos = await _adminVueloService.ObtenerHistorialVuelos();
                return Ok(vuelos);
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "No se pudo cargar el historial de vuelos." });
            }
        }

        /// <summary>
        /// Cancela un vuelo activo por su identificador. Si el vuelo ya esta cancelado
        /// o no existe, retorna un error 404.
        /// </summary>
        [HttpPut("{id}/cancelar")]
        public async Task<IActionResult> CancelarVuelo(int id)
        {
            try
            {
                var ip        = HttpContext.Connection.RemoteIpAddress?.ToString();
                var userAgent = Request.Headers["User-Agent"].ToString();
                var resultado = await _adminVueloService.CancelarVuelo(id, ip, userAgent);
                if (!resultado)
                    return NotFound(new { message = "No se pudo cancelar el vuelo. Es posible que ya esté cancelado." });

                return Ok(new { message = "Vuelo cancelado exitosamente" });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (SqlException ex)
            {
                return BadRequest(new { message = TraducirErrorSql(ex, null) });
            }
            catch (Exception ex) when (ex.InnerException is SqlException sqlEx)
            {
                return BadRequest(new { message = TraducirErrorSql(sqlEx, null) });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Ocurrió un error inesperado al cancelar el vuelo." });
            }
        }

        /// <summary>
        /// Edita un vuelo existente. Requiere mas de 48h de anticipacion.
        /// Recalcula la hora de llegada y reasigna la tripulacion.
        /// </summary>
        [HttpPut("{id}")]
        public async Task<IActionResult> EditarVuelo(int id, [FromBody] EditarVueloDTO dto)
        {
            try
            {
                var vueloId = await _adminVueloService.EditarVuelo(id, dto);
                return Ok(new { message = "Vuelo actualizado correctamente", vueloId });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (SqlException ex)
            {
                return BadRequest(new { message = TraducirErrorSql(ex, null) });
            }
            catch (Exception ex) when (ex.InnerException is SqlException sqlEx)
            {
                return BadRequest(new { message = TraducirErrorSql(sqlEx, null) });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Ocurrió un error inesperado al editar el vuelo. Intenta de nuevo." });
            }
        }

        /// <summary>
        /// Cambia el avión asignado a un vuelo. Requiere al menos 48h de anticipación
        /// y que el nuevo avión tenga capacidad >= boletos ya vendidos.
        /// </summary>
        [HttpPut("{id}/avion")]
        public async Task<IActionResult> CambiarAvion(int id, [FromBody] CambiarAvionDTO dto)
        {
            try
            {
                await _adminVueloService.CambiarAvion(id, dto.AvionId);
                return Ok(new { message = "Avión del vuelo actualizado correctamente" });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Error inesperado al cambiar el avión." });
            }
        }

        // ── GET /api/admin/vuelos/siguiente-numero ───────────────────────────
        /// <summary>
        /// Devuelve el siguiente numero de secuencia disponible para un prefijo de vuelo.
        /// Por ejemplo, si ya existen BM 0001 y BM 0003, devuelve 4 (MAX + 1).
        /// Si no hay vuelos previos con ese prefijo, devuelve 1.
        /// </summary>
        [HttpGet("siguiente-numero")]
        public async Task<IActionResult> ObtenerSiguienteNumero([FromQuery] string prefijo)
        {
            if (string.IsNullOrWhiteSpace(prefijo))
                return BadRequest(new { message = "El prefijo es obligatorio." });

            prefijo = prefijo.Trim().ToUpper();

            if (!System.Text.RegularExpressions.Regex.IsMatch(prefijo, @"^[A-Z]{4}$"))
                return BadRequest(new { message = "El prefijo debe tener exactamente 4 letras mayúsculas." });

            try
            {
                var siguienteNumero = await _adminVueloService.ObtenerSiguienteNumeroVuelo(prefijo);
                return Ok(new
                {
                    siguienteNumero,
                    numeroCompleto = $"{prefijo} {siguienteNumero}"
                });
            }
            catch (ArgumentException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Error al calcular el siguiente número de vuelo." });
            }
        }

        // ── GET /api/admin/vuelos/aviones-ocupados ───────────────────────────
        // horaSalida y aeropuertoOrigenId son opcionales:
        //   - si no viene horaSalida se usa 00:00 (bloquea todo el día)
        //   - si no viene aeropuertoOrigenId se usa 0 (sin filtro de aeropuerto)
        /// <summary>
        /// Devuelve los identificadores de aviones que ya tienen vuelo asignado en la fecha y hora indicadas.
        /// Permite filtrar adicionalmente por aeropuerto de origen. Se usa en el formulario de creacion
        /// de vuelo para deshabilitar aviones no disponibles.
        /// </summary>
        [HttpGet("aviones-ocupados")]
        public async Task<IActionResult> AvionesOcupados(
            [FromQuery] string fecha,
            [FromQuery] string? horaSalida = null,
            [FromQuery] int aeropuertoOrigenId = 0)
        {
            if (!DateTime.TryParse(fecha, out var fechaDate))
                return BadRequest(new { message = "Formato de fecha inválido" });

            // Si no viene horaSalida usar medianoche como fallback
            var hora = TimeSpan.Zero;
            if (!string.IsNullOrEmpty(horaSalida) && !TimeSpan.TryParse(horaSalida, out hora))
                return BadRequest(new { message = "Formato de hora inválido" });

            var ids = await _adminVueloService.ObtenerAvionesOcupados(fechaDate, hora, aeropuertoOrigenId);
            return Ok(ids);
        }

        // ── GET /api/admin/vuelos/{id}/tripulantes ───────────────────────────
        /// <summary>
        /// Retorna la lista de tripulantes actualmente asignados a un vuelo especifico.
        /// Se usa en el modal de edicion de vuelo para precargar la tripulacion existente.
        /// </summary>
        [HttpGet("{id}/tripulantes")]
        public async Task<IActionResult> ObtenerTripulantesDelVuelo(int id)
        {
            try
            {
                var tripulantes = await _adminVueloService.ObtenerTripulantesDelVuelo(id);
                return Ok(tripulantes);
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Error al obtener tripulantes del vuelo." });
            }
        }

        // ── GET /api/admin/vuelos/tripulantes-ocupados ───────────────────────
        // horaSalida es opcional — si no viene se usa 00:00
        /// <summary>
        /// Devuelve los identificadores de tripulantes que ya tienen vuelo asignado en la fecha y hora indicadas.
        /// Se usa en el formulario de creacion de vuelo para deshabilitar tripulantes no disponibles.
        /// </summary>
        [HttpGet("tripulantes-ocupados")]
        public async Task<IActionResult> TripulantesOcupados(
            [FromQuery] string fecha,
            [FromQuery] string? horaSalida = null)
        {
            if (!DateTime.TryParse(fecha, out var fechaDate))
                return BadRequest(new { message = "Formato de fecha inválido" });

            var hora = TimeSpan.Zero;
            if (!string.IsNullOrEmpty(horaSalida) && !TimeSpan.TryParse(horaSalida, out hora))
                return BadRequest(new { message = "Formato de hora inválido" });

            var ids = await _adminVueloService.ObtenerTripulantesOcupados(fechaDate, hora);
            return Ok(ids);
        }

        // ── Traducción de errores SQL a mensajes legibles ────────────────────
        private static string TraducirErrorSql(SqlException ex, CrearVueloAdminDTO? dto)
        {
            return ex.Number switch
            {
                2601 or 2627 => TraducirDuplicado(ex.Message, dto),
                547 => "Uno de los datos seleccionados (avión, aeropuerto o ruta) ya no existe en el sistema.",
                515 => "Faltan datos obligatorios. Verifica que todos los campos estén completos.",
                8152 => "Uno de los campos excede el tamaño máximo permitido.",
                1205 => "El servidor está ocupado en este momento. Intenta de nuevo en unos segundos.",
                -2 => "La operación tardó demasiado. Intenta de nuevo.",
                _ => "Error al procesar la solicitud. Intenta de nuevo."
            };
        }

        private static string TraducirDuplicado(string sqlMessage, CrearVueloAdminDTO? dto)
        {
            string valorDuplicado = "";
            var match = System.Text.RegularExpressions.Regex
                .Match(sqlMessage, @"The duplicate key value is \((.+?)\)");
            if (match.Success)
                valorDuplicado = match.Groups[1].Value;

            if (sqlMessage.Contains("UQ__Vuelo") || sqlMessage.Contains("NumeroVuelo") ||
                sqlMessage.Contains("IX_Vuelo"))
            {
                var numero = !string.IsNullOrEmpty(valorDuplicado)
                    ? valorDuplicado
                    : dto?.NumeroVuelo ?? "ese número";
                return $"Ya existe un vuelo con el número \"{numero}\". Usa un número de vuelo diferente.";
            }

            if (sqlMessage.Contains("Ruta") || sqlMessage.Contains("UQ__Ruta"))
                return "Ya existe esa ruta (origen-destino) en el sistema.";

            if (sqlMessage.Contains("EquipoPivote") || sqlMessage.Contains("Tripulacion"))
                return "Uno de los tripulantes ya está asignado a este vuelo.";

            return !string.IsNullOrEmpty(valorDuplicado)
                ? $"El valor \"{valorDuplicado}\" ya existe y no puede repetirse."
                : "Ya existe un registro con esos datos. Verifica los campos e intenta de nuevo.";
        }
    }
}
