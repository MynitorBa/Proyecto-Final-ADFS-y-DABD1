package org.example.services;

import org.example.dtos.UsuarioAdminDTO;
import org.example.dtos.UsuarioPerfilResponseDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.EmailHelper;
import org.example.helpers.PasswordHelper;
import org.example.repositories.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository             usuarioRepository             = new UsuarioRepository();
    private final PaisRepository                paisRepository                = new PaisRepository();
    private final CiudadRepository              ciudadRepository              = new CiudadRepository();
    private final NacionalidadRepository        nacionalidadRepository        = new NacionalidadRepository();
    private final UsuarioNacionalidadRepository usuarioNacionalidadRepository = new UsuarioNacionalidadRepository();

    // ─────────────────────── Validar disponibilidad ────────────────────────

    public UsuarioValidacionResponseDTO validarDisponibilidad(UsuarioValidacionRequestDTO request) {
        boolean usernameExiste  = usuarioRepository.existeUsername(request.getUsername());
        boolean correoExiste    = usuarioRepository.existeCorreo(request.getCorreo());
        boolean pasaporteExiste = usuarioRepository.existePasaporte(request.getPasaporte());

        return new UsuarioValidacionResponseDTO(usernameExiste, correoExiste, pasaporteExiste);
    }

    // ─────────────────────── Registrar usuario ─────────────────────────────

    public int registrarUsuario(UsuarioValidacionRequestDTO request) {

        UsuarioValidacionResponseDTO validacion = validarDisponibilidad(request);
        if (validacion.isUsernameExiste() || validacion.isCorreoExiste() || validacion.isPasaporteExiste()) {
            throw new CamposDuplicadosException(validacion);
        }

        int paisId   = paisRepository.buscarOCrearPorNombre(request.getPais());
        int ciudadId = ciudadRepository.buscarOCrearPorNombre(request.getCiudad(), paisId);

        String contrasenaHasheada = PasswordHelper.hashear(request.getContrasena());
        Date   fechaNacimiento    = Date.valueOf(LocalDate.parse(request.getFechaNacimiento()));

        int nuevoUsuarioId = usuarioRepository.crearUsuario(
                request.getCorreo(),
                contrasenaHasheada,
                request.getPasaporte(),
                request.getUsername(),
                request.getNombre(),
                request.getApellido(),
                request.getTelefono(),
                fechaNacimiento,
                ciudadId
        );

        if (request.getNacionalidades() != null && !request.getNacionalidades().isEmpty()) {
            List<Integer> nacionalidadIds = new ArrayList<>();
            for (String nombreNac : request.getNacionalidades()) {
                nacionalidadIds.add(nacionalidadRepository.buscarOCrearPorNombre(nombreNac));
            }
            usuarioNacionalidadRepository.asignarNacionalidades(nuevoUsuarioId, nacionalidadIds);
        }

        // ══════════════════════════════════════════════════════════════════
        // ENVIAR CORREO DE BIENVENIDA
        // ══════════════════════════════════════════════════════════════════
        try {
            String html = construirCorreoBienvenida(
                    request.getNombre(),
                    request.getApellido(),
                    request.getUsername(),
                    request.getCorreo(),
                    request.getContrasena(),
                    request.getTelefono(),
                    request.getPais(),
                    request.getCiudad()
            );
            EmailHelper.enviar(
                    request.getCorreo(),
                    "\u00A1Bienvenido a Miku Inn, " + request.getNombre() + "! \uD83C\uDFE8",
                    html
            );
        } catch (Exception e) {
            // No impedir el registro si el correo falla
            System.err.println("\u26A0 No se pudo enviar correo de bienvenida a "
                    + request.getCorreo() + ": " + e.getMessage());
        }

        return nuevoUsuarioId;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HTML del correo de bienvenida
    // ═══════════════════════════════════════════════════════════════════════

    private String construirCorreoBienvenida(
            String nombre, String apellido, String username,
            String correo, String contrasena, String telefono,
            String pais, String ciudad
    ) {
        String contrasenaMask = enmascararContrasena(contrasena);

        return "<!DOCTYPE html>"
                + "<html lang=\"es\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin:0;padding:0;background-color:#0f172a;font-family:'Segoe UI',Roboto,Arial,sans-serif;\">"
                + "<div style=\"max-width:600px;margin:0 auto;padding:40px 20px;\">"

                // ── Card principal ──
                + "<div style=\"background:linear-gradient(135deg,#1e293b 0%,#334155 100%);border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.1);box-shadow:0 20px 60px rgba(0,0,0,0.4);\">"

                // ── Header con gradiente ──
                + "<div style=\"background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:40px 30px;text-align:center;\">"
                + "<h1 style=\"margin:0;font-size:28px;color:#ffffff;font-weight:700;\">\uD83C\uDFE8 Miku Inn</h1>"
                + "<p style=\"margin:8px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;\">Tu aventura comienza aqu\u00ED</p>"
                + "</div>"

                // ── Contenido ──
                + "<div style=\"padding:35px 30px;\">"

                // Saludo
                + "<h2 style=\"margin:0 0 8px;font-size:22px;color:#f8fafc;font-weight:700;\">"
                + "\u00A1Bienvenido, " + nombre + "! \uD83C\uDF89</h2>"
                + "<p style=\"margin:0 0 25px;font-size:15px;color:#94a3b8;line-height:1.6;\">"
                + "Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesi\u00F3n y comenzar a reservar experiencias inolvidables."
                + "</p>"

                // ── Datos de la cuenta ──
                + "<div style=\"background:rgba(15,23,42,0.6);border:1px solid rgba(102,126,234,0.3);border-radius:12px;padding:20px 24px;margin-bottom:20px;\">"
                + "<h3 style=\"margin:0 0 16px;font-size:14px;color:#667eea;font-weight:700;text-transform:uppercase;letter-spacing:1px;\">"
                + "\uD83D\uDCCB Datos de tu cuenta</h3>"
                + "<table style=\"width:100%;border-collapse:collapse;\">"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;width:140px;\">Nombre completo</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;font-weight:600;\">" + nombre + " " + apellido + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;\">Usuario</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#667eea;font-weight:700;\">" + username + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;\">Correo</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;\">" + correo + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;\">Contrase\u00F1a</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;font-family:monospace;\">" + contrasenaMask + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;\">Tel\u00E9fono</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;\">" + telefono + "</td>"
                + "</tr>"

                + "<tr>"
                + "<td style=\"padding:8px 0;font-size:13px;color:#64748b;\">Ubicaci\u00F3n</td>"
                + "<td style=\"padding:8px 0;font-size:14px;color:#f1f5f9;\">" + ciudad + ", " + pais + "</td>"
                + "</tr>"

                + "</table>"
                + "</div>"

                // ── Consejos de seguridad ──
                + "<div style=\"background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.25);border-radius:12px;padding:20px 24px;margin-bottom:25px;\">"
                + "<h3 style=\"margin:0 0 12px;font-size:14px;color:#ef4444;font-weight:700;\">"
                + "\uD83D\uDD12 Consejos de Seguridad</h3>"
                + "<ul style=\"margin:0;padding:0 0 0 18px;color:#fca5a5;font-size:13px;line-height:2;\">"
                + "<li>No compartas tu contrase\u00F1a con nadie.</li>"
                + "<li>Miku Inn nunca te pedir\u00E1 tu contrase\u00F1a por correo o tel\u00E9fono.</li>"
                + "<li>Si no reconoces esta cuenta, cont\u00E1ctanos de inmediato.</li>"
                + "<li>Te recomendamos cambiar tu contrase\u00F1a peri\u00F3dicamente.</li>"
                + "<li>Usa una contrase\u00F1a \u00FAnica que no utilices en otros sitios.</li>"
                + "</ul>"
                + "</div>"

                // ── Bot\u00F3n iniciar sesi\u00F3n ──
                + "<div style=\"text-align:center;margin-bottom:10px;\">"
                + "<a href=\"http://localhost:5173\" "
                + "style=\"display:inline-block;padding:14px 40px;background:linear-gradient(135deg,#667eea,#764ba2);color:#ffffff;text-decoration:none;border-radius:10px;font-size:15px;font-weight:700;box-shadow:0 4px 20px rgba(102,126,234,0.4);\">"
                + "Iniciar Sesi\u00F3n \u2192</a>"
                + "</div>"

                + "</div>"

                // ── Footer ──
                + "<div style=\"padding:20px 30px;border-top:1px solid rgba(255,255,255,0.06);text-align:center;\">"
                + "<p style=\"margin:0;font-size:12px;color:#475569;\">"
                + "Este correo fue enviado autom\u00E1ticamente por Miku Inn.<br>"
                + "Si no creaste esta cuenta, por favor ignora este mensaje o cont\u00E1ctanos."
                + "</p>"
                + "</div>"

                + "</div>"
                + "</div>"
                + "</body></html>";
    }

    /**
     * Enmascara la contrase\u00F1a: muestra los primeros 2 y el \u00FAltimo car\u00E1cter.
     * Ejemplo: "Pine123" → "Pi***3"
     */
    private String enmascararContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() <= 3) return "***";
        return contrasena.substring(0, 2)
                + "*".repeat(contrasena.length() - 3)
                + contrasena.charAt(contrasena.length() - 1);
    }

    // ─────────────────────── Obtener perfil completo ───────────────────────

    public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId) {
        UsuarioPerfilResponseDTO perfil = usuarioRepository.obtenerPerfil(usuarioId);
        if (perfil == null) throw new RuntimeException("Usuario no encontrado");

        List<String> nacionalidades = usuarioRepository.obtenerNacionalidades(usuarioId);
        perfil.setNacionalidades(nacionalidades);

        return perfil;
    }

    // ─────────────────────── Cambiar teléfono ──────────────────────────────

    public void cambiarTelefono(int usuarioId, String nuevoTelefono) {
        if (nuevoTelefono == null || nuevoTelefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        usuarioRepository.actualizarTelefono(usuarioId, nuevoTelefono);
    }

    // ─────────────────────── Cambiar contraseña ────────────────────────────

    public void cambiarContrasena(int usuarioId, String contrasenaActual, String contrasenaNueva) {
        String hashActual = usuarioRepository.obtenerContrasena(usuarioId);
        if (!PasswordHelper.verificar(contrasenaActual, hashActual)) {
            throw new CredencialesInvalidasException();
        }

        String nuevoHash = PasswordHelper.hashear(contrasenaNueva);
        usuarioRepository.actualizarContrasena(usuarioId, nuevoHash);
    }

    // ─────────────────────── Admin: listar todos los usuarios ──────────────

    public List<UsuarioAdminDTO> listarTodosUsuarios() {
        return usuarioRepository.listarTodosConRol();
    }

    // ─────────────────────── Admin: cambiar rol ────────────────────────────

    public void cambiarRol(int usuarioId, int nuevoRolId) {
        if (nuevoRolId != 1 && nuevoRolId != 2 && nuevoRolId != 3) {
            throw new IllegalArgumentException("Rol inválido. Solo se permiten 1 (Usuario), 2 (Administrador) o 3 (Webservice)");
        }
        usuarioRepository.actualizarRol(usuarioId, nuevoRolId);
    }
}