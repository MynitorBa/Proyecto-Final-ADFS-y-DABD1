namespace Aerolinea.API.DTOs
{
    public class TripulanteDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public int RolID { get; set; }
        public string NombreRol { get; set; }
        public string NombreCompleto { get; set; }
        // Base64 de la imagen (puede ser null si no tiene imagen)
        public string? ImagenBase64 { get; set; }
    }
}