package org.example.controllers;

import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import org.example.dtos.BusquedaRequestDTO;
import org.example.helpers.JwtHelper;
import org.example.services.BusquedaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BusquedaController - Tests unitarios")
class BusquedaControllerTest {

    @Mock private BusquedaService busquedaService;
    @Mock private Context ctx;
    @Mock private BusquedaRequestDTO requestDTO;
    private BusquedaController controller;

    @BeforeEach
    void setUp() {
        controller = new BusquedaController(busquedaService);
    }

    // ---- handleBuscar ----

    @Test
    @DisplayName("handleBuscar_sinToken_buscaComoAnonimo")
    void handleBuscar_sinToken_buscaComoAnonimo() {
        // arrange
        List<Object> resultados = List.of(Map.of("hotelId", 1), Map.of("hotelId", 2));
        when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.cookie("auth_token")).thenReturn(null);
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(resultados).when(busquedaService).buscar(requestDTO, null);

        // act
        controller.handleBuscar(ctx);

        // assert
        verify(busquedaService).buscar(requestDTO, null);
        verify(ctx).status(200);
        verify(ctx).json(resultados);
    }

    @Test
    @DisplayName("handleBuscar_tokenValido_buscaConUsuarioId")
    void handleBuscar_tokenValido_buscaConUsuarioId() {
        // arrange
        Claims mockClaims = mock(Claims.class);
        List<Object> resultados = List.of(Map.of("hotelId", 3));
        when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.cookie("auth_token")).thenReturn("valid-token");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(resultados).when(busquedaService).buscar(requestDTO, 7);

        try (MockedStatic<JwtHelper> mocked = mockStatic(JwtHelper.class)) {
            mocked.when(() -> JwtHelper.esValido("valid-token")).thenReturn(true);
            mocked.when(() -> JwtHelper.verificarToken("valid-token")).thenReturn(mockClaims);
            mocked.when(() -> JwtHelper.getUsuarioId(mockClaims)).thenReturn(7);

            // act
            controller.handleBuscar(ctx);

            // assert
            verify(busquedaService).buscar(requestDTO, 7);
            verify(ctx).status(200);
            verify(ctx).json(resultados);
        }
    }

    @Test
    @DisplayName("handleBuscar_servicioLanzaIllegalArgument_retorna404")
    void handleBuscar_servicioLanzaIllegalArgument_retorna404() {
        // arrange
        when(ctx.bodyAsClass(BusquedaRequestDTO.class)).thenReturn(requestDTO);
        when(ctx.cookie("auth_token")).thenReturn(null);
        when(ctx.status(404)).thenReturn(ctx);
        when(busquedaService.buscar(requestDTO, null))
                .thenThrow(new IllegalArgumentException("No se encontraron habitaciones disponibles"));

        // act
        controller.handleBuscar(ctx);

        // assert
        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "No se encontraron habitaciones disponibles"));
        verify(ctx, never()).status(200);
    }
}
