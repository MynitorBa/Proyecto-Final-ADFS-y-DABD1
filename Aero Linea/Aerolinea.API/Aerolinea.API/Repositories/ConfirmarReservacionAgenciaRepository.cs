using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de confirmacion de reservaciones para agencias. Gestiona el proceso
    /// transaccional de confirmar una reservacion pendiente: validaciones, creacion de
    /// factura y actualizacion de estados de boletos y reservacion.
    /// </summary>
    public class ConfirmarReservacionAgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public ConfirmarReservacionAgenciaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Confirma una reservacion pendiente de una agencia. Verifica pertenencia a la
        /// agencia, estado y expiracion de la reservacion, que todos los boletos tengan
        /// pasajero asignado, crea la factura y actualiza estados de boletos y reservacion
        /// dentro de una transaccion atomica.
        /// Retorna el DTO con los datos de la confirmacion realizada.
        /// </summary>
        public async Task<ConfirmacionAgenciaDTO> ConfirmarReservacion(
            int reservacionId,
            int agenciaId,
            ConfirmarReservacionAgenciaDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que la reservación pertenece a la agencia y está pendiente
                string queryVerificar = @"
                    SELECT r.EstadoReservaID, r.FechaExpiracion, r.Total, r.NoReservacion
                    FROM Reservacion r
                    INNER JOIN Agencia ag ON ag.UsuarioWebID = r.UsuarioID
                    WHERE r.ID = @reservacionId AND ag.ID = @agenciaId";

                int estadoReserva = 0;
                DateTime? fechaExp = null;
                decimal total = 0;
                string noReservacion = "";

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("La reservación no existe o no pertenece a esta agencia.");

                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1)) fechaExp = reader.GetDateTime(1);
                    total = reader.GetDecimal(2);
                    noReservacion = reader.GetString(3);
                }

                if (estadoReserva == 2)
                    throw new Exception("Esta reservación ya fue confirmada.");
                if (estadoReserva == 3)
                    throw new Exception("Esta reservación fue cancelada.");
                if (estadoReserva == 4)
                    throw new Exception("Esta reservación ha expirado.");
                if (estadoReserva != 1)
                    throw new Exception("La reservación no está en estado pendiente.");

                if (fechaExp.HasValue && fechaExp.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                // 2. Verificar que todos los boletos tienen pasajero asignado
                string queryBoletos = @"
                    SELECT COUNT(*) FROM Boleto
                    WHERE ReservacionID = @reservacionId
                      AND DatosPasajeroID IS NULL
                      AND EstadoBoletoID = 2";

                using (var cmd = new SqlCommand(queryBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    int sinPasajero = (int)await cmd.ExecuteScalarAsync();
                    if (sinPasajero > 0)
                        throw new Exception("Hay boletos sin pasajeros asignados. Completa todos los datos antes de confirmar.");
                }

                // 3. Crear factura
                DateTime fechaConfirmacion = DateTime.Now;
                int facturaId;

                string insertFactura = @"
                    INSERT INTO Factura (ReservacionID, Fecha, NIT, CodigoPostal, Total)
                    VALUES (@reservacionId, @fecha, @nit, @codigoPostal, @total);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                using (var cmd = new SqlCommand(insertFactura, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@fecha", fechaConfirmacion);
                    cmd.Parameters.AddWithValue("@nit", dto.NIT?.Trim() ?? "CF");
                    cmd.Parameters.AddWithValue("@codigoPostal", dto.CodigoPostal?.Trim() ?? "");
                    cmd.Parameters.AddWithValue("@total", total);
                    facturaId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // 4. Boletos → Vendido (3)
                string updateBoletos = @"
                    UPDATE Boleto SET EstadoBoletoID = 3
                    WHERE ReservacionID = @reservacionId AND EstadoBoletoID = 2";

                using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 5. Reservación → Confirmada (2), limpiar expiración
                string updateReserva = @"
                    UPDATE Reservacion
                    SET EstadoReservaID = 2, FechaExpiracion = NULL
                    WHERE ID = @reservacionId";

                using (var cmd = new SqlCommand(updateReserva, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                transaction.Commit();

                return new ConfirmacionAgenciaDTO
                {
                    FacturaId = facturaId,
                    ReservacionId = reservacionId,
                    NoReservacion = noReservacion,
                    Total = total,
                    NIT = dto.NIT?.Trim() ?? "CF",
                    CodigoPostal = dto.CodigoPostal?.Trim() ?? "",
                    FechaConfirmacion = fechaConfirmacion
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}
