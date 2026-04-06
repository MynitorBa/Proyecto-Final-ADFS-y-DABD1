using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de facturacion. Gestiona el proceso transaccional de compra
    /// de una reservacion por parte de un usuario: validaciones, creacion de factura
    /// y actualizacion de estados de boletos y reservacion.
    /// </summary>
    public class FacturaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public FacturaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Procesa el pago de una reservacion pendiente del usuario autenticado.
        /// Verifica propiedad, estado y expiracion de la reservacion, que todos
        /// los boletos tengan pasajero asignado, crea la factura y actualiza
        /// estados de boletos y reservacion dentro de una transaccion atomica.
        /// Retorna el DTO con los datos de la compra realizada.
        /// </summary>
        public async Task<CompraRealizadaDTO> ComprarReservacion(
            int reservacionId,
            int usuarioId,
            ComprarReservacionDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // ── 1. Verificar que la reservación existe, pertenece al usuario
                //       y está en estado Pendiente (1) y no expirada ──────────────
                string queryReserva = @"
                    SELECT r.EstadoReservaID, r.FechaExpiracion, r.Total, r.NoReservacion,
                           r.UsuarioID
                    FROM Reservacion r
                    WHERE r.ID = @reservacionId";

                int estadoReserva = 0;
                DateTime? fechaExp = null;
                decimal total = 0;
                string noReservacion = "";
                int? propietario = null;

                using (var cmd = new SqlCommand(queryReserva, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();

                    if (!await reader.ReadAsync())
                        throw new Exception("La reservación no existe.");

                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1)) fechaExp = reader.GetDateTime(1);
                    total = reader.GetDecimal(2);
                    noReservacion = reader.GetString(3);
                    if (!reader.IsDBNull(4)) propietario = reader.GetInt32(4);
                }

                // Validar propietario
                if (propietario == null || propietario.Value != usuarioId)
                    throw new Exception("No tienes permiso para pagar esta reservación.");

                // Validar estado
                if (estadoReserva == 2)
                    throw new Exception("Esta reservación ya fue confirmada anteriormente.");
                if (estadoReserva == 3)
                    throw new Exception("Esta reservación fue cancelada.");
                if (estadoReserva == 4)
                    throw new Exception("Esta reservación ha expirado.");
                if (estadoReserva != 1)
                    throw new Exception("La reservación no está en estado pendiente.");

                // Validar expiración
                if (fechaExp.HasValue && fechaExp.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado. Por favor crea una nueva.");

                // ── 2. Verificar que todos los boletos tienen pasajero asignado ──
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
                        throw new Exception(
                            "Hay boletos sin pasajeros asignados. Completa todos los datos antes de pagar.");
                }

                // ── 3. Crear la factura ────────────────────────────────────────────
                DateTime fechaFactura = DateTime.Now;
                int facturaId;

                string insertFactura = @"
                    INSERT INTO Factura (ReservacionID, Fecha, NIT, CodigoPostal, Total)
                    VALUES (@reservacionId, @fecha, @nit, @codigoPostal, @total);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                using (var cmd = new SqlCommand(insertFactura, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@fecha", fechaFactura);
                    cmd.Parameters.AddWithValue("@nit", dto.NIT?.Trim() ?? "CF");
                    cmd.Parameters.AddWithValue("@codigoPostal", dto.CodigoPostal?.Trim() ?? "");
                    cmd.Parameters.AddWithValue("@total", total);
                    facturaId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // ── 4. Boletos Vendido (3) ──────────────────────────────────────
                string updateBoletos = @"
                    UPDATE Boleto
                    SET EstadoBoletoID = 3
                    WHERE ReservacionID = @reservacionId
                      AND EstadoBoletoID = 2";

                using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // ── 5. Reservación Confirmada (2), limpiar expiración ───────────
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

                return new CompraRealizadaDTO
                {
                    FacturaId = facturaId,
                    ReservacionId = reservacionId,
                    NoReservacion = noReservacion,
                    Fecha = fechaFactura,
                    NIT = dto.NIT?.Trim() ?? "CF",
                    CodigoPostal = dto.CodigoPostal?.Trim() ?? "",
                    Total = total
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
