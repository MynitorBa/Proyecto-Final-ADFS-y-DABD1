namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa un boleto de avion asignado a un vuelo especifico.
    /// Contiene numero de boleto, asiento, precio, clase, estado y referencias
    /// a la reservacion y datos del pasajero.
    /// </summary>
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
