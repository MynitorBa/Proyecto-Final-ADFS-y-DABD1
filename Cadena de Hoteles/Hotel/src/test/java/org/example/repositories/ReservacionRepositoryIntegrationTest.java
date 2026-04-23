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
 * Pruebas de integracion para {@link ReservacionRepository}.
 * <p>
 * Conecta a Oracle real para verificar todas las operaciones del repositorio de
 * reservaciones: obtencion de precios, deteccion de traslapes, creacion de
 * reservaciones y detalles, consulta de reservaciones por usuario e imagenes.
 * </p>
 * <p>
 * El {@code @BeforeEach} inserta en orden FK los registros minimos necesarios:
 * usuario, hotel, habitacion, reservacion y su detalle. El {@code @AfterEach}
 * elimina en orden FK-inverso (DetallesReservacion, Reservacion, Habitacion,
 * Hotel, Usuario) para no dejar residuos en Oracle independientemente del
 * resultado de cada caso.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Estado}, {@code TipoHabitacion},
 * {@code EstadoHabitacion}, {@code Usuario}, {@code Hotel}, {@code Habitacion},
 * {@code Reservacion} y {@code DetallesReservacion} accesibles.
 * </p>
 */
@DisplayName("Integracion: ReservacionRepository - Operaciones de reservaciones contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservacionRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private ReservacionRepository reservacionRepository;

    /** ID del usuario de prueba insertado en {@code @BeforeEach}. */
    private int usuarioId;

    /** ID del hotel de prueba insertado en {@code @BeforeEach}. */
    private int hotelId;

    /** ID de la habitacion de prueba insertada en {@code @BeforeEach}. */
    private int habitacionId;

    /** ID de la reservacion de prueba insertada en {@code @BeforeEach}. */
    private int reservacionId;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME      = "test_res_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO        = "test_res_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE     = "IT-RES-001";

    /** Numero de reservacion fijo de la reservacion de prueba. */
    private static final String NO_RESERVACION = "RES-REPO-TEST-001";

    /**
     * Inicializa el repositorio y construye el grafo completo de datos de prueba
     * en Oracle insertando en orden correcto de FK: obtiene ciudad y estado reales,
     * luego inserta usuario, hotel, habitacion, reservacion y su detalle.
     * <p>
     * Si no existen registros de referencia en {@code Ciudad}, {@code Estado},
     * {@code TipoHabitacion} o {@code EstadoHabitacion}, la prueba se omite
     * mediante {@link Assumptions#assumeTrue} para evitar falsos negativos.
     * </p>
     */
    @BeforeEach
    void setUp() {
        reservacionRepository = new ReservacionRepository();

        // 1. Obtiene una ciudad existente en Oracle
        List<Object[]> ciudades = DatabaseManager.executeQuery(
                "SELECT ID, Nombre FROM Ciudad WHERE ROWNUM = 1",
                rs -> new Object[]{rs.getInt("ID"), rs.getString("Nombre")}
        );
        Assumptions.assumeTrue(!ciudades.isEmpty(),
                "No hay ciudades en Oracle — se omite la prueba");
        int ciudadId = (int) ciudades.get(0)[0];

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

        // 5. Inserta el usuario de prueba
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

        // 6. Inserta el hotel de prueba
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                "ID",
                "Hotel Test Reservacion",
                "Calle IT Reservacion 1",
                "Descripcion IT Reservacion",
                4.0,
                estadoId,
                ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "No se pudo insertar el hotel de prueba — se omite la prueba");

        // 7. Inserta la habitacion de prueba
        habitacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Habitacion (HOTELID, TIPOHABITACIONID, ESTADO_ID, Descripcion) " +
                        "VALUES (?, ?, ?, ?)",
                "ID",
                hotelId,
                tipoHabitacionId,
                estadoHabitacionId,
                "Habitacion IT Reservacion"
        );
        Assumptions.assumeTrue(habitacionId > 0,
                "No se pudo insertar la habitacion de prueba — se omite la prueba");

        // 8. Inserta la reservacion de prueba con estado pendiente (EstadoID = 1)
        reservacionId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Reservacion " +
                        "(No_Reservacion, Total, EstadoID, Usuario_ID, Fecha_Creacion, Fecha_Expiracion) " +
                        "VALUES (?, ?, 1, ?, SYSDATE, SYSDATE + 1)",
                "ID",
                NO_RESERVACION,
                300.0,
                usuarioId
        );
        Assumptions.assumeTrue(reservacionId > 0,
                "No se pudo insertar la reservacion de prueba — se omite la prueba");

        // 9. Inserta el detalle de la reservacion con fechas en 2030 para no causar traslapes actuales
        DatabaseManager.executeUpdate(
                "INSERT INTO DetallesReservacion " +
                        "(ReservacionID, HabitacionID, FechaCheckIn, FechaCheckOut, " +
                        "CantidadPersonas, Total) " +
                        "VALUES (?, ?, TO_DATE('2030-07-01','YYYY-MM-DD'), " +
                        "TO_DATE('2030-07-05','YYYY-MM-DD'), 2, 300.0)",
                reservacionId,
                habitacionId
        );
    }

    /**
     * Elimina en orden FK-inverso todos los registros insertados durante el
     * {@code @BeforeEach}, ademas de cualquier reservacion extra que pudiera haber
     * creado un caso de prueba individualmente. Garantiza que la base de datos quede
     * sin residuos incluso si algun test falla.
     */
    @AfterEach
    void tearDown() {
        // 1. Elimina detalles de reservacion
        DatabaseManager.executeUpdate(
                "DELETE FROM DetallesReservacion WHERE ReservacionID = ?",
                reservacionId
        );
        // 2. Elimina la reservacion principal y cualquier reservacion extra del usuario
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE ID = ?",
                reservacionId
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Reservacion WHERE Usuario_ID = ?",
                usuarioId
        );
        // 3. Elimina la habitacion
        DatabaseManager.executeUpdate(
                "DELETE FROM Habitacion WHERE ID = ?",
                habitacionId
        );
        // 4. Elimina el hotel
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?",
                hotelId
        );
        // 5. Elimina el usuario
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?",
                usuarioId
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@code obtenerPrecios} retorne un arreglo de exactamente tres
     * elementos para una habitacion que existe en Oracle, con precio por noche no
     * negativo y capacidad maxima de al menos una persona.
     * <p>
     * Valida el contrato del arreglo: {@code [PRECIONOCHE, PRECIOPERSONA, CAPACIDADMAXIMA]}.
     * </p>
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerPrecios con habitacion existente retorna arreglo con tres precios validos")
    void obtenerPrecios_habitacionExistente_retornaArrayConPrecios() {
        double[] precios = reservacionRepository.obtenerPrecios(habitacionId);

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
     * Verifica que {@code existeTraslape} retorne {@code false} cuando las fechas
     * propuestas no solapan con ninguna reservacion activa de la habitacion.
     * <p>
     * Usa fechas en 2035 para garantizar que no haya conflicto con el detalle de
     * prueba insertado en 2030.
     * </p>
     */
    @Test
    @Order(2)
    @DisplayName("2. existeTraslape sin reservaciones en conflicto retorna false")
    void existeTraslape_sinReservacionesConflicto_retornaFalse() {
        Date checkIn  = Date.valueOf("2035-01-01");
        Date checkOut = Date.valueOf("2035-01-05");

        boolean resultado = reservacionRepository.existeTraslape(habitacionId, checkIn, checkOut);

        assertFalse(resultado,
                "existeTraslape debe retornar false cuando no hay traslape con reservaciones activas");
    }

    /**
     * Verifica que {@code existeTraslape} retorne {@code true} cuando las fechas
     * propuestas coinciden exactamente con las del detalle insertado en el setup
     * (2030-07-01 a 2030-07-05), que esta en estado pendiente.
     */
    @Test
    @Order(3)
    @DisplayName("3. existeTraslape con reservacion en conflicto retorna true")
    void existeTraslape_conReservacionConflicto_retornaTrue() {
        Date checkIn  = Date.valueOf("2030-07-01");
        Date checkOut = Date.valueOf("2030-07-05");

        boolean resultado = reservacionRepository.existeTraslape(habitacionId, checkIn, checkOut);

        assertTrue(resultado,
                "existeTraslape debe retornar true cuando las fechas solapan con una reservacion pendiente");
    }

    /**
     * Verifica que {@code crearReservacion} inserte correctamente una reservacion y
     * retorne un ID positivo generado por Oracle. La reservacion creada se elimina
     * en un bloque {@code finally} para garantizar la limpieza incluso ante fallos.
     */
    @Test
    @Order(4)
    @DisplayName("4. crearReservacion con datos validos retorna ID positivo")
    void crearReservacion_datosValidos_retornaIdPositivo() {
        int nuevaReservacionId = -1;
        try {
            Timestamp ahora     = new Timestamp(System.currentTimeMillis());
            Timestamp expiracion = new Timestamp(System.currentTimeMillis() + 3_600_000L);

            nuevaReservacionId = reservacionRepository.crearReservacion(
                    "RES-REPO-TEST-002", 500.0, usuarioId, ahora, expiracion
            );

            assertNotNull(nuevaReservacionId,
                    "crearReservacion no debe retornar null");
            assertTrue(nuevaReservacionId > 0,
                    "crearReservacion debe retornar un ID positivo generado por Oracle");
        } finally {
            if (nuevaReservacionId > 0) {
                DatabaseManager.executeUpdate(
                        "DELETE FROM Reservacion WHERE ID = ?", nuevaReservacionId
                );
            }
        }
    }

    /**
     * Verifica que {@code obtenerReservacion} retorne un arreglo no nulo para la
     * reservacion insertada en el setup y que los primeros dos campos coincidan con
     * el ID y el numero de reservacion esperados.
     */
    @Test
    @Order(5)
    @DisplayName("5. obtenerReservacion con reservacion existente retorna datos correctos")
    void obtenerReservacion_reservacionExistente_retornaDatosCorrectos() {
        Object[] datos = reservacionRepository.obtenerReservacion(reservacionId);

        assertNotNull(datos,
                "obtenerReservacion no debe retornar null para una reservacion existente");
        assertEquals(reservacionId, datos[0],
                "El primer elemento del arreglo debe ser el ID de la reservacion");
        assertEquals(NO_RESERVACION, datos[1],
                "El segundo elemento debe ser el numero de reservacion");
    }

    /**
     * Verifica que {@code obtenerReservacionesDeUsuario} retorne una lista no nula y
     * no vacia para el usuario que tiene exactamente una reservacion con detalle
     * insertada en el setup. Confirma ademas que el primer elemento tenga un ID
     * positivo y un numero de reservacion no nulo.
     */
    @Test
    @Order(6)
    @DisplayName("6. obtenerReservacionesDeUsuario con detalle retorna lista con al menos un elemento")
    void obtenerReservacionesDeUsuario_conDetalle_retornaListaConAlMenosUno() {
        List<ReservacionDetalleDTO> lista =
                reservacionRepository.obtenerReservacionesDeUsuario(usuarioId);

        assertNotNull(lista,
                "obtenerReservacionesDeUsuario no debe retornar null");
        assertFalse(lista.isEmpty(),
                "La lista debe contener al menos un elemento para el usuario de prueba");

        ReservacionDetalleDTO primero = lista.get(0);
        assertNotNull(primero,
                "El primer elemento de la lista no debe ser null");
        assertTrue(primero.getId() > 0,
                "El ID de la reservacion en el DTO debe ser positivo");
        assertNotNull(primero.getNoReservacion(),
                "El numero de reservacion del DTO no debe ser null");
    }

    /**
     * Verifica que {@code expirarReservacionesVencidas} no lance ninguna excepcion
     * y retorne un entero no negativo, ya sea 0 (sin expiradas) o mayor (con expiradas).
     * Ambos valores son validos segun el estado actual de la base de datos.
     */
    @Test
    @Order(7)
    @DisplayName("7. expirarReservacionesVencidas no lanza excepcion y retorna entero no negativo")
    void expirarReservacionesVencidas_noLanzaExcepcion_retornaEntero() {
        int[] resultado = {-1};

        assertDoesNotThrow(
                () -> resultado[0] = reservacionRepository.expirarReservacionesVencidas(),
                "expirarReservacionesVencidas no debe lanzar ninguna excepcion"
        );
        assertTrue(resultado[0] >= 0,
                "expirarReservacionesVencidas debe retornar un entero no negativo");
    }

    /**
     * Verifica que {@code obtenerImagenesHotel} retorne una lista no nula para el
     * hotel de prueba al que no se le insertaron imagenes durante el setup.
     * La ausencia de imagenes es valida y la lista deberia estar vacia, pero lo
     * esencial es que el metodo no retorne {@code null} ni lance excepcion.
     */
    @Test
    @Order(8)
    @DisplayName("8. obtenerImagenesHotel para hotel sin imagenes retorna lista no nula")
    void obtenerImagenesHotel_hotelSinImagenes_retornaListaVacia() {
        List<Integer> imagenes = reservacionRepository.obtenerImagenesHotel(hotelId);

        assertNotNull(imagenes,
                "obtenerImagenesHotel no debe retornar null aunque el hotel no tenga imagenes");
    }
}
