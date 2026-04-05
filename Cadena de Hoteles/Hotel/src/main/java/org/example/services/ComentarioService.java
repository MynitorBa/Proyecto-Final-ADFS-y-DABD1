package org.example.services;

import org.example.dtos.ComentarioRequestDTO;
import org.example.dtos.ComentarioResponseDTO;
import org.example.repositories.ComentarioRepository;

import java.util.List;

/**
 * Service para la gestion de comentarios y resenas de hoteles.
 * Diferencia entre comentarios de hotel (con resena) y respuestas a comentarios (sin resena).
 */
public class ComentarioService {

    private final ComentarioRepository comentarioRepository = new ComentarioRepository();

    /**
     * Agrega un comentario o respuesta segun el contenido del request.
     * Si tiene comentarioPadreId es una respuesta, si no es un comentario de hotel con resena.
     * Valida contenido, resena y que el usuario no haya comentado ya en ese hotel.
     * @param request   datos del comentario: contenido, resena, hotelId y comentarioPadreId opcional.
     * @param usuarioId ID del usuario que escribe el comentario.
     * @return DTO con los datos del comentario recien creado.
     * @throws IllegalArgumentException si el contenido es invalido, la resena esta fuera de rango
     *                                  o el usuario ya tiene resena en ese hotel.
     */
    public ComentarioResponseDTO agregarComentario(ComentarioRequestDTO request, int usuarioId) {

        if (request.getContenido() == null || request.getContenido().isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacio");
        }
        if (request.getContenido().length() > 500) {
            throw new IllegalArgumentException("El contenido no puede superar 500 caracteres");
        }

        boolean esRespuesta = request.getComentarioPadreId() != null;

        if (esRespuesta) {
            // Respuesta a comentario: no lleva resena
            if (request.getResena() != null) {
                throw new IllegalArgumentException("Las respuestas a comentarios no llevan resena");
            }
        } else {
            // Comentario de hotel: resena obligatoria y unica por usuario/hotel
            if (request.getResena() == null) {
                throw new IllegalArgumentException("Los comentarios de hotel requieren una resena (1-5 estrellas)");
            }
            if (request.getResena() < 1 || request.getResena() > 5) {
                throw new IllegalArgumentException("La resena debe ser entre 1 y 5 estrellas");
            }
            if (comentarioRepository.existeComentarioConResena(usuarioId, request.getHotelId())) {
                throw new IllegalArgumentException("Ya tienes un comentario con resena en este hotel");
            }
        }

        int nuevoId = comentarioRepository.crearComentario(
                usuarioId,
                request.getHotelId(),
                request.getComentarioPadreId(),
                request.getResena(),
                request.getContenido()
        );

        // El rating del hotel se recalcula solo cuando es un comentario con resena
        if (!esRespuesta) {
            comentarioRepository.actualizarRatingHotel(request.getHotelId());
        }

        return comentarioRepository.obtenerComentario(nuevoId);
    }

    /**
     * Retorna todos los comentarios escritos por un usuario especifico.
     * @param usuarioId ID del usuario.
     * @return lista de comentarios del usuario.
     */
    public List<ComentarioResponseDTO> obtenerComentariosPorUsuario(int usuarioId) {
        return comentarioRepository.obtenerComentariosPorUsuario(usuarioId);
    }

    /**
     * Retorna todos los comentarios de un hotel especifico.
     * @param hotelId ID del hotel.
     * @return lista de comentarios del hotel.
     */
    public List<ComentarioResponseDTO> obtenerComentariosPorHotel(int hotelId) {
        return comentarioRepository.obtenerComentariosPorHotel(hotelId);
    }
}