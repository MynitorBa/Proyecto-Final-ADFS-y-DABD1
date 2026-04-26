using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;
using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Genera un PDF de métricas con gráficas visuales usando la API nativa de QuestPDF
    /// (tablas con celdas proporcionales coloreadas). Esquema dorado Broom AirLine.
    /// </summary>
    public static class MetricasPdfHelper
    {
        // ── Paleta ───────────────────────────────────────────────────────────
        private const string Gold   = "#D4AF37";
        private const string Dark   = "#1C1A18";
        private const string Gray   = "#9CA3AF";
        private const string Red    = "#EF4444";
        private const string Green  = "#10B981";
        private const string Blue   = "#60A5FA";
        private const string Purple = "#8B5CF6";
        private const string Amber  = "#F59E0B";
        private const string BarBg  = "#F3F4F6";
        private const string AltRow = "#FFFDF7";

        // ── Punto de entrada ─────────────────────────────────────────────────
        public static byte[] GenerarPdf(
            MetricasResumenDTO? resumen,
            NegocioMetricasDTO? negocio,
            ListadoBusquedasDTO? listado,
            ExportSecciones sec,
            string periodo)
        {
            QuestPDF.Settings.License = LicenseType.Community;

            return Document.Create(doc =>
            {
                doc.Page(page =>
                {
                    page.Size(PageSizes.A4);
                    page.MarginHorizontal(28);
                    page.MarginVertical(22);
                    page.DefaultTextStyle(s => s.FontFamily("Arial").FontSize(9).FontColor("#374151"));

                    page.Header().Element(c => Encabezado(c, periodo));

                    page.Content().Column(col =>
                    {
                        col.Spacing(20);

                        if (sec.Kpi && resumen != null)
                            col.Item().Element(c => SeccionKpi(c, resumen));

                        if (sec.BusquedasDiarias && resumen?.BusquedasPorDia.Count > 0)
                            col.Item().Element(c => SeccionBusquedasDiarias(c, resumen.BusquedasPorDia));

                        if (sec.Canal && resumen?.BusquedasPorTipo.Count > 0)
                            col.Item().Element(c => SeccionCanal(c, resumen.BusquedasPorTipo));

                        if (sec.Embudo && negocio != null)
                            col.Item().Element(c => SeccionEmbudo(c, negocio.Embudo));

                        if (sec.Rutas && negocio?.RutasRendimiento.Count > 0)
                            col.Item().Element(c => SeccionRutas(c, negocio.RutasRendimiento));

                        if (sec.Cancelaciones && negocio != null)
                            col.Item().Element(c => SeccionCancelaciones(c, negocio.Cancelaciones));

                        if (sec.Tendencia && negocio?.IngresosTendencia.Count > 0)
                            col.Item().Element(c => SeccionTendencia(c, negocio.IngresosTendencia));

                        if (sec.Heatmap && negocio?.Heatmap.Count > 0)
                            col.Item().Element(c => SeccionHeatmap(c, negocio.Heatmap));

                        if (sec.Registro && listado?.Registros.Count > 0)
                            col.Item().Element(c => SeccionRegistro(c, listado));
                    });

                    page.Footer().AlignCenter().Text(t =>
                    {
                        t.Span("Broom AirLine · Reporte de Métricas · ").FontColor(Gray).FontSize(7);
                        t.Span(DateTime.Now.ToString("dd/MM/yyyy HH:mm")).FontColor(Gray).FontSize(7);
                        t.Span("  |  Página ").FontColor(Gray).FontSize(7);
                        t.CurrentPageNumber().FontColor(Gold).FontSize(7);
                        t.Span(" de ").FontColor(Gray).FontSize(7);
                        t.TotalPages().FontColor(Gold).FontSize(7);
                    });
                });
            }).GeneratePdf();
        }

        // ══════════════════════════════════════════════════════════════════════
        // UTILIDADES DE LAYOUT
        // ══════════════════════════════════════════════════════════════════════

        private static void Encabezado(IContainer c, string periodo)
        {
            c.BorderBottom(2).BorderColor(Gold).PaddingBottom(8).Row(row =>
            {
                row.RelativeItem().Column(col =>
                {
                    col.Item().Text("Broom AirLine").Bold().FontSize(20).FontColor(Dark);
                    col.Item().Text("Reporte de Métricas Administrativas").FontSize(10).FontColor(Gray);
                });
                row.AutoItem().AlignRight().AlignMiddle().Column(col =>
                {
                    col.Item().Text($"Período: {periodo}").Bold().FontSize(9).FontColor(Gold);
                    col.Item().Text($"Generado: {DateTime.Now:dd/MM/yyyy HH:mm}").FontSize(8).FontColor(Gray);
                });
            });
        }

        private static void Seccion(IContainer c, string titulo, Action<IContainer> body)
        {
            c.Border(1).BorderColor("#E5E7EB").Padding(14).Column(col =>
            {
                col.Item()
                   .BorderBottom(2).BorderColor(Gold).PaddingBottom(7)
                   .Text(titulo).Bold().FontSize(11).FontColor(Dark);
                col.Item().PaddingTop(8).Element(body);
            });
        }

        /// <summary>Barra horizontal proporcional con etiqueta y valor.</summary>
        private static void BarraHorizontal(IContainer container, string label, double value, double maxValue,
            string color, string? valorTexto = null, float labelWidth = 90, float valueWidth = 55)
        {
            double pct = maxValue > 0 ? value / maxValue : 0;
            pct = Math.Clamp(pct, 0, 1);

            container.Row(row =>
            {
                row.ConstantItem(labelWidth).AlignMiddle()
                   .Text(label.Length > 14 ? label[..14] + "…" : label)
                   .FontSize(8).FontColor(Dark);

                row.RelativeItem().AlignMiddle().Height(13).Row(barRow =>
                {
                    float filled = (float)(pct * 1000);
                    float empty  = (float)((1 - pct) * 1000);
                    // Guardar contra RelativeItem(0) que lanza excepcion en QuestPDF
                    if (filled > 0) barRow.RelativeItem(filled).Height(11).Background(color);
                    if (empty  > 0) barRow.RelativeItem(empty).Height(11).Background(BarBg);
                    // Caso extremo: ambos cero (no deberia ocurrir con Math.Clamp)
                    if (filled <= 0 && empty <= 0) barRow.RelativeItem(1000).Height(11).Background(BarBg);
                });

                row.ConstantItem(valueWidth).AlignMiddle().AlignRight()
                   .Text(valorTexto ?? value.ToString("N0"))
                   .FontSize(7.5f).FontColor(Gray);
            });
        }

        private static void FilaTabla(IContainer c, string[] celdas, bool esHeader, bool alternada = false)
        {
            c.Background(esHeader ? Dark : alternada ? AltRow : "#FFFFFF")
             .Padding(0)
             .Row(row =>
             {
                 foreach (var celda in celdas)
                 {
                     var cell = row.RelativeItem().PaddingVertical(5).PaddingHorizontal(7);
                     if (esHeader)
                         cell.Text(celda).FontSize(8).FontColor(Gold).Bold();
                     else
                         cell.Text(celda).FontSize(8).FontColor("#374151");
                 }
             });
        }

        // ══════════════════════════════════════════════════════════════════════
        // KPI
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionKpi(IContainer c, MetricasResumenDTO r)
        {
            var kpi = r.IngresosKpi;
            decimal total = kpi.IngresosTurista + kpi.IngresosEjecutivo;

            Seccion(c, "KPI de Ingresos", body =>
            {
                body.Row(row =>
                {
                    KpiBox(row.RelativeItem(), "Ingresos Totales", $"${total:N2}",                  Gold);
                    row.ConstantItem(5);
                    KpiBox(row.RelativeItem(), "Turista",          $"${kpi.IngresosTurista:N2}",     Green);
                    row.ConstantItem(5);
                    KpiBox(row.RelativeItem(), "Ejecutivo",        $"${kpi.IngresosEjecutivo:N2}",   Purple);
                    row.ConstantItem(5);
                    KpiBox(row.RelativeItem(), "Ticket Prom.",     $"${kpi.TicketPromedio:N2}",      Blue);
                    row.ConstantItem(5);
                    KpiBox(row.RelativeItem(), "Boletos",          kpi.TotalBoletos.ToString("N0"),  Amber);
                    row.ConstantItem(5);
                    KpiBox(row.RelativeItem(), "Búsquedas",        r.TotalBusquedas.ToString("N0"), Red);
                });
            });
        }

        private static void KpiBox(IContainer c, string label, string valor, string color)
        {
            c.Border(1).BorderColor(color).Padding(8).Column(col =>
            {
                col.Item().PaddingBottom(4).Text(label).FontSize(7.5f).FontColor(Gray).Bold();
                col.Item().Text(valor).FontSize(12).Bold().FontColor(color);
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // BÚSQUEDAS DIARIAS
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionBusquedasDiarias(IContainer c, List<BusquedasPorDiaDTO> datos)
        {
            double maxV = datos.Max(d => d.Total); if (maxV == 0) maxV = 1;

            Seccion(c, $"Búsquedas Diarias — Total: {datos.Sum(d => d.Total):N0}", body =>
            {
                body.Column(col =>
                {
                    var top = datos.OrderByDescending(d => d.Total).Take(12).ToList();
                    foreach (var d in top)
                    {
                        col.Item().PaddingBottom(6).Element(cont =>
                            BarraHorizontal(cont,
                                d.Fecha.Length >= 5 ? d.Fecha[5..] : d.Fecha,
                                d.Total, maxV, Gold, labelWidth: 50, valueWidth: 40));
                    }
                    if (datos.Count > 12)
                        col.Item().PaddingTop(6).Text($"… y {datos.Count - 12} días más").FontSize(7.5f).FontColor(Gray).Italic();
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // CANAL
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionCanal(IContainer c, List<BusquedasPorTipoDTO> datos)
        {
            var colores = new[] { Gold, Dark, Green, Blue };
            int total = datos.Sum(d => d.Total);
            double maxV = datos.Max(d => d.Total); if (maxV == 0) maxV = 1;

            Seccion(c, "Búsquedas por Canal", body =>
            {
                body.Row(row =>
                {
                    row.RelativeItem(6).Column(col =>
                    {
                        for (int i = 0; i < datos.Count; i++)
                        {
                            var d = datos[i];
                            double pct = total > 0 ? (double)d.Total / total : 0;
                            col.Item().PaddingBottom(8).Element(cont =>
                                BarraHorizontal(cont, d.Tipo, d.Total, maxV, colores[i % colores.Length],
                                    valorTexto: $"{d.Total:N0} ({pct * 100:F1}%)"));
                        }
                    });

                    row.ConstantItem(16);

                    row.RelativeItem(4).Column(col =>
                    {
                        col.Item().PaddingBottom(6).Text("Distribución").FontSize(8).Bold().FontColor(Dark);
                        for (int i = 0; i < datos.Count; i++)
                        {
                            var d = datos[i]; double pct = total > 0 ? (double)d.Total / total * 100 : 0;
                            col.Item().PaddingBottom(6).Row(r =>
                            {
                                r.ConstantItem(8).Height(8).AlignMiddle().Background(colores[i % colores.Length]);
                                r.AutoItem().PaddingLeft(5).AlignMiddle()
                                 .Text($"{d.Tipo}: {pct:F1}%").FontSize(8).FontColor("#374151");
                            });
                        }
                    });
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // EMBUDO
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionEmbudo(IContainer c, EmbudoNegocioDTO emb)
        {
            int baseEmb = Math.Max(
                emb.Completadas + emb.Pagadas + emb.Pendientes + emb.Expiradas + emb.Canceladas, 1);
            var pasos = new[]
            {
                ("Completadas",   (double)emb.Completadas,    Purple),
                ("Pagadas",       (double)emb.Pagadas,        Green),
                ("Pendientes",    (double)emb.Pendientes,     Blue),
                ("Expiradas",     (double)emb.Expiradas,      Amber),
                ("Canceladas",    (double)emb.Canceladas,     Red),
            };
            double baseV = Math.Max(pasos.Max(p => p.Item2), 1);

            Seccion(c, "Embudo de Conversión", body =>
            {
                body.Column(col =>
                {
                    foreach (var (label, value, color) in pasos)
                    {
                        double pct = value / baseEmb * 100;
                        col.Item().PaddingBottom(8).Element(cont =>
                            BarraHorizontal(cont, label, value, baseV, color,
                                valorTexto: $"{value:N0} ({pct:F1}%)", labelWidth: 90, valueWidth: 70));
                    }
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // RUTAS
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionRutas(IContainer c, List<RutaRendimientoDTO> datos)
        {
            decimal totalIng = datos.Sum(r => r.RevenueTotal);
            double maxIng = datos.Max(r => (double)r.RevenueTotal); if (maxIng == 0) maxIng = 1;

            Seccion(c, $"Rendimiento de Rutas — Total: ${totalIng:N2}", body =>
            {
                body.Column(col =>
                {
                    col.Item().Element(t => FilaTabla(t,
                        new[] { "Ruta", "Búsquedas", "Reserv.", "Boletos", "Ingresos", "%" }, esHeader: true));
                    for (int i = 0; i < datos.Count; i++)
                    {
                        var d = datos[i];
                        double pct = totalIng > 0 ? (double)d.RevenueTotal / (double)totalIng * 100 : 0;
                        col.Item().Element(t => FilaTabla(t,
                            new[] { d.Ruta, d.Busquedas.ToString(), d.TotalReservaciones.ToString(),
                                    d.BoletosVendidos.ToString(), $"${d.RevenueTotal:N2}", $"{pct:F1}%" },
                            esHeader: false, alternada: i % 2 == 1));
                    }

                    col.Item().PaddingTop(12).PaddingBottom(4).Text("Ingresos por ruta:").FontSize(8).FontColor(Gray);
                    foreach (var d in datos)
                    {
                        col.Item().PaddingBottom(6).Element(cont =>
                            BarraHorizontal(cont, d.Ruta, (double)d.RevenueTotal, maxIng, Gold,
                                valorTexto: $"${d.RevenueTotal:N2}"));
                    }
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // CANCELACIONES
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionCancelaciones(IContainer c, CancelacionesAnalisisDTO canc)
        {
            var colA = new[] { Red, Amber, Gold, Green };

            Seccion(c, "Análisis de Cancelaciones", body =>
            {
                body.Row(row =>
                {
                    row.RelativeItem().Column(col =>
                    {
                        col.Item().PaddingBottom(5).Text("Rutas afectadas").Bold().FontSize(9).FontColor(Dark);
                        double maxR = canc.PorRuta.Count > 0 ? canc.PorRuta.Max(r => r.Total) : 1;
                        foreach (var r in canc.PorRuta)
                            col.Item().PaddingBottom(7).Element(cont =>
                                BarraHorizontal(cont, $"{r.OrigenCodigo}→{r.DestinoCodigo}",
                                    r.Total, maxR, Red, labelWidth: 65, valueWidth: 28));
                    });

                    row.ConstantItem(10);

                    row.RelativeItem().Column(col =>
                    {
                        col.Item().PaddingBottom(5).Text("Quién canceló").Bold().FontSize(9).FontColor(Dark);
                        int totalT = canc.PorTipo.Sum(t => t.Total);
                        var coloresTipo = new[] { Dark, Gold, Green };
                        for (int i = 0; i < canc.PorTipo.Count; i++)
                        {
                            var t = canc.PorTipo[i];
                            double pct = totalT > 0 ? (double)t.Total / totalT * 100 : 0;
                            col.Item().PaddingBottom(9).Row(r =>
                            {
                                r.ConstantItem(8).Height(8).AlignMiddle().Background(coloresTipo[i % coloresTipo.Length]);
                                r.RelativeItem().PaddingLeft(5).AlignMiddle()
                                 .Text($"{t.Tipo}: {t.Total:N0} ({pct:F1}%)").FontSize(7.5f).FontColor("#374151");
                            });
                        }
                    });

                    row.ConstantItem(10);

                    row.RelativeItem().Column(col =>
                    {
                        col.Item().PaddingBottom(5).Text("Anticipación").Bold().FontSize(9).FontColor(Dark);
                        double maxA = canc.PorAnticipacion.Count > 0 ? canc.PorAnticipacion.Max(a => a.Total) : 1;
                        int totalA = canc.PorAnticipacion.Sum(a => a.Total);
                        for (int i = 0; i < canc.PorAnticipacion.Count; i++)
                        {
                            var a = canc.PorAnticipacion[i];
                            double pct = totalA > 0 ? (double)a.Total / totalA * 100 : 0;
                            col.Item().PaddingBottom(7).Element(cont =>
                                BarraHorizontal(cont, a.Bucket, a.Total, maxA, colA[i % colA.Length],
                                    valorTexto: $"{a.Total} ({pct:F1}%)", labelWidth: 60, valueWidth: 40));
                        }
                    });
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // TENDENCIA
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionTendencia(IContainer c, List<IngresosMensualDTO> datos)
        {
            var meses   = datos.Select(d => d.Mes).Distinct().OrderBy(m => m).ToList();
            var clases  = datos.Select(d => d.Clase).Distinct().ToList();
            var byKey   = datos.ToDictionary(d => $"{d.Mes}|{d.Clase}", d => d.Revenue);
            var colores = new[] { Gold, Dark, Green, Blue };
            double maxV = datos.Max(d => (double)d.Revenue); if (maxV == 0) maxV = 1;

            Seccion(c, "Tendencia de Ingresos por Clase", body =>
            {
                body.Column(col =>
                {
                    // Leyenda
                    col.Item().PaddingBottom(8).Row(row =>
                    {
                        for (int i = 0; i < clases.Count; i++)
                        {
                            row.AutoItem().Row(r =>
                            {
                                r.ConstantItem(10).Height(8).AlignMiddle().Background(colores[i % colores.Length]);
                                r.AutoItem().PaddingLeft(4).AlignMiddle().Text(clases[i]).FontSize(8).FontColor("#374151");
                                r.ConstantItem(14);
                            });
                        }
                    });

                    // Barras por mes
                    foreach (var mes in meses)
                    {
                        var labelMes = mes.Length >= 7 ? $"{mes[5..7]}/{mes[2..4]}" : mes;
                        col.Item().PaddingBottom(10).Row(row =>
                        {
                            row.ConstantItem(42).AlignMiddle().Text(labelMes).FontSize(7.5f).FontColor(Gray);
                            row.RelativeItem().Column(innerCol =>
                            {
                                for (int ci = 0; ci < clases.Count; ci++)
                                {
                                    var rev = (double)byKey.GetValueOrDefault($"{mes}|{clases[ci]}", 0);
                                    innerCol.Item().PaddingBottom(5).Element(cont =>
                                        BarraHorizontal(cont, clases[ci], rev, maxV, colores[ci % colores.Length],
                                            valorTexto: $"${rev:N0}", labelWidth: 55, valueWidth: 52));
                                }
                            });
                        });
                    }
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // HEATMAP — tabla con celdas coloreadas por intensidad
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionHeatmap(IContainer c, List<HeatmapCeldaDTO> datos)
        {
            var diasNombres = new Dictionary<int, string>
                { {1,"Dom"},{2,"Lun"},{3,"Mar"},{4,"Mié"},{5,"Jue"},{6,"Vie"},{7,"Sáb"} };
            int pico = datos.Count > 0 ? datos.Max(d => d.AsientosVendidos) : 1;

            Seccion(c, "Mapa de Calor de Búsquedas", body =>
            {
                body.Column(col =>
                {
                    col.Item().PaddingBottom(6)
                       .Text("Intensidad relativa al pico del período. Más oscuro = más búsquedas.")
                       .FontSize(7.5f).FontColor(Gray);

                    var horas      = datos.Select(d => d.Hora).Distinct().OrderBy(h => h).ToList();
                    var diasOrden  = new[] { 2, 3, 4, 5, 6, 7, 1 };

                    // Header de horas
                    col.Item().Row(row =>
                    {
                        row.ConstantItem(30).Text("").FontSize(7);
                        foreach (var h in horas)
                            row.RelativeItem().AlignCenter().Text($"{h}h").FontSize(6.5f).FontColor(Gray);
                    });

                    foreach (var dia in diasOrden)
                    {
                        var celdas = datos.Where(d => d.DiaSemana == dia)
                                          .ToDictionary(d => d.Hora, d => d.AsientosVendidos);
                        col.Item().PaddingVertical(2).Row(row =>
                        {
                            row.ConstantItem(30).AlignMiddle()
                               .Text(diasNombres.GetValueOrDefault(dia, "?")).FontSize(7.5f).FontColor("#374151");
                            foreach (var h in horas)
                            {
                                int val = celdas.GetValueOrDefault(h, 0);
                                double pct = pico > 0 ? (double)val / pico : 0;
                                string bg = pct == 0 ? "#F3F4F6"
                                    : pct < 0.2 ? "#FEF3C7"
                                    : pct < 0.4 ? "#FCD34D"
                                    : pct < 0.7 ? Gold : "#92400E";
                                string fg = pct >= 0.4 ? "#FFFFFF" : "#374151";

                                row.RelativeItem().Height(18).Background(bg).Padding(1)
                                   .AlignCenter().AlignMiddle()
                                   .Text(val > 0 ? val.ToString() : "").FontSize(6.5f).FontColor(fg);
                            }
                        });
                    }

                    // Leyenda
                    col.Item().PaddingTop(6).Row(row =>
                    {
                        row.AutoItem().Text("Intensidad: ").FontSize(7).FontColor(Gray);
                        foreach (var (bg, lbl) in new[] {
                            ("#FEF3C7","Baja"), ("#FCD34D","Media"), (Gold,"Alta"), ("#92400E","Pico") })
                        {
                            row.ConstantItem(4);
                            row.ConstantItem(10).Height(8).AlignMiddle().Background(bg);
                            row.AutoItem().PaddingLeft(2).AlignMiddle().Text(lbl).FontSize(7).FontColor(Gray);
                            row.ConstantItem(8);
                        }
                    });
                });
            });
        }

        // ══════════════════════════════════════════════════════════════════════
        // REGISTRO
        // ══════════════════════════════════════════════════════════════════════
        private static void SeccionRegistro(IContainer c, ListadoBusquedasDTO listado)
        {
            Seccion(c, $"Registro de Búsquedas — {listado.TotalRegistros:N0} registros", body =>
            {
                body.Column(col =>
                {
                    col.Item().Element(t => FilaTabla(t,
                        new[] { "ID", "Ruta", "F. Salida", "Pax", "Usuario", "Canal", "F. Búsqueda" },
                        esHeader: true));

                    int max = Math.Min(listado.Registros.Count, 50);
                    for (int i = 0; i < max; i++)
                    {
                        var r = listado.Registros[i];
                        col.Item().Element(t => FilaTabla(t,
                            new[] { r.Id.ToString(), $"{r.OrigenCodigo}→{r.DestinoCodigo}",
                                    r.FechaSalida, r.CantidadPersonas.ToString(),
                                    r.Usuario ?? "No registrado", r.Tipo, r.FechaBusqueda },
                            esHeader: false, alternada: i % 2 == 1));
                    }

                    if (listado.TotalRegistros > 50)
                        col.Item().PaddingTop(6)
                           .Text($"Mostrando 50 de {listado.TotalRegistros:N0} registros. Descarga el Excel para el listado completo.")
                           .FontSize(7.5f).FontColor(Gray).Italic();
                });
            });
        }
    }
}
