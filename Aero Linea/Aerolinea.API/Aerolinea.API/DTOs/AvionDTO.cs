namespace Aerolinea.API.DTOs
{
    public class AvionDTO
    {
        public int Id { get; set; }
        public string Marca { get; set; }
        public string Modelo { get; set; }
        public int CapacidadPasajeros { get; set; }
        public string NombreCompleto { get; set; } 
    }
}