using Aerolinea.API.DTOs;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de agencias. Gestiona la logica de negocio para crear, consultar
    /// y administrar agencias de viaje, incluyendo asignacion de usuarios webservice,
    /// descuentos y estados de la agencia.
    /// </summary>
    public class AgenciaService
    {
        private readonly AgenciaRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de agencias.
        /// </summary>
        public AgenciaService(AgenciaRepository repository)
        {
            _repository = repository;
        }

        // Usado por el Admin para crear una agencia asignando cualquier usuario webservice.
        /// <summary>
        /// Crea una nueva agencia asignando el usuario webservice indicado en el DTO.
        /// Verifica que el usuario exista, tenga rol WebService y no tenga ya una agencia asignada.
        /// Uso exclusivo del administrador.
        /// </summary>
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
        /// <summary>
        /// Permite que un usuario con rol Webservice registre su propia agencia.
        /// Solo acepta nombre y correo; el ID del usuario se toma de la sesion activa.
        /// Un usuario Webservice solo puede tener una agencia registrada a la vez.
        /// </summary>
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

        /// <summary>
        /// Retorna la informacion de la agencia asociada al usuario Webservice autenticado.
        /// Retorna null si el usuario aun no tiene ninguna agencia registrada.
        /// </summary>
        public async Task<MiAgenciaDTO?> ObtenerMiAgencia(int usuarioId)
        {
            return await _repository.ObtenerAgenciaPorUsuarioId(usuarioId);
        }

        /// <summary>
        /// Retorna la lista completa de agencias registradas en el sistema,
        /// incluyendo datos ampliados para uso administrativo.
        /// </summary>
        public async Task<List<AgenciaAdminDTO>> ObtenerTodasAdmin()
            => await _repository.ObtenerTodasAdmin();

        /// <summary>
        /// Retorna la lista de usuarios con rol Webservice que aun no tienen
        /// ninguna agencia asignada. Util para el formulario de asignacion del admin.
        /// </summary>
        public async Task<List<UsuarioWebserviceDTO>> ObtenerWebserviceSinAgencia()
            => await _repository.ObtenerWebserviceSinAgencia();

        /// <summary>
        /// Asigna un usuario Webservice a una agencia existente. Verifica que el usuario
        /// exista, tenga rol Webservice y no este ya asignado a otra agencia.
        /// </summary>
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

        /// <summary>
        /// Actualiza el porcentaje de descuento de una agencia. El valor debe estar
        /// entre 0 y 100. Este descuento se aplica sobre el precio de los vuelos al buscar.
        /// </summary>
        public async Task ActualizarDescuento(int agenciaId, decimal descuento)
        {
            if (descuento < 0 || descuento > 100)
                throw new Exception("El descuento debe estar entre 0 y 100.");
            bool ok = await _repository.ActualizarDescuento(agenciaId, descuento);
            if (!ok) throw new Exception("No se encontró la agencia indicada.");
        }

        /// <summary>
        /// Actualiza el estado de una agencia. El valor del estado debe ser un ID valido
        /// entre 1 y 3 segun el catalogo de estados de agencia.
        /// </summary>
        public async Task ActualizarEstado(int agenciaId, int estadoId)
        {
            if (estadoId < 1 || estadoId > 3)
                throw new Exception("Estado no válido.");
            bool ok = await _repository.ActualizarEstado(agenciaId, estadoId);
            if (!ok) throw new Exception("No se encontró la agencia indicada.");
        }
    }
}
