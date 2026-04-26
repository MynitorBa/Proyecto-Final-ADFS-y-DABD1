using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de metricas y analiticos del sistema. Expone endpoints para que el administrador
    /// consulte resumenes, graficas de busquedas por dia, rutas mas buscadas, busquedas por tipo
    /// de canal, listados con filtros paginados y exportacion de reportes por correo.
    /// Todos los endpoints requieren rol Administrador.
    /// </summary>
    [ApiController]
    [Route("api/metricas")]
    [Authorize(Roles = "Administrador")]
    public class MetricasController : ControllerBase
    {
        private readonly MetricasService _service;
        private readonly EmailHelper     _emailHelper;

        /// <summary>
        /// Inicializa el controlador con el servicio de metricas y el helper de correo.
        /// </summary>
        public MetricasController(MetricasService service, EmailHelper emailHelper)
        {
            _service     = service;
            _emailHelper = emailHelper;
        }

        // GET api/metricas/resumen?fechaDesde=2025-01-01&fechaHasta=2025-01-31
        /// <summary>
        /// Retorna un resumen de metricas clave del sistema (totales, conversiones, etc.)
        /// para el rango de fechas indicado. Si no se especifican fechas se usa el periodo completo.
        /// </summary>
        [HttpGet("resumen")]
        public async Task<IActionResult> ObtenerResumen(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerResumen(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener métricas: " + ex.Message });
            }
        }

        // GET api/metricas/busquedas-por-dia
        /// <summary>
        /// Retorna la cantidad de busquedas realizadas por dia dentro del rango de fechas indicado.
        /// Se usa para renderizar la grafica de linea en el panel de analiticos del administrador.
        /// </summary>
        [HttpGet("busquedas-por-dia")]
        public async Task<IActionResult> BusquedasPorDia(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerBusquedasPorDia(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET api/metricas/rutas-mas-buscadas
        /// <summary>
        /// Retorna las rutas origen-destino mas frecuentes en el periodo indicado, con opcion de
        /// filtrar por tipo de canal (Web o REST). Se usa para la grafica de barras del panel.
        /// </summary>
        [HttpGet("rutas-mas-buscadas")]
        public async Task<IActionResult> RutasMasBuscadas(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta,
            [FromQuery] string? tipo)
        {
            try
            {
                var resultado = await _service.ObtenerRutasMasBuscadas(fechaDesde, fechaHasta, tipo);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET api/metricas/busquedas-por-tipo
        /// <summary>
        /// Retorna el desglose de busquedas por tipo de canal (Web vs REST) en el periodo indicado.
        /// Se usa para la grafica de dona del panel de analiticos.
        /// </summary>
        [HttpGet("busquedas-por-tipo")]
        public async Task<IActionResult> BusquedasPorTipo(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerBusquedasPorTipo(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // POST api/metricas/listado  (con filtros en body)
        /// <summary>
        /// Retorna un listado paginado de registros de busqueda con los filtros especificados
        /// en el cuerpo de la solicitud (fechas, tipo de canal, usuario y tamano de pagina).
        /// </summary>
        [HttpPost("listado")]
        public async Task<IActionResult> ObtenerListado([FromBody] MetricasFiltroDTO filtro)
        {
            try
            {
                var resultado = await _service.ObtenerListado(filtro);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = ex.Message });
            }
        }

        // GET api/metricas/negocio?fechaDesde=&fechaHasta=
        /// <summary>
        /// Retorna los 5 graficos de analisis de negocio: embudo de conversion, rendimiento de rutas,
        /// analisis de cancelaciones, tendencia mensual de ingresos por clase y mapa de calor de
        /// ocupacion por dia y hora. Solo accesible para administradores.
        /// </summary>
        [HttpGet("negocio")]
        public async Task<IActionResult> ObtenerNegocio(
            [FromQuery] string? fechaDesde,
            [FromQuery] string? fechaHasta)
        {
            try
            {
                var resultado = await _service.ObtenerNegocio(fechaDesde, fechaHasta);
                return Ok(resultado);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener análisis de negocio: " + ex.Message });
            }
        }

        // POST api/metricas/exportar-correo
        /// <summary>
        /// Genera un reporte HTML con todos los registros de busqueda segun los filtros indicados
        /// y lo envia por correo electronico a la direccion especificada. No aplica paginacion
        /// al exportar, incluye hasta 9999 registros.
        /// </summary>
        // POST api/metricas/exportar-correo — envía Excel como adjunto por correo
        [HttpPost("exportar-correo")]
        public async Task<IActionResult> ExportarPorCorreo([FromBody] ExportarMetricasDTO dto)
        {
            var correosValidos = (dto.Correos ?? new())
                .Select(c => c.Trim())
                .Where(c => !string.IsNullOrWhiteSpace(c) && c.Contains("@"))
                .Distinct().ToList();

            if (correosValidos.Count == 0)
                return BadRequest(new { message = "Debe indicar al menos un correo válido" });

            try
            {
                var sec = dto.Secciones ?? new ExportSecciones();
                string periodo = $"{dto.FechaDesde ?? "inicio"} → {dto.FechaHasta ?? "hoy"}";

                MetricasResumenDTO? resumen = sec.NecesitaResumen
                    ? await _service.ObtenerResumen(dto.FechaDesde, dto.FechaHasta) : null;
                NegocioMetricasDTO? negocio = sec.NecesitaNegocio
                    ? await _service.ObtenerNegocio(dto.FechaDesde, dto.FechaHasta) : null;
                ListadoBusquedasDTO? listado = sec.Registro
                    ? await _service.ObtenerListadoCompleto(new MetricasFiltroDTO
                      { FechaDesde = dto.FechaDesde, FechaHasta = dto.FechaHasta,
                        Tipo = dto.Tipo, Usuario = dto.Usuario, TamañoPagina = 9999 }) : null;

                string asunto  = $"Broom AirLine — Reporte de Métricas ({periodo})";
                string htmlMsg = $"<p style='font-family:sans-serif'>Adjunto el reporte de métricas del período <strong>{periodo}</strong>.</p>" +
                                 $"<p style='font-family:sans-serif;color:#666'>Generado por el panel administrativo de Broom AirLine.</p>";
                string fechaTag = $"{(dto.FechaDesde ?? "todo")}_{(dto.FechaHasta ?? "hoy")}";

                byte[] adjunto;
                string nombre;
                string mime;

                if (dto.Formato == "csv")
                {
                    adjunto = MetricasExportHelper.GenerarCsvZip(resumen, negocio, listado, sec, periodo);
                    nombre  = $"metricas_broom_{fechaTag}.zip";
                    mime    = "application/zip";
                }
                else if (dto.Formato == "pdf")
                {
                    adjunto = MetricasPdfHelper.GenerarPdf(resumen, negocio, listado, sec, periodo);
                    nombre  = $"metricas_broom_{fechaTag}.pdf";
                    mime    = "application/pdf";
                }
                else // excel (default)
                {
                    adjunto = MetricasExportHelper.GenerarExcel(resumen, negocio, listado, sec, periodo);
                    nombre  = $"metricas_broom_{fechaTag}.xlsx";
                    mime    = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                }

                await _emailHelper.EnviarConAdjunto(correosValidos, asunto, htmlMsg, adjunto, nombre, mime);

                return Ok(new { message = $"Reporte enviado a {string.Join(", ", correosValidos)}" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al enviar el reporte: " + ex.Message });
            }
        }

        // POST api/metricas/exportar-pdf — descarga PDF con gráficas reales
        [HttpPost("exportar-pdf")]
        public async Task<IActionResult> ExportarPdf([FromBody] ExportarArchivoDTO dto)
        {
            try
            {
                var sec = dto.Secciones ?? new ExportSecciones();
                string periodo = $"{dto.FechaDesde ?? "inicio"} → {dto.FechaHasta ?? "hoy"}";

                MetricasResumenDTO? resumen = sec.NecesitaResumen
                    ? await _service.ObtenerResumen(dto.FechaDesde, dto.FechaHasta) : null;
                NegocioMetricasDTO? negocio = sec.NecesitaNegocio
                    ? await _service.ObtenerNegocio(dto.FechaDesde, dto.FechaHasta) : null;
                ListadoBusquedasDTO? listado = sec.Registro
                    ? await _service.ObtenerListadoCompleto(new MetricasFiltroDTO
                      { FechaDesde = dto.FechaDesde, FechaHasta = dto.FechaHasta,
                        Tipo = dto.Tipo, Usuario = dto.Usuario, TamañoPagina = 9999 }) : null;

                string fecha = $"{(dto.FechaDesde ?? "todo")}_{(dto.FechaHasta ?? "hoy")}";
                byte[] pdf = MetricasPdfHelper.GenerarPdf(resumen, negocio, listado, sec, periodo);
                return File(pdf, "application/pdf", $"metricas_broom_{fecha}.pdf");
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al generar el PDF: " + ex.Message });
            }
        }

        // POST api/metricas/exportar-archivo — descarga Excel o ZIP de CSVs
        [HttpPost("exportar-archivo")]
        public async Task<IActionResult> ExportarArchivo([FromBody] ExportarArchivoDTO dto)
        {
            try
            {
                var sec = dto.Secciones ?? new ExportSecciones();
                string periodo = $"{dto.FechaDesde ?? "inicio"} → {dto.FechaHasta ?? "hoy"}";

                MetricasResumenDTO? resumen = sec.NecesitaResumen
                    ? await _service.ObtenerResumen(dto.FechaDesde, dto.FechaHasta) : null;
                NegocioMetricasDTO? negocio = sec.NecesitaNegocio
                    ? await _service.ObtenerNegocio(dto.FechaDesde, dto.FechaHasta) : null;
                ListadoBusquedasDTO? listado = sec.Registro
                    ? await _service.ObtenerListadoCompleto(new MetricasFiltroDTO
                      { FechaDesde = dto.FechaDesde, FechaHasta = dto.FechaHasta,
                        Tipo = dto.Tipo, Usuario = dto.Usuario, TamañoPagina = 9999 }) : null;

                string fecha = $"{(dto.FechaDesde ?? "todo")}_{(dto.FechaHasta ?? "hoy")}";

                if (dto.Formato == "csv")
                {
                    byte[] zip = MetricasExportHelper.GenerarCsvZip(resumen, negocio, listado, sec, periodo);
                    return File(zip, "application/zip", $"metricas_broom_{fecha}.zip");
                }
                else
                {
                    byte[] excel = MetricasExportHelper.GenerarExcel(resumen, negocio, listado, sec, periodo);
                    return File(excel,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        $"metricas_broom_{fecha}.xlsx");
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al generar el archivo: " + ex.Message });
            }
        }

        // ── Generador de HTML del reporte ────────────────────────────────────
        private static string GenerarHtmlExporte(ListadoBusquedasDTO listado, ExportarMetricasDTO filtros)
        {
            var periodoDesde = filtros.FechaDesde ?? "inicio";
            var periodoHasta = filtros.FechaHasta ?? "hoy";
            var canal = string.IsNullOrEmpty(filtros.Tipo) ? "Todos" : filtros.Tipo;
            var usuario = string.IsNullOrEmpty(filtros.Usuario) ? "Todos" : filtros.Usuario;

            // Construir filas de la tabla
            var filas = new System.Text.StringBuilder();
            foreach (var r in listado.Registros)
            {
                var canalBadge = r.Tipo == "Web"
                    ? "<span style='background:#D4AF37;color:#1C1A18;padding:2px 8px;border-radius:12px;font-size:11px;font-weight:700'>Web</span>"
                    : "<span style='background:#1C1A18;color:white;padding:2px 8px;border-radius:12px;font-size:11px;font-weight:700'>REST</span>";

                filas.Append($@"
                <tr style='border-bottom:1px solid #F0EBE3'>
                  <td style='padding:10px 12px;color:#6b7280;font-size:13px'>#{r.Id}</td>
                  <td style='padding:10px 12px;font-weight:600;color:#1C1A18'>
                    <span style='background:#FFF8E7;color:#92400e;padding:2px 7px;border-radius:4px;font-family:monospace;font-size:12px'>{r.OrigenCodigo}</span>
                    &nbsp;→&nbsp;
                    <span style='background:#FFF8E7;color:#92400e;padding:2px 7px;border-radius:4px;font-family:monospace;font-size:12px'>{r.DestinoCodigo}</span>
                  </td>
                  <td style='padding:10px 12px;color:#374151;font-size:13px'>{r.FechaSalida}</td>
                  <td style='padding:10px 12px;color:#374151;font-size:13px;text-align:center'>{r.CantidadPersonas}</td>
                  <td style='padding:10px 12px;color:#374151;font-size:13px'>{r.Usuario ?? "<em style='color:#9ca3af'>anónimo</em>"}</td>
                  <td style='padding:10px 12px'>{canalBadge}</td>
                  <td style='padding:10px 12px;color:#6b7280;font-size:12px'>{r.FechaBusqueda}</td>
                </tr>");
            }

            return $@"<!DOCTYPE html>
<html lang='es'>
<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'></head>
<body style='margin:0;padding:0;background:#F5F0E8;font-family:Arial,sans-serif'>
  <table width='100%' cellpadding='0' cellspacing='0' style='background:#F5F0E8;padding:32px 0'>
    <tr><td align='center'>
      <table width='700' cellpadding='0' cellspacing='0' style='background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08)'>

        <!-- Header -->
        <tr>
          <td style='background:#1C1A18;padding:28px 32px'>
            <h1 style='margin:0;color:#D4AF37;font-size:22px;font-weight:800'>✈ Airbroom Airline</h1>
            <p style='margin:6px 0 0;color:rgba(255,255,255,.6);font-size:13px'>Reporte de Analíticos y Búsquedas</p>
          </td>
        </tr>

        <!-- Filtros aplicados -->
        <tr>
          <td style='padding:20px 32px;background:#FAFAF8;border-bottom:1px solid #EBE6E0'>
            <table width='100%' cellpadding='0' cellspacing='0'>
              <tr>
                <td style='font-size:12px;color:#6b7280;text-transform:uppercase;letter-spacing:.08em;padding-bottom:8px' colspan='4'>
                  Filtros aplicados
                </td>
              </tr>
              <tr>
                <td style='padding:4px 16px 4px 0'>
                  <span style='color:#9ca3af;font-size:11px'>DESDE</span><br>
                  <strong style='color:#1C1A18;font-size:13px'>{periodoDesde}</strong>
                </td>
                <td style='padding:4px 16px 4px 0'>
                  <span style='color:#9ca3af;font-size:11px'>HASTA</span><br>
                  <strong style='color:#1C1A18;font-size:13px'>{periodoHasta}</strong>
                </td>
                <td style='padding:4px 16px 4px 0'>
                  <span style='color:#9ca3af;font-size:11px'>CANAL</span><br>
                  <strong style='color:#1C1A18;font-size:13px'>{canal}</strong>
                </td>
                <td style='padding:4px 0'>
                  <span style='color:#9ca3af;font-size:11px'>USUARIO</span><br>
                  <strong style='color:#1C1A18;font-size:13px'>{usuario}</strong>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Resumen -->
        <tr>
          <td style='padding:20px 32px 0'>
            <p style='margin:0 0 4px;font-size:12px;color:#9ca3af;text-transform:uppercase;letter-spacing:.08em'>Total de registros</p>
            <p style='margin:0;font-size:28px;font-weight:800;color:#1C1A18'>{listado.TotalRegistros.ToString("N0")}</p>
            <p style='margin:4px 0 0;font-size:12px;color:#9ca3af'>búsquedas en el periodo</p>
          </td>
        </tr>

        <!-- Tabla -->
        <tr>
          <td style='padding:20px 32px 32px'>
            <table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #EBE6E0;border-radius:8px;overflow:hidden'>
              <thead>
                <tr style='background:#F9F6F1'>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>#</th>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Ruta</th>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Fecha salida</th>
                  <th style='padding:10px 12px;text-align:center;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Pax</th>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Usuario</th>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Canal</th>
                  <th style='padding:10px 12px;text-align:left;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:.06em;font-weight:600'>Fecha búsqueda</th>
                </tr>
              </thead>
              <tbody>{filas}</tbody>
            </table>
            {(listado.TotalRegistros == 0 ? "<p style='text-align:center;color:#9ca3af;padding:24px'>Sin registros para el periodo seleccionado</p>" : "")}
          </td>
        </tr>

        <!-- Footer -->
        <tr>
          <td style='background:#F9F6F1;padding:16px 32px;border-top:1px solid #EBE6E0;text-align:center'>
            <p style='margin:0;font-size:11px;color:#9ca3af'>
              Reporte generado por <strong style='color:#1C1A18'>Airbroom Airline</strong> · {DateTime.Now:dd/MM/yyyy HH:mm}
            </p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>";
        }
    }

    // ── DTO para exportar ─────────────────────────────────────────────────────
    /// <summary>
    /// DTO con los filtros y el correo destino para la exportacion del reporte de metricas.
    /// </summary>
    public class ExportarMetricasDTO
    {
        public List<string> Correos  { get; set; } = new();
        public string Formato        { get; set; } = "excel"; // "excel" | "csv" | "pdf"
        public string? FechaDesde    { get; set; }
        public string? FechaHasta    { get; set; }
        public string? Tipo          { get; set; }
        public string? Usuario       { get; set; }
        public ExportSecciones? Secciones { get; set; }
    }

    public class ExportarArchivoDTO
    {
        public string Formato        { get; set; } = "excel"; // "excel" | "csv"
        public string? FechaDesde    { get; set; }
        public string? FechaHasta    { get; set; }
        public string? Tipo          { get; set; }
        public string? Usuario       { get; set; }
        public ExportSecciones? Secciones { get; set; }
    }
}
