using Aerolinea.API.DTOs;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de generacion de documentos PDF usando QuestPDF Community.
    /// Se usa principalmente para generar el comprobante de reservacion que se
    /// adjunta al correo de confirmacion de compra.
    /// </summary>
    public class PdfService
    {
        /// <summary>
        /// Genera un PDF generico a partir de contenido HTML.
        /// Metodo heredado mantenido por compatibilidad; retorna array vacio
        /// porque QuestPDF no renderiza HTML arbitrario.
        /// </summary>
        public byte[] GenerarPdf(string html)
        {
            return Array.Empty<byte>();
        }

        /// <summary>
        /// Genera el comprobante de reservacion en formato PDF a partir del DTO
        /// devuelto por el repositorio de facturas tras una compra exitosa.
        /// El documento incluye datos del pasajero, detalle de factura y total.
        /// </summary>
        public byte[] GenerarComprobante(CompraRealizadaDTO dto)
        {
            return Document.Create(container =>
            {
                container.Page(page =>
                {
                    page.Size(PageSizes.A4);
                    page.Margin(2, Unit.Centimetre);
                    page.DefaultTextStyle(x => x.FontFamily(Fonts.Arial));

                    // ── Encabezado ──────────────────────────────────────────
                    page.Header().Column(col =>
                    {
                        col.Item().Row(row =>
                        {
                            row.RelativeItem().Column(inner =>
                            {
                                inner.Item()
                                    .Text("BROOM AIRLINE")
                                    .FontSize(22).Bold()
                                    .FontColor(Color.FromHex("#D4AF37"));

                                inner.Item()
                                    .Text("Comprobante de Reservacion")
                                    .FontSize(11)
                                    .FontColor(Color.FromHex("#6b7280"));
                            });

                            row.ConstantItem(150).AlignRight().Column(inner =>
                            {
                                inner.Item()
                                    .Text($"N\u00b0 {dto.NoReservacion}")
                                    .FontSize(12).Bold()
                                    .FontColor(Color.FromHex("#1C1A18"));

                                inner.Item()
                                    .Text(dto.Fecha.ToString("dd/MM/yyyy"))
                                    .FontSize(10)
                                    .FontColor(Color.FromHex("#6b7280"));
                            });
                        });

                        col.Item().PaddingTop(8)
                            .LineHorizontal(1)
                            .LineColor(Color.FromHex("#EBE6E0"));
                    });

                    // ── Contenido ────────────────────────────────────────────
                    page.Content().PaddingTop(20).Column(col =>
                    {
                        // Datos del pasajero
                        col.Item()
                            .Text("Datos del pasajero")
                            .FontSize(12).Bold()
                            .FontColor(Color.FromHex("#1C1A18"));

                        col.Item().PaddingTop(8).Table(t =>
                        {
                            t.ColumnsDefinition(cols =>
                            {
                                cols.RelativeColumn(1);
                                cols.RelativeColumn(2);
                            });

                            FilaTabla(t, "Nombre",         dto.UsuarioNombre ?? "-");
                            FilaTabla(t, "Correo",         dto.UsuarioEmail  ?? "-");
                            FilaTabla(t, "N\u00b0 Reservacion", dto.NoReservacion ?? "-");
                        });

                        col.Item().PaddingTop(20)
                            .LineHorizontal(1)
                            .LineColor(Color.FromHex("#EBE6E0"));

                        // Detalle de factura
                        col.Item().PaddingTop(20)
                            .Text("Detalle de factura")
                            .FontSize(12).Bold()
                            .FontColor(Color.FromHex("#1C1A18"));

                        col.Item().PaddingTop(8).Table(t =>
                        {
                            t.ColumnsDefinition(cols =>
                            {
                                cols.RelativeColumn(1);
                                cols.RelativeColumn(2);
                            });

                            FilaTabla(t, "NIT",          dto.NIT          ?? "-");
                            FilaTabla(t, "Codigo Postal", dto.CodigoPostal ?? "-");
                            FilaTabla(t, "Fecha",         dto.Fecha.ToString("dd/MM/yyyy HH:mm"));
                        });

                        // Total
                        col.Item().PaddingTop(28)
                            .Background(Color.FromHex("#F9F6F1"))
                            .Padding(14)
                            .Row(row =>
                            {
                                row.RelativeItem()
                                    .Text("TOTAL PAGADO")
                                    .FontSize(13).Bold()
                                    .FontColor(Color.FromHex("#1C1A18"));

                                row.ConstantItem(160).AlignRight()
                                    .Text($"Q {dto.Total:N2}")
                                    .FontSize(16).Bold()
                                    .FontColor(Color.FromHex("#D4AF37"));
                            });
                    });

                    // ── Pie de pagina ─────────────────────────────────────────
                    page.Footer().AlignCenter().Text(text =>
                    {
                        text.Span("Broom AirLine  \u2022  ")
                            .FontSize(9)
                            .FontColor(Color.FromHex("#9ca3af"));
                        text.Span($"Generado el {DateTime.Now:dd/MM/yyyy HH:mm}")
                            .FontSize(9)
                            .FontColor(Color.FromHex("#9ca3af"));
                    });
                });
            }).GeneratePdf();
        }

        // ── Auxiliar ────────────────────────────────────────────────────────

        /// <summary>
        /// Agrega una fila de dos columnas (etiqueta / valor) a una tabla QuestPDF.
        /// </summary>
        private static void FilaTabla(TableDescriptor t, string etiqueta, string valor)
        {
            t.Cell().PaddingVertical(5)
                .Text(etiqueta)
                .FontSize(10)
                .FontColor(Color.FromHex("#9ca3af"));

            t.Cell().PaddingVertical(5)
                .Text(valor)
                .FontSize(10);
        }
    }
}
