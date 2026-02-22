package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.dtos.*;
import org.example.services.HotelService;

import java.util.Map;

public class HotelController {

    private final HotelService hotelService = new HotelService();

    public void registerRoutes(Javalin app) {

        // ════════════════════════════════════════════════════
        //  CATÁLOGOS
        // ════════════════════════════════════════════════════
        app.get("/admin/amenidades", ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarAmenidades()); });
        app.get("/admin/paises",     ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarPaises()); });
        app.get("/admin/ciudades",   ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarCiudades()); });

        // ════════════════════════════════════════════════════
        //  HOTELES
        // ════════════════════════════════════════════════════

        // GET  /admin/hoteles
        app.get("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarTodos());
        });

        // POST /admin/hoteles  →  crear
        app.post("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.crearHotel(ctx.bodyAsClass(CrearHotelRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /admin/hoteles/{id}  →  editar
        app.patch("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHotel(id(ctx, "id"), ctx.bodyAsClass(EditarHotelRequestDTO.class));
                ctx.json(Map.of("mensaje", "Hotel actualizado"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
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

        // GET  /admin/hoteles/{id}/amenidades
        app.get("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarAmenidadesHotel(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // POST /admin/hoteles/{id}/amenidades  →  agregar amenidad
        app.post("/admin/hoteles/{id}/amenidades", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarAmenidadHotel(
                        id(ctx, "id"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /admin/hoteles/amenidades/{haId}  →  editar descripción
        app.patch("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.actualizarAmenidadHotel(id(ctx, "haId"), ctx.bodyAsClass(AgregarAmenidadRequestDTO.class));
            ctx.json(Map.of("mensaje", "Amenidad actualizada"));
        });

        // DELETE /admin/hoteles/amenidades/{haId}
        app.delete("/admin/hoteles/amenidades/{haId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarAmenidadHotel(id(ctx, "haId"));
            ctx.json(Map.of("mensaje", "Amenidad eliminada"));
        });

        // POST /admin/hoteles/amenidades/{haId}/imagenes
        app.post("/admin/hoteles/amenidades/{haId}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.agregarImagenAmenidad(
                        id(ctx, "haId"), ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64()));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // DELETE /admin/hoteles/amenidades/imagenes/{imgId}
        app.delete("/admin/hoteles/amenidades/imagenes/{imgId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenAmenidad(id(ctx, "imgId"));
            ctx.json(Map.of("mensaje", "Imagen de amenidad eliminada"));
        });

        // ════════════════════════════════════════════════════
        //  HABITACIONES
        // ════════════════════════════════════════════════════

        // GET  /admin/hoteles/{id}/habitaciones
        app.get("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.json(hotelService.listarHabitaciones(id(ctx, "id")));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // POST /admin/hoteles/{id}/habitaciones  →  crear
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

        // PATCH /admin/habitaciones/{id}  →  editar
        app.patch("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                hotelService.editarHabitacion(id(ctx, "id"), ctx.bodyAsClass(EditarHabitacionRequestDTO.class));
                ctx.json(Map.of("mensaje", "Habitación actualizada"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
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
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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