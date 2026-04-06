using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio de rutas para agencias. Proporciona acceso de solo lectura
    /// al catalogo completo de rutas disponibles con informacion de ciudad, pais
    /// y duracion estimada de cada trayecto.
    /// </summary>
    public class RutaAgenciaRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public RutaAgenciaRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna la lista completa de rutas con ciudad y pais de origen y destino
        /// obtenidos a traves de los aeropuertos asociados a cada ruta.
        /// </summary>
        public async Task<List<RutaAgenciaDTO>> ObtenerTodasLasRutas()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            var query = @"
                SELECT
                    r.ID,
                    co.Nombre          AS CiudadOrigen,
                    po.Nombre          AS PaisOrigen,
                    cd.Nombre          AS CiudadDestino,
                    pd.Nombre          AS PaisDestino,
                    r.DuracionEstimada
                FROM Ruta r
                INNER JOIN Aeropuerto ao ON ao.ID = r.OrigenID
                INNER JOIN Aeropuerto ad ON ad.ID = r.DestinoID
                INNER JOIN Ciudad     co ON co.ID = ao.CiudadID
                INNER JOIN Ciudad     cd ON cd.ID = ad.CiudadID
                INNER JOIN Pais       po ON po.ID = co.PaisID
                INNER JOIN Pais       pd ON pd.ID = cd.PaisID";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var rutas = new List<RutaAgenciaDTO>();
            while (await reader.ReadAsync())
            {
                rutas.Add(new RutaAgenciaDTO
                {
                    ID = reader.GetInt32(0),
                    CiudadOrigen = reader.GetString(1),
                    PaisOrigen = reader.GetString(2),
                    CiudadDestino = reader.GetString(3),
                    PaisDestino = reader.GetString(4),
                    Duracion = reader.GetInt32(5)
                });
            }
            return rutas;
        }
    }
}
