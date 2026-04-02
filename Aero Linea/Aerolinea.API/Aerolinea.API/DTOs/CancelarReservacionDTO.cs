namespace Aerolinea.API.DTOs
{
    public class CancelarReservacionDTO
    {
        public string Motivo { get; set; } // Opcional, puede ser null
    }

    public class PuedeCancelarDTO
    {
        public bool PuedeCancelar { get; set; }
        public string Razon { get; set; }
    }
}