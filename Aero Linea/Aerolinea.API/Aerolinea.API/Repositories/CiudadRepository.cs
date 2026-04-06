using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de ciudades. Permite buscar o crear ciudades asociadas a un pais
    /// dentro de la base de datos, reutilizando registros existentes cuando sea posible.
    /// </summary>
    public class CiudadRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public CiudadRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Busca una ciudad por nombre y pais. Si no existe la crea y retorna su ID.
        /// Acepta una conexion y transaccion opcionales para participar en operaciones mayores.
        /// </summary>
        public async Task<int> ObtenerOCrearId(string nombre, int paisId, SqlConnection connection, SqlTransaction transaction = null)
        {
            // Buscar si ya existe en ese país
            using var selectCmd = new SqlCommand(
                "SELECT ID FROM Ciudad WHERE Nombre = @Nombre AND PaisID = @PaisId", connection, transaction);
            selectCmd.Parameters.AddWithValue("@Nombre", nombre);
            selectCmd.Parameters.AddWithValue("@PaisId", paisId);
            var result = await selectCmd.ExecuteScalarAsync();
            if (result != null) return (int)result;

            // Si no existe, crearla
            using var insertCmd = new SqlCommand(
                "INSERT INTO Ciudad (Nombre, PaisID) OUTPUT INSERTED.ID VALUES (@Nombre, @PaisId)", connection, transaction);
            insertCmd.Parameters.AddWithValue("@Nombre", nombre);
            insertCmd.Parameters.AddWithValue("@PaisId", paisId);
            return (int)await insertCmd.ExecuteScalarAsync();
        }
    }
}
