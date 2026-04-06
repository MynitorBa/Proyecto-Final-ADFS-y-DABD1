namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que almacena la informacion personal de un pasajero asociado a un boleto.
    /// Incluye nombre, apellido, numero de pasaporte, telefono y ubicacion geografica.
    /// </summary>
    public class DatosPasajero
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public string Apellido { get; set; }
        public string Pasaporte { get; set; }
        public string Telefono { get; set; }
        public string Pais { get; set; }
        public string Ciudad { get; set; }
    }
}
