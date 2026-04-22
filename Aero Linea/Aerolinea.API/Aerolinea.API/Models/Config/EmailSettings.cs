namespace Aerolinea.API.Models.Config
{
    /// <summary>
    /// Configuracion SMTP para el envio de correos electronicos.
    /// Se enlaza desde la seccion "EmailSettings" de appsettings.json mediante IOptions.
    /// En Docker el valor SenderPassword debe inyectarse como variable de entorno:
    ///   EmailSettings__SenderPassword=tu_app_password
    /// </summary>
    public class EmailSettings
    {
        public string SmtpServer      { get; set; } = string.Empty;
        public int    SmtpPort        { get; set; } = 587;
        public string SenderEmail     { get; set; } = string.Empty;
        public string SenderPassword  { get; set; } = string.Empty;
        public string SenderName      { get; set; } = "Broom AirLine";
    }
}
