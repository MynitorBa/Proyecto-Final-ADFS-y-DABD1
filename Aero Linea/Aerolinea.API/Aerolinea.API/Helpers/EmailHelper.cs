using Aerolinea.API.Models.Config;
using Microsoft.Extensions.Options;
using System.Net;
using System.Net.Mail;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Servicio de envio de correos electronicos. Encapsula la configuracion SMTP
    /// leida desde IOptions<EmailSettings> (appsettings.json / variables de entorno).
    /// Se registra como Singleton en el contenedor de DI para reutilizar la instancia.
    /// El metodo Esc se mantiene estatico para que EmailTemplates y PdfHtmlHelper
    /// puedan invocarlo sin necesidad de inyectar la clase.
    /// </summary>
    public class EmailHelper
    {
        private readonly EmailSettings _cfg;

        /// <summary>
        /// Inicializa el helper con la configuracion SMTP inyectada.
        /// </summary>
        public EmailHelper(IOptions<EmailSettings> options)
        {
            _cfg = options.Value;
        }

        // ── Metodos de envio ─────────────────────────────────────────────────

        /// <summary>
        /// Envia un correo electronico con cuerpo HTML al destinatario especificado.
        /// Utiliza las credenciales SMTP configuradas en appsettings.json.
        /// </summary>
        public async Task Enviar(string destinatario, string asunto, string cuerpoHtml)
        {
            using var smtp = CrearCliente();
            var mensaje = new MailMessage(_cfg.SenderEmail, destinatario, asunto, cuerpoHtml)
            {
                IsBodyHtml = true
            };
            await smtp.SendMailAsync(mensaje);
        }

        /// <summary>
        /// Envia un correo electronico con cuerpo HTML al destinatario e incluye una copia
        /// oculta (BCC) a la direccion indicada, si esta no esta vacia.
        /// </summary>
        public async Task EnviarConCopia(
            string destinatario, string asunto, string cuerpoHtml, string copiaOculta)
        {
            using var smtp = CrearCliente();
            var mensaje = new MailMessage(_cfg.SenderEmail, destinatario, asunto, cuerpoHtml)
            {
                IsBodyHtml = true
            };
            if (!string.IsNullOrEmpty(copiaOculta))
                mensaje.Bcc.Add(copiaOculta);
            await smtp.SendMailAsync(mensaje);
        }

        /// <summary>
        /// Envia un correo electronico con un archivo adjunto en bytes.
        /// Si el array de bytes esta vacio se envia el correo sin adjunto.
        /// Se usa principalmente para enviar el comprobante en PDF tras la compra.
        /// </summary>
        public async Task EnviarConAdjunto(
            string destinatario,
            string asunto,
            string cuerpoHtml,
            byte[] adjunto,
            string nombreArchivo,
            string tipoMime = "application/pdf")
        {
            using var smtp = CrearCliente();
            var mensaje = new MailMessage(_cfg.SenderEmail, destinatario, asunto, cuerpoHtml)
            {
                IsBodyHtml = true
            };

            if (adjunto != null && adjunto.Length > 0)
            {
                var stream = new MemoryStream(adjunto);
                mensaje.Attachments.Add(new Attachment(stream, nombreArchivo, tipoMime));
            }

            await smtp.SendMailAsync(mensaje);
        }

        // ── Metodo estatico de utilidad ──────────────────────────────────────

        /// <summary>
        /// Escapa los caracteres especiales de HTML en el texto recibido para evitar
        /// inyeccion de etiquetas al incrustar contenido dinamico en plantillas HTML.
        /// Retorna cadena vacia si el texto es nulo o vacio.
        /// Se mantiene estatico para que EmailTemplates y PdfHtmlHelper lo usen
        /// sin requerir una instancia inyectada.
        /// </summary>
        public static string Esc(string texto)
        {
            if (string.IsNullOrEmpty(texto)) return "";
            return texto
                .Replace("&", "&amp;")
                .Replace("<", "&lt;")
                .Replace(">", "&gt;")
                .Replace("\"", "&quot;");
        }

        // ── Metodo privado auxiliar ──────────────────────────────────────────

        private SmtpClient CrearCliente() =>
            new SmtpClient(_cfg.SmtpServer, _cfg.SmtpPort)
            {
                Credentials = new NetworkCredential(_cfg.SenderEmail, _cfg.SenderPassword),
                EnableSsl    = true
            };
    }
}
