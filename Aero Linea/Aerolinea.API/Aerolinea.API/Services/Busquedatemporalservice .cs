using Aerolinea.API.Models;
using System.Collections.Concurrent;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de busquedas temporales en memoria. Almacena el contexto de busqueda de vuelos
    /// de forma temporal usando un diccionario concurrente para soportar multiples solicitudes simultaneas.
    /// Las entradas expiradas (mayores a 1 hora) se eliminan automaticamente en cada escritura.
    /// </summary>
    public class BusquedaTemporalService
    {
        // Almacenamiento en memoria (en producción usarías Redis o similar)
        private static readonly ConcurrentDictionary<string, BusquedaVuelo> _busquedas = new();

        /// <summary>
        /// Guarda el contexto de una busqueda de vuelo en memoria y retorna su identificador unico.
        /// Antes de guardar, elimina todas las busquedas con mas de una hora de antiguedad.
        /// </summary>
        public string GuardarBusqueda(BusquedaVuelo busqueda)
        {
            // Limpiar búsquedas antiguas (más de 1 hora)
            LimpiarBusquedasAntiguas();

            _busquedas.TryAdd(busqueda.Id, busqueda);
            return busqueda.Id;
        }

        /// <summary>
        /// Recupera el contexto de una busqueda previamente guardada usando su identificador unico.
        /// Retorna null si el ID no existe o ya fue eliminado por expiracion.
        /// </summary>
        public BusquedaVuelo ObtenerBusqueda(string id)
        {
            _busquedas.TryGetValue(id, out var busqueda);
            return busqueda;
        }

        /// <summary>
        /// Elimina del diccionario todas las busquedas cuya fecha de creacion sea
        /// mayor a una hora respecto al tiempo actual en UTC.
        /// </summary>
        private void LimpiarBusquedasAntiguas()
        {
            var antiguedadMaxima = DateTime.UtcNow.AddHours(-1);
            var idsAEliminar = _busquedas
                .Where(x => x.Value.FechaCreacion < antiguedadMaxima)
                .Select(x => x.Key)
                .ToList();

            foreach (var id in idsAEliminar)
            {
                _busquedas.TryRemove(id, out _);
            }
        }
    }
}
