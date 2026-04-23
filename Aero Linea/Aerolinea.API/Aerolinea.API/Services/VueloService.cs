using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de vuelos para usuarios. Gestiona la busqueda de vuelos directos y con escala,
    /// el registro de busquedas para metricas y la aplicacion de filtros de precio en memoria.
    /// </summary>
    public class VueloService : IVueloService
    {
        private readonly IVueloRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de vuelos.
        /// </summary>
        public VueloService(IVueloRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Realiza una busqueda general de vuelos por texto libre.
        /// Retorna una lista de vuelos cuyo numero, origen o destino coincidan con la consulta.
        /// </summary>
        public async Task<List<VueloDetalleDTO>> BusquedaGeneral(string query)
        {
            return await _repository.BusquedaGeneral(query);
        }

        /// <summary>
        /// Busca vuelos disponibles entre dos aeropuertos en una fecha y con los filtros indicados.
        /// Registra la busqueda en la base de datos para metricas. Retorna vuelos directos
        /// y con escala, ambos filtrados por rango de precio si se especifica.
        /// </summary>
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

            // Vuelos con escala
            var conEscala = await _repository.BuscarVuelosConEscalas(
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

        /// <summary>
        /// Filtra en memoria la lista de vuelos directos aplicando el rango de precio minimo
        /// y maximo del DTO. Selecciona el precio de la clase indicada o el menor precio disponible
        /// si no se especifica clase. Retorna la lista sin modificar si no hay filtros de precio.
        /// </summary>
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
