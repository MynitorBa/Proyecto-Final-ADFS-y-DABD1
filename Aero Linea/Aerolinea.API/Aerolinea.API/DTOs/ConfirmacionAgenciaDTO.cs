namespace Aerolinea.API.DTOs
{
    // Body que recibe el endpoint
    public class ConfirmarReservacionAgenciaDTO
    {
        public string NIT { get; set; }
        public string CodigoPostal { get; set; }
    }


    // Response que devuelve el endpoint
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