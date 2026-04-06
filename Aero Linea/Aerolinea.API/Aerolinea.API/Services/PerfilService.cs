using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de perfil de usuario. Gestiona la consulta de datos personales,
    /// la actualizacion del numero de telefono y el cambio de contrasena
    /// con verificacion del hash actual almacenado.
    /// </summary>
    public class PerfilService
    {
        private readonly PerfilRepository _repository;

        /// <summary>
        /// Inicializa el servicio con el repositorio de perfil de usuario.
        /// </summary>
        public PerfilService(PerfilRepository repository)
        {
            _repository = repository;
        }

        /// <summary>
        /// Retorna los datos del perfil del usuario autenticado.
        /// Retorna null si el usuario no existe en el sistema.
        /// </summary>
        public async Task<PerfilDTO?> ObtenerPerfil(int usuarioId)
        {
            return await _repository.ObtenerPerfil(usuarioId);
        }

        /// <summary>
        /// Actualiza el numero de telefono del usuario. Valida que el campo no este vacio.
        /// Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.
        /// </summary>
        public async Task<(bool exito, string mensaje)> ActualizarTelefono(int usuarioId, string telefono)
        {
            if (string.IsNullOrWhiteSpace(telefono))
                return (false, "El teléfono no puede estar vacío.");

            bool ok = await _repository.ActualizarTelefono(usuarioId, telefono);
            return ok
                ? (true, "Teléfono actualizado correctamente.")
                : (false, "No se pudo actualizar el teléfono.");
        }

        /// <summary>
        /// Cambia la contrasena del usuario tras verificar que la contrasena actual sea correcta
        /// y que la nueva tenga al menos 8 caracteres. Hashea la nueva contrasena antes de guardarla.
        /// Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.
        /// </summary>
        public async Task<(bool exito, string mensaje)> CambiarContrasena(int usuarioId, CambiarContrasenaDTO dto)
        {
            if (dto.NuevaContrasena.Length < 8)
                return (false, "La contraseña debe tener al menos 8 caracteres.");

            string? hashActual = await _repository.ObtenerHashContrasena(usuarioId);
            if (hashActual == null)
                return (false, "Usuario no encontrado.");

            if (!PasswordHasher.Verify(dto.ContrasenaActual, hashActual))
                return (false, "La contraseña actual es incorrecta.");

            string nuevoHash = PasswordHasher.Hash(dto.NuevaContrasena);
            bool ok = await _repository.ActualizarContrasena(usuarioId, nuevoHash);
            return ok
                ? (true, "Contraseña actualizada correctamente.")
                : (false, "No se pudo actualizar la contraseña.");
        }
    }
}
