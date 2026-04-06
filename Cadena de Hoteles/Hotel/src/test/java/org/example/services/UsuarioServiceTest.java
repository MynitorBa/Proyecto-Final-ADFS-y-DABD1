package org.example.services;

import org.example.dtos.UsuarioAdminDTO;
import org.example.dtos.UsuarioPerfilResponseDTO;
import org.example.dtos.UsuarioValidacionRequestDTO;
import org.example.dtos.UsuarioValidacionResponseDTO;
import org.example.helpers.CamposDuplicadosException;
import org.example.helpers.CredencialesInvalidasException;
import org.example.helpers.PasswordHelper;
import org.example.repositories.CiudadRepository;
import org.example.repositories.NacionalidadRepository;
import org.example.repositories.PaisRepository;
import org.example.repositories.UsuarioNacionalidadRepository;
import org.example.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService.
 * Verifica el comportamiento de registro, validacion, perfil,
 * actualizacion de datos y operaciones de rol sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Pruebas unitarias")
class UsuarioServiceTest {

    @Mock private UsuarioRepository             usuarioRepository;
    @Mock private PaisRepository                paisRepository;
    @Mock private CiudadRepository              ciudadRepository;
    @Mock private NacionalidadRepository        nacionalidadRepository;
    @Mock private UsuarioNacionalidadRepository usuarioNacionalidadRepository;

    private UsuarioService service;

    /**
     * Inicializa el service con los mocks antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new UsuarioService(
                usuarioRepository,
                paisRepository,
                ciudadRepository,
                nacionalidadRepository,
                usuarioNacionalidadRepository
        );
    }

    /**
     * Verifica que todos los campos retornen false cuando estan disponibles.
     */
    @Test
    @DisplayName("Todos los campos disponibles retorna todo en false")
    void validarDisponibilidad_todosLibres() {
        UsuarioValidacionRequestDTO req = buildRequest();
        when(usuarioRepository.existeUsername(req.getUsername())).thenReturn(false);
        when(usuarioRepository.existeCorreo(req.getCorreo())).thenReturn(false);
        when(usuarioRepository.existePasaporte(req.getPasaporte())).thenReturn(false);

        UsuarioValidacionResponseDTO resp = service.validarDisponibilidad(req);

        assertFalse(resp.isUsernameExiste());
        assertFalse(resp.isCorreoExiste());
        assertFalse(resp.isPasaporteExiste());
    }

    /**
     * Verifica que usernameExiste sea true cuando el username ya esta registrado.
     */
    @Test
    @DisplayName("Username ocupado retorna usernameExiste en true")
    void validarDisponibilidad_usernameOcupado() {
        UsuarioValidacionRequestDTO req = buildRequest();
        when(usuarioRepository.existeUsername(req.getUsername())).thenReturn(true);
        when(usuarioRepository.existeCorreo(req.getCorreo())).thenReturn(false);
        when(usuarioRepository.existePasaporte(req.getPasaporte())).thenReturn(false);

        UsuarioValidacionResponseDTO resp = service.validarDisponibilidad(req);

        assertTrue(resp.isUsernameExiste());
    }

    /**
     * Verifica que se lance CamposDuplicadosException si el username ya existe.
     */
    @Test
    @DisplayName("Username duplicado lanza CamposDuplicadosException")
    void registrarUsuario_usernameDuplicado_lanzaExcepcion() {
        UsuarioValidacionRequestDTO req = buildRequest();
        when(usuarioRepository.existeUsername(req.getUsername())).thenReturn(true);
        when(usuarioRepository.existeCorreo(req.getCorreo())).thenReturn(false);
        when(usuarioRepository.existePasaporte(req.getPasaporte())).thenReturn(false);

        assertThrows(CamposDuplicadosException.class, () -> service.registrarUsuario(req));
    }

