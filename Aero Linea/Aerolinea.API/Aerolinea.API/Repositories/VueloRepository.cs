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

        public async Task<List<VueloDetalleDTO>> BuscarVuelos(int origenId, int destinoId, DateTime fecha, int cantidadPasajeros)
        {
            var vuelos = new List<VueloDetalleDTO>();

            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT 
                    v.ID,
                    v.NumeroVuelo,
                    v.Fecha,
                    v.HoraSalida,
                    v.HoraLlegada,
                    v.BoletosDisponibles,
                    
                    -- Estado
                    e.ID AS EstadoId,
                    e.Estatus,
                    
                    -- Avión
                    a.ID AS AvionId,
                    a.Modelo AS AvionModelo,
                    a.Marca AS AvionMarca,
                    a.CapacidadPasajeros,
                    
                    -- Origen
                    ao.ID AS OrigenId,
                    ao.Nombre AS OrigenNombre,
                    ao.Codigo AS OrigenCodigo,
                    co.Nombre AS OrigenCiudad,
                    po.Nombre AS OrigenPais,
                    
                    -- Destino
                    ad.ID AS DestinoId,
                    ad.Nombre AS DestinoNombre,
                    ad.Codigo AS DestinoCodigo,
                    cd.Nombre AS DestinoCiudad,
                    pd.Nombre AS DestinoPais,
                    
                    -- Ruta
                    r.ID AS RutaId,
                    r.DuracionEstimada,
                    
                    -- Precios por clase (primer boleto disponible de cada clase)
                    (SELECT TOP 1 b.Precio 
                     FROM Boleto b 
                     WHERE b.VueloID = v.ID 
                       AND b.ClaseID = 1
                       AND b.EstadoBoletoID = 1
                     ORDER BY b.Precio ASC) AS PrecioTurista,
                    
                    (SELECT TOP 1 b.Precio 
                     FROM Boleto b 
                     WHERE b.VueloID = v.ID 
                       AND b.ClaseID = 2
                       AND b.EstadoBoletoID = 1
                     ORDER BY b.Precio ASC) AS PrecioEjecutiva,
                    
                    -- Boletos disponibles por clase
                    (SELECT COUNT(*) 
                     FROM Boleto b 
                     WHERE b.VueloID = v.ID 
                       AND b.ClaseID = 1
                       AND b.EstadoBoletoID = 1) AS BoletosDisponiblesTurista,
                    
                    (SELECT COUNT(*) 
                     FROM Boleto b 
                     WHERE b.VueloID = v.ID 
                       AND b.ClaseID = 2
                       AND b.EstadoBoletoID = 1) AS BoletosDisponiblesEjecutiva
                    
                FROM Vuelo v
                INNER JOIN Estado e ON v.EstadoID = e.ID
                INNER JOIN Avion a ON v.AvionID = a.ID
                INNER JOIN Ruta r ON v.RutaID = r.ID
                INNER JOIN Aeropuerto ao ON r.OrigenID = ao.ID
                INNER JOIN Aeropuerto ad ON r.DestinoID = ad.ID
                INNER JOIN Ciudad co ON ao.CiudadID = co.ID
                INNER JOIN Ciudad cd ON ad.CiudadID = cd.ID
                INNER JOIN Pais po ON co.PaisID = po.ID
                INNER JOIN Pais pd ON cd.PaisID = pd.ID
                
                WHERE r.OrigenID = @origenId
                  AND r.DestinoID = @destinoId
                  AND v.Fecha = @fecha
                  AND e.Estatus = 'A tiempo'
                  AND v.BoletosDisponibles >= @cantidadPasajeros
                  
                ORDER BY v.HoraSalida";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@origenId", origenId);
            command.Parameters.AddWithValue("@destinoId", destinoId);
            command.Parameters.AddWithValue("@fecha", fecha.Date);
            command.Parameters.AddWithValue("@cantidadPasajeros", cantidadPasajeros);

            using var reader = await command.ExecuteReaderAsync();

            while (await reader.ReadAsync())
            {
                var vuelo = new VueloDetalleDTO
                {
                    Id = reader.GetInt32(0),
                    NumeroVuelo = reader.GetString(1),
                    Fecha = reader.GetDateTime(2),
                    HoraSalida = reader.GetTimeSpan(3),
                    HoraLlegada = reader.GetTimeSpan(4),
                    BoletosDisponibles = reader.GetInt32(5),

                    // Estado
                    EstadoId = reader.GetInt32(6),
                    Estado = reader.GetString(7),

                    // Avión
                    AvionId = reader.GetInt32(8),
                    AvionModelo = reader.GetString(9),
                    AvionMarca = reader.GetString(10),
                    CapacidadPasajeros = reader.GetInt32(11),

                    // Origen
                    OrigenId = reader.GetInt32(12),
                    OrigenNombre = reader.GetString(13),
                    OrigenCodigo = reader.GetString(14),
                    OrigenCiudad = reader.GetString(15),
                    OrigenPais = reader.GetString(16),

                    // Destino
                    DestinoId = reader.GetInt32(17),
                    DestinoNombre = reader.GetString(18),
                    DestinoCodigo = reader.GetString(19),
                    DestinoCiudad = reader.GetString(20),
                    DestinoPais = reader.GetString(21),

                    // Ruta
                    RutaId = reader.GetInt32(22),
                    DuracionMinutos = reader.GetInt32(23),

                    // Precios por clase
                    PrecioTurista = reader.IsDBNull(24) ? null : reader.GetDecimal(24),
                    PrecioEjecutiva = reader.IsDBNull(25) ? null : reader.GetDecimal(25),
                    BoletosDisponiblesTurista = reader.IsDBNull(26) ? null : reader.GetInt32(26),
                    BoletosDisponiblesEjecutiva = reader.IsDBNull(27) ? null : reader.GetInt32(27)
                };

                vuelos.Add(vuelo);
            }

            // Cerrar el reader antes de obtener tripulantes
            reader.Close();

            // Obtener tripulantes para cada vuelo
            foreach (var vuelo in vuelos)
            {
                vuelo.Tripulantes = await ObtenerTripulantesPorVuelo(connection, vuelo.Id);
            }

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
                INNER JOIN RolTripulacion rt ON mt.RolID = rt.ID
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
    }
}