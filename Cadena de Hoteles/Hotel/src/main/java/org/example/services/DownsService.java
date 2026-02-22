package org.example.services;

import org.example.dtos.DownResponseDTO;
import org.example.repositories.ComentarioRepository;
import org.example.repositories.DownsRepository;

import java.util.List;

public class DownsService {

    private final DownsRepository      downsRepository      = new DownsRepository();
    private final ComentarioRepository comentarioRepository = new ComentarioRepository();

    //Agregar down

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

    //eliminar down

    public void eliminarDown(int comentarioId, int usuarioId) {
        Integer valorExistente = downsRepository.obtenerValorDown(usuarioId, comentarioId);
        if (valorExistente == null) {
            throw new IllegalArgumentException("No tienes ningún down en este comentario");
        }
        downsRepository.actualizarContadorDown(comentarioId, -valorExistente);
        downsRepository.eliminarDown(usuarioId, comentarioId);
    }

    // Actualizar down

    public void actualizarDown(int comentarioId, int usuarioId, int nuevoValor) {
        if (nuevoValor != 1 && nuevoValor != -1) {
            throw new IllegalArgumentException("El valor del down debe ser 1 o -1");
        }
        Integer valorExistente = downsRepository.obtenerValorDown(usuarioId, comentarioId);
        if (valorExistente == null) {
            throw new IllegalArgumentException("No tienes ningún down en este comentario, usa agregar");
        }
        if (valorExistente == nuevoValor) {
            throw new IllegalArgumentException("El down ya tiene ese valor");
        }
        downsRepository.actualizarContadorDown(comentarioId, -valorExistente);
        downsRepository.eliminarDown(usuarioId, comentarioId);
        downsRepository.insertarDown(usuarioId, comentarioId, nuevoValor);
        downsRepository.actualizarContadorDown(comentarioId, nuevoValor);
    }

    // Obtener todos los downs del usuario

    public List<DownResponseDTO> obtenerDownsDeUsuario(int usuarioId) {
        return downsRepository.obtenerDownsDeUsuario(usuarioId);
    }

    // Obtener downs del usuario filtrados por hotel

    public List<DownResponseDTO> obtenerDownsDeUsuarioPorHotel(int usuarioId, int hotelId) {
        return downsRepository.obtenerDownsDeUsuarioPorHotel(usuarioId, hotelId);
    }
}