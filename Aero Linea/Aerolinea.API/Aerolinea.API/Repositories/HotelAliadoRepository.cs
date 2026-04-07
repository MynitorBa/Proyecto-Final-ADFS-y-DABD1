using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio que consulta los hoteles aliados activos registrados en la base de datos.
    /// </summary>
    public class HotelAliadoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public HotelAliadoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Retorna todos los hoteles aliados activos con su URL y TokenHASH
        /// para que el service pueda consultarlos dinamicamente.
        /// </summary>
        public async Task<List<HotelAliadoConexionDTO>> ObtenerHotelesActivos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            string query = @"
                SELECT h.ID, h.Nombre, h.URL, h.TokenHASH
                FROM HotelAliado h
                JOIN EstadoAliado e ON h.EstadoID = e.ID
                WHERE LOWER(TRIM(e.Estado)) = 'activo'";

            var hoteles = new List<HotelAliadoConexionDTO>();

            using var cmd = new SqlCommand(query, connection);
            using var reader = await cmd.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                hoteles.Add(new HotelAliadoConexionDTO
                {
                    Id = reader.GetInt32(0),
                    Nombre = reader.GetString(1),
                    Url = reader.GetString(2),
                    TokenHash = reader.GetString(3)
                });
            }

            return hoteles;
        }
    }
}