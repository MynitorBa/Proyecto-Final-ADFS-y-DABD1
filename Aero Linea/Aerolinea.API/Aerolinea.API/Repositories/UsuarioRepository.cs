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
        private readonly PaisRepository _paisRepository;
        private readonly CiudadRepository _ciudadRepository;

        public UsuarioRepository(
            DbConnectionFactory connectionFactory,
            NacionalidadRepository nacionalidadRepository,
            PaisRepository paisRepository,
            CiudadRepository ciudadRepository)
        {
            _connectionFactory = connectionFactory;
            _nacionalidadRepository = nacionalidadRepository;
            _paisRepository = paisRepository;
            _ciudadRepository = ciudadRepository;
        }

        public async Task<int> CrearUsuario(Usuario usuario)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                INSERT INTO Usuario (Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, 
                                    Telefono, FechaNacimiento, PaisId, CiudadId, RolID)
                OUTPUT INSERTED.Id
                VALUES (@Correo, @ContrasenaHash, @Pasaporte, @Username, @Nombre, @Apellido, 
                        @Telefono, @FechaNacimiento, @PaisId, @CiudadId, @RolID)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Correo", usuario.Correo);
            command.Parameters.AddWithValue("@ContrasenaHash", usuario.ContrasenaHash);
            command.Parameters.AddWithValue("@Pasaporte", usuario.Pasaporte);
            command.Parameters.AddWithValue("@Username", usuario.Username);
            command.Parameters.AddWithValue("@Nombre", usuario.Nombre);
            command.Parameters.AddWithValue("@Apellido", usuario.Apellido);
            command.Parameters.AddWithValue("@Telefono", usuario.Telefono);
            command.Parameters.AddWithValue("@FechaNacimiento", usuario.FechaNacimiento);
            command.Parameters.AddWithValue("@PaisId", usuario.PaisId);
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
                // Obtener o crear el Id de la nacionalidad
                int nacionalidadId = await _nacionalidadRepository.ObtenerOCrearId(nacionalidad, connection);

                // Insertar en UsuarioNacionalidad
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

        public async Task<Usuario> ObtenerPorCorreoOUsername(string correoOUsername)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT Id, Correo, ContrasenaHash, Pasaporte, Username, Nombre, Apellido, 
                       Telefono, FechaNacimiento, PaisId, CiudadId, RolID
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
                    Pasaporte = reader.GetString(3),
                    Username = reader.GetString(4),
                    Nombre = reader.GetString(5),
                    Apellido = reader.GetString(6),
                    Telefono = reader.GetString(7),
                    FechaNacimiento = reader.GetDateTime(8),
                    PaisId = reader.GetInt32(9),
                    CiudadId = reader.GetInt32(10),
                    RolID = reader.GetInt32(11)
                };
            }

            return null;
        }
    }
}