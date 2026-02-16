namespace Aerolinea.API.DTOs
{
    public class CrearAeropuertoDTO
    {
        public string Nombre { get; set; }
        public string Codigo { get; set; }
        public string Ciudad { get; set; }  // Nombre de la ciudad
        public string Pais { get; set; }    // Nombre del país
    }
}