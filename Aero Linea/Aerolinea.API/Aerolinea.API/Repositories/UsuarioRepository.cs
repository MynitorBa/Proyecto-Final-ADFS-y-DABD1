using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class UsuarioRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public UsuarioRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task CrearUsuario(Usuario usuario)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string sql = @"
                INSERT INTO Usuario
                (Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, Edad, NumeroEmergencia, NacionalidadId)
                VALUES
                (@Correo, @ContrasenaHash, @Pasaporte, @Username, @Nombre, @Apellido, @Edad, @NumeroEmergencia, @NacionalidadId)";

            using var command = new SqlCommand(sql, connection);

            command.Parameters.AddWithValue("@Correo", usuario.Correo);
            command.Parameters.AddWithValue("@ContrasenaHash", usuario.ContrasenaHash);
            command.Parameters.AddWithValue("@Pasaporte", usuario.Pasaporte);
            command.Parameters.AddWithValue("@Username", usuario.Username);
            command.Parameters.AddWithValue("@Nombre", usuario.Nombre);
            command.Parameters.AddWithValue("@Apellido", usuario.Apellido);
            command.Parameters.AddWithValue("@Edad", usuario.Edad);
            command.Parameters.AddWithValue("@NumeroEmergencia", usuario.NumeroEmergencia ?? "");
            command.Parameters.AddWithValue("@NacionalidadId", usuario.NacionalidadId);

            await command.ExecuteNonQueryAsync();
        }
    }
}
