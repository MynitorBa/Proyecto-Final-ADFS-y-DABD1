namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa una nacionalidad disponible en el catalogo del sistema.
    /// Se utiliza para asociar una o mas nacionalidades a un usuario registrado.
    /// </summary>
    public class Nacionalidad
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
    }
}
