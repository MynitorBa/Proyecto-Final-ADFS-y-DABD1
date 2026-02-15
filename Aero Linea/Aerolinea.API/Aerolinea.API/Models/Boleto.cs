namespace Aerolinea.API.Models
{
    public class Boleto
    {
        public int Id { get; set; }
        public string NoBoleto { get; set; }
        public string NoAsiento { get; set; }
        public decimal Precio { get; set; }
        public int VueloId { get; set; }
        public int ClaseId { get; set; }
        public int EstadoBoletoId { get; set; }
        public int? ReservacionId { get; set; }
        public int? DatosPasajeroId { get; set; }
    }
}