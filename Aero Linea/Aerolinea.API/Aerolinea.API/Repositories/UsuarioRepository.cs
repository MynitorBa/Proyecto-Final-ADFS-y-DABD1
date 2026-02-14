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

        public async Task<Usuario?> ObtenerPorCorreoOUsername(string valor)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT Id, Correo, Username, ContrasenaHash
                FROM Usuario
                WHERE Correo = @valor OR Username = @valor";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@valor", valor);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Usuario
                {
                    Id = reader.GetInt32(0),
                    Correo = reader.GetString(1),
                    Username = reader.GetString(2),
                    ContrasenaHash = reader.GetString(3)
                };
            }

            return null;
        }

        public async Task<RegisterConstraint> VerificarExistencia(string correo, string username, string pasaporte)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var constraint = new RegisterConstraint();

            // Verificar correo
            string queryCorreo = "SELECT COUNT(*) FROM Usuario WHERE Correo = @correo";
            using (var command = new SqlCommand(queryCorreo, connection))
            {
                command.Parameters.AddWithValue("@correo", correo);
                constraint.CorreoExiste = (int)await command.ExecuteScalarAsync() > 0;
            }

            // Verificar username
            string queryUsername = "SELECT COUNT(*) FROM Usuario WHERE Username = @username";
            using (var command = new SqlCommand(queryUsername, connection))
            {
                command.Parameters.AddWithValue("@username", username);
                constraint.UsernameExiste = (int)await command.ExecuteScalarAsync() > 0;
            }

            // Verificar pasaporte
            string queryPasaporte = "SELECT COUNT(*) FROM Usuario WHERE Pasaporte = @pasaporte";
            using (var command = new SqlCommand(queryPasaporte, connection))
            {
                command.Parameters.AddWithValue("@pasaporte", pasaporte);
                constraint.PasaporteExiste = (int)await command.ExecuteScalarAsync() > 0;
            }

            return constraint;
        }
    }
}
