package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.helpers.PdfHelper;
import org.example.repositories.PdfReservacionRepository;

import java.util.List;

/**
 * Service para la generacion de PDFs de reservaciones.
 * Valida que la reservacion pertenezca al usuario y delega
 * la construccion del PDF a PdfHelper.
 */
public class PdfReservacionService {

    private final PdfReservacionRepository repository = new PdfReservacionRepository();

    /**
     * Genera el PDF de una reservacion para un usuario especifico.
     * Verifica que la reservacion pertenezca al usuario, obtiene los detalles
     * y la factura, y construye el PDF con esos datos.
     * @param reservacionId ID de la reservacion a exportar.
     * @param usuarioId     ID del usuario dueno de la reservacion.
     * @return array de bytes del PDF generado.
     * @throws IllegalArgumentException si la reservacion no pertenece al usuario
     *                                  o no tiene detalles registrados.
     */
    public byte[] generarPdf(int reservacionId, int usuarioId) {

        // Verifica que la reservacion pertenece al usuario antes de continuar
        if (!repository.perteneceAlUsuario(reservacionId, usuarioId)) {
            throw new IllegalArgumentException("Reservacion no encontrada");
        }

        List<ReservacionDetalleDTO> detalles = repository.obtenerDetalles(reservacionId);
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La reservacion no tiene detalles");
        }

        Object[] factura = repository.obtenerFactura(reservacionId);

        return PdfHelper.generarPdfReservacion(detalles, factura);
    }
}