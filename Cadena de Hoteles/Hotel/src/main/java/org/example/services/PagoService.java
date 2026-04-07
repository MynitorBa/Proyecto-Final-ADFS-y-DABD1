package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.TarjetaHelper;
import org.example.repositories.PagoRepository;
import org.example.repositories.TokenValidacionRepository;
import org.example.dtos.TokenValidacionResponseDTO;

/**
 * Service para procesar pagos de reservaciones de usuarios web.
 * Valida la reservacion, simula la verificacion de tarjeta y genera la factura.
 */
public class PagoService {

    private final PagoRepository pagoRepository;
    private final TokenValidacionRepository tokenValidacionRepository;

    /**
     * Crea una instancia de PagoService con sus dependencias inyectadas.
     */
    public PagoService(PagoRepository pagoRepository, TokenValidacionRepository tokenValidacionRepository) {
        this.pagoRepository            = pagoRepository;
        this.tokenValidacionRepository = tokenValidacionRepository;
    }

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

        Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, usuarioId);
        if (reservacion == null) {
            throw new IllegalArgumentException("Reservacion no encontrada o no pertenece al usuario");
        }

        int estadoId = (int) reservacion[4];
        String estado = (String) reservacion[3];
        if (estadoId != 1) {
            throw new IllegalArgumentException(
                    "La reservacion no puede ser pagada, estado actual: " + estado
            );
        }

        TarjetaHelper.validar(request);

        // Declarar total ANTES de usarlo
        double total = (double) reservacion[2];

        // Aplica descuento si viene con token de aerolinea
        if (request.getTokenAlianza() != null && !request.getTokenAlianza().isBlank()) {
            TokenValidacionResponseDTO datosToken = tokenValidacionRepository
                    .buscarTokenValido(request.getTokenAlianza());
            if (datosToken == null) {
                throw new IllegalArgumentException("Token de alianza invalido, ya utilizado o expirado");
            }
            double factor = 1.0 - (datosToken.getPorcentajeDescuento() / 100.0);
            total = Math.round(total * factor * 100.0) / 100.0;
            pagoRepository.actualizarTotalReservacion(reservacionId, total);
            pagoRepository.actualizarTotalDetalles(reservacionId, factor);
        }

        // Confirmar UNA sola vez
        pagoRepository.confirmarReservacion(reservacionId);

        // Cierra el token despues de confirmar
        if (request.getTokenAlianza() != null && !request.getTokenAlianza().isBlank()) {
            tokenValidacionRepository.marcarTokenUsado(request.getTokenAlianza(), reservacionId);
        }

        int facturaId = pagoRepository.crearFactura(
                reservacionId,
                request.getNit(),
                request.getCodigoPostal(),
                total
        );

        return pagoRepository.obtenerFactura(facturaId);
    }
}