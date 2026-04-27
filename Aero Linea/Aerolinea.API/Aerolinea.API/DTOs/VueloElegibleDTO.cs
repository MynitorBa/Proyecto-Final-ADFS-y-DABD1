namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// Representa un vuelo elegible para cambio de reservacion.
    /// Cumple con los criterios de mismo pais de origen, mismo destino
    /// y mismo precio por boleto que la reservacion original.
    /// </summary>
    public class VueloElegibleDTO
    {
        public int VueloId { get; set; }
        public string NumeroVuelo { get; set; } = "";
        public string FechaSalida { get; set; } = "";
        public string HoraSalida { get; set; } = "";
        public string HoraLlegada { get; set; } = "";
        public string OrigenCodigo { get; set; } = "";
        public string OrigenCiudad { get; set; } = "";
        public string OrigenPais { get; set; } = "";
        public string DestinoCodigo { get; set; } = "";
        public string DestinoCiudad { get; set; } = "";
        public decimal PrecioPorBoleto { get; set; }
        public int CantidadBoletos { get; set; }
        public decimal PrecioTotal { get; set; }
        public int AsientosDisponibles { get; set; }
    }

    /// <summary>
    /// DTO para solicitar el cambio de vuelo de una reservacion.
    /// </summary>
    public class CambiarVueloRequestDTO
    {
        public int NuevoVueloId { get; set; }
    }
}
