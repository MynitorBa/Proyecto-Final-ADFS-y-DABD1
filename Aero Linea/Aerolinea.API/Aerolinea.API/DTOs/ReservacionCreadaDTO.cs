namespace Aerolinea.API.DTOs
{
    public class ReservacionCreadaDTO
    {
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public DateTime FechaExpiracion { get; set; }
        public decimal Total { get; set; }
        public int MinutosRestantes { get; set; }
        public List<BoletoReservadoDTO> Boletos { get; set; }
    }

    public class BoletoReservadoDTO
    {
        public int BoletoId { get; set; }
        public string NoBoleto { get; set; }
        public string NoAsiento { get; set; }
        public decimal Precio { get; set; }
        public string NumeroVuelo { get; set; }
        public string Clase { get; set; }
    }
}