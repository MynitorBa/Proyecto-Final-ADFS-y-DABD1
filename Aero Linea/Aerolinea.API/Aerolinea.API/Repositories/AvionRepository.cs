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
                ORDER BY ID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var aviones = new List<Avion>();
            while (await reader.ReadAsync())
            {
                aviones.Add(new Avion
                {
                    Id = reader.GetInt32(0),           // ID
                    Modelo = reader.GetString(1),      // Modelo
                    Marca = reader.GetString(2),       // Marca
                    CapacidadPasajeros = reader.GetInt32(3)  // CapacidadPasajeros
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
                    Id = reader.GetInt32(0),           // ID
                    Modelo = reader.GetString(1),      // Modelo
                    Marca = reader.GetString(2),       // Marca
                    CapacidadPasajeros = reader.GetInt32(3)  // CapacidadPasajeros
                };
            }

            return null;
        }
    }
}