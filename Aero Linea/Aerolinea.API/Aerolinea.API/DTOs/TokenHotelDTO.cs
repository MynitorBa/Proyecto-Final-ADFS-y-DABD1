namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO con los datos necesarios para solicitar un token de alianza a un hotel.
    /// </summary>
    public class TokenHotelRequestDTO
    {
        public string Ciudad { get; set; }
        public string Pais { get; set; }

        public DateOnly FechaIda { get; set; }
        public DateOnly FechaVuelta { get; set; }
    }

    /// <summary>
    /// DTO con la respuesta del hotel tras generar el token de alianza.
    /// Contiene el token, la URL lista para redirigir al usuario y la fecha de expiracion.
    /// </summary>
    public class TokenHotelResponseDTO
    {
        public string Token { get; set; }
        public string UrlRedireccion { get; set; }
        public string FechaExpiracion { get; set; }
    }
}