using Aerolinea.API.Models.DTOs;

namespace Aerolinea.API.Repositories
{
    public interface IRutaRepository
    {
        Task<List<RutaDTO>> ObtenerTodas();
        Task<bool> ActualizarDuracion(int rutaId, int minutos);
        Task<(int duracion, string? tzOrigen, string? tzDestino)> ObtenerInfoRuta(int origenId, int destinoId);
        Task<bool> ExisteRuta(int origenId, int destinoId);
        Task<int> CrearRuta(int origenId, int destinoId, int duracionEstimada);
        Task<bool> TieneReservacionesActivas(int rutaId);
        Task<bool> DesactivarRuta(int rutaId);
        Task<bool> ActivarRuta(int rutaId);
        Task<(string origenCodigo, string origenCiudad, string destinoCodigo, string destinoCiudad)> ObtenerDescripcionRuta(int rutaId);
        Task<List<(string email, string nombreContacto, string nombreAgencia)>> ObtenerEmailsAgencias();
        Task<List<(string NumeroVuelo, string FechaSalida, string HoraSalida)>> ObtenerVuelosFuturosPorRuta(int rutaId);
    }
}
