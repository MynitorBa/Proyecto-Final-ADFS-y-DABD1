package org.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.DatabaseManager;
import org.example.dtos.ResultadoNotificacionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AgenciaNotificadorExternoService.
 * Usa MockedStatic sobre DatabaseManager para controlar las consultas SQL
 * y un HttpClient mockeado para evitar conexiones HTTP reales a sistemas externos.
 * Verifica los 4 flujos de notificacion: no es agencia, sin URL, sin token, HTTP exitoso y con error.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AgenciaNotificadorExternoService - Tests unitarios")
class AgenciaNotificadorExternoServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private AgenciaNotificadorExternoService service;
    private ObjectMapper objectMapper;

    /**
     * Fila simulada de BD con todos los datos de una agencia activa con URL y token validos.
     * Formato: [agenciaId, nombre, Token_HASH_Salida, URL_Agencia]
     */
    private static final String[] FILA_AGENCIA_COMPLETA =
            {"1", "Agencia Test", "token-hash-salida-ok", "http://agencia.test.com"};

    /** Fila con URL vacia (agencia sin URL_Agencia configurada). */
    private static final String[] FILA_AGENCIA_SIN_URL =
            {"2", "Agencia Sin URL", "", ""};

    /** Fila con token vacio (agencia sin Token_HASH_Salida configurado). */
    private static final String[] FILA_AGENCIA_SIN_TOKEN =
            {"3", "Agencia Sin Token", "", "http://agencia.sintoken.com"};

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AgenciaNotificadorExternoService(httpClient, objectMapper);
    }

    /**
     * Verifica que cuando la reservacion no pertenece a ninguna agencia, el DTO
     * retornado tiene esReservaDeAgencia=false y no se hace ninguna llamada HTTP.
     */
    @Test
    @DisplayName("notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP")
    void notificarCancelacion_noEsReservaDeAgencia_retornaEsReservaFalseYSinHTTP() throws Exception {
        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.emptyList());

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "motivo");

            assertNotNull(resultado);
            assertFalse(resultado.isEsReservaDeAgencia());
            assertFalse(resultado.isEnviado());
            assertNull(resultado.getError());
            verify(httpClient, never()).send(any(), any());
        }
    }

    /**
     * Verifica que cuando la agencia no tiene URL_Agencia configurada, el DTO
     * retorna esReservaDeAgencia=true pero con un mensaje de error y sin llamada HTTP.
     */
    @Test
    @DisplayName("notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP")
    void notificarCancelacion_agenciaSinURL_retornaErrorSinHTTP() throws Exception {
        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.singletonList(FILA_AGENCIA_SIN_URL));

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "motivo");

            assertNotNull(resultado);
            assertTrue(resultado.isEsReservaDeAgencia());
            assertFalse(resultado.isEnviado());
            assertNotNull(resultado.getError());
            assertTrue(resultado.getError().contains("URL_Agencia"));
            verify(httpClient, never()).send(any(), any());
        }
    }

    /**
     * Verifica que cuando la agencia no tiene Token_HASH_Salida configurado, el DTO
     * retorna esReservaDeAgencia=true pero con mensaje de error y sin llamada HTTP.
     */
    @Test
    @DisplayName("notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP")
    void notificarCancelacion_agenciaSinToken_retornaErrorSinHTTP() throws Exception {
        // Fila: id=3, nombre, tokenSalida vacío, URL presente
        String[] filaConUrlSinToken = {"3", "Agencia Sin Token", "", "http://agencia.sintoken.com"};

        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.singletonList(filaConUrlSinToken));

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "motivo");

            assertNotNull(resultado);
            assertTrue(resultado.isEsReservaDeAgencia());
            assertFalse(resultado.isEnviado());
            assertNotNull(resultado.getError());
            assertTrue(resultado.getError().contains("Token_HASH_Salida"));
            verify(httpClient, never()).send(any(), any());
        }
    }

    /**
     * Verifica que cuando el endpoint externo responde HTTP 200, el DTO retorna
     * enviado=true, httpStatus=200, respuestaAgencia del body, y error=null.
     */
    @Test
    @DisplayName("notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto")
    void notificarCancelacion_httpExitoso200_retornaEnviadoTrueYStatusCorrecto() throws Exception {
        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.singletonList(FILA_AGENCIA_COMPLETA));

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("{\"mensaje\":\"cancelacion recibida\"}");
            doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "cancelado por admin");

            assertNotNull(resultado);
            assertTrue(resultado.isEsReservaDeAgencia());
            assertTrue(resultado.isEnviado());
            assertEquals(200, resultado.getHttpStatus());
            assertNotNull(resultado.getRespuestaAgencia());
            assertNull(resultado.getError());
            verify(httpClient).send(any(HttpRequest.class), any());
        }
    }

    /**
     * Verifica que cuando el endpoint externo responde HTTP 500, el DTO retorna
     * enviado=true con el httpStatus 500 (no es un error de red, solo un error HTTP).
     */
    @Test
    @DisplayName("notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500")
    void notificarCancelacion_httpError500_retornaEnviadoTrueConStatus500() throws Exception {
        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.singletonList(FILA_AGENCIA_COMPLETA));

            when(httpResponse.statusCode()).thenReturn(500);
            when(httpResponse.body()).thenReturn("Internal Server Error");
            doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "motivo");

            assertNotNull(resultado);
            assertTrue(resultado.isEsReservaDeAgencia());
            assertTrue(resultado.isEnviado());
            assertEquals(500, resultado.getHttpStatus());
            verify(httpClient).send(any(), any());
        }
    }

    /**
     * Verifica que cuando el HttpClient lanza IOException (timeout, conexion rechazada),
     * el servicio captura la excepcion, retorna un DTO con error no nulo y NO propaga la excepcion.
     */
    @Test
    @DisplayName("notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar")
    void notificarCancelacion_excepcionRed_retornaErrorEnDTOSinPropagar() throws Exception {
        try (MockedStatic<DatabaseManager> mockedDB = mockStatic(DatabaseManager.class)) {
            mockedDB.when(() -> DatabaseManager.executeQuery(anyString(), any(), any()))
                    .thenReturn(Collections.singletonList(FILA_AGENCIA_COMPLETA));

            doThrow(new java.io.IOException("Connection refused")).when(httpClient).send(any(HttpRequest.class), any());

            ResultadoNotificacionDTO resultado = service.notificarCancelacion(10, "motivo");

            assertNotNull(resultado);
            assertTrue(resultado.isEsReservaDeAgencia());
            assertFalse(resultado.isEnviado());
            assertNotNull(resultado.getError());
            assertTrue(resultado.getError().contains("Connection refused"));
        }
    }
}
