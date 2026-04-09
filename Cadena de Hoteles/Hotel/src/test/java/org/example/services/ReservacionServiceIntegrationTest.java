package org.example.services;

import org.example.data.DatabaseManager;
import org.example.dtos.HabitacionReservaRequestDTO;
import org.example.dtos.ReservacionRequestDTO;
import org.example.dtos.ReservacionResponseDTO;
import org.example.helpers.PasswordHelper;
import org.example.repositories.ReservacionRepository;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para ReservacionService.
 * Conecta a Oracle real, inserta un usuario de prueba y usa una habitacion
 * existente en la base de datos para ejecutar el flujo completo de creacion
 * de reservaciones. Limpia todos los registros insertados al finalizar cada caso.
 * Requiere que Oracle este corriendo en localhost:1521/XEPDB1.
 */
@DisplayName("Integracion: ReservacionService - Flujo de Creacion de Reservaciones")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservacionServiceIntegrationTest {

    private ReservacionService reservacionService;
    private int usuarioIdInsertado;
    private int habitacionIdReal;
    private int reservacionIdInsertada = -1;

    /**
     * Inicializa el service con el repositorio real, inserta un usuario de prueba
     * y obtiene el ID de una habitacion existente en Oracle para usar en los casos.
     */
    @BeforeEach
    void setUp() {
        reservacionService = new ReservacionService(new ReservacionRepository());

        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 2, 'IT-RES')",
                "ID",
                "test_res_integration",
                "test_res_integration@hotel.com",
                PasswordHelper.hashear("TestPass123")
        );

        List<Integer> habitaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Habitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!habitaciones.isEmpty(), "No hay habitaciones en Oracle, se omite la prueba");
        habitacionIdReal = habitaciones.get(0);
    }

    /**
     * Elimina en orden correcto los datos insertados en Oracle: primero los detalles,
     * luego la reservacion y finalmente el usuario de prueba.
     */
    @AfterEach
    void tearDown() {
        if (reservacionIdInsertada != -1) {
            DatabaseManager.executeUpdate(
                    "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionIdInsertada);
            DatabaseManager.executeUpdate(
                    "DELETE FROM Reservacion WHERE ID = ?", reservacionIdInsertada);
        }
        DatabaseManager.executeUpdate("DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    /**
     * Construye un request de reservacion con una sola habitacion y fechas futuras validas.
     * @param habitacionId ID de la habitacion a reservar.
     * @param personas     cantidad de personas.
     * @return request listo para pasar al service.
     */
    private ReservacionRequestDTO crearRequest(int habitacionId, int personas) {
        HabitacionReservaRequestDTO hab = new HabitacionReservaRequestDTO();
        hab.setHabitacionId(habitacionId);
        hab.setCantidadPersonas(personas);
        hab.setFechaCheckIn(LocalDate.now().plusDays(30).toString());
        hab.setFechaCheckOut(LocalDate.now().plusDays(33).toString());

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(List.of(hab));
        return request;
    }

    /**
     * Verifica el flujo completo de creacion de una reservacion contra Oracle:
     * el service valida fechas, consulta disponibilidad, inserta en Reservacion
     * y DetallesReservacion, y retorna el DTO con los datos reales generados por Oracle.
     */
    @Test
    @Order(1)
    @DisplayName("1. Crea reservacion real en Oracle y el registro queda persistido")
    void crearReservacionExitosa() {

        ReservacionResponseDTO respuesta =
                reservacionService.crearReservacion(crearRequest(habitacionIdReal, 1), usuarioIdInsertado);

        reservacionIdInsertada = respuesta.getId();

        assertNotNull(respuesta,                                        "La respuesta no debe ser null");
        assertTrue(respuesta.getId() > 0,                               "Oracle debe generar un ID positivo");
        assertTrue(respuesta.getNoReservacion().startsWith("MIKU-"),    "Debe tener el prefijo MIKU-");
        assertEquals("pendiente", respuesta.getEstado().toLowerCase(),  "El estado inicial debe ser pendiente en Oracle");
        assertTrue(respuesta.getTotal() > 0,                            "El total calculado debe ser mayor a cero");

        // Verifica que el registro existe fisicamente en Oracle
        List<Integer> existe = DatabaseManager.executeQuery(
                "SELECT ID FROM Reservacion WHERE ID = ?",
                rs -> rs.getInt("ID"), respuesta.getId()
        );
        assertFalse(existe.isEmpty(), "La reservacion debe existir fisicamente en la tabla Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el request llega
     * sin habitaciones y que Oracle no recibe ninguna insercion.
     */
    @Test
    @Order(2)
    @DisplayName("2. Lanza excepcion sin habitaciones y Oracle no recibe ninguna insercion")
    void crearReservacionFallaSinHabitaciones() {

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservacionService.crearReservacion(request, usuarioIdInsertado)
        );

        List<Integer> reservaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Reservacion WHERE Usuario_ID = ?",
                rs -> rs.getInt("ID"), usuarioIdInsertado
        );
        assertTrue(reservaciones.isEmpty(), "No debe haberse insertado nada en Oracle");
    }

    /**
     * Verifica que se lanza IllegalArgumentException cuando el check-in es
     * una fecha anterior a hoy y que Oracle no recibe ninguna insercion.
     */
    @Test
    @Order(3)
    @DisplayName("3. Lanza excepcion con check-in en el pasado y Oracle no recibe ninguna insercion")
    void crearReservacionFallaFechaPasada() {

        HabitacionReservaRequestDTO hab = new HabitacionReservaRequestDTO();
        hab.setHabitacionId(habitacionIdReal);
        hab.setCantidadPersonas(1);
        hab.setFechaCheckIn(LocalDate.now().minusDays(2).toString());
        hab.setFechaCheckOut(LocalDate.now().plusDays(1).toString());

        ReservacionRequestDTO request = new ReservacionRequestDTO();
        request.setHabitaciones(List.of(hab));

        assertThrows(
                IllegalArgumentException.class,
                () -> reservacionService.crearReservacion(request, usuarioIdInsertado)
        );

        List<Integer> reservaciones = DatabaseManager.executeQuery(
                "SELECT ID FROM Reservacion WHERE Usuario_ID = ?",
                rs -> rs.getInt("ID"), usuarioIdInsertado
        );
        assertTrue(reservaciones.isEmpty(), "Oracle no debe tener ninguna reservacion del usuario de prueba");
    }
}