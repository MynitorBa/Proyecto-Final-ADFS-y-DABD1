package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.TarjetaHelper;
import org.example.repositories.PagoRepository;

/**
 * Service para procesar pagos de reservaciones de usuarios web.
 * Valida la reservacion, simula la verificacion de tarjeta y genera la factura.
 */
public class PagoService {

    private final PagoRepository pagoRepository = new PagoRepository();

    /**
     * Procesa el pago de una reservacion de usuario web.
     * Verifica que la reservacion exista y este pendiente, valida la tarjeta
     * en memoria, confirma la reservacion y genera la factura.
     * @param reservacionId ID de la reservacion a pagar.
     * @param usuarioId     ID del usuario dueno de la reservacion.
     * @param request       datos de pago: tarjeta, NIT y codigo postal.
     * @return DTO con los datos de la factura generada.
     * @throws IllegalArgumentException si la reservacion no existe, no pertenece al usuario
     *                                  o su estado no permite el pago.
     */
    public PagoResponseDTO procesarPago(int reservacionId, int usuarioId, PagoRequestDTO request) {

        // Verifica que la reservacion existe y pertenece al usuario
        Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, usuarioId);
        if (reservacion == null) {
            throw new IllegalArgumentException("Reservacion no encontrada o no pertenece al usuario");
        }

        // Solo se puede pagar si la reservacion esta en estado Pendiente (EstadoID = 1)
        int estadoId = (int) reservacion[4];
        String estado = (String) reservacion[3];
        if (estadoId != 1) {
            throw new IllegalArgumentException(
                    "La reservacion no puede ser pagada, estado actual: " + estado
            );
        }

        // Validacion simulada de tarjeta, no se persiste ningun dato de pago
        TarjetaHelper.validar(request);

        // Cambia estado a Confirmada (2) y elimina la fecha de expiracion
        pagoRepository.confirmarReservacion(reservacionId);

        // Genera la factura con el total de la reservacion
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