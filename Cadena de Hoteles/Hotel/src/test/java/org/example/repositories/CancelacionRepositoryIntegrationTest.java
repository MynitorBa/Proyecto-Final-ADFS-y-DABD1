package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link CancelacionRepository}.
 * <p>
 * Conecta a Oracle real para verificar las operaciones de cancelacion de reservaciones,
 * incluyendo la consulta de reservaciones por usuario, la obtencion de la fecha de
 * check-in mas reciente, la actualizacion del estado a cancelada y la busqueda
 * de reservaciones por agencia.
 * </p>
 * <p>
 * El {@code @BeforeEach} inserta un usuario y una reservacion de prueba directamente
 * via SQL con datos conocidos. El {@code @AfterEach} elimina esos registros en orden
 * FK-inverso (primero Reservacion, luego Usuario) para no dejar residuos en Oracle
 * independientemente del resultado de cada caso.
 * </p>
 * <p>
 * No se insertan {@code DetallesReservacion} en el setup de esta suite para poder
 * verificar el comportamiento de {@code obtenerFechaCheckInMasReciente} cuando no
 * existen detalles asociados a la reservacion.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las tablas
 * {@code Usuario}, {@code Reservacion}, {@code EstadoReserva} y {@code Agencia} accesibles.
 * </p>
 */
