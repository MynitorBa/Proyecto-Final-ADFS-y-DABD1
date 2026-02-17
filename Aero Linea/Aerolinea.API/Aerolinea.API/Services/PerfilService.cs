using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    public class PerfilService
    {
        private readonly PerfilRepository _repository;

        public PerfilService(PerfilRepository repository)
        {
            _repository = repository;
        }

        public async Task<PerfilDTO?> ObtenerPerfil(int usuarioId)
        {
            return await _repository.ObtenerPerfil(usuarioId);
        }

        public async Task<(bool exito, string mensaje)> ActualizarTelefono(int usuarioId, string telefono)
        {
            if (string.IsNullOrWhiteSpace(telefono))
                return (false, "El teléfono no puede estar vacío.");

            bool ok = await _repository.ActualizarTelefono(usuarioId, telefono);
            return ok
                ? (true, "Teléfono actualizado correctamente.")
                : (false, "No se pudo actualizar el teléfono.");
        }

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