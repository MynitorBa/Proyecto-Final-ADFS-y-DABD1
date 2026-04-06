using Aerolinea.API.Controllers;
using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de asientos para usuarios. Gestiona la consulta del mapa de asientos
    /// de un vuelo y el cambio de asiento de un boleto pendiente, verificando propiedad,
    /// disponibilidad con bloqueo pesimista y reglas de clase.
    /// </summary>
    public class AsientoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        private const int ColumnasPorFila = 6;
        private static readonly string[] Columnas = { "A", "B", "C", "D", "E", "F" };

        public AsientoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        // GET: mapa completo de asientos de un vuelo
        /// <summary>
        /// Retorna el mapa completo de asientos de un vuelo para un usuario especifico.
        /// Calcula el layout dinamico por clase, identifica los asientos ocupados por otros
        /// pasajeros y lista los boletos del usuario en su reservacion activa.
        /// </summary>
        public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Capacidad del avión + boletos por clase del vuelo
            int capacidad = 0;
            int boletosEjecutivo = 0;
            int boletosTurista = 0;

            string queryCapacidad = @"
                SELECT a.CapacidadPasajeros, v.BoletosEjecutivo, v.BoletosTurista
                FROM Vuelo v
                INNER JOIN Avion a ON a.ID = v.AvionID
                WHERE v.ID = @vueloId";

            using (var cmd = new SqlCommand(queryCapacidad, connection))
            {
                cmd.Parameters.AddWithValue("@vueloId", vueloId);
                using var reader = await cmd.ExecuteReaderAsync();
                if (!await reader.ReadAsync())
                    throw new Exception("El vuelo no existe o no tiene avión asignado.");
                capacidad = reader.GetInt32(0);
                boletosEjecutivo = reader.IsDBNull(1) ? 0 : reader.GetInt32(1);
                boletosTurista = reader.IsDBNull(2) ? 0 : reader.GetInt32(2);
            }

            // 2. Calcular filas dinámicamente según cada clase
            int filasEjecutiva = (int)Math.Ceiling(boletosEjecutivo / (double)ColumnasPorFila);
            int filasTurista = (int)Math.Ceiling(boletosTurista / (double)ColumnasPorFila);
            int totalFilas = filasEjecutiva + filasTurista;

            // 3. Reservación pendiente activa del usuario para este vuelo
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

            // 4. Boletos del usuario en su reservación activa para este vuelo
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

            // 5. Asientos del usuario para excluirlos de "ocupados"
            var asientosDelUsuario = new HashSet<string>(
                boletosUsuario.Select(b => b.Asiento),
                StringComparer.OrdinalIgnoreCase);

            // 6. Todos los asientos ocupados (estado 2 o 3), excluyendo los propios del usuario
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
                FilasEjecutiva = filasEjecutiva,
                TotalFilas = totalFilas,
                AsientosOcupados = asientosOcupados,
                BoletosUsuario = boletosUsuario
            };
        }

        // PUT: cambiar asiento de un boleto
        /// <summary>
        /// Cambia el asiento de un boleto del usuario autenticado. Verifica que el boleto
        /// pertenezca al usuario, que la reservacion este pendiente y no expirada,
        /// que el asiento sea valido en el layout del vuelo, que este disponible con
        /// bloqueo pesimista y que corresponda a la clase del boleto.
        /// </summary>
        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction(System.Data.IsolationLevel.Serializable);

            try
            {
                // 1. Verificar propiedad, estado de reserva y obtener datos del vuelo
                string queryVerificar = @"
                    SELECT b.VueloID, b.ClaseID, r.EstadoReservaID, r.FechaExpiracion,
                           v.BoletosEjecutivo, v.BoletosTurista
                    FROM Boleto b
                    INNER JOIN Reservacion r ON r.ID = b.ReservacionID
                    INNER JOIN Vuelo v ON v.ID = b.VueloID
                    WHERE b.ID = @boletoId AND r.UsuarioID = @usuarioId AND b.EstadoBoletoID = 2";

                int vueloId, claseId, estadoR, boletosEjecutivo, boletosTurista;
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
                    boletosEjecutivo = reader.IsDBNull(4) ? 0 : reader.GetInt32(4);
                    boletosTurista = reader.IsDBNull(5) ? 0 : reader.GetInt32(5);
                }

                if (estadoR != 1)
                    throw new Exception("La reservación no está en estado pendiente.");
                if (fechaExp.HasValue && fechaExp.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                // 2. Validar que el asiento exista dentro del layout dinámico del vuelo
                ValidarLimitesAsiento(nuevoAsiento, boletosEjecutivo, boletosTurista);

                // 3. Verificar disponibilidad (UPDLOCK)
                string queryDisponible = @"
                    SELECT COUNT(1) FROM Boleto WITH (UPDLOCK, ROWLOCK)
                    WHERE VueloID = @vueloId AND NoAsiento = @nuevoAsiento
                      AND EstadoBoletoID IN (2, 3) AND ID <> @boletoId";

                using (var cmd = new SqlCommand(queryDisponible, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@vueloId", vueloId);
                    cmd.Parameters.AddWithValue("@nuevoAsiento", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    if (Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0)
                        throw new Exception($"El asiento {nuevoAsiento} ya está ocupado.");
                }

                // 4. Validar regla de clase
                bool esEjecutiva = nuevoAsiento.StartsWith("E-", StringComparison.OrdinalIgnoreCase);
                if (claseId == 2 && !esEjecutiva) throw new Exception("Asiento inválido para clase Ejecutiva.");
                if (claseId == 1 && esEjecutiva) throw new Exception("Asiento inválido para clase Turista.");

                // 5. Actualizar el asiento
                string queryUpdate = "UPDATE Boleto SET NoAsiento = @nuevoAsiento WHERE ID = @boletoId";
                using (var cmd = new SqlCommand(queryUpdate, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@nuevoAsiento", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    await cmd.ExecuteNonQueryAsync();
                }

                transaction.Commit();
            }
            catch { transaction.Rollback(); throw; }
        }

        // Valida que el asiento exista en el layout real del vuelo (dinámico)
        private void ValidarLimitesAsiento(string asiento, int boletosEjecutivo, int boletosTurista)
        {
            bool esEjecutiva = asiento.StartsWith("E-", StringComparison.OrdinalIgnoreCase);

            // Quitar prefijo E- para parsear fila y columna
            string limpio = asiento.ToUpper().Replace("E-", "");
            string strLetra = new string(limpio.Where(char.IsLetter).ToArray());
            string strNumero = new string(limpio.Where(char.IsDigit).ToArray());

            if (string.IsNullOrEmpty(strLetra) || string.IsNullOrEmpty(strNumero))
                throw new Exception($"El formato del asiento '{asiento}' es inválido.");

            if (!Columnas.Contains(strLetra))
                throw new Exception($"La columna '{strLetra}' no existe en este avión.");

            int numFila = int.Parse(strNumero);

            if (esEjecutiva)
            {
                int filasEjecutiva = (int)Math.Ceiling(boletosEjecutivo / (double)ColumnasPorFila);
                if (numFila < 1 || numFila > filasEjecutiva)
                    throw new Exception(
                        $"La fila ejecutiva {numFila} no existe en este vuelo (máximo: {filasEjecutiva}).");
            }
            else
            {
                int filasTurista = (int)Math.Ceiling(boletosTurista / (double)ColumnasPorFila);
                if (numFila < 1 || numFila > filasTurista)
                    throw new Exception(
                        $"La fila turista {numFila} no existe en este vuelo (máximo: {filasTurista}).");
            }
        }
    }
}
