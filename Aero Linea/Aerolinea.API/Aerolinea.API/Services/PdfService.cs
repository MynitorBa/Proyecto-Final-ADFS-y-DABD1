namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de generacion de documentos PDF. Convierte contenido HTML a bytes de PDF.
    /// Temporalmente retorna un array vacio mientras la libreria nativa wkhtmltopdf
    /// no este disponible en el entorno de desarrollo.
    /// </summary>
    public class PdfService
    {
        /// <summary>
        /// Genera un archivo PDF a partir del contenido HTML recibido.
        /// Retorna un array vacio si la libreria nativa no esta disponible.
        /// </summary>
        public byte[] GenerarPdf(string html)
        {
            return Array.Empty<byte>();
        }
    }
}