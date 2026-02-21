package org.example.services;

import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.PasswordHelper;
import org.example.repositories.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository             usuarioRepository             = new UsuarioRepository();
    private final PaisRepository                paisRepository                = new PaisRepository();
    private final CiudadRepository              ciudadRepository              = new CiudadRepository();
    private final NacionalidadRepository        nacionalidadRepository        = new NacionalidadRepository();
    private final UsuarioNacionalidadRepository usuarioNacionalidadRepository = new UsuarioNacionalidadRepository();

    // ─--------------------- Validar disponibilidad ---------------------------------

    public UsuarioValidacionResponseDTO validarDisponibilidad(UsuarioValidacionRequestDTO request) {
        boolean usernameExiste  = usuarioRepository.existeUsername(request.getUsername());
        boolean correoExiste    = usuarioRepository.existeCorreo(request.getCorreo());
        boolean pasaporteExiste = usuarioRepository.existePasaporte(request.getPasaporte());

        return new UsuarioValidacionResponseDTO(usernameExiste, correoExiste, pasaporteExiste);
    }

    // ------------------ Registrar usuario completo -------------------------

    public int registrarUsuario(UsuarioValidacionRequestDTO request) {

        UsuarioValidacionResponseDTO validacion = validarDisponibilidad(request);
        if (validacion.isUsernameExiste() || validacion.isCorreoExiste() || validacion.isPasaporteExiste()) {
            throw new CamposDuplicadosException(validacion);
        }

        int paisId = paisRepository.buscarOCrearPorNombre(request.getPais());

        int ciudadId = ciudadRepository.buscarOCrearPorNombre(request.getCiudad(), paisId);

        //Hashear contraseña
        String contrasenaHasheada = PasswordHelper.hashear(request.getContrasena());

        Date fechaNacimiento = Date.valueOf(LocalDate.parse(request.getFechaNacimiento()));

        int nuevoUsuarioId = usuarioRepository.crearUsuario(
                request.getCorreo(),
                contrasenaHasheada,
                request.getPasaporte(),
                request.getUsername(),
                request.getNombre(),
                request.getApellido(),
                request.getTelefono(),
                fechaNacimiento,
                ciudadId
        );

        if (request.getNacionalidades() != null && !request.getNacionalidades().isEmpty()) {
            List<Integer> nacionalidadIds = new ArrayList<>();
            for (String nombreNac : request.getNacionalidades()) {
                int nacId = nacionalidadRepository.buscarOCrearPorNombre(nombreNac);
                nacionalidadIds.add(nacId);
            }
            usuarioNacionalidadRepository.asignarNacionalidades(nuevoUsuarioId, nacionalidadIds);
        }

        return nuevoUsuarioId;
    }
}