package org.example.services;

import org.example.dtos.SesionDTO;
import org.example.repositories.SesionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para SesionService.
 * Verifica la construccion del DTO de sesion activa y la sesion vacia
 * sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SesionService — Pruebas unitarias")
class SesionServiceTest {

    @Mock private SesionRepository sesionRepository;

    private SesionService service;

    /**
     * Inicializa el service con el mock antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new SesionService(sesionRepository);
    }

    // -- obtenerSesion

    /**
     * Verifica que obtenerSesion construya el DTO con todos los campos correctos
     * y autenticado en true.
     */
    @Test
    @DisplayName("obtenerSesion retorna DTO con datos del usuario y autenticado true")
    void obtenerSesion_datosValidos_retornaDtoCompleto() {
        when(sesionRepository.obtenerNombreRol(1)).thenReturn("Usuario");

        SesionDTO dto = service.obtenerSesion(5, "testUser", 1);

        assertNotNull(dto);
        assertEquals(5,          dto.getUsuarioId());
        assertEquals("testUser", dto.getUsername());
        assertEquals(1,          dto.getRolId());
        assertEquals("Usuario",  dto.getRol());
        assertTrue(dto.isAutenticado());
    }

    /**
     * Verifica que obtenerSesion consulte el nombre del rol en el repositorio.
     */
    @Test
    @DisplayName("obtenerSesion consulta el nombre del rol en el repositorio")
    void obtenerSesion_rolConsultado_invocaRepositorio() {
        when(sesionRepository.obtenerNombreRol(2)).thenReturn("Administrador");

        service.obtenerSesion(3, "adminUser", 2);

        verify(sesionRepository).obtenerNombreRol(2);
    }

    // -- sinSesion

    /**
     * Verifica que sinSesion retorne un DTO con autenticado en false
     * sin realizar ninguna llamada al repositorio.
     */
    @Test
    @DisplayName("sinSesion retorna DTO con autenticado false y sin llamar al repositorio")
    void sinSesion_siempre_retornaDtoNoAutenticado() {
        SesionDTO dto = service.sinSesion();

        assertNotNull(dto);
        assertFalse(dto.isAutenticado());
        verifyNoInteractions(sesionRepository);
    }

    /**
     * Verifica que sinSesion no establezca un ID de usuario ni un rol en el DTO.
     */
    @Test
    @DisplayName("sinSesion retorna DTO con usuarioId cero y rol null")
    void sinSesion_siempre_retornaDtoSinDatosDeUsuario() {
        SesionDTO dto = service.sinSesion();

        assertEquals(0,    dto.getUsuarioId());
        assertNull(dto.getRol());
    }
}
