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

/**
 * Service para la gestion de usuarios del sistema.
 * Cubre el registro, validacion de campos unicos, consulta de perfil,
 * actualizacion de datos personales y operaciones administrativas de rol.
 */
public class UsuarioService {

    private final UsuarioRepository             usuarioRepository;
    private final PaisRepository                paisRepository;
    private final CiudadRepository              ciudadRepository;
    private final NacionalidadRepository        nacionalidadRepository;
    private final UsuarioNacionalidadRepository usuarioNacionalidadRepository;
    private final LogRepository                 logRepository;
    private final OfertasEmailService           ofertasEmailService;

    /**
     * Crea una instancia de UsuarioService con sus dependencias inyectadas.
     */
    public UsuarioService(UsuarioRepository usuarioRepository,
                          PaisRepository paisRepository,
                          CiudadRepository ciudadRepository,
                          NacionalidadRepository nacionalidadRepository,
                          UsuarioNacionalidadRepository usuarioNacionalidadRepository,
                          LogRepository logRepository,
                          OfertasEmailService ofertasEmailService) {
        this.usuarioRepository             = usuarioRepository;
        this.paisRepository                = paisRepository;
        this.ciudadRepository              = ciudadRepository;
        this.nacionalidadRepository        = nacionalidadRepository;
        this.usuarioNacionalidadRepository = usuarioNacionalidadRepository;
        this.logRepository                 = logRepository;
        this.ofertasEmailService           = ofertasEmailService;
    }

