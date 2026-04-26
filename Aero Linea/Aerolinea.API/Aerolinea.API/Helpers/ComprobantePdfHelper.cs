using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;
using Aerolinea.API.DTOs;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Genera el comprobante de reservación en formato PDF real usando QuestPDF.
    /// Incluye encabezado institucional, tabla de boletos con precios,
    /// datos de pasajeros, datos fiscales si aplica, y pie de página.
    /// </summary>
    public static class ComprobantePdfHelper
    {
        private const string Gold   = "#D4AF37";
        private const string Dark   = "#1C1A18";
        private const string Gray   = "#9CA3AF";
        private const string Brown  = "#8B6B4A";
        private const string LightBg = "#F9F6F0";
        private const string AltRow  = "#F7F4EF";

        public static byte[] GenerarPdf(ReservacionDetalleDTO r)
        {
            QuestPDF.Settings.License = LicenseType.Community;

            string ec = r.EstadoReserva?.ToLower() switch
            {
                "confirmada" => "#2C5F2D",
                "cancelada"  => "#EF4444",
                "pendiente"  => Brown,
                "expirada"   => Gray,
                _            => Brown
            };

            string avion = r.Boletos.Any()
                ? $"{r.Boletos.First().AvionMarca} {r.Boletos.First().AvionModelo}".Trim()
                : "—";

            return Document.Create(doc =>
            {
                doc.Page(page =>
                {
                    page.Size(PageSizes.A4);
                    page.MarginHorizontal(24);
                    page.MarginVertical(20);
                    page.DefaultTextStyle(s => s.FontFamily("Arial").FontSize(9).FontColor("#374151"));

                    // ── ENCABEZADO ────────────────────────────────────────────
                    page.Header().BorderBottom(3).BorderColor(Gold).PaddingBottom(10).Row(row =>
                    {
                        row.RelativeItem().Column(col =>
                        {
                            col.Item().Text("BROOM AIRLINE").Bold().FontSize(22).FontColor(Dark);
                            col.Item().Text("Comprobante Oficial de Reservación").FontSize(10).FontColor(Gray);
                            col.Item().PaddingTop(2)
                               .Text($"Emitido: {DateTime.Now:dd/MM/yyyy HH:mm}").FontSize(8).FontColor(Gray);
                        });
                        row.AutoItem().AlignRight().AlignMiddle()
                           .Background(LightBg).Border(1).BorderColor(Brown).Padding(10).Column(col =>
                        {
                            col.Item().Text(r.NoReservacion ?? "—").Bold().FontSize(14).FontColor(Dark);
                            col.Item().PaddingTop(3)
                               .Text(r.EstadoReserva?.ToUpper() ?? "—").Bold().FontSize(9).FontColor(ec);
                        });
                    });

                    // ── CONTENIDO ─────────────────────────────────────────────
                    page.Content().PaddingTop(12).Column(col =>
                    {
                        col.Spacing(14);

                        // Datos de la reservación
                        col.Item().Border(1).BorderColor("#E5E7EB").Padding(12).Column(sec =>
                        {
                            sec.Item().BorderBottom(2).BorderColor(Gold).PaddingBottom(5)
                               .Text("Datos de la Reservación").Bold().FontSize(10).FontColor(Dark);
                            sec.Item().PaddingTop(8).Row(r2 =>
                            {
                                LabelVal(r2.RelativeItem(), "Nro. Reservación", r.NoReservacion ?? "—");
                                LabelVal(r2.RelativeItem(), "Avión", avion);
                            });
                            sec.Item().PaddingTop(6).Row(r2 =>
                            {
                                LabelVal(r2.RelativeItem(), "Pasajero", r.UsuarioNombre ?? "—");
                                LabelVal(r2.RelativeItem(), "Email", r.UsuarioEmail ?? "—");
                            });
                            sec.Item().PaddingTop(6).Row(r2 =>
                            {
                                LabelVal(r2.RelativeItem(), "Fecha Emisión", r.FechaCreacion.ToString("yyyy-MM-dd HH:mm"));
                                LabelVal(r2.RelativeItem(), "Estado", r.EstadoReserva?.ToUpper() ?? "—");
                            });
                        });

                        // Detalle de boletos
                        col.Item().Border(1).BorderColor("#E5E7EB").Column(sec =>
                        {
                            sec.Item().Background(Dark).PaddingVertical(7).PaddingHorizontal(10)
                               .Text("Detalle de Boletos").Bold().FontSize(10).FontColor(Gold);

                            // Cabecera
                            sec.Item().Row(row =>
                            {
                                BoletoTH(row.RelativeItem(0.6f), "#");
                                BoletoTH(row.RelativeItem(1.8f), "Boleto");
                                BoletoTH(row.RelativeItem(1.6f), "Vuelo");
                                BoletoTH(row.RelativeItem(0.8f), "Asiento");
                                BoletoTH(row.RelativeItem(2.4f), "Ruta");
                                BoletoTH(row.RelativeItem(1.2f), "Clase");
                                BoletoTH(row.RelativeItem(1.8f), "Fecha");
                                BoletoTH(row.RelativeItem(2.2f), "Horario");
                                BoletoTH(row.RelativeItem(1.6f), "Precio");
                            });

                            int idx = 1;
                            foreach (var b in r.Boletos)
                            {
                                bool alt = idx % 2 == 0;
                                sec.Item().Background(alt ? AltRow : "#FFFFFF").Row(row =>
                                {
                                    BoletoTD(row.RelativeItem(0.6f), idx.ToString(), center: true);
                                    BoletoTD(row.RelativeItem(1.8f), b.NoBoleto ?? "—");
                                    BoletoTD(row.RelativeItem(1.6f), b.NumeroVuelo ?? "—");
                                    BoletoTD(row.RelativeItem(0.8f), b.NoAsiento ?? "—", center: true);
                                    BoletoTD(row.RelativeItem(2.4f), $"{b.OrigenCodigo} → {b.DestinoCodigo}");
                                    BoletoTD(row.RelativeItem(1.2f), b.Clase ?? "—", center: true);
                                    BoletoTD(row.RelativeItem(1.8f), b.FechaVuelo.ToString("yyyy-MM-dd"), center: true);
                                    BoletoTD(row.RelativeItem(2.2f),
                                        $"{(int)b.HoraSalida.TotalHours:D2}:{b.HoraSalida.Minutes:D2} – {(int)b.HoraLlegada.TotalHours:D2}:{b.HoraLlegada.Minutes:D2}",
                                        center: true);
                                    BoletoTD(row.RelativeItem(1.6f), $"$ {b.Precio:N2}", right: true, bold: true);
                                });
                                idx++;
                            }

                            // Subtotales por vuelo si hay múltiples
                            var grupos = r.Boletos.GroupBy(b => b.NumeroVuelo).ToList();
                            if (grupos.Count > 1)
                                foreach (var g in grupos)
                                {
                                    sec.Item().Background("#EDE8E2").PaddingVertical(4).PaddingHorizontal(10)
                                       .AlignRight()
                                       .Text($"Subtotal Vuelo {g.Key} ({g.Count()} boleto{(g.Count() != 1 ? "s" : "")}): $ {g.Sum(b => b.Precio):N2}")
                                       .FontSize(8).Bold().FontColor(Dark);
                                }

                            // Total
                            sec.Item().Background("#3A3531").PaddingVertical(9).PaddingHorizontal(10).Row(row =>
                            {
                                row.RelativeItem().AlignMiddle()
                                   .Text("TOTAL RESERVACIÓN").Bold().FontSize(10).FontColor("#F2EFEA");
                                row.AutoItem().AlignRight().AlignMiddle()
                                   .Text($"$ {r.Total:N2}").Bold().FontSize(13).FontColor(Gold);
                            });
                        });

                        // Datos de pasajeros
                        var conPasajero = r.Boletos.Where(b => b.Pasajero != null).ToList();
                        if (conPasajero.Any())
                        {
                            col.Item().Border(1).BorderColor("#E5E7EB").Column(sec =>
                            {
                                sec.Item().Background(Dark).PaddingVertical(7).PaddingHorizontal(10)
                                   .Text("Datos de Pasajeros").Bold().FontSize(10).FontColor(Gold);

                                sec.Item().Row(row =>
                                {
                                    BoletoTH(row.RelativeItem(0.5f), "#");
                                    BoletoTH(row.RelativeItem(3f), "Nombre Completo");
                                    BoletoTH(row.RelativeItem(2f), "Pasaporte");
                                    BoletoTH(row.RelativeItem(2f), "Teléfono");
                                    BoletoTH(row.RelativeItem(3f), "Ciudad, País");
                                    BoletoTH(row.RelativeItem(1f), "Asiento");
                                    BoletoTH(row.RelativeItem(1.5f), "Vuelo");
                                });

                                int pi = 1;
                                foreach (var b in conPasajero)
                                {
                                    var p = b.Pasajero!;
                                    bool alt = pi % 2 == 0;
                                    sec.Item().Background(alt ? AltRow : "#FFFFFF").Row(row =>
                                    {
                                        BoletoTD(row.RelativeItem(0.5f), pi.ToString(), center: true);
                                        BoletoTD(row.RelativeItem(3f), $"{p.Nombre} {p.Apellido}");
                                        BoletoTD(row.RelativeItem(2f), p.Pasaporte ?? "—");
                                        BoletoTD(row.RelativeItem(2f), p.Telefono ?? "—");
                                        BoletoTD(row.RelativeItem(3f), $"{p.Ciudad}, {p.Pais}");
                                        BoletoTD(row.RelativeItem(1f), b.NoAsiento ?? "—", center: true);
                                        BoletoTD(row.RelativeItem(1.5f), b.NumeroVuelo ?? "—");
                                    });
                                    pi++;
                                }
                            });
                        }

                        // Datos fiscales
                        if (r.Factura != null)
                        {
                            var f = r.Factura;
                            col.Item().Border(1).BorderColor("#E5E7EB").Padding(12).Column(sec =>
                            {
                                sec.Item().BorderBottom(2).BorderColor(Gold).PaddingBottom(5)
                                   .Text("Datos Fiscales").Bold().FontSize(10).FontColor(Dark);
                                sec.Item().PaddingTop(8).Row(r2 =>
                                {
                                    LabelVal(r2.RelativeItem(), "NIT / RFC", f.NIT ?? "—");
                                    LabelVal(r2.RelativeItem(), "Código Postal", f.CodigoPostal ?? "—");
                                    LabelVal(r2.RelativeItem(), "Fecha Emisión", f.Fecha.ToString("yyyy-MM-dd"));
                                    LabelVal(r2.RelativeItem(), "Total Factura", $"$ {f.Total:N2}");
                                });
                            });
                        }

                        // Términos y condiciones
                        col.Item().Background(LightBg).Border(1).BorderColor("#E5E7EB").Padding(10).Column(sec =>
                        {
                            sec.Item().PaddingBottom(5)
                               .Text("Términos y Condiciones").FontSize(8.5f).Bold().FontColor(Brown);
                            foreach (var term in new[]
                            {
                                "1. Este comprobante es válido únicamente para los vuelos indicados.",
                                "2. Presentar pasaporte vigente al momento del check-in.",
                                "3. Abordaje cierra 30 minutos antes de la hora de salida.",
                                "4. Cancelaciones están sujetas a la política vigente de Broom AirLine.",
                                "5. Este documento es comprobante oficial de reservación."
                            })
                                sec.Item().PaddingBottom(2)
                                   .Text(term).FontSize(7.5f).FontColor(Gray);
                        });
                    });

                    // ── PIE ───────────────────────────────────────────────────
                    page.Footer().BorderTop(2).BorderColor(Gold).PaddingTop(6).Row(row =>
                    {
                        row.RelativeItem().AlignMiddle()
                           .Text("BROOM AIRLINE · Guatemala City, Guatemala · distribuidorapine@gmail.com")
                           .FontSize(7).FontColor(Gray);
                        row.AutoItem().AlignRight().AlignMiddle().Text(t =>
                        {
                            t.Span("Página ").FontSize(7).FontColor(Gray);
                            t.CurrentPageNumber().FontSize(7).FontColor(Gold);
                            t.Span(" de ").FontSize(7).FontColor(Gray);
                            t.TotalPages().FontSize(7).FontColor(Gold);
                        });
                    });
                });
            }).GeneratePdf();
        }

        private static void LabelVal(IContainer c, string label, string valor)
        {
            c.Column(col =>
            {
                col.Item().Text(label).FontSize(7.5f).FontColor(Brown).Bold();
                col.Item().PaddingTop(2).Text(valor).FontSize(9).FontColor(Dark);
            });
        }

        private static void BoletoTH(IContainer c, string texto)
        {
            c.Background(Dark).PaddingVertical(5).PaddingHorizontal(5)
             .Text(texto).FontSize(7).FontColor(Gold).Bold();
        }

        private static void BoletoTD(IContainer c, string texto,
            bool center = false, bool right = false, bool bold = false)
        {
            var cell = c.BorderBottom(1).BorderColor("#E6E1DA").PaddingVertical(4).PaddingHorizontal(5);
            var txt  = cell.Text(texto).FontSize(7.5f).FontColor("#374151");
            if (bold)     txt.Bold();
        }
    }
}
