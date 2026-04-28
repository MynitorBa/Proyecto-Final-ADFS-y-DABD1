using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio administrativo de reservaciones.
    /// Accede a SqlDataReader exclusivamente via indexer reader["columna"] para evitar
    /// cualquier conflicto con metodos de extension o helpers del proyecto.
    /// </summary>
    public class AdminReservacionesRepository
    {
        private readonly DbConnectionFactory _db;
        public AdminReservacionesRepository(DbConnectionFactory db) => _db = db;

        // ── DTOs ──────────────────────────────────────────────────────────────────────

        public class ReservacionResumenDto
        {
            public int ReservacionId { get; set; }
            public string NoReservacion { get; set; } = "";
            public string EstadoReserva { get; set; } = "";
            public decimal Total { get; set; }
            public DateTime FechaCreacion { get; set; }
            public DateTime? FechaExpiracion { get; set; }
            public DateTime? FechaCancelacion { get; set; }
            public string? MotivoCancelacion { get; set; }
            public string UsuarioNombre { get; set; } = "";
            public string UsuarioEmail { get; set; } = "";
            public List<BoletoResumenDto> Boletos { get; set; } = new();
        }

        public class BoletoResumenDto
        {
            public int BoletoId { get; set; }
            public string NoBoleto { get; set; } = "";
            public string NumeroVuelo { get; set; } = "";
            public string OrigenCodigo { get; set; } = "";
            public string OrigenCiudad { get; set; } = "";
            public string DestinoCodigo { get; set; } = "";
            public string DestinoCiudad { get; set; } = "";
            public string? HoraSalida { get; set; }
            public DateTime? FechaVuelo { get; set; }
            public int? DuracionMinutos { get; set; }
            public string EstadoBoleto { get; set; } = "";
            public decimal Precio { get; set; }
            public string NoAsiento { get; set; } = "";
            public string Clase { get; set; } = "";
            public string? AvionMarca { get; set; }
            public string? AvionModelo { get; set; }
            public int? RutaId { get; set; }
            public PasajeroDto? Pasajero { get; set; }
        }

        public class PasajeroDto
        {
            public string Nombre { get; set; } = "";
            public string Apellido { get; set; } = "";
            public string Pasaporte { get; set; } = "";
            public string Telefono { get; set; } = "";
            public string Ciudad { get; set; } = "";
            public string Pais { get; set; } = "";
        }

        public class UsuarioEmailDto
        {
            public int UsuarioId { get; set; }
            public string Nombre { get; set; } = "";
            public string Email { get; set; } = "";
        }

        // ── Listado general ───────────────────────────────────────────────────────────

        public async Task<List<ReservacionResumenDto>> ObtenerTodasAsync()
        {
            const string sql = @"
                SELECT
                    r.ID                                     AS ReservacionId,
                    r.NoReservacion,
                    ISNULL(er.Estado, 'Pendiente')           AS EstadoReserva,
                    ISNULL(r.Total, 0)                       AS Total,
                    r.FechaCreacion,
                    r.FechaExpiracion,
                    r.FechaCancelacion,
                    r.MotivoCancelacion,
                    ISNULL(u.Nombre + ' ' + u.Apellido, '') AS UsuarioNombre,
                    ISNULL(u.Correo, '')                     AS UsuarioEmail
                FROM  Reservacion    r
                INNER JOIN Usuario       u  ON u.ID = r.UsuarioID
                LEFT  JOIN EstadoReserva er ON er.ID = r.EstadoReservaID
                ORDER BY r.FechaCreacion DESC";

            var lista = new List<ReservacionResumenDto>();
            using var conn = _db.CreateConnection();
            await conn.OpenAsync();

            using (var cmd = new SqlCommand(sql, conn))
            using (var dr = await cmd.ExecuteReaderAsync())
            {
                while (await dr.ReadAsync())
                {
                    lista.Add(new ReservacionResumenDto
                    {
                        ReservacionId = Convert.ToInt32(dr["ReservacionId"]),
                        NoReservacion = dr["NoReservacion"].ToString()!,
                        EstadoReserva = dr["EstadoReserva"].ToString()!,
                        Total = Convert.ToDecimal(dr["Total"]),
                        FechaCreacion = Convert.ToDateTime(dr["FechaCreacion"]),
                        FechaExpiracion = dr["FechaExpiracion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaExpiracion"]),
                        FechaCancelacion = dr["FechaCancelacion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaCancelacion"]),
                        MotivoCancelacion = dr["MotivoCancelacion"] == DBNull.Value ? null : dr["MotivoCancelacion"].ToString(),
                        UsuarioNombre = dr["UsuarioNombre"].ToString()!,
                        UsuarioEmail = dr["UsuarioEmail"].ToString()!,
                    });
                }
            }

            foreach (var res in lista)
            {
                try { res.Boletos = await ObtenerBoletosResumenAsync(conn, res.ReservacionId); }
                catch { res.Boletos = new List<BoletoResumenDto>(); }
            }

            return lista;
        }

        // ── Detalle por ID ────────────────────────────────────────────────────────────

        public async Task<ReservacionResumenDto?> ObtenerPorIdAsync(int reservacionId)
        {
            const string sql = @"
                SELECT
                    r.ID                                     AS ReservacionId,
                    r.NoReservacion,
                    ISNULL(er.Estado, 'Pendiente')           AS EstadoReserva,
                    ISNULL(r.Total, 0)                       AS Total,
                    r.FechaCreacion,
                    r.FechaExpiracion,
                    r.FechaCancelacion,
                    r.MotivoCancelacion,
                    ISNULL(u.Nombre + ' ' + u.Apellido, '') AS UsuarioNombre,
                    ISNULL(u.Correo, '')                     AS UsuarioEmail
                FROM  Reservacion    r
                INNER JOIN Usuario       u  ON u.ID = r.UsuarioID
                LEFT  JOIN EstadoReserva er ON er.ID = r.EstadoReservaID
                WHERE r.ID = @Id";

            using var conn = _db.CreateConnection();
            await conn.OpenAsync();

            ReservacionResumenDto? dto = null;
            using (var cmd = new SqlCommand(sql, conn))
            {
                cmd.Parameters.AddWithValue("@Id", reservacionId);
                using var dr = await cmd.ExecuteReaderAsync();
                if (await dr.ReadAsync())
                {
                    dto = new ReservacionResumenDto
                    {
                        ReservacionId = Convert.ToInt32(dr["ReservacionId"]),
                        NoReservacion = dr["NoReservacion"].ToString()!,
                        EstadoReserva = dr["EstadoReserva"].ToString()!,
                        Total = Convert.ToDecimal(dr["Total"]),
                        FechaCreacion = Convert.ToDateTime(dr["FechaCreacion"]),
                        FechaExpiracion = dr["FechaExpiracion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaExpiracion"]),
                        FechaCancelacion = dr["FechaCancelacion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaCancelacion"]),
                        MotivoCancelacion = dr["MotivoCancelacion"] == DBNull.Value ? null : dr["MotivoCancelacion"].ToString(),
                        UsuarioNombre = dr["UsuarioNombre"].ToString()!,
                        UsuarioEmail = dr["UsuarioEmail"].ToString()!,
                    };
                }
            }

            if (dto == null) return null;
            dto.Boletos = await ObtenerBoletosDetalleAsync(conn, reservacionId);
            return dto;
        }

        // ── Usuario para correo ───────────────────────────────────────────────────────

        public async Task<UsuarioEmailDto?> ObtenerUsuarioPorReservacionAsync(int reservacionId)
        {
            const string sql = @"
                SELECT  u.ID                        AS UsuarioId,
                        u.Nombre + ' ' + u.Apellido AS Nombre,
                        u.Correo                    AS Email
                FROM  Reservacion r
                INNER JOIN Usuario u ON u.ID = r.UsuarioID
                WHERE r.ID = @Id";

            using var conn = _db.CreateConnection();
            await conn.OpenAsync();
            using var cmd = new SqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@Id", reservacionId);
            using var dr = await cmd.ExecuteReaderAsync();
            if (!await dr.ReadAsync()) return null;
            return new UsuarioEmailDto
            {
                UsuarioId = Convert.ToInt32(dr["UsuarioId"]),
                Nombre = dr["Nombre"].ToString()!,
                Email = dr["Email"].ToString()!,
            };
        }

        // ── Cancelacion atomica ───────────────────────────────────────────────────────

        public async Task<bool> CancelarAsync(int reservacionId, string motivo)
        {
            // Replica la logica de ReservacionRepository.LiberarReservasExpiradas:
            // 1. Verificar estado actual (solo cancelar si es Pendiente=1 o Confirmada=2)
            // 2. Obtener grupos de boletos activos (estado 2=Pendiente o 3=Vendido) por VueloID+ClaseID
            // 3. Cancelar esos boletos (EstadoBoletoID = 4, igual que expiracion)
            // 4. Devolver disponibilidad: incrementar BoletosTurista/BoletosEjecutivo en Vuelo
            // 5. Cancelar reservacion (EstadoReservaID = 3, FechaCancelacion, MotivoCancelacion)

            using var conn = _db.CreateConnection();
            await conn.OpenAsync();
            using var tx = conn.BeginTransaction();
            try
            {
                // 1. Verificar que existe y puede cancelarse
                int estadoActual;
                using (var cmd = new SqlCommand(
                    "SELECT EstadoReservaID FROM Reservacion WHERE ID = @Id", conn, tx))
                {
                    cmd.Parameters.AddWithValue("@Id", reservacionId);
                    var val = await cmd.ExecuteScalarAsync();
                    if (val == null) return false;
                    estadoActual = Convert.ToInt32(val);
                }
                // No cancelar si ya esta Cancelada(3), Expirada(4) o Completada(5)
                if (estadoActual >= 3) return false;

                // 2. Obtener grupos de boletos activos para devolver disponibilidad
                //    Estados activos: 2=Pendiente, 3=Vendido (ocupan asientos en Vuelo)
                var grupos = new List<(int VueloId, int ClaseId, int Cantidad)>();
                using (var cmd = new SqlCommand(@"
                    SELECT VueloID, ClaseID, COUNT(*) AS Cantidad
                    FROM   Boleto
                    WHERE  ReservacionID = @Id AND EstadoBoletoID IN (2, 3)
                    GROUP BY VueloID, ClaseID", conn, tx))
                {
                    cmd.Parameters.AddWithValue("@Id", reservacionId);
                    using var reader = await cmd.ExecuteReaderAsync();
                    while (await reader.ReadAsync())
                        grupos.Add((reader.GetInt32(0), reader.GetInt32(1), reader.GetInt32(2)));
                }

                // 3. Cancelar boletos activos -> estado 4 (igual patron que expiracion)
                using (var cmd = new SqlCommand(@"
                    UPDATE Boleto SET EstadoBoletoID = 4
                    WHERE  ReservacionID = @Id AND EstadoBoletoID IN (2, 3)", conn, tx))
                {
                    cmd.Parameters.AddWithValue("@Id", reservacionId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 4. Devolver disponibilidad de asientos en Vuelo
                //    ClaseID=1 => BoletosTurista, ClaseID=2 => BoletosEjecutivo
                foreach (var (vueloId, claseId, cantidad) in grupos)
                {
                    string campo = claseId == 1 ? "BoletosTurista" : "BoletosEjecutivo";
                    string sqlDevolver = $"UPDATE Vuelo SET {campo} = {campo} + @cantidad WHERE ID = @vueloId";
                    using var cmd = new SqlCommand(sqlDevolver, conn, tx);
                    cmd.Parameters.AddWithValue("@cantidad", cantidad);
                    cmd.Parameters.AddWithValue("@vueloId", vueloId);
                    await cmd.ExecuteNonQueryAsync();
                }

                // 5. Marcar reservacion como Cancelada (EstadoReservaID = 3)
                using (var cmd = new SqlCommand(@"
                    UPDATE Reservacion
                    SET    EstadoReservaID   = 3,
                           FechaCancelacion  = GETDATE(),
                           MotivoCancelacion = @Motivo
                    WHERE  ID = @Id", conn, tx))
                {
                    cmd.Parameters.AddWithValue("@Id", reservacionId);
                    cmd.Parameters.AddWithValue("@Motivo", motivo);
                    await cmd.ExecuteNonQueryAsync();
                }

                await tx.CommitAsync();
                return true;
            }
            catch { await tx.RollbackAsync(); throw; }
        }

        // ── Boletos resumen (lista) ───────────────────────────────────────────────────

        private static async Task<List<BoletoResumenDto>> ObtenerBoletosResumenAsync(
            SqlConnection conn, int reservacionId)
        {
            const string sql = @"
                SELECT TOP 1
                    b.ID                            AS BoletoId,
                    b.NoBoleto,
                    v.NumeroVuelo,
                    v.Fecha                         AS FechaVuelo,
                    v.RutaID                        AS RutaId,
                    ISNULL(rut.DuracionEstimada, 0) AS DuracionMinutos,
                    ISNULL(ao.Codigo,  '')           AS OrigenCodigo,
                    ISNULL(co.Nombre,  '')           AS OrigenCiudad,
                    ISNULL(ad.Codigo,  '')           AS DestinoCodigo,
                    ISNULL(cd.Nombre,  '')           AS DestinoCiudad,
                    b.Precio
                FROM  Boleto b
                INNER JOIN Vuelo      v   ON  v.ID   = b.VueloID
                LEFT  JOIN Ruta       rut ON  rut.ID = v.RutaID
                LEFT  JOIN Aeropuerto ao  ON  ao.ID  = rut.OrigenID
                LEFT  JOIN Ciudad     co  ON  co.ID  = ao.CiudadID
                LEFT  JOIN Aeropuerto ad  ON  ad.ID  = rut.DestinoID
                LEFT  JOIN Ciudad     cd  ON  cd.ID  = ad.CiudadID
                WHERE b.ReservacionID = @Id
                ORDER BY b.ID";

            var lista = new List<BoletoResumenDto>();
            using var cmd = new SqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@Id", reservacionId);
            using var dr = await cmd.ExecuteReaderAsync();
            while (await dr.ReadAsync())
            {
                lista.Add(new BoletoResumenDto
                {
                    BoletoId = Convert.ToInt32(dr["BoletoId"]),
                    NoBoleto = dr["NoBoleto"].ToString()!,
                    NumeroVuelo = dr["NumeroVuelo"].ToString()!,
                    FechaVuelo = dr["FechaVuelo"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaVuelo"]),
                    RutaId = dr["RutaId"] == DBNull.Value ? null : (int?)Convert.ToInt32(dr["RutaId"]),
                    DuracionMinutos = Convert.ToInt32(dr["DuracionMinutos"]),
                    OrigenCodigo = dr["OrigenCodigo"].ToString()!,
                    OrigenCiudad = dr["OrigenCiudad"].ToString()!,
                    DestinoCodigo = dr["DestinoCodigo"].ToString()!,
                    DestinoCiudad = dr["DestinoCiudad"].ToString()!,
                    Precio = Convert.ToDecimal(dr["Precio"]),
                });
            }
            return lista;
        }

        // ── Boletos detalle (modal) ───────────────────────────────────────────────────

        private static async Task<List<BoletoResumenDto>> ObtenerBoletosDetalleAsync(
            SqlConnection conn, int reservacionId)
        {
            const string sql = @"
                SELECT
                    b.ID                            AS BoletoId,
                    b.NoBoleto,
                    b.Precio,
                    ISNULL(b.NoAsiento, '')          AS NoAsiento,
                    v.NumeroVuelo,
                    v.Fecha                         AS FechaVuelo,
                    v.RutaID                        AS RutaId,
                    v.HoraSalida,
                    ISNULL(cl.TipoDeClase, '')       AS Clase,
                    ISNULL(rut.DuracionEstimada, 0) AS DuracionMinutos,
                    ISNULL(ao.Codigo,  '')           AS OrigenCodigo,
                    ISNULL(co.Nombre,  '')           AS OrigenCiudad,
                    ISNULL(ad.Codigo,  '')           AS DestinoCodigo,
                    ISNULL(cd.Nombre,  '')           AS DestinoCiudad,
                    av.Marca                        AS AvionMarca,
                    av.Modelo                       AS AvionModelo,
                    ISNULL(eb.Estado, 'Pendiente')  AS EstadoBoleto,
                    dp.Nombre                       AS PasajeroNombre,
                    dp.Apellido                     AS PasajeroApellido,
                    ISNULL(dp.Pasaporte, '')         AS Pasaporte,
                    ISNULL(dp.Telefono,  '')         AS Telefono,
                    ISNULL(cp.Nombre,    '')         AS PasajeroCiudad,
                    ISNULL(pp.Nombre,    '')         AS PasajeroPais
                FROM  Boleto b
                INNER JOIN Vuelo         v   ON  v.ID   = b.VueloID
                LEFT  JOIN Ruta          rut ON  rut.ID = v.RutaID
                LEFT  JOIN Aeropuerto    ao  ON  ao.ID  = rut.OrigenID
                LEFT  JOIN Ciudad        co  ON  co.ID  = ao.CiudadID
                LEFT  JOIN Aeropuerto    ad  ON  ad.ID  = rut.DestinoID
                LEFT  JOIN Ciudad        cd  ON  cd.ID  = ad.CiudadID
                LEFT  JOIN Clase         cl  ON  cl.ID  = b.ClaseID
                LEFT  JOIN Avion         av  ON  av.ID  = v.AvionID
                LEFT  JOIN EstadoBoleto  eb  ON  eb.ID  = b.EstadoBoletoID
                LEFT  JOIN DatosPasajero dp  ON  dp.ID  = b.DatosPasajeroID
                LEFT  JOIN Ciudad        cp  ON  cp.ID  = dp.CiudadID
                LEFT  JOIN Pais          pp  ON  pp.ID  = cp.PaisID
                WHERE b.ReservacionID = @Id
                ORDER BY b.ID";

            var lista = new List<BoletoResumenDto>();
            using var cmd = new SqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@Id", reservacionId);
            using var dr = await cmd.ExecuteReaderAsync();

            while (await dr.ReadAsync())
            {
                string? horaSalida = null;
                if (dr["HoraSalida"] != DBNull.Value)
                    horaSalida = ((TimeSpan)dr["HoraSalida"]).ToString(@"hh\:mm\:ss");

                var tienePasajero = dr["PasajeroNombre"] != DBNull.Value;

                lista.Add(new BoletoResumenDto
                {
                    BoletoId = Convert.ToInt32(dr["BoletoId"]),
                    NoBoleto = dr["NoBoleto"].ToString()!,
                    NumeroVuelo = dr["NumeroVuelo"].ToString()!,
                    FechaVuelo = dr["FechaVuelo"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaVuelo"]),
                    RutaId = dr["RutaId"] == DBNull.Value ? null : (int?)Convert.ToInt32(dr["RutaId"]),
                    DuracionMinutos = Convert.ToInt32(dr["DuracionMinutos"]),
                    HoraSalida = horaSalida,
                    OrigenCodigo = dr["OrigenCodigo"].ToString()!,
                    OrigenCiudad = dr["OrigenCiudad"].ToString()!,
                    DestinoCodigo = dr["DestinoCodigo"].ToString()!,
                    DestinoCiudad = dr["DestinoCiudad"].ToString()!,
                    NoAsiento = dr["NoAsiento"].ToString()!,
                    Clase = dr["Clase"].ToString()!,
                    AvionMarca = dr["AvionMarca"] == DBNull.Value ? null : dr["AvionMarca"].ToString(),
                    AvionModelo = dr["AvionModelo"] == DBNull.Value ? null : dr["AvionModelo"].ToString(),
                    EstadoBoleto = dr["EstadoBoleto"].ToString()!,
                    Precio = Convert.ToDecimal(dr["Precio"]),
                    Pasajero = tienePasajero ? new PasajeroDto
                    {
                        Nombre = dr["PasajeroNombre"].ToString()!,
                        Apellido = dr["PasajeroApellido"].ToString()!,
                        Pasaporte = dr["Pasaporte"].ToString()!,
                        Telefono = dr["Telefono"].ToString()!,
                        Ciudad = dr["PasajeroCiudad"].ToString()!,
                        Pais = dr["PasajeroPais"].ToString()!,
                    } : null,
                });
            }
            return lista;
        }

        // ── DTO: Vuelo con resumen de reservaciones ───────────────────────────────────

        public class VueloResumenDto
        {
            public int VueloId { get; set; }
            public string NumeroVuelo { get; set; } = "";
            public DateTime? FechaVuelo { get; set; }
            public string? HoraSalida { get; set; }
            public string? HoraLlegada { get; set; }
            public int DuracionEstimada { get; set; }
            public string OrigenCodigo { get; set; } = "";
            public string OrigenCiudad { get; set; } = "";
            public string DestinoCodigo { get; set; } = "";
            public string DestinoCiudad { get; set; } = "";
            public int TotalReservaciones { get; set; }
            public int Pendientes { get; set; }
            public int Confirmadas { get; set; }
            public int Canceladas { get; set; }
            public int Completadas { get; set; }
        }

        // ── Vuelos con reservaciones agrupadas ────────────────────────────────────────

        /// <summary>
        /// Retorna todos los vuelos que tienen al menos una reservacion, con conteos
        /// por estado. Usado para la vista agrupada del panel administrativo.
        /// </summary>
        public async Task<List<VueloResumenDto>> ObtenerVuelosConReservacionesAsync()
        {
            const string sql = @"
                SELECT
                    v.ID                                    AS VueloId,
                    v.NumeroVuelo,
                    v.Fecha                                 AS FechaVuelo,
                    v.HoraSalida,
                    v.HoraLlegada,
                    ISNULL(rut.DuracionEstimada, 0)         AS DuracionEstimada,
                    ISNULL(ao.Codigo, '')                   AS OrigenCodigo,
                    ISNULL(co.Nombre, '')                   AS OrigenCiudad,
                    ISNULL(ad.Codigo, '')                   AS DestinoCodigo,
                    ISNULL(cd.Nombre, '')                   AS DestinoCiudad,
                    COUNT(DISTINCT r.ID)                    AS TotalReservaciones,
                    COUNT(DISTINCT CASE WHEN r.EstadoReservaID = 1 THEN r.ID END) AS Pendientes,
                    COUNT(DISTINCT CASE WHEN r.EstadoReservaID = 2 THEN r.ID END) AS Confirmadas,
                    COUNT(DISTINCT CASE WHEN r.EstadoReservaID = 3 THEN r.ID END) AS Canceladas,
                    COUNT(DISTINCT CASE WHEN r.EstadoReservaID = 5 THEN r.ID END) AS Completadas
                FROM  Vuelo v
                LEFT  JOIN Ruta       rut ON rut.ID = v.RutaID
                LEFT  JOIN Aeropuerto ao  ON ao.ID  = rut.OrigenID
                LEFT  JOIN Ciudad     co  ON co.ID  = ao.CiudadID
                LEFT  JOIN Aeropuerto ad  ON ad.ID  = rut.DestinoID
                LEFT  JOIN Ciudad     cd  ON cd.ID  = ad.CiudadID
                INNER JOIN Boleto     b   ON b.VueloID = v.ID
                INNER JOIN Reservacion r  ON r.ID = b.ReservacionID
                GROUP BY
                    v.ID, v.NumeroVuelo, v.Fecha, v.HoraSalida, v.HoraLlegada,
                    rut.DuracionEstimada, ao.Codigo, co.Nombre, ad.Codigo, cd.Nombre
                ORDER BY v.Fecha DESC, v.HoraSalida DESC";

            var lista = new List<VueloResumenDto>();
            using var conn = _db.CreateConnection();
            await conn.OpenAsync();
            using var cmd = new SqlCommand(sql, conn);
            using var dr = await cmd.ExecuteReaderAsync();

            while (await dr.ReadAsync())
            {
                string? horaSalida = dr["HoraSalida"] == DBNull.Value ? null : ((TimeSpan)dr["HoraSalida"]).ToString(@"hh\:mm\:ss");
                string? horaLlegada = dr["HoraLlegada"] == DBNull.Value ? null : ((TimeSpan)dr["HoraLlegada"]).ToString(@"hh\:mm\:ss");

                lista.Add(new VueloResumenDto
                {
                    VueloId = Convert.ToInt32(dr["VueloId"]),
                    NumeroVuelo = dr["NumeroVuelo"].ToString()!,
                    FechaVuelo = dr["FechaVuelo"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaVuelo"]),
                    HoraSalida = horaSalida,
                    HoraLlegada = horaLlegada,
                    DuracionEstimada = Convert.ToInt32(dr["DuracionEstimada"]),
                    OrigenCodigo = dr["OrigenCodigo"].ToString()!,
                    OrigenCiudad = dr["OrigenCiudad"].ToString()!,
                    DestinoCodigo = dr["DestinoCodigo"].ToString()!,
                    DestinoCiudad = dr["DestinoCiudad"].ToString()!,
                    TotalReservaciones = Convert.ToInt32(dr["TotalReservaciones"]),
                    Pendientes = Convert.ToInt32(dr["Pendientes"]),
                    Confirmadas = Convert.ToInt32(dr["Confirmadas"]),
                    Canceladas = Convert.ToInt32(dr["Canceladas"]),
                    Completadas = Convert.ToInt32(dr["Completadas"]),
                });
            }
            return lista;
        }

        // ── Listado filtrado por vuelo ─────────────────────────────────────────────

        /// <summary>
        /// Obtiene reservaciones que contienen al menos un boleto del vuelo indicado.
        /// </summary>
        public async Task<List<ReservacionResumenDto>> ObtenerPorVueloAsync(int vueloId)
        {
            const string sql = @"
                SELECT
                    r.ID                                     AS ReservacionId,
                    r.NoReservacion,
                    ISNULL(er.Estado, 'Pendiente')           AS EstadoReserva,
                    ISNULL(r.Total, 0)                       AS Total,
                    r.FechaCreacion,
                    r.FechaExpiracion,
                    r.FechaCancelacion,
                    r.MotivoCancelacion,
                    ISNULL(u.Nombre + ' ' + u.Apellido, '') AS UsuarioNombre,
                    ISNULL(u.Correo, '')                     AS UsuarioEmail
                FROM  Reservacion    r
                INNER JOIN Usuario       u  ON u.ID = r.UsuarioID
                LEFT  JOIN EstadoReserva er ON er.ID = r.EstadoReservaID
                WHERE EXISTS (
                    SELECT 1 FROM Boleto bx WHERE bx.ReservacionID = r.ID AND bx.VueloID = @VueloId
                )
                ORDER BY r.FechaCreacion DESC";

            var lista = new List<ReservacionResumenDto>();
            using var conn = _db.CreateConnection();
            await conn.OpenAsync();

            using (var cmd = new SqlCommand(sql, conn))
            {
                cmd.Parameters.AddWithValue("@VueloId", vueloId);
                using var dr = await cmd.ExecuteReaderAsync();
                while (await dr.ReadAsync())
                {
                    lista.Add(new ReservacionResumenDto
                    {
                        ReservacionId = Convert.ToInt32(dr["ReservacionId"]),
                        NoReservacion = dr["NoReservacion"].ToString()!,
                        EstadoReserva = dr["EstadoReserva"].ToString()!,
                        Total = Convert.ToDecimal(dr["Total"]),
                        FechaCreacion = Convert.ToDateTime(dr["FechaCreacion"]),
                        FechaExpiracion = dr["FechaExpiracion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaExpiracion"]),
                        FechaCancelacion = dr["FechaCancelacion"] == DBNull.Value ? null : (DateTime?)Convert.ToDateTime(dr["FechaCancelacion"]),
                        MotivoCancelacion = dr["MotivoCancelacion"] == DBNull.Value ? null : dr["MotivoCancelacion"].ToString(),
                        UsuarioNombre = dr["UsuarioNombre"].ToString()!,
                        UsuarioEmail = dr["UsuarioEmail"].ToString()!,
                    });
                }
            }

            foreach (var res in lista)
            {
                try { res.Boletos = await ObtenerBoletosResumenAsync(conn, res.ReservacionId); }
                catch { res.Boletos = new List<BoletoResumenDto>(); }
            }

            return lista;
        }

        // ── Editar datos de pasajero (Admin — sin restriccion de usuario) ─────────────

        /// <summary>
        /// Actualiza los datos del pasajero de un boleto sin verificar pertenencia al usuario.
        /// Solo disponible para administradores. Valida que el boleto exista.
        /// </summary>
        public async Task EditarDatosPasajeroAsync(int boletoId, EditarDatosPasajeroDTO dto)
        {
            using var conn = _db.CreateConnection();
            await conn.OpenAsync();

            // Obtener el ID del registro DatosPasajero ligado al boleto
            int datosPasajeroId;
            using (var cmd = new SqlCommand(
                "SELECT DatosPasajeroID FROM Boleto WHERE ID = @boletoId", conn))
            {
                cmd.Parameters.AddWithValue("@boletoId", boletoId);
                var result = await cmd.ExecuteScalarAsync();
                if (result == null || result == DBNull.Value)
                    throw new ArgumentException($"Boleto {boletoId} no encontrado.");
                datosPasajeroId = Convert.ToInt32(result);
            }

            using var cmdUpd = new SqlCommand(@"
                UPDATE DatosPasajero SET
                    Nombre    = @nombre,
                    Apellido  = @apellido,
                    Pasaporte = @pasaporte,
                    Telefono  = @telefono
                WHERE ID = @datosPasajeroId", conn);
            cmdUpd.Parameters.AddWithValue("@nombre",          dto.Nombre.Trim());
            cmdUpd.Parameters.AddWithValue("@apellido",        dto.Apellido.Trim());
            cmdUpd.Parameters.AddWithValue("@pasaporte",       dto.Pasaporte.Trim());
            cmdUpd.Parameters.AddWithValue("@telefono",        dto.Telefono.Trim());
            cmdUpd.Parameters.AddWithValue("@datosPasajeroId", datosPasajeroId);
            await cmdUpd.ExecuteNonQueryAsync();
        }

        // ── CAMBIAR VUELO (Admin) — DESHABILITADO ─────────────────────────────────────
        // FUNCIONALIDAD REMOVIDA: No se permite cambiar de vuelo una vez creada la reserva
        // ObtenerVuelosElegiblesAdmin() — REMOVIDO
        // EjecutarCambioVueloAdmin() — REMOVIDO
    }
}