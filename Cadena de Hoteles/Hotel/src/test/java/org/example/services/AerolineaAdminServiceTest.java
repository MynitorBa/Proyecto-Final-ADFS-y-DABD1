package org.example.services;

import org.example.dtos.AerolineaAdminDTO;
import org.example.dtos.CrearAerolineaAdminRequestDTO;
import org.example.dtos.EditarAerolineaRequestDTO;
import org.example.dtos.UsuarioWebserviceLibreDTO;
import org.example.repositories.AerolineaAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AerolineaAdminService.
 * Mockea AerolineaAdminRepository para aislar la logica de negocio
 * del panel de administracion de aerolineas aliadas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AerolineaAdminService - Tests unitarios")
class AerolineaAdminServiceTest {

    @Mock
    private AerolineaAdminRepository repo;

    private AerolineaAdminService service;

    @BeforeEach
    void setUp() {
        service = new AerolineaAdminService(repo);
    }

    // -- listarTodas

    /**
     * Verifica que listarTodas retorne la lista completa de aerolineas del repositorio.
     */
    @Test
    @DisplayName("listarTodas_conAerolineas_retornaListaDelRepo")
    void listarTodas_conAerolineas_retornaListaDelRepo() {
        AerolineaAdminDTO dto1 = new AerolineaAdminDTO();
        dto1.setId(1);
        dto1.setNombre("AeroTest S.A.");
        AerolineaAdminDTO dto2 = new AerolineaAdminDTO();
        dto2.setId(2);
        dto2.setNombre("Vuelos Express");
        List<AerolineaAdminDTO> esperada = List.of(dto1, dto2);

        when(repo.listarTodas()).thenReturn(esperada);

        List<AerolineaAdminDTO> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(esperada, resultado);
        verify(repo).listarTodas();
    }

    /**
     * Verifica que listarTodas retorne lista vacia cuando no hay aerolineas registradas.
     */
    @Test
    @DisplayName("listarTodas_sinAerolineas_retornaListaVacia")
    void listarTodas_sinAerolineas_retornaListaVacia() {
        when(repo.listarTodas()).thenReturn(Collections.emptyList());

        List<AerolineaAdminDTO> resultado = service.listarTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarTodas();
    }

    // -- crear

    /**
     * Verifica que crear delegue al repositorio y retorne el DTO de la aerolinea creada.
     */
    @Test
    @DisplayName("crear_requestValido_retornaAerolineaCreadaDelRepo")
    void crear_requestValido_retornaAerolineaCreadaDelRepo() {
        CrearAerolineaAdminRequestDTO req = new CrearAerolineaAdminRequestDTO();
        req.setNombre("Nueva Aerolinea");
        req.setUrl("https://nueva.com/api");
        req.setUrlParaUsuario("https://nueva.com");
        req.setUsuarioWebisId(5);

        AerolineaAdminDTO creada = new AerolineaAdminDTO();
        creada.setId(10);
        creada.setNombre("Nueva Aerolinea");
        creada.setEstadoId(1);
        creada.setEstado("Activo");

        when(repo.crear(req)).thenReturn(creada);

        AerolineaAdminDTO resultado = service.crear(req);

        assertNotNull(resultado);
        assertEquals(10, resultado.getId());
        assertEquals("Nueva Aerolinea", resultado.getNombre());
        assertEquals("Activo", resultado.getEstado());
        verify(repo).crear(req);
    }

    // -- editar

    /**
     * Verifica que editar delegue al repositorio con el ID y request correctos.
     */
    @Test
    @DisplayName("editar_requestValido_delegaAlRepo")
    void editar_requestValido_delegaAlRepo() {
        EditarAerolineaRequestDTO req = new EditarAerolineaRequestDTO();
        req.setNombre("Aerolinea Editada");
        req.setUrl("https://editada.com/api");
        req.setUrlParaUsuario("https://editada.com");
        req.setPorcentajeDescuento(15.0);
        req.setEstadoId(1);

        service.editar(7, req);

        verify(repo).editar(7, req);
    }

    /**
     * Verifica que editar con ID distinto delegue correctamente al repositorio.
     */
    @Test
    @DisplayName("editar_idDistinto_invocaRepoConIdCorrecto")
    void editar_idDistinto_invocaRepoConIdCorrecto() {
        EditarAerolineaRequestDTO req = new EditarAerolineaRequestDTO();
        req.setNombre("Otra Aerolinea");
        req.setUrl("https://otra.com/api");
        req.setUrlParaUsuario("https://otra.com");
        req.setPorcentajeDescuento(5.0);
        req.setEstadoId(2);

        service.editar(42, req);

        verify(repo).editar(42, req);
        verify(repo, never()).editar(eq(7), any());
    }

    // -- listarWebserviceLibres

    /**
     * Verifica que listarWebserviceLibres retorne los usuarios disponibles del repositorio.
     */
    @Test
    @DisplayName("listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo")
    void listarWebserviceLibres_conUsuariosDisponibles_retornaListaDelRepo() {
        UsuarioWebserviceLibreDTO u1 = new UsuarioWebserviceLibreDTO();
        u1.setId(3);
        u1.setUsername("ws_libre_01");
        UsuarioWebserviceLibreDTO u2 = new UsuarioWebserviceLibreDTO();
        u2.setId(4);
        u2.setUsername("ws_libre_02");
        List<UsuarioWebserviceLibreDTO> esperada = List.of(u1, u2);

        when(repo.listarWebserviceLibres()).thenReturn(esperada);

        List<UsuarioWebserviceLibreDTO> resultado = service.listarWebserviceLibres();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(repo).listarWebserviceLibres();
    }

    /**
     * Verifica que listarWebserviceLibres retorne lista vacia cuando todos los
     * usuarios ya tienen una entidad asignada.
     */
    @Test
    @DisplayName("listarWebserviceLibres_todosAsignados_retornaListaVacia")
    void listarWebserviceLibres_todosAsignados_retornaListaVacia() {
        when(repo.listarWebserviceLibres()).thenReturn(Collections.emptyList());

        List<UsuarioWebserviceLibreDTO> resultado = service.listarWebserviceLibres();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repo).listarWebserviceLibres();
    }
}
