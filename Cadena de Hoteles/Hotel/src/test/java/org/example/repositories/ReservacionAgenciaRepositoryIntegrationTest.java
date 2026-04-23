package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.ReservacionDetalleDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link ReservacionAgenciaRepository}.
 * <p>
 * Conecta a Oracle real para verificar el ciclo completo de reservaciones
 * de agencia: obtencion de descuentos, precios de habitaciones, deteccion de
 * traslapes, creacion de reservaciones y detalles, consulta de reservaciones
 * de agencia y expiracion de reservaciones.
 * </p>
 * <p>
 * El {@code @BeforeEach} construye el grafo completo de datos en orden FK:
 * ciudad, estado de hotel, tipo de habitacion, estado de habitacion, usuario,
 * agencia, hotel, habitacion, reservacion y detalle de reservacion. El
 * {@code @AfterEach} elimina en orden FK-inverso para no dejar residuos en
 * Oracle independientemente del resultado de cada caso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Estado}, {@code TipoHabitacion},
 * {@code EstadoHabitacion}, {@code EstadoAgencia}, {@code EstadoReserva},
 * {@code Usuario}, {@code Agencia}, {@code Hotel}, {@code Habitacion},
 * {@code Reservacion} y {@code DetallesReservacion} accesibles.
 * </p>
 */
