namespace Aerolinea.API.DTOs
{
    public class CiudadDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public int PaisId { get; set; }
        public string NombrePais { get; set; }
        public string NombreCompleto { get; set; } // "Ciudad, País"
    }
}