    /**
     * Verifica que un registro exitoso retorne el ID del usuario creado.
     */
    @Test
    @DisplayName("Registro exitoso retorna el ID del nuevo usuario")
    void registrarUsuario_exitoso_retornaId() {
        UsuarioValidacionRequestDTO req = buildRequest();
        when(usuarioRepository.existeUsername(req.getUsername())).thenReturn(false);
        when(usuarioRepository.existeCorreo(req.getCorreo())).thenReturn(false);
        when(usuarioRepository.existePasaporte(req.getPasaporte())).thenReturn(false);
        when(paisRepository.buscarOCrearPorNombre(req.getPais())).thenReturn(1);
        when(ciudadRepository.buscarOCrearPorNombre(req.getCiudad(), 1)).thenReturn(2);
        when(usuarioRepository.crearUsuario(any(), any(), any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(99);

        int id = service.registrarUsuario(req);

        assertEquals(99, id);
    }

    /**
     * Verifica que obtenerPerfil lance RuntimeException si el usuario no existe.
     */
    @Test
    @DisplayName("Perfil de usuario inexistente lanza RuntimeException")
    void obtenerPerfil_noExiste_lanzaExcepcion() {
        when(usuarioRepository.obtenerPerfil(999)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.obtenerPerfil(999));
    }

    /**
     * Verifica que obtenerPerfil retorne el perfil con sus nacionalidades correctamente.
     */
    @Test
    @DisplayName("Perfil existente retorna datos con nacionalidades")
    void obtenerPerfil_existente_retornaPerfil() {
        UsuarioPerfilResponseDTO perfil = new UsuarioPerfilResponseDTO();
        when(usuarioRepository.obtenerPerfil(1)).thenReturn(perfil);
        when(usuarioRepository.obtenerNacionalidades(1)).thenReturn(List.of("Guatemala", "Mexico"));

        UsuarioPerfilResponseDTO resultado = service.obtenerPerfil(1);

        assertNotNull(resultado);
        assertEquals(2, resultado.getNacionalidades().size());
    }

    /**
     * Verifica que cambiarTelefono lance IllegalArgumentException si el telefono esta vacio.
     */
    @Test
    @DisplayName("Telefono vacio lanza IllegalArgumentException")
    void cambiarTelefono_vacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarTelefono(1, "   "));
    }

    /**
     * Verifica que cambiarTelefono llame al repositorio con el numero correcto.
     */
    @Test
    @DisplayName("Telefono valido llama al repositorio correctamente")
    void cambiarTelefono_valido_actualizaRepositorio() {
        service.cambiarTelefono(1, "+502 12345678");

        verify(usuarioRepository).actualizarTelefono(1, "+502 12345678");
    }

    /**
     * Verifica que cambiarRol lance IllegalArgumentException con un rol inexistente.
     */
    @Test
    @DisplayName("Rol invalido lanza IllegalArgumentException")
    void cambiarRol_invalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarRol(1, 99));
    }

    /**
     * Verifica que cambiarRol llame al repositorio con el rol correcto.
     */
    @Test
    @DisplayName("Rol valido llama al repositorio correctamente")
    void cambiarRol_valido_actualizaRepositorio() {
        service.cambiarRol(1, 2);

        verify(usuarioRepository).actualizarRol(1, 2);
    }

    /**
     * Verifica que cambiarContrasena lance CredencialesInvalidasException cuando
     * la contrasena actual proporcionada no coincide con el hash almacenado.
     */
    @Test
    @DisplayName("Contrasena incorrecta lanza CredencialesInvalidasException")
    void cambiarContrasena_contrasenaIncorrecta_lanzaExcepcion() {
        String hashReal = PasswordHelper.hashear("passwordCorrecto");
        when(usuarioRepository.obtenerContrasena(1)).thenReturn(hashReal);

        assertThrows(
                CredencialesInvalidasException.class,
                () -> service.cambiarContrasena(1, "passwordIncorrecto", "nuevo")
        );
    }

    /**
     * Verifica que listarTodosUsuarios delegue al repositorio y retorne
     * la lista de usuarios con su rol.
     */
    @Test
    @DisplayName("listarTodosUsuarios retorna la lista del repositorio")
    void listarTodosUsuarios_retornaLista() {
        when(usuarioRepository.listarTodosConRol()).thenReturn(List.of(new UsuarioAdminDTO()));

        List<UsuarioAdminDTO> result = service.listarTodosUsuarios();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Construye un UsuarioValidacionRequestDTO con datos de prueba validos.
     * @return DTO listo para usar en los tests.
     */
    private UsuarioValidacionRequestDTO buildRequest() {
        UsuarioValidacionRequestDTO req = new UsuarioValidacionRequestDTO();
        req.setUsername("memitos");
        req.setCorreo("memitos@test.com");
        req.setPasaporte("GT123456");
        req.setNombre("Memo");
        req.setApellido("Garcia");
        req.setContrasena("Pass1234");
        req.setTelefono("+502 12345678");
        req.setFechaNacimiento("2000-01-15");
        req.setPais("Guatemala");
        req.setCiudad("Guatemala City");
        return req;
    }
}
