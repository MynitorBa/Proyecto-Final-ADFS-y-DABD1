using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class VueloService
    {
        private readonly VueloRepository _repository;

        public VueloService(VueloRepository repository)
        {
            _repository = repository;
        }

        public async Task<List<VueloDetalleDTO>> BuscarVuelos(BuscarVueloDTO dto, int? usuarioId)
        {
            // Guardar la búsqueda (usuarioId null = búsqueda anónima, TipoBusquedaID siempre 1)
            await _repository.GuardarBusqueda(
                origenId: dto.OrigenId,
                destinoId: dto.DestinoId,
                fechaSalida: dto.Fecha,
                cantidadPersonas: dto.CantidadPasajeros,
                usuarioId: usuarioId
            );

            // Obtener resultados con disponibilidad por clase
            var vuelos = await _repository.BuscarVuelos(
                dto.OrigenId,
                dto.DestinoId,
                dto.Fecha,
                dto.CantidadPasajeros,
                dto.ClaseId
            );

            // Filtro de precio en memoria
            if (dto.PrecioMinimo.HasValue || dto.PrecioMaximo.HasValue)
            {
                vuelos = vuelos.Where(v =>
                {
                    decimal? precio = dto.ClaseId == 1 ? v.PrecioTurista
                                    : dto.ClaseId == 2 ? v.PrecioEjecutiva
                                    : v.PrecioTurista.HasValue && v.PrecioEjecutiva.HasValue
                                        ? Math.Min(v.PrecioTurista.Value, v.PrecioEjecutiva.Value)
                                        : v.PrecioTurista ?? v.PrecioEjecutiva;

                    if (!precio.HasValue) return false;
                    if (dto.PrecioMinimo.HasValue && precio < dto.PrecioMinimo.Value) return false;
                    if (dto.PrecioMaximo.HasValue && precio > dto.PrecioMaximo.Value) return false;

                    return true;
                }).ToList();
            }

            return vuelos;
        }
    }
}