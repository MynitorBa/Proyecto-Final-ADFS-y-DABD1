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

        // Usado por el Admin para crear una agencia asignando cualquier usuario webservice.
        public async Task<AgenciaResponseDTO> CrearAgencia(CrearAgenciaDTO dto)
        {
            int rolID = await _repository.ObtenerRolUsuario(dto.UsuarioWebID);
            if (rolID == 0)
                throw new Exception("El usuario no existe.");
            if (rolID != 3)
                throw new Exception("El usuario debe tener rol WebService.");

            bool yaExiste = await _repository.UsuarioYaTieneAgencia(dto.UsuarioWebID);
            if (yaExiste)
                throw new Exception("El usuario WebService ya tiene una agencia asignada.");

            return await _repository.CrearAgencia(dto);
        }

        // Usado por el propio usuario Webservice para registrar su agencia.
        // Solo puede pasar Nombre y Correo; el UsuarioWebID viene de la sesión.
        public async Task<AgenciaResponseDTO> CrearAgenciaWebservice(int usuarioId, CrearAgenciaWebserviceDTO dto)
        {
            bool yaExiste = await _repository.UsuarioYaTieneAgencia(usuarioId);
            if (yaExiste)
                throw new Exception("Ya tienes una agencia registrada. Solo se permite una por cuenta Webservice.");

            // Construimos el DTO completo con los valores predeterminados
            var dtoCompleto = new CrearAgenciaDTO
            {
                Nombre = dto.Nombre,
                Correo = dto.Correo,
                UsuarioWebID = usuarioId,
                PorcentajeDescuento = 0   // El admin lo asigna después
            };

            return await _repository.CrearAgencia(dtoCompleto);
        }

        // Devuelve la agencia del usuario Webservice autenticado, o null si aún no tiene.
        public async Task<MiAgenciaDTO?> ObtenerMiAgencia(int usuarioId)
        {
            return await _repository.ObtenerAgenciaPorUsuarioId(usuarioId);
        }

        // ── Admin ─────────────────────────────────────────────────────────────

        public async Task<List<AgenciaAdminDTO>> ObtenerTodasAdmin()
            => await _repository.ObtenerTodasAdmin();

        public async Task<List<UsuarioWebserviceDTO>> ObtenerWebserviceSinAgencia()
            => await _repository.ObtenerWebserviceSinAgencia();

        public async Task AsignarUsuario(int agenciaId, int usuarioId)
        {
            // Verificar que el usuario existe y es Webservice
            int rolId = await _repository.ObtenerRolUsuario(usuarioId);
            if (rolId == 0) throw new Exception("El usuario no existe.");
            if (rolId != 3) throw new Exception("El usuario debe tener rol Webservice.");

            // Verificar que no tenga ya otra agencia
            bool yaAsignado = await _repository.UsuarioYaTieneAgencia(usuarioId);
            if (yaAsignado) throw new Exception("Ese usuario ya está asignado a otra agencia.");

            bool ok = await _repository.AsignarUsuarioAAgencia(agenciaId, usuarioId);
            if (!ok) throw new Exception("No se encontró la agencia indicada.");
        }

        public async Task ActualizarDescuento(int agenciaId, decimal descuento)
        {
            if (descuento < 0 || descuento > 100)
                throw new Exception("El descuento debe estar entre 0 y 100.");
            bool ok = await _repository.ActualizarDescuento(agenciaId, descuento);
            if (!ok) throw new Exception("No se encontró la agencia indicada.");
        }

        public async Task ActualizarEstado(int agenciaId, int estadoId)
        {
            if (estadoId < 1 || estadoId > 3)
                throw new Exception("Estado no válido.");
            bool ok = await _repository.ActualizarEstado(agenciaId, estadoId);
            if (!ok) throw new Exception("No se encontró la agencia indicada.");
        }
    }
}