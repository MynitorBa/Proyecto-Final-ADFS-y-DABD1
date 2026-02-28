namespace Aerolinea.API.DTOs
{
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

    public class CompraRealizadaDTO
    {
        public int FacturaId { get; set; }
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public DateTime Fecha { get; set; }
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }
        public decimal Total { get; set; }
    }
}