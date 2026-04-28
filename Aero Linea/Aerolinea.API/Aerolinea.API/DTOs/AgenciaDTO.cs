namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO simple que transporta el ID y porcentaje de descuento de una agencia.
    /// Se utiliza durante el proceso de handshake para retornar el porcentaje de ganancia.
    /// </summary>
    public class AgenciaDTO
    {
        /// <summary>
        /// ID único de la agencia.
        /// </summary>
        public int ID { get; set; }

        /// <summary>
        /// Porcentaje de descuento configurado para la agencia.
        /// </summary>
        public decimal PorcentajeDescuento { get; set; }
    }
}
