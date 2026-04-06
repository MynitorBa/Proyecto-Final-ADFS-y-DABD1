namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que representa un comentario o resena publicado por un usuario sobre una ruta.
    /// Incluye calificacion por estrellas, contenido textual, conteo de votos negativos y fecha.
    /// </summary>
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
