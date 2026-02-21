package org.example.helpers;

import org.example.dtos.PagoRequestDTO;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class TarjetaHelper {

    public static void validar(PagoRequestDTO request) {

        // numero de tarjeta: 16 dígitos numéricos
        String numero = request.getNumeroTarjeta().replaceAll("\\s+", "");
        if (!numero.matches("\\d{16}")) {
            throw new IllegalArgumentException("Número de tarjeta inválido");
        }

        // Nombre del titular: no vacío
        if (request.getNombreTitular() == null || request.getNombreTitular().isBlank()) {
            throw new IllegalArgumentException("Nombre del titular requerido");
        }

        // Fecha de vencimiento: formato MM/YY y no vencida
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth vencimiento = YearMonth.parse(request.getFechaVencimiento(), fmt);
            if (vencimiento.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException("La tarjeta está vencida");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha de vencimiento inválido, use MM/YY");
        }

        // CVV: 3 o 4 dígitos
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV inválido");
        }
    }
}