namespace Aerolinea.API.DTOs
{
    public class BuscarVueloAgenciaDTO
    {
        public string Origen { get; set; } = string.Empty;
        public string OrigenPais { get; set; } = string.Empty;
        public string Destino { get; set; } = string.Empty;
        public string DestinoPais { get; set; } = string.Empty;
        public DateTime Fecha { get; set; }
        public int CantidadPasajeros { get; set; }
        public int? ClaseId { get; set; }
        public decimal? PrecioMinimo { get; set; }
        public decimal? PrecioMaximo { get; set; }
    }
}