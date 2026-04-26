using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio para el registro de eventos de sesion en la tabla LogSesion.
    /// Centraliza todos los INSERT de logs del sistema. Si el INSERT falla,
    /// imprime el error en consola pero no lanza excepcion para no interrumpir
    /// el flujo principal.
    /// </summary>
    public class LogRepository
    {
        public const int TipoLoginExitoso = 1;
        public const int TipoLoginFallido = 2;
        public const int TipoLoginErrorInterno = 3;
        public const int TipoLogoutExitoso = 4;
        public const int TipoLogoutErrorInterno = 5;

        public const int TipoRegistroExitoso = 6;
        public const int TipoRegistroFallido = 7;
        public const int TipoRegistroErrorInterno = 8;

        private readonly DbConnectionFactory _connectionFactory;

        public LogRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Registra un evento de sesion en la tabla LogSesion.
        /// </summary>
        /// <param name="tipoEventoId">ID del tipo de evento (usar constantes Tipo* de esta clase).</param>
        /// <param name="usuarioId">ID del usuario autenticado, null si el login fallo.</param>
        /// <param name="loginIntentado">Username o correo intentado, puede ser null.</param>
        /// <param name="exitoso">true si el evento fue exitoso.</param>
        /// <param name="ipOrigen">IP del cliente, puede ser null.</param>
        /// <param name="userAgent">User-Agent del cliente, puede ser null.</param>
        /// <param name="mensaje">Mensaje adicional descriptivo, puede ser null.</param>
        public async Task Registrar(int tipoEventoId, int? usuarioId, string? loginIntentado,
                                    bool exitoso, string? ipOrigen, string? userAgent, string? mensaje)
        {
            try
            {
                using var connection = _connectionFactory.CreateConnection();
                await connection.OpenAsync();

                var sql = @"
                    INSERT INTO LogSesion
                        (TipoEventoID, UsuarioID, LoginIntentado, Exitoso, IPOrigen, UserAgent, Mensaje, Fecha)
                    VALUES
                        (@TipoEventoID, @UsuarioID, @LoginIntentado, @Exitoso, @IPOrigen, @UserAgent, @Mensaje, GETDATE())";

                using var command = new SqlCommand(sql, connection);
                command.Parameters.AddWithValue("@TipoEventoID", tipoEventoId);
                command.Parameters.AddWithValue("@UsuarioID", (object?)usuarioId ?? DBNull.Value);
                command.Parameters.AddWithValue("@LoginIntentado", (object?)loginIntentado ?? DBNull.Value);
                command.Parameters.AddWithValue("@Exitoso", exitoso);
                command.Parameters.AddWithValue("@IPOrigen", (object?)ipOrigen ?? DBNull.Value);
                command.Parameters.AddWithValue("@UserAgent", (object?)userAgent ?? DBNull.Value);
                command.Parameters.AddWithValue("@Mensaje", (object?)mensaje ?? DBNull.Value);

                await command.ExecuteNonQueryAsync();
            }
            catch (Exception e)
            {
                Console.Error.WriteLine($"[LogRepository] Error al registrar log (tipo={tipoEventoId}): {e.Message}");
            }
        }
    }
}