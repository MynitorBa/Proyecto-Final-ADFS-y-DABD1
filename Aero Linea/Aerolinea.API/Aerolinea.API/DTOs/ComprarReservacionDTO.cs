namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para confirmar la compra de una reservacion pendiente.
    /// Contiene datos de facturacion y datos de tarjeta para validacion de formato.
    /// Los datos de la tarjeta nunca se persisten en el sistema.
    /// </summary>
    public class ComprarReservacionDTO
    {
        // Datos de factura
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }

        // Datos de tarjeta (solo validación de formato, nunca se persisten)
        public string NumeroTarjeta { get; set; }   // 16 dígitos
        public string NombreTitular { get; set; }
        public string FechaExpiracion { get; set; }   // MM/YY
        public string CVV { get; set; }   // 3-4 dígitos
    }

    /// <summary>
    /// DTO de respuesta devuelto tras completar exitosamente la compra de una reservacion.
    /// Contiene los datos de la factura generada y el resumen de la transaccion.
    /// UsuarioNombre y UsuarioEmail se usan internamente para enviar el correo de confirmacion
    /// y se incluyen en la respuesta como referencia para el frontend.
    /// </summary>
    public class CompraRealizadaDTO
    {
        public int FacturaId { get; set; }
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public DateTime Fecha { get; set; }
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }
        public decimal Total { get; set; }

        // Datos del usuario propietario — necesarios para el correo de confirmacion
        public string UsuarioNombre { get; set; }
        public string UsuarioEmail { get; set; }
    }
}