    /**
     * Verifica si el username, correo o pasaporte ya estan registrados en el sistema.
     *
     * @param request DTO con los campos a validar.
     * @return DTO indicando cuales campos ya existen.
     */
    public UsuarioValidacionResponseDTO validarDisponibilidad(UsuarioValidacionRequestDTO request) {
        boolean usernameExiste  = usuarioRepository.existeUsername(request.getUsername());
        boolean correoExiste    = usuarioRepository.existeCorreo(request.getCorreo());
        boolean pasaporteExiste = usuarioRepository.existePasaporte(request.getPasaporte());

        return new UsuarioValidacionResponseDTO(usernameExiste, correoExiste, pasaporteExiste);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida que no existan campos duplicados, hashea la contrasena, resuelve
     * pais y ciudad (creandolos si no existen), asigna nacionalidades y envia
     * un correo de bienvenida. El correo no bloquea el registro si falla.
     *
     * @param request datos del usuario a registrar.
     * @return ID del usuario recien creado.
     * @throws CamposDuplicadosException si el username, correo o pasaporte ya estan en uso.
     */
    public int registrarUsuario(UsuarioValidacionRequestDTO request, String ip, String userAgent) {
            try {
                UsuarioValidacionResponseDTO validacion = validarDisponibilidad(request);
                if (validacion.isUsernameExiste() || validacion.isCorreoExiste() || validacion.isPasaporteExiste()) {
                    logRepository.registrar(
                            LogRepository.TIPO_REGISTRO_FALLIDO,
                            null,
                            request.getUsername(),
                            false,
                            ip,
                            userAgent,
                            "Campos duplicados: " + construirDetalleDuplicados(validacion)
                    );
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
                        ciudadId,
                        request.getPreferenciasOferta()
                );

                if (request.getNacionalidades() != null && !request.getNacionalidades().isEmpty()) {
                    List<Integer> nacionalidadIds = new ArrayList<>();
                    for (String nombreNac : request.getNacionalidades()) {
                        nacionalidadIds.add(nacionalidadRepository.buscarOCrearPorNombre(nombreNac));
                    }
                    usuarioNacionalidadRepository.asignarNacionalidades(nuevoUsuarioId, nacionalidadIds);
                }

                logRepository.registrar(
                        LogRepository.TIPO_REGISTRO_EXITOSO,
                        nuevoUsuarioId,
                        request.getUsername(),
                        true,
                        ip,
                        userAgent,
                        null
                );

                try {
                    String html = construirCorreoBienvenida(
                            request.getNombre(), request.getApellido(), request.getUsername(),
                            request.getCorreo(), request.getContrasena(),
                            request.getTelefono(), request.getPais(), request.getCiudad()
                    );
                    EmailHelper.enviar(
                            request.getCorreo(),
                            "\u00A1Bienvenido a Miku Inn, " + request.getNombre() + "! \uD83C\uDFE8",
                            html
                    );
                } catch (Exception e) {
                    System.err.println("\u26A0 No se pudo enviar correo de bienvenida a "
                            + request.getCorreo() + ": " + e.getMessage());
                }

                // Si el usuario opted-in a ofertas, enviarle las ofertas inmediatamente
                if (request.getPreferenciasOferta() != null && !request.getPreferenciasOferta().isBlank()) {
                    try {
                        ofertasEmailService.enviarOfertasAUsuario(nuevoUsuarioId);
                    } catch (Exception e) {
                        System.err.println("\u26A0 No se pudo enviar correo de ofertas a "
                                + request.getCorreo() + ": " + e.getMessage());
                    }
                }

                return nuevoUsuarioId;

            } catch (CamposDuplicadosException e) {
                throw e;
            } catch (Exception e) {
                logRepository.registrar(
                        LogRepository.TIPO_REGISTRO_ERROR_INTERNO,
                        null,
                        request.getUsername(),
                        false,
                        ip,
                        userAgent,
                        e.getMessage()
                );
                throw new RuntimeException("Error interno al registrar usuario", e);
            }
        }

    /**
     * Construye el HTML del correo de bienvenida para el usuario recien registrado.
     * Incluye los datos de la cuenta y consejos de seguridad.
     *
     * @param nombre     nombre del usuario.
     * @param apellido   apellido del usuario.
     * @param username   nombre de usuario elegido.
     * @param correo     direccion de correo electronico.
     * @param contrasena contrasena en texto plano (se enmascara antes de incluirla).
     * @param telefono   numero de telefono del usuario.
     * @param pais       pais de residencia.
     * @param ciudad     ciudad de residencia.
     * @return cadena con el HTML completo del correo.
     */
    private String construirCorreoBienvenida(
            String nombre, String apellido, String username,
            String correo, String contrasena, String telefono,
            String pais, String ciudad
    ) {
        // Enmascarar la contrasena antes de incluirla en el correo
        String contrasenaMask = enmascararContrasena(contrasena);

        return "<!DOCTYPE html>"
                + "<html lang=\"es\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"margin:0;padding:0;background-color:#0f172a;font-family:'Segoe UI',Roboto,Arial,sans-serif;\">"
                + "<div style=\"max-width:600px;margin:0 auto;padding:40px 20px;\">"

                + "<div style=\"background:linear-gradient(135deg,#1e293b 0%,#334155 100%);border-radius:20px;overflow:hidden;border:1px solid rgba(255,255,255,0.1);box-shadow:0 20px 60px rgba(0,0,0,0.4);\">"

                + "<div style=\"background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:40px 30px;text-align:center;\">"
                + "<h1 style=\"margin:0;font-size:28px;color:#ffffff;font-weight:700;\">\uD83C\uDFE8 Miku Inn</h1>"
                + "<p style=\"margin:8px 0 0;font-size:14px;color:rgba(255,255,255,0.85);font-weight:400;\">Tu aventura comienza aqu\u00ED</p>"
                + "</div>"

                + "<div style=\"padding:35px 30px;\">"

                + "<h2 style=\"margin:0 0 8px;font-size:22px;color:#f8fafc;font-weight:700;\">"
                + "\u00A1Bienvenido, " + nombre + "! \uD83C\uDF89</h2>"
                + "<p style=\"margin:0 0 25px;font-size:15px;color:#94a3b8;line-height:1.6;\">"
                + "Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesi\u00F3n y comenzar a reservar experiencias inolvidables."
                + "</p>"

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

                + "<div style=\"text-align:center;margin-bottom:10px;\">"
                + "<a href=\"http://localhost:5173\" "
                + "style=\"display:inline-block;padding:14px 40px;background:linear-gradient(135deg,#667eea,#764ba2);color:#ffffff;text-decoration:none;border-radius:10px;font-size:15px;font-weight:700;box-shadow:0 4px 20px rgba(102,126,234,0.4);\">"
                + "Iniciar Sesi\u00F3n \u2192</a>"
                + "</div>"

                + "</div>"

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
     * Enmascara la contrasena mostrando solo los primeros 2 y el ultimo caracter.
     * Ejemplo: "Pine123" se convierte en "Pi***3".
     *
     * @param contrasena contrasena en texto plano a enmascarar.
     * @return contrasena enmascarada, o "***" si es nula o muy corta.
     */
    private String enmascararContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() <= 3) return "***";
        return contrasena.substring(0, 2)
                + "*".repeat(contrasena.length() - 3)
                + contrasena.charAt(contrasena.length() - 1);
    }

