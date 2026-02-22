package org.example.services;

import org.example.dtos.ComentarioRequestDTO;
import org.example.dtos.ComentarioResponseDTO;
import org.example.repositories.ComentarioRepository;

import java.util.List;

public class ComentarioService {

    private final ComentarioRepository comentarioRepository = new ComentarioRepository();

    public ComentarioResponseDTO agregarComentario(ComentarioRequestDTO request, int usuarioId) {

        if (request.getContenido() == null || request.getContenido().isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío");
        }
        if (request.getContenido().length() > 500) {
            throw new IllegalArgumentException("El contenido no puede superar 500 caracteres");
        }

        boolean esRespuesta = request.getComentarioPadreId() != null;

        if (esRespuesta) {
            // Respuesta a comentario — sin reseña, sin límite
            if (request.getResena() != null) {
                throw new IllegalArgumentException("Las respuestas a comentarios no llevan reseña");
            }
        } else {
            // Comentario de hotel — reseña obligatoria y único por usuario/hotel
            if (request.getResena() == null) {
                throw new IllegalArgumentException("Los comentarios de hotel requieren una reseña (1-5 estrellas)");
            }
            if (request.getResena() < 1 || request.getResena() > 5) {
                throw new IllegalArgumentException("La reseña debe ser entre 1 y 5 estrellas");
            }
            if (comentarioRepository.existeComentarioConResena(usuarioId, request.getHotelId())) {
                throw new IllegalArgumentException("Ya tienes un comentario con reseña en este hotel");
            }
        }

        int nuevoId = comentarioRepository.crearComentario(
                usuarioId,
                request.getHotelId(),
                request.getComentarioPadreId(),
                request.getResena(),
                request.getContenido()
        );

        // Recalcular rating solo en comentarios de hotel
        if (!esRespuesta) {
            comentarioRepository.actualizarRatingHotel(request.getHotelId());
        }

        return comentarioRepository.obtenerComentario(nuevoId);
    }

    public List<ComentarioResponseDTO> obtenerComentariosPorUsuario(int usuarioId) {
        return comentarioRepository.obtenerComentariosPorUsuario(usuarioId);
    }

    public List<ComentarioResponseDTO> obtenerComentariosPorHotel(int hotelId) {
        return comentarioRepository.obtenerComentariosPorHotel(hotelId);
    }
}