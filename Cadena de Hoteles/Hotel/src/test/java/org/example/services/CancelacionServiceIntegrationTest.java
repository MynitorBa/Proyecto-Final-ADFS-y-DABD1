package org.example.services;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.example.repositories.CancelacionRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para CancelacionService.
 * Conecta a Oracle real, inserta un usuario, una reservacion confirmada y su detalle
 * antes de cada caso. Verifica que el estado cambie correctamente en Oracle
 * y limpia todos los registros al finalizar.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: CancelacionService - Flujo de Cancelacion")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CancelacionServiceIntegrationTest {

    private CancelacionService cancelacionService;
    private int usuarioIdInsertado;
    private int reservacionIdInsertada;
    private int habitacionIdReal;

    /**
     * Inserta en Oracle un usuario, una reservacion confirmada y un detalle
     * con check-in a 10 dias antes de cada caso de prueba.
     */
    @BeforeEach
    void setUp() {
        cancelacionService = new CancelacionService(new CancelacionRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 2, 'IT-CANCEL')",
                "ID",
                "test_cancel_integration",
                "test_cancel_integration@hotel.com",
                PasswordHelper.hashear("TestPass123")
        );

        // Reservacion en estado Confirmada (EstadoID = 2)
        reservacionIdInsertada = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES ('IT-CANCEL-001', 500.0, 2, ?, SYSDATE, NULL)",
                "ID",
                usuarioIdInsertado
        );

        List<Integer> habitaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Habitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!habitaciones.isEmpty(), "No hay habitaciones en Oracle, se omite la prueba");
        habitacionIdReal = habitaciones.get(0);

        // Detalle con check-in a 10 dias para cumplir la regla de 24 horas
        String checkIn  = LocalDate.now().plusDays(10).toString();
        String checkOut = LocalDate.now().plusDays(13).toString();
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                        "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), 1, 500.0)",
                reservacionIdInsertada, habitacionIdReal, checkIn, checkOut
        );
    }

    /**
     * Elimina en orden correcto los datos de prueba: detalle, reservacion y usuario.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate("DELETE FROM Reservacion WHERE ID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate("DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Verifica el flujo completo de cancelacion contra Oracle: el service valida estado
     * y regla de 24 horas, actualiza el estado a Cancelada y registra el motivo.
     * Comprueba directamente en la tabla que el estado cambio en Oracle.
     */
    @Test
    @Order(1)
    @DisplayName("1. Cancela reservacion confirmada con check-in lejano y Oracle persiste el cambio")
    void cancelacionExitosaPersistidaEnOracle() {

        cancelacionService.cancelarReservacion(reservacionIdInsertada, usuarioIdInsertado, "Motivo IT");

        List<String> estados = DatabaseManager.executeQuery(
                "SELECT er.Estado FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE r.ID = ?",
                rs -> rs.getString("Estado"), reservacionIdInsertada
        );

        assertFalse(estados.isEmpty(),                             "La reservacion debe seguir en Oracle");
        assertEquals("cancelada", estados.get(0).toLowerCase(),   "El estado debe ser cancelada en Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la reservacion no pertenece
     * al usuario indicado y que Oracle no modifica el estado.
     */
    @Test
    @Order(2)
    @DisplayName("2. Lanza excepcion con usuario incorrecto y Oracle no cambia el estado")
    void cancelacionFallaUsuarioIncorrecto() {

        int otroUsuarioId = usuarioIdInsertado + 9999;

        assertThrows(
                IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(reservacionIdInsertada, otroUsuarioId, "motivo")
        );

        List<String> estados = DatabaseManager.executeQuery(
                "SELECT er.Estado FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE r.ID = ?",
                rs -> rs.getString("Estado"), reservacionIdInsertada
        );
        assertEquals("confirmada", estados.get(0).toLowerCase(),
                "El estado no debe haber cambiado en Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el check-in es hoy,
     * violando la regla de las 24 horas, y que Oracle mantiene el estado sin cambios.
     */
    @Test
    @Order(3)
    @DisplayName("3. Lanza excepcion con check-in hoy y Oracle mantiene el estado confirmada")
    void cancelacionFallaMenosDe24Horas() {

        // Actualiza el detalle para que el check-in sea hoy en Oracle
        DatabaseManager.executeUpdate(
                "UPDATE DetallesReservacion SET FechaCheckIn = SYSDATE WHERE ReservacionID = ?",
                reservacionIdInsertada
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cancelacionService.cancelarReservacion(reservacionIdInsertada, usuarioIdInsertado, "motivo")
        );

        List<String> estados = DatabaseManager.executeQuery(
                "SELECT er.Estado FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE r.ID = ?",
                rs -> rs.getString("Estado"), reservacionIdInsertada
        );
        assertEquals("confirmada", estados.get(0).toLowerCase(),
                "El estado debe seguir siendo confirmada en Oracle");
    }
}