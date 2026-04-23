using Aerolinea.API.DTOs;

namespace Aerolinea.API.Services
{
    public interface IVueloService
    {
        Task<List<VueloDetalleDTO>> BusquedaGeneral(string query);
        Task<ResultadoBusquedaDTO> BuscarVuelos(BuscarVueloDTO dto, int? usuarioId);
    }
}
