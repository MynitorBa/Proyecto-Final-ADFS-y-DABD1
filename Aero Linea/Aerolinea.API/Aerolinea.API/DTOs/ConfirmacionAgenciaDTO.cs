namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para que una agencia confirme y pague una reservacion pendiente.
    /// Contiene los datos de facturacion requeridos para generar la factura.
    /// </summary>
    public class ConfirmarReservacionAgenciaDTO
    {
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }
    }


    /// <summary>
    /// DTO de respuesta devuelto tras confirmar exitosamente una reservacion desde una agencia.
    /// Incluye identificadores de factura y reservacion, total cobrado y fecha de confirmacion.
    /// </summary>
    public class ConfirmacionAgenciaDTO
    {
        public int FacturaId { get; set; }
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public decimal Total { get; set; }
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }
        public DateTime FechaConfirmacion { get; set; }
    }
}
