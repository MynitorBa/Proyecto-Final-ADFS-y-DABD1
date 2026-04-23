package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.*;
import org.example.services.AdminReservacionService;
import org.example.services.HotelService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HotelController - Tests unitarios")
class HotelControllerTest {

    @Mock private HotelService            hotelService;
    @Mock private AdminReservacionService adminReservacionService;
    @Mock private Context                 ctx;

    private HotelController controller;

    @BeforeEach
    void setUp() {
        controller = new HotelController(hotelService, adminReservacionService);
    }

    // =========================================================================
    // 1. handleListarAmenidades
    // =========================================================================

    @Test
    @DisplayName("handleListarAmenidades - rol admin - retorna lista del servicio")
    void handleListarAmenidades_rolAdmin_retornaListaDelServicio() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(List.of()).when(hotelService).listarAmenidades();

        controller.handleListarAmenidades(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarAmenidades();
    }

    @Test
    @DisplayName("handleListarAmenidades - sin rol admin - retorna 403")
    void handleListarAmenidades_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarAmenidades(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarAmenidades();
    }

    // =========================================================================
    // 2. handleCrearAmenidad
    // =========================================================================

    @Test
    @DisplayName("handleCrearAmenidad - rol admin - crea amenidad y retorna 201")
    void handleCrearAmenidad_rolAdmin_creaAmenidadYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        Map<String, Object> body = Map.of("nombre", "Piscina");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        doReturn(null).when(hotelService).crearAmenidad("Piscina");
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleCrearAmenidad(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).crearAmenidad("Piscina");
    }

    @Test
    @DisplayName("handleCrearAmenidad - sin rol admin - retorna 403")
    void handleCrearAmenidad_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(1);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCrearAmenidad(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).crearAmenidad(any());
    }

    @Test
    @DisplayName("handleCrearAmenidad - servicio lanza IllegalArgumentException - retorna 400")
    void handleCrearAmenidad_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        Map<String, Object> body = Map.of("nombre", "");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(hotelService.crearAmenidad("")).thenThrow(new IllegalArgumentException("Nombre invalido"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCrearAmenidad(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Nombre invalido"));
    }

    // =========================================================================
    // 3. handleListarPaises
    // =========================================================================

    @Test
    @DisplayName("handleListarPaises - rol admin - retorna lista de paises")
    void handleListarPaises_rolAdmin_retornaListaDePaises() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(List.of()).when(hotelService).listarPaises();

        controller.handleListarPaises(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarPaises();
    }

    @Test
    @DisplayName("handleListarPaises - sin rol admin - retorna 403")
    void handleListarPaises_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarPaises(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarPaises();
    }

    // =========================================================================
    // 4. handleListarCiudades
    // =========================================================================

    @Test
    @DisplayName("handleListarCiudades - rol admin - retorna lista de ciudades")
    void handleListarCiudades_rolAdmin_retornaListaDeCiudades() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(List.of()).when(hotelService).listarCiudades();

        controller.handleListarCiudades(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarCiudades();
    }

    @Test
    @DisplayName("handleListarCiudades - sin rol admin - retorna 403")
    void handleListarCiudades_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarCiudades(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarCiudades();
    }

    // =========================================================================
    // 5. handleListarHoteles
    // =========================================================================

    @Test
    @DisplayName("handleListarHoteles - rol admin - retorna lista de hoteles")
    void handleListarHoteles_rolAdmin_retornaListaDeHoteles() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(List.of()).when(hotelService).listarTodos();

        controller.handleListarHoteles(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarTodos();
    }

    @Test
    @DisplayName("handleListarHoteles - sin rol admin - retorna 403")
    void handleListarHoteles_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarHoteles(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarTodos();
    }

    // =========================================================================
    // 6. handleCrearHotel
    // =========================================================================

    @Test
    @DisplayName("handleCrearHotel - rol admin - crea hotel y retorna 201")
    void handleCrearHotel_rolAdmin_creaHotelYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        CrearHotelRequestDTO dto = mock(CrearHotelRequestDTO.class);
        when(ctx.bodyAsClass(CrearHotelRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).crearHotel(dto);
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleCrearHotel(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).crearHotel(dto);
    }

    @Test
    @DisplayName("handleCrearHotel - sin rol admin - retorna 403")
    void handleCrearHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCrearHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).crearHotel(any());
    }

    @Test
    @DisplayName("handleCrearHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleCrearHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        CrearHotelRequestDTO dto = mock(CrearHotelRequestDTO.class);
        when(ctx.bodyAsClass(CrearHotelRequestDTO.class)).thenReturn(dto);
        when(hotelService.crearHotel(dto)).thenThrow(new IllegalArgumentException("Datos invalidos"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCrearHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Datos invalidos"));
    }

    // =========================================================================
    // 7. handleEditarHotel
    // =========================================================================

    @Test
    @DisplayName("handleEditarHotel - rol admin - edita hotel exitosamente")
    void handleEditarHotel_rolAdmin_editaHotelExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        EditarHotelRequestDTO dto = mock(EditarHotelRequestDTO.class);
        when(ctx.bodyAsClass(EditarHotelRequestDTO.class)).thenReturn(dto);

        controller.handleEditarHotel(ctx);

        verify(hotelService).editarHotel(5, dto);
        verify(ctx).json(Map.of("mensaje", "Hotel actualizado"));
    }

    @Test
    @DisplayName("handleEditarHotel - sin rol admin - retorna 403")
    void handleEditarHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEditarHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).editarHotel(anyInt(), any());
    }

    @Test
    @DisplayName("handleEditarHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleEditarHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        EditarHotelRequestDTO dto = mock(EditarHotelRequestDTO.class);
        when(ctx.bodyAsClass(EditarHotelRequestDTO.class)).thenReturn(dto);
        doThrow(new IllegalArgumentException("Hotel no encontrado")).when(hotelService).editarHotel(5, dto);
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleEditarHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Hotel no encontrado"));
    }

    // =========================================================================
    // 8. handleEliminarHotel
    // =========================================================================

    @Test
    @DisplayName("handleEliminarHotel - rol admin - elimina hotel exitosamente")
    void handleEliminarHotel_rolAdmin_eliminaHotelExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("7");

        controller.handleEliminarHotel(ctx);

        verify(hotelService).eliminarHotel(7);
        verify(ctx).json(Map.of("mensaje", "Hotel eliminado"));
    }

    @Test
    @DisplayName("handleEliminarHotel - sin rol admin - retorna 403")
    void handleEliminarHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarHotel(anyInt());
    }

    @Test
    @DisplayName("handleEliminarHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleEliminarHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("7");
        doThrow(new IllegalArgumentException("Hotel con reservas")).when(hotelService).eliminarHotel(7);
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleEliminarHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Hotel con reservas"));
    }

    // =========================================================================
    // 9. handleReservasActivasHotel
    // =========================================================================

    @Test
    @DisplayName("handleReservasActivasHotel - rol admin - retorna reservas activas")
    void handleReservasActivasHotel_rolAdmin_retornaReservasActivas() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("3");
        doReturn(null).when(hotelService).obtenerReservasActivasHotel(3);

        controller.handleReservasActivasHotel(ctx);

        verify(ctx).json(any());
        verify(hotelService).obtenerReservasActivasHotel(3);
    }

    @Test
    @DisplayName("handleReservasActivasHotel - sin rol admin - retorna 403")
    void handleReservasActivasHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleReservasActivasHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).obtenerReservasActivasHotel(anyInt());
    }

    @Test
    @DisplayName("handleReservasActivasHotel - hotel no encontrado - retorna 404")
    void handleReservasActivasHotel_hotelNoEncontrado_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("3");
        when(hotelService.obtenerReservasActivasHotel(3))
                .thenThrow(new IllegalArgumentException("Hotel no encontrado"));
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleReservasActivasHotel(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Hotel no encontrado"));
    }

    // =========================================================================
    // 10. handleCerrarHotel
    // =========================================================================

    @Test
    @DisplayName("handleCerrarHotel - rol admin - cierra hotel exitosamente")
    void handleCerrarHotel_rolAdmin_cierraHotelExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("1");
        Map<String, Object> body = new HashMap<>();
        body.put("hotelNombre", "Hotel Plaza");
        body.put("eliminarDefinitivo", "false");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        Map<String, Object> resultado = Map.of("canceladas", 3);
        doReturn(resultado).when(hotelService).cerrarHotelConCancelaciones(1, "Hotel Plaza", false);

        controller.handleCerrarHotel(ctx);

        verify(ctx).json(resultado);
        verify(hotelService).cerrarHotelConCancelaciones(1, "Hotel Plaza", false);
    }

    @Test
    @DisplayName("handleCerrarHotel - sin rol admin - retorna 403")
    void handleCerrarHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCerrarHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).cerrarHotelConCancelaciones(anyInt(), any(), anyBoolean());
    }

    @Test
    @DisplayName("handleCerrarHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleCerrarHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("1");
        Map<String, Object> body = new HashMap<>();
        body.put("hotelNombre", "Hotel Plaza");
        body.put("eliminarDefinitivo", "false");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(hotelService.cerrarHotelConCancelaciones(1, "Hotel Plaza", false))
                .thenThrow(new IllegalArgumentException("Hotel ya cerrado"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCerrarHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Hotel ya cerrado"));
    }

    // =========================================================================
    // 11. handleReactivarHotel
    // =========================================================================

    @Test
    @DisplayName("handleReactivarHotel - rol admin - reactiva hotel exitosamente")
    void handleReactivarHotel_rolAdmin_reactivaHotelExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("2");

        controller.handleReactivarHotel(ctx);

        verify(hotelService).reactivarHotel(2);
        verify(ctx).json(Map.of("mensaje", "Hotel reactivado correctamente"));
    }

    @Test
    @DisplayName("handleReactivarHotel - sin rol admin - retorna 403")
    void handleReactivarHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleReactivarHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).reactivarHotel(anyInt());
    }

    @Test
    @DisplayName("handleReactivarHotel - hotel no encontrado - retorna 404")
    void handleReactivarHotel_hotelNoEncontrado_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("2");
        doThrow(new IllegalArgumentException("Hotel no encontrado")).when(hotelService).reactivarHotel(2);
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleReactivarHotel(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Hotel no encontrado"));
    }

    // =========================================================================
    // 12. handleAgregarImagenHotel
    // =========================================================================

    @Test
    @DisplayName("handleAgregarImagenHotel - rol admin - agrega imagen y retorna 201")
    void handleAgregarImagenHotel_rolAdmin_agregaImagenYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("4");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("base64data");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).agregarImagenHotel(4, "base64data");
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleAgregarImagenHotel(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).agregarImagenHotel(4, "base64data");
    }

    @Test
    @DisplayName("handleAgregarImagenHotel - sin rol admin - retorna 403")
    void handleAgregarImagenHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleAgregarImagenHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).agregarImagenHotel(anyInt(), any());
    }

    @Test
    @DisplayName("handleAgregarImagenHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleAgregarImagenHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("4");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("baddata");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        when(hotelService.agregarImagenHotel(4, "baddata"))
                .thenThrow(new IllegalArgumentException("Base64 invalido"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleAgregarImagenHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Base64 invalido"));
    }

    // =========================================================================
    // 13. handleEliminarImagenHotel
    // =========================================================================

    @Test
    @DisplayName("handleEliminarImagenHotel - rol admin - elimina imagen exitosamente")
    void handleEliminarImagenHotel_rolAdmin_eliminaImagenExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("imgId")).thenReturn("10");

        controller.handleEliminarImagenHotel(ctx);

        verify(hotelService).eliminarImagenHotel(10);
        verify(ctx).json(Map.of("mensaje", "Imagen eliminada"));
    }

    @Test
    @DisplayName("handleEliminarImagenHotel - sin rol admin - retorna 403")
    void handleEliminarImagenHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarImagenHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarImagenHotel(anyInt());
    }

    // =========================================================================
    // 14. handleListarAmenidadesHotel
    // =========================================================================

    @Test
    @DisplayName("handleListarAmenidadesHotel - rol admin - retorna amenidades del hotel")
    void handleListarAmenidadesHotel_rolAdmin_retornaAmenidadesDelHotel() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("6");
        doReturn(List.of()).when(hotelService).listarAmenidadesHotel(6);

        controller.handleListarAmenidadesHotel(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarAmenidadesHotel(6);
    }

    @Test
    @DisplayName("handleListarAmenidadesHotel - sin rol admin - retorna 403")
    void handleListarAmenidadesHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarAmenidadesHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarAmenidadesHotel(anyInt());
    }

    @Test
    @DisplayName("handleListarAmenidadesHotel - hotel no encontrado - retorna 404")
    void handleListarAmenidadesHotel_hotelNoEncontrado_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("6");
        when(hotelService.listarAmenidadesHotel(6))
                .thenThrow(new IllegalArgumentException("Hotel no encontrado"));
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleListarAmenidadesHotel(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Hotel no encontrado"));
    }

    // =========================================================================
    // 15. handleAgregarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("handleAgregarAmenidadHotel - rol admin - agrega amenidad y retorna 201")
    void handleAgregarAmenidadHotel_rolAdmin_agregaAmenidadYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("6");
        AgregarAmenidadRequestDTO dto = mock(AgregarAmenidadRequestDTO.class);
        when(ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).agregarAmenidadHotel(6, dto);
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleAgregarAmenidadHotel(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).agregarAmenidadHotel(6, dto);
    }

    @Test
    @DisplayName("handleAgregarAmenidadHotel - sin rol admin - retorna 403")
    void handleAgregarAmenidadHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleAgregarAmenidadHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).agregarAmenidadHotel(anyInt(), any());
    }

    @Test
    @DisplayName("handleAgregarAmenidadHotel - servicio lanza IllegalArgumentException - retorna 400")
    void handleAgregarAmenidadHotel_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("6");
        AgregarAmenidadRequestDTO dto = mock(AgregarAmenidadRequestDTO.class);
        when(ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)).thenReturn(dto);
        when(hotelService.agregarAmenidadHotel(6, dto))
                .thenThrow(new IllegalArgumentException("Amenidad ya asignada"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleAgregarAmenidadHotel(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Amenidad ya asignada"));
    }

    // =========================================================================
    // 16. handleActualizarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("handleActualizarAmenidadHotel - rol admin - actualiza amenidad exitosamente")
    void handleActualizarAmenidadHotel_rolAdmin_actualizaAmenidadExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("haId")).thenReturn("8");
        AgregarAmenidadRequestDTO dto = mock(AgregarAmenidadRequestDTO.class);
        when(ctx.bodyAsClass(AgregarAmenidadRequestDTO.class)).thenReturn(dto);

        controller.handleActualizarAmenidadHotel(ctx);

        verify(hotelService).actualizarAmenidadHotel(8, dto);
        verify(ctx).json(Map.of("mensaje", "Amenidad actualizada"));
    }

    @Test
    @DisplayName("handleActualizarAmenidadHotel - sin rol admin - retorna 403")
    void handleActualizarAmenidadHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleActualizarAmenidadHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).actualizarAmenidadHotel(anyInt(), any());
    }

    // =========================================================================
    // 17. handleEliminarAmenidadHotel
    // =========================================================================

    @Test
    @DisplayName("handleEliminarAmenidadHotel - rol admin - elimina amenidad exitosamente")
    void handleEliminarAmenidadHotel_rolAdmin_eliminaAmenidadExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("haId")).thenReturn("9");

        controller.handleEliminarAmenidadHotel(ctx);

        verify(hotelService).eliminarAmenidadHotel(9);
        verify(ctx).json(Map.of("mensaje", "Amenidad eliminada"));
    }

    @Test
    @DisplayName("handleEliminarAmenidadHotel - sin rol admin - retorna 403")
    void handleEliminarAmenidadHotel_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarAmenidadHotel(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarAmenidadHotel(anyInt());
    }

    // =========================================================================
    // 18. handleAgregarImagenAmenidad
    // =========================================================================

    @Test
    @DisplayName("handleAgregarImagenAmenidad - rol admin - agrega imagen y retorna 201")
    void handleAgregarImagenAmenidad_rolAdmin_agregaImagenYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("haId")).thenReturn("11");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("imgBase64");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).agregarImagenAmenidad(11, "imgBase64");
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleAgregarImagenAmenidad(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).agregarImagenAmenidad(11, "imgBase64");
    }

    @Test
    @DisplayName("handleAgregarImagenAmenidad - sin rol admin - retorna 403")
    void handleAgregarImagenAmenidad_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleAgregarImagenAmenidad(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).agregarImagenAmenidad(anyInt(), any());
    }

    @Test
    @DisplayName("handleAgregarImagenAmenidad - servicio lanza IllegalArgumentException - retorna 400")
    void handleAgregarImagenAmenidad_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("haId")).thenReturn("11");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("bad");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        when(hotelService.agregarImagenAmenidad(11, "bad"))
                .thenThrow(new IllegalArgumentException("Imagen invalida"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleAgregarImagenAmenidad(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Imagen invalida"));
    }

    // =========================================================================
    // 19. handleEliminarImagenAmenidad
    // =========================================================================

    @Test
    @DisplayName("handleEliminarImagenAmenidad - rol admin - elimina imagen exitosamente")
    void handleEliminarImagenAmenidad_rolAdmin_eliminaImagenExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("imgId")).thenReturn("20");

        controller.handleEliminarImagenAmenidad(ctx);

        verify(hotelService).eliminarImagenAmenidad(20);
        verify(ctx).json(Map.of("mensaje", "Imagen de amenidad eliminada"));
    }

    @Test
    @DisplayName("handleEliminarImagenAmenidad - sin rol admin - retorna 403")
    void handleEliminarImagenAmenidad_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarImagenAmenidad(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarImagenAmenidad(anyInt());
    }

    // =========================================================================
    // 20. handleListarHabitaciones
    // =========================================================================

    @Test
    @DisplayName("handleListarHabitaciones - rol admin - retorna habitaciones del hotel")
    void handleListarHabitaciones_rolAdmin_retornaHabitacionesDelHotel() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("3");
        doReturn(List.of()).when(hotelService).listarHabitaciones(3);

        controller.handleListarHabitaciones(ctx);

        verify(ctx).json(any());
        verify(hotelService).listarHabitaciones(3);
    }

    @Test
    @DisplayName("handleListarHabitaciones - sin rol admin - retorna 403")
    void handleListarHabitaciones_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarHabitaciones(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).listarHabitaciones(anyInt());
    }

    @Test
    @DisplayName("handleListarHabitaciones - hotel no encontrado - retorna 404")
    void handleListarHabitaciones_hotelNoEncontrado_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("3");
        when(hotelService.listarHabitaciones(3))
                .thenThrow(new IllegalArgumentException("Hotel no encontrado"));
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleListarHabitaciones(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Hotel no encontrado"));
    }

    // =========================================================================
    // 21. handleCrearHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleCrearHabitacion - rol admin - crea habitacion y retorna 201")
    void handleCrearHabitacion_rolAdmin_creaHabitacionYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        CrearHabitacionRequestDTO dto = mock(CrearHabitacionRequestDTO.class);
        when(ctx.bodyAsClass(CrearHabitacionRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).crearHabitacion(dto);
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleCrearHabitacion(ctx);

        verify(dto).setHotelId(5);
        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).crearHabitacion(dto);
    }

    @Test
    @DisplayName("handleCrearHabitacion - sin rol admin - retorna 403")
    void handleCrearHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCrearHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).crearHabitacion(any());
    }

    @Test
    @DisplayName("handleCrearHabitacion - servicio lanza IllegalArgumentException - retorna 400")
    void handleCrearHabitacion_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("5");
        CrearHabitacionRequestDTO dto = mock(CrearHabitacionRequestDTO.class);
        when(ctx.bodyAsClass(CrearHabitacionRequestDTO.class)).thenReturn(dto);
        when(hotelService.crearHabitacion(dto))
                .thenThrow(new IllegalArgumentException("Tipo invalido"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCrearHabitacion(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Tipo invalido"));
    }

    // =========================================================================
    // 22. handleEditarHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleEditarHabitacion - rol admin - edita habitacion exitosamente")
    void handleEditarHabitacion_rolAdmin_editaHabitacionExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("12");
        EditarHabitacionRequestDTO dto = mock(EditarHabitacionRequestDTO.class);
        when(ctx.bodyAsClass(EditarHabitacionRequestDTO.class)).thenReturn(dto);

        controller.handleEditarHabitacion(ctx);

        verify(hotelService).editarHabitacion(12, dto);
        verify(ctx).json(Map.of("mensaje", "Habitacion actualizada"));
    }

    @Test
    @DisplayName("handleEditarHabitacion - sin rol admin - retorna 403")
    void handleEditarHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEditarHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).editarHabitacion(anyInt(), any());
    }

    @Test
    @DisplayName("handleEditarHabitacion - servicio lanza IllegalArgumentException - retorna 400")
    void handleEditarHabitacion_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("12");
        EditarHabitacionRequestDTO dto = mock(EditarHabitacionRequestDTO.class);
        when(ctx.bodyAsClass(EditarHabitacionRequestDTO.class)).thenReturn(dto);
        doThrow(new IllegalArgumentException("Habitacion no encontrada"))
                .when(hotelService).editarHabitacion(12, dto);
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleEditarHabitacion(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Habitacion no encontrada"));
    }

    // =========================================================================
    // 23. handleEliminarHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleEliminarHabitacion - rol admin - elimina habitacion exitosamente")
    void handleEliminarHabitacion_rolAdmin_eliminaHabitacionExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("15");

        controller.handleEliminarHabitacion(ctx);

        verify(hotelService).eliminarHabitacion(15);
        verify(ctx).json(Map.of("mensaje", "Habitacion eliminada"));
    }

    @Test
    @DisplayName("handleEliminarHabitacion - sin rol admin - retorna 403")
    void handleEliminarHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarHabitacion(anyInt());
    }

    @Test
    @DisplayName("handleEliminarHabitacion - habitacion no encontrada - retorna 404")
    void handleEliminarHabitacion_habitacionNoEncontrada_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("15");
        doThrow(new IllegalArgumentException("Habitacion no encontrada"))
                .when(hotelService).eliminarHabitacion(15);
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleEliminarHabitacion(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Habitacion no encontrada"));
    }

    // =========================================================================
    // 24. handleReservasActivasHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleReservasActivasHabitacion - rol admin - retorna reservas activas")
    void handleReservasActivasHabitacion_rolAdmin_retornaReservasActivas() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("7");
        doReturn(null).when(hotelService).obtenerReservasActivasHabitacion(7);

        controller.handleReservasActivasHabitacion(ctx);

        verify(ctx).json(any());
        verify(hotelService).obtenerReservasActivasHabitacion(7);
    }

    @Test
    @DisplayName("handleReservasActivasHabitacion - sin rol admin - retorna 403")
    void handleReservasActivasHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleReservasActivasHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).obtenerReservasActivasHabitacion(anyInt());
    }

    @Test
    @DisplayName("handleReservasActivasHabitacion - habitacion no encontrada - retorna 404")
    void handleReservasActivasHabitacion_habitacionNoEncontrada_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("7");
        when(hotelService.obtenerReservasActivasHabitacion(7))
                .thenThrow(new IllegalArgumentException("Habitacion no encontrada"));
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleReservasActivasHabitacion(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Habitacion no encontrada"));
    }

    // =========================================================================
    // 25. handleCerrarHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleCerrarHabitacion - rol admin - cierra habitacion exitosamente")
    void handleCerrarHabitacion_rolAdmin_cierraHabitacionExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("9");
        Map<String, Object> body = new HashMap<>();
        body.put("nombreHabitacion", "Suite 101");
        body.put("eliminarDefinitivo", "true");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        Map<String, Object> resultado = Map.of("canceladas", 1);
        doReturn(resultado).when(hotelService).cerrarHabitacionConCancelaciones(9, "Suite 101", true);

        controller.handleCerrarHabitacion(ctx);

        verify(ctx).json(resultado);
        verify(hotelService).cerrarHabitacionConCancelaciones(9, "Suite 101", true);
    }

    @Test
    @DisplayName("handleCerrarHabitacion - sin rol admin - retorna 403")
    void handleCerrarHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCerrarHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).cerrarHabitacionConCancelaciones(anyInt(), any(), anyBoolean());
    }

    @Test
    @DisplayName("handleCerrarHabitacion - servicio lanza IllegalArgumentException - retorna 400")
    void handleCerrarHabitacion_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("9");
        Map<String, Object> body = new HashMap<>();
        body.put("nombreHabitacion", "Suite 101");
        body.put("eliminarDefinitivo", "false");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(hotelService.cerrarHabitacionConCancelaciones(9, "Suite 101", false))
                .thenThrow(new IllegalArgumentException("Habitacion ya cerrada"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCerrarHabitacion(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Habitacion ya cerrada"));
    }

    // =========================================================================
    // 26. handleReactivarHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleReactivarHabitacion - rol admin - reactiva habitacion exitosamente")
    void handleReactivarHabitacion_rolAdmin_reactivaHabitacionExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("14");

        controller.handleReactivarHabitacion(ctx);

        verify(hotelService).reactivarHabitacion(14);
        verify(ctx).json(Map.of("mensaje", "Habitacion reactivada correctamente"));
    }

    @Test
    @DisplayName("handleReactivarHabitacion - sin rol admin - retorna 403")
    void handleReactivarHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleReactivarHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).reactivarHabitacion(anyInt());
    }

    @Test
    @DisplayName("handleReactivarHabitacion - habitacion no encontrada - retorna 404")
    void handleReactivarHabitacion_habitacionNoEncontrada_retorna404() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("14");
        doThrow(new IllegalArgumentException("Habitacion no encontrada"))
                .when(hotelService).reactivarHabitacion(14);
        when(ctx.status(404)).thenReturn(ctx);

        controller.handleReactivarHabitacion(ctx);

        verify(ctx).status(404);
        verify(ctx).json(Map.of("mensaje", "Habitacion no encontrada"));
    }

    // =========================================================================
    // 27. handleAgregarImagenHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleAgregarImagenHabitacion - rol admin - agrega imagen y retorna 201")
    void handleAgregarImagenHabitacion_rolAdmin_agregaImagenYRetorna201() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("16");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("habitacionImg");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        doReturn(null).when(hotelService).agregarImagenHabitacion(16, "habitacionImg");
        when(ctx.status(201)).thenReturn(ctx);

        controller.handleAgregarImagenHabitacion(ctx);

        verify(ctx).status(201);
        verify(ctx).json(any());
        verify(hotelService).agregarImagenHabitacion(16, "habitacionImg");
    }

    @Test
    @DisplayName("handleAgregarImagenHabitacion - sin rol admin - retorna 403")
    void handleAgregarImagenHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleAgregarImagenHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).agregarImagenHabitacion(anyInt(), any());
    }

    @Test
    @DisplayName("handleAgregarImagenHabitacion - servicio lanza IllegalArgumentException - retorna 400")
    void handleAgregarImagenHabitacion_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("16");
        SubirImagenRequestDTO dto = mock(SubirImagenRequestDTO.class);
        when(dto.getBase64()).thenReturn("bad");
        when(ctx.bodyAsClass(SubirImagenRequestDTO.class)).thenReturn(dto);
        when(hotelService.agregarImagenHabitacion(16, "bad"))
                .thenThrow(new IllegalArgumentException("Imagen invalida"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleAgregarImagenHabitacion(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Imagen invalida"));
    }

    // =========================================================================
    // 28. handleEliminarImagenHabitacion
    // =========================================================================

    @Test
    @DisplayName("handleEliminarImagenHabitacion - rol admin - elimina imagen exitosamente")
    void handleEliminarImagenHabitacion_rolAdmin_eliminaImagenExitosamente() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("imgId")).thenReturn("25");

        controller.handleEliminarImagenHabitacion(ctx);

        verify(hotelService).eliminarImagenHabitacion(25);
        verify(ctx).json(Map.of("mensaje", "Imagen eliminada"));
    }

    @Test
    @DisplayName("handleEliminarImagenHabitacion - sin rol admin - retorna 403")
    void handleEliminarImagenHabitacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleEliminarImagenHabitacion(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).eliminarImagenHabitacion(anyInt());
    }

    // =========================================================================
    // 29. handleListarReservaciones
    // =========================================================================

    @Test
    @DisplayName("handleListarReservaciones - rol admin - retorna todas las reservaciones")
    void handleListarReservaciones_rolAdmin_retornaTodasLasReservaciones() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(List.of()).when(adminReservacionService).listarTodas();

        controller.handleListarReservaciones(ctx);

        verify(ctx).json(any());
        verify(adminReservacionService).listarTodas();
    }

    @Test
    @DisplayName("handleListarReservaciones - sin rol admin - retorna 403")
    void handleListarReservaciones_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleListarReservaciones(ctx);

        verify(ctx).status(403);
        verify(adminReservacionService, never()).listarTodas();
    }

    // =========================================================================
    // 30. handleCancelarReservacion
    // =========================================================================

    @Test
    @DisplayName("handleCancelarReservacion - rol admin con motivo en body - cancela y retorna respuesta")
    void handleCancelarReservacion_rolAdminConMotivo_cancelaYRetornaRespuesta() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("30");
        Map<String, Object> body = new HashMap<>();
        body.put("motivo", "Fuerza mayor");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        ResultadoNotificacionDTO resultado = mock(ResultadoNotificacionDTO.class);
        doReturn(resultado).when(adminReservacionService).cancelarReservacion(30, "Fuerza mayor");

        controller.handleCancelarReservacion(ctx);

        verify(adminReservacionService).cancelarReservacion(30, "Fuerza mayor");
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleCancelarReservacion - body lanza excepcion - usa motivo por defecto")
    void handleCancelarReservacion_bodyLanzaExcepcion_usaMotivoDefault() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("30");
        when(ctx.bodyAsClass(Map.class)).thenThrow(new RuntimeException("parse error"));
        ResultadoNotificacionDTO resultado = mock(ResultadoNotificacionDTO.class);
        doReturn(resultado).when(adminReservacionService).cancelarReservacion(30, "Cancelada por administrador");

        controller.handleCancelarReservacion(ctx);

        verify(adminReservacionService).cancelarReservacion(30, "Cancelada por administrador");
        verify(ctx).json(any());
    }

    @Test
    @DisplayName("handleCancelarReservacion - sin rol admin - retorna 403")
    void handleCancelarReservacion_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleCancelarReservacion(ctx);

        verify(ctx).status(403);
        verify(adminReservacionService, never()).cancelarReservacion(anyInt(), any());
    }

    @Test
    @DisplayName("handleCancelarReservacion - servicio lanza IllegalArgumentException - retorna 400")
    void handleCancelarReservacion_servicioLanzaExcepcion_retorna400() {
        when(ctx.attribute("rolId")).thenReturn(2);
        when(ctx.pathParam("id")).thenReturn("30");
        Map<String, Object> body = new HashMap<>();
        body.put("motivo", "Fuerza mayor");
        when(ctx.bodyAsClass(Map.class)).thenReturn(body);
        when(adminReservacionService.cancelarReservacion(30, "Fuerza mayor"))
                .thenThrow(new IllegalArgumentException("Reservacion no encontrada"));
        when(ctx.status(400)).thenReturn(ctx);

        controller.handleCancelarReservacion(ctx);

        verify(ctx).status(400);
        verify(ctx).json(Map.of("mensaje", "Reservacion no encontrada"));
    }

    // =========================================================================
    // 31. handleObtenerMetricas
    // =========================================================================

    @Test
    @DisplayName("handleObtenerMetricas - rol admin - retorna metricas del sistema")
    void handleObtenerMetricas_rolAdmin_retornaMetricasDelSistema() {
        when(ctx.attribute("rolId")).thenReturn(2);
        doReturn(null).when(hotelService).obtenerMetricas();

        controller.handleObtenerMetricas(ctx);

        verify(ctx).json(any());
        verify(hotelService).obtenerMetricas();
    }

    @Test
    @DisplayName("handleObtenerMetricas - sin rol admin - retorna 403")
    void handleObtenerMetricas_sinRolAdmin_retorna403() {
        when(ctx.attribute("rolId")).thenReturn(null);
        when(ctx.status(403)).thenReturn(ctx);

        controller.handleObtenerMetricas(ctx);

        verify(ctx).status(403);
        verify(hotelService, never()).obtenerMetricas();
    }
}
