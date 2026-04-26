package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.ActualizarCiudadRequestDTO;
import org.example.dtos.ActualizarCredencialesRequestDTO;
import org.example.dtos.ActualizarDatosPersonalesRequestDTO;
import org.example.dtos.ActualizarNacionalidadesRequestDTO;
import org.example.dtos.ActualizarPreferenciasRequestDTO;
import org.example.dtos.CambiarContrasenaRequestDTO;
import org.example.dtos.CambiarRolRequestDTO;
import org.example.dtos.CambiarTelefonoRequestDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.services.UsuarioService;

import java.util.Map;

/**
 * Controller que gestiona las operaciones sobre usuarios del sistema.
 * Expone rutas publicas de registro y validacion, rutas privadas para el perfil
 * del usuario autenticado, y rutas administrativas exclusivas para rol 2.
 */
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Crea una instancia de UsuarioController con sus dependencias inyectadas.
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registra todas las rutas de usuarios en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {

        // Verifica si el username o correo proporcionados ya estan en uso
        app.get("/usuarios/validar", this::handleValidar);

        // Registra un nuevo usuario en el sistema; responde 409 si hay campos duplicados
        app.post("/usuarios/registrar", this::handleRegistrar);

        // Retorna el perfil del usuario autenticado
        app.get("/usuarios/perfil", this::handleObtenerPerfil);

        // Actualiza el numero de telefono del usuario autenticado
        app.patch("/usuarios/telefono", this::handleCambiarTelefono);

        // Actualiza la contrasena del usuario autenticado validando la contrasena actual
        app.patch("/usuarios/contrasena", this::handleCambiarContrasena);

        // Actualiza nombre, apellido y fecha de nacimiento del usuario autenticado
        app.patch("/usuarios/datos-personales", this::handleActualizarDatosPersonales);

        // Actualiza username, correo y/o pasaporte (con verificación de duplicados)
        app.patch("/usuarios/credenciales", this::handleActualizarCredenciales);

        // Actualiza el país y ciudad de residencia del usuario autenticado
        app.patch("/usuarios/ciudad", this::handleActualizarCiudad);

        // Reemplaza las nacionalidades del usuario autenticado
        app.patch("/usuarios/nacionalidades", this::handleActualizarNacionalidades);

        // Guarda o limpia las preferencias de ofertas del usuario autenticado
        app.patch("/usuarios/preferencias", this::handleActualizarPreferencias);

        // Retorna la lista completa de usuarios con su rol; exclusivo para administradores
        app.get("/admin/usuarios", this::handleListarAdmin);

        // Cambia el rol de un usuario especifico; exclusivo para administradores
        app.patch("/admin/usuarios/{id}/rol", this::handleCambiarRol);
    }

    void handleValidar(Context ctx) {
        UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
        ctx.json(usuarioService.validarDisponibilidad(request));
    }

    void handleRegistrar(Context ctx) {
        UsuarioValidacionRequestDTO request = ctx.bodyAsClass(UsuarioValidacionRequestDTO.class);
        try {
            int nuevoId = usuarioService.registrarUsuario(request, ctx.ip(), ctx.userAgent());
            ctx.status(201).json(Map.of(
                    "mensaje",   "Usuario creado exitosamente",
                    "usuarioId", nuevoId
            ));
        } catch (CamposDuplicadosException e) {
            ctx.status(409).json(Map.of(
                    "mensaje", "No se pudo crear el usuario, algunos campos ya existen",
                    "campos",  e.getDetalle()
            ));
        }
    }

    void handleObtenerPerfil(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        ctx.json(usuarioService.obtenerPerfil(usuarioId));
    }

    void handleCambiarTelefono(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        CambiarTelefonoRequestDTO request = ctx.bodyAsClass(CambiarTelefonoRequestDTO.class);
        try {
            usuarioService.cambiarTelefono(usuarioId, request.getTelefono(), ctx.ip(), ctx.userAgent());
            ctx.status(200).json(Map.of("mensaje", "Telefono actualizado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleCambiarContrasena(Context ctx) {
        int usuarioId = ctx.attribute("usuarioId");
        CambiarContrasenaRequestDTO request = ctx.bodyAsClass(CambiarContrasenaRequestDTO.class);
        try {
            usuarioService.cambiarContrasena(
                    usuarioId,
                    request.getContrasenaActual(),
                    request.getContrasenaNueva(),
                    ctx.ip(),
                    ctx.userAgent()
            );
            ctx.status(200).json(Map.of("mensaje", "Contrasena actualizada correctamente"));
        } catch (CredencialesInvalidasException e) {
            ctx.status(401).json(Map.of("mensaje", "La contrasena actual es incorrecta"));
        }
    }

    void handleListarAdmin(Context ctx) {
        int rolId = ctx.attribute("rolId");

        // Verifica que el usuario tenga rol Administrador antes de continuar
        if (rolId != 2) {
            ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
            return;
        }

        ctx.json(usuarioService.listarTodosUsuarios());
    }

    void handleCambiarRol(Context ctx) {
        int rolId = ctx.attribute("rolId");

        // Verifica que el usuario tenga rol Administrador antes de continuar
        if (rolId != 2) {
            ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
            return;
        }

        // Extrae el ID del usuario a modificar y el nuevo rol del cuerpo de la peticion
        int usuarioId  = Integer.parseInt(ctx.pathParam("id"));
        int nuevoRolId = ctx.bodyAsClass(CambiarRolRequestDTO.class).getRolId();

        try {
            usuarioService.cambiarRol(usuarioId, nuevoRolId);
            ctx.status(200).json(Map.of("mensaje", "Rol actualizado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarDatosPersonales(Context ctx) {
        int id = ctx.attribute("usuarioId");
        ActualizarDatosPersonalesRequestDTO req = ctx.bodyAsClass(ActualizarDatosPersonalesRequestDTO.class);
        try {
            usuarioService.actualizarDatosPersonales(id, req.getNombre(), req.getApellido(), req.getFechaNacimiento());
            ctx.status(200).json(Map.of("mensaje", "Datos personales actualizados correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarCredenciales(Context ctx) {
        int id = ctx.attribute("usuarioId");
        ActualizarCredencialesRequestDTO req = ctx.bodyAsClass(ActualizarCredencialesRequestDTO.class);
        try {
            usuarioService.actualizarCredenciales(id, req.getUsername(), req.getCorreo(), req.getPasaporte());
            ctx.status(200).json(Map.of("mensaje", "Credenciales actualizadas correctamente"));
        } catch (CamposDuplicadosException e) {
            ctx.status(409).json(Map.of("mensaje", "Algunos campos ya están en uso", "campos", e.getDetalle()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarCiudad(Context ctx) {
        int id = ctx.attribute("usuarioId");
        ActualizarCiudadRequestDTO req = ctx.bodyAsClass(ActualizarCiudadRequestDTO.class);
        try {
            usuarioService.actualizarCiudad(id, req.getPais(), req.getCiudad());
            ctx.status(200).json(Map.of("mensaje", "Ubicación actualizada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarNacionalidades(Context ctx) {
        int id = ctx.attribute("usuarioId");
        ActualizarNacionalidadesRequestDTO req = ctx.bodyAsClass(ActualizarNacionalidadesRequestDTO.class);
        try {
            usuarioService.actualizarNacionalidades(id, req.getNacionalidades());
            ctx.status(200).json(Map.of("mensaje", "Nacionalidades actualizadas correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    void handleActualizarPreferencias(Context ctx) {
        int id = ctx.attribute("usuarioId");
        ActualizarPreferenciasRequestDTO req = ctx.bodyAsClass(ActualizarPreferenciasRequestDTO.class);
        usuarioService.actualizarPreferencias(id, req.getPreferenciasOferta());
        ctx.status(200).json(Map.of("mensaje", "Preferencias guardadas correctamente"));
    }
}
