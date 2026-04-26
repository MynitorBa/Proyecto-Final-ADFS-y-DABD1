package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.*;
import org.example.services.AdminReservacionService;
import org.example.services.HotelService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller que centraliza la administracion de hoteles, habitaciones, amenidades,
 * imagenes, reservaciones y metricas del sistema.
 * Todas las rutas requieren rol Administrador (rol 2).
 */
public class HotelController {

    private final HotelService            hotelService;
    private final AdminReservacionService adminReservacionService;

    /**
     * Crea una instancia de HotelController con sus dependencias inyectadas.
     */
    public HotelController(HotelService hotelService,
                           AdminReservacionService adminReservacionService) {
        this.hotelService            = hotelService;
        this.adminReservacionService = adminReservacionService;
    }

    /**
     * Registra todas las rutas administrativas en la aplicacion Javalin.
     * @param app instancia de Javalin donde se registran las rutas.
     */
    public void registerRoutes(Javalin app) {
        app.get   ("/admin/amenidades",                                    this::handleListarAmenidades);
        app.post  ("/admin/amenidades",                                    this::handleCrearAmenidad);
        app.get   ("/admin/paises",                                        this::handleListarPaises);
        app.get   ("/admin/ciudades",                                      this::handleListarCiudades);
        app.get   ("/admin/hoteles",                                       this::handleListarHoteles);
        app.post  ("/admin/hoteles",                                       this::handleCrearHotel);
        app.patch ("/admin/hoteles/{id}",                                  this::handleEditarHotel);
        app.delete("/admin/hoteles/{id}",                                  this::handleEliminarHotel);
        app.get   ("/admin/hoteles/{id}/reservas-activas",                 this::handleReservasActivasHotel);
        app.post  ("/admin/hoteles/{id}/cerrar-con-cancelaciones",         this::handleCerrarHotel);
        app.post  ("/admin/hoteles/{id}/reactivar",                        this::handleReactivarHotel);
        app.post  ("/admin/hoteles/{id}/imagenes",                         this::handleAgregarImagenHotel);
        app.delete("/admin/hoteles/imagenes/{imgId}",                      this::handleEliminarImagenHotel);
        app.get   ("/admin/hoteles/{id}/amenidades",                       this::handleListarAmenidadesHotel);
        app.post  ("/admin/hoteles/{id}/amenidades",                       this::handleAgregarAmenidadHotel);
        app.patch ("/admin/hoteles/amenidades/{haId}",                     this::handleActualizarAmenidadHotel);
        app.delete("/admin/hoteles/amenidades/{haId}",                     this::handleEliminarAmenidadHotel);
        app.post  ("/admin/hoteles/amenidades/{haId}/imagenes",            this::handleAgregarImagenAmenidad);
        app.delete("/admin/hoteles/amenidades/imagenes/{imgId}",           this::handleEliminarImagenAmenidad);
        app.get   ("/admin/hoteles/{id}/habitaciones",                     this::handleListarHabitaciones);
        app.post  ("/admin/hoteles/{id}/habitaciones",                     this::handleCrearHabitacion);
        app.patch ("/admin/habitaciones/{id}",                             this::handleEditarHabitacion);
        app.delete("/admin/habitaciones/{id}",                             this::handleEliminarHabitacion);
        app.get   ("/admin/habitaciones/{id}/reservas-activas",            this::handleReservasActivasHabitacion);
        app.post  ("/admin/habitaciones/{id}/cerrar-con-cancelaciones",    this::handleCerrarHabitacion);
        app.post  ("/admin/habitaciones/{id}/reactivar",                   this::handleReactivarHabitacion);
        app.post  ("/admin/habitaciones/{id}/imagenes",                    this::handleAgregarImagenHabitacion);
        app.delete("/admin/habitaciones/imagenes/{imgId}",                 this::handleEliminarImagenHabitacion);
        app.get   ("/admin/reservaciones",                                 this::handleListarReservaciones);
        app.get   ("/admin/reservaciones/recientes",                       this::handleListarReservacionesRecientes);
        app.patch ("/admin/reservaciones/{id}/cancelar",                   this::handleCancelarReservacion);
        app.get   ("/admin/metricas",                                      this::handleObtenerMetricas);


        // GET  /admin/tipos-habitacion                          → listar todos los tipos con imagenes
        // PATCH /admin/tipos-habitacion/{id}                    → editar solo precios del tipo
        // POST  /admin/tipos-habitacion/{id}/imagenes           → agregar imagen al tipo
        // DELETE /admin/tipos-habitacion/imagenes/{imgId}       → eliminar imagen del tipo

        app.get   ("/admin/tipos-habitacion",                          this::handleListarTiposHabitacion);
        app.patch ("/admin/tipos-habitacion/{id}",                     this::handleEditarTipoHabitacion);
        app.post  ("/admin/tipos-habitacion/{id}/imagenes",            this::handleAgregarImagenTipoHabitacion);
        app.delete("/admin/tipos-habitacion/imagenes/{imgId}",         this::handleEliminarImagenTipoHabitacion);

    }

