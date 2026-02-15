namespace Aerolinea.API.Models
{
    public class Comentario
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public int RutaId { get; set; }
        public int CantidadEstrellas { get; set; }
        public string Contenido { get; set; }
        public int Downs { get; set; }
        public DateTime Fecha { get; set; }
    }
}