using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class ComentarioRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public ComentarioRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<ComentarioDTO> CrearComentario(CrearComentarioDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que el usuario no tenga ya un comentario en esta ruta
                string queryVerificar = @"
                    SELECT COUNT(*)
                    FROM Comentario
                    WHERE UsuarioID = @usuarioId
                      AND RutaID = @rutaId";

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", dto.UsuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);

                    int yaComentado = (int)await cmd.ExecuteScalarAsync();

                    if (yaComentado > 0)
                    {
                        throw new Exception("Ya has comentado en esta ruta. Solo puedes hacer un comentario por ruta.");
                    }
                }

                // 2. Verificar que la ruta existe
                string queryRuta = "SELECT COUNT(*) FROM Ruta WHERE ID = @rutaId";
                using (var cmd = new SqlCommand(queryRuta, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    int rutaExiste = (int)await cmd.ExecuteScalarAsync();

                    if (rutaExiste == 0)
                    {
                        throw new Exception("La ruta especificada no existe.");
                    }
                }

                // 3. Crear el comentario
                string insertComentario = @"
                    INSERT INTO Comentario (UsuarioID, RutaID, CantidadEstrellas, Contenido, Downs, Fecha)
                    VALUES (@usuarioId, @rutaId, @cantidadEstrellas, @contenido, 0, GETDATE());
                    SELECT SCOPE_IDENTITY();";

                int comentarioId;
                using (var cmd = new SqlCommand(insertComentario, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", dto.UsuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    cmd.Parameters.AddWithValue("@cantidadEstrellas", dto.CantidadEstrellas);
                    cmd.Parameters.AddWithValue("@contenido", dto.Contenido);

                    comentarioId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // 4. Obtener el comentario completo con toda la información
                string queryCompleto = @"
                    SELECT 
                        c.ID,
                        c.UsuarioID,
                        u.Username,
                        u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                        c.RutaID,
                        ao.Codigo AS Origen,
                        ad.Codigo AS Destino,
                        c.CantidadEstrellas,
                        c.Contenido,
                        c.Downs,
                        c.Fecha
                    FROM Comentario c
                    INNER JOIN Usuario u ON c.UsuarioID = u.ID
                    INNER JOIN Ruta r ON c.RutaID = r.ID
                    INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                    INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                    WHERE c.ID = @comentarioId";

                ComentarioDTO comentario = null;

                using (var cmd = new SqlCommand(queryCompleto, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
                    using var reader = await cmd.ExecuteReaderAsync();

                    if (await reader.ReadAsync())
                    {
                        comentario = new ComentarioDTO
                        {
                            Id = reader.GetInt32(0),
                            UsuarioId = reader.GetInt32(1),
                            Username = reader.GetString(2),
                            NombreCompleto = reader.GetString(3),
                            RutaId = reader.GetInt32(4),
                            Origen = reader.GetString(5),
                            Destino = reader.GetString(6),
                            CantidadEstrellas = reader.GetInt32(7),
                            Contenido = reader.GetString(8),
                            Downs = reader.GetInt32(9),
                            Fecha = reader.GetDateTime(10)
                        };
                    }
                }

                transaction.Commit();
                return comentario;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
        {
            var comentarios = new List<ComentarioDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo AS Origen,
                    ad.Codigo AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha
                FROM Comentario c
                INNER JOIN Usuario u ON c.UsuarioID = u.ID
                INNER JOIN Ruta r ON c.RutaID = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                WHERE c.RutaID = @rutaId
                ORDER BY c.Downs DESC, c.Fecha DESC";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);

            using var reader = await cmd.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                comentarios.Add(new ComentarioDTO
                {
                    Id = reader.GetInt32(0),
                    UsuarioId = reader.GetInt32(1),
                    Username = reader.GetString(2),
                    NombreCompleto = reader.GetString(3),
                    RutaId = reader.GetInt32(4),
                    Origen = reader.GetString(5),
                    Destino = reader.GetString(6),
                    CantidadEstrellas = reader.GetInt32(7),
                    Contenido = reader.GetString(8),
                    Downs = reader.GetInt32(9),
                    Fecha = reader.GetDateTime(10)
                });
            }

            return comentarios;
        }
    }
}