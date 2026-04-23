package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link PagoAgenciaRepository}.
 * <p>
 * Conecta a Oracle real para verificar las operaciones de pago de reservaciones
 * realizadas por agencias: obtencion de la reservacion para pago verificando la
 * pertenencia a la agencia, confirmacion de la reservacion, creacion de factura
 * y consulta de factura existente.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene registros maestros existentes en Oracle
 * (Ciudad, Estado de hotel, TipoHabitacion, EstadoHabitacion) mediante
 * {@link Assumptions#assumeTrue} y luego inserta en FK-orden: Usuario webservice,
 * Agencia, Hotel, Habitacion, Reservacion (vinculada al usuario webservice de la
 * agencia) y DetallesReservacion.
 * </p>
 * <p>
 * El {@code @AfterEach} elimina todos esos registros en FK-orden inverso:
 * Factura residual → DetallesReservacion → Reservacion → Habitacion → Hotel →
 * Agencia → Usuario, para no dejar residuos independientemente del resultado.
 * </p>
 * <p>
 * Los tests que crean {@code Factura} limpian el registro dentro del propio
 * metodo con un bloque {@code finally}, ya que el ID generado solo esta disponible
 * durante la ejecucion del caso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las tablas
 * {@code Ciudad}, {@code Estado}, {@code TipoHabitacion}, {@code EstadoHabitacion},
 * {@code Usuario}, {@code Agencia}, {@code Hotel}, {@code Habitacion},
 * {@code Reservacion}, {@code DetallesReservacion}, {@code EstadoReserva} y
 * {@code Factura} accesibles.
 * </p>
 */
@DisplayName("Integracion: PagoAgenciaRepository - Procesamiento de pagos de agencias contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PagoAgenciaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private PagoAgenciaRepository pagoAgenciaRepository;

    /** ID del usuario webservice de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID de la agencia de prueba insertada en {@code @BeforeEach}. */
    private int agenciaId;

    /** ID del hotel de prueba insertado en {@code @BeforeEach}. */
    private int hotelId;

    /** ID de la habitacion de prueba insertada en {@code @BeforeEach}. */
    private int habitacionId;

    /** ID de la reservacion de prueba insertada en {@code @BeforeEach}. */
    private int reservacionId;

    /** ID de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private int ciudadIdReal;

    /** ID del estado de hotel obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoHotelIdReal;

    /** ID del tipo de habitacion obtenido de Oracle en {@code @BeforeEach}. */
    private int tipoHabitacionIdReal;

    /** ID del estado de habitacion obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoHabitacionIdReal;

    /** Username fijo del usuario webservice de prueba. */
    private static final String USERNAME      = "test_pag_agc_repo";

    /** Correo fijo del usuario webservice de prueba. */
    private static final String CORREO        = "test_pag_agc_repo@hotel.com";

    /** Pasaporte fijo del usuario webservice de prueba. */
    private static final String PASAPORTE     = "IT-PAGAGC-001";

    /** Numero de reservacion fijo de la reservacion de prueba. */
    private static final String NO_RESERVACION = "RES-PAG-AGC-001";

    /**
     * Inicializa el repositorio, obtiene registros maestros existentes en Oracle
     * (Ciudad, Estado de hotel, TipoHabitacion, EstadoHabitacion) y luego inserta
     * en FK-orden: Usuario webservice, Agencia, Hotel, Habitacion, Reservacion y
     * DetallesReservacion. La Reservacion se vincula al usuario webservice de la
     * agencia, ya que las reservaciones de agencia pertenecen a ese usuario.
     * <p>
     * Si no se pueden obtener los datos maestros o si algun INSERT falla, la prueba
     * completa se omite mediante {@link Assumptions#assumeTrue} para evitar falsos
     * negativos por problemas del entorno de prueba.
     * </p>
     */
    @BeforeEach
    void setUp() {
        pagoAgenciaRepository = new PagoAgenciaRepository();

        // 1. Obtiene una Ciudad existente en Oracle
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Ciudad WHERE ROWNUM = 1",
                rs -> new Object[]{rs.getInt("ID"), rs.getString("Nombre")}
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay Ciudad en Oracle; se omite la prueba");
        ciudadIdReal = (int) ciudades.get(0)[0];

        // 2. Obtiene un Estado de hotel existente en Oracle
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM Estado WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay Estado en Oracle; se omite la prueba");
        estadoHotelIdReal = estados.get(0);

        // 3. Obtiene un TipoHabitacion existente en Oracle
        List<Integer> tiposHab = DatabaseManager.executeQuery(
                "SELECT ID FROM TipoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!tiposHab.isEmpty(),
                "No hay TipoHabitacion en Oracle; se omite la prueba");
        tipoHabitacionIdReal = tiposHab.get(0);

        // 4. Obtiene un EstadoHabitacion existente en Oracle
        List<Integer> estadosHab = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estadosHab.isEmpty(),
                "No hay EstadoHabitacion en Oracle; se omite la prueba");
        estadoHabitacionIdReal = estadosHab.get(0);

        // 5. Inserta el Usuario webservice de prueba con Rol_ID = 3
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 3, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "El INSERT de Usuario no retorno un ID valido; se omite la prueba");

        // 6. Inserta la Agencia vinculada al usuario webservice de prueba
        agenciaId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento, EstadoID, URL_Agencia) " +
                        "VALUES (?, ?, ?, 0, 1, ?)",
                "ID",
                "Agencia Pago Test IT",
                "agencia_pago_it@test.com",
                usuarioId,
                "http://agencia-pago-test.com"
        );
        Assumptions.assumeTrue(agenciaId > 0,
                "El INSERT de Agencia no retorno un ID valido; se omite la prueba");

        // 7. Inserta el Hotel de prueba
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID",
                "Hotel Pago Agencia Test",
                "Calle Pago Agencia 1",
                "Desc Pago Agencia",
                0.0,
                estadoHotelIdReal,
                ciudadIdReal
        );
        Assumptions.assumeTrue(hotelId > 0,
                "El INSERT de Hotel no retorno un ID valido; se omite la prueba");

        // 8. Inserta la Habitacion de prueba
        habitacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Habitacion (HOTELID, TIPOHABITACIONID, ESTADO_ID, Descripcion) " +
                        "VALUES (?, ?, ?, ?)",
                "ID",
                hotelId,
                tipoHabitacionIdReal,
                estadoHabitacionIdReal,
                "Hab Pago Agencia Test"
        );
        Assumptions.assumeTrue(habitacionId > 0,
                "El INSERT de Habitacion no retorno un ID valido; se omite la prueba");

        // 9. Inserta la Reservacion vinculada al usuario webservice (dueno de la agencia)
        //    con estado Pendiente (EstadoID = 1)
        reservacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES (?, ?, 1, ?, SYSDATE, SYSDATE + 1)",
                "ID",
                NO_RESERVACION,
                750.0,
                usuarioId
        );
        Assumptions.assumeTrue(reservacionId > 0,
                "El INSERT de Reservacion no retorno un ID valido; se omite la prueba");

        // 10. Inserta DetallesReservacion para completar la cadena FK
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                        "VALUES (?, ?, TO_DATE('2030-08-01','YYYY-MM-DD'), TO_DATE('2030-08-05','YYYY-MM-DD'), 2, 750.0)",
                reservacionId,
                habitacionId
        );
    }

    /**
     * Elimina en FK-orden inverso todos los registros insertados durante el setup:
     * primero cualquier Factura residual (asociada a la reservacion de prueba), luego
     * DetallesReservacion, Reservacion, Habitacion, Hotel, Agencia y finalmente Usuario.
     * Garantiza que ningun caso deje residuos en Oracle independientemente del resultado.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Factura WHERE ReservacionID = ?", reservacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?", reservacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID = ?", habitacionId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?", hotelId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Agencia WHERE ID = ?", agenciaId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link PagoAgenciaRepository#obtenerReservacionParaPago} retorne un
     * arreglo no nulo con los campos correctos cuando la reservacion pertenece a la
     * agencia indicada. La reservacion pertenece al usuario webservice dueno de la
     * agencia, que fue insertado en el setup.
     * <p>
     * Se comprueba que el arreglo no sea nulo, que el elemento [0] (ID) coincida con el
     * ID de la reservacion insertada y que el elemento [1] (No_Reservacion) sea
     * exactamente {@code "RES-PAG-AGC-001"}.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerReservacionParaPago con reservacion de agencia retorna datos correctos")
    void obtenerReservacionParaPago_reservacionDeAgencia_retornaDatos() {
        Object[] resultado = pagoAgenciaRepository.obtenerReservacionParaPago(
                reservacionId, agenciaId);

        assertNotNull(resultado,
                "El arreglo retornado no debe ser null para una reservacion de la agencia de prueba");
        assertEquals(reservacionId, ((Number) resultado[0]).intValue(),
                "El elemento [0] debe ser el ID de la reservacion insertada en el setup");
        assertNotNull(resultado[1],
                "El elemento [1] (No_Reservacion) no debe ser null");
        assertEquals(NO_RESERVACION, resultado[1],
                "El elemento [1] debe ser el numero de reservacion exacto del setup");
    }

    /**
     * Verifica que {@link PagoAgenciaRepository#obtenerReservacionParaPago} retorne
     * {@code null} cuando el {@code agenciaId} proporcionado no corresponde a la agencia
     * propietaria de la reservacion. Se usa el ID ficticio {@code -999} que no puede
     * coincidir con ninguna agencia real en Oracle.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerReservacionParaPago con agenciaId incorrecto retorna null")
    void obtenerReservacionParaPago_agenciaIncorrecta_retornaNull() {
        Object[] resultado = pagoAgenciaRepository.obtenerReservacionParaPago(
                reservacionId, -999);

        assertNull(resultado,
                "Debe retornar null cuando el agenciaId no corresponde a la propietaria de la reservacion");
    }

    /**
     * Verifica que {@link PagoAgenciaRepository#confirmarReservacion} actualice el
     * {@code EstadoID} a {@code 2} (Confirmada) y establezca {@code Fecha_Expiracion}
     * a {@code NULL} en Oracle. Tras invocar el metodo se consulta directamente la
     * tabla {@code Reservacion} para leer ambas columnas y verificar su nuevo valor.
     */
    @Test
    @Order(3)
    @DisplayName("3. confirmarReservacion con estado pendiente actualiza a EstadoID 2")
    void confirmarReservacion_estadoPendiente_actualizaAEstado2() {
        pagoAgenciaRepository.confirmarReservacion(reservacionId);

        List<Object[]> filas = DatabaseManager.executeQuery(
                "SELECT EstadoID, Fecha_Expiracion FROM Reservacion WHERE ID = ?",
                rs -> new Object[]{rs.getInt("EstadoID"), rs.getDate("Fecha_Expiracion")},
                reservacionId
        );

        assertNotNull(filas,
                "La consulta de verificacion no debe retornar null");
        assertFalse(filas.isEmpty(),
                "La consulta de verificacion debe retornar al menos una fila");
        assertEquals(2, ((Number) filas.get(0)[0]).intValue(),
                "El EstadoID debe ser 2 (Confirmada) tras invocar confirmarReservacion");
        assertNull(filas.get(0)[1],
                "Fecha_Expiracion debe ser NULL tras invocar confirmarReservacion");
    }

    /**
     * Verifica que {@link PagoAgenciaRepository#crearFactura} inserte una nueva fila en
     * la tabla {@code Factura} y retorne un ID positivo para datos validos. La factura
     * creada durante este caso se elimina dentro del propio metodo en un bloque
     * {@code finally} para no interferir con la limpieza del {@code @AfterEach}.
     */
    @Test
    @Order(4)
    @DisplayName("4. crearFactura con datos validos retorna ID positivo")
    void crearFactura_datosValidos_retornaIdPositivo() {
        int facturaId = -1;
        try {
            facturaId = pagoAgenciaRepository.crearFactura(
                    reservacionId, "NIT-AGC-TEST", "01001", 750.0);

            assertTrue(facturaId > 0,
                    "crearFactura debe retornar un ID mayor a cero para datos validos");
        } finally {
            if (facturaId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Factura WHERE ID = ?", facturaId);
            }
        }
    }

    /**
     * Verifica que {@link PagoAgenciaRepository#obtenerFactura} retorne un
     * {@link PagoResponseDTO} no nulo con los campos correctos para una factura
     * existente en Oracle. Se inserta directamente una factura antes de la consulta
     * y se elimina en un bloque {@code finally} para no dejar residuos.
     * <p>
     * Se comprueba que el DTO no sea nulo, que el {@code noReservacion} coincida con
     * el numero insertado en el setup y que el {@code nit} coincida con el valor
     * usado en la insercion de la factura.
     * </p>
     */
    @Test
    @Order(5)
    @DisplayName("5. obtenerFactura con factura existente retorna DTO con datos correctos")
    void obtenerFactura_facturaExistente_retornaDtoConDatos() {
        int facturaId = -1;
        try {
            facturaId = DatabaseManager.executeInsertReturnId(
                    "INSERT INTO Factura (ReservacionID, Fecha, NIT, Codigo_Postal, Total) " +
                            "VALUES (?, SYSDATE, ?, ?, ?)",
                    "ID",
                    reservacionId,
                    "NIT-AGC-FACTURA-TEST",
                    "01010",
                    750.0
            );
            Assumptions.assumeTrue(facturaId > 0,
                    "El INSERT de Factura no retorno un ID valido; se omite la prueba");

            PagoResponseDTO dto = pagoAgenciaRepository.obtenerFactura(facturaId);

            assertNotNull(dto,
                    "obtenerFactura no debe retornar null para una factura existente");
            assertNotNull(dto.getNoReservacion(),
                    "El noReservacion del DTO no debe ser null");
            assertEquals(NO_RESERVACION, dto.getNoReservacion(),
                    "El noReservacion del DTO debe coincidir con el numero de reservacion del setup");
            assertNotNull(dto.getNit(),
                    "El nit del DTO no debe ser null");
            assertEquals("NIT-AGC-FACTURA-TEST", dto.getNit(),
                    "El nit del DTO debe coincidir con el NIT usado en la insercion de la factura");
            assertNotNull(dto.getFecha(),
                    "La fecha del DTO no debe ser null");
            assertNotNull(dto.getEstado(),
                    "El estado del DTO no debe ser null");
        } finally {
            if (facturaId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Factura WHERE ID = ?", facturaId);
            }
        }
    }
}
