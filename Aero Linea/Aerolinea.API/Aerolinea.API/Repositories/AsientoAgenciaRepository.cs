using Aerolinea.API.Data;
using Aerolinea.API.DTOs.Agencia;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de asientos para agencias de viaje. Permite consultar el mapa
    /// de asientos de una reservacion y cambiar asientos de boletos dentro
    /// del contexto de una agencia autenticada.
    /// </summary>
    public class AsientoAgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private const int ColumnasPorFila = 6;
        private static readonly string[] Columnas = { "A", "B", "C", "D", "E", "F" };

        public AsientoAgenciaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna el mapa de asientos de todos los vuelos de una reservacion de agencia.
        /// Incluye asientos ocupados, boletos propios de la agencia y el layout dinamico
        /// calculado segun la cantidad de boletos por clase en cada vuelo.
        /// Lanza una excepcion si la reservacion no pertenece a la agencia.
        /// </summary>
        public async Task<List<AsientosVueloAgenciaDTO>> ObtenerAsientosPorReservacion(int reservacionId, int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Verificar pertenencia de reservación a la agencia
            string queryVerificar = @"
                SELECT ag.UsuarioWebID
                FROM Reservacion r
                INNER JOIN Agencia ag ON ag.UsuarioWebID = r.UsuarioID
                WHERE r.ID = @reservacionId AND ag.ID = @agenciaId AND r.EstadoReservaID = 1";

            using (var cmd = new SqlCommand(queryVerificar, connection))
            {
                cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
                var result = await cmd.ExecuteScalarAsync();
                if (result == null || result == DBNull.Value)
                    throw new Exception("La reservación no existe o no pertenece a esta agencia.");
            }

            // 2. Obtener vuelos
            var vuelos = new List<(int VueloId, string NumeroVuelo, int Capacidad, int BoletosEjecutivo, int BoletosTurista)>();
            string queryVuelos = @"
                SELECT DISTINCT v.ID, v.NumeroVuelo, a.CapacidadPasajeros, v.BoletosEjecutivo, v.BoletosTurista
                FROM Boleto b
                INNER JOIN Vuelo v ON v.ID = b.VueloID
                INNER JOIN Avion a ON a.ID = v.AvionID
                WHERE b.ReservacionID = @reservacionId AND b.EstadoBoletoID = 2";

            using (var cmd = new SqlCommand(queryVuelos, connection))
            {
                cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                using var reader = await cmd.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    vuelos.Add((reader.GetInt32(0), reader.GetString(1), reader.GetInt32(2),
                               reader.IsDBNull(3) ? 0 : reader.GetInt32(3),
                               reader.IsDBNull(4) ? 0 : reader.GetInt32(4)));
                }
            }

            var resultado = new List<AsientosVueloAgenciaDTO>();

            foreach (var v in vuelos)
            {
                int filasEjecutiva = (int)Math.Ceiling(v.BoletosEjecutivo / (double)ColumnasPorFila);
                int totalFilas = filasEjecutiva + (int)Math.Ceiling(v.BoletosTurista / (double)ColumnasPorFila);

                var boletosAgencia = new List<BoletoAsientoAgenciaDTO>();
                string queryBoletos = @"
                    SELECT b.ID, b.NoBoleto, b.NoAsiento, b.ClaseID,
                           CASE b.ClaseID WHEN 1 THEN 'Turista' ELSE 'Ejecutiva' END
                    FROM Boleto b WHERE b.ReservacionID = @rId AND b.VueloID = @vId AND b.EstadoBoletoID = 2";

                using (var cmd = new SqlCommand(queryBoletos, connection))
                {
                    cmd.Parameters.AddWithValue("@rId", reservacionId);
                    cmd.Parameters.AddWithValue("@vId", v.VueloId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                    {
                        boletosAgencia.Add(new BoletoAsientoAgenciaDTO
                        {
                            BoletoId = reader.GetInt32(0),
                            NoBoleto = reader.GetString(1),
                            Asiento = reader.GetString(2),
                            ClaseId = reader.GetInt32(3),
                            Clase = reader.GetString(4)
                        });
                    }
                }

                var asientosOcupados = new List<string>();
                string queryOcupados = "SELECT NoAsiento FROM Boleto WHERE VueloID = @vId AND EstadoBoletoID IN (2, 3)";
                using (var cmd = new SqlCommand(queryOcupados, connection))
                {
                    cmd.Parameters.AddWithValue("@vId", v.VueloId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    var misAsientos = new HashSet<string>(boletosAgencia.Select(b => b.Asiento));
                    while (await reader.ReadAsync())
                    {
                        var asien = reader.GetString(0);
                        if (!misAsientos.Contains(asien)) asientosOcupados.Add(asien);
                    }
                }

                resultado.Add(new AsientosVueloAgenciaDTO
                {
                    VueloId = v.VueloId,
                    NumeroVuelo = v.NumeroVuelo,
                    CapacidadPasajeros = v.Capacidad,
                    Columnas = Columnas.ToList(),
                    FilasEjecutiva = filasEjecutiva,
                    TotalFilas = totalFilas,
                    AsientosOcupados = asientosOcupados,
                    BoletosAgencia = boletosAgencia
                });
            }
            return resultado;
        }

        // PUT: cambiar asiento de un boleto (vista agencia)
        /// <summary>
        /// Cambia el asiento de un boleto perteneciente a una reservacion de agencia.
        /// Verifica propiedad, validez del asiento en el layout del vuelo, disponibilidad
        /// con bloqueo pesimista y regla de clase antes de aplicar el cambio.
        /// </summary>
        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction(System.Data.IsolationLevel.Serializable);

            try
            {
                // 1. Verificar propiedad y obtener datos del vuelo
                string queryVerificar = @"
                    SELECT b.VueloID, b.ClaseID, r.EstadoReservaID,
                           v.BoletosEjecutivo, v.BoletosTurista
                    FROM Boleto b
                    INNER JOIN Reservacion r ON r.ID = b.ReservacionID
                    INNER JOIN Agencia ag    ON ag.UsuarioWebID = r.UsuarioID
                    INNER JOIN Vuelo v       ON v.ID = b.VueloID
                    WHERE b.ID = @boletoId AND ag.ID = @agenciaId AND b.EstadoBoletoID = 2";

                int vueloId, claseId, estadoR, boletosEjecutivo, boletosTurista;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@boletoId", boletoId);
                    cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("Acceso denegado al boleto o no está en estado pendiente.");

                    vueloId = reader.GetInt32(0);
                    claseId = reader.GetInt32(1);
                    estadoR = reader.GetInt32(2);
                    boletosEjecutivo = reader.IsDBNull(3) ? 0 : reader.GetInt32(3);
                    boletosTurista = reader.IsDBNull(4) ? 0 : reader.GetInt32(4);
                }

                if (estadoR != 1)
                    throw new Exception("La reservación no está en estado pendiente.");

                // 2. Validar que el asiento exista en el layout dinámico del vuelo
                ValidarLimitesAsiento(nuevoAsiento, boletosEjecutivo, boletosTurista);

                // 3. Verificar disponibilidad (UPDLOCK)
                string queryDisp = @"
                    SELECT COUNT(1) FROM Boleto WITH (UPDLOCK, ROWLOCK)
                    WHERE VueloID = @vId AND NoAsiento = @nas
                      AND EstadoBoletoID IN (2, 3) AND ID <> @bId";

                using (var cmd = new SqlCommand(queryDisp, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@vId", vueloId);
                    cmd.Parameters.AddWithValue("@nas", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@bId", boletoId);
                    if (Convert.ToInt32(await cmd.ExecuteScalarAsync()) > 0)
                        throw new Exception($"El asiento {nuevoAsiento} ya está ocupado.");
                }

                // 4. Validar regla de clase
                bool esEjecutiva = nuevoAsiento.StartsWith("E-", StringComparison.OrdinalIgnoreCase);
                if (claseId == 2 && !esEjecutiva) throw new Exception("Asiento inválido para clase Ejecutiva.");
                if (claseId == 1 && esEjecutiva) throw new Exception("Asiento inválido para clase Turista.");

                // 5. Actualizar asiento
                using (var cmd = new SqlCommand("UPDATE Boleto SET NoAsiento = @nas WHERE ID = @bId", connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@nas", nuevoAsiento);
                    cmd.Parameters.AddWithValue("@bId", boletoId);
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