    /**
     * Retorna el perfil completo de un usuario incluyendo sus nacionalidades.
     *
     * @param usuarioId ID del usuario a consultar.
     * @return DTO con los datos del perfil y lista de nacionalidades.
     * @throws RuntimeException si el usuario no existe.
     */
    public UsuarioPerfilResponseDTO obtenerPerfil(int usuarioId) {
        UsuarioPerfilResponseDTO perfil = usuarioRepository.obtenerPerfil(usuarioId);
        if (perfil == null) throw new RuntimeException("Usuario no encontrado");

        List<String> nacionalidades = usuarioRepository.obtenerNacionalidades(usuarioId);
        perfil.setNacionalidades(nacionalidades);

        return perfil;
    }

    /**
     * Actualiza el numero de telefono de un usuario.
     *
     * @param usuarioId     ID del usuario a actualizar.
     * @param nuevoTelefono nuevo numero de telefono; no puede ser nulo ni vacio.
     * @throws IllegalArgumentException si el telefono es nulo o esta en blanco.
     */
    public void cambiarTelefono(int usuarioId, String nuevoTelefono, String ip, String userAgent) {
        if (nuevoTelefono == null || nuevoTelefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        try {
            usuarioRepository.actualizarTelefono(usuarioId, nuevoTelefono);
            logRepository.registrar(
                    LogRepository.TIPO_CAMBIO_TELEFONO_EXITOSO,
                    usuarioId,
                    null,
                    true,
                    ip,
                    userAgent,
                    null
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logRepository.registrar(
                    LogRepository.TIPO_CAMBIO_TELEFONO_ERROR,
                    usuarioId,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Cambia la contrasena de un usuario previa verificacion de la contrasena actual.
     *
     * @param usuarioId        ID del usuario.
     * @param contrasenaActual contrasena actual en texto plano para verificacion.
     * @param contrasenaNueva  nueva contrasena en texto plano que sera hasheada.
     * @throws CredencialesInvalidasException si la contrasena actual no coincide.
     */
    public void cambiarContrasena(int usuarioId, String contrasenaActual, String contrasenaNueva,
                                  String ip, String userAgent) {
        try {
            String hashActual = usuarioRepository.obtenerContrasena(usuarioId);
            if (!PasswordHelper.verificar(contrasenaActual, hashActual)) {
                logRepository.registrar(
                        LogRepository.TIPO_CAMBIO_CONTRASENA_FALLIDO,
                        usuarioId,
                        null,
                        false,
                        ip,
                        userAgent,
                        "Contrasena actual incorrecta"
                );
                throw new CredencialesInvalidasException();
            }

            String nuevoHash = PasswordHelper.hashear(contrasenaNueva);
            usuarioRepository.actualizarContrasena(usuarioId, nuevoHash);

            logRepository.registrar(
                    LogRepository.TIPO_CAMBIO_CONTRASENA_EXITOSO,
                    usuarioId,
                    null,
                    true,
                    ip,
                    userAgent,
                    null
            );

        } catch (CredencialesInvalidasException e) {
            throw e;
        } catch (Exception e) {
            logRepository.registrar(
                    LogRepository.TIPO_CAMBIO_CONTRASENA_ERROR,
                    usuarioId,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Retorna todos los usuarios registrados junto con su rol asignado.
     * Uso exclusivo del panel de administracion.
     *
     * @return lista de DTOs con los datos de cada usuario y su rol.
     */
    public List<UsuarioAdminDTO> listarTodosUsuarios() {
        return usuarioRepository.listarTodosConRol();
    }

    /**
     * Cambia el rol de un usuario. Solo se aceptan los roles definidos en el sistema.
     *
     * @param usuarioId  ID del usuario al que se le cambiara el rol.
     * @param nuevoRolId ID del nuevo rol: 1 (Usuario), 2 (Administrador) o 3 (Webservice).
     * @throws IllegalArgumentException si el ID de rol no corresponde a ninguno de los roles validos.
     */
    public void cambiarRol(int usuarioId, int nuevoRolId) {
        if (nuevoRolId != 1 && nuevoRolId != 2 && nuevoRolId != 3) {
            throw new IllegalArgumentException("Rol inválido. Solo se permiten 1 (Usuario), 2 (Administrador) o 3 (Webservice)");
        }
        usuarioRepository.actualizarRol(usuarioId, nuevoRolId);
    }



    // ── Actualizaciones de perfil ──────────────────────────────────────────────

    /** Actualiza nombre, apellido y fecha de nacimiento. */
    public void actualizarDatosPersonales(int usuarioId, String nombre, String apellido, String fechaNacimientoStr) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre no puede estar vacío");
        if (apellido == null || apellido.isBlank()) throw new IllegalArgumentException("El apellido no puede estar vacío");
        java.sql.Date fecha = java.sql.Date.valueOf(java.time.LocalDate.parse(fechaNacimientoStr));
        usuarioRepository.actualizarDatosPersonales(usuarioId, nombre.trim(), apellido.trim(), fecha);
    }

    /**
     * Actualiza username, correo y/o pasaporte verificando que los nuevos valores
     * no estén en uso por otro usuario.
     */
    public void actualizarCredenciales(int usuarioId, String username, String correo, String pasaporte) {
        boolean usernameExiste  = username  != null && !username.isBlank()  && usuarioRepository.existeUsernameExceptoId(username, usuarioId);
        boolean correoExiste    = correo    != null && !correo.isBlank()    && usuarioRepository.existeCorreoExceptoId(correo, usuarioId);
        boolean pasaporteExiste = pasaporte != null && !pasaporte.isBlank() && usuarioRepository.existePasaporteExceptoId(pasaporte, usuarioId);

        if (usernameExiste || correoExiste || pasaporteExiste) {
            throw new org.example.helpers.CamposDuplicadosException(
                new org.example.dtos.UsuarioValidacionResponseDTO(usernameExiste, correoExiste, pasaporteExiste)
            );
        }

        if (username  != null && !username.isBlank())  usuarioRepository.actualizarUsername(usuarioId, username.trim());
        if (correo    != null && !correo.isBlank())    usuarioRepository.actualizarCorreo(usuarioId, correo.trim().toLowerCase());
        if (pasaporte != null && !pasaporte.isBlank()) usuarioRepository.actualizarPasaporte(usuarioId, pasaporte.trim().toUpperCase());
    }

    /** Actualiza el país y ciudad de residencia del usuario. */
    public void actualizarCiudad(int usuarioId, String pais, String ciudad) {
        if (pais == null || pais.isBlank()) throw new IllegalArgumentException("El país no puede estar vacío");
        if (ciudad == null || ciudad.isBlank()) throw new IllegalArgumentException("La ciudad no puede estar vacía");
        int paisId   = paisRepository.buscarOCrearPorNombre(pais.trim());
        int ciudadId = ciudadRepository.buscarOCrearPorNombre(ciudad.trim(), paisId);
        usuarioRepository.actualizarCiudad(usuarioId, ciudadId);
    }

    /** Reemplaza todas las nacionalidades del usuario. */
    public void actualizarNacionalidades(int usuarioId, List<String> nacionalidades) {
        if (nacionalidades == null || nacionalidades.isEmpty())
            throw new IllegalArgumentException("Debe indicar al menos una nacionalidad");
        usuarioNacionalidadRepository.eliminarPorUsuario(usuarioId);
        List<Integer> ids = new ArrayList<>();
        for (String n : nacionalidades) ids.add(nacionalidadRepository.buscarOCrearPorNombre(n));
        usuarioNacionalidadRepository.asignarNacionalidades(usuarioId, ids);
    }

    /** Guarda o limpia las preferencias de ofertas del usuario. */
    public void actualizarPreferencias(int usuarioId, String preferenciasOferta) {
        usuarioRepository.actualizarPreferencias(usuarioId, preferenciasOferta);
    }

    /** Construye un string descriptivo con los campos duplicados encontrados. */
    private String construirDetalleDuplicados(UsuarioValidacionResponseDTO v) {
        List<String> campos = new ArrayList<>();
        if (v.isUsernameExiste())  campos.add("username");
        if (v.isCorreoExiste())    campos.add("correo");
        if (v.isPasaporteExiste()) campos.add("pasaporte");
        return String.join(", ", campos);
    }
}