using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de votos (downs) en comentarios. Permite votar a favor o en contra
    /// de un comentario, cambiar un voto existente, quitarlo y consultar el voto
    /// actual de un usuario en un comentario especifico.
    /// </summary>
    public class DownRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public DownRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        //  VOTAR  (crear o actualizar voto)
        /// <summary>
        /// Registra o actualiza el voto de un usuario en un comentario. Si es nuevo
        /// crea el registro; si ya voto igual lanza excepcion; si cambio de voto
        /// actualiza el registro. Actualiza el contador de downs en el comentario.
        /// Retorna el resultado con el nuevo conteo y la accion realizada.
        /// </summary>
        public async Task<ResultadoVotoDTO> VotarComentario(int usuarioId, VotarComentarioDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que el comentario existe
                string queryComentario = "SELECT COUNT(*) FROM Comentario WHERE ID = @comentarioId";
                using (var cmd = new SqlCommand(queryComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                    if ((int)await cmd.ExecuteScalarAsync() == 0)
                        throw new Exception("El comentario no existe.");
                }

                // 2. Verificar si ya tiene un voto previo
                string queryVotoExistente = @"
                    SELECT Valor FROM Downs
                    WHERE UsuarioID = @usuarioId AND ComentarioID = @comentarioId";

                int? valorAnterior = null;
                using (var cmd = new SqlCommand(queryVotoExistente, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado != null && resultado != DBNull.Value)
                        valorAnterior = (int)resultado;
                }

                string accion;
                int cambioEnDowns;

                if (valorAnterior == null)
                {
                    // Nuevo voto
                    string insert = @"
                        INSERT INTO Downs (UsuarioID, ComentarioID, Valor, FechaVoto)
                        VALUES (@usuarioId, @comentarioId, @valor, GETDATE())";

                    using (var cmd = new SqlCommand(insert, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                        cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                        cmd.Parameters.AddWithValue("@valor", dto.Valor);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    cambioEnDowns = dto.Valor;
                    accion = "votado";
                }
                else if (valorAnterior == dto.Valor)
                {
                    throw new Exception("Ya votaste de esta manera en este comentario.");
                }
                else
                {
                    // Cambiar voto
                    string update = @"
                        UPDATE Downs SET Valor = @valor, FechaVoto = GETDATE()
                        WHERE UsuarioID = @usuarioId AND ComentarioID = @comentarioId";

                    using (var cmd = new SqlCommand(update, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                        cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                        cmd.Parameters.AddWithValue("@valor", dto.Valor);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    cambioEnDowns = dto.Valor - valorAnterior.Value; // ±2
                    accion = "voto_cambiado";
                }

                // 3. Actualizar contador en Comentario
                string updateComentario = @"
                    UPDATE Comentario SET Downs = Downs + @cambio WHERE ID = @comentarioId";

                using (var cmd = new SqlCommand(updateComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@cambio", cambioEnDowns);
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                int nuevosDowns = await ObtenerDowns(dto.ComentarioId, connection, transaction);

                transaction.Commit();

                return new ResultadoVotoDTO
                {
                    ComentarioId = dto.ComentarioId,
                    NuevosDowns = nuevosDowns,
                    Accion = accion,
                    ValorAnterior = valorAnterior,
                    ValorNuevo = dto.Valor
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        //  QUITAR VOTO
        /// <summary>
        /// Elimina el voto de un usuario en un comentario y actualiza el contador de downs.
        /// Lanza excepcion si el usuario no habia votado en ese comentario.
        /// Retorna el resultado con el nuevo conteo y la accion 'voto_eliminado'.
        /// </summary>
        public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                string queryVoto = @"
                    SELECT Valor FROM Downs
                    WHERE UsuarioID = @usuarioId AND ComentarioID = @comentarioId";

                int? valorAnterior = null;
                using (var cmd = new SqlCommand(queryVoto, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado == null || resultado == DBNull.Value)
                        throw new Exception("No has votado en este comentario.");
                    valorAnterior = (int)resultado;
                }

                string delete = @"
                    DELETE FROM Downs
                    WHERE UsuarioID = @usuarioId AND ComentarioID = @comentarioId";

                using (var cmd = new SqlCommand(delete, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                string updateComentario = @"
                    UPDATE Comentario SET Downs = Downs + @cambio WHERE ID = @comentarioId";

                using (var cmd = new SqlCommand(updateComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@cambio", -valorAnterior.Value);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                int nuevosDowns = await ObtenerDowns(comentarioId, connection, transaction);

                transaction.Commit();

                return new ResultadoVotoDTO
                {
                    ComentarioId = comentarioId,
                    NuevosDowns = nuevosDowns,
                    Accion = "voto_eliminado",
                    ValorAnterior = valorAnterior,
                    ValorNuevo = null
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        //  OBTENER VOTO DE UN USUARIO EN UN COMENTARIO
        /// <summary>
        /// Retorna el valor del voto del usuario en el comentario indicado.
        /// Retorna null si el usuario no ha votado en ese comentario.
        /// </summary>
        public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT Valor FROM Downs
                WHERE UsuarioID = @usuarioId AND ComentarioID = @comentarioId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            cmd.Parameters.AddWithValue("@comentarioId", comentarioId);

            var resultado = await cmd.ExecuteScalarAsync();
            return resultado == null || resultado == DBNull.Value ? null : (int?)resultado;
        }

        //  HELPER PRIVADO
        private async Task<int> ObtenerDowns(int comentarioId, SqlConnection connection, SqlTransaction transaction)
        {
            string query = "SELECT Downs FROM Comentario WHERE ID = @comentarioId";
            using var cmd = new SqlCommand(query, connection, transaction);
            cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
            return (int)await cmd.ExecuteScalarAsync();
        }
    }
}
