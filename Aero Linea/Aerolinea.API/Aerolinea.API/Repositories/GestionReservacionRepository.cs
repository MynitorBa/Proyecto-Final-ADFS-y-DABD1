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
    }
}
