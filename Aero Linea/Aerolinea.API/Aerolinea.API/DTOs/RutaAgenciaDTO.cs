namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura que expone la informacion de una ruta disponible para agencias.
    /// Incluye ciudad y pais de origen y destino, ademas de la duracion estimada en minutos.
    /// </summary>
    public class RutaAgenciaDTO
    {
        public int ID { get; set; }
        public string CiudadOrigen { get; set; } = string.Empty;
        public string PaisOrigen { get; set; } = string.Empty;
        public string CiudadDestino { get; set; } = string.Empty;
        public string PaisDestino { get; set; } = string.Empty;
        public int Duracion { get; set; }
    }
}
