using Aerolinea.API.Data;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class UsuarioRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private readonly NacionalidadRepository _nacRepository;

        public UsuarioRepository(DbConnectionFactory connectionFactory, NacionalidadRepository nacRepository)
        {
            _connectionFactory = connectionFactory;
            _nacRepository = nacRepository;
        }

        public async Task<int> CrearUsuario(Usuario usuario)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string sql = @"
                INSERT INTO Usuario
                (Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, Telefono, FechaNacimiento, Ciudad, Pais, RolID)
                OUTPUT INSERTED.Id
                VALUES
                (@Correo, @ContrasenaHash, @Pasaporte, @Username, @Nombre, @Apellido, @Telefono, @FechaNacimiento, @Ciudad, @Pais, @RolID)";

            using var command = new SqlCommand(sql, connection);
            command.Parameters.AddWithValue("@Correo", usuario.Correo);
            command.Parameters.AddWithValue("@ContrasenaHash", usuario.ContrasenaHash);
            command.Parameters.AddWithValue("@Pasaporte", usuario.Pasaporte);
            command.Parameters.AddWithValue("@Username", usuario.Username);
            command.Parameters.AddWithValue("@Nombre", usuario.Nombre);
            command.Parameters.AddWithValue("@Apellido", usuario.Apellido);
            command.Parameters.AddWithValue("@Telefono", usuario.Telefono ?? "");
            command.Parameters.AddWithValue("@FechaNacimiento", usuario.FechaNacimiento);
            command.Parameters.AddWithValue("@Ciudad", usuario.Ciudad ?? "");
            command.Parameters.AddWithValue("@Pais", usuario.Pais ?? "");
            command.Parameters.AddWithValue("@RolID", usuario.RolID);

            return (int)await command.ExecuteScalarAsync();
        }

        public async Task AgregarNacionalidades(int usuarioId, List<string> nacionalidades)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            foreach (var nombre in nacionalidades.Where(n => !string.IsNullOrWhiteSpace(n)))
            {
                // Obtener o crear el Id de la nacionalidad
                int nacId = await _nacRepository.ObtenerOCrearId(nombre, connection);

                string sql = "INSERT INTO UsuarioNacionalidad (UsuarioId, NacionalidadId) VALUES (@UsuarioId, @NacionalidadId)";
                using var cmd = new SqlCommand(sql, connection);
                cmd.Parameters.AddWithValue("@UsuarioId", usuarioId);
                cmd.Parameters.AddWithValue("@NacionalidadId", nacId);
                await cmd.ExecuteNonQueryAsync();
            }
        }

        public async Task<Usuario?> ObtenerPorCorreoOUsername(string valor)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT Id, Correo, Username, ContrasenaHash FROM Usuario WHERE Correo = @v OR Username = @v",
                connection);
            command.Parameters.AddWithValue("@v", valor);

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

            var c = new RegisterConstraint();

            using (var cmd = new SqlCommand("SELECT COUNT(*) FROM Usuario WHERE Correo = @v", connection))
            { cmd.Parameters.AddWithValue("@v", correo); c.CorreoExiste = (int)await cmd.ExecuteScalarAsync() > 0; }

            using (var cmd = new SqlCommand("SELECT COUNT(*) FROM Usuario WHERE Username = @v", connection))
            { cmd.Parameters.AddWithValue("@v", username); c.UsernameExiste = (int)await cmd.ExecuteScalarAsync() > 0; }

            using (var cmd = new SqlCommand("SELECT COUNT(*) FROM Usuario WHERE Pasaporte = @v", connection))
            { cmd.Parameters.AddWithValue("@v", pasaporte); c.PasaporteExiste = (int)await cmd.ExecuteScalarAsync() > 0; }

            return c;
        }
    }
}