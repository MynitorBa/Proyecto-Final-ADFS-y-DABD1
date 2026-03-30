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

        // ── GET /api/admin/vuelos/aviones-ocupados ───────────────────────────
        // horaSalida y aeropuertoOrigenId son opcionales:
        //   - si no viene horaSalida se usa 00:00 (bloquea todo el día)
        //   - si no viene aeropuertoOrigenId se usa 0 (sin filtro de aeropuerto)
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

        // ── GET /api/admin/vuelos/tripulantes-ocupados ───────────────────────
        // horaSalida es opcional — si no viene se usa 00:00
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