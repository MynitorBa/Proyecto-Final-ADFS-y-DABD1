package org.example.controllers;

import io.javalin.http.Context;
import org.example.services.DestinosService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DestinosController - Tests unitarios")
class DestinosControllerTest {

    @Mock private DestinosService service;
    @Mock private Context ctx;
    private DestinosController controller;

    @BeforeEach
    void setUp() {
        controller = new DestinosController(service);
    }

    @Test
    @DisplayName("handleObtenerDestinos_conDestinosExistentes_retorna200ConLista")
    void handleObtenerDestinos_conDestinosExistentes_retorna200ConLista() {
        // arrange
        List<Object> destinos = List.of("Hotel A", "Hotel B");
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(destinos).when(service).obtenerDestinos();

        // act
        controller.handleObtenerDestinos(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(destinos);
    }

    @Test
    @DisplayName("handleObtenerDestinos_sinDestinos_retorna200ConListaVacia")
    void handleObtenerDestinos_sinDestinos_retorna200ConListaVacia() {
        // arrange
        List<Object> destinos = Collections.emptyList();
        when(ctx.status(200)).thenReturn(ctx);
        doReturn(destinos).when(service).obtenerDestinos();

        // act
        controller.handleObtenerDestinos(ctx);

        // assert
        verify(ctx).status(200);
        verify(ctx).json(destinos);
    }
}