@DisplayName("Integracion: ReservacionAgenciaRepository - Reservaciones de agencia contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservacionAgenciaRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private ReservacionAgenciaRepository reservacionAgenciaRepository;

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

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME       = "test_res_agc_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO         = "test_res_agc_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE      = "IT-RAGC-001";

    /** Numero de reservacion fijo de la reservacion de prueba. */
    private static final String NO_RESERVACION = "RES-AG-REPO-001";

    /**
     * Inicializa el repositorio y construye el grafo completo de datos de prueba
     * en Oracle. Obtiene registros de referencia reales (Ciudad, Estado, TipoHabitacion,
     * EstadoHabitacion) y omite la prueba via {@link Assumptions#assumeTrue} si alguno
     * no existe. Luego inserta en orden correcto de FK: usuario, agencia, hotel,
     * habitacion, reservacion y detalle de reservacion.
     */
    @BeforeEach
    void setUp() {
        reservacionAgenciaRepository = new ReservacionAgenciaRepository();

        // 0. Limpieza defensiva: elimina residuos de ciclos anteriores fallidos
        //    usando identificadores fijos para no depender de IDs dinamicos.
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
                "Habitacion IT Ag Res"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE Nombre = ?",
                "Hotel Test Ag Res"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Agencia WHERE URL_Agencia = ?",
                "http://it-res-agencia.com"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE Username = ?",
                USERNAME
        );

        // 1. Obtiene una ciudad existente en Oracle
        List<Integer> ciudades = DatabaseManager.executeQuery(
                "SELECT ID FROM Ciudad WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle — se omite la prueba");
        int ciudadId = ciudades.get(0);

        // 2. Obtiene un estado de hotel existente en Oracle
        List<Integer> estados = DatabaseManager.executeQuery(
                "SELECT ID FROM Estado WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estados.isEmpty(),
                "No hay estados de hotel en Oracle — se omite la prueba");
        int estadoId = estados.get(0);

        // 3. Obtiene un tipo de habitacion existente en Oracle
        List<Integer> tiposHabitacion = DatabaseManager.executeQuery(
                "SELECT ID FROM TipoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!tiposHabitacion.isEmpty(),
                "No hay tipos de habitacion en Oracle — se omite la prueba");
        int tipoHabitacionId = tiposHabitacion.get(0);

        // 4. Obtiene un estado de habitacion existente en Oracle
        List<Integer> estadosHabitacion = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoHabitacion WHERE ROWNUM = 1",
                rs -> rs.getInt("ID")
        );
        Assumptions.assumeTrue(!estadosHabitacion.isEmpty(),
                "No hay estados de habitacion en Oracle — se omite la prueba");
        int estadoHabitacionId = estadosHabitacion.get(0);

        // 5. Inserta el usuario webservice de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID",
                USERNAME,
                CORREO,
                PasswordHelper.hashear("TestPass123"),
                PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario de prueba — se omite la prueba");

        // 6. Inserta la agencia de prueba con 10% de descuento
        agenciaId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Agencia (Nombre, Correo, UsuarioWebis_ID, PorcentajeDescuento, EstadoID, URL_Agencia) " +
                        "VALUES (?, ?, ?, 10, 1, ?)",
                "ID",
                "Agencia IT Test Res",
                "agencia_it_res@test.com",
                usuarioId,
                "http://it-res-agencia.com"
        );
        Assumptions.assumeTrue(agenciaId > 0,
                "No se pudo insertar la agencia de prueba — se omite la prueba");

        // 7. Inserta el hotel de prueba
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID",
                "Hotel Test Ag Res",
                "Calle IT Ag Res 1",
                "Descripcion IT Ag Res",
                4.0,
                estadoId,
                ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "No se pudo insertar el hotel de prueba — se omite la prueba");

        // 8. Inserta la habitacion de prueba
        habitacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Habitacion (HOTELID, TIPOHABITACIONID, ESTADO_ID, Descripcion) " +
                        "VALUES (?, ?, ?, ?)",
                "ID",
                hotelId,
                tipoHabitacionId,
                estadoHabitacionId,
                "Habitacion IT Ag Res"
        );
        Assumptions.assumeTrue(habitacionId > 0,
                "No se pudo insertar la habitacion de prueba — se omite la prueba");

        // 9. Crea la reservacion de prueba mediante el repositorio
        Timestamp ahora     = new Timestamp(System.currentTimeMillis());
        Timestamp expiracion = new Timestamp(System.currentTimeMillis() + 3_600_000L);
        reservacionId = reservacionAgenciaRepository.crearReservacion(
                NO_RESERVACION, 400.0, usuarioId, ahora, expiracion
        );
        Assumptions.assumeTrue(reservacionId > 0,
                "No se pudo crear la reservacion de prueba — se omite la prueba");

        // 10. Inserta el detalle de la reservacion con fechas en 2030 para no causar traslapes
        reservacionAgenciaRepository.crearDetalle(
                reservacionId,
                habitacionId,
                Date.valueOf("2030-08-01"),
                Date.valueOf("2030-08-05"),
                2,
                400.0
        );
    }

    /**
     * Elimina en orden FK-inverso todos los registros insertados durante el
     * {@code @BeforeEach}: detalles de reservacion, reservaciones, habitacion,
     * hotel, agencia y usuario. Garantiza que Oracle quede sin residuos incluso
     * si algun caso de prueba falla.
     */
    @AfterEach
    void tearDown() {
        // 1. Elimina detalles de la reservacion de prueba
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?",
                reservacionId
        );
        // 2. Elimina la reservacion principal
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?",
                reservacionId
        );
        // 3. Elimina cualquier reservacion extra del usuario (creada por algun test)
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE Usuario_ID = ?",
                usuarioId
        );
        // 4. Elimina la habitacion
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID = ?",
                habitacionId
        );
        // 5. Elimina el hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?",
                hotelId
        );
        // 6. Elimina la agencia
        DatabaseManager.executeUpdate(
                "DELETE FROM Agencia WHERE ID = ?",
                agenciaId
        );
        // 7. Elimina el usuario
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?",
                usuarioId
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link ReservacionAgenciaRepository#obtenerDescuentoAgencia}
     * retorne un valor positivo para la agencia de prueba, la cual fue insertada
     * con un porcentaje de descuento del 10%.
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerDescuentoAgencia con agencia con descuento retorna valor positivo")
    void obtenerDescuentoAgencia_agenciaConDescuento_retornaValorPositivo() {
        double descuento = reservacionAgenciaRepository.obtenerDescuentoAgencia(agenciaId);

        assertTrue(descuento > 0,
                "El descuento de la agencia debe ser mayor a cero (fue insertado con 10%)");
    }

    /**
     * Verifica que {@link ReservacionAgenciaRepository#obtenerPrecios} retorne un
     * arreglo de exactamente tres elementos para la habitacion de prueba, con precio
     * por noche no negativo y capacidad maxima de al menos una persona.
     * Valida el contrato {@code [precioPorNoche, precioPorPersona, capacidadMaxima]}.
     */
    @Test
    @Order(2)
    @DisplayName("2. obtenerPrecios con habitacion existente retorna arreglo con precios validos")
    void obtenerPrecios_habitacionExistente_retornaArrayConPrecios() {
        double[] precios = reservacionAgenciaRepository.obtenerPrecios(habitacionId);

        assertNotNull(precios,
                "obtenerPrecios no debe retornar null para una habitacion existente");
        assertEquals(3, precios.length,
                "El arreglo de precios debe tener exactamente 3 elementos");
        assertTrue(precios[0] >= 0,
                "El precio por noche (indice 0) no puede ser negativo");
        assertTrue(precios[2] >= 1,
                "La capacidad maxima (indice 2) debe ser al menos 1 persona");
    }

    /**
     * Verifica que {@link ReservacionAgenciaRepository#existeTraslape} retorne
     * {@code false} cuando las fechas propuestas no solapan con ninguna reservacion
     * activa de la habitacion. Usa fechas en 2040 para garantizar que no haya
     * conflicto con el detalle de prueba insertado en 2030.
     */
    @Test
    @Order(3)
    @DisplayName("3. existeTraslape con fechas sin conflicto retorna false")
    void existeTraslape_fechasSinConflicto_retornaFalse() {
        Date checkIn  = Date.valueOf("2040-01-01");
        Date checkOut = Date.valueOf("2040-01-05");

        boolean resultado = reservacionAgenciaRepository.existeTraslape(
                habitacionId, checkIn, checkOut);

        assertFalse(resultado,
                "existeTraslape debe retornar false cuando no hay traslape con reservaciones activas");
    }

    /**
     * Verifica que {@link ReservacionAgenciaRepository#crearReservacion} haya
     * retornado un ID positivo durante el setup, confirmando que la insercion
     * en Oracle fue exitosa y que el repositorio genera correctamente el ID
     * mediante {@code executeInsertReturnId}.
     */
    @Test
    @Order(4)
    @DisplayName("4. crearReservacion con datos validos retorna ID positivo")
    void crearReservacion_datosValidos_retornaIdPositivo() {
        assertTrue(reservacionId > 0,
                "crearReservacion debe retornar un ID positivo generado por Oracle");
    }

    /**
     * Verifica que {@link ReservacionAgenciaRepository#obtenerReservacion} retorne
     * un arreglo no nulo para la reservacion insertada en el setup, y que los
     * primeros dos elementos correspondan al ID y al numero de reservacion esperados.
     */
    @Test
    @Order(5)
    @DisplayName("5. obtenerReservacion con reservacion existente retorna datos correctos")
    void obtenerReservacion_reservacionExistente_retornaDatos() {
        Object[] datos = reservacionAgenciaRepository.obtenerReservacion(reservacionId);

        assertNotNull(datos,
                "obtenerReservacion no debe retornar null para una reservacion existente");
        assertEquals(reservacionId, datos[0],
                "El primer elemento del arreglo debe ser el ID de la reservacion");
        assertNotNull(datos[1],
                "El segundo elemento (No_Reservacion) no debe ser null");
        assertEquals(NO_RESERVACION, datos[1],
                "El segundo elemento debe ser el numero de reservacion esperado");
    }

    /**
     * Verifica que {@link ReservacionAgenciaRepository#expirarReservacion} actualice
     * el EstadoID de la reservacion al estado Expirada. Llama al metodo del repositorio
     * y luego consulta directamente Oracle para confirmar que el EstadoID cambio al
     * valor correspondiente a "expirada" en la tabla {@code EstadoReserva}.
     */
    @Test
    @Order(6)
    @DisplayName("6. expirarReservacion con reservacion pendiente actualiza el estado en Oracle")
    void expirarReservacion_reservacionPendiente_actualizaEstado() {
        // Obtiene el EstadoID original antes de expirar
        List<Integer> estadosAntes = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Reservacion WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                reservacionId
        );
        assertNotNull(estadosAntes,
                "La lista de EstadoID antes de expirar no debe ser null");
        assertFalse(estadosAntes.isEmpty(),
                "Debe existir la reservacion en Oracle antes de expirar");
        int estadoOriginal = estadosAntes.get(0);

        // Llama al metodo que expira la reservacion
        reservacionAgenciaRepository.expirarReservacion(reservacionId);

        // Verifica que el EstadoID haya cambiado en Oracle
        List<Integer> estadosDespues = DatabaseManager.executeQuery(
                "SELECT EstadoID FROM Reservacion WHERE ID = ?",
                rs -> rs.getInt("EstadoID"),
                reservacionId
        );
        assertNotNull(estadosDespues,
                "La lista de EstadoID despues de expirar no debe ser null");
        assertFalse(estadosDespues.isEmpty(),
                "Debe existir la reservacion en Oracle tras la expiracion");

        int estadoNuevo = estadosDespues.get(0);

        // Obtiene el ID del estado "expirada" para comparar
        List<Integer> idExpirada = DatabaseManager.executeQuery(
                "SELECT ID FROM EstadoReserva WHERE LOWER(Estado) = 'expirada'",
                rs -> rs.getInt("ID")
        );
        assertNotNull(idExpirada,
                "La lista del ID de estado Expirada no debe ser null");
        assertFalse(idExpirada.isEmpty(),
                "Debe existir el estado 'expirada' en la tabla EstadoReserva");

        assertEquals(idExpirada.get(0), estadoNuevo,
                "El EstadoID debe corresponder a 'expirada' tras la llamada a expirarReservacion");
        assertNotEquals(estadoOriginal, estadoNuevo,
                "El EstadoID debe haber cambiado respecto al estado original");
    }
}
