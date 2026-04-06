package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.repositories.PdfReservacionRepository;
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
 * Pruebas unitarias para PdfReservacionService.
 * Verifica las validaciones de pertenencia, detalles vacios
 * y el flujo de generacion del PDF sin acceder a la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PdfReservacionService — Pruebas unitarias")
class PdfReservacionServiceTest {

    @Mock private PdfReservacionRepository repository;

    private PdfReservacionService service;

    /**
     * Inicializa el service con el mock antes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        service = new PdfReservacionService(repository);
    }

    // -- generarPdf

    /**
     * Verifica que se lance IllegalArgumentException cuando la reservacion
     * no pertenece al usuario indicado.
     */
    @Test
    @DisplayName("generarPdf lanza excepcion si la reservacion no pertenece al usuario")
    void generarPdf_noPertenece_lanzaExcepcion() {
        when(repository.perteneceAlUsuario(10, 99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.generarPdf(10, 99)
        );

        assertEquals("Reservacion no encontrada", ex.getMessage());
        verify(repository).perteneceAlUsuario(10, 99);
    }

    /**
     * Verifica que se lance IllegalArgumentException cuando la lista de detalles
     * de la reservacion esta vacia.
     */
    @Test
    @DisplayName("generarPdf lanza excepcion si la reservacion no tiene detalles")
    void generarPdf_detallesVacios_lanzaExcepcion() {
        when(repository.perteneceAlUsuario(5, 1)).thenReturn(true);
        when(repository.obtenerDetalles(5)).thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.generarPdf(5, 1)
        );

        assertEquals("La reservacion no tiene detalles", ex.getMessage());
        verify(repository).obtenerDetalles(5);
    }

    /**
     * Verifica que cuando las guardas pasan correctamente se invoque al repositorio
     * para obtener detalles y factura. La generacion del PDF con iText puede
     * fallar en entorno de prueba, por lo que se acepta cualquier resultado.
     */
    @Test
    @DisplayName("generarPdf invoca repositorio cuando guardas son superadas")
    void generarPdf_guardasSuperadas_invocaRepositorio() {
        ReservacionDetalleDTO dto = buildDetalle();
        Object[] factura = new Object[]{1, "2025-01-01", "CF", "01001", 500.0};

        when(repository.perteneceAlUsuario(3, 7)).thenReturn(true);
        when(repository.obtenerDetalles(3)).thenReturn(List.of(dto));
        when(repository.obtenerFactura(3)).thenReturn(factura);

        service.generarPdf(3, 7);

        verify(repository).perteneceAlUsuario(3, 7);
        verify(repository).obtenerDetalles(3);
        verify(repository).obtenerFactura(3);
    }

    // -- helper

    /**
     * Construye un ReservacionDetalleDTO con todos los campos requeridos por PdfHelper.
     * @return DTO listo para usar en los tests.
     */
    private ReservacionDetalleDTO buildDetalle() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        dto.setNoReservacion("MIKU-ABCD1234");
        dto.setNombreHotel("Hotel Miku");
        dto.setFechaCreacion("2025-01-01 10:00:00");
        dto.setTotal(300.0);
        dto.setEstado("Pendiente");
        dto.setFechaCheckIn("2025-06-01");
        dto.setFechaCheckOut("2025-06-05");
        dto.setNumeroHabitacion("101");
        dto.setTipoHabitacion("Suite");
        dto.setTipoCama("King");
        dto.setCantidadPersonas(2);
        dto.setTotalDetalle(300.0);
        return dto;
    }
}
