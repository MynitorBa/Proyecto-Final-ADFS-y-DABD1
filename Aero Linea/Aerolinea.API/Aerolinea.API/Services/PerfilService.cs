using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de perfil de usuario. Gestiona la consulta de datos personales,
    /// la actualizacion del numero de telefono y el cambio de contrasena
    /// con verificacion del hash actual almacenado.
    /// Envia correos de notificacion al usuario tras cada actualizacion exitosa.
    /// </summary>
    public class PerfilService
    {
        private readonly PerfilRepository _repository;
        private readonly EmailHelper      _email;

        /// <summary>
        /// Inicializa el servicio con el repositorio de perfil y el helper de correo.
        /// </summary>
        public PerfilService(PerfilRepository repository, EmailHelper email)
        {
            _repository = repository;
            _email      = email;
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
        /// Envia correo de notificacion al usuario tras el cambio exitoso.
        /// Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.
        /// </summary>
        public async Task<(bool exito, string mensaje)> ActualizarTelefono(int usuarioId, string telefono)
        {
            if (string.IsNullOrWhiteSpace(telefono))
                return (false, "El teléfono no puede estar vacío.");

            bool ok = await _repository.ActualizarTelefono(usuarioId, telefono);
            if (ok)
            {
                var perfil = await _repository.ObtenerPerfil(usuarioId);
                if (perfil != null)
                {
                    _ = _email.Enviar(
                        perfil.Correo,
                        "Actualización de Perfil — Broom AirLine",
                        EmailTemplates.CorreoActualizacionPerfil(perfil.Nombre, "Teléfono"));
                }
            }
            return ok
                ? (true, "Teléfono actualizado correctamente.")
                : (false, "No se pudo actualizar el teléfono.");
        }

        /// <summary>
        /// Cambia la contrasena del usuario tras verificar que la contrasena actual sea correcta
        /// y que la nueva tenga al menos 8 caracteres, una mayuscula, un numero y un caracter especial.
        /// Hashea la nueva contrasena antes de guardarla.
        /// Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.
        /// </summary>
        public async Task<(bool exito, string mensaje)> CambiarContrasena(int usuarioId, CambiarContrasenaDTO dto)
        {
            if (dto.NuevaContrasena.Length < 8)
                return (false, "La contraseña debe tener al menos 8 caracteres.");

            if (!System.Text.RegularExpressions.Regex.IsMatch(dto.NuevaContrasena, "[A-Z]"))
                return (false, "La contraseña debe contener al menos una letra mayúscula.");

            if (!System.Text.RegularExpressions.Regex.IsMatch(dto.NuevaContrasena, "[0-9]"))
                return (false, "La contraseña debe contener al menos un número.");

            if (!System.Text.RegularExpressions.Regex.IsMatch(dto.NuevaContrasena, "[^A-Za-z0-9]"))
                return (false, "La contraseña debe contener al menos un carácter especial (ej. #, @, !, $).");

            string? hashActual = await _repository.ObtenerHashContrasena(usuarioId);
            if (hashActual == null)
                return (false, "Usuario no encontrado.");

            if (!PasswordHasher.Verify(dto.ContrasenaActual, hashActual))
                return (false, "La contraseña actual es incorrecta.");

            string nuevoHash = PasswordHasher.Hash(dto.NuevaContrasena);
            bool ok = await _repository.ActualizarContrasena(usuarioId, nuevoHash);
            if (ok)
            {
                var perfil = await _repository.ObtenerPerfil(usuarioId);
                if (perfil != null)
                {
                    _ = _email.Enviar(
                        perfil.Correo,
                        "Actualización de Perfil — Broom AirLine",
                        EmailTemplates.CorreoActualizacionPerfil(perfil.Nombre, "Contraseña"));
                }
            }
            return ok
                ? (true, "Contraseña actualizada correctamente.")
                : (false, "No se pudo actualizar la contraseña.");
        }

        /// <summary>
        /// Actualiza los datos personales del usuario: nombre, apellido, username, pasaporte,
        /// fecha de nacimiento, pais y ciudad. Valida unicidad de username y pasaporte.
        /// Envia correo de notificacion al usuario tras el cambio exitoso.
        /// </summary>
        public async Task<(bool exito, string mensaje)> ActualizarDatosPersonales(
            int usuarioId, ActualizarDatosPersonalesDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Nombre))    return (false, "El nombre es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Apellido))  return (false, "El apellido es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Username))  return (false, "El username es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Pasaporte)) return (false, "El pasaporte es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Pais))      return (false, "El país es obligatorio.");
            if (string.IsNullOrWhiteSpace(dto.Ciudad))    return (false, "La ciudad es obligatoria.");

            if (await _repository.ExisteUsername(dto.Username.Trim(), usuarioId))
                return (false, "Ese username ya está en uso por otro usuario.");

            if (await _repository.ExistePasaporte(dto.Pasaporte.Trim(), usuarioId))
                return (false, "Ese pasaporte ya está registrado por otro usuario.");

            bool ok = await _repository.ActualizarDatosPersonales(usuarioId, dto);
            if (ok)
            {
                var perfil = await _repository.ObtenerPerfil(usuarioId);
                if (perfil != null)
                {
                    _ = _email.Enviar(
                        perfil.Correo,
                        "Actualización de Perfil — Broom AirLine",
                        EmailTemplates.CorreoActualizacionPerfil(perfil.Nombre, "datos personales"));
                }
            }
            return ok
                ? (true, "Datos personales actualizados correctamente.")
                : (false, "No se pudieron actualizar los datos personales.");
        }

        /// <summary>
        /// Actualiza el correo electronico del usuario tras validar el formato y la unicidad.
        /// Retorna una tupla con un indicador de exito y un mensaje descriptivo del resultado.
        /// </summary>
        public async Task<(bool exito, string mensaje)> ActualizarCorreo(int usuarioId, string nuevoCorreo)
        {
            if (string.IsNullOrWhiteSpace(nuevoCorreo))
                return (false, "El correo no puede estar vacío.");

            if (!System.Text.RegularExpressions.Regex.IsMatch(nuevoCorreo, @"^[^@\s]+@[^@\s]+\.[^@\s]+$"))
                return (false, "El formato del correo no es válido.");

            if (await _repository.ExisteCorreo(nuevoCorreo.Trim(), usuarioId))
                return (false, "Ese correo ya está registrado por otro usuario.");

            // Guardar correo anterior antes de actualizar
            string? correoAnterior = await _repository.ObtenerCorreo(usuarioId);
            var perfil = await _repository.ObtenerPerfil(usuarioId);

            bool ok = await _repository.ActualizarCorreo(usuarioId, nuevoCorreo.Trim());
            if (ok && perfil != null)
            {
                // Notificar al correo anterior (ya no podra usarlo para login)
                if (correoAnterior != null)
                    _ = _email.Enviar(
                        correoAnterior,
                        "Actualización de Perfil — Broom AirLine",
                        EmailTemplates.CorreoActualizacionPerfil(
                            perfil.Nombre, "Correo electrónico", correoAnterior));

                // Notificar al nuevo correo confirmando el cambio
                _ = _email.Enviar(
                    nuevoCorreo.Trim(),
                    "Actualización de Perfil — Broom AirLine",
                    EmailTemplates.CorreoActualizacionPerfil(perfil.Nombre, "Correo electrónico", correoAnterior));
            }
            return ok
                ? (true, "Correo actualizado correctamente.")
                : (false, "No se pudo actualizar el correo.");
        }
    }
}
