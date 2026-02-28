using System.Net;
using System.Net.Mail;

namespace Aerolinea.API.Helpers
{
    public static class EmailHelper
    {
        private static readonly string SmtpHost = "smtp.gmail.com";
        private static readonly int SmtpPort = 587;
        private static readonly string SmtpUser = "distribuidorapine@gmail.com";
        private static readonly string SmtpPass = "axvv hnkv gylv gupb";

        /// <summary>
        /// Envía un correo HTML a un destinatario.
        /// </summary>
        public static async Task Enviar(string destinatario, string asunto, string cuerpoHtml)
        {
            using var smtp = new SmtpClient(SmtpHost, SmtpPort)
            {
                Credentials = new NetworkCredential(SmtpUser, SmtpPass),
                EnableSsl = true
            };

            var mensaje = new MailMessage(SmtpUser, destinatario, asunto, cuerpoHtml)
            {
                IsBodyHtml = true
            };

            await smtp.SendMailAsync(mensaje);
        }

        /// <summary>
        /// Envía un correo con copia oculta al admin.
        /// </summary>
        public static async Task EnviarConCopia(string destinatario, string asunto, string cuerpoHtml, string copiaOculta)
        {
            using var smtp = new SmtpClient(SmtpHost, SmtpPort)
            {
                Credentials = new NetworkCredential(SmtpUser, SmtpPass),
                EnableSsl = true
            };

            var mensaje = new MailMessage(SmtpUser, destinatario, asunto, cuerpoHtml)
            {
                IsBodyHtml = true
            };

            if (!string.IsNullOrEmpty(copiaOculta))
                mensaje.Bcc.Add(copiaOculta);

            await smtp.SendMailAsync(mensaje);
        }

        /// <summary>
        /// Escapa caracteres HTML.
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
    }
}