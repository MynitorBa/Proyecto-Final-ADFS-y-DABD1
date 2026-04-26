using ClosedXML.Excel;
using OfficeOpenXml;
using OfficeOpenXml.Style;
using OfficeOpenXml.Drawing.Chart;
using Aerolinea.API.DTOs;
using System.IO.Compression;
using System.Text;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Genera archivos Excel (.xlsx) y ZIP de CSVs con las métricas del panel administrativo.
    /// El Excel se genera con EPPlus (incluye gráficos reales).
    /// Los CSVs se generan con ClosedXML-independiente (texto plano).
    /// </summary>
    public static class MetricasExportHelper
    {
        // ── Paleta de colores ─────────────────────────────────────────────────
        private static readonly System.Drawing.Color Gold   = System.Drawing.Color.FromArgb(212, 175, 55);
        private static readonly System.Drawing.Color Dark   = System.Drawing.Color.FromArgb(28,  26,  24);
        private static readonly System.Drawing.Color Light  = System.Drawing.Color.FromArgb(242, 239, 234);
        private static readonly System.Drawing.Color RowAlt = System.Drawing.Color.FromArgb(255, 253, 247);
        private static readonly System.Drawing.Color Gray   = System.Drawing.Color.FromArgb(107, 114, 128);
        private static readonly System.Drawing.Color Blue   = System.Drawing.Color.FromArgb(59,  130, 246);
        private static readonly System.Drawing.Color Red    = System.Drawing.Color.FromArgb(220, 38,  38);
        private static readonly System.Drawing.Color Purple = System.Drawing.Color.FromArgb(124, 58,  237);
        private static readonly System.Drawing.Color White  = System.Drawing.Color.White;

        // ══════════════════════════════════════════════════════════════════════
        // Excel principal (EPPlus — soporta gráficos)
        // ══════════════════════════════════════════════════════════════════════

        public static byte[] GenerarExcel(
            MetricasResumenDTO?    resumen,
            NegocioMetricasDTO?    negocio,
            ListadoBusquedasDTO?   listado,
            ExportSecciones        sec,
            string                 periodo)
        {
            using var package = new ExcelPackage();
            package.Workbook.Properties.Title  = "Métricas Broom AirLine";
            package.Workbook.Properties.Author = "Broom AirLine Admin";

            if (sec.Kpi             && resumen != null) HojaKpi(package, resumen, periodo);
            if (sec.BusquedasDiarias && resumen != null) HojaBusquedasDiarias(package, resumen.BusquedasPorDia ?? new(), periodo);
            if (sec.Canal           && resumen != null) HojaCanal(package, resumen.BusquedasPorTipo ?? new(), periodo);
            if (sec.Embudo          && negocio != null) HojaEmbudo(package, negocio.Embudo, periodo);
            if (sec.Rutas           && negocio != null) HojaRutas(package, negocio.RutasRendimiento ?? new(), periodo);
            if (sec.Cancelaciones   && negocio != null) HojaCancelaciones(package, negocio.Cancelaciones, periodo);
            if (sec.Tendencia       && negocio != null) HojaTendencia(package, negocio.IngresosTendencia ?? new(), periodo);
            if (sec.Heatmap         && negocio != null) HojaHeatmap(package, negocio.Heatmap ?? new(), periodo);
            if (sec.Registro        && listado != null) HojaRegistro(package, listado.Registros ?? new(), periodo);

            return package.GetAsByteArray();
        }

        // ══════════════════════════════════════════════════════════════════════
        // ZIP de CSVs (sin dependencia de EPPlus ni ClosedXML)
        // ══════════════════════════════════════════════════════════════════════

        public static byte[] GenerarCsvZip(
            MetricasResumenDTO?  resumen,
            NegocioMetricasDTO?  negocio,
            ListadoBusquedasDTO? listado,
            ExportSecciones      sec,
            string               periodo)
        {
            using var ms  = new MemoryStream();
            using var zip = new ZipArchive(ms, ZipArchiveMode.Create, true);

            void Add(string nombre, string csv)
            {
                var entry = zip.CreateEntry(nombre, System.IO.Compression.CompressionLevel.Optimal);
                using var sw = new StreamWriter(entry.Open(), Encoding.UTF8);
                sw.Write(csv);
            }

            if (sec.Kpi             && resumen != null) Add("kpi_ingresos.csv",      CsvKpi(resumen, periodo));
            if (sec.BusquedasDiarias && resumen != null) Add("busquedas_diarias.csv", CsvBusquedasDiarias(resumen.BusquedasPorDia ?? new()));
            if (sec.Canal           && resumen != null) Add("canal_busquedas.csv",    CsvCanal(resumen.BusquedasPorTipo ?? new()));
            if (sec.Embudo          && negocio != null) Add("embudo_conversion.csv",  CsvEmbudo(negocio.Embudo));
            if (sec.Rutas           && negocio != null) Add("rutas_rendimiento.csv",  CsvRutas(negocio.RutasRendimiento ?? new()));
            if (sec.Cancelaciones   && negocio != null) Add("cancelaciones.csv",      CsvCancelaciones(negocio.Cancelaciones));
            if (sec.Tendencia       && negocio != null) Add("tendencia_ingresos.csv", CsvTendencia(negocio.IngresosTendencia ?? new()));
            if (sec.Heatmap         && negocio != null) Add("mapa_busquedas.csv",     CsvHeatmap(negocio.Heatmap ?? new()));
            if (sec.Registro        && listado != null) Add("registro_busquedas.csv", CsvRegistro(listado.Registros ?? new()));

            zip.Dispose();
            return ms.ToArray();
        }

        // ══════════════════════════════════════════════════════════════════════
        // HELPERS EPPlus
        // ══════════════════════════════════════════════════════════════════════

        private static ExcelWorksheet CrearHoja(ExcelPackage pkg, string nombre)
        {
            var ws = pkg.Workbook.Worksheets.Add(nombre);
            ws.TabColor = Gold;
            return ws;
        }

        /// <summary>Título principal (fila 1) y subtítulo de período (fila 2).</summary>
        private static void Titulo(ExcelWorksheet ws, string titulo, string periodo, int cols)
        {
            ws.Cells[1, 1, 1, cols].Merge = true;
            ws.Cells[1, 1].Value = titulo;
            ws.Cells[1, 1].Style.Font.Bold = true;
            ws.Cells[1, 1].Style.Font.Size = 14;
            ws.Cells[1, 1].Style.Font.Color.SetColor(Gold);
            ws.Cells[1, 1].Style.Fill.PatternType = ExcelFillStyle.Solid;
            ws.Cells[1, 1].Style.Fill.BackgroundColor.SetColor(Dark);
            ws.Row(1).Height = 24;

            ws.Cells[2, 1, 2, cols].Merge = true;
            ws.Cells[2, 1].Value = $"Período: {periodo}";
            ws.Cells[2, 1].Style.Font.Italic = true;
            ws.Cells[2, 1].Style.Font.Color.SetColor(Gray);
            ws.Row(2).Height = 16;
        }

        /// <summary>Fila de encabezados con fondo dorado y texto oscuro.</summary>
        private static void Header(ExcelWorksheet ws, int fila, params string[] columnas)
        {
            for (int i = 0; i < columnas.Length; i++)
            {
                var c = ws.Cells[fila, i + 1];
                c.Value = columnas[i];
                c.Style.Font.Bold = true;
                c.Style.Font.Color.SetColor(Dark);
                c.Style.Fill.PatternType = ExcelFillStyle.Solid;
                c.Style.Fill.BackgroundColor.SetColor(Gold);
                c.Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                c.Style.Border.Bottom.Style = ExcelBorderStyle.Medium;
                c.Style.Border.Bottom.Color.SetColor(Dark);
            }
            ws.Row(fila).Height = 20;
        }

        /// <summary>Colorea una fila de datos con color alternado.</summary>
        private static void ApplyAltRow(ExcelWorksheet ws, int fila, int cols, bool aplicar)
        {
            if (!aplicar) return;
            for (int c = 1; c <= cols; c++)
            {
                ws.Cells[fila, c].Style.Fill.PatternType = ExcelFillStyle.Solid;
                ws.Cells[fila, c].Style.Fill.BackgroundColor.SetColor(RowAlt);
            }
        }

        /// <summary>Ajusta el ancho de todas las columnas usadas.</summary>
        private static void AutoFit(ExcelWorksheet ws) => ws.Cells[ws.Dimension?.Address ?? "A1"].AutoFitColumns();

        /// <summary>
        /// Aplica formato de tabla (cabecera dorado+oscuro, filas alternas, borde) a un rango auxiliar.
        /// headerRow: fila del encabezado (1-based). startCol: columna inicial (1-based).
        /// cols: numero de columnas. dataRows: numero de filas de datos bajo el encabezado.
        /// </summary>
        private static void EstilarAuxTabla(ExcelWorksheet ws, int headerRow, int startCol, int cols, int dataRows)
        {
            // Encabezado
            for (int c = startCol; c < startCol + cols; c++)
            {
                var cell = ws.Cells[headerRow, c];
                cell.Style.Fill.PatternType = ExcelFillStyle.Solid;
                cell.Style.Fill.BackgroundColor.SetColor(Dark);
                cell.Style.Font.Color.SetColor(Gold);
                cell.Style.Font.Bold = true;
                cell.Style.Font.Size = 9;
                cell.Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                cell.Style.Border.Bottom.Style = ExcelBorderStyle.Medium;
                cell.Style.Border.Bottom.Color.SetColor(Gold);
            }
            ws.Row(headerRow).Height = 18;

            // Filas de datos
            for (int r = 0; r < dataRows; r++)
            {
                int fila = headerRow + 1 + r;
                bool alt = r % 2 == 1;
                for (int c = startCol; c < startCol + cols; c++)
                {
                    var cell = ws.Cells[fila, c];
                    cell.Style.Fill.PatternType = ExcelFillStyle.Solid;
                    cell.Style.Fill.BackgroundColor.SetColor(alt ? RowAlt : White);
                    cell.Style.Font.Size = 9;
                    cell.Style.HorizontalAlignment = c == startCol
                        ? ExcelHorizontalAlignment.Left
                        : ExcelHorizontalAlignment.Right;
                }
            }

            // Borde exterior del rango completo
            if (dataRows > 0)
            {
                int lastRow = headerRow + dataRows;
                int lastCol = startCol + cols - 1;
                var rng = ws.Cells[headerRow, startCol, lastRow, lastCol];
                rng.Style.Border.BorderAround(ExcelBorderStyle.Thin, System.Drawing.Color.FromArgb(229, 217, 198));
                // Separador vertical entre columnas
                for (int c = startCol; c < lastCol; c++)
                    ws.Cells[headerRow, c, lastRow, c].Style.Border.Right.Style = ExcelBorderStyle.Hair;
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 1 — KPI Ingresos  (Pie chart: Turista vs Ejecutivo)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaKpi(ExcelPackage pkg, MetricasResumenDTO r, string periodo)
        {
            var ws = CrearHoja(pkg, "KPI Ingresos");
            var kpi = r.IngresosKpi ?? new IngresosKpiDTO();
            decimal ingTotal = kpi.IngresosTurista + kpi.IngresosEjecutivo;

            Titulo(ws, "KPI de Ingresos", periodo, 4);
            Header(ws, 4, "Métrica", "Valor");

            var filas = new[]
            {
                ("Ingresos Totales",         $"${ingTotal:N2}"),
                ("Ingresos Turista",          $"${kpi.IngresosTurista:N2}"),
                ("Ingresos Ejecutivo",        $"${kpi.IngresosEjecutivo:N2}"),
                ("Ticket Promedio",           $"${kpi.TicketPromedio:N2}"),
                ("Total Boletos",            kpi.TotalBoletos.ToString("N0")),
                ("Reservaciones Pagadas",    kpi.TotalReservaciones.ToString("N0")),
                ("Total Búsquedas",          r.TotalBusquedas.ToString("N0")),
                ("Búsquedas Web",            r.TotalBusquedasWeb.ToString("N0")),
                ("Búsquedas REST / Agencia", r.TotalBusquedasRest.ToString("N0")),
            };

            var colorTuristaFondo  = System.Drawing.Color.FromArgb(209, 250, 229);
            var colorTuristaTexto  = System.Drawing.Color.FromArgb(6,   95,  70);
            var colorEjecFondo     = System.Drawing.Color.FromArgb(237, 233, 254);
            var colorEjecTexto     = System.Drawing.Color.FromArgb(91,  33, 182);

            for (int i = 0; i < filas.Length; i++)
            {
                int f = i + 5;
                ws.Cells[f, 1].Value = filas[i].Item1;
                ws.Cells[f, 2].Value = filas[i].Item2;
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Right;

                if (i == 1) // Ingresos Turista
                {
                    foreach (int col in new[] { 1, 2 })
                    {
                        ws.Cells[f, col].Style.Fill.PatternType = ExcelFillStyle.Solid;
                        ws.Cells[f, col].Style.Fill.BackgroundColor.SetColor(colorTuristaFondo);
                        ws.Cells[f, col].Style.Font.Color.SetColor(colorTuristaTexto);
                        ws.Cells[f, col].Style.Font.Bold = true;
                    }
                }
                else if (i == 2) // Ingresos Ejecutivo
                {
                    foreach (int col in new[] { 1, 2 })
                    {
                        ws.Cells[f, col].Style.Fill.PatternType = ExcelFillStyle.Solid;
                        ws.Cells[f, col].Style.Fill.BackgroundColor.SetColor(colorEjecFondo);
                        ws.Cells[f, col].Style.Font.Color.SetColor(colorEjecTexto);
                        ws.Cells[f, col].Style.Font.Bold = true;
                    }
                }
                else
                {
                    ApplyAltRow(ws, f, 2, i % 2 == 1);
                }
            }

            ws.Column(1).Width = 28;
            ws.Column(2).Width = 18;

            // ── Datos auxiliares para el pie chart (filas 14-15, col D-E, ocultas pero referenciadas)
            // Turista en E14, Ejecutivo en E15; etiquetas en D14-D15
            ws.Cells[14, 4].Value = "Turista";
            ws.Cells[15, 4].Value = "Ejecutivo";
            ws.Cells[14, 5].Value = (double)kpi.IngresosTurista;
            ws.Cells[15, 5].Value = (double)kpi.IngresosEjecutivo;

            // ── Pie chart — a la derecha de la tabla, sin solapar aux data (D-E) ─
            var pie = (ExcelPieChart)ws.Drawings.AddChart("PieKpi", eChartType.Pie);
            pie.Title.Text = "Distribución de Ingresos";
            pie.SetPosition(3, 0, 6, 0);   // fila 4 (header), col G (0-based=6)
            pie.SetSize(380, 280);
            pie.Style = eChartStyle.Style26;

            var pieSerie = pie.Series.Add(ws.Cells[14, 5, 15, 5], ws.Cells[14, 4, 15, 4]);
            pieSerie.Header = "Ingresos";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 2 — Búsquedas Diarias  (Bar chart)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaBusquedasDiarias(ExcelPackage pkg, List<BusquedasPorDiaDTO> datos, string periodo)
        {
            var ws = CrearHoja(pkg, "Búsquedas Diarias");
            Titulo(ws, "Búsquedas por Día", periodo, 2);
            Header(ws, 4, "Fecha", "Total");

            int n = datos.Count;
            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                ws.Cells[f, 1].Value = datos[i].Fecha;
                ws.Cells[f, 2].Value = datos[i].Total;
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 2, i % 2 == 1);
            }

            // Fila total
            int totalF = n + 5;
            ws.Cells[totalF, 1].Value = "TOTAL";
            ws.Cells[totalF, 1].Style.Font.Bold = true;
            ws.Cells[totalF, 2].Value = datos.Sum(d => d.Total);
            ws.Cells[totalF, 2].Style.Font.Bold = true;
            ws.Cells[totalF, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;

            ws.Column(1).Width = 16;
            ws.Column(2).Width = 12;

            if (n == 0) return;

            // ── Bar chart — a la derecha de la tabla (cols A-B) ─────────────
            var bar = (ExcelBarChart)ws.Drawings.AddChart("BarBusquedas", eChartType.BarClustered);
            bar.Title.Text = "Búsquedas por Día";
            bar.SetPosition(3, 0, 3, 0);   // fila 4 (header), col D (0-based=3)
            bar.SetSize(500, 300);
            bar.Style = eChartStyle.Style26;

            var serie = bar.Series.Add(ws.Cells[5, 2, n + 4, 2], ws.Cells[5, 1, n + 4, 1]);
            serie.Header = "Búsquedas";
            bar.XAxis.Title.Text = "Fecha";
            bar.YAxis.Title.Text = "Total";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 3 — Canal de Búsquedas  (Doughnut chart)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaCanal(ExcelPackage pkg, List<BusquedasPorTipoDTO> datos, string periodo)
        {
            var ws = CrearHoja(pkg, "Canal de Búsquedas");
            Titulo(ws, "Búsquedas por Canal", periodo, 3);
            Header(ws, 4, "Canal", "Total", "% del Total");

            int total = datos.Sum(d => d.Total);
            int n = datos.Count;

            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                double pct = total > 0 ? (double)datos[i].Total / total * 100 : 0;
                ws.Cells[f, 1].Value = datos[i].Tipo;
                ws.Cells[f, 2].Value = datos[i].Total;
                ws.Cells[f, 3].Value = $"{pct:F1}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 3, i % 2 == 1);
            }

            ws.Column(1).Width = 22;
            ws.Column(2).Width = 12;
            ws.Column(3).Width = 14;

            if (n == 0) return;

            // ── Doughnut chart ───────────────────────────────────────────────
            var donut = (ExcelDoughnutChart)ws.Drawings.AddChart("DonutCanal", eChartType.DoughnutExploded);
            donut.Title.Text = "Distribución por Canal";
            donut.SetPosition(3, 0, 4, 0);
            donut.SetSize(380, 300);
            donut.Style = eChartStyle.Style26;

            var serie = donut.Series.Add(ws.Cells[5, 2, n + 4, 2], ws.Cells[5, 1, n + 4, 1]);
            serie.Header = "Búsquedas";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 4 — Embudo Conversión  (Bar chart horizontal)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaEmbudo(ExcelPackage pkg, EmbudoNegocioDTO emb, string periodo)
        {
            var ws = CrearHoja(pkg, "Embudo Conversión");
            Titulo(ws, "Embudo de Conversión: Reservación → Pago", periodo, 3);
            Header(ws, 4, "Etapa", "Total", "% del total");

            int baseEmb = emb == null ? 1 : Math.Max(
                emb.Completadas + emb.Pagadas + emb.Pendientes + emb.Expiradas + emb.Canceladas, 1);
            var etapas = emb == null ? Array.Empty<(string, int)>() : new[]
            {
                ("Completadas (ya volaron)",  emb.Completadas),
                ("Pagadas (confirmadas)",     emb.Pagadas),
                ("Pendientes de pago",        emb.Pendientes),
                ("Expiradas sin pagar",       emb.Expiradas),
                ("Canceladas",               emb.Canceladas),
            };

            for (int i = 0; i < etapas.Length; i++)
            {
                int f = i + 5;
                double pct = (double)etapas[i].Item2 / baseEmb * 100;
                ws.Cells[f, 1].Value = etapas[i].Item1;
                ws.Cells[f, 2].Value = etapas[i].Item2;
                ws.Cells[f, 3].Value = $"{pct:F1}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 3, i % 2 == 1);
            }

            ws.Column(1).Width = 28;
            ws.Column(2).Width = 12;
            ws.Column(3).Width = 20;

            if (etapas.Length == 0) return;

            // ── Bar chart — a la derecha de la tabla (cols A-C) ─────────────
            var bar = (ExcelBarChart)ws.Drawings.AddChart("BarEmbudo", eChartType.BarClustered);
            bar.Title.Text = "Embudo de Conversión";
            bar.SetPosition(3, 0, 4, 0);   // fila 4 (header), col E (0-based=4)
            bar.SetSize(480, 320);
            bar.Style = eChartStyle.Style26;

            var serie = bar.Series.Add(ws.Cells[5, 2, etapas.Length + 4, 2], ws.Cells[5, 1, etapas.Length + 4, 1]);
            serie.Header = "Total";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 5 — Rutas  (Bar chart por búsquedas)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaRutas(ExcelPackage pkg, List<RutaRendimientoDTO> datos, string periodo)
        {
            var ws = CrearHoja(pkg, "Rutas");
            Titulo(ws, "Rendimiento de Rutas", periodo, 6);
            Header(ws, 4, "Ruta", "Búsquedas", "Reservaciones", "Boletos", "Ingresos", "% del Total");

            decimal totalIng = datos.Sum(r => r.RevenueTotal);
            int n = datos.Count;

            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                var d = datos[i];
                double pct = totalIng > 0 ? (double)d.RevenueTotal / (double)totalIng * 100 : 0;
                ws.Cells[f, 1].Value = d.Ruta;
                ws.Cells[f, 2].Value = d.Busquedas;
                ws.Cells[f, 3].Value = d.TotalReservaciones;
                ws.Cells[f, 4].Value = d.BoletosVendidos;
                ws.Cells[f, 5].Value = (double)d.RevenueTotal;
                ws.Cells[f, 5].Style.Numberformat.Format = "$#,##0.00";
                ws.Cells[f, 6].Value = $"{pct:F1}%";
                for (int c = 2; c <= 6; c++)
                    ws.Cells[f, c].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 6, i % 2 == 1);
            }

            // Fila total
            int totalF = n + 5;
            ws.Cells[totalF, 1].Value = "TOTAL";
            ws.Cells[totalF, 1].Style.Font.Bold = true;
            ws.Cells[totalF, 5].Value = (double)totalIng;
            ws.Cells[totalF, 5].Style.Font.Bold = true;
            ws.Cells[totalF, 5].Style.Numberformat.Format = "$#,##0.00";
            ws.Cells[totalF, 6].Value = "100%";
            ws.Cells[totalF, 6].Style.Font.Bold = true;

            ws.Column(1).Width = 26;
            for (int c = 2; c <= 6; c++) ws.Column(c).Width = 15;

            if (n == 0) return;

            // ── Bar chart — a la derecha de la tabla (cols A-F) ─────────────
            var bar = (ExcelBarChart)ws.Drawings.AddChart("BarRutas", eChartType.BarClustered);
            bar.Title.Text = "Búsquedas por Ruta";
            bar.SetPosition(3, 0, 7, 0);   // fila 4 (header), col H (0-based=7)
            bar.SetSize(480, 300);
            bar.Style = eChartStyle.Style26;

            var serie = bar.Series.Add(ws.Cells[5, 2, n + 4, 2], ws.Cells[5, 1, n + 4, 1]);
            serie.Header = "Búsquedas";
            bar.XAxis.Title.Text = "Ruta";
            bar.YAxis.Title.Text = "Búsquedas";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 6 — Cancelaciones  (Pie chart: quién canceló)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaCancelaciones(ExcelPackage pkg, CancelacionesAnalisisDTO canc, string periodo)
        {
            var ws = CrearHoja(pkg, "Cancelaciones");
            Titulo(ws, "Análisis de Cancelaciones", periodo, 3);

            var porRuta        = canc?.PorRuta        ?? new();
            var porTipo        = canc?.PorTipo        ?? new();
            var porAnticipacion = canc?.PorAnticipacion ?? new();

            int f = 4;

            // — Sub-sección: Rutas más afectadas —
            ws.Cells[f, 1].Value = "Rutas más afectadas";
            ws.Cells[f, 1].Style.Font.Bold = true;
            f++;
            Header(ws, f, "Ruta", "Total", "% del Total"); f++;
            int trRuta = porRuta.Sum(r => r.Total);
            foreach (var r in porRuta)
            {
                double pct = trRuta > 0 ? (double)r.Total / trRuta * 100 : 0;
                ws.Cells[f, 1].Value = $"{r.OrigenCodigo} → {r.DestinoCodigo}";
                ws.Cells[f, 2].Value = r.Total;
                ws.Cells[f, 3].Value = $"{pct:F1}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                f++;
            }
            f++;

            // — Sub-sección: Quién canceló —
            int tipoStartRow = f;
            ws.Cells[f, 1].Value = "Quién canceló";
            ws.Cells[f, 1].Style.Font.Bold = true;
            f++;
            Header(ws, f, "Tipo", "Total", "% del Total"); f++;
            int tipoDataStart = f;
            int tt = porTipo.Sum(t => t.Total);
            foreach (var t in porTipo)
            {
                double pct = tt > 0 ? (double)t.Total / tt * 100 : 0;
                ws.Cells[f, 1].Value = t.Tipo;
                ws.Cells[f, 2].Value = t.Total;
                ws.Cells[f, 3].Value = $"{pct:F1}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                f++;
            }
            int tipoDataEnd = f - 1;
            f++;

            // — Sub-sección: Anticipación —
            ws.Cells[f, 1].Value = "Anticipación al vuelo";
            ws.Cells[f, 1].Style.Font.Bold = true;
            f++;
            Header(ws, f, "Bucket", "Total", "% del Total"); f++;
            int ta = porAnticipacion.Sum(a => a.Total);
            foreach (var a in porAnticipacion)
            {
                double pct = ta > 0 ? (double)a.Total / ta * 100 : 0;
                ws.Cells[f, 1].Value = a.Bucket;
                ws.Cells[f, 2].Value = a.Total;
                ws.Cells[f, 3].Value = $"{pct:F1}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                f++;
            }

            ws.Column(1).Width = 30;
            ws.Column(2).Width = 12;
            ws.Column(3).Width = 14;

            // ── Pie chart: quién canceló ─────────────────────────────────────
            if (porTipo.Count > 0)
            {
                var pie = (ExcelPieChart)ws.Drawings.AddChart("PieCancelaciones", eChartType.Pie);
                pie.Title.Text = "Cancelaciones por Actor";
                pie.SetPosition(3, 0, 4, 0);
                pie.SetSize(380, 280);
                pie.Style = eChartStyle.Style26;

                var serie = pie.Series.Add(
                    ws.Cells[tipoDataStart, 2, tipoDataEnd, 2],
                    ws.Cells[tipoDataStart, 1, tipoDataEnd, 1]);
                serie.Header = "Total";
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 7 — Tendencia Ingresos  (Line chart)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaTendencia(ExcelPackage pkg, List<IngresosMensualDTO> datos, string periodo)
        {
            var ws = CrearHoja(pkg, "Tendencia Ingresos");
            Titulo(ws, "Tendencia de Ingresos por Clase", periodo, 4);
            Header(ws, 4, "Mes", "Clase", "Ingresos", "Reservaciones");

            int n = datos.Count;
            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                var d = datos[i];
                ws.Cells[f, 1].Value = d.Mes;
                ws.Cells[f, 2].Value = d.Clase;
                ws.Cells[f, 3].Value = (double)d.Revenue;
                ws.Cells[f, 3].Style.Numberformat.Format = "$#,##0.00";
                ws.Cells[f, 4].Value = d.Reservaciones;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Right;
                ws.Cells[f, 4].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 4, i % 2 == 1);
            }

            ws.Column(1).Width = 14;
            ws.Column(2).Width = 14;
            ws.Column(3).Width = 16;
            ws.Column(4).Width = 16;

            if (n == 0) return;

            // Construimos series separadas por clase para el line chart
            // Turista
            var turista   = datos.Where(d => d.Clase == "Turista").ToList();
            var ejecutivo = datos.Where(d => d.Clase == "Ejecutivo").ToList();

            // Datos auxiliares: cols G-I para turista, cols K-M para ejecutivo (0-based col 6, 10)
            int auxRow = 5;
            int tCol = 7; // columna G (Mes_T, Turista)
            int eCol = 9; // columna I (Mes_E, Ejecutivo) — sin columna vacía entre H y I

            ws.Cells[4, tCol].Value = "Mes_T";
            ws.Cells[4, tCol + 1].Value = "Turista";
            for (int i = 0; i < turista.Count; i++)
            {
                ws.Cells[auxRow + i, tCol].Value = turista[i].Mes;
                ws.Cells[auxRow + i, tCol + 1].Value = (double)turista[i].Revenue;
            }

            ws.Cells[4, eCol].Value = "Mes_E";
            ws.Cells[4, eCol + 1].Value = "Ejecutivo";
            for (int i = 0; i < ejecutivo.Count; i++)
            {
                ws.Cells[auxRow + i, eCol].Value = ejecutivo[i].Mes;
                ws.Cells[auxRow + i, eCol + 1].Value = (double)ejecutivo[i].Revenue;
                ws.Cells[auxRow + i, eCol + 1].Style.Numberformat.Format = "$#,##0.00";
            }

            // Formatear columnas aux como tablas coloreadas
            if (turista.Count > 0)
            {
                EstilarAuxTabla(ws, 4, tCol, 2, turista.Count);
                ws.Column(tCol).Width     = 14;
                ws.Column(tCol + 1).Width = 16;
            }
            if (ejecutivo.Count > 0)
            {
                EstilarAuxTabla(ws, 4, eCol, 2, ejecutivo.Count);
                ws.Column(eCol).Width     = 14;
                ws.Column(eCol + 1).Width = 16;
            }

            // ── Line chart — a la derecha de la tabla (cols A-D) y aux (G-J) ─
            // Tabla: cols A-D (0-3). Aux: G-J (6-9). Chart arranca en col L (0-based=11)
            var line = (ExcelLineChart)ws.Drawings.AddChart("LineTendencia", eChartType.Line);
            line.Title.Text = "Tendencia de Ingresos por Clase";
            line.SetPosition(3, 0, 11, 0);  // fila 4 (header), col L (0-based=11)
            line.SetSize(520, 300);
            line.Style = eChartStyle.Style26;

            if (turista.Count > 0)
            {
                var s1 = line.Series.Add(
                    ws.Cells[auxRow, tCol + 1, auxRow + turista.Count - 1, tCol + 1],
                    ws.Cells[auxRow, tCol,     auxRow + turista.Count - 1, tCol]);
                s1.Header = "Turista";
            }
            if (ejecutivo.Count > 0)
            {
                var s2 = line.Series.Add(
                    ws.Cells[auxRow, eCol + 1, auxRow + ejecutivo.Count - 1, eCol + 1],
                    ws.Cells[auxRow, eCol,     auxRow + ejecutivo.Count - 1, eCol]);
                s2.Header = "Ejecutivo";
            }

            line.XAxis.Title.Text  = "Mes";
            line.YAxis.Title.Text  = "Ingresos ($)";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 8 — Mapa de Búsquedas  (Bar chart por día de semana)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaHeatmap(ExcelPackage pkg, List<HeatmapCeldaDTO> datos, string periodo)
        {
            var ws = CrearHoja(pkg, "Mapa de Búsquedas");
            Titulo(ws, "Mapa de Calor de Búsquedas (Día × Hora)", periodo, 4);
            Header(ws, 4, "Día", "Hora", "Asientos Vendidos", "% Ocupación");

            var dias = new Dictionary<int, string>
            {
                {1,"Dom"}, {2,"Lun"}, {3,"Mar"}, {4,"Mié"}, {5,"Jue"}, {6,"Vie"}, {7,"Sáb"}
            };

            int n = datos.Count;
            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                var d = datos[i];
                ws.Cells[f, 1].Value = dias.GetValueOrDefault(d.DiaSemana, $"Día {d.DiaSemana}");
                ws.Cells[f, 2].Value = $"{d.Hora}:00";
                ws.Cells[f, 3].Value = d.AsientosVendidos;
                ws.Cells[f, 4].Value = $"{d.OcupacionPct:F0}%";
                ws.Cells[f, 2].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 3].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ws.Cells[f, 4].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 4, i % 2 == 1);
            }

            ws.Column(1).Width = 10;
            ws.Column(2).Width = 10;
            ws.Column(3).Width = 20;
            ws.Column(4).Width = 14;

            if (n == 0) return;

            // Agregamos totales por día para el chart (cols F-G)
            var porDia = datos
                .GroupBy(d => d.DiaSemana)
                .OrderBy(g => g.Key)
                .Select(g => (Dia: dias.GetValueOrDefault(g.Key, $"D{g.Key}"), Total: g.Sum(x => x.AsientosVendidos)))
                .ToList();

            int auxStart = 5;
            int diaCol   = 6; // col F
            ws.Cells[4, diaCol].Value = "Día";
            ws.Cells[4, diaCol + 1].Value = "Total";
            for (int i = 0; i < porDia.Count; i++)
            {
                ws.Cells[auxStart + i, diaCol].Value     = porDia[i].Dia;
                ws.Cells[auxStart + i, diaCol + 1].Value = porDia[i].Total;
            }

            // Formatear columnas aux como tabla coloreada
            if (porDia.Count > 0)
            {
                EstilarAuxTabla(ws, 4, diaCol, 2, porDia.Count);
                ws.Column(diaCol).Width     = 10;
                ws.Column(diaCol + 1).Width = 10;
            }

            // ── Bar chart — a la derecha de la tabla (cols A-D) y aux (F-G) ──
            // Tabla: cols A-D (0-3). Aux: F-G (5-6). Chart arranca en col I (0-based=8)
            var bar = (ExcelBarChart)ws.Drawings.AddChart("BarHeatmap", eChartType.BarClustered);
            bar.Title.Text = "Búsquedas por Día de Semana";
            bar.SetPosition(3, 0, 8, 0);    // fila 4 (header), col I (0-based=8)
            bar.SetSize(460, 280);
            bar.Style = eChartStyle.Style26;

            var serie = bar.Series.Add(
                ws.Cells[auxStart, diaCol + 1, auxStart + porDia.Count - 1, diaCol + 1],
                ws.Cells[auxStart, diaCol,     auxStart + porDia.Count - 1, diaCol]);
            serie.Header = "Asientos";
            bar.XAxis.Title.Text = "Día";
            bar.YAxis.Title.Text = "Total";
        }

        // ══════════════════════════════════════════════════════════════════════
        // HOJA 9 — Registro Búsquedas  (datos planos, sin gráfico)
        // ══════════════════════════════════════════════════════════════════════
        private static void HojaRegistro(ExcelPackage pkg, List<BusquedaDetalleDTO> registros, string periodo)
        {
            var ws = CrearHoja(pkg, "Registro Búsquedas");
            Titulo(ws, "Registro Completo de Búsquedas", periodo, 7);
            Header(ws, 4, "ID", "Ruta", "Fecha Salida", "Pasajeros", "Usuario", "Canal", "Fecha Búsqueda");

            int n = registros.Count;
            for (int i = 0; i < n; i++)
            {
                int f = i + 5;
                var r = registros[i];
                ws.Cells[f, 1].Value = r.Id;
                ws.Cells[f, 2].Value = $"{r.OrigenCodigo} → {r.DestinoCodigo}";
                ws.Cells[f, 3].Value = r.FechaSalida;
                ws.Cells[f, 4].Value = r.CantidadPersonas;
                ws.Cells[f, 5].Value = r.Usuario ?? "No registrado";
                ws.Cells[f, 6].Value = r.Tipo;
                ws.Cells[f, 7].Value = r.FechaBusqueda;
                for (int c = 1; c <= 7; c++)
                    ws.Cells[f, c].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                ApplyAltRow(ws, f, 7, i % 2 == 1);
            }

            ws.Column(1).Width = 8;
            ws.Column(2).Width = 22;
            ws.Column(3).Width = 14;
            ws.Column(4).Width = 12;
            ws.Column(5).Width = 20;
            ws.Column(6).Width = 12;
            ws.Column(7).Width = 18;
        }

        // ══════════════════════════════════════════════════════════════════════
        // GENERADORES CSV
        // ══════════════════════════════════════════════════════════════════════

        private static string CsvKpi(MetricasResumenDTO r, string periodo)
        {
            var kpi = r.IngresosKpi ?? new IngresosKpiDTO();
            decimal tot = kpi.IngresosTurista + kpi.IngresosEjecutivo;
            var sb = new StringBuilder();
            sb.AppendLine($"KPI Ingresos — Período: {periodo}");
            sb.AppendLine("Métrica,Valor");
            sb.AppendLine($"Ingresos Totales,{tot:N2}");
            sb.AppendLine($"Ingresos Turista,{kpi.IngresosTurista:N2}");
            sb.AppendLine($"Ingresos Ejecutivo,{kpi.IngresosEjecutivo:N2}");
            sb.AppendLine($"Ticket Promedio,{kpi.TicketPromedio:N2}");
            sb.AppendLine($"Total Boletos,{kpi.TotalBoletos}");
            sb.AppendLine($"Reservaciones Pagadas,{kpi.TotalReservaciones}");
            sb.AppendLine($"Total Búsquedas,{r.TotalBusquedas}");
            return sb.ToString();
        }

        private static string CsvBusquedasDiarias(List<BusquedasPorDiaDTO> datos)
        {
            var sb = new StringBuilder();
            sb.AppendLine("Fecha,Total");
            foreach (var d in datos) sb.AppendLine($"{d.Fecha},{d.Total}");
            sb.AppendLine($"TOTAL,{datos.Sum(d => d.Total)}");
            return sb.ToString();
        }

        private static string CsvCanal(List<BusquedasPorTipoDTO> datos)
        {
            var sb = new StringBuilder();
            int total = datos.Sum(d => d.Total);
            sb.AppendLine("Canal,Total,% del Total");
            foreach (var d in datos)
            {
                double pct = total > 0 ? (double)d.Total / total * 100 : 0;
                sb.AppendLine($"{d.Tipo},{d.Total},{pct:F1}%");
            }
            return sb.ToString();
        }

        private static string CsvEmbudo(EmbudoNegocioDTO? e)
        {
            if (e == null) return "Etapa,Total\n";
            var sb = new StringBuilder();
            int b = Math.Max(e.Completadas + e.Pagadas + e.Pendientes + e.Expiradas + e.Canceladas, 1);
            sb.AppendLine("Etapa,Total,% del total");
            sb.AppendLine($"Completadas,{e.Completadas},{(double)e.Completadas/b*100:F1}%");
            sb.AppendLine($"Pagadas,{e.Pagadas},{(double)e.Pagadas/b*100:F1}%");
            sb.AppendLine($"Pendientes,{e.Pendientes},{(double)e.Pendientes/b*100:F1}%");
            sb.AppendLine($"Expiradas,{e.Expiradas},{(double)e.Expiradas/b*100:F1}%");
            sb.AppendLine($"Canceladas,{e.Canceladas},{(double)e.Canceladas/b*100:F1}%");
            return sb.ToString();
        }

        private static string CsvRutas(List<RutaRendimientoDTO> datos)
        {
            var sb = new StringBuilder();
            decimal tot = datos.Sum(r => r.RevenueTotal);
            sb.AppendLine("Ruta,Búsquedas,Reservaciones,Boletos,Ingresos,% del Total");
            foreach (var d in datos)
            {
                double pct = tot > 0 ? (double)d.RevenueTotal / (double)tot * 100 : 0;
                sb.AppendLine($"{d.Ruta},{d.Busquedas},{d.TotalReservaciones},{d.BoletosVendidos},{d.RevenueTotal:N2},{pct:F1}%");
            }
            return sb.ToString();
        }

        private static string CsvCancelaciones(CancelacionesAnalisisDTO? c)
        {
            if (c == null) return "";
            var sb = new StringBuilder();
            sb.AppendLine("=== Rutas más afectadas ===");
            sb.AppendLine("Ruta,Total");
            foreach (var r in c.PorRuta) sb.AppendLine($"{r.OrigenCodigo}→{r.DestinoCodigo},{r.Total}");
            sb.AppendLine();
            sb.AppendLine("=== Quién canceló ===");
            sb.AppendLine("Tipo,Total");
            foreach (var t in c.PorTipo) sb.AppendLine($"{t.Tipo},{t.Total}");
            sb.AppendLine();
            sb.AppendLine("=== Anticipación ===");
            sb.AppendLine("Bucket,Total");
            foreach (var a in c.PorAnticipacion) sb.AppendLine($"{a.Bucket},{a.Total}");
            return sb.ToString();
        }

        private static string CsvTendencia(List<IngresosMensualDTO> datos)
        {
            var sb = new StringBuilder();
            sb.AppendLine("Mes,Clase,Ingresos,Reservaciones");
            foreach (var d in datos) sb.AppendLine($"{d.Mes},{d.Clase},{d.Revenue:N2},{d.Reservaciones}");
            return sb.ToString();
        }

        private static string CsvHeatmap(List<HeatmapCeldaDTO> datos)
        {
            var dias = new Dictionary<int, string>
            {
                {1,"Dom"},{2,"Lun"},{3,"Mar"},{4,"Mié"},{5,"Jue"},{6,"Vie"},{7,"Sáb"}
            };
            var sb = new StringBuilder();
            sb.AppendLine("Día,Hora,Asientos Vendidos,% Ocupación");
            foreach (var d in datos)
                sb.AppendLine($"{dias.GetValueOrDefault(d.DiaSemana, $"{d.DiaSemana}")},{d.Hora}:00,{d.AsientosVendidos},{d.OcupacionPct:F0}%");
            return sb.ToString();
        }

        private static string CsvRegistro(List<BusquedaDetalleDTO> registros)
        {
            var sb = new StringBuilder();
            sb.AppendLine("ID,Ruta,Fecha Salida,Pasajeros,Usuario,Canal,Fecha Búsqueda");
            foreach (var r in registros)
                sb.AppendLine($"{r.Id},{r.OrigenCodigo}→{r.DestinoCodigo},{r.FechaSalida},{r.CantidadPersonas},{r.Usuario ?? "No registrado"},{r.Tipo},{r.FechaBusqueda}");
            return sb.ToString();
        }
    }

    /// <summary>Secciones del panel a incluir en el export.</summary>
    public class ExportSecciones
    {
        public bool Kpi              { get; set; } = true;
        public bool BusquedasDiarias { get; set; } = true;
        public bool Canal            { get; set; } = true;
        public bool Embudo           { get; set; } = true;
        public bool Rutas            { get; set; } = true;
        public bool Cancelaciones    { get; set; } = true;
        public bool Tendencia        { get; set; } = true;
        public bool Heatmap          { get; set; } = true;
        public bool Registro         { get; set; } = true;

        public bool NecesitaResumen => Kpi || BusquedasDiarias || Canal;
        public bool NecesitaNegocio => Embudo || Rutas || Cancelaciones || Tendencia || Heatmap;
    }
}
