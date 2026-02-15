namespace Aerolinea.API.DTOs
{
    public class BuscarVueloDTO
    {
        public int OrigenId { get; set; }
        public int DestinoId { get; set; }
        public DateTime Fecha { get; set; }
        public int CantidadPasajeros { get; set; } = 1;
        public decimal? PrecioMinimo { get; set; }
        public decimal? PrecioMaximo { get; set; }
        public int? ClaseId { get; set; }
    }
}