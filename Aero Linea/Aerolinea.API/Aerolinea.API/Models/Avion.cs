namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa una aeronave registrada en la flota de la aerolinea.
    /// Almacena marca, modelo, capacidad de pasajeros e imagen opcional en Base64.
    /// </summary>
    public class Avion
    {
        public int Id { get; set; }
        public string Marca { get; set; }
        public string Modelo { get; set; }
        public int CapacidadPasajeros { get; set; }
        public string? ImagenBase64 { get; set; }
        public bool Activo { get; set; } = true;
    }
}
