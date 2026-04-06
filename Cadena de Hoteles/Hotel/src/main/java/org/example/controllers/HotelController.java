package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.*;
import org.example.services.AdminReservacionService;
import org.example.services.HotelService;

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

        // Retorna el catalogo completo de amenidades disponibles
        app.get("/admin/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarAmenidades());
        });

        // Agrega una nueva amenidad al catalogo del sistema
        app.post("/admin/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                Map<?, ?> body = ctx.bodyAsClass(Map.class);
                String nombre = body.get("nombre") != null ? body.get("nombre").toString() : "";
                ctx.status(201).json(hotelService.crearAmenidad(nombre));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Retorna el catalogo de paises registrados
        app.get("/admin/paises",   ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarPaises()); });

        // Retorna el catalogo de ciudades registradas
        app.get("/admin/ciudades", ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarCiudades()); });

        // Retorna todos los hoteles registrados en el sistema
        app.get("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarTodos());
        });

        // Crea un nuevo hotel con los datos proporcionados en el cuerpo de la peticion
        app.post("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.crearHotel(ctx.bodyAsClass(CrearHotelRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Actualiza los datos de un hotel existente
        app.patch("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHotel(id(ctx, "id"), ctx.bodyAsClass(EditarHotelRequestDTO.class));
                ctx.json(Map.of("mensaje", "Hotel actualizado"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Elimina un hotel del sistema por su ID
        app.delete("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.eliminarHotel(id(ctx, "id"));
                ctx.json(Map.of("mensaje", "Hotel eliminado"));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Agrega una imagen en base64 a un hotel especifico
        app.post("/admin/hoteles/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenHotel(
                        id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Elimina una imagen de hotel por su ID de imagen
        app.delete("/admin/hoteles/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHotel(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen eliminada"));
        });

        // Retorna las amenidades asignadas a un hotel especifico
        app.get("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarAmenidadesHotel(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Asigna una amenidad del catalogo a un hotel especifico
        app.post("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarAmenidadHotel(
                        id(ctx, "id"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Actualiza los datos de una amenidad ya asignada a un hotel
        app.patch("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.actualizarAmenidadHotel(id(ctx, "haId"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class));
            ctx.json(Map.of("mensaje", "Amenidad actualizada"));
        });

        // Elimina la relacion entre una amenidad y un hotel
        app.delete("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarAmenidadHotel(id(ctx, "haId"));
            ctx.json(Map.of("mensaje", "Amenidad eliminada"));
        });

        // Agrega una imagen en base64 a una amenidad de hotel
        app.post("/admin/hoteles/amenidades/{haId}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenAmenidad(
                        id(ctx, "haId"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Elimina una imagen asociada a una amenidad de hotel
        app.delete("/admin/hoteles/amenidades/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenAmenidad(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen de amenidad eliminada"));
        });

        // Retorna las habitaciones registradas para un hotel especifico
        app.get("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarHabitaciones(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Crea una nueva habitacion asociada al hotel indicado en el path
        app.post("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                CrearHabitacionRequestDTO req = ctx.bodyAsClass(CrearHabitacionRequestDTO.class);
                // Inyecta el hotelId del path en el DTO antes de procesar la creacion
                req.setHotelId(id(ctx, "id"));
                ctx.status(201).json(hotelService.crearHabitacion(req));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Actualiza los datos de una habitacion existente
        app.patch("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHabitacion(id(ctx, "id"), ctx.bodyAsClass(EditarHabitacionRequestDTO.class));
                ctx.json(Map.of("mensaje", "Habitacion actualizada"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Elimina una habitacion del sistema por su ID
        app.delete("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.eliminarHabitacion(id(ctx, "id"));
                ctx.json(Map.of("mensaje", "Habitacion eliminada"));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Agrega una imagen en base64 a una habitacion especifica
        app.post("/admin/habitaciones/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenHabitacion(
                        id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Elimina una imagen de habitacion por su ID de imagen
        app.delete("/admin/habitaciones/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHabitacion(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen eliminada"));
        });

        // Retorna todas las reservaciones registradas en el sistema
        app.get("/admin/reservaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(adminReservacionService.listarTodas());
        });

        // Cancela una reservacion con un motivo opcional; usa motivo por defecto si no se proporciona
        app.patch("/admin/reservaciones/{id}/cancelar", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int reservacionId = id(ctx, "id");
            String motivo = "Cancelada por administrador";

            // Intenta leer el motivo del cuerpo; si falla o no viene, conserva el valor por defecto
            try {
                Map<?, ?> body = ctx.bodyAsClass(Map.class);
                if (body.containsKey("motivo") && body.get("motivo") != null)
                    motivo = body.get("motivo").toString();
            } catch (Exception ignored) {}

            try {
                adminReservacionService.cancelarReservacion(reservacionId, motivo);
                ctx.json(Map.of("mensaje", "Reservacion cancelada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // Retorna las metricas generales del sistema para el panel de administracion
        app.get("/admin/metricas", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.obtenerMetricas());
        });
    }

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
}