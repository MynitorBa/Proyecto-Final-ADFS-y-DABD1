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

        public async Task<List<VueloDetalleDTO>> BuscarVuelos(BuscarVueloDTO dto)
        {
            var vuelos = await _repository.BuscarVuelos(dto.OrigenId, dto.DestinoId, dto.Fecha, dto.CantidadPasajeros);

            // Aplicar filtros de precio si están presentes
            if (dto.PrecioMinimo.HasValue || dto.PrecioMaximo.HasValue || dto.ClaseId.HasValue)
            {
                vuelos = vuelos.Where(v =>
                {
                    decimal? precioConsiderar = null;

                    // Si se especifica una clase, usar el precio de esa clase
                    if (dto.ClaseId.HasValue)
                    {
                        if (dto.ClaseId.Value == 1) // Turista
                        {
                            precioConsiderar = v.PrecioTurista;
                        }
                        else if (dto.ClaseId.Value == 2) // Ejecutiva
                        {
                            precioConsiderar = v.PrecioEjecutiva;
                        }
                    }
                    else
                    {
                        // Si no se especifica clase, usar el menor precio disponible
                        if (v.PrecioTurista.HasValue && v.PrecioEjecutiva.HasValue)
                        {
                            precioConsiderar = Math.Min(v.PrecioTurista.Value, v.PrecioEjecutiva.Value);
                        }
                        else
                        {
                            precioConsiderar = v.PrecioTurista ?? v.PrecioEjecutiva;
                        }
                    }

                    // Si no hay precio, filtrar el vuelo
                    if (!precioConsiderar.HasValue)
                        return false;

                    // Aplicar filtros de rango de precio
                    if (dto.PrecioMinimo.HasValue && precioConsiderar < dto.PrecioMinimo.Value)
                        return false;

                    if (dto.PrecioMaximo.HasValue && precioConsiderar > dto.PrecioMaximo.Value)
                        return false;

                    return true;
                }).ToList();
            }

            return vuelos;
        }
    }
}