@DisplayName("Integracion: CancelacionRepository - Cancelacion de reservaciones contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CancelacionRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private CancelacionRepository cancelacionRepository;

    /** ID del usuario de prueba insertado en {@code @BeforeEach}. */
    private int usuarioIdInsertado;

    /** ID de la reservacion de prueba insertada en {@code @BeforeEach}. */
    private int reservacionIdInsertada;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME   = "test_cancel_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO     = "test_cancel_repo@hotel.com";

    /** Numero de pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE  = "IT-CAN-001";

    /** Numero de reservacion fijo de la reservacion de prueba. */
    private static final String NO_RESERVACION = "RES-CANCEL-TEST-001";

    /**
     * Crea una nueva instancia del repositorio, inserta un usuario de prueba con
     * contrasena hasheada en Oracle y, a continuacion, inserta una reservacion
     * asociada a ese usuario con estado pendiente (EstadoID = 1).
     * <p>
     * Si alguno de los INSERT no retorna un ID valido la prueba se omite mediante
     * {@link Assumptions#assumeTrue}, evitando falsos negativos por problemas del entorno.
     * </p>
     */
    @BeforeEach
    void setUp() {
        cancelacionRepository = new CancelacionRepository();

        // 1. Inserta el usuario de prueba
        usuarioIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioIdInsertado > 0,
                "El INSERT de Usuario no retorno un ID valido; se omite la prueba");

        // 2. Inserta la reservacion de prueba con estado Pendiente (EstadoID = 1)
        reservacionIdInsertada = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES (?, ?, 1, ?, SYSDATE, SYSDATE + 1)",
                "ID",
                NO_RESERVACION,
                100.0,
                usuarioIdInsertado
        );
        Assumptions.assumeTrue(reservacionIdInsertada > 0,
                "El INSERT de Reservacion no retorno un ID valido; se omite la prueba");
    }

    /**
     * Elimina la reservacion y el usuario de prueba en orden FK-inverso, garantizando
     * que ningun caso deje residuos en Oracle independientemente de su resultado.
     * La reservacion se elimina primero porque tiene FK hacia Usuario.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code obtenerReservacionParaCancelar} retorne un arreglo no nulo
     * con los datos correctos cuando la reservacion pertenece al usuario indicado.
     * <p>
     * Se afirma que el arreglo no es nulo, que el elemento [0] (ID) coincide con el
     * ID de la reservacion insertada y que el elemento [2] (Estado) es una cadena
     * no nula, confirmando que el JOIN con {@code EstadoReserva} funciona correctamente.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerReservacionParaCancelar con reservacion del usuario retorna datos correctos")
    void obtenerReservacionParaCancelar_reservacionDelUsuario_retornaDatos() {
        Object[] resultado = cancelacionRepository.obtenerReservacionParaCancelar(
                reservacionIdInsertada, usuarioIdInsertado);

        assertNotNull(resultado,
                "El arreglo retornado no debe ser null para una reservacion existente del usuario");
        assertEquals(reservacionIdInsertada, ((Number) resultado[0]).intValue(),
                "El elemento [0] del arreglo debe ser el ID de la reservacion");
        assertNotNull(resultado[2],
                "El elemento [2] (Estado) no debe ser null; el JOIN con EstadoReserva debe resolverse");
    }

    /**
     * Verifica que {@code obtenerReservacionParaCancelar} retorne {@code null} cuando
     * el {@code usuarioId} proporcionado no corresponde al propietario de la reservacion.
     * <p>
     * Se usa {@code usuarioId = -999} que no puede coincidir con ningun registro real,
     * garantizando que el filtro {@code r.Usuario_ID = ?} rechace la consulta.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerReservacionParaCancelar con otro usuarioId retorna null")
    void obtenerReservacionParaCancelar_otroUsuarioId_retornaNull() {
        Object[] resultado = cancelacionRepository.obtenerReservacionParaCancelar(
                reservacionIdInsertada, -999);

        assertNull(resultado,
                "Debe retornar null cuando el usuarioId no corresponde al propietario de la reservacion");
    }

    /**
     * Verifica que {@code obtenerFechaCheckInMasReciente} retorne {@code null} cuando
     * la reservacion no tiene filas en {@code DetallesReservacion}.
     * <p>
     * El setup de esta suite no inserta detalles deliberadamente, por lo que la
     * consulta {@code MIN(FechaCheckIn)} no debe encontrar registros y el repositorio
     * debe retornar {@code null} en lugar de una fecha.
     * </p>
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerFechaCheckInMasReciente sin detalles retorna null")
    void obtenerFechaCheckInMasReciente_sinDetalles_retornaNull() {
        java.sql.Date fecha = cancelacionRepository.obtenerFechaCheckInMasReciente(
                reservacionIdInsertada);

        assertNull(fecha,
                "Debe retornar null cuando la reservacion no tiene DetallesReservacion asociados");
    }

    /**
     * Verifica que {@code cancelarReservacion} actualice el {@code EstadoID} de la
     * reservacion a {@code 4} (Cancelada) en Oracle.
     * <p>
     * Tras invocar el metodo se consulta directamente la tabla {@code Reservacion}
     * para leer el {@code EstadoID} actualizado y confirmar que el UPDATE fue aplicado.
     * </p>
     */
    @Test
    @Order(4)
    @DisplayName("4. cancelarReservacion con estado pendiente actualiza a EstadoID 4")
    void cancelarReservacion_estadoPendiente_actualizaAEstado4() {
        cancelacionRepository.cancelarReservacion(reservacionIdInsertada, "Test motivo");

        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Reservacion WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                reservacionIdInsertada
        );

        assertFalse(estados.isEmpty(),
                "La consulta de verificacion debe retornar al menos una fila");
        assertEquals(4, estados.get(0).intValue(),
                "El EstadoID debe ser 4 (Cancelada) tras invocar cancelarReservacion");
    }

    /**
     * Verifica que {@code obtenerReservacionAgenciaParaCancelar} retorne {@code null}
     * cuando no existe ninguna {@code Agencia} cuyo {@code usuariowebis_id} coincida
     * con el {@code Usuario_ID} de la reservacion.
     * <p>
     * Se usa {@code agenciaId = -999} que no puede coincidir con ningun registro real,
     * garantizando que el JOIN con {@code Agencia} no produzca resultados y el metodo
     * retorne {@code null}.
     * </p>
     */
    @Test
    @Order(5)
    @DisplayName("5. obtenerReservacionAgenciaParaCancelar sin agencia vinculada retorna null")
    void obtenerReservacionAgenciaParaCancelar_sinAgenciaVinculada_retornaNull() {
        Object[] resultado = cancelacionRepository.obtenerReservacionAgenciaParaCancelar(
                reservacionIdInsertada, -999);

        assertNull(resultado,
                "Debe retornar null cuando no existe Agencia vinculada a la reservacion con el ID dado");
    }
}
