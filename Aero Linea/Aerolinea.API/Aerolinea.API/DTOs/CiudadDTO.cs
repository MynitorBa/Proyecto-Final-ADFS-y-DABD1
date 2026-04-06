namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura que expone la informacion de una ciudad junto con su pais de pertenencia.
    /// Incluye nombre completo con formato "Ciudad, Pais" para uso en listas desplegables.
    /// </summary>
    public class CiudadDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public int PaisId { get; set; }
        public string NombrePais { get; set; }
        public string NombreCompleto { get; set; } // "Ciudad, País"
    }
}
