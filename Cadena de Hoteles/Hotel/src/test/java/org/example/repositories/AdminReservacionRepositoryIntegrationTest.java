package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link AdminReservacionRepository}.
 * <p>
 * Conecta a Oracle real para verificar el listado de todas las reservaciones,
 * la obtencion de datos de una reservacion especifica, la obtencion de datos
 * del usuario por reservacion y la cancelacion de reservaciones.
 * </p>
 * <p>
 * El {@code @BeforeEach} construye el grafo completo de datos en orden FK:
 * ciudad, estado de hotel, tipo de habitacion, estado de habitacion, usuario,
 * hotel, habitacion, reservacion y detalle de reservacion. El
 * {@code @AfterEach} elimina en orden FK-inverso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Estado}, {@code TipoHabitacion},
 * {@code EstadoHabitacion}, {@code EstadoReserva}, {@code Usuario},
 * {@code Hotel}, {@code Habitacion}, {@code Reservacion} y
 * {@code DetallesReservacion} accesibles.
 * </p>
 */
@DisplayName("Integracion: AdminReservacionRepository - Administracion de reservaciones contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminReservacionRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private AdminReservacionRepository adminReservacionRepository;

    /** ID del usuario de prueba. */
    private int usuarioId;

    /** ID del hotel de prueba. */
    private int hotelId;

    /** ID de la habitacion de prueba. */
    private int habitacionId;

    /** ID de la reservacion de prueba. */
    private int reservacionId;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME       = "test_admin_res_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO         = "test_admin_res_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE      = "IT-ADMRES-001";

    /** Numero de reservacion fijo. */
    private static final String NO_RESERVACION = "RES-ADMRES-REPO-001";

    /**
     * Inicializa el repositorio y construye el grafo completo de datos de prueba
     * en Oracle. Realiza pre-limpieza defensiva por identificadores fijos.
     */
    @BeforeEach
    void setUp() {
        adminReservacionRepository = new AdminReservacionRepository();

        // 0. Limpieza defensiva
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID IN " +
                        "(SELECT ID FROM Reservacion WHERE No_Reservacion = ?)",
                NO_RESERVACION
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE No_Reservacion = ?",
                NO_RESERVACION
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE Descripcion = ?",
                "Habitacion IT AdminRes"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE Nombre = ?",
                "Hotel IT AdminRes"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE Username = ?",
                USERNAME
        );

        // 1. Obtiene ciudad existente
        List<Integer> ciudades = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle — se omite la prueba");
        int ciudadId = ciudades.get(0);

        // 2. Obtiene estado de hotel existente
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM Estado WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay estados de hotel en Oracle — se omite la prueba");
        int estadoId = estados.get(0);

        // 3. Obtiene tipo de habitacion existente
        List<Integer> tipos = DatabaseManager.executeQuery(
                "SELECT ID FROM TipoHabitacion WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!tipos.isEmpty(),
                "No hay tipos de habitacion en Oracle — se omite la prueba");
        int tipoId = tipos.get(0);

        // 4. Obtiene estado de habitacion existente
        List<Integer> estadosHab = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoHabitacion WHERE ROWNUM = 1", rs -> rs.getInt("ID"));
        Assumptions.assumeTrue(!estadosHab.isEmpty(),
                "No hay estados de habitacion en Oracle — se omite la prueba");
        int estadoHabId = estadosHab.get(0);

        // 5. Inserta usuario de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID", USERNAME, CORREO,
                PasswordHelper.hashear("TestPass123"), PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario de prueba — se omite la prueba");

        // 6. Inserta hotel de prueba
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID", "Hotel IT AdminRes", "Calle IT AdminRes 1",
                "Desc IT AdminRes", 4.0, estadoId, ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "No se pudo insertar el hotel de prueba — se omite la prueba");

        // 7. Inserta habitacion de prueba
        habitacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Habitacion (HOTELID, TIPOHABITACIONID, ESTADO_ID, Descripcion) " +
                        "VALUES (?, ?, ?, ?)",
                "ID", hotelId, tipoId, estadoHabId, "Habitacion IT AdminRes"
        );
        Assumptions.assumeTrue(habitacionId > 0,
                "No se pudo insertar la habitacion de prueba — se omite la prueba");

        // 8. Inserta reservacion de prueba
        Timestamp ahora     = new Timestamp(System.currentTimeMillis());
        Timestamp expiracion = new Timestamp(System.currentTimeMillis() + 3_600_000L);
        reservacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES (?, 350.0, 1, ?, ?, ?)",
                "ID", NO_RESERVACION, usuarioId, ahora, expiracion
        );
        Assumptions.assumeTrue(reservacionId > 0,
                "No se pudo insertar la reservacion de prueba — se omite la prueba");

        // 9. Inserta detalle de reservacion
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                        "VALUES (?, ?, DATE '2031-07-01', DATE '2031-07-05', 2, 350.0)",
                reservacionId, habitacionId
        );
    }

    /**
     * Elimina en orden FK-inverso todos los registros insertados en el setup.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?", reservacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE Usuario_ID = ?", usuarioId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID = ?", habitacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?", hotelId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link AdminReservacionRepository#listarTodas} retorne una
     * lista no nula y que la reservacion de prueba este incluida en ella.
     */
    @Test
    @Order(1)
    @DisplayName("1. listarTodas retorna lista no nula que incluye la reservacion de prueba")
    void listarTodas_retornaListaConReservacionDePrueba() {
        List<Map<String, Object>> lista = adminReservacionRepository.listarTodas();

        assertNotNull(lista,
                "listarTodas no debe retornar null");
        boolean encontrada = lista.stream()
                .anyMatch(row -> NO_RESERVACION.equals(row.get("noReservacion")));
        assertTrue(encontrada,
                "La reservacion de prueba debe aparecer en listarTodas");
    }

    /**
     * Verifica que {@link AdminReservacionRepository#obtenerReservacion} retorne
     * un arreglo no nulo con el ID correcto para la reservacion de prueba.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerReservacion con ID existente retorna datos correctos")
    void obtenerReservacion_idExistente_retornaDatos() {
        Object[] datos = adminReservacionRepository.obtenerReservacion(reservacionId);

        assertNotNull(datos,
                "obtenerReservacion no debe retornar null para una reservacion existente");
        assertEquals(reservacionId, datos[0],
                "El primer elemento debe ser el ID de la reservacion");
        assertNotNull(datos[2],
                "El tercer elemento (Estado) no debe ser null");
    }

    /**
     * Verifica que {@link AdminReservacionRepository#obtenerReservacion} retorne
     * {@code null} para un ID que no existe en la base de datos.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerReservacion con ID inexistente retorna null")
    void obtenerReservacion_idInexistente_retornaNull() {
        Object[] datos = adminReservacionRepository.obtenerReservacion(-999);

        assertNull(datos,
                "obtenerReservacion debe retornar null para un ID que no existe");
    }

    /**
     * Verifica que {@link AdminReservacionRepository#obtenerDatosUsuarioPorReservacion}
     * retorne un arreglo con el correo correcto del usuario de prueba.
     */
    @Test
    @Order(4)
    @DisplayName("4. obtenerDatosUsuarioPorReservacion retorna correo del usuario")
    void obtenerDatosUsuarioPorReservacion_reservacionExistente_retornaDatos() {
        Object[] datos = adminReservacionRepository.obtenerDatosUsuarioPorReservacion(reservacionId);

        assertNotNull(datos,
                "obtenerDatosUsuarioPorReservacion no debe retornar null para una reservacion existente");
        assertEquals(4, datos.length,
                "El arreglo debe tener 4 elementos: correo, nombre, noReservacion, total");
        assertEquals(CORREO, datos[0],
                "El primer elemento debe ser el correo del usuario de prueba");
        assertEquals(NO_RESERVACION, datos[2],
                "El tercer elemento debe ser el numero de reservacion");
    }

    /**
     * Verifica que {@link AdminReservacionRepository#cancelarReservacion} cambie
     * el EstadoID de la reservacion a 4 (Cancelada) en Oracle.
     */
    @Test
    @Order(5)
    @DisplayName("5. cancelarReservacion actualiza el estado a Cancelada en Oracle")
    void cancelarReservacion_reservacionPendiente_cambiaEstado() {
        adminReservacionRepository.cancelarReservacion(reservacionId, "Motivo de prueba IT");

        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Reservacion WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                reservacionId
        );
        assertFalse(estados.isEmpty(),
                "La reservacion debe existir en Oracle tras cancelar");
        assertEquals(4, estados.get(0),
                "El EstadoID debe ser 4 (Cancelada) tras llamar a cancelarReservacion");
    }
}
