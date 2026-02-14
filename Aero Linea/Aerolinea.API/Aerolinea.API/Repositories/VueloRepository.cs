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

        public async Task<List<VueloDetalleDTO>> BuscarVuelos(int origenId, int destinoId, DateTime fecha)
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
                    r.DuracionEstimada
                    
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
                  
                ORDER BY v.HoraSalida";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@origenId", origenId);
            command.Parameters.AddWithValue("@destinoId", destinoId);
            command.Parameters.AddWithValue("@fecha", fecha.Date);

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

                    // Estado
                    EstadoId = reader.GetInt32(5),
                    Estado = reader.GetString(6),

                    // Avión
                    AvionId = reader.GetInt32(7),
                    AvionModelo = reader.GetString(8),
                    AvionMarca = reader.GetString(9),
                    CapacidadPasajeros = reader.GetInt32(10),

                    // Origen
                    OrigenId = reader.GetInt32(11),
                    OrigenNombre = reader.GetString(12),
                    OrigenCodigo = reader.GetString(13),
                    OrigenCiudad = reader.GetString(14),
                    OrigenPais = reader.GetString(15),

                    // Destino
                    DestinoId = reader.GetInt32(16),
                    DestinoNombre = reader.GetString(17),
                    DestinoCodigo = reader.GetString(18),
                    DestinoCiudad = reader.GetString(19),
                    DestinoPais = reader.GetString(20),

                    // Ruta
                    RutaId = reader.GetInt32(21),
                    DuracionMinutos = reader.GetInt32(22)
                });
            }

            return vuelos;
        }
    }
}