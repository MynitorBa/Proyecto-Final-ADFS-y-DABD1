package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.*;
import org.example.services.AdminReservacionService;
import org.example.services.HotelService;

import java.util.Map;

public class HotelController {

    private final HotelService            hotelService            = new HotelService();
    private final AdminReservacionService adminReservacionService = new AdminReservacionService();

    public void registerRoutes(Javalin app) {

        // ════════════════════════════════════════════════════
        //  CATÁLOGOS
        // ════════════════════════════════════════════════════

        // GET  /admin/amenidades  →  listar catálogo
        app.get("/admin/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarAmenidades());
        });

        // POST /admin/amenidades  →  crear nueva amenidad en el catálogo
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

        app.get("/admin/paises",   ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarPaises()); });
        app.get("/admin/ciudades", ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarCiudades()); });

        // ════════════════════════════════════════════════════
        //  HOTELES
        // ════════════════════════════════════════════════════

        app.get("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarTodos());
        });

        app.post("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.crearHotel(ctx.bodyAsClass(CrearHotelRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.patch("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHotel(id(ctx, "id"), ctx.bodyAsClass(EditarHotelRequestDTO.class));
                ctx.json(Map.of("mensaje", "Hotel actualizado"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.delete("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.eliminarHotel(id(ctx, "id"));
                ctx.json(Map.of("mensaje", "Hotel eliminado"));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  IMÁGENES DE HOTEL
        // ════════════════════════════════════════════════════

        app.post("/admin/hoteles/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenHotel(
                        id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.delete("/admin/hoteles/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHotel(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen eliminada"));
        });

        // ════════════════════════════════════════════════════
        //  AMENIDADES DEL HOTEL
        // ════════════════════════════════════════════════════

        app.get("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarAmenidadesHotel(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.post("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarAmenidadHotel(
                        id(ctx, "id"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.patch("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.actualizarAmenidadHotel(id(ctx, "haId"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class));
            ctx.json(Map.of("mensaje", "Amenidad actualizada"));
        });

        app.delete("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarAmenidadHotel(id(ctx, "haId"));
            ctx.json(Map.of("mensaje", "Amenidad eliminada"));
        });

        app.post("/admin/hoteles/amenidades/{haId}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenAmenidad(
                        id(ctx, "haId"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.delete("/admin/hoteles/amenidades/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenAmenidad(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen de amenidad eliminada"));
        });

        // ════════════════════════════════════════════════════
        //  HABITACIONES
        // ════════════════════════════════════════════════════

        app.get("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarHabitaciones(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.post("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                CrearHabitacionRequestDTO req = ctx.bodyAsClass(CrearHabitacionRequestDTO.class);
                req.setHotelId(id(ctx, "id"));
                ctx.status(201).json(hotelService.crearHabitacion(req));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.patch("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHabitacion(id(ctx, "id"), ctx.bodyAsClass(EditarHabitacionRequestDTO.class));
                ctx.json(Map.of("mensaje", "Habitación actualizada"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.delete("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.eliminarHabitacion(id(ctx, "id"));
                ctx.json(Map.of("mensaje", "Habitación eliminada"));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  IMÁGENES DE HABITACIÓN
        // ════════════════════════════════════════════════════

        app.post("/admin/habitaciones/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenHabitacion(
                        id(ctx, "id"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        app.delete("/admin/habitaciones/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHabitacion(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen eliminada"));
        });

        // ════════════════════════════════════════════════════
        //  ADMIN — RESERVACIONES
        // ════════════════════════════════════════════════════

        app.get("/admin/reservaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(adminReservacionService.listarTodas());
        });

        app.patch("/admin/reservaciones/{id}/cancelar", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int reservacionId = id(ctx, "id");
            String motivo = "Cancelada por administrador";
            try {
                Map<?, ?> body = ctx.bodyAsClass(Map.class);
                if (body.containsKey("motivo") && body.get("motivo") != null)
                    motivo = body.get("motivo").toString();
            } catch (Exception ignored) {}
            try {
                adminReservacionService.cancelarReservacion(reservacionId, motivo);
                ctx.json(Map.of("mensaje", "Reservación cancelada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  ADMIN — MÉTRICAS
        // ════════════════════════════════════════════════════

        app.get("/admin/metricas", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.obtenerMetricas());
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private boolean esAdmin(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }
    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }
    private int id(Context ctx, String param) {
        return Integer.parseInt(ctx.pathParam(param));
    }
}