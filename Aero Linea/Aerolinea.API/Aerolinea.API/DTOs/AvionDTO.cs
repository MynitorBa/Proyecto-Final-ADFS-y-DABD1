namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura que expone la informacion de una aeronave registrada en la flota.
    /// Incluye marca, modelo, capacidad, nombre completo e imagen opcional en Base64.
    /// </summary>
    public class AvionDTO
    {
        public int Id { get; set; }
        public string Marca { get; set; }
        public string Modelo { get; set; }
        public int CapacidadPasajeros { get; set; }
        public string NombreCompleto { get; set; }
        public bool Activo { get; set; }
        // Base64 de la imagen (puede ser null si no tiene imagen)
        public string? ImagenBase64 { get; set; }
    }
}
