using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Models;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class ReservacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;
        private readonly PaisRepository _paisRepository;
        private readonly CiudadRepository _ciudadRepository;

        public ReservacionRepository(
            DbConnectionFactory connectionFactory,
            PaisRepository paisRepository,
            CiudadRepository ciudadRepository)
        {
            _connectionFactory = connectionFactory;
            _paisRepository = paisRepository;
            _ciudadRepository = ciudadRepository;
        }

        // ─────────────────────────────────────────────────────────────────
        //  CREAR RESERVACIÓN
        // ─────────────────────────────────────────────────────────────────
        public async Task<ReservacionCreadaDTO> CrearReservacion(int? usuarioId, List<SeleccionVueloDTO> vuelos)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction(System.Data.IsolationLevel.Serializable);

            try
            {
                decimal total = 0;
                var boletosReservados = new List<BoletoReservadoDTO>();

                // ── Expirar reservas pendientes anteriores del mismo usuario ──────
                if (usuarioId.HasValue)
                {
                    string queryPendientes = @"
                        SELECT ID FROM Reservacion
                        WHERE UsuarioID = @usuarioId AND EstadoReservaID = 1";

                    var pendientesAnteriores = new List<int>();
                    using (var cmd = new SqlCommand(queryPendientes, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@usuarioId", usuarioId.Value);
                        using var reader = await cmd.ExecuteReaderAsync();
                        while (await reader.ReadAsync())
                            pendientesAnteriores.Add(reader.GetInt32(0));
                    }

                    foreach (var pendienteId in pendientesAnteriores)
                    {
                        string queryGrupos = @"
                            SELECT VueloID, ClaseID, COUNT(*) AS Cantidad
                            FROM Boleto
                            WHERE ReservacionID = @pendienteId AND EstadoBoletoID = 2
                            GROUP BY VueloID, ClaseID";

                        var grupos = new List<(int VueloId, int ClaseId, int Cantidad)>();
                        using (var cmd = new SqlCommand(queryGrupos, connection, transaction))
                        {
                            cmd.Parameters.AddWithValue("@pendienteId", pendienteId);
                            using var reader = await cmd.ExecuteReaderAsync();
                            while (await reader.ReadAsync())
                                grupos.Add((reader.GetInt32(0), reader.GetInt32(1), reader.GetInt32(2)));
                        }

                        string cancelarBoletos = @"
                            UPDATE Boleto SET EstadoBoletoID = 4
                            WHERE ReservacionID = @pendienteId AND EstadoBoletoID = 2";
                        using (var cmd = new SqlCommand(cancelarBoletos, connection, transaction))
                        {
                            cmd.Parameters.AddWithValue("@pendienteId", pendienteId);
                            await cmd.ExecuteNonQueryAsync();
                        }

                        foreach (var (vueloId, claseId, cantidad) in grupos)
                        {
                            string campo = claseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                            string devolver = $@"
                                UPDATE Vuelo SET {campo} = {campo} + @cantidad WHERE ID = @vueloId";
                            using var cmd = new SqlCommand(devolver, connection, transaction);
                            cmd.Parameters.AddWithValue("@cantidad", cantidad);
                            cmd.Parameters.AddWithValue("@vueloId", vueloId);
                            await cmd.ExecuteNonQueryAsync();
                        }

                        string expirar = @"
                            UPDATE Reservacion SET EstadoReservaID = 4 WHERE ID = @pendienteId";
                        using (var cmd = new SqlCommand(expirar, connection, transaction))
                        {
                            cmd.Parameters.AddWithValue("@pendienteId", pendienteId);
                            await cmd.ExecuteNonQueryAsync();
                        }
                    }
                }

                // ── PASO 1: Verificar disponibilidad de TODOS los vuelos ──────────
                foreach (var vuelo in vuelos)
                {
                    string campoDisponible = vuelo.ClaseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string campoPrecio = vuelo.ClaseId == 1 ? "PrecioTurista" : "PrecioEjecutivo";

                    string queryVerificar = $@"
                        SELECT {campoDisponible}, {campoPrecio}
                        FROM Vuelo WITH (UPDLOCK, ROWLOCK)
                        WHERE ID = @vueloId";

                    int disponibles = 0;
                    decimal precio = 0;

                    using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        if (!await reader.ReadAsync())
                            throw new Exception($"El vuelo {vuelo.VueloId} no existe.");
                        disponibles = reader.IsDBNull(0) ? 0 : reader.GetInt32(0);
                        precio = reader.IsDBNull(1) ? 0 : reader.GetDecimal(1);
                    }

                    if (disponibles < vuelo.CantidadPasajeros)
                    {
                        string clase = vuelo.ClaseId == 1 ? "Turista" : "Ejecutivo";
                        throw new Exception(
                            $"No hay suficientes boletos de clase {clase} en el vuelo {vuelo.VueloId}. " +
                            $"Disponibles: {disponibles}, solicitados: {vuelo.CantidadPasajeros}.");
                    }

                    total += precio * vuelo.CantidadPasajeros;
                }

                // ── PASO 2: Crear la reservación ──────────────────────────────────
                string noReservacion = GenerarNoReservacion();
                DateTime fechaExpiracion = DateTime.Now.AddMinutes(10);
                int reservacionId;

                string insertReservacion = @"
                    INSERT INTO Reservacion
                        (NoReservacion, UsuarioID, FechaReservacion, FechaExpiracion, Total, EstadoReservaID)
                    VALUES
                        (@noReservacion, @usuarioId, GETDATE(), @fechaExpiracion, @total, 1);
                    SELECT CAST(SCOPE_IDENTITY() AS INT);";

                using (var cmd = new SqlCommand(insertReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@noReservacion", noReservacion);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId.HasValue ? (object)usuarioId.Value : DBNull.Value);
                    cmd.Parameters.AddWithValue("@fechaExpiracion", fechaExpiracion);
                    cmd.Parameters.AddWithValue("@total", total);
                    reservacionId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                }

                // ── PASO 3: Por cada vuelo, decrementar y crear boletos ───────────
                foreach (var vuelo in vuelos)
                {
                    string campoDisponible = vuelo.ClaseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string campoPrecio = vuelo.ClaseId == 1 ? "PrecioTurista" : "PrecioEjecutivo";
                    string nombreClase = vuelo.ClaseId == 1 ? "Turista" : "Ejecutivo";

                    // 3a. Decrementar disponibilidad
                    string updateVuelo = $@"
                        UPDATE Vuelo
                        SET {campoDisponible} = {campoDisponible} - @cantidad
                        WHERE ID = @vueloId";

                    using (var cmd = new SqlCommand(updateVuelo, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@cantidad", vuelo.CantidadPasajeros);
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    // 3b. Precio de la clase
                    decimal precioClase = 0;
                    string queryPrecio = $"SELECT {campoPrecio} FROM Vuelo WHERE ID = @vueloId";
                    using (var cmd = new SqlCommand(queryPrecio, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        var result = await cmd.ExecuteScalarAsync();
                        precioClase = result == DBNull.Value ? 0 : Convert.ToDecimal(result);
                    }

                    // ── 3c. FIX DE ASIENTOS ──────────────────────────────────────
                    // Cargamos SOLO los asientos activos (Reservado=2 o Vendido=3).
                    // Los cancelados (4) quedan disponibles para reutilizarse.
                    string queryAsientosOcupados = @"
                        SELECT NoAsiento
                        FROM Boleto
                        WHERE VueloID    = @vueloId
                          AND ClaseID    = @claseId
                          AND EstadoBoletoID IN (2, 3)";

                    var asientosOcupados = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
                    using (var cmd = new SqlCommand(queryAsientosOcupados, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                        cmd.Parameters.AddWithValue("@claseId", vuelo.ClaseId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        while (await reader.ReadAsync())
                            asientosOcupados.Add(reader.GetString(0));
                    }

                    // 3d. Crear boletos buscando el primer asiento libre
                    string noBoletoBase = $"BOL{DateTime.Now:yyyyMMddHHmmss}";
                    string asientoActual = null; // cursor; null = empezar desde el principio

                    for (int i = 0; i < vuelo.CantidadPasajeros; i++)
                    {
                        // Iterar hasta encontrar un asiento no ocupado
                        do
                        {
                            asientoActual = SiguienteAsiento(asientoActual, vuelo.ClaseId);
                        }
                        while (asientosOcupados.Contains(asientoActual));

                        // Reservar en memoria para que el siguiente pasajero no lo tome
                        asientosOcupados.Add(asientoActual);

                        string noBoleto = $"{noBoletoBase}{reservacionId}{i}";

                        string insertBoleto = @"
                            INSERT INTO Boleto
                                (NoBoleto, NoAsiento, Precio, VueloID, ClaseID, EstadoBoletoID, ReservacionID)
                            VALUES
                                (@noBoleto, @noAsiento, @precio, @vueloId, @claseId, 2, @reservacionId);
                            SELECT CAST(SCOPE_IDENTITY() AS INT);";

                        int boletoId;
                        using (var cmd = new SqlCommand(insertBoleto, connection, transaction))
                        {
                            cmd.Parameters.AddWithValue("@noBoleto", noBoleto);
                            cmd.Parameters.AddWithValue("@noAsiento", asientoActual);
                            cmd.Parameters.AddWithValue("@precio", precioClase);
                            cmd.Parameters.AddWithValue("@vueloId", vuelo.VueloId);
                            cmd.Parameters.AddWithValue("@claseId", vuelo.ClaseId);
                            cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                            boletoId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                        }

                        boletosReservados.Add(new BoletoReservadoDTO
                        {
                            BoletoId = boletoId,
                            NoBoleto = noBoleto,
                            NoAsiento = asientoActual,
                            Precio = precioClase,
                            NumeroVuelo = vuelo.VueloId.ToString(), // se rellena abajo
                            Clase = nombreClase
                        });
                    }
                }

                // ── PASO 4: Enriquecer boletos con número de vuelo real ───────────
                var vueloIds = vuelos.Select(v => v.VueloId).Distinct().ToList();
                var numerosVuelo = new Dictionary<int, string>();

                string queryNumeros = $@"
                    SELECT ID, NumeroVuelo FROM Vuelo
                    WHERE ID IN ({string.Join(",", vueloIds)})";

                using (var cmd = new SqlCommand(queryNumeros, connection, transaction))
                {
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        numerosVuelo[reader.GetInt32(0)] = reader.GetString(1);
                }

                int idx = 0;
                foreach (var vuelo in vuelos)
                {
                    for (int i = 0; i < vuelo.CantidadPasajeros; i++)
                    {
                        if (numerosVuelo.TryGetValue(vuelo.VueloId, out var numVuelo))
                            boletosReservados[idx].NumeroVuelo = numVuelo;
                        idx++;
                    }
                }

                transaction.Commit();

                return new ReservacionCreadaDTO
                {
                    ReservacionId = reservacionId,
                    NoReservacion = noReservacion,
                    FechaExpiracion = fechaExpiracion,
                    Total = total,
                    Boletos = boletosReservados,
                    MinutosRestantes = 10
                };
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        // ─────────────────────────────────────────────────────────────────
        //  LIBERAR RESERVAS EXPIRADAS
        // ─────────────────────────────────────────────────────────────────
        public async Task<int> LiberarReservasExpiradas()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                string queryExpiradas = @"
                    SELECT ID FROM Reservacion
                    WHERE EstadoReservaID = 1
                      AND FechaExpiracion IS NOT NULL
                      AND FechaExpiracion < GETDATE()";

                var reservasExpiradas = new List<int>();
                using (var cmd = new SqlCommand(queryExpiradas, connection, transaction))
                {
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        reservasExpiradas.Add(reader.GetInt32(0));
                }

                if (reservasExpiradas.Count == 0)
                {
                    transaction.Commit();
                    return 0;
                }

                foreach (var reservaId in reservasExpiradas)
                {
                    string queryBoletosAgrupados = @"
                        SELECT VueloID, ClaseID, COUNT(*) AS Cantidad
                        FROM Boleto
                        WHERE ReservacionID = @reservaId AND EstadoBoletoID = 2
                        GROUP BY VueloID, ClaseID";

                    var grupos = new List<(int VueloId, int ClaseId, int Cantidad)>();
                    using (var cmd = new SqlCommand(queryBoletosAgrupados, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        while (await reader.ReadAsync())
                            grupos.Add((reader.GetInt32(0), reader.GetInt32(1), reader.GetInt32(2)));
                    }

                    string cancelarBoletos = @"
                        UPDATE Boleto SET EstadoBoletoID = 4
                        WHERE ReservacionID = @reservaId AND EstadoBoletoID = 2";
                    using (var cmd = new SqlCommand(cancelarBoletos, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    foreach (var (vueloId, claseId, cantidad) in grupos)
                    {
                        string campoDisponible = claseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                        string devolverDisp = $@"
                            UPDATE Vuelo SET {campoDisponible} = {campoDisponible} + @cantidad
                            WHERE ID = @vueloId";
                        using var cmd = new SqlCommand(devolverDisp, connection, transaction);
                        cmd.Parameters.AddWithValue("@cantidad", cantidad);
                        cmd.Parameters.AddWithValue("@vueloId", vueloId);
                        await cmd.ExecuteNonQueryAsync();
                    }

                    string expirarReservacion = @"
                        UPDATE Reservacion SET EstadoReservaID = 4 WHERE ID = @reservaId";
                    using (var cmd = new SqlCommand(expirarReservacion, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservaId", reservaId);
                        await cmd.ExecuteNonQueryAsync();
                    }
                }

                transaction.Commit();
                return reservasExpiradas.Count;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        // ─────────────────────────────────────────────────────────────────
        //  AGREGAR PASAJEROS
        // ─────────────────────────────────────────────────────────────────
        public async Task AgregarPasajerosAReservacion(int reservacionId, List<DatosPasajeroDTO> pasajeros)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                string queryVerificar = @"
                    SELECT EstadoReservaID, FechaExpiracion
                    FROM Reservacion WHERE ID = @reservacionId";

                int estadoReserva = 0;
                DateTime? fechaExpiracion = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("La reservación no existe.");
                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1)) fechaExpiracion = reader.GetDateTime(1);
                }

                if (estadoReserva != 1)
                    throw new Exception("La reservación no está en estado pendiente.");
                if (fechaExpiracion.HasValue && fechaExpiracion.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                foreach (var pasajero in pasajeros)
                {
                    string queryVerificarBoleto = @"
                        SELECT ReservacionID, DatosPasajeroID
                        FROM Boleto WHERE ID = @boletoId";

                    int? reservacionDelBoleto = null;
                    int? pasajeroExistente = null;

                    using (var cmd = new SqlCommand(queryVerificarBoleto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@boletoId", pasajero.BoletoId);
                        using var reader = await cmd.ExecuteReaderAsync();
                        if (!await reader.ReadAsync())
                            throw new Exception($"El boleto {pasajero.BoletoId} no existe.");
                        if (!reader.IsDBNull(0)) reservacionDelBoleto = reader.GetInt32(0);
                        if (!reader.IsDBNull(1)) pasajeroExistente = reader.GetInt32(1);
                    }

                    if (reservacionDelBoleto != reservacionId)
                        throw new Exception($"El boleto {pasajero.BoletoId} no pertenece a esta reservación.");

                    int paisId = await _paisRepository.ObtenerOCrearId(pasajero.Pais, connection, transaction);
                    int ciudadId = await _ciudadRepository.ObtenerOCrearId(pasajero.Ciudad, paisId, connection, transaction);

                    int datosPasajeroId;

                    if (pasajeroExistente.HasValue)
                    {
                        string updatePasajero = @"
                            UPDATE DatosPasajero
                            SET Nombre = @nombre, Apellido = @apellido, Pasaporte = @pasaporte,
                                Telefono = @telefono, CiudadID = @ciudadId
                            WHERE ID = @id";

                        using var cmd = new SqlCommand(updatePasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@id", pasajeroExistente.Value);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@ciudadId", ciudadId);
                        await cmd.ExecuteNonQueryAsync();
                        datosPasajeroId = pasajeroExistente.Value;
                    }
                    else
                    {
                        string insertPasajero = @"
                            INSERT INTO DatosPasajero (Nombre, Apellido, Pasaporte, Telefono, CiudadID)
                            VALUES (@nombre, @apellido, @pasaporte, @telefono, @ciudadId);
                            SELECT CAST(SCOPE_IDENTITY() AS INT);";

                        using var cmd = new SqlCommand(insertPasajero, connection, transaction);
                        cmd.Parameters.AddWithValue("@nombre", pasajero.Nombre);
                        cmd.Parameters.AddWithValue("@apellido", pasajero.Apellido);
                        cmd.Parameters.AddWithValue("@pasaporte", pasajero.Pasaporte);
                        cmd.Parameters.AddWithValue("@telefono", pasajero.Telefono);
                        cmd.Parameters.AddWithValue("@ciudadId", ciudadId);
                        datosPasajeroId = Convert.ToInt32(await cmd.ExecuteScalarAsync());
                    }

                    string updateBoleto = @"
                        UPDATE Boleto SET DatosPasajeroID = @datosPasajeroId WHERE ID = @boletoId";

                    using (var cmd = new SqlCommand(updateBoleto, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@datosPasajeroId", datosPasajeroId);
                        cmd.Parameters.AddWithValue("@boletoId", pasajero.BoletoId);
                        await cmd.ExecuteNonQueryAsync();
                    }
                }

                transaction.Commit();
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        // ─────────────────────────────────────────────────────────────────
        //  CONFIRMAR RESERVACIÓN
        // ─────────────────────────────────────────────────────────────────
        public async Task ConfirmarReservacion(int reservacionId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                string queryVerificar = @"
                    SELECT EstadoReservaID, FechaExpiracion
                    FROM Reservacion WHERE ID = @reservacionId";

                int estadoReserva = 0;
                DateTime? fechaExpiracion = null;

                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    if (!await reader.ReadAsync())
                        throw new Exception("La reservación no existe.");
                    estadoReserva = reader.GetInt32(0);
                    if (!reader.IsDBNull(1)) fechaExpiracion = reader.GetDateTime(1);
                }

                if (estadoReserva != 1)
                    throw new Exception("La reservación no está en estado pendiente.");
                if (fechaExpiracion.HasValue && fechaExpiracion.Value < DateTime.Now)
                    throw new Exception("La reservación ha expirado.");

                string queryVerificarPasajeros = @"
                    SELECT COUNT(*) FROM Boleto
                    WHERE ReservacionID = @reservacionId AND DatosPasajeroID IS NULL";

                using (var cmd = new SqlCommand(queryVerificarPasajeros, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    int sinPasajero = (int)await cmd.ExecuteScalarAsync();
                    if (sinPasajero > 0)
                        throw new Exception("Hay boletos sin pasajeros asignados. Complete todos los datos.");
                }

                string updateBoletos = @"
                    UPDATE Boleto SET EstadoBoletoID = 3 WHERE ReservacionID = @reservacionId";
                using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                string updateReservacion = @"
                    UPDATE Reservacion
                    SET EstadoReservaID = 2, FechaExpiracion = NULL
                    WHERE ID = @reservacionId";
                using (var cmd = new SqlCommand(updateReservacion, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
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

        // ─────────────────────────────────────────────────────────────────
        //  HELPERS PRIVADOS
        // ─────────────────────────────────────────────────────────────────
        private string GenerarNoReservacion()
            => "RES" + DateTime.Now.ToString("yyyyMMddHHmmss") + new Random().Next(1000, 9999);

        private string SiguienteAsiento(string ultimoAsiento, int claseId)
        {
            string prefijo = claseId == 2 ? "E-" : "";
            int columnas = 6;

            if (ultimoAsiento == null)
                return $"{prefijo}A1";

            string raw = (prefijo != "" && ultimoAsiento.StartsWith(prefijo))
                ? ultimoAsiento.Substring(prefijo.Length)
                : ultimoAsiento;

            int splitIdx = 0;
            while (splitIdx < raw.Length && char.IsLetter(raw[splitIdx])) splitIdx++;

            string filaLetras = raw.Substring(0, splitIdx);
            int columna = int.Parse(raw.Substring(splitIdx));

            if (columna < columnas)
                return $"{prefijo}{filaLetras}{columna + 1}";
            else
                return $"{prefijo}{SiguienteLetraFila(filaLetras)}1";
        }

        private string SiguienteLetraFila(string fila)
        {
            char[] letras = fila.ToCharArray();
            int i = letras.Length - 1;

            while (i >= 0)
            {
                if (letras[i] < 'Z')
                {
                    letras[i]++;
                    return new string(letras);
                }
                letras[i] = 'A';
                i--;
            }

            return "A" + new string(letras);
        }

        public async Task<int> CompletarReservaciones()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // Reservaciones confirmadas donde todos sus vuelos ya finalizaron
                // (EstadoVueloID = 3 o el valor que uses para Finalizado en tu tabla)
                string queryCompletables = @"
            SELECT DISTINCT r.ID
            FROM Reservacion r
            INNER JOIN Boleto b ON b.ReservacionID = r.ID
            INNER JOIN Vuelo  v ON v.ID = b.VueloID
            WHERE r.EstadoReservaID = 2
              AND NOT EXISTS (
                  SELECT 1
                  FROM Boleto b2
                  INNER JOIN Vuelo v2 ON v2.ID = b2.VueloID
                  WHERE b2.ReservacionID = r.ID
                    AND v2.EstadoID != 3  -- aún no finalizado
              )";

                var reservacionesACompletar = new List<int>();
                using (var cmd = new SqlCommand(queryCompletables, connection, transaction))
                {
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        reservacionesACompletar.Add(reader.GetInt32(0));
                }

                if (reservacionesACompletar.Count == 0)
                {
                    transaction.Commit();
                    return 0;
                }

                string ids = string.Join(",", reservacionesACompletar);
                string updateReservaciones = $@"
            UPDATE Reservacion
            SET EstadoReservaID = 5
            WHERE ID IN ({ids})";

                using (var cmd = new SqlCommand(updateReservaciones, connection, transaction))
                    await cmd.ExecuteNonQueryAsync();

                transaction.Commit();
                return reservacionesACompletar.Count;
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}