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
            // Obtener o crear los IDs de Pais y Ciudad
            using var connection = _connectionFactory.CreateConnection();
            await connection.OpenAsync();

            int paisId = await _paisRepository.ObtenerOCrearId(dto.Pais, connection);
            int ciudadId = await _ciudadRepository.ObtenerOCrearId(dto.Ciudad, paisId, connection);

            // Crear el usuario con los IDs
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
                RolID = dto.RolID
            };

            int usuarioId = await _repository.CrearUsuario(usuario);

            // Agregar nacionalidades si hay
            if (dto.Nacionalidades != null && dto.Nacionalidades.Count > 0)
                await _repository.AgregarNacionalidades(usuarioId, dto.Nacionalidades);
        }

        public async Task<RegisterConstraint> VerificarConstraints(CrearUsuarioDTO dto)
        {
            return await _repository.VerificarExistencia(dto.Correo, dto.Username, dto.Pasaporte);
        }
    }
}