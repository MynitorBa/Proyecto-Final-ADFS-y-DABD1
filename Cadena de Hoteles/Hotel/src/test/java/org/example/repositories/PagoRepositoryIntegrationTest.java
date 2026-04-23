package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.PagoResponseDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link PagoRepository}.
 * <p>
 * Conecta a Oracle real para verificar las operaciones de pago de reservaciones:
 * obtencion de reservaciones por usuario, confirmacion de reservacion, creacion
 * y consulta de facturas, actualizacion de totales y resolucion de la ciudad
 * asociada a una reservacion a traves de la cadena
 * {@code DetallesReservacion → Habitacion → Hotel → Ciudad}.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene registros maestros existentes en Oracle
 * (Ciudad, Estado, TipoHabitacion, EstadoHabitacion) mediante {@link Assumptions#assumeTrue}
 * y luego inserta en FK-orden: Usuario, Hotel, Habitacion, Reservacion y
 * DetallesReservacion. El {@code @AfterEach} elimina esos registros en FK-orden
 * inverso para no dejar residuos independientemente del resultado de cada caso.
 * </p>
 * <p>
 * Los tests que crean {@code Factura} se encargan de limpiarla dentro del propio
 * metodo con un bloque {@code finally}, ya que la limpieza de Factura depende del
 * ID generado durante la ejecucion del caso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las tablas
 * {@code Ciudad}, {@code Estado}, {@code TipoHabitacion}, {@code EstadoHabitacion},
 * {@code Usuario}, {@code Hotel}, {@code Habitacion}, {@code Reservacion},
 * {@code DetallesReservacion} y {@code Factura} accesibles.
 * </p>
 */
@DisplayName("Integracion: PagoRepository - Procesamiento de pagos y facturas contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PagoRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private PagoRepository pagoRepository;

    /** ID del usuario de prueba insertado en {@code @BeforeEach}. */
    private int usuarioIdInsertado;

    /** ID del hotel de prueba insertado en {@code @BeforeEach}. */
    private int hotelIdInsertado;

    /** ID de la habitacion de prueba insertada en {@code @BeforeEach}. */
    private int habitacionIdInsertada;

    /** ID de la reservacion de prueba insertada en {@code @BeforeEach}. */
    private int reservacionIdInsertada;

    /** ID de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private int ciudadIdReal;

    /** Nombre de la ciudad real obtenida de Oracle en {@code @BeforeEach}. */
    private String ciudadNombreReal;

    /** ID del estado de hotel activo obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoHotelIdReal;

    /** ID del tipo de habitacion obtenido de Oracle en {@code @BeforeEach}. */
    private int tipoHabitacionIdReal;

    /** ID del estado de habitacion obtenido de Oracle en {@code @BeforeEach}. */
    private int estadoHabitacionIdReal;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME       = "test_pago_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO         = "test_pago_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE      = "IT-PAG-001";

    /** Numero de reservacion fijo de la reservacion de prueba. */
    private static final String NO_RESERVACION = "RES-PAGO-TEST-001";

    /**
     * Inicializa el repositorio, obtiene registros maestros existentes en Oracle
     * (Ciudad, Estado de hotel, TipoHabitacion, EstadoHabitacion) y luego inserta
     * en FK-orden: Usuario, Hotel, Habitacion, Reservacion y DetallesReservacion.
     * <p>
     * Si no se pueden obtener los datos maestros o si algun INSERT falla, la prueba
     * completa se omite mediante {@link Assumptions#assumeTrue} para evitar falsos
     * negativos por problemas del entorno de prueba.
     * </p>
     */
    @BeforeEach
    void setUp() {
        pagoRepository = new PagoRepository();

        // 1. Obtiene una Ciudad existente en Oracle
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Ciudad WHERE ROWNUM = 1",
                rs -> new Object[]{rs.getInt("ID"), rs.getString("Nombre")}
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay Ciudad en Oracle; se omite la prueba");
        ciudadIdReal    = (int)    ciudades.get(0)[0];
        ciudadNombreReal = (String) ciudades.get(0)[1];

        // 2. Obtiene un Estado de hotel activo existente en Oracle
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

        // 4. Obtiene un EstadoHabitacion activo existente en Oracle
        List<Integer> estadosHab = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estadosHab.isEmpty(),
                "No hay EstadoHabitacion en Oracle; se omite la prueba");
        estadoHabitacionIdReal = estadosHab.get(0);

        // 5. Inserta el Usuario de prueba
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

        // 6. Inserta el Hotel de prueba
        hotelIdInsertado = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID",
                "Hotel Test Pago",
                "Calle Pago 1",
                "Desc Pago",
                0.0,
                estadoHotelIdReal,
                ciudadIdReal
        );
        Assumptions.assumeTrue(hotelIdInsertado > 0,
                "El INSERT de Hotel no retorno un ID valido; se omite la prueba");

        // 7. Inserta la Habitacion de prueba
        habitacionIdInsertada = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Habitacion (HOTELID, TIPOHABITACIONID, ESTADO_ID, Descripcion) " +
                        "VALUES (?, ?, ?, ?)",
                "ID",
                hotelIdInsertado,
                tipoHabitacionIdReal,
                estadoHabitacionIdReal,
                "Hab Test Pago"
        );
        Assumptions.assumeTrue(habitacionIdInsertada > 0,
                "El INSERT de Habitacion no retorno un ID valido; se omite la prueba");

        // 8. Inserta la Reservacion de prueba con estado Pendiente (EstadoID = 1)
        reservacionIdInsertada = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES (?, ?, 1, ?, SYSDATE, SYSDATE + 1)",
                "ID",
                NO_RESERVACION,
                500.0,
                usuarioIdInsertado
        );
        Assumptions.assumeTrue(reservacionIdInsertada > 0,
                "El INSERT de Reservacion no retorno un ID valido; se omite la prueba");

        // 9. Inserta DetallesReservacion para habilitar la cadena Ciudad → Hotel → Habitacion
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, CantidadPersonas, Total) " +
                        "VALUES (?, ?, TO_DATE('2030-06-01','YYYY-MM-DD'), TO_DATE('2030-06-05','YYYY-MM-DD'), 2, 500.0)",
                reservacionIdInsertada,
                habitacionIdInsertada
        );
    }

    /**
     * Elimina en FK-orden inverso todos los registros insertados durante el setup:
     * primero Factura (si hubiera alguna residual), luego DetallesReservacion,
     * Reservacion, Habitacion, Hotel y finalmente Usuario.
     * Garantiza que ningun caso deje residuos en Oracle.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Factura WHERE ReservacionID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?", reservacionIdInsertada);
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID = ?", habitacionIdInsertada);
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?", hotelIdInsertado);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioIdInsertado);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code obtenerReservacionParaPago} retorne un arreglo no nulo con
     * los campos correctos cuando la reservacion pertenece al usuario indicado.
     * <p>
     * Se comprueba que el arreglo no sea nulo, que el elemento [0] (ID) coincida con
     * el ID insertado, que el elemento [1] (No_Reservacion) sea exactamente
     * {@code "RES-PAGO-TEST-001"} y que el elemento [4] (EstadoID) sea {@code 1}
     * (estado Pendiente con el que se inserto la reservacion).
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerReservacionParaPago con reservacion del usuario retorna datos correctos")
    void obtenerReservacionParaPago_reservacionDelUsuario_retornaDatosCorrectos() {
        Object[] resultado = pagoRepository.obtenerReservacionParaPago(
                reservacionIdInsertada, usuarioIdInsertado);

        assertNotNull(resultado,
                "El arreglo retornado no debe ser null para una reservacion existente del usuario");
        assertEquals(reservacionIdInsertada, ((Number) resultado[0]).intValue(),
                "El elemento [0] debe ser el ID de la reservacion insertada");
        assertEquals(NO_RESERVACION, resultado[1],
                "El elemento [1] debe ser el numero de reservacion exacto");
        assertEquals(1, ((Number) resultado[4]).intValue(),
                "El elemento [4] (EstadoID) debe ser 1 (Pendiente)");
    }

    /**
     * Verifica que {@code obtenerReservacionParaPago} retorne {@code null} cuando el
     * {@code usuarioId} proporcionado no corresponde al propietario de la reservacion.
     * <p>
     * Se usa {@code usuarioId = -999} que no puede coincidir con ningun registro real,
     * garantizando que el filtro {@code r.Usuario_ID = ?} rechace la consulta.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerReservacionParaPago con otro usuarioId retorna null")
    void obtenerReservacionParaPago_otroUsuarioId_retornaNull() {
        Object[] resultado = pagoRepository.obtenerReservacionParaPago(
                reservacionIdInsertada, -999);

        assertNull(resultado,
                "Debe retornar null cuando el usuarioId no corresponde al propietario de la reservacion");
    }

    /**
     * Verifica que {@code confirmarReservacion} actualice el {@code EstadoID} a {@code 2}
     * (Confirmada) y establezca {@code Fecha_Expiracion} a {@code NULL} en Oracle.
     * <p>
     * Tras invocar el metodo se consulta directamente la tabla {@code Reservacion} para
     * leer el {@code EstadoID} y verificar que {@code Fecha_Expiracion} es nulo,
     * confirmando que ambas columnas fueron actualizadas correctamente.
     * </p>
     */
    @Test
    @Order(3)
    @DisplayName("3. confirmarReservacion con estado pendiente cambia a EstadoID 2 y Fecha_Expiracion null")
    void confirmarReservacion_estadoPendiente_cambiaAEstado2() {
        pagoRepository.confirmarReservacion(reservacionIdInsertada);

        List<Object[]> filas = DatabaseManager.executeQuery(
                "SELECT EstadoID, Fecha_Expiracion FROM Reservacion WHERE ID = ?",
                rs -> new Object[]{rs.getInt("EstadoID"), rs.getDate("Fecha_Expiracion")},
                reservacionIdInsertada
        );

        assertFalse(filas.isEmpty(),
                "La consulta de verificacion debe retornar al menos una fila");
        assertEquals(2, ((Number) filas.get(0)[0]).intValue(),
                "El EstadoID debe ser 2 (Confirmada) tras invocar confirmarReservacion");
        assertNull(filas.get(0)[1],
                "Fecha_Expiracion debe ser NULL tras invocar confirmarReservacion");
    }

    /**
     * Verifica que {@code crearFactura} inserte una nueva fila en {@code Factura} y
     * retorne un ID positivo.
     * <p>
     * La factura creada durante este caso se elimina dentro del propio metodo en un
     * bloque {@code finally} para no interferir con la limpieza del {@code @AfterEach}
     * y mantener el esquema consistente.
     * </p>
     */
    @Test
    @Order(4)
    @DisplayName("4. crearFactura con datos validos retorna ID positivo")
    void crearFactura_datosValidos_retornaIdPositivo() {
        int facturaId = -1;
        try {
            facturaId = pagoRepository.crearFactura(
                    reservacionIdInsertada, "NIT-TEST", "01001", 500.0);

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
     * Verifica que {@code obtenerFactura} retorne un {@link PagoResponseDTO} no nulo
     * con los campos correctos para una factura existente en Oracle.
     * <p>
     * Se inserta directamente una factura antes de la consulta y se elimina en un
     * bloque {@code finally} para no dejar residuos. Se comprueba que el DTO no sea
     * nulo, que el {@code noReservacion} coincida con el numero de reservacion
     * insertado en el setup y que el {@code nit} coincida con el valor usado en la
     * insercion.
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
                    reservacionIdInsertada,
                    "NIT-FACTURA-TEST",
                    "01010",
                    500.0
            );
            Assumptions.assumeTrue(facturaId > 0,
                    "El INSERT de Factura no retorno un ID valido; se omite la prueba");

            PagoResponseDTO dto = pagoRepository.obtenerFactura(facturaId);

            assertNotNull(dto,
                    "obtenerFactura no debe retornar null para una factura existente");
            assertEquals(NO_RESERVACION, dto.getNoReservacion(),
                    "El noReservacion del DTO debe coincidir con el numero de reservacion insertado");
            assertEquals("NIT-FACTURA-TEST", dto.getNit(),
                    "El nit del DTO debe coincidir con el NIT usado en la insercion");
        } finally {
            if (facturaId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Factura WHERE ID = ?", facturaId);
            }
        }
    }

    /**
     * Verifica que {@code actualizarTotalReservacion} modifique efectivamente el
     * campo {@code Total} de la reservacion en Oracle al valor indicado.
     * <p>
     * Tras invocar el metodo se consulta directamente la tabla {@code Reservacion}
     * para leer el {@code Total} y confirmar que el UPDATE fue aplicado.
     * </p>
     */
    @Test
    @Order(6)
    @DisplayName("6. actualizarTotalReservacion con nuevo total actualiza correctamente en Oracle")
    void actualizarTotalReservacion_nuevoTotal_actualizaCorrectamente() {
        double nuevoTotal = 250.0;

        pagoRepository.actualizarTotalReservacion(reservacionIdInsertada, nuevoTotal);

        List<Double> totales = DatabaseManager.executeQuery(
                "SELECT Total FROM Reservacion WHERE ID = ?",
                rs -> rs.getDouble("Total"),
                reservacionIdInsertada
        );

        assertFalse(totales.isEmpty(),
                "La consulta de verificacion debe retornar al menos una fila");
        assertEquals(nuevoTotal, totales.get(0), 0.001,
                "El Total de la Reservacion debe reflejar el valor actualizado en Oracle");
    }

    /**
     * Verifica que {@code obtenerCiudadReservacion} retorne el nombre de la ciudad
     * del hotel asociado a la reservacion resolviendo correctamente la cadena
     * {@code DetallesReservacion → Habitacion → Hotel → Ciudad}.
     * <p>
     * El setup de esta suite inserta todos los elementos de esa cadena, por lo que
     * la consulta debe retornar una cadena no nula y no vacia. Se verifica ademas
     * que el nombre retornado coincida con el nombre de la ciudad obtenida en el setup.
     * </p>
     */
    @Test
    @Order(7)
    @DisplayName("7. obtenerCiudadReservacion con detalles y hotel retorna nombre de ciudad")
    void obtenerCiudadReservacion_conDetallesYHotel_retornaNombreCiudad() {
        String ciudad = pagoRepository.obtenerCiudadReservacion(reservacionIdInsertada);

        assertNotNull(ciudad,
                "obtenerCiudadReservacion no debe retornar null cuando la cadena de FK esta completa");
        assertFalse(ciudad.isBlank(),
                "El nombre de ciudad retornado no debe ser una cadena vacia o en blanco");
        assertEquals(ciudadNombreReal, ciudad,
                "El nombre de ciudad debe coincidir con el de la ciudad usada al insertar el hotel");
    }
}
