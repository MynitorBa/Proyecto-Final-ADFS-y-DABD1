using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class TripulacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public TripulacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<Tripulante>> ObtenerTodos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // La columna Imagen está directamente en MiembroTripulacion
            var query = @"
                SELECT ID, Nombre, Apellido, RolID, Imagen
                FROM MiembroTripulacion 
                ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var tripulantes = new List<Tripulante>();
            while (await reader.ReadAsync())
            {
                tripulantes.Add(new Tripulante
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                });
            }

            return tripulantes;
        }

        public async Task<Tripulante?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ID, Nombre, Apellido, RolID, Imagen
                FROM MiembroTripulacion 
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Tripulante
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    ImagenBase64 = reader.IsDBNull(4) ? null : reader.GetString(4)
                };
            }

            return null;
        }

        public async Task<string?> ObtenerNombreRol(int rolId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT Cargo FROM RolTripulacion WHERE ID = @RolId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@RolId", rolId);

            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }

        public async Task<int> Crear(Tripulante tripulante)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO MiembroTripulacion (Nombre, Apellido, RolID, Imagen)
                VALUES (@Nombre, @Apellido, @RolID, @Imagen);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);
            command.Parameters.AddWithValue("@Imagen", (object?)tripulante.ImagenBase64 ?? DBNull.Value);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        public async Task<bool> Actualizar(Tripulante tripulante)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Solo actualiza la imagen si se proporciona una nueva
            string query;
            if (tripulante.ImagenBase64 != null)
            {
                query = @"
                    UPDATE MiembroTripulacion 
                    SET Nombre = @Nombre, Apellido = @Apellido, RolID = @RolID, Imagen = @Imagen
                    WHERE ID = @Id";
            }
            else
            {
                query = @"
                    UPDATE MiembroTripulacion 
                    SET Nombre = @Nombre, Apellido = @Apellido, RolID = @RolID
                    WHERE ID = @Id";
            }

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulante.Id);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);

            if (tripulante.ImagenBase64 != null)
                command.Parameters.AddWithValue("@Imagen", tripulante.ImagenBase64);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        public async Task<bool> Eliminar(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "DELETE FROM MiembroTripulacion WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        // ===== IMAGEN (columna directa en MiembroTripulacion) =====

        public async Task GuardarImagen(int tripulanteId, string imagenBase64)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE MiembroTripulacion SET Imagen = @Imagen WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulanteId);
            command.Parameters.AddWithValue("@Imagen", imagenBase64);
            await command.ExecuteNonQueryAsync();
        }

        public async Task EliminarImagen(int tripulanteId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE MiembroTripulacion SET Imagen = NULL WHERE ID = @Id";
            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulanteId);
            await command.ExecuteNonQueryAsync();
        }

        public async Task<List<RolTripulacion>> ObtenerRoles()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "SELECT ID, Cargo FROM RolTripulacion ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var roles = new List<RolTripulacion>();
            while (await reader.ReadAsync())
            {
                roles.Add(new RolTripulacion
                {
                    Id = reader.GetInt32(0),
                    Cargo = reader.GetString(1)
                });
            }

            return roles;
        }
    }
}