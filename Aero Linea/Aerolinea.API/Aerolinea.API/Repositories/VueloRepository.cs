using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    public class VueloRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public VueloRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        public async Task<List<VueloDetalleDTO>> BuscarVuelos(int origenId, int destinoId, DateTime fecha, int cantidadPasajeros, int? claseId = null)
        {
            var vuelos = new List<VueloDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // El WHERE de disponibilidad depende de si se pide clase específica o cualquiera

            string filtroClase = claseId == 1
                ? "AND v.BoletosTurista   >= @cantidadPasajeros"
                : claseId == 2
                    ? "AND v.BoletosEjecutivo >= @cantidadPasajeros"
                    : "AND (v.BoletosTurista >= @cantidadPasajeros OR v.BoletosEjecutivo >= @cantidadPasajeros)";

            string query = $@"
                SELECT 
                    v.ID,
                    v.NumeroVuelo,
                    v.Fecha,
                    v.HoraSalida,
                    v.HoraLlegada,

                    -- Estado
                    e.ID   AS EstadoId,
                    e.Estatus,

                    -- Avión
                    a.ID                AS AvionId,
                    a.Modelo            AS AvionModelo,
                    a.Marca             AS AvionMarca,
                    a.CapacidadPasajeros,

                    -- Origen
                    ao.ID     AS OrigenId,
                    ao.Nombre AS OrigenNombre,
                    ao.Codigo AS OrigenCodigo,
                    co.Nombre AS OrigenCiudad,
                    po.Nombre AS OrigenPais,

                    -- Destino
                    ad.ID     AS DestinoId,
                    ad.Nombre AS DestinoNombre,
                    ad.Codigo AS DestinoCodigo,
                    cd.Nombre AS DestinoCiudad,
                    pd.Nombre AS DestinoPais,

                    -- Ruta
                    r.ID               AS RutaId,
                    r.DuracionEstimada,

                    -- Precios y disponibilidad directo del vuelo
                    v.PrecioTurista,
                    v.PrecioEjecutivo,
                    v.BoletosTurista,
                    v.BoletosEjecutivo

                FROM Vuelo v
                INNER JOIN Estado      e  ON v.EstadoID   = e.ID
                INNER JOIN Avion       a  ON v.AvionID    = a.ID
                INNER JOIN Ruta        r  ON v.RutaID     = r.ID
                INNER JOIN Aeropuerto  ao ON r.OrigenID   = ao.ID
                INNER JOIN Aeropuerto  ad ON r.DestinoID  = ad.ID
                INNER JOIN Ciudad      co ON ao.CiudadID  = co.ID
                INNER JOIN Ciudad      cd ON ad.CiudadID  = cd.ID
                INNER JOIN Pais        po ON co.PaisID    = po.ID
                INNER JOIN Pais        pd ON cd.PaisID    = pd.ID

                WHERE r.OrigenID  = @origenId
                  AND r.DestinoID = @destinoId
                  AND v.Fecha     = @fecha
                  AND e.Estatus   = 'A tiempo'
                  {filtroClase}

                ORDER BY v.HoraSalida";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@origenId", origenId);
            command.Parameters.AddWithValue("@destinoId", destinoId);
            command.Parameters.AddWithValue("@fecha", fecha.Date);
            command.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                vuelos.Add(new VueloDetalleDTO
                {
                    Id = reader.GetInt32(0),
                    NumeroVuelo = reader.GetString(1),
                    Fecha = reader.GetDateTime(2),
                    HoraSalida = reader.GetTimeSpan(3),
                    HoraLlegada = reader.GetTimeSpan(4),

                    EstadoId = reader.GetInt32(5),
                    Estado = reader.GetString(6),

                    AvionId = reader.GetInt32(7),
                    AvionModelo = reader.GetString(8),
                    AvionMarca = reader.GetString(9),
                    CapacidadPasajeros = reader.GetInt32(10),

                    OrigenId = reader.GetInt32(11),
                    OrigenNombre = reader.GetString(12),
                    OrigenCodigo = reader.GetString(13),
                    OrigenCiudad = reader.GetString(14),
                    OrigenPais = reader.GetString(15),

                    DestinoId = reader.GetInt32(16),
                    DestinoNombre = reader.GetString(17),
                    DestinoCodigo = reader.GetString(18),
                    DestinoCiudad = reader.GetString(19),
                    DestinoPais = reader.GetString(20),

                    RutaId = reader.GetInt32(21),
                    DuracionMinutos = reader.GetInt32(22),

                    PrecioTurista = reader.IsDBNull(23) ? null : reader.GetDecimal(23),
                    PrecioEjecutiva = reader.IsDBNull(24) ? null : reader.GetDecimal(24),
                    BoletosDisponiblesTurista = reader.IsDBNull(25) ? null : reader.GetInt32(25),
                    BoletosDisponiblesEjecutiva = reader.IsDBNull(26) ? null : reader.GetInt32(26)
                });
            }

            reader.Close();

            foreach (var vuelo in vuelos)
                vuelo.Tripulantes = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);

            return vuelos;
        }

        private async Task<List<TripulanteDTO>> ObtenerTripulantesPorVuelo(SqlConnection connection, int vueloId)
        {
            var tripulantes = new List<TripulanteDTO>();

            string query = @"
                SELECT 
                    mt.ID,
                    mt.Nombre,
                    mt.Apellido,
                    mt.RolID,
                    rt.Cargo AS NombreRol
                FROM EquipoPivote ep
                INNER JOIN MiembroTripulacion mt ON ep.MiembroTripulacionID = mt.ID
                INNER JOIN RolTripulacion     rt ON mt.RolID                = rt.ID
                WHERE ep.VueloID = @vueloId
                ORDER BY rt.Cargo, mt.Nombre";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@vueloId", vueloId);

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                tripulantes.Add(new TripulanteDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Apellido = reader.GetString(2),
                    RolID = reader.GetInt32(3),
                    NombreRol = reader.GetString(4),
                    NombreCompleto = $"{reader.GetString(1)} {reader.GetString(2)}"
                });
            }

            return tripulantes;
        }

        public async Task GuardarBusqueda(int origenId, int destinoId, DateTime fechaSalida, int cantidadPersonas, int? usuarioId)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            // Buscar RutaID a partir de origen y destino
            int? rutaId = null;
            using (var cmdRuta = new SqlCommand(
                "SELECT TOP 1 ID FROM Ruta WHERE OrigenID = @OrigenId AND DestinoID = @DestinoId", connection))
            {
                cmdRuta.Parameters.AddWithValue("@OrigenId", origenId);
                cmdRuta.Parameters.AddWithValue("@DestinoId", destinoId);
                var result = await cmdRuta.ExecuteScalarAsync();
                if (result != null) rutaId = (int)result;
            }

            // Si no existe la ruta, no guardamos la búsqueda
            if (!rutaId.HasValue) return;

            using var cmd = new SqlCommand(@"
                INSERT INTO Busqueda (RutaID, FechaSalida, CantidadPersonas, UsuarioID, TipoBusquedaID, Fecha)
                VALUES (@RutaId, @FechaSalida, @CantidadPersonas, @UsuarioId, 1, @FechaHoy)", connection);

            cmd.Parameters.AddWithValue("@RutaId", rutaId.Value);
            cmd.Parameters.AddWithValue("@FechaSalida", fechaSalida.Date);
            cmd.Parameters.AddWithValue("@CantidadPersonas", cantidadPersonas);
            cmd.Parameters.AddWithValue("@UsuarioId", usuarioId.HasValue ? (object)usuarioId.Value : DBNull.Value);
            cmd.Parameters.AddWithValue("@FechaHoy", DateTime.Now);

            await cmd.ExecuteNonQueryAsync();
        }
    }
}