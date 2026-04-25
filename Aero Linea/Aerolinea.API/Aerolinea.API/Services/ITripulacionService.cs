using Aerolinea.API.DTOs;
using Aerolinea.API.Models;

namespace Aerolinea.API.Services
{
    public interface ITripulacionService
    {
        Task<List<TripulanteDTO>> ObtenerTodos(bool incluirInactivos = false);
        Task<TripulanteDTO?> ObtenerPorId(int id);
        Task<TripulanteDTO> Crear(CrearTripulanteDTO crearTripulanteDTO);
        Task<bool> Actualizar(int id, CrearTripulanteDTO actualizarTripulanteDto);
        Task<bool> Eliminar(int id);
        Task GuardarImagen(int tripulanteId, string imagenBase64);
        Task EliminarImagen(int tripulanteId);
        Task<List<RolTripulacion>> ObtenerRoles();
        Task<bool> CambiarEstado(int id, bool activo);
        Task<(int totalFuturos, List<string> numeros48h)> VerificarVuelosAsignados(int tripulanteId);
        Task<List<VueloActivoInfoDTO>> ObtenerVuelosAsignadosDetallados(int tripulanteId);
        Task<int> DesasignarDeFuturosVuelos(int tripulanteId, IEnumerable<int> vueloIds);
        Task<List<Tripulante>> ObtenerEquipoVuelo(int vueloId);
        Task AsignarTripulantesAVuelo(int vueloId, IEnumerable<int> tripulanteIds);
    }
}
