namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para cancelar una reservacion activa.
    /// El campo Motivo es opcional y puede enviarse como null.
    /// </summary>
    public class CancelarReservacionDTO
    {
        public string Motivo { get; set; } // Opcional, puede ser null
    }

    /// <summary>
    /// DTO de respuesta que indica si una reservacion puede ser cancelada.
    /// En caso negativo incluye la razon por la que no es posible realizar la cancelacion.
    /// </summary>
    public class PuedeCancelarDTO
    {
        public bool PuedeCancelar { get; set; }
        public string Razon { get; set; }
    }
}
