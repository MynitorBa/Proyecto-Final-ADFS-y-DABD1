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


        public async Task<ComentarioDTO> CrearComentarioRuta(int usuarioId, CrearComentarioRutaDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que la ruta existe
                string queryRuta = "SELECT COUNT(*) FROM Ruta WHERE ID = @rutaId";
                using (var cmd = new SqlCommand(queryRuta, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    if ((int)await cmd.ExecuteScalarAsync() == 0)
                        throw new Exception("La ruta especificada no existe.");
                }

                // 2. Verificar que el usuario voló en esa ruta (reservación Completada = 5)
                string queryVolo = @"
                    SELECT COUNT(*)
                    FROM Reservacion r
                    INNER JOIN Boleto  b ON b.ReservacionID = r.ID
                    INNER JOIN Vuelo   v ON v.ID = b.VueloID
                    WHERE r.UsuarioID       = @usuarioId
                      AND r.EstadoReservaID = 5
                      AND v.RutaID          = @rutaId";

                using (var cmd = new SqlCommand(queryVolo, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    if ((int)await cmd.ExecuteScalarAsync() == 0)
                        throw new Exception(
                            "Solo puedes reseñar rutas en las que hayas viajado con una reservación completada.");
                }

                // 3. Verificar que no haya comentado ya esta ruta
                string queryDuplicado = @"
                    SELECT COUNT(*) FROM Comentario
                    WHERE UsuarioID          = @usuarioId
                      AND RutaID             = @rutaId
                      AND ComentarioPadreID IS NULL";

                using (var cmd = new SqlCommand(queryDuplicado, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    if ((int)await cmd.ExecuteScalarAsync() > 0)
                        throw new Exception("Ya dejaste una reseña en esta ruta. Solo se permite una por ruta.");
                }

                // 4. Insertar comentario (ComentarioPadreID = NULL, con estrellas)
                string insert = @"
                    INSERT INTO Comentario
                        (UsuarioID, RutaID, Contenido, CantidadEstrellas, Downs, Fecha, ComentarioPadreID)
                    VALUES
                        (@usuarioId, @rutaId, @contenido, @estrellas, 0, GETDATE(), NULL);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                int comentarioId;
                using (var cmd = new SqlCommand(insert, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", dto.RutaId);
                    cmd.Parameters.AddWithValue("@contenido", dto.Contenido);
                    cmd.Parameters.AddWithValue("@estrellas", dto.CantidadEstrellas);
                    comentarioId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                transaction.Commit();

                return await ObtenerComentarioPorId(comentarioId);
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }


        public async Task<ComentarioDTO> CrearRespuesta(int usuarioId, CrearRespuestaDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que el comentario padre existe y pertenece a la misma ruta
                string queryPadre = @"
                    SELECT RutaID, ComentarioPadreID
                    FROM Comentario
                    WHERE ID = @padreId";

                int rutaId;
                using (var cmd = new SqlCommand(queryPadre, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@padreId", dto.ComentarioPadreId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("El comentario al que intentas responder no existe.");

                    rutaId = reader.GetInt32(0);
                }

                // 2. Insertar respuesta (CantidadEstrellas = NULL)
                string insert = @"
                    INSERT INTO Comentario
                        (UsuarioID, RutaID, Contenido, CantidadEstrellas, Downs, Fecha, ComentarioPadreID)
                    VALUES
                        (@usuarioId, @rutaId, @contenido, NULL, 0, GETDATE(), @padreId);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                int comentarioId;
                using (var cmd = new SqlCommand(insert, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    cmd.Parameters.AddWithValue("@rutaId", rutaId);
                    cmd.Parameters.AddWithValue("@contenido", dto.Contenido);
                    cmd.Parameters.AddWithValue("@padreId", dto.ComentarioPadreId);
                    comentarioId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                transaction.Commit();

                return await ObtenerComentarioPorId(comentarioId);
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }


        public async Task<List<ComentarioDTO>> ObtenerComentariosPorRuta(int rutaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo  AS Origen,
                    ad.Codigo  AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha,
                    c.ComentarioPadreID
                FROM Comentario c
                INNER JOIN Usuario    u  ON u.ID  = c.UsuarioID
                INNER JOIN Ruta       r  ON r.ID  = c.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                WHERE c.RutaID = @rutaId
                ORDER BY
                    ISNULL(c.ComentarioPadreID, c.ID),  -- agrupa respuestas junto a su padre
                    c.ComentarioPadreID,                 -- raíz primero
                    c.Fecha ASC";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            using var reader = await cmd.ExecuteReaderAsync();

            var comentarios = new List<ComentarioDTO>();
            while (await reader.ReadAsync())
            {
                comentarios.Add(MapearComentario(reader));
            }

            return comentarios;
        }


        public async Task<List<ComentarioConVotoDTO>> ObtenerTodosConVoto(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo  AS Origen,
                    ad.Codigo  AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha,
                    c.ComentarioPadreID,
                    d.Valor    AS VotoUsuario
                FROM Comentario c
                INNER JOIN Usuario    u  ON u.ID  = c.UsuarioID
                INNER JOIN Ruta       r  ON r.ID  = c.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                LEFT  JOIN Downs      d  ON d.ComentarioID = c.ID AND d.UsuarioID = @usuarioId
                ORDER BY c.Fecha DESC";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            using var reader = await cmd.ExecuteReaderAsync();

            var lista = new List<ComentarioConVotoDTO>();
            while (await reader.ReadAsync())
                lista.Add(MapearConVoto(reader));

            return lista;
        }


        public async Task<List<ComentarioDTO>> ObtenerComentariosPorUsuario(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo  AS Origen,
                    ad.Codigo  AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha,
                    c.ComentarioPadreID
                FROM Comentario c
                INNER JOIN Usuario    u  ON u.ID  = c.UsuarioID
                INNER JOIN Ruta       r  ON r.ID  = c.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                WHERE c.UsuarioID = @usuarioId
                ORDER BY c.Fecha DESC";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            using var reader = await cmd.ExecuteReaderAsync();

            var lista = new List<ComentarioDTO>();
            while (await reader.ReadAsync())
                lista.Add(MapearComentario(reader));

            return lista;
        }


        public async Task<List<ComentarioConVotoDTO>> ObtenerComentariosRutaConVoto(int rutaId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo  AS Origen,
                    ad.Codigo  AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha,
                    c.ComentarioPadreID,
                    d.Valor    AS VotoUsuario
                FROM Comentario c
                INNER JOIN Usuario    u  ON u.ID  = c.UsuarioID
                INNER JOIN Ruta       r  ON r.ID  = c.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                LEFT  JOIN Downs      d  ON d.ComentarioID = c.ID AND d.UsuarioID = @usuarioId
                WHERE c.RutaID = @rutaId
                ORDER BY
                    ISNULL(c.ComentarioPadreID, c.ID),
                    c.ComentarioPadreID,
                    c.Fecha ASC";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@rutaId", rutaId);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            using var reader = await cmd.ExecuteReaderAsync();

            var lista = new List<ComentarioConVotoDTO>();
            while (await reader.ReadAsync())
                lista.Add(MapearConVoto(reader));

            return lista;
        }


        private async Task<ComentarioDTO> ObtenerComentarioPorId(int comentarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    c.ID,
                    c.UsuarioID,
                    u.Username,
                    u.Nombre + ' ' + u.Apellido AS NombreCompleto,
                    c.RutaID,
                    ao.Codigo  AS Origen,
                    ad.Codigo  AS Destino,
                    c.CantidadEstrellas,
                    c.Contenido,
                    c.Downs,
                    c.Fecha,
                    c.ComentarioPadreID
                FROM Comentario c
                INNER JOIN Usuario    u  ON u.ID  = c.UsuarioID
                INNER JOIN Ruta       r  ON r.ID  = c.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                WHERE c.ID = @comentarioId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@comentarioId", comentarioId);
            using var reader = await cmd.ExecuteReaderAsync();

            if (await reader.ReadAsync())
                return MapearComentario(reader);

            return null;
        }

        private ComentarioDTO MapearComentario(SqlDataReader reader)
        {
            return new ComentarioDTO
            {
                Id = reader.GetInt32(0),
                UsuarioId = reader.GetInt32(1),
                Username = reader.GetString(2),
                NombreCompleto = reader.GetString(3),
                RutaId = reader.GetInt32(4),
                Origen = reader.GetString(5),
                Destino = reader.GetString(6),
                CantidadEstrellas = reader.IsDBNull(7) ? null : reader.GetInt32(7),
                Contenido = reader.GetString(8),
                Downs = reader.GetInt32(9),
                Fecha = reader.GetDateTime(10),
                ComentarioPadreId = reader.IsDBNull(11) ? null : reader.GetInt32(11)
            };
        }

        private ComentarioConVotoDTO MapearConVoto(SqlDataReader reader)
        {
            return new ComentarioConVotoDTO
            {
                Id = reader.GetInt32(0),
                UsuarioId = reader.GetInt32(1),
                Username = reader.GetString(2),
                NombreCompleto = reader.GetString(3),
                RutaId = reader.GetInt32(4),
                Origen = reader.GetString(5),
                Destino = reader.GetString(6),
                CantidadEstrellas = reader.IsDBNull(7) ? null : reader.GetInt32(7),
                Contenido = reader.GetString(8),
                Downs = reader.GetInt32(9),
                Fecha = reader.GetDateTime(10),
                ComentarioPadreId = reader.IsDBNull(11) ? null : reader.GetInt32(11),
                VotoUsuario = reader.IsDBNull(12) ? null : reader.GetInt32(12)
            };
        }
    }
}