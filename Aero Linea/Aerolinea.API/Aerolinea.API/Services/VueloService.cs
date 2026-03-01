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

        public async Task<ResultadoBusquedaDTO> BuscarVuelos(BuscarVueloDTO dto, int? usuarioId)
        {
            // Guardar búsqueda
            await _repository.GuardarBusqueda(
                origenId: dto.OrigenId,
                destinoId: dto.DestinoId,
                fechaSalida: dto.Fecha,
                cantidadPersonas: dto.CantidadPasajeros,
                usuarioId: usuarioId
            );

            //  Vuelos directos 
            var directos = await _repository.BuscarVuelos(
                dto.OrigenId, dto.DestinoId,
                dto.Fecha, dto.CantidadPasajeros, dto.ClaseId);

            // Filtro de precio en memoria para directos
            directos = AplicarFiltroPrecio(directos, dto);

            // Vuelos con 1 escala 
            var conEscala = await _repository.BuscarVuelosConEscala(
                dto.OrigenId, dto.DestinoId,
                dto.Fecha, dto.CantidadPasajeros, dto.ClaseId);

            // Filtro de precio para escalas (sobre el precio total)
            if (dto.PrecioMinimo.HasValue || dto.PrecioMaximo.HasValue)
            {
                conEscala = conEscala.Where(v =>
                {
                    decimal? precio = dto.ClaseId == 1 ? v.PrecioTuristaTotal
                                    : dto.ClaseId == 2 ? v.PrecioEjecutivaTotal
                                    : v.PrecioTuristaTotal.HasValue && v.PrecioEjecutivaTotal.HasValue
                                        ? Math.Min(v.PrecioTuristaTotal.Value, v.PrecioEjecutivaTotal.Value)
                                        : v.PrecioTuristaTotal ?? v.PrecioEjecutivaTotal;

                    if (!precio.HasValue) return false;
                    if (dto.PrecioMinimo.HasValue && precio < dto.PrecioMinimo.Value) return false;
                    if (dto.PrecioMaximo.HasValue && precio > dto.PrecioMaximo.Value) return false;
                    return true;
                }).ToList();
            }

            return new ResultadoBusquedaDTO
            {
                Directos = directos,
                ConEscala = conEscala
            };
        }

        
        private List<VueloDetalleDTO> AplicarFiltroPrecio(List<VueloDetalleDTO> vuelos, BuscarVueloDTO dto)
        {
            if (!dto.PrecioMinimo.HasValue && !dto.PrecioMaximo.HasValue)
                return vuelos;

            return vuelos.Where(v =>
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
    }
}