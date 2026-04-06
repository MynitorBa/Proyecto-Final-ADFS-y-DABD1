package org.example.helpers;

import org.example.dtos.ReservacionDetalleDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PdfHelper.
 * Verifica que generarPdfReservacion produce un arreglo de bytes no vacio
 * tanto en modo comprobante como en modo factura.
 * iText7 se ejecuta directamente sin mocks.
 */
class PdfHelperTest {

    /**
     * Crea un ReservacionDetalleDTO con todos los campos necesarios para generar el PDF.
     *
     * @return DTO con datos de prueba completos.
     */
    private ReservacionDetalleDTO buildDetalle() {
        ReservacionDetalleDTO dto = new ReservacionDetalleDTO();
        dto.setNoReservacion("MIKU-TEST01");
        dto.setNombreHotel("Hotel Test");
        dto.setFechaCreacion("2025-01-01");
        dto.setTotal(200.0);
        dto.setEstado("confirmada");
        dto.setFechaCheckIn("2025-01-10");
        dto.setFechaCheckOut("2025-01-15");
        dto.setNumeroHabitacion("101");
        dto.setTipoHabitacion("Suite");
        dto.setTipoCama("King");
        dto.setCantidadPersonas(2);
        dto.setTotalDetalle(200.0);
        return dto;
    }

    // -- generarPdfReservacion

    /**
     * Verifica que generarPdfReservacion retorna un arreglo de bytes no nulo y no vacio
     * cuando se genera un comprobante simple (factura null).
     */
    @Test
    void generarPdfReservacion_retornaPdfNoNulo() {
        ReservacionDetalleDTO dto = buildDetalle();

        byte[] result = PdfHelper.generarPdfReservacion(List.of(dto), null);

        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.length > 0, "El PDF debe tener contenido");
    }

    /**
     * Verifica que generarPdfReservacion retorna un arreglo de bytes no nulo y no vacio
     * cuando se generan datos de facturacion (modo factura).
     */
    @Test
    void generarPdfReservacion_conFactura_retornaPdf() {
        ReservacionDetalleDTO dto = buildDetalle();
        Object[] factura = new Object[]{1, "2025-01-01", "CF", "01001", 100.0};

        byte[] result = PdfHelper.generarPdfReservacion(List.of(dto), factura);

        assertNotNull(result, "El resultado no debe ser null");
        assertTrue(result.length > 0, "El PDF con factura debe tener contenido");
    }
}
