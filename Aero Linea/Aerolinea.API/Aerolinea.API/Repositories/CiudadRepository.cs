using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class CiudadRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public CiudadRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // Busca o crea la ciudad y devuelve su Id
        public async Task<int> ObtenerOCrearId(string nombre, int paisId, SqlConnection connection)
        {
            // Buscar si ya existe en ese país
            using var selectCmd = new SqlCommand(
                "SELECT ID FROM Ciudad WHERE Nombre = @Nombre AND PaisID = @PaisId", connection);
            selectCmd.Parameters.AddWithValue("@Nombre", nombre);
            selectCmd.Parameters.AddWithValue("@PaisId", paisId);
            var result = await selectCmd.ExecuteScalarAsync();

            if (result != null) return (int)result;

            // Si no existe, crearla
            using var insertCmd = new SqlCommand(
                "INSERT INTO Ciudad (Nombre, PaisID) OUTPUT INSERTED.ID VALUES (@Nombre, @PaisId)", connection);
            insertCmd.Parameters.AddWithValue("@Nombre", nombre);
            insertCmd.Parameters.AddWithValue("@PaisId", paisId);
            return (int)await insertCmd.ExecuteScalarAsync();
        }
    }
}