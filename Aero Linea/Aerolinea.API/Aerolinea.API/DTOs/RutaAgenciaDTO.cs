namespace Aerolinea.API.DTOs
{
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