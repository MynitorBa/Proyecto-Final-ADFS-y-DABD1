namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad de relacion que vincula a un usuario con una o mas nacionalidades.
    /// Permite que un usuario tenga multiples nacionalidades registradas en el sistema.
    /// </summary>
    public class UsuarioNacionalidad
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public string Nacionalidad { get; set; }
    }
}
