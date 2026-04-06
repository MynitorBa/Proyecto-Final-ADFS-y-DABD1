namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para asignar los datos de los pasajeros a los boletos de una reservacion existente.
    /// Contiene el identificador de la reservacion y la lista de pasajeros con sus datos personales.
    /// </summary>
    public class AgregarPasajerosDTO
    {
        public int ReservacionId { get; set; }
        public List<DatosPasajeroDTO> Pasajeros { get; set; }
    }

    /// <summary>
    /// DTO con la informacion personal de un pasajero vinculado a un boleto especifico.
    /// Incluye nombre, apellido, pasaporte, telefono y ubicacion geografica.
    /// </summary>
    public class DatosPasajeroDTO
    {
        public int BoletoId { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Pasaporte { get; set; }
        public string Telefono { get; set; }
        public string Pais { get; set; }
        public string Ciudad { get; set; }
    }
}
