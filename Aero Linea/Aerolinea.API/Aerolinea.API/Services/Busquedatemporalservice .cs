using Aerolinea.API.Models;
using System.Collections.Concurrent;

namespace Aerolinea.API.Services
{
    public class BusquedaTemporalService
    {
        // Almacenamiento en memoria (en producción usarías Redis o similar)
        private static readonly ConcurrentDictionary<string, BusquedaVuelo> _busquedas = new();

        // Guardar búsqueda y retornar el ID
        public string GuardarBusqueda(BusquedaVuelo busqueda)
        {
            // Limpiar búsquedas antiguas (más de 1 hora)
            LimpiarBusquedasAntiguas();

            _busquedas.TryAdd(busqueda.Id, busqueda);
            return busqueda.Id;
        }

        // Obtener búsqueda por ID
        public BusquedaVuelo ObtenerBusqueda(string id)
        {
            _busquedas.TryGetValue(id, out var busqueda);
            return busqueda;
        }

        // Limpiar búsquedas de más de 1 hora
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