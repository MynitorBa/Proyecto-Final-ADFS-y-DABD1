using Aerolinea.API.DTOs;
using Aerolinea.API.Helpers;
using Aerolinea.API.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Aerolinea.API.Controllers
{
    /// <summary>
    /// Controlador de correos electronicos. Gestiona el envio de comprobantes de reservacion,
    /// mensajes de contacto desde el formulario publico y suscripciones al boletin informativo.
    /// </summary>
    [ApiController]
    [Route("api")]
    public class EmailController : ControllerBase
    {
        private const string ADMIN_EMAIL = "distribuidorapine@gmail.com";

        private readonly GestionReservacionRepository _reservacionRepo;
        private readonly EmailHelper                  _emailHelper;

        /// <summary>
        /// Inicializa el controlador con el repositorio de reservaciones y el helper de correo.
        /// </summary>
        public EmailController(
            GestionReservacionRepository reservacionRepo,
            EmailHelper                  emailHelper)
        {
            _reservacionRepo = reservacionRepo;
            _emailHelper     = emailHelper;
        }

        // ══════════════════════════════════════════════════════════════════
        //  GET /api/reservaciones/{id}/correo — envía correo de reservación al usuario
        //  Solo roles 1 (Admin) y 2 (Cliente/Usuario registrado)
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Envia al correo del usuario el comprobante de una reservacion especifica.
        /// Solo accesible para roles Administrador (1) y Cliente (2). Verifica que la reservacion
        /// pertenezca al usuario autenticado antes de enviar el correo.
        /// </summary>
        [HttpGet("reservaciones/{id}/correo")]
        [Authorize]
        public async Task<IActionResult> EnviarCorreoReservacion(int id)
        {
            var usuarioId = SessionHelper.GetUsuarioId(HttpContext);
            var rolId = SessionHelper.GetRolId(HttpContext);

            if (usuarioId == null)
                return Unauthorized(new { mensaje = "No autenticado" });

            if (rolId != 1 && rolId != 2)
                return StatusCode(403, new { mensaje = "Acceso denegado" });

            try
            {
                // Obtener detalle completo de la reservación
                var reservacion = await _reservacionRepo.ObtenerReservacionPorId(id, usuarioId.Value);

                if (reservacion == null)
                    return NotFound(new { mensaje = "Reservación no encontrada o no tienes acceso" });

                // Generar HTML del correo
                string html = EmailTemplates.CorreoReservacion(reservacion);
                string asunto = $"✈ Broom AirLine — Comprobante de Reservación {reservacion.NoReservacion}";

                // Enviar al correo del usuario
                await _emailHelper.Enviar(reservacion.UsuarioEmail, asunto, html);

                return Ok(new { mensaje = "Correo enviado correctamente" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al enviar el correo: " + ex.Message });
            }
        }

        // ══════════════════════════════════════════════════════════════════
        //  POST /api/contacto — formulario de contacto (público)
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Recibe un mensaje del formulario de contacto publico y lo reenvía al correo
        /// del administrador. Requiere nombre, correo y mensaje como campos obligatorios.
        /// </summary>
        [HttpPost("contacto")]
        [AllowAnonymous]
        public async Task<IActionResult> Contacto([FromBody] ContactoDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Nombre) ||
                string.IsNullOrWhiteSpace(dto.Correo) ||
                string.IsNullOrWhiteSpace(dto.Mensaje))
            {
                return BadRequest(new { mensaje = "Nombre, correo y mensaje son obligatorios" });
            }

            try
            {
                string html = EmailTemplates.CorreoContacto(dto.Nombre, dto.Correo, dto.Asunto ?? "", dto.Mensaje);
                string asunto = "📩 Nuevo mensaje de contacto — " +
                    (string.IsNullOrWhiteSpace(dto.Asunto) ? "Sin asunto" : dto.Asunto);

                await _emailHelper.Enviar(ADMIN_EMAIL, asunto, html);

                return Ok(new { mensaje = "Mensaje enviado correctamente" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al enviar el mensaje: " + ex.Message });
            }
        }

        // ══════════════════════════════════════════════════════════════════
        //  POST /api/newsletter — suscripción al boletín (público)
        // ══════════════════════════════════════════════════════════════════
        /// <summary>
        /// Registra una suscripcion al boletin informativo y notifica al administrador por correo.
        /// Endpoint publico, valida que el correo tenga formato valido antes de procesar.
        /// </summary>
        [HttpPost("newsletter")]
        [AllowAnonymous]
        public async Task<IActionResult> Newsletter([FromBody] NewsletterDTO dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Correo) || !dto.Correo.Contains("@"))
            {
                return BadRequest(new { mensaje = "Correo inválido" });
            }

            try
            {
                string html = EmailTemplates.CorreoNewsletter(dto.Correo);
                string asunto = $"📬 Nueva suscripción al boletín — {dto.Correo}";

                await _emailHelper.Enviar(ADMIN_EMAIL, asunto, html);

                return Ok(new { mensaje = "Suscripción registrada correctamente" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al registrar suscripción: " + ex.Message });
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DTOs para los endpoints de contacto y newsletter
    // ══════════════════════════════════════════════════════════════════
    /// <summary>
    /// DTO para el formulario de contacto. Contiene los datos del remitente y el mensaje.
    /// </summary>
    public class ContactoDTO
    {
        public string Nombre { get; set; }
        public string Correo { get; set; }
        public string Asunto { get; set; }
        public string Mensaje { get; set; }
    }

    /// <summary>
    /// DTO para la suscripcion al boletin informativo. Contiene el correo del suscriptor.
    /// </summary>
    public class NewsletterDTO
    {
        public string Correo { get; set; }
    }
}
