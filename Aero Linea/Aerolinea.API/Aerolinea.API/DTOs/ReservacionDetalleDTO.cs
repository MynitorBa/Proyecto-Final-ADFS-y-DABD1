namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura con el detalle completo de una reservacion.
    /// Incluye estado, usuario propietario, factura asociada, lista de boletos
    /// y datos de cancelacion si aplica.
    /// </summary>
    public class ReservacionDetalleDTO
    {
        public int ReservacionId { get; set; }
        public string NoReservacion { get; set; }
        public DateTime FechaCreacion { get; set; }
        public DateTime? FechaExpiracion { get; set; }
        public decimal Total { get; set; }
        public string EstadoReserva { get; set; }
        public int EstadoReservaId { get; set; }

        // Información del usuario
        public int UsuarioId { get; set; }
        public string UsuarioNombre { get; set; }
        public string UsuarioEmail { get; set; }

        public DateTime? FechaCancelacion { get; set; }
        public string? MotivoCancelacion { get; set; }

        // Lista de boletos
        public List<BoletoDetalleDTO> Boletos { get; set; }

        public FacturaDTO? Factura { get; set; }
    }

    /// <summary>
    /// DTO con el detalle completo de un boleto dentro de una reservacion.
    /// Incluye informacion del vuelo, ruta, avion y datos del pasajero asignado.
    /// </summary>
    public class BoletoDetalleDTO
    {
        public int BoletoId { get; set; }
        public string NoBoleto { get; set; }
        public string NoAsiento { get; set; }
        public decimal Precio { get; set; }
        public string Clase { get; set; }
        public string EstadoBoleto { get; set; }

        // Información del vuelo
        public int VueloId { get; set; }
        public string NumeroVuelo { get; set; }
        public DateTime FechaVuelo { get; set; }
        public TimeSpan HoraSalida { get; set; }
        public TimeSpan HoraLlegada { get; set; }
        public int DuracionMinutos { get; set; }

        // Información de la ruta
        public int RutaId { get; set; }
        public string OrigenCodigo { get; set; }
        public string OrigenNombre { get; set; }
        public string OrigenCiudad { get; set; }
        public string DestinoCodigo { get; set; }
        public string DestinoNombre { get; set; }
        public string DestinoCiudad { get; set; }

        // Información del avión
        public string AvionModelo { get; set; }
        public string AvionMarca { get; set; }

        // Información del pasajero (si existe)
        public DatosPasajeroInfoDTO Pasajero { get; set; }
    }

    /// <summary>
    /// DTO con la informacion personal del pasajero asignado a un boleto especifico.
    /// </summary>
    public class DatosPasajeroInfoDTO
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Pasaporte { get; set; }
        public string Telefono { get; set; }
        public string Pais { get; set; }
        public string Ciudad { get; set; }
    }

    /// <summary>
    /// DTO de resumen estadistico de las reservaciones de un usuario.
    /// Muestra conteos por estado y el total gastado en todas las reservaciones confirmadas.
    /// </summary>
    public class ResumenReservacionesDTO
    {
        public int TotalReservaciones { get; set; }
        public int Pendientes { get; set; }
        public int Confirmadas { get; set; }
        public int Canceladas { get; set; }
        public int Expiradas { get; set; }
        public int Completadas { get; set; }
        public decimal TotalGastado { get; set; }
    }
}