    // -------------------------------------------------------------------------
    // Amenidades (catalogo global)
    // -------------------------------------------------------------------------

    /** Retorna el catalogo completo de amenidades disponibles. */
    void handleListarAmenidades(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.listarAmenidades());
    }

    /** Agrega una nueva amenidad al catalogo del sistema. */
    void handleCrearAmenidad(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            Map<?, ?> body  = ctx.bodyAsClass(Map.class);
            String    nombre = body.get("nombre") != null ? body.get("nombre").toString() : "";
            ctx.status(201).json(hotelService.crearAmenidad(nombre));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Paises / Ciudades
    // -------------------------------------------------------------------------

    /** Retorna el catalogo de paises registrados. */
    void handleListarPaises(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.listarPaises());
    }

    /** Retorna el catalogo de ciudades registradas. */
    void handleListarCiudades(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.listarCiudades());
    }

    // -------------------------------------------------------------------------
    // Hoteles - CRUD
    // -------------------------------------------------------------------------

    /** Retorna todos los hoteles registrados en el sistema. */
    void handleListarHoteles(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.listarTodos());
    }

    /** Crea un nuevo hotel con los datos proporcionados en el cuerpo de la peticion. */
    void handleCrearHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.crearHotel(ctx.bodyAsClass(CrearHotelRequestDTO.class)));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Actualiza los datos de un hotel existente. */
    void handleEditarHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.editarHotel(id(ctx, "id"), ctx.bodyAsClass(EditarHotelRequestDTO.class));
            ctx.json(Map.of("mensaje", "Hotel actualizado"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina un hotel del sistema por su ID (rechaza si tiene reservas activas). */
    void handleEliminarHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.eliminarHotel(id(ctx, "id"));
            ctx.json(Map.of("mensaje", "Hotel eliminado"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Hoteles - operaciones especiales
    // -------------------------------------------------------------------------

    /** Retorna el recuento y datos de las reservaciones activas del hotel. */
    void handleReservasActivasHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.json(hotelService.obtenerReservasActivasHotel(id(ctx, "id")));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Cancela todas las reservas activas del hotel y notifica a usuarios por correo.
     * Si eliminarDefinitivo=true  elimina el hotel fisicamente.
     * Si eliminarDefinitivo=false cambia EstadoID a 2 (Cerrado).
     * Body: { "hotelNombre": "...", "eliminarDefinitivo": false }
     */
    void handleCerrarHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String hotelNombre = body != null && body.get("hotelNombre") != null
                    ? body.get("hotelNombre").toString() : "Hotel";
            boolean eliminarDefinitivo = body != null && body.get("eliminarDefinitivo") != null
                    && Boolean.parseBoolean(body.get("eliminarDefinitivo").toString());
            Map<String, Object> resultado =
                    hotelService.cerrarHotelConCancelaciones(id(ctx, "id"), hotelNombre, eliminarDefinitivo);
            ctx.json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Reactiva un hotel cerrado (EstadoID 2 → 1). El hotel vuelve a aparecer en busquedas. */
    void handleReactivarHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.reactivarHotel(id(ctx, "id"));
            ctx.json(Map.of("mensaje", "Hotel reactivado correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Hoteles - imagenes
    // -------------------------------------------------------------------------

    /** Agrega una imagen en base64 a un hotel especifico. */
    void handleAgregarImagenHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.agregarImagenHotel(
                    id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina una imagen de hotel por su ID de imagen. */
    void handleEliminarImagenHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.eliminarImagenHotel(id(ctx, "imgId"));
        ctx.json(Map.of("mensaje", "Imagen eliminada"));
    }

    // -------------------------------------------------------------------------
    // Hoteles - amenidades asignadas
    // -------------------------------------------------------------------------

    /** Retorna las amenidades asignadas a un hotel especifico. */
    void handleListarAmenidadesHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.json(hotelService.listarAmenidadesHotel(id(ctx, "id")));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Asigna una amenidad del catalogo a un hotel especifico. */
    void handleAgregarAmenidadHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.agregarAmenidadHotel(
                    id(ctx, "id"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Actualiza los datos de una amenidad ya asignada a un hotel. */
    void handleActualizarAmenidadHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.actualizarAmenidadHotel(id(ctx, "haId"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class));
        ctx.json(Map.of("mensaje", "Amenidad actualizada"));
    }

    /** Elimina la relacion entre una amenidad y un hotel. */
    void handleEliminarAmenidadHotel(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.eliminarAmenidadHotel(id(ctx, "haId"));
        ctx.json(Map.of("mensaje", "Amenidad eliminada"));
    }

    // -------------------------------------------------------------------------
    // Amenidades de hotel - imagenes
    // -------------------------------------------------------------------------

    /** Agrega una imagen en base64 a una amenidad de hotel. */
    void handleAgregarImagenAmenidad(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.agregarImagenAmenidad(
                    id(ctx, "haId"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina una imagen asociada a una amenidad de hotel. */
    void handleEliminarImagenAmenidad(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.eliminarImagenAmenidad(id(ctx, "imgId"));
        ctx.json(Map.of("mensaje", "Imagen de amenidad eliminada"));
    }

    // -------------------------------------------------------------------------
    // Habitaciones - CRUD
    // -------------------------------------------------------------------------

    /** Retorna las habitaciones registradas para un hotel especifico. */
    void handleListarHabitaciones(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.json(hotelService.listarHabitaciones(id(ctx, "id")));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Crea una nueva habitacion asociada al hotel indicado en el path. */
    void handleCrearHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            CrearHabitacionRequestDTO req = ctx.bodyAsClass(CrearHabitacionRequestDTO.class);
            req.setHotelId(id(ctx, "id"));
            ctx.status(201).json(hotelService.crearHabitacion(req));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Actualiza los datos de una habitacion existente. */
    void handleEditarHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.editarHabitacion(id(ctx, "id"), ctx.bodyAsClass(EditarHabitacionRequestDTO.class));
            ctx.json(Map.of("mensaje", "Habitacion actualizada"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina una habitacion del sistema por su ID. */
    void handleEliminarHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.eliminarHabitacion(id(ctx, "id"));
            ctx.json(Map.of("mensaje", "Habitacion eliminada"));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Habitaciones - operaciones especiales
    // -------------------------------------------------------------------------

    /** Retorna el recuento y datos de las reservaciones activas de la habitacion. */
    void handleReservasActivasHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.json(hotelService.obtenerReservasActivasHabitacion(id(ctx, "id")));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Cancela todas las reservas activas de la habitacion y notifica a usuarios por correo.
     * Si eliminarDefinitivo=true  elimina la habitacion fisicamente.
     * Si eliminarDefinitivo=false cambia ESTADO_ID a 2 (Cerrada).
     * Body: { "nombreHabitacion": "...", "eliminarDefinitivo": false }
     */
    void handleCerrarHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String nombreHabitacion = body != null && body.get("nombreHabitacion") != null
                    ? body.get("nombreHabitacion").toString() : "Habitacion";
            boolean eliminarDefinitivo = body != null && body.get("eliminarDefinitivo") != null
                    && Boolean.parseBoolean(body.get("eliminarDefinitivo").toString());
            Map<String, Object> resultado =
                    hotelService.cerrarHabitacionConCancelaciones(id(ctx, "id"), nombreHabitacion, eliminarDefinitivo);
            ctx.json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Reactiva una habitacion cerrada (ESTADO_ID 2 → 1). */
    void handleReactivarHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.reactivarHabitacion(id(ctx, "id"));
            ctx.json(Map.of("mensaje", "Habitacion reactivada correctamente"));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Habitaciones - imagenes
    // -------------------------------------------------------------------------

    /** Agrega una imagen en base64 a una habitacion especifica. */
    void handleAgregarImagenHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.agregarImagenHabitacion(
                    id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina una imagen de habitacion por su ID de imagen. */
    void handleEliminarImagenHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.eliminarImagenHabitacion(id(ctx, "imgId"));
        ctx.json(Map.of("mensaje", "Imagen eliminada"));
    }

    // -------------------------------------------------------------------------
    // Reservaciones
    // -------------------------------------------------------------------------

    /** Retorna todas las reservaciones registradas en el sistema. */
    void handleListarReservaciones(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(adminReservacionService.listarTodas());
    }

    /** Retorna las 10 reservaciones mas recientes (version ligera para el dashboard). */
    void handleListarReservacionesRecientes(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(adminReservacionService.listarRecientes(10));
    }

    /**
     * Cancela una reservacion con un motivo opcional.
     * Notifica al sistema externo de la agencia (si corresponde) y envia correo al usuario.
     * Respuesta: { mensaje, notificacionAgencia: { esReservaDeAgencia, nombreAgencia,
     *              enviado, httpStatus, respuestaAgencia, error } }
     */
    void handleCancelarReservacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        int    reservacionId = id(ctx, "id");
        String motivo        = "Cancelada por administrador";

        // Intenta leer el motivo del cuerpo; si falla o no viene, conserva el valor por defecto
        try {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            if (body.containsKey("motivo") && body.get("motivo") != null)
                motivo = body.get("motivo").toString();
        } catch (Exception ignored) {}

        try {
            ResultadoNotificacionDTO resultadoAgencia =
                    adminReservacionService.cancelarReservacion(reservacionId, motivo,
                            ctx.ip(), ctx.header("User-Agent"));

            // Construye respuesta enriquecida para que el admin vea el estado de la agencia
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("mensaje", "Reservacion cancelada correctamente");
            respuesta.put("notificacionAgencia", resultadoAgencia);

            ctx.json(respuesta);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Metricas
    // -------------------------------------------------------------------------

    /** Retorna las metricas generales del sistema para el panel de administracion. */
    void handleObtenerMetricas(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.obtenerMetricas());
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Verifica si el usuario autenticado tiene rol Administrador (rol 2).
     * @param ctx contexto de la peticion HTTP.
     * @return true si el rolId del contexto es 2, false en caso contrario.
     */
    private boolean esAdmin(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }

    /**
     * Responde con 403 cuando el acceso requiere rol Administrador y el usuario no lo tiene.
     * @param ctx contexto de la peticion HTTP.
     */
    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }

    /**
     * Extrae y convierte a entero el path parameter indicado.
     * @param ctx   contexto de la peticion HTTP.
     * @param param nombre del path parameter a extraer.
     * @return valor del parametro convertido a int.
     */
    private int id(Context ctx, String param) {
        return Integer.parseInt(ctx.pathParam(param));
    }






    // Tipos de habitacion

    /** Retorna todos los tipos de habitacion con sus datos y IDs de imagenes. */
    void handleListarTiposHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        ctx.json(hotelService.listarTiposHabitacion());
    }

    /**
     * Actualiza los precios de un tipo de habitacion.
     * Solo acepta precioPorPersona y precioPorNoche en el body.
     * Body: { "precioPorPersona": 90.0, "precioPorNoche": 150.0 }
     */
    void handleEditarTipoHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            hotelService.editarPreciosTipoHabitacion(
                    id(ctx, "id"),
                    ctx.bodyAsClass(org.example.dtos.EditarTipoHabitacionRequestDTO.class));
            ctx.json(Map.of("mensaje", "Precios del tipo de habitacion actualizados"));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Agrega una imagen en base64 a un tipo de habitacion especifico. */
    void handleAgregarImagenTipoHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        try {
            ctx.status(201).json(hotelService.agregarImagenTipoHabitacion(
                    id(ctx, "id"),
                    ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Elimina una imagen de tipo de habitacion por su ID de imagen. */
    void handleEliminarImagenTipoHabitacion(Context ctx) {
        if (!esAdmin(ctx)) { deny(ctx); return; }
        hotelService.eliminarImagenTipoHabitacion(id(ctx, "imgId"));
        ctx.json(Map.of("mensaje", "Imagen de tipo de habitacion eliminada"));
    }
}
