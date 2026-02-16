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


            var query = @"
                SELECT ID, Nombre, Apellido, RolID
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
                    RolID = reader.GetInt32(3)
                });
            }

            return tripulantes;
        }

        public async Task<Tripulante?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ID, Nombre, Apellido, RolID
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
                    RolID = reader.GetInt32(3)
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
                INSERT INTO MiembroTripulacion (Nombre, Apellido, RolID)
                VALUES (@Nombre, @Apellido, @RolID);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        public async Task<bool> Actualizar(Tripulante tripulante)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                UPDATE MiembroTripulacion 
                SET Nombre = @Nombre,
                    Apellido = @Apellido,
                    RolID = @RolID
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", tripulante.Id);
            command.Parameters.AddWithValue("@Nombre", tripulante.Nombre);
            command.Parameters.AddWithValue("@Apellido", tripulante.Apellido);
            command.Parameters.AddWithValue("@RolID", tripulante.RolID);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
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