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

        /// <summary>
        /// Verifica si ya existe un usuario con el username indicado, excluyendo al usuario actual.
        /// </summary>
        public async Task<bool> ExisteUsername(string username, int exceptoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var command = new SqlCommand(
                "SELECT COUNT(1) FROM Usuario WHERE Username = @Username AND Id <> @ExceptoId", connection);
            command.Parameters.AddWithValue("@Username", username);
            command.Parameters.AddWithValue("@ExceptoId", exceptoId);
            return Convert.ToInt32(await command.ExecuteScalarAsync()) > 0;
        }

        /// <summary>
        /// Verifica si ya existe un usuario con el pasaporte indicado, excluyendo al usuario actual.
        /// </summary>
        public async Task<bool> ExistePasaporte(string pasaporte, int exceptoId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var command = new SqlCommand(
                "SELECT COUNT(1) FROM Usuario WHERE Pasaporte = @Pasaporte AND Id <> @ExceptoId", connection);
            command.Parameters.AddWithValue("@Pasaporte", pasaporte);
            command.Parameters.AddWithValue("@ExceptoId", exceptoId);
            return Convert.ToInt32(await command.ExecuteScalarAsync()) > 0;
        }

        /// <summary>
        /// Actualiza los datos personales del usuario (nombre, apellido, username, pasaporte,
        /// fecha de nacimiento, pais y ciudad). Obtiene o crea el pais y la ciudad si no existen.
        /// Opera dentro de una transaccion para garantizar consistencia.
        /// </summary>
        public async Task<bool> ActualizarDatosPersonales(int usuarioId, ActualizarDatosPersonalesDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();
            try
            {
                // 1. Obtener o crear Pais
                int paisId;
                using (var cmd = new SqlCommand(
                    "SELECT Id FROM Pais WHERE Nombre = @Nombre", connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@Nombre", dto.Pais);
                    var result = await cmd.ExecuteScalarAsync();
                    if (result != null)
                    {
                        paisId = (int)result;
                    }
                    else
                    {
                        using var ins = new SqlCommand(
                            "INSERT INTO Pais (Nombre) OUTPUT INSERTED.ID VALUES (@Nombre)",
                            connection, transaction);
                        ins.Parameters.AddWithValue("@Nombre", dto.Pais);
                        paisId = (int)(await ins.ExecuteScalarAsync())!;
                    }
                }

                // 2. Obtener o crear Ciudad
                int ciudadId;
                using (var cmd = new SqlCommand(
                    "SELECT Id FROM Ciudad WHERE Nombre = @Nombre AND PaisID = @PaisId",
                    connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@Nombre", dto.Ciudad);
                    cmd.Parameters.AddWithValue("@PaisId", paisId);
                    var result = await cmd.ExecuteScalarAsync();
                    if (result != null)
                    {
                        ciudadId = (int)result;
                    }
                    else
                    {
                        using var ins = new SqlCommand(
                            "INSERT INTO Ciudad (Nombre, PaisID) OUTPUT INSERTED.ID VALUES (@Nombre, @PaisId)",
                            connection, transaction);
                        ins.Parameters.AddWithValue("@Nombre", dto.Ciudad);
                        ins.Parameters.AddWithValue("@PaisId", paisId);
                        ciudadId = (int)(await ins.ExecuteScalarAsync())!;
                    }
                }

                // 3. Actualizar Usuario
                using var update = new SqlCommand(@"
                    UPDATE Usuario SET
                        Nombre          = @Nombre,
                        Apellido        = @Apellido,
                        Username        = @Username,
                        Pasaporte       = @Pasaporte,
                        FechaNacimiento = @FechaNacimiento,
                        CiudadId        = @CiudadId
                    WHERE Id = @UsuarioId", connection, transaction);

                update.Parameters.AddWithValue("@Nombre",    dto.Nombre);
                update.Parameters.AddWithValue("@Apellido",  dto.Apellido);
                update.Parameters.AddWithValue("@Username",  dto.Username);
                update.Parameters.AddWithValue("@Pasaporte", dto.Pasaporte);
                update.Parameters.AddWithValue("@FechaNacimiento",
                    dto.FechaNacimiento.HasValue ? dto.FechaNacimiento.Value : DBNull.Value);
                update.Parameters.AddWithValue("@CiudadId",   ciudadId);
                update.Parameters.AddWithValue("@UsuarioId",  usuarioId);

                var rows = await update.ExecuteNonQueryAsync();
                transaction.Commit();
                return rows > 0;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}
