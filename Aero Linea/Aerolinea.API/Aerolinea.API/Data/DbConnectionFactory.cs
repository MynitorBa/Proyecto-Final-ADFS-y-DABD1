using Microsoft.Data.SqlClient;

namespace Aerolinea.API.Data
{
    /// <summary>
    /// Fabrica de conexiones a la base de datos. Lee la cadena de conexion desde la
    /// configuracion de la aplicacion y expone un metodo para obtener nuevas instancias
    /// de SqlConnection listas para usar. Se registra como Singleton en el contenedor
    /// de dependencias para ser inyectada en los repositorios que acceden a datos con Dapper.
    /// </summary>
    public class DbConnectionFactory
    {
        private readonly string _connectionString;

        /// <summary>
        /// Inicializa la fabrica de conexiones leyendo la cadena de conexion
        /// llamada DefaultConnection desde la configuracion de la aplicacion (appsettings.json).
        /// </summary>
        public DbConnectionFactory(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection");
        }

        /// <summary>
        /// Crea y retorna una nueva instancia de SqlConnection configurada con la cadena de
        /// conexion de la aplicacion. La conexion no se abre automaticamente; el llamador
        /// es responsable de abrirla y cerrarla usando un bloque using.
        /// </summary>
        public SqlConnection CreateConnection()
        {
            return new SqlConnection(_connectionString);
        }
    }
}
