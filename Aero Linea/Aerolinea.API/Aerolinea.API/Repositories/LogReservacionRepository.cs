using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio para el registro de eventos de reservacion en la tabla LogReservacion.
    /// Si el INSERT falla, imprime el error en consola sin interrumpir el flujo principal.
    /// </summary>
    public class LogReservacionRepository
    {
        public const int TipoReservacionExitosa = 1;
        public const int TipoReservacionFallida = 2;
        public const int TipoReservacionErrorInterno = 3;
        public const int TipoAgenciaExitosa = 4;
        public const int TipoAgenciaFallida = 5;
        public const int TipoAgenciaError = 6;
        public const int TipoAgenciaExpirada = 7;
        public const int TipoPasajerosAgregados = 8;
        public const int TipoPasajerosError = 9;
        public const int TipoPasajerosAgenciaAgregados = 10;
        public const int TipoPasajerosAgenciaError = 11;

        public const int TipoPagoExitoso = 12;
        public const int TipoPagoFallido = 13;
        public const int TipoPagoErrorInterno = 14;
        public const int TipoPagoAgenciaExitoso = 15;
        public const int TipoPagoAgenciaFallido = 16;
        public const int TipoPagoAgenciaError = 17;

        public const int TipoCancelacionExitosa = 18;
        public const int TipoCancelacionFallida = 19;
        public const int TipoCancelacionAgenciaExitosa = 20;
        public const int TipoCancelacionAgenciaFallida = 21;

        public const int TipoReservacionExpiradaAuto = 22;

        private readonly DbConnectionFactory _connectionFactory;

        public LogReservacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Registra un evento de reservacion en la tabla LogReservacion.
        /// </summary>
        public async Task Registrar(int tipoEventoId, int? reservacionId, int? usuarioId,
                                    int? agenciaId, decimal? total, bool exitoso,
                                    string? ip, string? userAgent, string? mensaje)
        {
            try
            {
                using var connection = _connectionFactory.CreateConnection();
                await connection.OpenAsync();

                var sql = @"
                    INSERT INTO LogReservacion
                        (TipoEventoID, ReservacionID, UsuarioID, AgenciaID,
                         TotalCalculado, Exitoso, IPOrigen, UserAgent, Mensaje, Fecha)
                    VALUES
                        (@TipoEventoID, @ReservacionID, @UsuarioID, @AgenciaID,
                         @TotalCalculado, @Exitoso, @IPOrigen, @UserAgent, @Mensaje, GETDATE())";

                using var command = new SqlCommand(sql, connection);
                command.Parameters.AddWithValue("@TipoEventoID", tipoEventoId);
                command.Parameters.AddWithValue("@ReservacionID", (object?)reservacionId ?? DBNull.Value);
                command.Parameters.AddWithValue("@UsuarioID", (object?)usuarioId ?? DBNull.Value);
                command.Parameters.AddWithValue("@AgenciaID", (object?)agenciaId ?? DBNull.Value);
                command.Parameters.AddWithValue("@TotalCalculado", (object?)total ?? DBNull.Value);
                command.Parameters.AddWithValue("@Exitoso", exitoso);
                command.Parameters.AddWithValue("@IPOrigen", (object?)ip ?? DBNull.Value);
                command.Parameters.AddWithValue("@UserAgent", (object?)userAgent ?? DBNull.Value);
                command.Parameters.AddWithValue("@Mensaje", (object?)mensaje ?? DBNull.Value);

                await command.ExecuteNonQueryAsync();
            }
            catch (Exception e)
            {
                Console.Error.WriteLine($"[LogReservacionRepository] Error al registrar log (tipo={tipoEventoId}): {e.Message}");
            }
        }
    }
}