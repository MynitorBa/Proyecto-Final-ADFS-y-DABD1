using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class UsuarioRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private readonly NacionalidadRepository _nacionalidadRepository;
        private readonly CiudadRepository _ciudadRepository;

        public UsuarioRepository(
            DbConnectionFactory connectionFactory,
            NacionalidadRepository nacionalidadRepository,
            CiudadRepository ciudadRepository)
        {
            _connectionFactory = connectionFactory;
            _nacionalidadRepository = nacionalidadRepository;
            _ciudadRepository = ciudadRepository;
        }

        public async Task<int> CrearUsuario(Usuario usuario)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO Usuario (Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, 
                                    Telefono, FechaNacimiento, CiudadId, RolID)
                OUTPUT INSERTED.Id
                VALUES (@Correo, @ContrasenaHash, @Pasaporte, @Username, @Nombre, @Apellido, 
                        @Telefono, @FechaNacimiento, @CiudadId, @RolID)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Correo", usuario.Correo);
            command.Parameters.AddWithValue("@ContrasenaHash", usuario.ContrasenaHash);
            command.Parameters.AddWithValue("@Pasaporte", usuario.Pasaporte);
            command.Parameters.AddWithValue("@Username", usuario.Username);
            command.Parameters.AddWithValue("@Nombre", usuario.Nombre);
            command.Parameters.AddWithValue("@Apellido", usuario.Apellido);
            command.Parameters.AddWithValue("@Telefono", usuario.Telefono);
            command.Parameters.AddWithValue("@FechaNacimiento", usuario.FechaNacimiento);
            command.Parameters.AddWithValue("@CiudadId", usuario.CiudadId);
            command.Parameters.AddWithValue("@RolID", usuario.RolID);

            return (int)await command.ExecuteScalarAsync();
        }

        public async Task AgregarNacionalidades(int usuarioId, List<string> nacionalidades)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            foreach (var nacionalidad in nacionalidades)
            {
                int nacionalidadId = await _nacionalidadRepository.ObtenerOCrearId(nacionalidad, connection);

                using var command = new SqlCommand(
                    "INSERT INTO UsuarioNacionalidad (UsuarioId, NacionalidadId) VALUES (@UsuarioId, @NacionalidadId)",
                    connection);
                command.Parameters.AddWithValue("@UsuarioId", usuarioId);
                command.Parameters.AddWithValue("@NacionalidadId", nacionalidadId);
                await command.ExecuteNonQueryAsync();
            }
        }

        public async Task<RegisterConstraint> VerificarExistencia(string correo, string username, string pasaporte)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var constraint = new RegisterConstraint();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM Usuario WHERE Correo = @Correo", connection);
            command.Parameters.AddWithValue("@Correo", correo);
            constraint.CorreoExiste = (int)await command.ExecuteScalarAsync() > 0;

            command.CommandText = "SELECT COUNT(*) FROM Usuario WHERE Username = @Username";
            command.Parameters.Clear();
            command.Parameters.AddWithValue("@Username", username);
            constraint.UsernameExiste = (int)await command.ExecuteScalarAsync() > 0;

            command.CommandText = "SELECT COUNT(*) FROM Usuario WHERE Pasaporte = @Pasaporte";
            command.Parameters.Clear();
            command.Parameters.AddWithValue("@Pasaporte", pasaporte);
            constraint.PasaporteExiste = (int)await command.ExecuteScalarAsync() > 0;

            return constraint;
        }

        public async Task<Usuario?> ObtenerPorCorreoOUsername(string correoOUsername)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT Id, Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, 
                       Telefono, FechaNacimiento, CiudadId, RolID
                FROM Usuario 
                WHERE Correo = @CorreoOUsername OR Username = @CorreoOUsername";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@CorreoOUsername", correoOUsername);

            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new Usuario
                {
                    Id = reader.GetInt32(0),
                    Correo = reader.GetString(1),
                    ContrasenaHash = reader.GetString(2),
                    Pasaporte = reader.IsDBNull(3) ? "" : reader.GetString(3),
                    Username = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    Nombre = reader.GetString(5),
                    Apellido = reader.GetString(6),
                    Telefono = reader.IsDBNull(7) ? "" : reader.GetString(7),
                    FechaNacimiento = reader.IsDBNull(8) ? DateTime.MinValue : reader.GetDateTime(8),
                    CiudadId = reader.IsDBNull(9) ? 0 : reader.GetInt32(9),
                    RolID = reader.GetInt32(10)
                };
            }

            return null;
        }

        public async Task<string?> ObtenerNombreRol(int rolId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT RolNombre FROM Rol WHERE ID = @RolId", connection);
            command.Parameters.AddWithValue("@RolId", rolId);

            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }

        public async Task<bool> ActualizarRol(int usuarioId, int nuevoRolId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = "UPDATE Usuario SET RolID = @NuevoRolId WHERE Id = @UsuarioId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            command.Parameters.AddWithValue("@NuevoRolId", nuevoRolId);

            int filasAfectadas = await command.ExecuteNonQueryAsync();
            return filasAfectadas > 0;
        }

        public async Task<bool> UsuarioExiste(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM Usuario WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);

            int count = (int)await command.ExecuteScalarAsync();
            return count > 0;
        }

        public async Task<bool> RolExiste(int rolId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(*) FROM Rol WHERE Id = @RolId", connection);
            command.Parameters.AddWithValue("@RolId", rolId);

            int count = (int)await command.ExecuteScalarAsync();
            return count > 0;
        }

        public async Task<List<Usuario>> ObtenerTodos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var lista = new List<Usuario>();
            using var command = new SqlCommand(
                "SELECT Id, Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, Telefono, FechaNacimiento, CiudadId, RolID FROM Usuario ORDER BY Id",
                connection);
            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                lista.Add(new Usuario
                {
                    Id = reader.GetInt32(0),
                    Correo = reader.GetString(1),
                    ContrasenaHash = reader.GetString(2),
                    Pasaporte = reader.IsDBNull(3) ? "" : reader.GetString(3),
                    Username = reader.IsDBNull(4) ? "" : reader.GetString(4),
                    Nombre = reader.GetString(5),
                    Apellido = reader.GetString(6),
                    Telefono = reader.IsDBNull(7) ? "" : reader.GetString(7),
                    FechaNacimiento = reader.IsDBNull(8) ? DateTime.MinValue : reader.GetDateTime(8),
                    CiudadId = reader.IsDBNull(9) ? 0 : reader.GetInt32(9),
                    RolID = reader.GetInt32(10)
                });
            }

            return lista;
        }
    }
}