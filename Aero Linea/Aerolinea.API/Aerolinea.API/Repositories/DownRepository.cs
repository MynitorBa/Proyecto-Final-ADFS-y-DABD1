using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class DownRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public DownRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<ResultadoVotoDTO> VotarComentario(VotarComentarioDTO dto)
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
                    int comentarioExiste = (int)await cmd.ExecuteScalarAsync();

                    if (comentarioExiste == 0)
                    {
                        throw new Exception("El comentario no existe.");
                    }
                }

                // 2. Verificar si el usuario ya votó en este comentario
                string queryVotoExistente = @"
                    SELECT Valor 
                    FROM Downs 
                    WHERE UsuarioID = @usuarioId 
                      AND ComentarioID = @comentarioId";

                int? valorAnterior = null;
                using (var cmd = new SqlCommand(queryVotoExistente, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", dto.UsuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);

                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado != null && resultado != DBNull.Value)
                    {
                        valorAnterior = (int)resultado;
                    }
                }

                string accion = "";
                int cambioEnDowns = 0;

                if (valorAnterior == null)
                {
                    // CASO 1: Usuario no ha votado - CREAR voto
                    string insertVoto = @"
                        INSERT INTO Downs (UsuarioID, ComentarioID, Valor, FechaVoto)
                        VALUES (@usuarioId, @comentarioId, @valor, GETDATE())";

                    using (var cmd = new SqlCommand(insertVoto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@usuarioId", dto.UsuarioId);
                        cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                        cmd.Parameters.AddWithValue("@valor", dto.Valor);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    cambioEnDowns = dto.Valor; // +1 o -1
                    accion = "votado";
                }
                else if (valorAnterior == dto.Valor)
                {
                    // CASO 2: Usuario ya votó lo mismo 
                    throw new Exception("Ya has votado de esta manera en este comentario.");
                }
                else
                {
                    // CASO 3: Usuario cambió su voto - ACTUALIZAR
                    string updateVoto = @"
                        UPDATE Downs 
                        SET Valor = @valor, FechaVoto = GETDATE()
                        WHERE UsuarioID = @usuarioId 
                          AND ComentarioID = @comentarioId";

                    using (var cmd = new SqlCommand(updateVoto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@usuarioId", dto.UsuarioId);
                        cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                        cmd.Parameters.AddWithValue("@valor", dto.Valor);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    // Si era +1 y ahora es -1: -2
                    // Si era -1 y ahora es +1: +2
                    cambioEnDowns = dto.Valor - valorAnterior.Value;
                    accion = "voto_cambiado";
                }

                // 3. Actualizar el contador de Downs en el Comentario
                string updateComentario = @"
                    UPDATE Comentario 
                    SET Downs = Downs + @cambio
                    WHERE ID = @comentarioId";

                using (var cmd = new SqlCommand(updateComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@cambio", cambioEnDowns);
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 4. Obtener el nuevo valor de Downs
                string queryNuevosDowns = "SELECT Downs FROM Comentario WHERE ID = @comentarioId";
                int nuevosDowns = 0;
                using (var cmd = new SqlCommand(queryNuevosDowns, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@comentarioId", dto.ComentarioId);
                    nuevosDowns = (int)await cmd.ExecuteScalarAsync();
                }

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

        public async Task<ResultadoVotoDTO> QuitarVoto(int usuarioId, int comentarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que el usuario tiene un voto en este comentario
                string queryVotoExistente = @"
                    SELECT Valor 
                    FROM Downs 
                    WHERE UsuarioID = @usuarioId 
                      AND ComentarioID = @comentarioId";

                int? valorAnterior = null;
                using (var cmd = new SqlCommand(queryVotoExistente, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);

                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado == null || resultado == DBNull.Value)
                    {
                        throw new Exception("No has votado en este comentario.");
                    }

                    valorAnterior = (int)resultado;
                }

                // 2. Eliminar el voto
                string deleteVoto = @"
                    DELETE FROM Downs 
                    WHERE UsuarioID = @usuarioId 
                      AND ComentarioID = @comentarioId";

                using (var cmd = new SqlCommand(deleteVoto, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 3. Actualizar el contador de Downs en el Comentario
                // Si era +1, restar 1 (cambio = -1)
                // Si era -1, sumar 1 (cambio = +1)
                int cambioEnDowns = -valorAnterior.Value;

                string updateComentario = @"
                    UPDATE Comentario 
                    SET Downs = Downs + @cambio
                    WHERE ID = @comentarioId";

                using (var cmd = new SqlCommand(updateComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@cambio", cambioEnDowns);
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 4. Obtener el nuevo valor de Downs
                string queryNuevosDowns = "SELECT Downs FROM Comentario WHERE ID = @comentarioId";
                int nuevosDowns = 0;
                using (var cmd = new SqlCommand(queryNuevosDowns, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    nuevosDowns = (int)await cmd.ExecuteScalarAsync();
                }

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

        public async Task<int?> ObtenerVotoUsuario(int usuarioId, int comentarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT Valor 
                FROM Downs 
                WHERE UsuarioID = @usuarioId 
                  AND ComentarioID = @comentarioId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            cmd.Parameters.AddWithValue("@comentarioId", comentarioId);

            var resultado = await cmd.ExecuteScalarAsync();

            if (resultado == null || resultado == DBNull.Value)
            {
                return null; // No ha votado
            }

            return (int)resultado; // 1 o -1
        }
    }
}