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

            // Se usa PaisRepository para obtener el PaisId
            // Luego se usa ese PaisId para buscar/crear la Ciudad
            // Pero en Usuario solo se guarda CiudadId
            // (la relacion Pais se obtiene via Ciudad -> Pais)
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
                CiudadId = ciudadId,
                RolID = 2  // Cliente por defecto
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
            bool usuarioExiste = await _repository.UsuarioExiste(dto.UsuarioId);
            if (!usuarioExiste)
                return (false, "El usuario no existe");

            bool rolExiste = await _repository.RolExiste(dto.NuevoRolId);
            if (!rolExiste)
                return (false, "El rol especificado no existe");

            bool actualizado = await _repository.ActualizarRol(dto.UsuarioId, dto.NuevoRolId);
            return actualizado
                ? (true, "Rol actualizado correctamente")
                : (false, "No se pudo actualizar el rol");
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