using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AvionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public AvionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<Avion>> ObtenerTodos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ID, Modelo, Marca, CapacidadPasajeros
                FROM Avion 
                ORDER BY Marca, Modelo";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var aviones = new List<Avion>();
            while (await reader.ReadAsync())
            {
                aviones.Add(new Avion
                {
                    Id = reader.GetInt32(0),
                    Modelo = reader.GetString(1),
                    Marca = reader.GetString(2),
                    CapacidadPasajeros = reader.GetInt32(3)
                });
            }

            return aviones;
        }

        public async Task<Avion?> ObtenerPorId(int id)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT ID, Modelo, Marca, CapacidadPasajeros
                FROM Avion 
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Avion
                {
                    Id = reader.GetInt32(0),
                    Modelo = reader.GetString(1),
                    Marca = reader.GetString(2),
                    CapacidadPasajeros = reader.GetInt32(3)
                };
            }

            return null;
        }

        public async Task<int> Crear(Avion avion)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO Avion (Modelo, Marca, CapacidadPasajeros)
                VALUES (@Modelo, @Marca, @CapacidadPasajeros);
                SELECT CAST(SCOPE_IDENTITY() as int);";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Modelo", avion.Modelo);
            command.Parameters.AddWithValue("@Marca", avion.Marca);
            command.Parameters.AddWithValue("@CapacidadPasajeros", avion.CapacidadPasajeros);

            var nuevoId = await command.ExecuteScalarAsync();
            return Convert.ToInt32(nuevoId);
        }

        public async Task<bool> Actualizar(Avion avion)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                UPDATE Avion 
                SET Modelo = @Modelo,
                    Marca = @Marca,
                    CapacidadPasajeros = @CapacidadPasajeros
                WHERE ID = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", avion.Id);
            command.Parameters.AddWithValue("@Modelo", avion.Modelo);
            command.Parameters.AddWithValue("@Marca", avion.Marca);
            command.Parameters.AddWithValue("@CapacidadPasajeros", avion.CapacidadPasajeros);

            var filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        
    }
}