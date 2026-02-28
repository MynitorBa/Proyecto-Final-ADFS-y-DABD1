namespace Aerolinea.API.DTOs
{
    public class CrearComentarioRutaDTO
    {
        public int RutaId { get; set; }
        public int CantidadEstrellas { get; set; }
        public string Contenido { get; set; }
    }

    public class CrearRespuestaDTO
    {
        public int ComentarioPadreId { get; set; }
        public string Contenido { get; set; }
    }

    // Respuesta base
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

    public class ComentarioConVotoDTO : ComentarioDTO
    {
        public int? VotoUsuario { get; set; }  // 1, -1 o null si no votó
    }
}