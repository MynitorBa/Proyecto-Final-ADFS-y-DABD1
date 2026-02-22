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
        //  CATÁLOGOS (para los dropdowns del frontend)
        // ════════════════════════════════════════════════════

        app.get("/admin/paises",   ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarPaises()); });
        app.get("/admin/ciudades", ctx -> { if (!esAdmin(ctx)) { deny(ctx); return; } ctx.json(hotelService.listarCiudades()); });

        // ════════════════════════════════════════════════════
        //  HOTELES
        // ════════════════════════════════════════════════════

        // GET /admin/hoteles
        app.get("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            ctx.json(hotelService.listarTodos());
        });

        // POST /admin/hoteles  →  crear hotel
        app.post("/admin/hoteles", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            try {
                ctx.status(201).json(hotelService.crearHotel(ctx.bodyAsClass(CrearHotelRequestDTO.class)));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /admin/hoteles/{id}  →  editar hotel
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

        app.delete("/admin/hoteles/imagenes/{imagenId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHotel(Integer.parseInt(ctx.pathParam("imagenId")));
            ctx.status(200).json(Map.of("mensaje", "Imagen eliminada correctamente"));
        });

        // ════════════════════════════════════════════════════
        //  HABITACIONES
        // ════════════════════════════════════════════════════

        // GET /admin/hoteles/{id}/habitaciones
        app.get("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            try {
                ctx.json(hotelService.listarHabitaciones(hotelId));
            } catch (IllegalArgumentException e) {
                ctx.status(404).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // POST /admin/hoteles/{id}/habitaciones  →  crear habitación
        app.post("/admin/hoteles/{id}/habitaciones", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            int hotelId = Integer.parseInt(ctx.pathParam("id"));
            try {
                CrearHabitacionRequestDTO req = ctx.bodyAsClass(CrearHabitacionRequestDTO.class);
                req.setHotelId(hotelId);   // asegurar que viene del path
                ctx.status(201).json(hotelService.crearHabitacion(req));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(Map.of("mensaje", e.getMessage()));
            }
        });

        // PATCH /admin/habitaciones/{id}
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

        app.delete("/admin/habitaciones/imagenes/{imagenId}", ctx -> {
            if (!esAdmin(ctx)) { deny(ctx); return; }
            hotelService.eliminarImagenHabitacion(Integer.parseInt(ctx.pathParam("imagenId")));
            ctx.status(200).json(Map.of("mensaje", "Imagen eliminada correctamente"));
        });
    }

    private boolean esAdmin(Context ctx) {
        Integer rolId = ctx.attribute("rolId");
        return rolId != null && rolId == 2;
    }

    private void deny(Context ctx) {
        ctx.status(403).json(Map.of("mensaje", "Acceso denegado: se requiere rol Administrador"));
    }
}