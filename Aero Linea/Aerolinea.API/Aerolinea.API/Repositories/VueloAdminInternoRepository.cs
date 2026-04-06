using Aerolinea.API.Data;
using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Repositories
{
    /// <summary>
    /// Repositorio interno de actualizacion de estados de vuelos. Ejecuta el proceso
    /// automatico que transiciona vuelos de 'A tiempo' (1) a 'En transcurso' (2) o
    /// 'Finalizado' (3) segun la hora actual en relacion con sus horas de salida y llegada.
    /// </summary>
    public class VueloAdminInternoRepository
    {
        private readonly DbConnectionFactory _connectionFactory;

        public VueloAdminInternoRepository(DbConnectionFactory connectionFactory)
        {
            _connectionFactory = connectionFactory;
        }

        /// <summary>
        /// Actualiza el estado de todos los vuelos cuya hora de salida o llegada ya ocurrio.
        /// Pasa a estado 2 (en transcurso) los vuelos que ya salieron pero no aterrizaron,
        /// y a estado 3 (finalizado) los vuelos cuya hora de llegada ya paso.
        /// Retorna una tupla con la cantidad de vuelos pasados a cada estado.
        /// </summary>
        public async Task<(int enTranscurso, int finalizados)> ActualizarEstadosVuelos()
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();
            using var transaction = connection.BeginTransaction();

            try
            {
                // 1. Estado 1 a 2: vuelos que ya salieron pero aún no aterrizan
                string queryEnTranscurso = @"
                    UPDATE Vuelo
                    SET EstadoID = 2
                    WHERE EstadoID = 1
                      AND DATEADD(MINUTE, DATEDIFF(MINUTE, 0, HoraSalida), CAST(Fecha AS DATETIME)) <= GETDATE()
                      AND DATEADD(MINUTE, DATEDIFF(MINUTE, 0, HoraLlegada), CAST(Fecha AS DATETIME)) > GETDATE()";

                int enTranscurso;
                using (var cmd = new SqlCommand(queryEnTranscurso, connection, transaction))
                    enTranscurso = await cmd.ExecuteNonQueryAsync();

                // 2. Estado 1 o 2 a 3: vuelos cuya hora de llegada ya pasó
                //    Cubre estado 1 también por si el job se saltó un ciclo
                string queryFinalizados = @"
                    UPDATE Vuelo
                    SET EstadoID = 3
                    WHERE EstadoID IN (1, 2)
                      AND DATEADD(MINUTE, DATEDIFF(MINUTE, 0, HoraLlegada), CAST(Fecha AS DATETIME)) <= GETDATE()";

                int finalizados;
                using (var cmd = new SqlCommand(queryFinalizados, connection, transaction))
                    finalizados = await cmd.ExecuteNonQueryAsync();

                transaction.Commit();
                return (enTranscurso, finalizados);
            }
            catch
            {
                transaction.Rollback();
                throw;
            }
        }
    }
}
