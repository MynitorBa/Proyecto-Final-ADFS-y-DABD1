namespace Aerolinea.API.Models.DTOs
{
    /// <summary>
    /// DTO para la creacion de un nuevo vuelo desde el panel de administracion.
    /// Contiene numero de vuelo, aeropuertos, avion, fecha, hora de salida local,
    /// disponibilidad y precios por clase, y los tripulantes asignados.
    /// </summary>
    public class CrearVueloAdminDTO
    {
        public string NumeroVuelo { get; set; }
        public int AeropuertoOrigenId { get; set; }
        public int AeropuertoDestinoId { get; set; }
        public int AvionId { get; set; }
        public DateTime Fecha { get; set; }

        /// <summary>
        /// Hora de salida local en el aeropuerto de origen (formato "HH:mm").
        /// La hora de llegada se calcula automáticamente usando la duración
        /// de la ruta y las zonas horarias de ambos aeropuertos.
        /// </summary>
        public string HoraSalida { get; set; }

        public int BoletosTurista { get; set; }
        public int BoletosEjecutivo { get; set; }
        public decimal PrecioTurista { get; set; }
        public decimal PrecioEjecutiva { get; set; }
        public List<int> TripulantesIds { get; set; }
    }
}
