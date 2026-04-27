using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de gestion de reservaciones para usuarios. Permite consultar el
    /// historial de reservaciones, obtener el detalle de una reservacion con sus boletos
    /// y pasajeros, cancelar reservaciones y verificar si se puede cancelar segun
    /// las reglas de tiempo antes del vuelo.
    /// </summary>
    public class GestionReservacionRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public GestionReservacionRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna el historial completo de reservaciones de un usuario con sus boletos
        /// asociados. Incluye informacion de vuelo, ruta, avion y datos del pasajero
        /// por cada boleto. Ordenadas por fecha de creacion descendente.
        /// </summary>
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

        /// <summary>
        /// Retorna el detalle completo de una reservacion especifica del usuario,
        /// incluyendo boletos con datos de vuelo, ruta, pasajeros y la factura si existe.
        /// Retorna null si la reservacion no existe o no pertenece al usuario.
        /// </summary>
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

        /// <summary>
        /// Retorna un resumen estadistico de las reservaciones del usuario: totales
        /// por estado (pendiente, confirmada, cancelada, expirada, completada)
        /// y el monto total gastado en reservaciones confirmadas y completadas.
        /// </summary>
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

        /// <summary>
        /// Cancela una reservacion pendiente o confirmada del usuario delegando toda
        /// la logica transaccional al procedimiento almacenado usp_CancelarReservacion.
        /// Dicho SP valida propiedad, estado y, para confirmadas, llama internamente a
        /// dbo.ufn_HorasHastaVuelo para aplicar la regla de las 24 horas antes del vuelo.
        /// Retorna los datos del usuario (NoReservacion, NombreUsuario, EmailUsuario)
        /// para que el servicio pueda enviar el correo de cancelacion.
        /// </summary>
        public async Task<(string NoReservacion, string NombreUsuario, string EmailUsuario)> CancelarReservacion(
            int reservacionId, int usuarioId, string motivo)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            using var cmd = new SqlCommand("usp_CancelarReservacion", connection);
            cmd.CommandType = System.Data.CommandType.StoredProcedure;

            cmd.Parameters.AddWithValue("@ReservacionID", reservacionId);
            cmd.Parameters.AddWithValue("@UsuarioID",     usuarioId);
            cmd.Parameters.AddWithValue("@Motivo", string.IsNullOrWhiteSpace(motivo)
                ? (object)DBNull.Value : motivo.Trim());
            cmd.Parameters.AddWithValue("@EsAdmin", 0);

            var pResultado     = cmd.Parameters.Add("@Resultado",     System.Data.SqlDbType.Int);
            var pMensaje       = cmd.Parameters.Add("@Mensaje",       System.Data.SqlDbType.VarChar, 500);
            var pNoReservacion = cmd.Parameters.Add("@NoReservacion", System.Data.SqlDbType.VarChar, 50);
            var pNombreUsuario = cmd.Parameters.Add("@NombreUsuario", System.Data.SqlDbType.VarChar, 200);
            var pEmailUsuario  = cmd.Parameters.Add("@EmailUsuario",  System.Data.SqlDbType.VarChar, 150);

            pResultado.Direction     = System.Data.ParameterDirection.Output;
            pMensaje.Direction       = System.Data.ParameterDirection.Output;
            pNoReservacion.Direction = System.Data.ParameterDirection.Output;
            pNombreUsuario.Direction = System.Data.ParameterDirection.Output;
            pEmailUsuario.Direction  = System.Data.ParameterDirection.Output;

            await cmd.ExecuteNonQueryAsync();

            int    resultado = (int)pResultado.Value;
            string mensaje   = pMensaje.Value?.ToString() ?? "";

            if (resultado != 0)
                throw new Exception(mensaje);

            return (
                pNoReservacion.Value?.ToString() ?? "",
                pNombreUsuario.Value?.ToString() ?? "",
                pEmailUsuario.Value?.ToString()  ?? ""
            );
        }
        // ── CAMBIAR VUELO ────────────────────────────────────────────────────────

        /// <summary>
        /// Retorna los vuelos elegibles para cambio de la reservacion del usuario.
        /// Criterios: mismo pais de origen, mismo aeropuerto de destino, mismo precio por boleto,
        /// misma clase, vuelo futuro, con asientos disponibles, diferente al actual.
        /// </summary>
        public async Task<List<VueloElegibleDTO>> ObtenerVuelosElegibles(int reservacionId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Obtener info del boleto actual (vueloId, claseId, precio, cantidad)
            var qInfo = @"
                SELECT b.VueloID, b.ClaseID, b.Precio, COUNT(*) AS Cantidad,
                       po.ID AS OrigenPaisId, r.DestinoID AS DestinoAeropuertoId
                FROM Boleto b
                INNER JOIN Reservacion res ON res.ID = b.ReservacionID
                INNER JOIN Vuelo v ON v.ID = b.VueloID
                INNER JOIN Ruta r ON r.ID = v.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Ciudad co ON co.ID = ao.CiudadID
                INNER JOIN Pais po ON po.ID = co.PaisID
                WHERE b.ReservacionID = @resId
                  AND res.UsuarioID = @userId
                  AND res.EstadoReservaID IN (1, 2)
                  AND b.EstadoBoletoID IN (2, 3)
                GROUP BY b.VueloID, b.ClaseID, b.Precio, po.ID, r.DestinoID";

            using var cmdInfo = new SqlCommand(qInfo, connection);
            cmdInfo.Parameters.AddWithValue("@resId",  reservacionId);
            cmdInfo.Parameters.AddWithValue("@userId", usuarioId);

            int currentVueloId = 0, claseId = 0, cantidad = 0, origenPaisId = 0, destinoAeropuertoId = 0;
            decimal precioActual = 0;

            using (var r = await cmdInfo.ExecuteReaderAsync())
            {
                if (!await r.ReadAsync())
                    return new List<VueloElegibleDTO>();
                currentVueloId       = r.GetInt32(0);
                claseId              = r.GetInt32(1);
                precioActual         = r.GetDecimal(2);
                cantidad             = r.GetInt32(3);
                origenPaisId         = r.GetInt32(4);
                destinoAeropuertoId  = r.GetInt32(5);
            }

            // 2. Buscar vuelos elegibles
            string campoPrecio = claseId == 2 ? "v.PrecioEjecutivo" : "v.PrecioTurista";
            string campoCapacidad = claseId == 2 ? "v.BoletosEjecutivo" : "v.BoletosTurista";

            var qElegibles = $@"
                SELECT v.ID, v.NumeroVuelo,
                       CONVERT(VARCHAR(10), v.Fecha, 23) AS FechaSalida,
                       LEFT(CAST(v.HoraSalida AS VARCHAR(8)), 5) AS HoraSalida,
                       LEFT(CAST(v.HoraLlegada AS VARCHAR(8)), 5) AS HoraLlegada,
                       ao.Codigo, co.Nombre AS OrigenCiudad, po.Nombre AS OrigenPais,
                       ad.Codigo, cd.Nombre AS DestinoCiudad,
                       {campoPrecio} AS Precio,
                       {campoCapacidad} - ISNULL((
                           SELECT COUNT(*) FROM Boleto b2
                           WHERE b2.VueloID = v.ID AND b2.ClaseID = @claseId
                             AND b2.EstadoBoletoID IN (2, 3)
                       ), 0) AS Disponibles
                FROM Vuelo v
                INNER JOIN Ruta r ON r.ID = v.RutaID
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Ciudad co ON co.ID = ao.CiudadID
                INNER JOIN Pais po ON po.ID = co.PaisID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                INNER JOIN Ciudad cd ON cd.ID = ad.CiudadID
                INNER JOIN Estado e ON e.ID = v.EstadoID
                WHERE po.ID = @origenPaisId
                  AND r.DestinoID = @destinoAeropuertoId
                  AND v.ID <> @currentVueloId
                  AND {campoPrecio} = @precio
                  AND e.Estatus = 'A tiempo'
                  AND (v.Fecha > CAST(GETDATE() AS DATE)
                       OR (v.Fecha = CAST(GETDATE() AS DATE) AND v.HoraSalida > CAST(GETDATE() AS TIME)))
                  AND ({campoCapacidad} - ISNULL((
                           SELECT COUNT(*) FROM Boleto b2
                           WHERE b2.VueloID = v.ID AND b2.ClaseID = @claseId
                             AND b2.EstadoBoletoID IN (2, 3)
                       ), 0)) >= @cantidad
                ORDER BY v.Fecha, v.HoraSalida";

            using var cmdE = new SqlCommand(qElegibles, connection);
            cmdE.Parameters.AddWithValue("@claseId",             claseId);
            cmdE.Parameters.AddWithValue("@origenPaisId",        origenPaisId);
            cmdE.Parameters.AddWithValue("@destinoAeropuertoId", destinoAeropuertoId);
            cmdE.Parameters.AddWithValue("@currentVueloId",      currentVueloId);
            cmdE.Parameters.AddWithValue("@precio",              precioActual);
            cmdE.Parameters.AddWithValue("@cantidad",            cantidad);

            var lista = new List<VueloElegibleDTO>();
            using var rdr = await cmdE.ExecuteReaderAsync();
            while (await rdr.ReadAsync())
            {
                var disponibles = rdr.IsDBNull(11) ? 0 : Convert.ToInt32(rdr.GetValue(11));
                lista.Add(new VueloElegibleDTO
                {
                    VueloId            = rdr.GetInt32(0),
                    NumeroVuelo        = rdr.GetString(1),
                    FechaSalida        = rdr.IsDBNull(2)  ? "" : rdr.GetString(2),
                    HoraSalida         = rdr.IsDBNull(3)  ? "" : rdr.GetString(3),
                    HoraLlegada        = rdr.IsDBNull(4)  ? "" : rdr.GetString(4),
                    OrigenCodigo       = rdr.IsDBNull(5)  ? "" : rdr.GetString(5),
                    OrigenCiudad       = rdr.IsDBNull(6)  ? "" : rdr.GetString(6),
                    OrigenPais         = rdr.IsDBNull(7)  ? "" : rdr.GetString(7),
                    DestinoCodigo      = rdr.IsDBNull(8)  ? "" : rdr.GetString(8),
                    DestinoCiudad      = rdr.IsDBNull(9)  ? "" : rdr.GetString(9),
                    PrecioPorBoleto    = rdr.IsDBNull(10) ? 0 : rdr.GetDecimal(10),
                    CantidadBoletos    = cantidad,
                    PrecioTotal        = (rdr.IsDBNull(10) ? 0 : rdr.GetDecimal(10)) * cantidad,
                    AsientosDisponibles = disponibles
                });
            }
            return lista;
        }

        /// <summary>
        /// Ejecuta el cambio de vuelo para la reservacion del usuario: reasigna cada boleto
        /// al nuevo vuelo con asientos frescos. Valida propiedad, estado y disponibilidad.
        /// Retorna la informacion necesaria para enviar el correo de confirmacion.
        /// </summary>
        public async Task<(string noReservacion, string noVuelo, string fecha, string hora, string nombreUsuario, string emailUsuario, decimal precioTotal)>
            EjecutarCambioVuelo(int reservacionId, int nuevoVueloId, int usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Validar reservacion (propiedad + estado)
                var qRes = @"
                    SELECT res.NoReservacion, res.EstadoReservaID, res.Total,
                           u.Nombre + ' ' + u.Apellido, u.Correo
                    FROM Reservacion res
                    INNER JOIN Usuario u ON u.ID = res.UsuarioID
                    WHERE res.ID = @resId AND res.UsuarioID = @userId";
                using var cmdRes = new SqlCommand(qRes, connection, transaction);
                cmdRes.Parameters.AddWithValue("@resId",  reservacionId);
                cmdRes.Parameters.AddWithValue("@userId", usuarioId);

                string noReservacion = "", nombreUsuario = "", emailUsuario = "";
                int estadoActual = 0;
                decimal totalReservacion = 0;
                using (var rr = await cmdRes.ExecuteReaderAsync())
                {
                    if (!await rr.ReadAsync()) throw new Exception("Reservación no encontrada o no tienes acceso.");
                    noReservacion    = rr.GetString(0);
                    estadoActual     = rr.GetInt32(1);
                    totalReservacion = rr.GetDecimal(2);
                    nombreUsuario    = rr.IsDBNull(3) ? "" : rr.GetString(3);
                    emailUsuario     = rr.IsDBNull(4) ? "" : rr.GetString(4);
                }
                if (estadoActual != 1 && estadoActual != 2)
                    throw new Exception("Solo se puede cambiar el vuelo de reservaciones pendientes o confirmadas.");

                // 2. Obtener boletos actuales
                var qBoletos = @"
                    SELECT b.ID, b.ClaseID, b.Precio, b.DatosPasajeroID
                    FROM Boleto b
                    WHERE b.ReservacionID = @resId AND b.EstadoBoletoID IN (2, 3)";
                using var cmdB = new SqlCommand(qBoletos, connection, transaction);
                cmdB.Parameters.AddWithValue("@resId", reservacionId);

                var boletos = new List<(int Id, int ClaseId, decimal Precio, int DatosPasajeroId)>();
                using (var rb = await cmdB.ExecuteReaderAsync())
                {
                    while (await rb.ReadAsync())
                        boletos.Add((rb.GetInt32(0), rb.GetInt32(1), rb.GetDecimal(2), rb.GetInt32(3)));
                }
                if (boletos.Count == 0) throw new Exception("No hay boletos activos en esta reservación.");

                int claseId = boletos[0].ClaseId;

                // 3. Validar nuevo vuelo (existe, futuro, mismo precio, capacidad)
                string campoPrecio    = claseId == 2 ? "v.PrecioEjecutivo" : "v.PrecioTurista";
                string campoCapacidad = claseId == 2 ? "v.BoletosEjecutivo" : "v.BoletosTurista";
                var qVuelo = $@"
                    SELECT v.NumeroVuelo, CONVERT(VARCHAR(10), v.Fecha, 23),
                           LEFT(CAST(v.HoraSalida AS VARCHAR(8)), 5),
                           {campoPrecio} AS Precio, {campoCapacidad} AS Capacidad
                    FROM Vuelo v
                    INNER JOIN Estado e ON e.ID = v.EstadoID
                    WHERE v.ID = @nuevoVueloId AND e.Estatus = 'A tiempo'
                      AND (v.Fecha > CAST(GETDATE() AS DATE)
                           OR (v.Fecha = CAST(GETDATE() AS DATE) AND v.HoraSalida > CAST(GETDATE() AS TIME)))";
                using var cmdV = new SqlCommand(qVuelo, connection, transaction);
                cmdV.Parameters.AddWithValue("@nuevoVueloId", nuevoVueloId);

                string noVuelo = "", fechaNueva = "", horaNueva = "";
                decimal precioNuevo = 0; int capacidad = 0;
                using (var rv = await cmdV.ExecuteReaderAsync())
                {
                    if (!await rv.ReadAsync()) throw new Exception("El vuelo seleccionado no está disponible o ya pasó.");
                    noVuelo    = rv.GetString(0);
                    fechaNueva = rv.GetString(1);
                    horaNueva  = rv.GetString(2);
                    precioNuevo = rv.IsDBNull(3) ? 0 : rv.GetDecimal(3);
                    capacidad  = rv.IsDBNull(4) ? 0 : rv.GetInt32(4);
                }
                if (precioNuevo != boletos[0].Precio)
                    throw new Exception("El precio del vuelo seleccionado no coincide con el de la reservación.");

                // 4. Verificar asientos disponibles en nuevo vuelo
                var qOcupados = @"SELECT COUNT(*) FROM Boleto WHERE VueloID = @vid AND ClaseID = @cid AND EstadoBoletoID IN (2,3)";
                using var cmdOc = new SqlCommand(qOcupados, connection, transaction);
                cmdOc.Parameters.AddWithValue("@vid", nuevoVueloId);
                cmdOc.Parameters.AddWithValue("@cid", claseId);
                int ocupados = Convert.ToInt32(await cmdOc.ExecuteScalarAsync());
                int disponibles = capacidad - ocupados;
                if (disponibles < boletos.Count)
                    throw new Exception($"El vuelo solo tiene {disponibles} asiento(s) disponible(s) y necesitas {boletos.Count}.");

                // 5. Obtener asientos ya ocupados en nuevo vuelo para asignar nuevos
                var qAsientosOc = @"SELECT NoAsiento FROM Boleto WHERE VueloID = @vid AND ClaseID = @cid AND EstadoBoletoID IN (2,3)";
                using var cmdAO = new SqlCommand(qAsientosOc, connection, transaction);
                cmdAO.Parameters.AddWithValue("@vid", nuevoVueloId);
                cmdAO.Parameters.AddWithValue("@cid", claseId);
                var asientosOcupados = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
                using (var rao = await cmdAO.ExecuteReaderAsync())
                    while (await rao.ReadAsync())
                        asientosOcupados.Add(rao.GetString(0));

                // 6. Reasignar cada boleto
                string prefijo = claseId == 2 ? "E-" : "";
                string[] columnas = { "A", "B", "C", "D", "E", "F" };
                string ultimoAsiento = null;

                foreach (var (boletoId, _, precio, _) in boletos)
                {
                    // Calcular siguiente asiento disponible
                    do { ultimoAsiento = SiguienteAsientoLocal(ultimoAsiento, prefijo, columnas); }
                    while (asientosOcupados.Contains(ultimoAsiento));
                    asientosOcupados.Add(ultimoAsiento);

                    string nuevoNoBoleto = $"CHG{reservacionId}{nuevoVueloId}{boletoId}{Guid.NewGuid().ToString("N")[..6].ToUpper()}";

                    var qUpdate = @"UPDATE Boleto SET VueloID = @vid, NoAsiento = @asiento, NoBoleto = @nob WHERE ID = @bid";
                    using var cmdU = new SqlCommand(qUpdate, connection, transaction);
                    cmdU.Parameters.AddWithValue("@vid",    nuevoVueloId);
                    cmdU.Parameters.AddWithValue("@asiento", ultimoAsiento);
                    cmdU.Parameters.AddWithValue("@nob",    nuevoNoBoleto);
                    cmdU.Parameters.AddWithValue("@bid",    boletoId);
                    await cmdU.ExecuteNonQueryAsync();
                }

                transaction.Commit();
                return (noReservacion, noVuelo, fechaNueva, horaNueva, nombreUsuario, emailUsuario, totalReservacion);
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }

        private static string SiguienteAsientoLocal(string ultimo, string prefijo, string[] columnas)
        {
            if (ultimo == null) return $"{prefijo}A1";
            string raw = prefijo != "" && ultimo.StartsWith(prefijo) ? ultimo.Substring(prefijo.Length) : ultimo;
            int idx = 0;
            while (idx < raw.Length && char.IsLetter(raw[idx])) idx++;
            string col = raw.Substring(0, idx);
            int fila = int.Parse(raw.Substring(idx));
            int colIdx = Array.IndexOf(columnas, col);
            if (colIdx < columnas.Length - 1) return $"{prefijo}{columnas[colIdx + 1]}{fila}";
            return $"{prefijo}A{fila + 1}";
        }

        //  HELPERS PRIVADOS

        /// <summary>
        /// Carga los boletos de una reservacion usando la vista vw_BoletoDetalle,
        /// que fusiona 13 JOINs en una sola consulta incluyendo datos del pasajero.
        /// Elimina la necesidad de llamar a ObtenerDatosPasajero por separado.
        /// </summary>
        private async Task CargarBoletos(ReservacionDetalleDTO reservacion, SqlConnection connection)
        {
            string queryBoletos = @"
                SELECT
                    BoletoID, NoBoleto, NoAsiento, Precio,
                    Clase, EstadoBoleto, VueloID, NumeroVuelo,
                    FechaVuelo, HoraSalida, HoraLlegada, DuracionEstimada,
                    RutaID, OrigenCodigo, OrigenNombre, OrigenCiudad,
                    DestinoCodigo, DestinoNombre, DestinoCiudad,
                    AvionModelo, AvionMarca, DatosPasajeroID,
                    PasajeroNombre, PasajeroApellido, PasajeroPasaporte,
                    PasajeroTelefono, PasajeroCiudad, PasajeroPais
                FROM dbo.vw_BoletoDetalle
                WHERE ReservacionID = @reservacionId
                ORDER BY NoAsiento";

            using var cmd = new SqlCommand(queryBoletos, connection);
            cmd.Parameters.AddWithValue("@reservacionId", reservacion.ReservacionId);
            using var reader = await cmd.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                var boleto = new BoletoDetalleDTO
                {
                    BoletoId        = reader.GetInt32(0),
                    NoBoleto        = reader.GetString(1),
                    NoAsiento       = reader.GetString(2),
                    Precio          = reader.GetDecimal(3),
                    Clase           = reader.GetString(4),
                    EstadoBoleto    = reader.GetString(5),
                    VueloId         = reader.GetInt32(6),
                    NumeroVuelo     = reader.GetString(7),
                    FechaVuelo      = reader.GetDateTime(8),
                    HoraSalida      = reader.GetTimeSpan(9),
                    HoraLlegada     = reader.GetTimeSpan(10),
                    DuracionMinutos = reader.GetInt32(11),
                    RutaId          = reader.GetInt32(12),
                    OrigenCodigo    = reader.GetString(13),
                    OrigenNombre    = reader.GetString(14),
                    OrigenCiudad    = reader.GetString(15),
                    DestinoCodigo   = reader.GetString(16),
                    DestinoNombre   = reader.GetString(17),
                    DestinoCiudad   = reader.GetString(18),
                    AvionModelo     = reader.GetString(19),
                    AvionMarca      = reader.GetString(20)
                };

                if (!reader.IsDBNull(21))
                {
                    boleto.Pasajero = new DatosPasajeroInfoDTO
                    {
                        Id        = reader.GetInt32(21),
                        Nombre    = reader.IsDBNull(22) ? "" : reader.GetString(22),
                        Apellido  = reader.IsDBNull(23) ? "" : reader.GetString(23),
                        Pasaporte = reader.IsDBNull(24) ? "" : reader.GetString(24),
                        Telefono  = reader.IsDBNull(25) ? "" : reader.GetString(25),
                        Ciudad    = reader.IsDBNull(26) ? "" : reader.GetString(26),
                        Pais      = reader.IsDBNull(27) ? "" : reader.GetString(27)
                    };
                }

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
        /// <summary>
        /// Obtiene el ID del usuario webservice asociado a la agencia indicada.
        /// Se usa para operaciones de gestion que requieren conocer el propietario de la agencia.
        /// </summary>
        public async Task<int> ObtenerUsuarioWebIdDeAgencia(int agenciaId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = "SELECT UsuarioWebID FROM Agencia WHERE ID = @agenciaId";
            using var cmd = new SqlCommand(query, connection);
            cmd.Parameters.AddWithValue("@agenciaId", agenciaId);
            return Convert.ToInt32(await cmd.ExecuteScalarAsync());
        }

        /// <summary>
        /// Verifica si una reservacion puede ser cancelada por el usuario. Evalua el
        /// estado actual y, para reservaciones confirmadas, si faltan mas de 24 horas
        /// para el vuelo. Retorna un DTO con el resultado y el motivo de la decision.
        /// </summary>
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

            // 3. Si es confirmada, validar 24hrs mínimas antes del vuelo usando la UDF
            using (var cmd = new SqlCommand("SELECT dbo.ufn_HorasHastaVuelo(@reservacionId)", connection))
            {
                cmd.Parameters.AddWithValue("@reservacionId", reservacionId);
                var resultado = await cmd.ExecuteScalarAsync();

                if (resultado == null || resultado == DBNull.Value)
                    return new PuedeCancelarDTO { PuedeCancelar = false, Razon = "No se encontraron vuelos activos en esta reservación." };

                int horas = Convert.ToInt32(resultado);

                if (horas == -1)
                    return new PuedeCancelarDTO { PuedeCancelar = false, Razon = "No se encontraron vuelos activos en esta reservación." };

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

        /// <summary>
        /// Edita los datos del pasajero (Nombre, Apellido, Pasaporte, Telefono) de un boleto
        /// perteneciente al usuario indicado. Valida que:
        ///  - El boleto exista y pertenezca al usuario.
        ///  - La reservacion este en estado Pendiente (1) o Confirmada (2).
        ///  - El vuelo salga en mas de 24 horas.
        /// </summary>
        public async Task EditarDatosPasajero(int boletoId, int usuarioId, EditarDatosPasajeroDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // 1. Verificar que el boleto pertenece al usuario y obtener datos del vuelo/reservacion
            var queryVerificar = @"
                SELECT
                    v.Fecha,
                    v.HoraSalida,
                    r.EstadoReservaID,
                    b.DatosPasajeroID
                FROM   Boleto b
                INNER JOIN Reservacion r ON r.ID = b.ReservacionID
                INNER JOIN Vuelo       v ON v.ID = b.VueloID
                WHERE  b.ID = @boletoId
                  AND  r.UsuarioID = @usuarioId";

            using var cmdVer = new SqlCommand(queryVerificar, connection);
            cmdVer.Parameters.AddWithValue("@boletoId",  boletoId);
            cmdVer.Parameters.AddWithValue("@usuarioId", usuarioId);

            DateTime fechaVuelo;
            TimeSpan horaSalida;
            int estadoReserva;
            int datosPasajeroId;

            using (var reader = await cmdVer.ExecuteReaderAsync())
            {
                if (!await reader.ReadAsync())
                    throw new ArgumentException("Boleto no encontrado o no pertenece a tu cuenta.");

                fechaVuelo     = reader.GetDateTime(0);
                horaSalida     = reader.GetTimeSpan(1);
                estadoReserva  = reader.GetInt32(2);
                datosPasajeroId = reader.GetInt32(3);
            }

            // 2. Validar estado de reservacion (Pendiente=1, Confirmada=2)
            if (estadoReserva != 1 && estadoReserva != 2)
                throw new InvalidOperationException("Solo puedes editar datos de reservaciones pendientes o confirmadas.");

            // 3. Validar que falten mas de 24 horas para el vuelo
            var salidaDateTime = fechaVuelo.Date + horaSalida;
            var horasRestantes = (salidaDateTime - DateTime.Now).TotalHours;
            if (horasRestantes < 24)
                throw new InvalidOperationException(
                    $"No puedes editar los datos: faltan menos de 24 horas para el vuelo ({horasRestantes:F0}h).");

            // 4. Actualizar los datos del pasajero
            var queryUpdate = @"
                UPDATE DatosPasajero SET
                    Nombre    = @nombre,
                    Apellido  = @apellido,
                    Pasaporte = @pasaporte,
                    Telefono  = @telefono
                WHERE ID = @datosPasajeroId";

            using var cmdUpd = new SqlCommand(queryUpdate, connection);
            cmdUpd.Parameters.AddWithValue("@nombre",          dto.Nombre.Trim());
            cmdUpd.Parameters.AddWithValue("@apellido",        dto.Apellido.Trim());
            cmdUpd.Parameters.AddWithValue("@pasaporte",       dto.Pasaporte.Trim());
            cmdUpd.Parameters.AddWithValue("@telefono",        dto.Telefono.Trim());
            cmdUpd.Parameters.AddWithValue("@datosPasajeroId", datosPasajeroId);

            await cmdUpd.ExecuteNonQueryAsync();
        }
    }
}
