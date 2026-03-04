namespace Aerolinea.API.DTOs
{
    public class AeropuertoDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Codigo { get; set; }
        public string Ciudad { get; set; }
        public string Pais { get; set; }
        // Base64 de la imagen (puede ser null si no tiene imagen)
        public string? ImagenBase64 { get; set; }
        // Nombre IANA de la zona horaria (puede ser null si no está configurada)
        public string? ZonaHoraria { get; set; }
    }
}