using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de paises. Permite buscar o crear registros de pais por nombre,
    /// reutilizando entradas existentes para mantener la integridad referencial.
    /// </summary>
    public class PaisRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public PaisRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Busca un pais por nombre. Si no existe lo crea y retorna su ID.
        /// Acepta una conexion y transaccion opcionales para participar en transacciones externas.
        /// </summary>
        public async Task<int> ObtenerOCrearId(string nombre, SqlConnection connection, SqlTransaction transaction = null)
        {
            // Buscar si ya existe
            using var selectCmd = new SqlCommand("SELECT ID FROM Pais WHERE Nombre = @Nombre", connection, transaction);
            selectCmd.Parameters.AddWithValue("@Nombre", nombre);
            var result = await selectCmd.ExecuteScalarAsync();
            if (result != null) return (int)result;

            // Si no existe, crearlo
            using var insertCmd = new SqlCommand(
                "INSERT INTO Pais (Nombre) OUTPUT INSERTED.ID VALUES (@Nombre)", connection, transaction);
            insertCmd.Parameters.AddWithValue("@Nombre", nombre);
            return (int)await insertCmd.ExecuteScalarAsync();
        }
    }
}
