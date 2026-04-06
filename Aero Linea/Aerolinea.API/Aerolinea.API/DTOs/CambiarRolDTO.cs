namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para cambiar el rol de un usuario desde el panel de administracion.
    /// Contiene el identificador del usuario y el identificador del nuevo rol a asignar.
    /// </summary>
    public class CambiarRolDTO
    {
        public int UsuarioId { get; set; }
        public int NuevoRolId { get; set; }
    }
}
