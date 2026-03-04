using Aerolinea.API.Models.DTOs;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Controllers
{
    [ApiController]
    [Route("api/admin/vuelos")]
    [Authorize(Roles = "Administrador")]
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
                return Ok(new { message = "Vuelo creado exitosamente", vueloId });
            }
            catch (ArgumentException ex)
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

        [HttpPut("{id}/cancelar")]
        public async Task<IActionResult> CancelarVuelo(int id)
        {
            try
            {
                var resultado = await _adminVueloService.CancelarVuelo(id);
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

        // ─── Traducción de errores SQL a mensajes legibles ───────────────────
        private static string TraducirErrorSql(SqlException ex, CrearVueloAdminDTO? dto)
        {
            return ex.Number switch
            {
                // Unique key / primary key violation
                2601 or 2627 => TraducirDuplicado(ex.Message, dto),

                // Foreign key violation
                547 => "Uno de los datos seleccionados (avión, aeropuerto o ruta) ya no existe en el sistema.",

                // Cannot insert null
                515 => "Faltan datos obligatorios. Verifica que todos los campos estén completos.",

                // String or binary data would be truncated
                8152 => "Uno de los campos excede el tamaño máximo permitido.",

                // Deadlock
                1205 => "El servidor está ocupado en este momento. Intenta de nuevo en unos segundos.",

                // Timeout
                -2 => "La operación tardó demasiado. Intenta de nuevo.",

                _ => "Error al procesar la solicitud. Intenta de nuevo."
            };
        }

        private static string TraducirDuplicado(string sqlMessage, CrearVueloAdminDTO? dto)
        {
            // Extraer el valor duplicado del mensaje SQL si está disponible
            // El mensaje tiene el formato: "...The duplicate key value is (AA 500)."
            string valorDuplicado = "";
            var match = System.Text.RegularExpressions.Regex
                .Match(sqlMessage, @"The duplicate key value is \((.+?)\)");
            if (match.Success)
                valorDuplicado = match.Groups[1].Value;

            // Identificar qué campo es por el nombre de la constraint o el valor
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

            // Genérico con el valor si lo tenemos
            return !string.IsNullOrEmpty(valorDuplicado)
                ? $"El valor \"{valorDuplicado}\" ya existe y no puede repetirse."
                : "Ya existe un registro con esos datos. Verifica los campos e intenta de nuevo.";
        }
        // GET /api/admin/vuelos/aviones-ocupados?fecha=2025-12-01
        [HttpGet("aviones-ocupados")]
        public async Task<IActionResult> AvionesOcupados([FromQuery] string fecha)
        {
            if (!DateTime.TryParse(fecha, out var fechaDate))
                return BadRequest(new { message = "Formato de fecha inválido" });

            var ids = await _adminVueloService.ObtenerAvionesOcupados(fechaDate);
            return Ok(ids);
        }

        // GET /api/admin/vuelos/tripulantes-ocupados?fecha=2025-12-01&horaSalida=08:00
        [HttpGet("tripulantes-ocupados")]
        public async Task<IActionResult> TripulantesOcupados(
            [FromQuery] string fecha, [FromQuery] string horaSalida)
        {
            if (!DateTime.TryParse(fecha, out var fechaDate))
                return BadRequest(new { message = "Formato de fecha inválido" });

            if (!TimeSpan.TryParse(horaSalida, out var hora))
                return BadRequest(new { message = "Formato de hora inválido" });

            var ids = await _adminVueloService.ObtenerTripulantesOcupados(fechaDate, hora);
            return Ok(ids);
        }

    }
}