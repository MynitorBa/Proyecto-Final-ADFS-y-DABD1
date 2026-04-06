namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa una ciudad del catalogo geografico del sistema.
    /// Pertenece a un pais y sirve como referencia para aeropuertos y usuarios.
    /// </summary>
    public class Ciudad
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
        public int PaisId { get; set; }
    }
}
