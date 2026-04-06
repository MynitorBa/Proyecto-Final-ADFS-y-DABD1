namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa un aeropuerto registrado en el sistema.
    /// Contiene codigo IATA, nombre, ciudad asociada y zona horaria opcional.
    /// </summary>
    public class Aeropuerto
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Codigo { get; set; }
        public int CiudadId { get; set; }
        // FK a la tabla ZonaHoraria — null si no fue configurada
        public int? ZonaHorariaId { get; set; }
    }
}
