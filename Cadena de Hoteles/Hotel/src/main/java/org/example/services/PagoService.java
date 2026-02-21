package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.TarjetaHelper;
import org.example.repositories.PagoRepository;

public class PagoService {

    private final PagoRepository pagoRepository = new PagoRepository();

    public PagoResponseDTO procesarPago(int reservacionId, int usuarioId, PagoRequestDTO request) {

        // Verificar que la reservación existe y pertenece al usuario
        Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, usuarioId);
        if (reservacion == null) {
            throw new IllegalArgumentException("Reservación no encontrada o no pertenece al usuario");
        }

        // Verificar que la reservación esté en estado Pendiente (EstadoID = 1)
        int estadoId = (int) reservacion[4];
        String estado = (String) reservacion[3];
        if (estadoId != 1) {
            throw new IllegalArgumentException(
                    "La reservación no puede ser pagada, estado actual: " + estado
            );
        }

        // Validar tarjeta simulada (solo en memoria, no se guarda nada)
        TarjetaHelper.validar(request);

        // Confirmar reservación — estado 2, quitar expiración
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