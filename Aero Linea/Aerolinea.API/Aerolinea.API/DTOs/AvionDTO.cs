namespace Aerolinea.API.DTOs
{
    public class AvionDTO
    {
        public int Id { get; set; }
        public string Marca { get; set; }
        public string Modelo { get; set; }
        public int CapacidadPasajeros { get; set; }
        public string NombreCompleto { get; set; }
        // Base64 de la imagen (puede ser null si no tiene imagen)
        public string? ImagenBase64 { get; set; }
    }
}