using Aerolinea.API.Controllers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de asientos para usuarios registrados. Gestiona la consulta del mapa de asientos
    /// de un vuelo y el cambio de asiento en boletos pertenecientes al usuario autenticado.
    /// </summary>
    public class AsientoService
    {
        private readonly AsientoRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de asientos.
        /// </summary>
        public AsientoService(AsientoRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna el mapa de asientos de un vuelo especifico, indicando cuales estan
        /// disponibles y cuales ya han sido reservados por el usuario autenticado.
        /// </summary>
        public async Task<AsientosVueloDTO> ObtenerAsientosVuelo(int vueloId, int usuarioId)
        {
            return await _repository.ObtenerAsientosVuelo(vueloId, usuarioId);
        }

        /// <summary>
        /// Cambia el asiento asignado a un boleto del usuario. Valida que el nuevo asiento
        /// no sea vacio y lo normaliza a mayusculas antes de persistirlo.
        /// </summary>
        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int usuarioId)
        {
            if (string.IsNullOrWhiteSpace(nuevoAsiento))
                throw new Exception("El asiento no puede estar vacío.");

            await _repository.CambiarAsiento(boletoId, nuevoAsiento.Trim().ToUpper(), usuarioId);
        }
    }
}
