using DinkToPdf;
using DinkToPdf.Contracts;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de generacion de documentos PDF. Convierte contenido HTML a un archivo PDF
    /// usando la libreria DinkToPdf con configuracion de pagina A4 horizontal y codificacion UTF-8.
    /// </summary>
    public class PdfService
    {
        private readonly IConverter _converter;

        /// <summary>
        /// Inicializa el servicio con el conversor de HTML a PDF provisto por DinkToPdf.
        /// </summary>
        public PdfService(IConverter converter)
        {
            _converter = converter;
        }

        /// <summary>
        /// Genera un archivo PDF a partir del contenido HTML recibido.
        /// Configura el documento en orientacion horizontal, tamano A4, sin margenes
        /// y con codificacion UTF-8. Retorna los bytes del PDF generado.
        /// </summary>
        public byte[] GenerarPdf(string html)
        {
            var doc = new HtmlToPdfDocument()
            {
                GlobalSettings = new GlobalSettings
                {
                    ColorMode = ColorMode.Color,
                    Orientation = Orientation.Landscape,
                    PaperSize = PaperKind.A4,
                    Margins = new MarginSettings
                    {
                        Top = 0,
                        Bottom = 0,
                        Left = 0,
                        Right = 0
                    },
                },
                Objects =
                {
                    new ObjectSettings
                    {
                        HtmlContent = html,
                        WebSettings = new WebSettings
                        {
                            DefaultEncoding = "utf-8",
                            EnableIntelligentShrinking = false
                        }
                    }
                }
            };
            return _converter.Convert(doc);
        }
    }
}