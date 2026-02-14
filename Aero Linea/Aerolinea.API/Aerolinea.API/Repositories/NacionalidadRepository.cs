using Microsoft.Data.SqlClient;
using Aerolinea.API.Models;

namespace Aerolinea.API.Repositories
{
    public class NacionalidadRepository
    {
        private readonly string _connectionString;

        public NacionalidadRepository(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection");
        }

        public async Task<List<Nacionalidad>> ObtenerTodas()
        {
            var lista = new List<Nacionalidad>();

            using var connection = new SqlConnection(_connectionString);
            using var command = new SqlCommand(
                "SELECT Id, Pais FROM Nacionalidad",
                connection
            );

            await connection.OpenAsync();

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                lista.Add(new Nacionalidad
                {
                    Id = reader.GetInt32(0),
                    Pais = reader.GetString(1)
                });
            }

            return lista;
        }
    }
}
