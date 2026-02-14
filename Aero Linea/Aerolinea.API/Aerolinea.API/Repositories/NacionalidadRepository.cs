using Aerolinea.API.Models;
using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class NacionalidadRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public NacionalidadRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<Nacionalidad>> ObtenerTodas()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<Nacionalidad>();
            using var command = new SqlCommand("SELECT Id, Nombre FROM Nacionalidad ORDER BY Nombre", connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
                lista.Add(new Nacionalidad { Id = reader.GetInt32(0), Nombre = reader.GetString(1) });

            return lista;
        }

        // Busca o crea la nacionalidad y devuelve su Id
        public async Task<int> ObtenerOCrearId(string nombre, SqlConnection connection)
        {
            // Buscar si ya existe
            using var selectCmd = new SqlCommand("SELECT Id FROM Nacionalidad WHERE Nombre = @Nombre", connection);
            selectCmd.Parameters.AddWithValue("@Nombre", nombre);
            var result = await selectCmd.ExecuteScalarAsync();

            if (result != null) return (int)result;

            // Si no existe, crearla
            using var insertCmd = new SqlCommand(
                "INSERT INTO Nacionalidad (Nombre) OUTPUT INSERTED.Id VALUES (@Nombre)", connection);
            insertCmd.Parameters.AddWithValue("@Nombre", nombre);
            return (int)await insertCmd.ExecuteScalarAsync();
        }
    }
}