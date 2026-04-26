using Aerolinea.API.DTOs;

namespace Aerolinea.API.Repositories
{
    public interface IVueloRepository
    {
        Task<List<VueloDetalleDTO>> BusquedaGeneral(string query);
        Task GuardarBusqueda(int origenId, int destinoId, DateTime fechaSalida, int cantidadPersonas, int? usuarioId, int tipoBusquedaId = 1);
        Task<List<VueloDetalleDTO>> BuscarVuelos(int origenId, int destinoId, DateTime fecha, int cantidadPasajeros, int? claseId);
        Task<List<VueloConEscalaDTO>> BuscarVuelosConEscalas(int origenId, int destinoId, DateTime fecha, int cantidadPasajeros, int? claseId = null, int maxEscalas = 3);
    }
}
