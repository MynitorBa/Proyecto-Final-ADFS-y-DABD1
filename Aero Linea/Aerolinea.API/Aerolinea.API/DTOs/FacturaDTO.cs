namespace Aerolinea.API.DTOs
{
    public class FacturaDTO
    {
        public int Id { get; set; }
        public int ReservacionId { get; set; }
        public DateTime Fecha { get; set; }
        public string NIT { get; set; } = "";
        public string CodigoPostal { get; set; } = "";
        public decimal Total { get; set; }
    }
}