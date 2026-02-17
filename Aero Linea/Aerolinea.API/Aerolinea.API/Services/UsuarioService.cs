using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class UsuarioService
    {
        private readonly UsuarioRepository _repository;
        private readonly PaisRepository _paisRepository;
        private readonly CiudadRepository _ciudadRepository;
        private readonly DbConnectionFactory _connectionFactory;

        public UsuarioService(
            UsuarioRepository repository,
            PaisRepository paisRepository,
            CiudadRepository ciudadRepository,
            DbConnectionFactory connectionFactory)
        {
            _repository = repository;
            _paisRepository = paisRepository;
            _ciudadRepository = ciudadRepository;
            _connectionFactory = connectionFactory;
        }

        public async Task CrearUsuario(CrearUsuarioDTO dto)
        {
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            int paisId = await _paisRepository.ObtenerOCrearId(dto.Pais, connection);
            int ciudadId = await _ciudadRepository.ObtenerOCrearId(dto.Ciudad, paisId, connection);

            var usuario = new Usuario
            {
                Correo = dto.Correo,
                ContrasenaHash = PasswordHasher.Hash(dto.Contrasena),
                Pasaporte = dto.Pasaporte,
                Username = dto.Username,
                Nombre = dto.Nombre,
                Apellido = dto.Apellido,
                Telefono = dto.Telefono,
                FechaNacimiento = dto.FechaNacimiento,
                PaisId = paisId,
                CiudadId = ciudadId,
                RolID = 1
            };

            int usuarioId = await _repository.CrearUsuario(usuario);

            if (dto.Nacionalidades != null && dto.Nacionalidades.Count > 0)
                await _repository.AgregarNacionalidades(usuarioId, dto.Nacionalidades);
        }

        public async Task<RegisterConstraint> VerificarConstraints(CrearUsuarioDTO dto)
        {
            return await _repository.VerificarExistencia(dto.Correo, dto.Username, dto.Pasaporte);
        }

        public async Task<(bool exito, string mensaje)> CambiarRol(CambiarRolDTO dto)
        {
            // Validar que el usuario existe
            bool usuarioExiste = await _repository.UsuarioExiste(dto.UsuarioId);
            if (!usuarioExiste)
            {
                return (false, "El usuario no existe");
            }

            // Validar que el rol existe
            bool rolExiste = await _repository.RolExiste(dto.NuevoRolId);
            if (!rolExiste)
            {
                return (false, "El rol especificado no existe");
            }

            // Actualizar el rol
            bool actualizado = await _repository.ActualizarRol(dto.UsuarioId, dto.NuevoRolId);

            if (actualizado)
            {
                return (true, "Rol actualizado correctamente");
            }
            else
            {
                return (false, "No se pudo actualizar el rol");
            }
        }

        public async Task<List<object>> ObtenerTodos()
        {
            var usuarios = await _repository.ObtenerTodos();
            var resultado = new List<object>();

            foreach (var u in usuarios)
            {
                var rolNombre = await _repository.ObtenerNombreRol(u.RolID);
                resultado.Add(new
                {
                    id = u.Id,
                    nombre = u.Nombre,
                    apellido = u.Apellido,
                    correo = u.Correo,
                    username = u.Username,
                    rolId = u.RolID,
                    rolNombre = rolNombre ?? "Desconocido"
                });
            }

            return resultado;
        }
    }
}