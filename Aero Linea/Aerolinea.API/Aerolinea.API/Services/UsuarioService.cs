using Aerolinea.API.Data;
using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Models;
using Aerolinea.API.Repositories;

namespace Aerolinea.API.Services
{
    /// <summary>
    /// Servicio de usuarios. Gestiona el registro de nuevos usuarios con validaciones de formato,
    /// la verificacion de campos unicos, el cambio de rol por parte del administrador
    /// y la consulta del listado completo de usuarios del sistema.
    /// </summary>
    public class UsuarioService
    {
        private readonly UsuarioRepository   _repository;
        private readonly PaisRepository      _paisRepository;
        private readonly CiudadRepository    _ciudadRepository;
        private readonly DbConnectionFactory _connectionFactory;
        private readonly EmailHelper         _emailHelper;

        /// <summary>
        /// Inicializa el servicio con los repositorios de usuario, pais, ciudad,
        /// la fabrica de conexiones y el helper de correo.
        /// </summary>
        public UsuarioService(
            UsuarioRepository   repository,
            PaisRepository      paisRepository,
            CiudadRepository    ciudadRepository,
            DbConnectionFactory connectionFactory,
            EmailHelper         emailHelper)
        {
            _repository        = repository;
            _paisRepository    = paisRepository;
            _ciudadRepository  = ciudadRepository;
            _connectionFactory = connectionFactory;
            _emailHelper       = emailHelper;
        }

        /// <summary>
        /// Registra un nuevo usuario en el sistema. Valida formato de pasaporte y telefono,
        /// resuelve o crea el pais y ciudad correspondientes, hashea la contrasena y asigna
        /// el rol de cliente por defecto. Tambien guarda las nacionalidades si se proveen
        /// y envia un correo de bienvenida de forma no bloqueante.
        /// </summary>
        public async Task CrearUsuario(CrearUsuarioDTO dto)
        {
            // ══════════════════════════════════════════════════════════════
            //  VALIDACIONES DE FORMATO
            // ══════════════════════════════════════════════════════════════

            // Pasaporte: solo números
            if (string.IsNullOrWhiteSpace(dto.Pasaporte))
                throw new Exception("El número de pasaporte es obligatorio.");

            if (!dto.Pasaporte.All(char.IsDigit))
                throw new Exception("El número de pasaporte debe contener solo números.");

            // Teléfono: solo dígitos, espacios y el signo +
            if (string.IsNullOrWhiteSpace(dto.Telefono))
                throw new Exception("El número de teléfono es obligatorio.");

            string telefonoLimpio = dto.Telefono.Replace(" ", "").Replace("+", "");
            if (!telefonoLimpio.All(char.IsDigit))
                throw new Exception("El número de teléfono debe contener solo números.");

            // ══════════════════════════════════════════════════════════════

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
                CiudadId = ciudadId,
                RolID = 1  // Cliente por defecto
            };

            int usuarioId = await _repository.CrearUsuario(usuario);

            if (dto.Nacionalidades != null && dto.Nacionalidades.Count > 0)
                await _repository.AgregarNacionalidades(usuarioId, dto.Nacionalidades);

            // ══════════════════════════════════════════════════════════════
            //  ENVIAR CORREO DE BIENVENIDA al nuevo usuario
            //  (no bloquea el registro si falla el envío)
            // ══════════════════════════════════════════════════════════════
            try
            {
                string html = EmailTemplates.CorreoBienvenida(
                    dto.Nombre,
                    dto.Apellido,
                    dto.Username,
                    dto.Correo,
                    dto.Pasaporte,
                    dto.Telefono,
                    dto.Pais,
                    dto.Ciudad,
                    dto.FechaNacimiento.ToString("yyyy-MM-dd"),
                    dto.Nacionalidades ?? new List<string>()
                );

                string asunto = "Bienvenido a Broom AirLine - Cuenta creada exitosamente";
                await _emailHelper.Enviar(dto.Correo, asunto, html);
            }
            catch (Exception ex)
            {
                // Log del error pero no interrumpir el registro
                Console.WriteLine($"[WARN] No se pudo enviar correo de bienvenida a {dto.Correo}: {ex.Message}");
            }
        }

        /// <summary>
        /// Verifica si ya existe algun usuario con el mismo correo, nombre de usuario o pasaporte.
        /// Retorna un objeto con los campos que generarian conflicto de unicidad.
        /// </summary>
        public async Task<RegisterConstraint> VerificarConstraints(CrearUsuarioDTO dto)
        {
            return await _repository.VerificarExistencia(dto.Correo, dto.Username, dto.Pasaporte);
        }

        /// <summary>
        /// Cambia el rol de un usuario existente. Verifica que tanto el usuario como el rol
        /// indicados existan antes de aplicar el cambio. Retorna una tupla con resultado y mensaje.
        /// </summary>
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

        /// <summary>
        /// Retorna la lista de todos los usuarios registrados en el sistema como objetos anonimos,
        /// incluyendo ID, nombre, apellido, correo, username, ID de rol y nombre del rol.
        /// </summary>
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
