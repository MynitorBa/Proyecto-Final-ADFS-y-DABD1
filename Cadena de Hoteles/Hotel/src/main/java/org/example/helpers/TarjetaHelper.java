package org.example.helpers;

import org.example.dtos.PagoRequestDTO;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Helper para la validacion de datos de tarjeta de credito o debito.
 * Verifica formato del numero, nombre del titular, fecha de vencimiento y CVV
 * antes de procesar cualquier pago.
 */
public class TarjetaHelper {

    /**
     * Valida los datos de tarjeta contenidos en el request de pago.
     * Verifica que el numero tenga 16 digitos, el titular no este vacio,
     * la fecha de vencimiento sea valida y no este expirada, y el CVV tenga 3 o 4 digitos.
     *
     * @param request DTO con los datos de la tarjeta a validar.
     * @throws IllegalArgumentException si cualquiera de los campos no cumple el formato esperado
     *                                  o la tarjeta esta vencida.
     */
    public static void validar(PagoRequestDTO request) {

        // Numero de tarjeta: 16 digitos numericos sin espacios
        String numero = request.getNumeroTarjeta().replaceAll("\\s+", "");
        if (!numero.matches("\\d{16}")) {
            throw new IllegalArgumentException("Numero de tarjeta invalido");
        }

        // Nombre del titular: no puede estar vacio
        if (request.getNombreTitular() == null || request.getNombreTitular().isBlank()) {
            throw new IllegalArgumentException("Nombre del titular requerido");
        }

        // Fecha de vencimiento: formato MM/YY y no debe estar expirada
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth vencimiento = YearMonth.parse(request.getFechaVencimiento(), fmt);
            if (vencimiento.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException("La tarjeta esta vencida");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha de vencimiento invalido, use MM/YY");
        }

        // CVV: 3 digitos para la mayoria de tarjetas, 4 para American Express
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV invalido");
        }
    }
}