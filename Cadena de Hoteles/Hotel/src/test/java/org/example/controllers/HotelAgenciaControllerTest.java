package org.example.controllers;

import io.javalin.http.Context;
import org.example.helpers.AgenciaAuthMiddleware;
import org.example.services.HotelAgenciaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HotelAgenciaController - Tests unitarios")
class HotelAgenciaControllerTest {

    @Mock private HotelAgenciaService service;
    @Mock private Context ctx;
    private HotelAgenciaController controller;

    @BeforeEach
    void setUp() {
        controller = new HotelAgenciaController(service);
    }

    @Test
    @DisplayName("handleObtenerHoteles_autenticacionValida_retornaListaHoteles")
    void handleObtenerHoteles_autenticacionValida_retornaListaHoteles() {
        // arrange
        List<Object> hoteles = List.of("Hotel X", "Hotel Y");
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(true);
            doReturn(hoteles).when(service).obtenerHotelesParaAgencia();

            // act
            controller.handleObtenerHoteles(ctx);

            // assert
            verify(ctx).json(hoteles);
        }
    }

    @Test
    @DisplayName("handleObtenerHoteles_autenticacionFallida_noRetornaDatos")
    void handleObtenerHoteles_autenticacionFallida_noRetornaDatos() {
        // arrange
        try (MockedStatic<AgenciaAuthMiddleware> mocked = mockStatic(AgenciaAuthMiddleware.class)) {
            mocked.when(() -> AgenciaAuthMiddleware.verificar(ctx)).thenReturn(false);

            // act
            controller.handleObtenerHoteles(ctx);

            // assert
            verify(ctx, never()).json(any());
            verify(service, never()).obtenerHotelesParaAgencia();
        }
    }
}
