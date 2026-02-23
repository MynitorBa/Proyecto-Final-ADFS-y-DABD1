package org.example.services;

import org.example.dtos.ReservacionDetalleDTO;
import org.example.helpers.PdfHelper;
import org.example.repositories.PdfReservacionRepository;

import java.util.List;

public class PdfReservacionService {

    private final PdfReservacionRepository repository = new PdfReservacionRepository();

    public byte[] generarPdf(int reservacionId, int usuarioId) {

        // Verificar que la reservación pertenece al usuario
        if (!repository.perteneceAlUsuario(reservacionId, usuarioId)) {
            throw new IllegalArgumentException("Reservación no encontrada");
        }

        List<ReservacionDetalleDTO> detalles = repository.obtenerDetalles(reservacionId);
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La reservación no tiene detalles");
        }

        Object[] factura = repository.obtenerFactura(reservacionId);

        return PdfHelper.generarPdfReservacion(detalles, factura);
    }
}