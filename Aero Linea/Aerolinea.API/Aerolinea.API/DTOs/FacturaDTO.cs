namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura que expone los datos de una factura generada al confirmar una reservacion.
    /// Incluye identificador, fecha, datos fiscales y monto total de la transaccion.
    /// </summary>
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
