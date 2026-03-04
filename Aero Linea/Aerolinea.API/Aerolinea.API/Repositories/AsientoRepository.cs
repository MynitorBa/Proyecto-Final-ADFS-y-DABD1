using Aerolinea.API.Controllers;
using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class AsientoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        // Configuración fija del avión (igual que el frontend)
        private const int ColumnasPorFila = 6;
        private const int FilasEjecutiva = 4;
        private static readonly string[] Columnas = { "A", "B", "C", "D", "E", "F" };

        public AsientoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // GET: mapa completo de asientos de un vuelo 
        public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Capacidad del avión asignado al vuelo
            int capacidad = 0;
            string queryCapacidad = @"
                SELECT a.CapacidadPasajeros
                FROM Vuelo v
                INNER JOIN Avion a ON a.ID = v.AvionID
                WHERE v.ID = @vueloId";

            using (var cmd = new SqlCommand(queryCapacidad, connection))
            {
                cmd.Parameters.AddWithValue("@vueloId", vueloId);
                var result = await cmd.ExecuteScalarAsync();
                if (result == null || result == DBNull.Value)
                    throw new Exception("El vuelo no existe o no tiene avión asignado.");
                capacidad = Convert.ToInt32(result);
            }

            int totalFilas = FilasEjecutiva + (int)Math.Ceiling(
                (capacidad - FilasEjecutiva * ColumnasPorFila) / (double)ColumnasPorFila);

            // 2. Reservación pendiente activa del usuario para este vuelo
            //    (EstadoReservaID = 1 y no expirada)
            int? reservacionActivaId = null;
            string queryReservaActiva = @"
                SELECT TOP 1 r.ID
                FROM Reservacion r
                INNER JOIN Boleto b ON b.ReservacionID = r.ID
                WHERE r.UsuarioID        = @usuarioId
                  AND r.EstadoReservaID  = 1
                  AND (r.FechaExpiracion IS NULL OR r.FechaExpiracion > GETDATE())
                  AND b.VueloID          = @vueloId
                  AND b.EstadoBoletoID   = 2";

            using (var cmd = new SqlCommand(queryReservaActiva, connection))
            {
                cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                cmd.Parameters.AddWithValue("@vueloId", vueloId);
                var result = await cmd.ExecuteScalarAsync();
                if (result != null && result != DBNull.Value)
                    reservacionActivaId = Convert.ToInt32(result);
            }

            // 3. Boletos del usuario en su reservación activa para este vuelo
            var boletosUsuario = new List<BoletoAsientoDTO>();

            if (reservacionActivaId.HasValue)
            {
                string queryBoletosUsuario = @"
                    SELECT b.ID, b.NoBoleto, b.NoAsiento, b.ClaseID,
                           CASE b.ClaseID WHEN 1 THEN 'Turista' ELSE 'Ejecutiva' END AS Clase
                    FROM Boleto b
                    WHERE b.ReservacionID   = @reservacionId
                      AND b.VueloID         = @vueloId
                      AND b.EstadoBoletoID  = 2";

                using var cmd = new SqlCommand(queryBoletosUsuario, connection);
                cmd.Parameters.AddWithValue("@reservacionId", reservacionActivaId.Value);
                cmd.Parameters.AddWithValue("@vueloId", vueloId);

                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    boletosUsuario.Add(new BoletoAsientoDTO
                    {
                        BoletoId = reader.GetInt32(0),
                        NoBoleto = reader.GetString(1),
                        Asiento = reader.GetString(2),
                        ClaseId = reader.GetInt32(3),
                        Clase = reader.GetString(4)
                    });
                }
            }

            // 4. Asientos del usuario para excluirlos de "ocupados"
            var asientosDelUsuario = new HashSet<string>(
                boletosUsuario.Select(b => b.Asiento),
                StringComparer.OrdinalIgnoreCase);

            // 5. Todos los asientos ocupados (estado 2 o 3), excluyendo los propios del usuario
            var asientosOcupados = new List<string>();

            string queryOcupados = @"
                SELECT b.NoAsiento
                FROM Boleto b
                WHERE b.VueloID        = @vueloId
                  AND b.EstadoBoletoID IN (2, 3)";

            using (var cmd = new SqlCommand(queryOcupados, connection))
            {
                cmd.Parameters.AddWithValue("@vueloId", vueloId);
                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    var asiento = reader.GetString(0);
                    if (!asientosDelUsuario.Contains(asiento))
                        asientosOcupados.Add(asiento);
                }
            }

            return new AsientosVueloDTO
            {
                CapacidadPasajeros = capacidad,
                Columnas = Columnas.ToList(),
                FilasEjecutiva = FilasEjecutiva,
                TotalFilas = totalFilas,
                AsientosOcupados = asientosOcupados,
                BoletosUsuario = boletosUsuario
            };
        }

        //  PUT: cambiar asiento de un boleto 
        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Transacción serializable para evitar condiciones de carrera
            using var transaction = connection.BeginTransaction(System.Data.IsolationLevel.Serializable);

            try
            {
                // 1. Verificar que el boleto pertenece al usuario y está en estado pendiente (2)
                string queryVerificar = @"
                    SELECT b.VueloID, b.ClaseID, r.EstadoReservaID, r.FechaExpiracion
                    FROM Boleto b
                    INNER JOIN Reservacion r ON r.ID = b.ReservacionID
                    WHERE b.ID           = @boletoId
                      AND r.UsuarioID    = @usuarioId
                      AND b.EstadoBoletoID = 2";

                int vueloId = 0;
                int claseId = 0;
                int estadoR = 0;
                DateTime? fechaExp = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("El boleto no existe, no te pertenece o no está en estado pendiente.");
                    vueloId = reader.GetInt32(0);
                    claseId = reader.GetInt32(1);
                    estadoR = reader.GetInt32(2);
                    if (!reader.IsDBNull(3)) fechaExp = reader.GetDateTime(3);
                }

                if (estadoR != 1)
                    throw new Exception("La reservación no está en estado pendiente.");
                if (fechaExp.HasValue && fechaExp.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                // 2. Verificar que el nuevo asiento no está ocupado por otro boleto activo (WITH UPDLOCK)
                string queryDisponible = @"
                    SELECT COUNT(1)
                    FROM Boleto WITH (UPDLOCK, ROWLOCK)
                    WHERE VueloID        = @vueloId
                      AND NoAsiento      = @nuevoAsiento
                      AND EstadoBoletoID IN (2, 3)
                      AND ID             <> @boletoId";

                using (var cmd = new SqlCommand(queryDisponible, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@vueloId", vueloId);
                    cmd.Parameters.AddWithValue("@nuevoAsiento", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    int ocupado = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                    if (ocupado > 0)
                        throw new Exception($"El asiento {nuevoAsiento} ya está ocupado. Por favor elige otro.");
                }

                // 3. Verificar que el asiento corresponde a la clase del boleto
                bool esEjecutiva = nuevoAsiento.StartsWith("E-", StringComparison.OrdinalIgnoreCase);
                if (claseId == 2 && !esEjecutiva)
                    throw new Exception("El asiento seleccionado no corresponde a la clase Ejecutiva.");
                if (claseId == 1 && esEjecutiva)
                    throw new Exception("El asiento seleccionado no corresponde a la clase Turista.");

                // 4. Actualizar el asiento
                string queryUpdate = "UPDATE Boleto SET NoAsiento = @nuevoAsiento WHERE ID = @boletoId";
                using (var cmd = new SqlCommand(queryUpdate, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@nuevoAsiento", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    await cmd.ExecuteNonQueryAsync();
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}