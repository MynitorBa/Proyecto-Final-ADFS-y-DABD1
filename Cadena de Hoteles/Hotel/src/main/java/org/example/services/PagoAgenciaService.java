package org.example.services;

import org.example.dtos.PagoAgenciaRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.repositories.PagoAgenciaRepository;

import org.example.repositories.LogReservacionRepository;

/**
 * Service para procesar pagos de reservaciones realizadas por agencias.
 * Valida los datos de facturacion, confirma la reservacion y genera la factura.
 */
public class PagoAgenciaService {

    private final PagoAgenciaRepository pagoRepository;
    private final LogReservacionRepository logReservacionRepository;


    /**
     * Crea una instancia de PagoAgenciaService con sus dependencias inyectadas.
     */
    public PagoAgenciaService(PagoAgenciaRepository pagoRepository,
                              LogReservacionRepository logReservacionRepository) {
        this.pagoRepository           = pagoRepository;
        this.logReservacionRepository = logReservacionRepository;
    }
    /**
     * Procesa el pago de una reservacion de agencia.
     * Valida NIT y codigo postal, verifica que la reservacion pertenezca a la agencia
     * y este en estado Pendiente, luego la confirma y genera la factura.
     * @param reservacionId ID de la reservacion a pagar.
     * @param agenciaId     ID de la agencia duena de la reservacion.
     * @param request       datos de facturacion: NIT y codigo postal.
     * @return DTO con los datos de la factura generada.
     * @throws IllegalArgumentException si los datos son invalidos, la reservacion no existe
     *                                  o su estado no permite el pago.
     */
    public PagoResponseDTO procesarPago(int reservacionId, int agenciaId,
                                        PagoAgenciaRequestDTO request, String ip, String userAgent) {
        try {
            if (request.getNit() == null || request.getNit().isBlank())
                throw new IllegalArgumentException("El NIT es requerido. Si no tienes, ingresa 'CF'.");

            if (request.getCodigoPostal() == null || request.getCodigoPostal().isBlank())
                throw new IllegalArgumentException("El codigo postal es requerido.");

            Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, agenciaId);
            if (reservacion == null)
                throw new IllegalArgumentException("Reservacion no encontrada o no pertenece a esta agencia.");

            int estadoId  = (int) reservacion[4];
            String estado = (String) reservacion[3];
            if (estadoId != 1)
                throw new IllegalArgumentException(
                        "La reservacion no puede ser pagada, estado actual: " + estado);

            pagoRepository.confirmarReservacion(reservacionId);

            double total  = (double) reservacion[2];
            int facturaId = pagoRepository.crearFactura(
                    reservacionId,
                    request.getNit(),
                    request.getCodigoPostal(),
                    total
            );

            PagoResponseDTO resultado = pagoRepository.obtenerFactura(facturaId);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_PAGO_AGENCIA_EXITOSO,
                    reservacionId,
                    null,
                    agenciaId,
                    null,
                    total,
                    true,
                    ip,
                    userAgent,
                    null
            );

            return resultado;

        } catch (IllegalArgumentException e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_PAGO_AGENCIA_FALLIDO,
                    reservacionId,
                    null,
                    agenciaId,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw e;
        } catch (Exception e) {
            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_PAGO_AGENCIA_ERROR,
                    reservacionId,
                    null,
                    agenciaId,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw new RuntimeException("Error interno al procesar el pago de agencia", e);
        }
    }
}