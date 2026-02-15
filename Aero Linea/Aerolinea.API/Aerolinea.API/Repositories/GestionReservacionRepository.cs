using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class GestionReservacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public GestionReservacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<ReservacionDetalleDTO>> ObtenerReservacionesPorUsuario(int usuarioId)
        {
            var reservaciones = new List<ReservacionDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Obtener todas las reservaciones del usuario
            string queryReservaciones = @"
                SELECT 
                    r.ID,
                    r.NoReservacion,
                    r.FechaCreacion,
                    r.FechaExpiracion,
                    r.Total,
                    er.Estado AS EstadoReserva,
                    r.EstadoReservaID,
                    u.ID AS UsuarioID,
                    u.Nombre + ' ' + u.Apellido AS UsuarioNombre,
                    u.Correo AS UsuarioEmail
                FROM Reservacion r
                INNER JOIN EstadoReserva er ON r.EstadoReservaID = er.ID
                INNER JOIN Usuario u ON r.UsuarioID = u.ID
                WHERE r.UsuarioID = @usuarioId
                ORDER BY r.FechaCreacion DESC";

            using var cmdReservaciones = new SqlCommand(queryReservaciones, connection);
            cmdReservaciones.Parameters.AddWithValue("@usuarioId", usuarioId);

            using var readerReservaciones = await cmdReservaciones.ExecuteReaderAsync();

            while (await readerReservaciones.ReadAsync())
            {
                var reservacion = new ReservacionDetalleDTO
                {
                    ReservacionId = readerReservaciones.GetInt32(0),
                    NoReservacion = readerReservaciones.GetString(1),
                    FechaCreacion = readerReservaciones.GetDateTime(2),
                    FechaExpiracion = readerReservaciones.IsDBNull(3) ? null : readerReservaciones.GetDateTime(3),
                    Total = readerReservaciones.GetDecimal(4),
                    EstadoReserva = readerReservaciones.GetString(5),
                    EstadoReservaId = readerReservaciones.GetInt32(6),
                    UsuarioId = readerReservaciones.GetInt32(7),
                    UsuarioNombre = readerReservaciones.GetString(8),
                    UsuarioEmail = readerReservaciones.GetString(9),
                    Boletos = new List<BoletoDetalleDTO>()
                };

                reservaciones.Add(reservacion);
            }

            readerReservaciones.Close();

            // 2. Para cada reservación, obtener sus boletos
            foreach (var reservacion in reservaciones)
            {
                string queryBoletos = @"
                    SELECT 
                        b.ID AS BoletoID,
                        b.NoBoleto,
                        b.NoAsiento,
                        b.Precio,
                        c.TipoDeClase AS Clase,
                        eb.Estado AS EstadoBoleto,
                        v.ID AS VueloID,
                        v.NumeroVuelo,
                        v.Fecha AS FechaVuelo,
                        v.HoraSalida,
                        v.HoraLlegada,
                        r.DuracionEstimada,
                        r.ID AS RutaID,
                        ao.Codigo AS OrigenCodigo,
                        ao.Nombre AS OrigenNombre,
                        co.Nombre AS OrigenCiudad,
                        ad.Codigo AS DestinoCodigo,
                        ad.Nombre AS DestinoNombre,
                        cd.Nombre AS DestinoCiudad,
                        a.Modelo AS AvionModelo,
                        a.Marca AS AvionMarca,
                        b.DatosPasajeroID
                    FROM Boleto b
                    INNER JOIN Clase c ON b.ClaseID = c.ID
                    INNER JOIN EstadoBoleto eb ON b.EstadoBoletoID = eb.ID
                    INNER JOIN Vuelo v ON b.VueloID = v.ID
                    INNER JOIN Ruta r ON v.RutaID = r.ID
                    INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                    INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                    INNER JOIN Ciudad co ON ao.CiudadID = co.ID
                    INNER JOIN Ciudad cd ON ad.CiudadID = cd.ID
                    INNER JOIN Avion a ON v.AvionID = a.ID
                    WHERE b.ReservacionID = @reservacionId
                    ORDER BY b.NoAsiento";

                using var cmdBoletos = new SqlCommand(queryBoletos, connection);
                cmdBoletos.Parameters.AddWithValue("@reservacionId", reservacion.ReservacionId);

                using var readerBoletos = await cmdBoletos.ExecuteReaderAsync();

                // Primero leer todos los boletos y guardar los IDs de pasajeros
                var boletosTemp = new List<(BoletoDetalleDTO boleto, int? pasajeroId)>();

                while (await readerBoletos.ReadAsync())
                {
                    var boleto = new BoletoDetalleDTO
                    {
                        BoletoId = readerBoletos.GetInt32(0),
                        NoBoleto = readerBoletos.GetString(1),
                        NoAsiento = readerBoletos.GetString(2),
                        Precio = readerBoletos.GetDecimal(3),
                        Clase = readerBoletos.GetString(4),
                        EstadoBoleto = readerBoletos.GetString(5),
                        VueloId = readerBoletos.GetInt32(6),
                        NumeroVuelo = readerBoletos.GetString(7),
                        FechaVuelo = readerBoletos.GetDateTime(8),
                        HoraSalida = readerBoletos.GetTimeSpan(9),
                        HoraLlegada = readerBoletos.GetTimeSpan(10),
                        DuracionMinutos = readerBoletos.GetInt32(11),
                        RutaId = readerBoletos.GetInt32(12),
                        OrigenCodigo = readerBoletos.GetString(13),
                        OrigenNombre = readerBoletos.GetString(14),
                        OrigenCiudad = readerBoletos.GetString(15),
                        DestinoCodigo = readerBoletos.GetString(16),
                        DestinoNombre = readerBoletos.GetString(17),
                        DestinoCiudad = readerBoletos.GetString(18),
                        AvionModelo = readerBoletos.GetString(19),
                        AvionMarca = readerBoletos.GetString(20)
                    };

                    int? pasajeroId = null;
                    if (!readerBoletos.IsDBNull(21))
                    {
                        pasajeroId = readerBoletos.GetInt32(21);
                    }

                    boletosTemp.Add((boleto, pasajeroId));
                }

                readerBoletos.Close();

                // Ahora obtener los datos de los pasajeros
                foreach (var (boleto, pasajeroId) in boletosTemp)
                {
                    if (pasajeroId.HasValue)
                    {
                        boleto.Pasajero = await ObtenerDatosPasajero(pasajeroId.Value, connection);
                    }
                    reservacion.Boletos.Add(boleto);
                }
            }

            return reservaciones;
        }

        public async Task<ReservacionDetalleDTO> ObtenerReservacionPorId(int reservacionId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Obtener la reservación
            string queryReservacion = @"
                SELECT 
                    r.ID,
                    r.NoReservacion,
                    r.FechaCreacion,
                    r.FechaExpiracion,
                    r.Total,
                    er.Estado AS EstadoReserva,
                    r.EstadoReservaID,
                    u.ID AS UsuarioID,
                    u.Nombre + ' ' + u.Apellido AS UsuarioNombre,
                    u.Correo AS UsuarioEmail
                FROM Reservacion r
                INNER JOIN EstadoReserva er ON r.EstadoReservaID = er.ID
                INNER JOIN Usuario u ON r.UsuarioID = u.ID
                WHERE r.ID = @reservacionId 
                  AND r.UsuarioID = @usuarioId";

            using var cmd = new SqlCommand(queryReservacion, connection);
            cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);

            using var reader = await cmd.ExecuteReaderAsync();

            if (!await reader.ReadAsync())
            {
                return null; // No existe o no pertenece al usuario
            }

            var reservacion = new ReservacionDetalleDTO
            {
                ReservacionId = reader.GetInt32(0),
                NoReservacion = reader.GetString(1),
                FechaCreacion = reader.GetDateTime(2),
                FechaExpiracion = reader.IsDBNull(3) ? null : reader.GetDateTime(3),
                Total = reader.GetDecimal(4),
                EstadoReserva = reader.GetString(5),
                EstadoReservaId = reader.GetInt32(6),
                UsuarioId = reader.GetInt32(7),
                UsuarioNombre = reader.GetString(8),
                UsuarioEmail = reader.GetString(9),
                Boletos = new List<BoletoDetalleDTO>()
            };

            reader.Close();

            // Obtener boletos (reutilizamos la misma lógica)
            string queryBoletos = @"
                SELECT 
                    b.ID AS BoletoID,
                    b.NoBoleto,
                    b.NoAsiento,
                    b.Precio,
                    c.TipoDeClase AS Clase,
                    eb.Estado AS EstadoBoleto,
                    v.ID AS VueloID,
                    v.NumeroVuelo,
                    v.Fecha AS FechaVuelo,
                    v.HoraSalida,
                    v.HoraLlegada,
                    r.DuracionEstimada,
                    r.ID AS RutaID,
                    ao.Codigo AS OrigenCodigo,
                    ao.Nombre AS OrigenNombre,
                    co.Nombre AS OrigenCiudad,
                    ad.Codigo AS DestinoCodigo,
                    ad.Nombre AS DestinoNombre,
                    cd.Nombre AS DestinoCiudad,
                    a.Modelo AS AvionModelo,
                    a.Marca AS AvionMarca,
                    b.DatosPasajeroID
                FROM Boleto b
                INNER JOIN Clase c ON b.ClaseID = c.ID
                INNER JOIN EstadoBoleto eb ON b.EstadoBoletoID = eb.ID
                INNER JOIN Vuelo v ON b.VueloID = v.ID
                INNER JOIN Ruta r ON v.RutaID = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad cd ON ad.CiudadID = cd.ID
                INNER JOIN Avion a ON v.AvionID = a.ID
                WHERE b.ReservacionID = @reservacionId
                ORDER BY b.NoAsiento";

            using var cmdBoletos = new SqlCommand(queryBoletos, connection);
            cmdBoletos.Parameters.AddWithValue("@reservacionId", reservacionId);

            using var readerBoletos = await cmdBoletos.ExecuteReaderAsync();

            var boletosTemp = new List<(BoletoDetalleDTO boleto, int? pasajeroId)>();

            while (await readerBoletos.ReadAsync())
            {
                var boleto = new BoletoDetalleDTO
                {
                    BoletoId = readerBoletos.GetInt32(0),
                    NoBoleto = readerBoletos.GetString(1),
                    NoAsiento = readerBoletos.GetString(2),
                    Precio = readerBoletos.GetDecimal(3),
                    Clase = readerBoletos.GetString(4),
                    EstadoBoleto = readerBoletos.GetString(5),
                    VueloId = readerBoletos.GetInt32(6),
                    NumeroVuelo = readerBoletos.GetString(7),
                    FechaVuelo = readerBoletos.GetDateTime(8),
                    HoraSalida = readerBoletos.GetTimeSpan(9),
                    HoraLlegada = readerBoletos.GetTimeSpan(10),
                    DuracionMinutos = readerBoletos.GetInt32(11),
                    RutaId = readerBoletos.GetInt32(12),
                    OrigenCodigo = readerBoletos.GetString(13),
                    OrigenNombre = readerBoletos.GetString(14),
                    OrigenCiudad = readerBoletos.GetString(15),
                    DestinoCodigo = readerBoletos.GetString(16),
                    DestinoNombre = readerBoletos.GetString(17),
                    DestinoCiudad = readerBoletos.GetString(18),
                    AvionModelo = readerBoletos.GetString(19),
                    AvionMarca = readerBoletos.GetString(20)
                };

                int? pasajeroId = null;
                if (!readerBoletos.IsDBNull(21))
                {
                    pasajeroId = readerBoletos.GetInt32(21);
                }

                boletosTemp.Add((boleto, pasajeroId));
            }

            readerBoletos.Close();

            // Ahora obtener los datos de los pasajeros
            foreach (var (boleto, pasajeroId) in boletosTemp)
            {
                if (pasajeroId.HasValue)
                {
                    boleto.Pasajero = await ObtenerDatosPasajero(pasajeroId.Value, connection);
                }
                reservacion.Boletos.Add(boleto);
            }

            return reservacion;
        }

        public async Task<ResumenReservacionesDTO> ObtenerResumenReservaciones(int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    COUNT(*) AS Total,
                    SUM(CASE WHEN EstadoReservaID = 1 THEN 1 ELSE 0 END) AS Pendientes,
                    SUM(CASE WHEN EstadoReservaID = 2 THEN 1 ELSE 0 END) AS Confirmadas,
                    SUM(CASE WHEN EstadoReservaID = 3 THEN 1 ELSE 0 END) AS Canceladas,
                    SUM(CASE WHEN EstadoReservaID = 4 THEN 1 ELSE 0 END) AS Expiradas,
                    SUM(CASE WHEN EstadoReservaID = 2 THEN Total ELSE 0 END) AS TotalGastado
                FROM Reservacion
                WHERE UsuarioID = @usuarioId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);

            using var reader = await cmd.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new ResumenReservacionesDTO
                {
                    TotalReservaciones = reader.GetInt32(0),
                    Pendientes = reader.GetInt32(1),
                    Confirmadas = reader.GetInt32(2),
                    Canceladas = reader.GetInt32(3),
                    Expiradas = reader.GetInt32(4),
                    TotalGastado = reader.GetDecimal(5)
                };
            }

            return new ResumenReservacionesDTO();
        }

        public async Task CancelarReservacion(int reservacionId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que la reservación existe y pertenece al usuario
                string queryVerificar = @"
                    SELECT EstadoReservaID
                    FROM Reservacion
                    WHERE ID = @reservacionId
                      AND UsuarioID = @usuarioId";

                int? estadoReserva = null;
                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);

                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado == null || resultado == DBNull.Value)
                    {
                        throw new Exception("Reservación no encontrada o no tienes acceso a ella.");
                    }

                    estadoReserva = (int)resultado;
                }

                // 2. Verificar que la reservación está en estado Pendiente o Confirmada
                if (estadoReserva != 1 && estadoReserva != 2) // 1: Pendiente, 2: Confirmada
                {
                    throw new Exception("Solo puedes cancelar reservaciones pendientes o confirmadas.");
                }

                // 3. Verificar que todos los vuelos son al menos 24 horas después
                string queryVuelos = @"
                    SELECT MIN(DATEDIFF(HOUR, GETDATE(), DATEADD(HOUR, DATEPART(HOUR, v.HoraSalida), DATEADD(MINUTE, DATEPART(MINUTE, v.HoraSalida), CAST(v.Fecha AS DATETIME))))) AS HorasHastaVuelo
                    FROM Boleto b
                    INNER JOIN Vuelo v ON b.VueloID = v.ID
                    WHERE b.ReservacionID = @reservacionId";

                using (var cmd = new SqlCommand(queryVuelos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    var horasHastaVuelo = await cmd.ExecuteScalarAsync();

                    if (horasHastaVuelo == null || horasHastaVuelo == DBNull.Value)
                    {
                        throw new Exception("No se encontraron vuelos en esta reservación.");
                    }

                    int horas = Convert.ToInt32(horasHastaVuelo);

                    if (horas < 24)
                    {
                        throw new Exception($"No puedes cancelar la reservación. Faltan menos de 24 horas para tu vuelo (quedan {horas} horas).");
                    }
                }

                // 4. Obtener información de los vuelos afectados (para actualizar BoletosDisponibles)
                string queryBoletosVuelo = @"
                    SELECT VueloID, COUNT(*) as Cantidad
                    FROM Boleto
                    WHERE ReservacionID = @reservacionId
                    GROUP BY VueloID";

                var vuelosAfectados = new Dictionary<int, int>();

                using (var cmd = new SqlCommand(queryBoletosVuelo, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();

                    while (await reader.ReadAsync())
                    {
                        vuelosAfectados[reader.GetInt32(0)] = reader.GetInt32(1);
                    }
                }

                // 5. Cambiar estado de los boletos a Disponible (1) y limpiar datos
                string updateBoletos = @"
                    UPDATE Boleto
                    SET EstadoBoletoID = 1,
                        ReservacionID = NULL,
                        DatosPasajeroID = NULL
                    WHERE ReservacionID = @reservacionId";

                using (var cmd = new SqlCommand(updateBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 6. Restaurar BoletosDisponibles en cada vuelo
                foreach (var vuelo in vuelosAfectados)
                {
                    string updateVuelo = @"
                        UPDATE Vuelo
                        SET BoletosDisponibles = BoletosDisponibles + @cantidad
                        WHERE ID = @vueloId";

                    using var cmd = new SqlCommand(updateVuelo, connection, transaction);
                    cmd.Parameters.AddWithValue("@cantidad", vuelo.Value);
                    cmd.Parameters.AddWithValue("@vueloId", vuelo.Key);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 7. Cambiar estado de la reservación a Cancelada (3)
                string updateReservacion = @"
                    UPDATE Reservacion
                    SET EstadoReservaID = 3,
                        FechaExpiracion = NULL
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

        private async Task<DatosPasajeroInfoDTO> ObtenerDatosPasajero(int pasajeroId, SqlConnection connection)
        {
            string query = @"
                SELECT 
                    dp.ID,
                    dp.Nombre,
                    dp.Apellido,
                    dp.Pasaporte,
                    dp.Telefono,
                    p.Nombre AS Pais,
                    c.Nombre AS Ciudad
                FROM DatosPasajero dp
                INNER JOIN Pais p ON dp.PaisID = p.ID
                INNER JOIN Ciudad c ON dp.CiudadID = c.ID
                WHERE dp.ID = @pasajeroId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@pasajeroId", pasajeroId);

            using var reader = await cmd.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new DatosPasajeroInfoDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    Pasaporte = reader.GetString(3),
                    Telefono = reader.GetString(4),
                    Pais = reader.GetString(5),
                    Ciudad = reader.GetString(6)
                };
            }

            return null;
        }
    }
}