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

            string queryReservaciones = @"
                SELECT 
                    r.ID,
                    r.NoReservacion,
                    r.FechaReservacion,
                    r.FechaExpiracion,
                    r.Total,
                    er.Estado AS EstadoReserva,
                    r.EstadoReservaID,
                    u.ID AS UsuarioID,
                    u.Nombre + ' ' + u.Apellido AS UsuarioNombre,
                    u.Correo AS UsuarioEmail,
                    r.FechaCancelacion,
                    r.MotivoCancelacion
                FROM Reservacion r
                INNER JOIN EstadoReserva er ON r.EstadoReservaID = er.ID
                INNER JOIN Usuario u ON r.UsuarioID = u.ID
                WHERE r.UsuarioID = @usuarioId
                ORDER BY r.FechaReservacion DESC";

            using var cmdRes = new SqlCommand(queryReservaciones, connection);
            cmdRes.Parameters.AddWithValue("@usuarioId", usuarioId);
            using var readerRes = await cmdRes.ExecuteReaderAsync();

            while (await readerRes.ReadAsync())
            {
                reservaciones.Add(new ReservacionDetalleDTO
                {
                    ReservacionId = readerRes.GetInt32(0),
                    NoReservacion = readerRes.GetString(1),
                    FechaCreacion = readerRes.GetDateTime(2),
                    FechaExpiracion = readerRes.IsDBNull(3) ? null : readerRes.GetDateTime(3),
                    Total = readerRes.GetDecimal(4),
                    EstadoReserva = readerRes.GetString(5),
                    EstadoReservaId = readerRes.GetInt32(6),
                    UsuarioId = readerRes.GetInt32(7),
                    UsuarioNombre = readerRes.GetString(8),
                    UsuarioEmail = readerRes.GetString(9),
                    FechaCancelacion = readerRes.IsDBNull(10) ? null : readerRes.GetDateTime(10),
                    MotivoCancelacion = readerRes.IsDBNull(11) ? null : readerRes.GetString(11),
                    Boletos = new List<BoletoDetalleDTO>()
                });
            }

            readerRes.Close();

            foreach (var reservacion in reservaciones)
                await CargarBoletos(reservacion, connection);

            return reservaciones;
        }

        public async Task<ReservacionDetalleDTO> ObtenerReservacionPorId(int reservacionId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string queryReservacion = @"
                SELECT 
                    r.ID,
                    r.NoReservacion,
                    r.FechaReservacion,
                    r.FechaExpiracion,
                    r.Total,
                    er.Estado AS EstadoReserva,
                    r.EstadoReservaID,
                    u.ID AS UsuarioID,
                    u.Nombre + ' ' + u.Apellido AS UsuarioNombre,
                    u.Correo AS UsuarioEmail,
                    r.FechaCancelacion,
                    r.MotivoCancelacion
                FROM Reservacion r
                INNER JOIN EstadoReserva er ON r.EstadoReservaID = er.ID
                INNER JOIN Usuario u ON r.UsuarioID = u.ID
                WHERE r.ID = @reservacionId
                  AND r.UsuarioID = @usuarioId";

            using var cmd = new SqlCommand(queryReservacion, connection);
            cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
            cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
            using var reader = await cmd.ExecuteReaderAsync();

            if (!await reader.ReadAsync()) return null;

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
                FechaCancelacion = reader.IsDBNull(10) ? null : reader.GetDateTime(10),
                MotivoCancelacion = reader.IsDBNull(11) ? null : reader.GetString(11),
                Boletos = new List<BoletoDetalleDTO>()
            };

            reader.Close();
            await CargarBoletos(reservacion, connection);

            reservacion.Factura = await ObtenerFactura(reservacionId, connection);
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
                    SUM(CASE WHEN EstadoReservaID = 5 THEN 1 ELSE 0 END) AS Completadas,
                    SUM(CASE WHEN EstadoReservaID IN (2, 5) THEN Total ELSE 0 END) AS TotalGastado
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
                    Completadas = reader.GetInt32(5),
                    TotalGastado = reader.GetDecimal(6)
                };
            }

            return new ResumenReservacionesDTO();
        }

        public async Task CancelarReservacion(int reservacionId, int usuarioId, string motivo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Verificar que existe y pertenece al usuario
                string queryVerificar = @"
                    SELECT EstadoReservaID FROM Reservacion
                    WHERE ID = @reservacionId AND UsuarioID = @usuarioId";

                int? estadoReserva = null;
                using (var cmd = new SqlCommand(queryVerificar, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                    var resultado = await cmd.ExecuteScalarAsync();
                    if (resultado == null || resultado == DBNull.Value)
                        throw new Exception("Reservación no encontrada o no tienes acceso a ella.");
                    estadoReserva = (int)resultado;
                }

                // 2. Solo Pendiente (1) o Confirmada (2)
                if (estadoReserva != 1 && estadoReserva != 2)
                    throw new Exception("Solo puedes cancelar reservaciones pendientes o confirmadas.");

                // 3. Validar 24 horas mínimas antes del vuelo (solo aplica a confirmadas)
                if (estadoReserva == 2)
                {
                    string queryHoras = @"
                        SELECT MIN(DATEDIFF(HOUR, GETDATE(),
                            DATEADD(HOUR,   DATEPART(HOUR,   v.HoraSalida),
                            DATEADD(MINUTE, DATEPART(MINUTE, v.HoraSalida),
                            CAST(v.Fecha AS DATETIME)))))
                        FROM Boleto b
                        INNER JOIN Vuelo v ON v.ID = b.VueloID
                        WHERE b.ReservacionID  = @reservacionId
                          AND b.EstadoBoletoID IN (2, 3)";

                    using (var cmd = new SqlCommand(queryHoras, connection, transaction))
                    {
                        cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                        var resultado = await cmd.ExecuteScalarAsync();
                        if (resultado == null || resultado == DBNull.Value)
                            throw new Exception("No se encontraron vuelos activos en esta reservación.");

                        int horas = Convert.ToInt32(resultado);
                        if (horas < 24)
                            throw new Exception(
                                $"No puedes cancelar. Faltan menos de 24 horas para tu vuelo (quedan {horas} horas).");
                    }
                }

                // 4. Agrupar boletos activos por vuelo y clase para devolver disponibilidad
                string queryGrupos = @"
                    SELECT VueloID, ClaseID, COUNT(*) AS Cantidad
                    FROM Boleto
                    WHERE ReservacionID  = @reservacionId
                      AND EstadoBoletoID IN (2, 3)
                    GROUP BY VueloID, ClaseID";

                var grupos = new List<(int VueloId, int ClaseId, int Cantidad)>();
                using (var cmd = new SqlCommand(queryGrupos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        grupos.Add((reader.GetInt32(0), reader.GetInt32(1), reader.GetInt32(2)));
                }

                // 5. Boletos → Cancelado (4)
                string cancelarBoletos = @"
                    UPDATE Boleto SET EstadoBoletoID = 4
                    WHERE ReservacionID  = @reservacionId
                      AND EstadoBoletoID IN (2, 3)";

                using (var cmd = new SqlCommand(cancelarBoletos, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 6. Devolver disponibilidad por clase
                foreach (var (vueloId, claseId, cantidad) in grupos)
                {
                    string campo = claseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string devolver = $"UPDATE Vuelo SET {campo} = {campo} + @cantidad WHERE ID = @vueloId";

                    using var cmd = new SqlCommand(devolver, connection, transaction);
                    cmd.Parameters.AddWithValue("@cantidad", cantidad);
                    cmd.Parameters.AddWithValue("@vueloId", vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 7. Reservación → Cancelada (3)
                string cancelarReserva = @"
                    UPDATE Reservacion
                    SET EstadoReservaID   = 3,
                        FechaExpiracion   = NULL,
                        FechaCancelacion  = GETDATE(),
                        MotivoCancelacion = @motivo
                    WHERE ID = @reservacionId";

                using (var cmd = new SqlCommand(cancelarReserva, connection, transaction))
                {
                    cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                    cmd.Parameters.AddWithValue("@motivo", string.IsNullOrWhiteSpace(motivo)
                        ? (object)DBNull.Value
                        : motivo.Trim());
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
        //  HELPERS PRIVADOS

        private async Task CargarBoletos(ReservacionDetalleDTO reservacion, SqlConnection connection)
        {
            string queryBoletos = @"
                SELECT 
                    b.ID,
                    b.NoBoleto,
                    b.NoAsiento,
                    b.Precio,
                    c.TipoDeClase   AS Clase,
                    eb.Estado       AS EstadoBoleto,
                    v.ID            AS VueloID,
                    v.NumeroVuelo,
                    v.Fecha         AS FechaVuelo,
                    v.HoraSalida,
                    v.HoraLlegada,
                    ru.DuracionEstimada,
                    ru.ID           AS RutaID,
                    ao.Codigo       AS OrigenCodigo,
                    ao.Nombre       AS OrigenNombre,
                    co.Nombre       AS OrigenCiudad,
                    ad.Codigo       AS DestinoCodigo,
                    ad.Nombre       AS DestinoNombre,
                    cd.Nombre       AS DestinoCiudad,
                    a.Modelo        AS AvionModelo,
                    a.Marca         AS AvionMarca,
                    b.DatosPasajeroID
                FROM Boleto b
                INNER JOIN Clase        c  ON c.ID  = b.ClaseID
                INNER JOIN EstadoBoleto eb ON eb.ID = b.EstadoBoletoID
                INNER JOIN Vuelo        v  ON v.ID  = b.VueloID
                INNER JOIN Ruta         ru ON ru.ID = v.RutaID
                INNER JOIN Aeropuerto   ao ON ao.ID = ru.OrigenID
                INNER JOIN Aeropuerto   ad ON ad.ID = ru.DestinoID
                INNER JOIN Ciudad       co ON co.ID = ao.CiudadID
                INNER JOIN Ciudad       cd ON cd.ID = ad.CiudadID
                INNER JOIN Avion        a  ON a.ID  = v.AvionID
                WHERE b.ReservacionID = @reservacionId
                ORDER BY b.NoAsiento";

            using var cmd = new SqlCommand(queryBoletos, connection);
            cmd.Parameters.AddWithValue("@reservacionId", reservacion.ReservacionId);
            using var reader = await cmd.ExecuteReaderAsync();

            var temp = new List<(BoletoDetalleDTO boleto, int? pasajeroId)>();

            while (await reader.ReadAsync())
            {
                temp.Add((new BoletoDetalleDTO
                {
                    BoletoId = reader.GetInt32(0),
                    NoBoleto = reader.GetString(1),
                    NoAsiento = reader.GetString(2),
                    Precio = reader.GetDecimal(3),
                    Clase = reader.GetString(4),
                    EstadoBoleto = reader.GetString(5),
                    VueloId = reader.GetInt32(6),
                    NumeroVuelo = reader.GetString(7),
                    FechaVuelo = reader.GetDateTime(8),
                    HoraSalida = reader.GetTimeSpan(9),
                    HoraLlegada = reader.GetTimeSpan(10),
                    DuracionMinutos = reader.GetInt32(11),
                    RutaId = reader.GetInt32(12),
                    OrigenCodigo = reader.GetString(13),
                    OrigenNombre = reader.GetString(14),
                    OrigenCiudad = reader.GetString(15),
                    DestinoCodigo = reader.GetString(16),
                    DestinoNombre = reader.GetString(17),
                    DestinoCiudad = reader.GetString(18),
                    AvionModelo = reader.GetString(19),
                    AvionMarca = reader.GetString(20)
                }, reader.IsDBNull(21) ? null : reader.GetInt32(21)));
            }

            reader.Close();

            foreach (var (boleto, pasajeroId) in temp)
            {
                if (pasajeroId.HasValue)
                    boleto.Pasajero = await ObtenerDatosPasajero(pasajeroId.Value, connection);
                reservacion.Boletos.Add(boleto);
            }
        }

        // El país se obtiene desde la ciudad (Ciudad → Pais), no del pasajero directamente
        private async Task<DatosPasajeroInfoDTO> ObtenerDatosPasajero(int pasajeroId, SqlConnection connection)
        {
            string query = @"
                SELECT 
                    dp.ID,
                    dp.Nombre,
                    dp.Apellido,
                    dp.Pasaporte,
                    dp.Telefono,
                    c.Nombre AS Ciudad,
                    p.Nombre AS Pais
                FROM DatosPasajero dp
                INNER JOIN Ciudad c ON c.ID  = dp.CiudadID
                INNER JOIN Pais   p ON p.ID  = c.PaisID
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
                    Ciudad = reader.GetString(5),
                    Pais = reader.GetString(6)
                };
            }

            return null;
        }

        private async Task<FacturaDTO?> ObtenerFactura(int reservacionId, SqlConnection connection)
        {
            string query = @"
        SELECT ID, ReservacionID, Fecha, NIT, CodigoPostal, Total
        FROM Factura
        WHERE ReservacionID = @reservacionId";

            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
            using var reader = await cmd.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return new FacturaDTO
                {
                    Id = reader.GetInt32(0),
                    ReservacionId = reader.GetInt32(1),
                    Fecha = reader.GetDateTime(2),
                    NIT = reader.GetString(3),
                    CodigoPostal = reader.GetString(4),
                    Total = reader.GetDecimal(5)
                };
            }

            return null;
        }




        //agencias
        public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = "SELECT UsuarioWebID FROM Agencia WHERE ID = @agenciaId";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync());
        }
        public async Task<PuedeCancelarDTO> PuedeCancelar(int reservacionId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Verificar que existe, pertenece al usuario y está en estado cancelable
            string queryEstado = @"
        SELECT EstadoReservaID FROM Reservacion
        WHERE ID = @reservacionId AND UsuarioID = @usuarioId";

            int? estado = null;
            using (var cmd = new SqlCommand(queryEstado, connection))
            {
                cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                cmd.Parameters.AddWithValue("@usuarioId", usuarioId);
                var resultado = await cmd.ExecuteScalarAsync();
                if (resultado == null || resultado == DBNull.Value)
                    return new PuedeCancelarDTO { PuedeCancelar = false, Razon = "Reservación no encontrada o no tienes acceso." };
                estado = (int)resultado;
            }

            if (estado != 1 && estado != 2)
                return new PuedeCancelarDTO { PuedeCancelar = false, Razon = "Solo se pueden cancelar reservaciones pendientes o confirmadas." };

            // 2. Si es pendiente, no hay vuelo confirmado aún — se puede cancelar directo
            if (estado == 1)
                return new PuedeCancelarDTO { PuedeCancelar = true, Razon = "Reservación pendiente, puede cancelarse." };

            // 3. Si es confirmada, validar 24hrs mínimas antes del vuelo
            string queryHoras = @"
        SELECT MIN(DATEDIFF(HOUR, GETDATE(),
            DATEADD(HOUR,   DATEPART(HOUR,   v.HoraSalida),
            DATEADD(MINUTE, DATEPART(MINUTE, v.HoraSalida),
            CAST(v.Fecha AS DATETIME)))))
        FROM Boleto b
        INNER JOIN Vuelo v ON v.ID = b.VueloID
        WHERE b.ReservacionID  = @reservacionId
          AND b.EstadoBoletoID IN (2, 3)";

            using (var cmd = new SqlCommand(queryHoras, connection))
            {
                cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                var resultado = await cmd.ExecuteScalarAsync();

                if (resultado == null || resultado == DBNull.Value)
                    return new PuedeCancelarDTO { PuedeCancelar = false, Razon = "No se encontraron vuelos activos en esta reservación." };

                int horas = Convert.ToInt32(resultado);
                if (horas < 24)
                    return new PuedeCancelarDTO
                    {
                        PuedeCancelar = false,
                        Razon = $"No puedes cancelar. Faltan menos de 24 horas para tu vuelo (quedan {horas} horas)."
                    };

                return new PuedeCancelarDTO
                {
                    PuedeCancelar = true,
                    Razon = $"Puedes cancelar. Faltan {horas} horas para tu vuelo."
                };
            }
        }
    }
}