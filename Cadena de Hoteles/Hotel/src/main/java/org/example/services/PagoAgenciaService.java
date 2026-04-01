package org.example.services;

import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.repositories.PagoAgenciaRepository;

public class PagoAgenciaService {

    private final PagoAgenciaRepository pagoRepository = new PagoAgenciaRepository();

    public PagoResponseDTO procesarPago(int reservacionId, int agenciaId, PagoAgenciaRequestDTO request) {

        if (request.getNit() == null || request.getNit().isBlank())
            throw new IllegalArgumentException("El NIT es requerido. Si no tienes, ingresa 'CF'.");

        if (request.getCodigoPostal() == null || request.getCodigoPostal().isBlank())
            throw new IllegalArgumentException("El código postal es requerido.");

        // Verificar que la reservación existe y pertenece a la agencia
        Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, agenciaId);
        if (reservacion == null)
            throw new IllegalArgumentException("Reservación no encontrada o no pertenece a esta agencia.");

        // Verificar estado pendiente (EstadoID = 1)
        int estadoId = (int) reservacion[4];
        String estado = (String) reservacion[3];
        if (estadoId != 1)
            throw new IllegalArgumentException("La reservación no puede ser pagada, estado actual: " + estado);

        // Confirmar reservación
        pagoRepository.confirmarReservacion(reservacionId);

        // Crear factura
        double total = (double) reservacion[2];
        int facturaId = pagoRepository.crearFactura(
                reservacionId,
                request.getNit(),
                request.getCodigoPostal(),
                total
        );

        return pagoRepository.obtenerFactura(facturaId);
    }
}