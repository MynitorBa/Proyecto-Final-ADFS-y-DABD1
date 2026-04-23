package org.example.repositories;

import org.example.data.DatabaseManager;
import org.example.dtos.DownResponseDTO;
import org.example.helpers.PasswordHelper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion para {@link DownsRepository}.
 * <p>
 * Conecta a Oracle real para verificar el ciclo completo de downs sobre
 * comentarios de hotel: consulta, insercion, actualizacion del contador,
 * eliminacion y verificacion de inexistencia.
 * </p>
 * <p>
 * El {@code @BeforeEach} obtiene datos de referencia reales (Ciudad, Estado)
 * e inserta usuario, hotel y comentario de prueba. El {@code @AfterEach}
 * elimina en orden FK-inverso: Downs, Comentario, Hotel, Usuario.
 * </p>
 * <p>
 * Requiere que Oracle este corriendo en {@code localhost:1521/XEPDB1} con las
 * tablas {@code Ciudad}, {@code Estado}, {@code Usuario}, {@code Hotel},
 * {@code Comentario} y {@code Downs} accesibles.
 * </p>
 */
@DisplayName("Integracion: DownsRepository - Downs sobre comentarios contra Oracle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DownsRepositoryIntegrationTest {

    /** Repositorio bajo prueba. */
    private DownsRepository downsRepository;

    /** ID del usuario de prueba. */
    private int usuarioId;

    /** ID del hotel de prueba. */
    private int hotelId;

    /** ID del comentario de prueba. */
    private int comentarioId;

    /** Username fijo del usuario de prueba. */
    private static final String USERNAME  = "test_downs_repo";

    /** Correo fijo del usuario de prueba. */
    private static final String CORREO    = "test_downs_repo@hotel.com";

    /** Pasaporte fijo del usuario de prueba. */
    private static final String PASAPORTE = "IT-DOWNS-001";

    /**
     * Inicializa el repositorio y crea el grafo de datos en Oracle.
     * Realiza pre-limpieza defensiva por identificadores fijos antes de insertar.
     */
    @BeforeEach
    void setUp() {
        downsRepository = new DownsRepository();

        // 0. Limpieza defensiva
        DatabaseManager.executeUpdate(
                "DELETE FROM Downs WHERE Usuario_ID IN " +
                        "(SELECT ID FROM Usuario WHERE Username = ?)",
                USERNAME
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Comentario WHERE HotelID IN " +
                        "(SELECT ID FROM Hotel WHERE Nombre = ?)",
                "Hotel IT Downs"
        );
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE Nombre = ?", "Hotel IT Downs");
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE Username = ?", USERNAME);

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

        // 3. Inserta usuario de prueba
        usuarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Usuario (Username, Correo, Contrasena, Rol_ID, Pasaporte) " +
                        "VALUES (?, ?, ?, 1, ?)",
                "ID", USERNAME, CORREO,
                PasswordHelper.hashear("TestPass123"), PASAPORTE
        );
        Assumptions.assumeTrue(usuarioId > 0,
                "No se pudo insertar el usuario de prueba — se omite la prueba");

        // 4. Inserta hotel de prueba
        hotelId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Hotel (Nombre, Direccion, Descripcion, Rating, EstadoID, CiudadID) " +
                        "VALUES (?, ?, ?, 4.0, ?, ?)",
                "ID", "Hotel IT Downs", "Calle IT Downs 1",
                "Desc IT Downs", estadoId, ciudadId
        );
        Assumptions.assumeTrue(hotelId > 0,
                "No se pudo insertar el hotel de prueba — se omite la prueba");

        // 5. Inserta comentario de prueba (sin resena, con Downs = 0)
        comentarioId = DatabaseManager.executeInsertReturnId(
                "INSERT INTO Comentario (HotelID, Usuario_ID, Contenido, Fecha, Downs) " +
                        "VALUES (?, ?, ?, SYSDATE, 0)",
                "ID", hotelId, usuarioId, "Comentario de prueba IT Downs"
        );
        Assumptions.assumeTrue(comentarioId > 0,
                "No se pudo insertar el comentario de prueba — se omite la prueba");
    }

    /**
     * Elimina en orden FK-inverso: Downs, Comentario, Hotel, Usuario.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.executeUpdate(
                "DELETE FROM Downs WHERE Usuario_ID = ?", usuarioId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Comentario WHERE ID = ?", comentarioId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Hotel WHERE ID = ?", hotelId);
        DatabaseManager.executeUpdate(
                "DELETE FROM Usuario WHERE ID = ?", usuarioId);
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Verifica que {@link DownsRepository#obtenerValorDown} retorne {@code null}
     * cuando el usuario no ha registrado un down sobre el comentario.
     */
    @Test
    @Order(1)
    @DisplayName("1. obtenerValorDown sin down previo retorna null")
    void obtenerValorDown_sinDown_retornaNull() {
        Integer valor = downsRepository.obtenerValorDown(usuarioId, comentarioId);

        assertNull(valor,
                "obtenerValorDown debe retornar null cuando no existe down para ese par usuario-comentario");
    }

    /**
     * Verifica que {@link DownsRepository#insertarDown} registre un down en
     * Oracle sin lanzar excepcion, y que {@link DownsRepository#obtenerValorDown}
     * retorne el valor insertado.
     */
    @Test
    @Order(2)
    @DisplayName("2. insertarDown registra el down y obtenerValorDown lo confirma")
    void insertarDown_registraDown_obtenibleEnOracle() {
        assertDoesNotThrow(
                () -> downsRepository.insertarDown(usuarioId, comentarioId, -1),
                "insertarDown no debe lanzar excepcion con datos validos"
        );

        Integer valor = downsRepository.obtenerValorDown(usuarioId, comentarioId);

        assertNotNull(valor,
                "obtenerValorDown debe retornar el valor del down insertado");
        assertEquals(-1, valor,
                "El valor del down debe ser -1 (el valor insertado)");
    }

    /**
     * Verifica que {@link DownsRepository#obtenerDownsDeUsuario} retorne una
     * lista no nula y que incluya el down de prueba tras insertarlo.
     */
    @Test
    @Order(3)
    @DisplayName("3. obtenerDownsDeUsuario tras insertar down retorna lista con el down")
    void obtenerDownsDeUsuario_trasInsertarDown_retornaListaConDown() {
        downsRepository.insertarDown(usuarioId, comentarioId, -1);

        List<DownResponseDTO> lista = downsRepository.obtenerDownsDeUsuario(usuarioId);

        assertNotNull(lista,
                "obtenerDownsDeUsuario no debe retornar null");
        assertFalse(lista.isEmpty(),
                "La lista de downs debe contener al menos un elemento tras insertar");
        assertEquals(comentarioId, lista.get(0).getComentarioId(),
                "El ComentarioID del down debe coincidir con el comentario de prueba");
    }

    /**
     * Verifica que {@link DownsRepository#obtenerDownsDeUsuarioPorHotel} retorne
     * una lista no nula y filtre correctamente por hotel.
     */
    @Test
    @Order(4)
    @DisplayName("4. obtenerDownsDeUsuarioPorHotel filtra correctamente por hotel")
    void obtenerDownsDeUsuarioPorHotel_filtraPorHotel() {
        downsRepository.insertarDown(usuarioId, comentarioId, -1);

        List<DownResponseDTO> lista =
                downsRepository.obtenerDownsDeUsuarioPorHotel(usuarioId, hotelId);

        assertNotNull(lista,
                "obtenerDownsDeUsuarioPorHotel no debe retornar null");
        assertFalse(lista.isEmpty(),
                "Debe retornar al menos el down insertado para el hotel de prueba");
        assertEquals(hotelId, lista.get(0).getHotelId(),
                "El HotelID del down debe coincidir con el hotel de prueba");
    }

    /**
     * Verifica que {@link DownsRepository#actualizarContadorDown} incremente
     * el campo Downs del comentario en Oracle.
     */
    @Test
    @Order(5)
    @DisplayName("5. actualizarContadorDown incrementa el contador del comentario")
    void actualizarContadorDown_incrementaContador() {
        assertDoesNotThrow(
                () -> downsRepository.actualizarContadorDown(comentarioId, 1),
                "actualizarContadorDown no debe lanzar excepcion"
        );

        List<Integer> contador = DatabaseManager.executeQuery(
                "SELECT Downs FROM Comentario WHERE ID = ?",
                rs -> rs.getInt("Downs"),
                comentarioId
        );
        assertFalse(contador.isEmpty(),
                "El comentario debe existir en Oracle");
        assertEquals(1, contador.get(0),
                "El contador de Downs debe ser 1 tras incrementar desde 0");
    }

    /**
     * Verifica que {@link DownsRepository#eliminarDown} elimine el down de
     * Oracle y que {@link DownsRepository#obtenerValorDown} retorne {@code null}
     * tras la eliminacion.
     */
    @Test
    @Order(6)
    @DisplayName("6. eliminarDown elimina el down y obtenerValorDown retorna null")
    void eliminarDown_eliminaDown_obtenerValorRetornaNull() {
        downsRepository.insertarDown(usuarioId, comentarioId, -1);
        assertNotNull(downsRepository.obtenerValorDown(usuarioId, comentarioId),
                "El down debe existir antes de eliminarlo");

        downsRepository.eliminarDown(usuarioId, comentarioId);

        Integer valorTras = downsRepository.obtenerValorDown(usuarioId, comentarioId);
        assertNull(valorTras,
                "obtenerValorDown debe retornar null tras eliminar el down");
    }
}
