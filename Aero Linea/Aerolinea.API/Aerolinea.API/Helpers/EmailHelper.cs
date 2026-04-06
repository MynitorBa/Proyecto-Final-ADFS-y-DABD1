using System.Net;
using System.Net.Mail;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Clase estatica de utilidad para el envio de correos electronicos mediante SMTP.
    /// Encapsula la configuracion del servidor de correo y expone metodos para enviar
    /// mensajes HTML simples, con copia oculta, y para escapar texto en HTML.
    /// </summary>
    public static class EmailHelper
    {
        private static readonly string SmtpHost = "smtp.gmail.com";
        private static readonly int SmtpPort = 587;
        private static readonly string SmtpUser = "distribuidorapine@gmail.com";
        private static readonly string SmtpPass = "axvv hnkv gylv gupb";

        /// <summary>
        /// Envia un correo electronico con cuerpo HTML al destinatario especificado.
        /// Utiliza el servidor SMTP configurado con SSL en el puerto 587.
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
        /// Envia un correo electronico con cuerpo HTML al destinatario e incluye una copia
        /// oculta (BCC) a la direccion indicada, si esta no esta vacia.
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
        /// Escapa los caracteres especiales de HTML en el texto recibido para evitar
        /// inyeccion de etiquetas al incrustar contenido dinamico en plantillas HTML.
        /// Retorna cadena vacia si el texto es nulo o vacio.
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
