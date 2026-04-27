namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO para que el usuario edite los datos del pasajero de un boleto propio.
    /// Solo permitido si el vuelo sale en mas de 24 horas y la reservacion esta activa.
    /// </summary>
    public class EditarDatosPasajeroDTO
    {
        public string Nombre    { get; set; } = string.Empty;
        public string Apellido  { get; set; } = string.Empty;
        public string Pasaporte { get; set; } = string.Empty;
        public string Telefono  { get; set; } = string.Empty;
    }
}
