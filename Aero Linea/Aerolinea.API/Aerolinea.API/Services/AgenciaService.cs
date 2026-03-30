using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class AgenciaService
    {
        private readonly AgenciaRepository _repository;

        public AgenciaService(AgenciaRepository repository)
        {
            _repository = repository;
        }

        public async Task<AgenciaResponseDTO> CrearAgencia(CrearAgenciaDTO dto)
        {
            // 1. El usuario debe existir y ser rol webservice (rol 3)
            int rolID = await _repository.ObtenerRolUsuario(dto.UsuarioWebID);
            if (rolID == 0)
                throw new Exception("El usuario no existe.");
            if (rolID != 3)
                throw new Exception("El usuario debe tener rol WebService.");

            // 2. Un webservice solo puede tener una agencia
            bool yaExiste = await _repository.UsuarioYaTieneAgencia(dto.UsuarioWebID);
            if (yaExiste)
                throw new Exception("El usuario WebService ya tiene una agencia asignada.");

            // 3. Crear
            return await _repository.CrearAgencia(dto);
        }
    }
}