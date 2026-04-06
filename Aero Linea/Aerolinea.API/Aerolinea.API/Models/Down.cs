namespace Aerolinea.API.Models
{
    /// <summary>
    /// Entidad que registra el voto de un usuario sobre un comentario.
    /// El campo Valor indica el tipo de voto: 1 para positivo y -1 para negativo.
    /// Garantiza que cada usuario solo pueda votar una vez por comentario.
    /// </summary>
    public class Down
    {
        public int Id { get; set; }
        public int UsuarioId { get; set; }
        public int ComentarioId { get; set; }
        public int Valor { get; set; } // 1 o -1
        public DateTime FechaVoto { get; set; }
    }
}
