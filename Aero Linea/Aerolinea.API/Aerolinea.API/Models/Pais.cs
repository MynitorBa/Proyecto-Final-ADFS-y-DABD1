namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa un pais del catalogo geografico del sistema.
    /// Sirve como referencia para ciudades, aeropuertos y datos de usuario.
    /// </summary>
    public class Pais
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
    }
}
