package org.example.services;

import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.TarjetaHelper;
import org.example.repositories.PagoRepository;
import org.example.repositories.TokenValidacionRepository;

import org.example.repositories.LogReservacionRepository;

import org.example.dtos.TokenValidacionResponseDTO;

/**
 * Service para procesar pagos de reservaciones de usuarios web.
 * Valida la reservacion, simula la verificacion de tarjeta y genera la factura.
 */
public class PagoService {

    private final PagoRepository pagoRepository;
    private final TokenValidacionRepository tokenValidacionRepository;
    private final LogReservacionRepository logReservacionRepository;

    /**
     * Crea una instancia de PagoService con sus dependencias inyectadas.
     */
    public PagoService(PagoRepository pagoRepository,
                       TokenValidacionRepository tokenValidacionRepository,
                       LogReservacionRepository logReservacionRepository) {
        this.pagoRepository            = pagoRepository;
        this.tokenValidacionRepository = tokenValidacionRepository;
        this.logReservacionRepository  = logReservacionRepository;
    }


    /**
     * Procesa el pago de una reservacion de usuario web.
     * Verifica que la reservacion exista y este pendiente, valida la tarjeta
     * en memoria, confirma la reservacion y genera la factura.
     * Si se incluye un token de alianza, valida que el hotel de la reservacion
     * se encuentre en la misma ciudad para la que fue generado el token antes
     * de aplicar el descuento.
     * @param reservacionId ID de la reservacion a pagar.
     * @param usuarioId     ID del usuario dueno de la reservacion.
     * @param request       datos de pago: tarjeta, NIT, codigo postal y token opcional.
     * @return DTO con los datos de la factura generada.
     * @throws IllegalArgumentException si la reservacion no existe, no pertenece al usuario,
     *                                  su estado no permite el pago, el token es invalido
     *                                  o el token no aplica para la ciudad del hotel.
     */
    public PagoResponseDTO procesarPago(int reservacionId, int usuarioId,
                                        PagoRequestDTO request, String ip, String userAgent) {
        try {
            Object[] reservacion = pagoRepository.obtenerReservacionParaPago(reservacionId, usuarioId);
            if (reservacion == null) {
                throw new IllegalArgumentException("Reservacion no encontrada o no pertenece al usuario");
            }

            int estadoId  = (int) reservacion[4];
            String estado = (String) reservacion[3];
            if (estadoId != 1) {
                throw new IllegalArgumentException(
                        "La reservacion no puede ser pagada, estado actual: " + estado
                );
            }

            TarjetaHelper.validar(request);

            double total = (double) reservacion[2];

            if (request.getTokenAlianza() != null && !request.getTokenAlianza().isBlank()) {
                TokenValidacionResponseDTO datosToken = tokenValidacionRepository
                        .buscarTokenValido(request.getTokenAlianza());

                if (datosToken == null) {
                    throw new IllegalArgumentException("Token de alianza inválido, ya utilizado o expirado");
                }

                // 1. Validar Ciudad
                String ciudadHotel = pagoRepository.obtenerCiudadReservacion(reservacionId);
                if (ciudadHotel == null || !ciudadHotel.equalsIgnoreCase(datosToken.getCiudad())) {
                    throw new IllegalArgumentException("El token de alianza no aplica para hoteles en esta ciudad");
                }

                // 2. Validar Rango de Fechas (Estancia dentro del rango de vuelo)
                // Extraemos las fechas del objeto reservacion que viene del Repository actualizado
                java.util.Date fechaInicioHotel = (java.util.Date) reservacion[5];
                java.util.Date fechaFinHotel    = (java.util.Date) reservacion[6];

// CORRECCIÓN: Convertir el String del DTO a java.sql.Date para poder comparar
                java.util.Date fechaIdaVuelo    = java.sql.Date.valueOf(datosToken.getFechaIda());
                java.util.Date fechaVueltaVuelo = java.sql.Date.valueOf(datosToken.getFechaVuelta());

// Validación lógica: CheckIn no puede ser antes de la Ida, CheckOut no puede ser después de la Vuelta
                if (fechaInicioHotel.before(fechaIdaVuelo) || fechaFinHotel.after(fechaVueltaVuelo)) {
                    throw new IllegalArgumentException(
                            "Las fechas de hospedaje deben estar dentro del rango del vuelo (Vuelo: "
                                    + datosToken.getFechaIda() + " al " + datosToken.getFechaVuelta() + ")"
                    );
                }

                // 3. Aplicar Descuento
                double factor = 1.0 - (datosToken.getPorcentajeDescuento() / 100.0);
                total = Math.round(total * factor * 100.0) / 100.0;

                // 4. Persistir cambios de precio en la base de datos
                // Actualizamos el total de la cabecera de la reservación
                pagoRepository.actualizarTotalReservacion(reservacionId, total);

                // Actualizamos proporcionalmente cada detalle (habitación) de la reserva
                pagoRepository.actualizarTotalDetalles(reservacionId, factor);
            }

            pagoRepository.confirmarReservacion(reservacionId);

            if (request.getTokenAlianza() != null && !request.getTokenAlianza().isBlank()) {
                tokenValidacionRepository.marcarTokenUsado(request.getTokenAlianza(), reservacionId);
            }

            int facturaId = pagoRepository.crearFactura(
                    reservacionId,
                    request.getNit(),
                    request.getCodigoPostal(),
                    total
            );

            PagoResponseDTO resultado = pagoRepository.obtenerFactura(facturaId);

            logReservacionRepository.registrar(
                    LogReservacionRepository.TIPO_PAGO_EXITOSO,
                    reservacionId,
                    usuarioId,
                    null,
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
                    LogReservacionRepository.TIPO_PAGO_FALLIDO,
                    reservacionId,
                    usuarioId,
                    null,
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
                    LogReservacionRepository.TIPO_PAGO_ERROR_INTERNO,
                    reservacionId,
                    usuarioId,
                    null,
                    null,
                    null,
                    false,
                    ip,
                    userAgent,
                    e.getMessage()
            );
            throw new RuntimeException("Error interno al procesar el pago", e);
        }
    }
}