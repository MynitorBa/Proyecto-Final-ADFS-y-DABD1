package org.example.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para DataAccessException.
 * Valida constructores y comportamiento como RuntimeException.
 */
@DisplayName("DataAccessException - Excepcion personalizada de acceso a datos")
class DataAccessExceptionTest {

    @Test
    @DisplayName("constructor con mensaje y causa almacena ambos valores")
    void constructor_conMensajeYCausa_almacenaAmbosValores() {
        String mensaje = "Error al consultar la base de datos";
        Exception causa = new RuntimeException("Conexion rechazada");

        DataAccessException ex = new DataAccessException(mensaje, causa);

        assertEquals(mensaje, ex.getMessage());
        assertEquals(causa,   ex.getCause());
    }

    @Test
    @DisplayName("es subclase de RuntimeException")
    void esSubclase_deRuntimeException() {
        DataAccessException ex = new DataAccessException("error", new Exception("causa"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("se puede lanzar y capturar como RuntimeException")
    void sePuedeLanzarYCapturar_comoRuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            throw new DataAccessException("fallo SQL", new Exception("SQL error"));
        });
    }

    @Test
    @DisplayName("se puede capturar especificamente como DataAccessException")
    void sePuedeCapturar_comoDataAccessException() {
        assertThrows(DataAccessException.class, () -> {
            throw new DataAccessException("tabla no encontrada", new Exception("ORA-00942"));
        });
    }

    @Test
    @DisplayName("causa puede ser SQLException simulada")
    void causa_puedeSerSQLException_simulada() {
        Exception sqlEx = new java.sql.SQLException("Constraint violation");
        DataAccessException ex = new DataAccessException("Fallo al insertar", sqlEx);

        assertNotNull(ex.getCause());
        assertEquals("Constraint violation", ex.getCause().getMessage());
    }

    @Test
    @DisplayName("mensaje descriptivo se preserva al propagar la excepcion")
    void mensajeDescriptivo_sePropaga_correctamente() {
        String mensajeEsperado = "executeQuery fallo en HotelRepository.listarTodos";
        DataAccessException ex = new DataAccessException(mensajeEsperado, new Exception());

        assertTrue(ex.getMessage().contains("HotelRepository"));
        assertTrue(ex.getMessage().contains("listarTodos"));
    }
}
