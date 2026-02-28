using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class PerfilRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public PerfilRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<PerfilDTO?> ObtenerPerfil(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT 
                    u.Id, u.Nombre, u.Apellido, u.Correo, u.Username,
                    u.Telefono, u.Pasaporte, u.FechaNacimiento,
                    p.Nombre AS Pais,
                    c.Nombre AS Ciudad
                FROM Usuario u
                LEFT JOIN Ciudad c ON u.CiudadId = c.Id
                LEFT JOIN Pais p   ON c.PaisID   = p.Id
                WHERE u.Id = @UsuarioId";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);

            using var reader = await command.ExecuteReaderAsync();
            if (!await reader.ReadAsync())
                return null;

            return new PerfilDTO
            {
                Id = reader.GetInt32(0),
                Nombre = reader.GetString(1),
                Apellido = reader.GetString(2),
                Correo = reader.GetString(3),
                Username = reader.GetString(4),
                Telefono = reader.IsDBNull(5) ? "" : reader.GetString(5),
                Pasaporte = reader.IsDBNull(6) ? "" : reader.GetString(6),
                FechaNacimiento = reader.IsDBNull(7) ? DateTime.MinValue : reader.GetDateTime(7),
                Pais = reader.IsDBNull(8) ? "" : reader.GetString(8),
                Ciudad = reader.IsDBNull(9) ? "" : reader.GetString(9)
            };
        }

        public async Task<bool> ActualizarTelefono(int usuarioId, string telefono)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Usuario SET Telefono = @Telefono WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@Telefono", telefono);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            return await command.ExecuteNonQueryAsync() > 0;
        }

        public async Task<string?> ObtenerHashContrasena(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT ContrasenaHash FROM Usuario WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }

        public async Task<bool> ActualizarContrasena(int usuarioId, string nuevoHash)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Usuario SET ContrasenaHash = @Hash WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@Hash", nuevoHash);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            return await command.ExecuteNonQueryAsync() > 0;
        }
    }
}