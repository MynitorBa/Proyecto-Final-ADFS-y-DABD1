namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de respuesta devuelto al crear una nueva reservacion exitosamente.
    /// Incluye identificador, numero de reservacion, expiracion, total y lista de boletos asignados.
    /// </summary>
    public class ReservacionCreadaDTO
    {
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public DateTime FechaExpiracion { get; set; }
        public decimal Total { get; set; }
        public int MinutosRestantes { get; set; }
        public List<BoletoReservadoDTO> Boletos { get; set; }
    }

    /// <summary>
    /// DTO que representa un boleto incluido en la respuesta de creacion de reservacion.
    /// Contiene numero de boleto, asiento asignado, precio, vuelo y clase correspondiente.
    /// </summary>
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
