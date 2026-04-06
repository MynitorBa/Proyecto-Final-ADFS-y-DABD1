namespace Aerolinea.API.DTOs
{
    /// <summary>
    /// DTO de peticion para publicar un nuevo comentario o resena sobre una ruta especifica.
    /// Contiene identificador de la ruta, calificacion por estrellas y texto del comentario.
    /// </summary>
    public class CrearComentarioRutaDTO
    {
        public int RutaId { get; set; }
        public int CantidadEstrellas { get; set; }
        public string Contenido { get; set; }
    }

    /// <summary>
    /// DTO de peticion para publicar una respuesta a un comentario existente.
    /// Contiene el identificador del comentario padre y el contenido de la respuesta.
    /// </summary>
    public class CrearRespuestaDTO
    {
        public int ComentarioPadreId { get; set; }
        public string Contenido { get; set; }
    }

    /// <summary>
    /// DTO de lectura que expone la informacion de un comentario o respuesta publicada en el sistema.
    /// Incluye datos del autor, ruta, estrellas, contenido, votos y fecha de publicacion.
    /// </summary>
    public class ComentarioDTO
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public string Username { get; set; }
        public string NombreCompleto { get; set; }
        public int RutaId { get; set; }
        public string Origen { get; set; }
        public string Destino { get; set; }
        public int? CantidadEstrellas { get; set; }
        public string Contenido { get; set; }
        public int Downs { get; set; }
        public DateTime Fecha { get; set; }
        public int? ComentarioPadreId { get; set; }
    }

    /// <summary>
    /// Extension de ComentarioDTO que incluye el voto del usuario autenticado sobre ese comentario.
    /// El campo VotoUsuario puede ser 1, -1 o null si el usuario no ha votado.
    /// </summary>
    public class ComentarioConVotoDTO : ComentarioDTO
    {
        public int? VotoUsuario { get; set; }  // 1, -1 o null si no votó
    }
}
