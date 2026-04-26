using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de perfil de usuario. Permite consultar y actualizar datos personales
    /// del usuario autenticado, incluyendo telefono y contrasena.
    /// </summary>
    public class PerfilRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public PerfilRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna el perfil completo del usuario incluyendo nombre, apellido, correo,
        /// telefono, pasaporte, fecha de nacimiento, ciudad y pais.
        /// Retorna null si el usuario no existe.
        /// </summary>
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

        /// <summary>
        /// Actualiza el numero de telefono del usuario indicado.
        /// Retorna true si la actualizacion afecto al menos una fila.
        /// </summary>
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

        /// <summary>
        /// Retorna el correo electronico del usuario indicado.
        /// Retorna null si el usuario no existe.
        /// </summary>
        public async Task<string?> ObtenerCorreo(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT Correo FROM Usuario WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            var result = await command.ExecuteScalarAsync();
            return result?.ToString();
        }

        /// <summary>
        /// Retorna el hash de la contrasena almacenada para el usuario indicado.
        /// Retorna null si el usuario no existe.
        /// </summary>
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

        /// <summary>
        /// Actualiza el hash de contrasena del usuario con el nuevo valor proporcionado.
        /// Retorna true si la actualizacion afecto al menos una fila.
        /// </summary>
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

        /// <summary>
        /// Verifica si ya existe un usuario con el correo indicado, excluyendo al usuario actual.
        /// Retorna true si el correo ya esta en uso por otro usuario.
        /// </summary>
        public async Task<bool> ExisteCorreo(string correo, int exceptoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "SELECT COUNT(1) FROM Usuario WHERE Correo = @Correo AND Id <> @ExceptoId", connection);
            command.Parameters.AddWithValue("@Correo", correo);
            command.Parameters.AddWithValue("@ExceptoId", exceptoId);
            var result = await command.ExecuteScalarAsync();
            return Convert.ToInt32(result) > 0;
        }

        /// <summary>
        /// Actualiza el correo electronico del usuario indicado.
        /// Retorna true si la actualizacion afecto al menos una fila.
        /// </summary>
        public async Task<bool> ActualizarCorreo(int usuarioId, string nuevoCorreo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var command = new SqlCommand(
                "UPDATE Usuario SET Correo = @Correo WHERE Id = @UsuarioId", connection);
            command.Parameters.AddWithValue("@Correo", nuevoCorreo);
            command.Parameters.AddWithValue("@UsuarioId", usuarioId);
            return await command.ExecuteNonQueryAsync() > 0;
        }
    }
}
