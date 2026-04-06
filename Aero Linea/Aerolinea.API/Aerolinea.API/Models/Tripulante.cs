namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa a un miembro de la tripulacion registrado en el sistema.
    /// Contiene nombre, apellido, rol asignado e imagen opcional en Base64.
    /// </summary>
    public class Tripulante
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public int RolID { get; set; }
        public string? ImagenBase64 { get; set; }
    }
}
