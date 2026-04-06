namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de lectura que expone el identificador y nombre de una nacionalidad del catalogo.
    /// Utilizado en listas desplegables durante el registro de usuarios.
    /// </summary>
    public class NacionalidadDto
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
    }
}
