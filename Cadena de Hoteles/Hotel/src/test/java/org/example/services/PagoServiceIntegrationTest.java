package org.example.services;

import org.example.data.DatabaseManager;
import org.example.dtos.PagoRequestDTO;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.PasswordHelper;
import org.example.repositories.PagoRepository;
import org.example.repositories.TokenValidacionRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para PagoService.
 * Conecta a Oracle real, inserta un usuario y una reservacion pendiente con su detalle
 * antes de cada caso y limpia todos los registros al finalizar.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: PagoService - Flujo de Pago")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PagoServiceIntegrationTest {

    private PagoService pagoService;
    private int usuarioIdInsertado;
    private int reservacionIdInsertada;
    private int habitacionIdReal;
    private int facturaIdInsertada = -1;

    /**
     * Inserta en Oracle un usuario de prueba, una reservacion en estado pendiente
     * y un detalle con fechas futuras antes de cada caso.
     */
    @BeforeEach
    void setUp() {
        pagoService = new PagoService(new PagoRepository(), new TokenValidacionRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 2, 'IT-PAGO')",
                "ID",
                "test_pago_integration",
                "test_pago_integration@hotel.com",
                PasswordHelper.hashear("TestPass123")
        );

        // Reservacion en estado Pendiente (EstadoID = 1)
        reservacionIdInsertada = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES ('IT-PAGO-001', 300.0, 1, ?, SYSDATE, SYSDATE + 1)",
                "ID",
                usuarioIdInsertado
        );

        List<Integer> habitaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Habitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!habitaciones.isEmpty(), "No hay habitaciones en Oracle, se omite la prueba");
        habitacionIdReal = habitaciones.get(0);

        String checkIn  = LocalDate.now().plusDays(20).toString();
        String checkOut = LocalDate.now().plusDays(23).toString();
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                        "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), 1, 300.0)",
                reservacionIdInsertada, habitacionIdReal, checkIn, checkOut
        );
    }

    /**
     * Elimina en orden correcto los datos insertados en Oracle: factura, detalle,
     * reservacion y usuario de prueba.
     */
    @AfterEach
    void tearDown() {
        if (facturaIdInsertada != -1) {
            DatabaseManager.executeUpdate("DELETE FROM Factura WHERE ID = ?", facturaIdInsertada);
        }
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate("DELETE FROM Reservacion WHERE ID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate("DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Construye un PagoRequestDTO con datos de tarjeta validos y sin token de alianza.
     * @return request listo para los casos de prueba de pago.
     */
    private PagoRequestDTO buildRequestValido() {
        PagoRequestDTO request = new PagoRequestDTO();
        request.setNumeroTarjeta("1234567890123456");
        request.setNombreTitular("Test Integration");
        request.setFechaVencimiento("12/30");
        request.setCvv("123");
        request.setNit("CF");
        request.setCodigoPostal("01001");
        return request;
    }

    /**
     * Verifica el flujo completo de pago contra Oracle: el service confirma la reservacion,
     * crea la factura y retorna el DTO. Comprueba que el estado en Oracle cambie a Confirmada
     * y que la factura quede registrada en la tabla Factura.
     */
    @Test
    @Order(1)
    @DisplayName("1. Pago exitoso confirma reservacion y crea factura en Oracle")
    void pagoExitosoConfirmaReservacionYCreaFactura() {

        PagoResponseDTO respuesta =
                pagoService.procesarPago(reservacionIdInsertada, usuarioIdInsertado, buildRequestValido());

        facturaIdInsertada = respuesta.getFacturaId();

        assertNotNull(respuesta,                                       "La respuesta no debe ser null");
        assertTrue(respuesta.getFacturaId() > 0,                       "Oracle debe generar un ID de factura positivo");
        assertEquals("CF",    respuesta.getNit(),                      "El NIT debe coincidir con el enviado");
        assertEquals("01001", respuesta.getCodigoPostal(),             "El codigo postal debe coincidir");

        // Verifica que el estado cambio a Confirmada en Oracle
        List<String> estados = DatabaseManager.executeQuery(
                "SELECT er.Estado FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE r.ID = ?",
                rs -> rs.getString("Estado"), reservacionIdInsertada
        );
        assertEquals("confirmada", estados.get(0).toLowerCase(),
                "El estado debe ser confirmada en Oracle tras el pago");

        // Verifica que la factura existe en Oracle
        List<Integer> facturas = DatabaseManager.executeQuery(
                "SELECT ID FROM Factura WHERE ReservacionID = ?",
                rs -> rs.getInt("ID"), reservacionIdInsertada
        );
        assertFalse(facturas.isEmpty(), "Debe existir una factura en Oracle asociada a la reservacion");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando la reservacion no existe
     * o no pertenece al usuario, y que Oracle no registra ninguna factura.
     */
    @Test
    @Order(2)
    @DisplayName("2. Lanza excepcion cuando la reservacion no pertenece al usuario y Oracle no crea factura")
    void pagoFallaReservacionDeOtroUsuario() {

        int otroUsuarioId = usuarioIdInsertado + 9999;

        assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.procesarPago(reservacionIdInsertada, otroUsuarioId, buildRequestValido())
        );

        List<Integer> facturas = DatabaseManager.executeQuery(
                "SELECT ID FROM Factura WHERE ReservacionID = ?",
                rs -> rs.getInt("ID"), reservacionIdInsertada
        );
        assertTrue(facturas.isEmpty(), "No debe haberse creado ninguna factura en Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el CVV de la tarjeta
     * es invalido y que Oracle no cambia el estado de la reservacion ni crea factura.
     */
    @Test
    @Order(3)
    @DisplayName("3. Lanza excepcion con CVV invalido y Oracle no cambia el estado de la reservacion")
    void pagoFallaCvvInvalido() {

        PagoRequestDTO request = buildRequestValido();
        request.setCvv("12");

        assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.procesarPago(reservacionIdInsertada, usuarioIdInsertado, request)
        );

        // Verifica que el estado sigue siendo Pendiente en Oracle
        List<String> estados = DatabaseManager.executeQuery(
                "SELECT er.Estado FROM Reservacion r " +
                        "JOIN EstadoReserva er ON r.EstadoID = er.ID WHERE r.ID = ?",
                rs -> rs.getString("Estado"), reservacionIdInsertada
        );
        assertEquals("pendiente", estados.get(0).toLowerCase(),
                "El estado debe seguir siendo pendiente en Oracle");
    }
}