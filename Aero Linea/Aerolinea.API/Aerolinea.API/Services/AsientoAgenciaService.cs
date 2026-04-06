using Aerolinea.API.DTOs.Agencia;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de asientos para agencias. Gestiona la consulta y cambio de asientos
    /// en reservaciones realizadas por una agencia de viaje.
    /// </summary>
    public class AsientoAgenciaService
    {
        private readonly AsientoAgenciaRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de asientos de agencia.
        /// </summary>
        public AsientoAgenciaService(AsientoAgenciaRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna la lista de asientos asignados en una reservacion especifica
        /// perteneciente a la agencia indicada.
        /// </summary>
        public async Task<List<AsientosVueloAgenciaDTO>> ObtenerAsientosPorReservacion(int reservacionId, int agenciaId)
        {
            return await _repository.ObtenerAsientosPorReservacion(reservacionId, agenciaId);
        }

        /// <summary>
        /// Cambia el asiento de un boleto perteneciente a una reservacion de la agencia.
        /// Valida que el nuevo numero de asiento no sea vacio y lo convierte a mayusculas.
        /// </summary>
        public async Task CambiarAsiento(int boletoId, string nuevoAsiento, int agenciaId)
        {
            if (string.IsNullOrWhiteSpace(nuevoAsiento))
                throw new Exception("El asiento es obligatorio.");

            await _repository.CambiarAsiento(boletoId, nuevoAsiento.Trim().ToUpper(), agenciaId);
        }
    }
}
