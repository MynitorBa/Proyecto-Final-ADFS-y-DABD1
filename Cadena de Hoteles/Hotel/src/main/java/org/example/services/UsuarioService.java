package org.example.services;

import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.repositories.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public UsuarioValidacionResponseDTO validarDisponibilidad(UsuarioValidacionRequestDTO request) {
        boolean usernameExiste  = usuarioRepository.existeUsername(request.getUsername());
        boolean correoExiste    = usuarioRepository.existeCorreo(request.getCorreo());
        boolean pasaporteExiste = usuarioRepository.existePasaporte(request.getPasaporte());

        return new UsuarioValidacionResponseDTO(usernameExiste, correoExiste, pasaporteExiste);
    }
}