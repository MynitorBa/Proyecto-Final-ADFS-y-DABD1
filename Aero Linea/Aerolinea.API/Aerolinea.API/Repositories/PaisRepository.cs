using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class PaisRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public PaisRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // Busca o crea el país y devuelve su Id
        public async Task<int> ObtenerOCrearId(string nombre, SqlConnection connection)
        {
            // Buscar si ya existe
            using var selectCmd = new SqlCommand("SELECT ID FROM Pais WHERE Nombre = @Nombre", connection);
            selectCmd.Parameters.AddWithValue("@Nombre", nombre);
            var result = await selectCmd.ExecuteScalarAsync();

            if (result != null) return (int)result;

            // Si no existe, crearlo
            using var insertCmd = new SqlCommand(
                "INSERT INTO Pais (Nombre) OUTPUT INSERTED.ID VALUES (@Nombre)", connection);
            insertCmd.Parameters.AddWithValue("@Nombre", nombre);
            return (int)await insertCmd.ExecuteScalarAsync();
        }
    }
}