package org.example.helpers;

import org.example.dtos.PagoRequestDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TarjetaHelper.
 * Verifica la validacion del numero de tarjeta, nombre del titular,
 * fecha de vencimiento y CVV segun las reglas de negocio definidas.
 */
class TarjetaHelperTest {

    /**
     * Crea un PagoRequestDTO con datos validos listos para usar en cada prueba.
     *
     * @return DTO con datos de tarjeta validos.
     */
    private PagoRequestDTO requestValido() {
        PagoRequestDTO req = new PagoRequestDTO();
        req.setNumeroTarjeta("1234567890123456");
        req.setNombreTitular("Juan Perez");
        req.setFechaVencimiento("12/30");
        req.setCvv("123");
        return req;
    }

    // -- validar

    /**
     * Verifica que validar no lanza excepcion cuando todos los datos de tarjeta son validos.
     */
    @Test
    void validar_datosValidos_noLanzaExcepcion() {
        PagoRequestDTO req = requestValido();

        assertDoesNotThrow(() -> TarjetaHelper.validar(req));
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando el numero de tarjeta
     * no tiene 16 digitos.
     */
    @Test
    void validar_numeroInvalido_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setNumeroTarjeta("123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TarjetaHelper.validar(req));
        assertEquals("Numero de tarjeta invalido", ex.getMessage());
    }

    /**
     * Verifica que validar acepta un numero de tarjeta con espacios entre grupos
     * ya que los espacios se eliminan antes de validar.
     */
    @Test
    void validar_numeroConEspacios_esValido() {
        PagoRequestDTO req = requestValido();
        req.setNumeroTarjeta("1234 5678 9012 3456");

        assertDoesNotThrow(() -> TarjetaHelper.validar(req));
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando el nombre del titular
     * esta vacio.
     */
    @Test
    void validar_titularVacio_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setNombreTitular("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TarjetaHelper.validar(req));
        assertEquals("Nombre del titular requerido", ex.getMessage());
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando el nombre del titular
     * es null.
     */
    @Test
    void validar_titularNull_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setNombreTitular(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TarjetaHelper.validar(req));
        assertEquals("Nombre del titular requerido", ex.getMessage());
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando la tarjeta
     * tiene fecha de vencimiento anterior a la fecha actual.
     */
    @Test
    void validar_tarjetaVencida_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setFechaVencimiento("01/20");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TarjetaHelper.validar(req));
        assertEquals("La tarjeta esta vencida", ex.getMessage());
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando el formato de la
     * fecha de vencimiento no es MM/YY.
     */
    @Test
    void validar_formatoFechaInvalido_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setFechaVencimiento("2025-12");

        assertThrows(IllegalArgumentException.class, () -> TarjetaHelper.validar(req));
    }

    /**
     * Verifica que validar lanza IllegalArgumentException cuando el CVV tiene
     * menos de 3 digitos.
     */
    @Test
    void validar_cvvInvalido_lanzaExcepcion() {
        PagoRequestDTO req = requestValido();
        req.setCvv("12");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> TarjetaHelper.validar(req));
        assertEquals("CVV invalido", ex.getMessage());
    }

    /**
     * Verifica que validar acepta un CVV de 4 digitos, como el formato de American Express.
     */
    @Test
    void validar_cvv4digitos_esValido() {
        PagoRequestDTO req = requestValido();
        req.setCvv("1234");

        assertDoesNotThrow(() -> TarjetaHelper.validar(req));
    }
}
