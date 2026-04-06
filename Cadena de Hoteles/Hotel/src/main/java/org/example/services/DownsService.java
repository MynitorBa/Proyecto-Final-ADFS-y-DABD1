package org.example.services;

import org.example.dtos.DownResponseDTO;
import org.example.repositories.ComentarioRepository;
import org.example.repositories.DownsRepository;

import java.util.List;

/**
 * Service para la gestion de downs (votos) en comentarios.
 * Un down puede ser positivo (1) o negativo (-1) y cada usuario
 * solo puede tener un down por comentario.
 */
public class DownsService {

    private final DownsRepository      downsRepository;
    private final ComentarioRepository comentarioRepository;

    /**
     * Crea una instancia de DownsService con sus dependencias inyectadas.
     */
    public DownsService(DownsRepository downsRepository,
                        ComentarioRepository comentarioRepository) {
        this.downsRepository      = downsRepository;
        this.comentarioRepository = comentarioRepository;
    }

    /**
     * Agrega un down de un usuario a un comentario.
     * Valida que el valor sea 1 o -1, que el comentario exista
     * y que el usuario no haya votado ya en ese comentario.
     * @param comentarioId ID del comentario a votar.
     * @param usuarioId    ID del usuario que vota.
     * @param valor        1 para voto positivo, -1 para voto negativo.
     * @throws IllegalArgumentException si el valor es invalido, el comentario no existe
     *                                  o el usuario ya tiene un down en ese comentario.
     */
    public void agregarDown(int comentarioId, int usuarioId, int valor) {
        if (valor != 1 && valor != -1) {
            throw new IllegalArgumentException("El valor del down debe ser 1 o -1");
        }
        if (comentarioRepository.obtenerComentario(comentarioId) == null) {
            throw new IllegalArgumentException("El comentario no existe");
        }
        if (downsRepository.obtenerValorDown(usuarioId, comentarioId) != null) {
            throw new IllegalArgumentException("Ya tienes un down en este comentario, usa actualizar");
        }
        downsRepository.insertarDown(usuarioId, comentarioId, valor);
        downsRepository.actualizarContadorDown(comentarioId, valor);
    }

    /**
     * Elimina el down de un usuario en un comentario y ajusta el contador.
     * @param comentarioId ID del comentario.
     * @param usuarioId    ID del usuario cuyo down se va a eliminar.
     * @throws IllegalArgumentException si el usuario no tiene down en ese comentario.
     */
    public void eliminarDown(int comentarioId, int usuarioId) {
        Integer valorExistente = downsRepository.obtenerValorDown(usuarioId, comentarioId);
        if (valorExistente == null) {
            throw new IllegalArgumentException("No tienes ningun down en este comentario");
        }
        downsRepository.actualizarContadorDown(comentarioId, -valorExistente);
        downsRepository.eliminarDown(usuarioId, comentarioId);
    }

    /**
     * Cambia el valor del down existente de un usuario en un comentario.
     * Elimina el voto anterior e inserta el nuevo ajustando el contador en ambos pasos.
     * @param comentarioId ID del comentario.
     * @param usuarioId    ID del usuario que actualiza su voto.
     * @param nuevoValor   nuevo valor: 1 o -1.
     * @throws IllegalArgumentException si el valor es invalido, el usuario no tiene down
     *                                  o el nuevo valor es igual al actual.
     */
    public void actualizarDown(int comentarioId, int usuarioId, int nuevoValor) {
        if (nuevoValor != 1 && nuevoValor != -1) {
            throw new IllegalArgumentException("El valor del down debe ser 1 o -1");
        }
        Integer valorExistente = downsRepository.obtenerValorDown(usuarioId, comentarioId);
        if (valorExistente == null) {
            throw new IllegalArgumentException("No tienes ningun down en este comentario, usa agregar");
        }
        if (valorExistente == nuevoValor) {
            throw new IllegalArgumentException("El down ya tiene ese valor");
        }
        downsRepository.actualizarContadorDown(comentarioId, -valorExistente);
        downsRepository.eliminarDown(usuarioId, comentarioId);
        downsRepository.insertarDown(usuarioId, comentarioId, nuevoValor);
        downsRepository.actualizarContadorDown(comentarioId, nuevoValor);
    }

    /**
     * Retorna todos los downs registrados por un usuario.
     * @param usuarioId ID del usuario.
     * @return lista de downs del usuario.
     */
    public List<DownResponseDTO> obtenerDownsDeUsuario(int usuarioId) {
        return downsRepository.obtenerDownsDeUsuario(usuarioId);
    }

    /**
     * Retorna los downs de un usuario filtrados por hotel.
     * Util para saber como voto el usuario en los comentarios de un hotel especifico.
     * @param usuarioId ID del usuario.
     * @param hotelId   ID del hotel a filtrar.
     * @return lista de downs del usuario en comentarios de ese hotel.
     */
    public List<DownResponseDTO> obtenerDownsDeUsuarioPorHotel(int usuarioId, int hotelId) {
        return downsRepository.obtenerDownsDeUsuarioPorHotel(usuarioId, hotelId);
    }
}