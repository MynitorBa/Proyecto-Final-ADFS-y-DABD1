namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para buscar vuelos disponibles desde el portal web.
    /// Permite filtrar por aeropuerto de origen y destino, fecha, cantidad de
    /// pasajeros, clase y rango de precios.
    /// </summary>
    public class BuscarVueloDTO
    {
        public int OrigenId { get; set; }
        public int DestinoId { get; set; }
        public DateTime Fecha { get; set; }
        public int CantidadPasajeros { get; set; } = 1;
        public decimal? PrecioMinimo { get; set; }
        public decimal? PrecioMaximo { get; set; }
        public int? ClaseId { get; set; }
    }
}
