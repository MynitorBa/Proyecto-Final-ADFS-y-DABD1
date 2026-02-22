package org.example.controllers;

import io.javalin.Javalin;
import org.example.dtos.EditarHabitacionRequestDTO;
import org.example.dtos.EditarHotelRequestDTO;
import org.example.dtos.SubirImagenRequestDTO;
import org.example.services.HotelService;

import java.util.Map;

public class HotelController {

    private final HotelService hotelService = new HotelService();

    public void registerRoutes(Javalin app) {

        // ── Guard helper ─────────────────────────────────────────────────────
        // Todas las rutas requieren rolId == 2 (Administrador)

        // ════════════════════════════════════════════════════
        //  HOTELES
        // ════════════════════════════════════════════════════

        // GET /admin/hoteles  →  lista todos los hoteles
        app.get("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarTodos());
        });

        // PATCH /admin/hoteles/{id}  →  edita info del hotel
        app.patch("/admin/hoteles/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            try {
                hotelService.editarHotel(hotelId, ctx.bodyAsClass(EditarHotelRequestDTO.class));
                ctx.status(200).json(Map.of("mensaje", "Hotel actualizado correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  IMÁGENES DE HOTEL
        // ════════════════════════════════════════════════════

        // POST /admin/hoteles/{id}/imagenes  →  sube una imagen al hotel
        app.post("/admin/hoteles/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            String base64 = ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64();
            try {
                ctx.status(201).json(hotelService.agregarImagenHotel(hotelId, base64));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // DELETE /admin/hoteles/imagenes/{imagenId}  →  elimina una imagen del hotel
        app.delete("/admin/hoteles/imagenes/{imagenId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int imagenId = Integer.parseInt(ctx.pathParam("imagenId"));
            hotelService.eliminarImagenHotel(imagenId);
            ctx.status(200).json(Map.of("mensaje", "Imagen eliminada correctamente"));
        });

        // ════════════════════════════════════════════════════
        //  HABITACIONES
        // ════════════════════════════════════════════════════

        // GET /admin/hoteles/{id}/habitaciones  →  lista habitaciones del hotel
        app.get("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            try {
                ctx.json(hotelService.listarHabitaciones(hotelId));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /admin/habitaciones/{id}  →  edita una habitación
        app.patch("/admin/habitaciones/{id}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int habitacionId = Integer.parseInt(ctx.pathParam("id"));
            try {
                hotelService.editarHabitacion(habitacionId, ctx.bodyAsClass(EditarHabitacionRequestDTO.class));
                ctx.status(200).json(Map.of("mensaje", "Habitación actualizada correctamente"));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // ════════════════════════════════════════════════════
        //  IMÁGENES DE HABITACIÓN
        // ════════════════════════════════════════════════════

        // POST /admin/habitaciones/{id}/imagenes  →  sube imagen a habitación
        app.post("/admin/habitaciones/{id}/imagenes", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int habitacionId = Integer.parseInt(ctx.pathParam("id"));
            String base64 = ctx.bodyAsClass(SubirImagenRequestDTO.class).getBase64();
            try {
                ctx.status(201).json(hotelService.agregarImagenHabitacion(habitacionId, base64));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // DELETE /admin/habitaciones/imagenes/{imagenId}  →  elimina imagen de habitación
        app.delete("/admin/habitaciones/imagenes/{imagenId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int imagenId = Integer.parseInt(ctx.pathParam("imagenId"));
            hotelService.eliminarImagenHabitacion(imagenId);
            ctx.status(200).json(Map.of("mensaje", "Imagen eliminada correctamente"));
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean esAdmin(io.javalin.http.Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }

    private void deny(io.javalin.http.Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }
}