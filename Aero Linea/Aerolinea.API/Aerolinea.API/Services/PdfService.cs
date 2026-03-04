using DinkToPdf;
using DinkToPdf.Contracts;
namespace Aerolinea.API.Services
{
    public class PdfService
    {
        private readonly IConverter _converter;
        public PdfService(IConverter converter)
        {
            _converter = converter;
        }
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